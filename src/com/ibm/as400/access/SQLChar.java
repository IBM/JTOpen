///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLChar.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
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
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;



class SQLChar
implements SQLData
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";




    // Private data.
    private static final String     default_ = ""; // @C4A

    private SQLConversionSettings   settings_;
    private boolean                 graphic_;
    private int                     maxLength_;
    private int                     truncated_;
    private String                  value_;
    private String                  originalValue_; // @F66a  (in case we need to repad)



    SQLChar (int maxLength,
             boolean graphic,
             SQLConversionSettings settings)
    {
        settings_       = settings;
        graphic_        = graphic;
        maxLength_      = maxLength;
        truncated_      = 0;
        value_          = default_; // @C4C
        originalValue_  = default_; // @F66a
    }



    public Object clone ()
    {
        return new SQLChar (maxLength_, graphic_, settings_);
    }



//---------------------------------------------------------//
//                                                         //
// CONVERSION TO AND FROM RAW BYTES                        //
//                                                         //
//---------------------------------------------------------//

    public void convertFromRawBytes (byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
        throws SQLException
    {
        // @C3D int length = maxLength_;

        // @A0A
        // Added code to do hand conversion if ccsidConverter is null or not Unicode.     // @E1C
        if ((ccsidConverter != null) && (ccsidConverter.getCcsid() != 13488))             // @A0A @C3C @E1C
        {
            int length = /* @C5D graphic_ ? maxLength_ * 2 :*/ maxLength_;                // @C3A
	    
	    int bidiStringType = settings_.getBidiStringType();  //@E3A

	    // if bidiStringType is not set by user, use ccsid to get value
	    if (bidiStringType == -1)			         //@E3A
          //@P0D bidiStringType = AS400BidiTransform.getStringType((char)ccsidConverter.getCcsid()); //@E3A
             bidiStringType = ccsidConverter.bidiStringType_; //@P0A

          value_ = ccsidConverter.byteArrayToString (rawBytes, offset, length, bidiStringType); //@E3C
        }                                                                       // @C3A
        else {                                                                  // @A0A
            // This is a 13488 Unicode ccsid. Do the hand conversion.
            // Note 'length' here is the number of bytes in the string.
            int length = maxLength_;                                            // @C3A
            char[] result = new char[length/2];                                 // @A0A
            int resultIndex = 0;                                                // @A0A

            for (int i=offset; i<offset + length; i+=2) {                       // @A0A
                // Get the two bytes that make up this char
                int byte1 = rawBytes[i] &0xFF;                                  // @A0A
                int byte2 = rawBytes[i+1] &0xFF;                                // @A0A

                // Construct a char out of the two bytes
                result[resultIndex++] = (char)((byte1 << 8) + byte2);           // @A0A
            }                                                                   // @A0A

            // Assign the result to value_
            value_ = new String(result);                                        // @A0A
        }                                                                       // @A0A

    }

    public void convertToRawBytes (byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
        throws SQLException
    {
      // @F66 We originally padded with a single byte space.  We now have the
      //   ccsid so we can figure out if that was right or not.  If we should
      //   have use the double byte space, re-pad.
      if (graphic_ && (ccsidConverter.getCcsid() != 13488))          // @F66a
      {                                                              // @F66a
         int valueLength = originalValue_.length ();                 // @F66a
         int exactLength = getDisplaySize ();                        // @F66a
         if (valueLength < exactLength)                              // @F66a
         {                                                           // @F66a
            StringBuffer buffer = new StringBuffer (originalValue_); // @F66a
            char c = '\u3000';                                       // @F66a
            for (int i = valueLength; i < exactLength; ++i)          // @F66a
                buffer.append (c);                                   // @F66a
            value_ = buffer.toString ();                             // @F66a
         }                                                           // @F66a
      }                                                              // @F66a
         
      
      int bidiStringType = settings_.getBidiStringType();  //@E3A

      // if bidiStringType is not set by user, use ccsid to get value
      if (bidiStringType == -1)			     //@E3A
	    //@P0D bidiStringType = AS400BidiTransform.getStringType((char)ccsidConverter.getCcsid()); //@E3A
      bidiStringType = ccsidConverter.bidiStringType_; //@P0A
            
        try {
	     ccsidConverter.stringToByteArray (value_, rawBytes, offset, maxLength_, bidiStringType);	   //@E3C
      }
        catch (CharConversionException e) {
            maxLength_ = ccsidConverter.stringToByteArray(value_, bidiStringType).length; // @BAA @E3C
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
        String value = null;                                                        // @C2A

        if (object instanceof String)
            value = (String) object;                                                // @C2C

        else if (object instanceof Number)
            value = object.toString();                                              // @C2C

        else if (object instanceof Boolean)
            value = object.toString();                                              // @C2C

        else if (object instanceof Time)
            value = SQLTime.timeToString ((Time) object, settings_, calendar);      // @C2C

        else if (object instanceof Timestamp)
            value = SQLTimestamp.timestampToString ((Timestamp) object, calendar);  // @C2C

        else if (object instanceof java.util.Date)                                  // @F5M @F5C
            value = SQLDate.dateToString ((java.util.Date) object, settings_, calendar); // @C2C @F5C

        else {                                                                      // @C2C
            try {                                                                   // @C2C
                if (object instanceof Clob) {                                       // @C2C
                    Clob clob = (Clob) object;                                      // @C2C
                    value = clob.getSubString (1, (int) clob.length ());            // @C2C   @D1
                }                                                                   // @C2C
            }                                                                       // @C2C
            catch (NoClassDefFoundError e) {                                        // @C2C
                // Ignore.  It just means we are running under JDK 1.1.             // @C2C
            }                                                                       // @C2C
        }                                                                           // @C2C

        if (value == null)                                                          // @C2C
            JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        value_ = value;                                                             // @C2A
        originalValue_ = value;                               // @F66a
        

        // Set to the exact length.
        int valueLength = value_.length ();
        int exactLength = getDisplaySize ();                        // @C1A
        if (valueLength < exactLength)                              // @C1C
        {
            StringBuffer buffer = new StringBuffer (value_);
            char c = '\u0020';                                //@F66c - Pad with single byte space for now
            for (int i = valueLength; i < exactLength; ++i)         // @C1C
                buffer.append (c); //@F6C
            value_ = buffer.toString ();
            truncated_ = 0;
        }
        else if (valueLength > exactLength) {                       // @C1C @F6C
            value_ = value_.substring (0, exactLength);             // @C1C
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


    //@F1A JDBC 3.0
    public String getJavaClassName()
    {
        return "java.lang.String";
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
        return (graphic_ ? 468 : 452);
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
		return java.sql.Types.CHAR;
	}



	public String getTypeName ()
	{
		return "CHAR";
	}



    // @E1D public boolean isGraphic ()
    // @E1D {
    // @E1D     return graphic_;
    // @E1D }



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
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);            // @E2C
    		return null;
        }
	}



	public BigDecimal toBigDecimal (int scale)
	    throws SQLException
	{
	    try {
    	    BigDecimal bigDecimal = new BigDecimal (SQLDataFactory.convertScientificNotation(value_.trim ())); // @F3C
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

	    // If value equals "true", "false", "1", or "0", then return the
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
	        // @B1D truncated_ = value_.length() - maxFieldSize;
	        return value_.substring (0, maxFieldSize);
	    }
	    else {
    	    // @B1D truncated_ = 0;
	        return value_;
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
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);            // @E2C
    		return null;
        }
	}



        // @A1A
        // Added method trim() to trim the string.
        public void trim()                               // @A1A
	{                                                // @A1A
            value_ = value_.trim();                      // @A1A
	}                                                // @A1A

}

