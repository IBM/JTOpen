///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCPreparedStatement.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream;
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
/* ifdef JDBC40 */
import java.sql.NClob;
/* endif */ 
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
/* ifdef JDBC40 */
import java.sql.RowId;
/* endif */ 
import java.sql.SQLException;
/* ifdef JDBC40 */
import java.sql.SQLXML;
/* endif */ 
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Enumeration;
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

<p>For method that sets parameters, the application should not modify the parameter
   value until after the execute completes.  Modifying a value
   between the setXXXX method and the execute method may result in unpredictable
   behavior. 
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
    static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";


    private boolean             dataTruncation_;        // @B5A
    private int                 descriptorHandle_;
    boolean             executed_;              // private protected
    private boolean             outputParametersExpected_;
    int                 parameterCount_;        // private protected
    int                 parameterInputCount_;        // private protected //@array4
    boolean             batchExecute_;          // private protected            @G9A
    private boolean executingBatchedStatement_ = false;  // Flag to prevent clearParameters from causing execute exception @DAA
    private int[]               parameterLengths_;
    private int[]               parameterOffsets_;
    private boolean[]           parameterNulls_;
    private boolean[]           parameterDefaults_;    //@EIA
    private boolean[]           parameterUnassigned_;  //@EIA
    private String[]            parameterNames_; //@pda jdbc40
    //@re-prep move to statement JDServerRow    JDServerRow         parameterRow_;          // private protected
    Vector              batchParameterRows_;    // private protected            @G9A
    private int                 parameterTotalSize_;
    private int                 indicatorTotalSize_; //@array Used with array containing data only.  Is total size of all indicators (including array element indicators)
    private int                 headerTotalSize_; //@array Used to calculate size of stream header
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
    private static boolean isjvm16SynchronizerStatic;//@dmy
    static {
    	// Changed 2/21/2011 to not use unless the JDBC.jvm16Synchronize property is true.  @C6A
    	
 /*  
   Here is some information from service about this error. 

Yes, this trace code was added for a very ugly issue that showed up when customers started moving to Java 6. 
While trying to debug it, we found that the trace points ended up changing the behavior, so they were 
altered to trace to a dummy stream so that it would workaround Sun's bug.  
The CPS discussion item was 7LXN87. 

Here's the contents of our KB doc on the issue:

Abstract	
A problem with the Sun HotSpot Server in the 1.6 JDK causes a variety of errors.

Problem Summary:
A problem was introduced into the version 1.6 JDK (Java Development Kit) and 
JRE (Java Runtime Environment) from Sun.  The problem was introduced somewhere between
 update number 7 and update 12, which can cause a number of problems.  Java version 1.6.0_7 works; 
however, version 1.6.0_12 produces the errors.  The problem is specific to the HotSpot Server which is 
something like an optimizing compiler that is designed to provide the best operating speed for long-running 
applications similar to a Web server.  The problem seems to always manifest itself by 'removing' parameters 
that had been bound to a statement.  However, it is not possible to know that this has occurred without 
tracing the application.  The outward symptoms are exceptions which will vary depending on what data is 
missing.  The common errors that have been reported are as follows:

SQLException: Descriptor index not valid
CPD4374  -  Field HVR000n and value N not compatible.    <-- where N might a variety of different numbers
SQL0302  -  Conversion error on host variable or parameter *N.  <-- where n might a variety of different numbers
SQL0406  -  Conversion error on assignment to column N.  <-- where N might a variety of different numbers

Resolution:
The problem has been reported to Sun; however, at this time, no fix is available from them.  
We have found three ways to circumvent the problem:

1.  Do not use JDK 1.6.
2.	  Use JVM property -client (this turns off performance code in Sun Hotspot).
3.  Use JVM property  -XX:CompileCommand=exclude,com/ibm/as400/access/AS400JDBCPreparedStatement,commonExecuteBefore (more selectively, turn off part of Hotspot).
4.	 Use the latest version of jt400.jar (currently 6.6).  Additional trace points that were added while searching for the source of the problem appear to have changed the Hotspot behavior.



Update 2/24/2011.  This was probably a problem with the buffer synchonization.  Before JTOpen 7.1, a flag 
was set to indicate that a buffer was available.  This flag did not utilize any synchronization.  In JTOpen 7.1, 
the buffer management code was restructure to used synchronzation. 

A recreate for the original problem was found.  It failed using the JTOpen 6.4 jar.  We then used a jar
with the change the set the default isjvm16SynchronizerStatic to false and set the default
value of the property to false.  The problem did not occur with the jar file. 
*/ 
    	
    	
    	
        //Temporary fix for jvm 1.6 memroy stomp issue. (remove //@dmy code when jvm issue is resolved)
        //This fix will just trace a few extra traces to a dummy stream
        //if system property or jdbc property is set to false then extra trace is not executed
        //null value for system property means not specified...so true by default
        String jvm16Synchronize = SystemProperties.getProperty (SystemProperties.JDBC_JVM16_SYNCHRONIZE); //@dmy
        isjvm16SynchronizerStatic = false;  //@dmy //false by default  @C6C
        if((jvm16Synchronize != null) && (Boolean.valueOf(jvm16Synchronize.trim()).booleanValue() == true)) {
            try{                                                    //@dmy
                Class.forName("java.sql.SQLXML");                   //@dmy
                isjvm16SynchronizerStatic = true;                        //@dmy
            }catch(Exception e){                                    //@dmy
                isjvm16SynchronizerStatic = false;                        //@dmy
            }        	
        
        } else { 
        		   //@dmy    
            isjvm16SynchronizerStatic = false;  //@dmy
        }
        
    }
    // @C6C  -- Changed to remove the dummy PrimWriter.  The dummy PrintWriter uses a 
    // 16k buffer of storage.  This causes storage problems when a lot of statements are 
    // cached. Instead we'll use the write(byte[]) method instead of the buffered print writer
    //                
    //@dmy private dummy outputstream
    OutputStream dummyOutputStream = new OutputStream() {
        int b1 = 0;
        public synchronized void write(int b) throws IOException {  b1 = b; }
        	
    };
    
    // Any method that can deal with extremely large data values must be prepared
    // to deal with them in blocks instead of as one giant unit.  This value is
    // used to determine the size of each block.  Eventually, we might externalize
    // this value so that users can set it as they see fit.
    static final int LOB_BLOCK_SIZE = 1000000; //@pdc Match Native JDBC Driver for IBM i

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

                                                   //@dmy
        //Temporary fix for jvm 1.6 memroy stomp issue. (remove //@dmy code when jvm issue is resolved)
        //This fix will just trace a few extra traces to a dummy stream
        //if system property or jdbc property is set to false then extra trace is not executed
        //null value for system property means not specified...so true by default
        isjvm16Synchronizer = isjvm16SynchronizerStatic; 
        if( connection_.getProperties().getBoolean(JDProperties.JVM16_SYNCHRONIZE))    //@dmy    
            isjvm16Synchronizer = true;  //@dmy@C6C

        
        batchExecute_               = false;                                        // @G9A
        outputParametersExpected_   = outputParametersExpected;
        parameterCount_             = sqlStatement.countParameters();
        parameterInputCount_        = 0;  //@array4 calculate while we prepare
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
        	JDTrace.logInformation (this, "isjvm16Synchronizer="+isjvm16Synchronizer);  // @C6A
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
                        sqlType == SQLData.DBCLOB_LOCATOR ||        //@KBA  //@pdc jdbc40
/* ifdef JDBC40 */
                        sqlType == SQLData.NCLOB_LOCATOR ||                 //@pda jdbc40
/* endif */ 
                        sqlType == SQLData.XML_LOCATOR)                     //@xml3
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
            
            if(isjvm16Synchronizer) {
            	try { 
                dummyOutputStream.write(("!!!changeDescriptor:  totalParameterLength_ = " + parameterTotalSize_).getBytes());  //@dmy@C6C
            	} catch (Exception e) { 
            		
            	}
            } 
            
            for(int i = 0; i < parameterCount_; ++i)
            {
                SQLData sqlData = parameterRow_.getSQLData (i+1);

                parameterMarkerDataFormat.setFieldDescriptionLength (i);
                if(sqlData.getNativeType() == SQLData.NATIVE_ARRAY)    //@array
                {                                                      //@array
                    int arrayLen =  ((SQLArray)sqlData).getArrayCount();                              //@array
                    if(arrayLen > 0)                                                                  //@array
                        parameterMarkerDataFormat.setFieldLength (i, parameterLengths_[i]/arrayLen);  //@array
                    else                                                                              //@array
                        parameterMarkerDataFormat.setFieldLength (i, parameterLengths_[i]);           //@array
                }                                                                                     //@array
                else                                                                                  //@array
                {
                    parameterMarkerDataFormat.setFieldLength (i, parameterLengths_[i]);
                }
                parameterMarkerDataFormat.setFieldCCSID (i, parameterRow_.getCCSID (i+1));

                parameterMarkerDataFormat.setFieldNameLength (i, 0);
                parameterMarkerDataFormat.setFieldNameCCSID (i, 0);
                parameterMarkerDataFormat.setFieldName (i, "", connection_.converter_); //@P0C

                //@array (arrays sent in as the element type and zda will know they are arrays)
                if(sqlData.getNativeType() == SQLData.NATIVE_ARRAY)    //@array
                {                                                      //@array
                    parameterMarkerDataFormat.setFieldSQLType (i,
                                                       (short) (((SQLArray)sqlData).getElementNativeType() | 0x0001));  //@array
                }                                                       //@array
                else
                {
                    parameterMarkerDataFormat.setFieldSQLType (i,
                                                               (short) (sqlData.getNativeType() | 0x0001));
                }
                
                parameterMarkerDataFormat.setFieldScale (i,
                                                         (short) sqlData.getScale());
                parameterMarkerDataFormat.setFieldPrecision (i,
                                                             (short) sqlData.getPrecision());
                if(isjvm16Synchronizer) {
                	try { 
                    dummyOutputStream.write(("!!!changeDescriptor:  Parameter " + (i+1) + " length = " + parameterLengths_[i]).getBytes()); //@C6C
                	} catch (Exception e) { 
                		
                	}
                }
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
            	if (request2 != null) { 
            		try { 
                dummyOutputStream.write(("!!!changeDescriptor.inUser_(false): request2-id=" +  request2.hashCode()).getBytes()); //@C6C
            		} catch (Exception e) {}; 
            	}
            }
            if(request2 != null) { request2.returnToPool(); request2= null; } //@P0C
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
                    if(isjvm16Synchronizer) {
                    	try { 
                        dummyOutputStream.write(("!!!close.inUser_(false): request-id=" +  request.hashCode()).getBytes()); // @C6C
                    	} catch (Exception e) { 
                    		
                    	}
                    }
                    if(request != null) { request.returnToPool();  request = null; } //@P0C
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

        // Validate each parameters. If a parameter is not an
        // input parameter, then it is okay for it not to have been
        // set. However, if an input parameter was not set,
        // we throw an exception.
        boolean outputExpected_ = false; // @K2A We do not want to increment our
                                         // row index in commonExecuteAfter() if
                                         // there are no output parameters
      for (int i = 0; i < parameterCount_; ++i) {

        // We don't need to validate the parameters if executing a batched
        // statement.
        // The parameter were validated during addBatch.
        // If we attempt to validate, this will fail when
        // clearParameters is called immediately before executeBatch().
        // Problem reported via CPS 8KLGCZ August 2011 @DAA
        if (!executingBatchedStatement_) {

          if (!parameterSet_[i] && parameterRow_.isInput(i + 1)) {
            JDError.throwSQLException(this,
                JDError.EXC_PARAMETER_COUNT_MISMATCH);
          }
        }
        if (parameterRow_.isOutput(i + 1)) // @K2A
          outputExpected_ = true; // @K2A
        if (parameterRow_.isInput(i + 1)) // @array4
          parameterInputCount_++; // @array4
      }
      
        if (!outputExpected_) // @K2A
          outputParametersExpected_ = false; // @K2A
      
            // Create the descriptor if needed.  This should only
            // be done once (on the first execute for the prepared
            // statement).
            if((parameterCount_ > 0) && (descriptorHandle_ == 0))
            {
                // Get the offset and length for each parameter.
                // We just use the information that came in the parameter
                // marker format from reply for the prepare.
                parameterTotalSize_ = 0;
                indicatorTotalSize_ = 0;   //@array
                headerTotalSize_ = 2; //@array start with 2 since column count is 2 bytes 
                for(int i = 0; i < parameterCount_; ++i)
                {       
                    if(!parameterRow_.containsArray_ || parameterRow_.isInput(i+1)) //@array4
                    {
                        SQLData sqlData = parameterRow_.getSQLData(i+1);    //@array
                        int arrayLen = 1;  //@array 1 by default so size can be multiplied for non arrays also
                        // boolean arrayIndicatorSet = false; //@array
                        if(sqlData.getType() == java.sql.Types.ARRAY)       //@array
                        {
                            arrayLen = ((SQLArray)sqlData).getArrayCount();    //@array
                            if (parameterNulls_[i] || parameterDefaults_[i] || parameterUnassigned_[i])  //@array
                                headerTotalSize_ += 4; //@array space for x9911ffff 
                            else
                                headerTotalSize_ += 12;  //@array (array column requires 12 bytes in header x9911) //@array2
                        }
                        else
                        {
                            //non array value
                            headerTotalSize_ += 8;  //@array (assuming row has array.  x9912 is length 8)
                        }
                        //@array set input (to host) array lengths of data
                        //@array if null array or 0 length array, then data length is 0
                        parameterLengths_[i] = parameterRow_.getLength (i+1) * arrayLen;  //@array 0, 1, or more datatype-length blocks 
                        parameterOffsets_[i] = parameterTotalSize_;
                        parameterTotalSize_ += parameterLengths_[i];

                        indicatorTotalSize_ += (arrayLen*2);//@array
                    }
                    
                    if(isjvm16Synchronizer) {
                    try {
                        dummyOutputStream.write(("!!!commonExecuteBefore:  Parameter " + (i+1) + " length = " + parameterLengths_[i] ).getBytes()); //@C6C
						
					} catch (Exception e) {
					}	
                    }
                }
                if(isjvm16Synchronizer) { 
                    try {
                    dummyOutputStream.write(("!!!commonExecuteBefore:  totalParameterLength_ = " + parameterTotalSize_).getBytes());  //@C6C
					} catch (Exception e) {
					}
                }
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
                        //@array create new x382f here if parms contain array
                        if(parameterRow_.containsArray_)  //@array
                        {                                 //@array
                            parameterMarkerData = new DBVariableData(parameterInputCount_, 2, headerTotalSize_, indicatorTotalSize_, parameterTotalSize_); //@array x382f codepoint //@array4
                        }                                 //@array
                        else if(connection_.useExtendedFormats ())
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
                                    
                                    SQLData sqlData = parameterRow_.getSQLType(i+1);                   //@array
 
                                    //@array Don't set indicator here for null array, since setting header below will set it
                                    if(sqlData.getType() != java.sql.Types.ARRAY)                   
                                        parameterMarkerData.setIndicator(rowLoop, i, indicatorValue);    // @G1a @G9C @EIC
                                    
                                    //@array only zero-out data on non-arrays
                                    //If the whole array is null, then we do not even include blank data in the stream since a null array has space for values (just 0X9911ffff in header of 0X382f)
                                    if(sqlData.getType() != java.sql.Types.ARRAY)  //@array
                                    {
                                        byte[] parameterData = parameterMarkerData.getRawBytes();           // @G1a
                                        int parameterDataOffset = rowDataOffset + parameterOffsets_[i];   // @G1a
                                        int parameterDataLength = parameterLengths_[i] + parameterDataOffset;
                                        for(int z=parameterDataOffset; z < parameterDataLength; parameterData[z++] = 0x00);
                                    }
                                    
                                    //@array If the row contains an array, then we must also set the columnInfo in stream header
                                    if(parameterRow_.containsArray_ && parameterRow_.isInput(i+1)) //@array //@array4
                                    {                                                         //@array
                                        int arrayLen = -1;                                   //@array
                                        int elementType = -1;                                //@array
                                        int size = -1;                                       //@array
                                        if(sqlData.getType() == java.sql.Types.ARRAY)        //@array
                                        {                                                    //@array
                                            arrayLen = ((SQLArray)sqlData).getArrayCount();  //@array
                                            elementType = ((SQLArray)sqlData).getElementNativeType(); //@array
                                            size = parameterRow_.getLength(i+1);             //@array  
                                        }                                                       //@array
                                        ((DBVariableData)parameterMarkerData).setHeaderColumnInfo(i, (short)sqlData.getNativeType(), (short)indicatorValue, (short)elementType, size, (short)arrayLen); //@array
                                    }                                                        //@array
                                }
                                else
                                {
                                    SQLData sqlData = parameterRow_.getSQLType(i+1);                   //@array
                                    if(!parameterRow_.containsArray_ || parameterRow_.isInput(i+1)) //@array4
                                    {
                                        //Setting array null value here for elements inside of array)
                                        if(sqlData.getType() == java.sql.Types.ARRAY )   //@array
                                        {                                                                  //@array 
                                            //iterate through elements and set null indicators.  Array as a whole null is not set here (see above)
                                            for (int e = 0 ; e < ((SQLArray)sqlData).getArrayCount() ; e++) //@array 
                                            {                                                        //@array 
                                                if(((SQLArray)sqlData).isElementNull(e))             //@array 
                                                    parameterMarkerData.setIndicator(0, i, -1);      //@array 
                                                else                                                 //@array 
                                                    parameterMarkerData.setIndicator(0, i, 0);       //@array 
                                            }                                                        //@array 
                                        }else
                                        {
                                            parameterMarkerData.setIndicator(rowLoop, i, (short) 0);     // @G9C
                                        }
                                    }
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

                                    //SQLData sqlData = parameterRow_.getSQLType(i+1);                        // @BAC @P0C @G9C //@array move above

                                    try
                                    {
                                        if(!parameterRow_.containsArray_ || parameterRow_.isInput(i+1)) //@array4 (if array then only send input parm data)
                                        { 
                                            //@CRS - This is the only place convertToRawBytes is ever called.
                                            sqlData.convertToRawBytes(parameterMarkerData.getRawBytes(), rowDataOffset + parameterOffsets_[i], ccsidConverter);
                                            if(ccsidConverter.getCcsid() == 5035) //@trnc this is not caught at setX() time
                                                testDataTruncation(i+1, sqlData); //@trnc
                                        }
                                        
                                        //@array If the row contains an array, then we must also set the columnInfo in stream header
                                        if(parameterRow_.containsArray_ && parameterRow_.isInput(i+1)) //@array //@array4
                                        {                                                         //@array
                                            //Set the stream header info for each column in addition to data in rawbytes above.
                                            int arrayLen = -1;                                   //@array
                                            int elementType = -1;                                //@array
                                            int size = parameterRow_.getLength(i+1);             //@array
                                            if(sqlData.getType() == java.sql.Types.ARRAY)        //@array
                                            {                                                    //@array
                                                arrayLen = ((SQLArray)sqlData).getArrayCount();  //@array
                                                elementType = ((SQLArray)sqlData).getElementNativeType(); //@array
                                            }                                                    //@array
                                            ((DBVariableData)parameterMarkerData).setHeaderColumnInfo(i, (short)sqlData.getNativeType(), (short)0, (short)elementType, size, (short)arrayLen); //@array
                                        }                                                        //@array
                                        
                                    }
                                    catch(SQLException e)
                                    {
                                        if(e.getSQLState().trim().equals("HY000"))      //AN INTERNAL DRIVER ERROR
                                        {
                                            //Check error to see if it was thrown from another error
                                            if(parameterRow_.containsArray_) //@array always use prepare/describe lengths
                                                throw e;                     //@array
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
            //boolean notInsert = false; //@blksql

            try
            {
                // Only INSERTs can be batched, UPDATE statements must still be done one at a time.
                //if(!(sqlStatement_.isInsert_)) //@blksql
                //{
                //    canBatch = false;
                //    notInsert = true;
                //}

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
                	int maximumBlockedInputRows = connection_.getMaximumBlockedInputRows(); 
                    Enumeration list = batch_.elements();
                    int count = 0;                                //@K1A   Added support for allowing more than 32000 SQL Statements to be batched and run
                    int totalUpdateCount = 0;  /* @A4A*/ 
                    while (list.hasMoreElements())                
                    {
                        batchParameterRows_.add(list.nextElement());
                        count++;                                    //@K1A
                        if(count == maximumBlockedInputRows && list.hasMoreElements())//@K1A  Checks if 32000 statements have been added to the batch, if so execute the first 32000, then continue processing the batch
                        {                                           //@K1A
                            if(JDTrace.isTraceOn()) JDTrace.logInformation(this, "Begin batching via server-side with "+batchParameterRows_.size()+" rows.");  //@K1A
                            executingBatchedStatement_ = true;   /*@DAA*/ 
                            commonExecute(sqlStatement_, resultRow_);        //@K1A
                            executingBatchedStatement_ = false; /*@DAA*/
                            totalUpdateCount += updateCount_;    /* @A4A*/
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
                    
                    //
                    // There is a quirk that if clearParameters is called after addBatch but before executeBatch then
                    // the commonExecute fails because it doesn't think the parameters are set.
                    // Set a flag that we are doing server side batching. 
                    //
                    executingBatchedStatement_ = true; /*@DAA*/
                    commonExecute(sqlStatement_, resultRow_);
                    executingBatchedStatement_ = false; /*@DAA*/
                    
                    totalUpdateCount += updateCount_;      /* @A4A*/
                    batchParameterRows_.clear();
                    if(resultSet_ != null)
                    {
                        closeResultSet(JDCursor.REUSE_YES);
                        JDError.throwSQLException(this, JDError.EXC_CURSOR_STATE_INVALID);
                    }
                    numSuccessful = batchSize;
                    // The host server does not currently report the update counts for each statement in
                    // the batch.  We use -2 here because that is the constant for Statement.SUCCESS_NO_INFO
                    // as of JDBC 3.0 and JDK 1.4. When we change to build against JDK 1.4 instead of 1.3,
                    // we can change this to use the actual constant.
                    // However, if the total number of updated rows is the same as the batch size then
                    // we can set each of the update counts to 1.    @A4A
                    
                    // Only set the count to one if the statement is an insert statement.  
                    // The logic in JDSQLStatement only allows in insert to be batched if it is of the 
                    // form insert ... VALUES(?,?,?) ... Any other form will not be batched  
                    int updateCount = -2; 
                    if ( batchSize == totalUpdateCount && sqlStatement_.isInsert_) {
                    	updateCount = 1; 
                    }
                    for(int i=0; i<batchSize; ++i)
                    {
                        updateCounts[i] = updateCount;
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
                        // Indicate we are batching to prevent clearParameter /*@DAA*/
                        executingBatchedStatement_ = true; 
                        commonExecute(sqlStatement_, resultRow_);
                        executingBatchedStatement_ = false;  /*@DAA*/
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
                BatchUpdateException batchUpdateException = new BatchUpdateException(e.getMessage(), e.getSQLState(), e.getErrorCode(), counts);
                // Attempt to set the cause, ignoring any failures (i.e. in Pre JDK 1.4) /*@DAA*/
                try {
                  batchUpdateException.initCause(e); 
                }catch (Exception e2) {} 
                
                throw batchUpdateException; 
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
    only supports arrays in stored procedures.
  
    @param  parameterIndex  The parameter index (1-based).
    @param  parameterValue  The parameter value.
  
    @exception  SQLException    Always thrown because DB2 for IBM i does not support arrays.
    **/
    public void setArray (int parameterIndex, Array parameterValue)
    throws SQLException
    {
        //@array new support
        if(JDTrace.isTraceOn())
        {
            JDTrace.logInformation (this, "setArray()");         
            if(parameterValue == null)  
                JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: NULL"); 
            else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: Array type " + parameterValue.getBaseTypeName()); 
        }

        if(!sqlStatement_.isProcedureCall())                                     //@array
            JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID); //@array

        setValue (parameterIndex, parameterValue, null, -1);
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
                    int sqlType = sqlData.getSQLType();  //@xml3
                    if(sqlType == SQLData.CLOB_LOCATOR ||
                       sqlType == SQLData.BLOB_LOCATOR ||
                       sqlType == SQLData.DBCLOB_LOCATOR ||                 //@pdc jdbc40
/* ifdef JDBC40 */
                       sqlType == SQLData.NCLOB_LOCATOR ||                  //@pda jdbc40
/* endif */ 
                       sqlType == SQLData.XML_LOCATOR)                      //@xml3
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
    
    <br>If a parameter is set using setBinaryStream, then the parameter  
        must be reset prior to the second execute of the PreparedStatement object.  
  
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
                  // Don't convert immediately to Bytes.  This causes memory problems with Large lobs @B3A
	              //  sqlData.set (JDUtilities.streamToBytes(parameterValue, length), null, length);//@set1 allow setX one time and reuse execute() without having to reset stream
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
                int sqlType = sqlData.getSQLType();  //@xml3
                if(sqlType == SQLData.CLOB_LOCATOR ||
                   sqlType == SQLData.BLOB_LOCATOR ||
                   sqlType == SQLData.DBCLOB_LOCATOR ||                 //@pdc jdbc40
                   sqlType == SQLData.NCLOB_LOCATOR ||                  //@pda jdbc40
                   sqlType == SQLData.XML_LOCATOR)                      //@xml3
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
    Sets an input parameter to the default value.  This is a the same as setDB2Default.
    @param  parameterIndex  The parameter index (1-based).
    @exception  SQLException    If the statement is not open,
                                the index is not valid, the parameter
                                is not an input parameter.
    **/
    public void setDBDefault(int parameterIndex) throws SQLException
    {
        setDB2Default(parameterIndex);         
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

    
    //@EIA 550 extended indicator defaults
    /**
    Sets an input parameter to unassigned.  This is a the same as setDB2Unassigned.
    @param  parameterIndex  The parameter index (1-based).
    @exception  SQLException    If the statement is not open,
                                the index is not valid, the parameter
                                is not an input parameter.
    **/
    public void setDBUnassigned(int parameterIndex) throws SQLException
    {
        setDB2Unassigned(parameterIndex); //2 is unassigned   
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
    //        nothing is symmetrical and certain INOUT
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
/* ifdef JDBC40 */
        if (parameterValue instanceof SQLXML)                   //@xmlspec
            setSQLXML(parameterIndex, (SQLXML)parameterValue);  //@xmlspec
        else
/* endif */ 

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
        //if(parameterIndex <= parameterCount_ && parameterIndex > 0) //@pdc
        //parameterValue = AS400BidiTransform.convertDataToHostCCSID(parameterValue, connection_,		//Bidi-HCG
        //		parameterRow_.getCCSID (parameterIndex));											//Bidi-HCG 
                     
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
                    int sqlType = sqlData.getSQLType();  //@xml3
                    if(sqlType == SQLData.CLOB_LOCATOR ||
                       sqlType == SQLData.BLOB_LOCATOR ||
                       sqlType == SQLData.DBCLOB_LOCATOR ||                 //@pdc jdbc40
/* ifdef JDBC40 */
                       sqlType == SQLData.NCLOB_LOCATOR ||                  //@pda jdbc40
/* endif */ 
                       sqlType == SQLData.XML_LOCATOR)                      //@xml3
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
                    sqlType == SQLData.DBCLOB_LOCATOR ||                 //@pdc jdbc40
/* ifdef JDBC40 */
                    sqlType == SQLData.NCLOB_LOCATOR ||                   //@pda jdbc40
/* endif */ 
                    sqlType == SQLData.XML_LOCATOR))                      //@xml3
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
     void testSQLType(int sqlType, int parameterIndex)
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

   
    //@PDA jdbc40
    /**
     * Sets the designated parameter to the given <code>java.sql.RowId</code> object. The
     * driver converts this to a SQL <code>ROWID</code> value when it sends it
     * to the database
     *
     * @param parameterIndex 
     * @param x the parameter value
     * @throws SQLException if a database access error occurs
     *
     */
/* ifdef JDBC40 */
    public void setRowId(int parameterIndex, RowId x) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {              
            JDTrace.logInformation (this, "setRowId()");                  
            if(x == null)
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");
            else
                JDTrace.logInformation (this, "parameter index: "  + parameterIndex + " value: "  + x.toString());
        }                                                                

        setValue (parameterIndex, x, null, -1);
    }
/* endif */ 
    //@PDA jdbc40
    /**
     * Sets the designated paramter to the given <code>String</code> object.
     * The driver converts this to a SQL <code>NCHAR</code> or
     * <code>NVARCHAR</code> or <code>LONGNVARCHAR</code> value
     * (depending on the argument's
     * size relative to the driver's limits on <code>NVARCHAR</code> values)
     * when it sends it to the database.
     *
     * @param parameterIndex
     * @param value the parameter value
     * @throws SQLException if the driver does not support national
     *         character sets;  if the driver can detect that a data conversion
     *  error could occur ; or if a database access error occurs
     */
     public void setNString(int parameterIndex, String value) throws SQLException
     {
         if(JDTrace.isTraceOn())
         {                                       
             JDTrace.logInformation (this, "setNString()"); 
             if(value == null) 
                 JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL"); 
             else if(value.length() > maxToLog_) 
                 JDTrace.logInformation (this, "parameter index: " + parameterIndex + " length: " + value.length()); 
             else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: " + value);
         }
         setString(parameterIndex, value);
     }

     //@PDA jdbc40
    /**
     * Sets the designated parameter to a <code>Reader</code> object. The
     * <code>Reader</code> reads the data till end-of-file is reached. The
     * driver does the necessary conversion from Java character format to
     * the national character set in the database.
     * @param parameterIndex
     * @param value the parameter value
     * @param length the number of characters in the parameter data.
     * @throws SQLException if the driver does not support national
     *         character sets;  if the driver can detect that a data conversion
     *  error could occur ; or if a database access error occurs
     */
     public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException
     {          
         if(JDTrace.isTraceOn())
         { 
             JDTrace.logInformation (this, "setNCharacterStream()"); 
             if(value == null)   
                 JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");
             else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " length: " + length);
         }              
         setCharacterStream(parameterIndex, value, (int) length); 
     }

     //@PDA jdbc40
    /**
     * Sets the designated parameter to a <code>java.sql.NClob</code> object. The driver converts this to a
     * SQL <code>NCLOB</code> value when it sends it to the database.
     * @param parameterIndex
     * @param value the parameter value
     * @throws SQLException if the driver does not support national
     *         character sets;  if the driver can detect that a data conversion
     *  error could occur ; or if a database access error occurs
     */
/* ifdef JDBC40 */
     public void setNClob(int parameterIndex, NClob value) throws SQLException
     {

         if(JDTrace.isTraceOn())
         {  
             JDTrace.logInformation (this, "setNClob()"); 
             if(value == null) 
                 JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");  
             else if(value.length() > maxToLog_) 
                 JDTrace.logInformation (this, "parameter index: "  + parameterIndex + " value: "  + value.getSubString(1, (int)value.length())); 
             else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " length: " + value.length()); 
         }         
         setClob(parameterIndex, value);
     }
/* endif */ 
     
     //@PDA jdbc40
    /**
     * Sets the designated parameter to a <code>Reader</code> object.  The reader must contain  the number
     * of characters specified by length otherwise a <code>SQLException</code> will be
     * generated when the <code>PreparedStatement</code> is executed.
     * @param parameterIndex
     * @param reader An object that contains the data to set the parameter value to.
     * @param length the number of characters in the parameter data.
     * @throws SQLException if parameterIndex does not correspond to a parameter
     * marker in the SQL statement, or if the length specified is less than zero.
     *
     */
     public void setClob(int parameterIndex, Reader reader, long length) throws SQLException
     {
         if(JDTrace.isTraceOn())
         {     
             JDTrace.logInformation (this, "setClob()"); 
             if(reader == null)   
                 JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL"); 
             else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " length: " + length); 
         } 

         setCharacterStream(parameterIndex, reader, (int)length);
     }

     //@PDA jdbc40
    /**
     * Sets the designated parameter to an <code>InputStream</code> object.  The inputStream must contain  the number
     * of characters specified by length otherwise a <code>SQLException</code> will be
     * generated when the <code>PreparedStatement</code> is executed.
     * @param parameterIndex
     * @param inputStream An object that contains the data to set the parameter
     * value to.
     * @param length the number of bytes in the parameter data.
     * @throws SQLException if parameterIndex does not correspond
     * to a parameter marker in the SQL statement,  if the length specified
     * is less than zero or if the number of bytes in the inputstream does not match
     * the specfied length.
     *
     */
     public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException
     {
         if(JDTrace.isTraceOn())
         { 
             JDTrace.logInformation (this, "setBlob()");  
             if(inputStream == null) 
                 JDTrace.logInformation (this, "parameter index: " + parameterIndex + " value: NULL"); 
             else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " length: " + length); 
         }     
         setBinaryStream(parameterIndex, inputStream, (int)length);
     }
     
     //@PDA jdbc40
    /**
     * Sets the designated parameter to a <code>Reader</code> object.  The reader must contain  the number
     * of characters specified by length otherwise a <code>SQLException</code> will be
     * generated when the <code>PreparedStatement</code> is executed.
     * @param parameterIndex
     * @param reader An object that contains the data to set the parameter value to.
     * @param length the number of characters in the parameter data.
     * @throws SQLException if parameterIndex does not correspond to a parameter
     * marker in the SQL statement; if the length specified is less than zero;
     * if the driver does not support national character sets;
     * if the driver can detect that a data conversion
     *  error could occur; or if a database access error occurs
     *
     */
     public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException
     {
         if(JDTrace.isTraceOn())
         { 
             JDTrace.logInformation (this, "setNClob()");  
             if(reader == null) 
                 JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL"); 
             else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " length: " + length); 
         } 

         setCharacterStream(parameterIndex, reader, (int)length);
     }

     //@PDA jdbc40
     /**
      * Sets the designated parameter to the given <code>java.sql.SQLXML</code> object. 
      * @param parameterIndex
      * @param xmlObject a <code>SQLXML</code> object that maps an SQL <code>XML</code> value
      * @throws SQLException if a database access error occurs
      */
/* ifdef JDBC40 */
     public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException
     {
         if(JDTrace.isTraceOn())
         {              
             int len;  
            
             if(xmlObject == null)
                 len = 0;
             else 
                 len = xmlObject.getString().length();  //no length() method yet in jdbc.
                     
             JDTrace.logInformation (this, "setSQLXML()");                  
             if(xmlObject == null)                                   
                 JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");  
             else if(len < maxToLog_)                  
                 JDTrace.logInformation (this, "parameter index: "  + parameterIndex + " value: "  + xmlObject.getString());
             else JDTrace.logInformation (this, "parameter index: " + parameterIndex + " length: " + len); 
         }                                                                

         //@xmlspec special handling of blob/clob column types
         if(xmlObject == null)                                                      //@xmlspec3
         {                                                                          //@xmlspec3
             setValue (parameterIndex, xmlObject, null, -1);                        //@xmlspec3
             return;                                                                //@xmlspec3
         }                                                                          //@xmlspec3
         SQLData sqlData = parameterRow_.getSQLType(parameterIndex);                //@xmlspec
         int sqlDataType = sqlData.getType();                                       //@xmlspec
         switch(sqlDataType) {                                                      //@xmlspec
             case Types.CLOB:                                                       //@xmlspec
                 setCharacterStream(parameterIndex, xmlObject.getCharacterStream());//@xmlspec
                 break;                                                             //@xmlspec
             case Types.BLOB:                                                       //@xmlspec
                 setBinaryStream(parameterIndex,  xmlObject.getBinaryStream());     //@xmlspec
                 break;                                                             //@xmlspec
             default:                                                               //@xmlspec
                 setValue (parameterIndex, xmlObject, null, -1);
         }
     }
/* endif */ 
    

    //@pda jdbc40
    protected String[] getValidWrappedList()
    {
        return new String[] { "com.ibm.as400.access.AS400JDBCPreparedStatement", "java.sql.PreparedStatement" };
    } 
    
    
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
     * @param parameterIndex
     * @param x the Java input stream that contains the ASCII parameter value
     * @param length the number of bytes in the stream 
     * @exception SQLException if a database access error occurs or 
     * this method is called on a closed <code>PreparedStatement</code>
     */
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException
    {
        setAsciiStream( parameterIndex, x, (int)length);
    }

    //@PDA jdbc40
    /**
     * Sets the designated parameter to the given input stream, which will have 
     * the specified number of bytes.
     * When a very large binary value is input to a <code>LONGVARBINARY</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code> object. The data will be read from the 
     * stream as needed until end-of-file is reached.
     * 
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     *
     * @param parameterIndex
     * @param x the java input stream which contains the binary parameter value
     * @param length the number of bytes in the stream 
     * @exception SQLException if a database access error occurs or 
     * this method is called on a closed <code>PreparedStatement</code>
     */
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException
    {
        setBinaryStream(parameterIndex, x, (int)length); 
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
     * @param parameterIndex
     * @param reader the <code>java.io.Reader</code> object that contains the 
     *        Unicode data
     * @param length the number of characters in the stream 
     * @exception SQLException if a database access error occurs or 
     * this method is called on a closed <code>PreparedStatement</code>
     */
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException
    {
        setCharacterStream(parameterIndex, reader, (int)length);
    }

    //@pda jdbc40 needed for rowset.setX methods.  Moved from callableStatement.
    /*
    Find the column index that matches this parameter name.
    @param  parameterName    The parameter name to change into a column index (1-based).
    */
    int findParameterIndex(String parameterName)
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

              if(schema == null)    // No default schema was set on the connection url, or by the libraries connection property.
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
                                  + parameterCount_);
                  if(!rs.next())
                    JDError.throwSQLException(this, JDError.EXC_INTERNAL);  // didn't find the procedure in any schema

                  // If we get this far, at least one schema contains a procedure similar to ours.
                  boolean found = false;
                  for(int i=0; i<libList.length && !found; i++)
                  {
                    if (libList[i].length() != 0) {
                      rs.beforeFirst(); // re-position to before the first row
                      while(rs.next() && !found){
                        if(rs.getString(1).equals(libList[i])) {
                          schema = rs.getString(1);
                          found = true; // we found a procedure that matches our criteria
                        }
                      }
                    }
                  }
                  rs.close(); //@SS
                  s1.close(); //@SS
                  if(!found)    // none of the libraries in our library list contain a stored procedure that we are looking for
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
                if(rs != null) //@scan1
                    rs.close(); //@SS
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

    //@PDA jdbc40 move from callableStatement
    private static final String unquote(String name)
    {
      return JDUtilities.prepareForSingleQuotes(name, true);
    }
    
    //@PDA jdbc40 move from callableStatement
    private static final String unquoteNoUppercase(String name)
    {
      return JDUtilities.prepareForSingleQuotes(name, false);
    }

    //@PDA jdbc40 helper method
    private void setInputStream(int parameterIndex, InputStream x) throws SQLException
    {                                                                   
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
            if(x != null)
            {
                // If the data is a locator, then set its handle.
                int sqlType = sqlData.getSQLType();  //@xml3
                if(sqlType == SQLData.CLOB_LOCATOR ||
                   sqlType == SQLData.BLOB_LOCATOR ||
                   sqlType == SQLData.DBCLOB_LOCATOR ||                 //@pdc jdbc40
                   sqlType == SQLData.NCLOB_LOCATOR ||                  //@pda jdbc40
                   sqlType == SQLData.XML_LOCATOR)                      //@xml3
                {
                    SQLLocator sqlDataAsLocator = (SQLLocator) sqlData;
                    sqlDataAsLocator.setHandle(parameterRow_.getFieldLOBLocatorHandle(parameterIndex));
                    if(JDTrace.isTraceOn()) JDTrace.logInformation(this, "locator handle: " + parameterRow_.getFieldLOBLocatorHandle(parameterIndex));
                    sqlData.set(x, null, -2);//new ConvTableReader(x, 819, 0, LOB_BLOCK_SIZE), null, -2); //@readerlen -2 flag to read all of reader bytes
                }
                else
                {
                    sqlData.set(x, null, -2);//sqlData.set (JDUtilities.readerToString(new ConvTableReader(x, 819, 0, LOB_BLOCK_SIZE)), null, -1); //@readerlen -2 flag to read all of reader bytes
                }

                testDataTruncation (parameterIndex, sqlData);
            }
            // Parameters can be null; you can call one of the set methods to null out a
            // field of the database.
            parameterNulls_[parameterIndex-1] = (x == null);
            parameterSet_[parameterIndex-1] = true;

        }
    }
    
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
     *
     * @param parameterIndex
     * @param x the Java input stream that contains the ASCII parameter value
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * marker in the SQL statement; if a database access error occurs or 
     * this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     */
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                          
            JDTrace.logInformation (this, "setAsciiStream(int, InputStream)");        
            if(x == null)                                 
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");   
        }                                                                   
        setInputStream(parameterIndex, x);
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
     *
     * @param parameterIndex
     * @param x the java input stream which contains the binary parameter value
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * marker in the SQL statement; if a database access error occurs or 
     * this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     */
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                          
            JDTrace.logInformation (this, "setBinaryStream(int, InputStream)");        
            if(x == null)                                 
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");   
        }                                                                   
        setInputStream(parameterIndex, x);
    }

    //@PDA jdbc40 
    /**
     * Sets the designated parameter to a <code>InputStream</code> object. 
     * This method differs from the <code>setBinaryStream (int, InputStream)</code>
     * method because it informs the driver that the parameter value should be
     * sent to the server as a <code>BLOB</code>.  When the <code>setBinaryStream</code> method is used,
     * the driver may have to do extra work to determine whether the parameter
     * data should be sent to the server as a <code>LONGVARBINARY</code> or a <code>BLOB</code>
     *
     * @param parameterIndex
     * @param inputStream An object that contains the data to set the parameter
     * value to.
     * @throws SQLException if parameterIndex does not correspond to a parameter
     * marker in the SQL statement; if a database access error occurs; 
     * this method is called on a closed <code>PreparedStatement</code> or
     * if parameterIndex does not correspond
     * to a parameter marker in the SQL statement,  
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     *
     */
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {                                          
            JDTrace.logInformation (this, "setBlob(int, InputStream)");        
            if(inputStream == null)                                 
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");   
        }                                                                   
        setInputStream(parameterIndex, inputStream);
    }

    //@PDA jdbc40 helper
    private void setReader(int parameterIndex, Reader reader) throws SQLException
    {
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
            if(reader != null)
            {

                // If the data is a locator, then set its handle.
                int sqlType = sqlData.getSQLType();  //@xml3
                if(sqlType == SQLData.CLOB_LOCATOR ||
                   sqlType == SQLData.BLOB_LOCATOR ||
                   sqlType == SQLData.DBCLOB_LOCATOR ||                 //@pdc jdbc40
                   sqlType == SQLData.NCLOB_LOCATOR ||                  //@pda jdbc40
                   sqlType == SQLData.XML_LOCATOR)                      //@xml3
                {
                    SQLLocator sqlDataAsLocator = (SQLLocator) sqlData;
                    sqlDataAsLocator.setHandle(parameterRow_.getFieldLOBLocatorHandle(parameterIndex));
                    if(JDTrace.isTraceOn()) JDTrace.logInformation(this, "locator handle: " + parameterRow_.getFieldLOBLocatorHandle(parameterIndex));
                    sqlData.set(reader, null, -2); //@readerlen -2 flag to read all of reader chars
                }
                else
                {
                    sqlData.set(JDUtilities.readerToString(reader), null, -1);
                }

                testDataTruncation (parameterIndex, sqlData);
            }
            // Parameters can be null; you can call one of the set methods to null out a
            // field of the database.
            parameterNulls_[parameterIndex-1] = (reader == null);
            parameterSet_[parameterIndex-1] = true;

        }
        
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
     * @param parameterIndex
     * @param reader the <code>java.io.Reader</code> object that contains the 
     *        Unicode data
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * marker in the SQL statement; if a database access error occurs or 
     * this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     */
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {
            JDTrace.logInformation (this, "setCharacterStream(int, Reader)");
            if(reader == null)
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");
        }
        
        setReader(parameterIndex, reader);
    }


    //@PDA jdbc40 
    /**
     * Sets the designated parameter to a <code>Reader</code> object. 
     * This method differs from the <code>setCharacterStream (int, Reader)</code> method
     * because it informs the driver that the parameter value should be sent to
     * the server as a <code>CLOB</code>.  When the <code>setCharacterStream</code> method is used, the
     * driver may have to do extra work to determine whether the parameter
     * data should be sent to the server as a <code>LONGVARCHAR</code> or a <code>CLOB</code>
     * 
     * <P><B>Note:</B> Consult your JDBC driver documentation to determine if 
     * it might be more efficient to use a version of 
     * <code>setClob</code> which takes a length parameter.
     *
     * @param parameterIndex
     * @param reader An object that contains the data to set the parameter value to.
     * @throws SQLException if parameterIndex does not correspond to a parameter
     * marker in the SQL statement; if a database access error occurs; this method is called on
     * a closed <code>PreparedStatement</code>or if parameterIndex does not correspond to a parameter
     * marker in the SQL statement
     *
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     */
    public void setClob(int parameterIndex, Reader reader) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {
            JDTrace.logInformation (this, "setClob(int, Reader)");
            if(reader == null)
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");
        }
        
        setReader(parameterIndex, reader);
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
     * @param parameterIndex
     * @param value the parameter value
     * @throws SQLException if parameterIndex does not correspond to a parameter
     * marker in the SQL statement; if the driver does not support national
     *         character sets;  if the driver can detect that a data conversion
     *  error could occur; if a database access error occurs; or 
     * this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     */
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {
            JDTrace.logInformation (this, "setNCharacterStream(int, Reader)");
            if(value == null)
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");
        }
        
        setReader(parameterIndex, value);
    }

    //@PDA jdbc40 
    /**
     * Sets the designated parameter to a <code>Reader</code> object.  
     * This method differs from the <code>setCharacterStream (int, Reader)</code> method
     * because it informs the driver that the parameter value should be sent to
     * the server as a <code>NCLOB</code>.  When the <code>setCharacterStream</code> method is used, the
     * driver may have to do extra work to determine whether the parameter
     * data should be sent to the server as a <code>LONGNVARCHAR</code> or a <code>NCLOB</code>
     * <P><B>Note:</B> Consult your JDBC driver documentation to determine if 
     * it might be more efficient to use a version of 
     * <code>setNClob</code> which takes a length parameter.
     *
     * @param parameterIndex
     * @param reader An object that contains the data to set the parameter value to.
     * @throws SQLException if parameterIndex does not correspond to a parameter
     * marker in the SQL statement; 
     * if the driver does not support national character sets;
     * if the driver can detect that a data conversion
     *  error could occur;  if a database access error occurs or 
     * this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException  if the JDBC driver does not support this method
     */
    public void setNClob(int parameterIndex, Reader reader) throws SQLException
    {
        if(JDTrace.isTraceOn())
        {
            JDTrace.logInformation (this, "setNClob(int, Reader)");
            if(reader == null)
                JDTrace.logInformation (this, "parameter index: " + parameterIndex  + " value: NULL");
        }
        
        setReader(parameterIndex, reader);
    }
}
