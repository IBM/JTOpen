///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JdbcMeService.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import com.ibm.as400.access.Trace;
import java.io.*;
import java.sql.*;
import java.util.*;

class JdbcMeService implements Service
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    private Vector connections_;
    private Vector statements_;
    private Hashtable statementresults_; // DOMINO
    private Hashtable map_;
    private Properties properties_;

    private MicroDataInputStream in_;
    private MicroDataOutputStream out_;
    private int dataFlowType_;

    // Note: for a multi-threaded client, we would have to synchronize
    //       access to this.  Of course, we would have to do a lot more
    //       than that. :)
    private int NextObjectId_;


    private ConnectionHandler connectionHandler_;
    private StatementHandler statementHandler_;
    private ResultSetHandler resultSetHandler_;


    /**
    Constructor so we do not have to pass the streams around in all
    the helper functions.
    **/
    public JdbcMeService()
    {
        // Create the data structures to hold our JDBC objects.
        connections_ = new Vector();
        statements_  = new Vector();
        map_         = new Hashtable();
        statementresults_ = new Hashtable(); // DOMINO

        NextObjectId_ = 1;
        dataFlowType_ = MEConstants.DATA_FLOW_LIMITED;

        registerDrivers();
    }


    /**
    **/
    public void setDataStreams(MicroDataInputStream in, MicroDataOutputStream out)
    {
        in_ = in;
        out_ = out;

        // Create the JDBC internal handlers.
        connectionHandler_ = new ConnectionHandler(this, in_, out_);
        statementHandler_ = new StatementHandler(this, in_, out_);
        resultSetHandler_ = new ResultSetHandler(this, in_, out_);
    }


    /**
    Tells the caller whether or not this service request
    handler can handle the specified request being input.
    **/
    public boolean acceptsRequest(int functionId)
    {

        if ((functionId > 999) && (functionId < 2000))
            return true;

        return false;
    }


    /**
    Routes the specified function request to the correct
    internal handler.
    **/
    public void handleRequest(int functionId) throws IOException 
    {
        // Output the function id for debugging.
        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, "Function id is " + Integer.toHexString(functionId));

        // First check for and handle any service requests.
        // Just like the various JDBC object handlers have a process
        // method for dealing with requests, so do service handlers.
        if (isServiceRequest(functionId))
        {
            process(functionId);
            return;
        }


        // If a new connection is needed, do it inline here.  As there
        // is not Driver object, there is no JDBC object to follow in
        // the data stream.
        if (functionId == MEConstants.CONN_NEW)
        {
            if (Trace.isTraceOn())
                Trace.log(Trace.PROXY, "JDBC Service: The request is here. " + Integer.toHexString(functionId));

            boolean trace = Trace.isTraceOn();

            if (trace)
                Trace.setTraceOn(false);

            // Input Parm - the url to use to get the connection.
           String url = in_.readUTF();

           if (trace)
               Trace.setTraceOn(true);
            
            // Create the connection
            try
            {
                Connection c = DriverManager.getConnection(url);

                // Add it to our vector.
                connections_.addElement(c);

                // Put the object into our map.
                int objectId = getNextObjectId();
                map_.put(new Integer(objectId), new Integer(c.hashCode()));
                out_.writeInt(objectId);
                out_.flush();
            }
            catch (SQLException e)
            {
                handleException(e);
            }
            catch (Exception e)
            {
                if (Trace.isTraceOn())
                    Trace.log(Trace.ERROR, e);
            }

            return;
        }

        // The first value on all JDBC requests that are not for a new
        // connections or service requests is an object id.
        int handle = in_.readInt();

        // Get the object that is represented by the JDBC handle.
        Object object = processJdbcObject(handle);

        // Do the work - I don't really like this layout... perhaps
        // I will change it as Fred suggested to do things more OO.
        if (object instanceof Connection)
        {
            connectionHandler_.process((Connection) object, functionId);
        }
        else if (object instanceof Statement)
        {
            statementHandler_.process((Statement) object, functionId);
        }
        else if (object instanceof ResultSet)
        {
            resultSetHandler_.process((ResultSet) object, functionId);
        }
        // DOMINO START
        else
        {
            if (Trace.isTraceOn())
                Trace.log(Trace.PROXY, "Error, unknown Jdbc object < " + object + ">");
            try
            {
                throw new SQLException("Error, unknown Jdbc object < " + object + ">");
            }
            catch (SQLException e)
            {
                handleException(e);
            }
        }
        // DOMINO END
    }


    /**
    Figure out what the handle type is so that we can figure
    out what the function indentifier means.
 
    TODO:  We will probably change this so that that handle
    is really the object hash code instead of the CLI handle.
    Otherwise we can't be JDBC driver neutral.
    **/
    public Object  processJdbcObject(int handle) throws IOException 
    {
        // Get the object hashcode based off of the user input handle.
        Integer oObjectHash = (Integer) map_.get(new Integer(handle));
        int iObjectHash = oObjectHash.intValue();

        Integer mapValue = null;

        try
        {
// DOMINO ADD
            // Loop through all our result sets
            // The Hostserver requires JDK 1.2, so using this
            // method should be fine.
            Collection col = statementresults_.values();
            Iterator   it  = col.iterator();
            while (it.hasNext())
            {
                ResultSet rs = (ResultSet) it.next();
                int   rsHandle = rs.hashCode();
                if (iObjectHash == rsHandle)
                    return rs;
            }
// DOMINO END ADD

// DOMINO REORDER
            // Loop through all our statements
            for (int i = 0; i < statements_.size(); i++)
            {
                Statement s = (Statement) statements_.elementAt(i);
                int stmtHandle = s.hashCode();
                if (iObjectHash == stmtHandle)
                    return s;
            }

            // Loop through all our connections...
            for (int i = 0; i < connections_.size(); i++)
            {
                // Get the next connection object.
                Connection c = (Connection) connections_.elementAt(i);
                // Get the connection's hashcode
                int connHandle = c.hashCode();
                // Compare it.
                if (iObjectHash == connHandle)
                    return c;
            }
        }
        catch (Exception e)
        {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, e);
        }

        if (Trace.isTraceOn())
            Trace.log(Trace.ERROR, "ERROR! returning null from processJdbcObject!");
        
        return null;
    }


    /**
    This function can handle adding statements, preparedStatements,
    and callableStatements to the internal data structures.
    **/
    public void addStatement(Statement s)
    {
        statements_.addElement(s);
        // don't add a statementresults entry until the result
        // set is created.
    }

    /**
    This function adds connections to the internal data structures.
    **/
    public void addConnection(Connection c)
    {
        connections_.addElement(c);
    }

    /**
     * This function adds result sets to the internal data structure
     */
    public void addResultSet(Statement s, ResultSet rs) throws SQLException 
    {     // DOMINO
        // The DOMINO Jdbc driver doesn't implement
        // the getStatement() method. We can't use it
        // here. 'lotus.jdbc.domino.DominoDriver'
        //// Statement s = rs.getStatement();

        // statementresults key is statement, value is the resultset
        statementresults_.remove(s);   // 'close' existing result set
        statementresults_.put(s, rs);  // Add the new one.
    }

    /**
    This function can handle removing statements, preparedStatements,
    and callableStatements to the internal data structures.
    **/
    public void removeStatement(Statement s)
    {
        statements_.remove(s);
        // Also remove the result set associated with the statement
        // if there is one.
        statementresults_.remove(s);
    }

    public void removeResultSet(ResultSet rs) throws SQLException 
    {   // DOMINO
        // The DOMINO Jdbc driver doesn't implement
        // the getStatement() method. We can't use it
        // here. 'lotus.jdbc.domino.DominoDriver'
        //// Statement s = rs.getStatement();
        //// statementresults.remove(s);

        // Remove the statement->resultset mapping by going
        // backwards its a bit slower.
        Collection col = statementresults_.values();
        // The Collection is a live view who's backing
        // is the Hashtable, removing this element, removes
        // it from the hash table too.
        col.remove(rs);
    }

    /**
    This function removes connections to the internal data structures.
    **/
    public void removeConnection(Connection c)
    {
        // Before removing the connection from our data storage, we
        // want to remove the statements from storage for that connection.
        Connection toTry = null;
        for (int i = 0; i < statements_.size(); i++)
        {
            Statement s = (Statement) statements_.elementAt(i);
            try
            {
                toTry = s.getConnection();
            }
            catch (SQLException e)
            {
                if (Trace.isTraceOn())
                    Trace.log(Trace.ERROR, "Exception thrown trying to get the connection for a statement.", e);
            }

            if (c == toTry)
            {
                if (Trace.isTraceOn())
                    Trace.log(Trace.PROXY, "Implicitly closing a statement because of connection close");

                removeStatement(s);
            }
        }
        connections_.remove(c);
    }


    /**
    This is the generic exception handler for all SQLExceptions
    that can happen.
    **/
    public void handleException(SQLException e) throws IOException 
    {
        if (Trace.isTraceOn())
            Trace.log(Trace.ERROR, e);

        out_.writeInt(-1);
        
        String s = e.getSQLState();
        
        if (s == null)
            s = "null";

        out_.writeUTF(s);
        
        s = e.getMessage();
        
        if (s == null)
            s = "null";

        out_.writeUTF(s);
        out_.flush();
    }


    /**
    This function returns the next usable object id..
    **/
    public int getNextObjectId()
    {
        // TODO:  at some point, it should be changed to reuse opened
        // object ids from holes that get created in its map.
        int returnValue = NextObjectId_;
        NextObjectId_++;
        
        return returnValue;
    }


    /**
    This method encapsulates the map out of the service handlers.
    **/
    public int mapObject(Object object)
    {
        int objectId = getNextObjectId();
        map_.put(new Integer(objectId), new Integer(object.hashCode()));
        
        return objectId;
    }


    /**
    Get a list of JDBC drivers that the server should attempt to make available.
    **/
    public void registerDrivers()
    {
        String driver = "com.ibm.as400.access.AS400JDBCDriver";
        
        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, "Loading driver: " + driver);
        try
        {
            Class.forName(driver);
        }
        catch (ClassNotFoundException e)
        {
            if (Trace.isTraceOn())
                Trace.log(Trace.PROXY, "Failed to load driver " + driver);
        }
    }



    /**
    **/
    public boolean isServiceRequest(int funcId)
    {
        // Check for function id in the JDBCME service request range.
        if ((funcId > 1900) && (funcId < 1999))
            return true;

        return false;
    }



    /**
    Process service requests
    **/
    public void process(int funcId) throws IOException
    {
        switch (funcId)
        {
        case MEConstants.JDBCME_DATA_TYPE_FLOW:
            setDataFlowType();
            break;
        default:
            // TODO:  This is an exception condition...
            System.out.println("Error - JDBC-ME Service request unrecognized - function code: " + funcId);
            break;
        }
    }


    /**
    Allows JDBCME service handlers to determine the type of
    data type flows the user application is interested in.
    **/
    public int getDataFlowType()
    {
        return dataFlowType_;
    }


    /**
    **/
    public void setDataFlowType() throws IOException
    {
        // TODO:  Verification should be done that this is
        //        a value value passed in and stuff like
        //        that.  Also, the function should be broken
        //        out so that appropriate JavaDoc can be
        //        created for it.  
        int type = in_.readInt();
        if ((type == MEConstants.DATA_FLOW_ALL) ||
            (type == MEConstants.DATA_FLOW_LIMITED) ||
            (type == MEConstants.DATA_FLOW_STRINGS_ONLY))
        {
            dataFlowType_ = type;
            out_.writeInt(1);
            out_.flush();
        }
        else
        {
            handleServiceException("An invalid setting was passed for setting the data flow type: " + type);
        }
    }


    /**
    Handles exceptions that happen at a service level.
    **/
    public void handleServiceException(String message) throws IOException 
    {
        out_.writeInt(-1);
        out_.writeUTF("JDBC");
        out_.writeUTF(message);
        out_.flush();
    }

}



