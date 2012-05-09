///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DDMConnection.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ddm;

import com.ibm.jtopenlite.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Represents a TCP/IP socket connection to the System i Distributed Data Management (DDM) host server (QUSRWRK/QRWTSRVR job).
**/
public class DDMConnection extends HostServerConnection
{
  private static final boolean DEBUG = false;

  public static final int DEFAULT_DDM_SERVER_PORT = 446;

  private final byte[] messageBuf_ = new byte[1024];
  private final char[] charBuffer_ = new char[1024];

  private int dclNamCounter_ = 0;
  private int correlationID_ = 1;

  private int newCorrelationID()
  {
    if (correlationID_ == 0x7FFF) correlationID_ = 0;
    return ++correlationID_;
  }

  private DDMConnection(SystemInfo info, Socket socket, HostInputStream in, HostOutputStream out, String user, String jobString)
  {
    super(info, user, jobString, socket, in, out);
  }

  /**
   * No-op. The DDM host server does not use an end job datastream.
  **/
  protected void sendEndJobRequest() throws IOException
  {
  }

  /**
   * Establishes a new socket connection to the specified system and authenticates the specified user and password.
  **/
  public static DDMConnection getConnection(String system, String user, String password) throws IOException
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

  /**
   * Establishes a new socket connection to the specified system and authenticates the specified user and password.
  **/
  public static DDMConnection getConnection(SystemInfo info, String user, String password) throws IOException
  {
    return getConnection(info, user, password, DEFAULT_DDM_SERVER_PORT);
  }

  /**
   * Establishes a new socket connection to the specified system and port, and authenticates the specified user and password.
  **/
  public static DDMConnection getConnection(SystemInfo info, String user, String password, int ddmPort) throws IOException
  {
    if (ddmPort < 0 || ddmPort > 65535)
    {
      throw new IOException("Bad DDM port: "+ddmPort);
    }
    DDMConnection conn = null;

    Socket ddmServer = new Socket(info.getSystem(), ddmPort);
    InputStream in = ddmServer.getInputStream();
    OutputStream out = ddmServer.getOutputStream();
    try
    {
      // Exchange random seeds.
      HostOutputStream dout = new HostOutputStream(new BufferedOutputStream(out, 1024));
      sendEXCSATRequest(dout);
      dout.flush();

      HostInputStream din = new HostInputStream(new BufferedInputStream(in, 32768));
      int length = din.readShort();
      if (length < 10)
      {
        throw DataStreamException.badLength("ddmExchangeServerAttributes", length);
      }
      int gdsID = din.read();
      int typeCorrelationChainedContinue = din.read(); // bit mask
      int correlation = din.readShort();
      int excSATLength = din.readShort(); // LL.
      int codepoint = din.readShort(); // CP.
      if (codepoint != 0x1443) // EXCSATRD
      {
        throw DataStreamException.badReply("ddmExchangeServerAttributes", codepoint);
      }
      int numRead = 10;
      byte[] extNam = null; // Job string.
      while (length-numRead > 4 && extNam == null)
      {
        int extNamLL = din.readShort();
        int extNamCP = din.readShort();
        numRead += 4;
        if (extNamCP == 0x115E) // EXTNAM
        {
          extNam = new byte[extNamLL-4];
          din.readFully(extNam);
          numRead += extNam.length;
        }
        else
        {
          din.skipBytes(extNamLL-4);
          numRead += (extNamLL-4);
        }
      }
      din.skipBytes(length-numRead);
      din.end();

      final String jobString = extNam != null ? Conv.ebcdicByteArrayToString(extNam, 0, extNam.length) : null;

      long seed = sendACCSECRequest(dout);
      byte[] clientSeed = Conv.longToByteArray(seed);
      dout.flush();

      length = din.readShort();
      if (length < 28)
      {
        throw DataStreamException.badLength("ddmAccessMethodExchange", length);
      }
      gdsID = din.read();
      typeCorrelationChainedContinue = din.read();
      correlation = din.readShort();
      //length = din.readShort(); // LL.
      din.skipBytes(2);
      codepoint = din.readShort(); // CP.
      if (codepoint != 0x14AC) // ACCSECRD
      {
        throw DataStreamException.badReply("ddmAccessMethodExchange", codepoint);
      }
      din.skipBytes(10);
      byte[] serverSeed = new byte[8];
      din.readFully(serverSeed);
      din.skipBytes(length-28);
      din.end();

      byte[] userBytes = getUserBytes(user);
      byte[] passwordBytes = getPasswordBytes(password);
      password = null;
      byte[] encryptedPassword = getEncryptedPassword(userBytes, passwordBytes, clientSeed, serverSeed, info.getPasswordLevel());

      sendSECCHKRequest(dout, userBytes, encryptedPassword);
      dout.flush();

      length = din.readShort();
      if (length < 21)
      {
        throw DataStreamException.badLength("ddmSecurityCheck", length);
      }
      gdsID = din.read();
      typeCorrelationChainedContinue = din.read();
      correlation = din.readShort();
      //length = din.readShort(); // LL.
      din.skipBytes(2);
      codepoint = din.readShort(); // CP.
      if (codepoint != 0x1219) // SECCHKRD
      {
        throw DataStreamException.badReply("ddmSecurityCheckSECCHKRD", codepoint);
      }
      din.skipBytes(8);
      codepoint = din.readShort();
      if (codepoint != 0x11A4) // SECCHKCD.
      {
        throw DataStreamException.badReply("ddmSecurityCheckSECCHKCD", codepoint);
      }
      int rc = din.read();
      if (rc != 0)
      {
        throw DataStreamException.badReturnCode("ddmSecurityCheck", rc);
      }
      din.skipBytes(length-21);
      din.end();

      conn = new DDMConnection(info, ddmServer, din, dout, user, jobString);
      return conn;
    }
    finally
    {
      if (conn == null)
      {
        in.close();
        out.close();
        ddmServer.close();
      }
    }
  }

  // Copied from SystemInfo.
  private static byte[] getUserBytes(String user) throws IOException
  {
    if (user.length() > 10)
    {
      throw new IOException("User too long");
    }
    return Conv.blankPadEBCDIC10(user.toUpperCase());
  }

  // Copied from SystemInfo.
  private static byte[] getPasswordBytes(String password) throws IOException
  {
    // Prepend a Q to numeric password.
    if (password.length() > 0 && Character.isDigit(password.charAt(0)))
    {
      password = "Q"+password;
    }
    if (password.length() > 10)
    {
      throw new IOException("Password too long");
    }
    return Conv.blankPadEBCDIC10(password.toUpperCase());
  }

  /**
   * Closes the specified file and returns any messages that were issued.
  **/
  public Message[] close(DDMFile file) throws IOException
  {
    byte[] dclNam = file.getDCLNAM();
    sendS38CloseRequest(out_, dclNam);
    out_.flush();

    int length = in_.readShort();
    if (length < 10)
    {
      throw DataStreamException.badLength("ddmS38Close", length);
    }
    int gdsID = in_.read();
    int typeCorrelationChainedContinue = in_.read(); // bit mask
    int correlation = in_.readShort();
    int numRead = 8;
    Vector messages = new Vector();
    int[] msgNumRead = new int[1];
    while (numRead+4 < length)
    {
      int ll = in_.readShort();
      int cp = in_.readShort();
      numRead += 4;
      if (cp == 0xD201) // S38MSGRM
      {
        Message msg = getMessage(in_, ll, msgNumRead);
        numRead += msgNumRead[0];
        if (msg != null)
        {
          messages.addElement(msg);
        }
      }
      else
      {
        in_.skipBytes(ll-4);
        numRead += ll-4;
      }
    }
    in_.skipBytes(length-numRead);
    in_.end();
    Message[] msgs = new Message[messages.size()];
    messages.copyInto(msgs);
    return msgs;
  }

  private Message getMessage(HostInputStream din, final int ll, int[] saved) throws IOException
  {
    int severity = -1;
    String messageID = null;
    String messageText = null;
    byte[] b = null;
    char[] c = null;
    int msgNumRead = 0;
    while (msgNumRead < ll-4)
    {
      int msgLength = din.readShort();
      int msgCodepoint = din.readShort();
      switch (msgCodepoint)
      {
        case 0x1149: // SVRCOD
          severity = din.readShort();
          din.skipBytes(msgLength-6);
          break;
        case 0xD112: // S38MID
          final boolean tooLong4 = msgLength > 1028;
          final int msgLength4 = msgLength-4;
          b = tooLong4 ? new byte[msgLength4] : messageBuf_;
          c = tooLong4 ? new char[msgLength4] : charBuffer_;
          din.readFully(b, 0, msgLength4);
          messageID = Conv.ebcdicByteArrayToString(b, 0, msgLength4, c);
          break;
        case 0xD116: // S38MTEXT
          final boolean tooLong6 = msgLength > 1030;
          final int msgLength6 = msgLength-6;
          b = tooLong6 ? new byte[msgLength6] : messageBuf_;
          c = tooLong6 ? new char[msgLength6] : charBuffer_;
          din.skipBytes(2);
          din.readFully(b, 0, msgLength6);
          messageText = Conv.ebcdicByteArrayToString(b, 0, msgLength6, c);
          break;
        default:
          din.skipBytes(msgLength-4);
          break;
      }
      msgNumRead += msgLength;
    }
    saved[0] = msgNumRead;
    return messageID == null ? null : new Message(messageID, messageText, severity);
  }

  /**
   * Opens the specified file for sequential read-only access and a preferred batch size of 100.
   * @param library The name of the library in which the file resides. For example, "QSYS".
   * @param file The name of the physical or logical file.
   * @param member The member within the file. This can be a special value such as "*FIRST".
   * @param recordFormat The name of the record format. This value can also be obtained from {@link DDMRecordFormat#getName DDMRecordFormat.getName()}.
  **/
  public DDMFile open(String library, String file, String member, String recordFormat) throws IOException
  {
    return open(library, file, member, recordFormat, DDMFile.READ_ONLY, false, 100, 1);
  }

  /**
   * Opens the specified file for reading or writing records.
   * @param library The name of the library in which the file resides. For example, "QSYS".
   * @param file The name of the physical or logical file.
   * @param member The member within the file. This can be a special value such as "*FIRST".
   * @param recordFormat The name of the record format. This value can also be obtained from {@link DDMRecordFormat#getName DDMRecordFormat.getName()}.
   * @param readWriteType The read-write access type to use. Allowed values are:
   * <ul>
   * <li>{@link DDMFile#READ_ONLY DDMFile.READ_ONLY}</li>
   * <li>{@link DDMFile#READ_WRITE DDMFile.READ_WRITE}</li>
   * <li>{@link DDMFile#WRITE_ONLY DDMFile.WRITE_ONLY}</li>
   * </ul>
   * @param keyed Indicates if the file should be opened for sequential or keyed access.
   * @param preferredBatchSize The number of records to read or write at a time. This is a preferred number because the DDMConnection
   * may decide to use a different batch size depending on circumstances (e.g. READ_WRITE files always use a batch size of 1).
   * @param numBuffers The number of data buffers to allocate for use when reading new records. The DDMConnection will round-robin
   * through the data buffers as it calls {@link DDMReadCallback#newRecord DDMReadCallback.newRecord()}. This can be useful
   * for multi-threaded readers that process new record data, such as {@link DDMThreadedReader DDMThreadedReader}. In such cases,
   * each processing thread could be assigned a specific data buffer, to avoid contention. See {@link DDMDataBuffer DDMDataBuffer}.
  **/
  public DDMFile open(String library, String file, String member, String recordFormat, int readWriteType, boolean keyed, int preferredBatchSize, int numBuffers) throws IOException
  {
    final boolean doWrite = readWriteType == DDMFile.READ_WRITE || readWriteType == DDMFile.WRITE_ONLY;
    final boolean doRead = readWriteType == DDMFile.READ_WRITE || readWriteType == DDMFile.READ_ONLY || !doWrite;
    if ((doWrite && doRead) || preferredBatchSize <= 0) preferredBatchSize = 1;
    preferredBatchSize = preferredBatchSize & 0x7FFF;
    final byte[] dclNam = generateDCLNAM();
    sendS38OpenRequest(out_, file, library, member, doRead, doWrite, keyed, recordFormat, dclNam, preferredBatchSize);
    out_.flush();

    int length = in_.readShort();
    if (length < 10)
    {
      throw DataStreamException.badLength("ddmS38Open", length);
    }
    int gdsID = in_.read();
    int type = in_.read(); // bit mask
    boolean isChained = (type & 0x40) != 0;
    int correlation = in_.readShort();
    int ll = in_.readShort();
    int cp = in_.readShort();
    int numRead = 10;
    if (DEBUG) System.out.println("open: "+gdsID+","+type+","+isChained+","+correlation+","+ll+","+cp);
    while (cp != 0xD404 && numRead+4 <= length)
    {
      if (cp == 0xD201) // S38MSGRM
      {
        int[] numMsgRead = new int[1];
        Message msg = getMessage(in_, ll, numMsgRead);
        numRead += numMsgRead[0];
        if (msg != null)
        {
          throw DataStreamException.errorMessage("ddmS38Open", msg);
        }
      }
      else
      {
        if (DEBUG) System.out.println("Got cp "+Integer.toHexString(cp));
        in_.skipBytes(ll-4);
        numRead += (ll-4);
      }
      ll = in_.readShort();
      cp = in_.readShort();
      numRead += 4;
    }
    if (cp != 0xD404) // S38OPNFB
    {
      throw DataStreamException.badReply("ddmS38Open", cp);
    }
    int openType = in_.readByte();
    final byte[] b = new byte[10];
    final char[] c = new char[10];
    in_.readFully(b);
    String realFile = Conv.ebcdicByteArrayToString(b, 0, b.length, c);
    in_.readFully(b);
    String realLibrary = Conv.ebcdicByteArrayToString(b, 0, b.length, c);
    in_.readFully(b);
    String realMember = Conv.ebcdicByteArrayToString(b, 0, b.length, c);
    int recordLength = in_.readShort();
    in_.skipBytes(10);
    int numRecordsReturned = in_.readInt();
    in_.readFully(b, 0, 2);
    String accessType = Conv.ebcdicByteArrayToString(b, 0, 2, c);
    boolean supportDuplicateKeys = in_.readByte() == 0x00C4;
    boolean isSourceFile = in_.readByte() == 0x00E8;
    in_.skipBytes(10); // UFCB parameters
    int maxBlockedRecordsTransferred = in_.readShort();
    int recordIncrement = in_.readShort();
    int openFlags1 = in_.readByte();
    in_.skipBytes(6); // Number of associated physical file members, and other stuff?
    int maxRecordLength = in_.readShort();
    int recordWaitTime = in_.readInt();
    int openFlags2 = in_.readShort();
    int nullFieldByteMapOffset = in_.readShort();
    int nullKeyFieldByteMapOffset = in_.readShort();
    in_.skipBytes(4); // Other stuff?
    int ccsid = in_.readShort();
    int totalFixedFieldLength = in_.readShort();
    numRead += 92;
    in_.skipBytes(length-numRead); // Min record length and other stuff?

    while (isChained)
    {
      // Skip the rest.
      length = in_.readShort();
      if (length < 10)
      {
        throw DataStreamException.badLength("ddmS38Open", length);
      }
      gdsID = in_.read();
      type = in_.read(); // bit mask
      isChained = (type & 0x40) != 0;
      correlation = in_.readShort();
      numRead = 6;
      in_.skipBytes(length-6);
    }
    in_.end();

    byte[] recordFormatName = Conv.blankPadEBCDIC10(recordFormat);
    return new DDMFile(realLibrary, realFile, realMember, recordFormatName, dclNam, readWriteType, recordLength, recordIncrement, preferredBatchSize, nullFieldByteMapOffset, numBuffers);
  }

  /**
   * Retrieves the record format of the specified file. This currently only retrieves the first record format in a multi-format file.
  **/
  public DDMRecordFormat getRecordFormat(final String library, final String file) throws IOException
  {
    DDMFile f = new DDMFile(library, file, null, null, null, DDMFile.READ_ONLY, 0, 0, 0, 0, 1);
    return getRecordFormat(f);
  }

  /**
   * Retrieves the record format of the specified file. This currently only retrieves the first record format in a multi-format file.
  **/
  public DDMRecordFormat getRecordFormat(DDMFile file) throws IOException
  {
    Message[] messages = execute("DSPFFD FILE("+file.getLibrary().trim()+"/"+file.getFile().trim()+") OUTPUT(*OUTFILE) OUTFILE(QTEMP/TB2FFD)");
    if (messages.length == 0)
    {
      throw new IOException("DSPFFD failed to return success message");
    }
    boolean error = false;
    for (int i=0; !error && i<messages.length; ++i)
    {
      if (DEBUG) System.out.println(messages[i]);
      if (!messages[i].getID().equals("CPF9861") && // Output file created.
          !messages[i].getID().equals("CPF9862") && // Member added to output file.
          !messages[i].getID().equals("CPF3030")) // Records added to member.
      {
        error = true;
      }
    }
    if (error)
    {
      DataStreamException dse = DataStreamException.errorMessage("DSPFFD", messages[0]);
      for (int i=1; i<messages.length; ++i)
      {
        dse.addMessage(messages[i]);
      }
      throw dse;
    }
    if (DEBUG) System.out.println("Opening file...");
    final DDMFile temp = open("QTEMP", "TB2FFD", "TB2FFD", "QWHDRFFD", DDMFile.READ_ONLY, false, 100, 1);
    final DDMRecordFormatReader reader = new DDMRecordFormatReader(getInfo().getServerCCSID());
    while (!reader.eof())
    {
      readNext(temp, reader);
    }
    close(temp);
    final DDMRecordFormat rf = new DDMRecordFormat(reader.getLibrary(),
                                                   reader.getFile(),
                                                   reader.getName(),
                                                   reader.getType(),
                                                   reader.getText(),
                                                   reader.getFields(),
                                                   reader.getLength());

    messages = execute("DLTF FILE("+temp.getLibrary().trim()+"/"+temp.getFile().trim()+")");
    error = false;
    for (int i=0; !error && i<messages.length; ++i)
    {
      if (DEBUG) System.out.println(messages[i]);
      if (!messages[i].getID().equals("CPC2191")) // Object deleted.
      {
        error = true;
      }
    }
    if (error)
    {
      DataStreamException dse = DataStreamException.errorMessage("DLTF", messages[0]);
      for (int i=1; i<messages.length; ++i)
      {
        dse.addMessage(messages[i]);
      }
      throw dse;
    }

    return rf;
  }

  /**
   * Reads the record with the specified record number from the file.
  **/
  public void read(DDMFile file, DDMReadCallback listener, int recordNumber) throws IOException
  {
    sendS38GetDRequest(out_, file.getDCLNAM(), file.getReadWriteType() == DDMFile.READ_WRITE, file.getRecordFormatName(), recordNumber, false);
    out_.flush();

    handleReply(file, "ddmS38GetD", listener);
  }

  /**
   * Reads the next record whose key is equal to the specified key.
  **/
  public void readKeyEqual(DDMFile file, DDMReadCallback listener, byte[] key, int numberOfKeyFields) throws IOException
  {
    readTypeKey(file, listener, key, numberOfKeyFields, 0x0B, false);
  }

  /**
   * Reads the next record whose key is greater than the specified key.
  **/
  public void readKeyGreater(DDMFile file, DDMReadCallback listener, byte[] key, int numberOfKeyFields) throws IOException
  {
    readTypeKey(file, listener, key, numberOfKeyFields, 0x0D, false);
  }

  /**
   * Reads the next record whose key is greater than or equal to the specified key.
  **/
  public void readKeyGreaterOrEqual(DDMFile file, DDMReadCallback listener, byte[] key, int numberOfKeyFields) throws IOException
  {
    readTypeKey(file, listener, key, numberOfKeyFields, 0x0C, false);
  }

  /**
   * Reads the next record whose key is less than the specified key.
  **/
  public void readKeyLess(DDMFile file, DDMReadCallback listener, byte[] key, int numberOfKeyFields) throws IOException
  {
    readTypeKey(file, listener, key, numberOfKeyFields, 0x09, false);
  }

  /**
   * Reads the next record whose key is less than or equal to the specified key.
  **/
  public void readKeyLessOrEqual(DDMFile file, DDMReadCallback listener, byte[] key, int numberOfKeyFields) throws IOException
  {
    readTypeKey(file, listener, key, numberOfKeyFields, 0x0A, false);
  }

  /**
   * Positions the file cursor to the record whose key is equal to the specified key.
  **/
  public void positionToKeyEqual(DDMFile file, DDMReadCallback listener, byte[] key, int numberOfKeyFields) throws IOException
  {
    readTypeKey(file, listener, key, numberOfKeyFields, 0x0B, true);
  }

  /**
   * Positions the file cursor to the record whose key is greater than the specified key.
  **/
  public void positionToKeyGreater(DDMFile file, DDMReadCallback listener, byte[] key, int numberOfKeyFields) throws IOException
  {
    readTypeKey(file, listener, key, numberOfKeyFields, 0x0D, true);
  }

  /**
   * Positions the file cursor to the record whose key is greater than or equal to the specified key.
  **/
  public void positionToKeyGreaterOrEqual(DDMFile file, DDMReadCallback listener, byte[] key, int numberOfKeyFields) throws IOException
  {
    readTypeKey(file, listener, key, numberOfKeyFields, 0x0C, true);
  }

  /**
   * Positions the file cursor to the record whose key is less than the specified key.
  **/
  public void positionToKeyLess(DDMFile file, DDMReadCallback listener, byte[] key, int numberOfKeyFields) throws IOException
  {
    readTypeKey(file, listener, key, numberOfKeyFields, 0x09, true);
  }

  /**
   * Positions the file cursor to the record whose key is less than or equal to the specified key.
  **/
  public void positionToKeyLessOrEqual(DDMFile file, DDMReadCallback listener, byte[] key, int numberOfKeyFields) throws IOException
  {
    readTypeKey(file, listener, key, numberOfKeyFields, 0x0A, true);
  }

  private void readTypeKey(DDMFile file, DDMReadCallback listener, byte[] key, int numberOfKeyFields, int readType, boolean doPosition) throws IOException
  {
    sendS38GetKRequest(out_, file.getDCLNAM(), file.getReadWriteType() == DDMFile.READ_WRITE, file.getRecordFormatName(), readType, key, numberOfKeyFields, doPosition);
    out_.flush();

    handleReply(file, "ddmS38GetK", listener);
  }


  /**
   * Reads the next record from the specified file and positions the cursor on or after it.
  **/
  public void readNext(DDMFile file, DDMReadCallback listener) throws IOException
  {
    readType(file, listener, 3, false);
  }

  /**
   * Reads the previous record from the specified file and positions the cursor on or before it.
  **/
  public void readPrevious(DDMFile file, DDMReadCallback listener) throws IOException
  {
    readType(file, listener, 4, false);
  }

  /**
   * Reads the first record from the specified file and positions the cursor on or after it.
  **/
  public void readFirst(DDMFile file, DDMReadCallback listener) throws IOException
  {
    readType(file, listener, 1, false);
  }

  /**
   * Reads the last record from the specified file and positions the cursor on or after it.
  **/
  public void readLast(DDMFile file, DDMReadCallback listener) throws IOException
  {
    readType(file, listener, 2, false);
  }

  /**
   * Reads the current record from the specified file and positions the cursor on or after it.
  **/
  public void readCurrent(DDMFile file, DDMReadCallback listener) throws IOException
  {
    readType(file, listener, 33, false);
  }

  /**
   * Positions the cursor to the next record in the file.
  **/
  public void positionToNext(DDMFile file, DDMReadCallback listener) throws IOException
  {
    readType(file, listener, 3, true);
  }

  /**
   * Positions the cursor to the previous record in the file.
  **/
  public void positionToPrevious(DDMFile file, DDMReadCallback listener) throws IOException
  {
    readType(file, listener, 4, true);
  }

  /**
   * Positions the cursor to the first record in the file.
  **/
  public void positionToFirst(DDMFile file) throws IOException
  {
    readType(file, null, 1, true);
  }

  /**
   * Positions the cursor to the last record in the file.
  **/
  public void positionToLast(DDMFile file) throws IOException
  {
    readType(file, null, 2, true);
  }

  /**
   * Positions the cursor to the end of the file (after the last record).
  **/
  public void positionAfterLast(DDMFile file) throws IOException
  {
    position(file, 2);
  }

  /**
   * Positions the cursor to the beginning of the file (before the first record).
  **/
  public void positionBeforeFirst(DDMFile file) throws IOException
  {
    position(file, 1);
  }

  /**
   * Positions the cursor to the specified record number.
  **/
  public void position(DDMFile file, DDMReadCallback listener, int recordNumber) throws IOException
  {
    sendS38GetDRequest(out_, file.getDCLNAM(), file.getReadWriteType() == DDMFile.READ_WRITE, file.getRecordFormatName(), recordNumber, true);
    out_.flush();

    handleReply(file, "ddmS38GetD", listener);
  }

  /**
   * Writes a single record to the end of the file.
  **/
  public void write(DDMFile file, byte[] data, int offset, boolean[] nullFieldValues, DDMReadCallback listener) throws IOException
  {
    int id = newCorrelationID();
    sendS38PUTMRequest(out_, file.getDCLNAM(), id);
    file.getEventBuffer().setEventType(DDMCallbackEvent.EVENT_WRITE);
    sendS38BUFRequest(out_, id, file.getRecordIncrement(), data, offset, file.getRecordLength(), nullFieldValues);
    out_.flush();

    handleReply(file, "ddmS38PUTM", listener);
  }

  /**
   * Writes records provided by the callback to the file.
  **/
  public void write(DDMFile file, DDMWriteCallback listener) throws IOException
  {
    final DDMCallbackEvent event = file.getEventBuffer();
    event.setEventType(DDMCallbackEvent.EVENT_WRITE);

    int blockingFactor = file.getBatchSize();
    int numRecords = listener.getNumberOfRecords(event);
    int startingRecordNumber = 0;
    int batchSize = numRecords > blockingFactor ? blockingFactor : numRecords;
    int id = newCorrelationID();
    while (startingRecordNumber < numRecords)
    {
      if (startingRecordNumber+batchSize >= numRecords) batchSize = numRecords-startingRecordNumber;
      sendS38PUTMRequest(out_, file.getDCLNAM(), id);
      sendS38BUFRequest(file, out_, id, file.getRecordIncrement(), listener, file.getRecordLength(), startingRecordNumber, batchSize);
      out_.flush();

      handleReply(file, "ddmS38PUTM", null);
      startingRecordNumber += batchSize;
    }
  }

  /**
   * Updates the current record with the specified data.
  **/
  public void updateCurrent(DDMFile file, byte[] data, int offset, boolean[] nullFieldValues) throws IOException
  {
    final int id = sendS38UPDATRequest(out_, file.getDCLNAM());
    file.getEventBuffer().setEventType(DDMCallbackEvent.EVENT_UPDATE);
    sendS38BUFRequest(out_, id, file.getRecordIncrement(), data, offset, file.getRecordLength(), nullFieldValues);
    out_.flush();

    handleReply(file, "ddmS38UPDAT", null);
  }

  /**
   * Updates the current record with the first record provided by the callback.
  **/
  public void updateCurrent(DDMFile file, DDMWriteCallback listener) throws IOException
  {
    int id = sendS38UPDATRequest(out_, file.getDCLNAM());
    file.getEventBuffer().setEventType(DDMCallbackEvent.EVENT_UPDATE);
    sendS38BUFRequest(file, out_, id, file.getRecordIncrement(), listener, file.getRecordLength(), 0, 1);
    out_.flush();

    handleReply(file, "ddmS38UPDAT", null);
  }

  /**
   * Executes the specified CL command within the DDM host server job.
  **/
  public Message[] execute(final String command) throws IOException
  {
    Vector messages = new Vector();
    sendS38CMDRequest(out_, command);
    out_.flush();

    handleReply(null, "ddmS38CMD", null, messages);
    Message[] arr = new Message[messages.size()];
    messages.copyInto(arr);
    return arr;
  }

  /**
   * Removes the current record from the file.
  **/
  public void deleteCurrent(DDMFile file) throws IOException
  {
    sendS38DELRequest(out_, file.getDCLNAM());
    out_.flush();

    handleReply(file, "ddmS38DEL", null);
  }

  private void position(DDMFile file, int positionType) throws IOException
  {
    sendS38FEODRequest(out_, file.getDCLNAM(), positionType);
    out_.flush();

    handleReply(file, "ddmS38FEOD", null);
  }

  private void readType(DDMFile file, DDMReadCallback listener, int readType, boolean doPosition) throws IOException
  {
    sendS38GetRequest(out_, file.getDCLNAM(), file.getReadWriteType() == DDMFile.READ_WRITE, readType, doPosition);
    out_.flush();

    handleReply(file, "ddmS38Get", listener);
  }

  private void handleReply(final DDMFile file, final String ds, final DDMReadCallback listener) throws IOException
  {
    handleReply(file, ds, listener, null);
  }

  private void handleReply(final DDMFile file, final String ds, final DDMReadCallback listener, final Vector messages) throws IOException
  {
    if (DEBUG) System.out.println("---- HANDLE REPLY ----");
    int length = in_.readShort();
    boolean isContinued = (length > 0x7FFF);
    if (isContinued)
    {
      length = length & 0x7FFF; // The last 2 bytes are the size of the next continued packet.
    }
    if (DEBUG) System.out.println("LENGTH: "+length);
    if (DEBUG) System.out.println("isContinued? "+isContinued);
    if (length < 10)
    {
      throw DataStreamException.badLength(ds, length);
    }
    int gdsID = in_.read();
    int type = in_.read(); // bit mask
    if (DEBUG) System.out.println("GDS, TYPE: "+gdsID+", "+type);
    boolean isChained = (type & 0x40) != 0;
    int correlation = in_.readShort();
    int numRead = 6;
    if (DEBUG) System.out.println("CHAINED: "+isChained);
    DataStreamException exception = null;
    while (numRead < length)
    {
      if (DEBUG) System.out.println(numRead+" vs "+length);
      int ll = in_.readShort();
      int cp = in_.readShort();
      numRead += 4;
      if (DEBUG) System.out.println("HANDLEREPLY: ll="+ll+", cp="+Integer.toHexString(cp));
      if (cp == 0xD402) // S38IOFB
      {
        // Probably end of file or record not found.
        boolean end = !isChained;
        in_.skipBytes(length-numRead);
        numRead += length-numRead;
        while (isChained)
        {
          int localLength = in_.readShort();
          if (localLength < 10)
          {
            throw DataStreamException.badLength(ds, localLength);
          }
          gdsID = in_.read();
          type = in_.read(); // bit mask
          isChained = (type & 0x40) != 0;
          correlation = in_.readShort();
          ll = in_.readShort();
          cp = in_.readShort();
          int localNumRead = 10;
          numRead += 10;
          if (DEBUG) System.out.println("Next is chained? "+isChained+", "+Integer.toHexString(cp));
          if (cp == 0xD201) // S38MSGRM
          {
            int[] numMsgRead = new int[1];
            Message msg = getMessage(in_, ll, numMsgRead);
            numRead += numMsgRead[0];
            localNumRead += numMsgRead[0];
            if (DEBUG) System.out.println("Got message "+msg);
            if (msg != null)
            {
              if (messages != null) messages.addElement(msg);
              String id = msg.getID();
              if (id != null)
              {
                if (id.equals("CPF5001") || id.equals("CPF5025"))
                {
                  if (listener != null)
                  {
                    file.getEventBuffer().setEventType(DDMCallbackEvent.EVENT_READ);
                    listener.endOfFile(file.getEventBuffer());
                  }
                  end = true;
                }
                else if (id.equals("CPF5006"))
                {
                  if (listener != null)
                  {
                    file.getEventBuffer().setEventType(DDMCallbackEvent.EVENT_READ);
                    listener.recordNotFound(file.getEventBuffer());
                  }
                  end = true;
                }
                else if (messages == null)
                {
                  if (exception == null)
                  {
                    exception = DataStreamException.errorMessage(ds+"_chained", msg);
                    exception.fillInStackTrace();
                  }
                  else
                  {
                    exception.addMessage(msg);
                  }
                  end = true;
                }
              }
              else if (messages == null)
              {
                if (exception == null)
                {
                  exception = DataStreamException.errorMessage(ds+"_chained", msg);
                  exception.fillInStackTrace();
                }
                else
                {
                  exception.addMessage(msg);
                }
                end = true;
              }
            }
          }
          else
          {
            in_.skipBytes(ll-4);
            numRead += ll-4;
            localNumRead += ll-4;
          }
          in_.skipBytes(localLength-localNumRead);
          numRead += localLength-localNumRead;
        }
        if (!end)
        {
          throw DataStreamException.badReply(ds, cp);
        }
      }
      else if (cp == 0xD405) // S38BUF
      {
        final boolean largeBuffer = ll > 0x7FFF;
        if (DEBUG) System.out.println("LARGE BUFFER: "+largeBuffer);
        int ioFeedbackOffset = largeBuffer ? in_.readInt() + 18 : ll + 10;
        if (DEBUG) System.out.println("IOFB offset: "+ioFeedbackOffset);
        numRead += largeBuffer ? 4 : 0;

        if (listener == null)
        {
          int toSkip = ioFeedbackOffset - numRead - 4;
          in_.skipBytes(toSkip);
          numRead += toSkip;
        }
        else
        {
          final int recordDataLength = file.getRecordLength();
          final int recordIncrement = file.getRecordIncrement();
          while (numRead+recordIncrement <= ioFeedbackOffset)
          {
            final byte[] recordData = file.getRecordDataBuffer();

            boolean didExtraBytes = false;
            if (isContinued && numRead+recordIncrement > length)
            {
              // Not enough bytes left.
              int diff = length-numRead;
              int dataToRead = diff > recordData.length ? recordData.length : diff;
              if (DEBUG) System.out.println("Data to read: "+dataToRead+" will make numRead: "+(numRead+dataToRead));
              in_.readFully(recordData, 0, dataToRead);
              numRead += dataToRead;
              int remainingRecordData = recordData.length-dataToRead;
              if (DEBUG) System.out.println("Remaining record data: "+remainingRecordData);
              if (remainingRecordData > 0)
              {
                int nextPacketLength = in_.readShort();
                if (DEBUG) System.out.println("Next packet length: "+nextPacketLength);
                if (nextPacketLength <= 0x7FFF)
                {
                  isContinued = false;
                }
                int extraLength = (nextPacketLength & 0x7FFF) - 2;
                if (DEBUG) System.out.println("Still continued? "+isContinued+"; next packet length: "+extraLength);
                length += extraLength;
                if (DEBUG) System.out.println("New length: "+length);
                in_.readFully(recordData, dataToRead, remainingRecordData);
                numRead += remainingRecordData;
              }
              else
              {
                diff -= recordData.length;
                // This reads in the remaining data for the record, including the 2 bytes for the next packet length, if needed.
                byte[] packetBuffer = file.getPacketBuffer();
                in_.readFully(packetBuffer);
                numRead += packetBuffer.length;
                int nextPacketLength = ((packetBuffer[diff] & 0x00FF) << 8) | (packetBuffer[diff+1] & 0x00FF);
                if (nextPacketLength <= 0x7FFF)
                {
                  isContinued = false;
                }
                int extraLength = (nextPacketLength & 0x7FFF) - 2;
                length += extraLength;
                if (DEBUG) System.out.println("NEW length: "+length);

                int recordNumber = -1;
                if (diff < 3)
                {
                  recordNumber = Conv.byteArrayToInt(packetBuffer, 4);
                }
                else if (diff > 5)
                {
                  recordNumber = Conv.byteArrayToInt(packetBuffer, 2);
                }
                else
                {
                  // The packet busted us in the middle of the record number.
                  int offset = 2;
                  int b1 = packetBuffer[offset++];
                  if (diff == offset) offset += 2;
                  int b2 = packetBuffer[offset++];
                  if (diff == offset) offset += 2;
                  int b3 = packetBuffer[offset++];
                  if (diff == offset) offset += 2;
                  int b4 = packetBuffer[offset];
                  recordNumber = ((b1 & 0x00FF) << 24) |
                                 ((b2 & 0x00FF) << 16) |
                                 ((b3 & 0x00FF) << 8) |
                                 (b4 & 0x00FF);
                }
                DDMCallbackEvent ev = file.getEventBuffer();
                ev.setEventType(DDMCallbackEvent.EVENT_READ);
                DDMDataBuffer dataBuffer = file.getDataBuffer(file.getCurrentBufferIndex());
                dataBuffer.setRecordNumber(recordNumber);
                listener.newRecord(ev, dataBuffer);
                file.nextBuffer();
                didExtraBytes = true;
              }
            }
            else
            {
              in_.readFully(recordData);
              numRead += recordData.length;
            }
            if (!didExtraBytes)
            {
              in_.skipBytes(2);
              int recordNumber = in_.readInt();
              // Skip bytes between here and null field map.
              numRead += 6;
              int relative = file.getNullFieldByteMapOffset()-file.getRecordLength()-6;
              in_.skipBytes(relative);
              numRead += relative;
              // Read null field map.
              byte[] nullFieldMap = file.getNullFieldMap();
              in_.readFully(nullFieldMap);
              numRead += nullFieldMap.length;
              boolean[] nullFieldValues = file.getNullFieldValues();
              for (int i=0; i<nullFieldMap.length; ++i)
              {
                nullFieldValues[i] = nullFieldMap[i] == (byte)0xF1;
              }
              DDMCallbackEvent ev = file.getEventBuffer();
              ev.setEventType(DDMCallbackEvent.EVENT_READ);
              DDMDataBuffer dataBuffer = file.getDataBuffer(file.getCurrentBufferIndex());
              dataBuffer.setRecordNumber(recordNumber);
              listener.newRecord(ev, dataBuffer);
              file.nextBuffer();
            }
            if (DEBUG) System.out.println("NUMREAD: "+numRead);
          }
          int toSkip = ioFeedbackOffset-numRead-4;
          int lenSkip = length-numRead;
          if (toSkip > lenSkip) toSkip = lenSkip;
          if (DEBUG) System.out.println("internal skip "+toSkip);
          in_.skipBytes(toSkip);
          numRead += toSkip;
          if (ll < 32768 && ll < file.getRecordIncrement()*file.getBatchSize())
          {
            //TODO: Have probably read all the records??
            file.getEventBuffer().setEventType(DDMCallbackEvent.EVENT_READ);
            listener.endOfFile(file.getEventBuffer());
          }
        }
        // IOFB
        if (DEBUG) System.out.println("Before IOFB, numRead = "+numRead+", length="+length);
        int toSkip = length-numRead;
        if (DEBUG) System.out.println("Skipping "+toSkip+" maybe");
        if (toSkip > 0)
        {
          in_.skipBytes(toSkip);
          numRead += toSkip;
        }
        while (isChained)
        {
          int localLength = in_.readShort();
          if (DEBUG) System.out.println("In loop, length = "+localLength);
          if (DEBUG) System.out.println("3isContinued? "+((localLength & 0x8000) != 0));
          if (localLength < 10)
          {
            throw DataStreamException.badLength(ds+"_chained", localLength);
          }
          gdsID = in_.read();
          type = in_.read(); // bit mask
          isChained = (type & 0x40) != 0;
          if (DEBUG) System.out.println("CHAIN2: "+isChained);
          correlation = in_.readShort();
          ll = in_.readShort();
          cp = in_.readShort();
          if (DEBUG) System.out.println("CP in chain: "+Integer.toHexString(cp));
          int localNumRead = 10;
          numRead += 10;
          if (cp == 0xD201) // S38MSGRM
          {
            int[] numMsgRead = new int[1];
            Message msg = getMessage(in_, ll, numMsgRead);
            localNumRead += numMsgRead[0];
            numRead += numMsgRead[0];
            if (msg != null)
            {
              if (messages != null) messages.addElement(msg);
              String id = msg.getID();
              if (id != null)
              {
                if (id.equals("CPF5001") || id.equals("CPF5025"))
                {
                  if (listener != null)
                  {
                    file.getEventBuffer().setEventType(DDMCallbackEvent.EVENT_READ);
                    listener.endOfFile(file.getEventBuffer());
                  }
                }
                else if (id.equals("CPF5006"))
                {
                  if (listener != null)
                  {
                    file.getEventBuffer().setEventType(DDMCallbackEvent.EVENT_READ);
                    listener.recordNotFound(file.getEventBuffer());
                  }
                }
                else if (messages == null)
                {
                  throw DataStreamException.errorMessage(ds+"_chained", msg);
                }
              }
              else if (messages == null)
              {
                throw DataStreamException.errorMessage(ds+"_chained", msg);
              }
            }
          }
          else if (cp == 0xD402) // S38IOFB
          {
            if (DEBUG) System.out.println("SKipping "+(localLength-localNumRead));
            in_.skipBytes(localLength-localNumRead);
            numRead += (localLength-localNumRead);
            localNumRead = localLength;
          }
          else
          {
            throw DataStreamException.badReply(ds+"_chained", cp);
          }
          if (DEBUG) System.out.println("Skipping extra "+(localLength-localNumRead));
          in_.skipBytes(localLength-localNumRead);
          numRead += localLength-localNumRead;
        }
        if (DEBUG) System.out.println("Should be at end: "+numRead+" and "+length);
      }
      else if (cp == 0xD201) // S38MSGRM
      {
        int[] numMsgRead = new int[1];
        Message msg = getMessage(in_, ll, numMsgRead);
        numRead += numMsgRead[0];
        if (msg != null)
        {
          if (messages != null)
          {
            messages.addElement(msg);
          }
          else
          {
            throw DataStreamException.errorMessage(ds, msg);
          }
        }
        int toSkip = length-numRead;
        if (DEBUG) System.out.println("Skipping "+toSkip+" mmaybe");
        if (toSkip > 0)
        {
          in_.skipBytes(toSkip);
          numRead += toSkip;
        }
        while (isChained)
        {
          int localLength = in_.readShort();
          if (DEBUG) System.out.println("In loop, length = "+localLength);
          if (DEBUG) System.out.println("3isContinued? "+((localLength & 0x8000) != 0));
          if (localLength < 10)
          {
            throw DataStreamException.badLength(ds+"_chained", localLength);
          }
          gdsID = in_.read();
          type = in_.read(); // bit mask
          isChained = (type & 0x40) != 0;
          if (DEBUG) System.out.println("CHAIN2: "+isChained);
          correlation = in_.readShort();
          ll = in_.readShort();
          cp = in_.readShort();
          if (DEBUG) System.out.println("CP in chain: "+Integer.toHexString(cp));
          int localNumRead = 10;
          numRead += 10;
          if (cp == 0xD201) // S38MSGRM
          {
            numMsgRead = new int[1];
            msg = getMessage(in_, ll, numMsgRead);
            localNumRead += numMsgRead[0];
            numRead += numMsgRead[0];
            if (msg != null)
            {
              if (messages != null) messages.addElement(msg);
              String id = msg.getID();
              if (id != null)
              {
                if (id.equals("CPF5001") || id.equals("CPF5025"))
                {
                  if (listener != null)
                  {
                    file.getEventBuffer().setEventType(DDMCallbackEvent.EVENT_READ);
                    listener.endOfFile(file.getEventBuffer());
                  }
                }
                else if (id.equals("CPF5006"))
                {
                  if (listener != null)
                  {
                    file.getEventBuffer().setEventType(DDMCallbackEvent.EVENT_READ);
                    listener.recordNotFound(file.getEventBuffer());
                  }
                }
                else if (messages == null)
                {
                  throw DataStreamException.errorMessage(ds+"_chained", msg);
                }
              }
              else if (messages == null)
              {
                throw DataStreamException.errorMessage(ds+"_chained", msg);
              }
            }
          }
          else if (cp == 0xD402) // S38IOFB
          {
            if (DEBUG) System.out.println("SKipping "+(localLength-localNumRead));
            in_.skipBytes(localLength-localNumRead);
            numRead += (localLength-localNumRead);
            localNumRead = localLength;
          }
          else
          {
            throw DataStreamException.badReply(ds+"_chained", cp);
          }
          if (DEBUG) System.out.println("Skipping extra "+(localLength-localNumRead));
          in_.skipBytes(localLength-localNumRead);
          numRead += localLength-localNumRead;
        }
      }
      else
      {
        throw DataStreamException.badReply(ds, cp);
      }
    }
    in_.end();
    if (exception != null)
    {
      throw exception;
    }
  }

  // The declared name is an 8-byte unique handle that DDM will use to quickly access the file.
  // I assume it is unique within a given connection. It must be EBCDIC and blank-padded.
  // Which means, if we use an 8-digit number, we can only have 100,000,000 files open simultaneously. :-)
  private byte[] generateDCLNAM()
  {
    final byte[] dclName = new byte[8];

    int num = dclNamCounter_++; // No need to synchronize, this class isn't threadsafe anyway.

    // Hooray for loop-unrolling and temp variables!
    // This works great with the IBM 1.5 compiler and JIT. Not so much with Sun 1.4.
    int mod = num % 10;
    dclName[7] = (byte)(mod + 0xF0);
    num = num / 10;
    mod = num % 10;
    dclName[6] = (byte)(mod + 0xF0);
    num = num == 0 ? 0 : num / 10;
    mod = num == 0 ? 0 : num % 10;
    dclName[5] = (byte)(mod + 0xF0);
    num = num == 0 ? 0 : num / 10;
    mod = num == 0 ? 0 : num % 10;
    dclName[4] = (byte)(mod + 0xF0);
    num = num == 0 ? 0 : num / 10;
    mod = num == 0 ? 0 : num % 10;
    dclName[3] = (byte)(mod + 0xF0);
    num = num == 0 ? 0 : num / 10;
    mod = num == 0 ? 0 : num % 10;
    dclName[2] = (byte)(mod + 0xF0);
    num = num == 0 ? 0 : num / 10;
    mod = num == 0 ? 0 : num % 10;
    dclName[1] = (byte)(mod + 0xF0);
    num = num == 0 ? 0 : num / 10;
    mod = num == 0 ? 0 : num % 10;
    dclName[0] = (byte)(mod + 0xF0);
    return dclName;
  }

  private void sendS38CMDRequest(HostOutputStream out, String command) throws IOException
  {
    final byte[] commandBytes = Conv.stringToEBCDICByteArray37(command);
    out.writeShort(14+commandBytes.length); // Length.
    out.write(0xD0); // SNA GDS architecture ID.
    out.write(1); // Format ID.
    out.writeShort(newCorrelationID());
    out.writeShort(8+commandBytes.length); // S38CMD LL.
    out.writeShort(0xD006); // S38CMD CP.
    out.writeShort(4+commandBytes.length); // S38CMDST LL.
    out.writeShort(0xD103); // S38CMDST CP.
    out.write(commandBytes);
  }

  private void sendS38DELRequest(final HostOutputStream out, final byte[] dclNam) throws IOException
  {
    out.writeInt(0x0016D001); // Combined length, SNA GDS arch ID, format ID.
    out.writeShort(newCorrelationID());
    out.writeInt(0x0010D007); // Combined S38DEL LL and CP.
    out.writeInt(0x000C1136); // Combined DCLNAM LL and CP.
    out.write(dclNam, 0, 8);
  }

  private int sendS38UPDATRequest(HostOutputStream out, byte[] dclNam) throws IOException
  {
    out.writeInt(0x001ED051); // Combined length, SNA GDS arch ID, and format ID.
    int id = newCorrelationID();
    out.writeShort(id);
    out.writeInt(0x0018D019); // Combined S38UPDAT LL and CP.
    out.writeInt(0x000C1136); // Combined DCLNAM LL and CP.
    out.write(dclNam, 0, 8);
    out.writeInt(0x0008D119); // Combined S38OPTL LL and CP.
//    out.write(33); // Type of read (current).
//    out.write(3); // Share - update norm.
//    out.write(1); // Data - don't retrieve record, ignore deleted records.
//    out.write(7); // Operation - update.
    out.writeInt(0x21030107); // Combined options.
    return id;
  }

  private void sendS38PUTMRequest(HostOutputStream out, byte[] dclNam, final int correlationID) throws IOException
  {
    out.writeInt(0x0016D051); // Combined length, SNA GDS arch ID, and format ID.
    out.writeShort(correlationID);
    out.writeInt(0x0010D013); // Combined S38PUTM LL and CP.
    out.writeInt(0x000C1136); // Combined DCLNAM LL and CP.
    out.write(dclNam, 0, 8);
  }

  private void sendS38BUFRequest(HostOutputStream out, final int correlationID, final int recordIncrement, final byte[] data, final int offset, final int length, final boolean[] nullFieldValues) throws IOException
  {
    // Now send S38BUF with data in it.
    out.writeShort(recordIncrement+10); // Length.
    out.writeShort(0xD003); // Combined SNA GDS arch ID and format ID.
    out.writeShort(correlationID);
    out.writeShort(recordIncrement+4); // S38BUF LL.
    out.writeShort(0xD405); // S38BUF CP.
    out.write(data, offset, length);
    final int remaining = recordIncrement - length;
    final boolean doNullFields = nullFieldValues != null;
    for (int i=0; i<remaining; ++i)
    {
      int toWrite = (doNullFields && i < nullFieldValues.length) ? (nullFieldValues[i] ? 0xF1 : 0xF0) : 0xF0;
      out.write(toWrite);
    }
  }

  private void sendS38BUFRequest(DDMFile file, HostOutputStream out, final int correlationID, final int recordIncrement, DDMWriteCallback listener, final int length, final int startingRecordNumber, final int batchSize) throws IOException
  {
    final int total = batchSize*recordIncrement;

    // Now send S38BUF with data in it.
    out.writeShort(total+10); // Length.
    out.write(0xD003); // Combined SNA GDS arch ID and format ID.
    out.writeShort(correlationID);
    out.writeShort(total+4); // S38BUF LL.
    out.writeShort(0xD405); // S38BUF CP.
    final int limit = startingRecordNumber + batchSize;
    final DDMCallbackEvent event = file.getEventBuffer();
    for (int i=startingRecordNumber; i<limit; ++i)
    {
      final byte[] data = listener.getRecordData(event, i);
      final int offset = listener.getRecordDataOffset(event, i);
      out.write(data, offset, length);
      final int remaining = recordIncrement - length;
      final boolean[] nullFieldValues = listener.getNullFieldValues(event, i);
      final boolean doNullFields = nullFieldValues != null;
      for (int j=0; j<remaining; ++j)
      {
        int toWrite = (doNullFields && j < nullFieldValues.length) ? (nullFieldValues[j] ? 0xF1 : 0xF0) : 0xF0;
        out.write(toWrite);
      }
    }
  }

  private void sendS38GetRequest(HostOutputStream out, byte[] dclNam, boolean forUpdate, int readType, boolean doPosition) throws IOException
  {
    out.writeInt(0x001ED001); // Combined length, SNA GDS architecture ID, and format ID.
    out.writeShort(newCorrelationID()); // Request correlation ID.
    out.writeInt(0x0018D00C); // Combined S38GET LL and CP.
    out.writeInt(0x000C1136); // Combined DCLNAM LL and CP.
    out.write(dclNam, 0, 8);
    out.writeInt(0x0008D119); // Combined S38OPTL LL and CP.
    out.write(readType); // Type of read (next, prev, first, last, current).
    out.write(forUpdate ? 3 : 0); // Share - update norm, or read norm.
    out.write(doPosition ? 1 : 0); // Data - don't retrieve or do retrieve record, ignore deleted records.
    out.write(1); // Operation - get.
  }

  private void sendS38GetKRequest(HostOutputStream out, byte[] dclNam, boolean forUpdate, byte[] recordFormatName, int readType, byte[] key, int numberOfKeyFields, boolean doPosition) throws IOException
  {
    out.writeShort(63+key.length); // Length;
    out.writeShort(0xD001); // Combined SNA GDS architecture ID and format ID.
    out.writeShort(newCorrelationID()); // Request correlation ID.
    out.writeShort(57+key.length); // S38GETK LL.
    out.writeInt(0xD00E000C); // Combined S38GETK CP and DCLNAM LL.
    out.writeShort(0x1136); // DCLNAM CP.
    out.write(dclNam, 0, 8);
    out.writeInt(0x0008D119); // Combined S38OPTL LL and CP.
    out.write(readType); // Type of read (next, prev, first, last, current).
    if (DEBUG) System.out.println("Read type? 0x"+Integer.toHexString(readType));
    if (DEBUG) System.out.println("For UPDATE? "+forUpdate);
    out.write(forUpdate ? 3 : 0); // Share - update norm, or read norm.
    out.writeShort(doPosition ? 0x0103 : 0x0003); // Combined data (retrieve record, ignore deleted records) and operation (getk).

    out.writeShort(33+key.length); // S38CTLL LL.
    out.writeInt(0xD1050100); // Combined S38CTLL CP (Control list), record format ID, and record format length.
    out.write(10);
    if (DEBUG) System.out.println("recfmt length is "+recordFormatName.length);
    out.write(recordFormatName);
    out.writeInt(0x0F000200); // Combined member number ID, member number length, half of member number value.
    out.writeInt(0x00080004); // Combined half of member number value, number of fields ID, and number of fields length.
    out.writeInt(numberOfKeyFields);

    out.write(7);
    out.writeShort(key.length);
    out.write(key);

    out.write(0xFF); // Control list end.
  }

  private void sendS38GetDRequest(HostOutputStream out, byte[] dclNam, boolean forUpdate, byte[] recordFormatName, int recordNumber, boolean doPosition) throws IOException
  {
    out.writeInt(0x003CD001); // Combined length, SNA GDS architecture ID, and format ID.
    out.writeShort(newCorrelationID()); // Request correlation ID.
    out.writeInt(0x0036D00D); // Combined S38GETD LL and CP.
    out.writeInt(0x000C1136); // Combined DCLNAM LL and CP.
    out.write(dclNam, 0, 8);
    out.writeInt(0x0008D119); // Combined S38OPTL LL and CP.
    out.write(8); // Type of read - definite.
    out.write(forUpdate ? 3 : 0); // Share - update norm, or read norm.
    out.write(doPosition ? 1 : 0); // Data - don't retrieve or do retrieve record, ignore deleted records.
    out.writeInt(0x02001ED1); // Combind operation (getd), S38CTLLL, and half of S38CTLL CP (Control list).
    out.writeInt(0x0501000C); // Combined half of S38CTLL CP (Control list), record format ID, and record format length.
    out.write(recordFormatName);
    out.writeInt(0x0F000200); // Combined member number ID, member number length, and half of member number value.
    out.writeInt(0x00020004); // Combined half of member number value, relative record number ID, and relative record number length.
    out.writeInt(recordNumber);
    out.write(0xFF); // Control list end.
  }

  private void sendS38FEODRequest(HostOutputStream out, byte[] dclNam, int positionType) throws IOException
  {
    out.writeInt(0x001ED051); // Combined length, SNA GDS architecture ID, and format ID (this is a chained request, same correlation ID on chain).
    final int id = newCorrelationID();
    out.writeShort(id); // Request correlation ID.
    out.writeInt(0x0018D00B); // Combined S38FEOD LL and CP.
    out.writeInt(0x000C1136); // Combined DCLNAM LL and CP.
    out.write(dclNam, 0, 8);
    out.writeInt(0x0008D119); // Combined S38OPTL LL and CP.
    out.write(positionType); // Type of read (next, prev, first, last, current).
    out.writeInt(0x02010100); // Combined share (read and release previous lock), data (do not retrieve record, ignore deleted records), operation (get), and half of chained S38BUF length.

    // Now send chained S38BUF. This is what the protocol expects.
    out.write(0x0B); // Other half of chained S38BUF length.
    out.writeShort(0xD003); // Combined SNA GDS architecture ID and format ID.
    out.writeShort(id);
    out.writeInt(0x0005D405); // Combined S38BUF LL and CP.
    out.write(0); // Value.
  }

  private void sendS38CloseRequest(HostOutputStream out, byte[] dclNam) throws IOException
  {
    out.writeInt(0x001BD001); // Combined length, SNA GDS architecture ID, and format ID.
    out.writeShort(newCorrelationID()); // Request correlation ID.
    out.writeShort(21); // S38CLOSE LL.
    out.writeShort(0xD004); // S38CLOSE CP.
    out.writeShort(12); // DCLNAM LL.
    out.writeShort(0x1136); // DCLNAM CP.
    out.write(dclNam, 0, 8);
    out.writeShort(5); // S38CLOST LL.
    out.writeShort(0xD121); // S38CLOST CP.
    out.write(0x02); // S38CLOST value. 2 means permanent close.
  }

  private void sendS38OpenRequest(HostOutputStream out, String file, String library, String member, boolean doRead, boolean doWrite, boolean keyed, String recordFormatName, final byte[] dclNam, int batchSize) throws IOException
  {
    if (!doRead && !doWrite) doRead = true;
    final boolean commitmentControl = false;
    final boolean userBuffer = false;
    final int ufcbLength = 106 + (commitmentControl ? 3 : 0) + (keyed || doRead ? 3 : 0);

    final int totalLength = 26+ufcbLength;

    if (batchSize < 1 || (doRead && doWrite))
    {
      batchSize = 1;
    }
    batchSize = batchSize & 0x7FFF;

    out.writeShort(totalLength);
    out.write(0xD0); // SNA GDS architecture ID.
    // Format ID:
    // continue on error mask = 0010 0000
    // type mask              = 0000 0011  1 = RQSDSS, 2 = RPYDSS, 3 = OBJDSS
    // same correlation mask  = 0001 0000
    // chained mask           = 0100 0000
    out.write(1); // Format ID.
    out.writeShort(newCorrelationID()); // Request correlation ID.
    out.writeShort(20+ufcbLength); // S38OPEN LL.
    out.writeShort(0xD011); // S38OPEN CP.
    out.writeShort(12); // DCLNAM LL.
    out.writeShort(0x1136); // DCLNAM CP.
    out.write(dclNam); // 8 bytes.
    out.writeShort(4+ufcbLength); // S38UFCB LL.
    out.writeShort(0xD11F); // S38UFCB CP.
    int numWritten = 18+dclNam.length;
    // UFCB.
    writePadEBCDIC10(file, out); // Filename.
    out.writeShort(72); // WDMHLIB.
    writePadEBCDIC10(library, out); // Library.
    out.writeShort(73); // WDMHMBR.
    writePadEBCDIC10(member, out); // Member.
    numWritten += 34;
    out.writeInt(0); out.writeInt(0); out.writeInt(0); // Skip 12 bytes.
    numWritten += 12;
    int openOptions = userBuffer ? 0x1003 : 0x1002;
    if (doRead && !doWrite) openOptions |= 0x0020; // Read-only.
    else if (doRead && doWrite) openOptions |= 0x3C; // Read-write.
    else openOptions |= 0x10;
    out.writeShort(openOptions); // index 46
    out.writeInt(0xF0F1F0F0); // Release and version numbers;
    out.writeInt(0); // Skip 4 bytes.
    out.writeInt(0x20000000); // Record blocking on, skip 3 bytes.
    out.writeInt(0x02000000); // Handle null-capable fields, skip 3 bytes.  index 60-63
    out.writeInt(0); out.writeInt(0); out.writeInt(0); out.writeInt(0); // Skip 16 bytes.
    out.writeShort(6); // LVLCHK CP.
    out.write(0); // Don't do LVLCHK.
    numWritten += 37;
    if (keyed || doRead)
    {
      out.writeShort(60); // ARRSEQ CP.
      if (DEBUG) System.out.println("Opening keyed? "+keyed+", read? "+doRead);
      out.write(!keyed || !doRead ? 0x80 : 0x00); // 0x80 = arrival sequenece, 0x00 = keyed access path.
      numWritten += 3;
    }
    if (commitmentControl)
    {
      out.writeShort(59); // COMMIT CP.
      out.write(0x00); // Commit lock level: none = 0x00, default = 0x80, *CHG = 0x82, *CS = 0x86, *ALL = 0x87.
      numWritten += 3;
    }
    out.writeShort(58); // SEQONLY CP.
    out.write(doRead && doWrite ? 0x40 : 0xC0); // 0x40 = NO, 0xC0 = YES.
    out.writeShort(batchSize); // Blocking factor.
    out.writeShort(9); // Record format group CP.
    out.writeShort(1); // Max # of record formats.
    out.writeShort(1); // Cur # of record formats.
    writePadEBCDIC10(recordFormatName, out);
    out.writeShort(32767); // End of variable-length UFCB.
    numWritten += 23;
    if (DEBUG) System.out.println("NUM written vs TOTAL length on OPEN: "+numWritten+", "+totalLength);
  }

  private static void sendSECCHKRequest(HostOutputStream out, byte[] userBytes, byte[] encryptedPassword) throws IOException
  {
    out.writeShort(34 + encryptedPassword.length);
    out.write(0xD0); // GDS ID.
    out.write(1); // Type is RQSDSS.
    out.writeShort(0); // Skip 2 bytes.
    out.writeShort(28 + encryptedPassword.length); // SECCHK LL.
    out.writeShort(0x106E); // SECCHK CP.
    out.writeShort(6); // SECMEC LL.
    out.writeShort(0x11A2); // SECMEC CP.
    out.writeShort(encryptedPassword.length == 20 ? 8 : 6); // SHA or DES.
    out.writeShort(14); // USRID LL.
    out.writeShort(0x11A0); // USRID CP.
    out.write(userBytes, 0, 10);
    out.writeShort(4 + encryptedPassword.length); // PASSWORD LL.
    out.writeShort(0x11A1); // PASSWORD CP.
    out.write(encryptedPassword);
  }

  private static long sendACCSECRequest(HostOutputStream out) throws IOException
  {
    out.writeShort(28); // Length.
    out.write(0xD0); // GDS ID.
    out.write(1); // Type is RQSDSS.
    out.writeShort(1); // Correlation ID.
    out.writeShort(22); // ACCSEC LL.
    out.writeShort(0x106D); // ACCSEC CP.
    out.writeShort(6); // SECMEC LL.
    out.writeShort(0x11A2); // SECMEC CP.
    boolean useStrongEncryption = false;
    out.writeShort(useStrongEncryption ? 8 : 6);
    out.writeShort(12); // SECTKN LL.
    out.writeShort(0x11DC); // SECTKN CP.
    long clientSeed = System.currentTimeMillis();
    out.writeLong(clientSeed);
    return clientSeed;
  }

  private static void sendEXCSATRequest(HostOutputStream out) throws IOException
  {
    out.writeShort(126); // Length.
    out.write(0xD0); // GDS ID.
    out.write(1); // Type is RQSDSS.
    out.writeShort(0); // Skip 2 bytes.
    out.writeShort(120); // EXCSAT LL.
    out.writeShort(0x1041); // EXCSAT CP.
    out.writeShort(9); // EXTNAM LL.
    out.writeShort(0x115E); // EXTNAM CP.
    out.writeInt(0xE3C2D6E7); out.write(0xF2); // EXTNAM - EBCDIC "TBOX2".
    out.writeShort(11); // SRVCLSNM LL.
    out.writeShort(0x1147); // SRVCLSNM CP.
    out.writeShort(7); // CHRSTRDR LL.
    out.writeShort(0x0009); // CHRSTRDR CP.
    out.writeShort(0xD8C1); out.write(0xE2); // SRVCLSNM - EBCDIC "QA5".
    out.writeShort(96); // MGRLVLLS LL.
    out.writeShort(0x1404); // MGRLVLLS CP.
    out.writeShort(0x1403); // AGENT CP.
    out.writeShort(3);
    out.writeShort(0x1423); // ALTINDF CP.
    out.writeShort(3);
    out.writeShort(0x1405); // CMBACCAM CP.
    out.writeShort(3);
    out.writeShort(0x1406); // CMBKEYAM CP.
    out.writeShort(3);
    out.writeShort(0x1407); // CMBRNBAM CP.
    out.writeShort(3);
    out.writeShort(0x1474); // CMNTCPIP CP.
    out.writeShort(5);
    out.writeShort(0x1458); // DICTIONARY CP.
    out.writeShort(1);
    out.writeShort(0x1457); // DIRECTORY CP.
    out.writeShort(3);
    out.writeShort(0x140C); // DIRFIL CP.
    out.writeShort(3);
    out.writeShort(0x1419); // DRCAM CP.
    out.writeShort(3);
    out.writeShort(0x141E); // KEYFIL CP.
    out.writeShort(3);
    out.writeShort(0x1422); // LCKMGR CP.
    out.writeShort(3);
    out.writeShort(0x240F); // RDB CP.
    out.writeShort(3);
    out.writeShort(0x1432); // RELKEYAM CP.
    out.writeShort(3);
    out.writeShort(0x1433); // RELRNBAM CP.
    out.writeShort(3);
    out.writeShort(0x1440); // SECMGR CP.
    out.writeShort(1);
    out.writeShort(0x143B); // SEQFIL CP.
    out.writeShort(3);
    out.writeShort(0x2407); // SQLAM CP.
    out.writeShort(3);
    out.writeShort(0x1463); // STRAM CP.
    out.writeShort(3);
    out.writeShort(0x1465); // STRFIL CP.
    out.writeShort(3);
    out.writeShort(0x143C); // SUPERVISOR CP.
    out.writeShort(3);
    out.writeShort(0x147F); // SYSCMDMGR CP.
    out.writeShort(4);
    out.writeShort(0x14A0); // RSCRCVM CP.
    out.writeShort(4);
  }
}
