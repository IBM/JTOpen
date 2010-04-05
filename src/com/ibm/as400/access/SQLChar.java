///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLChar.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
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
import java.net.URL;

final class SQLChar
implements SQLData
{
    static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";

    // Private data.
    private SQLConversionSettings   settings_;
    private int                     maxLength_;
    private int                     truncated_;
    private String                  value_;

    SQLChar(int maxLength, SQLConversionSettings settings)
    {
        settings_       = settings;
        maxLength_      = maxLength;
        truncated_      = 0;
        value_          = ""; // @C4C
    }

    public Object clone()
    {
        return new SQLChar(maxLength_,settings_);
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

        BidiConversionProperties bidiConversionProperties = new BidiConversionProperties(bidiStringType);   //@KBA
        bidiConversionProperties.setBidiImplicitReordering(settings_.getBidiImplicitReordering());          //@KBA
        bidiConversionProperties.setBidiNumericOrderingRoundTrip(settings_.getBidiNumericOrdering());       //@KBA

        try{
            value_ = ccsidConverter.byteArrayToString(rawBytes, offset, maxLength_, bidiConversionProperties);  //@KBC bidiStringType);
        }catch(Exception e){
            JDError.throwSQLException(JDError.EXC_CHAR_CONVERSION_INVALID, e);
        }
    }

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
    throws SQLException
    {
        int bidiStringType = settings_.getBidiStringType();

        // if bidiStringType is not set by user, use ccsid to get value
        if(bidiStringType == -1)
            bidiStringType = ccsidConverter.bidiStringType_;

        BidiConversionProperties bidiConversionProperties = new BidiConversionProperties(bidiStringType);   //@KBA
        bidiConversionProperties.setBidiImplicitReordering(settings_.getBidiImplicitReordering());          //@KBA
        bidiConversionProperties.setBidiNumericOrderingRoundTrip(settings_.getBidiNumericOrdering());       //@KBA

        try
        {
            //in cases of variable length chars, check for truncation after conversion
            truncated_ = ccsidConverter.stringToByteArray(value_, rawBytes, offset, maxLength_, bidiConversionProperties); //@KBC bidiStringType);  //@trnc
        }
        catch(CharConversionException e)
        {
            maxLength_ = ccsidConverter.stringToByteArray(value_, bidiConversionProperties).length;   //@KBC changed to use bidiConversionProperties_ instead of bidiStringType
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
        String value = null;                                                        // @C2A

        if(object instanceof String)
            value = (String)object;                                                 // @C2C

        else if(object instanceof Character)
            value = object.toString();

        else if(object instanceof Number)
            value = object.toString();                                              // @C2C

        else if(object instanceof Boolean)
        { 
            // @PDC
            // if "translate boolean" == false, then use "0" and "1" values to match native driver
            if(settings_.getTranslateBoolean() == true)
                value = object.toString();  //"true" or "false"     
            else
                value = ((Boolean)object).booleanValue() == true ? "1" : "0";
        }
        else if(object instanceof Time)
            value = SQLTime.timeToString((Time)object, settings_, calendar);        // @C2C

        else if(object instanceof Timestamp)
            value = SQLTimestamp.timestampToString((Timestamp)object, calendar);    // @C2C

        else if(object instanceof java.util.Date)                                   // @F5M @F5C
            value = SQLDate.dateToString((java.util.Date)object, settings_, calendar); // @C2C @F5C

        else if(object instanceof URL)
            value = object.toString();

        else if(JDUtilities.JDBCLevel_ >= 20 && object instanceof Clob)
        {
            Clob clob = (Clob)object;
            value = clob.getSubString(1, (int)clob.length());
        }      
        // @C2C
        /* ifdef JDBC40

        else if(object instanceof SQLXML) //@PDA jdbc40 
        {    
            SQLXML xml = (SQLXML)object;
            value = xml.getString();
        }     
endif */ 
        if(value == null)                                                           // @C2C
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);

        value_ = value;                                                             // @C2A

        // Set to the exact length.
        int valueLength = value_.length();
        int exactLength = getDisplaySize();                         // @C1A
        if(valueLength < exactLength)                               // @C1C
        {
            StringBuffer buffer = new StringBuffer(value_);
            char c = '\u0020';                                      // @F66c - Pad with single byte space for now
            for(int i = valueLength; i < exactLength; ++i)          // @C1C
                buffer.append(c);                                   //@F6C
            value_ = buffer.toString();
            truncated_ = 0;
        }
        else if(valueLength > exactLength)
        {                                                           // @C1C @F6C
            value_ = value_.substring(0, exactLength);              // @C1C
            truncated_ = valueLength - exactLength;                 // @C1C
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
        return SQLData.CHAR;
    }

    public String getCreateParameters()
    {
        return AS400JDBCDriver.getResource("MAXLENGTH");
    }

    public int getDisplaySize()
    {
        return maxLength_;
    }

    //@F1A JDBC 3.0
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
        return "CHAR";
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
        return java.sql.Types.CHAR;
    }

    public String getTypeName()
    {
        return "CHAR";
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
            BigDecimal bigDecimal = new BigDecimal(SQLDataFactory.convertScientificNotation(getString().trim())); // @F3C
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
            //return(new Double(getString().trim())).byteValue();           //@trunc
            Double doubleValue  = new Double (value_.trim ());              //@trunc
            double d = doubleValue.doubleValue();                           //@trunc
            if(d > Byte.MAX_VALUE || d < Byte.MIN_VALUE)                    //@trunc
                truncated_ = 1;                                             //@trunc

            return doubleValue.byteValue ();                                //@trunc
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
        if(calendar == null) //@dat1
        {
            //getter methods do not enforce strict conversion
            calendar = Calendar.getInstance(); //@dat1
        }
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
            //return(new Double(getString().trim())).intValue();   //@trunc
            Double doubleValue  = new Double (value_.trim ());     //@trunc
            double d = doubleValue.doubleValue();                  //@trunc 

            if( d > Integer.MAX_VALUE || d < Integer.MIN_VALUE)    //@trunc    
                truncated_ = 1;                                    //@trunc
                 
            return doubleValue.intValue ();                        //@trunc
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
            //return(new Double(getString().trim())).longValue();  //@trunc
            Double doubleValue  = new Double (value_.trim ()); //@trunc
            double d = doubleValue.doubleValue();              //@trunc

            if( d > Long.MAX_VALUE || d < Long.MIN_VALUE)      //@trunc
                truncated_ = 1;                                //@trunc

            return doubleValue.longValue ();                   //@trunc
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
            //return(new Double(getString().trim())).shortValue();           //@trunc
            Double doubleValue  = new Double (value_.trim ());               //@trunc
            double d = doubleValue.doubleValue();                            //@trunc

            if( d > Short.MAX_VALUE || d < Short.MIN_VALUE)                  //@trunc      
                truncated_ = 1;                                              //@trunc

            return doubleValue.shortValue ();                                //@trunc
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
        // Do not signal a DataTruncation per the spec. @B1A
        int maxFieldSize = settings_.getMaxFieldSize();
        if((value_.length() > maxFieldSize) && (maxFieldSize > 0))
        {
            // @B1D truncated_ = value_.length() - maxFieldSize;
            return value_.substring(0, maxFieldSize);
        }
        else
        {
            // @B1D truncated_ = 0;
            return value_;
        }
    }

    public Time getTime(Calendar calendar)
    throws SQLException
    {
        truncated_ = 0;
        if(calendar == null) //@dat1
        {
            //getter methods do not enforce strict conversion
            calendar = Calendar.getInstance(); //@dat1
        }
        return SQLTime.stringToTime(getString(), settings_, calendar);
    }

    public Timestamp getTimestamp(Calendar calendar)
    throws SQLException
    {
        truncated_ = 0;
        if(calendar == null) //@dat1
        {
            //getter methods do not enforce strict conversion
            calendar = Calendar.getInstance(); //@dat1
        }
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

    // @A1A
    // Added method trim() to trim the string.
    public void trim()                               // @A1A
    {                                                // @A1A
        value_ = value_.trim();                      // @A1A
    }                                                // @A1A
    

    //@pda jdbc40
    public Reader getNCharacterStream() throws SQLException
    {
        truncated_ = 0;

        // This is written in terms of getNString(), since it will
        // handle truncating to the max field size if needed.
        return new StringReader(getNString());
    }
    
    //@pda jdbc40
    /* ifdef JDBC40

    public NClob getNClob() throws SQLException
    {
        truncated_ = 0;

        // This is written in terms of getNString(), since it will
        // handle truncating to the max field size if needed.
        String string = getNString();
        return new AS400JDBCNClob(string, string.length());
    }
    endif */ 

    //@pda jdbc40
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

    //@pda jdbc40
    /* ifdef JDBC40

    public RowId getRowId() throws SQLException
    {
        //truncated_ = 0;
        //try
        //{
        //    return new AS400JDBCRowId(BinaryConverter.stringToBytes(value_));
        //}
        //catch(NumberFormatException nfe)
        //{
            // this string contains non-hex characters
        //    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, nfe);
        //    return null;
        //}
        //decided this is of no use
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
   endif */ 
    //@pda jdbc40
   /* ifdef JDBC40

    public SQLXML getSQLXML() throws SQLException
    {
        //This is written in terms of getString(), since it will
        // handle truncating to the max field size if needed.
        truncated_ = 0;
        return new AS400JDBCSQLXML(getString().toCharArray());     
    }
   endif */ 
    // @array
    public Array getArray() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
}

