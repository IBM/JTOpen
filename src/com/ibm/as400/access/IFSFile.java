///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: IFSFile.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
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
The IFSFile class represents
 an object in the AS/400 integrated file system.
 <br>
 IFSFile objects are capable of generating file events that call the
 following FileListener methods: fileDeleted and fileModified.
 <br>
 The following example demonstrates the use of IFSFile:
 <pre>
 // Work with /Dir/File.txt on the system eniac.
 AS400 as400 = new AS400("eniac");
 IFSFile file = new IFSFile(as400, "/Dir/File.txt");
 // Determine the parent directory of the file.
 String directory = file.getParent();
 // Determine the name of the file.
 String name = file.getName();
 // Determine the file size.
 long length = file.length();
 // Determine when the file was last modified.
 Date date = new Date(file.lastModified());
 // Delete the file.
 if (file.delete() == false)
 {
   // Display the error code.
   System.out.println("Unable to delete file.");
 }
 </pre>
@see com.ibm.as400.access.FileEvent
@see #addFileListener(com.ibm.as400.access.FileListener)
@see #removeFileListener(com.ibm.as400.access.FileListener)
 **/
public class IFSFile
  implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  /**
   The integrated file system path separator string used to separate paths in a path list.
   **/
  public final static String pathSeparator = ";";
  /**
   The integrated file system path separator character used to separate paths in a path list.
   **/
  public final static char pathSeparatorChar = ';';
  /**
   The integrated file system directory separator string used to separate directory/file components in a path.
   **/
  public final static String separator = "/";
  /**
   The integrated file system directory separator character used to separate directory/file components in a path.
   **/
  public final static char separatorChar = '/';
  private final static String SECURITY_EXCEPTION = "Security exception.";

  transient private PropertyChangeSupport changes_;
  transient private VetoableChangeSupport vetos_;
  transient private Vector fileListeners_;
  transient private IFSFileImpl impl_;

  private AS400 system_;
  private String path_ = "";  // Note: This is never allowed to be null.
  private Permission permission_; //@A6A


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
   It creates an IFSFile instance that represents the integrated file system object on <i>system</i> that has a path name of <i>directory</i>,  that is followed by the separator character and <i>name</i>.
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
   It creates an IFSFile instance that represents the integrated file system object on <i>system</i>  that has a  path name of <i>path</i>.
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
   It creates an IFSFile instance that represents the integrated file system object on <i>system</i> that has a
   path name is of <i>directory</i>, followed by the separator character and <i>name</i>.
   @param system The AS400 that contains the file.
   @param path The directory path name.
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

  /** @A4A
   Constructs an IFSFile object.
   It creates an IFSFile instance that represents the integrated file system object on <i>system</i> that has a path name of <i>directory</i>,  that is followed by the separator character and <i>name</i>.
   @param system The AS400 that contains the file.
   @param directory The directory.
   @param name The file name.
   **/
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

  /**
   Adds a file listener to receive file events from this IFSFile.
   @param listener The file listener.
   **/
  public void addFileListener(FileListener listener)
  {
    if (listener == null)
      throw new NullPointerException("listener");

    synchronized(fileListeners_)
    {
      fileListeners_.addElement(listener);
    }
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

    return impl_.canRead0();
  }

  /**
   Determines if the applet or application can read from the integrated file system object represented by this object.
   @return true if the object exists and is readable; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.
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
      returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    }
    return (returnCode == IFSReturnCodeRep.SUCCESS);
  }

//internal version of canWrite()
  int canWrite0()
    throws IOException, AS400SecurityException
  {
    if (impl_ == null)
      chooseImpl();

    return impl_.canWrite0();
  }

  /**
   Determines if the applet or application can write to the integrated file system object represented by this object.
   @return true if the object exists and is writeable; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

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
      returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
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


//internal delete that returns a return code status indicator
  int delete0()
    throws IOException, AS400SecurityException
  {
    if (impl_ == null)
      chooseImpl();

    int rc = impl_.delete0();

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
    }

    return rc;
  }

  /**
   Deletes the integrated file system object represented by this object.

   @return true if the object is successfully deleted; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

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
      returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    }
    return (returnCode == IFSReturnCodeRep.SUCCESS);
  }

  /**
   Determines if two IFSFile objects are equal.
   @param obj The object with which to compare.
   @return true if the path name and system name of the objects are equal; false otherwise.
   **/
  public boolean equals(Object obj)
  {
    // Determine the system name and path name.
    String targetPathName = "";
    String targetSystemName = null;
    if (obj != null && obj instanceof IFSFile)
    {
      IFSFile target = (IFSFile) obj;
      targetPathName = target.getPath();
      AS400 targetSystem = target.getSystem();
      if (targetSystem != null)
      {
        targetSystemName = targetSystem.getSystemName();
      }
    }

    // Two IFSFile objects are equal if their path names and systems
    // are equal.
    return (system_ != null && targetSystemName != null &&
            path_.equals(targetPathName) &&
            system_.getSystemName().equals(targetSystemName));
  }

//internal exists that returns a return code status indicator
  int exists0(String name)
    throws IOException, AS400SecurityException
  {
    // Assume the argument has been validated as non-null.

    if (impl_ == null)
      chooseImpl();

    return impl_.exists0(name);
  }

  /**
   Determines if the integrated file system object represented by this object exists.
   @return true if the object exists; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

   **/
  public boolean exists()
    throws IOException
  {
    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    try
    {
      returnCode = exists0(path_);
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, SECURITY_EXCEPTION, e);
      returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    }
    return (returnCode == IFSReturnCodeRep.SUCCESS);
  }


  /**
   Returns the path name of the integrated file system object represented by this object.  This is the full path starting at the root directory.
   @return The absolute path name of this integrated file system object.
   **/
  public String getAbsolutePath()
  {
    return path_;
  }


  /**
   Returns the path name of the integrated file system object represented by this object.  This is the full path starting at the root directory.
   @return The canonical path name of this integrated file system object.
   **/
  public String getCanonicalPath()
  {
    return path_;
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
   Determines the amount of unused storage space in the file system.
   @return The number of bytes of storage available.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

   **/
  public long getFreeSpace()
    throws IOException
  {
    if (impl_ == null)
    try
    {
      chooseImpl();
    }
    catch (AS400SecurityException e)
    {
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    return impl_.getFreeSpace();
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
  public  String getName()
  {
    String name = null;

    // The name is everything after the last occurrence of the
    // file separator character.
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

    return name;
  }

  /**
   Returns the parent directory of the integrated file system object represented by this object. The parent directory is everything in the path name before the last occurrence of the separator character, or null if the separator character does not appear in the path name.
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


 // @A6A
 /**
   * Returns the permission of the object.
   * @return The permission of the object.
   * @see #setPermission
   * @exception AS400Exception If the AS/400 system returns an error message.
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicationg with the AS/400.
   * @exception ObjectDoesNotExistException If the AS/400 object does not exist.
   * @exception UnknownHostException If the AS/400 system cannot be located.
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
    if (impl_ == null)
      chooseImpl();

    return impl_.isDirectory0();
  }

  /**
   Determines if the integrated file system object represented by this object is a directory.

   @return true if the integrated file system object exists and is a directory; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

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
      returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    }
    return (returnCode == IFSReturnCodeRep.SUCCESS);
  }

//internal isFile that returns a return code status indicator
  int isFile0()
    throws IOException, AS400SecurityException
  {
    if (impl_ == null)
      chooseImpl();

    return impl_.isFile0();
  }

  /**
   Determines if the integrated file system object represented by this object is a "normal" file.<br>
   A file is "normal" if it is not a directory or a container of other objects.

   @return true if the specified file exists and is a "normal" file; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

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
      returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    }
    return (returnCode == IFSReturnCodeRep.SUCCESS);
  }

//internal lastModified that throws a security exception
  long lastModified0()
    throws IOException, AS400SecurityException
  {
    if (impl_ == null)
      chooseImpl();

    return impl_.lastModified0();
  }

  /**
   Determines the time that the integrated file system object represented by this object was last modified.
   @return The time (measured in milliseconds since 01/01/1970 00:00:00 GMT) that the integrated file system object was last modified, or 0L if it does not exist.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

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
      return 0L;
    }
  }

//internal length that throws a security exception
  long length0()
    throws IOException, AS400SecurityException
  {
    if (impl_ == null)
      chooseImpl();

    return impl_.length0();
  }

  /**
   Determines the length of the integrated file system object represented by this object.
   @return The length, in bytes, of the integrated file system object, or 0L if it does not exist.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

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
      return 0L;
    }
  }

  /**
   Lists the integrated file system objects in the directory represented by this object.

   @return An array of object names in the directory. This list does not include the current directory or the parent directory.  If this object does not represent a directory, null is returned.  If this object represents an empty directory, an empty string array is returned.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

   **/
  public String[] list()
    throws IOException
  {
    return list("*");
  }

  /**
   Lists the integrated file system objects in the directory represented by this object that satisfy <i>filter</i>.
   @param filter A file object filter.  If null, then no filtering is done.
   @return An array of object names in the directory that satisfy the filter. This list does not include the current directory or the parent directory.  If this object does not represent a directory, null is returned. If this object represents an empty directory, or the filter does not match any files, an empty string array is returned. The IFSFile object passed to the filter object have cached file attribute information.  Maintaining references to these IFSFile objects after the list operation increases the chances that their file attribute information will not be valid.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

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
    // Ensure that we are connected to the server.
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
          addThisOne = false;  // Broke up this "double if" check to avoid construction of
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
   Lists the integrated file system objects in the directory represented by this object that satisfy <i>filter</i>.
   @param filter A file object filter.
   @param pattern The pattern that all filenames must match. Acceptable characters are wildcards (*) and
   question marks (?).
   @return An array of object names in the directory that satisfy the filter and pattern. This list does not include the current directory or the parent directory.  If this object does not represent a directory, null is returned. If this object represents an empty directory, or the filter or pattern does not match any files, an empty string array is returned. The IFSFile object passed to the filter object have cached file attribute information.  Maintaining references to these IFSFile objects after the list operation increases the chances that their file attribute information will not be valid.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

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
      return null;
    }
  }

  /**
   Lists the integrated file system objects in the directory represented by this object that match <i>pattern</i>.
   @param pattern The pattern that all filenames must match. Acceptable characters are wildcards (*) and
   question marks (?).
   @return An array of object names in the directory that match the pattern. This list does not include the current directory or the parent directory.  If this object does not represent a directory, null is returned. If this object represents an empty directory, or the pattern does not match any files, an empty string array is returned.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

   **/
  public String[] list(String pattern)
    throws IOException
  {
    return list(null, pattern);
  }

//internal mkdir that returns return codes and throws exceptions.
  int mkdir0(String directory)
    throws IOException, AS400SecurityException
  {
    // Assume the argument has been validated as non-null.

    if (impl_ == null)
      chooseImpl();

    return impl_.mkdir0(directory);
  }

  /** Creates an integrated file system directory whose path name is specified by this object.

   @return true if the directory was created; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

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

    return impl_.mkdirs0();
  }

  /**
   Creates an integrated file system directory whose path name is specified by this object. In addition, create all parent directories as necessary.

   @return true if the directory (or directories) were created; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

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
      returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
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

    synchronized(fileListeners_)
    {
      fileListeners_.removeElement(listener);
    }
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
    int rc = impl_.renameTo0(file.getImpl());

    if (rc == IFSReturnCodeRep.SUCCESS)
    {
      String oldPath = path_;
      path_ = file.getAbsolutePath();

      // Fire the property change event having null as the name to
      // indicate that the path, parent, etc. have changed.
      changes_.firePropertyChange("path", oldPath, path_);
    }

    return rc;
  }

  /**
   Renames the integrated file system object specified by this object to have the path name of <i>file</i>.  Wildcards are not permitted in this file name.
   @param file The new file name.

   @return true if successful; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception PropertyVetoException If the change is vetoed.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

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
      returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    }
    return (returnCode == IFSReturnCodeRep.SUCCESS);
  }

  /**
   Changes the last modified time of the integrated file system object represented by this object to <i>time</i>.
   @param time The desired last modification time (measured in milliseconds since January 1, 1970 00:00:00 GMT).
   @return true if successful; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.
   @exception PropertyVetoException If the change is vetoed.
   **/
  public boolean setLastModified(long time)
    throws IOException, PropertyVetoException
  {
    // Validate arguments.
    if (time < 0)
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
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    boolean success = impl_.setLastModified(time);

    if (success)
    {
      // Fire the property change event.
      changes_.firePropertyChange("lastModified", null, new Long(time));

      // Fire the file modified event.
      if (fileListeners_.size() != 0)
      {
        FileEvent event = new FileEvent(this, FileEvent.FILE_MODIFIED);
        synchronized(fileListeners_)
        {
          Enumeration e = fileListeners_.elements();
          while (e.hasMoreElements())
          {
            FileListener listener = (FileListener)e.nextElement();
            listener.fileModified(event);
          }
        }
      }
    }

    return success;
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


// @A6A
 /**
   * Sets the permission of the object.
   * @param permission The permission that will be set to the object.
   * @see #getPermission
   * @exception AS400Exception If the AS/400 system returns an error message.
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicationg with the AS/400.
   * @exception ObjectDoesNotExistException If the AS/400 object does not exist.
   * @exception PropertyVetoException If the change is vetoed.
   * @exception UnknownHostException If the AS/400 system cannot be located.

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
   Sets the system.
   The system cannot be changed once a connection is made to the server.
   @param system The AS/400 system object.
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
