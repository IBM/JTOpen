///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  OpenListEnumeration.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access.list;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import com.ibm.as400.access.Trace;

// Helper class.  Used to wrap the OpenList objects with an Enumeration.
final class OpenListEnumeration implements Enumeration
{
    // Reference back to list object.
    private OpenList list_;
    // Indication if list is closed.
    private boolean closed_ = false;
    // Total number of objects in the server list.
    private int length_;
    // Position in the server list.
    private int counter_;
    // Offset in the server list.
    private int listOffset_ = 0;
    // Cached objects from list.
    private Object[] objectCache_;
    // Current position within the cache.
    private int cachePosition_ = 0;

    OpenListEnumeration(OpenList list, int length)
    {
        list_ = list;
        length_ = length;
    }

    // Called by OpenList when someone closes it.  This invalidates us.
    synchronized void close()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "OpenList closed enumeration: " + this);
        closed_ = true;
    }

    public boolean hasMoreElements()
    {
        return closed_ && counter_ < listOffset_ || !closed_ && counter_ < length_;
    }

    public synchronized Object nextElement()
    {
        // If we are closed, but we still have objects in the cache, then we might as well return them.
        if (closed_ && counter_ >= listOffset_ || !closed_ && counter_ >= length_)
        {
            throw new NoSuchElementException();
        }

        if (objectCache_ == null || cachePosition_ >= objectCache_.length)
        {
            try
            {
                int blockSize = list_.getEnumerationBlockSize();
                objectCache_ = list_.getItems(listOffset_, blockSize);
                if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Loaded next block in OpenListEnumeration: " + objectCache_.length + " messages at offset " + listOffset_ + " out of " + length_ + " total, using block size " + blockSize + ".");
            }
            catch (Exception e)
            {
                if (Trace.isTraceOn()) Trace.log(Trace.ERROR, "Exception while loading nextElement() in OpenListEnumeration:", e);
                throw new NoSuchElementException();
            }
            cachePosition_ = 0;
            listOffset_ += objectCache_.length;
        }
        ++counter_;
        Object obj = objectCache_[cachePosition_];
        // Set to null to reduce memory usage as nextElement() is called.
        objectCache_[cachePosition_++] = null;

        // When we reach the end, our Enumeration is now useless.
        // We "close" ourselves and notify our Open List that we're used up.
        // This aids in garbage collection.
        if (counter_ >= length_)
        {
            closed_ = true;
            list_.remove(this);
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "OpenListEnumeration reached last element: " + this);
        }

        return obj;
    }
}
