///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: MessageQueueClearAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.MessageQueue;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;



/**
The MessageQueueClearAction class defines the action of
clearing a message queue.
**/
class MessageQueueClearAction
extends ConfirmedAction
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String confirmTitleText_   = ResourceLoader.getText ("DLG_CONFIRM_CLEAR_TITLE");
    private static final String confirmMessageText_ = ResourceLoader.getText ("DLG_CONFIRM_CLEAR");
    private static final String text_               = ResourceLoader.getText ("ACTION_CLEAR");



    // Private data.
    private JComboBox       messageType_;
    private VMessageQueue   object_;
    private MessageQueue    queue_;



/**
Constructs a MessageQueueClearAction object.

@param  object  The object.
@param  queue   The message queue.
**/
    public MessageQueueClearAction (VMessageQueue object, MessageQueue queue)
    {
        super (object, confirmTitleText_, confirmMessageText_);
        object_ = object;
        queue_  = queue;
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
Performs the action.
**/
    public void perform2 (VActionContext context)
    {
        fireStartWorking ();

        try {
            int count = object_.getDetailsChildCount ();
            VObject[] detailsChildren = new VObject[count];
            for (int i = 0; i < count; ++i)
                detailsChildren[i] = object_.getDetailsChildAt (i);

            queue_.remove ();

            for (int i = 0; i < count; ++i)
                fireObjectDeleted (detailsChildren[i]);
        }
        catch (Exception e) {
            fireError (e);
        }

        fireStopWorking ();
    }



}
