///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCConnectionPoolDataSource.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
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
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;

    //@B2D private transient AS400JDBCConnectionPool connectionPool_;  //@A2A

    //@B2D private int initialPoolSize_ = 0; //@B0A
    //@B2D private int maxIdleTime_     = 3600; //@B0A
    //@B2D private int maxPoolSize_     = 0; //@B0A
    //@B2D //private int maxStatements_   = 0; //@B0A - Ignore. Use package caching instead.
    //@B2D private int minPoolSize_     = 0; //@B0A
    //@B2D private int propertyCycle_   = 300; //@B0A

    /**
    *  Constructs a default AS400JDBCConnectionPoolDataSource object.
    **/
    public AS400JDBCConnectionPoolDataSource()
    {
        super();
        //@B2D initializeTransient();    //@A2A
    }

    /**
    *  Constructs an AS400JDBCConnectionPoolDataSource with the specified <i>serverName</i>.
    *  @param serverName The i5/OS system name.
    **/
    public AS400JDBCConnectionPoolDataSource(String serverName)
    {
        super(serverName);
        //@B2D initializeTransient();    //@A2A
    }

    /**
    *  Constructs an AS400JDBCConnectionPoolDataSource with the specified signon information.
    *  @param serverName The i5/OS system name.
    *  @param user The user id.
    *  @param password The password.
    **/
    public AS400JDBCConnectionPoolDataSource(String serverName, String user, String password)
    {
        super(serverName, user, password);
        //@B2D initializeTransient();    //@A2A
    }

    //@A1A
    /**
    *  Constructs an AS400JDBCConnectionPoolDataSource with the specified signon information
    *  to use for SSL communications with the system.
    *  @param serverName The i5/OS system name.
    *  @param user The user id.
    *  @param password The password.
    *  @param keyRingName The key ring class name to be used for SSL communications with the system.
    *  @param keyRingPassword The password for the key ring class to be used for SSL communications with the system.
    **/
    public AS400JDBCConnectionPoolDataSource(String serverName, String user, String password,
                                             String keyRingName, String keyRingPassword)
    {
        super(serverName, user, password, keyRingName, keyRingPassword);
        //@B2D initializeTransient();    //@A2A
    }

    // @F0A - added the following constructor to avoid some object construction
    /**
    *  Constructs an AS400JDBCConnectionPoolDataSource from the specified Reference
    *  @param reference to retrieve DataSource properties from
    **/
    AS400JDBCConnectionPoolDataSource(Reference reference) {
        super(reference);
    }


    //@B2D //@B0A
    //@B2D /**
    //@B2D  * Returns the number of connections this pool contains when it is created.
    //@B2D  * @return The number of pooled connections. The default value is 0.
    //@B2D **/
    //@B2D public int getInitialPoolSize()
    //@B2D {
    //@B2D     return initialPoolSize_;
    //@B2D }


    //@B2D //@B0A
    //@B2D /**
    //@B2D  * Returns the maximum amount of time (in seconds) that a pooled
    //@B2D  * connection in this pool is allowed to remain idle before it is
    //@B2D  * automatically closed. A value of 0 indicates pooled connections
    //@B2D  * are never automatically closed.
    //@B2D  * @return The maximum idle time for a pooled connection in seconds.
    //@B2D  * The default value is 1 hour (3600 seconds).
    //@B2D **/
    //@B2D public int getMaxIdleTime()
    //@B2D {
    //@B2D     return maxIdleTime_;
    //@B2D }


    //@B2D //@B0A
    //@B2D /**
    //@B2D  * Returns the maximum number of connections this connection pool
    //@B2D  * contains. A value of 0 indicates there is no maximum.
    //@B2D  * @return The maximum number of connections in the pool. The default
    //@B2D  * value is 0 (no maximum).
    //@B2D **/
    //@B2D public int getMaxPoolSize()
    //@B2D {
    //@B2D     return maxPoolSize_;
    //@B2D }


    //@B2D //@B0A
    //@B2D /**
    //@B2D  * Returns the maximum number of statements this connection pool
    //@B2D  * should keep open. A value of 0 indicates that no statements
    //@B2D  * will be cached.
    //@B2D  * @return The maximum number of cached open statements. The default
    //@B2D  * value is 0 (no caching).
    //@B2D **/
    //@B2D // Note: We don't support statement caching this way.
    //@B2D //       That's what package caching is for.
    //@B2D //  public int getMaxStatements()
    //@B2D //  {
    //@B2D //    return maxStatements_;
    //@B2D //  }


    //@B2D //@B0A
    //@B2D /**
    //@B2D  * Returns the minimum number of connections this connection pool
    //@B2D  * contains. A value of 0 indicates there is no minimum and connections
    //@B2D  * are created as they are needed.
    //@B2D  * @return The minimum number of available connections in the pool. The
    //@B2D  * default value is 0.
    //@B2D **/
    //@B2D public int getMinPoolSize()
    //@B2D {
    //@B2D     return minPoolSize_;
    //@B2D }


    /**
  *  Returns a pooled connection that is connected to the i5/OS system.
  *  @return A pooled connection.
  *  @exception SQLException If a database error occurs.
  **/
    public PooledConnection getPooledConnection() throws SQLException
    {
        PooledConnection pc = new AS400JDBCPooledConnection(getConnection());

        log("PooledConnection created");
        return pc;

        //@B2D  //Get a connection from the connection pool.
        //@B2D  PooledConnection pc = null;  //@A2A
        //@B2D  try
        //@B2D  {     //@A2A
        //@B2D      connect(); //@B0A
        //@B2D      pc = connectionPool_.getPooledConnection(); //@A2C
        //@B2D 
        //@B2D      log("PooledConnection created");
        //@B2D      return pc;
        //@B2D  }
        //@B2D  catch (ConnectionPoolException cpe)       //@A2A
        //@B2D  {
        //@B2D      //@A2A
        //@B2D      JDError.throwSQLException (JDError.EXC_INTERNAL, cpe);  //@A2A
        //@B2D  }
        //@B2D  return pc; //@A2M
    }

    /**
    *  Returns a pooled connection that is connected to the i5/OS system.
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

        //@B2D PooledConnection pc = null;  //@A2A
        //@B2D try
        //@B2D {  //@A2A
        //@B2D     connect(); //@B0A
        //@B2D     // Set user and password if user has not been set in the datasource yet.
        //@B2D     if (getUser().equals(""))                 //@A2A
        //@B2D     {
        //@B2D         //@A2A
        //@B2D         setUser(user);          //@A2A						
        //@B2D         setPassword(password);                //@A2A
        //@B2D     }             //@A2A
        //@B2D     // If the user specified is not equal to the user of the datasource, 
        //@B2D     // throw an ExtendedIllegalStateException. 
        //@B2D     user = user.toUpperCase();                      //@A2A
        //@B2D     if (!(getUser().equals(user)))                      //@A2A
        //@B2D     {
        //@B2D         //@A2A
        //@B2D         Trace.log(Trace.ERROR, "User in data source already set.");           //@A2A
        //@B2D         throw new ExtendedIllegalStateException("user", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);    //@A2A   
        //@B2D     }                           //@A2A
        //@B2D     pc = connectionPool_.getPooledConnection(); //@A2C
        //@B2D 
        //@B2D     log("PooledConnection created");
        //@B2D }
        //@B2D catch (ConnectionPoolException cpe)       //@A2A
        //@B2D {
        //@B2D     //@A2A
        //@B2D     JDError.throwSQLException (JDError.EXC_INTERNAL, cpe);  //@A2A
        //@B2D }
        //@B2D return pc; //@A2M      					        
    }


    //@B2D //@B0A - Is it OK to have a maintenance thread according to the EJB spec?
    //@B2D //       Don't we have to run without creating additional threads?
    //@B2D /**
    //@B2D  * Returns the interval (in seconds) between runs of this pool's
    //@B2D  * maintenance thread. The maintenance thread enforces this pool's
    //@B2D  * connections and statements so that they conform to the specified
    //@B2D  * minimum and maximum pool sizes, idle time, and maximum number of
    //@B2D  * open statements.
    //@B2D  * @return The number of seconds that this pool should wait before enforcing
    //@B2D  * its properties. The default value is 5 minutes (300 seconds).
    //@B2D **/
    //@B2D public int getPropertyCycle()
    //@B2D {
    //@B2D     return propertyCycle_;
    //@B2D }


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


    //@B2D //@A2A
    //@B2D /**
    //@B2D  *  Initializes the transient data for object de-serialization.
    //@B2D  **/
    //@B2D private void initializeTransient()
    //@B2D {
    //@B2D     connectionPool_ = new AS400JDBCConnectionPool(this);
    //@B2D }


    /**
    *  Deserializes and initializes transient data.
    **/
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        //@B2D initializeTransient();  //@A2A
    }


    //@B2D //@B0A
    //@B2D /**
    //@B2D  * Sets the number of connections this pool contains when it is created.
    //@B2D  * @param initialPoolSize The number of pooled connections. Valid values
    //@B2D  * are 0 or greater.
    //@B2D **/
    //@B2D public void setInitialPoolSize(int initialPoolSize)
    //@B2D {
    //@B2D     String property = "initialPoolSize";
    //@B2D     if (initialPoolSize < 0)
    //@B2D     {
    //@B2D         throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    //@B2D     }
    //@B2D     if (!connected_)
    //@B2D     {
    //@B2D         Integer old = new Integer(initialPoolSize_);
    //@B2D         initialPoolSize_ = initialPoolSize;
    //@B2D         changes_.firePropertyChange(property, old, new Integer(initialPoolSize_));
    //@B2D     }
    //@B2D     else
    //@B2D     {
    //@B2D         throw new ExtendedIllegalStateException(property, ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    //@B2D     }
    //@B2D }


    //@B2D //@B0A
    //@B2D /**
    //@B2D  * Sets the maximum amount of time (in seconds) that a pooled
    //@B2D  * connection in this pool is allowed to remain idle before it is
    //@B2D  * automatically closed. A value of 0 indicates pooled connections
    //@B2D  * are never automatically closed.
    //@B2D  * @param maxIdleTime The maximum idle time for a pooled connection in seconds.
    //@B2D  * Valid values are 0 or greater.
    //@B2D **/
    //@B2D public void setMaxIdleTime(int maxIdleTime)
    //@B2D {
    //@B2D     String property = "maxIdleTime";
    //@B2D     Integer old = new Integer(maxIdleTime_);
    //@B2D     maxIdleTime_ = maxIdleTime;
    //@B2D     changes_.firePropertyChange(property, old, new Integer(maxIdleTime_));
    //@B2D }


    //@B2D //@B0A
    //@B2D /**
    //@B2D  * Sets the maximum number of connections this connection pool
    //@B2D  * contains. A value of 0 indicates there is no maximum.
    //@B2D  * @param maxPoolSize The maximum number of connections in this pool.
    //@B2D  * Valid values are 0 or greater.
    //@B2D **/
    //@B2D public void setMaxPoolSize(int maxPoolSize)
    //@B2D {
    //@B2D     String property = "maxPoolSize";
    //@B2D     Integer old = new Integer(maxPoolSize_);
    //@B2D     maxPoolSize_ = maxPoolSize;
    //@B2D     changes_.firePropertyChange(property, old, new Integer(maxPoolSize_));
    //@B2D }


    //@B2D //@B0A
    //@B2D /**
    //@B2D  * Sets the maximum number of statements this connection pool
    //@B2D  * should keep open. A value of 0 indicates that no statements
    //@B2D  * will be cached.
    //@B2D  * @param maxStatements The maximum number of cached open statements.
    //@B2D  * Valid values are 0 or greater.
    //@B2D **/
    //@B2D // Note: Use package caching to get statement caching.
    //@B2D /*  public void setMaxStatements(int maxStatements)
    //@B2D   {
    //@B2D     String property = "maxStatements";
    //@B2D       Integer old = new Integer(maxStatements_);
    //@B2D       maxStatements_ = maxStatements;
    //@B2D       changes_.firePropertyChange(property, old, new Integer(maxStatements_));
    //@B2D   }
    //@B2D */


    //@B2D //@B0A
    //@B2D /**
    //@B2D  * Sets the minimum number of connections this connection pool
    //@B2D  * contains. A value of 0 indicates there is no minimum and connections
    //@B2D  * are created as they are needed.
    //@B2D  * @param minPoolSize The minimum number of available connections in the pool.
    //@B2D  * Valid values are 0 or greater.
    //@B2D **/
    //@B2D public void setMinPoolSize(int minPoolSize)
    //@B2D {
    //@B2D     String property = "minPoolSize";
    //@B2D         Integer old = new Integer(minPoolSize_);
    //@B2D         minPoolSize_ = minPoolSize;
    //@B2D         changes_.firePropertyChange(property, old, new Integer(minPoolSize_));
    //@B2D }


    //@B2D //@B0A - Is it OK to have a maintenance thread according to the EJB spec?
    //@B2D //       Don't we have to run without creating additional threads?
    //@B2D /**
    //@B2D  * Sets the interval (in seconds) between runs of this pool's
    //@B2D  * maintenance thread. The maintenance thread enforces this pool's
    //@B2D  * connections and statements so that they conform to the specified
    //@B2D  * minimum and maximum pool sizes, idle time, and maximum number of
    //@B2D  * open statements. A value of 0 indicates that a maintenance thread
    //@B2D  * should not be created.
    //@B2D  * @param The number of seconds that this pool should wait before enforcing
    //@B2D  * its properties. Valid values are 0 or greater.
    //@B2D **/
    //@B2D public void setPropertyCycle(int propertyCycle)
    //@B2D {
    //@B2D     String property = "propertyCycle";
    //@B2D     Integer old = new Integer(propertyCycle_);
    //@B2D     propertyCycle_ = propertyCycle;
    //@B2D     changes_.firePropertyChange(property, old, new Integer(propertyCycle_));
    //@B2D }
}

