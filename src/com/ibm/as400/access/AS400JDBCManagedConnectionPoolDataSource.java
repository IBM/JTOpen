///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCManagedConnectionPoolDataSource.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2005-2005 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.*;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;


// TBD - Add link from AS400JDBCConnectionPoolDataSource to this class.
/**
 This implementation of <tt>javax.sql.ConnectionPoolDataSource</tt> can be used in conjunction with {@link AS400JDBCManagedDataSource AS400JDBCManagedDataSource} to produce PooledConnection objects ({@link AS400JDBCPooledConnection AS400JDBCPooledConnection}) that are managed by the Toolbox's built-in connection pooling manager.
 <p>
 A ConnectionPoolDataSource is a factory for PooledConnection objects.
 An object that implements the ConnectionPoolDataSource interface will typically be registered with a naming service that is based on the Java Naming and Directory Interface (JNDI).
 <p>
 The operations on a ConnectionPoolDataSource class are completely internal to a driver implementation; the ConnectionPoolDataSource interface is not part of the API typically used by Java application programmers.  Driver vendors use it in their implementation of connection pooling.  Application programmers specify a DataSource class in their code to get a connection.  However, since users/database administrators must register the corresponding ConnectionPoolDataSource with JNDI, the class needs to be public.  Users must be able to call the different getters/setters to set up their connection pooling environment when they register the ConnectionPoolDataSource.  Drivers then provide DataSource classes that implement javax.sql.DataSource, and the user will use this data source in their code.
 <p>
 Design note: This class extends AS400JDBCManagedDataSource, solely in order to exploit shared implementation.  This class should <em>not</em> be used as a "kind of" AS400JDBCManagedDataSource.
 <p>
 <em>Caution:</em> To avoid the pitfalls of "double-managed" pools, do not use this class in conjunction with a separate connection pool manager, such as that available in WebSphere.  When a separate pool manager is provided, use {@link AS400JDBCConnectionPoolDataSource AS400JDBCConnectionPoolDataSource} instead.

 @see AS400JDBCDataSource
 @see AS400JDBCConnectionPoolDataSource
 @see AS400JDBCXADataSource
 **/
public class AS400JDBCManagedConnectionPoolDataSource extends AS400JDBCManagedDataSource implements ConnectionPoolDataSource, Referenceable, Serializable
{
  private static final String copyright = "Copyright (C) 2005-2006 International Business Machines Corporation and others.";

  // Note to developer: If you add a new property (that's not also in the superclass), remember to add a clause for the property to both the getReference() method and the constructor that takes a Reference argument.

  // Note:
  // There are three kinds of data sources:
  //  1. Basic DataSource (javax.sql.DataSource).  Provides a getConnection() method that returns an instance of java.sql.Connection.
  //  2. Data source class implemented to provide connection pooling (javax.sql.ConnectionPoolDataSource).  Provides a getPooledConnection() method that returns an instance of javax.sql.PooledConnection.
  //  3. Data Source class implemented to provide distributed transactions (javax.sql.XADataSource).  Provides a getXAConnection() method that returns an instance of javax.sql.XAConnection.

  // Note:
  // Refer to pages 570 - 574 of the JDBC Tutorial.  The book does say that a DataSource implementation that supports distributed transactions is almost always implemented to support connection pooling as well (most likely why there is a UDBXADataSource on the native side.)  I'll leave it up to you whether or not you want to do an XA version with connection pooling.  We definitely need the ConnectionPoolDataSource version though, as that is what the line item is for.

  // Note:  (From comments in UDBConnectionPoolDataSource)
  //
  // "ConnectionPoolDataSource does not extend DataSource and it is not a superset of the functionality of the DataSource.  It exists to provide pooling functionality to the DataSource user interface.  Therefore, the ConnectionPoolDataSource requires a DataSource in order to do action connection work."


  // Standard JDBC connection pool properties.  (See JDBC Tutorial p. 442, table 14.1)
  // Following are the standard properties that a ConnectionPoolDataSource implementation may set for a PooledConnection object.  An application never uses these properties directly.  An application server that is managing the pool of PooledConnection objects uses these properties to determine how to manage the pool.

  // For consistency, all time-related properties in this class are stored as milliseconds.
  // Note that all xxxPoolSize_ variables refer to the total number of connections ('active', 'available', and 'condemned'), not just available connections.

  private int initialPoolSize_ = 5;// The # of physical connections the pool should contain
                                   // when it is created.
  private int minPoolSize_ = 0;    // The minimum # of physical connections in the pool.
  private int maxPoolSize_ = 0;    // The maximum # of physical connections the pool should contain.
                                   // 0 (zero) indicates no maximum size.
  private long maxIdleTime_ = 3600*1000;
                                   // The # of msecs that a physical connection should remain
                                   // unused in the pool before it is closed; that is, when the
                                   // connection is considered "stale".
                                   // 0 (zero) indicates no time limit.
                                   // See JDBC Tutorial, p. 643, 2nd paragraph.
                                   // Default is 1 hour.
  private long propertyCycle_ = 300*1000;
                                   // The interval, in msecs, that the pool should wait before
                                   // enforcing the policy defined by the values currently assigned
                                   // to these connection pool properties.
                                   // Default is 5 minutes.
  //          maxStatements_;      // The total # of statements the pool should keep open.
                                   // 0 (zero) indicates that the caching of statements is disabled.
                                   // Note: This implementation doesn't cache prepared statements.



// Additional connection pool properties.

  private long maxLifetime_ = 86400*1000;
                                   // The # of msecs that a physical connection should
                                   // remain in the pool before it is closed; that is, when the
                                   // connection is considered "expired".
                                   // 0 (zero) indicates no time limit.
                                   // Default is 24 hours.
  private boolean reuseConnections_ = true; // Re-use connections that have been returned to pool.



  /**
   Constructs a default AS400JDBCManagedConnectionPoolDataSource object.
   **/
  public AS400JDBCManagedConnectionPoolDataSource()
  {
    super();
  }

  /**
   Constructs an AS400JDBCManagedConnectionPoolDataSource with the specified <i>serverName</i>.
   @param serverName The i5/OS system name.
   **/
  public AS400JDBCManagedConnectionPoolDataSource(String serverName)
  {
    super(serverName);
  }

  /**
   Constructs an AS400JDBCManagedConnectionPoolDataSource with the specified signon information.
   @param serverName The i5/OS system name.
   @param user The user id.
   @param password The password.
   **/
  public AS400JDBCManagedConnectionPoolDataSource(String serverName, String user, String password)
  {
    super(serverName, user, password);
  }


  // Note: We do not provide a constructor that takes an AS400 object,
  // because we need to capture the password so we can use it as part of the pool key.


  /**
   Constructs an AS400JDBCManagedConnectionPoolDataSource with the specified signon information
   to use for SSL communications with the i5/OS system.
   @param serverName The i5/OS system name.
   @param user The user id.
   @param password The password.
   @param keyRingName The key ring class name to be used for SSL communications with the system.
   @param keyRingPassword The password for the key ring class to be used for SSL communications with the system.
   **/
  public AS400JDBCManagedConnectionPoolDataSource(String serverName, String user, String password,
                                                  String keyRingName, String keyRingPassword)
  {
    super(serverName, user, password, keyRingName, keyRingPassword);
  }

  /**
   Constructs an AS400JDBCManagedConnectionPoolDataSource from the specified Reference
   @param reference reference to retrieve DataSource properties from
   **/
  AS400JDBCManagedConnectionPoolDataSource(Reference reference)
  {
    super(reference);

    RefAddr refAddr;

    refAddr = reference.get("initialPoolSize");
    if (refAddr != null)
      setInitialPoolSize(Integer.parseInt((String)refAddr.getContent()));

    refAddr = reference.get("maxLifetime");
    if (refAddr != null)
      setMaxLifetime(Integer.parseInt((String)refAddr.getContent()));

    refAddr = reference.get("minPoolSize");
    if (refAddr != null)
      setMinPoolSize(Integer.parseInt((String)refAddr.getContent()));

    refAddr = reference.get("maxPoolSize");
    if (refAddr != null)
      setMaxPoolSize(Integer.parseInt((String)refAddr.getContent()));

    refAddr = reference.get("maxIdleTime");
    if (refAddr != null)
      setMaxIdleTime(Integer.parseInt((String)refAddr.getContent()));

    refAddr = reference.get("propertyCycle");
    if (refAddr != null)
      setPropertyCycle(Integer.parseInt((String)refAddr.getContent()));

    refAddr = reference.get("reuseConnections");
    if (refAddr != null)
      setReuseConnections(Boolean.valueOf((String)refAddr.getContent()).booleanValue());

    // Note: This class does not support the "maxStatements" property.
  }


  /**
   Prints a warning and calls the superclass's method.
   Users are discouraged from calling this method.
   @param logStatistics If true, additional information is logged.
   @return true if connection pool exists and appears healthy; false otherwise.
   @deprecated Use {@link AS400JDBCManagedDataSource#checkPoolHealth(boolean) checkPoolHealth()} instead.
   **/
  public boolean checkPoolHealth(boolean logStatistics)
  {
    logWarning("AS400JDBCManagedConnectionPoolDataSource.checkPoolHealth() is deprecated");
    return super.checkPoolHealth(logStatistics);
  }


  /**
   Prints a warning and calls the superclass's method.
   Users are discouraged from calling this method.
   @deprecated Use {@link AS400JDBCManagedDataSource#closePool() closePool()} instead.
   **/
  public void closePool()
  {
    logWarning("AS400JDBCManagedConnectionPoolDataSource.closePool() is deprecated");
    super.closePool();
  }


  /**
   Prints a warning and calls the superclass's method.
   Users are discouraged from calling this method.
   @return The connection.
   @throws SQLException If a database error occurs.
   @deprecated Use {@link AS400JDBCManagedDataSource#getConnection() getConnection()} instead.
   **/
  public Connection getConnection() throws SQLException
  {
    logWarning("AS400JDBCManagedConnectionPoolDataSource.getConnection() is deprecated");
    return super.getConnection();
  }

  /**
   Prints a warning and calls the superclass's method.
   Users are discouraged from calling this method.
   @param user The database user.
   @param password The database password.
   @return The connection
   @throws SQLException If a database error occurs.
   @deprecated Use {@link AS400JDBCManagedDataSource#getConnection(String,String) getConnection()} instead.
   **/
  public Connection getConnection(String user, String password) throws SQLException
  {
    logWarning("AS400JDBCManagedConnectionPoolDataSource.getConnection() is deprecated");
    return super.getConnection(user, password);
  }


  /**
   Returns the number of physical connections the connection pool should contain when it is created.
   @return The initial number of pooled connections. The default value is 0.
   **/
  public int getInitialPoolSize()
  {
    return initialPoolSize_;
  }


  /**
   Returns the amount of time (in seconds) after which an available pooled
   physical connection is considered "stale" and should be closed.
   A value of 0 indicates pooled connections are never automatically closed.
   @return The maximum idle time for a pooled connection, in seconds.
   The default value is 1 hour.
   **/
  public int getMaxIdleTime()
  {
    return (int)(maxIdleTime_/1000);  // stored internally as milliseconds

    // Design note: In general we prefer to express "duration" values in milliseconds,
    // but the JDBC Tutorial and Reference shows this property in seconds (int).
  }

  /**
   Returns the maximum amount of time (in seconds) after which a
   physical connection is considered to be expired and should be closed.
   A value of 0 indicates in-use connections
   are never automatically closed.
   @return The maximum lifetime for an in-use connection, in seconds.
   The default value is 24 hours.
   **/
  public int getMaxLifetime()
  {
    return (int)(maxLifetime_/1000);  // stored internally as milliseconds

    // Design note: In general we prefer to express "duration" values in milliseconds,
    // but the JDBC Tutorial and Reference shows this property in seconds (int).
  }

  /**
   Returns the maximum number of physical connections that the connection pool
   contains. A value of 0 indicates there is no maximum.
   @return The maximum number of connections in the pool. The default
   value is 0 (no maximum).
   **/
  public int getMaxPoolSize()
  {
    return maxPoolSize_;
  }


  /**
   Returns the minimum number of physical connections that the connection pool
   contains. A value of 0 indicates there is no minimum and connections
   are created as they are needed.
   @return The minimum number of available connections in the pool. The
   default value is 0.
   **/
  public int getMinPoolSize()
  {
    return minPoolSize_;
  }


  // method required by javax.sql.ConnectionPoolDataSource
  /**
   Returns a pooled connection that is connected to the i5/OS system.
   @return A pooled connection.
   @throws SQLException If a database error occurs.
   **/
  public PooledConnection getPooledConnection() throws SQLException
  {
    PooledConnection pc = new AS400JDBCPooledConnection(createPhysicalConnection());
    if (JDTrace.isTraceOn() || log_ != null) logInformation("PooledConnection created");

    return pc;
  }

  // method required by javax.sql.ConnectionPoolDataSource
  /**
   Returns a pooled connection that is connected to the i5/OS system.
   @param user The userid for the connection.
   @param password The password for the connection.
   @return A pooled connection.
   @throws SQLException If a database error occurs.
   **/
  public PooledConnection getPooledConnection(String user, String password) throws SQLException
  {
    PooledConnection pc = new AS400JDBCPooledConnection(createPhysicalConnection(user,password));
    if (JDTrace.isTraceOn() || log_ != null) logInformation("PooledConnection created for user " + user);

    return pc;
  }


  /**
   Returns the interval (in seconds) between runs of the connection pool's
   maintenance thread. The maintenance thread enforces this pool's
   connections and statements so that they conform to the specified
   minimum and maximum pool sizes, idle time, and maximum number of
   open statements.
   @return The number of seconds that this pool should wait before enforcing
   its properties. The default value is 5 minutes.
   **/
  public int getPropertyCycle()
  {
    return (int)(propertyCycle_/1000);  // stored internally as milliseconds

    // Design note: In general we prefer to express "duration" values in milliseconds,
    // but the JDBC Tutorial and Reference shows this property in seconds (int).
  }


  // method required by javax.naming.Referenceable
  /**
   Returns a Reference object for the data source object.
   This is used by JNDI when bound in a JNDI naming service.
   Contains the information necessary to reconstruct the data source
   object when it is later retrieved from JNDI via an object factory.
   *
   @return A Reference object for the data source object.
   @throws NamingException If a naming error occurs resolving the object.
   **/
  public Reference getReference() throws NamingException
  {
    Reference ref = new Reference(this.getClass().getName(),
                                  "com.ibm.as400.access.AS400JDBCObjectFactory",
                                  null);

    ref.add(new StringRefAddr("initialPoolSize", String.valueOf(getInitialPoolSize())));
    ref.add(new StringRefAddr("maxLifetime", String.valueOf(getMaxLifetime())));
    ref.add(new StringRefAddr("minPoolSize", String.valueOf(getMinPoolSize())));
    ref.add(new StringRefAddr("maxPoolSize", String.valueOf(getMaxPoolSize())));
    ref.add(new StringRefAddr("maxIdleTime", String.valueOf(getMaxIdleTime())));
    ref.add(new StringRefAddr("propertyCycle", String.valueOf(getPropertyCycle())));
    ref.add(new StringRefAddr("reuseConnections", String.valueOf(isReuseConnections())));
    // Note: This class does not support the 'maxStatements' property.

    // Add the properties from the associated AS400JDBCManagedDataSource.
    Reference dsRef = super.getReference();
    for (int i=0; i<dsRef.size(); i++)
    {
      ref.add(dsRef.get(i));
    }

    return ref;
  }

  // Used by AS400JDBCManagedDataSource.
  static final boolean isConnectionPoolProperty(String prop)
  {
    return (prop.equals("initialPoolSize") ||
            prop.equals("maxLifetime") ||
            prop.equals("minPoolSize") ||
            prop.equals("maxPoolSize") ||
            prop.equals("maxIdleTime") ||
            prop.equals("propertyCycle") ||
            prop.equals("reuseConnections"));
  }

  /**
   Returns whether connections are re-used after being returned to the connection pool.
   @return true if connections may be reused; false if connections are closed after they are returned to the pool.  The default value is <tt>true</tt>.
   **/
  public boolean isReuseConnections()
  {
    return reuseConnections_;
  }


  /**
   Deserializes and initializes transient data.
   **/
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();
  }


  /**
   Sets the number of connections that the connection pool contains when it is created.
   If the pool has already been created, this method has no effect.
   @param initialPoolSize The number of pooled connections. Valid values
   are 0 or greater.
   **/
  public void setInitialPoolSize(int initialPoolSize)
  {
    final String property = "initialPoolSize";
    if (initialPoolSize < 0)
    {
      throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    initialPoolSize_ = initialPoolSize;
    logProperty(property, Integer.toString(initialPoolSize_));
  }

  /**
   Sets the maximum amount of time (in seconds) that a pooled
   connection in this pool is allowed to remain idle before it is
   automatically closed. A value of 0 indicates pooled connections
   are never automatically closed.
   @param maxIdleTime The maximum idle time for a pooled connection, in seconds.
   Valid values are 0 or greater.
   **/
  public void setMaxIdleTime(int maxIdleTime)
  {
    final String property = "maxIdleTime";
    if (maxIdleTime < 0)
    {
      throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    maxIdleTime_ = maxIdleTime*1000;  // store internally as milliseconds
    logProperty(property, Integer.toString(maxIdleTime));

    // Design note: In general we prefer to express "duration" values in milliseconds,
    // but the JDBC Tutorial and Reference shows this property in seconds (int).
  }


  /**
   Sets the maximum amount of time (in seconds) after which an
   in-use physical connection is considered to be expired and should be closed.
   A value of 0 indicates in-use connections are never automatically closed.
   @param maxLifetime The maximum lifetime for an in-use connection, in seconds.
   Valid values are 0 or greater.
   **/
  public void setMaxLifetime(int maxLifetime)
  {
    final String property = "maxLifetime";
    if (maxLifetime < 0)
    {
      throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    maxLifetime_ = maxLifetime*1000;  // store internally as milliseconds
    logProperty(property, Integer.toString(maxLifetime));

    // Design note: In general we prefer to express "duration" values in milliseconds,
    // but the JDBC Tutorial and Reference shows this property in seconds (int).
  }


  /**
   Sets the maximum number of connections that the connection pool
   contains. A value of 0 indicates there is no maximum.
   @param maxPoolSize The maximum number of connections in this pool.
   Valid values are 0 or greater.
   **/
  public void setMaxPoolSize(int maxPoolSize)
  {
    final String property = "maxPoolSize";
    if (maxPoolSize < 0)
    {
      throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    maxPoolSize_ = maxPoolSize;
    logProperty(property, Integer.toString(maxPoolSize_));
  }


  /**
   Sets the minimum number of connections that the connection pool
   contains. A value of 0 indicates there is no minimum and connections
   are created as they are needed.
   @param minPoolSize The minimum number of available connections in the pool.
   Valid values are 0 or greater.
   **/
  public void setMinPoolSize(int minPoolSize)
  {
    final String property = "minPoolSize";
    if (minPoolSize < 0)
    {
      throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    minPoolSize_ = minPoolSize;
    logProperty(property, Integer.toString(minPoolSize_));
  }


  /**
   Sets the interval (in seconds) between runs of the connection pool's
   maintenance thread. The maintenance thread enforces this pool's
   connections and statements so that they conform to the specified
   minimum and maximum pool sizes, idle time, and maximum number of
   open statements. A value of 0 indicates that a maintenance thread
   should not be created.
   @param propertyCycle The number of seconds that this pool should wait before enforcing
   its properties. Valid values are 0 or greater.
   **/
  public void setPropertyCycle(int propertyCycle)
  {
    final String property = "propertyCycle";
    if (propertyCycle < 0)
    {
      throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    propertyCycle_ = propertyCycle*1000;  // store internally as milliseconds
    logProperty(property, Integer.toString(propertyCycle));

    // Design note: In general we prefer to express "duration" values in milliseconds,
    // but the JDBC Tutorial and Reference shows this property in seconds (int).
  }


  /**
   Sets whether connections may be re-used after being returned to the connection pool.
   @param reuse If true, then connections may be reused; if false, then connections are closed after they are returned to the pool.  The default value is <tt>true</tt>.
   **/
  public void setReuseConnections(boolean reuse)
  {
    reuseConnections_ = reuse;
    logProperty("reuseConnections", Boolean.toString(reuseConnections_));
  }


}

