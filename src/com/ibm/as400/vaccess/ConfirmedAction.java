///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ConfirmedAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.JOptionPane;
import java.awt.Component;



/**
The ConfirmedAction abstract class presents an optional confirmation
to the user before performing the action.  The confirmation will be
presented only if the action context requires such confirmations.
**/
abstract class ConfirmedAction
extends AbstractVAction
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private String  messageText_;
    private VObject object_;
    private String  titleText_;



/**
Constructs a ConfirmedAction object.

@param  object      The object to which the action is associated.
@param  titleText   The title text for the confirmation.
@param  messageText The message text for the confirmation.
**/
    public ConfirmedAction (VObject object,
                            String titleText,
                            String messageText)
    {
        super (object);
        object_      = object;
        messageText_ = messageText;
        titleText_   = titleText;
    }



/**
Performs the action.

@param  context   The action context.
**/
    public void perform (VActionContext context)
    {
        boolean confirmed = true;
        if (context.getConfirm ()) {

            // Create the full message text.
            StringBuffer buffer = new StringBuffer ();
            if (object_ != null) {
                buffer.append (object_.toString ());
                buffer.append ("\n\n");
            }
            buffer.append (messageText_);

            // Show the confirmation.
            int response = JOptionPane.showConfirmDialog (context.getFrame (),
                buffer.toString (), titleText_, JOptionPane.YES_NO_OPTION);
            confirmed = (response == JOptionPane.YES_OPTION);
        }

        if (confirmed)
            perform2 (context);
    }



/**
Performs the action after the user has confirmed it.

@param  context   The action context.
**/
    abstract void perform2 (VActionContext context);



}
