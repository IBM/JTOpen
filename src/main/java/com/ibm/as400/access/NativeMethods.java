///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  NativeMethods.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2005, 2007 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// The NativeMethods class is used to call the native methods for the IBM Toolbox for Java Native Classes.
public class NativeMethods
{
    static boolean paseLibLoaded = false;
    static String nativeLibraryQyjspart = "/QSYS.LIB/QYJSPART.SRVPGM"; 
    static
    {
        // Check to see which version of native code to use                 //@pase1
        String osVersion = System.getProperty("os.version");                //"V5" or lower we do not try to load pase
        if ((System.getProperty("java.vm.name").indexOf("Classic VM") < 0)
                && (osVersion.indexOf("V5") == -1) )                        //@pase1
        {                                                                   //@pase1
            try{                                                            //@pase1
                //we are in j9 jvm
                if (System.getProperty("java.home").indexOf("64bit") > 0)       //@pase1
                {                                                               //@pase1
                    //load 64 bit version
                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Loading Native PASE methods for 64bit libs if available");//@pase1
                    System.load("/QIBM/ProdData/OS400/jt400/lib/qyjspase64.so");  //@pase1
                    paseLibLoaded = true;

                } else                                                          //@pase1
                {                                                               //@pase1
                    //load 32 bit version
                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Loading Native PASE methods for 32bit libs if available");//@pase1
                    System.load("/QIBM/ProdData/OS400/jt400/lib/qyjspase32.so");
                    paseLibLoaded = true;
                }                                                               //@pase1
            }catch(Throwable t)                                                 //@pase1
            {                                                                   //@pase1
                //note that if pase version load of libs fail (ie do not exist), then then we just default to ile version below
                if (Trace.traceOn_) Trace.log(Trace.ERROR, t);                  //@pase1
            }                                                                   //@pase1

        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Loading Native non-PASE methods ");  //@pase1

        String alternateLibrary = System.getProperty("com.ibm.as400.access.native.library"); 
        if (alternateLibrary != null) { 
        	nativeLibraryQyjspart = "/QSYS.LIB/"+alternateLibrary+".LIB/QYJSPART.SRVPGM"; 
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "QYJSPART as "+nativeLibraryQyjspart);  
        }
        try{
            System.load(nativeLibraryQyjspart );  //if j9, then socket functions in this lib are overridden
        } catch(Throwable e)
        {
                Trace.log(Trace.ERROR, "Error loading QYJSPART service program:", e); //may be that it is already loaded in multiple .war classloader
        }
    }

    // Assure that the native library is loaded. 
    static void loadNativeLibraryQyjspart() {
        try{
            System.load(nativeLibraryQyjspart );  //if j9, then socket functions in this lib are overridden
        } catch(Throwable e)
        {
                Trace.log(Trace.ERROR, "Error loading QYJSPART service program from "+nativeLibraryQyjspart+":", e); //may be that it is already loaded in multiple .war classloader
        }
    }
    // Assure that the native library is loaded. 
    static void loadNativeLibraryQyjspartThrowsException() throws SecurityException, UnsatisfiedLinkError {
        System.load(nativeLibraryQyjspart );  
    }
    
    static void load()
    {
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

    // Note: The socketPaseXxx() methods deal with a 2-part descriptor.
    static native int socketAvailable(int sd) throws NativeException;
    static native int socketPaseAvailable(int sd, int sd2) throws NativeException;

    static native int socketCreate(int serverNumber) throws NativeException;
    static native int[] socketPaseCreate(int serverNumber) throws NativeException;  // returns 2 descriptors

    static native void socketClose(int sd) throws NativeException;
    static native void socketPaseClose(int sd, int sd2) throws NativeException;

    static native int socketRead(int sd, byte b[], int off, int len) throws NativeException;
    static native int socketPaseRead(int sd, int sd2, byte b[], int off, int len) throws NativeException;

    static native void socketWrite(int sd, byte b[], int off, int len) throws NativeException;
    static native void socketPaseWrite(int sd, int sd2, byte b[], int off, int len) throws NativeException;

    static native byte[] getUserId() throws NativeException;
    static native byte[] getUserInfo(byte[] cSeed, byte[] sSeed) throws NativeException;
    static native byte[] runCommand(byte[] command, int ccsid, int messageOption) throws NativeException;
    static native byte[] runProgram(byte[] name, byte[] library, int numberParameters, byte[] offsetArray, byte[] programParameters, int messageOption) throws NativeException;
}
