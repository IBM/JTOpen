///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VPrinters.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

// Toolbox imports
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.Printer;
import com.ibm.as400.access.PrinterList;
import com.ibm.as400.access.PrintObject;
import com.ibm.as400.access.Trace;

// swing imports
import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreeNode;

// awt imports
import java.awt.Component;
import java.awt.GridLayout;

// Java beans imports
import java.beans.PropertyChangeListener;
//import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
//import java.beans.VetoableChangeSupport;

// Java imports
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;


/**
The VPrinters class represents
a list of system printers for use in various models
and panes in this package.

<p>Both the children and details children of a VPrinters
object are the printers (VPrinter objects) in this list.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>VPrinters objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>PropertyChangeEvent
    <li>VObjectEvent
    <li>WorkingEvent
</ul>
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class VPrinters
implements VNode, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // MRI.
    private static String description_                = ResourceLoader.getPrintText("AS400_PRINTERS");
    private static String descriptionColumnHeader_    = ResourceLoader.getPrintText ("DESCRIPTION");
    private static Icon   icon16_                     = ResourceLoader.getIcon ("VPrinters16.gif", description_);
    private static Icon   icon32_                     = ResourceLoader.getIcon ("VPrinters32.gif", description_);
    private static String name_                       = ResourceLoader.getPrintText ("PRINTERS");
    private static String outputQueueColumnHeader_    = ResourceLoader.getPrintText ("OUTPUT_QUEUE");
    private static String printerColumnHeader_        = ResourceLoader.getPrintText ("PRINTER");
    private static String statusColumnHeader_         = ResourceLoader.getPrintText ("STATUS");

    private static TableColumnModel detailsColumnModel_     = null;
    private static Object           detailsColumnModelLock_ = new Object();

    // Default column widths
    private static final int    dfltPrinterColWidth = 15;
    private static final int    dfltStatusColWidth = 25;
    private static final int    dfltDescriptionColWidth = 25;
    private static final int    dfltOutputQueueColWidth = 12;

    // Attributes to retreive
    private static int  attrsToRetrieve[] =
    {
        PrintObject.ATTR_AFP,
        PrintObject.ATTR_ALIGNFORMS,
        PrintObject.ATTR_ALWDRTPRT,
        PrintObject.ATTR_CHANGES,
        PrintObject.ATTR_DESCRIPTION,
        PrintObject.ATTR_DEVSTATUS,
        PrintObject.ATTR_DEVTYPE,
        PrintObject.ATTR_DRWRSEP,
        PrintObject.ATTR_PRINTER,
        PrintObject.ATTR_FILESEP,
        PrintObject.ATTR_FORMTYPE,
        PrintObject.ATTR_MESSAGE_QUEUE,
        PrintObject.ATTR_OUTPUT_QUEUE,
        PrintObject.ATTR_OUTQSTS,
        PrintObject.ATTR_OVERALLSTS,
        PrintObject.ATTR_PRTDEVTYPE,
        PrintObject.ATTR_STARTEDBY,
        PrintObject.ATTR_WTRAUTOEND,
        PrintObject.ATTR_WTRJOBNAME,
        PrintObject.ATTR_WTRJOBNUM,
        PrintObject.ATTR_WTRJOBSTS,
        PrintObject.ATTR_WTRJOBUSER,
        PrintObject.ATTR_WTRSTRTD, // @A1A
        PrintObject.ATTR_WRTNGSTS, // @A1A
        PrintObject.ATTR_WTNGMSGSTS, // @A1A
        PrintObject.ATTR_HELDSTS, // @A1A
        PrintObject.ATTR_ENDPNDSTS, // @A1A
        PrintObject.ATTR_HOLDPNDSTS, // @A1A
        PrintObject.ATTR_BTWNFILESTS, // @A1A
        PrintObject.ATTR_BTWNCPYSTS, // @A1A
        PrintObject.ATTR_WTNGDATASTS, // @A1A
        PrintObject.ATTR_WTNGDEVSTS, // @A1A
        PrintObject.ATTR_ONJOBQSTS // @A1A
    };

    // Properites
    private PrinterList         list_ = null;               //@A3C

    // Private data.
    transient private VObject[]           children_;
    transient private boolean             childrenLoaded_;
    transient private boolean             childrenLoading_; //@A4A
    transient private VNode               parent_;
    transient private VPropertiesPane     propertiesPane_;
    transient private VPrinters           thisPointer_;

    // Event support.
    transient private ErrorEventSupport           errorEventSupport_;
    transient private VObjectEventSupport         objectEventSupport_;
    transient private PropertyChangeSupport       propertyChangeSupport_;
    transient private VetoableChangeSupport       vetoableChangeSupport_;
    transient private WorkingEventSupport         workingEventSupport_;

/**
Constructs a VPrinters object.
**/
    public VPrinters()
    {
        list_ = new PrinterList();

        // initialize transient data
        parent_ = null;
        initializeTransient();
    }

/**
Constructs a VPrinters object.

@param  system      The system from which the list will be retrieved.
**/
    public VPrinters( AS400 system )
    {
        if (system == null)
            throw new NullPointerException ("system");

        list_ = new PrinterList(system);

        // initialize transient data
        parent_ = null;
        initializeTransient();
    }

/**
Constructs a VPrinters object.

@param  parent      The parent.
@param  system      The system from which the list will be retrieved.
**/
    public VPrinters(VNode parent, AS400 system )
    {
        if (parent == null)
            throw new NullPointerException ("parent");
        if (system == null)
            throw new NullPointerException ("system");

        list_ = new PrinterList(system);

        // initialize transient data
        parent_ = parent;
        initializeTransient();
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
The children are the printers.

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
@return         The child node, or null if the
                index is not valid.
**/
    public synchronized TreeNode getChildAt (int index)
    {
        updateChildren();

        if (index < 0 || index >= children_.length)
            return null;

        return (TreeNode) children_[index];
    }

/**
Returns the number of children.
This is the number of printers.

@return  The number of children.
**/
    public synchronized int getChildCount ()
    {
        updateChildren();

        return children_.length;
    }

/**
Returns the default action.

@return Always null. There is no default action.
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
    public synchronized VObject getDetailsChildAt (int index)
    {
        updateChildren();

        if (index < 0 || index >= children_.length)
            return null;

        return children_[index];
    }

/**
Returns the number of children for the details.

@return  The number of children for the details.
**/
    public /* @A4D synchronized */ int getDetailsChildCount ()
    {
        updateChildren();

        return children_.length;
    }

/**
Returns the table column model to use in the details
when representing the children.  This column model
describes the details values for the children.

@return The details column model.
**/
    public TableColumnModel getDetailsColumnModel ()
    {
        int iLen;

        synchronized (detailsColumnModelLock_) {

            if (detailsColumnModel_ == null) {

                detailsColumnModel_ = new DefaultTableColumnModel ();

                // printer column
                VTableColumn printerColumn = new VTableColumn (0,VPrinter.PRINTER_PROPERTY);
                printerColumn.setCellRenderer (new VObjectCellRenderer());
                printerColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
                printerColumn.setHeaderValue (printerColumnHeader_);
                iLen = printerColumnHeader_.length();
                if (iLen < dfltPrinterColWidth)
                {
                    iLen = dfltPrinterColWidth;
                }
                printerColumn.setPreferredCharWidth (iLen);
                detailsColumnModel_.addColumn (printerColumn);

                // status column
                VTableColumn statusColumn = new VTableColumn (1,VPrinter.STATUS_PROPERTY);
                statusColumn.setCellRenderer (new VObjectCellRenderer());
                statusColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
                statusColumn.setHeaderValue (statusColumnHeader_);
                iLen = statusColumnHeader_.length();
                if (iLen < dfltStatusColWidth)
                {
                    iLen = dfltStatusColWidth;
                }
                statusColumn.setPreferredCharWidth (iLen);
                detailsColumnModel_.addColumn (statusColumn);

                // description column
                VTableColumn descriptionColumn = new VTableColumn (2,VPrinter.DESCRIPTION_PROPERTY);
                descriptionColumn.setCellRenderer (new VObjectCellRenderer());
                descriptionColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
                descriptionColumn.setHeaderValue (descriptionColumnHeader_);
                iLen = descriptionColumnHeader_.length();
                if (iLen < dfltDescriptionColWidth)
                {
                    iLen = dfltDescriptionColWidth;
                }
                descriptionColumn.setPreferredCharWidth (iLen);
                detailsColumnModel_.addColumn (descriptionColumn);

                // output queue column
                VTableColumn outputQueueColumn = new VTableColumn (3,VPrinter.OUTPUTQUEUE_PROPERTY);
                outputQueueColumn.setCellRenderer (new VObjectCellRenderer());
                outputQueueColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
                outputQueueColumn.setHeaderValue (outputQueueColumnHeader_);
                iLen = outputQueueColumnHeader_.length();
                if (iLen < dfltOutputQueueColWidth)
                {
                    iLen = dfltOutputQueueColWidth;
                }
                outputQueueColumn.setPreferredCharWidth (iLen);
                detailsColumnModel_.addColumn (outputQueueColumn);
            }
        }

        return detailsColumnModel_;
    }

/**
Returns the index of the specified child for the details.

@param  detailsChild   The details child.
@return                The index, or -1 if the child is not found
                       in the details.
**/
    public synchronized int getDetailsIndex (VObject detailsChild)
    {
        updateChildren();

        for (int i = 0; i < children_.length; ++i) {
            if (children_[i] == detailsChild) {
                return i;
            }
        }
        return -1;
    }

/**
Returns the icon.

@param  size    The icon size, either 16 or 32.  If any other
                value is given, then return the default of 16.
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
    public synchronized int getIndex (TreeNode child)
    {
        updateChildren();

        for (int i = 0; i < children_.length; ++i) {
            if (children_[i] == child) {
                return i;
            }
        }
        return -1;
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
Returns the printer list filter.

@return The printer filter.
**/
    public String getPrinterFilter()
    {
        return list_.getPrinterFilter();
    }

/**
Returns the properties pane.

@return The properties pane.
**/
    public VPropertiesPane getPropertiesPane()
    {
        return propertiesPane_;
    }

/**
Returns a property value.

@param      propertyIdentifier  The property identifier.  The choices are
                                <ul>
                                  <li>NAME_PROPERTY
                                  <li>DESCRIPTION_PROPERTY
                                </ul>
@return                         The property value, or null if the
                                property identifier is not recognized.
**/
    public synchronized Object getPropertyValue (Object propertyIdentifier)
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
Returns the system from which the list will be retrieved.

@return The system from which the list will be retrieved.
**/
    public AS400 getSystem ()
    {
        return list_.getSystem();
    }

/**
Returns the text.  This is a constant string which
identifies this object as a list of printers.

@return The text.
**/
    public String getText ()
    {
        return name_;
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

        children_               = new VNode[0];

        propertiesPane_ = new PrintersPropertiesPane(this,list_);
        propertiesPane_.addErrorListener (errorEventSupport_);
        propertiesPane_.addVObjectListener (objectEventSupport_);
        // propertiesPane_.addWorkingListener (workingEventSupport_);    @A2D

        thisPointer_            = this; // used by the loader

        // Initialize the children loaded flag to true.  This
        // makes sure that we do not go to the system until
        // after load() has been called.
        childrenLoaded_         = true;
        childrenLoading_        = false;                            // @A4A

        list_.setAttributesToRetrieve(attrsToRetrieve);
        list_.addPropertyChangeListener(propertyChangeSupport_);    //@A2A
        list_.addVetoableChangeListener(vetoableChangeSupport_);    //@A2A
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
        return false;
    }

/**
Loads information about the object from the system.
**/
    public /* @A4D synchronized */ void load ()
    {
        workingEventSupport_.fireStartWorking ();

        childrenLoaded_ = false;

        workingEventSupport_.fireStopWorking ();
    }

/**
Loads the children (printers) from the system
**/
    private /* @A4D synchronized */ void loadChildren ()
        throws Exception
    {
        if (childrenLoading_ == false)                               // @A4A
        {                                                            // @A4A
            childrenLoading_ = true;                                 // @A4A

            if (children_ != null)
            {
                // Stop listening to the previous children, if any.
                for (int i = 0; i < children_.length; ++i) {
                    children_[i].removeErrorListener (errorEventSupport_);
                    children_[i].removeVObjectListener (objectEventSupport_);
                    children_[i].removeWorkingListener (workingEventSupport_);
                }
            }

            // open the list and fill it with objects
            list_.openSynchronously();

            int listSize = list_.size();

            // trace the size of the list
            if (Trace.isTraceOn()) {
                Trace.log (Trace.INFORMATION, "ListSize:" + listSize);
            }

            // allocate the array of childern objects
            children_ = new VObject[listSize];

            Printer prtD = null;
            VObject child = null;

            // load the children array from the list
            for (int i=0; i<listSize; ++i) {
                // retrieve the spooled file
                prtD = (Printer)list_.getObject(i);

                // create an Printer object using the printer
                child = new VPrinter( thisPointer_, prtD);

                // Add to the list
                children_[i] = child;

                // Listen to the child.
                child.addErrorListener (errorEventSupport_);
                child.addVObjectListener (objectEventSupport_);
                child.addWorkingListener (workingEventSupport_);
            } // end for

            // close the list
            list_.close();

            childrenLoaded_ = true;
            childrenLoading_ = false;                                // @A4A
        }                                                            // @A4A
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
        vetoableChangeSupport_.removeVetoableChangeListener (listener);     //@A3C
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
Sets printer list filter.  A call to load() must be done
after calling this funtion inorder to update the details
and tree children.

@param printerFilter The name of the printers to list.
It cannot be greater than 10 characters in length.
It can be a specific name, a generic name, or the special
value *ALL. The default for the printerFilter is *ALL.

@exception PropertyVetoException If the change is vetoed.
 **/
    public void setPrinterFilter(String printerFilter)
      throws PropertyVetoException
    {
        list_.setPrinterFilter(printerFilter);
    }

/**
Sets the system from which the list will be retrieved.  A call
to load() must be done after calling this funtion inorder to
update the details and tree children.

@param system The system from which the list will be retrieved.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setSystem (AS400 system)
        throws PropertyVetoException
    {
        list_.setSystem (system);
    }


/**
Sorts the children for the details.  Since sorting is not supported,
this method does nothing.

@param  propertyIdentifiers The property identifiers.
@param  orders              The sort orders for each property
                            identifier; true for ascending order,
                            false for descending order.
**/
    public void sortDetailsChildren (Object[] propertyIdentifiers, boolean[] orders)
    {
      // No sorting here!
    }

/**
Returns the string representation.  This is a constant string which
identifies this object as a list of printers.

@return The string representation.
**/
    public String toString ()
    {
        return name_;
    }

/**
Updates the children with event support.
**/
    private void updateChildren()
    {
        if (childrenLoaded_ == false)
        {
            try {
                workingEventSupport_.fireStartWorking ();
                loadChildren ();
            }
            catch (Exception e) {
                errorEventSupport_.fireError(e);
            }
            finally {
                workingEventSupport_.fireStopWorking ();
            }
        }
    }

} // end VPrinters class
