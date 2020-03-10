///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: JDError.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2010 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/* ifdef JDBC40
import java.sql.ClientInfoStatus;
import java.sql.SQLClientInfoException;
import java.sql.SQLDataException;
endif */
import java.sql.SQLException;
/* ifdef JDBC40
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransactionRollbackException;
import java.util.Map;
endif */

import java.sql.SQLWarning;



/**
The JDError class provides access to all of the error and
warning SQL states and message string resources.
**/
//
// Implementation note:
//
// When adding or changing an error message or SQL state, there
// are places that you need to add or change:
//
// 1. A String constant defined to be equal to the SQL state.
// 2. A resource in JDMRI.properties, named JD + SQL state.
//
final class JDError
{
  // Constants for SQL states.
  static final String EXC_ACCESS_MISMATCH              = "42505";
  static final String EXC_ATTRIBUTE_VALUE_INVALID      = "HY024";
  static final String EXC_BUFFER_LENGTH_INVALID        = "HY090";
  static final String EXC_CHAR_CONVERSION_INVALID      = "22524";
  static final String EXC_CCSID_INVALID                = "22522";
  static final String EXC_COLUMN_NOT_FOUND             = "42703";
  static final String EXC_CONCURRENCY_INVALID          = "HY108";
  static final String EXC_CONNECTION_NONE              = "08003";
  static final String EXC_CONNECTION_REJECTED          = "08004";
  static final String EXC_CONNECTION_UNABLE            = "08001";
  static final String EXC_COMMUNICATION_LINK_FAILURE   = "08S01"; // @F2A
  static final String EXC_CURSOR_NAME_AMBIGUOUS        = "3C000";
  static final String EXC_CURSOR_NAME_INVALID          = "34000";
  static final String EXC_CURSOR_POSITION_INVALID      = "HY109";
  static final String EXC_CURSOR_STATE_INVALID         = "24000";
  static final String EXC_DATA_TYPE_INVALID            = "HY004";
  static final String EXC_DATA_TYPE_MISMATCH           = "07006";
  static final String EXC_DESCRIPTOR_INDEX_INVALID     = "07009";
  static final String EXC_FUNCTION_NOT_SUPPORTED       = "IM001";
  static final String EXC_FUNCTION_SEQUENCE            = "HY010";
  static final String EXC_INTERNAL                     = "HY000";
  static final String EXC_MAX_STATEMENTS_EXCEEDED      = "HY014";
  static final String EXC_OPERATION_CANCELLED          = "HY008";
  static final String EXC_PARAMETER_COUNT_MISMATCH     = "07001";
  static final String EXC_PARAMETER_TYPE_INVALID       = "HY105";
  static final String EXC_SCALE_INVALID                = "HY094";
  static final String EXC_SERVER_ERROR                 = "HY001";
  static final String EXC_SYNTAX_BLANK                 = "43617";
  static final String EXC_SYNTAX_ERROR                 = "42601";
  static final String EXC_TXN_STATE_INVALID            = "25000"; // @E1C
  static final String EXC_SQL_STATEMENT_TOO_LONG       = "54001"; // @E9A
  static final String EXC_SAVEPOINT_INVALID_IN_CONTEXT = "3B001"; // @E10a
  static final String EXC_SAVEPOINT_ALREADY_EXISTS     = "3B501"; // @E10a
  static final String EXC_SAVEPOINT_DOES_NOT_EXIST     = "3B502"; // @E10a
  static final String EXC_RDB_DOES_NOT_EXIST           = "42705"; // @J2a
  static final String EXC_XML_PARSING_ERROR            = "2200M"; // @xml2
  static final String EXC_CONNECTION_REESTABLISHED     = "08506"; 
  static final String WARN_ATTRIBUTE_VALUE_CHANGED     = "01608";
  static final String WARN_EXTENDED_DYNAMIC_DISABLED   = "01H11";
  static final String WARN_OPTION_VALUE_CHANGED        = "01S02";
  static final String WARN_PACKAGE_CACHE_DISABLED      = "01H12";
  static final String WARN_PROPERTY_EXTRA_IGNORED      = "01H20";
  static final String WARN_TXN_COMMITTED               = "01H30";
  static final String WARN_URL_EXTRA_IGNORED           = "01H10";
  static final String WARN_URL_SCHEMA_INVALID          = "01H13";
  static final String WARN_1000_OPEN_STATEMENTS        = "01G00";   //@K1A



  // Remove static variable that is corrupted when multiple
  // threads are running @Q4D
  // static String       lastServerSQLState_             = null;

  static boolean jdk14 = false;
  static {
    jdk14 = JVMInfo.isJDK14();
  }

/**
Private constructor to prevent instantiation.  All methods in
this class are static.
**/
  private JDError ()
  {
  }



/**
Returns the reason text based on a SQL state.

@param  sqlState    the SQL State.
@param  replacementText Strings to use for the replacement text
@return             Reason - error description.
**/
  
  static final String getReason (String sqlState, String[] replacementVariables)
  {
    return AS400JDBCDriver.getResource ("JD" + sqlState, replacementVariables);
  }



// @E2C
/**
Returns the message text for the last operation on the IBM i system.

@param  connection  Connection to the system.
@param  id          Id for the last operation.
@param  returnCode  The return code from the last operation.
@return             Reason - error description.
@throws  SQLException  Throws an exception if the connection dropped and we reconnected
**/
  private static String getReason (AS400JDBCConnection connection,
                                   int id,
                                   int returnCode)  throws SQLException           // @E2A
  {
    try
    {
      // Check to see if the caller wants second level text, too.
      boolean secondLevelText = connection.getProperties().equals (JDProperties.ERRORS, JDProperties.ERRORS_FULL);

      // Get the message text from the system.  We also retrieve
      // the SQL state at this time to save a trip to the system.
      int orsBitmap = DBBaseRequestDS.ORS_BITMAP_RETURN_DATA
                      + DBBaseRequestDS.ORS_BITMAP_SQLCA
                      + DBBaseRequestDS.ORS_BITMAP_MESSAGE_ID
                      + DBBaseRequestDS.ORS_BITMAP_FIRST_LEVEL_TEXT;
      if (secondLevelText)
        orsBitmap += DBBaseRequestDS.ORS_BITMAP_SECOND_LEVEL_TEXT;

      DBSQLResultSetDS request = null; //@P0A
      DBReplyRequestedDS reply = null; //@P0A
      try //@P0A
      {
        request = DBDSPool.getDBSQLResultSetDS ( //@P0C
                                                      DBSQLResultSetDS.FUNCTIONID_SEND_RESULTS_SET,
                                                      id, orsBitmap, 0);

        reply = connection.sendAndReceive (request, id); //@P0C
      DBReplySQLCA sqlca = reply.getSQLCA();                                                  // @E2A

      // Build up the error description.
      StringBuffer errorDescription = new StringBuffer ();
      errorDescription.append ("[");
      errorDescription.append (reply.getMessageId());
      errorDescription.append ("] ");

      // If pre-V5R4:
      // If the return code is +-438 (from an SQL stored procedure) or                                   @E4A
      // +-443 (from an external stored procedure) AND errd[3] is 0, then                                @E4A @E6C
      // an error was signalled by the stored procedure itself.                                          @E4A
      boolean textAppended = false;                                                                   // @E6A
      int absReturnCode = Math.abs(returnCode);                                                       // @E4A

        if (((absReturnCode == 438) || (absReturnCode == 443)) && // @E2A @E4C
                                                                // @E5C @E6C
            (connection.getVRM() < JDUtilities.vrm540)) // @25955a
        {
          try // @25955a
          {
            if (sqlca.getErrd(4) == 0) // @F1C // @E6A
            {
              if (absReturnCode == 438) // @E2A @E4C @E5C
              {
                String errorString = sqlca.getErrmc(connection
                    .getConverter()); 
                errorDescription.append(errorString); // @E2A @P0C
                textAppended = true; // @E8A
              } else if (absReturnCode == 443) // @E5A
              {
                errorDescription.append(sqlca.getErrmc(6,
                    connection.getConverter())); // @E5A @P0C
                textAppended = true; // @E8A
              }
            } // @E6A
          } catch (Exception e) { // In some circumstances the getErrmc() can
                                  // throw a NegativeArraySizeException or
                                  // ArrayIndexOutOfBoundsException. @25955a
            JDTrace.logException(null, e.getMessage(), e); // just trace it
          }
        }

      // Otherwise, get the text directly from the reply.                                             // @E6A
      if (textAppended == false)                                                                      // @E2A @E6C
      {
        errorDescription.append (reply.getFirstLevelMessageText());
        if (secondLevelText)
        {
          errorDescription.append (" ");
          errorDescription.append (reply.getSecondLevelMessageText ());
        }
      }                                                                                       // @E2A

      // Get the SQL state and remember it for the next
      // call to getSQLState().
      // Remember it for the connection @Q4A
      String lastSqlState = sqlca.getSQLState (connection.getConverter()); 
      connection.setLastServerSQLState(lastSqlState);                 // @E2C @P0C
      if (lastSqlState == null)
        connection.setLastServerSQLState(EXC_SERVER_ERROR);

      return errorDescription.toString ();
      }
      finally
      {
        if (request != null) { request.returnToPool(); request = null; }
        if (reply != null) { reply.returnToPool(); reply = null; }
      }
    }
    catch (DBDataStreamException e)
    {
      return getReason (EXC_INTERNAL, null);
    }
    catch (SQLException e)
    {
      String sqlState = e.getSQLState(); 
      // If we get an exception that we reconnected -- that superceeds all exceptions so we just 
      // sned it directly back. 
      if (sqlState.equals(EXC_CONNECTION_REESTABLISHED)) {
         throw e; 
      } else { 
        // We can get a connection does not exist error. If that is the case,
        // just bubble it back
        if (sqlState.equals(EXC_CONNECTION_NONE)) {
          return getReason(EXC_CONNECTION_NONE, null);
        } else {
          return getReason(EXC_INTERNAL, null);
        }
      }
    }
  }



/**
Returns the SQL state for the last operation on the IBM i system.

@param  connection  Connection to the system.
@param  id          Id for the last operation.
@return             The SQL state.
**/
  private static String getSQLState (AS400JDBCConnection connection,
                                     int id)
  {
    // If the SQL state was retrieved by a previous call to
    // getReason(), then use that.
	// Remember for the connection @Q4C
    String lastServerSQLState = connection.getLastServerSQLState(); 
    if (lastServerSQLState != null)
    {
     
      connection.setLastServerSQLState(null); 
      return lastServerSQLState; 
    }

    // Otherwise, go to the system to get it.
    try
    {
      int orsBitmap = DBBaseRequestDS.ORS_BITMAP_RETURN_DATA + DBBaseRequestDS.ORS_BITMAP_SQLCA;

      DBSQLResultSetDS request = null; //@P0A
      DBReplyRequestedDS reply = null; //@P0A
      try //@P0A
      {
        request = DBDSPool.getDBSQLResultSetDS (DBSQLResultSetDS.FUNCTIONID_SEND_RESULTS_SET, id, orsBitmap, 0); //@P0C

        reply = connection.sendAndReceive (request, id); //@P0C

      String sqlState = reply.getSQLCA ().getSQLState (connection.getConverter()); //@P0C

      if (sqlState == null)
        sqlState = EXC_SERVER_ERROR;

      return sqlState;
      }
      finally
      {
        if (request != null) { request.returnToPool(); request = null; }
        if (reply != null) { reply.returnToPool(); reply = null; }
      }
    }
    catch (DBDataStreamException e)
    {
      return EXC_INTERNAL;
    }
    catch (SQLException e)
    {
      return EXC_INTERNAL;
    }
  }



/**
Returns an SQL warning based on an error in the
error table.

@param  sqlState    The SQL State.
 * @return SQLWarning
**/
  public static SQLWarning getSQLWarning (String sqlState)
  {
    String reason = getReason(sqlState, null);
    int sqlCode = getSqlCode(sqlState); 
    SQLWarning warning = new SQLWarning (reason, sqlState, sqlCode);

    if (JDTrace.isTraceOn ())                                           // @J3a
    {
      // @J3a
      String message = "Posting warning, sqlState: " + sqlState        // @J3a
                       + " reason: " + reason         // @J3a
                       + " vendor code "+sqlCode;       // @J3a
      JDTrace.logException(null, message, warning);                    // @J3a
    }                                                                   // @J3a

    return warning;
  }



/**
Returns an SQL warning based on information
retrieved from the IBM i system.

@param  connection  connection to the system.
@param  id          id for the last operation.
@param  errorClass  error class from the system reply.
@param  returnCode  return code from the system reply.
 * @return SQLWarning
 * @throws SQLException  if connection was re-established 
**/
  public static SQLWarning getSQLWarning (AS400JDBCConnection connection,
                                          int id,
                                          int errorClass,
                                          int returnCode)
  throws SQLException 
  {
    String reason;
    if (returnCode != 0) { 
       reason = getReason (connection, id, returnCode);
    } else {
       // if the return code is zero, then there should not be a reason
      reason=""; 
    }
    String state  = getSQLState (connection, id);

    SQLWarning warning = new SQLWarning (reason, state, returnCode);   // @E2C

    if (JDTrace.isTraceOn ())                                           // @J3a
    {
      // @J3a
      String message = "Posting warning, id: " + id                    // @J3a
                       + " error class: " + errorClass           // @J3a
                       + " return code: " + returnCode           // @J3a
                       + " reason: "      + reason               // @J3a
                       + " state: "       + state;               // @J3a
      JDTrace.logException(connection, message, warning);              // @J3a
    }                                                                   // @J3a

    return warning;
  }




/**
Throws an SQL exception based on an error in the
error table.

@param  sqlState    The SQL State.

@exception          SQLException    Always.
**/
  public static void throwSQLException (String sqlState)
  throws SQLException
  {
     // @J4 - changed this method to call the method that takes an object ID.  Don't
     //       know why the same code was in two places.  A null object ID is handled
     //       by both this class and JDTrace.
     JDError.throwSQLException((Object)null, sqlState);
  }


// @J3 new method
/**
Throws an SQL exception based on an error in the
error table.

@param  thrower      The object throwing the exception.  This can be null.
@param  sqlState    The SQL State.
 * @return SQLException


@exception          SQLException    Always.
**/
  public static SQLException throwSQLException (Object thrower, String sqlState)
  throws SQLException
  {
    String reason  = getReason(sqlState, null);
    SQLException e ;
    int sqlCode = getSqlCode(sqlState); 
    
    e = createSQLExceptionSubClass(thrower, reason, sqlState, sqlCode); //@PDA jdbc40
    if (JDTrace.isTraceOn ())
    {
       String message = "Throwing exception, sqlState: " + sqlState
                        +  " reason: "   + reason
                        +  " vendor code "+sqlCode;
       JDTrace.logException(thrower, message, e);
    }

    throw e;
  }


/** 
 * Return the sqlcode to use for the specified SQL state. 
 * @param sqlState
 * @return
 */
private static int getSqlCode(String sqlState) {
  // If we re-established the connection, use a different sqlCode
  if (sqlState.equals(EXC_CONNECTION_REESTABLISHED)) { 
    return -4498; 
  }
  // 
  // The DB2 for IBM i SQL CLI manual says that
  // we should set the native error code to -99999
  // when the driver generates the error.
  //

  return -99999; 
}



/**
Throws an SQL exception based on an error in the
error table and dumps an internal exception stack
trace for debugging purposes.

@param  sqlState    The SQL State.
@param  e           The internal exception.

@exception          SQLException    Always.
**/
  public static void throwSQLException (String sqlState, Exception e)
  throws SQLException
  {
     // @J4 - changed this method to call the method that takes an object ID.
     //     This method can be removed once all code that uses is has been updated
     //     to call the method that takes a reference to the thrower.  That is, once
     //     all jdbc classes have been changed from
     //         JDError.throwSQLException(state, e);
     //     to
     //         JDError.throwSQLException(this, state, e);

     JDError.throwSQLException(null, sqlState, e);
  }


/**
Throws an SQL exception based on an error in the
error table and dumps an internal exception stack
trace for debugging purposes.

@param  sqlState    The SQL State.
@param  message     The message text.

@exception          SQLException    Always.
**/
  public static void throwSQLException (String sqlState, String message)
  throws SQLException
  {
     JDError.throwSQLException(null, sqlState, message);
  }


/**
Throws an SQL exception based on an error in the
error table and dumps an internal exception stack
trace for debugging purposes.
@param  thrower     The thrower of the error.
@param  sqlState    The SQL State.
@param  message     The message text.

@exception          SQLException    Always.
**/
  public static void throwSQLException (Object thrower, String sqlState, String message)
  throws SQLException
  {
    String reason = getReason(sqlState, null);
    StringBuffer buffer = new StringBuffer(reason);
    if (message != null)
    {
      buffer.append(" (");
      buffer.append(message);
      buffer.append(')');
    }

    int sqlCode = getSqlCode(sqlState); 
    SQLException e2  = createSQLExceptionSubClass(thrower, buffer.toString(), sqlState, sqlCode); 
    if (JDTrace.isTraceOn ())
    {
      String m2 = "Throwing exception. Message text: "+message;
      JDTrace.logInformation(thrower, m2);

      m2 = "Throwing exception.  Actual exception: "
           + buffer.toString()
           + " sqlState: " + sqlState
           + " vendor code "+sqlCode;
      JDTrace.logException(thrower, m2, e2);
    }

    throw e2;
  }


// @J4 new method.  It has all the code from the method that takes a state and an
//     exception.  What is added is a reference to the thrower so we know
//     who is throwing the exception.
/**
Throws an SQL exception based on an error in the
error table and dumps an internal exception stack
trace for debugging purposes.
 * @param thrower  The thrower of the error
@param  sqlState    The SQL State.
@param  e           The internal exception.

@exception          SQLException    Always.
**/
  public static void throwSQLException (Object thrower, String sqlState, Exception e)
  throws SQLException
  {
    throwSQLException(thrower, sqlState, null, e); 
     
  }
  
  /**
  Throws an SQL exception based on an error in the
  error table and dumps an internal exception stack
  trace for debugging purposes.
   * @param thrower  The thrower of the error
  @param  sqlState    The SQL State.
  @param  replacement variables -- Replacement variables 
  @param  e           The internal exception.

  @exception          SQLException    Always.
  **/
    public static void throwSQLException (Object thrower, String sqlState, String[] replacementVariables, Exception e)
    throws SQLException
    {
  
    // Dump the internal exception stack trace if
    // trace is on.
    // @J3d if (JDTrace.isTraceOn ()) {                                     // @D0A
    // @J3d    synchronized (DriverManager.class) {                        // @D0A
    // @J3d        e.printStackTrace (DriverManager.getLogWriter());
    // @J3d    }                                                           // @D0A
    // @J3d }                                                               // @D0A

    String reason = getReason(sqlState, replacementVariables);
    StringBuffer buffer = new StringBuffer(reason); // @E3A
    if (e != null) {
      buffer.append(" ("); // @E3A
      String message = e.getMessage(); // @E3A
      if (message != null) // @E3A
        buffer.append(message); // @E3A
      else
        // @E3A
        buffer.append(e.getClass()); // @E3A
      buffer.append(')'); // @E7A
    }

    int sqlCode = getSqlCode(sqlState); 

    SQLException e2 = createSQLExceptionSubClass(thrower, buffer.toString(), sqlState, sqlCode); //@PDA jdbc40
    
    if (JDTrace.isTraceOn ())                                           // @J3a
    {                                                                   // @J3a
      String m2 = "Throwing exception. Original exception: ";          // @J3a
      if (e != null) { 
        JDTrace.logException(thrower, m2, e);                             // @J3a
      } else {
        JDTrace.logInformation("Original exception was was null");
      }
                                                                       // @J3a
      m2 = "Throwing exception.  Actual exception: "                   // @J3a
           + buffer.toString()                      // @J3a
           + " sqlState: " + sqlState               // @J3a
           + " vendor code "+sqlCode;                 // @J3a
      JDTrace.logException(thrower, m2, e2);                            // @J3a
    }                                                                   // @J3a

    //
    // Set the cause for JDK 1.4 and later
    //
    if (jdk14) {
      if (e != null) { 
    	  e2.initCause(e);
      }
    }
    throw e2;
  }

/**
Throws an SQL exception based on an error in the
error table and dumps an internal exception stack
trace for debugging purposes.

@param  thrower     The object that threw the exception.
@param  sqlState    The SQL State.
@param  e           The internal exception.
@param  m            A message for the exception

@exception          SQLException    Always.
**/
  public static void throwSQLException (Object thrower, String sqlState, Exception e, String m)
  throws SQLException
  {

    String reason = getReason(sqlState, null);
    StringBuffer buffer = new StringBuffer(reason);
    buffer.append(" (");
    String message = e.getMessage();
    if (message != null)
      buffer.append(message);
    else
      buffer.append(e.getClass());
    buffer.append(", ");
    buffer.append(m);
    buffer.append(')');

    int sqlCode = getSqlCode(sqlState); 
    SQLException e2 = createSQLExceptionSubClass(thrower, buffer.toString(), sqlState, sqlCode); //@PDA jdbc40
    
    if (JDTrace.isTraceOn ())
    {
      String m2 = "Throwing exception. Original exception: ";
      JDTrace.logException(thrower, m2, e);

      m2 = "Throwing exception.  Actual exception: "
           + buffer.toString()
           + " sqlState: " + sqlState
           + " vendor code "+sqlCode;
      JDTrace.logException(thrower, m2, e2);
    }
      try {
      e2.initCause(e);
      } catch (Throwable t) {} 
    throw e2;
  }


/**
Throws an SQL exception based on information
retrieved from the IBM i system.

@param  connection  connection to the system.
@param  id          id for the last operation.
@param  errorClass  error class from the system reply.
@param  returnCode  return code from the system reply.

@exception          SQLException    Always.
**/
  public static void throwSQLException (AS400JDBCConnection connection,
                                        int id,
                                        int errorClass,
                                        int returnCode)
  throws SQLException
  {
     // @J4 code moved to the method that takes a reference to the object
     //     that is throwing the exception.  This method can be removed
     //     once all code that uses is has been updated to call the
     //     method that takes a reference to the thrower.  That is, once
     //     all jdbc classes have been changed from
     //         JDError.throwSQLException(connection, id, errorClass, returnCode);
     //     to
     //         JDError.throwSQLException(this, connection, id, errorClass, returnCode);

     JDError.throwSQLException(null, connection, id, errorClass, returnCode);
  }



// @J4 new method.  It has all the code from the method that takes a connection,
//     and the error stuff.  What is added is a reference to the thrower so we know
//     who is throwing the exception.
/**
Throws an SQL exception based on information
retrieved from the system.
 * @param thrower 

@param  connection  connection to the system.
@param  id          id for the last operation.
@param  errorClass  error class from the system reply.
@param  returnCode  return code from the system reply.
 * @return SQLException

@exception          SQLException    Always.
**/
  public static SQLException throwSQLException (Object thrower,
                                        AS400JDBCConnection connection,
                                        int id,
                                        int errorClass,
                                        int returnCode)
  throws SQLException
  {
    String reason = getReason(connection, id, returnCode);
    String state  = getSQLState(connection, id);

   SQLException e = createSQLExceptionSubClass(thrower, reason, state, returnCode); //@PDA jdbc40
    if (JDTrace.isTraceOn ())                                           // @J3a
    {                                                           // @J3a
      String message = "Throwing exception, id: " + id                 // @J3a
                       + " error class: "   + errorClass         // @J3a
                       + " return code: "   + returnCode         // @J3a
                       + " reason: "        + reason             // @J3a
                       + " state: "         + state;             // @J3a
      JDTrace.logException(thrower, connection, message, e);    // @J3a
    }                                                                   // @J3a

    if (connection instanceof AS400JDBCConnectionRedirect ) {
      ((AS400JDBCConnectionRedirect ) connection ).handleException(e); 
    }
    throw e;
  }


  public static SQLException throwSQLException(Object thrower, SQLException exception)
      throws SQLException {

    String reason = exception.getMessage();
    String state = exception.getSQLState();
    int returnCode = exception.getErrorCode();


     SQLException e = createSQLExceptionSubClass(thrower, reason, state, returnCode);
    try {
      e.setNextException(exception);
    } catch (Exception setError) {
      // Just ignore
    }
    if (JDTrace.isTraceOn()) {
      String message = "Throwing exception, " + " return code: " + returnCode
          + " reason: " + reason + " state: " + state;
      JDTrace.logException(thrower, message, e);
    }

    throw e;
  }




  //@PDA jdbc40
//JDBC40DOC   /**
//JDBC40DOC    Throws an SQLClientInfoException exception based on an error in the
//JDBC40DOC    error table and dumps an internal exception stack
//JDBC40DOC    trace for debugging purposes.
//JDBC40DOC
//JDBC40DOC    @param  thrower     The object that is throwing the exception.
//JDBC40DOC    @param  sqlState    The SQL State.
//JDBC40DOC    @param  e           The internal exception.
//JDBC40DOC
//JDBC40DOC    @exception          SQLClientInfoException    Always.
//JDBC40DOC    **/
  //
  //@pdc jdbc40 merge public static void throwSQLClientInfoException (Object thrower, String sqlState, Exception e, Map<String,ClientInfoStatus> m)
  /* ifdef JDBC40
  public static SQLException throwSQLClientInfoException (Object thrower, String sqlState, Exception e, Map m)
  throws SQLClientInfoException
  {
      String reason = getReason(sqlState, null);
      StringBuffer buffer = new StringBuffer(reason);
      buffer.append(" (");
      String message = e.getMessage();
      if (message != null)
          buffer.append(message);
      else
          buffer.append(e.getClass());
      buffer.append(')');

      int sqlCode = getSqlCode(sqlState); 

      SQLClientInfoException e2 = new SQLClientInfoException (buffer.toString(), sqlState, sqlCode, m);
    
      
      if (JDTrace.isTraceOn ())
      {
          String m2 = "Throwing exception. Original exception: ";
          JDTrace.logException(thrower, m2, e);
          // @J3a
          m2 = "Throwing exception.  Actual exception: "
              + buffer.toString()
              + " sqlState: " + sqlState
              + " vendor code "+sqlCode;
          JDTrace.logException(thrower, m2, e2);
      }

      if (e != null) { 
        e2.initCause(e); 
      }
      throw e2;
  }
  endif */
  //@PDA jdbc40
//JDBC40DOC   /**
//JDBC40DOC    Helper class that creates a new sub-class object of SQLException for new jdbc 4.0 SQLException sub-classes.
//JDBC40DOC    Sub-class is determined based upon sqlState.
//JDBC40DOC    Modeled after Native driver SQLException factory.
//JDBC40DOC
//JDBC40DOC    @param  sqlState    The SQL State.
//JDBC40DOC    **/


  public static SQLException createSQLExceptionSubClass ( Object thrower, String message, String sqlState, int vendorCode )
  {

      //
      // Check the first two digits of the SQL state and create the appropriate
      // exception
      //

      char digit0 = sqlState.charAt(0);
      char digit1 = sqlState.charAt(1);

      switch (digit0) {
      case '0': {
          switch (digit1) {
          case 'A':
/* ifdef JDBC40
              return new SQLFeatureNotSupportedException(message, sqlState, vendorCode);
endif */
/* ifndef JDBC40 */            
            return new SQLException(message, sqlState, vendorCode);
/* endif */             
          case '8':
              if (vendorCode == -30082) {
/* ifdef JDBC40
                  return new SQLInvalidAuthorizationSpecException(message, sqlState, vendorCode);
endif */
/* ifndef JDBC40 */            
                return new SQLException(message, sqlState, vendorCode);
/* endif */     
              } else if (vendorCode == -4498) {
                 return new AS400JDBCTransientException(message, sqlState, vendorCode); 
              } else {
                  // All connection exceptions on IBM i are NonTransient
/* ifdef JDBC40
                  return new SQLNonTransientConnectionException(message, sqlState, vendorCode);
endif */
/* ifndef JDBC40 */            
                return new SQLException(message, sqlState, vendorCode);
/* endif */             
              }
          default:
              return new SQLException(message, sqlState, vendorCode);

          }
      }
      case '2': {
          switch (digit1) {
          case '2':
            /* ifdef JDBC40
              return new SQLDataException(message, sqlState, vendorCode);
              endif */
              /* ifndef JDBC40 */            
                              return new SQLException(message, sqlState, vendorCode);
              /* endif */             
          case '3':
            /* ifdef JDBC40
              return new SQLIntegrityConstraintViolationException(message, sqlState, vendorCode);
              endif */
              /* ifndef JDBC40 */            
                              return new SQLException(message, sqlState, vendorCode);
              /* endif */             
          case '8':
            /* ifdef JDBC40
              return new SQLInvalidAuthorizationSpecException(message, sqlState, vendorCode);
              endif */
              /* ifndef JDBC40 */            
                              return new SQLException(message, sqlState, vendorCode);
              /* endif */             

          default :
              return new SQLException(message, sqlState, vendorCode);

          }
      }
      case '4':
          switch (digit1) {
          case '0':
            /* ifdef JDBC40
              return new SQLTransactionRollbackException(message, sqlState, vendorCode);
              endif */
              /* ifndef JDBC40 */            
                              return new SQLException(message, sqlState, vendorCode);
              /* endif */             
          case '2':
             {
              int positionOfSyntaxError = 0; 
              String sqlStatementText = null; 
              if (thrower instanceof AS400JDBCStatement) {
                  try {
                    positionOfSyntaxError = ((AS400JDBCStatement) thrower).getPositionOfSyntaxError();
                  } catch (SQLException e) {
                    // Just ignore any errors.  We don't expect any to happen
                  } 
                  JDSQLStatement jdsqlStatement = ((AS400JDBCStatement) thrower).getJDSQLStatement();
                   if (jdsqlStatement != null) { 
                      sqlStatementText = jdsqlStatement.toString(); 
                   }
              }
              return new AS400JDBCSQLSyntaxErrorException(message, sqlState, vendorCode, positionOfSyntaxError, sqlStatementText);
             }
          default :
              return new SQLException(message, sqlState, vendorCode);
          }
      case '5':
          if ( vendorCode == -952) {
            /* ifdef JDBC40
              return new SQLTimeoutException(message, sqlState, vendorCode);
              endif */
              /* ifndef JDBC40 */            
                              return new SQLException(message, sqlState, vendorCode);
              /* endif */             
          } else {
              return new SQLException(message, sqlState, vendorCode);
          }
      case 'I':
          if ("IM001".equals(sqlState)) {
            /* ifdef JDBC40
              return new SQLFeatureNotSupportedException(message, sqlState, vendorCode);
              endif */
              /* ifndef JDBC40 */            
                              return new SQLException(message, sqlState, vendorCode);
              /* endif */             
          } else {
              return new SQLException(message, sqlState, vendorCode);
          }

      case 'H' :
          if ("HY017".equals(sqlState)) {
            /* ifdef JDBC40
              return new SQLNonTransientConnectionException(message, sqlState, vendorCode);
              endif */
              /* ifndef JDBC40 */            
                              return new SQLException(message, sqlState, vendorCode);
              /* endif */             
          } else {
              return new SQLException(message, sqlState, vendorCode);
          }

      default:
          return new SQLException(message, sqlState, vendorCode);
      }

  }
}


