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

  public int exists0(String name)
    throws IOException, AS400SecurityException
  {
    try {
      return connection_.callMethod (pxId_, "exists0",
                                     new Class[] { String.class },
                                     new Object[] { name })
                        .getReturnValueInt();
    }
    catch (InvocationTargetException e) {
      throw rethrow2 (e);
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
