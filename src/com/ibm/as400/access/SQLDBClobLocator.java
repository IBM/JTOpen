///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLDBClobLocator.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.*;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Enumeration;                               // @G5A
import java.util.Vector;                                    // @G5A

class SQLDBClobLocator
implements SQLLocator                                       // @B3C
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // Private data.
    private static final AS400Bin4  typeConverter_ = new AS400Bin4();

    private AS400JDBCConnection     connection_;
    private ConvTable               converter_; //@P0C
    private int                     id_;
    private JDLobLocator            locator_;
    private int                     maxLength_;
    private SQLConversionSettings   settings_;
    private int                     truncated_;
    private int                     columnIndex_;   //@E3A

    SQLDBClobLocator(AS400JDBCConnection connection,
                   int id,
                   int maxLength, 
                   SQLConversionSettings settings,
                   ConvTable converter,                                  // @E1A @P0C
                   int columnIndex)                //@E3A
    {
        connection_     = connection;
        id_             = id;
        locator_        = new JDLobLocator(connection, id, maxLength);             // @B3C
        locator_.setGraphic(true); // @E4A
        maxLength_      = maxLength;
        settings_       = settings;
        truncated_      = 0;

        // @E1D try {
        // @E1D     converter_      = graphic ? connection.getGraphicConverter()
        // @E1D                               : connection.getConverter();            
        // @E1D }
        // @E1D catch(SQLException e) {
        // @E1D     converter_  = null;
        // @E1D }

        converter_      = converter;                                                // @E1A
        columnIndex_    = columnIndex;     //@E3A
    }

    // @E1D     SQLClobLocator(AS400JDBCConnection connection,
    // @E1D                     int id,
    // @E1D                     int maxLength, 
    // @E1D                     SQLConversionSettings settings)                           
    // @E1D     {
    // @E1D         this(connection, id, maxLength, false, settings);             
    // @E1D     }

    public Object clone()
    {
        return new SQLDBClobLocator(connection_, id_, maxLength_, settings_, converter_, columnIndex_);                // @E1C //@E3C
    }

    public void setHandle(int handle)                          // @B3A
    {                                                           // @B3A
        locator_.setHandle(handle);                            // @B3A
    }                                                           // @B3A

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
        //int locatorHandle = ((Integer) typeConverter_.toObject(rawBytes, offset)).intValue(); //@H0D
        int locatorHandle = BinaryConverter.byteArrayToInt(rawBytes, offset);  //@H0A sync with SQLBlobLocator
        locator_.setHandle(locatorHandle);
        locator_.setColumnIndex(columnIndex_);  //@E3A
    }

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
        //typeConverter_.toBytes(locator_.getHandle(), rawBytes, offset); //@H0D
        BinaryConverter.intToByteArray(locator_.getHandle(), rawBytes, offset); //@H0A sync with SQLBlobLocator
    }

    //---------------------------------------------------------//
    //                                                         //
    // SET METHODS                                             //
    //                                                         //
    //---------------------------------------------------------//

    public void set(Object object, Calendar calendar, int scale)
    throws SQLException
    {
        boolean set = false;                                                            // @B2A

        if(object instanceof String)
        {                                                 // @B3A
            String string = (String)object;                                            // @B3A
            byte[] bytes = converter_.stringToByteArray(string);                       // @B3A
            locator_.writeData(0, string.length(), bytes);                             // @B3A @E2C
            set = true;                                                                 // @B3A
        }                                                                               // @B3A
        //@H0A start change to use readers and streams
        else if(object instanceof Reader)
        {
            int length = scale; // hack to get the length into the set method
            if(length > 0)
            {
                Reader reader = (Reader)object;
                char[] charBuffer = new char[AS400JDBCPreparedStatement.LOB_BLOCK_SIZE]; // buffer is 256K
                try
                {
                    int totalCharsRead = 0;
                    int start = 0;
                    int charsRead = 0;

                    // create a writer here and use an bytearrayoutputstream
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    ConvTableWriter writer = new ConvTableWriter(bout, converter_.getCcsid(), 0, AS400JDBCPreparedStatement.LOB_BLOCK_SIZE);

                    while(charsRead > -1 && totalCharsRead < length)
                    {
                        if(totalCharsRead+AS400JDBCPreparedStatement.LOB_BLOCK_SIZE < length)
                        {
                            charsRead = reader.read(charBuffer);
                        }
                        else
                        {
                            charsRead = reader.read(charBuffer,0,length-totalCharsRead);
                        }
                        totalCharsRead += charsRead;

                        // take the chars read from the ConvTableReader and write them to a ConvTableWriter
                        // wrapped around a ByteArrayOutputStream to get the bytes using a stateful converter
                        writer.write(charBuffer, 0, charsRead);
                        writer.flush();

                        byte[] byteBuffer = bout.toByteArray();
                        locator_.writeData(start, charsRead, byteBuffer);

                        // reset the byte array output stream
                        bout.reset();
                        start += charsRead; // keep track of the starting offset into the lob for the next block
                    }
                    set = true;
                }
                catch(IOException ie)
                {
                    JDError.throwSQLException(JDError.EXC_INTERNAL, ie);
                }
            }
        }
        else if(object instanceof InputStream)
        {
            int length = scale; // hack to get the length into the set method
            if(length > 0)
            {
                InputStream stream = (InputStream)object;
                byte[] byteBuffer = new byte[AS400JDBCPreparedStatement.LOB_BLOCK_SIZE]; // buffer is 256KB
                try
                {
                    int totalBytesRead = 0;
                    int start = 0;
                    int bytesRead = 0;

                    while(bytesRead > -1 && totalBytesRead < length)
                    {
                        if(totalBytesRead+AS400JDBCPreparedStatement.LOB_BLOCK_SIZE < length)
                        {
                            bytesRead = stream.read(byteBuffer);
                        }
                        else
                        {
                            bytesRead = stream.read(byteBuffer, 0, length-totalBytesRead);
                        }
                        totalBytesRead += bytesRead;

                        locator_.writeData(start, bytesRead, byteBuffer);

                        start += bytesRead; // keep track of the starting offset into the lob for the next block
                    }
                    set = true;
                }
                catch(IOException ie)
                {
                    JDError.throwSQLException(JDError.EXC_INTERNAL, ie);
                }
            }
        }
        //@H0A end change to use readers and streams
        else
        {                                                                          // @B3A
            //@H0D try // we took this out because different versions of different JVMs behave differently...
            //@H0D {                                                                       // @B2A
            if(JDUtilities.JDBCLevel_ >= 20 && object instanceof Clob)//@H0A check for jdbc level to know if lobs exist
            {
                //@G5A Start new code for updateable locator case
                if(object instanceof AS400JDBCClobLocator)
                {
                    AS400JDBCClobLocator clob = (AS400JDBCClobLocator)object;
                    synchronized(clob.getInternalLock())
                    {
                        Vector positionsToStartUpdates = clob.getPositionsToStartUpdates();
                        if(positionsToStartUpdates != null)
                        {
                            Vector stringsToUpdate = clob.getStringsToUpdate();
                            for(int i = 0; i < stringsToUpdate.size(); i++)
                            {
                                long startPosition = ((Long)positionsToStartUpdates.elementAt(i)).longValue();     //@D3C
                                String updateString = (String)stringsToUpdate.elementAt(i);                        //@D3C
                                locator_.writeData((int)startPosition, updateString.length(), converter_.stringToByteArray(updateString));
                            }
                            // If writeData calls do not throw an exception, update has been successfully made.
                            positionsToStartUpdates = null;
                            stringsToUpdate = null;
                            set = true;
                        }
                    }
                }

                //@G5A End new code

                //@G5A If the code for updateable lob locators did not run, then run old code.
                if(!set)
                {
                    Clob clob = (Clob)object;
                    int length = (int)clob.length();
                    String substring = clob.getSubString(1, length);                   // @D1
                    locator_.writeData(0, length, converter_.stringToByteArray(substring));
                    set = true;                                                         // @B2A
                }
            }
            //@H0D }
            //@H0D catch(NoClassDefFoundError e)
            //@H0D {                                            // @B2C
            //@H0D     // Ignore.  It just means we are running under JDK 1.1.                 // @B2C
            //@H0D }                                                                           // @B2C        
        }                                                                               // @B3A

        if(! set)                                                                     // @B2C
            JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    }

    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public String getCreateParameters()
    {
        return AS400JDBCDriver.getResource("MAXLENGTH"); 
    }

    public int getDisplaySize()
    {
        //if(graphic_)
            return(maxLength_ / 2);
        //else
        //    return maxLength_;
    }

    //@F1A JDBC 3.0
    public String getJavaClassName()
    {
        return "com.ibm.as400.access.AS400JDBCClobLocator";   
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
        return 15728640;
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
        //if(graphic_)
            return 968;
        //else
        //    return 964;        
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
        return java.sql.Types.CLOB;
    }

    public String getTypeName()
    {
        //if(graphic_)
            return "DBCLOB";
        //else
        //    return "CLOB";
    }

    // @E1D     public boolean isGraphic()
    // @E1D     {
    // @E1D         return graphic_;
    // @E1D     }

    public boolean isSigned()
    {
        return false;
    }

    public boolean isText()
    {
        return true;
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSIONS TO JAVA TYPES                               //
    //                                                         //
    //---------------------------------------------------------//

    public int getActualSize()
    {
        return maxLength_;
    }

    public int getTruncated()
    {
        return 0;
    }

    public InputStream toAsciiStream()
    throws SQLException
    {
        return new AS400JDBCInputStream((JDLobLocator)locator_.clone(), converter_, "ISO8859_1"); // @D2c
    }

    public BigDecimal toBigDecimal(int scale)
    throws SQLException
    {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public InputStream toBinaryStream()
    throws SQLException
    {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Blob toBlob()
    throws SQLException
    {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public boolean toBoolean()
    throws SQLException
    {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return false;
    }

    public byte toByte()
    throws SQLException
    {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }

    public byte[] toBytes()
    throws SQLException
    {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Reader toCharacterStream()
    throws SQLException
    {
        try
        {                                                                    // @B3A
            //@E4D return new InputStreamReader(new AS400JDBCInputStream(locator_), converter_.getEncoding()); // @B3C  
            return new ConvTableReader(new AS400JDBCInputStream((JDLobLocator)locator_.clone()), converter_.getCcsid()); // @E4A @D2c
        }                                                                        // @B3A
        catch(UnsupportedEncodingException e)
        {                                 // @B3A
            JDError.throwSQLException(JDError.EXC_INTERNAL, e);                 // @B3A
            return null;                                                         // @B3A
        }                                                                        // @B3A
    }

    public Clob toClob()
    throws SQLException
    {
        return new AS400JDBCClobLocator((JDLobLocator)locator_.clone(), converter_);        
    }

    public Date toDate(Calendar calendar)
    throws SQLException
    {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public double toDouble()
    throws SQLException
    {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }

    public float toFloat()
    throws SQLException
    {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }

    public int toInt()
    throws SQLException
    {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }

    public long toLong()
    throws SQLException
    {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }

    public Object toObject()
    {
        return new AS400JDBCClobLocator((JDLobLocator)locator_.clone(), converter_);
    }

    public short toShort()
    throws SQLException
    {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return 0;
    }

    public String toString()
    {
        try
        {                                                                           // @C0A
            DBLobData data = locator_.retrieveData(0, locator_.getMaxLength());        // @C0A
            String value = converter_.byteArrayToString(data.getRawBytes(),           // @C0A
                                                        data.getOffset(),             // @C0A
                                                        data.getLength());            // @C0A
            return value;                                                               // @C0A
        }                                                                               // @C0A
        catch(SQLException e)
        {                                                        // @C0A
            // toString() should not throw exceptions!                                  // @C0A
            return super.toString();
        }                                                                               // @C0A
    }

    public Time toTime(Calendar calendar)
    throws SQLException
    {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Timestamp toTimestamp(Calendar calendar)
    throws SQLException
    {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public InputStream toUnicodeStream()
    throws SQLException
    {
        return new AS400JDBCInputStream((JDLobLocator)locator_.clone(), converter_, "UnicodeBigUnmarked"); // @B1C @D2c
    }
}

