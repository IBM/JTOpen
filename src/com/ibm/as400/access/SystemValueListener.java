///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SystemValueListener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2006 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.EventListener;

/**
 The SystemValueListener interface provides a listener interface for receiving SystemValueEvents.
 @see  SystemValueEvent
 **/
public interface SystemValueListener extends EventListener
{
    /**
     Invoked when a system value on the system is changed by this object.
     @param  event  The system value changed event.
     **/
    abstract public void systemValueChanged(SystemValueEvent event);
}
