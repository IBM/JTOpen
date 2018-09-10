///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ActiveStatusEvent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;


import com.ibm.as400.access.ExtendedIllegalArgumentException;
import java.util.EventObject;



/**
The ActiveStatusEvent class represents an active status event.
@deprecated Use packages <tt>com.ibm.as400.access</tt> and <tt>com.ibm.as400.access.list</tt> instead. 
**/
public class ActiveStatusEvent
extends EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Private data.
    private int             eventID_        = -1;



/**
Event ID indicating that the active status is busy.  This indicates
that a potentially long-running operation has started.
**/
    public static final int BUSY                            = 1;



/**
Event ID indicating that the active status is idle.  This indicates
that a potentially long-running operation has ended.
**/
    public static final int IDLE                            = 2;



    private static final int FIRST_EVENT_ID_    = BUSY;
    private static final int LAST_EVENT_ID_     = IDLE;



/**
Constructs a ActiveStatusEvent object.

@param source   The source.
@param eventID  The event ID.
**/
    public ActiveStatusEvent(Object source, int eventID)
    {
        super(source);
        validateEventID(eventID);
        eventID_ = eventID;
    }



/**
Returns the event ID.

@return The event ID.
**/
    public int getID()
    {        
        return eventID_;
    }



/**
Validates the event ID.

@param eventID  The event ID.
**/
    private void validateEventID(int eventID)
    {
        if ((eventID < FIRST_EVENT_ID_) || (eventID > LAST_EVENT_ID_))
            throw new ExtendedIllegalArgumentException("eventID", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }



}
