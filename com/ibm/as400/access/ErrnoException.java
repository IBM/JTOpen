///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ErrnoException.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2006 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 The ErrnoException class represents an exception that indicates that an errno has been returned by a system API.  The <A HREF="AS400Message.html">AS400Message</A> corresponding to the errno is set, however the load() method must be called to retrieve the text and help.
**/
public class ErrnoException extends AS400Exception
{
    static final long serialVersionUID = 4L;

    int errno_;

    ErrnoException(AS400 system, int errno)
    {
        super(createMessage(system, errno));
        errno_ = errno;
    }

    public int getErrno()
    {
        return errno_;
    }

    private static AS400Message createMessage(AS400 system, int errno)
    {
        AS400Message msg = new AS400Message();
        msg.setFileName("QCPFMSG");
        msg.setID("CPE" + errno);
        msg.setLibraryName("*LIBL");
        msg.setSystem(system);
        return msg;
    }
}
