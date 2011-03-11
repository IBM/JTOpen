///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: OutputDeleteAction.java
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

/**
The OutputDeleteAction class represents the action of deleting a
spooled file.
**/
class OutputDeleteAction
extends ConfirmedAction
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // MRI.
    private static final String confirmTitleText_       = ResourceLoader.getText ("DLG_CONFIRM_DELETION_TITLE");
    private static final String confirmMessageText_     = ResourceLoader.getText ("DLG_CONFIRM_DELETION");
    private static final String text_                   = ResourceLoader.getText ("ACTION_DELETE");

    // Private data.
    private SpooledFile splF_                           = null; // the spooled file
    private VPrinterOutput parent_                      = null; // parent (the spooled list)

/**
Constructs an OutputDeleteAction object.

@param  object      The object.
@param  splF        The spooled file.
**/
    public OutputDeleteAction (VObject object, SpooledFile splF, VPrinterOutput parent)
    {
        super (object, confirmTitleText_, confirmMessageText_);
        splF_ = splF;
        parent_ = parent;
    }


/**
Returns the localized text for the action.

@return The text.
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
        try {
            // fire started working event
            fireStartWorking();

            // delete the spooled file
            splF_.delete ();
            
            // retrieve a reference to the VObject for the spooled file
            VObject deletedObject = getObject ();

            // fire stopped working event
            fireStopWorking();

            if (Trace.isTraceOn())
                Trace.log (Trace.INFORMATION, "Deleted file ["
                           + splF_.getName () + "].");

            // notify of the delete
            fireObjectDeleted ();

            // remove the object from the list
            parent_.remove (deletedObject);

            } // end try block
        catch (Exception e)
            {
            // trace the error
            if (Trace.isTraceOn())
                Trace.log (Trace.ERROR, "ERROR Deleting file [" + splF_.getName () + "].");

            fireError (e);
            }
    }

} // end OutputDeleteAction class

