///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: TunnelProxyServer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import java.io.IOException;

/**
  * TunnelProxyServer is an HttpServlet implementation that enables proxy tunneling.
  **/
public class TunnelProxyServer extends HttpServlet
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

	//each servlet will have a controller
	private PSTunnelController    controller_;

	/**
	 * Sends information back to the web server.
	 * 
	 * @param  req  HttpServletRequest that encapsulates the request to the servlet. 
	 * @param  resp  HttpServletResponse that encapsulates the response from the servlet. 
	 * @exception   ServletException If a problem with the servlet occurs.
	 * @exception   IOException If a file I/O error occurs.
	 */
	public void doPost(HttpServletRequest req,
					   HttpServletResponse resp)
	throws ServletException, IOException
	{
		Trace.log(Trace.INFORMATION, "entering TunnelProxyServer::doPost");
		resp.setContentType("application/octet-stream");

		Trace.log(Trace.INFORMATION, "starting processing of request in TunnelProxyServer");
		controller_.runInputStream(req.getInputStream(), resp.getOutputStream());
		Trace.log(Trace.INFORMATION, "request processed in TunnelProxyServer");

		resp.getOutputStream().flush();
		Trace.log(Trace.INFORMATION, "exiting TunnelProxyServer::doPost");
	}

	/**
	 * Requests information from the web server.
	 * 
	 * @param  req  HttpServletRequest that encapsulates the request to the servlet. 
	 * @param  resp  HttpServletResponse that encapsulates the response from the servlet. 
	 * @exception   ServletException If a problem with the servlet occurs.
	 * @exception   IOException If a file I/O error occurs.
	 */
	public void doGet(HttpServletRequest req,
					  HttpServletResponse resp)
	throws ServletException, IOException
	{
		Trace.log(Trace.INFORMATION, "entering TunnelProxyServer::doGet");
		doPost(req,resp);
		Trace.log(Trace.INFORMATION, "exiting TunnelProxyServer::doGet");
	}


	/**
	 * Servlet initialization.  
	 *
	 * @param   config  The servlet configuration.  
	 */
	public void init(ServletConfig config)
	{
		Trace.log(Trace.INFORMATION, "entering TunnelProxyServer::init");

		try
		{
			super.init(config);
		}
		catch (ServletException e)
		{
			Trace.log(Trace.ERROR, "exception thrown from super.init", e);
		}

		ProxyServer proxyServer = new ProxyServer();  
		controller_ = new PSTunnelController (proxyServer);
		Trace.log(Trace.INFORMATION, "exiting TunnelProxyServer::init");
	}
}
