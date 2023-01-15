///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  UserSpaceImplProxy.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

// The UserSpaceImplProxy class is an implementation of the UserSpace class used on a client communicating with a proxy server.
class UserSpaceImplProxy extends AbstractProxyImpl implements UserSpaceImpl
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    UserSpaceImplProxy()
    {
        super("UserSpace");
    }

    public void close() throws IOException
    {
        try
        {
            connection_.callMethod(pxId_, "close");
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow1(e);
        }
    }

    public void create(byte[] domainBytes, int length, boolean replace, String extendedAttribute, byte initialValue, String textDescription, String authority) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        try
        {
            connection_.callMethod(pxId_, "create", new Class[] { byte[].class, Integer.TYPE, Boolean.TYPE, String.class, Byte.TYPE, String.class, String.class }, new Object[] { domainBytes, new Integer(length), new Boolean(replace), extendedAttribute, new Byte(initialValue), textDescription, authority } );
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow5(e);
        }
    }

    public void delete() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        try
        {
            connection_.callMethod(pxId_, "delete");
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow5(e);
        }
    }

    public byte getInitialValue() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        try
        {
            return (byte)connection_.callMethod(pxId_, "getInitialValue").getReturnValueByte();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow5(e);
        }
    }

    public int getLength() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        try
        {
            return connection_.callMethod(pxId_, "getLength").getReturnValueInt();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow5(e);
        }
    }

    public boolean isAutoExtendible() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        try
        {
            return connection_.callMethod(pxId_, "isAutoExtendible").getReturnValueBoolean();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow5(e);
        }
    }

    public int read(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        try
        {
            ProxyReturnValue rv = connection_.callMethod(pxId_, "read", new Class[] { byte[].class, Integer.TYPE, Integer.TYPE, Integer.TYPE }, new Object[] { dataBuffer, new Integer(userSpaceOffset), new Integer(dataOffset), new Integer(length) }, new boolean[] { true, false, false, false }, false);
            byte [] returnDataB = (byte[])rv.getArgument(0);
            for (int i = 0; i < dataBuffer.length; ++i)
            {
                dataBuffer[i] = returnDataB[i];
            }
            return rv.getReturnValueInt();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow5(e);
        }
    }

    public void setAutoExtendible(boolean autoExtendibility) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        try
        {
            connection_.callMethod(pxId_, "setAutoExtendible", new Class[] { Boolean.TYPE }, new Object[] { new Boolean(autoExtendibility) } );
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow5(e);
        }
    }

    public void setInitialValue(byte initialValue) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        try
        {
            connection_.callMethod(pxId_, "setInitialValue", new Class[] { Byte.TYPE }, new Object[] { new Byte(initialValue) } );
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow5(e);
        }
    }

    public void setLength(int length) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        try
        {
            connection_.callMethod(pxId_, "setLength", new Class[] { Integer.TYPE }, new Object[] { new Integer(length) } );
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow5(e);
        }
    }

    public void setProperties(AS400Impl system, String path, String name, String library, boolean mustUseProgramCall, boolean mustUseSockets)
    {
        try
        {
            connection_.callMethod(pxId_, "setProperties", new Class[] { AS400Impl.class, String.class, String.class, String.class, Boolean.TYPE, Boolean.TYPE }, new Object[] { system, path, name, library, new Boolean(mustUseProgramCall), new Boolean(mustUseSockets) } );
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    public void write(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length, int forceAuxiliary) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        try
        {
            connection_.callMethod(pxId_, "write", new Class[] { byte[].class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE }, new Object[] { dataBuffer, new Integer(userSpaceOffset), new Integer(dataOffset), new Integer(length), new Integer(forceAuxiliary) } );
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow5(e);
        }
    }
}
