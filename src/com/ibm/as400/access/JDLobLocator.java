///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDLobLocator.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;



/**
 * The JDLobLocator class provides access to large objects via a locator.
**/
//
// Implementation note:
//
// LOBs in OS/400 were 15MB max until a V5R2 PTF, when they became
// 2GB max. This is OK, since the size can still be stored in a Java int
// without worrying about the sign.
//
// Note: A "LOB-character" refers to a one-byte value in the case of a BLOB or CLOB, 
// and a two-byte value in the case of a DBCLOB.
//
class JDLobLocator
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";


  private AS400JDBCConnection     connection_;
  private boolean                 dataCompression_;        
  private int                     id_;
  private int                     handle_             = -1;
  private long                    length_             = -1; // The current length in LOB-characters.
  private int                     maxLength_; // The max length in LOB-characters.
  private int                     columnIndex_        = -1;
  private boolean                 graphic_;        


  /**
   * Constructs an JDLobLocator object.  
   * @param  connection              The connection to the server.
   * @param  id                      The correlation ID used for any subsequent LOB datastreams (@CRS - why is this here?).
   * @param  maxLength               The max length in bytes on the server.
  **/
  JDLobLocator(AS400JDBCConnection connection, int id, int maxLength, boolean graphic)
  {
    connection_      = connection;
    id_              = id;
    maxLength_       = maxLength;
    dataCompression_ = connection_.getDataCompression() == AS400JDBCConnection.DATA_COMPRESSION_OLD_;
    graphic_ = graphic;
  }


  // This is used by the SQLLocator classes to clone the locator so that the internal
  // copy of the locator (that gets reused) does not get handed to the user. If that
  // were to happen, the next time a statement went to update the locator object with
  // a new handle id, the user's locator would all of a sudden be pointing to the
  // wrong data on the server.
  JDLobLocator(JDLobLocator loc)
  {
    connection_ = loc.connection_;
    id_ = loc.id_;
    maxLength_ = loc.maxLength_;
    dataCompression_ = loc.dataCompression_;
    graphic_ = loc.graphic_;
    handle_ = loc.handle_;
    length_ = loc.length_;
    columnIndex_ = loc.columnIndex_;
  }


  /**
   * Returns the locator handle.
   * @return The locator handle, or -1 if not set.
  **/
  int getHandle()
  {
    return handle_;
  }


  /**
   * Returns the length of this LOB in LOB-characters (the length returned from the server).
   * For BLOBs and CLOBs (single/mixed) this is the same as the number of bytes.
   * For DBCLOBs, this is half the number of bytes.
  **/
  synchronized long getLength() throws SQLException
  {
    if (length_ < 0) // Re-retrieve it.
    {
      try
      {
        DBSQLRequestDS request = null;                                                        
        DBReplyRequestedDS reply = null;                                                      
        try
        {
          request = DBDSPool.getDBSQLRequestDS(DBSQLRequestDS.FUNCTIONID_RETRIEVE_LOB_DATA,   
                                               id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA    
                                               + DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);  
          request.setLOBLocatorHandle(handle_);                                               
          request.setRequestedSize(0);                                                        
          request.setStartOffset(0);                                                          
          request.setCompressionIndicator(dataCompression_ ? 0xF1 : 0xF0);                    
          request.setReturnCurrentLengthIndicator(0xF1);                                      
          if (columnIndex_ != -1)
          {
            request.setColumnIndex(columnIndex_);                                             
          }
          reply = connection_.sendAndReceive(request, id_); //@CRS: Why are we reusing the correlation ID here?
          int errorClass = reply.getErrorClass();                                             
          int returnCode = reply.getReturnCode();                                             

          if (errorClass != 0)
            JDError.throwSQLException(this, connection_, id_, errorClass, returnCode);

          length_ = reply.getCurrentLOBLength();                                              
        }
        finally
        {
          if (request != null) request.inUse_ = false;
          if (reply != null) reply.inUse_ = false;
        }                                                                                     
      }
      catch (DBDataStreamException e)
      {
        JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);                             
      }
    }
    return length_;
  }


  /**
   * Returns the max size of this locator column in LOB-characters.
  **/
  int getMaxLength()
  {
    return maxLength_;
  }


/**
Retrieves part of the contents of the lob.

@param  offset      The offset within the LOB, in LOB-characters.
@param  length      The number of LOB-characters to read from the LOB.
@return             The contents.

@exception  SQLException    If the position is not valid,
                            if the length is not valid,
                            or an error occurs.
**/
  synchronized DBLobData retrieveData(long offset, int length) throws SQLException
  {
    if (offset < 0 || length < 0) JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);

    if (offset >= getMaxLength()) JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);

    // The DB host server currently only supports 4-byte integers for length and offset on the request.
    if (offset > 0x7FFFFFFF) offset = 0x7FFFFFFF;

    try
    {
      DBSQLRequestDS request = null;
      DBReplyRequestedDS reply = null;
      try
      {
        request = DBDSPool.getDBSQLRequestDS(DBSQLRequestDS.FUNCTIONID_RETRIEVE_LOB_DATA,
                                             id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA
                                             + DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);
        request.setLOBLocatorHandle(handle_);
        request.setRequestedSize(length);
        request.setStartOffset((int)offset); // Some day the server will support 8-byte offsets.
        request.setCompressionIndicator(dataCompression_ ? 0xF1 : 0xF0);
        request.setReturnCurrentLengthIndicator(0xF1);
        // If a column index has not been set for this locator, then do not pass
        // the optional column index parameter to the server.
        if (columnIndex_ != -1)
        {
          request.setColumnIndex(columnIndex_);
        }

        if (JDTrace.isTraceOn())
        {
          JDTrace.logInformation(connection_, "Retrieving lob data from handle: " + handle_ + 
                                 " bytesToRead: " + length + " startingOffset: " + offset +
                                 " dataCompression: " + dataCompression_ + " columnIndex: " + columnIndex_);
        }

        reply = connection_.sendAndReceive(request, id_);
        int errorClass = reply.getErrorClass();
        int returnCode = reply.getReturnCode();

        if (errorClass != 0) JDError.throwSQLException(this, connection_, id_, errorClass, returnCode);

        length_ = reply.getCurrentLOBLength();

        DBLobData lobData = reply.getLOBData();

        if (graphic_)
        {
          lobData.adjustForGraphic();   
        }

        return lobData;
      }
      finally
      {
        if (request != null) request.inUse_ = false;
        if (reply != null) reply.inUse_ = false;
      }
    }
    catch (DBDataStreamException e)
    {
      JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
      return null;
    }
  }


/**
Sets the column index.

@param handle The column index.
**/
  void setColumnIndex(int columnIndex)
  {
    columnIndex_ = columnIndex;
  }



/**
Sets the locator handle.

@param handle The locator handle.
**/
  synchronized void setHandle(int handle)
  {
    handle_ = handle;
    length_ = -1;
  }


  int writeData(long offset, byte data, boolean truncate) throws SQLException               //@K1C
  {
    return writeData(offset, new byte[] { data}, 0, 1, truncate);                           //@K1C
  }


  int writeData(long offset, byte[] data, boolean truncate) throws SQLException             //@K1C
  {
    return writeData(offset, data, 0, data.length, truncate);                               //@k1C
  }


/**
Writes part of the contents of the lob.

@param  lobOffset   The offset (in LOB-characters) within the lob.
@param  data        The data to write.
@param  offset      The offset into the byte array from which to copy data.
@param  length      The number of bytes out of the byte array to write.

@exception  SQLException    If the position is not valid,
                            if the length is not valid,
                            or an error occurs.
**/
  synchronized int writeData(long lobOffset, byte[] data, int offset, int length, boolean truncate) throws SQLException     //@K1C
  {
    if (data == null) throw new NullPointerException("data");

    if ((lobOffset < 0) || (length < 0)) JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);

    // The DB host server currently only supports 4-byte integers for the offset on the request.
    // Note that we can keep the length as a 4-byte integer because Java does not support
    // using a long as a byte[] index, so the most data we could ever send at a time would
    // be 2 GB.
    if (lobOffset > 0x7FFFFFFF) lobOffset = 0x7FFFFFFF;

    // If we are a DBCLOB, the data in the byte array is already double-byte data,
    // but we need to tell the server that the number of characters we're writing is
    // half of that (that is, we need to tell it the number of LOB-characters). 
    // The lobOffset is still the right offset, in terms of LOB-characters.
    int lengthToUse = graphic_ ? length / 2 : length;

    try
    {
      DBSQLRequestDS request = null;
      DBReplyRequestedDS reply = null;
      try
      {
        request = DBDSPool.getDBSQLRequestDS(DBSQLRequestDS.FUNCTIONID_WRITE_LOB_DATA,
                                             id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA
                                             + DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);

        request.setLobTruncation(truncate);          //Do not truncate   @K1A
        request.setLOBLocatorHandle(handle_);
        request.setRequestedSize(lengthToUse);
        request.setStartOffset((int)lobOffset); // Some day the server will support 8-byte offsets.
        request.setCompressionIndicator(0xF0); // No compression for now.
        request.setLOBData(data, offset, length);
        if (JDTrace.isTraceOn())
        {
          JDTrace.logInformation(connection_, "Writing lob data to handle: " + handle_ + " offset: " + lobOffset + " length: " + length);
        }

        reply = connection_.sendAndReceive(request, id_);
        int errorClass = reply.getErrorClass();
        int returnCode = reply.getReturnCode();

        if (errorClass != 0)
        {
          JDError.throwSQLException(this, connection_, id_, errorClass, returnCode);
        }
        length_ = -1; //@CRS - We could probably re-calculate it, but for now, we force another call to the server.
        return length;
      }
      finally
      {
        if (request != null) request.inUse_ = false;
        if (reply != null) reply.inUse_ = false;
      }
    }
    catch (DBDataStreamException e)
    {
      JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
    }
    return -1;
  }


  boolean isGraphic()
  {
    return graphic_;
  }
}
