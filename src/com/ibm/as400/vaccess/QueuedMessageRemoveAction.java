///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: QueuedMessageRemoveAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.MessageQueue;
import com.ibm.as400.access.QueuedMessage;
import com.ibm.as400.access.Trace;


/**
The QueuedMessageRemoveAction class defines the action
of removing a queued message.
**/
class QueuedMessageRemoveAction
extends ConfirmedAction
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String confirmTitleText_           = ResourceLoader.getText ("DLG_CONFIRM_REMOVE_TITLE");
    private static final String confirmMessageText_         = ResourceLoader.getText ("DLG_CONFIRM_REMOVE");
    private static final String text_                       = ResourceLoader.getText ("ACTION_REMOVE");



    // Private data.
    private QueuedMessage           message_    = null;
    private MessageQueue            queue_      = null;




/**
Constructs a QueuedMessageRemoveAction object.

@param  object  The object representing the message.
@param  message The queued message.
@param  queue   The message queue.
**/
    public QueuedMessageRemoveAction (VObject object,
                                QueuedMessage message,
                                MessageQueue queue)
    {
        super (object, confirmTitleText_, confirmMessageText_);

        message_    = message;
        queue_      = queue;
    }



/**
Returns the display name for the action.

@return The display name.
**/
    public String getText ()
    {
        return text_;
    }



/**
Performs the action after the user has confirmed it.

@param  context The action context.
**/
    public void perform2 (VActionContext context)
    {
        if (Trace.isTraceOn())
            Trace.log (Trace.INFORMATION, "Removing queued message ["
                + message_.getID () + "] from message queue ["
                + queue_ + "].");

        fireStartWorking ();
        boolean deleted = false;                                            // @D1A

        try {
            queue_.remove (message_.getKey());
            deleted = true;                                                 // @D1C
        }
        catch (AS400Exception e)
        {
            fireError (e);

            // If the error is that the message is already removed,
            // then remove it from the pane.
            if (e.getAS400Message ().getID ().equals ("CPF2410"))
                deleted = true;                                             // @D1C
        }
        catch (Exception e)
        {
            fireError (e);
        }

        fireStopWorking ();

        // It is important to fire the object deleted event after              @D1A
        // the stop working.  The parent stops listening for events            @D1A
        // when it knows the object is deleted.                                @D1A
        if (deleted)                                                        // @D1A
            fireObjectDeleted();                                            // @D1A
    }



}



