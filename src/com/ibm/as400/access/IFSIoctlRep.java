///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: IFSIoctlRep.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2006 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class IFSIoctlRep extends IFSDataStream
{
    public Object getNewDataStream()
    {
        return new IFSIoctlRep();
    }

    byte[] getReplyData()
    {
        byte[] replyData = new byte[data_.length - 22];
        System.arraycopy(data_, 22, replyData, 0, replyData.length);
        return replyData;
    }

    public int hashCode()
    {
        return 0x8015;
    }
}
