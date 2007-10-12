///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLGraphic.java
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

final class SQLGraphic
implements SQLData
{
    private static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";

    // Private data.
    private SQLConversionSettings   settings_;
    private int                     maxLength_;
    private int                     truncated_;
    private String                  value_;
    private String                  originalValue_;
    private int                     ccsid_; //@cca1

    SQLGraphic(int maxLength, SQLConversionSettings settings, int ccsid)  //@cca1
    {
        settings_       = settings;
        maxLength_      = maxLength;
        truncated_      = 0;
        value_          = "";
        originalValue_  = "";
        ccsid_          = ccsid;  //@cca1
    }

    public Object clone()
    {
        return new SQLGraphic(maxLength_, settings_, ccsid_); //@cca1
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

        BidiConversionProperties bidiConversionProperties = new BidiConversionProperties(bidiStringType);  //@KBA
        bidiConversionProperties.setBidiImplicitReordering(settings_.getBidiImplicitReordering());         //@KBA
        bidiConversionProperties.setBidiNumericOrderingRoundTrip(settings_.getBidiNumericOrdering());      //@KBA

        value_ = ccsidConverter.byteArrayToString(rawBytes, offset, maxLength_, bidiConversionProperties);  //@KBC changed to use bidiConversionProperties instead of bidiStringType
    }

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
    throws SQLException
    {
        //   We originally padded with a single byte space.  We now have the
        //   ccsid so we can figure out if that was right or not.  If we should
        //   have use the double byte space, re-pad.
        int ccsid = ccsidConverter.getCcsid();
        if(ccsid != 13488 && ccsid != 1200)
        {
            int valueLength = originalValue_.length();
            int exactLength = getDisplaySize();
            if(valueLength < exactLength)
            {
                StringBuffer buffer = new StringBuffer(originalValue_);
                char c = '\u3000';
                for(int i = valueLength; i < exactLength; ++i)
                    buffer.append(c);
                value_ = buffer.toString();
            }
        }

        int bidiStringType = settings_.getBidiStringType();

        // if bidiStringType is not set by user, use ccsid to get value
        if(bidiStringType == -1)
            bidiStringType = ccsidConverter.bidiStringType_;
        
        BidiConversionProperties bidiConversionProperties = new BidiConversionProperties(bidiStringType);  //@KBA
        bidiConversionProperties.setBidiImplicitReordering(settings_.getBidiImplicitReordering());         //@KBA
        bidiConversionProperties.setBidiNumericOrderingRoundTrip(settings_.getBidiNumericOrdering());      //@KBA

        try
        {
            ccsidConverter.stringToByteArray(value_, rawBytes, offset, maxLength_, bidiConversionProperties);   //@KBC changed to use bidiConversionProperties instead of bidiStringType
        }
        catch(CharConversionException e)
        {
            maxLength_ = ccsidConverter.stringToByteArray(value_, bidiConversionProperties).length; //@KBC changed to use bidiConversionProperties instead of bidiStringType
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
        String value = null;

        if(object instanceof String)
            value = (String) object;

        else if(object instanceof Number)
            value = object.toString();

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
            value = SQLTime.timeToString((Time) object, settings_, calendar);

        else if(object instanceof Timestamp)
            value = SQLTimestamp.timestampToString((Timestamp) object, calendar);

        else if(object instanceof java.util.Date)
            value = SQLDate.dateToString((java.util.Date) object, settings_, calendar);

        else if(object instanceof URL)
            value = object.toString();

        else if(JDUtilities.JDBCLevel_ >= 20 && object instanceof Clob)
        {
            Clob clob = (Clob)object;
            value = clob.getSubString(1, (int)clob.length());
        }
        else if(object instanceof SQLXML) //@PDA jdbc40 
        {    
            SQLXML xml = (SQLXML)object;
            value = xml.getString();
        }     

        if(value == null)
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);

        value_ = value;
        originalValue_ = value;


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
        return SQLData.GRAPHIC;
    }

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
        return "GRAPHIC";
    }

    public int getMaximumPrecision()
    {
        return 16382;
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
        return 468;
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
        if( ccsid_ == 13488 || ccsid_ == 1200)  //@cca1
            return "NCHAR";  //@cca1 same as native
        return "GRAPHIC";
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
        truncated_ = 0;
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
            // this field contains non-hex characters
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
            // this field contains non-hex characters
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
        return new AS400JDBCClob(getString(), maxLength_);
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
        truncated_ = 0;
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
        truncated_ = 0;
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
    
    //@pda jdbc40
    public Reader getNCharacterStream() throws SQLException
    {
        truncated_ = 0;

        // This is written in terms of getNString(), since it will
        // handle truncating to the max field size if needed.
        return new StringReader(getNString());
    }
    
    //@pda jdbc40
    public NClob getNClob() throws SQLException
    {
        truncated_ = 0;

        // This is written in terms of getNString(), since it will
        // handle truncating to the max field size if needed.
        String string = getNString();
        return new AS400JDBCNClob(string, string.length());
    }

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

    //@pda jdbc40
    public SQLXML getSQLXML() throws SQLException
    {
        //This is written in terms of getString(), since it will
        // handle truncating to the max field size if needed.
        truncated_ = 0;
        return new AS400JDBCSQLXML(getString().toCharArray());     
    }
}

