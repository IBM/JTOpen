///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SystemClassLoader.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import java.net.*;
import java.io.*;

/**
 * The SystemClassLoader loads system classes (those in your classpath).
 * This is an attempt to unify the handling of system classes and ClassLoader
 * classes.
 */
class SystemClassLoader extends java.lang.ClassLoader
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

  protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException
  {
    return findSystemClass(name);
  }

  public InputStream getResourceAsStream(String name)
  {
    return ClassLoader.getSystemResourceAsStream(name);
  }

  public URL getResource(String name)
  {
    return ClassLoader.getSystemResource(name);
  }
}

