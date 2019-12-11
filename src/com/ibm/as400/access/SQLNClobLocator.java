///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLNClobLocator.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2006-2018 International Business Machines Corporation and     
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
/*ifdef JDBC40 
import java.sql.NClob;
import java.sql.RowId;
endif */ 
import java.sql.SQLException;
/*ifdef JDBC40
import java.sql.SQLXML;
endif */ 
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

//@PDA jdbc40 new class

final class SQLNClobLocator implements SQLLocator
{
   
    private AS400JDBCConnection     connection_;
    private SQLConversionSettings   settings_;
    private ConvTable               converter_;
    private int                     id_;
    private JDLobLocator            locator_;
    private int                     maxLength_; //note length in chars
    private int                     truncated_;
    private boolean                 outOfBounds_; 
    private int                     columnIndex_;
    private String                  value_; //@loch //Note that value_ is not used as the output for a ResultSet.getX() call.  We Get the value from a call to the JDLocator (not from value_) and not from the savedObject_, unless resultSet.updateX(obj1) is called followed by a obj2 = resultSet.getX()
    private String                  savedValue_; 
    private Object savedObject_; // This is the AS400JDBCBlobLocator or InputStream or whatever got set into us.
    private int scale_; // This is actually the length that got set into us.
    private boolean savedObjectWrittenToServer_ = false; 

    SQLNClobLocator(AS400JDBCConnection connection,
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
        truncated_ = 0; outOfBounds_ = false; 
        converter_      = converter;
        columnIndex_    = columnIndex;
    }

    public Object clone()
    {
        return new SQLNClobLocator(connection_, id_, maxLength_, settings_, converter_, columnIndex_);
    }

    public void setHandle(int handle)
    {
        locator_.setHandle(handle);
        //  @T1A reset saved handle after setting new value
       savedObject_ = null;
       savedObjectWrittenToServer_ = false;    
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

    // This is only called from AS400JDBCPreparedStatement in one place.
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
    	//@selins1
        // If it's a String we check for data truncation.
        if(object instanceof String)
        {
            String s = (String)object;
            int length = s.length(); 
            truncated_ = (length > maxLength_ ? length-maxLength_ : 0);  
            outOfBounds_ = false;
        } 
        else if( !(object instanceof Reader) &&
           !(object instanceof InputStream) &&
           !(object instanceof Clob) 
/*ifdef JDBC40 
           && !(object instanceof NClob)
           && !(object instanceof SQLXML)
           endif */ 
           
           ) //@PDC jdbc40
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
    private void doConversion()
    throws SQLException
    {
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
            Object object = savedObject_;
            if(savedObject_ instanceof String)
            {
                value_ = (String)object;
            }
            else if(object instanceof Reader)
            {
              value_ = SQLDataBase.getStringFromReader((Reader)object, length_, this);
              // update the saved object since the reader has been read
              savedObject_=value_; 
            }
            else if( object instanceof Clob)  
            {
              // Updateable locator path 
              if(object instanceof AS400JDBCClobLocator) //@PDA jdbc40 comment: AS400JDBCNClobLocator isa AS400JDBCClobLocator
              {
                  AS400JDBCClobLocator clob = (AS400JDBCClobLocator)object;

                  //Synchronize on a lock so that the user can't keep making updates
                  //to the clob while we are taking updates off the vectors.
                  synchronized(clob)
                  {
                      // See if we saved off our real object from earlier.
                      if(clob.savedObject_ != null)
                      {
                          savedObject_ = clob.savedObject_;
                          savedObjectWrittenToServer_ = false; 
                          scale_ = clob.savedScale_;
                          clob.savedObject_ = null;
                          if (!(savedObject_ instanceof AS400JDBCClobLocator)) {
                             doConversion(); 
                             return;
                          }
                      }
                          
                  }
              }

                Clob clob = (Clob)object;
                value_ = clob.getSubString(1, (int)clob.length());
            }
            /* ifdef JDBC40 
            else if( object instanceof SQLXML ) 
            {
                SQLXML xml = (SQLXML)object;
                value_ = xml.getString();
            }
            endif */ 
            
            else
            {
                JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
            }

            // Truncate if necessary.
            int valueLength = value_.length();
            if(valueLength > maxLength_)
            {
                value_ = value_.substring(0, maxLength_);
            }
        }
        finally
        {
           //nothing
        }
    }
    
    private void writeToServer()
    throws SQLException
    {
        try
        {
            Object object = savedObject_;
            if(object instanceof String)
            {
                String string = (String)object;
            byte[] bytes = converter_.stringToByteArray(string);
            locator_.writeData(0L, bytes, true);            //@k1C
            }
            else if(object instanceof Reader)
            {
                int length = scale_ * 2; // This is always graphic
                // Need to write even if there are 0 bytes in case we are batching and
                // the host server reuses the same handle for the previous locator; otherwise,
                // we'll have data in the current row from the previous row.
                if (length == 0) 
                {
                  locator_.writeData(0, new byte[0], 0, 0, true);            
                }
                else if(length > 0)
                {
                    try
                    {
                        int blockSize = length < AS400JDBCPreparedStatement.LOB_BLOCK_SIZE ? length : AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
                        int bidiStringType = settings_.getBidiStringType();
                        if(bidiStringType == -1) bidiStringType = converter_.bidiStringType_;

                        BidiConversionProperties bidiConversionProperties = new BidiConversionProperties(bidiStringType); 
                        bidiConversionProperties.setBidiImplicitReordering(settings_.getBidiImplicitReordering());      
                        bidiConversionProperties.setBidiNumericOrderingRoundTrip(settings_.getBidiNumericOrdering());     

                        ReaderInputStream stream = new ReaderInputStream((Reader)savedObject_, converter_.getCcsid(), bidiConversionProperties, blockSize); 
                        try{
                            byte[] byteBuffer = new byte[blockSize];

                            int totalBytesRead = 0;
                            int bytesRead = stream.read(byteBuffer, 0, blockSize);
                            while(bytesRead > -1 && totalBytesRead < length)
                            {
                                locator_.writeData((long)(totalBytesRead/2), byteBuffer, 0, bytesRead, true); // totalBytesRead is our offset. 
                                totalBytesRead += bytesRead;
                                int bytesRemaining = length - totalBytesRead;
                                if(bytesRemaining < blockSize)
                                {
                                    blockSize = bytesRemaining;
                                    if(stream.available() == 0 && blockSize != 0)
                                    {
                                    	stream.close(); 
                                        stream = new ReaderInputStream((Reader)savedObject_, converter_.getCcsid(), bidiConversionProperties, blockSize); // do this so we don't read more chars out of the Reader than we have to. //@KBC changed to use bidiConversionProperties instead of bidiStringType
                                    }
                                }
                                bytesRead = stream.read(byteBuffer, 0, blockSize);
                            }

                           
                            if(totalBytesRead < length)
                            {
                                // a length longer than the stream was specified
                                JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                            }
                        }finally{
                            try{
                                stream.close();
                            }catch(Exception e){
                              JDTrace.logException(this, "writeToServer stream.close()", e);  
                            }
                        }

                    }
                    catch(IOException ie)
                    {
                        JDError.throwSQLException(this, JDError.EXC_INTERNAL, ie);
                    }
                }
                else if(length == ALL_READER_BYTES) //@readerlen new else-if block (read all data)
                {
                    try
                    {
                        int blockSize =  AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
                        int bidiStringType = settings_.getBidiStringType();
                        if(bidiStringType == -1) bidiStringType = converter_.bidiStringType_;

                        BidiConversionProperties bidiConversionProperties = new BidiConversionProperties(bidiStringType); 
                        bidiConversionProperties.setBidiImplicitReordering(settings_.getBidiImplicitReordering());      
                        bidiConversionProperties.setBidiNumericOrderingRoundTrip(settings_.getBidiNumericOrdering());     

                        ReaderInputStream stream = new ReaderInputStream((Reader)savedObject_, converter_.getCcsid(), bidiConversionProperties, blockSize); 
                        try{
                            byte[] byteBuffer = new byte[blockSize];

                            int totalBytesRead = 0;
                            int bytesRead = stream.read(byteBuffer, 0, blockSize);
                            while(bytesRead > -1 )
                            {
                                locator_.writeData((long)(totalBytesRead/2), byteBuffer, 0, bytesRead, true); // totalBytesRead is our offset. 
                                totalBytesRead += bytesRead;
                               
                                bytesRead = stream.read(byteBuffer, 0, blockSize);
                            }

                        }finally{
                            try{
                                stream.close();
                            }catch(Exception e){
                              JDTrace.logException(this, "writeToServer stream2.close()", e);  
                            }
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
            else if(object instanceof InputStream)
            {
                int length = scale_ * 2; // We are always graphic
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
                            locator_.writeData((long)totalBytesRead/2, byteBuffer, 0, bytesRead, true); // totalBytesRead is our offset.  
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
                else if(length == ALL_READER_BYTES) //@readerlen new else-if block (read all data)
                {
                    InputStream stream = (InputStream)savedObject_;
                    int blockSize = AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
                    byte[] byteBuffer = new byte[blockSize];
                    try
                    {
                        int totalBytesRead = 0;
                        int bytesRead = stream.read(byteBuffer, 0, blockSize);
                        while(bytesRead > -1 )
                        {
                            locator_.writeData((long)totalBytesRead/2, byteBuffer, 0, bytesRead, true); // totalBytesRead is our offset.  
                            totalBytesRead += bytesRead;
                           
                            bytesRead = stream.read(byteBuffer, 0, blockSize);
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
            else if( object instanceof Clob) //check for jdbc level to know if lobs exist  //@PDD jdbc40 NClob isa Clob
            {
                // Start new code for updateable locator case
                boolean set = false;
                if(object instanceof AS400JDBCClobLocator) //@PDA jdbc40 comment: AS400JDBCNClobLocator isa AS400JDBCClobLocator
                {
                    AS400JDBCClobLocator clob = (AS400JDBCClobLocator)object;

                    //Synchronize on a lock so that the user can't keep making updates
                    //to the clob while we are taking updates off the vectors.
                    synchronized(clob)
                    {
                        // See if we saved off our real object from earlier.
                        if(clob.savedObject_ != null)
                        {
                            savedObject_ = clob.savedObject_;
                            savedObjectWrittenToServer_ = false; 
                            scale_ = clob.savedScale_;
                            clob.savedObject_ = null;
                            writeToServer();
                            savedObjectWrittenToServer_ = false; 
                            return;
                        }
                    }
                }

                //If the code for updateable lob locators did not run, then run old code.
                if(!set)
                {
                    Clob clob = (Clob)object;
                    int length = (int)clob.length();
                int blockSize = AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
                if(length < blockSize) blockSize = length;
                int position = 1;
                AS400JDBCClobLocator thisClob = new AS400JDBCClobLocator(new JDLobLocator(locator_), converter_, savedObject_, scale_);   //@hloc1 getClob() returns local value since it was just set.  Here we want the locator on the host so we can write to it. 
                while(position <= length)
                {
                    String substring = clob.getSubString(position, blockSize);
                    thisClob.setString(position, substring);
                    position += blockSize;
                    if((length - position) < blockSize)
                    {
                        blockSize = length - position + 1;
                    }
                }
                    set = true;
                }
                else
                {
                    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                }
            }
/* ifdef JDBC40 
            else if( object instanceof SQLXML ) //@PDA jdbc40 
            {
                SQLXML xml = (SQLXML)object;
               
                String stringVal = xml.getString();
                 // @J5C Use length of output array not substring
                byte[] outByteArray = converter_.stringToByteArray(stringVal);
                locator_.writeData(0L, outByteArray, 0, outByteArray.length, true);           
            }
endif */ 
            else
            {
                JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
            }
        }
        finally
        {
            // Do not delete the saved object after writing it to 
            // the server.  We may still need it if we need to 
            // re-execute the statement. 
            // savedObject_ = null;
            savedObjectWrittenToServer_ = true; 
        }
        scale_ = (int)locator_.getLength();
    }

    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public int getSQLType()
    {
        return SQLData.NCLOB_LOCATOR;
    }

    public String getCreateParameters()
    {
        return AS400JDBCDriver.getResource("MAXLENGTH",null); 
    }

    public int getDisplaySize()
    {
        return maxLength_ / 2;
    }

    public String getJavaClassName()
    {
        return "com.ibm.as400.access.AS400JDBCNClobLocator";   
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
        return "NCLOB"; 
    }

    public int getMaximumPrecision()
    {
        return 1073741822; // the DB2 SQL reference says this should be 1073741823 but we return 1 less to allow for NOT NULL columns
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
    	/* ifdef JDBC40 
        return java.sql.Types.NCLOB;
        endif */ 
    	/* ifndef JDBC40 */ 
    	return java.sql.Types.CLOB; 
    	/* endif */ 
    }

    public String getTypeName()
    {
        return "NCLOB";
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
                return new ByteArrayInputStream(ConvTable.getTable(819, null).stringToByteArray(value_));//@loch
            }                       //@loch
            
            return new ReaderInputStream(new ConvTableReader(new AS400JDBCInputStream(new JDLobLocator(locator_)), converter_.getCcsid()), 819); // ISO-8859-1.
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
        try
        {
            if(savedObject_ != null)//@loch
            {                       //@loch
                //get value from RS.updateX(value)
                doConversion();     //@loch
                truncated_ = 0; outOfBounds_ = false;      //@loch
                return new HexReaderInputStream(new StringReader(value_)); //@loch
            }                       //@loch
            
            return new HexReaderInputStream(new ConvTableReader(new AS400JDBCInputStream(new JDLobLocator(locator_)), converter_.getCcsid()));
        }
        catch(UnsupportedEncodingException e)
        {
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
            return null;
        }
    }

    public Blob getBlob()
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
                return  new AS400JDBCBlob(BinaryConverter.stringToBytes(value_), maxLength_);
            }                       //@loch
            
            return new AS400JDBCBlob(BinaryConverter.stringToBytes(getString()), maxLength_);
        }
        catch(NumberFormatException nfe)
        {
            // this Clob contains non-hex characters
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, nfe);
            return null;
        }
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
        return 0;
    }

    public byte[] getBytes()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        try
        {
            return BinaryConverter.stringToBytes(getString());
        }
        catch(NumberFormatException nfe)
        {
            // this Clob contains non-hex characters
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, nfe);
            return null;
        }
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
                return new StringReader(value_); //@loch
            }                       //@loch
            
            return new ConvTableReader(new AS400JDBCInputStream(new JDLobLocator(locator_)), converter_.getCcsid());
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
            return new AS400JDBCClob(value_, maxLength_); //@loch
        }                       //@loch
        
        return new AS400JDBCClobLocator(new JDLobLocator(locator_), converter_, savedObject_, scale_);        
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
        return 0;
    }

    public float getFloat()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }

    public int getInt()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }

    public long getLong()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }

    public Object getObject()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        // getObject is used by AS400JDBCPreparedStatement for batching, so we save off our InputStream
        // inside the AS400JDBCNClobLocator. Then, when convertToRawBytes() is called, the writeToServer()
        // code checks the AS400JDBCNClobLocator's saved InputStream... if it exists, then it writes the
        // data out of the InputStream to the system by calling writeToServer() again.
        /* ifdef JDBC40 
        return new AS400JDBCNClobLocator(new JDLobLocator(locator_), converter_, savedObject_, scale_);
        endif */ 
        /* ifndef JDBC40 */ 
        return new AS400JDBCClobLocator(new JDLobLocator(locator_), converter_, savedObject_, scale_);
        
        /* endif */ 
    }


  


    public Object getBatchableObject() throws SQLException {
      return getObject();
    }

    public short getShort()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }

    public String getString()
    throws SQLException
    {
        if(savedObject_ != null)//@loch
        {                       //@loch
            //get value from RS.updateX(value)
            doConversion();     //@loch
            truncated_ = 0; outOfBounds_ = false;      //@loch
            return value_;      //@loch
        }                       //@loch
        
        DBLobData data = locator_.retrieveData(0, locator_.getMaxLength());
        String value = converter_.byteArrayToString(data.getRawBytes(),
                                                    data.getOffset(),
                                                    data.getLength());
        truncated_ = 0; outOfBounds_ = false;  //@pda jdbc40 make consistent with other SQLData Clob classes
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
                return new ReaderInputStream(new StringReader(value_), 13488); //@loch
            }                       //@loch
        
        
            return new ReaderInputStream(new ConvTableReader(new AS400JDBCInputStream(new JDLobLocator(locator_)), converter_.getCcsid()), 13488);
        }
        catch(UnsupportedEncodingException e)
        {
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
            return null;
        }
    }
   

    public Reader getNCharacterStream() throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        
        if(savedObject_ != null)//@loch
        {                       //@loch
            //get value from RS.updateX(value)
            doConversion();     //@loch
            truncated_ = 0; outOfBounds_ = false;      //@loch
            return new StringReader(value_);  //@loch
        }                       //@loch
        
        try
        {
            return new ConvTableReader(new AS400JDBCInputStream(new JDLobLocator(locator_)), converter_.getCcsid());
        }
        catch(UnsupportedEncodingException e)
        {
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
            return null;
        }
    }
    
/* ifdef JDBC40 
    public NClob getNClob() throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        
        if(savedObject_ != null)//@loch
        {                       //@loch
            //get value from RS.updateX(value)
            doConversion();     //@loch
            truncated_ = 0; outOfBounds_ = false;      //@loch
            return new AS400JDBCNClob(value_, maxLength_); //@loch
        }                       //@loch
        
        return new AS400JDBCNClobLocator(new JDLobLocator(locator_), converter_, savedObject_, scale_);        
 
    }
endif */ 
   
    public String getNString() throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        
        if(savedObject_ != null)//@loch
        {                       //@loch
            //get value from RS.updateX(value)
            doConversion();     //@loch
            truncated_ = 0; outOfBounds_ = false;      //@loch
            return value_;      //@loch
        }                       //@loch

        DBLobData data = locator_.retrieveData(0, locator_.getMaxLength());
        String value = converter_.byteArrayToString(data.getRawBytes(),
                                                    data.getOffset(),
                                                    data.getLength());
        return value;  
    }

    /* ifdef JDBC40 
    public RowId getRowId() throws SQLException
    {
        
        //truncated_ = 0; outOfBounds_ = false; 
        //try
        //{
         //   return new AS400JDBCRowId(BinaryConverter.stringToBytes(getString()));
        //}
        //catch(NumberFormatException nfe)
        //{
            // this Clob contains non-hex characters
        //    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, nfe);
        //    return null;
        //}
        
        //Decided this is of no use because rowid is so specific to the dbms internals.
        //And there are issues in length and difficulties in converting to a
        //valid rowid that is useful.
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public SQLXML getSQLXML() throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        if(savedObject_ != null)//@loch
        {                       //@loch
            //get value from RS.updateX(value)
            doConversion();     //@loch
            truncated_ = 0; outOfBounds_ = false;      //@loch
            return new AS400JDBCSQLXML(value_, maxLength_); //@loch
        }                       //@loch
        return new AS400JDBCSQLXML( getString() );        
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
      if (value_ == null  && savedObject_ != null ) {
        doConversion(); 
      }
      
      savedValue_ = value_; 
   }

    public Object getSavedValue() {
      
      return savedValue_; 
    }
    
}



