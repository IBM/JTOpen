///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: URLParser.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import java.util.Properties;
import java.util.StringTokenizer;
import java.beans.PropertyVetoException;

import com.ibm.as400.util.servlet.ServletHyperlink;

import com.ibm.as400.access.Trace;


/**
*  The URLParser class parses a URL string for the URI, properties, and reference (also known as the "anchor").
*  The reference is indicated by the sharp sign character "#" followed by more characters. For example,
*  <BLOCKQUOTE><PRE>
*  http://www.toolbox.com/index.html#answer1
*  </PRE></BLOCKQUOTE>
*  <P>
*  The reference indicates that after the specified resource is retrieved, the application is specifically interested 
*  in that part of the document that has the tag <i>answer1</i> attached to it. 
*  
*  <P>For example, the following URL string can be parsed into its individual components:
*  <BLOCKQUOTE><PRE>
*  http://myWebSite.com/servlet/myServlet#2043562?parm1="/library/test1#partA"
*  </PRE></BLOCKQUOTE>
*  <P>
*  Here are the individual pieces of the URL:
*  <BLOCKQUOTE><PRE>
*  URL: http://myWebSite.com/servlet/myServlet#2043562?parm1="/library/test1#partA"
*  URI: http://myWebSite.com/servlet/myServlet
*  Reference: 2043562
*  Parameter: {parm1="/library/test1#partA"}
*  </PRE></BLOCKQUOTE>
**/
public class URLParser
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

	private String url_;
	private String uri_;
	private String reference_;
	private Properties parameters_;
	

   /**
    *  Constructs a URLParser object with the specified <i>url</i>.
    *
    *  @param url The url to parse.
    **/
	public URLParser(String url)
	{  
      if (url == null)
         throw new NullPointerException("url");

		url_ = url;
		
		parse(url);    
	}
	

   /**
    *  Returns the URL.
    *  @return The URL.
    **/
	public String getURL()
	{
		return url_;
	}
	

   /**
    *  Returns the URI.
    *  @return The URI.
    **/
	public String getURI()
	{
		return uri_;
	}
	

   /**
    *  Returns the reference, also known as the "anchor".
    *  @return The reference.
    **/
	public String getReference()
	{
		return reference_;
	}
	

   /**
    *  Returns the parameters.
    *  @return The parameters.
    **/
	public Properties getParameters()
	{
		return parameters_;
	}
	

   /**
    *  Returns the HTMLHyperlink.
    *  @return The HTMLHyperlink.
    **/
	public HTMLHyperlink getHTMLHyperlink(String text)
	{		
      if (text == null)
         throw new NullPointerException("text");

		return fillHyperlink(new HTMLHyperlink(), text);
	}
	

   /**
    *  Returns the ServletHyperlink.
    *  @return The ServletHyperlink.
    **/
	public ServletHyperlink getServletHyperlink(String text)
	{
      if (text == null)
         throw new NullPointerException("text");

		return (ServletHyperlink)fillHyperlink(new ServletHyperlink(), text);
	}


   /**
    *  Fills in the HTMLHyperlink with the uri, bookmarks, and properties
    *  if they exist.
    **/
   private HTMLHyperlink fillHyperlink(HTMLHyperlink link, String text)
	{
		try
		{
			if (reference_ == null)
				link.setLink(uri_);			
			else
				link.setLink(uri_ + "#" + reference_);
			
			if (parameters_ != null)
				link.setProperties(parameters_);
         
         link.setText(text);
		}
		catch (PropertyVetoException e)
		{ /* Ignore */ }
		
		return link;
	}


	/**
    *  Parse out the uri, reference or anchor, and properties from the url.
    **/
	private void parse(String url)
	{
		if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "Parsing URL String...");

		int index = url.indexOf("?");
		String parms;
		
		// first parse out the parameters from the URI
		if (index != -1)
		{
			uri_ = url.substring(0, index);
			parms = url.substring(index+1);
		}
		else
		{
			uri_ = url;
			parms = null;
		}
		
		// now parse out the reference or anchor target
		index = uri_.indexOf("#");
		if (index != -1)
		{
			reference_ = uri_.substring(index+1);
			uri_ = uri_.substring(0, index);
		}
		else
		{
			reference_ = null;
		}
		
		// then parse the parameters
		if (parms != null)
		{
			parameters_ = new Properties();
			
			StringTokenizer st = new StringTokenizer(parms, "&");
			while (st.hasMoreTokens())
			{
				String parm = st.nextToken();
				
				index = parm.indexOf("=");
				String key;
				String value;
				if (index != -1)
				{
					key = parm.substring(0, index);
					value = parm.substring(index+1);
				}
				else
				{
					key = parm;
					value = "";
				}
				
				parameters_.put(key, value);
			}
		}
	}		
}
