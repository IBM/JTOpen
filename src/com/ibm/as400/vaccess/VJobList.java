///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VJobList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.Trace;
import com.ibm.as400.resource.ResourceException;
import com.ibm.as400.resource.RJob;
import com.ibm.as400.resource.RJobList;
import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.tree.TreeNode;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

/**
The VJobList class defines the representation of a job list on
a server for use in various models and panes in this package.
You must explicitly call load() to load the information from
the server.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>VJobList objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>PropertyChangeEvent
    <li>VObjectEvent
    <li>WorkingEvent
</ul>
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class VJobList
implements VNode, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static String           description_            = ResourceLoader.getText ("JOB_LIST_DESCRIPTION");
    private static Icon             icon16_                 = ResourceLoader.getIcon ("VJobList16.gif", description_);
    private static Icon             icon32_                 = ResourceLoader.getIcon ("VJobList32.gif", description_);
    private static String           cpuUsedColumnHeader_    = ResourceLoader.getText ("JOB_CPU_USED");
    private static String           dateColumnHeader_       = ResourceLoader.getText ("JOB_DATE");
    private static String           functionColumnHeader_   = ResourceLoader.getText ("JOB_FUNCTION");
    private static String           nameColumnHeader_       = ResourceLoader.getText ("JOB_NAME");
    private static String           numberColumnHeader_     = ResourceLoader.getText ("JOB_NUMBER");
    private static String           statusColumnHeader_     = ResourceLoader.getText ("JOB_STATUS");
    private static String           subsystemColumnHeader_  = ResourceLoader.getText ("JOB_SUBSYSTEM");
    private static String           subtypeColumnHeader_    = ResourceLoader.getText ("JOB_SUBTYPE");
    private static String           typeColumnHeader_       = ResourceLoader.getText ("JOB_TYPE");
    private static String           userColumnHeader_       = ResourceLoader.getText ("JOB_USER");



    // Properties.
    private RJobList                jobList_            = null;
    private String                  name_               = null;
    private String                  number_             = null;
    private VNode                   parent_             = null;
    private String                  user_               = null;



    // Static data.
    private static TableColumnModel detailsColumnModel_     = null;



    // Private data.
    transient private VNode[]           children_;
    transient private Object            childrenLock_;
    transient private boolean           loaded_             = false;
    transient private VPropertiesPane   propertiesPane_;
             


    // Event support.
    transient private ErrorEventSupport           errorEventSupport_;
    transient private PropertyChangeSupport       propertyChangeSupport_;
    transient private VetoableChangeSupport       vetoableChangeSupport_;
    transient private VObjectEventSupport         objectEventSupport_;
    transient private WorkingEventSupport         workingEventSupport_;



/**
Static initializer.
**/
//
// Implementation note:
//
// * The column widths are completely arbitrary.
//
    static
    {
        detailsColumnModel_ = new DefaultTableColumnModel ();
        int columnIndex = 0;

        /* @C2D
        // Subsystem column.
        VTableColumn subsystemColumn = new VTableColumn (columnIndex++, VJob.SUBSYSTEM_PROPERTY);
        subsystemColumn.setCellRenderer (new VObjectCellRenderer ());
        subsystemColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        subsystemColumn.setHeaderValue (subsystemColumnHeader_);
        subsystemColumn.setPreferredCharWidth (10);
        detailsColumnModel_.addColumn (subsystemColumn);
        */

        // Name column.
        VTableColumn nameColumn = new VTableColumn (columnIndex++, VJob.NAME_PROPERTY);
        nameColumn.setCellRenderer (new VObjectCellRenderer ());
        nameColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        nameColumn.setHeaderValue (nameColumnHeader_);
        nameColumn.setPreferredCharWidth (10);
        detailsColumnModel_.addColumn (nameColumn);

        // User column.
        VTableColumn userColumn = new VTableColumn (columnIndex++, VJob.USER_PROPERTY);
        userColumn.setCellRenderer (new VObjectCellRenderer ());
        userColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        userColumn.setHeaderValue (userColumnHeader_);
        userColumn.setPreferredCharWidth (10);
        detailsColumnModel_.addColumn (userColumn);

        // Type column.
        VTableColumn typeColumn = new VTableColumn (columnIndex++, VJob.TYPE_PROPERTY);
        typeColumn.setCellRenderer (new VObjectCellRenderer ());
        typeColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        typeColumn.setHeaderValue (typeColumnHeader_);
        typeColumn.setPreferredCharWidth (10);
        detailsColumnModel_.addColumn (typeColumn);

        /* @C2D
        // CPU used column.
        VTableColumn cpuUsedColumn = new VTableColumn (columnIndex++, VJob.CPUUSED_PROPERTY);
        cpuUsedColumn.setCellRenderer (new VObjectCellRenderer (SwingConstants.RIGHT));
        cpuUsedColumn.setHeaderRenderer (new VObjectHeaderRenderer (SwingConstants.RIGHT));
        cpuUsedColumn.setHeaderValue (cpuUsedColumnHeader_);
        cpuUsedColumn.setPreferredCharWidth (10);
        detailsColumnModel_.addColumn (cpuUsedColumn);

        // Function column.
        VTableColumn functionColumn = new VTableColumn (columnIndex++, VJob.FUNCTION_PROPERTY);
        functionColumn.setCellRenderer (new VObjectCellRenderer ());
        functionColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        functionColumn.setHeaderValue (functionColumnHeader_);
        functionColumn.setPreferredCharWidth (20);
        detailsColumnModel_.addColumn (functionColumn);
        */

        // Status column.
        VTableColumn statusColumn = new VTableColumn (columnIndex++, VJob.STATUS_PROPERTY);
        statusColumn.setCellRenderer (new VObjectCellRenderer ());
        statusColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        statusColumn.setHeaderValue (statusColumnHeader_);
        statusColumn.setPreferredCharWidth (10);
        detailsColumnModel_.addColumn (statusColumn);

        // Number column.
        VTableColumn numberColumn = new VTableColumn (columnIndex++, VJob.NUMBER_PROPERTY);
        numberColumn.setCellRenderer (new VObjectCellRenderer ());
        numberColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        numberColumn.setHeaderValue (numberColumnHeader_);
        numberColumn.setPreferredCharWidth (6);
        detailsColumnModel_.addColumn (numberColumn);

        /* @C2D
        // Date column.
        VTableColumn dateColumn = new VTableColumn (columnIndex++, VJob.DATE_PROPERTY);
        dateColumn.setCellRenderer (new VObjectCellRenderer ());
        dateColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        dateColumn.setHeaderValue (dateColumnHeader_);
        dateColumn.setPreferredCharWidth (20);
        detailsColumnModel_.addColumn (dateColumn);
        */
    }



/**
Constructs a VJobList object.
**/
    public VJobList ()
    {
        jobList_ = new RJobList ();
        try {
            jobList_.setSelectionValue(RJobList.PRIMARY_JOB_STATUSES, new String[] { RJob.JOB_STATUS_ACTIVE });
            jobList_.setSortValue(new Object[] {RJob.JOB_NAME, RJob.USER_NAME, RJob.JOB_NUMBER }); // @C3A
        }
        catch(ResourceException e) { }
        initializeTransient ();
    }



/**
Constructs a VJobList object.

@param system   The system on which the list resides.
**/
    public VJobList (AS400 system)
    {
        if (system == null)
            throw new NullPointerException ("system");

        jobList_ = new RJobList (system);
        try {
            jobList_.setSelectionValue(RJobList.PRIMARY_JOB_STATUSES, new String[] { RJob.JOB_STATUS_ACTIVE });
            jobList_.setSortValue(new Object[] {RJob.JOB_NAME, RJob.USER_NAME, RJob.JOB_NUMBER }); // @C3A
        }
        catch(ResourceException e) { }
        initializeTransient ();
    }



/**
Constructs a VJobList object.

@param parent   The parent.
@param system   The system on which the list resides.
**/
    public VJobList (VNode parent, AS400 system)
    {
        if (parent == null)
            throw new NullPointerException ("parent");
        if (system == null)
            throw new NullPointerException ("system");

        parent_  = parent;
        jobList_ = new RJobList (system);
        try {
            jobList_.setSelectionValue(RJobList.PRIMARY_JOB_STATUSES, new String[] { RJob.JOB_STATUS_ACTIVE });
            jobList_.setSortValue(new Object[] {RJob.JOB_NAME, RJob.USER_NAME, RJob.JOB_NUMBER }); // @C3A
        }
        catch(ResourceException e) { }
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
Adds a listener to be notified when the value of any
constrained property changes.

@param  listener  The listener.
**/
    public void addVetoableChangeListener (VetoableChangeListener listener)
    {
        vetoableChangeSupport_.addVetoableChangeListener (listener);
    }



/**
Adds a listener to be notified when a VObject is changed,
created, or deleted.

@param  listener    The listener.
**/
    public void addVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener (listener);
    }



/**
Adds a listener to be notified when work starts and stops
on potentially long-running operations.

@param  listener    The listener.
**/
    public void addWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener (listener);
    }



/**
Returns the children of the node.

@return         The children.
**/
    public Enumeration children ()
    {
        return new VEnumeration (this);
    }



/**
Returns the list of actions that can be performed.

@return Always null.  There are no actions.
**/
    public VAction[] getActions ()
    {
        return null;
    }



/**
Indiciates if the node allows children.

@return  Always true.
**/
    public boolean getAllowsChildren ()
    {
        return true;
    }



/**
Returns the child node at the specified index.

@param  index   The index.
@return         The child, or null if the index is not
                valid.
**/
    public TreeNode getChildAt (int index)
    {
        if (!loaded_)
            return null;
        if (children_ == null)
            loadFirst();
        if ((index < 0) || (index >= children_.length))
            return null;
        if (children_[index] == null)
            loadSingle(index);
        return children_[index];
    }                                



/**
Returns the number of children.

@return  The number of children.
**/
    public int getChildCount ()
    {
        if (!loaded_)
            return 0;
        if (children_ == null)
            loadFirst();
        return children_.length;
    }




/**
Returns the default action.

@return Always null.  There is no default action.
**/
    public VAction getDefaultAction ()
    {
        return null;
    }



/**
Returns the child for the details at the specified index.

@param  index   The index.
@return         The child, or null if the index is not
                valid.
**/
    public VObject getDetailsChildAt (int index)
    {
        return (VObject)getChildAt(index);
    }



/**
Returns the number of children for the details.

@return  The number of children for the details.
**/
    public int getDetailsChildCount ()
    {
        return getChildCount();
    }



/**
Returns the table column model to use in the details
when representing the children.  This column model
describes the details values for the children.

@return The details column model.
**/
    public TableColumnModel getDetailsColumnModel ()
    {
        return detailsColumnModel_;
    }



/**
Returns the index of the specified child for the details.

@param  detailsChild   The details child.
@return                The index, or -1 if the child is not found
                       in the details.
**/
    public int getDetailsIndex (VObject detailsChild)
    {
        if (detailsChild instanceof TreeNode)
            return getIndex((TreeNode)detailsChild);
        else
            return -1;
    }




/**
Returns the icon.

@param  size    The icon size, either 16 or 32.  If any other
                value is given, then return a default.
@param  open    This parameter has no effect.
@return         The icon.
**/
    public Icon getIcon (int size, boolean open)
    {
        if (size == 32)
            return icon32_;
        else
            return icon16_;
    }



/**
Returns the index of the specified child.

@param  child   The child.
@return         The index, or -1 if the child is not found.
**/
    public int getIndex (TreeNode child)
    {
        if (!loaded_)
            return -1;
        if (child == null)
            return -1;
        if (children_ == null)
            loadFirst();
        for (int i = 0; i < children_.length; ++i)
            if (children_[i] == child)
                return i;
        return -1;
    }



/**
Returns the job name.

@return The job name.

@see com.ibm.as400.access.JobList#getName
**/
    public String getName ()
    {
        if (name_ == null)
            return RJobList.ALL;
        else
            return name_;
    }



/**
Returns the job number.

@return The job number.

@see com.ibm.as400.access.JobList#getNumber
**/
    public String getNumber ()
    {
        if (number_ == null)
            return RJobList.ALL;
        else
            return number_;
    }



/**
Returns the parent node.

@return The parent node, or null if there is no parent.
**/
    public TreeNode getParent ()
    {
        return parent_;
    }



/**
Returns the properties pane.

@return The properties pane.
**/
    public VPropertiesPane getPropertiesPane ()
    {
        return propertiesPane_;
    }



/**
Returns a property value.

@param      propertyIdentifier  The property identifier.  The choices are
                                NAME_PROPERTY and DESCRIPTION_PROPERTY.
@return                         The property value, or null if the
                                property identifier is not recognized.
**/
    public Object getPropertyValue (Object propertyIdentifier)
    {
        // Get the name.
        if (propertyIdentifier == NAME_PROPERTY)
            return this;

        // Get the description.
        else if (propertyIdentifier == DESCRIPTION_PROPERTY)
            return description_;

        // By default, return null.
        return null;
    }



/**
Returns the system on which the list resides.

@return The system on which the list resides.
**/
    public AS400 getSystem ()
    {
        return jobList_.getSystem ();
    }



/**
Returns the text.

@return The text.
**/
    public String getText ()
    {
        return description_;
    }



/**
Returns the user name.

@return The user name.

@see com.ibm.as400.access.JobList#getUser
**/
    public String getUser ()
    {
        if (user_ == null)
            return RJobList.ALL;
        else
            return user_;
    }



/**
Initializes the transient data.
**/
    private void initializeTransient ()
    {
        // Initialize the event support.
        errorEventSupport_      = new ErrorEventSupport (this);
        objectEventSupport_     = new VObjectEventSupport (this);
        propertyChangeSupport_  = new PropertyChangeSupport (this);
        vetoableChangeSupport_  = new VetoableChangeSupport (this);
        workingEventSupport_    = new WorkingEventSupport (this);

        jobList_.addPropertyChangeListener (propertyChangeSupport_);
        jobList_.addVetoableChangeListener (vetoableChangeSupport_);

        // Initialize the private data.
        children_               = new VNode[0];
        childrenLock_           = new Object();

        // Initialize the properties pane.
        propertiesPane_ = new VResourceListPropertiesPane(this, jobList_);

        propertiesPane_.addErrorListener (errorEventSupport_);
        propertiesPane_.addVObjectListener (objectEventSupport_);
        propertiesPane_.addWorkingListener (workingEventSupport_);
    }



/**
Indicates if the node is a leaf.

@return  true if the node if a leaf; false otherwise.
**/
    public boolean isLeaf ()
    {
        return (getChildCount () == 0);
    }



/**
Indicates if the details children are sortable.
@return Always false.
**/
    public boolean isSortable ()
    {
        return false; //@A2C @C1C
    }



/**
Loads information about the object from the server.
**/
    public void load()
    {
        synchronized(childrenLock_) {
        
            // Stop listening to the previous children.
            if (children_ != null) {
                for (int i = 0; i < children_.length; ++i) { //@A2C
                    if (children_[i] != null) {
                        children_[i].removeErrorListener (errorEventSupport_);
                        children_[i].removeVObjectListener (objectEventSupport_);
                        children_[i].removeWorkingListener (workingEventSupport_);
                    }
                }
            }

            children_ = null;
            loaded_ = true; //@A2A
        }

        objectEventSupport_.fireObjectChanged(this, true);
    }



    private void loadFirst()
    {
        workingEventSupport_.fireStartWorking ();
        Exception e = null;
        synchronized(childrenLock_) {
            if (children_ == null) {
                try {
                    // @C3D jobList_.setSortValue(new Object[] {RJob.JOB_NAME, RJob.USER_NAME, RJob.JOB_NUMBER });
                    jobList_.refreshContents();
                    jobList_.waitForComplete();
                    children_ = new VJob[(int)jobList_.getListLength()];
                } 
                catch (Exception e2)  {
                    e = e2;
                    children_ = new VJob[0]; // Avoid future errors.
                }
            }
        }
        if (e != null)
            errorEventSupport_.fireError(e);
        workingEventSupport_.fireStopWorking ();
    }



    private void loadSingle(int index)
    {
        workingEventSupport_.fireStartWorking ();
        Exception e = null;
        synchronized(childrenLock_) {
            if (children_[index] == null) {
                try {
                    RJob job = (RJob)jobList_.resourceAt(index);
                    children_[index] = new VJob(this, jobList_.getSystem(), job);
                    children_[index].addErrorListener (errorEventSupport_);
                    children_[index].addVObjectListener (objectEventSupport_);
                    children_[index].addWorkingListener (workingEventSupport_);
                    children_[index].load();
                } 
                catch (Exception e2)  {
                    e = e2;
                    children_ = new VJob[0]; // Avoid future errors.
                }
            }
        }
        if (e != null)
            errorEventSupport_.fireError(e);
        workingEventSupport_.fireStopWorking ();
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
Removes a vetoable change listener.

@param  listener  The listener.
**/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        vetoableChangeSupport_.removeVetoableChangeListener (listener);
    }



/**
Removes a VObjectListener.

@param  listener    The listener.
**/
    public void removeVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.removeVObjectListener (listener);
    }



/**
Removes a working listener.

@param  listener    The listener.
**/
    public void removeWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener (listener);
    }



/**
Sets the job name.  The default is *ALL.

@param name The job name, or *ALL to list jobs with any name.

@exception PropertyVetoException If the change is vetoed.

@see com.ibm.as400.access.JobList#setName
**/
    public void setName (String name)
        throws PropertyVetoException
    {
        if (name == null)
            throw new NullPointerException ("name");

        String oldValue = name_;
        vetoableChangeSupport_.fireVetoableChange("name", oldValue, name);
        name_ = name;
        try {
            jobList_.setSelectionValue(RJobList.JOB_NAME, name_);
        }
        catch(ResourceException e) {
            errorEventSupport_.fireError(e);
        }
        propertyChangeSupport_.firePropertyChange("name", oldValue, name);
    }



/**
Sets the job number.  The default is *ALL.

@param number The job number, or *ALL to list jobs with any number.

@exception PropertyVetoException If the change is vetoed.

@see com.ibm.as400.access.JobList#setNumber
**/
    public void setNumber (String number)
        throws PropertyVetoException
    {
        if (number == null)
            throw new NullPointerException ("number");

        String oldValue = number_;
        vetoableChangeSupport_.fireVetoableChange("number", oldValue, number);
        number_ = number;      
        try {
            jobList_.setSelectionValue(RJobList.JOB_NUMBER, number_);
        }
        catch(ResourceException e) {
            errorEventSupport_.fireError(e);
        }
        propertyChangeSupport_.firePropertyChange("number", oldValue, number);
    }



/**
Sets the system on which the list resides.

@param system The system on which the list resides.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setSystem (AS400 system)
        throws PropertyVetoException
    {
        if (system == null)
            throw new NullPointerException ("system");

        AS400 oldValue = jobList_.getSystem();
        vetoableChangeSupport_.fireVetoableChange("system", oldValue, system);
        jobList_.setSystem (system);
        propertyChangeSupport_.firePropertyChange("system", oldValue, system);
    }



/**
Sets the job user.  The default is *ALL.

@param user The job user.

@exception PropertyVetoException If the change is vetoed.

@see com.ibm.as400.access.JobList#setUser
**/
    public void setUser (String user)
        throws PropertyVetoException
    {
        if (user == null)
            throw new NullPointerException ("user");

        String oldValue = user_;
        vetoableChangeSupport_.fireVetoableChange("user", oldValue, user);
        user_ = user;
        try {
            jobList_.setSelectionValue(RJobList.USER_NAME, user_);
        }
        catch(ResourceException e) {
            errorEventSupport_.fireError(e);
        }
        propertyChangeSupport_.firePropertyChange("user", oldValue, user);
    }



/**
Sorts the children for the details.
@param  propertyIdentifiers The property identifiers.
@param  orders              The sort orders for each property
                            identifier. true for ascending order;
                            false for descending order.
**/
    public void sortDetailsChildren (Object[] propertyIdentifiers,
                                                  boolean[] orders)
    {
//@A2D        if (propertyIdentifiers == null)
//@A2D            throw new NullPointerException ("propertyIdentifiers");
//@A2D        if (orders == null)
//@A2D            throw new NullPointerException ("orders");
        if (propertyIdentifiers == null)                            //@B0A
            throw new NullPointerException ("propertyIdentifiers"); //@B0A
        if (orders == null)                                         //@B0A
            throw new NullPointerException ("orders");              //@B0A
        // @C1D VUtilities.sort(children_, propertyIdentifiers, orders); //@A2A
    }



/**
Returns the string representation of the description.

@return The string representation of the description.
**/
    public String toString ()
    {
        return description_;
    }
}
