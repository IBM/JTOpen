///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: OutputSendAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.SpooledFile;
import com.ibm.as400.access.PrintObject;
import com.ibm.as400.access.PrintParameterList;
import com.ibm.as400.access.Trace;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import com.ibm.as400.access.SpooledFile;
import javax.swing.JTextField;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.SwingConstants;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
The OutputSendAction class represents the action of sending a spooled file.
**/
class OutputSendAction
extends DialogAction
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Private data.
    private static final String displayName_            = ResourceLoader.getText("ACTION_SEND");
    private static final String prtOutToSendText_       = ResourceLoader.getPrintText("PRINTER_OUTPUT_TO_SEND") + ":";
    private static final String sendOutputText_         = ResourceLoader.getPrintText("SEND_TO") + ":";
    private static final String userNameText_           = ResourceLoader.getPrintText("USER_NAME");
    private static final String systemNameText_         = ResourceLoader.getPrintText("SYSTEM_NAME");
    private static final String recordFormatText_       = ResourceLoader.getPrintText("RECORD_FORMAT") + ":";
    private static final String recordDataOnlyText_     = ResourceLoader.getPrintText("RECORD_DATA");
    private static final String allDataText_            = ResourceLoader.getPrintText("ALL_DATA");
    private static final String vmClassText_            = ResourceLoader.getPrintText("VM_MVS_CLASS");
    private static final String sendPriorityText_       = ResourceLoader.getPrintText("SEND_PRIORITY") + ":";
    private static final String normalPriorityText_     = ResourceLoader.getPrintText("NORMAL_PRIORITY");
    private static final String highPriorityText_       = ResourceLoader.getPrintText("HIGH_PRIORITY");

    private SpooledFile splF_                           = null; // the spooled file
    private JTextField userNameField_                   = null; // user name
    private JTextField systemNameField_                 = null; // system name
    private JRadioButton recordDataButton_              = null; // record data only
    private JRadioButton allDataButton_                 = null; // all data
    private JComboBox vmClassBox_                       = null; // VM/MVX class
    private JRadioButton normalPriorityButton_          = null; // normal priority
    private JRadioButton highPriorityButton_            = null; // high priority

/**
Constructs an OutputSendAction object.

@param  object      The object.
@param  splF        The spooled file.
**/
    public OutputSendAction (VObject object, SpooledFile splF)
    {
        super (object);
        splF_ = splF;
    }


    // Returns the copyright.
    private static String getCopyright()
    {
        return Copyright_v.copyright;
    }


/**
Returns the component for the dialog box.

@return The component.
**/
    public JComponent getInputComponent()
    {
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout ();
        GridBagConstraints constraints;
        panel.setLayout (layout);
        panel.setBorder (new EmptyBorder (10, 10, 10, 10));

        try
            {
            // build components to display the output to be moved
            VUtilities.constrain( new JLabel(prtOutToSendText_), panel, layout, 0,0,1,1);

            JTextField text = new JTextField(getObject().toString());
            text.setEditable(false);
            VUtilities.constrain( text, panel, layout, 0,1,1,1);

            VUtilities.constrain (new JLabel (" "), panel, layout, 0, 2, 2, 1);

            // build components to ask user for send input
            VUtilities.constrain( new JLabel(sendOutputText_), panel, layout, 0,3,1,1);

            // request the user name
            VUtilities.constrain( new JLabel(userNameText_), panel, layout, 0,4,1,1);
            userNameField_ = new JTextField(10);
            VUtilities.constrain( userNameField_, panel, layout, 1,4,1,1);

            // request the system name
            VUtilities.constrain( new JLabel(systemNameText_), panel, layout, 0,5,1,1);
            systemNameField_ = new JTextField(10);
            VUtilities.constrain( systemNameField_, panel, layout, 1,5,1,1);

            VUtilities.constrain (new JLabel (" "), panel, layout, 0, 6, 2, 1);

            // request the record format
            VUtilities.constrain( new JLabel(recordFormatText_), panel, layout, 0,7,1,1);
            recordDataButton_ = new JRadioButton(recordDataOnlyText_);
            recordDataButton_.setHorizontalAlignment(SwingConstants.LEFT);
            allDataButton_ = new JRadioButton(allDataText_);
            allDataButton_.setHorizontalAlignment(SwingConstants.LEFT);
            allDataButton_.setSelected(true);

            // group the buttons so that only one can be on at a time
            ButtonGroup group = new ButtonGroup();
            group.add(recordDataButton_);
            group.add(allDataButton_);

            VUtilities.constrain( recordDataButton_, panel, layout, 0,8,1,1);
            VUtilities.constrain( allDataButton_, panel, layout, 0,9,1,1);

            VUtilities.constrain (new JLabel (" "), panel, layout, 0, 10, 2, 1);

            // request the VM/MVS class
            VUtilities.constrain( new JLabel(vmClassText_), panel, layout, 0,11,1,1);
            vmClassBox_ = new JComboBox();
            vmClassBox_.setEditable(false);
            vmClassBox_.addItem("A"); vmClassBox_.addItem("B"); vmClassBox_.addItem("C");
            vmClassBox_.addItem("D"); vmClassBox_.addItem("E"); vmClassBox_.addItem("F");
            vmClassBox_.addItem("G"); vmClassBox_.addItem("H"); vmClassBox_.addItem("I");
            vmClassBox_.addItem("J"); vmClassBox_.addItem("K"); vmClassBox_.addItem("L");
            vmClassBox_.addItem("M"); vmClassBox_.addItem("N"); vmClassBox_.addItem("O");
            vmClassBox_.addItem("P"); vmClassBox_.addItem("Q"); vmClassBox_.addItem("R");
            vmClassBox_.addItem("S"); vmClassBox_.addItem("T"); vmClassBox_.addItem("U");
            vmClassBox_.addItem("V"); vmClassBox_.addItem("W"); vmClassBox_.addItem("X");
            vmClassBox_.addItem("Y"); vmClassBox_.addItem("Z"); vmClassBox_.addItem("0");
            vmClassBox_.addItem("1"); vmClassBox_.addItem("2"); vmClassBox_.addItem("3");
            vmClassBox_.addItem("4"); vmClassBox_.addItem("5"); vmClassBox_.addItem("6");
            vmClassBox_.addItem("7"); vmClassBox_.addItem("8"); vmClassBox_.addItem("9");
            vmClassBox_.setSelectedItem("A");
            VUtilities.constrain( vmClassBox_, panel, layout, 1,11,1,1);

            VUtilities.constrain (new JLabel (" "), panel, layout, 0, 12, 2, 1);

            // request the send priority
            VUtilities.constrain( new JLabel(sendPriorityText_), panel, layout, 0,13,1,1);
            normalPriorityButton_ = new JRadioButton(normalPriorityText_);
            normalPriorityButton_.setHorizontalAlignment(SwingConstants.LEFT);
            normalPriorityButton_.setSelected(true);
            highPriorityButton_ = new JRadioButton(highPriorityText_);
            highPriorityButton_.setHorizontalAlignment(SwingConstants.LEFT);

            // group the buttons so that only one can be on at a time
            ButtonGroup groupA = new ButtonGroup();
            groupA.add(normalPriorityButton_);
            groupA.add(highPriorityButton_);

            VUtilities.constrain( normalPriorityButton_, panel, layout, 0,14,1,1);
            VUtilities.constrain( highPriorityButton_, panel, layout, 0,15,1,1);
            }
        catch (Exception e)
            {
            panel = null;
            fireError (e);
            }

        return panel;
    }


/**
Returns the display name for the action.

@return The display name.
**/
    public String getText ()
    {
        return displayName_;
    }

/**
Performs the action.
**/
    public void perform2 ()
    {
        try
            {
            // create a print parm list to hold the information we need to send
            PrintParameterList pList = new PrintParameterList();

            // set the user name
            pList.setParameter(PrintObject.ATTR_TOUSERID, userNameField_.getText().trim());

            // set the system name
            pList.setParameter(PrintObject.ATTR_TOADDRESS, systemNameField_.getText().trim());

            // retrieve the record format
            if (recordDataButton_.isSelected()) pList.setParameter(PrintObject.ATTR_DATAFORMAT, "*RCDDATA");
            else pList.setParameter(PrintObject.ATTR_DATAFORMAT, "*ALLDATA");

            // retrieve the vm/mvs class
            pList.setParameter(PrintObject.ATTR_VMMVSCLASS, (String)vmClassBox_.getSelectedItem());

            // retrieve the send priority
            if (normalPriorityButton_.isSelected()) pList.setParameter(PrintObject.ATTR_SENDPTY, "*NORMAL");
            else pList.setParameter(PrintObject.ATTR_SENDPTY, "*HIGH");

            // fire started working event
            fireStartWorking();

            // send the spooled file
            splF_.sendNet(pList);

            // fire stopped working event
            fireStopWorking();

            // trace the send
            if (Trace.isTraceOn())
                Trace.log (Trace.INFORMATION, "Sent file [" + splF_.getName () + "].");

            fireObjectChanged ();
            } // end try block
        catch (Exception e)
            {
            // trace the error
            if (Trace.isTraceOn())
                Trace.log (Trace.ERROR, "ERROR Sending file [" + splF_.getName () + "].");

            fireError (e);
            }
    }

} // end OutputSendAction class

