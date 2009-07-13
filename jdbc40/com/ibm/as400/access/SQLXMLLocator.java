///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLXMLLocator.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2009-2009 International Business Machines Corporation and     
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
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

//@xml3 new class
//sending xml to host, this class acts like BlobLocator
//reading xml from host, this class acts like ClobLocator
final class SQLXMLLocator implements SQLLocator
{
    private static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";

    private AS400JDBCConnection     connection_;
    //writing to host, we let host do all the conversion
    //reading from host, we do all the conversion
    private ConvTable               converter_; //used when reading from host
    private ConvTable               unicodeConverter_; //1200 unicode to simulate no conversion //used when writing to host
    private ConvTable               unicodeUtf8Converter_; //1208 when no XML declaration  //@xmlutf8
    private int                     id_;
    private JDLobLocator            locator_;
    private int                     maxLength_;
    private SQLConversionSettings   settings_;
    private int                     truncated_;
    private int                     columnIndex_;
    private byte[]                  valueBlob_; //@loch //Note that value_ is not used as the output for a ResultSet.getX() call.  We Get the value from a call to the JDLocator (not from value_) and not from the savedObject_, unless resultSet.updateX(obj1) is called followed by a obj2 = resultSet.getX()
    private String                  valueClob_; 
    
    private Object savedObject_; // This is the AS400JDBCXMLLocator or InputStream or whatever got set into us.
    private int scale_; // This is actually the length that got set into us.
    private int xmlType_; //@xml3 0=SB 1=DB 2=binary XML

    SQLXMLLocator(AS400JDBCConnection connection,
                   int id,
                   int maxLength, 
                   SQLConversionSettings settings,
                   ConvTable converter,
                   int columnIndex,
                   int xmlType) //@xml3 
    {
        connection_     = connection;
        maxLength = AS400JDBCDatabaseMetaData.MAX_LOB_LENGTH; //@xml3 //for xml, length is unknown. use max for single byte lobs since xml is returned via bloblocator
        id_             = id;
        if(xmlType == 1)
            locator_        = new JDLobLocator(connection, id, maxLength, true);   //DB
        else
            locator_        = new JDLobLocator(connection, id, maxLength, false);  //SB
        maxLength_      = maxLength; 
        settings_       = settings;
        truncated_      = 0;
        converter_      = converter;
        columnIndex_    = columnIndex;
        xmlType_       = xmlType; //@xml3  0=SB 1=DB 2=binary XML
        try{
            //Since we want to pass string's bytes as 1200 (java default for String) 
            //But if XML does not have a declaration then hostserver expects 1208
            unicodeConverter_ =  connection.getConverter(1200); 
            unicodeUtf8Converter_ =  connection.getConverter(1208); //@xmlutf8 
        }catch(SQLException e){
            unicodeConverter_ = connection.converter_;//should never happen
        }
    }

    public Object clone()
    {
        return new SQLXMLLocator(connection_, id_, maxLength_, settings_, converter_, columnIndex_, xmlType_);  //@xml3
    }

    public void setHandle(int handle)      
    {
        locator_.setHandle(handle);          
    }                     
    
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

    //This is only called from AS400JDBCPreparedStatement in one place.
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
            locator_.writeData(0, bytes, true);   
        }
        else if(savedObject_ instanceof InputStream)
        {
            int length = scale_; // hack to get the length into the set method
            if(xmlType_ == 1)
                length *=2; //double-byte
            
            // Need to write even if there are 0 bytes in case we are batching and
            // the host server reuses the same handle for the previous locator; otherwise,
            // we'll have data in the current row from the previous row.
            if (length == 0) 
            {
              locator_.writeData(0, new byte[0], 0, 0, true);  
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
                        if(xmlType_ == 1)
                            locator_.writeData(totalBytesRead/2, byteBuffer, 0, bytesRead, true); // totalBytesRead is our offset.
                        else
                            locator_.writeData(totalBytesRead, byteBuffer, 0, bytesRead, true); 
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
        else if (savedObject_ instanceof Blob)
        {
            //Start new code for updateable locator case to go through the Vectors 
            //and update the blob copy when ResultSet.updateBlob() is called.
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

            //If the code for updateable lob locators did not run, then run old code.
            if(!set)
            {
                Blob blob = (Blob)savedObject_; 
                int length = (int)blob.length();
                byte[] data = blob.getBytes(1, length);
                locator_.writeData(0, data, 0, length, true); 
            }
        }
        else if(savedObject_ instanceof String)
        {
                String string = (String)savedObject_;
                byte[] bytes;
                if(JDUtilities.hasXMLDeclaration(string))                                 //@xmlutf8
                    bytes = unicodeConverter_.stringToByteArray(string); //just get bytes
                else                                                          //@xmlutf8
                    bytes = unicodeUtf8Converter_.stringToByteArray(string);  //@xmlutf8
                locator_.writeData(0L, bytes, true); 
        }
        else if(savedObject_ instanceof Reader)
        {
            int length = scale_; // hack to get the length into the set method
            // Need to write even if there are 0 bytes in case we are batching and
            // the host server reuses the same handle for the previous locator; otherwise,
            // we'll have data in the current row from the previous row.
            if (length == 0) 
            {
              locator_.writeData(0, new byte[0], 0, 0, true);  
            }
            else if(length > 0)
            {
                //@xmlutf8 (for reader and stream, don't do any conversion)
                //@xmlutf8 added code here similar to SQLBlob.set() for Reader input.
                byte[] bytes = null;
                if(length >= 0)
                {
                    try
                    {
                        int blockSize = length < AS400JDBCPreparedStatement.LOB_BLOCK_SIZE ? length : AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        HexReaderInputStream stream = new HexReaderInputStream((Reader)savedObject_);
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
                        stream.close(); //@scan1
                        locator_.writeData(0, bytes, true);   
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
                
            }
            else
            {
                JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
            }
        }
        else if( savedObject_ instanceof Clob) //jdbc40 NClob isa Clob
        {
            //@G5A Start new code for updateable locator case
            boolean set = false;
            if(savedObject_ instanceof AS400JDBCClobLocator) //@PDA jdbc40 comment: AS400JDBCNClobLocator isa AS400JDBCClobLocator
            {
                AS400JDBCClobLocator clob = (AS400JDBCClobLocator)savedObject_;

                //Synchronize on a lock so that the user can't keep making updates
                //to the clob while we are taking updates off the vectors.
                synchronized(clob)
                {
                    // See if we saved off our real object from earlier.
                    if(clob.savedObject_ != null)
                    {
                        savedObject_ = clob.savedObject_;
                        scale_ = clob.savedScale_;
                        clob.savedObject_ = null;
                        writeToServer();
                        return;
                    }
                }
            }

            //If the code for updateable lob locators did not run, then run old code.
            if(!set)
            {
                Clob clob = (Clob)savedObject_;
                int length = (int)clob.length();
                String substring = clob.getSubString(1, length);
                if(JDUtilities.hasXMLDeclaration(substring))                                 //@xmlutf8
                    locator_.writeData(0L, unicodeConverter_.stringToByteArray(substring), 0, length, true); 
                else
                    locator_.writeData(0L, unicodeUtf8Converter_.stringToByteArray(substring), 0, length, true);  //@xmlutf8
                set = true;
            }
            else
            {
                JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
            }
        }
        else if( savedObject_ instanceof SQLXML ) 
        {
           SQLXML xml = (SQLXML)savedObject_;
           
           //getString() handles internal representation of clob/dbclob/blob...
           String stringVal = xml.getString();
           if(JDUtilities.hasXMLDeclaration(stringVal))                                 //@xmlutf8
               locator_.writeData(0L, unicodeConverter_.stringToByteArray(stringVal), 0, stringVal.length(), true); 
           else
               locator_.writeData(0L, unicodeUtf8Converter_.stringToByteArray(stringVal), 0, stringVal.length(), true);  //@xmlutf8
        }
        else
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        }
    }

    public void set(Object object, Calendar calendar, int scale)
    throws SQLException
    {
        //no need to check truncation since xml does not have size
        savedObject_ = object;
        if(scale != -1) scale_ = scale; // Skip resetting it if we don't know the real length
    }

    //Method to temporary convert from object input to output before even going to host (writeToServer() does the conversion needed before writing to host)
    //This will only be used when resultSet.updateX(obj1) is called followed by a obj2 = resultSet.getX()
    //Purpose is to do a local type conversion from obj1 to obj2 like other non-locator lob types
    private void doConversion()
    throws SQLException
    {
        valueClob_ = null;
        valueBlob_ = null;
        int length_ = scale_;

        if( length_ == -1)
        {
            try{
                //try to get length from locator
                length_ = (int)locator_.getLength();        
            }catch(Exception e){ }
        }
        
        try
        {
           
            if(savedObject_ instanceof String)
            {
                valueClob_ = (String)savedObject_;
                
            }
            else if(savedObject_ instanceof Reader)
            {
                if(length_ >= 0)
                {
                    try
                    {
                        int blockSize = length_ < AS400JDBCPreparedStatement.LOB_BLOCK_SIZE ? length_ : AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
                        Reader stream = (Reader)savedObject_;
                        StringBuffer buf = new StringBuffer();
                        char[] charBuffer = new char[blockSize];
                        int totalCharsRead = 0;
                        int charsRead = stream.read(charBuffer, 0, blockSize);
                        while(charsRead > -1 && totalCharsRead < length_)
                        {
                            buf.append(charBuffer, 0, charsRead);
                            totalCharsRead += charsRead;
                            int charsRemaining = length_ - totalCharsRead;
                            if(charsRemaining < blockSize)
                            {
                                blockSize = charsRemaining;
                            }
                            charsRead = stream.read(charBuffer, 0, blockSize);
                        }
                        valueClob_ = buf.toString();

                        if(valueClob_.length() < length_)
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
            else if( savedObject_ instanceof Clob)  
            {
                Clob clob = (Clob)savedObject_;
                valueClob_ = clob.getSubString(1, (int)clob.length());
            }
            else if(savedObject_ instanceof byte[])
            {
                valueBlob_ = (byte[]) savedObject_;
                int objectLength = valueBlob_.length;
                if(objectLength > maxLength_)
                {
                    byte[] newValue = new byte[maxLength_];
                    System.arraycopy(valueBlob_, 0, newValue, 0, maxLength_);
                    valueBlob_ = newValue;
                }
                //xml has no max size truncated_ = objectLength - valueBlob_.length;
            }
            else if(savedObject_ instanceof Blob)
            {
                Blob blob = (Blob) savedObject_;
                int blobLength = (int)blob.length();
                int lengthToUse = blobLength < 0 ? 0x7FFFFFFF : blobLength;
                if(lengthToUse > maxLength_) lengthToUse = maxLength_;
                valueBlob_ = blob.getBytes(1, lengthToUse);
              //xml has no max sizetruncated_ = blobLength - lengthToUse;
            }
            else if(savedObject_ instanceof InputStream)
            {
                int length = scale_; 
                if(length >= 0)
                {
                    InputStream stream = (InputStream)savedObject_;
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
                    
                    valueBlob_ = baos.toByteArray();

                    if(valueBlob_.length < length)
                    {
                        // a length longer than the stream was specified
                        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                    }

                    int objectLength = valueBlob_.length;
                    if(objectLength > maxLength_)
                    {
                        byte[] newValue = new byte[maxLength_];
                        System.arraycopy(valueBlob_, 0, newValue, 0, maxLength_);
                        valueBlob_ = newValue;
                    }
                  //xml has no max sizetruncated_ = objectLength - valueBlob_.length;
                }
                else if( savedObject_ instanceof SQLXML ) 
                {
                    SQLXML xml = (SQLXML)savedObject_;
                    valueClob_ = xml.getString();
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
        return SQLData.XML_LOCATOR;
    }

    public String getCreateParameters()
    {
        return AS400JDBCDriver.getResource("MAXLENGTH"); 
    }

    public int getDisplaySize()
    {
        return maxLength_;
    }

    public String getJavaClassName()
    {
        return "com.ibm.as400.access.AS400JDBCSQLXML";    
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
        return "SQLXML"; 
    }

    public int getMaximumPrecision()
    {
        return AS400JDBCSQLXML.MAX_XML_SIZE;  
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
        return 2452;
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
        return java.sql.Types.SQLXML;
    }

    public String getTypeName()
    {
        return "SQLXML"; 
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
        try
        {
            if(savedObject_ != null)//@loch
            {                       //@loch
                //get value from RS.updateX(value)
                doConversion();     //@loch
                truncated_ = 0;     //@loch
                return new ByteArrayInputStream(ConvTable.getTable(819, null).stringToByteArray(valueClob_));//@loch
            }                       //@loch
            //remove xml declaration via ConvTableReader
            Reader tmpUnicodeReader = new ConvTableReader(new AS400JDBCInputStream(new JDLobLocator(locator_)), converter_.getCcsid(), true); 
           // tmpUnicodeReader.
            return new ReaderInputStream(tmpUnicodeReader, 819); // ISO-8859-1. //is xml type
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
            return new AS400JDBCBlob(valueBlob_, maxLength_); //@loch
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
            return valueBlob_;//@loch
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
        try
        {
            if(savedObject_ != null)//@loch
            {                       //@loch
                //get value from RS.updateX(value)
                doConversion();     //@loch
                truncated_ = 0;     //@loch
                return new StringReader(valueClob_); //@loch
            }                       //@loch
            //remove xml declaration via ConvTableReader
            return new ConvTableReader(new AS400JDBCInputStream(new JDLobLocator(locator_)), converter_.getCcsid(), true); //@xml4
        }
        catch(UnsupportedEncodingException e)
        {
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
            return null;
        }
    }

    public Clob getClob()
    throws SQLException
    {
        truncated_ = 0;
        if(savedObject_ != null)//@loch
        {                       //@loch
            //get value from RS.updateX(value)
            doConversion();     //@loch
            truncated_ = 0;     //@loch
            return new AS400JDBCClob(valueClob_, maxLength_); //@loch
        }                       //@loch
        
        return new AS400JDBCClobLocator(new JDLobLocator(locator_), converter_, savedObject_, scale_, true);//@xml4     
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
        return new AS400JDBCSQLXMLLocator(new JDLobLocator(locator_), converter_, savedObject_, scale_, true);  //@xml4   
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
        if(savedObject_ != null)//@loch
        {                       //@loch
            //get value from RS.updateX(value)
            doConversion();     //@loch
            truncated_ = 0;     //@loch
            return valueClob_;      //@loch
        }                       //@loch
        
        DBLobData data = locator_.retrieveData(0, locator_.getMaxLength());
        String value = converter_.byteArrayToString(data.getRawBytes(),
                                                    data.getOffset(),
                                                    data.getLength());
        value = JDUtilities.stripXMLDeclaration(value);  //remove xml declaration 
        truncated_ = 0;  
        return value;
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
        try
        {
            if(savedObject_ != null)//@loch
            {                       //@loch
                //get value from RS.updateX(value)
                doConversion();     //@loch
                truncated_ = 0;     //@loch
                return new ReaderInputStream(new StringReader(valueClob_), 13488); //@loch
            }                       //@loch
        
            //remove xml declaration via ConvTableReader
            return new ReaderInputStream(new ConvTableReader(new AS400JDBCInputStream(new JDLobLocator(locator_)), converter_.getCcsid(), true), 13488); //@xml4
        }
        catch(UnsupportedEncodingException e)
        {
            JDError.throwSQLException(JDError.EXC_INTERNAL, e);
            return null;
        }
    }
    
    //@PDA jdbc40
    public Reader getNCharacterStream() throws SQLException
    {
        truncated_ = 0;
        
        if(savedObject_ != null)//@loch
        {                       //@loch
            //get value from RS.updateX(value)
            doConversion();     //@loch
            truncated_ = 0;     //@loch
            return new StringReader(valueClob_);  //@loch
        }                       //@loch
        
        try
        {
            //remove xml declaration via ConvTableReader
            return new ConvTableReader(new AS400JDBCInputStream(new JDLobLocator(locator_)), converter_.getCcsid(), true);   //@xml4
        }
        catch(UnsupportedEncodingException e)
        {
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
            return null;
        }
    }
    
    //@pda jdbc40
    public NClob getNClob() throws SQLException
    {
        truncated_ = 0;
        
        if(savedObject_ != null)//@loch
        {                       //@loch
            //get value from RS.updateX(value)
            doConversion();     //@loch
            truncated_ = 0;     //@loch
            return new AS400JDBCNClob(valueClob_, maxLength_); //@loch
        }                       //@loch
        
        return new AS400JDBCNClobLocator(new JDLobLocator(locator_), converter_, savedObject_, scale_, true);   //@xml4    
 
    }

    //@pda jdbc40
    public String getNString() throws SQLException
    {
        truncated_ = 0;
        
        if(savedObject_ != null)//@loch
        {                       //@loch
            //get value from RS.updateX(value)
            doConversion();     //@loch
            truncated_ = 0;     //@loch
            return valueClob_;   //@loch
        }                       //@loch

        DBLobData data = locator_.retrieveData(0, locator_.getMaxLength());
        String value = converter_.byteArrayToString(data.getRawBytes(),
                                                    data.getOffset(),
                                                    data.getLength());
        value = JDUtilities.stripXMLDeclaration(value); //remove xml declaration 
        return value;  
    }

    //@pda jdbc40
    public RowId getRowId() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    //@PDA jdbc40
    public SQLXML getSQLXML() throws SQLException
    {
        truncated_ = 0;
        //xml data is always retrieved from host in binary 65535 format
        //may or maynot need converter depending on how user gets data from SQLXML
        //return xmllocator which will treat data as cloblocator and do conversion
        //@xml3 if xml column, remove xml declaration from within internal cloblocator inside of sqlxmllocator.
        return new AS400JDBCSQLXMLLocator(new JDLobLocator(locator_), converter_, savedObject_, scale_, true);   //@xml4   
    }
     
    // @array
    public Array getArray() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
}

