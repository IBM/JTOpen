///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLDBClobLocator.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
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

class SQLDBClobLocator implements SQLLocator
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

  private AS400JDBCConnection     connection_;
  private ConvTable               converter_; //@P0C
  private int                     id_;
  private JDLobLocator            locator_;
  private int                     maxLength_;
  private SQLConversionSettings   settings_;
  private int                     truncated_;
  private int                     columnIndex_;   //@E3A

  private Object savedObject_; // This is the AS400JDBCBlobLocator or InputStream or whatever got set into us.
  private int scale_; // This is actually the length that got set into us.

  SQLDBClobLocator(AS400JDBCConnection connection,
                   int id,
                   int maxLength, 
                   SQLConversionSettings settings,
                   ConvTable converter,
                   int columnIndex)
  {
    connection_     = connection;
    id_             = id;
    locator_        = new JDLobLocator(connection, id, maxLength, true);
    maxLength_      = maxLength;
    settings_       = settings;
    truncated_      = 0;
    converter_      = converter;
    columnIndex_    = columnIndex;
  }

  public Object clone()
  {
    return new SQLDBClobLocator(connection_, id_, maxLength_, settings_, converter_, columnIndex_);
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

  public void set(Object object, Calendar calendar, int scale) throws SQLException
  {
    if (!(object instanceof String) &&
        !(object instanceof Reader) &&
        !(object instanceof InputStream) &&
        (JDUtilities.JDBCLevel_ >= 20 && !(object instanceof Clob)))
    {
      JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    }

    savedObject_ = object;
    if (scale != -1) scale_ = scale; // Skip resetting it if we don't know the real length
  }


  private void writeToServer() throws SQLException
  {
    Object object = savedObject_;
    if (object instanceof String)
    {                                                 // @B3A
      String string = (String)object;                                            // @B3A
      byte[] bytes = converter_.stringToByteArray(string);                       // @B3A
      locator_.writeData(0L, bytes);                             // @B3A @E2C
    }                                                                               // @B3A
    else if (object instanceof Reader)
    {
      int length = scale_*2; // We are always graphic.
      if (length >= 0)
      {
        try
        {
          int blockSize = length < AS400JDBCPreparedStatement.LOB_BLOCK_SIZE ? length : AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
          int bidiStringType = settings_.getBidiStringType();
          if (bidiStringType == -1) bidiStringType = converter_.bidiStringType_;
          ReaderInputStream stream = new ReaderInputStream((Reader)savedObject_, converter_.getCcsid(), bidiStringType, blockSize);
          byte[] byteBuffer = new byte[blockSize];
          int totalBytesRead = 0;
          int bytesRead = stream.read(byteBuffer, 0, blockSize);
          while (bytesRead > -1 && totalBytesRead < length)
          {
            locator_.writeData((long)totalBytesRead, byteBuffer, 0, bytesRead); // totalBytesRead is our offset.
            totalBytesRead += bytesRead;
            int bytesRemaining = length - totalBytesRead;
            if (bytesRemaining < blockSize)
            {
              blockSize = bytesRemaining;
              if (stream.available() == 0 && blockSize != 0)
              {
                stream = new ReaderInputStream((Reader)savedObject_, converter_.getCcsid(), bidiStringType, blockSize); // do this so we don't read more chars out of the Reader than we have to.
              }
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
    else if (object instanceof InputStream)
    {
      int length = scale_*2; // We are always graphic.
      if (length >= 0)
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
            locator_.writeData((long)totalBytesRead, byteBuffer, 0, bytesRead); // totalBytesRead is our offset.
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
    else if (JDUtilities.JDBCLevel_ >= 20 && object instanceof Clob) //@H0A check for jdbc level to know if lobs exist
    {
      //@G5A Start new code for updateable locator case
      boolean set = false;
      if (object instanceof AS400JDBCClobLocator)
      {
        AS400JDBCClobLocator clob = (AS400JDBCClobLocator)object;

        //Synchronize on a lock so that the user can't keep making updates
        //to the clob while we are taking updates off the vectors.
        synchronized (clob)
        {
          // See if we saved off our real object from earlier.
          if (clob.savedObject_ != null)
          {
            savedObject_ = clob.savedObject_;
            scale_ = clob.savedScale_;
            clob.savedObject_ = null;
            writeToServer();
            return;
          }
        }
      }
      if (!set)
      {
        Clob clob = (Clob)object;
        int length = (int)clob.length();
        int blockSize = AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
        if (length < blockSize) blockSize = length;
        int position = 1;
        AS400JDBCClobLocator thisClob = (AS400JDBCClobLocator)toClob();
        while (position <= length)
        {
          String substring = clob.getSubString(position, blockSize);
          thisClob.setString(position, substring);
          position += blockSize;
          if ((length - position) < blockSize)
          {
            blockSize = length - position + 1;
          }
        }
//        locator_.writeData(0L, converter_.stringToByteArray(substring), 0, length);
        set = true;
      }
      else
      {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
      }
    }
    else
    {
      JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    }
  }



  //---------------------------------------------------------//
  //                                                         //
  // DESCRIPTION OF SQL TYPE                                 //
  //                                                         //
  //---------------------------------------------------------//



  public String getCreateParameters()
  {
    return AS400JDBCDriver.getResource("MAXLENGTH"); 
  }

  public int getDisplaySize()
  {
    return maxLength_ / 2;
  }


  public String getJavaClassName()
  {
    return "com.ibm.as400.access.AS400JDBCClobLocator";   
  }

  public String getLiteralPrefix()
  {
    return null;
  }

  public String getLiteralSuffix()
  {
    return null;
  }

  public String getLocalName()
  {
    return "DBCLOB"; 
  }

  public int getMaximumPrecision()
  {
    return 15728640;
  }

  public int getMaximumScale()
  {
    return 0;
  }

  public int getMinimumScale()
  {
    return 0;
  }

  public int getNativeType()
  {
    return 968;
  }

  public int getPrecision()
  {
    return maxLength_;
  }

  public int getRadix()
  {
    return 0;
  }

  public int getScale()
  {
    return 0;
  }

  public int getType()
  {
    return java.sql.Types.CLOB;
  }

  public String getTypeName()
  {
    return "DBCLOB";
  }

  public boolean isSigned()
  {
    return false;
  }

  public boolean isText()
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
    return 0;
  }

  public InputStream toAsciiStream() throws SQLException
  {
    try
    {
      return new ReaderInputStream(new ConvTableReader(new AS400JDBCInputStream(new JDLobLocator(locator_)), converter_.getCcsid()), 819); // ISO-8859-1.
    }
    catch (UnsupportedEncodingException e)
    {
      JDError.throwSQLException(JDError.EXC_INTERNAL, e);
      return null;
    }
  }

  public BigDecimal toBigDecimal(int scale)
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  public InputStream toBinaryStream()
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  public Blob toBlob()
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  public boolean toBoolean()
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return false;
  }

  public byte toByte()
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return 0;
  }

  public byte[] toBytes()
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  public Reader toCharacterStream() throws SQLException
  {
    try
    {
      return new ConvTableReader(new AS400JDBCInputStream(new JDLobLocator(locator_)), converter_.getCcsid());
    }
    catch (UnsupportedEncodingException e)
    {
      JDError.throwSQLException(JDError.EXC_INTERNAL, e);
      return null;
    }
  }

  public Clob toClob() throws SQLException
  {
    return new AS400JDBCClobLocator(new JDLobLocator(locator_), converter_, savedObject_, scale_);
  }

  public Date toDate(Calendar calendar)
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  public double toDouble()
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return 0;
  }

  public float toFloat()
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return 0;
  }

  public int toInt()
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return 0;
  }

  public long toLong()
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return 0;
  }

  public Object toObject()
  {
    // toObject is used by AS400JDBCPreparedStatement for batching, so we save off our InputStream
    // inside the AS400JDBCClobLocator. Then, when convertToRawBytes() is called, the writeToServer()
    // code checks the AS400JDBCClobLocator's saved InputStream... if it exists, then it writes the
    // data out of the InputStream to the server by calling writeToServer() again.
    return new AS400JDBCClobLocator(new JDLobLocator(locator_), converter_, savedObject_, scale_);
  }

  public short toShort()
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return 0;
  }

  public String toString()
  {
    try
    {
      Clob c = toClob();
      return c.getSubString(1L, (int)c.length());
/*      DBLobData data = locator_.retrieveData(0, locator_.getMaxLength());        // @C0A
      String value = converter_.byteArrayToString(data.getRawBytes(),           // @C0A
                                                  data.getOffset(),             // @C0A
                                                  data.getLength());            // @C0A
      return value;                                                               // @C0A
*/      
    }                                                                               // @C0A
    catch (SQLException e)
    {                                                        // @C0A
      // toString() should not throw exceptions!                                  // @C0A
      return super.toString();
    }                                                                               // @C0A
  }

  public Time toTime(Calendar calendar)
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  public Timestamp toTimestamp(Calendar calendar)
  throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  public InputStream toUnicodeStream() throws SQLException
  {
    try
    {
      return new ReaderInputStream(new ConvTableReader(new AS400JDBCInputStream(locator_), converter_.getCcsid()), 13488);
    }
    catch (UnsupportedEncodingException e)
    {
      JDError.throwSQLException(JDError.EXC_INTERNAL, e);
      return null;
    }
  }
}

