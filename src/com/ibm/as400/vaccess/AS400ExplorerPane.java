///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400ExplorerPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;
import javax.swing.CellEditor;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.Serializable;



/**
The AS400ExplorerPane class represents a graphical user interface
that is useful for working with the hierarchy of objects rooted
at an AS/400 resource.  The graphical user interface presents a
tree on the left side and the details of the selected resource
in the right side.  You must explicitly call load() to load the
information from the AS/400.

<p>AS400ExplorerPane objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>ListSelectionEvent
    <li>PropertyChangeEvent
    <li>TreeSelectionEvent
</ul>

<p>The following example creates an explorer pane filled with
the contents of a directory in the integrated file system
of an AS/400.

<pre>
// Set up the explorer pane.
AS400 system = new AS400 ("MySystem", "Userid", "Password");
VIFSDirectory directory = new VIFSDirectory (system, "/myDirectory");
AS400ExplorerPane explorerPane = new AS400ExplorerPane (directory);
explorerPane.load ();
<br>

// Add the explorer pane to a frame.
JFrame frame = new JFrame ("My Window");
frame.getContentPane().add (explorerPane);
</pre>

@see AS400DetailsPane
@see AS400TreePane
**/
//
// Implementation notes:
//
// 1, Object selection works as follows:
//
//    * Only one object may be selected in the tree.  This
//      will update the details view.
//    * Multiple objects may be selected in the details view.
//    * The current selection is considered to be those in
//      the details view, unless there are none, in which case
//      it is the object selected in the tree.
//
public class AS400ExplorerPane
extends JComponent
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Properties.
    AS400DetailsPane            detailsPane_            = null; // Private.
    AS400TreePane               treePane_               = null; // Private.



    // Event support.
    transient private ErrorEventSupport           errorEventSupport_;
    transient private ListSelectionEventSupport   listSelectionEventSupport_;
    transient private PropertyChangeSupport       propertyChangeSupport_;
    transient private TreeSelectionEventSupport   treeSelectionEventSupport_;
    transient private VetoableChangeSupport       vetoableChangeSupport_;



/**
Constructs an AS400ExplorerPane object.
**/
    public AS400ExplorerPane ()
    {
        // Initialize the tree pane.
        treePane_ = new AS400TreePane ();

        // Initialize the details pane.
        detailsPane_ = new AS400DetailsPane ();

        // Set the contained panes minimum size to 0, 0.  This is
        // because JSplitPane will not make a component resize to
        // smaller than its minimum size.  This allows the user
        // to move the divider anywhere, allowing one pane to
        // resize to nothing.
        treePane_.setMinimumSize (new Dimension (0, 0));
        detailsPane_.setMinimumSize (new Dimension (0, 0));

        // Layout the pane.
        setLayout (new BorderLayout ());
        JSplitPane splitPane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT,
            false, treePane_, detailsPane_);
        splitPane.setOneTouchExpandable (true);

        add ("Center", splitPane);

        initializeTransient ();
    }



/**
Constructs an AS400ExplorerPane object.

@param  root     The root, or the AS/400 resource, from which all information for the model is gathered.
**/
    public AS400ExplorerPane (VNode root)
    {
        this ();

        if (root == null)
            throw new NullPointerException ("root");

        // Set the root object in the tree and select it.  Selecting
        // it will automatically set it in the details.
        try {
            treePane_.setRoot (root);
        }
        catch (PropertyVetoException e) {
            // Ignore.
        }

        TreePath rootTreePath = treePane_.getPath (treePane_.getRoot ());
        TreeSelectionModel treeSelectionModel = treePane_.getSelectionModel ();
        treeSelectionModel.clearSelection ();
        treeSelectionModel.setSelectionPath (rootTreePath);
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
Adds a listener to be notified when a list selection occurs in the details pane.

@param  listener  The listener.
**/
    public void addListSelectionListener (ListSelectionListener listener)
    {
        listSelectionEventSupport_.addListSelectionListener (listener);
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



/**
Adds a listener to be notified when the value of any
constrained property changes.

@param  listener  The listener.
**/
    public void addVetoableChangeListener (VetoableChangeListener listener)
    {
        super.addVetoableChangeListener (listener);
        vetoableChangeSupport_.addVetoableChangeListener (listener);
    }



/**
Collapses the specified object in the tree.

@param  object  The object in the tree.
**/
    public void collapse (VNode object)
    {
        treePane_.collapse (object);
    }



/**
Expands the specified object in the tree.

@param  object    The object in the tree.
**/
    public void expand (VNode object)
    {
        treePane_.expand (object);
    }



/**
Returns the context in which actions will be performed.

@return The action context.
**/
    public VActionContext getActionContext ()
    {
        // If an object is selected in the details, then
        // the action will be performed there, otherwise
        // it will be performed in the tree.
        if (detailsPane_.getSelectedObject () != null)
            return detailsPane_.getActionContext ();
        else
            return treePane_.getActionContext ();
    }



/**
Indicates if actions can be invoked on objects.

@return true if a actions can be invoked; false otherwise.
**/
    public boolean getAllowActions ()
    {
        return detailsPane_.getAllowActions ();
    }



/**
Indicates if certain actions are confirmed with the user.

@return true if certain actions are confirmed with the user;
        false otherwise.
**/
    public boolean getConfirm ()
    {
        return treePane_.getConfirm ();
    }



/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
    }



/**
Returns the column model that is used to maintain the columns
of the details.  This provides the ability to programmatically
add and remove columns.

@return The column model.
**/
    public TableColumnModel getDetailsColumnModel ()
    {
        return detailsPane_.getColumnModel ();
    }



/**
Returns the details model.

@return The details model.
**/
    public TableModel getDetailsModel ()
    {
        return detailsPane_.getModel ();
    }



/**
Returns the root of the details pane.

@return The root the details pane, or null
        if there is none.
**/
    public VNode getDetailsRoot ()
    {
        // This is the same as the current selection
        // in the tree pane.
        return treePane_.getSelectedObject ();
    }



/**
Returns the selection model that is used to maintain
selection state in the details.  This provides the ability
to programmatically select and deselect objects.

@return The selection model, or null if selections are
        not allowed.
**/
    public ListSelectionModel getDetailsSelectionModel ()
    {
        return detailsPane_.getSelectionModel ();
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
        return treePane_.getPath (object);
    }



/**
Returns the root, or the AS/400 resource, from which all information for the model is gathered.

@return     The root, or the AS/400 resource, from which all information for the model is gathered. It will be null if none has been set.
**/
    public VNode getRoot ()
    {
        return treePane_.getRoot ();
    }



/**
Returns the first selected object.  This is the first selected
object in the details pane.  If no objects are selected in
the details pane, then this is the object selected in the
tree pane.

@return The first selected object, or null if none are
        selected.
**/
    public VObject getSelectedObject ()
    {
        VObject selectedObject = detailsPane_.getSelectedObject ();
        if (selectedObject == null)
            selectedObject = treePane_.getSelectedObject ();
        return selectedObject;
    }



/**
Returns the selected objects.  These are the selected
objects in the details pane.  If no objects are selected in
the details pane, then this is the object selected in the
tree pane.

@return  The selected objects.
**/
    public VObject[] getSelectedObjects ()
    {
        VObject[] selectedObjects = detailsPane_.getSelectedObjects ();
        if (selectedObjects.length == 0)
            selectedObjects = treePane_.getSelectedObjects ();
        return selectedObjects;
    }



/**
Returns the tree model.

@return The tree model.
**/
    public TreeModel getTreeModel ()
    {
        return treePane_.getModel ();
    }



/**
Returns the selection model that is used to maintain
selection state in the tree.  This provides the ability to
programmatically select and deselect objects.

@return The selection model, or null if selections are not
        allowed.
**/
    public TreeSelectionModel getTreeSelectionModel ()
    {
        return treePane_.getSelectionModel ();
    }



/**
Initializes the transient data.
**/
    private void initializeTransient ()
    {
        // Initialize the event support.
        errorEventSupport_          = new ErrorEventSupport (this);
        listSelectionEventSupport_  = new ListSelectionEventSupport (this);
        propertyChangeSupport_      = new PropertyChangeSupport (this);
        treeSelectionEventSupport_  = new TreeSelectionEventSupport (this);
        vetoableChangeSupport_      = new VetoableChangeSupport (this);

        treePane_.addErrorListener (errorEventSupport_);
        treePane_.addPropertyChangeListener (propertyChangeSupport_);
        treePane_.addTreeSelectionListener (treeSelectionEventSupport_);
        treePane_.addVetoableChangeListener (vetoableChangeSupport_);

        // @A2D Do not listen to the details pane, since all
        //      relevant error events bubble up through the tree
        //      pane.
        // @A2D detailsPane_.addErrorListener (errorEventSupport_);
        detailsPane_.addListSelectionListener (listSelectionEventSupport_);
        detailsPane_.addPropertyChangeListener (propertyChangeSupport_);
        detailsPane_.addVetoableChangeListener (vetoableChangeSupport_);

        // Initialize the other adapters.
        treePane_.addTreeSelectionListener (new TreeSelectionListener_ ());
        detailsPane_.addPropertyChangeListener (new PropertyChangeListener_ ());
    }



/**
Indicates if the object in the tree is currently collapsed.

@param  object    The object in the tree.
@return true if the object is collapsed; false otherwise.
**/
    public boolean isCollapsed (VNode object)
    {
        return treePane_.isCollapsed (object);
    }



/**
Indicates if the object in the tree is currently expanded.

@param  object    The object in the tree.
@return true if the object is expanded; false otherwise.
**/
    public boolean isExpanded (VNode object)
    {
        return treePane_.isExpanded (object);
    }



/**
Indicates if the object in the tree or the details is selected.

@param  object The object in the tree or the details.
@return true if the object is selected, false otherwise.
**/
    public boolean isSelected (VObject object)
    {
        if (object == null)
            throw new NullPointerException ("object");

        VObject[] selectedObjects = getSelectedObjects ();
        for (int i = 0; i < selectedObjects.length; ++i)
            if (selectedObjects[i] == object)
                return true;
        return false;
    }



/**
Indicates if the object in the tree is currently visible in the tree.

@param  object    The object in the tree.
@return true if the object is visible; false otherwise.
**/
    public boolean isVisible (VNode object)
    {
        return treePane_.isVisible (object);
    }



/**
Loads the objects from the AS/400.
**/
    public void load ()
    {
        treePane_.load ();
        if (treePane_.getRoot() != detailsPane_.getRoot ())
            detailsPane_.load ();
    }



/**
Makes the object visible in the tree by expanding its
parent objects as needed.

@param  object    The object.
**/
    public void makeVisible (VNode object)
    {
        treePane_.makeVisible (object);
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
Removes a list selection listener.

@param  listener  The listener.
**/
    public void removeListSelectionListener (ListSelectionListener listener)
    {
        listSelectionEventSupport_.removeListSelectionListener (listener);
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
  <li>popup menu on selected object in the tree and details
  <li>double clicking on a object sets the root object in the details
</ul>

<p>The default is true.

@param allowActions true if actions are allowed; false otherwise.
**/
    public void setAllowActions (boolean allowActions)
    {
        treePane_.setAllowActions (allowActions);
        detailsPane_.setAllowActions (allowActions);
    }



/**
Sets whether certain actions are confirmed with the user.  The default
is true.

@param confirm    true if certain actions are confirmed with the
                  user; false otherwise.
**/
    public void setConfirm (boolean confirm)
    {
        treePane_.setConfirm (confirm);
        detailsPane_.setConfirm (confirm);
    }



/**
Sets the selection model that is used to maintain selection
state in the details.  This provides the ability to
programmatically select and deselect objects.

@param  selectionModel  The selection model, or null if
                        selections are not allowed.
**/
    public void setDetailsSelectionModel (ListSelectionModel selectionModel)
    {
        detailsPane_.setSelectionModel (selectionModel);
    }



/**
Sets the root, or the AS/400 resource, from which all information for the model is gathered. It will not take effect until load() is done.

@param  root    The root, or the AS/400 resource, from which all information for the model is gathered.

@exception PropertyVetoException It the change is vetoed.
**/
    public void setRoot (VNode root)
        throws PropertyVetoException
    {
        if (root == null)
            throw new NullPointerException ("root");

        treePane_.setRoot (root);
    }



/**
Sets the selection model that is used to maintain selection
state in the tree.  This provides the ability to programmatically
select and deselect objects.

@param  selectionModel  The selection model, or null if
                        selections are not allowed.
**/
    public void setTreeSelectionModel (TreeSelectionModel selectionModel)
    {
        treePane_.setSelectionModel (selectionModel);
    }



/**
Sorts the contents. The propertyIdentifer[0], orders[0] combination  is used to do the sort. If the values are equal, propertyIdentifier[1], orders[1]  is used to break the tie, and so forth.

@param  propertyIdentifiers The property identifiers.  If any of
                            the property identifiers are null, it
                            means to sort using the string
                            representation of the object.
@param  orders              The sort orders for each property
                            identifier, true for ascending order,
                            false for descending order.
**/
    public void sort (Object[] propertyIdentifiers, boolean[] orders)
    {
        detailsPane_.sort (propertyIdentifiers, orders);
    }



/**
The PropertyChangeListener_ class listens to the details pane
for changes in its object.  When this happens it will set the
selection in the tree pane accordingly.
**/
    private class PropertyChangeListener_
    implements PropertyChangeListener
    {
        public void propertyChange (PropertyChangeEvent event)
        {
            if ((event.getSource () == detailsPane_)
                && (event.getPropertyName () == "root")) {

                VNode object = detailsPane_.getRoot ();
                treePane_.makeVisible (object);
                treePane_.getSelectionModel ().setSelectionPath (treePane_.getPath (object));
            }
        }

        private String getCopyright ()             { return Copyright_v.copyright; }
    }


/**
The TreeSelectionListener_ class listens to the tree pane for
selections.  When this happens it will set the contents of the
details pane.
**/
    private class TreeSelectionListener_
    implements TreeSelectionListener
    {
        public void valueChanged (TreeSelectionEvent event)
        {
            VNode object = (VNode) event.getPath ().getLastPathComponent ();

            // Select the object in the details pane.
            try {
                detailsPane_.setRoot (object);  
                               
                // @A1D When this load happens, the tree was not being
                //      updated, so the tree sometimes listed the children
                //      in a different order than the details.  (To 
                //      reproduce, create a new child on the tree side,
                //      then click on the parent.)  By not loading,
                //      the tree and details are always consistent, and
                //      I have not found any side effects.
                //
                // @A1D detailsPane_.load ();
            }
            catch (PropertyVetoException e) {
                // Ignore.
            }
        }

        private String getCopyright ()             { return Copyright_v.copyright; }
    }


}
