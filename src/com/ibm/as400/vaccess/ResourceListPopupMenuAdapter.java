///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceListPopupMenuAdapter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.resource.Resource;
import com.ibm.as400.resource.ResourceList;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;



/**
The ResourceListPopupMenuAdapter class represents the pop-up menu for
the resource list GUI classes.
**/
class ResourceListPopupMenuAdapter
extends MouseAdapter
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String ACTION_PROPERTIES_        = ResourceLoader.getText("ACTION_PROPERTIES");
    private static final String ACTION_LIST_PROPERTIES_   = ResourceLoader.getText("ACTION_LIST_PROPERTIES");



    // Private data.
    private DialogCache               dialogCache_                = new DialogCache();
    private ErrorEventSupport         errorEventSupport_;
    private ResourceProperties        resourceProperties_;
    private ResourceList              resourceList_;
    private Object                    target_;



/**
Constructs a ResourceListPopupMenuAdapter object.

@param target               The target resource list GUI object.
@param resourceList         The resource list.
@param resourceProperties   The resource properties.
@param errorEventSupport    The error event support.
**/
    public ResourceListPopupMenuAdapter(Object target,
                                        ResourceList resourceList,
                                        ResourceProperties resourceProperties,
                                        ErrorEventSupport errorEventSupport)
    {
        target_ = target;
        resourceList_ = resourceList;
        resourceProperties_ = resourceProperties;
        errorEventSupport_ = errorEventSupport;
    }



/**
Shows the pop-up menu when the right mouse button is released.

@param event    The event.
**/
    public void mouseReleased(MouseEvent event)
    {
        if (event.isPopupTrigger())
            showPopupMenu(event.getComponent(),event.getPoint());
    }



/**
Sets the resource properties.

@param resourceProperties   The resource properties.
**/
    public void setResourceProperties(ResourceProperties resourceProperties)
    {
        resourceProperties_ = resourceProperties;
    }



/**
Sets the resource list.

@param resourceList   The resource list.
**/
    public void setResourceList(ResourceList resourceList)
    {
        resourceList_ = resourceList;
    }



/**
Shows the pop-up menu.

@param component    The component which was clicked.
@param point        The point.
**/
    private void showPopupMenu(Component component, Point point)
    {
        // Find out which resource was clicked on, if any.
        Resource resource = null;
        if (target_ instanceof ResourceListPane)
            resource = ((ResourceListPane)target_).getResourceAtPoint(point);
        else if (target_ instanceof ResourceListDetailsPane) 
            resource = ((ResourceListDetailsPane)target_).getResourceAtPoint(point);
        else
            return;

        JPopupMenu menu = new JPopupMenu();
        boolean showPopup = false;

        // If a resource was clicked on, then put up a menu for it.
        if (resource != null) {
            JMenuItem propertiesMenuItem = new JMenuItem(ACTION_PROPERTIES_);
            final Resource resource2 = resource;
            propertiesMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    JDialog dialog = dialogCache_.resolveDialog(resource2);
                    if (dialog == null) {
                        dialog = new ResourcePropertiesPane(resource2, resourceProperties_, errorEventSupport_);
                        dialogCache_.addDialog(resource2, dialog);
                        dialog.show();
                    }
                }
            });
            menu.add(propertiesMenuItem);
            showPopup = true;
        }

        // If a resource list is set, then put up a menu for it.
        if (resourceList_ != null) {
            JMenuItem listPropertiesMenuItem = new JMenuItem(ACTION_LIST_PROPERTIES_);
            listPropertiesMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    JDialog dialog = dialogCache_.resolveDialog(resourceList_);
                    if (dialog == null) {
                        dialog = new ResourceListPropertiesPane(resourceList_, errorEventSupport_);
                        dialogCache_.addDialog(resourceList_, dialog);
                        dialog.show();
                    }
                }
            });
            if (showPopup)
                menu.addSeparator();
            menu.add(listPropertiesMenuItem);
            showPopup = true;
        }           

        if (showPopup)
            menu.show(component, point.x, point.y);
    }


}
