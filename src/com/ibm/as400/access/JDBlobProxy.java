///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDBlobProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Blob;
import java.sql.SQLException;



// JDBC 2.0
/**
The JDBlobProxy class provides access to binary large
objects.  The data is valid only within the current
transaction.
**/
class JDBlobProxy
extends AbstractProxyImpl
implements Blob
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




  public InputStream getBinaryStream ()
    throws SQLException
  {
    try {
      JDInputStreamProxy newStream = new JDInputStreamProxy ();
      return (JDInputStreamProxy) connection_.callFactoryMethod (
                                   pxId_, "getBinaryStream", newStream);
    }
    catch (InvocationTargetException e) {
      throw JDConnectionProxy.rethrow1 (e);
    }
  }

  public byte[] getBytes (long start, int length)
    throws SQLException
  {
    try {
      return (byte[]) connection_.callMethod (pxId_, "getBytes",
                               new Class[] { Long.TYPE, Integer.TYPE },
                               new Object[] { new Long (start),
                                              new Integer (length) })
                         .getReturnValue();
    }
    catch (InvocationTargetException e) {
      throw JDConnectionProxy.rethrow1 (e);
    }
  }

  public long length ()
    throws SQLException
  {
    try {
      return connection_.callMethod (pxId_, "length")
                             .getReturnValueLong ();
    }
    catch (InvocationTargetException e) {
      throw JDConnectionProxy.rethrow1 (e);
    }
  }

  public long position (byte[] pattern, long start)
    throws SQLException
  {
    try {
      return connection_.callMethod (pxId_, "position",
                               new Class[] { byte[].class, Long.TYPE },
                               new Object[] { pattern, new Long (start) })
                             .getReturnValueLong ();
    }
    catch (InvocationTargetException e) {
      throw JDConnectionProxy.rethrow1 (e);
    }
  }


  public long position (Blob pattern, long start)
    throws SQLException
  {
    try {
      return connection_.callMethod (pxId_, "position",
                               new Class[] { Blob.class, Long.TYPE },
                               new Object[] { pattern, new Long (start) })
                             .getReturnValueLong ();
    }
    catch (InvocationTargetException e) {
      throw JDConnectionProxy.rethrow1 (e);
    }
  }



}
