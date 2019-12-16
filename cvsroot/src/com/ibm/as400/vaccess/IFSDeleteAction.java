///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSDeleteAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.Trace;
import java.io.IOException;



/**
The IFSDeleteAction class represents the action of deleting a
file or directory.
**/
class IFSDeleteAction
extends ConfirmedAction
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String confirmTitleText_           = ResourceLoader.getText ("DLG_CONFIRM_DELETION_TITLE");
    private static final String confirmMessageText_         = ResourceLoader.getText ("DLG_CONFIRM_DELETION");
    private static final String text_                       = ResourceLoader.getText ("ACTION_DELETE");



    // Private data.
    private IFSFile                         file_       = null;



/**
Constructs an IFSDeleteAction object.

@param  object      The object representing the file or directory.
@param  file        The file or directory.
@param  parent      The parent directory.
**/
    public IFSDeleteAction (VObject object,
                            IFSFile file)
    {
        super (object, confirmTitleText_, confirmMessageText_);
        file_ = file;
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
Performs the action after the user has confirmed it.

@param  context The action context.
**/
    public void perform2 (VActionContext context)
    {
        if (Trace.isTraceOn())
            Trace.log (Trace.INFORMATION, "Deleting file or directory ["
                + file_.getPath () + "].");

        // It is VERY important to fire the "stop working" event
        // before firing the "object deleted" event.  Otherwise,
        // the working cursor adapters will never detect the
        // "stop working" event because all of the working listener
        // wiring gets disconnected by the processing of the
        // "object deleted" event.
        fireStartWorking ();
        boolean objectDeleted = false;

        try {

            // If for some reason the file has already been deleted,
            // then get rid of it on the pane, but still fire an
            // error event.
            if (file_.exists () == false) {
                fireError (new IOException (ResourceLoader.getText ("EXC_FILE_NOT_FOUND")));
                objectDeleted = true;
            }

            // Try to delete the file.
            else if (file_.delete () == true)
                objectDeleted = true;
            else {
                // Unfortunately, we do not get any information as
                // to why the file or directory could not be deleted,
                // so we fire a generic error event.
                fireError (new IOException (ResourceLoader.getText ("EXC_FILE_NOT_DELETED")));
            }
        }
        catch (IOException e) {
            fireError (e);
        }

        fireStopWorking ();
        if (objectDeleted)
            fireObjectDeleted ();
    }



}
