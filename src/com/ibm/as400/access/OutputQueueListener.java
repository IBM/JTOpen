///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: OutputQueueListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
 * The OutputQueueListener interface provides a
 * listener interface for receiving OutputQueue events.
 *
 **/
public interface OutputQueueListener extends java.util.EventListener
{
    /**
     * Invoked when the output queue has been cleared.
     **/
    public abstract void outputQueueCleared(OutputQueueEvent evt);

    /**
     * Invoked when the output queue has been held.
     **/
    public abstract void outputQueueHeld(OutputQueueEvent evt);

    /**
     * Invoked when the output queue has been released.
     **/
    public abstract void outputQueueReleased(OutputQueueEvent evt);
}

