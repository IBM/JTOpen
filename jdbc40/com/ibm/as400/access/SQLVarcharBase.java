///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLVarchar.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2014 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.StringReader;
import java.sql.Blob;
import java.sql.Clob;
/* ifdef JDBC40 */
import java.sql.NClob;
import java.sql.RowId;
/* endif */ 
import java.sql.SQLException;
/* ifdef JDBC40 */
import java.sql.SQLXML;
/* endif */ 
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.net.URL;

abstract class SQLVarcharBase
extends SQLDataBase  implements SQLVariableCompressible
{
    static final String copyright = "Copyright (C) 1997-2013 International Business Machines Corporation and others.";

    // Private data.
    protected int                     length_;
    protected int                     maxLength_;
    protected String                  value_;
    protected int                     bytesPerCharacter_; 

    // Note: maxLength is in bytes not counting 2 for LL.
    //
    SQLVarcharBase(SQLConversionSettings settings, int length, int maxLength, String value)
    {
        super(settings); 
        length_         = length;
        maxLength_      = maxLength;
        value_          = value;
        bytesPerCharacter_ = 1; 
    }

    SQLVarcharBase(SQLConversionSettings settings, int length, int maxLength, String value, int bytesPerCharacter)
    {
        super(settings); 
        length_         = length;
        maxLength_      = maxLength;
        value_          = value;
        bytesPerCharacter_ = bytesPerCharacter;  
    }

    // @A2A
    // Added method trim() to trim the string.
    public void trim()                                // @A2A
    {                                                 // @A2A
        value_ = value_.trim();                       // @A2A
    }                                                 // @A2A

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
            
        BidiConversionProperties bidiConversionProperties = new BidiConversionProperties(bidiStringType);  //@KBA
        bidiConversionProperties.setBidiImplicitReordering(settings_.getBidiImplicitReordering());         //@KBA
        bidiConversionProperties.setBidiNumericOrderingRoundTrip(settings_.getBidiNumericOrdering());      //@KBA

        try{
            // If the field is VARGRAPHIC, length_ contains the number
            // of characters in the string, while the converter is expecting
            // the number of bytes. Thus, we need to multiply length_ by bytesPerCharacter.
            value_ = ccsidConverter.byteArrayToString(rawBytes, offset+2, length_*bytesPerCharacter_, bidiConversionProperties);   //@KBC changed to use bidiConversionProperties instead of bidiStringType
        }catch(Exception e){
            JDError.throwSQLException(JDError.EXC_CHAR_CONVERSION_INVALID, e);
        }
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
                
            BidiConversionProperties bidiConversionProperties = new BidiConversionProperties(bidiStringType);  //@KBA
            bidiConversionProperties.setBidiImplicitReordering(settings_.getBidiImplicitReordering());         //@KBA
            bidiConversionProperties.setBidiNumericOrderingRoundTrip(settings_.getBidiNumericOrdering());      //@KBA

            // The length in the first 2 bytes is actually the length in characters.
            byte[] temp = ccsidConverter.stringToByteArray(value_, bidiConversionProperties);   //@KBC changed to used bidiConversionProperties instead of bidiStringType
            BinaryConverter.unsignedShortToByteArray(temp.length/bytesPerCharacter_, rawBytes, offset);
            if(temp.length > maxLength_)
            {
                truncated_ = temp.length - maxLength_;  /*@H2C*/
                maxLength_ = temp.length;
                JDError.throwSQLException(this, JDError.EXC_INTERNAL, "Change Descriptor");
            }
            System.arraycopy(temp, 0, rawBytes, offset+2, temp.length);

            // The buffer we are filling with data is big enough to hold the entire field.
            // For varchar fields the actual data is often smaller than the field width.
            // That means whatever is in the buffer from the previous send is sent to the
            // system.  The data stream includes actual data length so the old bytes are not 
            // written to the database, but the junk left over may decrease the effectiveness 
            // of compression.  The following code will write hex 0s to the buffer when
            // actual length is less that field length.  Note the 0s are written only if 
            // the field length is pretty big.  The data stream code (DBBaseRequestDS)
            // does not compress anything smaller than 1K.
            if( (maxLength_ - temp.length > 16))  //@rle
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

    
    public int convertToCompressedBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
    throws SQLException
    {
      int bytesWritten = 0; 
        try
        {
            int bidiStringType = settings_.getBidiStringType();
            // if bidiStringType is not set by user, use ccsid to get value
            if(bidiStringType == -1)
                bidiStringType = ccsidConverter.bidiStringType_;
                
            BidiConversionProperties bidiConversionProperties = new BidiConversionProperties(bidiStringType);  //@KBA
            bidiConversionProperties.setBidiImplicitReordering(settings_.getBidiImplicitReordering());         //@KBA
            bidiConversionProperties.setBidiNumericOrderingRoundTrip(settings_.getBidiNumericOrdering());      //@KBA

            // The length in the first 2 bytes is actually the length in characters.
            byte[] temp = ccsidConverter.stringToByteArray(value_, bidiConversionProperties);   //@KBC changed to used bidiConversionProperties instead of bidiStringType
            BinaryConverter.unsignedShortToByteArray(temp.length / bytesPerCharacter_, rawBytes, offset);
            bytesWritten += 2; 
            if(temp.length > maxLength_)
            {
                truncated_ = temp.length - maxLength_;  /*@H2C*/
                maxLength_ = temp.length;
                JDError.throwSQLException(this, JDError.EXC_INTERNAL, "Change Descriptor");
            }
            if (temp.length > 0)  {
            System.arraycopy(temp, 0, rawBytes, offset+2, temp.length);
               bytesWritten+= temp.length;
            }
        }
        catch(Exception e)
        {
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
        }
        return bytesWritten; 
    }


    
    
    //---------------------------------------------------------//
    //                                                         //
    // SET METHODS                                             //
    //                                                         //
    //---------------------------------------------------------//

    public void set(Object object, Calendar calendar, int scale)
    throws SQLException
    {
        String value = null;                                                        // @C1A

        if(object instanceof String)
            value = (String) object;                                                // @C1C

        else if(object instanceof Number)
            value = object.toString();                                              // @C1C

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
            value = SQLTime.timeToString((Time) object, settings_, calendar);      // @C1C

        else if(object instanceof Timestamp)
            value = SQLTimestamp.timestampToStringTrimTrailingZeros((Timestamp) object, calendar);  // @C1C

        else if(object instanceof java.util.Date)                                  // @F5M @F5C
            value = SQLDate.dateToString((java.util.Date) object, settings_, calendar); // @C1C @F5C

        else if(object instanceof URL)
            value = object.toString();

        else if(JDUtilities.JDBCLevel_ >= 20 && object instanceof Clob)
        {                                                                          // @C1C
            Clob clob = (Clob)object;                                              // @C1C
            value = clob.getSubString(1, (int)clob.length());                      // @C1C  @D1
        }                                                                     // @C1C
             
/* ifdef JDBC40 */
        else if(object instanceof SQLXML) //@PDA jdbc40 
        {    
            SQLXML xml = (SQLXML)object;
            value = xml.getString();
        }   
/* endif */ 

        if(value == null)                                                          // @C1C
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        value_ = value;                                                            // @C1A

        // Truncate if necessary.
        int valueLength = value_.length();

        int truncLimit = maxLength_ / bytesPerCharacter_;              // @F2a

        if(valueLength > truncLimit)             // @F2c
        {
            value_ = value_.substring(0, truncLimit); // @F2c
            truncated_ = valueLength - truncLimit;     // @F2c
            outOfBounds_ = false;
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
        return SQLData.VARCHAR;
    }

    public String getCreateParameters()
    {
        return AS400JDBCDriver.getResource("MAXLENGTH");
    }

    public int getDisplaySize()
    {
        return maxLength_;
    }

    //@F1A JDBC 3.0
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
        return "VARCHAR";
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
        return 448;
    }

    public int getPrecision()
    {
        return maxLength_ / bytesPerCharacter_;
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
        return java.sql.Types.VARCHAR;
    }

    public String getTypeName()
    {
        return "VARCHAR";
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
            byte[] bytes = BinaryConverter.stringToBytes(getString());
            return new AS400JDBCBlob(bytes, bytes.length);
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


    public Object getObject()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        // This is written in terms of getString(), since it will
        // handle truncating to the max field size if needed.
        return getString();
    }


    public String getString() throws SQLException
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

/* ifdef JDBC40 */
    //@pda jdbc40
    public RowId getRowId() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    //@pda jdbc40
    public SQLXML getSQLXML() throws SQLException
    {
        //This is written in terms of getString(), since it will
        // handle truncating to the max field size if needed.
        truncated_ = 0; outOfBounds_ = false; 
        return new AS400JDBCSQLXML(getString());     
    }
/* endif */ 
    
}

