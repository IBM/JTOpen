///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: OutputReplyAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.SpooledFile;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.PrintObject;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DateFormat;
import java.util.Date;


/**
The OutputReplyAction class represents the action of replying to a spooled file
that has a message waiting status.
**/
class OutputReplyAction
extends DialogAction
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Private data.
    private static final String displayName_            = ResourceLoader.getText("ACTION_REPLY");
    private static final String replyText_              = ResourceLoader.getPrintText("REPLY") + ":";
    private static final String messageIDText_          = ResourceLoader.getPrintText("MESSAGE_ID") + ":";
    private static final String dateSentText_           = ResourceLoader.getPrintText("DATE_SENT") + ":";
    private static final String messageText_            = ResourceLoader.getPrintText("MESSAGE") + ":";
    private static final String messageHelpText_        = ResourceLoader.getPrintText("MESSAGE_HELP") + ":";

    private static  DateFormat  dateFormat_             = DateFormat.getDateTimeInstance ();

    private SpooledFile splF_                           = null; // spooled file
    private AS400Message msg_                           = null; // message to reply to
    private JTextField replyField_                      = null; // reply


/**
Constructs an OutputReplyAction object.

@param  object      The object.
@param  splF        The spooled file.
**/
    public OutputReplyAction (VObject object, SpooledFile splF)
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
            // retrieve the message that is waiting and it's various replies
            msg_ = splF_.getMessage();

            // message ID
            VUtilities.constrain( new JLabel(messageIDText_), panel, layout, 0,0,1,1);
            VUtilities.constrain( new JLabel(msg_.getID().trim()), panel, layout, 1,0,1,1);

            // message date
            VUtilities.constrain( new JLabel(dateSentText_), panel, layout, 0,2,1,1);
            Date newDate = msg_.getDate().getTime();
            VUtilities.constrain( new JLabel (dateFormat_.format(newDate)), panel, layout, 1,2,1,1);

            VUtilities.constrain (new JLabel (" "), panel, layout, 0, 3, 2, 1);

            // message
            VUtilities.constrain( new JLabel(messageText_), panel, layout, 0,4,1,1);
            VUtilities.constrain( new JLabel(msg_.getText().trim()), panel, layout, 1,4,1,1);

            VUtilities.constrain (new JLabel (" "), panel, layout, 0, 5, 2, 1);

            // message help
            String msgHelp = msg_.getHelp().trim();
            if (msgHelp != null)
                {
                VUtilities.constrain( new JLabel(messageHelpText_), panel, layout, 0,6,1,1);
//@B0D                JTextArea helpText = new JTextArea(VUtilities.formatHelp(msgHelp,80));
                JTextArea helpText = new JTextArea(msgHelp); //@B0A
                helpText.setEditable(false);
                helpText.setColumns(80);          //@B1C
                helpText.setLineWrap(true);       //@B0A
                helpText.setWrapStyleWord(true);  //@B0A
                VUtilities.constrain (helpText, panel, layout, 1,6,1,1);
                }

            // reply
            VUtilities.constrain( new JLabel(replyText_), panel, layout, 0,7,1,1);
            replyField_ = new JTextField(msg_.getDefaultReply().trim());
            replyField_.setEditable(true);
            VUtilities.constrain( replyField_, panel, layout, 1,7,1,1);
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
            // retrieve the reply
            String reply = (String)replyField_.getText().trim();

            // fire started working event
            fireStartWorking();

            // reply to the spooled file
            splF_.answerMessage(reply);

            // fire stopped working event
            fireStopWorking();

            // trace the reply
            if (Trace.isTraceOn())
                Trace.log (Trace.INFORMATION, "Replied to file ["
                           + splF_.getName () + "].");

            fireObjectChanged ();
            } // end try block
        catch (Exception e)
            {
            // trace the error
            if (Trace.isTraceOn())
                Trace.log (Trace.ERROR, "ERROR repling to file [" + splF_.getName () + "].");

            fireError (e);
            }
    }

} // end OutputReplyAction class

