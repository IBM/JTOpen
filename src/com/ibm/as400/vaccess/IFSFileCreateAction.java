///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileCreateAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.IFSFileOutputStream;
import com.ibm.as400.access.Trace;
import javax.swing.CellEditor;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import java.io.IOException;




/**
Represents the action of creating a new file.
**/
class IFSFileCreateAction
implements VAction, CellEditorListener
{
    // MRI.
    private static final String newName_    = ResourceLoader.getText ("IFS_NEW_FILE");
    private static final String text_       = ResourceLoader.getText ("ACTION_FILE_CREATE");



    // Private data.
    private boolean         enabled_        = true;
    private IFSFile         file_           = null;
    private IFSFile         newFile_        = null;
    private VIFSFile        newObject_      = null;
    private VIFSDirectory   object_         = null;



    // Event support.
    private   ErrorEventSupport           errorEventSupport_    = new ErrorEventSupport (this);
    private   VObjectEventSupport         objectEventSupport_   = new VObjectEventSupport (this);
    private   WorkingEventSupport         workingEventSupport_  = new WorkingEventSupport (this);



/**
Constructs an IFSFileCreateAction object.

@param  object      The object.
@param  file        The file.
**/
    public IFSFileCreateAction (VIFSDirectory object, IFSFile file)
    {
        file_ = file;
        object_ = object;
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
        // Go with the default name, no need to
        // rename.
    }



/**
Processes an editing stopped event.

@param  event   The event.
**/
    public void editingStopped (ChangeEvent event)
    {
        CellEditor editor = (CellEditor) event.getSource ();
        editor.removeCellEditorListener (this);

        // Rename the file.
        String newName = editor.getCellEditorValue ().toString ();
        try {
            if (Trace.isTraceOn())
                Trace.log (Trace.INFORMATION, "Renaming new file ["
                    + newFile_.getName () + "] to [" + newName + "].");

            newFile_.renameTo (new IFSFile (newFile_.getSystem (),
                newFile_.getParent (), newName));
            objectEventSupport_.fireObjectChanged (newObject_);
        }
        catch (Exception e) {
            errorEventSupport_.fireError (e);
        }
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

@param context The action context.
**/
    public void perform (VActionContext context)
    {
        IFSFileOutputStream outputStream = null;
        try {
            // Determine the default name for the new file.  Make
            // sure that a file with the name does not already
            // exist.
            int count = 1;
            boolean success = false;
            AS400 system = file_.getSystem ();
            newFile_ = new IFSFile (system, file_, newName_);
            while (newFile_.exists ()) {
                ++count;
                newFile_ = new IFSFile (system, file_, newName_ + " (" + count + ")");
            }

            // Create the file.
            if (Trace.isTraceOn())
                Trace.log (Trace.INFORMATION, "Creating file ["
                    + newFile_.getName () + "].");

            outputStream = new IFSFileOutputStream (system,
                newFile_, IFSFileOutputStream.SHARE_ALL, false); // creates file
            newObject_ = new VIFSFile (newFile_);

            // Make sure new directory is incorporated into object.
            // We can do this without a reload.
            objectEventSupport_.fireObjectCreated (newObject_, object_);

            // Automatically put the user into a rename.
            CellEditor editor = context.startEditing (newObject_, VIFSDirectory.NAME_PROPERTY);
            if (editor != null)
                editor.addCellEditorListener (this);
        }
        catch (Exception e) {
            errorEventSupport_.fireError (e);
        }
        finally {
          if (outputStream != null) {
            try { outputStream.close(); }
            catch (Throwable e) { Trace.log(Trace.ERROR, e); }
          }
        }
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
        return getText ();
    }



}
