///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ConnectionHandler.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import java.io.*;
import java.sql.*;

import com.ibm.as400.access.Trace;


/**
 *  The ConnectionHandler class is designed to handle all interactions
 *  needed by the JDBC-ME driver with the JDBC Connection interface.
 **/
class ConnectionHandler
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    private JdbcMeService service_;
    private MicroDataInputStream in_;
    private MicroDataOutputStream out_;
    
    /**
    Constructor.  Creates a JDBC-ME handler for Connection objects.
    **/
    public ConnectionHandler(JdbcMeService jdbcme, MicroDataInputStream in, MicroDataOutputStream out)
    {
        service_ = jdbcme;
        in_ = in;
        out_ = out;
    }


    /**
    The process function routes the function id and the Connection
    to the proper handler.
    **/
    public void process(Connection connection, int funcId) throws IOException
    {
        switch (funcId)
        {
        case MEConstants.CONN_CLOSE:
            close(connection);
            break;
        case MEConstants.CONN_CREATE_STATEMENT:
            createStatement(connection);
            break;
        case MEConstants.CONN_CREATE_STATEMENT2:
            createStatement2(connection);
            break;
        case MEConstants.CONN_PREPARE_STATEMENT:
            prepareStatement(connection);
            break;
        case MEConstants.CONN_SET_AUTOCOMMIT:
            setAutoCommit(connection);
            break;
        case MEConstants.CONN_SET_TRANSACTION_ISOLATION:
            setTransactionIsolation(connection);
            break;
        case MEConstants.CONN_COMMIT:
            commit(connection);
            break;
        case MEConstants.CONN_ROLLBACK:
            rollback(connection);
            break;
        default:
            // TODO:  This is an exception condition...
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Connection Function ID not recognized - function code: " + funcId);
            break;
        }
    }


    /**
    Closes the Connection object.  This function has the side effect
    of closing all Statements and ResultSets that are open under the
    connection as well.  Unlike most of the methods of this class,
    if an exception occurs while closing the Connection, this method
    will not report it back to the caller in any way.  It is simply
    swollowed and a message is logged concerning the failure.
    <P>
    The data flow is as follows:
    <input>
       - nothing more
    <output>
       - nothing more
    <P>
 
    @param connection    The connection object to close.
    **/
    public void close(Connection connection) throws IOException
    {
        try
        {
            connection.close();
        }
        catch (SQLException e)
        {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Exception caught trying to close connection object " + connection, e);
        }

        // Remove it from the vector.
        service_.removeConnection(connection);
    }
                                   

    /**
    Creates a JDBC Statement object under the given connection
    and returns a int handle to it to the caller.
    <P>
    The data flow is as follows:
    <input>
       - nothing more
    <output>
       int - the statement handle for the object.
    <P>
 
    @param connection    The connection object to use.
    **/
    public void createStatement(Connection connection) throws IOException
    {
        try
        {
            Statement s = connection.createStatement();

            // Insert it into the vector
            service_.addStatement(s);

            // Put the object into our map
            int objectId = service_.mapObject(s);
            out_.writeInt(objectId);
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }    


    /**
    Creates a JDBC Statement object under the given connection
    and returns a int handle to it to the caller.  The statement
    object that will get created by this function will be scrollable
    and updatable.
    <P>
    The data flow is as follows:
    <input>
       - nothing more
    <output>
       int - the statement handle for the object.
    <P>
 
    @param connection    The connection object to use.
    **/
    public void createStatement2(Connection connection) throws IOException
    {
        try
        {
            // This is the JDBC create statement enhancement.
            int type = in_.readInt();
            int concurrency = in_.readInt();
            Statement s = connection.createStatement(type, concurrency);
            
            // Insert it into the vector
            service_.addStatement(s);

            // Put the object into our map
            int objectId = service_.mapObject(s);
            out_.writeInt(objectId);
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }


    /**
    Creates a JDBC PreparedStatement object under the given connection
    and returns a int handle to it to the caller.
    <P>
    The data flow is as follows:
    <input>
       String   - the sql statement to prepare.
       - nothing more
    <output>
       int - the prepared statement handle for the object.
          int 1 = there is a ResultSet
             int   - the number of columns in the ResultSet
             int   - list of all the columns data types.
          int 0 = there is no Resultset
    <P>
 
    @param connection    The connection object to use.
    **/
    public void prepareStatement(Connection connection) throws IOException
    {
        try
        {
            // TODO:  We may have to ensure that all the SQL processing happens
            //        before we try to write anything so that we can put out
            //        a clean stream....
            // Read in the SQL statement to prepare.
            String sql = in_.readUTF();
            
            // Create the JDBC PreparedStatement
            PreparedStatement ps = connection.prepareStatement(sql);
            
            // Insert it into the vector
            service_.addStatement(ps);

            // Put the object into our map
            int objectId = service_.mapObject(ps);
            out_.writeInt(objectId);

            ResultSetMetaData rsmd = ps.getMetaData();
            
            if (rsmd == null)
                out_.writeInt(0);
            else
            {
                out_.writeInt(1);
                int count = rsmd.getColumnCount();
                out_.writeInt(count);
                
                for (int i = 0; i < count; i ++)
                {
                    out_.writeInt(rsmd.getColumnType(i + 1));
                }
            }
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }


    /**
    Creates a JDBC PreparedStatement object under the given connection
    and returns a int handle to it to the caller.  The prepared statement
    object that will get created by this function will be scrollable
    and updatable.
    <P>
    The data flow is as follows:
    <input>
       String   - the sql statement to prepare.
       - nothing more
    <output>
       int - the prepared statement handle for the object.
          int 1 = there is a ResultSet
             int   - the number of columns in the ResultSet
             int   - list of all the columns data types.
          int 0 = there is no Resultset
    <P>
 
    @param connection    The connection object to use.
    **/
    public void prepareStatement2(Connection connection) throws IOException
    {
        try
        {
            // TODO:  We may have to ensure that all the SQL processing happens
            //        before we try to write anything so that we can put out
            //        a clean stream....
            // Read in the SQL statement to prepare.
            String sql = in_.readUTF();
            
            // Create the JDBC PreparedStatement
            PreparedStatement ps = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            // Insert it into the vector
            service_.addStatement(ps);

            // Put the object into our map
            int objectId = service_.mapObject(ps);
            out_.writeInt(objectId);

            ResultSetMetaData rsmd = ps.getMetaData();
            
            if (rsmd == null)
                out_.writeInt(0);
            else
            {
                out_.writeInt(1);
                int count = rsmd.getColumnCount();
                out_.writeInt(count);
                
                for (int i = 0; i < count; i ++)
                {
                    out_.writeInt(rsmd.getColumnType(i + 1));
                }
            }
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }


    /**
    Sets the autocommit attribute for the underlying
    connection object.
    <P>
    The data flow is as follows:
    <input>
       boolean - t/f for autocommit setting
    <output>
       int - 1/0 for success/failure.
    <P>
 
    @param connection    The connection object to use.
    **/
    public void setAutoCommit(Connection connection) throws IOException
    {
        try
        {
            boolean b = in_.readBoolean();
            connection.setAutoCommit(b);
            out_.writeInt(1);

        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }


    /**
    Sets the transaction isolation level for the underlying
    connection object.
    <P>
    The data flow is as follows:
    <input>
       int - the transaction isolation level to set
    <output>
       int - 1/0 for success/failure.
    <P>
 
    @param connection    The connection object to use.
    **/
    public void setTransactionIsolation(Connection connection) throws IOException
    {
        try
        {
            int level = in_.readInt();
            connection.setTransactionIsolation(level);
            out_.writeInt(1);
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }


    /**
    Commits the work that is pending under the current
    transaction.
    <P>
    The data flow is as follows:
    <input>
       none
    <output>
       int - 1/0 for success/failure.
    <P>
 
    @param connection    The connection object to use.
    **/
    public void commit(Connection connection) throws IOException
    {
        try
        {
            connection.commit();
            out_.writeInt(1);
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }


    /**
    Rolls back the work that is pending under the current
    transaction.
    <P>
    The data flow is as follows:
    <input>
       none
    <output>
       int - 1/0 for success/failure.
    <P>
 
    @param connection    The connection object to use.
    **/
    public void rollback(Connection connection) throws IOException
    {
        try
        {
            connection.rollback();
            out_.writeInt(1);
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }
}
