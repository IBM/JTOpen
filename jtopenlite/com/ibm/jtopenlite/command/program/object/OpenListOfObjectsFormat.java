///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OpenListOfObjectsFormat.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.object;

import com.ibm.jtopenlite.*;
import com.ibm.jtopenlite.command.program.openlist.*;

public class OpenListOfObjectsFormat implements ListEntryFormat<OpenListOfObjectsFormatListener>
{
  private final char[] charBuffer_ = new char[10];

  public OpenListOfObjectsFormat()
  {
  }

  private final byte[] lastLibraryBytes_ = new byte[10];
  private String lastLibrary_ = "          ";
  private final byte[] lastTypeBytes_ = new byte[10];
  private String lastType_ = "          ";

  private static boolean matches(final byte[] data, final int offset, final byte[] data2)
  {
    for (int i=0; i<data2.length; ++i)
    {
      if (data[offset+i] != data2[i]) return false;
    }
    return true;
  }

  private void getLibrary(final byte[] data, final int numRead)
  {
    if (!matches(data, numRead, lastLibraryBytes_))
    {
      System.arraycopy(data, numRead, lastLibraryBytes_, 0, 10);
      lastLibrary_ = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
    }
  }

  private void getType(final byte[] data, final int numRead)
  {
    if (!matches(data, numRead, lastTypeBytes_))
    {
      System.arraycopy(data, numRead, lastTypeBytes_, 0, 10);
      lastType_ = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
    }
  }

  public void format(final byte[] data, final int maxLength, final int recordLength, OpenListOfObjectsFormatListener listener)
  {
    if (listener == null)
    {
      return;
    }

    int numRead = 0;
    while (numRead+36 <= maxLength)
    {
      String objectName = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
//      String objectLibrary = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      getLibrary(data, numRead);
      String objectLibrary = lastLibrary_;
      numRead += 10;
//      String objectType = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      getType(data, numRead);
      String objectType = lastType_;
      numRead += 10;
      String informationStatus = Conv.ebcdicByteArrayToString(data, numRead, 1, charBuffer_);
      numRead += 2;
      final int numFields = Conv.byteArrayToInt(data, numRead);
      numRead += 4;
      listener.newObjectEntry(objectName, objectLibrary, objectType, informationStatus, numFields);
      if (!informationStatus.equals("A") && !informationStatus.equals("L"))
      {
        for (int i=0; i<numFields; ++i)
        {
          int lengthOfFieldInfo = Conv.byteArrayToInt(data, numRead);
          int keyField = Conv.byteArrayToInt(data, numRead+4);
          String typeOfData = Conv.ebcdicByteArrayToString(data, numRead+8, 1, charBuffer_);
          int lengthOfData = Conv.byteArrayToInt(data, numRead+12);
          listener.newObjectFieldData(lengthOfFieldInfo, keyField, typeOfData, lengthOfData, numRead+16, data);
          numRead += lengthOfFieldInfo;
        }
      }
      else
      {
        numRead += (recordLength-36);
      }
    }
  }
}
