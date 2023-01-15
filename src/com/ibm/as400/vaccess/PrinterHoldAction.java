///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrinterHoldAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Printer;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.PrintObject;
import com.ibm.as400.access.Trace;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.GridBagLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.SwingConstants;

/**
The PrinterHoldAction class represents the action of holding a printer.
The actual affect is to hold the writer that is associated with the
printer.
**/
class PrinterHoldAction
extends DialogAction
{
  static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Private data.
    private static final String displayName_            = ResourceLoader.getText("ACTION_HOLD");
    private static final String prtrToHoldText_         = ResourceLoader.getPrintText("PRINTER_TO_HOLD") + ":";
    private static final String holdPrinterText_        = ResourceLoader.getPrintText("HOLD_PRINTER") + ":";
    private static final String immediatelyText_        = ResourceLoader.getPrintText("IMMEDIATELY");
    private static final String pageEndText_            = ResourceLoader.getPrintText("AT_PAGE_END");
    private static final String copyEndText_            = ResourceLoader.getPrintText("AT_COPY_END");

    private Printer printer_                            = null; // the printer
    private JRadioButton immedButton_                   = null; // hold immediately
    private JRadioButton endPageButton_                 = null; // hold at end of page
    private JRadioButton endCopyButton_                 = null; // hold at end of current copy

/**
Constructs an PrinterHoldAction object.

@param  object      The object.
@param  printer     The printer.
**/
    public PrinterHoldAction (VObject object, Printer printer )
    {
        super (object);
        printer_ = printer;
    }


/**
Returns the component for the dialog box.

@return The component.
**/
    public JComponent getInputComponent()
    {
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout ();
        // GridBagConstraints constraints;
        panel.setLayout (layout);
        panel.setBorder (new EmptyBorder (10, 10, 10, 10));

        try
        {
            // build components to display the printer to be held
            VUtilities.constrain( new JLabel(prtrToHoldText_), panel, layout, 0,0,1,1);

            // get the printer name
            JTextField text = new JTextField(printer_.getName().trim());
            text.setEditable(false);
            VUtilities.constrain( text, panel, layout, 0,1,1,1);
            VUtilities.constrain (new JLabel (" "), panel, layout, 0, 2, 2, 1);
            // build components to ask user to hold *IMMED or *PAGEEND
            VUtilities.constrain( new JLabel(holdPrinterText_), panel, layout, 0,3,1,1);

            immedButton_ = new JRadioButton(immediatelyText_);
            immedButton_.setHorizontalAlignment(SwingConstants.LEFT);
            immedButton_.setSelected(true);
            endPageButton_ = new JRadioButton(pageEndText_);
            endPageButton_.setHorizontalAlignment(SwingConstants.LEFT);
            endCopyButton_ = new JRadioButton(copyEndText_);
            endCopyButton_.setHorizontalAlignment(SwingConstants.LEFT);

            // group the buttons so that only one can be on at a time
            ButtonGroup group = new ButtonGroup();
            group.add(immedButton_);
            group.add(endPageButton_);
            group.add(endCopyButton_);

            VUtilities.constrain( immedButton_, panel, layout, 0,4,1,1);
            VUtilities.constrain( endPageButton_, panel, layout, 0,5,1,1);
            VUtilities.constrain( endCopyButton_, panel, layout, 0,6,1,1);
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
            // fire started working event
            fireStartWorking();

            // We need to get the status of the writer associated with this printer.
            String status_ = printer_.getStringAttribute(PrintObject.ATTR_WTRJOBNAME).trim();

            // If the writer name is null then there is no writer and we
            // shouldn't even be here.
            if((status_ == null) || ("".equals(status_)))
            {
                // Trace the error
                if (Trace.isTraceOn())
                    Trace.log (Trace.ERROR, "ERROR No writer for [" + printer_.getName () + "].");

                Exception e = new Exception(ResourceLoader.getText("EXC_AS400_ERROR"));         //@A1A
                fireError(e);                                                                   //@A1A
            }
            else
            {
                // The writer exists so issue the call
                CommandCall cmd = new CommandCall( printer_.getSystem());
                String cmdString = "HLDWTR WTR("+ printer_.getName() + ") OPTION(";
                try
                {
                    // Finish the command string
                    if (immedButton_.isSelected())
                        cmdString += "*IMMED)";
                    else if (endPageButton_.isSelected())
                        cmdString += "*PAGEEND)";
                    else if (endCopyButton_.isSelected())
                        cmdString += "*CNTRLD)";

                    if (cmd.run(cmdString)!=true)
                    {
                        // Note that there was an error
                        if (Trace.isTraceOn())
                            Trace.log (Trace.ERROR, "ERROR HLDWTR cmd for [" + printer_.getName () + "].");

                        // fire an error event
                        Exception e = new Exception(ResourceLoader.getText("EXC_AS400_ERROR"));
                        fireError(e);
                    }
                    else                                                    //@A1A
                    {
                        // Everything worked great
                        // trace the hold
                        if (Trace.isTraceOn())                              //@A1M
                            Trace.log (Trace.INFORMATION, "Held printer [" + printer_.getName () + "]."); //@A1M

                        fireObjectChanged ();                               //@A1M
                    }
                    // Show the messages (returned whether or not there was an error)
                    if (Trace.isTraceOn())
                    {
                        AS400Message[] messagelist = cmd.getMessageList();
                        for (int i=0; i < messagelist.length; i++)
                        {
                            // show each message
                            Trace.log (Trace.INFORMATION, messagelist[i].getText());    //@A1C
                        }
                    }
                }
                catch (Exception e)
                {
                    if (Trace.isTraceOn())
                        Trace.log (Trace.ERROR, "ERROR CommandCall exception for [" + printer_.getName () + "].");

                    fireError(e);                       //@A1A
                }
            }

            // fire stopped working event
            fireStopWorking();
        } // end try block
        catch (Exception e)
        {
            // trace the error
            if (Trace.isTraceOn())
                Trace.log (Trace.ERROR, "ERROR Holding printer [" + printer_.getName () + "].");

            fireError (e);
        }
    }

} // end PrinterHoldAction class

