///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: QueuedMessageReplyAction.java
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
import com.ibm.as400.access.Trace;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;



/**
The QueuedMessageReplyAction class defines the action
of replying to a queued message.
**/
class QueuedMessageReplyAction
extends DialogAction
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String     replyPrompt_    = ResourceLoader.getText ("MESSAGE_REPLY") + ": ";
    private static final String     text_           = ResourceLoader.getText ("ACTION_REPLY");



    // Private data.
    private QueuedMessage           message_    = null;
    private MessageQueue            queue_      = null;
    private JTextField              replyText_  = new JTextField (" ", 40);



/**
Constructs a QueuedMessageReplyAction object.

@param  object  The object representing the message.
@param  message The queued message.
@param  queue   The message queue.
**/
    public QueuedMessageReplyAction (VObject object,
                                     QueuedMessage message,
                                     MessageQueue queue)
    {
        super (object);

        message_    = message;
        queue_      = queue;
    }



/**
Returns the text for the action.

@return The text.
**/
    public String getText ()
    {
        return text_;
    }



/**
Indicates if the action is enabled.

@return true if the action is enabled, false otherwise.
**/
    public boolean isEnabled ()
    {
        // This action can only be enabled if when the
        // message is replyable (5 is inquiry).
        return ((super.isEnabled ()) && (message_.getType () == 5));
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

        VUtilities.constrain (new JLabel (replyPrompt_), panel, layout,
            0, 0, 1, 1);
        String defaultReply = message_.getDefaultReply();
        if (defaultReply != null)
            replyText_.setText (defaultReply);
        VUtilities.constrain (replyText_, panel, layout, 1, 0, 1, 1);

        return panel;
    }




/**
Performs the real action.
**/
    public void perform2 ()
    {
        if (Trace.isTraceOn())
            Trace.log (Trace.INFORMATION, "Replying to queued message ["
                + message_.getID () + "] in message queue ["
                + queue_ + "].");

        fireStartWorking ();

        try {
            queue_.reply (message_.getKey (), replyText_.getText());
        }
        catch (Exception e) {
            fireError (e);
        }
        finally {
            fireStopWorking(); // @D1A
            fireObjectDeleted ();
        }

        // @D1D fireStopWorking ();
    }



/**
Sets the enabled state of the action.

@param enabled true if the action is enabled, false otherwise.
**/
    public void setEnabled (boolean enabled)
    {
        super.setEnabled (enabled);
    }



}
