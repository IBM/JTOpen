///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VIFSDirectory.java
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
import com.ibm.as400.access.IFSFileFilter;
import com.ibm.as400.access.Permission;
import com.ibm.as400.access.Trace;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreeNode;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;



/**
The VIFSDirectory class defines the representation of a
directory in the integrated file system of an AS/400 for use
in various models and panes in this package.
You must explicitly call load() to load the information from
the AS/400.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>VIFSDirectory objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>PropertyChangeEvent
    <li>VObjectEvent
    <li>WorkingEvent
</ul>

@see com.ibm.as400.access.IFSFile
**/
public class VIFSDirectory
implements VNode, VIFSConstants, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




/**
Constant for including files only.
**/
    public static final int         INCLUDE_FILES               = 0;

/**
Constant for including directories only.
**/
    public static final int         INCLUDE_DIRECTORIES         = 1;

/**
Constant for including both files and directories.
**/
    public static final int         INCLUDE_BOTH                = 2;



    // MRI.
    private static String           attributesColumnHeader_ = ResourceLoader.getText ("COLUMN_ATTRIBUTES");
    private static String           description_            = ResourceLoader.getText ("IFS_DIRECTORY_DESCRIPTION");
    private static Icon             closedIcon16_           = ResourceLoader.getIcon ("VIFSDirectory16.gif", description_);
    private static Icon             closedIcon32_           = ResourceLoader.getIcon ("VIFSDirectory32.gif", description_);
    private static String           modifiedColumnHeader_   = ResourceLoader.getText ("COLUMN_MODIFIED");
    private static String           nameColumnHeader_       = ResourceLoader.getText ("COLUMN_NAME");
    private static Icon             openIcon16_             = ResourceLoader.getIcon ("VIFSDirectoryOpen16.gif", description_);
    private static Icon             openIcon32_             = ResourceLoader.getIcon ("VIFSDirectoryOpen32.gif", description_);
    private static String           sizeColumnHeader_       = ResourceLoader.getText ("COLUMN_SIZE");



    // Properties.
    private IFSDirectoryFilter      actualFilter_           = new IFSDirectoryFilter (INCLUDE_BOTH, null);
    private IFSFile                 directory_              = null;
    private VNode                   parent_                 = null;
    private String                  pattern_                = "*";



    // Static data.
    private static TableColumnModel detailsColumnModel_     = null;



    // Private data.
    transient private VAction[]         actions_;
    transient private boolean           actionsInitialized_; //@C2A
    transient         VNode[]           children_; // Private.
    transient private boolean           childrenLoaded_;
    transient         boolean           deleted_;         // Private. @A1A
    transient         VObject[]         detailsChildren_; // Private.
    transient private Date              modified_;
    transient private VPropertiesPane   propertiesPane_;



    // Event support.
    transient         ErrorEventSupport           errorEventSupport_; // Private.
    transient         VObjectEventSupport         objectEventSupport_; // Private.
    transient         VObjectListener_            objectListener_; // Private.
    transient         PropertyChangeSupport       propertyChangeSupport_; // Private.
    transient         VetoableChangeSupport       vetoableChangeSupport_; // Private.
    transient         WorkingEventSupport         workingEventSupport_; // Private.



/**
Static initializer.
**/
//
// Implementation note:
//
// * The column widths are completely arbitrary.
//
    static
    {
        detailsColumnModel_ = new DefaultTableColumnModel ();
        int columnIndex = 0;

        // Name column.
        VTableColumn nameColumn = new VTableColumn (columnIndex++, NAME_PROPERTY);
        nameColumn.setCellEditor (new VObjectCellEditor ());
        nameColumn.setCellRenderer (new VObjectCellRenderer ());
        nameColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        nameColumn.setHeaderValue (nameColumnHeader_);
        nameColumn.setPreferredCharWidth (25);
        detailsColumnModel_.addColumn (nameColumn);

        // Size column.
        VTableColumn sizeColumn = new VTableColumn (columnIndex++, SIZE_PROPERTY);
        sizeColumn.setCellRenderer (new VObjectCellRenderer (SwingConstants.RIGHT));
        sizeColumn.setHeaderRenderer (new VObjectHeaderRenderer (SwingConstants.RIGHT));
        sizeColumn.setHeaderValue (sizeColumnHeader_);
        sizeColumn.setPreferredCharWidth (8);
        detailsColumnModel_.addColumn (sizeColumn);

        // Modified column.
        VTableColumn modifiedColumn = new VTableColumn (columnIndex++, MODIFIED_PROPERTY);
        modifiedColumn.setCellRenderer (new VObjectCellRenderer ());
        modifiedColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        modifiedColumn.setHeaderValue (modifiedColumnHeader_);
        modifiedColumn.setPreferredCharWidth (20);
        detailsColumnModel_.addColumn (modifiedColumn);

        // Attributes column.
        VTableColumn attributesColumn = new VTableColumn (columnIndex++, ATTRIBUTES_PROPERTY);
        attributesColumn.setCellRenderer (new VObjectCellRenderer ());
        attributesColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        attributesColumn.setHeaderValue (attributesColumnHeader_);
        attributesColumn.setPreferredCharWidth (10);
        detailsColumnModel_.addColumn (attributesColumn);
    }



/**
Constructs a VIFSDirectory object.
**/
    public VIFSDirectory ()
    {
        directory_ = new IFSFile ();
        initializeTransient ();
    }



/**
Constructs a VIFSDirectory object.

@param  directory   The directory.
**/
    public VIFSDirectory (IFSFile directory)
    {
        if (directory == null)
            throw new NullPointerException ("directory");

        directory_ = directory;
        initializeTransient ();
    }



/**
Constructs a VIFSDirectory object.

@param  parent      The parent.
@param  directory   The directory.
**/
    public VIFSDirectory (VNode parent, IFSFile directory)
    {
        if (parent == null)
            throw new NullPointerException ("parent");
        if (directory == null)
            throw new NullPointerException ("directory");

        parent_     = parent;
        directory_  = directory;
        initializeTransient ();
    }



/**
Constructs a VIFSDirectory object.

@param  system      The AS/400 on which the file resides.
@param  path        The fully qualified path name of the file.
**/
    public VIFSDirectory (AS400 system, String path)
    {
        if (system == null)
            throw new NullPointerException ("system");
        if (path == null)
            throw new NullPointerException ("path");

        directory_ = new IFSFile (system, path);
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
Returns the children of the node.
The children are the subdirectories.

@return         The children.
**/
    public synchronized Enumeration children ()
    {
        return new VEnumeration (this);
    }



/**
Returns the list of actions that can be performed.
<ul>
    <li>create file
    <li>create directory
    <li>rename
    <li>delete
</ul>

@return The actions.
**/
    public VAction[] getActions ()
    {
        initializeCreationActions ();  //@C2A
        return actions_;
    }



/**
Indiciates if the node allows children.

@return  Always true.
**/
    public boolean getAllowsChildren ()
    {
        return true;
    }



/**
Returns the child node at the specified index.

@param  index   The index.
@return         The child node, or null if the
                index is not valid.
**/
    public /* @A2D synchronized */ TreeNode getChildAt (int index)
    {
        if (childrenLoaded_ == false)
            loadChildren ();

        if (index < 0 || index >= children_.length)
            return null;

        return children_[index];
    }



/**
Returns the number of children.
This is the number of subdirectories.

@return  The number of children.
**/
    public /* @A2D synchronized */ int getChildCount ()
    {
        if (childrenLoaded_ == false)
            loadChildren ();

        return children_.length;
    }

/**
Returns the default action.

@return Always null. There is no default action.
**/
    public VAction getDefaultAction ()
    {
        return null;
    }



/**
Returns the child for the details at the specified index.

@param  index   The index.
@return         The child, or null if the index is not
                valid.
**/
    public /* @A2D synchronized */ VObject getDetailsChildAt (int index)
    {
        if (childrenLoaded_ == false)
            loadChildren ();

        synchronized (detailsChildren_) {  // @C3A
          if (index < 0 || index >= detailsChildren_.length)
            return null;

          return detailsChildren_[index];
        }
    }



/**
Returns the number of children for the details.
This is the number of subdirectories and files.

@return  The number of children for the details.
**/
    public /* @A2D synchronized */ int getDetailsChildCount ()
    {
        if (childrenLoaded_ == false)
            loadChildren ();

        return detailsChildren_.length;
    }



/**
Returns the table column model to use in the details
when representing the children.  This column model
describes the details values for the children.

@return The details column model.
**/
    public TableColumnModel getDetailsColumnModel ()
    {
        return detailsColumnModel_;
    }



/**
Returns the index of the specified child for the details.

@param  detailsChild   The details child.
@return                The index, or -1 if the child is not found
                       in the details.
**/
    public /* @A2D synchronized */ int getDetailsIndex (VObject detailsChild)
    {
        if (childrenLoaded_ == false)
            loadChildren ();

        synchronized (detailsChildren_) {  // @C3A
          for (int i = 0; i < detailsChildren_.length; ++i)
            if (detailsChildren_[i] == detailsChild)
              return i;
        }
        return -1;
    }



/**
Returns the number of children that are directories.

@return The number of children that are directories.
**/
    int getDirectoryCount ()
    {
        return getChildCount ();
    }



/**
Returns the number of children that are files.

@return The number of children that are files.
**/
    int getFileCount ()
    {
        return getDetailsChildCount () - getChildCount ();
    }



/**
Returns the filter which determines which files and directories
are included as children.

@return  The filter which determines which files are included
         as children, or null to include all files and
         directories.
**/
    public IFSFileFilter getFilter ()
    {
        return actualFilter_.getOtherFilter ();
    }



/**
Returns the icon.

@param  size    The icon size, either 16 or 32.  If any other
                value is given, then return a default.
@param  open    true for the open icon; false for the closed
                icon.
@return         The icon.
**/
    public Icon getIcon (int size, boolean open)
    {
        if (size == 32) {
            if (open)
                return openIcon32_;
            else
                return closedIcon32_;
        }
        else {
            if (open)
                return openIcon16_;
            else
                return closedIcon16_;
        }
    }



/**
Indicates if files, directories, or both are contained in the
list of details children.

@return  One of the constants: INCLUDE_FILES, INCLUDE_DIRECTORIES,
         or INCLUDE_BOTH.
**/
    public int getInclude ()
    {
        return actualFilter_.getInclude ();
    }



/**
Returns the index of the specified child.

@param  child   The child.
@return         The index.
**/
    public /* @A2D synchronized */ int getIndex (TreeNode child)
    {
        if (childrenLoaded_ == false)
            loadChildren ();

        synchronized (children_) {  // @C3A
          for (int i = 0; i < children_.length; ++i)
            if (children_[i] == child)
              return i;
        }
        return -1;
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
Returns the directory name.

@return The directory name.

@see com.ibm.as400.access.IFSFile#getName
**/
    public String getName ()
    {
        return directory_.getName ();
    }



/**
Returns the parent node.

@return The parent node, or null if there is no parent.
**/
    public TreeNode getParent ()
    {
        return parent_;
    }



/**
Returns the parent directory name.

@return The parent directory name.

@see com.ibm.as400.access.IFSFile#getParent
**/
    public String getParentDirectory ()
    {
        return directory_.getParent ();
    }



/**
Returns the pattern that all file and directory names must match
to be included as children.  The pattern is defined in terms
of * and ?.

@return The pattern that all file and directory names must match
        to be included as children, or null to include all files
        and directories.
**/
    public String getPattern ()
    {
        return pattern_;
    }



/**
Returns the fully qualified path name of the file.

@return The fully qualified path name of the file.

@see com.ibm.as400.access.IFSFile#getPath
**/
    public String getPath ()
    {
        return directory_.getPath ();
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
    public /* @A2D synchronized */ Object getPropertyValue (Object propertyIdentifier)
    {
        // Get the name.
        if (propertyIdentifier == NAME_PROPERTY)
            return this;

        // Get the description.
        else if (propertyIdentifier == DESCRIPTION_PROPERTY)
            return description_;

        // Size column.
        else if (propertyIdentifier == SIZE_PROPERTY)
            return ""; // Not applicable for directories.

        // Modified column.
        else if (propertyIdentifier == MODIFIED_PROPERTY)
            return modified_;

        // Attributes column.
        else if (propertyIdentifier == ATTRIBUTES_PROPERTY)
            return ""; // Not applicable for directories.

        // By default, return null.
        return null;
    }



/**
Returns the AS/400 on which the file resides.

@return The AS/400 on which the file resides.

@see com.ibm.as400.access.IFSFile#getSystem
**/
    public AS400 getSystem ()
    {
        return directory_.getSystem ();
    }



/**
Returns the text.  This is the name of the directory.

@return The text which is the name of the directory.
**/
    public String getText ()
    {
        String text = directory_.getName ();
        if (text.length() == 0)
            text = "/";
        return text;
    }


//@C2A
/**
If we are dealing with an object that is under QDLS or QSYS,
disables the "creation" actions.
**/
    private void initializeCreationActions ()
    {
      if (actionsInitialized_) return;  //@C2A
      // @C2C - This block was formerly located in initializeTransient().
      // @B3A
      // If we are operating on an object that is in QDLS or QSYS,
      // we want to disable the creation
      // actions, since there are problems with creating folders and
      // things in areas of the file system that aren't normal IFS.
      // Note: The Rename and Delete actions will work on "non-IFS"
      //       objects because the objects were pre-existing so there
      //       wouldn't be any naming problems. This is assuming the
      //       user knows the naming convention of the object they
      //       are trying to rename.

//    Permission perm = ((PermissionAction)actions_[4]).getPermission ();//@C2C @C4D
//    if (perm != null && perm.getType() != Permission.TYPE_ROOT)  @C4D

      String pathPrefix = directory_.getPath().toUpperCase(); //@C4A
      if (pathPrefix.startsWith("/QSYS.LIB/") || //@C4A
          pathPrefix.startsWith("/QDLS/"))       //@C4A
      {
        for (int i=0; i<2; ++i)
          actions_[i].setEnabled(false);
      }
      actionsInitialized_ = true;  //@C2A

    }



/**
Initializes the transient data.
**/
    private void initializeTransient ()
    {
        // Initialize the event support.
        errorEventSupport_      = new ErrorEventSupport (this);
        objectEventSupport_     = new VObjectEventSupport (this);
        objectListener_         = new VObjectListener_ ();
        propertyChangeSupport_  = new PropertyChangeSupport (this);
        vetoableChangeSupport_  = new VetoableChangeSupport (this);
        workingEventSupport_    = new WorkingEventSupport (this);

        directory_.addPropertyChangeListener (propertyChangeSupport_);
        directory_.addVetoableChangeListener (vetoableChangeSupport_);

        // Initialize the actions.
        actions_    = new VAction[5]; //@B2C Changed from [4] to [5]
        actions_[0] = new IFSFileCreateAction (this, directory_);
        actions_[1] = new IFSDirectoryCreateAction (this, directory_);
        actions_[2] = new IFSRenameAction (this, directory_);
        actions_[3] = new IFSDeleteAction (this, directory_);
        actions_[4] = new PermissionAction(directory_); // @B2A Add PermissionAction.

        //@C2C
        // Postpone disabling the creation actions until we need to use the actions.
        actionsInitialized_ = false;  //@C2C

        for (int i = 0; i < actions_.length; ++i) {
            actions_[i].addErrorListener (errorEventSupport_);
            actions_[i].addVObjectListener (objectListener_);
//@C0D            actions_[i].addVObjectListener (objectEventSupport_); // @B2A
            actions_[i].addWorkingListener (workingEventSupport_);
        }

        // Initialize the properties pane.
        propertiesPane_ = new IFSDirectoryPropertiesPane (this);

        propertiesPane_.addErrorListener (errorEventSupport_);
        propertiesPane_.addVObjectListener (objectListener_);
//@C0D        propertiesPane_.addVObjectListener (objectEventSupport_); //@B2A
        propertiesPane_.addWorkingListener (workingEventSupport_);

        // Initialize the details values.
        children_               = new VNode[0];
        detailsChildren_        = new VObject[0];
        modified_               = new Date ();  //@C1C

        // Initialize the children loaded flag to true.  This
        // makes sure that we do not go to the server until
        // after load() has been called.
        childrenLoaded_         = true;

        deleted_                = false; // @A1A
    }



/**
Indicates if the node is a leaf.

@param  true if the node if a leaf; false otherwise.
**/
    public boolean isLeaf ()
    {
        // This is a performance kludge to help avoid
        // loading the grandchild of the root right away.
        // All nodes will look like non-leafs, until
        // actually loaded for some other (more important)
        // reason.
        if (childrenLoaded_)
            return (getChildCount () == 0);
        else
            return false;
    }



/**
Indicates if the details children are sortable.

@return Always true.
**/
    public boolean isSortable ()
    {
        return true;
    }



/**
Loads information about the object from the AS/400.
**/
    public void load ()
    {
        workingEventSupport_.fireStartWorking ();

        // @A2D synchronized (this) {
            initializeCreationActions ();  //@C2A

            // Load the modified date.
            try {
                modified_ = new Date (directory_.lastModified());
            }
            catch (Exception e) {
                modified_ = new Date (0);
                errorEventSupport_.fireError (e);
            }

            // Reset children.  Force a reload of children
            // the next time that they are needed.
            childrenLoaded_ = false;
        // @A2D }

        // Done loading.
        workingEventSupport_.fireStopWorking ();
    }



/**
Loads the children from the AS/400.
**/
    private void loadChildren ()
    {
        workingEventSupport_.fireStartWorking ();

        try {

            // @A2D synchronized (this) {

                // Keep a list of current children.  This is necessary
                // because we do not want to recreate objects that
                // that continue to exist.  If we did this, the explorer
                // pane does not work as expected.  (I.e. we end up
                // with 2 different objects representing the same
                // directory.)
                Hashtable cache = new Hashtable ();
                for (int i = 0; i < detailsChildren_.length; ++i)
                    cache.put (detailsChildren_[i].getText (), detailsChildren_[i]);

                // Get the list of file names.  Handle the case where
                // a system or path may not have been set yet.
                if (Trace.isTraceOn ())
                    Trace.log (Trace.INFORMATION, "Loading IFS file list ["
                       + directory_.getName () + "].");

                // Make sure all necessary properties have been set.
                Vector directories = null;
                synchronized (detailsChildren_) {  // @C3A
                  if (directory_.getPath ().length () > 0) {

                    //@C5C Change to use listFiles() rather than list().
                    //@C5C Rename the array from fileNames to files to reflect change in meaning.
                    IFSFile[] files = directory_.listFiles (actualFilter_, pattern_);

                    // If list returns null, it could be because we
                    // just don't have authority (in which case we should
                    // pretend that the directory is empty) or it may not
                    // exist (in which we should report an error).
                    if (files == null) {  
                      Trace.log(Trace.DIAGNOSTIC, "File list returned as null"); //@C5A
                      if (directory_.exists ())
                        files = new IFSFile[0]; //@C5C
                      else
                        throw new IOException (ResourceLoader.getText ("EXC_FILE_NOT_FOUND"));
                    }

                    // Build a list of children.  Actually build two lists.
                    // One contains all children, and one contains only the
                    // subdirectories, which gets used for trees.
                    int fileCount = files.length;   
                    //AS400 system = directory_.getSystem ();  //@C5D Delete these two lines.
                    //String path = directory_.getPath ();
                    detailsChildren_ = new VObject[fileCount];
                    directories = new Vector (fileCount);

                    // For each file and directory that came back from the
                    // server...
                    for (int i = 0; i < fileCount; ++i) {

                      // Check to see if the VObject already exists.
                      // If not, then create a new one.
                      VObject child = null;
                      //@C5C Change to use getName().
                      if (cache.containsKey (files[i].getName())) 
                        child = (VObject) cache.remove (files[i].getName()); 
                      else {
                        IFSFile file = files[i]; //@C5A
                        //IFSFile file = new IFSFile (system, path, fileNames[i]); //@C5D
                        if (file.isDirectory ())
                          child = new VIFSDirectory (VIFSDirectory.this, file);
                        else
                          child = new VIFSFile (file);

                        // Listen to the new child.
                        child.addErrorListener (errorEventSupport_);
                        child.addVObjectListener (objectEventSupport_);
                        child.addVObjectListener (objectListener_);
                        child.addWorkingListener (workingEventSupport_);
                      }

                      // Load the child.
                      child.load ();

                      // Add to the lists.
                      detailsChildren_[i] = child;
                      if ((child instanceof VIFSDirectory) &&
                          (!directories.contains (child)))  // @C3A
                        directories.addElement (child);
                    }
                  }
                }

                // Create the children array for directories only.
                if (directories != null) {
                  synchronized (children_) {  // @C3A
                    children_ = new VNode[directories.size ()];
                    directories.copyInto (children_);
                  }
                }

                // Stop listening to children that are no more, if any.
                Enumeration enum = cache.elements ();
                while (enum.hasMoreElements ()) {
                    VObject child = (VObject) enum.nextElement ();
                    child.removeErrorListener (errorEventSupport_);
                    child.removeVObjectListener (objectEventSupport_);
                    child.removeVObjectListener (objectListener_);
                    child.removeWorkingListener (workingEventSupport_);
                }

                childrenLoaded_ = true;
            // @A2D }
        }
        catch (Exception e) {

            // @A2D synchronized (this) {
                children_ = new VNode[0];
                detailsChildren_ = new VObject[0];
                childrenLoaded_ = true;
            // @A2D }

            if (! deleted_) // @A1A
                errorEventSupport_.fireError (e);
        }

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
    public void removeVetoableChangeListener(VetoableChangeListener listener)
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
Sets the filter which determines which files and directories
are included as children.

@param filter  The filter which determines which files are included
               as children, or null to include all files and
               directories.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setFilter (IFSFileFilter filter)
        throws PropertyVetoException
    {
        if (filter == null)
            throw new NullPointerException ("filter");

        IFSFileFilter oldValue = actualFilter_.getOtherFilter ();
        IFSFileFilter newValue = filter;
        vetoableChangeSupport_.fireVetoableChange ("filter", oldValue, newValue);

        if (oldValue != newValue)
            actualFilter_.setOtherFilter (newValue);

        propertyChangeSupport_.firePropertyChange ("filter", oldValue, newValue);
    }



/**
Sets whether files, directories, or both are contained in the
list of details children.

@param include  One of the constants: INCLUDE_FILES,
                INCLUDE_DIRECTORIES, or INCLUDE_BOTH.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setInclude (int include)
        throws PropertyVetoException
    {
        Integer oldValue = new Integer (actualFilter_.getInclude ());
        Integer newValue = new Integer (include);
        vetoableChangeSupport_.fireVetoableChange ("includeFiles", oldValue, newValue);

        if (oldValue != newValue)
            actualFilter_.setInclude (include);

        propertyChangeSupport_.firePropertyChange ("includeFiles", oldValue, newValue);
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

        directory_.setPath (path);
    }



/**
Sets the pattern that all file and directory names must match
to be included as children.  The pattern is defined in terms
of * and ?.  The default is to include all files and directories.

@param pattern  The pattern that all file and directory names
                must match to be included as children, or null
                to include all files  and directories.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setPattern (String pattern)
        throws PropertyVetoException
    {
        if (pattern == null)
            throw new NullPointerException ("pattern");

        String oldValue = pattern_;
        String newValue = pattern;
        vetoableChangeSupport_.fireVetoableChange ("pattern", oldValue, newValue);

        if (! oldValue.equals (newValue))
            pattern_ = newValue;

        propertyChangeSupport_.firePropertyChange ("pattern", oldValue, newValue);
    }



/**
Sets the AS/400 system on which the file resides.

@param system The AS/400 system on which the file resides.

@exception PropertyVetoException If the change is vetoed.

@see com.ibm.as400.access.IFSFile#setSystem
**/
    public void setSystem (AS400 system)
        throws PropertyVetoException
    {
        if (system == null)
            throw new NullPointerException ("system");

        directory_.setSystem (system);
    }



/**
Sorts the children for the details.

@param  propertyIdentifiers The property identifiers.  If any of
                            the property identifiers are null, it
                            means to sort using the string
                            representation of the object.
@param  orders              The sort orders for each property
                            identifier, true for ascending order,
                            false for descending order.
**/
    public synchronized void sortDetailsChildren (Object[] propertyIdentifiers,
                                                  boolean[] orders)
    {
        if (propertyIdentifiers == null)
            throw new NullPointerException ("propertyIdentifiers");
        if (orders == null)
            throw new NullPointerException ("orders");

        VUtilities.sort (detailsChildren_, propertyIdentifiers, orders);
    }



/**
Returns the string representation.  This is the name of the directory.

@return The string representation of the directory.
**/
    public String toString ()
    {
        return directory_.getName ();
    }



/**
Listens for events and adjusts the children accordingly.
**/
    private class VObjectListener_
    implements VObjectListener, Serializable
    {
        public void objectChanged (VObjectEvent event)
        {
            // Forward this event to the event support.
            objectEventSupport_.objectChanged (event);
        }



        public void objectCreated (VObjectEvent event)
        {
            VObject object = event.getObject ();
            VNode parent = event.getParent ();

            if (parent == VIFSDirectory.this) {

                // @C3D synchronized (VIFSDirectory.this) {

                    // Add to the details children.
                    synchronized (detailsChildren_) {  // @C3A
                      VObject[] oldDetailsChildren = detailsChildren_;
                      int count = detailsChildren_.length;
                      detailsChildren_ = new VObject[count + 1];
                      System.arraycopy (oldDetailsChildren, 0,
                                        detailsChildren_, 0, count);
                      detailsChildren_[count] = object;
                    }

                    // If its a directory, then add to the tree children.
                    if (object instanceof VIFSDirectory) {
                      synchronized (children_) {  // @C3A
                        VNode[] oldChildren = children_;
                        int countX = children_.length;
                        children_ = new VNode[countX + 1];
                        System.arraycopy (oldChildren, 0, children_,
                            0, countX);
                        children_[countX] = (VIFSDirectory) object;
                      }
                    }
                }

                // Listen to the new object.
                object.addErrorListener (errorEventSupport_);
                object.addVObjectListener (objectListener_);
                object.addVObjectListener (objectEventSupport_);
                object.addWorkingListener (workingEventSupport_);
            // @C3D }

            // Forward this event to the event support last,
            // so the the listener can handle it in the case
            // that we just added it to our list.
            objectEventSupport_.objectCreated (event);
        }



        public void objectDeleted (VObjectEvent event)
        {
            VObject object = event.getObject ();

            if (object == VIFSDirectory.this) {     // @A1A
                deleted_ = true;                    // @A1A

                // Forward this event to the event support first,
                // so the the listener can handle it before our parent
                // goes and removes the object from its list.
                objectEventSupport_.objectDeleted (event);

                return;                             // @A1A
            }                                       // @A1A

            // If the deleted object is contained in the list,
            // then remove it from the list.
            // @C3D synchronized (VIFSDirectory.this) {

                // Remove from the details children.
                int count;
                int index = getDetailsIndex (object);
                if (index >= 0) {
                  synchronized (detailsChildren_) {  // @C3A
                    VObject[] oldDetailsChildren = detailsChildren_;
                    count = detailsChildren_.length;
                    detailsChildren_ = new VObject[count - 1];
                    System.arraycopy (oldDetailsChildren, 0,
                        detailsChildren_, 0, index);
                    System.arraycopy (oldDetailsChildren, index + 1,
                        detailsChildren_, index, count - index - 1);
                  }
                };

                // If its a directory, then remove from the tree children.
                if (object instanceof VIFSDirectory) {
                    index = getIndex ((VIFSDirectory) object);
                    if (index >= 0) {
                      synchronized (children_) {  // @C3A
                        VNode[] oldChildren = children_;
                        count = children_.length;
                        children_ = new VNode[count - 1];
                        System.arraycopy (oldChildren, 0,
                            children_, 0, index);
                        System.arraycopy (oldChildren, index + 1,
                            children_, 0, count - index - 1);
                      }
                    }
                }
            // @C3D  }

            // Stop listening to the object.
            object.removeErrorListener (errorEventSupport_);
            object.removeVObjectListener (objectEventSupport_);
            object.removeVObjectListener (objectListener_);
            object.removeWorkingListener (workingEventSupport_);
        }


    }

}
