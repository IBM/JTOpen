///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OpenListOfJobsFormatOLJB0100.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

import com.ibm.jtopenlite.*;

public class OpenListOfJobsFormatOLJB0100 extends JobFormatAdapter
{
  private OpenListOfJobsFormatOLJB0100Listener listener_;
  private final char[] charBuffer_ = new char[10];

  public OpenListOfJobsFormatOLJB0100(OpenListOfJobsFormatOLJB0100Listener listener)
  {
    listener_ = listener;
  }

  synchronized public OpenListOfJobsFormatOLJB0100Listener getListener()
  {
    return listener_;
  }

  synchronized public void setListener(OpenListOfJobsFormatOLJB0100Listener listener)
  {
    listener_ = listener;
  }

  public void setKeyFields(OpenListOfJobsKeyField[] keyFields)
  {
  }

  String getNameSubclass()
  {
    return "OLJB0100";
  }

  int getTypeSubclass()
  {
    return FORMAT_OLJB0100;
  }

  int getMinimumRecordLengthSubclass()
  {
    return 56;
  }

  synchronized void formatSubclass(final byte[] data, final int maxLength, final int recordLength)
  {
    if (listener_ == null)
    {
//      in.skipBytes(maxLength);
      return;
    }

    int numRead = 0;
//    final byte[] b = new byte[10];
    while (numRead+54 <= maxLength)
    {
//      in.readFully(b);
//      String jobNameUsed = new String(b, "Cp037");
      String jobNameUsed = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
//      in.readFully(b);
//      String userNameUsed = new String(b, "Cp037");
      String userNameUsed = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
//      in.readFully(b, 0, 6);
//      String jobNumberUsed = new String(b, 0, 6, "Cp037");
      String jobNumberUsed = Conv.ebcdicByteArrayToString(data, numRead, 6, charBuffer_);
      numRead += 6;
      byte[] internalJobIdentifier = new byte[16];
//      in.readFully(internalJobIdentifier);
      System.arraycopy(data, numRead, internalJobIdentifier, 0, 16);
      numRead += 16;
//      in.readFully(b);
//      String status = new String(b, "Cp037");
      String status = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
//      in.readFully(b, 0, 1);
//      String jobType = new String(b, 0, 1, "Cp037");
      numRead += 10;
      String jobType = Conv.ebcdicByteArrayToString(data, numRead, 1, charBuffer_);
      numRead += 1;
//      in.readFully(b, 0, 1);
//      String jobSubtype = new String(b, 0, 1, "Cp037");
      String jobSubtype = Conv.ebcdicByteArrayToString(data, numRead, 1, charBuffer_);
      numRead += 1;
      listener_.newJobEntry(jobNameUsed, userNameUsed, jobNumberUsed, internalJobIdentifier, status, jobType, jobSubtype);
      if (numRead+2 <= maxLength)
      {
//        in.skipBytes(2);
        numRead += 2;
      }
    }
//    in.skipBytes(maxLength-numRead);
  }
}
