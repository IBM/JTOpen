///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RIFSFileList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.IFSFileFilter;
import com.ibm.as400.access.IFSJavaFile;
import java.beans.PropertyVetoException;
import java.util.Enumeration;
import java.util.Vector;



/**
The RIFSFileList class represents a list of files and directories
in the AS/400 integrated file system.  This class provides function similar to
{@link com.ibm.as400.access.IFSFile IFSFile } and
{@link com.ibm.as400.access.IFSJavaFile IFSJavaFile }
except that it is a subclass of
{@link com.ibm.as400.resource.ResourceList ResourceList}.  As a result,
it can be used directly in conjunction with components written for ResourceList objects.

<p>If the specified directory does not exist, the list will be empty.

<p>The contents of the list are always loaded sequentially, regardless of the
order that you request them.  In addition, they are loaded on demand, meaning the
entire list is not loaded until the last item is requested.  Use the
{@link #resourceAt resourceAt()} method to request a particular list item.  If that
list item is not yet loaded, resourceAt() returns null.  To ensure that a
particular list item is loaded, call {@link #waitForResource waitForResource()}.
To ensure that the entire list is loaded, call {@link #waitForComplete waitForComplete()}.
Note that both wait methods will block until the requested resource(s) are loaded.

<a name="selectionIDs"><p>The following selection IDs are supported:
<ul>
<li>{@link #FILTER FILTER}
<li>{@link #PATTERN PATTERN}
</ul>

<p>Use one or more of these selection IDs with
{@link com.ibm.as400.resource.ResourceList#getSelectionValue getSelectionValue()}
and {@link com.ibm.as400.resource.ResourceList#setSelectionValue setSelectionValue()}
to access the selection values for an RIFSFileList.

<p>RIFSFileList objects generate {@link com.ibm.as400.resource.RIFSFile RIFSFile} objects.

<blockquote><pre>
// Create an RIFSFileList object to represent a list of files.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RIFSFileList fileList = new RIFSFileList(system, "/home/myuserid");
<br>
// Set the selection so that only Java source files are listed.
fileList.setSelectionValue(RIFSFileList.PATTERN, "*.java");
<br>
// Open the list and get the first 50 items.
fileList.open();
fileList.waitForResource(50);
<br>
// Read and print the file names and last modified dates
// for the first 50 items in the list.
for(long i = 0; i &lt; 50; ++i)
{
    RIFSFile file = (RIFSFile)fileList.resourceAt(i);
    System.out.println(file.getAttributeValue(RIFSFile.NAME));
    System.out.println(file.getAttributeValue(RIFSFile.LAST_MODIFIED));
    System.out.println();
}
<br>
// Close the list.
fileList.close();
</pre></blockquote>

@see RIFSFile
**/
public class RIFSFileList
extends ResourceList
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



//-----------------------------------------------------------------------------------------
// Presentation.
//-----------------------------------------------------------------------------------------

    private static final String                 PRESENTATION_KEY_           = "IFSFILE_LIST";
    private static final String                 ICON_BASE_NAME_             = "RIFSFileList";
    private static PresentationLoader           presentationLoader_         = new PresentationLoader("com.ibm.as400.resource.ResourceMRI");




//-----------------------------------------------------------------------------------------
// Selection IDs.
//
// * If you add a selection here, make sure and add it to the class javadoc
//   and in ResourceMRI.java.
//-----------------------------------------------------------------------------------------

    private static ResourceMetaDataTable selections_        = new ResourceMetaDataTable(presentationLoader_, PRESENTATION_KEY_);



/**
Selection ID for filter.  This identifies an
{@link com.ibm.as400.access.IFSFileFilter IFSFileFilter} selection,
which represents the filter used to select which files are included in the
list.
**/
    public static final String FILTER                      = "FILTER";

    static {
        selections_.add(FILTER, IFSFileFilter.class, false);
    }



/**
Selection ID for pattern.  This identifies a String selection,
which represents the pattern used to select which files are included in the list.
This value can include wildcards (*) and question marks (?).
**/
    public static final String PATTERN                      = "PATTERN";

    static {
        selections_.add(PATTERN, String.class, false);
    }



//-----------------------------------------------------------------------------------------
// Private data.
//-----------------------------------------------------------------------------------------

    private Vector          cache_;
    private Enumeration     enum_;
    private IFSFile         file_       = new IFSFile();
    private String          path_       = null;



//-----------------------------------------------------------------------------------------
// Code.
//-----------------------------------------------------------------------------------------

/**
Constructs an RIFSFileList object.
**/
    public RIFSFileList()
    {
        super(presentationLoader_.getPresentationWithIcon(PRESENTATION_KEY_, ICON_BASE_NAME_),
              RIFSFile.attributes_,
              selections_,
              null);
    }



/**
Constructs an RIFSFileList object.

@param system   The system.
@param path     The directory path.
**/
    public RIFSFileList(AS400 system, String path)
    {
        this();
        try {
            setSystem(system);
            setPath(path);
        }
        catch(PropertyVetoException e) {
            // Ignore.
        }
    }



/**
Constructs an RIFSFileList object.

@param file     The directory.
**/
//
// Implementation note: This constructor is to faciliate cases where list items
//                      (RIFSFile objects) must be recursively treated as
//                      lists (RIFSFileList objects).
//
    public RIFSFileList(RIFSFile file)
    {
        this();
        try {
            setSystem(file.getSystem());
            setPath(file.getPath());
        }
        catch(PropertyVetoException e) {
            // Ignore.
        }
    }




/**
Establishes the connection to the AS/400.

<p>The method is called by the resource framework automatically
when the connection needs to be established.

@exception ResourceException                If an error occurs.
**/
    protected void establishConnection()
    throws ResourceException
    {
        // Validate if we can establish the connection.
        if (file_.getPath() == null)
            throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        // Call the superclass.
        super.establishConnection();

        try {
            file_.setSystem(getSystem());
        }
        catch(PropertyVetoException e) {
            // Ignore.
        }
    }



/**
Freezes any property changes.  After this is called, property
changes should not be made.  Properties are not the same thing
as attributes.  Properties are basic pieces of information
which must be set to make the object usable, such as the system
and the name.

<p>The method is called by the resource framework automatically
when the properties need to be frozen.

@exception ResourceException                If an error occurs.
**/
    protected void freezeProperties()
    throws ResourceException
    {
        // Validate if we can establish the connection.
        if (file_.getPath() == null)
            throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        // Update the presentation.
        Presentation presentation = getPresentation();
        presentation.setName(file_.getName());
        presentation.setFullName(file_.getPath());

        // Call the superclass.
        super.freezeProperties();
    }



/**
Returns the directory path.

@return The directory path.
**/
    public String getPath()
    {
        return path_;
    }



/**
Opens the list.  The list must be open in order to
perform most operations.  This method has no effect
if the list is already opened.

@exception ResourceException                If an error occurs.
**/
     public void open()
    throws ResourceException
    {
        if (isOpen())
            return;

        super.open();

        fireBusy();
        try {
            synchronized(this) {

                // Establish the connection if needed.
                if (! isConnectionEstablished())
                    establishConnection();

                try {
                    String pattern = (String)getSelectionValue(PATTERN);
                    if (pattern == null)
                        enum_ = file_.enumerateFiles((IFSFileFilter)getSelectionValue(FILTER));
                    else
                        enum_ = file_.enumerateFiles((IFSFileFilter)getSelectionValue(FILTER), pattern);
                    cache_ = new Vector();
                    if (! enum_.hasMoreElements())
                        fireListCompleted();
                }
                catch(Exception e) {
                    throw new ResourceException(e);
                }
            }
        }
        finally {
            fireIdle();
        }
    }



/**
Refreshes the contents of the list.

<p>This will implicitly open the list if needed.

@exception ResourceException                If an error occurs.
**/
     public void refreshContents()
    throws ResourceException
    {
        if (isOpen())
            close();
        super.refreshContents();
    }



/**
Returns the resource specified by the index.

<p>This will implicitly open the list if needed.

@param  index   The index.
@return         The resource specified by the index, or null
                if the resource is not yet available.

@exception ResourceException                If an error occurs.
**/
     public Resource resourceAt(long index)
    throws ResourceException
    {
        synchronized(this) {

            // It may already be here.
            Resource resource = super.resourceAt(index);

            // If not, try to load it.
            if (resource == null) {

                synchronized(cache_) {
                    if (index < cache_.size()) {
                        return (Resource)cache_.elementAt((int)index);
                    }

                    else {
                        for(int i = cache_.size(); i <= index; ++i) {
                            resource = new RIFSFile((IFSFile)enum_.nextElement());
                            cache_.addElement(resource);
                            resource.freezeProperties();
                            fireResourceAdded(resource, i);
                            fireLengthChanged(i + 1);
                            if (! enum_.hasMoreElements()) {
                                fireListCompleted();
                                break;
                            }
                        }
                    }
                }
            }

            return resource;
        }
    }



/**
Sets the directory path.  This does not change the directory on
the AS/400.  Instead, it changes the directory to which
this object references.  This cannot be changed
if the object has established a connection to the AS/400.

@param path    The directory path.
**/
    public void setPath(String path)
    {
        if (path == null)
            throw new NullPointerException("path");
        if (arePropertiesFrozen())
            throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

        String oldValue = file_.getPath();
        try {
            path_ = path;
            // Handle switching the direction of separator chars, if necessary.
            String tempPath = path.replace(IFSJavaFile.separatorChar, IFSFile.separatorChar);
            file_.setPath(tempPath);
        }
        catch(PropertyVetoException e) {
            // Ignore.
        }
        firePropertyChange("path", oldValue, path);
    }



/**
Waits until the list is completely loaded.

<p>This will implicitly open the list if needed.

@exception ResourceException                If an error occurs.
**/
     public void waitForComplete()
    throws ResourceException
    {
        super.waitForComplete();

        // Load all files into the cache.
        int i = 0;
        while(!isComplete()) {
            resourceAt(i++);
        }
    }



/**
Waits until the resource is available or the list is
complete.  This waits until all resources up to and including
the specified resource are loaded.

<p>This will implicitly open the list if needed.

@param index    The index.

@exception ResourceException                If an error occurs.
**/
     public void waitForResource(long index)
    throws ResourceException
    {
        super.waitForResource(index);

        // Load all files up to and including this one into the cache.
        int i = 0;
        while((!isComplete()) && (i <= index))
            resourceAt(i++);
    }



}

