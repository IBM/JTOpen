///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCCallableStatement.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
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
/* ifdef JDBC40 
import java.sql.NClob;
endif */ 
import java.sql.Ref;
/* ifdef JDBC40 
import java.sql.ResultSet;              //@G4A
import java.sql.RowId;
endif */ 
import java.sql.SQLException;
/* ifdef JDBC40 
import java.sql.SQLXML;
import java.sql.Statement;              //@G4A
endif */ 
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Map;
/* ifdef JDBC40 
import java.util.Hashtable;             //@G4A
import java.util.Vector;
endif */ 


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
public class AS400JDBCCallableStatement
extends AS400JDBCPreparedStatement
implements CallableStatement
{
    static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";

    static final int    NO_VALIDATION_  = -9999;

    private int[]               registeredTypes_; // array of types to track what the user registers the output parm as
    private boolean[]           registered_;      // array of booleans to keep track of which parameters were registered

    private boolean             returnValueParameterRegistered_;        // @E2A
    private boolean             wasNull_;
    private boolean             wasDataMappingError_;

    //private String[]            parameterNames_; //@PDD jdbc40 move to preparedStatement 
    private int                 maxToLog_ = 10000;        // Log value of parameter markers up to this length // @G7A

    private Object byteArrayClass_;

    /**
    Constructs an AS400JDBCCallableStatement object.
    
    @param   connection             The connection to the system.
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
    AS400JDBCCallableStatement(AS400JDBCConnection connection,
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
        super(connection, id, transactionManager,
              packageManager, blockCriteria, blockSize,
              false, sqlStatement, true, packageCriteria,
              resultSetType, resultSetConcurrency, resultSetHoldability,
              generatedKeysRequested);

        registeredTypes_ = new int[parameterCount_];
        registered_ = new boolean[parameterCount_];
        for(int i = 0; i < parameterCount_; ++i)
        {
            registered_[i] = false;
        }

        returnValueParameterRegistered_ = false;                            // @E2A

        wasNull_ = false;
        wasDataMappingError_ = false;
    }

    // @C1A
    /**
    Performs common operations needed before an execute.
    
    @param  sqlStatement    The SQL statement.
    @param  request         The execute request.
    
    @exception      SQLException    If an error occurs.
    **/
    void commonExecuteBefore(JDSQLStatement sqlStatement, DBSQLRequestDS request)
    throws SQLException
    {
        // Validate each parameters.   If a parameter is not an
        // output parameter, then it is okay for it not to have been
        // registered.  However, if an output parameter was not
        // registered, we throw an exception.
        for(int i = 0; i < parameterCount_; ++i)
            if((registered_[i] == false) && (parameterRow_.isOutput(i+1)))
                JDError.throwSQLException(this, JDError.EXC_PARAMETER_COUNT_MISMATCH);

        super.commonExecuteBefore(sqlStatement, request);
    }

    //@G4A
    /*
    Find the column index that matches this parameter name.
    @param  parameterName    The parameter name to change into a column index (1-based).
    */
    //@PDD jdbc40 move method to preparedStatement
    /*int findParameterIndex(String parameterName)
    throws SQLException
    {                                                          
        // Throw an exception if null was passed in
        if(parameterName == null)
            JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        // Throw an exception if the Statement is closed (FUNCTION SEQUENCE)
        if(isClosed())
            JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE);

        boolean caseSensitive = false;
        int count = 0;
        int returnParm = 0;

        // determine if our search should be case insensitive or not
        if(parameterName.startsWith("\"") && parameterName.endsWith("\"")) // assume no leading or trailing blanks

        {
            parameterName = JDUtilities.stripOuterDoubleQuotes(parameterName);

            caseSensitive = true;
        }

        // If we have a cache created, try to find the column name in it.
        if(parameterNames_ != null)
        {
            // Look up the mapping in our cache.
            while(count < parameterNames_.length)
            {
              if (parameterNames_[count] != null)
              {
                if((caseSensitive && parameterNames_[count].equals(parameterName))
                   || (!caseSensitive && parameterNames_[count].equalsIgnoreCase(parameterName)))
                {
                    returnParm = count+1;
                    break;
                }
              }

              ++count;
            }
        }
        else
        {
            // Else, create a new hash table to hold all the column name/number mappings.
            parameterNames_ = new String[parameterCount_];

            // Cache all the parm names and numbers.
            Statement s = null; //@scan1
            ResultSet rs = null; //@scan1
            try{
            s = connection_.createStatement();
            String catalogSeparator = "";                                                           //@74A Added a check for the naming used.  Need to use separator appropriate to naming.
            if (connection_.getProperties().equals (JDProperties.NAMING, JDProperties.NAMING_SQL))  //@74A
                catalogSeparator = ".";                                                             //@74A
            else                                                                                    //@74A
                catalogSeparator = "/";                                                             //@74A

            String schema = sqlStatement_.getSchema();
            if(schema == null || schema.equals(""))  // no schema in statement
            { // Derive the schema.
              schema = connection_.getDefaultSchema(true); // get raw value

              if(schema == null)	// No default SQL schema was set on the connection url, or by the libraries connection property.
              {
                if(catalogSeparator.equals(".")) // using sql naming
                {
                  schema = connection_.getUserName(); // set to user profile
                }
                else // using system naming
                {
                  // Retrieve the library list from the IBM i - Use ROI Retrieve Library List.
                  ResultSet rs1 = JDUtilities.getLibraries(this, connection_, null, true);
                  Vector libListV = new Vector();
                  while(rs1.next()) {
                    libListV.addElement(rs1.getString(1));
                  }
                  rs1.close(); //@SS
                  String[] libList = new String[libListV.size()];
                  libListV.toArray(libList);

                  // Get a result set that we can scroll forward/backward through.
                  Statement s1 = connection_.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                  rs = s1.executeQuery("SELECT ROUTINE_SCHEMA FROM QSYS2"
                                  + catalogSeparator
                                  + "SYSPROCS WHERE ROUTINE_NAME='"
                                  + unquote(sqlStatement_.getProcedure())
                                  + "' AND IN_PARMS + OUT_PARMS + INOUT_PARMS = "
                                  + parameterCount_);//@scan1
                  if(!rs.next())
                    JDError.throwSQLException(this, JDError.EXC_INTERNAL);	// didn't find the procedure in any schema

                  // If we get this far, at least one schema contains a procedure similar to ours.
                  boolean found = false;
                  for(int i=0; i<libList.length && !found; i++)
                  {
                    if (libList[i].length() != 0) {
                      rs.beforeFirst();	// re-position to before the first row
                      while(rs.next() && !found){
                        if(rs.getString(1).equals(libList[i])) {
                          schema = rs.getString(1);
                          found = true; // we found a procedure that matches our criteria
                        }
                      }
                    }
                  }
                  try{
                      rs.close(); //@SS
                  }catch(Exception e){} //allow next close to execute
                  s1.close(); //@SS
                  if(!found)	// none of the libraries in our library list contain a stored procedure that we are looking for
                    JDError.throwSQLException(this, JDError.EXC_INTERNAL);
                }
              }
            }

            rs = s.executeQuery("SELECT SPECIFIC_NAME FROM QSYS2" + catalogSeparator + "SYSPROCS WHERE ROUTINE_SCHEMA = '" + unquote(schema) + //@74C @DELIMc
                                          "' AND ROUTINE_NAME = '" + unquote(sqlStatement_.getProcedure()) + //@DELIMc
                                          "' AND IN_PARMS + OUT_PARMS + INOUT_PARMS = " + parameterCount_);

            // If there are no rows, throw an internal driver exception
            if(!rs.next())
                JDError.throwSQLException(this, JDError.EXC_INTERNAL);

            String specificName = rs.getString(1);
            rs.close(); //@SS
            
            rs = s.executeQuery("SELECT PARAMETER_NAME, ORDINAL_POSITION FROM QSYS2" + catalogSeparator + "SYSPARMS WHERE " + //@74A
                                " SPECIFIC_NAME = '" + unquoteNoUppercase(specificName) + "' AND SPECIFIC_SCHEMA = '" + unquote(schema) + "'"); //@DELIMc

            while(rs.next())
            {
                count++;
    
                String colName = rs.getString(1);
                int colInd = rs.getInt(2);
                parameterNames_[colInd-1] = colName; 
    
                if(caseSensitive && colName.equals(parameterName))
                    returnParm = colInd;
                else if(!caseSensitive && colName.equalsIgnoreCase(parameterName))
                    returnParm = colInd;
            }
            }finally //@scan1
            {
                try{
                    if(rs != null) //@scan1
                        rs.close(); //@SS
                }catch(Exception e){} //allow next close to execute
                if(s != null)  //@scan1
                    s.close();  //@SS
            }
    
            // If the number of parm names didn't equal the number of parameters, throw
            // an exception (INTERNAL).
            if(count != parameterCount_) {
                JDError.throwSQLException(this, JDError.EXC_INTERNAL);
            }
    
        }

        // Throw an exception if the column name is not found (COLUMN NOT FOUND). 
        if(returnParm == 0)
            JDError.throwSQLException(this, JDError.EXC_COLUMN_NOT_FOUND);

        return returnParm;
    }

    // JDBC 2.0
    /**
    Returns the value of an SQL ARRAY output parameter as an Array value.
    
    @param  parameterIndex  The parameter index (1-based).
    @return                 The parameter value or null if the value is SQL NULL.
    
    @exception  SQLException   If the statement is not open,
                                the index is not valid, the index is
                                not registered as an output parameter,
                                the statement was not executed, or
                                the requested conversion is not valid.
    **/
    public Array getArray(int parameterIndex)
    throws SQLException
    {
        //@array3 implement this method
        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(!parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // make sure the registered type is valid for this get method
                switch(registeredTypes_[parameterIndex-1]) {
                case Types.ARRAY:
                case Types.JAVA_OBJECT:
                  break;
                  default:
                    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                    
                }

                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            Array value = (data == null) ? null : data.getArray();
            testDataTruncation(parameterIndex, data, true);
            return value;
        }
    }

    //@G4A JDBC 3.0
    /**
    Returns the value of an SQL ARRAY output parameter as an Array value.
    DB2 for IBM i does not support arrays.
        
    @param  parameterName   The parameter name.
    @return                 The parameter value or 0 if the value is SQL NULL.
        
    @exception  SQLException    If the statement is not open,
                                the index is not valid, the index is
                                not registered as an output parameter,
                                the statement was not executed, or
                                the requested conversion is not valid.
                                
    **/
    public Array getArray(String parameterName)
    throws SQLException
    {
        return getArray(findParameterIndex(parameterName)); //@array3
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
    public BigDecimal getBigDecimal(int parameterIndex)
    throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(!parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registeredTypes_[parameterIndex-1] == -1)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // make sure the registered type is valid for this get method
                validateNumericRegisteredType(registeredTypes_[parameterIndex-1]); 

                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            BigDecimal value = (data == null) ? null : data.getBigDecimal(-1);
            testDataTruncation(parameterIndex, data, true);
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
    public BigDecimal getBigDecimal(int parameterIndex, int scale)
    throws SQLException
    {
        // Check for negative scale.
        if(scale < 0)
            JDError.throwSQLException(this, JDError.EXC_SCALE_INVALID);

        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(!parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // make sure the registered type is valid for this get method
                validateNumericRegisteredType(registeredTypes_[parameterIndex-1] ); 

                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            BigDecimal value = (data == null) ? null : data.getBigDecimal(scale);
            testDataTruncation(parameterIndex, data, true);
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
    **/
    public BigDecimal getBigDecimal(String parameterName)
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
    public Blob getBlob(int parameterIndex)
    throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(!parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // make sure the registered type is valid for this get method
        switch (registeredTypes_[parameterIndex - 1]) {
        case Types.BLOB:
        case Types.JAVA_OBJECT:
          break;
        default:
          JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        }

                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            Blob value = (data == null) ? null : data.getBlob();
            testDataTruncation(parameterIndex, data, false);
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
    **/
    public Blob getBlob(String parameterName)
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
    // The spec defines this in terms of SQL BIT, but DB2 for IBM i
    // does not support that.
    //
    public boolean getBoolean(int parameterIndex)
    throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(!parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // make sure the registered type is valid for this get method
                // 16 is the value of Types.BOOLEAN added for JDK 1.4
                validateNumericRegisteredType(registeredTypes_[parameterIndex-1]); 

                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            boolean value = (data == null) ? false : data.getBoolean();
            testDataTruncation(parameterIndex, data, true);
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
    **/
    //
    // Implementation note:
    //
    // The spec defines this in terms of SQL BIT, but DB2 for IBM i
    // does not support that.
    //
    public boolean getBoolean(String parameterName)
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
    // The spec defines this in terms of SQL TINYINT, but DB2 for IBM i
    // does not support that.
    //
    public byte getByte(int parameterIndex)
    throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(!parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // make sure the registered type is valid for this get method
                validateNumericRegisteredType(registeredTypes_[parameterIndex-1]); 

                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            byte value = (data == null) ? 0 : data.getByte();
            testDataTruncation(parameterIndex, data, true);
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
    **/
    //
    // Implementation note:
    //
    // The spec defines this in terms of SQL TINYINT, but DB2 for IBM i 
    // does not support that.
    //
    public byte getByte(String parameterName)
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
    public byte[] getBytes(int parameterIndex)
    throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(!parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // make sure the registered type is valid for this get method
                //if(registeredTypes_[parameterIndex-1] != Types.BINARY && registeredTypes_[parameterIndex-1] != Types.VARBINARY)
                //    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);

                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            byte[] value = (data == null) ? null : data.getBytes();
            testDataTruncation(parameterIndex, data, false);
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
    **/
    public byte[] getBytes(String parameterName)
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
    public Clob getClob(int parameterIndex)
    throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(! parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // make sure the registered type is valid for this get method
                switch (registeredTypes_[parameterIndex - 1]) {
                  case Types.CLOB:
                  case 2011: /* nclob */ 
                  case Types.JAVA_OBJECT:
                    break; 
                    default:
                      JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
  
                }
                    
                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            Clob value = (data == null) ? null : data.getClob();
            testDataTruncation(parameterIndex, data, false);
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
    **/
    public Clob getClob(String parameterName)
    throws SQLException
    {
        return getClob(findParameterIndex(parameterName));    
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
    public Date getDate(int parameterIndex)
    throws SQLException
    {
        return getDate(parameterIndex, AS400Calendar.getGregorianInstance());
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
    public Date getDate(int parameterIndex, Calendar calendar)
    throws SQLException
    {
        // Check for null calendar.
        if(calendar == null)
            JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(! parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // make sure the registered type is valid for this get method
                // Changed 9/29/2011 for JDBC 4.1 
                // The getter method says this can be used again several type other than Types.DATE
                switch(registeredTypes_[parameterIndex-1]) {
                  case Types.DATE:
                  case Types.CHAR:
                  case Types.VARCHAR:
                  case Types.LONGVARCHAR:
                  case Types.TIMESTAMP:
                  case Types.JAVA_OBJECT:
                  break; 
                  default: 
                    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                }

                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            Date value = (data == null) ? null : data.getDate(calendar);
            testDataTruncation(parameterIndex, data, false);
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
    **/

    public Date getDate(String parameterName)
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
    public Date getDate(String parameterName, Calendar calendar)
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
    public double getDouble(int parameterIndex)
    throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(! parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // make sure the registered type is valid for this get method
                validateNumericRegisteredType(registeredTypes_[parameterIndex-1]); 

                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            double value = (data == null) ? 0 : data.getDouble();
            testDataTruncation(parameterIndex, data, true);
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
    **/
    public double getDouble(String parameterName)
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
    public float getFloat(int parameterIndex)
    throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(! parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // make sure the registered type is valid for this get method
                validateNumericRegisteredType(registeredTypes_[parameterIndex-1]);

                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            float value = (data == null) ? 0 : data.getFloat();
            testDataTruncation(parameterIndex, data, true);
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
    **/
    public float getFloat(String parameterName)
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
    public int getInt(int parameterIndex)
    throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(! parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                validateNumericRegisteredType(registeredTypes_[parameterIndex-1]); 
                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            int value = (data == null) ? 0 : data.getInt();
            testDataTruncation(parameterIndex, data, true);
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
    **/
    public int getInt(String parameterName)
    throws SQLException
    {
        return getInt(findParameterIndex(parameterName));
    }

    // @D0C
    /**
    If the connected system supports SQL BIGINT data, this returns
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
    // The spec defines this in terms of SQL BIGINT, but DB2 for IBM i
    // does not support that until V4R5.
    //
    public long getLong(int parameterIndex)
    throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(! parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // make sure the registered type is valid for this get method
                validateNumericRegisteredType(registeredTypes_[parameterIndex-1]); 

                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            long value = (data == null) ? 0 : data.getLong();
            testDataTruncation(parameterIndex, data, true);
            return value;
        }
    }

    //@G4A  JDBC 3.0
    /**
    If the connected system supports SQL BIGINT data, this returns
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
    **/
    //
    // Implementation note:
    //
    // The spec defines this in terms of SQL BIGINT, but DB2 for IBM i 
    // does not support that until V4R5.
    //
    public long getLong(String parameterName)
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
    public Object getObject(int parameterIndex)
    throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(! parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            if(data == null)
                return null;
            Object value = (data == null) ? null : data.getObject();
            testDataTruncation(parameterIndex, data, false);
            return value;
        }
    }

    // JDBC 2.0
    /**
    Returns the value of an output parameter as a Java Object.
    This driver does not support the type map.
    
    @param  parameterIndex  The parameter index (1-based).
    @param  typeMap            The type map.  This is not used.
    @return                 The parameter value or null if the value is SQL NULL.
    
    @exception  SQLException    If the statement is not open,
                                the index is not valid, the index is
                                not registered as an output parameter,
                                the statement was not executed, or
                                the requested conversion is not valid.
    **/
    public Object getObject(int parameterIndex, Map typeMap)
    throws SQLException
    {
        // Check for null type map, even though we don't use it.
        if(typeMap == null)
            JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        return getObject(parameterIndex);
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
    **/
    public Object getObject(String parameterName)
    throws SQLException
    {
        return getObject(findParameterIndex(parameterName));
    }

    //@G4A JDBC 3.0
    /**
    Returns the value of an output parameter as a Java Object.
    This driver does not support the type map.
        
    @param  parameterName   The parameter name.
    @param  typeMap            The type map.  This is not used.
    @return                 The parameter value or null if the value is SQL NULL.
        
    @exception  SQLException    If the statement is not open,
                                the index is not valid, the index is
                                not registered as an output parameter,
                                the statement was not executed, or
                                the requested conversion is not valid.
    **/
    public Object getObject(String parameterName, Map typeMap)
    throws SQLException
    {
        return getObject(findParameterIndex(parameterName));
    }

    // JDBC 2.0
    /**
    Returns the value of an SQL REF output parameter as a Ref value.
    DB2 for IBM i does not support structured types.
    
    @param  parameterIndex  The parameter index (1-based).
    @return                 The parameter value or 0 if the value is SQL NULL.
    
    @exception  SQLException    Always thrown because DB2 for IBM i does not support REFs.
    **/
    public Ref getRef(int parameterIndex)
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    //@G4A  JDBC 3.0
    /**
    Returns the value of an SQL REF output parameter as a Ref value.
    DB2 for IBM i does not support structured types.
    
    @param  parameterName   The parameter name.
    @return                 The parameter value or 0 if the value is SQL NULL.
    
    @exception  SQLException    Always thrown because DB2 for IBM i does not support REFs.
    **/
    public Ref getRef(String parameterName)
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
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
    public short getShort(int parameterIndex)
    throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(! parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // make sure the registered type is valid for this get method
                validateNumericRegisteredType(registeredTypes_[parameterIndex-1]);
                
                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            short value = (data == null) ? 0 : data.getShort();
            testDataTruncation(parameterIndex, data, true);
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
    **/
    public short getShort(String parameterName)
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
    public String getString(int parameterIndex)
    throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(! parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // make sure the registered type is valid for this get method
                //if(registeredTypes_[parameterIndex-1] != Types.CHAR && registeredTypes_[parameterIndex-1] != Types.VARCHAR)
                //    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);

                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            String value = (data == null) ? null : data.getString();
            testDataTruncation(parameterIndex, data, false);
            return value;
        }
    }

    //@G4A JDBC 3.0
    /**
    Returns the value of an SQL CHAR or VARCHAR output
    parameter as a Java String object.
    
    @param  parameterName  The parameter name.
    @return                 The parameter value or null if the value is SQL NULL.
    
    @exception  SQLException    If the statement is not open,
                                the index is not valid, the index is
                                not registered as an output parameter,
                                the statement was not executed, or
                                the requested conversion is not valid.
    **/
    public String getString(String parameterName)
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
    public Time getTime(int parameterIndex)
    throws SQLException
    {
        return getTime(parameterIndex, AS400Calendar.getGregorianInstance());
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
    public Time getTime(int parameterIndex, Calendar calendar)
    throws SQLException
    {
        // Check for null calendar.
        if(calendar == null)
            JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(! parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // make sure the registered type is valid for this get method
                // Changed 9/29/2011 for JDBC 4.1 
                // The getter method says this can be used again several type other than Types.Time

                switch(registeredTypes_[parameterIndex-1]) {
                  case Types.TIME:
                  case Types.CHAR:
                  case Types.VARCHAR:
                  case Types.LONGVARCHAR:
                  case Types.TIMESTAMP:
                  case Types.JAVA_OBJECT:
                  break;
                  default: 
                    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                }

                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            Time value = (data == null) ? null : data.getTime(calendar);
            testDataTruncation(parameterIndex, data, false);
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
    **/
    public Time getTime(String parameterName)
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
    **/
    public Time getTime(String parameterName, Calendar calendar)
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
    public Timestamp getTimestamp(int parameterIndex)
    throws SQLException
    {
        return getTimestamp(parameterIndex, AS400Calendar.getGregorianInstance());
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
    public Timestamp getTimestamp(int parameterIndex, Calendar calendar)
    throws SQLException
    {
        // Check for null calendar.
        if(calendar == null)
            JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(! parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // make sure the registered type is valid for this get method
                // Changed 9/29/2011 for JDBC 4.1 
                // The getter method says this can be used again several type other than Types.TIMESTAMP
                switch(registeredTypes_[parameterIndex-1]){
                
                  case Types.TIMESTAMP:
                  case Types.CHAR:
                  case Types.VARCHAR:
                  case Types.LONGVARCHAR:
                  case Types.TIME:
                  case Types.DATE:
                  case Types.JAVA_OBJECT:
                     break;
                  default:
                    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                    
                }

                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            Timestamp value = (data == null) ? null : data.getTimestamp(calendar);
            testDataTruncation(parameterIndex, data, false);
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
    **/
    public Timestamp getTimestamp(String parameterName)
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
    **/
    public Timestamp getTimestamp(String parameterName, Calendar calendar)
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
    **/
    public URL getURL(int parameterIndex) throws SQLException {
    synchronized (internalLock_) {
      checkOpen();

      SQLData data = null;

      // Check if the parameter index refers to the return value parameter.
      // If it is not parameter index 1, then decrement the parameter index,
      // since we are "faking" the return value parameter.
      if (useReturnValueParameter_ && parameterIndex == 1) {
        if (!returnValueParameterRegistered_)
          JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

        data = returnValueParameter_;
      } else {
        if (useReturnValueParameter_) {
          --parameterIndex;
        }

        // Validate the parameter index.
        if ((parameterIndex < 1) || (parameterIndex > parameterCount_))
          JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

        // Check that the parameter is an output parameter.
        if (!parameterRow_.isOutput(parameterIndex))
          JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

        // Verify that the output parameter is registered.
        if (registered_[parameterIndex - 1] == false)
          JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

        // make sure the registered type is valid for this get method
        switch (registeredTypes_[parameterIndex - 1]) {
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.DATALINK:
        case Types.JAVA_OBJECT:
          break;
        default:
          JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        }

        // Get the data and check for SQL NULL.
        data = getValue(parameterIndex);
      }

      String value = (data == null) ? null : data.getString();
      testDataTruncation(parameterIndex, data, false);
      if (value != null) {
        try {
          return new java.net.URL(value);
        } catch (MalformedURLException e) {
          JDError
              .throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, e);
          return null;
        }
      } else {
        return null;
      }
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
    **/
    public URL getURL(String parameterName)
    throws SQLException
    {
        return getURL(findParameterIndex(parameterName));
    }

    /**
    Returns the value for an output parameter for the specified
    index, and performs all appropriate validation.  Also checks
    for SQL NULL.
    
    @param  parameterIndex  The parameter index (1-based).
    @return                 The parameter value or null if the value is SQL NULL.
    
    @exception  SQLException    If the statement is not open,
                                the index is not valid, the index is
                                not registered as an output parameter,
                                the statement was not executed, or
                                the requested conversion is not valid.
    **/
    private SQLData getValue(int parameterIndex)
    throws SQLException
    {
        // Verify that the statement has been executed.
        if(! executed_)
            JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE);

        // Get the data and check for SQL NULL.
        SQLData data = parameterRow_.getSQLData(parameterIndex);
        wasNull_ = parameterRow_.isNull(parameterIndex);
        wasDataMappingError_ = parameterRow_.isDataMappingError(parameterIndex);

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
    public void registerOutParameter(int parameterIndex,
                                     int sqlType,
                                     int scale)
    throws SQLException
    {
        synchronized(internalLock_)
        {                                            // @E1A
            checkOpen();

            // Check if the parameter index refers to the return value parameter.              @E2A
            // If so, it must be registed as an INTEGER.                                       @E2A
            // If it is not parameter index 1, then decrement the parameter index,             @E2A
            // since we are "faking" the return value parameter.                               @E2A
            if(useReturnValueParameter_)
            {                                                 // @E2A
                if(parameterIndex == 1)
                {                                                  // @E2A
                    if(sqlType != Types.INTEGER)                                           // @E2A
                        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);         // @E2A
                    returnValueParameterRegistered_ = true;                                 // @E2A
                    return;                                                                 // @E2A
                }                                                                           // @E2A
                else                                                                        // @E2A
                    --parameterIndex;                                                       // @E2A
            }                                                                               // @E2A

            // Validate the parameter index.
            if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

            // Validate the scale.
            if(scale < 0)
                JDError.throwSQLException(this, JDError.EXC_SCALE_INVALID);

            // Check that the parameter is an output parameter.
            if(!parameterRow_.isOutput(parameterIndex))
                JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

            registeredTypes_[parameterIndex-1] = sqlType;
            registered_[parameterIndex-1] = true;
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
    public void registerOutParameter(int parameterIndex, int sqlType)
    throws SQLException
    {
        registerOutParameter(parameterIndex, sqlType, 0);
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
    public void registerOutParameter(int parameterIndex, int sqlType, String typeName)
    throws SQLException
    {
        registerOutParameter(parameterIndex, sqlType, 0);
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
    **/
    public void registerOutParameter(String parameterName, int sqlType)
    throws SQLException
    {
        registerOutParameter(findParameterIndex(parameterName), sqlType, 0);
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
    **/
    public void registerOutParameter(String parameterName, int sqlType, int scale)
    throws SQLException
    {
        registerOutParameter(findParameterIndex(parameterName), sqlType, scale);
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
    **/
    public void registerOutParameter(String parameterName, int sqlType, String typeName)
    throws SQLException
    {
        registerOutParameter(findParameterIndex(parameterName), sqlType, 0);
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
    **/
    public void setAsciiStream(String parameterName, InputStream parameterValue, int length) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setAsciiStream()");             // @G7A
            if(parameterValue == null)                                  // @G7A
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName)  + " value: NULL");  // @G7A
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " length: " + length); // @G7A
        }                                                                  // @G7A

        setAsciiStream(findParameterIndex(parameterName), parameterValue, length);
    }

    //@G4A JDBC 3.0
    /**
    Sets an input parameter to a BigDecimal value.  The driver converts
    this to an SQL NUMERIC value.
    
    @param  parameterName  The parameter name.
    @param  parameterValue  The parameter value or null to set
                            the value to SQL NULL.
    
    @exception  SQLException    If the statement is not open,
                                the index is not valid, or the parameter
                                is not an input parameter.
    **/
    public void setBigDecimal(String parameterName, BigDecimal parameterValue) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setBigDecimal()");              // @G7A
            if(parameterValue == null)                                  // @G7A
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");  // @G7A
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: " + parameterValue.toString());  // @G7A
        }                                                                  // @G7A

        setBigDecimal(findParameterIndex(parameterName), parameterValue);
    }

    //@G4A JDBC 3.0
    /**
    Sets an input parameter to a binary stream value.  The driver
    reads the data from the stream as needed until no more bytes
    are available.  The driver converts this to an SQL VARBINARY
    value.
        
    @param  parameterName  The parameter name.
    @param  parameterValue  The parameter value or null to set
                            the value to SQL NULL.
    @param  length          The number of bytes in the stream.
        
    @exception  SQLException    If the statement is not open,
                                the index is not valid, the parameter
                                is not an input parameter,
                                the length is not valid, or
                                an error occurs while reading the
                                input stream.
    **/
    public void setBinaryStream(String parameterName, InputStream parameterValue, int length) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setBinaryStream()");            // @G7A
            if(parameterValue == null)                                  // @G7A
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");  // @G7A
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " length: " + length);  // @G7A
        }                                                                  // @G7A

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
    // The spec defines this in terms of SQL BIT, but DB2 for IBM i 
    // does not support that.
    //
    public void setBoolean(String parameterName, boolean parameterValue) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setBoolean()");                 // @G7A
            JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: " + parameterValue);  // @G7A
        }                                                                  // @G7A

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
    **/
    //
    // Implementation note:
    //
    // The spec defines this in terms of SQL TINYINT, but DB2 for IBM i 
    // does not support that.
    //
    public void setByte(String parameterName, byte parameterValue) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setByte()");                    // @G7A
            JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: " + parameterValue);  // @G7A
        }                                                                  // @G7A

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
    **/
    public void setBytes(String parameterName, byte[] parameterValue) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setBytes()");                   // @G7A
            if(parameterValue == null)                                  // @G7A
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");  // @G7A
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: " + parameterValue.toString()); // @G7A
        }                                                                  // @G7A

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
    **/
    public void setCharacterStream(String parameterName, Reader parameterValue, int length) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setCharacterStream()");         // @G7A
            if(parameterValue == null)                                  // @G7A
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName)  + " value: NULL");  // @G7A
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " length: " + length); // @G7A
        }                                                                  // @G7A

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
                                
    **/
    public void setDate(String parameterName, Date parameterValue) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setDate()");                    // @G7A
            if(parameterValue == null)                                  // @G7A
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName)  + " value: NULL");  // @G7A
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: " + parameterValue.toString()); // @G7A
        }                                                                  // @G7A

        setDate(findParameterIndex(parameterName), parameterValue);
    }

    //@G4A JDBC 3.0
    /**
    Sets an input parameter to a java.sql.Date value using a
    calendar other than the default.  The driver converts this
    to an SQL DATE value.
        
    @param  parameterName   The parameter name.
    @param  parameterValue  The parameter value or null to set
                            the value to SQL NULL.
    @param  cal        The calendar.
        
    @exception  SQLException    If the statement is not open,
                                the index is not valid, the parameter
                                is not an input parameter,
                                or the calendar is null.
                                
    **/
    public void setDate(String parameterName, Date parameterValue, Calendar cal) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setDate()");                    // @G7A
            if(parameterValue == null)                                  // @G7A
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName)  + " value: NULL");  // @G7A
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: " + parameterValue.toString()); // @G7A
        }                                                                  // @G7A

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
    **/
    public void setDouble(String parameterName, double parameterValue) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setDouble()");                  // @G7A
            JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: " + parameterValue);  // @G7A
        }                                                                  // @G7A

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
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setFloat()");                   // @G7A
            JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: " + parameterValue);  // @G7A
        }                                                                  // @G7A

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
    **/
    public void setInt(String parameterName, int parameterValue) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setInt()");                     // @G7A
            JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: " + parameterValue);  // @G7A
        }                                                                  // @G7A

        setInt(findParameterIndex(parameterName), parameterValue);
    }

    //@G4A JDBC 3.0
    /**
    Sets an input parameter to a Java long value.
    If the connected system supports SQL BIGINT data, the driver
    converts this to an SQL BIGINT value.  Otherwise, the driver
    converts this to an SQL INTEGER value.  SQL BIGINT data is
    supported on V4R5 and later.
    
    @param  parameterName   The parameter name.
    @param  parameterValue  The parameter value.
    
    @exception  SQLException    If the statement is not open,
                                the parameter name is not valid, or
                                the parameter is not an input parameter.
    **/
    //
    // Implementation note:
    //
    // The spec defines this in terms of SQL BIGINT, but DB2 for IBM i 
    // does not support that until V4R5.
    //
    public void setLong(String parameterName, long parameterValue) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setLong()");                    // @G7A
            JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: " + parameterValue);  // @G7A
        }                                                                  // @G7A

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
    **/
    public void setNull(String parameterName, int sqlType) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setNull()");                    // @G7A
            JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");  // @G7A
        }                                                                  // @G7A

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
    **/
    public void setNull(String parameterName, int sqlType, String typeName) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setNull()");                    // @G7A
            JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");  // @G7A
        }                                                                  // @G7A

        setNull(findParameterIndex(parameterName), sqlType, typeName);
    }

    //@G4A JDBC 3.0
    /**
    Sets an input parameter to an Object value.  The driver converts
    this to a value of an SQL type, depending on the type of the
    specified value.  The JDBC specification defines a standard
    mapping from Java types to SQL types.  In the cases where a
    SQL type is not supported by DB2 for IBM i, the
    <a href="doc-files/SQLTypes.html#unsupported">next closest matching type</a> 
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
    **/
    public void setObject(String parameterName, Object parameterValue) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setObject()");                  // @G7A
            if(parameterValue == null)                                  // @G7A
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName)  + " value: NULL");  // @G7A
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " type: " + parameterValue.getClass().getName()); // @G7A
        }                                                                  // @G7A

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
    @param  targetSqlType   The SQL type code defined in java.sql.Types.
    
    @exception  SQLException    If the statement is not open,
                                the index is not valid,
                                the parameter is not an input parameter,
                                the SQL type is not valid,
                                the scale is not valid,
                                or the parameter is not serializable
                                (when proxy support is in use).
    **/
    public void setObject(String parameterName, Object parameterValue, int targetSqlType) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setObject()");                  // @G7A
            if(parameterValue == null)                                  // @G7A
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName)  + " value: NULL");  // @G7A
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " type: " + parameterValue.getClass().getName()); // @G7A
        }                                                                  // @G7A

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
    @param  targetSqlType   The SQL type code defined in java.sql.Types.
    @param  scale           The number of digits after the decimal
                            if sqlType is DECIMAL or NUMERIC.
    
    @exception  SQLException    If the statement is not open,
                                the index is not valid,
                                the parameter is not an input parameter,
                                the SQL type is not valid,
                                the scale is not valid,
                                or the parameter is not serializable
                                (when proxy support is in use).
    **/
    public void setObject(String parameterName, Object parameterValue, int targetSqlType, int scale) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setObject()");                  // @G7A
            if(parameterValue == null)                                  // @G7A
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName)  + " value: NULL");  // @G7A
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " type: " + parameterValue.getClass().getName()); // @G7A
        }                                                                  // @G7A

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
    **/
    public void setShort(String parameterName, short parameterValue) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setShort()");                   // @G7A
            JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: " + parameterValue);  // @G7A
        }                                                                  // @G7A

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
    **/
    public void setString(String parameterName, String parameterValue) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setString()");                  // @G7A
            if(parameterValue == null)                                  // @G7A
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName)  + " value: NULL");  // @G7A
            else if(parameterValue.length() > maxToLog_)                // @G7A
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " length: " + parameterValue.length());  // @G7A
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: " + parameterValue);  // @G7A
        }                                                                  // @G7A

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
    **/
    public void setTime(String parameterName, Time parameterValue) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setTime()");                    // @G7A
            if(parameterValue == null)                                  // @G7A
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName)  + " value: NULL");  // @G7A
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: " + parameterValue.toString()); // @G7A
        }                                                                  // @G7A

        setTime(findParameterIndex(parameterName), parameterValue);
    }

    //@G4A JDBC 3.0
    /**
    Sets an input parameter to a java.sql.Time value using a calendar
    other than the default.  The driver converts this to an SQL TIME
    value.
        
    @param  parameterName   The parameter name.
    @param  parameterValue  The parameter value or null to set
                            the value to SQL NULL.
    @param  cal        The calendar.
        
    @exception  SQLException    If the statement is not open,
                                the index is not valid, the parameter
                                is not an input parameter,
                                or the calendar is null.
    **/
    public void setTime(String parameterName, Time parameterValue, Calendar cal) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setTime()");                    // @G7A
            if(parameterValue == null)                                  // @G7A
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName)  + " value: NULL");  // @G7A
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: " + parameterValue.toString()); // @G7A
        }                                                                  // @G7A

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
    **/
    public void setTimestamp(String parameterName, Timestamp parameterValue) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setTimeStamp()");               // @G7A
            if(parameterValue == null)                                  // @G7A
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName)  + " value: NULL");  // @G7A
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: " + parameterValue.toString()); // @G7A
        }                                                                  // @G7A

        setTimestamp(findParameterIndex(parameterName), parameterValue);
    }

    //@G4A JDBC 3.0
    /**
    Sets an input parameter to a java.sql.Timestamp value using a
    calendar other than the default.  The driver converts this to
    an SQL TIMESTAMP value.
        
    @param  parameterName   The parameter name.
    @param  parameterValue  The parameter value or null to set
                            the value to SQL NULL.
    @param  cal        The calendar.
        
    @exception  SQLException    If the statement is not open,
                                the index is not valid, the parameter
                                is not an input parameter,
                                or the calendar is null.
    **/
    public void setTimestamp(String parameterName, Timestamp parameterValue, Calendar cal) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setTimeStamp()");               // @G7A
            if(parameterValue == null)                                  // @G7A
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName)  + " value: NULL");  // @G7A
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: " + parameterValue.toString()); // @G7A
        }                                                                  // @G7A

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
    **/
    public void setURL(String parameterName, URL parameterValue) 
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @G7A
            JDTrace.logInformation(this, "setURL()");                     // @G7A
            if(parameterValue == null)                                  // @G7A
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName)  + " value: NULL");  // @G7A
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: " + parameterValue.toString()); // @G7A
        }                                                                  // @G7A

        setURL(findParameterIndex(parameterName), parameterValue);
    }

    /**
    Tests if a DataTruncation occurred on the read of a piece of
    data and posts a DataTruncation warning if so.
    
    @param  parameterIndex  The parameter index (1-based).
    @param  data            The data that was read, or null for SQL NULL.
    @param  exceptionOnTrunc Flag to notify method whether or not to throw an SQLException when there is truncation.
                             numeric types should always throw exception on truncation.  This was added because
                             we now support getMethods for compatible types.  
    **/
    private void testDataTruncation(int parameterIndex, SQLData data, boolean exceptionOnTrunc) throws SQLException
    {
        if(wasDataMappingError_)
        {
            postWarning(new DataTruncation(parameterIndex, true, true, -1, -1));
        }

        if(data != null)
        {
            int truncated = data.getTruncated();

            if(truncated > 0)
            {
              if((exceptionOnTrunc == true))   {  //@trunc
                  if (data.getOutOfBounds()) { 
                     JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH); //@trunc
                  }
              }                                                                    //@trunc
                int actualSize = data.getActualSize();
                postWarning(new DataTruncation(parameterIndex, true, true, actualSize, actualSize - truncated));
            }
        }
    }

    //@pdd jdbc40 move method to preparedStatement
    //@pdd private static final String unquote(String name)
    //@pdd    {
    //@pdd      return JDUtilities.prepareForSingleQuotes(name, true);
    //@pdd    }

    //@pdd jdbc40 move methodto preparedStatement
    //@pdd    private static final String unquoteNoUppercase(String name)
    //@pdd    {
    //@pdd      return JDUtilities.prepareForSingleQuotes(name, false);
    //@pdd    }

    /**
    Indicates if the last parameter read has the
    value of SQL NULL.
    
    @return     true if the value is SQL NULL;
                false otherwise.
    
    @exception  SQLException    If the statement is not open.
    **/
    public boolean wasNull()
    throws SQLException
    {
        synchronized(internalLock_)
        {                                            // @E1A
            checkOpen();
            return wasNull_;
        }
    }
    

    //@pda jdbc40
    protected String[] getValidWrappedList()
    {
        return new String[] { "com.ibm.as400.access.AS400JDBCCallableStatement", "java.sql.CallableStatement" };
    }
    
    //@PDA jdbc40
    /**
     * Retrieves the value of the designated parameter as a
     * <code>java.io.Reader</code> object in the Java programming language.
     *
     * @return a <code>java.io.Reader</code> object that contains the parameter
     * value; if the value is SQL <code>NULL</code>, the value returned is
     * <code>null</code> in the Java programming language.
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @exception SQLException if a database access error occurs or 
     * this method is called on a closed <code>CallableStatement</code>
     */
    public Reader getCharacterStream(int parameterIndex) throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(! parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            Reader value = (data == null) ? null : data.getCharacterStream();
            testDataTruncation(parameterIndex, data, false);
            return value;
        }
    }

    //@PDA jdbc40
    /**
     * Retrieves the value of the designated parameter as a
     * <code>java.io.Reader</code> object in the Java programming language.
     * 
     * @param parameterName the name of the parameter
     * @return a <code>java.io.Reader</code> object that contains the parameter
     * value; if the value is SQL <code>NULL</code>, the value returned is
     * <code>null</code> in the Java programming language
     * @exception SQLException if a database access error occurs or 
     * this method is called on a closed <code>CallableStatement</code>
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public Reader getCharacterStream(String parameterName) throws SQLException
    {
        return getCharacterStream(findParameterIndex(parameterName));  
    }
    
    //@PDA jdbc40
    /**
     * Retrieves the value of the designated parameter as a
     * <code>java.io.Reader</code> object in the Java programming language.
     * It is intended for use when
     * accessing  <code>NCHAR</code>,<code>NVARCHAR</code>
     * and <code>LONGNVARCHAR</code> parameters.
     *
     * @return a <code>java.io.Reader</code> object that contains the parameter
     * value; if the value is SQL <code>NULL</code>, the value returned is
     * <code>null</code> in the Java programming language.
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @exception SQLException if a database access error occurs or 
     * this method is called on a closed <code>CallableStatement</code>
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public Reader getNCharacterStream(int parameterIndex) throws SQLException
    {

        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(! parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            Reader value = (data == null) ? null : data.getNCharacterStream();
            testDataTruncation(parameterIndex, data, false);
            return value;
        }
    }

    //@PDA jdbc40
    /**
     * Retrieves the value of the designated parameter as a
     * <code>java.io.Reader</code> object in the Java programming language.
     * It is intended for use when
     * accessing  <code>NCHAR</code>,<code>NVARCHAR</code>
     * and <code>LONGNVARCHAR</code> parameters.
     * 
     * @param parameterName the name of the parameter
     * @return a <code>java.io.Reader</code> object that contains the parameter
     * value; if the value is SQL <code>NULL</code>, the value returned is
     * <code>null</code> in the Java programming language
     * @exception SQLException if a database access error occurs or 
     * this method is called on a closed <code>CallableStatement</code>
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public Reader getNCharacterStream(String parameterName) throws SQLException
    {
        return getNCharacterStream(findParameterIndex(parameterName));  
    }

    //@PDA jdbc40
  //JDBC40DOC     /**
  //JDBC40DOC      * Retrieves the value of the designated JDBC <code>NCLOB</code> parameter as a
  //JDBC40DOC      * <code>java.sql.NClob</code> object in the Java programming language.
  //JDBC40DOC      * 
  //JDBC40DOC      * @param parameterIndex the first parameter is 1, the second is 2, and
  //JDBC40DOC      * so on
  //JDBC40DOC      * @return the parameter value as a <code>NClob</code> object in the
  //JDBC40DOC      * Java programming language.  If the value was SQL <code>NULL</code>, the
  //JDBC40DOC      * value <code>null</code> is returned.
  //JDBC40DOC      * @exception SQLException if the driver does not support national
  //JDBC40DOC      *         character sets;  if the driver can detect that a data conversion
  //JDBC40DOC      *  error could occur; if a database access error occurs or 
  //JDBC40DOC      * this method is called on a closed <code>CallableStatement</code>
  //JDBC40DOC      * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
  //JDBC40DOC      * this method
  //JDBC40DOC      */
    /* ifdef JDBC40 
    public NClob getNClob(int parameterIndex) throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(! parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // make sure the registered type is valid for this get method
                switch(registeredTypes_[parameterIndex-1]) {
                   case Types.CLOB:
                   case Types.NCLOB :
                   case Types.JAVA_OBJECT:
                     break;
                   default:
                     JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                }

                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            NClob value = (data == null) ? null : data.getNClob();
            testDataTruncation(parameterIndex, data, false);
            return value;
        }
    }
    endif */
    
    //@PDA jdbc40
  //JDBC40DOC     /**
  //JDBC40DOC      * Retrieves the value of a JDBC <code>NCLOB</code> parameter as a
  //JDBC40DOC      * <code>java.sql.NClob</code> object in the Java programming language.
  //JDBC40DOC      * @param parameterName the name of the parameter
  //JDBC40DOC      * @return the parameter value as a <code>NClob</code> object in the
  //JDBC40DOC      *         Java programming language.  If the value was SQL <code>NULL</code>, 
  //JDBC40DOC      *         the value <code>null</code> is returned.
  //JDBC40DOC      * @exception SQLException if the driver does not support national
  //JDBC40DOC      *         character sets;  if the driver can detect that a data conversion
  //JDBC40DOC      *  error could occur; if a database access error occurs or 
  //JDBC40DOC      * this method is called on a closed <code>CallableStatement</code>
  //JDBC40DOC      * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
  //JDBC40DOC      * this method
  //JDBC40DOC      */
    /* ifdef JDBC40 
    public NClob getNClob(String parameterName) throws SQLException
    {
        return getNClob(findParameterIndex(parameterName));  
    }
    endif */ 

    //@PDA jdbc40
    /**
     * Retrieves the value of the designated <code>NCHAR</code>,
     * <code>NVARCHAR</code>
     * or <code>LONGNVARCHAR</code> parameter as
     * a <code>String</code> in the Java programming language.
     *  <p>
     * For the fixed-length type JDBC <code>NCHAR</code>,
     * the <code>String</code> object
     * returned has exactly the same value the SQL
     * <code>NCHAR</code> value had in the
     * database, including any padding added by the database.
     *
     * @param parameterIndex index of the first parameter is 1, the second is 2, ...
     * @return a <code>String</code> object that maps an 
     * <code>NCHAR</code>, <code>NVARCHAR</code> or <code>LONGNVARCHAR</code> value
     * @exception SQLException if a database access error occurs or 
     * this method is called on a closed <code>CallableStatement</code>
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     * @see #setNString
     */
    public String getNString(int parameterIndex) throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(! parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            String value = (data == null) ? null : data.getNString();
            testDataTruncation(parameterIndex, data, false);
            return value;
        }
    }

    //@PDA jdbc40    
    /**
     *  Retrieves the value of the designated <code>NCHAR</code>,
     * <code>NVARCHAR</code>
     * or <code>LONGNVARCHAR</code> parameter as
     * a <code>String</code> in the Java programming language.
     * <p>
     * For the fixed-length type JDBC <code>NCHAR</code>,
     * the <code>String</code> object
     * returned has exactly the same value the SQL
     * <code>NCHAR</code> value had in the
     * database, including any padding added by the database.
     *
     * @param parameterName the name of the parameter
     * @return a <code>String</code> object that maps an 
     * <code>NCHAR</code>, <code>NVARCHAR</code> or <code>LONGNVARCHAR</code> value
     * @exception SQLException if a database access error occurs or 
     * this method is called on a closed <code>CallableStatement</code>
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     * @see #setNString
     */
    public String getNString(String parameterName) throws SQLException
    {
        return getNString(findParameterIndex(parameterName));  
    }

    //@PDA jdbc40
  //JDBC40DOC     /**
  //JDBC40DOC      * Retrieves the value of the designated JDBC <code>ROWID</code> parameter as a  
  //JDBC40DOC      * <code>java.sql.RowId</code> object.  
  //JDBC40DOC      *
  //JDBC40DOC      * @param parameterIndex the first parameter is 1, the second is 2,...
  //JDBC40DOC      * @return a <code>RowId</code> object that represents the JDBC <code>ROWID</code>
  //JDBC40DOC      *     value is used as the designated parameter. If the parameter contains
  //JDBC40DOC      * a SQL <code>NULL</code>, then a <code>null</code> value is returned.
  //JDBC40DOC      * @throws SQLException if a database access error occurs or 
  //JDBC40DOC      * this method is called on a closed <code>CallableStatement</code>
  //JDBC40DOC      * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
  //JDBC40DOC      * this method
  //JDBC40DOC      */
    /* ifdef JDBC40 
    public RowId getRowId(int parameterIndex) throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(!parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            RowId value = (data == null) ? null : data.getRowId();
            testDataTruncation(parameterIndex, data, false);
            return value;
        }
    }
    endif */ 
    
    //@PDA jdbc40
  //JDBC40DOC     /**
  //JDBC40DOC      * Retrieves the value of the designated JDBC <code>ROWID</code> parameter as a  
  //JDBC40DOC      * <code>java.sql.RowId</code> object.  
  //JDBC40DOC      *
  //JDBC40DOC      * @param parameterName the name of the parameter
  //JDBC40DOC      * @return a <code>RowId</code> object that represents the JDBC <code>ROWID</code>
  //JDBC40DOC      *     value is used as the designated parameter. If the parameter contains
  //JDBC40DOC      * a SQL <code>NULL</code>, then a <code>null</code> value is returned.
  //JDBC40DOC      * @throws SQLException if a database access error occurs or 
  //JDBC40DOC      * this method is called on a closed <code>CallableStatement</code>
  //JDBC40DOC      * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
  //JDBC40DOC      * this method
  //JDBC40DOC      */
    /* ifdef JDBC40 
    public RowId getRowId(String parameterName) throws SQLException
    {
        return getRowId(findParameterIndex(parameterName));  
    }
    endif */ 
    
    //@PDA jdbc40
  //JDBC40DOC     /**
  //JDBC40DOC      * Retrieves the value of the designated <code>SQL XML</code> parameter as a
  //JDBC40DOC      * <code>java.sql.SQLXML</code> object in the Java programming language.
  //JDBC40DOC      * @param parameterIndex index of the first parameter is 1, the second is 2, ...
  //JDBC40DOC      * @return a <code>SQLXML</code> object that maps an <code>SQL XML</code> value
  //JDBC40DOC      * @throws SQLException if a database access error occurs or 
  //JDBC40DOC      * this method is called on a closed <code>CallableStatement</code>
  //JDBC40DOC      * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
  //JDBC40DOC      * this method
  //JDBC40DOC      */
    /* ifdef JDBC40 
    public SQLXML getSQLXML(int parameterIndex) throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();

            SQLData data = null;

            // Check if the parameter index refers to the return value parameter.
            // If it is not parameter index 1, then decrement the parameter index,
            // since we are "faking" the return value parameter.
            if(useReturnValueParameter_ && parameterIndex == 1)
            {
                if(!returnValueParameterRegistered_)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                data = returnValueParameter_;
            }
            else
            {
                if(useReturnValueParameter_)
                {
                    --parameterIndex;
                }

                // Validate the parameter index.
                if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                    JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

                // Check that the parameter is an output parameter.
                if(! parameterRow_.isOutput(parameterIndex))
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Verify that the output parameter is registered.
                if(registered_[parameterIndex-1] == false)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // Get the data and check for SQL NULL.
                data = getValue(parameterIndex);
            }

            SQLXML value = (data == null) ? null : data.getSQLXML();
            testDataTruncation(parameterIndex, data, false);
            return value;
        }
    }
    endif */ 
    
    //@PDA jdbc40
  //JDBC40DOC     /**
  //JDBC40DOC      * Retrieves the value of the designated <code>SQL XML</code> parameter as a
  //JDBC40DOC      * <code>java.sql.SQLXML</code> object in the Java programming language.
  //JDBC40DOC      * @param parameterName the name of the parameter
  //JDBC40DOC      * @return a <code>SQLXML</code> object that maps an <code>SQL XML</code> value
  //JDBC40DOC      * @throws SQLException if a database access error occurs or 
  //JDBC40DOC      * this method is called on a closed <code>CallableStatement</code>
  //JDBC40DOC      * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
  //JDBC40DOC      * this method
  //JDBC40DOC      */
    /* ifdef JDBC40 
    public SQLXML getSQLXML(String parameterName) throws SQLException
    {
        return getSQLXML(findParameterIndex(parameterName));  
    } 
    endif */ 

    //@PDA jdbc40
    /**
     * Sets the designated parameter to the given input stream, which will have 
     * the specified number of bytes.
     * When a very large ASCII value is input to a <code>LONGVARCHAR</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code>. Data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from ASCII to the database char format.
     * 
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     *
     * @param parameterName the name of the parameter
     * @param x the Java input stream that contains the ASCII parameter value
     * @param length the number of bytes in the stream 
     * @exception SQLException if a database access error occurs or 
     * this method is called on a closed <code>CallableStatement</code>
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {
            JDTrace.logInformation(this, "setAsciiStream()");
            if(x == null)
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName)  + " value: NULL");
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " length: " + length);
        }

        setAsciiStream(findParameterIndex(parameterName), x, length);
    }

    //@PDA jdbc40
    /**
     * Sets the designated parameter to the given input stream, which will have 
     * the specified number of bytes.
     * When a very large binary value is input to a <code>LONGVARBINARY</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code> object. The data will be read from the stream
     * as needed until end-of-file is reached.
     * 
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     *
     * @param parameterName the name of the parameter
     * @param x the java input stream which contains the binary parameter value
     * @param length the number of bytes in the stream 
     * @exception SQLException if a database access error occurs or 
     * this method is called on a closed <code>CallableStatement</code>
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method.
     */
    public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {  
            JDTrace.logInformation(this, "setBinaryStream()"); 
            if(x == null) 
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " length: " + length);
        }

        setBinaryStream(findParameterIndex(parameterName), x, length);
    }

    //@PDA jdbc40
    /**
     * Sets the designated parameter to the given <code>java.sql.Blob</code> object.
     * The driver converts this to an SQL <code>BLOB</code> value when it
     * sends it to the database.
     *
     * @param parameterName the name of the parameter
     * @param x a <code>Blob</code> object that maps an SQL <code>BLOB</code> value
     * @exception SQLException if a database access error occurs or 
     * this method is called on a closed <code>CallableStatement</code>
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public void setBlob(String parameterName, Blob x) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {  
            JDTrace.logInformation(this, "setBlob()"); 
            if(x == null) 
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " length: " + x.length());
        }

        setBlob(findParameterIndex(parameterName), x);  
    }

    //@PDA jdbc40
    /**
     * Sets the designated parameter to a <code>InputStream</code> object.  The <code>InputStream</code> must contain  the number
     * of characters specified by length, otherwise a <code>SQLException</code> will be
     * generated when the <code>CallableStatement</code> is executed.
     * This method differs from the <code>setBinaryStream (int, InputStream, int)</code>
     * method because it informs the driver that the parameter value should be
     * sent to the system as a <code>BLOB</code>.  When the <code>setBinaryStream</code> method is used,
     * the driver may have to do extra work to determine whether the parameter
     * data should be sent to the system as a <code>LONGVARBINARY</code> or a <code>BLOB</code>
     *
     * @param parameterName the name of the parameter to be set
     * 
     * @param inputStream An object that contains the data to set the parameter
     * value to.
     * @param length the number of bytes in the parameter data.
     * @throws SQLException  if parameterIndex does not correspond
     * to a parameter marker in the SQL statement,  or if the length specified
     * is less than zero; if the number of bytes in the inputStream does not match
     * the specfied length; if a database access error occurs or 
     * this method is called on a closed <code>CallableStatement</code>
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     *
     */
    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {  
            JDTrace.logInformation(this, "setBlob()"); 
            if(inputStream == null) 
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " length: " + length);
        }

        setBlob(findParameterIndex(parameterName), inputStream, length);        
    }
    
    //@PDA jdbc40
    /**
     * Sets the designated parameter to the given <code>Reader</code>
     * object, which is the given number of characters long.
     * When a very large UNICODE value is input to a <code>LONGVARCHAR</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.Reader</code> object. The data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from UNICODE to the database char format.
     * 
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     *
     * @param parameterName the name of the parameter
     * @param reader the <code>java.io.Reader</code> object that
     *        contains the UNICODE data used as the designated parameter
     * @param length the number of characters in the stream 
     * @exception SQLException if a database access error occurs or 
     * this method is called on a closed <code>CallableStatement</code>
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {  
            JDTrace.logInformation(this, "setCharacterStream()"); 
            if(reader == null) 
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " length: " + length);
        }

        setCharacterStream(findParameterIndex(parameterName), reader, length);
    }

    //@PDA jdbc40
    /**
     * Sets the designated parameter to the given <code>java.sql.Clob</code> object.
     * The driver converts this to an SQL <code>CLOB</code> value when it
     * sends it to the database.
     *
     * @param parameterName the name of the parameter
     * @param x a <code>Clob</code> object that maps an SQL <code>CLOB</code> value
     * @exception SQLException if a database access error occurs or 
     * this method is called on a closed <code>CallableStatement</code>
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public void setClob(String parameterName, Clob x) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {  
            JDTrace.logInformation(this, "setClob()"); 
            if(x == null) 
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " length: " + x.length());
        }

        setClob(findParameterIndex(parameterName), x);
        
    }

    //@PDA jdbc40
    /**
     * Sets the designated parameter to a <code>Reader</code> object.  The <code>reader</code> must contain  the number
     * of characters specified by length otherwise a <code>SQLException</code> will be
     * generated when the <code>CallableStatement</code> is executed.
     * This method differs from the <code>setCharacterStream (int, Reader, int)</code> method
     * because it informs the driver that the parameter value should be sent to
     * the system as a <code>CLOB</code>.  When the <code>setCharacterStream</code> method is used, the
     * driver may have to do extra work to determine whether the parameter
     * data should be sent to the system as a <code>LONGVARCHAR</code> or a <code>CLOB</code>
     * @param parameterName the name of the parameter to be set
     * @param reader An object that contains the data to set the parameter value to.
     * @param length the number of characters in the parameter data.
     * @throws SQLException if parameterIndex does not correspond to a parameter
     * marker in the SQL statement; if the length specified is less than zero;
     * a database access error occurs or 
     * this method is called on a closed <code>CallableStatement</code>
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     *
     */
    public void setClob(String parameterName, Reader reader, long length) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {  
            JDTrace.logInformation(this, "setClob()"); 
            if(reader == null) 
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " length: " + length);
        }

        setClob(findParameterIndex(parameterName), reader, length);
    }

    //@PDA jdbc40
    /**
     * Sets the designated parameter to a <code>Reader</code> object. The
     * <code>Reader</code> reads the data till end-of-file is reached. The
     * driver does the necessary conversion from Java character format to
     * the national character set in the database.
     * @param parameterName the name of the parameter to be set
     * @param value the parameter value
     * @param length the number of characters in the parameter data.
     * @throws SQLException if the driver does not support national
     *         character sets;  if the driver can detect that a data conversion
     *  error could occur; if a database access error occurs or 
     * this method is called on a closed <code>CallableStatement</code>
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {  
            JDTrace.logInformation(this, "setNCharacterStream()"); 
            if(value == null) 
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " length: " + length);
        }

        setNCharacterStream(findParameterIndex(parameterName), value, length);
    }

    //@PDA jdbc40
  //JDBC40DOC     /**
  //JDBC40DOC      * Sets the designated parameter to a <code>java.sql.NClob</code> object. The object
  //JDBC40DOC      * implements the <code>java.sql.NClob</code> interface. This <code>NClob</code>
  //JDBC40DOC      * object maps to a SQL <code>NCLOB</code>.
  //JDBC40DOC      * @param parameterName the name of the parameter to be set
  //JDBC40DOC      * @param value the parameter value
  //JDBC40DOC      * @throws SQLException if the driver does not support national
  //JDBC40DOC      *         character sets;  if the driver can detect that a data conversion
  //JDBC40DOC      *  error could occur; if a database access error occurs or 
  //JDBC40DOC      * this method is called on a closed <code>CallableStatement</code>
  //JDBC40DOC      * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
  //JDBC40DOC      * this method
  //JDBC40DOC      */
    /* ifdef JDBC40 
    public void setNClob(String parameterName, NClob value) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {  
            JDTrace.logInformation(this, "setNClob()"); 
            if(value == null) 
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " length: " + value.length());
        }

        setNClob(findParameterIndex(parameterName), value);
    }
    endif */ 
    
    //@PDA jdbc40
    /**
     * Sets the designated parameter to a <code>Reader</code> object.  The <code>reader</code> must contain  the number
     * of characters specified by length otherwise a <code>SQLException</code> will be
     * generated when the <code>CallableStatement</code> is executed.
     * This method differs from the <code>setCharacterStream (int, Reader, int)</code> method
     * because it informs the driver that the parameter value should be sent to
     * the system as a <code>NCLOB</code>.  When the <code>setCharacterStream</code> method is used, the
     * driver may have to do extra work to determine whether the parameter
     * data should be sent to the system as a <code>LONGNVARCHAR</code> or a <code>NCLOB</code>
     * 
     * @param parameterName the name of the parameter to be set
     * @param reader An object that contains the data to set the parameter value to.
     * @param length the number of characters in the parameter data.
     * @throws SQLException if parameterIndex does not correspond to a parameter
     * marker in the SQL statement; if the length specified is less than zero;
     * if the driver does not support national
     *         character sets;  if the driver can detect that a data conversion
     *  error could occur; if a database access error occurs or 
     * this method is called on a closed <code>CallableStatement</code>
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */     
    public void setNClob(String parameterName, Reader reader, long length) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {  
            JDTrace.logInformation(this, "setNClob()"); 
            if(reader == null) 
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " length: " + length);
        }

        setNClob(findParameterIndex(parameterName), reader, length);
    }

    //@PDA jdbc40
    /**
     * Sets the designated parameter to the given <code>String</code> object.
     * The driver converts this to a SQL <code>NCHAR</code> or
     * <code>NVARCHAR</code> or <code>LONGNVARCHAR</code>
     * @param parameterName the name of the parameter to be set
     * @param value the parameter value
     * @throws SQLException if the driver does not support national
     *         character sets;  if the driver can detect that a data conversion
     *  error could occur; if a database access error occurs or 
     * this method is called on a closed <code>CallableStatement</code>
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public void setNString(String parameterName, String value) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {
            JDTrace.logInformation(this, "setNString()");
            if(value == null)
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName)  + " value: NULL");
            else if(value.length() > maxToLog_)
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " length: " + value.length());
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: " + value);
        }

        setNString(findParameterIndex(parameterName), value);
    }

    //@PDA jdbc40
  //JDBC40DOC     /**
  //JDBC40DOC      * Sets the designated parameter to the given <code>java.sql.RowId</code> object. The
  //JDBC40DOC      * driver converts this to a SQL <code>ROWID</code> when it sends it to the
  //JDBC40DOC      * database.
  //JDBC40DOC      *
  //JDBC40DOC      * @param parameterName the name of the parameter
  //JDBC40DOC      * @param x the parameter value
  //JDBC40DOC      * @throws SQLException if a database access error occurs or 
  //JDBC40DOC      * this method is called on a closed <code>CallableStatement</code>
  //JDBC40DOC      * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
  //JDBC40DOC      * this method
  //JDBC40DOC      */
    /* ifdef JDBC40 
    public void setRowId(String parameterName, RowId x) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {  
            JDTrace.logInformation(this, "setRowId()"); 
            if(x == null) 
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");  
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: " + x.toString());
        }   

        setRowId(findParameterIndex(parameterName), x);
    }
   endif */ 
    
    //@PDA jdbc40
  //JDBC40DOC     /**
  //JDBC40DOC      * Sets the designated parameter to the given <code>java.sql.SQLXML</code> object. The driver converts this to an
  //JDBC40DOC      * <code>SQL XML</code> value when it sends it to the database.
  //JDBC40DOC      *
  //JDBC40DOC      * @param parameterName the name of the parameter
  //JDBC40DOC      * @param xmlObject a <code>SQLXML</code> object that maps an <code>SQL XML</code> value
  //JDBC40DOC      * @throws SQLException if a database access error occurs, 
  //JDBC40DOC      * this method is called on a closed <code>CallableStatement</code> or 
  //JDBC40DOC      * the <code>java.xml.transform.Result</code>,
  //JDBC40DOC      *  <code>Writer</code> or <code>OutputStream</code> has not been closed for the <code>SQLXML</code> object 
  //JDBC40DOC      * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
  //JDBC40DOC      * this method
  //JDBC40DOC      */
    /* ifdef JDBC40 
    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {  
            JDTrace.logInformation(this, "setClob()"); 
            if(xmlObject == null) 
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");
            else JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " length: " + xmlObject.toString().length());
        }

        setSQLXML(findParameterIndex(parameterName), xmlObject);
    }
    endif */ 

    //@PDA jdbc40 
    /**
     * Sets the designated parameter to the given input stream.
     * When a very large ASCII value is input to a <code>LONGVARCHAR</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code>. Data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from ASCII to the database char format.
     * 
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     * <P><B>Note:</B> Consult your JDBC driver documentation to determine if 
     * it might be more efficient to use a version of 
     * <code>setAsciiStream</code> which takes a length parameter. 
     *
     * @param parameterName the name of the parameter
     * @param x the Java input stream that contains the ASCII parameter value
     * @exception SQLException if parameterName does not correspond to a named 
     * parameter; if a database access error occurs or 
     * this method is called on a closed <code>CallableStatement</code>
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
    */
    public void setAsciiStream(String parameterName, InputStream x) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {  
            JDTrace.logInformation(this, "setAsciiStream(String, InputStream)"); 
            if(x == null) 
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");
        }

        setAsciiStream(findParameterIndex(parameterName), x);
    }

    //@PDA jdbc40 
    /**
     * Sets the designated parameter to the given input stream.
     * When a very large binary value is input to a <code>LONGVARBINARY</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code> object. The data will be read from the 
     * stream as needed until end-of-file is reached.
     * 
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     * <P><B>Note:</B> Consult your JDBC driver documentation to determine if 
     * it might be more efficient to use a version of 
     * <code>setBinaryStream</code> which takes a length parameter. 
     *
     * @param parameterName the name of the parameter
     * @param x the java input stream which contains the binary parameter value
     * @exception SQLException if parameterName does not correspond to a named 
     * parameter; if a database access error occurs or 
     * this method is called on a closed <code>CallableStatement</code>
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     */
    public void setBinaryStream(String parameterName, InputStream x) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {  
            JDTrace.logInformation(this, "setBinaryStream(String, InputStream)"); 
            if(x == null) 
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");
        }

        setBinaryStream(findParameterIndex(parameterName), x);
    }

    //@PDA jdbc40 
    /**
     * Sets the designated parameter to a <code>InputStream</code> object. 
     * This method differs from the <code>setBinaryStream (int, InputStream)</code>
     * method because it informs the driver that the parameter value should be
     * sent to the system as a <code>BLOB</code>.  When the <code>setBinaryStream</code> method is used,
     * the driver may have to do extra work to determine whether the parameter
     * data should be sent to the system as a <code>LONGVARBINARY</code> or a <code>BLOB</code>
     *
     * <P><B>Note:</B> Consult your JDBC driver documentation to determine if 
     * it might be more efficient to use a version of 
     * <code>setBlob</code> which takes a length parameter.
     *
     * @param parameterName the name of the parameter
     * @param inputStream An object that contains the data to set the parameter
     * value to.
     * @throws SQLException if parameterName does not correspond to a named 
     * parameter; if a database access error occurs or 
     * this method is called on a closed <code>CallableStatement</code>
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     */
    public void setBlob(String parameterName, InputStream inputStream) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {  
            JDTrace.logInformation(this, "setBlob(String, InputStream)"); 
            if(inputStream == null) 
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");
        }

        setBlob(findParameterIndex(parameterName), inputStream);
    }

    //@PDA jdbc40 
    /**
     * Sets the designated parameter to the given <code>Reader</code>
     * object.
     * When a very large UNICODE value is input to a <code>LONGVARCHAR</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.Reader</code> object. The data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from UNICODE to the database char format.
     * 
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     * <P><B>Note:</B> Consult your JDBC driver documentation to determine if 
     * it might be more efficient to use a version of 
     * <code>setCharacterStream</code> which takes a length parameter. 
     *
     * @param parameterName the name of the parameter
     * @param reader the <code>java.io.Reader</code> object that contains the 
     *        Unicode data
     * @exception SQLException if parameterName does not correspond to a named 
     * parameter; if a database access error occurs or 
     * this method is called on a closed <code>CallableStatement</code>
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     */
    public void setCharacterStream(String parameterName, Reader reader) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {  
            JDTrace.logInformation(this, "setCharacterStream(String, Reader)"); 
            if(reader == null) 
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");
        }

        setCharacterStream(findParameterIndex(parameterName), reader);
    }

    //@PDA jdbc40 
    /**
     * Sets the designated parameter to a <code>Reader</code> object. 
     * This method differs from the <code>setCharacterStream (int, Reader)</code> method
     * because it informs the driver that the parameter value should be sent to
     * the system as a <code>CLOB</code>.  When the <code>setCharacterStream</code> method is used, the
     * driver may have to do extra work to determine whether the parameter
     * data should be sent to the system as a <code>LONGVARCHAR</code> or a <code>CLOB</code>
     * 
     * <P><B>Note:</B> Consult your JDBC driver documentation to determine if 
     * it might be more efficient to use a version of 
     * <code>setClob</code> which takes a length parameter.
     *
     * @param parameterName the name of the parameter
     * @param reader An object that contains the data to set the parameter value to.
     * @throws SQLException if parameterName does not correspond to a named 
     * parameter; if a database access error occurs or this method is called on
     * a closed <code>CallableStatement</code>
     *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     */
    public void setClob(String parameterName, Reader reader) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {  
            JDTrace.logInformation(this, "setClob(String, Reader)"); 
            if(reader == null) 
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");
        }

        setClob(findParameterIndex(parameterName), reader);
    }

    //@PDA jdbc40 
    /**
     * Sets the designated parameter to a <code>Reader</code> object. The
     * <code>Reader</code> reads the data till end-of-file is reached. The
     * driver does the necessary conversion from Java character format to
     * the national character set in the database.
     
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     * <P><B>Note:</B> Consult your JDBC driver documentation to determine if 
     * it might be more efficient to use a version of 
     * <code>setNCharacterStream</code> which takes a length parameter.
     *      
     * @param parameterName the name of the parameter
     * @param value the parameter value
     * @throws SQLException if parameterName does not correspond to a named 
     * parameter; if the driver does not support national
     *         character sets;  if the driver can detect that a data conversion
     *  error could occur; if a database access error occurs; or 
     * this method is called on a closed <code>CallableStatement</code>
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     */
    public void setNCharacterStream(String parameterName, Reader value) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {  
            JDTrace.logInformation(this, "setNCharacterStream(String, Reader)"); 
            if(value == null) 
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");
        }

        setNCharacterStream(findParameterIndex(parameterName), value);
    }

    //@PDA jdbc40 
    /**
     * Sets the designated parameter to a <code>Reader</code> object.  
     * This method differs from the <code>setCharacterStream (int, Reader)</code> method
     * because it informs the driver that the parameter value should be sent to
     * the system as a <code>NCLOB</code>.  When the <code>setCharacterStream</code> method is used, the
     * driver may have to do extra work to determine whether the parameter
     * data should be sent to the system as a <code>LONGNVARCHAR</code> or a <code>NCLOB</code>
     * <P><B>Note:</B> Consult your JDBC driver documentation to determine if 
     * it might be more efficient to use a version of 
     * <code>setNClob</code> which takes a length parameter.
     *
     * @param parameterName the name of the parameter
     * @param reader An object that contains the data to set the parameter value to.
     * @throws SQLException if parameterName does not correspond to a named 
     * parameter; if the driver does not support national character sets;
     * if the driver can detect that a data conversion
     *  error could occur;  if a database access error occurs or 
     * this method is called on a closed <code>CallableStatement</code>
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     *
     */
    public void setNClob(String parameterName, Reader reader) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {  
            JDTrace.logInformation(this, "setNClob(String, Reader)"); 
            if(reader == null) 
                JDTrace.logInformation(this, "parameter index: " + findParameterIndex(parameterName) + " value: NULL");
        }

        setNClob(findParameterIndex(parameterName), reader);
    }
    
    
    public Object getObject(int parameterIndex, Class type)
        throws SQLException {
      // Throw exception if type is null 
      if (type == null) {
        JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);
      }
      if (byteArrayClass_ == null) {
        byte[] byteArray = new byte[1]; 
        byteArrayClass_ = byteArray.getClass(); 
      }
      // Use the appropriate method to get the correct data type.
      // After checking for string, we check for classes in the 
      // order specified in Table B-6 of the JDBC 4.0 specification
      // 
      if (type == java.lang.String.class ) {
        return getString(parameterIndex); 
      } else if (type == java.lang.Byte.class){
        byte b = getByte(parameterIndex); 
        if (b == 0 && wasNull()) { 
          return null;  
        } else { 
          return new Byte(b);
        }
      } else if (type == java.lang.Short.class){
        short s = getShort(parameterIndex); 
        if (s == 0 && wasNull()) { 
          return null;  
        } else { 
          return new Short(s);
        }
      } else if (type == java.lang.Integer.class){
        int i = getInt(parameterIndex); 
        if (i == 0 && wasNull()) { 
          return null;  
        } else { 
          return new Integer(i);
        }
      } else if (type == java.lang.Long.class){
        long l = getLong(parameterIndex); 
        if (l == 0 && wasNull()) { 
          return null;  
        } else { 
          return new Long(l);
        }
      } else if (type == java.lang.Float.class){
        float f = getFloat(parameterIndex);
        if (f == 0 && wasNull()) { 
          return null;  
        } else { 
        return new Float(f);
        }
      } else if (type == java.lang.Double.class){
        double d = getDouble(parameterIndex); 
        if (d == 0 && wasNull()) { 
          return null;  
        } else { 
          return new Double(d);
        }
      } else if (type == java.math.BigDecimal.class){
        return getBigDecimal(parameterIndex); 
      } else if (type == java.lang.Boolean.class) {
        boolean b = getBoolean(parameterIndex);
        if (b == false && wasNull()) { 
          return null;  
        } else { 
          return new Boolean (b);
        }
        
      } else if (type == java.sql.Date.class){
        return getDate(parameterIndex); 
      } else if (type == java.sql.Time.class){
        return getTime(parameterIndex); 
      } else if (type == java.sql.Timestamp.class){
        return getTimestamp(parameterIndex); 
      } else if (type == byteArrayClass_){
        return getBytes(parameterIndex);
      } else if (type == InputStream.class){
        Blob b = getBlob(parameterIndex); 
        if (b == null) {
          return b; 
        } else { 
          return b.getBinaryStream();
        }
      } else if (type == Reader.class){
        return getCharacterStream(parameterIndex); 
      } else if (type == Clob.class){
        return getClob(parameterIndex);
      } else if (type == Blob.class){
        return getBlob(parameterIndex);
      } else if (type == Array.class){
        return getArray(parameterIndex);
      } else if (type == Ref.class){
        return getRef(parameterIndex);
      } else if (type == URL.class){
        return getURL(parameterIndex);
/* ifdef JDBC40 
      } else if (type == NClob.class){
        return getNClob(parameterIndex);
      } else if (type == RowId.class){
        return getRowId(parameterIndex);
      } else if (type == SQLXML.class){
        return getSQLXML(parameterIndex);
endif */
      } else if (type == Object.class){
        return getObject(parameterIndex);
      }

      JDError.throwSQLException (JDError.EXC_DATA_TYPE_INVALID);
      return null; 
    }

    public Object getObject(String parameterName, Class type)
        throws SQLException {
      return getObject(findParameterIndex(parameterName), type); 
    }
    
    /*
     * Validate that the registered type is valid for getNumeric type conversions.
     * Previously, getInt, ...., could only get used against a type registered as INT.   
     * @param registeredType
     */
    private void validateNumericRegisteredType(int registeredType) throws SQLException { 
      // make sure the registered type is valid for this get method
      // Assuming that the compatibility should be the same as the get methods, 
      // the following registered types are allowed.
      // Updated 9/29/2011 as part of JDBC 4.1 updates to allow getObject(x,java.lang.Integer) to work. 
      switch(registeredType) {
        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
        case Types.BIGINT:
        case Types.REAL:
        case Types.FLOAT:
        case Types.DOUBLE:
        case Types.DECIMAL:
        case Types.NUMERIC:
        case Types.BIT:
        case Types.BOOLEAN:
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
        case Types.JAVA_OBJECT:
          /* types are good */ 
          break; 
        default:
          JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
      }
      }



    
}
