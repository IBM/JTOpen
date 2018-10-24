///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLXMLLocator.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2009-2017 International Business Machines Corporation and     
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
import java.sql.Types;
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

//@xml3 new class
//sending xml to host, this class acts like BlobLocator
//reading xml from host, this class acts like ClobLocator
final class SQLXMLLocator extends SQLLocatorBase
{
    static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";

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
    private boolean                 outOfBounds_; 
    private int                     columnIndex_;
    private byte[]                  valueBlob_; //@loch //Note that value_ is not used as the output for a ResultSet.getX() call.  We Get the value from a call to the JDLocator (not from value_) and not from the savedObject_, unless resultSet.updateX(obj1) is called followed by a obj2 = resultSet.getX()
    private String                  valueClob_; 
    private Object                  savedValue_; 
    private Object savedObject_; // This is the AS400JDBCXMLLocator or InputStream or whatever got set into us.
    private int scale_; // This is actually the length that got set into us.
    private boolean savedObjectWrittenToServer_ = false; 
    private int xmlType_; //@xml3 0=SB 1=DB 2=binary XML

    private boolean inputStreamRead_ = false;

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
            locator_        = new JDLobLocator(connection, id, maxLength, false);   //DB //@xmlgraphic (xml is always sent with bytecount)
        else
            locator_        = new JDLobLocator(connection, id, maxLength, false);  //SB
        maxLength_      = maxLength; 
        settings_       = settings;
        truncated_ = 0; outOfBounds_ = false; 
        converter_      = converter;
        columnIndex_    = columnIndex;
        xmlType_       = xmlType; //@xml3  0=SB 1=DB 2=binary XML
        try{
            //Since we want to pass string's bytes as 1200 (java default for String) 
            //But if XML does not have a declaration then hostserver expects 1208
            unicodeConverter_ =  connection.getConverter(1200); 
            unicodeUtf8Converter_ =  connection.getConverter(1208); //@xmlutf8 
        }catch(SQLException e){
            unicodeConverter_ = connection.getConverter();//should never happen
        }
    }

    public Object clone()
    {
        return new SQLXMLLocator(connection_, id_, maxLength_, settings_, converter_, columnIndex_, xmlType_);  //@xml3
    }

    public void setHandle(int handle)      
    {
        locator_.setHandle(handle);   
        //  @T1A reset saved handle after setting new value
       savedObject_ = null;
       savedObjectWrittenToServer_ = false;    
      
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

    public void convertFromRawBytes(byte[] rawBytes, int offset,
        ConvTable converter) throws SQLException {
      convertFromRawBytes(rawBytes, offset, converter, false); 
    }

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter, boolean ignoreConversionErrors)
    throws SQLException
    {
        int locatorHandle = BinaryConverter.byteArrayToInt(rawBytes, offset);
        locator_.setHandle(locatorHandle);
        locator_.setColumnIndex(columnIndex_);
        //  @T1A reset saved handle after setting new value
       savedObject_ = null;
       savedObjectWrittenToServer_ = false;
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
        if((! savedObjectWrittenToServer_ ) && (savedObject_ != null)) writeToServer();
    }
    
    /**
    validates that raw truncated data is correct.  The data is corrected if is not correct. 
    This is only used when converting to MIXED CCSID and UTF-8. 
    @param  rawBytes         the raw bytes for the system.
    @param  offset           the offset into the byte array.
    @param  ccsidConverter   the converter.
     * @throws SQLException 
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
        //no need to check truncation since xml does not have size
        savedObject_ = object;
        if(object instanceof ConvTableReader) //@ascii
        {
            //set xml flag so ConvTableReader will trim off xml declaration since we will be transmitting in utf-8
            ((ConvTableReader)savedObject_).isXML_ = true; //@ascii
            scale_ = ALL_READER_BYTES;//@ascii flag -2 to read to end of stream (xml transmits in utf8 which may have 2-byte chars, which does not match length)
        }
        else if(scale != -1) 
            scale_ = scale; // Skip resetting it if we don't know the real length
    }

    //Method to temporary convert from object input to output before even going to host (writeToServer() does the conversion needed before writing to host)
    //This will only be used when resultSet.updateX(obj1) is called followed by a obj2 = resultSet.getX()
    //Purpose is to do a local type conversion from obj1 to obj2 like other non-locator lob types
    private void doConversion()
    throws SQLException
    {
      
        valueClob_ = null;
        valueBlob_ = null;
        int length = scale_;

        if( length == -1)
        {
            try{
                //try to get length from locator
                length = (int)locator_.getLength();        
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
              
              valueClob_ = SQLDataBase.getStringFromReader((Reader)savedObject_, length, this);
              // The Reader has been read, so adjust the savedObject to be the read string
              savedObject_ = valueClob_; 
            }
            else if( savedObject_ instanceof Clob)  
            {
              // Updateable locator path 
            	if(savedObject_ instanceof AS400JDBCClobLocator) 
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
                          if ((savedObject_ != null) && (! ( savedObject_ instanceof AS400JDBCClobLocator))) {
                            doConversion(); 
                            return;
                          }
                      }
                  }
              }

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
                length = scale_; 
                if(length >= 0)
                {
                    InputStream stream = (InputStream)savedObject_;
                    
                    valueBlob_ = readInputStream(stream, length, null, false);
                    inputStreamRead_ = true; 
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
                    // Input stream is no longer usable 
                    savedObject_ = valueBlob_; 
                  //xml has no max sizetruncated_ = objectLength - valueBlob_.length;
                }
                else if(length == ALL_READER_BYTES )//@readerlen new else-if block (read all data)
                {
                    InputStream stream = (InputStream)savedObject_;
                    valueBlob_ = readInputStream(stream, length, null, false);
                    inputStreamRead_ = true; 
                    
                    int objectLength = valueBlob_.length;
                    if(objectLength > maxLength_)
                    {
                        byte[] newValue = new byte[maxLength_];
                        System.arraycopy(valueBlob_, 0, newValue, 0, maxLength_);
                        valueBlob_ = newValue;
                    }
                    // Input stream is no longer usable 
                    savedObject_ = valueBlob_; 
                    
                  //xml has no max sizetruncated_ = objectLength - valueBlob_.length;
                }
                else
                {
                    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
                }
            }
            /* ifdef JDBC40 
            else if( savedObject_ instanceof SQLXML ) 
            {
                SQLXML xml = (SQLXML)savedObject_;
                valueClob_ = xml.getString();
            }
            endif */
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
    private void writeToServer()
    throws SQLException
    {
        if (inputStreamRead_) {
          locator_.writeData(0, valueBlob_, true);   
        } else  if(savedObject_ instanceof byte[]) {
            byte[] bytes = (byte[])savedObject_;        
            locator_.writeData(0, bytes, true);   
        }
        else if(savedObject_ instanceof InputStream)
        {
            int length = scale_; // hack to get the length into the set method
            //if(xmlType_ == 1)   
            //    length *=2; //double-byte
            
            // Need to write even if there are 0 bytes in case we are batching and
            // the host server reuses the same handle for the previous locator; otherwise,
            // we'll have data in the current row from the previous row.
            if (length == 0)      {
              locator_.writeData(0, new byte[0], 0, 0, true);  
            }
            else if(length > 0)
            {
                InputStream stream = (InputStream)savedObject_;
                valueBlob_ = readInputStream(stream, length, locator_, false);
               inputStreamRead_ = true; 
                    if(valueBlob_.length < length)
                    {
                        // a length longer than the stream was specified
                        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                    }
                
                
            }
            else if(length == ALL_READER_BYTES) //@readerlen new else-if block (read all data)
            {
              InputStream stream = (InputStream)savedObject_;
              boolean doubleByteOffset = false; 
              if (xmlType_ == 1) { 
                doubleByteOffset = true; 
              }
              valueBlob_ = readInputStream(stream, length, locator_, doubleByteOffset);
              inputStreamRead_ = true; 
              
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
        else if(savedObject_ instanceof String)    {
                String string = (String)savedObject_;
                byte[] bytes;
                if(JDUtilities.hasXMLDeclaration(string))                                 //@xmlutf8
                {
                    string = JDUtilities.handleXMLDeclarationEncoding(string); //if encoding is non utf-16 then remove to match Java Strings  //@xmlutf16
                    bytes = unicodeConverter_.stringToByteArray(string); //just get bytes
                }
                else                                                          //@xmlutf8
                    bytes = unicodeUtf8Converter_.stringToByteArray(string);  //@xmlutf8
                locator_.writeData(0L, bytes, true); 
        } else if(savedObject_ instanceof Reader) {
            int length = scale_; // hack to get the length into the set method
            // Need to write even if there are 0 bytes in case we are batching and
            // the host server reuses the same handle for the previous locator; otherwise,
            // we'll have data in the current row from the previous row.
            if (length == 0)        {
              locator_.writeData(0, new byte[0], 0, 0, true);  
            }  else if(length > 0)      {
                           
              valueClob_ = SQLDataBase.getStringFromReader((Reader)savedObject_, length, this);
              String string = valueClob_; 
              savedObject_ = valueClob_; 
              byte[] bytes;
              if(JDUtilities.hasXMLDeclaration(valueClob_))   {                               //@xmlutf8              
                  string = JDUtilities.handleXMLDeclarationEncoding(string); //if encoding is non utf-16 then remove to match Java Strings  //@xmlutf16
                  bytes = unicodeConverter_.stringToByteArray(string); //just get bytes
              } else {                                                          //@xmlutf8
                  bytes = unicodeUtf8Converter_.stringToByteArray(string);  //@xmlutf8
              }
              locator_.writeData(0L, bytes, true); 

              if(bytes.length < length)        {
                  // a length longer than the stream was specified
                  JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
              }
              
            }
            else if(length == ALL_READER_BYTES) { //@readerlen new else-if block (read all data)
              valueClob_ = SQLDataBase.getStringFromReader((Reader)savedObject_, length, this);
              String string = valueClob_; 
              savedObject_ = valueClob_; 
              byte[] bytes;
              if(JDUtilities.hasXMLDeclaration(valueClob_))                                 //@xmlutf8
              {
                  string = JDUtilities.handleXMLDeclarationEncoding(string); //if encoding is non utf-16 then remove to match Java Strings  //@xmlutf16
                  bytes = unicodeConverter_.stringToByteArray(string); //just get bytes
              }
              else                                                          //@xmlutf8
                  bytes = unicodeUtf8Converter_.stringToByteArray(string);  //@xmlutf8
              locator_.writeData(0L, bytes, true); 

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
                byte[] inputBytes;                                                            //@len2utf8
                if(JDUtilities.hasXMLDeclaration(substring))                                 //@xmlutf8
                    inputBytes = unicodeConverter_.stringToByteArray(JDUtilities.handleXMLDeclarationEncoding(substring));      //@len2utf8 //here, we have a string, so it has to be in utf-16, so just take out encoding and sq and detect encoding
                else
                    inputBytes = unicodeUtf8Converter_.stringToByteArray(substring); //@len2utf8
                    
               
                locator_.writeData(0L, inputBytes, 0, inputBytes.length, true);  //@xmlutf8
                set = true;
            }
            else
            {
                JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
            }
        }
/* ifdef JDBC40  
        else if( savedObject_ instanceof SQLXML ) //@olddesc
        {
           SQLXML xml = (SQLXML)savedObject_;
           
           boolean set = false;
           if(savedObject_ instanceof AS400JDBCSQLXMLLocator)
           {
               AS400JDBCSQLXMLLocator xmlLocator = (AS400JDBCSQLXMLLocator)savedObject_;

               //Synchronize on a lock so that the user can't keep making updates
               //to the xml while we are taking updates off the vectors.
               synchronized(xmlLocator)
               {
                   // See if we saved off our real object from earlier.
                   if(xmlLocator.clobLocatorValue_ != null && xmlLocator.clobLocatorValue_.savedObject_ != null)
                   {
                       savedObject_ = xmlLocator.clobLocatorValue_.savedObject_;
                       scale_ = xmlLocator.clobLocatorValue_.savedScale_;
                       xmlLocator.clobLocatorValue_.savedObject_ = null;
                       writeToServer();
                       return;
                   }
                   else if(xmlLocator.blobLocatorValue_ != null && xmlLocator.blobLocatorValue_.savedObject_ != null)
                   {
                       savedObject_ = xmlLocator.blobLocatorValue_.savedObject_;
                       scale_ = xmlLocator.blobLocatorValue_.savedScale_;
                       xmlLocator.blobLocatorValue_.savedObject_ = null;
                       writeToServer();
                       return;
                   }
               }
           }
                      
           if(!set)
           {
               
               //getString() handles internal representation of clob/dbclob/blob...
               String stringVal = xml.getString();
               byte[] inputBytes;                                                            //@len2utf8
               if(JDUtilities.hasXMLDeclaration(stringVal))                                 //@xmlutf8
                   inputBytes = unicodeConverter_.stringToByteArray(JDUtilities.handleXMLDeclarationEncoding(stringVal));      //@len2utf8 //here, we have a string, so it has to be in utf-16, so just take out encoding and sq and detect encoding
               else
                   inputBytes = unicodeUtf8Converter_.stringToByteArray(stringVal); //@len2utf8
                   
              
               locator_.writeData(0L, inputBytes, 0, inputBytes.length, true);  //@xmlutf8
           }
        }
endif */ 
        else
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        }
        savedObjectWrittenToServer_ = true; 
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
        return AS400JDBCDriver.getResource("MAXLENGTH",null); 
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
        return "XML"; 
    }

    public int getMaximumPrecision()
    {
/*ifdef JDBC40 
        return AS400JDBCSQLXML.MAX_XML_SIZE;  
endif */ 
/* ifndef JDBC40 */ 
        return AS400JDBCDatabaseMetaData.MAX_LOB_LENGTH;
/* endif */         
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
/*ifdef JDBC40 
        return Types.SQLXML;   
endif */ 
/* ifndef JDBC40 */ 
        return Types.CLOB;
/* endif */         
    }

    public String getTypeName()
    {
        return "XML";
         
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
    public boolean getOutOfBounds() {
      return outOfBounds_; 
    }

    public void clearTruncated() { 
      truncated_ = 0; 
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
        try
        {
            if(savedObject_ != null)//@loch
            {                       //@loch
                //get value from RS.updateX(value)
                doConversion();     //@loch
                truncated_ = 0; outOfBounds_ = false;      //@loch
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
            if (valueBlob_ != null) { 
               return new AS400JDBCBlob(valueBlob_, maxLength_); //@loch
            } else {
               try {
                return new AS400JDBCBlob(valueClob_.getBytes("UTF-8"), maxLength_);
              } catch (UnsupportedEncodingException e) {
                // Should never happen since UTF-8 is a valid encoding.
              } 
            }
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
            if (valueBlob_ != null) {
              return valueBlob_;//@loch
            }
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
        try
        {
            if(savedObject_ != null)//@loch
            {                       //@loch
                //get value from RS.updateX(value)
                doConversion();     //@loch
                truncated_ = 0; outOfBounds_ = false;      //@loch
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
        truncated_ = 0; outOfBounds_ = false; 
        if(savedObject_ != null)//@loch
        {                       //@loch
            //get value from RS.updateX(value)
            doConversion();     //@loch
            truncated_ = 0; outOfBounds_ = false;      //@loch
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
        truncated_ = 0; outOfBounds_ = false; 
        /* ifdef JDBC40 
        if(savedObject_ != null)//@xmlupdate //either return savedObject_ here, or add two iterations of getting savedObject_ in writeToServer if type is AS400JDBCSQLXML since it contains clob which contains savedObject_
            return savedObject_;    //@xmlupdate
        return new AS400JDBCSQLXMLLocator(new JDLobLocator(locator_), converter_, savedObject_, scale_, true);  //@xml4   

        endif */
        /* ifndef JDBC40  */ 
        return  getClob();  //@xml4   
        /* endif */ 
    }
     

    public Object getBatchableObject() throws SQLException {
      return getObject();
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
            truncated_ = 0; outOfBounds_ = false;      //@loch
            return valueClob_;      //@loch
        }                       //@loch
        
        DBLobData data = locator_.retrieveData(0, locator_.getMaxLength());
        String value = converter_.byteArrayToString(data.getRawBytes(),
                                                    data.getOffset(),
                                                    data.getLength());
        value = JDUtilities.stripXMLDeclaration(value);  //remove xml declaration 
        truncated_ = 0; outOfBounds_ = false;   
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
        truncated_ = 0; outOfBounds_ = false; 
        try
        {
            if(savedObject_ != null)//@loch
            {                       //@loch
                //get value from RS.updateX(value)
                doConversion();     //@loch
                truncated_ = 0; outOfBounds_ = false;      //@loch
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
        truncated_ = 0; outOfBounds_ = false; 
        
        if(savedObject_ != null)//@loch
        {                       //@loch
            //get value from RS.updateX(value)
            doConversion();     //@loch
            truncated_ = 0; outOfBounds_ = false;      //@loch
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
    
    /* ifdef JDBC40 
    //@pda jdbc40
    public NClob getNClob() throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        
        if(savedObject_ != null)//@loch
        {                       //@loch
            //get value from RS.updateX(value)
            doConversion();     //@loch
            truncated_ = 0; outOfBounds_ = false;      //@loch
            return new AS400JDBCNClob(valueClob_, maxLength_); //@loch
        }                       //@loch
        
        return new AS400JDBCNClobLocator(new JDLobLocator(locator_), converter_, savedObject_, scale_, true);   //@xml4    
 
    }
    endif */ 
    
    //@pda jdbc40
    public String getNString() throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        
        if(savedObject_ != null)//@loch
        {                       //@loch
            //get value from RS.updateX(value)
            doConversion();     //@loch
            truncated_ = 0; outOfBounds_ = false;      //@loch
            return valueClob_;   //@loch
        }                       //@loch

        DBLobData data = locator_.retrieveData(0, locator_.getMaxLength());
        String value = converter_.byteArrayToString(data.getRawBytes(),
                                                    data.getOffset(),
                                                    data.getLength());
        value = JDUtilities.stripXMLDeclaration(value); //remove xml declaration 
        return value;  
    }

/* ifdef JDBC40 
    //@pda jdbc40
    public RowId getRowId() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    //@PDA jdbc40
    public SQLXML getSQLXML() throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        //xml data is always retrieved from host in binary 65535 format
        //may or maynot need converter depending on how user gets data from SQLXML
        //return xmllocator which will treat data as cloblocator and do conversion
        //@xml3 if xml column, remove xml declaration from within internal cloblocator inside of sqlxmllocator.
        return new AS400JDBCSQLXMLLocator(new JDLobLocator(locator_), converter_, savedObject_, scale_, true);   //@xml4   
    }
endif */ 

     
    // @array
    public Array getArray() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
    
    public void updateSettings(SQLConversionSettings settings) {
      settings_ = settings; 
    }

    
    public void saveValue() throws SQLException {
      if (valueClob_ == null && valueBlob_ == null   && savedObject_ != null ) {
        doConversion(); 
      }
      if (valueClob_ != null ) {
        savedValue_ = valueClob_; 
      } else {
        savedValue_ = valueBlob_; 
      }
      
   }

    public Object getSavedValue() {
      
      return savedValue_; 
    }
    
}
