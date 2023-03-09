///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: MessageQueuePropertiesPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.MessageQueue;
import com.ibm.as400.access.Trace;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;



/**
The MessageQueuePropertyPane class represents the properties pane
for a message queue.
**/
class MessageQueuePropertiesPane
implements VPropertiesPane
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String allMessagesChoice_              = ResourceLoader.getText ("MESSAGE_QUEUE_CHOICE_ALL");
    private static final String generalTabText_                 = ResourceLoader.getText ("TAB_GENERAL");
    private static final String messagesNeedingReplyChoice_     = ResourceLoader.getText ("MESSAGE_QUEUE_CHOICE_MNR");
    private static final String messagesNotNeedingReplyChoice_  = ResourceLoader.getText ("MESSAGE_QUEUE_CHOICE_MNNR");
    private static final String selectionPrompt_                = ResourceLoader.getText ("MESSAGE_SELECTION") + ": ";
    private static final String sendersCopyNeedingReplyChoice_  = ResourceLoader.getText ("MESSAGE_QUEUE_CHOICE_SCNR");
    private static final String severityPrompt_                 = ResourceLoader.getText ("MESSAGE_SEVERITY") + ": ";



    // Constants.
    private static final String allMessagesAbbreviation_                = "*ALL";
    private static final String messagesNeedingReplyAbbreviation_       = "*MNR";
    private static final String messagesNotNeedingReplyAbbreviation_    = "*MNNR";
    private static final String sendersCopyNeedingReplyAbbreviation_    = "*SCNR";



    // Private data.
    private VMessageQueue   object_;
    private JComboBox       selection_;
    private JTextField      severity_;



    // Event support.
    ChangeEventSupport  changeEventSupport_     = new ChangeEventSupport (this);    // Private.
    ErrorEventSupport   errorEventSupport_      = new ErrorEventSupport (this);     // Private.
    VObjectEventSupport objectEventSupport_     = new VObjectEventSupport (this);   // Private.
    WorkingEventSupport workingEventSupport_    = new WorkingEventSupport (this);   // Private.



/**
Constructs a MessageQueuePropertyPane object.

@param object   The object.
**/
    public MessageQueuePropertiesPane (VMessageQueue object)
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
        // Apply severity.
        object_.setSeverity (Integer.parseInt (severity_.getText ()));

        // Apply selection.
        Object userSelection = selection_.getSelectedItem ();
        if (userSelection.equals (messagesNeedingReplyChoice_))
            object_.setSelection (messagesNeedingReplyAbbreviation_);
        else if (userSelection.equals (sendersCopyNeedingReplyChoice_))
            object_.setSelection (sendersCopyNeedingReplyAbbreviation_);
        else if (userSelection.equals (messagesNotNeedingReplyChoice_))
            object_.setSelection (messagesNotNeedingReplyAbbreviation_);
        else
            object_.setSelection (allMessagesAbbreviation_);

        // Reload.
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

        // Initialize a key listener.
        KeyListener keyListener = new KeyAdapter () {
            public void keyPressed (KeyEvent event) { changeEventSupport_.fireStateChanged (); } // @C1A
            public void keyTyped (KeyEvent event) { changeEventSupport_.fireStateChanged (); }
            };

        // Queue information.
        VUtilities.constrain (new JLabel (object_.getText (), object_.getIcon (32, false),  SwingConstants.LEFT),
            generalTab, layout, 0, row++, 2, 1);

        VUtilities.constrain (new JSeparator (),
            generalTab, layout, 0, row++, 2, 1);

        // Severity prompt.
        VUtilities.constrain (new JLabel (severityPrompt_),
            generalTab, layout, 0, row, 1, 1);

        severity_ = new JTextField (Integer.toString (object_.getSeverity ()));
        severity_.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent event) {
                changeEventSupport_.fireStateChanged ();
            }});
        severity_.addKeyListener (keyListener);

        VUtilities.constrain (severity_,
            generalTab, layout, 1, row++, 1, 1);

        // Selection prompt.
        VUtilities.constrain (new JLabel (selectionPrompt_),
            generalTab, layout, 0, row, 1, 1);

        selection_ = new JComboBox ();
        selection_.setEditable (false);
        selection_.addItem (allMessagesChoice_);
        selection_.addItem (messagesNeedingReplyChoice_);
        selection_.addItem (sendersCopyNeedingReplyChoice_);
        selection_.addItem (messagesNotNeedingReplyChoice_);

        String initialSelection = object_.getSelection ();
        if (initialSelection.equals (messagesNeedingReplyAbbreviation_))
            selection_.setSelectedItem (messagesNeedingReplyChoice_);
        else if (initialSelection.equals (sendersCopyNeedingReplyAbbreviation_))
            selection_.setSelectedItem (sendersCopyNeedingReplyChoice_);
        else if (initialSelection.equals (messagesNotNeedingReplyAbbreviation_))
            selection_.setSelectedItem (messagesNotNeedingReplyChoice_);
        else
            selection_.setSelectedItem (allMessagesChoice_);

        selection_.addItemListener (new ItemListener () {
            public void itemStateChanged (ItemEvent event) {
                changeEventSupport_.fireStateChanged ();
            }});

        VUtilities.constrain (selection_,
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
