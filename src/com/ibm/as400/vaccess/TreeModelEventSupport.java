///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: TreeModelEventSupport.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import java.util.Vector;



/**
The TreeModelEventSupport represents a list of TreeModelListeners.
This is also a TreeModelListener and will dispatch all tree model
events that it receives.
**/
class TreeModelEventSupport
implements TreeModelListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private transient   TreeModelListener[] listeners_      = new TreeModelListener[0]; // For speed.
    private transient   Vector              listenersV_     = new Vector ();
    private             Object              source_;



/**
Constructs a TreeModelEventSupport object.

@param  source      The source of the events.
**/
    public TreeModelEventSupport (Object source)
    {
        source_ = source;
    }



/**
Adds a listener.

@param  listener    The listener.
**/
    public void addTreeModelListener (TreeModelListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        listenersV_.addElement (listener);
        synchronized (listeners_) {
            listeners_ = new TreeModelListener[listenersV_.size()];
            listenersV_.copyInto (listeners_);
        }
    }



/**
Fires a tree nodes changed event.

@param  path            The path.
@param  childIndices    The child indices.
@param  children        The children.
**/
    public void fireTreeNodesChanged (TreePath path, int[] childIndices, Object[] children)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].treeNodesChanged (new TreeModelEvent (source_, path, childIndices, children));
        }
    }



/**
Fires a tree nodes changed event.

@param  path            The path.
@param  childIndices    The child index.
@param  children        The child.
**/
    public void fireTreeNodesChanged (TreePath path, int childIndex, Object child)
    {
        int[] childIndices = { childIndex };
        Object[] children = { child };
        fireTreeNodesChanged (path, childIndices, children);
    }



/**
Fires a tree nodes inserted event.

@param  path            The path.
@param  childIndices    The child indices.
@param  children        The children.
**/
    public void fireTreeNodesInserted (TreePath path, int[] childIndices, Object[] children)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].treeNodesInserted (new TreeModelEvent (source_, path, childIndices, children));
        }
    }



/**
Fires a tree nodes inserted event.

@param  path            The path.
@param  childIndices    The child index.
@param  children        The child.
**/
    public void fireTreeNodesInserted (TreePath path, int childIndex, Object child)
    {
        int[] childIndices = { childIndex };
        Object[] children = { child };
        fireTreeNodesInserted (path, childIndices, children);
    }



/**
Fires a tree nodes removed event.

@param  path            The path.
@param  childIndices    The child indices.
@param  children        The children.
**/
    public void fireTreeNodesRemoved (TreePath path, int[] childIndices, Object[] children)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].treeNodesRemoved (new TreeModelEvent (source_, path, childIndices, children));
        }
    }



/**
Fires a tree nodes removed event.

@param  path            The path.
@param  childIndices    The child index.
@param  children        The child.
**/
    public void fireTreeNodesRemoved (TreePath path, int childIndex, Object child)
    {
        int[] childIndices = { childIndex };
        Object[] children = { child };
        fireTreeNodesRemoved (path, childIndices, children);
    }



/**
Fires a tree structure changed event.

@param  path            The path.
**/
    public void fireTreeStructureChanged (TreePath path)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].treeStructureChanged (new TreeModelEvent (source_, path));
        }
    }



/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
    }



/**
Removes a listener.

@param  listener    The listener.
**/
    public void removeTreeModelListener (TreeModelListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        if (listenersV_.removeElement (listener)) {
            synchronized (listeners_) {
                listeners_ = new TreeModelListener[listenersV_.size()];
                listenersV_.copyInto (listeners_);
            }
        }
    }



/**
Processes a tree nodes changed event.

@param  event       The event.
**/
    public void treeNodesChanged (TreeModelEvent event)
    {
         fireTreeNodesChanged (event.getTreePath (), event.getChildIndices (), event.getChildren());
    }



/**
Processes a tree nodes inserted event.

@param  event       The event.
**/
    public void treeNodesInserted (TreeModelEvent event)
    {
         fireTreeNodesInserted (event.getTreePath (), event.getChildIndices (), event.getChildren());
    }



/**
Processes a tree nodes removed event.

@param  event       The event.
**/
    public void treeNodesRemoved (TreeModelEvent event)
    {
         fireTreeNodesRemoved (event.getTreePath (), event.getChildIndices (), event.getChildren());
    }



/**
Processes a tree structure changed event.

@param  event       The event.
**/
    public void treeStructureChanged (TreeModelEvent event)
    {
         fireTreeStructureChanged (event.getTreePath ());
    }



}


