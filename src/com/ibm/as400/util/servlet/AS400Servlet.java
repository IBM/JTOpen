///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400Servlet.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

import java.util.Properties;
import java.io.IOException;

import javax.servlet.http.*;
import javax.servlet.*;

import com.ibm.as400.access.Trace;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400ConnectionPool;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ConnectionPoolException;
import com.ibm.as400.access.ExtendedIllegalArgumentException;


/**
  *  The AS400Servlet class is an abstract class that represents an HTML Servlet.
  *
  *  A connection pool can be used to share connections and manage the number of 
  *  connections a servlet user can have to the AS/400. When using connection 
  *  pooling and a system is requested, a fully functional AS400 object is returned 
  *  to the calling application. It is then up to the application to return the AS400 
  *  object to the pool. It is not recommended that an application use this object to 
  *  create additional connections as the pool would not keep track of these connections. 
  *
  **/

public abstract class AS400Servlet extends AuthenticationServlet
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   private String head_ = "<html>\n<body>\n";
   private String end_ = "</body>\n</html>\n";

	private boolean useConnectionPool_ = false;

   // Handles loading the appropriate resource bundle
   private static ResourceBundleLoader_s loader_;       //$A1A

   static private AS400ConnectionPool connectionPool_ = null;
		
   
   
	/**
    *  Close the connection pool.
    **/
	public void destroy()
	{
		log(loader_.getText("PROP_DESC_SHUTDOWN"));
		if (useConnectionPool_)
		{
			log(loader_.getText("PROP_DESC_CLEANUP"));
			connectionPool_.close();
		}
		log(loader_.getText("PROP_DESC_SHUTDOWNCOMP"));
	}


   /**
    *  Constructs a default AS400Servlet object.
    **/
   public AS400Servlet()
   {
   }


   /**
    *  Constructs an AS400Servlet object specifing whether to use the connection pool.  
    *  The default is false.
    *
    *  @param useConnectionPool true if using connection pool; false otherwise.
    **/
   public AS400Servlet(boolean useConnectionPool)                    //$A4C
   {
      setUseConnectionPool(useConnectionPool);
   }


   /**
    *  Returns the connection pool.  The returned pool object
    *  allows the connection pool properties to be changed.
    *
    *  @return The connection pool.
    **/
   public AS400ConnectionPool getConnectionPool()
   {
      return connectionPool_;
   }

   
   /**
    *  Returns the tag containing the servlet ending.
    *  @return The tag.
    **/
   public String getDocumentEnd()
   {
      return end_;
   }


   /**
    *  Returns the tag containing the servlet head.
    *  @return The tag.
    **/
   public String getDocumentHead()
   {
      return head_;
   }
   
   
   /**
	  *  Returns an AS400 object.
     *
     *  @exception ConnectionPoolException If a connection pool error occurs. 
     *
     *  @return The AS400 system object.
	 **/
    public AS400 getSystem()
		throws ConnectionPoolException  
    {
		Thread currentThread = Thread.currentThread();
		String threadId = currentThread.getName();		
		Properties p = (Properties)getSessionData().get(threadId);
		String sysName = p.getProperty("realm");
		String uid = p.getProperty("uid");
		String pwd = p.getProperty("pwd");
		
		AS400 sys;
		if (connectionPool_ != null)
			sys = connectionPool_.getConnection(sysName, uid, pwd);
		else
			sys = new AS400(sysName, uid, pwd);
		
		return sys;    
    }
                                     

    /**
	  *  Returns an AS400 object. It uses the specified <i>system</i>.
     *  
     *  @param  systemName  The name of the AS/400.  
     *
     *  @exception ConnectionPoolException If a connection pool error occurs. 
     *
     *  @return The AS400 systen object.
     **/
    public AS400 getSystem(String systemName)
		throws ConnectionPoolException   
    {
		Thread currentThread = Thread.currentThread();
		String threadId = currentThread.getName();
		Properties p = (Properties)getSessionData().get(threadId);
		String uid = p.getProperty("uid");
		String pwd = p.getProperty("pwd");
		
		AS400 sys;
		if (connectionPool_ != null)
			sys = connectionPool_.getConnection(systemName, uid, pwd);
		else
			sys = new AS400(systemName, uid, pwd);
		
		return sys;    
    }
    
    
    /**
	  *  Returns an AS400 object. It connects to the specified <i>service</i>.
     *  
     *  @param  service  The name of the AS/400 service.  
     *
     *  @exception AS400SecurityException If a security or authority error occurs.
     *  @exception IOException If an error occurs while communicating with the AS/400.
     *  @exception ConnectionPoolException If a connection pool error occurs. 
     *
     *  @return The AS400 systen object.
     **/
    public AS400 getSystem(int service)
		throws AS400SecurityException, IOException, ConnectionPoolException
    {
		Thread currentThread = Thread.currentThread();
		String threadId = currentThread.getName();
		Properties p = (Properties)getSessionData().get(threadId);
		String sysName = p.getProperty("realm");
		String uid = p.getProperty("uid");
		String pwd = p.getProperty("pwd");
				
		AS400 sys;
		if (connectionPool_ != null)
			sys = connectionPool_.getConnection(sysName, uid, pwd, service);
		else
		{
			sys = new AS400(sysName, uid, pwd);
			sys.connectService(service);
		}
		
		return sys;
    }
    
    
    /**
	  *  Returns an AS400 object. It connects to the specified <i>system</i> and <i>service</i>.
     *  
     *  @param system   The name of the AS400.
     *  @param service  The name of the AS/400 service.
     *
     *  @exception AS400SecurityException If a security or authority error occurs.
     *  @exception IOException If an error occurs while communicating with the AS/400.
     *  @exception ConnectionPoolException If a connection pool error occurs. 
     *
     *  @return The AS400 systen object.
     **/
    public AS400 getSystem(String system, int service)
		throws AS400SecurityException, IOException, ConnectionPoolException   
    {
		Thread currentThread = Thread.currentThread();
		String threadId = currentThread.getName();
		Properties p = (Properties)getSessionData().get(threadId);
		String uid = p.getProperty("uid");
		String pwd = p.getProperty("pwd");
		
		AS400 sys;
		if (connectionPool_ != null)
			sys = connectionPool_.getConnection(system, uid, pwd, service);
		else
		{
			sys = new AS400(system, uid, pwd);
			sys.connectService(service);
		}
		
		return sys;
    }

    
    /**
	  *  Constructs an AS400 object. It uses the specified <i>system</i>, <i>user ID</i>, and <i>password</i>.  
	  *
     *  @param  systemName  The name of the AS/400.  
	  *  @param  userId  The user ID to use to connect to the system.  
	  *  @param  password  The password to use to connect to the system.  
     *
     *  @exception ConnectionPoolException If a connection pool error occurs. 
     *
     *  @return The AS400 systen object.
	 **/
    public AS400 getSystem(String systemName, String userId, String password)
		throws ConnectionPoolException                 
    {
		AS400 sys;
		if (connectionPool_ != null)
			sys = connectionPool_.getConnection(systemName, userId, password);
		else
			sys = new AS400(systemName, userId, password);

		return sys;    
    }
   
    
    /**
	  *  Constructs an AS400 object. It uses the specified <i>system</i>, <i>user ID</i>, <i>password</i>, and <i>service</i>.  
	  *
     *  @param  systemName  The name of the AS/400.  
	  *  @param  userId  The user ID to use to connect to the system.  
	  *  @param  password  The password to use to connect to the system.  
     *  @param  service  The name of the AS/400 service.
     *
     *  @exception AS400SecurityException If a security or authority error occurs.
     *  @exception IOException If an error occurs while communicating with the AS/400.
     *  @exception ConnectionPoolException If a connection pool error occurs. 
     *
     *  @return The AS400 systen object.
	 **/
    public AS400 getSystem(String systemName, String userId, String password, int service)
		throws AS400SecurityException, IOException, ConnectionPoolException
    {
		AS400 sys;
		if (connectionPool_ != null)
		{
         log(loader_.getText("PROP_DESC_USEPOOL"));                                        //$A1C
			sys = connectionPool_.getConnection(systemName, userId, password, service);
		}
		else
		{
			sys = new AS400(systemName, userId, password);
			sys.connectService(service);
		}
		
		return sys;
    }
   
   
   /**
    *  Indicates if the connection pool is being used.
    *  The default value is false.
    *  @return true if using connection pool; false otherwise.
   **/
	public boolean isUseConnectionPool()
	{
		return useConnectionPool_;
	}


   /**
    *  Set the html document end tags.
    *
    *  @param html The end tags.
    **/
	public void setDocumentEnd(String end)
	{
      if (end == null)
         throw new NullPointerException("end");

		end_ = end;
	}

   /**
    *  Sets the html document starting tags.
    *
    * @param html The starting tags.
    **/
   public void setDocumentHead(String head)
	{
      if (head == null)
         throw new NullPointerException("head");

		head_ = head;
	}


   /**
    *  Sets the AS400Servlet to use the connection pool.  
    *  The default is false.
    *
    *  @param useConnectionPool true if using connection pool; false otherwise.
    *
    *  @see com.ibm.as400.access.AS400ConnectionPool
    **/
	public void setUseConnectionPool(boolean useConnectionPool)
	{
		useConnectionPool_ = useConnectionPool;
		if (useConnectionPool_)
		{
			log(loader_.getText("PROP_DESC_USEPOOL"));               //$A1C

			if (connectionPool_ == null)
			{
				log(loader_.getText("PROP_DESC_CREATEPOOL"));         //$A1C
            connectionPool_ = new AS400ConnectionPool();          //$A2C
			}

         connectionPool_.setLog(getLog());                        //$A3C
		}
		else
		{
			log(loader_.getText("PROP_DESC_NOTUSEPOOL"));            //$A1C
			if (connectionPool_ != null)
			{
				log(loader_.getText("PROP_DESC_CLEANUPEXT"));         //$A1C
				connectionPool_.close();
				connectionPool_ = null;
			}
		}
	}


   /**
    *  Return the AS400 object to the pool when connection pooling is being used.
    *
    *  @param system The AS400 system object.
    **/
   public void returnSystem(AS400 system)                  // @A7C
   {
      if (connectionPool_ == null)
         log(loader_.getText("PROP_DESC_NOTUSEPOOL"));
      else
         connectionPool_.returnConnectionToPool(system);
   }


   /**
	 * Method used to validate authority.
	 *
	 * @param   realm  The realm to validate against.
	 * @param   uid  The user ID to use for validation.
	 * @param   pw  The password to use for validation.
    *
    * @return  always true.
    *
	 * @exception   SecurityException  This exception should be thrown if validation fails.
	 * @exception   IOException  This exception should be thrown if a communication error occurs during validation.
	 **/
   final public boolean validateAuthority(String realm, String uid, String pw)                                //$A5C
		throws SecurityException, IOException
	{
		try
		{
         log(loader_.substitute(loader_.getText("PROP_DESC_AUTHENTICATE"), new String[] {uid, realm} ));      //$A1C
         uid = uid.toUpperCase();
			realm = realm.toUpperCase();						
         
         AS400 sys = new AS400(realm, uid, pw);
			sys.validateSignon();
			
         return true;
		}
      catch (ExtendedIllegalArgumentException e)                       //$A8A
      {                                                                //$A8A
         log(loader_.getText("PROP_DESC_AUTHFAILED"));                 //$A8A
         if (Trace.isTraceOn())                                        //$A8A
            Trace.log(Trace.ERROR, e);                                 //$A8A
			throw new SecurityException(e.getMessage());                  //$A8A
      }                                                                //$A8A
      catch (AS400SecurityException e)
		{
			log(loader_.getText("PROP_DESC_AUTHFAILED"));           //$A1C  $A6C
         if (Trace.isTraceOn())                                        //$A6A
            Trace.log(Trace.ERROR, e);                                 //$A6A
			throw new SecurityException(e.getMessage());
		}
	}
}
