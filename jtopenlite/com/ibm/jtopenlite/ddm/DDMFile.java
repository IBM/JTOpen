///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DDMFile.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ddm;


/**
 * Represents a handle to a file.
**/
public final class DDMFile
{
  public static final int READ_ONLY = 0;
  public static final int WRITE_ONLY = 1;
  public static final int READ_WRITE = 2;

  private final String library_;
  private final String file_;
  private final String member_;
  private final byte[] recordFormatName_;
  private final byte[] dclNam_;
  private final int openType_;
  private final int recordLength_;
  private final int recordIncrement_;
  private final int batchSize_;
  private final int nullFieldByteMapOffset_;
  private final DDMDataBuffer[] buffers_;
  private final DDMCallbackEvent eventBuffer_;

  DDMFile(String library, String file, String member, byte[] recordFormatName,
          byte[] dclName, int openType, int recLength, int recInc, int batchSize, int nullFieldOffset, int numBuffers)
  {
    library_ = library;
    file_ = file;
    member_ = member;
    recordFormatName_ = recordFormatName;
    dclNam_ = dclName;
    openType_ = openType;
    recordLength_ = recLength;
    recordIncrement_ = recInc;
    batchSize_ = batchSize;
    nullFieldByteMapOffset_ = nullFieldOffset;
    eventBuffer_ = new DDMCallbackEvent(this);

    numBuffers = numBuffers <= 0 ? 1 : numBuffers;

    buffers_ = new DDMDataBuffer[numBuffers];
    for (int i=0; i<numBuffers; ++i)
    {
      buffers_[i] = new DDMDataBuffer(recordLength_, recordIncrement_ - recordLength_ + 2,
                                      recordIncrement_ - nullFieldOffset);
    }
  }

  byte[] getDCLNAM()
  {
    return dclNam_;
  }

  byte[] getRecordFormatName()
  {
    return recordFormatName_;
  }

  int getRecordIncrement()
  {
    return recordIncrement_;
  }

  int getBatchSize()
  {
    return batchSize_;
  }

  int getNullFieldByteMapOffset()
  {
    return nullFieldByteMapOffset_;
  }

  private int bufferIndex_;

  void nextBuffer()
  {
    if (++bufferIndex_ == buffers_.length)
    {
      bufferIndex_ = 0;
    }
  }

  /**
   * Returns the number of data buffers used for reading records from this file.
  **/
  public int getBufferCount()
  {
    return buffers_.length;
  }

  int getCurrentBufferIndex()
  {
    return bufferIndex_;
  }

  DDMDataBuffer getDataBuffer(final int index)
  {
    return buffers_[index];
  }

  DDMDataBuffer getNextDataBuffer()
  {
    int index = bufferIndex_+1;
    if (index == buffers_.length) index = 0;
    return buffers_[index];
  }

  byte[] getNullFieldMap()
  {
    return buffers_[bufferIndex_].getNullFieldMap();
  }

  boolean[] getNullFieldValues()
  {
    return buffers_[bufferIndex_].getNullFieldValues();
  }

  /**
   * Returns the record length in bytes of this file.
  **/
  public int getRecordLength()
  {
    return recordLength_;
  }

  /**
   * Returns the current data buffer's record data buffer.
  **/
  public byte[] getRecordDataBuffer()
  {
    return buffers_[bufferIndex_].getRecordDataBuffer();
  }

  byte[] getPacketBuffer()
  {
    return buffers_[bufferIndex_].getPacketBuffer();
  }

  DDMCallbackEvent getEventBuffer()
  {
    return eventBuffer_;
  }

  /**
   * Returns the read-write access type used to open this file.
  **/
  public int getReadWriteType()
  {
    return openType_;
  }

  /**
   * Returns the library in which this file resides.
  **/
  public String getLibrary()
  {
    return library_;
  }

  /**
   * Returns the name of this file.
  **/
  public String getFile()
  {
    return file_;
  }

  /**
   * Returns the member name of this file.
  **/
  public String getMember()
  {
    return member_;
  }

  public String toString()
  {
    return library_+file_+member_;
  }
}

