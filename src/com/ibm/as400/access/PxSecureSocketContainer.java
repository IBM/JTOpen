///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: PxSecureSocketContainer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;

import com.ibm.sslight.SSLCert;
import com.ibm.sslight.SSLContext;
import com.ibm.sslight.SSLSocket;
import com.ibm.sslight.SSLightKeyRing;
import com.ibm.sslight.SSLException;

// The PxSecureSocketContainer class represents a wrapper around an SSL socket.
class PxSecureSocketContainer extends PxSocketContainerAdapter
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
    // Constructs a PxSecureSocketContainer object.
    // @param  hostName  The host name.
    // @param  port  The port.
    // @exception  IOException  If an error occurs.
    public PxSecureSocketContainer(String hostName, int port, SSLOptions sslOptions) throws IOException
    {
        super(createSSLSocket(hostName, port, sslOptions));
    }

    // Creates an SSL socket.
    // @param  hostName  The host name.
    // @param  port  The port.
    // @return  The socket.
    // @exception  IOException  If an error occurs.
    private static Socket createSSLSocket(String hostName, int port, SSLOptions sslOptions) throws IOException
    {
        SSLContext context = initializeClientSSLContext(sslOptions);
        SSLSocket socket = null;
        try
        {
           socket = new SSLSocket(hostName, port, context, SSLSocket.CLIENT, null);
        }
        catch(SSLException e)
        {
           Trace.logSSL(Trace.DIAGNOSTIC, e.getCategory(), e.getError(), e.getInt1());
           throw e; // @B2A
        }
        if (Trace.isTraceOn()) traceSSLSocket(socket);
        return socket;
    }

    // Initializes the Proxy Clients SSL context.
    // @return  The context.
    // @exception  IOException  If an error occurs.
    static SSLContext initializeClientSSLContext(SSLOptions sslOptions) throws IOException
    {
        // Create the SSL context.
        SSLContext context = new SSLContext();
        if (Trace.isTraceOn()) context.debug = true;

        // Load the key ring.
        if (sslOptions.keyRingData_ == null)
        {
            try
            {
                SSLightKeyRing ring = (SSLightKeyRing)Class.forName(sslOptions.keyRingName_).newInstance();
                sslOptions.keyRingData_ = ring.getKeyRingData();
            }
            catch (Exception e)
            {
                Trace.log(Trace.ERROR, "Error loading key ring:", e);
                throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
            }
        }
        boolean success = context.importKeyRings(sslOptions.keyRingData_, sslOptions.keyRingPassword_);

        // Trace the cipher suites.
        if (Trace.isTraceOn())
        {
            Trace.log(Trace.DIAGNOSTIC, "Import key rings successful = " + success + ".");
            String[] cipherSuites = context.getEnabledCipherSuites();
            Trace.log(Trace.DIAGNOSTIC, "Enabled cipher suites:");
            for (int i = 0; i < cipherSuites.length; ++i)
            {
                Trace.log(Trace.DIAGNOSTIC, "  " + cipherSuites[i]);
            }
        }

        return context;
    }

    // Initializes the Proxy Servers SSL context.
    // @param  keyringName  The name of the proxy servers keyring.
    // @param  keyringPwd  The password to the proxy servers keyring.
    // @return  The context.
    // @exception  IOException  If an error occurs.
    static SSLContext initializeServerSSLContext(String keyringName, String keyringPwd)throws IOException
    {
        // Create the SSL context.
        SSLContext context = null;

        // Load the key ring.
        SSLightKeyRing keyRing = null;
        boolean success = false;
        try
        {
            keyRing = (SSLightKeyRing) Class.forName(keyringName).newInstance();
            String keyRingData = keyRing.getKeyRingData();
            
            context = new SSLContext();
            
            if (Trace.isTraceOn()) 
               context.debug = true;
            
            success = context.importKeyRings(keyRingData, keyringPwd);
        }
        catch (SSLException e)                                                                  //$B1A
        {                                                                                       //$B1A
            context = null;                                                                     //$B1A
            if (Trace.isTraceOn())                                                              //$B1A
               Trace.logSSL(Trace.DIAGNOSTIC, e.getCategory(), e.getError(), e.getInt1());      //$B1A
        }                                                                                       //$B1A
        catch (InstantiationException e)
        {
            Trace.log(Trace.ERROR, "Unable to instantiate key ring object:", e);
        }
        catch (IllegalAccessException e)
        {
            Trace.log(Trace.ERROR, "Unable to access key ring object:", e);
        }
        catch (ClassNotFoundException e)
        {
            Verbose.println("  java.lang.ClassNotFoundException: " + keyringName);
            Trace.log(Trace.ERROR, "Unable to locate key ring class:", e);
        }
        catch (NoClassDefFoundError e)
        {
            Verbose.println("  java.lang.NoClassDefFoundError: " + keyringName);
            Trace.log(Trace.ERROR, "Unable to locate key ring class:", e);
        }

        // Trace the cipher suites.
        if (Trace.isTraceOn())
        {
            Trace.log(Trace.DIAGNOSTIC, "Import key rings successful = " + success + ".");

            if (context != null)
            {
                String[] cipherSuites = context.getEnabledCipherSuites();
                Trace.log(Trace.DIAGNOSTIC, "Enabled cipher suites:");
                for (int i = 0; i < cipherSuites.length; ++i)
                {
                    Trace.log(Trace.DIAGNOSTIC, "  " + cipherSuites[i]);
                }
            }
        }

        return context;
    }

    // Logs trace information about an SSL socket.
    // @param  socket   The socket.
    static void traceSSLSocket(SSLSocket socket)
    {
        if (Trace.isTraceOn())
        {
            Trace.log(Trace.DIAGNOSTIC, "SSL connection established.");
            Trace.log(Trace.DIAGNOSTIC, "Cipher suite = " + socket.getCipherSuite() + ".");
            Trace.log(Trace.DIAGNOSTIC, "Compression method = " + socket.getCompressionMethod() + ".");

            SSLCert[] peerCertificateChain = socket.getPeerCertificateChain();
            if (peerCertificateChain != null)
            {
                Trace.log(Trace.DIAGNOSTIC, "Peer certificate: " + peerCertificateChain[0].getKeyInfo() + " bits.");
                int[] components = { SSLCert.CN, SSLCert.OU, SSLCert.O, SSLCert.C, SSLCert.L };
                for (int i = 0; i < components.length; ++i)
                {
                    String nameComponent = peerCertificateChain[0].getNameComponent(SSLCert.SUBJECT, components[i]);
                    if (nameComponent != null)
                    {
                        Trace.log(Trace.DIAGNOSTIC, "Name component[" + i + "] = " + nameComponent + ".");
                    }
                    else
                    {
                        Trace.log(Trace.DIAGNOSTIC, "Name component[" + i + "] = (null).");
                    }
                }
                Date[] validity = peerCertificateChain[0].getValidity();
                if (validity != null)
                {
                    Trace.log(Trace.DIAGNOSTIC, "Valid from " + validity[0] + " to " + validity[1] + ".");
                }
            }
        }
    }
}
