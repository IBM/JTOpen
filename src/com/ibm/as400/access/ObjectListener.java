///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ObjectListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.EventListener;

/**
 The ObjectListener interface provides a listener interface for receiving Object events.
 **/
public interface ObjectListener extends EventListener
{
    /**
     Invoked when an object has been closed.
     @param  event  The object event.
     **/
    public void objectClosed(ObjectEvent event);

    /**
     Invoked when an object has been created.
     @param  event  The object event.
     **/
    public void objectCreated(ObjectEvent event);

    /**
     Invoked when an object has been deleted.
     @param  event  The object event.
     **/
    public void objectDeleted(ObjectEvent event);

    /**
     Invoked when an object has been opened.
     @param  event  The object event.
     **/
    public void objectOpened(ObjectEvent event);
}
