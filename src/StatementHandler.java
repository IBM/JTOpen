///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: StatementHandler.java
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
import java.util.*;

/**
The StatementHandler class is designed to handle all interactions
needed by the JDBC-ME driver with the JDBC Statement interface.
**/
class StatementHandler
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    private JdbcMeService service_;
    private MicroDataInputStream in_;
    private MicroDataOutputStream out_;


    /**
    Constructor.  Creates a new JDBC-ME handler for Statement
    objects.
    **/
    public StatementHandler(JdbcMeService jdbcme, MicroDataInputStream in, MicroDataOutputStream out)
    {
        service_ = jdbcme;
        in_ = in;
        out_ = out;
    } 


    /**
    The process function routes the function id and the Statement
    to the proper handler.
    **/
    public void process(Statement statement, int funcId) throws IOException 
    {
        switch (funcId)
        {
        case MEConstants.STMT_CLOSE:
            close(statement);
            break;
        case MEConstants.STMT_EXECUTE:
            execute(statement);
            break;
        default:
            // TODO:  This is an exception condition...
            System.out.println("Error - Statement Function ID not recognized - function code: " + funcId);
            break;
        }
    }           


    /**
    Closes the Statement object.  This function has the side effect
    of closing any ResultSets that are open under it as well.
    Unlike most of the methods of this class, if an exception occurs
    while closing the Statement, this method will not report it back
    to the caller in any way.  It is simply swallowed and a message
    is logged concerning the failure.
    <P>
    The data flow is as follows:
    <input>
       - nothing more
    <output>
       - nothing more
    <P>
 
    @param connection    The connection object to close.
    **/
    public void close(Statement statement) throws IOException
    {
        try
        {
            statement.close();
        }
        catch (SQLException e)
        {
            System.out.println("Exception caught trying to close Statement object " + statement);
            e.printStackTrace();
        }

        // Remove it from the vector.
        service_.removeStatement(statement);
    }           


    /**
    Executes an SQL Statement.  This method is used to handle executing
    both queries and updates.  The client can determine what was executed
    through the returned data stream.
    <P>
    The data flow is as follows:
    <input>
       String - the SQL statement that is to be executed.
    <output>
       <int> - whether or not there is a ResultSet.
       1:    There is a ResultSet
          <int> The ResultSet object handle
          <int> The number of columns in the ResultSet
          <int list>  The data types of each of the columns in the ResultSet
          // <int list> The sizes of the columns.  We don't care about this yet.
 
       0:    There is no ResultSet
          <int> The update count for the executed SQL Statement.
       -1:   An exception occurred (see exception handling for details)
    <P>
 
    @param rs    The ResultSet object to use.
    **/
    public void execute(Statement statement) throws IOException
    {
        ResultSetMetaData rsmd;
        int count;

        try
        {
            // Get the SQL statement.
            String sql = in_.readUTF();
            boolean rsYes = statement.execute(sql);
            if (rsYes)
            {
                // There is a result set
                out_.writeInt(1);
                ResultSet rs = statement.getResultSet();
                service_.addResultSet(statement, rs);     // DOMINO

                // Put the object into our map.
                int objectId = service_.mapObject(rs);
                out_.writeInt(objectId);

                rsmd = rs.getMetaData();
                count = rsmd.getColumnCount();
                out_.writeInt(count);
                for (int i = 0; i < count; i++)
                {
                    out_.writeInt(rsmd.getColumnType(i+1));
                }
            }
            else
            {
                // No ResultSet
                out_.writeInt(0);
                // output the updateCount
                out_.writeInt(statement.getUpdateCount());
            }
            out_.flush();
        }
        catch (SQLException e)
        {
            service_.handleException(e);
        }
    }
}
