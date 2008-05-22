///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLBlobLocator.java
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

final class SQLBlobLocator implements SQLLocator
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    private AS400JDBCConnection     connection_;
    private ConvTable               converter_;
    private int                     id_;
    private JDLobLocator            locator_;
    private int                     maxLength_;
    private SQLConversionSettings   settings_;
    private int                     truncated_;
    private int                     columnIndex_;
    private byte[]                  value_; //@loch //Note that value_ is not used as the output for a ResultSet.getX() call.  We Get the value from a call to the JDLocator (not from value_) and not from the savedObject_, unless resultSet.updateX(obj1) is called followed by a obj2 = resultSet.getX()

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
    
    //@loch
    public int getHandle()
    {
        return locator_.getHandle();
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
    throws SQLException
    {
        int locatorHandle = BinaryConverter.byteArrayToInt(rawBytes, offset);
        locator_.setHandle(locatorHandle);
        locator_.setColumnIndex(columnIndex_);
    }

    //@CRS - This is only called from AS400JDBCPreparedStatement in one place.
    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
    throws SQLException
    {
        BinaryConverter.intToByteArray(locator_.getHandle(), rawBytes, offset);

        // Now we write our saved data to the system, because the prepared statement is being executed.
        // We used to write the data to the system on the call to set(), but this messed up
        // batch executes, since the host server only reserves temporary space for locator handles one row at a time.
        // See the toObject() method in this class for more details.
        if(savedObject_ != null) writeToServer();
    }

    //---------------------------------------------------------//
    //                                                         //
    // SET METHODS                                             //
    //                                                         //
    //---------------------------------------------------------//

    // This method actually writes the data to the system.
    private void writeToServer()
    throws SQLException
    {
        if(savedObject_ instanceof byte[])
        {
            byte[] bytes = (byte[])savedObject_;        
            locator_.writeData(0, bytes, true);         //@K1A
        }
        else if(savedObject_ instanceof InputStream)
        {
            int length = scale_; // hack to get the length into the set method
            
            // Need to write even if there are 0 bytes in case we are batching and
            // the host server reuses the same handle for the previous locator; otherwise,
            // we'll have data in the current row from the previous row.
            if (length == 0) 
            {
              locator_.writeData(0, new byte[0], 0, 0, true);       //@K1A
            }
            else if(length > 0)
            {
                InputStream stream = (InputStream)savedObject_;
                int blockSize = length < AS400JDBCPreparedStatement.LOB_BLOCK_SIZE ? length : AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
                byte[] byteBuffer = new byte[blockSize];
                try
                {
                    int totalBytesRead = 0;
                    int bytesRead = stream.read(byteBuffer, 0, blockSize);
                    while(bytesRead > -1 && totalBytesRead < length)
                    {
                        locator_.writeData(totalBytesRead, byteBuffer, 0, bytesRead, true); // totalBytesRead is our offset.  @K1A
                        totalBytesRead += bytesRead;
                        int bytesRemaining = length - totalBytesRead;
                        if(bytesRemaining < blockSize)
                        {
                            blockSize = bytesRemaining;
                        }
                        bytesRead = stream.read(byteBuffer, 0, blockSize);
                    }

                    if(totalBytesRead < length)
                    {
                        // a length longer than the stream was specified
                        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                    }
                }
                catch(IOException ie)
                {
                    JDError.throwSQLException(this, JDError.EXC_INTERNAL, ie);
                }
            }
            else
            {
                JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
            }
        }
        else if(JDUtilities.JDBCLevel_ >= 20 && savedObject_ instanceof Blob) //@H0A check for jdbc level to know if lobs exist
        {
            // @A1C
            //@G5A Start new code for updateable locator case to go through the Vectors 
            //@G5A and update the blob copy when ResultSet.updateBlob() is called.
            boolean set = false;
            if(savedObject_ instanceof AS400JDBCBlobLocator)
            {
                AS400JDBCBlobLocator blob = (AS400JDBCBlobLocator)savedObject_;

                //Synchronize on a lock so that the user can't keep making updates
                //to the blob while we are taking updates off the vectors.
                synchronized(blob)
                {
                    // See if we saved off our real object from earlier.
                    if(blob.savedObject_ != null)
                    {
                        savedObject_ = blob.savedObject_;
                        scale_ = blob.savedScale_;
                        blob.savedObject_ = null;
                        writeToServer();
                        return;
                    }
                }
            }

            //@G5A If the code for updateable lob locators did not run, then run old code.
            if(!set)
            {
                Blob blob = (Blob)savedObject_;                                      // @A1C
                int length = (int)blob.length();
                byte[] data = blob.getBytes(1, length);
                locator_.writeData(0, data, 0, length, true);                   //@K1A
            }
        }
        else
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        }
    }

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
            truncated_ = 0;
        }
        else if(object instanceof Reader)
        {
            int length = scale; // hack to get the length into the set method
            byte[] bytes = null;
            if(length >= 0)
            {
                try
                {
                    int blockSize = length < AS400JDBCPreparedStatement.LOB_BLOCK_SIZE ? length : AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    HexReaderInputStream stream = new HexReaderInputStream((Reader)object);
                    byte[] byteBuffer = new byte[blockSize];
                    int totalBytesRead = 0;
                    int bytesRead = stream.read(byteBuffer, 0, blockSize);
                    while(bytesRead > -1 && totalBytesRead < length)
                    {
                        baos.write(byteBuffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        int bytesRemaining = length - totalBytesRead;
                        if(bytesRemaining < blockSize)
                        {
                            blockSize = bytesRemaining;
                        }
                        bytesRead = stream.read(byteBuffer, 0, blockSize);
                    }

                    bytes = baos.toByteArray();

                    if(bytes.length < length)
                    {
                        // a length longer than the stream was specified
                        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                    }

                    int objectLength = bytes.length;
                    if(bytes.length > maxLength_)
                    {
                        byte[] newValue = new byte[maxLength_];
                        System.arraycopy(bytes, 0, newValue, 0, maxLength_);
                        bytes = newValue;
                    }
                    object = bytes;
                    truncated_ = objectLength - bytes.length;
                }
                catch(ExtendedIOException eie)
                {
                    // the Reader contains non-hex characters
                    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, eie);
                }
                catch(IOException ie)
                {
                    JDError.throwSQLException(JDError.EXC_INTERNAL, ie);
                }
            }
            else
            {
                JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
            }
        }
        else if(!(object instanceof String) &&
                (JDUtilities.JDBCLevel_ >= 20 && !(object instanceof Blob)) &&
                !(object instanceof Reader) &&
                !(object instanceof InputStream))
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        }
        savedObject_ = object;
        if(scale != -1) scale_ = scale; // Skip resetting it if we don't know the real length
    }

    //@loch method to temporary convert from object input to output before even going to host (writeToServer() does the conversion needed before writting to host)
    //This will only be used when resultSet.updateX(obj1) is called followed by a obj2 = resultSet.getX()
    //Purpose is to do a local type conversion from obj1 to obj2 like other non-locator lob types
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
            else if(JDUtilities.JDBCLevel_ >= 20 && object instanceof Blob)
            {
                Blob blob = (Blob) object;
                int blobLength = (int)blob.length();
                int lengthToUse = blobLength < 0 ? 0x7FFFFFFF : blobLength;
                if(lengthToUse > maxLength_) lengthToUse = maxLength_;
                value_ = blob.getBytes(1, lengthToUse);
                truncated_ = blobLength - lengthToUse;
            }
            else if(object instanceof InputStream)
            {
                int length = scale_; // hack to get the length into the set method
                if(length >= 0)
                {
                    InputStream stream = (InputStream)object;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int blockSize = length < AS400JDBCPreparedStatement.LOB_BLOCK_SIZE ? length : AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
                    byte[] byteBuffer = new byte[blockSize];
                    try
                    {
                        int totalBytesRead = 0;
                        int bytesRead = stream.read(byteBuffer, 0, blockSize);
                        while(bytesRead > -1 && totalBytesRead < length)
                        {
                            baos.write(byteBuffer, 0, bytesRead);
                            totalBytesRead += bytesRead;
                            int bytesRemaining = length - totalBytesRead;
                            if(bytesRemaining < blockSize)
                            {
                                blockSize = bytesRemaining;
                            }
                            bytesRead = stream.read(byteBuffer, 0, blockSize);
                        }
                    }
                    catch(IOException ie)
                    {
                        JDError.throwSQLException(JDError.EXC_INTERNAL, ie);
                    }
                    
                    value_ = baos.toByteArray();

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
        finally
        {
            //nothing
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
        return AS400JDBCDriver.getResource("MAXLENGTH"); 
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
        return 2147483646; // the DB2 SQL reference says this should be 2147483647 but we return 1 less to allow for NOT NULL columns
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

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSIONS TO JAVA TYPES                               //
    //                                                         //
    //---------------------------------------------------------//

    public InputStream getAsciiStream()
    throws SQLException
    {
        truncated_ = 0;
        //return new AS400JDBCInputStream(new JDLobLocator(locator_));

        // fix this to use a Stream
        try
        {
            return new ByteArrayInputStream(ConvTable.getTable(819, null).stringToByteArray(BinaryConverter.bytesToString(getBytes())));
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
        truncated_ = 0;
        return new AS400JDBCInputStream(new JDLobLocator(locator_));
    }

    public Blob getBlob()
    throws SQLException
    {
        if(savedObject_ != null)//@loch
        {                       //@loch
            //get value from RS.updateX(value)
            doConversion();     //@loch
            truncated_ = 0;     //@loch
            return new AS400JDBCBlob(value_, maxLength_); //@loch
        }                       //@loch
        
        // We don't want to give out our internal locator to the public,
        // otherwise when we go to change its handle on the next row, they'll
        // get confused.  So we have to clone it.
        truncated_ = 0;
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
            truncated_ = 0;     //@loch
            return value_;//@loch
        }                       //@loch
        
        int locatorLength = (int)locator_.getLength();
        if(locatorLength == 0) return new byte[0];
        DBLobData data = locator_.retrieveData(0, locatorLength);
        int actualLength = data.getLength();
        byte[] bytes = new byte[actualLength];
        System.arraycopy(data.getRawBytes(), data.getOffset(), bytes, 0, actualLength);
        truncated_ = 0;
        return bytes;
    }

    public Reader getCharacterStream()
    throws SQLException
    {
        truncated_ = 0;
        //return new InputStreamReader(new AS400JDBCInputStream(new JDLobLocator(locator_)));
        
        // fix this to use a Stream
        return new StringReader(BinaryConverter.bytesToString(getBytes()));
    }

    public Clob getClob()
    throws SQLException
    {
        truncated_ = 0;
        String string = BinaryConverter.bytesToString(getBytes());
        return new AS400JDBCClob(string, string.length());
    }

    public Date getDate(Calendar calendar)
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public double getDouble()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public float getFloat()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public int getInt()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public long getLong()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public Object getObject()
    throws SQLException
    {
        // toObject is used by AS400JDBCPreparedStatement for batching, so we save off our InputStream
        // inside the AS400JDBCBlobLocator. Then, when convertToRawBytes() is called, the writeToServer()
        // code checks the AS400JDBCBlobLocator's saved InputStream... if it exists, then it writes the
        // data out of the InputStream to the system by calling writeToServer() again.

        // Since toObject could also be called from an external user's standpoint, we have
        // to clone our internal locator (because we reuse it internally).
        // This doesn't make much sense, since we technically can't reuse it because
        // the prepared statement is calling toObject() to store off the parameters,
        // but it's all we can do for now.
        truncated_ = 0;
        return new AS400JDBCBlobLocator(new JDLobLocator(locator_), savedObject_, scale_);
    }

    public short getShort()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public String getString()
    throws SQLException
    {
        truncated_ = 0;
        return BinaryConverter.bytesToString(getBytes());
    }

    public Time getTime(Calendar calendar)
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Timestamp getTimestamp(Calendar calendar)
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public InputStream getUnicodeStream()
    throws SQLException
    {
        truncated_ = 0;
        //return new AS400JDBCInputStream(new JDLobLocator(locator_));
        
        // fix this to use a Stream
        try
        {
            return new ByteArrayInputStream(ConvTable.getTable(13488, null).stringToByteArray(BinaryConverter.bytesToString(getBytes())));
        }
        catch(UnsupportedEncodingException e)
        {
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
            return null;
        }
    }
}

