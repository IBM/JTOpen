///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  DQDataStream.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// Base class for common data queue data stream requests.
class DQDataStream extends ClientAccessDataStream
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    protected DQDataStream(int size)
    {
        super(new byte[size]);
        setLength(size);
        // setHeaderID(0x0000);
        setServerID(0xE007);
        // setCSInstance(0x00000000);
        // setCorrelation(0x00000000);
        setTemplateLen(size - 20);
        // May need to setTemplateLen in subclass.
        // Need to setReqRepID(id) in subclass.
    }

    protected void setQueueAndLibrary(byte[] name, byte[] library)
    {
        // Fill in data queue name.
        System.arraycopy(name, 0, data_, 20, 10);
        // Fill in library.
        System.arraycopy(library, 0, data_, 30, 10);
    }
}
