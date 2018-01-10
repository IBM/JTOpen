///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDInputStreamProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;



// JDBC 2.0
/**
<p>The JDInputStreamProxy class provides access to binary data
using an input stream.  The data is valid only within the current
transaction.
**/
class JDInputStreamProxy
extends InputStream
implements ProxyFactoryImpl
{
  static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


  // Private data.
  
  private long                    pxId_;
  private ProxyClientConnection   connection_;



  public int available ()
    throws IOException
  {
    try {
      return connection_.callMethodReturnsInt (pxId_, "available");
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

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


  public void mark (int readLimit)
  {
    try {
      connection_.callMethod (pxId_, "mark",
                               new Class[] { Integer.TYPE },
                               new Object[] { new Integer (readLimit) });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
  }


  public boolean markSupported ()
  {
    try {
      return connection_.callMethodReturnsBoolean (pxId_, "markSupported");
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
  }


  public int read ()
    throws IOException
  {
    try {
      return connection_.callMethodReturnsInt (pxId_, "read");
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }


  public int read (byte[] data)
    throws IOException
  {
    return read (data, 0, data.length);
  }


  private static final boolean[] ARGS_TO_RETURN = new boolean[] {true, false, false};

  public int read (byte[] data, int start, int length)
    throws IOException
  {
    try {
      ProxyReturnValue rv = connection_.callMethod (pxId_, "read",
                                   new Class[] { byte[].class,
                                                 Integer.TYPE,
                                                 Integer.TYPE },
                                   new Object[] { data,
                                                  new Integer (start),
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


  public void reset ()
    throws IOException
  {
    try {
      connection_.callMethod (pxId_, "reset");
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }


  public long skip (long length)
    throws IOException
  {
    try {
      return connection_.callMethod (pxId_, "skip",
                     new Class[] { Long.TYPE },
                     new Object[] { new Long (length) })
               .getReturnValueLong ();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }


}
