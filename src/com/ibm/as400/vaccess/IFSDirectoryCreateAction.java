///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSDirectoryCreateAction.java
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
import com.ibm.as400.access.Trace;
import javax.swing.CellEditor;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import java.io.IOException;



/**
The IFSDirectoryCreateAction class represents the action of
creating a new directory.  It creates the directory with a
default name, and then prompts the user to rename it.
**/
class IFSDirectoryCreateAction
implements VAction, CellEditorListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String newName_    = ResourceLoader.getText ("IFS_NEW_DIRECTORY");
    private static final String text_       = ResourceLoader.getText ("ACTION_DIRECTORY_CREATE");



    // Private data.
    private IFSFile         directory_      = null;
    private boolean         enabled_        = true;
    private IFSFile         newDirectory_   = null;
    private VIFSDirectory   newObject_      = null;
    private VIFSDirectory   object_         = null;



    // Event support.
    private   ErrorEventSupport           errorEventSupport_    = new ErrorEventSupport (this);
    private   VObjectEventSupport         objectEventSupport_   = new VObjectEventSupport (this);
    private   WorkingEventSupport         workingEventSupport_  = new WorkingEventSupport (this);



/**
Constructs an IFSDirectoryCreateAction object.

@param  object      The object.
@param  directory   The directory.
**/
    public IFSDirectoryCreateAction (VIFSDirectory object, IFSFile directory)
    {
        directory_ = directory;
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

        // Rename the directory.
        String newName = editor.getCellEditorValue ().toString ();
        try {
            if (Trace.isTraceOn())
                Trace.log (Trace.INFORMATION, "Renaming new directory ["
                    + newDirectory_.getName () + "] to [" + newName + "].");

            newDirectory_.renameTo (new IFSFile (newDirectory_.getSystem (),
                newDirectory_.getParent (), newName));
            objectEventSupport_.fireObjectChanged (newObject_);
        }
        catch (Exception e) {
            errorEventSupport_.fireError (e);
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
        try {
            // Determine the default name for the new directory.  Make
            // sure that a directory with the name does not already exist.
            int count = 1;
            boolean success = false;
            AS400 system = directory_.getSystem ();
            newDirectory_ = new IFSFile (system, directory_, newName_);
            while (newDirectory_.exists ()) {
                ++count;
                newDirectory_ = new IFSFile (system, directory_,
                    newName_ + " (" + count + ")");
            }

            // Create the directory.
            if (Trace.isTraceOn())
                Trace.log (Trace.INFORMATION, "Creating new directory ["
                    + newDirectory_.getName () + "].");

            newDirectory_.mkdir ();
            newObject_ = new VIFSDirectory (object_, newDirectory_);

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
