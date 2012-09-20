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
import com.ibm.jtopenlite.command.program.object.*; 
import com.ibm.jtopenlite.command.program.openlist.OpenListHandler;

import java.io.*;

class ListObjectsImpl implements OpenListOfObjectsFormatListener, OpenListOfObjectsSelectionListener
{
  private static final int[] KEYS = new int[] { 202, 203 }; // Extended object attribute, text description
  private final OpenListOfObjectsFormat format_ = new OpenListOfObjectsFormat();
  private final OpenListOfObjects objectList_ = new OpenListOfObjects(format_, 8, 1, null, null, null, null, null, this, KEYS);
  private final OpenListHandler handler_ = new OpenListHandler(objectList_, format_, this);

  private ObjectInfo[] objects_;
  private int counter_ = -1;

  private final char[] charBuffer_ = new char[50];

  public ListObjectsImpl()
  {
  }
  public void openComplete()
  {
  }

  public void totalRecordsInList(int total)
  {
    objects_ = new ObjectInfo[total];
    counter_ = -1;
  }

  public boolean stopProcessing()
  {
    return false;
  }

  public ObjectInfo[] getObjects(final CommandConnection conn, String name, String library, String type) throws IOException
  {
    objectList_.setObjectName(name);
    objectList_.setObjectLibrary(library);
    objectList_.setObjectType(type);
    objects_ = null;
      counter_ = -1;
    handler_.process(conn, 2800);
      return objects_;
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
