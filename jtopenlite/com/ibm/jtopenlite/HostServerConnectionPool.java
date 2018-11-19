///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  HostServerConnectionPool.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite;

import java.io.*;
import java.util.*;

/**
 * Used to pool HostServerConnections of a specific type to a specific system.
 *
 * For example:
 * <pre>
 *   // Get the SystemInfo object used to seed our pool.
 *   SignonConnection signon = SignonConnection.getConnection("system", "user", "password");
 *   SystemInfo info = signon.getInfo();
 *   signon.close();
 *
 *   // Construct the pool (initially empty).
 *   HostServerConnectionPool&lt;CommandConnection&gt; commandPool = new HostServerConnectionPool&lt;CommandConnection&gt;(info);
 *
 *   // To populate the pool, create connections and check them in.
 *   // To use the pool, check out connections from the pool.
 *   CommandConnection conn = CommandConnection.getConnection(info, "FRED", "password");
 *   commandPool.checkin(conn);
 *   conn = commandPool.checkout("FRED");
 *   commandPool.checkin(conn);
 *
 *   // You can check the current size of the pool.
 *   int used = commandPool.getUsedConnectionCount();
 *   int free = commandPool.getFreeConnectionCount();
 *
 *   // You can check the number of connections per user.
 *   int fredUsed = commandPool.getUsedConnectionCount("FRED");
 *   int fredFree = commandPool.getFreeConnectionCount("FRED");
 *
 *   // Closing the pool will close all connections in the pool, both free and in use.
 *   commandPool.close();
 * </pre>
**/
public class HostServerConnectionPool<T extends HostServerConnection>
{
  private final SystemInfo info_;
  private final Map<String, Set<T>> freeConnections_ = new HashMap<String, Set<T>>();
  private final Set<T> usedConnections_ = new HashSet<T>();

  private int freeConnectionCount_;
  private int usedConnectionCount_;

  /**
   * Constructs a new connection pool for the specified system.
   * All connections checked into this pool must have a matching SystemInfo object.
  **/
  public HostServerConnectionPool(SystemInfo info)
  {
    info_ = info;
  }

  /**
   * Returns the system information for this pool.
  **/
  public SystemInfo getInfo()
  {
    return info_;
  }

  protected void finalize() throws Throwable
  {
    close();
  }

  /**
   * Adds or returns a connection to this pool.
   * If the connection is closed or its SystemInfo does not match what was defined for this pool,
   * the connection is removed from this pool if it already exists in this pool, but is otherwise ignored.
  **/
  public void checkin(final T conn) throws IOException
  {
    if (!conn.isClosed() && conn.getInfo().equals(info_))
    {
      final String user = conn.getUser();

      Set systems = freeConnections_.get(user);
      if (systems == null)
      {
        systems = new HashSet<T>();
        freeConnections_.put(user, systems);
      }
      systems.add(conn);
      ++freeConnectionCount_;
      if (usedConnections_.remove(conn))
      {
        --usedConnectionCount_;
      }
    }
    else
    {
      remove(conn);
    }
  }

  /**
   * Obtains a free connection from this pool for the specified user.
   * If there are no free connections in the pool for the specified user, null is returned.
  **/
  public T checkout(String user) throws IOException
  {
    Set<T> systems = freeConnections_.get(user);
    if (systems != null)
    {
      Iterator<T> it = systems.iterator();
      if (it.hasNext())
      {
        T conn = it.next();
        it.remove();
        --freeConnectionCount_;
        usedConnections_.add(conn);
        ++usedConnectionCount_;
        return conn;
      }
    }
    return null;
  }

  /**
   * Removes the specified connection from this pool, regardless if it is free or in use.
   * If the connection is not in the pool, it is ignored.
  **/
  public void remove(final T conn)
  {
    final String user = conn.getUser();
    Set systems = freeConnections_.get(user);
    if (systems != null)
    {
      if (systems.remove(conn))
      {
        --freeConnectionCount_;
      }
    }
    if (usedConnections_.remove(conn))
    {
      --usedConnectionCount_;
    }
  }

  /**
   * Closes and removes all connections in this pool, both free and in use.
  **/
  public void close() throws IOException
  {
    closeFree();
    closeUsed();
  }

  /**
   * Closes and removes all free connections in this pool.
  **/
  public void closeFree() throws IOException
  {
    final Iterator<String> it = freeConnections_.keySet().iterator();
    while (it.hasNext())
    {
      final String user = it.next();
      final Set<T> systems = freeConnections_.get(user);
      final Iterator<T> it2 = systems.iterator();
      while (it2.hasNext())
      {
        final T conn = it2.next();
        try
        {
          conn.close();
        }
        catch (IOException io)
        {
        }
        it2.remove();
        --freeConnectionCount_;
      }
      it.remove();
    }
  }

  /**
   * Closes and removes all in-use connections in this pool.
  **/
  public void closeUsed() throws IOException
  {
    final Iterator<T> it = usedConnections_.iterator();
    while (it.hasNext())
    {
      final T conn = it.next();
      try
      {
        conn.close();
      }
      catch (IOException io)
      {
      }
      it.remove();
      --usedConnectionCount_;
    }
  }

  /**
   * Closes and removes all connections for the specified user in this pool, both free and in use.
  **/
  public void close(final String user) throws IOException
  {
    closeFree(user);
    closeUsed(user);
  }

  /**
   * Closes and removes all free connections for the specified user in this pool.
  **/
  public void closeFree(final String user) throws IOException
  {
    final Set<T> systems = freeConnections_.remove(user);
    if (systems != null)
    {
      final Iterator<T> it = systems.iterator();
      while (it.hasNext())
      {
        final T conn = it.next();
        try
        {
          conn.close();
        }
        catch (IOException io)
        {
        }
        --freeConnectionCount_;
      }
    }
  }

  /**
   * Closes and removes all in-use connections for the specified user in this pool.
  **/
  public void closeUsed(final String user) throws IOException
  {
    final Iterator<T> it = usedConnections_.iterator();
    while (it.hasNext())
    {
      final T conn = it.next();
      if (conn.getUser().equals(user))
      {
        it.remove();
        try
        {
          conn.close();
        }
        catch (IOException io)
        {
        }
        --usedConnectionCount_;
      }
    }
  }

  /**
   * Returns the total number of connections in this pool, both free and in use.
  **/
  public int getConnectionCount()
  {
    return freeConnectionCount_ + usedConnectionCount_;
  }

  /**
   * Returns the number of free connections in this pool.
  **/
  public int getFreeConnectionCount()
  {
    return freeConnectionCount_;
  }

  /**
   * Returns the number of in-use connections in this pool.
  **/
  public int getUsedConnectionCount()
  {
    return usedConnectionCount_;
  }

  /**
   * Returns the total number of connections for the specified user in this pool, both free and in use.
  **/
  public int getConnectionCount(final String user)
  {
    return getFreeConnectionCount(user) + getUsedConnectionCount(user);
  }

  /**
   * Returns the number of free connections for the specified user in this pool.
  **/
  public int getFreeConnectionCount(final String user)
  {
    final Set systems = freeConnections_.get(user);
    return systems == null ? 0 : systems.size();
  }

  /**
   * Returns the number of used connections for the specified user in this pool.
  **/
  public int getUsedConnectionCount(final String user)
  {
    final Iterator<T> it = usedConnections_.iterator();
    int count = 0;
    while (it.hasNext())
    {
      final T conn = it.next();
      if (conn.getUser().equals(user)) ++count;
    }
    return count;
  }

  /**
   * Returns an array of users of connections in this pool, both free and in use.
  **/
  public String[] getUsers()
  {
    Set<String> set = freeConnections_.keySet();
    return set.toArray(new String[set.size()]);
  }
}
