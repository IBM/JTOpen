///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDOutputStreamProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.OutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;



// JDBC 2.0
class JDOutputStreamProxy
extends OutputStream
implements ProxyFactoryImpl
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";


  // Private data.
  
  private long                    pxId_;
  private ProxyClientConnection   connection_;



  public void close ()
    throws IOException
  {
    try {
      connection_.callMethod (pxId_, "close");
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }


  protected void finalize() throws Throwable
  {
    connection_.callFinalize (pxId_);
    super.finalize();
  }


  // Implementation of ProxyFactoryImpl interface.
  // This method gets called by ProxyClientConnection.callFactoryMethod().
  public void initialize (long proxyId, ProxyClientConnection connection)
  {
    pxId_ = proxyId;
    connection_ = connection;
  }


  public void write (int b)
  {
    try {
      connection_.callMethod (pxId_, "write",
                               new Class[] { Integer.TYPE },
                               new Object[] { new Integer (b) });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
  }

  public void write (byte[] b)
  {
    try {
      connection_.callMethod (pxId_, "write",
                               new Class[] { byte[].class },
                               new Object[] { b });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
  }

  public void write (byte[] b, int off, int len)
  {
    try {
      connection_.callMethod (pxId_, "write",
                               new Class[] { byte[].class, Integer.TYPE, Integer.TYPE },
                               new Object[] { b, new Integer(off), new Integer(len) });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
  }


  public void flush ()
    throws IOException
  {
    try {
      connection_.callMethod (pxId_, "flush");
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }


}
