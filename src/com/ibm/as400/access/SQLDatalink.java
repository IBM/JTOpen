///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLDatalink.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.net.URL;                     // @d2a



final class SQLDatalink
implements SQLData
{
    private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    // Private data.
    private int                     length_;
    private int                     maxLength_;
    private SQLConversionSettings   settings_;
    private String                  value_;




    SQLDatalink (int maxLength, SQLConversionSettings settings)
    {
        length_         = 0;
        maxLength_      = maxLength;
        settings_       = settings;
        value_          = ""; // @A1C
    }



    public Object clone ()
    {
        return new SQLDatalink (maxLength_, settings_);
    }



    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//



    public void convertFromRawBytes (byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
        length_ = BinaryConverter.byteArrayToUnsignedShort (rawBytes, offset);
        value_ = ccsidConverter.byteArrayToString (rawBytes, offset+2, length_);
    }



    public void convertToRawBytes (byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
        BinaryConverter.unsignedShortToByteArray (length_, rawBytes, offset);
        try
        {
            ccsidConverter.stringToByteArray (value_, rawBytes, offset + 2, length_);
        }
        catch(Exception e)
        {
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);              // @C2A
        }
    }



    //---------------------------------------------------------//
    //                                                         //
    // SET METHODS                                             //
    //                                                         //
    //---------------------------------------------------------//



    public void set (Object object, Calendar calendar, int scale)
    throws SQLException
    {
        if(object instanceof String)
            value_ = (String) object;

        else
            JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);

        length_ = value_.length ();
    }



    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public int getSQLType()
    {
        return SQLData.DATALINK;
    }

    public String getCreateParameters ()
    {
        return AS400JDBCDriver.getResource ("MAXLENGTH");
    }


    public int getDisplaySize ()
    {
        return maxLength_;
    }

    //@D1A JDBC 3.0
    //@d2a - method rewritten for URLs
    //@j4c - rewritten now that JDUtilites knows the JDBC level
    public String getJavaClassName()
    {
        if(JDUtilities.JDBCLevel_ >= 30)
            return "java.net.URL";
        else
            return "java.lang.Datalink";       
    }

    public String getLiteralPrefix ()
    {
        return "\'";
    }


    public String getLiteralSuffix ()
    {
        return "\'";
    }


    public String getLocalName ()
    {
        return "DATALINK"; 
    }


    public int getMaximumPrecision ()
    {
        return 32717;
    }


    public int getMaximumScale ()
    {
        return 0;
    }


    public int getMinimumScale ()
    {
        return 0;
    }


    public int getNativeType ()
    {
        return 396;
    }


    public int getPrecision ()
    {
        return maxLength_;
    }


    public int getRadix ()
    {
        return 0;
    }


    public int getScale ()
    {
        return 0;
    }


    public int getType ()
    {
        if(JDUtilities.JDBCLevel_ >= 30)                                //@J5A
            return 70;  //java.sql.Types.DATALINK without requiring 1.4    //@J5A
        else                                                             //@J5A
            return java.sql.Types.VARCHAR;
    }



    public String getTypeName ()
    {
        return "DATALINK"; 
    }



    // @C1D public boolean isGraphic ()
    // @C1D {
    // @C1D    return false;
    // @C1D }



    public boolean isSigned ()
    {
        return false;
    }



    public boolean isText ()
    {
        return true;
    }




    //---------------------------------------------------------//
    //                                                         //
    // CONVERSIONS TO JAVA TYPES                               //
    //                                                         //
    //---------------------------------------------------------//



    public int getActualSize ()
    {
        return value_.length();
    }



    public int getTruncated ()
    {
        return 0;
    }



    public InputStream toAsciiStream ()
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }



    public BigDecimal toBigDecimal (int scale)
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }



    public InputStream toBinaryStream ()
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }



    public Blob toBlob ()
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }



    public boolean toBoolean ()
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return false;
    }



    public byte toByte ()
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }



    public byte[] toBytes ()
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }



    public Reader toCharacterStream ()
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }



    public Clob toClob ()
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }



    public Date toDate (Calendar calendar)
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }



    public double toDouble ()
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }



    public float toFloat ()
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }



    public int toInt ()
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }



    public long toLong ()
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }



    // @d2a entire method reworked.  Used to simply return value_.
    // @j4c - rewritten now that JDUtilites knows the JDBC level
    public Object toObject ()
    {
        // if JDBC 3.0 or later return a URL instead of a string.
        // If we are not able to turn the string into a URL then return
        // the string (that is why there is no "else".  That shouldn't
        // happen because the database makes sure the cell contains
        // a valid URL for this data type.
        if(JDUtilities.JDBCLevel_ >= 30)
            try
            {
                return new java.net.URL(value_);
            }
            catch(Exception e)
            {
            }

        return value_;
    }



    public short toShort ()
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }



    public String toString ()
    {
        return value_;
    }



    public Time toTime (Calendar calendar)
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }



    public Timestamp toTimestamp (Calendar calendar)
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }



    public InputStream toUnicodeStream ()
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }



}

