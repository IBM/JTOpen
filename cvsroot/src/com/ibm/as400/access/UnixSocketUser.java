///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: UnixSocketUser.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

class UnixSocketUser
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
    static
    {
        System.load("/QSYS.LIB/QYJSPSCK.SRVPGM");
    }

    public UnixSocketUser()
    {
    }

    public byte[] getUserId() throws IOException
    {
        return this.getUserIdNative();
    }

    public byte[] getSubstitutePassword(byte[] cSeed, byte[] sSeed) throws IOException
    {
        return this.getSubstitutePasswordNative(cSeed, sSeed);
    }

    private native byte[] getUserIdNative() throws IOException;
    private native byte[] getSubstitutePasswordNative(byte[] cSeed, byte[] sSeed) throws IOException;
}
