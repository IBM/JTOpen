///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDResultSetProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;


class JDResultSetProxy
extends AbstractProxyImpl
implements ResultSet
{
  private static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";


  // Private data.
 
  private JDConnectionProxy       jdConnection_;
         // The object that caused this object to be created.

  private JDStatementProxy        cachedStatement_;

  private final static String NOT_SERIALIZABLE = "Parameter is not serializable.";


/*---------------------------------------------------------*/
/*                                                         */
/* MISCELLANEOUS METHODS.                                  */
/*                                                         */
/*---------------------------------------------------------*/


  public JDResultSetProxy (JDConnectionProxy jdConnection)
  {
    jdConnection_ = jdConnection;
  }


  public JDResultSetProxy (JDConnectionProxy jdConnection,
                           JDStatementProxy statement)
  {
    jdConnection_ = jdConnection;
    cachedStatement_ = statement;
  }


  // Call a method.  No return value is expected.
  private void callMethod (String methodName)
    throws SQLException
  {
    try {
      connection_.callMethod (pxId_, methodName);
    }
    catch (InvocationTargetException e) {
      throw JDConnectionProxy.rethrow1 (e);
    }
  }

  private void callMethod (String methodName,
                             Class[] argClasses,
                             Object[] argValues)
    throws SQLException
  {
    try {
      connection_.callMethod (pxId_, methodName, argClasses, argValues);
    }
    catch (InvocationTargetException e) {
      throw JDConnectionProxy.rethrow1 (e);
    }
  }

  // Call a method, and return a boolean.
  private boolean callMethodRtnBool (String methodName)
    throws SQLException
  {
    try {
      return connection_.callMethodReturnsBoolean (pxId_, methodName);
    }
    catch (InvocationTargetException e) {
      throw JDConnectionProxy.rethrow1 (e);
    }
  }

  private JDInputStreamProxy callMethodRtnInpStrm (String methodName,
                                                   int argValue)
    throws SQLException
  {
    try {
      JDInputStreamProxy newStream = new JDInputStreamProxy ();
      return (JDInputStreamProxy) connection_.callFactoryMethod (
                                          pxId_, methodName,
                                          new Class[] { Integer.TYPE },
                                          new Object[] { new Integer (argValue) },
                                          newStream);
    }
    catch (InvocationTargetException e) {
      throw JDConnectionProxy.rethrow1 (e);
    }
  }

  // Call a method, and return an int.
  private int callMethodRtnInt (String methodName)
    throws SQLException
  {
    try {
      return connection_.callMethodReturnsInt (pxId_, methodName);
    }
    catch (InvocationTargetException e) {
      throw JDConnectionProxy.rethrow1 (e);
    }
  }

  // Call a method, and return an Object.
  private Object callMethodRtnObj (String methodName)
    throws SQLException
  {
    try {
      return connection_.callMethodReturnsObject (pxId_, methodName);
    }
    catch (InvocationTargetException e) {
      throw JDConnectionProxy.rethrow1 (e);
    }
  }

  private Object callMethodRtnObj (String methodName,
                             Class[] argClasses,
                             Object[] argValues)
    throws SQLException
  {
    try {
      return connection_.callMethod (pxId_, methodName,
                                          argClasses, argValues)
                             .getReturnValue();
    }
    catch (InvocationTargetException e) {
      throw JDConnectionProxy.rethrow1 (e);
    }
  }

  // Call a method, and return a 'raw' ProxyReturnValue.
  private ProxyReturnValue callMethodRtnRaw (String methodName,
                             Class[] argClasses,
                             Object[] argValues)
    throws SQLException
  {
    try {
      return connection_.callMethod (pxId_, methodName,
                                          argClasses, argValues);
    }
    catch (InvocationTargetException e) {
      throw JDConnectionProxy.rethrow1 (e);
    }
  }



    public void clearWarnings ()
    throws SQLException
    {
      callMethod ("clearWarnings");
    }



    public void close ()
    throws SQLException
    {
      callMethod ("close");
    }



    public int findColumn (String columnName)
    throws SQLException
    {
      return callMethodRtnRaw ("findColumn",
                               new Class[] { String.class },
                               new Object[] { columnName })
                 .getReturnValueInt();
    }



// JDBC 2.0
    public int getConcurrency ()
    throws SQLException
    {
      return callMethodRtnInt ("getConcurrency");
    }


    public String getCursorName ()
    throws SQLException
    {
      return (String) callMethodRtnObj ("getCursorName");
    }



// JDBC 2.0
    public int getFetchDirection ()
    throws SQLException
    {
      return callMethodRtnInt ("getFetchDirection");
    }



// JDBC 2.0
    public int getFetchSize ()
    throws SQLException
    {
      return callMethodRtnInt ("getFetchSize");
    }



// JDBC 2.0
    public Statement getStatement ()
    throws SQLException
    {
      if (cachedStatement_ == null)
      {
        try {
          JDStatementProxy newStatement = new JDStatementProxy (jdConnection_);
          cachedStatement_ = (JDStatementProxy) connection_.callFactoryMethod (
                                     pxId_, "getStatement", newStatement);
        }
        catch (InvocationTargetException e) {
          throw JDConnectionProxy.rethrow1 (e);
        }
      }
      return cachedStatement_;
    }



// JDBC 2.0
    public int getType ()
    throws SQLException
    {
      return callMethodRtnInt ("getType");
    }



    public SQLWarning getWarnings ()
    throws SQLException
    {
      return (SQLWarning) callMethodRtnObj ("getWarnings");
    }



// JDBC 2.0
    public void setFetchDirection (int fetchDirection)
    throws SQLException
    {
      callMethod ("setFetchDirection",
                  new Class[] { Integer.TYPE },
                  new Object[] { new Integer (fetchDirection) });
    }



// JDBC 2.0
    public void setFetchSize (int fetchSize)
    throws SQLException
    {
      callMethod ("setFetchSize",
                  new Class[] { Integer.TYPE },
                  new Object[] { new Integer (fetchSize) });
    }


    // This method is not required by java.sql.ResultSet,
    // but it is used by the JDBC testcases, and is implemented
    // in the public class.
    public String toString ()
    {
      try {
        return (String) connection_.callMethodReturnsObject (pxId_, "toString");
      }
      catch (InvocationTargetException e) {
        throw ProxyClientConnection.rethrow (e);
      }
    }



/*---------------------------------------------------------*/
/*                                                         */
/* CURSOR POSITIONING.                                     */
/*                                                         */
/*---------------------------------------------------------*/



// JDBC 2.0
    public boolean absolute (int rowNumber)
    throws SQLException
    {
      return callMethodRtnRaw ("absolute",
                               new Class[] { Integer.TYPE },
                               new Object[] { new Integer (rowNumber) })
                 .getReturnValueBoolean();
    }



// JDBC 2.0
    public void afterLast ()
    throws SQLException
    {
      callMethod ("afterLast");
    }



// JDBC 2.0
    public void beforeFirst ()
    throws SQLException
    {
      callMethod ("beforeFirst");
    }



// JDBC 2.0
    public boolean first ()
    throws SQLException
    {
      return callMethodRtnBool ("first");
    }



// JDBC 2.0
    public int getRow ()
    throws SQLException
    {
      return callMethodRtnInt ("getRow");
    }



// JDBC 2.0
    public boolean isAfterLast ()
    throws SQLException
    {
      return callMethodRtnBool ("isAfterLast");
    }



// JDBC 2.0
    public boolean isBeforeFirst ()
    throws SQLException
    {
      return callMethodRtnBool ("isBeforeFirst");
    }



// JDBC 2.0
    public boolean isFirst ()
    throws SQLException
    {
      return callMethodRtnBool ("isFirst");
    }



// JDBC 2.0
    public boolean isLast ()
    throws SQLException
    {
      return callMethodRtnBool ("isLast");
    }



// JDBC 2.0
    public boolean last ()
    throws SQLException
    {
      return callMethodRtnBool ("last");
    }



// JDBC 2.0
    public void moveToCurrentRow ()
    throws SQLException
    {
      callMethod ("moveToCurrentRow");
    }



// JDBC 2.0
    public void moveToInsertRow ()
    throws SQLException
    {
      callMethod ("moveToInsertRow");
    }



    public boolean next ()
    throws SQLException
    {
      return callMethodRtnBool ("next");
    }



// JDBC 2.0
    public boolean previous ()
    throws SQLException
    {
      return callMethodRtnBool ("previous");
    }


// JDBC 2.0
    public void refreshRow ()
    throws SQLException
    {
      callMethod ("refreshRow");
    }



// JDBC 2.0
    public boolean relative (int rowNumber)
    throws SQLException
    {
      return callMethodRtnRaw ("relative",
                               new Class[] { Integer.TYPE },
                               new Object[] { new Integer (rowNumber) })
                 .getReturnValueBoolean();
    }



/*---------------------------------------------------------*/
/*                                                         */
/* GET DATA METHODS.                                       */
/*                                                         */
/*---------------------------------------------------------*/



// JDBC 2.0
    public Array getArray (int columnIndex)
    throws SQLException
    {
      return (Array) callMethodRtnObj ("getArray",
                  new Class[] { Integer.TYPE },
                  new Object[] { new Integer (columnIndex) });
    }



// JDBC 2.0
    public Array getArray (String columnName)
    throws SQLException
    {
      return getArray (findColumn (columnName));
    }



    public InputStream getAsciiStream (int columnIndex)
    throws SQLException
    {
      return callMethodRtnInpStrm ("getAsciiStream", columnIndex);
    }



    public InputStream getAsciiStream (String columnName)
    throws SQLException
    {
      return getAsciiStream (findColumn (columnName));
    }



// JDBC 2.0
    public BigDecimal getBigDecimal (int columnIndex)
    throws SQLException
    {
      return (BigDecimal) callMethodRtnObj ("getBigDecimal",
                  new Class[] { Integer.TYPE },
                  new Object[] { new Integer (columnIndex) });
    }



// JDBC 2.0
    public BigDecimal getBigDecimal (String columnName)
    throws SQLException
    {
      return getBigDecimal (findColumn (columnName));
    }



/**
@exception  SQLException    If a SQL error occurs.
@deprecated Use getBigDecimal(int) instead.
@see #getBigDecimal(int)
**/
    public BigDecimal getBigDecimal (int columnIndex, int scale)
    throws SQLException
    {
      return (BigDecimal) callMethodRtnObj ("getBigDecimal",
                  new Class[] { Integer.TYPE, Integer.TYPE },
                  new Object[] { new Integer (columnIndex),
                                 new Integer (scale) });
    }



/**
@exception  SQLException    If a SQL error occurs.
@deprecated Use getBigDecimal(String) instead.
@see #getBigDecimal(String)
**/
    public BigDecimal getBigDecimal (String columnName, int scale)
    throws SQLException
    {
      return getBigDecimal (findColumn (columnName), scale);
    }



    public InputStream getBinaryStream (int columnIndex)
    throws SQLException
    {
      return callMethodRtnInpStrm ("getBinaryStream", columnIndex);
    }



    public InputStream getBinaryStream (String columnName)
    throws SQLException
    {
        return getBinaryStream (findColumn (columnName));
    }



// JDBC 2.0
    public Blob getBlob (int columnIndex)
    throws SQLException
    {
      try {
        JDBlobProxy newBlob = new JDBlobProxy ();
        return (JDBlobProxy) connection_.callFactoryMethod (pxId_,
                             "getBlob",
                             new Class[] { Integer.TYPE },
                             new Object[] { new Integer(columnIndex) },
                             newBlob);
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
    }



// JDBC 2.0
    public Blob getBlob (String columnName)
    throws SQLException
    {
        return getBlob (findColumn (columnName));
    }



    public boolean getBoolean (int columnIndex)
    throws SQLException
    {
      return callMethodRtnRaw ("getBoolean",
                               new Class[] { Integer.TYPE },
                               new Object[] { new Integer (columnIndex) })
                 .getReturnValueBoolean();
    }



    public boolean getBoolean (String columnName)
    throws SQLException
    {
        return getBoolean (findColumn (columnName));
    }



    public byte getByte (int columnIndex)
    throws SQLException
    {
      return callMethodRtnRaw ("getByte",
                               new Class[] { Integer.TYPE },
                               new Object[] { new Integer (columnIndex) })
                 .getReturnValueByte();
    }



    public byte getByte (String columnName)
    throws SQLException
    {
        return getByte (findColumn (columnName));
    }



    public byte[] getBytes (int columnIndex)
    throws SQLException
    {
      return (byte[]) callMethodRtnObj ("getBytes",
                  new Class[] { Integer.TYPE },
                  new Object[] { new Integer (columnIndex) });
    }



    public byte[] getBytes (String columnName)
    throws SQLException
    {
        return getBytes (findColumn (columnName));
    }



// JDBC 2.0
    public Reader getCharacterStream (int columnIndex)
    throws SQLException
    {
      try {
        JDReaderProxy newReader = new JDReaderProxy ();
        return (JDReaderProxy) connection_.callFactoryMethod (
                                     pxId_, "getCharacterStream",
                                     new Class[] { Integer.TYPE },
                                     new Object[] { new Integer (columnIndex) },
                                     newReader);
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
    }



// JDBC 2.0
    public Reader getCharacterStream (String columnName)
    throws SQLException
    {
        return getCharacterStream (findColumn (columnName));
    }



// JDBC 2.0
    public Clob getClob (int columnIndex)
    throws SQLException
    {
      try {
        JDClobProxy newClob = new JDClobProxy ();
        return (JDClobProxy) connection_.callFactoryMethod (pxId_,
                             "getClob",
                             new Class[] { Integer.TYPE },
                             new Object[] { new Integer(columnIndex) },
                             newClob);
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
    }



// JDBC 2.0
    public Clob getClob (String columnName)
    throws SQLException
    {
        return getClob (findColumn (columnName));
    }



    public Date getDate (int columnIndex)
    throws SQLException
    {
        return (Date) callMethodRtnObj ("getDate",
                                        new Class[] { Integer.TYPE },
                                        new Object[] { new Integer (columnIndex) });
    }



    public Date getDate (String columnName)
    throws SQLException
    {
        return (Date) callMethodRtnObj ("getDate",
                                        new Class[] { String.class },
                                        new Object[] { columnName });
    }



// JDBC 2.0
    public Date getDate (int columnIndex, Calendar calendar)
    throws SQLException
    {
      return (Date) callMethodRtnObj ("getDate",
                  new Class[] { Integer.TYPE, Calendar.class },
                  new Object[] { new Integer (columnIndex),
                                 calendar });
    }



// JDBC 2.0
    public Date getDate (String columnName, Calendar calendar)
    throws SQLException
    {
        return getDate (findColumn (columnName), calendar);
    }



    public double getDouble (int columnIndex)
    throws SQLException
    {
      return callMethodRtnRaw ("getDouble",
                               new Class[] { Integer.TYPE },
                               new Object[] { new Integer (columnIndex) })
                 .getReturnValueDouble();
    }



    public double getDouble (String columnName)
    throws SQLException
    {
        return getDouble (findColumn (columnName));
    }



    public float getFloat (int columnIndex)
    throws SQLException
    {
      return callMethodRtnRaw ("getFloat",
                               new Class[] { Integer.TYPE },
                               new Object[] { new Integer (columnIndex) })
                 .getReturnValueFloat();
    }



    public float getFloat (String columnName)
    throws SQLException
    {
        return getFloat (findColumn (columnName));
    }



    public int getInt (int columnIndex)
    throws SQLException
    {
      return callMethodRtnRaw ("getInt",
                               new Class[] { Integer.TYPE },
                               new Object[] { new Integer (columnIndex) })
                 .getReturnValueInt();
    }



    public int getInt (String columnName)
    throws SQLException
    {
        return getInt (findColumn (columnName));
    }



    public long getLong (int columnIndex)
    throws SQLException
    {
      return callMethodRtnRaw ("getLong",
                               new Class[] { Integer.TYPE },
                               new Object[] { new Integer (columnIndex) })
                 .getReturnValueLong();
    }



    public long getLong (String columnName)
    throws SQLException
    {
        return getLong (findColumn (columnName));
    }



    public ResultSetMetaData getMetaData ()
    throws SQLException
    {
      try {
        JDResultSetMetaDataProxy newMetaData = new JDResultSetMetaDataProxy (jdConnection_);
        return (JDResultSetMetaDataProxy) connection_.callFactoryMethod (
                                      pxId_, "getMetaData", newMetaData);
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
    }



    public Object getObject (int columnIndex)
    throws SQLException
    {
      String typeName = getMetaData().getColumnTypeName(columnIndex);
      ProxyFactoryImpl proxyObject = null;
      try
      {
        if (typeName.equalsIgnoreCase("BLOB")) {
          proxyObject = new JDBlobProxy ();
        }
        else if (typeName.equalsIgnoreCase("CLOB")) {
          proxyObject = new JDClobProxy ();
        }
        else
          return callMethodRtnObj ("getObject",
                                   new Class[] { Integer.TYPE },
                                   new Object[] { new Integer (columnIndex) });

        return connection_.callFactoryMethod (pxId_, "getObject",
                                   new Class[] { Integer.TYPE },
                                   new Object[] { new Integer (columnIndex) },
                                   proxyObject);
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
    }



    public Object getObject (String columnName)
    throws SQLException
    {
        return getObject (findColumn (columnName));
    }



// JDBC 2.0
    public Object getObject (int columnIndex, Map typeMap)
    throws SQLException
    {
      String typeName = getMetaData().getColumnTypeName(columnIndex);
      ProxyFactoryImpl proxyObject = null;
      try
      {
        if (typeName.equalsIgnoreCase("BLOB")) {
          proxyObject = new JDBlobProxy ();
        }
        else if (typeName.equalsIgnoreCase("CLOB")) {
          proxyObject = new JDClobProxy ();
        }
        else
          return callMethodRtnObj ("getObject",
                                   new Class[] { Integer.TYPE, Map.class },
                                   new Object[] { new Integer (columnIndex),
                                                  typeMap });

        return connection_.callFactoryMethod (pxId_, "getObject",
                                   new Class[] { Integer.TYPE, Map.class },
                                   new Object[] { new Integer (columnIndex),
                                                  typeMap },
                                   proxyObject);
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
    }



// JDBC 2.0
    public Object getObject (String columnName, Map typeMap)
    throws SQLException
    {
        return getObject (findColumn (columnName), typeMap);
    }



// JDBC 2.0
    public Ref getRef (int columnIndex)
    throws SQLException
    {
      return (Ref) callMethodRtnObj ("getRef",
                  new Class[] { Integer.TYPE},
                  new Object[] { new Integer (columnIndex) });
    }



// JDBC 2.0
    public Ref getRef (String columnName)
    throws SQLException
    {
        return getRef (findColumn (columnName));
    }



    public short getShort (int columnIndex)
    throws SQLException
    {
      return callMethodRtnRaw ("getShort",
                               new Class[] { Integer.TYPE },
                               new Object[] { new Integer (columnIndex) })
                 .getReturnValueShort();
    }



    public short getShort (String columnName)
    throws SQLException
    {
        return getShort (findColumn (columnName));
    }



    public String getString (int columnIndex)
    throws SQLException
    {
      return (String) callMethodRtnObj ("getString",
                  new Class[] { Integer.TYPE},
                  new Object[] { new Integer (columnIndex) });
    }



    public String getString (String columnName)
    throws SQLException
    {
        return getString (findColumn (columnName));
    }



    public Time getTime (int columnIndex)
    throws SQLException
    {
        return (Time) callMethodRtnObj ("getTime",
                                        new Class[] { Integer.TYPE },
                                        new Object[] { new Integer (columnIndex) });
    }



    public Time getTime (String columnName)
    throws SQLException
    {
        return getTime (findColumn (columnName));
    }



// JDBC 2.0
    public Time getTime (int columnIndex, Calendar calendar)
    throws SQLException
    {
      return (Time) callMethodRtnObj ("getTime",
                  new Class[] { Integer.TYPE, Calendar.class },
                  new Object[] { new Integer (columnIndex),
                                 calendar });
    }



// JDBC 2.0
    public Time getTime (String columnName, Calendar calendar)
    throws SQLException
    {
        return getTime (findColumn (columnName), calendar);
    }



    public Timestamp getTimestamp (int columnIndex)
    throws SQLException
    {
        return (Timestamp) callMethodRtnObj ("getTimestamp",
                                             new Class[] { Integer.TYPE },
                                             new Object[] { new Integer (columnIndex) });
    }



    public Timestamp getTimestamp (String columnName)
    throws SQLException
    {
        return getTimestamp (findColumn (columnName));
    }



// JDBC 2.0
    public Timestamp getTimestamp (int columnIndex, Calendar calendar)
    throws SQLException
    {
      return (Timestamp) callMethodRtnObj ("getTimestamp",
                  new Class[] { Integer.TYPE, Calendar.class },
                  new Object[] { new Integer (columnIndex),
                                 calendar });
    }



// JDBC 2.0
    public Timestamp getTimestamp (String columnName, Calendar calendar)
    throws SQLException
    {
        return getTimestamp (findColumn (columnName), calendar);
    }



/**
@exception  SQLException    If a SQL error occurs.
@deprecated Use getCharacterStream(int) instead.
@see #getCharacterStream(int)
**/
    public InputStream getUnicodeStream (int columnIndex)
    throws SQLException
    {
      return callMethodRtnInpStrm ("getUnicodeStream", columnIndex);
    }



/**
@exception  SQLException    If a SQL error occurs.
@deprecated Use getCharacterStream(String) instead.
@see #getCharacterStream(String)
**/
    public InputStream getUnicodeStream (String columnName)
    throws SQLException
    {
        return getUnicodeStream (findColumn (columnName));
    }



// JDBC 3.0
    public URL getURL (int columnIndex)
    throws SQLException
    {
        return(URL) callMethodRtnObj ("getURL",
                                      new Class[] { Integer.TYPE},
                                      new Object[] { new Integer(columnIndex)});
    }


    
// JDBC 3.0
    public URL getURL (String columnName)
    throws SQLException
    {
        return(URL) callMethodRtnObj ("getURL",
                                      new Class[] { String.class},
                                      new Object[] { columnName});
    }



    public boolean wasNull ()
    throws SQLException
    {
      return callMethodRtnBool ("wasNull");
    }



/*---------------------------------------------------------*/
/*                                                         */
/* UPDATE DATA METHODS.                                    */
/*                                                         */
/*---------------------------------------------------------*/



// JDBC 2.0
    public void cancelRowUpdates ()
    throws SQLException
    {
      callMethod ("cancelRowUpdates");
    }



// JDBC 2.0
    public void deleteRow ()
    throws SQLException
    {
      callMethod ("deleteRow");
    }



// JDBC 2.0
    public void insertRow ()
    throws SQLException
    {
      callMethod ("insertRow");
    }



// JDBC 2.0
    public boolean rowDeleted ()
    throws SQLException
    {
      return callMethodRtnBool ("rowDeleted");
    }



// JDBC 2.0
    public boolean rowInserted ()
    throws SQLException
    {
      return callMethodRtnBool ("rowInserted");
    }



// JDBC 2.0
    public boolean rowUpdated ()
    throws SQLException
    {
      return callMethodRtnBool ("rowUpdated");
    }



// JDBC 3.0
    public void updateArray (int columnIndex, Array columnValue)
        throws SQLException
    {
      if (columnValue != null &&
          !(columnValue instanceof Serializable) ){
        if (JDTrace.isTraceOn())
          JDTrace.logInformation (this, NOT_SERIALIZABLE);
        throw new SQLException ();
      }

      callMethod ("updateArray",
                  new Class[] { Integer.TYPE, Array.class },
                  new Object[] { new Integer(columnIndex),
                                 columnValue });
    }



// JDBC 3.0
    public void updateArray (String columnName, Array columnValue)
        throws SQLException
    {
      if (columnValue != null &&
          !(columnValue instanceof Serializable) ){
        if (JDTrace.isTraceOn())
          JDTrace.logInformation (this, NOT_SERIALIZABLE);
        throw new SQLException ();
      }

      callMethod ("updateArray",
                  new Class[] { String.class, Array.class },
                  new Object[] { columnName,
                                 columnValue });
    }





// JDBC 2.0
    public void updateAsciiStream (int columnIndex,
                                   InputStream columnValue,
                                   int length)
    throws SQLException
    {
      InputStream iStream;
      if (columnValue == null ||
          columnValue instanceof Serializable)
        iStream = columnValue;
      else {
        try {
          iStream = new SerializableInputStream (columnValue);
        }
        catch (java.io.IOException e) {
          throw new SQLException (e.getMessage ());
        }
      }
      callMethod ("updateAsciiStream",
                  new Class[] { Integer.TYPE, InputStream.class,
                                Integer.TYPE },
                  new Object[] { new Integer (columnIndex),
                                 iStream,
                                 new Integer (length) });
    }



// JDBC 2.0
    public void updateAsciiStream (String columnName,
                                   InputStream columnValue,
                                   int length)
    throws SQLException
    {
        updateAsciiStream (findColumn (columnName), columnValue, length);
    }



// JDBC 2.0
    public void updateBigDecimal (int columnIndex, BigDecimal columnValue)
    throws SQLException
    {
      callMethod ("updateBigDecimal",
                  new Class[] { Integer.TYPE, BigDecimal.class },
                  new Object[] { new Integer (columnIndex),
                                 columnValue });
    }



// JDBC 2.0
    public void updateBigDecimal (String columnName, BigDecimal columnValue)
    throws SQLException
    {
        updateBigDecimal (findColumn (columnName), columnValue);
    }



// JDBC 2.0
    public void updateBinaryStream (int columnIndex,
                                    InputStream columnValue,
                                    int length)
    throws SQLException
    {
      InputStream iStream;
      if (columnValue == null ||
          columnValue instanceof Serializable)
        iStream = columnValue;
      else {
        try {
          iStream = new SerializableInputStream (columnValue);
        }
        catch (java.io.IOException e) {
          throw new SQLException (e.getMessage ());
        }
      }
      callMethod ("updateBinaryStream",
                  new Class[] { Integer.TYPE, InputStream.class,
                                Integer.TYPE },
                  new Object[] { new Integer (columnIndex),
                                 iStream,
                                 new Integer (length) });
    }



// JDBC 2.0
    public void updateBinaryStream (String columnName,
                                    InputStream columnValue,
                                    int length)
    throws SQLException
    {
        updateBinaryStream (findColumn (columnName), columnValue, length);
    }



// JDBC 3.0
    public void updateBlob (int columnIndex, Blob columnValue)
        throws SQLException
    {
      callMethod ("updateBlob",
                  new Class[] { Integer.TYPE, Blob.class },
                  new Object[] { new Integer (columnIndex),
                                 columnValue });
    }



// JDBC 3.0
    public void updateBlob (String columnName, Blob columnValue)
        throws SQLException
    {
      callMethod ("updateBlob",
                  new Class[] { String.class, Blob.class },
                  new Object[] { columnName,
                                 columnValue });
    }



// JDBC 2.0
    public void updateBoolean (int columnIndex, boolean columnValue)
    throws SQLException
    {
      callMethod ("updateBoolean",
                  new Class[] { Integer.TYPE, Boolean.TYPE },
                  new Object[] { new Integer (columnIndex),
                                 new Boolean (columnValue) });
    }



// JDBC 2.0
    public void updateBoolean (String columnName, boolean columnValue)
    throws SQLException
    {
        updateBoolean (findColumn (columnName), columnValue);
    }



// JDBC 2.0
    public void updateByte (int columnIndex, byte columnValue)
    throws SQLException
    {
      callMethod ("updateByte",
                  new Class[] { Integer.TYPE, Byte.TYPE },
                  new Object[] { new Integer (columnIndex),
                                 new Byte (columnValue) });
    }



// JDBC 2.0
    public void updateByte (String columnName, byte columnValue)
    throws SQLException
    {
        updateByte (findColumn (columnName), columnValue);
    }



// JDBC 2.0
    public void updateBytes (int columnIndex, byte[] columnValue)
    throws SQLException
    {
      callMethod ("updateBytes",
                  new Class[] { Integer.TYPE, byte[].class },
                  new Object[] { new Integer (columnIndex),
                                 columnValue });
    }



// JDBC 2.0
    public void updateBytes (String columnName, byte[] columnValue)
    throws SQLException
    {
        updateBytes (findColumn (columnName), columnValue);
    }



// JDBC 2.0
    public void updateCharacterStream (int columnIndex,
                                       Reader columnValue,
                                       int length)
    throws SQLException
    {
      try {
        SerializableReader reader;
        if (columnValue == null)
          reader = null;
        else
          reader = new SerializableReader (columnValue, Math.max(0,length));
        callMethod ("updateCharacterStream",
                    new Class[] { Integer.TYPE, Reader.class, Integer.TYPE },
                    new Object[] { new Integer (columnIndex),
                                   reader, new Integer (length) });
      }
      catch (java.io.IOException e) {
        throw new SQLException (e.getMessage ());
      }
    }



// JDBC 2.0
    public void updateCharacterStream (String columnName,
                                       Reader columnValue,
                                       int length)
    throws SQLException
    {
        updateCharacterStream (findColumn (columnName), columnValue, length);
    }



// JDBC 3.0
    public void updateClob (int columnIndex, Clob columnValue)
        throws SQLException
    {
      callMethod ("updateClob",
                  new Class[] { String.class, Clob.class },
                  new Object[] { new Integer (columnIndex),
                                 columnValue });
    }



// JDBC 3.0
    public void updateClob (String columnName, Clob columnValue)
        throws SQLException
    {
      callMethod ("updateClob",
                  new Class[] { String.class, Clob.class },
                  new Object[] { columnName,
                                 columnValue });
    }



// JDBC 2.0
    public void updateDate (int columnIndex, Date columnValue)
    throws SQLException
    {
      callMethod ("updateDate",
                  new Class[] { Integer.TYPE, Date.class },
                  new Object[] { new Integer (columnIndex),
                                 columnValue });
    }



// JDBC 2.0
    public void updateDate (String columnName, Date columnValue)
    throws SQLException
    {
        updateDate (findColumn (columnName), columnValue);
    }



// JDBC 2.0
    public void updateDouble (int columnIndex, double columnValue)
    throws SQLException
    {
      callMethod ("updateDouble",
                  new Class[] { Integer.TYPE, Double.TYPE },
                  new Object[] { new Integer (columnIndex),
                                 new Double (columnValue) });
    }



// JDBC 2.0
    public void updateDouble (String columnName, double columnValue)
    throws SQLException
    {
        updateDouble (findColumn (columnName), columnValue);
    }



// JDBC 2.0
    public void updateFloat (int columnIndex, float columnValue)
    throws SQLException
    {
      callMethod ("updateFloat",
                  new Class[] { Integer.TYPE, Float.TYPE },
                  new Object[] { new Integer (columnIndex),
                                 new Float (columnValue) });
    }



// JDBC 2.0
    public void updateFloat (String columnName, float columnValue)
    throws SQLException
    {
        updateFloat (findColumn (columnName), columnValue);
    }



// JDBC 2.0
    public void updateInt (int columnIndex, int columnValue)
    throws SQLException
    {
      callMethod ("updateInt",
                  new Class[] { Integer.TYPE, Integer.TYPE },
                  new Object[] { new Integer (columnIndex),
                                 new Integer (columnValue) });
    }



// JDBC 2.0
    public void updateInt (String columnName, int columnValue)
    throws SQLException
    {
        updateInt (findColumn (columnName), columnValue);
    }



// JDBC 2.0
    public void updateLong (int columnIndex, long columnValue)
    throws SQLException
    {
      callMethod ("updateLong",
                  new Class[] { Integer.TYPE, Long.TYPE },
                  new Object[] { new Integer (columnIndex),
                                 new Long (columnValue) });
    }



// JDBC 2.0
    public void updateLong (String columnName, long columnValue)
    throws SQLException
    {
        updateLong (findColumn (columnName), columnValue);
    }



// JDBC 2.0
    public void updateNull (int columnIndex)
    throws SQLException
    {
      callMethod ("updateNull",
                  new Class[] { Integer.TYPE },
                  new Object[] { new Integer (columnIndex) });
    }



// JDBC 2.0
    public void updateNull (String columnName)
    throws SQLException
    {
        updateNull (findColumn (columnName));
    }



// JDBC 2.0
    public void updateObject (int columnIndex, Object columnValue)
    throws SQLException
    {
      if (columnValue != null &&
          !(columnValue instanceof Serializable)) {
        if (JDTrace.isTraceOn())
          JDTrace.logInformation (this, NOT_SERIALIZABLE);
        throw new SQLException ();
      }

      callMethod ("updateObject",
                  new Class[] { Integer.TYPE, Object.class },
                  new Object[] { new Integer (columnIndex),
                                 columnValue });
    }



// JDBC 2.0
    public void updateObject (String columnName, Object columnValue)
    throws SQLException
    {
        updateObject (findColumn (columnName), columnValue);
    }



// JDBC 2.0
    public void updateObject (int columnIndex,
                              Object columnValue,
                              int scale)
    throws SQLException
    {
      if (columnValue != null &&
          !(columnValue instanceof Serializable)) {
        if (JDTrace.isTraceOn())
          JDTrace.logInformation (this, NOT_SERIALIZABLE);
        throw new SQLException ();
      }

      callMethod ("updateObject",
                  new Class[] { Integer.TYPE, Object.class, Integer.TYPE },
                  new Object[] { new Integer (columnIndex),
                                 columnValue,
                                 new Integer (scale) });
    }



// JDBC 2.0
    public void updateObject (String columnName,
                              Object columnValue,
                              int scale)
    throws SQLException
    {
        updateObject (findColumn (columnName), columnValue, scale);
    }



// JDBC 3.0
    public void updateRef (int columnIndex, Ref columnValue)
        throws SQLException
    {
      if (columnValue != null &&
          !(columnValue instanceof Serializable) ){
        if (JDTrace.isTraceOn())
          JDTrace.logInformation (this, NOT_SERIALIZABLE);
        throw new SQLException ();
      }

      callMethod ("updateRef",
                  new Class[] { Integer.TYPE, Ref.class },
                  new Object[] { new Integer (columnIndex),
                                 columnValue });
    }



// JDBC 3.0
    public void updateRef (String columnName, Ref columnValue)
        throws SQLException
    {
      if (columnValue != null &&
          !(columnValue instanceof Serializable) ){
        if (JDTrace.isTraceOn())
          JDTrace.logInformation (this, NOT_SERIALIZABLE);
        throw new SQLException ();
      }

      callMethod ("updateRef",
                  new Class[] { String.class, Ref.class },
                  new Object[] { columnName,
                                 columnValue });
    }



// JDBC 2.0
    public void updateRow ()
    throws SQLException
    {
      callMethod ("updateRow");
    }



// JDBC 2.0
    public void updateShort (int columnIndex, short columnValue)
    throws SQLException
    {
      callMethod ("updateShort",
                  new Class[] { Integer.TYPE, Short.TYPE },
                  new Object[] { new Integer (columnIndex),
                                 new Short (columnValue) });
    }



// JDBC 2.0
    public void updateShort (String columnName, short columnValue)
    throws SQLException
    {
        updateShort (findColumn (columnName), columnValue);
    }



// JDBC 2.0
    public void updateString (int columnIndex, String columnValue)
    throws SQLException
    {
      callMethod ("updateString",
                  new Class[] { Integer.TYPE, String.class },
                  new Object[] { new Integer (columnIndex),
                                 columnValue });
    }



// JDBC 2.0
    public void updateString (String columnName, String columnValue)
    throws SQLException
    {
        updateString (findColumn (columnName), columnValue);
    }



// JDBC 2.0
    public void updateTime (int columnIndex, Time columnValue)
    throws SQLException
    {
      callMethod ("updateTime",
                  new Class[] { Integer.TYPE, Time.class },
                  new Object[] { new Integer (columnIndex),
                                 columnValue });
    }



// JDBC 2.0
    public void updateTime (String columnName, Time columnValue)
    throws SQLException
    {
        updateTime (findColumn (columnName), columnValue);
    }



// JDBC 2.0
    public void updateTimestamp (int columnIndex, Timestamp columnValue)
    throws SQLException
    {
      callMethod ("updateTimestamp",
                  new Class[] { Integer.TYPE, Timestamp.class },
                  new Object[] { new Integer (columnIndex),
                                 columnValue });
    }



// JDBC 2.0
    public void updateTimestamp (String columnName, Timestamp columnValue)
    throws SQLException
    {
        updateTimestamp (findColumn (columnName), columnValue);
    }
                                                                     
    
    //@PDA jdbc40
    public int getHoldability() throws SQLException
    {
        return callMethodRtnInt ("getHoldability");
    }
    
    
    //@pda jdbc40
    public Reader getNCharacterStream(int columnIndex) throws SQLException
    {
        try {
            JDReaderProxy newReader = new JDReaderProxy ();
            return (JDReaderProxy) connection_.callFactoryMethod (
                    pxId_, "getNCharacterStream",
                    new Class[] { Integer.TYPE },
                    new Object[] { new Integer (columnIndex) },
                    newReader);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    
    //@pda jdbc40
    /**
     * Retrieves the value of the designated column in the current row 
     * of this <code>ResultSet</code> object as a
     * <code>java.io.Reader</code> object.
     * It is intended for use when
     * accessing  <code>NCHAR</code>,<code>NVARCHAR</code>
     * and <code>LONGNVARCHAR</code> columns.
     * 
     * @param columnName the name of the column
     * @return a <code>java.io.Reader</code> object that contains the column
     * value; if the value is SQL <code>NULL</code>, the value returned is
     * <code>null</code> in the Java programming language
     * @exception SQLException if a database access error occurs
     */
    public Reader getNCharacterStream(String columnName) throws SQLException
    {
        return getNCharacterStream (findColumn (columnName));
    }
    
    
    //@pda jdbc40
    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>NClob</code> object
     * in the Java programming language.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a <code>NClob</code> object representing the SQL 
     *         <code>NCLOB</code> value in the specified column
     * @exception SQLException if the driver does not support national
     *         character sets;  if the driver can detect that a data conversion
     *  error could occur; or if a database access error occurss
     */
    public NClob getNClob(int columnIndex) throws SQLException
    {
        try {
            JDNClobProxy newClob = new JDNClobProxy ();
            return (JDNClobProxy) connection_.callFactoryMethod (pxId_,
                    "getNClob",
                    new Class[] { Integer.TYPE },
                    new Object[] { new Integer(columnIndex) },
                    newClob);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    
    //@pda jdbc40
    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>NClob</code> object
     * in the Java programming language.
     *
     * @param columnName the name of the column from which to retrieve the value
     * @return a <code>NClob</code> object representing the SQL <code>NCLOB</code>
     * value in the specified column
     * @exception SQLException if the driver does not support national
     *         character sets;  if the driver can detect that a data conversion
     *  error could occur; or if a database access error occurs
     */
    public NClob getNClob(String columnName) throws SQLException
    {
        return getNClob (findColumn (columnName));
    }
    
    
    //@pda jdbc40
    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>String</code> in the Java programming language.
     * It is intended for use when
     * accessing  <code>NCHAR</code>,<code>NVARCHAR</code>
     * and <code>LONGNVARCHAR</code> columns.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs 
     */
    public String getNString(int columnIndex) throws SQLException
    {
        return (String) callMethodRtnObj ("getNString",
                new Class[] { Integer.TYPE},
                new Object[] { new Integer (columnIndex) });
    }
    
    
    //@pda jdbc40
    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>String</code> in the Java programming language.
     * It is intended for use when
     * accessing  <code>NCHAR</code>,<code>NVARCHAR</code>
     * and <code>LONGNVARCHAR</code> columns.
     *
     * @param columnName the SQL name of the column
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public String getNString(String columnName) throws SQLException
    {
        return getNString (findColumn (columnName));
    }
    
    
    //@pda jdbc40
    public RowId getRowId(int columnIndex) throws SQLException
    {
        try {
            JDRowIdProxy newClob = new JDRowIdProxy ();
            return (JDRowIdProxy) connection_.callFactoryMethod (pxId_,
                    "getRowId",
                    new Class[] { Integer.TYPE },
                    new Object[] { new Integer(columnIndex) },
                    newClob);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    
    //@pda jdbc40
    public RowId getRowId(String columnName) throws SQLException
    {
        return getRowId(findColumn (columnName));
    }
    
    //@pda jdbc40
    public SQLXML getSQLXML(int columnIndex) throws SQLException
    {
        try {
            JDSQLXMLProxy newXML = new JDSQLXMLProxy ();
            return (JDSQLXMLProxy) connection_.callFactoryMethod (pxId_,
                    "getSQLXML",
                    new Class[] { Integer.TYPE },
                    new Object[] { new Integer(columnIndex) },
                    newXML);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    //@pda jdbc40
    public SQLXML getSQLXML(String columnName) throws SQLException
    {
        return getSQLXML(findColumn (columnName));
    }
    
    //@pda jdbc40
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException
    {
        try {
            SerializableReader reader;
            if (x == null)
                reader = null;
            else
                reader = new SerializableReader (x, Math.max(0,(int)length));
            callMethod ("updateNCharacterStream",
                    new Class[] { Integer.TYPE, Reader.class, Long.TYPE },
                    new Object[] { new Integer (columnIndex),
                    reader, new Long (length) });
        }
        catch (java.io.IOException e) {
            throw new SQLException (e.getMessage ());
        }
        
    }
    
    //@PDA jdbc40
    public void updateNCharacterStream(String columnName, Reader x, long length) throws SQLException
    {
        updateNCharacterStream (findColumn (columnName), x, length);
    }
    
    //@PDA jdbc40
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException
    {
        callMethod ("updateNClob",
                new Class[] { String.class, NClob.class },
                new Object[] { new Integer (columnIndex),
                nClob });
    }
    
    //@PDA jdbc40
    public void updateNClob(String columnName, NClob nClob) throws SQLException
    {
        updateNClob (findColumn (columnName), nClob);
        
    }
    
    //@pda jdbc40
    public void updateNString(int columnIndex, String nString) throws SQLException
    {
        callMethod ("updateNString",
                new Class[] { Integer.TYPE, String.class },
                new Object[] { new Integer (columnIndex),
                nString });
    }
    
    //@PDA jdbc40
    public void updateNString(String columnName, String nString) throws SQLException
    {
        updateNString (findColumn (columnName), nString);
    }
    
    //@PDA jdbc40
    public void updateRowId(int columnIndex, RowId x) throws SQLException
    {
        callMethod ("updateRowId",
                new Class[] { String.class, RowId.class },
                new Object[] { new Integer (columnIndex),
                x });
    }
    
    //@pda jdbc40
    public void updateRowId(String columnName, RowId x) throws SQLException
    {
        updateRowId (findColumn (columnName), x);
    }
    
    
    //@pda jdbc40
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException
    {
        callMethod ("updateSQLXML",
                new Class[] { String.class, SQLXML.class },
                new Object[] { new Integer (columnIndex),
                xmlObject });
    }
    
    //@pda jdbc40
    public void updateSQLXML(String columnName, SQLXML xmlObject) throws SQLException
    {
        updateSQLXML(findColumn(columnName), xmlObject);
    }
     
    //@pda jdbc40
    protected String[] getValidWrappedList()
    {
        return new String[] {  "java.sql.ResultSet" }; //@pdc user cannot cast to AS400JDBCResultSet
    } 
    
    //@PDA jdbc40
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException
    {
        InputStream iStream;
        if (x == null || x instanceof Serializable)
            iStream = x;
        else {
            try {
                iStream = new SerializableInputStream (x);
            }
            catch (java.io.IOException e) {
                throw new SQLException (e.getMessage ());
            }
        }
        callMethod ("updateAsciiStream",
                new Class[] { Integer.TYPE, InputStream.class,
                Long.TYPE },
                new Object[] { new Integer (columnIndex),
                iStream,
                new Long (length) });        
    }
    
    
    //@PDA jdbc40
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException
    {
        updateAsciiStream (findColumn (columnLabel), x, length);
    }
    
    
    //@PDA jdbc40
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException
    {
        InputStream iStream;
        if (x == null || x instanceof Serializable)
            iStream = x;
        else {
            try {
                iStream = new SerializableInputStream (x);
            }
            catch (java.io.IOException e) {
                throw new SQLException (e.getMessage ());
            }
        }
        callMethod ("updateBinaryStream",
                new Class[] { Integer.TYPE, InputStream.class,
                Long.TYPE },
                new Object[] { new Integer (columnIndex),
                iStream,
                new Long (length) });
    }
    
    //@PDA jdbc40
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException
    {
        updateBinaryStream (findColumn (columnLabel), x, length);
    }
    
    //@PDA jdbc40
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException
    {
        InputStream iStream;
        if (inputStream == null || inputStream instanceof Serializable)
            iStream = inputStream;
        else {
            try {
                iStream = new SerializableInputStream (inputStream);
            }
            catch (java.io.IOException e) {
                throw new SQLException (e.getMessage ());
            }
        }
        callMethod ("updateBlob",
                new Class[] { Integer.TYPE, InputStream.class,
                Long.TYPE },
                new Object[] { new Integer (columnIndex),
                iStream,
                new Long (length) });
    }
    
    //@PDA jdbc40
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException
    {
        updateBlob (findColumn (columnLabel), inputStream, length);
    }
    
    //@PDA jdbc40
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException
    {
        try {
            SerializableReader reader;
            if (x == null)
                reader = null;
            else
                reader = new SerializableReader (x, Math.max(0,(int)length));
            callMethod ("updateCharacterStream",
                    new Class[] { Integer.TYPE, Reader.class, Long.TYPE },
                    new Object[] { new Integer (columnIndex),
                    reader, new Long (length) });
        }
        catch (java.io.IOException e) {
            throw new SQLException (e.getMessage ());
        }
    }
    
    //@PDA jdbc40
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException
    {
        updateCharacterStream (findColumn (columnLabel), reader, length);
    }
    
    //@PDA jdbc40
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException
    {
        try {
            SerializableReader sReader;
            if (reader == null)
                sReader = null;
            else
                sReader = new SerializableReader (reader, Math.max(0,(int)length));
            callMethod ("updateClob",
                    new Class[] { Integer.TYPE, Reader.class, Long.TYPE },
                    new Object[] { new Integer (columnIndex),
                    sReader, new Long (length) });
        }
        catch (java.io.IOException e) {
            throw new SQLException (e.getMessage ());
        }
    }
    
    //@PDA jdbc40
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException
    {
        updateClob (findColumn (columnLabel), reader, length);
    }
        
    
    
    //@PDA jdbc40
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException
    {
        try {
            SerializableReader sReader;
            if (reader == null)
                sReader = null;
            else
                sReader = new SerializableReader (reader, Math.max(0,(int)length));
            callMethod ("updateNClob",
                    new Class[] { Integer.TYPE, Reader.class, Long.TYPE },
                    new Object[] { new Integer (columnIndex),
                    sReader, new Long (length) });
        }
        catch (java.io.IOException e) {
            throw new SQLException (e.getMessage ());
        }
    }
    
    
    //@PDA jdbc40
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException
    {
        updateNClob (findColumn (columnLabel), reader, length);
    }
    
    //@PDA jdbc40 
    public boolean isClosed () throws SQLException
    {
        return callMethodRtnBool ("isClosed");
    }


    //@pda jdbc40 
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException
    {
        InputStream iStream;
        if (x == null || x instanceof Serializable)
            iStream = x;
        else {
            try {
                iStream = new SerializableInputStream (x);
            }
            catch (java.io.IOException e) {
                throw new SQLException (e.getMessage ());
            }
        }
        callMethod ("updateAsciiStream",
                new Class[] { Integer.TYPE, InputStream.class },
                new Object[] { new Integer (columnIndex),
                iStream });
    }


    //@PDA jdbc40 
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException
    {
        updateAsciiStream (findColumn (columnLabel), x);
    }


    //@PDA jdbc40 
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException
    {
        InputStream iStream;
        if (x == null || x instanceof Serializable)
            iStream = x;
        else {
            try {
                iStream = new SerializableInputStream (x);
            }
            catch (java.io.IOException e) {
                throw new SQLException (e.getMessage ());
            }
        }
        callMethod ("updateBinaryStream",
                new Class[] { Integer.TYPE, InputStream.class },
                new Object[] { new Integer (columnIndex),
                iStream });
    }


    //@PDA jdbc40 
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException
    {
        updateBinaryStream (findColumn (columnLabel), x);
        
    }


    //@PDA jdbc40 
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException
    {
        InputStream iStream;
        if ( inputStream== null || inputStream instanceof Serializable)
            iStream = inputStream;
        else {
            try {
                iStream = new SerializableInputStream (inputStream);
            }
            catch (java.io.IOException e) {
                throw new SQLException (e.getMessage ());
            }
        }
        callMethod ("updateBlob",
                new Class[] { Integer.TYPE, InputStream.class },
                new Object[] { new Integer (columnIndex),
                iStream });
    }


    //@PDA jdbc40 
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException
    {
        updateBlob (findColumn (columnLabel), inputStream);
    }


    //@PDA jdbc40 
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException
    {
        try {
            SerializableReader sReader;
            if (x == null)
                sReader = null;
            else
                sReader = new SerializableReader (x);
            callMethod ("updateCharacterStream",
                    new Class[] { Integer.TYPE, Reader.class },
                    new Object[] { new Integer (columnIndex), sReader });
        }
        catch (java.io.IOException e) {
            throw new SQLException (e.getMessage ());
        }
    }


    //@PDA jdbc40 
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException
    {
        updateCharacterStream(findColumn (columnLabel), reader);
    }


    //@PDA jdbc40 
    public void updateClob(int columnIndex, Reader reader) throws SQLException
    {
        try {
            SerializableReader sReader;
            if (reader == null)
                sReader = null;
            else
                sReader = new SerializableReader (reader);
            callMethod ("updateClob",
                    new Class[] { Integer.TYPE, Reader.class },
                    new Object[] { new Integer (columnIndex), sReader });
        }
        catch (java.io.IOException e) {
            throw new SQLException (e.getMessage ());
        }
    }


    //@PDA jdbc40 
    public void updateClob(String columnLabel, Reader reader) throws SQLException
    {
        updateClob(findColumn (columnLabel), reader);
    }

    //@PDA jdbc40 
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException
    {
        try {
            SerializableReader sReader;
            if (x == null)
                sReader = null;
            else
                sReader = new SerializableReader (x);
            callMethod ("updateNCharacterStream",
                    new Class[] { Integer.TYPE, Reader.class },
                    new Object[] { new Integer (columnIndex), sReader });
        }
        catch (java.io.IOException e) {
            throw new SQLException (e.getMessage ());
        }
        
    }

    //@PDA jdbc40 
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException
    {
        updateNCharacterStream(findColumn (columnLabel), reader);
    }

    //@PDA jdbc40 
    public void updateNClob(int columnIndex, Reader reader) throws SQLException
    {
        try {
            SerializableReader sReader;
            if (reader == null)
                sReader = null;
            else
                sReader = new SerializableReader (reader);
            callMethod ("updateNClob",
                    new Class[] { Integer.TYPE, Reader.class },
                    new Object[] { new Integer (columnIndex), sReader });
        }
        catch (java.io.IOException e) {
            throw new SQLException (e.getMessage ());
        }
    }

    //@PDA jdbc40 
    public void updateNClob(String columnLabel, Reader reader) throws SQLException
    {
        updateNClob(findColumn (columnLabel), reader);
    }

}
