///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: QueuedMessagePropertiesPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.MessageQueue;
import com.ibm.as400.access.QueuedMessage;
import com.ibm.as400.access.QSYSObjectPathName;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;



/**
The QueuedMessagePropertyPane class represents the properties pane
for a queued message.
**/
class QueuedMessagePropertiesPane
implements VPropertiesPane
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String dateText_               = ResourceLoader.getText ("MESSAGE_DATE") + ":";
    private static final String fromJobText_            = ResourceLoader.getText ("MESSAGE_FROM_JOB") + ":";
    private static final String fromJobNumberText_      = ResourceLoader.getText ("MESSAGE_FROM_JOB_NUMBER") + ":";
    private static final String fromProgramText_        = ResourceLoader.getText ("MESSAGE_FROM_PROGRAM") + ":";
    private static final String fromUserText_           = ResourceLoader.getText ("MESSAGE_FROM_USER") + ":";
    private static final String generalTabText_         = ResourceLoader.getText ("TAB_GENERAL");
    private static final String messageQueueText_       = ResourceLoader.getText ("MESSAGE_QUEUE") + ":";
    private static final String severityText_           = ResourceLoader.getText ("MESSAGE_SEVERITY") + ":";
    private static final String typeText_               = ResourceLoader.getText ("MESSAGE_TYPE") + ":";



    // Static data.
    private static  DateFormat  dateFormat_     = DateFormat.getDateTimeInstance ();



    // Private data.
    private QueuedMessage       message_;
    private VObject             object_;



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
Constructs a QueuedMessagePropertiesPane object.

@param  object      The object.
@param  message     The queued message.
**/
    public QueuedMessagePropertiesPane (VObject object,
                                        QueuedMessage message)
    {
        object_ = object;
        message_ = message;
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

//@B0D        JTextArea text = new JTextArea (VUtilities.formatHelp (message_.getText (), 60)); // @A1C
        JTextArea text = new JTextArea (message_.getText()); //@B0A
        text.setEditable (false);
        text.setBackground (generalTab.getBackground ());
        text.setColumns(40);          //@B0A
        text.setLineWrap(true);       //@B0A
        text.setWrapStyleWord(true);  //@B0A
        VUtilities.constrain (text,
            generalTab, layout, 0, row++, 2, 1);

        String help = message_.getHelp ();
        if (help != null)
            if (help.length () > 0) {
//@B0D                JTextArea helpText = new JTextArea (VUtilities.formatHelp (help, 60));  // @A1C
                JTextArea helpText = new JTextArea(help); //@B0A
                helpText.setEditable (false);
                helpText.setBackground (generalTab.getBackground ());
                helpText.setColumns(40);          //@B0A
                helpText.setLineWrap(true);       //@B0A
                helpText.setWrapStyleWord(true);  //@B0A
                VUtilities.constrain (helpText,
                    generalTab, layout, 0, row++, 2, 1);
            }

        VUtilities.constrain (new JSeparator (),
            generalTab, layout, 0, row++, 2, 1);

        VUtilities.constrain (new JLabel (severityText_),
            generalTab, layout, 0, row, 1, 1);
        VUtilities.constrain (new JLabel (Integer.toString (message_.getSeverity ())),
            generalTab, layout, 1, row, 1, 1);
        row++;

        VUtilities.constrain (new JLabel (typeText_),
            generalTab, layout, 0, row, 1, 1);
        VUtilities.constrain (new JLabel (MessageUtilities.getTypeText (message_.getType ())),
            generalTab, layout, 1, row, 1, 1);
        row++;

        VUtilities.constrain (new JLabel (dateText_),
            generalTab, layout, 0, row, 1, 1);
        Date date = message_.getDate ().getTime ();
        if (date != null)
            VUtilities.constrain (new JLabel (dateFormat_.format (date)),
                generalTab, layout, 1, row, 1, 1);
        row++;

        VUtilities.constrain (new JSeparator (),
            generalTab, layout, 0, row++, 2, 1);

        VUtilities.constrain (new JLabel (fromJobText_),
            generalTab, layout, 0, row, 1, 1);
        VUtilities.constrain (new JLabel (message_.getFromJobName ()),
            generalTab, layout, 1, row, 1, 1);
        row++;

        VUtilities.constrain (new JLabel (fromUserText_),
            generalTab, layout, 0, row, 1, 1);
        VUtilities.constrain (new JLabel (message_.getUser ()),
            generalTab, layout, 1, row, 1, 1);
        row++;

        VUtilities.constrain (new JLabel (fromJobNumberText_),
            generalTab, layout, 0, row, 1, 1);
        VUtilities.constrain (new JLabel (message_.getFromJobNumber ()),
            generalTab, layout, 1, row, 1, 1);
        row++;

        VUtilities.constrain (new JSeparator (),
            generalTab, layout, 0, row++, 2, 1);

        VUtilities.constrain (new JLabel (fromProgramText_),
            generalTab, layout, 0, row, 1, 1);
        VUtilities.constrain (new JLabel (message_.getFromProgram ()),
            generalTab, layout, 1, row, 1, 1);
        row++;

        VUtilities.constrain (new JSeparator (),
            generalTab, layout, 0, row++, 2, 1);

        VUtilities.constrain (new JLabel (messageQueueText_),
            generalTab, layout, 0, row, 1, 1);
        MessageQueue queue = message_.getQueue ();
        String path = (queue == null) ? "" : queue.getPath ();
        VUtilities.constrain (new JLabel (path),
            generalTab, layout, 1, row, 1, 1);
        row++;

        JTabbedPane tabbedPane = new JTabbedPane ();

        tabbedPane.addTab (generalTabText_, null, generalTab );

        tabbedPane.setSelectedIndex (0);

        return tabbedPane;
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
