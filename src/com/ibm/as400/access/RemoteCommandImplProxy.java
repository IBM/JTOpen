///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: RemoteCommandImplProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

// The RemoteCommandImplProxy class is an implementation of the CommandCall and ProgramCall classes used on a client communicating with a proxy server.
class RemoteCommandImplProxy extends AbstractProxyImpl implements RemoteCommandImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    public RemoteCommandImplProxy()
    {
        super("RemoteCommand");
    }

    // Get the message list from the proxy server.
    public AS400Message[] getMessageList()
    {
        try
        {
            return (AS400Message[])connection_.callMethod(pxId_, "getMessageList").getReturnValue();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    // Run the command on the proxy server.
    public boolean runCommand(String command) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        try
        {
            return connection_.callMethod(pxId_, "runCommand", new Class[] { String.class }, new Object[] { command }, true).getReturnValueBoolean();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow4(e);
        }
    }

    // Run the program on the proxy server.
    public boolean runProgram(String program, ProgramParameter[] parameterList) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        try
        {
            ProxyReturnValue rv = connection_.callMethod(pxId_, "runProgram", new Class[] { String.class, ProgramParameter[].class }, new Object[] { program, parameterList }, new boolean[] { false, true }, true);
            ProgramParameter[] returnParmL = (ProgramParameter[])rv.getArgument(1);
            for (int i = 0; i < parameterList.length; ++i)
            {
                parameterList[i].setOutputData(returnParmL[i].getOutputData());
            }
            return rv.getReturnValueBoolean();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow5(e);
        }
    }

    // Run the service program on the proxy server.
    public Object[] runServiceProgram(String program, String procedureName, int returnValueFormat, ProgramParameter[] parmList) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        try
        {
            ProxyReturnValue rv = connection_.callMethod(pxId_, "runServiceProgram", new Class[] { String.class, String.class, Integer.TYPE, ProgramParameter[].class }, new Object[] { program, procedureName, new Integer(returnValueFormat), parmList }, new boolean[] { false, false, false, true }, true);
            ProgramParameter[] returnParmL = (ProgramParameter[])rv.getArgument(3);
            for (int i = 0; i < parmList.length; ++i)
            {
                parmList[i].setOutputData(returnParmL[i].getOutputData());
            }
            return (Object[])rv.getReturnValue();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow5(e);
        }
    }

    // Set the AS400Impl into the proxy server.
    public void setSystem(AS400Impl system) throws IOException
    {
        try
        {
            connection_.callMethod(pxId_, "setSystem", new Class[] { AS400Impl.class }, new Object[] { system });
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow1(e);
        }
    }
}
