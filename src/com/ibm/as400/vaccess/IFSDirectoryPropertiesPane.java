///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSDirectoryPropertiesPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.Trace;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DateFormat;
import java.util.TimeZone;



/**
The IFSDirectoryPropertiesPane class represents the properties pane
for a VIFSDirectory object.
**/
class IFSDirectoryPropertiesPane
implements VPropertiesPane
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String byteText_       = ResourceLoader.getText ("IFS_BYTE");
    private static final String bytesText_      = ResourceLoader.getText ("IFS_BYTES");
    private static final String containsText_   = ResourceLoader.getText ("IFS_CONTAINS") + ":";
    private static final String directoryText_  = ResourceLoader.getText ("IFS_DIRECTORY");
    private static final String directoriesText_= ResourceLoader.getText ("IFS_DIRECTORIES");
    private static final String fileText_       = ResourceLoader.getText ("IFS_FILE");
    private static final String filesText_      = ResourceLoader.getText ("IFS_FILES");
    private static final String generalText_    = ResourceLoader.getText ("TAB_GENERAL");
    private static final String locationText_   = ResourceLoader.getText ("IFS_LOCATION") + ":";
    private static final String modifiedText_   = ResourceLoader.getText ("IFS_MODIFIED") + ":";
    private static final String sizeText_       = ResourceLoader.getText ("IFS_SIZE") + ":";



    // Static data.
    private static  DateFormat  dateFormat_     = DateFormat.getDateTimeInstance ();



    // Private data.
    private VIFSDirectory       object_;



    // Event support.
    private ChangeEventSupport  changeEventSupport_     = new ChangeEventSupport (this);
    private ErrorEventSupport   errorEventSupport_      = new ErrorEventSupport (this);
    private VObjectEventSupport objectEventSupport_     = new VObjectEventSupport (this);
    private WorkingEventSupport workingEventSupport_    = new WorkingEventSupport (this);



/**
Static initializer.
**/
    static
    {
        dateFormat_.setTimeZone (TimeZone.getDefault ());
    }



/**
Constructs an IFSDirectoryPropertiesPane object.

@param  object      The object.
**/
    public IFSDirectoryPropertiesPane (VIFSDirectory object)
    {
        object_     = object;
    }



/**
Adds a change listener.

@param  listener    The listener.
**/
    public void addChangeListener (ChangeListener listener)
    {
        changeEventSupport_.addChangeListener (listener);
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
Adds a listener to be notified when a VObject is changed,
created, or deleted.

@param  listener    The listener.
**/
    public void addVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener (listener);
    }



/**
Adds a listener to be notified when work in a different thread
starts and stops.

@param  listener    The listener.
**/
    public void addWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener (listener);
    }



/**
Applies the changes made by the user.

@throws Exception   If an error occurs.
**/
    public void applyChanges ()
        throws Exception
    {
        // No changes are allowed.
    }



/**
Returns the graphical component.

@return             The graphical component.
@throws Exception   If an error occurs.
**/
    public Component getComponent ()
    {
        // Initialize the general tab.
        JPanel generalTab = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        generalTab.setLayout (layout);
        generalTab.setBorder (new EmptyBorder (10, 10, 10, 10));

        // Add the components.
        int row = 0;
        VUtilities.constrain (new JLabel (object_.getText (), object_.getIcon (32, false),  SwingConstants.LEFT),
            generalTab, layout, 0, row++, 2, 1);

        VUtilities.constrain (new JSeparator (),
            generalTab, layout, 0, row++, 2, 1);

        VUtilities.constrain (new JLabel (locationText_),
            generalTab, layout, 0, row, 1, 1);
        VUtilities.constrain (new JLabel (object_.getParentDirectory ()),
            generalTab, layout, 1, row, 1, 1);
        row++;

        VUtilities.constrain (new JLabel (containsText_),
            generalTab, layout, 0, row, 1, 1);
        int directoryCount = object_.getDirectoryCount ();
        int fileCount = object_.getFileCount ();
        VUtilities.constrain (new JLabel (Integer.toString (fileCount) + " "
            + ((fileCount == 1) ? fileText_ : filesText_) + ", "
            + Integer.toString (directoryCount) + " "
            + ((directoryCount == 1) ? directoryText_ : directoriesText_)),
            generalTab, layout, 1, row, 1, 1);
        row++;

        VUtilities.constrain (new JSeparator (),
            generalTab, layout, 0, row++, 2, 1);

        VUtilities.constrain (new JLabel (modifiedText_),
            generalTab, layout, 0, row, 1, 1);
        VUtilities.constrain (new JLabel (dateFormat_.format (object_.getModified ())),
            generalTab, layout, 1, row, 1, 1);
        row++;

        // Build the pane.
        JTabbedPane pane = new JTabbedPane ();
        pane.addTab (generalText_, null, generalTab);
        pane.setSelectedIndex (0);
        return pane;
    }



/**
Removes a change listener.

@param  listener    The listener.
**/
    public void removeChangeListener (ChangeListener listener)
    {
        changeEventSupport_.removeChangeListener (listener);
    }



/**
Removes a listener to be notified when an error occurs.

@param  listener    The listener.
**/
    public void removeErrorListener (ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener (listener);
    }



/**
Removes a listener to be notified when a VObject is changed,
created, or deleted.

@param  listener    The listener.
**/
    public void removeVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.removeVObjectListener (listener);
    }



/**
Removes a listener to be notified when work in a different thread
starts and stops.

@param  listener    The listener.
**/
    public void removeWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener (listener);
    }



}
