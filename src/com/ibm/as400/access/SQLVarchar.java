///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: SQLVarchar.java
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
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;



class SQLVarchar
implements SQLData
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private static final String     default_        = ""; // @C3A

    private SQLConversionSettings   settings_;
    private boolean                 graphic_;
    private int                     length_;
    private boolean                 longValue_;
    private int                     maxLength_;
    private int                     truncated_;
    private String                  value_;




    // Note: maxLength is in bytes not counting 2 for LL.
    //
    SQLVarchar (int maxLength,
                boolean graphic,
                boolean longValue,
                SQLConversionSettings settings)
    {
        settings_       = settings;
        graphic_        = graphic;
        length_         = 0;
        longValue_      = longValue;
        maxLength_      = maxLength;
        truncated_      = 0;
        value_          = default_; // @C3C
    }



    SQLVarchar (int maxLength, SQLConversionSettings settings)
    {
        this (maxLength, false, false, settings);
    }



    public Object clone ()
    {
        return new SQLVarchar (maxLength_, graphic_, longValue_, settings_);
    }



    static private String getCopyright ()
    {
        return Copyright.copyright;
    }


    // @A2A
    public void trim()                                // @A2A
	{                                                 // @A2A
        value_ = value_.trim();                       // @A2A
	}                                                 // @A2A



//---------------------------------------------------------//
//                                                         //
// CONVERSION TO AND FROM RAW BYTES                        //
//                                                         //
//---------------------------------------------------------//



    public void convertFromRawBytes (byte[] rawBytes, int offset, ConverterImplRemote ccsidConverter)
        throws SQLException
    {
        length_ = BinaryConverter.byteArrayToUnsignedShort (rawBytes, offset);

        // Do hand conversion if ccsidConverter is null.                         // @A1A
        if (ccsidConverter != null)                                             // @A1A
            // If the field is VARGRAPHIC, length_ contains the number
            // of characters in the string, while the converter is expecting
            // the number of bytes. Thus, we need to multiply length_ by 2.
            if (graphic_)
                value_ = ccsidConverter.byteArrayToString (rawBytes, offset+2, length_*2);
            else
                value_ = ccsidConverter.byteArrayToString (rawBytes, offset+2, length_);
        else {                                                                  // @A1A
            // This is a 13488 Unicode ccsid. Do the hand conversion.
            // Note that the length_ here for a VARGRAPHIC field is the
            // number of characters in the string.
            char[] result = new char[length_];                                  // @A1A
            int resultIndex = 0;                                                // @A1A

            int cond = offset+2+length_*2;                                      // @A1A
            for (int i=offset+2; i<cond; i+=2) {                                // @A1A
                // Get the two bytes that make up this char
                int byte1 = rawBytes[i] &0xFF;                                  // @A1A
                int byte2 = rawBytes[i+1] &0xFF;                                // @A1A

                // Construct a char out of the two bytes
                result[resultIndex++] = (char)((byte1 << 8) + byte2);           // @A1A
            }                                                                   // @A1A

            // Assign the result to value_
            value_ = new String(result);                                        // @A1A
        }                                                                       // @A1A

    }



    public void convertToRawBytes (byte[] rawBytes, int offset, ConverterImplRemote ccsidConverter)
        throws SQLException
    {
        try {
            // The length in the first 2 bytes is actually the length in bytes, not             // @BAA
            // the length in characters.  Therefore, we have to convert before setting the      // @BAA
            // length.  The only way to get the length is to convert into a temporary           // @BAA
            // byte array.  Performance-wise, this is okay, since Converter would have          // @BAA
            // done that anyway.                                                                // @BAA
            byte[] temp = ccsidConverter.stringToByteArray(value_);                             // @BAA
            BinaryConverter.unsignedShortToByteArray (temp.length, rawBytes, offset);           // @BAA
            if (temp.length > maxLength_) {                                                     // @BAA
                maxLength_ = temp.length;                                                       // @BAA
                JDError.throwSQLException (JDError.EXC_INTERNAL);                               // @BAA
            }                                                                                   // @BAA
            System.arraycopy(temp, 0, rawBytes, offset+2, temp.length);                         // @BAA
        }
        catch (Exception e) {
          JDError.throwSQLException (JDError.EXC_INTERNAL, e);
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
        String value = null;                                                        // @C1A

        if (object instanceof String)
            value = (String) object;                                                // @C1C

        else if (object instanceof Number)
            value = object.toString();                                              // @C1C

        else if (object instanceof Boolean)
            value = object.toString();                                              // @C1C

        else if (object instanceof Date)
            value = SQLDate.dateToString ((Date) object, settings_, calendar);      // @C1C

        else if (object instanceof Time)
            value = SQLTime.timeToString ((Time) object, settings_, calendar);      // @C1C

        else if (object instanceof Timestamp)
            value = SQLTimestamp.timestampToString ((Timestamp) object, calendar);  // @C1C

        else {                                                                      // @C1C
            try {                                                                   // @C1C
                if (object instanceof Clob) {                                       // @C1C
                    Clob clob = (Clob) object;                                      // @C1C
                    value = clob.getSubString (1, (int) clob.length ());            // @C1C  @D1
                }                                                                   // @C1C
            }                                                                       // @C1C
            catch (NoClassDefFoundError e) {                                        // @C1C
                // Ignore.  It just means we are running under JDK 1.1.             // @C1C
            }                                                                       // @C1C
        }                                                                           // @C1C

        if (value == null)                                                          // @C1C
            JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        value_ = value;                                                             // @C1A

        // Truncate if necessary.
        int valueLength = value_.length ();
        if (valueLength > maxLength_) {
            value_ = value_.substring (0, maxLength_);
            truncated_ = valueLength - maxLength_;
        }
        else
            truncated_ = 0;

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
        if (graphic_)
            return (maxLength_ / 2);
        else
            return maxLength_;
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
        return "VARCHAR";
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
        if (graphic_) {
            if (longValue_)
                return 472;
            else
                return 464;
        }
        else { // non-graphic
            if (longValue_)
                return 456;
            else
                return 448;
        }
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
		return "VARCHAR";
	}



    public boolean isGraphic ()
    {
        return graphic_;
    }



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
        return truncated_;
    }



	public InputStream toAsciiStream ()
	    throws SQLException
	{
	    try {
    	    // This is written in terms of toString(), since it will
	        // handle truncating to the max field size if needed.
            return new ByteArrayInputStream (toString ().getBytes ("ISO8859_1"));
        }
        catch (UnsupportedEncodingException e) {
            JDError.throwSQLException (JDError.EXC_INTERNAL);
            return null;
        }
	}



	public BigDecimal toBigDecimal (int scale)
	    throws SQLException
	{
  	    try {
       	    BigDecimal bigDecimal = new BigDecimal (value_);
    	    if (scale >= 0) {
                if (scale >= bigDecimal.scale()) {
                    truncated_ = 0;
                    return bigDecimal.setScale (scale);
                }
                else {
                    truncated_ = bigDecimal.scale() - scale;
                    return bigDecimal.setScale (scale, BigDecimal.ROUND_HALF_UP);
                }
            }
    	    else
	            return bigDecimal;
        }
        catch (NumberFormatException e) {
            JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
            return null;
	    }
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
	    truncated_ = 0;

	    // If value equals "true" or "false", then return the
	    // corresponding boolean, otherwise an empty string is
	    // false, a non-empty string is true.
	    String trimmedValue = value_.trim ();        
	    return ((trimmedValue.length () > 0) 
    	        && (! trimmedValue.equalsIgnoreCase ("false"))
                && (! trimmedValue.equals ("0")));
	}



	public byte toByte ()
	    throws SQLException
	{
	    truncated_ = 0;

	    try {
	        return (new Double (value_.trim ())).byteValue ();
	    }
	    catch (NumberFormatException e) {
	        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
            return -1;
	    }
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
  	    // This is written in terms of toString(), since it will
        // handle truncating to the max field size if needed.
        return new StringReader (toString ());
	}



	public Clob toClob ()
	    throws SQLException
	{
  	    // This is written in terms of toString(), since it will
        // handle truncating to the max field size if needed.
        return new AS400JDBCClob (toString ());
	}



	public Date toDate (Calendar calendar)
	    throws SQLException
	{
	    truncated_ = 0;
	    return SQLDate.stringToDate (value_, settings_, calendar);
	}



	public double toDouble ()
	    throws SQLException
	{
	    truncated_ = 0;

	    try {
	        return (new Double (value_.trim ())).doubleValue ();
	    }
	    catch (NumberFormatException e) {
	        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
            return -1;
	    }
	}



	public float toFloat ()
	    throws SQLException
	{
	    truncated_ = 0;

	    try {
	        return (new Double (value_.trim ())).floatValue ();
	    }
	    catch (NumberFormatException e) {
	        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
            return -1;
	    }
	}



	public int toInt ()
	    throws SQLException
	{
	    truncated_ = 0;

	    try {
	        return (new Double (value_.trim ())).intValue ();
	    }
	    catch (NumberFormatException e) {
	        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
            return -1;
	    }
	}



	public long toLong ()
	    throws SQLException
	{
	    truncated_ = 0;

	    try {
	        return (new Double (value_.trim ())).longValue ();
	    }
	    catch (NumberFormatException e) {
	        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
            return -1;
	    }
	}



	public Object toObject ()
	{
	    // This is written in terms of toString(), since it will
	    // handle truncating to the max field size if needed.
	    return toString ();
	}



	public short toShort ()
	    throws SQLException
	{
	    truncated_ = 0;

	    try {
	        return (new Double (value_.trim ())).shortValue ();
	    }
	    catch (NumberFormatException e) {
	        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
            return -1;
	    }
	}



	public String toString ()
	{
	    // Truncate to the max field size if needed.
        // Do not signal a DataTruncation per the spec. @B1A
	    int maxFieldSize = settings_.getMaxFieldSize ();
	    if ((value_.length() > maxFieldSize) && (maxFieldSize > 0)) {
	        return value_.substring (0, maxFieldSize);    // @B1D
	    }
	    else {
	        return value_;           // @B1D
	    }
	}



	public Time toTime (Calendar calendar)
	    throws SQLException
	{
	    truncated_ = 0;
	    return SQLTime.stringToTime (value_, settings_, calendar);
	}



	public Timestamp toTimestamp (Calendar calendar)
	    throws SQLException
	{
	    truncated_ = 0;
	    return SQLTimestamp.stringToTimestamp (value_, calendar);
	}



	public InputStream toUnicodeStream ()
	    throws SQLException
	{
	    try {
    	    // This is written in terms of toString(), since it will
	        // handle truncating to the max field size if needed.
            return new ByteArrayInputStream (toString ().getBytes ("UnicodeBigUnmarked")); // @B2C
        }
        catch (UnsupportedEncodingException e) {
            JDError.throwSQLException (JDError.EXC_INTERNAL);
            return null;
        }
	}



}

