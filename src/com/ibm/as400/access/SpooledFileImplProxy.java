///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SpooledFileImplProxy.java
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
 * The SpooledFileImplProxy class implements proxy versions of
 * the public methods defined in the SpooledFileImpl class.
 * Unless commented otherwise, the implementations of the methods below
 * are merely proxy calls to the corresponding method in the remote
 * implementation class (SpooledFileImplRemote).
 **/

class SpooledFileImplProxy extends PrintObjectImplProxy
implements SpooledFileImpl, ProxyImpl
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";
    
    SpooledFileImplProxy()
    {
        super("SpooledFile");
    }

    public void answerMessage(String reply)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        try {
            connection_.callMethod(pxId_, "answerMessage",
                                   new Class[] { String.class },
                                   new Object[] { reply });
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow6a(e);
        }
    }



    public void delete()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException
    {
        try {
            connection_.callMethod(pxId_, "delete");
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow4(e);
        }
    }




    // Added method for synchronization with base class.
    public boolean getFMsgRetrieved()
    {
        try {
            return connection_.callMethod(pxId_, "getFMsgRetrieved").getReturnValueBoolean();
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }



    public AS400Message getMessage()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException
    {
        try {
            return (AS400Message) connection_.callMethod(pxId_, "getMessage").getReturnValue();
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow4(e);
        }
    }



    public void hold(String holdType)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        try {
            connection_.callMethod(pxId_, "hold",
                                   new Class[] { String.class },
                                   new Object[] { holdType });
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow6a(e);
        }
    }



    public void move(SpooledFileImpl targetSpooledFile)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        try {
            connection_.callMethod(pxId_, "move",
                                   new Class[] { SpooledFileImpl.class },
                                   new Object[] { targetSpooledFile });
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow6a(e);
        }
    }



    public void move(OutputQueueImpl targetOutputQueue)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        try {
            connection_.callMethod(pxId_, "move",
                                   new Class[] { OutputQueueImpl.class },
                                   new Object[] { targetOutputQueue });
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow6a(e);
        }
    }



    public void moveToTop()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        try {
            connection_.callMethod(pxId_, "moveToTop");
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow6a(e);
        }
    }



    public void release()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        try {
            connection_.callMethod(pxId_, "release");
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow6a(e);
        }
    }



    public void sendNet(PrintParameterList sendOptions)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException
    {
        try {
            connection_.callMethod(pxId_, "sendNet",
                                   new Class[] { PrintParameterList.class },
                                   new Object[] { sendOptions });
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow4(e);
        }
    }



    public void sendTCP(PrintParameterList sendOptions)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException
    {
        try {
            connection_.callMethod(pxId_, "sendTCP",
                                   new Class[] { PrintParameterList.class },
                                   new Object[] { sendOptions });
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow4(e);
        }
    }



    public void setAttributes(PrintParameterList attributes)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        try {
            connection_.callMethod(pxId_, "setAttributes",
                                   new Class[] { PrintParameterList.class },
                                   new Object[] { attributes });
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow6a(e);
        }
    }
    
    
    
    public NPCPIDSplF copy(OutputQueueImpl outputQueue)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        try {
            NPCPIDSplF cpID = (NPCPIDSplF) connection_.callMethod(pxId_, "copy",
                                   new Class[] { OutputQueueImpl.class },
                                   new Object[] { outputQueue }).getReturnValue();
            return cpID;         
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow6a(e);
        }
    }    
    
}

