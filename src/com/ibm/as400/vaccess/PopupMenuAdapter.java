///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PopupMenuAdapter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTree;
import java.io.Serializable;



/**
The PopupMenuAdapter class is a mouse listener that puts up
a popup menu when appropriate.  The menu contains some common items
as well as an item for each action that an object supports.
**/
class PopupMenuAdapter
extends MouseAdapter
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private VActionContext      actionContext_;
    private VPane               pane_;
    private VPropertiesAction   propertiesAction_;



/**
Constructs a PopupMenuAdapter object.

@param  pane            The pane.
@param  actionContext   The action context.
**/
    public PopupMenuAdapter (VPane pane,
                             VActionContext actionContext)
    {
        pane_               = pane;
        actionContext_      = actionContext;
        propertiesAction_   = new VPropertiesAction ();
    }



/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
    }



/**
Processes mouse pressed events.  If they are a popup
trigger, then put up the popup menu.

@param  event   The event.
**/
    public void mousePressed (MouseEvent event)
    {
        if (event.isPopupTrigger ())
            showPopupMenu (event.getComponent (), event.getPoint ());
    }



/**
Processes mouse released events.  If they are a popup
trigger, then put up the popup menu.

@param  event   The event.
**/
    public void mouseReleased (MouseEvent event)
    {
        if (event.isPopupTrigger ())
            showPopupMenu (event.getComponent (),event.getPoint ());
    }



/**
Shows the popup menu.

@param  component   The component.
@param  point       The point.
**/
    private void showPopupMenu (Component component, Point point)
    {
        // Initialization.
        VObject pointObject = pane_.getObjectAt (point);
        VObject rootObject = pane_.getRoot ();
        int itemCount = 0;

        // Compute the subject object.  This is the point object,
        // and if not set, this is the root object.  If neither
        // is set, then this is null.
        VObject subjectObject = null;
        if (pointObject != null)
            subjectObject = pointObject;
        else if (rootObject != null) 
        {
            subjectObject = rootObject;
            // root object used, clear any selection.    - $A1
            if (component instanceof JTree)
               ((JTree)component).getSelectionModel().clearSelection();
            else if (component instanceof JTable)
               ((JTable)component).getSelectionModel().clearSelection();            
        }
        // System.out.println ("Point object   = " + pointObject);
        // System.out.println ("Root object    = " + rootObject);
        // System.out.println ("Subject object = " + subjectObject);

        // Create the popup menu.
        JPopupMenu menu = new JPopupMenu ();
        JMenuItem menuItem;

        // Put up any actions for the subject object.
        VAction[] actions = subjectObject.getActions ();
        if (actions != null) {
            for (int i = 0; i < actions.length; ++i) {
                menuItem = new JMenuItem (actions[i].getText ());
                menuItem.addActionListener (new VActionAdapter (actions[i], actionContext_));
                menuItem.setEnabled (actions[i].isEnabled ());
                menu.add (menuItem);
                ++itemCount;
            }

            if (actions.length > 0)
                menu.addSeparator ();
        }

        // Drag and drop is currently not supported.  If and when
        // it is, here is some rules for it:
        //
        // If there is a point object, then put up cut and copy.
        // However, enable them only if the point object is a
        // potential drag source.
        //
        // If there is a subject object, then put up paste,
        // but only enable it if the subject object is a potential
        // drop target for what is currently in the clipboard.

        // Put up properties for the subject object.
        if (subjectObject != null) {
            menuItem = new JMenuItem (propertiesAction_.getText ());
            if (subjectObject.getPropertiesPane () != null) {
                propertiesAction_.setObject (subjectObject);
                menuItem.addActionListener (new VActionAdapter (propertiesAction_, actionContext_));
                menuItem.setEnabled (true);
            }
            else
                menuItem.setEnabled (false);
            menu.add (menuItem);
            ++itemCount;
        }

        // Show the menu.
        if (itemCount > 0)
            menu.show (component, point.x, point.y);
    }



}

