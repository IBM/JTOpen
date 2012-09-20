///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OpenListOfJobsFormatOLJB0300.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

import com.ibm.jtopenlite.*;
import java.util.*;

/**
 * @deprecated Use com.ibm.jtopenlite.command.program.workmgmt.OpenListOfJobsFormatOLJB0300 instead
 */
public class OpenListOfJobsFormatOLJB0300 extends JobFormatAdapter
{
  private OpenListOfJobsKeyField[] keyFields_;
  private OpenListOfJobsFormatOLJB0300Listener listener_;
  private final char[] charBuffer_ = new char[100];

  public OpenListOfJobsFormatOLJB0300()
  {
  }

  public OpenListOfJobsFormatOLJB0300(OpenListOfJobsKeyField[] keyFields, OpenListOfJobsFormatOLJB0300Listener listener)
  {
    keyFields_ = keyFields;
    listener_ = listener;
  }

  public OpenListOfJobsFormatOLJB0300Listener getListener()
  {
    return listener_;
  }

  public void setListener(OpenListOfJobsFormatOLJB0300Listener listener)
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
    return "OLJB0300";
  }

  int getTypeSubclass()
  {
    return FORMAT_OLJB0300;
  }

  int getMinimumRecordLengthSubclass()
  {
    return 40;
  }

  private final byte[] lastJobNameBytes_ = new byte[10];
  private String lastJobName_ = "          ";
  private final byte[] lastUserNameBytes_ = new byte[10];
  private String lastUserName_ = "          ";
  private final byte[] lastMemoryPoolBytes_ = new byte[10];
  private String lastMemoryPool_ = "          ";
  private final byte[] lastCurrentUserProfileBytes_ = new byte[10];
  private String lastCurrentUserProfile_ = "          ";
  private final byte[] lastSubsystemBytes_ = new byte[20];
  private String lastSubsystem_ = "                    ";

  private static boolean matches(final byte[] data, final int offset, final byte[] data2)
  {
    for (int i=0; i<data2.length; ++i)
    {
      if (data[offset+i] != data2[i]) return false;
    }
    return true;
  }

  private void getJobName(final byte[] data, final int numRead)
  {
    if (!matches(data, numRead, lastJobNameBytes_))
    {
      System.arraycopy(data, numRead, lastJobNameBytes_, 0, 10);
      lastJobName_ = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
    }
  }

  private void getUserName(final byte[] data, final int numRead)
  {
    if (!matches(data, numRead, lastUserNameBytes_))
    {
      System.arraycopy(data, numRead, lastUserNameBytes_, 0, 10);
      lastUserName_ = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
    }
  }

  private void getMemoryPool(final byte[] data, final int numRead)
  {
    if (!matches(data, numRead, lastMemoryPoolBytes_))
    {
      System.arraycopy(data, numRead, lastMemoryPoolBytes_, 0, 10);
      lastMemoryPool_ = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
    }
  }

  private void getCurrentUserProfile(final byte[] data, final int numRead)
  {
    if (!matches(data, numRead, lastCurrentUserProfileBytes_))
    {
      System.arraycopy(data, numRead, lastCurrentUserProfileBytes_, 0, 10);
      lastCurrentUserProfile_ = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
    }
  }

  private void getSubsystem(final byte[] data, final int numRead)
  {
    if (!matches(data, numRead, lastSubsystemBytes_))
    {
      System.arraycopy(data, numRead, lastSubsystemBytes_, 0, 20);
      lastSubsystem_ = Conv.ebcdicByteArrayToString(data, numRead, 20, charBuffer_);
    }
  }

  private final HashObject hashObject_ = new HashObject();
  private final HashMap statusCache_ = new HashMap();

  private String getActiveJobStatus(final byte[] data, final int numRead)
  {
    int num = Conv.byteArrayToInt(data, numRead);
    hashObject_.setHash(num);
    String status = (String)statusCache_.get(hashObject_);
    if (status == null)
    {
      HashObject obj = new HashObject();
      obj.setHash(num);
      status = Conv.ebcdicByteArrayToString(data, numRead, 4, charBuffer_);
      statusCache_.put(obj, status);
    }
    return status;
  }

  synchronized void formatSubclass(final byte[] data, final int maxLength, final int recordLength)
  {
    if (listener_ == null)
    {
//      in.skipBytes(maxLength);
      return;
    }

    int numRead = 0;
    while (numRead+36 <= maxLength)
    {
//      String jobNameUsed = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      getJobName(data, numRead);
      String jobNameUsed = lastJobName_;
      numRead += 10;
//      String userNameUsed = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      getUserName(data, numRead);
      String userNameUsed = lastUserName_;
      numRead += 10;
      String jobNumberUsed = Conv.ebcdicByteArrayToString(data, numRead, 6, charBuffer_);
      numRead += 6;
//      String activeJobStatus = Conv.ebcdicByteArrayToString(data, numRead, 4, charBuffer_);
      String activeJobStatus = getActiveJobStatus(data, numRead);
      numRead += 4;
      String jobType = Conv.ebcdicByteArrayToString(data, numRead, 1, charBuffer_);
      numRead += 1;
      String jobSubtype = Conv.ebcdicByteArrayToString(data, numRead, 1, charBuffer_);
      numRead += 1;
      // int totalLengthOfDataReturned = Conv.byteArrayToInt(data, numRead);
      numRead += 4;
      listener_.newJobEntry(jobNameUsed, userNameUsed, jobNumberUsed, activeJobStatus, jobType, jobSubtype);
      if (numRead+4 <= maxLength)
      {
        numRead += 4;
        int recordOffset = 40;
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
            if (key == 1306)
            {
              // Memory pool.
              getMemoryPool(data, numRead);
              listener_.newKeyData(key, lastMemoryPool_, data, numRead);
            }
            else if (key == 305)
            {
              // Current user profile.
              getCurrentUserProfile(data, numRead);
              listener_.newKeyData(key, lastCurrentUserProfile_, data, numRead);
            }
            else if (key == 1906)
            {
              // Subsystem.
              getSubsystem(data, numRead);
              listener_.newKeyData(key, lastSubsystem_, data, numRead);
            }
            else
            {
              Util.readKeyData(data, numRead, key, length, isBinary, listener_, charBuffer_);
            }
            numRead += length;
            recordOffset += length;
          }
        }
        int skip = recordLength-recordOffset;
        if (skip > 0) numRead += skip;
      }
    }
  }
}
