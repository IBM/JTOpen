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
    private transient AS400JDBCConnectionPool connectionPool_;	//@A2A


   /**
   *  Constructs a default AS400JDBCConnectionPoolDataSource object.
   **/
   public AS400JDBCConnectionPoolDataSource()
   {
      super();
      initializeTransient();	  //@A2A
   }

   /**
   *  Constructs an AS400JDBCConnectionPoolDataSource with the specified <i>serverName</i>.
   *  @param serverName The name of the AS/400 server.
   **/
   public AS400JDBCConnectionPoolDataSource(String serverName)
   {
      super(serverName);
      initializeTransient();	  //@A2A
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
      initializeTransient();	  //@A2A
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
      initializeTransient();	  //@A2A
   }

   /**
   *  Returns a pooled connection to the AS/400.
   *  @return A pooled connection.
   *  @exception SQLException If a database error occurs.
   **/
   public PooledConnection getPooledConnection() throws SQLException
   {
     //Get a connection from the connection pool.
     PooledConnection pc = null;  //@A2A
     try			//@A2A
     {  															
       pc = connectionPool_.getPooledConnection();	//@A2C

       log("PooledConnection created");
       return pc;
     }
     catch (ConnectionPoolException cpe)				//@A2A
     {						//@A2A
       JDError.throwSQLException (JDError.EXC_INTERNAL, cpe);	//@A2A
     }
     return pc; //@A2M
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
     PooledConnection pc = null;  //@A2A
     try  //@A2A
     {   
       // Set user and password if user has not been set in the datasource yet.
       if (getUser().equals(""))									//@A2A
       {														//@A2A
         setUser(user);					//@A2A						
         setPassword(password);								//@A2A
       }							//@A2A
       // If the user specified is not equal to the user of the datasource, 
       // throw an ExtendedIllegalStateException. 
       user = user.toUpperCase();											//@A2A
       if (!(getUser().equals(user)))											//@A2A
       {														//@A2A
         Trace.log(Trace.ERROR, "User in data source already set.");						//@A2A
         throw new ExtendedIllegalStateException("user", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);    //@A2A   
       }														//@A2A
       pc = connectionPool_.getPooledConnection();	//@A2C

       log("PooledConnection created");
     }
     catch (ConnectionPoolException cpe)				//@A2A
     {								//@A2A
       JDError.throwSQLException (JDError.EXC_INTERNAL, cpe);	//@A2A
     } 
     return pc; //@A2M      					        
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

	//@A2A
	/**
	 *  Initializes the transient data for object de-serialization.
	 **/
	private void initializeTransient()
	{
		connectionPool_ = new AS400JDBCConnectionPool(this);
	}


   /**
   *  Deserializes and initializes transient data.
   **/
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      initializeTransient();  //@A2A
   }

}
