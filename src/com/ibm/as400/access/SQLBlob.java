///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLBlob.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;



class SQLBlob
implements SQLData
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private static final byte[]     default_        = new byte[0];      // @A2A

    private int                     length_;
    private int                     maxLength_;
    private SQLConversionSettings   settings_;
    private int                     truncated_;
    private byte[]                  value_;



    SQLBlob (int maxLength, SQLConversionSettings settings)
    {
        settings_       = settings;
        length_         = 0;
        maxLength_      = maxLength;
        truncated_      = 0;
        value_          = default_; // @A2C
    }



    public Object clone ()
    {
        return new SQLBlob (maxLength_, settings_);
    }



    static private String getCopyright ()
    {
        return Copyright.copyright;
    }



//---------------------------------------------------------//
//                                                         //
// CONVERSION TO AND FROM RAW BYTES                        //
//                                                         //
//---------------------------------------------------------//



    public void convertFromRawBytes (byte[] rawBytes, int offset, ConverterImplRemote ccsidConverter)
        throws SQLException
    {
        length_ = BinaryConverter.byteArrayToInt (rawBytes, offset);
        AS400ByteArray typeConverter = new AS400ByteArray (length_);
        value_ = (byte[]) typeConverter.toObject (rawBytes, offset + 4);
    }



    public void convertToRawBytes (byte[] rawBytes, int offset, ConverterImplRemote ccsidConverter)
        throws SQLException
    {
        AS400ByteArray typeConverter = new AS400ByteArray (length_);
        BinaryConverter.intToByteArray (length_, rawBytes, offset);
        typeConverter.toBytes (value_, rawBytes, offset + 4);
    }



//---------------------------------------------------------//
//                                                         //
// SET METHODS                                             //
//                                                         //
//---------------------------------------------------------//



    public void set (Object object, Calendar calendar, int scale)
        throws SQLException
    {
        byte[] value = null;                                                        // @A1A

        if (object instanceof byte[])
            value = (byte[]) object;                                                // @A1C

        else {                                                                      // @A1C
            try {                                                                   // @A1C
                if (object instanceof Blob) {                                       // @A1C
                    Blob blob = (Blob) object;                                      // @A1C
                    value = blob.getBytes (0, (int) blob.length ());                // @A1C
                }                                                                   // @A1C
            }                                                                       // @A1C
            catch (NoClassDefFoundError e) {                                        // @A1C
                // Ignore.  It just means we are running under JDK 1.1.             // @A1C
            }                                                                       // @A1C
        }

        if (value == null)                                                          // @A1C
            JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        value_ = value;                                                             // @A1A

        // Truncate if necessary.
        int valueLength = value_.length;
        if (valueLength > maxLength_) {
            byte[] newValue = new byte[maxLength_];
            System.arraycopy (value_, 0, newValue, 0, maxLength_);
            value_ = newValue;
            truncated_ = valueLength - maxLength_;
        }
        else
            truncated_ = 0;

        length_ = value_.length;
    }



//---------------------------------------------------------//
//                                                         //
// DESCRIPTION OF SQL TYPE                                 //
//                                                         //
//---------------------------------------------------------//



    public String getCreateParameters ()
    {
        return AS400JDBCDriver.getResource ("MAXLENGTH"); 
    }



    public int getDisplaySize ()
    {
        return maxLength_;
    }



    public String getLiteralPrefix ()
    {
        return null;
    }



    public String getLiteralSuffix ()
    {
        return null;
    }



    public String getLocalName ()
    {
        return "BLOB"; 
    }



    public int getMaximumPrecision ()
    {
        return 15728640;
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
        return 404;
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
		return java.sql.Types.BLOB;
	}



	public String getTypeName ()
	{
		return "BLOB"; 
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
        return value_.length;
    }



    public int getTruncated ()
    {
        return truncated_;
    }



	public InputStream toAsciiStream ()
	    throws SQLException
	{
	    return new ByteArrayInputStream (value_);
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
	    return new ByteArrayInputStream (value_);
	}



	public Blob toBlob ()
	    throws SQLException
	{
	    return new AS400JDBCBlob (value_);
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
  		return -1;
	}



	public byte[] toBytes ()
	{
        return value_;
	}



	public Reader toCharacterStream ()
	    throws SQLException
	{
	    return new StringReader (new String (value_));
	}



	public Clob toClob ()
	    throws SQLException
	{
        return new AS400JDBCClob (new String (value_));
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
  		return -1;
	}



	public float toFloat ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return -1;
	}



	public int toInt ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return -1;
	}



	public long toLong ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return -1;
	}



	public Object toObject ()
	{
	    return new AS400JDBCBlob (value_);
	}



	public short toShort ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return -1;
	}



	public String toString ()
	{
	    return new String (value_);
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
	    return new ByteArrayInputStream (value_);
	}


}

