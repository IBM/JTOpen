///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: AS400ImplProxy.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2006 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import com.ibm.as400.security.auth.ProfileTokenCredential;
import java.beans.PropertyVetoException;

// AS400ImplProxy forwards implementation methods from proxy client to proxy server.
class AS400ImplProxy extends AbstractProxyImpl implements AS400Impl
{
    private static final String CLASSNAME = "com.ibm.as400.access.AS400ImplProxy";
    static
    {
        if (Trace.traceOn_) Trace.logLoadPath(CLASSNAME);
    }

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
    public SignonInfo changePassword(String systemName, boolean systemNameLocal, String userId, byte[] oldBytes, byte[] newBytes) throws AS400SecurityException, IOException
    {
        try
        {
            return (SignonInfo)connection_.callMethod(pxId_, "changePassword", new Class[] { String.class, Boolean.TYPE, String.class, byte[].class, byte[].class }, new Object[] { systemName, new Boolean(systemNameLocal), userId, oldBytes, newBytes }).getReturnValue();
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

    // Connect to port.
    public Socket connectToPort(int port) throws AS400SecurityException, IOException
    {
        try
        {
            return (Socket)connection_.callMethod(pxId_, "connectToPort", new Class[] { Integer.TYPE }, new Object[] { new Integer(port) }).getReturnValue();
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
        try {
          ProxyReturnValue rv = connection_.callMethod (pxId_, "generateProfileToken",
             new Class[] { ProfileTokenCredential.class, String.class },
             new Object[] { profileToken, userIdentity },
             new boolean[] { true, false }, // indicate that 1st arg gets modified
             true);
          ProfileTokenCredential returnArg = (ProfileTokenCredential)rv.getArgument(0);
          profileToken.setToken(returnArg.getToken());
          return;
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow2(e);
        }
        catch (PropertyVetoException e) { // will never happen
          Trace.log(Trace.ERROR, e);
          throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e.getMessage());
        }
    }

    // Sets the raw bytes for the provided profile token.
    public void generateProfileToken(ProfileTokenCredential profileToken, String userId, CredentialVault vault, String gssName) throws AS400SecurityException, IOException, InterruptedException
    {
        try {
          ProxyReturnValue rv = connection_.callMethod (pxId_, "generateProfileToken",
             new Class[] { ProfileTokenCredential.class, String.class, CredentialVault.class, String.class },
             new Object[] { profileToken, userId, vault, gssName },
             new boolean[] { true, false, false, false }, // indicate that 1st arg gets modified
             true);
          ProfileTokenCredential returnArg = (ProfileTokenCredential)rv.getArgument(0);
          profileToken.setToken(returnArg.getToken());
          return;
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow3(e);
        }
        catch (PropertyVetoException e) { // will never happen
          Trace.log(Trace.ERROR, e);
          throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e.getMessage());
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

    // Check connection's current status.
    public boolean isConnectionAlive()
    {
      try {
        return connection_.callMethodReturnsBoolean (pxId_, "isConnectionAlive");
      }
      catch (InvocationTargetException e) {
        throw ProxyClientConnection.rethrow(e);
      }
    }

    // Check connection's current status.
    public boolean isConnectionAlive(int service)
    {
        try
        {
            return connection_.callMethod(pxId_, "isConnectionAlive", new Class[] { Integer.TYPE }, new Object[] { new Integer(service) }).getReturnValueBoolean();
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

    // Set the GSS credential.
    public void setGSSCredential(Object gssCredential)
    {
        try
        {
            connection_.callMethod(pxId_, "setGSSCredential", new Class[] { Object.class }, new Object[] { gssCredential });
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
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
    public void setState(SSLOptions useSSLConnection, boolean canUseNativeOptimization, boolean threadUsed, int ccsid, String nlv, SocketProperties socketProperties, String ddmRDB, boolean mustUseNetSockets, boolean mustUseSuppliedProfile, boolean mustAddLanguageLibrary)
    {
        try
        {
            connection_.callMethod(pxId_, "setState", new Class[] { SSLOptions.class, Boolean.TYPE, Boolean.TYPE, Integer.TYPE, String.class, SocketProperties.class, String.class, Boolean.TYPE, Boolean.TYPE, Boolean.TYPE }, new Object[] { useSSLConnection, new Boolean(canUseNativeOptimization), new Boolean(threadUsed), new Integer(ccsid), nlv, socketProperties, ddmRDB, new Boolean(mustUseNetSockets), new Boolean(mustUseSuppliedProfile), new Boolean(mustAddLanguageLibrary) } );
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    // Sign-on.
    public SignonInfo signon(String systemName, boolean systemNameLocal, String userId, CredentialVault vault, String gssName) throws AS400SecurityException, IOException
    {
        try
        {
            return (SignonInfo)connection_.callMethod(pxId_, "signon", new Class[] { String.class, Boolean.TYPE, String.class, CredentialVault.class, String.class }, new Object[] { systemName, new Boolean(systemNameLocal), userId, vault, gssName }).getReturnValue();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow2(e);
        }
    }

}
