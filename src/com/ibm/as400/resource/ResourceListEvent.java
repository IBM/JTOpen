///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceListEvent.java
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
The ResourceListEvent class represents a resource list event.
**/
public class ResourceListEvent
extends EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";





    static final long serialVersionUID = 4L;



    // Private data.
    private int         eventID_        = -1;
    private long        index_          = -1;
    private long        length_         = -1;
    private Resource    resource_       = null;



/**
Event ID indicating that the length of the list changed.
**/
    public static final int LENGTH_CHANGED      = 1;



/**
Event ID indicating that the list is closed.
**/
    public static final int LIST_CLOSED         = 2;



/**
Event ID indicating that the list is completely loaded.
**/
    public static final int LIST_COMPLETED      = 3;



/**
Event ID indicating that the list is not completely 
loaded due to an error.
**/
    public static final int LIST_IN_ERROR       = 4;



/**
Event ID indicating that the list is opened.
**/
    public static final int LIST_OPENED         = 5;



/**
Event ID indicating that a resource is added to the list.
**/
    public static final int RESOURCE_ADDED      = 6;



    private static final int FIRST_EVENT_ID_    = LENGTH_CHANGED;
    private static final int LAST_EVENT_ID_     = RESOURCE_ADDED;



/**
Constructs a ResourceListEvent object.

@param source   The source.
@param eventID  The event ID.
**/
    public ResourceListEvent(Object source, int eventID)
    {
        super(source);
        validateEventID(eventID);
        eventID_ = eventID;
    }



/**
Constructs a ResourceListEvent object.

@param source   The source.
@param eventID  The event ID.
@param resource The associated resource, or null if none.
@param index    The index of the associated resource within the list.
**/
    public ResourceListEvent(Object source, int eventID, Resource resource, long index)
    {
        super(source);
        validateEventID(eventID);
        eventID_    = eventID;
        resource_   = resource;
        index_      = index;
    }



/**
Constructs a ResourceListEvent object.

@param source   The source.
@param eventID  The event ID.
@param length   The list length.
**/
    public ResourceListEvent(Object source, int eventID, long length)
    {
        super(source);
        validateEventID(eventID);
        eventID_    = eventID;
        length_     = length;
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
Returns the index of the associated resource within the list.

@return The index of the associated resource within the list,
        or -1 if there is no associated  resource.
**/
    public long getIndex()
    {
        return index_;
    }



/**
Returns the length of the list.

@return The length the list, or -1 if the length of the list is
        not relevant.
**/
    public long getLength()
    {
        return length_;
    }



/**
Returns the associated resource.

@return The associated resource, or null if none.
**/
    public Resource getResource()
    {
        return resource_;
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
