///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: MessagePropertiesPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.Trace;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;



/**
The MessagePropertiesPane class represents the properties pane
for a message.
**/
class MessagePropertiesPane
implements VPropertiesPane
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String dateText_               = ResourceLoader.getText ("MESSAGE_DATE") + ": ";
    private static final String generalTabText_         = ResourceLoader.getText ("TAB_GENERAL");
    private static final String messageFileText_        = ResourceLoader.getText ("MESSAGE_FILE") + ": ";
    private static final String severityText_           = ResourceLoader.getText ("MESSAGE_SEVERITY") + ": ";
    private static final String typeText_               = ResourceLoader.getText ("MESSAGE_TYPE") + ": ";



    // Static data.
    private static  DateFormat  dateFormat_     = DateFormat.getDateTimeInstance ();



    // Private data.
    private AS400Message        message_;
    private VMessage            object_;



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
Constructs an MessagePropertiesPane object.

@param  object      The object.
@param  message     The message.
**/
    public MessagePropertiesPane (VMessage object, AS400Message message)
    {
        object_     = object;
        message_    = message;
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

//@B0D        JTextArea text = new JTextArea (VUtilities.formatHelp (message_.getText (), 80));
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
//@B0D                JTextArea helpText = new JTextArea (VUtilities.formatHelp (help, 80));
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
        Calendar date = message_.getDate ();
        if (date != null)
            VUtilities.constrain (new JLabel (dateFormat_.format (date.getTime ())),
                generalTab, layout, 1, row, 1, 1);
        row++;

        VUtilities.constrain (new JSeparator (),
            generalTab, layout, 0, row++, 2, 1);

        VUtilities.constrain (new JLabel (messageFileText_),
            generalTab, layout, 0, row, 1, 1);
        VUtilities.constrain (new JLabel (message_.getPath ()),
            generalTab, layout, 1, row, 1, 1);
        row++;

        // Build the pane.
        JTabbedPane pane = new JTabbedPane ();
        pane.addTab (generalTabText_, null, generalTab);
        pane.setSelectedIndex (0);
        return pane;
    }



/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
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
