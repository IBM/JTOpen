///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PSPrintObjectListListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The PSPrintObjectListListener class dispatches
PrintObjectListEvents fired within the ProxyServer to the
client.
**/
class PSPrintObjectListListener
extends PSEventDispatcher
implements PrintObjectListListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private static final String listenerInterfaceName_ = "com.ibm.as400.access.PrintObjectListListener";



/**
Constructs a PSPrintObjectListListener object.

@param connection   The proxy server connection.
@param proxyTable   The proxy table.
@param proxyId      The proxy id.
**/
    public PSPrintObjectListListener (PSConnection connection, PxTable proxyTable, long proxyId)
    { 
        super (connection, proxyTable, proxyId);
    }



/**
Invoked when the list is closed.

@param event    The event.
**/
    public void listClosed (PrintObjectListEvent event)
    {
        fireEvent (listenerInterfaceName_, "listClosed", event);
    }



/**
Invoked when the list has completed.

@param event    The event.
**/
    public void listCompleted (PrintObjectListEvent event)
    {
        fireEvent (listenerInterfaceName_, "listCompleted", event);
    }



/**
Invoked when an error occurs while retrieving the list.

@param event    The event.
**/
    public void listErrorOccurred (PrintObjectListEvent event)
    {
        fireEvent (listenerInterfaceName_, "listErrorOccurred", event);
    }



/**
Invoked when an object is added to the list.

@param event    The event.
**/
    public void listObjectAdded (PrintObjectListEvent event)
    {
        fireEvent (listenerInterfaceName_, "listObjectAdded", event);
    }



/**
Invoked when the list is opened.

@param event    The event.
**/
    public void listOpened (PrintObjectListEvent event)
    {
        fireEvent (listenerInterfaceName_, "listOpened", event);
    }



}
