///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileDescriptorImplProxy.java
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
 Provides a local proxy implementation for the IFSFileDescriptor class.
 **/
class IFSFileDescriptorImplProxy
extends AbstractProxyImpl
implements IFSFileDescriptorImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  

  IFSFileDescriptorImplProxy ()
  {
    super ("IFSFileDescriptor");
  }

  public void close()
  {
    try {
      connection_.callMethod (pxId_, "close");
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
  }

  public int getFileOffset()
  {
    try {
      return connection_.callMethod (pxId_, "getFileOffset")
                        .getReturnValueInt();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
  }

  public void incrementFileOffset(int fileOffsetIncrement)
  {
    try {
      connection_.callMethod (pxId_, "incrementFileOffset",
                              new Class[] { Integer.TYPE },
                              new Object[] { new Integer(fileOffsetIncrement) });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
  }

  public void initialize(int fileOffset, Object parentImpl, String path, int shareOption,
                         AS400Impl system)
  {
    try {
      connection_.callMethod (pxId_, "initialize",
                              new Class[] { Integer.TYPE,
                                            Object.class,
                                            String.class,
                                            Integer.TYPE,
                                            AS400Impl.class
                                          },
                              new Object[] { new Integer(fileOffset),
                                            parentImpl,
                                             path,
                                             new Integer(shareOption),
                                             system
                                           });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
  }

  public boolean isOpen()
  {
    try {
      return connection_.callMethod (pxId_, "isOpen")
                        .getReturnValueBoolean();
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
  }

  public void setFileOffset(int fileOffset)
  {
    try {
      connection_.callMethod (pxId_, "setFileOffset",
                              new Class[] { Integer.TYPE },
                              new Object[] { new Integer(fileOffset) });
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow (e);
    }
  }

  public void sync() throws IOException
  {
    try {
      connection_.callMethod (pxId_, "sync");
    }
    catch (InvocationTargetException e) {
      throw ProxyClientConnection.rethrow1 (e);
    }
  }

}




