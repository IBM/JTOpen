///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400JDBCConnectionPoolDataSource.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
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
*  <pre><blockquote>
*  // Create a data source for making the connection.
*  AS400JDBCConnectionPoolDataSource dataSource = new AS400JDBCConnectionPoolDataSource("myAS400");
*  datasource.setUser("myUser");
*  datasource.setPassword("MYPWD");
*
*  // Get the PooledConnection.
*  PooledConnection pooledConnection = datasource.getPooledConnection();
*  </blockquote></pre>
**/
public class AS400JDBCConnectionPoolDataSource extends AS400JDBCDataSource implements ConnectionPoolDataSource, Referenceable, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;


   /**
   *  Constructs a default AS400JDBCConnectionPoolDataSource object.
   **/
   public AS400JDBCConnectionPoolDataSource()
   {
      super();
   }

   /**
   *  Constructs an AS400JDBCConnectionPoolDataSource with the specified <i>serverName</i>.
   *  @param serverName The name of the AS/400 server.
   **/
   public AS400JDBCConnectionPoolDataSource(String serverName)
   {
      super(serverName);
   }

   /**
   *  Constructs an AS400JDBCConnectionPoolDataSource with the specified signon information.
   *  @param serverName The AS/400 system name.
   *  @param user The user id.
   *  @param password The password.
   **/
   public AS400JDBCConnectionPoolDataSource(String serverName, String user, String password)
   {
      super(serverName, user, password);
   }

   //@A1A
   /**
   *  Constructs an AS400JDBCConnectionPoolDataSource with the specified signon information
   *  to use for SSL communications with the server.
   *  @param serverName The AS/400 system name.
   *  @param user The user id.
   *  @param password The password.
   *  @param keyRingName The key ring class name to be used for SSL communications with the server.
   *  @param keyRingPassword The password for the key ring class to be used for SSL communications with the server.
   **/
   public AS400JDBCConnectionPoolDataSource(String serverName, String user, String password,
					    String keyRingName, String keyRingPassword)
   {
      super(serverName, user, password, keyRingName, keyRingPassword);
   }

   /**
   *  Returns a pooled connection to the AS/400.
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
   *  Returns a pooled connection to the AS/400.
   *  @param user The userid for the connection.
   *  @param password The password for the connection.
   *  @return A pooled connection.
   *  @exception SQLException If a database error occurs.
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
   }

}
