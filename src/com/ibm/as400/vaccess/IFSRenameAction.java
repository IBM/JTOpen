///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSRenameAction.java
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
import javax.swing.CellEditor;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import java.io.IOException;



/**
The IFSRenameAction class represents the action of renaming a
file or directory.
**/
class IFSRenameAction
implements VAction, CellEditorListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String             text_    = ResourceLoader.getText ("ACTION_RENAME");



    // Private data.
    private boolean                         enabled_        = true;
    private IFSFile                         file_           = null;
    private VObject                         object_         = null;



    // Event support.
    private ErrorEventSupport           errorEventSupport_    = new ErrorEventSupport (this);
    private VObjectEventSupport         objectEventSupport_   = new VObjectEventSupport (this);
    private WorkingEventSupport         workingEventSupport_  = new WorkingEventSupport (this);



/**
Constructs an IFSRenameAction object.

@param  object      The object.
@param  file        The file or directory.
**/
    public IFSRenameAction (VObject object, IFSFile file)
    {
        object_ = object;
        file_ = file;
    }



/**
Adds an error listener.

@param  listener    The listener.
**/
    public void addErrorListener (ErrorListener listener)
    {
        errorEventSupport_.addErrorListener (listener);
    }



/**
Adds a VObjectListener.

@param  listener    The listener.
**/
    public void addVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener (listener);
    }



/**
Adds a working listener.

@param  listener    The listener.
**/
    public void addWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener (listener);
    }



/**
Processes an editing canceled event.

@param  event   The event.
**/
    public void editingCanceled (ChangeEvent event)
    {
        // No action needed.
    }



/**
Processes an editing stopped event.

@param  event   The event.
**/
    public void editingStopped (ChangeEvent event)
    {
        CellEditor editor = (CellEditor) event.getSource ();
        editor.removeCellEditorListener (this);

        // Rename the directory or file.
        String newName = editor.getCellEditorValue ().toString ();

        if (Trace.isTraceOn())
            Trace.log (Trace.INFORMATION, "Renaming file or directory ["
                + file_.getName () + "] to [" + newName + "].");

        workingEventSupport_.fireStartWorking ();
        boolean firedStopWorking = false;

        try {

            // If for some reason the file does not exist,
            // then get rid of it on the pane, but still fire an
            // error event.
            if (file_.exists () == false) {
                errorEventSupport_.fireError (new IOException (ResourceLoader.getText ("EXC_FILE_NOT_FOUND")));
                // Firing the objectDeleted event causes listeners to stop listening;
                // so fire the stop working event now, while they're still listening.
                workingEventSupport_.fireStopWorking ();
                firedStopWorking = true;

                objectEventSupport_.fireObjectDeleted (object_);
            }

            // Try to rename the file.
            else if (file_.renameTo (new IFSFile (file_.getSystem (),
                file_.getParent (), newName)) == true) {
                objectEventSupport_.fireObjectChanged (object_);
            }
            else {
                // Unfortunately, we do not get any information as
                // to why the file or directory could not be deleted,
                // so we fire a generic error event.
                errorEventSupport_.fireError (new IOException (ResourceLoader.getText ("EXC_FILE_NOT_RENAMED")));
            }
        }
        catch (Exception e) {
            errorEventSupport_.fireError (e);
        }

        if (!firedStopWorking) {
          workingEventSupport_.fireStopWorking ();
        }
    }



/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
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
        return enabled_;
    }



/**
Performs the action.

@param  context The action context.
**/
    public void perform (VActionContext context)
    {
        CellEditor editor = context.startEditing (object_, VObject.NAME_PROPERTY);
        if (editor != null)
            editor.addCellEditorListener (this);
    }



/**
Removes an error listener.

@param  listener    The listener.
**/
    public void removeErrorListener (ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener (listener);
    }



/**
Removes a VObjectListener.

@param  listener    The listener.
**/
    public void removeVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.removeVObjectListener (listener);
    }



/**
Removes a working listener.

@param  listener    The listener.
**/
    public void removeWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener (listener);
    }



/**
Sets the enabled state of the action.

@param enabled true if the action is enabled, false otherwise.
**/
    public void setEnabled (boolean enabled)
    {
        enabled_ = enabled;
    }



/**
Returns the text for the action.

@return The text.
**/
    public String toString ()
    {
        return text_;
    }



}
