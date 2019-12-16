///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileInputStreamImplProxy.java
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
 Provides a local proxy implementation for the IFSFileInputStream and
 IFSTextFileInputStream classes.
 **/
class IFSFileInputStreamImplProxy
extends AbstractProxyImpl
implements IFSFileInputStreamImpl
{
  IFSFileInputStreamImplProxy ()
  {
    super ("IFSFileInputStream");
  }

  public int available()
    throws IOException
  {
    try {
      return connection_.callMethod (pxId_, "available").getReturnValueInt();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
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


  public IFSKey lock(long length)
    throws IOException
  {
    try {
      return (IFSKey) connection_.callMethod (pxId_, "lock",
                              new Class[] { Long.TYPE },
                              new Object[] { new Long(length) })
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

  private static final boolean[] ARGS_TO_RETURN = new boolean[] {true, false, false};

  public int read(byte[] data,
                  int    dataOffset,
                  int    length)
    throws IOException
  {
    try {
      ProxyReturnValue rv = connection_.callMethod (pxId_, "read",
                            new Class[] { byte[].class,
                                          Integer.TYPE,
                                          Integer.TYPE },
                            new Object[] { data,
                                           new Integer (dataOffset),
                                           new Integer (length) },
                            ARGS_TO_RETURN, false );
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

  public String readText(int length)
    throws IOException
  {
    try {
      return (String) connection_.callMethod (pxId_, "readText",
                              new Class[] { Integer.TYPE },
                              new Object[] { new Integer(length) })
                        .getReturnValue();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
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


  public long skip(long bytesToSkip)
    throws IOException
  {
    try {
      return connection_.callMethod (pxId_, "skip",
                              new Class[] { Long.TYPE },
                              new Object[] { new Long(bytesToSkip) })
                        .getReturnValueLong();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
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

}
