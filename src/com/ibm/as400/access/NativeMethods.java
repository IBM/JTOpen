///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  NativeMethods.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// The NativeMethods class is used to call the native methods for the IBM Toolbox for Java Native Classes.
public class NativeMethods
{
    static
    {
        System.load("/QSYS.LIB/QYJSPART.SRVPGM");
    }

    static boolean loadSCK()
    {
        try
        {
            System.load("/QSYS.LIB/QYJSPSCK.SRVPGM");
            return true;
        }
        catch (Throwable e)
        {
            Trace.log(Trace.ERROR, "Error loading QYJSPSCK service program:", e);
            return false;
        }
    }

    static native int socketAvailable(int sd) throws NativeException;
    static native int socketCreate(int serverNumber) throws NativeException;
    static native void socketClose(int sd) throws NativeException;
    static native int socketRead(int sd, byte b[], int off, int len) throws NativeException;
    static native void socketWrite(int sd, byte b[], int off, int len) throws NativeException;
    static native byte[] getUserId() throws NativeException;
    static native byte[] getUserInfo(byte[] cSeed, byte[] sSeed) throws NativeException;
}
