///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PrintObjectInputStreamImplProxy.java
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

/**
 * The PrintObjectInputStreamImplProxy class implements proxy versions of
 * the public methods defined in the PrintObjectInputStreamImpl class.
 * Unless commented otherwise, the implementations of the methods below
 * are merely proxy calls to the corresponding method in the remote
 * implementation class (PrintObjectInputStreamImplRemote).
 **/

class PrintObjectInputStreamImplProxy extends AbstractProxyImpl
implements PrintObjectInputStreamImpl, ProxyImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   
    // myArgs needed for ProxyCall...see ProxyClientConnection
    private static final boolean[] myArgs = new boolean[] {true, false, false};  

    PrintObjectInputStreamImplProxy()
    {
        super("PrintObjectInputStream"); 
    }



    public void createPrintObjectInputStream(SpooledFileImpl sf,  
                                             PrintParameterList openOptions)
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException,
               RequestNotSupportedException
    {
        try {
            connection_.callMethod(pxId_, "createPrintObjectInputStream",
                                   new Class [] { SpooledFileImpl.class, PrintParameterList.class },
                                   new Object[] { sf, openOptions });
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow6a(e);
        }
    }



    public void createPrintObjectInputStream(PrintObjectImpl resource,
                                             PrintParameterList openOptions)
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException,
               RequestNotSupportedException
    {
        try {
            connection_.callMethod(pxId_, "createPrintObjectInputStream",
                                   new Class [] { PrintObjectImpl.class, PrintParameterList.class },
                                   new Object[] { resource, openOptions });
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow6a(e);
        }
    }



    public int available()
        throws IOException
    {
        try {
            return connection_.callMethod(pxId_, "available").getReturnValueInt();
        }
        catch (InvocationTargetException e) {
            Throwable error = e.getTargetException();
            if (error instanceof IOException)
                throw (IOException) error;
            else
                throw ProxyClientConnection.rethrow(e);
        }
    }



    public void close()
       throws IOException
    {
        try {
            connection_.callMethod(pxId_, "close");
        }
        catch (InvocationTargetException e) {
            Throwable error = e.getTargetException();
            if (error instanceof IOException)
                throw (IOException) error;
            else
                throw ProxyClientConnection.rethrow(e);
        }
    }



    public void mark(int readLimit)
    {
        try {
            connection_.callMethod(pxId_, "mark",
                                   new Class[] {Integer.TYPE},
                                   new Object[] { new Integer (readLimit) });
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }



    public int read(byte data[], int dataOffset, int length)
        throws IOException
    {
        try {
            ProxyReturnValue rv = connection_.callMethod(pxId_, "read",
                                     new Class[] { byte[].class , Integer.TYPE, Integer.TYPE },
                                     new Object[] { data, new Integer(dataOffset), new Integer(length) },
                                     myArgs, false);

            byte [] returnDataBuffer = (byte[])rv.getArgument(0);
            for (int i=0; i<data.length; i++) {
                data[i] = returnDataBuffer[i];
            }
            return rv.getReturnValueInt();
        }
        catch (InvocationTargetException e) {
            Throwable error = e.getTargetException();
            if (error instanceof IOException)
                throw (IOException) error;
            else
                throw ProxyClientConnection.rethrow(e);
        }
    }



    public void reset()
        throws IOException
    {
        try {
            connection_.callMethod(pxId_, "reset");
        }
        catch (InvocationTargetException e) {
            Throwable error = e.getTargetException();
            if (error instanceof IOException)
                throw (IOException) error;
            else
                throw ProxyClientConnection.rethrow(e);
        }
    }



    public long skip(long bytesToSkip)
        throws IOException
    {
        try {
            return connection_.callMethod(pxId_, "skip",
                                          new Class[] { Long.TYPE },
                                          new Object[] { new Long(bytesToSkip) }).getReturnValueLong();
        }
        catch (InvocationTargetException e) {
            Throwable error = e.getTargetException();
            if (error instanceof IOException)
                throw (IOException) error;
            else
                throw ProxyClientConnection.rethrow(e);
        }
    }
}
