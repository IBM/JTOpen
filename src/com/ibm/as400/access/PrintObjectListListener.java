///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrintObjectListListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
 * The PrintObjectListListener class provides a listener interface for receiving PrintObjectList events.
 *
 * @see PrintObjectList
 *
 **/
public interface PrintObjectListListener
       extends java.util.EventListener
{
    /**
     * Invoked when the list was closed.
     **/
    public abstract void listClosed(PrintObjectListEvent e);

    /**
     * Invoked when the list has completed.
     **/
    public abstract void listCompleted(PrintObjectListEvent e);

    /**
     * Invoked when an error occurred while retrieving the list.
     **/
    public abstract void listErrorOccurred(PrintObjectListEvent e);

    /**
     * Invoked when the list was opened.
     **/
    public abstract void listOpened(PrintObjectListEvent e);

    /**
     * Invoked when an object was added to the list.
     **/
    public abstract void listObjectAdded(PrintObjectListEvent e);
}
