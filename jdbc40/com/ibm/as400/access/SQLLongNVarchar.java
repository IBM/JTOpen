///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLLongNVarchar.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2006-2006 International Business Machines Corporation and     
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
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

//@PDA jdbc40 new class

final class SQLLongNVarchar
implements SQLData
{
  
    // Private data.
    private SQLConversionSettings   settings_;
    private int                     length_;
    private int                     maxLength_;
    private int                     truncated_;
    private String                  value_;

    // Note: maxLength is in bytes not counting 2 for LL.
    //
    SQLLongNVarchar(int maxLength, SQLConversionSettings settings)
    {
        settings_       = settings;
        length_         = 0;
        maxLength_      = maxLength;
        truncated_      = 0;
        value_          = "";
    }

    public Object clone()
    {
        return new SQLLongNVarchar(maxLength_, settings_);  //@pdc
    }
 
    public void trim()                               
    {                                                
        value_ = value_.trim();                     
    }                                        

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
    throws SQLException
    {
        length_ = BinaryConverter.byteArrayToUnsignedShort(rawBytes, offset);

        int bidiStringType = settings_.getBidiStringType();
        // if bidiStringType is not set by user, use ccsid to get value
        if(bidiStringType == -1)
            bidiStringType = ccsidConverter.bidiStringType_;
        BidiConversionProperties bidiConversionProperties = new BidiConversionProperties(bidiStringType);  
        bidiConversionProperties.setBidiImplicitReordering(settings_.getBidiImplicitReordering());      
        bidiConversionProperties.setBidiNumericOrderingRoundTrip(settings_.getBidiNumericOrdering()); 

        value_ = ccsidConverter.byteArrayToString(rawBytes, offset+2, length_, bidiConversionProperties);  
    }

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
    throws SQLException
    {
        try
        {
            int bidiStringType = settings_.getBidiStringType();
            // if bidiStringType is not set by user, use ccsid to get value
            if(bidiStringType == -1)
                bidiStringType = ccsidConverter.bidiStringType_;
            
            BidiConversionProperties bidiConversionProperties = new BidiConversionProperties(bidiStringType); 
            bidiConversionProperties.setBidiImplicitReordering(settings_.getBidiImplicitReordering());          
            bidiConversionProperties.setBidiNumericOrderingRoundTrip(settings_.getBidiNumericOrdering());    

            // The length in the first 2 bytes is actually the length in characters.
            byte[] temp = ccsidConverter.stringToByteArray(value_, bidiConversionProperties);  
            BinaryConverter.unsignedShortToByteArray(temp.length, rawBytes, offset);
            if(temp.length > maxLength_)
            {
                maxLength_ = temp.length;
                JDError.throwSQLException(this, JDError.EXC_INTERNAL);
            }
            System.arraycopy(temp, 0, rawBytes, offset+2, temp.length);

            // The buffer we are filling with data is big enough to hold the entire field.
            // For varchar fields the actual data is often smaller than the field width.
            // That means whatever is in the buffer from the previous send is sent to the
            // system.  The data stream includes actual data length so the old bytes are not 
            // written to the database, but the junk left over may decrease the affectiveness 
            // of compression.  The following code will write hex 0s to the buffer when
            // actual length is less that field length.  Note the 0s are written only if 
            // the field length is pretty big.  The data stream code (DBBaseRequestDS)
            // does not compress anything smaller than 1K.
            if((maxLength_ > 256) && (maxLength_ - temp.length > 16))
            {
                int stopHere = offset + 2 + maxLength_;
                for(int i=offset + 2 + temp.length; i<stopHere; i++)
                    rawBytes[i] = 0x00;
            }
        }
        catch(Exception e)
        {
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

        else if(JDUtilities.JDBCLevel_ >= 20 && object instanceof Clob)
        {                                                                          
            Clob clob = (Clob)object;                                
            value = clob.getSubString(1, (int)clob.length());   
        }                                                     
        else if(object instanceof SQLXML) 
        {    
            SQLXML xml = (SQLXML)object;
            value = xml.getString();
        }     

        if(value == null)                                                         
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        value_ = value;                                                         

        // Truncate if necessary.
        int valueLength = value_.length();

        int truncLimit = maxLength_;             

        if(valueLength > truncLimit)           
        {
            value_ = value_.substring(0, truncLimit); 
            truncated_ = valueLength - truncLimit;     
        }
        else
            truncated_ = 0;

        length_ = value_.length();
    }

    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public int getSQLType()
    {
        return SQLData.LONG_NVARCHAR;
    }

    public String getCreateParameters()
    {
        return AS400JDBCDriver.getResource("MAXLENGTH");
    }

    public int getDisplaySize()
    {
        return maxLength_;
    }

    // JDBC 3.0
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
        return "LONGNVARCHAR";      
    }

    public int getMaximumPrecision()
    {
        return 32739;
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
        return 456;
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
        return java.sql.Types.LONGNVARCHAR;
    	 
    }

    public String getTypeName()
    {
        return "LONGNVARCHAR";      
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
        truncated_ = 0;
        try
        {
            BigDecimal bigDecimal = new BigDecimal(SQLDataFactory.convertScientificNotation(value_));  
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
        return new HexReaderInputStream(new StringReader(value_));
    }

    public Blob getBlob()
    throws SQLException
    {
        truncated_ = 0;
        try
        {
            return new AS400JDBCBlob(BinaryConverter.stringToBytes(value_), maxLength_);
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

        // If value equals "true" or "false", then return the
        // corresponding boolean, otherwise an empty string is
        // false, a non-empty string is true.
        String trimmedValue = value_.trim();        
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
            //return(new Double(value_.trim())).byteValue();                //@trunc
            Double doubleValue  = new Double (value_.trim ());              //@trunc
            double d = doubleValue.doubleValue();                           //@trunc
            if(d > Byte.MAX_VALUE || d < Byte.MIN_VALUE)                    //@trunc
                truncated_ = 1;                                             //@trunc
            
            return doubleValue.byteValue ();                                //@trunc
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
            return BinaryConverter.stringToBytes(value_);
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
        if(calendar == null) //@dat1
        {
            //getter methods do not enforce strict conversion
            calendar = Calendar.getInstance(); //@dat1
        }
        return SQLDate.stringToDate(value_, settings_, calendar);
    }

    public double getDouble()
    throws SQLException
    {
        truncated_ = 0;

        try
        {
            return(new Double(value_.trim())).doubleValue();
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
            return(new Double(value_.trim())).floatValue();
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
            //return(new Double(value_.trim())).intValue();        //@trunc
            Double doubleValue  = new Double (value_.trim ());     //@trunc
            double d = doubleValue.doubleValue();                  //@trunc 

            if( d > Integer.MAX_VALUE || d < Integer.MIN_VALUE)    //@trunc    
                truncated_ = 1;                                    //@trunc
                
            return doubleValue.intValue ();                        //@trunc
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
            //return(new Double(value_.trim())).longValue();   //@trunc
            Double doubleValue  = new Double (value_.trim ()); //@trunc
            double d = doubleValue.doubleValue();              //@trunc

            if( d > Long.MAX_VALUE || d < Long.MIN_VALUE)      //@trunc
                truncated_ = 1;                                //@trunc
                
            return doubleValue.longValue ();                   //@trunc
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
            //return(new Double(value_.trim())).shortValue();                //@trunc
            Double doubleValue  = new Double (value_.trim ());               //@trunc
            double d = doubleValue.doubleValue();                            //@trunc

            if( d > Short.MAX_VALUE || d < Short.MIN_VALUE)                  //@trunc      
                truncated_ = 1;                                              //@trunc
        
            return doubleValue.shortValue ();                                //@trunc
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
        // Do not signal a DataTruncation per the spec. @B1A
        int maxFieldSize = settings_.getMaxFieldSize();
        if((value_.length() > maxFieldSize) && (maxFieldSize > 0))
        {
            // @B1D truncated_ = value_.length() - maxFieldSize;
            return value_.substring(0, maxFieldSize);
        }
        else
        {
            // @B1D truncated_ = 0;
            return value_;
        }
    }

    public Time getTime(Calendar calendar)
    throws SQLException
    {
        truncated_ = 0;
        if(calendar == null) //@dat1
        {
            //getter methods do not enforce strict conversion
            calendar = Calendar.getInstance(); //@dat1
        }
        return SQLTime.stringToTime(value_, settings_, calendar);
    }

    public Timestamp getTimestamp(Calendar calendar)
    throws SQLException
    {
        truncated_ = 0;
        if(calendar == null) //@dat1
        {
            //getter methods do not enforce strict conversion
            calendar = Calendar.getInstance(); //@dat1
        }
        return SQLTimestamp.stringToTimestamp(value_, calendar);
    }

    public InputStream getUnicodeStream()
    throws SQLException
    {
        truncated_ = 0;
        try
        {
            return new ReaderInputStream(new StringReader(value_), 13488);
        }
        catch(UnsupportedEncodingException e)
        {
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
            return null;
        }
    }
    
    
    public Reader getNCharacterStream() throws SQLException
    {
        truncated_ = 0;

        // This is written in terms of getNString(), since it will
        // handle truncating to the max field size if needed.
        return new StringReader(getNString());
    }
    
   
    public NClob getNClob() throws SQLException
    {
        truncated_ = 0;

        // This is written in terms of getNString(), since it will
        // handle truncating to the max field size if needed.
        String string = getNString();
        return new AS400JDBCNClob(string, maxLength_);
    }

    
    public String getNString() throws SQLException
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
        //Decided this is of no use because rowid is so specific to the dbms internals.
        //And there are issues in length and difficulties in converting to a
        //valid rowid that is useful.
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

   
    public SQLXML getSQLXML() throws SQLException
    {
        //This is written in terms of getString(), since it will
        // handle truncating to the max field size if needed.
        truncated_ = 0;
        return new AS400JDBCSQLXML(getString().toCharArray());     
    }

    // @array
    public Array getArray() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
}

