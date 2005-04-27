///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceListener.java
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
The ResourceListener represents a listener for ResourceEvents.
@deprecated Use packages <tt>com.ibm.as400.access</tt> and <tt>com.ibm.as400.access.list</tt> instead. 
**/
public interface ResourceListener
extends EventListener
{



/**
Invoked when attribute changes are canceled.

@param event    The event.
**/
    public abstract void attributeChangesCanceled(ResourceEvent event);
                                          


/**
Invoked when attribute changes are committed.

@param event    The event.
**/
    public abstract void attributeChangesCommitted(ResourceEvent event);
                                          


/**
Invoked when attributes values are refreshed.

@param event    The event.
**/
    public abstract void attributeValuesRefreshed(ResourceEvent event);



/**
Invoked when an attribute value is changed.

@param event    The event.
**/
    public abstract void attributeValueChanged(ResourceEvent event);


                                          
/**
Invoked when an resource is created.

@param event    The event.
**/
    public abstract void resourceCreated(ResourceEvent event);


                                          
/**
Invoked when an resource is deleted.

@param event    The event.
**/
    public abstract void resourceDeleted(ResourceEvent event);


                                          
}
