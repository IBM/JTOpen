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
import java.util.Enumeration;  //@G5A
import java.util.Vector;       //@G5A



class SQLBlobLocator
implements SQLLocator           // @A2C
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




  // Private data.
    //@D3D private static final AS400Bin4  typeConverter_ = new AS400Bin4 ();

  private AS400JDBCConnection     connection_;
  private int                     id_;
  private JDLobLocator            locator_;
  private int                     maxLength_;
  private SQLConversionSettings   settings_;
  private int                     truncated_;
  private int                     columnIndex_;  //@C3A



  SQLBlobLocator (AS400JDBCConnection connection,
                  int id,
                  int maxLength, 
                  SQLConversionSettings settings,
                  int columnIndex)    //@C3A
  {
    connection_     = connection;
    id_             = id;
    locator_        = new JDLobLocator (connection, id, maxLength);
    maxLength_      = maxLength;
    settings_       = settings;
    truncated_      = 0;
    columnIndex_    = columnIndex;    //@C3A
  }



  public Object clone ()
  {
    return new SQLBlobLocator (connection_, id_, maxLength_, settings_, columnIndex_);  //@C3C
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



  public void convertFromRawBytes (byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
  throws SQLException
  {
        //@D3D int locatorHandle = ((Integer) typeConverter_.toObject (rawBytes, offset)).intValue ();  
        int locatorHandle = BinaryConverter.byteArrayToInt(rawBytes, offset);  //@D3A
    locator_.setHandle (locatorHandle);
    locator_.setColumnIndex(columnIndex_);   //@C3A
  }



  public void convertToRawBytes (byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
  throws SQLException
  {
        //@D3D typeConverter_.toBytes (locator_.getHandle (), rawBytes, offset);
        BinaryConverter.intToByteArray(locator_.getHandle(), rawBytes, offset);  //@D3A
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

    if (object instanceof byte[])
    {                                             // @A2A
      byte[] bytes = (byte[]) object;                                         // @A2A
      locator_.writeData (0, bytes.length, bytes);                            // @A2A
      set = true;                                                             // @A2A
    }                                                                           // @A2A
    else
    {                                                                      // @A2A

      try
      {                                                                   // @A1C
        if (object instanceof Blob)
        {                                       // @A1C
                    //@G5A Start new code for updateable locator case to go through the Vectors 
                    //@G5A and update the blob copy when ResultSet.updateBlob() is called.
                    if (object instanceof AS400JDBCBlobLocator)
                    {
                        AS400JDBCBlobLocator blob = (AS400JDBCBlobLocator) object;
                        //Synchronize on a lock so that the user can't keep making updates
                        //to the blob while we are taking updates off the vectors.
                        synchronized (blob.getInternalLock())
                        {
                            Vector positionsToStartUpdates = blob.getPositionsToStartUpdates();
                            if (positionsToStartUpdates != null)
                            {
                                Vector bytesToUpdate = blob.getBytesToUpdate();
                                for (int i = 0; i < positionsToStartUpdates.size(); i++)
                                {
                                    long startPosition = ((Long)positionsToStartUpdates.elementAt(i)).longValue();
                                    byte[] updateBytes = (byte[])bytesToUpdate.elementAt(i);
                                    locator_.writeData((int)startPosition, updateBytes.length, updateBytes);
                                }
                                // If writeData calls do not throw an exception, update has been successfully made.
                                positionsToStartUpdates = null;
                                bytesToUpdate = null;
                                set = true;
                            }
                        }  //end synchronization
                    }//end if (object instaceof AS400JDBCBlobLocator)
                    //@G5A End new code

                    //@G5A If the code for updateable lob locators did not run, then run old code.
                    if (!set)
                    {
          Blob blob = (Blob) object;                                      // @A1C
          int length = (int) blob.length ();
          locator_.writeData (0, length, blob.getBytes (1, length));      // @C4C Blobs are 1 based.
          set = true;                                                     // @A1A
        }                                                                   // @A1C
                }                                                                       // @G5A
            }                                                               // @A1C                                                                         // @A1C
      catch (NoClassDefFoundError e)
      {                                        // @A1C
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


  //@D1A JDBC 3.0
  public String getJavaClassName()
  {
    return "com.ibm.as400.access.AS400JDBCBlobLocator";    
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
    return new AS400JDBCInputStream ((JDLobLocator)locator_.clone());             // @D2c
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
    return new AS400JDBCInputStream ((JDLobLocator)locator_.clone());                 // @D2c
  }



  public Blob toBlob ()
  throws SQLException
  {
    return new AS400JDBCBlobLocator ((JDLobLocator)locator_.clone());                 // @D2c
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
    return new InputStreamReader (new AS400JDBCInputStream ((JDLobLocator)locator_.clone()));    // @D2c
  }



  public Clob toClob ()
  throws SQLException
  {
    return new AS400JDBCClobLocator ((JDLobLocator)locator_.clone(), connection_.converter_);    // @D2c @P0C
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
    return new AS400JDBCBlobLocator ((JDLobLocator)locator_.clone());             // @D2c
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
    return new AS400JDBCInputStream ((JDLobLocator)locator_.clone());              // @D2c
  }


}

