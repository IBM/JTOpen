///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: IFSRandomAccessFileImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.InterruptedIOException;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.net.UnknownHostException;

/**
 Provides a full remote implementation for the IFSRandomAccessFile class.
 **/
class IFSRandomAccessFileImplRemote
implements IFSRandomAccessFileImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  private int existenceOption_;
  private IFSFileDescriptorImplRemote fd_;
  private String mode_ = "";
  private boolean forceToStorage_ = false;
  transient private byte[] readCache_ = new byte[4096];
  transient private int readCacheIndex_;
  transient private int readCacheLength_;
  private static int[] twoToThe = { 1, 2, 4, 8, 16 }; // powers of 2
  transient private byte[] bytes1 = new byte[1];
  transient private byte[] bytes2 = new byte[2];
  transient private byte[] bytes4 = new byte[4];
  transient private byte[] bytes8 = new byte[8];

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
  }


  /**
   Closes this random access file stream and releases any system resources
   associated with the stream.
   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
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
   Ensures that the stream is closed when there are no more references to it.
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

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

   **/
  public void flush()
    throws IOException
  {
    // Ensure that the file is open.
    open();

    fd_.flush();  // @B2C
  }


  /**
   Returns the current offset in this file.
   @return The offset from the beginning of the file, in bytes, at which the
   next read or write occurs.
   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

   **/
  private long getFilePointer()
    throws IOException
  {
    // Ensure that the file is open.
    open();

    return (long) fd_.getFileOffset();
  }

  /**
   Returns the file length.
   @return The file length, in bytes.
   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

   **/
  public long length()
    throws IOException
  {
    // Ensure that the file is open.
    open();

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
      Trace.log(Trace.ERROR, "Byte stream server connection lost");
      fd_.setServer(null);
      throw (ConnectionDroppedException)e.fillInStackTrace();
    }
    catch(InterruptedException e)
    {
      Trace.log(Trace.ERROR, "Interrupted", e);
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
          Trace.log(Trace.ERROR, "Byte stream server connection lost");
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
      Trace.log(Trace.ERROR, "no reply available");
      throw new InternalErrorException(InternalErrorException.UNKNOWN);
    }

    return (long) reply.getSize();
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
  public IFSKey lock(int offset,
                     int length)
    throws IOException
  {
    // Assume the arguments have been validated by the public class.

    // Ensure that the file is open.
    open();

    return fd_.lock(offset,length);  // @B2C
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

    // Throw ConnectionDroppedException if attempting to open the file
    // after it has been closed.
    if (!fd_.isOpenAllowed_)
    {
      Trace.log(Trace.ERROR, "Attempting to re-open a closed stream.");
      throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_NOT_ACTIVE);
    }

    // Ensure that the path and mode have been set.
    String path = fd_.getPath();
    if (path.length() == 0)
    {
      throw new ExtendedIllegalStateException("path",
                                   ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
    if (mode_.length() == 0)
    {
      throw new ExtendedIllegalStateException("mode",
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


    // Convert the path name to the AS/400 CCSID.
    byte[] pathname = fd_.getConverter().stringToByteArray(path);

    // Determine the access intent from the mode.
    int accessIntent = 0;
    if (mode_.indexOf('r') != -1)
    {
      accessIntent |= IFSOpenReq.READ_ACCESS;
    }
    if (mode_.indexOf('w') != -1)
    {
      accessIntent |= IFSOpenReq.WRITE_ACCESS;
    }

    // Request that the file be opened.
    int preferredCCSID = fd_.getPreferredCCSID();
    IFSOpenReq req = new IFSOpenReq(pathname, preferredCCSID,
                                    preferredCCSID,
                                    accessIntent, ~fd_.getShareOption(),
                                    IFSOpenReq.NO_CONVERSION,
                                    twoToThe[existenceOption_]);
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
        Trace.log(Trace.ERROR, path + " not found.");
        throw new FileNotFoundException(path);
      }
      else if (rc == IFSReturnCodeRep.FILE_IN_USE)
      {
        Trace.log(Trace.ERROR, "IFSReturnCodeRep return code = ", rc);
        throw new ExtendedIOException(ExtendedIOException.SHARING_VIOLATION);
      }
      else
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
  }


  /**
   Reads up to <i>length</i> bytes of data from this input stream into
   <i>data</i>, starting at the array offset <i>dataOffset</i>.
   @param data The buffer into which the data is read.
   @param offset The start offset of the data in the buffer.
   @param length The maximum number of bytes to read.
   @return The total number of bytes read into the buffer, or -1 if there is no more data because the end of file has been reached.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

   **/
  private int read(byte[] data,
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
   Reads up to <i>length</i> bytes of data from this input stream into
   <i>data</i>, starting at the array offset <i>dataOffset</i>.
   <br> If readFully is true: Reads exactly <i>length</i> bytes from this
   file into the byte array; this method reads repeatedly from the file
   until all the bytes are read or an exception is thrown.
   @param data The buffer into which the data is read.
   @param offset The start offset of the data in the buffer.
   @param length The maximum number of bytes to read.
   @param readFully Whether or not to read fully.
   @return The total number of bytes read into the buffer, or -1 if there is no more data because the end of file has been reached.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

   **/
  public int read(byte[] data,
                  int    dataOffset,
                  int    length,
                  boolean readFully)
    throws IOException
  {
    // Assume the arguments have been validated by the public class.

    if (!readFully)
      return read(data, dataOffset, length);
    else
    {  // Read fully.
      int totalBytesRead = 0;
      while (totalBytesRead < length)
      {
        // Try to read all data requested.
        int bytesRead = read(data, dataOffset + totalBytesRead,
                             length - totalBytesRead);
        if (bytesRead > 0)
        {
          totalBytesRead += bytesRead;
        }

        // Verify that all requested data was read.
        if (totalBytesRead != data.length)
        {
          try
          {
            // Pause for 64 milliseconds before trying again.
            Thread.sleep(64);
          }
          catch(Exception e)
          {}
        }
      }
      return totalBytesRead;
    }
  }

  /**
   Reads the cached data.
   @return The next byte of cached data.
   **/
  private int readFromCache()
    throws IOException
  {
    int value = -1;

    // If the cache is empty, refill it.
    int bytesInCache = readCacheLength_ - readCacheIndex_;
    if (bytesInCache == 0)
    {
      // Refill the cache
      readCacheLength_ = read(readCache_, 0, readCache_.length);
      if (readCacheLength_ == -1)
      {
        // End of file.
        return -1;
      }
      readCacheIndex_ = 0;
    }

    return (int) readCache_[readCacheIndex_++];
  }

  /**
   Reads the next line of text from this file. This method successively reads
   bytes from the file until it reaches the end of a line of text.  A line of
   text is terminated by a carriage return character (\r), a newline character
   (\n), a carriage return character immediately followed by a newline
   character, or the end of the input stream. The line-terminating characters,
   if any, are included as part of the string returned.

   @return The next line of text from this file.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

   **/
  public final synchronized String readLine()
    throws IOException
  {
    StringBuffer line = new StringBuffer();

    // Initialize the cache.
    readCacheLength_ = 0;
    readCacheIndex_ = 0;

    // Read bytes until end of line is reached.
    boolean done = false;
    while(!done)
    {
      // Read the next byte from the file.
      int nextByte = readFromCache();
      if (nextByte == -1)
      {
        break;
      }

      // Append the next byte to the line.
      line.append((char) nextByte);

      // Determine if the line is terminated.
      if (nextByte == (int) '\n')
      {
        done = true;
      }
      else if (nextByte == (int) '\r')
      {
        // Check for a line feed following.
        int i = readFromCache();
        if (i == (int) '\n')
        {
          // Include the line feed too.
          line.append((char) i);
        }
        else
        {
          // No line feed so back up the cache by one.
          readCacheIndex_--;
        }
        done = true;
      }
    }

    // 'Put back' any bytes that weren't used.
    int bytesInCache = readCacheLength_ - readCacheIndex_;
    if (bytesInCache > 0)
    {
      fd_.setFileOffset((int) getFilePointer() - bytesInCache);
    }

    return (line.length() == 0 ? null : line.toString());
  }


  /**
   Reads in a string from this file. The string has been encoded using a
   modified UTF-8 format.<br>The first two bytes are read as if by
   readUnsignedShort. This values gives the number of following bytes
   that are in the encoded string (note, not the length of the
   resulting string). The following bytes are then interpreted as bytes
   encoding characters in the UTF-8 format and are converted into characters.
   @return A Unicode string.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception EOFException If the end of file has been reached.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.
   @exception UTFDataFormatException If the bytes do not represent a valid UTF-8 encoding of a Unicode string.
   **/
  public final String readUTF()
    throws IOException
  {
    // Determine the length.
    int bytesRead = read(bytes2, 0, 2);
    if (bytesRead != 2)
    {
      throw new EOFException();
    }

    int length = ((bytes2[0] & 0xff) << 8) | (bytes2[1] & 0xff);
    if (length < 0)
    {
      length += 65536;
    }

    // Read the modified UTF-8 data.
    byte[] data = new byte[length];
    if (read(data, 0, length) != length)
    {
      throw new EOFException();
    }

    // Convert the modified UTF-8 data to a String.  UTF-8 format goes as follows:
    // All characters in the range '\u0001' to '\u007f' are represented by a single byte:
    // ________________
    // | 0 | bits 0-7 |
    //
    // The null character '\u0000' and characters in the range '\u0080' to
    // '\u07FF' are represented by a pair of bytes:
    // ____________________________________________
    // | 1 | 1 | 0 | bits 6-10 | 1 | 0 | bits 0-5 |
    //
    // Characters in the range '\u0800' to '\uFFFF' are represented by three bytes:
    // _____________________________________________________________________
    // | 1 | 1 | 1 | 0 | bits 12-15 | 1 | 0 | bits 6-11 | 1 | 0 | bits 0-5 |
    //
    String s = "";
    for (int i = 0; i < length;)
    {
      // Determine if the next character is in 1, 2, or 3 byte format.
      if ((data[i] & 0x80) == 0)
      {
        // One byte format.
        s += (char) data[i];
        i++;
      }
      else if ((data[i] & 0xe0) == 0xc0)
      {
        // Two byte format.  Ensure that we have one more byte.
        if (i + 1 < length)
        {
          char c = (char) (((data[i] & 0x1f) << 6) | (data[i + 1] & 0x3f));
          s += c;
          i += 2;
        }
        else
        {
          throw new UTFDataFormatException();
        }
      }
      else if ((data[i] & 0xe0) == 0xe0)
      {
        // Three byte format. Ensure that we have two more bytes.
        if (i + 2 < length)
        {
          char c = (char) (((data[i] & 0xf) << 12) |
                           ((data[i + 1] & 0x3f) << 6) | (data[i + 2] & 0x3f));
          s += c;
          i += 3;
        }
        else
        {
          throw new UTFDataFormatException();
        }
      }
      else
      {
        throw new UTFDataFormatException();
      }
    }

    return s;
  }

  /**
   Sets the offset, from the beginning of this file, at which the next read
   or write occurs.
   @param position The absolute position of the file pointer.

   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  // Note: This method is provided for use by UserSpaceImplRemote.
  public void seek(long position)
    throws IOException
  {
    // Assume the argument has been validated.

    // Ensure that the file is open.
    open();

    fd_.setFileOffset((int) position);
  }


  /**
   Sets the existence option.
   @param existenceOption Indicates if the file should be created, opened or if the request should fail based on the existence of the file. <ul><li>FAIL_OR_CREATE Fail if exists; create if not<li>OPEN_OR_CREATE Open if exists; create if not<li>OPEN_OR_FAIL Open if exists; fail if not<li>REPLACE_OR_CREATE Replace if exists; create if not<li>REPLACE_OR_FAIL Replace if exists; fail if not</ul>
   **/
  public void setExistenceOption(int existenceOption)
  {
    // Assume the argument has been validated by the public class.

    // Ensure that existenceOption is not changed after a connection
    // is established.
    if (fd_.isOpen_)
    {
      throw new ExtendedIllegalStateException("existenceOption",
                              ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    existenceOption_ = existenceOption;
  }


  public void setFD(IFSFileDescriptorImpl fd)
  {
    // Assume the argument has been validated by the public class.

    fd_ = IFSFileDescriptorImplRemote.castImplToImplRemote(fd);  // @B2C
  }


  /**
   A package-level method that sets the forceToStorage option. When forceToStorage
   is turned on, data must be written before the server replies. Otherwise, the
   server may asynchronously write the data.

   @param forceToStorage If data must be written before the server replies
   **/
  public void setForceToStorage(boolean forceToStorage)
  {
      forceToStorage_ = forceToStorage;
  }


  /**
   Sets the access mode.
   @param mode The access mode. <ul><li>"r" read only<li>"w" write only<li>"rw" read/write</ul>.
   **/
  public void setMode(String mode)
  {
    // Assume the argument has been validated by the public class.

    // Ensure that mode is not changed after the connection is established.
    if (fd_.isOpen_)
    {
      throw new ExtendedIllegalStateException("mode",
                              ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    mode_ = mode;
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

    // Ensure that the file is open.
    open();

    fd_.unlock(key);  // @B2C
  }


  /**
   Writes <i>length</i> bytes from the byte array <i>data</i>, starting at <i>dataOffset</i>, to this File.
   @param data The data.
   @param dataOffset The start offset in the data.
   @param length The number of bytes to write.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

   **/
  public void writeBytes(byte[]  data,
                          int     dataOffset,
                          int     length)
    throws IOException
  {
    // Assume the arguments have been validated by the public class.

    // Ensure that the file is open.
    open();

    fd_.writeBytes(data, dataOffset, length, forceToStorage_);  // @B2C
  }


  /**
   Writes out a string to the file using UTF-8 encoding in a
   machine-independent manner.<br>  First, two bytes are written to the file as
   if by the writeShort method giving the number of bytes to follow. This
   value is the number of bytes actually written out, not the length of the
   string. Following the length, each character of the string is output, in
   sequence, using the UTF-8 encoding for each character.

   @param s The string to write.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

   **/
  public final void writeUTF(String s)
    throws IOException
  {
    // Assume the argument has been validated by the public class.

    // Convert the character of the String to modified UTF-8 data.
    byte[] data = new byte[s.length() * 3];
    int j = 0;
    for (int i = 0; i < s.length(); i++)
    {
      char c = s.charAt(i);
      if (c == '\u0000')
      {
        data[j] = (byte) 0xc0;
        data[j + 1] =(byte) 0x80;
        j += 2;
      }
      else if ((c >= '\u0001') && (c <= '\u007f'))
      {
        data[j] = (byte) c;
        j++;
      }
      else if ((c >= '\u0080') && (c <= '\u07ff'))
      {
        data[j] = (byte) (0xc0 | ((c & 0x7c0) >>> 6));
        data[j + 1] = (byte) (0x80 | (c & 0x3f));
        j += 2;
      }
      else
      {
        data[j] = (byte) (0xe0 | ((c & 0xf000) >>> 4));
        data[j + 1] = (byte) (0x80 | ((c & 0xfc0) >>> 6));
        data[j + 2] = (byte) (0x80 | (c & 0x3f));
        j += 3;
      }
    }

    // Write the length.
    bytes2[1] = (byte) j;
    bytes2[0] = (byte) (j >>> 8);
    writeBytes(bytes2, 0, 2);

    // Write the bytes.
    writeBytes(data, 0, j);
  }

}
