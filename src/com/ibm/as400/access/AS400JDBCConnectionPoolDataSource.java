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

    private transient AS400JDBCConnectionPool connectionPool_;  //@A2A

    private int initialPoolSize_ = 0; //@B0A
    private int maxIdleTime_     = 3600; //@B0A
    private int maxPoolSize_     = 0; //@B0A
    //private int maxStatements_   = 0; //@B0A - Ignore. Use package caching instead.
    private int minPoolSize_     = 0; //@B0A
    private int propertyCycle_   = 300; //@B0A

    private boolean connected_ = false; //@B0A

    /**
    *  Constructs a default AS400JDBCConnectionPoolDataSource object.
    **/
    public AS400JDBCConnectionPoolDataSource()
    {
        super();
        initializeTransient();    //@A2A
    }

    /**
    *  Constructs an AS400JDBCConnectionPoolDataSource with the specified <i>serverName</i>.
    *  @param serverName The name of the AS/400 server.
    **/
    public AS400JDBCConnectionPoolDataSource(String serverName)
    {
        super(serverName);
        initializeTransient();    //@A2A
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
        initializeTransient();    //@A2A
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
        initializeTransient();    //@A2A
    }


    //@B0A
    // It would be nice to have a separate lock per property, that way we don't
    // bottleneck the entire object when one property is set, but it's easier to
    // just synchronize the methods and be done with it.
    //
    // Also, there are nasty circular references between AS400JDBCConnectionPool
    // and AS400JDBCConnectionPoolDataSource. So watch out.
    private synchronized void connect() throws ConnectionPoolException
    {
        if (connected_) return;
        if (minPoolSize_ > 0 && maxPoolSize_ > 0 && minPoolSize_ > maxPoolSize_)
        {
            if (Trace.isTraceOn()) Trace.log(Trace.ERROR, "Conflicting pool sizes: Min = "+minPoolSize_+"  Max = "+maxPoolSize_);
            throw new ConnectionPoolException(ConnectionPoolException.CONFLICTING_POOL_SIZES);
        }
        connectionPool_.setMinimumPoolSize(minPoolSize_);
        connectionPool_.setCleanupInterval(propertyCycle_*1000);
        connectionPool_.setMaxConnections(maxPoolSize_ == 0 ? -1 : maxPoolSize_);
        connectionPool_.setMaxInactivity(maxIdleTime_ == 0 ? -1 : maxIdleTime_*1000);
        //connectionPool_.setMaxStatements(maxStatements_);
        if (initialPoolSize_ > 0)                     //@B1A
        {
            connectionPool_.fill(initialPoolSize_);  
        }
        connected_ = true;
    }


    //@B0A
    /**
     * Returns the number of connections this pool contains when it is created.
     * @return The number of pooled connections. The default value is 0.
    **/
    public int getInitialPoolSize()
    {
        return initialPoolSize_;
    }


    //@B0A
    /**
     * Returns the maximum amount of time (in seconds) that a pooled
     * connection in this pool is allowed to remain idle before it is
     * automatically closed. A value of 0 indicates pooled connections
     * are never automatically closed.
     * @return The maximum idle time for a pooled connection in seconds.
     * The default value is 1 hour (3600 seconds).
    **/
    public int getMaxIdleTime()
    {
        return maxIdleTime_;
    }


    //@B0A
    /**
     * Returns the maximum number of connections this connection pool
     * contains. A value of 0 indicates there is no maximum.
     * @return The maximum number of connections in the pool. The default
     * value is 0 (no maximum).
    **/
    public int getMaxPoolSize()
    {
        return maxPoolSize_;
    }


    //@B0A
    /**
     * Returns the maximum number of statements this connection pool
     * should keep open. A value of 0 indicates that no statements
     * will be cached.
     * @return The maximum number of cached open statements. The default
     * value is 0 (no caching).
    **/
    // Note: We don't support statement caching this way.
    //       That's what package caching is for.
    //  public int getMaxStatements()
    //  {
    //    return maxStatements_;
    //  }


    //@B0A
    /**
     * Returns the minimum number of connections this connection pool
     * contains. A value of 0 indicates there is no minimum and connections
     * are created as they are needed.
     * @return The minimum number of available connections in the pool. The
     * default value is 0.
    **/
    public int getMinPoolSize()
    {
        return minPoolSize_;
    }


    /**
  *  Returns a pooled connection that is connected to the server.
  *  @return A pooled connection.
  *  @exception SQLException If a database error occurs.
  **/
    public PooledConnection getPooledConnection() throws SQLException
    {
        //Get a connection from the connection pool.
        PooledConnection pc = null;  //@A2A
        try
        {     //@A2A
            connect(); //@B0A
            pc = connectionPool_.getPooledConnection(); //@A2C

            log("PooledConnection created");
            return pc;
        }
        catch (ConnectionPoolException cpe)       //@A2A
        {
            //@A2A
            JDError.throwSQLException (JDError.EXC_INTERNAL, cpe);  //@A2A
        }
        return pc; //@A2M
    }

    /**
    *  Returns a pooled connection that is connected to the server.
    *  @param user The userid for the connection.
    *  @param password The password for the connection.
    *  @return A pooled connection.
    *  @exception SQLException If a database error occurs.
    **/
    public PooledConnection getPooledConnection(String user, String password) throws SQLException
    {
        PooledConnection pc = null;  //@A2A
        try
        {  //@A2A
            connect(); //@B0A
            // Set user and password if user has not been set in the datasource yet.
            if (getUser().equals(""))                 //@A2A
            {
                //@A2A
                setUser(user);          //@A2A						
                setPassword(password);                //@A2A
            }             //@A2A
            // If the user specified is not equal to the user of the datasource, 
            // throw an ExtendedIllegalStateException. 
            user = user.toUpperCase();                      //@A2A
            if (!(getUser().equals(user)))                      //@A2A
            {
                //@A2A
                Trace.log(Trace.ERROR, "User in data source already set.");           //@A2A
                throw new ExtendedIllegalStateException("user", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);    //@A2A   
            }                           //@A2A
            pc = connectionPool_.getPooledConnection(); //@A2C

            log("PooledConnection created");
        }
        catch (ConnectionPoolException cpe)       //@A2A
        {
            //@A2A
            JDError.throwSQLException (JDError.EXC_INTERNAL, cpe);  //@A2A
        }
        return pc; //@A2M      					        
    }


    //@B0A - Is it OK to have a maintenance thread according to the EJB spec?
    //       Don't we have to run without creating additional threads?
    /**
     * Returns the interval (in seconds) between runs of this pool's
     * maintenance thread. The maintenance thread enforces this pool's
     * connections and statements so that they conform to the specified
     * minimum and maximum pool sizes, idle time, and maximum number of
     * open statements.
     * @return The number of seconds that this pool should wait before enforcing
     * its properties. The default value is 5 minutes (300 seconds).
    **/
    public int getPropertyCycle()
    {
        return propertyCycle_;
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


    //@B0A
    /**
     * Sets the number of connections this pool contains when it is created.
     * @param initialPoolSize The number of pooled connections. Valid values
     * are 0 or greater.
    **/
    public synchronized void setInitialPoolSize(int initialPoolSize)
    {
        String property = "initialPoolSize";
        if (initialPoolSize < 0)
        {
            throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        if (!connected_)
        {
            Integer old = new Integer(initialPoolSize_);
            initialPoolSize_ = initialPoolSize;
            changes_.firePropertyChange(property, old, new Integer(initialPoolSize_));
        }
        else
        {
            throw new ExtendedIllegalStateException(property, ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }
    }


    //@B0A
    /**
     * Sets the maximum amount of time (in seconds) that a pooled
     * connection in this pool is allowed to remain idle before it is
     * automatically closed. A value of 0 indicates pooled connections
     * are never automatically closed.
     * @param maxIdleTime The maximum idle time for a pooled connection in seconds.
     * Valid values are 0 or greater.
    **/
    public synchronized void setMaxIdleTime(int maxIdleTime)
    {
        String property = "maxIdleTime";
        if (maxIdleTime < 0)
        {
            throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        if (!connected_)
        {
            Integer old = new Integer(maxIdleTime_);
            maxIdleTime_ = maxIdleTime;
            changes_.firePropertyChange(property, old, new Integer(maxIdleTime_));
        }
        else
        {
            throw new ExtendedIllegalStateException(property, ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }
    }


    //@B0A
    /**
     * Sets the maximum number of connections this connection pool
     * contains. A value of 0 indicates there is no maximum.
     * @param maxPoolSize The maximum number of connections in this pool.
     * Valid values are 0 or greater.
    **/
    public synchronized void setMaxPoolSize(int maxPoolSize)
    {
        String property = "maxPoolSize";
        if (maxPoolSize < 0)
        {
            throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        if (!connected_)
        {
            Integer old = new Integer(maxPoolSize_);
            maxPoolSize_ = maxPoolSize;
            changes_.firePropertyChange(property, old, new Integer(maxPoolSize_));
        }
        else
        {
            throw new ExtendedIllegalStateException(property, ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }
    }


    //@B0A
    /**
     * Sets the maximum number of statements this connection pool
     * should keep open. A value of 0 indicates that no statements
     * will be cached.
     * @param maxStatements The maximum number of cached open statements.
     * Valid values are 0 or greater.
    **/
    // Note: Use package caching to get statement caching.
    /*  public synchronized void setMaxStatements(int maxStatements)
      {
        String property = "maxStatements";
        if (maxStatements < 0)
        {
          throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        if (!connected_)
        {
          Integer old = new Integer(maxStatements_);
          maxStatements_ = maxStatements;
          changes_.firePropertyChange(property, old, new Integer(maxStatements_));
        }
        else
        {
          throw new ExtendedIllegalStateException(property, ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }
      }
    */

    //@B0A
    /**
     * Sets the minimum number of connections this connection pool
     * contains. A value of 0 indicates there is no minimum and connections
     * are created as they are needed.
     * @param minPoolSize The minimum number of available connections in the pool.
     * Valid values are 0 or greater.
    **/
    public synchronized void setMinPoolSize(int minPoolSize)
    {
        String property = "minPoolSize";
        if (minPoolSize < 0)
        {
            throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        if (!connected_)
        {
            Integer old = new Integer(minPoolSize_);
            minPoolSize_ = minPoolSize;
            changes_.firePropertyChange(property, old, new Integer(minPoolSize_));
        }
        else
        {
            throw new ExtendedIllegalStateException(property, ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }
    }


    //@B0A - Is it OK to have a maintenance thread according to the EJB spec?
    //       Don't we have to run without creating additional threads?
    /**
     * Sets the interval (in seconds) between runs of this pool's
     * maintenance thread. The maintenance thread enforces this pool's
     * connections and statements so that they conform to the specified
     * minimum and maximum pool sizes, idle time, and maximum number of
     * open statements. A value of 0 indicates that a maintenance thread
     * should not be created.
     * @param The number of seconds that this pool should wait before enforcing
     * its properties. Valid values are 0 or greater.
    **/
    public synchronized void setPropertyCycle(int propertyCycle)
    {
        String property = "propertyCycle";
        if (propertyCycle < 0)
        {
            throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        if (!connected_)
        {
            Integer old = new Integer(propertyCycle_);
            propertyCycle_ = propertyCycle;
            changes_.firePropertyChange(property, old, new Integer(propertyCycle_));
        }
        else
        {
            throw new ExtendedIllegalStateException(property, ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }
    }
}

