///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSRandomAccessFile.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
// @A1 2008-02-22 Change seek() method to remove (int) cast when calling
//     setFileOffset() which accepts a long parameter.
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.io.EOFException;
import java.io.IOException;
import java.util.Vector;

/**
 The IFSRandomAccessFile class supports read and write access to integrated file system objects.  The position at which the next access occurs can be modified.  This class offers methods that allow specified mode access of read-only, write-only, or read-write.<br>IFSRandomAccessFile objects are capable of generating file events that call the following FileListener methods: fileClosed, fileModified, and fileOpened.<br>
 The following example illustrates the use of IFSRandomAccessFile:
 <pre>
 // Work with /Dir/File on the system eniac.
 AS400 as400 = new AS400("eniac");
 IFSRandomAccessFile file = new IFSRandomAccessFile(as400, "/Dir/File", "rw");
 // Determine the file length.
 long length = file.length();
 // Lock the first 11 bytes.
 IFSKey key = file.lock(0, 11);
 // Write a string to the file.
 file.writeChars("Hello world");
 // Read the string we just wrote.
 file.seek(0);
 String s = file.readLine();
 // Close the file.
 file.close();
 </pre>
 @see com.ibm.as400.access.FileEvent
 @see #addFileListener
 @see #removeFileListener
 **/
public class IFSRandomAccessFile
implements java.io.DataInput, java.io.DataOutput, java.io.Serializable
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

  /**
   File existence option that indicates that the request is either to fail if the file exists or is to  create the file if it does not exist.
   **/
  public final static int FAIL_OR_CREATE = 2;
  /**
   File existence option that indicates that the file is either to be opened if it exists or is to be created if it does not exist.
   **/
  public final static int OPEN_OR_CREATE = 0;
  /**
   File existence option that indicates that the file is either to be opened if it exists or that the request is to fail if the file does not exist.
   **/
  public final static int OPEN_OR_FAIL = 3;
  /**
   File existence option that indicates that the file is either to be replaced if it exists or is to be created if it does not exist.
   **/
  public final static int REPLACE_OR_CREATE = 1;
  /**
   File existence option that indicates that the file is either to be replaced if it exists or that the request is to fail if the file does not exist.
   **/
  public final static int REPLACE_OR_FAIL = 4;


  

  transient private PropertyChangeSupport changes_;
  transient private VetoableChangeSupport vetos_;
  private int existenceOption_;
  private IFSFileDescriptor fd_; // file info - never allowed to be null
  transient private Vector fileListeners_;
  private String mode_ = "";
  private boolean forceToStorage_ = false;
  transient private byte[] bytes1;
  transient private byte[] bytes2;
  transient private byte[] bytes4;
  transient private byte[] bytes8;
  transient private IFSRandomAccessFileImpl impl_;


  /**
   Constructs an IFSRandomAccessFile object. It is a default random access file.
   Other readers and writers are allowed to access the file.
   **/
  public IFSRandomAccessFile()
  {
    // Instantiate a file descriptor.
    fd_ = new IFSFileDescriptor(SHARE_ALL, this);
    initializeTransient();
  }

  /**
   Constructs an IFSRandomAccessFile object. It uses the specified system name, file
   name, and mode.
   If the mode is <i>r</i>, the file is opened if it
   exists; otherwise, an IOException is thrown.  If the mode is <i>rw</i> or
   <i>w</i>, the file is opened if it exists; otherwise, the file is created.
   Other readers and writers are allowed to access the file.
   @param system The AS400 that contains the file.
   @param name The file name.
   @param mode The access mode <ul><li>"r" read only<li>"w" write only<li>"rw" read/write</ul>
   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the system.
   **/
  public IFSRandomAccessFile(AS400  system,
                             String name,
                             String mode)
    throws AS400SecurityException, IOException
  {
    // Validate arguments.
    if (system == null)
      throw new NullPointerException("system");
    if (name == null)
      throw new NullPointerException("name");

    initializeTransient();

    // Set the mode and existence option.
    validateMode(mode);
    mode_ = mode;
    existenceOption_ = (mode.equals("r") ? OPEN_OR_FAIL : OPEN_OR_CREATE);

    // Instantiate a file descriptor.
    fd_ = new IFSFileDescriptor(system, name, SHARE_ALL, this);

    // Connect to the AS400 byte stream server, and
    // open the file.
    connectAndOpen();
  }


  /**
    Constructs an IFSRandomAccessFile object.
   It uses the specified system name, file
   name, mode, share option, and existence option.
   @param system The AS400 that contains the file.
   @param name The file name.
   @param mode The acess mode <ul><li>"r" read only<li>"w" write only<li>"rw" read/write</ul>
   @param shareOption Indicates how users can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>
   @param existenceOption Indicates if the file should be created, opened or
   if the request should fail based on the existence of the file.
   <ul><li>FAIL_OR_CREATE Fail if exists; create if not<li>OPEN_OR_CREATE
   Open if exists; create if not<li>OPEN_OR_FAIL Open if exists; fail if
   not<li>REPLACE_OR_CREATE Replace if exists; create if not<li>REPLACE_OR_FAIL
   Replace if exists; fail if not</ul>

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the system.
   **/
  public IFSRandomAccessFile(AS400  system,
                             String name,
                             String mode,
                             int    shareOption,
                             int    existenceOption)
    throws AS400SecurityException, IOException
  {
    // Validate arguments.
    if (system == null)
      throw new NullPointerException("system");
    if (name == null)
      throw new NullPointerException("name");

    initializeTransient();

    // Set the mode, share option, and existence option.
    validateMode(mode);
    mode_ = mode;

    validateShareOption(shareOption);

    validateExistenceOption(existenceOption);
    existenceOption_ = existenceOption;

    // Instantiate a file descriptor.
    fd_ = new IFSFileDescriptor(system, name, shareOption, this);

    // Connect to the AS400 byte stream server, and
    // open the file.
    connectAndOpen();
  }


  /**
    Constructs an IFSRandomAccessFile object.
   It uses the specified system name, file
   name, mode, share option, and existence option.

   @param system The AS400 that contains the file.
   @param file The file to access.
   @param mode The access mode <ul><li>"r" read only<li>"w" write only<li>"rw" read/write</ul>
   @param shareOption Indicates how other user's can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>
   @param existenceOption Indicates if the file should be created, opened or if the request should fail based on the existence of the file. <ul><li>FAIL_OR_CREATE Fail if exists; create if not<li>OPEN_OR_CREATE Open if exists; create if not<li>OPEN_OR_FAIL Open if exists; fail if not<li>REPLACE_OR_CREATE Replace if exists; create if not<li>REPLACE_OR_FAIL Replace if exists; fail if not</ul>

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the system.
   @deprecated Use IFSRandomAccessFile(IFSFile,String,int,int) instead.
   **/
  public IFSRandomAccessFile(AS400   system,
                             IFSFile file,
                             String  mode,
                             int     shareOption,
                             int     existenceOption)
    throws AS400SecurityException, IOException
  {
    // Validate arguments.
    if (system == null)
      throw new NullPointerException("system");
    if (file == null)
      throw new NullPointerException("file");

    initializeTransient();

    // Set the mode, share option, and existence option.
    validateMode(mode);
    mode_ = mode;

    validateShareOption(shareOption);

    validateExistenceOption(existenceOption);
    existenceOption_ = existenceOption;

    // Instantiate a file descriptor.
    fd_ = new IFSFileDescriptor(system, file.getAbsolutePath(), shareOption, this);

    // Connect to the AS400 byte stream server, and
    // open the file.
    connectAndOpen();
  }


  /**
    Constructs an IFSRandomAccessFile object.
   It uses the specified file, mode, share option, and existence option.

   @param file The file to access.
   @param mode The access mode <ul><li>"r" read only<li>"w" write only<li>"rw" read/write</ul>
   @param shareOption Indicates how other user's can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>
   @param existenceOption Indicates if the file should be created, opened or if the request should fail based on the existence of the file. <ul><li>FAIL_OR_CREATE Fail if exists; create if not<li>OPEN_OR_CREATE Open if exists; create if not<li>OPEN_OR_FAIL Open if exists; fail if not<li>REPLACE_OR_CREATE Replace if exists; create if not<li>REPLACE_OR_FAIL Replace if exists; fail if not</ul>

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the system.
   **/
  public IFSRandomAccessFile(IFSFile file,
                             String  mode,
                             int     shareOption,
                             int     existenceOption)
    throws AS400SecurityException, IOException
  {
    // Validate arguments.
    if (file == null)
      throw new NullPointerException("file");

    initializeTransient();

    // Set the mode, share option, and existence option.
    validateMode(mode);
    mode_ = mode;

    validateShareOption(shareOption);

    validateExistenceOption(existenceOption);
    existenceOption_ = existenceOption;

    // Instantiate a file descriptor.
    fd_ = new IFSFileDescriptor(file.getSystem(), file.getAbsolutePath(), shareOption, this);

    // Connect to the AS400 byte stream server, and
    // open the file.
    connectAndOpen();
  }


  /**
   Adds a file listener to receive file events from this IFSRandomAccessFile.
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
   **/
  void chooseImpl ()
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

      impl_ = (IFSRandomAccessFileImpl) system.loadImpl2
        ("com.ibm.as400.access.IFSRandomAccessFileImplRemote",
         "com.ibm.as400.access.IFSRandomAccessFileImplProxy");
      impl_.setFD(fd_.getImpl());
      impl_.setExistenceOption(existenceOption_);
      impl_.setForceToStorage(forceToStorage_);
      impl_.setMode(mode_);
    }
  }

  /**
   Closes this random access file stream and releases any system resources
   associated with the stream.
   @exception IOException If an error occurs while communicating with the system.
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
   @exception IOException If an error occurs while communicating with the system.
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
   Ensures that the stream is closed when there are no more references to it.
   @exception IOException If an error occurs while communicating with the system.
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

   @exception IOException If an error occurs while communicating with the system.
   **/
  public void flush()
    throws IOException
  {
    // Ensure that the file is open.
    open();

    impl_.flush();
  }

  /**
   Returns the existence option for this object.
   @return The existence option.
   **/
  public int getExistenceOption()
  {
    return existenceOption_;
  }


  /**
   Returns a file descriptor associated with this stream.
   @return The file descriptor associated with this stream
   @exception IOException If an error occurs while communicating with the system.
   **/
  public final IFSFileDescriptor getFD()
    throws IOException
  {
    return fd_;
  }


  /**
   Returns the current offset in this file.
   @return The offset from the beginning of the file, in bytes, at which the
   next read or write occurs.
   @exception IOException If an error occurs while communicating with the system.
   **/
  public long getFilePointer()
    throws IOException
  {
    // Ensure that the file is open.
    open();

    return (long) fd_.getFileOffset();
  }


  /**
   Returns the implementation object.
   @return The implementation object associated with this object.
   **/
  IFSRandomAccessFileImpl getImpl()
  {
    return impl_;  // Note: This may be null.
  }


  /**
   Returns the integrated file system path name of the object represented by
   this IFSRandomAccessFile.
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
     bytes1 = new byte[1];
     bytes2 = new byte[2];
     bytes4 = new byte[4];
     bytes8 = new byte[8];
     impl_ = null;
   }


  /**
   Returns the file length.
   @return The file length, in bytes.
   @exception IOException If an error occurs while communicating with the system.
   **/
  public long length()
    throws IOException
  {
    // Ensure that the file is open.
    open();

    return impl_.length();
  }


  /**
   Places a lock on the file at the specified bytes.
   @param offset The first byte of the file to lock (zero is the first byte).
   @param length The number of bytes to lock.
   @return A key for undoing this lock.

   @exception IOException If an error occurs while communicating with the system.

   @see IFSKey
   @see #unlock
   **/
  public IFSKey lock(int offset,
                     int length)
    throws IOException
  {
    // Validate the arguments.
    if (offset < 0)
    {
      throw new ExtendedIllegalArgumentException("offset (" +
                                                 Integer.toString(offset) +
                                                 ")",
                ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (length <= 0)
    {
      throw new ExtendedIllegalArgumentException("length (" +
                                                 Integer.toString(length) +
                                                 ")",
                ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Ensure that the file is open.
    open();

    return impl_.lock(offset, length);
  }


  /**
   Opens the specified file.

   @exception IOException If an error occurs while communicating with the system.
   **/
  private void open()
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
      impl_.open(); // Note: This also does a connect() if necessary.

      // Fire the file opened event.
      if (fileListeners_.size() != 0)
        IFSFileDescriptor.fireOpenedEvents(this, fileListeners_);
    }
  }


  /**
   Reads the next byte of data from this file.
   @return The next byte of data, or -1 if the end of file is reached.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public int read()
    throws IOException
  {
    int rc = read(bytes1, 0, 1);
    int value;

    if (rc == 1)
    {
      value = (int) bytes1[0];
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

   @exception IOException If an error occurs while communicating with the system.
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
   Reads up to <i>length</i> bytes of data from this input stream into
   <i>data</i>, starting at the array offset <i>dataOffset</i>.
   @param data The buffer into which the data is read.
   @param dataOffset The start offset of the data in the buffer.
   @param length The maximum number of bytes to read.
   @return The total number of bytes read into the buffer, or -1 if there is no more data because the end of file has been reached.

   @exception IOException If an error occurs while communicating with the system.
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

    return impl_.read(data, dataOffset, length, false);
  }



  /**
   Reads a boolean from this file. This method reads a single byte from the
   file. A value of 0 represents false. Any other value represents true.
   @return The boolean value read.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final boolean readBoolean()
    throws IOException
  {
    return (readByte() == 0 ? false : true);
  }


  /**
   Reads a signed 8-bit value from this file.  This method reads a single byte
   from the file.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final byte readByte()
    throws IOException
  {
    int bytesRead = read(bytes1, 0, 1);
    if (bytesRead != 1)
    {
      throw new EOFException();
    }

    return bytes1[0];
  }


  /**
   Reads a Unicode character from this file.  Two bytes are read from the file.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final char readChar()
    throws  IOException
  {
    return (char) readShort();
  }


  /**
   Reads a double from this file. This method reads a long value as if by the
   readLong method and then converts that long to a double using the
   longBitsToDouble method in class Double.
   @return The next eight bytes of this file, interpreted as a double.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final double readDouble()
    throws IOException
  {
    long value = readLong();

    return Double.longBitsToDouble(value);
  }


  /**
   Reads a float from this file. This method reads an int value as if by the
   readInt method and then converts that int to a float using the intBitsToFloat
   method in class Float.
   @return The next four bytes of this file, interpreted as a float.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final float readFloat()
    throws IOException
  {
    int value = readInt();

    return Float.intBitsToFloat(value);
  }


  /**
   Reads <i>data.length</i> bytes from this file into the byte array. This
   method reads repeatedly from the file until all the bytes are read or an
   exception is thrown.
   @param data The buffer into which data is read.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final void readFully(byte[] data)
    throws IOException
  {
    // Validate arguments.
    if (data == null)
      throw new NullPointerException("data");

    readFully(data, 0, data.length);
  }


  /**
   Reads exactly <i>length</i> bytes from this file into the byte array. This
   method reads repeatedly from the file until all the bytes are read or
   an exception is thrown.
   @param data The buffer into which data is read.
   @param dataOffset The start offset in the data buffer.
   @param length The number of bytes to read.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final void readFully(byte[] data,
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
    open();

    impl_.read(data, dataOffset, length, true);
  }


  /**
   Reads a signed 32-bit integer from this file.

   @return The next four bytes of this file, interpreted as an integer.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final int readInt()
    throws IOException
  {
    int bytesRead = read(bytes4, 0, 4);
    if (bytesRead != 4)
    {
      throw new EOFException();
    }

    return (((bytes4[0] & 0xff) << 24) | ((bytes4[1] & 0xff) << 16) |
            ((bytes4[2] & 0xff) << 8) | (bytes4[3] & 0xff));
  }

  /**
   Reads the next line of text from this file. This method successively reads
   bytes from the file until it reaches the end of a line of text.  A line of
   text is terminated by a carriage return character (\r), a newline character
   (\n), a carriage return character immediately followed by a newline
   character, or the end of the input stream. The line-terminating characters,
   if any, are included as part of the string returned.

   @return The next line of text from this file.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final String readLine()
    throws  IOException
  {
    // Ensure that the file is open.
    open();

    return impl_.readLine();
  }


  /**
   Reads a signed 64-bit integer from this file.

   @return The next eight bytes of the file, interpreted as a long.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final long readLong()
    throws IOException
  {
    int bytesRead = read(bytes8, 0, 8);
    if (bytesRead != 8)
    {
      throw new EOFException();
    }

    return (((bytes8[0] & 0xffL) << 56L) | ((bytes8[1] & 0xffL) << 48L) |
            ((bytes8[2] & 0xffL) << 40L) | ((bytes8[3] & 0xffL) << 32L) |
            ((bytes8[4] & 0xffL) << 24L) | ((bytes8[5] & 0xffL) << 16L) |
            ((bytes8[6] & 0xffL) << 8L) | (bytes8[7] & 0xffL));
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
   Reads a signed 16-bit integer from this file.

   @return The next two bytes of this file, interpreted as a short.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final short readShort()
    throws IOException
  {
    int bytesRead = read(bytes2, 0, 2);
    if (bytesRead != 2)
    {
      throw new EOFException();
    }

    return (short) (((bytes2[0] & 0xff) << 8) | (bytes2[1] & 0xff));
  }


  /**
   Reads an unsigned 8-bit number from this file. This method reads a byte from
   this file and returns that byte.
   @return The next byte of this file, interpreted as an unsigned 8-bit number.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final int readUnsignedByte()
    throws IOException
  {
    int bytesRead = read(bytes1, 0, 1);
    if (bytesRead != 1)
    {
      throw new EOFException();
    }

    int i = (int) bytes1[0];
    i = i & 0xff;

    return i;
  }


  /**
   Reads an unsigned 16-bit number from this file.
   @return The next two bytes of this file, interpreted as an unsigned 16-bit
   number.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final int readUnsignedShort()
    throws IOException
  {
    int bytesRead = read(bytes2, 0, 2);
    if (bytesRead != 2)
    {
      throw new EOFException();
    }

    int i = ((bytes2[0] & 0xff) << 8) | (bytes2[1] & 0xff);
    if (i < 0)
    {
      i += 65536;
    }

    return i;
  }


  /**
   Reads in a string from this file. The string has been encoded using a
   modified UTF-8 format.<br>The first two bytes are read as if by
   readUnsignedShort. This values gives the number of following bytes
   that are in the encoded string (note, not the length of the
   resulting string). The following bytes are then interpreted as bytes
   encoding characters in the UTF-8 format and are converted into characters.
   @return A Unicode string.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final String readUTF()
    throws IOException
  {
    // Ensure that the file is open.
    open();

    return impl_.readUTF();
  }


  /**
   Removes a file listener so that it no longer receives file events from
   this IFSRandomAccessFile.
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
   Sets the offset, from the beginning of this file, at which the next read
   or write occurs.
   @param position The absolute position of the file pointer.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public void seek(long position)
    throws IOException
  {
    // Validate the argument.
    if (position < 0L)
    {
      throw new ExtendedIllegalArgumentException("position (" +
                                                 Long.toString(position) +
                                                 ")",
                      ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Ensure that the file is open.
    open();

    fd_.setFileOffset(position); // @A1C - Remove (int) cast to pass long parameter
  }


  /**
   Sets the existence option.
   @param existenceOption Indicates if the file should be created, opened or if the request should fail based on the existence of the file. <ul><li>FAIL_OR_CREATE Fail if exists; create if not<li>OPEN_OR_CREATE Open if exists; create if not<li>OPEN_OR_FAIL Open if exists; fail if not<li>REPLACE_OR_CREATE Replace if exists; create if not<li>REPLACE_OR_FAIL Replace if exists; fail if not</ul>
   @exception PropertyVetoException If the change is vetoed.
   **/
  public void setExistenceOption(int existenceOption)
    throws PropertyVetoException
  {
    validateExistenceOption(existenceOption);

    // Ensure that existenceOption is not changed after a connection
    // is established.
    if (fd_.valid())
    {
      throw new ExtendedIllegalStateException("existenceOption",
                              ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    // Remember the current existenceOption.
    Integer oldExistenceOption = new Integer(existenceOption_);
    Integer newExistenceOption = new Integer(existenceOption);

    // Fire the vetoable change event.
    vetos_.fireVetoableChange("existenceOption", oldExistenceOption,
                              newExistenceOption);

    existenceOption_ = existenceOption;

    // Fire the property change event.
    changes_.firePropertyChange("existenceOption", oldExistenceOption,
                                newExistenceOption);
  }

  /**
   A package-level method that sets the forceToStorage option. When forceToStorage
   is turned on, data must be written before the system replies. Otherwise, the
   system may asynchronously write the data.

   @param forceToStorage If data must be written before the system replies
   **/
  // Note: This method is needed by UserSpaceImplRemote.
  void setForceToStorage(boolean forceToStorage)
  {
    forceToStorage_ = forceToStorage;
    if (impl_ != null)
      impl_.setForceToStorage(forceToStorage);
  }

  // @A2a
  /**
   Sets the length of the file represented by this object.  The file can be made larger or smaller.  If the file is made larger, the contents of the new bytes of the file are undetermined.
   @param length The new length, in bytes.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public void setLength(int length)
    throws IOException
  {
    // Validate arguments.
    if (length < 0)
    {
      throw new ExtendedIllegalArgumentException("length + (" +
                                                 Integer.toString(length) + ")",
                                                 ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    if (impl_ == null)  chooseImpl();

    impl_.setLength(length);

    // Fire the events.
    if (fileListeners_.size() != 0) {
      IFSFileDescriptor.fireModifiedEvents(this, fileListeners_);
    }
  }


  /**
   Sets the access mode.
   @param mode The access mode. <ul><li>"r" read only<li>"w" write only<li>"rw" read/write</ul>.
   @exception PropertyVetoException If the change is vetoed.
   **/
  public void setMode(String mode)
    throws PropertyVetoException
  {
    validateMode(mode);

    // Ensure that mode is not changed after the connection is
    // established.
    if (fd_.valid())
    {
      throw new ExtendedIllegalStateException("mode",
                             ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    // Remember the current mode value.
    String oldMode = mode_;

    // Fire the property veto event.
    vetos_.fireVetoableChange("mode", oldMode, mode);

    mode_ = mode;

    // Fire the property change event.
    changes_.firePropertyChange("mode", oldMode, mode);
  }


  /**
   Sets the file path.
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
   @param shareOption Indicates how users can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>.
   @exception PropertyVetoException If the change is vetoed.
   **/
  public void setShareOption(int shareOption)
    throws PropertyVetoException
  {
    validateShareOption(shareOption);

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
   Skips over the next <i>bytesToSkip</i> bytes in the stream.
   @param bytesToSkip The number of bytes to skip.
   @return The number of bytes skipped.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public int skipBytes(int bytesToSkip)
    throws IOException
  {
    // Validate the agument.
    if (bytesToSkip < 0)
    {
      throw new ExtendedIllegalArgumentException("bytesToSkip (" +
                                                 Integer.toString(bytesToSkip) +
                                                 ")",
                     ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Ensure that the file is open.
    open();

    fd_.incrementFileOffset(bytesToSkip);

    return bytesToSkip;
  }


  /**
   Undoes a lock on this file.
   @param key The key for the lock.

   @exception IOException If an error occurs while communicating with the system.

   @see IFSKey
   @see #lock
   **/
  public void unlock(IFSKey key)
    throws  IOException
  {
    // Validate the argument.
    if (key == null)
      throw new NullPointerException("key");

    // Ensure that the file is open.
    open();

    impl_.unlock(key);
  }

  private static final void validateExistenceOption(int existenceOption)
  {
    if ((existenceOption < OPEN_OR_CREATE) || (existenceOption > REPLACE_OR_FAIL))
    {
      throw new ExtendedIllegalArgumentException("existenceOption (" +
                             Integer.toString(existenceOption) + ")",
                      ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
  }

  private static final void validateMode(String mode)
  {
    if (mode == null)
    {
      throw new NullPointerException("mode");
    }
    if (!(mode.equals("r") || mode.equals("w") || mode.equals("rw")))
    {
      throw new ExtendedIllegalArgumentException("mode (" + mode + ")",
                     ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
  }

  private static final void validateShareOption(int shareOption)
  {
    if ((shareOption < SHARE_NONE) || (shareOption > SHARE_ALL))
    {
      throw new ExtendedIllegalArgumentException("shareOption (" +
                                                 Integer.toString(shareOption) + ")",
                     ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
  }


  /**
   Writes the specified byte to this file.
   @param b The byte to write.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public void write(int b)
    throws  IOException
  {
    bytes1[0] = (byte) b;
    writeBytes(bytes1, 0, 1/*, true*/);
  }


  /**
   Writes <i>data.length</i> bytes of data from the byte array <i>data</i>
   to this file output stream.
   @param data The data to write.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public void write(byte[] data)
    throws  IOException
  {
    // Validate arguments.
    if (data == null)
      throw new NullPointerException("data");

    writeBytes(data, 0, data.length/*, true*/);
  }


  /**
   Writes <i>length</i> bytes from the byte array <i>data</i>, starting at <i>dataOffset</i>, to this file.
   @param data The data.
   @param dataOffset The start offset in the data.
   @param length The number of bytes to write.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public void write(byte[] data,
                    int    dataOffset,
                    int    length)
    throws  IOException
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

    writeBytes(data, dataOffset, length/*, true*/);
  }


  /**
   Writes a boolean to the file as a one-byte value. The value true is written
   out as the value (byte)1.  The value false is written out as the value
   (byte)0.
   @param value The value to be written.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final void writeBoolean(boolean value)
    throws  IOException
  {
    write(value ? (byte) 1 : (byte) 0);
  }


  /**
   Writes a byte to the file as a one-byte value.
   @param value The value to be written as a byte.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final void writeByte(int value)
    throws  IOException
  {
    bytes1[0] = (byte) value;
    writeBytes(bytes1, 0, 1/*, true*/);
  }


  /**
   Writes <i>length</i> bytes from the byte array <i>data</i>, starting at <i>dataOffset</i>, to this File.
   @param data The data.
   @param dataOffset The start offset in the data.
   @param length The number of bytes to write.

   @exception IOException If an error occurs while communicating with the system.
   **/
  private void writeBytes(byte[]  data,
                          int     dataOffset,
                          int     length)
    throws  IOException
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
    open();

    impl_.writeBytes(data, dataOffset, length);

    // Fire the events.
    if (fileListeners_.size() != 0)
      IFSFileDescriptor.fireModifiedEvents(this, fileListeners_);
  }


  /**
   Writes out the string to the file as a sequence of bytes. Each character
   in the string is written out, in sequence, by discarding its high eight
   bits.  Because this method discards eight bits of data, it should not be
   used to write double-byte characters.  Use writeChars(String) instead.
   @param s The bytes to write.

   @exception IOException If an error occurs while communicating with the system.

   @see #writeChars(String)
   **/
  public final void writeBytes(String s)
    throws IOException
  {
    // Validate the argument.
    if (s == null)
      throw new NullPointerException("s");

    // Convert the String to bytes.
    byte[] data = new byte[s.length()];
    for (int i = 0; i < s.length(); i++)
    {
      char c = s.charAt(i);
      data[i] = (byte) (c & 0xff);
    }

    // Write the bytes.
    writeBytes(data, 0, data.length);
  }

  /**
   Writes a char to the file as a two-byte value, high byte first.
   @param value A character value to write.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final void writeChar(int value)
    throws  IOException
  {
    // Convert value to two bytes.
    char c = (char) value;
    bytes2[0] = (byte) (c >>> 8);
    bytes2[1] = (byte) (c & 0xff);

    // Write the bytes.
    writeBytes(bytes2, 0, 2/*, true*/);
  }


  /**
   Writes a string to the file as a sequence of characters. Each character is
   written to the file as if by the writeChar method.
   @param s A String to write.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final void writeChars(String s)
    throws  IOException
  {
    // Validate the argument.
    if (s == null)
      throw new NullPointerException("s");

    // Convert the string to bytes.
    byte[] data = new byte[s.length() * 2];
    for (int i = 0, j = 0; i < s.length(); i++, j += 2)
    {
      char character = s.charAt(i);
      data[j + 1] = (byte) (character & 0xff);
      data[j] = (byte) (character >>> 8);
    }

    // Write the bytes.
    writeBytes(data, 0, data.length/*, true*/);
  }


  /**
   Converts the double argument to a long using the doubleToLongBits method in
   class Double, and then writes that long value to the file as an eight-byte
   quantity, high-byte first.

   @param value The value to write.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final void writeDouble(double value)
    throws  IOException
  {
    writeLong(Double.doubleToLongBits(value));
  }


  /**
   Converts the float argument to an int using the floatToIntBits method in
   class Float, and then writes that int value to the file as a four-byte
   quantity, high-byte first.

   @exception IOException If an error occurs while communicating with the system.
    **/
  public final void writeFloat(float value)
    throws  IOException
  {
    writeInt(Float.floatToIntBits(value));
  }


  /**
   Writes an int to the file as four bytes, high-byte first.

   @param value The int to write.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final void writeInt(int value)
    throws  IOException
  {
    // Convert the int to bytes.
    int v = value;
    for (int i = 0; i < 4; i++, v >>>= 8)
    {
      bytes4[3 - i] = (byte) v;
    }

    // Write the bytes.
    writeBytes(bytes4, 0, 4/*, true*/);
  }


  /**
   Writes a long to the file as eight bytes, high-byte first.

   @param value The value to write.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final void writeLong(long value)
    throws  IOException
  {
    // Convert the long to bytes.
    long v = value;
    for (int i = 0; i < 8; i++, v >>>= 8)
    {
      bytes8[7 - i] = (byte) v;
    }

    // Write the bytes.
    writeBytes(bytes8, 0, 8/*, true*/);
  }


  /**
   Writes a short to the file as two bytes, high-byte first.

   @param value The value to write.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final void writeShort(int value)
    throws  IOException
  {
    // Convert the int to two bytes.
    bytes2[1] = (byte) value;
    bytes2[0] = (byte) (value >>> 8);

    // Write the bytes.
    writeBytes(bytes2, 0, 2/*, true*/);
  }


  /**
   Writes out a string to the file using UTF-8 encoding in a
   machine-independent manner.<br>  First, two bytes are written to the file as
   if by the writeShort method giving the number of bytes to follow. This
   value is the number of bytes actually written out, not the length of the
   string. Following the length, each character of the string is output, in
   sequence, using the UTF-8 encoding for each character.

   @param s The string to write.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public final void writeUTF(String s)
    throws IOException
  {
    // Validate the argument.
    if (s == null)
      throw new NullPointerException("s");

    // Ensure that the file is open.
    open();

    impl_.writeUTF(s);

    if (fileListeners_.size() != 0)
      IFSFileDescriptor.fireModifiedEvents(this, fileListeners_);
  }

}




