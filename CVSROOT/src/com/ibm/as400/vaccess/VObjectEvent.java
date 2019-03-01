///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VObjectEvent.java
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
The VObjectEvent class represents an event that
is fired when a system resource is changed,
created, or deleted.

@see VObjectListener
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class VObjectEvent
extends EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private boolean duringLoad_      = false;                   // @A1A
    private VObject object_          = null;
    private VNode   parent_          = null;



/**
Constructs a VObjectEvent object.

@param  source      The event source.
@param  object      The affected system resource.
**/
    public VObjectEvent (Object source, VObject object)
    {
        super (source);
        object_ = object;
    }



// @A1A
    VObjectEvent (Object source, VObject object, boolean duringLoad)
    {
        super (source);
        object_ = object;
        duringLoad_ = duringLoad;
    }



/**
Constructs a VObjectEvent object.

@param  source      The event source.
@param  object      The affected system resource.
@param  parent      The parent of the affected system resource.
                    This is only relevant for object created
                    events.
**/
    public VObjectEvent (Object source, VObject object, VNode parent)
    {
        super (source);
        object_ = object;
        parent_ = parent;
    }



/**
Returns the affected system resource.

@return The system resource.
**/
    public VObject getObject ()
    {
        return object_;
    }



/**
Returns the parent of the affected system resource.
This is only relevant for object created events.

@return The parent, or null if none was set.
**/
    public VNode getParent ()
    {
        return parent_;
    }



// @A1A
    public boolean isDuringLoad ()
    {
        return duringLoad_;
    }


}

