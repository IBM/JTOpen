///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrintObjectPageInputStreamImplProxy.java
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
 * The PrintObjectPageInputStreamImplProxy class implements proxy versions of
 * the public methods defined in the PrintObjectPageInputStreamImpl class.
 * Unless commented otherwise, the implementations of the methods below
 * are merely proxy calls to the corresponding method in the remote
 * implementation class (PrintObjectPageInputStreamImplRemote).
 **/

class PrintObjectPageInputStreamImplProxy extends AbstractProxyImpl
implements PrintObjectPageInputStreamImpl, ProxyImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   
    private static final boolean[] myArgs = new boolean[] {true, false, false};
    
    PrintObjectPageInputStreamImplProxy()
    {
        super("PrintObjectPageInputStream");
    }



    public void createPrintObjectPageInputStream(SpooledFileImpl sf,  // @A1C
                                                 PrintParameterList openOptions)
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException,
               RequestNotSupportedException
    {
        try {
            connection_.callMethod(pxId_, "createPrintObjectPageInputStream",
                                   new Class [] { SpooledFileImpl.class, PrintParameterList.class }, // @A1C
                                   new Object[] { sf, openOptions });
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



    public int getCurrentPageNumber()
    {
        try {
            return connection_.callMethod(pxId_, "getCurrentPageNumber").getReturnValueInt();
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }



    public int getNumberOfPages()
    {
        try {
            return connection_.callMethod(pxId_, "getNumberOfPages").getReturnValueInt();
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }



    public boolean isPagesEstimated()
    {
        try {
            return connection_.callMethod(pxId_, "isPagesEstimated").getReturnValueBoolean();
        }
        catch (InvocationTargetException e) {
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



    public boolean nextPage()
        throws IOException
    {
        try {
            return connection_.callMethod(pxId_, "nextPage").getReturnValueBoolean();
        }
        catch (InvocationTargetException e) {
            Throwable error = e.getTargetException();
            if (error instanceof IOException)
                throw (IOException) error;
            else
                throw ProxyClientConnection.rethrow(e);
        }
    }



    public boolean previousPage()
        throws IOException
    {
        try {
            return connection_.callMethod(pxId_, "previousPage").getReturnValueBoolean();
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



    public boolean selectPage(int page)
        throws IOException, IllegalArgumentException
    {
        try {
            return (boolean) connection_.callMethod(pxId_, "selectPage",
                                                    new Class[] { Integer.TYPE },
                                                    new Object[] { new Integer(page) }).getReturnValueBoolean();
        }
        catch (InvocationTargetException e) {
            Throwable error = e.getTargetException();
            if (error instanceof IOException)
                throw (IOException) error;
            else if (error instanceof IllegalArgumentException)
                throw (IllegalArgumentException) error;
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
