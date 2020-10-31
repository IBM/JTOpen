///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: SQLGraphic.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Blob;
import java.sql.Clob;
/*ifdef JDBC40
import java.sql.NClob;
import java.sql.RowId;
endif */
import java.sql.SQLException;
/*ifdef JDBC40
import java.sql.SQLXML;
endif */
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.net.URL;

final class SQLGraphic
extends SQLDataBase
{
    static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // Private data.
    private int                     maxLength_;
    private String                  value_;
    private String                  originalValue_;
    private int                     ccsid_; //@cca1

    SQLGraphic(int maxLength, SQLConversionSettings settings, int ccsid)  //@cca1
    {
      super(settings);
        maxLength_      = maxLength;
        truncated_ = 0; outOfBounds_ = false; 
        value_          = "";
        originalValue_  = "";
        ccsid_          = ccsid;  //@cca1
    }

    public Object clone()
    {
        return new SQLGraphic(maxLength_, settings_, ccsid_); //@cca1
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter, boolean ignoreConversionErrors)
    throws SQLException
    {
        int bidiStringType = settings_.getBidiStringType();

        // if bidiStringType is not set by user, use ccsid to get value
        if(bidiStringType == -1)
            bidiStringType = ccsidConverter.bidiStringType_;

        BidiConversionProperties bidiConversionProperties = new BidiConversionProperties(bidiStringType);  //@KBA
        bidiConversionProperties.setBidiImplicitReordering(settings_.getBidiImplicitReordering());         //@KBA
        bidiConversionProperties.setBidiNumericOrderingRoundTrip(settings_.getBidiNumericOrdering());      //@KBA

        value_ = ccsidConverter.byteArrayToString(rawBytes, offset, maxLength_, bidiConversionProperties);  //@KBC changed to use bidiConversionProperties instead of bidiStringType
        originalValue_ = value_; 
    }

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
    throws SQLException
    {
        //   We originally padded with a single byte space.  We now have the
        //   ccsid so we can figure out if that was right or not.  If we should
        //   have use the double byte space, re-pad.
        int ccsid = ccsidConverter.getCcsid();
        if(ccsid != 13488 && ccsid != 1200)
        {
            int valueLength = originalValue_.length();
            int exactLength = getDisplaySize();
            if(valueLength < exactLength)
            {
                StringBuffer buffer = new StringBuffer(originalValue_);
                char c = '\u3000';
                for(int i = valueLength; i < exactLength; ++i)
                    buffer.append(c);
                value_ = buffer.toString();
            }
        }

        int bidiStringType = settings_.getBidiStringType();

        // if bidiStringType is not set by user, use ccsid to get value
        if(bidiStringType == -1)
            bidiStringType = ccsidConverter.bidiStringType_;

        BidiConversionProperties bidiConversionProperties = new BidiConversionProperties(bidiStringType);  //@KBA
        bidiConversionProperties.setBidiImplicitReordering(settings_.getBidiImplicitReordering());         //@KBA
        bidiConversionProperties.setBidiNumericOrderingRoundTrip(settings_.getBidiNumericOrdering());      //@KBA

        try
        {
            ccsidConverter.stringToByteArray(value_, rawBytes, offset, maxLength_, bidiConversionProperties);   //@KBC changed to use bidiConversionProperties instead of bidiStringType
        }
        catch(CharConversionException e)
        {
            maxLength_ = ccsidConverter.stringToByteArray(value_, bidiConversionProperties).length; //@KBC changed to use bidiConversionProperties instead of bidiStringType
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
        
        else if(object instanceof Character)
            value = object.toString();


        else if(object instanceof Number)
            value = object.toString();

        else if(object instanceof Boolean)
        {
            // @PDC
            // if "translate boolean" == false, then use "0" and "1" values to match native driver
            if(settings_.getTranslateBoolean() == true)
                value = object.toString();  //"true" or "false"
            else
                value = ((Boolean)object).booleanValue() == true ? "1" : "0";
        }

        else if(object instanceof Time)
            value = SQLTime.timeToString((Time) object, settings_, calendar);

        else if(object instanceof Timestamp)
            value = SQLTimestamp.timestampToStringTrimTrailingZeros((Timestamp) object, calendar, settings_);

        else if(object instanceof java.util.Date)
            value = SQLDate.dateToString((java.util.Date) object, settings_, calendar);

        else if(object instanceof URL)
            value = object.toString();

        else if( object instanceof Clob)
        {
            Clob clob = (Clob)object;
            value = clob.getSubString(1, (int)clob.length());
        }
        else if(object instanceof Reader)
        {
          value = getStringFromReader((Reader) object, ALL_READER_BYTES, this);
        }

        /* ifdef JDBC40
        else if(object instanceof SQLXML) //@PDA jdbc40
        {
            SQLXML xml = (SQLXML)object;
            value = xml.getString();
        }
        endif */

    if (value == null) {
      if (JDTrace.isTraceOn()) {
          if (object == null) { 
              JDTrace.logInformation(this, "Unable to assign null object");
            } else { 
                JDTrace.logInformation(this, "Unable to assign object("+object+") of class("+object.getClass().toString()+")");
            }
      }

      JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    }
        value_ = value;
        originalValue_ = value;


        // Set to the exact length.
        int valueLength = value_.length();
        int exactLength = getDisplaySize();
        if(valueLength < exactLength)
        {
            StringBuffer buffer = new StringBuffer(value_);
            char c = '\u0020';
            for(int i = valueLength; i < exactLength; ++i)
                buffer.append(c);
            value_ = buffer.toString();
            truncated_ = 0; outOfBounds_ = false;
        }
        else if(valueLength > exactLength)
        {
            value_ = value_.substring(0, exactLength);
            originalValue_ = value_; 
            truncated_ = valueLength - exactLength;
            outOfBounds_ = false;
        }
        else
            truncated_ = 0; outOfBounds_ = false;
    }

    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public int getSQLType()
    {
        return SQLData.GRAPHIC;
    }

    public String getCreateParameters()
    {
        return AS400JDBCDriver.getResource("MAXLENGTH",null);
    }

    public int getDisplaySize()
    {
        if(ccsid_ == 65535)    //@bingra
            return maxLength_; //@bingra
        else
            return maxLength_ / 2;
    }

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
        return "GRAPHIC";
    }

    public int getMaximumPrecision()
    {
        return 16382;
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
        return 468;
    }

    public int getPrecision()
    {
        /* maxLength_ is the length in bytes */
        return maxLength_ / 2 ;
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
        if (ccsid_ == 1200) {
           return java.sql.Types.NCHAR; 
        }
endif */ 
        return java.sql.Types.CHAR;
    }

    public String getTypeName()
    {
        if(  ccsid_ == 1200)  //@cca1
            return "NCHAR";  //@cca1 same as native
        return "GRAPHIC";
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
        return new HexReaderInputStream(new StringReader(getString()));
    }

    public Blob getBlob()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false;
        try
        {
            return new AS400JDBCBlob(BinaryConverter.stringToBytes(getString()), maxLength_);
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
            return BinaryConverter.stringToBytes(getString());
        }
        catch(NumberFormatException nfe)
        {
            // this field contains non-hex characters
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, nfe);
            return null;
        }
    }

    public Object getBatchableObject() throws SQLException {
      // Return the object that has not yet been padded
      return originalValue_; 
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


    // Added method trim() to trim the string.
    public void trim()
    {
        value_ = value_.trim();
        originalValue_ = value_; 
    }


    //@pda jdbc40
    public String getNString() throws SQLException
    {
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
    //@pda jdbc40
    public RowId getRowId() throws SQLException
    {
        //
        //truncated_ = 0; outOfBounds_ = false;
        //try
        //{
        //    return new AS400JDBCRowId(BinaryConverter.stringToBytes(value_));
        //}
        //catch(NumberFormatException nfe)
        //{
            // this string contains non-hex characters
        //    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, nfe);
        //    return null;
        //}

        //decided this is of no use
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

    public void saveValue() {
      savedValue_ = value_; 
   }
}

