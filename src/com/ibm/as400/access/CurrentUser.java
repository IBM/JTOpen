///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: CurrentUser.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

// The currentUser class interfaces with the native code for retrieving the current user's userid and encrypted password.
class CurrentUser
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    static
    {
        try
        {
            System.load("/QSYS.LIB/QYJSPSCK.SRVPGM");
        }
        catch (Throwable e)
        {
            Trace.log(Trace.ERROR, "Error loading QYJSPSCK service program:", e);
        }
    }

    static String getUserID(int vrm)
    {
        if (vrm >= 0x00050200)
        {
            try
            {
                byte[] currentUser = getUserIdNative();
                if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Current userID in EBCDIC: ", currentUser);
                String userID = SignonConverter.byteArrayToString(currentUser);
                if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Current userID: '" + userID + "'");
                return userID;
            }
            catch (NativeException e)
            {
                Trace.log(Trace.ERROR, "Error retrieving current userID:", e);
                return null;
            }
            catch (Throwable e)
            {
                Trace.log(Trace.ERROR, "Error retrieving current userID:", e);
            }
        }
        return getUserID();
    }

    // Returns current user ID.
    static String getUserID()
    {
        try
        {
            SocketContainerUnix container = new SocketContainerUnix();
            // Get user ID and convert to Java format.
            byte[] currentUser = container.getUser();
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Current userID in EBCDIC: ", currentUser);
            String userID = SignonConverter.byteArrayToString(currentUser);
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Current userID: '" + userID + "'");
            return userID;
        }
        catch (Throwable e)
        {
            Trace.log(Trace.ERROR, "Error retrieving current userID:", e);
            return null;
        }
    }

    static byte[] getUserInfo(int vrm, byte[] clientSeed, byte[] serverSeed) throws AS400SecurityException, IOException
    {
        if (vrm >= 0x00050200)
        {
            try
            {
                return getUserInfoNative(clientSeed, serverSeed);
            }
            catch (NativeException e)
            {
                throw AS400ImplRemote.returnSecurityException(BinaryConverter.byteArrayToInt(e.data, 0));
            }
            catch (Throwable e)
            {
                Trace.log(Trace.ERROR, "Error retrieving current user info:", e);
            }
        }
        return getUserInfo(clientSeed, serverSeed);
    }

    // Returns encrypted password.
    static byte[] getUserInfo(byte[] clientSeed, byte[] serverSeed) throws IOException
    {
        try
        {
            SocketContainerUnix container = new SocketContainerUnix();
            return container.getSubstPassword(clientSeed, serverSeed);
        }
        catch (Throwable e)
        {
            Trace.log(Trace.ERROR, "Error retrieving current user info:", e);
            return null;
        }
    }
    private static native byte[] getUserIdNative() throws NativeException;
    private static native byte[] getUserInfoNative(byte[] cSeed, byte[] sSeed) throws NativeException;
}
