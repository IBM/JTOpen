///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PrintObjectTransformedInputStreamImplProxy.java
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
 * The PrintObjectTransformedInputStreamImplProxy class implements proxy versions of
 * the public methods defined in the PrintObjectTransformedInputStreamImpl class.
 * Unless commented otherwise, the implementations of the methods below
 * are merely proxy calls to the corresponding method in the remote
 * implementation class (PrintObjectTransformedInputStreamImplRemote).
 **/

class PrintObjectTransformedInputStreamImplProxy extends AbstractProxyImpl
implements PrintObjectTransformedInputStreamImpl, ProxyImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   
    private static final boolean[] myArgs = new boolean[] {true, false, false};

    PrintObjectTransformedInputStreamImplProxy()
    {
        super("PrintObjectTransformedInputStream");
    }



    public void createPrintObjectTransformedInputStream(SpooledFileImpl spooledFile,  // @A1C
                                                        PrintParameterList transformOptions)
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException,
               RequestNotSupportedException
    {
        try {
            connection_.callMethod(pxId_, "createPrintObjectTransformedInputStream",
                                   new Class [] { SpooledFileImpl.class, PrintParameterList.class },  //@A1C
                                   new Object[] { spooledFile, transformOptions });
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
