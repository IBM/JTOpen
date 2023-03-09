///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileOutputStreamImplProxy.java
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
 Provides a local proxy implementation for the IFSFileOutputStream and
 IFSTextFileOutputStream classes.
 **/
class IFSFileOutputStreamImplProxy
extends AbstractProxyImpl
implements IFSFileOutputStreamImpl
{
  IFSFileOutputStreamImplProxy ()
  {
    super ("IFSFileOutputStream");
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

  public void connectAndOpen(int ccsid)
    throws AS400SecurityException, IOException
  {
    try {
      connection_.callMethod (pxId_, "connectAndOpen",
                              new Class[] { Integer.TYPE },
                              new Object[] { new Integer(ccsid) });
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


  public void open(int fileDataCCSID)
    throws IOException
  {
    try {
      connection_.callMethod (pxId_, "open",
                              new Class[] { Integer.TYPE },
                              new Object[] { new Integer(fileDataCCSID) });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }


  public void setAppend(boolean append)
  {
    try {
      connection_.callMethod (pxId_, "setAppend",
                              new Class[] { Boolean.TYPE },
                              new Object[] { new Boolean(append) });
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

  public void write(byte[] data,
                    int    dataOffset,
                    int    length)
    throws IOException
  {
    try {
      connection_.callMethod (pxId_, "write",
                              new Class[] { byte[].class, Integer.TYPE, Integer.TYPE },
                              new Object[] { data, new Integer(dataOffset),
                              new Integer(length) });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }


  public void writeText(String data, int ccsid)
    throws IOException
  {
    try {
      connection_.callMethod (pxId_, "writeText",
                              new Class[] { String.class, Integer.TYPE },
                              new Object[] { data, new Integer(ccsid) });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

}
