///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400TreeModel.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;



/**
The AS400TreeModel class implements an underlying model for
a tree, where all information for the tree is gathered from
the hierarchy of objects rooted at a server resource.
You must explicitly call load() to load the information from
the server.

<p>Use this class if you want to customize the graphical
user interface that presents a tree.  If you do not need
to customize the interface, then use AS400TreePane instead.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>AS400TreeModel objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>PropertyChangeEvent
    <li>TreeModelEvent
    <li>WorkingEvent
</ul>

<p>The following example creates a tree model filled with
the list of printers on a server.  It then presents the tree
in a JTree object.

<pre>
//Set up the tree model and JTree.
AS400 system = new AS400 ("MySystem", "Userid", "Password");
VPrinters printers = new VPrinters (system);
AS400TreeModel treeModel = new AS400TreeModel (printers);
treeModel.load ();
JTree tree = new JTree (treeModel);
<br>
// Add the JTree to a frame.
JFrame frame = new JFrame ("My Window");
frame.getContentPane().add(new JScrollPane(tree));


</pre>

@see AS400TreePane
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class AS400TreeModel
implements TreeModel, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Properties.
    VNode           root_                   = null; // Private.



    // Event support.
    transient private ErrorEventSupport       errorEventSupport_;
    transient private VObjectListener         objectListener_;
    transient private PropertyChangeSupport   propertyChangeSupport_;
    transient         TreeModelEventSupport   treeModelEventSupport_; // Private.
    transient private VetoableChangeSupport   vetoableChangeSupport_;
    transient private WorkingEventSupport     workingEventSupport_;



/**
Constructs an AS400TreeModel object.
**/
    public AS400TreeModel ()
    {
        initializeTransient ();
    }



/**
Constructs an AS400TreeModel object.

@param  root    The root, or the server resource, from which all information for the model is gathered.
**/
    public AS400TreeModel (VNode root)
    {
        if (root == null)
            throw new NullPointerException ("root");

        root_ = root;
        initializeTransient ();
    }



/**
Adds a listener to be notified when an error occurs.

@param  listener    The listener.
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
        propertyChangeSupport_.addPropertyChangeListener (listener);
    }




/**
Adds a listener to be notified when the contents of
the tree change.

@param  listener  The listener.
**/
    public void addTreeModelListener (TreeModelListener listener)
    {
        treeModelEventSupport_.addTreeModelListener (listener);
    }



/**
Adds a listener to be notified when the value of any
constrained property changes.

@param  listener  The listener.
**/
    public void addVetoableChangeListener (VetoableChangeListener listener)
    {
        vetoableChangeSupport_.addVetoableChangeListener (listener);
    }



/**
Adds a listener to be notified when work starts and stops
on potentially long-running operations.

@param  listener  The listener.
**/
    public void addWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener (listener);
    }



/**
Returns a child of the parent.

@param  parent  The parent.
@param  index   The index of the child.
@return         The child. It will be null if the parent is
                not a valid object or if the index is not
                valid for the parent.
**/
    public Object getChild (Object parent, int index)
    {
        // Validate the parent.
        if (parent == null)
            return null;
        if (! (parent instanceof VNode))
            return null;

        // Validate the index.
        int childCount = ((VNode) parent).getChildCount ();
        if ((index < 0) || (index >= childCount))
            return null;

        return ((VNode) parent).getChildAt (index);
    }



/**
Returns the number of children of the parent.

@param  parent  The parent.
@return         The number of children of the parent, or
                0 if the parent is not a valid object.
**/
    public int getChildCount (Object parent)
    {
        // Validate the parent.
        if (parent == null)
            return 0;
        if (! (parent instanceof VNode))
            return 0;

        return ((VNode) parent).getChildCount ();
    }



/**
Returns the index of a child in the parent.

@param parent   The parent.
@param child    The child.
@return         The index of the child in the parent. It will be -1
                if the parent or child are not valid objects or
                if the child is not in the parent.
**/
    public int getIndexOfChild (Object parent, Object child)
    {
        // Validate the parent.
        if (parent == null)
            return -1;
        if (! (parent instanceof VNode))
            return -1;

        // Validate the child.
        if (child == null)
            return -1;
        if (! (child instanceof VNode))
            return -1;

        return ((VNode) parent).getIndex ((VNode) child);
    }



/**
Returns the path from the root to get to this object in the tree
hierarchy. The last element in the path will be this object.

@param  object  The object.
@return         The path, or null if the object is not a valid
                object.
**/
    public TreePath getPath (Object object)
    {
        // Validate the object.
        if (object == null)
            return null;
        if (! (object instanceof VNode))
            return null;

        Vector pathV = new Vector ();
        for (TreeNode i = (VNode) object; i != null; i = i.getParent ())
            pathV.insertElementAt (i, 0);
        TreeNode[] path = new TreeNode[pathV.size ()];
        pathV.copyInto (path);
        return new TreePath (path);
    }



/**
Returns the root, or the server resource, from which all information for the model is gathered.

@return The root, or the server resource, from which all information for the model is gathered. It will be null if none has been set.
**/
    public Object getRoot ()
    {
        return root_;
    }



/**
Initializes transient data.
**/
    private void initializeTransient ()
    {
        errorEventSupport_      = new ErrorEventSupport (this);
        objectListener_         = new VObjectListener_ ();
        propertyChangeSupport_  = new PropertyChangeSupport (this);
        treeModelEventSupport_  = new TreeModelEventSupport (this);
        vetoableChangeSupport_  = new VetoableChangeSupport (this);
        workingEventSupport_    = new WorkingEventSupport (this);

        if (root_ != null) {
            root_.addErrorListener (errorEventSupport_);
            root_.addVObjectListener (objectListener_);
            root_.addWorkingListener (workingEventSupport_);
        }
    }



/**
Indicates if the object is a leaf in the tree.

@param  object  The object.
@return         true if the object is a leaf or the object
                is not a valid object; false if the
                object is not a leaf.
**/
    public boolean isLeaf (Object object)
    {
        // Validate the object.
        if (object == null)
            return true;
        if (! (object instanceof VNode))
            return true;

        return ((VNode) object).isLeaf ();
    }


/**
Loads the information from the server.
**/
    public void load ()
    {
        if (root_ != null)
            root_.load ();

        treeModelEventSupport_.fireTreeStructureChanged (getPath (root_));
    }



/**
Restores the state of the object from an input stream.
This is used when deserializing an object.

@param in   The input stream.
**/
    private void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject ();
        initializeTransient ();
    }



/**
Removes an error listener.

@param  listener    The listener.
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
        propertyChangeSupport_.removePropertyChangeListener (listener);
    }



/**
Removes a tree model listener.

@param  listener  The listener.
**/
    public void removeTreeModelListener (TreeModelListener listener)
    {
        treeModelEventSupport_.removeTreeModelListener (listener);
    }



/**
Removes a vetoable change listener.

@param  listener  The listener.
**/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        vetoableChangeSupport_.removeVetoableChangeListener (listener);
    }



/**
Removes a working listener.

@param  listener  The listener.
**/
    public void removeWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener (listener);
    }



/**
Sets the root, or the server resource, from which all information
for the model is gathered. It will not take effect until load() is done.

@param  root     The root, or the server resource, from which all
                 information for the model is gathered.  This must
                 be a VNode.

@exception PropertyVetoException If the change is vetoed.
**/
//
// Implementation note:  This method takes an Object and not a VNode
//                       so that it matches getObject().  Otherwise
//                       we get a beans IntrospectionException when
//                       trying to introspect the bean.
//
//                       getObject() returns Object in order to implement
//                       the TreeModel interface.
//
    public void setRoot (Object root)
        throws PropertyVetoException
    {
        if (root == null)
            throw new NullPointerException ("root");

        // If they did pass a VNode, this will throw a class cast
        // exception.

        VNode oldValue = root_;
        VNode newValue = (VNode) root;
        vetoableChangeSupport_.fireVetoableChange ("root", oldValue, newValue);

        if (oldValue != newValue) {

            // Redirect event support.
            if (oldValue != null) {
                oldValue.removeErrorListener (errorEventSupport_);
                oldValue.removeVObjectListener (objectListener_);
                oldValue.removeWorkingListener (workingEventSupport_);
            }
            newValue.addErrorListener (errorEventSupport_);
            newValue.addVObjectListener (objectListener_);
            newValue.addWorkingListener (workingEventSupport_);

            // Set the root.
            root_ = newValue;
            treeModelEventSupport_.fireTreeStructureChanged (getPath (root_));
        }

        propertyChangeSupport_.firePropertyChange ("root", oldValue, newValue);
    }



/**
Notifies the object that the value for the item identified by path
has changed.

@param  path    The path of the item containing the new value.
@param  value   The new value.
**/
    public void valueForPathChanged (TreePath path, Object value)
    {
        // Ignore.  We control all changes.
    }




/**
Listens for explorer events and adjusts the model accordingly.
**/
    private class VObjectListener_
    implements VObjectListener, Serializable
    {



        public void objectChanged (VObjectEvent event)
        {
            VObject object = event.getObject ();

            if (object != null) {                                   // @A1C
                if (! event.isDuringLoad ())                        // @A1A
                    object.load ();                                 

                if (object instanceof VNode) {
                    treeModelEventSupport_.fireTreeStructureChanged (getPath (object));
    
                    VNode parent = (VNode) ((VNode) object).getParent ();
                    if (parent != null) {
                        TreePath path = getPath (parent);
                        int index = parent.getIndex ((VNode) object);
                        if (index >= 0)
                            treeModelEventSupport_.fireTreeNodesChanged (path,
                                index, object);
                    }
                }
            }                                                       // @A1A
        }



        public void objectCreated (VObjectEvent event)
        {
            VObject object = event.getObject ();
            VNode parent = event.getParent ();

            if ((parent != null) && (object instanceof VNode)) {
                int index = parent.getIndex ((VNode) object);
                treeModelEventSupport_.fireTreeNodesInserted (getPath (parent),
                    index, object);
            }
        }



        public void objectDeleted (VObjectEvent event)
        {
            VObject object = event.getObject ();

            if (object instanceof VNode) {
                VNode parent = (VNode) ((VNode) object).getParent ();
                if (parent == null)
                    treeModelEventSupport_.fireTreeNodesRemoved (null, 0, object);
                else {
                    int index = parent.getIndex ((VNode) object);
                    if (index >= 0)
                        treeModelEventSupport_.fireTreeNodesRemoved (getPath (parent),
                            index, object);
                }
            }
        }


    }
}
