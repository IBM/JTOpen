///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLInteger.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.Reader;
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

final class SQLInteger
implements SQLData
{
    private static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";

    // Private data.
    private int                 truncated_;
    private int                 value_;
    private int                 scale_;                              // @A0A
    private BigDecimal          bigDecimalValue_ = null;             // @A0A
    private int                 vrm_;                                //trunc3

    SQLInteger(int vrm)          //@trunc3
    {
        this(0, vrm);            //@trunc3
    }

    SQLInteger(int scale, int vrm)                      // @A0A //@trunc3
    {
        truncated_          = 0;
        value_              = 0;
        scale_              = scale;                                      // @A0A
        if(scale_ > 0)                                                   // @C0A
            bigDecimalValue_    = new BigDecimal(Integer.toString(value_)); // @A0A
        vrm_                = vrm;  //@trunc3
    }

    public Object clone()
    {
        return new SQLInteger(scale_, vrm_);  //@trunc3
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
        value_ = BinaryConverter.byteArrayToInt(rawBytes, offset);                               // @D0C

        if(scale_ > 0)
        {                                                                        // @C0A
            bigDecimalValue_ = (new BigDecimal(Integer.toString(value_))).movePointLeft(scale_); // @A0A
            value_ = bigDecimalValue_.intValue();                                                // @A0A
        }                                                                                        // @C0A
    }

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
        BinaryConverter.intToByteArray(value_, rawBytes, offset);                                // @D0C
    }

    //---------------------------------------------------------//
    //                                                         //
    // SET METHODS                                             //
    //                                                         //
    //---------------------------------------------------------//

    public void set(Object object, Calendar calendar, int scale)
    throws SQLException
    {
        truncated_ = 0;                                                     // @D9c

        if(object instanceof String)
        {
            // @D10c new implementation
            // old ...
            //
            // try
            // {
            //     value_ = Integer.parseInt((String) object);
            // }
            // catch(NumberFormatException e)
            // {
            //     JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
            // }
            //
            // new ...

            // @P1 First try to convert the string to an int (no extra object creation).  If
            //     that fails try turning it into a Double, which will involve an extra object
            //     create but Double will accept bigger numbers and floating point numbers so it 
            //     will catch more truncation cases.  The bottom line is don't create an extra
            //     object in the normal case.  If the user does ps.setString(1, "111222333.444.555")
            //     on an integer field, they can't expect the best performance. 
            boolean tryAgain = false;                                                    // @P1a

            try
            {
                // @P1d long longValue = (long) Double.parseDouble((String) object); 
                long longValue = (long) Long.parseLong((String) object);              // @P1a

                if(( longValue > Integer.MAX_VALUE ) || ( longValue < Integer.MIN_VALUE ))
                {
                    truncated_ = 4;                                                           // @D9c
                    //@trunc3 match native for ps.setString() to throw mismatch instead of truncation
                    if(vrm_ >= JDUtilities.vrm610)                                       //@trunc3
                    {                                                                    //@trunc3
                        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH); //@trunc3
                    }                                                                    //@trunc3
                }
                value_ = (int) longValue;
            }
            catch(NumberFormatException e)
            {
                tryAgain = true;                                                          // @P1a
                // @P1d JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
            }

            if(tryAgain)                                                                // @P1a
            {
                // @P1a
                try                                                                       // @P1a
                {
                    // @P1a
                    double doubleValue = Double.valueOf((String) object).doubleValue();  // @P1a
                                                                                           // @P1a
                    if(( doubleValue > Integer.MAX_VALUE ) || ( doubleValue < Integer.MIN_VALUE )) // @P1a
                    {
                        // @P1a
                        truncated_ = 4;                                                    // @P1a
                        //@trunc3 match native for ps.setString() to throw mismatch instead of truncation
                        if(vrm_ >= JDUtilities.vrm610)                                       //@trunc3
                        {                                                                    //@trunc3
                            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH); //@trunc3
                        }                                                                    //@trunc3
                    }                                                                      // @P1a
                    value_ = (int) doubleValue;                                          // @P1a  
                }                                                                         // @P1a
                catch(NumberFormatException e)                                           // @P1a
                {
                    // @P1a
                    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);            // @P1a
                }                                                                         // @P1a
            }                                                                            // @P1a
        }

        else if(object instanceof Number)
        {
            // Compute truncation by getting the value as a long
            // and comparing it against MAX_VALUE/MIN_VALUE.  You
            // do this because truncation of the decimal portion of
            // the value is insignificant.  We only care if the
            // whole number portion of the value is too large/small
            // for the column.
            long longValue = ((Number) object).longValue();                              // @D9c
            if(( longValue > Integer.MAX_VALUE ) || ( longValue < Integer.MIN_VALUE ))   // @D9c
            {
                // Note:  Truncated here is set to 4 bytes.  This is based on
                //        the idea that a long was used and an int was the
                //        column type.  We could check for different types
                //        and provide a more accurate number, but I don't
                //        really know that this field is of any use to people
                //        in this case anyway (for example, you could have a
                //        float (4 bytes) that didn't fit into a bigint (8
                //        bytes) without some data truncation.
                truncated_ = 4;                                                           // @D9c
            }

            // Store the value.
            value_ = (int) longValue;                                                     // @D9c
        }

        else if(object instanceof Boolean)
            value_ = (((Boolean) object).booleanValue() == true) ? 1 : 0;

        else
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);

        if(scale_ > 0)
        {                                                                        // @C0A
            bigDecimalValue_ = (new BigDecimal(Integer.toString(value_))).movePointLeft(scale_); // @A0A
            value_ = bigDecimalValue_.intValue();                                                // @A0A
        }                                                                                        // @C0A
    }

    public void set(int value)                                                          // @E2A
    {                                                                                   // @E2A
        value_ = value;                                                                 // @E2A
    }                                                                                   // @E2A

    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public int getSQLType()
    {
        return SQLData.INTEGER;
    }

    public String getCreateParameters()
    {
        return null;
    }

    public int getDisplaySize()
    {
        return 11;
    }

    //@F1A JDBC 3.0
    public String getJavaClassName()
    {
        return "java.lang.Integer";
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
        return "INTEGER";
    }

    public int getMaximumPrecision()
    {
        return 10;
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
        return 496;
    }

    public int getPrecision()
    {
        return 10;
    }

    public int getRadix()
    {
        return 10;
    }

    public int getScale()
    {
        return scale_;
    }

    public int getType()
    {
        return java.sql.Types.INTEGER;
    }

    public String getTypeName()
    {
        return "INTEGER";
    }

    public boolean isSigned()
    {
        return true;
    }

    public boolean isText()
    {
        return false;
    }

    public int getActualSize()
    {
        return 4; // @D0C
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
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public BigDecimal getBigDecimal(int scale)
    throws SQLException
    {
        truncated_ = 0;
        if(scale_ > 0)
        {                                                   // @C0A
            if(scale >= 0)
                return bigDecimalValue_.setScale(scale);                    // @A0A
            else
                return bigDecimalValue_;
        }                                                                   // @C0A
        else
        {                                                              // @C0A
            if(scale <= 0)                                                 // @C0A
                return BigDecimal.valueOf((long) value_);                  // @C0A
            else                                                            // @C0A
                return BigDecimal.valueOf((long) value_).setScale(scale); // @C0A
        }                                                                   // @C0A
    }

    public InputStream getBinaryStream()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Blob getBlob()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public boolean getBoolean()
    throws SQLException
    {
        truncated_ = 0;
        return(value_ != 0);
    }

    public byte getByte()
    throws SQLException
    {
        if(value_ > Byte.MAX_VALUE || value_ < Byte.MIN_VALUE)
        {
            if(value_ > Short.MAX_VALUE || value_ < Short.MIN_VALUE)
            {
                truncated_ = 3;
            }
            else
            {
                truncated_ = 1;
            }
        }
        return(byte) value_;
    }

    public byte[] getBytes()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Reader getCharacterStream()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Clob getClob()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
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
        truncated_ = 0;
        if(scale_ > 0)                                 // @C0A
            return bigDecimalValue_.doubleValue();      // @A0A
        else                                            // @C0A
            return(double) value_;                     // @A0D @C0A
    }

    public float getFloat()
    throws SQLException
    {
        truncated_ = 0;
        if(scale_ > 0)                                 // @C0A
            return bigDecimalValue_.floatValue();       // @A0A
        else                                            // @C0A
            return(float) value_;                      // @A0D @C0A
    }

    public int getInt()
    throws SQLException
    {
        truncated_ = 0;
        return value_;
    }

    public long getLong()
    throws SQLException
    {
        truncated_ = 0;
        return value_;
    }

    public Object getObject()
    throws SQLException
    {
        truncated_ = 0;
        return new Integer((int) value_);
    }

    public short getShort()
    throws SQLException
    {
        truncated_ = 0;
        if(value_ > Short.MAX_VALUE || value_ < Short.MIN_VALUE)
        {
            truncated_ = 2;
        }
        return(short) value_;
    }

    public String getString()
    throws SQLException
    {
        truncated_ = 0;
        if(scale_ > 0)                                 // @C0A
            return bigDecimalValue_.toString();         // @A0A
        else                                            // @C0A
            return Integer.toString(value_);           // @A0D @C0A
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

    public InputStream  getUnicodeStream()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
    

    //@pda jdbc40
    public Reader getNCharacterStream() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
    
    //@pda jdbc40
    public NClob getNClob() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    //@pda jdbc40
    public String getNString() throws SQLException
    {
        truncated_ = 0;
        if(scale_ > 0)                         
            return bigDecimalValue_.toString();
        else                                
            return Integer.toString(value_);
    }

    //@pda jdbc40
    public RowId getRowId() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    //@pda jdbc40
    public SQLXML getSQLXML() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    // @array
    public Array getArray() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
}

