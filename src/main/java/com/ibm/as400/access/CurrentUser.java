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
                currentUser = NativeMethods.getUserId();
        }
        catch (NativeException e) {
            Trace.log(Trace.ERROR, "Error retrieving current userID:", e);
        }
        catch (Throwable e) {
            Trace.log(Trace.ERROR, "Error retrieving current userID:", e);
        }
        
        if (currentUser != null)
        {
        	try
        	{
        		if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Current userID in EBCDIC: ", currentUser);
                String userID = SignonConverter.byteArrayToString(currentUser);
                
                if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Current userID: '" + userID + "'");
                return userID;
        	}
        	catch (AS400SecurityException e) {
        		if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Current userID convert failed, user id characters are not valid");
        		return null;
        	}            
        }
        
        return null;
    }


    static byte[] getUserInfo(int vrm, byte[] clientSeed, byte[] serverSeed, String info) throws AS400SecurityException, IOException
    {
        try
        {
                return NativeMethods.getUserInfo(clientSeed, serverSeed);
        }
        catch (NativeException e) {
            throw AS400ImplRemote.returnSecurityException(BinaryConverter.byteArrayToInt(e.data, 0),null,info);
        }
    }

    private static native byte[] getUserIdNative() throws NativeException;
    private static native byte[] getUserInfoNative(byte[] cSeed, byte[] sSeed) throws NativeException;
}
