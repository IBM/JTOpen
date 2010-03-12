///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDResultSetMetaDataProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;



/**
<p>The JDResultSetMetaDataProxy class describes the
columns in a result set.
**/
class JDResultSetMetaDataProxy
extends AbstractProxyImpl
implements ResultSetMetaData
{
  static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


  // Private data.
 
  JDConnectionProxy       jdConnection_;
                                  // The associated JDBC Connection object.



  public JDResultSetMetaDataProxy (JDConnectionProxy jdConnection)
  {
    jdConnection_ = jdConnection;
  }

  // Call a method, and return a 'raw' ProxyReturnValue.
  private ProxyReturnValue callMethodRtnRaw (String methodName,
                                             int argValue)
    throws SQLException
  {
    try {
      return connection_.callMethod (pxId_, methodName,
                               new Class[] { Integer.TYPE },
                               new Object[] { new Integer (argValue) });
    }
    catch (InvocationTargetException e) {
      throw JDConnectionProxy.rethrow1 (e);
    }
  }

  // Call a method, and return a String.
  private String callMethodRtnStr (String methodName, int argValue)
    throws SQLException
  {
    try {
      return (String) connection_.callMethod (pxId_, methodName,
                               new Class[] { Integer.TYPE },
                               new Object[] { new Integer (argValue) })
                     .getReturnValue ();
    }
    catch (InvocationTargetException e) {
      throw JDConnectionProxy.rethrow1 (e);
    }
  }


    public String getCatalogName (int columnIndex)
      throws SQLException
    {
      return callMethodRtnStr ("getCatalogName", columnIndex);
    }


// JDBC 2.0
    public String getColumnClassName (int columnIndex)
      throws SQLException
    {
      return callMethodRtnStr ("getColumnClassName", columnIndex);
    }


    public int getColumnCount ()
      throws SQLException
    {
      try {
        return connection_.callMethodReturnsInt (pxId_, "getColumnCount");
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
    }


    public int getColumnDisplaySize (int columnIndex)
      throws SQLException
    {
      return callMethodRtnRaw ("getColumnDisplaySize", columnIndex)
                    .getReturnValueInt ();
    }

    public String getColumnLabel (int columnIndex)
      throws SQLException
    {
      return callMethodRtnStr ("getColumnLabel", columnIndex);
    }


    public String getColumnName (int columnIndex)
      throws SQLException
    {
      return callMethodRtnStr ("getColumnName", columnIndex);
    }


    public int getColumnType (int columnIndex)
      throws SQLException
    {
      return callMethodRtnRaw ("getColumnType", columnIndex)
                  .getReturnValueInt ();
    }


    public String getColumnTypeName (int columnIndex)
      throws SQLException
    {
      return callMethodRtnStr ("getColumnTypeName", columnIndex);
    }


    public int getPrecision (int columnIndex)
      throws SQLException
    {
      return callMethodRtnRaw ("getPrecision", columnIndex)
              .getReturnValueInt ();
    }


    public int getScale (int columnIndex)
      throws SQLException
    {
      return callMethodRtnRaw ("getScale", columnIndex)
              .getReturnValueInt ();
    }


    public String getSchemaName (int columnIndex)
      throws SQLException
    {
      return callMethodRtnStr ("getSchemaName", columnIndex);
    }


    public String getTableName (int columnIndex)
      throws SQLException
    {
      return callMethodRtnStr ("getTableName", columnIndex);
    }


    public boolean isAutoIncrement (int columnIndex)
      throws SQLException
    {
      return callMethodRtnRaw ("isAutoIncrement", columnIndex)
              .getReturnValueBoolean ();
    }


    public boolean isCaseSensitive (int columnIndex)
      throws SQLException
    {
      return callMethodRtnRaw ("isCaseSensitive", columnIndex)
              .getReturnValueBoolean ();
    }


    public boolean isCurrency (int columnIndex)
      throws SQLException
    {
      return callMethodRtnRaw ("isCurrency", columnIndex)
              .getReturnValueBoolean ();
    }


    public boolean isDefinitelyWritable (int columnIndex)
      throws SQLException
    {
      return callMethodRtnRaw ("isDefinitelyWritable", columnIndex)
              .getReturnValueBoolean ();
    }


    public int isNullable (int columnIndex)
      throws SQLException
    {
      return callMethodRtnRaw ("isNullable", columnIndex)
              .getReturnValueInt ();
    }


    public boolean isReadOnly (int columnIndex)
      throws SQLException
    {
      return callMethodRtnRaw ("isReadOnly", columnIndex)
              .getReturnValueBoolean ();
    }


    public boolean isSearchable (int columnIndex)
      throws SQLException
    {
      return callMethodRtnRaw ("isSearchable", columnIndex)
              .getReturnValueBoolean ();
    }


    public boolean isSigned (int columnIndex)
      throws SQLException
    {
      return callMethodRtnRaw ("isSigned", columnIndex)
              .getReturnValueBoolean ();
    }


    public boolean isWritable (int columnIndex)
      throws SQLException
    {
      return callMethodRtnRaw ("isWritable", columnIndex)
              .getReturnValueBoolean ();
    }


    // This method is not required by java.sql.ResultSetMetaData,
    // but it is used by the JDBC testcases, and is implemented
    // in the public class.
    public String toString ()
    {
      try {
        return (String) connection_.callMethodReturnsObject (pxId_,
                                                                  "toString");
      }
      catch (InvocationTargetException e) {
        throw ProxyClientConnection.rethrow (e);
      }
    }


}
