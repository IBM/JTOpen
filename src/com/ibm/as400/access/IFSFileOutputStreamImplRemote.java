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

  // Variables needed by subclass IFSTextFileOutputStream:
  //transient private OutputStreamWriter writer_;    // @B4d
  transient private ConverterImplRemote converter_;   // @B3a
  //private boolean recursing_ = false;  // @B4d

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
   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  public void close()
    throws IOException
  {
/* @B4d
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
*/
      // Close the OutputStream.
      //close0();    // @B4d
      fd_.close0();  // @B4a
/* @B4d
    }
*/
  }

/* B4d
  private void close0()
    throws IOException
  {
    fd_.close0();  // @B2C
  }
*/

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
   @exception IOException If an error occurs while communicating with the AS/400.
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
/* @B4d
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
*/
      // Flush the OutputStream.
      //flush0();  // @B4d
      open(fd_.getPreferredCCSID());  // @B4a
      fd_.flush();  // @B4a
/* @B4d
    }
*/
  }

/* @B4d
  private void flush0()
    throws IOException
  {
    // Ensure that the file is open.
    open(fd_.getPreferredCCSID());

    fd_.flush();  // @B2C
  }
*/


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

    return fd_.lock(length);  // @B2C
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
      fd_.connect();
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
    fd_ = IFSFileDescriptorImplRemote.castImplToImplRemote(fd);  // @B2C
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

    // Ensure that the file is open.
    open(fd_.getPreferredCCSID());

    fd_.unlock(key);  // @B2C
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
   <br>This method is implemented to qualify this class as an OutputStream.
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

    fd_.writeBytes(data, dataOffset, length);  // @B2C
  }


  // Used by IFSTextFileOutputStream only:
  /**
   Writes characters to this text file output stream.
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
    //if (writer_ == null && converter_ == null)   // @B3c @B4d
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
            new IFSListAttrsReq(fd_.getFileHandle(), (short)0x44);
          // Note: 0x44 indicates "Use the open instance of the file handle,
          //       and return an OA2 structure in the reply".

          ds = (ClientAccessDataStream) fd_.getServer().sendAndReceive(req);

          boolean done = false;
          boolean gotCCSID = false;
          do
          {
            if (ds instanceof IFSListAttrsRep)
            {
              if (gotCCSID)
                Trace.log(Trace.DIAGNOSTIC, "Received multiple replies " +
                          "from ListAttributes request.");
              ((IFSListAttrsRep)ds).setServerDatastreamLevel(fd_.serverDataStreamLevel_); // @B2A
              fileCCSID = ((IFSListAttrsRep) ds).getCCSID();
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

/*  @B4d

      // Convert the CCSID to the encoding string.  
//    String encoding = ConversionMaps.ccsidToEncoding(ccsid == 0xf200 ? //@D0C
//                                                       0x34b0 : ccsid);//@B3d
      String encoding = ConversionMaps.ccsidToEncoding(ccsid); // @B3c

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
//    if (encoding.equals("13488")) {  // @A1A @B3d
//      encoding = "Unicode";          // @A1A @B3d
//    }                                // @A1A @B3d
      if (ccsid == 13488 ||            // @A1A @B3c
          ccsid == 61952)              // @B3a
      {
        // Note: This is to avoid the problem of the writer getting confused about
        // a "missing byte-order mark" when dealing with Unicode streams.    @B3a
*/
        converter_ = ConverterImplRemote.getConverter(ccsid, fd_.getSystem()); //@B3a
/* @B4d
      }
      else
      {
        // Instantiate the OutputStreamWriter.
        writer_ = new OutputStreamWriter(this, encoding);
      }
*/
    }

    // Write the characters of the String.

/* @B4d
    if (writer_ != null)
    {
      writer_.write(data, 0, data.length());
    }
    else if (converter_ != null)  //@B3a
    {
*/
      this.write(converter_.stringToByteArray(data));  //@B3a
/* @B4d
    }
    else
    {
      Trace.log(Trace.DIAGNOSTIC, "Neither the writer nor the converter got set.");
    }
*/
  }

}
