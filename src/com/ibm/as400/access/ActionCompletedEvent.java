///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ActionCompletedEvent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.EventObject;

/**
 The ActionCompletedEvent class represents an ActionCompleted event.
 **/
public class ActionCompletedEvent extends EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    /**
     Constructs an ActionCompletedEvent object.  It uses the specified source object that completed the action.
     @param  source  The object where the event originated.
     **/
    public ActionCompletedEvent(Object source)
    {
        super(source);
    }
}
