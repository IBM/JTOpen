///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCConnectionPoolDataSource.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2010 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.sql.SQLException;
import javax.naming.*;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

/**
*  The AS400JDBCConnectionPoolDataSource class represents a factory for 
*  AS400PooledConnection objects.
*
*  <P>
*  The following is an example that creates an AS400JDBCConnectionPoolDataSource object
*  that can be used to cache JDBC connections.
*
*  <blockquote><pre>
*  // Create a data source for making the connection.
*  AS400JDBCConnectionPoolDataSource dataSource = new AS400JDBCConnectionPoolDataSource("myAS400");
*  datasource.setUser("myUser");
*  datasource.setPassword("MYPWD");
*
*  // Get the PooledConnection.
*  PooledConnection pooledConnection = datasource.getPooledConnection();
*  </pre></blockquote>
**/
public class AS400JDBCConnectionPoolDataSource extends AS400JDBCDataSource implements ConnectionPoolDataSource, Referenceable, Serializable
{
    static final String copyright = "Copyright (C) 1997-2010 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;

    /**
    *  Constructs a default AS400JDBCConnectionPoolDataSource object.
    **/
    public AS400JDBCConnectionPoolDataSource()
    {
        super();
    }

    public AS400JDBCConnectionPoolDataSource(AS400 as400) {
      super(as400); 
    }
    /**
    *  Constructs an AS400JDBCConnectionPoolDataSource with the specified <i>serverName</i>.
    *  @param serverName The IBM i system name.
    **/
    public AS400JDBCConnectionPoolDataSource(String serverName)
    {
        super(serverName);
        //@B2D initializeTransient();    //@A2A
    }

    /**
    *  Constructs an AS400JDBCConnectionPoolDataSource with the specified signon information.
    *  @param serverName The IBM i system name.
    *  @param user The user id.
    *  @param password The password.
    *  @deprecated Use  AS400JDBCConnectionPoolDataSource(String serverName, String user, char[] password) instead.
    **/
    public AS400JDBCConnectionPoolDataSource(String serverName, String user, String password)
    {
        super(serverName, user, password);
    }

      /**
    *  Constructs an AS400JDBCConnectionPoolDataSource with the specified signon information.
    *  @param serverName The IBM i system name.
    *  @param user The user id.
    *  @param password The password.
    *  
    **/
    public AS400JDBCConnectionPoolDataSource(String serverName, String user, char[] password)
    {
        super(serverName, user, password);
    }


    /**
    *  Constructs an AS400JDBCConnectionPoolDataSource from the specified Reference
    *  @param reference to retrieve DataSource properties from
    **/
    AS400JDBCConnectionPoolDataSource(Reference reference) {
        super(reference);
    }

    /**
  *  Returns a pooled connection that is connected to the IBM i system.
  *  @return A pooled connection.
  *  @exception SQLException If a database error occurs.
  **/
    public PooledConnection getPooledConnection() throws SQLException
    {
        PooledConnection pc = new AS400JDBCPooledConnection(getConnection());

        log("PooledConnection created");
        return pc;
    }

        /**
    *  Returns a pooled connection that is connected to the IBM i system.
    *  @param user The userid for the connection.
    *  @param password The password for the connection.
    *  @return A pooled connection.
    *  @exception SQLException If a database error occurs.
    * 
    **/
    public PooledConnection getPooledConnection(String user, char[] password) throws SQLException
    {
        PooledConnection pc = new AS400JDBCPooledConnection(getConnection(user,password));

        log("PooledConnection created");
        return pc;
    }
    /**
    *  Returns a pooled connection that is connected to the IBM i system.
    *  @param user The userid for the connection.
    *  @param password The password for the connection.
    *  @return A pooled connection.
    *  @exception SQLException If a database error occurs.
    *  @deprecated Use getPooledConnection(String user, char[] password) instead. 
    **/
    public PooledConnection getPooledConnection(String user, String password) throws SQLException
    {
        PooledConnection pc = new AS400JDBCPooledConnection(getConnection(user,password));

        log("PooledConnection created");
        return pc;

    }

    /**
    *  Returns the Reference object for the data source object.
    *  This is used by JNDI when bound in a JNDI naming service.
    *  Contains the information necessary to reconstruct the data source 
    *  object when it is later retrieved from JNDI via an object factory.
    *
    *  @return A Reference object for the data source object.
    *  @exception NamingException If a naming error occurs resolving the object.
    **/
    public Reference getReference() throws NamingException
    {
        Reference ref = new Reference(this.getClass().getName(),
                                      "com.ibm.as400.access.AS400JDBCObjectFactory", 
                                      null);

        Reference dsRef = super.getReference();
        for (int i=0; i< dsRef.size(); i++)
            ref.add( dsRef.get(i) );

        return ref;
    }




    /**
    *  Deserializes and initializes transient data.
    **/
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        //@B2D initializeTransient();  //@A2A
    }
   
    protected String[] getValidWrappedList()
    {
        return new String[] {  "com.ibm.as400.access.AS400JDBCConnectionPoolDataSource", "java.sql.ConnectionPoolDataSource" };
    }
}

