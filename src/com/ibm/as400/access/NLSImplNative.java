///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NLSImplNative.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.net.UnknownHostException;

// Native implementation of Central Server function
class NLSImplNative extends NLSImpl
{
  private static final String CLASSNAME = "com.ibm.as400.access.NLSImplNative";
  static
  {
    if (Trace.traceOn_) Trace.logLoadPath(CLASSNAME);
  }

    static
    {
        try{
            System.load("/QSYS.LIB/QYJSPART.SRVPGM");
        } catch(Throwable e)
        {
                Trace.log(Trace.ERROR, "Error loading QYJSPART service program:", e); //may be that it is already loaded in multiple .war classloader
        }
    }


    // connect to the central server of the server.
    void connect() throws ServerStartupException, UnknownHostException, AS400SecurityException, ConnectionDroppedException, InterruptedException, IOException
    {
    }


    // Disconnect from the central server.
    void disconnect()
    {

    }


    // Get the job ccsid
    int getCcsid() throws IOException
    {
        try
        {
            // Call native method
            return ccsidNative();
        }
        catch(NativeException e)  // Exception detected in C code
        {
            // Map to IOException
            throw new IOException();
        }
    }

    // Download table
    /*@B0D    char[] getTable(int fromCCSID, int toCCSID) throws ConnectionDroppedException, IOException, InterruptedException
     {
     try
     {
     // call native method
     byte[] byteData = tableNative(fromCCSID, toCCSID);
     // convert byte array to char array
     char[] table = new char[256];
     for (int i = 0, ii = 0; i < 256; ++i, ii+=2)
     {
     table[i] = (char)(((byteData[ii]   & 0xFF) << 8) +
     (byteData[ii+1] & 0xFF));
     }
     return table;
     }
     catch (NativeException e)  // Exception detected in C code
     {
     // Map to IOException
     throw new IOException();
     }
     }
     @B0D*/

    native int ccsidNative() throws NativeException;
    //@B0D    private native byte[] tableNative(int fromCCSID, int toCCSID) throws NativeException;
}
