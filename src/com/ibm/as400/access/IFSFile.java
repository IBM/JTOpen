///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFile.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.io.InterruptedIOException;
import java.io.IOException;
import java.io.UnsupportedEncodingException; //@A6A
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Vector;


/**
  * The IFSFile class represents
  * an object in the integrated file system.
  * As in java.io.File, IFSFile is designed to work
  * with the object as a whole.  For example, use IFSFile
  * to delete or rename a file, to access the
  * file's attributes (is the object a file or a directory,
  * when was the file last changed, is the file hidden, etc.),
  * or to list the contents of a directory.
  * Use {@link IFSFileInputStream IFSFileInputStream} or
  * {@link IFSRandomAccessFile IFSRandomAccessFile} to read
  * data from the file, and {@link IFSFileOutputStream IFSFileOutputStream} or
  * {@link IFSRandomAccessFile IFSRandomAccessFile}
  * to write data to the file.
  * Note that both {@link #isFile() isFile} and {@link #isDirectory() isDirectory}
  * will return false for invalid symbolic links.
  *
  * <p>
  * IFSFile objects are capable of generating file events that call the
  * following FileListener methods: fileDeleted() and fileModified().
  * <p>
  * The following example demonstrates the use of IFSFile:
  * <UL>
  * <pre>
  * // Work with /Dir/File.txt on the system myAS400.
  * AS400 as400 = new AS400("myAS400");
  * IFSFile file = new IFSFile(as400, "/Dir/File.txt");
  *
  * // Determine the parent directory of the file.
  * String directory = file.getParent();
  *
  * // Determine the name of the file.
  * String name = file.getName();
  *
  * // Determine the file size.
  * long length = file.length();
  *
  * // Determine when the file was last modified.
  * Date date = new Date(file.lastModified());
  *
  * // Delete the file.
  * if (file.delete() == false)
  * {
  *   // Display the error code.
  *   System.out.println("Unable to delete file.");
  * }
  * </pre>
  * </UL>
  *
  *<P>Note: Because of a host server restriction, you cannot access files in QTEMP.LIB.
  *
  * @see com.ibm.as400.access.FileEvent
  * @see #addFileListener(com.ibm.as400.access.FileListener)
  * @see #removeFileListener(com.ibm.as400.access.FileListener)
  * @see com.ibm.as400.access.IFSFileInputStream
  * @see com.ibm.as400.access.IFSFileOutputStream
  * @see com.ibm.as400.access.IFSRandomAccessFile
 **/

public class IFSFile
  implements java.io.Serializable, Comparable            // @B9c
{
  static final long serialVersionUID = 4L;

  /**
   The integrated file system path separator string used to separate paths in a path list.
   **/
  public final static String pathSeparator = ";";
  /**
   The integrated file system path separator character used to separate paths in a
   path list.
   **/
  public final static char pathSeparatorChar = ';';
  /**
   The integrated file system directory separator string used to separate
   directory/file components in a path.
   **/
  public final static String separator = "/";
  /**
   The integrated file system directory separator character used to separate
   directory/file components in a path.
   **/
  public final static char separatorChar = '/';
  /**
   Value for indicating that "POSIX" pattern-matching is used by the various <tt>list()</tt> and <tt>listFiles()</tt> methods.
   <br>Using POSIX semantics, all files are listed that match the pattern and do not begin with a period (unless the pattern begins with a period).  In that case, names beginning with a period are also listed.  Note that when no pattern is specified, the default pattern is "*".
   <br>Note: In OS/400 V5R1 and earlier, all files that match the pattern are listed, including those that begin with a period.
   **/
  public final static int PATTERN_POSIX = 0;
  final static int PATTERN_DEFAULT = PATTERN_POSIX;  // for use within package
  /**
   Value for indicating that "POSIX-all" pattern-matching is used by the various <tt>list()</tt> and <tt>listFiles()</tt> methods.
   <p>Using POSIX semantics, all files are listed that match the pattern, including those that begin with a period.
   **/
  public final static int PATTERN_POSIX_ALL = 1;
  /**
   Value for indicating that "OS/2" pattern-matching is used by the various <tt>list()</tt> and <tt>listFiles()</tt> methods.
   Using DOS semantics, all files are listed that match the pattern.
   **/
  public final static int PATTERN_OS2 = 2;

  private final static String SECURITY_EXCEPTION = "Security exception.";

  transient private PropertyChangeSupport changes_;
  transient private VetoableChangeSupport vetos_;
  transient private Vector fileListeners_;
  transient private IFSFileImpl impl_;

  private AS400 system_;
  private String path_ = "";  // Note: This is never allowed to be null.
  private Permission permission_; //@A6A

  //@D2C Changed IFSListAttrsRep to IFSCachedAttributes
  transient private IFSCachedAttributes cachedAttributes_;//@A7A
  private boolean isDirectory_; //@A7A
  private boolean isFile_; //@A7A
  private boolean isSymbolicLink_;
  private int patternMatching_;  // type of pattern matching to use when listing files
  private boolean sortLists_;    // whether file-lists are returned from the File Server in sorted order

  /**
   Constructs an IFSFile object.
   It creates a default IFSFile instance.
   **/
  public IFSFile()
  {
    initializeTransient();
  }


  /**
   Constructs an IFSFile object.
   It creates an IFSFile instance that represents the integrated file system
   object on <i>system</i> that has a path name of <i>directory</i>,  that is
   followed by the separator character and <i>name</i>.
   @param system The AS400 that contains the file.
   @param directory The directory.
   @param name The file name.
   **/
  public IFSFile(AS400   system,
                 IFSFile directory,
                 String  name)
  {
    // Validate arguments.
    if (system == null)
      throw new NullPointerException("system");
    if (directory == null)
      throw new NullPointerException("directory");
    else if (name == null)
      throw new NullPointerException("name");

    initializeTransient();

    // Build the file's full path name.
    path_ = directory.getAbsolutePath();
    if (path_.charAt(path_.length() - 1) != separatorChar)
    {
      // Append a separator character.
      path_ += separator;
    }
    path_ += name;

    system_ = system;

  // @A6A Add permission property.
    permission_ = null;
  }


  /**
   Constructs an IFSFile object.
   It creates an IFSFile instance that represents the integrated file system
   object on <i>system</i>  that has a  path name of <i>path</i>.
   @param system The AS400 that contains the file.
   @param path The file path name.
   **/
  public IFSFile(AS400  system,
                 String path)
  {
    // Validate arguments.
    if (system == null)
      throw new NullPointerException("system");
    else if (path == null)
      throw new NullPointerException("path");

    initializeTransient();

    // If the specified path doesn't start with the separator character,
    // add one.  All paths are absolute for IFS.
    if (path.length() == 0 || path.charAt(0) != separatorChar)
    {
      path_ = separator + path;
    }
    else
    {
      path_ = path;
    }

    system_ = system;
  }


  /**
   Constructs an IFSFile object.
   It creates an IFSFile instance that represents the integrated file system
   object on <i>system</i> that has a
   path name is of <i>directory</i>, followed by the separator character
   and <i>name</i>.
   @param system The AS400 that contains the file.
   @param directory The directory path name.
   @param name The file name.
   **/
  public IFSFile(AS400  system,
                 String directory,
                 String name)
  {
    // Validate arguments.
    if (system == null)
      throw new NullPointerException("system");
    else if (directory == null)
      throw new NullPointerException("directory");
    else if (name == null)
      throw new NullPointerException("name");

    initializeTransient();

    // Build the file's full path name.  Prepend a separator character
    // to the directory name if there isn't one already.  All paths
    // are absolute in IFS.
    if (directory.length() == 0 || directory.charAt(0) != separatorChar)
    {
      path_ = separator + directory;
    }
    else
    {
      path_ = directory;
    }
    if (path_.charAt(path_.length() - 1) != separatorChar)
    {
      // Append a separator character.
      path_ += separator;
    }
    path_ += name;

    system_ = system;
  }

  /**
   Constructs an IFSFile object.
   It creates an IFSFile instance that represents the integrated file system
   object on <i>system</i> that has a path name of <i>directory</i>,  that is
   followed by the separator character and <i>name</i>.
   @param system The AS400 that contains the file.
   @param directory The directory.
   @param name The file name.
   **/
   // @A4A
  public IFSFile(AS400   system,
                 IFSJavaFile directory,
                 String  name)
  {
    // Validate arguments.
    if (system == null)
      throw new NullPointerException("system");
    else if (directory == null)
      throw new NullPointerException("directory");
    else if (name == null)
      throw new NullPointerException("name");

    initializeTransient();

    // Build the file's full path name.
    path_ = directory.getAbsolutePath().replace (directory.separatorChar, separatorChar);
    if (path_.charAt(path_.length() - 1) != separatorChar)
    {
      // Append a separator character.
      path_ += separator;
    }
    path_ += name;

    system_ = system;
  }

  //@A7A  Added new IFSFile method to support caching file attributes.
  /**
   Constructs an IFSFile object.
   It creates an IFSFile instance that represents the integrated file system object
   on <i>system</i> that has a path name of <i>directory</i>, that is followed by the
   separator character and <i>name</i>.
   {@link #isDirectory() isDirectory} and {@link #isFile() isFile} will both return false
   for invalid symbolic links.
   @param system The AS400 that contains the file.
   @param attributes The attributes of the file.
   **/
  IFSFile(AS400 system, IFSCachedAttributes attributes)   //@D2C - Use IFSCachedAttributes
  {
    // Validate arguments.
    if (attributes == null)
      throw new NullPointerException("attributes");

    initializeTransient();

    String directory = attributes.getParent();  //@D2C - Use IFSCachedAttributes
    String name = attributes.getName();  //@D2C - Use IFSCachedAttributes

    // Build the file's full path name.  Prepend a separator character
    // to the directory name if there isn't one already.  All paths
    // are absolute in IFS.
    StringBuffer buff = new StringBuffer();
    if (directory.length() == 0 || directory.charAt(0) != separatorChar)
    {
       buff.append(separator).append(directory);
    }
    else
    {
       buff.append(directory);
    }
    if (buff.toString().charAt(buff.toString().length() - 1) != separatorChar)
    {
      // Append a separator character.
       buff.append(separator);
    }

    path_ = buff.append(name).toString();

    system_ = system;

    // Cache file attributes.
    cachedAttributes_ = attributes;
    //isDirectory and isFile can both be false if the object is an invalid
    //symbolic link.
    isDirectory_ = attributes.getIsDirectory();
    isFile_ = attributes.getIsFile();
    isSymbolicLink_ = attributes.isSymbolicLink();
  }


  /**
   Adds a file listener to receive file events from this IFSFile.
   @param listener The file listener.
   **/
  public void addFileListener(FileListener listener)
  {
    if (listener == null)
      throw new NullPointerException("listener");

      fileListeners_.addElement(listener);
  }

  /**
   Adds a property change listener.
   @param listener The property change listener to add.
   **/
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null)
      throw new NullPointerException("listener");

    changes_.addPropertyChangeListener(listener);
  }

  /**
   Adds a vetoable change listener.
   @param listener The vetoable change listener to add.
   **/
  public void addVetoableChangeListener(VetoableChangeListener listener)
  {
    if (listener == null)
      throw new NullPointerException("listener");

    vetos_.addVetoableChangeListener(listener);
  }

//internal version of canRead()
  int canRead0()
    throws IOException, AS400SecurityException
  {
    if (impl_ == null)
      chooseImpl();

    return impl_.canRead();
  }


  /**
   Determines if the applet or application can read from the integrated file system
   object represented by this object.
   Note that i5/OS <i>directories</i> are never readable; only <i>files</i> can be readable.
   @return true if the object exists and is readable by the application; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.
   **/
  public boolean canRead()
    throws IOException
  {
    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    try
    {
      returnCode = canRead0();
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      // returnCode = IFSReturnCodeRep.FILE_NOT_FOUND; // @A7D Unnecessary assignment
      ///throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED); // @B6a
    }
    return (returnCode == IFSReturnCodeRep.SUCCESS);
  }

//internal version of canWrite()
  int canWrite0()
    throws IOException, AS400SecurityException
  {
    if (impl_ == null)
      chooseImpl();

    return impl_.canWrite();
  }

  /**
   Determines if the applet or application can write to the integrated file system
   object represented by this object.
   Note that i5/OS <i>directories</i> are never writable; only <i>files</i> can be writable.
   @return true if the object exists and is writeable by the application; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

   **/
  public boolean canWrite()
    throws IOException
  {
    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    try
    {
      returnCode = canWrite0();
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      //returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;  //@A7D Unnecessary assignment.
      ///throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED); // @B6a
    }
    return (returnCode == IFSReturnCodeRep.SUCCESS);
  }

  /**
   Chooses the appropriate implementation.
   **/
  void chooseImpl()
    throws IOException, AS400SecurityException
  {
    if (impl_ == null)
    {
      // Ensure that the system has been set.
      if (system_ == null)
      {
        throw new ExtendedIllegalStateException("system",
                           ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }

      // Ensure that the path name is set.
      if (path_.length() == 0)
      {
        throw new ExtendedIllegalStateException("path",
                           ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }

      impl_ = (IFSFileImpl) system_.loadImpl2
        ("com.ibm.as400.access.IFSFileImplRemote",
         "com.ibm.as400.access.IFSFileImplProxy");
      system_.connectService(AS400.FILE);
      impl_.setSystem(system_.getImpl());
      impl_.setPath(path_);
    }
  }



// @D6A
  /**
  Clear the cached attributes.  This is needed when cached attributes
  need to be refreshed.           
        
  @see #listFiles  
  **/
  public void clearCachedAttributes()
  {
      cachedAttributes_ = null;
  }



// @B9a
/**
 * Compares the path of this IFSFile with an <code>Object</code>'s path.
 * If the other object is not an IFSFile or java.io.File,
 * this method throws a <code>ClassCastException</code>, since
 * IFSFile is comparable only to IFSFile and java.io.File.
 *
 * <p>Note:<br>The comparison is case sensitive.
 *
 * @param   obj The <code>Object</code> to be compared.
 *
 * @return  <code>0</code> if this IFSFile path equals the argument's path;
 *          a value less than <code>0</code> if this IFSFile path is less than the argument's
 *          path; and a value greater than <code>0</code> if this IFSFile path is greater
 *          than the argument's path.
 *
**/
  public int compareTo(Object obj)
  {
    if (obj instanceof IFSFile)
      return getPath().compareTo(((IFSFile)obj).getPath());
    else
      return getPath().compareTo(((java.io.File)obj).getPath());
  }


  /**
   * Copies this file or directory to the specified file or directory
   * on the system.
   * If the destination file already exists, it is overwritten.
   * If this IFSFile represents a directory:
   * <ul>
   * <li> The destination directory must be nonexistent, otherwise an exception is thrown.
   * <li> The entire directory (including all of its contents and subdirectories) is copied.
   * </ul>
   * @param path The destination path to copy this IFSFile to.
   * <br>If the system is V5R2 or earlier: If the current object is a file (rather than a directory), the destination path must also specify a file (rather than a directory), otherwise the copy may fail.
   * @return true if the copy succeeded (or at least one file of a source
   * directory's contents was copied); false otherwise.
  **/
  public boolean copyTo(String path) throws IOException, AS400SecurityException, ObjectAlreadyExistsException
  {
    return copyTo(path, true);
  }


  /**
   * Copies this file or directory to the specified file or directory
   * on the system.
   * If the destination file already exists:
   * <ul>
   * <li> If <i>replace</i> is true, the destination file is overwritten.
   * <li> If <i>replace</i> is false, an exception is thrown.
   * </ul>
   * If this IFSFile represents a directory:
   * <ul>
   * <li> The destination directory must be nonexistent, otherwise an exception is thrown.
   * <li> The entire directory (including all of its contents and subdirectories) is copied.
   * </ul>
   * @param path The destination path to copy this IFSFile to.
   * <br>Note: If the system is V5R2 or earlier: If the current object is a file (rather than a directory), the destination path must also specify a file (rather than a directory), otherwise the copy may fail.
   * @param replace true to overwrite the destination if it already exists, false otherwise.
   * <br>Note: If the system is V5R2 or earlier, this parameter has no effect; that is, the destination is always overwritten.
   * @return true if the copy succeeded (or at least one file of a source
   * directory's contents was copied); false otherwise.
  **/
  // Note: Don't make this method public for now.  The limitations of the 'replace'
  // option are too complex, in terms of what the File Server can do for us.
  // Wait until we have better support from the File Server.
  boolean copyTo(String path, boolean replace) throws IOException, AS400SecurityException, ObjectAlreadyExistsException
  {
    if (path == null) throw new NullPointerException("path");
    if (impl_ == null) chooseImpl();
    return impl_.copyTo(path, replace);
  }
    
    
//internal created method.  It throws a security exception.
  long created0()                                               //@D3a
    throws IOException, AS400SecurityException                  //@D3a
  {                                                             //@D3a
    //@A7A Added check for cached attributes.                   //@D3a
    if (cachedAttributes_ != null)                              //@D3a
    {                                                           //@D3a
       return cachedAttributes_.getCreationDate();              //@D3a
    }                                                           //@D3a
    else                                                        //@D3a
    {                                                           //@D3a
       if (impl_ == null)                                       //@D3a
         chooseImpl();                                          //@D3a
                                                                //@D3a
       return impl_.created();                                 //@D3a
    }                                                           //@D3a
  }                                                             //@D3a

  /**
   Determines the time that the integrated file system object represented by this
   object was created.
   @return The time (measured in milliseconds since 01/01/1970 00:00:00 GMT)
   that the integrated file system object was created, or 0L if
   the object does not exist or is not accessible.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

   **/
  public long created()                                         //D3a
    throws IOException                                          //D3a
  {                                                             //D3a
    try                                                         //D3a
    {                                                           //D3a
      return created0();                                        //D3a
    }                                                           //D3a
    catch (AS400SecurityException e)                            //D3a
    {                                                           //D3a
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);            //D3a
      //return 0L;                                              //D3a @B6d
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED); // @B6a
    }                                                           //D3a
  }                                                             //D3a








  /**
   Atomically creates a new, empty file named by this abstract pathname if and only if a file with this name does not yet exist.
   The check for the existence of the file and the creation of the file if it does not exist are a single operation that is atomic with respect to all other filesystem activities that might affect the file.
   If the file already exists, its data are left intact.
   @return <tt>true</tt> if the named file does not exist and was successfully created; <tt>false</tt> if the named file already exists.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.
   @exception IOException If the user is not authorized to create the file.
   **/
   // @D1 - new method because of changes to java.io.File in Java 2.

  public boolean createNewFile()
    throws IOException
  {
     int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
     try
     {
        if (impl_ == null)
          chooseImpl();

        returnCode = impl_.createNewFile();
     }
     catch (AS400SecurityException e)
     {
        Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
        throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED); // @B6a
     }
     return (returnCode == IFSReturnCodeRep.SUCCESS);
  }



//internal delete that returns a return code status indicator
  int delete0()
    throws IOException, AS400SecurityException
  {
    if (impl_ == null)
      chooseImpl();

    int rc = impl_.delete();

    // Verify that the request was successful.
    if (rc == IFSReturnCodeRep.SUCCESS)
    {
      // Fire the file deleted event.
      if (fileListeners_.size() != 0)
      {
        FileEvent event = new FileEvent(this, FileEvent.FILE_DELETED);
        synchronized(fileListeners_)
        {
          Enumeration e = fileListeners_.elements();
          while (e.hasMoreElements())
          {
            FileListener listener = (FileListener)e.nextElement();
            listener.fileDeleted(event);
          }
        }
      }

      // Clear any cached attributes.
      cachedAttributes_ = null;         //@A7a
    }

    return rc;
  }

  /**
   Deletes the integrated file system object represented by this object.

   @return true if the file system object is successfully deleted; false otherwise.
   Returns false if the file system object did not exist prior to the delete() or is not accessible.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

   **/
  public boolean delete()
    throws IOException
  {
    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    try
    {
      returnCode = delete0();
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      // returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;  //@A7D Unnecessary assignment.
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED); // @B6a
    }
    return (returnCode == IFSReturnCodeRep.SUCCESS);
  }


  // @D4A
   /**
   Lists the integrated file system objects in the directory represented by this
   object that satisfy <i>filter</i>.  The returned Enumeration contains an IFSFile
   object for each file or directory in the list.  The list is loaded incrementally,
   which will improve initial response time for large lists.  

   @param filter    A file object filter.
   @param pattern   The pattern that all filenames must match. Acceptable
                    characters are wildcards (*) and question marks (?).
   
   @return An Enumeration of IFSFile objects which represent the contents of the directory that satisfy the filter
   and pattern. This Enumeration does not include the current directory or the parent
   directory.  If this object does not represent a directory, 
   this object represents an empty directory, the filter or pattern does
   not match any files,  or the directory is not accessible, then an empty Enumeration is returned. The IFSFile object
   passed to the filter object has cached file attribute information. 
   
   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.
   **/
  public Enumeration enumerateFiles(IFSFileFilter filter, String pattern)
    throws IOException
  {
      // Validate arguments.  Note that we tolerate a null-valued 'filter'.
      if (pattern == null)
        throw new NullPointerException("pattern");

      try {
            return new IFSFileEnumeration(this, filter, pattern);
      }
      catch (AS400SecurityException e) {
            Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
            //return null;      // @B6d
            throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED); // @B6a
      }
  }


// @D4A
   /**
   Lists the integrated file system objects in the directory represented by this
   object.  The returned Enumeration contains an IFSFile
   object for each file or directory in the list.  The list is loaded incrementally,
   which will improve initial response time for large lists.  

   @param pattern   The pattern that all filenames must match. Acceptable
                    characters are wildcards (*) and question marks (?).
   
   @return An Enumeration of IFSFile objects which represent the contents of the directory.
   This Enumeration does not include the current directory or the parent
   directory.  If this object does not represent a directory,
   this object represents an empty directory, the pattern does
   not match any files, or the directory is not accessible, then an empty Enumeration is returned. 

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.
   **/
  public Enumeration enumerateFiles(String pattern)
    throws IOException
  {
      // Validate arguments.  Note that we tolerate a null-valued 'filter'.
      if (pattern == null)
        throw new NullPointerException("pattern");

      try {
        return new IFSFileEnumeration(this, null, pattern);
      }
      catch (AS400SecurityException e) {
            Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
            //return null;  // @B6d
            throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED); // @B6a
      }
  }


// @D4A
   /**
   Lists the integrated file system objects in the directory represented by this
   object that satisfy <i>filter</i>.  The returned Enumeration contains an IFSFile
   object for each file or directory in the list.  The list is loaded incrementally,
   which will improve initial response time for large lists.  

   @param filter    A file object filter.
   
   @return An Enumeration of IFSFile objects which represent the contents of the directory.
   This Enumeration does not include the current directory or the parent
   directory.  If this object does not represent a directory, 
   this object represents an empty directory, the filter does
   not match any files, or the directory is not accessible, then an empty Enumeration is returned. The IFSFile object
   passed to the filter object has cached file attribute information. 

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.
   **/
  public Enumeration enumerateFiles(IFSFileFilter filter)
    throws IOException
  {
      try {
        return new IFSFileEnumeration(this, filter, "*");
      }
      catch (AS400SecurityException e) {
            Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
            // return null;  // @B6d
            throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED); // @B6a
      }
  }


// @D4A
   /**
   Lists the integrated file system objects in the directory represented by this
   object.  The returned Enumeration contains an IFSFile
   object for each file or directory in the list.  The list is loaded incrementally,
   which will improve initial response time for large lists.  

   @return An Enumeration of IFSFile objects which represent the contents of the directory.
   This Enumeration does not include the current directory or the parent
   directory.  If this object does not represent a directory,
   this object represents an empty directory, the filter or pattern does
   not match any files,  or the directory not accessible, then an empty Enumeration is returned.  

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.
   **/
  public Enumeration enumerateFiles()
    throws IOException
  {
      try {
        return new IFSFileEnumeration(this, null, "*");
      }
      catch (AS400SecurityException e) {
            Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
            // return null;  // @B6d
            throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED); // @B6a
      }
  }



  /**
   Determines if two IFSFile objects are equal.
   @param obj The object with which to compare.
   @return true if the path name and system names of the objects are equal;
           false otherwise.
   **/
  public boolean equals(Object obj)
  {
    if (obj == null || !(obj instanceof IFSFile))  // @A8C
      return false;

    // Determine the system name and path name.
    IFSFile target = (IFSFile) obj;
    String targetPathName = target.getPath();
    AS400 targetSystem = target.getSystem();
    String targetSystemName = null;
    if (targetSystem != null)
    {
      targetSystemName = targetSystem.getSystemName();
    }

    // @A8D
    //return (system_ != null && targetSystemName != null &&
    //        path_.equals(targetPathName) &&
    //        system_.getSystemName().equals(targetSystemName));

    // @A8A:
    boolean result = true;
    // It's OK for *neither* or *both* objects have system==null.
    if (system_ == null)
      result = (targetSystemName == null);
    else
      result = system_.getSystemName().equals(targetSystemName);
    result = result && (path_.equals(targetPathName));
    return result;
  }

//internal exists that returns a return code status indicator
  int exists0()
    throws IOException, AS400SecurityException
  {
    // Assume the argument has been validated as non-null.

    if (impl_ == null)
      chooseImpl();

    return impl_.exists();
  }

  /**
   Determines if the integrated file system object represented by this object exists.
   @return true if the object exists; false if the object does not exist or is not accessible.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

   **/
  public boolean exists()
    throws IOException
  {
    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    try
    {
      returnCode = exists0();
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      // returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;  //@A7D Unnecessary assignment.
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED); // @B6a
    }
    return (returnCode == IFSReturnCodeRep.SUCCESS);
  }



  /**
   Returns the path name of the integrated file system object represented by
   this object.  This is the full path starting at the root directory.
   @return The absolute path name of this integrated file system object.
   **/
  public String getAbsolutePath()
  {
    return path_;
  }


  /**
   Returns the path name of the integrated file system object represented by
   this object.  This is the full path starting at the root directory.
   @return The canonical path name of this integrated file system object.
   **/
  public String getCanonicalPath()
  {
    return path_;
  }


  //@A9a
  /**
   Returns the file's data CCSID.  All files in the system's integrated file system
   are tagged with a CCSID.  This method returns the value of that tag.
   If the file is non-existent or is a directory, returns -1.
   @return The file's CCSID.
   @exception IOException If an error occurs while communicating with the system.
   **/
  public int getCCSID()
    throws IOException
  {
    int result = -1;
    try
    {
      if (impl_ == null)
        chooseImpl();

      result = impl_.getCCSID();
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }
    return result;
  }


 //@A6A
 /**
   * Returns the full path of the object.
   * @return The full path of the object.
   *
  **/
  public String getFileSystem()
  {
    return getPath();
  }



  /**
   Determines the amount of unused storage space that is available to the user.
   @return The number of bytes of storage available.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

   **/
  public long getFreeSpace()
    throws IOException
  {
    try
    {
      if (impl_ == null)
        chooseImpl();

      return impl_.getFreeSpace();
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }
  }

  IFSFileImpl getImpl()
    throws IOException, AS400SecurityException
  {
    if (impl_ == null)
      chooseImpl();
    return impl_;
  }


  /**
   Determines the name of the integrated file system object represented by this object.
   @return The name (without directory components).
   **/
  public String getName()
  {
    String name = null;

    // The name is everything after the last occurrence of the
    // file separator character.
    // (Unless quoted, e.g. /parent/"/filename", in which case the name is "/filename", quotes included.)
    int index = path_.lastIndexOf(separatorChar);
    if (index >= 0)
    {
      if (index < path_.length() - 1)
      {
        name = path_.substring(index + 1);
      }
      else
      {
        name = "";
      }
    }
    else
    {
      name = path_;
    }

    // If name contains exactly 1 quote, and there's another quote earlier in the path, back up to the separator character before the prior quote.
    int quoteIndex = name.indexOf('"');
    if (quoteIndex >= 0)  // name includes at least 1 quote
    {
      if (quoteIndex == name.lastIndexOf('"')) // we only captured 1 quote
      {
        int priorQuoteIndex = path_.lastIndexOf('"', index);
        if (priorQuoteIndex >= 0)  // there's an earlier quote in path
        {
          int priorSeparatorIndex = path_.lastIndexOf(separatorChar, priorQuoteIndex);
          if (priorSeparatorIndex >= 0) // there's a separator prior to 1st quote
          {
            name = path_.substring(priorSeparatorIndex+1);
          }
          else // no separator, so back up to beginning of path
          {
            name = path_;
          }
        }
      }
    }

    return name;
  }


  /**
   Returns the name of the user profile that is the owner of the file.
   @return The name of the user profile that owns the file, or "" if owner cannot be determined (for example, if the file is a directory).

   @exception AS400SecurityException If a security or authority error occurs.
   @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   @exception InterruptedException If this thread is interrupted.
   @exception IOException If an error occurs while communicating with the system.
   @exception ExtendedIOException If the file does not exist.
   **/
  public String getOwnerName()
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException
  {
    // Design note: The File Server doesn't support changing the owner name.
    // It only supports retrieving it.  Hence we do not provide a setOwnerName() method.

    if (impl_ == null)
      chooseImpl();

    return impl_.getOwnerName();
  }


  // @C0a
  /**
   Returns the "user ID number" of the owner of the integrated file system file.
   If the file is non-existent or is a directory, returns -1.
   @return The file owner's ID number.
   @exception IOException If an error occurs while communicating with the system.
   **/
  public long getOwnerUID()
    throws IOException
  {
    // Design note: It would be preferable if we could report the user profile name instead of UID number.  However, the File Server doesn't report the user name.  FYI: There is a service program (QSYPAPI/getpwuid()) that reports the user profile name associated with a given uid number, but getpwuid returns its results as a pointer to a struct, which our ServiceProgramCall class can't handle at the moment.
    long result = -1L;
    try
    {
      if (impl_ == null)
        chooseImpl();

      result = impl_.getOwnerUID();
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }
    return result;
  }


  // @B7a
  /**
   Returns the "user ID number" of the owner of the integrated file system file.
   If the file is non-existent or is a directory, returns -1.
   @return The file owner's ID number.
   @exception IOException If an error occurs while communicating with the system.
   @deprecated Use getOwnerUID() instead.
   **/  // @C0c
  public int getOwnerId()
    throws IOException
  {
    // Design note: It would be preferable if we could report the user profile name instead of UID number.  However, the File Server doesn't report the user name.  FYI: There is a service program (QSYPAPI/getpwuid()) that reports the user profile name associated with a given uid number, but getpwuid returns its results as a pointer to a struct, which our ServiceProgramCall class can't handle at the moment.  - 1 Feb 2001
    int result = -1;
    try
    {
      if (impl_ == null)
        chooseImpl();

      result = (int)impl_.getOwnerUID();   // @C0c
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }
    return result;
  }

  /**
   Returns the parent directory of the integrated file system object
   represented by this object. The parent directory is everything in
   the path name before the last occurrence of the separator character,
   or null if the separator character does not appear in the path name.
   @return The parent directory.
   **/
  public String getParent()
  {
    return getParent(path_); // Note: path_ is never allowed to be null.
  }


  // Returns the parent directory of the file represented by this object.
  static String getParent(String directory)
  {
    String parent = null;

    // The parent directory is everything in the pathname before
    // the last occurrence of the file separator character.
    if (!directory.equals(separator))
    {
      int index = directory.lastIndexOf(separatorChar);
      if (index <= 0)
      {
        // This object is in the root.
        parent = separator;
      }
      else if (index == directory.length() - 1)
      {
        // The path has a trailing separator, ignore it.
        return getParent(directory.substring(0, directory.length() - 1));
      }
      else
      {
        parent = directory.substring(0, index);
      }
    }

    return parent;
  }


  /**
   Returns the path of the integrated file system object represented by this object.
   @return The integrated file system path name.
   **/
  public String getPath()
  {
    return path_;
  }


  /**
   Returns the pattern-matching behavior used when files are listed by any of the <tt>list()</tt> or <tt>listFiles()</tt> methods.  The default is PATTERN_POSIX.
   @return Either {@link #PATTERN_POSIX PATTERN_POSIX}, {@link #PATTERN_POSIX_ALL PATTERN_POSIX_ALL}, or {@link #PATTERN_OS2 PATTERN_OS2}
   **/
  public int getPatternMatching()
    throws IOException
  {
    return patternMatching_;
  }


 // @A6A
 /**
   * Returns the permission of the object.
   * @return The permission of the object.
   * @see #setPermission
   * @exception AS400Exception If the system returns an error message.
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with the system.
   * @exception ObjectDoesNotExistException If the object does not exist on the system.
   * @exception UnknownHostException If the system cannot be located.
   *
  **/
  public Permission getPermission()
         throws AS400Exception,
                AS400SecurityException,
                ConnectionDroppedException,
                ErrorCompletingRequestException,
                InterruptedException,
                ObjectDoesNotExistException,
                IOException,
                UnsupportedEncodingException

  {
    if (permission_ == null)
    {
      permission_ = new Permission(this);
    }
    return permission_;
  }


// @C3a
  // Returns the Restart ID associated with the file, or null if no attributes have been cached.
  byte[] getRestartID()
  {
    if (cachedAttributes_ != null)
    {
      return cachedAttributes_.getRestartID();
    }
    else
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR,
                                    "IFSFile.getRestartID() was called when cachedAttributes_==null.");
      return null;
    }
  }


// @B5a
  /**
   Returns the subtype of the integrated file system object represented by this object.  Some possible values that might be returned include:<br>
   CMNF, DKTF, DSPF, ICFF, LF, PF, PRTF, SAVF, TAPF.<br>
   Note that many file system objects do not have a subtype: for example,
   <tt>.MBR</tt> objects, and any Root, QOpenSys or UDFS object.
   <br>Returns a zero-length string if the object has no subtype.
   @return The subtype of the object.

   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception AS400SecurityException If a security or authority error occurs.
   **/
  public String getSubtype()
    throws IOException, AS400SecurityException
  {
    if (impl_ == null)
    {
      chooseImpl();
    }
    return impl_.getSubtype();
  }


  /**
   Returns the system that this object references.
   @return The system object.
   **/
  public AS400 getSystem()
  {
    return system_;
  }

  /**
   Computes a hash code for this object.
   @return A hash code value for this object.
   **/
  public int hashCode()
  {
    // @A3 - We need to be about to compute a hash code even when
    //       the path is not set.  This ends up getting called
    //       when serializing the object via MS Internet Explorer.
    //       As a result, we were not able to serialize an object
    //       unless its path was set.
    //
    //       I commented out this chunk of code:
    //
    // // Ensure that the path name is set.
    // if (path_.length() == 0)
    // {
    //   throw new ExtendedIllegalStateException("path",
    //                                           ExtendedIllegalStateException.PROPERTY_NOT_SET);
    // }

    return path_.hashCode();
  }

   /**
   Provided to initialize transient data if this object is de-serialized.
   **/
   private void initializeTransient()
   {
     changes_ = new PropertyChangeSupport(this);
     vetos_ = new VetoableChangeSupport(this);
     fileListeners_ = new Vector();

     cachedAttributes_ = null;
     impl_ = null;
   }

  /**
   Determines if the path name of this integrated file system object is an
   absolute path name.
   @return true if the path name specification is absolute; false otherwise.
   **/
  public boolean isAbsolute()
  {
    // Ensure that the path name is set.
    if (path_.length() == 0)
    {
      throw new ExtendedIllegalStateException("path",
                                   ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    return (path_.length() > 0 && path_.charAt(0) == '/');
  }

//internal isDirectory that returns a return code status indicator
  int isDirectory0()
    throws IOException, AS400SecurityException
  {
    //@A7A Added check for cached attributes.
    if (cachedAttributes_ != null)
    {
       if (isDirectory_)
          return IFSReturnCodeRep.SUCCESS;
       else
          return IFSReturnCodeRep.FILE_NOT_FOUND;
    }
    else
    {
       if (impl_ == null)
         chooseImpl();

       return impl_.isDirectory();
    }
  }

  /**
   Determines if the integrated file system object represented by this object is a
   directory.<br>
   Both isDirectory() and {@link #isFile() isFile} will return false
   for invalid symbolic links.

   @return true if the integrated file system object exists and is a directory; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

  **/
  public boolean isDirectory()
    throws IOException
  {
    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    try
    {
      returnCode = isDirectory0();
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      // returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;  //@A7D Unnecessary assignment.
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED); // @B6a
    }
    return (returnCode == IFSReturnCodeRep.SUCCESS);
  }

//internal isFile that returns a return code status indicator
  int isFile0()
    throws IOException, AS400SecurityException
  {
    //@A7A Added check for cached attributes.
    if (cachedAttributes_ != null)
    {
       if (isFile_)
          return IFSReturnCodeRep.SUCCESS;
       else
          return IFSReturnCodeRep.FILE_NOT_FOUND;
    }
    else
    {
       if (impl_ == null)
         chooseImpl();

       return impl_.isFile();
    }
  }

  /**
   Determines if the integrated file system object represented by this object is a
   "normal" file.<br>
   A file is "normal" if it is not a directory or a container of other objects. <br>
   Both {@link #isDirectory() isDirectory} and isFile() will return false
   for invalid symbolic links.

   @return true if the specified file exists and is a "normal" file; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

   **/
  public boolean isFile()
    throws IOException
  {
    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    try
    {
      returnCode = isFile0();
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      // returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;   //@A7D Unnecessary assignment.
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED); // @B6a
    }
    return (returnCode == IFSReturnCodeRep.SUCCESS);
  }

  /**
   Determines if the integrated file system object represented by this object is hidden.

   @return true if the hidden attribute of this integrated file system object
   is set; false otherwise.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.
  **/
   // @D1 - new method because of changes to java.io.File in Java 2.
  public boolean isHidden()
    throws IOException, AS400SecurityException
  {
    // If the attributes are cached from an earlier listFiles() operation
    // use the cached attributes instead of getting a new set from the system.
    if (cachedAttributes_ != null)
       return (cachedAttributes_.getFixedAttributes() &
                                 IFSCachedAttributes.FA_HIDDEN) != 0;
    //@D2C - Changed IFSListAttrsRep to IFSCached Attributes
    else
    {
       if (impl_ == null)
         chooseImpl();

       return impl_.isHidden();
    }
  }

  /**
   Determines if the integrated file system object represented by this object is a
   symbolic link.<br>
   A file is "normal" if it is not a directory or a container of other objects. <br>
   Note: Both {@link #isDirectory() isDirectory} and {@link #isFile() isFile} resolve symbolic links to their ultimate destination.  For example, if this object represents a symbolic link on the system, that resolves to a file object, then isSymbolicLink() will return true, isFile() will return true, and isDirectory() will return false. 
   <br>Note: If the system is V5R2 or earlier, this method always returns false, regardless of whether the system object is a link or not.

   @return true if the specified file exists and is a symbolic link; false otherwise.
   <br>If the system is V5R2 or earlier, this method always returns false.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

   **/
  public boolean isSymbolicLink()
    throws IOException, AS400SecurityException
  {
    if (cachedAttributes_ != null) {
       return isSymbolicLink_;
    }
    else
    {
       if (impl_ == null)
         chooseImpl();

       return impl_.isSymbolicLink();
    }
  }


  /**
   Determines if the integrated file system object represented by this object is read only.

   @return true if the read only attribute of this integrated file system object
   is set; false otherwise.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.
  **/
   // @D1 - new method because of changes to java.io.File in Java 2.

  public boolean isReadOnly()
    throws IOException, AS400SecurityException
  {
    // If the attributes are cached from an earlier listFiles() operation
    // use the cached attributes instead of getting a new set from the system.
    if (cachedAttributes_ != null)
       return (cachedAttributes_.getFixedAttributes() &
                                 IFSCachedAttributes.FA_READONLY) != 0;
    //@D2C - Changed IFSListAttrsRep to IFSCached Attributes
    else
    {
       if (impl_ == null)
         chooseImpl();

       return impl_.isReadOnly();
    }
  }


  /**
   Returns the sorting behavior used when files are listed by any of the <tt>list()</tt> or <tt>listFiles()</tt> methods.
   @return <tt>true</tt> if lists of files are returned in sorted order; <tt>false</tt> otherwise.
   **/
  public boolean isSorted()
    throws IOException
  {
    return sortLists_;
  }


  /**
   Determines the time that the integrated file system object represented by this
   object was last accessed. With the use of the {@link #listFiles() listFiles} methods, attribute
   information is cached and will not be automatically refreshed from the system.
   This means the reported last accessed time may become inconsistent with the system.
   @return The time (measured in milliseconds since 01/01/1970 00:00:00 GMT)
   that the integrated file system object was last accessed, or 0L if
   the object does not exist or is not accessible.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

   **/
  public long lastAccessed()                                    //D3a
    throws IOException                                          //D3a
  {                                                             //D3a
    try                                                         //D3a
    {                                                           //D3a
      //@A7A Added check for cached attributes.                 //@D3a
      if (cachedAttributes_ != null)                            //@D3a
      {                                                         //@D3a
        return cachedAttributes_.getAccessDate();               //@D3a
      }                                                         //@D3a
      else                                                      //@D3a
      {                                                         //@D3a
        if (impl_ == null)                                      //@D3a
          chooseImpl();                                         //@D3a
        //@D3a
        return impl_.lastAccessed();                            //@D3a
      }                                                         //@D3a
    }                                                           //D3a
    catch (AS400SecurityException e)                            //D3a
    {                                                           //D3a
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);            //D3a
      //return 0L;                                              //D3a @B6d
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED); // @B6a
    }                                                           //D3a
  }                                                             //D3a





//internal  lastModified that throws a security exception
  long lastModified0()
    throws IOException, AS400SecurityException
  {
    //@A7A Added check for cached attributes.
    if (cachedAttributes_ != null)
    {
       return cachedAttributes_.getModificationDate();
    }
    else
    {
       if (impl_ == null)
         chooseImpl();

       return impl_.lastModified();
    }
  }

  /**
   Determines the time that the integrated file system object represented by this
   object was last modified. With the use of the {@link #listFiles() listFiles} methods, attribute
   information is cached and will not be automatically refreshed from the system.
   This means the reported last modified time may become inconsistent with the system.
   @return The time (measured in milliseconds since 01/01/1970 00:00:00 GMT)
   that the integrated file system object was last modified, or 0L if it does not exist
    or is not accessible.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

   **/
  public long lastModified()
    throws IOException
  {
    try
    {
      return lastModified0();
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      //return 0L;  // @B6d
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED); // @B6a
    }
  }

//internal length that throws a security exception
  long length0()
    throws IOException, AS400SecurityException
  {
    //@A7A Added check for cached attributes.
    if (cachedAttributes_ != null)
    {
       return cachedAttributes_.getSize();
    }
    else
    {
       if (impl_ == null)
         chooseImpl();

       return impl_.length();
    }
  }

  /**
   Determines the length of the integrated file system object represented by this
   object.  With the use of the {@link #listFiles() listFiles} methods, attribute
   information is cached and will not be automatically refreshed from the system.
   This means the reported length may become inconsistent with the system.
   @return The length, in bytes, of the integrated file system object, or
   0L if it does not exist  or is not accessible.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

   **/
  public long length()
    throws IOException
  {
    try
    {
       return length0();
    }
    catch (AS400SecurityException e)
    {
       Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
       // return 0L;  // @B6d
       throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED); // @B6a
    }
  }

  /**
   Lists the integrated file system objects in the directory represented by this object.

   @return An array of object names in the directory. This list does not
   include the current directory or the parent directory.  If this object
   does not represent a directory, or the directory is not accessible, null is returned.  If this object represents
   an empty directory, an empty string array is returned.
   <br>Note: Due to a limitation in the File Server, at most 65,535 files will be listed.

   @see #listFiles()
   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

   **/
  public String[] list()
    throws IOException
  {
    return list("*");
  }

  /**
   Lists the integrated file system objects in the directory represented by this
   object that satisfy <i>filter</i>.
   @param filter A file object filter.  If null, then no filtering is done.
   @return An array of object names in the directory that satisfy the filter. This
   list does not include the current directory or the parent directory.  If this
   object does not represent a directory,  or the directory is not accessible, null is returned. If this object
   represents an empty directory, or the filter does not match any files,
   an empty string array is returned. The IFSFile object passed to the filter
   object have cached file attribute information.  Maintaining references to
   these IFSFile objects after the list operation increases the chances that
   their file attribute information will not be valid.
   <br>Note: Due to a limitation in the File Server, at most 65,535 files will be listed.

   @see #listFiles(IFSFileFilter)
   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

   **/
  public String[] list(IFSFileFilter filter)
    throws IOException
  { // @A5D Removed null filter check (null means no filtering)
     return list(filter, "*");
  }


  /**
   Lists the integrated file system objects in the directory represented by this object
   that satisfy <i>filter</i>.
   @param filter A file object filter.
   @param pattern The pattern that all filenames must match.
   Acceptable characters are wildcards (*) and question marks (?).
   **/
  String[] list0(IFSFileFilter filter, String pattern)
    throws IOException, AS400SecurityException
  { // @A5D removed check for null filter (null means no filtering).
    // Ensure that we are connected to the system.
    if (impl_ == null)
      chooseImpl();

    // Assume that the 'pattern' argument has been validated as non-null.
    // Note that we tolerate a null-valued 'filter' argument.

    // List the attributes of all files in this directory.  Have to append
    // a file separator and * to the path so that all files in the
    // directory are returned.
    String directory = path_;
    if (directory.lastIndexOf(separatorChar) != directory.length() - 1)
    {
      // Add a separator character.
      directory = directory + separatorChar;
    }
    String[] allNames = impl_.listDirectoryContents(directory + pattern);

    // Add the name for each reply that matches the filter to the array
    // of names.

    // @A1C
    // Changed the behavior of the list() to conform to that of the JDK1.1.x
    // so that a NULL is returned if and only if the directory or file represented
    // by this IFSFile object doesn't exist.
    //
    // Original code:
    // if (replys != null && replys.size() != 0)
    String[] names = null;
    if (allNames != null)
    {
      names = new String[allNames.length];
      int j = 0;
      for (int i = 0; i < allNames.length; i++)
      {
        String name = allNames[i];
        boolean addThisOne = false;
        if (!(name.equals(".") || name.equals("..")))
        {
          addThisOne = false;  // Broke up this "double if" check to avoid constructing
          if (filter == null)  // the IFSFile object when there is no filter specified.
          {
            addThisOne = true;
          } else
          {
            IFSFile file = new IFSFile(system_, directory, name);
            if (filter.accept(file))
            {
              addThisOne = true;
            }
          }
          if (addThisOne == true) // Specified the add in only one place to avoid confusion
          {                       // over how many add sequences actually take place.
            names[j++] = name;
          }
        }
      }

      if (j == 0)
      {
        // @A1C
        //
        // Original code:
        // names = null;
        names = new String[0];      // @A1C
      }
      else if (names.length != j)
      {
        // Copy the names to an array of the exact size.
        String[] newNames = new String[j];
        System.arraycopy(names, 0, newNames, 0, j);
        names = newNames;
      }
    }

    return names;
  }

  /**
   Lists the integrated file system objects in the directory represented by this
   object that satisfy <i>filter</i>.
   @param filter A file object filter.
   @param pattern The pattern that all filenames must match. Acceptable characters
   are wildcards (*) and
   question marks (?).
   @return An array of object names in the directory that satisfy the filter
   and pattern. This list does not include the current directory or the parent
   directory.  If this object does not represent a directory,  or the directory is not accessible, null is returned.
   If this object represents an empty directory, or the filter or pattern does
   not match any files, an empty string array is returned. The IFSFile object
   passed to the filter object have cached file attribute information.
   Maintaining references to these IFSFile objects after the list operation
   increases the chances that their file attribute information will not be valid.
   <br>Note: Due to a limitation in the File Server, at most 65,535 files will be listed.

   @see #listFiles(IFSFileFilter,String)
   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

   **/
  public String[] list(IFSFileFilter filter,
                       String        pattern)
    throws IOException
  {
    // Validate arguments.  Note that we tolerate a null-valued 'filter'.
    if (pattern == null)
      throw new NullPointerException("pattern");

    try
    {
      return list0(filter, pattern);
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      // return null;  // @B6d
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED); // @B6a
    }
  }

  /**
   Lists the integrated file system objects in the directory represented
   by this object that match <i>pattern</i>.
   @param pattern The pattern that all filenames must match. Acceptable
   characters are wildcards (*) and
   question marks (?).
   @return An array of object names in the directory that match the pattern.
   This list does not include the current directory or the parent directory.
   If this object does not represent a directory, or the directory is not accessible, null is returned. If
   this object represents an empty directory, or the pattern does not
   match any files, an empty string array is returned.
   <br>Note: Due to a limitation in the File Server, at most 65,535 files will be listed.

   @see #listFiles(String)
   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

   **/
  public String[] list(String pattern)
    throws IOException
  {
    return list(null, pattern);
  }

  //@A7A Added function to return an array of files in a directory.
  /**
   Lists the integrated file system objects in the directory represented by this object. With the use of this method, attribute information is cached and will not be automatically refreshed from the system. This means that retrieving attribute information for files returned in the list is much faster than using the {@link #list() list} method, but attribute information may become inconsistent with the system.  
   <p>
   When the IFSFile object represents the root of the QSYS file system, this method may return a partial list of the contents of the directory.

   @return An array of objects in the directory. This list does not
   include the current directory or the parent directory.  If this
   object does not represent a directory,  or the directory is not accessible, null is returned.  If this
   object represents an empty directory, an empty object array is returned.
   <br>Note: Due to a limitation in the File Server, at most 65,535 files will be listed.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

   **/
  public IFSFile[] listFiles()
    throws IOException
  {
    return listFiles((IFSFileFilter)null, "*");
  }


  //@A7A Added function to return an array of files in a directory.
  /**
   Lists the integrated file system objects in the directory represented by this object that satisfy <i>filter</i>. With the use of this method, attribute information is cached and will not be automatically refreshed from the system. This means that retrieving attribute information for files returned in the list is much faster than using the {@link #list(IFSFileFilter) list} method, but attribute information may become inconsistent with the system.  
   <p>
   When the IFSFile object represents the root of the QSYS file system, this method may return a partial list of the contents of the directory.

   @param filter A file object filter.
   @return An array of objects in the directory. This list does not
   include the current directory or the parent directory.  If this
   object does not represent a directory,  or the directory is not accessible, null is returned.  If this
   object represents an empty directory, an empty object array is returned.
   <br>Note: Due to a limitation in the File Server, at most 65,535 files will be listed.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

   **/
  public IFSFile[] listFiles(IFSFileFilter filter)
    throws IOException
  {
    return listFiles(filter, "*");
  }


  //@A7A Added function to return an array of files in a directory.
  /**
   Lists the integrated file system objects in the directory represented by this object that satisfy <i>filter</i>. With the use of this method, attribute information is cached and will not be automatically refreshed from the system. This means that retrieving attribute information for files returned in the list is much faster than using the {@link #list(IFSFileFilter,String) list} method, but attribute information may become inconsistent with the system.  
   <p>
   When <code>pattern</code> is "*" and the IFSFile object represents the root of the QSYS file system, this method may return a partial list of the contents of the directory.

   @param filter A file object filter.
   @param pattern The pattern that all filenames must match. Acceptable
   characters are wildcards (*) and
   question marks (?).
   @return An array of object names in the directory that satisfy the filter
   and pattern. This list does not include the current directory or the parent
   directory.  If this object does not represent a directory,  or the directory is not accessible, null is returned.
   If this object represents an empty directory, or the filter or pattern does
   not match any files, an empty object array is returned. The IFSFile object
   passed to the filter object has cached file attribute information.  Maintaining
   references to these IFSFile objects after the list operation increases the
   chances that their file attribute information will not be valid.
   <br>Note: Due to a limitation in the File Server, at most 65,535 files will be listed.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

   **/
  public IFSFile[] listFiles(IFSFileFilter filter, String pattern)
    throws IOException
  {
    // Validate arguments.  Note that we tolerate a null-valued 'filter'.
    if (pattern == null)
      throw new NullPointerException("pattern");

    try
    {
      return listFiles0(filter, pattern, -1, (String)null, (byte[])null);                             // @D4C @C3C
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      // return null;  // @B6d
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED); // @B6a
    }
  }


  //@A7A Added function to return an array of files in a directory.
  /**
   Lists the integrated file system objects in the directory represented by this object that match <i>pattern</i>. With the use of this method, attribute information is cached and will not be automatically refreshed from the system. This means that retrieving attribute information for files returned in the list is much faster than using the {@link #list(String) list} method, but attribute information may become inconsistent with the system.  
   <p>
   When <code>pattern</code> is "*" and the IFSFile object represents the root of the QSYS file system, this method may return a partial list of the contents of the directory.

   @param pattern The pattern that all filenames must match. Acceptable characters
   are wildcards (*) and
   question marks (?).
   @return An array of object names in the directory that match the pattern. This
   list does not include the current directory or the parent directory.  If this
   object does not represent a directory,  or the directory is not accessible, null is returned. If this object
   represents an empty directory, or the pattern does not match any files,
   an empty object array is returned.
   <br>Note: Due to a limitation in the File Server, at most 65,535 files will be listed.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

   **/
  public IFSFile[] listFiles(String pattern)
    throws IOException
  {
    return listFiles((IFSFileFilter)null, pattern);
  }

  //@A7A Added function to return an array of files in a directory.
  //@C3c Moved logic to new private method.
  /**
   Lists the integrated file system objects in the directory represented by this
   object that satisfy <i>filter</i>.  With the use of this method, attribute
   information is cached and will not be automatically refreshed from the system.
   This means attribute information may become inconsistent with the system.
   @param filter A file object filter.
   @param pattern The pattern that all filenames must match.
   @param maxGetCount The maximum number of directory entries to retrieve.
           -1 indicates that all entries that match the search criteria should be retrieved.
   @param restartName The file name from which to start the search.
           If null, the search is started at the beginning of the list.
   Acceptable characters are wildcards (*) and question marks (?).
   **/
  IFSFile[] listFiles0(IFSFileFilter filter, String pattern, int maxGetCount, String restartName)   // @D4C
    throws IOException, AS400SecurityException
  {
    return listFiles0(filter, pattern, maxGetCount, restartName, null);
  }

  //@C3a Relocated logic from original listFiles0 method.
  /**
   Lists the integrated file system objects in the directory represented by this
   object that satisfy <i>filter</i>.  With the use of this method, attribute
   information is cached and will not be automatically refreshed from the system.
   This means attribute information may become inconsistent with the system.
   @param filter A file object filter.
   @param pattern The pattern that all filenames must match.
   Acceptable characters are wildcards (*) and question marks (?).
   **/
  private IFSFile[] listFiles0(IFSFileFilter filter, String pattern, int maxGetCount, String restartName, byte[] restartID)   // @D4C
    throws IOException, AS400SecurityException
  {
    // Do not specify both restartName and restartID.  Specify one or the other.

    // Ensure that we are connected to the system.
    if (impl_ == null)
       chooseImpl();

    // Assume that the 'pattern' argument has been validated as non-null.
    // Note that we tolerate a null-valued 'filter' argument.

    // List the attributes of all files in this directory.  Have to append
    // a file separator and * to the path so that all files in the
    // directory are returned.
    String directory = path_;
    if (directory.lastIndexOf(separatorChar) != directory.length() - 1)
    {
      // Add a separator character.
      directory = directory + separatorChar;
    }
    IFSCachedAttributes[] fileAttributeList; //@C3C
    if (restartName != null) {
      fileAttributeList = impl_.listDirectoryDetails(directory + pattern, directory, maxGetCount, restartName); //@D2C @D4C
    }
    else {
      fileAttributeList = impl_.listDirectoryDetails(directory + pattern, directory, maxGetCount, restartID); //@C3a
    }

    // Add the name for each reply that matches the filter to the array
    // of files.

    if (fileAttributeList == null) {
       return new IFSFile[0];
    }
    else
    {
      IFSFile[] files = new IFSFile[fileAttributeList.length];
      int j = 0;
      for (int i = 0; i < fileAttributeList.length; i++) {
         IFSFile file = new IFSFile(system_, fileAttributeList[i]); //@D2C
         if (filter == null || filter.accept(file))  //@D2C
         {
             files[j++] = file;  //@D2C
         }
      }

      if (j == 0)
      {
         files = new IFSFile[0];
      }
      else if (files.length != j)
      {
         // Copy the objects to an array of the exact size.
         IFSFile[] newFiles = new IFSFile[j];
         System.arraycopy(files, 0, newFiles, 0, j);
         files = newFiles;
      }
      return files;
    }
  }

  //@C3a
  IFSFile[] listFiles0(IFSFileFilter filter, String pattern, int maxGetCount, byte[] restartID)
    throws IOException, AS400SecurityException
  {
    return listFiles0(filter, pattern, maxGetCount, null, restartID);
  }

  //@C3a
  IFSFile[] listFiles0(IFSFileFilter filter, String pattern)
    throws IOException, AS400SecurityException
  {
    return listFiles0(filter, pattern, -1, null, null);
  }


//internal mkdir that returns return codes and throws exceptions.
  int mkdir0(String directory)
    throws IOException, AS400SecurityException
  {
    // Assume the argument has been validated as non-null.

    if (impl_ == null)
      chooseImpl();

    return impl_.mkdir(directory);
  }

  /** Creates an integrated file system directory whose path name is
  specified by this object.

   @return true if the directory was created; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

   **/
  public boolean mkdir()
    throws IOException
  {
    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    try
    {
      returnCode = mkdir0(path_);
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    return (returnCode == IFSReturnCodeRep.SUCCESS);
  }

//internal mkdirs that returns return codes and throws exceptions
  int mkdirs0()
    throws IOException, AS400SecurityException
  {
    if (impl_ == null)
      chooseImpl();

    return impl_.mkdirs();
  }

  /**
   Creates an integrated file system directory whose path name is
   specified by this object. In addition, create all parent directories as necessary.

   @return true if the directory (or directories) were created; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

    **/
  public boolean mkdirs()
    throws IOException
  {
    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    try
    {
      returnCode = mkdirs0();
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      // returnCode = IFSReturnCodeRep.FILE_NOT_FOUND; //@A7D Unnecessary assignment.
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED); // @B6a
    }
    return (returnCode == IFSReturnCodeRep.SUCCESS);
  }

  /**
   Restores the state of this object from an object input stream.
   @param ois The stream of state information.
   @exception IOException
   @exception ClassNotFoundException
   **/
  private void readObject(java.io.ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    // Restore the non-static and non-transient fields.
    ois.defaultReadObject();

    // Initialize the transient fields.
    initializeTransient();
  }

  /**
   Removes a file listener so that it no longer receives file events from
   this IFSFile.
   @param listener The file listener.
   **/
  public void removeFileListener(FileListener listener)
  {
    if (listener == null)
      throw new NullPointerException("listener");

      fileListeners_.removeElement(listener);
  }

  /**
   Removes a property change listener.
   @param listener The property change listener to remove.
   **/
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null)
      throw new NullPointerException("listener");

    changes_.removePropertyChangeListener(listener);
  }

  /**
   Removes a vetoable change listener.
   @param listener The vetoable change listener to remove.
   **/
  public void removeVetoableChangeListener(VetoableChangeListener listener)
  {
    if (listener == null)
      throw new NullPointerException("listener");

    vetos_.removeVetoableChangeListener(listener);
  }


//internal renameTo that returns an error code
  int renameTo0(IFSFile file)
    throws IOException, PropertyVetoException, AS400SecurityException
  {
    // Assume the argument has been validated as non-null.

    if (impl_ == null)
      chooseImpl();

    String targetPath = file.getAbsolutePath();
    if (targetPath.length() == 0)
    {
      throw new ExtendedIllegalStateException("path",
                        ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    // Fire a vetoable change event.
    vetos_.fireVetoableChange("path", path_, file.getAbsolutePath());

    // Rename the file.
    int rc = impl_.renameTo(file.getImpl());

    if (rc == IFSReturnCodeRep.SUCCESS)
    {
      String oldPath = path_;
      path_ = file.getAbsolutePath();

      // Fire the property change event having null as the name to
      // indicate that the path, parent, etc. have changed.
      changes_.firePropertyChange("path", oldPath, path_);

      // Clear any cached attributes.
      cachedAttributes_ = null;          //@A7a
    }

    return rc;
  }

  /**
   Renames the integrated file system object specified by this object to
   have the path name of <i>file</i>.  Wildcards are not permitted in this file name.
   @param file The new file name.

   @return true if successful; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception PropertyVetoException If the change is vetoed.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.

   **/
  public boolean renameTo(IFSFile file)
    throws IOException, PropertyVetoException
  {
    // Validate the argument.
    if (file == null)
      throw new NullPointerException("file");

    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    try
    {
      returnCode = renameTo0(file);
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      // returnCode = IFSReturnCodeRep.FILE_NOT_FOUND; //@A7D Unnecessary assignment.
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED); // @B6a
    }
    return (returnCode == IFSReturnCodeRep.SUCCESS);
  }


  /**
   Sets the file's data CCSID.
   @param ccsid The file data CCSID.  Note that the data in the file is not changed; only the CCSID "tag" on the file is changed.
   @return true if successful; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system, or if the file doesn't exist or is a directory.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.
   **/
  public boolean setCCSID(int ccsid)
    throws IOException
  {
    boolean success = false;
    try
    {
      if (impl_ == null) chooseImpl();
      success = impl_.setCCSID(ccsid);
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    if (success)
    {
      // Fire the file modified event.
      if (fileListeners_.size() != 0) {
        IFSFileDescriptor.fireModifiedEvents(this, fileListeners_);
      }
    }
    return success;
  }



  /**
   Changes the fixed attributes (read only, hidden, etc.) of the integrated
   file system object
   represented by this object to <i>attributes</i>.
   @param attributes The attributes to set on the file.  These attributes are <i>not</i>
                     ORed with existing attributes.  They replace the existing
                     fixed attributes of the file.  The attributes are a bit map as
                     follows
                     <UL>
                     <LI>0x0001: on = file is a readonly file
                     <li>0x0002: on = file is a hidden file
                     <li>0x0004: on = file is a system file
                     <li>0x0010: on = file is a directory
                     <li>0x0020: on = file has been changed (archive bit)
                     </UL>
                     For example, 0x0023 is a readonly, hidden file with the
                     archive bit on.
   @return true if successful; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.
   **/
     // @D1 - new method because of changes to java.io.File in Java 2.
  boolean setFixedAttributes(int attributes)
    throws IOException
  {
    // Validate arguments.
    if ((attributes & 0xFFFFFF00) != 0)
    {
      throw new ExtendedIllegalArgumentException("attributes",
                     ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    if (impl_ == null)
    try
    {
      chooseImpl();
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    boolean success = impl_.setFixedAttributes(attributes);

    if (success)
    {
      cachedAttributes_ = null;
    }

    return success;
  }

  /**
   Marks the integrated file system object represented by this object as hidden.
   @return true if successful; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.
   **/
   // @D1 - new method because of changes to java.io.File in Java 2.

  public boolean setHidden()
    throws IOException
  {
    return setHidden(true);
  }


  /**
   Changes the hidden attribute of the integrated file system object
   represented by this object.
   @param attribute True to set the hidden attribute of the file.
                    False to turn off the hidden attribute.

   @return true if successful; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.
   **/
   // @D1 - new method because of changes to java.io.File in Java 2.

  public boolean setHidden(boolean attribute)
    throws IOException
  {
    if (impl_ == null)
    try
    {
      chooseImpl();
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    boolean success = impl_.setHidden(attribute);

    if (success)
    {
      cachedAttributes_ = null;

      // Fire the file modified event.
      if (fileListeners_.size() != 0) {
        IFSFileDescriptor.fireModifiedEvents(this, fileListeners_);
      }
    }
    return success;
  }


  /**
   Changes the last modified time of the integrated file system object
   represented by this object to <i>time</i>.
   @param time The desired last modification time (measured in milliseconds
   since January 1, 1970 00:00:00 GMT), or 0 to leave the last modification
   time unchanged, or -1 to set the last modification time to the current system time.

   @return true if successful; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.
   @exception PropertyVetoException If the change is vetoed.
   **/
  public boolean setLastModified(long time)
    throws IOException, PropertyVetoException
  {
    // Validate arguments.
    if (time < -1)  // @B8c
    {
      throw new ExtendedIllegalArgumentException("time + (" +
                                                 Long.toString(time) + ")",
                     ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Fire a vetoable change event for lastModified.
    vetos_.fireVetoableChange("lastModified", null, new Long(time));

    if (impl_ == null)
    try
    {
      chooseImpl();
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    boolean success = impl_.setLastModified(time);

    if (success)
    {
      // Fire the property change event.
      changes_.firePropertyChange("lastModified", null, new Long(time));

      // Fire the file modified event.
      if (fileListeners_.size() != 0) {
        IFSFileDescriptor.fireModifiedEvents(this, fileListeners_);
      }

      // Clear any cached attributes.
      cachedAttributes_ = null;         //@A7a
    }

    return success;
  }

  // @B8a
  /**
   Sets the length of the integrated file system object represented by this object.  The file can be made larger or smaller.  If the file is made larger, the contents of the new bytes of the file are undetermined.
   @param length The new length, in bytes.
   @return true if successful; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.
   **/
  public boolean setLength(int length)
    throws IOException
  {
    // Validate arguments.
    if (length < 0)
    {
      throw new ExtendedIllegalArgumentException("length + (" +
                                                 Integer.toString(length) + ")",
                     ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    // Note: The file system will not allow us to set the length of a file to a value larger than (2Gig minus 1), or 2147483647 (0x7FFFFFFF) bytes, which happens to be the maximum positive value which an 'int' will hold.  Therefore we do not provide a setLength(long) method.

    try
    {
      if (impl_ == null)
        chooseImpl();

      boolean success = impl_.setLength(length);

      if (success)
      {
        // Fire the file modified event.
        if (fileListeners_.size() != 0) {
          IFSFileDescriptor.fireModifiedEvents(this, fileListeners_);
        }

        // Clear any cached attributes.
        cachedAttributes_ = null;
      }

      return success;
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }
  }

  /**
   Sets the file path.
   @param path The absolute file path.
   @exception PropertyVetoException If the change is vetoed.
   **/
  public void setPath(String path)
    throws PropertyVetoException
  {
    if (path == null)
    {
      throw new NullPointerException("path");
    }

    // Ensure that the path is not altered after the connection is
    // established.
    if (impl_ != null)
    {
      throw new ExtendedIllegalStateException("path",
                      ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    // Remember the current path value.
    String oldPath = path_;

    // If the specified path doesn't start with the separator character,
    // add one.  All paths are absolute for IFS.
    String newPath;
    if (path.length() == 0 || path.charAt(0) != separatorChar)
    {
      newPath = separator + path;
    }
    else
    {
      newPath = path;
    }

    // Fire a vetoable change event for the path.
    vetos_.fireVetoableChange("path", oldPath, newPath);

    // Update the path value.
    path_ = newPath;

    // Fire the property change event having null as the name to
    // indicate that the path, parent, etc. have changed.
    changes_.firePropertyChange("path", oldPath, newPath);
  }


  /**
   Sets the pattern-matching behavior used when files are listed by any of the <tt>list()</tt> or <tt>listFiles()</tt> methods.  The default is PATTERN_POSIX.
   @param patternMatching Either {@link #PATTERN_POSIX PATTERN_POSIX}, {@link #PATTERN_POSIX_ALL PATTERN_POSIX_ALL}, or {@link #PATTERN_OS2 PATTERN_OS2}

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.
   **/
  public void setPatternMatching(int patternMatching)
    throws IOException
  {
    if (patternMatching < PATTERN_POSIX || patternMatching > PATTERN_OS2) {
      throw new ExtendedIllegalArgumentException("patternMatching",
                     ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (impl_ == null)
    try
    {
      chooseImpl();
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    impl_.setPatternMatching(patternMatching);
    patternMatching_ = patternMatching;
  }


// @A6A
 /**
   * Sets the permission of the object.
   * @param permission The permission that will be set to the object.
   * @see #getPermission
   * @exception AS400Exception If the system returns an error message.
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with the system.
   * @exception ObjectDoesNotExistException If the object does not exist on the system.
   * @exception PropertyVetoException If the change is vetoed.
   * @exception UnknownHostException If the system cannot be located.

   *
  **/
  public void setPermission(Permission permission)
    throws AS400Exception,
           AS400SecurityException,
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException,
           ServerStartupException,
           PropertyVetoException,
           UnknownHostException
  {
    if (permission == null)
    {
      throw new NullPointerException("permission");
    }

    AS400 system=permission.getSystem();

    if (system.equals(system_))
    {
      if ((this.getFileSystem()).equals(permission.getObjectPath()))
      {
        vetos_.fireVetoableChange("permission", null, permission_);

        this.permission_=permission;
        permission.commit();

        changes_.firePropertyChange("permission", null, permission_);

      }
      else
      {
        throw new ExtendedIllegalArgumentException("permission.objectPath + (" +
                                                   permission.getObjectPath() + ")",
                         ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      }
    }
    else
    {
      String systemName = null;
      if (system != null)
        systemName = system.getSystemName();
      throw new ExtendedIllegalArgumentException("permission.system + (" +
                                         systemName + ")",
                       ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
  }


  /**
   Marks the integrated file system object represented by this object so
   that only read operations are allowed.
   @return true if successful; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.
   **/
   // @D1 - new method because of changes to java.io.File in Java 2.

  public boolean setReadOnly()
    throws IOException
  {
     return setReadOnly(true);
  }


  /**
   Changes the read only attribute of the integrated file system object
   represented by this object.
   @param attribute True to set the read only attribute of the file such that
                    the file cannot be changed.  False to set the read only
                    attributes such that the file can be changed.

   @return true if successful; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.
   **/

  // @D1a new method because of changes to java.io.File in Java 2.
  public boolean setReadOnly(boolean attribute)
    throws IOException
  {
    if (impl_ == null)
    try
    {
      chooseImpl();
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    boolean success = impl_.setReadOnly(attribute);

    if (success)
    {
      cachedAttributes_ = null;

      // Fire the file modified event.
      if (fileListeners_.size() != 0) {
        IFSFileDescriptor.fireModifiedEvents(this, fileListeners_);
      }

    }
    return success;
  }


  /**
   Sets the sorting behavior used when files are listed by any of the <tt>list()</tt> or <tt>listFiles()</tt> methods.  The default is <tt>false</tt> (unsorted).
   @param sort If <tt>true</tt>: Lists of files are returned in sorted order.
   If <tt>false</tt>: Lists of files are returned in whatever order the file system provides.

   @exception IOException If an error occurs while communicating with the system.
   @exception AS400SecurityException If a security or authority error occurs.
   **/
  public void setSorted(boolean sort)
    throws IOException, AS400SecurityException
  {
    if (impl_ == null)
    try
    {
      chooseImpl();
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    impl_.setSorted(sort);
    sortLists_ = sort;
  }



  /**
   Sets the system.
   The system cannot be changed once a connection is made to the system.
   @param system The system object.
   @exception PropertyVetoException If the change is vetoed.
   **/
  public void setSystem(AS400 system)
    throws PropertyVetoException
  {
    if (system == null)
    {
      throw new NullPointerException("system");
    }

    // Ensure that system is not altered after the connection is
    // established.
    if (impl_ != null)
    {
      Trace.log(Trace.ERROR, "Cannot set property 'system' after connect.");
      throw new ExtendedIllegalStateException("system",
                                  ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    // Fire a vetoable change event for system.
    vetos_.fireVetoableChange("system", system_, system);

    // Remember the old system value.
    AS400 oldSystem = system_;

    system_ = system;

    // Fire the property change event.
    changes_.firePropertyChange("system", oldSystem, system_);
  }

  /**
   Generates a String representation of this object.
   @return The path name of the integrated file system object represented by this object.
   **/
  public String toString()
  {
    return path_;
  }
}

