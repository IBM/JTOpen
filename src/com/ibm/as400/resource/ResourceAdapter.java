///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceAdapter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;



/**
The ResourceAdapter class is a default implementation of the
{@link com.ibm.as400.resource.ResourceListener ResourceListener}
interface.
**/
public class ResourceAdapter
implements ResourceListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




/**
Invoked when attribute changes are canceled.

@param event    The event.
**/
    public void attributeChangesCanceled(ResourceEvent event) { }



/**
Invoked when attribute changes are committed.

@param event    The event.
**/
    public void attributeChangesCommitted(ResourceEvent event) { }



/**
Invoked when attributes values are refreshed.

@param event    The event.
**/
    public void attributeValuesRefreshed(ResourceEvent event) { }



/**
Invoked when an attribute value is changed.

@param event    The event.
**/
    public void attributeValueChanged(ResourceEvent event) { }



/**
Invoked when an resource is created.

@param event    The event.
**/
    public void resourceCreated(ResourceEvent event) { }



/**
Invoked when an resource is deleted.

@param event    The event.
**/
    public void resourceDeleted(ResourceEvent event) { }



}
