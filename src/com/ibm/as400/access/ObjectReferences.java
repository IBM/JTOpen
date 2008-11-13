///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ObjectReferences.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2006 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

/**
 Represents the set of information about integrated file system references on an object that can be retrieved through the QP0LROR API.
 <p>A reference is an individual type of access or lock obtained on the object when using integrated file system interfaces.  An object may have multiple references concurrently held, provided that the reference types do not conflict with one another.
 <p>This class will not return information about byte range locks that may currently be held on an object.
 <p>The user must have execute (*X) data authority to each directory preceding the object whose references are to be obtained.  The user must have read (*R) data authority to the object whose references are to be obtained.
 **/
public class ObjectReferences
{
    // The system where object is located.
    private AS400 system_;
    // Path to the object.
    private String path_;

    // Flag indicating if the attributes have been retrieved.
    private boolean attributesRetrieved_ = false;

    // Reference Count.
    private long referenceCount_;
    // In-Use Indicator.
    private boolean inUseIndicator_;
    // Simple Reference Types Structure.
    private SimpleObjectReferenceTypesStructure simple_;
    // Extended Reference Types Structure.
    private ExtendedObjectReferenceTypesStructure extended_;
    // Referencing job list.
    private JobUsingObjectStructure[] jobs_;

    /**
     Constructs an ObjectReferences object.
     @param  system  The system object representing the system on which the object exists.
     @param  path  The path name of the object for which object reference information is retrieved.
     **/
    public ObjectReferences(AS400 system, String path)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing ObjectReferences object, system: " + system + ", path: " + path);
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

    /**
     Returns the number of references of the attribute lock type.  The attribute lock type indicates that attribute changes are prevented.
     @return  The number of references of the attribute lock type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getAttributeLock() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return simple_.attributeLock_;
    }

    /**
     Returns an indication of whether the object is currently checked out.  If it is checked out, then getCheckedOutUserName() returns the name of the user who has it checked out.
     @return  An indication of whether the object is currently checked out.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getCheckedOut() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return simple_.checkedOut_;
    }

    /**
     Returns the name of the user who has the object checked out.  An empty string ("") is returned if the object is not checked out.
     @return  The name of the user who has the object checked out.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getCheckedOutUserName() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return simple_.checkedOutUserName_;
    }

    /**
     Returns the number of references of the current directory type.  The current directory type indicates that object is a directory that is being used as the current directory of the job.
     @return  The number of references of the current directory type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getCurrentDirectory() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.currentDirectory_;
    }

    /**
     Returns the number of references of the execute type.  The execute type indicates that the reference has execute only access.
     @return  The number of references of the execute type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getExecute() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return simple_.execute_;
    }

    /**
     Returns the number of references of the execute, share with readers only type.  The execute, share with readers only type indicates that the reference has execute only access.  The sharing mode allows sharing with read and execute access intents only.
     @return  The number of references of the execute, share with readers only type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getExecuteShareWithReadersOnly() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.executeShareWithReadersOnly_;
    }

    /**
     Returns the number of references of the execute, share with readers and writers type.  The execute, share with readers and writers type indicates that the reference has execute only access.  The sharing mode allows sharing with read, execute, and write access intents.
     @return  The number of references of the execute, share with readers and writers type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getExecuteShareWithReadersAndWriters() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.executeShareWithReadersAndWriters_;
    }

    /**
     Returns the number of references of the execute, share with writers only type.  The execute, share with writers only type indicates that the reference has execute only access.  The sharing mode allows sharing with write access intents only.
     @return  The number of references of the execute, share with writers only type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getExecuteShareWithWritersOnly() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.executeShareWithWritersOnly_;
    }

    /**
     Returns the number of references of the execute, share with neither readers nor writers type.  The execute, share with neither readers nor writers type indicates that the reference has execute only access.  The sharing mode allows sharing with no other access intents.
     @return  The number of references of the execute, share with neither readers nor writers type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getExecuteShareWithNeitherReadersNorWriters() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.executeShareWithNeitherReadersNorWriters_;
    }

    /**
     Returns the number of references of the execute/read, share with readers only type.  The execute/read, share with readers only type indicates that the reference has execute and read access.  The sharing mode allows sharing with read and execute access intents only.
     @return  The number of references of the execute/read, share with readers only type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getExecuteReadShareWithReadersOnly() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.executeReadShareWithReadersOnly_;
    }

    /**
     Returns the number of references of the execute/read, share with readers and writers type.  The execute/read, share with readers and writers type indicates that the reference has execute and read access.  The sharing mode allows sharing with read, execute, and write access intents.
     @return  The number of references of the execute/read, share with readers and writers type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getExecuteReadShareWithReadersAndWriters() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.executeReadShareWithReadersAndWriters_;
    }

    /**
     Returns the number of references of the execute/read, share with writers only type.  The execute/read, share with writers only type indicates that the reference has execute and read access.  The sharing mode allows sharing with write access intents only.
     @return  The number of references of the execute/read, share with writers only type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getExecuteReadShareWithWritersOnly() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.executeReadShareWithWritersOnly_;
    }

    /**
     Returns the number of references of the execute/read, share with neither readers nor writers type.  The execute/read, share with neither readers nor writers type indicates that the reference has execute and read access.  The sharing mode allows sharing with no other access intents.
     @return  The number of references of the execute/read, share with neither readers nor writers type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getExecuteReadShareWithNeitherReadersNorWriters() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.executeReadShareWithNeitherReadersNorWriters_;
    }

    /**
     Returns the number of references of the file server reference type.  The file server reference type indicates that the File Server is holding a generic reference on the object on behalf of a client.  If this field is not 0, then session information may have been returned.
     @return  The number of references of the file server reference type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getFileServerReference() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.fileServerReference_;
    }

    /**
     Returns the number of references of the file server working directory type.  The file server working directory type indicates that the object is a directory, and the File Server is holding a working directory reference on it on behalf of a client.  If this field is not 0, then session information may have been returned.
     @return  The number of references of the file server working directory type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getFileServerWorkingDirectory() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.fileServerWorkingDirectory_;
    }

    /**
     Returns whether the object is currently in-use.  If the object is not in use, all of the reference type fields returned are 0.  If the object is in use, at least one of the reference type fields is greater than 0.  This condition may occur even if the getReferenceCount() value is 0.
     @return  true if the object is currently in-use; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isInUseIndicator() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return inUseIndicator_;
    }

    /**
     Returns the number of references of the internal save lock type.  The internal save lock type indicates that object is being referenced internally during a save operation on a different object.
     @return  The number of references of the internal save lock type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getInternalSaveLock() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return simple_.internalSaveLock_;
    }

    /**
     Returns the number of references of the link changes lock type.  The link changes lock type indicates that changes to links in the directory are prevented.
     @return  The number of references of the link changes lock type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getLinkChangesLock() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return simple_.linkChangesLock_;
    }

    /**
     Returns the number of references of the read only type.  The read only type indicates that the reference has read only access.
     @return  The number of references of the read only type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getReadOnly() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return simple_.readOnly_;
    }

    /**
     Returns the number of references of the read only, share with readers only type.  The read only, share with readers only type indicates that the reference has read only access.  The sharing mode allows sharing with read and execute access intents only.
     @return  The number of references of the read only, share with readers only type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getReadOnlyShareWithReadersOnly() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.readOnlyShareWithReadersOnly_;
    }

    /**
     Returns the number of references of the read only, share with readers and writers type.  The read only, share with readers and writers type indicates that the reference has read only access.  The sharing mode allows sharing with read, execute, and write access intents.
     @return  The number of references of the read only, share with readers and writers type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getReadOnlyShareWithReadersAndWriters() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.readOnlyShareWithReadersAndWriters_;
    }

    /**
     Returns the number of references of the read only, share with writers only type.  The read only, share with writers only type indicates that the reference has read only access.  The sharing mode allows sharing with write access intents only.
     @return  The number of references of the read only, share with writers only type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getReadOnlyShareWithWritersOnly() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.readOnlyShareWithWritersOnly_;
    }

    /**
     Returns the number of references of the read only, share with neither readers nor writers type.  The read only, share with neither readers nor writers type indicates that the reference has read only access.  The sharing mode allows sharing with no other access intents.
     @return  The number of references of the read only, share with neither readers nor writers type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getReadOnlyShareWithNeitherReadersNorWriters() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.readOnlyShareWithNeitherReadersNorWriters_;
    }

    /**
     Returns the number of references of the read/write type.  The read/write type indicates that the reference has read and write access.
     @return  The number of references of the read/write type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getReadWrite() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return simple_.readWrite_;
    }

    /**
     Returns the number of references of the read/write, share with readers only type.  The read/write, share with readers only type indicates that the reference has read and write access.  The sharing mode allows sharing with read and execute access intents only.
     @return  The number of references of the read/write, share with readers only type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getReadWriteShareWithReadersOnly() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.readWriteShareWithReadersOnly_;
    }

    /**
     Returns the number of references of the read/write, share with readers and writers type.  The read/write, share with readers and writers type indicates that the reference has read and write access.  The sharing mode allows sharing with read, execute, and write access intents.
     @return  The number of references of the read/write, share with readers and writers type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getReadWriteShareWithReadersAndWriters() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.readWriteShareWithReadersAndWriters_;
    }

    /**
     Returns the number of references of the read/write, share with writers only type.  The read/write, share with writers only type indicates that the reference has read and write access.  The sharing mode allows sharing with write access intents only.
     @return  The number of references of the read/write, share with writers only type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getReadWriteShareWithWritersOnly() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.readWriteShareWithWritersOnly_;
    }

    /**
     Returns the number of references of the read/write, share with neither readers nor writers type.  The read/write, share with neither readers nor writers type indicates that the reference has read and write access.  The sharing mode allows sharing with no other access intents.
     @return  The number of references of the read/write, share with neither readers nor writers type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getReadWriteShareWithNeitherReadersNorWriters() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.readWriteShareWithNeitherReadersNorWriters_;
    }

    /**
     Returns the current number of references on the object.  NOTE: This may be 0 even though the isInUseIndicator() indicates that the object is in use.
     @return  The current number of references on the object.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getReferenceCount() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return referenceCount_;
    }

    /**
     Returns the number of references of the root directory type.  The root directory type indicates that object is a directory that is being used as the root directory of the job.
     @return  The number of references of the root directory type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getRootDirectory() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.rootDirectory_;
    }

    /**
     Returns the number of references of the save lock type.  The save lock type indicates that object is being referenced by an object save operation.
     @return  The number of references of the save lock type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getSaveLock() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return simple_.saveLock_;
    }

    /**
     Returns the number of references of the share with readers only type.  The share with readers only type indicates that the sharing mode allows sharing with read and execute access intents only.
     @return  The number of references of the share with readers only type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getShareWithReadersOnly() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return simple_.shareWithReadersOnly_;
    }

    /**
     Returns the number of references of the share with readers and writers type.  The share with readers and writers type indicates that the sharing mode allows sharing with read, execute, and write access intents.
     @return  The number of references of the share with readers and writers type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getShareWithReadersAndWriters() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return simple_.shareWithReadersAndWriters_;
    }

    /**
     Returns the number of references of the share with writers only type.  The share with writers only type indicates that the sharing mode allows sharing with write access intents only.
     @return  The number of references of the share with writers only type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getShareWithWritersOnly() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return simple_.shareWithWritersOnly_;
    }

    /**
     Returns the number of references of the share with neither readers nor writers type.  The share with neither readers nor writers type indicates that the sharing mode allows sharing with no other access intents.
     @return  The number of references of the share with neither readers nor writers type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getShareWithNeitherReadersNorWriters() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return simple_.shareWithNeitherReadersNorWriters_;
    }

    /**
     Returns the number of references of the write only type.  The write only type indicates that the reference has write only access.
     @return  The number of references of the write only type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getWriteOnly() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return simple_.writeOnly_;
    }

    /**
     Returns the number of references of the write only, share with readers only type.  The write only, share with readers only type indicates that the reference has write only access.  The sharing mode allows sharing with read and execute access intents only.
     @return  The number of references of the write only, share with readers only type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getWriteOnlyShareWithReadersOnly() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.writeOnlyShareWithReadersOnly_;
    }

    /**
     Returns the number of references of the write only, share with readers and writers type.  The write only, share with readers and writers type indicates that the reference has write only access.  The sharing mode allows sharing with read, execute, and write access intents.
     @return  The number of references of the write only, share with readers and writers type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getWriteOnlyShareWithReadersAndWriters() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.writeOnlyShareWithReadersAndWriters_;
    }

    /**
     Returns the number of references of the write only, share with writers only type.  The write only, share with writers only type indicates that the reference has write only access.  The sharing mode allows sharing with write access intents only.
     @return  The number of references of the write only, share with writers only type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getWriteOnlyShareWithWritersOnly() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.writeOnlyShareWithWritersOnly_;
    }

    /**
     Returns the number of references of the write only, share with neither readers nor writers type.  The write only, share with neither readers nor writers type indicates that the reference has write only access.  The sharing mode allows sharing with no other access intents.
     @return  The number of references of the write only, share with neither readers nor writers type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public long getWriteOnlyShareWithNeitherReadersNorWriters() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return extended_.writeOnlyShareWithNeitherReadersNorWriters_;
    }

    /**
     Returns information about the jobs that are known to be holding a reference on the object.
     @return  Information about the jobs that are known to be holding a reference on the object.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public JobUsingObjectStructure[] getJobUsingObjectStructures() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve();
        return jobs_;
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
        retrieve();
    }

    // Call the QP0LROR API if necessary.
    private void retrieve() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (attributesRetrieved_) return;

        int vrm = system_.getVRM();

        int bufferSizeProvided = 2048;
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // void *Receiver_Ptr, output.
            new ProgramParameter(bufferSizeProvided),
            // unsigned int Receiver_Length, input.
            new ProgramParameter(BinaryConverter.intToByteArray(bufferSizeProvided)),
            // char *Format_Ptr, input, EBCDIC 'ROR00200'.
            new ProgramParameter(new byte[] { (byte)0xD9, (byte)0xD6, (byte)0xD9, (byte)0xD6, (byte)0xF0, (byte)0xF2, (byte)0xF0, (byte)0xF0 } ),
            // Qlg_Path_Name_T *Path_Ptr.
            new ProgramParameter(createPathName()),
            // void *Error_Code_Ptr.
            new ProgramParameter(new byte[8]),
        };

        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QP0LROR.PGM", parameters);
        // QP0LROR is not thread safe.
        boolean repeatRun;
        byte[] receiverVariable;
        do
        {
            repeatRun = false;
            if (!pc.run())
            {
                throw new AS400Exception(pc.getMessageList());
            }

            receiverVariable = parameters[0].getOutputData();

            int bytesReturned = BinaryConverter.byteArrayToInt(receiverVariable, 0);
            int bytesAvailable = BinaryConverter.byteArrayToInt(receiverVariable, 4);
            if (bytesReturned < bytesAvailable)
            {
                repeatRun = true;
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieve object references receiver variable too small, bytes returned: " + bytesReturned + ", bytes available: " + bytesAvailable);
                parameters[0] = new ProgramParameter(bytesAvailable);
                parameters[1] = new ProgramParameter(BinaryConverter.intToByteArray(bytesAvailable));
            }
        }
        while (repeatRun);
        attributesRetrieved_ = true;

        Converter conv = new Converter(system_.getJobCcsid(), system_);

        referenceCount_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, 8);
        inUseIndicator_ = BinaryConverter.byteArrayToInt(receiverVariable, 12) == 0x01;
        int offsetToSimpleReferenceTypes = BinaryConverter.byteArrayToInt(receiverVariable, 16);
        //int lengthOfSimpleReferenceTypes = BinaryConverter.byteArrayToInt(receiverVariable, 20);
        int offsetToExtendedReferenceTypes = BinaryConverter.byteArrayToInt(receiverVariable, 24);
        //int lengthOfExtendedReferenceTypes = BinaryConverter.byteArrayToInt(receiverVariable, 28);
        int offsetToJobList = BinaryConverter.byteArrayToInt(receiverVariable, 32);
        int jobsReturned = BinaryConverter.byteArrayToInt(receiverVariable, 36);
        if (Trace.traceOn_)
        {
            int jobsAvailable = BinaryConverter.byteArrayToInt(receiverVariable, 40);
            if (jobsAvailable > jobsReturned)
            {
                Trace.log(Trace.WARNING, "Retrieve object references receiver variable too small, jobs returned: " + jobsReturned + ", jobs available: " + jobsAvailable);
            }
        }

        simple_ = new SimpleObjectReferenceTypesStructure(receiverVariable, offsetToSimpleReferenceTypes, conv);
        extended_ = new ExtendedObjectReferenceTypesStructure(receiverVariable, offsetToExtendedReferenceTypes, conv);
        jobs_ = new JobUsingObjectStructure[jobsReturned];
        for (int i = 0; i < jobsReturned; ++i)
        {
            jobs_[i] = new JobUsingObjectStructure(receiverVariable, offsetToJobList, conv, vrm);
            offsetToJobList += jobs_[i].displacementToNextJobEntry_;
        }
    }

    private static final class SimpleObjectReferenceTypesStructure
    {
        // Read Only.
        long readOnly_;
        // Write Only.
        long writeOnly_;
        // Read/Write.
        long readWrite_;
        // Execute.
        long execute_;
        // Share with Readers Only.
        long shareWithReadersOnly_;
        // Share with Writers Only.
        long shareWithWritersOnly_;
        // Share with Readers and Writers.
        long shareWithReadersAndWriters_;
        // Share with neither Readers nor Writers.
        long shareWithNeitherReadersNorWriters_;
        // Attribute Lock.
        long attributeLock_;
        // Save Lock.
        long saveLock_;
        // Internal Save Lock.
        long internalSaveLock_;
        // Link Changes Lock.
        long linkChangesLock_;
        // Checked Out.
        long checkedOut_;
        // Checked Out User Name.
        String checkedOutUserName_;

        SimpleObjectReferenceTypesStructure(byte[] receiverVariable, int offset, Converter conv)
        {
            readOnly_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset);
            writeOnly_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 4);
            readWrite_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 8);
            execute_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 12);
            shareWithReadersOnly_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 16);
            shareWithWritersOnly_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 20);
            shareWithReadersAndWriters_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 24);
            shareWithNeitherReadersNorWriters_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 28);
            attributeLock_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 32);
            saveLock_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 36);
            internalSaveLock_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 40);
            linkChangesLock_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 44);
            checkedOut_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 48);
            checkedOutUserName_ = conv.byteArrayToString(receiverVariable, offset + 52, 10).trim();
        }
    }

    private static final class ExtendedObjectReferenceTypesStructure
    {
        // Read Only, Share with Readers Only.
        long readOnlyShareWithReadersOnly_;
        // Read Only, Share with Writers Only.
        long readOnlyShareWithWritersOnly_;
        // Read Only, Share with Readers and Writers.
        long readOnlyShareWithReadersAndWriters_;
        // Read Only, Share with neither Readers nor Writers.
        long readOnlyShareWithNeitherReadersNorWriters_;
        // Write Only, Share with Readers Only.
        long writeOnlyShareWithReadersOnly_;
        // Write Only, Share with Writers Only.
        long writeOnlyShareWithWritersOnly_;
        // Write Only, Share with Readers and Writers.
        long writeOnlyShareWithReadersAndWriters_;
        // Write Only, Share with neither Readers nor Writers.
        long writeOnlyShareWithNeitherReadersNorWriters_;
        // Read/Write, Share with Readers Only.
        long readWriteShareWithReadersOnly_;
        // Read/Write, Share with Writers Only.
        long readWriteShareWithWritersOnly_;
        // Read/Write, Share with Readers and Writers.
        long readWriteShareWithReadersAndWriters_;
        // Read/Write, Share with neither Readers nor Writers.
        long readWriteShareWithNeitherReadersNorWriters_;
        // Execute, Share with Readers Only.
        long executeShareWithReadersOnly_;
        // Execute, Share with Writers Only.
        long executeShareWithWritersOnly_;
        // Execute, Share with Readers and Writers.
        long executeShareWithReadersAndWriters_;
        // Execute, Share with neither Readers nor Writers.
        long executeShareWithNeitherReadersNorWriters_;
        // Execute/Read, Share with Readers Only.
        long executeReadShareWithReadersOnly_;
        // Execute/Read, Share with Writers Only.
        long executeReadShareWithWritersOnly_;
        // Execute/Read, Share with Readers and Writers.
        long executeReadShareWithReadersAndWriters_;
        // Execute/Read, Share with neither Readers nor Writers.
        long executeReadShareWithNeitherReadersNorWriters_;
        // Attribute Lock.
        //long attributeLock_;
        // Save Lock.
        //long saveLock_;
        // Internal Save Lock.
        //long internalSaveLock_;
        // Link Changes Lock.
        //long linkChangesLock_;
        // Current Directory.
        long currentDirectory_;
        // Root Directory.
        long rootDirectory_;
        // File Server Reference.
        long fileServerReference_;
        // File Server Working Directory.
        long fileServerWorkingDirectory_;
        // Checked Out.
        //long checkedOut_;
        // Checked Out User Name.
        //String checkedOutUserName_;

        ExtendedObjectReferenceTypesStructure(byte[] receiverVariable, int offset, Converter conv)
        {
            readOnlyShareWithReadersOnly_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset);
            readOnlyShareWithWritersOnly_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 4);
            readOnlyShareWithReadersAndWriters_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 8);
            readOnlyShareWithNeitherReadersNorWriters_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 12);
            writeOnlyShareWithReadersOnly_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 16);
            writeOnlyShareWithWritersOnly_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 20);
            writeOnlyShareWithReadersAndWriters_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 24);
            writeOnlyShareWithNeitherReadersNorWriters_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 28);
            readWriteShareWithReadersOnly_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 32);
            readWriteShareWithWritersOnly_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 36);
            readWriteShareWithReadersAndWriters_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 40);
            readWriteShareWithNeitherReadersNorWriters_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 44);
            executeShareWithReadersOnly_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 48);
            executeShareWithWritersOnly_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 52);
            executeShareWithReadersAndWriters_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 56);
            executeShareWithNeitherReadersNorWriters_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 60);
            executeReadShareWithReadersOnly_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 64);
            executeReadShareWithWritersOnly_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 68);
            executeReadShareWithReadersAndWriters_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 72);
            executeReadShareWithNeitherReadersNorWriters_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 76);
            // attributeLock_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 80);
            // saveLock_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 84);
            // internalSaveLock_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 88);
            // linkChangesLock_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 92);
            currentDirectory_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 96);
            rootDirectory_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 100);
            fileServerReference_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 104);
            fileServerWorkingDirectory_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 108);
            // checkedOut_ = BinaryConverter.byteArrayToUnsignedInt(receiverVariable, offset + 112);
            // checkedOutUserName_ = conv.byteArrayToString(receiverVariable, offset + 116, 10).trim();
        }
    }

    /**
     Contains information about the jobs that are known to be holding a reference on the object.
     **/
    public static class JobUsingObjectStructure
    {
        // Displacement to Next Job Entry.
        private int displacementToNextJobEntry_;
        // Job Name.
        private String jobName_;
        // Job User.
        private String jobUser_;
        // Job Number.
        private String jobNumber_;
        // Simple reference types structure.
        private SimpleObjectReferenceTypesStructure jobSimple_;
        // Extended reference types structure.
        private ExtendedObjectReferenceTypesStructure jobExtended_;
        // Session Using Object Structure.
        private SessionUsingObjectStructure[] sessions_;

        /**
         Returns the name of the job.
         @return  The name of the job.
         **/
        public String getJobName()
        {
            return jobName_;
        }

        /**
         Returns the number associated with the job.
         @return  The number associated with the job.
         **/
        public String getJobNumber()
        {
            return jobNumber_;
        }

        /**
         Returns the user profile associated with the job.
         @return  The user profile associated with the job.
         **/
        public String getJobUser()
        {
            return jobUser_;
        }

        /**
         Returns the number of references within this specific job of the attribute lock type.  The attribute lock type indicates that attribute changes are prevented.
         @return  The number of references within this specific job of the attribute lock type.
         **/
        public long getAttributeLock()
        {
            return jobSimple_.attributeLock_;
        }

        /**
         Returns an indication of whether the object is currently checked out within this specific job.  If it is checked out, then getCheckedOutUserName() returns the name of the user who has it checked out.
         @return  An indication of whether the object is currently checked out within this specific job.
         **/
        public long getCheckedOut()
        {
            return jobSimple_.checkedOut_;
        }

        /**
         Returns the name of the user who has the object checked out within this specific job.  An empty string ("") is returned if the object is not checked out.
         @return  The name of the user who has the object checked out within this specific job.
         **/
        public String getCheckedOutUserName()
        {
            return jobSimple_.checkedOutUserName_;
        }

        /**
         Returns the number of references within this specific job of the current directory type.  The current directory type indicates that object is a directory that is being used as the current directory of the job.
         @return  The number of references within this specific job of the current directory type.
         **/
        public long getCurrentDirectory()
        {
            return jobExtended_.currentDirectory_;
        }

        /**
         Returns the number of references within this specific job of the execute type.  The execute type indicates that the reference has execute only access.
         @return  The number of references within this specific job of the execute type.
         **/
        public long getExecute()
        {
            return jobSimple_.execute_;
        }

        /**
         Returns the number of references within this specific job of the execute, share with readers only type.  The execute, share with readers only type indicates that the reference has execute only access.  The sharing mode allows sharing with read and execute access intents only.
         @return  The number of references within this specific job of the execute, share with readers only type.
         **/
        public long getExecuteShareWithReadersOnly()
        {
            return jobExtended_.executeShareWithReadersOnly_;
        }

        /**
         Returns the number of references within this specific job of the execute, share with readers and writers type.  The execute, share with readers and writers type indicates that the reference has execute only access.  The sharing mode allows sharing with read, execute, and write access intents.
         @return  The number of references within this specific job of the execute, share with readers and writers type.
         **/
        public long getExecuteShareWithReadersAndWriters()
        {
            return jobExtended_.executeShareWithReadersAndWriters_;
        }

        /**
         Returns the number of references within this specific job of the execute, share with writers only type.  The execute, share with writers only type indicates that the reference has execute only access.  The sharing mode allows sharing with write access intents only.
         @return  The number of references within this specific job of the execute, share with writers only type.
         **/
        public long getExecuteShareWithWritersOnly()
        {
            return jobExtended_.executeShareWithWritersOnly_;
        }

        /**
         Returns the number of references within this specific job of the execute, share with neither readers nor writers type.  The execute, share with neither readers nor writers type indicates that the reference has execute only access.  The sharing mode allows sharing with no other access intents.
         @return  The number of references within this specific job of the execute, share with neither readers nor writers type.
         **/
        public long getExecuteShareWithNeitherReadersNorWriters()
        {
            return jobExtended_.executeShareWithNeitherReadersNorWriters_;
        }

        /**
         Returns the number of references within this specific job of the execute/read, share with readers only type.  The execute/read, share with readers only type indicates that the reference has execute and read access.  The sharing mode allows sharing with read and execute access intents only.
         @return  The number of references within this specific job of the execute/read, share with readers only type.
         **/
        public long getExecuteReadShareWithReadersOnly()
        {
            return jobExtended_.executeReadShareWithReadersOnly_;
        }

        /**
         Returns the number of references within this specific job of the execute/read, share with readers and writers type.  The execute/read, share with readers and writers type indicates that the reference has execute and read access.  The sharing mode allows sharing with read, execute, and write access intents.
         @return  The number of references within this specific job of the execute/read, share with readers and writers type.
         **/
        public long getExecuteReadShareWithReadersAndWriters()
        {
            return jobExtended_.executeReadShareWithReadersAndWriters_;
        }

        /**
         Returns the number of references within this specific job of the execute/read, share with writers only type.  The execute/read, share with writers only type indicates that the reference has execute and read access.  The sharing mode allows sharing with write access intents only.
         @return  The number of references within this specific job of the execute/read, share with writers only type.
         **/
        public long getExecuteReadShareWithWritersOnly()
        {
            return jobExtended_.executeReadShareWithWritersOnly_;
        }

        /**
         Returns the number of references within this specific job of the execute/read, share with neither readers nor writers type.  The execute/read, share with neither readers nor writers type indicates that the reference has execute and read access.  The sharing mode allows sharing with no other access intents.
         @return  The number of references within this specific job of the execute/read, share with neither readers nor writers type.
         **/
        public long getExecuteReadShareWithNeitherReadersNorWriters()
        {
            return jobExtended_.executeReadShareWithNeitherReadersNorWriters_;
        }

        /**
         Returns the number of references within this specific job of the file server reference type.  The file server reference type indicates that the File Server is holding a generic reference on the object on behalf of a client.  If this field is not 0, then session information may have been returned.
         @return  The number of references within this specific job of the file server reference type.
         **/
        public long getFileServerReference()
        {
            return jobExtended_.fileServerReference_;
        }

        /**
         Returns the number of references within this specific job of the file server working directory type.  The file server working directory type indicates that the object is a directory, and the File Server is holding a working directory reference on it on behalf of a client.  If this field is not 0, then session information may have been returned.
         @return  The number of references within this specific job of the file server working directory type.
         **/
        public long getFileServerWorkingDirectory()
        {
            return jobExtended_.fileServerWorkingDirectory_;
        }

        /**
         Returns the number of references within this specific job of the internal save lock type.  The internal save lock type indicates that object is being referenced internally during a save operation on a different object.
         @return  The number of references within this specific job of the internal save lock type.
         **/
        public long getInternalSaveLock()
        {
            return jobSimple_.internalSaveLock_;
        }

        /**
         Returns the number of references within this specific job of the link changes lock type.  The link changes lock type indicates that changes to links in the directory are prevented.
         @return  The number of references within this specific job of the link changes lock type.
         **/
        public long getLinkChangesLock()
        {
            return jobSimple_.linkChangesLock_;
        }

        /**
         Returns the number of references within this specific job of the read only type.  The read only type indicates that the reference has read only access.
         @return  The number of references within this specific job of the read only type.
         **/
        public long getReadOnly()
        {
            return jobSimple_.readOnly_;
        }

        /**
         Returns the number of references within this specific job of the read only, share with readers only type.  The read only, share with readers only type indicates that the reference has read only access.  The sharing mode allows sharing with read and execute access intents only.
         @return  The number of references within this specific job of the read only, share with readers only type.
         **/
        public long getReadOnlyShareWithReadersOnly()
        {
            return jobExtended_.readOnlyShareWithReadersOnly_;
        }

        /**
         Returns the number of references within this specific job of the read only, share with readers and writers type.  The read only, share with readers and writers type indicates that the reference has read only access.  The sharing mode allows sharing with read, execute, and write access intents.
         @return  The number of references within this specific job of the read only, share with readers and writers type.
         **/
        public long getReadOnlyShareWithReadersAndWriters()
        {
            return jobExtended_.readOnlyShareWithReadersAndWriters_;
        }

        /**
         Returns the number of references within this specific job of the read only, share with writers only type.  The read only, share with writers only type indicates that the reference has read only access.  The sharing mode allows sharing with write access intents only.
         @return  The number of references within this specific job of the read only, share with writers only type.
         **/
        public long getReadOnlyShareWithWritersOnly()
        {
            return jobExtended_.readOnlyShareWithWritersOnly_;
        }

        /**
         Returns the number of references within this specific job of the read only, share with neither readers nor writers type.  The read only, share with neither readers nor writers type indicates that the reference has read only access.  The sharing mode allows sharing with no other access intents.
         @return  The number of references within this specific job of the read only, share with neither readers nor writers type.
         **/
        public long getReadOnlyShareWithNeitherReadersNorWriters()
        {
            return jobExtended_.readOnlyShareWithNeitherReadersNorWriters_;
        }

        /**
         Returns the number of references within this specific job of the read/write type.  The read/write type indicates that the reference has read and write access.
         @return  The number of references within this specific job of the read/write type.
         **/
        public long getReadWrite()
        {
            return jobSimple_.readWrite_;
        }

        /**
         Returns the number of references within this specific job of the read/write, share with readers only type.  The read/write, share with readers only type indicates that the reference has read and write access.  The sharing mode allows sharing with read and execute access intents only.
         @return  The number of references within this specific job of the read/write, share with readers only type.
         **/
        public long getReadWriteShareWithReadersOnly()
        {
            return jobExtended_.readWriteShareWithReadersOnly_;
        }

        /**
         Returns the number of references within this specific job of the read/write, share with readers and writers type.  The read/write, share with readers and writers type indicates that the reference has read and write access.  The sharing mode allows sharing with read, execute, and write access intents.
         @return  The number of references within this specific job of the read/write, share with readers and writers type.
         **/
        public long getReadWriteShareWithReadersAndWriters()
        {
            return jobExtended_.readWriteShareWithReadersAndWriters_;
        }

        /**
         Returns the number of references within this specific job of the read/write, share with writers only type.  The read/write, share with writers only type indicates that the reference has read and write access.  The sharing mode allows sharing with write access intents only.
         @return  The number of references within this specific job of the read/write, share with writers only type.
         **/
        public long getReadWriteShareWithWritersOnly()
        {
            return jobExtended_.readWriteShareWithWritersOnly_;
        }

        /**
         Returns the number of references within this specific job of the read/write, share with neither readers nor writers type.  The read/write, share with neither readers nor writers type indicates that the reference has read and write access.  The sharing mode allows sharing with no other access intents.
         @return  The number of references within this specific job of the read/write, share with neither readers nor writers type.
         **/
        public long getReadWriteShareWithNeitherReadersNorWriters()
        {
            return jobExtended_.readWriteShareWithNeitherReadersNorWriters_;
        }

        /**
         Returns the number of references within this specific job of the root directory type.  The root directory type indicates that object is a directory that is being used as the root directory of the job.
         @return  The number of references within this specific job of the root directory type.
         **/
        public long getRootDirectory()
        {
            return jobExtended_.rootDirectory_;
        }

        /**
         Returns the number of references within this specific job of the save lock type.  The save lock type indicates that object is being referenced by an object save operation.
         @return  The number of references within this specific job of the save lock type.
         **/
        public long getSaveLock()
        {
            return jobSimple_.saveLock_;
        }

        /**
         Returns the number of references within this specific job of the share with readers only type.  The share with readers only type indicates that the sharing mode allows sharing with read and execute access intents only.
         @return  The number of references within this specific job of the share with readers only type.
         **/
        public long getShareWithReadersOnly()
        {
            return jobSimple_.shareWithReadersOnly_;
        }

        /**
         Returns the number of references within this specific job of the share with readers and writers type.  The share with readers and writers type indicates that the sharing mode allows sharing with read, execute, and write access intents.
         @return  The number of references within this specific job of the share with readers and writers type.
         **/
        public long getShareWithReadersAndWriters()
        {
            return jobSimple_.shareWithReadersAndWriters_;
        }

        /**
         Returns the number of references within this specific job of the share with writers only type.  The share with writers only type indicates that the sharing mode allows sharing with write access intents only.
         @return  The number of references within this specific job of the share with writers only type.
         **/
        public long getShareWithWritersOnly()
        {
            return jobSimple_.shareWithWritersOnly_;
        }

        /**
         Returns the number of references within this specific job of the share with neither readers nor writers type.  The share with neither readers nor writers type indicates that the sharing mode allows sharing with no other access intents.
         @return  The number of references within this specific job of the share with neither readers nor writers type.
         **/
        public long getShareWithNeitherReadersNorWriters()
        {
            return jobSimple_.shareWithNeitherReadersNorWriters_;
        }

        /**
         Returns the number of references within this specific job of the write only type.  The write only type indicates that the reference has write only access.
         @return  The number of references within this specific job of the write only type.
         **/
        public long getWriteOnly()
        {
            return jobSimple_.writeOnly_;
        }

        /**
         Returns the number of references within this specific job of the write only, share with readers only type.  The write only, share with readers only type indicates that the reference has write only access.  The sharing mode allows sharing with read and execute access intents only.
         @return  The number of references within this specific job of the write only, share with readers only type.
         **/
        public long getWriteOnlyShareWithReadersOnly()
        {
            return jobExtended_.writeOnlyShareWithReadersOnly_;
        }

        /**
         Returns the number of references within this specific job of the write only, share with readers and writers type.  The write only, share with readers and writers type indicates that the reference has write only access.  The sharing mode allows sharing with read, execute, and write access intents.
         @return  The number of references within this specific job of the write only, share with readers and writers type.
         **/
        public long getWriteOnlyShareWithReadersAndWriters()
        {
            return jobExtended_.writeOnlyShareWithReadersAndWriters_;
        }

        /**
         Returns the number of references within this specific job of the write only, share with writers only type.  The write only, share with writers only type indicates that the reference has write only access.  The sharing mode allows sharing with write access intents only.
         @return  The number of references within this specific job of the write only, share with writers only type.
         **/
        public long getWriteOnlyShareWithWritersOnly()
        {
            return jobExtended_.writeOnlyShareWithWritersOnly_;
        }

        /**
         Returns the number of references within this specific job of the write only, share with neither readers nor writers type.  The write only, share with neither readers nor writers type indicates that the reference has write only access.  The sharing mode allows sharing with no other access intents.
         @return  The number of references within this specific job of the write only, share with neither readers nor writers type.
         **/
        public long getWriteOnlyShareWithNeitherReadersNorWriters()
        {
            return jobExtended_.writeOnlyShareWithNeitherReadersNorWriters_;
        }

        /**
         Returns information about the sessions that are known to be holding a reference on the object.
         @return  Information about the sessions that are known to be holding a reference on the object.
         **/
        public SessionUsingObjectStructure[] getSessionUsingObjectStructures()
        {
            return sessions_;
        }

        private JobUsingObjectStructure(byte[] receiverVariable, int offset, Converter conv, int vrm)
        {
            int displacementToSimpleReferenceTypes = BinaryConverter.byteArrayToInt(receiverVariable, offset);
            //int lengthOfSimpleReferenceTypes = BinaryConverter.byteArrayToInt(receiverVariable, offset + 4);
            int displacementToExtendedReferenceTypes = BinaryConverter.byteArrayToInt(receiverVariable, offset + 8);
            int lengthOfExtendedReferenceTypes = BinaryConverter.byteArrayToInt(receiverVariable, offset + 12);
            displacementToNextJobEntry_ = BinaryConverter.byteArrayToInt(receiverVariable, offset + 16);
            jobName_ = conv.byteArrayToString(receiverVariable, offset + 20, 10).trim();
            jobUser_ = conv.byteArrayToString(receiverVariable, offset + 30, 10).trim();
            jobNumber_ = conv.byteArrayToString(receiverVariable, offset + 40, 6).trim();
            jobSimple_ = new SimpleObjectReferenceTypesStructure(receiverVariable, offset + displacementToSimpleReferenceTypes, conv);
            jobExtended_ = new ExtendedObjectReferenceTypesStructure(receiverVariable, offset + displacementToExtendedReferenceTypes, conv);
            if (vrm < 0x00050400)
            {
                sessions_ = new SessionUsingObjectStructure[0];
            }
            else
            {
                int displacementToSessionList = BinaryConverter.byteArrayToInt(receiverVariable, offset + 48);
                int sessionsReturned = BinaryConverter.byteArrayToInt(receiverVariable, offset + 52);
                sessions_ = new SessionUsingObjectStructure[sessionsReturned];
                for (int i = 0; i < sessionsReturned; ++i)
                {
                    sessions_[i] = new SessionUsingObjectStructure(receiverVariable, offset + displacementToSessionList, conv);
                    displacementToSessionList += sessions_[i].displacementToNextSessionEntry_;
                }
            }
        }
    }

    /**
     Contains information about the sessions that are known to be holding a reference on the object.
     **/
    public static class SessionUsingObjectStructure
    {
        // Session identifier.
        private byte[] sessionIdentifier_;
        // Displacement to Next Session Entry.
        private int displacementToNextSessionEntry_;
        // User Name.
        private String userName_;
        // Workstation Name.
        private String workstationName_;
        // Workstation Address.
        private String workstationAddress_;

        /**
         Returns the unique identifier for the session.
         @return  The unique identifier for the session.
         **/
        public byte[] getSessionIdentifier()
        {
            return sessionIdentifier_;
        }

        /**
         Returns the name of the user that is associated with the session.
         @return  The name of the user that is associated with the session.
         **/
        public String getUserName()
        {
            return userName_;
        }

        /**
         Returns the IP address of the workstation from which the session to the system was established.  If this information is not available, an empty string ("") is returned.
         @return  The IP address of the workstation from which the session to the system was established.
         **/
        public String getWorkstationAddress()
        {
            return workstationAddress_;
        }

        /**
         Returns the name of the workstation from which the session to the system was established.  If this information is not available, an empty string ("") is returned.
         @return  The name of the workstation from which the session to the system was established.
         **/
        public String getWorkstationName()
        {
            return workstationName_;
        }

        private SessionUsingObjectStructure(byte[] receiverVariable, int offset, Converter conv)
        {
            sessionIdentifier_ = new byte[8];
            System.arraycopy(receiverVariable, offset, sessionIdentifier_, 0, 8);
            displacementToNextSessionEntry_ = BinaryConverter.byteArrayToInt(receiverVariable, offset + 8);
            userName_ = conv.byteArrayToString(receiverVariable, offset + 12, 10).trim();
            workstationName_ = conv.byteArrayToString(receiverVariable, offset + 22, 15).trim();
            workstationAddress_ = conv.byteArrayToString(receiverVariable, offset + 37, 45).trim();
        }
    }
}
