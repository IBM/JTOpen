///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SpooledFileOutputStreamImplProxy.java
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
 * The SpooledFileOutputStreamImplProxy class implements proxy versions of
 * the public methods defined in the SpooledFileOutputStreamImpl class.
 * Unless commented otherwise, the implementations of the methods below
 * are merely proxy calls to the corresponding method in the remote
 * implementation class (SpooledFileOutputStreamImplRemote).
 **/

class SpooledFileOutputStreamImplProxy extends AbstractProxyImpl
implements SpooledFileOutputStreamImpl, ProxyImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    
    
    
    SpooledFileOutputStreamImplProxy()
    {
        super("SpooledFileOutputStream");
    }


    public void createSpooledFileOutputStream(AS400Impl system,  //@A1C
                                         PrintParameterList options,
                                        PrinterFileImpl printerFile,  //@A1C
                                        OutputQueueImpl outputQueue)  //@A1C
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException
    {
        try {
            // @A1C - Changed classes to XXXImpl...
            connection_.callMethod(pxId_, "createSpooledFileOutputStream",
                                   new Class [] { AS400Impl.class, PrintParameterList.class,
                                                       PrinterFileImpl.class, OutputQueueImpl.class }, 
                                   new Object[] { system, options, printerFile, outputQueue });
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow4(e);
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



    public void flush()
        throws IOException
   {
        try {
            connection_.callMethod(pxId_, "flush");
        }
        catch (InvocationTargetException e) {
            Throwable error = e.getTargetException();
            if (error instanceof IOException)
                throw (IOException) error;
            else
                throw ProxyClientConnection.rethrow(e);
        }
    }



    public /* synchronized */ NPCPIDSplF getSpooledFile()
       throws IOException
    {
        try {
            return (NPCPIDSplF) connection_.callMethod(pxId_, "getSpooledFile").getReturnValue();
        }
        catch (InvocationTargetException e) {
            Throwable error = e.getTargetException();
            if (error instanceof IOException)
                throw (IOException) error;
            else
                throw ProxyClientConnection.rethrow(e);
        }
    }



    public /* synchronized */ void write(byte data[], int offset, int length)
        throws IOException
    {
        try {
            connection_.callMethod(pxId_, "write",
                                   new Class[] { byte[].class , Integer.TYPE, Integer.TYPE },
                                   new Object[] { data, new Integer(offset), new Integer(length) });
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
