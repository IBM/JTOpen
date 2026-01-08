///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ConnectionList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
// @C1 - 2008-06-06 - Added support for ProfileTokenCredential authentication
//                    by using AS400ConnectionPoolAuthentication class.
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Vector;    // Java 2
import java.io.IOException;
import java.util.Locale;      //@B2A

/** 
  *  ConnectionList is a list of connections specific to an IBM i system and userID.  The 
  *  connection list is used to create new connections and get connections from the pool.
  *  The connection list can remove connections that have exceeded inactivity time and 
  *  replace connections that have exceeded the maximum use count or maximum lifetime.
 **/
final class ConnectionList 
{
    // Values returned by checkConnectionExpiration().
    // Note: These correspond to MRI text IDs in class MRI2.
    private static final String NOT_EXPIRED = null;
    private static final String EXPIRED_INACTIVE = "CL_REMUNUSED";
    private static final String EXPIRED_MAX_LIFETIME = "CL_REMLIFE";
    private static final String EXPIRED_MAX_USE_COUNT = "CL_REMUSECOUNT";
    private static final String EXPIRED_MAX_USE_TIME = "CL_REMUSETIME";
    private static final String EXPIRED_FAILED_PRETEST = "CL_REMPRETEST";

    private String systemName_;
    private String userID_;
    private ConnectionPoolProperties properties_;
    private Log log_;
    private Vector connectionList_ = new Vector(); 

    /**
     *  Construct a ConnectionList object.  
     *  @param systemName The system where the ConnectionList will exist.
     *  @param userID The name of the user.
     *  @param properties The properties of the ConnectionList.
    **/
    ConnectionList(String systemName, String userID, ConnectionPoolProperties properties)  
    {
        if (systemName == null) throw new NullPointerException("systemName");
        if (systemName.length() == 0) throw new ExtendedIllegalArgumentException("systemName", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        if (userID == null) throw new NullPointerException("userID");
        if (userID.length() == 0) throw new ExtendedIllegalArgumentException("userID", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        if (properties == null) throw new NullPointerException("properties");

        this.systemName_ = systemName;
        this.userID_ = userID;
        this.properties_ = properties;
    }

    /**
     * Sees if the specified connection is due for removal.
     *
     * @param poolItem The pool item.
     * @return The MRI textID specifying the type of expiration, or null if not expired.
     **/
    private String checkConnectionExpiration(PoolItem poolItem)
    {
        // See if the item has exceeded the maximum inactivity time.
        if ((properties_.getMaxInactivity() >= 0) && (poolItem.getInactivityTime() >= properties_.getMaxInactivity()))
            return EXPIRED_INACTIVE;

        // See if the item has exceeded the maximum use count.
        if ((properties_.getMaxUseCount() >= 0) && (poolItem.getUseCount() >= properties_.getMaxUseCount()))
            return EXPIRED_MAX_USE_COUNT;

        // See if the item has exceeded the maximum lifetime.
        if ( (properties_.getMaxLifetime() >= 0) && (poolItem.getLifeSpan() >= properties_.getMaxLifetime()))
            return EXPIRED_MAX_LIFETIME;

        // See if the item has exceeded the maximum use time.
        if ((properties_.getMaxUseTime() >= 0) && (poolItem.getInUseTime() >= properties_.getMaxUseTime()))
            return EXPIRED_MAX_USE_TIME;

        return NOT_EXPIRED;
    }

    /**
     *  Close the connection.
     **/
    void close()
    {
        if (log_ != null || Trace.traceOn_) log(ResourceBundleLoader.getText("CL_CLEANUP", new String[] {systemName_, userID_} ));
        
        synchronized (connectionList_) 
        {
            int size = connectionList_.size();  //@A5M
            for (int i=0; i<size; i++)
            {
                PoolItem p = (PoolItem)connectionList_.elementAt(i); 
                p.getAS400Object().resetAllServices();
            }
            connectionList_.removeAllElements();   
        }
        
        if (log_ != null || Trace.traceOn_) log(ResourceBundleLoader.getText("CL_CLEANUPCOMP"));
    }

    /**
     *  Return a connection, which can connect to a service.
     *
     *  @param service The service to connect.
     *  @param connect If true connect the specified service.
     *  @param secure  If true a secure AS400 object was requested.
     *  @param poolListeners The pool listeners to which events will be fired. 
     *  @param socketProperties The socket properties to assign to the new AS400 object.
     *  If null, this parameter is ignored.
     *  @return The PoolItem that was connected.
     *  @exception AS400SecurityException If a security error occured.
     *  @exception IOException If a communications error occured.
     *  @exception ConnectionPoolException If max connection limit is reached.
     **/
    private PoolItem createNewConnection(int service, boolean connect, boolean secure, 
                                       ConnectionPoolEventSupport poolListeners, Locale locale, 
                                       AS400ConnectionPoolAuthentication poolAuth, SocketProperties socketProperties, int ccsid, AS400 rootSystem)
                                               throws AS400SecurityException, IOException, ConnectionPoolException
    {     
        if (log_ != null || Trace.traceOn_) log(ResourceBundleLoader.getText("CL_CREATING", new String[] {systemName_, userID_} ));

        if ((properties_.getMaxConnections() > 0) && (getConnectionCount() >= properties_.getMaxConnections()))
        {
            if (log_ != null || Trace.traceOn_) log(ResourceBundleLoader.getText("CL_CLEANUPEXP"));
      
            // see if anything frees up
            removeExpiredConnections(poolListeners);

            // if that didn't do the trick, try shutting down unused connections
            if (getConnectionCount() >= properties_.getMaxConnections())
            {
                if (log_ != null || Trace.traceOn_) log(ResourceBundleLoader.getText("CL_CLEANUPOLD"));
                
                shutDownOldest(); 
                
                // if not enough connections were freed, throw an exception!
                if (getConnectionCount() >= properties_.getMaxConnections())
                    throw new ConnectionPoolException(ConnectionPoolException.MAX_CONNECTIONS_REACHED);
            }
        }

        boolean threadUse = properties_.isThreadUsed();
        boolean virtualThreads = properties_.isVirtualThreads();
        // create a new connection
        PoolItem sys = new PoolItem (systemName_, userID_, poolAuth, secure, locale, service, connect, threadUse, virtualThreads, socketProperties, ccsid, rootSystem);

        // set the item is in use since we are going to return it to caller
        sys.setInUse(true);
        connectionList_.addElement(sys);  

        if (poolListeners != null)
        {
            ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(sys.getAS400Object(), ConnectionPoolEvent.CONNECTION_CREATED);
            poolListeners.fireConnectionCreatedEvent(poolEvent);  
        }
        
        if (log_ != null || Trace.traceOn_) log(ResourceBundleLoader.getText("CL_CREATED", new String[] {systemName_, userID_} ));

        return sys;
    }

    /**
     *  Return the poolItem in the list that contains this AS400 instance.
     *
     *  @return The matching poolItem.
     **/
    PoolItem findElement(AS400 systemToFind)
    {
        synchronized (connectionList_)
        {
            int size = connectionList_.size();        
            for (int i=0; i<size; i++)
            {
                PoolItem item = (PoolItem)connectionList_.elementAt(i);
                if (item.getAS400Object().equals(systemToFind))
                    return item;
            }
        }
        
        return null;
    }

    /**
     *  Return the number of active connections (that is, connections that are in use).
     *
     *  @return The number of active connections.
     **/
    public int getActiveConnectionCount()
    {
        int count = 0;
        synchronized (connectionList_) 
        {
            int size = connectionList_.size();    
            for (int i=0; i<size; i++)
            {
                PoolItem p = (PoolItem)connectionList_.elementAt(i);      
                if (p.isInUse())
                    count++;
            }
        }
        
        return count;
    }

    /**
     *  Return the number of available connections (that is, connections that are not in use).
     *
     *  @return The number of available connections.
     **/
    public int getAvailableConnectionCount()
    {
        int count = 0;
        synchronized (connectionList_) 
        {
            int size = connectionList_.size();    
            for (int i=0; i<size; i++)
            {
                PoolItem p = (PoolItem)connectionList_.elementAt(i);      
                if (!p.isInUse())
                    count++;
            }
        }
        
        return count;
    }

    /**
     *  Gets a connection to a service from the pool.
     *
     *  @param service The service.
     *  @param secure  If true a secure AS400 object was requested.
     *  @param poolListeners The pool listeners to which events will be fired.
     *  @param socketProperties The socket properties to use if a new AS400 object is created.
     *  If null, this parameter is ignored.
     *  @exception AS400SecurityException If a security error occured.
     *  @exception IOException If a communications error occured.
     *  @exception ConnectionPoolException If a connection pool error occured.
     *  @return The pool item.
     **/
    PoolItem getConnection(Integer service, boolean secure, ConnectionPoolEventSupport poolListeners, Locale locale, 
          AS400ConnectionPoolAuthentication poolAuth, SocketProperties socketProperties, int ccsid, AS400 rootSystem) 
                  throws AS400SecurityException, IOException, ConnectionPoolException
    {
        PoolItem poolItem = null;
        boolean pretestConnections = properties_.isPretestConnections();
        
        synchronized (connectionList_)
        {
            int size = connectionList_.size();       
            for (int i=0; i<size; i++)
            {
                PoolItem item = (PoolItem)connectionList_.elementAt(i); 
                
                // If in-use or not right type of AS400 object, skip it. 
                if (item.isInUse()
                        || (secure && !(item.getAS400Object().isSecure()))
                        || (!secure && (item.getAS400Object().isSecure()))
                        || (service != null && !item.getAS400Object().isConnected(service)))
                    continue;
                
                if (((item.getLocale() == null && locale == null) 
                        || (locale != null && (item.getLocale() != null) && item.getLocale().equals(locale))))
                {
                    if (!pretestConnections || (pretestConnections && isConnectionAlive(item)))
                    {
                        if (Trace.traceOn_) log(Trace.INFORMATION, "Using already connected connection");
                        
                        poolItem = item;
                        break;
                    }
                }
            }

            // If a service was requested and we could not find one, then get first available one so we can connect it to service.
            if (poolItem == null && service != null)
            {
                // must not have found a suitable connected system, use the first available
                for (int i=0; i<size; i++)
                {
                    PoolItem item = (PoolItem)connectionList_.elementAt(i);    
                    
                    // If in-use or not right type of AS400 object, skip it. 
                    if (item.isInUse()
                            || (secure && !(item.getAS400Object().isSecure()))
                            || (!secure && (item.getAS400Object().isSecure())))
                        continue;
                    
                    //@B2A Add a check for locales.  If the user did not specify a locale at
                    //creation time, item.getLocale() will be null.  If the user did 
                    // not pass in a locale on their getConnection(), locale will be null.
                    if (((item.getLocale() == null && locale == null) 
                                    || (locale != null && (item.getLocale() != null) && item.getLocale().equals(locale)))) 
                    {
                        if (!pretestConnections || (pretestConnections && isConnectionAlive(item)))
                        {
                            poolItem = item; 
                            break;   
                        }
                    }
                }  //end 'for' loop
            }
            
            if (poolItem != null)
            {
                synchronized(poolItem )
                {   
                    if (service != null && !poolItem.getAS400Object().isConnected(service)) 
                        poolItem.getAS400Object().connectService(service);
                    poolItem.setInUse(true);
                }
            }
        }//@B1A end outer synchronized block

        if (poolItem == null)
            poolItem = createNewConnection((service != null) ? service : 0, 
                                           (service != null), 
                                           secure, poolListeners, locale, poolAuth, socketProperties, ccsid, rootSystem);

        return poolItem;
    }

    /**
     *  Return the number of connections.
     *
     *  @return The number of connections.
     **/
    public int getConnectionCount()
    {
        return connectionList_.size();      
    }

    /**
     *  Indicates whether at least one of the pool items in the list is
     *  currently connected to any service; that is, whether at least one pool item
     *  is not in a disconnected state.
     *
     *  @return true if a connection exists
     **/
    boolean hasConnectedConnection()
    {
        synchronized (connectionList_)
        {
            int size = connectionList_.size();       
            for (int i=0; i<size; i++)
            {
                PoolItem item = (PoolItem)connectionList_.elementAt(i);  
                if (item.getAS400Object().isConnected()) 
                    return true; 
            }
        }
        
        return false;
    }

    // Returns false if the connection has previously (or currently) failed a connection validation pretest.  If the connection fails this test, it is marked as failed.
    private final boolean isConnectionAlive(PoolItem item)
    {
        if (item.isFailedPretest())
            return false;
        else if (item.getAS400Object().isConnectionAlive())
            return true;

        item.setFailedPretest();  // Mark it for removal from pool.
        return false;
    }

    /**
     * Log a message to the event log.
     *
     * @param   msg  The message to log.
     **/
    private final void log(String msg)
    {
        if (Trace.traceOn_) Trace.log(Trace.INFORMATION, msg);
        
        if (log_ != null)
            log_.log(msg);
    }

    /**
     * Log the message to the log.
     *   
     * @param   category  The trace category.
     * @param   msg  The message to log.
     **/ 
    private final void log(int category, String msg)
    {
        if (Trace.traceOn_ && Trace.isTraceOn(category))
        {
            Trace.log(category, msg);
            if (log_ != null)
                log_.log(msg);
        }
    }

    /**
     * Removes any connection that has exceeded the time limits or usage count limits.
     *
     * @param poolListeners The pool listeners to which events will be fired.
     * @exception AS400SecurityException If a security error occured.
     * @exception IOException If a communications error occured.
     **/
    void removeExpiredConnections(ConnectionPoolEventSupport poolListeners)  throws AS400SecurityException, IOException
    {    
        synchronized (connectionList_)
        {
            int size = connectionList_.size();  
            for (int i=size-1; i>=0; i--)
            {
                PoolItem p = (PoolItem)connectionList_.elementAt(i);    

                // Be conservative about removing in-use connections.
                if (p.isInUse())
                {
                    // Reclaim an in-use connection, only if its maxUseTime limit is exceeded.
                    if ((properties_.getMaxUseTime() >= 0) && (p.getInUseTime() >= properties_.getMaxUseTime()))
                    {
                        // Limit exceeded, so disconnect and remove the connection.
                        if (log_ != null || Trace.traceOn_) {
                            log(ResourceBundleLoader.getText(EXPIRED_MAX_USE_TIME, new String[] {systemName_, userID_} ));
                            log(Trace.WARNING, "Disconnecting pooled connection (currently in use) because it has exceeded the maximum use time limit of " + properties_.getMaxUseTime() + " milliseconds.");
                        }
                        
                        p.getAS400Object().resetAllServices();
                        connectionList_.removeElementAt(i);
                        if (poolListeners != null)
                        {
                            ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(p.getAS400Object(), ConnectionPoolEvent.CONNECTION_EXPIRED); //@A5C
                            poolListeners.fireConnectionExpiredEvent(poolEvent);  
                        }
                    }
                }
                else if (p.isFailedPretest())
                {
                    // Pool item has failed a connection validity pretest, so disconnect and remove the connection.
                    if (log_ != null || Trace.traceOn_)
                    {
                        log(ResourceBundleLoader.getText(EXPIRED_FAILED_PRETEST, new String[] {systemName_, userID_} ));
                        log(Trace.DIAGNOSTIC, "Disconnecting pooled connection (not currently in use) because it has failed a validation pretest.");
                    }
                    
                    p.getAS400Object().resetAllServices();
                    connectionList_.removeElementAt(i);
                    if (poolListeners != null)
                    {
                        ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(p.getAS400Object(), ConnectionPoolEvent.CONNECTION_EXPIRED);
                        poolListeners.fireConnectionExpiredEvent(poolEvent);  
                    }
                }
                else if ((properties_.getMaxInactivity() >= 0) && (p.getInactivityTime() >= properties_.getMaxInactivity()))
                {
                    // Connection has exceeded the maximum inactivity time, so disconnect and remove the connection.
                    if (log_ != null || Trace.traceOn_)
                    {
                        log(ResourceBundleLoader.getText(EXPIRED_INACTIVE, new String[] {systemName_, userID_} ));
                        log(Trace.DIAGNOSTIC, "Disconnecting pooled connection (not currently in use) because it has exceeded the maximum inactivity time limit of " + properties_.getMaxInactivity() + " milliseconds.");
                    }
                    
                    p.getAS400Object().resetAllServices();
                    connectionList_.removeElementAt(i);
                    if (poolListeners != null)
                    {
                        ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(p.getAS400Object(), ConnectionPoolEvent.CONNECTION_EXPIRED); //@A5C
                        poolListeners.fireConnectionExpiredEvent(poolEvent);  
                    }
                }
                else if ((properties_.getMaxUseCount() >= 0) && (p.getUseCount() >= properties_.getMaxUseCount()))
                {
                    // Connection has exceeded the maximum use count, so disconnect and remove the connection.
                    if (log_ != null || Trace.traceOn_)
                    {
                        log(ResourceBundleLoader.getText(EXPIRED_MAX_USE_COUNT, new String[] {systemName_, userID_} ));
                        log(Trace.DIAGNOSTIC, "Disconnecting pooled connection (not currently in use) because it has exceeded the maximum use count of " + properties_.getMaxUseCount());
                    }
                    
                    p.getAS400Object().resetAllServices();
                    connectionList_.removeElementAt(i);             
                    if (poolListeners != null)
                    {
                        ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(p.getAS400Object(), ConnectionPoolEvent.CONNECTION_EXPIRED); //@A5C //@B2C
                        poolListeners.fireConnectionExpiredEvent(poolEvent);  
                    }
                }
                else if ( (properties_.getMaxLifetime() >= 0) &&  (p.getLifeSpan() >= properties_.getMaxLifetime()))
                {
                    // Connection has exceeded the maximum lifetime, so disconnect and remove the connection.
                    if (log_ != null || Trace.traceOn_)
                    {
                        log(ResourceBundleLoader.getText(EXPIRED_MAX_LIFETIME, new String[] {systemName_, userID_} ));
                        log(Trace.DIAGNOSTIC, "Disconnecting pooled connection (not currently in use) because it has exceeded the maximum lifetime limit of " + properties_.getMaxLifetime() + " milliseconds.");
                    }
                    
                    p.getAS400Object().resetAllServices();
                    connectionList_.removeElementAt(i);           
                    if (poolListeners != null)
                    {
                        ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(p.getAS400Object(), ConnectionPoolEvent.CONNECTION_EXPIRED); //@A5C //@B2C
                        poolListeners.fireConnectionExpiredEvent(poolEvent);  
                    }
                }
            }//end 'for' loop
        }//@B1A end synchronized
    }

    /**
     * Removes the connection from the pool if it is due for removal.
     *
     * @param poolItem The pool item.
     * @param poolListeners The pool listeners to which events will be fired.
     * @return true if the pool item was found and removed from the pool; false otherwise.
     * @exception AS400SecurityException If a security error occured.
     * @exception IOException If a communications error occured.
     **/
    boolean removeIfExpired(PoolItem poolItem, ConnectionPoolEventSupport poolListeners)
    {
        if (connectionList_.isEmpty())
            return false;

        boolean connectionIsExpired = false;
        String expirationStatus = null;
        synchronized (connectionList_)
        {
            expirationStatus = checkConnectionExpiration(poolItem);
            if (expirationStatus == NOT_EXPIRED) {}  // do nothing
            else {
                connectionList_.removeElement(poolItem);
                connectionIsExpired = true;
            }
        }
        
        // Now that we're out of the sync block (and the connection has been removed from connectionList_), disconnect the connection.
        if (connectionIsExpired)
        {
            if ((log_ != null || Trace.traceOn_) && expirationStatus != null)
                log(ResourceBundleLoader.getText(expirationStatus, new String[] {systemName_, userID_} ));

            if (poolListeners != null)
            {
                ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(poolItem.getAS400Object(), ConnectionPoolEvent.CONNECTION_EXPIRED);
                poolListeners.fireConnectionExpiredEvent(poolEvent);  
            }
            
            if (Trace.traceOn_)
                log(Trace.DIAGNOSTIC, "Disconnecting pooled connection (not currently in use) because it has expired.");

            poolItem.getAS400Object().resetAllServices();
        }

        return connectionIsExpired;
    }

    /**
     *  New method to work with AS400ConnectionPool.removeFromPool().
     **/
    boolean removeUnusedElements()
    {
        synchronized (connectionList_)
        {
            if (connectionList_.size() > 0)
            {
                int size = connectionList_.size();

                if (size == 0)
                    return false;
        
                // incrementally search the list, looking for elements that are not in
                // use to remove

                for (int numToCheck = size - 1; numToCheck >= 0; numToCheck--)
                {
                    PoolItem item = (PoolItem)connectionList_.elementAt(numToCheck);
                    if (!item.isInUse())
                    {
                        if (Trace.traceOn_) log(Trace.DIAGNOSTIC, "Disconnecting pooled connection (not currently in use) because removeFromPool() was called.");
                      
                        item.getAS400Object().resetAllServices();
                        connectionList_.removeElementAt(numToCheck);   
                    }
                }
            }  
        }
      
        return true;
    }

    /**
     *  Remove the pool item in the list that contains this AS400 instance.
     *  The caller takes responsibility for subsequently calling resetAllServices().
     *  Called by AS400ConnectionPool.
     **/
    void removeElement(AS400 systemToFind)
    {
        synchronized(connectionList_)
        {
            int size = connectionList_.size();    
   
            for (int i=0; i<size; i++)
            {
                PoolItem item = (PoolItem)connectionList_.elementAt(i);
                if (item.getAS400Object().equals(systemToFind))
                {
                    connectionList_.removeElement(item);
                    return;
                }
            }  
        }                   
    }
    
    /**
     *  Sets the event log to log events. The default is to not log events.
     *
     *  @param log The event log object to use, or null if events should not be logged.
     *
     *  @see com.ibm.as400.access.Log 
     **/
    void setLog(Log log)
    {
        this.log_ = log;
    } 

    /**
     *  Removes old inactive connections from the list.
     **/
    void shutDownOldest()
    {
        if (log_ != null || Trace.traceOn_) log(ResourceBundleLoader.getText("CL_REMOLD", new String[] {systemName_, userID_} ));
        
        int oldest = 0;
        synchronized (connectionList_)
        {
            int reduce = ((getConnectionCount() - properties_.getMaxConnections() + 1));
            for (int j = 0; j < reduce; j++)
            {
                oldest = 0;
                if (connectionList_.size() > 0)
                {
                    long t = 0;
                    int size = connectionList_.size();            
                    for (int i=0; i<size; i++)
                    {
                        PoolItem item = (PoolItem)connectionList_.elementAt(i);
                        if (!item.isInUse())
                        {
                            if (item.getInactivityTime() > t || oldest == 0)
                            {
                                oldest = i;
                                t = item.getInactivityTime();
                            }
                        }
                    }

                    //only disconnect oldest item if it is not in use
                    PoolItem item = (PoolItem)connectionList_.elementAt(oldest);      
                    if (!item.isInUse())
                    {
                        if (Trace.traceOn_)
                            log(Trace.DIAGNOSTIC, "Disconnecting pooled connection (not currently in use) during removal of oldest unallocated connections.");

                        item.getAS400Object().resetAllServices();
                        connectionList_.removeElementAt(oldest);   
                        if (log_ != null || Trace.traceOn_)
                            log(ResourceBundleLoader.getText("CL_REMOLDCOMP", new String[] {systemName_, userID_} ));
                    }
                } 
            }
        }
    }
}
