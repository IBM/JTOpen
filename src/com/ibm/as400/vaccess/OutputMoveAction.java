///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: OutputMoveAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.SpooledFile;
import com.ibm.as400.access.OutputQueue;
import com.ibm.as400.access.PrintObject;
import com.ibm.as400.access.Trace;
import javax.swing.JComboBox;
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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
The OutputMoveAction class represents the action of moving a spooled file.
**/
class OutputMoveAction
extends DialogAction
implements ActionListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Private data.
    private static final String displayName_            = ResourceLoader.getText("ACTION_MOVE");
    private static final String prtOutToMoveText_       = ResourceLoader.getPrintText("PRINTER_OUTPUT_TO_MOVE") + ":";
    private static final String moveOutputText_         = ResourceLoader.getPrintText("MOVE_OUTPUT") + ":";
    private static final String useLibListText_         = ResourceLoader.getPrintText("USE_LIBRARY_LIST");
    private static final String printerText_            = ResourceLoader.getPrintText("PRINTER");
    private static final String outQText_               = ResourceLoader.getPrintText("OUTPUT_QUEUE");
    private static final String outQLibText_            = ResourceLoader.getPrintText("LIBRARY");

    private SpooledFile splF_                           = null; // the spooled file
    private JRadioButton printerButton_                 = null; // printer
    private JTextField printerField_                    = null; // printer name
    private JRadioButton outQButton_                    = null; // output queue
    private JTextField outQField_                       = null; // output queue name
    private JComboBox outQLibBox_                       = null; // output queue library

    private VPrinterOutput parent_                      = null; // parent (the spooled list)

/**
Constructs an OutputMoveAction object.

@param  object      The object.
@param  splF        The spooled file.
**/
    public OutputMoveAction (VObject object, SpooledFile splF, VPrinterOutput parent)
    {
        super (object);
        splF_ = splF;
        parent_ = parent;
    }

/**
catches the state change of radio buttons
**/
    public void actionPerformed(ActionEvent e)
    {
        // if the printer radio button is selected
        if (e.getActionCommand() == printerText_)
            {
            // enable the printer field
            printerField_.setEnabled(true);

            // disable the output queue fields
            outQField_.setEnabled(false);
            outQLibBox_.setEnabled(false);
            }
        else // the output queue button is selected
            {
            // enable the output queue fields
            outQField_.setEnabled(true);
            outQLibBox_.setEnabled(true);

            // disable the printer field
            printerField_.setEnabled(false);
            }
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
            VUtilities.constrain( new JLabel(prtOutToMoveText_), panel, layout, 0,0,1,1);

            JTextField text = new JTextField(getObject().toString());
            text.setEditable(false);
            VUtilities.constrain( text, panel, layout, 0,1,1,1);

            VUtilities.constrain (new JLabel (" "), panel, layout, 0, 2, 2, 1);

            // build components to ask user for move input
            VUtilities.constrain( new JLabel(moveOutputText_), panel, layout, 0,3,1,1);

            // build printer/output queue buttons
            printerButton_ = new JRadioButton(printerText_);
            printerButton_.setHorizontalAlignment(SwingConstants.LEFT);
            printerButton_.setSelected(true);
            outQButton_ = new JRadioButton(outQText_);
            outQButton_.setHorizontalAlignment(SwingConstants.LEFT);

            // add listeners for the printer and output queue buttons
            printerButton_.addActionListener(this);
            outQButton_.addActionListener(this);

            // group the buttons so that only one can be on at a time
            ButtonGroup group = new ButtonGroup();
            group.add(printerButton_);
            group.add(outQButton_);

            // printer name
            printerField_ = new JTextField(10);
            VUtilities.constrain( printerButton_, panel, layout, 0,4,1,1);
            VUtilities.constrain( printerField_, panel, layout, 1,4,1,1);

            // output queue name
            outQField_ = new JTextField(10);
            outQField_.setEnabled(false); // disable until outQButton is selected
            VUtilities.constrain( outQButton_, panel, layout, 0,5,1,1);
            VUtilities.constrain( outQField_, panel, layout, 1,5,1,1);

            // output queue library
            outQLibBox_ = new JComboBox();
            outQLibBox_.setEditable(true);
            outQLibBox_.addItem(useLibListText_);
            outQLibBox_.setEnabled(false); // disable until outQButton is selected

            VUtilities.constrain( new JLabel(outQLibText_), panel, layout, 0,6,1,1);
            VUtilities.constrain( outQLibBox_, panel, layout, 1,6,1,1);
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
            // determine if they selected printer
            if (printerButton_.isSelected())
                {
                // retrieve the printer name
                String ptr = printerField_.getText().trim();

                // build an output queue object using the printer name
                // Note:  QUSRSYS is the library name for all system printers
                OutputQueue outQ = new OutputQueue(splF_.getSystem(), "/QSYS.LIB/QUSRSYS.LIB/" + ptr + ".OUTQ");

                // fire started working event
                fireStartWorking();

                // move the spooled file to the output queue
                splF_.move(outQ);

                // fire stopped working event
                fireStopWorking();

                // trace the move
                if (Trace.isTraceOn())
                    Trace.log (Trace.INFORMATION, "Moved file ["
                               + splF_.getName () + "].");
                }
            // they selected output queue
            else
                {
                // retieve the output queue name
                String outputQ = outQField_.getText().trim();

                // retrieve the library value
                String outputQLib = (String)outQLibBox_.getSelectedItem();

                OutputQueue outQ = null;

                // if use the library list was selected
                if (outputQLib.equals(useLibListText_))
                    // build an output queue object using name
                    outQ = new OutputQueue(splF_.getSystem(), "/QSYS.LIB/%LIBL%.LIB/" + outputQ + ".OUTQ");
                else
                    // build an output queue object using name and lib
                    outQ = new OutputQueue(splF_.getSystem(), "/QSYS.LIB/" + outputQLib + ".LIB/" + outputQ + ".OUTQ");

                // fire started working event
                fireStartWorking();

                // move the spooled file to the output queue
                splF_.move(outQ);

                // fire stopped working event
                fireStopWorking();

                // trace the move
                if (Trace.isTraceOn())
                    Trace.log (Trace.INFORMATION, "Moved file ["
                               + splF_.getName () + "].");
                }

            // fire the object changed passing in the object's parent so the list is
            // rearranged
            fireObjectChanged (parent_);
            } // end try block
        catch (Exception e)
            {
            // trace the error
            if (Trace.isTraceOn())
                Trace.log (Trace.ERROR, "ERROR Moving file [" + splF_.getName () + "].");

            fireError (e);
            }
    }

} // end OutputMoveAction class

