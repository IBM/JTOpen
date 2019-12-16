///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ActionCompletedListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.EventListener;

/**
 The ActionCompletedListener interface provides a listener interface for receiving ActionCompleted events.
 **/
public interface ActionCompletedListener extends EventListener
{
    /**
     Invoked when an action has completed.
     @param  event  The action completed event.
     **/
    public void actionCompleted(ActionCompletedEvent event);
}
