///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DiskStatus.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.components;

/**
 * Represents disk status information returned by the ListDiskStatuses class.
 * The toString() method will print various fields in a format similar to
 * what WRKDSKSTS does.
**/
public final class DiskStatus
{
  private final String unit_;
  private final String type_;
  private final String size_;
  private final String percentUsed_;
  private final String ioRequests_;
  private final String requestSize_;
  private final String readRequests_;
  private final String writeRequests_;
  private final String read_;
  private final String write_;
  private final String percentBusy_;
  private final String asp_;
  private final String protectionType_;
  private final String protectionStatus_;
  private final String compression_;

  DiskStatus(String unit, String type, String sizeMB, String percentUsed,
             String ioRequests, String requestSizeKB, String readRequests,
             String writeRequests, String readKB, String writeKB, String percentBusy,
             String asp, String protectionType, String protectionStatus, String compression)
  {
    unit_ = unit;
    type_ = type;
    size_ = sizeMB;
    percentUsed_ = percentUsed;
    ioRequests_ = ioRequests;
    requestSize_ = requestSizeKB;
    readRequests_ = readRequests;
    writeRequests_ = writeRequests;
    read_ = readKB;
    write_ = writeKB;
    percentBusy_ = percentBusy;
    asp_ = asp;
    protectionType_ = protectionType;
    protectionStatus_ = protectionStatus;
    compression_ = compression;
  }

  public String getUnit()
  {
    return unit_;
  }

  public String getType()
  {
    return type_;
  }

  public String getSize()
  {
    return size_;
  }

  public String getPercentUsed()
  {
    return percentUsed_;
  }

  public String getIORequests()
  {
    return ioRequests_;
  }

  public String getRequestSize()
  {
    return requestSize_;
  }

  public String getReadRequests()
  {
    return readRequests_;
  }

  public String getWriteRequests()
  {
    return writeRequests_;
  }

  public String getReadKB()
  {
    return read_;
  }

  public String getWriteKB()
  {
    return write_;
  }

  public String getPercentBusy()
  {
    return percentBusy_;
  }

  public String getASP()
  {
    return asp_;
  }

  public String getProtectionType()
  {
    return protectionType_;
  }

  public String getProtectionStatus()
  {
    return protectionStatus_;
  }

  public String getCompression()
  {
    return compression_;
  }

  private final DiskStatus print(final StringBuffer buf, final String value)
  {
    buf.append(" ");
    final int num = 5-value.length();
    for (int i=0; i<num; ++i)
    {
      buf.append(" ");
    }
    buf.append(value);
    return this;
  }

  public String toString()
  {
    StringBuffer buf = new StringBuffer();
    int num = 2-unit_.length();
    buf.append(" ");
    for (int i=0; i<num; ++i)
    {
      buf.append(" ");
    }
    buf.append(unit_).append("  ").append(type_).append("  ").append(size_);
    print(buf, percentUsed_).print(buf, ioRequests_).print(buf, requestSize_);
    print(buf, readRequests_).print(buf, writeRequests_);
    print(buf, read_).print(buf, write_);
    buf.append(" ");
    num = 3-percentBusy_.length();
    for (int i=0; i<num; ++i)
    {
      buf.append(" ");
    }
    buf.append(percentBusy_);
    buf.append("  ").append(asp_);
    print(buf, protectionType_).print(buf, protectionStatus_);
    if (compression_ != null)
    {
      buf.append(" ");
      buf.append(compression_);
    }
    return buf.toString();
  }
}

