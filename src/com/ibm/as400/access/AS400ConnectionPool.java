///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400ConnectionPool.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Hashtable;
import java.util.Vector;         //Java 2
import java.util.Enumeration;
import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 *  The AS400ConnectionPool class manages a pool of AS400 objects.  A connection pool is used to 
 *  share connections and manage the number of connections a user can have to the AS/400.
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
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



   private transient Hashtable as400ConnectionPool_;
	// Hashtable of lists of connections that have been marked invalid by the user
	// by calling removeFromPool().
	private transient Hashtable removedAS400ConnectionPool_;  //@A6A
   private transient Log log_;

   // Handles loading the appropriate resource bundle
   private static ResourceBundleLoader loader_;

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
	       connList.removeAndReplace(poolListeners_);  
	    }
	    catch (Exception e)
	    {
	       log(e, key);
	    }
	  }
      } 
      ConnectionPoolEvent poolEvent = new ConnectionPoolEvent(this, ConnectionPoolEvent.MAINTENANCE_THREAD_RUN);
      poolListeners_.fireMaintenanceThreadRun(poolEvent);
   }		
	

   /**
    * Close and cleanup the connection pool.
    **/
   public void close()
   {
      log(loader_.getText("AS400CP_SHUTDOWN"));
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
      //if maintenance thread is running, stop it
      if (maintenance_ != null && maintenance_.isRunning())    
         maintenance_.setRunning(false);

      ConnectionPoolEvent event = new ConnectionPoolEvent(this, ConnectionPoolEvent.CONNECTION_POOL_CLOSED);
      poolListeners_.fireClosedEvent(event);  
      log(loader_.getText("AS400CP_SHUTDOWNCOMP"));
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
    * password, and service.  
    *
    * @param systemName The name of the system where the connections should exist.
    * @param userID The name of the user.
    * @param password The password of the user.
    * @param service The service to be connected. See the service number constants defined by AS400 class.
    * @param numberOfConnections The number of connections to be made.
    *
    * @exception ConnectionPoolException If a connection pool error occured.
    **/
   public void fill(String systemName, String userID, String password, int service, int numberOfConnections) 
      throws ConnectionPoolException
   {
      if (numberOfConnections < 1)
         throw new ExtendedIllegalArgumentException("numberOfConnections", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
      Vector newAS400Connections = new Vector();
      if (Trace.isTraceOn()) //@A5A
	Trace.log(Trace.INFORMATION, "fill() key before resolving= " + systemName + "/" + userID); //@A5A
      systemName = AS400.resolveSystem(systemName);  //@A5A
      userID = AS400.resolveUserId(userID);          //@A5A
      String key = createKey(systemName, userID);
      if (Trace.isTraceOn()) //@A5A
	Trace.log(Trace.INFORMATION, "fill() key after resolving= " + key); //@A5A
      try 
      {  
         ConnectionList connections = (ConnectionList)as400ConnectionPool_.get(key);
         log(loader_.substitute(loader_.getText("AS400CP_FILLING"), new String[] { (new Integer(numberOfConnections)).toString(), 
             systemName, userID} ));
         // create the specified number of connections
         AS400.addPasswordCacheEntry(systemName, userID, password);
         for (int i = 0; i < numberOfConnections; i++)
         {
            newAS400Connections.addElement(getConnection(systemName, userID, service, true, false));
         }
         connections = (ConnectionList)as400ConnectionPool_.get(key);
         for (int j = 0; j < numberOfConnections; j++) 
         {
            connections.findElement((AS400)newAS400Connections.elementAt(j)).setInUse(false);
         }   
      }
      catch (AS400SecurityException e)	   //@A2C
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
         log(loader_.getText("AS400CP_FILLEXC"));         
         throw new ConnectionPoolException(e);
      }
      catch (IOException ie)	                                                              //@A2A
      {                                                                                       //@A2A
         // If exception occurs, stop creating connections, run maintenance thread, and       //@A2A
         // throw whatever exception was received on creation to user.                        //@A2A
         ConnectionList connections = (ConnectionList)as400ConnectionPool_.get(key);          //@A2A
         for (int k = 0; k < newAS400Connections.size(); k++)                                 //@A2A
         {                                                                                    //@A2A
            connections.findElement((AS400)newAS400Connections.elementAt(k)).setInUse(false); //@A2A
         }                                                                                    //@A2A
         if (maintenance_ != null && maintenance_.isRunning())                                //@A2A
            cleanupConnections();                                                             //@A2A
         log(loader_.getText("AS400CP_FILLEXC"));                                             //@A2A
         throw new ConnectionPoolException(ie);                                               //@A2A
      }                                                                                       //@A2A7
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
       userID = AS400.resolveUserId(userID);          //@A5A
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
       return (connections.getActiveConnectionCount());
   }


  /**
   * Get the number of available connections to a system. 
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
      userID = AS400.resolveUserId(userID);          //@A5A
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
    *
    * @param   systemName  The name of the system where the object should exist.
    * @param   userID  The name of the user.
    * @param   password  The password of the user.
    * @param   service  The service to connect. See the service number constants defined by AS400 class.
    * @return     A connected AS400 object.
    * @exception ConnectionPoolException If a connection pool error occured.
    **/
   public AS400 getConnection(String systemName, String userID, String password, int service)
      throws ConnectionPoolException
   {
      try 
      {
         AS400.addPasswordCacheEntry(systemName, userID, password);
         AS400 releaseConnection = getConnection(systemName, userID, service, true, false);
         ConnectionPoolEvent event = new ConnectionPoolEvent(this, ConnectionPoolEvent.CONNECTION_RELEASED);
         poolListeners_.fireConnectionReleasedEvent(event); 
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
    * @exception ConnectionPoolException If a connection pool error occured.
   **/
   public AS400 getConnection(String systemName, String userID, int service)
      throws ConnectionPoolException
   {
      try 
      {
         AS400 releaseConnection = getConnection(systemName, userID, service, true, false);
         ConnectionPoolEvent event = new ConnectionPoolEvent(this, ConnectionPoolEvent.CONNECTION_RELEASED);
         poolListeners_.fireConnectionReleasedEvent(event); 
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
    * @param   password  The password of the user.
    * @return     An AS400 object.
    * @exception ConnectionPoolException If a connection pool error occured.
    **/	
   public AS400 getConnection(String systemName, String userID, String password)
      throws ConnectionPoolException
   {	
      try
      {
         AS400.addPasswordCacheEntry(systemName, userID, password);
         AS400 releaseConnection = getConnection(systemName, userID, 0, false, false);
         ConnectionPoolEvent event = new ConnectionPoolEvent(this, ConnectionPoolEvent.CONNECTION_RELEASED);
         poolListeners_.fireConnectionReleasedEvent(event); 
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
    * @exception ConnectionPoolException If a connection pool error occured.
    **/	
   public AS400 getConnection(String systemName, String userID)
      throws ConnectionPoolException
   {	
      try
      {
         AS400 releaseConnection = getConnection(systemName, userID, 0, false, false);
         releaseConnection.getVRM();
         ConnectionPoolEvent event = new ConnectionPoolEvent(this, ConnectionPoolEvent.CONNECTION_RELEASED);
         poolListeners_.fireConnectionReleasedEvent(event); 
         return releaseConnection; 
      }
      catch (AS400SecurityException ae)     
      {
         throw new ConnectionPoolException(ae);
      }
      catch (IOException ie)
      {
         throw new ConnectionPoolException(ie);
      }  
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
    * @return     An AS400 object.
    *
    * @exception   AS400SecurityException  If a security error occured.
    * @exception   IOException  If a communications error occured.
    * @exception ConnectionPoolException If a connection pool error occured.
    **/ 
   private AS400 getConnection(String systemName, String userID, int service, boolean connect, boolean secure)
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
            
      if (Trace.isTraceOn()) //@A5A
	Trace.log(Trace.INFORMATION, "getConnection() key before resolving= " + systemName + "/" + userID); //@A5A
      systemName = AS400.resolveSystem(systemName);  					//@A5A
      userID = AS400.resolveUserId(userID);          					//@A5A

      String key = createKey(systemName, userID);
      if (Trace.isTraceOn())                                                            //@A5A
	Trace.log(Trace.INFORMATION, "getConnection() key after resolving= " + key); //@A5A

      if (!isInUse()) 	            						 //@A3A     				
      {			       	                                      		 //@A3A
	setInUse(true);       // threadUsed property can now not be changed.     //@A3A
      }                                                                          //@A3A
										 
      //Work with maintenance thread
      if (isRunMaintenance())
      { 
         // Start thread if it has not been initialized.
         if (maintenance_ == null) 
         {  
            maintenance_ = new PoolMaintenance();
            maintenance_.start();
	    // Give thread a chance to start.
	    if (!maintenance_.isRunning())				         //@A1A
	    {
		try                                                              //@A1A
		{                                                                //@A1A
		    Thread.sleep(10);                                            //@A1A
		}                                                                //@A1A
		catch (InterruptedException e)				       	 //@A1A
		{ /*Should not happen*/ }                                                                //@A1A
	    }
	    // If thread has still not started, keep giving it chances for 5 minutes.
            for (int i = 1; !maintenance_.isRunning() && i<6000; i++)            //@A1C 
            {
		try 			   			    		 //@A1A
		{			   		             		 //@A1A
		    Thread.sleep(50);	                             		 //@A1A
		}			                            		 //@A1A
		catch (InterruptedException e)	                            		 
		{  /*Should not happen*/}                                                   		 //@A1A
	    }                                                        
	    if (!maintenance_.isRunning())                          		 //@A1A
	       Trace.log(Trace.WARNING, "maintenance thread failed to start");   //@A1A
         }
         // Restart the thread.
         else if (!maintenance_.isRunning()) 
            maintenance_.setRunning(true);     
      }     

      synchronized (as400ConnectionPool_)
      {
         ConnectionList connections = (ConnectionList)as400ConnectionPool_.get(key);
		
         if (connections == null)
	 {			
            // no connections found, better start a new list
 	    if (log_ != null) 
               log(loader_.substitute(loader_.getText("AS400CP_CONNLIST"), new String[] {systemName, userID} ));			
	    connections = new ConnectionList(systemName, userID, getProperties());
            // log_ can be null, meaning events should not be logged
	    connections.setLog(log_);
			
	    // create a new connection
            PoolItem sys = connections.createNewConnection(service, connect, secure, poolListeners_); 
			
	    as400ConnectionPool_.put(key, connections);
			 
	    return sys.getAS400Object(); 
  	 }
   	 else
	 {   
            if (connect) 
               return connections.getConnection(service, secure, poolListeners_).getAS400Object();  
            else
               return connections.getConnection(secure, poolListeners_).getAS400Object();     
         }
      }
   }


   /**
    * Get an secure connected AS400 object from the connection pool.  If an appropriate one is not found, 
    * one is created.  If the maximum connection limit has been reached, an exception
    * will be thrown.  The AS400 object may not be connected to any services.
    *
    * @param   systemName  The name of the system where the object should exist.
    * @param   userID  The name of the user.
    * @param   password  The password of the user.
    * @return     A secure connected AS400 object.
    * @exception ConnectionPoolException If a connection pool error occured.
   **/	
   public AS400 getSecureConnection(String systemName, String userID, String password)
      throws ConnectionPoolException
   {	
      try
      {
         AS400.addPasswordCacheEntry(systemName, userID, password);
         AS400 releaseConnection = getConnection(systemName, userID, 0, false, true);
         releaseConnection.getVRM();
         ConnectionPoolEvent event = new ConnectionPoolEvent(this, ConnectionPoolEvent.CONNECTION_RELEASED);
         poolListeners_.fireConnectionReleasedEvent(event); 
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
    * @exception ConnectionPoolException If a connection pool error occured.
   **/	
   public AS400 getSecureConnection(String systemName, String userID)
      throws ConnectionPoolException
   {	
      try
      {
         AS400 releaseConnection = getConnection(systemName, userID, 0, false, true);
         releaseConnection.getVRM();
         ConnectionPoolEvent event = new ConnectionPoolEvent(this, ConnectionPoolEvent.CONNECTION_RELEASED);
         poolListeners_.fireConnectionReleasedEvent(event); 
         return releaseConnection; 
      }
      catch (AS400SecurityException ae)     
      {
         throw new ConnectionPoolException(ae);
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
    * @param   password  The password of the user.
    * @param   service  The service to connect. See the service number constants defined by AS400 class.
    * @return     A connected AS400 object.
    * @exception ConnectionPoolException If a connection pool error occured.
   **/
   public AS400 getSecureConnection(String systemName, String userID, String password, int service)
      throws ConnectionPoolException
   {
      try 
      {
         AS400.addPasswordCacheEntry(systemName, userID, password);
         AS400 releaseConnection = getConnection(systemName, userID, service, true, true);
         ConnectionPoolEvent event = new ConnectionPoolEvent(this, ConnectionPoolEvent.CONNECTION_RELEASED);
         poolListeners_.fireConnectionReleasedEvent(event); 
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
    * @exception ConnectionPoolException If a connection pool error occured.
    **/
   public AS400 getSecureConnection(String systemName, String userID, int service)
     throws ConnectionPoolException
   {
      try 
      {
         AS400 releaseConnection = getConnection(systemName, userID, service, true, true);
         ConnectionPoolEvent event = new ConnectionPoolEvent(this, ConnectionPoolEvent.CONNECTION_RELEASED);
         poolListeners_.fireConnectionReleasedEvent(event); 
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
    *  Initializes the transient data.
    **/
   private void initializeTransient()
   {
      // log_ was originally not transient, however the EventLog class (one of the possible
      // implementations of the Log interface) uses a java.io.PrintWriter object which is 
      // not serializable.  Therefore, log_ was changed to be transient and the user
      // will need to reset log_ after a serialization of the pool.
      as400ConnectionPool_ = new Hashtable();
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
	 * particular system/userID/password would no longer be valid.  For example, if you change the password for a 
	 * userId, you will want to call this method to remove the connections for the old userID/password 
	 * combination.  This will not invalidate connections in use; rather it will mean that the next time
	 * a connection is requested for that userID, a new connection 
	 * will be established to the server.  After you call this method, you may want 
	 * to fill the pool with connections with your new password to avoid connection time. 
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
		userID = AS400.resolveUserId(userID);		   
		String key = createKey(systemName, userID);
		ConnectionList listToBeRemoved = (ConnectionList)as400ConnectionPool_.get(key);
		if (listToBeRemoved != null)
		{
			listToBeRemoved.removeUnusedElements();
			removedAS400ConnectionPool_.put(key, listToBeRemoved);
			as400ConnectionPool_.remove(key);
		}
		else
			Trace.log(Trace.WARNING, "A list of connections for: " + key + "does not exist");
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
		if (system == null)
			throw new NullPointerException("system");
		if (Trace.isTraceOn()) //@A5A
			Trace.log(Trace.INFORMATION, "returnConnectionToPool key= " + system.getSystemName() + "/" + system.getUserId()); //@A5A
		String key = createKey(system.getSystemName(), system.getUserId());
		ConnectionList connections = (ConnectionList)as400ConnectionPool_.get(key);
		PoolItem poolItem = null;

		// First look for the list for the systemName/userId key for the AS400 object.
		// If such a list exists, search that list for a reference to the AS400 object returned.
		if (connections != null)
			poolItem = connections.findElement(system);

		// If such an item is found, set it not in use and send an event that the connection
		// was returned to the pool.
		if (poolItem != null)
		{
			poolItem.setInUse(false);
			if (log_ != null)
				log(loader_.substitute(loader_.getText("AS400CP_RETCONN"), new String[] {system.getSystemName(), system.getUserId()} ));
			ConnectionPoolEvent event = new ConnectionPoolEvent(this, ConnectionPoolEvent.CONNECTION_RETURNED);
			poolListeners_.fireConnectionReturnedEvent(event);
			Trace.log(Trace.INFORMATION, "returned connection to pool"); 
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
					Trace.log(Trace.WARNING, "connection belongs to a different list than expected");
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
				poolItem = removedConnections.findElement(system);

			// If the object is found, disconnect it and remove the element from removed pool.
			if (poolItem != null)
			{
			        poolItem.getAS400Object().disconnectAllServices();
			        removedConnections.removeElement(system); 
				if (log_ != null)
				{
					log(loader_.substitute(loader_.getText("AS400CP_RETCONN"), new String[] {system.getSystemName(), system.getUserId()} ));
					ConnectionPoolEvent event = new ConnectionPoolEvent(this, ConnectionPoolEvent.CONNECTION_RETURNED);
					poolListeners_.fireConnectionReturnedEvent(event);
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
						Trace.log(Trace.INFORMATION, "returned connection to removed pool");
						break;
					}
				} 
			}
			//@A6A New code ends

			// If the object was not found in either pool, it does not belong to the pool
			// and trace a warning message.
			if (poolItem == null)
				Trace.log(Trace.WARNING, "connection does not belong to this pool");
		}

		//If running single-threaded and cleanup interval has elapsed, run cleanup
		if (!isThreadUsed() && isRunMaintenance() && 
			System.currentTimeMillis() - maintenance_.getLastTime() > getCleanupInterval())
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
		if (maintenance_ != null && maintenance_.isRunning())
		{  
			synchronized(maintenance_)
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
	
}
