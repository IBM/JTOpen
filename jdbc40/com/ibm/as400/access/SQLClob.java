///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLClob.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;
 
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
/* ifdef JDBC40 */
import java.sql.NClob;
import java.sql.RowId;
/* endif */ 
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
/* ifdef JDBC40 */
import java.sql.SQLXML;  //@PDA jdbc40
/* endif */ 
final class SQLClob extends SQLDataBase
{
    static final String copyright0 = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";

    private int                     length_;                    // Length of string, in characters.
    private int                     maxLength_;                 // Max length of field, in bytes.
    private String                  value_;
    private Object savedObject_; // This is our byte[] or InputStream or whatever that we save to convert to bytes until we really need to.

    // Note: maxLength is in bytes not counting 2 for LL.
    //
    SQLClob(int maxLength, SQLConversionSettings settings)
    {
        super(settings); 
        length_         = 0;
        maxLength_      = maxLength;
        value_          = "";
    }

    public Object clone()
    {
        return new SQLClob(maxLength_, settings_);
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
    throws SQLException
    {
        length_ = BinaryConverter.byteArrayToInt(rawBytes, offset);

        int bidiStringType = settings_.getBidiStringType();

        // if bidiStringType is not set by user, use ccsid to get value
        if(bidiStringType == -1) bidiStringType = ccsidConverter.bidiStringType_;

        BidiConversionProperties bidiConversionProperties = new BidiConversionProperties(bidiStringType);   //@KBA
        bidiConversionProperties.setBidiImplicitReordering(settings_.getBidiImplicitReordering());          //@KBA
        bidiConversionProperties.setBidiNumericOrderingRoundTrip(settings_.getBidiNumericOrdering());       //@KBA

        value_ = ccsidConverter.byteArrayToString(rawBytes, offset + 4, length_, bidiConversionProperties); //@KBC changed to used bidiConversionProperties instead of bidiStringType
        savedObject_ = null;
    }

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
    throws SQLException
    {
        if(savedObject_ != null) doConversion();

        //@PDC change to do same as SQLVarchar to get true length of byte array instead of number of chars
        //BinaryConverter.intToByteArray(length_, rawBytes, offset);
        try
        {
            int bidiStringType = settings_.getBidiStringType();
            if(bidiStringType == -1) bidiStringType = ccsidConverter.bidiStringType_;

            BidiConversionProperties bidiConversionProperties = new BidiConversionProperties(bidiStringType);   //@KBA
            bidiConversionProperties.setBidiImplicitReordering(settings_.getBidiImplicitReordering());          //@KBA
            bidiConversionProperties.setBidiNumericOrderingRoundTrip(settings_.getBidiNumericOrdering());       //@KBA

            //@PDC  ccsidConverter.stringToByteArray(value_, rawBytes, offset + 4, maxLength_, bidiConversionProperties);   //@KBC changed to use bidiConversionProperties instead of bidiStringType
            byte[] temp = ccsidConverter.stringToByteArray(value_, bidiConversionProperties);  
            // The length in the first 4 bytes is actually the length in characters.
            BinaryConverter.intToByteArray(temp.length, rawBytes, offset);
            if(temp.length > maxLength_)
            {
                maxLength_ = temp.length;
                JDError.throwSQLException(this, JDError.EXC_INTERNAL, "Change Descriptor");
            }
            System.arraycopy(temp, 0, rawBytes, offset+4, temp.length);
        }
        catch(Exception e)
        {
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
        }
    }

    //---------------------------------------------------------//
    //                                                         //
    // SET METHODS                                             //
    //                                                         //
    //---------------------------------------------------------//

    public void set(Object object, Calendar calendar, int scale)
    throws SQLException
    {
        //@selins1 make same as SQLCloblocator
        // If it's a String we check for data truncation.
        if(object instanceof String)
        {
            String s = (String)object;
            int byteLength = s.length(); //@selins1
            truncated_ = (byteLength > maxLength_ ? byteLength-maxLength_ : 0);  
            outOfBounds_ = false; 
        }
        //@PDD jdbc40 (JDUtilities.JDBCLevel_ >= 20 incorrect logic, but n/a now
        else if(!(object instanceof Clob) && //@PDC NClob extends Clob
                !(object instanceof Reader) && //@PDC jdbc40
                !(object instanceof InputStream) 
/* ifdef JDBC40 */
                &&  !(object instanceof SQLXML)
/* endif */ 
                ) //@PDC jdbc40

        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        }

        savedObject_ = object;
        if(scale != -1) length_ = scale;
    }

    private void doConversion()
    throws SQLException
    {
        try
        {
            Object object = savedObject_;
            if(savedObject_ instanceof String)
            {
                value_ = (String)object;
            }
            else if(object instanceof Reader)
            {
                if(length_ >= 0)
                {
                    try
                    {
                        int blockSize = length_ < AS400JDBCPreparedStatement.LOB_BLOCK_SIZE ? length_ : AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
                        Reader stream = (Reader)object;
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
                        value_ = buf.toString();

                        if(value_.length() < length_)
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
                else if(length_ == -2) //@readerlen new else-if block (read all data)
                {
                    try
                    {
                        int blockSize = AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
                        Reader stream = (Reader)object;
                        StringBuffer buf = new StringBuffer();
                        char[] charBuffer = new char[blockSize];
                        // int totalCharsRead = 0;
                        int charsRead = stream.read(charBuffer, 0, blockSize);
                        while(charsRead > -1)
                        {
                            buf.append(charBuffer, 0, charsRead);
                            // totalCharsRead += charsRead;
                            // int charsRemaining = length_ - totalCharsRead;
                          
                            charsRead = stream.read(charBuffer, 0, blockSize);
                        }
                        value_ = buf.toString();

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
            else if(JDUtilities.JDBCLevel_ >= 20 && object instanceof Clob)
            {
                Clob clob = (Clob)object;
                value_ = clob.getSubString(1, (int)clob.length());
            }
/* ifdef JDBC40 */
            else if( object instanceof SQLXML ) //@PDA jdbc40 
            {
                SQLXML xml = (SQLXML)object;
                value_ = xml.getString();
            }
/* endif */ 
            else
            {
                JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
            }

            // Truncate if necessary.
            int valueLength = value_.length();
            if(valueLength > maxLength_)
            {
                value_ = value_.substring(0, maxLength_);
                truncated_ = valueLength - maxLength_;
                outOfBounds_ = false; 
            }
            else
            {
                truncated_ = 0; outOfBounds_ = false; 
            }

            length_ = value_.length();
        }
        finally
        {
            savedObject_ = null;
        }
    }

    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public int getSQLType()
    {
        return SQLData.CLOB;
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
        return "com.ibm.as400.access.AS400JDBCClob";
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
        return "CLOB"; 
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
        return 408;
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
        return "CLOB";
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
        return value_.length();
    }

    public int getTruncated()
    {
        return truncated_;
    }

    public boolean getOutOfBounds() {
      return outOfBounds_; 
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSIONS TO JAVA TYPES                               //
    //                                                         //
    //---------------------------------------------------------//


    public BigDecimal getBigDecimal(int scale)
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public InputStream getBinaryStream()
    throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false; 
        return new HexReaderInputStream(new StringReader(value_));
    }

    public Blob getBlob()
    throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false; 
        try
        {
            return new AS400JDBCBlob(BinaryConverter.stringToBytes(value_), maxLength_);
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
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false; 
        try
        {
            return BinaryConverter.stringToBytes(value_);
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
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false; 
        return new StringReader(value_);
    }

    public Clob getClob()
    throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false; 
        return new AS400JDBCClob(value_, maxLength_);
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
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false; 
        return new AS400JDBCClob(value_, maxLength_);
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
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false; 
        return value_;     
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
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false; 
        try
        {
            return new ReaderInputStream(new StringReader(value_), 13488);
        }
        catch(UnsupportedEncodingException e)
        {
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
            return null;
        }
    }

    //@pda jdbc40
    public Reader getNCharacterStream() throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false; 
        return new StringReader(value_);
    }
    
    //@pda jdbc40
/* ifdef JDBC40 */
    public NClob getNClob() throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false; 
        return new AS400JDBCNClob(value_, maxLength_);
    }
/* endif */ 
    //@pda jdbc40
    public String getNString() throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false; 
        return value_;     
    }

    //@pda jdbc40
/* ifdef JDBC40 */
    public RowId getRowId() throws SQLException
    {
        //
        //if(savedObject_ != null) doConversion();
        //truncated_ = 0; outOfBounds_ = false; 
        //try
        //{
        //    return new AS400JDBCRowId(BinaryConverter.stringToBytes(value_));
        //}
        //catch(NumberFormatException nfe)
        //{
            // this Clob contains non-hex characters
            //JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, nfe);
            //return null;
        //} 
        //decided this is of no use
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
/* endif */ 
    //@pda jdbc40
/* ifdef JDBC40 */
    public SQLXML getSQLXML() throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false; 
        return new AS400JDBCSQLXML(value_);     
    }
/* endif */ 

    // @array
}

