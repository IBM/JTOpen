///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSRandomAccessFileImplProxy.java
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
 Provides a local proxy implementation for the IFSRandomAccessFile class.
 **/
class IFSRandomAccessFileImplProxy
extends AbstractProxyImpl
implements IFSRandomAccessFileImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  

  IFSRandomAccessFileImplProxy ()
  {
    super ("IFSRandomAccessFile");
  }

  public void close()
    throws IOException
  {
    try {
      connection_.callMethod (pxId_, "close");
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  public void connectAndOpen()
    throws AS400SecurityException, IOException
  {
    try {
      connection_.callMethod (pxId_, "connectAndOpen");
    }
    catch (InvocationTargetException e) {
    Throwable e1 = e.getTargetException ();
    if (e1 instanceof AS400SecurityException)
      throw (AS400SecurityException) e1;
    else
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  public void flush()
    throws IOException
  {
    try {
      connection_.callMethod (pxId_, "flush");
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  public long length()
    throws IOException
  {
    try {
      return connection_.callMethod (pxId_, "length").getReturnValueLong();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  public IFSKey lock(int offset,
                     int length)
    throws IOException
  {
    try {
      return (IFSKey) connection_.callMethod (pxId_, "lock",
                              new Class[] { Integer.TYPE, Integer.TYPE },
                              new Object[] { new Integer(offset), new Integer(length) })
                        .getReturnValue();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }


  public void open()
    throws IOException
  {
    try {
      connection_.callMethod (pxId_, "open");
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  private static final boolean[] ARGS_TO_RETURN =
                                  new boolean[] {true, false, false, false};

  public int read(byte[] data,
                  int    dataOffset,
                  int    length,
                  boolean readFully)
    throws IOException
  {
    try {
      ProxyReturnValue rv = connection_.callMethod (pxId_, "read",
                                   new Class[] { byte[].class,
                                                 Integer.TYPE,
                                                 Integer.TYPE,
                                                 Boolean.TYPE},
                                   new Object[] { data,
                                                  new Integer (dataOffset),
                                                  new Integer (length),
                                                  new Boolean (readFully) },
                                   ARGS_TO_RETURN, readFully );
      // Note: The 6th arg says whether to call the method asynchronously.
      // In the case of readFully, we want asynchronous, since the read
      // will wait for the requested number of bytes to become available.

      byte [] returnDataBuffer = (byte[])rv.getArgument(0);
      for (int i=0; i<data.length; i++) {
        data[i] = returnDataBuffer[i];
      }
      return rv.getReturnValueInt();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  public String readLine()
    throws IOException
  {
    try {
      return (String) connection_.callMethod (pxId_, "readLine").getReturnValue();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  public String readUTF()
    throws IOException
  {
    try {
      return (String) connection_.callMethod (pxId_, "readUTF").getReturnValue();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }


  public void setExistenceOption(int existenceOption)
  {
    try {
      connection_.callMethod (pxId_, "setExistenceOption",
                              new Class[] { Integer.TYPE },
                              new Object[] { new Integer(existenceOption) });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
  }


  public void setFD(IFSFileDescriptorImpl fd)
  {
    try {
      connection_.callMethod (pxId_, "setFD",
                              new Class[] { IFSFileDescriptorImpl.class },
                              new Object[] { fd });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
  }


  public void setForceToStorage(boolean forceToStorage)
  {
    try {
      connection_.callMethod (pxId_, "setForceToStorage",
                              new Class[] { Boolean.TYPE },
                              new Object[] { new Boolean(forceToStorage) });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
  }


  public void setLength(int length)
    throws IOException
  {
    try {
      connection_.callMethod (pxId_, "setLength",
                              new Class[] { Integer.TYPE },
                              new Object[] { new Integer(length) });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }


  public void setMode(String mode)
  {
    try {
      connection_.callMethod (pxId_, "setMode",
                              new Class[] { String.class },
                              new Object[] { mode });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
  }


  public void unlock(IFSKey key)
    throws IOException
  {
    try {
      connection_.callMethod (pxId_, "unlock",
                              new Class[] { IFSKey.class },
                              new Object[] { key });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }


  public void writeBytes(byte[]  data,
                          int     dataOffset,
                          int     length)
    throws IOException
  {
    try {
      connection_.callMethod (pxId_, "writeBytes",
                              new Class[] { byte[].class, Integer.TYPE, Integer.TYPE },
                              new Object[] { data, new Integer(dataOffset),
                                             new Integer(length) });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }


  public void writeUTF(String s)
    throws IOException
  {
    try {
      connection_.callMethod (pxId_, "writeUTF",
                              new Class[] { String.class },
                              new Object[] { s });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

}




