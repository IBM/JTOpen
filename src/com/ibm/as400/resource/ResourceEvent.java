///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceEvent.java
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
The ResourceEvent class represents a resource event.
@deprecated Use packages <tt>com.ibm.as400.access</tt> and <tt>com.ibm.as400.access.list</tt> instead. 
**/
public class ResourceEvent
extends EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Private data.
    private Object          attributeID_    = null;
    private int             eventID_        = -1;
    private Object          value_          = null;



/**
Event ID indicating that attribute changes are canceled.
**/
    public static final int ATTRIBUTE_CHANGES_CANCELED      = 1;



/**
Event ID indicating that attribute changes are committed.
**/
    public static final int ATTRIBUTE_CHANGES_COMMITTED     = 2;



/**
Event ID indicating that attribute values are refreshed.
**/
    public static final int ATTRIBUTE_VALUES_REFRESHED      = 3;



/**
Event ID indicating that an attribute is changed.
**/
    public static final int ATTRIBUTE_VALUE_CHANGED         = 4;



/**
Event ID indicating that a resource is created.
**/
    public static final int RESOURCE_CREATED                = 5;



/**
Event ID indicating that a resource is deleted.
**/
    public static final int RESOURCE_DELETED                = 6;



    private static final int FIRST_EVENT_ID_    = ATTRIBUTE_CHANGES_CANCELED;
    private static final int LAST_EVENT_ID_     = RESOURCE_DELETED;



/**
Constructs a ResourceEvent object.

@param source   The source.
@param eventID  The event ID.
**/
    public ResourceEvent(Object source, int eventID)
    {
        super(source);
        validateEventID(eventID);
        eventID_ = eventID;
    }



/**
Constructs a ResourceEvent object.

@param source       The source.
@param eventID      The event ID.
@param attributeID  Identifies the associated attribute, or null if none.
@param value        The attribute value.
**/
    public ResourceEvent(Object source, 
                         int eventID, 
                         Object attributeID, 
                         Object value)
    {
        super(source);
        validateEventID(eventID);

        eventID_        = eventID;
        attributeID_    = attributeID;
        value_          = value;
    }



/**
Returns the associated attribute ID, or null if none.

@return The associated attribute ID, or null if none.
**/
    public Object getAttributeID()
    {
        return attributeID_;
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
Returns the associated attribute value, or null if none.

@return The associated attribute value, or null if none.
**/
    public Object getValue()
    {
        return value_;
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
