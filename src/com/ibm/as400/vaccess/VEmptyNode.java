///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VEmptyNode.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.Icon;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreeNode;
import java.io.Serializable;
import java.util.Enumeration;



/**
The VEmptyNode class is an empty node for use in representing
a tree with no root.  This is used to workaround a Swing
bug documented in AS400TreePane constructor.
**/
class VEmptyNode
implements VNode, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




/**
Adds a listener to be notified when an error occurs.

@param  listener    The listener.
**/
    public void addErrorListener (ErrorListener listener)
    {
    }



/**
Adds a listener to be notified when a VObject is changed,
created, or deleted.

@param  listener    The listener.
**/
    public void addVObjectListener (VObjectListener listener)
    {
    }



/**
Adds a listener to be notified when work in a different thread
starts and stops.

@param  listener    The listener.
**/
    public void addWorkingListener (WorkingListener listener)
    {
    }



/**
Returns the children of the node.

@return         An empty enumeration.
**/
    public Enumeration children ()
    {
        return new Enumeration () {
            public boolean hasMoreElements ()   { return false; }
            public Object nextElement ()        { return null; }
        };
    }



/**
Returns the list of actions that can be performed.

@return Always null.
**/
    public VAction[] getActions ()
    {
        return null;
    }



/**
Indiciates if the node allows children.

@param  Always true.
**/
    public boolean getAllowsChildren ()
    {
        return true;
    }



/**
Returns the child node at the specified index.

@param  index   The index.
@return         Always null.
**/
    public TreeNode getChildAt (int index)
    {
        return null;
    }



/**
Returns the number of children.

@return  Always 0.
**/
    public int getChildCount ()
    {
        return 0;
    }



/**
Returns the default action.

@return Always null.
**/
    public VAction getDefaultAction ()
    {
        return null;
    }



/**
Returns the child for the details at the specified index.

@param  index   The index.
@return         Always null.
**/
    public VObject getDetailsChildAt (int index)
    {
        return null;
    }



/**
Returns the number of children for the details.

@param  Always 0.
**/
    public int getDetailsChildCount ()
    {
        return 0;
    }



/**
Returns the index of the specified child for the details.

@param  detailsChild   The detailsChild.
@return                Always -1.
**/
    public int getDetailsIndex (VObject detailsChild)
    {
        return -1;
    }



/**
Returns the table column model to use in the details
when representing the children.  This column model
describes the details values for the children.

@return The details column model.
**/
    public TableColumnModel getDetailsColumnModel ()
    {
        return new DefaultTableColumnModel ();
    }



/**
Returns the icon.

@param  size    The icon size, either 16 or 32.  If any other
                value is given, then return a default.
@param  open    true for the open icon, false for the closed
                icon.
@return         Always null.
**/
    public Icon getIcon (int size, boolean open)
    {
        return null;
    }



/**
Returns the index of the specified child.

@param  child   The child.
@return         Always -1.
**/
    public int getIndex (TreeNode child)
    {
        return -1;
    }



/**
Returns the parent node.

@return Always null.
**/
    public TreeNode getParent ()
    {
        return null;
    }



/**
Returns the properties pane.

@return Always null.
**/
    public VPropertiesPane getPropertiesPane ()
    {
        return null;
    }



/**
Returns a property value.

@param      propertyIdentifier  The property identifier.
@return                         Always null.
**/
    public Object getPropertyValue (Object propertyIdentifier)
    {
        return null;
    }



/**
Returns the text.  This is the name of the directory.

@return Always "".
**/
    public String getText ()
    {
        return "";
    }



/**
Indicates if the node is a leaf.

@param Always true.
**/
    public boolean isLeaf ()
    {
        return true;
    }



/**
Indicates if the details children are sortable.

@return Always false.
**/
    public boolean isSortable ()
    {
        return false;
    }


/**
Loads the information from the server.
**/
    public void load ()
    {
    }



/**
Removes an error listener.

@param  listener    The listener.
**/
    public void removeErrorListener (ErrorListener listener)
    {
    }



/**
Removes a VObjectListener.

@param  listener    The listener.
**/
    public void removeVObjectListener (VObjectListener listener)
    {
    }



/**
Removes a working listener.

@param  listener    The listener.
**/
    public void removeWorkingListener (WorkingListener listener)
    {
    }



/**
Sorts the children for the details.

@param  propertyIdentifiers The property identifiers.  If any of
                            the property identifiers are null, it
                            means to sort using the string
                            representation of the object.
@param  orders              The sort orders for each property
                            identifier, true for ascending order,
                            false for descending order.
**/
    public void sortDetailsChildren (Object[] propertyIdentifiers,
                                     boolean[] orders)
    {
    }



/**
Returns the string representation.  This is the name of the directory.

@return Always "".
**/
    public String toString ()
    {
        return "";
    }


}
