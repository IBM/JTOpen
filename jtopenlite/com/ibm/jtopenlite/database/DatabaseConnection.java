///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DatabaseConnection.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
//
// Major Change Log
// Version   Date       Description
// -------   ---------- ---------------------------------------
// 1.7       2012.10.04 Moved counting of bytes actually read from the 
//                      datastream to the in_ object.  This removed a lot of 
//                      the counting logic that was added to deal with 
//                      compression. 
//           
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database;

import com.ibm.jtopenlite.*;

import java.io.*;
import java.net.*;

/**
 * Represents a TCP/IP socket connection to the System i Database host server (QUSRWRK/QZDASOINIT job).
**/
public class DatabaseConnection extends HostServerConnection implements OperationalResultBitmap
{
  private final byte[] byteBuffer_ = new byte[1024];
  private char[] charBuffer_ = new char[1024];

  private static final boolean DEBUG = false;

  public static final int DEFAULT_DATABASE_SERVER_PORT = 8471;
private static final int TYPE_CALL = 3;

  private int correlationID_ = 1;
  private int currentRPB_;

  private boolean compress_ = true;

  private int newCorrelationID()
  {
    if (correlationID_ == 0x7FFFFFFF) correlationID_ = 0;
    return ++correlationID_;
  }

  private DatabaseWarningCallback warningCallback_;
  private DatabaseSQLCommunicationsAreaCallback sqlcaCallback_;

  private boolean returnMessageInfo_ = false;

  private DatabaseConnection(SystemInfo info, Socket socket, HostInputStream in, HostOutputStream out, String user, String jobName)
  {
    super(info, user, jobName, socket, in, out);

    // When we run locally on the iSeries, don't use data stream compression, it slows us down.
    InetAddress i = socket.getInetAddress();
    if (i.isLoopbackAddress())
    {
      compress_ = false;
    }
    else
    {
      String sys = info.getSystem();
      if (sys.equalsIgnoreCase("localhost") || sys.equalsIgnoreCase("127.0.0.1"))
      {
        compress_ = false;
      }
      else
      {
        try
        {
          if (i.equals(InetAddress.getLocalHost()))
          {
            compress_ = false;
          }
        }
        catch (Throwable t)
        {
        }
      }
    }
  }

  /**
   * Indicates if the MESSAGE_ID, FIRST_LEVEL_TEXT, and SECOND_LEVEL_TEXT bits are set on
   * the operational result bitmap for a database request.
  **/
  public boolean isMessageInfoReturned()
  {
    return returnMessageInfo_;
  }

  public void setMessageInfoReturned(boolean b)
  {
    returnMessageInfo_ = b;
  }

  public void setDebug(boolean b)
  {
    in_.setDebug(b);
    out_.setDebug(b);
  }

  public void setWarningCallback(DatabaseWarningCallback warningCallback)
  {
    warningCallback_ = warningCallback;
  }

  public void setSQLCommunicationsAreaCallback(DatabaseSQLCommunicationsAreaCallback callback)
  {
    sqlcaCallback_ = callback;
  }

  protected void sendEndJobRequest() throws IOException
  {
    // Header.
    out_.writeInt(40); // Length is 40 if server ID is E004.
    out_.writeShort(0); // Header ID.
    out_.writeShort(0xE004); // Server ID.
    out_.writeInt(0); // CS instance.
    out_.writeInt(0); // Correlation ID.
    out_.writeShort(0); // Template length.
    out_.writeShort(0x1FFF); // ReqRep ID for database server.

    // Template.
    out_.writeInt(0);
    out_.writeInt(0);
    out_.writeInt(0);
    out_.writeInt(0);
    out_.writeInt(0);
  }

  public static DatabaseConnection getConnection(String system, String user, String password) throws IOException
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

  public static DatabaseConnection getConnection(SystemInfo info, String user, String password) throws IOException
  {
    return getConnection(info, user, password, DEFAULT_DATABASE_SERVER_PORT);
  }

  public static DatabaseConnection getConnection(SystemInfo info, String user, String password, int databasePort) throws IOException
  {
    if (databasePort < 0 || databasePort > 65535)
    {
      throw new IOException("Bad database port: "+databasePort);
    }
    DatabaseConnection conn = null;

    Socket databaseServer = new Socket(info.getSystem(), databasePort);
//    databaseServer.setKeepAlive(false);
//    databaseServer.setReceiveBufferSize(8192);
//    databaseServer.setSendBufferSize(8192);
//    databaseServer.setSoLinger(true, 0);
//    databaseServer.setSoTimeout(0);
//    databaseServer.setTcpNoDelay(false);
    databaseServer.setPerformancePreferences(0,1,2);
    InputStream in = databaseServer.getInputStream();
    OutputStream out = databaseServer.getOutputStream();
    try
    {
      // Exchange random seeds.
      HostOutputStream dout = new HostOutputStream(new BufferedOutputStream(out, 1024));
      HostInputStream din = new HostInputStream(new BufferedInputStream(in, 32768));
      String jobName = connect(info, dout, din, 0xE004, user, password);

//      din.setDebug(true);
//      dout.setDebug(true);
      conn = new DatabaseConnection(info, databaseServer, din, dout, user, jobName);
      return conn;
    }
    finally
    {
      if (conn == null)
      {
        in.close();
        out.close();
        databaseServer.close();
      }
    }
  }

  private void readFullReply(String name) throws IOException
  {
    int length = readReplyHeader(name);
    skipBytes(length-40);
    in_.end();
  }

  public int getCurrentRequestParameterBlockID()
  {
    return currentRPB_;
  }

  /**
   * Sets the current RPB handle to use for all actions, excluding those that accept an rpbID as a parameter.
  **/
  public void setCurrentRequestParameterBlockID(final int rpbID)
  {
    currentRPB_ = rpbID;
  }

  /**
   * Sends a request to create an RPB and sets the current RPB ID to be the one specified.
  **/
  public void createRequestParameterBlock(DatabaseCreateRequestParameterBlockAttributes attribs, int rpbID) throws IOException
  {
    sendCreateSQLRPBRequest(attribs, true, rpbID);
    out_.flush();

    readFullReply("createSQLRPB");
    currentRPB_ = rpbID;
  }

  public void deleteRequestParameterBlock(DatabaseDeleteRequestParameterBlockAttributes attribs, int rpbID) throws IOException
  {
    sendDeleteSQLRPBRequest(attribs, rpbID);
    out_.flush();

    readFullReply("deleteSQLRPB");
  }

  /**
   * Sends a request to reset an RPB and sets the current RPB ID to be the one specified.
  **/
  public void resetRequestParameterBlock(DatabaseCreateRequestParameterBlockAttributes attribs, int rpbID) throws IOException
  {
    sendResetSQLRPBRequest(attribs, true, rpbID);
    out_.flush();

    readFullReply("resetSQLRPB");
    currentRPB_ = rpbID;
  }

  public void prepare(DatabasePrepareAttributes attribs) throws IOException
  {
    sendPrepareRequest(attribs);
    out_.flush();

    readFullReply("prepare");
  }

  public void prepareAndDescribe(DatabasePrepareAndDescribeAttributes attribs, DatabaseDescribeCallback listener, DatabaseParameterMarkerCallback pmListener) throws IOException
  {
    sendPrepareAndDescribeRequest(attribs);
    out_.flush();

    parseReply("prepareAndDescribe", listener, null, null, pmListener, null);
  }

  public void prepareAndExecute(DatabasePrepareAndExecuteAttributes attribs, DatabaseDescribeCallback listener) throws IOException
  {
    sendPrepareAndExecuteRequest(attribs);
    out_.flush();

    parseReply("prepareAndExecute", listener, null);
  }

  private void sendPrepareAndExecuteRequest(DatabasePrepareAndExecuteAttributes attribs) throws IOException
  {
    int length = 40;
    int parms = 0;
    if (attribs != null)
    {
      if (attribs.isPrepareStatementNameSet())
      {
        length += getPrepareStatementNameLength(attribs);
        ++parms;
      }
      if (attribs.isSQLStatementTextSet())
      {
        length += getSQLStatementTextLength(attribs);
        ++parms;
      }
      if (attribs.isSQLStatementTypeSet())
      {
        length += getSQLStatementTypeLength(attribs);
        ++parms;
      }
      if (attribs.isPrepareOptionSet())
      {
        length += getPrepareOptionLength(attribs);
        ++parms;
      }
      if (attribs.isOpenAttributesSet())
      {
        length += getOpenAttributesLength(attribs);
        ++parms;
      }
      if (attribs.isDescribeOptionSet())
      {
        ++parms;
        length += getDescribeOptionLength(attribs);
      }
      if (attribs.isCursorNameSet())
      {
        ++parms;
        length += getCursorNameLength(attribs);
      }
      if (attribs.isBlockingFactorSet())
      {
        ++parms;
        length += getBlockingFactorLength(attribs);
      }
      if (attribs.isScrollableCursorFlagSet())
      {
        ++parms;
        length += getScrollableCursorFlagLength(attribs);
      }
      if (attribs.isSQLParameterMarkerDataSet())
      {
        ++parms;
        length += getSQLParameterMarkerDataLength(attribs);
      }
      if (attribs.isSQLExtendedParameterMarkerDataSet())
      {
        ++parms;
        length += getSQLExtendedParameterMarkerDataLength(attribs);
      }
      if (attribs.isSQLParameterMarkerBlockIndicatorSet())
      {
        ++parms;
        length += getSQLParameterMarkerBlockIndicatorLength(attribs);
      }
      if (attribs.isPackageNameSet())
      {
        length += getPackageNameLength(attribs);
        ++parms;
      }
      if (attribs.isPackageLibrarySet())
      {
        length += getPackageLibraryLength(attribs);
        ++parms;
      }
      if (attribs.isTranslateIndicatorSet())
      {
        length += getTranslateIndicatorLength(attribs);
        ++parms;
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        length += getRLECompressedFunctionParametersLength(attribs);
        ++parms;
      }
      if (attribs.isExtendedColumnDescriptorOptionSet())
      {
        length += getExtendedColumnDescriptorOptionLength(attribs);
        ++parms;
      }
      if (attribs.isExtendedSQLStatementTextSet())
      {
        length += getExtendedSQLStatementTextLength(attribs);
        ++parms;
      }
    }

    writeHeader(length, 0x180D);
    writeTemplate(parms);

    if (attribs != null)
    {
      if (attribs.isPrepareStatementNameSet())
      {
        writePrepareStatementName(attribs);
      }
      if (attribs.isSQLStatementTextSet())
      {
        writeSQLStatementText(attribs);
      }
      if (attribs.isSQLStatementTypeSet())
      {
        writeSQLStatementType(attribs);
      }
      if (attribs.isPrepareOptionSet())
      {
        writePrepareOption(attribs);
      }
      if (attribs.isOpenAttributesSet())
      {
        writeOpenAttributes(attribs);
      }
      if (attribs.isDescribeOptionSet())
      {
        writeDescribeOption(attribs);
      }
      if (attribs.isCursorNameSet())
      {
        writeCursorName(attribs);
      }
      if (attribs.isBlockingFactorSet())
      {
        writeBlockingFactor(attribs);
      }
      if (attribs.isScrollableCursorFlagSet())
      {
        writeScrollableCursorFlag(attribs);
      }
      if (attribs.isSQLParameterMarkerDataSet())
      {
        writeSQLParameterMarkerData(attribs);
      }
      if (attribs.isSQLExtendedParameterMarkerDataSet())
      {
        writeSQLExtendedParameterMarkerData(attribs);
      }
      if (attribs.isSQLParameterMarkerBlockIndicatorSet())
      {
        writeSQLParameterMarkerBlockIndicator(attribs);
      }
      if (attribs.isPackageNameSet())
      {
        writePackageName(attribs);
      }
      if (attribs.isPackageLibrarySet())
      {
        writePackageLibrary(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        writeTranslateIndicator(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        writeRLECompressedFunctionParameters(attribs);
      }
      if (attribs.isExtendedColumnDescriptorOptionSet())
      {
        writeExtendedColumnDescriptorOption(attribs);
      }
      if (attribs.isExtendedSQLStatementTextSet())
      {
        writeExtendedSQLStatementText(attribs);
      }
    }
  }

  private int getPackageLibraryLength(AttributePackageLibrary a)
  {
    return 10+a.getPackageLibrary().length();
  }

  private void writePackageLibrary(AttributePackageLibrary a) throws IOException
  {
    String lib = a.getPackageLibrary();
    out_.writeInt(10+lib.length());
    out_.writeShort(0x3801);
    out_.writeShort(37);
    out_.writeShort(lib.length());
    writePadEBCDIC(lib, lib.length(), out_);
  }

  private int getPackageNameLength(AttributePackageName a)
  {
    return 10+a.getPackageName().length();
  }

  private void writePackageName(AttributePackageName a) throws IOException
  {
    String name = a.getPackageName();
    out_.writeInt(10+name.length());
    out_.writeShort(0x3804);
    out_.writeShort(37);
    out_.writeShort(name.length());
    writePadEBCDIC(name, name.length(), out_);
  }

  private int getPrepareStatementNameLength(AttributePrepareStatementName a)
  {
    return 10+a.getPrepareStatementName().length();
  }

  private void writePrepareStatementName(AttributePrepareStatementName a) throws IOException
  {
    String name = a.getPrepareStatementName();
    out_.writeInt(10+name.length());
    out_.writeShort(0x3806);
    out_.writeShort(37);
    out_.writeShort(name.length());
    writePadEBCDIC(name, name.length(), out_);
  }

  private int getSQLStatementTextLength(AttributeSQLStatementText a)
  {
    return 10+(a.getSQLStatementText().length()*2);
//    return 10 + a.getSQLStatementText().length();
  }

  private void writeSQLStatementText(AttributeSQLStatementText a) throws IOException
  {
    String text = a.getSQLStatementText();
    out_.writeInt(10+(text.length()*2));
//    out_.writeInt(10+text.length());
    out_.writeShort(0x3807);
    out_.writeShort(13488);
//    out_.writeShort(37);
//    byte[] b = text.getBytes("Cp037");
//    out_.writeShort(b.length);
//    out_.write(b);
    out_.writeShort(text.length()*2);
    writeStringToUnicodeBytes(text, out_);
  }

  private int getSQLStatementTypeLength(AttributeSQLStatementType a)
  {
    return 8;
  }

  private void writeSQLStatementType(AttributeSQLStatementType a) throws IOException
  {
    out_.writeInt(8);
    out_.writeShort(0x3812);
    out_.writeShort(a.getSQLStatementType());
  }

  private int getPrepareOptionLength(AttributePrepareOption a)
  {
    return 7;
  }

  private void writePrepareOption(AttributePrepareOption a) throws IOException
  {
    out_.writeInt(7);
    out_.writeShort(0x3808);
    out_.writeByte(a.getPrepareOption());
  }

  private int getOpenAttributesLength(AttributeOpenAttributes a)
  {
    return 7;
  }

  private void writeOpenAttributes(AttributeOpenAttributes a) throws IOException
  {
    out_.writeInt(7);
    out_.writeShort(0x3809);
    out_.writeByte(a.getOpenAttributes());
  }

  private int getTranslateIndicatorLength(AttributeTranslateIndicator a)
  {
    return 7;
  }

  private void writeTranslateIndicator(AttributeTranslateIndicator a) throws IOException
  {
    out_.writeInt(7);
    out_.writeShort(0x3805);
    out_.writeByte(a.getTranslateIndicator());
  }

  private int getRLECompressedFunctionParametersLength(AttributeRLECompressedFunctionParameters a)
  {
    return 10+a.getRLECompressedFunctionParameters().length;
  }

  private void writeRLECompressedFunctionParameters(AttributeRLECompressedFunctionParameters a) throws IOException
  {
    byte[] data = a.getRLECompressedFunctionParameters();
    out_.writeInt(10+data.length);
    out_.writeShort(0x3832);
    out_.writeInt(data.length);
    out_.write(data);
  }

  private int getExtendedColumnDescriptorOptionLength(AttributeExtendedColumnDescriptorOption a)
  {
    return 7;
  }

  private void writeExtendedColumnDescriptorOption(AttributeExtendedColumnDescriptorOption a) throws IOException
  {
    out_.writeInt(7);
    out_.writeShort(0x3829);
    out_.writeByte(a.getExtendedColumnDescriptorOption());
  }

  private int getExtendedSQLStatementTextLength(AttributeExtendedSQLStatementText a)
  {
    return 12+(a.getExtendedSQLStatementText().length()*2);
  }

  private void writeExtendedSQLStatementText(AttributeExtendedSQLStatementText a) throws IOException
  {
    String text = a.getExtendedSQLStatementText();
    out_.writeInt(12+(text.length()*2));
    out_.writeShort(0x3831);
    out_.writeShort(13488);
    out_.writeInt(text.length()*2);
    writeStringToUnicodeBytes(text, out_);
  }

  private int getSyncPointCountLength(AttributeSyncPointCount a)
  {
    return 10;
  }

  private void writeSyncPointCount(AttributeSyncPointCount a) throws IOException
  {
    out_.writeInt(10);
    out_.writeShort(0x3816);
    out_.writeInt(a.getSyncPointCount());
  }

  private void sendPrepareRequest(DatabasePrepareAttributes attribs) throws IOException
  {
    int length = 40;
    int parms = 0;
    if (attribs != null)
    {
      if (attribs.isPrepareStatementNameSet())
      {
        length += getPrepareStatementNameLength(attribs);
        ++parms;
      }
      if (attribs.isSQLStatementTextSet())
      {
        length += getSQLStatementTextLength(attribs);
        ++parms;
      }
      if (attribs.isSQLStatementTypeSet())
      {
        length += getSQLStatementTypeLength(attribs);
        ++parms;
      }
      if (attribs.isPrepareOptionSet())
      {
        length += getPrepareOptionLength(attribs);
        ++parms;
      }
      if (attribs.isOpenAttributesSet())
      {
        length += getOpenAttributesLength(attribs);
        ++parms;
      }
      if (attribs.isPackageNameSet())
      {
        length += getPackageNameLength(attribs);
        ++parms;
      }
      if (attribs.isPackageLibrarySet())
      {
        length += getPackageLibraryLength(attribs);
        ++parms;
      }
      if (attribs.isTranslateIndicatorSet())
      {
        length += getTranslateIndicatorLength(attribs);
        ++parms;
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        length += getRLECompressedFunctionParametersLength(attribs);
        ++parms;
      }
      if (attribs.isExtendedColumnDescriptorOptionSet())
      {
        length += getExtendedColumnDescriptorOptionLength(attribs);
        ++parms;
      }
      if (attribs.isExtendedSQLStatementTextSet())
      {
        length += getExtendedSQLStatementTextLength(attribs);
        ++parms;
      }
    }

    writeHeader(length, 0x1800);
    int template = SEND_REPLY_IMMED; 
    writeTemplate(parms, template);

    if (attribs != null)
    {
      if (attribs.isPrepareStatementNameSet())
      {
        writePrepareStatementName(attribs);
      }
      if (attribs.isSQLStatementTextSet())
      {
        writeSQLStatementText(attribs);
      }
      if (attribs.isSQLStatementTypeSet())
      {
        writeSQLStatementType(attribs);
      }
      if (attribs.isPrepareOptionSet())
      {
        writePrepareOption(attribs);
      }
      if (attribs.isOpenAttributesSet())
      {
        writeOpenAttributes(attribs);
      }
      if (attribs.isPackageLibrarySet())
      {
        writePackageLibrary(attribs);
      }
      if (attribs.isPackageNameSet())
      {
        writePackageName(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        writeTranslateIndicator(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        writeRLECompressedFunctionParameters(attribs);
      }
      if (attribs.isExtendedColumnDescriptorOptionSet())
      {
        writeExtendedColumnDescriptorOption(attribs);
      }
      if (attribs.isExtendedSQLStatementTextSet())
      {
        writeExtendedSQLStatementText(attribs);
      }
    }
  }

  private void sendStreamFetchRequest(DatabaseStreamFetchAttributes attribs) throws IOException
  {
    int length = 40;
    int parms = 0;
    if (attribs != null)
    {
      if (attribs.isPrepareStatementNameSet())
      {
        length += getPrepareStatementNameLength(attribs);
        ++parms;
      }
      if (attribs.isSQLStatementTextSet())
      {
        length += getSQLStatementTextLength(attribs);
        ++parms;
      }
      if (attribs.isSQLStatementTypeSet())
      {
        length += getSQLStatementTypeLength(attribs);
        ++parms;
      }
      if (attribs.isPackageNameSet())
      {
        length += getPackageNameLength(attribs);
        ++parms;
      }
      if (attribs.isPackageLibrarySet())
      {
        length += getPackageLibraryLength(attribs);
        ++parms;
      }
      if (attribs.isTranslateIndicatorSet())
      {
        length += getTranslateIndicatorLength(attribs);
        ++parms;
      }
      if (attribs.isSyncPointCountSet())
      {
        length += getSyncPointCountLength(attribs);
        ++parms;
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        length += getRLECompressedFunctionParametersLength(attribs);
        ++parms;
      }
      if (attribs.isExtendedSQLStatementTextSet())
      {
        length += getExtendedSQLStatementTextLength(attribs);
        ++parms;
      }
    }

    writeHeader(length, 0x180C);
    int template = SEND_REPLY_IMMED | RESULT_DATA;
    writeTemplate(parms, template);

    if (attribs != null)
    {
      if (attribs.isPrepareStatementNameSet())
      {
        writePrepareStatementName(attribs);
      }
      if (attribs.isSQLStatementTextSet())
      {
        writeSQLStatementText(attribs);
      }
      if (attribs.isSQLStatementTypeSet())
      {
        writeSQLStatementType(attribs);
      }
      if (attribs.isPackageLibrarySet())
      {
        writePackageLibrary(attribs);
      }
      if (attribs.isPackageNameSet())
      {
        writePackageName(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        writeTranslateIndicator(attribs);
      }
      if (attribs.isSyncPointCountSet())
      {
        writeSyncPointCount(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        writeRLECompressedFunctionParameters(attribs);
      }
      if (attribs.isExtendedSQLStatementTextSet())
      {
        writeExtendedSQLStatementText(attribs);
      }
    }
  }

  private void sendEndStreamFetchRequest(DatabaseEndStreamFetchAttributes attribs) throws IOException
  {
    int length = 40;
    int parms = 0;
    if (attribs != null)
    {
      if (attribs.isTranslateIndicatorSet())
      {
        length += getTranslateIndicatorLength(attribs);
        ++parms;
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        length += getRLECompressedFunctionParametersLength(attribs);
        ++parms;
      }
    }

    writeHeader(length, 0x1813);
    int template = SEND_REPLY_IMMED; // | MESSAGE_ID | FIRST_LEVEL_TEXT | SECOND_LEVEL_TEXT;
    writeTemplate(parms, template);

    if (attribs != null)
    {
      if (attribs.isTranslateIndicatorSet())
      {
        writeTranslateIndicator(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        writeRLECompressedFunctionParameters(attribs);
      }
    }
  }

  private void writeHeader(final int length, final int reqRepID) throws IOException
  {
    out_.writeInt(length); // Length.
//    out_.writeShort(0); // Header ID.
//    out_.writeShort(0xE004); // Server ID.
    out_.writeInt(0x0000E004); // Header ID and Server ID.
    out_.writeInt(0); // CS instance.
    out_.writeInt(newCorrelationID()); // Correlation ID.
    out_.writeShort(20); // Template length.
    out_.writeShort(reqRepID); // ReqRep ID.
  }

  private void writeTemplate(final int parms) throws IOException
  {
    int template = SEND_REPLY_IMMED; // | MESSAGE_ID | FIRST_LEVEL_TEXT | SECOND_LEVEL_TEXT;
    writeTemplate(parms, template);
  }

  private void writeTemplate(final int parms, final int orsBitmap) throws IOException
  {
    writeTemplate(parms, orsBitmap, 0);
  }

  private void writeTemplate(final int parms, final int orsBitmap, final int pmHandle) throws IOException
  {
    writeTemplate(parms, orsBitmap, pmHandle, currentRPB_);
  }

  /** writes the template, adding the SQLCA, REPLY_RELCOMPRESSED, and MESSAGE_ID bits if needed
   * 
   */
  private void writeTemplate(final int parms, final int orsBitmap, final int pmHandle, final int rpbHandle) throws IOException
  {
    int bitmap = sqlcaCallback_ != null ? (orsBitmap | SQLCA) : orsBitmap;
    if (compress_) bitmap = bitmap | REPLY_RLE_COMPRESSED;
    if (returnMessageInfo_) bitmap = bitmap | MESSAGE_ID | FIRST_LEVEL_TEXT | SECOND_LEVEL_TEXT;
    out_.writeInt(bitmap); // Operational result (ORS) bitmap.
    out_.writeInt(0); // Reserved.
//    out_.writeShort(1); // Return ORS handle - after operation completes.
//    out_.writeShort(1); // Fill ORS handle.
    out_.writeInt(0x00010001); // Return ORS handle, Fill ORS handle.
    out_.writeShort(0); // Based on ORS handle.
    out_.writeShort(rpbHandle); // Request parameter block (RPB) handle.
//    out_.writeInt(0x00000001); // Based on ORS handle, Request parameter block (RPB) handle.
    out_.writeShort(pmHandle); // Parameter marker descriptor handle.
    out_.writeShort(parms); // Parameter count.
  }

  private int getSQLParameterMarkerDataLength(AttributeSQLParameterMarkerData a)
  {
    return 6+a.getSQLParameterMarkerData().length;
  }

  private void writeSQLParameterMarkerData(AttributeSQLParameterMarkerData a) throws IOException
  {
    byte[] data = a.getSQLParameterMarkerData();
    out_.writeInt(6+data.length);
    out_.writeShort(0x3811);
    out_.write(data);
  }

  private int getSQLExtendedParameterMarkerDataLength(AttributeSQLExtendedParameterMarkerData a)
  {
    return 6+a.getSQLExtendedParameterMarkerData().length;
  }

  private void writeSQLExtendedParameterMarkerData(AttributeSQLExtendedParameterMarkerData a) throws IOException
  {
    byte[] data = a.getSQLExtendedParameterMarkerData();
    out_.writeInt(6+data.length);
    out_.writeShort(0x381F);
    out_.write(data);
  }

  private int getSQLParameterMarkerBlockIndicatorLength(AttributeSQLParameterMarkerBlockIndicator a)
  {
    return 8;
  }

  private void writeSQLParameterMarkerBlockIndicator(AttributeSQLParameterMarkerBlockIndicator a) throws IOException
  {
    out_.writeInt(8);
    out_.writeShort(0x3814);
    out_.writeShort(a.getSQLParameterMarkerBlockIndicator());
  }

  private void sendFetchRequest(DatabaseFetchAttributes attribs) throws IOException
  {
    int length = 40;
    int parms = 0;
    if (attribs != null)
    {
      if (attribs.isCursorNameSet())
      {
        ++parms;
        length += getCursorNameLength(attribs);
      }
      if (attribs.isVariableFieldCompressionSet())
      {
        ++parms;
        length += getVariableFieldCompressionLength(attribs);
      }
      if (attribs.isBlockingFactorSet())
      {
        ++parms;
        length += getBlockingFactorLength(attribs);
      }
      if (attribs.isFetchScrollOptionSet())
      {
        ++parms;
        length += getFetchScrollOptionLength(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        ++parms;
        length += getTranslateIndicatorLength(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        ++parms;
        length += getRLECompressedFunctionParametersLength(attribs);
      }
      if (attribs.isFetchBufferSizeSet())
      {
        ++parms;
        length += getFetchBufferSizeLength(attribs);
      }
    }

    writeHeader(length, 0x180B);
    // writeTemplate(parms, compress_ ? 0x84040000 : 0x84000000);
    // Note:  The new writeTemplate adds compression if needed.  
    int template = SEND_REPLY_IMMED | RESULT_DATA;
    writeTemplate(parms, template);


    if (attribs != null)
    {
      if (attribs.isCursorNameSet())
      {
        writeCursorName(attribs);
      }
      if (attribs.isVariableFieldCompressionSet())
      {
        writeVariableFieldCompression(attribs);
      }
      if (attribs.isBlockingFactorSet())
      {
        writeBlockingFactor(attribs);
      }
      if (attribs.isFetchScrollOptionSet())
      {
        writeFetchScrollOption(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        writeTranslateIndicator(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        writeRLECompressedFunctionParameters(attribs);
      }
      if (attribs.isFetchBufferSizeSet())
      {
        writeFetchBufferSize(attribs);
      }
    }
  }

  private void sendPackageRequest(DatabasePackageAttributes attribs, boolean createOrDelete) throws IOException
  {
    int length = 40;
    int parms = 0;
    if (attribs != null)
    {
      if (attribs.isPackageNameSet())
      {
        ++parms;
        length += getPackageNameLength(attribs);
      }
      if (attribs.isPackageLibrarySet())
      {
        ++parms;
        length += getPackageLibraryLength(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        ++parms;
        length += getTranslateIndicatorLength(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        ++parms;
        length += getRLECompressedFunctionParametersLength(attribs);
      }
    }

    writeHeader(length, createOrDelete ? 0x180F : 0x1811);
    // writeTemplate(parms, 0xF2000000);

    int template = SEND_REPLY_IMMED | SQLCA; 
    writeTemplate(parms, template);

    
    if (attribs != null)
    {
      if (attribs.isPackageNameSet())
      {
        writePackageName(attribs);
      }
      if (attribs.isPackageLibrarySet())
      {
        writePackageLibrary(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        writeTranslateIndicator(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        writeRLECompressedFunctionParameters(attribs);
      }
    }
  }

  private int getReturnSizeLength(AttributeReturnSize a)
  {
    return 10;
  }

  private void writeReturnSize(AttributeReturnSize a) throws IOException
  {
    out_.writeInt(10);
    out_.writeShort(0x3815);
    out_.writeInt(a.getReturnSize());
  }

  private void sendRetrievePackageRequest(DatabaseRetrievePackageAttributes attribs) throws IOException
  {
    int length = 40;
    int parms = 0;
    if (attribs != null)
    {
      if (attribs.isPackageNameSet())
      {
        ++parms;
        length += getPackageNameLength(attribs);
      }
      if (attribs.isPackageLibrarySet())
      {
        ++parms;
        length += getPackageLibraryLength(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        ++parms;
        length += getTranslateIndicatorLength(attribs);
      }
      if (attribs.isReturnSizeSet())
      {
        ++parms;
        length += getReturnSizeLength(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        ++parms;
        length += getRLECompressedFunctionParametersLength(attribs);
      }
    }

    writeHeader(length, 0x1815);
    // writeTemplate(parms, 0x80100000);
    int template = SEND_REPLY_IMMED | PACKAGE_INFORMATION;
    writeTemplate(parms, template);

    if (attribs != null)
    {
      if (attribs.isPackageNameSet())
      {
        writePackageName(attribs);
      }
      if (attribs.isPackageLibrarySet())
      {
        writePackageLibrary(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        writeTranslateIndicator(attribs);
      }
      if (attribs.isReturnSizeSet())
      {
        writeReturnSize(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        writeRLECompressedFunctionParameters(attribs);
      }
    }
  }

  private int getSQLParameterMarkerDataFormatLength(AttributeSQLParameterMarkerDataFormat a)
  {
    return 6+a.getSQLParameterMarkerDataFormat().length;
  }

  private void writeSQLParameterMarkerDataFormat(AttributeSQLParameterMarkerDataFormat a) throws IOException
  {
    out_.writeInt(getSQLParameterMarkerDataFormatLength(a));
    out_.writeShort(0x3801);
    out_.write(a.getSQLParameterMarkerDataFormat());
  }

  private int getExtendedSQLParameterMarkerDataFormatLength(AttributeExtendedSQLParameterMarkerDataFormat a)
  {
    return 6+a.getExtendedSQLParameterMarkerDataFormat().length;
  }

  private void writeExtendedSQLParameterMarkerDataFormat(AttributeExtendedSQLParameterMarkerDataFormat a) throws IOException
  {
    out_.writeInt(getExtendedSQLParameterMarkerDataFormatLength(a));
    out_.writeShort(0x381E);
    out_.write(a.getExtendedSQLParameterMarkerDataFormat());
  }

  private void sendDeleteDescriptorRequest(DatabaseDeleteDescriptorAttributes attribs, int descriptorHandle) throws IOException
  {
    int length = 40;
    int parms = 0;
    if (attribs != null)
    {
      if (attribs.isTranslateIndicatorSet())
      {
        ++parms;
        length += getTranslateIndicatorLength(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        ++parms;
        length += getRLECompressedFunctionParametersLength(attribs);
      }
    }

    writeHeader(length, 0x1E01);
    // writeTemplate(parms, 0x80800000, descriptorHandle);
    int template = SEND_REPLY_IMMED | PARAMETER_MARKER_FORMAT;
    writeTemplate(parms, template, descriptorHandle);


    if (attribs != null)
    {
      if (attribs.isTranslateIndicatorSet())
      {
        writeTranslateIndicator(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        writeRLECompressedFunctionParameters(attribs);
      }
    }
  }

  private void sendChangeDescriptorRequest(DatabaseChangeDescriptorAttributes attribs, int descriptorHandle) throws IOException
  {
    int length = 40;
    int parms = 0;
    if (attribs != null)
    {
      if (attribs.isSQLParameterMarkerDataFormatSet())
      {
        ++parms;
        length += getSQLParameterMarkerDataFormatLength(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        ++parms;
        length += getTranslateIndicatorLength(attribs);
      }
      if (attribs.isExtendedSQLParameterMarkerDataFormatSet())
      {
        ++parms;
        length += getExtendedSQLParameterMarkerDataFormatLength(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        ++parms;
        length += getRLECompressedFunctionParametersLength(attribs);
      }
    }

    writeHeader(length, 0x1E00);
    // writeTemplate(parms, 0x00040000, descriptorHandle);
    int template = REPLY_RLE_COMPRESSED;
    writeTemplate(parms, template, descriptorHandle);
   

    if (attribs != null)
    {
      if (attribs.isSQLParameterMarkerDataFormatSet())
      {
        writeSQLParameterMarkerDataFormat(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        writeTranslateIndicator(attribs);
      }
      if (attribs.isExtendedSQLParameterMarkerDataFormatSet())
      {
        writeExtendedSQLParameterMarkerDataFormat(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        writeRLECompressedFunctionParameters(attribs);
      }
    }
  }

  private void sendDescribeParameterMarkerRequest(DatabaseDescribeParameterMarkerAttributes attribs) throws IOException
  {
    int length = 40;
    int parms = 0;
    if (attribs != null)
    {
      if (attribs.isPackageNameSet())
      {
        ++parms;
        length += getPackageNameLength(attribs);
      }
      if (attribs.isPackageLibrarySet())
      {
        ++parms;
        length += getPackageLibraryLength(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        ++parms;
        length += getTranslateIndicatorLength(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        ++parms;
        length += getRLECompressedFunctionParametersLength(attribs);
      }
      if (attribs.isPrepareStatementNameSet())
      {
        ++parms;
        length += getPrepareStatementNameLength(attribs);
      }
    }

    writeHeader(length, 0x1802);
    // writeTemplate(parms, 0xF0800000);
    int template = SEND_REPLY_IMMED | PARAMETER_MARKER_FORMAT; // | MESSAGE_ID | FIRST_LEVEL_TEXT | SECOND_LEVEL_TEXT | PARAMETER_MARKER_FORMAT;
    writeTemplate(parms, template);


    if (attribs != null)
    {
      if (attribs.isPackageNameSet())
      {
        writePackageName(attribs);
      }
      if (attribs.isPackageLibrarySet())
      {
        writePackageLibrary(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        writeTranslateIndicator(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        writeRLECompressedFunctionParameters(attribs);
      }
      if (attribs.isPrepareStatementNameSet())
      {
        writePrepareStatementName(attribs);
      }
    }
  }

  private void sendDescribeRequest(DatabaseDescribeAttributes attribs) throws IOException
  {
    int length = 40;
    int parms = 0;
    if (attribs != null)
    {
      if (attribs.isPrepareStatementNameSet())
      {
        ++parms;
        length += getPrepareStatementNameLength(attribs);
      }
      if (attribs.isDescribeOptionSet())
      {
        ++parms;
        length += getDescribeOptionLength(attribs);
      }
      if (attribs.isPackageNameSet())
      {
        ++parms;
        length += getPackageNameLength(attribs);
      }
      if (attribs.isPackageLibrarySet())
      {
        ++parms;
        length += getPackageLibraryLength(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        ++parms;
        length += getTranslateIndicatorLength(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        ++parms;
        length += getRLECompressedFunctionParametersLength(attribs);
      }
    }

    writeHeader(length, 0x1801);
    // writeTemplate(parms, 0x88000000);
    int template = SEND_REPLY_IMMED | DATA_FORMAT;
    writeTemplate(parms, template);

    if (attribs != null)
    {
      if (attribs.isPrepareStatementNameSet())
      {
        writePrepareStatementName(attribs);
      }
      if (attribs.isDescribeOptionSet())
      {
        writeDescribeOption(attribs);
      }
      if (attribs.isPackageNameSet())
      {
        writePackageName(attribs);
      }
      if (attribs.isPackageLibrarySet())
      {
        writePackageLibrary(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        writeTranslateIndicator(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        writeRLECompressedFunctionParameters(attribs);
      }
    }
  }

  private void sendOpenAndDescribeRequest(DatabaseOpenAndDescribeAttributes attribs, int pmDescriptorHandle) throws IOException
  {
    int length = 40;
    int parms = 0;
    if (attribs != null)
    {
      if (attribs.isPrepareStatementNameSet())
      {
        ++parms;
        length += getPrepareStatementNameLength(attribs);
      }
      if (attribs.isCursorNameSet())
      {
        ++parms;
        length += getCursorNameLength(attribs);
      }
      if (attribs.isOpenAttributesSet())
      {
        ++parms;
        length += getOpenAttributesLength(attribs);
      }
      if (attribs.isVariableFieldCompressionSet())
      {
        ++parms;
        length += getVariableFieldCompressionLength(attribs);
      }
      if (attribs.isDescribeOptionSet())
      {
        ++parms;
        length += getDescribeOptionLength(attribs);
      }
      if (attribs.isBlockingFactorSet())
      {
        ++parms;
        length += getBlockingFactorLength(attribs);
      }
      if (attribs.isScrollableCursorFlagSet())
      {
        ++parms;
        length += getScrollableCursorFlagLength(attribs);
      }
      if (attribs.isSQLParameterMarkerDataSet())
      {
        ++parms;
        length += getSQLParameterMarkerDataLength(attribs);
      }
      if (attribs.isSQLExtendedParameterMarkerDataSet())
      {
        ++parms;
        length += getSQLExtendedParameterMarkerDataLength(attribs);
      }
      if (attribs.isSQLParameterMarkerBlockIndicatorSet())
      {
        ++parms;
        length += getSQLParameterMarkerBlockIndicatorLength(attribs);
      }
      if (attribs.isPackageNameSet())
      {
        ++parms;
        length += getPackageNameLength(attribs);
      }
      if (attribs.isPackageLibrarySet())
      {
        ++parms;
        length += getPackageLibraryLength(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        ++parms;
        length += getTranslateIndicatorLength(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        ++parms;
        length += getRLECompressedFunctionParametersLength(attribs);
      }
      if (attribs.isResultSetHoldabilityOptionSet())
      {
        ++parms;
        length += getResultSetHoldabilityOptionLength(attribs);
      }
    }

    writeHeader(length, 0x1804);
//    writeTemplate(parms, 0xF8000000, pmDescriptorHandle);
//    writeTemplate(parms, 0x86040000, pmDescriptorHandle);
//    writeTemplate(parms, 0xFE040000, pmDescriptorHandle);
//    Statement before update to use constants. 
//    writeTemplate(parms, 0xF8040000, pmDescriptorHandle);
    int template = SEND_REPLY_IMMED | DATA_FORMAT | REPLY_RLE_COMPRESSED; // | MESSAGE_ID | FIRST_LEVEL_TEXT | SECOND_LEVEL_TEXT | DATA_FORMAT | REPLY_RLE_COMPRESSED;
    writeTemplate(parms, template, pmDescriptorHandle);


    if (attribs != null)
    {
      if (attribs.isPrepareStatementNameSet())
      {
        writePrepareStatementName(attribs);
      }
      if (attribs.isCursorNameSet())
      {
        writeCursorName(attribs);
      }
      if (attribs.isOpenAttributesSet())
      {
        writeOpenAttributes(attribs);
      }
      if (attribs.isVariableFieldCompressionSet())
      {
        writeVariableFieldCompression(attribs);
      }
      if (attribs.isDescribeOptionSet())
      {
        writeDescribeOption(attribs);
      }
      if (attribs.isBlockingFactorSet())
      {
        writeBlockingFactor(attribs);
      }
      if (attribs.isScrollableCursorFlagSet())
      {
        writeScrollableCursorFlag(attribs);
      }
      if (attribs.isSQLParameterMarkerDataSet())
      {
        writeSQLParameterMarkerData(attribs);
      }
      if (attribs.isSQLExtendedParameterMarkerDataSet())
      {
        writeSQLExtendedParameterMarkerData(attribs);
      }
      if (attribs.isSQLParameterMarkerBlockIndicatorSet())
      {
        writeSQLParameterMarkerBlockIndicator(attribs);
      }
      if (attribs.isPackageNameSet())
      {
        writePackageName(attribs);
      }
      if (attribs.isPackageLibrarySet())
      {
        writePackageLibrary(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        writeTranslateIndicator(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        writeRLECompressedFunctionParameters(attribs);
      }
      if (attribs.isResultSetHoldabilityOptionSet())
      {
        writeResultSetHoldabilityOption(attribs);
      }
    }
  }

  private void sendExecuteOrOpenAndDescribeRequest(DatabaseExecuteOrOpenAndDescribeAttributes attribs) throws IOException
  {
    int length = 40;
    int parms = 0;
    if (attribs != null)
    {
      if (attribs.isPrepareStatementNameSet())
      {
        ++parms;
        length += getPrepareStatementNameLength(attribs);
      }
      if (attribs.isCursorNameSet())
      {
        ++parms;
        length += getCursorNameLength(attribs);
      }
      if (attribs.isOpenAttributesSet())
      {
        ++parms;
        length += getOpenAttributesLength(attribs);
      }
      if (attribs.isDescribeOptionSet())
      {
        ++parms;
        length += getDescribeOptionLength(attribs);
      }
      if (attribs.isBlockingFactorSet())
      {
        ++parms;
        length += getBlockingFactorLength(attribs);
      }
      if (attribs.isScrollableCursorFlagSet())
      {
        ++parms;
        length += getScrollableCursorFlagLength(attribs);
      }
      if (attribs.isSQLParameterMarkerDataSet())
      {
        ++parms;
        length += getSQLParameterMarkerDataLength(attribs);
      }
      if (attribs.isSQLExtendedParameterMarkerDataSet())
      {
        ++parms;
        length += getSQLExtendedParameterMarkerDataLength(attribs);
      }
      if (attribs.isSQLParameterMarkerBlockIndicatorSet())
      {
        ++parms;
        length += getSQLParameterMarkerBlockIndicatorLength(attribs);
      }
      if (attribs.isPackageNameSet())
      {
        ++parms;
        length += getPackageNameLength(attribs);
      }
      if (attribs.isPackageLibrarySet())
      {
        ++parms;
        length += getPackageLibraryLength(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        ++parms;
        length += getTranslateIndicatorLength(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        ++parms;
        length += getRLECompressedFunctionParametersLength(attribs);
      }
      if (attribs.isResultSetHoldabilityOptionSet())
      {
        ++parms;
        length += getResultSetHoldabilityOptionLength(attribs);
      }
    }

    writeHeader(length, 0x1812);
    // writeTemplate(parms, 0x88000000);
    int template = SEND_REPLY_IMMED | DATA_FORMAT;
    writeTemplate(parms, template);

    if (attribs != null)
    {
      if (attribs.isPrepareStatementNameSet())
      {
        writePrepareStatementName(attribs);
      }
      if (attribs.isCursorNameSet())
      {
        writeCursorName(attribs);
      }
      if (attribs.isOpenAttributesSet())
      {
        writeOpenAttributes(attribs);
      }
      if (attribs.isDescribeOptionSet())
      {
        writeDescribeOption(attribs);
      }
      if (attribs.isBlockingFactorSet())
      {
        writeBlockingFactor(attribs);
      }
      if (attribs.isScrollableCursorFlagSet())
      {
        writeScrollableCursorFlag(attribs);
      }
      if (attribs.isSQLParameterMarkerDataSet())
      {
        writeSQLParameterMarkerData(attribs);
      }
      if (attribs.isSQLExtendedParameterMarkerDataSet())
      {
        writeSQLExtendedParameterMarkerData(attribs);
      }
      if (attribs.isSQLParameterMarkerBlockIndicatorSet())
      {
        writeSQLParameterMarkerBlockIndicator(attribs);
      }
      if (attribs.isPackageNameSet())
      {
        writePackageName(attribs);
      }
      if (attribs.isPackageLibrarySet())
      {
        writePackageLibrary(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        writeTranslateIndicator(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        writeRLECompressedFunctionParameters(attribs);
      }
      if (attribs.isResultSetHoldabilityOptionSet())
      {
        writeResultSetHoldabilityOption(attribs);
      }
    }
  }

  private void sendOpenDescribeFetchRequest(DatabaseOpenDescribeFetchAttributes attribs) throws IOException
  {
    int length = 40;
    int parms = 0;
    if (attribs != null)
    {
      if (attribs.isPrepareStatementNameSet())
      {
        ++parms;
        length += getPrepareStatementNameLength(attribs);
      }
      if (attribs.isCursorNameSet())
      {
        ++parms;
        length += getCursorNameLength(attribs);
      }
      if (attribs.isOpenAttributesSet())
      {
        ++parms;
        length += getOpenAttributesLength(attribs);
      }
      if (attribs.isVariableFieldCompressionSet())
      {
        ++parms;
        length += getVariableFieldCompressionLength(attribs);
      }
      if (attribs.isDescribeOptionSet())
      {
        ++parms;
        length += getDescribeOptionLength(attribs);
      }
      if (attribs.isBlockingFactorSet())
      {
        ++parms;
        length += getBlockingFactorLength(attribs);
      }
      if (attribs.isScrollableCursorFlagSet())
      {
        ++parms;
        length += getScrollableCursorFlagLength(attribs);
      }
      if (attribs.isSQLParameterMarkerDataSet())
      {
        ++parms;
        length += getSQLParameterMarkerDataLength(attribs);
      }
      if (attribs.isSQLExtendedParameterMarkerDataSet())
      {
        ++parms;
        length += getSQLExtendedParameterMarkerDataLength(attribs);
      }
      if (attribs.isSQLParameterMarkerBlockIndicatorSet())
      {
        ++parms;
        length += getSQLParameterMarkerBlockIndicatorLength(attribs);
      }
      if (attribs.isPackageNameSet())
      {
        ++parms;
        length += getPackageNameLength(attribs);
      }
      if (attribs.isPackageLibrarySet())
      {
        ++parms;
        length += getPackageLibraryLength(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        ++parms;
        length += getTranslateIndicatorLength(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        ++parms;
        length += getRLECompressedFunctionParametersLength(attribs);
      }
      if (attribs.isResultSetHoldabilityOptionSet())
      {
        ++parms;
        length += getResultSetHoldabilityOptionLength(attribs);
      }
      if (attribs.isFetchScrollOptionSet())
      {
        ++parms;
        length += getFetchScrollOptionLength(attribs);
      }
      if (attribs.isFetchBufferSizeSet())
      {
        ++parms;
        length += getFetchBufferSizeLength(attribs);
      }
    }

    writeHeader(length, 0x180E);
    // writeTemplate(parms, 0x86048000);//0x8C000000);
    int template = SEND_REPLY_IMMED | RESULT_DATA | SQLCA | REPLY_RLE_COMPRESSED | RETURN_RESULT_SET_ATTRIBUTES;
    writeTemplate(parms, template);//0x8C000000);

    if (attribs != null)
    {
      if (attribs.isPrepareStatementNameSet())
      {
        writePrepareStatementName(attribs);
      }
      if (attribs.isCursorNameSet())
      {
        writeCursorName(attribs);
      }
      if (attribs.isOpenAttributesSet())
      {
        writeOpenAttributes(attribs);
      }
      if (attribs.isVariableFieldCompressionSet())
      {
        writeVariableFieldCompression(attribs);
      }
      if (attribs.isDescribeOptionSet())
      {
        writeDescribeOption(attribs);
      }
      if (attribs.isBlockingFactorSet())
      {
        writeBlockingFactor(attribs);
      }
      if (attribs.isScrollableCursorFlagSet())
      {
        writeScrollableCursorFlag(attribs);
      }
      if (attribs.isSQLParameterMarkerDataSet())
      {
        writeSQLParameterMarkerData(attribs);
      }
      if (attribs.isSQLExtendedParameterMarkerDataSet())
      {
        writeSQLExtendedParameterMarkerData(attribs);
      }
      if (attribs.isSQLParameterMarkerBlockIndicatorSet())
      {
        writeSQLParameterMarkerBlockIndicator(attribs);
      }
      if (attribs.isPackageNameSet())
      {
        writePackageName(attribs);
      }
      if (attribs.isPackageLibrarySet())
      {
        writePackageLibrary(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        writeTranslateIndicator(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        writeRLECompressedFunctionParameters(attribs);
      }
      if (attribs.isResultSetHoldabilityOptionSet())
      {
        writeResultSetHoldabilityOption(attribs);
      }
      if (attribs.isFetchScrollOptionSet())
      {
        writeFetchScrollOption(attribs);
      }
      if (attribs.isFetchBufferSizeSet())
      {
        writeFetchBufferSize(attribs);
      }
    }
  }

  public void openAndDescribe(DatabaseOpenAndDescribeAttributes attribs, DatabaseDescribeCallback listener) throws IOException
  {
    openAndDescribe(attribs, 0, listener);
  }

  public void openAndDescribe(DatabaseOpenAndDescribeAttributes attribs, int parameterMarkerDescriptorHandle, DatabaseDescribeCallback listener) throws IOException
  {
    sendOpenAndDescribeRequest(attribs, parameterMarkerDescriptorHandle);
    out_.flush();

    parseReply("openAndDescribe", listener, null);
  }

  public void openDescribeFetch(DatabaseOpenDescribeFetchAttributes attribs, DatabaseDescribeCallback describeListener, DatabaseFetchCallback fetchListener) throws IOException
  {
    sendOpenDescribeFetchRequest(attribs);
    out_.flush();

    parseReply("openDescribeFetch", describeListener, fetchListener);
  }

  public void createPackage(DatabasePackageAttributes attribs) throws IOException
  {
    sendPackageRequest(attribs, true);
    out_.flush();

    parseReply("createPackage", null, null);
  }

  public void deletePackage(DatabasePackageAttributes attribs) throws IOException
  {
    sendPackageRequest(attribs, false);
    out_.flush();

    parseReply("deletePackage", null, null);
  }

  public void retrievePackage(DatabaseRetrievePackageAttributes attribs, DatabasePackageCallback listener) throws IOException
  {
    sendRetrievePackageRequest(attribs);
    out_.flush();

    parseReply("retrievePackage", listener);
  }

  public void describeParameterMarker(DatabaseDescribeParameterMarkerAttributes attribs, DatabaseParameterMarkerCallback callback) throws IOException
  {
    sendDescribeParameterMarkerRequest(attribs);
    out_.flush();

    parseReply("describeParameterMarker", callback);
  }

  public void changeDescriptor(DatabaseChangeDescriptorAttributes attribs, int descriptorHandle) throws IOException
  {
    sendChangeDescriptorRequest(attribs, descriptorHandle);
    out_.flush();

//    parseReply("changeDescriptor", null, null);
  }

  public void deleteDescriptor(DatabaseDeleteDescriptorAttributes attribs, int descriptorHandle) throws IOException
  {
    sendDeleteDescriptorRequest(attribs, descriptorHandle);
    out_.flush();

    parseReply("deleteDescriptor", null, null);
  }

  public void describe(DatabaseDescribeAttributes attribs, DatabaseDescribeCallback listener) throws IOException
  {
    sendDescribeRequest(attribs);
    out_.flush();

    parseReply("describe", listener, null);
  }

  private void parseReply(String datastream, DatabaseParameterMarkerCallback callback) throws IOException
  {
    parseReply(datastream, null, null, null, callback, null);
  }

  private void parseReply(String datastream, DatabasePackageCallback callback) throws IOException
  {
    parseReply(datastream, null, null, callback, null, null);
  }

  private void parseReply(String datastream, DatabaseDescribeCallback describeCallback, DatabaseFetchCallback fetchCallback) throws IOException
  {
    parseReply(datastream, describeCallback, fetchCallback, null, null, null);
  }

  private int readCompressedInt() throws IOException
  {
    int b1 = readCompressedByte();
    int b2 = readCompressedByte();
    int b3 = readCompressedByte();
    int b4 = readCompressedByte();
    return(b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
  }

  private int readCompressedShort() throws IOException
  {
    int b1 = readCompressedByte();
    int b2 = readCompressedByte();
    return(b1 << 8) | b2;
  }

  private void readCompressedFully(final byte[] b) throws IOException
  {
    for (int i=0; i<b.length; ++i)
    {
      b[i] = (byte)readCompressedByte();
    }
  }

  // Reads compressed bytes from the stream.  Returned the number of bytes actually read.
  private void readCompressedFully(final byte[] b, final int off, final int len) throws IOException
  {
    final int stop = off+len;
    for (int i=off; i<stop; ++i)
    {
      b[i] = (byte)readCompressedByte();
    }
  }

  private void skipCompressedBytes(final int num) throws IOException
  {
    for (int i=0; i<num; ++i)
    {
      readCompressedByte();
    }
  }

  private int rleRepeatValue1_ = 0;
  private int rleRepeatValue2_ = 0;
  private int rleRepeatTotal_ = 0;
  private int rleRepeatCount_ = 0;


  private int readCompressedByte() throws IOException
  {
    if (rleRepeatCount_ < rleRepeatTotal_)
    {
      return(rleRepeatCount_++ % 2) == 0 ? rleRepeatValue1_ : rleRepeatValue2_;
    }
    int b1 = in_.readByte();
    if (b1 == 0x1B)
    {
      // Escape byte.
      int b2 = in_.readByte();

      if (b2 == 0x1B)
      {
        // Escape byte.
        return 0x1B;
      }
      else
      {
        // Regular byte -- repeater record.
        rleRepeatValue1_ = b2;
        rleRepeatValue2_ = in_.readByte();
        int b4 = in_.readByte();
        int b5 = in_.readByte();
        rleRepeatTotal_ = ((b4 << 8) | b5)*2;
        rleRepeatCount_ = 1;
        return rleRepeatValue1_;
      }
    }
    else
    {
      // Regular byte.
      return b1;
    }
  }

  private int readInt() throws IOException
  {
    return rleCompression_ ? readCompressedInt() : in_.readInt();
	}

  private int readShort() throws IOException
  {
    return rleCompression_ ? readCompressedShort() : in_.readShort();
  }

  private int readByte() throws IOException
  {
    return rleCompression_ ? readCompressedByte() : in_.readByte();
  }

  private void readFullyReturnCount(byte[] b) throws IOException
  {
    if (rleCompression_)
    {
      readCompressedFully(b);
    }
    else
    {
      in_.readFully(b);
    }
  }

  //
  // Reads len uncompressed bytes from the buffer
  // Returns the number of bytes actually consumed from the
  // input stream.
  //
  private void readFully(byte[] b, int off, int len) throws IOException
  {
    if (rleCompression_)
    {
      readCompressedFully(b, off, len);
    }
    else
    {
      in_.readFully(b, off, len);
    }
  }

  private void skipBytes(int num) throws IOException
  {
    if (rleCompression_)
    {
      skipCompressedBytes(num);
    }
    else
    {
      in_.skipBytes(num);
    }
  }

  private boolean rleCompression_ = false;

  private final byte[] tempIndicator_ = new byte[2];

  private void parseReply(String datastream, DatabaseDescribeCallback describeCallback, DatabaseFetchCallback fetchCallback, DatabasePackageCallback packageCallback, DatabaseParameterMarkerCallback pmCallback, DatabaseLOBDataCallback lobCallback) throws IOException
  {
	in_.resetLatestBytesReceived();  
    int length = readReplyHeader(datastream);
    // int numRead = 40;
    // numRead is now obtained from in_.getLatestBytesReceived
    rleCompression_ = false;
    while (in_.getLatestBytesReceived() < length)
    {
      int ll = readInt();
      int cp = readShort();
      
      if (cp == 0x3832)
      {
        int realLength = readInt();
        rleCompression_ = true;
      }
      else if (cp == 0x3801 && warningCallback_ != null)
      {
        if (ll > 6)
        {
          // Message ID.
          int shortBytesRead = 2; 	
          int ccsid = readShort();
          
          byte[] messageID = new byte[ll-8];
          readFullyReturnCount(messageID);
          if (ll > charBuffer_.length) {
            charBuffer_ = new char[ll];
          }
          warningCallback_.newMessageID(Conv.ebcdicByteArrayToString(messageID, charBuffer_));
          
        }
      }
      else if (cp == 0x3802 && warningCallback_ != null)
      {
        if (ll > 6)
        {
          // First level message text.
          int ccsid = readShort();
          int len = readShort();

          byte[] firstLevelMessageText = new byte[ll-10];
          readFullyReturnCount(firstLevelMessageText);
          if (ll > charBuffer_.length) {
            charBuffer_ = new char[ll];
          }

          warningCallback_.newMessageText(Conv.ebcdicByteArrayToString(firstLevelMessageText, charBuffer_));
        }
      }
      else if (cp == 0x3803 && warningCallback_ != null)
      {
        if (ll > 6)
        {
          // Second level message text.
          int ccsid = readShort();
          int len = readShort();
          byte[] secondLevelMessageText = new byte[ll-10];
          readFullyReturnCount(secondLevelMessageText);
          if (ll > charBuffer_.length) {
            charBuffer_ = new char[ll];
          }
          warningCallback_.newSecondLevelText(Conv.ebcdicByteArrayToString(secondLevelMessageText, charBuffer_));
        }
      }
      else if (cp == 0x3811 && describeCallback != null)
      {
        if (ll > 6)
        {
          // oldNumRead is the number of bytes processed before processing the
          // current datastream parameter.  As such, we subtract 6
          // bytes for the header. 
          int oldNumRead = (int) in_.getLatestBytesReceived() -6 ;
          int virtualRead = oldNumRead + 6;  
          // Extended column descriptors.
          int numColumns = readInt();

          int[] offsets = new int[numColumns];
          int[] lengths = new int[numColumns];
          skipBytes(6); // Reserved.
          virtualRead += 10; 
          
          for (int i=0; i<numColumns; ++i)
          {
            int updateable = readByte();
            int searchable = readByte();
            int attributeBitmap = readShort();
            describeCallback.columnAttributes(i, updateable, searchable,
                                              (attributeBitmap & 0x8000) != 0, // Identity.
                                              (attributeBitmap & 0x4000) == 0, // Generation mode.
                                              (attributeBitmap & 0x2000) != 0, // Part of any index.
                                              (attributeBitmap & 0x1000) != 0, // Lone unique index.
                                              (attributeBitmap & 0x0800) != 0, // Part of unique index.
                                              (attributeBitmap & 0x0400) != 0, // Expression.
                                              (attributeBitmap & 0x0200) != 0, // Primary key.
                                              (attributeBitmap & 0x0100) == 0, // Named.
                                              (attributeBitmap & 0x0080) != 0, // Row ID.
                                              (attributeBitmap & 0x0040) != 0); // Row change timestamp.
            offsets[i] = readInt();
            lengths[i] = readInt();
            readInt(); // Reserved
            virtualRead += 16; 
          }
          for (int i=0; i<numColumns; ++i)
          {
            int base = virtualRead-oldNumRead;
            int toSkip = offsets[i] - base;
            if (toSkip > 0)
            {
              skipBytes(toSkip);
              virtualRead += toSkip; 
            }
            if (lengths[i] >= 8)
            {
              int descRead = 0;
              while (descRead < lengths[i])
              {
                int oldDescRead = descRead;

                int descriptorLength = readInt();
                int codepoint = readShort();
                descRead += 6;
                virtualRead += 6; 
                
                int ccsid = 37;
                int len = descriptorLength-6;
                if (codepoint == 0x3902)
                {
                  ccsid = readShort();
                  descRead += 2;
                  virtualRead += 2; 
                  
                  len = descriptorLength-8;
		  if (ccsid == 65535) {
		      ccsid = 37;
		  }
                }
                readFully(byteBuffer_, 0, len);
                descRead += len;
                virtualRead += len; 
                
                String name = Conv.ebcdicByteArrayToString(byteBuffer_, 0, len, charBuffer_, ccsid);
                switch (codepoint)
                {
                  case 0x3900: describeCallback.baseColumnName(i, name); break;
                  case 0x3901: describeCallback.baseTableName(i, name); break;
                  case 0x3902: describeCallback.columnLabel(i, name); break;
                  case 0x3904: describeCallback.baseSchemaName(i, name); break;
                  case 0x3905: describeCallback.sqlFromTable(i, name); break;
                  case 0x3906: describeCallback.sqlFromSchema(i, name); break;
                }
                int descSkip = descriptorLength-descRead+oldDescRead;
                if (descSkip > 0)
                {
                  skipBytes(descSkip);
                  descRead += descSkip;
                  virtualRead += descSkip; 
                }
              }
            }
          }
          int remaining = ll-virtualRead+oldNumRead;
          skipBytes(remaining);
          virtualRead += remaining; 
        }
      }
      else if (cp == 0x3812)
      {
        if (ll > 6 && describeCallback != null)
        {
          int oldNumRead = (int) in_.getLatestBytesReceived() - 6; 
          int virtualRead = oldNumRead; 
          virtualRead += 6; 
          
          // Super extended data format.
          int consistencyToken = readInt();
          int numFields = readInt();
          int dateFormat = readByte();
          int timeFormat = readByte();
          int dateSeparator = readByte();
          int timeSeparator = readByte();
          int recordSize = readInt();
          
          describeCallback.resultSetDescription(numFields, dateFormat, timeFormat, dateSeparator, timeSeparator, recordSize);
          
          virtualRead += 16;
          
          int[] offsets = new int[numFields];
          int[] lengths = new int[numFields];
          final int fixedLengthRead = 48*numFields;
          for (int i=0; i<numFields; ++i)
          {
        	  
            int fieldLL = readShort();
            int fieldType = readShort();
            int fieldLength = readInt();
            int fieldScale = readShort();
            int fieldPrecision = readShort();
            int fieldCCSID = readShort();
            readByte(); // reserved
            int fieldJoinRefPosition = readShort();
            readInt(); // reserved
            int fieldAttributeBitmap = readByte();
            readInt(); // reserved
            int fieldLOBMaxSize = readInt();
            readShort(); // reserved
            int offsetToVariableLengthInformation = readInt();
            int lengthOfVariableLengthInformation = readInt();
            readInt(); // Reserved
            readInt(); // Reserved 
            virtualRead += 48; 

            
            describeCallback.fieldDescription(i, fieldType, fieldLength, fieldScale, fieldPrecision, fieldCCSID, fieldJoinRefPosition, fieldAttributeBitmap, fieldLOBMaxSize);
            offsets[i] = (48*i)+offsetToVariableLengthInformation-fixedLengthRead;
            lengths[i] = lengthOfVariableLengthInformation;
          }
          int varLengthRead = 0;
          for (int i=0; i<numFields; ++i)
          {
            int toSkip = offsets[i] - varLengthRead;
            skipBytes(toSkip);
            virtualRead += toSkip; 
            varLengthRead += toSkip;         /* Also count the skipped bytes */
        	int variableLength =   lengths[i];
        	while (variableLength > 0) {
        		int varFieldLL = readInt();
				int varFieldCP = readShort();
				int varFieldCCSID = readShort(); // Always 65535?
				
				int varFieldNameLength = varFieldLL - 8;
				readFully(byteBuffer_, 0, varFieldNameLength);
				
				virtualRead+= varFieldLL; 
				String varFieldName = Conv.ebcdicByteArrayToString(
									byteBuffer_, 0, varFieldNameLength,
									charBuffer_);
				switch(varFieldCP) {
					case 0x3840:  describeCallback.fieldName(i, varFieldName); break;
					case 0x3841:  describeCallback.udtName(i, varFieldName); break;
				}
				varLengthRead += varFieldLL;
				// adjusted above by readFully 
				// numRead += varFieldLL;
				variableLength -= varFieldLL;

        	}
          }
          int remaining = ll-virtualRead+oldNumRead;
          skipBytes(remaining);
        }
        else
        {
          skipBytes(ll-6);
        }
      }
      else if (cp == 0x380E && fetchCallback != null)
      {
        int oldNumRead = (int) in_.getLatestBytesReceived() - 6; 
        int virtualRead = oldNumRead + 6; 
        if (virtualRead+20 <= length)
        {
          // Extended result data.
          int consistencyToken = readInt();
          int rowCount = readInt();
          int columnCount = readShort();
          int indicatorSize = readShort();
          readInt(); // reserved
          int rowSize = readInt();
          
          fetchCallback.newResultData(rowCount, columnCount, rowSize);
          virtualRead += 20; 
          final byte[] tempIndicator;
          if (indicatorSize == 0) {
             tempIndicator_[0] = 0;
             tempIndicator_[1] = 0;
             tempIndicator = tempIndicator_;
          } else {
            tempIndicator = indicatorSize == 2 ? tempIndicator_ : new byte[indicatorSize];
          }
          for (int i=0; i<rowCount; ++i)
          {
            for (int j=0; j<columnCount; ++j)
            {
              if (indicatorSize > 0) {
            	  readFullyReturnCount(tempIndicator);
            	  virtualRead += indicatorSize; 
              }
              fetchCallback.newIndicator(i, j, tempIndicator);
            }
          }

          byte[] callbackBuffer = fetchCallback.getTempDataBuffer(rowSize);
          final byte[] tempData = callbackBuffer != null && callbackBuffer.length >= rowSize ? callbackBuffer : new byte[rowSize];
          final int max = ll+oldNumRead;
          for (int i=0; i<rowCount && virtualRead < max; ++i)
          {
        	// Todo.. Think about the numRead calculation as well as the
        	// skip bytes calculation below
            readFully(tempData, 0, rowSize);
            fetchCallback.newRowData(i, tempData);
            virtualRead += rowSize;
          }
        }
        int remaining = ll-(virtualRead-oldNumRead);
        skipBytes(remaining);
      }
      else if (cp == 0x380B && packageCallback != null)
      {
        int oldNumRead = (int) in_.getLatestBytesReceived() - 6;
        int virtualRead = oldNumRead; 
        virtualRead +=6; 
        int packageLength = readInt();
        int packageCCSID = readShort();
        byte[] buf18 = new byte[18];
        readFullyReturnCount(buf18);
        String packageDefaultCollection = Conv.ebcdicByteArrayToString(buf18, charBuffer_);
        int numStatements = readShort();
        int pieceBytesRead = 26;
        virtualRead +=26; 
        
        skipBytes(16); // Reserved.
        virtualRead += 16; 
        packageCallback.newPackageInfo(packageCCSID, packageDefaultCollection, numStatements);

        int packageOffset = 48;
        int[] textOffsets = new int[numStatements];
        int[] textLengths = new int[numStatements];
        int[] formatOffsets = new int[numStatements];
        int[] formatLengths = new int[numStatements];
        int[] parameterMarkerOffsets = new int[numStatements];
        int[] parameterMarkerLengths = new int[numStatements];
        for (int i=0; i<numStatements; ++i)
        {
          int statementNeedsDefaultCollection = readByte();
          int statementType = readShort();
          readFullyReturnCount(buf18);
          String statementName = Conv.ebcdicByteArrayToString(buf18, charBuffer_);
          packageCallback.newStatementInfo(i, statementNeedsDefaultCollection, statementType, statementName);
          skipBytes(19); // Reserved.
          virtualRead += 21 + 19; 
          int formatOffset = readInt();
          int formatLength = readInt();
          formatOffsets[i] = formatOffset;
          formatLengths[i] = formatLength;
          int textOffset = readInt();
          int textLength = readInt();
          textOffsets[i] = textOffset;
          textLengths[i] = textLength;
          int parameterMarkerOffset = readInt();
          int parameterMarkerLength = readInt();
          parameterMarkerOffsets[i] = parameterMarkerOffset;
          parameterMarkerLengths[i] = parameterMarkerLength;
          virtualRead += 24; 
          packageOffset += 64;
        }
        for (int i=0; i<numStatements; ++i)
        {
          if (textLengths[i] > 0)
          {
            int diff = textOffsets[i]-packageOffset;
            skipBytes(diff);
            virtualRead += diff; 
            packageOffset += diff;
            byte[] buf = new byte[textLengths[i]];
            readFullyReturnCount(buf);
            virtualRead += textLengths[i]; 
            packageOffset += textLengths[i];
            String text = Conv.unicodeByteArrayToString(buf, 0, buf.length);
            packageCallback.statementText(i, text);
          }
          if (formatLengths[i] > 0)
          {
            int diff = formatOffsets[i]-packageOffset;
            skipBytes(diff);
            virtualRead += diff; 
            packageOffset += diff;
            byte[] statementFormat = new byte[formatLengths[i]];
            readFullyReturnCount(statementFormat);
            packageOffset += formatLengths[i];
            virtualRead += formatLengths[i]; 
            packageCallback.statementDataFormat(i, statementFormat);
          }
          if (parameterMarkerLengths[i] > 0)
          {
            int diff = parameterMarkerOffsets[i]-packageOffset;
            skipBytes(diff);
            virtualRead += diff; 
            packageOffset += diff;
            byte[] parameterMarkerFormat = new byte[parameterMarkerLengths[i]];
            readFullyReturnCount(parameterMarkerFormat);
            virtualRead += parameterMarkerLengths[i]; 
            packageOffset += parameterMarkerLengths[i];
            packageCallback.statementParameterMarkerFormat(i, parameterMarkerFormat);
          }
        }
        int remaining = ll-virtualRead+oldNumRead;
        skipBytes(remaining);
      }
      else if (cp == 0x3807 && sqlcaCallback_ != null)
      {
        // SQL CA.
        readFully(byteBuffer_, 0, ll-6);
        parseSQLCA();
      }
      else if (cp == 0x3813 && pmCallback != null)
      {
        // Super extended parameter marker format.
        int oldNumRead = (int) in_.getLatestBytesReceived() - 6; 
        // The header for the 3813 is a size of virtual bytes, but may not 
        // match the physical bytes read.  Track the physical and virtual bytes read
        // separately. 
        int virtualRead = oldNumRead;   
        virtualRead += 6; 
        
        if ((oldNumRead + 6)+16 <= length && ll > 6)
        {
          
          int consistencyToken = readInt();
          int numFields = readInt();
          readInt(); // reserved 
          int recordSize = readInt();
          pmCallback.parameterMarkerDescription(numFields, recordSize);
          virtualRead += 16; 
          
          int[] offsets = new int[numFields];
          int[] lengths = new int[numFields];
          final int fixedLengthRead = 48*numFields;
          for (int i=0; i<numFields; ++i)
          {
        	  
            int descLL = readShort();
            int type = readShort();
            int fieldLength = readInt();
            int scale = readShort(); // Scale for numeric, character count for GRAPHIC.
            int precision = readShort();
            int fieldCCSID = readShort();
            int parmType = readByte();
            int joinRefPosition = readShort();
            //Error in LIPI doc.          skipBytes(2); // Reserved.
            int lobLocator = readInt();
            // numRead+= skipBytesReturnCount(5); // Reserved.
            readInt(); // Reserved
            readByte(); // Reserved
            int lobMaxSize = readInt();
            // numRead+= skipBytesReturnCount(2); // Reserved.
            readShort();                          // Reserved 
            int offsetToVariableLengthInformation = readInt(); // Based on start of fixed length info.
            int lengthOfVariableLengthInformation = readInt();
            // numRead +=  skipBytesReturnCount(8); // Reserved.
            readInt() ; // Reserved
            readInt() ; // Reserved
            virtualRead += 48; 
            
            pmCallback.parameterMarkerFieldDescription(i, type, fieldLength, scale, precision, fieldCCSID, parmType, joinRefPosition, lobLocator, lobMaxSize);
            
            offsets[i] = (48*i)+offsetToVariableLengthInformation-fixedLengthRead;
            lengths[i] = lengthOfVariableLengthInformation;
            
          }
          
          int varLengthRead = 0;
          for (int i=0; i<numFields; ++i)
          {
            int toSkip = offsets[i]-varLengthRead;
            skipBytes(toSkip);
            virtualRead += toSkip; 
            
            int varFieldLL = readInt();
            int varFieldCP = readShort();
            int varFieldCCSID = readShort(); // Always 65535?
            
            int varFieldNameLength = lengths[i]-8;
            readFully(byteBuffer_, 0, varFieldNameLength);
            String varFieldName = Conv.ebcdicByteArrayToString(byteBuffer_, 0, varFieldNameLength, charBuffer_);
            if (varFieldCP == 0x3840)
            {
              pmCallback.parameterMarkerFieldName(i, varFieldName);
            }
            else if (varFieldCP == 0x3841)
            {
              pmCallback.parameterMarkerUDTName(i, varFieldName);
            }
            varLengthRead += lengths[i];
            // 
            // Actual bytes read is calculated with readFully
            // numRead += lengths[i];
            // 
            virtualRead += lengths[i]; 
          }
        }
        else
        {
          // Statement was prepared but has no parameter markers.
          pmCallback.parameterMarkerDescription(0, 0);
        }
        int remaining = ll-virtualRead+oldNumRead;
        skipBytes(remaining);
      }
      else if (cp == 0x3810 && lobCallback != null) // LOB data length
      {
        int num = readShort();
        int moreSkip = 0;
        if (num == 0)
        {
          lobCallback.newLOBLength(0);
          moreSkip = 2;
        }
        else if (num == 4)
        {
          int loblen = readInt();
          lobCallback.newLOBLength(loblen);
          moreSkip = 6;
        }
        else
        {
          readShort();
          int upperLen = readInt();
          int lowerLen = readInt();
          long totalLen = ((long)upperLen << 32) | (long)lowerLen;
          lobCallback.newLOBLength(totalLen);
          moreSkip = 12;
        }
        int physicalRead = moreSkip; 
        skipBytes(ll-6-moreSkip);
      }
      else if (cp == 0x380F && lobCallback != null) // LOB data
      {
        int oldNumRead = (int) in_.getLatestBytesReceived() - 6;
        int virtualRead = oldNumRead + 6;  
        if (in_.getLatestBytesReceived()+6 <= length && ll > 6)
        {
          int ccsid = readShort();
          int lobLL = readInt();
          lobCallback.newLOBData(ccsid, lobLL);
          byte[] buffer = lobCallback.getLOBBuffer();
          if (buffer == null || buffer.length == 0)
          {
            int max = 0x00FFFF > lobLL ? lobLL : 0x00FFFF; // 64 kB
            buffer = new byte[max];
            lobCallback.setLOBBuffer(buffer);
          }
          int remainingLob = lobLL;
          int segmentLimit = buffer.length > remainingLob ? remainingLob : buffer.length;
          readFully(buffer, 0, segmentLimit);
          virtualRead += segmentLimit; 
          
          lobCallback.newLOBSegment(buffer, 0, segmentLimit);
          remainingLob -= segmentLimit;
          while (remainingLob > 0)
          {
            segmentLimit = buffer.length > remainingLob ? remainingLob : buffer.length;
            readFully(buffer, 0, segmentLimit);
            virtualRead += segmentLimit; 
            
            lobCallback.newLOBSegment(buffer, 0, segmentLimit);
            remainingLob -= segmentLimit;
          }
        }
        int remaining = ll-virtualRead+oldNumRead;
        skipBytes(remaining);
      }
      else
      {
        skipBytes(ll-6);
      }
    }
    in_.end();
  }

  private void parseSQLCA()
  {
    int sqlCode = Conv.byteArrayToInt(byteBuffer_, 12);
    int updateCount = Conv.byteArrayToInt(byteBuffer_, 104);
    String sqlState = Conv.ebcdicByteArrayToString(byteBuffer_, 131, 5, charBuffer_);
    String generatedKey = Conv.packedDecimalToString(byteBuffer_, 72, 30, 0, charBuffer_);
    int resultSetsCount = Conv.byteArrayToInt(byteBuffer_, 100);
    sqlcaCallback_.newSQLCommunicationsAreaData(sqlCode, sqlState, generatedKey, updateCount, resultSetsCount);
  }

  public void fetch(DatabaseFetchAttributes attribs, DatabaseFetchCallback listener) throws IOException
  {
    sendFetchRequest(attribs);
    out_.flush();

    parseReply("fetch", null, listener);
  }

  public void streamFetch(DatabaseStreamFetchAttributes attribs, DatabaseFetchCallback listener) throws IOException
  {
    sendStreamFetchRequest(attribs);
    out_.flush();

    parseReply("streamFetch", null, listener);
  }

  public void endStreamFetch(DatabaseEndStreamFetchAttributes attribs) throws IOException
  {
    sendEndStreamFetchRequest(attribs);
    out_.flush();

    readFullReply("endStreamFetch");
  }

  public void executeImmediate(DatabaseExecuteImmediateAttributes attribs) throws IOException
  {
    sendExecuteImmediateRequest(attribs);
    out_.flush();

    if (sqlcaCallback_ != null)
    {
      parseReply("executeImmediate", null, null, null, null, null); // In case someone wants generated keys.
    }
    else
    {
      readFullReply("executeImmediate");
    }
  }

  public void execute(DatabaseExecuteAttributes attribs) throws IOException
  {
    execute(attribs, 0);
  }

  public void execute(DatabaseExecuteAttributes attribs, int parameterMarkerDescriptorHandle) throws IOException
  {
    sendExecuteRequest(attribs, parameterMarkerDescriptorHandle);
    out_.flush();

    if (sqlcaCallback_ != null)
    {
      parseReply("execute", null, null, null, null, null); // In case someone wants generated keys.
    }
    else
    {
      readFullReply("execute");
    }
  }

  public void executeOrOpenAndDescribe(DatabaseExecuteOrOpenAndDescribeAttributes attrib, DatabaseDescribeCallback listener) throws IOException
  {
    sendExecuteOrOpenAndDescribeRequest(attrib);
    out_.flush();

    parseReply("executeOrOpenAndDescribe", listener, null);
  }

  private int getScrollableCursorFlagLength(AttributeScrollableCursorFlag a)
  {
    return 8;
  }

  private void writeScrollableCursorFlag(AttributeScrollableCursorFlag a) throws IOException
  {
    out_.writeInt(8);
    out_.writeShort(0x380D);
    out_.writeShort(a.getScrollableCursorFlag());
  }

  private void sendExecuteImmediateRequest(DatabaseExecuteImmediateAttributes attribs) throws IOException
  {
    int length = 40;
    int parms = 0;
    if (attribs != null)
    {
      if (attribs.isSQLStatementTextSet())
      {
        ++parms;
        length += getSQLStatementTextLength(attribs);
      }
      if (attribs.isCursorNameSet())
      {
        ++parms;
        length += getCursorNameLength(attribs);
      }
      if (attribs.isSQLStatementTypeSet())
      {
        ++parms;
        length += getSQLStatementTypeLength(attribs);
      }
      if (attribs.isOpenAttributesSet())
      {
        ++parms;
        length += getOpenAttributesLength(attribs);
      }
      if (attribs.isDescribeOptionSet())
      {
        ++parms;
        length += getDescribeOptionLength(attribs);
      }
      if (attribs.isBlockingFactorSet())
      {
        ++parms;
        length += getBlockingFactorLength(attribs);
      }
      if (attribs.isScrollableCursorFlagSet())
      {
        ++parms;
        length += getScrollableCursorFlagLength(attribs);
      }
      if (attribs.isSQLParameterMarkerDataSet())
      {
        ++parms;
        length += getSQLParameterMarkerDataLength(attribs);
      }
      if (attribs.isSQLExtendedParameterMarkerDataSet())
      {
        ++parms;
        length += getSQLExtendedParameterMarkerDataLength(attribs);
      }
      if (attribs.isSQLParameterMarkerBlockIndicatorSet())
      {
        ++parms;
        length += getSQLParameterMarkerBlockIndicatorLength(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        ++parms;
        length += getTranslateIndicatorLength(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        ++parms;
        length += getRLECompressedFunctionParametersLength(attribs);
      }
      if (attribs.isExtendedSQLStatementTextSet())
      {
        ++parms;
        length += getExtendedSQLStatementTextLength(attribs);
      }
      if (attribs.isPrepareOptionSet()) {
        ++parms;
        length += getPrepareOptionLength(attribs);
      }
    }

    writeHeader(length, 0x1806);
    writeTemplate(parms);

    if (attribs != null)
    {
      if (attribs.isSQLStatementTextSet())
      {
        writeSQLStatementText(attribs);
      }
      if (attribs.isCursorNameSet())
      {
        writeCursorName(attribs);
      }
      if (attribs.isSQLStatementTypeSet())
      {
        writeSQLStatementType(attribs);
      }
      if (attribs.isOpenAttributesSet())
      {
        writeOpenAttributes(attribs);
      }
      if (attribs.isDescribeOptionSet())
      {
        writeDescribeOption(attribs);
      }
      if (attribs.isBlockingFactorSet())
      {
        writeBlockingFactor(attribs);
      }
      if (attribs.isScrollableCursorFlagSet())
      {
        writeScrollableCursorFlag(attribs);
      }
      if (attribs.isSQLParameterMarkerDataSet())
      {
        writeSQLParameterMarkerData(attribs);
      }
      if (attribs.isSQLExtendedParameterMarkerDataSet())
      {
        writeSQLExtendedParameterMarkerData(attribs);
      }
      if (attribs.isSQLParameterMarkerBlockIndicatorSet())
      {
        writeSQLParameterMarkerBlockIndicator(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        writeTranslateIndicator(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        writeRLECompressedFunctionParameters(attribs);
      }
      if (attribs.isExtendedSQLStatementTextSet())
      {
        writeExtendedSQLStatementText(attribs);
      }
      if (attribs.isPrepareOptionSet()) {
        writePrepareOption(attribs);
      }
    }
  }

  private void sendExecuteRequest(DatabaseExecuteAttributes attribs, int pmDescriptorHandle) throws IOException
  {
    int length = 40;
    int parms = 0;
    if (attribs != null)
    {
      if (attribs.isPrepareStatementNameSet())
      {
        ++parms;
        length += getPrepareStatementNameLength(attribs);
      }
      if (attribs.isCursorNameSet())
      {
        ++parms;
        length += getCursorNameLength(attribs);
      }
      if (attribs.isSQLParameterMarkerDataSet())
      {
        ++parms;
        length += getSQLParameterMarkerDataLength(attribs);
      }
      if (attribs.isSQLExtendedParameterMarkerDataSet())
      {
        ++parms;
        length += getSQLExtendedParameterMarkerDataLength(attribs);
      }
      if (attribs.isSQLParameterMarkerBlockIndicatorSet())
      {
        ++parms;
        length += getSQLParameterMarkerBlockIndicatorLength(attribs);
      }
      if (attribs.isPackageNameSet())
      {
        ++parms;
        length += getPackageNameLength(attribs);
      }
      if (attribs.isPackageLibrarySet())
      {
        ++parms;
        length += getPackageLibraryLength(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        ++parms;
        length += getTranslateIndicatorLength(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        ++parms;
        length += getRLECompressedFunctionParametersLength(attribs);
      }


      if (attribs.isSQLStatementTypeSet())
      {
        ++parms;
        length += getSQLStatementTypeLength(attribs);
      }

    }

    writeHeader(length, 0x1805);
    // writeTemplate(parms, 0xF2000000, pmDescriptorHandle);
    int template = SEND_REPLY_IMMED | SQLCA ; 
    writeTemplate(parms, template, pmDescriptorHandle);

    if (attribs != null)
    {
      if (attribs.isPrepareStatementNameSet())
      {
        writePrepareStatementName(attribs);
      }
      if (attribs.isCursorNameSet())
      {
        writeCursorName(attribs);
      }
      if (attribs.isSQLParameterMarkerDataSet())
      {
        writeSQLParameterMarkerData(attribs);
      }
      if (attribs.isSQLExtendedParameterMarkerDataSet())
      {
        writeSQLExtendedParameterMarkerData(attribs);
      }
      if (attribs.isSQLParameterMarkerBlockIndicatorSet())
      {
        writeSQLParameterMarkerBlockIndicator(attribs);
      }
      if (attribs.isPackageNameSet())
      {
        writePackageName(attribs);
      }
      if (attribs.isPackageLibrarySet())
      {
        writePackageLibrary(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        writeTranslateIndicator(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        writeRLECompressedFunctionParameters(attribs);
      }

      if (attribs.isSQLStatementTypeSet())
      {
        writeSQLStatementType(attribs);
      }

    }
  }

  public void closeCursor(DatabaseCloseCursorAttributes attribs) throws IOException
  {
    sendCloseCursorRequest(attribs, true);
    out_.flush();

    readFullReply("closeCursor");
  }


  public void commit() throws IOException {
      sendCommitRequest(out_);
      out_.flush();
      readFullReply("commit");
  }


  public void rollback() throws IOException {
      sendRollbackRequest(out_);
      out_.flush();
      readFullReply("rollback");
  }



  public void setServerAttributes(DatabaseServerAttributes attributes) throws IOException
  {
    sendSetServerAttributesRequest(out_, attributes);
    out_.flush();

    int length = readReplyHeader("setServerAttributes");
    int virtualRead = 40;
    if (length > 46)
    {
      int ll = readInt();
      int cp = readShort();
      if (cp != 0x3804)
      {
        throw DataStreamException.badReply("setServerAttributes-reply", cp);
      }
      if (ll < 122)
      {
        throw DataStreamException.badLength("setServerAttributes-reply", ll);
      }
      int ccsid = readShort();
      int dateFormatParserOption = readShort();
      attributes.setDateFormatParserOption(dateFormatParserOption);
      int dateSeparatorParserOption = readShort();
      attributes.setDateSeparatorParserOption(dateSeparatorParserOption);
      int timeFormatParserOption = readShort();
      attributes.setTimeFormatParserOption(timeFormatParserOption);
      int timeSeparatorParserOption = readShort();
      attributes.setTimeSeparatorParserOption(timeSeparatorParserOption);
      int decimalSeparatorParserOption = readShort();
      attributes.setDecimalSeparatorParserOption(decimalSeparatorParserOption);
      int namingConventionParserOption = readShort();
      attributes.setNamingConventionParserOption(namingConventionParserOption);
      int ignoreDecimalDataErrorParserOption = readShort();
      attributes.setIgnoreDecimalDataErrorParserOption(ignoreDecimalDataErrorParserOption);
      int commitmentControlLevelParserOption = readShort();
      attributes.setCommitmentControlLevelParserOption(commitmentControlLevelParserOption);
      int drdaPackageSize = readShort();
      attributes.setDRDAPackageSize(drdaPackageSize);
      int translationIndicator = readByte();
      attributes.setTranslateIndicator(translationIndicator);
      int serverCCSIDValue = readShort();
      attributes.setServerCCSID(serverCCSIDValue);
      int serverNLSSValue = readShort();
      attributes.setNLSSIdentifier(serverNLSSValue);
      final byte[] buf = new byte[32];
      final char[] cbuf = new char[32];
      readFully(buf, 0, 3);
      String serverLanguageID = Conv.ebcdicByteArrayToString(buf, 0, 3, cbuf);
      attributes.setNLSSIdentifierLanguageID(serverLanguageID);
      readFully(buf, 0, 10);
      String serverLanguageTableName = Conv.ebcdicByteArrayToString(buf, 0, 10, cbuf);
      attributes.setNLSSIdentifierLanguageTableName(serverLanguageTableName);
      readFully(buf, 0, 10);
      String serverLanguageTableLibrary = Conv.ebcdicByteArrayToString(buf, 0, 10, cbuf);
      attributes.setNLSSIdentifierLanguageTableLibrary(serverLanguageTableLibrary);
      readFully(buf, 0, 4);
      String serverLanguageFeatureCode = Conv.ebcdicByteArrayToString(buf, 0, 4, cbuf);
      attributes.setLanguageFeatureCode(serverLanguageFeatureCode);
      readFully(buf, 0, 10);
      String serverFunctionalLevel = Conv.ebcdicByteArrayToString(buf, 0, 10, cbuf);
      attributes.setServerFunctionalLevel(serverFunctionalLevel);
      readFully(buf, 0, 18);
      String relationalDBName = Conv.ebcdicByteArrayToString(buf, 0, 18, cbuf);
      attributes.setRDBName(relationalDBName);
      readFully(buf, 0, 10);
      String defaultSQLLibraryName = Conv.ebcdicByteArrayToString(buf, 0, 10, cbuf);
      attributes.setDefaultSQLLibraryName(defaultSQLLibraryName);
      readFully(buf, 0, 26);
      String jobName = Conv.ebcdicByteArrayToString(buf, 0, 10, cbuf);
      String userName = Conv.ebcdicByteArrayToString(buf, 10, 10, cbuf);
      String jobNumber = Conv.ebcdicByteArrayToString(buf, 20, 6, cbuf);
      attributes.setServerJob(jobName, userName, jobNumber);
      skipBytes(ll-122);
      virtualRead += ll;
    }
    skipBytes(length-virtualRead);
    in_.end();
  }

  public void retrieveLOBData(DatabaseRetrieveLOBDataAttributes attribs, DatabaseLOBDataCallback lobCallback) throws IOException
  {
    sendRetrieveLOBDataRequest(out_, attribs, currentRPB_);
    out_.flush();

    parseReply("retrieveLOBData", null, null, null, null, lobCallback);
  }

  // I don't think this is needed anymore.

/*  private void sendDeleteResultSetRequest() throws IOException
  {
    writeHeader(40, 0x1F01);

    // Write template.
    out_.writeInt(0x00000000); // Operational result (ORS) bitmap.
    out_.writeInt(0); // Reserved.
    out_.writeShort(1); // Return ORS handle - after operation completes.
    out_.writeShort(1); // Fill ORS handle.
    out_.writeShort(0); // Based on ORS handle.
    out_.writeShort(1); // Request parameter block (RPB) handle.
    out_.writeShort(0); // Parameter marker descriptor handle.
    out_.writeShort(0); // Parameter count.
  }
*/

  private void sendDeleteSQLRPBRequest(DatabaseDeleteRequestParameterBlockAttributes attribs, final int rpbID) throws IOException
  {
    int length = 40;
    int parms = 0;
    if (attribs != null)
    {
      if (attribs.isTranslateIndicatorSet())
      {
        length += getTranslateIndicatorLength(attribs);
        ++parms;
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        length += getRLECompressedFunctionParametersLength(attribs);
        ++parms;
      }
    }

    writeHeader(length, 0x1D02);
    // writeTemplate(parms, 0x80000000, 0, rpbID);
    int template = SEND_REPLY_IMMED;
    writeTemplate(parms, template, 0, rpbID);


    // Write template.
/*    out_.writeInt(0x80000000); // Operational result (ORS) bitmap.
    out_.writeInt(0); // Reserved.
    out_.writeShort(1); // Return ORS handle - after operation completes.
    out_.writeShort(1); // Fill ORS handle.
    out_.writeShort(0); // Based on ORS handle.
    out_.writeShort(rpbID); // Request parameter block (RPB) handle.
    out_.writeShort(0); // Parameter marker descriptor handle.
    out_.writeShort(parms); // Parameter count.
*/
    if (attribs != null)
    {
      if (attribs.isTranslateIndicatorSet())
      {
        writeTranslateIndicator(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        writeRLECompressedFunctionParameters(attribs);
      }
    }
  }

  private int getCursorNameLength(AttributeCursorName a)
  {
    return 10+a.getCursorName().length();
  }

  private void writeCursorName(AttributeCursorName a) throws IOException
  {
    String name = a.getCursorName();
    out_.writeInt(10+name.length());
    out_.writeShort(0x380B);
    out_.writeShort(37);
    out_.writeShort(name.length());
    writePadEBCDIC(name, name.length(), out_);
  }

  private int getReuseIndicatorLength(AttributeReuseIndicator a)
  {
    return 7;
  }

  private void writeReuseIndicator(AttributeReuseIndicator a) throws IOException
  {
    out_.writeInt(7);
    out_.writeShort(0x3810);
    out_.write(a.getReuseIndicator());
  }

  private void sendCloseCursorRequest(DatabaseCloseCursorAttributes attribs, final boolean doReply) throws IOException
  {
    int length = 40;
    int parms = 0;
    if (attribs != null)
    {
      if (attribs.isCursorNameSet())
      {
        length += getCursorNameLength(attribs);
        ++parms;
      }
      if (attribs.isReuseIndicatorSet())
      {
        length += getReuseIndicatorLength(attribs);
        ++parms;
      }
      if (attribs.isTranslateIndicatorSet())
      {
        length += getTranslateIndicatorLength(attribs);
        ++parms;
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        length += getRLECompressedFunctionParametersLength(attribs);
        ++parms;
      }
    }

    writeHeader(length, 0x180A);

    // Write template.
    out_.writeInt(doReply ? 0x80000000 : 0x00000000); // Operational result (ORS) bitmap.
    out_.writeInt(0); // Reserved.
    out_.writeShort(1); // Return ORS handle - after operation completes.
    out_.writeShort(1); // Fill ORS handle.
    out_.writeShort(0); // Based on ORS handle.
    out_.writeShort(currentRPB_); // Request parameter block (RPB) handle.
    out_.writeShort(0); // Parameter marker descriptor handle.
    out_.writeShort(parms); // Parameter count.

    if (attribs != null)
    {
      if (attribs.isCursorNameSet())
      {
        writeCursorName(attribs);
      }
      if (attribs.isReuseIndicatorSet())
      {
        writeReuseIndicator(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        writeTranslateIndicator(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        writeRLECompressedFunctionParameters(attribs);
      }
    }
  }

  private int getDescribeOptionLength(AttributeDescribeOption a)
  {
    return 7;
  }

  private void writeDescribeOption(AttributeDescribeOption a) throws IOException
  {
    out_.writeInt(7);
    out_.writeShort(0x380A);
    out_.writeByte(a.getDescribeOption());
  }

  private int getBlockingFactorLength(AttributeBlockingFactor a)
  {
    return 10;
  }

  private void writeBlockingFactor(AttributeBlockingFactor a) throws IOException
  {
    out_.writeInt(10);
    out_.writeShort(0x380C);
    out_.writeInt((int)(a.getBlockingFactor()));
  }

  private int getFetchScrollOptionLength(AttributeFetchScrollOption a)
  {
    int option = a.getFetchScrollOption();
    return(option == 0x0007 || option == 0x0008) ? 12 : 8;
  }

  private void writeFetchScrollOption(AttributeFetchScrollOption a) throws IOException
  {
    int option = a.getFetchScrollOption();
    boolean relative = option == 0x0007 || option == 0x0008;
    out_.writeInt(relative ? 12 : 8);
    out_.writeShort(0x380E);
    out_.writeShort(option);
    if (relative) out_.writeInt(a.getFetchScrollOptionRelativeValue());
  }

  private int getFetchBufferSizeLength(AttributeFetchBufferSize a)
  {
    return 10;
  }

  private void writeFetchBufferSize(AttributeFetchBufferSize a) throws IOException
  {
    out_.writeInt(10);
    out_.writeShort(0x3834);
    out_.writeInt((int)a.getFetchBufferSize());
  }

  private int getHoldIndicatorLength(AttributeHoldIndicator a)
  {
    return 7;
  }

  private void writeHoldIndicator(AttributeHoldIndicator a) throws IOException
  {
    out_.writeInt(7);
    out_.writeShort(0x380F);
    out_.writeByte(a.getHoldIndicator());
  }

  private int getQueryTimeoutLimitLength(AttributeQueryTimeoutLimit a)
  {
    return 10;
  }

  private void writeQueryTimeoutLimit(AttributeQueryTimeoutLimit a) throws IOException
  {
    out_.writeInt(10);
    out_.writeShort(0x3817);
    out_.writeInt(a.getQueryTimeoutLimit());
  }

  private int getServerSideStaticCursorResultSetSizeLength(AttributeServerSideStaticCursorResultSetSize a)
  {
    return 10;
  }

  private void writeServerSideStaticCursorResultSetSize(AttributeServerSideStaticCursorResultSetSize a) throws IOException
  {
    out_.writeInt(10);
    out_.writeShort(0x3827);
    out_.writeInt(a.getServerSideStaticCursorResultSetSize());
  }

  private int getResultSetHoldabilityOptionLength(AttributeResultSetHoldabilityOption a)
  {
    return 7;
  }

  private void writeResultSetHoldabilityOption(AttributeResultSetHoldabilityOption a) throws IOException
  {
    out_.writeInt(7);
    out_.writeShort(0x3830);
    out_.writeByte(a.getResultSetHoldabilityOption());
  }

  private int getVariableFieldCompressionLength(AttributeVariableFieldCompression a)
  {
    return 7;
  }

  private void writeVariableFieldCompression(AttributeVariableFieldCompression a) throws IOException
  {
    out_.writeInt(7);
    out_.writeShort(0x3833);
    out_.writeByte(a.getVariableFieldCompression());
  }

  private int getReturnOptimisticLockingColumnsLength(AttributeReturnOptimisticLockingColumns a)
  {
    return 7;
  }

  private void writeReturnOptimisticLockingColumns(AttributeReturnOptimisticLockingColumns a) throws IOException
  {
    out_.writeInt(7);
    out_.writeShort(0x3835);
    out_.writeByte(a.getReturnOptimisticLockingColumns());
  }

  private void sendCreateSQLRPBRequest(final DatabaseCreateRequestParameterBlockAttributes attribs, final boolean doReply, final int rpbID) throws IOException
  {
    sendSQLRPBRequest(attribs, doReply, rpbID, 0x1D00);
  }

  private void sendResetSQLRPBRequest(final DatabaseCreateRequestParameterBlockAttributes attribs, final boolean doReply, final int rpbID) throws IOException
  {
    sendSQLRPBRequest(attribs, doReply, rpbID, 0x1D04);
  }

  private void sendSQLRPBRequest(final DatabaseCreateRequestParameterBlockAttributes attribs, final boolean doReply, final int rpbID, final int datastream) throws IOException
  {
    int length = 40;
    int parms = 0;
    if (attribs != null)
    {
      if (attribs.isPackageLibrarySet())
      {
        ++parms;
        length += getPackageLibraryLength(attribs);
      }
      if (attribs.isPackageNameSet())
      {
        ++parms;
        length += getPackageNameLength(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        ++parms;
        length += getTranslateIndicatorLength(attribs);
      }
      if (attribs.isPrepareStatementNameSet())
      {
        ++parms;
        length += getPrepareStatementNameLength(attribs);
      }
      if (attribs.isSQLStatementTextSet())
      {
        ++parms;
        length += getSQLStatementTextLength(attribs);
      }
      if (attribs.isPrepareOptionSet())
      {
        ++parms;
        length += getPrepareOptionLength(attribs);
      }
      if (attribs.isOpenAttributesSet())
      {
        ++parms;
        length += getOpenAttributesLength(attribs);
      }
      if (attribs.isDescribeOptionSet())
      {
        ++parms;
        length += getDescribeOptionLength(attribs);
      }
      if (attribs.isCursorNameSet())
      {
        ++parms;
        length += getCursorNameLength(attribs);
      }
      if (attribs.isVariableFieldCompressionSet())
      {
        ++parms;
        length += getVariableFieldCompressionLength(attribs);
      }
      if (attribs.isBlockingFactorSet())
      {
        ++parms;
        length += getBlockingFactorLength(attribs);
      }
      if (attribs.isScrollableCursorFlagSet())
      {
        ++parms;
        length += getScrollableCursorFlagLength(attribs);
      }
      if (attribs.isFetchScrollOptionSet())
      {
        ++parms;
        length += getFetchScrollOptionLength(attribs);
      }
      if (attribs.isHoldIndicatorSet())
      {
        ++parms;
        length += getHoldIndicatorLength(attribs);
      }
      if (attribs.isReuseIndicatorSet())
      {
        ++parms;
        length += getReuseIndicatorLength(attribs);
      }
      if (attribs.isSQLStatementTypeSet())
      {
        ++parms;
        length += getSQLStatementTypeLength(attribs);
      }
      if (attribs.isSQLParameterMarkerBlockIndicatorSet())
      {
        ++parms;
        length += getSQLParameterMarkerBlockIndicatorLength(attribs);
      }
      if (attribs.isQueryTimeoutLimitSet())
      {
        ++parms;
        length += getQueryTimeoutLimitLength(attribs);
      }
      if (attribs.isServerSideStaticCursorResultSetSizeSet())
      {
        ++parms;
        length += getServerSideStaticCursorResultSetSizeLength(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        ++parms;
        length += getRLECompressedFunctionParametersLength(attribs);
      }
      if (attribs.isExtendedColumnDescriptorOptionSet())
      {
        ++parms;
        length += getExtendedColumnDescriptorOptionLength(attribs);
      }
      if (attribs.isResultSetHoldabilityOptionSet())
      {
        ++parms;
        length += getResultSetHoldabilityOptionLength(attribs);
      }
      if (attribs.isExtendedSQLStatementTextSet())
      {
        ++parms;
        length += getExtendedSQLStatementTextLength(attribs);
      }
      if (attribs.isFetchBufferSizeSet())
      {
        ++parms;
        length += getFetchBufferSizeLength(attribs);
      }
      if (attribs.isReturnOptimisticLockingColumnsSet())
      {
        ++parms;
        length += getReturnOptimisticLockingColumnsLength(attribs);
      }
    }

    writeHeader(length, datastream);
    writeTemplate(parms, doReply ? SEND_REPLY_IMMED : 0x00000000, 0, rpbID);

    if (attribs != null)
    {
      if (attribs.isPackageLibrarySet())
      {
        writePackageLibrary(attribs);
      }
      if (attribs.isPackageNameSet())
      {
        writePackageName(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        writeTranslateIndicator(attribs);
      }
      if (attribs.isPrepareStatementNameSet())
      {
        writePrepareStatementName(attribs);
      }
      if (attribs.isSQLStatementTextSet())
      {
        writeSQLStatementText(attribs);
      }
      if (attribs.isPrepareOptionSet())
      {
        writePrepareOption(attribs);
      }
      if (attribs.isOpenAttributesSet())
      {
        writeOpenAttributes(attribs);
      }
      if (attribs.isDescribeOptionSet())
      {
        writeDescribeOption(attribs);
      }
      if (attribs.isCursorNameSet())
      {
        writeCursorName(attribs);
      }
      if (attribs.isVariableFieldCompressionSet())
      {
        writeVariableFieldCompression(attribs);
      }
      if (attribs.isBlockingFactorSet())
      {
        writeBlockingFactor(attribs);
      }
      if (attribs.isScrollableCursorFlagSet())
      {
        writeScrollableCursorFlag(attribs);
      }
      if (attribs.isFetchScrollOptionSet())
      {
        writeFetchScrollOption(attribs);
      }
      if (attribs.isHoldIndicatorSet())
      {
        writeHoldIndicator(attribs);
      }
      if (attribs.isReuseIndicatorSet())
      {
        writeReuseIndicator(attribs);
      }
      if (attribs.isSQLStatementTypeSet())
      {
        writeSQLStatementType(attribs);
      }
      if (attribs.isSQLParameterMarkerBlockIndicatorSet())
      {
        writeSQLParameterMarkerBlockIndicator(attribs);
      }
      if (attribs.isQueryTimeoutLimitSet())
      {
        writeQueryTimeoutLimit(attribs);
      }
      if (attribs.isServerSideStaticCursorResultSetSizeSet())
      {
        writeServerSideStaticCursorResultSetSize(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        writeRLECompressedFunctionParameters(attribs);
      }
      if (attribs.isExtendedColumnDescriptorOptionSet())
      {
        writeExtendedColumnDescriptorOption(attribs);
      }
      if (attribs.isResultSetHoldabilityOptionSet())
      {
        writeResultSetHoldabilityOption(attribs);
      }
      if (attribs.isExtendedSQLStatementTextSet())
      {
        writeExtendedSQLStatementText(attribs);
      }
      if (attribs.isFetchBufferSizeSet())
      {
        writeFetchBufferSize(attribs);
      }
      if (attribs.isReturnOptimisticLockingColumnsSet())
      {
        writeReturnOptimisticLockingColumns(attribs);
      }
    }
  }

  private void sendPrepareAndDescribeRequest(DatabasePrepareAndDescribeAttributes attribs) throws IOException
  {
    int length = 40;
    int parms = 0;
    boolean hasParameterMarkers = true; // To be on the safe side, default to requesting parameter marker info.
    if (attribs != null)
    {
      if (attribs.isPrepareStatementNameSet())
      {
        length += getPrepareStatementNameLength(attribs);
        ++parms;
      }
      if (attribs.isSQLStatementTextSet())
      {
        length += getSQLStatementTextLength(attribs);
        ++parms;
        if (attribs.getSQLStatementText().indexOf("?") < 0) hasParameterMarkers = false;
      }
      if (attribs.isSQLStatementTypeSet())
      {
        length += getSQLStatementTypeLength(attribs);
        ++parms;
      }
      if (attribs.isPrepareOptionSet())
      {
        length += getPrepareOptionLength(attribs);
        ++parms;
      }
      if (attribs.isDescribeOptionSet())
      {
        length += getDescribeOptionLength(attribs);
        ++parms;
      }
      if (attribs.isOpenAttributesSet())
      {
        length += getOpenAttributesLength(attribs);
        ++parms;
      }
      if (attribs.isPackageNameSet())
      {
        length += getPackageNameLength(attribs);
        ++parms;
      }
      if (attribs.isPackageLibrarySet())
      {
        length += getPackageLibraryLength(attribs);
        ++parms;
      }
      if (attribs.isTranslateIndicatorSet())
      {
        length += getTranslateIndicatorLength(attribs);
        ++parms;
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        length += getRLECompressedFunctionParametersLength(attribs);
        ++parms;
      }
      if (attribs.isExtendedColumnDescriptorOptionSet())
      {
        length += getExtendedColumnDescriptorOptionLength(attribs);
        ++parms;
      }
      if (attribs.isExtendedSQLStatementTextSet())
      {
        length += getExtendedSQLStatementTextLength(attribs);
        ++parms;
        if (attribs.getExtendedSQLStatementText().indexOf("?") < 0) hasParameterMarkers = false;
      }
    }

    writeHeader(length, 0x1803);
//    writeTemplate(parms, 0xF8020000);
//    writeTemplate(parms, 0x8A840000);
    //Writing BITMAP --
    // ORS Bitmap:                    Bitmap: 0xFA820000
    // Bit  1: Send Results Immediately
    // Bit  2: Send Message ID
    // Bit  3: Send First Level Text
    // Bit  4: Send Second Level Text
    // Bit  5: Send Data Format
    // Bit  7: Send SQLCA
    // Bit  9: Send Parameter Marker Format
    // Bit 15: Send Extended Column Descriptors
    int template = SEND_REPLY_IMMED | 
    		       DATA_FORMAT ; // | MESSAGE_ID | FIRST_LEVEL_TEXT | SECOND_LEVEL_TEXT | DATA_FORMAT | PARAMETER_MARKER_FORMAT | EXTENDED_COLUMN_DESCRIPTORS;

    
    if (attribs.isSQLStatementTypeSet() && attribs.getSQLStatementType() == TYPE_CALL) {
    	// Do not request column descriptors for call statement
    	// writeTemplate(parms, 0xF8800000);
    } else {
    	// writeTemplate(parms, 0xF8820000); 
    	template |= EXTENDED_COLUMN_DESCRIPTORS;
    }
    if (hasParameterMarkers) template |= PARAMETER_MARKER_FORMAT;
    writeTemplate(parms, template);
    
    if (attribs != null)
    {
      if (attribs.isPrepareStatementNameSet())
      {
        writePrepareStatementName(attribs);
      }
      if (attribs.isSQLStatementTextSet())
      {
        writeSQLStatementText(attribs);
      }
      if (attribs.isSQLStatementTypeSet())
      {
        writeSQLStatementType(attribs);
      }
      if (attribs.isPrepareOptionSet())
      {
        writePrepareOption(attribs);
      }
      if (attribs.isDescribeOptionSet())
      {
        writeDescribeOption(attribs);
      }
      if (attribs.isOpenAttributesSet())
      {
        writeOpenAttributes(attribs);
      }
      if (attribs.isPackageLibrarySet())
      {
        writePackageLibrary(attribs);
      }
      if (attribs.isPackageNameSet())
      {
        writePackageName(attribs);
      }
      if (attribs.isTranslateIndicatorSet())
      {
        writeTranslateIndicator(attribs);
      }
      if (attribs.isRLECompressedFunctionParametersSet())
      {
        writeRLECompressedFunctionParameters(attribs);
      }
      if (attribs.isExtendedColumnDescriptorOptionSet())
      {
        writeExtendedColumnDescriptorOption(attribs);
      }
      if (attribs.isExtendedSQLStatementTextSet())
      {
        writeExtendedSQLStatementText(attribs);
      }
    }
  }

  private int readReplyHeader(String datastream) throws IOException
  {
    // Read header.
    int length = in_.readInt();
    if (length < 40)
    {
      throw DataStreamException.badLength(datastream, length);
    }
    int headerID = in_.readShort();
    int serverID = in_.readShort();
    int csInstance = in_.readInt();
    int correlationID = in_.readInt();
    int templateLength = in_.readShort();
    int reqRepID = in_.readShort();
    if (reqRepID != 0x2800)
    {
//      in_.skipBytes(length-20);
      in_.end();
      throw DataStreamException.badReply(datastream, reqRepID);
    }

    // Read template.
    int orsBitmap = in_.readInt();
    int compressed = in_.readInt(); // First byte counts, last 3 reserved.
    int returnORSHandle = in_.readShort();
    int returnDataFunctionID = in_.readShort();
    int requestDataFunctionID = in_.readShort();
    int rcClass = in_.readShort();
    int rcClassReturnCode = in_.readInt();
    int numRead = 40;
    if (rcClass != 0)
    {
      if (rcClassReturnCode >= 0)
      {
        // Warning.
        if (warningCallback_ != null)
        {
          warningCallback_.newWarning(rcClass, rcClassReturnCode);
        }
      }
//      if (rcClass == 2 && rcClassReturnCode == 0x02BD)
//      {
//        // Last record warning.
//      }
      else
      {
        String msgID = null;
        String msgText = null;
        String msgText2 = null;
        while (numRead < length)
        {
          int ll = in_.readInt();

          int cp = in_.readShort();

          if (cp == 0x3801)
          {
            // Message ID.
            int ccsid = in_.readShort();
            byte[] messageID = new byte[ll-8];
            in_.readFully(messageID);
            if (ll > charBuffer_.length) {
                charBuffer_ = new char[ll];
            }
            msgID = Conv.ebcdicByteArrayToString(messageID, charBuffer_);
          }
          else if (cp == 0x3802)
          {
            // First level message text.
            int ccsid = in_.readShort();
            int len = in_.readShort();
            byte[] firstLevelMessageText = new byte[ll-10];
            in_.readFully(firstLevelMessageText);
            if (ll > charBuffer_.length) {
              charBuffer_ = new char[ll];
          }
            msgText = Conv.ebcdicByteArrayToString(firstLevelMessageText, charBuffer_);
          }
          else if (cp == 0x3803)
          {
            // Second level message text.
            int ccsid = in_.readShort();
            int len = in_.readShort();
            byte[] secondLevelMessageText = new byte[ll-10];
            in_.readFully(secondLevelMessageText);
            if (ll > charBuffer_.length) {
              charBuffer_ = new char[ll];
          }
            msgText2 = Conv.ebcdicByteArrayToString(secondLevelMessageText, charBuffer_);
          }
          else if (cp == 0x3807 && sqlcaCallback_ != null)
          {
            // SQLCA - Communication Area.
            in_.readFully(byteBuffer_, 0, ll-6);
            parseSQLCA();
          }
          else
          {
            skipBytes(ll-6);
          }
          numRead += ll;
        }
        skipBytes(length-numRead);
        in_.end();
        if (msgID != null)
        {
          String text = msgText == null ? "" : msgText;
          text = text + (msgText2 == null ? "" : " "+msgText2);
          throw new MessageException(new Message[] { new Message(msgID, text) });
        }
        throw new DatabaseException(datastream, rcClass, rcClassReturnCode);
      }
    }
    else if (warningCallback_ != null)
    {
      warningCallback_.noWarnings();
    }

    return length;
  }

/*    while (numRead < length)
    {
      int ll = in_.readInt();
      int cp = in_.readShort();
      System.out.println("Reply CP 0x"+Integer.toHexString(cp));
      if (cp == 0x3801)
      {
        int ccsid = in_.readShort();
        byte[] messageID = new byte[ll-8];
        in_.readFully(messageID);
      }
      else if (cp == 0x3802)
      {
        int ccsid = in_.readShort();
        byte[] firstLevelMessageText = new byte[ll-8];
        in_.readFully(firstLevelMessageText);
      }
      else if (cp == 0x3803)
      {
        int ccsid = in_.readShort();
        byte[] secondLevelMessageText = new byte[ll-8];
        in_.readFully(secondLevelMessageText);
      }
      else if (cp == 0x3804)
      {
        int ccsid = in_.readShort();
        System.out.println("CCSID: "+ccsid);
        int dateFormatParserOption = in_.readShort();
        int dateSeparatorParserOption = in_.readShort();
        int timeFormatParserOption = in_.readShort();
        int timeSeparatorParserOption = in_.readShort();
        int decimalSeparatorParserOption = in_.readShort();
        int namingConventionParserOption = in_.readShort();
        int ignoreDecimalDataErrorParserOption = in_.readShort();
        int commitmentControlLevelParserOption = in_.readShort();
        int drdaPackageSize = in_.readShort();
        int translationIndicator = in_.readByte();
        int serverCCSIDValue = in_.readShort();
        int serverNLSSValue = in_.readShort();
        final byte[] buf = new byte[32];
        final char[] cbuf = new char[32];
        in_.readFully(buf, 0, 3);
        String serverLanguageID = Conv.ebcdicByteArrayToString(buf, 0, 3, cbuf);
        System.out.println(serverLanguageID);
        in_.readFully(buf, 0, 10);
        String serverLanguageTableName = Conv.ebcdicByteArrayToString(buf, 0, 10, cbuf);
        System.out.println(serverLanguageTableName);
        in_.readFully(buf, 0, 10);
        String serverLanguageTableLibrary = Conv.ebcdicByteArrayToString(buf, 0, 10, cbuf);
        System.out.println(serverLanguageTableLibrary);
        in_.readFully(buf, 0, 4);
        String serverLanguageFeatureCode = Conv.ebcdicByteArrayToString(buf, 0, 4, cbuf);
        System.out.println(serverLanguageFeatureCode);
        in_.readFully(buf, 0, 10);
        String serverFunctionalLevel = Conv.ebcdicByteArrayToString(buf, 0, 10, cbuf);
        System.out.println(serverFunctionalLevel);
        in_.readFully(buf, 0, 18);
        String relationalDBName = Conv.ebcdicByteArrayToString(buf, 0, 18, cbuf);
        System.out.println(relationalDBName);
        in_.readFully(buf, 0, 10);
        String defaultSQLLibraryName = Conv.ebcdicByteArrayToString(buf, 0, 10, cbuf);
        System.out.println(defaultSQLLibraryName);
        in_.readFully(buf, 0, 26);
        String jobName = Conv.ebcdicByteArrayToString(buf, 0, 10, cbuf);
        String userName = Conv.ebcdicByteArrayToString(buf, 10, 10, cbuf);
        String jobNumber = Conv.ebcdicByteArrayToString(buf, 20, 6, cbuf);
        System.out.println(jobName+"/"+userName+"/"+jobNumber);
        in_.skipBytes(ll-122);
      }
//      else if (cp == 0x3805)
//      {
//        // Data Format.
//      }
//      else if (cp == 0x3806)
//      {
//        // Result Data.
//      }
      else if (cp == 0x3807)
      {
        byte[] sqlCA = new byte[ll-6]; // SQL Communication Area
        in_.readFully(sqlCA);
      }
//      else if (cp == 0x3808)
//      {
//        // Parameter Marker Format.
//      }
//      else if (cp == 0x3809)
//      {
//        // Translation Table Information.
//      }
//      else if (cp == 0x380A)
//      {
//        // Data Source Name (DSN) Attributes
//      }
//      else if (cp == 0x380B)
//      {
//        // Package Return Info.
//      }
//      else if (cp == 0x380C)
//      {
//        // Extended Data Format.
//      }
//      else if (cp == 0x380D)
//      {
//        // Extended Parameter Marker Format.
//      }
//      else if (cp == 0x380E)
//      {
//        // Extended Result Data.
//      }
      else if (cp == 0x380F)
      {
        // LOB Data.
        if (ll > 6)
        {
          int ccsid = in_.readShort();
          int secondLL = in_.readInt();
          byte[] lobData = new byte[secondLL];
          in_.readFully(lobData);
        }
      }
//      else if (cp == 0x3810)
//      {
//        // Current LOB Length.
//      }
//      else if (cp == 0x3811)
//      {
//        // Extended Column Descriptors.
//      }
//      else if (cp == 0x3812)
//      {
//        // Super Extended Data Format.
//      }
//      else if (cp == 0x3813)
//      {
//        // Super Extended Parameter Marker Format.
//      }
      else if (cp == 0x3814)
      {
        // Cursor Attributes.
        int cursorHoldability = in_.readByte();
        int cursorScrollability = in_.readByte();
        int cursorConcurrency = in_.readByte();
        int cursorSensitivity = in_.readByte();
        in_.skipBytes(ll-10);
      }
//      else if (cp == 0x3832)
//      {
//        // RLE Compressed function parameters.
//      }
      else
      {
        in_.skipBytes(ll-6);
      }
      numRead += ll;
    }
    in_.skipBytes(length-numRead);
  }
*/

  private void writeLOBLocatorHandle(AttributeLOBLocatorHandle attrib) throws IOException
  {
    out_.writeInt(10);
    out_.writeShort(0x3818);
    out_.writeInt(attrib.getLOBLocatorHandle());
  }

  private void writeRequestedSize(AttributeRequestedSize attrib) throws IOException
  {
    out_.writeInt(10);
    out_.writeShort(0x3819);
    out_.writeInt(attrib.getRequestedSize());
  }

  private void writeStartOffset(AttributeStartOffset attrib) throws IOException
  {
    out_.writeInt(10);
    out_.writeShort(0x381A);
    out_.writeInt(attrib.getStartOffset());
  }

  private void writeCompressionIndicator(AttributeCompressionIndicator attrib) throws IOException
  {
    out_.writeInt(7);
    out_.writeShort(0x381B);
    out_.write(attrib.getCompressionIndicator());
  }

  private void writeReturnCurrentLengthIndicator(AttributeReturnCurrentLengthIndicator attrib) throws IOException
  {
    out_.writeInt(7);
    out_.writeShort(0x3821);
    out_.write(attrib.getReturnCurrentLengthIndicator());
  }

  private void writeColumnIndex(AttributeColumnIndex attrib) throws IOException
  {
    out_.writeInt(10);
    out_.writeShort(0x3828);
    out_.writeInt(attrib.getColumnIndex());
  }

  private void sendRetrieveLOBDataRequest(HostOutputStream out, DatabaseRetrieveLOBDataAttributes attribs, int rpbID) throws IOException
  {
    int length = 40;
    int parms = 0;
    if (attribs.isLOBLocatorHandleSet())
    {
      length += 10; ++parms;
    }
    if (attribs.isRequestedSizeSet())
    {
      length += 10; ++parms;
    }
    if (attribs.isStartOffsetSet())
    {
      length += 10; ++parms;
    }
    if (attribs.isCompressionIndicatorSet())
    {
      length += 7; ++parms;
    }
    if (attribs.isTranslateIndicatorSet())
    {
      length += 7; ++parms;
    }
    if (attribs.isReturnCurrentLengthIndicatorSet())
    {
      length += 7; ++parms;
    }
    if (attribs.isColumnIndexSet())
    {
      length += 10; ++parms;
    }
    if (attribs.isRLECompressedFunctionParametersSet())
    {
      ++parms;
      byte[] comp = attribs.getRLECompressedFunctionParameters();
      length += 10 + comp.length;
    }

    writeHeader(length, 0x1816);
    // writeTemplate(parms, 0xFC000000, 0, rpbID);
    int template = SEND_REPLY_IMMED | DATA_FORMAT | RESULT_DATA; // | MESSAGE_ID | FIRST_LEVEL_TEXT | SECOND_LEVEL_TEXT | DATA_FORMAT | RESULT_DATA;
    writeTemplate(parms, template, 0, rpbID);

    if (attribs.isLOBLocatorHandleSet())
    {
      writeLOBLocatorHandle(attribs);
    }
    if (attribs.isRequestedSizeSet())
    {
      writeRequestedSize(attribs);
    }
    if (attribs.isStartOffsetSet())
    {
      writeStartOffset(attribs);
    }
    if (attribs.isCompressionIndicatorSet())
    {
      writeCompressionIndicator(attribs);
    }
    if (attribs.isTranslateIndicatorSet())
    {
      writeTranslateIndicator(attribs);
    }
    if (attribs.isReturnCurrentLengthIndicatorSet())
    {
      writeReturnCurrentLengthIndicator(attribs);
    }
    if (attribs.isColumnIndexSet())
    {
      writeColumnIndex(attribs);
    }
    if (attribs.isRLECompressedFunctionParametersSet())
    {
      writeRLECompressedFunctionParameters(attribs);
    }
  }

  private void sendSetServerAttributesRequest(HostOutputStream out, DatabaseServerAttributes attribs) throws IOException
  {
    // Calculate total length.
    int length = 40;
    int parms = 0;
    if (attribs.isDefaultClientCCSIDSet())
    {
      length += 8; ++parms;
    }
    if (attribs.isLanguageFeatureCodeSet())
    {
      length += 12; ++parms;
    }
    if (attribs.isClientFunctionalLevelSet())
    {
      length += 18; ++parms;
    }
    if (attribs.isNLSSIdentifierSet())
    {
      ++parms;
      int val = attribs.getNLSSIdentifier();
      length += 8;
      if (val == 1 || val == 2)
      {
        length += 5;
      }
      else if (val == 3)
      {
        length += 6;
        String tableName = attribs.getNLSSIdentifierLanguageTableName();
        length += tableName.length();
        String tableLibrary = attribs.getNLSSIdentifierLanguageTableLibrary();
        length += tableLibrary.length();
      }
    }
    if (attribs.isTranslateIndicatorSet())
    {
      length += getTranslateIndicatorLength(attribs); ++parms;
    }
    if (attribs.isDRDAPackageSizeSet())
    {
      length += 8; ++parms;
    }
    if (attribs.isDateFormatParserOptionSet())
    {
      length += 8; ++parms;
    }
    if (attribs.isDateSeparatorParserOptionSet())
    {
      length += 8; ++parms;
    }
    if (attribs.isTimeFormatParserOptionSet())
    {
      length += 8; ++parms;
    }
    if (attribs.isTimeSeparatorParserOptionSet())
    {
      length += 8; ++parms;
    }
    if (attribs.isDecimalSeparatorParserOptionSet())
    {
      length += 8; ++parms;
    }
    if (attribs.isNamingConventionParserOptionSet())
    {
      length += 8; ++parms;
    }
    if (attribs.isIgnoreDecimalDataErrorParserOptionSet())
    {
      length += 8; ++parms;
    }
    if (attribs.isCommitmentControlLevelParserOptionSet())
    {
      length += 8; ++parms;
    }
    if (attribs.isDefaultSQLLibraryNameSet())
    {
      ++parms;
      String library = attribs.getDefaultSQLLibraryName();
      length += 10 + library.length();
    }
    if (attribs.isASCIICCSIDForTranslationTableSet())
    {
      length += 8; ++parms;
    }
    if (attribs.isAmbiguousSelectOptionSet())
    {
      length += 8; ++parms;
    }
    if (attribs.isPackageAddStatementAllowedSet())
    {
      length += 8; ++parms;
    }
    if (attribs.isUseExtendedFormatsSet())
    {
      length += 7; ++parms;
    }
    if (attribs.isLOBFieldThresholdSet())
    {
      length += 10; ++parms;
    }
    if (attribs.isDataCompressionParameterSet())
    {
      length += 8; ++parms;
    }
    if (attribs.isTrueAutoCommitIndicatorSet())
    {
      length += 7; ++parms;
    }
    if (attribs.isClientSupportInformationSet())
    {
      length += 10; ++parms;
    }
    if (attribs.isRDBNameSet())
    {
      ++parms;
      String name = attribs.getRDBName();
      length += 8 + name.length();
    }
    if (attribs.isDecimalPrecisionAndScaleAttributesSet())
    {
      length += 12; ++parms;
    }
    if (attribs.isHexadecimalConstantParserOptionSet())
    {
      length += 7; ++parms;
    }
    if (attribs.isInputLocatorTypeSet())
    {
      length += 7; ++parms;
    }
    if (attribs.isLocatorPersistenceSet())
    {
      length += 8; ++parms;
    }
    if (attribs.isEWLMCorrelatorSet())
    {
      ++parms;
      byte[] corr = attribs.getEWLMCorrelator();
      length += 6 + corr.length;
    }
    if (attribs.isRLECompressionSet())
    {
      ++parms;
      byte[] comp = attribs.getRLECompression();
      length += 10 + comp.length;
    }
    if (attribs.isOptimizationGoalIndicatorSet())
    {
      length += 7; ++parms;
    }
    if (attribs.isQueryStorageLimitSet())
    {
      length += 10; ++parms;
    }
    if (attribs.isDecimalFloatingPointRoundingModeOptionSet())
    {
      length += 8; ++parms;
    }
    if (attribs.isDecimalFloatingPointErrorReportingOptionSet())
    {
      length += 8; ++parms;
    }
    if (attribs.isClientAccountingInformationSet())
    {
      ++parms;
      String info = attribs.getClientAccountingInformation();
      length += 10 + info.length();
    }
    if (attribs.isClientApplicationNameSet())
    {
      ++parms;
      String name = attribs.getClientApplicationName();
      length += 10 + name.length();
    }
    if (attribs.isClientUserIdentifierSet())
    {
      ++parms;
      String user = attribs.getClientUserIdentifier();
      length += 10 + user.length();
    }
    if (attribs.isClientWorkstationNameSet())
    {
      ++parms;
      String name = attribs.getClientWorkstationName();
      length += 10 + name.length();
    }
    if (attribs.isClientProgramIdentifierSet())
    {
      ++parms;
      String prog = attribs.getClientProgramIdentifier();
      length += 10 + prog.length();
    }
    if (attribs.isInterfaceTypeSet())
    {
      ++parms;
      String type = attribs.getInterfaceType();
      length += 10 + type.length();
    }
    if (attribs.isInterfaceNameSet())
    {
      ++parms;
      String name = attribs.getInterfaceName();
      length += 10 + name.length();
    }
    if (attribs.isInterfaceLevelSet())
    {
      ++parms;
      String level = attribs.getInterfaceLevel();
      length += 10 + level.length();
    }
    if (attribs.isCloseOnEOFSet())
    {
      length += 7; ++parms;
    }

    writeHeader(length, 0x1F80);

    // Write template.
    out.writeInt(0x81000000); // Operational result (ORS) bitmap - return data + server attributes (no RLE compression).
    out.writeInt(0); // Reserved.
    out.writeShort(0); // Return ORS handle - after operation completes.
    out.writeShort(0); // Fill ORS handle.
    out.writeShort(0); // Based on ORS handle.
    out.writeShort(0); // Request parameter block (RPB) handle.
    out.writeShort(0); // Parameter marker descriptor handle.
    out.writeShort(parms); // Parameter count.


    // Write parameters.
    if (attribs.isDefaultClientCCSIDSet())
    {
      out.writeInt(8);
      out.writeShort(0x3801);
      out.writeShort(attribs.getDefaultClientCCSID());
    }
    if (attribs.isLanguageFeatureCodeSet())
    {
      out.writeInt(12);
      out.writeShort(0x3802);
      out.writeShort(37);
      writePadEBCDIC(attribs.getLanguageFeatureCode(), 4, out);
    }
    if (attribs.isClientFunctionalLevelSet())
    {
      out.writeInt(18);
      out.writeShort(0x3803);
      out.writeShort(37);
      writePadEBCDIC(attribs.getClientFunctionalLevel(), 10, out);
    }
    if (attribs.isNLSSIdentifierSet())
    {
      int val = attribs.getNLSSIdentifier();
      int ll = 8;
      if (val == 1 || val == 2)
      {
        ll += 5;
      }
      else if (val == 3)
      {
        ll += 6;
        String tableName = attribs.getNLSSIdentifierLanguageTableName();
        ll += tableName.length();
        String tableLibrary = attribs.getNLSSIdentifierLanguageTableLibrary();
        ll += tableLibrary.length();
      }
      out.writeInt(ll);
      out.writeShort(0x3804);
      out.writeShort(val);
      if (val == 1 || val == 2)
      {
        out.writeShort(37);
        writePadEBCDIC(attribs.getNLSSIdentifierLanguageID(), 3, out);
      }
      else if (val == 3)
      {
        out.writeShort(37);
        String tableName = attribs.getNLSSIdentifierLanguageTableName();
        out.writeShort(tableName.length());
        writePadEBCDIC(tableName, tableName.length(), out);
        String tableLibrary = attribs.getNLSSIdentifierLanguageTableLibrary();
        out.writeShort(tableLibrary.length());
        writePadEBCDIC(tableLibrary, tableLibrary.length(), out);
      }
    }
    if (attribs.isTranslateIndicatorSet())
    {
      writeTranslateIndicator(attribs);
    }
    if (attribs.isDRDAPackageSizeSet())
    {
      out.writeInt(8);
      out.writeShort(0x3806);
      out.writeShort(attribs.getDRDAPackageSize());
    }
    if (attribs.isDateFormatParserOptionSet())
    {
      out.writeInt(8);
      out.writeShort(0x3807);
      out.writeShort(attribs.getDateFormatParserOption());
    }
    if (attribs.isDateSeparatorParserOptionSet())
    {
      out.writeInt(8);
      out.writeShort(0x3808);
      out.writeShort(attribs.getDateSeparatorParserOption());
    }
    if (attribs.isTimeFormatParserOptionSet())
    {
      out.writeInt(8);
      out.writeShort(0x3809);
      out.writeShort(attribs.getTimeFormatParserOption());
    }
    if (attribs.isTimeSeparatorParserOptionSet())
    {
      out.writeInt(8);
      out.writeShort(0x380A);
      out.writeShort(attribs.getTimeSeparatorParserOption());
    }
    if (attribs.isDecimalSeparatorParserOptionSet())
    {
      out.writeInt(8);
      out.writeShort(0x380B);
      out.writeShort(attribs.getDecimalSeparatorParserOption());
    }
    if (attribs.isNamingConventionParserOptionSet())
    {
      out.writeInt(8);
      out.writeShort(0x380C);
      out.writeShort(attribs.getNamingConventionParserOption());
    }
    if (attribs.isIgnoreDecimalDataErrorParserOptionSet())
    {
      out.writeInt(8);
      out.writeShort(0x380D);
      out.writeShort(attribs.getIgnoreDecimalDataErrorParserOption());
    }
    if (attribs.isCommitmentControlLevelParserOptionSet())
    {
      out.writeInt(8);
      out.writeShort(0x380E);
      out.writeShort(attribs.getCommitmentControlLevelParserOption());
    }
    if (attribs.isDefaultSQLLibraryNameSet())
    {
      String library = attribs.getDefaultSQLLibraryName();
      out.writeInt(10+library.length());
      out.writeShort(0x380F);
      out.writeShort(37);
      out.writeShort(library.length());
      writePadEBCDIC(library, library.length(), out);
    }
    if (attribs.isASCIICCSIDForTranslationTableSet())
    {
      out.writeInt(8);
      out.writeShort(0x3810);
      out.writeShort(attribs.getASCIICCSIDForTranslationTable());
    }
    if (attribs.isAmbiguousSelectOptionSet())
    {
      out.writeInt(8);
      out.writeShort(0x3811);
      out.writeShort(attribs.getAmbiguousSelectOption());
    }
    if (attribs.isPackageAddStatementAllowedSet())
    {
      out.writeInt(8);
      out.writeShort(0x3812);
      out.writeShort(attribs.getPackageAddStatementAllowed());
    }
    // Skip Data Source Name (DSN) parameters, this is what JTOpen does, too.
    if (attribs.isUseExtendedFormatsSet())
    {
      out.writeInt(7);
      out.writeShort(0x3821);
      out.writeByte(attribs.getUseExtendedFormats());
    }
    if (attribs.isLOBFieldThresholdSet())
    {
      out.writeInt(10);
      out.writeShort(0x3822);
      out.writeInt(attribs.getLOBFieldThreshold());
    }
    if (attribs.isDataCompressionParameterSet())
    {
      out.writeInt(8);
      out.writeShort(0x3823);
      out.writeShort(attribs.getDataCompressionParameter());
    }
    if (attribs.isTrueAutoCommitIndicatorSet())
    {
      out.writeInt(7);
      out.writeShort(0x3824);
      out.writeByte(attribs.getTrueAutoCommitIndicator());
    }
    if (attribs.isClientSupportInformationSet())
    {
      out.writeInt(10);
      out.writeShort(0x3825);
      out.writeInt(attribs.getClientSupportInformation());
    }
    if (attribs.isRDBNameSet())
    {
      String name = attribs.getRDBName();
      out.writeInt(8+name.length());
      out.writeShort(0x3826);
      out.writeShort(37);
      writePadEBCDIC(name, name.length(), out);
    }
    if (attribs.isDecimalPrecisionAndScaleAttributesSet())
    {
      out.writeInt(12);
      out.writeShort(0x3827);
      out.writeShort(attribs.getMaximumDecimalPrecision());
      out.writeShort(attribs.getMaximumDecimalScale());
      out.writeShort(attribs.getMinimumDivideScale());
    }
    if (attribs.isHexadecimalConstantParserOptionSet())
    {
      out.writeInt(7);
      out.writeShort(0x3828);
      out.writeByte(attribs.getHexadecimalConstantParserOption());
    }
    if (attribs.isInputLocatorTypeSet())
    {
      out.writeInt(7);
      out.writeShort(0x3829);
      out.writeByte(attribs.getInputLocatorType());
    }
    // Don't ask me why someone skipped 0x382A-0x382F... my guess is they don't know how to count.
    if (attribs.isLocatorPersistenceSet())
    {
      out.writeInt(8);
      out.writeShort(0x3830);
      out.writeShort(attribs.getLocatorPersistence());
    }
    if (attribs.isEWLMCorrelatorSet())
    {
      byte[] corr = attribs.getEWLMCorrelator();
      out.writeInt(6+corr.length);
      out.writeShort(0x3831);
      out.write(corr);
    }
    if (attribs.isRLECompressionSet())
    {
      byte[] comp = attribs.getRLECompression();
      out.writeInt(10+comp.length);
      out.writeShort(0x3832);
      out.writeInt(comp.length);
      out.write(comp);
    }
    if (attribs.isOptimizationGoalIndicatorSet())
    {
      out.writeInt(7);
      out.writeShort(0x3833);
      out.writeByte(attribs.getOptimizationGoalIndicator());
    }
    if (attribs.isQueryStorageLimitSet())
    {
      out.writeInt(10);
      out.writeShort(0x3834);
      out.writeInt(attribs.getQueryStorageLimit());
    }
    if (attribs.isDecimalFloatingPointRoundingModeOptionSet())
    {
      out.writeInt(8);
      out.writeShort(0x3835);
      out.writeShort(attribs.getDecimalFloatingPointRoundingModeOption());
    }
    if (attribs.isDecimalFloatingPointErrorReportingOptionSet())
    {
      out.writeInt(8);
      out.writeShort(0x3836);
      out.writeShort(attribs.getDecimalFloatingPointErrorReportingOption());
    }
    if (attribs.isClientAccountingInformationSet())
    {
      String info = attribs.getClientAccountingInformation();
      out.writeInt(10+info.length());
      out.writeShort(0x3837);
      out.writeShort(37);
      out.writeShort(info.length());
      writePadEBCDIC(info, info.length(), out);
    }
    if (attribs.isClientApplicationNameSet())
    {
      String name = attribs.getClientApplicationName();
      out.writeInt(10+name.length());
      out.writeShort(0x3838);
      out.writeShort(37);
      out.writeShort(name.length());
      writePadEBCDIC(name, name.length(), out);
    }
    if (attribs.isClientUserIdentifierSet())
    {
      String user = attribs.getClientUserIdentifier();
      out.writeInt(10+user.length());
      out.writeShort(0x3839);
      out.writeShort(37);
      out.writeShort(user.length());
      writePadEBCDIC(user, user.length(), out);
    }
    if (attribs.isClientWorkstationNameSet())
    {
      String name = attribs.getClientWorkstationName();
      out.writeInt(10+name.length());
      out.writeShort(0x383A);
      out.writeShort(37);
      out.writeShort(name.length());
      writePadEBCDIC(name, name.length(), out);
    }
    if (attribs.isClientProgramIdentifierSet())
    {
      String prog = attribs.getClientProgramIdentifier();
      out.writeInt(10+prog.length());
      out.writeShort(0x383B);
      out.writeShort(37);
      out.writeShort(prog.length());
      writePadEBCDIC(prog, prog.length(), out);
    }
    if (attribs.isInterfaceTypeSet())
    {
      String type = attribs.getInterfaceType();
      out.writeInt(10+type.length());
      out.writeShort(0x383C);
      out.writeShort(37);
      out.writeShort(type.length());
      writePadEBCDIC(type, type.length(), out);
    }
    if (attribs.isInterfaceNameSet())
    {
      String name = attribs.getInterfaceName();
      out.writeInt(10+name.length());
      out.writeShort(0x383D);
      out.writeShort(37);
      out.writeShort(name.length());
      writePadEBCDIC(name, name.length(), out);
    }
    if (attribs.isInterfaceLevelSet())
    {
      String level = attribs.getInterfaceLevel();
      out.writeInt(10+level.length());
      out.writeShort(0x383E);
      out.writeShort(37);
      out.writeShort(level.length());
      writePadEBCDIC(level, level.length(), out);
    }
    if (attribs.isCloseOnEOFSet())
    {
      out.writeInt(7);
      out.writeShort(0x383F);
      out.writeByte(attribs.getCloseOnEOF());
    }
  }







  private void sendCommitRequest(HostOutputStream out) throws IOException
  {
    int length = 40;

    // Write header (20 bytes)
    writeHeader(length, 0x1807);

    // Write template (20 bytes)
    out.writeInt(0x80000000); // Operational result (ORS) bitmap - return data .
    out.writeInt(0); // Reserved.
    out.writeShort(0); // Return ORS handle - after operation completes.
    out.writeShort(0); // Fill ORS handle.
    out.writeShort(0); // Based on ORS handle.
    out.writeShort(0); // Request parameter block (RPB) handle.
    out.writeShort(0); // Parameter marker descriptor handle.
    out.writeShort(0); // Parameter count.

  }

  private void sendRollbackRequest(HostOutputStream out) throws IOException
  {
    int length = 40;

    // Write header (20 bytes)
    writeHeader(length, 0x1808);

    // Write template (20 bytes)
    out.writeInt(0x80000000); // Operational result (ORS) bitmap - return data .
    out.writeInt(0); // Reserved.
    out.writeShort(0); // Return ORS handle - after operation completes.
    out.writeShort(0); // Fill ORS handle.
    out.writeShort(0); // Based on ORS handle.
    out.writeShort(0); // Request parameter block (RPB) handle.
    out.writeShort(0); // Parameter marker descriptor handle.
    out.writeShort(0); // Parameter count.

 }


}
