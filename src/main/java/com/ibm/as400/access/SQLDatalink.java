///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: SQLDatalink.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2006 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Blob;
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
import java.net.MalformedURLException;

final class SQLDatalink
extends SQLDataBase
{
    static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    // Private data.
    private int                     length_;
    private int                     maxLength_;
    private String                  value_;

    SQLDatalink(int maxLength, SQLConversionSettings settings)
    {
        super(settings);
        length_         = 0;
        maxLength_      = maxLength;
        value_          = ""; // @A1C
    }

    public Object clone()
    {
        return new SQLDatalink(maxLength_, settings_);
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter, boolean ignoreConversionErrors) //@P0C
    throws SQLException
    {
        length_ = BinaryConverter.byteArrayToUnsignedShort(rawBytes, offset);
        value_ = ccsidConverter.byteArrayToString(rawBytes, offset+2, length_);
    }

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
        BinaryConverter.unsignedShortToByteArray(length_, rawBytes, offset);
        try
        {
            ccsidConverter.stringToByteArray(value_, rawBytes, offset + 2, length_);
        }
        catch(Exception e)
        {
            JDError.throwSQLException(JDError.EXC_INTERNAL, e);              // @C2A
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
        if(object instanceof String) {
            value_ = (String)object;
        } else if (object instanceof java.net.URL) {  /*@FCA*/
          value_ = ((java.net.URL)object).toExternalForm();
        } else {
          if (JDTrace.isTraceOn()) {
              if (object == null) { 
                  JDTrace.logInformation(this, "Unable to assign null object");
                } else { 
                    JDTrace.logInformation(this, "Unable to assign object("+object+") of class("+object.getClass().toString()+")");
                }
          }

            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        }

        length_ = value_.length();
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

    public String getCreateParameters()
    {
        return AS400JDBCDriver.getResource("MAXLENGTH",null);
    }

    public int getDisplaySize()
    {
        return maxLength_;
    }

    //@D1A JDBC 3.0
    //@d2a - method rewritten for URLs
    //@j4c - rewritten now that JDUtilites knows the JDBC level
    public String getJavaClassName()
    {
            return "java.net.URL";
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
        return "DATALINK";
    }

    public int getMaximumPrecision()
    {
        return 32717;
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
        return 396;
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
       
            return 70;  //java.sql.Types.DATALINK without requiring 1.4    //@J5A
       
    }

    public String getTypeName()
    {
        return "DATALINK";
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

    public InputStream getAsciiStream()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false;

        try
        {
            return new ByteArrayInputStream(ConvTable.getTable(819, null).stringToByteArray(value_));
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

    // @d2a entire method reworked.  Used to simply return value_.
    // @j4c - rewritten now that JDUtilites knows the JDBC level
    public Object getObject()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false;
        // if JDBC 3.0 or later return a URL instead of a string.
        // If we are not able to turn the string into a URL then return
        // the string (that is why there is no "else".  That shouldn't
        // happen because the database makes sure the cell contains
        // a valid URL for this data type.
            try
            {
                return new java.net.URL(value_);
            }
            catch(MalformedURLException e)
            {
                JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, e);
                return null;
            }
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

    //@pda jdbc40
    public String getNString() throws SQLException
    {
        return value_;
    }

    //@pda jdbc40
    /* ifdef JDBC40
    public RowId getRowId() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
    endif */
    //@pda jdbc40
    /* ifdef JDBC40
	public SQLXML getSQLXML() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
    endif */
    // @array
    
 
    public void saveValue() throws SQLException {
  
      
      savedValue_ = value_; 
   }

   
    
  
    
}

