///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400JDBCCallableStatement.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

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



/**
<p>The AS400JDBCCallableStatement class runs a stored procedure.
Use Connection.prepareCall() to create new CallableStatement
objects.

<p>Parameters are indexed sequentially, by number, starting
at 1.  The caller must register output parameters before executing
the stored procedure.
**/
//
// @E2D - Removed paragraph in javadoc that describes escape syntax
//        and the limitation of not supporting return values.
//
public class AS400JDBCCallableStatement
extends AS400JDBCPreparedStatement
implements CallableStatement
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    private static final int    NO_VALIDATION_  = -9999;



    private SQLData[]           registeredTypes_;
    private boolean             returnValueParameterRegistered_;        // @E2A
    private boolean			    wasNull_;



/**
Constructs an AS400JDBCCallableStatement object.

@param   connection             The connection to the server.
@param   id                     The id.
@param   transactionManager     The transaction manager for the connection.
@param   packageManager         The package manager for the connection.
@param   blockCriteria          The block criteria.
@param   blockSize              The block size (in KB).
@param   sqlStatement           The SQL statement.
@parma   packageCriteria        The package criteria.
@param   resultSetType          The result set type.
@param   resultSetConcurrency   The result set concurrency.

@exception  SQLException    If the SQL statement contains a syntax
                            error or an error occurs.
**/
    AS400JDBCCallableStatement (AS400JDBCConnection connection,
        	 				    int id,
	 	        			    JDTransactionManager transactionManager,
        	 				    JDPackageManager packageManager,
	 	        			    String blockCriteria,
	 			        	    int blockSize,
          					    JDSQLStatement sqlStatement,
          					    String packageCriteria,
          					    int resultSetType,
          					    int resultSetConcurrency)
		throws SQLException
    {
        // Turn off pre-fetch, since the output parameter values
        // come back as result data.  If we prefetched data,
        // we would not be able to differentiate between
        // pre-fetched data from the output parameter values.
		super (connection, id, transactionManager,
		    packageManager, blockCriteria, blockSize,
		    false, sqlStatement, true, packageCriteria,
		    resultSetType, resultSetConcurrency);

        registeredTypes_ = new SQLData[parameterCount_];
	    for (int i = 0; i < parameterCount_; ++i)
	        registeredTypes_[i] = null;
        returnValueParameterRegistered_ = false;                            // @E2A

	    wasNull_ = false;
    }



// @E3D /**
// @E3D Releases the resources used by the current input parameter
// @E3D values and registered output parameters. In general, input
// @E3D parameter values and registered output parameters remain in
// @E3D effect for repeated executions of the callable statement.
// @E3D Setting an input parameter value automatically clears its
// @E3D previous value.
// @E3D 
// @E3D @exception  SQLException    If the statement is not open.
// @E3D **/
// @E3D     public void clearParameters ()
// @E3D       throws SQLException
// @E3D     {
// @E3D         synchronized(internalLock_) {                                            // @E1A
// @E3D             super.clearParameters ();
// @E3D     
// @E3D             // This method gets called during the super class's
// @E3D             // constructor, in which case, this part of the
// @E3D             // object has not be initialized yet.  We handle this
// @E3D             // case by checking for null.
// @E3D             if (registeredTypes_ != null)
// @E3D         	    for (int i = 0; i < parameterCount_; ++i)
// @E3D     	            registeredTypes_[i] = null;
// @E3D 
// @E3D             returnValueParameterRegistered_ = false;                            // @E2A
// @E3D         }
// @E3D     }



// @C1A
/**
Performs common operations needed before an execute.

@param  sqlStatement    The SQL statement.
@param  request         The execute request.

@exception      SQLException    If an error occurs.
**/
    void commonExecuteBefore (JDSQLStatement sqlStatement,
                              DBSQLRequestDS request) // private protected
        throws SQLException
    {
        // Validate each parameters.   If a parameter is not an
        // output parameter, then it is okay for it not to have been
        // registered.  However, if an output parameter was not 
        // registered, we throw an exception.
        for (int i = 0; i < parameterCount_; ++i)
            if ((registeredTypes_[i] == null) && (parameterRow_.isOutput (i+1)))
                JDError.throwSQLException (JDError.EXC_PARAMETER_COUNT_MISMATCH);

        super.commonExecuteBefore (sqlStatement, request);
    }



// JDBC 2.0
/**
Returns the value of an SQL ARRAY output parameter as an Array value.
DB2 for OS/400 does not support arrays.

@param  parameterIndex  The parameter index (1-based).
@return                 The parameter value or 0 if the value is SQL NULL.

@exception  SQLException    Always thrown because DB2
                            for OS/400 does not support arrays.
**/
    public Array getArray (int parameterIndex)
        throws SQLException
    {
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
		return null;
    }



// JDBC 2.0
/**
Returns the value of an SQL NUMERIC or DECIMAL output parameter as a
BigDecimal object.

@param  parameterIndex  The parameter index (1-based).
@return                 The parameter value or null if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
**/
    public BigDecimal getBigDecimal (int parameterIndex)
        throws SQLException
    {
        synchronized(internalLock_) {                                            // @E1A
            // Get the data and check for SQL NULL.
            SQLData data = getValue (parameterIndex, Types.NUMERIC, Types.DECIMAL);
            BigDecimal value = (data == null) ? null : data.toBigDecimal (-1);
            testDataTruncation (parameterIndex, data);
            return value;
        }
    }



/**
Returns the value of an SQL NUMERIC or DECIMAL output parameter as a
BigDecimal object.

@param  parameterIndex  The parameter index (1-based).
@param  scale           The number of digits after the decimal.
@return                 The parameter value or null if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the scale is not valid,
                            the statement was not executed, or
                            the requested conversion is not valid.

@deprecated Use getBigDecimal(int) instead.
@see #getBigDecimal(int)
**/
    public BigDecimal getBigDecimal (int parameterIndex, int scale)
        throws SQLException
    {
        // Check for negative scale.
        if (scale < 0)
            JDError.throwSQLException (JDError.EXC_SCALE_INVALID);

        synchronized(internalLock_) {                                            // @E1A
            // Get the data and check for SQL NULL.
            SQLData data = getValue (parameterIndex, Types.NUMERIC, Types.DECIMAL);
            BigDecimal value = (data == null) ? null : data.toBigDecimal (scale);
            testDataTruncation (parameterIndex, data);
            return value;
        }
    }



// JDBC 2.0
/**
Returns the value of an SQL BLOB output parameter as a Blob value.

@param  parameterIndex  The parameter index (1-based).
@return                 The parameter value or 0 if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
**/
    public Blob getBlob (int parameterIndex)
        throws SQLException
    {
        synchronized(internalLock_) {                                            // @E1A
            // Get the data and check for SQL NULL.
            SQLData data = getValue (parameterIndex, Types.BLOB, NO_VALIDATION_);
            Blob value = (data == null) ? null : data.toBlob ();
            testDataTruncation (parameterIndex, data);
            return value;
        }
    }



/**
Returns the value of an SQL SMALLINT output parameter as a
Java boolean.

@param  parameterIndex  The parameter index (1-based).
@return                 The parameter value or false if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
**/
//
// Implementation note:
//
// The spec defines this in terms of SQL BIT, but DB2 for OS/400
// does not support that.
//
    public boolean getBoolean (int parameterIndex)
        throws SQLException
    {
        synchronized(internalLock_) {                                            // @E1A
            // Get the data and check for SQL NULL.
            SQLData data = getValue (parameterIndex, Types.SMALLINT, NO_VALIDATION_);
            boolean value = (data == null) ? false : data.toBoolean ();
            testDataTruncation (parameterIndex, data);
            return value;
        }
    }



/**
Returns the value of an SQL SMALLINT output parameter as a
Java byte.

@param  parameterIndex  The parameter index (1-based).
@return                 The parameter value or 0 if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid,
                            or an error occurs.
**/
//
// Implementation note:
//
// The spec defines this in terms of SQL TINYINT, but DB2 for OS/400
// does not support that.
//
    public byte getByte (int parameterIndex)
        throws SQLException
    {
        synchronized(internalLock_) {                                            // @E1A
            // Get the data and check for SQL NULL.
            SQLData data = getValue (parameterIndex, Types.SMALLINT, NO_VALIDATION_);
            byte value = (data == null) ? 0 : data.toByte ();
            testDataTruncation (parameterIndex, data);
            return value;
        }
    }



/**
Returns the value of an SQL BINARY or VARBINARY output parameter as a
Java byte array.

@param  parameterIndex  The parameter index (1-based).
@return                 The parameter value or null if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
**/
    public byte[] getBytes (int parameterIndex)
        throws SQLException
    {
        synchronized(internalLock_) {                                            // @E1A
            // Get the data and check for SQL NULL.
            SQLData data = getValue (parameterIndex, Types.BINARY, Types.VARBINARY);
            byte[] value = (data == null) ? null : data.toBytes ();
            testDataTruncation (parameterIndex, data);
            return value;
        }
    }



// JDBC 2.0
/**
Returns the value of an SQL CLOB output parameter as a Clob value.

@param  parameterIndex  The parameter index (1-based).
@return                 The parameter value or 0 if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
**/
    public Clob getClob (int parameterIndex)
        throws SQLException
    {
        synchronized(internalLock_) {                                            // @E1A
            // Get the data and check for SQL NULL.
            SQLData data = getValue (parameterIndex, Types.CLOB, NO_VALIDATION_);
            Clob value = (data == null) ? null : data.toClob ();
            testDataTruncation (parameterIndex, data);
            return value;
        }
    }



/**
Copyright.
**/
    static private String getCopyright ()
    {
        return Copyright.copyright;
    }



/**
Returns the value of an SQL DATE output parameter as a
java.sql.Date object using the default calendar.

@param  parameterIndex  The parameter index (1-based).
@return                 The parameter value or null if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
**/
    public Date getDate (int parameterIndex)
        throws SQLException
    {
        return getDate (parameterIndex, Calendar.getInstance ());
    }



// JDBC 2.0
/**
Returns the value of an SQL DATE output parameter as a
java.sql.Date object using a calendar other than the default.

@param  parameterIndex  The parameter index (1-based).
@param  calendar        The calendar.
@return                 The parameter value or null if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed,
                            the calendar is null, or
                            the requested conversion is not valid.
**/
    public Date getDate (int parameterIndex, Calendar calendar)
        throws SQLException
    {
        // Check for null calendar.
        if (calendar == null)
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        synchronized(internalLock_) {                                            // @E1A
            // Get the data and check for SQL NULL.
            SQLData data = getValue (parameterIndex, Types.DATE, NO_VALIDATION_);
            Date value = (data == null) ? null : data.toDate (calendar);
            testDataTruncation (parameterIndex, data);
            return value;
        }
    }



/**
Returns the value of an SQL DOUBLE or FLOAT output parameter as a
Java double.

@param  parameterIndex  The parameter index (1-based).
@return                 The parameter value or 0 if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
**/
    public double getDouble (int parameterIndex)
        throws SQLException
    {
        synchronized(internalLock_) {                                            // @E1A
            // Get the data and check for SQL NULL.
            SQLData data = getValue (parameterIndex, Types.DOUBLE, Types.FLOAT);
            double value = (data == null) ? 0 : data.toDouble ();
            testDataTruncation (parameterIndex, data);
            return value;
        }
    }



/**
Returns the value of an SQL REAL or FLOAT output parameter as a
Java float.

@param  parameterIndex  The parameter index (1-based).
@return                 The parameter value or 0 if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
**/
    public float getFloat (int parameterIndex)
        throws SQLException
    {
        synchronized(internalLock_) {                                            // @E1A
            // Get the data and check for SQL NULL.
            SQLData data = getValue (parameterIndex, Types.REAL, Types.FLOAT);
            float value = (data == null) ? 0 : data.toFloat ();
            testDataTruncation (parameterIndex, data);
            return value;
        }
    }



/**
Returns the value of an SQL INTEGER output parameter as a
Java int.

@param  parameterIndex  The parameter index (1-based).
@return                 The parameter value or 0 if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
**/
    public int getInt (int parameterIndex)
        throws SQLException
    {
        synchronized(internalLock_) {                                            // @E1A
            // Get the data and check for SQL NULL.
            SQLData data = getValue (parameterIndex, Types.INTEGER, NO_VALIDATION_);
            int value = (data == null) ? 0 : data.toInt ();
            testDataTruncation (parameterIndex, data);
            return value;
        }
    }



// @D0C
/**
If the connected AS/400 supports SQL BIGINT data, this returns
the value of an SQL BIGINT output parameter as a Java long.
Otherwise, this returns the value of an SQL INTEGER output 
parameter as a Java long.  SQL BIGINT data is supported on V4R5 
and later.   
          
@param  parameterIndex  The parameter index (1-based).
@return                 The parameter value or 0 if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
**/
//
// Implementation note:
//
// The spec defines this in terms of SQL BIGINT, but DB2 for OS/400
// does not support that until V4R5.
//
    public long getLong (int parameterIndex)
        throws SQLException
    {
        synchronized(internalLock_) {                                            // @E1A
            // Get the data and check for SQL NULL.
            SQLData data;                                                                   // @D0A
            if (connection_.getVRM() >= AS400JDBCConnection.BIGINT_SUPPORTED_)              // @D0A
                data = getValue(parameterIndex, Types.BIGINT, NO_VALIDATION_);              // @D0A
            else                                                                            // @D0A
                data = getValue(parameterIndex, Types.INTEGER, NO_VALIDATION_);             // @D0C
            long value = (data == null) ? 0 : data.toLong ();
            testDataTruncation (parameterIndex, data);
            return value;
        }
    }



/**
Returns the value of an output parameter as a Java Object.
The type of the object corresponds to the SQL type that was
registered for this parameter using registerOutParameter().
When the parameter is a user-defined type, then the
connection's type map is used to create the object.

@param  parameterIndex  The parameter index (1-based).
@return                 The parameter value or null if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
**/
    public Object getObject (int parameterIndex)
        throws SQLException
    {
        synchronized(internalLock_) {                                            // @E1A
            // Get the data and check for SQL NULL.
            SQLData data = getValue (parameterIndex, NO_VALIDATION_, NO_VALIDATION_);
            if (data == null)
                return null;
            Object value = (data == null) ? null : data.toObject ();
            testDataTruncation (parameterIndex, data);
            return value;
        }
    }



// JDBC 2.0
/**
Returns the value of an output parameter as a Java Object.
This driver does not support the type map.
 
@param  parameterIndex  The parameter index (1-based).
@param  type            The type map.  This is not used.
@return                 The parameter value or null if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
**/
    public Object getObject (int parameterIndex, Map typeMap)
        throws SQLException
    {
        // Check for null type map, even though we don't use it.
        if (typeMap == null)
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        return getObject (parameterIndex);
    }



// JDBC 2.0
/**
Returns the value of an SQL REF output parameter as a Ref value.
DB2 for OS/400 does not support structured types.

@param  parameterIndex  The parameter index (1-based).
@return                 The parameter value or 0 if the value is SQL NULL.

@exception  SQLException    Always thrown because DB2
                            for OS/400 does not support REFs.
**/
    public Ref getRef (int parameterIndex)
        throws SQLException
    {
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
		return null;
    }



/**
Returns the value of an SQL SMALLINT output parameter as a
Java short value.

@param  parameterIndex  The parameter index (1-based).
@return                 The parameter value or 0 if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
**/
    public short getShort (int parameterIndex)
        throws SQLException
    {
        synchronized(internalLock_) {                                            // @E1A
            // Get the data and check for SQL NULL.
            SQLData data = getValue (parameterIndex, Types.SMALLINT, NO_VALIDATION_);
            short value = (data == null) ? 0 : data.toShort ();
            testDataTruncation (parameterIndex, data);
            return value;
        }
    }



/**
Returns the value of an SQL CHAR or VARCHAR output
parameter as a Java String object.

@param  parameterIndex  The parameter index (1-based).
@return                 The parameter value or null if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
**/
    public String getString (int parameterIndex)
        throws SQLException
    {
        synchronized(internalLock_) {                                            // @E1A
            // Get the data and check for SQL NULL.
            SQLData data = getValue (parameterIndex, Types.CHAR, Types.VARCHAR);
            String value = (data == null) ? null : data.toString ();
            testDataTruncation (parameterIndex, data);
            return value;
        }
    }



/**
Returns the value of an SQL TIME output parameter as a
java.sql.Time object using the default calendar.

@param  parameterIndex  The parameter index (1-based).
@return                 The parameter value or null if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
**/
    public Time getTime (int parameterIndex)
        throws SQLException
    {
        return getTime (parameterIndex, Calendar.getInstance ());
    }



// JDBC 2.0
/**
Returns the value of an SQL TIME output parameter as a
java.sql.Time object using a calendar other than the
default.

@param  parameterIndex  The parameter index (1-based).
@param  calendar        The calendar.
@return                 The parameter value or null if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed,
                            the calendar is null, or
                            the requested conversion is not valid.
**/
    public Time getTime (int parameterIndex, Calendar calendar)
        throws SQLException
    {
        // Check for null calendar.
        if (calendar == null)
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        synchronized(internalLock_) {                                            // @E1A
            // Get the data and check for SQL NULL.
            SQLData data = getValue (parameterIndex, Types.TIME, NO_VALIDATION_);
            Time value = (data == null) ? null : data.toTime (calendar);
            testDataTruncation (parameterIndex, data);
            return value;
        }
    }



/**
Returns the value of an SQL TIMESTAMP output parameter as a
java.sql.Timestamp object using the default calendar.

@param  parameterIndex  The parameter index (1-based).
@return                 The parameter value or null if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
**/
    public Timestamp getTimestamp (int parameterIndex)
        throws SQLException
    {
        return getTimestamp (parameterIndex, Calendar.getInstance ());
    }



// JDBC 2.0
/**
Returns the value of an SQL TIMESTAMP output parameter as a
java.sql.Timestamp object using a calendar other than the
default.

@param  parameterIndex  The parameter index (1-based).
@param  calendar        The calendar.
@return                 The parameter value or null if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed,
                            the calendar is null, or
                            the requested conversion is not valid.
**/
    public Timestamp getTimestamp (int parameterIndex, Calendar calendar)
        throws SQLException
    {
        // Check for null calendar.
        if (calendar == null)
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        synchronized(internalLock_) {                                            // @E1A
            // Get the data and check for SQL NULL.
            SQLData data = getValue (parameterIndex, Types.TIMESTAMP, NO_VALIDATION_);
            Timestamp value = (data == null) ? null : data.toTimestamp (calendar);
            testDataTruncation (parameterIndex, data);
            return value;
        }
    }



/**
Returns the value for an output parameter for the specified
index, and performs all appropriate validation.  Also checks
for SQL NULL.

@param  parameterIndex  The parameter index (1-based).
@param  sqlType1        The first SQL type code that must be registered,
                        or NO_VALIDATION_ for no validation.
@param  sqlType2        The second SQL type code that must be registered,
                        or NO_VALIDATION_ for no validation.
@return                 The parameter value or null if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
**/
    private SQLData getValue (int parameterIndex, int sqlType1, int sqlType2)
        throws SQLException
    {
        checkOpen ();

        // Check if the parameter index refers to the return value parameter.              @E2A
        // If it is not parameter index 1, then decrement the parameter index,             @E2A
        // since we are "faking" the return value parameter.                               @E2A
        if (useReturnValueParameter_) {                                                 // @E2A
            if (parameterIndex == 1) {                                                  // @E2A
                if (!returnValueParameterRegistered_)                                   // @E2A
                    JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);     // @E2A
                return returnValueParameter_;                                           // @E2A
            }                                                                           // @E2A
            else                                                                        // @E2A
                --parameterIndex;                                                       // @E2A
        }                                                                               // @E2A

        // Validate the parameter index.
        if ((parameterIndex < 1) || (parameterIndex > parameterCount_))
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID);

        // Check that the parameter is an output parameter.
        if (! parameterRow_.isOutput (parameterIndex))
            JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);

        // Verify that the output parameter is registered.
        SQLData registeredType = registeredTypes_[parameterIndex-1];
        if (registeredType == null)
            JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);

        // Verify that the output parameter is registered as the
        // appropriate type.
        int registeredSQLType = registeredType.getType ();
        if ((registeredSQLType != sqlType1)
            && (registeredSQLType != sqlType2)
            && ((sqlType1 != NO_VALIDATION_) || (sqlType2 != NO_VALIDATION_)))
            JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);

        // Verify that the statement has been executed.
        if (! executed_)
            JDError.throwSQLException (JDError.EXC_FUNCTION_SEQUENCE);

        // Get the data and check for SQL NULL.
        SQLData data = parameterRow_.getSQLData (parameterIndex);
        wasNull_ = parameterRow_.isNull (parameterIndex);

        return wasNull_ ? null : data;
    }



/**
Registers the type for an output parameter.  Before
executing the stored procedure call, explicitly
register the type of each output parameter.  When
reading the value of an output parameter, use a
get method that corresponds to the registered type.  A
parameter that is used for both input and output can not
be registered with a different type than was used when
it was set.

@param  parameterIndex  The parameter index (1-based).
@param  sqlType         The SQL type code defined in java.sql.Types.
@param  scale           The number of digits after the decimal
                        if sqlType is DECIMAL or NUMERIC.

@exception  SQLException    If the index is not valid,
                            the scale is not valid,
                            the parameter is not an output parameter,
                            or the requested conversion is not valid.
**/
	public void registerOutParameter (int parameterIndex,
	                                  int sqlType,
	                                  int scale)
      throws SQLException
    {
        synchronized(internalLock_) {                                            // @E1A
            checkOpen ();
    
            // Check if the parameter index refers to the return value parameter.              @E2A
            // If so, it must be registed as an INTEGER.                                       @E2A
            // If it is not parameter index 1, then decrement the parameter index,             @E2A
            // since we are "faking" the return value parameter.                               @E2A
            if (useReturnValueParameter_) {                                                 // @E2A
                if (parameterIndex == 1) {                                                  // @E2A
                    if (sqlType != Types.INTEGER)                                           // @E2A
                        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);         // @E2A
                    returnValueParameterRegistered_ = true;                                 // @E2A
                    return;                                                                 // @E2A
                }                                                                           // @E2A
                else                                                                        // @E2A
                    --parameterIndex;                                                       // @E2A
            }                                                                               // @E2A

            // Validate the parameter index.
            if ((parameterIndex < 1) || (parameterIndex > parameterCount_))
                JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID);
    
            // Validate the scale.
            if (scale < 0)
                JDError.throwSQLException (JDError.EXC_SCALE_INVALID);
    
            // Check that the parameter is an output parameter.
            if (! parameterRow_.isOutput (parameterIndex))
                JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);
    
            // Check that the type is the same as what came back in
            // the parameter row format.
            int expectedType = parameterRow_.getSQLData (parameterIndex).getType();

            // @D8 ignore the type supplied by the user.  We are checking it
            // only to rigidly follow the JDBC spec.  Ignoring the type
            // will make us a friendlier driver.

            // @D8d if (sqlType != expectedType)
            // @D8d    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    
            // Register the parameter.
            registeredTypes_[parameterIndex-1] = SQLDataFactory.newData (expectedType,  // @E8c @D8c
                0, scale+1, scale, settings_, connection_.getVRM());                // @D0C
        }
    }



/**
Registers the type for an output parameter.  Before
executing the stored procedure call, explicitly
register the type of each output parameter.  When
reading the value of an output parameter, use a
get method that corresponds to the registered type.  A
parameter that is used for both input and output can not
be registered with a different type than was used when
it was set.

@param  parameterIndex  The parameter index (1-based).
@param  sqlType         The SQL type code defined in java.sql.Types.

@exception  SQLException    If the index is not valid,
                            the parameter is not an output parameter,
                            or the requested conversion is not valid.
**/
    public void registerOutParameter (int parameterIndex, int sqlType)
      throws SQLException
    {
	    registerOutParameter (parameterIndex, sqlType, 0);
    }


// @A1 - Added for JDK 2.0RC1 - typeName can be ignored, since it is not relevant to the AS/400.
/**
Registers the type for an output parameter.  Before
executing the stored procedure call, explicitly
register the type of each output parameter.  When
reading the value of an output parameter, use a
get method that corresponds to the registered type.  A
parameter that is used for both input and output can not
be registered with a different type than was used when
it was set.

@param  parameterIndex  The parameter index (1-based).
@param  sqlType         The SQL type code defined in java.sql.Types.
@param  typeName        The fully-qualified name of an SQL structured type.  This value will be ignored.

@exception  SQLException    If the index is not valid,
                            the parameter is not an output parameter,
                            or the requested conversion is not valid.
**/
    public void registerOutParameter (int parameterIndex, int sqlType, String typeName)
      throws SQLException
    {
	    registerOutParameter (parameterIndex, sqlType, 0);
    }

/**
Tests if a DataTruncation occurred on the read of a piece of
data and posts a DataTruncation warning if so.

@param  parameterIndex  The parameter index (1-based).
@param  data            The data that was read, or null for SQL NULL.
**/
    private void testDataTruncation (int parameterIndex, SQLData data)
    {
        if (data != null) {
            int truncated = data.getTruncated ();
            if (truncated > 0) {
                int actualSize = data.getActualSize ();
                postWarning (new DataTruncation (parameterIndex, true, true,
                    actualSize, actualSize - truncated));
            }
        }
    }



/**
Indicates if the last parameter read has the
value of SQL NULL.

@return     true if the value is SQL NULL;
            false otherwise.

@exception  SQLException    If the statement is not open.
**/
    public boolean wasNull ()
		throws SQLException
    {
        synchronized(internalLock_) {                                            // @E1A
            checkOpen ();
		    return wasNull_;
        }
    }




}
