///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
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
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
/* ifdef JDBC40 */
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
/* endif */ 
import java.sql.SQLException;
/* ifdef JDBC40 */
import java.sql.SQLXML;
/* endif */ 
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
The SQLData interface represents native SQL data.  A specific
implementation of this interface will implement a specific
type of SQL data.

<p>This base class contains conversion methods usable by the sub classes. 
**/
public abstract class SQLDataBase implements SQLData
{
    
   protected int                     truncated_;
   protected boolean                 outOfBounds_; 
   protected SQLConversionSettings   settings_;

   public SQLDataBase(SQLConversionSettings settings) {
     this.settings_= settings; 
     truncated_ = 0; outOfBounds_ = false; 

   }
    /**
    Returns a clone of the SQLData object.  Use this sparingly
    so that we minimize the number of copies.
    @return     The clone.
    **/
    public abstract Object clone();

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    /**
    Loads the contents of the data from raw bytes, as returned
    in a reply from the system.
    @param  rawBytes    raw bytes from the system.
    @param  offset      offset.
    @param  converter   the converter.
    @exception  SQLException    If the raw bytes are not in
                                the expected format.
    **/
    public abstract void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable converter)
    throws SQLException;

    /**
    Converts the contents of the data in raw bytes, as needed
    in a request to the system.
    @param  rawBytes         the raw bytes for the system.
    @param  offset           the offset into the byte array.
    @param  ccsidConverter   the converter.
    **/
    public abstract void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
    throws SQLException;

    //---------------------------------------------------------//
    //                                                         //
    // SET METHODS                                             //
    //                                                         //
    // The set methods initialize the data in a uniform way    //
    // across all types.  If a specific initialization is      //
    // needed based on a Java type, then add other flavors     //
    // of set() methods.                                       //
    //                                                         //
    //---------------------------------------------------------//

    /**
    Sets the contents of the data based on a Java object.
    This performs all conversions described in Table 6
    of the JDBC specification.
    @param  object      a Java object.
    @param  calendar    The calendar.
    @param  scale       The scale.
    @exception  SQLException    If the Java object is not an
                                appropriate type.
    **/
    public abstract void set(Object object, Calendar calendar, int scale)
    throws SQLException;

    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    // These methods describe information about the actual     //
    // type of data.                                           //
    //                                                         //
    /*---------------------------------------------------------*/

    /**
    Returns the SQL type constant for the implementing class.
    @return     the SQL type constant.
    **/
    public abstract int getSQLType();

    /**
    Returns the parameters used in creating the
    type.
    @return     the parameters, separated by commas,
                or null if none.
    **/
    public abstract String getCreateParameters();

    /**
    Returns the display size.  This is defined in Appendix
    D of the ODBC 2.0 Programmer's Reference.
    @return                 the display size (in characters).
    @exception  SQLException    If the index is invalid
                                or an error occurs.
    **/
    public abstract int getDisplaySize();

    //@F1A JDBC 3.0
    /**
    Returns the Java class name for ParameterMetaData.getParameterClassName().
    @return                 the Java class name.
    **/
    public abstract String getJavaClassName();

    /**
    Returns the prefix used to quote a literal.
    @return     the prefix, or null if none.
    **/
    public abstract String getLiteralPrefix();

    /**
    Returns the suffix used to quote a literal.
    @return     the suffix, or null if none.
    **/
    public abstract String getLiteralSuffix();

    /**
    Returns the localized version of the name of the
    data type.
    @return     the name, or null.
    **/
    public abstract String getLocalName();

    /**
    Returns the maximum precision of the type. This is
    defined in Appendix D of the ODBC 2.0 Programmer's
    Reference.
    @return     the maximum precision.
    **/
    public abstract int getMaximumPrecision();

    /**
    Returns the maximum scale of the type.  This is
    defined in Appendix D of the ODBC 2.0 Programmer's
    Reference.
    @return     the maximum scale.
    **/
    public abstract int getMaximumScale();

    /**
    Returns the minimum scale of the type.  This is
    defined in Appendix D of the ODBC 2.0 Programmer's
    Reference.
    @return     the minimum scale.
    **/
    public abstract int getMinimumScale();

    /**
    Returns the native IBM i identifier for the type.
    @return     the native type.
    **/
    public abstract int getNativeType();

    /**
    Returns the precision of the type. This is
    defined in Appendix D of the ODBC 2.0 Programmer's
    Reference.
    @return     the precision.
    **/
    public abstract int getPrecision();

    /**
    Returns the radix for the type.
    @return     the radix.
    **/
    public abstract int getRadix();

    /**
    Returns the scale of the type. This is
    defined in Appendix D of the ODBC 2.0 Programmer's
    Reference.
    @return     the scale.
    **/
    public abstract int getScale();

    /**
    Returns the type constant associated with the type.
    @return     SQL type code defined in java.sql.Types.
    **/
    public abstract int getType();

    /**
    Returns the name of the data type.
    @return     the name.
    **/
    public abstract String getTypeName();

    /**
    Indicates whether the type is signed.
    @return     true or false
    **/
    public abstract boolean isSigned();

    /**
    Indicates whether the type is text.  This also
    indicates that the associated data needs to be
    converted.
    @return     true or false
    **/
    public abstract boolean isText();

    /**
    Returns the actual size of this piece of data in bytes.
    @return the actual size of this piece of data in bytes.
    **/
    public abstract int getActualSize();

    /**
    Returns the number of bytes truncated by the last conversion
    of this piece of data.
    @return the number of bytes truncated by the last conversion
    **/
    public abstract int getTruncated();
    
    /**
     * Returns true if the last conversion of this piece of
     * data was out of bounds of the range of the requested
     * datatype.  This will only happen when requesting
     * conversion to a numeric type.  
     */
    public abstract boolean getOutOfBounds(); 

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSIONS TO JAVA TYPES                               //
    //                                                         //
    // These methods convert the data to a specific Java       //
    // type.  These conversions should be provided per         //
    // section 7, table 1 ("Use of ResultSet.getXxx methods    //
    // to retrieve common SQL data types") of the JDBC 1.10    //
    // specification.  If a conversion is not required or is   //
    // not possible given the data, then the method should     //
    // throw an exception.                                     //
    //                                                         //
    /*---------------------------------------------------------*/

    /**
    Converts the data to a stream of ASCII characters.
    @return     the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
   
    public InputStream getAsciiStream()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
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

    /**
    Converts the data to a Java BigDecimal object.
    @param      scale   scale, or -1 to use full scale.
    @return             the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
    
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
                    truncated_ = 0; outOfBounds_ = false; 
                    return bigDecimal.setScale(scale);
                }
                else
                {
                    truncated_ = bigDecimal.scale() - scale; outOfBounds_ = false; 
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


    /**
    Converts the data to a stream of uninterpreted bytes.
    @return     the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
    public abstract InputStream getBinaryStream()
    throws SQLException;

    /**
    Converts the data to a java.sql.Blob object.
    @return             the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
    public abstract Blob getBlob()
    throws SQLException;

    /**
    Converts the data to a Java boolean.
    @return     the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
   
    public boolean getBoolean()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 

        // If value equals "true", "false", "1", or "0", then return the
        // corresponding boolean, otherwise an empty string is
        // false, a non-empty string is true.
        String trimmedValue = getString().trim();        
        return((trimmedValue.length() > 0) 
               && (! trimmedValue.equalsIgnoreCase("false"))
               && (! trimmedValue.equals("0")));
    }

    /**
    Converts the data to a Java byte.
    @return     the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/

    public byte getByte()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 

        try
        {
            //return(new Double(value_.trim())).byteValue();  //@trunc
            Double doubleValue  = new Double (getString().trim ());              //@trunc
            double d = doubleValue.doubleValue();                           //@trunc
            if(d > Byte.MAX_VALUE || d < Byte.MIN_VALUE) {                  //@trunc
                truncated_ = 1;                                             //@trunc
                outOfBounds_ = true; 
            }
            
            return doubleValue.byteValue ();                                //@trunc
        }
        catch(NumberFormatException e)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, e);
            return -1;
        }
    }


    /**
    Converts the data to a Java byte array containing
    uninterpreted bytes.
    @return     the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
    public abstract byte[] getBytes()
    throws SQLException;

    /**
    Converts the data to a java.io.Reader object.
    @return             the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
 
    public Reader getCharacterStream()
    throws SQLException
    {
        return new java.io.StringReader(getString());
    }

    /**
    Converts the data to a java.sql.Clob object.
    @return             the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
    public Clob getClob()
    throws SQLException
    {
        String string = getString(); 
      return new AS400JDBCClob(string, string.length());
    }
  
    /**
    Converts the data to a java.sql.Date object.
    @param  calendar    The calendar.
    @return             the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
    public Date getDate(Calendar calendar)
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        if(calendar == null) //@dat1
        {
            //getter methods do not enforce strict conversion
            calendar = AS400Calendar.getGregorianInstance(); //@dat1
        }
        else {
          calendar = AS400Calendar.getConversionCalendar(calendar); 
        }
        return SQLDate.stringToDate(getString(), settings_, calendar);
    }


    /**
    Converts the data to a Java double.
    @return     the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
    public double getDouble()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 

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


    /**
    Converts the data to a Java float.
    @return     the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
    public float getFloat()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 

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

    /**
    Converts the data to a Java int.
    @return     the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
    public int getInt()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 

        try
        {
            //return(new Double(getString().trim())).intValue();   //@trunc
            Double doubleValue  = new Double (getString().trim ());     //@trunc
            double d = doubleValue.doubleValue();                  //@trunc 

            if( d > Integer.MAX_VALUE || d < Integer.MIN_VALUE) {   //@trunc    
                truncated_ = 1;                                    //@trunc
                outOfBounds_ = true; 
            }
                 
            return doubleValue.intValue ();                        //@trunc
        }
        catch(NumberFormatException e)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, e);
            return -1;
        }
    }

    /**
    Converts the data to a Java long.
    @return     the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
    public long getLong()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 

        try
        {
            //return(new Double(getString().trim())).longValue();  //@trunc
            Double doubleValue  = new Double (getString().trim ()); //@trunc
            double d = doubleValue.doubleValue();              //@trunc

            if( d > Long.MAX_VALUE || d < Long.MIN_VALUE) {     //@trunc
                truncated_ = 1;                                //@trunc
                outOfBounds_ = true; 
            }

            return doubleValue.longValue ();                   //@trunc
        }
        catch(NumberFormatException e)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, e);
            return -1;
        }
    }





    /**
    Converts the data to a Java object.  The actual type
    of the Java object is dictated per section 8,
    table 2 ("Standard mapping from SQL types to Java types")
    of the JDBC 1.10 specification
    @return             the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
    public abstract Object getObject()
    throws SQLException;

    /**
    Converts the data to a Java short.
    @return     the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
    
    public short getShort()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 

        try
        {
            //return(new Double(getString().trim())).shortValue();           //@trunc
            Double doubleValue  = new Double (getString().trim ());               //@trunc
            double d = doubleValue.doubleValue();                            //@trunc

            if( d > Short.MAX_VALUE || d < Short.MIN_VALUE)  {                //@trunc      
                truncated_ = 1;                                              //@trunc
                outOfBounds_ = true; 
            }

            return doubleValue.shortValue ();                                //@trunc
        }
        catch(NumberFormatException e)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, e);
            return -1;
        }
    }


    /**
    Converts the data to a Java String object.  This
    conversion must be provided by the implementation.
    @return             the result of the conversion.
    **/
    public abstract String getString()
    throws SQLException;

    /**
    Converts the data to a java.sql.Time object.
    @param  calendar    The calendar.
    @return             the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
     public Time getTime(Calendar calendar)
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        if(calendar == null) //@dat1
        {
            //getter methods do not enforce strict conversion
            calendar = AS400Calendar.getGregorianInstance(); //@dat1
        }
        else {
          calendar = AS400Calendar.getConversionCalendar(calendar); 
        }
       return SQLTime.stringToTime(getString(), settings_, calendar);
    }

    
    /**
    Converts the data to a java.sql.Timestamp object.
    @param  calendar    The calendar.
    @return             the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
     public Timestamp getTimestamp(Calendar calendar)
     throws SQLException
     {
         truncated_ = 0; outOfBounds_ = false; 
         if(calendar == null) //@dat1
         {
             //getter methods do not enforce strict conversion
             calendar = AS400Calendar.getGregorianInstance(); //@dat1
         }
         else {
           calendar = AS400Calendar.getConversionCalendar(calendar); 
         }
         return SQLTimestamp.stringToTimestamp(getString(), calendar);
     }

    /**
    Converts the data to a stream of Unicdoe characters.
    @return     the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
     public InputStream getUnicodeStream()
     throws SQLException
     {
         truncated_ = 0; outOfBounds_ = false; 

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

    
    //@PDA jdbc40
    /**
    Converts the data to a java.io.Reader object.
    @return     the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
     public Reader getNCharacterStream() throws SQLException
     {
         truncated_ = 0; outOfBounds_ = false; 

         // This is written in terms of getNString(), since it will
         // handle truncating to the max field size if needed.
         return new StringReader(getNString());
     }
    
    //@PDA jdbc40
    /**
    Converts the data to a java.sql.NClob object
    @return     the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
/* ifdef JDBC40 */
    public NClob getNClob() throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 

        // This is written in terms of getNString(), since it will
        // handle truncating to the max field size if needed.
        String string = getNString();
        return new AS400JDBCNClob(string, string.length());
    }
/* endif */ 
    //@PDA jdbc40
    /**
    Converts the data to String object.
    @return     the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
    public abstract String getNString()
    throws SQLException;
    
    //@PDA jdbc40
    /**
    Converts the data to a java.sql.SQLXML object.
    @return     the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
/* ifdef JDBC40 */
    public abstract SQLXML getSQLXML()
    throws SQLException;
/* endif */ 
    
    //@PDA jdbc40
    /**
    Converts the data to a java.sql.RowId object.
    @return     the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
/* ifdef JDBC40 */
    
    public abstract RowId getRowId()
    throws SQLException;
/* endif */ 
    
    //@array
    /**
    Converts (returns) the data to a java.sql.Array object.
    @return     the result of the conversion.
    @exception  SQLException    If the conversion is not
                                required or not possible.
    **/
    
    public Array getArray() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

   

 

}
