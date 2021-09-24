///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: SQLDBClob.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2006 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
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

final class SQLDBClob extends SQLDataBase
{
    static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    private int                     length_;                    // Length of string, in characters.     @E3C
    private int                     maxLength_;                 // Max length of field, in bytes.       @E3C
    private String                  value_;
    private Object savedObject_; // This is our byte[] or InputStream or whatever that we save to convert to bytes until we really need to.

    private int ccsid_;  /*@P3A*/

    // Note: maxLength is in bytes not counting 2 for LL.
    //
    SQLDBClob(int maxLength, SQLConversionSettings settings)
    {
      super(settings);
        length_         = 0;
        maxLength_      = maxLength;
        value_          = "";
    }

    public Object clone()
    {
       SQLDBClob newClob =new SQLDBClob(maxLength_, settings_);
       newClob.setCcsid(ccsid_);
        return newClob;
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter, boolean ignoreConversionErrors)
    throws SQLException
    {
        length_ = BinaryConverter.byteArrayToInt(rawBytes, offset);

        int bidiStringType = settings_.getBidiStringType();

        // if bidiStringType is not set by user, use ccsid to get value
        if(bidiStringType == -1) bidiStringType = ccsidConverter.bidiStringType_;

        BidiConversionProperties bidiConversionProperties = new BidiConversionProperties(bidiStringType);  //@KBA
        bidiConversionProperties.setBidiImplicitReordering(settings_.getBidiImplicitReordering());         //@KBA
        bidiConversionProperties.setBidiNumericOrderingRoundTrip(settings_.getBidiNumericOrdering());      //@KBA

        // If the field is DBCLOB, length_ contains the number
        // of characters in the string, while the converter is expecting
        // the number of bytes. Thus, we need to multiply length_ by 2.
        value_ = ccsidConverter.byteArrayToString(rawBytes, offset + 4, length_*2, bidiConversionProperties);   //@KBA changed to use bidiConversionProperties instead of bidiStringType
        savedObject_ = null;
    }

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
    throws SQLException
    {
        if(savedObject_ != null) doConversion();

        BinaryConverter.intToByteArray(length_, rawBytes, offset);
        try
        {
            int bidiStringType = settings_.getBidiStringType();
            if(bidiStringType == -1) bidiStringType = ccsidConverter.bidiStringType_;

            BidiConversionProperties bidiConversionProperties = new BidiConversionProperties(bidiStringType);  //@KBA
            bidiConversionProperties.setBidiImplicitReordering(settings_.getBidiImplicitReordering());         //@KBA
            bidiConversionProperties.setBidiNumericOrderingRoundTrip(settings_.getBidiNumericOrdering());      //@KBA

            ccsidConverter.stringToByteArray(value_, rawBytes, offset + 4, maxLength_, bidiConversionProperties);   //@KBC changed to bidiConversionProperties instead of bidiStringType
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
        //@selins1 make similar to SQLDBClobLocator
        // If it's a String we check for data truncation.
        if(object instanceof String)
        {
            String s = (String)object;
            int byteLength = s.length() * 2; //@selins1
            truncated_ = (byteLength > maxLength_ ? byteLength-maxLength_ : 0); //@selins1
            outOfBounds_ = false;
        } else if( !(object instanceof Reader) &&
                !(object instanceof InputStream) &&
                ( !(object instanceof Clob))
/*ifdef JDBC40
                  && !(object instanceof SQLXML)
endif */


                )
        {
          if (JDTrace.isTraceOn()) {
              if (object == null) { 
                  JDTrace.logInformation(this, "Unable to assign null object");
                } else { 
                    JDTrace.logInformation(this, "Unable to assign object("+object+") of class("+object.getClass().toString()+")");
                }
          }

            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        }

        savedObject_ = object;
        if(scale != -1) {
          length_ = scale;
        } else {
            length_ = ALL_READER_BYTES; 
            
        } 
    }

    private void doConversion() throws SQLException
    {
        try
        {
            Object object = savedObject_;
            if(savedObject_ instanceof String)
            {
                value_ = (String)object;
            }
            else if(object instanceof Reader)
            {
                value_ = getStringFromReader((Reader)object, length_, this); 
            }
            else if( object instanceof Clob)
            {
                Clob clob = (Clob)object;
                value_ = clob.getSubString(1, (int)clob.length());
            }
            /* ifdef JDBC40

            else if(object instanceof SQLXML)  //@PDA jdbc40
            {
                SQLXML xml = (SQLXML)object;
                value_ = xml.getString();
            }
            endif */
            else
            {
                JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
            }

            // Truncate if necessary.
            int valueLength = value_.length();
            // Max length is in bytes. We should truncate
            // before sending to the server.  Other code 
            // should have issued the warning for data truncation
            
            if(valueLength > maxLength_ / 2)
            {
                value_ = value_.substring(0, maxLength_ / 2);
                truncated_ = valueLength - maxLength_ / 2;
                outOfBounds_ = false;
            }
            else
            {
                truncated_ = 0; outOfBounds_ = false;
            }

            length_ = value_.length();
        }
        finally
        {
            savedObject_ = null;
        }
    }

    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public int getSQLType()
    {
        return SQLData.DBCLOB;
    }

    public String getCreateParameters()
    {
        return AS400JDBCDriver.getResource("MAXLENGTH",null);
    }

    public int getDisplaySize()
    {
        return(maxLength_ / 2);
    }


    public String getJavaClassName()
    {
/* ifdef JDBC40 
      if (ccsid_ == 1200 ) {
        return "com.ibm.as400.access.AS400JDBCNClob";
      }
endif */       
      
      return "com.ibm.as400.access.AS400JDBCClob";
    }

    public String getLiteralPrefix()
    {
        return null;
    }

    public String getLiteralSuffix()
    {
        return null;
    }

    public String getLocalName()
    {
        return "DBCLOB";
    }

    public int getMaximumPrecision()
    {
        return 1073741822; // the DB2 SQL reference says this should be 1073741823 but we return 1 less to allow for NOT NULL columns
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
        return 412;
    }

    public int getPrecision()
    {
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
      // @P3A
      if (ccsid_ == 1200 ) {
        return java.sql.Types.NCLOB;   
      }
endif */       
        return java.sql.Types.CLOB;
    }

    public String getTypeName()
    {
      // @P3A
      if (ccsid_ == 1200 ) {
        return "NCLOB";  
      }

        return "DBCLOB";
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


    public BigDecimal getBigDecimal(int scale)
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public InputStream getBinaryStream()
    throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false;
        return new HexReaderInputStream(new StringReader(value_));
    }

    public Blob getBlob()
    throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false;
        try
        {
            return new AS400JDBCBlob(BinaryConverter.stringToBytes(value_), maxLength_);
        }
        catch(NumberFormatException nfe)
        {
            // this DBClob contains non-hex characters
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, nfe);
            return null;
        }
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
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false;
        try
        {
            return BinaryConverter.stringToBytes(value_);
        }
        catch(NumberFormatException nfe)
        {
            // this DBClob contains non-hex characters
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, nfe);
            return null;
        }
    }

    public Reader getCharacterStream()
    throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false;
        return new StringReader(value_);
    }

    public Clob getClob()
    throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false;
        return new AS400JDBCClob(value_, maxLength_);
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

    public Object getObject()
    throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false;
/* ifdef JDBC40 
        if (ccsid_ == 1200 ) {
           return new AS400JDBCNClob(value_, maxLength_);
        }
  endif */       
        
        
        return new AS400JDBCClob(value_, maxLength_);
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
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false;
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

    public InputStream getUnicodeStream()
    throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false;
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


    //@pda jdbc40
    public Reader getNCharacterStream() throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false;
        return new StringReader(value_);
    }

    //@pda jdbc40
    /* ifdef JDBC40

    public NClob getNClob() throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false;
        return new AS400JDBCNClob(value_, maxLength_);
    }
    endif */
    //@pda jdbc40
    public String getNString() throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false;
        return value_;
    }
   
    /*@P3A*/
    public void setCcsid(int ccsid) {
     ccsid_ = ccsid; 
    }

    //@pda jdbc40
    /* ifdef JDBC40

    public RowId getRowId() throws SQLException
    {

        //if(savedObject_ != null) doConversion();
        //truncated_ = 0; outOfBounds_ = false;
        //try
        //{
        //    return new AS400JDBCRowId(BinaryConverter.stringToBytes(value_));
        //}
        //catch(NumberFormatException nfe)
        //{
            // this Clob contains non-hex characters
        //    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, nfe);
        //    return null;
        //}
        //decided this is of no use
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
    endif */

    //@pda jdbc40
    /* ifdef JDBC40
    public SQLXML getSQLXML() throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0; outOfBounds_ = false;
        return new AS400JDBCSQLXML(value_);
    }
    endif */
    // @array
    
    
    public void saveValue() throws SQLException {
      if (savedObject_ != null  && (value_ == null || value_.length() == 0)   ) {
        doConversion(); 
      }
      
      savedValue_ = value_; 
   }

   
    
}
