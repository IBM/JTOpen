///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JdbcMeDriver.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import java.sql.*;
import java.io.*;


/**
 *  The JdbcMeDriver class is a driver that accesses DB2 for OS/400 databases.
 *
 *  <p>This class registers itself automatically when it is loaded.  To use the
 *  iSeries Developer Kit for Java Native JDBC driver, specify the "driver=native" on the url.
 *
 *  <p>Applications make connection requests to the DriverManager, which dispatches 
 *  them to the appropriate driver.  This driver accepts connection requests
 *  for databases specified by the URLs that match the following syntax:
 *  <pre>
 *  jdbc:as400://<em>server-name</em>/<em>default-schema</em>;meserver=&lt;server&gt;[:port];<em>[other properties]</em>;
 *  </pre>
 *  Additionally, for the JdbcMe driver, if the port is unspecified, the port number 3470 is used.
 *
 *  <p>The driver uses the specified server name to connect
 *  to a corresponding iSeries server.  If a server name is not
 *  specified, an exception will occur.  If a userid or password
 *  is not specified via the url or by using the getConnection(url, userid, password)
 *  method, an exception will occur.
 *
 *  <p>The default schema is optional and the driver uses it to resolve 
 *  unqualified names in SQL statements.  If no default schema is set, then
 *  the driver resolves unqualified names based on the naming convention
 *  for the connection.  If SQL naming is being used, and no default schema
 *  is set, then the driver resolves unqualified names using the schema with 
 *  the same name as the user.  If system naming is being used, and no
 *  default schema is set, then the driver resolves unqualified names using
 *  the server job's library list.  See <a href="../../../../JDBCProperties.html"> JDBC properties</a>
 *  for more details on how to set the naming convention and library list.
 *
 *  <p>Several properties can optionally be set within the URL.  They are 
 *  separated by semicolons and are in the form:
 *  <pre>
 *  <em>name1</em>=<em>value1</em>;<em>name2</em>=<em>value2</em>;<em>...</em>
 *  </pre>
 *  See <a href="../../../../JDBCProperties.html"> JDBC properties</a> for a complete list of properties supported by this driver.
 *
 *  <p>The following example URL specifies a connection to the
 *  database on server <em>mysystem.helloworld.com</em> with
 *  <em>mylibrary</em> as the default schema.  The connection will
 *  use the system naming convention, return full error messages, and
 *  connect to the iSeries through the specified MEServer <em>myMeServer</em>:
 *
 *  <pre>
 *  jdbc:as400://mysystem.helloworld.com/mylibrary;naming=system;errors=full;meserver=myMeServer;
 *  </pre>
 *  
 *  <p><b>Note:</b> Since Java 2 Micro-Edition does not include java.sql,
 *  JdbcMeDriver implements the java.sql package that is also part 
 *  of this driver.
 *
 *  @see com.ibm.as400.micro.MEServer
 **/
public class JdbcMeDriver
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    private static AS400 system_;


    /**
     *  Default private constructor.
     **/
    private void JdbcMeDriver()
    {
    }


    /**
     *  Process an exception line flow from the server
     *  and throw the resulting SQL exception.
     *  
     *  The exception line flow consists of the
     *  SQL state followed by the SQL message text.
     *  Throw the resulting SQL exception.*
     **/
    static void processException(JdbcMeConnection conn) throws JdbcMeException 
    {
        /**
         * Line flows out:
         *    None
         * Line flows in:
         *    ID indicating an exception (read by caller)
         *    UTF String for SQL state
         *    UTF String for SQL message
         **/
        try
        {
            String sqlState = system_.fromServer_.readUTF();
            String message = system_.fromServer_.readUTF();

            throw new JdbcMeException(message, sqlState);
        }
        catch (IOException e)
        {
            throw new JdbcMeException(e.toString(), null);
        }
    }

    /**
     *  Connects to the database named by the specified URL.
     *  See <a href="../../../../JDBCProperties.html">
     *  JDBC properties</a> for a complete list of properties
     *  supported by this driver.
     *
     *  <b>Note:</b> The java.sql.DriverManager
     *  doesn't support drivers other than JdbcMe
     *
     *  @return The connection to the database or null if the driver does not understand how to connect to the database.
     *
     *  @see com.ibm.as400.micro.JdbcMeDriver#getConnection(String, String, String)
     *
     *  @exception JdbcMeException If the driver is unable to make the connection.
     **/
    public static java.sql.Connection getConnection(String url) throws JdbcMeException 
    {
        /**
         *  Using this method as static prevents us from having
         *  to create more objects.
         **/
        return getConnection(url, null, null);
    }

    /**
     *  The JdbcMe driver supports URLs of the following
     *  form:  "jdbc:<subprotocol>:<target-db-specification>;meserver=<server>[:port]"
     *  The subprotocol and target-db-specification is specified
     *  as required by the target JDBC driver being used on the
     *  JdbcMe host server.
     *  Additionally, for the JdbcMe driver, if the port is
     *  unspecified, the port number 3470 is used.
     *
     *  The meserver property is used to find the MEServer. 
     *  All other values/properties are
     *  passed through to the Jdbc driver used by the target MEServer.
     *
     *  <b>Note:</b>  The java.sql.DriverManager doesn't support drivers other than JdbcMe.
     *
     *  @param url The URL for the database.
     *  @param user The user on whose behalf the connection is being made.
     *  @param password  The user's password.
     *
     *  @exception JdbcMeException If the driver is unable to make the connection.
     **/
    public static java.sql.Connection getConnection(String url, String user, String password) throws JdbcMeException 
    {
        /**
         *  Using this method as static prevents us from having
         *  to create more objects.
         **/
        String meServer = null;
        int i, k;

        i = url.indexOf(";meserver=");
        if (i == -1)
            throw new JdbcMeException("Invalid URL, no jdbcme property: " + url, null);

        String remoteUrl = url.substring(0, i);

        // Start after the first semi-colon and search for the next semi-colon
        for (k=i+10; k<url.length(); ++k)
        {
            if (url.charAt(k) == ';')
                break;
        }

        // If we didn't find it, no more properties
        // were specified, and the hostPort is at the
        // end of the URL. Lets take it.
        if (k >= url.length())
        {
            meServer = url.substring(k+10);
        }
        else
        {
            // There are more properties after the host:port
            meServer = url.substring(i+10, k);
            // Add the rest (including the semi-colon) to the original string.
            remoteUrl += url.substring(k);
        }
        
        String uid = null;
        String pwd = null;
        // This data gets sent to the server in the JDBC URL
        // for each DB connection attempt unless it is
        // already specified in the URL.
        if (user != null && password != null)
        {
            if (url.indexOf(";user=") == -1)
                remoteUrl += ";user=" + user;

            if (url.indexOf(";password=") == -1)
                remoteUrl += ";password=" + password;
        }
        else
        {
            String b = url.substring(url.indexOf("user="));
            uid = b.substring(5, b.indexOf(';'));
            
            b = url.substring(url.indexOf("password="));
            pwd = b.substring(9, b.indexOf(';'));
        }

        try
        {
            String systemName;
            String begin = url.substring(0, url.indexOf(";"));
            String system = begin.substring(begin.indexOf("//")+2);

            int n = system.indexOf("/");
            if (n  != -1)
                systemName = system.substring(0, n);
            else
                systemName = system;

            system_ = new AS400(systemName, uid, pwd, meServer);
            system_.connect();
        }
        catch (Exception e)
        {
            try
            {
                system_.disconnect();
            }
            catch (Exception ex)
            {
            }

            throw new JdbcMeException(e.toString(), null);
        }

        return new JdbcMeConnection(remoteUrl, system_);
    }
}
