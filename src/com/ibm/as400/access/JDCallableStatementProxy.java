///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JDCallableStatementProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.DataTruncation;
import java.sql.Date;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Map;
import java.util.Vector;



class JDCallableStatementProxy
extends JDPreparedStatementProxy
implements CallableStatement
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  private Vector registeredTypes_ = new Vector ();
  // Note: We will only use elements starting at index 1.
  // The Vector element at index 0 will simply be a place-holder.
  // This way we can match parameter indexes directory to element indexes.


  public JDCallableStatementProxy (JDConnectionProxy jdConnection)
  {
    super (jdConnection);
  }


  // Call a method, and return a 'raw' ProxyReturnValue.
  private ProxyReturnValue callMethodRtnRaw (String methodName, int argValue)
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



// JDBC 2.0
    public Array getArray (int parameterIndex)
        throws SQLException
    {
      return (Array) callMethodRtnRaw ("getArray", parameterIndex)
                       .getReturnValue ();
    }



// JDBC 2.0
    public BigDecimal getBigDecimal (int parameterIndex)
        throws SQLException
    {
      return (BigDecimal) callMethodRtnRaw ("getBigDecimal", parameterIndex)
                         .getReturnValue ();
    }



/**
@exception  SQLException    If a SQL error occurs.
@deprecated Use getBigDecimal(int) instead.
@see #getBigDecimal(int)
**/
    public BigDecimal getBigDecimal (int parameterIndex, int scale)
        throws SQLException
    {
      return (BigDecimal) callMethodRtnRaw ("getBigDecimal",
                              new Class[] { Integer.TYPE, Integer.TYPE },
                              new Object[] { new Integer (parameterIndex),
                                             new Integer (scale) })
                         .getReturnValue ();
    }



// JDBC 2.0
    public Blob getBlob (int parameterIndex)
        throws SQLException
    {
      try {
        JDBlobProxy newBlob = new JDBlobProxy ();
        return (JDBlobProxy) connection_.callFactoryMethod (
                             pxId_,
                             "getBlob",
                             new Class[] { Integer.TYPE },
                             new Object[] { new Integer(parameterIndex) },
                             newBlob);
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
    }



    public boolean getBoolean (int parameterIndex)
        throws SQLException
    {
      return callMethodRtnRaw ("getBoolean", parameterIndex)
                        .getReturnValueBoolean ();
    }



    public byte getByte (int parameterIndex)
        throws SQLException
    {
      return callMethodRtnRaw ("getByte", parameterIndex)
                         .getReturnValueByte ();
    }



    public byte[] getBytes (int parameterIndex)
        throws SQLException
    {
      return (byte[]) callMethodRtnRaw ("getBytes", parameterIndex)
                         .getReturnValue ();
    }



// JDBC 2.0
    public Clob getClob (int parameterIndex)
        throws SQLException
    {
      try {
        JDClobProxy newClob = new JDClobProxy ();
        return (JDClobProxy) connection_.callFactoryMethod (
                             pxId_,
                             "getClob",
                             new Class[] { Integer.TYPE },
                             new Object[] { new Integer(parameterIndex) },
                             newClob);
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
    }



    public Date getDate (int parameterIndex)
        throws SQLException
    {
      return (Date) callMethodRtnRaw ("getDate", parameterIndex)
                         .getReturnValue ();
    }



// JDBC 2.0
    public Date getDate (int parameterIndex, Calendar calendar)
        throws SQLException
    {
      return (Date) callMethodRtnRaw ("getDate",
                              new Class[] { Integer.TYPE, Calendar.class },
                              new Object[] { new Integer (parameterIndex),
                                             calendar })
                         .getReturnValue ();
    }



    public double getDouble (int parameterIndex)
        throws SQLException
    {
      return callMethodRtnRaw ("getDouble", parameterIndex)
                         .getReturnValueDouble ();
    }



    public float getFloat (int parameterIndex)
        throws SQLException
    {
      return callMethodRtnRaw ("getFloat", parameterIndex)
                         .getReturnValueFloat ();
    }



    public int getInt (int parameterIndex)
        throws SQLException
    {
      return callMethodRtnRaw ("getInt", parameterIndex)
                         .getReturnValueInt ();
    }



    public long getLong (int parameterIndex)
        throws SQLException
    {
      return callMethodRtnRaw ("getLong", parameterIndex)
                         .getReturnValueLong ();
    }



    public Object getObject (int parameterIndex)
        throws SQLException
    {
      try
      {
        if (parameterIndex > 0 &&
            parameterIndex < registeredTypes_.size() )
        {
          Integer typeInt = (Integer)registeredTypes_.elementAt(parameterIndex);
          if (typeInt != null)
          {
            int type = typeInt.intValue();
            ProxyFactoryImpl proxyObject = null;
            if (type == Types.BLOB) {
              proxyObject = new JDBlobProxy ();
            }
            else if (type == Types.CLOB) {
              proxyObject = new JDClobProxy ();
            }
            if (proxyObject != null) {
              return connection_.callFactoryMethod (pxId_, "getObject",
                                 new Class[] { Integer.TYPE },
                                 new Object[] { new Integer (parameterIndex) },
                                 proxyObject);
            }
          }
        }
        return connection_.callMethod (pxId_, "getObject",
                                new Class[] { Integer.TYPE },
                                new Object[] { new Integer (parameterIndex) })
          .getReturnValue();
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
    }


// JDBC 2.0
    public Object getObject (int parameterIndex, Map typeMap)
        throws SQLException
    {
      try
      {
        if (parameterIndex > 0 &&
            parameterIndex < registeredTypes_.size() )
        {
          Integer typeInt = (Integer)registeredTypes_.elementAt(parameterIndex);
          if (typeInt != null)
          {
            int type = typeInt.intValue();
            ProxyFactoryImpl proxyObject = null;
            if (type == Types.BLOB) {
              proxyObject = new JDBlobProxy ();
            }
            else if (type == Types.CLOB) {
              proxyObject = new JDClobProxy ();
            }
            if (proxyObject != null) {
              return connection_.callFactoryMethod (pxId_, "getObject",
                                 new Class[] { Integer.TYPE, Map.class },
                                 new Object[] { new Integer (parameterIndex),
                                                typeMap },
                                 proxyObject);
            }
          }
        }
        return connection_.callMethod (pxId_, "getObject",
                                new Class[] { Integer.TYPE, Map.class },
                                new Object[] { new Integer (parameterIndex),
                                               typeMap })
          .getReturnValue();
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
    }



// JDBC 2.0
    public Ref getRef (int parameterIndex)
        throws SQLException
    {
      return (Ref) callMethodRtnRaw ("getRef", parameterIndex)
                         .getReturnValue ();
    }



    public short getShort (int parameterIndex)
        throws SQLException
    {
      return callMethodRtnRaw ("getShort", parameterIndex)
                         .getReturnValueShort ();
    }



    public String getString (int parameterIndex)
        throws SQLException
    {
      return (String) callMethodRtnRaw ("getString", parameterIndex)
                         .getReturnValue ();
    }



    public Time getTime (int parameterIndex)
        throws SQLException
    {
      return (Time) callMethodRtnRaw ("getTime", parameterIndex)
                         .getReturnValue ();
    }



// JDBC 2.0
    public Time getTime (int parameterIndex, Calendar calendar)
        throws SQLException
    {
      return (Time) callMethodRtnRaw ("getTime",
                              new Class[] { Integer.TYPE, Calendar.class },
                              new Object[] { new Integer (parameterIndex),
                                             calendar })
                         .getReturnValue ();
    }



    public Timestamp getTimestamp (int parameterIndex)
        throws SQLException
    {
      return (Timestamp) callMethodRtnRaw ("getTimestamp", parameterIndex)
                         .getReturnValue ();
    }



// JDBC 2.0
    public Timestamp getTimestamp (int parameterIndex, Calendar calendar)
        throws SQLException
    {
      return (Timestamp) callMethodRtnRaw ("getTimestamp",
                              new Class[] { Integer.TYPE, Calendar.class },
                              new Object[] { new Integer (parameterIndex),
                                             calendar })
                         .getReturnValue ();
    }



    private void registerLocally (int parameterIndex, int sqlType)
    {
      for (int i=registeredTypes_.size(); i<=parameterIndex; i++)
        registeredTypes_.addElement (null);
      registeredTypes_.setElementAt (new Integer(sqlType), parameterIndex);
    }



    public void registerOutParameter (int parameterIndex,
                                      int sqlType,
                                      int scale)
      throws SQLException
    {
      callMethod ("registerOutParameter",
                  new Class[] { Integer.TYPE, Integer.TYPE,
                                Integer.TYPE },
                  new Object[] { new Integer (parameterIndex),
                                 new Integer (sqlType),
                                 new Integer (scale) });

      registerLocally (parameterIndex, sqlType);  //@A1C
    }


    public void registerOutParameter (int parameterIndex, int sqlType)
      throws SQLException
    {
      callMethod ("registerOutParameter",
                  new Class[] { Integer.TYPE, Integer.TYPE },
                  new Object[] { new Integer (parameterIndex),
                                 new Integer (sqlType) });

      registerLocally (parameterIndex, sqlType);  //@A1C
    }


    public void registerOutParameter (int parameterIndex, int sqlType, String typeName)
      throws SQLException
    {

      callMethod ("registerOutParameter",
                  new Class[] { Integer.TYPE, Integer.TYPE,
                                String.class },
                  new Object[] { new Integer (parameterIndex),
                                 new Integer (sqlType),
                                 new Integer (typeName) });

      registerLocally (parameterIndex, sqlType);  //@A1C
    }


    public boolean wasNull ()
		throws SQLException
    {
      return callMethodRtnBool ("wasNull");
    }




}
