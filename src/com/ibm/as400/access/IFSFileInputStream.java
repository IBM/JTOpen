///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileInputStream.java
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
import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;


/**
 The IFSFileInputStream class represents an integrated file system
input stream.
 An integrated file system file input stream is an input stream for reading
 data from integrated file system objects.<br>IFSFileInputStream objects are
 capable of generating file events that call the following FileListener
 methods: fileClosed and fileOpened.<br>
 The following example illustrates the use of IFSFileInputStream:
 <pre>
 // Work with /Dir/File on the system eniac.
 AS400 as400 = new AS400("eniac");
 IFSFileInputStream file = new IFSFileInputStream(as400, "/Dir/File");<br>
 // Determine how many bytes are available on the stream.
 int available = file.available();<br>
 // Lock the first 8 bytes of the file.
 IFSKey key = file.lock(8);<br>
 // Read the first 8 bytes.
 byte[] data = new byte[8];
 int bytesRead = file.read(data, 0, 8);<br>
 // Unlock the first 8 bytes of the file.
 file.unlock(key);<br>
 // Close the file.
 file.close();
 </pre>
@see com.ibm.as400.access.FileEvent
@see #addFileListener
@see #removeFileListener
 **/
public class IFSFileInputStream extends InputStream
  implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";



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

  protected IFSFileDescriptor fd_; // file info - never allowed to be null

  

  transient private PropertyChangeSupport changes_ ;
  transient private Vector fileListeners_;
  transient private VetoableChangeSupport vetos_;
  transient IFSFileInputStreamImpl impl_;
             // Design note: impl_ is available for use by subclasses.

  /**
    Constructs an IFSFileInputStream.
    It creates a default file input stream.
   **/
  public IFSFileInputStream()
  {
    // Instantiate a file descriptor.
    fd_ = new IFSFileDescriptor(SHARE_ALL, this);
    initializeTransient();
  }

  /**
   Constructs an IFSFileInputStream. 
   It creates a file input stream to read from the file <i>name</i>.
   Other readers and writers are allowed to access the file.
   The file is opened if it exists; otherwise, an exception is thrown.
   @param system The AS400 that contains the file.
   @param name The integrated file system name.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the server.
   **/
  public IFSFileInputStream(AS400  system,
                            String name)
    throws AS400SecurityException, IOException
  {
    this(system, name, SHARE_ALL);
  }

  /**
   Constructs an IFSFileInputStream. 
   It creates a file input stream to read from the file <i>name</i>.
   @param system The server that contains the file.
   @param name The integrated file system name.
   @param shareOption Indicates how users can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the server.
   **/
  public IFSFileInputStream(AS400  system,
                            String name,
                            int    shareOption)
    throws AS400SecurityException, IOException
  {
    // Validate arguments.
    if (system == null)
      throw new NullPointerException("system");
    else if (name == null)
      throw new NullPointerException("name");
    validateShareOption(shareOption);
    initializeTransient();

    // Instantiate a file descriptor.
    fd_ = new IFSFileDescriptor(system, name, shareOption, this);

    // Connect to the byte stream server, and
    // open the file.
    connectAndOpen();
  }


  // @A5a
  /**
   Creates a file input stream to read from the file specified by <i>file</i>.
   Other readers and writers are allowed to access the file.
   The file is opened if it exists; otherwise, an exception is thrown.
   @param file The file to be opened for reading.
 
   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the server.
    **/
  public IFSFileInputStream(IFSFile file)
    throws AS400SecurityException, IOException
  {
    this((file==null ? null : file.getSystem()), file, SHARE_ALL);
  }


  /**
   Creates a file input stream to read from the file specified by <i>file</i>.
   @param system The server that contains the file.
   @param file The file to be opened for reading.
   @param shareOption Indicates how users can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>
 
   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the server.
   @deprecated Use IFSFileInputStream(IFSFile, int) instead.
    **/
  public IFSFileInputStream(AS400   system,
                            IFSFile file,
                            int     shareOption)
    throws AS400SecurityException, IOException
  {
    // Validate arguments.
    if (file == null)                         // @A5c Swapped order of checks.
      throw new NullPointerException("file");
    else if (system == null)
      throw new NullPointerException("system");
    validateShareOption(shareOption);
    initializeTransient();

    // Instantiate a file descriptor.
    fd_ = new IFSFileDescriptor(system, file.getAbsolutePath(), shareOption, this);

    // Connect to the byte stream server, and
    // open the file.
    connectAndOpen();
  }


  /**
   Creates a file input stream to read from the file specified by <i>file</i>.
   @param file The file to be opened for reading.
   @param shareOption Indicates how users can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>
 
   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the server.
    **/
  public IFSFileInputStream(IFSFile file,
                            int     shareOption)
    throws AS400SecurityException, IOException
  {
    this((file==null ? null : file.getSystem()), file, shareOption);
  }


  /**
   Creates a file input stream to read from file descriptor <i>fd</i>.
   @param fd The file descriptor to be opened for reading.
   **/
  public IFSFileInputStream(IFSFileDescriptor fd)
  {
    // Validate arguments.
    if (fd == null)
      throw new NullPointerException("fd");
    validateShareOption(fd.getShareOption());

    initializeTransient();

    fd_ = fd;

    // Design note: The original implementation did not do a connect/open.  -jlee 1/27/99
  }


  // @A5a
  /**
   Creates a file input stream to read from the file specified by <i>file</i>.
   Other readers and writers are allowed to access the file.
   @param file The file to be opened for reading.
 
   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the server.
    **/
  public IFSFileInputStream(IFSJavaFile file)
    throws AS400SecurityException, IOException
  {
    this((file==null ? null : file.getSystem()), file, SHARE_ALL);
  }

  // @A1A Added IFSJavaFile support
  /**
   Creates a file input stream to read from the file specified by <i>file</i>.
   @param system The server that contains the file.
   @param file The file to be opened for reading.
   @param shareOption Indicates how users can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>
 
   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the server.
   @deprecated Use IFSFileInputStream(IFSJavaFile, int) instead.
    **/
  public IFSFileInputStream(AS400   system,
                            IFSJavaFile file,
                            int     shareOption)
    throws AS400SecurityException, IOException
  {
    // Validate arguments.
    if (file == null)                         // @A5c Swapped order of checks.
      throw new NullPointerException("file");
    else if (system == null)
      throw new NullPointerException("system");
    validateShareOption(shareOption);
    initializeTransient();

    // Instantiate a file descriptor.
    fd_ = new IFSFileDescriptor(system, file.getAbsolutePath().replace (file.separatorChar, IFSFile.separatorChar), shareOption, this);

    // Connect to the byte stream server, and open the file.
    connectAndOpen();
  }
  //@A1A End of IFSJavaFile support.


  /**
   Creates a file input stream to read from the file specified by <i>file</i>.
   @param file The file to be opened for reading.
   @param shareOption Indicates how users can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>
 
   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the server.
    **/
  public IFSFileInputStream(IFSJavaFile file,
                            int     shareOption)
    throws AS400SecurityException, IOException
  {
    this((file==null ? null : file.getSystem()), file, shareOption);
  }


  /**
   Adds a file listener to receive file events from this IFSFileInputStream.
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
   Returns the number of bytes that can be read from this file input stream.
   @return The number of bytes that can be read from this file input stream.

   @exception IOException If an error occurs while communicating with the server.
  **/
  public int available()
    throws IOException
  {
    // Ensure that the file is open.
    open();

    return impl_.available();
  }

  /**
   Chooses the appropriate implementation.
   This method is available for use by subclasses.
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

      impl_ = (IFSFileInputStreamImpl) system.loadImpl2
        ("com.ibm.as400.access.IFSFileInputStreamImplRemote",
         "com.ibm.as400.access.IFSFileInputStreamImplProxy");
      impl_.setFD(fd_.getImpl());
    }
  }

  /**
   Closes this file input stream and releases any system resources associated
   with the stream.

   @exception IOException If an error occurs while communicating with the server.
  **/
  public void close()
    throws IOException
  {
    if (!fd_.isClosed() && fd_.isOpen())
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
   @exception IOException If an error occurs while communicating with the server.
  **/
  private void connectAndOpen()
    throws AS400SecurityException, IOException
  {
    if (impl_ == null)
    {
      chooseImpl();
      fd_.getSystem().connectService(AS400.FILE);
    }

    impl_.connectAndOpen();

    // Fire the file opened event.
    if (fileListeners_.size() != 0)
      IFSFileDescriptor.fireOpenedEvents(this, fileListeners_);
  }


  /**
   Ensures that the file input stream is closed when there are no more
   references to it.
   @exception IOException If an error occurs while communicating with the server.
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
      Trace.log(Trace.ERROR, "Error during finalization.");
      throw new IOException(e.toString());
    }
}


  /**
   Returns the opaque file descriptor associated with this stream.
   @return The file descriptor object associated with this stream.
   @exception IOException If an error occurs while communicating with the server.
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
  IFSFileInputStreamImpl getImpl()
  {
    return impl_;  // Note: This may be null.
  }


  /**
   Returns the integrated file system path name of the object represented by this IFSFileInputStream object.
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
   Returns the AS400 system object for this file input stream.
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
     fileListeners_ = new Vector();
     vetos_ = new VetoableChangeSupport(this);
     impl_ = null;
   }


  /**
   Places a lock on the file at the current position for the specified
   number of bytes.
   @param length The number of bytes to lock.
   @return The key for undoing this lock.
 
   @exception IOException If an error occurs while communicating with the server.

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
    open();

    return impl_.lock(length);
  }


  /**
   Opens the specified file.
   @exception IOException If an error occurs while communicating with the server.
   **/
  protected void open()
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
      impl_.open();

      // Fire the file opened event.
      if (fileListeners_.size() != 0)
        IFSFileDescriptor.fireOpenedEvents(this, fileListeners_);
    }
  }

  /**
   Restores the state of this object from an object input stream.
   @param ois The stream of state information.
   @exception IOException If an error occurs while communicating with the server.
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
   Reads the next byte of data from this input stream.
   @return The next byte of data, or -1 if the end of file is reached.

   @exception IOException If an error occurs while communicating with the server.
   **/
  public int read()
    throws IOException
  {
    byte[] data = new byte[1];
    int rc = read(data, 0, 1);
    int value;

    if (rc == 1)
    {
      value = (int) data[0];
      value = value & 0xff;
    }
    else
    {
      value = -1;
    }

    return value;
  }

  /**
   Reads up to <i>data.length</i> bytes of data from this input stream into
   <i>data</i>.
   @param data The buffer into which data is read.
   @return The total number of bytes read into the buffer, or -1 if there is no more data because the end of file has been reached.

   @exception IOException If an error occurs while communicating with the server.
   **/
  public int read(byte[] data)
    throws IOException
  {
    // Validate arguments.
    if (data == null)
      throw new NullPointerException("data");

    return read(data, 0, data.length);
  }

  /**
   Reads up to <i>length</i> bytes of data from this input stream into <i>data</i>, starting at the array offset <i>dataOffset</i>.
   @param data The buffer into which the data is read.
   @param dataOffset The start offset of the data in the buffer.
   @param length The maximum number of bytes to read
   @return The total number of bytes read into the buffer, or -1 if there is no more data because the end of file has been reached.

   @exception IOException If an error occurs while communicating with the server.
   **/
  public int read(byte[] data,
                  int    dataOffset,
                  int    length)
    throws IOException
  {
    // Validate arguments.
    if (data == null)
      throw new NullPointerException("data");
    if (dataOffset < 0)
      throw new ExtendedIllegalArgumentException("dataOffset",
                     ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    if (length < 0)
      throw new ExtendedIllegalArgumentException("length",
                     ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

    // If length is zero then return zero.
    if (length == 0)
    {
      return 0;
    }

    // Ensure that the file is open.
    open();

    return impl_.read(data, dataOffset, length);
  }

  /**
   Removes a file listener so that it no longer receives file events from
   this IFSFileInputStream.
   @param listener The file listener .
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
   Rewinds the input stream.  After this call, the stream will read from the beginning.
   **/
  public void reset() throws IOException
  {
    if (fd_.isClosed()) {
      Trace.log(Trace.ERROR, "The stream has been closed.");
      throw new java.nio.channels.ClosedChannelException();
    }
    rewind();
  }

  //@A2A Added rewind.
  /**
   Rewinds the input stream.  After this call, the stream will read from the beginning.
   @deprecated Use reset() instead.
   **/
  public void rewind()
  {
    if (fd_.isOpen())
    {
      fd_.setFileOffset(0);
    }
  }
  //@A2A End of rewind

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

    // Ensure that FD is not altered after the connection is established.
    if (fd_.valid())
    {
      throw new ExtendedIllegalStateException("FD",
                          ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    // Remember the current FD value.
    IFSFileDescriptor oldFD = fd_;

    // Fire a vetoable change event for FD.
    vetos_.fireVetoableChange("FD", oldFD, fd);

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

    // Ensure that the path is not altered after the connection is
    // established.
    if (fd_.valid())
    {
      throw new ExtendedIllegalStateException("path",
                            ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    String newPath;
    if (path.length() == 0 || path.charAt(0) != IFSFile.separatorChar)
    {
      newPath = IFSFile.separator + path;
    }
    else
    {
      newPath = path;
    }

    // Remember the current path value.
    String oldPath = fd_.getPath();

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
    validateShareOption(shareOption);

    // Ensure that shareOption is not altered after the connection
    // is established.
    if (fd_.valid())
    {
      throw new ExtendedIllegalStateException("shareOption",
                        ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    Integer oldShareOption = null;
    Integer newShareOption = null;

    oldShareOption = new Integer(fd_.getShareOption());
    newShareOption = new Integer(shareOption);

    // Fire a vetoable change event for shareOption.
    vetos_.fireVetoableChange("shareOption", oldShareOption,
                              newShareOption);

    fd_.setShareOption(shareOption);

    // Fire the property change event.
    changes_.firePropertyChange("shareOption", oldShareOption,
                                  newShareOption);
  }


  /**
   Validates a share option value.
   @param shareOption Indicates how users can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>
   **/
  static final void validateShareOption(int shareOption)
  {
    if ((shareOption < SHARE_NONE) || (shareOption > SHARE_ALL))
    {
      throw new ExtendedIllegalArgumentException("shareOption (" +
                    Integer.toString(shareOption) + ")",
                    ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
  }


  /**
   Sets the system.
   The system cannot be changed once a connection is made to the server.
   @param system The system object.
   @exception PropertyVetoException If the change is vetoed.
   **/
  public void setSystem(AS400 system)
    throws PropertyVetoException
  {
    if (system == null)
      throw new NullPointerException("system");

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
   Skips over the next <i>bytesToSkip</i> bytes in the file input stream.
   This method may skip less bytes than specified if the end of file is
   reached.  The actual number of bytes skipped is returned.

   @param bytesToSkip The number of bytes to skip.
   @return The actual number of bytes skipped.

   @exception IOException If an error occurs while communicating with the server.
   **/
  public long skip(long bytesToSkip)
    throws IOException
  {
    // Validate argument.
    if (bytesToSkip < 0)
    {
      throw new ExtendedIllegalArgumentException("bytesToSkip (" +
                         Long.toString(bytesToSkip) + ")",
                      ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Ensure that the file is open.
    open();

    return impl_.skip(bytesToSkip);
  }

  /**
   Undoes a lock on this file.
   @param key The key for the lock.

   @exception IOException If an error occurs while communicating with the server.
 
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
    open();

    impl_.unlock(key);
  }
}
