///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DDMDataBuffer.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ddm;

/**
 * Represents a set of temporary data for a given record.
 * Data buffers are reused internally by a DDMConnection when reading records from a file.
 * When a data buffer is passed to {@link DDMReadCallback#newRecord DDMReadCallback.newRecord()}, its record data buffer, record number,
 * and null field values will all be in sync. If a data buffer is referenced elsewhere, no
 * guarantee is made as to what the actual values stored in the buffer will be, as data buffers
 * are reused while records are read from a file.
**/
public final class DDMDataBuffer
{
  private volatile boolean processing_;
  private final byte[] recordDataBuffer_;
  private int recordNumber_;
  private final byte[] nullFieldMap_;
  private final boolean[] nullFieldValues_;
  private final byte[] packetBuffer_;

  DDMDataBuffer(final int recordLength, final int packetLength, final int nullFieldLength)
  {
    recordDataBuffer_ = new byte[recordLength];
    packetBuffer_ = new byte[packetLength];
    nullFieldMap_ = new byte[nullFieldLength];
    nullFieldValues_ = new boolean[nullFieldLength];
  }

  final boolean isProcessing()
  {
    return processing_;
  }

  final void startProcessing()
  {
    processing_ = true;
  }

  final void doneProcessing()
  {
    processing_ = false;
  }

  /**
   * Returns the current record data stored in this buffer.
  **/
  public final byte[] getRecordDataBuffer()
  {
    return recordDataBuffer_;
  }

  final byte[] getPacketBuffer()
  {
    return packetBuffer_;
  }

  /**
   * Returns the current record number stored in this buffer.
  **/
  public final int getRecordNumber()
  {
    return recordNumber_;
  }

  void setRecordNumber(int recnum)
  {
    recordNumber_ = recnum;
  }

  final byte[] getNullFieldMap()
  {
    return nullFieldMap_;
  }

  /**
   * Returns the current null field values stored in this buffer.
  **/
  public final boolean[] getNullFieldValues()
  {
    return nullFieldValues_;
  }
}

