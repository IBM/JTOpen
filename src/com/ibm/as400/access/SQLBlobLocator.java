///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLBlobLocator.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2017 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.*;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
/* ifdef JDBC40 
import java.sql.NClob;
import java.sql.RowId;
endif */ 
import java.sql.SQLException;
/* ifdef JDBC40 
import java.sql.SQLXML;
endif */ 
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;


final class SQLBlobLocator extends SQLLocatorBase
{
    static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";

    private AS400JDBCConnection     connection_;
    private ConvTable               converter_;
    private int                     id_;
    private JDLobLocator            locator_;
    private int                     maxLength_;
    private SQLConversionSettings   settings_;
    private int                     truncated_;
    private boolean outOfBounds_ = false; 

    private int                     columnIndex_;
    private byte[]                  value_; 
    //@loch //Note that value_ is not used as the output for a ResultSet.getX() call.
    // We Get the value from a call to the JDLocator (not from value_) and not 
    // from the savedObject_, unless resultSet.updateX(obj1) is called followed by 
    // a obj2 = resultSet.getX()

    private Object savedObject_; // This is the AS400JDBCBlobLocator or InputStream or whatever got set into us.
    private int scale_; // This is actually the length that got set into us.
    private boolean savedObjectWrittenToServer_ = false; 
    private Object savedValue_;

    private boolean inputStreamRead_ = false;   // Has the input stream been read yet. 
    
  SQLBlobLocator(AS400JDBCConnection connection, int id, int maxLength,
      SQLConversionSettings settings, ConvTable converter, int columnIndex) {
    connection_ = connection;
    id_ = id;
    locator_ = new JDLobLocator(connection, id, maxLength, false); // @CRS - We
                                                                   // know it's
                                                                   // not
                                                                   // graphic,
                                                                   // because we
                                                                   // are not a
                                                                   // DBClob.
    // Check for maxLength of 0. This may happen for a locator parameter of a
    // stored procedure
    // If zero, then set to maximum length @F3A
    if (maxLength == 0) {
      maxLength_ = 2147483647;
    } else {
      maxLength_ = maxLength;
    }

    settings_ = settings;
    truncated_ = 0;
    outOfBounds_ = false;
    converter_ = converter;
    columnIndex_ = columnIndex;
  }

  public Object clone() {
    return new SQLBlobLocator(connection_, id_, maxLength_, settings_,
        converter_, columnIndex_);
  }

  public void setHandle(int handle) {
    locator_.setHandle(handle);
    // @T1A Reset saved object
    savedObject_ = null;
    savedObjectWrittenToServer_ = false;
    value_ = null;
    savedValue_ = null;
  }
    
  // @loch
  public int getHandle() {
    return locator_.getHandle();
  }

  // ---------------------------------------------------------//
  // //
  // CONVERSION TO AND FROM RAW BYTES //
  // //
  // ---------------------------------------------------------//

  public void convertFromRawBytes(byte[] rawBytes, int offset,
      ConvTable converter) throws SQLException {
    convertFromRawBytes(rawBytes, offset, converter, false);
  }

  // Use to set the locator value received from the server 
  public void convertFromRawBytes(byte[] rawBytes, int offset,
      ConvTable ccsidConverter, boolean ignoreConversionErrors)
      throws SQLException {
    int locatorHandle = BinaryConverter.byteArrayToInt(rawBytes, offset);
    locator_.setHandle(locatorHandle);
    locator_.setColumnIndex(columnIndex_);
    // @T1A Reset saved object
    savedObject_ = null;
    savedObjectWrittenToServer_ = false;
    value_ = null;
    savedValue_ = null;
  }

  // @CRS - This is only called from AS400JDBCPreparedStatement in one place.
  public void convertToRawBytes(byte[] rawBytes, int offset,
      ConvTable ccsidConverter) throws SQLException {
    BinaryConverter.intToByteArray(locator_.getHandle(), rawBytes, offset);

    // Now we write our saved data to the system, because the prepared statement
    // is being executed.
    // We used to write the data to the system on the call to set(), but this
    // messed up
    // batch executes, since the host server only reserves temporary space for
    // locator handles one row at a time.
    // See the toObject() method in this class for more details.
    if (((!savedObjectWrittenToServer_) && (savedObject_ != null))
        || (value_ != null))
      writeToServer();

  }

  /**
  validates that raw truncated data is correct.  The data is corrected if is not correct. 
  This is only used when converting to MIXED CCSID and UTF-8. 
  @param  rawBytes         the raw bytes for the system.
  @param  offset           the offset into the byte array.
  @param  ccsidConverter   the converter.
   * @throws SQLException  If a database error occurs.
  **/ 
  public void validateRawTruncatedData(byte[] rawBytes, int offset, ConvTable ccsidConverter) {
    // Most data type do not need to validate truncated data.  Just return. 
    
  }

    //---------------------------------------------------------//
    //                                                         //
    // SET METHODS                                             //
    //                                                         //
    //---------------------------------------------------------//

    public void set(Object object, Calendar calendar, int scale)
    throws SQLException
    {
        // If it's a byte[] we can check for data truncation.
        if(object instanceof byte[])
        {
            byte[] bytes = (byte[])object;
            truncated_ = (bytes.length > maxLength_ ? bytes.length-maxLength_ : 0);
        }
        else if(object instanceof String)
        {
            byte[] bytes = null;
            try
            {
                bytes = BinaryConverter.stringToBytes((String)object);
            }
            catch(NumberFormatException nfe)
            {
                // the String contains non-hex characters
                JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, nfe);
            }
            object = bytes;
            truncated_ = 0; outOfBounds_ = false; 
        }
        else if(object instanceof Reader)
        {
                    object = new HexReaderInputStream((Reader)object);
        }
        else if(!(object instanceof String) &&
                ( !(object instanceof Blob)) &&
                !(object instanceof Reader) &&
                !(object instanceof InputStream))
        {
          if (JDTrace.isTraceOn()) {
              if (object == null) { 
                  JDTrace.logInformation(this, "Unable to assign null object");
                } else { 
                    JDTrace.logInformation(this, "Unable to assign object("+object+") of class("+object.getClass().toString()+")");
                }
          }

            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        }
        savedObject_ = object;
        savedObjectWrittenToServer_ = false;    
      if(scale != -1) scale_ = scale; // Skip resetting it if we don't know the real length
    }

    //@loch method to temporary convert from object input to output before even going to host (writeToServer() does the conversion needed before writting to host)
    //This will only be used when resultSet.updateX(obj1) is called followed by a obj2 = resultSet.getX()
    //Purpose is to do a local type conversion from obj1 to obj2 like other non-locator lob types
    // This function converts the savedObject_ to  value_. 
    
    private void doConversion()
    throws SQLException
    {
        try
        {
            Object object = savedObject_;
            if(object instanceof byte[])
            {
                value_ = (byte[]) object;
                int objectLength = value_.length;
                if(value_.length > maxLength_)
                {
                    byte[] newValue = new byte[maxLength_];
                    System.arraycopy(value_, 0, newValue, 0, maxLength_);
                    value_ = newValue;
                }
                truncated_ = objectLength - value_.length;
            }
            else if( object instanceof Blob)
            {
              // Handle the update path 
            	if (savedObject_ instanceof AS400JDBCBlobLocator) {
                AS400JDBCBlobLocator blob = (AS400JDBCBlobLocator) savedObject_;

                // Synchronize on a lock so that the user can't keep making updates
                // to the blob while we are taking updates off the vectors.
                synchronized (blob) {
                  // See if we saved off our real object from earlier.
                  if (blob.savedObject_ != null) {
                    savedObject_ = blob.savedObject_;
                    savedObjectWrittenToServer_ = false;
                    scale_ = blob.savedScale_;
                    blob.savedObject_ = null;
                    if  (!( savedObject_ instanceof AS400JDBCBlobLocator)) {
                        doConversion(); 
                        return;
                    }
                  }
                }
              }
                Blob blob = (Blob) object;
                int blobLength = (int)blob.length();
                int lengthToUse = blobLength < 0 ? 0x7FFFFFFF : blobLength;
                if(lengthToUse > maxLength_) lengthToUse = maxLength_;
                value_ = blob.getBytes(1, lengthToUse);
                truncated_ = blobLength - lengthToUse;
            }
            else if(object instanceof InputStream)
            {
              if (inputStreamRead_ ) {
                // Value should already be set
              } else { 

                int length = scale_; // hack to get the length into the set method
                if(length >= 0)
                {
                  
                    value_ = readInputStream((InputStream)object, length,null, false);
                    
                    if(value_.length < length)
                    {
                        // a length longer than the stream was specified
                        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                    }

                    int objectLength = value_.length;
                    if(value_.length > maxLength_)
                    {
                        byte[] newValue = new byte[maxLength_];
                        System.arraycopy(value_, 0, newValue, 0, maxLength_);
                        value_ = newValue;
                    }
                    truncated_ = objectLength - value_.length;
                }
                else if(length == -2) //@readerlen new else-if block (read all data)
                {
                    value_ = readInputStream((InputStream)object, length, null, false); 
                    inputStreamRead_ = true; 
                    int objectLength = value_.length;
                    if(value_.length > maxLength_)
                    {
                        byte[] newValue = new byte[maxLength_];
                        System.arraycopy(value_, 0, newValue, 0, maxLength_);
                        value_ = newValue;
                    }
                    truncated_ = objectLength - value_.length;
                }
                else
                {
                    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
                }
              }
            }
            else
            {
                JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
            }
            
        }
        finally
        {
            //nothing
        }
    }
    

    
  // This method actually writes the data to the system.
  private void writeToServer() throws SQLException {
    // Write the value if we already read it.
    if (value_ != null) {
      locator_.writeData(0, value_, true);
    } else if (savedObject_ instanceof byte[]) {
      byte[] bytes = (byte[]) savedObject_;
      locator_.writeData(0, bytes, true); // @K1A
    } else if (savedObject_ instanceof InputStream) {
      int length = scale_; // hack to get the length into the set method

      // Need to write even if there are 0 bytes in case we are batching and
      // the host server reuses the same handle for the previous locator;
      // otherwise,
      // we'll have data in the current row from the previous row.
      if (length == 0) {
        locator_.writeData(0, new byte[0], 0, 0, true); // @K1A
      } else if (length > 0) {
        value_ = readInputStream((InputStream) savedObject_, length, locator_, false);
        if (value_.length < length) {
          // a length longer than the stream was specified
          JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        }

      } else if (length == -2) // @readerlen new else-if block (read all data)
      {
        value_ = readInputStream((InputStream) savedObject_, length, locator_, false);
      } else {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
      }
    } else if (savedObject_ instanceof Blob) // @H0A check for jdbc level to
                                             // know if lobs exist
    {
      // @A1C
      // @G5A Start new code for updateable locator case to go through the
      // Vectors
      // @G5A and update the blob copy when ResultSet.updateBlob() is called.
      boolean set = false;
      if (savedObject_ instanceof AS400JDBCBlobLocator) {
        AS400JDBCBlobLocator blob = (AS400JDBCBlobLocator) savedObject_;

        // Synchronize on a lock so that the user can't keep making updates
        // to the blob while we are taking updates off the vectors.
        synchronized (blob) {
          // See if we saved off our real object from earlier.
          if (blob.savedObject_ != null) {
            savedObject_ = blob.savedObject_;
            savedObjectWrittenToServer_ = false;
            scale_ = blob.savedScale_;
            blob.savedObject_ = null;
            writeToServer();
            return;
          }
        }
      }

      // @G5A If the code for updateable lob locators did not run, then run old
      // code.
      if (!set) {
        Blob blob = (Blob) savedObject_; // @A1C
        int length = (int) blob.length();
        byte[] data = blob.getBytes(1, length);
        locator_.writeData(0, data, 0, length, true); // @K1A
      }
    } else {
      JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    }

  }

    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public int getSQLType()
    {
        return SQLData.BLOB_LOCATOR;
    }

    public String getCreateParameters()
    {
        return AS400JDBCDriver.getResource("MAXLENGTH",null); 
    }

    public int getDisplaySize()
    {
        return maxLength_;
    }

    //@D1A JDBC 3.0
    public String getJavaClassName()
    {
        return "com.ibm.as400.access.AS400JDBCBlobLocator";    
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
        return "BLOB"; 
    }

    public int getMaximumPrecision()
    {
        return AS400JDBCDatabaseMetaData.MAX_LOB_LENGTH; //@xml3 // the DB2 SQL reference says this should be 2147483647 but we return 1 less to allow for NOT NULL columns
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
        return 960;
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
        return java.sql.Types.BLOB;
    }

    public String getTypeName()
    {
        return "BLOB"; 
    }

    public boolean isSigned()
    {
        return false;
    }

    public boolean isText()
    {
        return true;
    }

    public int getActualSize()
    {
        return maxLength_;
    }

    public int getTruncated()
    {
        return truncated_;
    }
    public void clearTruncated() {
      truncated_ = 0; 
    }
    public boolean getOutOfBounds() {
      return outOfBounds_; 
    }
    public void clearOutOfBounds() { 
      outOfBounds_ = false; 
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSIONS TO JAVA TYPES                               //
    //                                                         //
    //---------------------------------------------------------//

    public InputStream getAsciiStream()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        //return new AS400JDBCInputStream(new JDLobLocator(locator_));

        // fix this to use a Stream
        try
        {
            return new ByteArrayInputStream(ConvTable.getTable(819, null).stringToByteArray(BinaryConverter.bytesToHexString(getBytes())));
        }
        catch(UnsupportedEncodingException e)
        {
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
            return null;
        }
    }

    public BigDecimal getBigDecimal(int scale)
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public InputStream getBinaryStream()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        return new AS400JDBCInputStream(new JDLobLocator(locator_));
    }

    public Blob getBlob()
    throws SQLException
    {
        if(savedObject_ != null)//@loch
        {                       //@loch
            //get value from RS.updateX(value)
            doConversion();     //@loch
            truncated_ = 0; outOfBounds_ = false;      //@loch
            return new AS400JDBCBlob(value_, maxLength_); //@loch
        }                       //@loch
        
        // We don't want to give out our internal locator to the public,
        // otherwise when we go to change its handle on the next row, they'll
        // get confused.  So we have to clone it.
        truncated_ = 0; outOfBounds_ = false; 
        return new AS400JDBCBlobLocator(new JDLobLocator(locator_), savedObject_, scale_);
    }

    public boolean getBoolean()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return false;
    }

    public byte getByte()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public byte[] getBytes()
    throws SQLException
    {
        if(savedObject_ != null)//@loch
        {                       //@loch
            //get value from RS.updateX(value)
            doConversion();     //@loch
            truncated_ = 0; outOfBounds_ = false;      //@loch
            return value_;//@loch
        }                       //@loch
        
        int locatorLength = (int)locator_.getLength();
        if(locatorLength == 0) return new byte[0];
        DBLobData data = locator_.retrieveData(0, locatorLength);
        int actualLength = data.getLength();
        byte[] bytes = new byte[actualLength];
        System.arraycopy(data.getRawBytes(), data.getOffset(), bytes, 0, actualLength);
        truncated_ = 0; outOfBounds_ = false; 
        return bytes;
    }

    public Reader getCharacterStream()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        //return new InputStreamReader(new AS400JDBCInputStream(new JDLobLocator(locator_)));
        
        // fix this to use a Stream
        return new StringReader(BinaryConverter.bytesToHexString(getBytes()));
    }

  public Clob getClob() throws SQLException {
    truncated_ = 0;
    outOfBounds_ = false;
    String string = BinaryConverter.bytesToHexString(getBytes());
    return new AS400JDBCClob(string, string.length());
  }

  public Date getDate(Calendar calendar) throws SQLException {
    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  public double getDouble() throws SQLException {
    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    return -1;
  }

  public float getFloat() throws SQLException {
    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    return -1;
  }

  public int getInt() throws SQLException {
    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    return -1;
  }

  public long getLong() throws SQLException {
    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    return -1;
  }

  public Object getObject() throws SQLException {
    // toObject is used by AS400JDBCPreparedStatement for batching, so we save
    // off our InputStream
    // inside the AS400JDBCBlobLocator. Then, when convertToRawBytes() is
    // called, the writeToServer()
    // code checks the AS400JDBCBlobLocator's saved InputStream... if it exists,
    // then it writes the
    // data out of the InputStream to the system by calling writeToServer()
    // again.

    // Since toObject could also be called from an external user's standpoint,
    // we have
    // to clone our internal locator (because we reuse it internally).
    // This doesn't make much sense, since we technically can't reuse it because
    // the prepared statement is calling toObject() to store off the parameters,
    // but it's all we can do for now.
    truncated_ = 0;
    outOfBounds_ = false;
    return new AS400JDBCBlobLocator(new JDLobLocator(locator_), savedObject_,
        scale_);
  }

  public Object getBatchableObject() throws SQLException {
    return getObject();
  }

  public short getShort() throws SQLException {
    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    return -1;
  }

  public String getString() throws SQLException {
    truncated_ = 0;
    outOfBounds_ = false;
    return BinaryConverter.bytesToHexString(getBytes());
  }

  public Time getTime(Calendar calendar) throws SQLException {
    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  public Timestamp getTimestamp(Calendar calendar) throws SQLException {
    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  public InputStream getUnicodeStream() throws SQLException {
    truncated_ = 0;
    outOfBounds_ = false;
    // return new AS400JDBCInputStream(new JDLobLocator(locator_));

    // fix this to use a Stream
    try {
      return new ByteArrayInputStream(ConvTable.getTable(13488, null)
          .stringToByteArray(BinaryConverter.bytesToHexString(getBytes())));
    } catch (UnsupportedEncodingException e) {
      JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
      return null;
    }
  }
    
  // @PDA jdbc40
  public Reader getNCharacterStream() throws SQLException {
    truncated_ = 0;
    outOfBounds_ = false; // @PDC
    return new StringReader(BinaryConverter.bytesToHexString(getBytes())); // @PDC
  }

    //@PDA jdbc40
    /* ifdef JDBC40 

    public NClob getNClob() throws SQLException
    {        
        truncated_ = 0; outOfBounds_ = false; 
        String string = BinaryConverter.bytesToHexString(getBytes());//@pdc
        return new AS400JDBCNClob(string, string.length()); //@pdc
    }
endif */ 
  // @PDA jdbc40
  public String getNString() throws SQLException {
    truncated_ = 0;
    outOfBounds_ = false; // @pdc
    return BinaryConverter.bytesToHexString(getBytes()); // @pdc
  }

    /* ifdef JDBC40 

    //@PDA jdbc40
    public RowId getRowId() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
endif */ 

    /* ifdef JDBC40 

    //@PDA jdbc40
    public SQLXML getSQLXML() throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        if(savedObject_ != null)//@loch
        {                       //@loch
            //get value from RS.updateX(value)
            doConversion();     //@loch
            truncated_ = 0; outOfBounds_ = false;      //@loch
            return new AS400JDBCSQLXML(value_, value_.length);//@loch
        }                       //@loch
        
        String string = BinaryConverter.bytesToHexString(getBytes());
        //return new AS400JDBCSQLXML(string, string.length());
        return new AS400JDBCSQLXMLLocator(new JDLobLocator(locator_), savedObject_, scale_); //@xml3
    }
    
endif */ 
    // @array
  public Array getArray() throws SQLException {
    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }

  public void updateSettings(SQLConversionSettings settings) {
    settings_ = settings;
  }

  public void saveValue() throws SQLException {
    if (value_ == null && savedObject_ != null) {
      doConversion();
    }

    savedValue_ = value_;
  }

  public Object getSavedValue() {

    return savedValue_;
  }    
   
}

