///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileOutputStream.java
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
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;


/**
 The IFSFileOutputStream class represents an integrated file system file output stream.
 An integrated file system file output stream is an output stream for writing
 data to integrated file system objects.
 <br>
 IFSFileOutputStream objects
 are capable of generating file events that call the following FileListener
 methods: fileClosed, fileModified, and fileOpended.
 <br>
 The following example illustrates the use of IFSFileOutputStream:
 <pre>
 // Work with /Dir/File on the system eniac.
 AS400 as400 = new AS400("eniac");
 IFSFileOutputStream file = new IFSFileOutputStream(as400, "/Dir/File");<br>
 // Lock the first 8 bytes of the file.
 IFSKey key = file.lock(8);<br>
 // Write 8 bytes to the file.
 byte[] data = { 0, 1, 2, 3, 4, 5, 6, 7 };
 file.write(data, 0, 8);<br>
 // Unlock the first 8 bytes.
 file.unlock(key);<br>
 // Close the file.
 file.close();
 </pre>
 @see com.ibm.as400.access.FileEvent
 @see #addFileListener
 @see #removeFileListener
**/
public class IFSFileOutputStream extends OutputStream
  implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;


  /**
   Share option that allows read and write access by other users.
   **/
  public final static int SHARE_ALL = 0xffffffff;
  /**
   Share option that does not allow read or write access by other users.
   **/
  public final static int SHARE_NONE = 0xfffffffc;
  /**
   Share option that allows only read access by other users.
   **/
  public final static int SHARE_READERS = 0xfffffffd;
  /**
   Share option that allows only write access by other users.
   **/
  public final static int SHARE_WRITERS = 0xfffffffe;


  transient protected PropertyChangeSupport changes_;
  transient protected VetoableChangeSupport vetos_;
  protected IFSFileDescriptor fd_; // file info - never allowed to be null

  

  transient Vector fileListeners_;
  transient IFSFileOutputStreamImpl impl_;
                // Note: Leave impl_ available to subclasses.

  private boolean append_ = false;

  private int ccsid_ = -1;  // The target CCSID with which to write data.

  /**
     Constructs an IFSFileOutputStream object.
    It is a default file output stream.
   **/
  public IFSFileOutputStream()
  {
    // Instantiate a file descriptor.
    fd_ = new IFSFileDescriptor(SHARE_ALL, this);
    initializeTransient();
  }


  /**
   Constructs an IFSFileOutputStream object.
   It creates a file output stream to write to the file with the specified name.
   Other readers and writers are allowed to access the file.  The file is
   replaced if it exists; otherwise, the file is created.
   @param system The AS/400 that contains the file.
   @param name The file to be opened for writing.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  public IFSFileOutputStream(AS400  system,
                             String name)
    throws AS400SecurityException, IOException
  {
    // Validate arguments.        @C2a
    if (system == null)
      throw new NullPointerException("system");
    if (name == null)
      throw new NullPointerException("name");

    myConstructor(system, name, SHARE_ALL, false, -1);   // @C2c
  }


  // @A2A
  /**
   Constructs an IFSFileOutputStream object.
   It creates a file output stream to write to the file with the specified name and ccsid.
   Other readers and writers are allowed to access the file.  The file is
   replaced if it exists; otherwise, the file is created.
   @param system The AS/400 that contains the file.
   @param name The file to be opened for writing.
   @param ccsid The target CCSID with which to write data.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  public IFSFileOutputStream(AS400  system,		// @C1A
                             String name,
                             int ccsid)
    throws AS400SecurityException, IOException
  {
    this(system, name, SHARE_ALL, false, ccsid);
  }


  /**
   Constructs an IFSFileOutputStream object.
   It creates a file output stream to write to the file with the specified name.
   @param system The AS/400 that contains the file.
   @param name The file to be opened for writing.
   @param shareOption Indicates how users can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>
   @param append Controls the behavior of the file.
   If true, output is appended to the file;
   otherwise, the current contents of the file are erased,
   and output replaces the file contents.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  // @A2A
  public IFSFileOutputStream(AS400   system,
                             String  name,
                             int     shareOption,
                             boolean append)
    throws AS400SecurityException, IOException
  {
    // Validate arguments.             @C2a
    if (system == null)
      throw new NullPointerException("system");
    if (name == null)
      throw new NullPointerException("name");
    IFSFileInputStream.validateShareOption(shareOption);

    myConstructor(system, name, shareOption, append, -1);   // @C2c
  }


  /**
   Constructs an IFSFileOutputStream object.
   It creates a file output stream to write to the file with the specified name and ccsid.
   @param system The AS/400 that contains the file.
   @param name The file to be opened for writing.
   @param shareOption Indicates how users can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>
   @param append Controls the behavior of the file.
   If true, output is appended to the file;
   otherwise, the current contents of the file are erased,
   and output replaces the file contents.
   @param ccsid The target CCSID with which to write data.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  
  public IFSFileOutputStream(AS400   system,       // @A2D, @C1B
                             String  name,
                             int     shareOption,
                             boolean append,
                             int ccsid)              // @A2A
    throws AS400SecurityException, IOException
  {
    // Validate arguments.
    if (system == null)
      throw new NullPointerException("system");
    if (name == null)
      throw new NullPointerException("name");
    IFSFileInputStream.validateShareOption(shareOption);
    if (ccsid < 0)   // @C2a
      throw new ExtendedIllegalArgumentException("ccsid",
                 ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

    myConstructor(system, name, shareOption, append, ccsid);  // @C2c
  }


  // @A5a
  /**
   Constructs an IFSFileOutputStream object.
   It creates a file output stream to write to the file specified by <i>file</i>.
   Other readers and writers are allowed to access the file.
   The file is replaced if it exists; otherwise, the file is created.
   @param file The file to be opened for writing.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the AS/400.
    **/
  public IFSFileOutputStream(IFSFile file)
    throws AS400SecurityException, IOException
  {
    this((file==null ? null : file.getSystem()), file, SHARE_ALL, false);
  }


  /**
   Constructs an IFSFileOutputStream object.
   It creates a file output stream to write to the file specified by <i>file</i>.
   @param system The AS/400 that contains the file.
   @param file The file to be opened for writing.
   @param shareOption Indicates how users can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>
   @param append Controls the behavior of the file.
   If true, output is appended to the file;
   otherwise, the current contents of the file are erased,
   and output replaces the file contents.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the AS/400.
    **/

  // @A2A
  public IFSFileOutputStream(AS400   system,
                             IFSFile file,
                             int     shareOption,
                             boolean append)
    throws AS400SecurityException, IOException
  {
    // Validate arguments.    @C2c
    if (file == null)                         // @A5c Swapped order of checks.
      throw new NullPointerException("file");
    else if (system == null)
      throw new NullPointerException("system");
    IFSFileInputStream.validateShareOption(shareOption);

    myConstructor(system, file.getAbsolutePath(), shareOption, append, -1);  // @C2c
  }


  /**
   Constructs an IFSFileOutputStream object.
   It creates a file output stream to write to the file specified by <i>file</i> using the
   ccsid specified by <i>ccsid</i>.
   @param system The AS/400 that contains the file.
   @param file The file to be opened for writing.
   @param shareOption Indicates how users can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>
   @param append Controls the behavior of the file.
   If true, output is appended to the file;
   otherwise, the current contents of the file are erased,
   and output replaces the file contents.
   @param ccsid The target CCSID with which to write data.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the AS/400.
    **/
  
  public IFSFileOutputStream(AS400   system,    // @A2D, @C1C
                             IFSFile file,
                             int     shareOption,
                             boolean append,
                             int ccsid)             // @A2A
    throws AS400SecurityException, IOException
  {
    // Validate arguments.
    if (system == null)
      throw new NullPointerException("system");
    if (file == null)
      throw new NullPointerException("file");
    IFSFileInputStream.validateShareOption(shareOption);
    if (ccsid < 0)   // @C2a
      throw new ExtendedIllegalArgumentException("ccsid",
                 ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

    myConstructor(system, file.getAbsolutePath(), shareOption, append, ccsid);
  }

  // common package scope constructor
  void myConstructor (AS400   system,
                      String  filePath,
                      int     shareOption,
                      boolean append,
                      int ccsid) // @A2A
    throws AS400SecurityException, IOException
  {
    // Assume the arguments have been been validated.
    append_ = append;
    ccsid_ = ccsid;
    initializeTransient();

    // Instantiate a file descriptor.
    fd_ = new IFSFileDescriptor(system, filePath, shareOption, this);

    // Connect to the AS400 byte stream server, and
    // open the file.
    connectAndOpen();
  }

  /**
   Creates a file output stream to write to file descriptor <i>fd</i>.
   @param fd The file descriptor to be opened for writing.
   **/
  public IFSFileOutputStream(IFSFileDescriptor fd)
  {
    // Validate arguments.
    if (fd == null)
      throw new NullPointerException("fd");
    IFSFileInputStream.validateShareOption(fd.getShareOption());

    initializeTransient();

    fd_ = fd;
  }


  // @A5a
  /**
   Constructs an IFSFileOutputStream object.
   It creates a file output stream to write to the file specified by <i>file</i>.
   Other readers and writers are allowed to access the file.
   The file is replaced if it exists; otherwise, the file is created.
   @param file The file to be opened for writing.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the AS/400.
    **/
  public IFSFileOutputStream(IFSJavaFile file)
    throws AS400SecurityException, IOException
  {
    this((file==null ? null : file.getSystem()), file, SHARE_ALL, false);
  }

  // @A3A
  /**
   Constructs an IFSFileOutputStream object.
   It creates a file output stream to write to the file specified by <i>file</i>.
   @param system The AS/400 that contains the file.
   @param file The file to be opened for writing.
   @param shareOption Indicates how users can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>
   @param append Controls the behavior of the file.
   If true, output is appended to the file;
   otherwise, the current contents of the file are erased,
   and output replaces the file contents.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the AS/400.
    **/
  public IFSFileOutputStream(AS400   system,
                             IFSJavaFile file,
                             int     shareOption,
                             boolean append)
    throws AS400SecurityException, IOException
  {
    // Validate arguments.
    if (file == null)                         // @A5c Swapped order of checks.
      throw new NullPointerException("file");
    else if (system == null)
      throw new NullPointerException("system");
    IFSFileInputStream.validateShareOption(shareOption);

    myConstructor(system, file.getAbsolutePath().replace (file.separatorChar, IFSFile.separatorChar), shareOption, append, -1);
  }


  // @A3A
  /**
   Constructs an IFSFileOutputStream object.
   It creates a file output stream to write to the file specified by <i>file</i> using the
   ccsid specified by <i>ccsid</i>.
   @param system The AS/400 that contains the file.
   @param file The file to be opened for writing.
   @param shareOption Indicates how users can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>
   @param append Controls the behavior of the file.
   If true, output is appended to the file;
   otherwise, the current contents of the file are erased,
   and output replaces the file contents.
   @param ccsid The target CCSID with which to write data.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the AS/400.
    **/
  public IFSFileOutputStream(AS400   system,
                             IFSJavaFile file,
                             int     shareOption,
                             boolean append,
                             int ccsid)
    throws AS400SecurityException, IOException
  {
    // Validate arguments.
    if (system == null)
      throw new NullPointerException("system");
    if (file == null)
      throw new NullPointerException("file");
    IFSFileInputStream.validateShareOption(shareOption);
    if (ccsid < 0)   // @C2a
      throw new ExtendedIllegalArgumentException("ccsid",
                 ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

    myConstructor(system, file.getAbsolutePath().replace (file.separatorChar, IFSFile.separatorChar), shareOption, append, ccsid);
  }


  /**
   Adds a file listener to receive file events from this IFSFileOutputStream.
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

  /**
   Chooses the appropriate implementation.
   Note: This method is available for use by subclasses.
   **/
  private void chooseImpl ()
  {
    if (impl_ == null)
    {
      // Ensure that the system has been set.
      AS400 system = fd_.getSystem();
      if (system == null)
      {
        throw new ExtendedIllegalStateException("system",
                              ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }

      impl_ = (IFSFileOutputStreamImpl) system.loadImpl2
        ("com.ibm.as400.access.IFSFileOutputStreamImplRemote",
         "com.ibm.as400.access.IFSFileOutputStreamImplProxy");
      impl_.setFD(fd_.getImpl());
      impl_.setAppend(append_);
    }
  }

  /**
   Closes this file output stream and releases any system resources associated
   with this stream.

   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  public void close()
    throws IOException
  {
    if (fd_.isOpen())
    {
      if (impl_ != null)
        impl_.close();
      else
        fd_.close();

      // Fire the file close event.
      if (fileListeners_.size() != 0)
      {
        IFSFileDescriptor.fireClosedEvents(this, fileListeners_);
      }
    }
  }

  /**
   Establishes communications with the AS400, and opens the file.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  private void connectAndOpen()
    throws AS400SecurityException, IOException
  {
    if (impl_ == null)
    {
      chooseImpl();
      fd_.getSystem().connectService(AS400.FILE);
    }

    impl_.connectAndOpen(ccsid_);

    // Fire the file opened event.
    if (fileListeners_.size() != 0)
      IFSFileDescriptor.fireOpenedEvents(this, fileListeners_);
  }

  /**
   Ensures that the file output stream is closed when there are no more
   references to it.
   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  protected void finalize()
    throws IOException
  {
    if (fd_ != null && fd_.isOpen())
    {
      // Close the file.  Send a close request to the server.
      if (impl_ != null)
        impl_.close();
      else
        fd_.close();
    }

    try
    {
      super.finalize();
    }
    catch(Throwable e)
    {
      throw new IOException(e.toString());
    }
  }

  /**
   Forces any buffered output bytes to be written.

   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  public void flush()
    throws IOException
  {
    // Ensure that the file is open.
    open(ccsid_);

    impl_.flush();
  }

  /**
   Returns the CCSID.
   @return The CCSID.
   **/
  // Note: This method is provided for use by IFSTextFileOutputStream.
  int getCCSID()
  {
    return ccsid_;
  }


  /**
   Returns the file descriptor associated with this stream.
   @return The file descriptor associated with this stream.
   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  public final IFSFileDescriptor getFD()
    throws IOException
  {
    return fd_;
  }


  /**
   Returns the implementation object.
   @return The implementation object associated with this stream.
   **/
  IFSFileOutputStreamImpl getImpl()
  {
    return impl_;  // Note: This may be null.
  }

  /**
   Returns the integrated file system path name of the object represented by
   this IFSFileOutputStream object.
   @return The absolute path name of the object.
   **/
  public String getPath()
  {
    return fd_.getPath();
  }


  /**
   Returns the share option for this object.
   @return The share option.
   **/
  public int getShareOption()
  {
    return fd_.getShareOption();
  }


  /**
   Returns the AS400 system object for this stream.
   @return The AS400 system object.
   **/
  public AS400 getSystem()
  {
    return fd_.getSystem();
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
   Places a lock on the file at the current position for the specified number
   of bytes.
   @param length The number of bytes to lock.
   @return A key for undoing this lock.

   @exception IOException If an error occurs while communicating with the AS/400.

   @see IFSKey
   @see #unlock
   **/
  public IFSKey lock(int length)
    throws IOException
  {
    // Validate the argument.
    if (length <= 0)
    {
      throw new ExtendedIllegalArgumentException("length (" +
                                                 Integer.toString(length) +
                                                 ")",
                  ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Ensure that the file is open.
    open(ccsid_);

    return impl_.lock(length);
  }


  /**
   Opens the specified file.

   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  protected void open(int fileDataCCSID)
    throws IOException
  {
    if (impl_ == null)
    {
      chooseImpl();
      try { fd_.getSystem().connectService(AS400.FILE); }
      catch(AS400SecurityException e)
      {
        Trace.log(Trace.ERROR, "Access to byte stream server on '" +
                  fd_.getSystem().getSystemName() + "' denied.", e);
        throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
      }
    }

    // If the file is already open, do nothing.
    if (fd_.isOpen())
    {
      return;
    }
    else
    {
      impl_.open(fileDataCCSID);

      // Fire the file opened event.
      if (fileListeners_.size() != 0)
        IFSFileDescriptor.fireOpenedEvents(this, fileListeners_);
    }
  }

  /**
   Restores the state of this object from an object input stream.
   @param ois The stream of state information.
   @exception IOException If an error occurs while communicating with the AS/400.
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
   this IFSFileOutputStream.
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

  /**
   Sets the append option.
   @param append If true, data is appended to an existing file;
   otherwise, output replaces the file contents.
   @exception PropertyVetoException If the change is vetoed.
   **/
  public void setAppend(boolean append)
    throws PropertyVetoException
  {
    // Ensure that append is not changed after a connection is established.
    if (fd_.valid())
    {
      throw new ExtendedIllegalStateException("append",
                         ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    Boolean oldAppend = new Boolean(append_);
    Boolean newAppend = new Boolean(append);

    // Fire a vetoable change event for append.
    vetos_.fireVetoableChange("append", oldAppend, newAppend);

    append_ = append;

    // Fire the property change event.
    changes_.firePropertyChange("append", oldAppend, newAppend);
  }


  /**
   Sets the CCSID for the data written to the file.
   @param ccsid The target CCSID.
   @exception PropertyVetoException If the change is vetoed.
   **/
  // Note: This method is provided for use by IFSTextFileOutputStream.
  void setCCSID(int ccsid)
    throws PropertyVetoException
  {
    // Validate the arguments.
    if (ccsid < 0)
    {
      throw new ExtendedIllegalArgumentException("ccsid",
                      ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // @A1A
    // Ensure that the CCSID isn't changed after the file is opened.
    if (fd_.isOpen())
    {
      throw new ExtendedIllegalStateException("CCSID",
                               ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }
    // End of @A1A

    Integer oldCCSID = new Integer(ccsid_);
    Integer newCCSID = new Integer(ccsid);

    // Fire a vetoable change event for shareOption.
    vetos_.fireVetoableChange("CCSID", oldCCSID, newCCSID);

    ccsid_ = ccsid;

    // Fire the property change event.
    changes_.firePropertyChange("CCSID", oldCCSID, newCCSID);
  }

  /**
   Sets the file descriptor.
   @param fd The file descriptor.

   @exception PropertyVetoException If the change is vetoed.

   **/
  public void setFD(IFSFileDescriptor fd)
    throws PropertyVetoException
  {
    if (fd == null)
    {
      throw new NullPointerException("fd");
    }

    // Ensure that FD is not changed after a connection is established.
    if (fd_.valid())
    {
      throw new ExtendedIllegalStateException("FD",
                         ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    // Fire a vetoable change event for FD.
    vetos_.fireVetoableChange("FD", fd_, fd);

    // Remember the current FD value.
    IFSFileDescriptor oldFD = fd_;

    fd_ = fd;

    // Fire the property change event.
    changes_.firePropertyChange("FD", oldFD, fd_);
  }

  /**
   Sets the integrated file system path name.
   @param path The absolute integrated file system path name.
   @exception PropertyVetoException If the change is vetoed.
   **/
  public void setPath(String path)
    throws PropertyVetoException
  {
    if (path == null)
    {
      throw new NullPointerException("path");
    }

    // Ensure that the path is not altered after a connection is
    // established.
    if (fd_.valid())
    {
      throw new ExtendedIllegalStateException("path",
                          ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    // Remember the current path value.
    String oldPath = fd_.getPath();

    String newPath;
    if (path.length() == 0 || path.charAt(0) != IFSFile.separatorChar)
    {
      newPath = IFSFile.separator + path;
    }
    else
    {
      newPath = path;
    }

    // Fire a vetoable change event for the path.
    vetos_.fireVetoableChange("path", oldPath, newPath);

    // Update the path value.
    fd_.setPath(newPath);

    // Fire the property change event.
    changes_.firePropertyChange("path", oldPath, newPath);
  }


  /**
   Sets the share option.
   @param shareOption Indicates how users can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>
  @exception PropertyVetoException If the change is vetoed.
   **/
  public void setShareOption(int shareOption)
    throws PropertyVetoException
  {
    IFSFileInputStream.validateShareOption(shareOption);

    // Ensure that shareOption is not altered after a connection
    // is established.
    if (fd_.valid())
    {
      throw new ExtendedIllegalStateException("shareOption",
                          ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    Integer oldShareOption = new Integer(fd_.getShareOption());
    Integer newShareOption = new Integer(shareOption);

    // Fire a vetoable change event for shareOption.
    vetos_.fireVetoableChange("shareOption", oldShareOption,
                              newShareOption);

    fd_.setShareOption(shareOption);

    // Fire the property change event.
    changes_.firePropertyChange("shareOption", oldShareOption,
                                newShareOption);
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
    if (fd_.valid())
    {
      Trace.log(Trace.ERROR, "Cannot set property 'system' after connect.");
      throw new ExtendedIllegalStateException("system",
                          ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    // Remember the old system value.
    AS400 oldSystem = fd_.getSystem();

    // Fire a vetoable change event for system.
    vetos_.fireVetoableChange("system", oldSystem, system);

    fd_.setSystem(system);

    // Fire the property change event.
    changes_.firePropertyChange("system", oldSystem, system);
  }



  /**
   Undoes a lock on this file.
   @param key The key for the lock.

   @exception IOException If an error occurs while communicating with the AS/400.

   @see IFSKey
   @see #lock
   **/
  public void unlock(IFSKey key)
    throws IOException
  {
    // Validate the argument.
    if (key == null)
      throw new NullPointerException("key");

    // Ensure that the file is open.
    open(ccsid_);

    impl_.unlock(key);
  }


  /**
   Writes the specified byte to this file output stream.
   @param b The byte to be written.

   @exception IOException If an error occurs while communicating with the AS/400.

   **/
  public void write(int b)
    throws IOException
  {
    byte[] data = new byte[1];
    data[0] = (byte) b;
    write(data, 0, 1);
  }


  /**
   Writes <i>data.length</i> bytes of data from the byte array <i>data</i>
   to this file output stream.
   @param data The data to be written.

   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  public void write(byte[] data)
    throws IOException
  {
    // Validate arguments.
    if (data == null)
      throw new NullPointerException("data");

    write(data, 0, data.length);
  }

  /**
   Writes <i>length</i> bytes of data from the byte array <i>data</i>, starting
   at <i>offset</i>, to this file output stream.
   @param data The data to be written.
   @param offset The start offset in the data.
   @param length The number of bytes to write.

   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  public void write(byte[] data,
                    int    dataOffset,
                    int    length)
    throws IOException
  {
    // Validate arguments.
    if (data == null)
      throw new NullPointerException("data");
    if (dataOffset < 0)
    {
      throw new ExtendedIllegalArgumentException("dataOffset",
                      ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (length < 0)
    {
      throw new ExtendedIllegalArgumentException("length",
                      ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Ensure that the file is open.
    open(ccsid_);

    impl_.write(data, dataOffset, length);

    // Fire the file modified event.
    if (fileListeners_.size() != 0)
    {
      IFSFileDescriptor.fireModifiedEvents(this, fileListeners_);
    }
  }

}
