///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename: OpenListOfJobsFormatOLJB0200.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.workmgmt;

import com.ibm.jtopenlite.*;

public class OpenListOfJobsFormatOLJB0200 implements OpenListOfJobsFormat<OpenListOfJobsFormatOLJB0200Listener>
{
  private OpenListOfJobsKeyField[] keyFields_;
  private final char[] charBuffer_ = new char[100];

  public OpenListOfJobsFormatOLJB0200()
  {
  }

  public OpenListOfJobsFormatOLJB0200(OpenListOfJobsKeyField[] keyFields)
  {
    keyFields_ = keyFields;
  }

  public OpenListOfJobsKeyField[] getKeyFields()
  {
    return keyFields_;
  }

  public void setKeyFields(OpenListOfJobsKeyField[] keyFields)
  {
    keyFields_ = keyFields;
  }

  public String getName()
  {
    return "OLJB0200";
  }

  public int getType()
  {
    return FORMAT_OLJB0200;
  }

  public int getMinimumRecordLength()
  {
    return 60;
  }

  public void format(final byte[] data, final int maxLength, final int recordLength, OpenListOfJobsFormatOLJB0200Listener listener)
  {
    if (listener == null)
    {
      return;
    }

    int numRead = 0;
    while (numRead+recordLength <= maxLength)
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
      numRead += 2;
      int jobInfoStatus = data[numRead] & 0x00FF;
      boolean infoStatus = jobInfoStatus == 0x40;
      numRead += 4;
      listener.newJobEntry(jobNameUsed, userNameUsed, jobNumberUsed, internalJobIdentifier, status, jobType, jobSubtype, infoStatus);
      int recordOffset = 60;
      if (keyFields_ != null)
      {
        for (int i=0; i<keyFields_.length; ++i)
        {
          int skip = keyFields_[i].getDisplacement()-recordOffset;
          numRead += skip;
          recordOffset += skip;
          final int key = keyFields_[i].getKey();
          final int length = keyFields_[i].getLength();
          final boolean isBinary = keyFields_[i].isBinary();
          Util.readKeyData(data, numRead, key, length, isBinary, listener, charBuffer_);
          numRead += length;
          recordOffset += length;
        }
      }
      int skip = recordLength-recordOffset;
      if (skip > 0) numRead += skip;
    }
  }
}
