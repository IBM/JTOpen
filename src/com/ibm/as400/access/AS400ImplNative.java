///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400ImplNative.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

class AS400ImplNative
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private static final String CLASSNAME = "com.ibm.as400.access.AS400ImplNative";
    static
    {
        if (Trace.traceOn_) Trace.logLoadPath(CLASSNAME);
        System.load("/QSYS.LIB/QYJSPART.SRVPGM");
    }

    static native byte[] signonNative(byte[] userId) throws NativeException;
    static native void swapToNative(byte[] userId, byte[] bytes, byte[] swapToPH, byte[] swapFromPH) throws NativeException;
    static native void swapBackNative(byte[] swapToPH, byte[] swapFromPH) throws NativeException;
}
