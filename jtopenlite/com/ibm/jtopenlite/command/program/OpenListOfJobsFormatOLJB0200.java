///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OpenListOfJobsFormatOLJB0200.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

import com.ibm.jtopenlite.*;

public class OpenListOfJobsFormatOLJB0200 extends JobFormatAdapter
{
  private OpenListOfJobsKeyField[] keyFields_;
  private OpenListOfJobsFormatOLJB0200Listener listener_;
  private final char[] charBuffer_ = new char[100];

  public OpenListOfJobsFormatOLJB0200()
  {
  }

  public OpenListOfJobsFormatOLJB0200(OpenListOfJobsKeyField[] keyFields, OpenListOfJobsFormatOLJB0200Listener listener)
  {
    keyFields_ = keyFields;
    listener_ = listener;
  }

  public OpenListOfJobsFormatOLJB0200Listener getListener()
  {
    return listener_;
  }

  public void setListener(OpenListOfJobsFormatOLJB0200Listener listener)
  {
    listener_ = listener;
  }

  public OpenListOfJobsKeyField[] getKeyFields()
  {
    return keyFields_;
  }

  public void setKeyFields(OpenListOfJobsKeyField[] keyFields)
  {
    keyFields_ = keyFields;
  }

  String getNameSubclass()
  {
    return "OLJB0200";
  }

  int getTypeSubclass()
  {
    return FORMAT_OLJB0200;
  }

  int getMinimumRecordLengthSubclass()
  {
    return 60;
  }

  synchronized void formatSubclass(final byte[] data, final int maxLength, final int recordLength)
  {
    if (listener_ == null)
    {
//      in.skipBytes(maxLength);
      return;
    }

    int numRead = 0;
//    final byte[] b1 = new byte[1];
//    final byte[] b2 = new byte[2];
//    final byte[] b4 = new byte[4];
//    final byte[] b10 = new byte[10];
//    final byte[] b20 = new byte[20];
    while (numRead+recordLength <= maxLength)
    {
//      in.readFully(b10);
//      String jobNameUsed = new String(b10, "Cp037");
      String jobNameUsed = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
//      in.readFully(b10);
//      String userNameUsed = new String(b10, "Cp037");
      String userNameUsed = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
//      in.readFully(b10, 0, 6);
//      String jobNumberUsed = new String(b10, 0, 6, "Cp037");
      String jobNumberUsed = Conv.ebcdicByteArrayToString(data, numRead, 6, charBuffer_);
      numRead += 6;
      byte[] internalJobIdentifier = new byte[16];
//      in.readFully(internalJobIdentifier);
      System.arraycopy(data, numRead, internalJobIdentifier, 0, 16);
      numRead += 16;
//      in.readFully(b10);
//      String status = new String(b10, "Cp037");
      String status = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
//      in.readFully(b1);
//      String jobType = new String(b1, "Cp037");
      String jobType = Conv.ebcdicByteArrayToString(data, numRead, 1, charBuffer_);
      numRead += 1;
//      in.readFully(b1);
//      String jobSubtype = new String(b1, "Cp037");
      String jobSubtype = Conv.ebcdicByteArrayToString(data, numRead, 1, charBuffer_);
      numRead += 1;
//      in.skipBytes(2);
      numRead += 2;
      int jobInfoStatus = data[numRead] & 0x00FF;
      boolean infoStatus = jobInfoStatus == 0x40;
      //in.skipBytes(3);
      numRead += 4;
      listener_.newJobEntry(jobNameUsed, userNameUsed, jobNumberUsed, internalJobIdentifier, status, jobType, jobSubtype, infoStatus);
      int recordOffset = 60;
      if (keyFields_ != null)
      {
        for (int i=0; i<keyFields_.length; ++i)
        {
          int skip = keyFields_[i].getDisplacement()-recordOffset;
          //in.skipBytes(skip);
          numRead += skip;
          recordOffset += skip;
          final int key = keyFields_[i].getKey();
          final int length = keyFields_[i].getLength();
          final boolean isBinary = keyFields_[i].isBinary();
          Util.readKeyData(data, numRead, key, length, isBinary, listener_, charBuffer_);
          numRead += length;
          recordOffset += length;
        }
      }
      int skip = recordLength-recordOffset;
      if (skip > 0) numRead += skip;
    }
//    in.skipBytes(maxLength-numRead);
  }
}
