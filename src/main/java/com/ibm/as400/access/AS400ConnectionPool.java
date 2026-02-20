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
import java.util.Objects;

import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;

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
public class AS400ConnectionPool extends ConnectionPool implements Serializable, Referenceable
{
    static final long serialVersionUID = 4L;
    
    private static final String CCSID_PROPERTY = "ccsid";
    private static final String CLEANUP_INTERVAL_PROPERTY = "cleanupInterval";
    private static final String MAX_CONNECTIONS_PROPERTY = "maxConnections";
    private static final String MAX_INACTIVITY_PROPERTY = "maxInactivity";
    private static final String MAX_LIFETIME_PROPERTY = "maxLifetime";
    private static final String MAX_USE_COUNT_PROPERTY = "maxUseCount";
    private static final String MAX_USE_TIME_PROPERTY = "maxUseTime";
    private static final String PRETEST_CONNECTIONS_PROPERTY = "pretestConnections";
    private static final String RUN_MAINTENANCE_PROPERTY = "runMaintenance";
    private static final String THREAD_USED_PROPERTY = "threadUsed";
    private static final String VIRTUAL_THREADS_PROPERTY = "virtualThreads";

    /**
       Indicates that the CCSID used for new connections is the same as the system default CCSID.
     **/
    public static final int CCSID_DEFAULT = ConnectionPool.CCSID_DEFAULT;

    private transient Hashtable<String, ConnectionList> as400ConnectionPool_;
    private transient Hashtable<String, ConnectionList> removedAS400ConnectionPool_;  // invalid connections marked by user by calling removeFromPool().
    private transient Log log_;
    private SocketProperties socketProperties_;
    private transient long lastRun_=0;     // Last time cleanupConnections() was called.  Added for fix to JTOpen Bug #3863
    private transient boolean connectionHasBeenCreated_ = false;
  
    /**
     *  Constructs an AS400ConnectionPool with default ConnectionPoolProperties.
     **/
    public AS400ConnectionPool()
    {
        super();
        initializeTransient();
    }

    /**
     * Constructs an AS400ConnectionPool from the specified Reference object.
     *
     * @param reference to retrieve the ConnectionPool properties from
     */
    AS400ConnectionPool(Reference reference) {
        super();
        initializeTransient();

        Objects.requireNonNull(reference, "reference");
        Enumeration<RefAddr> list = reference.getAll();
        while (list.hasMoreElements()) {
            RefAddr refAddr = list.nextElement();
            String property = refAddr.getType();
            String value = (String) refAddr.getContent();
            switch (property) {
                case CCSID_PROPERTY:
                    setCCSID(Integer.parseInt(value));
                    break;
                case CLEANUP_INTERVAL_PROPERTY:
                    setCleanupInterval(Long.parseLong(value));
                    break;
                case MAX_CONNECTIONS_PROPERTY:
                    setMaxConnections(Integer.parseInt(value));
                    break;
                case MAX_INACTIVITY_PROPERTY:
                    setMaxInactivity(Long.parseLong(value));
                    break;
                case MAX_LIFETIME_PROPERTY:
                    setMaxLifetime(Long.parseLong(value));
                    break;
                case MAX_USE_COUNT_PROPERTY:
                    setMaxUseCount(Integer.parseInt(value));
                    break;
                case MAX_USE_TIME_PROPERTY:
                    setMaxUseTime(Long.parseLong(value));
                    break;
                case PRETEST_CONNECTIONS_PROPERTY:
                    setPretestConnections(Boolean.parseBoolean(value));
                    break;
                case RUN_MAINTENANCE_PROPERTY:
                    setRunMaintenance(Boolean.parseBoolean(value));
                    break;
                case THREAD_USED_PROPERTY:
                    setThreadUsed(Boolean.parseBoolean(value));
                    break;
                case VIRTUAL_THREADS_PROPERTY:
                	setVirtualThreads(Boolean.parseBoolean(value));
                    break;
                default:
                    if (SocketProperties.isSocketProperty(property)) {
                        if (socketProperties_ == null) {
                            socketProperties_ = new SocketProperties();
                        }
                        socketProperties_.restore(property, value);
                    }
                    break;
            }
        }
    }

    /**
     * Returns the Reference object for the pool object. This is used by
     * JNDI when bound in a JNDI naming service. Contains the information
     * necessary to reconstruct the pool object when it is later
     * retrieved from JNDI via an object factory.
     *
     * @return A Reference object of the pool object.
     * @exception NamingException If a naming error occurs in resolving the
     * object.
     *
     */
    @Override
    public Reference getReference() throws NamingException {
        Trace.log(Trace.INFORMATION, "AS400ConnectionPool.getReference"); 

        Reference ref = new Reference(this.getClass().getName(),
                                     AS400ObjectFactory.class.getName(),
                                      null);

        ref.add(new StringRefAddr(CCSID_PROPERTY, Integer.toString(getCCSID())));
        ref.add(new StringRefAddr(CLEANUP_INTERVAL_PROPERTY, Long.toString(getCleanupInterval())));
        ref.add(new StringRefAddr(MAX_CONNECTIONS_PROPERTY, Integer.toString(getMaxConnections())));
        ref.add(new StringRefAddr(MAX_INACTIVITY_PROPERTY, Long.toString(getMaxInactivity())));
        ref.add(new StringRefAddr(MAX_LIFETIME_PROPERTY, Long.toString(getMaxLifetime())));
        ref.add(new StringRefAddr(MAX_USE_COUNT_PROPERTY, Integer.toString(getMaxUseCount())));
        ref.add(new StringRefAddr(MAX_USE_TIME_PROPERTY, Long.toString(getMaxUseTime())));
        ref.add(new StringRefAddr(PRETEST_CONNECTIONS_PROPERTY, Boolean.toString(isPretestConnections())));
        ref.add(new StringRefAddr(RUN_MAINTENANCE_PROPERTY, Boolean.toString(isRunMaintenance())));
        ref.add(new StringRefAddr(THREAD_USED_PROPERTY, Boolean.toString(isThreadUsed())));

        // Add the Socket options
        socketProperties_.save(ref);

        return ref;
    }

    /**
     * Remove any connections that have exceeded maximum inactivity time, replace any 
     * that have aged past maximum usage or maximum lifetime, and remove any that have 
     * been in use too long.
     *
     * @see ConnectionPoolProperties
     **/
    @Override
    void cleanupConnections()
    {
        synchronized (as400ConnectionPool_)
        {
            Enumeration<String> keys = as400ConnectionPool_.keys();
            while (keys.hasMoreElements())
            {
                String key = keys.nextElement();
                try
                {
                    ConnectionList connList = as400ConnectionPool_.get(key);
                    connList.removeExpiredConnections(poolListeners_);
                }
                catch (Exception e) {
                    log(e, key);
                }
            }
        }
        
        if (poolListeners_ != null)
        {
            ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(this, ConnectionPoolEvent.MAINTENANCE_THREAD_RUN);
            poolListeners_.fireMaintenanceThreadRun(poolEvent);
        }
        
        lastRun_ = System.currentTimeMillis();
    }        


    /**
     * Close and cleanup the connection pool.
     **/
    @Override
    public void close()
    {
        log(ResourceBundleLoader.getText("AS400CP_SHUTDOWN"));
        
        synchronized (as400ConnectionPool_)
        {
            Enumeration<String> keys = as400ConnectionPool_.keys();
            while (keys.hasMoreElements())
            {
                String key = keys.nextElement();
                ConnectionList connList = as400ConnectionPool_.get(key);
                connList.close();
            }
            as400ConnectionPool_.clear();
        }
    
        // Terminate the maintenance thread, if it's still alive.
        if (maintenance_ != null && maintenance_.isAlive())
            maintenance_.shutdown();
    
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
    public void fill(String systemName, String userID, ProfileTokenCredential profileToken, int service, int numberOfConnections) throws ConnectionPoolException
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
    public void fill(String systemName, String userID, ProfileTokenCredential profileToken, int service, int numberOfConnections, Locale locale) throws ConnectionPoolException
    {
        if (numberOfConnections < 1)
            throw new ExtendedIllegalArgumentException("numberOfConnections", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        
        if (Trace.traceOn_) log(Trace.INFORMATION, "fill() key before resolving= " + systemName + "/" + userID);
        
        Vector<AS400> newAS400Connections = new Vector<>();
        systemName = AS400.resolveSystem(systemName);  
        userID = AS400.resolveUserId(userID.toUpperCase());   //@KBA   
        String key = createKey(systemName, userID);
    
        if (Trace.traceOn_) log(Trace.INFORMATION, "fill() key after resolving= " + key);
    
        try
        {
            ConnectionList connections = as400ConnectionPool_.get(key);
            if (log_ != null || Trace.traceOn_)
                log(ResourceBundleLoader.substitute(ResourceBundleLoader.getText("AS400CP_FILLING"), 
                                                    new String[] { (Integer.valueOf(numberOfConnections)).toString(), systemName, userID} ));
            // create the specified number of connections
            for (int i = 0; i < numberOfConnections; i++) {
                newAS400Connections.addElement(getConnection(systemName, userID, service, true, false, locale, profileToken));
            }
            
            connections = as400ConnectionPool_.get(key);
            for (int j = 0; j < numberOfConnections; j++) {
                connections.findElement(newAS400Connections.elementAt(j)).setInUse(false);
            }
            
            if (Trace.traceOn_ && locale != null) log(Trace.INFORMATION, "Created " + numberOfConnections + "with a locale.");
        }
        catch (AS400SecurityException|IOException e)
        {
            // If exception occurs, stop creating connections, run maintenance thread, and 
            // throw whatever exception was received on creation to user.
            ConnectionList connections = as400ConnectionPool_.get(key);
            for (int k = 0; k < newAS400Connections.size(); k++) {
                connections.findElement(newAS400Connections.elementAt(k)).setInUse(false); 
            }
            
            if (maintenance_ != null && maintenance_.isRunning())
                cleanupConnections();
            log(ResourceBundleLoader.getText("AS400CP_FILLEXC"));         
            throw new ConnectionPoolException(e);
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
     * @deprecated Use fill(String systemName, String userID, char[] password, int service, int numberOfConnections) instead.
     **/
    @Deprecated
    public void fill(String systemName, String userID, String password, int service, int numberOfConnections) throws ConnectionPoolException {
        fill(systemName, userID, password, service, numberOfConnections, null);
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
    public void fill(String systemName, String userID, char[] password, int service, int numberOfConnections) throws ConnectionPoolException {
        fill(systemName, userID, password, service, numberOfConnections, null);
    }

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
     * @deprecated Use fill(String systemName, String userID, char[] password, int service, int numberOfConnections, Locale locale) instead.
     **/
    @Deprecated
    public void fill(String systemName, String userID, String password, int service, int numberOfConnections, Locale locale)  throws ConnectionPoolException
    {
        char[] passwordChars = (password != null) ? password.toCharArray() : null; 
        fill(systemName, userID, passwordChars, service, numberOfConnections, locale); 
        CredentialVault.clearArray(passwordChars);
    }

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
    public void fill(String systemName, String userID, char[] password, int service, int numberOfConnections, Locale locale) throws ConnectionPoolException
    {
        if (numberOfConnections < 1)
            throw new ExtendedIllegalArgumentException("numberOfConnections", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        
        if (Trace.traceOn_) log(Trace.INFORMATION, "fill() key before resolving= " + systemName + "/" + userID);
    
        Vector<AS400> newAS400Connections = new Vector<>();

        systemName = AS400.resolveSystem(systemName);  
        userID = AS400.resolveUserId(userID.toUpperCase());
        String key = createKey(systemName, userID);
    
        if (Trace.traceOn_) log(Trace.INFORMATION, "fill() key after resolving= " + key);
    
        try
        {
            ConnectionList connections = as400ConnectionPool_.get(key);
            if (log_ != null || Trace.traceOn_)
                log(ResourceBundleLoader.substitute(ResourceBundleLoader.getText("AS400CP_FILLING"), 
                                            new String[] { (Integer.valueOf(numberOfConnections)).toString(), systemName, userID} ));
            
            // create the specified number of connections
            //@B4D AS400.addPasswordCacheEntry(systemName, userID, password);
            for (int i = 0; i < numberOfConnections; i++) {
                newAS400Connections.addElement(getConnection(systemName, userID, service, true, false, locale, password));
            }
            
            connections = as400ConnectionPool_.get(key);
            for (int j = 0; j < numberOfConnections; j++) {
                connections.findElement(newAS400Connections.elementAt(j)).setInUse(false);
            }
            
            if (Trace.traceOn_ && locale != null) log(Trace.INFORMATION, "Created " + numberOfConnections + "with a locale.");
        }
        catch (AS400SecurityException|IOException e)
        {
            // If exception occurs, stop creating connections, run maintenance thread, and 
            // throw whatever exception was received on creation to user.
            ConnectionList connections = as400ConnectionPool_.get(key);
            for (int k = 0; k < newAS400Connections.size(); k++) {
                connections.findElement(newAS400Connections.elementAt(k)).setInUse(false); 
            }
            
            if (maintenance_ != null && maintenance_.isRunning())
                cleanupConnections();
            log(ResourceBundleLoader.getText("AS400CP_FILLEXC"));         
            throw new ConnectionPoolException(e);
        }
    }
    
    /** 
     * Preconnects a specified number of connections to a specific system based on existing AS400 object and service.
     * The AS400 object being passed in must have system and authentication information specified. 
     *
     * @param system An AS400 object that will be used to create new AS400 connections. 
     * @param service The service to be connected. See the service number constants defined by AS400 class.
     * @param numberOfConnections The number of connections to be made.
     *
     * @exception ConnectionPoolException If a connection pool error occurred.
     **/
    public void fill(AS400 system, int service, int numberOfConnections) throws ConnectionPoolException
    {        
        if (system == null) throw new NullPointerException("system");

        if (numberOfConnections < 1)
            throw new ExtendedIllegalArgumentException("numberOfConnections", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        
        String systemName = system.getSystemName();
        String userID     = system.getUserId();
        
        if (Trace.traceOn_) log(Trace.INFORMATION, "fill() key before resolving= " + systemName + "/" + userID);
        
        Vector<AS400> newAS400Connections = new Vector<>();
        systemName = AS400.resolveSystem(systemName);  
        userID = AS400.resolveUserId(userID.toUpperCase());
        String key = createKey(systemName, userID);
    
        if (Trace.traceOn_) log(Trace.INFORMATION, "fill() key after resolving= " + key);
    
        try
        {
            ConnectionList connections = as400ConnectionPool_.get(key);
            if (log_ != null || Trace.traceOn_)
                log(ResourceBundleLoader.substitute(ResourceBundleLoader.getText("AS400CP_FILLING"), 
                                                    new String[] { (Integer.valueOf(numberOfConnections)).toString(), systemName, userID} ));
            // create the specified number of connections
            for (int i = 0; i < numberOfConnections; i++) {
                newAS400Connections.addElement(getConnection(systemName, userID, service, true, system.isSecure(), system.getLocale(), null, system));
            }
            
            connections = as400ConnectionPool_.get(key);
            for (int j = 0; j < numberOfConnections; j++) {
                connections.findElement(newAS400Connections.elementAt(j)).setInUse(false);
            }
            
            if (Trace.traceOn_) log(Trace.INFORMATION, "Created " + numberOfConnections + "based on AS400 object.");
        }
        catch (AS400SecurityException|IOException e)
        {
            // If exception occurs, stop creating connections, run maintenance thread, and 
            // throw whatever exception was received on creation to user.
            ConnectionList connections = as400ConnectionPool_.get(key);
            for (int k = 0; k < newAS400Connections.size(); k++) {
                connections.findElement(newAS400Connections.elementAt(k)).setInUse(false); 
            }
            
            if (maintenance_ != null && maintenance_.isRunning())
                cleanupConnections();
            log(ResourceBundleLoader.getText("AS400CP_FILLEXC"));         
            throw new ConnectionPoolException(e);
        }
    }


    /**
     * Closes the connection if not explicitly closed by the caller.
     *
     * @exception   Throwable      If an error occurs.
     **/
    protected void finalize () throws Throwable
    {
        close();
        super.finalize ();
    }

    /**
     * Get the number of active (in-use) connections to a system.
     * Note: The value returned is based only on systemName and userID, and does not
     * reflect the authentication scheme (e.g. password, profile token).
     *
     * @param   systemName  The name of the system where the connections exist.
     * @param   userID  The name of the user.
     * @return  The number of connections that are currently in use.
     **/
    public int getActiveConnectionCount(String systemName, String userID)
    {
        if (systemName == null) throw new NullPointerException("systemName");
        if (userID == null) throw new NullPointerException("userID");
        
        systemName = AS400.resolveSystem(systemName);
        userID = AS400.resolveUserId(userID.toUpperCase());
        String key = createKey(systemName, userID);
        
        if (Trace.traceOn_) log(Trace.INFORMATION, "getActiveConnectionCount key= " + key);
    
        ConnectionList connections = as400ConnectionPool_.get(key);
        if (connections == null)
        {
            if (Trace.traceOn_) log(Trace.WARNING, "getActiveConnectionCount found no " + key + " list in the pool");
            return 0;
        }
        
        return(connections.getActiveConnectionCount());
    }


    /**
     * Get the number of available connections to a system. 
     * Note: The value returned is based only on systemName and userID, and does not
     * reflect the authentication scheme (e.g. password, profile token).
     *
     * @param   systemName  The name of the system where the connections exist.
     * @param   userID  The name of the user.
     * @return  The number of connections that are not currently in use.
     **/
    public int getAvailableConnectionCount(String systemName, String userID)
    {
        if (systemName == null) throw new NullPointerException("systemName");
        if (userID == null) throw new NullPointerException("userID");
        
        systemName = AS400.resolveSystem(systemName);
        userID = AS400.resolveUserId(userID.toUpperCase());
        String key = createKey(systemName, userID);
        
        if (Trace.traceOn_)  log(Trace.INFORMATION, "getAvailableConnectionCount key= " + key);
        
        ConnectionList connections = as400ConnectionPool_.get(key);
        if (connections == null)
        {
            if (Trace.traceOn_) log(Trace.WARNING, "getAvailableConnectionCount found no " + key + " list in the pool");
            return 0;
        }
        
        return connections.getAvailableConnectionCount();
    }

    /**
     * Returns the CCSID that is used when creating new connections.
     * The default value is the system default CCSID as determined by the AS400 class.
     * @return The CCSID, or {@link #CCSID_DEFAULT CCSID_DEFAULT} if the system default CCSID is used.
     **/
    public int getCCSID() {
        return super.getCCSID();
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
     * @deprecated
     **/
    @Deprecated
    public AS400 getConnection(String systemName, String userID, String password, int service) throws ConnectionPoolException
    {
        char[] passwordChars = (password != null) ? password.toCharArray() : null;
        AS400 answer = getConnection(systemName, userID, passwordChars, service);
        CredentialVault.clearArray(passwordChars);
        
        return answer;
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
    public AS400 getConnection(String systemName, String userID, char[] password, int service) throws ConnectionPoolException
    {
        try
        {
            //@B4D AS400.addPasswordCacheEntry(systemName, userID, password);
            AS400 releaseConnection = getConnection(systemName, userID, service, true, false, null, password);
            if (poolListeners_ != null)
            {
                ConnectionPoolEvent event = new ConnectionPoolEvent(releaseConnection, ConnectionPoolEvent.CONNECTION_RELEASED);
                poolListeners_.fireConnectionReleasedEvent(event); 
            }
            
            return releaseConnection;
        }
        catch (AS400SecurityException|IOException e) {
            throw new ConnectionPoolException(e);
        }
    }

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
     * @deprecated Use getConnection(String systemName, String userID, char[] password, int service, Locale locale) instead. 
     **/
    @Deprecated
    public AS400 getConnection(String systemName, String userID, String password, int service, Locale locale) throws ConnectionPoolException
    {
        char[] passwordChars = (password != null) ? password.toCharArray() : null;
        AS400 answer =  getConnection(systemName, userID, passwordChars, service, locale);
        CredentialVault.clearArray(passwordChars);
        
        return answer;
    } 
  
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
    public AS400 getConnection(String systemName, String userID, char[] password, int service, Locale locale) throws ConnectionPoolException
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
        catch (AS400SecurityException|IOException e) {
            throw new ConnectionPoolException(e);
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
     * @deprecated  Use getConnection(String systemName, String userID, char[] password, int service) instead.
     **/
    @Deprecated
    public AS400 getConnection(String systemName, String userID, int service) throws ConnectionPoolException
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
     * @deprecated Use getConnection(String systemName, String userID, char[] password) instead. 
    **/ 
    @Deprecated
    public AS400 getConnection(String systemName, String userID, String password) throws ConnectionPoolException
    {    
        char[] passwordChars = (password != null) ? password.toCharArray() : null; 
        AS400 as400 = getConnection(systemName, userID, passwordChars);
        CredentialVault.clearArray(passwordChars);
        
        return as400; 
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
    public AS400 getConnection(String systemName, String userID, char[] password) throws ConnectionPoolException
    {    
        try
        {
            //@B4D AS400.addPasswordCacheEntry(systemName, userID, password);
            AS400 releaseConnection = getConnection(systemName, userID, 0, false, false, null, password);
            if (poolListeners_ != null)
            {
                ConnectionPoolEvent event = new ConnectionPoolEvent(releaseConnection, ConnectionPoolEvent.CONNECTION_RELEASED);
                poolListeners_.fireConnectionReleasedEvent(event); 
            }
            
            return releaseConnection; 
        }
        catch (AS400SecurityException|IOException e) {
            throw new ConnectionPoolException(e);
        }
    }
    
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
     * @deprecated Use getConnection(String systemName, String userID, char[] password, Locale locale)  instead. 
     **/ 
    @Deprecated
    public AS400 getConnection(String systemName, String userID, String password, Locale locale)  throws ConnectionPoolException
    {    
        char[] passwordChars = (password != null) ? password.toCharArray() : null; 
        AS400 as400 = getConnection(systemName, userID, passwordChars, locale);
        CredentialVault.clearArray(passwordChars);
        
        return as400; 
    }

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
    public AS400 getConnection(String systemName, String userID, char[] password, Locale locale) throws ConnectionPoolException
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
        catch (AS400SecurityException|IOException e) {
            throw new ConnectionPoolException(e);
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
     * @deprecated  Use method with password instead.
     **/ 
    @Deprecated
    public AS400 getConnection(String systemName, String userID) throws ConnectionPoolException
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
    public AS400 getConnection(String systemName, String userID, ProfileTokenCredential profileToken, int service) throws ConnectionPoolException
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
        catch (AS400SecurityException|IOException e) {
            throw new ConnectionPoolException(e);
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
    public AS400 getConnection(String systemName, String userID, ProfileTokenCredential profileToken, int service, Locale locale) throws ConnectionPoolException
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
        catch (AS400SecurityException | IOException e) {
            throw new ConnectionPoolException(e);
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
    public AS400 getConnection(String systemName, String userID, ProfileTokenCredential profileToken) throws ConnectionPoolException
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
        catch (AS400SecurityException|IOException e) {
            throw new ConnectionPoolException(e);
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
    public AS400 getConnection(String systemName, String userID, ProfileTokenCredential profileToken, Locale locale) throws ConnectionPoolException
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
        catch (AS400SecurityException|IOException e) {
            throw new ConnectionPoolException(e);
        }
    }
    
    /** 
     * Get an AS400 object from the connection pool with similar properties to passed-in AS400 object.  
     * If an appropriate one is not found, one is created based on the passed-in AS400 object. 
     * The AS400 object being passed in must have system and authentication information specified.
     * If the maximum connection limit has been reached, an exception
     * will be thrown.  The AS400 object may not be connected to any services.
     *
     * @param system An AS400 object that will be used to create new AS400 connections. 
     * @return     An AS400 object.
     * 
     * @exception ConnectionPoolException If a connection pool error occurred.
     *
     **/
    public AS400 getConnection(AS400 system)  throws ConnectionPoolException 
    {
        if (system == null) throw new NullPointerException("system");
        
        try {
            return getConnection(AS400.resolveSystem(system.getSystemName()), AS400.resolveUserId(system.getUserId()), 0, false, system.isSecure(), system.getLocale(), null, system);
        } catch (AS400SecurityException|IOException e) {
            throw new ConnectionPoolException(e);
        }
    }
    
    /** 
     * Get an AS400 object from the connection pool with similar properties to passed-in AS400 object.  
     * If an appropriate one is not found, one is created based on the passed-in AS400 object.  
     * The AS400 object being passed in must have system and authentication information specified.
     * If the maximum connection limit has been reached, an exception
     * will be thrown.  The AS400 object may not be connected to any services.
     *
     * @param system An AS400 object that will be used to create new AS400 connections. 
     * @param service The service to be connected. See the service number constants defined by AS400 class.
     * @return     An AS400 object.
     * 
     * @exception ConnectionPoolException If a connection pool error occurred.
     *
     **/
    public AS400 getConnection(AS400 system, int service)  throws ConnectionPoolException 
    {
        if (system == null) throw new NullPointerException("system");
        
        try {
            return getConnection(AS400.resolveSystem(system.getSystemName()), AS400.resolveUserId(system.getUserId()), service, true, system.isSecure(), system.getLocale(), null, system);
        } catch (AS400SecurityException|IOException e) {
            throw new ConnectionPoolException(e);
        }
    }
  
    private AS400 getConnection(String systemName, String userID, int service, boolean connect, 
            boolean secure, Locale locale, AS400ConnectionPoolAuthentication poolAuth, AS400 rootSystem)  throws AS400SecurityException, IOException, ConnectionPoolException 
    {
        if (systemName == null) throw new NullPointerException("systemName");
        if (systemName.length() == 0) throw new ExtendedIllegalArgumentException("systemName", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        if (userID == null) throw new NullPointerException("userID");
        if (userID.length() == 0) throw new ExtendedIllegalArgumentException("userID", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

        if (Trace.traceOn_)  log(Trace.INFORMATION, "getConnection() key before resolving= " + systemName + "/" + userID);

        systemName = AS400.resolveSystem(systemName);
        userID = AS400.resolveUserId(userID.toUpperCase());

        String key = createKey(systemName, userID);
        
        if (Trace.traceOn_) log(Trace.INFORMATION, "getConnection() key after resolving= " + key);

        if (!isInUse())          
            setInUse(true);     // threadUsed property can now not be changed. 

        //Work with maintenance thread
        if (isRunMaintenance())
        {
            if (isThreadUsed())
            {
                // Start thread if it has not been initialized.
                if (maintenance_ == null)
                {
                    synchronized (this)
                    {
                        if (maintenance_ == null)
                        {
                            maintenance_ = new PoolMaintenance(this);
                            maintenance_.start();
                        }
                    }
                }
            
                // Restart the thread.
                if (!maintenance_.isRunning())
                    maintenance_.setRunning(true);
            }
            else if ((System.currentTimeMillis() - lastRun_) > getCleanupInterval())
            {
                // Running single-threaded and cleanup interval has elapsed, run cleanup 
                cleanupConnections();
            }
        }

        // Create a connection list if one does not already exist based on the host name and user ID
        ConnectionList connections = as400ConnectionPool_.get(key);

        if (connections == null)
        {
            synchronized (as400ConnectionPool_)
            {
                connections = as400ConnectionPool_.get(key);

                if (connections == null)
                {
                    if (log_ != null || Trace.traceOn_)
                        log(ResourceBundleLoader.substitute(ResourceBundleLoader.getText("AS400CP_CONNLIST"), new String[] {systemName, userID} ));

                    connections = new ConnectionList(systemName, userID, properties_);
                    connections.setLog(log_);

                    as400ConnectionPool_.put(key, connections);
                }
            }
        }

        // We don't want to hold the lock on the entire pool if we are trying to get a connection
        // for a system that is down or non-existent. ConnectionList.getConnection() is synchronized
        // inside itself, anyway. Get a connection from the list.
        AS400 connection = connections.getConnection(connect ? service : null, secure, poolListeners_, locale, poolAuth, socketProperties_, getCCSID(), rootSystem).getAS400Object();

        connectionHasBeenCreated_ = true;  // remember that we've created at least 1 connection
    
        return connection;
    }

    private AS400 getConnection(String systemName, String userID, int service, boolean connect, boolean secure, Locale locale, char[] password)
            throws AS400SecurityException, IOException, ConnectionPoolException 
    {
        AS400ConnectionPoolAuthentication poolAuth = new AS400ConnectionPoolAuthentication(password);
        return (getConnection(systemName, userID, service, connect, secure, locale, poolAuth, null));
    }


    private AS400 getConnection(String systemName, String userID, int service, boolean connect, boolean secure, Locale locale,  ProfileTokenCredential profileToken)
          throws AS400SecurityException, IOException, ConnectionPoolException 
    {
        AS400ConnectionPoolAuthentication poolAuth = new AS400ConnectionPoolAuthentication(profileToken);
        return (getConnection(systemName, userID, service, connect, secure, locale, poolAuth, null));
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
     * @deprecated Use  getSecureConnection(String systemName, String userID, char[] password) instead.
     **/  
    @Deprecated
    public AS400 getSecureConnection(String systemName, String userID, String password) throws ConnectionPoolException
    {
        char[] passwordChars = (password != null) ? password.toCharArray() : null;
        AS400 as400 = getSecureConnection(systemName, userID, passwordChars);
        CredentialVault.clearArray(passwordChars);

        return as400;
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
    public AS400 getSecureConnection(String systemName, String userID, char[] password) throws ConnectionPoolException
    {    
        try
        {
            //@B4D AS400.addPasswordCacheEntry(systemName, userID, password);
            AS400 releaseConnection = getConnection(systemName, userID, 0, false, true, null, password);
            releaseConnection.getVRM();
            if (poolListeners_ != null)
            {
                ConnectionPoolEvent event = new ConnectionPoolEvent(releaseConnection, ConnectionPoolEvent.CONNECTION_RELEASED); 
                poolListeners_.fireConnectionReleasedEvent(event); 
            }
            
            return releaseConnection; 
        }
        catch (AS400SecurityException|IOException e) {
            throw new ConnectionPoolException(e);
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
     * @deprecated  Use getSecureConnection(String systemName, String userID, char[] password) instead.
     **/  
    @Deprecated
    public AS400 getSecureConnection(String systemName, String userID) throws ConnectionPoolException
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
     * @deprecated Use getSecureConnection(String systemName, String userID, char[] password, int service) instead. 
     **/
    @Deprecated
    public AS400 getSecureConnection(String systemName, String userID, String password, int service) throws ConnectionPoolException
    {
        char[] passwordChars = (password != null) ? password.toCharArray() : null;
        AS400 as400 = getSecureConnection(systemName, userID, passwordChars, service);
        CredentialVault.clearArray(passwordChars);
        return as400;
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
    public AS400 getSecureConnection(String systemName, String userID, char[] password, int service) throws ConnectionPoolException
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
        catch (AS400SecurityException|IOException e) {
            throw new ConnectionPoolException(e);
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
     * @deprecated  Use getConnection(String systemName, String userID, char[] password, int service) instead.
     **/
    @Deprecated
    public AS400 getSecureConnection(String systemName, String userID, int service) throws ConnectionPoolException
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
    public AS400 getSecureConnection(String systemName, String userID, ProfileTokenCredential profileToken) throws ConnectionPoolException
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
        catch (AS400SecurityException|IOException e) {
            throw new ConnectionPoolException(e);
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
    public AS400 getSecureConnection(String systemName, String userID, ProfileTokenCredential profileToken, int service) throws ConnectionPoolException
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
        catch (AS400SecurityException|IOException e) {
            throw new ConnectionPoolException(e);
        }
    }

    /**
       Returns a copy of the socket properties that this AS400ConnectionPool specifies when it creates new AS400 objects,
       for example in <tt>fill()</tt>, <tt>getConnection()</tt>, or <tt>getSecureConnection()</tt>.
       @return  The socket properties.  A null value indicates that this AS400ConnectionPool specifies no socket properties.
    **/
    public SocketProperties getSocketProperties()
    {
        if (Trace.traceOn_) log(Trace.DIAGNOSTIC, "Getting socket properties.");

        if (socketProperties_ == null)
            return null;

        SocketProperties socketProperties = new SocketProperties();
        socketProperties.copyValues(socketProperties_);
        return socketProperties;
    }

    /**
     * Get a list of the system names in the pool.
     *
     * @return     A list of the system names in the pool.
     *             If the the pool is empty, returns a zero-length list.
     **/  
    public String[] getSystemNames()
    {
        Enumeration<String> keys = as400ConnectionPool_.keys();
        Vector<String> hosts = new Vector<>();
    
        while (keys.hasMoreElements())
        {
            String key = keys.nextElement();
            String host = key.substring(0, key.indexOf("/"));
            if (!hosts.contains(host))
                hosts.addElement(host);
        }
        
        String[] hostsArr = new String[hosts.size()];
        hosts.copyInto(hostsArr);
        
        return hostsArr;
    }

    /**
     * Get an enumeration of the systemName/userId pairs in the pool.
     * Note: The returned information is regardless of the authentication 
     * scheme used when filling the pool (e.g. password, profile token).
     *
     * @return     An enumeration of the systemName/userIds in the pool
     **/  
    public Enumeration<String> getUsers() {
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
        Enumeration<String> keys = as400ConnectionPool_.keys();
        Vector<String> users = new Vector<>();
        systemName = systemName.toUpperCase().trim();
        String compareKey = systemName + "/";
        
        while (keys.hasMoreElements())
        {
            String key = keys.nextElement();
            if (key.startsWith(compareKey))
            {
                ConnectionList connections = as400ConnectionPool_.get(key);
                if (!listConnectedOnly || connections.hasConnectedConnection())
                {
                    String user = key.substring(key.indexOf("/") + 1);
                    if (!users.contains(user))
                        users.addElement(user);
                }
            }
        }
        
        String[] usersArr = new String[users.size()];
        users.copyInto(usersArr);
        return usersArr;
    }

    private void initializeTransient()
    {
        // log_ was originally not transient, however the EventLog class (one of the possible
        // implementations of the Log interface) uses a java.io.PrintWriter object which is 
        // not serializable.  Therefore, log_ was changed to be transient and the user
        // will need to reset log_ after a serialization of the pool.
        as400ConnectionPool_ = new Hashtable<>();
        removedAS400ConnectionPool_ = new Hashtable<>();
        lastRun_ = System.currentTimeMillis();
        connectionHasBeenCreated_ = false;
    }

    private final void log(String msg)
    {
        if (Trace.traceOn_) Trace.log(Trace.INFORMATION, msg);
        
         if (log_ != null)
             log_.log(msg);
    }

    private final void log(int category, String msg)
    {
        if (Trace.traceOn_ && Trace.isTraceOn(category))
        {
            Trace.log(category, msg);
            
            if (log_ != null)
                log_.log(msg);
        }
    }

    private final void log(Exception exception, String msg)
    {
        if (Trace.traceOn_) Trace.log(Trace.ERROR, msg, exception);
        
        if (log_ != null)
            log_.log(msg, exception);
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
        if (systemName == null) throw new NullPointerException("systemName");
        if (userID == null) throw new NullPointerException("userID");
        
        if (Trace.traceOn_) log(Trace.INFORMATION, "removeFromPool("+systemName+","+userID);

        systemName = AS400.resolveSystem(systemName);  
        userID = AS400.resolveUserId(userID.toUpperCase());
        String key = createKey(systemName, userID);
        ConnectionList listToBeRemoved = as400ConnectionPool_.get(key);
        
        if (listToBeRemoved != null)
        {
            listToBeRemoved.removeUnusedElements();  // this disconnects the connections
            removedAS400ConnectionPool_.put(key, listToBeRemoved);
            as400ConnectionPool_.remove(key);
        }
        else if (Trace.traceOn_)
            log(Trace.WARNING, "A list of connections for: " + key + " does not exist.");
    } 

    private static final boolean DISCARD_CONNECTION = true;

    /**
     * Remove the specified connection from the pool, and disconnect it.
     * Call this function if a specific connection has been discovered to
     * no longer be valid.
     * <p>
     * This method should only be called by the process or thread to which
     * the connection is currently allocated; that is,
     * the process or thread that most recently obtained the connection
     * via <tt>getConnection()</tt> or <tt>getSecureConnection()</tt>.
     * To remove the connection from the pool, the process or thread
     * holding the connection should call this method <i>rather than</i>
     * {@link #returnConnectionToPool returnConnectionToPool()}.
     * <p>
     * Caution: The pool does not verify that the process or thread removing
     * the connection, is the same as the process or thread to which
     * the connection is currently allocated.
     * This may cause unpredictable results if the connection is in use
     * by another process or thread.
     *
     * @param   system  The system to remove from the pool.
     **/
    public void removeFromPool(AS400 system)
    {
        if (system == null) throw new NullPointerException("system");
        
        if (Trace.traceOn_)
            log(Trace.INFORMATION, "removeFromPool() key= " + system.getSystemName() + "/" + system.getUserId() + "; hashcode= " + system.hashCode());

        // Remove the connection from the pool, and disconnect it.
        acceptReturnedConnection(system, DISCARD_CONNECTION);
    } 

    /**
     * Return the AS400 object to the connection pool.
     * <p>
     * This method should only be called by the process or thread to which
     * the connection is currently allocated; that is,
     * the process or thread that most recently obtained the connection
     * via <tt>getConnection()</tt> or <tt>getSecureConnection()</tt>.
     * <p>
     * Caution: The pool does not verify that the process or thread returning
     * the connection, is the same as the process or thread to which
     * the connection is currently allocated.
     * This may cause unpredictable results if the connection is in use
     * by another process or thread.
     *
     * @param   system  The system to return to the pool.
     * @see #removeFromPool(AS400)
     **/
    public void returnConnectionToPool(AS400 system)
    {
        // This method searches the lists of connections for a reference to the AS400
        // object that was returned.  There will be one list per systemName/userId key.
        if (system == null) throw new NullPointerException("system");
        
        if (Trace.traceOn_)
            log(Trace.INFORMATION, "returnConnectionToPool() key= " + system.getSystemName() + "/" + system.getUserId());

        // Return the connection to the pool, making it available for re-use.
        acceptReturnedConnection(system, !DISCARD_CONNECTION);

        //If running single-threaded and cleanup interval has elapsed, run cleanup.
        if (!isThreadUsed() && isRunMaintenance() && 
                ((System.currentTimeMillis() - lastRun_) > getCleanupInterval()))
        {
            cleanupConnections();
        }
    }

    private void acceptReturnedConnection(AS400 system, boolean discardConnection)
    {
        String key = createKey(system.getSystemName(), system.getUserId());
        ConnectionList connections = as400ConnectionPool_.get(key);
        PoolItem poolItem = null;

        // First look for the list for the systemName/userId key for the AS400 object.
        // If such a list exists, search that list for a reference to the AS400 object returned.
        if (connections != null)
            poolItem = connections.findElement(system);

        // If such an item is found, set it "not in use" and send an event that the connection was returned to the pool.
        if (poolItem != null)
        {
            if (discardConnection)  // caller wants the connection deleted from the pool
            {
                if (Trace.traceOn_)
                    log(Trace.DIAGNOSTIC, "Disconnecting pooled connection because removeFromPool() was invoked for that connection.");
                connections.removeElement(system);  
                poolItem.getAS400Object().resetAllServices();
            }
            else  // caller is simply returning the connection to the pool
            {
                // Before making the connection available for re-use, see if it's expired.
                boolean removed = connections.removeIfExpired(poolItem, poolListeners_);
                if (!removed)
                    poolItem.setInUse(false); // indicate that this connection is available

                if (log_ != null || Trace.traceOn_)
                    log(ResourceBundleLoader.substitute(ResourceBundleLoader.getText("AS400CP_RETCONN"), new String[] {system.getSystemName(), system.getUserId()} ));

                if (poolListeners_ != null)
                {
                    ConnectionPoolEvent event = new ConnectionPoolEvent(poolItem.getAS400Object(), ConnectionPoolEvent.CONNECTION_RETURNED); //@A7C
                    poolListeners_.fireConnectionReturnedEvent(event);
                }
            }
        }

        // If the item was not found, search all the lists, looking for a reference to that 
        // item.  If it is found, disconnect it and remove it from the pool since its
        // systemName/userId has been changed from what was expected.
        if (poolItem == null)
        {
            Enumeration<String> keys = as400ConnectionPool_.keys();
            while (keys.hasMoreElements())
            {
                String tryKey = keys.nextElement();     
                ConnectionList connList = as400ConnectionPool_.get(tryKey);
                poolItem = connList.findElement(system);
                
                if (poolItem != null)
                {
                    if (Trace.traceOn_)
                        log(Trace.WARNING, "Disconnecting pooled connection because it was returned, and belongs to a different list than expected.");
                    
                    connList.removeElement(system);  
                    poolItem.getAS400Object().disconnectAllServices();
                    break;
                }
            } 
        }

        // A list of connections will
        // be moved into the removed pool if removeFromPool() with the systemName/userId of the
        // list is called.  This allows us to keep references to an AS400 object until
        // the user calls returnConnectionToPool() on it, but not give it out again 
        // to the user.
        // Code was added here to handle checking in removed pool for the connection
        // and removing it from that pool if it is found.
    
        // If the pooled connection was not found in the regular lists, start looking 
        // in the lists in the removed pool.
        if (poolItem == null)
        {
            ConnectionList removedConnections = removedAS400ConnectionPool_.get(key);

            // Start looking in the list with the systemName/userId combination.
            if (removedConnections != null)
                poolItem = removedConnections.findElement(system);

            // If the object is found, disconnect it and remove the element from removed pool.
            if (poolItem != null)
            {
                if (Trace.traceOn_)
                    log(Trace.DIAGNOSTIC, "Disconnecting pooled connection because it was returned, and removeFromPool() has been called for its systemName/userID.");

                removedConnections.removeElement(system); 
                poolItem.getAS400Object().resetAllServices();
                
                if (log_ != null || Trace.traceOn_)
                    log(ResourceBundleLoader.substitute(ResourceBundleLoader.getText("AS400CP_RETCONN"), new String[] {system.getSystemName(), system.getUserId()} ));
                
                if (poolListeners_ != null)
                {
                    ConnectionPoolEvent event = new ConnectionPoolEvent(poolItem.getAS400Object(), ConnectionPoolEvent.CONNECTION_RETURNED); //@A7C
                    poolListeners_.fireConnectionReturnedEvent(event);
                }
            }

            // If the object was not found, search through every list in the removed pool,
            // looking for a reference to the object.
            if (poolItem == null)
            {
                Enumeration<String> keys = removedAS400ConnectionPool_.keys();
                while (keys.hasMoreElements())
                {
                    String tryKey = keys.nextElement();     
                    ConnectionList connList = removedAS400ConnectionPool_.get(tryKey);
                    poolItem = connList.findElement(system);
                    
                    if (poolItem != null)
                    {
                        if (Trace.traceOn_)
                            log(Trace.DIAGNOSTIC, "Disconnecting pooled connection because it was returned, and removeFromPool() has been called for its systemName/userID.");
                        
                        connList.removeElement(system);
                        poolItem.getAS400Object().resetAllServices();
                        break;
                    }
                } 
            }

            // If the object was not found in any of our lists, it does not belong to this pool.
            // In that case, just disconnect the connection.
            if (poolItem == null)
            {
                if (discardConnection) // the caller wants the connection disconnected
                {
                    if (Trace.traceOn_)
                        log(Trace.WARNING, "Disconnecting pooled connection because removeFromPool(AS400) was called. The connection is not currently a member of this pool.");
                }
                else
                    log(Trace.ERROR, "Disconnecting pooled connection because it was returned, and the connection is not currently a member of this pool.");

                system.resetAllServices();
            }
        }
    }

    /**
     *  Notify the maintenance thread to run cleanupConnections().
     *  @param reduced true if need to check current num connections; false otherwise.
     **/
    void runMaintenance(boolean reduced)
    {
        if (maintenance_ == null) 
            return;
        
        synchronized(maintenance_)
        {
            if (maintenance_.isRunning())
            {
                if (reduced)
                {
                    synchronized (as400ConnectionPool_)
                    {
                        Enumeration<String> keys = as400ConnectionPool_.keys();
                        while (keys.hasMoreElements())
                        {
                            String key = keys.nextElement();
                            try
                            {
                                ConnectionList connList = as400ConnectionPool_.get(key);
                                connList.shutDownOldest(); 
                            }
                            catch (Exception e)
                            {
                                if (log_ != null || Trace.traceOn_)
                                    log(e, key);
                            }
                        }
                    }
                }

                synchronized (removedAS400ConnectionPool_)                              
                {
                    Enumeration<String> removedKeys = removedAS400ConnectionPool_.keys();       
                    while (removedKeys != null && removedKeys.hasMoreElements())
                    {
                        //go through each list of systemName/userID
                        String key = removedKeys.nextElement();                                                                                 
                        ConnectionList connList = removedAS400ConnectionPool_.get(key); 
                        //disconnect and remove any unused connections from the list
                        if (!connList.removeUnusedElements())  // this disconnects the connections
                        {
                            //if there are no more connections remaining, remove the 
                            //list from the pool
                            removedAS400ConnectionPool_.remove(key);                
                        }

                    }                                                                   
                }                                                                       

                maintenance_.notify();  // PoolMaintenance.run() calls ConnectionPool.cleanupConnections()
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
        if (Trace.traceOn_)
        {
            Trace.log(Trace.INFORMATION, "setCCSID("+ccsid+")");
            
            if (connectionHasBeenCreated_)
                log(Trace.WARNING, "setCCSID() was called after the pool already contains connections.");
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
        if (Trace.traceOn_) {
            String val = (log == null ? "null" : log.toString());
            Trace.log(Trace.INFORMATION, "setLog(" + val + ")");
        }
      
        this.log_ = log;
    }

    /**
       Sets the socket properties that this AS400ConnectionPool specifies when it creates new AS400 objects, 
       for example in <tt>fill()</tt>, <tt>getConnection()</tt>, or <tt>getSecureConnection()</tt>.
       <br>Note that <tt>properties</tt> will apply only to AS400 objects created <em>after</em> this method is called.  
       Any AS400 objects already in the pool are not affected.

       @param properties  The socket properties.  If null, then this AS400ConnectionPool will specify no socket 
                          properties when it creates new AS400 objects.  That is, <tt>setSocketProperties(null)</tt> cancels the 
                          effects of any previous <tt>setSocketProperties()</tt>.
    **/
    public void setSocketProperties(SocketProperties properties)
    {
        if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "setSocketProperties()");

        socketProperties_ = properties;
    }
}
