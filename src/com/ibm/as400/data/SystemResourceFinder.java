///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SystemResourceFinder.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
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
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

  public static final String  m_pcmlExtension = ".pcml";
  public static final String  m_pcmlSerializedExtension = ".pcml.ser";
  public static final String  m_rfmlExtension = ".rfml";                          // @B2A
  public static final String  m_rfmlSerializedExtension = ".rfml.ser";            // @B2A
  private static final String  m_rfmlHeaderName = "com/ibm/as400/data/rfml.dtd";  // @B2A

  private static final String  m_pcmlHeaderName = "com/ibm/as400/data/pcml.dtd";
  private static int   m_headerLineCount;

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


  public static final int getHeaderLineCount()
  {
    return m_headerLineCount;
  }


  /**
   * Helper method used to try loading from all 3 of our class loaders.
  **/
  private static final InputStream loadResource(String docPath)
  {
    InputStream stream = null;
    
    ClassLoader loader = getLoader1();
    if (loader != null) stream = loader.getResourceAsStream(docPath);
    if (stream != null) return new BufferedInputStream(stream);

    loader = getLoader2();
    if (loader != null) stream = loader.getResourceAsStream(docPath);
    if (stream != null) return new BufferedInputStream(stream);

    loader = getLoader3();
    if (loader != null) stream = loader.getResourceAsStream(docPath);
    if (stream != null) return new BufferedInputStream(stream);
    
    return null;
  }


  /**
   * This method tries to get the current thread's context class loader.
  **/
  private static final ClassLoader getLoader1()
  {
    //3/5/2003 OYG: This doesn't work if this class is in a parent
    //classloader and the resource being loaded is in the child.
    //Fixed by using the current thread's class loader.
    try
    {
      return Thread.currentThread().getContextClassLoader(); // Fix for WAS.
    }
    catch (Throwable t)
    {
      if (Trace.isTraceOn()) Trace.log(Trace.PCML, "Couldn't get current thread's context class loader: "+t.toString());
      return null;
    }
  }


  /**
   * This method tries to get the SystemResourceFinder class's class loader.
  **/
  private static final ClassLoader getLoader2()
  {
    try
    {
      Class c = Class.forName("com.ibm.as400.data.SystemResourceFinder");
      try
      {
        return c.getClassLoader();
      }
      catch (Throwable t2)
      {
        if (Trace.isTraceOn()) Trace.log(Trace.PCML, "Couldn't get SystemResourceFinder class loader: "+t2.toString());
        return null;
      }
    }
    catch (Throwable t)
    {
      if (Trace.isTraceOn()) Trace.log(Trace.PCML, "Couldn't get SystemResourceFinder class: "+t.toString());
      return null;
    }
  }


  /**
   * This method returns our own SystemClassLoader, and should be called when
   * the previous getLoaderX() methods either return null or return a class loader
   * that is unable to load the PCML.
  **/
  private static final ClassLoader getLoader3()
  {
    return new SystemClassLoader();
  }


  public static synchronized InputStream getPCMLHeader()
  {
    // Construct the full resource name
    String docPath = m_pcmlHeaderName;

    InputStream stream = loadResource(docPath);
    if (stream == null)
    {
      // We couldn't load the resource, no matter how many class loaders we tried.
      throw new MissingResourceException(SystemResourceFinder.format(DAMRI.PCML_DTD_NOT_FOUND, new Object[] {m_pcmlHeaderName}), m_pcmlHeaderName, "");
    }

    if (m_headerLineCount <= 0)
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
      stream = loadResource(docPath);
    }

    return stream;
  }


  // @B4A -- New method for XPCML
  public static synchronized InputStream getXPCMLTransformFile(String fileName)
  {
    String docPath = "com/ibm/as400/data/"+fileName;

    InputStream stream = loadResource(docPath);

    if (stream == null)
    {
      if (Trace.isTraceOn()) Trace.log(Trace.PCML, "XSL file not found.");
      throw new MissingResourceException(SystemResourceFinder.format(DAMRI.XML_NOT_FOUND, new Object[] {fileName}), fileName, "");
    }
    return stream;
  }


  static synchronized InputStream getPCMLDocument(String docName, ClassLoader loader)     // @B1C
  throws MissingResourceException
  {
    String docPath = null;

    // Construct the full resource name
    // $B4 -- Add xpcml and xpcmlsrc as possible doc endings
    if (docName.endsWith(".pcml") || docName.endsWith(".pcmlsrc") ||
        docName.endsWith(".xpcml") || docName.endsWith(".xpcmlsrc"))     //@B4
    {
      String baseName = docName.substring(0, docName.lastIndexOf('.') );
      String extension = docName.substring(docName.lastIndexOf('.') );
      docPath = baseName.replace('.', '/') + extension;
    }
    else
    {
      docPath = docName.replace('.', '/') + m_pcmlExtension;
    }

    InputStream stream = null;
    if (loader != null)
    {
      stream = loader.getResourceAsStream(docPath);
      if (stream != null) return new BufferedInputStream(stream);
      // We throw an exception here because if the user specified a ClassLoader,
      // they probably don't want us to use ours, and we don't want to return another
      // version of the document found by a different class loader.
      throw new MissingResourceException(SystemResourceFinder.format(DAMRI.PCML_NOT_FOUND, new Object[] {docName}), docName, "");
    }
    
    stream = loadResource(docPath);
    if (stream != null) return stream;
    
    // We couldn't load the resource, no matter how many class loaders we tried.
    throw new MissingResourceException(SystemResourceFinder.format(DAMRI.PCML_NOT_FOUND, new Object[] {docName}), docName, "");
  }


  static synchronized InputStream getSerializedPCMLDocument(String docName, ClassLoader loader)   // @B1C
  throws MissingResourceException
  {
    // Construct the full resource name
    String docPath = docName.replace('.', '/') + m_pcmlSerializedExtension;

    InputStream stream = null;
    if (loader != null)
    {
      stream = loader.getResourceAsStream(docPath);
      if (stream != null) return new BufferedInputStream(stream);
      // We throw an exception here because if the user specified a ClassLoader,
      // they probably don't want us to use ours, and we don't want to return another
      // version of the document found by a different class loader.
      throw new MissingResourceException(SystemResourceFinder.format(DAMRI.SERIALIZED_PCML_NOT_FOUND, new Object[] {docName} ), docName, "");
    }

    stream = loadResource(docPath);
    if (stream != null) return stream;

    // We couldn't load the resource, no matter how many class loaders we tried.
    throw new MissingResourceException(SystemResourceFinder.format(DAMRI.SERIALIZED_PCML_NOT_FOUND, new Object[] {docName}), docName, "");
  }


  public static synchronized InputStream getRFMLHeader()
  {
    String docPath = m_rfmlHeaderName;

    InputStream stream = loadResource(docPath);
    if (stream == null)
    {
      throw new MissingResourceException(SystemResourceFinder.format(DAMRI.DTD_NOT_FOUND, new Object[] {"RFML", m_rfmlHeaderName}), m_rfmlHeaderName, "");
    }
    return stream;
  }


  static synchronized InputStream getRFMLDocument(String docName, ClassLoader loader)     // @B2A
  throws MissingResourceException
  {
    String docPath = null;

    // Construct the full resource name
    if (docName.endsWith(".rfml")
        || docName.endsWith(".rfmlsrc"))
    {
      String baseName = docName.substring(0, docName.lastIndexOf('.') );
      String extension = docName.substring(docName.lastIndexOf('.') );
      docPath = baseName.replace('.', '/') + extension;
    }
    else
    {
      docPath = docName.replace('.', '/') + m_rfmlExtension;
    }

    InputStream stream = null;
    if (loader != null)
    {
      stream = loader.getResourceAsStream(docPath);
      if (stream != null) return new BufferedInputStream(stream);
      // We throw an exception here because if the user specified a ClassLoader,
      // they probably don't want us to use ours, and we don't want to return another
      // version of the document found by a different class loader.
      throw new MissingResourceException(SystemResourceFinder.format(DAMRI.XML_NOT_FOUND, new Object[] {"RFML", docName}), docName, "");
    }

    stream = loadResource(docPath);
    if (stream != null) return stream;

    // We couldn't load the resource, no matter how many class loaders we tried.
    throw new MissingResourceException(SystemResourceFinder.format(DAMRI.XML_NOT_FOUND, new Object[] {"RFML", docName}), docName, "");
  }


  static synchronized InputStream getSerializedRFMLDocument(String docName, ClassLoader loader)   // @B2A
  throws MissingResourceException
  {
    // Construct the full resource name
    String docPath = docName.replace('.', '/') + m_rfmlSerializedExtension;

    InputStream stream = null;
    if (loader != null)
    {
      stream = loader.getResourceAsStream(docPath);
      if (stream != null) return new BufferedInputStream(stream);
      // We throw an exception here because if the user specified a ClassLoader,
      // they probably don't want us to use ours, and we don't want to return another
      // version of the document found by a different class loader.
      throw new MissingResourceException(SystemResourceFinder.format(DAMRI.SERIALIZED_XML_NOT_FOUND, new Object[] {"RFML", docName} ), docName, "");
    }

    stream = loadResource(docPath);
    if (stream != null) return stream;

    // We couldn't load the resource, no matter how many class loaders we tried.
    throw new MissingResourceException(SystemResourceFinder.format(DAMRI.SERIALIZED_XML_NOT_FOUND, new Object[] {"RFML", docName} ), docName, "");
  }


/** @B4 -- NEW METHOD FOR XPCML
 * isXPCML -- Returns true or false depending on whether the document to
 * parse is determined to be an XPCML document.  Returns true is XPCML,
 * false if PCML.
 */
  protected static boolean isXPCML(String docName,ClassLoader loader)
  throws MissingResourceException, IOException  
  {

    String docPath = null;

    // Construct the full resource name
    // $B4 -- Add xpcml and xpcmlsrc as possible doc endings
    if (docName.endsWith(".pcml") || docName.endsWith(".pcmlsrc") ||
        docName.endsWith(".xpcml") || docName.endsWith(".xpcmlsrc"))     //@B4
    {
      String baseName = docName.substring(0, docName.lastIndexOf('.') );
      String extension = docName.substring(docName.lastIndexOf('.') );
      docPath = baseName.replace('.', '/') + extension;
    }
    else
    {
      docPath = docName.replace('.', '/') + m_pcmlExtension;
    }

    boolean isXPCML = false;
    InputStream stream = null;
    if (loader != null)
    {
      stream = loader.getResourceAsStream(docPath);
      if (stream == null)
      {
        // We throw an exception here because if the user specified a ClassLoader,
        // they probably don't want us to use ours, and we don't want to return another
        // version of the document found by a different class loader.
        throw new MissingResourceException(SystemResourceFinder.format(DAMRI.PCML_DTD_NOT_FOUND, new Object[] {docName}), docName, "");
      }
    }
    if (stream == null)
    {
      stream = loadResource(docPath);
      if (stream == null)
      {
        // We couldn't load the resource, no matter how many class loaders we tried.
        throw new MissingResourceException(SystemResourceFinder.format(DAMRI.PCML_DTD_NOT_FOUND, new Object[] {docName}), docName, "");
      }
    }
    
    // Cache the line count of the header
    LineNumberReader lnr = new LineNumberReader(new InputStreamReader(stream));
    try
    {
      String line = lnr.readLine();
      boolean found=false;
      while (line != null && !found)
      {
        // Look for xpcml tag
        if (line.indexOf("<xpcml") != -1)
        {
          found = true;
          isXPCML= true;
          continue;
        }
        if (line.indexOf("<pcml") != -1)
        {
          found = true;
          isXPCML = false;
          continue;
        }
        line = lnr.readLine();
      }
    }
    catch (IOException e)
    {
      Trace.log(Trace.PCML, "Error when reading input stream in isXPCML");
      throw e;
    }
    // Return isXPCML
    return isXPCML;
  }
}

