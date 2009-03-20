///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  JobCCSIDNative.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2003 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class JobCCSIDNative
{
  private static final String CLASSNAME = "com.ibm.as400.access.JobCCSIDNative";
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

    static int retrieveCcsid() throws ErrorCompletingRequestException
    {
        try
        {
            if (AS400.nativeVRM.vrm_ < 0x00050300)
            {
                return new NLSImplNative().ccsidNative();
            }
            else
            {
                return retrieveCcsidNative();
            }
        }
        catch (NativeException e)
        {
            // Exception detected in C code.
            throw new ErrorCompletingRequestException(ErrorCompletingRequestException.AS400_ERROR);
        }
    }

    private static native int retrieveCcsidNative() throws NativeException;
}
