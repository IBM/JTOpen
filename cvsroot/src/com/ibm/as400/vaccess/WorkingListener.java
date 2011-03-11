///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: WorkingListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.util.EventListener;



/**
The WorkingListener interface provides a listener interface
for working events.  This is useful for graphical user
interfaces that need to give the user some feedback that
work is being done.

@see WorkingEvent
@see WorkingCursorAdapter
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public interface WorkingListener
extends EventListener
{



/**
Invoked when a potentially long-running unit of work
is about to begin.

@param  event   The event.
**/
    abstract public void startWorking (WorkingEvent event);



/**
Invoked when a potentially long-running unit of work
has completed.

@param  event   The event.
**/
    abstract public void stopWorking (WorkingEvent event);



}


