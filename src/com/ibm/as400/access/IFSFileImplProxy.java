///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: IFSFileImplProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
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
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  IFSFileImplProxy ()
  {
    super ("IFSFile");
  }

  public int canRead0()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethodReturnsInt (pxId_, "canRead0");
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  public int canWrite0()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethodReturnsInt (pxId_, "canWrite0");
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  // @D3 created0 is a new method
  public long created0()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "created0").getReturnValueLong();
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



  public int delete0()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethodReturnsInt (pxId_, "delete0");
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  public int exists0()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "exists0")
                        .getReturnValueInt();
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
      return connection_.callMethod (pxId_, "getCCSID").getReturnValueInt();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  public long getFreeSpace()
    throws IOException
  {
    try {
      return connection_.callMethod (pxId_, "getFreeSpace").getReturnValueLong();
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

  public int isDirectory0()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethodReturnsInt (pxId_, "isDirectory0");
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  public int isFile0()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethodReturnsInt (pxId_, "isFile0");
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

   // @D1 - method is new for V5R1 because of changes to java.io.file in Java 2.
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

   // @D1 - method is new for V5R1 because of changes to java.io.file in Java 2.
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

  // @D3 lastAccessed0 is a new method
  public long lastAccessed0()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "lastAccessed0").getReturnValueLong();
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  public long lastModified0()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "lastModified0").getReturnValueLong();
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  public long length0()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "length0").getReturnValueLong();
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
  public IFSCachedAttributes[] listDirectoryDetails(String directoryPath,
                                                    int maxGetCount,            // @D4A
                                                    String restartName)         // @D4A
    throws IOException, AS400SecurityException
  {
    try {
      return (IFSCachedAttributes[]) connection_.callMethod (pxId_, "listDirectoryDetails",
                              new Class[] { String.class, Integer.TYPE, String.class },     // @D4C
                              new Object[] { directoryPath, new Integer(maxGetCount), restartName })    // @D4C
        .getReturnValue();
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  public int mkdir0(String directory)
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "mkdir0",
                              new Class[] { String.class },
                              new Object[] { directory })
        .getReturnValueInt();
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  public int mkdirs0()
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethodReturnsInt (pxId_, "mkdirs0");
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
    }
  }

  public int renameTo0(IFSFileImpl file)
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "renameTo0",
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

   // @D1 - method is new for V5R1 because of changes to java.io.file in Java 2.
  public boolean setFixedAttributes(int attributes)
    throws IOException
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

   // @D1 - method is new for V5R1 because of changes to java.io.file in Java 2.
  public boolean setHidden(boolean attribute)
    throws IOException
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
    throws IOException
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

   // @D1 - method is new for V5R1 because of changes to java.io.file in Java 2.
  public boolean setReadOnly(boolean attribute)
    throws IOException
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
