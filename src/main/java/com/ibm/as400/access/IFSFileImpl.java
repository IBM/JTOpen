///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2007 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
// @D7 - 07/25/2007 - Add allowSortedRequests to the listDirectoryDetails()
//                    method to resolve problem of issuing PWFS List Attributes 
//                    request with both "Sort" indication and "RestartByID" 
//                    which is documented to be an invalid combination.
// @D8 - 04/03/2008 - Add clearCachedAttributes() to clear impl cache attributes.
//                    
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

import java.io.IOException;

/**
 * Specifies the methods which the implementation objects for the IFSFile class need to support.
 **/
interface IFSFileImpl
{
    /**
     * Determines if the application can execute the integrated file system object represented by this object. If the
     * file does not exist, returns false.
     * 
     * @return true if the object exists and is executable by the application; false otherwise.
     **/
    boolean canExecute() throws IOException, AS400SecurityException;

    /**
     * Determines if the application can read from the integrated file system object represented by this object. Note
     * that IBM i directories are never readable; only files can be readable.
     * 
     * @return true if the object exists and is readable by the application; false otherwise.
     **/
    boolean canRead() throws IOException, AS400SecurityException;

    /**
     * Determines if the application can write to the integrated file system object represented by this object. Note
     * that IBM i directories are never writable; only files can be writable. 
     * 
     * @return true if the object exists and is writeable by the application; false otherwise.
     **/
    boolean canWrite() throws IOException, AS400SecurityException;

    /**
     * Clear the cached attributes. This is needed when cached attributes need to be refreshed.
     **/
    void clearCachedAttributes();

    /**
     * Copies the current file to the specified path.
     * 
     * @return true if successful copy; false otherwise.
     **/
    boolean copyTo(String path, boolean replace) throws IOException, AS400SecurityException, ObjectAlreadyExistsException;

    /**
     * Determines the time that the integrated file system object represented by this object was created.
     * 
     * @return time object was created.
     **/
    long created() throws IOException, AS400SecurityException;

    /**
     * If file does not exist, create it. If the file does exist, return an error. The goal is to atomically create a
     * new file if and only if the file does not yet exist.
     * 
     * @return 0 on success; an error code is returned on failure.
     **/
    int createNewFile() throws IOException, AS400SecurityException;

    /**
     * Deletes the integrated file system object represented by this object.
     * 
     * @return 0 on success; an error code is returned on failure.
     **/
    int delete() throws IOException, AS400SecurityException;

    /**
     * Determines if the integrated file system object represented by this object exists.
     * 
     * @return 0 if object exists; otherwise, an error code is returned.
     **/
    int exists() throws IOException, AS400SecurityException;

    /**
     * Determines the amount of unused storage space in the file system.
     * 
     * @param forUserOnly Whether to report only the space for the user. If false, report space in entire file system.
     * @return The number of bytes of storage available. Returns special value Long.MAX_VALUE if the system reports "no
     *         maximum".
     **/
    long getAvailableSpace(boolean forUserOnly) throws IOException, AS400SecurityException;

    /**
     * Determines the total amount of storage space in the file system.
     * 
     * @param forUserOnly Whether to report only the space for the user. If false, report space in entire file system.
     * @return The number of bytes of storage. Returns special value Long.MAX_VALUE if the system reports "no maximum".
     **/
    long getTotalSpace(boolean forUserOnly) throws IOException, AS400SecurityException;

    /**
     * Returns the file's data CCSID. All files in the system's integrated file system are tagged with a CCSID. This
     * method returns the value of that tag. If the file is non-existent, returns -1. If the file is a directory and the
     * authentication scheme is not password, returns -1. Returns the file's data CCSID. Returns -1 if failure or if
     * directory.
     **/
    int getCCSID() throws IOException, AS400SecurityException;
    int getCCSID(boolean retrieveAll) throws IOException, AS400SecurityException;


    /**
     * Returns the name of the user profile that is the owner of the file. 
     * 
     * @return object owner name, or zero-length string if unable to retrieve value. 
     **/
    String getOwnerName() throws IOException, AS400SecurityException;
    String getOwnerName(boolean retrieveAll) throws IOException, AS400SecurityException;
    String getOwnerNameByUserHandle(boolean forceRetrieve) throws IOException, AS400SecurityException;

    int getASP() throws IOException, AS400SecurityException;// @RDA @SAD

    String getFileSystemType() throws IOException, AS400SecurityException;

    /**
     * Returns the file's owner's "user ID" number. 
     * 
     * @return file UID, or -1 if an error occurred.
     **/
    long getOwnerUID() throws IOException, AS400SecurityException;

    /**
     * Returns the path of the integrated file system object that is directly pointed to by the symbolic link represented
     * by this object. Returns <tt>null</tt> if the file is not a symbolic link, does not exist, or is in an unsupported
     * file system.
     * <p>
     * This method is not supported for files in the following file systems:
     * <ul>
     * <li>QSYS.LIB
     * <li>Independent ASP QSYS.LIB
     * <li>QDLS
     * <li>QOPT
     * <li>QNTC
     * </ul>
     * 
     * @return The path directly pointed to by the symbolic link, or <tt>null</tt> if the IFS object is not a symbolic
     *         link or does not exist. Depending on how the symbolic link was defined, the path may be either relative or
     *         absolute.
     **/
    String getPathPointedTo() throws IOException, AS400SecurityException;

    /**
     * Retrieve object subtype.
     * 
     * @return Return object subtype. If object does not have a subtype, a zero length strength is returned.
     **/
    String getSubtype() throws IOException, AS400SecurityException;

    /**
     * Determines if the integrated file system object represented by this object is a directory.
     * 
     * @return 0 if object is a directory; otherwise, error code.
     **/
    int isDirectory() throws IOException, AS400SecurityException;

    /**
     * Determines if the integrated file system object represented by this object is a "normal" file.<br>
     * A file is "normal" if it is not a directory or a container of other objects.
     * 
     * @return 0 if object is a file; otherwise, error code.
     **/
    int isFile() throws IOException, AS400SecurityException;

    /**
     * Determines if the integrated file system object represented by this object has its hidden attribute set.
     * 
     * @return true if object is hidden; otherwise, false.
     **/
    boolean isHidden() throws IOException, AS400SecurityException;

    /**
     * Determines if the integrated file system object represented by this object has its hidden attribute set.
     * 
     * @return true if object is read-only; otherwise, false.
     **/
    boolean isReadOnly() throws IOException, AS400SecurityException;

    /**
     * Determines if the file is a "source physical file".
     * 
     * @return true if object is source physical file; otherwise, false.
     **/
    boolean isSourcePhysicalFile() throws IOException, AS400SecurityException, AS400Exception;

    /**
     * Determines if the integrated file system object represented by this object is a symbolic link.
     * 
     * @return true if symbolic link; otherwise, false.
     **/
    boolean isSymbolicLink() throws IOException, AS400SecurityException;

    /**
     * Determines the time that the integrated file system object represented by this object was last accessed.
     * 
     * @return time object was last accessed
     **/
    long lastAccessed() throws IOException, AS400SecurityException;

    /**
     * Determines the time that the integrated file system object represented by this object was last modified.
     * 
     * @return time object was last modified
     **/
    long lastModified() throws IOException, AS400SecurityException;

    /**
     * Determines the length of the integrated file system object represented by this object.
     * 
     * @return length of object.
     */
    long length() throws IOException, AS400SecurityException;

    /**
     * List the files/directories in the specified directory.
     * 
     * @param directoryPath
     * @return List of object names in specified directory, or null if specified file or directory does not exist.
     */
    String[] listDirectoryContents(String directoryPath) throws IOException, AS400SecurityException;


    /**
     *  List the files/directories details in the specified directory.
     *  
     *  @return List of object attributes, or null if specified file or directory does not exist.
     */
    IFSCachedAttributes[] listDirectoryDetails(String directoryPattern, String directoryPath, int maximumGetCount,
            String restartName) throws IOException, AS400SecurityException;

    /**
     *  List the files/directories details in the specified directory.
     *  
     *  @return List of object attributes, or null if specified file or directory does not exist.
     */
    IFSCachedAttributes[] listDirectoryDetails(String directoryPattern, String directoryPath, int maximumGetCount,
            byte[] restartID, boolean allowSortedRequests) throws IOException, AS400SecurityException;

    /**
     * Creates an integrated file system directory whose path name is specified by this object.
     * 
     * @param directory Path of directory to create.
     * @return 0 for success; otherwise, error return code.
     */
    int mkdir(String directory) throws IOException, AS400SecurityException;

    /**
     * Creates an integrated file system directory whose path name is specified by this object. In addition, create all
     * parent directories as necessary.
     * 
     * @return 0 for success; otherwise, error return code.
     **/
    int mkdirs() throws IOException, AS400SecurityException;

    /**
     * Renames the integrated file system object specified by this object to have the path name of <i>file</i>. Wildcards
     * are not permitted in this file name.
     * 
     * @param file The new file name.
     **/
    int renameTo(IFSFileImpl file) throws IOException, AS400SecurityException;

    /**
     * Sets the file's "data CCSID" tag.
     * 
     * @param ccsid the CCSID for the file.
     * @return true if successful; false otherwise.
     */
    boolean setCCSID(int ccsid) throws IOException, AS400SecurityException;

    boolean setAccess(int accessType, boolean enableAccess, boolean ownerOnly) throws IOException, AS400SecurityException;

    /**
     * Changes the fixed attributes (read only, hidden, etc.) of the integrated file system object represented by this
     * object to <i>attributes</i>.
     * 
     * @param attributes The set of attributes to apply to the object. Note these attributes are not ORed with the
     *                   existing attributes. They replace the existing fixed attributes of the file.
     * @return true if successful; false otherwise.
     **/
    boolean setFixedAttributes(int attributes) throws IOException, AS400SecurityException;

    /**
     * Alters the hidden attribute of the object. If <i>attribute</i> is true, the bit is turned on. If <i>attribute</i>
     * is turned off, the bit is turned off.
     * 
     * @param attribute The new state of the hidden attribute. The hidden attribute is the second bit from the right.
     * 
     * @return true if successful; false otherwise.
     **/
    boolean setHidden(boolean attribute) throws IOException, AS400SecurityException;

    /**
     * Changes the last modified time of the integrated file system object represented by this object to <i>time</i>.
     * 
     * @param time The desired last modification time (measured in milliseconds since January 1, 1970 00:00:00 GMT), or -1
     *             to set the last modification time to the current system time.
     * 
     * @return true if successful; false otherwise.
     **/
    boolean setLastModified(long time) throws IOException, AS400SecurityException;

    /**
     * Sets the length of the integrated file system object represented by this object. The file can be made larger or
     * smaller. If the file is made larger, the contents of the new bytes of the file are undetermined.
     * 
     * @param length The new length, in bytes.
     * @return true if successful; false otherwise.
     **/
    boolean setLength(int length) throws IOException, AS400SecurityException;

    /**
     * Sets the pattern-matching behavior used when files are listed by any of the <tt>list()</tt> or <tt>listFiles()</tt>
     * methods. The default is PATTERN_POSIX.
     * 
     * @param patternMatching Either {@link IFSFile#PATTERN_POSIX PATTERN_POSIX}, {@link IFSFile#PATTERN_POSIX_ALL
     *                        PATTERN_POSIX_ALL}, or {@link IFSFile#PATTERN_OS2 PATTERN_OS2}
     **/
    void setPatternMatching(int patternMatching);

    /**
     * Alters the read only attribute of the object. If <i>attribute</i> is true, the bit is turned on. If
     * <i>attribute</i> is turned off, the bit is turned off.
     * 
     * @param attribute The new state of the read only attribute
     * 
     * @return true if successful; false otherwise.
     **/
    boolean setReadOnly(boolean attribute) throws IOException, AS400SecurityException;

    /**
     * Sets the file path.
     * 
     * @param path The absolute file path.
     **/
    void setPath(String path);

    /**
     * Sets the sorting behavior used when files are listed by any of the <tt>list()</tt> or <tt>listFiles()</tt>
     * methods. The default is <tt>false</tt> (unsorted).
     * 
     * @param sort If <tt>true</tt>: Return lists of files in sorted order. If <tt>false</tt>: Return lists of files in
     *             whatever order the file system provides.
     **/
    void setSorted(boolean sort);

    /**
     * Sets the system.
     * 
     * @param system The server object.
     **/
    void setSystem(AS400Impl system);

    /**
     * Return object description.
     * 
     * @return The object description.
     */
    String getDescription() throws IOException, AS400SecurityException;
}

