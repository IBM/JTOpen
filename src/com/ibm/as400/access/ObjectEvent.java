///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ObjectEvent.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.EventObject;

/**
 The ObjectEvent class represents an Object event.
 **/
public class ObjectEvent extends EventObject
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;

    // The object closed event ID.
    static final int OBJECT_CLOSED = 0;

    // The object created event ID.
    static final int OBJECT_CREATED = 1;

    // The object deleted event ID.
    static final int OBJECT_DELETED = 2;

    // The object opened event ID.
    static final int OBJECT_OPENED = 3;

    /**
     Constructs an ObjectEvent object.  It uses the specified source.
     @param  source  The object where the event originated.
     **/
    public ObjectEvent(Object source)
    {
        super(source);
    }
}
