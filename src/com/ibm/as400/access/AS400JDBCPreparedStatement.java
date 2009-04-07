///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCPreparedStatement.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.io.Reader;
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
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


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
//    indices to the database's indices by decrementing by 1 as needed.
//
// @G8c
// 3. If there is a return value (ie ?=call xxxx) and the parameter
//    index is 1 then return data for the return value (always an Integer).
//    If not, decrement the parm index by one because internally the return
//    value doesn't count.  If there is no return value the count is correct
//    so don't do anything in that case.  Also, the database supports returning
//    only integers so the metadata will always be an SQLInteger.
//
public class AS400JDBCPreparedStatement extends AS400JDBCStatement implements PreparedStatement
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";


    private boolean             dataTruncation_;        // @B5A
    private int                 descriptorHandle_;
    boolean             executed_;              // private protected
    private boolean             outputParametersExpected_;
    int                 parameterCount_;        // private protected
    boolean             batchExecute_;          // private protected            @G9A
    private int[]               parameterLengths_;
    private int[]               parameterOffsets_;
    private boolean[]           parameterNulls_;
    private boolean[]           parameterDefaults_;    //@EIA
    private boolean[]           parameterUnassigned_;  //@EIA
    //@re-prep move to statement JDServerRow         parameterRow_;          // private protected
    Vector              batchParameterRows_;    // private protected            @G9A
    private int                 parameterTotalSize_;
    boolean[]           parameterSet_;          // private protected
    private boolean             prepared_;
    private JDServerRow         resultRow_;
    SQLInteger          returnValueParameter_;  // private protected            @F2A
    JDSQLStatement      sqlStatement_;            // @G4c (used by callable statement)
    boolean             useReturnValueParameter_; // private protected          @F2A
    private int         maxToLog_ = 10000;        // Log value of parameter markers up to this length // @H1A

    private int containsLocator_ = LOCATOR_UNKNOWN;
    private static final int LOCATOR_UNKNOWN = -1;
    private static final int LOCATOR_NOT_FOUND = 0;
    private static final int LOCATOR_FOUND = 1;
    
    private static final short INDICATOR_NULL = -1;        //@EIA
    private static final short INDICATOR_DEFAULT = -5;     //@EIA
    private static final short INDICATOR_UNASSIGNED = -7;  //@EIA
    private boolean isjvm16Synchronizer;//@dmy
    //@dmy private dummy outputstream
    OutputStream dummyOutputStream = new OutputStream() {
        private int b1 = 0;
        public synchronized void write(int b) throws IOException {  b1 = b; }
    };
    private PrintWriter dummyPrint = new PrintWriter(dummyOutputStream, true); //@dmy
    
    static final int LOB_BLOCK_SIZE = 262144;

    /**
    Constructs an AS400JDBCPreparedStatement object.
  
    @param   connection                 The connection to the system.
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
    @param   resultSetHoldability       The result set holdability.
    @param   autoGeneratedKeys          The auto-generated keys requested
  
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
                                int resultSetConcurrency,
                                int resultSetHoldability,           //@G4A
                                int autoGeneratedKeys)       //@G4A
    throws SQLException
    {
        super (connection, id, transactionManager,
               packageManager, blockCriteria, blockSize,
               prefetch, packageCriteria, resultSetType,
               resultSetConcurrency, resultSetHoldability, autoGeneratedKeys);

        //Temporary fix for jvm 1.6 memroy stomp issue. (remove //@dmy code when jvm issue is resolved)
        //This fix will just trace a few extra traces to a dummy stream
        //if system property or jdbc property is set to false then extra trace is not executed
        //null value for system property means not specified...so true by default
        String jvm16Synchronize = SystemProperties.getProperty (SystemProperties.JDBC_JVM16_SYNCHRONIZE); //@dmy
        isjvm16Synchronizer = true;  //@dmy //true by default
        if(((jvm16Synchronize != null) && (Boolean.valueOf(jvm16Synchronize.trim()).booleanValue() == false)) || !connection_.getProperties().getBoolean(JDProperties.JVM16_SYNCHRONIZE))    //@dmy    
            isjvm16Synchronizer = false;  //@dmy
        //also only set to true if jvm 1.6.  Try to load jvm 1.6 only class
        try{                                                    //@dmy
            Class.forName("java.sql.SQLXML");                   //@dmy
        }catch(Exception e){                                    //@dmy
            isjvm16Synchronizer = false;                        //@dmy
        }                                                       //@dmy
        
        batchExecute_               = false;                                        // @G9A
        outputParametersExpected_   = outputParametersExpected;
        parameterCount_             = sqlStatement.countParameters();
        parameterLengths_           = new int[parameterCount_];
        parameterNulls_             = new boolean[parameterCount_];
        parameterDefaults_          = new boolean[parameterCount_];          //@EIA
        parameterUnassigned_        = new boolean[parameterCount_];          //@EIA
        parameterOffsets_           = new int[parameterCount_];
        parameterSet_               = new boolean[parameterCount_];
        sqlStatement_               = sqlStatement;
        useReturnValueParameter_    = sqlStatement.hasReturnValueParameter();       // @F2A

        if(useReturnValueParameter_)                                               // @F2A
            returnValueParameter_   = new SQLInteger(connection_.getVRM());                             // @F2A  //@trunc3

        if(JDTrace.isTraceOn())
        {                                                  // @D1A @F2C
            JDTrace.logInformation (this, "Preparing [" + sqlStatement_ + "]");     // @D1A
            if(useReturnValueParameter_)                                           // @F2A
                JDTrace.logInformation(this, "Suppressing return value parameter (?=CALL)"); // @F2A
        }                                                                           // @F2A

        // Do not allow statements to be immediately
        // executed.  If we did not do this, then some
        // statements would get executed at prepare time.
        allowImmediate_ = false;

        // Prepare.
        prepared_ = true;
        //@L1A  Added try catch block around commonPrepare() for JTOpen Bug #3605 Statement not fully closed on error.
        // If an error occurs in the preparing of the statement, we need to close any resources used by the PreparedStatement object.
        try{                                                    
            resultRow_ = commonPrepare(sqlStatement_);
        }catch(SQLException e){
            close();
            throw e;
        }
        executed_ = false;

        dataTruncation_ = connection.getProperties().getBoolean(JDProperties.DATA_TRUNCATION);

        clearParameters();
    }



    // JDBC 2.0
    /**
    Adds the set of parameters to the current batch.
  
    @exception SQLException     If the statement is not open or
                                an input parameter has not been set.
    **/
    public void addBatch() throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();
            Object[] parameters = new Object[parameterCount_];
            for(int i = 0; i < parameterCount_; ++i)
            {
                // Statements with output or inout parameters are not allowed in the batch.
                if(parameterRow_.isOutput(i+1)) JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

                // If an input parameter is not set, we throw an exception.
                if(!parameterSet_[i]) JDError.throwSQLException(this, JDError.EXC_PARAMETER_COUNT_MISMATCH);

                //@KBA Need to check for locators even if the parameter is null
                //  If we don't check for locators and the first row had locator fields that are all null, the LOCATOR_FOUND
                //  flag was not being set.  This meant that we thought we could batch when executeBatch() was called.  We cannot
                //  batch when locators are being used.  
                SQLData sqlData = parameterRow_.getSQLData(i+1);    //@KBA  
                // Save the parameter in the array.  If it's null, just leave it null.
                if(!parameterNulls_[i])
                {
                  //@KBD  SQLData sqlData = parameterRow_.getSQLData(i+1);
                  //For default and unassigned extended indicator values, we use Byte to contain the indicator flag
                  if(parameterDefaults_[i])                          //@EIA
                      parameters[i] = new Byte("1");  //default      //@EIA
                  else if(parameterUnassigned_[i])                   //@EIA
                      parameters[i] = new Byte("2");  //unassigned   //@EIA
                  else                                               //@EIA
                      parameters[i] = sqlData.getObject();
                  //@KBD  if(containsLocator_ == LOCATOR_UNKNOWN)
                  //@KBD  { 
                  //@KBD    int sqlType = sqlData.getSQLType();
                  //@KBD    if (sqlType == SQLData.CLOB_LOCATOR ||
                  //@KBD        sqlType == SQLData.BLOB_LOCATOR ||
                  //@KBD        sqlType == SQLData.DBCLOB_LOCATOR)
                  //@KBD    {
                  //@KBD      containsLocator_ = LOCATOR_FOUND;
                  //@KBD    }
                  //@KBD  }
                }

                //@KBA check to see if the parameter is a locator field
                if(containsLocator_ == LOCATOR_UNKNOWN)             //@KBA
                {                                                   //@KBA
                    int sqlType = sqlData.getSQLType();             //@KBA
                    if (sqlType == SQLData.CLOB_LOCATOR ||          //@KBA
                        sqlType == SQLData.BLOB_LOCATOR ||          //@KBA
                        sqlType == SQLData.DBCLOB_LOCATOR)          //@KBA
                    {                                               //@KBA
                        containsLocator_ = LOCATOR_FOUND;           //@KBA
                    }                                               //@KBA
                }                                                   //@KBA
            }
            if(containsLocator_ == LOCATOR_UNKNOWN) containsLocator_ = LOCATOR_NOT_FOUND;

            if(batch_ == null) batch_ = new Vector(); //@P0A
            if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "addBatch()");
            batch_.addElement(parameters);
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
    public void addBatch(String sql) throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); // @B1C
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
        DBSQLDescriptorDS request2 = null; //@P0C
        try
        {
            descriptorHandle_ = id_;
            //@P0CDBSQLDescriptorDS request2 = new DBSQLDescriptorDS (DBSQLDescriptorDS.FUNCTIONID_CHANGE_DESCRIPTOR,
            //@P0C    id_, 0, descriptorHandle_);
            request2 = DBDSPool.getDBSQLDescriptorDS(DBSQLDescriptorDS.FUNCTIONID_CHANGE_DESCRIPTOR, id_, 0, descriptorHandle_); //@P0C

            DBDataFormat parameterMarkerDataFormat;
            if(connection_.useExtendedFormats ())
            {
                //@540  We are going to continue using DBExtendedDataFormat for this part.  We use an empty string for the parameter name so we don't need 128 bytes
                parameterMarkerDataFormat = new DBExtendedDataFormat (parameterCount_);
            }
            else
                parameterMarkerDataFormat = new DBOriginalDataFormat (parameterCount_);
            request2.setParameterMarkerDataFormat (parameterMarkerDataFormat);

            parameterMarkerDataFormat.setConsistencyToken (1);
            parameterMarkerDataFormat.setRecordSize (parameterTotalSize_);
            
            if(isjvm16Synchronizer)
                dummyPrint.print("!!!changeDescriptor:  totalParameterLength_ = " + parameterTotalSize_);  //@dmy
            
            
            for(int i = 0; i < parameterCount_; ++i)
            {
                SQLData sqlData = parameterRow_.getSQLData (i+1);

                parameterMarkerDataFormat.setFieldDescriptionLength (i);
                parameterMarkerDataFormat.setFieldLength (i, parameterLengths_[i]);
                parameterMarkerDataFormat.setFieldCCSID (i, parameterRow_.getCCSID (i+1));

                parameterMarkerDataFormat.setFieldNameLength (i, 0);
                parameterMarkerDataFormat.setFieldNameCCSID (i, 0);
                parameterMarkerDataFormat.setFieldName (i, "", connection_.converter_); //@P0C

                parameterMarkerDataFormat.setFieldSQLType (i,
                                                           (short) (sqlData.getNativeType() | 0x0001));
                parameterMarkerDataFormat.setFieldScale (i,
                                                         (short) sqlData.getScale());
                parameterMarkerDataFormat.setFieldPrecision (i,
                                                             (short) sqlData.getPrecision());
                if(isjvm16Synchronizer)
                    dummyPrint.print("!!!changeDescriptor:  Parameter " + (i+1) + " length = " + parameterLengths_[i]);
            }

            connection_.send (request2, descriptorHandle_);

            if(JDTrace.isTraceOn())
                JDTrace.logInformation (this, "Descriptor " + descriptorHandle_ + " created or changed");
        }
        catch(DBDataStreamException e)
        {
            JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
        }
        finally
        { //@P0C
            
            if(isjvm16Synchronizer){
                dummyPrint.print("!!!changeDescriptor.inUser_(false): request2-id=" +  request2.hashCode());
                dummyPrint.flush();
            }
            if(request2 != null) request2.inUse_ = false; //@P0C
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
        synchronized(internalLock_)
        {                                            // @F1A
            checkOpen ();

            for(int i = 0; i < parameterCount_; ++i)
            {
                // @E1D parameterLengths_[i]    = 0;
                parameterNulls_[i]      = false;
                parameterDefaults_[i]      = false;   //@EIA
                parameterUnassigned_[i]    = false;   //@EIA
                // @E1D parameterOffsets_[i]    = 0;
                parameterSet_[i]        = false;
            }

            // @E1D parameterTotalSize_ = 0;

            if(useReturnValueParameter_)                                       // @F2A
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
        synchronized(internalLock_)
        {                                            // @F1A
            // If this is already closed, then just do nothing.
            //
            // The spec does not define what happens when a connection
            // is closed multiple times.  The official word from the Sun
            // JDBC team is that "the driver's behavior in this case
            // is implementation defined.   Applications that do this are
            // non-portable."
            if(isClosed ())
                return;

            // If a descriptor was created somewhere along
            // the lines, then delete it now.
            if(descriptorHandle_ != 0)
            {
                //@P0CDBSQLDescriptorDS request = new DBSQLDescriptorDS (
                //@P0C    DBSQLDescriptorDS.FUNCTIONID_DELETE_DESCRIPTOR,
                //@P0C    id_, 0, descriptorHandle_);
                DBSQLDescriptorDS request = null; //@P0C
                try
                { //@P0C
                    request = DBDSPool.getDBSQLDescriptorDS(DBSQLDescriptorDS.FUNCTIONID_DELETE_DESCRIPTOR, id_, 0, descriptorHandle_); //@P0C

                    connection_.send (request, descriptorHandle_);
                }
                finally
                { //@P0C
                    if(isjvm16Synchronizer)
                        dummyPrint.print("!!!close.inUser_(false): request-id=" +  request.hashCode());
                    if(request != null) request.inUse_ = false; //@P0C
                }

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

        if(prepared_)
        {

            DBData resultData = null;
            if(outputParametersExpected_)
                resultData = reply.getResultData ();

            // Store the output parameters, if needed.
            if((outputParametersExpected_) && (resultData != null))
            {
                parameterRow_.setServerData (resultData);
                parameterRow_.setRowIndex (0);
            }

            // Handle the return value parameter, if needed.                           @F2A
            try
            {                                                                   // @F2A
                if(useReturnValueParameter_)                                       // @F2A
                    returnValueParameter_.set(reply.getSQLCA().getErrd (1));        // @F2A  //@G3C
            }                                                                       // @F2A
            catch(DBDataStreamException e)
            {                                       // @F2A
                JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);                // @F2A
            }                                                                       // @F2A
        }
    }



    /**
    Performs common operations needed before an execute.
  
    @param  sqlStatement    The SQL statement.
    @param  request         The execute request.
  
    @exception      SQLException    If an error occurs.
    **/
    void commonExecuteBefore(JDSQLStatement sqlStatement, DBSQLRequestDS request) throws SQLException
    {
        super.commonExecuteBefore (sqlStatement, request);

        if(prepared_)
        {
            // Close the result set before executing again.
            closeResultSet (JDCursor.REUSE_YES);

            // Validate each parameters.   If a parameter is not an
            // input parameter, then it is okay for it not to have been
            // set.  However, if an input parameter was not set,
            // we throw an exception.
            boolean outputExpected_ = false;        // @K2A We do not want to increment our row index in commonExecuteAfter() if there are no output parameters
            for(int i = 0; i < parameterCount_; ++i)
            {
                if(!parameterSet_[i] && parameterRow_.isInput(i+1))
                {
                    JDError.throwSQLException (this, JDError.EXC_PARAMETER_COUNT_MISMATCH);
                }

                if(parameterRow_.isOutput(i+1))    //  @K2A
                    outputExpected_ = true;        //  @K2A
            }
            if(!outputExpected_)                   //  @K2A
                outputParametersExpected_ = false; //  @K2A

            // Create the descriptor if needed.  This should only
            // be done once (on the first execute for the prepared
            // statement).
            if((parameterCount_ > 0) && (descriptorHandle_ == 0))
            {
                // Get the offset and length for each parameter.
                // We just use the information that came in the parameter
                // marker format from reply for the prepare.
                parameterTotalSize_ = 0;
                for(int i = 0; i < parameterCount_; ++i)
                {
                    parameterLengths_[i] = parameterRow_.getLength (i+1);
                    parameterOffsets_[i] = parameterTotalSize_;
                    parameterTotalSize_ += parameterLengths_[i];
                    if(isjvm16Synchronizer)
                        dummyPrint.print("!!!commonExecuteBefore:  Parameter " + (i+1) + " length = " + parameterLengths_[i] );
                }
                if(isjvm16Synchronizer)
                    dummyPrint.print("!!!commonExecuteBefore:  totalParameterLength_ = " + parameterTotalSize_);
                changeDescriptor();
            }

            // Add the parameter information to the execute request.
            try
            {
                request.setStatementType(sqlStatement.getNativeType());

                // Set the descriptor handle.
                request.setParameterMarkerDescriptorHandle (descriptorHandle_);

                // If there are any parameters, then send the parameter
                // values with the execute data stream.  Only the
                // input parameters are included here.
                if(parameterCount_ > 0)
                {
                    // In building the parameter marker data, we may discover that the              // @BAA
                    // descriptor needs to be changed.  If so, we will need to change the           // @BAA
                    // descriptor, then rebuild the parameter marker data based on that             // @BAA
                    // change.  This is implemented using a do-while, but it should never           // @BAA
                    // have to loop more than once, since the second time through (in a             // @BAA
                    // particular execute) every thing should be great.                             // @BAA
                    boolean descriptorChangeNeeded = false;

                    do
                    {
                        descriptorChangeNeeded = false; // Reset our flag every time through the loop.

                        // Allocate the space for the Extended Parameter Marker Data                   @G9A
                        // This is the amount of space for all of the rows' data and indicators        @G9A
                        DBData parameterMarkerData;
                        int rowCount = batchExecute_ ? batchParameterRows_.size() : 1;
                        if(connection_.useExtendedFormats ())
                        {
                            parameterMarkerData = new DBExtendedData(rowCount, parameterCount_, 2, parameterTotalSize_);
                        }
                        else
                        {
                            parameterMarkerData = new DBOriginalData(rowCount, parameterCount_, 2, parameterTotalSize_);
                        }
                        for(int rowLoop = 0; rowLoop < rowCount; ++rowLoop)                    // @G9a
                        {
                            Object[] parameters = null;                                               // @G9A
                            if(batchExecute_)                                                      // @G9A
                            {
                                // @G9A
                                //@CRS - Don't need to synch around this because we have been called
                                // by executeBatch() which is already inside the synch block.
                                parameters = (Object[])batchParameterRows_.get(rowLoop);              // @G9A
                            }                                                                         // @G9A

                            // If this is the first of multiple rows.  Set the Parameter Marker          @G9A
                            //   Data code point and consistency token only once                         @G9A
                            if(rowLoop == 0)                                                       // @G9A
                            {
                                request.setParameterMarkerData (parameterMarkerData);
                                parameterMarkerData.setConsistencyToken (1);                           // @G9M
                            }                                                                         // @G9A

                            int rowDataOffset = parameterMarkerData.getRowDataOffset(rowLoop);     // @G9C
                            for(int i = 0; i < parameterCount_; ++i)
                            {
                                // @G1 -- zero out the comm buffer if the parameter marker is null.
                                //        If the buffer is not zero'ed out old data will be sent to
                                //        the system possibily messing up a future request.
                                if((batchExecute_ && (parameters[i] == null || parameters[i] instanceof Byte)) ||               // @G9A //@EIC
                                   (!batchExecute_ && (parameterNulls_[i] || parameterDefaults_[i] || parameterUnassigned_[i])))              // @B9C @G9C  //@EIC
                                {
                                    short indicatorValue = INDICATOR_NULL;                  //@EIA
                                    if( batchExecute_ )                                     //@EIA
                                    {                                                       //@EIA
                                        if( parameters[i] == null )                         //@EIA
                                            indicatorValue = INDICATOR_NULL;                //@EIA
                                        else if( ((Byte)parameters[i]).byteValue() == 1 )   //@EIA
                                            indicatorValue = INDICATOR_DEFAULT;             //@EIA
                                        else if( ((Byte)parameters[i]).byteValue() == 2 )   //@EIA
                                            indicatorValue = INDICATOR_UNASSIGNED;          //@EIA
                                    }                                                       //@EIA
                                    else                                                    //@EIA
                                    {                                                       //@EIA
                                        if( parameterNulls_[i] )                            //@EIA
                                            indicatorValue = INDICATOR_NULL;                //@EIA
                                        else if( parameterDefaults_[i] )                    //@EIA
                                            indicatorValue = INDICATOR_DEFAULT;             //@EIA
                                        else if ( parameterUnassigned_[i] )                 //@EIA
                                            indicatorValue = INDICATOR_UNASSIGNED;          //@EIA
                                    }                                                       //@EIA
                                    parameterMarkerData.setIndicator(rowLoop, i, indicatorValue);    // @G1a @G9C @EIC
                                    byte[] parameterData = parameterMarkerData.getRawBytes();           // @G1a
                                    int parameterDataOffset = rowDataOffset + parameterOffsets_[i];   // @G1a
                                    int parameterDataLength = parameterLengths_[i] + parameterDataOffset;
                                    for(int z=parameterDataOffset; z < parameterDataLength; parameterData[z++] = 0x00);
                                }
                                else
                                {
                                    parameterMarkerData.setIndicator(rowLoop, i, (short) 0);     // @G9C
                                    ConvTable ccsidConverter = connection_.getConverter (parameterRow_.getCCSID (i+1)); //@P0C

                                    // Convert the data to bytes into the parameter marker data.    // @BAA
                                    // If there is an exception here, it means that there were      // @BAA
                                    // not enough bytes in the descriptor for the conversion.       // @BAA
                                    // If so, we get the correct length via getPrecision()          // @BAA
                                    // (assume the SQLData implementation has updated its own       // @BAA
                                    // precision as needed).                                        // @BAA
                                    int correctLength = -1;                                         // @BAA

                                    // Put each row's values back into the SQL data type for their respective columns.
                                    if(batchExecute_)
                                    {
                                        //@CRS If the type is a locator, we pass -1 here so the locator will know
                                        // not to reset its length, because the length wasn't saved with the
                                        // parameter when addBatch() was called, but since we reuse the SQLData
                                        // objects, it's still saved off inside the SQLLocator.
                                        setValue(i+1, parameters[i], null, -1);
                                    }

                                    SQLData sqlData = parameterRow_.getSQLType(i+1);                        // @BAC @P0C @G9C

                                    try
                                    {
                                        //@CRS - This is the only place convertToRawBytes is ever called.
                                        sqlData.convertToRawBytes(parameterMarkerData.getRawBytes(), rowDataOffset + parameterOffsets_[i], ccsidConverter);
                                        if(ccsidConverter.getCcsid() == 5035) //@trnc this is not caught at setX() time
                                            testDataTruncation(i+1, sqlData); //@trnc
                                    }
                                    catch(SQLException e)
                                    {
                                        if(e.getSQLState().trim().equals("HY000"))      //AN INTERNAL DRIVER ERROR
                                        {
                                            //Check error to see if it was thrown from another error
                                            if(e.getMessage().indexOf("Change Descriptor") != -1){
                                                correctLength = sqlData.getPrecision();                     // @BAA
                                            }
                                            else
                                                throw e;
                                        }
                                        else throw e;
                                    }                                                               // @BAA

                                    // If the length needed is larger than what was allocated in    // @BAA
                                    // the descriptor, then change the descriptor, and start        // @BAA
                                    // again.                                                       // @BAA
                                    if(correctLength >= 0)
                                    {
                                        descriptorChangeNeeded = true;                              // @BAA
                                        parameterLengths_[i] = correctLength;                       // @BAA
                                        parameterTotalSize_ = parameterOffsets_[i] + correctLength; // @BAA
                                        if((i+1) < parameterCount_)
                                        {
                                            for(int j = i+1; j < parameterCount_; ++j)
                                            {
                                                parameterOffsets_[j] = parameterTotalSize_;         // @BAA
                                                parameterTotalSize_ += parameterLengths_[j];        // @BAA
                                            }                                                       // @BAA
                                        }                                                           // @BAA
                                    }                                                               // @BAA
                                }
                            }
                            if(descriptorChangeNeeded) changeDescriptor();
                        }
                    } while(descriptorChangeNeeded);
                    request.setParameterMarkerBlockIndicator (0);
                }

                // If we are expecting output parameters
                // to be returned, then ask for them as result data.
                if(outputParametersExpected_) request.addOperationResultBitmap(DBSQLRequestDS.ORS_BITMAP_RESULT_DATA);
            }
            catch(DBDataStreamException e)
            {
                JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
            }
        }
    }



    /**
    Performs common operations needed after a prepare.
  
    @param  sqlStatement    The SQL statement.
    @param  reply           The prepare reply.
  
    @exception      SQLException    If an error occurs.
    **/
    void commonPrepareAfter(JDSQLStatement sqlStatement, DBReplyRequestedDS reply) throws SQLException
    {
        super.commonPrepareAfter(sqlStatement, reply);

        if(prepared_)
        {
            parameterRow_ = new JDServerRow(connection_, id_, reply.getParameterMarkerFormat(), settings_);
        }
    }



    /**
    Performs common operations needed before a prepare.
  
    @param  sqlStatement    The SQL statement.
    @param  request         The prepare request.
  
    @exception      SQLException    If an error occurs.
    **/
    void commonPrepareBefore(JDSQLStatement sqlStatement, DBSQLRequestDS request) throws SQLException
    {
        super.commonPrepareBefore(sqlStatement, request);

        if(prepared_)
        {
            request.addOperationResultBitmap(DBSQLRequestDS.ORS_BITMAP_PARAMETER_MARKER_FORMAT);
        }
    }



    /**
    Performs common operations in leiu of a prepare.
  
    @param  sqlStatement    The SQL statement.
    @param  statementIndex  The cached statement index.
  
    @exception      SQLException    If an error occurs.
    **/
    void commonPrepareBypass(JDSQLStatement sqlStatement, int statementIndex) throws SQLException
    {
        super.commonPrepareBypass(sqlStatement, statementIndex);

        if(prepared_)
        {
            parameterRow_ = new JDServerRow(connection_, id_, packageManager_.getCachedParameterMarkerFormat(statementIndex), settings_);
        }
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
    public boolean execute() throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();

            if(!prepared_)
            {
                resultRow_ = commonPrepare(sqlStatement_);
                prepared_ = true;
            }

            commonExecute(sqlStatement_, resultRow_);
            executed_ = true;

            return(resultSet_ != null);
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
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_SEQUENCE);  // @B1A
        return false;                                               // @B1A
    }


    //@GAA
    /**
    Runs an SQL statement that may return multiple result sets and
    makes any auto-generated keys available for retrieval using
    Statement.getGeneratedKeys().  This closes the current result set
    and clears warnings before executing the new SQL statement.
  
    <p>Do not use this form of execute() on a prepared statement.
  
    @param  sql               The SQL statement.
    @param  autoGeneratedKeys Indicates whether auto-generated keys should be made available for
                              retrieval.  Valid values are Statement.RETURN_GENERATED_KEYS and
                              Statement.NO_GENERATED_KEYS.
    @return                   true if a result set was returned, false
                              if an update count was returned or nothing
                              was returned.
  
    @exception      SQLException    This exception is always thrown.
    @since Modification 5
    **/
    public boolean execute (String sql, int autoGeneratedKeys)
    throws SQLException
    {
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_SEQUENCE);
        return false;                                                                                              // @B1A
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
    public int[] executeBatch() throws SQLException
    {
        synchronized(internalLock_)
        {                                            // @F1A
            checkOpen();

            if(batch_ == null || batch_.size() == 0) return new int[0];

            batchParameterRows_ = new Vector();

            int batchSize = batch_.size();
            int[] updateCounts = new int[batchSize];

            int numSuccessful = 0; // Number of successfully executed statements in the batch.

            boolean canBatch = true;
            boolean notInsert = false;

            try
            {
                // Only INSERTs can be batched, UPDATE statements must still be done one at a time.
                if(!(sqlStatement_.isInsert_))
                {
                    canBatch = false;
                    notInsert = true;
                }

                if(!(sqlStatement_.canBatch()))
                {
                    canBatch = false;
                }

                // For sure we have a locator, so we can't batch it,
                // because the host server only reserves space for one locator handle.
                if(containsLocator_ == LOCATOR_FOUND)
                {
                    canBatch = false;
                }

                // Set the batch execute flag so common execute knows to use the list  @G9A
                // of parameter rows.                                                  @G9A
                batchExecute_ = true;                                               // @G9A
                rowsInserted_ = 0;                                                  // @G9A

                // Prepare the statement if it is not already done.
                if(!prepared_)
                {
                    //@H7 Native type should ONLY be BLOCK_INSERT if the statement is of type
                    //@H7 "INSERT INTO MYTABLE ? ROWS VALUES (?,?)" with a ROWS VALUES clause,
                    //@H7 not just if we are going to send the values as a batch to the system.
                    //@H7 We determine whether the statement is of that form in 
                    //@H7 JDSQLStatement.java, not here.
                    //@H7D sqlStatement_.setNativeType(JDSQLStatement.TYPE_BLOCK_INSERT);  // @G9A
                    resultRow_ = commonPrepare(sqlStatement_);
                    prepared_ = true;
                    // See if the prepare returned a ResultSet. If so, error out now to avoid
                    // opening a cursor. Note some stored procedures won't return a ResultSet
                    // until the execute, so we check in both places.
                    if(resultSet_ != null)
                    {
                        closeResultSet(JDCursor.REUSE_YES);
                        JDError.throwSQLException(this, JDError.EXC_CURSOR_STATE_INVALID);
                    }
                }

                // Execute.
                if(canBatch)
                {
                    Enumeration list = batch_.elements();
                    int count = 0;                                //@K1A   Added support for allowing more than 32000 SQL Statements to be batched and run
                    while (list.hasMoreElements())                
                    {
                        batchParameterRows_.add(list.nextElement());
                        count++;                                    //@K1A
                        if(count == 32000 && list.hasMoreElements())//@K1A  Checks if 32000 statements have been added to the batch, if so execute the first 32000, then continue processing the batch
                        {                                           //@K1A
                            if(JDTrace.isTraceOn()) JDTrace.logInformation(this, "Begin batching via server-side with "+batchParameterRows_.size()+" rows.");  //@K1A
                            commonExecute(sqlStatement_, resultRow_);        //@K1A
                            batchParameterRows_.clear();                     //@K1A
                            if (resultSet_ != null)                          //@K1A
                            {                                                //@K1A
                                closeResultSet(JDCursor.REUSE_YES);          //@K1A
                                JDError.throwSQLException(this, JDError.EXC_CURSOR_STATE_INVALID);  //@K1A
                            }                                                                       //@K1A
                            count = 0;                                                              //@K1A set the count for the number of statements in the batch back to zero
                        }                                                    //@K1A
                    }
                    if(JDTrace.isTraceOn()) JDTrace.logInformation(this, "Begin batching via server-side with "+batchParameterRows_.size()+" rows.");
                    commonExecute(sqlStatement_, resultRow_);
                    batchParameterRows_.clear();
                    if(resultSet_ != null)
                    {
                        closeResultSet(JDCursor.REUSE_YES);
                        JDError.throwSQLException(this, JDError.EXC_CURSOR_STATE_INVALID);
                    }
                    numSuccessful = batchSize;
                    for(int i=0; i<batchSize; ++i)
                    {
                        // The host server does not currently report the update counts for each statement in
                        // the batch.  We use -2 here because that is the constant for Statement.SUCCESS_NO_INFO
                        // as of JDBC 3.0 and JDK 1.4. When we change to build against JDK 1.4 instead of 1.3,
                        // we can change this to use the actual constant.
                        updateCounts[i] = -2;
                    }
                }
                else
                {
                    // We can't really batch because we are not an INSERT, we contain a locator, or
                    // there is some other reason.
                    Enumeration list = batch_.elements();
                    if(JDTrace.isTraceOn()) JDTrace.logInformation(this, "Begin batching via client-side multiple executes.");
                    while(list.hasMoreElements())
                    {
                        batchParameterRows_.addElement(list.nextElement());
                        commonExecute(sqlStatement_, resultRow_);
                        batchParameterRows_.removeAllElements();
                        if(resultSet_ != null)
                        {
                            closeResultSet(JDCursor.REUSE_YES);
                            JDError.throwSQLException(this, JDError.EXC_CURSOR_STATE_INVALID);
                        }
                        updateCounts[numSuccessful++] = rowsInserted_;
                    }
                }
                executed_ = true;
            }
            catch(SQLException e)
            {
                // The specification says that if we get an error,
                // then the size of the update counts array should
                // reflect the number of statements that were
                // executed without error.
                int[] counts = null;
                if(canBatch)
                {
                    //@CRS - We must be an INSERT...??  But we don't know which statement in the batch
                    // caused the exception, so all we can do is return an array of size 1 with the
                    // number of rows inserted/updated according to the host server.
                    //   After some investigation, the above is not true.  See below:                               //@550
                    //  If autocommit is on and we are running under *NONE, then rowsInserted_ contains the number  //@550
                    //  of inserts that executed successfully before the error.  rowsInserted_ is set from the      //@550
                    //  the value in SQLERRD3.  If autocommit is running under an isolation level other than *NONE, //@550 
                    //  or autocommit is off, no rows are committed.  Thus rowsInserted_ will be zero.               //@550
                    //  Since we don't have any update counts for each statement, use Statement.SUCCESS_NO_INFO     //@550
                    //@550D counts = new int[] { rowsInserted_};
                    counts = new int[rowsInserted_];                                                                //@550 batch update support
                    for(int i=0; i<counts.length; i++)                                                              //@550
                        counts[i] = Statement.SUCCESS_NO_INFO;                                                                             //@550  
                    
                }
                else
                {
                    //@CRS - Since we haven't really been batching, we've been keeping track of everything
                    // per execute, so we can return more useful information.
                    counts = new int[numSuccessful];
                    System.arraycopy(updateCounts, 0, counts, 0, numSuccessful);
                }
                throw new BatchUpdateException(e.getMessage(), e.getSQLState(), e.getErrorCode(), counts);
            }
            finally
            {
                batch_.removeAllElements();
                batchExecute_ = false;                                          //@K1A
                if(JDTrace.isTraceOn()) JDTrace.logInformation(this, "Done batching.");
            }
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
    public ResultSet executeQuery() throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();

            if(!prepared_)
            {
                resultRow_ = commonPrepare(sqlStatement_);
                prepared_ = true;
            }

            commonExecute(sqlStatement_, resultRow_);
            executed_ = true;

            if(resultSet_ == null && ((behaviorOverride_ & 1) == 0))
            {
                JDError.throwSQLException(this, JDError.EXC_CURSOR_STATE_INVALID);
            }

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
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_SEQUENCE);  // @B1A
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
    public int executeUpdate() throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen();

            // Prepare and execute.  Check for a result set in both
            // places.  It is best to catch it after the prepare (so
            // we don't open a cursor), but with some stored procedures,
            // we can't catch it until the execute.

            // Prepare the statement if it is not already done.
            if(!prepared_)
            {
                resultRow_ = commonPrepare(sqlStatement_);
                prepared_ = true;
                if(resultRow_ != null) JDError.throwSQLException(this, JDError.EXC_CURSOR_STATE_INVALID);
            }

            // Execute.
            commonExecute(sqlStatement_, resultRow_);
            executed_ = true;

            if(resultSet_ != null)
            {
                closeResultSet(JDCursor.REUSE_YES);
                JDError.throwSQLException(this, JDError.EXC_CURSOR_STATE_INVALID);
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
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_SEQUENCE);  // @B1A
        return 0;                                                   // @B1A
    }



    //@GAA
    /**
    Runs an SQL INSERT, UPDATE, or DELETE statement, or any
    SQL statement that does not return a result set and
    makes any auto-generated keys available for retrieval using
    Statement.getGeneratedKeys().
    This closes the current result set and clears warnings
    before executing the new SQL statement.
  
    <p>Do not use this form of executeUpdate() on a prepared statement.
  
    @param  sql     The SQL statement.
    @return         Either the row count for INSERT, UPDATE, or
                    DELETE, or 0 for SQL statements that
                    return nothing.
  
    @exception      SQLException    This exception is always thrown.
    @since Modification 5
    **/
    public int executeUpdate (String sql, int autoGeneratedKeys)
    throws SQLException
    {
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_SEQUENCE);
        return 0;
    }



    // JDBC 2.0
    /**
    Returns the ResultSetMetaData object that describes the
    result set's columns.  Null is returned if the statement
    does not return a result set.  In the following example
    rsmd is null since the statement does not return a result set. 
    <PRE> 
    PreparedStatement ps   = connection.prepareStatement("INSERT INTO COLLECTION.TABLE VALUES(?)");
    ResultSetMetaData rsmd = ps.getMetaData();
    </PRE>
  
    @return     The metadata object, or null if the statement does not return a result set.
  
    @exception  SQLException    If the statement is not open.
    **/
    public ResultSetMetaData getMetaData ()
    throws SQLException
    {
        synchronized(internalLock_)
        {                                            // @F1A
            checkOpen ();

            if(resultRow_ == null)                                              // @H6a
                return null;                                                      // @H6a

            ConvTable convTable = null;                                                                    // @G6A
            DBExtendedColumnDescriptors extendedDescriptors = getExtendedColumnDescriptors();              // @G6A
            // If we have extendedDescriptors, send a ConvTable to convert them, else pass null            // @G6A
            if(extendedDescriptors != null)                                                               // @G6A
            {
                // @G6A
                convTable = ((AS400JDBCConnection)connection_).converter_;                                // @G6A
            }                                                                                              // @G6A
            return new AS400JDBCResultSetMetaData (connection_.getCatalog (),
                                                   resultSetConcurrency_, cursor_.getName (), resultRow_,
                                                   extendedDescriptors, convTable, connection_);  //@in1                       // @G6A
        }
    }



    // @G4A
    // Return the class name of a parameter for ParameterMetaData support.
    String getParameterClassName (int param)
    throws SQLException
    {
        if(param > getParameterCount() || param < 1)
        {
            JDError.throwSQLException (this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);
        }
        synchronized(internalLock_)
        {
            checkOpen ();

            if(useReturnValueParameter_)                               // @G8a
            {
                // @G8a
                if(param == 1)                                          // @G8a
                    return returnValueParameter_.getJavaClassName();      // @G8a
                else                                                     // @G8a
                    param--;                                              // @G8a
            }                                                           // @G8a

            return parameterRow_.getSQLData(param).getJavaClassName();
        }
    }



    // @G4A
    // Return the parameter count for ParameterMetaData support.
    int getParameterCount ()
    throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen ();
            if(useReturnValueParameter_)
            {
                return parameterCount_ + 1;
            }
            return parameterCount_;
        }
    }



    // @G4A JDBC 3.0
    /**
    Returns the number, types, and properties of a PreparedStatement
    object's parameters.
  
    @return     The ParameterMetaData object that describes this
    prepared statement object.
  
    @exception  SQLException    If the statement is not open.
    @since Modification 5
    **/
    public ParameterMetaData getParameterMetaData()
    throws SQLException
    {
        synchronized(internalLock_)
        {
            checkOpen ();
            return(ParameterMetaData)(Object) new AS400JDBCParameterMetaData (this);
        }
    }



    // @G4A
    // Return the mode of a parameter for ParameterMetaData support.
    int getParameterMode (int param)
    throws SQLException
    {
        if(param > getParameterCount() || param < 1)
        {
            JDError.throwSQLException (this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);
        }
        synchronized(internalLock_)
        {
            checkOpen ();

            if(useReturnValueParameter_)                               // @G8a
            {
                // @G8a
                if(param == 1)                                          // @G8a
                    return ParameterMetaData.parameterModeOut;            // @G8a
                else                                                     // @G8a
                    param--;                                              // @G8a
            }                                                           // @G8a

            boolean input = parameterRow_.isInput(param);
            boolean output = parameterRow_.isOutput(param);

            if(input && output)
            {
                return ParameterMetaData.parameterModeInOut;
            }
            else if(input)
            {
                return ParameterMetaData.parameterModeIn;
            }
            else if(output)
            {
                return ParameterMetaData.parameterModeOut;
            }
            else
                return ParameterMetaData.parameterModeUnknown;
        }
    }



    // @G4A
    // Return the type of a parameter for ParameterMetaData support.
    int getParameterType (int param)
    throws SQLException
    {
        if(param > getParameterCount() || param < 1)
        {
            JDError.throwSQLException (this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);
        }
        synchronized(internalLock_)
        {
            checkOpen ();

            if(useReturnValueParameter_)                               // @G8a
            {
                // @G8a
                if(param == 1)                                          // @G8a
                    return returnValueParameter_.getType();               // @G8a
                else                                                     // @G8a
                    param--;                                              // @G8a
            }                                                           // @G8a

            return parameterRow_.getSQLData(param).getType();
        }
    }



    // @G4A
    // Return the type name of a parameter for ParameterMetaData support.
    String getParameterTypeName (int param)
    throws SQLException
    {
        if(param > getParameterCount() || param < 1)
        {
            JDError.throwSQLException (this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);
        }
        synchronized(internalLock_)
        {
            checkOpen ();

            if(useReturnValueParameter_)                               // @G8a
            {
                // @G8a
                if(param == 1)                                          // @G8a
                    return returnValueParameter_.getTypeName();           // @G8a
                else                                                     // @G8a
                    param--;                                              // @G8a
            }                                                           // @G8a

            return parameterRow_.getSQLData(param).getTypeName();
        }
    }



    // @G4A
    // Return the precision of a parameter for ParameterMetaData support.
    int getPrecision (int param)
    throws SQLException
    {
        if(param > getParameterCount() || param < 1)
        {
            JDError.throwSQLException (this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);
        }
        synchronized(internalLock_)
        {
            checkOpen ();

            if(useReturnValueParameter_)                               // @G8a
            {
                // @G8a
                if(param == 1)                                          // @G8a
                    return returnValueParameter_.getPrecision();          // @G8a
                else                                                     // @G8a
                    param--;                                              // @G8a
            }                                                           // @G8a

            return parameterRow_.getSQLData(param).getPrecision();
        }
    }



    // @G4A
    // Return the scale of a parameter for ParameterMetaData support.
    int getScale (int param)
    throws SQLException
    {
        if(param > getParameterCount() || param < 1)
        {
            JDError.throwSQLException (this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);
        }
        synchronized(internalLock_)
        {
            checkOpen ();

            if(useReturnValueParameter_)                               // @G8a
            {
                // @G8a
                if(param == 1)                                          // @G8a
                    return returnValueParameter_.getScale();              // @G8a
                else                                                     // @G8a
                    param--;                                              // @G8a
            }                                                           // @G8a

            return parameterRow_.getSQLData(param).getScale();
        }
    }



    // @G4A
    // Return whether a parameter is nullable for ParameterMetaData support.
    int isNullable (int param)
    throws SQLException
    {
        if(param > getParameterCount() || param < 1)
        {
            JDError.throwSQLException (this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);
        }
        synchronized(internalLock_)
        {
            checkOpen ();

            if(useReturnValueParameter_)                               // @G8a
            {
                // @G8a
                if(param == 1)                                          // @G8a
                    return ResultSetMetaData.columnNoNulls;               // @G8a
                else                                                     // @G8a
                    param--;                                              // @G8a
            }                                                           // @G8a

            return parameterRow_.isNullable(param);
        }
    }



    // @G4A
    // Return whether a parameter is signed for ParameterMetaData support.
    boolean isSigned (int param)
    throws SQLException
    {
        if(param > getParameterCount() || param < 1)
        {
            JDError.throwSQLException (this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);
        }
        synchronized(internalLock_)
        {
            checkOpen ();

            if(useReturnValueParameter_)                               // @G8a
            {
                // @G8a
                if(param == 1)                                          // @G8a
                    return returnValueParameter_.isSigned();              // @G8a
                else                                                     // @G8a
                    param--;                                              // @G8a
            }                                                           // @G8a

            return parameterRow_.getSQLData(param).isSigned();
        }
    }



    // JDBC 2.0
    /**
    Sets an input parameter to an Array value.  DB2 for IBM i
    does not support arrays.
  
    @param  parameterIndex  The parameter index (1-based).
    @param  parameterValue  The parameter value.
  
    @exception  SQLException    Always thrown because DB2 for IBM i does not support arrays.
    **/
    public void setArray (int parameterIndex, Array parameterValue)
    throws SQLException
    {
        JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);
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
        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setAsciiStream()");             // @H1A
            if(parameterValue == null)                                  // @H1A
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");  // @H1A
            else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " length: " + length); // @H1A
        }                                                                  // @H1A

        // Validate the length parameter
        if(length < 0)
            JDError.throwSQLException (this, JDError.EXC_BUFFER_LENGTH_INVALID);

        // @J0A added the code from setValue in this method because streams and readers are handled specially
        synchronized(internalLock_)
        {
            checkOpen ();

            // Validate the parameter index.
            if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                JDError.throwSQLException (this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

            // Check if the parameter index refers to the return value parameter.
            // This is an OUT parameter, so sets are not allowed.  If its not
            // parameter index 1, then decrement the parameter index, since we
            // are "faking" the return value parameter.
            if(useReturnValueParameter_)
            {
                if(parameterIndex == 1)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);
                else
                    --parameterIndex;
            }

            // Check that the parameter is an input parameter.
            if(! parameterRow_.isInput (parameterIndex))
                JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);

            // Set the parameter data.  If there is a type mismatch,
            // set() with throw an exception.
            SQLData sqlData = parameterRow_.getSQLType(parameterIndex);
            if(parameterValue != null)
            {

                try
                {
                    // If the data is a locator, then set its handle.
                    if((sqlData.getSQLType() == SQLData.CLOB_LOCATOR ||
                        sqlData.getSQLType() == SQLData.BLOB_LOCATOR ||
                        sqlData.getSQLType() == SQLData.DBCLOB_LOCATOR))
                    {
                        SQLLocator sqlDataAsLocator = (SQLLocator) sqlData;
                        sqlDataAsLocator.setHandle(parameterRow_.getFieldLOBLocatorHandle(parameterIndex));
                        if(JDTrace.isTraceOn()) JDTrace.logInformation(this, "locator handle: " + parameterRow_.getFieldLOBLocatorHandle(parameterIndex));
                        sqlData.set(new ConvTableReader(parameterValue, 819, 0, LOB_BLOCK_SIZE), null, length); // @J0M hacked this to use the scale parm for the length
                    }
                    else
                    {
                        sqlData.set (JDUtilities.readerToString(new ConvTableReader(parameterValue, 819, 0, LOB_BLOCK_SIZE), length), null, -1);
                    }
                }
                catch(UnsupportedEncodingException uee)
                {
                    /* do nothing */
                }

                testDataTruncation (parameterIndex, sqlData);
            }
            // Parameters can be null; you can call one of the set methods to null out a
            // field of the database.
            parameterNulls_[parameterIndex-1] = (parameterValue == null);
            parameterDefaults_[parameterIndex-1] = false;   //@EIA
            parameterUnassigned_[parameterIndex-1] = false; //@EIA
            parameterSet_[parameterIndex-1] = true;

        }

        //@J0M setValue (parameterIndex,
        //@J0M          (parameterValue == null) ? null : JDUtilities.streamToString (parameterValue, length, "ISO8859_1"), // @B2C
        //@J0M          null, -1); //@P0C
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
        // @B2D    JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);

        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setBigDecimal()");              // @H1A
            if(parameterValue == null)                                  // @H1A
                JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: NULL");  // @H1A
            else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: " + parameterValue.toString());  // @H1A
        }                                                                  // @H1A

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
    public void setBinaryStream(int parameterIndex, InputStream parameterValue, int length) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setBinaryStream()");            // @H1A
            if(parameterValue == null)                                  // @H1A
                JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: NULL");  // @H1A
            else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " length: " + length);  // @H1A
        }                                                                  // @H1A

        // Validate the length parameter
        if(length < 0)
            JDError.throwSQLException (this, JDError.EXC_BUFFER_LENGTH_INVALID);

        // @J0A added the code from setValue in this method because streams and readers are handled specially
            synchronized(internalLock_)         //@KKC Removed comment brace
            {
              checkOpen ();
        
              // Validate the parameter index.
              if ((parameterIndex < 1) || (parameterIndex > parameterCount_))
                JDError.throwSQLException (this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);
        
              // Check if the parameter index refers to the return value parameter.
              // This is an OUT parameter, so sets are not allowed.  If it's not
              // parameter index 1, then decrement the parameter index, since we
              // are "faking" the return value parameter.
              if (useReturnValueParameter_)
              {
                if (parameterIndex == 1)
                  JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);
                else
                  --parameterIndex;
              }
        
              // Check that the parameter is an input parameter.
              if (! parameterRow_.isInput (parameterIndex))
                JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);
        
              // Set the parameter data.  If there is a type mismatch,
              // set() will throw an exception.
              SQLData sqlData = parameterRow_.getSQLType(parameterIndex);
              if (parameterValue != null)
              {
                // If the data is a locator, then set its handle.
                if (sqlData instanceof SQLLocator)
                {
                  SQLLocator sqlDataAsLocator = (SQLLocator) sqlData;
                  sqlDataAsLocator.setHandle (parameterRow_.getFieldLOBLocatorHandle (parameterIndex));
                  sqlData.set (parameterValue, null, length); // @J0M hacked this to use the scale parm for the length
                }
                else
                {
                    sqlData.set (JDUtilities.streamToBytes(parameterValue, length), null, length);
                }
        
                testDataTruncation (parameterIndex, sqlData);
              }
              // Parameters can be null; you can call one of the set methods to null out a
              // field of the database.
              parameterNulls_[parameterIndex-1] = (parameterValue == null);
              parameterDefaults_[parameterIndex-1] = false;   //@EIA
              parameterUnassigned_[parameterIndex-1] = false; //@EIA
              parameterSet_[parameterIndex-1] = true;
        
            }
        //@KKC */
        //@KKC setValue(parameterIndex, parameterValue, null, length);
        //@J0D setValue (parameterIndex,
        //@J0D           (parameterValue == null) ? null : JDUtilities.streamToBytes (parameterValue, length), // @B2C
        //@J0D           null, -1);
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
        // @B2D     JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);

        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setBlob()");                    // @H1A
            if(parameterValue == null)                                  // @H1A
                JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: NULL");  // @H1A
            else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " length: " + parameterValue.length());   // @H1A
        }                                                                  // @H1A

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
    // The spec defines this in terms of SQL BIT, but DB2 for IBM i
    // does not support that.
    //
    public void setBoolean (int parameterIndex, boolean parameterValue)
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setBoolean()");                 // @H1A
            JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: " + parameterValue);  // @H1A
        }                                                                  // @H1A

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
    // The spec defines this in terms of SQL TINYINT, but DB2 for IBM i
    // does not support that.
    //
    public void setByte (int parameterIndex, byte parameterValue)
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setByte()");                    // @H1A
            JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: " + parameterValue);  // @H1A
        }                                                                  // @H1A

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
        // @B2D     JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);

        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setBytes()");                   // @H1A
            if(parameterValue == null)                                  // @H1A
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");  // @H1A
            else if(parameterValue.length > maxToLog_)
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " length: " + parameterValue.length);  // @H1A
            else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " length: " + parameterValue.length + " value: " + new String(parameterValue) ); // @H1A  //@PDC
        }                                                                  // @H1A

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
    @param  length          The number of characters to read from the reader.
  
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
        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setCharacterStream()");         // @H1A
            if(parameterValue == null)                                  // @H1A
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");  // @H1A
            else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " length: " + length); // @H1A
        }                                                                  // @H1A

        // Validate length parameter
        if(length < 0)
            JDError.throwSQLException (this, JDError.EXC_BUFFER_LENGTH_INVALID);

        // @J0A added the code from setValue in this method because streams and readers are handled specially
        synchronized(internalLock_)
        {
            checkOpen ();

            // Validate the parameter index.
            if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                JDError.throwSQLException (this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

            // Check if the parameter index refers to the return value parameter.
            // This is an OUT parameter, so sets are not allowed.  If its not
            // parameter index 1, then decrement the parameter index, since we
            // are "faking" the return value parameter.
            if(useReturnValueParameter_)
            {
                if(parameterIndex == 1)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);
                else
                    --parameterIndex;
            }

            // Check that the parameter is an input parameter.
            if(! parameterRow_.isInput (parameterIndex))
                JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);

            // Set the parameter data.  If there is a type mismatch,
            // set() with throw an exception.
            SQLData sqlData = parameterRow_.getSQLType(parameterIndex);
            if(parameterValue != null)
            {

                // If the data is a locator, then set its handle.
                if((sqlData.getSQLType() == SQLData.CLOB_LOCATOR ||
                    sqlData.getSQLType() == SQLData.BLOB_LOCATOR ||
                    sqlData.getSQLType() == SQLData.DBCLOB_LOCATOR))
                {
                    SQLLocator sqlDataAsLocator = (SQLLocator) sqlData;
                    sqlDataAsLocator.setHandle(parameterRow_.getFieldLOBLocatorHandle(parameterIndex));
                    if(JDTrace.isTraceOn()) JDTrace.logInformation(this, "locator handle: " + parameterRow_.getFieldLOBLocatorHandle(parameterIndex));
                    //@pddsqlData.set(parameterValue, null, length); // @J0M hacked this to use the scale parameter for the length
                    sqlData.set(JDUtilities.readerToString(parameterValue, length), null, -1); //@pdc length is incorrect for double-byte chars.  Use a slower, but correct method, until we can create a real ConvTableReader
                }
                else
                {
                    sqlData.set(JDUtilities.readerToString(parameterValue, length), null, -1);
                }

                testDataTruncation (parameterIndex, sqlData);
            }
            // Parameters can be null; you can call one of the set methods to null out a
            // field of the database.
            parameterNulls_[parameterIndex-1] = (parameterValue == null);
            parameterDefaults_[parameterIndex-1] = false;   //@EIA
            parameterUnassigned_[parameterIndex-1] = false; //@EIA
            parameterSet_[parameterIndex-1] = true;

        }

        // @J0D setValue (parameterIndex,
        // @J0D          (parameterValue == null) ? null : JDUtilities.readerToString (parameterValue, length), // @B2C
        // @J0D          null, -1); //@P0C
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
        // @B2D     JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);

        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setClob()");                    // @H1A
            if(parameterValue == null)                                  // @H1A
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");  // @H1A
            else if(parameterValue.length() > maxToLog_)                   // @H1A
                JDTrace.logInformation (this, "parameter index: "  + parameterIndex + " value: "  + parameterValue.getSubString(1, (int)parameterValue.length())); // @H1A
            else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " length: " + parameterValue.length()); // @H1A
        }                                                                  // @H1A

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
        // @B2D     JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);

        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setDate()");                    // @H1A
            if(parameterValue == null)                                  // @H1A
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");  // @H1A
            else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: " + parameterValue.toString()); // @H1A
        }                                                                  // @H1A

        setValue (parameterIndex, parameterValue, null, -1); //@P0C
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
        // @B2D     JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);
        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setDate()");                    // @H1A
            if(parameterValue == null)                                  // @H1A
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");  // @H1A
            else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: " + parameterValue.toString()); // @H1A
        }                                                                  // @H1A

        if(calendar == null)
            JDError.throwSQLException (this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        setValue (parameterIndex, parameterValue, calendar, -1);
    }

    //@EIA 550 extended indicator defaults
    /**
    Sets an input parameter to the default value
    @param  parameterIndex  The parameter index (1-based).
    @exception  SQLException    If the statement is not open,
                                the index is not valid, the parameter
                                is not an input parameter.
    **/
    public void setDB2Default(int parameterIndex) throws SQLException
    {
    	 if(JDTrace.isTraceOn())
         {                                         
             JDTrace.logInformation (this, "setDB2Default()");            
             JDTrace.logInformation (this, "parameter index: " + parameterIndex);
         }                                                                 

         setValueExtendedIndicator(parameterIndex, 1); //1 is default
         
    }
    
    //@EIA 550 extended indicator defaults
    /**
    Sets an input parameter to unassigned
    @param  parameterIndex  The parameter index (1-based).
    @exception  SQLException    If the statement is not open,
                                the index is not valid, the parameter
                                is not an input parameter.
    **/
    public void setDB2Unassigned(int parameterIndex) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         
            JDTrace.logInformation (this, "setDB2Unassigned()");            
            JDTrace.logInformation (this, "parameter index: " + parameterIndex);
        }                                                                 

        setValueExtendedIndicator(parameterIndex, 2); //2 is unassigned
    	
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
        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setDouble()");                  // @H1A
            JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: " + parameterValue);  // @H1A
        }                                                                  // @H1A

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
        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setFloat()");                   // @H1A
            JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: " + parameterValue);  // @H1A
        }                                                                  // @H1A

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
        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setInt()");                     // @H1A
            JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: " + parameterValue);  // @H1A
        }                                                                  // @H1A

        setValue (parameterIndex, new Integer (parameterValue), null, -1);
    }



    // @D0C
    /**
    Sets an input parameter to a Java long value.
    If the connected system supports SQL BIGINT data, the driver
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
    // The spec defines this in terms of SQL BIGINT, but DB2 for IBM i
    // does not support that until V4R5.
    //
    public void setLong (int parameterIndex, long parameterValue)
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setLong()");                    // @H1A
            JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: " + parameterValue);  // @H1A
        }                                                                  // @H1A

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
        // @BBD     JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);

        // @D8 ignore the type supplied by the user.  We are checking it
        // only to rigidly follow the JDBC spec.  Ignoring the type
        // will make us a friendlier driver.
        //
        // @D8d testSQLType(sqlType, parameterIndex);                                               // @BBA

        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setNull()");                    // @H1A
            JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: NULL");  // @H1A
        }                                                                  // @H1A

        setValue (parameterIndex, null, null, -1);
    }


    // @B4 - Added for JDK 2.0RC1 - typeName can be ignored, since it is not relevant to IBM i.
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
        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setNull()");                    // @H1A
            JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: NULL");  // @H1A
        }                                                                  // @H1A

        setNull (parameterIndex, sqlType);
    }

    /**
    Sets an input parameter to an Object value.  The driver converts
    this to a value of an SQL type, depending on the type of the
    specified value.  The JDBC specification defines a standard
    mapping from Java types to SQL types.  In the cases where a
    SQL type is not supported by DB2 for IBM i, the
    <a href="doc-files/SQLTypes.html#unsupported">next closest matching type</a>
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
        // @B2D     JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);

        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setObject()");                  // @H1A
            if(parameterValue == null)                                  // @H1A
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");  // @H1A
            else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " type: " + parameterValue.getClass().getName()); // @H1A
        }                                                                  // @H1A

        setValue (parameterIndex, parameterValue, null, -1); //@P0C
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
        // @B2D     JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);

        // @BBD if (sqlType != parameterRow_.getSQLData (parameterIndex).getType ())
        // @BBD     JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);

        // @D8 ignore the type supplied by the user.  We are checking it
        // only to rigidly follow the JDBC spec.  Ignoring the type
        // will make us a friendlier driver.
        //
        // @D8d testSQLType(sqlType, parameterIndex);                                               // @BBA

        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setObject()");                  // @H1A
            if(parameterValue == null)                                  // @H1A
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");  // @H1A
            else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " type: " + parameterValue.getClass().getName()); // @H1A
        }                                                                  // @H1A

        setValue (parameterIndex, parameterValue, null, -1); //@P0C
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
        // @B2D     JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);

        // @BBD if (sqlType != parameterRow_.getSQLData (parameterIndex).getType ())
        // @BBD     JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);

        // @D8 ignore the type supplied by the user.  We are checking it
        // only to rigidly follow the JDBC spec.  Ignoring the type
        // will make us a friendlier driver.
        //
        // @D8d testSQLType(sqlType, parameterIndex);                                               // @BBA

        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setObject()");                  // @H1A
            if(parameterValue == null)                                  // @H1A
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");  // @H1A
            else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " type: " + parameterValue.getClass().getName()); // @H1A
        }                                                                  // @H1A

        if(scale < 0)
            JDError.throwSQLException (this, JDError.EXC_SCALE_INVALID);

        setValue (parameterIndex, parameterValue, null, scale); //@P0C
    }



    // JDBC 2.0
    /**
    Sets an input parameter to a Ref value.  DB2 for IBM i
    does not support structured types.
  
    @param  parameterIndex  The parameter index (1-based).
    @param  parameterValue  The parameter value.
  
    @exception  SQLException    Always thrown because DB2 for IBM i does not support structured types.
    **/
    public void setRef (int parameterIndex, Ref parameterValue)
    throws SQLException
    {
        JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);
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
        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setShort()");                   // @H1A
            JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: " + parameterValue);  // @H1A
        }                                                                  // @H1A

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
        // @B2D     JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);

        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setString()");                  // @H1A
            if(parameterValue == null)                                  // @H1A
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");  // @H1A
            else if(parameterValue.length() > maxToLog_)                // @H1A
                JDTrace.logInformation (this, "parameter index: " + parameterIndex + " length: " + parameterValue.length());  // @H1A
            else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: " + parameterValue);  // @H1A
        }                                                                  // @H1A

        setValue (parameterIndex, parameterValue, null, -1); // @B7C @P0C
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
        // @B2D     JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);

        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setTime()");                    // @H1A
            if(parameterValue == null)                                  // @H1A
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");  // @H1A
            else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: " + parameterValue.toString()); // @H1A
        }                                                                  // @H1A

        setValue (parameterIndex, parameterValue, null, -1); //@P0C
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
        // @B2D     JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);
        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setTime()");                    // @H1
            if(parameterValue == null)                                  // @H1A
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");  // @H1A
            else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: " + parameterValue.toString()); // @H1A
        }                                                                  // @H1A

        if(calendar == null)
            JDError.throwSQLException (this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);

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
        // @B2D     JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);

        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setTimeStamp()");               // @H1A
            if(parameterValue == null)                                  // @H1A
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");  // @H1A
            else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: " + parameterValue.toString()); // @H1A
        }                                                                  // @H1A

        setValue (parameterIndex, parameterValue, null, -1); //@P0C
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
        // @B2D     JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);
        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setTimeStamp()");               // @H1A
            if(parameterValue == null)                                  // @H1A
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");  // @H1A
            else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: " + parameterValue.toString()); // @H1A
        }                                                                  // @H1A

        if(calendar == null)
            JDError.throwSQLException (this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);

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
        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setUnicodeStream()");           // @H1A
            if(parameterValue == null)                                  // @H1A
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");  // @H1A
            else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " length: " + length); // @H1A
        }                                                                  // @H1A

        // Validate the length parameter
        if(length < 0)
            JDError.throwSQLException (this, JDError.EXC_BUFFER_LENGTH_INVALID);

        // @J0A added the code from setValue in this method because streams and readers are handled specially
        synchronized(internalLock_)
        {
            checkOpen ();

            // Validate the parameter index.
            if((parameterIndex < 1) || (parameterIndex > parameterCount_))
                JDError.throwSQLException (this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);

            // Check if the parameter index refers to the return value parameter.
            // This is an OUT parameter, so sets are not allowed.  If its not
            // parameter index 1, then decrement the parameter index, since we
            // are "faking" the return value parameter.
            if(useReturnValueParameter_)
            {
                if(parameterIndex == 1)
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);
                else
                    --parameterIndex;
            }

            // Check that the parameter is an input parameter.
            if(! parameterRow_.isInput (parameterIndex))
                JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);

            // Set the parameter data.  If there is a type mismatch,
            // set() with throw an exception.
            SQLData sqlData = parameterRow_.getSQLType(parameterIndex);
            if(parameterValue != null)
            {

                try
                {
                    // If the data is a locator, then set its handle.
                    if((sqlData.getSQLType() == SQLData.CLOB_LOCATOR ||
                        sqlData.getSQLType() == SQLData.BLOB_LOCATOR ||
                        sqlData.getSQLType() == SQLData.DBCLOB_LOCATOR))
                    {
                        SQLLocator sqlDataAsLocator = (SQLLocator) sqlData;
                        sqlDataAsLocator.setHandle(parameterRow_.getFieldLOBLocatorHandle(parameterIndex));
                        if(JDTrace.isTraceOn()) JDTrace.logInformation(this, "locator handle: " + parameterRow_.getFieldLOBLocatorHandle(parameterIndex));
                        sqlData.set(new ConvTableReader(parameterValue, 13488, 0, LOB_BLOCK_SIZE), null, length/2); // @J0M hacked this to use the scale parm for the length
                    }
                    else
                    {
                        sqlData.set (JDUtilities.readerToString(new ConvTableReader(parameterValue, 13488, 0, LOB_BLOCK_SIZE), length/2), null, -1);
                    }
                }
                catch(UnsupportedEncodingException uee)
                {
                    /* do nothing */
                }

                testDataTruncation (parameterIndex, sqlData);
            }
            // Parameters can be null; you can call one of the set methods to null out a
            // field of the database.
            parameterNulls_[parameterIndex-1] = (parameterValue == null);
            parameterDefaults_[parameterIndex-1] = false;   //@EIA
            parameterUnassigned_[parameterIndex-1] = false; //@EIA
            parameterSet_[parameterIndex-1] = true;

        }

        //@J0D setValue (parameterIndex,
        //@J0D          (parameterValue == null) ? null : JDUtilities.streamToString (parameterValue, length, "UnicodeBig"), // @B2C @B3C @H2C @H3C
        //@J0D          null, -1); //@P0C
    }



    // @G4A JDBC 3.0
    /**
    Sets an input parameter to a URL value.  The driver converts this to an
    SQL DATALINK value.
  
    @param  parameterIndex  The parameter index (1-based).
    @param  parameterValue  The parameter value or null to set
                        the value to SQL NULL.
  
    @exception  SQLException    If the statement is not open,
                           the index is not valid, or the parameter
                           is not an input parameter.
    @since Modification 5
    **/
    public void setURL (int parameterIndex, URL parameterValue)
    throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                         // @H1A
            JDTrace.logInformation (this, "setURL()");                     // @H1A
            if(parameterValue == null)                                  // @H1A
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");  // @H1A
            else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: " + parameterValue.toString()); // @H1A
        }                                                                  // @H1A

        setValue (parameterIndex, parameterValue, null, -1);
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
    void setValue(int parameterIndex, Object parameterValue, Calendar calendar, int scale) throws SQLException
    {
        
        synchronized(internalLock_)
        {                                            // @F1A
            checkOpen();

            // Check if the parameter index refers to the return value parameter.          @F2A
            // This is an OUT parameter, so sets are not allowed.  If its not              @F2A
            // parameter index 1, then decrement the parameter index, since we             @F2A
            // are "faking" the return value parameter.                                    @F2A
            if(useReturnValueParameter_)
            {                                             // @F2A
                if(parameterIndex == 1)                                                // @F2A
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);// @F2A
                else                                                                    // @F2A
                    --parameterIndex;                                                   // @F2A
            }

            // Validate the parameter index.
            if((parameterIndex < 1) || (parameterIndex > parameterCount_))
            {
                JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);
            }

            // Check that the parameter is an input parameter.
            if(!parameterRow_.isInput(parameterIndex)) JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

            // Set the parameter data.  If there is a type mismatch,
            // set() with throw an exception.
            SQLData sqlData = parameterRow_.getSQLType(parameterIndex); //@P0C
            if(parameterValue != null)
            {                                                                   // @B6C
                // If the data is a locator, then set its handle.                                              @B6A
                int sqlType = sqlData.getSQLType();
                if((sqlType == SQLData.CLOB_LOCATOR ||
                    sqlType == SQLData.BLOB_LOCATOR ||
                    sqlType == SQLData.DBCLOB_LOCATOR))
                {                                                        // @B6A
                    SQLLocator sqlDataAsLocator = (SQLLocator) sqlData;                                     // @B6A
                    sqlDataAsLocator.setHandle(parameterRow_.getFieldLOBLocatorHandle(parameterIndex));   // @B6A
                    if(JDTrace.isTraceOn()) JDTrace.logInformation(this, "locator handle: " + parameterRow_.getFieldLOBLocatorHandle(parameterIndex));
                }                                                                                           // @B6A

                sqlData.set(parameterValue, calendar, scale);
                if (dataTruncation_ || !sqlData.isText())
                {
                  testDataTruncation(parameterIndex, sqlData);       // @B5C @G5move
                }
            }
            // Parameters can be null; you can call one of the set methods to null out a
            // field of the database.                                                                                            // @B6A
            parameterNulls_[parameterIndex-1] = (parameterValue == null);
            parameterDefaults_[parameterIndex-1] = false;    //@EIA 
            parameterUnassigned_[parameterIndex-1] = false;  //@EIA 
            parameterSet_[parameterIndex-1] = true;
        }
    }

    //@EIA new method
    /**
    Sets an input parameter value for the specified index,
    and performs all appropriate validation when the value is one of the
    valid Extended Indicator values: default or unassigned.
    
    Note: this is the same type of method as setValue() above, but we
    have no way to pass in the special values without hacking some sort
    of flag string for the value, and that seemed to be a messy and slow
    way to do this.
  
    @param  parameterIndex  The parameter index (1-based).
    @param  parameterValue  The parameter 1="default" or 2="unassigned".
  
    @exception  SQLException    If the statement is not open,
                                the index is not valid or
                                the parameter is not an input
                                parameter.
    **/
    void setValueExtendedIndicator(int parameterIndex, int parameterValue) throws SQLException
    {
        synchronized(internalLock_)
        {                                          
            checkOpen();

            // Check if the parameter index refers to the return value parameter.          
            // This is an OUT parameter, so sets are not allowed.  If its not              
            // parameter index 1, then decrement the parameter index, since we             
            // are "faking" the return value parameter.                                   
            if(useReturnValueParameter_)
            {                                             
                if(parameterIndex == 1)                                                 
                    JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID); 
                else                                                                   
                    --parameterIndex;                                                   
            }

            // Validate the parameter index.
            if((parameterIndex < 1) || (parameterIndex > parameterCount_))
            {
                JDError.throwSQLException(this, JDError.EXC_DESCRIPTOR_INDEX_INVALID);
            }

            // Check that the parameter is an input parameter.
            if(!parameterRow_.isInput(parameterIndex)) JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);

            parameterNulls_[parameterIndex-1] = false;
            parameterDefaults_[parameterIndex-1] = parameterValue == 1 ? true: false;     
            parameterUnassigned_[parameterIndex-1] =  parameterValue == 2 ? true: false;   
            parameterSet_[parameterIndex-1] = true;
        }
    }


    /**
    Tests if a DataTruncation occurred on the write of a piece of
    data and throws a DataTruncation exception if so.  The data truncation
    flag is also taken into consideration for string data.  The rules are:
       1) If updating database with numeric data and data truncated, throw exception
       2) If numeric data is part of a query and data truncated, post warning
       3) If string data and suppress truncation, return
       4) If updating database with string data and check truncation and data truncated, throw exception
       5) If string data is part of a query and check truncation and data truncated, post warning
  
    @param  index   The index (1-based).
    @param  data    The data that was written or null for SQL NULL.
    **/
    private void testDataTruncation(int parameterIndex, SQLData data) throws SQLException //@trunc
    {
        if(data != null && (dataTruncation_ || !data.isText()))
        {
            // The SQLData object determined if data was truncated as part of the setValue() processing.
            int truncated = data.getTruncated ();
            if(truncated > 0)
            {                
                int actualSize = data.getActualSize ();
                //boolean isRead = sqlStatement_.isSelect(); //@pda jdbc40 //@pdc same as native (only select is read) //@trunc //@pdc match native
                DataTruncation dt = new DataTruncation(parameterIndex, true, false, actualSize + truncated, actualSize); //@pdc jdbc40 //@trunc //@pdc match native

                //if 610 and number data type, then throw DataTruncation
                //if text, then use old code path and post/throw DataTruncation
                if((connection_.getVRM() >= JDUtilities.vrm610) && (data.isText() == false))   //@trunc2
                {                                                                    //@trunc2
                    throw dt;                                                        //@trunc2
                }                                                                    //@trunc2
                else if((sqlStatement_ != null) && (sqlStatement_.isSelect()) && (!sqlStatement_.isSelectFromInsert()))       //@trunc2 //@selins1
                {
                    postWarning(dt);
                }
                else
                {
                    throw dt;
                }
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
        int parameterType = parameterRow_.getSQLType(parameterIndex).getType(); //@P0C

        if(sqlType != parameterType)
        {

            // If the only reason the types don't match is because one
            // is a CHAR and the other is a VARCHAR, then let this
            // slide.
            if(((sqlType == Types.CHAR) || (sqlType == Types.VARCHAR))
               && ((parameterType == Types.CHAR) || (parameterType == Types.VARCHAR)))
                ; // Do nothing!
            else
                JDError.throwSQLException (this, JDError.EXC_PARAMETER_TYPE_INVALID);
        }
    }

    //@GKA
    // Returns the JDServerRow object associated with this statement.
    JDServerRow getResultRow()
    {
        return resultRow_;
    }

}
