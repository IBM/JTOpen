///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrinterOutputPropertiesPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.SpooledFileList;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.QSYSObjectPathName;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JComboBox;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.event.ChangeListener;
import javax.swing.border.EmptyBorder;


/**
The PrinterOutputPropertiesPane class represents the property pane
for the VPrinterOutput object.
**/
class PrinterOutputPropertiesPane
implements VPropertiesPane, ItemListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Private data.
    private VPrinterOutput object_;             // our visual object
    private SpooledFileList list_;              // the list of spooled files

    private JComboBox userFilter_;              // user filter
            JComboBox outQFilter_;              // output queue name filter     @A4C
            JComboBox outQLibFilter_;           // output queue library filter  @A4C
    private JComboBox formTypeFilter_;          // formtype filter
    private JComboBox userDataFilter_;          // userdata filter

    private boolean    disableFeature_;  // @A1A

    // Event support.
            ChangeEventSupport  changeEventSupport_     = new ChangeEventSupport (this); // @A4C
    private ErrorEventSupport   errorEventSupport_      = new ErrorEventSupport (this);
    private VObjectEventSupport objectEventSupport_     = new VObjectEventSupport (this);
    private WorkingEventSupport workingEventSupport_    = new WorkingEventSupport (this);

    // MRI
    private static final String includeText_       = ResourceLoader.getPrintText("INCLUDE");
    private static final String userText_          = ResourceLoader.getPrintText("USER") + ":";
    private static final String currentUserText_   = ResourceLoader.getPrintText("CURRENT_USER");
            static final String allText_           = ResourceLoader.getPrintText("ALL");  // @A4C
            static final String liblText_          = ResourceLoader.getPrintText("LIBRARY_LIST"); // @A4C
    private static final String outQText_          = ResourceLoader.getPrintText("OUTPUT_QUEUE") + ":";
    private static final String outQLibText_       = ResourceLoader.getPrintText("OUTPUT_QUEUE_LIB") + ":";
    private static final String formTypeText_      = ResourceLoader.getPrintText("FORM_TYPE") + ":";
    private static final String standardText_      = ResourceLoader.getPrintText("STANDARD");
    private static final String userSpecDataText_  = ResourceLoader.getPrintText("USER_SPEC_DATA") + ":";

    // package scoped data
    boolean            fChanges_; // @A2A

/**
Constructs an PrinterOutputPropertiesPane object.

@param  object The object.
@param  list The spooled file list.
**/
    public PrinterOutputPropertiesPane (VPrinterOutput object, SpooledFileList list)
    {
        object_ = object;
        list_ = list;
        disableFeature_ = false; // @A1A
        fChanges_ = false; // @A2A
    }

// @A1A Package Scoped constructor
// Constructs an PrinterOutputPropertiesPane object with a flag
// to enable/disable output queue and output queue lib lists.
    PrinterOutputPropertiesPane (VPrinterOutput object, SpooledFileList list, boolean disable)
    {
        object_ = object;
        list_ = list;
        disableFeature_ = disable; // @A1A
        fChanges_ = false; // @A2A
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
**/
    public void applyChanges ()
      throws Exception
    {
        fChanges_ = false; // indicates an object change event should be fired @A2C

        //////////////////
        // user filter ///
        //////////////////

        // get the previous filter value from the list
        String oldUser = list_.getUserFilter().trim();

        // get the new filter value from the combo box and upper case it
        String newUser = (String)userFilter_.getSelectedItem();
        newUser = newUser.trim();

        // change new value if necessary
        if (newUser.equals(currentUserText_))
            newUser = "*CURRENT";
        else if (newUser.equals(allText_))
            newUser = "*ALL";

        // compare the filters to see if a change is needed, and set as necessary
        if (!oldUser.equals(newUser))
            {
            list_.setUserFilter(newUser);
            fChanges_ = true; // @A2C
            }

        if (disableFeature_ == false) // @A1A
        {
            //////////////////////////
            // Output Queue filter ///
            //////////////////////////

            // get the previous filter value from the list
            String oldOutQ = list_.getQueueFilter().trim();

            // get the new filter values from the combo boxes
            String newOutQ = (String)outQFilter_.getSelectedItem();
            String newOutQLib = (String)outQLibFilter_.getSelectedItem();
            newOutQ = newOutQ.trim();
            newOutQLib = newOutQLib.trim();

            // if outq or outq lib are empty
            if ( (newOutQ.equals("")) || (newOutQLib.equals("")) )
                {
                // just reset the filter
                list_.setQueueFilter("");
                fChanges_ = true; // @A2C
                }
            else
                {
                // change the values if necessary
                if (newOutQ.equals(allText_)) newOutQ = "%ALL%";
                if (newOutQLib.equals(allText_)) newOutQLib = "%ALL%";
                else if (newOutQLib.equals(liblText_)) newOutQLib = "%LIBL%";

                // create a QSYSObjectPathName that represents what the user selected
                QSYSObjectPathName newOutQPath = new QSYSObjectPathName(newOutQLib, newOutQ, "OUTQ");

                // compare the filters to see if a change is needed
                if (!oldOutQ.equals(newOutQPath.getPath().trim()))
                    {
                    // set the new outq and lib
//  @A3C                  newOutQPath.setObjectName(newOutQ);
//  @A3C                  newOutQPath.setLibraryName(newOutQLib);

                    // set the new filter
                    list_.setQueueFilter(newOutQPath.getPath());
                    fChanges_ = true; // @A2C
                    } // end if the filters dont match
                } // end else
        }

        ///////////////////////
        // Form Type filter ///
        ///////////////////////

        // get the previous filter value from the list
        String oldFormType = list_.getFormTypeFilter().trim();

        // get the new filter value from the combo box and upper case it
        String newFormType = (String)formTypeFilter_.getSelectedItem();
        newFormType = newFormType.trim();

        // change new value if necessary
        if (newFormType.equals(standardText_))
            newFormType = "*STD";
        else if (newFormType.equals(allText_))
            newFormType = "*ALL";

        // compare the filters to see if a change is needed, and set as necessary
        if (!oldFormType.equals(newFormType))
            {
            list_.setFormTypeFilter(newFormType);
            fChanges_ = true; // @A2C
            }

        ///////////////////////
        // User Data filter ///
        ///////////////////////

        // get the previous filter value from the list
        String oldUserData = list_.getUserDataFilter().trim();

        // get the new filter value from the combo box and upper case it
        String newUserData = (String)userDataFilter_.getSelectedItem();
        newUserData = newUserData.trim();

        // change new value if necessary
        if (newUserData.equals(allText_))
            newUserData = "*ALL";

        // compare the filters to see if a change is needed, and set as necessary
        if (!oldUserData.equals(newUserData))
            {
            list_.setUserDataFilter(newUserData);
            fChanges_ = true; // @A2C
            }

        // notify object that there have been changes, if necessary
        if (fChanges_ == true) objectEventSupport_.fireObjectChanged(object_); // @A2C
    }

/**
Builds Include pane for the properties pane.
Also used by VPrinter class
**/
    public JPanel buildIncludePane()
    {
        JPanel thePane = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        thePane.setLayout (layout);
        thePane.setBorder (new EmptyBorder (10, 10, 10, 10));

        try
            {
            // User filter
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (userText_),   // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            userFilter_ = new JComboBox();
            userFilter_.setEditable(true);
            userFilter_.addItem(currentUserText_);
            userFilter_.addItem(allText_);

            String curUserFilter = list_.getUserFilter().trim();
            if ( (curUserFilter.equals("*CURRENT")) ||
                 (curUserFilter.equals("")) )
                userFilter_.setSelectedItem(currentUserText_);
            else if (curUserFilter.equals("*ALL"))
                userFilter_.setSelectedItem(allText_);
            else
                {
                userFilter_.addItem(curUserFilter);
                userFilter_.setSelectedItem(curUserFilter);
                }

            userFilter_.addItemListener(this);
            constraints = new GridBagConstraints(); // @A1A
            VUtilities.constrain (userFilter_,      // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  0,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // Output queue and library filter
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (outQText_),   // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            constraints = new GridBagConstraints();         // @A1A
            VUtilities.constrain (new JLabel (outQLibText_),// @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  2,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // output queue
            outQFilter_ = new JComboBox();
            outQFilter_.setEditable(true);
            outQFilter_.addItem(allText_);          // all

            // output queue library
            outQLibFilter_ = new JComboBox();
            outQLibFilter_.setEditable(true);
            outQLibFilter_.addItem(allText_);       // all
            outQLibFilter_.addItem(liblText_);      // library list


            // retrieve the current filter from the list
            String queueFilter = list_.getQueueFilter().trim();

            // check to see if the filter is set
            if (!queueFilter.equals(""))
                {
                QSYSObjectPathName outQPath = new QSYSObjectPathName(queueFilter);

                // output queue
                String curQueueFilter = outQPath.getObjectName();

                // output queue name can be *ALL or a specific name
                if (curQueueFilter.equals("*ALL"))
                    outQFilter_.setSelectedItem(allText_);
                else
                    {
                    outQFilter_.addItem(curQueueFilter);
                    outQFilter_.setSelectedItem(curQueueFilter);
                    }

                // output queue library
                String curQueueLibFilter = outQPath.getLibraryName();

                // output queue library can be *ALL or *LIBL
                if (curQueueLibFilter.equals("*ALL"))
                    outQLibFilter_.setSelectedItem(allText_);
                else if (curQueueLibFilter.equals("*LIBL"))
                    outQLibFilter_.setSelectedItem(liblText_);
                else
                    {
                    outQLibFilter_.addItem(curQueueLibFilter);
                    outQLibFilter_.setSelectedItem(curQueueLibFilter);
                    }
                } // end if filter was set
            else // filter was not set, so just select default values
                {
                // output queue
                outQFilter_.setSelectedItem(allText_);

                // output queue library
                outQLibFilter_.setSelectedItem(allText_);
                }

            // output queue
            if (disableFeature_ == true) // @A1A
            {
                outQFilter_.setEnabled(false);
            }
            else
            {
                outQFilter_.addItemListener( new ItemListener ()
                                             {
                                             public void itemStateChanged(ItemEvent e)
                                                 {
                                                 String newOutQ = (String)outQFilter_.getSelectedItem();

                                                 // check to see if outq is all  and outq lib is not all
                                                 if ( (newOutQ.trim().equals(allText_)) &&
                                                      (!outQLibFilter_.getSelectedItem().equals(allText_)) )
                                                      {
                                                     // set the outq lib to all
                                                     outQLibFilter_.setSelectedItem(allText_);
                                                      }
                                                 else if ( (!newOutQ.trim().equals(allText_)) &&  // @A3A
                                                           (outQLibFilter_.getSelectedItem().equals(allText_)) )
                                                      {
                                                         // set the outq lib to library list
                                                         outQLibFilter_.setSelectedItem(liblText_);
                                                      }

                                                 // notify that something has changed so the apply button is enabled
                                                 changeEventSupport_.fireStateChanged ();
                                             } // end itemStateChanged method

                                             } // end ItemListener
                                             );
            }

            constraints = new GridBagConstraints(); // @A1A
            VUtilities.constrain (outQFilter_,      // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  1,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // output queue Library
            if (disableFeature_ == true) // @A1A
            {
                outQLibFilter_.setEnabled(false);
            }
            else
            {
                outQLibFilter_.addItemListener( new ItemListener ()
                                                {
                                                public void itemStateChanged(ItemEvent e)
                                                    {
                                                    String newOutQLib = (String)outQLibFilter_.getSelectedItem();

                                                    // check to see if outq lib is all and outq is not all
                                                    if ( (newOutQLib.trim().equals(allText_)) &&
                                                         (!outQFilter_.getSelectedItem().equals(allText_)) )
                                                        // set the outq to all
                                                        outQFilter_.setSelectedItem(allText_);

                                                    // notify that something has changed so the apply button is enabled
                                                    changeEventSupport_.fireStateChanged ();
                                                    } // end itemStateChanged method

                                                } // end ItemListener
                                                );
            }

            constraints = new GridBagConstraints(); // @A1A
            VUtilities.constrain (outQLibFilter_,   // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  2,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // Form type filter
            constraints = new GridBagConstraints();             // @A1A
            VUtilities.constrain (new JLabel (formTypeText_),   // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  3,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            formTypeFilter_ = new JComboBox();
            formTypeFilter_.setEditable(true);
            formTypeFilter_.addItem(allText_);
            formTypeFilter_.addItem(standardText_);

            String curFormTypeFilter = list_.getFormTypeFilter().trim();
            if ( (curFormTypeFilter.equals("*ALL")) ||
                 (curFormTypeFilter.equals("")) )
                formTypeFilter_.setSelectedItem(allText_);
            else if (curFormTypeFilter.equals("*STD"))
                formTypeFilter_.setSelectedItem(standardText_);
            else
                {
                formTypeFilter_.addItem(curFormTypeFilter);
                formTypeFilter_.setSelectedItem(curFormTypeFilter);
                }
            formTypeFilter_.addItemListener(this);
            constraints = new GridBagConstraints(); // @A1A
            VUtilities.constrain (formTypeFilter_,  // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  3,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);

            // User-specified data filter
            constraints = new GridBagConstraints();                 // @A1A
            VUtilities.constrain (new JLabel (userSpecDataText_),   // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  0,1,1,
                                  4,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            userDataFilter_ = new JComboBox();
            userDataFilter_.setEditable(true);
            userDataFilter_.addItem(allText_);

            String curUserDataFilter = list_.getUserDataFilter().trim();
            if ( (curUserDataFilter.equals("*ALL")) ||
                 (curUserDataFilter.equals("")) )
                userDataFilter_.setSelectedItem(allText_);
            else
                {
                userDataFilter_.addItem(curUserDataFilter);
                userDataFilter_.setSelectedItem(curUserDataFilter);
                }

            userDataFilter_.addItemListener(this);
            constraints = new GridBagConstraints(); // @A1A
            VUtilities.constrain (userDataFilter_,  // @A1C
                                  thePane,
                                  layout,
                                  constraints,
                                  1,1,1,
                                  4,1,1,
                                  constraints.HORIZONTAL,
                                  constraints.WEST);
            }
        catch (Exception e)
            {
            thePane = null;
            errorEventSupport_.fireError (e);
            }

        return thePane;
    }


    // Returns the copyright.
    private static String getCopyright()
    {
        return Copyright_v.copyright;
    }


/**
Returns the editor pane.

@return             The properties pane.
@throws Exception   If an error occurs.
**/
    public Component getComponent ()
    {
        JTabbedPane tabbedPane = null;
        tabbedPane = new JTabbedPane ();
        tabbedPane.addTab (includeText_, null, buildIncludePane());
        tabbedPane.setSelectedIndex (0);
        return tabbedPane;
    }

/**
catches the state change of combo boxes
**/
    public void itemStateChanged(ItemEvent e)
    {
        // notify that something has changed so the apply button is enabled
        changeEventSupport_.fireStateChanged ();
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


} // end PrinterOutputPropertiesPane class


