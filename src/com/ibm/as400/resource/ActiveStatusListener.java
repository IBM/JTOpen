///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ActiveStatusListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import java.util.EventListener;



/**
The ActiveStatusListener represents a listener for ActiveStatusEvents.
**/
public interface ActiveStatusListener
extends EventListener
{


/**
Invoked when the active status changes to busy.   This indicates
that a potentially long-running operation has started.

@param event    The event.
**/
    public abstract void busy(ActiveStatusEvent event);
                                          


/**
Invoked when the active status changes to idle.   This indicates
that a potentially long-running operation has ended.

@param event    The event.
**/
    public abstract void idle(ActiveStatusEvent event);
                                          


}
