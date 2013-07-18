///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  CommandConnection.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command;

import com.ibm.jtopenlite.*;
//import com.ibm.jtopenlite.base.experimental.*;
import java.io.*;
import java.net.*;
import javax.net.ssl.SSLSocketFactory;

/**
 * Represents a TCP/IP socket connection to the System i Remote Command host server (QSYSWRK/QZRCSRVS prestart jobs).
**/
public class CommandConnection extends HostServerConnection
{
  /**
   * The default TCP/IP port the Remote Command host server listens on.
   * If your system has been configured to use a different port, use
   * the {@link PortMapper PortMapper} class to determine the port.
  **/
  public static final int DEFAULT_COMMAND_SERVER_PORT = 8475;

  private int ccsid_;
  private int datastreamLevel_;

  private CommandConnection(SystemInfo info, Socket socket, HostInputStream in, HostOutputStream out, int ccsid, int datastreamLevel, String user, String jobName)
  {
    super(info, user, jobName, socket, in, out);
    ccsid_ = ccsid;
    datastreamLevel_ = datastreamLevel;
  }

  protected void sendEndJobRequest() throws IOException
  {
    out_.writeInt(20); // Length is 40 if server ID is E004.
    out_.writeShort(0); // Header ID.
    out_.writeShort(0xE008); // Server ID.
    out_.writeInt(0); // CS instance.
    out_.writeInt(0); // Correlation ID.
    out_.writeShort(0); // Template length.
    out_.writeShort(0x1004); // ReqRep ID for remote command server.
    out_.flush();
  }

  /**
   * Connects to the Remote Command host server on the default port after first connecting
   * to the Signon host server and authenticating the specified user.
  **/
  public static CommandConnection getConnection(String system, String user, String password) throws IOException
  {
    return getConnection(false, system, user, password);
  }

  /**
   * Connects to the Remote Command host server on the default port after first connecting
   * to the Signon host server and authenticating the specified user.
  **/
  public static CommandConnection getConnection(final boolean isSSL, String system, String user, String password) throws IOException
  {
    SignonConnection conn = SignonConnection.getConnection(isSSL, system, user, password);
    try
    {
      return getConnection(isSSL, conn.getInfo(), user, password);
    }
    finally
    {
      conn.close();
    }
  }

  /**
   * Connects to the Remote Command host server on the default port using the specified system information and user.
  **/
  public static CommandConnection getConnection(SystemInfo info, String user, String password) throws IOException
  {
    return getConnection(false, info, user, password);
  }
  /**
   * Connects to the Remote Command host server on the default port using the specified system information and user.
  **/
  public static CommandConnection getConnection(final boolean isSSL, SystemInfo info, String user, String password) throws IOException
  {
    return getConnection(isSSL, info, user, password, DEFAULT_COMMAND_SERVER_PORT, false);
  }

  /**
   * Connects to the Remote Command host server on the specified port using the specified system information and user.
  **/
  public static CommandConnection getConnection(SystemInfo info, String user, String password, int commandPort, boolean compress) throws IOException
  {
      return getConnection(false, info, user, password, commandPort, compress);
  }
  /**
   * Connects to the Remote Command host server on the specified port using the specified system information and user.
  **/
  public static CommandConnection getConnection(final boolean isSSL, SystemInfo info, String user, String password, int commandPort, boolean compress) throws IOException
  {
    if (commandPort < 0 || commandPort > 65535)
    {
      throw new IOException("Bad command port: "+commandPort);
    }
    CommandConnection conn = null;

    Socket commandServer = isSSL? SSLSocketFactory.getDefault().createSocket(info.getSystem(), commandPort) : new Socket(info.getSystem(), commandPort);
    InputStream in = commandServer.getInputStream();
    OutputStream out = commandServer.getOutputStream();
    try
    {
      if (compress)
      {
        throw new IOException("Experimental compression streams not enabled.");
//        in = new CompressionInputStream(in);
//        out = new CompressionOutputStream(new BufferedOutputStream(out));
      }

      // Exchange random seeds.
      HostOutputStream dout = new HostOutputStream(compress ? out : new BufferedOutputStream(out));
      HostInputStream din = new HostInputStream(new BufferedInputStream(in));
      String jobName = connect(info, dout, din, 0xE008, user, password);

      sendExchangeAttributesRequest(dout);
      dout.flush();

      int length = din.readInt();
      if (length < 20)
      {
        throw DataStreamException.badLength("commandExchangeAttributes", length);
      }
      din.skipBytes(16);
      int rc = din.readShort();
      if (rc != 0 &&
          rc != 0x0100) // Limited user.
      {
        throw DataStreamException.badReturnCode("commandExchangeAttributes", rc);
      }
      int ccsid = din.readInt();
      din.skipBytes(8);
      int datastreamLevel = din.readShort();
      din.skipBytes(length-36);
      din.end();

      conn = new CommandConnection(info, commandServer, din, dout, ccsid, datastreamLevel, user, jobName);
      return conn;
    }
    finally
    {
      if (conn == null)
      {
        in.close();
        out.close();
        commandServer.close();
      }
    }
  }

  /**
   * Calls the specified program using the specified parameter data and returns the result.
  **/
  public CommandResult call(String pgmLibrary, String pgmName, Parameter[] parms) throws IOException
  {
    if (isClosed()) throw new IOException("Connection closed");

    sendCallProgramRequest(out_, pgmLibrary, pgmName, parms, datastreamLevel_);
    out_.flush();

    int length = in_.readInt();
    if (length < 20)
    {
      throw DataStreamException.badLength("commandCallProgram", length);
    }
    in_.skipBytes(16);
    int rc = in_.readShort();
    final boolean success = rc == 0;
    Message[] messages = null;
    if (rc == 0)
    {
      in_.skipBytes(2);
      for (int i=0; i<parms.length; ++i)
      {
        if (parms[i].getOutputLength() > 0)
        {
          int byteLength = in_.readInt();
          in_.skipBytes(2);
          int outputLength = in_.readInt();
          int usage = in_.readShort();
          byte[] outputData = new byte[byteLength-12];
          in_.readFully(outputData);
          parms[i].setOutputData(outputData);
        }
      }
      messages = new Message[0];
    }
    else
    {
      messages = getMessages(length);
//      throw DataStreamException.badReturnCode("commandCallProgram", rc);
    }
    in_.end();
    return new CommandResult(success, messages, rc);
  }

  /**
   * Calls the specified program and returns the result.
  **/
  public CommandResult call(Program pgm) throws IOException
  {
    if (isClosed()) throw new IOException("Connection closed");

    pgm.newCall();

    sendCallProgramRequest(out_, pgm, datastreamLevel_);
    out_.flush();

    int length = in_.readInt();
    if (length < 20)
    {
      throw DataStreamException.badLength("commandCallProgram", length);
    }
    in_.skipBytes(16);
    int rc = in_.readShort();
    final boolean success = rc == 0;
    Message[] messages = null;
    if (rc == 0)
    {
      in_.skipBytes(2);
      final int numParms = pgm.getNumberOfParameters();
      for (int i=0; i<numParms; ++i)
      {
        if (pgm.getParameterOutputLength(i) > 0)
        {
          int byteLength = in_.readInt();
          in_.skipBytes(2);
          int outputLength = in_.readInt();
          int usage = in_.readShort();
          int parmLength = byteLength-12;
          byte[] buf = pgm.getTempDataBuffer();
          if (buf == null || buf.length < parmLength) buf = new byte[parmLength];
          in_.readFully(buf, 0, parmLength);
          pgm.setParameterOutputData(i, buf, parmLength);
        }
      }
      messages = new Message[0];
    }
    else
    {
      messages = getMessages(length);
//      throw DataStreamException.badReturnCode("commandCallProgram", rc);
    }
    in_.end();
    return new CommandResult(success, messages, rc);
  }

  private static void sendCallProgramRequest(HostOutputStream out, Program pgm, int datastreamLevel) throws IOException
  {
    int length = 43;
    final int numParms = pgm.getNumberOfParameters();
    for (int i=0; i<numParms; ++i)
    {
      length += 12 + pgm.getParameterInputLength(i);
    }

    out.writeInt(length); // Length;
    out.writeShort(0); // Header ID.
    out.writeShort(0xE008); // Server ID.
    out.writeInt(0); // CS instance.
    out.writeInt(0); // Correlation ID.
    out.writeShort(23); // Template length.
    out.writeShort(0x1003); // ReqRep ID.

//    byte[] programBytes = Util.blankPadEBCDIC10(pgm.getProgramName());
//    out.write(programBytes, 0, 10);
    writePadEBCDIC10(pgm.getProgramName(), out);
//    byte[] libraryBytes = Util.blankPadEBCDIC10(pgm.getProgramLibrary());
//    out.write(libraryBytes, 0, 10);
    writePadEBCDIC10(pgm.getProgramLibrary(), out);

    final boolean newerServer = datastreamLevel >= 10;
    final int messageOption = newerServer ? 4 : (datastreamLevel < 7 ? 0 : 2); // Always return all messages when possible.
    out.writeByte(messageOption);

    out.writeShort(numParms); // Number of parameters.
    for (int i=0; i<numParms; ++i)
    {
      final int inputLength = pgm.getParameterInputLength(i);
      final int outputLength = pgm.getParameterOutputLength(i);
      out.writeInt(12 + inputLength); // Parameter LL.
      out.writeShort(0x1103); // Parameter CP.
      final int maxLength = inputLength > outputLength ? inputLength : outputLength;
      out.writeInt(maxLength); // Either the input length or output length, whichever is larger.
      switch (pgm.getParameterType(i))
      {
        case Parameter.TYPE_NULL:
          if (datastreamLevel < 6)
          {
            // Nulls not allowed.
            out.writeShort(1); // Treat as input.
          }
          else
          {
            out.writeShort(255);
          }
          break;
        case Parameter.TYPE_INPUT:
          out.writeShort(11);
          byte[] inputBuf = pgm.getParameterInputData(i);
          out.write(inputBuf, 0, inputLength);
          break;
        case Parameter.TYPE_OUTPUT:
          out.writeShort(12);
          break;
        case Parameter.TYPE_INPUT_OUTPUT:
          out.writeShort(13);
          inputBuf = pgm.getParameterInputData(i);
          out.write(inputBuf, 0, inputLength);
          break;
      }
    }
  }

  private static void sendCallProgramRequest(HostOutputStream out, String pgmLibrary, String pgmName, Parameter[] parms, int datastreamLevel) throws IOException
  {
    int length = 43;
    for (int i=0; i<parms.length; ++i)
    {
      length += 12 + parms[i].getInputLength();
    }

    out.writeInt(length); // Length;
    out.writeShort(0); // Header ID.
    out.writeShort(0xE008); // Server ID.
    out.writeInt(0); // CS instance.
    out.writeInt(0); // Correlation ID.
    out.writeShort(23); // Template length.
    out.writeShort(0x1003); // ReqRep ID.

//    byte[] programBytes = Util.blankPadEBCDIC10(pgmName);
//    out.write(programBytes, 0, 10);
    writePadEBCDIC10(pgmName, out);
//    byte[] libraryBytes = Util.blankPadEBCDIC10(pgmLibrary);
//    out.write(libraryBytes, 0, 10);
    writePadEBCDIC10(pgmLibrary, out);

    final boolean newerServer = datastreamLevel >= 10;
    final int messageOption = newerServer ? 4 : (datastreamLevel < 7 ? 0 : 2); // Always return all messages when possible.
    out.writeByte(messageOption);

    out.writeShort(parms.length); // Number of parameters.
    for (int i=0; i<parms.length; ++i)
    {
      out.writeInt(12 + parms[i].getInputLength()); // Parameter LL.
      out.writeShort(0x1103); // Parameter CP.
      out.writeInt(parms[i].getMaxLength()); // Either the input length or output length, whichever is larger.
      switch (parms[i].getType())
      {
        case Parameter.TYPE_NULL:
          if (datastreamLevel < 6)
          {
            // Nulls not allowed.
            out.writeShort(1); // Treat as input.
          }
          else
          {
            out.writeShort(255);
          }
          break;
        case Parameter.TYPE_INPUT:
          out.writeShort(11);
          out.write(parms[i].getInputData(), 0, parms[i].getInputLength());
          break;
        case Parameter.TYPE_OUTPUT:
          out.writeShort(12);
          break;
        case Parameter.TYPE_INPUT_OUTPUT:
          out.writeShort(13);
          out.write(parms[i].getInputData(), 0, parms[i].getInputLength());
          break;
      }
    }
  }

  /**
   * Executes the specified CL command string and returns the result.
   * The command must be non-interactive.
  **/
  public CommandResult execute(String cmd) throws IOException
  {
    if (isClosed()) throw new IOException("Connection closed");

    sendRunCommandRequest(out_, cmd, datastreamLevel_);
    out_.flush();

    int length = in_.readInt();
    if (length < 20)
    {
      throw DataStreamException.badLength("commandRunCommand", length);
    }
    in_.skipBytes(16);
    int rc = in_.readShort();
    if (rc != 0 && rc != 0x0400)
    {
      in_.skipBytes(length-22);
      throw DataStreamException.badReturnCode("commandRunCommand", rc);
    }
    Message[] messages = getMessages(length);
    in_.end();
    return new CommandResult(rc == 0, messages, rc);
  }

  private Message[] getMessages(int length) throws IOException
  {
    char[] buffer = new char[1024];
    int numMessages = in_.readShort();
    Message[] messages = new Message[numMessages];
    int curLength = 24;
    for (int i=0; i<numMessages; ++i)
    {
      int oldLength = curLength;
      int messageLength = in_.readInt();
      curLength += 4;
      int messageCP = in_.readShort();
      curLength += 2;
      if (messageCP == 0x1106)
      {
        int textCCSID = in_.readInt();
        int substitutionCCSID = in_.readInt();
        int severity = in_.readShort();
        int messageTypeLength = in_.readInt();
        int messageType = in_.readShort();
        in_.skipBytes(messageTypeLength-2);
        int messageIDLength = in_.readInt();
        byte[] messageID = new byte[messageIDLength];
        in_.readFully(messageID);
        int messageFileNameLength = in_.readInt();
        byte[] messageFileName = new byte[messageFileNameLength];
        in_.readFully(messageFileName);
        int messageFileLibraryNameLength = in_.readInt();
        byte[] messageFileLibrary = new byte[messageFileLibraryNameLength];
        in_.readFully(messageFileLibrary);
        int messageTextLength = in_.readInt();
        byte[] messageText = new byte[messageTextLength];
        in_.readFully(messageText);
        int messageSubstitutionTextLength = in_.readInt();
        byte[] substitutionData = new byte[messageSubstitutionTextLength];
        in_.readFully(substitutionData);
        int messageHelpLength = in_.readInt();
        byte[] messageHelp = new byte[messageHelpLength];
        in_.readFully(messageHelp);
        // String messageIDString = new String(messageID, "Cp037");
        if (messageID.length > buffer.length) {
            buffer = new char[messageID.length];
        }
        String messageIDString = Conv.ebcdicByteArrayToString(messageID, buffer);

        // String messageTextString = new String(messageText, "Cp037");
        if (messageText.length > buffer.length) {
          buffer = new char[messageText.length];
        }
        String messageTextString = Conv.ebcdicByteArrayToString(messageText, buffer);

        messages[i] = new Message(messageIDString, messageTextString);
        curLength += 40 + messageTypeLength-2 + messageIDLength + messageFileNameLength + messageFileLibraryNameLength + messageTextLength + messageSubstitutionTextLength + messageHelpLength;
      }
      else if (messageCP == 0x1102)
      {
        byte[] messageID = new byte[7];
        in_.readFully(messageID);
        int messageType = in_.readShort();
        int severity = in_.readShort();
        byte[] fileName = new byte[10];
        in_.readFully(fileName);
        byte[] libraryName = new byte[10];
        in_.readFully(libraryName);
        int substitutionDataLength = in_.readShort();
        int textLength = in_.readShort();
        byte[] substitutionData = new byte[substitutionDataLength];
        in_.readFully(substitutionData);
        byte[] text = new byte[textLength];
        in_.readFully(text);
        curLength += 35 + substitutionDataLength + textLength;
        // String messageIDString = new String(messageID, "Cp037");
        if (messageID.length > buffer.length) {
            buffer = new char[messageID.length];
        }
        String messageIDString = Conv.ebcdicByteArrayToString(messageID, buffer);

        // String messageTextString = new String(messageText, "Cp037");
        if (text.length > buffer.length) {
          buffer = new char[text.length];
        }
        String messageTextString = Conv.ebcdicByteArrayToString(text, buffer);


        messages[i] = new Message(messageIDString, messageTextString);
      }
      int remaining = messageLength-(curLength-oldLength);
      in_.skipBytes(remaining);
    }
    in_.skipBytes(length-curLength);
    return messages;
  }

  private static void sendRunCommandRequest(HostOutputStream out, String cmd, int datastreamLevel) throws IOException
  {
    final boolean newerServer = datastreamLevel >= 10;
    final byte[] commandBytes = newerServer ? Conv.stringToUnicodeByteArray(cmd) : Conv.stringToEBCDICByteArray37(cmd);
    out.writeInt(newerServer ? 31 + commandBytes.length : 27 + commandBytes.length); // Length.
    out.writeShort(0); // Header ID.
    out.writeShort(0xE008); // Server ID.
    out.writeInt(0); // CS instance.
    out.writeInt(0); // Correlation ID.
    out.writeShort(1); // Template length.
    out.writeShort(0x1002); // ReqRep ID.

    final int messageOption = newerServer ? 4 : (datastreamLevel < 7 ? 0 : 2); // Always return all messages when possible.
    out.writeByte(messageOption);

    if (newerServer)
    {
      out.writeInt(10+commandBytes.length); // Command LL.
      out.writeShort(0x1104); // Command CP.
      out.writeInt(1200); // Command CCSID.
      out.write(commandBytes);
    }
    else
    {
      out.writeInt(6+commandBytes.length); // Command LL.
      out.writeShort(0x1101); // Command CP.
      out.write(commandBytes);
    }
  }

  private static void sendExchangeAttributesRequest(HostOutputStream out) throws IOException
  {
    out.writeInt(34); // Length.
    out.writeShort(0); // Header ID.
    out.writeShort(0xE008); // Server ID.
    out.writeInt(0); // CS instance.
    out.writeInt(0); // Correlation ID.
    out.writeShort(14); // Template length.
    out.writeShort(0x1001); // ReqRep ID.
    out.writeInt(1200); // CCSID.
    // out.write("2924".getBytes("Cp037")); // NLV.
    out.writeByte(0xF2);
    out.writeByte(0xF9);
    out.writeByte(0xF2);
    out.writeByte(0xF4);
    out.writeInt(1); // Client version.
    out.writeShort(0); // Client datastream level.
  }
}
