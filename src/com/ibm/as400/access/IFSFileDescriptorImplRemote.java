///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: IFSFileDescriptorImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InterruptedIOException;


/**
 Provides a full remote implementation for the IFSFileDescriptor class.
 **/
class IFSFileDescriptorImplRemote
implements IFSFileDescriptorImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  // Note: We allow direct access to some of these fields, for performance.  @B2C
          ConverterImplRemote converter_;
  private int         fileHandle_;
          int         preferredServerCCSID_;
          int         serverDataStreamLevel_; // @B3A
  private int         fileOffset_;
          boolean     isOpen_;
          boolean     isOpenAllowed_ = true;
  private Object      parent_;  // The object that instantiated this IFSDescriptor.
          String      path_ = "";
          AS400Server server_;  // Note: AS400Server is not serializable.
  private int         shareOption_;
          AS400ImplRemote system_;

  private Boolean     fileOffsetLock_ = new Boolean("true");
                         // Semaphore for synchronizing access to fileOffset_.

  private int         maxDataBlockSize_ = 1024; // @B2A
       // Used by IFSFileOutputStreamImplRemote, IFSRandomAccessFileImplRemote.

  private boolean     determinedSystemVRM_ = false;  // @B3A @B4C
  private int         systemVRM_;                    // @B3A @B4C

  // Used for debugging only.  This should always be false for production.
  // When this is false, all debug code will theoretically compile out.
  private static final boolean DEBUG = false;  // @B3A

  // Static initialization code.
  static
  {
    // Add all byte stream reply data streams of interest to the
    // AS400 server's reply data stream hash table.
    AS400Server.addReplyStream(new IFSCloseRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSExchangeAttrRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSLockBytesRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSReturnCodeRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSWriteRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSReadRep(), AS400.FILE);
  }


  // Cast an IFSFileDescriptorImpl object into an IFSFileDescriptorImplRemote.
  // Used by various IFS*ImplRemote classes.
  static IFSFileDescriptorImplRemote castImplToImplRemote(IFSFileDescriptorImpl fd)
  {
    try
    {
      return (IFSFileDescriptorImplRemote)fd;
    }
    catch (ClassCastException e)
    {
      Trace.log(Trace.ERROR, "Argument is not an instance of IFSFileDescriptorImplRemote", e);
      throw new
        InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
    }
  }


  public void close()
  {
    isOpen_ = false;
  }

  void close0()  // @B2A - code relocated from IFSFileOutputStreamImplRemote,etc.
    throws IOException
  {
    if (isOpen_)
    {
      // Close the file.  Send a close request to the server.
      ClientAccessDataStream ds = null;
      IFSCloseReq req = new IFSCloseReq(fileHandle_);
      try
      {
        ds = (ClientAccessDataStream) server_.sendAndReceive(req);
      }
      catch(ConnectionDroppedException e)
      {
        Trace.log(Trace.ERROR, "Byte stream connection lost during close", e);
        connectionDropped(e);
      }
      catch(InterruptedException e)
      {
        isOpen_ = false;
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
      isOpen_ = false;
    }
  }


  /**
   Establishes communications with the AS/400.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  void connect()  // @B2A - code relocated from IFSFileOutputStreamImplRemote,etc.
    throws AS400SecurityException, IOException
  {
    // Connect to the AS/400 byte stream server if not already connected.
    if (server_ == null)
    {
      // Ensure that the system has been set.
      if (system_ == null)
      {
        throw new ExtendedIllegalStateException("system",
                                 ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }

      try {
        server_ = system_.getConnection(AS400.FILE, false);
      }
      catch(AS400SecurityException e)
      {
        Trace.log(Trace.ERROR, "Access to byte stream server on '" +
                  system_.getSystemName() + "' denied.");
        throw (AS400SecurityException)e.fillInStackTrace();
      }

      // Exchange attributes with the server.
      exchangeServerAttributes();
    }
  }

  /**
   Disconnects from the byte stream server.
   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   **/
  void connectionDropped(ConnectionDroppedException e) // @B2A - code relocated from IFSFileOutputStreamImplRemote,etc.
    throws ConnectionDroppedException
  {
    if (server_ != null)
    {
      system_.disconnectServer(server_);
      server_ = null;
      isOpen_ = false;  // Note: Not relevant for IFSFileImplRemote.
    }
    Trace.log(Trace.ERROR, "Byte stream connection lost.");
    throw (ConnectionDroppedException)e.fillInStackTrace();
  }


  /**
   Exchanges server attributes.
   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  void exchangeServerAttributes() // @B2A - code relocated from IFSFileOutputStreamImplRemote,etc.
    throws IOException
  {
      synchronized (server_)
      {
          IFSExchangeAttrRep rep =
            (IFSExchangeAttrRep)server_.getExchangeAttrReply();
          if (rep == null)
          {
              try
              {
                int requestedDataStreamLevel; // @A2A
                int[] preferredCcsids;        // @A2A
                // Note: Pre-V4R5 systems hang when presented with multiple
                // preferred CCSIDs in the exchange of attributes.   @B3A @B4C
                if (getSystemVRM() >= 0x00040500)                 // @B3A @B4C
                { // System is V4R5 or later.  We can present a list of preferred CCSIDs.

                  requestedDataStreamLevel = 2;
                  preferredCcsids = new int[] {0x34b0,0xf200}; // New or old Unicode.
                }
                else
                { // System is pre-V4R5 or later.  Exchange attr's the old way.

                  requestedDataStreamLevel = 0;
                  preferredCcsids = new int[] {0xf200}; // Old Unicode only.
                }

                // Use GMT date/time, don't use posix style return codes,
                // use PC pattern matching semantics,
                // maximum data transfer size of 0xffffffff.
                rep = (IFSExchangeAttrRep)server_.sendExchangeAttrRequest( //@B3A
                         new IFSExchangeAttrReq(true, false,
                                                IFSExchangeAttrReq.PC_PATTERN_MATCH,
                                                0xffffffff,
                                                requestedDataStreamLevel,
                                                preferredCcsids)); // @A2C
              }
              catch(ConnectionDroppedException e)
              {
                  Trace.log(Trace.ERROR, "Byte stream server connection lost");
                  connectionDropped(e);
              }
              catch(InterruptedException e)
              {
                  system_.disconnectServer(server_);
                  server_ = null;
                  Trace.log(Trace.ERROR, "Interrupted", e);
                  throw new InterruptedIOException(e.getMessage());
              }
              catch(IOException e)
              {
                  system_.disconnectServer(server_);
                  server_ = null;
                  Trace.log(Trace.ERROR, "I/O error during attribute exchange.");
                  throw (IOException)e.fillInStackTrace();
              }
          }

          // Process the exchange attributes reply.
          if (rep instanceof IFSExchangeAttrRep)
          {
              maxDataBlockSize_ = ((IFSExchangeAttrRep) rep).getMaxDataBlockSize();
              preferredServerCCSID_ = rep.getPreferredCCSID();
              serverDataStreamLevel_ = rep.getDataStreamLevel();
              setConverter(ConverterImplRemote.getConverter(preferredServerCCSID_,
                                                                system_));
              if (DEBUG) {
                System.out.println("DEBUG: IFSFileDescriptorImplRemote.exchangeServerAttributes(): " +
                                   "preferredServerCCSID_ == " + preferredServerCCSID_);
                int[] list = rep.getPreferredCCSIDs();
                for (int i=0; i<list.length; i++)
                  System.out.println("-- Server's preferred CCSID (#" + i+1 + "): " + list[i]);
                System.out.println("-- Server's dataStreamLevel : " + Integer.toHexString(serverDataStreamLevel_));
              }
          }
          else
          {
              // Should never happen.
              system_.disconnectServer(server_);
              server_ = null;
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
  void finalize0()  // @B2A
    throws IOException
  {
    if (isOpen_)
    {
      // Close the file.  Send a close request to the server.
      IFSCloseReq req = new IFSCloseReq(fileHandle_);
      try
      {
        server_.sendAndDiscardReply(req);
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
        isOpen_ = false;
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

  // Common code used by IFSFileOutputStreamImplRemote and IFSRandomAccessFileImplRemote.
  /**
   Forces any buffered output bytes to be written.
   **/
  void flush()  // @B2A - code relocated from IFSFileOutputStreamImplRemote,etc.
    throws IOException
  {
    // Request that changes be committed to disk.
    IFSCommitReq req = new IFSCommitReq(fileHandle_);
    ClientAccessDataStream ds = null;
    try
    {
      ds = (ClientAccessDataStream) server_.sendAndReceive(req);
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

  ConverterImplRemote getConverter()
  {
    return converter_;
  }

  int getFileHandle()
  {
    return fileHandle_;
  }

  public int getFileOffset()
  {
    return fileOffset_;
  }

  Object getParent()
  {
    return parent_;
  }

  String getPath()
  {
    return path_;
  }

  int getPreferredCCSID()
  {
    return preferredServerCCSID_;
  }

  AS400Server getServer()
  {
    return server_;
  }

  int getShareOption()
  {
    return shareOption_;
  }

  AS400ImplRemote getSystem()
  {
    return system_;
  }


  // Determine the system version.
  private int getSystemVRM()  // @B3A
  {
    if (!determinedSystemVRM_)
    {
      // The version number is in the high 16 bits of VRM.
      // High 16 bits represent version next 8 bits represent release,
      // low 8 bits represent modification.
      // Thus Version 4, release 5, modification level 0 is 0x00040500.
      systemVRM_ = system_.getVRM();  // @B4C
      determinedSystemVRM_ = true;
    }
    if (DEBUG) System.out.println("DEBUG IFSFileImplRemote.getSystemVRM(): " +
                                  "System VRM is 000" + Integer.toHexString(systemVRM_));
    return systemVRM_;
  }


  public void incrementFileOffset(int fileOffsetIncrement)
  {
    synchronized(fileOffsetLock_)
    {
      fileOffset_ += fileOffsetIncrement;
    }
  }

  public void initialize(int fileOffset, Object parentImpl, String path, int shareOption,
                         AS400Impl system)
  {
    fileOffset_           = fileOffset;
    parent_               = parentImpl;
    path_                 = path;
    shareOption_          = shareOption;
    system_               = (AS400ImplRemote) system;
  }

  public boolean isOpen()
  {
    return isOpen_;
  }

  boolean isOpenAllowed()
  {
    return isOpenAllowed_;
  }


  /**
   Places a lock on the file at the specified bytes.
   @param offset The first byte of the file to lock (zero is the first byte).
   @param length The number of bytes to lock.
   @return A key for undoing this lock.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

   @see IFSKey
   @see #unlock
   **/
  IFSKey lock(int length)   // @B2A
    throws IOException
  {
    return lock(fileOffset_, length);
  }
  IFSKey lock(int offset,   // @B2A - code relocated from IFSFileOutputStreamImplRemote,etc.
              int length)
    throws IOException
  {
    // Assume the arguments have been validated by the caller.

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
        new IFSLockBytesReq(fileHandle_, true, false, offset,
                            length);
      ds = (ClientAccessDataStream) server_.sendAndReceive(req);
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
      if (rc != IFSReturnCodeRep.SUCCESS)
      {
        Trace.log(Trace.ERROR, "IFSReturnCodeRep return code", rc);
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

    // Generate the key for this lock.
    IFSKey key = new IFSKey(fileHandle_, offset, length, true);

    return key;
  }

  // Common code used by IFSFileInputStreamImplRemote and IFSRandomAccessFileImplRemote.
  /**
   Reads up to <i>length</i> bytes of data from this input stream into <i>data</i>,
   starting at the array offset <i>dataOffset</i>.
   @param data The buffer into which the data is read.
   @param offset The start offset of the data in the buffer.
   @param length The maximum number of bytes to read
   @return The total number of bytes read into the buffer, or -1 if there is no more data because the end of file has been reached.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

   **/
  int read(byte[] data,  // @B2A - code relocated from IFSFileInputStreamImplRemote,etc.
           int    dataOffset,
           int    length)
    throws IOException
  {
    // Assume the arguments have been validated by the public class.

    // If length is zero then return zero.
    if (length == 0)
    {
      return 0;
    }

    // Issue the read data request.
    int bytesRead = 0;
    IFSReadReq req = new IFSReadReq(fileHandle_, fileOffset_,
                                    length);
    ClientAccessDataStream ds = null;
    try
    {
      ds = (ClientAccessDataStream) server_.sendAndReceive(req);
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

    // Receive replys until the end of chain.
    boolean done;
    do
    {
      if (ds instanceof IFSReadRep)
      {
        // Copy the data from the reply to the data parameter.
        byte[] buffer = ((IFSReadRep) ds).getData();
        if (buffer.length > 0)
        {
          System.arraycopy(buffer, 0, data, dataOffset, buffer.length);
          bytesRead += buffer.length;
          dataOffset += buffer.length;
        }
        else
        {
          bytesRead = -1;
        }
      }
      else if (ds instanceof IFSReturnCodeRep)
      {
        // Check for failure.
        int rc = ((IFSReturnCodeRep) ds).getReturnCode();
        if (rc == IFSReturnCodeRep.NO_MORE_DATA)
        {
          // End of file.
          bytesRead = -1;
        }
        else if (rc != IFSReturnCodeRep.SUCCESS)
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

      // Get the next reply if not end of chain.
      done = ((IFSDataStream) ds).isEndOfChain();
      if (!done)
      {
        try
        {
          ds = (ClientAccessDataStream)
            server_.receive(req.getCorrelation());
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
      }
    }
    while (!done);

    // Advance the file pointer.
    if (bytesRead > 0)
    {
      incrementFileOffset(bytesRead);
    }

    return bytesRead;
  }

  void setConverter(ConverterImplRemote converter)
  {
    converter_ = converter;
  }

  void setFileHandle(int fileHandle)
  {
    fileHandle_ = fileHandle;
  }

  public void setFileOffset(int fileOffset)
  {
    synchronized(fileOffsetLock_)
    {
      fileOffset_ = fileOffset;
    }
  }

  void setOpen(boolean state)
  {
    isOpen_ = state;
  }

  void setOpenAllowed(boolean state)
  {
    isOpenAllowed_ = state;
  }

  void setPreferredCCSID(int ccsid)
  {
    preferredServerCCSID_ = ccsid;
  }

  void setServer(AS400Server server)
  {
    server_ = server;
  }

  /**
   Force the system buffers to synchronize with the underlying device.
  **/                                                                      // $A1
  public void sync() throws IOException
  {
    if (parent_ == null)
    {
      Trace.log(Trace.ERROR, "IFSFileDescriptor.sync() was called when parent is null.");
    }
    // Note: UserSpaceImplRemote creates an IFSFileDescriptorImplRemote directly.
    else if (parent_ instanceof IFSRandomAccessFileImplRemote)
    {
      ((IFSRandomAccessFileImplRemote)parent_).flush();
    }
    else if (parent_ instanceof IFSFileOutputStreamImplRemote)
    {
      ((IFSFileOutputStreamImplRemote)parent_).flush();
    }
    else
    {
      Trace.log(Trace.WARNING, "IFSFileDescriptor.sync() was called " +
      "when parent is neither an IFSRandomAccessFile nor an IFSFileOutputStream.");
    }
  }


  /**
   Undoes a lock on this file.
   @param key The key for the lock.

   @exception IOException If an error occurs while communicating with the AS/400.

   @see IFSKey
   @see #lock
   **/
  void unlock(IFSKey key)  // @B2A - code relocated from IFSFileOutputStreamImplRemote,etc.
    throws IOException
  {
    // Assume the argument has been validated by the caller.

    // Verify that this key is compatible with this file.
    if (key.fileHandle_ != fileHandle_)
    {
      Trace.log(Trace.ERROR, "Attempt to use IFSKey on different file stream.");
      throw new ExtendedIllegalArgumentException("key",
                     ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Attempt to unlock the file.
    ClientAccessDataStream ds = null;
    // Issue an unlock bytes request.
    IFSUnlockBytesReq req =
      new IFSUnlockBytesReq(key.fileHandle_, key.offset_,
                            key.length_, key.isMandatory_);
    try
    {
      ds = (ClientAccessDataStream) server_.sendAndReceive(req);
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


  // Common code used by IFSFileOutputStreamImplRemote and IFSRandomAccessFileImplRemote.
  /**
   Writes <i>length</i> bytes from the byte array <i>data</i>, starting at <i>dataOffset</i>, to this File.
   @param data The data.
   @param dataOffset The start offset in the data.
   @param length The number of bytes to write.
   @parm forceToStorage Whether data must be written before the server replies.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

   **/
  void writeBytes(byte[]  data,     // @B2A
                  int     dataOffset,
                  int     length)
    throws IOException
  {
    writeBytes(data, dataOffset, length, false);
  }
  void writeBytes(byte[]  data,  // @B2A - code relocated from IFSFileOutputStreamImplRemote,etc.
                  int     dataOffset,
                  int     length,
                  boolean forceToStorage)
    throws IOException
  {
    // Assume the arguments have been validated by the caller.

    // Send write requests until all data has been written.
    while(length > 0)
    {
      // Determine how much data can be written on this request.
      int writeLength = (length > maxDataBlockSize_ ?
                         maxDataBlockSize_ : length);

      // Build the write request.  Set the chain bit if there is
      // more data to write.
      IFSWriteReq req = new IFSWriteReq(fileHandle_, fileOffset_,
                                        data, dataOffset, writeLength,
                                        0xffff, forceToStorage);
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
        ds = (ClientAccessDataStream) server_.sendAndReceive(req);
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
          Trace.log(Trace.ERROR, "IFSWriteRep return code ", rc);
          throw new ExtendedIOException(rc);
        }

        // Advance the file pointer the length of the data
        // written.
        int lengthWritten = writeLength - rep.getLengthNotWritten();
        incrementFileOffset(lengthWritten);
        dataOffset += lengthWritten;
        length -= lengthWritten;

        // Ensure that all data requested was written.
        if (lengthWritten != writeLength)
        {
          Trace.log(Trace.ERROR, "Incomplete write.  Only " +
                    Integer.toString(lengthWritten) + " bytes of a requested " +
                    Integer.toString(writeLength) + " were written.");
          throw new ExtendedIOException(ExtendedIOException.UNKNOWN_ERROR);
        }
      }
      else if (ds instanceof IFSReturnCodeRep)
      {
        int rc = ((IFSReturnCodeRep) ds).getReturnCode();
        if (rc != IFSReturnCodeRep.SUCCESS)
        {
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

}




