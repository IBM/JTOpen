///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLBlobLocator.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;



class SQLBlobLocator
implements SQLLocator           // @A2C
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    // Private data.
    private static final AS400Bin4  typeConverter_ = new AS400Bin4 ();

    private AS400JDBCConnection     connection_;
    private int                     id_;
    private JDLobLocator            locator_;
    private int                     maxLength_;
    private SQLConversionSettings   settings_;
    private int                     truncated_;
    private int                     columnIndex_;  //@D2A



    SQLBlobLocator (AS400JDBCConnection connection,
                    int id,
                    int maxLength, 
                    SQLConversionSettings settings,
		          int columnIndex)	  //@D2A
    {
        connection_     = connection;
        id_             = id;
        locator_        = new JDLobLocator (connection, id, maxLength);
        maxLength_      = maxLength;
        settings_       = settings;
        truncated_      = 0;
	   columnIndex_    = columnIndex;    //@D2A
    }



    public Object clone ()
    {
        return new SQLBlobLocator (connection_, id_, maxLength_, settings_, columnIndex_);  //@D2C
    }



    public void setHandle (int handle)                          // @A2A
    {                                                           // @A2A
        locator_.setHandle (handle);                            // @A2A
    }                                                           // @A2A



//---------------------------------------------------------//
//                                                         //
// CONVERSION TO AND FROM RAW BYTES                        //
//                                                         //
//---------------------------------------------------------//



    public void convertFromRawBytes (byte[] rawBytes, int offset, ConverterImplRemote ccsidConverter)
        throws SQLException
    {
        int locatorHandle = ((Integer) typeConverter_.toObject (rawBytes, offset)).intValue ();        
        locator_.setHandle (locatorHandle);
	   locator_.setColumnIndex(columnIndex_);   //@D2A
    }



    public void convertToRawBytes (byte[] rawBytes, int offset, ConverterImplRemote ccsidConverter)
        throws SQLException
    {
        typeConverter_.toBytes (locator_.getHandle (), rawBytes, offset);
    }



//---------------------------------------------------------//
//                                                         //
// SET METHODS                                             //
//                                                         //
//---------------------------------------------------------//



    public void set (Object object, Calendar calendar, int scale)
        throws SQLException
    {        
        boolean set = false;                                                        // @A1A

        if (object instanceof byte[]) {                                             // @A2A
            byte[] bytes = (byte[]) object;                                         // @A2A
            locator_.writeData (0, bytes.length, bytes);                            // @A2A
            set = true;                                                             // @A2A
        }                                                                           // @A2A
        else {                                                                      // @A2A

            try {                                                                   // @A1C
                if (object instanceof Blob) {                                       // @A1C
                    Blob blob = (Blob) object;                                      // @A1C
                    int length = (int) blob.length ();
                    locator_.writeData (0, length, blob.getBytes (0, length));
                    set = true;                                                     // @A1A
                }                                                                   // @A1C
            }                                                                       // @A1C
            catch (NoClassDefFoundError e) {                                        // @A1C
                // Ignore.  It just means we are running under JDK 1.1.             // @A1C
            }                                                                       // @A1C        
        }                                                                           // @A2A
        
        if (! set)                                                                  // @A1C
            JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
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
        return maxLength_;
    }



    public String getLiteralPrefix ()
    {
        return null;
    }



    public String getLiteralSuffix ()
    {
        return null;
    }



    public String getLocalName ()
    {
        return "BLOB"; 
    }



    public int getMaximumPrecision ()
    {
        return 15728640;
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
        return 960;
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
		return java.sql.Types.BLOB;
	}



	public String getTypeName ()
	{
		return "BLOB"; 
	}



// @C1D    public boolean isGraphic ()
// @C1D    {
// @C1D        return false;
// @C1D    }



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
        return maxLength_;
    }



    public int getTruncated ()
    {
        return 0;
    }



	public InputStream toAsciiStream ()
	    throws SQLException
	{
	    return new AS400JDBCInputStream (locator_);
	}



	public BigDecimal toBigDecimal (int scale)
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
	}



	public InputStream toBinaryStream ()
	    throws SQLException
	{
	    return new AS400JDBCInputStream (locator_);
	}



	public Blob toBlob ()
	    throws SQLException
	{
	    return new AS400JDBCBlobLocator (locator_);
	}



	public boolean toBoolean ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return false;
	}



	public byte toByte ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return -1;
	}



	public byte[] toBytes ()
        throws SQLException
	{
        DBLobData data = locator_.retrieveData(0, (int)locator_.getLength());               // @C2A
        int actualLength = data.getLength();                                                // @C2A
        byte[] bytes = new byte[actualLength];                                              // @C2A
        System.arraycopy(data.getRawBytes(), data.getOffset(), bytes, 0, actualLength);     // @C2A
        return bytes;                                                                       // @C2A

		// @C2D JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		// @C2D return null;
	}



	public Reader toCharacterStream ()
	    throws SQLException
	{
	    return new InputStreamReader (new AS400JDBCInputStream (locator_));
	}



	public Clob toClob ()
	    throws SQLException
	{
	    return new AS400JDBCClobLocator (locator_, connection_.getConverter ());        
	}



	public Date toDate (Calendar calendar)
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
	}



	public double toDouble ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return -1;
	}



	public float toFloat ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return -1;
	}



	public int toInt ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return -1;
	}



	public long toLong ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return -1;
	}



	public Object toObject ()
	{
	    return new AS400JDBCBlobLocator (locator_);
	}



	public short toShort ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return -1;
	}



	public String toString ()        
	{
          return super.toString ();
	}



	public Time toTime (Calendar calendar)
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
	}



	public Timestamp toTimestamp (Calendar calendar)
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
	}



	public InputStream toUnicodeStream ()
	    throws SQLException
	{
	    return new AS400JDBCInputStream (locator_);
	}


}

