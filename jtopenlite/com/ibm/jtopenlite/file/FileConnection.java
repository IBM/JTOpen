///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  FileConnection.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.file;

import com.ibm.jtopenlite.*;
import java.io.*;
import java.net.*;

/**
 * Represents a TCP/IP socket connection to the System i File host server (QSERVER/QPWFSERVSO prestart jobs).
**/
public class FileConnection extends HostServerConnection //implements Connection
{
  public static final int DEFAULT_FILE_SERVER_PORT = 8473;

  private int ccsid_;
  private int datastreamLevel_;
  private int maxDataBlockSize_;

  private int correlationID_ = 1;

  private int newCorrelationID()
  {
    if (correlationID_ == 0x7FFFFFFF) correlationID_ = 0;
    return ++correlationID_;
  }

  private FileConnection(SystemInfo info, Socket socket, HostInputStream in, HostOutputStream out, int ccsid, int datastreamLevel, int maxDataBlockSize, String user, String jobName)
  {
    super(info, user, jobName, socket, in, out);
    ccsid_ = ccsid;
    datastreamLevel_ = datastreamLevel;
    maxDataBlockSize_ = maxDataBlockSize;
  }

  protected void sendEndJobRequest() throws IOException
  {
  }

  public static FileConnection getConnection(String system, String user, String password) throws IOException
  {
    SignonConnection conn = SignonConnection.getConnection(system, user, password);
    try
    {
      return getConnection(conn.getInfo(), user, password);
    }
    finally
    {
      conn.close();
    }
  }

  public static FileConnection getConnection(SystemInfo info, String user, String password) throws IOException
  {
    return getConnection(info, user, password, DEFAULT_FILE_SERVER_PORT);
  }

  public static FileConnection getConnection(SystemInfo info, String user, String password, int filePort) throws IOException
  {
    if (filePort < 0 || filePort > 65535)
    {
      throw new IOException("Bad file port: "+filePort);
    }
    FileConnection conn = null;

    Socket fileServer = new Socket(info.getSystem(), filePort);
    InputStream in = fileServer.getInputStream();
    OutputStream out = fileServer.getOutputStream();
    try
    {
      HostOutputStream dout = new HostOutputStream(new BufferedOutputStream(out));
      HostInputStream din = new HostInputStream(new BufferedInputStream(in));
      String jobName = connect(info, dout, din, 0xE002, user, password);

      sendExchangeAttributesRequest(dout);
      dout.flush();

      int length = din.readInt();
      if (length < 22)
      {
        throw DataStreamException.badLength("fileExchangeAttributes", length);
      }
      din.skipBytes(16);
      int rc = din.readShort();
      if (rc != 0)
      {
        throw DataStreamException.badReturnCode("fileExchangeAttributes", rc);
      }
      int datastreamLevel = din.readShort();
      din.skipBytes(2);
      int maxDataBlockSize = din.readInt();
      //int ccsidLL = din.readInt();
      //int ccsidCP = din.readShort();
      din.skipBytes(6);
      int ccsid = din.readShort();
      int remaining = length-38;
      din.skipBytes(remaining);

      conn = new FileConnection(info, fileServer, din, dout, ccsid, datastreamLevel, maxDataBlockSize, user, jobName);
      return conn;
    }
    finally
    {
      if (conn == null)
      {
        in.close();
        out.close();
        fileServer.close();
      }
    }
  }

  /**
   * Deletes the specified file.
  **/
  public int deleteFile(String filename) throws IOException
  {
    sendDeleteFileRequest(out_, Conv.stringToUnicodeByteArray(filename));
    out_.flush();

    int length = in_.readInt();
    if (length < 24)
    {
      throw DataStreamException.badLength("deleteFile", length);
    }
    int headerID = in_.readShort();
    int serverID = in_.readShort();
    int csInstance = in_.readInt();
    int correlationID = in_.readInt();
    int templateLength = in_.readShort();
    int reqRepID = in_.readShort();
    in_.skipBytes(2);
    if (reqRepID == 0x8001)
    {
      int rc = in_.readShort();
      int remaining = length-24;
      in_.skipBytes(remaining);
      return rc;
    }
    else
    {
      int remaining = length-22;
      in_.skipBytes(remaining);
      throw DataStreamException.badReply("deleteFile", reqRepID);
    }
  }

  /**
   * Opens a file for read-write access, a share option of SHARE_ALL, and a data CCSID of 1208 (UTF-8).
  **/
  public int openFile(String filename, FileHandle buffer) throws IOException
  {
    return openFile(filename, buffer, FileHandle.OPEN_READ_WRITE, FileHandle.SHARE_ALL, true, 1208);
  }

  public int openFile(String filename, FileHandle buffer, int openType, int shareOption, boolean createIfNotExist, int dataCCSID) throws IOException
  {
    if (buffer.isOpen())
    {
      // Someone is re-using their buffer without closing the file!
      //TODO closeFile(buffer);
    }
    sendOpenFileRequest(Conv.stringToUnicodeByteArray(filename), openType, shareOption, createIfNotExist, dataCCSID);
    out_.flush();

    int length = in_.readInt();
    if (length < 24)
    {
      throw DataStreamException.badLength("openFile", length);
    }
    int headerID = in_.readShort();
    int serverID = in_.readShort();
    int csInstance = in_.readInt();
    int correlationID = in_.readInt();
    int templateLength = in_.readShort();
    int reqRepID = in_.readShort();
    in_.skipBytes(2);
    int remaining = length-22;
    int rc = 0;
    if (reqRepID == 0x8001)
    {
      rc = in_.readShort();
      remaining -= 2;
    }
    else if (reqRepID == 0x8002)
    {
      int handle = in_.readInt();
      long id = in_.readLong();
      dataCCSID = in_.readShort();
      int actionTaken = in_.readShort();
      long createDate = convertDate(in_);
      long modifyDate = convertDate(in_);
      long accessDate = convertDate(in_);
      int fileSize = in_.readInt();
      long actualFileSize = fileSize;
      int fixedAttribs = in_.readInt();
      int needExtAttribs = in_.readShort();
      int numExtAttribs = in_.readShort();
      int charsExtAttribsNames = in_.readInt();
      int bytesExtAttribsValues = in_.readInt();
      int version = in_.readInt();
      int amountAccessed = in_.readShort();
      int accessHistory = in_.readByte();
      remaining -= 67;
      if (length >= 97)
      {
        long largeFileSize = in_.readLong();
        remaining -= 8;
        actualFileSize = largeFileSize;
      }
      buffer.setOpen(true);
      buffer.setOpenType(openType);
      buffer.setName(filename);
      buffer.setHandle(handle);
      buffer.setID(id);
      buffer.setDataCCSID(dataCCSID);
      buffer.setCreateDate(createDate);
      buffer.setModifyDate(modifyDate);
      buffer.setAccessDate(accessDate);
      buffer.setSize(actualFileSize);
      buffer.setVersion(version);
    }
    else
    {
      in_.skipBytes(remaining);
      throw DataStreamException.badReply("deleteFile", reqRepID);
    }
    in_.skipBytes(remaining-2);
    return rc;
  }

  public int closeFile(FileHandle handle) throws IOException
  {
    if (!handle.isOpen())
    {
      // Someone is trying to close an already-closed handle!
      //TODO
      return -1;
    }
    sendCloseFileRequest(handle.getHandle());
    out_.flush();

    int length = in_.readInt();
    if (length < 24)
    {
      throw DataStreamException.badLength("closeFile", length);
    }
    int headerID = in_.readShort();
    int serverID = in_.readShort();
    int csInstance = in_.readInt();
    int correlationID = in_.readInt();
    int templateLength = in_.readShort();
    int reqRepID = in_.readShort();
    in_.skipBytes(2);
    int remaining = length-22;
    if (reqRepID == 0x8001 || reqRepID == 0x8004)
    {
      int rc = in_.readShort();
      in_.skipBytes(remaining-2);
      return rc;
    }
    else
    {
      in_.skipBytes(remaining);
      throw DataStreamException.badReply("closeFile", reqRepID);
    }
  }

  public int readFile(FileHandle handle, byte[] buffer, int bufferOffset, int bufferLength) throws IOException
  {
    long currentOffset = handle.getOffset();
    sendReadRequest(handle.getHandle(), currentOffset, bufferLength);
    out_.flush();

    int length = in_.readInt();
    if (length < 24)
    {
      throw DataStreamException.badLength("readFile", length);
    }
    int headerID = in_.readShort();
    int serverID = in_.readShort();
    int csInstance = in_.readInt();
    int correlationID = in_.readInt();
    int templateLength = in_.readShort();
    int reqRepID = in_.readShort();
    int chain = in_.readShort(); //in_.skipBytes(2);
    int numRead = 22;
    int remaining = length-22;
    int rc = 0;
    int numToRead = -1;
    if (reqRepID == 0x8001)
    {
      rc = in_.readShort();
      remaining -= 2;
      numRead += 2;
    }
    else if (reqRepID == 0x8003)
    {
      int ccsid = in_.readShort();
      int dataLength = in_.readInt();
      in_.skipBytes(2);
      remaining -= 8;
      int numBytes = dataLength-6;
      numToRead = numBytes > length ? length : numBytes;
      int numToSkip = numToRead >= numBytes ? 0 : numBytes-length;
      in_.readFully(buffer, bufferOffset, numToRead);
      in_.skipBytes(numToSkip);
      handle.setOffset(currentOffset + numToRead);
      remaining -= numBytes;
      numRead += 6;
      numRead += numToRead;
      numRead += numToSkip;
    }
    else
    {
      in_.skipBytes(remaining);
      numRead += remaining;
      throw DataStreamException.badReply("readFile", reqRepID);
    }
    in_.skipBytes(remaining-2);
    handle.setLastStatus(rc);
    return numToRead;
  }

  /**
   * Writes data to the file starting at the current file offset in the handle, and optionally sync-ing the data to disk;
   * upon a successful write, the current file offset and size are incremented.
  **/
  public void writeFile(FileHandle handle, byte[] buffer, int bufferOffset, int bufferLength, boolean sync) throws IOException
  {
    long currentOffset = handle.getOffset();
    sendWriteRequest(handle.getHandle(), currentOffset, buffer, bufferOffset, bufferLength, handle.getDataCCSID(), sync);
    out_.flush();

    int length = in_.readInt();
    if (length < 24)
    {
      throw DataStreamException.badLength("writeFile", length);
    }
    int headerID = in_.readShort();
    int serverID = in_.readShort();
    int csInstance = in_.readInt();
    int correlationID = in_.readInt();
    int templateLength = in_.readShort();
    int reqRepID = in_.readShort();
    int chain = in_.readShort(); //in_.skipBytes(2);
    int numRead = 22;
    int rc = 0;
    if (reqRepID == 0x8001)
    {
      rc = in_.readShort();
      numRead += 2;
    }
    else if (reqRepID == 0x800B)
    {
      rc = in_.readShort();
      int previousFileSize = in_.readInt();
      int bytesNotWritten = in_.readInt();
      numRead += 10;
      handle.setOffset(currentOffset + (bufferLength-bytesNotWritten));
      handle.setSize(handle.getSize() + (bufferLength-bytesNotWritten));
    }
    else
    {
      in_.skipBytes(length-numRead);
      numRead = length;
      throw DataStreamException.badReply("writeFile", reqRepID);
    }
    in_.skipBytes(length-numRead);
    handle.setLastStatus(rc);
  }

  private void sendWriteRequest(int handle, long fileOffset, byte[] data, int dataOffset, int dataLength, int dataCCSID, boolean sync) throws IOException
  {
    if (datastreamLevel_ < 16 && fileOffset > 0x007FFFFFFFL)
    {
      throw new IOException("File offset too large: "+fileOffset);
    }
    final int templateLength = datastreamLevel_ < 16 ? 18 : 34;
    out_.writeInt(26+templateLength+dataLength); // Length.
    out_.writeShort(0); // Header ID.
    out_.writeShort(0xE002); // Server ID.
    out_.writeInt(0); // CS instance.
    out_.writeInt(newCorrelationID()); // Correlation ID.
    out_.writeShort(templateLength); // Template length.
    out_.writeShort(0x0004); // ReqRep ID.
    out_.writeShort(0); // Chain indicator.
    out_.writeInt(handle); // File handle.
    if (datastreamLevel_ < 16)
    {
      out_.writeInt(0); // Base offset.
      out_.writeInt((int)fileOffset); // Relative offset.
    }
    else
    {
      out_.writeInt(0); // Base offset.
      out_.writeInt(0); // Relative offset.
    }
    out_.writeShort(sync ? 3 : 2); // Data flags. 3=forceToStorage(sync),2=return immediately without sync.
    out_.writeShort(dataCCSID); // Data CCSID.

    if (datastreamLevel_ >= 16)
    {
      out_.writeLong(0); // Large base offset.
      out_.writeLong(fileOffset); // Large relative offset.
    }

    out_.writeInt(dataLength+6); // Data LL.
    out_.writeShort(0x0020); // Data CP.
    out_.write(data, dataOffset, dataLength); // Data.
  }

  private void sendReadRequest(int handle, long fileOffset, int length) throws IOException
  {
    if (datastreamLevel_ < 16 && fileOffset > 0x007FFFFFFFL)
    {
      throw new IOException("File offset too large: "+fileOffset);
    }
    final int templateLength = datastreamLevel_ < 16 ? 22 : 38;
    out_.writeInt(20+templateLength); // Length.
    out_.writeShort(0); // Header ID.
    out_.writeShort(0xE002); // Server ID.
    out_.writeInt(0); // CS instance.
    out_.writeInt(newCorrelationID()); // Correlation ID.
    out_.writeShort(templateLength); // Template length.
    out_.writeShort(0x0003); // ReqRep ID.
    out_.writeShort(0); // Chain indicator.
    out_.writeInt(handle); // File handle.
    if (datastreamLevel_ < 16)
    {
      out_.writeInt(0); // Base offset.
      out_.writeInt((int)fileOffset); // Relative offset.
    }
    else
    {
      out_.writeInt(0); // Base offset.
      out_.writeInt(0); // Relative offset.
    }
    out_.writeInt(length); // Read length.
    out_.writeInt(0); // Pre-read length.
    if (datastreamLevel_ >= 16)
    {
      out_.writeLong(0); // Large base offset.
      out_.writeLong(fileOffset); // Large relative offset.
    }
  }

  // Only used to get the owner of a file. Boooo.
  private void sendListAttributesOA1Request(int handle, int flags1, int flags2) throws IOException
  {
    out_.writeInt(54); // Length.
    out_.writeShort(0); // Header ID.
    out_.writeShort(0xE002); // Server ID.
    out_.writeInt(0); // CS instance.
    out_.writeInt(newCorrelationID()); // Correlation ID.
    out_.writeShort(20); // Template length.
    out_.writeShort(0x000A); // ReqRep ID.
    out_.writeShort(0); // Chain indicator.

    out_.writeInt(handle); // File handle.
    out_.writeShort(0); // CCSID.
    out_.writeInt(1); // Working dir handle.
    out_.writeShort(0); // Check authority: 0=no auth required, 1=read required, 2=write required, 4=exec required.
    out_.writeShort(0xFFFF); // Max get count.
    out_.writeShort(0x42); // Use OA1 with open file handle.
    out_.writeShort(0); // Pattern matching. 0=posix

    out_.writeInt(14); // OA1 flags LL.
    out_.writeShort(0x0010); // OA1 flags CP.
    out_.writeInt(flags1); // OA1 flags1.
    out_.writeInt(flags2); // OA1 flags2.
  }

  private void sendCloseFileRequest(int handle) throws IOException
  {
    out_.writeInt(41); // Length.
    out_.writeShort(0); // Header ID.
    out_.writeShort(0xE002); // Server ID.
    out_.writeInt(0); // CS instance.
    out_.writeInt(newCorrelationID()); // Correlation ID.
    out_.writeShort(21); // Template length.
    out_.writeShort(0x0009); // ReqRep ID.
    out_.writeShort(0); // Chain indicator.

    out_.writeInt(handle); // File handle.
    out_.writeShort(2); // Data flags.
    out_.writeShort(0xFFFF); // CCSID.
    out_.writeShort(100); // Amount accessed.
    out_.writeByte(0); // Access history.
    out_.writeLong(0); // Modify date.
  }

  private void sendOpenFileRequest(byte[] filenameUnicode, int openType, int share, boolean create, int dataCCSID) throws IOException
  {
    final int templateLength = datastreamLevel_ < 16 ? 36 : 44;
    out_.writeInt(26+templateLength+filenameUnicode.length); // Length.
    out_.writeShort(0); // Header ID.
    out_.writeShort(0xE002); // Server ID.
    out_.writeInt(0); // CS instance.
    out_.writeInt(newCorrelationID()); // Correlation ID.
    out_.writeShort(templateLength); // Template length.
    out_.writeShort(0x0002); // ReqRep ID.
    out_.writeShort(0); // Chain indicator.
    out_.writeShort(1200); // Filename CCSID.
    out_.writeInt(1); // Working dir handle.
    out_.writeShort(dataCCSID); // File data CCSID.
    out_.writeShort(openType); // Access intent. 1=read, 2=write, 4=program load
    out_.writeShort(share); // Share option. 0=all, 3=none, 2=read, 1=write.
    out_.writeShort(0); // Data conversion. 0=none, 1=use client CCSID, 2=use server CCSID.
    out_.writeShort(create ? 1 : 8); // Duplicate file option. 16=failIfNoExist+replaceIfExist, 8=failIfNoExist+openIfExist, 4=createOpenIfNoExist+failIfExist, 2=createOpenIfNoExist+replaceIfExist, 1=createOpenIfNoExist+openIfExist
    out_.writeInt(0); // Create size.
    out_.writeInt(0); // Fixed attributes.
    out_.writeShort(1); // Attribute list level.
    out_.writeInt(0); // Pre-read offset.
    out_.writeInt(0); // Pre-read length.
    if (datastreamLevel_ >= 16)
    {
      out_.writeLong(0); // Large create size.
    }
    out_.writeInt(filenameUnicode.length+6); // Filename LL.
    out_.writeShort(2); // Filename CP.
    out_.write(filenameUnicode);
  }

  private void sendDeleteFileRequest(HostOutputStream out, byte[] filenameUnicode) throws IOException
  {
    out.writeInt(34+filenameUnicode.length); // Length.
    out.writeShort(0); // Header ID.
    out.writeShort(0xE002); // Server ID.
    out.writeInt(0); // CS instance.
    out.writeInt(newCorrelationID()); // Correlation ID.
    out.writeShort(8); // Template length.
    out.writeShort(0x000C); // ReqRep ID.
    out.writeShort(0); // Chain indicator.
    out.writeShort(1200); // CCSID.
    out.writeInt(1); // Working dir handle.
    out.writeInt(filenameUnicode.length+6); // Filename LL.
    out.writeShort(2); // Filename CP.
    out.write(filenameUnicode);
  }

  private static void sendExchangeAttributesRequest(HostOutputStream out) throws IOException
  {
    out.writeInt(42); // Length.
    out.writeShort(0); // Header ID.
    out.writeShort(0xE002); // Server ID.
    out.writeInt(0); // CS instance.
    out.writeInt(0); // Correlation ID.
    out.writeShort(10); // Template length.
    out.writeShort(0x0016); // ReqRep ID.
    out.writeShort(0); // Chain indicator.
    out.writeShort(8); // Datastream level. V5R3 or higher.
    out.writeShort(6); // Don't use posix return codes (8), use GMT (4), use PC pattern match (2).
    out.writeInt(0xFFFFFFFF); // Max data block.
    out.writeInt(12); // CCSID LL.
    out.writeShort(10); // CCSID CP.
    out.writeShort(1200); // Preferred CCSID UTF-16.
    out.writeShort(13488); // Preferred CCSID UnicodeBig.
    out.writeShort(61952); // Preferred CCSID old IFS UnicodeBig.
  }

  private static long convertDate(HostInputStream in) throws IOException
  {
    // The IFS format is 32-bit seconds, 32-bit milliseconds.
    int seconds = in.readInt();
    int microseconds = in.readInt();
    long milliseconds = (seconds * 1000L) + (microseconds/1000);
    return milliseconds;
  }
}
