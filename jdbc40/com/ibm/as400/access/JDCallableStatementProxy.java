///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDCallableStatementProxy.java
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
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
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
  private static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";


  private Vector registeredTypes_ = new Vector ();
  // Note: We will only use elements starting at index 1.
  // The Vector element at index 0 will simply be a place-holder.
  // This way we can match parameter indexes directory to element indexes.

    // Copied from JDError:
    private static final String EXC_FUNCTION_NOT_SUPPORTED       = "IM001";

    private final static String NOT_SERIALIZABLE = "Parameter is not serializable.";

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


    // Added for JDBC 3.0 support
    // Call a method, and return a 'raw' ProxyReturnValue.
    private ProxyReturnValue callMethodRtnRaw (String methodName, String argValue)
    throws SQLException
    {
        try {
            return connection_.callMethod (pxId_, methodName,
                                           new Class[] { String.class},
                                           new Object[] { argValue});
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }



    // Added for JDBC 3.0 support 
    private int findParameterIndex(String parameterName)
    throws SQLException
    {
        try
        {
            return((Integer)connection_.callMethod (pxId_, "findParameterIndex",
                                                    new Class[] { String.class},
                                                    new Object[] { parameterName})
                   .getReturnValue())
            .intValue();
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



// JDBC 3.0
    public Array getArray (String parameterName)
    throws SQLException
    {
        return(Array) callMethodRtnRaw ("getArray", parameterName)
        .getReturnValue ();
    }



// JDBC 2.0
    public BigDecimal getBigDecimal (int parameterIndex)
        throws SQLException
    {
      return (BigDecimal) callMethodRtnRaw ("getBigDecimal", parameterIndex)
                         .getReturnValue ();
    }



// JDBC 3.0
    public BigDecimal getBigDecimal (String parameterName)
    throws SQLException
    {
        return(BigDecimal) callMethodRtnRaw ("getBigDecimal", parameterName)
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



// JDBC 3.0
    public Blob getBlob (String parameterName)
    throws SQLException
    {
        try {
            JDBlobProxy newBlob = new JDBlobProxy ();
            return(JDBlobProxy) connection_.callFactoryMethod (
                                                              pxId_,
                                                              "getBlob",
                                                              new Class[] { String.class},
                                                              new Object[] { parameterName},
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



// JDBC 3.0
    public boolean getBoolean (String parameterName)
    throws SQLException
    {
        return callMethodRtnRaw ("getBoolean", parameterName)
        .getReturnValueBoolean ();
    }



    public byte getByte (int parameterIndex)
        throws SQLException
    {
      return callMethodRtnRaw ("getByte", parameterIndex)
                         .getReturnValueByte ();
    }



//JDBC 3.0
    public byte getByte (String parameterName)
    throws SQLException
    {
        return callMethodRtnRaw ("getByte", parameterName)
        .getReturnValueByte ();
    }



    public byte[] getBytes (int parameterIndex)
        throws SQLException
    {
      return (byte[]) callMethodRtnRaw ("getBytes", parameterIndex)
                         .getReturnValue ();
    }



// JDBC 3.0
    public byte[] getBytes (String parameterName)
    throws SQLException
    {
        return(byte[]) callMethodRtnRaw ("getBytes", parameterName)
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



// JDBC 3.0
    public Clob getClob (String parameterName)
    throws SQLException
    {
        try {
            JDClobProxy newClob = new JDClobProxy ();
            return(JDClobProxy) connection_.callFactoryMethod (
                                                              pxId_,
                                                              "getClob",
                                                              new Class[] { String.class},
                                                              new Object[] { parameterName},
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



// JDBC 3.0
    public Date getDate (String parameterName)
    throws SQLException
    {
        return(Date) callMethodRtnRaw ("getDate", parameterName)
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



// JDBC 3.0
    public Date getDate (String parameterName, Calendar calendar)
    throws SQLException
    {
        return(Date) callMethodRtnRaw ("getDate",
                                       new Class[] { String.class, Calendar.class},
                                       new Object[] { parameterName,
                                           calendar})
        .getReturnValue ();
    }



    public double getDouble (int parameterIndex)
        throws SQLException
    {
      return callMethodRtnRaw ("getDouble", parameterIndex)
                         .getReturnValueDouble ();
    }



// JDBC 3.0
    public double getDouble (String parameterName)
    throws SQLException
    {
        return callMethodRtnRaw ("getDouble", parameterName)
        .getReturnValueDouble ();
    }



    public float getFloat (int parameterIndex)
        throws SQLException
    {
      return callMethodRtnRaw ("getFloat", parameterIndex)
                         .getReturnValueFloat ();
    }



// JDBC 3.0
    public float getFloat (String parameterName)
    throws SQLException
    {
        return callMethodRtnRaw ("getFloat", parameterName)
        .getReturnValueFloat ();
    }



    public int getInt (int parameterIndex)
        throws SQLException
    {
      return callMethodRtnRaw ("getInt", parameterIndex)
                         .getReturnValueInt ();
    }



// JDBC 3.0
    public int getInt (String parameterName)
    throws SQLException
    {
        return callMethodRtnRaw ("getInt", parameterName)
        .getReturnValueInt ();
    }



    public long getLong (int parameterIndex)
        throws SQLException
    {
      return callMethodRtnRaw ("getLong", parameterIndex)
                         .getReturnValueLong ();
    }



// JDBC 3.0
    public long getLong (String parameterName)
    throws SQLException
    {
        return callMethodRtnRaw ("getLong", parameterName)
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


// JDBC 3.0
    public Object getObject (String parameterName)
    throws SQLException
    {
        return getObject (findParameterIndex(parameterName));
    }



// JDBC 3.0
    public Object getObject (String parameterName, Map typeMap)
    throws SQLException
    {
        return getObject(findParameterIndex(parameterName), typeMap);
    }




// JDBC 2.0
    public Ref getRef (int parameterIndex)
        throws SQLException
    {
      return (Ref) callMethodRtnRaw ("getRef", parameterIndex)
                         .getReturnValue ();
    }



// JDBC 3.0
    public Ref getRef (String parameterName)
    throws SQLException
    {
        return(Ref) callMethodRtnRaw ("getRef", parameterName)
        .getReturnValue ();
    }



    public short getShort (int parameterIndex)
        throws SQLException
    {
      return callMethodRtnRaw ("getShort", parameterIndex)
                         .getReturnValueShort ();
    }


// JDBC 3.0
    public short getShort (String parameterName)
    throws SQLException
    {
        return callMethodRtnRaw ("getShort", parameterName)
        .getReturnValueShort ();
    }



    public String getString (int parameterIndex)
        throws SQLException
    {
      return (String) callMethodRtnRaw ("getString", parameterIndex)
                         .getReturnValue ();
    }



// JDBC 3.0
    public String getString (String parameterName)
    throws SQLException
    {
        return(String) callMethodRtnRaw ("getString", parameterName)
        .getReturnValue ();
    }



    public Time getTime (int parameterIndex)
        throws SQLException
    {
      return (Time) callMethodRtnRaw ("getTime", parameterIndex)
                         .getReturnValue ();
    }



// JDBC 3.0
    public Time getTime (String parameterName)
    throws SQLException
    {
        return(Time) callMethodRtnRaw ("getTime", parameterName)
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



// JDBC 3.0
    public Time getTime (String parameterName, Calendar calendar)
    throws SQLException
    {
        return(Time) callMethodRtnRaw ("getTime",
                                       new Class[] { String.class, Calendar.class},
                                       new Object[] { parameterName,
                                           calendar})
        .getReturnValue ();
    }



    public Timestamp getTimestamp (int parameterIndex)
        throws SQLException
    {
      return (Timestamp) callMethodRtnRaw ("getTimestamp", parameterIndex)
                         .getReturnValue ();
    }



// JDBC 3.0
    public Timestamp getTimestamp (String parameterName)
    throws SQLException
    {
        return(Timestamp) callMethodRtnRaw ("getTimestamp", parameterName)
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



// JDBC 3.0
    public Timestamp getTimestamp (String parameterName, Calendar calendar)
    throws SQLException
    {
        return(Timestamp) callMethodRtnRaw ("getTimestamp",
                                            new Class[] { String.class, Calendar.class},
                                            new Object[] { parameterName,
                                                calendar})
        .getReturnValue ();
    }



// JDBC 3.0
    public URL getURL (int parameterIndex)
    throws SQLException
    {
        return(URL) callMethodRtnRaw ("getURL",
                                      new Class[] { Integer.TYPE},
                                      new Object[] { new Integer (parameterIndex)})
        .getReturnValue ();
    }



// JDBC 3.0
    public URL getURL (String parameterName)
    throws SQLException
    {
        return(URL) callMethodRtnRaw ("getURL",
                                      new Class[] { String.class},
                                      new Object[] { parameterName})
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



// JDBC 3.0
    public void registerOutParameter (String parameterName, int sqlType)
    throws SQLException
    {
        registerOutParameter(findParameterIndex(parameterName), sqlType);
    }



// JDBC 3.0
    public void registerOutParameter (String parameterName, int sqlType, int scale)
    throws SQLException
    {
        registerOutParameter(findParameterIndex(parameterName), sqlType, scale);
    }



// JDBC 3.0
    public void registerOutParameter (String parameterName, int sqlType, String typeName)
    throws SQLException
    {
        registerOutParameter(findParameterIndex(parameterName), sqlType, typeName);
    }



// JDBC 3.0
    public void setAsciiStream (String parameterName, InputStream parameterValue, int length)
    throws SQLException
    {
        InputStream iStream;
        if (parameterValue == null ||
            parameterValue instanceof Serializable)
            iStream = parameterValue;
        else {
            try {
                iStream = new SerializableInputStream (parameterValue);
            }
            catch (java.io.IOException e) {
                throw new SQLException (e.getMessage ());
            }
        }
        callMethod ("setAsciiStream",
                    new Class[] { String.class, InputStream.class,
                        Integer.TYPE},
                    new Object[] { parameterName,
                        iStream,
                        new Integer (length)});
    }



// JDBC 3.0
    public void setBigDecimal (String parameterName, BigDecimal parameterValue)
    throws SQLException
    {
        callMethod ("setBigDecimal",
                    new Class[] { String.class, BigDecimal.class},
                    new Object[] { parameterName,
                        parameterValue});
    }




// JDBC 3.0
    public void setBinaryStream (String parameterName,
                                 InputStream parameterValue,
                                 int length)
    throws SQLException
    {
        InputStream iStream;
        if (parameterValue == null ||
            parameterValue instanceof Serializable)
            iStream = parameterValue;
        else {
            try {
                iStream = new SerializableInputStream (parameterValue);
            }
            catch (java.io.IOException e) {
                throw new SQLException (e.getMessage ());
            }
        }
        callMethod ("setBinaryStream",
                    new Class[] { String.class, InputStream.class,
                        Integer.TYPE},
                    new Object[] { parameterName,
                        iStream,
                        new Integer (length)});
    }




// JDBC 3.0
    public void setBoolean (String parameterName, boolean parameterValue)
    throws SQLException
    {
        callMethod ("setBoolean",
                    new Class[] { String.class, Boolean.TYPE},
                    new Object[] { parameterName,
                        new Boolean(parameterValue)});
    }



// JDBC 3.0
    public void setByte (String parameterName, byte parameterValue)
    throws SQLException
    {
        callMethod ("setByte",
                    new Class[] { String.class, Byte.TYPE},
                    new Object[] { parameterName,
                        new Byte(parameterValue)});
    }



// JDBC 3.0
    public void setBytes (String parameterName, byte[] parameterValue)
    throws SQLException
    {
        callMethod ("setBytes",
                    new Class[] { String.class, byte[].class},       //@K1C Changed from Byte.class to byte[].class 
                    new Object[] { parameterName,
                        parameterValue});
    }



//JDBC 3.0
    public void setCharacterStream (String parameterName,
                                    Reader parameterValue,
                                    int length)
    throws SQLException
    {
        try {
            SerializableReader reader;
            if (parameterValue == null)
                reader = null;
            else
                reader = new SerializableReader (parameterValue, Math.max(0,length));
            callMethod ("setCharacterStream",
                        new Class[] { String.class, Reader.class,
                            Integer.TYPE},
                        new Object[] { parameterName,
                            reader,
                            new Integer (length)});
        }
        catch (java.io.IOException e) {
            throw new SQLException (e.getMessage ());
        }
    }



// JDBC 3.0
    public void setDate (String parameterName, Date parameterValue)
    throws SQLException
    {
        callMethod ("setDate",
                    new Class[] { String.class, Date.class},
                    new Object[] { parameterName,
                        parameterValue});
    }



// JDBC 3.0
    public void setDate (String parameterName, Date parameterValue, Calendar cal)
    throws SQLException
    {
        callMethod ("setDate",
                    new Class[] { String.class, Date.class, Calendar.class},
                    new Object[] { parameterName,
                        parameterValue, cal});
    }



// JDBC 3.0
    public void setDouble (String parameterName, double parameterValue)
    throws SQLException
    {
        callMethod ("setDouble",
                    new Class[] { String.class, Double.TYPE},
                    new Object[] { parameterName,
                        new Double(parameterValue)});
    }



// JDBC 3.0
    public void setFloat (String parameterName, float parameterValue)
    throws SQLException
    {
        callMethod ("setFloat",
                    new Class[] { String.class, Float.TYPE},
                    new Object[] { parameterName,
                        new Float(parameterValue)});
    }



// JDBC 3.0
    public void setInt (String parameterName, int parameterValue)
    throws SQLException
    {
        callMethod ("setInt",
                    new Class[] { String.class, Integer.TYPE},
                    new Object[] { parameterName,
                        new Integer(parameterValue)});
    }



// JDBC 3.0
    public void setLong (String parameterName, long parameterValue)
    throws SQLException
    {
        callMethod ("setLong",
                    new Class[] { String.class, Long.TYPE},
                    new Object[] { parameterName,
                        new Long(parameterValue)});
    }



// JDBC 3.0
    public void setNull (String parameterName, int sqlType)
    throws SQLException
    {
        callMethod ("setNull",
                    new Class[] { String.class, Integer.TYPE},
                    new Object[] { parameterName,
                        new Integer(sqlType)});
    }



// JDBC 3.0
    public void setNull (String parameterName, int sqlType, String typeName)
    throws SQLException
    {
        callMethod ("setNull",
                    new Class[] { String.class, Integer.TYPE, String.class},
                    new Object[] { parameterName,
                        new Integer(sqlType), typeName});
    }



// JDBC 3.0
    public void setObject (String parameterName, Object parameterValue)
    throws SQLException
    {
        if (parameterValue != null &&
            !(parameterValue instanceof Serializable) ){
            if (JDTrace.isTraceOn())
                JDTrace.logInformation (this, NOT_SERIALIZABLE);
            throw new SQLException ();
        }

        callMethod ("setObject",
                    new Class[] { String.class, Object.class},
                    new Object[] { parameterName,
                        parameterValue});
    }



// JDBC 3.0
    public void setObject (String parameterName,
                           Object parameterValue,
                           int sqlType)
    throws SQLException
    {
        if (parameterValue != null &&
            !(parameterValue instanceof Serializable) ){
            if (JDTrace.isTraceOn())
                JDTrace.logInformation (this, NOT_SERIALIZABLE);
            throw new SQLException ();
        }

        callMethod ("setObject",
                    new Class[] { String.class, Object.class,
                        Integer.TYPE},
                    new Object[] { parameterName,
                        parameterValue,
                        new Integer (sqlType)});
    }



// JDBC 3.0
    public void setObject (String parameterName,
                           Object parameterValue,
                           int sqlType,
                           int scale)
    throws SQLException
    {
        if (parameterValue != null &&
            !(parameterValue instanceof Serializable) ){
            if (JDTrace.isTraceOn())
                JDTrace.logInformation (this, NOT_SERIALIZABLE);
            throw new SQLException ();
        }

        callMethod ("setObject",
                    new Class[] { String.class, Object.class,
                        Integer.TYPE, Integer.TYPE},
                    new Object[] { parameterName,
                        parameterValue,
                        new Integer (sqlType),
                        new Integer (scale)});
    }




// JDBC 3.0
    public void setShort (String parameterName, short parameterValue)
    throws SQLException
    {
        callMethod ("setShort",
                    new Class[] { String.class, Short.TYPE},
                    new Object[] { parameterName,
                        new Short(parameterValue)});
    }



// JDBC 3.0
    public void setString (String parameterName, String sqlType)
    throws SQLException
    {
        callMethod ("setString",
                    new Class[] { String.class, String.class},
                    new Object[] { parameterName,
                        sqlType});
    }



// JDBC 3.0
    public void setTime (String parameterName, Time parameterValue)
    throws SQLException
    {
        callMethod ("setTime",
                    new Class[] { String.class, Time.class},
                    new Object[] { parameterName,
                        parameterValue});
    }



// JDBC 3.0
    public void setTime (String parameterName, Time parameterValue, Calendar cal)
    throws SQLException
    {
        callMethod ("setTime",
                    new Class[] { String.class, Time.class, Calendar.class},
                    new Object[] { parameterName,
                        parameterValue, cal});
    }



// JDBC 3.0
    public void setTimestamp (String parameterName, Timestamp parameterValue)
    throws SQLException
    {
        callMethod ("setTimestamp",
                    new Class[] { String.class, Timestamp.class},
                    new Object[] { parameterName,
                        parameterValue});
    }



// JDBC 3.0
    public void setTimestamp (String parameterName, Timestamp parameterValue, Calendar cal)
    throws SQLException
    {
        callMethod ("setTimestamp",
                    new Class[] { String.class, Timestamp.class, Calendar.class},
                    new Object[] { parameterName,
                        parameterValue, cal});
    }



// JDBC 3.0
    public void setURL (String parameterName, URL parameterValue)
    throws SQLException
    {
        callMethod ("setURL",
                    new Class[] { String.class, URL.class},
                    new Object[] { parameterName,
                        parameterValue});
    }



    public boolean wasNull ()
    throws SQLException
    {
      return callMethodRtnBool ("wasNull");
    }


    //@pda jdbc40
    protected String[] getValidWrappedList()
    {
        return new String[] { "java.sql.CallableStatement" };
    } 
    
   
    
    //@PDA jdbc40
    public Reader getCharacterStream(int parameterIndex) throws SQLException
    {
        try {
            JDReaderProxy newReader = new JDReaderProxy ();
            return (JDReaderProxy) connection_.callFactoryMethod (
                    pxId_, "getCharacterStream",
                    new Class[] { Integer.TYPE },
                    new Object[] { new Integer (parameterIndex) },
                    newReader);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    //@PDA jdbc40
    public Reader getCharacterStream(String parameterName) throws SQLException
    {
        try {
            JDReaderProxy newReader = new JDReaderProxy ();
            return (JDReaderProxy) connection_.callFactoryMethod (
                    pxId_, "getCharacterStream",
                    new Class[] { String.class },
                    new Object[] { new Integer (parameterName) },
                    newReader);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    //@PDA jdbc40
    public Reader getNCharacterStream(int parameterIndex) throws SQLException
    {
        try {
            JDReaderProxy newReader = new JDReaderProxy ();
            return (JDReaderProxy) connection_.callFactoryMethod (
                    pxId_, "getNCharacterStream",
                    new Class[] { Integer.TYPE },
                    new Object[] { new Integer (parameterIndex) },
                    newReader);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    //@PDA jdbc40
    public Reader getNCharacterStream(String parameterName) throws SQLException
    {
        try {
            JDReaderProxy newReader = new JDReaderProxy ();
            return (JDReaderProxy) connection_.callFactoryMethod (
                    pxId_, "getNCharacterStream",
                    new Class[] { String.class },
                    new Object[] { new Integer (parameterName) },
                    newReader);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    //@PDA jdbc40
    public NClob getNClob(int parameterIndex) throws SQLException
    {
        try {
            JDNClobProxy newClob = new JDNClobProxy ();
            return (JDNClobProxy) connection_.callFactoryMethod (pxId_,
                    "getNClob",
                    new Class[] { Integer.TYPE },
                    new Object[] { new Integer(parameterIndex) },
                    newClob);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    //@PDA jdbc40
    public NClob getNClob(String parameterName) throws SQLException
    {
        try {
            JDNClobProxy newClob = new JDNClobProxy ();
            return (JDNClobProxy) connection_.callFactoryMethod (pxId_,
                    "getNClob",
                    new Class[] { String.class },
                    new Object[] { new Integer(parameterName) },
                    newClob);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    //@PDA jdbc40
    public String getNString(int parameterIndex) throws SQLException
    {
        return (String) callMethodRtnRaw ("getNString", parameterIndex).getReturnValue ();
    }
    
    //@PDA jdbc40    
    public String getNString(String parameterName) throws SQLException
    {
        return (String) callMethodRtnRaw ("getNString", parameterName).getReturnValue ();
    }
    
    //@PDA jdbc40
    public RowId getRowId(int parameterIndex) throws SQLException
    {
        try {
            JDRowIdProxy newRowId = new JDRowIdProxy ();
            return (JDRowIdProxy) connection_.callFactoryMethod (pxId_,
                    "getRowId",
                    new Class[] { Integer.TYPE },
                    new Object[] { new Integer(parameterIndex) },
                    newRowId);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    //@PDA jdbc40
    public RowId getRowId(String parameterName) throws SQLException
    {
        try {
            JDRowIdProxy newRowId = new JDRowIdProxy ();
            return (JDRowIdProxy) connection_.callFactoryMethod (pxId_,
                    "getRowId",
                    new Class[] { String.class },
                    new Object[] { new Integer(parameterName) },
                    newRowId);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    //@PDA jdbc40
    public SQLXML getSQLXML(int parameterIndex) throws SQLException
    {
        try {
            JDSQLXMLProxy newXML = new JDSQLXMLProxy ();
            return (JDSQLXMLProxy) connection_.callFactoryMethod (pxId_,
                    "getSQLXML",
                    new Class[] { Integer.TYPE },
                    new Object[] { new Integer(parameterIndex) },
                    newXML);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    //@PDA jdbc40
    public SQLXML getSQLXML(String parameterName) throws SQLException
    {
        try {
            JDSQLXMLProxy newXML = new JDSQLXMLProxy ();
            return (JDSQLXMLProxy) connection_.callFactoryMethod (pxId_,
                    "getSQLXML",
                    new Class[] { String.class },
                    new Object[] { new Integer(parameterName) },
                    newXML);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    //@PDA jdbc40
    public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException
    {
        InputStream iStream;
        if (x == null ||
                x instanceof Serializable)
            iStream = x;
        else {
            try {
                iStream = new SerializableInputStream (x);
            }
            catch (java.io.IOException e) {
                throw new SQLException (e.getMessage ());
            }
        }
        callMethod ("setAsciiStream",
                new Class[] { String.class, InputStream.class,
                Long.TYPE },
                new Object[] { parameterName,
                iStream,
                new Long (length) });
    }
    
    //@PDA jdbc40
    public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException
    {
        InputStream iStream;
        if (x == null ||
                x instanceof Serializable)
            iStream = x;
        else {
            try {
                iStream = new SerializableInputStream (x);
            }
            catch (java.io.IOException e) {
                throw new SQLException (e.getMessage ());
            }
        }
        callMethod ("setBinaryStream",
                new Class[] { String.class, InputStream.class,
                Long.TYPE },
                new Object[] { parameterName,
                iStream,
                new Long (length) });
    }
    
    //@PDA jdbc40
    public void setBlob(String parameterName, Blob x) throws SQLException
    {
        if (x != null &&
                !(x instanceof Serializable) ){
            if (JDTrace.isTraceOn())
                JDTrace.logInformation (this, NOT_SERIALIZABLE);
            throw new SQLException ();
        }
        
        callMethod ("setBlob",
                new Class[] { String.class, Blob.class },
                new Object[] { parameterName,
                x });
    }
    
    //@PDA jdbc40
    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException
    {
        InputStream iStream;
        if (inputStream == null ||
                inputStream instanceof Serializable)
            iStream = inputStream;
        else {
            try {
                iStream = new SerializableInputStream (inputStream);
            }
            catch (java.io.IOException e) {
                throw new SQLException (e.getMessage ());
            }
        }
        callMethod ("setBlob",
                new Class[] { String.class, InputStream.class,
                Long.TYPE },
                new Object[] { parameterName,
                iStream,
                new Long (length) });
    }
    
    //@PDA jdbc40
    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException
    {
        try {
            SerializableReader serialReader;
            if (reader == null)
                serialReader = null;
            else
                serialReader = new SerializableReader (reader, (int)Math.max(0,length));
            callMethod ("setCharacterStream",
                    new Class[] { String.class, Reader.class,
                    Long.TYPE },
                    new Object[] { parameterName,
                    serialReader,
                    new Long (length) });
        }
        catch (java.io.IOException e) {
            throw new SQLException (e.getMessage ());
        }
    }
    
    //@PDA jdbc40
    public void setClob(String parameterName, Clob x) throws SQLException
    {
        if (x != null &&
                !(x instanceof Serializable) ){
            if (JDTrace.isTraceOn())
                JDTrace.logInformation (this, NOT_SERIALIZABLE);
            throw new SQLException ();
        }
        
        callMethod ("setClob",
                new Class[] { String.class, Clob.class },
                new Object[] { parameterName,
                x });   
    }
    
    //@PDA jdbc40
    public void setClob(String parameterName, Reader reader, long length) throws SQLException
    {
        try {
            SerializableReader serialRreader;
            if (reader == null)
                serialRreader = null;
            else
                serialRreader = new SerializableReader (reader, (int) Math.max(0,length));
            callMethod ("setClob",
                    new Class[] { String.class, Reader.class,
                    Long.TYPE },
                    new Object[] { parameterName,
                    serialRreader,
                    new Long (length) });
        }
        catch (java.io.IOException e) {
            throw new SQLException (e.getMessage ());
        }
    }
    
    //@PDA jdbc40
    public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException
    {
        try {
            SerializableReader reader;
            if (value == null)
                reader = null;
            else
                reader = new SerializableReader (value, (int) Math.max(0,length));
            callMethod ("setNCharacterStream",
                    new Class[] { String.class, Reader.class,
                    Long.TYPE },
                    new Object[] { parameterName,
                    reader,
                    new Long(length) });
        }
        catch (java.io.IOException e) {
            throw new SQLException (e.getMessage ());
        }
    }
    
    //@PDA jdbc40
    public void setNClob(String parameterName, NClob value) throws SQLException
    {
        if (value != null &&
                !(value instanceof Serializable) ){
            if (JDTrace.isTraceOn())
                JDTrace.logInformation (this, NOT_SERIALIZABLE);
            throw new SQLException ();
        }
        
        callMethod ("setNClob",
                new Class[] { String.class, NClob.class },
                new Object[] { parameterName,
                value });
    }
    
    //@PDA jdbc40
    public void setNClob(String parameterName, Reader reader, long length) throws SQLException
    {
        try {
            SerializableReader serialRreader;
            if (reader == null)
                serialRreader = null;
            else
                serialRreader = new SerializableReader (reader, (int) Math.max(0,length));
            callMethod ("setNClob",
                    new Class[] { String.class, Reader.class,
                    Long.TYPE },
                    new Object[] { parameterName,
                    serialRreader,
                    new Long (length) });
        }
        catch (java.io.IOException e) {
            throw new SQLException (e.getMessage ());
        }
    }
    
    //@PDA jdbc40
    public void setNString(String parameterName, String value) throws SQLException
    {
        callMethod ("setNString",
                new Class[] { String.class, String.class },
                new Object[] { parameterName, value });          
    }
    
    //@PDA jdbc40
    public void setRowId(String parameterName, RowId x) throws SQLException
    {
        callMethod ("setRowId",
                new Class[] { String.class, RowId.class},
                new Object[] { parameterName, x });  
    }
    
    //@PDA jdbc40
    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException
    {
        if (xmlObject != null &&
                !(xmlObject instanceof Serializable) ){
            if (JDTrace.isTraceOn())
                JDTrace.logInformation (this, NOT_SERIALIZABLE);
            throw new SQLException ();
        }
        
        callMethod ("setSQLXML",
                new Class[] { String.class, SQLXML.class },
                new Object[] { parameterName,
                xmlObject });
    }


    public void setAsciiStream(String parameterName, InputStream x) throws SQLException
    {
        InputStream iStream;
        if (x == null ||
                x instanceof Serializable)
            iStream = x;
        else {
            try {
                iStream = new SerializableInputStream (x);
            }
            catch (java.io.IOException e) {
                throw new SQLException (e.getMessage ());
            }
        }
        callMethod ("setAsciiStream",
                new Class[] { String.class, InputStream.class },
                new Object[] { parameterName,
                iStream });
    }


    public void setBinaryStream(String parameterName, InputStream x) throws SQLException
    {
        InputStream iStream;
        if (x == null ||
                x instanceof Serializable)
            iStream = x;
        else {
            try {
                iStream = new SerializableInputStream (x);
            }
            catch (java.io.IOException e) {
                throw new SQLException (e.getMessage ());
            }
        }
        callMethod ("setBinaryStream",
                new Class[] { String.class, InputStream.class },
                new Object[] { parameterName,
                iStream });
    }


    public void setBlob(String parameterName, InputStream inputStream) throws SQLException
    {
        InputStream iStream;
        if (inputStream == null ||
                inputStream instanceof Serializable)
            iStream = inputStream;
        else {
            try {
                iStream = new SerializableInputStream (inputStream);
            }
            catch (java.io.IOException e) {
                throw new SQLException (e.getMessage ());
            }
        }
        callMethod ("setBlob",
                new Class[] { String.class, InputStream.class },
                new Object[] { parameterName,
                iStream });
    }


    public void setCharacterStream(String parameterName, Reader reader) throws SQLException
    {
        try {
            SerializableReader serialRreader;
            if (reader == null)
                serialRreader = null;
            else
                serialRreader = new SerializableReader (reader);
            callMethod ("setCharacterStream",
                    new Class[] { String.class, Reader.class },
                    new Object[] { parameterName,
                    serialRreader });
        }
        catch (java.io.IOException e) {
            throw new SQLException (e.getMessage ());
        }    
    }


    public void setClob(String parameterName, Reader reader) throws SQLException
    {
        try {
            SerializableReader serialRreader;
            if (reader == null)
                serialRreader = null;
            else
                serialRreader = new SerializableReader (reader);
            callMethod ("setClob",
                    new Class[] { String.class, Reader.class },
                    new Object[] { parameterName,
                    serialRreader });
        }
        catch (java.io.IOException e) {
            throw new SQLException (e.getMessage ());
        }    
    }


    public void setNCharacterStream(String parameterName, Reader value) throws SQLException
    {
        try {
            SerializableReader serialRreader;
            if (value == null)
                serialRreader = null;
            else
                serialRreader = new SerializableReader (value);
            callMethod ("setNCharacterStream",
                    new Class[] { String.class, Reader.class },
                    new Object[] { parameterName,
                    serialRreader });
        }
        catch (java.io.IOException e) {
            throw new SQLException (e.getMessage ());
        }    
    }


    public void setNClob(String parameterName, Reader reader) throws SQLException
    {
        try {
            SerializableReader serialRreader;
            if (reader == null)
                serialRreader = null;
            else
                serialRreader = new SerializableReader (reader);
            callMethod ("setNClob",
                    new Class[] { String.class, Reader.class },
                    new Object[] { parameterName,
                    serialRreader });
        }
        catch (java.io.IOException e) {
            throw new SQLException (e.getMessage ());
        }    
    }
    
}
