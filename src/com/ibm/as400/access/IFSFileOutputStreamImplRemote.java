///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: IFSFileOutputStreamImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


import java.io.InterruptedIOException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;


/**
 Provides a full remote implementation for the IFSFileOutputStream and
 IFSTextFileOutputStream classes.
 **/
class IFSFileOutputStreamImplRemote
extends OutputStream
implements IFSFileOutputStreamImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  private boolean append_ = false;
  private IFSFileDescriptorImplRemote fd_; // file info
  private int maxDataBlockSize_ = 1024; // default

  // Variables used by IFSTextFileOutputStream:
  private OutputStreamWriter writer_;
  private boolean recursing_ = false;

  // Static initialization code.
  static
  {
    // Add all byte stream reply data streams of interest to the
    // AS400 server's reply data stream hash table.
    AS400Server.addReplyStream(new IFSCloseRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSExchangeAttrRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSLockBytesRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSListAttrsRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSOpenRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSReturnCodeRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSWriteRep(), AS400.FILE);
  }

  /**
   Closes this file output stream and releases any system resources associated
   with this stream.
   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  public void close()
    throws IOException
  {
    // An IFSTextFileOutputStream can have a "writer" associated with it.
    // If a writer was instantiated, close it.
    if (writer_ != null)
    {
      // The OutputStreamWriter must be closed but the close method 
      // will attempt to close the underlying OutputStream, which is
      // this object.  This will cause recursion.  To avoid infinite
      // recursion we use a boolean flag to identify when we are
      // recursing.
      synchronized(this)
      {
        if (!recursing_)
        {
          // We aren't recursing yet so set the recursion flag and
          // close the OutputStreamWriter.  Then clear the flag.
          recursing_ = true;
          writer_.close();
          writer_ = null;
          recursing_ = false;
        }
        else
        {
          // Close the OutputStream.
          close0();
        }
      }
    }
    else
    {
      // Close the OutputStream.
      close0();
    }
  }

  private void close0()
    throws IOException
  {
    if (fd_.isOpen_)
    {
      // Close the file.  Send a close request to the server.
      ClientAccessDataStream ds = null;
      IFSCloseReq req = new IFSCloseReq(fd_.getFileHandle());
      try
      {
        ds = (ClientAccessDataStream) fd_.getServer().sendAndReceive(req);
      }
      catch(ConnectionDroppedException e)
      {
        Trace.log(Trace.ERROR, "Byte stream connection lost during close", e);
        connectionDropped(e);
      }
      catch(InterruptedException e)
      {
        fd_.setOpen(false);
        Trace.log(Trace.ERROR, "Interrupted", e);
        throw new InterruptedIOException(e.getMessage());
      }

      // Validate the reply.
      if (ds instanceof IFSCloseRep)
      {
        int rc = ((IFSCloseRep) ds).getReturnCode();
        if (rc != 0)
        {
          Trace.log(Trace.ERROR, "IFSCloseRep return code ", rc);
          throw new ExtendedIOException(rc);
        }
      }
      else if (ds instanceof IFSReturnCodeRep)
      {
        int rc = ((IFSReturnCodeRep) ds).getReturnCode();
        if (rc != IFSReturnCodeRep.SUCCESS)
        {
          Trace.log(Trace.ERROR, "IFSReturnCodeRep return code ", rc);
          throw new ExtendedIOException(rc);
        }
      }
      else
      {
        Trace.log(Trace.ERROR, "Unknown reply data stream ", ds.data_);
        throw new
          InternalErrorException(Integer.toHexString(ds.getReqRepID()),
                                 InternalErrorException.DATA_STREAM_UNKNOWN);
      }
      fd_.setOpen(false);
    }
  }


  /**
   Establishes communications with the AS400.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  private void connect()
    throws AS400SecurityException, IOException
  {
    // Connect to the AS400 byte stream server if not already connected.
    if (fd_.getServer() == null)
    {
      // Ensure that the system has been set.
      AS400ImplRemote system = fd_.getSystem();
      if (system == null)
      {
        throw new ExtendedIllegalStateException("system",
                                 ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }

      fd_.setServer(system.getConnection(AS400.FILE, false));

      // Exchange attributes with the server.
      exchangeServerAttributes();
    }
  }

  public void connectAndOpen(int ccsid)
    throws AS400SecurityException, IOException
  {
    connect();
    if (ccsid == -1)
      open(fd_.getPreferredCCSID());
    else
      open(ccsid);
  }

  /**
   Disconnects from the byte stream server.
   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   **/
  void connectionDropped(ConnectionDroppedException e)
    throws ConnectionDroppedException
  {
    if (fd_.getServer() != null)
    {
      fd_.getSystem().disconnectServer(fd_.getServer());
      fd_.setServer(null);
      fd_.setOpen(false);
    }
    Trace.log(Trace.ERROR, "Byte stream connection lost.");
    throw (ConnectionDroppedException)e.fillInStackTrace();
  }


  /**
   Exchanges server attributes.
   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  private void exchangeServerAttributes()
    throws IOException
  {
      AS400Server server = fd_.getServer();
      synchronized (server)
      {
	  IFSExchangeAttrRep rep =
	    (IFSExchangeAttrRep)server.getExchangeAttrReply();
	  if (rep == null)
	  {
	      try
	      {
	          // Use GMT date/time, don't use posix style return codes,
	          // use PC pattern matching semantics, maximum data transfer size
	          // of 0xffffffff, preferred CCSID of 0xf200.
		  rep = (IFSExchangeAttrRep)server.sendExchangeAttrRequest(new IFSExchangeAttrReq(true, false,
					   IFSExchangeAttrReq.PC_PATTERN_MATCH,
					   0xffffffff, 0xf200));
	      }
	      catch(ConnectionDroppedException e)
	      {
		  Trace.log(Trace.ERROR, "Byte stream server connection lost");
		  connectionDropped(e);
	      }
	      catch(InterruptedException e)
	      {
		  fd_.getSystem().disconnectServer(server);
		  fd_.setServer(null);
		  Trace.log(Trace.ERROR, "Interrupted", e);
		  throw new InterruptedIOException(e.getMessage());
	      }
	      catch(IOException e)
	      {
		  fd_.getSystem().disconnectServer(server);
		  fd_.setServer(null);
		  Trace.log(Trace.ERROR, "I/O error during attribute exchange.");
		  throw (IOException)e.fillInStackTrace();
	      }
	  }

          // Process the exchange attributes reply.
	  if (rep instanceof IFSExchangeAttrRep)
	  {
	      maxDataBlockSize_ = ((IFSExchangeAttrRep) rep).getMaxDataBlockSize();
	      int preferredCCSID = rep.getPreferredCCSID();
	      fd_.setPreferredCCSID(preferredCCSID);
	      fd_.setConverter(ConverterImplRemote.getConverter(preferredCCSID,
								fd_.getSystem()));
	  }
	  else
	  {
              // Should never happen.
	      fd_.getSystem().disconnectServer(server);
	      fd_.setServer(null);
	      Trace.log(Trace.ERROR, "Unknown reply data stream ", rep.data_);
	      throw new
		InternalErrorException(Integer.toHexString(rep.getReqRepID()),
				       InternalErrorException.DATA_STREAM_UNKNOWN);
	  }
      }
  }


  /**
   Ensures that the file output stream is closed when there are no more
   references to it.
   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  protected void finalize()
    throws IOException
  {
    if (fd_ != null && fd_.isOpen_)
    {
      // Close the file.  Send a close request to the server.
      IFSCloseReq req = new IFSCloseReq(fd_.getFileHandle());
      try
      {
        fd_.getServer().sendAndDiscardReply(req);
      }
      catch(IOException e)
      {
        throw (IOException)e.fillInStackTrace();
      }
      catch(Exception e)
      {
        Trace.log(Trace.ERROR, "Error during finalization.", e);
      }
      finally
      {
        fd_.setOpen(false);
      }
    }

    try
    {
      super.finalize();
    }
    catch(Throwable e)
    {
      throw new IOException(e.getMessage());
    }
  }

  /**
   Forces any buffered output bytes to be written.

   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  public void flush()
    throws IOException
  {
    // An IFSTextFileOutputStream can have a "writer" associated with it.
    // If a writer was instantiated, flush it.
    if (writer_ != null)
    {
      // The OutputStreamWriter must be flushed but the flush method 
      // will attempt to flush the underlying OutputStream, which is
      // this object.  This will cause recursion.  To avoid infinite
      // recursion we use a boolean flag to identify when we are
      // recursing.
      synchronized(this)
      {
        if (!recursing_)
        {
          // We aren't recursing yet so set the recursion flag and
          // flush the OutputStreamWriter.  Then clear the flag.
          recursing_ = true;
          writer_.flush();
          recursing_ = false;
        }
        else
        {
          // Flush the OutputStream.
          flush0();
        }
      }
    }
    else
    {
      // Flush the OutputStream.
      flush0();
    }
  }

  private void flush0()
    throws IOException
  {
    // Ensure that the file is open.
    open(fd_.getPreferredCCSID());

    // Request that changes be committed to disk.
    IFSCommitReq req = new IFSCommitReq(fd_.getFileHandle());
    ClientAccessDataStream ds = null;
    try
    {
      ds = (ClientAccessDataStream) fd_.getServer().sendAndReceive(req);
    }
    catch(ConnectionDroppedException e)
    {
      Trace.log(Trace.ERROR, "Byte stream server connection lost");
      connectionDropped(e);
    }
    catch(InterruptedException e)
    {
      Trace.log(Trace.ERROR, "Interrupted", e);
      throw new InterruptedIOException(e.getMessage());
    }

    // Verify that the request was successful.
    if (ds instanceof IFSReturnCodeRep)
    {
      int rc = ((IFSReturnCodeRep) ds).getReturnCode();
      if (rc != IFSReturnCodeRep.SUCCESS)
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
    // Assume the argument has been validated by the public class.

    // Ensure that the file is open.
    open(fd_.getPreferredCCSID());

    // Attempt to lock the file.
    ClientAccessDataStream ds = null;
    try
    {
      // Issue a mandatory, exclusive lock bytes request.  Mandatory
      // means that the file system enforces the lock by causing any
      // operation which conflicts with the lock to fail.  Exclusive
      // means that only the owner of the lock can read or write the
      // locked area.
      IFSLockBytesReq req =
        new IFSLockBytesReq(fd_.getFileHandle(), true, false,
                            fd_.getFileOffset(), length);
      ds = (ClientAccessDataStream) fd_.getServer().sendAndReceive(req);
    }
    catch(ConnectionDroppedException e)
    {
      Trace.log(Trace.ERROR, "Byte stream server connection lost");
      connectionDropped(e);
    }
    catch(InterruptedException e)
    {
      Trace.log(Trace.ERROR, "Interrupted", e);
      throw new InterruptedIOException(e.getMessage());
    }

    // Verify the reply.
    if (ds instanceof IFSLockBytesRep)
    {
      int rc = ((IFSLockBytesRep) ds).getReturnCode();
      if (rc != 0)
      {
        Trace.log(Trace.ERROR, "IFSLockBytesRep return code ", rc);
        throw new ExtendedIOException(rc);
      }
    }
    else if (ds instanceof IFSReturnCodeRep)
    {
      int rc = ((IFSReturnCodeRep) ds).getReturnCode();
      Trace.log(Trace.ERROR, "IFSReturnCodeRep return code ", rc);
      throw new ExtendedIOException(rc);
    }
    else
    {
      // Unknown data stream.
      Trace.log(Trace.ERROR, "Unknown reply data stream ", ds.data_);
      throw new
        InternalErrorException(Integer.toHexString(ds.getReqRepID()),
                               InternalErrorException.DATA_STREAM_UNKNOWN);
    }

    // Generate the key for this lock.
    IFSKey key = new IFSKey(fd_.getFileHandle(), fd_.getFileOffset(), length, true);

    return key;
  }


  /**
   Opens the specified file.

   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  public void open(int fileDataCCSID)
    throws IOException
  {
    // If the file is already open, do nothing.
    if (fd_.isOpen_)
    {
      return;
    }

    // Throw ConnectionDroppedException if attempting to open the file
    // after it has been closed.
    if (!fd_.isOpenAllowed_)
    {
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
      connect();
    }
    catch(AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, "Security exception", e);
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    // Convert the path name to the AS/400 CCSID.
    byte[] pathname = fd_.getConverter().stringToByteArray(path);

    // Request that the file be created if it doesn't exist, opened
    // if it does.
    if (fileDataCCSID == -1)
    {
      fileDataCCSID = fd_.getPreferredCCSID();
    }
    IFSOpenReq req = new IFSOpenReq(pathname, fd_.getPreferredCCSID(),
                                    fileDataCCSID,
                                    IFSOpenReq.WRITE_ACCESS,
                                    ~fd_.getShareOption(),
                                    IFSOpenReq.NO_CONVERSION,
                                    (append_ ? 1 : 2));
    ClientAccessDataStream ds = null;
    try
    {
      ds = (ClientAccessDataStream) fd_.getServer().sendAndReceive(req);
    }
    catch(ConnectionDroppedException e)
    {
      Trace.log(Trace.ERROR, "Byte stream server connection lost");
      connectionDropped(e);
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
      fd_.setFileHandle(rep.getFileHandle());
      fd_.setOpen(true);
      fd_.setOpenAllowed(false);
      if (append_)
      {
        // We must append to the file.
        fd_.setFileOffset(rep.getFileSize());
      }
    }
    else if (ds instanceof IFSReturnCodeRep)
    {
      // If the file can't be opened because another open instance of
      // this file isn't allowing file sharing, then the byte stream
      // server returns file-in-use.  In this case, throw an IOException
      // with a detail message of 32 (sharing-violation).
      int rc = ((IFSReturnCodeRep) ds).getReturnCode();
      if (rc == IFSReturnCodeRep.FILE_IN_USE)
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


  public void setAppend(boolean append)
  {
    append_ = append;
  }

  /**
   Sets the file descriptor.
   @param fd The file descriptor.

   **/
  public void setFD(IFSFileDescriptorImpl fd)
  {
    // Assume the argument has been validated by the public class.

    // Cast the argument to an xxxImplRemote.
    try
    {
      fd_ = (IFSFileDescriptorImplRemote)fd;
    }
    catch (ClassCastException e)
    {
      Trace.log(Trace.ERROR, "Argument is not an instance of IFSFileDescriptorImplRemote", e);
      throw new
        InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
    }
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
    // Assume the argument has been validated by the public class.

    // Verify that this key is compatible with this file.
    if (key.fileHandle_ != fd_.getFileHandle())
    {
      Trace.log(Trace.ERROR, "Attempt to use IFSKey on different file stream.");
      throw new ExtendedIllegalArgumentException("key",
                                    ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Ensure that the file is open.
    open(fd_.getPreferredCCSID());

    // Attempt to unlock the file.
    ClientAccessDataStream ds = null;
    try
    {
      // Issue an unlock bytes request.
      IFSUnlockBytesReq req =
        new IFSUnlockBytesReq(key.fileHandle_, key.offset_,
                              key.length_, key.isMandatory_);
      ds = (ClientAccessDataStream) fd_.getServer().sendAndReceive(req);
    }
    catch(ConnectionDroppedException e)
    {
      Trace.log(Trace.ERROR, "Byte stream server connection lost");
      connectionDropped(e);
    }
    catch(InterruptedException e)
    {
      Trace.log(Trace.ERROR, "Interrupted", e);
      throw new InterruptedIOException(e.getMessage());
    }

    // Verify the reply.
    if (ds instanceof IFSReturnCodeRep)
    {
      int rc = ((IFSReturnCodeRep) ds).getReturnCode();
      if (rc != IFSReturnCodeRep.SUCCESS)
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
   Writes the specified byte to this file output stream.
   <br>This method is implemented to qualify this class as an OutputStream.
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
    // Assume the arguments have been validated by the public class.

    // Ensure that the file is open.
    open(fd_.getPreferredCCSID());

    int fileHandle = fd_.getFileHandle();

    // @A4A (beginning of code block)
    if (append_)  // We must append to the very end of the file.
    {
      IFSListAttrsReq req = new IFSListAttrsReq(fileHandle);

      // Send the request.
      ClientAccessDataStream ds = null;
      try
      {
        // Send the request and receive the response.
        ds = (ClientAccessDataStream) fd_.getServer().sendAndReceive(req);
      }
      catch(ConnectionDroppedException e)
      {
        Trace.log(Trace.ERROR, "Byte stream server connection lost");
        connectionDropped(e);
      }
      catch(InterruptedException e)
      {
        Trace.log(Trace.ERROR, "Interrupted", e);
        throw new InterruptedIOException(e.getMessage());
      }

      if (ds instanceof IFSListAttrsRep)
      {
        // Get the file information.
        IFSListAttrsRep rep = (IFSListAttrsRep) ds;

        fd_.setFileOffset(rep.getSize());
      }
      else if (ds instanceof IFSReturnCodeRep)
      {
        int rc = ((IFSReturnCodeRep) ds).getReturnCode();
        if (rc != IFSReturnCodeRep.SUCCESS)
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
    // @A4A (end of code block)

    // Send write requests until all data has been written.
    while(length > 0)
    {
      // Determine how much data can be written on this request.
      int writeLength = (length > maxDataBlockSize_ ?
                         maxDataBlockSize_ : length);

      // Build the write request.  Set the chain bit if there is
      // more data to write.
      IFSWriteReq req = new IFSWriteReq(fileHandle, fd_.getFileOffset(),
                                        data, dataOffset, writeLength,
                                        0xffff);
      if (length - writeLength > 0)
      {
        // Indicate that there is more to write.
        req.setChainIndicator(1);
      }

      // Send the request.
      ClientAccessDataStream ds = null;
      try
      {
        // Send the request and receive the response.
        ds = (ClientAccessDataStream) fd_.getServer().sendAndReceive(req);
      }
      catch(ConnectionDroppedException e)
      {
        Trace.log(Trace.ERROR, "Byte stream server connection lost");
        connectionDropped(e);
      }
      catch(InterruptedException e)
      {
        Trace.log(Trace.ERROR, "Interrupted", e);
        throw new InterruptedIOException(e.getMessage());
      }

      // Check the reply.
      if (ds instanceof IFSWriteRep)
      {
        IFSWriteRep rep = (IFSWriteRep) ds;
        int rc = rep.getReturnCode();
        if (rc != 0)
        {
          throw new ExtendedIOException(rc);
        }

        // Advance the file pointer the length of the data
        // written.
        int lengthWritten = writeLength - rep.getLengthNotWritten();
        fd_.incrementFileOffset(lengthWritten);
        dataOffset += lengthWritten;
        length -= lengthWritten;

        // Ensure that all data requested was written.
        if (lengthWritten != writeLength)
        {
          throw new ExtendedIOException(ExtendedIOException.UNKNOWN_ERROR);
        }
      }
      else if (ds instanceof IFSReturnCodeRep)
      {
        int rc = ((IFSReturnCodeRep) ds).getReturnCode();
        if (rc != IFSReturnCodeRep.SUCCESS)
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
  }


  // Used by IFSTextFileOutputStream only:
  /**
   Writes characters to this text file input stream.
   The characters that are written to the file are converted to the
   specified CCSID.  
   @param data The characters to write to the stream.
   @param ccsid The CCSID for the data.

   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  public void writeText(String data, int ccsid)
    throws IOException
  {
    // Assume the arguments have been validated by the public class.

    // Ensure that the file is open.
    open(ccsid);

    // Create the OutputStreamWriter if we don't already have one.
    if (writer_ == null)
    {
      // Determine the file character encoding if the CCSID property
      // value has not been set.
      if (ccsid == -1)
      {
        // Issue a Look Up request to obtain the OA2 structure, which
        // contains the code page.
        int codePage = 0;
        try
        {
          ClientAccessDataStream ds = null;

          // Issue the "list attributes" request.
          byte[] pathname = fd_.getConverter().stringToByteArray(fd_.getPath());

          IFSListAttrsReq req =
            new IFSListAttrsReq(fd_.getFileHandle(), (short)0x44);
          // Note: 0x44 indicates "Use the open instance of the file handle,
          //       and return an OA2 structure in the reply".

          ds = (ClientAccessDataStream) fd_.getServer().sendAndReceive(req);

          boolean done = false;
          boolean gotCodePage = false;
          do
          {
            if (ds instanceof IFSListAttrsRep)
            {
              if (gotCodePage)
                Trace.log(Trace.DIAGNOSTIC, "Received multiple replies " +
                          "from ListAttributes request.");
              codePage = ((IFSListAttrsRep) ds).getCodePage();
              gotCodePage = true;
            }
            else if (ds instanceof IFSReturnCodeRep)
            {
              // If the return code is NO_MORE_FILES then all files
              // that match the specification have been returned.
              int rc = ((IFSReturnCodeRep) ds).getReturnCode();
              if (rc != IFSReturnCodeRep.NO_MORE_FILES &&
                  rc != IFSReturnCodeRep.SUCCESS)
              {
                Trace.log(Trace.ERROR, "IFSReturnCodeRep return code = ", rc);
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
                ds = (ClientAccessDataStream) fd_.getServer().receive(req.getCorrelation());
              }
              catch(ConnectionDroppedException e)
              {
                Trace.log(Trace.ERROR, "Byte stream server connection lost.");
                connectionDropped(e);
              }
              catch(InterruptedException e)
              {
                Trace.log(Trace.ERROR, "Interrupted");
                throw new InterruptedIOException(e.getMessage());
              }
            }
          }
          while (!done);

          if (!gotCodePage || codePage == 0)
          {
            Trace.log(Trace.ERROR, "Unable to determine code page of file.");
            throw new ExtendedIOException(ExtendedIOException.UNKNOWN_ERROR);
          }

        }
        catch(ConnectionDroppedException e)
        {
          connectionDropped(e);
        }
        catch(InterruptedException e)
        {
          Trace.log(Trace.ERROR, "Interrupted", e);
          throw new InterruptedIOException(e.getMessage());
        }

        // Convert the code page to CCSID.  
        ccsid = codePage;
      }

      // Convert the CCSID to the encoding string. 
      String encoding = CcsidEncodingMap.ccsidToEncoding(ccsid == 0xf200 ?
                                                         0x34b0 : ccsid);

      // If there is no encoding for this CCSID, throw an
      // UnsupportedEncodingException.
      if (encoding == null)
      {
        throw new UnsupportedEncodingException(Integer.toString(ccsid));
      }

      // @A1A
      // Added code to check for 13488 because of a change made
      // in the ccsid to encoding mapping (in v3r2m0, 13488 is
      // mapped to "Unicode". Now, 13488 is mapped to 13488.)
      if (encoding.equals("13488")) {  // @A1A
        encoding = "Unicode";        // @A1A
      }                                // @A1A

      // Instantiate the OutputStreamWriter.
      writer_ = new OutputStreamWriter(this, encoding);
    }

    // Write the characters of the String.
    writer_.write(data, 0, data.length());
  }

}
