///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  RemoteCommandImplProxy.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1999-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

// The RemoteCommandImplProxy class is an implementation of the CommandCall and ProgramCall classes used on a client communicating with a proxy server.
class RemoteCommandImplProxy extends AbstractProxyImpl implements RemoteCommandImpl
{
    private static final String copyright = "Copyright (C) 1999-2003 International Business Machines Corporation and others.";

    RemoteCommandImplProxy()
    {
        super("RemoteCommand");
    }

    // Get the job info from the proxy server.
    public String getJobInfo(boolean threadSafety) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        try
        {
            return (String)connection_.callMethod(pxId_, "getJobInfo", new Class[] { Boolean.TYPE }, new Object[] { new Boolean(threadSafety) }).getReturnValue ();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow4(e);
        }
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

    // Check command thread safety on the proxy server.
    public boolean isCommandThreadSafe(String command) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        try
        {
            return connection_.callMethod(pxId_, "isCommandThreadSafe", new Class[] { String.class }, new Object[] { command }).getReturnValueBoolean();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow4(e);
        }
    }

    // Run the command on the proxy server.
    public boolean runCommand(String command, boolean threadSafety, int messageCount) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        try
        {
            return connection_.callMethod(pxId_, "runCommand", new Class[] { String.class, Boolean.TYPE, Integer.TYPE }, new Object[] { command, new Boolean(threadSafety), new Integer(messageCount) }, true).getReturnValueBoolean();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow4(e);
        }
    }

    // Run the command on the proxy server.
    public boolean runCommand(byte[] command, boolean threadSafety, int messageCount) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        try
        {
            return connection_.callMethod(pxId_, "runCommand", new Class[] { byte[].class, Boolean.TYPE, Integer.TYPE }, new Object[] { command, new Boolean(threadSafety), new Integer(messageCount) }, true).getReturnValueBoolean();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow4(e);
        }
    }

    // Run the program on the proxy server.
    public boolean runProgram(String library, String name, ProgramParameter[] parameterList, boolean threadSafety, int messageCount) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        try
        {
            ProxyReturnValue rv = connection_.callMethod(pxId_, "runProgram", new Class[] { String.class, String.class, ProgramParameter[].class, Boolean.TYPE, Integer.TYPE }, new Object[] { library,  name, parameterList, new Boolean(threadSafety), new Integer(messageCount) }, new boolean[] { false, false, true, false, false }, true);
            ProgramParameter[] returnParmL = (ProgramParameter[])rv.getArgument(2);
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
    public byte[] runServiceProgram(String library, String name, String procedureName, int returnValueFormat, ProgramParameter[] parameterList, boolean threadSafety, int procedureNameCCSID, int messageCount) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        try
        {
            ProxyReturnValue rv = connection_.callMethod(pxId_, "runServiceProgram", new Class[] { String.class, String.class, String.class, Integer.TYPE, ProgramParameter[].class, Boolean.TYPE, Integer.TYPE, Integer.TYPE }, new Object[] { library,  name, procedureName, new Integer(returnValueFormat), parameterList, new Boolean(threadSafety), new Integer(procedureNameCCSID), new Integer(messageCount) }, new boolean[] { false, false, false, false, true, false, false, false }, true);
            ProgramParameter[] returnParmL = (ProgramParameter[])rv.getArgument(4);
            for (int i = 0; i < parameterList.length; ++i)
            {
                parameterList[i].setOutputData(returnParmL[i].getOutputData());
            }
            return (byte[])rv.getReturnValue();
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
