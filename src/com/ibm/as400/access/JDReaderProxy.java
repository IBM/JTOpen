///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JDReaderProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Reader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;



/**
<p>The JDReaderProxy class provides access to Reader objects
which are returned from Toolbox method calls.
**/
class JDReaderProxy
extends Reader
implements ProxyFactoryImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  // Private data.

  private long                    pxId_;
  private ProxyClientConnection   connection_;


  public void close() throws IOException
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

  public void mark(int readAheadLimit) throws IOException
  {
    try {
      connection_.callMethod (pxId_, "mark",
                             new Class[] { Integer.TYPE },
                             new Object[] { new Integer (readAheadLimit) });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  public boolean markSupported()
  {
    try {
      return connection_.callMethodReturnsBoolean (pxId_, "markSupported");
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
  }


  public int read() throws IOException
  {
    try {
      return connection_.callMethodReturnsInt (pxId_, "read");
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  public int read(char cbuf[]) throws IOException
  {
    return read (cbuf, 0, cbuf.length);
  }

  private static final boolean[] ARGS_TO_RETURN = new boolean[] {true, false, false};

  public int read(char cbuf[],
                  int off,
                  int len) throws IOException
  {
    try {
      ProxyReturnValue rv = connection_.callMethod (pxId_, "read",
                            new Class[] { char[].class,
                                          Integer.TYPE,
                                          Integer.TYPE },
                            new Object[] { cbuf,
                                           new Integer (off),
                                           new Integer (len) },
                            ARGS_TO_RETURN, false );
      char[] returnDataBuffer = (char[])rv.getArgument(0);
      for (int i=0; i<cbuf.length; i++) {
        cbuf[i] = returnDataBuffer[i];
      }
      return rv.getReturnValueInt();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  public boolean ready() throws IOException
  {
    try {
      return connection_.callMethodReturnsBoolean (pxId_, "ready");
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  public void reset() throws IOException
  {
    try {
      connection_.callMethod (pxId_, "reset");
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

  public long skip(long n) throws IOException
  {
    try {
      return connection_.callMethod (pxId_, "skip",
                     new Class[] { Long.TYPE },
                     new Object[] { new Long (n) })
               .getReturnValueLong ();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }


}
