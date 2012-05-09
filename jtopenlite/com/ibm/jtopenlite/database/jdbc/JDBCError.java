///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  JDBCError.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database.jdbc;

import java.util.Hashtable;
import java.sql.*;
/**
 * Utility class to report common SQL errors
 */
public class JDBCError {

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
  static final String EXC_COMMUNICATION_LINK_FAILURE   = "08S01";
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
  static final String EXC_TXN_STATE_INVALID            = "25000";
  static final String EXC_SQL_STATEMENT_TOO_LONG       = "54001";



  private final static Object[][] errors = {
      { EXC_PARAMETER_COUNT_MISMATCH,   "The number of parameter values set or registered does not match the number of parameters." },
      { EXC_DATA_TYPE_MISMATCH,         "Data type mismatch." },
      { EXC_DESCRIPTOR_INDEX_INVALID,   "Descriptor index not valid." },
      { EXC_CONNECTION_NONE,            "The connection does not exist." },
      { EXC_CONNECTION_REJECTED,        "The connection was rejected." },
      { EXC_COMMUNICATION_LINK_FAILURE, "Communication link failure." },
      { EXC_CCSID_INVALID,              "CCSID value is not valid." },
      { EXC_CHAR_CONVERSION_INVALID,    "Character conversion resulted in truncation." },
      { EXC_CURSOR_STATE_INVALID,       "Cursor state not valid." },
      { EXC_TXN_STATE_INVALID,          "Transaction state not valid." },
      { EXC_CURSOR_NAME_INVALID,        "Cursor name not valid." },
      { EXC_CURSOR_NAME_AMBIGUOUS,      "Cursor name is ambiguous." },
      { EXC_ACCESS_MISMATCH,            "Connection authorization failure occurred." },
      { EXC_SYNTAX_ERROR,               "A character, token, or clause is not valid or is missing." },
      { EXC_COLUMN_NOT_FOUND,           "An undefined column name was detected." },
      { EXC_SYNTAX_BLANK,               "A string parameter value with zero length was detected." }, // @A3A
      { EXC_INTERNAL,                   "Internal driver error." },
      { EXC_SERVER_ERROR,               "Internal server error." },
      { EXC_DATA_TYPE_INVALID,          "Data type not valid." },
      { EXC_OPERATION_CANCELLED,        "Operation cancelled." },
      { EXC_FUNCTION_SEQUENCE,          "Function sequence error." },
      { EXC_MAX_STATEMENTS_EXCEEDED,    "Limit on number of statements exceeded." },
      { EXC_ATTRIBUTE_VALUE_INVALID,    "Attribute value not valid." },
      { EXC_BUFFER_LENGTH_INVALID,      "String or buffer length not valid." },
      { EXC_SCALE_INVALID,              "Scale not valid." },
      { EXC_PARAMETER_TYPE_INVALID,     "Parameter type not valid." },
      { EXC_CONCURRENCY_INVALID,        "Concurrency or type option not valid." },
      { EXC_CURSOR_POSITION_INVALID,    "Cursor position not valid." },
      { EXC_FUNCTION_NOT_SUPPORTED,     "The driver does not support this function." },
      { EXC_SQL_STATEMENT_TOO_LONG,     "SQL statement too long or complex." },




   };


   @SuppressWarnings("unchecked")
static Hashtable reasonHashtable = null;



/**
Throws an SQL exception based on an error in the
error table.

@param  sqlState    The SQL State.

@exception          SQLException    Always.
**/
  public static void throwSQLException (String sqlState)
  throws SQLException
  {
    throw getSQLException(sqlState);
  }

  public static SQLException getSQLException(String sqlState) {
    String reason  = getReason(sqlState);
    SQLException e = new SQLException (reason, sqlState, -99999);
    return e;
  }


  public static SQLException getSQLException(String sqlState, String extra) {
    String reason  = getReason(sqlState);
    SQLException e;
    if (extra != null) {
	e = new SQLException (reason +" : "+extra, sqlState, -99999);
    } else {
	e = new SQLException (reason, sqlState, -99999);
    }
    return e;
  }

/**
Returns the reason text based on a SQL state.

@param  sqlState    the SQL State.
@return             Reason - error description.
**/
  @SuppressWarnings("unchecked")
static final String getReason (String sqlState)
  {
      if (reasonHashtable == null) {
	  reasonHashtable = new Hashtable();
	  for (int i = 0; i < errors.length; i++) {
	      reasonHashtable.put(errors[i][0],errors[i][1]);
	  }
      }

      String reason = (String) reasonHashtable.get(sqlState);
      if (reason == null) {
	  reason = "No message for "+sqlState;
      }
      return reason;
  }


}
