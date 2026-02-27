///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: AS400ImplProxy.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2024 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

import org.ietf.jgss.GSSCredential;

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
    @Override
    public void addConnectionListener(ConnectionListener listener)
    {
        connection_.addListener(pxId_, listener, "Connection");
    }

    // Map from CCSID to encoding string.
    @Override
    public String ccsidToEncoding(int ccsid)
    {
        try
        {
            return (String)connection_.callMethod(pxId_, "ccsidToEncoding", new Class[] { Integer.TYPE }, new Object[] { Integer.valueOf(ccsid) }).getReturnValue();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    // Change password.
    @Override
    public SignonInfo changePassword(String systemName, boolean systemNameLocal, String userId, byte[] oldBytes, byte[] newBytes) throws AS400SecurityException, IOException
    {
        return changePassword(systemName, systemNameLocal, userId, oldBytes, newBytes, null);
    }
    
    // Change password.
    @Override
    public SignonInfo changePassword(String systemName, boolean systemNameLocal, String userId, byte[] oldBytes, byte[] newBytes, char[] additionalAuthenticationFactor) throws AS400SecurityException, IOException
    {
        try
        {
            return (SignonInfo)connection_.callMethod(pxId_, "changePassword", new Class[] { String.class, Boolean.TYPE, String.class, byte[].class, byte[].class }, new Object[] { systemName, Boolean.valueOf(systemNameLocal), userId, oldBytes, newBytes }).getReturnValue();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow2(e);
        }
    }

    // Connect to service.
    @Override
    public void connect(int service) throws AS400SecurityException, IOException
    {
      connect(service, -1, false); 
    }
    
    @Override
    public void connect(int service, int overridePort, boolean skipSignonServer) throws AS400SecurityException, IOException
    {
        try
        {
            connection_.callMethod(pxId_, "connect", 
                new Class[] { Integer.TYPE, Integer.TYPE, Boolean.TYPE }, 
                new Object[] { Integer.valueOf(service), Integer.valueOf(overridePort), Boolean.valueOf(skipSignonServer) });
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow2(e);
        }
    }

    // Disconnect from service.
    @Override
    public void disconnect(int service)
    {
        try
        {
            connection_.callMethod(pxId_, "disconnect", new Class[] { Integer.TYPE }, new Object[] { Integer.valueOf(service) });
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    // Exchange seeds with proxy server.
    @Override
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
    @Override
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
          throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e);
        }
    }

    // Sets the raw bytes for the provided profile token.
    @Override
    public void generateProfileToken(ProfileTokenCredential profileToken, String userId, CredentialVault vault, char[] additionalAuthFactor, String gssName) throws AS400SecurityException, IOException, InterruptedException
    {
        try {
          ProxyReturnValue rv = connection_.callMethod (pxId_, "generateProfileToken",
             new Class[] { ProfileTokenCredential.class, String.class, CredentialVault.class,char[].class ,String.class },
             new Object[] { profileToken, userId, vault, additionalAuthFactor, gssName },
             new boolean[] { true, false, false, false, false }, // indicate that 1st arg gets modified
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
          throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e);
        }
    }

    // Get the jobs with which we are connected.
    @Override
    public String[] getJobs(int service)
    {
        try
        {
            return (String[])connection_.callMethod(pxId_, "getJobs", new Class[] { Integer.TYPE }, new Object[] { Integer.valueOf(service) }).getReturnValue();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    // Get the port number for a service.
    @Override
    public int getServicePort(String systemName, int service)
    {
        try
        {
            return connection_.callMethod(pxId_, "getServicePort", new Class[] { String.class, Integer.TYPE }, new Object[] { systemName, Integer.valueOf(service) }).getReturnValueInt();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    // Check service connection.
    @Override
    public boolean isConnected(int service)
    {
        try
        {
            return connection_.callMethod(pxId_, "isConnected", new Class[] { Integer.TYPE }, new Object[] { Integer.valueOf(service) }).getReturnValueBoolean();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    // Check connection's current status.
    @Override
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
    @Override
    public boolean isConnectionAlive(int service)
    {
        try
        {
            return connection_.callMethod(pxId_, "isConnectionAlive", new Class[] { Integer.TYPE }, new Object[] { Integer.valueOf(service) }).getReturnValueBoolean();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    // Load converter into converter pool.
    @Override
    public void newConverter(int ccsid) throws UnsupportedEncodingException
    {
        try
        {
            connection_.callMethod(pxId_, "newConverter", new Class[] { Integer.TYPE }, new Object[] { Integer.valueOf(ccsid) });
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
    @Override
    public void removeConnectionListener(ConnectionListener listener)
    {
        connection_.removeListener(pxId_, listener, "Connection");
    }

    // Set the GSS credential.
    @Override
    public void setGSSCredential(GSSCredential gssCredential)
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
    @Override
    public void setServicePort(String systemName, int service, int port)
    {
        try
        {
            connection_.callMethod(pxId_, "setServicePort", new Class[] { String.class, Integer.TYPE, Integer.TYPE }, new Object[] { systemName, Integer.valueOf(service), Integer.valueOf(port) });
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    // Set the ports to default.
    @Override
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
    @Override
    public void setState(SSLOptions useSSLConnection, boolean canUseNativeOptimization, boolean threadUsed, boolean virtualThreads, int ccsid, String nlv, SocketProperties socketProperties, String ddmRDB, boolean mustUseNetSockets, boolean mustUseSuppliedProfile, boolean mustAddLanguageLibrary)
    {
        try
        {
            connection_.callMethod(pxId_, "setState", new Class[] { SSLOptions.class, Boolean.TYPE, Boolean.TYPE, Boolean.TYPE, Integer.TYPE, String.class, SocketProperties.class, String.class, Boolean.TYPE, Boolean.TYPE, Boolean.TYPE }, new Object[] { useSSLConnection, Boolean.valueOf(canUseNativeOptimization), Boolean.valueOf(threadUsed), Boolean.valueOf(virtualThreads), Integer.valueOf(ccsid), nlv, socketProperties, ddmRDB, Boolean.valueOf(mustUseNetSockets), Boolean.valueOf(mustUseSuppliedProfile), Boolean.valueOf(mustAddLanguageLibrary) } );
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }
    
    @Override
    public SignonInfo setState(AS400Impl impl, CredentialVault credVault) 
    {
        return null;
    }

    // Sign-on.
    @Override
    public SignonInfo signon(String systemName, boolean systemNameLocal, String userId, CredentialVault vault, String gssName) throws AS400SecurityException, IOException
    {
        return signon(systemName, systemNameLocal, userId, vault, gssName, null);
    }

    // Sign-on.
    @Override
    public SignonInfo signon(String systemName, boolean systemNameLocal, String userId, CredentialVault vault, String gssName, char[] additionalAuthenticationFactor) throws AS400SecurityException, IOException
    {
        try
        {
            return (SignonInfo)connection_.callMethod(pxId_, "signon", new Class[] { String.class, Boolean.TYPE, String.class, CredentialVault.class, String.class }, new Object[] { systemName, Boolean.valueOf(systemNameLocal), userId, vault, gssName }).getReturnValue();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow2(e);
        }
    }

    // Skip Sign-on. /*@V1A*/
    @Override
    public SignonInfo skipSignon(String systemName, boolean systemNameLocal, String userId, CredentialVault vault, String gssName) throws AS400SecurityException, IOException
    {
        try
        {
            return (SignonInfo)connection_.callMethod(pxId_, "skipSignon", new Class[] { String.class, Boolean.TYPE, String.class, CredentialVault.class, String.class }, new Object[] { systemName, Boolean.valueOf(systemNameLocal), userId, vault, gssName }).getReturnValue();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow2(e);
        }
    }


    private int bidiStringType = BidiStringType.DEFAULT;
    private byte[] kerbTicket_;
    
    /**
     * Sets bidi string type of the connection. 
     * See <a href="BidiStringType.html">BidiStringType</a> for more information and valid values.
     */
    @Override
    public void setBidiStringType(int bidiStringType){
        this.bidiStringType = bidiStringType;
    }
    
    /**
     * Returns bidi string type of the connection. 
     * See <a href="BidiStringType.html">BidiStringType</a> for more information and valid values.
     */
    @Override
    public int getBidiStringType(){
        return bidiStringType;
    }

    @Override
    public String getSystemName() {
      return connection_.getSystemName(); 
    }

	@Override
	public void setVRM(int v, int r, int m) {
		// Does nothing for the proxy class
	}
	
    @Override
    public void setAdditionalAuthenticationFactor(char[] additionalAuthFactor) {
		// Does nothing for the proxy class
    }

    @Override
    public void setKerbTicket(byte[] ticket) {
        this.kerbTicket_ = ticket;
    }

    private byte[] getKerbTicket() {
        return this.kerbTicket_;
    }
  
}
