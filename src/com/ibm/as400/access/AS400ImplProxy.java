///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: AS400ImplProxy.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import com.ibm.as400.security.auth.ProfileTokenCredential;

// AS400ImplProxy forwards implementation methods from proxy client to proxy server.
class AS400ImplProxy extends AbstractProxyImpl implements AS400Impl
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
    // Tell super what type of impl we are.
    AS400ImplProxy()
    {
        super("AS400");
    }

    // Tell proxy server to listen for connection events.
    public void addConnectionListener(ConnectionListener listener)
    {
        connection_.addListener(pxId_, listener, "Connection");
    }

    // Map from CCSID to encoding string.
    public String ccsidToEncoding(int ccsid)
    {
        try
        {
            return (String)connection_.callMethod(pxId_, "ccsidToEncoding", new Class[] { Integer.TYPE }, new Object[] { new Integer(ccsid) }).getReturnValue();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    // Change password.
    public SignonInfo changePassword(String systemName, String userId, byte[] oldBytes, byte[] newBytes) throws AS400SecurityException, IOException
    {
        try
        {
            return (SignonInfo)connection_.callMethod(pxId_, "changePassword", new Class[] { String.class, String.class, byte[].class, byte[].class }, new Object[] { systemName, userId, oldBytes, newBytes }).getReturnValue();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow2(e);
        }
    }

    // Connect to service.
    public void connect(int service) throws AS400SecurityException, IOException
    {
        try
        {
            connection_.callMethod(pxId_, "connect", new Class[] { Integer.TYPE }, new Object[] { new Integer(service) });
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow2(e);
        }
    }

    // Disconnect from service.
    public void disconnect(int service)
    {
        try
        {
            connection_.callMethod(pxId_, "disconnect", new Class[] { Integer.TYPE }, new Object[] { new Integer(service) });
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    // Exchange seeds with proxy server.
    public byte[] exchangeSeed(byte[] proxySeed)
    {
        try
        {
            return (byte[])connection_.callMethod(pxId_, "exchangeSeed", new Class[] { byte[].class }, new Object[] { proxySeed }).getReturnValue();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    // Sets the raw bytes for the provided profile token.
    public void generateProfileToken(ProfileTokenCredential profileToken, String userIdentity) throws AS400SecurityException, IOException
    {
        try
        {
            connection_.callMethod(pxId_, "generateProfileToken", new Class[] { ProfileTokenCredential.class, String.class }, new Object[] { profileToken, userIdentity });
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow2(e);
        }
    }

    // Sets the raw bytes for the provided profile token.
    public void generateProfileToken(ProfileTokenCredential profileToken, String userId, byte[] bytes, int byteType) throws AS400SecurityException, IOException, InterruptedException
    {
        try
        {
            connection_.callMethod(pxId_, "generateProfileToken", new Class[] { ProfileTokenCredential.class, String.class, byte[].class, Integer.TYPE }, new Object[] { profileToken, userId, bytes, new Integer(byteType) });
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow3(e);
        }
    }

    // Get the jobs with which we are connected.
    public String[] getJobs(int service)
    {
        try
        {
            return (String[])connection_.callMethod(pxId_, "getJobs", new Class[] { Integer.TYPE }, new Object[] { new Integer(service) }).getReturnValue();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    // Get the port number for a service.
    public int getServicePort(String systemName, int service)
    {
        try
        {
            return connection_.callMethod(pxId_, "getServicePort", new Class[] { String.class, Integer.TYPE }, new Object[] { systemName, new Integer(service) }).getReturnValueInt();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    // Check service connection.
    public boolean isConnected(int service)
    {
        try
        {
            return connection_.callMethod(pxId_, "isConnected", new Class[] { Integer.TYPE }, new Object[] { new Integer(service) }).getReturnValueBoolean();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    // Load converter into converter pool.
    public void newConverter(int ccsid) throws UnsupportedEncodingException
    {
        try
        {
            connection_.callMethod(pxId_, "newConverter", new Class[] { Integer.TYPE }, new Object[] { new Integer(ccsid) });
        }
        catch (InvocationTargetException e)
        {
            Throwable target = e.getTargetException();
            if (target instanceof UnsupportedEncodingException)
            {
                throw (UnsupportedEncodingException) target;
            }
            throw ProxyClientConnection.rethrow(e);
        }
    }

    // Remove connection listener dispatcher.
    public void removeConnectionListener(ConnectionListener listener)
    {
        connection_.removeListener(pxId_, listener, "Connection");
    }

    // Set the port for a service.
    public void setServicePort(String systemName, int service, int port)
    {
        try
        {
            connection_.callMethod(pxId_, "setServicePort", new Class[] { String.class, Integer.TYPE, Integer.TYPE }, new Object[] { systemName, new Integer(service), new Integer(port) });
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    // Set the ports to default.
    public void setServicePortsToDefault(String systemName)
    {
        try
        {
            connection_.callMethod(pxId_, "setServicePortsToDefault", new Class[] { String.class }, new Object[] { systemName });
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    // Set the significant instance variables for the AS400ImplRemote object.
    public void setState(SSLOptions useSSLConnection, boolean canUseNativeOptimization, boolean threadUsed, int ccsid, Locale locale, SocketProperties socketProperties, String ddmRDB)
    {
        try
        {
            connection_.callMethod(pxId_, "setState", new Class[] { SSLOptions.class, Boolean.TYPE, Boolean.TYPE, Integer.TYPE, Locale.class, SocketProperties.class, String.class }, new Object[] { useSSLConnection, new Boolean(canUseNativeOptimization), new Boolean(threadUsed), new Integer(ccsid), locale, socketProperties, ddmRDB });
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    // Sign-on.
    public SignonInfo signon(String systemName, String userId, byte[] bytes, int byteType, String gssName, int gssOption) throws AS400SecurityException, IOException
    {
        try
        {
            return (SignonInfo)connection_.callMethod(pxId_, "signon", new Class[] { String.class, String.class, byte[].class, Integer.TYPE, String.class, Integer.TYPE }, new Object[] { systemName, userId, bytes, new Integer(byteType), gssName, new Integer(gssOption) }).getReturnValue();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow2(e);
        }
    }
}
