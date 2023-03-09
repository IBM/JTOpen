///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename: OpenListOfJobsFormatOLJB0100.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.workmgmt;

import com.ibm.jtopenlite.*;

public class OpenListOfJobsFormatOLJB0100 implements OpenListOfJobsFormat<OpenListOfJobsFormatOLJB0100Listener>
{
  private final char[] charBuffer_ = new char[10];

  public OpenListOfJobsFormatOLJB0100()
  {
  }

  public void setKeyFields(OpenListOfJobsKeyField[] keyFields)
  {
  }

  public String getName()
  {
    return "OLJB0100";
  }

  public int getType()
  {
    return FORMAT_OLJB0100;
  }

  public int getMinimumRecordLength()
  {
    return 56;
  }

  public void format(final byte[] data, final int maxLength, final int recordLength, OpenListOfJobsFormatOLJB0100Listener listener)
  {
    if (listener == null)
    {
      return;
    }

    int numRead = 0;
    while (numRead+54 <= maxLength)
    {
      String jobNameUsed = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
      String userNameUsed = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
      String jobNumberUsed = Conv.ebcdicByteArrayToString(data, numRead, 6, charBuffer_);
      numRead += 6;
      byte[] internalJobIdentifier = new byte[16];
      System.arraycopy(data, numRead, internalJobIdentifier, 0, 16);
      numRead += 16;
      String status = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
      String jobType = Conv.ebcdicByteArrayToString(data, numRead, 1, charBuffer_);
      numRead += 1;
      String jobSubtype = Conv.ebcdicByteArrayToString(data, numRead, 1, charBuffer_);
      numRead += 1;
      listener.newJobEntry(jobNameUsed, userNameUsed, jobNumberUsed, internalJobIdentifier, status, jobType, jobSubtype);
      if (numRead+2 <= maxLength)
      {
        numRead += 2;
      }
    }
  }
}
