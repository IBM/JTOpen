///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDPreparedStatementProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.InvocationTargetException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;            
import java.sql.Array;
import java.sql.BatchUpdateException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.DataTruncation;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Enumeration;



class JDPreparedStatementProxy
extends JDStatementProxy
implements PreparedStatement
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


  // Copied from JDError:
  private static final String EXC_FUNCTION_NOT_SUPPORTED       = "IM001";


  private final static String NOT_SERIALIZABLE = "Parameter is not serializable.";
  

  public JDPreparedStatementProxy (JDConnectionProxy jdConnection)
  {
    super (jdConnection);
  }


// JDBC 2.0
    public void addBatch ()
        throws SQLException
    {
      callMethod ("addBatch");
    }



    public void clearParameters ()
      throws SQLException
    {
      callMethod ("clearParameters");
    }


    public boolean execute ()
      throws SQLException
    {
      cachedResultSet_ = null;
      return callMethodRtnBool ("execute");
    }


    public ResultSet executeQuery ()
        throws SQLException
    {
      cachedResultSet_ = null;
      try {
        JDResultSetProxy newResultSet = new JDResultSetProxy (jdConnection_, this);
        cachedResultSet_ = (JDResultSetProxy) connection_.callFactoryMethod (
                                         pxId_, "executeQuery", newResultSet);
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
      return cachedResultSet_;
    }



    public int executeUpdate ()
      throws SQLException
    {
      cachedResultSet_ = null;
      return callMethodRtnInt ("executeUpdate");
    }



// JDBC 2.0
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



// JDBC 3.0
    public ParameterMetaData getParameterMetaData ()
		throws SQLException
    {
        // Avoid dragging in JDError
        //@K1D throw new SQLException (
        //@K1D                       AS400JDBCDriver.getResource("JD" + EXC_FUNCTION_NOT_SUPPORTED),
        //@K1D                       EXC_FUNCTION_NOT_SUPPORTED, -99999);
        try {    //@K1A
        JDParameterMetaDataProxy newMetaData = new JDParameterMetaDataProxy (jdConnection_);
        return (JDParameterMetaDataProxy) connection_.callFactoryMethod (pxId_, "getParameterMetaData", newMetaData);
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
    }




// JDBC 2.0
    public void setArray (int parameterIndex, Array parameterValue)
      throws SQLException
    {
      if (parameterValue != null &&
          !(parameterValue instanceof Serializable) ){
        if (JDTrace.isTraceOn())
          JDTrace.logInformation (this, NOT_SERIALIZABLE);
        throw new SQLException ();
      }

      callMethod ("setArray",
                  new Class[] { Integer.TYPE, Array.class },
                  new Object[] { new Integer (parameterIndex),
                                 parameterValue });
    }



    public void setAsciiStream (int parameterIndex,
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
      callMethod ("setAsciiStream",
                  new Class[] { Integer.TYPE, InputStream.class,
                                Integer.TYPE },
                  new Object[] { new Integer (parameterIndex),
                                 iStream,
                                 new Integer (length) });
    }



    public void setBigDecimal (int parameterIndex, BigDecimal parameterValue)
        throws SQLException
    {
      callMethod ("setBigDecimal", 
                  new Class[] { Integer.TYPE, BigDecimal.class },
                  new Object[] { new Integer (parameterIndex),
                                 parameterValue });
    }



    public void setBinaryStream (int parameterIndex,
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
                  new Class[] { Integer.TYPE, InputStream.class,
                                Integer.TYPE },
                  new Object[] { new Integer (parameterIndex),
                                 iStream,
                                 new Integer (length) });
    }



// JDBC 2.0
    public void setBlob (int parameterIndex, Blob parameterValue)
        throws SQLException
    {
      if (parameterValue != null &&
          !(parameterValue instanceof Serializable) ){
        if (JDTrace.isTraceOn())
          JDTrace.logInformation (this, NOT_SERIALIZABLE);
        throw new SQLException ();
      }

      callMethod ("setBlob",
                  new Class[] { Integer.TYPE, Blob.class },
                  new Object[] { new Integer (parameterIndex),
                                 parameterValue });
    }



    public void setBoolean (int parameterIndex, boolean parameterValue)
        throws SQLException
    {
      callMethod ("setBoolean",
                  new Class[] { Integer.TYPE, Boolean.TYPE },
                  new Object[] { new Integer (parameterIndex),
                                 new Boolean (parameterValue) });
    }



    public void setByte (int parameterIndex, byte parameterValue)
        throws SQLException
    {
      callMethod ("setByte",
                  new Class[] { Integer.TYPE, Byte.TYPE },
                  new Object[] { new Integer (parameterIndex),
                                 new Byte (parameterValue) });
    }



    public void setBytes (int parameterIndex, byte[] parameterValue)
        throws SQLException
    {
      callMethod ("setBytes",
                  new Class[] { Integer.TYPE, byte[].class },
                  new Object[] { new Integer (parameterIndex),
                                 parameterValue });
    }



// JDBC 2.0
    public void setCharacterStream (int parameterIndex,
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
                    new Class[] { Integer.TYPE, Reader.class,
                                  Integer.TYPE },
                    new Object[] { new Integer (parameterIndex),
                                   reader,
                                   new Integer (length) });
      }
      catch (java.io.IOException e) {
        throw new SQLException (e.getMessage ());
      }
    }



// JDBC 2.0
    public void setClob (int parameterIndex, Clob parameterValue)
        throws SQLException
    {
      if (parameterValue != null &&
          !(parameterValue instanceof Serializable) ){
        if (JDTrace.isTraceOn())
          JDTrace.logInformation (this, NOT_SERIALIZABLE);
        throw new SQLException ();
      }

      callMethod ("setClob",
                  new Class[] { Integer.TYPE, Clob.class },
                  new Object[] { new Integer (parameterIndex),
                                 parameterValue });
    }



    public void setDate (int parameterIndex, Date parameterValue)
        throws SQLException
    {
      callMethod ("setDate",
                  new Class[] { Integer.TYPE, Date.class },
                  new Object[] { new Integer (parameterIndex),
                                 parameterValue });
    }



// JDBC 2.0
    public void setDate (int parameterIndex,
                         Date parameterValue,
                         Calendar calendar)
        throws SQLException
    {
      callMethod ("setDate",
                  new Class[] { Integer.TYPE, Date.class,
                                Calendar.class },
                  new Object[] { new Integer (parameterIndex),
                                 parameterValue,
                                 calendar });
    }



    public void setDouble (int parameterIndex, double parameterValue)
        throws SQLException
    {
      callMethod ("setDouble",
                  new Class[] { Integer.TYPE, Double.TYPE },
                  new Object[] { new Integer (parameterIndex),
                                 new Double (parameterValue) });
    }



    public void setFloat (int parameterIndex, float parameterValue)
        throws SQLException
    {
      callMethod ("setFloat",
                  new Class[] { Integer.TYPE, Float.TYPE },
                  new Object[] { new Integer (parameterIndex),
                                 new Float (parameterValue) });
    }



    public void setInt (int parameterIndex, int parameterValue)
        throws SQLException
    {
      callMethod ("setInt",
                  new Class[] { Integer.TYPE, Integer.TYPE },
                  new Object[] { new Integer (parameterIndex),
                                 new Integer (parameterValue) });
    }



    public void setLong (int parameterIndex, long parameterValue)
        throws SQLException
    {
      callMethod ("setLong",
                  new Class[] { Integer.TYPE, Long.TYPE },
                  new Object[] { new Integer (parameterIndex),
                                 new Long (parameterValue) });
    }



    public void setNull (int parameterIndex, int sqlType)
      throws SQLException
    {
      callMethod ("setNull",
                  new Class[] { Integer.TYPE, Integer.TYPE },
                  new Object[] { new Integer (parameterIndex),
                                 new Integer (sqlType) });
    }


    public void setNull (int parameterIndex, int sqlType, String typeName)
      throws SQLException
    {
      callMethod ("setNull",
                  new Class[] { Integer.TYPE, Integer.TYPE,
                                String.class },
                  new Object[] { new Integer (parameterIndex),
                                 new Integer (sqlType),
                                 typeName});
    }

    public void setObject (int parameterIndex, Object parameterValue)
        throws SQLException
    {
      if (parameterValue != null &&
          !(parameterValue instanceof Serializable) ){
        if (JDTrace.isTraceOn())
          JDTrace.logInformation (this, NOT_SERIALIZABLE);
        throw new SQLException ();
      }

      callMethod ("setObject",
                  new Class[] { Integer.TYPE, Object.class },
                  new Object[] { new Integer (parameterIndex),
                                 parameterValue });
    }



    public void setObject (int parameterIndex,
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
                  new Class[] { Integer.TYPE, Object.class,
                                Integer.TYPE },
                  new Object[] { new Integer (parameterIndex),
                                 parameterValue,
                                 new Integer (sqlType) });
    }



    public void setObject (int parameterIndex,
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
                  new Class[] { Integer.TYPE, Object.class,
                                Integer.TYPE, Integer.TYPE },
                  new Object[] { new Integer (parameterIndex),
                                 parameterValue,
                                 new Integer (sqlType),
                                 new Integer (scale) });
    }



// JDBC 2.0
    public void setRef (int parameterIndex, Ref parameterValue)
        throws SQLException
    {
      if (parameterValue != null &&
          !(parameterValue instanceof Serializable) ){
        if (JDTrace.isTraceOn())
          JDTrace.logInformation (this, NOT_SERIALIZABLE);
        throw new SQLException ();
      }

      callMethod ("setRef",
                  new Class[] { Integer.TYPE, Ref.class },
                  new Object[] { new Integer (parameterIndex),
                                 parameterValue });
    }



    public void setShort (int parameterIndex, short parameterValue)
        throws SQLException
    {
      callMethod ("setShort",
                  new Class[] { Integer.TYPE, Short.TYPE },
                  new Object[] { new Integer (parameterIndex),
                                 new Short (parameterValue) });
    }



    public void setString (int parameterIndex, String parameterValue)
        throws SQLException
    {
      callMethod ("setString",
                  new Class[] { Integer.TYPE, String.class },
                  new Object[] { new Integer (parameterIndex),
                                 parameterValue });
    }



    public void setTime (int parameterIndex, Time parameterValue)
        throws SQLException
    {
      callMethod ("setTime",
                  new Class[] { Integer.TYPE, Time.class },
                  new Object[] { new Integer (parameterIndex),
                                 parameterValue });
    }



// JDBC 2.0
    public void setTime (int parameterIndex,
                         Time parameterValue,
                         Calendar calendar)
        throws SQLException
    {
      callMethod ("setTime",
                  new Class[] { Integer.TYPE, Time.class,
                                Calendar.class },
                  new Object[] { new Integer (parameterIndex),
                                 parameterValue, calendar });
    }



    public void setTimestamp (int parameterIndex, Timestamp parameterValue)
        throws SQLException
    {
      callMethod ("setTimestamp",
                  new Class[] { Integer.TYPE, Timestamp.class },
                  new Object[] { new Integer (parameterIndex),
                                 parameterValue });
    }



// JDBC 2.0
    public void setTimestamp (int parameterIndex,
                              Timestamp parameterValue,
                              Calendar calendar)
        throws SQLException
    {
      callMethod ("setTimestamp",
                  new Class[] { Integer.TYPE, Timestamp.class,
                                Calendar.class },
                  new Object[] { new Integer (parameterIndex),
                                 parameterValue, calendar });
    }



/**
@exception  SQLException    If a SQL error occurs.
@deprecated Use setCharacterStream(int, Reader, int) instead.
@see #setCharacterStream
**/
    public void setUnicodeStream (int parameterIndex,
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
      callMethod ("setUnicodeStream",
                  new Class[] { Integer.TYPE, InputStream.class,
                                Integer.TYPE },
                  new Object[] { new Integer (parameterIndex),
                                 iStream,
                                 new Integer (length) });
    }


// JDBC 3.0
    public void setURL (int parameterIndex,
                              URL parameterValue)
        throws SQLException
    {
      callMethod ("setURL",
                  new Class[] { Integer.TYPE, URL.class},
                  new Object[] { new Integer (parameterIndex),
                                 parameterValue });
    }


}
