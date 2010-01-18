///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400ConnectionPool.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
// @C1 - 2008-06-06 - Added support for ProfileTokenCredential authentication.
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import com.ibm.as400.security.auth.ProfileTokenCredential;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import java.util.Enumeration;
import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;


/**
 *  Manages a pool of AS400 objects.  A connection pool is used to 
 *  share connections and manage the number of connections a user can have to the system.
 *  <p>
 *  A connection is defined by a systemName, userID, and an optional password and/or service.
 *  Services should be referred to using constants from the AS400 class 
 *  (FILE, PRINT, COMMAND, DATAQUEUE, etc.)
 *  <p>
 *  When a connection
 *  is requested, a fully functional AS400 object is returned to the calling application.  
 *  It is then 
 *  up to the application to return the AS400 object to the pool. It is not recommended 
 *  that an application use this object to create additional connections 
 *  as the pool would not keep track of these connections.
 *  <p>
 *  The AS400ConnectionPool class keeps track of the number of connections it creates.
 *  The user can set the maximum number of connections that can be given out by a
 *  pool.  If the maximum number of connections has already been given out when an
 *  additional connection is requested, an exception is thrown.
 *  <p>
 *  Note:  AS400ConnectionPool objects are threadsafe. 
 *  <p>
 *  This example creates an AS400ConnectionPool with a limit of 128 connections:
 *  
 *  <BLOCKQUOTE><PRE>
 *  // Create an AS400ConnectionPool.
 *  AS400ConnectionPool testPool = new AS400ConnectionPool();
 *  // Set a maximum of 128 connections to this pool.
 *  testPool.setMaxConnections(128);
 *  // Preconnect 5 connections to the AS400.COMMAND service.
 *  testPool.fill("myAS400", "myUserID", "myPassword", AS400.COMMAND, 5);
 *  // Create a connection to the AS400.COMMAND service. (Use the service number constants 
 *  // defined in the AS400 class (FILE, PRINT, COMMAND, DATAQUEUE, etc.))
 *  AS400 newConn = testPool.getConnection("myAS400", "myUserID", "myPassword", AS400.COMMAND);
 *  // Create a new command call object and run a command.
 *  CommandCall cmd = new CommandCall(newConn); 
 *  cmd.run("CRTLIB FRED");
 *  // Return the connection to the pool.
 *  testPool.returnConnectionToPool(newConn);  
 *  // Close the test pool.
 *  testPool.close();
 *  </PRE></BLOCKQUOTE>
 *
 * <P>AS400ConnectionPool objects generate the following events:
 *  <ul>
 *    <li><a href="ConnectionPoolEvent.html">ConnectionPoolEvent</a> - The events fired are:
 *      <ul>
 *       <li>CONNECTION_CREATED</li>
 *       <li>CONNECTION_EXPIRED</li>
 *       <li>CONNECTION_POOL_CLOSED</li>
 *       <li>CONNECTION_RELEASED</li>
 *       <li>CONNECTION_RETURNED</li>
 *       <li>MAINTENANCE_THREAD_RUN</li>
 *       </ul>
 *    </li>
 *    <li>PropertyChangeEvent</li>
 *  </ul>
 **/
public class AS400ConnectionPool extends ConnectionPool implements Serializable
{
  static final long serialVersionUID = 4L;

  /**
   Indicates that the CCSID used for new connections is the same as the system default CCSID.
   **/
  public static final int CCSID_DEFAULT = ConnectionPool.CCSID_DEFAULT;

  private transient Hashtable as400ConnectionPool_;
  // Hashtable of lists of connections that have been marked invalid by the user
  // by calling removeFromPool().
  private transient Hashtable removedAS400ConnectionPool_;  //@A6A
  private transient Log log_;
  private SocketProperties socketProperties_;
  private transient long lastRun_=0;     //@D1A Last time cleanupConnections() was called.  Added for fix to JTOpen Bug #3863
  private transient boolean connectionHasBeenCreated_ = false;
  
  // Handles loading the appropriate resource bundle
//@CRS  private static ResourceBundleLoader loader_;

  /**
   *  Constructs an AS400ConnectionPool with default ConnectionPoolProperties.
   **/
  public AS400ConnectionPool()
  {
    super();
    initializeTransient();
  }


  /**
   * Remove any connections that have exceeded maximum inactivity time, replace any 
   * that have aged past maximum usage or maximum lifetime, and remove any that have 
   * been in use too long.
   *
   * @see ConnectionPoolProperties
   **/
  void cleanupConnections()
  {
    synchronized (as400ConnectionPool_)
    {
      Enumeration keys = as400ConnectionPool_.keys();
      while (keys.hasMoreElements())
      {
        String key = (String)keys.nextElement();
        try
        {
          ConnectionList connList = (ConnectionList)as400ConnectionPool_.get(key);
          connList.removeExpiredConnections(poolListeners_);  
        }
        catch (Exception e)
        {
          log(e, key);
        }
      }
    } 
    if (poolListeners_ != null)
    {
      ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(this, ConnectionPoolEvent.MAINTENANCE_THREAD_RUN);
      poolListeners_.fireMaintenanceThreadRun(poolEvent);
    }
    lastRun_ = System.currentTimeMillis();     //@D1A
  }        


  /**
   * Close and cleanup the connection pool.
   **/
  public void close()
  {
    log(ResourceBundleLoader.getText("AS400CP_SHUTDOWN"));
    synchronized (as400ConnectionPool_)
    {
      Enumeration keys = as400ConnectionPool_.keys();
      while (keys.hasMoreElements())
      {
        String key = (String)keys.nextElement();
        ConnectionList connList = (ConnectionList)as400ConnectionPool_.get(key);
        connList.close();
      }
      as400ConnectionPool_.clear();
    }

    // Terminate the maintenance thread, if it's still alive.
    if (maintenance_ != null && maintenance_.isAlive()) {
      maintenance_.shutdown();  // tell the thread to terminate
    }

    if (poolListeners_ != null)
    {
      ConnectionPoolEvent event = new ConnectionPoolEvent(this, ConnectionPoolEvent.CONNECTION_POOL_CLOSED);
      poolListeners_.fireClosedEvent(event);
    }
    log(ResourceBundleLoader.getText("AS400CP_SHUTDOWNCOMP"));
  }


  /**
   * Create a key to use to access the connections hashtable.
   *
   * @param   systemName  The name of the system being queried.
   * @param   userID  The name of the user.
   * @return     The key to use for the hashtable.
   **/
  private String createKey(String systemName, String userID)
  {
    systemName = systemName.trim();
    userID = userID.trim();
    return systemName.toUpperCase() + "/" + userID.toUpperCase();
  }


  /** 
   * Preconnects a specified number of connections to a specific system, userID,
   * profileToken, and service.
   *
   * @param systemName The name of the system where the connections should exist.
   * @param userID The name of the user.
   * @param profileToken The profile token to use to authenticate to the system.
   * @param service The service to be connected. See the service number constants defined by AS400 class.
   * @param numberOfConnections The number of connections to be made.
   *
   * @exception ConnectionPoolException If a connection pool error occurred.
   **/
  public void fill(String systemName, String userID, ProfileTokenCredential profileToken, int service, int numberOfConnections) //@C1A
  throws ConnectionPoolException
  {
    fill(systemName, userID, profileToken, service, numberOfConnections, null);
  }

  /** 
   * Preconnects a specified number of connections to a specific system, userID,
   * profileToken, service, and Locale.  
   *
   * @param systemName The name of the system where the connections should exist.
   * @param userID The name of the user.
   * @param profileToken The profile token to use to authenticate to the system.
   * @param service The service to be connected. See the service number constants defined by AS400 class.
   * @param numberOfConnections The number of connections to be made.
   * @param locale The Locale used to set the National Language Version (NLV) on the system for the AS400 objects
   * created.  Only the COMMAND, PRINT, and DATABASE services accept an NLV.
   *
   * @exception ConnectionPoolException If a connection pool error occurred.
   **/
  public void fill(String systemName, String userID, ProfileTokenCredential profileToken, int service, int numberOfConnections, Locale locale) //@C1A 
  throws ConnectionPoolException
  {
    if (numberOfConnections < 1)
      throw new ExtendedIllegalArgumentException("numberOfConnections", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    Vector newAS400Connections = new Vector();
    if (Trace.isTraceOn())
      Trace.log(Trace.INFORMATION, "fill() key before resolving= " + systemName + "/" + userID);
    systemName = AS400.resolveSystem(systemName);  
    userID = AS400.resolveUserId(userID.toUpperCase());   //@KBA   
    String key = createKey(systemName, userID);
    if (Trace.isTraceOn())
      Trace.log(Trace.INFORMATION, "fill() key after resolving= " + key);
    try
    {
      ConnectionList connections = (ConnectionList)as400ConnectionPool_.get(key);
      log(ResourceBundleLoader.substitute(ResourceBundleLoader.getText("AS400CP_FILLING"), new String[] { (new Integer(numberOfConnections)).toString(), 
                               systemName, userID} ));
      // create the specified number of connections
      for (int i = 0; i < numberOfConnections; i++)
      {
        newAS400Connections.addElement(getConnection(systemName, userID, service, true, false, locale, profileToken));
        // Note: When filling an empty pool, getConnection() creates a new element and adds it to pool.
      }
      connections = (ConnectionList)as400ConnectionPool_.get(key);
      for (int j = 0; j < numberOfConnections; j++)
      {
        connections.findElement((AS400)newAS400Connections.elementAt(j)).setInUse(false);
      }
      if (Trace.isTraceOn() && locale != null)
        Trace.log(Trace.INFORMATION, "created " + numberOfConnections + "with a locale");
    }
    catch (AS400SecurityException e)
    {
      // If exception occurs, stop creating connections, run maintenance thread, and 
      // throw whatever exception was received on creation to user.
      ConnectionList connections = (ConnectionList)as400ConnectionPool_.get(key);
      for (int k = 0; k < newAS400Connections.size(); k++)
      {
        connections.findElement((AS400)newAS400Connections.elementAt(k)).setInUse(false); 
      }
      if (maintenance_ != null && maintenance_.isRunning())
        cleanupConnections();
      log(ResourceBundleLoader.getText("AS400CP_FILLEXC"));         
      throw new ConnectionPoolException(e);
    }
    catch (IOException ie) 
    {
      // If exception occurs, stop creating connections, run maintenance thread, and       
      // throw whatever exception was received on creation to user.                        
      ConnectionList connections = (ConnectionList)as400ConnectionPool_.get(key);      
      for (int k = 0; k < newAS400Connections.size(); k++)
      { 
        connections.findElement((AS400)newAS400Connections.elementAt(k)).setInUse(false); 
      } 
      if (maintenance_ != null && maintenance_.isRunning())
        cleanupConnections();                     
      log(ResourceBundleLoader.getText("AS400CP_FILLEXC"));   
      throw new ConnectionPoolException(ie);     
    }    
  }

  /** 
   * Preconnects a specified number of connections to a specific system, userID,
   * password, and service.  
   *
   * @param systemName The name of the system where the connections should exist.
   * @param userID The name of the user.
   * @param password The password of the user.
   * @param service The service to be connected. See the service number constants defined by AS400 class.
   * @param numberOfConnections The number of connections to be made.
   *
   * @exception ConnectionPoolException If a connection pool error occurred.
   **/
  public void fill(String systemName, String userID, String password, int service, int numberOfConnections) 
  throws ConnectionPoolException
  {
    fill(systemName, userID, password, service, numberOfConnections, null);
  }


  //@B3A
  /** 
   * Preconnects a specified number of connections to a specific system, userID,
   * password, service, and Locale.  
   *
   * @param systemName The name of the system where the connections should exist.
   * @param userID The name of the user.
   * @param password The password of the user.
   * @param service The service to be connected. See the service number constants defined by AS400 class.
   * @param numberOfConnections The number of connections to be made.
   * @param locale The Locale used to set the National Language Version (NLV) on the system for the AS400 objects
   * created.  Only the COMMAND, PRINT, and DATABASE services accept an NLV.
   *
   * @exception ConnectionPoolException If a connection pool error occurred.
   **/
  public void fill(String systemName, String userID, String password, int service, int numberOfConnections, Locale locale) 
  throws ConnectionPoolException
  {
    if (numberOfConnections < 1)
      throw new ExtendedIllegalArgumentException("numberOfConnections", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    Vector newAS400Connections = new Vector();
    if (Trace.isTraceOn())
      Trace.log(Trace.INFORMATION, "fill() key before resolving= " + systemName + "/" + userID);
    systemName = AS400.resolveSystem(systemName);  
    userID = AS400.resolveUserId(userID.toUpperCase());   //@KBA   
    String key = createKey(systemName, userID);
    if (Trace.isTraceOn())
      Trace.log(Trace.INFORMATION, "fill() key after resolving= " + key);
    try
    {
      ConnectionList connections = (ConnectionList)as400ConnectionPool_.get(key);
      log(ResourceBundleLoader.substitute(ResourceBundleLoader.getText("AS400CP_FILLING"), new String[] { (new Integer(numberOfConnections)).toString(), 
                               systemName, userID} ));
      // create the specified number of connections
      //@B4D AS400.addPasswordCacheEntry(systemName, userID, password);
      for (int i = 0; i < numberOfConnections; i++)
      {
        newAS400Connections.addElement(getConnection(systemName, userID, service, true, false, locale, password));  //@B4C
        // Note: When filling an empty pool, getConnection() creates a new element and adds it to pool.
      }
      connections = (ConnectionList)as400ConnectionPool_.get(key);
      for (int j = 0; j < numberOfConnections; j++)
      {
        connections.findElement((AS400)newAS400Connections.elementAt(j)).setInUse(false);
      }
      if (Trace.isTraceOn() && locale != null)
        Trace.log(Trace.INFORMATION, "created " + numberOfConnections + "with a locale");
    }
    catch (AS400SecurityException e)
    {
      // If exception occurs, stop creating connections, run maintenance thread, and 
      // throw whatever exception was received on creation to user.
      ConnectionList connections = (ConnectionList)as400ConnectionPool_.get(key);
      for (int k = 0; k < newAS400Connections.size(); k++)
      {
        connections.findElement((AS400)newAS400Connections.elementAt(k)).setInUse(false); 
      }
      if (maintenance_ != null && maintenance_.isRunning())
        cleanupConnections();
      log(ResourceBundleLoader.getText("AS400CP_FILLEXC"));         
      throw new ConnectionPoolException(e);
    }
    catch (IOException ie)                                  //@A2A
    {
      //@A2A
      // If exception occurs, stop creating connections, run maintenance thread, and       
      // throw whatever exception was received on creation to user.                        
      ConnectionList connections = (ConnectionList)as400ConnectionPool_.get(key);      
      for (int k = 0; k < newAS400Connections.size(); k++)
      {                                          //@A2A
        connections.findElement((AS400)newAS400Connections.elementAt(k)).setInUse(false); 
      }                                          //@A2A
      if (maintenance_ != null && maintenance_.isRunning())
        cleanupConnections();                               //@A2A
      log(ResourceBundleLoader.getText("AS400CP_FILLEXC"));                       //@A2A
      throw new ConnectionPoolException(ie);                         //@A2A
    }                                           //@A2A
  }

  /**
   * Closes the connection if not explicitly closed by the caller.
   *
   * @exception   Throwable      If an error occurs.
   **/
  protected void finalize ()
  throws Throwable
  {
    close();

    super.finalize ();
  }


  /**
   * Get the number of active connections to a system. 
   * Note: The value returned is based on systemName and userID and does not
   * reflect the authentication scheme (e.g. password, profile token).
   *
   * @param   systemName  The name of the system where the connections exist.
   * @param   userID  The name of the user.
   * @return    The number of active connections to a system.
   **/
  public int getActiveConnectionCount(String systemName, String userID)
  {
    if (systemName == null)
      throw new NullPointerException("systemName");
    if (userID == null)
      throw new NullPointerException("userID");
    systemName = AS400.resolveSystem(systemName);  //@A5A
    userID = AS400.resolveUserId(userID.toUpperCase());      //@A5A  //@KBA
    String key = createKey(systemName, userID);
    if (Trace.isTraceOn()) //@A5A
      Trace.log(Trace.INFORMATION, "getActiveConnectionCount key= " + key); //@A5A
    ConnectionList connections = (ConnectionList)as400ConnectionPool_.get(key);
    if (connections == null)
    {
      if (Trace.isTraceOn()) //@A5A
        Trace.log(Trace.WARNING, "getActiveConnectionCount found no " + key + " list in the pool"); //@A5A
      return 0;
    }
    return(connections.getActiveConnectionCount());
  }


  /**
   * Get the number of available connections to a system. 
   * Note: The value returned is based on systemName and userID and does not
   * reflect the authentication scheme (e.g. password, profile token).
   *
   * @param   systemName  The name of the system where the connections exist.
   * @param   userID  The name of the user.
   * @return    The number of available connections to a system.
   **/
  public int getAvailableConnectionCount(String systemName, String userID)
  {
    if (systemName == null)
      throw new NullPointerException("systemName");
    if (userID == null)
      throw new NullPointerException("userID");
    systemName = AS400.resolveSystem(systemName);  //@A5A
    userID = AS400.resolveUserId(userID.toUpperCase());      //@A5A   //@KBA
    String key = createKey(systemName, userID);
    if (Trace.isTraceOn()) //@A5A
      Trace.log(Trace.INFORMATION, "getAvailableConnectionCount key= " + key); //@A5A
    ConnectionList connections = (ConnectionList)as400ConnectionPool_.get(key);
    if (connections == null)
    {
      if (Trace.isTraceOn()) //@A5A
        Trace.log(Trace.WARNING, "getAvailableConnectionCount found no " + key + "list in the pool"); //@A5A
      return 0;
    }
    return connections.getAvailableConnectionCount();
  }


  /** 
   * Get a connected AS400 object from the connection pool.  If an appropriate one is 
   * not found, one is created.  If the maximum connection limit has been reached, an exception
   * will be thrown.  
   * Note that the password is validated only when creating a <b>new</b> connection, 
   * of the match criteria to pull a connection out of the pool.  This means that
   * if an <b>existing</b> connection in the pool matches the systemName and userID 
   * passed in, a connection will be returned without the password being validated.
   *
   * @param   systemName  The name of the system where the object should exist.
   * @param   userID  The name of the user.
   * @param   password  The password of the user.
   * @param   service  The service to connect. See the service number constants defined by AS400 class.
   * @return     A connected AS400 object.
   * @exception ConnectionPoolException If a connection pool error occurred.
   **/
  public AS400 getConnection(String systemName, String userID, String password, int service)
  throws ConnectionPoolException
  {
    try
    {
      //@B4D AS400.addPasswordCacheEntry(systemName, userID, password);
      AS400 releaseConnection = getConnection(systemName, userID, service, true, false, null, password); //@B3C add null locale //@B4C
      if (poolListeners_ != null)
      {
        ConnectionPoolEvent event = new ConnectionPoolEvent(releaseConnection, ConnectionPoolEvent.CONNECTION_RELEASED); //@A7C
        poolListeners_.fireConnectionReleasedEvent(event); 
      }
      return releaseConnection;
    }
    catch (AS400SecurityException e)
    {
      throw new ConnectionPoolException(e);
    }
    catch (IOException ie)
    {
      throw new ConnectionPoolException(ie);
    }
  }

  /**
   * Returns the CCSID that is used when creating new connections.
   * The default value is the system default CCSID as determined by the AS400 class.
   * @return The CCSID, or {@link #CCSID_DEFAULT CCSID_DEFAULT} if the system default CCSID is used.
   **/
  public int getCCSID()
  {
    return super.getCCSID();
  }


  //@B3A
  /**
   * Get a connected AS400 object from the connection pool with the specified Locale.  If an appropriate one is 
   * not found, one is created.  If the maximum connection limit has been reached, an exception
   * will be thrown.
   *
   * @param   systemName  The name of the system where the object should exist.
   * @param   userID  The name of the user.
   * @param   password  The password of the user.
   * @param   service  The service to connect. See the service number constants defined by AS400 class.
   * @param   locale   The Locale used to set the National Language Version (NLV) on the system for the AS400 object returned. 
   * Only the COMMAND, PRINT, and DATABASE services accept an NLV.
   * @return     A connected AS400 object.
   * @exception ConnectionPoolException If a connection pool error occurred.
   **/
  public AS400 getConnection(String systemName, String userID, String password, int service, Locale locale)
  throws ConnectionPoolException
  {
    try
    {
      //@B4D AS400.addPasswordCacheEntry(systemName, userID, password);
      AS400 releaseConnection = getConnection(systemName, userID, service, true, false, locale, password);     //@B4C
      if (poolListeners_ != null)
      {
        ConnectionPoolEvent event = new ConnectionPoolEvent(releaseConnection, ConnectionPoolEvent.CONNECTION_RELEASED); 
        poolListeners_.fireConnectionReleasedEvent(event); 
      }
      return releaseConnection;
    }
    catch (AS400SecurityException e)
    {
      throw new ConnectionPoolException(e);
    }
    catch (IOException ie)
    {
      throw new ConnectionPoolException(ie);
    }
  }


  /**
   * Get a connected AS400 object from the connection pool.  If an appropriate one is 
   * not found, one is created.  If the maximum connection limit has been reached, an exception
   * will be thrown.
   *
   * @param   systemName  The name of the system where the object should exist.
   * @param   userID  The name of the user.
   * @param   service  The service to connect. See the service number constants defined by AS400 class.
   * @return     A connected AS400 object.
     * @exception ConnectionPoolException Always thrown because this method has been deprecated.
     * @deprecated  Use getConnection(String systemName, String userID, String password, int service) instead.
  **/
  public AS400 getConnection(String systemName, String userID, int service)
  throws ConnectionPoolException
  {
    //@B4D try
    //@B4D {
    //@B4D     AS400 releaseConnection = getConnection(systemName, userID, service, true, false, null); //@B3C add null locale
    //@B4D     ConnectionPoolEvent event = new ConnectionPoolEvent(releaseConnection, ConnectionPoolEvent.CONNECTION_RELEASED); //@A7C
    //@B4D     poolListeners_.fireConnectionReleasedEvent(event); 
    //@B4D     return releaseConnection;
    //@B4D }
    //@B4D catch (AS400SecurityException e)
    //@B4D {
    //@B4D     throw new ConnectionPoolException(e);
    //@B4D }
    //@B4D catch (IOException ie)
    //@B4D {
    //@B4D     throw new ConnectionPoolException(ie);
    //@B4D }
    throw new ConnectionPoolException(new ExtendedIOException(ExtendedIOException.REQUEST_NOT_SUPPORTED));  //@B4A
  }


  /**
   * Get an AS400 object from the connection pool.  If an appropriate one is not found, 
   * one is created.  If the maximum connection limit has been reached, an exception
   * will be thrown.  The AS400 object may not be connected to any services.
   * Note that the password is validated only when creating a <b>new</b> connection, 
   * of the match criteria to pull a connection out of the pool.  This means that
   * if an <b>existing</b> connection in the pool matches the systemName and userID 
   * passed in, a connection will be returned without the password being validated.
   *
   * @param   systemName  The name of the system where the object should exist.
   * @param   userID  The name of the user.
   * @param   password  The password of the user.
   * @return     An AS400 object.
   * @exception ConnectionPoolException If a connection pool error occurred.
   **/ 
  public AS400 getConnection(String systemName, String userID, String password)
  throws ConnectionPoolException
  {    
    try
    {
      //@B4D AS400.addPasswordCacheEntry(systemName, userID, password);
      AS400 releaseConnection = getConnection(systemName, userID, 0, false, false, null, password);  //@B3C add null locale //@B4C
      if (poolListeners_ != null)
      {
        ConnectionPoolEvent event = new ConnectionPoolEvent(releaseConnection, ConnectionPoolEvent.CONNECTION_RELEASED); //@A7C
        poolListeners_.fireConnectionReleasedEvent(event); 
      }
      return releaseConnection; 
    }
    catch (AS400SecurityException e)
    {
      throw new ConnectionPoolException(e);
    }
    catch (IOException ie)
    {
      throw new ConnectionPoolException(ie);
    }
  }

  //@B3A
  /**
   * Get an AS400 object from the connection pool with the specified Locale.  If an appropriate one is not found, 
   * one is created.  If the maximum connection limit has been reached, an exception
   * will be thrown.  The AS400 object may not be connected to any services.
   *
   * @param   systemName  The name of the system where the object should exist.
   * @param   userID  The name of the user.
   * @param   password  The password of the user.
   * @param   locale   The Locale used to set the National Language Version (NLV) on the system for the AS400 object returned. 
   * Only the COMMAND, PRINT, and DATABASE services accept an NLV.
   * @return     An AS400 object.
   * @exception ConnectionPoolException If a connection pool error occurred.
   **/ 
  public AS400 getConnection(String systemName, String userID, String password, Locale locale)  
  throws ConnectionPoolException
  {    
    try
    {
      //@B4D AS400.addPasswordCacheEntry(systemName, userID, password);
      AS400 releaseConnection = getConnection(systemName, userID, 0, false, false, locale, password); //@B4C
      if (poolListeners_ != null)
      {
        ConnectionPoolEvent event = new ConnectionPoolEvent(releaseConnection, ConnectionPoolEvent.CONNECTION_RELEASED); 
        poolListeners_.fireConnectionReleasedEvent(event); 
      }
      return releaseConnection; 
    }
    catch (AS400SecurityException e)
    {
      throw new ConnectionPoolException(e);
    }
    catch (IOException ie)
    {
      throw new ConnectionPoolException(ie);
    }
  }



  /**
   * Get an AS400 object from the connection pool.  If an appropriate one is not found, 
   * one is created.  If the maximum connection limit has been reached, an exception
   * will be thrown.  The AS400 object may not be connected to any services.
   *
   * @param   systemName  The name of the system where the object should exist.
   * @param   userID  The name of the user.
   * @return     An AS400 object.
* @exception ConnectionPoolException Always thrown because this method has been deprecated.
     * @deprecated  Use getConnection(String systemName, String userID, String password) instead.
   **/ 
  public AS400 getConnection(String systemName, String userID)
  throws ConnectionPoolException
  {    
    //@B4D try
    //@B4D {
    //@B4D     AS400 releaseConnection = getConnection(systemName, userID, 0, false, false, null);  //@B3C add null locale
    //@B4D     releaseConnection.getVRM();
    //@B4D     ConnectionPoolEvent event = new ConnectionPoolEvent(releaseConnection, ConnectionPoolEvent.CONNECTION_RELEASED); //@A7C
    //@B4D     poolListeners_.fireConnectionReleasedEvent(event); 
    //@B4D     return releaseConnection; 
    //@B4D }
    //@B4D catch (AS400SecurityException ae)
    //@B4D {
    //@B4D     throw new ConnectionPoolException(ae);
    //@B4D }
    //@B4D catch (IOException ie)
    //@B4D {
    //@B4D     throw new ConnectionPoolException(ie);
    //@B4D }
    throw new ConnectionPoolException(new ExtendedIOException(ExtendedIOException.REQUEST_NOT_SUPPORTED));  //@B4A
  }
  
  /** 
   * Get a connected AS400 object from the connection pool.  If an appropriate one is 
   * not found, one is created.  If the maximum connection limit has been reached, an exception
   * will be thrown.  
   * Note that the profileTokenCredential is validated only when creating a <b>new</b> 
   * connection, of the match criteria to pull a connection out of the pool.  This means
   * that if an <b>existing</b> connection in the pool matches the systemName and
   * userID passed in, a connection will be returned without the profileTokenCredential 
   * being validated.
   *
   * @param   systemName  The name of the system where the object should exist.
   * @param   userID  The name of the user.
   * @param   profileToken  The profile token to use to authenticate to the system.
   * @param   service  The service to connect. See the service number constants defined by AS400 class.
   * @return     A connected AS400 object.
   * @exception ConnectionPoolException If a connection pool error occurred.
   **/
  public AS400 getConnection(String systemName, String userID, ProfileTokenCredential profileToken, int service) //@C1A
  throws ConnectionPoolException
  {
    try
    {
      AS400 releaseConnection = getConnection(systemName, userID, service, true, false, null, profileToken);
      if (poolListeners_ != null)
      {
        ConnectionPoolEvent event = new ConnectionPoolEvent(releaseConnection, ConnectionPoolEvent.CONNECTION_RELEASED);
        poolListeners_.fireConnectionReleasedEvent(event); 
      }
      return releaseConnection;
    }
    catch (AS400SecurityException e)
    {
      throw new ConnectionPoolException(e);
    }
    catch (IOException ie)
    {
      throw new ConnectionPoolException(ie);
    }
  }

  /**
   * Get a connected AS400 object from the connection pool with the specified Locale.  If an appropriate one is 
   * not found, one is created.  If the maximum connection limit has been reached, an exception
   * will be thrown.
   *
   * @param   systemName  The name of the system where the object should exist.
   * @param   userID  The name of the user.
   * @param   profileToken  The profile token to use to authenticate to the system.
   * @param   service  The service to connect. See the service number constants defined by AS400 class.
   * @param   locale   The Locale used to set the National Language Version (NLV) on the system for the AS400 object returned. 
   * Only the COMMAND, PRINT, and DATABASE services accept an NLV.
   * @return     A connected AS400 object.
   * @exception ConnectionPoolException If a connection pool error occurred.
   **/
  public AS400 getConnection(String systemName, String userID, ProfileTokenCredential profileToken, int service, Locale locale) //@C1A
  throws ConnectionPoolException
  {
    try
    {
      AS400 releaseConnection = getConnection(systemName, userID, service, true, false, locale, profileToken);
      if (poolListeners_ != null)
      {
        ConnectionPoolEvent event = new ConnectionPoolEvent(releaseConnection, ConnectionPoolEvent.CONNECTION_RELEASED); 
        poolListeners_.fireConnectionReleasedEvent(event); 
      }
      return releaseConnection;
    }
    catch (AS400SecurityException e)
    {
      throw new ConnectionPoolException(e);
    }
    catch (IOException ie)
    {
      throw new ConnectionPoolException(ie);
    }
  }


  /**
   * Get an AS400 object from the connection pool.  If an appropriate one is not found, 
   * one is created.  If the maximum connection limit has been reached, an exception
   * will be thrown.  The AS400 object may not be connected to any services.
     * Note that the password is validated only when creating a new connection, not as part 
     * of the match criteria to pull a connection out of the pool.  This means that
     * if a connection in the pool matches the systemName and userID passed in, a
     * connection will be returned without the password being validated.
   *
   * @param   systemName  The name of the system where the object should exist.
   * @param   userID  The name of the user.
   * @param   profileToken  The profile token to use to authenticate to the system.
   * @return     An AS400 object.
   * @exception ConnectionPoolException If a connection pool error occurred.
   **/ 
  public AS400 getConnection(String systemName, String userID, ProfileTokenCredential profileToken) //@C1A
  throws ConnectionPoolException
  {    
    try
    {
      AS400 releaseConnection = getConnection(systemName, userID, 0, false, false, null, profileToken);
      if (poolListeners_ != null)
      {
        ConnectionPoolEvent event = new ConnectionPoolEvent(releaseConnection, ConnectionPoolEvent.CONNECTION_RELEASED);
        poolListeners_.fireConnectionReleasedEvent(event); 
      }
      return releaseConnection; 
    }
    catch (AS400SecurityException e)
    {
      throw new ConnectionPoolException(e);
    }
    catch (IOException ie)
    {
      throw new ConnectionPoolException(ie);
    }
  }

  /**
   * Get an AS400 object from the connection pool with the specified Locale.  If an appropriate one is not found, 
   * one is created.  If the maximum connection limit has been reached, an exception
   * will be thrown.  The AS400 object may not be connected to any services.
   *
   * @param   systemName  The name of the system where the object should exist.
   * @param   userID  The name of the user.
   * @param   profileToken  The profile token to use to authenticate to the system.
   * @param   locale   The Locale used to set the National Language Version (NLV) on the system for the AS400 object returned. 
   * Only the COMMAND, PRINT, and DATABASE services accept an NLV.
   * @return     An AS400 object.
   * @exception ConnectionPoolException If a connection pool error occurred.
   **/ 
  public AS400 getConnection(String systemName, String userID, ProfileTokenCredential profileToken, Locale locale) //@C1A  
  throws ConnectionPoolException
  {    
    try
    {
      AS400 releaseConnection = getConnection(systemName, userID, 0, false, false, locale, profileToken);
      if (poolListeners_ != null)
      {
        ConnectionPoolEvent event = new ConnectionPoolEvent(releaseConnection, ConnectionPoolEvent.CONNECTION_RELEASED); 
        poolListeners_.fireConnectionReleasedEvent(event); 
      }
      return releaseConnection; 
    }
    catch (AS400SecurityException e)
    {
      throw new ConnectionPoolException(e);
    }
    catch (IOException ie)
    {
      throw new ConnectionPoolException(ie);
    }
  }


  
  private AS400 getConnection(String systemName, String userID, int service, boolean connect, boolean secure, Locale locale, AS400ConnectionPoolAuthentication poolAuth)  //@B3C //@B4C //@C1C
  throws AS400SecurityException, IOException, ConnectionPoolException 
  {
    if (systemName == null)
      throw new NullPointerException("systemName");
    if (systemName.length() == 0)
      throw new ExtendedIllegalArgumentException("systemName", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    if (userID == null)
      throw new NullPointerException("userID");
    if (userID.length() == 0)
      throw new ExtendedIllegalArgumentException("userID", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

    if (Trace.traceOn_) //@A5A
    {
      Trace.log(Trace.INFORMATION, "getConnection() key before resolving= " + systemName + "/" + userID); //@A5A
    }
    systemName = AS400.resolveSystem(systemName);           //@A5A
    userID = AS400.resolveUserId(userID.toUpperCase());               //@A5A  //@KBA

    String key = createKey(systemName, userID);
    if (Trace.traceOn_)                                //@A5A
    {
      Trace.log(Trace.INFORMATION, "getConnection() key after resolving= " + key); //@A5A
    }

    if (!isInUse())                      //@A3A             
    {
      //@A3A
      setInUse(true);     // threadUsed property can now not be changed.     //@A3A
    }                                      //@A3A

    //Work with maintenance thread
    if (isThreadUsed() && isRunMaintenance())
    {
      // Start thread if it has not been initialized.
      if (maintenance_ == null)
      {
        synchronized(this) //@CRS
        {
          if (maintenance_ == null) //@CRS
          {
            maintenance_ = new PoolMaintenance(this);
            maintenance_.start();
          }
        }
      }
      // Restart the thread.
      if (!maintenance_.isRunning()) maintenance_.setRunning(true);
    }
    else if (!isThreadUsed() && isRunMaintenance() && 
        ((System.currentTimeMillis() - lastRun_) > getCleanupInterval()))  // If running single-threaded and cleanup interval has elapsed, run cleanup 
    {
      cleanupConnections();
    }

    //@CRS - Let's do a double-check here for performance, per JTOpen bug #3727.
    ConnectionList connections = (ConnectionList)as400ConnectionPool_.get(key);

    if (connections == null)
    {
      synchronized (as400ConnectionPool_) //@B1M
      {
        connections = (ConnectionList)as400ConnectionPool_.get(key);  //@B1C

        if (connections == null) //@CRS - Double-check idiom.
        {
          // no connection list exists, start a new list
          if (log_ != null)
          {
            log(ResourceBundleLoader.substitute(ResourceBundleLoader.getText("AS400CP_CONNLIST"), new String[] {systemName, userID} ));
          }
          connections = new ConnectionList(systemName, userID, properties_);
          // log_ can be null, meaning events should not be logged
          connections.setLog(log_);

          // create a new connection
          as400ConnectionPool_.put(key, connections);
        }
      }
    }

    //@CRS - Moved the block below out of the synch block above per JTOpen bug #3727...
    // We don't want to hold the lock on the entire pool if we are trying to get a connection
    // for a system that is down or non-existent. ConnectionList.getConnection() is synchronized
    // inside itself, anyway.

    //Get a connection from the list
    AS400 connection = null;
    if (connect)
    {
      connection = connections.getConnection(service, secure, poolListeners_, locale, poolAuth, socketProperties_, getCCSID()).getAS400Object();  //@B3C add null locale  //@B4C //@C1C
    }
    else
    {
      connection = connections.getConnection(secure, poolListeners_, locale, poolAuth, socketProperties_, getCCSID()).getAS400Object();  //@B3C add null locale  //@B4C //@C1C
    }
    connectionHasBeenCreated_ = true;  // remember that we've created at least 1 connection
    return connection;
  }

  /** 
   * Get an AS400 object from the connection pool.  If an appropriate one is not found, 
   * one is created.  The AS400 object may not be connected to any services.  
   *
   * @param   systemName  The name of the system where the object should exist.
   * @param   userID  The name of the user.
   * @param   service  The service to connect. See the service number constants defined by AS400 class.
   * @param   connect  If true connect to specified service.
   * @param   secure   If true secure AS400 object was requested.   
   * @param   locale   The Locale used to set the National Language Version (NLV) on the system for the AS400 object returned. 
   * Only the COMMAND, PRINT, and DATABASE services accept an NLV.
   * @return     An AS400 object.
   *
   * @exception   AS400SecurityException  If a security error occurred.
   * @exception   IOException  If a communications error occurred.
   * @exception ConnectionPoolException If a connection pool error occurred.
   **/ 
  private AS400 getConnection(String systemName, String userID, int service, boolean connect, boolean secure, Locale locale, String password)  //@B3C //@B4C
  throws AS400SecurityException, IOException, ConnectionPoolException 
  {
    AS400ConnectionPoolAuthentication poolAuth = new AS400ConnectionPoolAuthentication(password); //@C1A
    return (getConnection(systemName, userID, service, connect, secure, locale, poolAuth));       //@C1C
}

  /** 
   * Get an AS400 object from the connection pool.  If an appropriate one is not found, 
   * one is created.  The AS400 object may not be connected to any services.  
   *
   * @param   systemName  The name of the system where the object should exist.
   * @param   userID  The name of the user.
   * @param   service  The service to connect. See the service number constants defined by AS400 class.
   * @param   connect  If true connect to specified service.
   * @param   secure   If true secure AS400 object was requested.   
   * @param   locale   The Locale used to set the National Language Version (NLV) on the system for the AS400 object returned. 
   * Only the COMMAND, PRINT, and DATABASE services accept an NLV.
   * @return     An AS400 object.
   *
   * @exception   AS400SecurityException  If a security error occurred.
   * @exception   IOException  If a communications error occurred.
   * @exception ConnectionPoolException If a connection pool error occurred.
   **/ 
  private AS400 getConnection(String systemName, String userID, int service, boolean connect, boolean secure, Locale locale, ProfileTokenCredential profileToken)
  throws AS400SecurityException, IOException, ConnectionPoolException 
  {
    AS400ConnectionPoolAuthentication poolAuth = new AS400ConnectionPoolAuthentication(profileToken); //@C1A
    return (getConnection(systemName, userID, service, connect, secure, locale, poolAuth));           //@C1C
  }


  /**
   * Get an secure connected AS400 object from the connection pool.  If an appropriate one is not found, 
   * one is created.  If the maximum connection limit has been reached, an exception
   * will be thrown.  The AS400 object may not be connected to any services.
   * Note that the password is validated only when creating a <b>new</b> connection, 
   * of the match criteria to pull a connection out of the pool.  This means that
   * if an <b>existing</b> connection in the pool matches the systemName and userID 
   * passed in, a connection will be returned without the password being validated.
   *
   * @param   systemName  The name of the system where the object should exist.
   * @param   userID  The name of the user.
   * @param   password  The password of the user.
   * @return     A secure connected AS400 object.
   * @exception ConnectionPoolException If a connection pool error occurred.
  **/  
  public AS400 getSecureConnection(String systemName, String userID, String password)
  throws ConnectionPoolException
  {    
    try
    {
      //@B4D AS400.addPasswordCacheEntry(systemName, userID, password);
      AS400 releaseConnection = getConnection(systemName, userID, 0, false, true, null, password);  //@B3C add null locale //@B4C
      releaseConnection.getVRM();
      if (poolListeners_ != null)
      {
        ConnectionPoolEvent event = new ConnectionPoolEvent(releaseConnection, ConnectionPoolEvent.CONNECTION_RELEASED); 
        poolListeners_.fireConnectionReleasedEvent(event); 
      }
      return releaseConnection; 
    }
    catch (AS400SecurityException e)
    {
      throw new ConnectionPoolException(e);
    }
    catch (IOException ie)
    {
      throw new ConnectionPoolException(ie);
    }
  }


  /**
   * Get an secure connected AS400 object from the connection pool.  If an appropriate one is not found, 
   * one is created.  If the maximum connection limit has been reached, an exception
   * will be thrown.  The AS400 object may not be connected to any services.
   *
   * @param   systemName  The name of the system where the object should exist.
   * @param   userID  The name of the user.
   * @return     A secure connected AS400 object.
     * @exception ConnectionPoolException Always thrown because this method has been deprecated.
     * @deprecated  Use getSecureConnection(String systemName, String userID, String password) instead.
  **/  
  public AS400 getSecureConnection(String systemName, String userID)
  throws ConnectionPoolException
  {    
    //@B4D try
    //@B4D {
    //@B4D     AS400 releaseConnection = getConnection(systemName, userID, 0, false, true, null);  //@B3C add null locale
    //@B4D     releaseConnection.getVRM();
    //@B4D     ConnectionPoolEvent event = new ConnectionPoolEvent(releaseConnection, ConnectionPoolEvent.CONNECTION_RELEASED); //@A7C
    //@B4D     poolListeners_.fireConnectionReleasedEvent(event); 
    //@B4D     return releaseConnection; 
    //@B4D }
    //@B4D catch (AS400SecurityException ae)
    //@B4D {
    //@B4D     throw new ConnectionPoolException(ae);
    //@B4D }
    //@B4D catch (IOException ie)
    //@B4D {        
    //@B4D     throw new ConnectionPoolException(ie);
    //@B4D }
    throw new ConnectionPoolException(new ExtendedIOException(ExtendedIOException.REQUEST_NOT_SUPPORTED));  //@B4A
  }


  /**
   * Get a secure connected AS400 object from the connection pool.  If an appropriate one is 
   * not found, one is created.  If the maximum connection limit has been reached, an exception
     * will be thrown.  
     * Note that the password is validated only when creating a <b>new</b> connection, 
     * of the match criteria to pull a connection out of the pool.  This means that
     * if an <b>existing</b> connection in the pool matches the systemName and userID 
     * passed in, a connection will be returned without the password being validated.
   *
   * @param   systemName  The name of the system where the object should exist.
   * @param   userID  The name of the user.
   * @param   password  The password of the user.
   * @param   service  The service to connect. See the service number constants defined by AS400 class.
   * @return     A connected AS400 object.
   * @exception ConnectionPoolException If a connection pool error occurred.
  **/
  public AS400 getSecureConnection(String systemName, String userID, String password, int service)
  throws ConnectionPoolException
  {
    try
    {
      //@B4D AS400.addPasswordCacheEntry(systemName, userID, password);
      AS400 releaseConnection = getConnection(systemName, userID, service, true, true, null, password);  //@B3C add null locale //@B4C
      if (poolListeners_ != null)
      {
        ConnectionPoolEvent event = new ConnectionPoolEvent(releaseConnection, ConnectionPoolEvent.CONNECTION_RELEASED); //@A7C
        poolListeners_.fireConnectionReleasedEvent(event); 
      }
      return releaseConnection;
    }
    catch (AS400SecurityException e)
    {
      throw new ConnectionPoolException(e);
    }
    catch (IOException ie)
    {
      throw new ConnectionPoolException(ie);
    }
  }


  /**
   * Get a secure connected AS400 object from the connection pool.  If an appropriate one is 
   * not found, one is created.  If the maximum connection limit has been reached, an exception
   * will be thrown.
   *
   * @param   systemName  The name of the system where the object should exist.
   * @param   userID  The name of the user.
   * @param   service  The service to connect. See the service number constants defined by AS400 class.
   * @return     A connected AS400 object.
     * @exception ConnectionPoolException Always thrown because this method has been deprecated.
     * @deprecated  Use getConnection(String systemName, String userID, String password, int service) instead.
   **/
  public AS400 getSecureConnection(String systemName, String userID, int service)
  throws ConnectionPoolException
  {
    //@B4D try
    //@B4D {
    //@B4D     AS400 releaseConnection = getConnection(systemName, userID, service, true, true, null);  //@B3C add null locale
    //@B4D     ConnectionPoolEvent event = new ConnectionPoolEvent(releaseConnection, ConnectionPoolEvent.CONNECTION_RELEASED); //@A7C
    //@B4D     poolListeners_.fireConnectionReleasedEvent(event); 
    //@B4D     return releaseConnection;
    //@B4D }
    //@B4D catch (AS400SecurityException e)
    //@B4D {
    //@B4D     throw new ConnectionPoolException(e);
    //@B4D }
    //@B4D catch (IOException ie)
    //@B4D {
    //@B4D     throw new ConnectionPoolException(ie);
    //@B4D }
    throw new ConnectionPoolException(new ExtendedIOException(ExtendedIOException.REQUEST_NOT_SUPPORTED));  //@B4A
  }

  /**
   * Get an secure connected AS400 object from the connection pool.  If an appropriate one is not found, 
   * one is created.  If the maximum connection limit has been reached, an exception
   * will be thrown.  The AS400 object may not be connected to any services.
   * Note that the profileTokenCredential is validated only when creating a <b>new</b> 
   * connection, of the match criteria to pull a connection out of the pool.  This means
   * that if an <b>existing</b> connection in the pool matches the systemName and
   * userID passed in, a connection will be returned without the profileTokenCredential 
   * being validated.
   *
   * @param   systemName  The name of the system where the object should exist.
   * @param   userID  The name of the user.
   * @param   profileToken  The profile token to use to authenticate to the system.
   * @return     A secure connected AS400 object.
   * @exception ConnectionPoolException If a connection pool error occurred.
  **/  
  public AS400 getSecureConnection(String systemName, String userID, ProfileTokenCredential profileToken) //@C1A
  throws ConnectionPoolException
  {    
    try
    {
      AS400 releaseConnection = getConnection(systemName, userID, 0, false, true, null, profileToken);
      releaseConnection.getVRM();
      if (poolListeners_ != null)
      {
        ConnectionPoolEvent event = new ConnectionPoolEvent(releaseConnection, ConnectionPoolEvent.CONNECTION_RELEASED); 
        poolListeners_.fireConnectionReleasedEvent(event); 
      }
      return releaseConnection; 
    }
    catch (AS400SecurityException e)
    {
      throw new ConnectionPoolException(e);
    }
    catch (IOException ie)
    {
      throw new ConnectionPoolException(ie);
    }
  }


  /**
   * Get a secure connected AS400 object from the connection pool.  If an appropriate one is 
   * not found, one is created.  If the maximum connection limit has been reached, an exception
   * will be thrown. 
   * Note that the profileTokenCredential is validated only when creating a <b>new</b> 
   * connection, of the match criteria to pull a connection out of the pool.  This means
   * that if an <b>existing</b> connection in the pool matches the systemName and
   * userID passed in, a connection will be returned without the profileTokenCredential 
   * being validated.
   *
   * @param   systemName  The name of the system where the object should exist.
   * @param   userID  The name of the user.
   * @param   profileToken  The profile token to use to authenticate to the system.
   * @param   service  The service to connect. See the service number constants defined by AS400 class.
   * @return     A connected AS400 object.
   * @exception ConnectionPoolException If a connection pool error occurred.
  **/
  public AS400 getSecureConnection(String systemName, String userID, ProfileTokenCredential profileToken, int service) //@C1A
  throws ConnectionPoolException
  {
    try
    {
      AS400 releaseConnection = getConnection(systemName, userID, service, true, true, null, profileToken);
      if (poolListeners_ != null)
      {
        ConnectionPoolEvent event = new ConnectionPoolEvent(releaseConnection, ConnectionPoolEvent.CONNECTION_RELEASED);
        poolListeners_.fireConnectionReleasedEvent(event); 
      }
      return releaseConnection;
    }
    catch (AS400SecurityException e)
    {
      throw new ConnectionPoolException(e);
    }
    catch (IOException ie)
    {
      throw new ConnectionPoolException(ie);
    }
  }



  /**
   Returns a copy of the socket properties that this AS400ConnectionPool specifies when it creates new AS400 objects, for example in <tt>fill()</tt>, <tt>getConnection()</tt>, or <tt>getSecureConnection()</tt>.
   @return  The socket properties.  A null value indicates that this AS400ConnectionPool specifies no socket properties.
    **/
  public SocketProperties getSocketProperties()
  {
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting socket properties.");
    if (socketProperties_ == null) return null;
    else {
      SocketProperties socketProperties = new SocketProperties();
      socketProperties.copyValues(socketProperties_);
      return socketProperties;
    }
  }


  /**
   * Get a list of the system names in the pool.
   *
   * @return     A list of the system names in the pool.
   *             If the the pool is empty, returns a zero-length list.
  **/  
  public String[] getSystemNames()
  {
    Enumeration keys = as400ConnectionPool_.keys();
    Vector hosts = new Vector();
    while (keys.hasMoreElements())
    {
      String key = (String)keys.nextElement();
      String host = key.substring(0, key.indexOf("/"));
      if (!hosts.contains(host)) hosts.addElement(host);
    }
    String[] hostsArr = new String[hosts.size()];
    hosts.copyInto(hostsArr);
    return hostsArr;
  }


  //@B2A
  /**
   * Get an enumeration of the systemName/userId pairs in the pool.
   * Note: The returned information is regardless of the authentication 
   * scheme used when filling the pool (e.g. password, profile token).
   *
   * @return     An enumeration of the systemName/userIds in the pool
  **/  
  public Enumeration getUsers()
  {
    return as400ConnectionPool_.keys();
  }


  /**
   * Get a list of the userIds in the pool with connections to a specific system.
   * UserIds are listed regardless of whether their connections are
   * currently active or disconnected.
   * Note: The returned information is regardless of the authentication 
   * scheme used when filling the pool (e.g. password, profile token).
   *
   * @param systemName  The name of the system of interest.
   * @return     A list of the userIds in the pool for system <tt>systemName</tt>.
   *             If there are no userIDs for that system, empty list is returned.
  **/  
  public String[] getUsers(String systemName)
  {
    if (systemName == null) throw new NullPointerException("systemName");
    return getUsers(systemName, false);
  }


  /**
   * Get a list of the userIds in the pool with connections to a specific system.
   * Only userIds with currently active (non-disconnected) connections are listed.
   * Note: The returned information is regardless of the authentication 
   * scheme used when filling the pool (e.g. password, profile token).
   *
   * @param systemName  The name of the system of interest.
   * @return     A list of the connected userIds in the pool for system <tt>systemName</tt>.
   *             If there are no connected userIDs for that system, empty list is returned.
  **/  
  public String[] getConnectedUsers(String systemName)
  {
    if (systemName == null) throw new NullPointerException("systemName");
    return getUsers(systemName, true);
  }


  private String[] getUsers(String systemName, boolean listConnectedOnly)
  {
    Enumeration keys = as400ConnectionPool_.keys();
    Vector users = new Vector();
    systemName = systemName.toUpperCase().trim();
    String compareKey = systemName+"/";
    while (keys.hasMoreElements())
    {
      String key = (String)keys.nextElement();
      if (key.startsWith(compareKey))
      {
        ConnectionList connections = (ConnectionList)as400ConnectionPool_.get(key);
        if (!listConnectedOnly || connections.hasConnectedConnection())
        {
          String user = key.substring(key.indexOf("/")+1);
          if (!users.contains(user)) users.addElement(user);
        }
      }
    }
    String[] usersArr = new String[users.size()];
    users.copyInto(usersArr);
    return usersArr;
  }


  /**
   *  Initializes the transient data.
   **/
  private void initializeTransient()
  {
    // log_ was originally not transient, however the EventLog class (one of the possible
    // implementations of the Log interface) uses a java.io.PrintWriter object which is 
    // not serializable.  Therefore, log_ was changed to be transient and the user
    // will need to reset log_ after a serialization of the pool.
    as400ConnectionPool_ = new Hashtable();
    removedAS400ConnectionPool_ = new Hashtable();   //@A5A
    lastRun_ = System.currentTimeMillis();
    connectionHasBeenCreated_ = false;
  }


  /**
   * Log the message to the log.
   *   
   * @param   msg  The message to log.
   **/ 
  private void log(String msg)
  {
    if (Trace.isTraceOn())
      Trace.log(Trace.INFORMATION, msg);
    if (log_ != null)
    {
      log_.log(msg);
    }
  }

  /**
   * Log the exception and message to the log.
   *
   * @param   exception  The exception to log.
   * @param   msg  The message to log.
   **/ 
  private void log(Exception exception, String msg)
  {
    if (Trace.isTraceOn())
      Trace.log(Trace.ERROR, msg, exception);
    if (log_ != null)
    {
      log_.log(msg, exception);
    }
  }

  /**
  *  Deserializes and initializes transient data.
  *  @exception IOException If a file I/O error occurs.
  *  @exception ClassNotFoundException If a file error occurs.
  **/
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    initializeTransient();      
  }

  //@A6A
  /**
   * Remove the connections for a systemName/userID.  Call this function if connections for a
   * particular system/userID would no longer be valid.  For example, if you change the password for a 
   * userId, you will want to call this method to remove the connections for the old userID 
   * combination.  This will not invalidate connections in use; rather it will mean that the next time
   * a connection is requested for that userID, a new connection 
   * will be established to the system.  After you call this method, you may want 
   * to fill the pool with connections with your new password to avoid connection time.
   * Note: This method will remove connections regardless of the authentication scheme
   * used when filling the pool  (e.g. password, profile token).
   *
   * @param systemName The system name of connections you want to remove.
   * @param userID The user ID of connections you want to remove.       
   **/
  public void removeFromPool(String systemName, String userID)
  {
    if (systemName == null)
      throw new NullPointerException("systemName");
    if (userID == null)
      throw new NullPointerException("userID");
    systemName = AS400.resolveSystem(systemName);  
    userID = AS400.resolveUserId(userID.toUpperCase());       //@KBA   
    String key = createKey(systemName, userID);
    ConnectionList listToBeRemoved = (ConnectionList)as400ConnectionPool_.get(key);
    if (listToBeRemoved != null)
    {
      listToBeRemoved.removeUnusedElements();
      removedAS400ConnectionPool_.put(key, listToBeRemoved);
      as400ConnectionPool_.remove(key);
    }
    else if (Trace.traceOn_)
    {
      Trace.log(Trace.WARNING, "A list of connections for: " + key + "does not exist");
    }
  } 

  /**
   * Return the AS400 object to the connection pool.
   *
   * @param   system  The system to return to the pool.
   **/
  public void returnConnectionToPool(AS400 system)
  //@A4D throws AS400SecurityException, IOException, ConnectionPoolException   
  {
    // This method searches the lists of connections for a reference to the AS400
    // object that was returned.  There will be one list per systemName/userId key.
    if (system == null) throw new NullPointerException("system");
    if (Trace.traceOn_)
    {
      Trace.log(Trace.INFORMATION, "returnConnectionToPool key= " + system.getSystemName() + "/" + system.getUserId()); //@A5A
    }
    String key = createKey(system.getSystemName(), system.getUserId());
    ConnectionList connections = (ConnectionList)as400ConnectionPool_.get(key);
    PoolItem poolItem = null;

    // First look for the list for the systemName/userId key for the AS400 object.
    // If such a list exists, search that list for a reference to the AS400 object returned.
    if (connections != null)
    {
      poolItem = connections.findElement(system);
    }

    // If such an item is found, set it not in use and send an event that the connection
    // was returned to the pool.
    if (poolItem != null)
    {
      // Before making the connection available for re-use, see if it's expired.
      boolean removed = connections.removeIfExpired(poolItem, poolListeners_);
      if (!removed) {
        poolItem.setInUse(false); // indicate that this connection is available
      }
      if (log_ != null)
      {
        log(ResourceBundleLoader.substitute(ResourceBundleLoader.getText("AS400CP_RETCONN"), new String[] {system.getSystemName(), system.getUserId()} ));
      }
      if (poolListeners_ != null)
      {
        ConnectionPoolEvent event = new ConnectionPoolEvent(poolItem.getAS400Object(), ConnectionPoolEvent.CONNECTION_RETURNED); //@A7C
        poolListeners_.fireConnectionReturnedEvent(event);
      }
      if (Trace.traceOn_)
      {
        Trace.log(Trace.INFORMATION, "returned connection to pool");
      }
    }

    // If the item was not found, search all the lists, looking for a reference to that 
    // item.  If it is found, disconnect it and remove it from the pool since its
    // systemName/userId has been changed from what was expected.
    if (poolItem == null)
    {
      Enumeration keys = as400ConnectionPool_.keys();
      while (keys.hasMoreElements())
      {
        String tryKey = (String)keys.nextElement();     
        ConnectionList connList = (ConnectionList)as400ConnectionPool_.get(tryKey);
        poolItem = connList.findElement(system);
        if (poolItem != null)
        {
          if (Trace.traceOn_)
          {
            Trace.log(Trace.WARNING, "connection belongs to a different list than expected");
          }
          poolItem.getAS400Object().disconnectAllServices();
          connList.removeElement(system);  
          break;
        }
      } 
    }

    // A removed pool was added to this class.  A list of connections will
    // be moved into the removed pool if removeFromPool() with the systemName/userId of the
    // list is called.  This allows us to keep references to an AS400 object until
    // the user calls returnConnectionToPool() on it, but not give it out again 
    // to the user.
    // Code was added here to handle checking in removed pool for the connection
    // and removing it from that pool if it is found.
    // @A6 New code starts here.

    // If the pooled connection was not found in the regular lists, start looking 
    // in the lists in the removed pool.
    if (poolItem == null)
    {
      ConnectionList removedConnections = (ConnectionList)removedAS400ConnectionPool_.get(key);

      // Start looking in the list with the systemName/userId combination.
      if (removedConnections != null)
      {
        poolItem = removedConnections.findElement(system);
      }

      // If the object is found, disconnect it and remove the element from removed pool.
      if (poolItem != null)
      {
        poolItem.getAS400Object().disconnectAllServices();
        removedConnections.removeElement(system); 
        if (log_ != null)
        {
          log(ResourceBundleLoader.substitute(ResourceBundleLoader.getText("AS400CP_RETCONN"), new String[] {system.getSystemName(), system.getUserId()} ));
        }
        if (poolListeners_ != null)
        {
          ConnectionPoolEvent event = new ConnectionPoolEvent(poolItem.getAS400Object(), ConnectionPoolEvent.CONNECTION_RETURNED); //@A7C
          poolListeners_.fireConnectionReturnedEvent(event);
        }
        if (Trace.traceOn_)
        {
          Trace.log(Trace.INFORMATION, "returned connection to removed pool");
        }
      }

      // If the object was not found, search through every list in the removed pool,
      // looking for a reference to the object.
      if (poolItem == null)
      {
        Enumeration keys = removedAS400ConnectionPool_.keys();
        while (keys.hasMoreElements())
        {
          String tryKey = (String)keys.nextElement();     
          ConnectionList connList = (ConnectionList)removedAS400ConnectionPool_.get(tryKey);
          poolItem = connList.findElement(system);
          if (poolItem != null)
          {
            poolItem.getAS400Object().disconnectAllServices();
            connList.removeElement(system);
            if (Trace.traceOn_)
            {
              Trace.log(Trace.INFORMATION, "returned connection to removed pool");
            }
            break;
          }
        } 
      }
      //@A6A New code ends

      // If the object was not found in either pool, it does not belong to the pool
      // and trace a warning message.
      if (poolItem == null && Trace.traceOn_)
      {
        Trace.log(Trace.WARNING, "connection does not belong to this pool");
      }
    }

    //If running single-threaded and cleanup interval has elapsed, run cleanup
    if (!isThreadUsed() && isRunMaintenance() && 
        ((System.currentTimeMillis() - lastRun_) > getCleanupInterval()))  //@D1C replace maintenance_.getLastTime() with lastRun_ 
    {
      cleanupConnections();
    }
  }

  /**
   *  Run cleanupConnections().
   *  @param reduced true if need to check current num connections; false otherwise.
   **/
  void runMaintenance(boolean reduced)
  {
    if (maintenance_ != null)
    {
      synchronized(maintenance_)
      {
        if (maintenance_.isRunning())
        {
          if (reduced)
          {
            synchronized (as400ConnectionPool_)
            {
              Enumeration keys = as400ConnectionPool_.keys();
              while (keys.hasMoreElements())
              {
                String key = (String)keys.nextElement();
                try
                {
                  ConnectionList connList = (ConnectionList)as400ConnectionPool_.get(key);
                  connList.shutDownOldest(); 
                }
                catch (Exception e)
                {
                  log(e, key);
                }
              }
            }
          }
          //@A6A Start new code
          synchronized (removedAS400ConnectionPool_)                              
          {
            Enumeration removedKeys = removedAS400ConnectionPool_.keys();       
            while (removedKeys != null && removedKeys.hasMoreElements())
            {
              //go through each list of systemName/userID
              String key = (String)removedKeys.nextElement();                                                                                 
              ConnectionList connList = (ConnectionList)removedAS400ConnectionPool_.get(key); 
              //disconnect and remove any unused connections from the list
              if (!connList.removeUnusedElements())
              {
                //if there are no more connections remaining, remove the 
                //list from the pool
                removedAS400ConnectionPool_.remove(key);                
              }

            }                                                                   
          }                                                                       
          //@A6A End new code
          maintenance_.notify();
        }
      }
    }
  }


  /**
   * Sets the CCSID to use when creating new connections.
   * The default value is the system default CCSID as determined by the AS400 class.
   * Note: This method only affects the CCSID of newly-created connections.
   * Existing connections are not affected.
   * @param ccsid The CCSID to use for connections in the pool, or {@link #CCSID_DEFAULT CCSID_DEFAULT} to indicate that the system default CCSID should be used.
   **/
  public void setCCSID(int ccsid)
  {
    if (connectionHasBeenCreated_ && Trace.traceOn_) {
      Trace.log(Trace.WARNING, "setCCSID("+ccsid+") is being called after the pool already contains connections.");
    }
    super.setCCSID(ccsid);
  }



  /**
    * Set the Log object to log events.  The default is to not log events.
    *
    * @param   log  The log object to use, or null if events should not be logged.
    *
    * @see com.ibm.as400.access.Log
    **/
  public void setLog(Log log)
  {
    this.log_ = log;
  }



  /**
   Sets the socket properties that this AS400ConnectionPool specifies when it creates new AS400 objects, for example in <tt>fill()</tt>, <tt>getConnection()</tt>, or <tt>getSecureConnection()</tt>.
   <br>Note that <tt>properties</tt> will apply only to AS400 objects created <em>after</em> this method is called.  Any AS400 objects already in the pool are not affected.

   @param properties  The socket properties.  If null, then this AS400ConnectionPool will specify no socket properties when it creates new AS400 objects.  That is, <tt>setSocketProperties(null)</tt> cancels the effects of any previous <tt>setSocketProperties()</tt>.
    **/
  public void setSocketProperties(SocketProperties properties)
  {
    socketProperties_ = properties;
  }

}
