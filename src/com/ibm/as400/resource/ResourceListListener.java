///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceListListener.java
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
The ResourceListListener represents a listener for ResourceListEvents.
**/
public interface ResourceListListener
extends EventListener
{



/**
Invoked when the length changes.

@param event    The event.
**/
    public abstract void lengthChanged(ResourceListEvent event);



/**
Invoked when the list is closed.

@param event    The event.
**/
    public abstract void listClosed(ResourceListEvent event);



/**
Invoked when the list is completely loaded.

@param event    The event.
**/
    public abstract void listCompleted(ResourceListEvent event);



/**
Invoked when the list is not completely loaded due to an error.

@param event    The event.
**/
    public abstract void listInError(ResourceListEvent event);



/**
Invoked when the list is opened.

@param event    The event.
**/
    public abstract void listOpened(ResourceListEvent event);



/**
Invoked when a resource is added to the list.

@param event    The event.
**/
    public abstract void resourceAdded(ResourceListEvent event);


}
