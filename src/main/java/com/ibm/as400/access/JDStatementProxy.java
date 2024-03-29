///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDStatementProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2010 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;


class JDStatementProxy
extends AbstractProxyImpl
implements java.sql.Statement
{
  static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


  // Protected data.
  protected JDConnectionProxy       jdConnection_;
         // The object that caused this object to be created.

  protected JDResultSetProxy        cachedResultSet_;

    // Copied from JDError:
    private static final String EXC_FUNCTION_NOT_SUPPORTED       = "IM001";




  public JDStatementProxy (JDConnectionProxy jdConnection)
  {
    jdConnection_ = jdConnection;
  }


// JDBC 2.0
    public void addBatch (String sql)
        throws SQLException
    {
      callMethod ("addBatch",
                  new Class[] { String.class },
                  new Object[] { sql });
    }


  // Call a method.  No return value is expected.
  protected void callMethod (String methodName)
    throws SQLException
  {
    try {
      connection_.callMethod (pxId_, methodName);
    }
    catch (InvocationTargetException e) {
      throw JDConnectionProxy.rethrow1 (e);
    }
  }


  protected void callMethod (String methodName,
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
  protected boolean callMethodRtnBool (String methodName)
    throws SQLException
  {
    try {
      return connection_.callMethodReturnsBoolean (pxId_, methodName);
    }
    catch (InvocationTargetException e) {
      throw JDConnectionProxy.rethrow1 (e);
    }
  }

  // Call a method, and return an int.
  protected int callMethodRtnInt (String methodName)
    throws SQLException
  {
    try {
      return connection_.callMethodReturnsInt (pxId_, methodName);
    }
    catch (InvocationTargetException e) {
      throw JDConnectionProxy.rethrow1 (e);
    }
  }
  // Call a method, and return an int.
  protected long callMethodRtnLong (String methodName)
    throws SQLException
  {
    try {
      return connection_.callMethodReturnsLong (pxId_, methodName);
    }
    catch (InvocationTargetException e) {
      throw JDConnectionProxy.rethrow1 (e);
    }
  }

  
  // Call a method, and return an Object.
  protected Object callMethodRtnObj (String methodName)
    throws SQLException
  {
    try {
      return connection_.callMethodReturnsObject (pxId_, methodName);
    }
    catch (InvocationTargetException e) {
      throw JDConnectionProxy.rethrow1 (e);
    }
  }

  // Call a method, and return a 'raw' ProxyReturnValue.
  protected ProxyReturnValue callMethodRtnRaw (String methodName,
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



    public void cancel ()
      throws SQLException
    {
      cachedResultSet_ = null;
      callMethod ("cancel");
    }



// JDBC 2.0
    public void clearBatch ()
        throws SQLException
    {
      callMethod ("clearBatch");
    }



    public void clearWarnings ()
      throws SQLException
    {
      callMethod ("clearWarnings");
    }



    public void close ()
      throws SQLException
    {
      cachedResultSet_ = null; 
      try { 
        callMethod ("close");
      } catch (ProxyException pe) { 
        String info = pe.toString(); 
        if (info.indexOf("dropped") >= 0) {
          // just ignore
        } else { 
          throw pe; 
        }
      }
    }


    public boolean execute (String sql)
      throws SQLException
    {
      cachedResultSet_ = null;
      return callMethodRtnRaw ("execute",
                               new Class[] { String.class },
                               new Object[] { sql })
        .getReturnValueBoolean ();
    }



    // JDBC 3.0
    public boolean execute (String sql, int autoGeneratedKeys)
    throws SQLException
    {
        cachedResultSet_ = null;
        return callMethodRtnRaw ("execute",
                                 new Class[] { String.class, Integer.TYPE},
                                 new Object[] { sql, Integer.valueOf(autoGeneratedKeys)})
        .getReturnValueBoolean ();
    }



    // JDBC 3.0
    public boolean execute (String sql, int[] columnIndexes)
    throws SQLException
    {
        cachedResultSet_ = null;
        // Avoid dragging in JDError
        throw new SQLException (
                               AS400JDBCDriver.getResource("JD" + EXC_FUNCTION_NOT_SUPPORTED,null),
                               EXC_FUNCTION_NOT_SUPPORTED, -99999);
    }



    // JDBC 3.0
    public boolean execute (String sql, String[] columnNames)
    throws SQLException
    {
        cachedResultSet_ = null;
        // Avoid dragging in JDError
        throw new SQLException (
                               AS400JDBCDriver.getResource("JD" + EXC_FUNCTION_NOT_SUPPORTED,null),
                               EXC_FUNCTION_NOT_SUPPORTED, -99999);
    }



// JDBC 2.0
    public int[] executeBatch ()
        throws SQLException
    {
      cachedResultSet_ = null;
      return (int[]) callMethodRtnObj ("executeBatch");
    }



    public ResultSet executeQuery (String sql)
        throws SQLException
    {
      cachedResultSet_ = null;
      try {
        JDResultSetProxy newResultSet = new JDResultSetProxy (jdConnection_, this);
        cachedResultSet_ = (JDResultSetProxy) connection_.callFactoryMethod (
                                            pxId_,
                                            "executeQuery",
                                            new Class[] { String.class },
                                            new Object[] { sql },
                                            newResultSet);
        return cachedResultSet_;
      }
      catch (InvocationTargetException e) {
        throw JDConnectionProxy.rethrow1 (e);
      }
    }



    public int executeUpdate (String sql)
      throws SQLException
    {
      cachedResultSet_ = null;
      return callMethodRtnRaw ("executeUpdate",
                               new Class[] { String.class },
                               new Object[] { sql })
        .getReturnValueInt ();
    }



    // JDBC 3.0
    public int executeUpdate (String sql, int autoGeneratedKeys)
    throws SQLException
    {
        cachedResultSet_ = null;
        return callMethodRtnRaw ("executeUpdate",
                                 new Class[] { String.class, Integer.TYPE},
                                 new Object[] { sql, Integer.valueOf(autoGeneratedKeys)})
        .getReturnValueInt ();
    }



    // JDBC 3.0
    public int executeUpdate (String sql, int[] columnIndexes)
    throws SQLException
    {
        cachedResultSet_ = null;
        // Avoid dragging in JDError
        throw new SQLException (
                               AS400JDBCDriver.getResource("JD" + EXC_FUNCTION_NOT_SUPPORTED,null),
                               EXC_FUNCTION_NOT_SUPPORTED, -99999);
    }



    // JDBC 3.0
    public int executeUpdate (String sql, String[] columnNames)
    throws SQLException
    {
        cachedResultSet_ = null;
        // Avoid dragging in JDError
        throw new SQLException (
                               AS400JDBCDriver.getResource("JD" + EXC_FUNCTION_NOT_SUPPORTED,null),
                               EXC_FUNCTION_NOT_SUPPORTED, -99999);
    }

 




// JDBC 2.0
    public Connection getConnection ()
    {
      return jdConnection_;
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



    // JDBC 3.0
    public ResultSet getGeneratedKeys ()
    throws SQLException
    {
        cachedResultSet_ = null;
        try {
            JDResultSetProxy newResultSet = new JDResultSetProxy (jdConnection_, this);
            cachedResultSet_ = (JDResultSetProxy) connection_.callFactoryMethod (
                                                                                pxId_,
                                                                                "getGeneratedKeys",
                                                                                newResultSet);
            return cachedResultSet_;
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }



    public int getMaxFieldSize ()
        throws SQLException
    {
      return callMethodRtnInt ("getMaxFieldSize");
    }



    public int getMaxRows ()
        throws SQLException
    {
      return callMethodRtnInt ("getMaxRows");
    }



    public boolean getMoreResults ()
      throws SQLException
    {
      cachedResultSet_ = null;
      return callMethodRtnBool ("getMoreResults");
    }



    // JDBC 3.0
    public boolean getMoreResults (int current)
    throws SQLException
    {
        cachedResultSet_ = null;
        // Avoid dragging in JDError
        throw new SQLException (
                               AS400JDBCDriver.getResource("JD" + EXC_FUNCTION_NOT_SUPPORTED,null),
                               EXC_FUNCTION_NOT_SUPPORTED, -99999);
    }



    public int getQueryTimeout ()
      throws SQLException
    {
      return callMethodRtnInt ("getQueryTimeout");
    }



    public ResultSet getResultSet ()
      throws SQLException
    {
      if (cachedResultSet_ == null)
      {
        try {
          JDResultSetProxy newResultSet = new JDResultSetProxy (jdConnection_, this);
          cachedResultSet_ = (JDResultSetProxy) connection_.callFactoryMethod (
                                       pxId_, "getResultSet", newResultSet);
        }
        catch (InvocationTargetException e) {
          throw JDConnectionProxy.rethrow1 (e);
        }
      }
      return cachedResultSet_;
    }



// JDBC 2.0
    public int getResultSetConcurrency ()
        throws SQLException
    {
      return callMethodRtnInt ("getResultSetConcurrency");
    }



    // JDBC 3.0
    public int getResultSetHoldability ()
    throws SQLException
    {
        return callMethodRtnInt ("getResultSetHoldability");
    }




// JDBC 2.0
    public int getResultSetType ()
        throws SQLException
    {
      return callMethodRtnInt ("getResultSetType");
    }



    public int getUpdateCount ()
      throws SQLException
    {
      return callMethodRtnInt ("getUpdateCount");
    }



    public SQLWarning getWarnings ()
      throws SQLException
    {
      return (SQLWarning) callMethodRtnObj ("getWarnings");
    }


    public void setCursorName (String cursorName)
      throws SQLException
    {
      cachedResultSet_ = null;
      callMethod ("setCursorName",
                  new Class[] { String.class },
                  new Object[] { cursorName });
    }



    public void setEscapeProcessing (boolean escapeProcessing)
      throws SQLException
    {
      callMethod ("setEscapeProcessing",
                  new Class[] { Boolean.TYPE },
                  new Object[] { Boolean.valueOf(escapeProcessing) });
    }



// JDBC 2.0
    public void setFetchDirection (int fetchDirection)
        throws SQLException
    {
      callMethod ("setFetchDirection",
                  new Class[] { Integer.TYPE },
                  new Object[] { Integer.valueOf(fetchDirection) });
    }



// JDBC 2.0
    public void setFetchSize (int fetchSize)
        throws SQLException
    {
      callMethod ("setFetchSize",
                  new Class[] { Integer.TYPE },
                  new Object[] { Integer.valueOf(fetchSize) });
    }



    public void setMaxFieldSize (int maxFieldSize)
      throws SQLException
    {
      callMethod ("setMaxFieldSize",
                  new Class[] { Integer.TYPE },
                  new Object[] { Integer.valueOf(maxFieldSize) });
    }


    public void setMaxRows (int maxRows)
      throws SQLException
    {
      callMethod ("setMaxRows",
                  new Class[] { Integer.TYPE },
                  new Object[] { Integer.valueOf(maxRows) });
    }



    public void setQueryTimeout (int queryTimeout)
        throws SQLException
    {
      callMethod ("setQueryTimeout",
                  new Class[] { Integer.TYPE },
                  new Object[] { Integer.valueOf(queryTimeout) });
    }


    // This method is not required by java.sql.Statement,
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
     
    //@pda jdbc40
    protected String[] getValidWrappedList()
    {
        return new String[] {  "java.sql.Statement" };
    } 
    
    //@PDA jdbc40
    public boolean isClosed () throws SQLException
    {
        return callMethodRtnBool ("isClosed");
    }
    
    //@PDA jdbc40
    public void setPoolable(boolean poolable) throws SQLException
    {
        callMethod ("setPoolable",
                new Class[] { Boolean.TYPE },
                new Object[] { Boolean.valueOf(poolable) });  
    }
    
    //@PDA jdbc40
    public boolean isPoolable() throws SQLException
    {
        return callMethodRtnBool ("isPoolable");
    }
    
    // JDBC 4.1
    public void closeOnCompletion() throws SQLException {
      callMethod("closeOnCompletion"); 
      
    }

    // JDC 4.1
    public boolean isCloseOnCompletion() throws SQLException {
      return callMethodRtnBool("isCloseOnCompletion"); 
    }

    public   long getLargeUpdateCount() throws SQLException {
      return callMethodRtnLong("getLargeUpdateCount"); 
    }

    public void setLargeMaxRows(long max) throws SQLException {
      callMethod ("setLargeMaxRows",
          new Class[] { Long.TYPE },
          new Object[] { Long.valueOf(max)});  
    }

    public long getLargeMaxRows() throws SQLException {
      return callMethodRtnLong("getLargeMaxRows"); 
    }

    
    
    public long[] executeLargeBatch()    throws SQLException  {
      return (long []) callMethodRtnObj("executeLargeBatch"); 
    }

   public long executeLargeUpdate(String sql)   throws SQLException {

     cachedResultSet_ = null;
     return callMethodRtnRaw ("executeLargeUpdate",
                              new Class[] { String.class },
                              new Object[] { sql })
       .getReturnValueLong ();
   }


    public long executeLargeUpdate(String sql,
                                    int autoGeneratedKeys)
                             throws SQLException {

      cachedResultSet_ = null;
      return callMethodRtnRaw ("executeLargeUpdate",
                               new Class[] { String.class, Integer.TYPE },
                               new Object[] { sql, Integer.valueOf(autoGeneratedKeys) })
        .getReturnValueLong ();
      
    }


    public long executeLargeUpdate(String sql,
                                    int[] columnIndexes) 
                             throws SQLException {
      cachedResultSet_ = null;
      // Avoid dragging in JDError
      throw new SQLException (
                             AS400JDBCDriver.getResource("JD" + EXC_FUNCTION_NOT_SUPPORTED,null),
                             EXC_FUNCTION_NOT_SUPPORTED, -99999);

    
    }

    public long executeLargeUpdate(String sql,
                                    String[] columnNames)  throws SQLException {
      
      cachedResultSet_ = null;
      // Avoid dragging in JDError
      throw new SQLException (
                             AS400JDBCDriver.getResource("JD" + EXC_FUNCTION_NOT_SUPPORTED,null),
                             EXC_FUNCTION_NOT_SUPPORTED, -99999);
      
    }



    
    
    
    
}
