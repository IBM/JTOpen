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



class SQLDatalink
implements SQLData
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    // Private data.
    private static final String     default_ = ""; // @A1A

    private int                     length_;
    private int                     maxLength_;
    private SQLConversionSettings   settings_;
    private String                  value_;




    SQLDatalink (int maxLength, SQLConversionSettings settings)
    {
        length_         = 0;
        maxLength_      = maxLength;
        settings_       = settings;
        value_          = default_; // @A1C
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
        try {
            ccsidConverter.stringToByteArray (value_, rawBytes, offset + 2, length_);
        }
        catch (Exception e) {
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
        if (object instanceof String)
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
    public String getJavaClassName()
    {

        String returnValue = "java.lang.Datalink";	      
        
        // Return a URL only when running JDBC 3.0 or later.
        try 
        { 
           Class.forName("java.sql.Savepoint"); 
           returnValue = "java.net.URL";
   
        }                                         
        catch (Exception e) { }
	   
        return returnValue;
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
        return 32739;
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
	public Object toObject ()
	{
	   // If we are running JDBC 2.0 or earlier, or if we cannot convert
	   // the string into a URL, then return the contents of the cell as a String.
	   Object returnValue = value_;       
                                                                          
      // Return a URL only when running JDBC 3.0 or later.
      try 
      { 
         Class.forName("java.sql.Savepoint"); 
         
         // try turning the String into a URL.  If that fails return the string.                                       
         try { returnValue = new java.net.URL(value_); } catch (Exception e) {}
      }                                         
      catch (Exception e) { }
	   
	   return returnValue;                 
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

