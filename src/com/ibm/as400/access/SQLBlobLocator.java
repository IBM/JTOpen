///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLBlobLocator.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.*;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;


class SQLBlobLocator implements SQLLocator
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


  private AS400JDBCConnection     connection_;
  private ConvTable               converter_;
  private int                     id_;
  private JDLobLocator            locator_;
  private int                     maxLength_;
  private SQLConversionSettings   settings_;
  private int                     truncated_;
  private int                     columnIndex_;

  private Object savedObject_; // This is the AS400JDBCBlobLocator or InputStream or whatever got set into us.
  private int scale_; // This is actually the length that got set into us.


  SQLBlobLocator(AS400JDBCConnection connection,
                 int id,
                 int maxLength, 
                 SQLConversionSettings settings,
                 ConvTable converter,
                 int columnIndex)
  {
    connection_     = connection;
    id_             = id;
    locator_        = new JDLobLocator(connection, id, maxLength, false); //@CRS - We know it's not graphic, because we are not a DBClob.
    maxLength_      = maxLength;
    settings_       = settings;
    truncated_      = 0;
    converter_      = converter;
    columnIndex_    = columnIndex;
  }



  public Object clone()
  {
    return new SQLBlobLocator(connection_, id_, maxLength_, settings_, converter_, columnIndex_);
  }



  public void setHandle(int handle)      
  {
    locator_.setHandle(handle);          
  }                                      



//---------------------------------------------------------//
//                                                         //
// CONVERSION TO AND FROM RAW BYTES                        //
//                                                         //
//---------------------------------------------------------//



  public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) throws SQLException
  {
    int locatorHandle = BinaryConverter.byteArrayToInt(rawBytes, offset);
    locator_.setHandle(locatorHandle);
    locator_.setColumnIndex(columnIndex_);
  }


  //@CRS - This is only called from AS400JDBCPreparedStatement in one place.
  public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) throws SQLException
  {
    BinaryConverter.intToByteArray(locator_.getHandle(), rawBytes, offset);

    // Now we write our saved data to the server, because the prepared statement is being executed.
    // We used to write the data to the server on the call to set(), but this messed up
    // batch executes, since the host server only reserves temporary space for locator handles one row at a time.
    // See the toObject() method in this class for more details.
    writeToServer();
  }



//---------------------------------------------------------//
//                                                         //
// SET METHODS                                             //
//                                                         //
//---------------------------------------------------------//

  // This method actually writes the data to the server.
  private void writeToServer() throws SQLException
  {
    if (savedObject_ instanceof byte[])
    {
      byte[] bytes = (byte[])savedObject_;        
      locator_.writeData(0, bytes);
    }
    else if (savedObject_ instanceof String)
    {
      String s = (String)savedObject_;
      locator_.writeData(0, SQLBinary.stringToBytes(s));
    }
    else if (savedObject_ instanceof Reader)
    {
      int length = scale_; // hack to get the length into the set method
      if (length > 0)
      {
        try
        {
          int blockSize = length < AS400JDBCPreparedStatement.LOB_BLOCK_SIZE ? length : AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
          HexReaderInputStream stream = new HexReaderInputStream((Reader)savedObject_);
          byte[] byteBuffer = new byte[blockSize];
          int totalBytesRead = 0;
          int bytesRead = stream.read(byteBuffer, 0, blockSize);
          while (bytesRead > -1 && totalBytesRead < length)
          {
            locator_.writeData(totalBytesRead, byteBuffer, 0, bytesRead); // totalBytesRead is our offset.
            totalBytesRead += bytesRead;
            int bytesRemaining = length - totalBytesRead;
            if (bytesRemaining < blockSize)
            {
              blockSize = bytesRemaining;
            }
            bytesRead = stream.read(byteBuffer, 0, blockSize);
          }
        }
        catch (IOException ie)
        {
          JDError.throwSQLException(JDError.EXC_INTERNAL, ie);
        }
      }
      else
      {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
      }
    }
    else if (savedObject_ instanceof InputStream)
    {
      int length = scale_; // hack to get the length into the set method
      if (length > 0)
      {
        InputStream stream = (InputStream)savedObject_;
        int blockSize = length < AS400JDBCPreparedStatement.LOB_BLOCK_SIZE ? length : AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
        byte[] byteBuffer = new byte[blockSize];
        try
        {
          int totalBytesRead = 0;
          int bytesRead = stream.read(byteBuffer, 0, blockSize);
          while (bytesRead > -1 && totalBytesRead < length)
          {
            locator_.writeData(totalBytesRead, byteBuffer, 0, bytesRead); // totalBytesRead is our offset.
            totalBytesRead += bytesRead;
            int bytesRemaining = length - totalBytesRead;
            if (bytesRemaining < blockSize)
            {
              blockSize = bytesRemaining;
            }
            bytesRead = stream.read(byteBuffer, 0, blockSize);
          }
        }
        catch (IOException ie)
        {
          JDError.throwSQLException(JDError.EXC_INTERNAL, ie);
        }
      }
      else
      {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
      }
    }
    else if (JDUtilities.JDBCLevel_ >= 20 && savedObject_ instanceof Blob) //@H0A check for jdbc level to know if lobs exist
    {
      // @A1C
      //@G5A Start new code for updateable locator case to go through the Vectors 
      //@G5A and update the blob copy when ResultSet.updateBlob() is called.
      boolean set = false;
      if (savedObject_ instanceof AS400JDBCBlobLocator)
      {
        AS400JDBCBlobLocator blob = (AS400JDBCBlobLocator)savedObject_;

        //Synchronize on a lock so that the user can't keep making updates
        //to the blob while we are taking updates off the vectors.
        synchronized (blob)
        {
          // See if we saved off our real object from earlier.
          if (blob.savedObject_ != null)
          {
            savedObject_ = blob.savedObject_;
            blob.savedObject_ = null;
            writeToServer();
            return;
          }


/*          Vector positionsToStartUpdates = blob.getPositionsToStartUpdates();
          if (positionsToStartUpdates != null)
          {
            Vector bytesToUpdate = blob.getBytesToUpdate();
            for (int i = 0; i < positionsToStartUpdates.size(); i++)
            {
              long startPosition = ((Long)positionsToStartUpdates.elementAt(i)).longValue();
              byte[] updateBytes = (byte[])bytesToUpdate.elementAt(i);
              locator_.writeData(startPosition, updateBytes);
            }
            // If writeData calls do not throw an exception, update has been successfully made.
            positionsToStartUpdates = null;
            bytesToUpdate = null;
            set = true;
          }
*/          
        }
      }

      //@G5A If the code for updateable lob locators did not run, then run old code.
      if (!set)
      {
        Blob blob = (Blob)savedObject_;                                      // @A1C
        int length = (int)blob.length();
        byte[] data = blob.getBytes(1, length);
        locator_.writeData(0, data, 0, length);
      }
    }
    else
    {
      JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    }
  }

  public void set(Object object, Calendar calendar, int scale) throws SQLException
  {
    // If it's a byte[] we can check for data truncation.
    if (object instanceof byte[])
    {
      byte[] bytes = (byte[])object;
      truncated_ = (bytes.length > maxLength_ ? bytes.length-maxLength_ : 0);
    }
    else if (!(object instanceof String) &&
             !(object instanceof Blob) &&
             !(object instanceof Reader) &&
             !(object instanceof InputStream))
    {
      JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    }
    savedObject_ = object;
    if (scale != -1) scale_ = scale; // Skip resetting it if we don't know the real length
  }



//---------------------------------------------------------//
//                                                         //
// DESCRIPTION OF SQL TYPE                                 //
//                                                         //
//---------------------------------------------------------//



  public String getCreateParameters ()
  {
    return AS400JDBCDriver.getResource ("MAXLENGTH"); 
  }



  public int getDisplaySize ()
  {
    return maxLength_;
  }


  //@D1A JDBC 3.0
  public String getJavaClassName()
  {
    return "com.ibm.as400.access.AS400JDBCBlobLocator";    
  }


  public String getLiteralPrefix ()
  {
    return null;
  }



  public String getLiteralSuffix ()
  {
    return null;
  }



  public String getLocalName ()
  {
    return "BLOB"; 
  }



  public int getMaximumPrecision ()
  {
    return 15728640;
  }



  public int getMaximumScale ()
  {
    return 0;
  }



  public int getMinimumScale ()
  {
    return 0;
  }



  public int getNativeType ()
  {
    return 960;
  }



  public int getPrecision ()
  {
    return maxLength_;
  }


  public int getRadix ()
  {
    return 0;
  }


  public int getScale ()
  {
    return 0;
  }


  public int getType ()
  {
    return java.sql.Types.BLOB;
  }



  public String getTypeName ()
  {
    return "BLOB"; 
  }



  public boolean isSigned ()
  {
    return false;
  }



  public boolean isText ()
  {
    return true;
  }



//---------------------------------------------------------//
//                                                         //
// CONVERSIONS TO JAVA TYPES                               //
//                                                         //
//---------------------------------------------------------//



  public int getActualSize()
  {
    return maxLength_;
  }



  public int getTruncated()
  {
    return truncated_;
  }



  public InputStream toAsciiStream() throws SQLException
  {
    return new AS400JDBCInputStream(new JDLobLocator(locator_));
  }



  public BigDecimal toBigDecimal (int scale)
  throws SQLException
  {
    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }



  public InputStream toBinaryStream() throws SQLException
  {
    return new AS400JDBCInputStream(new JDLobLocator(locator_));
  }



  public Blob toBlob() throws SQLException
  {
    // We don't want to give out our internal locator to the public,
    // otherwise when we go to change its handle on the next row, they'll
    // get confused.  So we have to clone it.
    return new AS400JDBCBlobLocator(new JDLobLocator(locator_), savedObject_);
  }



  public boolean toBoolean ()
  throws SQLException
  {
    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    return false;
  }



  public byte toByte ()
  throws SQLException
  {
    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    return -1;
  }



  public byte[] toBytes() throws SQLException
  {
    int locatorLength = (int)locator_.getLength();
    if (locatorLength == 0) return new byte[0];
    DBLobData data = locator_.retrieveData(0, locatorLength);
    int actualLength = data.getLength();                                                // @C2A
    byte[] bytes = new byte[actualLength];                                              // @C2A
    System.arraycopy(data.getRawBytes(), data.getOffset(), bytes, 0, actualLength);     // @C2A
    return bytes;                                                                       // @C2A
  }



  public Reader toCharacterStream() throws SQLException
  {
    return new InputStreamReader(new AS400JDBCInputStream(new JDLobLocator(locator_)));
  }



  public Clob toClob() throws SQLException
  {
    return new AS400JDBCClobLocator(new JDLobLocator(locator_), connection_.converter_, savedObject_);
  }



  public Date toDate (Calendar calendar)
  throws SQLException
  {
    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }



  public double toDouble ()
  throws SQLException
  {
    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    return -1;
  }



  public float toFloat ()
  throws SQLException
  {
    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    return -1;
  }



  public int toInt ()
  throws SQLException
  {
    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    return -1;
  }



  public long toLong ()
  throws SQLException
  {
    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    return -1;
  }



  public Object toObject()
  {
    // toObject is used by AS400JDBCPreparedStatement for batching, so we save off our InputStream
    // inside the AS400JDBCBlobLocator. Then, when convertToRawBytes() is called, the writeToServer()
    // code checks the AS400JDBCBlobLocator's saved InputStream... if it exists, then it writes the
    // data out of the InputStream to the server by calling writeToServer() again.

    // Since toObject could also be called from an external user's standpoint, we have
    // to clone our internal locator (because we reuse it internally).
    // This doesn't make much sense, since we technically can't reuse it because
    // the prepared statement is calling toObject() to store off the parameters,
    // but it's all we can do for now.
    return new AS400JDBCBlobLocator(new JDLobLocator(locator_), savedObject_);
  }



  public short toShort ()
  throws SQLException
  {
    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    return -1;
  }



  public String toString()
  {
    return super.toString();
  }



  public Time toTime (Calendar calendar)
  throws SQLException
  {
    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }



  public Timestamp toTimestamp (Calendar calendar)
  throws SQLException
  {
    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }



  public InputStream toUnicodeStream() throws SQLException
  {
    return new AS400JDBCInputStream(new JDLobLocator(locator_));
  }


}

