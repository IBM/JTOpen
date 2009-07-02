///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileImplProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2007 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
// @D7 - 07/25/2007 - Add allowSortedRequests to the listDirectoryDetails()
//                    method to resolve problem of issuing PWFS List Attributes 
//                    request with both "Sort" indication and "RestartByID" 
//                    which is documented to be an invalid combination.
// @D8 - 04/03/2008 - Add clearCachedAttributes() to clear impl cache attributes. 
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


/**
 Provides a local proxy implementation for the IFSFile class.
 **/
class IFSFileImplProxy
extends AbstractProxyImpl
implements IFSFileImpl
{

  IFSFileImplProxy ()
  {
    super ("IFSFile");
  }

  public boolean canExecute()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethodReturnsBoolean (pxId_, "canExecute");
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  public boolean canRead()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethodReturnsBoolean (pxId_, "canRead");
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  public boolean canWrite()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethodReturnsBoolean (pxId_, "canWrite");
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  public void clearCachedAttributes()                         //@D8A
  {
    try {
      connection_.callMethod(pxId_, "clearCachedAttributes");
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }

  }

  public boolean copyTo(String path, boolean replace)
    throws IOException, AS400SecurityException, ObjectAlreadyExistsException
  {
    try
    {
      return ((Boolean)connection_.callMethod(pxId_, "copyTo",
                                              new Class[] { String.class, Boolean.TYPE },
                                              new Object[] { path, new Boolean(replace) }).getReturnValue()).booleanValue();
    }
    catch (InvocationTargetException e)
    {
      throw rethrow2(e);
    }
  }
  
  // @D3 created0 is a new method
  public long created()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "created").getReturnValueLong();
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  public int createNewFile()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethodReturnsInt (pxId_, "createNewFile");
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }



  public int delete()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethodReturnsInt (pxId_, "delete");
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  public int exists()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethodReturnsInt (pxId_, "exists");
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  // @A3a
  public int getCCSID()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethodReturnsInt (pxId_, "getCCSID");
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  public long getAvailableSpace(boolean forUserOnly)
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "getAvailableSpace",
                              new Class[] { Boolean.TYPE },
                              new Object[] { new Boolean(forUserOnly) })
        .getReturnValueLong();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  public long getTotalSpace(boolean forUserOnly)
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "getTotalSpace",
                              new Class[] { Boolean.TYPE },
                              new Object[] { new Boolean(forUserOnly) })
        .getReturnValueLong();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }


  public String getOwnerName()
    throws IOException, AS400SecurityException
  {
    try {
      return (String)connection_.callMethod (pxId_, "getOwnerName").getReturnValue();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }


  // @B7a
  public long getOwnerUID()
    throws IOException, AS400SecurityException      // @C0c
  {
    try {
      return connection_.callMethod (pxId_, "getOwnerUID").getReturnValueLong();   // @C0c
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  public String getPathPointedTo()
    throws IOException, AS400SecurityException
  {
    try {
      return (String)connection_.callMethod (pxId_, "getPathPointedTo")
                                .getReturnValue();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  // @B5a
  public String getSubtype()
    throws IOException, AS400SecurityException
  {
    try {
      return (String)connection_.callMethod (pxId_, "getSubtype")
                                .getReturnValue();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  public int isDirectory()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethodReturnsInt (pxId_, "isDirectory");
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  public int isFile()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethodReturnsInt (pxId_, "isFile");
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

   // @D1 - new method because of changes to java.io.file in Java 2.
  public boolean isHidden()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethodReturnsBoolean (pxId_, "isHidden");
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

   // @D1 - new method because of changes to java.io.file in Java 2.
  public boolean isReadOnly()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethodReturnsBoolean (pxId_, "isReadOnly");
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  public boolean isSymbolicLink()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethodReturnsBoolean (pxId_, "isSymbolicLink");
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  // @D3 lastAccessed0 is a new method
  public long lastAccessed()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "lastAccessed").getReturnValueLong();
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  public long lastModified()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "lastModified").getReturnValueLong();
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  public long length()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "length").getReturnValueLong();
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }


  // @A1A
  // List the files/directories in the specified directory.
  public String[] listDirectoryContents(String directoryPath)
    throws IOException, AS400SecurityException
  {
    try {
      return (String[]) connection_.callMethod (pxId_, "listDirectoryContents",
                              new Class[] { String.class },
                              new Object[] { directoryPath })
        .getReturnValue();
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  // @A2A
  // List the file/directory details in the specified directory.
  public IFSCachedAttributes[] listDirectoryDetails(String directoryPattern,
                                                    String directoryPath,
                                                    int maxGetCount,            // @D4A
                                                    String restartName)         // @D4A
    throws IOException, AS400SecurityException
  {
    try {
      return (IFSCachedAttributes[]) connection_.callMethod (pxId_, "listDirectoryDetails",
                              new Class[] { String.class, String.class, Integer.TYPE, String.class },     // @D4C
                              new Object[] { directoryPattern, directoryPath, new Integer(maxGetCount), restartName })    // @D4C
        .getReturnValue();
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  // @C3A
  // List the file/directory details in the specified directory.
  public IFSCachedAttributes[] listDirectoryDetails(String directoryPattern,
                                                    String directoryPath,
                                                    int maxGetCount,
                                                    byte[] restartID, 
                                                    boolean allowSortedRequests) //@D7C
    throws IOException, AS400SecurityException
  {
    try {
      return (IFSCachedAttributes[]) connection_.callMethod (pxId_, "listDirectoryDetails",
                              new Class[] { String.class, String.class, Integer.TYPE, byte[].class, Boolean.TYPE },
                              new Object[] { directoryPattern, directoryPath, new Integer(maxGetCount), restartID, new Boolean(allowSortedRequests) })//@D7C
        .getReturnValue();
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  public int mkdir(String directory)
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "mkdir",
                              new Class[] { String.class },
                              new Object[] { directory })
        .getReturnValueInt();
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  public int mkdirs()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethodReturnsInt (pxId_, "mkdirs");
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  public int renameTo(IFSFileImpl file)
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "renameTo",
                                     new Class[] { IFSFileImpl.class },
                                     new Object[] { file })
        .getReturnValueInt();
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }


  private static InternalErrorException rethrow2 (InvocationTargetException e)
    throws IOException, AS400SecurityException
  {
    Throwable e2 = e.getTargetException ();
    if (e2 instanceof IOException)
      throw (IOException) e2;
    else if (e2 instanceof AS400SecurityException)
      throw (AS400SecurityException) e2;
    else
      return ProxyClientConnection.rethrow (e);
  }

  public boolean setAccess(int accessType, boolean enableAccess, boolean ownerOnly)
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "setAccess",
                                     new Class[] { Integer.TYPE, Boolean.TYPE, Boolean.TYPE },
                                     new Object[] { new Integer(accessType), new Boolean(enableAccess), new Boolean(ownerOnly) })
        .getReturnValueBoolean();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  public boolean setCCSID(int ccsid)
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "setCCSID",
                                     new Class[] { Integer.TYPE },
                                     new Object[] { new Integer(ccsid) })
        .getReturnValueBoolean();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }


   // @D1 - new method because of changes to java.io.file in Java 2.
  public boolean setFixedAttributes(int attributes)
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "setFixedAttributes",
                                     new Class[] { Integer.TYPE },
                                     new Object[] { new Integer(attributes) })
        .getReturnValueBoolean();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

   // @D1 - new method because of changes to java.io.file in Java 2.
  public boolean setHidden(boolean attribute)
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "setHidden",
                                     new Class[] { Boolean.TYPE },
                                     new Object[] { new Boolean(attribute) })
        .getReturnValueBoolean();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  public boolean setLastModified(long time)
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "setLastModified",
                                     new Class[] { Long.TYPE },
                                     new Object[] { new Long(time) })
        .getReturnValueBoolean();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  // @B8a
  public boolean setLength(int length)
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "setLength",
                                     new Class[] { Integer.TYPE },
                                     new Object[] { new Integer(length) })
        .getReturnValueBoolean();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  public void setPatternMatching(int patternMatching)
  {
    try {
      connection_.callMethod (pxId_, "setPatternMatching",
                              new Class[] { Integer.TYPE },
                              new Object[] { new Integer(patternMatching) });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
  }

   // @D1 - new method because of changes to java.io.file in Java 2.
  public boolean setReadOnly(boolean attribute)
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "setReadOnly",
                                     new Class[] { Boolean.TYPE },
                                     new Object[] { new Boolean(attribute) })
        .getReturnValueBoolean();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  public void setPath(String path)
  {
    try {
      connection_.callMethod (pxId_, "setPath",
                              new Class[] { String.class },
                              new Object[] { path });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
  }

  /**
   Sets the sorting behavior used when files are listed by any of the <tt>list()</tt> or <tt>listFiles()</tt> methods.  The default is <tt>false</tt> (unsorted).
   @param sort If <tt>true</tt>: Return lists of files in sorted order.
   If <tt>false</tt>: Return lists of files in whatever order the file system provides.

   @exception IOException If an error occurs while communicating with the server.
   @exception AS400SecurityException If a security or authority error occurs.
   **/
  public void setSorted(boolean sort)
  {
    try {
      connection_.callMethod (pxId_, "setSorted",
                              new Class[] { Boolean.TYPE },
                              new Object[] { new Boolean(sort) });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
  }

  public void setSystem(AS400Impl system)
  {
    try {
      connection_.callMethod (pxId_, "setSystem",
                              new Class[] { AS400Impl.class },
                              new Object[] { system });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
  }

}
