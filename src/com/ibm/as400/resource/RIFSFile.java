///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RIFSFile.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.IFSJavaFile;
import com.ibm.as400.access.IFSFile;
import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.Date;



/**
The RIFSFile class represents a file or directory in the integrated
file system on the system.  This class provides function similar to
{@link com.ibm.as400.access.IFSFile IFSFile } and
{@link com.ibm.as400.access.IFSJavaFile IFSJavaFile }
except that it is a subclass of {@link com.ibm.as400.resource.Resource Resource}.
As a result, it can be used directly in conjunction with components
written for Resource objects.

<a name="attributeIDs"><p>The following attribute IDs are supported:
<ul>
<li>{@link #ABSOLUTE_PATH ABSOLUTE_PATH}
<li>{@link #CANONICAL_PATH CANONICAL_PATH}
<li>{@link #CAN_READ CAN_READ}
<li>{@link #CAN_WRITE CAN_WRITE}
<li>{@link #CCSID CCSID}
<li>{@link #CREATED CREATED}
<li>{@link #EXISTS EXISTS}
<li>{@link #IS_ABSOLUTE IS_ABSOLUTE}
<li>{@link #IS_DIRECTORY IS_DIRECTORY}
<li>{@link #IS_FILE IS_FILE}
<li>{@link #IS_HIDDEN IS_HIDDEN}
<li>{@link #IS_READ_ONLY IS_READ_ONLY}
<li>{@link #LAST_ACCESSED LAST_ACCESSED}
<li>{@link #LAST_MODIFIED LAST_MODIFIED}
<li>{@link #LENGTH LENGTH}
<li>{@link #NAME NAME}
<li>{@link #PARENT PARENT}
<li>{@link #PATH PATH}
<li>{@link #TYPE TYPE}
</ul>
</a>

<p>Use any of these attribute IDs with
{@link com.ibm.as400.resource.ChangeableResource#getAttributeValue getAttributeValue()}
and {@link com.ibm.as400.resource.ChangeableResource#setAttributeValue setAttributeValue()}
to access the attribute values for an RIFSFile.

<blockquote><pre>
// Create an RIFSFile object to refer to a specific file.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RIFSFile file = new RIFSFile(system, "/home/myuserid/config.txt");
<br>
// Determine if the file is read only.
boolean readOnly = ((Boolean)file.getAttributeValue(RIFSFile.IS_READ_ONLY)).booleanValue();
<br>
// Set the last modified date to now.
file.setAttributeValue(RIFSFile.LAST_MODIFIED, new Date());
<br>
// Commit the attribute change.
file.commitAttributeChanges();
</pre></blockquote>
@deprecated Use
{@link com.ibm.as400.access.IFSFile IFSFile} instead, as this package may be removed in the future.
@see RIFSFileList
**/
//-----------------------------------------------------------------------------------------
// I thought delete() would be useful here.  There are other methods
// in IFSFile that we could duplicate here, but most of them deal with creating new
// files and directories (like createNewFile() and mkdir()).  I believe that, due to the
// nature of the resource framework, that this will be used mostly to deal with existing
// objects.  So for now I will leave these other methods off.  In the case that someone
// needs to use these, they just need to get the system and path from this object and
// use it to construct an IFSFile directly.
//-----------------------------------------------------------------------------------------
public class RIFSFile
extends ChangeableResource
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



//-----------------------------------------------------------------------------------------
// Presentation.
//-----------------------------------------------------------------------------------------

    private static PresentationLoader   presentationLoader_ = new PresentationLoader("com.ibm.as400.resource.ResourceMRI");
    private static final String         ICON_BASE_NAME_     = "RIFSFile";
    private static final String         PRESENTATION_KEY_   = "IFSFILE";



//-----------------------------------------------------------------------------------------
// Attribute IDs.
//
// * If you add an attribute here, make sure and add it to the class javadoc.
//-----------------------------------------------------------------------------------------

    // Private data.
            static ResourceMetaDataTable        attributes_             = new ResourceMetaDataTable(presentationLoader_, PRESENTATION_KEY_);



/**
Attribute ID for absolute path.  This identifies a read-only String
attribute, which represents the absolute path name.
**/
    public static final String ABSOLUTE_PATH                          = "ABSOLUTE_PATH";

    static {
        attributes_.add(ABSOLUTE_PATH, String.class, true);
    }



/**
Attribute ID for canonical path.  This identifies a read-only String
attribute, which represents the canonical path name.
**/
    public static final String CANONICAL_PATH                   = "CANONICAL_PATH";

    static {
        attributes_.add(CANONICAL_PATH, String.class, true);
    }



/**
Attribute ID for can read.  This identifies a read-only Boolean
attribute, which indicates whether this file can be read.
**/
    public static final String CAN_READ                          = "CAN_READ";

    static {
        attributes_.add(CAN_READ, Boolean.class, true);
    }



/**
Attribute ID for can write.  This identifies a read-only Boolean
attribute, which indicates whether this file can be written.
**/
    public static final String CAN_WRITE                          = "CAN_WRITE";

    static {
        attributes_.add(CAN_WRITE, Boolean.class, true);
    }



/**
Attribute ID for CCSID.  This identifies a read-only Integer
attribute, which represents the coded character set identifier
for the file.
**/
    public static final String CCSID                          = "CCSID";

    static {
        attributes_.add(CCSID, Integer.class, true);
    }



/**
Attribute ID for created.  This identifies a read-only Date
attribute, which represents the date and time that the file
was created.
**/
    public static final String CREATED                          = "CREATED";

    static {
        attributes_.add(CREATED, Date.class, true);
    }



/**
Attribute ID for exists.  This identifies a read-only Boolean
attribute, which indicates whether the file exists.
**/
    public static final String EXISTS                          = "EXISTS";

    static {
        attributes_.add(EXISTS, Boolean.class, true);
    }



/**
Attribute ID for is absolute.  This identifies a read-only Boolean
attribute, which indicates whether the path name is absolute.
**/
    public static final String IS_ABSOLUTE                          = "IS_ABSOLUTE";

    static {
        attributes_.add(IS_ABSOLUTE, Boolean.class, true);
    }



/**
Attribute ID for is directory.  This identifies a read-only Boolean
attribute, which indicates whether this is a directory.
**/
    public static final String IS_DIRECTORY                          = "IS_DIRECTORY";

    static {
        attributes_.add(IS_DIRECTORY, Boolean.class, true);
    }



/**
Attribute ID for is file.  This identifies a read-only Boolean
attribute, which indicates whether this is a file.
**/
    public static final String IS_FILE                          = "IS_FILE";

    static {
        attributes_.add(IS_FILE, Boolean.class, true);
    }



/**
Attribute ID for is hidden.  This identifies a Boolean
attribute, which indicates whether this file is hidden.
**/
    public static final String IS_HIDDEN                            = "IS_HIDDEN";

    static {
        attributes_.add(IS_HIDDEN, Boolean.class, false);
    }



/**
Attribute ID for is read only.  This identifies a Boolean
attribute, which indicates whether this file is read only.
**/
    public static final String IS_READ_ONLY                          = "IS_READ_ONLY";

    static {
        attributes_.add(IS_READ_ONLY, Boolean.class, false);
    }



/**
Attribute ID for last accessed.  This identifies a read-only Date
attribute, which represents the date and time the file was
last accessed.
**/
    public static final String LAST_ACCESSED                          = "LAST_ACCESSED";

    static {
        attributes_.add(LAST_ACCESSED, Date.class, true);
    }



/**
Attribute ID for last modified.  This identifies a Date
attribute, which represents the date and time the file was
last modified.  Setting this to 0 will leave the last modified
date unchanged.
**/
    public static final String LAST_MODIFIED                          = "LAST_MODIFIED";

    static {
        attributes_.add(LAST_MODIFIED, Date.class, false);
    }



/**
Attribute ID for length.  This identifies a read-only Long
attribute, which represents the length of the file in bytes.
**/
    public static final String LENGTH                          = "LENGTH";

    static {
        attributes_.add(LENGTH, Long.class, true);
    }



/**
Attribute ID for name.  This identifies a read-only String
attribute, which represents the name of the file.
**/
    public static final String NAME                          = "NAME";

    static {
        attributes_.add(NAME, String.class, true);
    }



// @A1a
/**
Attribute ID for owner ID.  This identifies a read-only Integer
attribute, which represents the owner ID number of the file.
**/
    public static final String OWNERID                       = "OWNERID";

    static {
        attributes_.add(OWNERID, Integer.class, true);
    }



/**
Attribute ID for parent.  This identifies a read-only String
attribute, which represents the name of the parent directory.
**/
    public static final String PARENT                          = "PARENT";

    static {
        attributes_.add(PARENT, String.class, true);
    }



/**
Attribute ID for path.  This identifies a read-only String
attribute, which represents the path name of the file.
**/
    public static final String PATH                          = "PATH";

    static {
        attributes_.add(PATH, String.class, true);
    }



/**
Attribute ID for type.  This identifies a read-only String
attribute, which indicates whether this is a directory or a file.
Possible values are:
<ul>
<li>{@link #TYPE_DIRECTORY TYPE_DIRECTORY} - This is a directory.
<li>{@link #TYPE_FILE TYPE_FILE} - This is a file.
<li>{@link #TYPE_UNKNOWN TYPE_UNKNOWN} - The type is unknown.
</ul>
**/
    public static final String TYPE                          = "TYPE";

    /**
    Attribute value indicating that this is a directory.

    @see #TYPE
    **/
    public static final String TYPE_DIRECTORY       = "*DIRECTORY";

    /**
    Attribute value indicating that this is a file.

    @see #TYPE
    **/
    public static final String TYPE_FILE       = "*FILE";

    /**
    Attribute value indicating that the tyype is unknown.

    @see #TYPE
    **/
    public static final String TYPE_UNKNOWN       = "*UNKNOWN";

    static {
        attributes_.add(TYPE, String.class, true,
                        new Object[] {TYPE_DIRECTORY, TYPE_FILE, TYPE_UNKNOWN }, null, true);
    }




//-----------------------------------------------------------------------------------------
// Private data.
//-----------------------------------------------------------------------------------------

    private IFSFile         file_               = new IFSFile();
    private String          path_               = null;




//-----------------------------------------------------------------------------------------
// Constructors.
//-----------------------------------------------------------------------------------------

/**
Constructs an RIFSFile object.
**/
    public RIFSFile()
    {
        super(presentationLoader_.getPresentationWithIcon(PRESENTATION_KEY_, ICON_BASE_NAME_), null, attributes_);
    }



/**
Constructs an RIFSFile object.

@param system   The system.
@param path     The file path name.
**/
    public RIFSFile(AS400 system, String path)
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
Constructs an RIFSFile object.

@param file     The file.
**/
    RIFSFile(IFSFile file)
    {
        this();

        try {
            setSystem(file.getSystem());
            file_ = file;
            path_ = file.getPath();
        }
        catch(PropertyVetoException e) {
            // Ignore.
        }
    }



/**
Commits the specified attribute changes.

@exception ResourceException                If an error occurs.
**/
    protected void commitAttributeChanges(Object[] attributeIDs, Object[] values)
    throws ResourceException
    {
        super.commitAttributeChanges(attributeIDs, values);

        // Establish the connection if needed.
        if (! isConnectionEstablished())
            establishConnection();

        // Make the changes.
        try {
           for(int i = 0; i < attributeIDs.length; ++i) {
               boolean success = true;
               if (attributeIDs[i].equals(RIFSFile.IS_HIDDEN))
                   success = file_.setHidden(((Boolean)values[i]).booleanValue());
               else if (attributeIDs[i].equals(RIFSFile.LAST_MODIFIED))
                   success = file_.setLastModified(((Date)values[i]).getTime());
               else if (attributeIDs[i].equals(RIFSFile.IS_READ_ONLY))
                   success = file_.setReadOnly(((Boolean)values[i]).booleanValue());
               else
                   throw new ExtendedIllegalArgumentException("attributeIDs[" + i + "](" + attributeIDs[i] + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

               if (!success)
                   throw new ResourceException(ResourceException.ATTRIBUTES_NOT_SET);
           }
       }
       catch(Exception e) {
           throw new ResourceException(ResourceException.ATTRIBUTES_NOT_SET, e);
       }
    }




/**
Computes a resource key.

@param system       The system.
@param path         The file path name.
@return             The resource key.
**/
    static Object computeResourceKey(AS400 system, String path)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(RIFSFile.class);
        buffer.append(':');
        buffer.append(system.getSystemName());
        buffer.append(':');
        buffer.append(system.getUserId());
        buffer.append(':');
        buffer.append(path);
        return buffer.toString();
    }



/**
Deletes this file or directory.

@exception ResourceException                If an error occurs.
**/
    public void delete()
    throws ResourceException
    {
        // Establish the connection if needed.
        if (!isConnectionEstablished())
            establishConnection();

        try {
            boolean success = file_.delete();
            if (success == false)
                throw new ResourceException(ResourceException.OPERATION_FAILED);
        }
        catch(Exception e) {
            throw new ResourceException(e);
        }
    }



/**
Establishes the connection to the system.

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

        // Update the resource key.
        if (getResourceKey() == null)
            setResourceKey(computeResourceKey(getSystem(), file_.getPath()));

        // Call the superclass.
        super.freezeProperties();
    }



/**
Returns the unchanged value of an attribute.   If the attribute
value has an uncommitted change, this returns the unchanged value.
If the attribute value does not have an uncommitted change, this
returns the same value as <b>getAttributeValue()</b>.

@param attributeID  Identifies the attribute.
@return             The attribute value, or null if the attribute
                    value is not available.

@exception ResourceException                If an error occurs.
**/
    public Object getAttributeUnchangedValue(Object attributeID)
    throws ResourceException
    {
        Object value = super.getAttributeUnchangedValue(attributeID);
        if (value == null) {

            // Establish the connection if needed.
            if (! isConnectionEstablished())
                establishConnection();

            try {
              if (attributeID.equals(RIFSFile.ABSOLUTE_PATH))
                  return file_.getAbsolutePath();
              else if (attributeID.equals(RIFSFile.CANONICAL_PATH))
                  return file_.getCanonicalPath();
              else if (attributeID.equals(RIFSFile.CAN_READ))
                  return file_.canRead() ? Boolean.TRUE : Boolean.FALSE;
              else if (attributeID.equals(RIFSFile.CAN_WRITE))
                  return file_.canWrite() ? Boolean.TRUE : Boolean.FALSE;
              else if (attributeID.equals(RIFSFile.CCSID))
                  return new Integer(file_.getCCSID());
              else if (attributeID.equals(RIFSFile.CREATED))
                  return new Date(file_.created());
              else if (attributeID.equals(RIFSFile.EXISTS))
                  return file_.exists() ? Boolean.TRUE : Boolean.FALSE;
              else if (attributeID.equals(RIFSFile.IS_ABSOLUTE))
                  return file_.isAbsolute() ? Boolean.TRUE : Boolean.FALSE;
              else if (attributeID.equals(RIFSFile.IS_DIRECTORY))
                  return file_.isDirectory() ? Boolean.TRUE : Boolean.FALSE;
              else if (attributeID.equals(RIFSFile.IS_FILE))
                  return file_.isFile() ? Boolean.TRUE : Boolean.FALSE;
              else if (attributeID.equals(RIFSFile.IS_HIDDEN))
                  return file_.isHidden() ? Boolean.TRUE : Boolean.FALSE;
              else if (attributeID.equals(RIFSFile.IS_READ_ONLY))
                  return file_.isReadOnly() ? Boolean.TRUE : Boolean.FALSE;
              else if (attributeID.equals(RIFSFile.LAST_ACCESSED))
                  return new Date(file_.lastAccessed());
              else if (attributeID.equals(RIFSFile.LAST_MODIFIED))
                  return new Date(file_.lastModified());
              else if (attributeID.equals(RIFSFile.LENGTH))
                  return new Long(file_.length());
              else if (attributeID.equals(RIFSFile.NAME))
                  return file_.getName();
              else if (attributeID.equals(RIFSFile.OWNERID))  // @A1a
                  return new Integer(file_.getOwnerId());
              else if (attributeID.equals(RIFSFile.PARENT))
                  return file_.getParent();
              else if (attributeID.equals(RIFSFile.PATH))
                  return file_.getPath();
              else if (attributeID.equals(RIFSFile.TYPE)) {
                  if (file_.isDirectory())
                      return TYPE_DIRECTORY;
                  else if (file_.isFile())
                      return TYPE_FILE;
                  else
                      return TYPE_UNKNOWN;
              }
              else
                  throw new ExtendedIllegalArgumentException("attributeID(" + attributeID + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            }
            catch(Exception e) {
                throw new ResourceException(e);
            }
        }
        else
            return value;
    }




/**
Returns the file path name.

@return The file path name.
**/
    public String getPath()
    {
        return path_;
    }


/**
Refreshes the values for all attributes.  This does not cancel
uncommitted changes.  This method fires an attributeValuesRefreshed()
ResourceEvent.

@exception ResourceException                If an error occurs.
**/
    public void refreshAttributeValues()
    throws ResourceException
    {
        super.refreshAttributeValues();
        file_.clearCachedAttributes();
    }




/**
Sets the file path name.  This does not change the file on
the system.  Instead, it changes the file to which
this object references.  This cannot be changed
if the object has established a connection to the system.

@param path    The file path name.
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
Sets the system.  This does not change the job on
the system.  Instead, it changes the system to which
this object references.  This cannot be changed
if the object has established a connection to the system.

@param system    The system.

@exception PropertyVetoException    If the property change is vetoed.
**/
    public void setSystem(AS400 system)
        throws PropertyVetoException
    {
        super.setSystem(system);
        file_.setSystem(system);
    }





}
