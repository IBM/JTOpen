///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDParameterMetaDataProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.InvocationTargetException;
import java.sql.ParameterMetaData;
import java.sql.SQLException;



/**
<p>The JDParameterMetaDataProxy class gets information about the types and properties of the parameters 
in a PreparedStatement object. 
**/
class JDParameterMetaDataProxy
extends AbstractProxyImpl
implements ParameterMetaData
{
  static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";


  // Private data.
 
  JDConnectionProxy       jdConnection_;
                                  // The associated JDBC Connection object.



  public JDParameterMetaDataProxy (JDConnectionProxy jdConnection)
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

  public int getParameterCount()
  throws SQLException
  {
      return callMethodRtnInt("getParameterCount");
  }

  public int isNullable(int param)
  throws SQLException
  {
      return callMethodRtnRaw ("isNullable", param).getReturnValueInt();
  }

  public boolean isSigned(int param)
  throws SQLException
  {
      return callMethodRtnRaw ("isSigned", param).getReturnValueBoolean();
  }

  public int getPrecision(int param)
  throws SQLException
  {
     return callMethodRtnRaw ("getPrecision", param).getReturnValueInt();
  }

  public int getScale(int param)
  throws SQLException
  {
     return callMethodRtnRaw ("getScale", param).getReturnValueInt();
  }

  public int getParameterType(int param)
  throws SQLException
  {
     return callMethodRtnRaw ("getParameterType", param).getReturnValueInt();
  }

  public String getParameterTypeName(int param)
  throws SQLException
  {
     return callMethodRtnStr ("getParameterTypeName", param);
  }

  public String getParameterClassName(int param)
  throws SQLException
  {
     return callMethodRtnStr ("getParameterClassName", param);
  }

  public int getParameterMode(int param)
  throws SQLException
  {
     return callMethodRtnRaw ("getParameterMode", param).getReturnValueInt();
  }

}
