///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSJavaFile.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
// @B1 - 2008-04-10 - Update length() for handling of symbolic link objects.
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;

import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.File;
import java.util.Vector;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Represents an object in the IBM i integrated file system.
 * <br>
 *
 * IFSJavaFile extends the java.io.File class and allows programs
 * to be written for the java.io.File interface and still access
 * the IBM i integrated file system.
 *
 * IFSFile should be considered as an alternate to java.io.File class.
 * <p>
 * When should IFSJavaFile be used?
 * <ul compact>
 * <li>
 * IFSJavaFile should be used when a portable interface, compatible with
 * java.io.File, is needed.  For example, you have written code
 * that accesses the native file system.  Now you want to move
 * the design to a networked file system.  More particularly,
 * you need to move the code to the IBM i integrated file system.
 * When a program is being ported and needs to use the IBM i
 * integrated file system, IFSJavaFile is a good choice.
 * IFSJavaFile also provides SecurityManager features defined in
 * java.io.File.
 * <p>
 * <li>
 * If you need to take full advantage of the IBM i integrated file
 * system, {@link IFSFile IFSFile} is more useful.  IFSFile is written to
 * handle more of the specific IBM i integrated file system details.
 * <p>
 * <li>
 * java.io.File can be used to access the IBM i file system
 * if you use a product like IBM i Access for Windows to map a local drive
 * to the IBM i integrated file system.
 * <p>
 * </ul>
 *
 * <p>
 * Notes:
 * <ol>
 * <li>IFSJavaFile is designed to be used with
 *     {@link IFSFileInputStream IFSFileInputStream} and
 *     {@link IFSFileOutputStream IFSFileOutputStream}.
 *     It does not support java.io.FileInputStream
 *     and java.io.FileOutputStream.
 * <li>IFSJavaFile cannot override createTempFile because
 *     java.io.File defines createTempFile as a static method.
 * <li>IFSJavaFile cannot override deleteOnExit because the
 *     Java Virtual Machine does nothing to indicate when it
 *     is preparing to exit.
 * <li>IFSJavaFile is designed to look more like
 *     java.io.File than IFSFile.  It is designed to enable
 *     a plug-in fit for previously written java.io.File code.
 * <li>IFSJavaFile always implements a SecurityManager using
 *     IBM i security.  The SecurityManager provides authority
 *     checks.  It throws security exceptions when illegal access
 *     attempts are made.
 * </ol>
 *
 * <p>
 * The following example demonstrates the use of IFSJavaFile.  It shows how a few lines
 * of platform specific code enable the creation of a file on either the IBM i system or
 * the local client.
 * <pre>
 *     int location            = ON_THE_SERVER;  // on the IBM i system
 *     java.io.File file       = null;
 *     java.io.OutputStream os = null;
 *<br>
 *     if (location == ON_THE_SERVER)
 *       file = new IFSJavaFile(new AS400("enterprise"), path); // Work with the file on the system "enterprise".
 *     else
 *       file = new java.io.File(path);                       // Work with the file on the local file system.
 *<br>
 *     if (file.exists())
 *       System.out.println("Length: " + file.length());      // Determine the file size.
 *     else
 *       System.out.println("File " + file.getName() + " not found");
 *<br>
 *     // Delete the file.  This should be done before creating an output stream.
 *     if (file.delete() == false)
 *       System.err.println("Unable to delete file.");         // Display the error message.
 *<br>
 *     if (location == ON_THE_SERVER)
 *       os = (OutputStream)new IFSFileOutputStream((IFSJavaFile)file);
 *     else
 *       os = new FileOutputStream(file);
 *<br>
 *     writeData(file, os);
 *     os.close();
 *<br>
 * void writeData(java.io.File file, java.io.OutputStream os)
 *   throws IOException
 * {
 *   // Determine the parent directory of the file.
 *   System.out.println("Directory: " + file.getParent());
 *<br>
 *   // Determine the name of the file.
 *   System.out.println("Name: " + file.getName());
 *<br>
 *   // Determine when the file was last modified.
 *   System.out.println("Date: " + new Date(file.lastModified()));
 *<br>
 *   System.out.println("Writing Data");
 *   for (int i = 0; i < 256; i++)
 *     os.write((byte)i);
 * }
 * </pre>
 *
 * @see com.ibm.as400.access.IFSFile
 * @see com.ibm.as400.access.IFSFileInputStream
 * @see com.ibm.as400.access.IFSFileOutputStream
**/
public class IFSJavaFile extends java.io.File implements java.io.Serializable
{
    static final long serialVersionUID = 4L;

  private IFSFile ifsFile_ = new IFSFile();
  private static final char AS400_SEPARATOR = '/';
  private static final String URL_PROTOCOL = "file";

  private static final boolean replace_ = (AS400_SEPARATOR != separatorChar); //@P2A

// Because pathSeparator, pathSeparatorChar, separator and separatorChar are declared final in java.io.File, we cannot override them.

  private boolean absolute_;

/**
 * Creates a default IFSJavaFile instance.
**/
  public IFSJavaFile()
  {
    this(separator); // Default to the root directory.        @A2C
    absolute_ = false;  // No path, so it can't be absolute.
  }

/**
 * Creates a new IFSJavaFile instance whose path name is given by <i>path</i>.
 *
 * @param   path   The file path name where the IFSJavaFile is or will be stored.
**/
  private IFSJavaFile(String path)
  {
    super(path);

    try
    {
      ifsFile_.setPath(replace_ ? path.replace(separatorChar, AS400_SEPARATOR) : path); //@P2C
      absolute_ = isAbsolutePath(path);
    }
    catch (PropertyVetoException e) {}  // will never happen
  }


/**
 * Creates a new IFSJavaFile instance from a parent pathname string and a child pathname string.
 *
 * @param   path   The file path name where the IFSJavaFile is or will be stored.
 * @param   name   The name of the IFSJavaFile object.
**/
  private IFSJavaFile(String path, String name)
  {
    super(path, name);

    try
    {
      String canon = canonicalizeDirectory(path) + name; //@P2A
      ifsFile_.setPath(replace_ ? canon.replace(separatorChar, AS400_SEPARATOR) : canon); //@P2C
      absolute_ = isAbsolutePath(canon);
    }
    catch (PropertyVetoException e) {}   // will never happen
  }

/**
 * Creates a new IFSJavaFile instance for the specified system, using a file pathname string.
 *
 * @param   system The system that contains the IFSJavaFile.
 * @param   path   The file path name where the IFSJavaFile is or will be stored.
**/
  public IFSJavaFile(AS400 system, String path)
  {
    this(path);
    if (system == null)
      throw new NullPointerException("system");

    setSystem(system);
    absolute_ = isAbsolutePath(path);
  }

/**
 * Creates a new IFSJavaFile instance for the specified system, from a parent pathname string and a child pathname string.
 *
 * @param   system The system that contains the IFSJavaFile.
 * @param   path   The file path name where the IFSJavaFile is or will be stored.
 * @param   name   The name of the IFSJavaFile object.
**/
  public IFSJavaFile(AS400 system, String path, String name)
  {
    this(path, name);
    if (system == null)
      throw new NullPointerException("system");

    setSystem(system);
    absolute_ = isAbsolutePath(path);
  }

/**
 * Creates a new IFSJavaFile instance from a parent abstract pathname and a child pathname string.
 * <p>
 * The directory argument cannot be null.  The constructed
 * IFSJavaFile instance uses the following settings taken from
 * <i>directory</i>:
 * <ul compact>
 * <li>system
 * <li>path
 * </ul>
 * The resulting file name is taken from the path name of <i>directory</i>,
 * followed by the separator character, followed by <i>name</i>.
 *
 * @param   directory The directory where the IFSJavaFile is or will be stored.
 * @param   name      The name of the IFSJavaFile object.
**/
  public IFSJavaFile(IFSJavaFile directory, String name)
  {
    this();

    if (directory == null)
      throw new NullPointerException("directory");

    if (name == null)
      throw new NullPointerException("name");

    String directoryPath = directory.getPath();
    try
    {
      String canon = canonicalizeDirectory(directoryPath) + name; //@P2A
      ifsFile_.setPath(replace_ ? canon.replace(separatorChar, AS400_SEPARATOR) : canon); //@P2C
    }
    catch (PropertyVetoException e) {}   // will never happen

    AS400 localSystem = directory.getSystem();
    if (localSystem != null)
    {
      setSystem(localSystem);
    }
    absolute_ = directory.isAbsolute();
  }

/**
 * Creates a new IFSJavaFile instance for the specified system, from a parent abstract pathname and a child pathname string.
 *
 * @param   system    The system that contains the IFSJavaFile.
 * @param   directory The directory where the IFSJavaFile is or will be stored.
 * @param   name      The name of the IFSJavaFile object.
**/
  public IFSJavaFile(AS400 system, IFSJavaFile directory, String name)
  {
    this(directory, name);
    if (system == null)
      throw new NullPointerException("system");

    setSystem(system);
    absolute_ = directory.isAbsolute();
  }


/**
 * Creates a new IFSJavaFile instance from an IFSFile object.
 *
 * @param   file  An IFSFile object.
**/
  public IFSJavaFile(IFSFile file)
  {
    super(file.getPath(), file.getName());
    ifsFile_ = file;
    absolute_ = true;  // all IFSFile objects have absolute paths
  }

  /* if the directory doesn't end in a file separator, add one */
  private String canonicalizeDirectory(String directory)
  {
    // Assume the argument has been validated as non-null.  //@A1C
    int separatorIndex = directory.length() - separator.length();
    if (!directory.substring(separatorIndex).equals(separator))
    {
      directory += separator;
    }
    return directory;
  }


  /**
   Tests whether the application can execute the file denoted by this abstract pathname.
   This method is supported for IBM i V5R1 and higher. For older releases, it simply returns false.
   If the user profile has *ALLOBJ special authority (and system is V5R1 or higher), this method always returns true.

   @return true if and only if the abstract pathname exists and the application is allowed to execute the file.
   For consistency with the behavior of java.io.File on IBM i JVM's: If a security error occurs simply because the user profile isn't allowed to access the file, the error is traced and false is returned.
   @exception SecurityException  If an unanticipated security error occurs.
   **/
  public boolean canExecute()
    throws SecurityException
  {
    try
    {
      return ifsFile_.canExecute();
    }
    catch (IOException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
  }


/**
 * Indicates if the program can read from the file denoted by this abstract pathname.
 *
 * @return true if and only if the abstract pathname exists and the application is allowed to read the file.
 * For consistency with the behavior of java.io.File on IBM i JVM's: If a security error occurs simply because the user profile isn't allowed to access the file, the error is traced and false is returned.
 * @exception SecurityException  If an unanticipated security error occurs.
**/
  public boolean canRead()
    throws SecurityException
  {
    try
    {
      return ifsFile_.canRead();
    }
    catch (IOException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
  }

/**
 * Indicates if the program can write to the file denoted by this abstract pathname.
 *
 * @return true if and only if the abstract pathname exists and the application is allowed to write the file.
 * For consistency with the behavior of java.io.File on IBM i JVM's: If a security error occurs simply because the user profile isn't allowed to access the file, the error is traced and false is returned.
 * @exception SecurityException  If an unanticipated security error occurs.
**/
  public boolean canWrite()
    throws SecurityException
  {
    try
    {
      return ifsFile_.canWrite();
    }
    catch (IOException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
  }

/**
 * Compares the paths of two IFSJavaFiles.
 * <p>
 * The following examples demonstrate the use of this method:
 *
 * <p>
 * In this example, returnCode would be less than <code>0</code> because the
 * path of <code>file</code> is less than the path of <code>file2</code>.
 * <pre>
 *  IFSJavaFile file  = new IFSJavaFile(new AS400("enterprise"), path);
 *  IFSJavaFile file2 = new IFSJavaFile(new AS400("enterprise"), path + "\\extra");
 * <br>
 *  int returnCode = file.compareTo(file2);
 * </pre>
 *
 * <p>
 * In this example, returnCode would be greater than <code>0</code> because the
 * path of <code>file</code> is greater than the path of <code>file2</code>.
 * <pre>
 *  IFSJavaFile file  = new IFSJavaFile(new AS400("enterprise"), path + "\\extra");
 *  IFSJavaFile file2 = new IFSJavaFile(new AS400("enterprise"), path);
 * <br>
 *  int returnCode = file.compareTo(file2);
 * </pre>
 *
 * <p>
 * In this example, returnCode would be less than <code>0</code> because the
 * path of <code>file</code> is less than the path of <code>file2</code>.
 * <pre>
 *  IFSJavaFile file  = new IFSJavaFile(new AS400("enterprise"), "\\QSYS.LIB\\herlib");
 *  IFSJavaFile file2 = new IFSJavaFile(new AS400("enterprise"), "\\QSYS.LIB\\hislib");
 * <br>
 *  int returnCode = file.compareTo(file2);
 * </pre>
 *
 * <p>Note:<br>The comparison is case sensitive.
 *
 * @param   file The IFSJavaFile to be compared.
 *
 * @return  <code>0</code> if this IFSJavaFile path equals <code>file</code>'s path;
 *          a value less than <code>0</code> if this IFSJavaFile path is less than the <code>file</code>'s
 *          path; and a value greater than <code>0</code> if this IFSJavaFile path is greater
 *          than the <code>file's</code> path.
 *
 * @since JDK1.2
**/
  public int compareTo(IFSJavaFile file)
  {
    return getPath().compareTo(file.getPath());
  }

// @A7a
/**
 * Compares the path of IFSJavaFile with a <code>File</code>'s path.
 *
 * <p>Note:<br>The comparison is case sensitive.
 *
 * @param   file The <code>File</code> to be compared.
 *
 * @return  <code>0</code> if this IFSJavaFile path equals the argument's path;
 *          a value less than <code>0</code> if this IFSJavaFile path is less than the argument's
 *          path; and a value greater than <code>0</code> if this IFSJavaFile path is greater
 *          than the argument's path.
 *
 * @since JDK1.2
**/
  public int compareTo(File file)
  {
    return getPath().compareTo(file.getPath());
  }

// @A8a
/**
 * Compares the path of IFSJavaFile with a <code>IFSFile</code>'s path.
 *
 * <p>Note:<br>The comparison is case sensitive.
 *
 * @param   file The <code>IFSFile</code> to be compared.
 *
 * @return  <code>0</code> if this IFSJavaFile path equals the argument's path;
 *          a value less than <code>0</code> if this IFSJavaFile path is less than the argument's
 *          path; and a value greater than <code>0</code> if this IFSJavaFile path is greater
 *          than the argument's path.
 *
 * @since JDK1.2
**/
  public int compareTo(IFSFile file)
  {
    return getPath().compareTo(file.getPath());
  }


// Note: The JDK 6.0 compiler doesn't like the following method signature.
// It causes the following error:
// "name clash: compareTo(java.lang.Object) in IFSJavaFile and compareTo(T) in Comparable<java.io.File> have the same erasure, yet neither overrides the other"

// /**
// * Compares this object with another object.
// *
// * @param   obj The object to be compared.
// *
// * @return  <code>0</code> if this IFSJavaFile path equals the argument's path;
// *          a value less than <code>0</code> if this IFSJavaFile path is less than the argument's
// *          path; and a value greater than <code>0</code> if this IFSJavaFile path is greater
// *          than the argument's path.
// *
// * @exception ClassCastException if the specified object's type prevents it from being compared to this Object.
// * @since JDK1.2
//**/
//  public int compareTo(Object obj)
//  {
//    if (obj instanceof IFSJavaFile) return compareTo((IFSJavaFile)obj);
//    else if (obj instanceof File) return compareTo((File)obj);
//    else if (obj instanceof IFSFile) return compareTo((IFSFile)obj);
//    else {
//      String classname = (obj == null ? "null" : obj.getClass().getName());
//      throw new ClassCastException(classname);
//    }
//  }


  /**
   * Atomically create a new, empty file.  The file is
   * created if and only if the file does not yet exist.  The
   * check for existence and the file creation is a
   * single atomic operation.
   * @return true if the file is created, false otherwise.
   * @exception IOException If an I/O error occurs while communicating with the IBM i system.
   **/
   // @D1 - new method because of changes to java.io.file in Java 2.
  public boolean createNewFile()
                 throws IOException
  {
     return ifsFile_.createNewFile();
  }




/**
 * Deletes the IFSJavaFile.  If the target is
 * a directory, it must be empty for deletion to succeed.
 *
 * @return <code>true</code> if the file is successfully deleted;
 *         <code>false</code> otherwise.
 * @exception SecurityException  If the user is denied access to the file.
**/
  public boolean delete()
    throws SecurityException
  {
    try
    {
      int returnCode = ifsFile_.delete0();
      if ((returnCode == IFSReturnCodeRep.ACCESS_DENIED_TO_DIR_ENTRY)
      ||  (returnCode == IFSReturnCodeRep.ACCESS_DENIED_TO_REQUEST))
      {
        throw new SecurityException(ResourceBundleLoader.getText(mapRC(returnCode)));
      }
      return (returnCode == IFSReturnCodeRep.SUCCESS);
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return false;
    }
  }

/**
 * Compares this IFSJavaFile against the specified object.
 * Returns <code>true</code> if and only if the argument is
 * not null and is an IFSJavaFile
 * object whose path name is equal to the path name of
 * this IFSJavaFile,
 * and system names of the objects are equal.
 *
 * @param   obj The object to compare with.
 *
 * @return  <code>true</code> if the objects are the same;
 *          <code>false</code> otherwise.
**/
  public boolean equals(Object obj)
  {
    if (obj == null || !(obj instanceof IFSJavaFile))
      return false;

    // return getPath().equals(((IFSJavaFile)obj).getPath()); // @A4D

    // @A4A:
    IFSFile otherIfsFile = ((IFSJavaFile)obj).getIfsFile();
    return (ifsFile_.equals(otherIfsFile));
  }

/**
 * Indicates if the IFSJavaFile exists.
 *
 * @return <code>true</code> if the file specified by this object
 *         exists; <code>false</code> otherwise.
 * @exception SecurityException  If the user is denied read access to the file.
 * @exception SecurityException  If the user is denied access to the file.
**/
  public boolean exists()
    throws SecurityException
  {
    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    try
    {
      returnCode = ifsFile_.exists0();                              //@A5c
      if (returnCode == IFSReturnCodeRep.ACCESS_DENIED_TO_DIR_ENTRY
      ||  returnCode == IFSReturnCodeRep.ACCESS_DENIED_TO_REQUEST)
      {
        throw new SecurityException(ResourceBundleLoader.getText(mapRC(returnCode)));
      }
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return false;
    }
    return (returnCode == IFSReturnCodeRep.SUCCESS);
  }

/**
 * Returns An IFSJavaFile object based on the absolute path name of the
 * current object.  If the system property is set, it is copied to
 * the returned object.
 *
 * @return an IFSJavaFile object based on the absolute path name
 *         of the current object.
 *
 * @see  #getAbsolutePath()
**/
  // @D1 - new method because of changes to java.io.file in Java 2.

  public File getAbsoluteFile()
  {
    if (getSystem() != null)
       return new IFSJavaFile(getSystem(), this.getAbsolutePath());
    else
       return new IFSJavaFile(this.getAbsolutePath());
  }


/**
 * Returns the absolute path name of the IFSJavaFile.
 * This is the full path starting at the root directory.
 *
 * @return The absolute path name for this IFSJavaFile.
 *         All paths are absolute paths in the integrated file system.
 *
 * @see  #isAbsolute()
**/
  public String getAbsolutePath()
  {
    String pathString = ifsFile_.getAbsolutePath();
    if (pathString != null && replace_) //@P2C
    {
      return pathString.replace(AS400_SEPARATOR, separatorChar);
    }
    return pathString;
  }


/**
 * Returns An IFSJavaFile object based on the canonical path name of the
 * current object.  If the system property is set, it is
 * copied to the returned object.
 *
 * @return an IFSJavaFile object based on the canonical path name
 *         of the current object.
 *
 * @exception IOException If an I/O error occurs while communicating with the IBM i system.
 * @see #getCanonicalPath
**/
  // @D1 - new method because of changes to java.io.file in Java 2.

  public File getCanonicalFile() throws IOException
  {
    if (getSystem() != null)
       return new IFSJavaFile(getSystem(), this.getCanonicalPath());
    else
       return new IFSJavaFile(this.getCanonicalPath());
  }

/**
 * Returns the path name in canonical form of IFSJavaFile path.
 * This is the full path starting at the root directory.
 *
 * @return The canonical path name for this IFSJavaFile.
 *
 * @exception IOException If an I/O error occurs while communicating with the IBM i system.
**/
  public String getCanonicalPath() throws IOException
  {
    String pathString = ifsFile_.getCanonicalPath();
    if (pathString != null && replace_) //@P2C
    {
      return pathString.replace(AS400_SEPARATOR, separatorChar);
    }
    return pathString;
  }


  /**
   Returns the number of unallocated bytes in the partition named by this abstract path name.
   <p>
   The returned number of unallocated bytes is a hint, but not a guarantee, that it is possible to use most or any of these bytes. The number of unallocated bytes is most likely to be accurate immediately after this call. It is likely to be made inaccurate by any external I/O operations including those made on the system outside of this virtual machine. This method makes no guarantee that write operations to this file system will succeed.

   @return The number of unallocated bytes on the partition; or 0L if the abstract pathname does not name a partition. This value will be less than or equal to the total file system size returned by getTotalSpace(). 
   @exception SecurityException  If the user is denied access to the file.
   **/
  public long getFreeSpace()
    throws SecurityException
  {
    try
    {
      return ifsFile_.getAvailableSpace(false);
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return 0L;
    }
  }


  /**
   Returns the size of the partition named by this abstract pathname.

   @return The size, in bytes, of the partition; or 0L if this abstract pathname does not name a partition.
   @exception SecurityException  If the user is denied access to the file.
   **/
  public long getTotalSpace()
    throws SecurityException
  {
    try
    {
      return ifsFile_.getTotalSpace(false);
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return 0L;
    }
  }


  /**
   Returns the number of bytes available to this virtual machine on the partition named by this abstract pathname. When possible, this method checks for write permissions and other operating system restrictions and will therefore usually provide a more accurate estimate of how much new data can actually be written than getFreeSpace().
   <p>
   The returned number of available bytes is a hint, but not a guarantee, that it is possible to use most or any of these bytes. The number of unallocated bytes is most likely to be accurate immediately after this call. It is likely to be made inaccurate by any external I/O operations including those made on the system outside of this virtual machine. This method makes no guarantee that write operations to this file system will succeed.
   <p>
   Note: If the user profile has a "maximum storage allowed" setting of *NOMAX, then getUsableSpace() returns the same value as {@link #getFreeSpace getFreeSpace()}.

   @return The number of available bytes on the partition; or 0L if the abstract pathname does not name a partition. 
   @exception SecurityException  If the user is denied access to the file.
   **/
  public long getUsableSpace()
    throws SecurityException
  {
    try
    {
      return ifsFile_.getAvailableSpace(true);
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return 0L;
    }
  }


/**
 * Returns the IFSFile object contained within this IFSJavaFile.
 *
 * @return The IFSFile object contained within this IFSJavaFile.
**/
  IFSFile getIfsFile()
  {
    return ifsFile_;
  }


/**
 * Returns the name of the IFSJavaFile. The name
 * is everything in the path name after the last occurrence of the
 * separator character.
 * <p>
 * The following example demonstrates the use of this method:
 * <p>
 * In this example, fileName would equal "file.dat".
 * <pre>
 *  String path = "\\path\\file.dat";
 *  IFSJavaFile file  = new IFSJavaFile(new AS400("enterprise"), path);
 * <br>
 *  String fileName = file.getName();
 * </pre>
 *
 * @return The name (without any directory components) of this IFSJavaFile.
**/
  public String getName()
  {
    return ifsFile_.getName();
  }

/**
 * Returns the parent directory of the IFSJavaFile. The parent directory is everything in
 * the path name before the last occurrence of the separator
 * character, or null if the separator character does not appear in
 * the path name.
 * <p>
 * The following example demonstrates the use of this method:
 * <p>
 * In this example, parentPath would equal "\test".
 * <pre>
 *  String path = "\\test\\path";
 *  IFSJavaFile file  = new IFSJavaFile(new AS400("enterprise"), path);
 * <br>
 *  String parentPath = file.getParent();
 * </pre>
 *
 * @return The parent directory if one exists; null otherwise.
**/
  public String getParent()
  {
    String parentString = ifsFile_.getParent();
    if (parentString != null && replace_) //@P2C
    {
      return parentString.replace(AS400_SEPARATOR, separatorChar);
    }
    return parentString;
  }

/**
 * Returns an IFSJavaFile object that represents the parent of
 * the current object. The parent is the path name before the
 * last occurrence of the separator character.  Null is returned
 * null if the separator character does not appear in the path
 * the path or the current object is the file system root.
 * If the system property is set, it is also copied to the returned object.
 *
 * @return an IFSJavaFile object representing the
 * parent directory if one exists; null otherwise.
 *
 * @see  #getParent()
**/
  // @D1 - new method because of changes to java.io.file in Java 2.

  public File getParentFile()
  {
    if (getParent() == null)
       return null;
    if (getSystem() != null)
       return new IFSJavaFile(getSystem(), this.getParent());
    else
       return new IFSJavaFile(this.getParent());
  }



/**
 * Returns the path name of the IFSJavaFile.
 * <p>
 * The following example demonstrates the use of this method:
 * <p>
 * In this example, thePath would equal "\test\path" the same value as path.
 * <pre>
 *  String path = "\\test\\path";
 *  IFSJavaFile file  = new IFSJavaFile(new AS400("enterprise"), path);
 * <br>
 *  String thePath = file.getPath();
 * </pre>
 *
 * @return The file path name.
**/
  public String getPath()
  {
    String pathString = ifsFile_.getPath();
    if (pathString != null && replace_) //@P2C
    {
      return pathString.replace(AS400_SEPARATOR, separatorChar);
    }
    return pathString;
  }


  /**
   Returns the pattern-matching behavior used when files are listed by any of the <tt>list()</tt> or <tt>listFiles()</tt> methods.  The default is PATTERN_POSIX.
   @return Either {@link IFSFile#PATTERN_POSIX PATTERN_POSIX}, {@link IFSFile#PATTERN_POSIX_ALL PATTERN_POSIX_ALL}, or {@link IFSFile#PATTERN_OS2 PATTERN_OS2}
   **/
  public int getPatternMatching()
    throws IOException
  {
    return ifsFile_.getPatternMatching();
  }

/**
 * Returns the system that this object references.
 *
 * @return The system object.
**/
  public AS400 getSystem()
  {
    return ifsFile_.getSystem();
  }

/**
 * Computes a hashcode for this object.
 *
 * @return A hash code value for this object.
**/
  public int hashCode()
  {
    return ifsFile_.hashCode();
  }

/**
 * Indicates if the path name of this IFSJavaFile is an
 * absolute path name.
 *
 * @return <code>true</code> if the path name specification is absolute; <code>false</code> otherwise.
**/
  public boolean isAbsolute()
  {
    // Formerly, this method always reported true, since IFSFile.isAbsolute always reports true.
    // In order for JFileChooser to work properly (when used in combination with IFSFileSystemView), instances of IFSJavaFile must remember whether they were created with an absolute path.  The underlying IFSFile instance assumes it was created with an absolute path, so we can't rely on the IFSFile to keep track of that for us.
    // More details: There is logic in javax.swing.plaf.basic.BasicFileChooserUI#ApproveSelectionAction.actionPerformed() that calls File.isAbsolute().  If not absolute, BasicFileChooserUI prepends the directory path and creates a new (absolute) File object.  For that reason, each instance of IFSJavaFile needs to remember whether it was created with an absolute path or not.

    //return ifsFile_.isAbsolute();
    return absolute_;
  }


  // Utility method for determining whether a path is absolute.
  static boolean isAbsolutePath(String path) {
    if (path != null && path.length() != 0 && path.charAt(0) == separatorChar) {
      return true;
    }
    else return false;
  }

/**
 * Indicates if the IFSJavaFile is a directory.
 *
 * @return <code>true</code> if the IFSJavaFile exists and is a directory; <code>false</code> otherwise.
 * @exception SecurityException  If the user is denied access to the file.
**/
  public boolean isDirectory()
    throws SecurityException
  {
    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    try
    {
      returnCode = ifsFile_.isDirectory0();
      if (returnCode == IFSReturnCodeRep.ACCESS_DENIED_TO_DIR_ENTRY
      ||  returnCode == IFSReturnCodeRep.ACCESS_DENIED_TO_REQUEST)
      {
        throw new SecurityException(ResourceBundleLoader.getText(mapRC(returnCode)));
      }
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return false;
    }
    return (returnCode == IFSReturnCodeRep.SUCCESS);
  }

/**
 * Indicates if the IFSJavaFile is a "normal" file.<br>
 * A file is "normal" if it is not a directory or a container of other objects.
 *
 * @return <code>true</code> if the specified file exists and is a "normal" file; <code>false</code> otherwise.
 * @exception SecurityException  If the user is denied access to the file.
**/
  public boolean isFile()
    throws SecurityException
  {
    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    try
    {
      returnCode = ifsFile_.isFile0();
      if (returnCode == IFSReturnCodeRep.ACCESS_DENIED_TO_DIR_ENTRY
      ||  returnCode == IFSReturnCodeRep.ACCESS_DENIED_TO_REQUEST)
      {
        throw new SecurityException(ResourceBundleLoader.getText(mapRC(returnCode)));
      }
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return false;
    }
    return (returnCode == IFSReturnCodeRep.SUCCESS);
  }


/**
 * Indicates if the IFSJavaFile is hidden.  On the IBM i system, a file is
 * hidden if its hidden attribute is set.
 *
 * @return <code>true</code> if the file is hidden; <code>false</code> otherwise.
 * @exception SecurityException  If the user is denied access to the file.
**/
  // @D1 - new method because of changes to java.io.file in Java 2.

  public boolean isHidden()
    throws SecurityException
  {
     try
     {
        return ifsFile_.isHidden();
     }
     catch (AS400SecurityException e)
     {
        throw new SecurityException(e.getMessage());
     }
     catch (IOException e)
     {
       if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
       return false;
     }
  }


/**
 * Indicates the time that the IFSJavaFile was last modified.
 *
 * @return The time (measured in milliseconds since
 * 01/01/1970 00:00:00 GMT) that the IFSJavaFile was
 * last modified, or <code>0</code> if it does not exist.
 * @exception SecurityException  If the user is denied access to the file.
**/
  public long lastModified()
    throws SecurityException
  {
    try
    {
      return ifsFile_.lastModified0();
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return 0L;
    }
  }

/**
 * Indicates the length of this IFSJavaFile.
 *
 * @return The length, in bytes, of the IFSJavaFile,
 * or <code>0</code>((IFSJavaFile) if it does not exist.
 * @exception SecurityException  If the user is denied access to the file.
**/
  public long length()
    throws SecurityException
  {
    try
    {
      // For the listFiles() methods, the IFS File Server reports the length of 
      // a symbolic link rather than the length of the target object.  Circumvention
      // is to clear the ifsFile cache and re-retrieve the attributes from the IFS
      // File Server.  When not using wild-cards the IFS File Server always follows
      // symlnk objects and returns the attributes of the target object.
      if (ifsFile_.isSymbolicLink())                 //@B1A
        ifsFile_.clearCachedAttributes();            //@B1A
      return ifsFile_.length0();
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return 0L;
    }
  }

/**
 * Lists the files in this IFSJavaFile directory.
 *
 * @return An array of object names in the directory.
 * This list does not include the current directory
 * or the parent directory.  If this IFSJavaFile is not
 * a directory, null is returned.
 * If this IFSJavaFile is an empty directory,
 * an empty string array is returned.
 * @see #listFiles()
 * @exception SecurityException  If the user is denied access to the file.
**/
  public String[] list()
    throws SecurityException
  {
    try
    {
      return ifsFile_.list0(null, "*");
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return new String[0];
    }
  }

/**
 * Lists the files in this IFSJavaFile directory that satisfy <i>filter</i>.
 *
 * @param   filter The file name filter.
 *
 * @return  An array of object names in the directory that satisfy
 * the file name filter. This list does not include the current
 * directory or the parent directory.  If this IFSJavaFile is not
 * a directory, null is returned. If this IFSJavaFile is an empty
 * directory, or the file name filter does
 * not match any files, an empty string array is returned.
 * The IFSJavaFile object passed to the file name filter object have cached
 * file attribute information.  Maintaining references to these
 * IFSJavaFile objects after the list operation increases the
 * chances that their file attribute information will not be valid.
 * <p>
 * The following example demonstrates the use of this method:
 * <pre>
 *  class AcceptClass implements java.io.FilenameFilter
 *  {
 *    public boolean accept(java.io.File dir, java.lang.String name)
 *    {
 *      if (name.startsWith("IFS"))
 *        return true;
 *      return false;
 *    }
 *  }
 * <br>
 *  IFSJavaFile file = new IFSJavaFile(new AS400("enterprise"), path);
 *  AcceptClass ac = new AcceptClass();
 *  file.list(ac);
 *</pre>
 * @exception SecurityException  If the user is denied access to the file.
 * @see #listFiles(FilenameFilter)
**/
  public String[] list(FilenameFilter filter)
    throws SecurityException
  {
    String names[] = null;

    try
    {
      names = ifsFile_.list0(null, "*");
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return null;
    }

    if (names == null)
    {
      return null;
    }

    // Fill in the Vector
    Vector v = new Vector();
    for (int i = 0 ; i < names.length ; i++)
    {
      if ((filter == null) || filter.accept(this, names[i]))
      {
        v.addElement(names[i]);
      }
    }

    // Create the array
    String files[] = new String[v.size()];
    v.copyInto(files);

    return files;
  }

/**
 * Lists the files in the IFSJavaFile directory that satisfy <i>file name filter</i>.
 *
 * @param   filter The file name filter.
 *
 * @return  An array of object names in the directory that
 * satisfy the file name filter. This list does not include the current
 * directory or the parent directory.
 * If this IFSJavaFile is not a directory, null is
 * returned. If this IFSJavaFile is an empty directory, or
 * the file name filter does not match any files, an empty string array
 * is returned. The IFSFile object passed to the file name filter object
 * have cached file attribute information.  Maintaining
 * references to these IFSFile objects after the list operation
 * increases the chances that their file attribute information
 * will not be valid.
 * <p>
 * The following example demonstrates the use of this method:
 * <pre>
 *  class AcceptClass implements IFSFileFilter
 *  {
 *    public boolean accept(IFSFile file)
 *    {
 *      if (file.getName().startsWith("IFS"))
 *        return true;
 *      return false;
 *    }
 *  }
 * <br>
 *  IFSJavaFile file = new IFSJavaFile(new AS400("enterprise"), path);
 *  AcceptClass ac = new AcceptClass();
 *  file.list(ac);
 *
 *</pre>
 * @exception SecurityException  If the user is denied access to the file.
 * @see #listFiles(IFSFileFilter)
**/
  public String[] list(IFSFileFilter filter)
    throws SecurityException
  {
    try
    {
      return ifsFile_.list0(filter,"*");
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return new String[0];
    }
  }

/**
 * Lists the files in this IFSJavaFile directory that satisfy <i>filter</i> and <i>pattern</i>.
 *
 * <p>Note:<br>If the file does not match <i>pattern</i>, it will not be processed by <i>filter</i>.
 *
 * @param   filter  The file name filter.
 * @param   pattern The pattern that all filenames must match. Acceptable characters are
 *          wildcards (* - matches multiple characters) and question marks (? - matches
 *          one character).  Pattern must not be null.
 *
 * @return  An array of object names in the directory that satisfy the file name filter
 *          and pattern.  This list does not include the current directory or the parent
 *          directory.  If this IFSJavaFile is not a directory, null is returned. If this
 *          IFSJavaFile is an empty directory, or the file name filter or pattern does not
 *          match any files, an empty string array is returned. The IFSFile object passed
 *          to the file name filter object have cached file attribute information.
 *          Maintaining references to these IFSFile objects after the list operation
 *          increases the chances that their file attribute information will not be valid.
 * @exception SecurityException  If the user is denied access to the file.
 * @see #listFiles(IFSFileFilter,String)
**/
  public String[] list(IFSFileFilter filter, String pattern)
    throws SecurityException
  {
    if (pattern == null)
      throw new NullPointerException("pattern");

    try
    {
      return ifsFile_.list0(filter, pattern);
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return new String[0];
    }
  }

/**
 * Lists the files in this IFSJavaFile directory that match <i>pattern</i>.
 *
 * @param   pattern The pattern that all filenames must match.
 *          Acceptable characters are wildcards (* - matches
 *          multiple characters) and question marks (? - matches
 *          one character).
 *
 * @return  An array of object names in the directory that match
 *          the pattern. This list does not include the current
 *          directory or the parent directory.  If this IFSJavaFile
 *          is not a directory, null is returned.  If this IFSJavaFile
 *          is an empty directory, or the pattern does not match any
 *          files, an empty string array is returned.
 * @exception SecurityException  If the user is denied access to the file.
 * @see #listFiles(String)
**/
  public String[] list(String pattern)
    throws SecurityException
  {
    if (pattern == null)
      throw new NullPointerException("pattern");

    try
    {
      return ifsFile_.list0(null,pattern);
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return new String[0];
    }
  }


//@A3A Added support for IFSFile.listFiles().
/**
 * Lists the files in this IFSJavaFile directory.
 * With the use of this function, attribute information is cached and
 * will not be refreshed from the IBM i system.  This means attribute information may
 * become inconsistent with the IBM i system.
 * @return An array of objects in the directory.
 * This list does not include the current directory
 * or the parent directory.  If this IFSJavaFile is not
 * a directory, null is returned.
 * If this IFSJavaFile is an empty directory,
 * an empty object array is returned.
 * @exception SecurityException  If the user is denied access to the file.
 * @see #list()
**/
  public File[] listFiles()
    throws SecurityException
  {
     return listFiles(null,"*");
  }

//@A3A Added support for IFSFile.listFiles().
/**
 * Lists the files in this IFSJavaFile directory that satisfy <i>filter</i>.
 * With the use of this function, attribute information is cached and
 * will not be refreshed from the IBM i system.  This means attribute information may
 * become inconsistent with the IBM i system.
 *
 * @param   filter The file name filter.
 *
 * @return  An array of objects in the directory that satisfy
 * the file name filter. This list does not include the current
 * directory or the parent directory.  If this IFSJavaFile is not
 * a directory, null is returned. If this IFSJavaFile is an empty
 * directory, or the file name filter does
 * not match any files, an empty object array is returned.
 * The IFSJavaFile object passed to the file name filter object has cached
 * file attribute information.  Maintaining references to these
 * IFSJavaFile objects after the list operation increases the
 * chances that their file attribute information will not be valid.
 * <p>
 * The following example demonstrates the use of this method:
 * <pre>
 *  class AcceptClass implements java.io.FilenameFilter
 *  {
 *    public boolean accept(java.io.File dir, java.lang.String name)
 *    {
 *      if (name.startsWith("IFS"))
 *        return true;
 *      return false;
 *    }
 *  }
 * <br>
 *  IFSJavaFile file = new IFSJavaFile(new AS400("enterprise"), path);
 *  AcceptClass ac = new AcceptClass();
 *  file.listFiles(ac);
 *</pre>
 * @exception SecurityException  If the user is denied access to the file.
 * @see #list(FilenameFilter)
**/
  public File[] listFiles(FilenameFilter filter)
    throws SecurityException
  {
    IFSFile[] files = null;
    try
    {
      files = ifsFile_.listFiles0(null, "*");             // @D2C @C3C
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return null;
    }

    if (files == null)
    {
      return null;
    }

    // Fill in the Vector
    Vector v = new Vector();
    for (int i = 0 ; i < files.length ; i++)
    {
      if ((filter == null) || filter.accept(this, files[i].getName()))
      {
        //v.addElement(files[i]);                         // @A6d
        v.addElement(new IFSJavaFile(files[i]));          // @A6a
      }
    }

    // Create the array
    IFSJavaFile newFiles[] = new IFSJavaFile[v.size()];   // @A6c
/* @A6d
    for (int i = 0; i < files.length; i++)
    {
       newFiles[i] = new IFSJavaFile(files[i]);
    }
*/
    v.copyInto(newFiles);  // @A6a

    return newFiles;
  }

// @A7a
/**
 * Lists the files in this IFSJavaFile directory that satisfy <i>filter</i>.
 * With the use of this function, attribute information is cached and
 * will not be refreshed from the IBM i system.  This means attribute information may
 * become inconsistent with the IBM i system.
 *
 * @param   filter The file filter.
 *
 * @return  An array of objects in the directory that satisfy
 * the file filter. This list does not include the current
 * directory or the parent directory.  If this IFSJavaFile is not
 * a directory, null is returned. If this IFSJavaFile is an empty
 * directory, or the file filter does
 * not match any files, an empty object array is returned.
 * The IFSJavaFile object passed to the file filter object has cached
 * file attribute information.  Maintaining references to these
 * IFSJavaFile objects after the list operation increases the
 * chances that their file attribute information will not be valid.
 * <p>
 * The following example demonstrates the use of this method:
 * <pre>
 *  class AcceptClass implements java.io.FileFilter
 *  {
 *    public boolean accept(java.io.File file)
 *    {
 *      if (file.getName().startsWith("IFS"))
 *        return true;
 *      return false;
 *    }
 *  }
 * <br>
 *  IFSJavaFile file = new IFSJavaFile(new AS400("enterprise"), path);
 *  AcceptClass ac = new AcceptClass();
 *  file.listFiles(ac);
 *</pre>
 * @exception SecurityException  If the user is denied access to the file.
**/
  public File[] listFiles(FileFilter filter)
    throws SecurityException
  {
    IFSFile[] files = null;
    try
    {
      files = ifsFile_.listFiles0(null, "*");   // @C3C
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return null;
    }

    if (files == null)
    {
      return null;
    }

    // Fill in the Vector
    Vector v = new Vector();
    for (int i = 0 ; i < files.length ; i++)
    {
      IFSJavaFile file = new IFSJavaFile(files[i]);
      if ((filter == null) || filter.accept(file))
      {
        v.addElement(file);
      }
    }

    // Create the array
    IFSJavaFile newFiles[] = new IFSJavaFile[v.size()];
    v.copyInto(newFiles);

    return newFiles;
  }

//@A3A Added support for IFSFile.listFiles().
/**
 * Lists the files in the IFSJavaFile directory that satisfy <i>file name filter</i>.
 * With the use of this function, attribute information is cached and
 * will not be refreshed from the IBM i system.  This means attribute information may
 * become inconsistent with the IBM i system.
 * @param   filter The file name filter.
 *
 * @return  An array of objects in the directory that
 * satisfy the file name filter. This list does not include the current
 * directory or the parent directory.
 * If this IFSJavaFile is not a directory, null is
 * returned. If this IFSJavaFile is an empty directory, or
 * the file name filter does not match any files, an empty object array
 * is returned. The IFSFile object passed to the file name filter object
 * has cached file attribute information.  Maintaining
 * references to these IFSFile objects after the list operation
 * increases the chances that their file attribute information
 * will not be valid.
 * <p>
 * The following example demonstrates the use of this method:
 * <pre>
 *  class AcceptClass implements IFSFileFilter
 *  {
 *    public boolean accept(IFSFile file)
 *    {
 *      if (file.getName().startsWith("IFS"))
 *        return true;
 *      return false;
 *    }
 *  }
 * <br>
 *  IFSJavaFile file = new IFSJavaFile(new AS400("enterprise"), path);
 *  AcceptClass ac = new AcceptClass();
 *  file.listFiles(ac);
 *
 *</pre>
 * @exception SecurityException  If the user is denied access to the file.
 * @see #list(IFSFileFilter)
**/
  public File[] listFiles(IFSFileFilter filter)
    throws SecurityException
  {
     return listFiles(filter,"*");
  }

//@A3A Added support for IFSFile.listFiles().
/**
 * Lists the files in this IFSJavaFile directory that satisfy <i>filter</i> and
 * <i>pattern</i>.  With the use of this function, attribute information is cached and
 * will not be refreshed from the IBM i system.  This means attribute information may
 * become inconsistent with the IBM i system.
 *
 * <p>Note:<br>If the file does not match <i>pattern</i>, it will not be processed by <i>filter</i>.
 *
 * @param   filter  The file name filter.
 * @param   pattern The pattern that all filenames must match. Acceptable characters are
 *          wildcards (* - matches multiple characters) and question marks (? - matches
 *          one character).  Pattern must not be null.
 *
 * @return  An array of objects in the directory that satisfy the file name filter
 *          and pattern.  This list does not include the current directory or the parent
 *          directory.  If this IFSJavaFile is not a directory, null is returned. If this
 *          IFSJavaFile is an empty directory, or the file name filter or pattern does not
 *          match any files, an empty object array is returned. The IFSFile object passed
 *          to the file name filter object has cached file attribute information.
 *          Maintaining references to these IFSFile objects after the list operation
 *          increases the chances that their file attribute information will not be valid.
 * @exception SecurityException  If the user is denied access to the file.
 * @see #list(IFSFileFilter,String)
**/
  public File[] listFiles(IFSFileFilter filter, String pattern)
    throws SecurityException
  {
    if (pattern == null)
      throw new NullPointerException("pattern");

    try
    {
      IFSFile files[] = ifsFile_.listFiles0(filter,pattern);            // @D2C @C3C
      IFSJavaFile newFiles[] = new IFSJavaFile[files.length];
      for (int i = 0; i < files.length; i++)
      {
         newFiles[i] = new IFSJavaFile(files[i]);
      }
      return newFiles;
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return new IFSJavaFile[0];
    }
  }

//@A3A Added support for IFSFile.listFiles().
/**
 * Lists the files in this IFSJavaFile directory that match <i>pattern</i>.
 * With the use of this function, attribute information is cached and
 * will not be refreshed from the IBM i system.  This means attribute information may
 * become inconsistent with the IBM i system.
 *
 * @param   pattern The pattern that all filenames must match.
 *          Acceptable characters are wildcards (* - matches
 *          multiple characters) and question marks (? - matches
 *          one character).
 *
 * @return  An array of object names in the directory that match
 *          the pattern. This list does not include the current
 *          directory or the parent directory.  If this IFSJavaFile
 *          is not a directory, null is returned.  If this IFSJavaFile
 *          is an empty directory, or the pattern does not match any
 *          files, an empty string array is returned.
 * @exception SecurityException  If the user is denied access to the file.
 * @see #list(String)
**/
  public File[] listFiles(String pattern)
    throws SecurityException
  {
    if (pattern == null)
      throw new NullPointerException("pattern");

    return listFiles(null, pattern);
  }


/**
 * Lists the file system roots for the integrated file system
 * of the IBM i system.  The IBM i integrated file system has
 * only one root -- "/".
 *
 * @return  An array of IFSJavaFile objects that represent the
 *          file system roots of the integrated file system
 *          of the IBM i system.  Since the IBM i integrated file system
 *          has only one root, the returned
 *          array contains only one element.
**/
  //@D1a New method because of changes in Java 2.
  public static File[] listRoots()
  {
     IFSJavaFile[] roots = new IFSJavaFile[1];
     //roots[0] = new IFSJavaFile(IFSFile.separator);  // @A6d
     roots[0] = new IFSJavaFile(File.separator);  // @A6c
     return roots;
  }



// convert return code to string for mri file
  private String mapRC(int returnCode)
  {
    if (returnCode== IFSReturnCodeRep.ACCESS_DENIED_TO_DIR_ENTRY)
    {
      return "EXC_ACCESS_DENIED";
    } else
    if (returnCode == IFSReturnCodeRep.ACCESS_DENIED_TO_REQUEST)
    {
      return "EXC_REQUEST_DENIED";
    }
    return "EXC_UNKNOWN";   // Bad return code was provided.
  }

/**
 * Creates a directory whose path name
 * is specified by this IFSJavaFile.

 *
 * @return <code>true</code> if the directory could be created;
 *         <code>false</code> otherwise.
 * @exception SecurityException  If the user is denied access to the file.
**/
  public boolean mkdir()
    throws SecurityException
  {
    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    try
    {
      returnCode = ifsFile_.mkdir0(ifsFile_.getAbsolutePath());
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return false;
    }
    return (returnCode == IFSReturnCodeRep.SUCCESS);
  }

/**
 * Creates a directory whose path name is
 * specified by this IFSJavaFile, including any necessary
 * parent directories.
 *
 * @return <code>true</code> if the directory (or directories) could be
 *         created; <code>false</code> otherwise.
 * @exception SecurityException  If the user is denied access to the file.
**/
  public boolean mkdirs()
    throws SecurityException
  {
    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    try
    {
      returnCode = ifsFile_.mkdirs0();
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return false;
    }
    return (returnCode == IFSReturnCodeRep.SUCCESS);
  }

/**
 * Renames the IFSJavaFile to have the path name of <i>dest</i>.
 * Wildcards are not permitted in this file name.
 *
 * @param   dest The new filename.
 *
 * @return  <code>true</code> if the renaming succeeds;
 *          <code>false</code> otherwise.
 * @exception SecurityException  If the user is denied access to the file.
**/
  public boolean renameTo(IFSJavaFile dest)
    throws SecurityException
  {
    if (dest == null)
      throw new NullPointerException("dest");

    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    IFSFile lFile = new IFSFile();

    try
    {
      lFile.setSystem(dest.getSystem());
      String destPath = dest.getPath(); //@P2A
      lFile.setPath(replace_ ? destPath.replace(separatorChar, AS400_SEPARATOR) : destPath); //@P2C
      returnCode = ifsFile_.renameTo0(lFile);
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (PropertyVetoException e) {}  // will never happen
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return false;
    }
    return (returnCode == IFSReturnCodeRep.SUCCESS);
  }

// @A7a
/**
 * Renames the IFSJavaFile to have the path name of <i>dest</i>.
 * Wildcards are not permitted in this file name.
 *
 * @param   dest An object specifying the new filename.
 *          If this object is not an IFSJavaFile, the rename will fail.
 *
 * @return  <code>true</code> if the renaming succeeds;
 *          <code>false</code> otherwise.
 * @exception SecurityException  If the user is denied access to the file.
**/
  public boolean renameTo(File dest)
    throws SecurityException
  {
    try {
      return renameTo((IFSJavaFile)dest);
    }
    catch (ClassCastException e) {
      Trace.log(Trace.ERROR, e);
      return false;
    }
  }


/**
 * Sets the last modified time of the file named by this
 * IFSJavaFile object.
 *
 * @param time The new last modified time, measured in milliseconds since
 *        00:00:00 GMT, January 1, 1970.
 *        If -1, sets the last modified time to the current system time.
 * @return <code>true</code> if the time is set; <code>false</code> otherwise.
 * @exception SecurityException  If the user is denied access to the file.
**/
  // @D1 - new method because of changes to java.io.file in Java 2.
  // @B8c - Documented new behavior if argument is -1.

  public boolean setLastModified(long time)
    throws SecurityException
  {
     try
     {
        return ifsFile_.setLastModified0(time);
     }
     catch (AS400SecurityException e)
     {
       Trace.log(Trace.ERROR, e);
       throw new SecurityException(e.getMessage());
     }
     catch (IOException e)
     {
        if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
        return false;
     }
  }

  // @B8a
  /**
   * Sets the length of the file named by this
   * IFSJavaFile object.  The file can be made larger or smaller.
   * If the file is made larger, the contents of the new bytes
   * of the file are undetermined.
   * @param length The new length, in bytes.
   * @return true if successful; false otherwise.
   * @exception SecurityException  If the user is denied access to the file.
   **/
  public boolean setLength(int length)
    throws SecurityException
  {
     try
     {
        return ifsFile_.setLength0(length);
     }
     catch (AS400SecurityException e)
     {
       Trace.log(Trace.ERROR, e);
       throw new SecurityException(e.getMessage());
     }
     catch (IOException e)
     {
        if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
        return false;
     }
  }


/**
 * Sets the path for this IFSJavaFile.
 *
 * @param   path The absolute file path.
 *
 * @return  <code>true</code> if the path was set;
 *          <code>false</code> otherwise.
**/
  public boolean setPath(String path)
  {
    if (path == null)
    {
      throw new NullPointerException("path");
    }

    try
    {
      ifsFile_.setPath(replace_ ? path.replace(separatorChar, AS400_SEPARATOR) : path); //@P2C
      absolute_ = isAbsolutePath(path);
    }
    catch (PropertyVetoException e) {}  // will never happen
    return true;
  }


  /**
   Sets the pattern-matching behavior used when files are listed by any of the <tt>list()</tt> or <tt>listFiles()</tt> methods.  The default is PATTERN_POSIX.
   @param patternMatching Either {@link IFSFile#PATTERN_POSIX PATTERN_POSIX}, {@link IFSFile#PATTERN_POSIX_ALL PATTERN_POSIX_ALL}, or {@link IFSFile#PATTERN_OS2 PATTERN_OS2}

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the system.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the host server cannot be started.
   @exception UnknownHostException If the system cannot be located.
   **/
  public void setPatternMatching(int patternMatching)
    throws IOException
  {
    ifsFile_.setPatternMatching(patternMatching);
  }


/**
 * Marks the file named by this IFSJavaFile object so that only
 * read operations are allowed.  On the IBM i system, a file is marked
 * read only by setting the read only attribute of the file.
 *
 * @return <code>true</code> if the read only attribute is set; <code>false</code> otherwise.
 * @exception SecurityException  If the user is denied access to the file.
**/
   // @D1 - new method because of changes to java.io.file in Java 2.

  public boolean setReadOnly()
    throws SecurityException
  {
     try
     {
        return ifsFile_.setReadOnly0(true);
     }
     catch (AS400SecurityException e)
     {
       Trace.log(Trace.ERROR, e);
       throw new SecurityException(e.getMessage());
     }
     catch (IOException e)
     {
        if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
        return false;
     }
  }



/**
 * Sets the system.
 *
 * @param   system The system object.
 *
 * @return  <code>true</code> if the system was set;
 *          <code>false</code> otherwise.
**/
  public boolean setSystem(AS400 system)
  {
    if (system == null)
    {
      throw new NullPointerException("system");
    }

    try
    {
      ifsFile_.setSystem(system);
    }
    catch (PropertyVetoException e) {}  // will never happen
    return true;
  }


  /**
   A convenience method to set the owner's execute permission for this abstract pathname.
   This method is supported for IBM i V5R1 and higher. For older releases, it simply returns false.
   <p>
   An invocation of this method of the form <tt>file.setExcutable(arg)</tt> behaves in exactly the same way as the invocation <tt>file.setExecutable(arg, true)</tt> 

   @param executable  If true, sets the access permission to allow execute operations; if false, to disallow execute operations.
   @return true if and only if the operation succeeded. The operation will fail if the user does not have permission to change the access permissions of this abstract pathname.
   @exception SecurityException  If the user is denied access to the file.
   **/
  public boolean setExecutable(boolean executable)
    throws SecurityException
  {
    return setExecutable(executable, true);
  }


  /**
   Sets the owner's or everybody's execute permission for this abstract pathname.
   This method is supported for IBM i V5R1 and higher. For older releases, it simply returns false.

   @param executable  If true, sets the access permission to allow execute operations; if false, to disallow execute operations.
   @param ownerOnly  If true, the execute permission applies only to the owner's execute permission; otherwise, it applies to everybody.
   @return true if and only if the operation succeeded. The operation will fail if the user does not have permission to change the access permissions of this abstract pathname.
   @exception SecurityException  If the user is denied access to the file.
   **/
  public boolean setExecutable(boolean executable,
                               boolean ownerOnly)
    throws SecurityException
  {
    try
    {
      return ifsFile_.setAccess(IFSFile.ACCESS_EXECUTE, executable, ownerOnly);
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return false;
    }
  }


  /**
   A convenience method to set the owner's read permission for this abstract pathname.
   This method is supported for IBM i V5R1 and higher. For older releases, it simply returns false.
   <p>
   An invocation of this method of the form <tt>file.setReadable(arg)</tt> behaves in exactly the same way as the invocation <tt>file.setReadable(arg, true)</tt>

   @param readable  If true, sets the access permission to allow read operations; if false, to disallow read operations.
   @return true if and only if the operation succeeded. The operation will fail if the user does not have permission to change the access permissions of this abstract pathname.
   @exception SecurityException  If the user is denied access to the file.
   **/
  public boolean setReadable(boolean readable)
    throws SecurityException
  {
    return setReadable(readable, true);
  }


  /**
   Sets the owner's or everybody's read permission for this abstract pathname.
   This method is supported for IBM i V5R1 and higher. For older releases, it simply returns false.

   @param readable  If true, sets the access permission to allow read operations; if false, to disallow read operations.
   @param ownerOnly  If true, the read permission applies only to the owner's read permission; otherwise, it applies to everybody.
   @return true if and only if the operation succeeded. The operation will fail if the user does not have permission to change the access permissions of this abstract pathname.
   @exception SecurityException  If the user is denied access to the file.
   **/
  public boolean setReadable(boolean readable,
                             boolean ownerOnly)
    throws SecurityException
  {
    try
    {
      return ifsFile_.setAccess(IFSFile.ACCESS_READ, readable, ownerOnly);
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return false;
    }
  }


  /**
   A convenience method to set the owner's write permission for this abstract pathname.
   This method is supported for IBM i V5R1 and higher. For older releases, it simply returns false.
   <p>
   An invocation of this method of the form <tt>file.setWritable(arg)</tt> behaves in exactly the same way as the invocation <tt>file.setWritable(arg, true)</tt>

   @param writable  If true, sets the access permission to allow write operations; if false to disallow write operations 
   @return true if and only if the operation succeeded. The operation will fail if the user does not have permission to change the access permissions of this abstract pathname. 
   @exception SecurityException  If the user is denied access to the file.
   **/
  public boolean setWritable(boolean writable)
    throws SecurityException
  {
    return setWritable(writable, true);
  }


  /**
   Sets the owner's or everybody's write permission for this abstract pathname.
   This method is supported for IBM i V5R1 and higher. For older releases, it simply returns false.

   @param writable  If true, sets the access permission to allow write operations; if false, to disallow write operations.
   @param ownerOnly  If true, the write permission applies only to the owner's write permission; otherwise, it applies to everybody.
   @return true  if and only if the operation succeeded. The operation will fail if the user does not have permission to change the access permissions of this abstract pathname. 
   @exception SecurityException  If the user is denied access to the file.
   **/
  public boolean setWritable(boolean writable,
                             boolean ownerOnly)
    throws SecurityException
  {
    try
    {
      return ifsFile_.setAccess(IFSFile.ACCESS_WRITE, writable, ownerOnly);
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, e);
      throw new SecurityException(e.getMessage());
    }
    catch (IOException e)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, e);
      return false;
    }
  }



/**
 * Returns a string representation of this object.
 *
 * @return A string giving the path name of this object.
**/
  public String toString()
  {
    if (replace_)
    {
      return ifsFile_.toString().replace(AS400_SEPARATOR, separatorChar); 
    }
    else {
      return ifsFile_.toString();
    }

//    return replace_ ? ifsFile_.toString().replace(AS400_SEPARATOR, separatorChar) : ifsFile_.toString(); //@P2C
  }


/**
 * Converts the abstract path name into a <code>file:</code> URL.
 * The IBM i file/directory will be accessed and if it is a directory the
 * resulting URL will end with the IBM i separator character
 * (forward slash).  The system name will be obtained from
 * the AS400 object.  If the path name or AS400 object has
 * not been set, a NullPointerException will be thrown.
 *
 * @return The URL form of the abstract path name of this object.
 *
 * @exception MalformedURLException If the URL cannot be formed.
 *
**/
   // @D1 - new method because of changes to java.io.file in Java 2.

  public URL toURL() throws MalformedURLException
  {
     String objectName = null;

     if (isDirectory())
        objectName = getAbsolutePath() + separatorChar;
     else
        objectName = getAbsolutePath();

     return new URL(URL_PROTOCOL, getSystem().getSystemName(), objectName);
  }



}

