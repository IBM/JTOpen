///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DoubleClickAdapter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.io.Serializable;



/**
The DoubleClickAdapter class is a mouse listener that
listens for double clicks.  If the object that is double
clicked has a default action, then the default action is
invoked.  If the object that is double clicked does not have
a default action, but it does have children, then it sets the
pane to display the object's children.
**/
class DoubleClickAdapter
extends MouseAdapter
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private VActionContext      actionContext_;
    private VPane               pane_;



/**
Constructs an DoubleClickAdapter object.

@param  pane            The pane.
@param  actionContext   The action context.
**/
    public DoubleClickAdapter (VPane pane,
                               VActionContext actionContext)
    {
        pane_           = pane;
        actionContext_  = actionContext;
    }



/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
    }



/**
Processes mouse clicked events.

@param  event   The event.
**/
    public void mouseClicked (MouseEvent event)
    {
        // Is this a double click?
        if (event.getClickCount () > 1) {

            // Get the object that was double clicked, if any.
            VObject object = pane_.getObjectAt (event.getPoint ());
            if (object != null) {

                // If the object has a default action, then perform it.
                VAction defaultAction = object.getDefaultAction ();
                if (defaultAction != null)
                    defaultAction.perform (actionContext_);

                // Otherwise, if the object is a VNode, then set the
                // pane to display this object.
                else if (object instanceof VNode) {
                    try {
                        pane_.setRoot ((VNode) object);
                    }
                    catch (PropertyVetoException e) {
                        // Ignore.
                    }
                }

            }
        }
    }



}
