///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: IFSIoctlReq.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2006 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class IFSIoctlReq extends IFSDataStreamReq
{
    IFSIoctlReq(int id, byte[] pathName)
    {
        super(26 + pathName.length);
        setLength(data_.length);
        setTemplateLen(6);
        setReqRepID(0x002A);
        set32bit(id, 22);
        System.arraycopy(pathName, 0, data_, 26, pathName.length);
    }
}
