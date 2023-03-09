///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VObjectListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.util.EventListener;



/**
The VObjectListener interface provides a listener interface for
receiving VObjectEvents.

@see VObjectEvent
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public interface VObjectListener
extends EventListener
{



/**
Invoked when a system resource is changed.

@param event The event.
**/
    abstract public void objectChanged (VObjectEvent event);



/**
Invoked when a system resource is created.

@param event The event.
**/
    abstract public void objectCreated (VObjectEvent event);



/**
Invoked when a system resource is deleted.

@param event The event.
**/
    abstract public void objectDeleted (VObjectEvent event);



}

