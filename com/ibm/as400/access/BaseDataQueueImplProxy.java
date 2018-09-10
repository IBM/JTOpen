///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  BaseDataQueueImplProxy.java
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

// Proxy implementation of data queues.
class BaseDataQueueImplProxy extends AbstractProxyImpl implements BaseDataQueueImpl
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    BaseDataQueueImplProxy()
    {
        super("BaseDataQueue");
    }

    // Get the implementation properties to the ProxyServer.
    public void setSystemAndPath(AS400Impl system, String path, String name, String library) throws IOException
    {
        try
        {
            connection_.callMethod(pxId_, "setSystemAndPath", new Class[] { AS400Impl.class, String.class, String.class, String.class }, new Object[] { system, path, name, library });
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow1(e);
        }
    }

    // Proxy implementation of clear, if key is null, do non-keyed clear.
    public void clear(byte[] key) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        try
        {
            connection_.callMethod(pxId_, "clear", new Class[] { byte[].class }, new Object[] { key });
        }
        catch (InvocationTargetException e)
        {
            // Throw an appropriate exception.
            Throwable target = e.getTargetException();
            if (target instanceof IllegalObjectTypeException)
            {
                throw (IllegalObjectTypeException)target;
            }
            throw ProxyClientConnection.rethrow5(e);
        }
    }

    // Proxy implementation of create, keyLength == 0 means non-keyed queue.
    public void create(int maxEntryLength, String authority, boolean saveSenderInformation, boolean FIFO, int keyLength, boolean forceToAuxiliaryStorage, String description) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectAlreadyExistsException, ObjectDoesNotExistException
    {
        try
        {
            connection_.callMethod(pxId_, "create", new Class[] { Integer.TYPE, String.class, Boolean.TYPE, Boolean.TYPE, Integer.TYPE, Boolean.TYPE, String.class }, new Object[] { new Integer(maxEntryLength), authority, new Boolean(saveSenderInformation), new Boolean(FIFO), new Integer(keyLength), new Boolean(forceToAuxiliaryStorage), description });
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow6(e);
        }
    }

    // Proxy implementaion of delete.
    public void delete() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        try
        {
            connection_.callMethod(pxId_, "delete");
        }
        catch (InvocationTargetException e)
        {
            // Throw an appropriate exception.
            throw ProxyClientConnection.rethrow5(e);
        }
    }

    // Proxy implementation of read for data queues.
    public DQReceiveRecord read(String search, int wait, boolean peek, byte[] key) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        try
        {
            return (DQReceiveRecord)connection_.callMethod(pxId_, "read", new Class[] { String.class, Integer.TYPE, Boolean.TYPE, byte[].class }, new Object[] { search, new Integer(wait), new Boolean(peek), key }, true).getReturnValue();
        }
        catch (InvocationTargetException e)
        {
            // Throw an appropriate exception.
            Throwable target = e.getTargetException();
            if (target instanceof IllegalObjectTypeException)
            {
                throw (IllegalObjectTypeException)target;
            }
            throw ProxyClientConnection.rethrow5(e);
        }
    }

    // Proxy implementation for retrieve attributes, keyed is false for non-keyed queues.
    public DQQueryRecord retrieveAttributes(boolean keyed) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        try
        {
            return (DQQueryRecord)connection_.callMethod(pxId_, "retrieveAttributes", new Class[] { Boolean.TYPE }, new Object[] { new Boolean(keyed) }).getReturnValue();
        }
        catch (InvocationTargetException e)
        {
            // Throw an appropriate exception.
            Throwable target = e.getTargetException();
            if (target instanceof IllegalObjectTypeException)
            {
                throw (IllegalObjectTypeException)target;
            }
            throw ProxyClientConnection.rethrow5(e);
        }
    }

    // Proxy implementation for write, key is null for non-keyed queues.
    public void write(byte[] key, byte[] data) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        try
        {
            connection_.callMethod(pxId_, "write", new Class[] { byte[].class, byte[].class }, new Object[] { key, data });
        }
        catch (InvocationTargetException e)
        {
            // Throw an appropriate exception.
            Throwable target = e.getTargetException();
            if (target instanceof IllegalObjectTypeException)
            {
                throw (IllegalObjectTypeException)target;
            }
            throw ProxyClientConnection.rethrow5(e);
        }
    }
}
