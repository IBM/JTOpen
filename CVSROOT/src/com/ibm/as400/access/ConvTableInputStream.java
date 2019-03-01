///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConvTableInputStream.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;

// ConvTableInputStream is a single purpose InputStream that should be used only be the ConvTable class.  It is designed to improve the performance of character set conversion.
class ConvTableInputStream extends InputStream
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    // The buffer into which incoming data is placed.
    private byte[] buffer_ = null;
    private int offset_ = 0;
    private int end_ = 0;

    ConvTableInputStream()
    {
    }

    // Hold this buffer.
    void setContents(byte[] buffer, int offset, int length)
    {
        buffer_ = buffer;
        offset_ = offset;
        end_ = offset + length;
    }

    // Read one byte.
    public int read()
    {
        if (buffer_ == null)
        {
            return -1;
        }
        int ret = buffer_[offset_++] & 0xFF;
        if (offset_ == end_)
        {
            // Now empty.
            buffer_ = null;
        }
        return ret;
    }

    // Read array of bytes.
    public int read(byte[] buffer, int offset, int length)
    {
        if (buffer_ == null)
        {
            return -1;
        }
        int bytesAvail = end_ - offset_;
        if (length < bytesAvail)
        {
            System.arraycopy(buffer_, offset_, buffer, offset, length);
            offset_ += length;
            return length;
        }
        else
        {
            System.arraycopy(buffer_, offset_, buffer, offset, bytesAvail);
            buffer_ = null;
            return bytesAvail;
        }
    }

    // Number of bytes available.
    public int available()
    {
        if (buffer_ == null)
        {
            return 0;
        }
        else
        {
            return end_ - offset_;
        }
    }
}
