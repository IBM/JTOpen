///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLClobLocator.java
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
import java.io.IOException;                                 // @B3A
import java.io.Reader;
import java.io.UnsupportedEncodingException;                // @B3A
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




class SQLClobLocator
implements SQLLocator                                       // @B3C
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




  // Private data.
  private static final AS400Bin4  typeConverter_ = new AS400Bin4 ();

  private boolean                 graphic_;
  private AS400JDBCConnection     connection_;
  private ConvTable               converter_; //@P0C
  private int                     id_;
  private JDLobLocator            locator_;
  private int                     maxLength_;
  private SQLConversionSettings   settings_;
  private int                     truncated_;
  private int                     columnIndex_;   //@E3A



  SQLClobLocator (AS400JDBCConnection connection,
                  int id,
                  int maxLength, 
                  boolean graphic, 
                  SQLConversionSettings settings,
                  ConvTable converter,                                  // @E1A @P0C
                  int columnIndex)                //@E3A
  {
    connection_     = connection;
    graphic_        = graphic;
    id_             = id;
    locator_        = new JDLobLocator (connection, id, maxLength);             // @B3C
    locator_.setGraphic(graphic); // @E4A
    maxLength_      = maxLength;
    settings_       = settings;
    truncated_      = 0;

    // @E1D try {
    // @E1D     converter_      = graphic ? connection.getGraphicConverter ()
    // @E1D                               : connection.getConverter ();            
    // @E1D }
    // @E1D catch (SQLException e) {
    // @E1D     converter_  = null;
    // @E1D }

    converter_      = converter;                                                // @E1A
    columnIndex_    = columnIndex;     //@E3A
  }



// @E1D     SQLClobLocator (AS400JDBCConnection connection,
// @E1D                     int id,
// @E1D                     int maxLength, 
// @E1D                     SQLConversionSettings settings)                           
// @E1D     {
// @E1D         this (connection, id, maxLength, false, settings);             
// @E1D     }



  public Object clone ()
  {
    return new SQLClobLocator (connection_, id_, maxLength_, graphic_, settings_, converter_, columnIndex_);                // @E1C //@E3C
  }



  public void setHandle (int handle)                          // @B3A
  {                                                           // @B3A
    locator_.setHandle (handle);                            // @B3A
  }                                                           // @B3A



//---------------------------------------------------------//
//                                                         //
// CONVERSION TO AND FROM RAW BYTES                        //
//                                                         //
//---------------------------------------------------------//



  public void convertFromRawBytes (byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
  throws SQLException
  {
    int locatorHandle = ((Integer) typeConverter_.toObject (rawBytes, offset)).intValue ();        
    locator_.setHandle (locatorHandle);
    locator_.setColumnIndex (columnIndex_);  //@E3A
  }



  public void convertToRawBytes (byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
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
    boolean set = false;                                                            // @B2A

    if (object instanceof String)
    {                                                 // @B3A
      String string = (String) object;                                            // @B3A
      byte[] bytes = converter_.stringToByteArray (string);                       // @B3A
      locator_.writeData (0, string.length(), bytes);                             // @B3A @E2C
      set = true;                                                                 // @B3A
    }                                                                               // @B3A
    else
    {                                                                          // @B3A
      try
      {                                                                       // @B2A
        if (object instanceof Clob)
        {
                    //@G5A Start new code for updateable locator case
                    if (object instanceof AS400JDBCClobLocator)
                    {
                        AS400JDBCClobLocator clob = (AS400JDBCClobLocator) object;
                        synchronized (clob.getInternalLock())
                        {
                            Vector positionsToStartUpdates = clob.getPositionsToStartUpdates();
                            if (positionsToStartUpdates != null)
                            {
                                Vector stringsToUpdate = clob.getStringsToUpdate();
                                for (int i = 0; i < stringsToUpdate.size(); i++)
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
                    if (!set)
                    {
          Clob clob = (Clob) object;
          int length = (int) clob.length ();
          String substring = clob.getSubString (1, length);                   // @D1
          locator_.writeData (0, length, converter_.stringToByteArray (substring));
          set = true;                                                         // @B2A
        }
      }
            }
      catch (NoClassDefFoundError e)
      {                                            // @B2C
        // Ignore.  It just means we are running under JDK 1.1.                 // @B2C
      }                                                                           // @B2C        
    }                                                                               // @B3A

    if (! set)                                                                      // @B2C
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
    if (graphic_)
      return(maxLength_ / 2);
    else
      return maxLength_;
  }

  //@F1A JDBC 3.0
  public String getJavaClassName()
  {
    return "com.ibm.as400.access.AS400JDBCClobLocator";   
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
    return "CLOB"; 
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
    if (graphic_)
      return 968;
    else
      return 964;        
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
    return java.sql.Types.CLOB;
  }



  public String getTypeName ()
  {
    if (graphic_)
      return "DBCLOB";
    else
      return "CLOB";
  }



// @E1D     public boolean isGraphic ()
// @E1D     {
// @E1D         return graphic_;
// @E1D     }



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
    return new AS400JDBCInputStream ((JDLobLocator)locator_.clone(), converter_, "ISO8859_1"); // @D2c
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
    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }



  public Blob toBlob ()
  throws SQLException
  {
    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
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
    return 0;
  }



  public byte[] toBytes ()
  throws SQLException
  {
    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }



  public Reader toCharacterStream ()
  throws SQLException
  {
    try
    {                                                                    // @B3A
      //@E4D return new InputStreamReader (new AS400JDBCInputStream (locator_), converter_.getEncoding ()); // @B3C  
      return new ConvTableReader (new AS400JDBCInputStream ((JDLobLocator)locator_.clone()), converter_.getCcsid()); // @E4A @D2c
    }                                                                        // @B3A
    catch (UnsupportedEncodingException e)
    {                                 // @B3A
      JDError.throwSQLException (JDError.EXC_INTERNAL, e);                 // @B3A
      return null;                                                         // @B3A
    }                                                                        // @B3A
  }



  public Clob toClob ()
  throws SQLException
  {
    return new AS400JDBCClobLocator ((JDLobLocator)locator_.clone(), converter_);        
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
    return 0;
  }



  public float toFloat ()
  throws SQLException
  {
    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    return 0;
  }



  public int toInt ()
  throws SQLException
  {
    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    return 0;
  }



  public long toLong ()
  throws SQLException
  {
    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    return 0;
  }



  public Object toObject ()
  {
    return new AS400JDBCClobLocator ((JDLobLocator)locator_.clone(), converter_);
  }



  public short toShort ()
  throws SQLException
  {
    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    return 0;
  }



  public String toString ()
  {
    try
    {                                                                           // @C0A
      DBLobData data = locator_.retrieveData (0, locator_.getMaxLength());        // @C0A
      String value = converter_.byteArrayToString (data.getRawBytes (),           // @C0A
                                                   data.getOffset (),             // @C0A
                                                   data.getLength ());            // @C0A
      return value;                                                               // @C0A
    }                                                                               // @C0A
    catch (SQLException e)
    {                                                        // @C0A
      // toString() should not throw exceptions!                                  // @C0A
      return super.toString();
    }                                                                               // @C0A
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
    return new AS400JDBCInputStream ((JDLobLocator)locator_.clone(), converter_, "UnicodeBigUnmarked"); // @B1C @D2c
  }



}

