///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLDataFactory.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
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

class SQLRowID implements SQLData
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";




    // Private data.
    private static final int        ROWID_SIZE  = 40; // This is the maxLength_ for this type of column.

    private SQLConversionSettings   settings;
    private int                     length;
    private int                     truncated;
    private byte[]                  value;


    SQLRowID(SQLConversionSettings settings) {
        this.settings   = settings;
        length          = 0;
        truncated       = 0;
        value           = new byte[0];
    }



    public Object clone() {
        return new SQLRowID(settings);
    }



//---------------------------------------------------------//
//                                                         //
// CONVERSION TO AND FROM RAW BYTES                        //
//                                                         //
//---------------------------------------------------------//

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) 
        throws SQLException {

        length = BinaryConverter.byteArrayToUnsignedShort(rawBytes, offset);
        value = new byte[length];
        System.arraycopy(rawBytes, offset+2, value, 0, length);
    }

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) 
        throws SQLException {

        BinaryConverter.unsignedShortToByteArray(length, rawBytes, offset);
        int len = (value.length < rawBytes.length) ? value.length : rawBytes.length;
        System.arraycopy(value, 0, rawBytes, offset + 2, len);
        // pad rawBytes with zeros if it has room
        for (int i=value.length; i<rawBytes.length; ++i) rawBytes[i] = 0;
    }

//---------------------------------------------------------//
//                                                         //
// SET METHODS                                             //
//                                                         //
//---------------------------------------------------------//

    public void set(Object object, Calendar calendar, int scale)
        throws SQLException {

        byte[] value = null;                                                        

        if (object instanceof String)
            value = SQLBinary.stringToBytes((String)object); 

        else if (object instanceof byte[])
            value = (byte[]) object;                                                

        else {                                                                      
            try {                                                                   
                if (object instanceof Blob) {                                       
                    Blob blob = (Blob) object;                                      
                    value = blob.getBytes(1, (int) blob.length());                
                } else if (object instanceof Clob) {                                  
                    Clob clob = (Clob) object;                                      
                    value = SQLBinary.stringToBytes(clob.getSubString(1, (int)clob.length())); 
                }                                                                   
            } catch (NoClassDefFoundError e) {                                        
                // Ignore.  It just means we are running under JDK 1.1.             
            }                                                                       
        }

        if (value == null)                                                          
            JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        value = value;                                                             

        // Truncate if necessary.
        int valueLength = value.length;
        if (valueLength > ROWID_SIZE) {
            byte[] newValue = new byte[ROWID_SIZE];
            System.arraycopy(value, 0, newValue, 0, ROWID_SIZE);
            value = newValue;
            truncated = valueLength - ROWID_SIZE;
        }
        else
            truncated = 0;

        length = value.length;
    }

//---------------------------------------------------------//
//                                                         //
// DESCRIPTION OF SQL TYPE                                 //
//                                                         //
//---------------------------------------------------------//

    public String getCreateParameters() {
        return null;
    }

    public int getDisplaySize() {
        return ROWID_SIZE ;
    }

    public String getJavaClassName() {
        return "[B";
    }

    public String getLiteralPrefix() {
        return null;
    }

    public String getLiteralSuffix() {
        return null;
    }

    public String getLocalName() {
        return "ROWID";
    }

    public int getMaximumPrecision() {
        return ROWID_SIZE;
    }

    public int getMaximumScale() {
        return 0;
    }

    public int getMinimumScale() {
        return 0;
    }

    public int getNativeType() {
        return 904;
    }

    public int getPrecision() {
        return ROWID_SIZE;
    }

    public int getRadix() {
        return 0;
    }

    public int getScale() {
        return 0;
    }
    
    public int getType() {
        return java.sql.Types.VARBINARY;
    }

    public String getTypeName() {
        return "ROWID";
    }

    public boolean isSigned() {
        return false;
    }

    public boolean isText() {
        return true;
    }

//---------------------------------------------------------//
//                                                         //
// CONVERSIONS TO JAVA TYPES                               //
//                                                         //
//---------------------------------------------------------//

    public int getActualSize() {
        return value.length;
    }

    public int getTruncated() {
        return truncated;
    }

    public InputStream toAsciiStream() throws SQLException {
        // This is written in terms of toBytes(), since it will
        // handle truncating to the max field size if needed.
        return new ByteArrayInputStream(toBytes());
    }

    public BigDecimal toBigDecimal(int scale) throws SQLException {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public InputStream toBinaryStream() throws SQLException {
        // This is written in terms of toBytes(), since it will
        // handle truncating to the max field size if needed.
        return new ByteArrayInputStream(toBytes());
    }

    public Blob toBlob() throws SQLException {
        // This is written in terms of toBytes(), since it will
        // handle truncating to the max field size if needed.
        return new AS400JDBCBlob(toBytes(), ROWID_SIZE);
    }

    public boolean toBoolean() throws SQLException {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return false;
    }

    public byte toByte() throws SQLException {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public byte[] toBytes() {
        // Truncate to the max field size if needed.
        // Do not signal a DataTruncation per the spec. 
        int maxFieldSize = settings.getMaxFieldSize();
        if ((value.length > maxFieldSize) && (maxFieldSize > 0)) {
            byte[] truncatedValue = new byte[maxFieldSize];
            System.arraycopy(value, 0, truncatedValue, 0, maxFieldSize);
            return truncatedValue;
        } else
            return value;
    }

    public Reader toCharacterStream() throws SQLException {
        // This is written in terms of toBytes(), since it will
        // handle truncating to the max field size if needed.
        return new StringReader(SQLBinary.bytesToString(toBytes()));
    }

    public Clob toClob() throws SQLException {
        // This is written in terms of toString(), since it will
        // handle truncating to the max field size if needed.
        return new AS400JDBCClob(SQLBinary.bytesToString(toBytes()), ROWID_SIZE);
    }

    public Date toDate(Calendar calendar) throws SQLException {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public double toDouble() throws SQLException {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public float toFloat() throws SQLException {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public int toInt() throws SQLException {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public long toLong() throws SQLException {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public Object toObject() {
        // This is written in terms of toBytes(), since it will
        // handle truncating to the max field size if needed.
        return toBytes();
    }

    public short toShort() throws SQLException {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public String toString() {
        // This is written in terms of toBytes(), since it will
        // handle truncating to the max field size if needed.
        return SQLBinary.bytesToString(toBytes());
    }

    public Time toTime(Calendar calendar) throws SQLException {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Timestamp toTimestamp(Calendar calendar) throws SQLException {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public InputStream toUnicodeStream() throws SQLException {
        // This is written in terms of toBytes(), since it will
        // handle truncating to the max field size if needed.
        return new ByteArrayInputStream(toBytes());
    }
}

