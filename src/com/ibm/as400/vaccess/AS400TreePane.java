///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400TreePane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.CellEditor;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.Serializable;



/**
The AS400TreePane class represents a graphical user interface
that presents a tree, where all information for the tree is
gathered from the hierarchy of objects rooted at a system resource.
You must explicitly call load() to load the information from
the system.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>AS400TreePane objects generate the following
events:
<ul>
    <li>ErrorEvent
    <li>PropertyChangeEvent
    <li>TreeSelectionEvent
</ul>

<p>The following example creates a tree pane filled with
the contents of a directory in the integrated file system
of a system.

<pre>
// Set up the tree pane.
AS400 system = new AS400 ("MySystem", "Userid", "Password");
VIFSDirectory directory = new VIFSDirectory (system, "/myDirectory");
AS400TreePane treePane = new AS400TreePane (directory);
treePane.load ();
<br>
// Add the tree pane to a frame.
JFrame frame = new JFrame ("My Window");
frame.getContentPane().add (treePane);
</pre>

@see AS400TreeModel
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class AS400TreePane
extends JComponent
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Constant.
    private static VNode dummyRoot_ = new VEmptyNode ();



    // Properties.
    boolean                         allowActions_           = true; // Private.
    boolean                         confirm_                = true; // Private.
    AS400TreeModel                  model_                  = null; // Private.
    JTree                           tree_                   = null; // Private.



    // Private data.
    transient private VActionContext                  actionContext_;
    transient private PopupMenuAdapter                popupMenuAdapter_;



    // Event support.
    transient private ErrorEventSupport               errorEventSupport_;
    transient private PropertyChangeSupport           propertyChangeSupport_;
    transient private TreeSelectionEventSupport       treeSelectionEventSupport_;
    transient private VetoableChangeSupport           vetoableChangeSupport_;



/**
Constructs an AS400TreePane object.
**/
    public AS400TreePane ()
    {
        // Initialize the model.
        //
        // The reason that we initialize the root to something
        // other than null is to workaround a Swing bug.  If
        // you create a JTree with a null root, just about
        // everything causes a NullPointerException inside
        // Swing, including setting the root to something else.
        //
        model_ = new AS400TreeModel (dummyRoot_);

        // Initialize the tree.
        tree_ = new JTree (model_);
        tree_.setCellEditor (new VObjectCellEditor ());
        tree_.setCellRenderer (new VObjectCellRenderer ());
        tree_.setEditable (true);
        tree_.setRootVisible (true);
        tree_.setShowsRootHandles (true);
        tree_.getSelectionModel ().setSelectionMode (TreeSelectionModel.SINGLE_TREE_SELECTION);

        // Layout the pane.
        setLayout (new BorderLayout ());
        add ("Center", new JScrollPane (tree_));

        initializeTransient ();
    }



/**
Constructs an AS400TreePane object.

@param  root  The root, or the system resource, from which all information for the model is gathered.
**/
    public AS400TreePane (VNode root)
    {
        this ();

        if (root == null)
            throw new NullPointerException ("root");

        try {
            model_.setRoot (root);
        }
        catch (PropertyVetoException e) {
            // Ignore.
        }

        expand (root);
    }



/**
Adds a listener to be notified when an error occurs.

@param  listener  The listener.
**/
    public void addErrorListener (ErrorListener listener)
    {
        errorEventSupport_.addErrorListener (listener);
    }



/**
Adds a listener to be notified when the value of any
bound property changes.

@param  listener  The listener.
**/
    public void addPropertyChangeListener (PropertyChangeListener listener)
    {
        super.addPropertyChangeListener (listener);
        propertyChangeSupport_.addPropertyChangeListener (listener);
    }



/**
Adds a listener to be notified when a tree selection occurs.

@param  listener  The listener.
**/
    public void addTreeSelectionListener (TreeSelectionListener listener)
    {
        treeSelectionEventSupport_.addTreeSelectionListener (listener);
    }



/**Adds a listener to be notified when the value of any
constrained property changes.

@param  listener  The listener.
**/
    public void addVetoableChangeListener (VetoableChangeListener listener)
    {
        super.addVetoableChangeListener (listener);
        vetoableChangeSupport_.addVetoableChangeListener (listener);
    }



/**
Collapses the specified object.

@param  object  The object.
**/
    public void collapse (VNode object)
    {
        if (object == null)
            throw new NullPointerException ("object");

        int row = findRow (object);
        if (row >= 0)
            tree_.collapseRow (row);
    }



/**
Expands the specified object.

@param  object    The object.
**/
    public void expand (VNode object)
    {
        if (object == null)
            throw new NullPointerException ("object");

        int row = findRow (object);
        if (row >= 0)
            tree_.expandRow (row);
    }



/**
Finds the row for the object.

@param  object        The object.
@return             The row, or -1 if the object is not in the
                    tree or is not visible.
**/
    private int findRow (VNode object)
    {
        if (object == null)
            throw new NullPointerException ("object");

        TreePath path = getPath (object);
        if (path != null)
            return tree_.getRowForPath (path);
        else
            return -1;
    }



/**
Returns the context in which actions will be performed.

@return The action context.
**/
    public VActionContext getActionContext ()
    {
        return actionContext_;
    }



/**
Indicates if actions can be invoked on objects.

@return true if actions can be invoked; false otherwise.
**/
    public boolean getAllowActions ()
    {
        return allowActions_;
    }



/**
Indicates if certain actions are confirmed with the user.

@return true if certain actions are confirmed with the user;
        false otherwise.
**/
    public boolean getConfirm()
    {
        return confirm_;
    }



/**
Returns the model which contains the data for the tree.

@return The tree model.
**/
    public TreeModel getModel ()
    {
        return model_;
    }



/**
Returns the path from the root to get to this object in the tree
hierarchy. The last element in the path will be this object.

@param  object  The object.
@return         The path, or null if the object is not a valid
                object.
**/
    public TreePath getPath (VNode object)
    {
        if (object == null)
            throw new NullPointerException ("object");

        return model_.getPath (object);
    }



/**
Returns the root, or the system resource, from which all information for the model is gathered.

@return     The root, or the system resource, from which all information for the model is gathered. It will be null if none has been set.
**/
    public VNode getRoot ()
    {
        VNode root = (VNode) model_.getRoot ();
        if (root == dummyRoot_)
            return null;
        else
            return root;
    }



/**
Returns the first selected object.

@return The first selected object, or null if none are
        selected.
**/
    public VNode getSelectedObject ()
    {
        VNode selectedObject = null;
        TreePath selectedPath = tree_.getSelectionPath ();
        if (selectedPath != null)
            selectedObject = (VNode) selectedPath.getLastPathComponent ();
        return selectedObject;
    }



/**
Returns the selected objects.

@return  The selected objects.
**/
    public VNode[] getSelectedObjects ()
    {
        VNode[] selectedObjects = null;
        TreePath[] selectedPaths = tree_.getSelectionPaths ();
        if (selectedPaths != null) {
            selectedObjects = new VNode[selectedPaths.length];
            for (int i = 0; i < selectedPaths.length; ++i)
                selectedObjects[i] = (VNode) selectedPaths[i].getLastPathComponent ();
        }
        else
            selectedObjects = new VNode[0];
        return selectedObjects;
    }



/**
Returns the selection model that is used to maintain
selection state.  This provides the ability to programmatically
select and deselect objects.

@return The selection model, or null if selections are not
        allowed.
**/
    public TreeSelectionModel getSelectionModel ()
    {
        return tree_.getSelectionModel ();
    }



/**
Initializes the transient data.
**/
    private void initializeTransient ()
    {
        // Initialize the event support.
        errorEventSupport_          = new ErrorEventSupport (this);
        propertyChangeSupport_      = new PropertyChangeSupport (this);
        treeSelectionEventSupport_  = new TreeSelectionEventSupport (this);
        vetoableChangeSupport_      = new VetoableChangeSupport (this);

        model_.addErrorListener (errorEventSupport_);
        model_.addPropertyChangeListener (propertyChangeSupport_);
        model_.addVetoableChangeListener (vetoableChangeSupport_);
        tree_.addTreeSelectionListener (treeSelectionEventSupport_);

        // Initialize the action context.
        actionContext_ = new VActionContext_ ();

        // Initialize the other adapters.
        model_.addWorkingListener (new WorkingCursorAdapter (tree_));

        VPane_ pane = new VPane_ ();
        popupMenuAdapter_ = new PopupMenuAdapter (pane, actionContext_);

        if (allowActions_)
            tree_.addMouseListener (popupMenuAdapter_);
    }



/**
Indicates if the object is currently collapsed.

@param  object    The object.
@return true if the object is collapsed; false otherwise.
**/
    public boolean isCollapsed (VNode object)
    {
        if (object == null)
            throw new NullPointerException ("object");

        int row = findRow (object);
        if (row >= 0)
            return tree_.isCollapsed (row);
        else
            return false;
    }



/**
Indicates if the object is currently expanded.

@param  object    The object.
@return true if the object is expanded; false otherwise.
**/
    public boolean isExpanded (VNode object)
    {
        if (object == null)
            throw new NullPointerException ("object");

        int row = findRow (object);
        if (row >= 0)
            return tree_.isExpanded (row);
        else
            return false;
    }



/**
Indicates if the object is selected.

@param  object The object.
@return true if the object is selected; false otherwise.
**/
    public boolean isSelected (VNode object)
    {
        if (object == null)
            throw new NullPointerException ("object");

        int row = findRow (object);
        if (row > 0)
            return tree_.isRowSelected (row);
        else
            return false;
    }



/**
Indicates if the object is currently visible.

@param  object    The object.
@return true if the object is visible; false otherwise.
**/
    public boolean isVisible (VNode object)
    {
        if (object == null)
            throw new NullPointerException ("object");

        TreePath path = getPath (object);
        if (path != null)
        {                                                            //@B0A
            //@B0A - Swing 1.1 bug. JTree.isVisible() returns true
            // when the node isn't in the tree.
            if (tree_.getPathBounds(model_.getPath(object)) == null) //@B0A
            {                                                        //@B0A
              return false;                                          //@B0A
            }                                                        //@B0A
            return tree_.isVisible (path);
        }                                                            //@B0A
        else
            return false;
    }



/**
Loads the information from the system.
**/
    public void load ()
    {
        model_.load ();
        expand ((VNode) model_.getRoot ());
    }



/**
Makes the object visible in the tree by expanding its
parent objects as needed.

@param  object    The object.
**/
    public void makeVisible (VNode object)
    {
        if (object == null)
            throw new NullPointerException ("object");

        TreePath path = getPath (object);
        if (path != null)
            tree_.makeVisible (path);
    }



/**
Removes an error listener.

@param  listener  The listener.
**/
    public void removeErrorListener (ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener (listener);
    }



/**
Removes a property change listener.

@param  listener  The listener.
**/
    public void removePropertyChangeListener (PropertyChangeListener listener)
    {
        super.removePropertyChangeListener (listener);
        propertyChangeSupport_.removePropertyChangeListener (listener);
    }



/**
Removes a tree selection listener.

@param  listener  The listener.
**/
    public void removeTreeSelectionListener (TreeSelectionListener listener)
    {
        treeSelectionEventSupport_.removeTreeSelectionListener (listener);
    }



/**
Removes a vetoable change listener.

@param  listener  The listener.
**/
    public void removeVetoableChangeListener (VetoableChangeListener listener)
    {
        super.removeVetoableChangeListener (listener);
        vetoableChangeSupport_.removeVetoableChangeListener (listener);
    }



/**
Sets whether actions are allowed.  The following are enabled only when
actions are allowed:

<ul>
  <li>popup menu on selected object
</ul>

<p>The default is true.

@param allowActions true if actions are allowed; false otherwise.
**/
    public void setAllowActions (boolean allowActions)
    {
        if (allowActions_ != allowActions) {

            allowActions_ = allowActions;

            if (allowActions_)
                tree_.addMouseListener (popupMenuAdapter_);
            else
                tree_.removeMouseListener (popupMenuAdapter_);
        }
    }



/**
Sets whether certain actions are confirmed with the user.  The default
is true.

@param confirm    true if certain actions are confirmed with the
                           user; false otherwise.
**/
    public void setConfirm (boolean confirm)
    {
        confirm_ = confirm;
    }



/**
Sets the root, or the system resource, from which all information for the model is gathered. It will not take effect until load() is done.

@param  root   The root, or the system resource, from which all information for the model is gathered.


@exception PropertyVetoException If the change is vetoed.
**/
    public void setRoot (VNode root)
        throws PropertyVetoException
    {
        if (root == null)
            throw new NullPointerException ("root");

        tree_.clearSelection ();
        model_.setRoot (root);
        expand (root);
    }



/**
Sets the selection model that is used to maintain selection
state.  This provides the ability to programmatically select
and deselect objects.

@param  selectionModel  The selection model, or null if selections
                        are not allowed.
**/
    public void setSelectionModel (TreeSelectionModel selectionModel)
    {
        // Do not dispatch events from the old selection model any more.
        TreeSelectionModel oldSelectionModel = tree_.getSelectionModel ();
        if (oldSelectionModel != null)
            oldSelectionModel.removeTreeSelectionListener (treeSelectionEventSupport_);

        tree_.setSelectionModel (selectionModel);

        // Dispatch events from the new selection model.
        if (selectionModel != null)
            selectionModel.addTreeSelectionListener (treeSelectionEventSupport_);
    }



/**
Implements the VActionContext interface.
**/
    private class VActionContext_
    implements VActionContext, Serializable
    {
        public boolean getConfirm ()
        {
            return confirm_;
        }

        public Frame getFrame ()
        {
            return VUtilities.getFrame (AS400TreePane.this);
        }

        public CellEditor startEditing (VObject object, Object propertyIdentifier)
        {
            // Validate the parameters.
            if (object == null)
                throw new NullPointerException ("object");
            if (propertyIdentifier == null)
                throw new NullPointerException ("propertyIdentifier");

            if (allowActions_ == false)
                return null;

            // Edit.

            //@B0A - Swing 1.1 bug. JTree.startEditingAtPath() will cause
            // a NullPointerException when it calls getPathBounds()
            // internally when the node isn't in the tree.
            if (tree_.getPathBounds(model_.getPath(object)) != null) //@B0A
            {                                                        //@B0A
              tree_.startEditingAtPath (model_.getPath (object));    //@B0A
            }                                                        //@B0A

            return tree_.getCellEditor ();
        }
    }



/**
Implements the VPane interface.
**/
    private class VPane_
    implements VPane, Serializable
    {

        public VNode getRoot ()
        {
            return AS400TreePane.this.getRoot ();
        }

        public VObject getObjectAt (Point point)
        {
            VObject object = null;
            TreePath treePath = tree_.getClosestPathForLocation (point.x, point.y);
            Rectangle pathBounds = tree_.getPathBounds (treePath);
            if (pathBounds.contains (point))
                object = (VObject) treePath.getLastPathComponent ();
            return object;
        }

        public void setRoot (VNode root)
            throws PropertyVetoException
        {
            AS400TreePane.this.setRoot (root);
        }
    }

}

