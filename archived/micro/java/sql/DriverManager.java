///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DriverManager.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package java.sql;


/**
 *  The basic service for managing a set of JDBC drivers.
 *
 *  When the method <code>getConnection</code> is called, the DriverManager will construct a JdbcMeConnection.
 **/
public class DriverManager
{
    /**
     * NOTE - Currently, we know of no other Jdbc drivers for
     * the Palm KVM device until we do, we'll simply construct
     * JdbcMe connections directly using internal interfaces
     * and ignore any other possible drivers.
     **/

    /**
     *  Create an SQL Connection given the URL identifying the connection properties.
     *
     *  @param url  a database url of the form jdbc:subprotocol:subname
     *
     *  @exception SQLException if a database access error occurs.
     **/
    public static synchronized Connection getConnection(String url) throws SQLException 
    {
        return com.ibm.as400.micro.JdbcMeDriver.getConnection(url);
    }

    /**
     *  Create an SQL Connection given the URL identifying the connection properties.
     *  
     *  @param url  A database url of the form jdbc:subprotocol:subname.
     *  @param user The database user on whose behalf the connection is being made.
     *  @param password  The user's password.
     *
     *  @exception SQLException if a database access error occurs.
     **/
    public static synchronized Connection getConnection(String url, String user, String password) throws SQLException 
    {
        return com.ibm.as400.micro.JdbcMeDriver.getConnection(url, user, password);
    }
}

