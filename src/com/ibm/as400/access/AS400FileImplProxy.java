///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400FileImplProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.InvocationTargetException;
import java.io.IOException;

class AS400FileImplProxy extends AbstractProxyImpl implements AS400FileImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  public AS400FileImplProxy()
  {
    super("AS400File");
  }
  
  
  public void doIt(String methodName, Class[] classes, Object[] objects)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    try
    {
      connection_.callMethod(pxId_, methodName, classes, objects);
    }      
    catch(InvocationTargetException e)
    {
      throw ProxyClientConnection.rethrow4a(e);
    }
  }


  public boolean doItBoolean(String methodName)
  {
    try
    {
      return connection_.callMethod(pxId_, methodName).getReturnValueBoolean();
    }      
    catch(InvocationTargetException e)
    {
      throw ProxyClientConnection.rethrow(e);
    }
  }


  public int doItInt(String methodName)
  {
    try
    {
      return connection_.callMethod(pxId_, methodName).getReturnValueInt();
    }      
    catch(InvocationTargetException e)
    {
      throw ProxyClientConnection.rethrow(e);
    }
  }


  public void doItNoExceptions(String methodName, Class[] classes, Object[] objects)
  {
    try
    {
      connection_.callMethod(pxId_, methodName, classes, objects);
    }      
    catch(InvocationTargetException e)
    {
      throw ProxyClientConnection.rethrow(e);
    }
  }


  public Record doItRecord(String methodName, Class[] classes, Object[] objects)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    try
    {
      return (Record)connection_.callMethod(pxId_, methodName, classes, objects).getReturnValue();
    }      
    catch(InvocationTargetException e)
    {
      throw ProxyClientConnection.rethrow4a(e);
    }
  }


  public Record[] doItRecordArray(String methodName, Class[] classes, Object[] objects)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    try
    {
      return (Record[])connection_.callMethod(pxId_, methodName, classes, objects).getReturnValue();
    }      
    catch(InvocationTargetException e)
    {
      throw ProxyClientConnection.rethrow4a(e);
    }
  }

  //@B2A
  public RecordFormat doItRecordFormat(String methodName, Class[] classes, Object[] objects)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    try
    {
      return (RecordFormat)connection_.callMethod(pxId_, methodName, classes, objects).getReturnValue();
    }      
    catch(InvocationTargetException e)
    {
      throw ProxyClientConnection.rethrow4a(e);
    }
  }


  public int[] getExplicitLocks()
  {
    try
    {
      return (int[])connection_.callMethod(pxId_, "getExplicitLocks").getReturnValue();
    }
    catch (InvocationTargetException e)
    {
      throw ProxyClientConnection.rethrow(e);
    }
  }


  public String[] openFile2(int openType, int bf, int level, boolean access)
    throws AS400Exception, AS400SecurityException, InterruptedException, IOException
  {
    try
    {
      return (String[])connection_.callMethod(pxId_, "openFile2",
                                                    new Class[] { Integer.TYPE, Integer.TYPE, Integer.TYPE, Boolean.TYPE },
                                                    new Object[] { new Integer(openType), new Integer(bf), new Integer(level), new Boolean(access) }).getReturnValue();
    }      
    catch(InvocationTargetException e)
    {
      throw ProxyClientConnection.rethrow4a(e);
    }
  }
}
