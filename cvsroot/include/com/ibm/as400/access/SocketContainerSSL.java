///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SocketContainerSSL.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;

import com.ibm.sslight.SSLCert;
import com.ibm.sslight.SSLContext;
import com.ibm.sslight.SSLSocket;
import com.ibm.sslight.SSLightKeyRing;

// SocketContainerSSL contains a socket capable of SSL communications.
class SocketContainerSSL extends SocketContainer
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    private SSLSocket sslSocket_;

    void setProperties(Socket socket, String serviceName, String systemName, int port, SSLOptions options) throws IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "SocketContainerSSL: create SSLContext");
        SSLContext context = new SSLContext();

        if (Trace.isTraceOn())
        {
            context.debug = true;
        }

        if (options.keyRingData_ == null)
        {
            try
            {
                SSLightKeyRing ring = (SSLightKeyRing)Class.forName(options.keyRingName_).newInstance();
                options.keyRingData_ = ring.getKeyRingData();
            }
            catch (Exception e)
            {
                Trace.log(Trace.ERROR, "Error loading key ring:", e);
                throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
            }
        }
        context.importKeyRings(options.keyRingData_, options.keyRingPassword_);

        if (Trace.isTraceOn())
        {
            String[] cipher_suites = context.getEnabledCipherSuites();
            Trace.log(Trace.DIAGNOSTIC, "Enabled cipher suites:");
            for (int i = 0; i < cipher_suites.length; ++i)
            {
                Trace.log(Trace.DIAGNOSTIC, "   " + cipher_suites[i]);
            }
        }

        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "SocketContainerSSL: create SSLSocket");
        sslSocket_ = new SSLSocket(socket, false, context, SSLSocket.CLIENT, null);
        if (Trace.isTraceOn())
        {
            Trace.log(Trace.DIAGNOSTIC, "SSL connection established");
            Trace.log(Trace.DIAGNOSTIC, "   cipher suite:       " + sslSocket_.getCipherSuite());
            Trace.log(Trace.DIAGNOSTIC, "   compression method: " + sslSocket_.getCompressionMethod());

            SSLCert[] chain = sslSocket_.getPeerCertificateChain();
            if (chain != null)
            {
                Trace.log(Trace.DIAGNOSTIC, "Peer Certificate:");
                Trace.log(Trace.DIAGNOSTIC, chain[0].getKeyInfo() + " bits");
                int[] components = {SSLCert.CN, SSLCert.OU, SSLCert.O, SSLCert.C, SSLCert.L};
                for (int i = 0; i < components.length; ++i)
                {
                    String nameComponent = chain[0].getNameComponent(SSLCert.SUBJECT, components[i]);
                    if (nameComponent != null)
                    {
                        Trace.log(Trace.DIAGNOSTIC, nameComponent);
                    }
                    else
                    {
                        Trace.log(Trace.DIAGNOSTIC, "");
                    }
                }
                Date[] validity = chain[0].getValidity();
                if (validity != null)
                {
                    Trace.log(Trace.DIAGNOSTIC, "Valid From: " + validity[0]);
                    Trace.log(Trace.DIAGNOSTIC, "        To: " + validity[1]);
                }
            }
        }
    }

    void close() throws IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "SocketContainerSSL: close");
        sslSocket_.close();
    }

    InputStream getInputStream() throws IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "SocketContainerSSL: getInputStream");
        return sslSocket_.getInputStream();
    }

    OutputStream getOutputStream() throws IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "SocketContainerSSL: getOutputStream");
        return sslSocket_.getOutputStream();
    }
}
