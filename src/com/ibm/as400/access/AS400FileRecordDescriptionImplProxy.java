///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400FileRecordDescriptionImplProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.InvocationTargetException;
import java.io.Serializable;
import java.io.IOException;

class AS400FileRecordDescriptionImplProxy extends AbstractProxyImpl
  implements AS400FileRecordDescriptionImpl,
             Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;


  public AS400FileRecordDescriptionImplProxy()
  {
    super("AS400FileRecordDescription");
  }
  
                                           
  public String[] createRecordFormatSource(String packageName)
    throws AS400Exception,
           AS400SecurityException,
           IOException,
           InterruptedException
  {
    try
    {
      return (String[])connection_.callMethod(pxId_,
                                   "createRecordFormatSource",
                                   new Class[] { String.class },
                                   new Object[] { packageName }).getReturnValue();
    }
    catch(InvocationTargetException e)
    {
      throw ProxyClientConnection.rethrow4a(e);
    }
  }


  public RecordFormat[] retrieveRecordFormat()
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    try
    {
      return (RecordFormat[])connection_.callMethod(pxId_, "retrieveRecordFormat").getReturnValue();
    }
    catch(InvocationTargetException e)
    {
      throw ProxyClientConnection.rethrow4a(e);
    }
  }


  public void setPath(String name)
  {
    try
    {
      connection_.callMethod(pxId_, "setPath",
                             new Class[] { String.class },
                             new Object[] { name });
    }
    catch(InvocationTargetException e)
    {
      throw ProxyClientConnection.rethrow(e);
    }
  }


  public void setSystem(AS400Impl system) //@B5C
  {
    try
    {
      connection_.callMethod(pxId_, "setSystem",
                             new Class[] { AS400Impl.class }, //@B5C
                             new Object[] { system });
    }
    catch(InvocationTargetException e)
    {
      throw ProxyClientConnection.rethrow(e);
    }
  }
}
