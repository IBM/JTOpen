///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: UserListPropertiesPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.UserList;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;



/**
The UserListPropertiesPane class represents the properties pane
for a user list.
**/
class UserListPropertiesPane
implements VPropertiesPane
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String generalTabText_                 = ResourceLoader.getText ("TAB_GENERAL");
    private static final String groupInfoPrompt_                = ResourceLoader.getText ("USER_LIST_GROUPINFO_PROMPT") + ": ";
    private static final String userInfoPrompt_                 = ResourceLoader.getText ("USER_LIST_USERINFO_PROMPT") + ": ";



    // Private data.
    private VUserList       object_;
    private JComboBox       groupInfo_;
    private JComboBox       userInfo_;



    // Event support.
    ChangeEventSupport  changeEventSupport_     = new ChangeEventSupport (this);    // Private.
    ErrorEventSupport   errorEventSupport_      = new ErrorEventSupport (this);     // Private.
    VObjectEventSupport objectEventSupport_     = new VObjectEventSupport (this);   // Private.
    WorkingEventSupport workingEventSupport_    = new WorkingEventSupport (this);   // Private.



/**
Constructs a UserListPropertyPane object.

@param object   The object.
**/
    public UserListPropertiesPane (VUserList object, UserList list) //@A1C
    {
        object_ = object;
    }



/**
Adds a listener to be notified when the user makes a change.

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

@exception Exception   If an error occurs.
**/
    public void applyChanges ()
        throws Exception
    {
        object_.setUserInfo (userInfo_.getSelectedItem ().toString ());
        object_.setGroupInfo (groupInfo_.getSelectedItem ().toString ());
        object_.load ();
    }



/**
Returns the graphical user interface component.

@return             The component.
**/
    public Component getComponent ()
    {
        // Initialize the general tab.
        JPanel generalTab = new JPanel ();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        generalTab.setLayout (layout);
        generalTab.setBorder (new EmptyBorder (10, 10, 10, 10));
        int row = 0;

        // Initialize a change listener.
        ChangeListener changeListener = new ChangeListener () {
            public void stateChanged (ChangeEvent event) {
                changeEventSupport_.fireStateChanged ();
            }};

        // Initialize an item listener.
        ItemListener itemListener = new ItemListener () {
            public void itemStateChanged (ItemEvent event) {
                changeEventSupport_.fireStateChanged ();
            }};

        // Initialize a key listener.
        KeyListener keyListener = new KeyAdapter () {
            public void keyTyped (KeyEvent event) { changeEventSupport_.fireStateChanged (); }
            };

        // User list information.
        VUtilities.constrain (new JLabel (object_.getText (), object_.getIcon (32, false),  SwingConstants.LEFT),
            generalTab, layout, 0, row++, 2, 1);

        VUtilities.constrain (new JSeparator (),
            generalTab, layout, 0, row++, 2, 1);

        // User info prompt.
        VUtilities.constrain (new JLabel (userInfoPrompt_),
            generalTab, layout, 0, row, 1, 1);

        userInfo_ = new JComboBox ();
        userInfo_.setEditable (false);
        userInfo_.addItem (UserList.ALL);
        userInfo_.addItem (UserList.USER);
        userInfo_.addItem (UserList.GROUP);
        userInfo_.addItem (UserList.MEMBER);
        userInfo_.setSelectedItem (object_.getUserInfo ());
        userInfo_.addItemListener (itemListener);

        VUtilities.constrain (userInfo_,
            generalTab, layout, 1, row++, 1, 1);

        // Group info prompt.
        VUtilities.constrain (new JLabel (groupInfoPrompt_),
            generalTab, layout, 0, row, 1, 1);

        groupInfo_ = new JComboBox ();
        groupInfo_.setEditable (true);
        groupInfo_.addItem (UserList.NONE);
        groupInfo_.addItem (UserList.NOGROUP);
        groupInfo_.setSelectedItem (object_.getGroupInfo ());
        groupInfo_.addItemListener (itemListener);
        groupInfo_.getEditor ().getEditorComponent ().addKeyListener (keyListener); // This does not really work!

        VUtilities.constrain (groupInfo_,
            generalTab, layout, 1, row++, 1, 1);

        // Build the pane.
        JTabbedPane pane = new JTabbedPane ();
        pane.addTab (generalTabText_, null, generalTab);
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
Removes an error listener.

@param  listener    The listener.
**/
    public void removeErrorListener (ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener (listener);
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



}

