///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SystemResourceFinder.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import com.ibm.as400.access.Trace;                  // @B3A
import java.io.*;
import java.text.MessageFormat;
import java.util.*;

class SystemResourceFinder
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    public static final String  m_pcmlExtension = ".pcml";
    public static final String  m_pcmlSerializedExtension = ".pcml.ser";
    public static final String  m_rfmlExtension = ".rfml";                          // @B2A
    public static final String  m_rfmlSerializedExtension = ".rfml.ser";            // @B2A
    private static final String  m_rfmlHeaderName = "com/ibm/as400/data/rfml.dtd";  // @B2A

    private static ResourceLoader m_loader = new ResourceLoader();

    static
    {
        // Load the resource bundle for common properties.
        m_loader.setResourceName("com.ibm.as400.data.DAMRI");
    }

    public static final String format(String key) 
    {
        return format(key, null);
    }

    public static final String format(String key, Object[] args) 
    {
        if (args != null)
        {
            try
            {
                return MessageFormat.format(m_loader.getString(key), args);
            }
            catch (Exception e)
            {
            }
        }
        return m_loader.getString(key);
    }

    public static final String getString(String key) 
    {
        return m_loader.getStringWithNoSubstitute(key);
    }
	
    public static final InputStream getPCMLHeader() 
    {
      return getPCMLHeader(getLoader());
    }
  
    public static final InputStream getRFMLHeader()     // @B2A
    {
      return getRFMLHeader(getLoader());
    }
  
    public static final int getHeaderLineCount() 
    {
      return m_headerLineCount;
    }


    /*
     * Automatic determination of the ClassLoader to be used to load
     * resources on behalf of the client.  N.B. The client is getLoader's
     * caller's caller.
     */
    private static ClassLoader getLoader() 
    {
        debug("SecurityManager=" + System.getSecurityManager());

        Class c = null;
        try    
        { 
            c = Class.forName("com.ibm.as400.data.SystemResourceFinder");    
        }
        catch (Throwable t) 
        { 
            if (Trace.isTraceOn()) Trace.log(Trace.PCML, format("Couldn't get SystemResourceFinder class.")); 
        }

        ClassLoader cl = null;
        if (c != null)
        {
          //3/5/2003 OYG: This doesn't work if this class is in a parent
          //classloader and the resource being loaded is in the child.
          //Fixed by using the current thread's class loader.
          try
          {
            cl = Thread.currentThread().getContextClassLoader(); // Fix for WAS.
          }
          catch(Throwable t)
          {
            if (Trace.isTraceOn()) Trace.log(Trace.PCML, format("Couldn't get current thread's context class loader."));
            try
            {
              cl = c.getClassLoader();
            }
            catch(Throwable t2)
            {
              if (Trace.isTraceOn()) Trace.log(Trace.PCML, format("Couldn't get SystemResourceFinder class loader."));
            }
          }
          if (cl == null)
          {
            cl = new SystemClassLoader();
          }
        }
        else {
            cl = new SystemClassLoader();
        }

        debug("getLoader returning " + cl);
        return cl; 
    }
    
    private static synchronized InputStream getPCMLHeader(ClassLoader loader)
        throws MissingResourceException	
    {
          
        InputStream stream = loader.getResourceAsStream(m_pcmlHeaderName);
        
        if (stream == null) 
        {
            throw new MissingResourceException(SystemResourceFinder.format(DAMRI.PCML_DTD_NOT_FOUND, new Object[] {m_pcmlHeaderName}), m_pcmlHeaderName, "");
        }
        
        if (!(m_headerLineCount > 0)) 
        {
            // Cache the line count of the header
            LineNumberReader lnr = new LineNumberReader(new InputStreamReader(stream));
  
            try 
            {        
                String line = lnr.readLine();
                while (line != null) 
                {
                    m_headerLineCount++;
                    line = lnr.readLine();
                }
            } 
            catch (IOException e) 
            {
            }
          
            // Get the stream again
            stream = loader.getResourceAsStream(m_pcmlHeaderName);
        }
        
        // Make sure stream is buffered
        return new BufferedInputStream(stream);
  	}

    static synchronized InputStream getPCMLDocument(String docName, ClassLoader loader)     // @B1C
        throws MissingResourceException
    {
		String docPath = null;
        
        // Construct the full resource name
		if ( docName.endsWith(".pcml")
		  || docName.endsWith(".pcmlsrc") )
	    {
			String baseName = docName.substring(0, docName.lastIndexOf('.') );
			String extension = docName.substring(docName.lastIndexOf('.') );
			docPath = baseName.replace('.', '/') + extension;
		}
		else
		{
			docPath = docName.replace('.', '/') + m_pcmlExtension;
		}

        if (loader == null)                                         // @B1A
            loader = getLoader();                                   // @B1A
            
        InputStream stream = loader.getResourceAsStream(docPath);
        if (stream == null) 
        {
            throw new MissingResourceException(SystemResourceFinder.format(DAMRI.PCML_NOT_FOUND, new Object[] {docName}), docName, "");
        }

        // Make sure stream is buffered
        return new BufferedInputStream(stream);
  	}
  
  
    static synchronized InputStream getSerializedPCMLDocument(String docName, ClassLoader loader)   // @B1C
        throws MissingResourceException
    {
        
        if (loader == null)                                         // @B1A
            loader = getLoader();                                   // @B1A
            
        // Construct the full resource name
        String docPath = docName.replace('.', '/') + m_pcmlSerializedExtension;

        InputStream stream = loader.getResourceAsStream(docPath);
        if (stream == null)
        {
            throw new MissingResourceException(SystemResourceFinder.format(DAMRI.SERIALIZED_PCML_NOT_FOUND, new Object[] {docName} ), docName, "");
        }
            
        // Make sure stream is buffered
        return new BufferedInputStream(stream);
    }

    private static synchronized InputStream getRFMLHeader(ClassLoader loader)   // @B2A
        throws MissingResourceException	
    {
          
        InputStream stream = loader.getResourceAsStream(m_rfmlHeaderName);
        
        if (stream == null) 
        {
            throw new MissingResourceException(SystemResourceFinder.format(DAMRI.DTD_NOT_FOUND, new Object[] {"RFML", m_rfmlHeaderName}), m_rfmlHeaderName, "");
        }

        // Make sure stream is buffered
        return new BufferedInputStream(stream);
  	}

    static synchronized InputStream getRFMLDocument(String docName, ClassLoader loader)     // @B2A
        throws MissingResourceException
    {
		String docPath = null;
        
        // Construct the full resource name
		if ( docName.endsWith(".rfml")
		  || docName.endsWith(".rfmlsrc") )
	    {
			String baseName = docName.substring(0, docName.lastIndexOf('.') );
			String extension = docName.substring(docName.lastIndexOf('.') );
			docPath = baseName.replace('.', '/') + extension;
		}
		else
		{
			docPath = docName.replace('.', '/') + m_rfmlExtension;
		}

        if (loader == null)
            loader = getLoader();
            
        InputStream stream = loader.getResourceAsStream(docPath);

        if (stream == null) 
        {
            throw new MissingResourceException(SystemResourceFinder.format(DAMRI.XML_NOT_FOUND, new Object[] {"RFML", docName}), docName, "");
        }

        // Make sure stream is buffered
        return new BufferedInputStream(stream);
  	}
  
  
    static synchronized InputStream getSerializedRFMLDocument(String docName, ClassLoader loader)   // @B2A
        throws MissingResourceException
    {
        
        if (loader == null)
            loader = getLoader();
            
        // Construct the full resource name
        String docPath = docName.replace('.', '/') + m_rfmlSerializedExtension;

        InputStream stream = loader.getResourceAsStream(docPath);
        if (stream == null)
        {
            throw new MissingResourceException(SystemResourceFinder.format(DAMRI.SERIALIZED_XML_NOT_FOUND, new Object[] {"RFML", docName} ), docName, "");
        }
            
        // Make sure stream is buffered
        return new BufferedInputStream(stream);
    }



  /**
     * For printf debugging.
     */
    private static boolean debugFlag = false;
    private static void debug(String str) 
    {
        if( debugFlag ) 
        {
            System.out.println("SystemResourceFinder: " + str);
        }
    }

    private static final String  m_pcmlHeaderName = "com/ibm/as400/data/pcml.dtd";
    private static int   m_headerLineCount;

}


/**
 * The SystemClassLoader loads system classes (those in your classpath).
 * This is an attempt to unify the handling of system classes and ClassLoader
 * classes.
 */
class SystemClassLoader extends java.lang.ClassLoader {

    protected Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        return findSystemClass(name);
    }

    public InputStream getResourceAsStream(String name)
    {
        return ClassLoader.getSystemResourceAsStream(name);
    }

    public java.net.URL getResource(String name)
    {
        return ClassLoader.getSystemResource(name);
    }

}
