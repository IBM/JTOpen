///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400JDBCPreparedStatement.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.BatchUpdateException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.DataTruncation;
import java.sql.Date;
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



/**
<p>The AS400JDBCPreparedStatement class precompiles and stores an
SQL statement.  This provides the ability to efficiently run
the statement multiple times.  In addition, the statement may
contain parameters.  Use Connection.prepareStatement() to create
new PreparedStatement objects.

<p>When setting input parameter values, the caller must specify types
that are compatible with the defined SQL type of the input parameter.
For example, if the input parameter has SQL type INTEGER, then the
caller must call setInt() to set the IN parameter value.  If
arbitrary type conversions are required, then use setObject() with
a target SQL type.
**/
//
// Implementation notes:
//
// 1. See implementation note in AS400JDBCStatement.java about
//    "private protected" methods.
//
// @F2A
// 2. We need to support ?=CALL statements.  This is where the stored
//    procedure returns an INTEGER value, and we treat it as the
//    first parameter marker.  The database and host server support
//    the return value, but not as a parameter marker.  Consequently,
//    we have to fake it as the first parameter marker.  If this appears,
//    in the SQL statement, we strip it off and maintain this separately.
//    Of course in that case we are always mapping the caller's parameter
//    indices to the server's indices by decrementing by 1 as needed.
//
public class AS400JDBCPreparedStatement
extends AS400JDBCStatement
implements PreparedStatement
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    private boolean             dataTruncation_;        // @B5A
    private int                 descriptorHandle_;
            boolean             executed_;              // private protected
    private boolean             outputParametersExpected_;
            int                 parameterCount_;        // private protected
    private int[]               parameterLengths_;
    private int[]               parameterOffsets_;
    private boolean[]           parameterNulls_;
            JDServerRow         parameterRow_;          // private protected
    private int                 parameterTotalSize_;
            boolean[]           parameterSet_;          // private protected
    private boolean             prepared_;
    private JDServerRow         resultRow_;
            SQLInteger          returnValueParameter_;  // private protected            @F2A
    private JDSQLStatement      sqlStatement_;
            boolean             useReturnValueParameter_; // private protected          @F2A



/**
Constructs an AS400JDBCPreparedStatement object.

@param   connection                 The connection to the server.
@param   id                         The id.
@param   transactionManager         The transaction manager for the connection.
@param   packageManager             The package manager for the connection.
@param   blockCriteria              The block criteria.
@param   blockSize                  The block size (in KB).
@param   prefetch                   Indicates if prefetching data.
@param   sqlStatement               The SQL statement.
@param   outputParametersExpected   Indicates if output parameters are expected.
@param   packageCriteria            The package criteria.
@param   resultSetType              The result set type.
@param   resultSetConcurrency       The result set concurrency.

@exception  SQLException    If the SQL statement contains a syntax
                            error or an error occurs.
**/
	AS400JDBCPreparedStatement (AS400JDBCConnection connection,
          				        int id,
		        			    JDTransactionManager transactionManager,
				        	    JDPackageManager packageManager,
        					    String blockCriteria,
		        			    int blockSize,
				        	    boolean prefetch,
        					    JDSQLStatement sqlStatement,
		        			    boolean outputParametersExpected,
		        			    String packageCriteria,
                                int resultSetType,
                                int resultSetConcurrency)
		throws SQLException
    {
		super (connection, id, transactionManager,
		    packageManager, blockCriteria, blockSize,
		    prefetch, packageCriteria, resultSetType,
		    resultSetConcurrency);

        outputParametersExpected_   = outputParametersExpected;
        parameterCount_             = sqlStatement.countParameters();
        parameterLengths_           = new int[parameterCount_];
        parameterNulls_             = new boolean[parameterCount_];
        parameterOffsets_           = new int[parameterCount_];
        parameterSet_               = new boolean[parameterCount_];
		sqlStatement_               = sqlStatement;
        useReturnValueParameter_    = sqlStatement.hasReturnValueParameter();       // @F2A

        if (useReturnValueParameter_)                                               // @F2A
            returnValueParameter_   = new SQLInteger();                             // @F2A

        if (JDTrace.isTraceOn()) {                                                  // @D1A @F2C
            JDTrace.logInformation (this, "Preparing [" + sqlStatement_ + "]");     // @D1A
            if (useReturnValueParameter_)                                           // @F2A
                JDTrace.logInformation(this, "Suppressing return value parameter (?=CALL)"); // @F2A
        }                                                                           // @F2A

        // Do not allow statements to be immediately
        // executed.  If we did not do this, then some
        // statements would get executed at prepare time.
        allowImmediate_ = false;

        // Prepare.
        prepared_ = true;
        resultRow_ = commonPrepare (sqlStatement_);
        executed_ = false;

        dataTruncation_ = connection.getProperties ().getBoolean (JDProperties.DATA_TRUNCATION); // @B5A

        clearParameters ();
    }



// JDBC 2.0
/**
Adds the set of parameters to the current batch.

@exception SQLException     If the statement is not open or
                            an input parameter has not been set.
**/
    public void addBatch ()
        throws SQLException
    {
        synchronized(internalLock_) {                                            // @F1A
            checkOpen ();
            Object[] parameters = new Object[parameterCount_];
            int[] scales = new int[parameterCount_];
            for (int i = 0; i < parameterCount_; ++i) {
    
                // Statements with output or input parameters are
                // not allowed in the batch.
                if (parameterRow_.isOutput (i+1))
                    JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);
    
                // If an input parameter is not set, we throw an exception.
                if (parameterSet_[i] == false)
                    JDError.throwSQLException (JDError.EXC_PARAMETER_COUNT_MISMATCH);
    
                // Save the parameter in the array.  If its null,
                // just save a null reference.
                if (parameterNulls_[i])
                    parameters[i] = null;
                else
                    parameters[i] = parameterRow_.getSQLData (i+1).toObject ();
            }
    
            batch_.addElement (parameters);
        }
    }



// JDBC 2.0
/**
Adds an SQL statement to the current batch of SQL statements.

<p>Do not use this form of addBatch() on a prepared statement.

@param sql  The SQL statement to be added to the current batch.
            This can be any SQL statement that does not return
            a result set.

@exception SQLException     This exception is always thrown.
**/
    public void addBatch (String sql)
        throws SQLException
    {        
        JDError.throwSQLException (JDError.EXC_FUNCTION_SEQUENCE); // @B1C
    }



// @BAA
/**
Creates or changes the descriptor, which describes the parameter
marker format.

@exception SQLException If an error occurs.
**/
    private void changeDescriptor()
        throws SQLException
    {
        // If a parameter that is not an input parameter has
        // not been set, we still include it in the format.
        //
        // Note that we set the native type, length, scale and
        // precision of each parameter in the descriptor.
        // That means that if the user sets a new parameter
        // value that changes one of these, then the
        // descriptor needs to be changed.
        //
        try {
            descriptorHandle_ = id_;
            DBSQLDescriptorDS request2 = new DBSQLDescriptorDS (DBSQLDescriptorDS.FUNCTIONID_CHANGE_DESCRIPTOR,
                id_, 0, descriptorHandle_);

            DBDataFormat parameterMarkerDataFormat;
            if (connection_.useExtendedFormats ())
                parameterMarkerDataFormat = new DBExtendedDataFormat (parameterCount_);
            else
                parameterMarkerDataFormat = new DBOriginalDataFormat (parameterCount_);
            request2.setParameterMarkerDataFormat (parameterMarkerDataFormat);

            parameterMarkerDataFormat.setConsistencyToken (1);
            parameterMarkerDataFormat.setRecordSize (parameterTotalSize_);
            for (int i = 0; i < parameterCount_; ++i) {
                SQLData sqlData = parameterRow_.getSQLData (i+1);

                parameterMarkerDataFormat.setFieldDescriptionLength (i);
                parameterMarkerDataFormat.setFieldLength (i, parameterLengths_[i]);
                parameterMarkerDataFormat.setFieldCCSID (i, parameterRow_.getCCSID (i+1));

                parameterMarkerDataFormat.setFieldNameLength (i, 0);
                parameterMarkerDataFormat.setFieldNameCCSID (i, 0);
                parameterMarkerDataFormat.setFieldName (i, "", connection_.getConverter ());

                parameterMarkerDataFormat.setFieldSQLType (i,
                    (short) (sqlData.getNativeType() | 0x0001));
                parameterMarkerDataFormat.setFieldScale (i,
                    (short) sqlData.getScale());
                parameterMarkerDataFormat.setFieldPrecision (i,
                    (short) sqlData.getPrecision());
            }

            connection_.send (request2, descriptorHandle_);

            if (JDTrace.isTraceOn())
                JDTrace.logInformation (this, "Descriptor " + descriptorHandle_ + " created or changed");
        }
        catch (DBDataStreamException e) {
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);
        }
    }



/**
Releases the resources used by the current input parameter
values. In general, input parameter values remain in effect
for repeated executions of the prepared statement.  Setting an
input parameter value to a new value automatically clears its
previous value.

@exception  SQLException    If the statement is not open.
**/
    public void clearParameters ()
      throws SQLException
    {
        synchronized(internalLock_) {                                            // @F1A
            checkOpen ();
    
    	    for (int i = 0; i < parameterCount_; ++i) {
    	        // @E1D parameterLengths_[i]    = 0;
    	        parameterNulls_[i]      = false;
    	        // @E1D parameterOffsets_[i]    = 0;
    	        parameterSet_[i]        = false;
    	    }
    
    	    // @E1D parameterTotalSize_ = 0;

            if (useReturnValueParameter_)                                       // @F2A
                returnValueParameter_.set(0);                                   // @F2A
        }
    }



/**
Releases the prepared statement's resources immediately instead of
waiting for them to be automatically released.  This closes the
current result set.

@exception SQLException If an error occurs.
**/
    public void close ()
      throws SQLException
    {
        synchronized(internalLock_) {                                            // @F1A
            // If this is already closed, then just do nothing.             
            // 
            // The spec does not define what happens when a connection
            // is closed multiple times.  The official word from the Sun 
            // JDBC team is that "the driver's behavior in this case 
            // is implementation defined.   Applications that do this are 
            // non-portable." 
            if (isClosed ())
                return;
    
            // If a descriptor was created somewhere along
            // the lines, then delete it now.
            if (descriptorHandle_ != 0) {
        	  	DBSQLDescriptorDS request = new DBSQLDescriptorDS (
    	            DBSQLDescriptorDS.FUNCTIONID_DELETE_DESCRIPTOR,
    	            id_, 0, descriptorHandle_);
       	    	connection_.send (request, descriptorHandle_);
       	    	descriptorHandle_ = 0;
            }
    
            super.close ();
        }
    }



/**
Performs common operations needed after an execute.

@param  sqlStatement    The SQL statement.
@param  reply           The execute reply.

@exception      SQLException    If an error occurs.
**/
    void commonExecuteAfter (JDSQLStatement sqlStatement,
                             DBReplyRequestedDS reply) // private protected
        throws SQLException
    {
        super.commonExecuteAfter (sqlStatement, reply);

        if (prepared_) {

            DBData resultData = null;
            if (outputParametersExpected_)
                resultData = reply.getResultData ();

            // Store the output parameters, if needed.  
            if ((outputParametersExpected_) && (resultData != null)) {
                parameterRow_.setServerData (resultData);
                parameterRow_.setRowIndex (0);                
            }

            // Handle the return value parameter, if needed.                           @F2A
            try {                                                                   // @F2A
                if (useReturnValueParameter_)                                       // @F2A
                    returnValueParameter_.set(reply.getSQLCA().getErrd1());         // @F2A
            }                                                                       // @F2A
	    	catch (DBDataStreamException e) {                                       // @F2A
		    	JDError.throwSQLException (JDError.EXC_INTERNAL, e);                // @F2A
    		}                                                                       // @F2A
        }
    }



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
        super.commonExecuteBefore (sqlStatement, request);

        if (prepared_) {

            // Close the result set before executing again.
	    	closeResultSet (JDCursor.REUSE_YES);

            // Validate each parameters.   If a parameter is not an
            // input parameter, then it is okay for it not to have been
            // set.  However, if an input parameter was not set,
            // we throw an exception.
            for (int i = 0; i < parameterCount_; ++i)
                if ((parameterSet_[i] == false) && (parameterRow_.isInput (i+1)))
                    JDError.throwSQLException (JDError.EXC_PARAMETER_COUNT_MISMATCH);

    		// Create the descriptor if needed.  This should only
    		// be done once (on the first execute for the prepared
    		// statement).
    		if ((parameterCount_ > 0) && (descriptorHandle_ == 0)) {

                // Get the offset and length for each parameter.
                // We just use the information that came in the parameter
                // marker format from reply for the prepare.
                parameterTotalSize_ = 0;
                for (int i = 0; i < parameterCount_; ++i) {
                    parameterLengths_[i] = parameterRow_.getLength (i+1);
                    parameterOffsets_[i] = parameterTotalSize_;
                    parameterTotalSize_ += parameterLengths_[i];
                }

                changeDescriptor();                                                             // @BAC
        	}

            // Add the parameter information to the execute request.
            try {                
                
                // Set the descriptor handle.
                request.setParameterMarkerDescriptorHandle (descriptorHandle_);

  		        // If there are any parameters, then send the parameter
       		    // values with the execute data stream.  Only the
                // input parameters are included here.
   		        if (parameterCount_ > 0) {

                    // In building the parameter marker data, we may discover that the              // @BAA
                    // descriptor needs to be changed.  If so, we will need to change the           // @BAA
                    // descriptor, then rebuild the parameter marker data based on that             // @BAA
                    // change.  This is implemented using a do-while, but it should never           // @BAA
                    // have to loop more than once, since the second time through (in a             // @BAA
                    // particular execute) every thing should be great.                             // @BAA
                    boolean rebuildNeeded = false;                                                  // @BAA
                    int loopCount = 0;                                                              // @BAA
                    do {                                                                            // @BAA
                        ++loopCount;                                                                // @BAA
    
                        DBData parameterMarkerData;
                        if (connection_.useExtendedFormats ())
                            parameterMarkerData = new DBExtendedData (1,
                                parameterCount_, 2, parameterTotalSize_);
                        else
                            parameterMarkerData = new DBOriginalData (1,
                                parameterCount_, 2, parameterTotalSize_);
                        request.setParameterMarkerData (parameterMarkerData);

                        boolean descriptorChangeNeeded = false;                                 // @BAA
                        rebuildNeeded = false;                                                  // @BAA

                        parameterMarkerData.setConsistencyToken (1);
	                    int rowDataOffset = parameterMarkerData.getRowDataOffset (0);
        		        for (int i = 0; i < parameterCount_; ++i) 
        		        {
                           // @G1 -- zero out the comm buffer if the parameter marker is null.
                           //        If the buffer is not zero'ed out old data will be sent to
                           //        the server possibily messing up a future request.  
                         if (parameterNulls_[i] == true)                                       // @B9C
                         {                                                                     // @G1a
                            parameterMarkerData.setIndicator (0, i, (short) -1);               // @G1a       
                            byte[] parameterData = parameterMarkerData.getRawBytes();          // @G1a
                            int parameterDataOffset = rowDataOffset + parameterOffsets_[i];    // @G1a
                            int parameterDataLength = parameterLengths_[i]                     // @G1a
                                                           + parameterDataOffset;              // @G1a
                            for (int z=parameterDataOffset;                                    // @G1a
                                     z < parameterDataLength;                                  // @G1a
                                     parameterData[z++] = 0x00);                               // @G1a
                         }                                                                      // @G1a

    		                else {
    		                    parameterMarkerData.setIndicator (0, i, (short) 0);
                                ConverterImplRemote ccsidConverter = connection_.getConverter (
                                    parameterRow_.getCCSID (i+1));

                                // Convert the data to bytes into the parameter marker data.    // @BAA
                                // If there is an exception here, it means that there were      // @BAA
                                // not enough bytes in the descriptor for the conversion.       // @BAA
                                // If so, we get the correct length via getPrecision()          // @BAA
                                // (assume the SQLData implementation has updated its own       // @BAA
                                // precision as needed).                                        // @BAA
                                int correctLength = -1;                                         // @BAA
                                SQLData sqlData = parameterRow_.getSQLData(i+1);                // @BAC
                                try {                                                           // @BAA
                                    sqlData.convertToRawBytes (parameterMarkerData.getRawBytes (),  // @BAC
                                        rowDataOffset + parameterOffsets_[i], ccsidConverter);                          
                                }                                                               // @BAA
                                catch(SQLException e) {                                         // @BAA
                                    correctLength = sqlData.getPrecision();                     // @BAA
                                }                                                               // @BAA

                                // If the length needed is larger than what was allocated in    // @BAA
                                // the descriptor, then change the descriptor, and start        // @BAA
                                // again.                                                       // @BAA
                                if (correctLength >= 0) {                                       // @BAA
                                    descriptorChangeNeeded = true;                              // @BAA
                                    rebuildNeeded = true;                                       // @BAA
                                    parameterLengths_[i] = correctLength;                       // @BAA
                                    parameterTotalSize_ = parameterOffsets_[i] + correctLength; // @BAA
                                    if ((i+1) < parameterCount_) {                              // @BAA
                                        for (int j = i+1; j < parameterCount_; ++j) {           // @BAA
                                            parameterOffsets_[j] = parameterTotalSize_;         // @BAA
                                            parameterTotalSize_ += parameterLengths_[j];        // @BAA
                                        }                                                       // @BAA
                                    }                                                           // @BAA
                                }                                                               // @BAA
                            }
                        }

                        if (descriptorChangeNeeded)                                             // @BAA
                            changeDescriptor();                                                 // @BAA
                    } while((rebuildNeeded) && (loopCount == 1));                               // @BAA

                    request.setParameterMarkerBlockIndicator (0);
                }

                // If we are expecting output parameters
                // to be returned, then ask for them as result data.
                if (outputParametersExpected_)
                    request.addOperationResultBitmap (DBSQLRequestDS.ORS_BITMAP_RESULT_DATA);
            }
	    	catch (DBDataStreamException e) {
		    	JDError.throwSQLException (JDError.EXC_INTERNAL, e);
    		}
    	}
    }



/**
Performs common operations needed after a prepare.

@param  sqlStatement    The SQL statement.
@param  reply           The prepare reply.

@exception      SQLException    If an error occurs.
**/
    void commonPrepareAfter (JDSQLStatement sqlStatement,
                             DBReplyRequestedDS reply) // private protected
        throws SQLException
    {
        super.commonPrepareAfter (sqlStatement, reply);

        if (prepared_)
            parameterRow_ = new JDServerRow (connection_, id_,
                reply.getParameterMarkerFormat (), settings_);
    }



/**
Performs common operations needed before a prepare.

@param  sqlStatement    The SQL statement.
@param  request         The prepare request.

@exception      SQLException    If an error occurs.
**/
    void commonPrepareBefore (JDSQLStatement sqlStatement,
                              DBSQLRequestDS request) // private protected
        throws SQLException
    {
        super.commonPrepareBefore (sqlStatement, request);

        if (prepared_) {

            request.addOperationResultBitmap (DBSQLRequestDS.ORS_BITMAP_PARAMETER_MARKER_FORMAT);

        }
    }



/**
Performs common operations in leiu of a prepare.

@param  sqlStatement    The SQL statement.
@param  statementIndex  The cached statement index.

@exception      SQLException    If an error occurs.
**/
    void commonPrepareBypass (JDSQLStatement sqlStatement,
                              int statementIndex) // private protected
        throws SQLException
    {
        super.commonPrepareBypass (sqlStatement, statementIndex);

        if (prepared_)
            parameterRow_ = new JDServerRow (connection_, id_,
                packageManager_.getCachedParameterMarkerFormat (statementIndex),
                settings_);
    }



/**
Runs an SQL statement that may return multiple result sets.
This closes the current result set and clears warnings
before executing the SQL statement again.

<p>Under some situations, a single SQL statement may return
multiple result sets, an update count, or both.  This might occur
either when executing a stored procedure that returns multiple
result sets or when dynamically executing an unknown SQL string.

<p>Use Statement.getMoreResults(), Statement.getResultSet(),
and Statement.getUpdateCount() to navigate through multiple
result sets, an update count, or both.

@return         true if a result set was returned; false
                if an update count was returned or nothing
                was returned.

@exception      SQLException    If the statement is not open,
                                the query timeout limit is
                                exceeded, or an error occurs.
**/
    public boolean execute ()
      throws SQLException
    {
        synchronized(internalLock_) {                                            // @F1A
            checkOpen ();
    
            // Prepare the statement if it is not already done.
            if (! prepared_) {
                resultRow_ = commonPrepare (sqlStatement_);
                prepared_ = true;
            }
    
            // Execute.
            commonExecute (sqlStatement_, resultRow_);
            executed_ = true;
    
            return (resultSet_ != null);
        }
    }



/**
Runs an SQL statement that may return multiple
result sets.  This closes the current result set
and clears warnings before executing a new SQL statement.

<p>Do not use this form of execute() on a prepared statement.

@param  sql     The SQL statement.
@return         true if a result set was returned, false
                if an update count was returned or nothing
                was returned.

@exception      SQLException    This exception is always thrown.
**/
    public boolean execute (String sql)
        throws SQLException
    {
        /* @B1D
        // Call the super class execute() method.  Note that this
        // results in the prepare of a different statement, so the
        // we must mark ours and not prepared.
        prepared_ = false;

        return super.execute (sql);
        */
        JDError.throwSQLException (JDError.EXC_FUNCTION_SEQUENCE);  // @B1A
        return false;                                               // @B1A
    }



/**
Runs the batch of SQL statements.  Batch updates can be used
to submit a set of SQL statements together as a single unit.
The SQL statements are run in the order in which they were
added to the batch.  The batch is cleared after the SQL statements
are run.  In addition, this closes the current result set and
clears warnings before executing the new SQL statement.

<p>When batch updates are run, autocommit should usually be turned off.
This allows the caller to decide whether or not to commit the
transaction in the event that an error occurs and some of the
SQL statements in a batch fail to run.

@return An array of row counts for the SQL statements that are run.
        The array contains one element for each statement in the
        batch of SQL statements.  The array is ordered according to
        the order in which the SQL statements were added to the batch.

@exception SQLException If the statement is not open,
                        an SQL statement contains a syntax
                        error, the query timeout limit is
                        exceeded, an SQL statement returns
                        a result set, or an error occurs.
**/
    public int[] executeBatch ()
        throws SQLException
    {
        synchronized(internalLock_) {                                            // @F1A
            checkOpen ();
            int batchSize = batch_.size ();
            int[] updateCounts = new int[batchSize];
    
            int i = 0;
            try {
                Enumeration enum = batch_.elements ();
                while (enum.hasMoreElements ()) {
    
                    // The caller can intermix direct SQL statements
                    // and sets of parameters for the prepared statement
                    // in the batch.  This code differentiates based on
                    // the type of object in the batch.
                    Object nextElement = enum.nextElement ();
                    if (nextElement instanceof JDSQLStatement) {
    
                        // Prepare and execute.  Check for a result set in
                        // both places.  It is best to catch it after the
                        // prepare (so we don't open a cursor), but with
                        // some stored procedures, we can't catch it until
                        // the execute.
                        prepared_ = false;
                        JDSQLStatement sqlStatement = (JDSQLStatement) nextElement;
                        JDServerRow resultRow = commonPrepare (sqlStatement);
                        if (resultRow != null)
                            JDError.throwSQLException (JDError.EXC_CURSOR_STATE_INVALID);
    
                        commonExecute (sqlStatement, resultRow);
                        executed_ = true;
                        if (resultSet_ != null) {
                            closeResultSet (JDCursor.REUSE_YES);
                            JDError.throwSQLException (JDError.EXC_CURSOR_STATE_INVALID);
                        }
                    }
    
                    else if (nextElement instanceof Object[]) {
    
                        // Prepare the statement if it is not already done.
                        if (! prepared_) {
                            resultRow_ = commonPrepare (sqlStatement_);
                            prepared_ = true;
                        }
    
                        // Execute.  Check for a result set in
                        // both places.  It is best to catch it after the
                        // prepare (so we don't open a cursor), but with
                        // some stored procedures, we can't catch it until
                        // the execute.
                        Object[] parameters = (Object[]) nextElement;
                        Calendar calendar = Calendar.getInstance ();
                        for (int j = 0; j < parameterCount_; ++j)
                            setValue (j+1, parameters[j], calendar, -1); // @B8C
    
                        commonExecute (sqlStatement_, resultRow_);
                        executed_ = true;
                        if (resultSet_ != null) {
                            closeResultSet (JDCursor.REUSE_YES);
                            JDError.throwSQLException (JDError.EXC_CURSOR_STATE_INVALID);
                        }
                    }
    
                    updateCounts[i++] = updateCount_;
                }
            }
            catch (SQLException e) {
    
                // The specification says that if we get an error,
                // then the size of the update counts array should
                // reflect the number of statements that were
                // executed without error.
                int[] updateCounts2 = new int[i];
                System.arraycopy (updateCounts, 0, updateCounts2, 0, i);
    
                batch_.removeAllElements ();
                throw new BatchUpdateException (e.getMessage (),
                    e.getSQLState (), e.getErrorCode (), updateCounts2);
            }
    
            batch_.removeAllElements ();
            return updateCounts;
        }
    }



/**
Runs the SQL statement that returns a single
result set.  This closes the current result set and
clears warnings before executing the SQL statement again.

@return         The result set that contains the data produced
                by the query.

@exception      SQLException    If the statement is not open, no
                                result set is returned by the database,
                                the query timeout limit is exceeded,
                                an input parameter has not been set,
                                or an error occurs.
**/
    public ResultSet executeQuery ()
        throws SQLException
    {
        synchronized(internalLock_) {                                            // @F1A
            checkOpen ();
    
            // Prepare the statement if it is not already done.
            if (! prepared_) {
                resultRow_ = commonPrepare (sqlStatement_);
                prepared_ = true;
            }
    
            // Execute.
            commonExecute (sqlStatement_, resultRow_);
            executed_ = true;
    
            if (resultSet_ == null)
                JDError.throwSQLException (JDError.EXC_CURSOR_STATE_INVALID);
     		return resultSet_;
        }
    }



/**
Runs an SQL statement that returns a single
result set.  This closes the current result set
and clears warnings before executing a new SQL statement.

<p>Do not use this form of executeQuery() on a prepared statement.

@param  sql     The SQL statement.
@return         The result set that contains the data produced
                by the query.

@exception      SQLException    This exception is always thrown.
**/
    public ResultSet executeQuery (String sql)
        throws SQLException
    {
        /* @B1D
        // Call the super class execute() method.  Note that this
        // results in the prepare of a different statement, so the
        // we must mark ours and not prepared.
        prepared_ = false;

        return super.executeQuery (sql);
        */
        JDError.throwSQLException (JDError.EXC_FUNCTION_SEQUENCE);  // @B1A
        return null;                                                // @B1A
    }



/**
Runs an SQL INSERT, UPDATE, or DELETE statement, or any
SQL statement that does not return a result set.
This closes the current result set and clears warnings
before executing the SQL statement again.

@return         Either the row count for INSERT, UPDATE, or
                DELETE, or 0 for SQL statements that
                return nothing.

@exception      SQLException    If the statement is not open,
                                the query timeout limit is
                                exceeded, the statement returns
                                a result set, an input parameter
                                has not been set, or an error occurs.
**/
    public int executeUpdate ()
      throws SQLException
    {
        synchronized(internalLock_) {                                            // @F1A
            checkOpen ();
    
            // Prepare and execute.  Check for a result set in both
            // places.  It is best to catch it after the prepare (so
            // we don't open a cursor), but with some stored procedures,
            // we can't catch it until the execute.
    
            // Prepare the statement if it is not already done.
            if (! prepared_) {
                resultRow_ = commonPrepare (sqlStatement_);
                prepared_ = true;
                if (resultRow_ != null)
                    JDError.throwSQLException (JDError.EXC_CURSOR_STATE_INVALID);
            }
    
            // Execute.
            commonExecute (sqlStatement_, resultRow_);
            executed_ = true;
            if (resultSet_ != null) {
                closeResultSet (JDCursor.REUSE_YES);
                JDError.throwSQLException (JDError.EXC_CURSOR_STATE_INVALID);
            }
    
    		return updateCount_;
        }
    }



/**
Runs an SQL INSERT, UPDATE, or DELETE statement, or any
SQL statement that does not return a result set.
This closes the current result set and clears warnings
before executing a new SQL statement.

<p>Do not use this form of executeUpdate() on a prepared statement.

@param  sql     The SQL statement.
@return         Either the row count for INSERT, UPDATE, or
                DELETE, or 0 for SQL statements that
                return nothing.

@exception      SQLException    This exception is always thrown.
**/
    public int executeUpdate (String sql)
      throws SQLException
    {
        /* @B1D
        // Call the super class execute() method.  Note that this
        // results in the prepare of a different statement, so the
        // we must mark ours and not prepared.
        prepared_ = false;

        return super.executeUpdate (sql);
        */
        JDError.throwSQLException (JDError.EXC_FUNCTION_SEQUENCE);  // @B1A
        return 0;                                                   // @B1A
    }



/**
Copyright.
**/
    static private String getCopyright ()
    {
        return Copyright.copyright;
    }



// JDBC 2.0
/**
Returns the ResultSetMetaData object that describes the
result set's columns.

@return     The metadata object.

@exception  SQLException    If the statement is not open.
**/
    public ResultSetMetaData getMetaData ()
		throws SQLException
    {
        synchronized(internalLock_) {                                            // @F1A
            checkOpen ();
            return new AS400JDBCResultSetMetaData (connection_.getCatalog (), 
                resultSetConcurrency_, cursor_.getName (), resultRow_);
        }
    }



// JDBC 2.0
/**
Sets an input parameter to an Array value.  DB2 for
OS/400 does not support arrays.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value.

@exception  SQLException    Always thrown because DB2
                            for OS/400 does not support arrays.
**/
    public void setArray (int parameterIndex, Array parameterValue)
        throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);
    }



/**
Sets an input parameter to an ASCII stream value.  The driver
reads the data from the stream as needed until no more bytes
are available.  The driver converts this to an SQL VARCHAR
value.

@param  parameterIndex  The parameter index (1-based).
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
    public void setAsciiStream (int parameterIndex,
                                InputStream parameterValue,
                                int length)
        throws SQLException
    {
        if (length < 0)
            JDError.throwSQLException (JDError.EXC_BUFFER_LENGTH_INVALID);
        // @B2D if (parameterValue == null)
        // @B2D    JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);

        setValue (parameterIndex, 
                  (parameterValue == null) ? null : JDUtilities.streamToString (parameterValue, length, "ISO8859_1"), // @B2C
                  Calendar.getInstance (), -1);
    }



/**
Sets an input parameter to a BigDecimal value.  The driver converts
this to an SQL NUMERIC value.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value or null to set
				the value to SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, or the parameter
                            is not an input parameter.
**/
    public void setBigDecimal (int parameterIndex, BigDecimal parameterValue)
        throws SQLException
    {
        // @B2D if (parameterValue == null)
        // @B2D    JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);

        setValue (parameterIndex, parameterValue, null, -1);
    }



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
**/
    public void setBinaryStream (int parameterIndex,
                                InputStream parameterValue,
                                int length)
        throws SQLException
    {
        if (length < 0)
            JDError.throwSQLException (JDError.EXC_BUFFER_LENGTH_INVALID);
        // @B2D if (parameterValue == null)
        // @B2D    JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);

        setValue (parameterIndex, 
                  (parameterValue == null) ? null : JDUtilities.streamToBytes (parameterValue, length), // @B2C
                  null, -1);
    }



// JDBC 2.0
/**
Sets an input parameter to a Blob value.  The driver
converts this to an SQL BLOB value.
<br>If proxy support is in use, the Blob must be serializable.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value or null to set
				the value to SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid,
                            the parameter is not an input parameter,
                            or the parameter is not serializable
                            (when proxy support is in use).
**/
    public void setBlob (int parameterIndex, Blob parameterValue)
        throws SQLException
    {
        // @B2D if (parameterValue == null)
        // @B2D     JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);

        setValue (parameterIndex, parameterValue, null, -1);
    }



/**
Sets an input parameter to a Java boolean value.  The driver
converts this to an SQL SMALLINT value.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value.

@exception  SQLException    If the statement is not open,
                            the index is not valid, or
                            the parameter is not an input parameter.
**/
//
// Implementation note:
//
// The spec defines this in terms of SQL BIT, but DB2 for OS/400
// does not support that.
//
    public void setBoolean (int parameterIndex, boolean parameterValue)
        throws SQLException
    {
        setValue (parameterIndex,
            new Short ((short) (parameterValue ? 1 : 0)), null, -1);
    }



/**
Sets an input parameter to a Java byte value.  The driver
converts this to an SQL SMALLINT value.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value.

@exception  SQLException    If the statement is not open,
                            the index is not valid, or
                            the parameter is not an input parameter.
**/
//
// Implementation note:
//
// The spec defines this in terms of SQL TINYINT, but DB2 for OS/400
// does not support that.
//
    public void setByte (int parameterIndex, byte parameterValue)
        throws SQLException
    {
        setValue (parameterIndex, new Short (parameterValue), null, -1);
    }



/**
Sets an input parameter to a Java byte array value.  The driver
converts this to an SQL VARBINARY value.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value or null to set
				the value to SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, or the parameter
                            is not an input parameter.
**/
    public void setBytes (int parameterIndex, byte[] parameterValue)
        throws SQLException
    {
        // @B2D if (parameterValue == null)
        // @B2D     JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);

        setValue (parameterIndex, parameterValue, null, -1);
    }



// JDBC 2.0
/**
Sets an input parameter to a character stream value.  The driver
reads the data from the character stream as needed until no more
characters are available.  The driver converts this to an SQL
VARCHAR value.

@param  parameterIndex  The parameter index (1-based).
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
    public void setCharacterStream (int parameterIndex,
                                    Reader parameterValue,
                                    int length)
        throws SQLException
    {
        if (length < 0)
            JDError.throwSQLException (JDError.EXC_BUFFER_LENGTH_INVALID);
        // @B2D if (parameterValue == null)
        // @B2D     JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);

        setValue (parameterIndex, 
                  (parameterValue == null) ? null : JDUtilities.readerToString (parameterValue, length), // @B2C
                  Calendar.getInstance (), -1);
    }



// JDBC 2.0
/**
Sets an input parameter to a Clob value.  The driver
converts this to an SQL CLOB value.
<br>If proxy support is in use, the Clob must be serializable.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value or null to set
				the value to SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid,
                            the parameter is not an input parameter,
                            or the parameter is not serializable
                            (when proxy support is in use).
**/
    public void setClob (int parameterIndex, Clob parameterValue)
        throws SQLException
    {
        // @B2D if (parameterValue == null)
        // @B2D     JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);

        setValue (parameterIndex, parameterValue, null, -1);
    }



/**
Sets an input parameter to a java.sql.Date value using the
default calendar.  The driver converts this to an SQL DATE
value.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value or null to set
				the value to SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, or the parameter
                            is not an input parameter.
**/
    public void setDate (int parameterIndex, Date parameterValue)
        throws SQLException
    {
        // @B2D if (parameterValue == null)
        // @B2D     JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);

        setValue (parameterIndex, parameterValue, Calendar.getInstance (), -1);
    }



// JDBC 2.0
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
**/
    public void setDate (int parameterIndex,
                         Date parameterValue,
                         Calendar calendar)
        throws SQLException
    {
        // @B2D if (parameterValue == null)
        // @B2D     JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);
        if (calendar == null)
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        setValue (parameterIndex, parameterValue, calendar, -1);
    }



/**
Sets an input parameter to a Java double value.  The driver
converts this to an SQL DOUBLE value.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value.

@exception  SQLException    If the statement is not open,
                            the index is not valid or
                            the parameter is not an input parameter.
**/
    public void setDouble (int parameterIndex, double parameterValue)
        throws SQLException
    {
        setValue (parameterIndex, new Double (parameterValue), null, -1);
    }



/**
Sets an input parameter to a Java float value.  The driver
converts this to an SQL REAL value.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value.

@exception  SQLException    If the statement is not open,
                            the index is not valid, or
                            the parameter is not an input parameter.
**/
//
// Note:  The JDBC 1.22 specification states that this
//        method should set an SQL FLOAT value.  However,
//        all tables map float to REAL.  Otherwise,
//        nothing is symetrical and certain INOUT
//        parameters do not work.
//
    public void setFloat (int parameterIndex, float parameterValue)
        throws SQLException
    {
        setValue (parameterIndex, new Float (parameterValue), null, -1);
    }



/**
Sets an input parameter to a Java int value.  The driver
converts this to an SQL INTEGER value.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value.

@exception  SQLException    If the statement is not open,
                            the index is not valid or
                            the parameter is not an input parameter.
**/
    public void setInt (int parameterIndex, int parameterValue)
        throws SQLException
    {
        setValue (parameterIndex, new Integer (parameterValue), null, -1);
    }



// @D0C
/**
Sets an input parameter to a Java long value.  
If the connected AS/400 supports SQL BIGINT data, the driver
converts this to an SQL BIGINT value.  Otherwise, the driver
converts this to an SQL INTEGER value.  SQL BIGINT data is
supported on V4R5 and later.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value.

@exception  SQLException    If the statement is not open,
                            the index is not valid, or
                            the parameter is not an input parameter.
**/
//
// Implementation note:
//
// The spec defines this in terms of SQL BIGINT, but DB2 for OS/400
// does not support that until V4R5.
//
    public void setLong (int parameterIndex, long parameterValue)
        throws SQLException
    {
        setValue (parameterIndex, new Long(parameterValue), null, -1); // @D0C
    }



/**
Sets an input parameter to SQL NULL.

@param  parameterIndex  The parameter index (1-based).
@param  sqlType         The SQL type code defined in java.sql.Types.

@exception  SQLException    If the statement is not open,
                            the index is not valid,
                            the parameter is not an input parameter,
                            or the SQL type is not valid.
**/
    public void setNull (int parameterIndex, int sqlType)
      throws SQLException
    {               
        // @BBD if (sqlType != parameterRow_.getSQLData (parameterIndex).getType ())
        // @BBD     JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);
        testSQLType(sqlType, parameterIndex);                                               // @BBA

        setValue (parameterIndex, null, null, -1);
    }


// @B4 - Added for JDK 2.0RC1 - typeName can be ignored, since it is not relevant to AS/400.
/**
Sets an input parameter to SQL NULL.

@param  parameterIndex  The parameter index (1-based).
@param  sqlType         The SQL type code defined in java.sql.Types.
@param  typeName        The fully-qualified name of an SQL structured type.  This value will be ignored.

@exception  SQLException    If the statement is not open,
                            the index is not valid,
                            the parameter is not an input parameter,
                            or the SQL type is not valid.
**/
    public void setNull (int parameterIndex, int sqlType, String typeName)
      throws SQLException
    {               
        setNull (parameterIndex, sqlType); 				
    }

/**
Sets an input parameter to an Object value.  The driver converts
this to a value of an SQL type, depending on the type of the
specified value.  The JDBC specification defines a standard
mapping from Java types to SQL types.  In the cases where a
SQL type is not supported by DB2 for OS/400, the 
<a href="../../../../SQLTypes.html#unsupported">next closest matching type</a>
is used.
<br>If proxy support is in use, the Object must be serializable.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value or null to set
				the value to SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid,
                            the parameter is not an input parameter,
                            the type of value is not supported,
                            or the parameter is not serializable
                            (when proxy support is in use).
**/
    public void setObject (int parameterIndex, Object parameterValue)
        throws SQLException
    {
        // @B2D if (parameterValue == null)
        // @B2D     JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);

        setValue (parameterIndex, parameterValue, Calendar.getInstance (), -1);
    }



/**
Sets an input parameter to an Object value.  The driver converts
this to a value with the specified SQL type.
<br>If proxy support is in use, the Object must be serializable.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value or null to set
				the value to SQL NULL.
@param  sqlType         The SQL type code defined in java.sql.Types.

@exception  SQLException    If the statement is not open,
                            the index is not valid,
                            the parameter is not an input parameter,
                            the SQL type is not valid,
                            or the parameter is not serializable
                            (when proxy support is in use).
**/
    public void setObject (int parameterIndex,
			               Object parameterValue,
			               int sqlType)
      throws SQLException
    {
        // @B2D if (parameterValue == null)
        // @B2D     JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);

        // @BBD if (sqlType != parameterRow_.getSQLData (parameterIndex).getType ())
        // @BBD     JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);
        testSQLType(sqlType, parameterIndex);                                               // @BBA

        setValue (parameterIndex, parameterValue, Calendar.getInstance (), -1);
    }



/**
Sets an input parameter to an Object value.  The driver converts
this to a value with the specified SQL type.
<br>If proxy support is in use, the Object must be serializable.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value or null to set
				the value to SQL NULL.
@param  sqlType         The SQL type code defined in java.sql.Types.
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
    public void setObject (int parameterIndex,
			               Object parameterValue,
			               int sqlType,
			               int scale)
        throws SQLException
    {
        // @B2D if (parameterValue == null)
        // @B2D     JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);

        // @BBD if (sqlType != parameterRow_.getSQLData (parameterIndex).getType ())
        // @BBD     JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);
        testSQLType(sqlType, parameterIndex);                                               // @BBA

        if (scale < 0)
            JDError.throwSQLException (JDError.EXC_SCALE_INVALID);

        setValue (parameterIndex, parameterValue, Calendar.getInstance (), scale);
	}



// JDBC 2.0
/**
Sets an input parameter to a Ref value.  DB2 for
OS/400 does not support structured types.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value.

@exception  SQLException    Always thrown because DB2
                            for OS/400 does not support structured types.
**/
    public void setRef (int parameterIndex, Ref parameterValue)
        throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);
    }



/**
Sets an input parameter to a Java short value.  The driver
converts this to an SQL SMALLINT value.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value.

@exception  SQLException    If the statement is not open,
                            the index is not valid or
                            the parameter is not an input parameter.
**/
    public void setShort (int parameterIndex, short parameterValue)
        throws SQLException
    {
        setValue (parameterIndex, new Short (parameterValue), null, -1);
    }



/**
Sets an input parameter to a String value.  The driver
converts this to an SQL VARCHAR value.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value or null to set
				the value to SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, or the parameter
                            is not an input parameter.
**/
    public void setString (int parameterIndex, String parameterValue)
        throws SQLException
    {
        // @B2D if (parameterValue == null)
        // @B2D     JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);

        setValue (parameterIndex, parameterValue, Calendar.getInstance (), -1); // @B7C
    }



/**
Sets an input parameter to a java.sql.Time value using the
default calendar.  The driver converts this to an SQL TIME value.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value or null to set
				the value to SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, or the parameter
                            is not an input parameter.
**/
    public void setTime (int parameterIndex, Time parameterValue)
        throws SQLException
    {
        // @B2D if (parameterValue == null)
        // @B2D     JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);

        setValue (parameterIndex, parameterValue, Calendar.getInstance (), -1);
    }



// JDBC 2.0
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
**/
    public void setTime (int parameterIndex,
                         Time parameterValue,
                         Calendar calendar)
        throws SQLException
    {
        // @B2D if (parameterValue == null)
        // @B2D     JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);
        if (calendar == null)
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        setValue (parameterIndex, parameterValue, calendar, -1);
    }



/**
Sets an input parameter to a java.sql.Timestamp value using the
default calendar.  The driver converts this to an SQL TIMESTAMP
value.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value or null to set
				the value to SQL NULL.

@exception  SQLException    If the statement is not open,
                            the index is not valid, or the parameter
                            is not an input parameter.
**/
    public void setTimestamp (int parameterIndex, Timestamp parameterValue)
        throws SQLException
    {
        // @B2D if (parameterValue == null)
        // @B2D     JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);

        setValue (parameterIndex, parameterValue, Calendar.getInstance (), -1);
    }



// JDBC 2.0
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
**/
    public void setTimestamp (int parameterIndex,
                              Timestamp parameterValue,
                              Calendar calendar)
        throws SQLException
    {
        // @B2D if (parameterValue == null)
        // @B2D     JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);
        if (calendar == null)
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        setValue (parameterIndex, parameterValue, calendar, -1);
    }



/**
Sets an input parameter to a Unicode stream value.  The driver
reads the data from the stream as needed until no more bytes
are available.  The driver converts this to an SQL VARCHAR
value.
<p>Note that the number of bytes in a Unicode stream can be
computed as 2 multiplied by the number of characters plus 2 bytes for the
byte-order mark.  If an uneven number of bytes is specified,
then Java will convert this to an empty String.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value or null to set
		        		the value to SQL NULL.
@param  length          The number of bytes in the stream.

@exception  SQLException    If the statement is not open,
                            the index is not valid, the parameter
                            is not an input parameter, the length 
        				    is not valid,
                            the input stream does not contain all
                            Unicode characters, or an error occurs
                            while reading the input stream

@deprecated Use setCharacterStream(int, Reader, int) instead.
@see #setCharacterStream
**/
    public void setUnicodeStream (int parameterIndex,
                                  InputStream parameterValue,
                                  int length)
        throws SQLException
    {
        if (length < 0)
            JDError.throwSQLException (JDError.EXC_BUFFER_LENGTH_INVALID);
        // @B2D if (parameterValue == null)
        // @B2D     JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);

        setValue (parameterIndex, 
                  (parameterValue == null) ? null : JDUtilities.streamToString (parameterValue, length, "UnicodeBig"), // @B2C @B3C
                  Calendar.getInstance (), -1);
    }



/**
Sets an input parameter value for the specified index,
and performs all appropriate validation.

@param  parameterIndex  The parameter index (1-based).
@param  parameterValue  The parameter value or null if
                        the value is SQL NULL.
@param  calendar        The calendar, or null if not
                        applicable.
@param  scale           The scale, or -1 if not applicable.                      

@exception  SQLException    If the statement is not open,
                            the index is not valid or
                            the parameter is not an input
                            parameter.
**/
    void setValue (int parameterIndex,
                   Object parameterValue,
                   Calendar calendar,
                   int scale) // private protected
        throws SQLException
    {
        synchronized(internalLock_) {                                            // @F1A
            checkOpen ();
    
            // Check if the parameter index refers to the return value parameter.          @F2A
            // This is an OUT parameter, so sets are not allowed.  If its not              @F2A
            // parameter index 1, then decrement the parameter index, since we             @F2A
            // are "faking" the return value parameter.                                    @F2A
            if (useReturnValueParameter_) {                                             // @F2A
                if (parameterIndex == 1)                                                // @F2A
                    JDError.throwSQLException(JDError.EXC_PARAMETER_TYPE_INVALID);      // @F2A
                else                                                                    // @F2A
                    --parameterIndex;                                                   // @F2A
            }

            // Validate the parameter index.
            if ((parameterIndex < 1) || (parameterIndex > parameterCount_))
                JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID);
    
            // Check that the parameter is an input parameter.
            if (! parameterRow_.isInput (parameterIndex))
                JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);
    
            // Set the parameter data.  If there is a type mismatch,
            // set() with throw an exception.
            SQLData sqlData = parameterRow_.getSQLData (parameterIndex);
            if (parameterValue != null) {                                                                   // @B6C    
                
                // If the data is a locator, then set its handle.                                              @B6A
                if (sqlData instanceof SQLLocator) {                                                        // @B6A
                    SQLLocator sqlDataAsLocator = (SQLLocator) sqlData;                                     // @B6A
                    sqlDataAsLocator.setHandle (parameterRow_.getFieldLOBLocatorHandle (parameterIndex));   // @B6A
                }                                                                                           // @B6A
    
                sqlData.set (parameterValue, calendar, scale);            
            }                                                                                               // @B6A
            parameterNulls_[parameterIndex-1] = (parameterValue == null);
            parameterSet_[parameterIndex-1] = true;
    
            if (dataTruncation_)                                    // @B5A
                testDataTruncation (parameterIndex, sqlData);       // @B5C
        }
    }



/**
Tests if a DataTruncation occurred on the write of a piece of
data and throws a DataTruncation exception if so.

@param  index   The index (1-based).
@param  data    The data that was written or null for SQL NULL.
**/
    private void testDataTruncation (int parameterIndex, SQLData data)
        throws DataTruncation
    {
        if (data != null) {
            int truncated = data.getTruncated ();
            if (truncated > 0) {
                int actualSize = data.getActualSize ();
                throw new DataTruncation (parameterIndex, true, false,
                    actualSize + truncated, actualSize);
            }
        }
    }


// @BBA
/**
Checks that an input SQL type is compatible with the actual parameter type.

@param sqlType          The SQL type.
@param parameterIndex   The index (1-based).

@exception  SQLException    If the SQL type is not compatible.
**/
    private void testSQLType(int sqlType, int parameterIndex)
        throws SQLException
    {
        int parameterType = parameterRow_.getSQLData(parameterIndex).getType();

        if (sqlType != parameterType) {

            // If the only reason the types don't match is because one
            // is a CHAR and the other is a VARCHAR, then let this 
            // slide.
            if (((sqlType == Types.CHAR) || (sqlType == Types.VARCHAR))
                && ((parameterType == Types.CHAR) || (parameterType == Types.VARCHAR)))
                ; // Do nothing!
            else
                JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID);
        }
    }



}
