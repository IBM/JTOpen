///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SystemValueEvent.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2006 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.EventObject;

/**
 The SystemValueEvent class represents a system value event.
 @see  SystemValueListener
 **/
public class SystemValueEvent extends EventObject
{
    static final long serialVersionUID = 4L;

    // Private data
    private Object newValue_;
    private Object oldValue_;

    /**
     Constructs a new SystemValueEvent object.
     @param  source  The source of the event.
     @param  newValue  The new value.
     @param  oldValue  The old value.
     **/
    public SystemValueEvent(Object source, Object newValue, Object oldValue)
    {
        super(source);
        newValue_ = newValue;
        oldValue_ = oldValue;
    }

    /**
     Returns the new value of the system value.
     @return  The new value of the system value.
     **/
    public Object getNewValue()
    {
        return newValue_;
    }

    /**
     Returns the old value of the system value.
     @return  The old value of the system value.
     **/
    public Object getOldValue()
    {
        return oldValue_;
    }
}
