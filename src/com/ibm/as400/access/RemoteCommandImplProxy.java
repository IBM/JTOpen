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
    RemoteCommandImplProxy()
    {
        super("RemoteCommand");
    }

    // Get the job info from the proxy server.
    public String getJobInfo(Boolean threadSafety) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        try
        {
            return (String)connection_.callMethod(pxId_, "getJobInfo", new Class[] { Boolean.class }, new Object[] { threadSafety }).getReturnValue ();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow4(e);
        }
    }

    // Report whether the RemoteCommandImpl object is a native object.
    public boolean isNative()
    {
      return false;
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

    // Report whether the command is designated as threadsafe on the system.
    public int getThreadsafeIndicator(String command) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        try
        {
            return connection_.callMethod(pxId_, "getThreadsafeIndicator", new Class[] { String.class }, new Object[] { command }).getReturnValueInt();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow4(e);
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

    // Run the command on the proxy server.
    public boolean runCommand(String command, Boolean threadSafety, int messageCount) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        try
        {
            return connection_.callMethod(pxId_, "runCommand", new Class[] { String.class, Boolean.class, Integer.TYPE }, new Object[] { command, threadSafety, new Integer(messageCount) }, true).getReturnValueBoolean();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow4(e);
        }
    }

    // Run the command on the proxy server.
    public boolean runCommand(byte[] commandAsBytes, String commandAsString) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        try
        {
            return connection_.callMethod(pxId_, "runCommand", new Class[] { byte[].class, String.class }, new Object[] { commandAsBytes, commandAsString }, true).getReturnValueBoolean();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow4(e);
        }
    }

    // Run the command on the proxy server.
    public boolean runCommand(byte[] command, Boolean threadSafety, int messageCount) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        try
        {
            return connection_.callMethod(pxId_, "runCommand", new Class[] { byte[].class, Boolean.class, Integer.TYPE }, new Object[] { command, threadSafety, new Integer(messageCount) }, true).getReturnValueBoolean();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow4(e);
        }
    }

    // Run the program on the proxy server.
    public boolean runProgram(String library, String name, ProgramParameter[] parameterList) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        try
        {
            ProxyReturnValue rv = connection_.callMethod(pxId_, "runProgram", new Class[] { String.class, String.class, ProgramParameter[].class }, new Object[] { library,  name, parameterList }, new boolean[] { false, false, true }, true);
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

    // Run the program on the proxy server.
    public boolean runProgram(String library, String name, ProgramParameter[] parameterList, Boolean threadSafety, int messageCount) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        try
        {
            ProxyReturnValue rv = connection_.callMethod(pxId_, "runProgram", new Class[] { String.class, String.class, ProgramParameter[].class, Boolean.class, Integer.TYPE }, new Object[] { library, name, parameterList, threadSafety, new Integer(messageCount) }, new boolean[] { false, false, true, false, false }, true);
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
    public byte[] runServiceProgram(String library, String name, String procedureName, int returnValueFormat, ProgramParameter[] parameterList, Boolean threadSafety, int procedureNameCCSID, int messageCount) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        try
        {
            ProxyReturnValue rv = connection_.callMethod(pxId_, "runServiceProgram", new Class[] { String.class, String.class, String.class, Integer.TYPE, ProgramParameter[].class, Boolean.class, Integer.TYPE, Integer.TYPE }, new Object[] { library,  name, procedureName, new Integer(returnValueFormat), parameterList, threadSafety, new Integer(procedureNameCCSID), new Integer(messageCount) }, new boolean[] { false, false, false, false, true, false, false, false }, true);
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
