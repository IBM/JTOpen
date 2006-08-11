///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileOutputStreamImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


import java.io.InterruptedIOException;
import java.io.IOException;
import java.io.OutputStream;


/**
 Provides a full remote implementation for the IFSFileOutputStream and
 IFSTextFileOutputStream classes.
 **/
class IFSFileOutputStreamImplRemote
extends OutputStream
implements IFSFileOutputStreamImpl
{
  private boolean append_ = false;
  private IFSFileDescriptorImplRemote fd_; // file info

  // Variables needed by subclass IFSTextFileOutputStream:
  transient private ConverterImplRemote converter_;   // @B3a

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

  /**
   Closes this file output stream and releases any system resources associated
   with this stream.
   @exception IOException If an error occurs while communicating with the server.
   **/
  public void close()
    throws IOException
  {
      // Close the OutputStream.
      fd_.close0();  // @B4a
  }


  public void connectAndOpen(int ccsid)
    throws AS400SecurityException, IOException
  {
    fd_.connect();
    if (ccsid == -1)
      open(fd_.getPreferredCCSID());
    else
      open(ccsid);
  }


  /**
   Ensures that the file output stream is closed when there are no more
   references to it.
   @exception IOException If an error occurs while communicating with the server.
   **/
  protected void finalize()
    throws Throwable
  {
    try
    {
      if (fd_ != null)
        fd_.finalize0();  // @B2C
    }
    catch(Throwable e)
    {
      Trace.log(Trace.ERROR, "Error during finalization.", e);
    }
    finally
    {
      super.finalize();
    }
  }

  /**
   Forces any buffered output bytes to be written.

   @exception IOException If an error occurs while communicating with the server.
   **/
  public void flush()
    throws IOException
  {
      // Flush the OutputStream.
      open(fd_.getPreferredCCSID());  // @B4a

      try {
        fd_.flush();  // @B4a
      }
      catch (AS400SecurityException e) {
        throw new IOException(e.getMessage());
      }
  }


  /**
   Places a lock on the file at the current position for the specified number
   of bytes.
   @param length The number of bytes to lock.
   @return A key for undoing this lock.

   @exception IOException If an error occurs while communicating with the server.

   @see IFSKey
   @see #unlock
   **/
  public IFSKey lock(int length)
    throws IOException
  {
    // Assume the argument has been validated by the public class.

    // Ensure that the file is open.
    open(fd_.getPreferredCCSID());

    try {
      return fd_.lock(length);  // @B2C
    }
    catch (AS400SecurityException e) {
      throw new IOException(e.getMessage());
    }
  }


  /**
   Opens the specified file.

   @exception IOException If an error occurs while communicating with the server.
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
      fd_.connect();
    }
    catch(AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, "Security exception", e);
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    // Convert the path name to the server CCSID.
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
                                    (append_ ? 1 : 2),
                                    fd_.serverDatastreamLevel_);
    ClientAccessDataStream ds = null;
    try
    {
      ds = (ClientAccessDataStream) fd_.getServer().sendAndReceive(req);
    }
    catch(ConnectionDroppedException e)
    {
      Trace.log(Trace.ERROR, "Byte stream server connection lost");
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
      if (append_)
      {
        // We must append to the file.
        fd_.setFileOffset(rep.getFileSize(fd_.serverDatastreamLevel_));        // @B7c
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
    fd_ = IFSFileDescriptorImplRemote.castImplToImplRemote(fd);  // @B2C
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
    // Assume the argument has been validated by the public class.

    // Ensure that the file is open.
    open(fd_.getPreferredCCSID());

    try {
      fd_.unlock(key);  // @B2C
    }
    catch (AS400SecurityException e) {
      throw new IOException(e.getMessage());
    }
  }


  /**
   Writes the specified byte to this file output stream.
   <br>This method is implemented to qualify this class as an OutputStream.
   @param b The byte to be written.

   @exception IOException If an error occurs while communicating with the server.
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
   <br>This method is implemented to qualify this class as an OutputStream.
   @param data The data to be written.

   @exception IOException If an error occurs while communicating with the server.
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

   @exception IOException If an error occurs while communicating with the server.
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
        fd_.connectionDropped(e);
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

        fd_.setFileOffset((int)rep.getSize(fd_.serverDatastreamLevel_));                // @B7c
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

    try {
      fd_.writeBytes(data, dataOffset, length);  // @B2C
    }
    catch (AS400SecurityException e) {
      throw new IOException(e.getMessage());
    }
  }


  // Used by IFSTextFileOutputStream.write(String) only:
  /**
   Writes characters to this text file output stream.
   The characters that are written to the file are converted to the
   specified CCSID.  
   @param data The characters to write to the stream.
   @param ccsid The CCSID for the data.

   @exception IOException If an error occurs while communicating with the server.
   **/
  public void writeText(String data, int ccsid)
    throws IOException
  {
    // Assume the arguments have been validated by the public class.

    // Ensure that the file is open.
    open(ccsid);

    // Create the OutputStreamWriter if we don't already have one.
    if (converter_ == null)   // @B4c
    {
      int fileCCSID = 0;  // @B2C - formerly codePage

      // Determine the file character encoding if the CCSID property
      // value has not been set.
      if (ccsid == -1)
      {
        // Issue a List File Attributes request to obtain the CCSID (or code page)
        // of the file.
        try
        {
          ClientAccessDataStream ds = null;

          // Issue the "list attributes" request.
          byte[] pathname = fd_.getConverter().stringToByteArray(fd_.getPath());

          IFSListAttrsReq req =
            new IFSListAttrsReq(fd_.getFileHandle(), IFSListAttrsReq.OA2, 0, 0);
          // We need to get an OA2 structure in the reply, since it contains the CCSID field.

          ds = (ClientAccessDataStream) fd_.getServer().sendAndReceive(req);

          boolean done = false;
          boolean gotCCSID = false;
          do
          {
            if (ds instanceof IFSListAttrsRep)
            {
              if (Trace.traceOn_ && gotCCSID)
                Trace.log(Trace.DIAGNOSTIC, "Received multiple replies " +
                          "from ListAttributes request.");
              fileCCSID = ((IFSListAttrsRep) ds).getCCSID(fd_.serverDatastreamLevel_);
              if (DEBUG)
                System.out.println("DEBUG: IFSFileOutputStreamImplRemote.writeText(): " +
                                   "Reported CCSID for file is " + fileCCSID);  // @B2A
              gotCCSID = true;
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
                fd_.connectionDropped(e);
              }
              catch(InterruptedException e)
              {
                Trace.log(Trace.ERROR, "Interrupted");
                throw new InterruptedIOException(e.getMessage());
              }
            }
          }
          while (!done);

          if (!gotCCSID || fileCCSID == 0)
          {
            Trace.log(Trace.ERROR, "Unable to determine CCSID of file " + fd_.path_);
            throw new ExtendedIOException(ExtendedIOException.UNKNOWN_ERROR);
          }

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

        ccsid = fileCCSID;
      }

      converter_ = ConverterImplRemote.getConverter(ccsid, fd_.getSystem()); //@B3a
    }

    // Write the characters of the String.
    this.write(converter_.stringToByteArray(data));  //@B3a
  }

}
