///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 *  The SQLStatement class represents an SQL statement.  This class allows the user to runl any SQL statement
 *  from a wireless device.  This class provides a modified subset of the JDBC functions available in 
 *  the com.ibm.as400.access package.
 *
 *  The IBM Toolbox for Java JDBC driver can use different JDBC driver implementations based on the environment. 
 *  If the environment is an iSeries Java Virtual Machine on the same iSeries as the database to which the program
 *  is connecting, the native iSeries Developer Kit for Java JDBC driver can be used. In any other environment, the 
 *  IBM Toolbox for Java JDBC driver is used.
 *
 *  <P>The following example demonstrates the use of SQLStatement:
 *  <br>
 *  <pre>
 *   AS400 system = new AS400("mySystem", "myUserid", "myPwd", "myMEServer");
 *   try
 *   {
 *       // Create a hashtable containing any SQL connection properties.
 *       Hashtable properties = new Hashtable();
 *       properties.put("package cache", "true");
 *       properties.put("trace", "true");
 
 *       // Execute an SQL statement.
 *       SQLResultSet rs = SQLStatement.executeQuery(system, properties, "select * from qiws.qcustcdt");
 *       
 *       // Get the row data.
 *       String[] columns = rs.getRowData();
 *       
 *       // While there are more rows, continue to call next() and
 *       // print out the selected columns.
 *       while (rs.next())
 *       {
 *           System.out.println(columns[2]+" "+columns[3]);
 *           columns = rs.getRowData();
 *       }
 *       
 *       // Close the result set.
 *       rs.close();
 *   }
 *   catch (Exception e)
 *   {
 *       // Handle the exception
 *   }
 *   // Done with the system object.
 *   system.disconnect();
 *  </pre>
 **/
public final class SQLStatement 
{
    /**
     *  Construct a SQLStatement object.  This class has a static method execute() and
     *  does not need a public constructor.
     **/
    private SQLStatement()
    {
    }


    /**
     *  Runs an SQL statement that returns a single result set. 
     *
     *  @param system The iSeries server.
     *  @param info      The connection properties or null meaning no properties.
     *  @param query   The SQL statement.
     *
     *  @return The result set that contains the data produced by the query.
     *
     *  @exception  IOException  If an error occurs while communicating with the server.
     *  @exception  MEException  If an error occurs while processing the ToolboxME request.
     **/
    public static SQLResultSet executeQuery(AS400 system, Hashtable info, String sql) throws IOException, MEException
    {
        if (system == null)
            throw new NullPointerException("system");

        if (sql == null)
            throw new NullPointerException("sql");
        
        synchronized(system)
        {
            system.connect();
            system.toServer_.writeInt(MEConstants.SQL_QUERY);

            if (info == null)
            {
                system.toServer_.writeInt(MEConstants.NO_CONNECTION_PROPERTIES);
                system.toServer_.flush();
            }
            else
            {
                // Write the number of sql url properties specified in the hashtable..
                int size = info.size();
                system.toServer_.writeInt(size);
                system.toServer_.flush();
                
                // Loop through the hashtable and write out the key (sql property name)
                // and the key value (sql property value)
                for (Enumeration e = info.keys() ; e.hasMoreElements() ;) 
                {
                    String key = (String)e.nextElement();
                    system.toServer_.writeUTF(key);
                    system.toServer_.writeUTF((String) info.get(key));
                }
                system.toServer_.flush();
            }

            system.toServer_.writeUTF(sql);
            system.toServer_.flush();

            int retVal = system.fromServer_.readInt();

            if (retVal == MEConstants.SQL_STATEMENT_SUCCEEDED)
            {
                int transactionID = system.fromServer_.readInt();

                return new SQLResultSet(system, transactionID);
            }
            else
            {
                throw new MEException(system.fromServer_.readUTF(), retVal);
            }
        }
    }


    /**
     *  Runs an SQL INSERT, UPDATE, or DELETE statement, or any SQL statement that does not return a result set. 
     *
     *  @param syatem The iSeries server.
     *  @param info  The connection properties or null meaning no properties.
     *  @param sql   The SQL statement.
     *
     *  @return Either the row count for INSERT, UPDATE, or DELETE, or 0 for SQL statements that return nothing.
     *
     *  @exception  IOException  If an error occurs while communicating with the server.
     *  @exception  MEException  If an error occurs while processing the ToolboxME request.
     **/
    public static int executeUpdate(AS400 system, Hashtable info, String sql) throws IOException, MEException
    {
        if (system == null)
            throw new NullPointerException("system");

        if (sql == null)
            throw new NullPointerException("sql");
        
        synchronized(system)
        {
            system.connect();
            system.toServer_.writeInt(MEConstants.SQL_UPDATE);
            
            if (info == null)
            {
                system.toServer_.writeInt(MEConstants.NO_CONNECTION_PROPERTIES);
                system.toServer_.flush();
            }
            else
            {
                // Write the number of sql url properties specified in the hashtable..
                int size = info.size();
                system.toServer_.writeInt(size);
                system.toServer_.flush();
                
                // Loop through the hashtable and write out the key (sql property name)
                // and the key value (sql property value)
                for (Enumeration e = info.keys() ; e.hasMoreElements() ;) 
                {
                    String key = (String)e.nextElement();
                    system.toServer_.writeUTF(key);
                    system.toServer_.writeUTF((String) info.get(key));
                }
                system.toServer_.flush();
            }
            
            system.toServer_.writeUTF(sql);
            system.toServer_.flush();

            int retVal = system.fromServer_.readInt();

            if (retVal == MEConstants.SQL_STATEMENT_SUCCEEDED)
            {
                // read and return the row count or zero.
                return system.fromServer_.readInt();
            }
            else
            {
                throw new MEException(system.fromServer_.readUTF(), retVal);
            }
        }
    }
}
