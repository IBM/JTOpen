///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: WorkingEvent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.util.EventObject;



/**
The WorkingEvent class represents an event that signals that
a model is working on a request.  This is useful for
graphical user interfaces that need to give the user some
feedback that work is being done.

@see WorkingListener
@see WorkingCursorAdapter
**/
public class WorkingEvent
extends EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




/**
Constructs a WorkingEvent object.

@param  source      The source of the event.
**/
    public WorkingEvent (Object source)
    {
        super (source);
    }



/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
    }



}


