///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JDError.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.DriverManager;
import java.sql.SQLException;
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
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Constants for SQL states.
	static final String EXC_ACCESS_MISMATCH		        = "42505";
	static final String EXC_ATTRIBUTE_VALUE_INVALID		= "HY024";
	static final String EXC_BUFFER_LENGTH_INVALID       = "HY090";
	static final String EXC_CHAR_CONVERSION_INVALID		= "22524";
	static final String EXC_CCSID_INVALID		        = "22522";
	static final String EXC_COLUMN_NOT_FOUND		    = "42703";
	static final String EXC_CONCURRENCY_INVALID		    = "HY108";
	static final String EXC_CONNECTION_NONE		        = "08003";
	static final String EXC_CONNECTION_REJECTED		    = "08004";
	static final String EXC_CONNECTION_UNABLE		    = "08001";
	static final String EXC_CURSOR_NAME_AMBIGUOUS		= "3C000";
	static final String EXC_CURSOR_NAME_INVALID 		= "34000";
	static final String EXC_CURSOR_POSITION_INVALID     = "HY109";
	static final String EXC_CURSOR_STATE_INVALID        = "24000";
	static final String EXC_DATA_TYPE_INVALID		    = "HY004";
	static final String EXC_DATA_TYPE_MISMATCH		    = "07006";
	static final String EXC_DESCRIPTOR_INDEX_INVALID    = "07009";
	static final String EXC_FUNCTION_NOT_SUPPORTED		= "IM001";
	static final String EXC_FUNCTION_SEQUENCE		    = "HY010";
	static final String EXC_INTERNAL		            = "HY000";
	static final String EXC_MAX_STATEMENTS_EXCEEDED		= "HY014";
	static final String EXC_OPERATION_CANCELLED         = "HY008";
	static final String EXC_PARAMETER_COUNT_MISMATCH	= "07001";
	static final String EXC_PARAMETER_TYPE_INVALID	    = "HY105";
	static final String EXC_SCALE_INVALID               = "HY094";
	static final String EXC_SERVER_ERROR                = "HY001";
      static final String EXC_SYNTAX_BLANK                    = "43617";
	static final String EXC_SYNTAX_ERROR		        = "42601";
	static final String EXC_TXN_ACTIVE		            = "25000";

	static final String WARN_ATTRIBUTE_VALUE_CHANGED    = "01608";
	static final String WARN_EXTENDED_DYNAMIC_DISABLED  = "01H11";
    static final String WARN_OPTION_VALUE_CHANGED       = "01S02";
    static final String WARN_PACKAGE_CACHE_DISABLED     = "01H12";
    static final String WARN_PROPERTY_EXTRA_IGNORED	    = "01H20";
	static final String WARN_TXN_COMMITTED		        = "01H30";
	static final String WARN_URL_EXTRA_IGNORED	        = "01H10";
	static final String WARN_URL_SCHEMA_INVALID 		= "01H13";



    static String       lastServerSQLState_             = null;



/**
Private constructor to prevent instantiation.  All methods in
this class are static.
**/
    private JDError () { }



/**
Copyright.
**/
    static private String getCopyright ()
    {
        return Copyright.copyright;
    }



/**
Returns the reason text based on a SQL state.

@param  sqlState    the SQL State.
@return             Reason - error description.
**/
	static final String getReason (String sqlState)
	{
	    return AS400JDBCDriver.getResource ("JD" + sqlState);
	}



/**
Returns the message text for the last operation on the server.

@param  connection  Connection to the server.
@param  id          Id for the last operation.
@return             Reason - error description.
**/
	private static String getReason (AS400JDBCConnection connection,
	                                 int id)
	{
		try {
		    // Check to see if the caller wants second level text, too.
		    boolean secondLevelText = connection.getProperties().equals (
		        JDProperties.ERRORS, JDProperties.ERRORS_FULL);

			// Get the message text from the server.  We also retrieve
			// the SQL state at this time to save a trip to the server.
            int orsBitmap = DBBaseRequestDS.ORS_BITMAP_RETURN_DATA
                + DBBaseRequestDS.ORS_BITMAP_SQLCA
				+ DBBaseRequestDS.ORS_BITMAP_MESSAGE_ID
				+ DBBaseRequestDS.ORS_BITMAP_FIRST_LEVEL_TEXT;
		    if (secondLevelText)
		        orsBitmap += DBBaseRequestDS.ORS_BITMAP_SECOND_LEVEL_TEXT;

    		DBSQLResultSetDS request = new DBSQLResultSetDS (
    		    DBSQLResultSetDS.FUNCTIONID_SEND_RESULTS_SET,
    		    id, orsBitmap, 0);

			DBReplyRequestedDS reply = connection.sendAndReceive (request, id);

            // Build up the error description.
            StringBuffer errorDescription = new StringBuffer ();
			errorDescription.append ("[");
			errorDescription.append (reply.getMessageId());
			errorDescription.append ("] ");
            errorDescription.append (reply.getFirstLevelMessageText());
	        if (secondLevelText) {
	            errorDescription.append (" ");
	            errorDescription.append (reply.getSecondLevelMessageText ());
	        }

            // Get the SQL state and remember it for the next
            // call to getSQLState().
            lastServerSQLState_ = reply.getSQLCA ().getSQLState (connection.getConverter ());
            if (lastServerSQLState_ == null)
                lastServerSQLState_ = EXC_SERVER_ERROR;

	        return errorDescription.toString ();
		}
		catch (DBDataStreamException e) {
			return getReason (EXC_INTERNAL);
		}
		catch (SQLException e) {
			return getReason (EXC_INTERNAL);
		}
	}



/**
Returns the SQL state for the last operation on the server.

@param  connection  Connection to the server.
@param  id          Id for the last operation.
@return             The SQL state.
**/
	private static String getSQLState (AS400JDBCConnection connection,
	                                   int id)
	{
	    // If the SQL state was retrieved by a previous call to
	    // getReason(), then use that.
	    if (lastServerSQLState_ != null) {
	        String sqlState = lastServerSQLState_;
	        lastServerSQLState_ = null;
	        return sqlState;
	    }

	    // Otherwise, go to the server to get it.
		try {
            int orsBitmap = DBBaseRequestDS.ORS_BITMAP_RETURN_DATA
				+ DBBaseRequestDS.ORS_BITMAP_SQLCA;

    		DBSQLResultSetDS request = new DBSQLResultSetDS (
    		    DBSQLResultSetDS.FUNCTIONID_SEND_RESULTS_SET,
    		    id, orsBitmap, 0);

			DBReplyRequestedDS reply = connection.sendAndReceive (request, id);

            String sqlState = reply.getSQLCA ().getSQLState (connection.getConverter ());
            if (sqlState == null)
                sqlState = EXC_SERVER_ERROR;

            return sqlState;
		}
		catch (DBDataStreamException e) {
			return getReason (EXC_INTERNAL);
		}
		catch (SQLException e) {
			return getReason (EXC_INTERNAL);
		}
	}



/**
Returns an SQL warning based on an error in the
error table.

@param  sqlState    The SQL State.
**/
    public static SQLWarning getSQLWarning (String sqlState)
    {
        // The DB2 for OS/400 SQL CLI manual says that
        // we should set the native error code to -99999
        // when the driver generates the warning.
        //
        return new SQLWarning (getReason (sqlState), sqlState,
            -99999);
    }



/**
Returns an SQL warning based on information
retrieved from the server.

@param  connection  connection to the server.
@param  id          id for the last operation.
@param  errorClass  error class from the server reply.
@param  returnCode  return code from the server reply.
**/
    public static SQLWarning getSQLWarning (AS400JDBCConnection connection,
         			                        int id,
    				                        int errorClass,
				                            int returnCode)
    {
        return new SQLWarning (getReason (connection, id),
            getSQLState (connection, id), returnCode);
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
        // The DB2 for OS/400 SQL CLI manual says that
        // we should set the native error code to -99999
        // when the driver generates the error.
        //
        throw new SQLException (getReason (sqlState), sqlState,
            -99999);
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
        // Dump the internal exception stack trace if
        // trace is on.
        if (JDTrace.isTraceOn ()) {                                     // @D0A
            synchronized (DriverManager.class) {                        // @D0A
                e.printStackTrace (DriverManager.getLogStream ());
            }                                                           // @D0A
        }                                                               // @D0A

        // The DB2 for OS/400 SQL CLI manual says that
        // we should set the native error code to -99999
        // when the driver generates the error.
        //
        throw new SQLException (getReason (sqlState), sqlState,
            -99999);
    }



/**
Throws an SQL exception based on information
retrieved from the server.

@param  connection  connection to the server.
@param  id          id for the last operation.
@param  errorClass  error class from the server reply.
@param  returnCode  return code from the server reply.

@exception          SQLException    Always.
**/
    public static void throwSQLException (AS400JDBCConnection connection,
         			                      int id,
				                          int errorClass,
				                          int returnCode)
        throws SQLException
    {
        throw new SQLException (getReason (connection, id),
            getSQLState (connection, id), returnCode);
    }



}


