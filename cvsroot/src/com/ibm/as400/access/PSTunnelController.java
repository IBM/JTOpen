///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PSTunnelController.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;



/**
The PSTunnelController class represents a connection
to a client of the proxy server.
**/
class PSTunnelController
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


	// Private data.
	private static long                         nextConnectionId_       = 1000;
	private static Object                       nextConnectionIdLock_   = new Object ();

	private long                                connectionId_           = -1;
	private PxDSFactory                         factory_;
	private static Hashtable                    clientIds_;
	private static long                         nextClientId_           = 1;
	private static Object                       nextClientIdLock_       = new Object ();
	private PxTable                             proxyTable_;
	private static Hashtable                    useTimes_;		 //@A1A

	private transient long clientCleanupInterval_ = 7200000;	 //@A1A 2 hours
	private transient long clientLifetime_ = 1800000;			 //@A1A 30 minutes

	transient TunnelProxyServerMaintenance maintenance_;

	/**
	Constructs a PSController object.

	@param  proxyServer  The proxy server.
	**/
	public PSTunnelController (ProxyServer proxyServer)
	{
		clientIds_ = new Hashtable();
		useTimes_ = new Hashtable();  //@A1A

		proxyTable_ = new PxTable ();

		factory_ = new PxDSFactory ();
		factory_.register (new PxByteParm ());
		factory_.register (new PxShortParm ());
		factory_.register (new PxBooleanParm ());
		factory_.register (new PxIntParm ());
		factory_.register (new PxStringParm ());
		factory_.register (new PxSerializedObjectParm (null));
		factory_.register (new PxTunnelConnectReqSV (this, clientIds_));
		factory_.register (new PxLoadReqSV (new PSLoad()));
		factory_.register (new PxLongParm ());
		factory_.register (new PxFloatParm ());
		factory_.register (new PxDoubleParm ());
		factory_.register (new PxCharParm ());
		factory_.register (new PxToolboxObjectParm ());
		factory_.register (new PxNullParm ());
		factory_.register (new PxClassParm ());
		factory_.register (new PxPxObjectParm (proxyTable_));

		factory_.register (new PxConstructorReqSV (proxyTable_));
		factory_.register (new PxMethodReqSV (proxyTable_));
		factory_.register (new PxFinalizeReqSV (proxyTable_));

		initializeTransient();
		if (Trace.isTraceProxyOn ())
			Trace.log (Trace.PROXY, "Px server controller " + this + " opened.");
	}

	//@A1A
	/**
	* Remove any connections that have exceeded maximum lifetime.
	**/
	void cleanupConnections()
	{
		synchronized (useTimes_)
		{
			Enumeration keys = useTimes_.keys();
			while (keys.hasMoreElements())
			{
				Long key = (Long)keys.nextElement();
				long timeLastRun = ((Long)useTimes_.get(key)).longValue();
				if ((System.currentTimeMillis()-timeLastRun) > 
					clientLifetime_)
				{
					//Remove from table of clientId/PSTunnelConnection object
					synchronized (clientIds_)
					{
						clientIds_.remove(key);
					}
					//Remove from table of last use times
					useTimes_.remove(key);
					//Remove all objects associated with clientId from table
					proxyTable_.removeClientId(key.longValue());
				}
			}
		} 
	}


	/**
	Returns a new clientId.

	@return A new clientId that hasn't been used.
	**/
	public static long getNextClientId ()
	{
		synchronized (nextClientIdLock_)
		{
			nextClientId_ = ++nextClientId_;
			return nextClientId_;
		}
	}

	/**
	Returns the current requesting client's unique connection id.

	@return The current requesting client's unique connection id.
	**/
	public long getConnectionId ()
	{
		return connectionId_;
	}

	/**
	Initialize transient data.
	**/
	private void initializeTransient ()
	{
		try
		{
			String clientCleanupInterval = SystemProperties.getProperty(SystemProperties.TUNNELPROXYSERVER_CLIENTCLEANUPINTERVAL);
			if (clientCleanupInterval != null)
			{
				clientCleanupInterval_ = Long.valueOf(clientCleanupInterval).longValue() * 1000;
			}
			String clientLifetime = SystemProperties.getProperty(SystemProperties.TUNNELPROXYSERVER_CLIENTLIFETIME);
			if (clientLifetime != null)
			{
				clientLifetime_ = Long.valueOf(clientLifetime).longValue() * 1000;
			}
		}
		catch (NumberFormatException ne)
		{
		}
		if (clientCleanupInterval_ > 0)	  // do not do cleanup if <= 0
		{
			maintenance_ = new TunnelProxyServerMaintenance();
			maintenance_.start();
			// Give thread a chance to start (similar to code in AS400ConnectionPool to fix
			// Linux JVM behavior).
			if (!maintenance_.isRunning())										 //@A1A
			{																	 //@A1A
				try																 //@A1A
				{															     //@A1A
					Thread.sleep(10);											 //@A1A
				}																 //@A1A
				catch (InterruptedException e)			 						 //@A1A
				{ /*Should not happen*/ }										 //@A1A
			}																	 //@A1A
			// If thread has still not started, keep giving it chances for 5 minutes.
			for (int i = 1; !maintenance_.isRunning() && i<6000; i++)			 //@A1A 
			{
				try
				{									   							 //@A1A			
					Thread.sleep(50);										 	 //@A1A
				}												 				 //@A1A
				catch (InterruptedException e)							 		 //@A1A
				{ /*Should not happen*/ }										 //@A1A
			}                                                        			 //@A1A
			if (!maintenance_.isRunning())									 	 //@A1A
				Trace.log(Trace.WARNING, "maintenance thread failed to start");	 //@A1A
		}
	}

	/**
	Runs the controller with an InputStream and an OutputStream.
	**/
	public void runInputStream (InputStream inputStream, OutputStream outputStream)
	throws IOException
	{
		try
		{
			// Get a request.
			PxReqSV request = (PxReqSV) factory_.getNextDS (inputStream);
			if (Trace.isTraceProxyOn ())
				request.dump (Trace.getPrintWriter ());

			long clientId = request.getClientId();

			// If the request is a connect request, process here.
			if (request instanceof PxTunnelConnectReqSV)
			{
				synchronized (nextConnectionIdLock_) {
					connectionId_ = ++nextConnectionId_;
				}

				PxRepSV reply;

				// Process the request and return the reply, if any.
				reply = (PxRepSV) request.process ();
				if (reply != null)
				{
					if (clientCleanupInterval_ > 0)	 // do not do cleanup if <= 0	                              //@A1A
					{												  //@A1A
						useTimes_.put(new Long(reply.getClientId()), new Long(System.currentTimeMillis()));	  //@A1A
					}												  //@A1A
					reply.setCorrelationId(request.getCorrelationId());

					synchronized (outputStream) {
						if (Trace.isTraceProxyOn ())
							reply.dump (Trace.getPrintWriter ());
						reply.writeTo(outputStream);
						outputStream.flush();
					}
				}
			}
			// Else check to see if a valid clientId_ was specified in request.  If no, return an
			// exception.  If yes, give the request to the connection object for this clientId_.
			else
			{
				PSTunnelConnection connectionObject = (PSTunnelConnection)clientIds_.get(new Long(clientId));
				if (connectionObject == null)
				{
					PxRepSV reply = new PxExceptionRepSV(new ProxyException (ProxyException.CONNECTION_DROPPED));
                    reply.setCorrelationId(request.getCorrelationId());
					synchronized (outputStream) {
						if (Trace.isTraceProxyOn ())
							reply.dump (Trace.getPrintWriter ());
						reply.writeTo(outputStream);
						outputStream.flush();
					}
				}
				else
				{
					connectionObject.runRequest(request, outputStream);
					if (clientCleanupInterval_ > 0)	  // do not do cleanup if <= 0			   //@A1A
					{										   //@A1A
						useTimes_.put(new Long(clientId), new Long(System.currentTimeMillis()));   //@A1A
					}										   //@A1A
				}
			}
		}
		catch (IOException e)
		{
			Trace.log (Trace.ERROR, "IOException in PSTunnelConnection::runInputStream", e);
			throw e;
		}
	}

	//@A1A
	//clean up client connections that have expired
	class TunnelProxyServerMaintenance extends Thread
	{
		private boolean run_ = false;					// Whether the maintenance is running.
		/**
		*  Constructs a TunnelProxyServerMaintenance object.
		**/
		public TunnelProxyServerMaintenance()
		{
			super();
			setDaemon(true);
		}

		/**
	    *  Indicates whether the maintenance thread is running.
	    *  @return true if running; false otherwise.
		*  Default value is false.
	    **/
		public boolean isRunning()
		{
			return run_;
		}

		/**
		*  Runs the TunnelProxyServer maintenance thread.
		**/
		public synchronized void run()
		{
			run_ = true;
			if (Trace.isTraceOn())
				Trace.log(Trace.INFORMATION, "TunnelProxyServer maintenance daemon is started...");

			while (true)
			{
				try
				{
					// sleep for cleanup interval.
					sleep(clientCleanupInterval_);
					cleanupConnections();
				}
				catch (InterruptedException ie)
				{
					// Should not happen.
					Trace.log(Trace.ERROR, "TunnelProxyServer maintenance daemon failed.");
				}
			}
		}
	}
}

