///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileInputStreamImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.InterruptedIOException;
import java.io.IOException;


/**
 Provides a full remote implementation for the IFSFileInputStream and
 IFSTextFileInputStream classes.
 **/
class IFSFileInputStreamImplRemote extends InputStream
implements IFSFileInputStreamImpl
{
  private IFSFileDescriptorImplRemote fd_; // file info

  // Variables needed by subclass IFSTextFileInputStream:
  transient private ConvTableReader reader_;            // @B7a

  // Used for debugging only.  This should always be false for production.
  // When this is false, all debug code will theoretically compile out.
  private static final boolean DEBUG = false;  // @B2A

  // Static initialization code.
  static
  {
    // Add all byte stream reply data streams of interest to the
    // AS400 server's reply data stream hash table.
    AS400Server.addReplyStream(new IFSListAttrsRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSOpenRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSReturnCodeRep(), AS400.FILE);
  }


  // Note: This method is required by java.io.InputStream
  /**
   Returns the number of bytes that can be read from this file input stream.
   If the actual number of available bytes exceeds <tt>Integer.MAX_VALUE</tt>, then <tt>Integer.MAX_VALUE</tt> is returned.
   @return The number of bytes that can be read from this file input stream, or <tt>Integer.MAX_VALUE</tt>, whichever is less.

   @exception IOException If an error occurs while communicating with the server.
  **/
  public int available()
    throws IOException
  {
    return (int)Math.min(availableLong(), (long)Integer.MAX_VALUE);
  }


  /**
   Returns the number of bytes that can be read from this file input stream.
   @return The number of bytes that can be read from this file input stream.

   @exception IOException If an error occurs while communicating with the server.
  **/
  private long availableLong()
    throws IOException
  {
    // Ensure that the file is open.
    open();

    // Bytes available = (file size) minus (current cursor position).
    return (getFileSize() - fd_.getFileOffset());  // @B8c
  }

  /**
   Closes this file input stream and releases any system resources associated
   with the stream.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the server.
   @exception InterruptedIOException If this thread is interrupted.
   
   **/
  public void close()
    throws IOException
  {
    fd_.close0();  // @B2C
  }

  public void connectAndOpen()
    throws AS400SecurityException, IOException
  {
    fd_.connect();
    open();
  }


  /**
   Ensures that the file input stream is closed when there are no more
   references to it.
   @exception IOException If an error occurs while communicating with the server.
   **/
  protected void finalize()
    throws IOException
  {
    if (fd_ != null)
      fd_.finalize0();  // @B2C

    try
    {
      super.finalize();
    }
    catch(Throwable e)
    {
      Trace.log(Trace.ERROR, "Error during finalization.", e);
      throw new IOException(e.getMessage());
    }
  }


  /**
   Returns the file length (in bytes).
   @return The file length.
   **/
  private final long getFileSize()                          // @B8c
    throws IOException
  {
    long size = 0;                                             // @B8c

    // Process attribute replies.
    IFSListAttrsRep reply = null;
    IFSListAttrsReq req = new IFSListAttrsReq(fd_.getFileHandle());
    ClientAccessDataStream ds = null;
    try
    {
      ds = (ClientAccessDataStream) fd_.getServer().sendAndReceive(req);
    }
    catch(ConnectionDroppedException e)
    {
      fd_.connectionDropped(e);
    }
    catch(InterruptedException e)
    {
      Trace.log(Trace.ERROR, "Interrupted.", e);
      throw new InterruptedIOException(e.getMessage());
    }

    boolean done = false;
    do
    {
      if ((reply == null) && (ds instanceof IFSListAttrsRep))
      {
        reply = (IFSListAttrsRep) ds;
      }
      else if (ds instanceof IFSReturnCodeRep)
      {
        // If the return code is NO_MORE_FILES then all files
        // that match the specification have been returned.
        // Anything else indicates an error.
        int rc = ((IFSReturnCodeRep) ds).getReturnCode();
        if (rc != IFSReturnCodeRep.NO_MORE_FILES)
        {
          Trace.log(Trace.ERROR, "IFSReturnCodeRep return code ", rc);
          throw new ExtendedIOException(rc);
        }
      }
      else
      {
        // Unknown data stream.
        Trace.log(Trace.ERROR, "Unknown reply data stream ", ds.data_);
        throw new
          InternalErrorException(Integer.toHexString(ds.getReqRepID()),
                                 InternalErrorException.DATA_STREAM_UNKNOWN);
      }

      // Fetch the next reply if not already done.
      done = ((IFSDataStream) ds).isEndOfChain();
      if (!done)
      {
        try
        {
          ds = (ClientAccessDataStream)
            fd_.getServer().receive(req.getCorrelation());
        }
        catch(ConnectionDroppedException e)
        {
          fd_.connectionDropped(e);
        }
        catch(InterruptedException e)
        {
          Trace.log(Trace.ERROR, "Interrupted", e);
          throw new InterruptedIOException(e.getMessage());
        }
      }
    }
    while (!done);

    if (reply == null)
    {
      // This shouldn't happen.  Since there is no reply to return we
      // throw an exception.
      Trace.log(Trace.ERROR, "No reply available.");
      throw new InternalErrorException(InternalErrorException.UNKNOWN);
    }
    size = reply.getSize(fd_.serverDatastreamLevel_);

    return size;
  }


  /**
   Places a lock on the file at the current position for the specified
   number of bytes.
   @param length The number of bytes to lock.
   @return The key for undoing this lock.
 
   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the server.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the server cannot be started.
   @exception UnknownHostException If the server cannot be located.

   @see IFSKey
   @see #unlock
   **/
  public IFSKey lock(int length)
    throws IOException
  {
    // Assume the argument has been validated by the public class.

    // Ensure that the file is open.
    open();

    return fd_.lock(length);  // @B2C
  }


  /**
   Opens the specified file.
   **/
  public void open()
    throws IOException
  {
    // If the file is already open, do nothing.
    if (fd_.isOpen_)
    {
      return;
    }

    // Throw ConnectionDroppedException if attempting to reopen the file.
    if (!fd_.isOpenAllowed_)
    {
      Trace.log(Trace.ERROR, "Attempting to re-open a closed stream.");
      throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_NOT_ACTIVE);
    }

    // Ensure that the path has been set.
    String path = fd_.getPath();
    if (path.length() == 0)
    {
      throw new ExtendedIllegalStateException("path",
                              ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    // Ensure that we are connected to the byte stream server.
    try
    {
      fd_.connect();
    }
    catch(AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, "Access to byte stream server on '" +
                fd_.getSystem().getSystemName() + "' denied.", e);
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    // Convert the path name to the server CCSID.
    byte[] pathname = fd_.getConverter().stringToByteArray(path);

    // Request that the file be opened.
    IFSOpenReq req = new IFSOpenReq(pathname, fd_.preferredServerCCSID_,
                                    fd_.preferredServerCCSID_,
                                    IFSOpenReq.READ_ACCESS,
                                    ~fd_.getShareOption(),
                                    IFSOpenReq.NO_CONVERSION, 8,
                                    fd_.serverDatastreamLevel_);
    ClientAccessDataStream ds = null;
    try
    {
      ds = (ClientAccessDataStream) fd_.getServer().sendAndReceive(req);
    }
    catch(ConnectionDroppedException e)
    {
      fd_.connectionDropped(e);
    }
    catch(InterruptedException e)
    {
      Trace.log(Trace.ERROR, "Interrupted", e);
      throw new InterruptedIOException(e.getMessage());
    }

    // Verify that the open request was successful.
    if (ds instanceof IFSOpenRep)
    {
      // Get the file information.
      IFSOpenRep rep = (IFSOpenRep) ds;
      fd_.setOpen(true, rep.getFileHandle());
      fd_.setOpenAllowed(false);
    }
    else if (ds instanceof IFSReturnCodeRep)
    {
      // The byte stream server reports file-not-found or path-not-found
      // if the file can't be opened.  If the file can't be opened
      // because another open instance of this file isn't allowing
      // file sharing, then the byte stream server returns file-in-use.  Throw
      // FileNotFoundException with the file name as the detail
      // message in the first two cases.  For the third case, throw
      // an IOException with a detail message of 32 (sharing-violation).
      int rc = ((IFSReturnCodeRep) ds).getReturnCode();
      if (rc == IFSReturnCodeRep.FILE_NOT_FOUND ||
          rc == IFSReturnCodeRep.PATH_NOT_FOUND)
      {
        Trace.log(Trace.ERROR, fd_.getPath() + " not found.");
        throw new FileNotFoundException(fd_.getPath());
      }
      else if (rc == IFSReturnCodeRep.FILE_IN_USE)
      {
        Trace.log(Trace.ERROR, "IFSReturnCodeRep return code ", rc);
        throw new ExtendedIOException(ExtendedIOException.SHARING_VIOLATION);
      }
      else
      {
        Trace.log(Trace.ERROR, "IFSReturnCodeRep return code ", rc);
        throw new ExtendedIOException(rc);
      }
    }
    else
    {
      // Unknown data stream.
      Trace.log(Trace.ERROR, "Unknown reply data stream ", ds.data_);
      throw new
        InternalErrorException(Integer.toHexString(ds.getReqRepID()),
                               InternalErrorException.DATA_STREAM_UNKNOWN);
    }
  }


  /**
   Reads the next byte of data from this input stream.
   <br> Note: This method is included in order to qualify this class as an extension
   of java.io.InputStream.
   @return The next byte of data, or -1 if the end of file is reached.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the server.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the server cannot be started.
   @exception UnknownHostException If the server cannot be located.

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
   Reads up to <i>length</i> bytes of data from this input stream into <i>data</i>, starting at the array offset <i>dataOffset</i>.
   @param data The buffer into which the data is read.
   @param offset The start offset of the data in the buffer.
   @param length The maximum number of bytes to read
   @return The total number of bytes read into the buffer, or -1 if there is no more data because the end of file has been reached.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the server.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the server cannot be started.
   @exception UnknownHostException If the server cannot be located.

   **/
  public int read(byte[] data,
                  int    dataOffset,
                  int    length)
    throws IOException
  {
    // Assume the arguments have been validated by the public class.

    // Ensure that the file is open.
    open();

    int bytesRead = fd_.read(data, dataOffset, length);  // @B2C
    return bytesRead;
  }

  /**
   Reads up to <i>length</i> characters from this text file input stream.
   The file contents are converted from the file data CCSID to Unicode if
   the encoding is supported.
   <br>Note: This method is for use by subclass IFSTextFileInputStream only.
   @param length The number of characters to read from the stream.
   @return The characters read from the stream.  If the end of file has been
   reached an empty String is returned.
   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the server.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the server cannot be started.
   @exception UnknownHostException If the server cannot be located.
   @exception UnsupportedEncodingException If the file's character encoding is not supported.
   @deprecated Used only by IFSTextFileInputStream, which is deprecated.
   **/
  public String readText(int length)
    throws IOException
  {
    // Assume the argument has been validated by the public class.

    String data = "";

    // Ensure that the file is open.
    open();

    // Create the InputStreamReader if we don't already have one.
    if (reader_ == null)   // @B4c
    {
      reader_ = new ConvTableReader(this, fd_.getCCSID());  // @B7a
    }

    data = reader_.read(length);  // @B7a
    if (data == null) data = "";  // @B7a

    return data;
  }


  /**
   Sets the file descriptor.
   @param fd The file descriptor.
   **/
  public void setFD(IFSFileDescriptorImpl fd)
  {
    // Assume the argument has been validated by the public class.

    // Cast the argument to an xxxImplRemote.
    fd_ = IFSFileDescriptorImplRemote.castImplToImplRemote(fd);  // @B2C
  }


  /**
   Skips over the next <i>bytesToSkip</i> bytes in the file input stream.
   This method may skip less bytes than specified if the end of file is
   reached.  The actual number of bytes skipped is returned.

   @param bytesToSkip The number of bytes to skip.
   @return The actual number of bytes skipped.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the server.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the server cannot be started.
   @exception UnknownHostException If the server cannot be located.
  
   **/
  public long skip(long bytesToSkip)
    throws IOException
  {
    // Assume the argument has been validated by the public class.

    // Ensure that the file is open.
    open();

    long bytesSkipped;
    long bytesAvail = availableLong();
    if (bytesToSkip > bytesAvail)
    {
      // Skip to the end of file.
      fd_.incrementFileOffset(bytesAvail);
      bytesSkipped = bytesAvail;
    }
    else
    {
      // Skip ahead the specified number of bytes.
      fd_.incrementFileOffset(bytesToSkip);
      bytesSkipped = bytesToSkip;
    }

    return bytesSkipped;
  }

  /**
   Undoes a lock on this file.
   @param key The key for the lock.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the server.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the server cannot be started.
   @exception UnknownHostException If the server cannot be located.
 
   @see IFSKey
   @see #lock
   **/
  public void unlock(IFSKey key)
    throws IOException
  {
    // Assume the argument has been validated by the public class.

    // Ensure that the file is open.
    open();

    fd_.unlock(key);  // @B2C
  }

}
