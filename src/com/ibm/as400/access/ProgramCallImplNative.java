///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ProgramCallImplNative.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// CommandCallImplNative function merged with ProgramCallImplNative and moved to RemoteCommandImplNative.
class ProgramCallImplNative
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static
    {
      System.load("/QSYS.LIB/QYJSPCMD.SRVPGM");
    }

    ProgramCallImplNative()
    {
    }

    native byte[] runProgramNative(byte[] request, int requestLength, byte[] replyBuffer, int replyBufferLength) throws NativeException;
}
