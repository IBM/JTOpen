///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrinterStopAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Printer;
import com.ibm.as400.access.WriterJob;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.WriterJobList;
import com.ibm.as400.access.PrintObject;

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
The PrinterStopAction class represents the action of holding a printer.
The actual affect is to hold the writer that is associated with the
printer.
**/
class PrinterStopAction
extends DialogAction
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Private data.
    private static final String displayName_            = ResourceLoader.getText("ACTION_STOP");
    private static String prtrToStopText_         = ResourceLoader.getPrintText("PRINTER_TO_STOP") + ":";
    private static String stopPrinterText_        = ResourceLoader.getPrintText("STOP_PRINTING") + ":";
    private static String immediatelyText_        = ResourceLoader.getPrintText("IMMEDIATELY");
    private static String pageEndText_            = ResourceLoader.getPrintText("AT_PAGE_END");
    private static String copyEndText_            = ResourceLoader.getPrintText("AT_COPY_END");

    private static boolean stringsLoaded_               = false; // Load MRI only once when needed
    private Integer             stringsLock_            = new Integer (0);

    private Printer printer_                            = null; // the printer
    private JRadioButton immedButton_                   = null; // end immediately
    private JRadioButton endPageButton_                 = null; // end at end of page
    private JRadioButton endCopyButton_                 = null; // end at end of current copy

/**
Constructs an PrinterStopAction object.

@param  object      The object.
@param  printer     The printer.
**/
    public PrinterStopAction (VObject object, Printer printer )
    {
        super (object);
        printer_ = printer;
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
            if(stringsLoaded_ == false)
                loadMRI();

            // build components to display the printer to be held
            VUtilities.constrain( new JLabel(prtrToStopText_), panel, layout, 0,0,1,1);

            // get the printer name
            JTextField text = new JTextField(printer_.getName().trim());
            text.setEditable(false);
            VUtilities.constrain( text, panel, layout, 1,0,1,1);
            VUtilities.constrain (new JLabel (" "), panel, layout, 0, 1, 2, 1);

            // build components to ask user to hold *IMMED or *PAGEEND
            VUtilities.constrain( new JLabel(stopPrinterText_), panel, layout, 0,2,1,1);

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

            VUtilities.constrain( immedButton_, panel, layout, 0,3,1,1);
            VUtilities.constrain( endPageButton_, panel, layout, 0,4,1,1);
            VUtilities.constrain( endCopyButton_, panel, layout, 0,5,1,1);
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


/*
Loads the MRI strings.  This is called only once per class
*/
    private void loadMRI()
    {
        synchronized (stringsLock_)
        {
            prtrToStopText_         = ResourceLoader.getPrintText("PRINTER_TO_STOP") + ":";
            stopPrinterText_        = ResourceLoader.getPrintText("STOP_PRINTING") + ":";
            immediatelyText_        = ResourceLoader.getPrintText("IMMEDIATELY");
            pageEndText_            = ResourceLoader.getPrintText("AT_PAGE_END");
            copyEndText_            = ResourceLoader.getPrintText("AT_COPY_END");
            stringsLoaded_          = true;
        }
    }

/**
Performs the action.
**/
    public void perform2 ()
    {
        try {
            // fire started working event
            fireStartWorking();

            // We need to get the status of the writer associated with this printer.
            String status_ = printer_.getStringAttribute(PrintObject.ATTR_WTRJOBNAME).trim();

            // If the writer name is null then there is no writer and we
            // shouldn't even be here.
            if((status_ == null) || (status_ == ""))
            {
                // Trace the error
                if (Trace.isTraceOn())
                    Trace.log (Trace.ERROR, "ERROR No writer for [" + printer_.getName () + "].");

                // fire an error event
                Exception e = new Exception(ResourceLoader.getText("EXC_AS400_ERROR"));   //@A1A
                fireError(e);                                                             //@A1A
            }
            else
            {
                // The writer exists so issue the call
                CommandCall cmd = new CommandCall( printer_.getSystem());
                String cmdString = new String("ENDWTR WTR("+ printer_.getName() + ") OPTION(");
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
                            Trace.log (Trace.ERROR, "ERROR ENDWTR cmd for [" + printer_.getName () + "].");

                        // fire an error event
                        Exception e = new Exception(ResourceLoader.getText("EXC_AS400_ERROR"));
                        fireError(e);
                    }
                    else
                    {
                        //Everything worked fine
                        // trace the hold
                        if (Trace.isTraceOn())                                                  //@A1M
                            Trace.log (Trace.INFORMATION, "Stopped printer [" + printer_.getName () + "].");//@A1M

                        fireObjectChanged ();                                                   //@A1M
                    }

                    // Show the messages (returned whether or not there was an error)
                    if (Trace.isTraceOn())
                    {
                        AS400Message[] messagelist = cmd.getMessageList();
                        for (int i=0; i < messagelist.length; i++)
                        {
                            // show each message
                            Trace.log (Trace.INFORMATION, messagelist[i].getText());
                        }
                    }
                }
                catch (Exception e)
                {
                    if (Trace.isTraceOn())
                        Trace.log (Trace.ERROR, "ERROR CommandCall exception for [" + printer_.getName () + "].");

                    fireError(e);                                   //@A1A
                }
            }

            // fire stopped working event
            fireStopWorking();
        } // end try block
        catch (Exception e)
        {
            // trace the error
            if (Trace.isTraceOn())
                Trace.log (Trace.ERROR, "ERROR Stopping printer [" + printer_.getName () + "].");

            fireError (e);
        }
    }

} // end PrinterStopAction class

