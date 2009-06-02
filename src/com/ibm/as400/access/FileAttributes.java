///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  FileAttributes.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2006 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.util.Date;

/**
 Represents the set of file attributes that can be retrieved and set through the Qp0lGetAttr and Qp0lSetAttr API's.  The object must exist and the caller must have authority to it.  Only attributes supported by the specific file system, object type, and system operating release can be retrieved or set.
 **/
public class FileAttributes
{
    // The system where file is located.
    private AS400 system_;
    // Path to the object.
    private String path_;
    // Whether symlinks are followed.
    private boolean followSymbolicLink_;

    // Flag indicating if the attributes have been retrieved.
    private boolean attributesRetrieved_ = false;

    /**
     Constructs a FileAttributes object.
     @param  system  The system object representing the system on which the file exists.
     @param  path  The path name of the object for which attribute information is retrieved or set.
     @param  followSymbolicLink  If the last component in the path is a symbolic link, indicates if the symbolic link is followed.  true to operate on the attributes of the object contained in the symbolic link, false to operate on the symbolic link.
     **/
    public FileAttributes(AS400 system, String path, boolean followSymbolicLink)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing FileAttributes object, system: " + system + ", path: " + path + ", followSymbolicLink: " + followSymbolicLink);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (path == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'path' is null.");
            throw new NullPointerException("path");
        }
        system_ = system;
        path_ = path;
        followSymbolicLink_ = followSymbolicLink;
    }

    // Utility method to convert String path into path name parameter used by API's.
    private byte[] createPathName() throws IOException
    {
        Converter conv = new Converter(1200, system_);

        byte[] pathName = new byte[32 + path_.length() * 2];
        BinaryConverter.intToByteArray(1200, pathName, 0);
        BinaryConverter.intToByteArray(2, pathName, 12);
        BinaryConverter.intToByteArray(path_.length() * 2, pathName, 16);
        conv.stringToByteArray("/", pathName, 20, 2);
        conv.stringToByteArray(path_, pathName, 32);
        return pathName;
    }

    // The object type.
    String objectType_;
    /**
     Returns the object type.
     @return  The object type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getObjectType() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return objectType_;
    }

    // The size of the extended attributes.
    long extendedAttributeSize_;
    /**
     Returns the total number of extended attribute bytes.
     @return  The total number of extended attribute bytes.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getExtendedAttributeSize() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return extendedAttributeSize_;
    }

    // The time the object was created.
    Date createTime_;
    /**
     Returns the time the object was created.
     @return  The time the object was created.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public Date getCreateTime() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return createTime_;
    }
    /**
     Sets the time the object was created.
     @param  createTime  The time the object was created.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setCreateTime(Date createTime) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (createTime == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'createTime' is null.");
            throw new NullPointerException("createTime");
        }
        setAttributes(createTime, (byte)4);
    }

    // The time that the object's data was last accessed.
    Date accessTime_;
    /**
     Returns the time that the object's data was last accessed.
     @return  The time that the object's data was last accessed.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public Date getAccessTime() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return accessTime_;
    }
    /**
     Sets the time that the object's data was last accessed.
     @param  accessTime  The time that the object's data was last accessed.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setAccessTime(Date accessTime) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (accessTime == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'accessTime' is null.");
            throw new NullPointerException("accessTime");
        }
        setAttributes(accessTime, (byte)5);
    }

    // The time that the object's data or attributes were last changed.
    Date changeTime_;
    /**
     Returns the time that the object's data or attributes were last changed.
     @return  The time that the object's data or attributes were last changed.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public Date getChangeTime() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return changeTime_;
    }

    // The time that the object's data was last changed.
    Date modifyTime_;
    /**
     Returns the time that the object's data was last changed.
     @return  The time that the object's data was last changed.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public Date getModifyTime() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return modifyTime_;
    }
    /**
     Sets the time that the object's data was last changed.
     @param  modifyTime  The time that the object's data was last changed.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setModifyTime(Date modifyTime) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (modifyTime == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'modifyTime' is null.");
            throw new NullPointerException("modifyTime");
        }
        setAttributes(modifyTime, (byte)7);
    }

    // Indicates if storage is freed.
    boolean storageFree_;
    /**
     Returns whether the object's data has been moved offline, freeing its online storage.
     @return  true if the object's data is offline; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isStorageFree() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return storageFree_;
    }

    // Indicates if object is checked out.
    boolean checkedOut_;
    /**
     Returns whether the object is checked out or not.  When an object is checked out, other users can read and copy the object.  Only the user who has the object checked out can change the object.
     @return  true if object is checked out; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isCheckedOut() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return checkedOut_;
    }

    // The user who has the object checked out.
    String checkedOutUser_;
    /**
     Returns the user who has the object checked out.
     @return  The user who has the object checked out.  An empty string ("") is returned if the object is not checked out.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getCheckedOutUser() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return checkedOutUser_;
    }

    // The time the object was checked out.
    Date checkOutTime_;
    /**
     Returns the time the object was checked out.
     @return  The time the object was checked out.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public Date getCheckOutTime() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return checkOutTime_;
    }

    /**
     Constant indicating that the object's data is stored locally.
     @see  #getLocalRemote
     **/
    public static final int LOCAL_OBJECT = 0x01;
    /**
     Constant indicating that the object's data is on a remote system.
     @see  #getLocalRemote
     **/
    public static final int REMOTE_OBJECT = 0x02;
    // The location of an object's storage.
    int localRemote_;
    /**
     Returns whether the object is stored locally or stored on a remote system.  The decision of whether a file is local or remote varies according to the respective file system rules.  Objects in file systems that do not carry either a local or remote indicator are treated as remote.
     @return  The location of an object's storage.  Possible values are:
     <ul>
     <li>{@link #LOCAL_OBJECT LOCAL_OBJECT} - The object's data is stored locally.
     <li>{@link #REMOTE_OBJECT REMOTE_OBJECT} - The object's data is on a remote system.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getLocalRemote() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return localRemote_;
    }

    // The name of the owner of the object.
    String objectOwner_;
    /**
     Returns the name of the user profile that is the owner of the object.
     @return  The name of the user profile that is the owner of the object or the special value "*NOUSRPRF" which is an indication by the Network File System that there is no user profile on the local IBM i system with a user ID (UID) matching the UID of the remote object.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getObjectOwner() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return objectOwner_;
    }

    // The name of the primary group of the object.
    String primaryGroup_;
    /**
     Returns the name of the user profile that is the primary group of the object.
     @return  The name of the user profile that is the primary group of the object or the following special values:
     <ul>
     <li>"*NONE" - The object does not have a primary group.
     <li>"*NOUSRPRF" - This special value is used by the Network File System to indicate that there is no user profile on the local system with a group ID (GID) matching the GID of the remote object.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getPrimaryGroup() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return primaryGroup_;
    }

    // The name of the authorization list.
    String authorizationListName_;
    /**
     Returns the name of the authorization list that is used to secure the named object.
     @return  The name of the authorization list that is used to secure the named object.  The value *NONE indicates that no authorization list is used in determining authority to the object.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getAuthorizationListName() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return authorizationListName_;
    }

    // The identifier associated with the referred to object.
    byte[] fileId_;
    /**
     Returns the identifier associated with the referred to object.
     @return  The identifier associated with the referred to object.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public byte[] getFileId() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return fileId_;
    }

    // The auxiliary storage pool in which the object is stored.
    short asp_;
    /**
     Returns the auxiliary storage pool in which the object is stored.
     @return  The auxiliary storage pool (ASP) in which the object is stored.
     Possible values are:
     <ul>
     <li>1: the system ASP (QASP01, also known as the system disk pool)
     <li>2 to 32: user ASPs (QASP02 to QASP32)
     <li>33 to 255: independent ASPs
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public short getAsp() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return asp_;
    }

    //
    long dataSize_;
    /**
     Returns the size in bytes of the data in this object.  The size varies by object type and file system.  This size does not include object headers or the size of extended attributes associated with the object.
     @return  The size in bytes of the data in this object.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getDataSize() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return dataSize_;
    }

    // The number of bytes that have been allocated for this object.
    long allocatedSize_;
    /**
     Returns the number of bytes that have been allocated for this object.  The allocated size varies by object type and file system.  For example, the allocated size includes the object data size as shown in getDataSize() as well as any logically sized extents to accommodate anticipated future requirements for the object data.  It may or may not include additional bytes for attribute information.
     @return  The number of bytes that have been allocated for this object.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getAllocatedSize() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return allocatedSize_;
    }

    // The date the days used count was last reset to zero (0).
    Date resetDate_;
    /**
     Returns the date the days used count was last reset to zero (0).  This date is set to the current date when resetDate() is called to reset the days used count to zero.
     @return  The date the days used count was last reset to zero (0).
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public Date getResetDate() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return resetDate_;
    }

    // The date the object was last used.
    Date lastUsedDate_;
    /**
     Returns the date the object was last used.  If the object has not been used or if usage data is not maintained for the IBM i type or the file system to which an object belongs, null is returned.
     @return  The date the object was last used.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public Date getLastUsedDate() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return lastUsedDate_;
    }

    // The number of days an object has been used.
    int daysUsedCount_;
    /**
     Returns the number of days an object has been used.  Usage has different meanings according to the specific file system and according to the individual object types supported within a file system.  Usage can indicate the opening or closing of a file or can refer to adding links, renaming, restoring, or checking out an object.  This count is incremented once each day that an object is used and is reset to zero by calling resetDate().
     @return  The number of days an object has been used.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getDaysUsedCount() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return daysUsedCount_;
    }

    // Whether the object is read only.
    boolean pcReadOnly_;
    /**
     Returns whether the object can be written to or deleted, have its extended attributes changed or deleted, or have its size changed.
     @return  true if the object cannot be changed; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isPcReadOnly() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return pcReadOnly_;
    }
    /**
     Sets whether the object can be written to or deleted, have its extended attributes changed or deleted, or have its size changed.
     @param  pcReadOnly  true if the object cannot be changed; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setPcReadOnly(boolean pcReadOnly) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAttributes(pcReadOnly, (byte)17);
    }

    // Whether the object is hidden.
    boolean pcHidden_;
    /**
     Returns whether the object can be displayed using an ordinary directory listing.
     @return  true if the object is hidden; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isPcHidden() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return pcHidden_;
    }
    /**
     Sets the whether the object can be displayed using an ordinary directory listing.
     @param  pcHidden  true if the object is hidden; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setPcHidden(boolean pcHidden) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAttributes(pcHidden, (byte)18);
    }

    // Whether the object is a system file.
    boolean pcSystem_;
    /**
     Returns whether the object is a system file and is excluded from normal directory searches.
     @return  true if the object is a system file; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isPcSystem() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return pcSystem_;
    }
    /**
     Sets whether the object is a system file and is excluded from normal directory searches.
     @param  pcSystem  true if the object is a system file; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setPcSystem(boolean pcSystem) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAttributes(pcSystem, (byte)19);
    }

    // Whether the object has changed.
    boolean pcArchive_;
    /**
     Returns whether the object has changed since the last time the file was examined.
     @return  true if the object has changed; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isPcArchive() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return pcArchive_;
    }
    /**
     Sets whether the object has changed since the last time the file was saved or reset by a PC client.
     @param  pcArchive  true if the object has changed; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setPcArchive(boolean pcArchive) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAttributes(pcArchive, (byte)20);
    }

    // Whether the object has changed and needs to be saved.
    boolean systemArchive_;
    /**
     Returns whether the object has changed and needs to be saved.  It is set on when an object's change time is updated, and set off when the object has been saved.
     @return  true if the object has changed and does need to be saved; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isSystemArchive() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return systemArchive_;
    }
    /**
     Sets whether the object has changed and needs to be saved.  It is set on when an object's change time is updated, and set off when the object has been saved.
     @param  systemArchive  true if the object has changed and does need to be saved; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setSystemArchive(boolean systemArchive) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAttributes(systemArchive, (byte)21);
    }

    // The code page.
    int codePage_;
    /**
     Returns the code page derived from the coded character set identifier (CCSID) used for the data in the file or the extended attributes of the directory.  If the returned value of this field is zero (0), there is more than one code page associated with the st_ccsid.  If the st_ccsid is not a supported system CCSID, the st_codepage is set equal to the st_ccsid.
     @return  The code page derived from the coded character set identifier (CCSID) used for the data in the file or the extended attributes of the directory.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getCodePage() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return codePage_;
    }
    /**
     Sets the code page used to derive a coded character set identifier (CCSID) used for the data in the file or the extended attributes of the directory.
     @param  codePage  The code page used to derive a coded character set identifier (CCSID) used for the data in the file or the extended attributes of the directory.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setCodePage(int codePage) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAttributes(codePage, (byte)22);
    }

    /**
     Constant indicating that the file has format type 1.
     @see  #getFileFormat
     **/
    public static final int FILE_FORMAT_TYPE1 = 0x00;
    /**
     Constant indicating that the file has format type 2.
     @see  #getFileFormat
     **/
    public static final int FILE_FORMAT_TYPE2 = 0x01;
    // The format of the stream file.
    int fileFormat_;
    /**
     Returns the format of the stream file.
     @return  The format of the stream file (*STMF).  Possible values are:
     <ul>
     <li>{@link #FILE_FORMAT_TYPE1 FILE_FORMAT_TYPE1} - The object has the same format as *STMF objects created on releases prior to Version 4 Release 4.  It has a minimum object size of 4096 bytes and a maximum object size of approximately 128 gigabytes.
     <li>{@link #FILE_FORMAT_TYPE2 FILE_FORMAT_TYPE2} - The *STMF has high performance file access and was new in OS/400 V4R4.  It has a minimum object size of 4096 bytes and a maximum object size of approximately one terabyte in the "root" (/), QOpenSys and user-defined file systems.  Otherwise, the maximum is approximately 256 gigabytes.  A *TYPE2 *STMF is capable of memory mapping as well as the ability to specify an attribute to optimize disk storage allocation.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getFileFormat() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return fileFormat_;
    }

    /**
     Constant indicating that the default file format of stream files created in the user-defined file system is format type 1.
     @see  #getUdfsDefaultFormat
     **/
    public static final int UDFS_DEFAULT_TYPE1 = 0x00;
    /**
     Constant indicating that the default file format of stream files created in the user-defined file system is format type 2.
     @see  #getUdfsDefaultFormat
     **/
    public static final int UDFS_DEFAULT_TYPE2 = 0x01;
    // The default file format of stream files created in the user-defined file system.
    int udfsDefaultFormat_;
    /**
     Returns the default file format of stream files (*STMF) created in the user-defined file system.
     @return  The default file format of stream files (*STMF) created in the user-defined file system.  Possible values are:
     <ul>
     <li>{@link #UDFS_DEFAULT_TYPE1 UDFS_DEFAULT_TYPE1} - The stream file (*STMF) has the same format as *STMF's created on releases prior to OS/400 V4R4.  It has a minimum object size of 4096 bytes and a maximum object size of approximately 256 gigabytes.
     <li>{@link #UDFS_DEFAULT_TYPE2 UDFS_DEFAULT_TYPE2} - A *TYPE2 *STMF has high performance file access and was new in OS/400 V4R4.  It has a minimum object size of 4096 bytes and a maximum object size of approximately one terabyte in the "root" (/), QOpenSys and user-defined file systems.  Otherwise, the maximum is approximately 256 gigabytes.  A *TYPE2 *STMF is capable of memory mapping as well as the ability to specify an attribute to optimize disk storage allocation.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getUdfsDefaultFormat() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return udfsDefaultFormat_;
    }

    // Whether the stream file can be shared during checkpoint processing.
    boolean allowCheckpointWrite_;
    /**
     Returns whether a stream file (*STMF) can be shared with readers and writers during the save-while-active checkpoint processing.
     @return  true if the object can be shared with readers and writers; false if the object can be shared with readers only.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isAllowCheckpointWrite() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return allowCheckpointWrite_;
    }
    /**
     Sets whether a stream file (*STMF) can be shared with readers and writers during the save-while-active checkpoint processing.  Setting this attribute may cause unexpected results.  See the Back up your system topic for details on this attribute.
     @param  allowCheckpointWrite  true if the object can be shared with readers and writers; false if the object can be shared with readers only.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setAllowCheckpointWrite(boolean allowCheckpointWrite) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAttributes(allowCheckpointWrite, (byte)26);
    }

    // The CCSID of the data and extended attributes of the object.
    int ccsid_;
    /**
     Returns the CCSID of the data and extended attributes of the object.
     @return  The CCSID of the data and extended attributes of the object.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getCcsid() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return ccsid_;
    }
    /**
     Sets the CCSID of the data and extended attributes of the object.
     @param  ccsid  The CCSID of the data and extended attributes of the object.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setCcsid(int ccsid) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAttributes(ccsid, (byte)27);
    }

    // Whether the object has an IBM i digital signature.
    boolean signed_;
    /**
     Returns whether the object has an IBM i digital signature.  This attribute is only returned for *STMF objects.
     @return  true if the object does have an IBM i digital signature; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isSigned() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return signed_;
    }

    // Whether the object was signed by a source that is trusted by the system.
    boolean systemSigned_;
    /**
     Returns whether the object was signed by a source that is trusted by the system.  This attribute is only returned for *STMF objects.
     @return  true if the object was signed by a source that is trusted by the system, if the object has multiple signatures, at least one of the signatures came from a source that is trusted by the system; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isSystemSigned() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return systemSigned_;
    }

    // Whether the object has more than one IBM i digital signature.
    boolean multipleSignatures_;
    /**
     Returns whether the object has more than one IBM i digital signature.  This attribute is only returned for *STMF objects.
     @return  true if the object has more than one digital signature; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isMultipleSignatures() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return multipleSignatures_;
    }

    /**
     Constant indicating that storage will be allocated normally.
     @see  #getDiskStorageOption
     @see  #setDiskStorageOption
     @see  #getMainStorageOption
     @see  #setMainStorageOption
     **/
    public static final int STORAGE_NORMAL = 0x00;
    /**
     Constant indicating that storage will be allocated to minimize the space used by the object.
     @see  #getDiskStorageOption
     @see  #setDiskStorageOption
     @see  #getMainStorageOption
     @see  #setMainStorageOption
     **/
    public static final int STORAGE_MINIMIZE = 0x01;
    /**
     Constant indicating that the system will dynamically determine the optimum storage allocation for the object.
     @see  #getDiskStorageOption
     @see  #setDiskStorageOption
     @see  #getMainStorageOption
     @see  #setMainStorageOption
     **/
    public static final int STORAGE_DYNAMIC = 0x02;
    // How auxiliary storage is allocated by the system.
    int diskStorageOption_;
    /**
     Returns how auxiliary storage is allocated by the system for the specified object.  This option can only be specified for stream files in the "root" (/), QOpenSys and user-defined file systems.  This option will be ignored for *TYPE1 byte stream files.
     @return  How auxiliary storage is allocated by the system for the specified object.  Possible values are:
     <ul>
     <li>{@link #STORAGE_NORMAL STORAGE_NORMAL} - The auxiliary storage will be allocated normally.  That is, as additional auxiliary storage is required, it will be allocated in logically sized extents to accommodate the current space requirement, and anticipated future requirements, while minimizing the number of disk I/O operations.
     <li>{@link #STORAGE_MINIMIZE STORAGE_MINIMIZE} - The auxiliary storage will be allocated to minimize the space used by the object.  That is, as additional auxiliary storage is required, it will be allocated in small sized extents to accommodate the current space requirement.  Accessing an object composed of many small extents may increase the number of disk I/O operations for that object.
     <li>{@link #STORAGE_DYNAMIC STORAGE_DYNAMIC} - The system will dynamically determine the optimum auxiliary storage allocation for the object, balancing space used versus disk I/O operations.  For example, if a file has many small extents, yet is frequently being read and written, then future auxiliary storage allocations will be larger extents to minimize the number of disk I/O operations.  Or, if a file is frequently truncated, then future auxiliary storage allocations will be small extents to minimize the space used.  Additionally, information will be maintained on the stream file sizes for this system and its activity.  This file size information will also be used to help determine the optimum auxiliary storage allocations for this object as it relates to the other objects sizes.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getDiskStorageOption() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return diskStorageOption_;
    }
    /**
     Sets which option should be used to determine how auxiliary storage is allocated by the system for the specified object.  The option will take effect immediately and be part of the next auxiliary storage allocation for the object.  This option can only be specified for byte stream files in the "root" (/), QOpenSys and user-defined file systems.  This option will be ignored for *TYPE1 byte stream files.
     @param  diskStorageOption  The option which should be used to determine how auxiliary storage is allocated by the system for the specified object.  Possible values are:
     <ul>
     <li>{@link #STORAGE_NORMAL STORAGE_NORMAL} - The auxiliary storage will be allocated normally.  That is, as additional auxiliary storage is required, it will be allocated in logically sized extents to accommodate the current space requirement, and anticipated future requirements, while minimizing the number of disk I/O operations.  This option is the default.
     <li>{@link #STORAGE_MINIMIZE STORAGE_MINIMIZE} - The auxiliary storage will be allocated to minimize the space used by the object.  That is, as additional auxiliary storage is required, it will be allocated in small sized extents to accommodate the current space requirement.  Accessing an object composed of many small extents may increase the number of disk I/O operations for that object.
     <li>{@link #STORAGE_DYNAMIC STORAGE_DYNAMIC} - The system will dynamically determine the optimum auxiliary storage allocation for the object, balancing space used versus disk I/O operations.  For example, if a file has many small extents, yet is frequently being read and written, then future auxiliary storage allocations will be larger extents to minimize the number of disk I/O operations.  Or, if a file is frequently truncated, then future auxiliary storage allocations will be small extents to minimize the space used.  Additionally, information will be maintained on the stream file sizes for this system and its activity.  This file size information will also be used to help determine the optimum auxiliary storage allocations for this object as it relates to the other objects sizes.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setDiskStorageOption(int diskStorageOption) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAttributes((byte)diskStorageOption, (byte)31);
    }

    // How main storage is allocated by the system.
    int mainStorageOption_;
     /**
     Returns how main storage is allocated and used by the system for the specified object.  This option can only be specified for stream files in the "root" (/), QOpenSys and user-defined file systems.
     @return  How main storage is allocated and used by the system for the specified object.  Possible values are:
     <ul>
     <li>{@link #STORAGE_NORMAL STORAGE_NORMAL} - The main storage will be allocated normally.  That is, as much main storage as possible will be allocated and used.  This minimizes the number of disk I/O operations since the information is cached in main storage.
     <li>{@link #STORAGE_MINIMIZE STORAGE_MINIMIZE} - The main storage will be allocated to minimize the space used by the object.  That is, as little main storage as possible will be allocated and used.  This minimizes main storage usage while increasing the number of disk I/O operations since less information is cached in main storage.
     <li>{@link #STORAGE_DYNAMIC STORAGE_DYNAMIC} - The system will dynamically determine the optimum main storage allocation for the object depending on other system activity and main storage contention.  That is, when there is little main storage contention, as much storage as possible will be allocated and used to minimize the number of disk I/O operations.  And when there is significant main storage contention, less main storage will be allocated and used to minimize the main storage contention.  This option only has an effect when the storage pool's paging option is *CALC.  When the storage pool's paging option is *FIXED, the behavior is the same as STORAGE_NORMAL.  When the object is accessed through a file server, this option has no effect.  Instead, its behavior is the same as STORAGE_NORMAL.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
   public int getMainStorageOption() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return mainStorageOption_;
    }
    /**
     Sets which option should be used to determine how main storage is allocated and used by the system for the specified object.  The option will take effect the next time the specified object is opened.  This option can only be specified for byte stream files in the "root" (/), QOpenSys and user-defined file systems.
     @param  mainStorageOption  The option which should be used to determine how main storage is allocated and used by the system for the specified object.  Possible values are:
     <ul>
     <li>{@link #STORAGE_NORMAL STORAGE_NORMAL} - The main storage will be allocated normally.  That is, as much main storage as possible will be allocated and used.  This minimizes the number of disk I/O operations since the information is cached in main storage.  This option is the default.
     <li>{@link #STORAGE_MINIMIZE STORAGE_MINIMIZE} - The main storage will be allocated to minimize the space used by the object.  That is, as little main storage as possible will be allocated and used.  This minimizes main storage usage while increasing the number of disk I/O operations since less information is cached in main storage.
     <li>{@link #STORAGE_DYNAMIC STORAGE_DYNAMIC} - The system will dynamically determine the optimum main storage allocation for the object depending on other system activity and main storage contention.  That is, when there is little main storage contention, as much storage as possible will be allocated and used to minimize the number of disk I/O operations.  And when there is significant main storage contention, less main storage will be allocated and used to minimize the main storage contention.  This option only has an effect when the storage pool's paging option is *CALC.  When the storage pool's paging option is *FIXED, the behavior is the same as *NORMAL.  When the object is accessed through a file server, this option has no effect.  Instead, its behavior is the same as *NORMAL.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setMainStorageOption(int mainStorageOption) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAttributes((byte)mainStorageOption, (byte)32);
    }

    /**
     Constant indicating that the directory has format type 1.
     @see  #getDirectoryFormat
     **/
    public static final int DIRECTORY_FORMAT_TYPE1 = 0x00;
    /**
     Constant indicating that the directory has format type 2.
     @see  #getDirectoryFormat
     **/
    public static final int DIRECTORY_FORMAT_TYPE2 = 0x01;
    // The format of the directory.
    int directoryFormat_;
    /**
     Returns the format of the specified directory object.
     @return  The format of the specified directory object.  Possible values are:
     <ul>
     <li>{@link #DIRECTORY_FORMAT_TYPE1 DIRECTORY_FORMAT_TYPE1} - The directory of type *DIR has the original directory format.  The Convert Directory (CVTDIR) command may be used to convert from the *TYPE1 format to the *TYPE2 format.
     <li>{@link #DIRECTORY_FORMAT_TYPE2 DIRECTORY_FORMAT_TYPE2} - The directory of type *DIR is optimized for performance, size, and reliability compared to directories having the *TYPE1 format.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getDirectoryFormat() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return directoryFormat_;
    }

    // The auditing value associated with the object.
    String audit_;
    /**
     Returns the auditing value associated with the object.  Note: The user must have all object (*ALLOBJ) or audit (*AUDIT) special authority to retrieve the auditing value.
     @return  The auditing value associated with the object.  Possible values are:
     <ul>
     <li>"*NONE" - No auditing occurs for this object when it is read or changed regardless of the user who is accessing the object.
     <li>"*USRPRF" - Audit this object only if the current user is being audited.  The current user is tested to determine if auditing should be done for this object.  The user profile can specify if only change access is audited or if both read and change accesses are audited for this object.  The OBJAUD parameter of the Change User Auditing (CHGUSRAUD) command is used to change the auditing for a specific user.
     <li>"*CHANGE" - Audit all change access to this object by all users on the system.
     <li>"*ALL" - Audit all access to this object by all users on the system.  All access is defined as a read or change operation.
     <li>"*NOTAVL" - The user performing the operation is not allowed to retrieve the current auditing value.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getAudit() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return audit_;
    }

    /**
     Constant indicating that the object will not be scanned.
     @see  #getCreateObjectScan
     @see  #setCreateObjectScan
     @see  #getScan
     @see  #setScan
     **/
    public static final int SCANNING_NO = 0x00;
    /**
     Constant indicating that the object will be scanned.
     @see  #getCreateObjectScan
     @see  #setCreateObjectScan
     @see  #getScan
     @see  #setScan
     **/
    public static final int SCANNING_YES = 0x01;
    /**
     Constant indicating that the object will be scanned only if the object has been modified since the last time the object was scanned.
     @see  #getCreateObjectScan
     @see  #setCreateObjectScan
     @see  #getScan
     @see  #setScan
     **/
    public static final int SCANNING_CHANGE_ONLY = 0x02;
    // Whether the objects created in a directory will be scanned.
    int createObjectScan_;
    /**
     Returns whether the objects created in a directory will be scanned when exit programs are registered with any of the integrated file system scan-related exit points.  This attribute can only have been specified for directories in the "root" (/), QOpenSys and user-defined file systems.  Even though this attribute can be set for *TYPE1 and *TYPE2 directories, only objects which are in file systems that have completely converted to the *TYPE2 directory format will actually be scanned, no matter what value is set for this attribute.
     @return  Whether the objects created in a directory will be scanned when exit programs are registered with any of the integrated file system scan-related exit points.  Possible values are:
     <ul>
     <li>{@link #SCANNING_NO SCANNING_NO} - After an object is created in the directory, the object will not be scanned according to the rules described in the scan-related exit programs.  Note: If the Scan file systems control (QSCANFSCTL) value *NOPOSTRST is not specified when an object with this attribute is restored, the object will be scanned at least once after the restore.
     <li>{@link #SCANNING_YES SCANNING_YES} - After an object is created in the directory, the object will be scanned according to the rules described in the scan-related exit programs if the object has been modified or if the scanning software has been updated since the last time the object was scanned.
     <li>{@link #SCANNING_CHANGE_ONLY SCANNING_CHANGE_ONLY} - After an object is created in the directory, the object will be scanned according to the rules described in the scan-related exit programs only if the object has been modified since the last time the object was scanned.  It will not be scanned if the scanning software has been updated.  This attribute only takes effect if the Scan file systems control (QSCANFSCTL) system value has *USEOCOATR specified.  Otherwise, it will be treated as if the attribute is SCANNING_YES.  Note: If the Scan file systems control (QSCANFSCTL) value *NOPOSTRST is not specified when an object with this attribute is restored, the object will be scanned at least once after the restore.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getCreateObjectScan() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return createObjectScan_;
    }
    /**
     Sets whether the objects created in a directory will be scanned when exit programs are registered with any of the integrated file system scan-related exit points.  This attribute can only be specified for directories in the "root" (/), QOpenSys and user-defined file systems.  Even though this attribute can be set for *TYPE1 and *TYPE2 directories, only objects which are in *TYPE2 directories will actually be scanned, no matter what value is set for this attribute.
     @param  createObjectScan  Whether the objects created in a directory will be scanned when exit programs are registered with any of the integrated file system scan-related exit points.  Possible values are:
     <ul>
     <li>{@link #SCANNING_NO SCANNING_NO} - After an object is created in the directory, the object will not be scanned according to the rules described in the scan-related exit programs.  Note: If the Scan file systems control (QSCANFSCTL) value *NOPOSTRST is not specified when an object with this attribute is restored, the object will be scanned at least once after the restore.
     <li>{@link #SCANNING_YES SCANNING_YES} - After an object is created in the directory, the object will be scanned according to the rules described in the scan-related exit programs if the object has been modified or if the scanning software has been updated since the last time the object was scanned.  This value is the default.
     <li>{@link #SCANNING_CHANGE_ONLY SCANNING_CHANGE_ONLY} - After an object is created in the directory, the object will be scanned according to the rules described in the scan-related exit programs only if the object has been modified since the last time the object was scanned.  It will not be scanned if the scanning software has been updated.  This attribute only takes effect if the Scan file systems control (QSCANFSCTL) system value has *USEOCOATR specified.  Otherwise, it will be treated as if the attribute is SCANNING_YES.  Note: If the Scan file systems control (QSCANFSCTL) value *NOPOSTRST is not specified when an object with this attribute is restored, the object will be scanned at least once after the restore.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setCreateObjectScan(int createObjectScan) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAttributes((byte)createObjectScan, (byte)35);
    }

    // Whether the object will be scanned.
    int scan_;
    /**
     Returns whether the object will be scanned when exit programs are registered with any of the integrated file system scan-related exit points.  This attribute can only have been specified for stream files in the "root" (/), QOpenSys and user-defined file systems.  Even though this attribute can be set for objects in *TYPE1 and *TYPE2 directories, only objects which are in file systems that have completely converted to the *TYPE2 directory format will actually be scanned, no matter what value is set for this attribute.
     @return  Whether the object will be scanned when exit programs are registered with any of the integrated file system scan-related exit points.  Possible values are:
     <ul>
     <li>{@link #SCANNING_NO SCANNING_NO} - The object will not be scanned according to the rules described in the scan-related exit programs.  Note: If the Scan file systems control (QSCANFSCTL) value *NOPOSTRST is not specified when an object with this attribute is restored, the object will be scanned at least once after the restore.
     <li>{@link #SCANNING_YES SCANNING_YES} - The object will be scanned according to the rules described in the scan-related exit programs if the object has been modified or if the scanning software has been updated since the last time the object was scanned.
     <li>{@link #SCANNING_CHANGE_ONLY SCANNING_CHANGE_ONLY} - The object will be scanned according to the rules described in the scan-related exit programs only if the object has been modified since the last time the object was scanned.  It will not be scanned if the scanning software has been updated.  This attribute only takes effect if the Scan file systems control (QSCANFSCTL) system value has *USEOCOATR specified.  Otherwise, it will be treated as if the attribute is SCANNING_YES.  Note: If the Scan file systems control (QSCANFSCTL) value *NOPOSTRST is not specified when an object with this attribute is restored, the object will be scanned at least once after the restore.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getScan() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return scan_;
    }
    /**
     Sets the whether the object will be scanned when exit programs are registered with any of the integrated file system scan-related exit points.  This attribute can only be specified for stream files in the "root" (/), QOpenSys and user-defined file systems  that are not virtual volumes or network server storage spaces.  Even though this attribute can be set for objects in *TYPE1 and *TYPE2 directories, only objects which are in *TYPE2 directories will actually be scanned, no matter what value is set for this attribute.
     @param  scan  Whether the object will be scanned when exit programs are registered with any of the integrated file system scan-related exit points.  Possible values are:
     <ul>
     <li>{@link #SCANNING_NO SCANNING_NO} - The object will not be scanned according to the rules described in the scan-related exit programs.  Note: If the Scan file systems control (QSCANFSCTL) value *NOPOSTRST is not specified when an object with this attribute is restored, the object will be scanned at least once after the restore.
     <li>{@link #SCANNING_YES SCANNING_YES} - The object will be scanned according to the rules described in the scan-related exit programs if the object has been modified or if the scanning software has been updated since the last time the object was scanned.  This value is the default.
     <li>{@link #SCANNING_CHANGE_ONLY SCANNING_CHANGE_ONLY} - The object will be scanned according to the rules described in the scan-related exit programs only if the object has been modified since the last time the object was scanned.  It will not be scanned if the scanning software has been updated.  This attribute only takes effect if the Scan file systems control (QSCANFSCTL) system value has *USEOCOATR specified.  Otherwise, it will be treated as if the attribute is SCANNING_YES.  Note: If the Scan file systems control (QSCANFSCTL) value *NOPOSTRST is not specified when an object with this attribute is restored, the object will be scanned at least once after the restore.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setScan(int scan) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAttributes((byte)scan, (byte)36);
    }

    /**
     Constant indicating that a scan is required for the object either because it has not yet been scanned by the scan-related exit programs, or because the objects data or CCSID has been modified since it was last scanned.  Examples of object data or CCSID modifications are: writing to the object, directly or through memory mapping; truncating the object; clearing the object; and changing the objects CCSID attribute etc..
     @see  #getScanStatus
     **/
    public static final int SCAN_REQUIRED = 0x00;
    /**
     Constant indicating that the object has been scanned by a scan-related exit program, and at the time of that last scan request, the object did not fail the scan.
     @see  #getScanStatus
     **/
    public static final int SCAN_SUCCESS = 0x01;
    /**
     Constant indicating that the object has been scanned by a scan-related exit program, and at the time of that last scan request, the object failed the scan and the operation did not complete.  Once an object has been marked as a failure, it will not be scanned again until the object's scan signature is different than the global scan key signature or independent ASP group scan key signature as appropriate.  Therefore, subsequent requests to work with the object will fail with a scan failure indication if that access meets the criteria for when an object is to be scanned.  Examples of requests which will fail are opening the object with more than write-only access, changing the CCSID of the object, copying the object etc..  See Integrated File System Scan on Open Exit Programs and Integrated File System Scan on Close Exit Programs for the criteria for when an object is to be scanned.  Note: 1. If scanning has been turned off using the QSCANFS system value, or if no exit programs are registered for a specific exit point, then any requests which trigger that specific exit point will return a scan failure indication.  2. If the scan attribute is set to not scan the object, then requests to work with the object will not fail with a scan failure indication.
     @see  #getScanStatus
     **/
    public static final int SCAN_FAILURE = 0x02;
    /**
     Constant indicating that the object is in a file system that has not completely converted to the *TYPE2 directory format, and therefore will not be scanned until the file system is completely converted.  For information on the *TYPE2 directory format, see the Convert Directory (CVTDIR) command and the Integrated file system information in the Files and file systems topic.
     @see  #getScanStatus
     **/
    public static final int SCAN_PENDING_CONVERSION = 0x05;
    /**
     Constant indicating that the object does not require any scanning because the object is marked to not be scanned.
     @see  #getScanStatus
     **/
    public static final int SCAN_NOT_REQUIRED = 0x06;
    // The scan status associated with this object.
    int scanStatus_;
    /**
     Returns the scan status associated with this object.
     @return  The scan status associated with this object.  Possible values are:
     <ul>
     <li>{@link #SCAN_REQUIRED SCAN_REQUIRED} - A scan is required for the object either because it has not yet been scanned by the scan-related exit programs, or because the objects data or CCSID has been modified since it was last scanned.  Examples of object data or CCSID modifications are: writing to the object, directly or through memory mapping; truncating the object; clearing the object; and changing the objects CCSID attribute etc..
     <li>{@link #SCAN_SUCCESS SCAN_SUCCESS} - The object has been scanned by a scan-related exit program, and at the time of that last scan request, the object did not fail the scan.
     <li>{@link #SCAN_FAILURE SCAN_FAILURE} - The object has been scanned by a scan-related exit program, and at the time of that last scan request, the object failed the scan and the operation did not complete.  Once an object has been marked as a failure, it will not be scanned again until the object's scan signature is different than the global scan key signature or independent ASP group scan key signature as appropriate.  Therefore, subsequent requests to work with the object will fail with a scan failure indication if that access meets the criteria for when an object is to be scanned.  Examples of requests which will fail are opening the object with more than write-only access, changing the CCSID of the object, copying the object etc..  See Integrated File System Scan on Open Exit Programs and Integrated File System Scan on Close Exit Programs for the criteria for when an object is to be scanned.  Note: 1. If scanning has been turned off using the QSCANFS system value, or if no exit programs are registered for a specific exit point, then any requests which trigger that specific exit point will return a scan failure indication.  2. If the scan attribute is set to not scan the object, then requests to work with the object will not fail with a scan failure indication.
     <li>{@link #SCAN_PENDING_CONVERSION SCAN_PENDING_CONVERSION} - The object is in a file system that has not completely converted to the *TYPE2 directory format, and therefore will not be scanned until the file system is completely converted.  For information on the *TYPE2 directory format, see the Convert Directory (CVTDIR) command and the Integrated file system information in the Files and file systems topic.
     <li>{@link #SCAN_NOT_REQUIRED SCAN_NOT_REQUIRED} - The object does not require any scanning because the object is marked to not be scanned.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getScanStatus() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return scanStatus_;
    }
    // Whether the scan signatures are different.
    boolean scanSignaturesDifferent_;
    /**
     Returns whether the scan signatures are different.  The scan signatures give an indication of the level of the scanning software support.  For more information, see Scan Key List and Scan Key Signatures in Integrated File System Scan on Open Exit Program.  When an object is in an independent ASP group, the object scan signature is compared to the associated independent ASP group scan signature.  When an object is not in an independent ASP group, the object scan signature is compared to the global scan signature value.
     @return  true if the compared signatures are different; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isScanSignaturesDifferent() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return scanSignaturesDifferent_;
    }
    // Whether the object has been scanned in binary mode.
    boolean binaryScan_;
    /**
     Returns whether the object has been scanned in binary mode when it was previously scanned.
     @return  true if the object was scanned in binary mode; false otherwise.  If the object scan status is SCAN_SUCCESS, then the object was successfully scanned in binary.  If the object scan status is SCAN_FAILURE, then the object failed the scan in binary.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isBinaryScan() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return binaryScan_;
    }
    // The first CCSID value that the object has been scanned in.
    int scanCcsid1_;
    /**
     Returns the CCSID value that the object has been scanned in if it was previously scanned in a CCSID.  If the object scan status is SCAN_SUCCESS, then the object was successfully scanned in this CCSID.  If the object scan status is SCAN_FAILURE, then the object failed the scan in this CCSID. A value of 0 means this field does not apply.
     @return  The CCSID value that the object has been scanned in if it was previously scanned in a CCSID.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getScanCcsid1() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return scanCcsid1_;
    }
    // The second CCSID value that the object has been scanned in.
    int scanCcsid2_;
    /**
     Returns the CCSID value that the object has been scanned in if it was previously scanned in a CCSID.  If the object scan status is SCAN_SUCCESS, then the object was successfully scanned in this CCSID.  If the object scan status is SCAN_FAILURE, then this field will be 0.  A value of 0 means this field does not apply.
     @return  The CCSID value that the object has been scanned in if it was previously scanned in a CCSID.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getScanCcsid2() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return scanCcsid2_;
    }

    // Whether the object can be saved or not.
    boolean allowSave_;
    /**
     Returns whether the object can be saved or not.
     @return  true if the object will be saved when using the Save Object (SAV) command or the QsrSave() API.  false if the object will not be saved when using the Save Object (SAV) command or the QsrSave() API.  Additionally, if this object is a directory, none of the objects in the directory's subtree will be saved unless they were explicitly specified as an object to be saved.  The subtree includes all subdirectories and the objects within those subdirectories.  Note: If this attribute is chosen for an object that has private authorities associated with it, or is chosen for the directory of an object that has private authorities associated with it, then the following consideration applies.  When the private authorities are saved, the fact that this attribute is false is not taken into consideration.  (Private authorities can be saved using either the Save System (SAVSYS) or Save Security Data (SAVSECDTA) command or the Save Object List (QSRSAVO) API.)  Therefore, when a private authority is restored using the Restore Authority (RSTAUT) command, message CPD3776 will be seen for each object that was not saved either because it had this attribute specified as false, or because the object was not specified on the save and it was in a directory that had this attribute specified as false.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isAllowSave() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return allowSave_;
    }
    /**
     Sets whether the object can be saved or not.  Note: It is highly recommended that this attribute not be changed for any system created objects.
     @param  allowSave  true if the object will be saved when using the Save Object (SAV) command or the QsrSave() API.  This value is the default.  false if the object will not be saved when using the Save Object (SAV) command or the QsrSave() API.  Additionally, if this object is a directory, none of the objects in the directory's subtree will be saved unless they were explicitly specified as an object to be saved.  The subtree includes all subdirectories and the objects within those subdirectories.  Note: If this attribute is chosen for an object that has private authorities associated with it, or is chosen for the directory of an object that has private authorities associated with it, then the following consideration applies.  When the private authorities are saved, the fact that this attribute is false is not taken into consideration.  (Private authorities can be saved using either the Save System (SAVSYS) or Save Security Data (SAVSECDTA) command or the Save Object List (QSRSAVO) API.)  Therefore, when a private authority is restored using the Restore Authority (RSTAUT) command, message CPD3776 will be seen for each object that was not saved either because it had this attribute specified as false, or because the object was not specified on the save and it was in a directory that had this attribute specified as false.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setAllowSave(boolean allowSave) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAttributes(allowSave, (byte)38);
    }

    // Whether renames and unlinks are restricted.
    boolean restrictedRenameAndUnlink_;
    /**
     Returns whether renames and unlinks are restricted for objects within a directory.  Objects can be linked into a directory that has this attribute set on, but cannot be renamed or unlinked from it unless one or more of the following are true for the user performing the operation:
     <ul>
     <li>The user is the owner of the object.
     <li>The user is the owner of the directory.
     <li>The user has *ALLOBJ special authority.
     </ul>
     This restriction only applies to directories.  Other types of object can have this attribute on, however, it will be ignored.  This attribute is equivalent to the S_ISVTX mode bit for an object.
     @return  true if the additional restrictions for rename and unlink operations are on; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isRestrictedRenameAndUnlink() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return restrictedRenameAndUnlink_;
    }
    /**
     Sets whether renames and unlinks are restricted for objects within a directory.  Objects can be linked into a directory that has this attribute set on, but cannot be renamed or unlinked from it unless one or more of the following are true for the user performing the operation:
     <ul>
     <li>The user is the owner of the object.
     <li>The user is the owner of the directory.
     <li>The user has *ALLOBJ special authority.
     </ul>
     This restriction only applies to directories.  Other types of object can have this attribute on, however, it will be ignored.  In addition, this attribute can only be specified for objects within the Network File System (NFS), QFileSvr.400, "root" (/), QOpenSys, or user-defined file systems.  Both the NFS and QFileSvr.400 file systems support this attribute by passing it to the system and surfacing it to the caller.  This attribute is also equivalent to the S_ISVTX mode bit for an object.
     @param  restrictedRenameAndUnlink  true if the additional restrictions for rename and unlink operations are on; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setRestrictedRenameAndUnlink(boolean restrictedRenameAndUnlink) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAttributes(restrictedRenameAndUnlink, (byte)39);
    }

    // Whether the object is currently being journaled.
    boolean journalingStatus_;
    /**
     Returns whether the object is currently being journaled.
     @return  true if the object is currently being journaled; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isJournalingStatus() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return journalingStatus_;
    }
    /**
     Constant indicating that this object is a directory with IFS journaling subtree semantics.  New objects created within this directory's subtree will inherit the journaling attributes and options from this directory.
     @see  #getJournalingOptions
     **/
    public static final int JOURNAL_SUBTREE = 0x80;
    /**
     Constant indicating that when journaling is active, entries that are considered optional are journaled.  The list of optional journal entries varies for each object type.  See the Integrated file system information in the Files and file systems topic for information regarding these optional entries for various objects.
     @see  #getJournalingOptions
     **/
    public static final int JOURNAL_OPTIONAL_ENTRIES = 0x08;
    /**
     Constant indicating that when journaling is active, the image of the object after a change is journaled.
     @see  #getJournalingOptions
     **/
    public static final int JOURNAL_AFTER_IMAGES = 0x20;
    /**
     Constant indicating that when journaling is active, the image of the object prior to a change is journaled.
     @see  #getJournalingOptions
     **/
    public static final int JOURNAL_BEFORE_IMAGES = 0x40;
    // The current journaling options.
    int journalingOptions_;
    /**
     Returns the current journaling options.
     @return  The current journaling options.  This field is composed of several bit flags and contains one or more of the following bit values:
     <ul>
     <li>{@link #JOURNAL_SUBTREE JOURNAL_SUBTREE} - When this flag is returned, this object is a directory with IFS journaling subtree semantics.  New objects created within this directory's subtree will inherit the journaling attributes and options from this directory.
     <li>{@link #JOURNAL_OPTIONAL_ENTRIES JOURNAL_OPTIONAL_ENTRIES} - When journaling is active, entries that are considered optional are journaled.  The list of optional journal entries varies for each object type.  See the Integrated file system information in the Files and file systems topic for information regarding these optional entries for various objects.
     <li>{@link #JOURNAL_AFTER_IMAGES JOURNAL_AFTER_IMAGES} - When journaling is active, the image of the object after a change is journaled.
     <li>{@link #JOURNAL_BEFORE_IMAGES JOURNAL_BEFORE_IMAGES} - When journaling is active, the image of the object prior to a change is journaled.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getJournalingOptions() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return journalingOptions_;
    }
    // The journal identifier (JID).
    byte[] journalIdentifier_;
    /**
     Returns the field that associates the object being journaled with an identifier that can be used on various journaling-related commands and APIs.
     @return  The field that associates the object being journaled with an identifier that can be used on various journaling-related commands and APIs.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public byte[] getJournalIdentifier() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return journalIdentifier_;
    }
    // The current or last journal.
    String journal_;
    /**
     Returns the fully qualified integrated file system path name of the current or last journal.  If the value of isJournalingStatus() is true, then this field contains the name of the journal currently being used. If the value of isJournalingStatus() is false, then this field contains the name of the journal last used for this object. If this object has never been journaled, null is returned.
     @return  The fully qualified integrated file system path name of the current or last journal.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getJournal() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return journal_;
    }
    // The last date and time for which the object had journaling started for it.
    Date lastJounalingStartTime_;
    /**
     Returns the last date and time for which the object had journaling started for it.  If this object has never been journaled, null is returned.
     @return  The last date and time for which the object had journaling started for it.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public Date getJounalingStartTime() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return lastJounalingStartTime_;
    }
    // The starting journal receiver for apply.
    String startingJournalReceiverForApply_;
    /**
     Returns the fully qualified integrated file system path name of the starting journal receiver for apply.  The oldest journal receiver needed to successfully Apply Journaled Changes (APYJRNCHG).  When getApplyJournaledChangesRequired() is true the journal receiver contains the journal entries representing the start of the partial transaction.  Otherwise; the journal receiver contains the journal entries representing the start-of-the-save operation.  If no information is available, null is returned.
     @return  The fully qualified integrated file system path name of the starting journal receiver for apply.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getStartingJournalReceiverForApply() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return startingJournalReceiverForApply_;
    }
    // The name of the ASP for the library that contains the starting journal receiver.
    String startingJournalReceiverAspDevice_;
    /**
     Returns the name of the ASP for the library that contains the starting journal receiver.  The special value "*SYSBAS" means the journal receiver library resides in the system or user ASP's.
     @return  The name of the ASP for the library that contains the starting journal receiver.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getStartingJournalReceiverAspDevice() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return startingJournalReceiverAspDevice_;
    }
    // Whether the object was restored with partial transactions.
    boolean applyJournaledChangesRequired_;
    /**
     Returns whether the object was restored with partial transactions which would require an Apply Journaled Changes (APYJRNCHG) command to complete the transaction.  A partial transaction can occur if an object was saved using save-while-active requesting that transactions with pending record changes do not have to reach a commit boundary before the object is saved.
     @return  true if the object was restored with partial transactions. This object can not be used until the Apply Journaled Changes (APYJRNCHG) or Remove Journaled Changes (RMVJRNCHG) command is used to complete or rollback the partial transactions.  false if the object does not have partial transactions.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isApplyJournaledChangesRequired() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return applyJournaledChangesRequired_;
    }
    // Whether the rollback was ended.
    boolean rollbackWasEnded_;
    /**
     Returns whether the object had rollback ended prior to completion of a request to roll back a transaction.
     @return  true if the object had a rollback operation ended using the "End Rollback" option on the Work with Commitment Definition (WRKCMTDFN) screen.  It is recommended that the object be restored as it can not be used.  As a last resort, the Change Journaled Object (CHGJRNOBJ) command can be used to allow the object to be used.  Doing this, however, may leave the object in an inconsistent state.  false if the object did not have a rollback operation ended prior to completion of a request to roll back a transaction.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isRollbackWasEnded() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return rollbackWasEnded_;
    }

    // The create object auditing value associated with the directory.
    String createObjectAuditing_;
    /**
     Returns the create object auditing value associated with the directory.  This is the auditing value given to any objects created in the directory.  Note: The user must have all object (*ALLOBJ) or audit (*AUDIT) special authority to retrieve the create object auditing value.
     @return  The create object auditing value associated with the directory.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The object auditing value for the objects created in the directory is determined by the system auditing value (QCRTOBJAUD).
     <li>"*NONE" - No auditing occurs for this object when it is read or changed regardless of the user who is accessing the object.
     <li>"*USRPRF" - Audit this object only if the current user is being audited.  The current user is tested to determine if auditing should be done for this object.  The user profile can specify if only change access is audited or if both read and change accesses are audited for this object.  The OBJAUD parameter of the Change User Auditing (CHGUSRAUD) command is used to change the auditing for a specific user.
     <li>"*CHANGE" - Audit all change access to this object by all users on the system.
     <li>"*ALL" - Audit all access to this object by all users on the system.  All access is defined as a read or change operation.
     <li>"*NOTAVL" - The user performing the operation is not allowed to retrieve the current create object auditing value.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getCreateObjectAuditing() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return createObjectAuditing_;
    }
    /**
     Sets the create object auditing value associated with the directory.  This is the auditing value given to any objects created in the directory.  This attribute can only be specified for directories in the "root" (/), QOpenSys, QSYS.LIB, independent ASP QSYS.LIB, QFileSvr.400 and user-defined file systems.
     @param  createObjectAuditing  The create object auditing value associated with the directory.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The object auditing value for the objects created in the directory is determined by the system auditing value (QCRTOBJAUD).
     <li>"*NONE" - No auditing occurs for this object when it is read or changed regardless of the user who is accessing the object.
     <li>"*USRPRF" - Audit this object only if the current user is being audited.  The current user is tested to determine if auditing should be done for this object.  The user profile can specify if only change access is audited or if both read and change accesses are audited for this object.  The OBJAUD parameter of the Change User Auditing (CHGUSRAUD) command is used to change the auditing for a specific user.
     <li>"*CHANGE" - Audit all change access to this object by all users on the system.
     <li>"*ALL" - Audit all access to this object by all users on the system.  All access is defined as a read or change operation.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setCreateObjectAuditing(String createObjectAuditing) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (createObjectAuditing == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'createObjectAuditing' is null.");
            throw new NullPointerException("createObjectAuditing");
        }
        setAttributes(createObjectAuditing, (byte)41);
    }

    /**
     Constant indicating that the file is a generic stream file.
     @see  #getSystemUse
     **/
    public static final int SYSTEM_USE_NONE = 0x00;
    /**
     Constant indicating that the file is a virtual volume.  Examples include tape and optical virtual volumes.
     @see  #getSystemUse
     **/
    public static final int SYSTEM_USE_VIRTUAL_VOLUME = 0x01;
    /**
     Constant indicating that the file is a network server storage space.
     @see  #getSystemUse
     **/
    public static final int SYSTEM_USE_NETWORK_SERVER_STORAGE = 0x02;
    // Whether the file has a special use by the system.
    int systemUse_;
    /**
     Returns whether the file has a special use by the system.  This attribute is valid only for stream files.
     <ul>
     <li>{@link #SYSTEM_USE_NONE SYSTEM_USE_NONE} - The file is a generic stream file.
     <li>{@link #SYSTEM_USE_VIRTUAL_VOLUME SYSTEM_USE_VIRTUAL_VOLUME} - The file is a virtual volume.  Examples include tape and optical virtual volumes.
     <li>{@link #SYSTEM_USE_NETWORK_SERVER_STORAGE SYSTEM_USE_NETWORK_SERVER_STORAGE} - The file is a network server storage space.
     </ul>
     @return  Whether the file has a special use by the system.  Possible values are:
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getSystemUse() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return systemUse_;
    }

    /**
     Resets the count of the number of days an object has been used.  Usage has different meanings according to the file system and according to the individual object types supported within a file system.  Usage can indicate the opening or closing of a file or can refer to adding links, renaming, restoring, or checking out an object.  When this attribute is set, the date use count reset for the object is set to the current date.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void resetDate() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        byte[] attribute = new byte[18];
        attribute[7] = (byte)200;
        attribute[11] = 2;
        setAttributes(attribute);
    }

    // Whether the effective user ID (UID) is set at execution time.
    boolean setEffectiveUserId_;
    /**
     Returns whether the effective user ID (UID) is set at execution time.  This value is ignored if the specified object is a directory.
     @return  true if the object owner is the effective user ID (UID) at execution time; false if the user ID (UID) is not set at execution time.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isSetEffectiveUserId() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return setEffectiveUserId_;
    }
    /**
     Sets whether the effective user ID (UID) is set at execution time.  This value is ignored if the specified object is a directory.
     @param  setEffectiveUserId  true if the object owner is the effective user ID (UID) at execution time; false if the user ID (UID) is not set at execution time.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setSetEffectiveUserId(boolean setEffectiveUserId) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAttributes(setEffectiveUserId, (byte)0x01, (byte)0x2C);
    }

    // Whether the effective group ID (GID) is set at execution time.
    boolean setEffectiveGroupId_;
    /**
     Returns whether the effective group ID (GID) is set at execution time.
     @return  true if the object is a file, the group ID (GID) is set at execution time.  If the object is a directory, the group ID (GID) of objects created in the directory is set to the GID of the parent directory.  false if the object is a file, the group ID (GID) is not set at execution time.  If the object is a directory in the "root" (/), QOpenSys, and user-defined file systems, the group ID (GID) of objects created in the directory is set to the effective GID of the thread creating the object.  This value cannot be set for other file systems.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isSetEffectiveGroupId() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return setEffectiveGroupId_;
    }
    /**
     Sets whether the effective group ID (GID) is set at execution time.
     @param  setEffectiveGroupId  true if the object is a file, the group ID (GID) is set at execution time.  If the object is a directory, the group ID (GID) of objects created in the directory is set to the GID of the parent directory.  false if the object is a file, the group ID (GID) is not set at execution time.  If the object is a directory in the "root" (/), QOpenSys, and user-defined file systems, the group ID (GID) of objects created in the directory is set to the effective GID of the thread creating the object.  This value cannot be set for other file systems.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setSetEffectiveGroupId(boolean setEffectiveGroupId) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAttributes(setEffectiveGroupId, (byte)0x01, (byte)0x2D);
    }

    // Whether the objects in the UDFS are temporary system objects.
    boolean containsTemporaryObjects_;
    /**
     Returns whether the objects in the user-defined file system (UDFS) are temporary system objects.
     <p>Note: This method is not supported until the release following IBM i V6R1.  For IBM i V6R1 and earlier, this method always returns false.
     @return  true if the objects in the UDFS are temporary system objects; false if the objects in the UDFS are permanent system objects.
     If the file system represented by this object is not a UDFS, this method returns false.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean containsTemporaryObjects() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return containsTemporaryObjects_;
    }

    // Whether the object is a temporary system object.
    boolean temporaryObject_;
    /**
     Returns whether the object is a temporary system object.
     <p>Note: This method is not supported until the release following IBM i V6R1.  For IBM i V6R1 and earlier, this method always returns false.
     @return  true if the object is a temporary system object; false if the object is a permanent system object.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isTemporaryObject() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        getAttributes();
        return temporaryObject_;
    }

    /**
     Refreshes the attributes from the system.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void refresh() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        attributesRetrieved_ = false;
        getAttributes();
    }

    private void getAttributes() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (attributesRetrieved_) return;

        int vrm = system_.getVRM();

        int bufferSizeProvided = 2048;
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Qlg_Path_Name_T *Path_Name
            new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, createPathName()),
            // Qp0l_AttrTypes_List_t *Attr_Array_ptr
            new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, new byte[4]),
            // char *Buffer_ptr
            new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, bufferSizeProvided),
            // uint Buffer_Size_Provided
            new ProgramParameter(ProgramParameter.PASS_BY_VALUE, BinaryConverter.intToByteArray(bufferSizeProvided)),
            // uint *Buffer_Size_Needed_ptr
            new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, 4),
            // uint *Num_Bytes_Returned_ptr
            new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, 4),
            // uint Follow_Symlnk
            new ProgramParameter(ProgramParameter.PASS_BY_VALUE, BinaryConverter.intToByteArray(followSymbolicLink_ ? 1 : 0))
        };

        ServiceProgramCall spc = new ServiceProgramCall(system_, "/QSYS.LIB/QP0LLIB2.SRVPGM", "Qp0lGetAttr", ServiceProgramCall.RETURN_INTEGER, parameters);
        boolean repeatRun;
        do
        {
            repeatRun = false;
            if (!spc.run())
            {
                throw new AS400Exception(spc.getMessageList());
            }
            if (spc.getIntegerReturnValue() == -1)
            {
                int errno = spc.getErrno();
                Trace.log(Trace.ERROR, "Get attributes was not successful, errno:", errno);
                throw new ErrnoException(system_, errno);
            }
            int bufferSizeNeeded = BinaryConverter.byteArrayToInt(parameters[2].getOutputData(), 0);
            if (bufferSizeProvided < bufferSizeNeeded)
            {
                repeatRun = true;
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Get attributes buffer too small, buffer size provided: " + bufferSizeProvided + ", buffer size needed: " + bufferSizeNeeded);
                bufferSizeProvided = bufferSizeNeeded;
                parameters[2] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, bufferSizeProvided);
                parameters[3] = new ProgramParameter(ProgramParameter.PASS_BY_VALUE, BinaryConverter.intToByteArray(bufferSizeProvided));
            }
        }
        while (repeatRun);
        attributesRetrieved_ = true;

        Converter conv = new Converter(system_.getJobCcsid(), system_);

        byte[] buffer = parameters[2].getOutputData();
        int offset = 0;
        int nextOffset = -1;
        while (nextOffset != 0)
        {
            nextOffset = BinaryConverter.byteArrayToInt(buffer, offset);
            int entrySize = BinaryConverter.byteArrayToInt(buffer, offset + 8);
            if (entrySize != 0)
            {
                switch (BinaryConverter.byteArrayToInt(buffer, offset + 4))
                {
                    case 0:
                        objectType_ = conv.byteArrayToString(buffer, offset + 16, 10).trim();
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        extendedAttributeSize_ = BinaryConverter.byteArrayToUnsignedInt(buffer, offset + 16);
                        break;
                    case 4:
                        createTime_ = new Date(BinaryConverter.byteArrayToUnsignedInt(buffer, offset + 16) * 1000);
                        break;
                    case 5:
                        accessTime_ = new Date(BinaryConverter.byteArrayToUnsignedInt(buffer, offset + 16) * 1000);
                        break;
                    case 6:
                        changeTime_ = new Date(BinaryConverter.byteArrayToUnsignedInt(buffer, offset + 16) * 1000);
                        break;
                    case 7:
                        modifyTime_ = new Date(BinaryConverter.byteArrayToUnsignedInt(buffer, offset + 16) * 1000);
                        break;
                    case 8:
                        storageFree_ = buffer[offset + 16] == 0x01;
                        break;
                    case 9:
                        checkedOut_ = buffer[offset + 16] == 0x01;
                        checkedOutUser_ = conv.byteArrayToString(buffer, offset + 17, 10).trim();
                        checkOutTime_ = new Date(BinaryConverter.byteArrayToUnsignedInt(buffer, offset + 28) * 1000);
                        break;
                    case 10:
                        localRemote_ = buffer[offset + 16];
                        break;
                    case 11:
                        objectOwner_ = conv.byteArrayToString(buffer, offset + 16, 10).trim();
                        primaryGroup_ = conv.byteArrayToString(buffer, offset + 26, 10).trim();
                        authorizationListName_ = conv.byteArrayToString(buffer, offset + 36, 10).trim();
                        break;
                    case 12:
                        fileId_ = new byte[16];
                        System.arraycopy(buffer, offset + 16, fileId_, 0, 16);
                        break;
                    case 13:
                        asp_ = BinaryConverter.byteArrayToShort(buffer, offset + 16);
                        break;
                    case 14:
                        dataSize_ = BinaryConverter.byteArrayToLong(buffer, offset + 16);
                        break;
                    case 15:
                        allocatedSize_ = BinaryConverter.byteArrayToLong(buffer, offset + 16);
                        break;
                    case 16:
                        resetDate_ = new Date(BinaryConverter.byteArrayToUnsignedInt(buffer, offset + 16) * 1000);
                        long seconds = BinaryConverter.byteArrayToUnsignedInt(buffer, offset + 20);
                        lastUsedDate_ = seconds == 0 ? null : new Date(seconds * 1000);
                        //daysUsedCount_ = BinaryConverter.byteArrayToInt(buffer, offset + 24);
                        daysUsedCount_ = BinaryConverter.byteArrayToUnsignedShort(buffer, offset + 24);
                        break;
                    case 17:
                        pcReadOnly_ = buffer[offset + 16] == 0x01;
                        break;
                    case 18:
                        pcHidden_ = buffer[offset + 16] == 0x01;
                        break;
                    case 19:
                        pcSystem_ = buffer[offset + 16] == 0x01;
                        break;
                    case 20:
                        pcArchive_ = buffer[offset + 16] == 0x01;
                        break;
                    case 21:
                        systemArchive_ = buffer[offset + 16] == 0x01;
                        break;
                    case 22:
                        codePage_ = BinaryConverter.byteArrayToInt(buffer, offset + 16);
                        break;
                    case 23:
                        fileFormat_ = buffer[offset + 16];
                        break;
                    case 24:
                        udfsDefaultFormat_ = buffer[offset + 16];
                        break;
                    case 25:
                        if (vrm < 0x00050300)
                        {
                            journalingStatus_ = buffer[offset + 16] == 0x01;
                            journalingOptions_ = buffer[offset + 17] & 0xFF;
                            journalIdentifier_ = new byte[10];
                            System.arraycopy(buffer, 18, journalIdentifier_, 0, 10);
                            if (buffer[offset + 28] == 0x00)
                            {
                                journal_ = null;
                            }
                            else
                            {
                                String journalName = conv.byteArrayToString(buffer, offset + 28, 10).trim();
                                String journalLibrary = conv.byteArrayToString(buffer, offset + 38, 10).trim();
                                journal_ = QSYSObjectPathName.toPath(journalLibrary, journalName, "JRN");
                            }
                            seconds = BinaryConverter.byteArrayToUnsignedInt(buffer, offset + 48);
                            lastJounalingStartTime_ = seconds == 0 ? null : new Date(seconds * 1000);
                        }
                        break;
                    case 26:
                        allowCheckpointWrite_ = buffer[offset + 16] == 0x01;
                        break;
                    case 27:
                        ccsid_ = BinaryConverter.byteArrayToInt(buffer, offset + 16);
                        break;
                    case 28:
                        signed_ = buffer[offset + 16] == 0x01;
                        break;
                    case 29:
                        systemSigned_ = buffer[offset + 16] == 0x01;
                        break;
                    case 30:
                        multipleSignatures_ = buffer[offset + 16] == 0x01;
                        break;
                    case 31:
                        diskStorageOption_ = buffer[offset + 16];
                        break;
                    case 32:
                        mainStorageOption_ = buffer[offset + 16];
                        break;
                    case 33:
                        directoryFormat_ = buffer[offset + 16];
                        break;
                    case 34:
                        audit_ = conv.byteArrayToString(buffer, offset + 16, 10).trim();
                        break;
                    case 35:
                        createObjectScan_ = buffer[offset + 16];
                        break;
                    case 36:
                        scan_ = buffer[offset + 16];
                        break;
                    case 37:
                        scanStatus_ = buffer[offset + 16];
                        scanSignaturesDifferent_ = buffer[offset + 17] == 0x01;
                        binaryScan_ = buffer[offset + 18] == 0x01;
                        scanCcsid1_ = BinaryConverter.byteArrayToInt(buffer, offset + 20);
                        scanCcsid2_ = BinaryConverter.byteArrayToInt(buffer, offset + 24);
                        break;
                    case 38:
                        allowSave_ = buffer[offset + 16] == 0x01;
                        break;
                    case 39:
                        restrictedRenameAndUnlink_ = buffer[offset + 16] == 0x01;
                        break;
                    case 40:
                        journalingStatus_ = buffer[offset + 16] == 0x01;
                        journalingOptions_ = buffer[offset + 17] & 0xFF;
                        journalIdentifier_ = new byte[10];
                        System.arraycopy(buffer, 18, journalIdentifier_, 0, 10);
                        if (buffer[offset + 28] == 0x00)
                        {
                            journal_ = null;
                        }
                        else
                        {
                            String journalName = conv.byteArrayToString(buffer, offset + 28, 10).trim();
                            String journalLibrary = conv.byteArrayToString(buffer, offset + 38, 10).trim();
                            journal_ = QSYSObjectPathName.toPath(journalLibrary, journalName, "JRN");
                        }
                        seconds = BinaryConverter.byteArrayToUnsignedInt(buffer, offset + 48);
                        lastJounalingStartTime_ = seconds == 0 ? null : new Date(seconds * 1000);
                        String journalReceiverName = conv.byteArrayToString(buffer, offset + 52, 10).trim();
                        String journalReceiverLibrary = conv.byteArrayToString(buffer, offset + 62, 10).trim();
                        startingJournalReceiverForApply_ = journalReceiverName.equals("") ? null : QSYSObjectPathName.toPath(journalReceiverLibrary, journalReceiverName, "JRNRCV");
                        startingJournalReceiverAspDevice_ = conv.byteArrayToString(buffer, offset + 72, 10).trim();
                        applyJournaledChangesRequired_ = buffer[offset + 82] == 0x01;
                        rollbackWasEnded_ = buffer[offset + 83] == 0x01;
                        break;
                    case 41:
                        createObjectAuditing_ = conv.byteArrayToString(buffer, offset + 16, 10).trim();
                        break;
                    case 42:
                        systemUse_ = buffer[offset + 16];
                        break;
                    case 43:
                        temporaryObject_ = buffer[offset + 16] == 0x01;
                        break;
                    case 44:
                        containsTemporaryObjects_ = buffer[offset + 16] == 0x01;
                        break;
                    case 300:
                        setEffectiveUserId_ = buffer[offset + 16] == 0x01;
                        break;
                    case 301:
                        setEffectiveGroupId_ = buffer[offset + 16] == 0x01;
                        break;
                    default:
                        // Ignore unrecognized entries.
                }
            }
            offset = nextOffset;
        }
    }

    private void setAttributes(String value, byte attributeId) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        byte[] attribute = new byte[26];
        attribute[7] = attributeId;
        attribute[11] = 10;
        for (int i = 16; i < 26; ++i) attribute[i] = 0x40;
        Converter conv = new Converter(system_.getJobCcsid(), system_);
        conv.stringToByteArray(value, attribute, 16, 10);
        setAttributes(attribute);
    }
    private void setAttributes(byte value, byte attributeId) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        byte[] attribute = new byte[17];
        attribute[7] = attributeId;
        attribute[11] = 1;
        attribute[16] = value;
        setAttributes(attribute);
    }
    private void setAttributes(int value, byte attributeId) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        byte[] attribute = new byte[20];
        attribute[7] = attributeId;
        attribute[11] = 4;
        BinaryConverter.intToByteArray(value, attribute, 16);
        setAttributes(attribute);
    }
    private void setAttributes(boolean value, byte attributeId) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAttributes(value, (byte)0x00, attributeId);
    }
    private void setAttributes(boolean value, byte attributeId1, byte attributeId2) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        byte[] attribute = new byte[17];
        attribute[6] = attributeId1;
        attribute[7] = attributeId2;
        attribute[11] = 1;
        if (value) attribute[16] = 0x01;
        setAttributes(attribute);
    }
    private void setAttributes(Date value, byte attributeId) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        byte[] attribute = new byte[20];
        attribute[7] = attributeId;
        attribute[11] = 4;
        BinaryConverter.unsignedIntToByteArray(value.getTime() / 1000, attribute, 16);
        setAttributes(attribute);
    }
    private void setAttributes(byte[] attribute) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        attributesRetrieved_ = false;

        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Qlg_Path_Name_T *Path_Name.
            new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, createPathName()),
            // char *Buffer_ptr.
            new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, attribute),
            // uint Buffer_Size.
            new ProgramParameter(ProgramParameter.PASS_BY_VALUE, BinaryConverter.intToByteArray(attribute.length)),
            // uint Follow_Symlnk.
            new ProgramParameter(ProgramParameter.PASS_BY_VALUE, BinaryConverter.intToByteArray(followSymbolicLink_ ? 1 : 0))
        };

        ServiceProgramCall spc = new ServiceProgramCall(system_, "/QSYS.LIB/QP0LLIB3.SRVPGM", "Qp0lSetAttr", ServiceProgramCall.RETURN_INTEGER, parameters);
        if (!spc.run())
        {
            throw new AS400Exception(spc.getMessageList());
        }
        if (spc.getIntegerReturnValue() == -1)
        {
            int errno = spc.getErrno();
            Trace.log(Trace.ERROR, "Set attributes was not successful, errno:", errno);
            throw new ErrnoException(system_, errno);
        }
    }
}
