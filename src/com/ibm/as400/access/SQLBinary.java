///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLBinary.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
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



class SQLBinary
implements SQLData
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    private SQLConversionSettings   settings_;
    private int                     maxLength_;
    private int                     truncated_;
    private AS400ByteArray          typeConverter_;
    private byte[]                  value_;



    SQLBinary (int maxLength, SQLConversionSettings settings)
    {
        settings_       = settings;
        maxLength_      = maxLength;
        truncated_      = 0;
        typeConverter_  = new AS400ByteArray (maxLength);
        value_          = new byte[maxLength];
    }



    public Object clone ()
    {
        return new SQLBinary (maxLength_, settings_);
    }




//---------------------------------------------------------//
//                                                         //
// CONVERSION TO AND FROM RAW BYTES                        //
//                                                         //
//---------------------------------------------------------//



    public void convertFromRawBytes (byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
        throws SQLException
    {
        value_ = (byte[]) typeConverter_.toObject (rawBytes, offset);
    }



    public void convertToRawBytes (byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
        throws SQLException
    {
        typeConverter_.toBytes (value_, rawBytes, offset);
    }



//---------------------------------------------------------//
//                                                         //
// SET METHODS                                             //
//                                                         //
//---------------------------------------------------------//



    public void set (Object object, Calendar calendar, int scale)
        throws SQLException
    {
        byte[] value = null;                                                        // @C1A

        if (object instanceof String)
            //@F1D value = ((String) object).getBytes();                                   // @C1C
            value = stringToBytes((String)object); //@F1A

        else if (object instanceof byte[])
            value = (byte[]) object;                                                // @C1C

        else {                                                                      // @C1C
            try {                                                                   // @C1C
                if (object instanceof Blob) {                                       // @C1C
                    Blob blob = (Blob) object;                                      // @C1C
                    value = blob.getBytes (1, (int) blob.length ());                // @C1C @E2C Blobs are 1 based.
                }                                                                   // @C1C
                else if (object instanceof Clob) {                                  // @C1C
                    Clob clob = (Clob) object;                                      // @C1C
                    //@F1D value = clob.getSubString (1, (int) clob.length ()).getBytes(); // @C1C  @D1
                    value = stringToBytes(clob.getSubString(1, (int)clob.length())); //@F1A
                }                                                                   // @C1C
            }                                                                       // @C1C
            catch (NoClassDefFoundError e) {                                        // @C1C
                // Ignore.  It just means we are running under JDK 1.1.             // @C1C
            }                                                                       // @C1C
        }

        if (value == null)                                                          // @C1C
            JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        value_ = value;                                                             // @C1A

        // Set to the exact length.
        int valueLength = value_.length;
        if (valueLength < maxLength_) {
            byte[] newValue = new byte[maxLength_];
            System.arraycopy (value_, 0, newValue, 0, valueLength);
            value_ = newValue;
            truncated_ = 0;
        }
        else if (valueLength > maxLength_) {
            byte[] newValue = new byte[maxLength_];
            System.arraycopy (value_, 0, newValue, 0, maxLength_);
            value_ = newValue;
            truncated_ = valueLength - maxLength_;
        }
        else
            truncated_ = 0;
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
        return "X\'";
    }



    public String getLiteralSuffix ()
    {
        return "\'";
    }



    public String getLocalName ()
    {
        // Use "CHAR" not "BINARY".  See ODBC SQLGetTypeInfo().
        return "CHAR";
    }



    public int getMaximumPrecision ()
    {
        return 32765;
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
        return 452;
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
		return java.sql.Types.BINARY;
	}



	public String getTypeName ()
	{
		return "CHAR() FOR BIT DATA";
	}



    // @E1D public boolean isGraphic ()
    // @E1D {
    // @E1D    return false;
    // @E1D }



    public boolean isSigned ()
    {
        return false;
    }



    public boolean isText ()
    {
        return true;
    }



    public int getActualSize ()
    {
        return value_.length;
    }


    //@F2A JDBC 3.0
    public String getJavaClassName()
    {
        return "[B";
    }


    public int getTruncated ()
    {
        return truncated_;
    }



//---------------------------------------------------------//
//                                                         //
// CONVERSIONS TO JAVA TYPES                               //
//                                                         //
//---------------------------------------------------------//



	public InputStream toAsciiStream ()
	    throws SQLException
	{
	    // This is written in terms of toBytes(), since it will
	    // handle truncating to the max field size if needed.
	    return new ByteArrayInputStream (toBytes ());
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
	    // This is written in terms of toBytes(), since it will
	    // handle truncating to the max field size if needed.
	    return new ByteArrayInputStream (toBytes ());
	}



	public Blob toBlob ()
	    throws SQLException
	{
	    // This is written in terms of toBytes(), since it will
	    // handle truncating to the max field size if needed.
	    return new AS400JDBCBlob (toBytes (), maxLength_);
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
	    // Truncate to the max field size if needed.
        // Do not signal a DataTruncation per the spec. @B1A
	    int maxFieldSize = settings_.getMaxFieldSize ();
	    if ((value_.length > maxFieldSize) && (maxFieldSize > 0)) {
	        // @B1D truncated_ = value_.length - maxFieldSize;
	        byte[] truncatedValue = new byte[maxFieldSize];
	        System.arraycopy (value_, 0, truncatedValue, 0, maxFieldSize);
	        return truncatedValue;
	    }
	    else {
	        // @B1D truncated_ = 0;
	        return value_;
	    }
	}



	public Reader toCharacterStream ()
	    throws SQLException
	{
	    // This is written in terms of toBytes(), since it will
	    // handle truncating to the max field size if needed.
	    //@F1D return new StringReader (new String (toBytes ()));
      return new StringReader(bytesToString(toBytes())); //@F1A
	}



	public Clob toClob ()
	    throws SQLException
	{
  	    // This is written in terms of toString(), since it will
        // handle truncating to the max field size if needed.
        //@F1D return new AS400JDBCClob (new String (toBytes ()));
        return new AS400JDBCClob(bytesToString(toBytes()), maxLength_); //@F1A
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
	    // This is written in terms of toBytes(), since it will
	    // handle truncating to the max field size if needed.
   	    return toBytes ();
	}



	public short toShort ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
		return -1;
	}



	public String toString ()
	{
	    // This is written in terms of toBytes(), since it will
	    // handle truncating to the max field size if needed.
	    //@F1D return new String (toBytes ());
      return bytesToString(toBytes()); //@F1A
	}


  //@F1A
  // Constant used in bytesToString()
  private static final char[] c_ = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

  static final char hiNibbleToChar(byte b)
  {
    return c_[(b >>> 4) & 0x0F];
  }

  static final char loNibbleToChar(byte b)
  {
    return c_[b & 0x0F];
  }

  static final String bytesToString(final byte[] b)
  {
    return bytesToString(b, 0, b.length);
  }

  static final String bytesToString(final byte[] b, int offset, int length)
  {
    char[] c = new char[length*2];
    int num = bytesToString(b, offset, length, c, 0);
    return new String(c, 0, num);
  }

  //@F1A
  // Helper method to convert a byte array into its hex string representation.
  // This is faster than calling Integer.toHexString(...)
  static final int bytesToString(final byte[] b, int offset, int length, final char[] c, int coffset)
  {
    for (int i=0; i<length; ++i)
    {
      final int j = i*2;
      final byte hi = (byte)((b[i+offset]>>>4) & 0x0F);
      final byte lo = (byte)((b[i+offset] & 0x0F));
      c[j+coffset] = c_[hi];
      c[j+coffset+1] = c_[lo];
    }
    return length*2;
  }

  //@F1A
  // Constant used in stringToBytes()
  // Note that 0x11 is "undefined".
  private static final byte[] b_ = 
  {
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,
    0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11
  };

  static final byte charsToByte(char hi, char lo)
  {
    int c1 = 0x00FFFF & hi;
    int c2 = 0x00FFFF & lo;
    if (c1 > 255 || c2 > 255) return 0;
    byte b1 = b_[c1];
    byte b2 = b_[c2];
    if (b1 == 0x11 || b2 == 0x11) return 0;
    return (byte)(((byte)(b1 << 4)) + b2);
  }


  static final byte[] stringToBytes(String s)
  {
    char[] c = s.toCharArray();
    return stringToBytes(c, 0, c.length);
  }

  static final byte[] stringToBytes(char[] hex, int offset, int length)
  {
    if (hex.length == 0) return new byte[0];
    byte[] buf = new byte[length/2];
    int num = stringToBytes(hex, offset, length, buf, 0);
    if (num < buf.length)
    {
      byte[] temp = buf;
      buf = new byte[num];
      System.arraycopy(temp, 0, buf, 0, num);
    }
    return buf;
  }

  //@F1A
  // Helper method to convert a String in hex into its corresponding byte array.
  static final int stringToBytes(char[] hex, int offset, int length, final byte[] b, int boff)
  {
    if (hex.length == 0) return 0;
      if (hex[offset] == '0' && (hex.length > offset+1 && (hex[offset+1] == 'X' || hex[offset+1] == 'x')))
      {
        offset += 2;
        length -= 2;
      }
      for (int i=0; i<b.length; ++i)
      {
        final int j = i*2;
        final int c1 = 0x00FFFF & hex[j+offset];
        final int c2 = 0x00FFFF & hex[j+offset+1];
        if (c1 > 255 || c2 > 255) // out of range
        {
          b[i+boff] = 0x00;
        }
        else
        {
          final byte b1 = b_[c1];
          final byte b2 = b_[c2];
          if (b1 == 0x11 || b2 == 0x11) // out of range
          {
            b[i+boff] = 0x00;
          }
          else
          {
            final byte hi = (byte)(b1<<4);
            b[i+boff] = (byte)(hi + b2);
          }
        }
      }
      return b.length;
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
	    // This is written in terms of toBytes(), since it will
	    // handle truncating to the max field size if needed.
	    return new ByteArrayInputStream (toBytes ());
	}


}

