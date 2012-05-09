///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  SignonConnection.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite;

import java.io.*;
import java.net.*;

/**
 * Represents a TCP/IP socket connection to the System i Signon host server (QSYSWRK/QZSOSIGN prestart jobs).
**/
public class SignonConnection extends HostServerConnection //implements Connection
{
  /**
   * The default TCP/IP port the Signon host server listens on.
   * If your system has been configured to use a different port, use
   * the {@link PortMapper PortMapper} class to determine the port.
  **/
  public static final int DEFAULT_SIGNON_SERVER_PORT = 8476;

  private SignonConnection(SystemInfo info, Socket socket, HostInputStream in, HostOutputStream out, String user)
  {
    super(info, user, info.getSignonJobName(), socket, in, out);
  }

  protected void sendEndJobRequest() throws IOException
  {
  }

  /**
   * Issues a request to the Signon host server to authenticate the specified user and password.
  **/
  public void authenticate(String user, String password) throws IOException
  {
    if (isClosed()) throw new IOException("Connection closed");

    Object[] ret = getInfo(true, getInfo().getSystem(), out_, in_);
    byte[] clientSeed = (byte[])ret[1];
    byte[] serverSeed = (byte[])ret[2];

    byte[] userBytes = getUserBytes(user);
    byte[] passwordBytes = getPasswordBytes(password);
    password = null;
    byte[] encryptedPassword = getEncryptedPassword(userBytes, passwordBytes, clientSeed, serverSeed, getInfo().getPasswordLevel());

    // Authenticate.
    sendSignonInfoRequest(out_, getInfo(), userBytes, encryptedPassword);
    out_.flush();

    int length = in_.readInt();
    if (length < 20)
    {
      throw DataStreamException.badLength("signonInfo", length);
    }
    in_.skipBytes(16);
    int rc = in_.readInt();
    int numRead = 24;
    try
    {
      if (rc != 0)
      {
        String message = "Bad return code from signon info: 0x"+Integer.toHexString(rc);
        switch (rc)
        {
          case 0x20001:
            message = "User ID is not known.";
            break;
          case 0x3000B:
            message = "Password is incorrect.";
            break;
        }
        throw new IOException(message);
      }
      else
      {
        int serverCCSID = 0;
        boolean foundServerCCSID = false;
        while (numRead < length && !foundServerCCSID)
        {
          int ll = in_.readInt();
          int cp = in_.readShort();
          int currentRead = 0;
          switch (cp)
          {
            case 0x1114:
              serverCCSID = in_.readInt();
              currentRead = 4;
              foundServerCCSID = true;
              break;
          }
          in_.skipBytes(ll-6-currentRead);
          numRead += ll;
        }
        if (foundServerCCSID)
        {
          getInfo().setServerCCSID(serverCCSID);
        }
      }
    }
    finally
    {
      in_.skipBytes(length-numRead);
      in_.end();
    }
  }

  private static Object[] getInfo(boolean doSeeds, String system, HostOutputStream out, HostInputStream in) throws IOException
  {
    Object[] ret = new Object[3];
    final long clientSeedLong = sendSignonExchangeAttributeRequest(out);
    if (doSeeds)
    {
      byte[] clientSeed = Conv.longToByteArray(clientSeedLong);
      ret[1] = clientSeed;
    }
    out.flush();

    int length = in.readInt();
    if (length < 20)
    {
      throw DataStreamException.badLength("signonExchangeAttributes", length);
    }
    in.skipBytes(16);
    int rc = in.readInt();
    if (rc != 0)
    {
      in.skipBytes(length-24);
      throw DataStreamException.badReturnCode("signonExchangeAttributes", rc);
    }
    int curLength = 24;
    int serverVersion = -1;
    boolean foundServerVersion = false;
    int serverLevel = -1;
    boolean foundServerLevel = false;
    boolean foundServerSeed = false;
    int passwordLevel = -1;
    boolean foundPasswordLevel = false;
    String jobName = null;
//        while (curLength < length && !foundServerSeed && !foundPasswordLevel && !foundJobName)
    while (curLength < length && (!foundServerVersion || !foundServerLevel || !foundPasswordLevel || (!doSeeds || (doSeeds && !foundServerSeed))))
    {
      int oldLength = curLength;
      int ll = in.readInt();
      int cp = in.readShort();
      curLength += 6;
      switch (cp)
      {
        case 0x1101:
          serverVersion = in.readInt();
          curLength += 4;
          foundServerVersion = true;
          break;
        case 0x1102:
          serverLevel = in.readShort();
          curLength += 2;
          foundServerLevel = true;
          break;
        case 0x1103:
          if (doSeeds)
          {
            byte[] serverSeed = new byte[ll-6];
            in.readFully(serverSeed);
            ret[2] = serverSeed;
            curLength += ll-6;
            foundServerSeed = true;
          }
          else
          {
            in.skipBytes(ll-6);
            curLength += ll-6;
          }
          break;
        case 0x1119:
          passwordLevel = in.read();
          curLength += 1;
          foundPasswordLevel = true;
          break;
        case 0x111F:
          in.skipBytes(4); // CCSID is always 0.
          curLength += 4;
          byte[] jobBytes = new byte[ll-10];
          in.readFully(jobBytes);
          jobName = Conv.ebcdicByteArrayToString(jobBytes, 0, jobBytes.length);
          curLength += ll-10;
          break;
        default:
          in.skipBytes(ll-6);
          curLength += ll-6;
          break;
      }
      int diff = ll - (curLength - oldLength);
      if (diff > 0)
      {
        in.skipBytes(diff);
      }
    }
    in.skipBytes(length-curLength);
    in.end();

    ret[0] = new SystemInfo(system, serverVersion, serverLevel, passwordLevel, jobName);
    return ret;
  }

  /**
   * Connects to the Signon host server on the default port and authenticates the specified user.
  **/
  public static SignonConnection getConnection(String system, String user, String password) throws IOException
  {
    return getConnection(system, user, password, DEFAULT_SIGNON_SERVER_PORT);
  }

  /**
   * Connects to the Signon host server on the specified port and authenticates the specified user.
  **/
  public static SignonConnection getConnection(String system, String user, String password, int signonPort) throws IOException
  {
    if (signonPort > 0 && signonPort < 65536)
    {
      Socket signonServer = new Socket(system, signonPort);
      InputStream in = signonServer.getInputStream();
      OutputStream out = signonServer.getOutputStream();
      HostOutputStream dout = new HostOutputStream(new BufferedOutputStream(out));
      HostInputStream din = new HostInputStream(new BufferedInputStream(in));

      SystemInfo info = (SystemInfo)getInfo(false, system, dout, din)[0];
      SignonConnection conn = new SignonConnection(info, signonServer, din, dout, user);
      conn.authenticate(user, password);
      return conn;
    }
    else
    {
      throw new IOException("Bad port number: "+signonPort);
    }
  }

  private static long sendSignonExchangeAttributeRequest(HostOutputStream out) throws IOException
  {
    out.writeInt(52); // Length;
    out.writeShort(0); // Header ID (almost always zero for all datastreams).
    out.writeShort(0xE009); // Server ID.
    out.writeInt(0); // CS instance.
    out.writeInt(0); // Correlation ID.
    out.writeShort(0); // Template length.
    out.writeShort(0x7003); // ReqRep ID.
    out.writeInt(10); // Client version LL.
    out.writeShort(0x1101); // Client version CP.
    out.writeInt(1); // Client version.
    out.writeInt(8); // Client datastream level LL.
    out.writeShort(0x1102); // Client datastream level CP.
    out.writeShort(2); // Client datastream level.
    out.writeInt(14); // Client seed LL.
    out.writeShort(0x1103); // Client seed CP.
    long clientSeed = System.currentTimeMillis();
    out.writeLong(clientSeed); // Client seed.
    return clientSeed;
  }

  private static void sendSignonInfoRequest(HostOutputStream out, SystemInfo info, byte[] userBytes, byte[] encryptedPassword) throws IOException
  {
    int total = 37 + encryptedPassword.length + 16;
    final boolean newerServer = info.getServerLevel() >= 5;
    if (newerServer) total += 7;

    out.writeInt(total); // Length.
    out.writeShort(0); // Header ID (almost always zero for all datastreams).
    out.writeShort(0xE009); // Server ID.
    out.writeInt(0); // CS instance.
    out.writeInt(0); // Correlation ID.
    out.writeShort(0x0001); // Template length.
    out.writeShort(0x7004); // ReqRep ID.
    out.writeByte(encryptedPassword.length == 8 ? 1 : 3); // Password encryption type.
    out.writeInt(10); // Client CCSID LL.
    out.writeShort(0x1113); // Client CCSID CP.
    out.writeInt(1200); // Client CCSID (big endian UTF-16).
    out.writeInt(6+encryptedPassword.length); // Password LL.
    out.writeShort(0x1105); // Password CP. 0x1115 is other.
    out.write(encryptedPassword); // Password.
    out.writeInt(16); // User ID LL.
    out.writeShort(0x1104); // User ID CP.
    out.write(userBytes); // User ID.
    if (newerServer)
    {
      out.writeInt(7); // Return error messages LL.
      out.writeShort(0x1128); // Return error messages CP.
      out.writeByte(1); // Return error messages.
    }
  }
}