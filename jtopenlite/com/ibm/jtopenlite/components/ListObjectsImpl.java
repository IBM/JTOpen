///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  ListObjectsImpl.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.components;

import com.ibm.jtopenlite.*;
import com.ibm.jtopenlite.command.*;
import com.ibm.jtopenlite.command.program.*;
import java.io.*;

class ListObjectsImpl implements OpenListOfObjectsFormatListener, OpenListOfObjectsSelectionListener
{
  private static final int[] KEYS = new int[] { 202, 203 }; // Extended object attribute, text description
  private final OpenListOfObjectsFormat format_ = new OpenListOfObjectsFormat();
  private final OpenListOfObjects objectList_ = new OpenListOfObjects(format_, 8, 1, null, null, null, null, null, this, KEYS);
  private final GetListEntries getObjects_ = new GetListEntries(0, null, 0, 0, 0, format_);
  private final CloseList close_ = new CloseList(null);

  private ObjectInfo[] objects_;
  private int counter_ = -1;

  private final char[] charBuffer_ = new char[50];

  public ListObjectsImpl()
  {
  }

  public ObjectInfo[] getObjects(final CommandConnection conn, String name, String library, String type) throws IOException
  {
    format_.setListener(null);
    objectList_.setObjectName(name);
    objectList_.setObjectLibrary(library);
    objectList_.setObjectType(type);

    CommandResult result = conn.call(objectList_);
    if (!result.succeeded())
    {
      throw new IOException("Object list failed: "+result.toString());
    }

    ListInformation listInfo = objectList_.getListInformation();
    byte[] requestHandle = listInfo.getRequestHandle();
    close_.setRequestHandle(requestHandle);

    try
    {
      int recordLength = listInfo.getRecordLength();
      // Now, the list is building on the server.
      // Call GetListEntries once to wait for the list to finish building, for example.
      int receiverSize = 8;
      int numRecordsToReturn = 0;
      int startingRecord = -1;
      getObjects_.setLengthOfReceiverVariable(receiverSize);
      getObjects_.setRequestHandle(requestHandle);
      getObjects_.setRecordLength(recordLength);
      getObjects_.setNumberOfRecordsToReturn(numRecordsToReturn);
      getObjects_.setStartingRecord(startingRecord);
      result = conn.call(getObjects_);
      if (!result.succeeded())
      {
        throw new IOException("Get objects failed: "+result.toString());
      }

      listInfo = getObjects_.getListInformation();
      int totalRecords = listInfo.getTotalRecords();
      objects_ = new ObjectInfo[totalRecords];
      counter_ = -1;

      // Now retrieve each object record in chunks of 800 at a time.
      numRecordsToReturn = 2800;
      receiverSize = recordLength * numRecordsToReturn;
      startingRecord = 1;
      getObjects_.setLengthOfReceiverVariable(receiverSize);
      getObjects_.setNumberOfRecordsToReturn(numRecordsToReturn);
      getObjects_.setStartingRecord(startingRecord);
      format_.setListener(this); // Ready to process.
      while (startingRecord <= totalRecords)
      {
        result = conn.call(getObjects_);
        if (!result.succeeded())
        {
          throw new IOException("Get objects failed: "+result.toString());
        }
        // Assuming it succeeded.
        listInfo = getObjects_.getListInformation();
        startingRecord += listInfo.getRecordsReturned();
        getObjects_.setStartingRecord(startingRecord);
      }
      return objects_;
    }
    finally
    {
      // All done.
      conn.call(close_);
    }
  }

  ////////////////////////////////////////
  //
  // Selection methods.
  //
  ////////////////////////////////////////

  public boolean isSelected()
  {
    return false;
  }

  public int getNumberOfStatuses()
  {
    return 1;
  }

  public String getStatus(int index)
  {
    return "A"; // Omit objects we do not have authority to.
  }

/*  public int getCallLevel()
  {
    return 0;
  }

  public int getNumberOfObjectAuthorities()
  {
    return 1;
  }

  public int getNumberOfLibraryAuthorities()
  {
    return 0;
  }

  public String getObjectAuthority(int index)
  {
    return "*OBJEXIST";
  }

  public String getLibraryAuthority(int index)
  {
    return null;
  }
*/
  ////////////////////////////////////////
  //
  // List entry format methods.
  //
  ////////////////////////////////////////

  public void newObjectEntry(String objectName, String objectLibrary, String objectType,
                             String informationStatus, int numFields)
  {
    objects_[++counter_] = new ObjectInfo(objectName, objectLibrary, objectType, informationStatus);
  }

  public void newObjectFieldData(int lengthOfFieldInfo, int key, String type, int dataLength, int dataOffset, byte[] data)
  {
    switch (key)
    {
      case 202:
        String attribute = isBlank(data, dataOffset) ? blankAttribute_ : Conv.ebcdicByteArrayToString(data, dataOffset, dataLength, charBuffer_);
        objects_[counter_].setAttribute(attribute);
        break;
      case 203:
        String description = Conv.ebcdicByteArrayToString(data, dataOffset, dataLength, charBuffer_);
        objects_[counter_].setTextDescription(description);
        break;
    }
  }

  private static final String blankAttribute_ = "          ";

  private boolean isBlank(final byte[] data, final int numRead)
  {
    int stop = numRead+10;
    for (int i=numRead; i<stop; ++i)
    {
      if (data[i] != 0x40) return false;
    }
    return true;
  }
}
