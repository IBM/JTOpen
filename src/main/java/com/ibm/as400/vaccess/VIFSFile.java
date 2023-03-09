///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VIFSFile.java
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
import com.ibm.as400.access.Permission;
import com.ibm.as400.access.Trace;
import javax.swing.Icon;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;



/**
The VIFSFile class defines the representation of a file
in the integrated file system for use in
various models and panes in this package.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>VIFSFile objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>PropertyChangeEvent
    <li>VObjectEvent
    <li>WorkingEvent
</ul>

@see com.ibm.as400.access.IFSFile
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
//
// Implementation note:
//
// * I made a conscious decision not to make this fire
//   file events.  The reason is that I just don't think
//   it will be used that way.
//
public class VIFSFile
implements VObject, VIFSConstants, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String description_            = ResourceLoader.getText ("IFS_FILE_DESCRIPTION");
    private static final Icon   icon16_                 = ResourceLoader.getIcon ("VIFSFile16.gif", description_);
    private static final Icon   icon32_                 = ResourceLoader.getIcon ("VIFSFile32.gif", description_);
    private static final String readAbbreviationText_   = ResourceLoader.getText ("IFS_READ_ABBREVIATION");
    private static final String writeAbbreviationText_  = ResourceLoader.getText ("IFS_WRITE_ABBREVIATION");



    // Properties.
             IFSFile                             file_               = null;    // @C2C



    // Private data.
    transient private VAction[]                 actions_;
    transient private boolean                   actionsInitialized_; //@B4A
    transient private String                    attributes_;
    transient private VAction                   defaultAction_;
    transient private Date                      modified_;
    transient private VPropertiesPane           propertiesPane_;
    transient private boolean                   readable_;
    transient private Long                      size_;
    transient private boolean                   writable_;



    // Event support.
    transient private ErrorEventSupport         errorEventSupport_;
    transient private VObjectEventSupport       objectEventSupport_;
    transient private PropertyChangeSupport     propertyChangeSupport_;
    transient private VetoableChangeSupport     vetoableChangeSupport_;
    transient private WorkingEventSupport       workingEventSupport_;



/**
Constructs a VIFSFile object. The system and path properties
will need to be set before using any method requiring a
connection to the system.
**/
    public VIFSFile ()
    {
        file_ = new IFSFile ();
        initializeTransient ();
    }



/**
Constructs a VIFSFile object.

@param  file    The file.
**/
    public VIFSFile (IFSFile file)
    {
        if (file == null)
            throw new NullPointerException ("file");

        file_ = file;
        initializeTransient ();
    }



/**
Constructs a VIFSFile object.

@param  system      The system on which the file resides.
@param  path        The fully qualified path name of the file. 
**/
    public VIFSFile (AS400 system, String path)
    {
        if (system == null)
            throw new NullPointerException ("system");
        if (path == null)
            throw new NullPointerException ("path");

        file_ = new IFSFile (system, path);
        initializeTransient ();
    }



/**
Adds a listener to be notified when an error occurs.

@param  listener    The listener.
**/
    public void addErrorListener (ErrorListener listener)
    {
        errorEventSupport_.addErrorListener (listener);
    }



/**
Adds a listener to be notified when the value of any
bound property changes.

@param  listener  The listener.
**/
    public void addPropertyChangeListener (PropertyChangeListener listener)
    {
        propertyChangeSupport_.addPropertyChangeListener (listener);
    }



/**
Adds a listener to be notified when the value of any
constrained property changes.

@param  listener  The listener.
**/
    public void addVetoableChangeListener (VetoableChangeListener listener)
    {
        vetoableChangeSupport_.addVetoableChangeListener (listener);
    }



/**
Adds a listener to be notified when a VObject is changed,
created, or deleted.

@param  listener    The listener.
**/
    public void addVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener (listener);
    }



/**
Adds a listener to be notified when work starts and stops
on potentially long-running operations.

@param  listener    The listener.
**/
    public void addWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener (listener);
    }



/**
Indicates if the file is readable.

@return true if the file is readable, false otherwise.
**/
    boolean canRead ()
    {
        return readable_;
    }



/**
Indicates if the file is writable.

@return true if the file is writable; false otherwise.
**/
    boolean canWrite ()
    {
        return writable_;
    }



/**
Returns the list of actions that can be performed.
<ul>
    <li>edit
    <li>view
    <li>rename
    <li>delete
</ul>

@return The actions that can be performed.
**/
    public VAction[] getActions ()
    {
        initializeCreationActions ();  //@B4A
        return actions_;
    }



/**
Returns the default action.  The default action is to edit
the file.

@return The default action.
**/
    public VAction getDefaultAction ()
    {
        return defaultAction_;
    }



/**
Returns the icon.

@param  size    The icon size, either 16 or 32.  If any other
                value is given, then return a default.
@param  open    This parameter has no effect.
@return         The icon.
**/
    public Icon getIcon (int size, boolean open)
    {
        if (size == 32)
            return icon32_;
        else
            return icon16_;
    }



/**
Returns the last modified date.

@return The last modified date.
**/
    Date getModified ()
    {
        return modified_;
    }



/**
Returns the file name.

@return The file name.

@see com.ibm.as400.access.IFSFile#getName
**/
    public String getName ()
    {
        return file_.getName ();
    }



/**
Returns the parent directory name.

@return The parent directory name.

@see com.ibm.as400.access.IFSFile#getParent
**/
    public String getParentDirectory ()
    {
        return file_.getParent ();
    }



/**
Returns the fully qualified path name of the file.

@return The fully qualified path name of the file. 

@see com.ibm.as400.access.IFSFile#getPath
**/
    public String getPath ()
    {
        return file_.getPath ();
    }



/**
Returns the properties pane.

@return The properties pane.
**/
    public VPropertiesPane getPropertiesPane ()
    {
        return propertiesPane_;
    }



/**
Returns a property value.

@param      propertyIdentifier  The property identifier.  The choices are
                                <ul>
                                  <li>NAME_PROPERTY
                                  <li>DESCRIPTION_PROPERTY
                                  <li>SIZE_PROPERTY
                                  <li>MODIFIED_PROPERTY
                                  <li>ATTRIBUTES_PROPERTY
                                </ul>
@return                         The property value, or null if the
                                property identifier is not recognized.
**/
    public synchronized Object getPropertyValue (Object propertyIdentifier)
    {
        // Get the file name.
        if (propertyIdentifier == NAME_PROPERTY)
            return this;

        // Get the description.
        else if (propertyIdentifier == DESCRIPTION_PROPERTY)
            return description_;

        // Get the file size.
        else if (propertyIdentifier == SIZE_PROPERTY)
            return size_;

        // Get the file modified timestamp.
        else if (propertyIdentifier == MODIFIED_PROPERTY)
            return modified_;

        // Get the file attributes.
        else if (propertyIdentifier == ATTRIBUTES_PROPERTY)
            return attributes_;

        // By default, return null.
        return null;
    }



/**
Returns the size.

@return The size.
**/
    long getSize ()
    {
        return size_.longValue ();
    }



/**
Returns the system on which the file resides.

@return The system on which the file resides.

@see com.ibm.as400.access.IFSFile#getSystem
**/
    public AS400 getSystem ()
    {
        return file_.getSystem ();
    }



/**
Returns the text.  This is the name of the file.

@return The text which is the name of the file.
**/
    public String getText ()
    {
        return file_.getName ();
    }


//@B4A
/**
If we are dealing with an object that is under QDLS or QSYS,
disables the "creation" actions.
**/
    private void initializeCreationActions ()
    {
      if (actionsInitialized_) return;  //@B4A
      // @B4C - This block was formerly located in initializeTransient().
      // @B3A
      // If we are operating on an object that is in QDLS or QSYS,
      // we want to disable the creation and editing
      // actions, since there are problems with creating folders and
      // things in areas of the file system that aren't normal IFS.
      // Note: The Rename action works, assuming the user knows the
      //       proper naming convention for the object they are changing.
      //       The Delete action works since the object was pre-existing
      //       and there wouldn't be naming problems.
      //       The Edit and View actions are useless on almost all objects
      //       so we disable them.

//    Permission perm = ((PermissionAction)actions_[4]).getPermission (); //@B4C @B5D
//    if (perm != null && perm.getType() != Permission.TYPE_ROOT)  @B5D

      String pathPrefix = file_.getPath().toUpperCase(); //@B5A
      if (pathPrefix.startsWith("/QSYS.LIB/") || //@B5A
          pathPrefix.startsWith("/QDLS/"))       //@B5A
      {
        for (int i=0; i<2; ++i)
          actions_[i].setEnabled(false);
        defaultAction_ = actions_[4]; // Change default action to Permission.
      }
      actionsInitialized_ = true;  //@B4A
    }



/**
Initializes the transient data.
**/
    private void initializeTransient ()
    {
        // Initialize the event support.
        errorEventSupport_      = new ErrorEventSupport (this);
        objectEventSupport_     = new VObjectEventSupport (this);
        propertyChangeSupport_  = new PropertyChangeSupport (this);
        vetoableChangeSupport_  = new VetoableChangeSupport (this);
        workingEventSupport_    = new WorkingEventSupport (this);

        file_.addPropertyChangeListener (propertyChangeSupport_);
        file_.addVetoableChangeListener (vetoableChangeSupport_);
        addVObjectListener(new VObjectListener_());            // @C2A 

        // Initialize the actions.
        actions_    = new VAction[5]; //@A1C Changed from [4] to [5]
        actions_[0] = new IFSEditAction (this, file_, true); // Edit
        actions_[1] = new IFSEditAction (this, file_, false); // View.
        actions_[2] = new IFSRenameAction (this, file_);
        actions_[3] = new IFSDeleteAction (this, file_);
        actions_[4] = new PermissionAction(file_);  // @A1A Add PermissionAction 
        defaultAction_ = actions_[0];

        //@B4C
        // Postpone disabling the creation actions until we need to use the actions.
        actionsInitialized_ = false;  //@B4C

        for (int i = 0; i < actions_.length; ++i) {
            actions_[i].addErrorListener (errorEventSupport_);
            actions_[i].addVObjectListener (objectEventSupport_);
            actions_[i].addWorkingListener (workingEventSupport_);
        }

        // Initialize the properties pane.
        propertiesPane_ = new IFSFilePropertiesPane (this);

        propertiesPane_.addErrorListener (errorEventSupport_);
        propertiesPane_.addVObjectListener (objectEventSupport_);
        propertiesPane_.addWorkingListener (workingEventSupport_);

        // Initialize the details values.
        attributes_             = "";

        // $B2 - Use the system current date for file creation
        modified_            = new Date ();
       
        readable_               = false;
        size_                   = new Long (0);
        writable_               = false;
    }



/**
Loads information about the object from the system.
**/
    public void load ()
    {
        workingEventSupport_.fireStartWorking ();
        initializeCreationActions ();  //@B4A

        // Load the size.
        try {
            size_ = new Long (file_.length());
        }
        catch (Exception e) {
            errorEventSupport_.fireError (e);
            size_ = new Long (0);
        }

        // Load the modified date.
        try {
            modified_ = new Date (file_.lastModified());
        }
        catch (Exception e) {
            modified_ = new Date (0);
            errorEventSupport_.fireError (e);
        }

        // Load the attributes.
        try {
            readable_ = file_.canRead ();
        }
        catch (Exception e) {
            readable_ = false;
            errorEventSupport_.fireError (e);
        }

        try {
            writable_ = file_.canWrite ();
        }
        catch (Exception e) {
            writable_ = false;
            errorEventSupport_.fireError (e);
        }

        StringBuffer buffer = new StringBuffer ();
        if (readable_)
            buffer.append (readAbbreviationText_);
        if (writable_)
            buffer.append (writeAbbreviationText_);
        attributes_ = buffer.toString ();

        // Done loading.
        workingEventSupport_.fireStopWorking ();
    }



/**
Restores the state of the object from an input stream.
This is used when deserializing an object.

@param in   The input stream.
**/
    private void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject ();
        initializeTransient ();
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
Removes a property change listener.

@param  listener  The listener.
**/
    public void removePropertyChangeListener (PropertyChangeListener listener)
    {
        propertyChangeSupport_.removePropertyChangeListener (listener);
    }



/**
Removes a vetoable change listener.

@param  listener  The listener.
**/
    public void removeVetoableChangeListener (VetoableChangeListener listener)
    {
        vetoableChangeSupport_.removeVetoableChangeListener (listener);
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
Sets the fully qualified path name of the file. 

@param path The fully qualified path name of the file. 

@exception PropertyVetoException If the change is vetoed.

@see com.ibm.as400.access.IFSFile#setPath
**/
    public void setPath (String path)
        throws PropertyVetoException
    {
        if (path == null)
            throw new NullPointerException ("path");

        file_.setPath (path);
    }



/**
Sets the system on which the file resides.

@param system The system on which the file resides.

@exception PropertyVetoException If the change is vetoed.

@see com.ibm.as400.access.IFSFile#setSystem
**/
    public void setSystem (AS400 system)
        throws PropertyVetoException
    {
        if (system == null)
            throw new NullPointerException ("system");

        file_.setSystem (system);
    }



/**
Returns the string representation of the name of the file.

@return The string representation of the name of the file.
**/
    public String toString ()
    {
        return file_.getName ();
    }




// @C2A
/**
Listens for events and clears the cached attributes when
the object changes.  This will ensure the the file size
and last modification date, etc., will be reflected.
**/
    private class VObjectListener_
    implements VObjectListener, Serializable
    {
        public void objectChanged (VObjectEvent event)
        {
            file_.clearCachedAttributes();
        }



        public void objectCreated (VObjectEvent event)
        {
        }



        public void objectDeleted (VObjectEvent event)
        {
        }


    }

}
