///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  CurrentUser.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

// The currentUser class interfaces with the native code for retrieving the current user's userid and encrypted password.
class CurrentUser
{
    static String getUserID(int vrm)
    {
        byte[] currentUser = null;
        try
        {
            if (vrm >= 0x00050400)
            {
                currentUser = NativeMethods.getUserId();
            }
            else if (vrm >= 0x00050200)
            {
                if (NativeMethods.loadSCK())
                {
                    currentUser = getUserIdNative();
                }
            }
            else
            {
                UnixSocketUser user = new UnixSocketUser();
                currentUser = user.getUserId();
            }
        }
        catch (NativeException e)
        {
            Trace.log(Trace.ERROR, "Error retrieving current userID:", e);
        }
        catch (Throwable e)
        {
            Trace.log(Trace.ERROR, "Error retrieving current userID:", e);
        }
        if (currentUser != null)
        {
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Current userID in EBCDIC: ", currentUser);
            String userID = SignonConverter.byteArrayToString(currentUser);
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Current userID: '" + userID + "'");
            return userID;
        }
        return null;
    }

    static byte[] getUserInfo(int vrm, byte[] clientSeed, byte[] serverSeed) throws AS400SecurityException, IOException
    {
        try
        {
            if (vrm >= 0x00050400)
            {
                return NativeMethods.getUserInfo(clientSeed, serverSeed);
            }
            else if (vrm >= 0x00050200)
            {
                return getUserInfoNative(clientSeed, serverSeed);
            }
            UnixSocketUser user = new UnixSocketUser();
            return user.getSubstitutePassword(clientSeed, serverSeed);
        }
        catch (NativeException e)
        {
            throw AS400ImplRemote.returnSecurityException(BinaryConverter.byteArrayToInt(e.data, 0));
        }
    }

    private static native byte[] getUserIdNative() throws NativeException;
    private static native byte[] getUserInfoNative(byte[] cSeed, byte[] sSeed) throws NativeException;
}
