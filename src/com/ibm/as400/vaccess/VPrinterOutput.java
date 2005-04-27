///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VPrinterOutput.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.SpooledFileList;
import com.ibm.as400.access.SpooledFile;
import com.ibm.as400.access.PrintObject;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.AS400;
import javax.swing.Icon;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import javax.swing.SwingConstants;
import javax.swing.tree.TreeNode;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Enumeration;
import java.util.Vector;


/**
The VPrinterOutput class defines the representation of a
list of spooled files on a server for use in various models
and panes in this package.

<p>A VPrinterOutput object has no children.  Its details
children are the spooled files (VOutput objects) in
this list.

<p>You must explicitly call load() to load the information from
the server.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>VPrinterOutput objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>PropertyChangeEvent
    <li>VObjectEvent
    <li>WorkingEvent
</ul>

@see com.ibm.as400.access.SpooledFileList
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/

public class VPrinterOutput
implements VNode, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Static data.
    private static final String name_             = ResourceLoader.getPrintText ("PRINTEROUTPUT_NAME");
    private static final String description_      = ResourceLoader.getPrintText ("PRINTEROUTPUT_DESCRIPTION");
    private static final Icon   icon16_           = ResourceLoader.getIcon ("VPrinterOutput16.gif", description_);
    private static final Icon   icon32_           = ResourceLoader.getIcon ("VPrinterOutput32.gif", description_);
    private static final String outputNameText_   = ResourceLoader.getPrintText ("OUTPUT_NAME");
    private static final String userSpecDataText_ = ResourceLoader.getPrintText ("USER_SPEC_DATA");
    private static final String userText_         = ResourceLoader.getPrintText ("USER");
    private static final String statusText_       = ResourceLoader.getPrintText ("STATUS");
    private static final String printerText_      = ResourceLoader.getPrintText ("PRINTER");
    private static final String pagesPerCopyText_ = ResourceLoader.getPrintText ("PAGES_PER_COPY");
    private static final String copiesLeftText_   = ResourceLoader.getPrintText ("COPIES_LEFT");
    private static final String dateCreatedText_  = ResourceLoader.getPrintText ("DATE_CREATED");
    private static final String formTypeText_     = ResourceLoader.getPrintText ("FORM_TYPE");
    private static final String jobText_          = ResourceLoader.getPrintText ("JOB");
    private static final String jobNumberText_    = ResourceLoader.getPrintText ("JOB_NUMBER");
    private static final String numberText_       = ResourceLoader.getPrintText ("NUMBER");
    private static final String outQText_         = ResourceLoader.getPrintText ("OUTPUT_QUEUE");
    private static final String outQLibText_      = ResourceLoader.getPrintText ("OUTPUT_QUEUE_LIB");
    private static final String priorityText_     = ResourceLoader.getPrintText ("PRIORITY");
    private static final String userCommentText_  = ResourceLoader.getPrintText ("USER_COMMENT");

    private static TableColumnModel             detailsColumnModel_     = null;
    private static int[]                        attrsToList_            = null;

    // @A1A
    private static final int    dfltOutputNameColWidth = 15;
    private static final int    dfltUserSpecDataColWidth = 15;
    private static final int    dfltUserColWidth = 10;
    private static final int    dfltStatusColWidth = 15;
    private static final int    dfltPrinterColWidth = 10;
    private static final int    dfltPagesPerCopyColWidth = 10;
    private static final int    dfltCopiesLeftColWidth = 10;
    private static final int    dfltDateCreatedColWidth = 25;
    private static final int    dfltFormTypeColWidth = 10;
    private static final int    dfltJobColWidth = 15;
    private static final int    dfltJobNumberColWidth = 12;
    private static final int    dfltNumberColWidth = 12;
    private static final int    dfltOutQueColWidth = 12;
    private static final int    dfltOutQueLibColWidth = 12;
    private static final int    dfltPriorityColWidth = 10;
    private static final int    dfltUserCommentColWidth = 25;

    // Properties
    private SpooledFileList                     list_ = null;

    // Transient data
    transient private Vector                    children_ ;
    transient private boolean                   childrenLoaded_;
    transient private boolean                   disableFeature_; // @A1A
    transient private VNode                     parent_;
    transient PrinterOutputPropertiesPane       propertiesPane_;


    // Event support.
    transient private ErrorEventSupport         errorEventSupport_;
    transient private VObjectEventSupport       objectEventSupport_;
    transient private PropertyChangeSupport     propertyChangeSupport_;
    transient private VetoableChangeSupport     vetoableChangeSupport_;
    transient private WorkingEventSupport       workingEventSupport_;

/**
Static Initializer
**/
    static
    {
        // initalize the detailsColumnModel_
        detailsColumnModel_ = new DefaultTableColumnModel ();
        int columnIndex = 0;
        int iLen;

        // output name
        VTableColumn outputNameColumn = new VTableColumn (columnIndex++, VOutput.OUTPUTNAME_PROPERTY);
        outputNameColumn.setCellRenderer (new VObjectCellRenderer ());
        outputNameColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        outputNameColumn.setHeaderValue(outputNameText_);
        iLen = outputNameText_.length();    // @A1A
        if (iLen < dfltOutputNameColWidth)
        {
           iLen = dfltOutputNameColWidth;
        }
        outputNameColumn.setPreferredCharWidth (iLen);
        detailsColumnModel_.addColumn (outputNameColumn);

        // user data
        VTableColumn userDataColumn = new VTableColumn (columnIndex++, VOutput.USERSPECDATA_PROPERTY);
        userDataColumn.setCellRenderer (new VObjectCellRenderer ());
        userDataColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        userDataColumn.setHeaderValue(userSpecDataText_);
        iLen = userSpecDataText_.length();    // @A1A
        if (iLen < dfltUserSpecDataColWidth)
        {
           iLen = dfltUserSpecDataColWidth;
        }
        userDataColumn.setPreferredCharWidth (iLen);
        detailsColumnModel_.addColumn (userDataColumn);

        // user
        VTableColumn userColumn = new VTableColumn (columnIndex++, VOutput.USER_PROPERTY);
        userColumn.setCellRenderer (new VObjectCellRenderer ());
        userColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        userColumn.setHeaderValue (userText_);
        iLen = userText_.length();    // @A1A
        if (iLen < dfltUserColWidth)
        {
           iLen = dfltUserColWidth;
        }
        userColumn.setPreferredCharWidth (iLen);
        detailsColumnModel_.addColumn (userColumn);

        // status
        VTableColumn statusColumn = new VTableColumn (columnIndex++, VOutput.STATUS_PROPERTY);
        statusColumn.setCellRenderer (new VObjectCellRenderer ());
        statusColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        statusColumn.setHeaderValue (statusText_);
        iLen = statusText_.length();    // @A1A
        if (iLen < dfltStatusColWidth)
        {
           iLen = dfltStatusColWidth;
        }
        statusColumn.setPreferredCharWidth (iLen);
        detailsColumnModel_.addColumn (statusColumn);

        // printer
        VTableColumn printerColumn = new VTableColumn (columnIndex++, VOutput.PRINTER_PROPERTY);
        printerColumn.setCellRenderer (new VObjectCellRenderer ());
        printerColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        printerColumn.setHeaderValue (printerText_);
        iLen = printerText_.length();    // @A1A
        if (iLen < dfltPrinterColWidth)
        {
           iLen = dfltPrinterColWidth;
        }
        printerColumn.setPreferredCharWidth (iLen);
        detailsColumnModel_.addColumn (printerColumn);

        // pages
        VTableColumn pagesColumn = new VTableColumn (columnIndex++, VOutput.PAGESPERCOPY_PROPERTY);
        pagesColumn.setCellRenderer (new VObjectCellRenderer (SwingConstants.CENTER));
        pagesColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        pagesColumn.setHeaderValue (pagesPerCopyText_);
        iLen = pagesPerCopyText_.length();    // @A1A
        if (iLen < dfltPagesPerCopyColWidth)
        {
           iLen = dfltPagesPerCopyColWidth;
        }
        pagesColumn.setPreferredCharWidth (iLen);
        detailsColumnModel_.addColumn (pagesColumn);

        // copies
        VTableColumn copiesColumn = new VTableColumn (columnIndex++, VOutput.COPIESLEFT_PROPERTY);
        copiesColumn.setCellRenderer (new VObjectCellRenderer (SwingConstants.CENTER));
        copiesColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        copiesColumn.setHeaderValue (copiesLeftText_);
        iLen = copiesLeftText_.length();    // @A1A
        if (iLen < dfltCopiesLeftColWidth)
        {
           iLen = dfltCopiesLeftColWidth;
        }
        copiesColumn.setPreferredCharWidth (iLen);
        detailsColumnModel_.addColumn (copiesColumn);

        // date
        VTableColumn dateColumn = new VTableColumn (columnIndex++, VOutput.DATE_PROPERTY);
        dateColumn.setCellRenderer (new VObjectCellRenderer ());
        dateColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        dateColumn.setHeaderValue (dateCreatedText_);
        iLen = dateCreatedText_.length();    // @A1A
        if (iLen < dfltDateCreatedColWidth)
        {
           iLen = dfltDateCreatedColWidth;
        }
        dateColumn.setPreferredCharWidth (iLen);
        detailsColumnModel_.addColumn (dateColumn);

        // form type
        VTableColumn formTypeColumn = new VTableColumn (columnIndex++, VOutput.FORMTYPE_PROPERTY);
        formTypeColumn.setCellRenderer (new VObjectCellRenderer ());
        formTypeColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        formTypeColumn.setHeaderValue (formTypeText_);
        iLen = formTypeText_.length();    // @A1A
        if (iLen < dfltFormTypeColWidth)
        {
           iLen = dfltFormTypeColWidth;
        }
        formTypeColumn.setPreferredCharWidth (iLen);
        detailsColumnModel_.addColumn (formTypeColumn);

        // job
        VTableColumn jobColumn = new VTableColumn (columnIndex++, VOutput.JOB_PROPERTY);
        jobColumn.setCellRenderer (new VObjectCellRenderer ());
        jobColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        jobColumn.setHeaderValue (jobText_);
        iLen = jobText_.length();    // @A1A
        if (iLen < dfltJobColWidth)
        {
           iLen = dfltJobColWidth;
        }
        jobColumn.setPreferredCharWidth (iLen);
        detailsColumnModel_.addColumn (jobColumn);

        // job number
        VTableColumn jobNumberColumn = new VTableColumn (columnIndex++, VOutput.JOBNUMBER_PROPERTY);
        jobNumberColumn.setCellRenderer (new VObjectCellRenderer ());
        jobNumberColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        jobNumberColumn.setHeaderValue (jobNumberText_);
        iLen = jobNumberText_.length();    // @A1A
        if (iLen < dfltJobNumberColWidth)
        {
           iLen = dfltJobNumberColWidth;
        }
        jobNumberColumn.setPreferredCharWidth (iLen);
        detailsColumnModel_.addColumn (jobNumberColumn);

        // number
        VTableColumn numberColumn = new VTableColumn (columnIndex++, VOutput.NUMBER_PROPERTY);
        numberColumn.setCellRenderer (new VObjectCellRenderer (SwingConstants.CENTER));
        numberColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        numberColumn.setHeaderValue (numberText_);
        iLen = numberText_.length();   // @A1A
        if (iLen < dfltNumberColWidth)
        {
           iLen = dfltNumberColWidth;
        }
        numberColumn.setPreferredCharWidth (iLen);
        detailsColumnModel_.addColumn (numberColumn);

        // output queue
        VTableColumn outQColumn = new VTableColumn (columnIndex++, VOutput.OUTPUTQUEUE_PROPERTY);
        outQColumn.setCellRenderer (new VObjectCellRenderer ());
        outQColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        outQColumn.setHeaderValue (outQText_);
        iLen = outQText_.length();    // @A1A
        if (iLen < dfltOutQueColWidth)
        {
           iLen = dfltOutQueColWidth;
        }
        outQColumn.setPreferredCharWidth (iLen);
        detailsColumnModel_.addColumn (outQColumn);

        // output queue library
        VTableColumn outQLibColumn = new VTableColumn (columnIndex++, VOutput.OUTPUTQUEUELIB_PROPERTY);
        outQLibColumn.setCellRenderer (new VObjectCellRenderer ());
        outQLibColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        outQLibColumn.setHeaderValue (outQLibText_);
        iLen = outQLibText_.length();    // @A1A
        if (iLen < dfltOutQueLibColWidth)
        {
           iLen = dfltOutQueLibColWidth;
        }
        outQLibColumn.setPreferredCharWidth (iLen);
        detailsColumnModel_.addColumn (outQLibColumn);

        // priority
        VTableColumn priorityColumn = new VTableColumn (columnIndex++, VOutput.PRIORITY_PROPERTY);
        priorityColumn.setCellRenderer (new VObjectCellRenderer (SwingConstants.CENTER));
        priorityColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        priorityColumn.setHeaderValue (priorityText_);
        iLen = priorityText_.length();    // @A1A
        if (iLen < dfltPriorityColWidth)
        {
           iLen = dfltPriorityColWidth;
        }
        priorityColumn.setPreferredCharWidth (iLen);
        detailsColumnModel_.addColumn (priorityColumn);

        // user comment
        VTableColumn userCommentColumn = new VTableColumn (columnIndex++, VOutput.USERCOMMENT_PROPERTY);
        userCommentColumn.setCellRenderer (new VObjectCellRenderer ());
        userCommentColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        userCommentColumn.setHeaderValue (userCommentText_);
        iLen = userCommentText_.length();    // @A1A
        if (iLen < dfltUserCommentColWidth)
        {
           iLen = dfltUserCommentColWidth;
        }
        userCommentColumn.setPreferredCharWidth (iLen);
        detailsColumnModel_.addColumn (userCommentColumn);

        // initalize the attributes to be retrieved in the list
        attrsToList_ = new int[19];
        attrsToList_[0]   = PrintObject.ATTR_COPIES;
        attrsToList_[1]   = PrintObject.ATTR_COPIESLEFT;
        attrsToList_[2]   = PrintObject.ATTR_CURPAGE;
        attrsToList_[3]   = PrintObject.ATTR_DATE;
        attrsToList_[4]   = PrintObject.ATTR_FORMTYPE;
        attrsToList_[5]   = PrintObject.ATTR_JOBNAME;
        attrsToList_[6]   = PrintObject.ATTR_JOBNUMBER;
        attrsToList_[7]   = PrintObject.ATTR_JOBUSER;
        attrsToList_[8]   = PrintObject.ATTR_LASTPAGE;
        attrsToList_[9]   = PrintObject.ATTR_OUTPTY;
        attrsToList_[10]  = PrintObject.ATTR_OUTPUT_QUEUE;
        attrsToList_[11]  = PrintObject.ATTR_PAGES;
        attrsToList_[12]  = PrintObject.ATTR_PRINTER;
        attrsToList_[13]  = PrintObject.ATTR_PRTASSIGNED;
        attrsToList_[14]  = PrintObject.ATTR_SAVE;
        attrsToList_[15]  = PrintObject.ATTR_SPLFSTATUS;
        attrsToList_[16]  = PrintObject.ATTR_TIME;
        attrsToList_[17]  = PrintObject.ATTR_USERCMT;
        attrsToList_[18]  = PrintObject.ATTR_USERDATA;
    }


/**
Constructs a VPrinterOutput object.
**/
    public VPrinterOutput ()
    {
        list_ = new SpooledFileList();

        // initialize transient data
        disableFeature_ = false;
        parent_ = null;
        initializeTransient ();
    }

    // @A1A
    // Special package scoped constructor to used to disable
    // output queue and output queue lib lists in properties pane.
    VPrinterOutput (boolean disable)
    {
        list_ = new SpooledFileList();

        // initialize transient data
        disableFeature_ = disable;
        parent_ = null;
        initializeTransient ();
    }

/**
Constructs a VPrinterOutput object.

@param  system      The server on which the output resides.
**/
    public VPrinterOutput (AS400 system)
    {
        if (system == null)
            throw new NullPointerException ("system");

        list_ = new SpooledFileList(system);

        // initialize transient data
        disableFeature_ = false;
        parent_ = null;
        initializeTransient ();
    }

    // @A1A
    // Special package scoped constructor to used to disable
    // output queue and output queue lib lists in properties pane.
    VPrinterOutput (AS400 system, boolean disable)
    {
        if (system == null)
            throw new NullPointerException ("system");

        list_ = new SpooledFileList(system);

        // initialize transient data
        disableFeature_ = disable;
        parent_ = null;
        initializeTransient ();
    }

/**
Constructs a VPrinterOutput object.

@param  parent   The parent.
@param  system   The server on which the output resides.
**/
    public VPrinterOutput (VNode parent, AS400 system)
    {
        if (parent == null)
            throw new NullPointerException ("parent");
        if (system == null)
            throw new NullPointerException ("system");

        list_ = new SpooledFileList(system);

        // initialize transient data
        disableFeature_ = false;
        parent_ = parent;
        initializeTransient ();
    }

    // @A1A
    // Special package scoped constructor to used to disable
    // output queue and output queue lib lists in properties pane.
    VPrinterOutput (VNode parent, AS400 system, boolean disable)
    {
        if (parent == null)
            throw new NullPointerException ("parent");
        if (system == null)
            throw new NullPointerException ("system");

        list_ = new SpooledFileList(system);

        // initialize transient data
        disableFeature_ = disable;
        parent_ = parent;
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

@return         An empty enumeration.
**/
    public synchronized Enumeration children ()
    {
        return new Enumeration ()
        {
            public boolean hasMoreElements ()   { return false; }
            public Object  nextElement ()       { throw new java.util.NoSuchElementException (); }
        };
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

@return  Always false.
**/
    public boolean getAllowsChildren ()
    {
        return false;
    }


/**
Returns the child node at the specified index.

@param  index   The index.

@return Always null.
**/
    public TreeNode getChildAt (int index)
    {
        return null;
    }


/**
Returns the number of children.

@return Always 0.
**/
    public int getChildCount ()
    {
        return 0;
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
The details child is a spooled file (VOutput object) in
this list.

@param  index   The index.
@return         The child, or null if the index is not
                valid.
**/
    public synchronized VObject getDetailsChildAt (int index)
    {
        if (childrenLoaded_ == false)
            {
            loadChildren ();
            }

        if (true == children_.isEmpty())
            return null;
        else
            return (VObject) children_.elementAt (index);
    }



/**
Returns the number of children for the details.
The details children are the spooled files (VOutput objects) in
this list.

@return  The number of children for the details.
**/
    public synchronized int getDetailsChildCount ()
    {
        if (childrenLoaded_ == false)
            {
            loadChildren ();
            }

        return children_.size ();
    }


/**
Returns the table column model to use in the details
when representing the children.  This column model
describes the details values for the children.
The details children are the spooled files (VOutput objects) in
this list.

@return The details column model.
**/
    public TableColumnModel getDetailsColumnModel ()
    {
        return detailsColumnModel_;
    }


/**
Returns the index of the specified child for the details.
The details child is a spooled file (VOutput object) in
this list.

@param  child   The details child.
@return                The index, or -1 if the child is not found
                       in the details.
**/
    public synchronized int getDetailsIndex (VObject child)
    {
        if (childrenLoaded_ == false)
            {
            loadChildren ();
            }

        return children_.indexOf (child);
    }


/**
Returns the form type filter for the list.
The filter contains the form type a spooled file must be
to be included in the list.

@return The form type filter.
**/
    public String getFormTypeFilter()
    {
        return list_.getFormTypeFilter();
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
@return Always -1.
**/
    public int getIndex (TreeNode child)
    {
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
                                NAME_PROPERTY or DESCRIPTION_PROPERTY.
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
Returns the output queue list filter.

@return The output queue filter.
**/
    public String getQueueFilter()
    {
        return list_.getQueueFilter();
    }


/**
Returns the system on which the output resides.

@return The system on which the output resides.
**/
    public AS400 getSystem ()
    {
        return list_.getSystem();
    }


/**
Returns the text.  This is a constant string which
identifies this object as a list of printer output.

@return The text.
**/
    public String getText ()
    {
        return name_;
    }


/**
Returns the user data list filter.

@return The user data filter.
**/
    public String getUserDataFilter()
    {
        return list_.getUserDataFilter();
    }


/**
Returns the user ID list filter.

@return The user ID filter.
**/
    public String getUserFilter()
    {
        return list_.getUserFilter();
    }


/**
Initializes the transient data.
**/
    private void initializeTransient ()
    {
    // Initialize event support
    errorEventSupport_      = new ErrorEventSupport (this);
    objectEventSupport_     = new VObjectEventSupport (this);
    propertyChangeSupport_  = new PropertyChangeSupport (this);
    vetoableChangeSupport_  = new VetoableChangeSupport (this);
    workingEventSupport_    = new WorkingEventSupport (this);

    list_.setAttributesToRetrieve(attrsToList_);
    list_.addPropertyChangeListener (propertyChangeSupport_);
    list_.addVetoableChangeListener (vetoableChangeSupport_);

    // initialize the properties pane
    propertiesPane_ = new PrinterOutputPropertiesPane (this, list_, disableFeature_);

    // Listen to the properties pane.
    propertiesPane_.addErrorListener (errorEventSupport_);
    propertiesPane_.addVObjectListener (objectEventSupport_);
    propertiesPane_.addWorkingListener (workingEventSupport_);

    // Initialize the children
    children_               = new Vector ();
    childrenLoaded_         = true;//@A2C set to true so beans don't call to system without load()
    }

/**
Indicates if the node is a leaf.

@return  Always true.
**/
    public boolean isLeaf ()
    {
        return true;
    }



/**
Indicates if the details children are sortable.

@return Always true.
**/
    public boolean isSortable ()
    {
        return true;
    }

/**
Loads information about the object from the server.
**/
    public synchronized void load ()
    {
//@B0D        workingEventSupport_.fireStartWorking ();

        // Reset children flag so that kids get loaded
        // on next getDetails call.
        childrenLoaded_ = false;

        loadChildren(); //@B0A

//@B0D        workingEventSupport_.fireStopWorking ();
    }


/**
Loads the children.
**/
    private synchronized void loadChildren ()
    {
        workingEventSupport_.fireStartWorking ();

        // stop listening to children
        for (Enumeration e = children_.elements (); e.hasMoreElements (); ) {
            VObject child = (VObject) e.nextElement ();
            child.removeErrorListener (errorEventSupport_);
            child.removeVObjectListener (objectEventSupport_);
            child.removeWorkingListener (workingEventSupport_);
            }

        children_.removeAllElements ();

        // build the list
        try {
            // open the list and fill it with objects
            list_.openSynchronously();

            int listSize = list_.size();

            // trace the size of the list
            if (Trace.isTraceOn())
                Trace.log (Trace.INFORMATION, "ListSize:" + listSize);

            // allocate the array of childern objects
            children_ = new Vector(listSize);

            SpooledFile splF = null;
            VObject child = null;

            // load the children array from the list
            for (int i=0; i<listSize; ++i)
                {
                // retrieve the spooled file
                splF = (SpooledFile)list_.getObject(i);

                // create a VOutput object
                child = new VOutput(VPrinterOutput.this, splF);

                // Load the child.
                child.load ();

                // Add to the lists.
                children_.addElement (child);

                // Listen to the child.
                child.addErrorListener (errorEventSupport_);
                child.addVObjectListener (objectEventSupport_);
                child.addWorkingListener (workingEventSupport_);
                } // end for
            } // end try block

        catch (Exception e)
            {
            children_ = new Vector ();
            errorEventSupport_.fireError (e);
            } // end catch block

        // close the list
        list_.close();

        childrenLoaded_ = true;
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
Removes a file that was a child.
This should only be called after a file is deleted.
This saves us from having to do a reload.

<p>Note that this does not actually delete the file.
That is the responsibility of the caller.

@param object The child object.
**/
    synchronized void remove (VObject object)
    {
        children_.removeElement (object);

        object.removeErrorListener (errorEventSupport_);
        object.removeVObjectListener (objectEventSupport_);
        object.removeWorkingListener (workingEventSupport_);
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
Sets the form type filter for the list.  A call to load() must be done after
calling this funtion inorder to update the details children.

@param formTypeFilter The form type the spooled file must be to be included
in the list.  It cannot be greater than 10 characters.
The value can be any specific value or any of these special values:
<ul>
  <li> *ALL - Spooled files with any form type will be included in the list.
  <li> *STD - Spooled files with the form type *STD will be included in the list.
</ul>
The default is *ALL.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setFormTypeFilter(String formTypeFilter)
      throws PropertyVetoException
    {
        list_.setFormTypeFilter(formTypeFilter);
    }


/**
Sets the output queue filter.  A call to load() must be done after calling
this funtion inorder to update the details children.

@param queueFilter The library and output queues on which to list spooled
 files.   The format of the queueFilter string must be in the
 format of /QSYS.LIB/libname.LIB/queuename.OUTQ where
<br>
  <I>libname</I> is the library name that contains the queues to search.
    It can be a specific name or one of these special values:
<ul>
 <li> %LIBL%   - The server job's library list.
 <li> %ALL%    - All libraries are searched.  This value is only valid
                if the queuename is %ALL%.
</ul>
  <I>queuename</I> is the name of the output queues to search.
  It can be a specific name or the special value %ALL%.
  If it is %ALL%, then the libname must also be %ALL%.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setQueueFilter(String queueFilter)
      throws PropertyVetoException
    {
        list_.setQueueFilter(queueFilter);
    }

/**
Sets the system on which the output resides.  A call to load() must be done
after calling this funtion inorder to update the details children.

@param system The system on which the output resides.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setSystem (AS400 system)
        throws PropertyVetoException
    {
        list_.setSystem (system);
    }


/**
Sets the user data list filter.  A call to load() must be done after
calling this funtion inorder to update the details children.

@param userDataFilter The user data the spooled file must
 have for it to be included in the list.  The value can be
 any specific value or the special value *ALL.  The value cannot be
 greater than 10 characters.
 The default is *ALL.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setUserDataFilter(String userDataFilter)
      throws PropertyVetoException
    {
        list_.setUserDataFilter(userDataFilter);
    }


/**
Sets the user ID list filter.  A call to load() must be done after calling
this funtion inorder to update the details children.

@param userFilter The user or users for which to list spooled files.
The value cannot be greater than 10 characters.
The value can be any specific user ID or any of these special values:
<UL>
 <LI>  *ALL - Spooled files created by all users will be included in the list.
 <LI>  *CURRENT - Spooled files created by the current user only will be in the list.
</UL>
The default is *CURRENT.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setUserFilter(String userFilter)
      throws PropertyVetoException
    {
        list_.setUserFilter(userFilter);
    }


/**
Sorts the children for the details.
The propertyIdentifer[0], orders[0] combination is used to do the sort.
If the values are equal, propertyIdentifier[1], orders[1] is used to
break the tie, and so forth.

@param  propertyIdentifiers The property identifiers.
@param  orders              The sort orders for each property
                            identifier; true for ascending order,
                            false for descending order.
**/
    public synchronized void sortDetailsChildren (Object[] propertyIdentifiers, boolean[] orders)
    {
        if (propertyIdentifiers == null)
            throw new NullPointerException ("propertyIdentifiers");
        if (orders == null)
            throw new NullPointerException ("orders");

        if (childrenLoaded_ == false)
            {
            loadChildren ();
            }

        VUtilities.sort (children_, propertyIdentifiers, orders);
    }


/**
Returns the string representation.
This is a constant string which
identifies this object as a list of printer output.

@return The string representation.
**/
    public String toString ()
    {
        return name_;
    }

} // end VPrinterOutput class
