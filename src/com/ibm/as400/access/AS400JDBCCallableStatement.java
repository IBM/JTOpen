///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCCallableStatement.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;             //@G4A
import java.io.Reader;                  //@G4A
import java.math.BigDecimal;
import java.net.MalformedURLException;  //@G4A
import java.net.URL;                    //@G4A
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.DataTruncation;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSet;              //@G4A
import java.sql.SQLException;
import java.sql.Statement;              //@G4A
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Map;
import java.util.Hashtable;             //@G4A



/**
<p>The AS400JDBCCallableStatement class runs a stored procedure.
Use Connection.prepareCall() to create new CallableStatement
objects.

<p>Parameters are indexed sequentially, by number, starting
at 1.  The caller must register output parameters before executing
the stored procedure.

<p>The new JDK 1.4 methods add the ability to 
retrieve information by column name in addition to column index.
Be aware you will see better performance accessing columns by their
index rather than accessing them by their name.
**/
//
// @E2D - Removed paragraph in javadoc that describes escape syntax
//        and the limitation of not supporting return values.
//
public class AS400JDBCCallableStatement
extends AS400JDBCPreparedStatement
implements CallableStatement
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    private static final int    NO_VALIDATION_  = -9999;

    private SQLData[]           registeredTypes_;
    private boolean             returnValueParameterRegistered_;        // @E2A
    private boolean             wasNull_;

    private Hashtable   parameterNames_ = null;     //@G4A



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
@param   resultSetHoldability   The result set holdability.
@param   generatedKeysRequested The generated keys requested.

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
                                int resultSetConcurrency,
                                int resultSetHoldability,         //@G4A
                                int generatedKeysRequested)      //@G4A
          throws SQLException
    {
        // Turn off pre-fetch, since the output parameter values
        // come back as result data.  If we prefetched data,
        // we would not be able to differentiate between
        // pre-fetched data from the output parameter values.
          super (connection, id, transactionManager,
              packageManager, blockCriteria, blockSize,
              false, sqlStatement, true, packageCriteria,
              resultSetType, resultSetConcurrency, resultSetHoldability,
              generatedKeysRequested);

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
// @E3D                 for (int i = 0; i < parameterCount_; ++i)
// @E3D                    registeredTypes_[i] = null;
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



    //@G4A
    /*
    Find the column index that matches this parameter name.

    @param  parameterName    The parameter name to change into a column index (1-based).
    */
    int findParameterIndex (String parameterName)
    throws SQLException
    {                                                          
        // Throw an exception if null was passed in
        if (parameterName == null)
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        // Throw an exception if the Statement is closed (FUNCTION SEQUENCE)
        if (isClosed())
            JDError.throwSQLException (JDError.EXC_FUNCTION_SEQUENCE);

        if (parameterName.indexOf("\"") < 0)                      // @G6a
        {                                                         // @G6a
           parameterName = parameterName.toUpperCase();           // @G6a
        }                                                         // @G6a
        else                                                      // @G6a
        {                                                         // @G6a
           parameterName = parameterName.replace('\"', ' ');      // @G6a
           parameterName = parameterName.trim();                  // @G6a
        }                                                         // @G6a


        // If we have a cache created, try to find the column name in it.
        if (parameterNames_ != null) 
        {
            // Look up the mapping in our cache.
            Integer parameterId = (Integer)parameterNames_.get(parameterName);  // @G6c (no longer uppercase parameterName here

            // If it is there, return the parm number; otherwise throw 
            // an exception (COLUMN NOT FOUND). 
            if (parameterId != null)
                return(parameterId.intValue());
            else
                JDError.throwSQLException (JDError.EXC_COLUMN_NOT_FOUND);
        }

        // Else, create a new hash table to hold all the column name/number mappings.
        parameterNames_ = new Hashtable(parameterCount_);

        // Cache all the parm names and numbers.
        int count = 0;
        int returnParm = 0;

        Statement s = connection_.createStatement();
        ResultSet rs = s.executeQuery("SELECT SPECIFIC_NAME from QSYS2.SYSPROCS WHERE ROUTINE_SCHEMA = '" + sqlStatement_.getSchema() + 
                                      "' AND ROUTINE_NAME = '" + sqlStatement_.getProcedure() + 
                                      "' AND IN_PARMS + OUT_PARMS + INOUT_PARMS = " + parameterCount_);

        // If there are no rows, throw an internal driver exception
        if (!rs.next())
            JDError.throwSQLException (JDError.EXC_INTERNAL);

        String specificName = rs.getString(1);

        rs = s.executeQuery("SELECT PARAMETER_NAME, ORDINAL_POSITION FROM QSYS2.SYSPARMS WHERE " +
                            " SPECIFIC_NAME = '" + specificName + "' AND SPECIFIC_SCHEMA = '" + sqlStatement_.getSchema() + "'");

        while (rs.next()) 
        {
            count++;

            String colName = rs.getString(1);           // @G6 the server will uppercase the name if not in quotes
            int colInd = rs.getInt(2);
            parameterNames_.put(colName, new Integer(colInd)); 
            
            if (colName.equals(parameterName))          //@G6c no longer equals ignore case
                returnParm = colInd;
        }

        // If the number of parm names didn't equal the number of parameters, throw
        // an exception (INTERNAL).
        if (count != parameterCount_)
            JDError.throwSQLException (JDError.EXC_INTERNAL);

        // Throw an exception if the column name is not found (COLUMN NOT FOUND). 
        if (returnParm == 0)
            JDError.throwSQLException (JDError.EXC_COLUMN_NOT_FOUND);

        return returnParm;
    }





// JDBC 2.0
/**
Returns the value of an SQL ARRAY output parameter as an Array value.
DB2 UDB for iSeries does not support arrays.

@param  parameterIndex  The parameter index (1-based).
@return                 The parameter value or 0 if the value is SQL NULL.

@exception  SQLException    Always thrown because DB2
                            UDB for iSeries does not support arrays.
**/
    public Array getArray (int parameterIndex)
        throws SQLException
    {
          JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
          return null;
    }



//@G4A JDBC 3.0
/**
Returns the value of an SQL ARRAY output parameter as an Array value.
DB2 UDB for iSeries does not support arrays.
    
@param  parameterName   The parameter name.
@return                 The parameter value or 0 if the value is SQL NULL.
    
@exception  SQLException    Always thrown because DB2
                            UDB for iSeries does not support arrays.
@since Modification 5
**/
    public Array getArray (String parameterName)
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

@deprecated Use getBigDecimal(int) or getBigDecimal(String) instead.
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



//@G4A JDBC 3.0
/**
Returns the value of an SQL NUMERIC or DECIMAL output parameter as a
BigDecimal object.
   
@param  parameterName  The parameter name.
@return                 The parameter value or null if the value is SQL NULL.
   
@exception  SQLException    If the statement is not open,
                            the index is not valid, the parameter name is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
@since Modification 5
**/
    public BigDecimal getBigDecimal (String parameterName)
    throws SQLException
    { 
        return getBigDecimal(findParameterIndex(parameterName));
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



//@G4A JDBC 3.0
/**
Returns the value of an SQL BLOB output parameter as a Blob value.
    
@param  parameterName   The parameter name.
@return                 The parameter value or 0 if the value is SQL NULL.
    
@exception  SQLException    If the statement is not open,
                            the index is not valid, the parameter name is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
@since Modification 5
**/
    public Blob getBlob (String parameterName)
    throws SQLException
    {
        return getBlob(findParameterIndex(parameterName));
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



//@G4A JDBC 3.0
/**
Returns the value of an SQL SMALLINT output parameter as a
Java boolean.
    
@param  parameterName   The parameter name.
@return                 The parameter value or false if the value is SQL NULL.
    
@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
@since Modification 5
**/
    //
    // Implementation note:
    //
    // The spec defines this in terms of SQL BIT, but DB2 for OS/400
    // does not support that.
    //
    public boolean getBoolean (String parameterName)
    throws SQLException
    {
        return getBoolean(findParameterIndex(parameterName));
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



//@G4A JDBC 3.0
/**
Returns the value of an SQL SMALLINT output parameter as a
Java byte.

@param  parameterName   The parameter name.
@return                 The parameter value or 0 if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid,
                            or an error occurs.
@since Modification 5
**/
    //
    // Implementation note:
    //
    // The spec defines this in terms of SQL TINYINT, but DB2 for OS/400
    // does not support that.
    //
    public byte getByte (String parameterName)
    throws SQLException
    {
        return getByte(findParameterIndex(parameterName));
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



//@G4A JDBC 3.0
/**
Returns the value of an SQL BINARY or VARBINARY output parameter as a
Java byte array.
   
@param  parameterName   The parameter name.
@return                 The parameter value or null if the value is SQL NULL.
    
@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
@since Modification 5
**/
    public byte[] getBytes (String parameterName)
    throws SQLException
    {
        return getBytes(findParameterIndex(parameterName));
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



//@G4A JDBC 3.0
/**
Returns the value of an SQL CLOB output parameter as a Clob value.
    
@param  parameterName   The parameter name.
@return                 The parameter value or 0 if the value is SQL NULL.
   
@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
@since Modification 5
**/
    public Clob getClob (String parameterName)
    throws SQLException
    {
        return getClob(findParameterIndex(parameterName));    
    }



/**
Copyright.
**/
    //@G4D static private String getCopyright ()
    //@G4D {
    //@G4D     return Copyright.copyright;
    //@G4D }



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



//@G4A JDBC 3.0
/**
Returns the value of an SQL DATE output parameter as a
java.sql.Date object using the default calendar.

@param  parameterName   The parameter name.
@return                 The parameter value or null if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the parameter name is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
@since Modification 5
**/

    public Date getDate (String parameterName)
    throws SQLException
    {
        return getDate(findParameterIndex(parameterName));
    }



    // JDBC 2.0
    /**
    Returns the value of an SQL DATE output parameter as a
    java.sql.Date object using a calendar other than the default.
    
    @param  parameterName   The parameter name.
    @param  calendar        The calendar.
    @return                 The parameter value or null if the value is SQL NULL.
    
    @exception  SQLException    If the statement is not open,
                                the index is not valid, the parameter name is
                                not registered as an output parameter,
                                the statement was not executed,
                                the calendar is null, or
                                the requested conversion is not valid.
    **/
    public Date getDate (String parameterName, Calendar calendar)
    throws SQLException
    {
        return getDate(findParameterIndex(parameterName), calendar);
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



//@G4A JDBC 3.0
/**
Returns the value of an SQL DOUBLE or FLOAT output parameter as a
Java double.
   
@param  parameterName   The parameter name.
@return                 The parameter value or 0 if the value is SQL NULL.
    
@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
@since Modification 5
**/
    public double getDouble (String parameterName)
    throws SQLException
    {
        return getDouble(findParameterIndex(parameterName));
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



//@G4A  JDBC 3.0
/**
Returns the value of an SQL REAL or FLOAT output parameter as a
Java float.
    
@param  parameterName  The parameter name.
@return                 The parameter value or 0 if the value is SQL NULL.
    
@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
@since Modification 5
**/
    public float getFloat (String parameterName)
    throws SQLException
    {
        return getFloat(findParameterIndex(parameterName));
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



//@G4A   JDBC 3.0
/**
Returns the value of an SQL INTEGER output parameter as a
Java int.

@param  parameterName   The parameter name.
@return                 The parameter value or 0 if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
@since Modification 5
**/
    public int getInt (String parameterName)
    throws SQLException
    {
        return getInt(findParameterIndex(parameterName));
    }


// @D0C
/**
If the connected AS/400 or iSeries server supports SQL BIGINT data, this returns
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



//@G4A  JDBC 3.0
/**
If the connected AS/400 or iSeries server supports SQL BIGINT data, this returns
the value of an SQL BIGINT output parameter as a Java long.
Otherwise, this returns the value of an SQL INTEGER output
parameter as a Java long.  SQL BIGINT data is supported on V4R5
and later.

@param  parameterName   The parameter name.
@return                 The parameter value or 0 if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
@since Modification 5
**/
    //
    // Implementation note:
    //
    // The spec defines this in terms of SQL BIGINT, but DB2 for OS/400
    // does not support that until V4R5.
    //
    public long getLong (String parameterName)
    throws SQLException
    {
        return getLong(findParameterIndex(parameterName));
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



//@G4A  JDBC 3.0
/**
Returns the value of an output parameter as a Java Object.
The type of the object corresponds to the SQL type that was
registered for this parameter using registerOutParameter().
When the parameter is a user-defined type, then the
connection's type map is used to create the object.

@param  parameterName   The parameter name.
@return                 The parameter value or null if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the parameter name is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
@since Modification 5
**/
    public Object getObject (String parameterName)
    throws SQLException
    {
        return getObject(findParameterIndex(parameterName));
    }



//@G4A JDBC 3.0
/**
Returns the value of an output parameter as a Java Object.
This driver does not support the type map.
    
@param  parameterName   The parameter name.
@param  type            The type map.  This is not used.
@return                 The parameter value or null if the value is SQL NULL.
    
@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
@since Modification 5
**/
    public Object getObject (String parameterName, Map typeMap)
    throws SQLException
    {
       return getObject(findParameterIndex(parameterName));
    }


// JDBC 2.0
/**
Returns the value of an SQL REF output parameter as a Ref value.
DB2 UDB for iSeries does not support structured types.

@param  parameterIndex  The parameter index (1-based).
@return                 The parameter value or 0 if the value is SQL NULL.

@exception  SQLException    Always thrown because DB2
                            UDB for iSeries does not support REFs.
**/
    public Ref getRef (int parameterIndex)
        throws SQLException
    {
          JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
          return null;
    }



//@G4A  JDBC 3.0
/**
Returns the value of an SQL REF output parameter as a Ref value.
DB2 UDB for iSeries does not support structured types.

@param  parameterName   The parameter name.
@return                 The parameter value or 0 if the value is SQL NULL.

@exception  SQLException    Always thrown because DB2
                            UDB for iSeries does not support REFs.
@since Modification 5
**/
    public Ref getRef (String parameterName)
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



//@G4A  JDBC 3.0
/**
Returns the value of an SQL SMALLINT output parameter as a
Java short value.

@param  parameterName  The parameter name.
@return                 The parameter value or 0 if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
@since Modification 5
**/
    public short getShort (String parameterName)
    throws SQLException
    {
        return getShort(findParameterIndex(parameterName));
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



//@G4A JDBC 3.0
/**
Returns the value of an SQL CHAR or VARCHAR output
parameter as a Java String object.

@param  parameterIndex  The parameter name.
@return                 The parameter value or null if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the index is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
@since Modification 5
**/
    public String getString (String parameterName)
    throws SQLException
    {
        return getString(findParameterIndex(parameterName)); 
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



//@G4A JDBC 3.0
/**
Returns the value of an SQL TIME output parameter as a
java.sql.Time object using the default calendar.

@param  parameterName   The parameter name.
@return                 The parameter value or null if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the parameter name is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
@since Modification 5
**/
    public Time getTime (String parameterName)
    throws SQLException
    {
        return getTime(findParameterIndex(parameterName));
    }



//@G4A JDBC 3.0
/**
Returns the value of an SQL TIME output parameter as a
java.sql.Time object using a calendar other than the
default.
    
@param  parameterName   The parameter name.
@param  calendar        The calendar.
@return                 The parameter value or null if the value is SQL NULL.
    
@exception  SQLException    If the statement is not open,
                            the index is not valid, the parameter name is
                            not registered as an output parameter,
                            the statement was not executed,
                            the calendar is null, or
                            the requested conversion is not valid.
@since Modification 5
**/
    public Time getTime (String parameterName, Calendar calendar)
    throws SQLException
    {
        return getTime(findParameterIndex(parameterName), calendar);
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



//@G4A JDBC 3.0
/**
Returns the value of an SQL TIMESTAMP output parameter as a
java.sql.Timestamp object using the default calendar.

@param  parameterName   The parameter name.
@return                 The parameter value or null if the value is SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the parameter name is
                            not registered as an output parameter,
                            the statement was not executed, or
                            the requested conversion is not valid.
@since Modification 5
**/
    public Timestamp getTimestamp (String parameterName)
    throws SQLException
    {
        return getTimestamp(findParameterIndex(parameterName));
    }



//@G4A JDBC 3.0
/**
Returns the value of an SQL TIMESTAMP output parameter as a
java.sql.Timestamp object using a calendar other than the
default.
    
@param  parameterName   The parameter name.
@param  calendar        The calendar.
@return                 The parameter value or null if the value is SQL NULL.
    
@exception  SQLException    If the statement is not open,
                            the index is not valid, the parameter name is
                            not registered as an output parameter,
                            the statement was not executed,
                            the calendar is null, or
                            the requested conversion is not valid.
@since Modification 5
**/
    public Timestamp getTimestamp (String parameterName, Calendar calendar)
    throws SQLException
    {
        return getTimestamp(findParameterIndex(parameterName), calendar);
    }



//@G4A  JDBC 3.0
/**
Returns the value of an SQL DATALINK output parameter as a
java.net.URL object.
     
@param  parameterIndex  The parameter index (1-based).
@return                 The parameter value or null if the value is SQL NULL.
    
@exception  SQLException    If the statement is not open,
                            the index is not valid, the parameter name is
                            not registered as an output parameter,
                            the statement was not executed or
                            the requested conversion is not valid.
@since Modification 5
**/
    public URL getURL (int parameterIndex)
    throws SQLException
    {
        try
        {
            return new java.net.URL(getString(parameterIndex));
        }
        catch (MalformedURLException e)
        {
            JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID, e);
            return null;
        }
    }



//@G4A  JDBC 3.0
/**
Returns the value of an SQL DATALINK output parameter as a
java.net.URL object.
    
@param  parameterName   The parameter name.
@return                 The parameter value or null if the value is SQL NULL.
    
@exception  SQLException    If the statement is not open,
                            the name is not valid, the parameter name is
                            not registered as an output parameter,
                            the statement was not executed or
                            the requested conversion is not valid.
@since Modification 5
**/
    public URL getURL (String parameterName)
    throws SQLException
    {
        return getURL(findParameterIndex(parameterName));
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
            //
            // @D8d if (sqlType != expectedType)
            // @D8d    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);

            // Register the parameter.
            registeredTypes_[parameterIndex-1] = SQLDataFactory.newData (expectedType,   // @D8c
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



//@G4A  JDBC 3.0
/**
Registers the type for an output parameter.  Before
executing the stored procedure call, explicitly
register the type of each output parameter.  When
reading the value of an output parameter, use a
get method that corresponds to the registered type.  A
parameter that is used for both input and output can not
be registered with a different type than was used when
it was set.
    
@param  parameterName  The parameter name.
@param  sqlType        The SQL type code defined in java.sql.Types.
    
@exception  SQLException    If the index is not valid,
                            the parameter is not an output parameter,
                            or the requested conversion is not valid.
@since Modification 5
**/
    public void registerOutParameter (String parameterName, int sqlType)
    throws SQLException
    {
        registerOutParameter (findParameterIndex(parameterName), sqlType, 0);
    }



//@G4A  JDBC 3.0
/**
Registers the type for an output parameter.  Before
executing the stored procedure call, explicitly
register the type of each output parameter.  When
reading the value of an output parameter, use a
get method that corresponds to the registered type.  A
parameter that is used for both input and output can not
be registered with a different type than was used when
it was set.
    
@param  parameterName   The parameter name.
@param  sqlType         The SQL type code defined in java.sql.Types.
@param  scale           The number of digits after the decimal
                        if sqlType is DECIMAL or NUMERIC.
    
@exception  SQLException    If the index is not valid,
                            the scale is not valid,
                            the parameter is not an output parameter,
                            or the requested conversion is not valid.
@since Modification 5
**/
    public void registerOutParameter (String parameterName,
                                      int sqlType,
                                      int scale)
    throws SQLException
    {
        registerOutParameter (findParameterIndex(parameterName), sqlType, scale);
    }



//@G4A  JDBC 3.0
/**
Registers the type for an output parameter.  Before
executing the stored procedure call, explicitly
register the type of each output parameter.  When
reading the value of an output parameter, use a
get method that corresponds to the registered type.  A
parameter that is used for both input and output can not
be registered with a different type than was used when
it was set.
    
@param  parameterName  The parameter name.
@param  sqlType         The SQL type code defined in java.sql.Types.
@param  typeName        The fully-qualified name of an SQL structured type.  This value will be ignored.
    
@exception  SQLException    If the index is not valid,
                            the parameter is not an output parameter,
                            or the requested conversion is not valid.
@since Modification 5
**/
    public void registerOutParameter (String parameterName, int sqlType, String typeName)
    throws SQLException
    {
        registerOutParameter (findParameterIndex(parameterName), sqlType, 0);
    }



//@G4A  JDBC 3.0
/**
Sets an input parameter to an ASCII stream value.  The driver
reads the data from the stream as needed until no more bytes
are available.  The driver converts this to an SQL VARCHAR
value.

@param  parameterName   The parameter name.
@param  parameterValue  The parameter value or null to set
                        the value to SQL NULL.
@param  length          The number of bytes in the stream.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the parameter
                            is not an input parameter,
                            the length is not valid,
                            the input stream does not contain all
                            ASCII characters, or an error occurs
                            while reading the input stream.
@since Modification 5
**/
    public void setAsciiStream(String parameterName, InputStream parameterValue, int length) 
    throws SQLException
    {
        setAsciiStream(findParameterIndex(parameterName), parameterValue, length);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to a BigDecimal value.  The driver converts
this to an SQL NUMERIC value.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value or null to set
                        the value to SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, or the parameter
                            is not an input parameter.
@since Modification 5
**/
    public void setBigDecimal(String parameterName, BigDecimal parameterValue) 
    throws SQLException
    {
        setBigDecimal(findParameterIndex(parameterName), parameterValue);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to a binary stream value.  The driver
reads the data from the stream as needed until no more bytes
are available.  The driver converts this to an SQL VARBINARY
value.
    
@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value or null to set
                        the value to SQL NULL.
@param  length          The number of bytes in the stream.
    
@exception  SQLException    If the statement is not open,
                            the index is not valid, the parameter
                            is not an input parameter,
                            the length is not valid, or
                            an error occurs while reading the
                            input stream.
@since Modification 5
**/
    public void setBinaryStream(String parameterName, InputStream parameterValue, int length) 
    throws SQLException
    {
        setBinaryStream(findParameterIndex(parameterName), parameterValue, length);
    }


/**
Sets an input parameter to a Java boolean value.  The driver
converts this to an SQL SMALLINT value.

@param  parameterName   The parameter name.
@param  parameterValue  The parameter value.

@exception  SQLException    If the statement is not open,
                            the parameterName is not valid, or
                            the parameter is not an input parameter.
**/
    //
    // Implementation note:
    //
    // The spec defines this in terms of SQL BIT, but DB2 for OS/400
    // does not support that.
    //
    public void setBoolean(String parameterName, boolean parameterValue) 
    throws SQLException
    {
        setBoolean(findParameterIndex(parameterName), parameterValue);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to a Java byte value.  The driver
converts this to an SQL SMALLINT value.
    
@param  parameterName   The parameter name.
@param  parameterValue  The parameter value.
    
@exception  SQLException    If the statement is not open,
                            the parameter name is not valid, or
                            the parameter is not an input parameter.
@since Modification 5
**/
    //
    // Implementation note:
    //
    // The spec defines this in terms of SQL TINYINT, but DB2 for OS/400
    // does not support that.
    //
    public void setByte(String parameterName, byte parameterValue) 
    throws SQLException
    {
        setByte(findParameterIndex(parameterName), parameterValue);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to a Java byte array value.  The driver
converts this to an SQL VARBINARY value.

@param  parameterName   The parameter name.
@param  parameterValue  The parameter value or null to set
                        the value to SQL NULL.

@exception  SQLException    If the statement is not open,
                            the parameter name is not valid, or the parameter
                            is not an input parameter.
@since Modification 5
**/
    public void setBytes(String parameterName, byte[] parameterValue) 
    throws SQLException
    {
        setBytes(findParameterIndex(parameterName), parameterValue);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to a character stream value.  The driver
reads the data from the character stream as needed until no more
characters are available.  The driver converts this to an SQL
VARCHAR value.
    
@param  parameterName   The parameter name.
@param  parameterValue  The parameter value or null to set
                        the value to SQL NULL.
@param  length          The number of bytes in the reader.
    
@exception  SQLException    If the statement is not open,
                            the index is not valid, the parameter
                            is not an input parameter,
                            the length is not valid,
                            or an error occurs while reading the
                            character stream
@since Modification 5
**/
    public void setCharacterStream(String parameterName, Reader parameterValue, int length) 
    throws SQLException
    {
        setCharacterStream(findParameterIndex(parameterName), parameterValue, length);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to a java.sql.Date value using the
default calendar.  The driver converts this to an SQL DATE
value.
    
@param  parameterName   The parameter name.
@param  parameterValue  The parameter value or null to set
                        the value to SQL NULL.
    
@exception  SQLException    If the statement is not open,
                            the parameter name is not valid, or the parameter
                            is not an input parameter.
                            
@since Modification 5
**/
    public void setDate(String parameterName, Date parameterValue) 
    throws SQLException
    {
        setDate(findParameterIndex(parameterName), parameterValue);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to a java.sql.Date value using a
calendar other than the default.  The driver converts this
to an SQL DATE value.
    
@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value or null to set
                        the value to SQL NULL.
@param  calendar        The calendar.
    
@exception  SQLException    If the statement is not open,
                            the index is not valid, the parameter
                            is not an input parameter,
                            or the calendar is null.
                            
@since Modification 5
**/
    public void setDate(String parameterName, Date parameterValue, Calendar cal) 
    throws SQLException
    {
        setDate(findParameterIndex(parameterName), parameterValue, cal);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to a Java double value.  The driver
converts this to an SQL DOUBLE value.

@param  parameterName   The parameter name.
@param  parameterValue  The parameter value.

@exception  SQLException    If the statement is not open,
                            the parameter name is not valid or
                            the parameter is not an input parameter.
@since Modification 5
**/
    public void setDouble(String parameterName, double parameterValue) 
    throws SQLException
    {
        setDouble(findParameterIndex(parameterName), parameterValue);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to a Java float value.  The driver
converts this to an SQL REAL value.

@param  parameterName   The parameter name.
@param  parameterValue  The parameter value.

@exception  SQLException    If the statement is not open,
                            the parameter name is not valid, or
                            the parameter is not an input parameter.
@since Modification 5
**/
    //
    // Note:  The JDBC 1.22 specification states that this
    //        method should set an SQL FLOAT value.  However,
    //        all tables map float to REAL.  Otherwise,
    //        nothing is symetrical and certain INOUT
    //        parameters do not work.
    //
    public void setFloat(String parameterName, float parameterValue) 
    throws SQLException
    {
        setFloat(findParameterIndex(parameterName), parameterValue);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to a Java int value.  The driver
converts this to an SQL INTEGER value.

@param  parameterName   The parameter name.
@param  parameterValue  The parameter value.

@exception  SQLException    If the statement is not open,
                            the parameter name is not valid or
                            the parameter is not an input parameter.
@since Modification 5
**/
    public void setInt(String parameterName, int parameterValue) 
    throws SQLException
    {
        setInt(findParameterIndex(parameterName), parameterValue);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to a Java long value.
If the connected AS/400 or iSeries server supports SQL BIGINT data, the driver
converts this to an SQL BIGINT value.  Otherwise, the driver
converts this to an SQL INTEGER value.  SQL BIGINT data is
supported on V4R5 and later.

@param  parameterName   The parameter name.
@param  parameterValue  The parameter value.

@exception  SQLException    If the statement is not open,
                            the parameter name is not valid, or
                            the parameter is not an input parameter.
@since Modification 5
**/
    //
    // Implementation note:
    //
    // The spec defines this in terms of SQL BIGINT, but DB2 for OS/400
    // does not support that until V4R5.
    //
    public void setLong(String parameterName, long parameterValue) 
    throws SQLException
    {
        setLong(findParameterIndex(parameterName), parameterValue);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to SQL NULL.

@param  parameterName   The parameter name.
@param  sqlType         The SQL type code defined in java.sql.Types.

@exception  SQLException    If the statement is not open,
                            the parameterName is not valid,
                            the parameter is not an input parameter,
                            or the SQL type is not valid.
@since Modification 5
**/
    public void setNull(String parameterName, int sqlType) 
    throws SQLException
    {
        setNull(findParameterIndex(parameterName), sqlType);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to SQL NULL.

@param  parameterName  The parameter name.
@param  sqlType         The SQL type code defined in java.sql.Types.
@param  typeName        The fully-qualified name of an SQL structured type.  This value will be ignored.

@exception  SQLException    If the statement is not open,
                            the index is not valid,
                            the parameter is not an input parameter,
                            or the SQL type is not valid.
@since Modification 5
**/
    public void setNull(String parameterName, int sqlType, String typeName) 
    throws SQLException
    {
        setNull(findParameterIndex(parameterName), sqlType, typeName);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to an Object value.  The driver converts
this to a value of an SQL type, depending on the type of the
specified value.  The JDBC specification defines a standard
mapping from Java types to SQL types.  In the cases where a
SQL type is not supported by DB2 UDB for iSeries, the
<a href="../../../../SQLTypes.html#unsupported">next closest matching type</a> 
is used.
<br>If proxy support is in use, the Object must be serializable.

@param  parameterName   The parameter name.
@param  parameterValue  The parameter value or null to set
                        the value to SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid,
                            the parameter is not an input parameter,
                            the type of value is not supported,
                            or the parameter is not serializable
                            (when proxy support is in use).
@since Modification 5
**/
    public void setObject(String parameterName, Object parameterValue) 
    throws SQLException
    {
        setObject(findParameterIndex(parameterName), parameterValue);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to an Object value.  The driver converts
this to a value with the specified SQL type.
<br>If proxy support is in use, the Object must be serializable.

@param  parameterName   The parameter name.
@param  parameterValue  The parameter value or null to set
                        the value to SQL NULL.
@param  targetSQLType   The SQL type code defined in java.sql.Types.

@exception  SQLException    If the statement is not open,
                            the index is not valid,
                            the parameter is not an input parameter,
                            the SQL type is not valid,
                            the scale is not valid,
                            or the parameter is not serializable
                            (when proxy support is in use).
@since Modification 5
**/
    public void setObject(String parameterName, Object parameterValue, int targetSqlType) 
    throws SQLException
    {
        setObject(findParameterIndex(parameterName), parameterValue, targetSqlType);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to an Object value.  The driver converts
this to a value with the specified SQL type.
<br>If proxy support is in use, the Object must be serializable.

@param  parameterName   The parameter name.
@param  parameterValue  The parameter value or null to set
                        the value to SQL NULL.
@param  targetSQLType   The SQL type code defined in java.sql.Types.
@param  scale           The number of digits after the decimal
                        if sqlType is DECIMAL or NUMERIC.

@exception  SQLException    If the statement is not open,
                            the index is not valid,
                            the parameter is not an input parameter,
                            the SQL type is not valid,
                            the scale is not valid,
                            or the parameter is not serializable
                            (when proxy support is in use).
@since Modification 5
**/
    public void setObject(String parameterName, Object parameterValue, int targetSqlType, int scale) 
    throws SQLException
    {
        setObject(findParameterIndex(parameterName), parameterValue, targetSqlType, scale);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to a Java short value.  The driver
converts this to an SQL SMALLINT value.

@param  parameterName   The parameter name.
@param  parameterValue  The parameter value.

@exception  SQLException    If the statement is not open,
                            the parameter name is not valid or
                            the parameter is not an input parameter.
@since Modification 5
**/
    public void setShort(String parameterName, short parameterValue) 
    throws SQLException
    {
        setShort(findParameterIndex(parameterName), parameterValue);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to a String value.  The driver
converts this to an SQL VARCHAR value.

@param  parameterName   The parameter name.
@param  parameterValue  The parameter value or null to set
                        the value to SQL NULL.

@exception  SQLException    If the statement is not open,
                            the parameter name is not valid, or the parameter
                            is not an input parameter.
@since Modification 5
**/
    public void setString(String parameterName, String parameterValue) 
    throws SQLException
    {
        setString(findParameterIndex(parameterName), parameterValue);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to a java.sql.Time value using the
default calendar.  The driver converts this to an SQL TIME value.
    
@param  parameterName   The parameter name.
@param  parameterValue  The parameter value or null to set
                        the value to SQL NULL.
    
@exception  SQLException    If the statement is not open,
                            the parameter name is not valid, or the parameter
                            is not an input parameter.
@since Modification 5
**/
    public void setTime(String parameterName, Time parameterValue) 
    throws SQLException
    {
        setTime(findParameterIndex(parameterName), parameterValue);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to a java.sql.Time value using a calendar
other than the default.  The driver converts this to an SQL TIME
value.
    
@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value or null to set
                        the value to SQL NULL.
@param  calendar        The calendar.
    
@exception  SQLException    If the statement is not open,
                            the index is not valid, the parameter
                            is not an input parameter,
                            or the calendar is null.
@since Modification 5
**/
    public void setTime(String parameterName, Time parameterValue, Calendar cal) 
    throws SQLException
    {
        setTime(findParameterIndex(parameterName), parameterValue, cal);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to a java.sql.Timestamp value using the
default calendar.  The driver converts this to an SQL TIMESTAMP
value.
    
@param  parameterName   The parameter name.
@param  parameterValue  The parameter value or null to set
                        the value to SQL NULL.
    
@exception  SQLException    If the statement is not open,
                            the parameter name is not valid, or the parameter
                            is not an input parameter.
@since Modification 5
**/
    public void setTimestamp(String parameterName, Timestamp parameterValue) 
    throws SQLException
    {
        setTimestamp(findParameterIndex(parameterName), parameterValue);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to a java.sql.Timestamp value using a
calendar other than the default.  The driver converts this to
an SQL TIMESTAMP value.
    
@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value or null to set
                        the value to SQL NULL.
@param  calendar        The calendar.
    
@exception  SQLException    If the statement is not open,
                            the index is not valid, the parameter
                            is not an input parameter,
                            or the calendar is null.
@since Modification 5
**/
    public void setTimestamp(String parameterName, Timestamp parameterValue, Calendar cal) 
    throws SQLException
    {
        setTimestamp(findParameterIndex(parameterName), parameterValue, cal);
    }



//@G4A JDBC 3.0
/**
Sets an input parameter to a URL value.  The driver converts this to an
SQL DATALINK value.

@param  parameterName   The parameter name.
@param  parameterValue  The parameter value or null to set
                        the value to SQL NULL.

@exception  SQLException    If the statement is not open,
                            the parameter name is not valid, or the parameter
                            is not an input parameter.
@since Modification 5
**/
    public void setURL(String parameterName, URL parameterValue) 
    throws SQLException
    {
        setURL(findParameterIndex(parameterName), parameterValue);
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
