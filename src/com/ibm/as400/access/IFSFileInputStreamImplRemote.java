///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: IFSFileInputStreamImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 Provides a full remote implementation for the IFSFileInputStream and
 IFSTextFileInputStream classes.
 **/
class IFSFileInputStreamImplRemote extends InputStream
implements IFSFileInputStreamImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  private IFSFileDescriptorImplRemote fd_; // file info

  transient private InputStreamReader reader_;
                              // For use by subclass IFSTextFileInputStream only.

  // Static initialization code.
  static
  {
    // Add all byte stream reply data streams of interest to the
    // AS400 server's reply data stream hash table.
    AS400Server.addReplyStream(new IFSCloseRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSExchangeAttrRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSListAttrsRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSLockBytesRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSOpenRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSReadRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSReturnCodeRep(), AS400.FILE);
  }


  /**
   Returns the number of bytes that can be read from this file input stream.
   @return The number of bytes that can be read from this file input stream.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception FileNotFoundException If the file does not exist.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

  **/
  public int available()
    throws IOException
  {
    // Ensure that the file is open.
    open();

    // Determine the file size.
    int fileSize = getFileSize();

    return (fileSize - fd_.getFileOffset());
  }

  /**
   Closes this file input stream and releases any system resources associated
   with the stream.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   
   **/
  public void close()
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
        Trace.log(Trace.ERROR, "Byte stream connection lost during close.");
        connectionDropped(e);
      }
      catch(InterruptedException e)
      {
        // Assume that the file will be closed.  Mark this stream as
        // closed.  Rethrow this exception as InterruptedIOException.
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
   Establish communications with the AS400.

   @exception AS400SecurityException If  a security or authority error occurs.
   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

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

  public void connectAndOpen()
    throws AS400SecurityException, IOException
  {
    connect();
    open();
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
      fd_.setOpen(false);
      fd_.setServer(null);
    }

    Trace.log(Trace.ERROR, "Byte stream connection lost.");
    throw (ConnectionDroppedException)e.fillInStackTrace();
  }

  /**
   Exchanges server attributes.
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
        connectionDropped(e);
         }
         catch(InterruptedException e)
         {
        fd_.getSystem().disconnectServer(fd_.getServer());
        fd_.setServer(null);
        Trace.log(Trace.ERROR, "Interrupted", e);
        throw new InterruptedIOException(e.getMessage());
         }
         catch(IOException e)
         {
        fd_.getSystem().disconnectServer(fd_.getServer());
        fd_.setServer(null);
        Trace.log(Trace.ERROR, "I/O error during attribute exchange.");
        throw (IOException)e.fillInStackTrace();
         }
     }

          // Process the exchange attributes reply.
     if (rep instanceof IFSExchangeAttrRep)
     {
         int preferredCCSID = rep.getPreferredCCSID();
         fd_.setPreferredCCSID(preferredCCSID);
         fd_.setConverter(ConverterImplRemote.getConverter(preferredCCSID,
                        fd_.getSystem()));
     }
     else
     {
         // Should never happen.
         fd_.getSystem().disconnectServer(fd_.getServer());
         fd_.setServer(null);
         Trace.log(Trace.ERROR, "Unknown reply during attribute exchange ",
         rep.data_);
         throw new
      InternalErrorException(Integer.toHexString(rep.getReqRepID()),
                   InternalErrorException.DATA_STREAM_UNKNOWN);
     }
      }
  }


  /**
   Ensures that the file input stream is closed when there are no more
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
      Trace.log(Trace.ERROR, "Error during finalization.", e);
      throw new IOException(e.getMessage());
    }
  }


  /**
   Returns the file length (in bytes).
   @return The file length.
   **/
  private final int getFileSize()
    throws IOException
  {
    int size = 0;

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
      connectionDropped(e);
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

    if (reply == null)
    {
      // This shouldn't happen.  Since there is no reply to return we
      // throw an exception.
      Trace.log(Trace.ERROR, "No reply available.");
      throw new InternalErrorException(InternalErrorException.UNKNOWN);
    }
    size = reply.getSize();

    return size;
  }


  /**
   Places a lock on the file at the current position for the specified
   number of bytes.
   @param length The number of bytes to lock.
   @return The key for undoing this lock.
 
   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

   @see IFSKey
   @see #unlock
   **/
  public IFSKey lock(int length)
    throws IOException
  {
    // Assume the argument has been validated by the public class.

    // Ensure that the file is open.
    open();

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
      Trace.log(Trace.ERROR, "IFSReturnCodeRep return code", rc);
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
      connect();
    }
    catch(AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, "Access to byte stream server on '" +
                fd_.getSystem().getSystemName() + "' denied.", e);
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    // Convert the path name to the AS/400 CCSID.
    byte[] pathname = fd_.getConverter().stringToByteArray(path);

    // Request that the file be opened.
    int preferredCCSID = fd_.getPreferredCCSID();
    IFSOpenReq req = new IFSOpenReq(pathname, preferredCCSID,
                                    preferredCCSID,
                                    IFSOpenReq.READ_ACCESS,
                                    ~fd_.getShareOption(),
                                    IFSOpenReq.NO_CONVERSION, 8);
    ClientAccessDataStream ds = null;
    try
    {
      ds = (ClientAccessDataStream) fd_.getServer().sendAndReceive(req);
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

    // Verify that the open request was successful.
    if (ds instanceof IFSOpenRep)
    {
      // Get the file information.
      IFSOpenRep rep = (IFSOpenRep) ds;
      fd_.setFileHandle(rep.getFileHandle());
      fd_.setOpen(true);
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
   @return The next byte of data, or -1 if the end of file is reached.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

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
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

   **/
  public int read(byte[] data,
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

    // Ensure that the file is open.
    open();

    // Issue the read data request.
    int bytesRead = 0;
    IFSReadReq req = new IFSReadReq(fd_.getFileHandle(), fd_.getFileOffset(),
                                    length);
    ClientAccessDataStream ds = null;
    try
    {
      ds = (ClientAccessDataStream) fd_.getServer().sendAndReceive(req);
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

    // Receive replys until the end of chain.
    boolean done;
    AS400Server server = fd_.getServer();
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
            server.receive(req.getCorrelation());
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
      }
    }
    while (!done);

    // Advance the file pointer.
    if (bytesRead > 0)
    {
      fd_.incrementFileOffset(bytesRead);
    }

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
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.
   @exception UnsupportedEncodingException If the file's character encoding is not supported.
   **/
  public String readText(int length)
    throws IOException
  {
    // Assume the argument has been validated by the public class.

    String data = "";

    // Ensure that the file is open.
    open();

    // Create the InputStreamReader if we don't already have one.
    if (reader_ == null)
    {
      // Determine the file character encoding.  Issue a List Attributes request
      // to obtain the OA2 structure, which contains the code page.
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
      int ccsid = codePage;

      // Convert the CCSID to the encoding string.  
      String encoding = ConversionMaps.ccsidToEncoding(ccsid == 0xf200 ?  //@B0C
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
        encoding = "Unicode";          // @A1A
      }                                // @A1A

      // Instantiate the InputStreamReader.
      reader_ = new InputStreamReader(this, encoding);
    }

    // Read the requested number of characters.
    char[] buffer = new char[length];
    length = reader_.read(buffer, 0, length);

    // Create a String from the characters.
    if (length > -1)
    {
      data = new String(buffer, 0, length);
    }

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
   Skips over the next <i>bytesToSkip</i> bytes in the file input stream.
   This method may skip less bytes than specified if the end of file is
   reached.  The actual number of bytes skipped is returned.

   @param bytesToSkip The number of bytes to skip.
   @return The actual number of bytes skipped.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.
  
   **/
  public long skip(long bytesToSkip)
    throws IOException
  {
    // Assume the argument has been validated by the public class.

    // Ensure that the file is open.
    open();

    long bytesSkipped;
    int available = available();
    if (bytesToSkip > available)
    {
      // Skip to the end of file.
      bytesSkipped = (long) available;
      fd_.incrementFileOffset(available);
    }
    else
    {
      // Skip ahead the specified number of bytes.
      fd_.incrementFileOffset((int) bytesToSkip);
      bytesSkipped = bytesToSkip;
    }

    return bytesSkipped;
  }

  /**
   Undoes a lock on this file.
   @param key The key for the lock.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.
 
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
    open();

    // Attempt to unlock the file.
    ClientAccessDataStream ds = null;
    IFSUnlockBytesReq req =
      new IFSUnlockBytesReq(key.fileHandle_, key.offset_,
                            key.length_, key.isMandatory_);
    try
    {
      ds = (ClientAccessDataStream) fd_.getServer().sendAndReceive(req);
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

}
