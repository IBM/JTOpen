///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  KeyedDataQueueEntry.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.UnsupportedEncodingException;

/**
 The KeyedDataQueueEntry class represents an entry on an iSeries server keyed data queue.
 **/
public class KeyedDataQueueEntry extends DataQueueEntry
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // The key for this entry.
    byte[] key_;

    // Constructs a KeyedDataQueueEntry object.
    // @param  key  The key of the entry read.
    // @param  data  The data of the entry read.
    // @param  senderInfo  The sender information of the entry read.  This may be null.
    KeyedDataQueueEntry(BaseDataQueue dq, byte[] key, byte[] data, String senderInfo)
    {
        super(dq, data, senderInfo);
        key_ = key;
    }

    /**
     Returns the key for this data queue entry.
     @return  The key for this data queue entry.
     **/
    public byte[] getKey()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting data queue key.");
        return key_;
    }

    /**
     Returns the key for this data queue entry as a string.
     @return  The key for this data queue entry as a string.
     @exception  UnsupportedEncodingException  If the <i>ccsid</i> is not supported.
     **/
    public String getKeyString() throws UnsupportedEncodingException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting data queue key as String.");

        int length = key_.length;
        while (length >= 1 && key_[length - 1] == 0) --length;
        byte[] copy = new byte[length];
        System.arraycopy(key_, 0, copy, 0, length);
        return dq_.byteArrayToString(copy);
    }
}
