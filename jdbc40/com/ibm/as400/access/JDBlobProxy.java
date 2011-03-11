///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDBlobProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.OutputStream;
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
  private static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";
  
  // Copied from JDError:
  private static final String EXC_FUNCTION_NOT_SUPPORTED       = "IM001";


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


// JDBC 3.0
    public OutputStream setBinaryStream (long pos)
    throws SQLException
    {
        // Avoid dragging in JDError
        //@K1D throw new SQLException (
        //@K1D                       AS400JDBCDriver.getResource("JD" + EXC_FUNCTION_NOT_SUPPORTED),
        //@K1D                       EXC_FUNCTION_NOT_SUPPORTED, -99999);
        try              //@K1A
        {
            JDOutputStreamProxy newStream = new JDOutputStreamProxy ();
            return (JDOutputStreamProxy) connection_.callFactoryMethod (pxId_, "setBinaryStream", 
                                                                        new Class[] { Long.TYPE},
                                                                        new Object[] { new Long(pos)},
                                                                        newStream);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }


// JDBC 3.0
    public int setBytes (long pos, byte[] bytes)
    throws SQLException
    {
        try {
            return connection_.callMethod (pxId_, "setBytes",
                                           new Class[] { Long.TYPE, byte[].class},
                                           new Object[] { new Long(pos), bytes})
            .getReturnValueInt ();
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }


// JDBC 3.0
    public int setBytes (long pos, byte[] bytes, int offset, int len)
    throws SQLException
    {
        try {
            return connection_.callMethod (pxId_, "setBytes",
                                           new Class[] { Long.TYPE, byte[].class, Integer.TYPE, Integer.TYPE},
                                           new Object[] { new Long(pos), bytes, new Integer(offset), new Integer(len)})
            .getReturnValueInt ();
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }


// JDBC 3.0
    public void truncate (long len)
    throws SQLException
    {
        try {
            connection_.callMethod (pxId_, "truncate",
                                                         new Class[] { Long.TYPE},
                                                         new Object[] { new Long(len)});
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }

    
    //@PDA jdbc40
    public synchronized void free() throws SQLException
    {
        try {
            connection_.callMethod (pxId_, "free");
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    //@PDA jdbc40
    public synchronized InputStream getBinaryStream(long pos, long length) throws SQLException
    {
        try {            
            JDInputStreamProxy newStream = new JDInputStreamProxy ();
            return (JDInputStreamProxy) connection_.callFactoryMethod (
                    pxId_, "getBinaryStream",
                    new Class[] { Long.TYPE, Long.TYPE},
                    new Object[] { new Long(pos),  new Long(length)},
                    newStream);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
}
