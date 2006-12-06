///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLNChar.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2006-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.ByteArrayInputStream;
import java.io.CharConversionException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
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
import java.net.URL;

//@PDA jdbc40 new class
final class SQLNChar
implements SQLData
{
   
    // Private data.
    private SQLConversionSettings   settings_;
    private int                     maxLength_;
    private int                     truncated_;
    private String                  value_;

    SQLNChar(int maxLength, SQLConversionSettings settings)
    {
        settings_       = settings;
        maxLength_      = maxLength;
        truncated_      = 0;
        value_          = "";  
    }

    public Object clone()
    {
        return new SQLNChar(maxLength_,settings_);
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
    throws SQLException
    {
        int bidiStringType = settings_.getBidiStringType();

        // if bidiStringType is not set by user, use ccsid to get value
        if(bidiStringType == -1)
            bidiStringType = ccsidConverter.bidiStringType_;

        BidiConversionProperties bidiConversionProperties = new BidiConversionProperties(bidiStringType);   
        bidiConversionProperties.setBidiImplicitReordering(settings_.getBidiImplicitReordering());          
        bidiConversionProperties.setBidiNumericOrderingRoundTrip(settings_.getBidiNumericOrdering());     

        value_ = ccsidConverter.byteArrayToString(rawBytes, offset, maxLength_, bidiConversionProperties);   
    }

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
    throws SQLException
    {
        int bidiStringType = settings_.getBidiStringType();

        // if bidiStringType is not set by user, use ccsid to get value
        if(bidiStringType == -1)
            bidiStringType = ccsidConverter.bidiStringType_;

        BidiConversionProperties bidiConversionProperties = new BidiConversionProperties(bidiStringType);  
        bidiConversionProperties.setBidiImplicitReordering(settings_.getBidiImplicitReordering());         
        bidiConversionProperties.setBidiNumericOrderingRoundTrip(settings_.getBidiNumericOrdering());     

        try
        {
            ccsidConverter.stringToByteArray(value_, rawBytes, offset, maxLength_, bidiConversionProperties);  
        }
        catch(CharConversionException e)
        {
            maxLength_ = ccsidConverter.stringToByteArray(value_, bidiConversionProperties).length;    
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e, "Change Descriptor");
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
        String value = null;                                                         

        if(object instanceof String)
            value = (String)object;                                                 

        else if(object instanceof Character)
            value = object.toString();

        else if(object instanceof Number)
            value = object.toString();                                              

        else if(object instanceof Boolean)
        { 
            // if "translate boolean" == false, then use "0" and "1" values to match native driver
            if(settings_.getTranslateBoolean() == true)
                value = object.toString();  //"true" or "false"     
            else
                value = ((Boolean)object).booleanValue() == true ? "1" : "0";
        }
        else if(object instanceof Time)
            value = SQLTime.timeToString((Time)object, settings_, calendar);        

        else if(object instanceof Timestamp)
            value = SQLTimestamp.timestampToString((Timestamp)object, calendar);    

        else if(object instanceof java.util.Date)                                    
            value = SQLDate.dateToString((java.util.Date)object, settings_, calendar);  

        else if(object instanceof URL)
            value = object.toString();

        else if(JDUtilities.JDBCLevel_ >= 20 && object instanceof Clob)
        {
            Clob clob = (Clob)object;
            value = clob.getSubString(1, (int)clob.length());
        }                                                                           

        if(value == null)                                                           
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);

        value_ = value;                                                            

        // Set to the exact length.
        int valueLength = value_.length();
        int exactLength = getDisplaySize();                        
        if(valueLength < exactLength)                                
        {
            StringBuffer buffer = new StringBuffer(value_);
            char c = '\u0020';                                     
            for(int i = valueLength; i < exactLength; ++i)           
                buffer.append(c);                                   
            value_ = buffer.toString();
            truncated_ = 0;
        }
        else if(valueLength > exactLength)
        {                                                           
            value_ = value_.substring(0, exactLength);             
            truncated_ = valueLength - exactLength;                  
        }
        else
            truncated_ = 0;
    }

    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public int getSQLType()
    {
        return SQLData.NCHAR;
    }

    public String getCreateParameters()
    {
        return AS400JDBCDriver.getResource("MAXLENGTH");
    }

    public int getDisplaySize()
    {
        return maxLength_;
    }

    // JDBC 3.0
    public String getJavaClassName()
    {
        return "java.lang.String";
    }

    public String getLiteralPrefix()
    {
        return "\'";
    }

    public String getLiteralSuffix()
    {
        return "\'";
    }

    public String getLocalName()
    {
        return "NCHAR";
    }

    public int getMaximumPrecision()
    {
        return 32765;
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
        return 452;
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
        //return java.sql.Types.NCHAR;
        return -15;  //@PDC jdbc40 merge.  Types.NCHAR not in pre-1.6.  
    }

    public String getTypeName()
    {
        return "NCHAR";
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
            return new ByteArrayInputStream(ConvTable.getTable(819, null).stringToByteArray(getString()));
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
        try
        {
            BigDecimal bigDecimal = new BigDecimal(SQLDataFactory.convertScientificNotation(getString().trim()));  
            if(scale >= 0)
            {
                if(scale >= bigDecimal.scale())
                {
                    truncated_ = 0;
                    return bigDecimal.setScale(scale);
                }
                else
                {
                    truncated_ = bigDecimal.scale() - scale;
                    return bigDecimal.setScale(scale, BigDecimal.ROUND_HALF_UP);
                }
            }
            else
                return bigDecimal;
        }
        catch(NumberFormatException e)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, e);
            return null;
        }
    }

    public InputStream getBinaryStream()
    throws SQLException
    {
        truncated_ = 0;
        return new HexReaderInputStream(new StringReader(getString()));
    }

    public Blob getBlob()
    throws SQLException
    {
        truncated_ = 0;

        try
        {
            return new AS400JDBCBlob(BinaryConverter.stringToBytes(getString()), maxLength_);
        }
        catch(NumberFormatException nfe)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, nfe);
            return null;
        }
    }

    public boolean getBoolean()
    throws SQLException
    {
        truncated_ = 0;

        // If value equals "true", "false", "1", or "0", then return the
        // corresponding boolean, otherwise an empty string is
        // false, a non-empty string is true.
        String trimmedValue = getString().trim();        
        return((trimmedValue.length() > 0) 
               && (! trimmedValue.equalsIgnoreCase("false"))
               && (! trimmedValue.equals("0")));
    }

    public byte getByte()
    throws SQLException
    {
        truncated_ = 0;

        try
        {
            return(new Double(getString().trim())).byteValue();
        }
        catch(NumberFormatException e)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, e);
            return -1;
        }
    }

    public byte[] getBytes()
    throws SQLException
    {
        truncated_ = 0;

        try
        {
            return BinaryConverter.stringToBytes(getString());
        }
        catch(NumberFormatException nfe)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, nfe);
            return null;
        }
    }

    public Reader getCharacterStream()
    throws SQLException
    {
        truncated_ = 0;

        // This is written in terms of getString(), since it will
        // handle truncating to the max field size if needed.
        return new StringReader(getString());
    }

    public Clob getClob()
    throws SQLException
    {
        truncated_ = 0;

        // This is written in terms of getString(), since it will
        // handle truncating to the max field size if needed.
        String string = getString();
        return new AS400JDBCClob(string, string.length());
    }

    public Date getDate(Calendar calendar)
    throws SQLException
    {
        truncated_ = 0;
        return SQLDate.stringToDate(getString(), settings_, calendar);
    }

    public double getDouble()
    throws SQLException
    {
        truncated_ = 0;

        try
        {
            return(new Double(getString().trim())).doubleValue();
        }
        catch(NumberFormatException e)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, e);
            return -1;
        }
    }

    public float getFloat()
    throws SQLException
    {
        truncated_ = 0;

        try
        {
            return(new Double(getString().trim())).floatValue();
        }
        catch(NumberFormatException e)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, e);
            return -1;
        }
    }

    public int getInt()
    throws SQLException
    {
        truncated_ = 0;

        try
        {
            return(new Double(getString().trim())).intValue();
        }
        catch(NumberFormatException e)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, e);
            return -1;
        }
    }

    public long getLong()
    throws SQLException
    {
        truncated_ = 0;

        try
        {
            return(new Double(getString().trim())).longValue();
        }
        catch(NumberFormatException e)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, e);
            return -1;
        }
    }

    public Object getObject()
    throws SQLException
    {
        // This is written in terms of getString(), since it will
        // handle truncating to the max field size if needed.
        return getString();
    }

    public short getShort()
    throws SQLException
    {
        truncated_ = 0;

        try
        {
            return(new Double(getString().trim())).shortValue();
        }
        catch(NumberFormatException e)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, e);
            return -1;
        }
    }

    public String getString()
    throws SQLException
    {
        // Truncate to the max field size if needed.
        // Do not signal a DataTruncation per the spec. 
        int maxFieldSize = settings_.getMaxFieldSize();
        if((value_.length() > maxFieldSize) && (maxFieldSize > 0))
        {
            return value_.substring(0, maxFieldSize);
        }
        else
        {
            return value_;
        }
    }

    public Time getTime(Calendar calendar)
    throws SQLException
    {
        truncated_ = 0;
        return SQLTime.stringToTime(getString(), settings_, calendar);
    }

    public Timestamp getTimestamp(Calendar calendar)
    throws SQLException
    {
        truncated_ = 0;
        return SQLTimestamp.stringToTimestamp(getString(), calendar);
    }

    public InputStream getUnicodeStream()
    throws SQLException
    {
        truncated_ = 0;

        try
        {
            return new ReaderInputStream(new StringReader(getString()), 13488);
        }
        catch(UnsupportedEncodingException e)
        {
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
            return null;
        }
    }

    
    // Added method trim() to trim the string.
    public void trim()                                
    {                                                
        value_ = value_.trim();                      
    }                                                 
    


    public Reader getNCharacterStream() throws SQLException
    {
        truncated_ = 0;

        // This is written in terms of getNString(), since it will
        // handle truncating to the max field size if needed.
        return new StringReader(getNString());
    }
    

    public NClob getNClob() throws SQLException
    {
        truncated_ = 0;

        // This is written in terms of getNString(), since it will
        // handle truncating to the max field size if needed.
        String string = getNString();
        return new AS400JDBCNClob(string, string.length());
    }


    public String getNString() throws SQLException
    {
        // Truncate to the max field size if needed.
        // Do not signal a DataTruncation per the spec.
        int maxFieldSize = settings_.getMaxFieldSize();
        if((value_.length() > maxFieldSize) && (maxFieldSize > 0))
        {
            return value_.substring(0, maxFieldSize);
        }
        else
        {
            return value_;
        } 
    }


    public RowId getRowId() throws SQLException
    {
        /*
        truncated_ = 0;
        try
        {
            return new AS400JDBCRowId(BinaryConverter.stringToBytes(value_));
        }
        catch(NumberFormatException nfe)
        {
            // this string contains non-hex characters
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, nfe);
            return null;
        }
        */
        //decided this is of no use
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

  
    public SQLXML getSQLXML() throws SQLException
    {
        //This is written in terms of getString(), since it will
        // handle truncating to the max field size if needed.
        truncated_ = 0;
        return new AS400JDBCSQLXML(getString().toCharArray());     
    }
}

