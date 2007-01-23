///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  QueuedMessageEnumeration.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Enumeration;
import java.util.NoSuchElementException;

// Helper class.  Used to wrap the QueuedMessage[] with an Enumeration.  This class is used by MessageQueue and JobLog.
class QueuedMessageEnumeration implements Enumeration
{
    private QueuedMessage[] messageCache_;
    private MessageQueue mq_;
    private JobLog jl_;
    private HistoryLog hl_;		//@HLA
    private int counter_;
    private int numMessages_;
    private int listOffset_ = 0;
    private int cachePos_ = 0;

    QueuedMessageEnumeration(MessageQueue mq, int length)
    {
        mq_ = mq;
        numMessages_ = length;
    }

    QueuedMessageEnumeration(JobLog jl, int length)
    {
        jl_ = jl;
        numMessages_ = length;
    }
    
    //@HLA
    QueuedMessageEnumeration(HistoryLog hl, int length)
    {
    	hl_ = hl;
    	numMessages_ = length;
    }

    public final boolean hasMoreElements()
    {
        return counter_ < numMessages_;
    }

    public final Object nextElement()
    {
        if (counter_ >= numMessages_)
        {
            throw new NoSuchElementException();
        }

        if (messageCache_ == null || cachePos_ >= messageCache_.length)
        {
            try
            {
                if (mq_ != null)
                {
                    messageCache_ = mq_.getMessages(listOffset_, 1000);
                }
                else if(hl_ != null)										//@HLA
                {															//@HLA
                	messageCache_ = hl_.getMessages(listOffset_, 1000);		//@HLA
                }															//@HLA
                else
                {
                    messageCache_ = jl_.getMessages(listOffset_, 1000);
                }
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Loaded next block in QueuedMessageEnumeration: " + messageCache_.length + " messages at offset " + listOffset_ + " out of " + numMessages_ + " total.");
            }
            catch (Exception e)
            {
                Trace.log(Trace.ERROR, "Exception while loading nextElement() in QueuedMessageEnumeration:", e);
                throw new NoSuchElementException();
            }
            cachePos_ = 0;
            listOffset_ += messageCache_.length;
        }
        ++counter_;
        return messageCache_[cachePos_++];
    }
}
