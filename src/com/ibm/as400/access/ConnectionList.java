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

package com.ibm.as400.access;

import java.util.Vector;    // Java 2
import java.io.IOException;
import java.util.Locale;      //@B2A

/** 
  *  ConnectionList is a list of AS400 connections specific to an AS400 and userID.  The 
  *  connection list is used to create new connections and get connections from the pool.
  *  The connection list can remove connections that have exceeded inactivity time and 
  *  replace connections that have exceeded the maximum use count or maximum lifetime.
 **/
final class ConnectionList 
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

  private String systemName_;
  private String userID_;
  private ConnectionPoolProperties properties_;
  private Log log_;
  private Vector connectionList_ = new Vector(); 

  // Handles loading the appropriate resource bundle
//@CRS  private static ResourceBundleLoader loader_;

  /**
   *  Construct a ConnectionList object.  
   *  @param systemName The system where the ConnectionList will exist.
   *  @param userID The name of the user.
   *  @param properties The properties of the ConnectionList.
   **/
  ConnectionList(String systemName, String userID, ConnectionPoolProperties properties)  
  {
    if (systemName == null)
      throw new NullPointerException("systemName");
    if (systemName.length() == 0)
      throw new ExtendedIllegalArgumentException("systemName", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    if (userID == null)
      throw new NullPointerException("userID");
    if (userID.length() == 0)
      throw new ExtendedIllegalArgumentException("userID", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    if (properties == null)
      throw new NullPointerException("properties");

    this.systemName_ = systemName;
    this.userID_ = userID;
    this.properties_ = properties;
  }


  /**
   *  Close the connection.
   **/
  void close()
  {
    if (log_ != null)
      log(ResourceBundleLoader.getText("CL_CLEANUP", new String[] {systemName_, userID_} ));
    synchronized (connectionList_) 
    {
      int size = connectionList_.size();  //@A5M
      for (int i=0; i<size; i++)
      {
        PoolItem p = (PoolItem)connectionList_.elementAt(i); 
        p.getAS400Object().disconnectAllServices();
      }
      connectionList_.removeAllElements();   
    }
    log(ResourceBundleLoader.getText("CL_CLEANUPCOMP"));
  }


  //@A5C  Changed method to private.
  /**
   *  Return a connection, which can connect to an AS400 service.
   *
   *  @param service The service to connect.
   *  @param connect If true connect the specified service.
   *  @param secure  If true a secure AS400 object was requested.
   *  @param poolListeners The pool listeners to which events will be fired. 
   *  @return The PoolItem that was connected.
   *  @exception AS400SecurityException If a security error occured.
   *  @exception IOException If a communications error occured.
   *  @exception ConnectionPoolException If max connection limit is reached.
   **/
  private PoolItem createNewConnection(int service, boolean connect, boolean secure, 
                                       ConnectionPoolEventSupport poolListeners, Locale locale, String password) //@B2C  //@B4C
  throws AS400SecurityException, IOException, ConnectionPoolException  //@A1C
  {     
    if (log_ != null)
      log(ResourceBundleLoader.getText("CL_CREATING", new String[] {systemName_, userID_} ));

    if ((properties_.getMaxConnections() > 0) && 
        (getConnectionCount() >= properties_.getMaxConnections()))
    {
      log(ResourceBundleLoader.getText("CL_CLEANUPEXP"));
      // see if anything frees up
      removeAndReplace(poolListeners);  

      // if that didn't do the trick, try shutting down unused connections
      if (getConnectionCount() >= properties_.getMaxConnections())
      {
        log(ResourceBundleLoader.getText("CL_CLEANUPOLD"));
        shutDownOldest(); 
        // if not enough connections were freed, throw an exception!
        if (getConnectionCount() >= properties_.getMaxConnections())
        {
          throw new ConnectionPoolException(ConnectionPoolException.MAX_CONNECTIONS_REACHED); //@A1C
        }
      }
    }

    boolean threadUse = properties_.isThreadUsed();
    // create a new connection
    PoolItem sys = new PoolItem (systemName_, userID_, password, secure, locale, service, connect, threadUse);    //@B2C //@B4C
    //@B4D if (connect)
    //@B4D {
    //@B4D 	sys.getAS400Object().connectService(service);
    //@B4D }

    //@B4D if (!properties_.isThreadUsed())						//@A2A
    //@B4D {									//@A2A
    //@B4D 	try
    //@B4D 	{							  //@A2A						   //@A2A		
    //@B4D 		sys.getAS400Object().setThreadUsed(false);	//@A2A
    //@B4D 	}							//@A2A
    //@B4D 	catch (java.beans.PropertyVetoException e)		//@A2A
    //@B4D 	{							//@A2A
    //@B4D 		//Ignore                                        //@A2A
    //@B4D 	}							//@A2A
    //@B4D }									//@A2A

    // set the item is in use since we are going to return it to caller
    sys.setInUse(true);
    connectionList_.addElement(sys);  

    if (poolListeners != null)
    {
      ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(sys.getAS400Object(), ConnectionPoolEvent.CONNECTION_CREATED); //@A5C
      poolListeners.fireConnectionCreatedEvent(poolEvent);  
    }
    if (log_ != null)
    {
      log(ResourceBundleLoader.getText("CL_CREATED", new String[] {systemName_, userID_} ));
    }
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
   *  Return the number of active connections which are in use.
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
        {
          count++;
        }
      }
    }
    return count;
  }

  /**
   *  Return the number of available connections which are not in use.
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
        {
          count++;
        }
      }
    }
    return count;
  }


  /**
   *  Get a connection from the pool.
   *
   *  @param secure  If true a secure AS400 object was requested.
   *  @param poolListeners The pool listeners to which events will be fired.
   *  @param locale The locale of the AS400 object.
   *  @exception AS400SecurityException If a security error occured.
   *  @exception IOException If a communications error occured.
   *  @exception ConnectionPoolException If a connection pool error occured.
   *  @return The pool item.
   **/
  PoolItem getConnection(boolean secure, ConnectionPoolEventSupport poolListeners, Locale locale, String password)    //@B2C //@B4C
  throws AS400SecurityException, IOException, ConnectionPoolException
  {
    PoolItem poolItem = null;
    synchronized(connectionList_)  //@A5A
    {
      int size = connectionList_.size();
      for (int i=0; i<size; i++)
      {
        PoolItem item = (PoolItem)connectionList_.elementAt(i);    
        // check to see if that connection is in use
        if (!item.isInUse())
        {
          //@B2A Add a check for locales.  If the user did not specify a locale at
          //creation time, item.getLocale() will be "".  If the user did 
          // not pass in a locale on their getConnection(), locale will be null.
          if (secure && item.getAS400Object() instanceof SecureAS400
              && ((item.getLocale().equals("") && locale == null)        //@B2A
                  || (locale != null && item.getLocale().equals(locale.toString()))))   //@B2A
          {
            // return item found
            poolItem = item; 
            break;   
          }
          else if (!secure && !(item.getAS400Object() instanceof SecureAS400)
                   && ((item.getLocale().equals("") && locale == null)   //@B2A
                       || (locale != null && item.getLocale().equals(locale.toString()))))   //@B2A
          {
            // return item found
            poolItem = item; 
            break;
          }
        }
      }
      if (poolItem != null)  //@A5A
      {
        poolItem.setInUse(true);  //@A5M
      }
    } //end synchronized

    if (poolItem == null)
    {
      // didn't find a suitable connection, create a new one
      poolItem = createNewConnection (0, false, secure, poolListeners, locale, password); //@B2C //@B4C
    }

    return poolItem;
  }


  /**
   *  Gets a connection to an AS400 service from the pool.
   *
   *  @param service The AS400 service.
   *  @param secure  If true a secure AS400 object was requested.
   *  @param poolListeners The pool listeners to which events will be fired.
   *  @exception AS400SecurityException If a security error occured.
   *  @exception IOException If a communications error occured.
   *  @exception ConnectionPoolException If a connection pool error occured.
   *  @return The pool item.
   **/
  PoolItem getConnection(int service, boolean secure, ConnectionPoolEventSupport poolListeners, Locale locale, String password)  //@B2C //@B4C 
  throws AS400SecurityException, IOException, ConnectionPoolException
  {
    PoolItem poolItem = null;
    synchronized (connectionList_)  //@B1A
    {
      int size = connectionList_.size();       
      for (int i=0; i<size; i++)
      {
        PoolItem item = (PoolItem)connectionList_.elementAt(i);  
        // check to see if that connection is in use
        if (!item.isInUse())
        {
          if (secure && item.getAS400Object() instanceof SecureAS400 &&
              item.getAS400Object().isConnected(service) 
              && ((item.getLocale().equals("") && locale == null)         //@B2A
                  || (locale != null && item.getLocale().equals(locale.toString())))) //@B2A
          {
            Trace.log(Trace.INFORMATION, "Using already connected connection");
            poolItem = item;
            break;
          }
          else if (!secure && !(item.getAS400Object() instanceof SecureAS400) &&
                   item.getAS400Object().isConnected(service)
                   && ((item.getLocale().equals("") && locale == null)     //@B2A
                       || (locale != null && item.getLocale().equals(locale.toString()))))     //@B2A
          {
            Trace.log(Trace.INFORMATION, "Using already connected connection");
            poolItem = item;
            break;
          }
        }
      }

      if (poolItem == null)
      {
        // must not have found a suitable connected system, use the first available
        //@B1D size = connectionList_.size();     
        for (int i=0; i<size; i++)
        {
          PoolItem item = (PoolItem)connectionList_.elementAt(i);   
          // check to see if that connection is in use
          if (!item.isInUse())
          {
            if (secure && item.getAS400Object() instanceof SecureAS400
                && ((item.getLocale().equals("") && locale == null)  //@B2A
                    || (locale != null && item.getLocale().equals(locale.toString()))))      //@B2A
            {
              Trace.log(Trace.INFORMATION, "Must not have found a suitable connection, using first available");
              poolItem = item;                  
              break;
            }
            else if (!secure && !(item.getAS400Object() instanceof SecureAS400)
                     && ((item.getLocale().equals("") && locale == null)     //@B2A
                         || (locale != null && item.getLocale().equals(locale.toString()))))     //@B2A
            {
              Trace.log(Trace.INFORMATION, "Must not have found a suitable connection, using first available");
              poolItem = item;                  
              break;
            }
          }
        }
      }
      if (poolItem != null)  //B1A
      {
        if (!poolItem.getAS400Object().isConnected(service)) poolItem.getAS400Object().connectService(service); //@CRS
        poolItem.setInUse(true); //@B1M
      }
    }//@B1A end synchronized block

    if (poolItem == null)
    {
      poolItem = createNewConnection(service, true, secure, poolListeners, locale, password);    //@B2C  //@B4C
    }

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
   * Log a message to the event log.
   *
   * @param   msg  The message to log.
   **/
  private void log(String msg)
  {
    if (Trace.isTraceOn())
      Trace.log(Trace.INFORMATION, msg);
    if (log_ != null)
      log_.log(msg);
  }


  /**
   * Log an exception and message to the event log.
   *
   * @param   exception  The exception to log.
   * @param   msg  The message to log.
   **/
  private void log(Exception exception, String msg)
  {
    if (Trace.isTraceOn())
      Trace.log(Trace.ERROR, msg, exception);
    if (log_ != null)
      log_.log(msg, exception);
  }


  /**
   *  Reconnect all services to the AS/400.
   *
   *  @param oldItem The old connection.
   *  @param newItem The new connection.
   *  @exception IOException If a communications error occured.
   *  @exception AS400SecurityException If a security error occured.
   **/
  private void reconnectAllServices(PoolItem oldItem, PoolItem newItem)
  throws IOException, AS400SecurityException
  {
    if (oldItem == null)
      throw new NullPointerException("oldItem");
    if (newItem == null)
      throw new NullPointerException("newItem");
    AS400 oldAS400Item = oldItem.getAS400Object();
    AS400 newAS400Item = newItem.getAS400Object();
    if (oldAS400Item.isConnected(AS400.FILE))
    {
      Trace.log(Trace.INFORMATION, "replacing connection to service FILE");
      newAS400Item.connectService(AS400.FILE);
    }
    if (oldAS400Item.isConnected(AS400.PRINT))
    {
      Trace.log(Trace.INFORMATION, "replacing connection to service PRINT");         
      newAS400Item.connectService(AS400.PRINT);
    }
    if (oldAS400Item.isConnected(AS400.COMMAND))
    {
      Trace.log(Trace.INFORMATION, "replacing connection to service COMMAND");
      newAS400Item.connectService(AS400.COMMAND);
    }
    if (oldAS400Item.isConnected(AS400.DATAQUEUE))
    {
      Trace.log(Trace.INFORMATION, "replacing connection to service DATAQUEUE");
      newAS400Item.connectService(AS400.DATAQUEUE);        
    }
    if (oldAS400Item.isConnected(AS400.DATABASE))
    {
      Trace.log(Trace.INFORMATION, "replacing connection to service DATABASE");
      newAS400Item.connectService(AS400.DATABASE);        
    }
    if (oldAS400Item.isConnected(AS400.RECORDACCESS))
    {
      Trace.log(Trace.INFORMATION, "replacing connection to service RECORDACCESS");
      newAS400Item.connectService(AS400.RECORDACCESS); 
    }
    if (oldAS400Item.isConnected(AS400.CENTRAL))
    {
      Trace.log(Trace.INFORMATION, "replacing connection to service CENTRAL");
      newAS400Item.connectService(AS400.CENTRAL);
    }
  }


  /**
   * Removes any connection that has exceeded inactivity time and also replace any connection 
   *  that exceeded the maximum use count or maximum lifetime with a new connection.
   *
   * @param poolListeners The pool listeners to which events will be fired.
   * @exception AS400SecurityException If a security error occured.
   * @exception IOException If a communications error occured.
   **/
  void removeAndReplace(ConnectionPoolEventSupport poolListeners)   //@B1D synchronized
  throws AS400SecurityException, IOException
  {    
    synchronized (connectionList_)  //@B1A
    {
      int size = connectionList_.size();  
      for (int i=size-1; i>=0; i--)
      {
        PoolItem p = (PoolItem)connectionList_.elementAt(i);    

        if ((properties_.getMaxInactivity() >= 0) && 
            (p.getInactivityTime() >= properties_.getMaxInactivity()))
        {
          // remove any item that has exceeded inactivity time
          if (log_ != null)
            log(ResourceBundleLoader.getText("CL_REMUNUSED", new String[] {systemName_, userID_} ));
          p.getAS400Object().disconnectAllServices();
          connectionList_.removeElementAt(i);
          if (poolListeners != null)
          {
            ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(p.getAS400Object(), ConnectionPoolEvent.CONNECTION_EXPIRED); //@A5C
            poolListeners.fireConnectionExpiredEvent(poolEvent);  
          }
        }
        else
        {
          if ((properties_.getMaxUseCount() >= 0) &&
              (p.getUseCount() >= properties_.getMaxUseCount()))
          {
            //@B4C remove any item that exceeded maximum use count	
            if (log_ != null)
              log(ResourceBundleLoader.getText("CL_REPUSE", new String[] {systemName_, userID_} ));
            p.getAS400Object().disconnectAllServices();
            //@B4D PoolItem newItem = new PoolItem(systemName_, userID_, (p.getAS400Object() instanceof SecureAS400), p.getAS400Object().getLocale());	  //@B2C
            //@B4D reconnectAllServices(p, newItem);
            connectionList_.removeElementAt(i);             
            //@B4D connectionList_.insertElementAt(newItem, i);
            //@B4D AS400 sys = p.getAS400Object();	//@A5A
            //@B4D p = newItem;
            if (poolListeners != null)
            {
              ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(p.getAS400Object(), ConnectionPoolEvent.CONNECTION_EXPIRED); //@A5C //@B2C
              poolListeners.fireConnectionExpiredEvent(poolEvent);  
            }
          }
          if ((properties_.getMaxLifetime() >= 0) &&
              (p.getLifeSpan() >= properties_.getMaxLifetime()))
          {
            //@B4C remove any item that has lived past expected lifetime
            if (log_ != null)
              log(ResourceBundleLoader.getText("CL_REPLIFE", new String[] {systemName_, userID_} ));
            p.getAS400Object().disconnectAllServices();
            //@B4D PoolItem newItem = new PoolItem(systemName_, userID_, (p.getAS400Object() instanceof SecureAS400), p.getAS400Object().getLocale());	  //@B2C
            //@B4D reconnectAllServices(p, newItem);
            connectionList_.removeElementAt(i);           
            //@B4D connectionList_.insertElementAt(newItem, i);             
            //@B4D AS400 sys = p.getAS400Object();	//@A5A
            //@B4D p = newItem;
            if (poolListeners != null)
            {
              ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(p.getAS400Object(), ConnectionPoolEvent.CONNECTION_EXPIRED); //@A5C //@B2C
              poolListeners.fireConnectionExpiredEvent(poolEvent);  
            }
          }
          if ((properties_.getMaxUseTime() >= 0) &&
              (p.getInUseTime() >= properties_.getMaxUseTime()))
          {
            // maximum usage time exceeded.  try and disconnect, then remove from list
            p.getAS400Object().disconnectAllServices();
            connectionList_.removeElementAt(i);
            if (poolListeners != null)
            {
              ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(p.getAS400Object(), ConnectionPoolEvent.CONNECTION_EXPIRED); //@A5C
              poolListeners.fireConnectionExpiredEvent(poolEvent);  
            }
          }
        }//end else
      }//end for
    }//@B1A end synchronized
  }


  //@A4A
  /**
  *  New method to work with remove() in AS400ConnectionPool.
  **/
  boolean removeUnusedElements()
  {
    synchronized (connectionList_)
    {
      if (connectionList_.size() > 0)
      {
        int size = connectionList_.size();
        //if there are no more elements remaining in the list, remove and 
        //return false.
        if (size == 0)
          return false;
        //incrementally search the list, looking for elements that are not in 
        //use to remove

        for (int numToCheck = size - 1; numToCheck >= 0; numToCheck--)
        {
          PoolItem item = (PoolItem)connectionList_.elementAt(numToCheck);
          if (!item.isInUse())
          {
            item.getAS400Object().disconnectAllServices();
            connectionList_.removeElementAt(numToCheck);   
          }
        }
      }//end if (connectionList_.size() > 0)	 
    }//end synchronized
    return true;
  }

  /**
   *  Remove the pool item in the list that contains this AS400 instance.
   **/
  void removeElement(AS400 systemToFind)
  {
    synchronized(connectionList_)   //@A3A
    {
      int size = connectionList_.size();        
      for (int i=0; i<size; i++)
      {
        PoolItem item = (PoolItem)connectionList_.elementAt(i);
        if (item.getAS400Object().equals(systemToFind))   //@A3C //@A4C
        {
          connectionList_.removeElement(item);
          return;     //@A3A
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
    if (log_ != null)
      log(ResourceBundleLoader.getText("CL_REMOLD", new String[] {systemName_, userID_} ));
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
            item.getAS400Object().disconnectAllServices();
            connectionList_.removeElementAt(oldest);   
            if (log_ != null)
              log(ResourceBundleLoader.getText("CL_REMOLDCOMP", new String[] {systemName_, userID_} ));
          }
        }//end if (connectionList_.size() > 0)	
      }//end fill  
    }//end synchronized
  }//end shutDownOldest()
}
