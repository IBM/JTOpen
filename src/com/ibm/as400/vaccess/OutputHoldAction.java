///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: OutputHoldAction.java
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
import com.ibm.as400.access.Trace;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.SwingConstants;

/**
The OutputHoldAction class represents the action of holding a spooled file.
**/
class OutputHoldAction
extends DialogAction
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Private data.
    private static final String displayName_            = ResourceLoader.getText("ACTION_HOLD");
    private static final String prtOutToHoldText_       = ResourceLoader.getPrintText("PRINTER_OUTPUT_TO_HOLD") + ":";
    private static final String holdOutputText_         = ResourceLoader.getPrintText("HOLD_OUTPUT") + ":";
    private static final String immediatelyText_        = ResourceLoader.getPrintText("IMMEDIATELY");
    private static final String pageEndText_            = ResourceLoader.getPrintText("AT_PAGE_END");

    private SpooledFile splF_                           = null; // the spooled file
    private JRadioButton immedButton_                   = null; // hold immediately
    private JRadioButton endButton_                     = null; // hold at end of page

/**
Constructs an OutputHoldAction object.

@param  object      The object.
@param  splF        The spooled file.
**/
    public OutputHoldAction (VObject object, SpooledFile splF)
    {
        super (object);
        splF_ = splF;
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
            // build components to display the output to be held
            VUtilities.constrain( new JLabel(prtOutToHoldText_), panel, layout, 0,0,1,1);

            JTextField text = new JTextField(getObject().toString());
            text.setEditable(false);
            VUtilities.constrain( text, panel, layout, 0,1,1,1);

            VUtilities.constrain (new JLabel (" "), panel, layout, 0, 2, 2, 1);

            // build components to ask user to hold *IMMED or *PAGEEND
            VUtilities.constrain( new JLabel(holdOutputText_), panel, layout, 0,3,1,1);

            immedButton_ = new JRadioButton(immediatelyText_);
            immedButton_.setHorizontalAlignment(SwingConstants.LEFT);
            immedButton_.setSelected(true);
            endButton_ = new JRadioButton(pageEndText_);
            endButton_.setHorizontalAlignment(SwingConstants.LEFT);

            // group the buttons so that only one can be on at a time
            ButtonGroup group = new ButtonGroup();
            group.add(immedButton_);
            group.add(endButton_);

            VUtilities.constrain( immedButton_, panel, layout, 0,4,1,1);
            VUtilities.constrain( endButton_, panel, layout, 0,5,1,1);
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
        try {
            // fire started working event
            fireStartWorking();

            // determine which hold option to send
            if (immedButton_.isSelected()) splF_.hold("*IMMED");
            else splF_.hold("*PAGEEND");

            // fire stopped working event
            fireStopWorking();

            // trace the hold
            if (Trace.isTraceOn())
                Trace.log (Trace.INFORMATION, "Held file [" + splF_.getName () + "].");

            fireObjectChanged ();
            } // end try block
        catch (Exception e)
            {
            // trace the error
            if (Trace.isTraceOn())
                Trace.log (Trace.ERROR, "ERROR Holding file [" + splF_.getName () + "].");

            fireError (e);
            }
    }

} // end OutputHoldAction class

