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

import java.io.InputStream;
import java.io.StringReader;
import java.sql.Blob;
import java.sql.Clob;
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

//@PDA jdbc40 new class

final class SQLLongNVarchar
extends SQLDataBase
{
  
    // Private data.
    private int                     length_;
    private int                     maxLength_;
    private String                  value_;

    // Note: maxLength is in bytes not counting 2 for LL.
    //
    SQLLongNVarchar(int maxLength, SQLConversionSettings settings)
    {
        super(settings);
        length_         = 0;
        maxLength_      = maxLength;
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
        /* ifdef JDBC40 
        else if(object instanceof SQLXML) 
        {    
            SQLXML xml = (SQLXML)object;
            value = xml.getString();
        }     
       endif */
        
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
            truncated_ = 0; outOfBounds_ = false; 

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
    	/* ifdef JDBC40 
        return java.sql.Types.LONGNVARCHAR;
        endif */ 
    	/* ifndef JDBC40 */ 
    	return java.sql.Types.LONGVARCHAR; 
    	/* endif */ 
    	
    	 
    }

    public String getTypeName()
    {
    	/* ifdef JDBC40 
        return "LONGNVARCHAR";    
        endif */ 
    	/* ifndef JDBC40 */ 
    	return "LONGVARCHAR";
    	/* endif */ 
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



    public InputStream getBinaryStream()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        return new HexReaderInputStream(new StringReader(value_));
    }

    public Blob getBlob()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
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


    public byte[] getBytes()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
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



    public Object getObject()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        // This is written in terms of getString(), since it will
        // handle truncating to the max field size if needed.
        return getString();
    }


    public String getString()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
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
            // @B1D truncated_ = 0; outOfBounds_ = false; 
            return value_;
        }
    }


    public String getNString() throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
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

 /* ifdef JDBC40 
    public RowId getRowId() throws SQLException
    {
        
        //truncated_ = 0; outOfBounds_ = false; 
        //try
        //{
        //    return new AS400JDBCRowId(BinaryConverter.stringToBytes(value_));
        //}
        //catch(NumberFormatException nfe)
        //{
            // this string contains non-hex characters
            //JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, nfe);
            //return null;
        //}
        
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
        truncated_ = 0; outOfBounds_ = false; 
        return new AS400JDBCSQLXML(getString());     
    }

  endif */ 
 
}

