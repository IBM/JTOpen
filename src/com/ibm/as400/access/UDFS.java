///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  UDFS.java
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
 The UDFS class represents a user-defined file system on the system.
 **/
public class UDFS
{
    // The system where the UDFS is located.
    private AS400 system_;
    // Path to the UDFS.
    private String path_;

    /**
     Constructs a UDFS object.
     @param  system  The system object representing the system on which the UDFS exists.
     @param  path  The path name of the file system.  It must be in one of the following two forms:
     <ol>
     <li>/dev/qaspXX/udfsname.udfs, where XX is one of the valid system or basic user auxiliary storage pool (ASP) numbers on the system, and udfsname is the name of the user-defined file system.  All other parts of the name must appear as in the example above.
     <li>/dev/aspname/udfsname.udfs, where aspname is one of the valid independent ASP names on the system, and udfsname is the name of the user-defined file system.  All other parts of the name must appear as in the example above.
     </ol>
     <p>The name part of the path must be unique within the specified qaspXX or aspname directory.
     <p>Wildcard characters such as '*' and '?' are not allowed in this parameter.
     **/
    public UDFS(AS400 system, String path)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing UDFS object, system: " + system + " path: " + path);
        // Validate arguments.
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

    /**
     Creates a file system that can be made visible to the rest of the integrated file system name space through the mount() method.
     <p>A UDFS is represented by the object type *BLKSF, or block special file.
     <p>The public data authority and public object authority will be *INDIR, the auditing value will be *SYSVAL, the scanning option will be *PARENT, rename and unlink will not be restricted, the default disk storage option and the default main storage option will be *NORMAL, the case sensitivity will be *MONO, the default file format will be *TYPE2, and the description will be *BLANK.
     <p>Restrictions:
     <ol>
     <li>The user must have input/output (I/O) system configuration (*IOSYSCFG) special authority.
     <li>A maximum of approximately 4,000 user-defined file systems can be created on an independent auxiliary storage pool (ASP).
     </ol>
     **/
    public void create() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Creating UDFS.");
        runCommand("CRTUDFS UDFS('" + path_ + "')");
    }

    /**
     Creates a file system that can be made visible to the rest of the integrated file system name space through the mount() method.
     <p>A UDFS is represented by the object type *BLKSF, or block special file.
     <p>Restrictions:
     <ol>
     <li>The user must have input/output (I/O) system configuration (*IOSYSCFG) special authority.
     <li>The audit (*AUDIT) special authority is required when specifying a value other than *SYSVAL on the auditingValue parameter.
     <li>The user must have all object (*ALLOBJ) and security administrator (*SECADM) special authorities to specify a value for the scanningOption parameter other than *PARENT.
     <li>A maximum of approximately 4,000 user-defined file systems can be created on an independent auxiliary storage pool (ASP).
     </ol>
     @param  publicDataAuthority  Specifies the public data authority given to the user for the new user-defined file system (UDFS), or specifies that all authorities are inherited from the directory it is to be created in.  Possible values are:
     <ul>
     <li>"*INDIR" - The authority for the UDFS to be created is determined by the directory it is to be created in.  This means the new UDFS will inherit its primary group, authorization list, and its public, private and primary group authorities from the /dev/qaspXX or /dev/aspname directory.  If the value *INDIR is specified for either the publicObjectAuthority parameter or the publicDataAuthority parameter, then *INDIR must be specified for both parameters.
     <li>"*RWX" - The user can change the object and perform basic functions on the object except those limited to the owner or controlled by object existence (*OBJEXIST), object management (*OBJMGT), object alter (*OBJALTER) and object reference (*OBJREF) authority.  Read, write, execute (*RWX) provides object operational (*OBJOPR) and all data authorities.
     <li>"*RW" - The user can view and change the contents of an object.  Read, write (*RW) authority provides *OBJOPR and data read (*READ), add (*ADD), update (*UPD) and delete (*DLT) authorities.
     <li>"*RX" - The user can perform basic operations on the object, such as run a program or display the contents of a file.  The user is prevented from changing the object.  Read, execute (*RX) authority provides *OBJOPR and data *READ and *EXECUTE authorities.
     <li>"*WX" - The user can change the contents of an object and run a program or search a library or directory.  Write, execute (*WX) authority provides *OBJOPR and data *READ, *UPD, *DLT, and *EXECUTE authorities.
     <li>"*R" - The user can view the contents of an object.  Read (*R) authority provides *OBJOPR and data *READ authorities.
     <li>"*W" - The user can change the contents of an object.  Write (*W) authority provides *OBJOPR and data *READ, *UPD, and *DLT authorities.
     <li>"*X" - The user can run a program or search a library or directory.  Execute (*X) authority provides *OBJOPR and data *EXECUTE authorities.
     <li>"*EXCLUDE" - The user cannot access the object.  The publicObjectAuthority parameter value must be *NONE, if this special value is used.
     <li>"*NONE" - The user is given no data authorities to the object.  This value cannot be used with publicObjectAuthority parameter value of *NONE.
     <li>authorization-list-name - The format of the authorization list name remains the current ten-character format.  The publicObjectAuthority parameter value must be *NONE, if this special value is used.
     </ul>
     @param  publicObjectAuthority  Specifies the public object authority given to users for the user-defined file system, or specifies that all authorities are inherited from the directory it is to be created in.  Possible values for the elements of this array are:
     <ul>
     <li>"*INDIR" - The object authority for the UDFS to be created is determined by the directory it is to be created in.  This means the new UDFS will inherit its primary group, authorization list, and its public, private and primary group authorities from the /dev/qaspXX or /dev/aspname directory.  If the value *INDIR is specified for either the publicObjectAuthority parameter or the publicDataAuthority parameter, then *INDIR must be specified for both parameters.  This value must be the only element of the array.
     <li>"*NONE" - None of the other object authorities (*OBJEXIST, *OBJMGT, *OBJALTER or *OBJREF) are given to the users.  If *EXCLUDE or an authorization list is specified for the publicDataAuthority parameter, *NONE must be specified.  This value cannot be used with the publicDataAuthority parameter value of *NONE.  This value must be the only element of the array.
     <li>"*ALL" - All of the other object authorities (*OBJEXIST, *OBJMGT, *OBJALTER or *OBJREF) are given to the users.  This value must be the only element of the array.
     <li>"*OBJEXIST" - The user is given object existence (*OBJEXIST) authority to the object.  The user can delete the object, free storage of the object, perform save and restore operations for the object, and transfer ownership of the object.
     <li>"*OBJMGT" - The user is given object management (*OBJMGT) authority to the object.  With this authority the user can specify security for the object, move or rename the object and add members to database files.
     <li>"*OBJALTER" - The user is given object alter (*OBJALTER) authority to the object.  The user is able to alter the attributes of the objects.  On a database file, the user can add and remove triggers, add and remove referential and unique constraints, and change the attributes of the database file.  With this authority on an SQL package, the user can change the attributes of the SQL package.  Currently, this authority is used only for database files and SQL packages.
     <li>"*OBJREF" - The user is given object reference (*OBJREF) authority to objects.  Used only for database files, the user can reference an object from another object such that operations on that object may be restricted by the other object.  On a physical file, the user can add a referential constraint in which the physical file is the parent.
     </ul>
     @param  auditingValue  Specifies the auditing value of root directory objects created in this user-defined file system.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The object auditing value for the objects in the UDFS is determined by the Create object auditing (QCRTOBJAUD) system value.
     <li>"*NONE" - Using or changing this object does not cause an audit entry to be sent to the security journal.
     <li>"*USRPRF" - The user profile of the user accessing this object is used to determine if an audit record is sent for this access.  The OBJAUD parameter of the Change User Auditing (CHGUSRAUD) command is used to turn on auditing for a specific user.
     <li>"*CHANGE" - All change accesses to this object by all users are logged.
     <li>"*ALL" - All change or read accesses to this object by all users are logged.
     </ul>
     @param  scanningOption  Specifies whether the root directory objects created in the user-defined file system will be scanned when exit programs are registered with any of the integrated file system scan-related exit points.
     <p>The integrated file system scan-related exit points are:
     <ul>
     <li>QIBM_QP0L_SCAN_OPEN - Integrated File System Scan on Open Exit Program
     <li>QIBM_QP0L_SCAN_CLOSE - Integrated File System Scan on Close Exit Program
     </ul>
     <p>For details on these exit points, see the System API Reference information in the i5/OS Information Center at http://www.ibm.com/systems/i/infocenter.
     <p>Even though this attribute can be set for user-defined file systems, only objects which are in *TYPE2 directories in that user-defined file system will actually be scanned, no matter what value is set for this attribute.
     <p>Possible values are:
     <ul>
     <li>"*PARENT" - The create object scanning attribute value for this user-defined file system is copied from the create object scanning attribute value of the parent directory.
     <li>"*YES" - After an object is created in the user-defined file system, the object will be scanned according to the rules described in the scan-related exit programs if the object has been modified or if the scanning software has been updated since the last time the object was scanned.
     <li>"*NO" - After an object is created in the user-defined file system, the object will not be scanned by the scan-related exit programs.
     <p>Note: If the Scan file systems control (QSCANFSCTL) value *NOPOSTRST is not specified when an object with this attribute is restored, the object will be scanned at least once after the restore.
     <li>"*CHGONLY" - After an object is created in the user-defined file system, the object will be scanned according to the rules described in the scan-related exit programs only if the object has been modified since the last time the object was scanned.  It will not be scanned if the scanning software has been updated.  This attribute only takes effect if the Scan file systems control (QSCANFSCTL) system value has *USEOCOATR specified.  Otherwise, it will be treated as if the attribute is *YES.
     <p>Note: If the Scan file systems control (QSCANFSCTL) value *NOPOSTRST is not specified when an object with this attribute is restored, the object will be scanned at least once after the restore.
     </ul>
     <p>This parameter is ignored on Version 5 Release 2 and earlier of i5/OS.
     @param  specialRestrictions  Specifies whether special restrictions apply for rename and unlink operations performed on objects within the root directory of the user-defined file system.  This attribute is equivalent to the S_ISVTX mode bit for this directory.  true if objects within the root directory of the user-defined file system may be renamed or unlinked only if one or more of the following are true for the user performing the operation:
     <ul>
     <li>The user is the owner of the object.
     <li>The user is the owner of the directory.
     <li>The user has all object (*ALLOBJ) special authority.
     </ul>
     <p>false if no additional restrictions for renaming or unlinking objects from the root directory of the user-defined file system.  This parameter is ignored on Version 5 Release 2 and earlier of i5/OS.
     @param  defaultDiskStorageOption  Specifies how auxiliary storage will be allocated by the system for the stream files (*STMF) created in this user-defined file system.  This option will be ignored for *TYPE1 stream files.  Possible values are:
     <ul>
     <li>"*NORMAL" - The auxiliary storage will be allocated normally.  That is, as additional auxiliary storage is required, it will be allocated in logically sized extents to accommodate the current space requirement, and anticipated future requirements, while minimizing the number of disk I/O operations.
     <li>"*MINIMIZE" - The auxiliary storage will be allocated to minimize the space used by the object.  That is, as additional auxiliary storage is required, it will be allocated in small sized extents to accommodate the current space requirement.  Accessing an object composed of many small extents may increase the number of disk I/O operations for that object.
     <li>"*DYNAMIC" - The system will dynamically determine the optimal auxiliary storage allocation for the object, balancing space used versus disk I/O operations.  For example, if a file has many small extents, yet is frequently being read and written, then future auxiliary storage allocations will be larger extents to minimize the number of disk I/O operations.  Or, if a file is frequently truncated, then future auxiliary storage allocations will be small extents to minimize the space used.  Additionally, information will be maintained on the stream file sizes for this system and its activity.  This file size information will also be used to help determine the optimal auxiliary storage allocations for this object as it relates to the other objects' sizes.
     </ul>
     <p>This parameter is ignored on Version 5 Release 4 and earlier of i5/OS.
     @param  defaultMainStorageOption  Specifies how main storage is allocated and used by the system for the stream files (*STMF) created in this user-defined file system.  Possible values are:
     <ul>
     <li>"*NORMAL" - The main storage will be allocated normally.  That is, as as much main storage as possible will be allocated and used.  This minimizes the number of disk I/O operations since the information is cached in main storage.
     <li>"*MINIMIZE" - The main storage will be allocated to minimize the space used by the object.  That is, as little main storage as possible will be allocated and used.  This minimizes main storage usage while increasing the number of disk I/O operations since less information is cached in main storage.
     <li>"*DYNAMIC" - The system will dynamically determine the optimal main storage allocation for the object depending on other system activity and main storage contention.  That is, when there is little main storage contention, as much storage as possible will be allocated and used to minimize the number of disk I/O operations.  When there is significant main storage contention, less main storage will be allocated and used to minimize the main storage contention.  This option only has an effect when the storage pool's paging option is *CALC.  When the storage pool's paging option is *FIXED, the behavior is the same as *NORMAL.  When the object is accessed through a file server, this option has no effect.  Instead, its behavior is the same as *NORMAL.
     </ul>
     <p>This parameter is ignored on Version 5 Release 4 and earlier of i5/OS.
     @param  caseSensitivity  Specifies the case sensitivity of this file system.  Possible values are:
     <ul>
     <li>"*MONO" - The file system will not be case sensitive.  For example, the names FileA and filea refer to the same object.
     <li>"*MIXED" - The file system will be case sensitive.  For example, the names FileA and filea do NOT refer to the same object.
     </ul>
     @param  defaultFileFormat  Specifies the format of stream files (*STMF) created in this user-defined file system.  Possible values are:
     <ul>
     <li>"*TYPE2" - A *TYPE2 *STMF has high performance file access and was new in Version 4 Release 4 of i5/OS.  It has a minimum object size of 4096 bytes and a maximum object size of approximately 1 terabyte.  A *TYPE2 stream file is capable of memory mapping as well as the ability to specify an attribute to optimize disk storage allocation.
     <li>"*TYPE1" - A *TYPE1 *STMF has the same format as *STMF objects created on releases prior to Version 4 Release 4 of i5/OS.  It has a minimum size of 4096 bytes and a maximum object size of approximately 256 gigabytes.
     </ul>
     @param  description  Text description for the user-defined file system.  Possible values are:
     <ul>
     <li>"*BLANK" - Text is not specified.
     <li>description - Specify no more than 50 characters.
     </ul>
     **/
    public void create(String publicDataAuthority, String[] publicObjectAuthority, String auditingValue, String scanningOption, boolean specialRestrictions, String defaultDiskStorageOption, String defaultMainStorageOption, String caseSensitivity, String defaultFileFormat, String description) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Creating UDFS.");
        int vrm = system_.getVRM();
        runCommand("CRTUDFS UDFS('" + path_ + "') DTAAUT(" + publicDataAuthority + ") OBJAUT(" + setArrayToString(publicObjectAuthority) + ") CRTOBJAUD(" + auditingValue + ") " + (vrm < 0x00050300 ? "" : "CRTOBJSCAN(" + scanningOption + ") RSTDRNMUNL(" + (specialRestrictions ? "*YES" : "*NO") + ")") + (vrm < 0x00060100 ? "" : " DFTDISKSTG(" + defaultDiskStorageOption + ") DFTMAINSTG(" + defaultMainStorageOption + ")") + " CASE(" + caseSensitivity + ") DFTFILEFMT(" + defaultFileFormat + ") TEXT('" + description + "')");
    }

    /**
     Deletes an existing and unmounted user-defined file system (UDFS) and all of the objects within it.  The command will fail if the UDFS is mounted.
     <p>Restrictions:
     <ol>
     <li>The UDFS to be deleted must not be mounted.
     <li>Only a user with input/output (I/O) system configuration (*IOSYSCFG) special authority can specify this command.
     <li>The user must have object existence (*OBJEXIST) authority to all of the objects in the UDFS.
     </ol>
     **/
    public void delete() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Deleting UDFS.");
        runCommand("DLTUDFS UDFS('" + path_ + "')");
    }

    /**
     Makes the objects in a file system accessible to the integrated file system name space.  The directory that is the destination for the mount, the mountPoint parameter, must exist.  The mount will be performed with default options, read-write and setuid execution allowed.
     <p>Restrictions:
     <ol>
     <li>The user must have input/output (I/O) system configuration (*IOSYSCFG) special authority to use this command.
     <li>The user must have write (*W) authority to the directory to be mounted over.
     <li>The user must have execute (*X) authority to each directory in the path.
     </ol>
     @param  mountPoint  Specifies the path name of the existing directory that the file system will be mounted over.  This directory gets 'covered' by the mounted file system.  This directory must exist.
     <p>Multiple file systems can be mounted over the same directory, one on top of the other.  However, only the topmost mounted file system is accessible, and the file systems must later be unmounted in the opposite order from which they were mounted (last-in first-out order).
     **/
    public void mount(String mountPoint) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Mounting UDFS.");
        runCommand("ADDMFS TYPE(*UDFS) MFS('" + path_ + "') MNTOVRDIR('" + mountPoint + "')");
    }

    /**
     Makes the objects in a file system accessible to the integrated file system name space.  The directory that is the destination for the mount, the mountPoint parameter, must exist.
     <p>Restrictions:
     <ol>
     <li>The user must have input/output (I/O) system configuration (*IOSYSCFG) special authority to use this command.
     <li>The user must have write (*W) authority to the directory to be mounted over.
     <li>The user must have execute (*X) authority to each directory in the path.
     </ol>
     @param  mountPoint  Specifies the path name of the existing directory that the file system will be mounted over.  This directory gets 'covered' by the mounted file system.  This directory must exist.
     <p>Multiple file systems can be mounted over the same directory, one on top of the other.  However, only the topmost mounted file system is accessible, and the file systems must later be unmounted in the opposite order from which they were mounted (last-in first-out order).
     @param  rwOption  This option specifies the protection for the mounted file system.  true if read-write, false if read-only.
     @param  suidOption  This option specifies whether setuid execution is allowed.  true if suid is specified and setuid execution is allowed.  This means that bits other than the permission bits may be set.  false if nosuid is specified, setuid execution is not allowed.  This parameter is ignored on Version 5 Release 2 and earlier of i5/OS.
     **/
    public void mount(String mountPoint, boolean rwOption, boolean suidOption) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Mounting UDFS.");
        runCommand("ADDMFS TYPE(*UDFS) MFS('" + path_ + "') MNTOVRDIR('" + mountPoint + "') OPTIONS('" + (rwOption ? "rw," : "ro,") + (suidOption ? "suid" : "nosuid") + "')");
    }

    /**
     Makes a previously mounted file system inaccessible within the integrated file system name space.  If any of the objects in the file system are in use, the command will return an error message to the user.  Note that if any part of the file system has itself been mounted over, then this file system cannot be unmounted until it is uncovered.
     <p>Restrictions:
     <ol>
     <li>The user must have input/output (I/O) system configuration (*IOSYSCFG) special authority to use this command.
     **/
    public void unmount() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Unmounting UDFS.");
        runCommand("RMVMFS TYPE(*UDFS) MFS('" + path_ + "')");
    }

    // Convenience method for making a command string from array of strings.
    static private String setArrayToString(String[] array)
    {
        int arrayLength = array.length;
        if (arrayLength == 0) return "*INDIR";
        if (arrayLength == 1) return array[0];
        String string = array[0];
        for (int i = 1; i < arrayLength; ++i)
        {
            string += " " + array[i];
        }
        return string;
    }

    // Common code for running the commands.
    private void runCommand(String command) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        CommandCall cmd = new CommandCall(system_, command);
        // CRTUDFS, DLTUDFS, ADDMFS, RMVMFS are not thread safe.
        cmd.suggestThreadsafe(false);
        if (!cmd.run())
        {
            throw new AS400Exception(cmd.getMessageList());
        }
    }

    // Add IOCTL reply stream.
    static
    {
        AS400Server.addReplyStream(new IFSIoctlRep(), AS400.FILE);
    }

    // File descriptor object for communication with the file server.
    private IFSFileDescriptorImplRemote fd_ = new IFSFileDescriptorImplRemote();

    // Create the path name expected by the file server.
    private static byte[] createPathName(AS400 system, String path) throws IOException
    {
        path = path.replace('/', '\\');
        Converter conv = new Converter(1200, system);

        byte[] pathName = new byte[4 + path.length() * 2];
        BinaryConverter.intToByteArray(path.length() * 2, pathName, 0);
        conv.stringToByteArray(path, pathName, 4);
        return pathName;
    }

    /**
     Returns information about a UDFS.
     @return  Information about a UDFS.
     **/
    public UdfsInformationStructure getUdfsInformationStructure() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        fd_.system_ = (AS400ImplRemote)system_.getImpl();
        fd_.connect();
        DataStream baseReply = fd_.server_.sendAndReceive(new IFSIoctlReq(0x00000001, createPathName(system_, path_)));

        // Punt if unknown data stream.
        if (!(baseReply instanceof IFSIoctlRep))
        {
            Trace.log(Trace.ERROR, "Unknown reply datastream:", baseReply.data_);
            throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
        }

        IFSIoctlRep reply = (IFSIoctlRep)baseReply;

        // Get info from reply.
        return new UdfsInformationStructure(reply.getReplyData());
    }

    /**
     Returns information about a mounted file system.
     @return  Information about a mounted file system.
     **/
    public MountedFsInformationStructure getMountedFsInformationStructure() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        fd_.system_ = (AS400ImplRemote)system_.getImpl();
        fd_.connect();
        DataStream baseReply = fd_.server_.sendAndReceive(new IFSIoctlReq(0x00000004, createPathName(system_, path_)));

        // Punt if unknown data stream.
        if (!(baseReply instanceof IFSIoctlRep))
        {
            Trace.log(Trace.ERROR, "Unknown reply datastream:", baseReply.data_);
            throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
        }

        IFSIoctlRep reply = (IFSIoctlRep)baseReply;

        // Get info from reply.
        return new MountedFsInformationStructure(reply.getReplyData());
    }

    /**
     Contains information about a UDFS.
     **/
    public class UdfsInformationStructure
    {
        private String owner_;
        private int ccsid_;
        private String caseSensitivity_;
        private String description_;
        private String pathWhereMounted_;

        /**
         Returns the user profile name of the user who owns the user-defined file system (UDFS).
         @return  The user profile name of the user who owns the user-defined file system (UDFS).
         **/
        public String getOwner()
        {
            return owner_;
        }

        /**
         Returns the coded character set identifier (CCSID) for the data in the user-defined file system (UDFS).
         @return  The coded character set identifier (CCSID) for the data in the user-defined file system (UDFS).
         **/
        public int getCcsid()
        {
            return ccsid_;
        }

        /**
         Returns the case sensitivity of this file system.
         @return  The case sensitivity of this file system.  Possible values are:
         <ul>
         <li>"*MONO" - The file system will not be case sensitive.  For example, the names FileA and filea refer to the same object.
         <li>"*MIXED" - The file system will be case sensitive.  For example, the names FileA and filea do NOT refer to the same object.
         </ul>
         **/
        public String getCaseSensitivity()
        {
            return caseSensitivity_;
        }

        /**
         Returns the text description for the user-defined file system.
         @return  The text description for the user-defined file system.
         **/
        public String getDescription()
        {
            return description_;
        }

        /**
         Returns the path name where the user-defined file system (UDFS) is mounted.
         @return  The path name where the user-defined file system (UDFS) is mounted.
         **/
        public String getPathWhereMounted()
        {
            return pathWhereMounted_;
        }

        // Parse the returned information.
        private UdfsInformationStructure(byte[] replyData) throws ErrorCompletingRequestException, IOException
        {
            Converter convu = new Converter(1200, system_);
            Converter conve = new Converter(37, system_);

            int rc = BinaryConverter.byteArrayToInt(replyData, 0);
            if (rc != 0)
            {
                Trace.log(Trace.ERROR, "Get UDFS information structure was not successful, errno:", rc);
                throw new ErrnoException(system_, rc);
            }
            owner_ = conve.byteArrayToString(replyData, 4, 10).trim();
            ccsid_ = BinaryConverter.byteArrayToUnsignedShort(replyData, 14);
            caseSensitivity_ = replyData[16] == 0x00 ? "*MONO" : "*MIXED";
            int descriptionLength = replyData[19];
            description_ = conve.byteArrayToString(replyData, 21, descriptionLength).trim();
            pathWhereMounted_ = convu.byteArrayToString(replyData, 121, replyData.length - 121);
        }
    }

    /**
     Contains information about a mounted file system.
     **/
    public class MountedFsInformationStructure
    {
        private boolean idUdfs_;
        private boolean isMounted_;

        /**
         Returns whether the the path is a user-defined file system (UDFS).
         @return  true if the object is a user-defined file system (UDFS); false otherwise.
         **/
        public boolean isUdfs()
        {
            return idUdfs_;
        }

        /**
         Returns whether the the path is mounted.
         @return  true if the object is mounted; false otherwise.
         **/
        public boolean isMounted()
        {
            return isMounted_;
        }

        // Parse the returned information.
        private MountedFsInformationStructure(byte[] replyData) throws ErrorCompletingRequestException
        {
            int rc = BinaryConverter.byteArrayToInt(replyData, 0);
            if (rc != 0)
            {
                Trace.log(Trace.ERROR, "Get mounted FS information structure was not successful, errno:", rc);
                throw new ErrnoException(system_, rc);
            }
            idUdfs_ = replyData[4] == 0x00 ? false : true;
            isMounted_ = replyData[5] == 0x00 ? false : true;
        }
    }
}
