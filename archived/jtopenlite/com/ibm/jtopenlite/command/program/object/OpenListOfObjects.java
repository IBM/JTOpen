///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OpenListOfObjects.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.object;

import com.ibm.jtopenlite.*;
import com.ibm.jtopenlite.command.*;
import com.ibm.jtopenlite.command.program.SortListener;
import com.ibm.jtopenlite.command.program.openlist.*;


/**
 * <a href="http://publib.boulder.ibm.com/infocenter/iseries/v5r4/topic/apis/qgyolobj.htm">QGYOLOBJ</a>
**/
public class OpenListOfObjects implements OpenListProgram<OpenListOfObjectsFormat,OpenListOfObjectsFormatListener>
{
  private static final byte[] ZERO = new byte[4];

  private OpenListOfObjectsFormat formatter_;
  private OpenListOfObjectsFormatListener formatListener_;
  private int inputLength_;
  private int numberOfRecordsToReturn_;
  private SortListener sortListener_;
  private String objectName_;
  private String objectLibrary_;
  private String objectType_;
  private OpenListOfObjectsAuthorityListener authorityListener_;
  private OpenListOfObjectsSelectionListener selectionListener_;
  private int[] keys_;
  private ListInformation info_;

  private byte[] tempData_;

  public OpenListOfObjects()
  {
  }

  public OpenListOfObjects(OpenListOfObjectsFormat format, int lengthOfReceiverVariable, int numberOfRecordsToReturn,
                           SortListener sortInformation,
                           String objectName, String libraryName, String objectType,
                           OpenListOfObjectsAuthorityListener authorityControl,
                           OpenListOfObjectsSelectionListener selectionControl,
                           int[] keysToReturn)
  {
    formatter_ = format;
    inputLength_ = lengthOfReceiverVariable <= 0 ? 1 : lengthOfReceiverVariable;
    numberOfRecordsToReturn_ = numberOfRecordsToReturn;
    sortListener_ = sortInformation;
    objectName_ = objectName;
    objectLibrary_ = libraryName;
    objectType_ = objectType;
    authorityListener_ = authorityControl;
    selectionListener_ = selectionControl;
    keys_ = keysToReturn;
  }

  public final byte[] getTempDataBuffer()
  {
    int maxSize = 0;
    for (int i=0; i<getNumberOfParameters(); ++i)
    {
      int len = getParameterOutputLength(i);
      if (len > maxSize) maxSize = len;
      len = getParameterInputLength(i);
      if (len > maxSize) maxSize = len;
    }
    if (tempData_ == null || tempData_.length < maxSize)
    {
      tempData_ = new byte[maxSize];
    }
    return tempData_;
  }

  public OpenListOfObjectsFormat getFormatter()
  {
    return formatter_;
  }

  public void setFormatter(OpenListOfObjectsFormat formatter)
  {
    formatter_ = formatter;
  }

  public OpenListOfObjectsFormatListener getFormatListener()
  {
    return formatListener_;
  }

  public void setFormatListener(OpenListOfObjectsFormatListener listener)
  {
    formatListener_ = listener;
  }

  public String getProgramName()
  {
    return "QGYOLOBJ";
  }

  public String getProgramLibrary()
  {
    return "QSYS";
  }

  public int getNumberOfParameters()
  {
    return 12;
  }

  public void newCall()
  {
    info_ = null;
  }

  public ListInformation getListInformation()
  {
    return info_;
  }

  public int getLengthOfReceiverVariable()
  {
    return inputLength_;
  }

  public void setLengthOfReceiverVariable(int length)
  {
    inputLength_ = length;
  }

  public int getNumberOfRecordsToReturn()
  {
    return numberOfRecordsToReturn_;
  }

  public void setNumberOfRecordsToReturn(int numberOfRecordsToReturn)
  {
    numberOfRecordsToReturn_ = numberOfRecordsToReturn;
  }

  public SortListener getSortListener()
  {
    return sortListener_;
  }

  public void setSortListener(SortListener listener)
  {
    sortListener_ = listener;
  }

  public String getObjectName()
  {
    return objectName_;
  }

  public void setObjectName(String name)
  {
    objectName_ = name;
  }

  public String getObjectLibrary()
  {
    return objectLibrary_;
  }

  public void setObjectLibrary(String library)
  {
    objectLibrary_ = library;
  }

  public String getObjectType()
  {
    return objectType_;
  }

  public void setObjectType(String type)
  {
    objectType_ = type;
  }

  public int getParameterInputLength(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return 0;
      case 1: return 4;
      case 2: return 0;
      case 3: return 4;
      case 4:
        return sortListener_ == null ? 4 :
          4 + (sortListener_.getNumberOfSortKeys()*12);
      case 5: return 20;
      case 6: return 10;
      case 7:
        return authorityListener_ == null ? 28 :
          28 + (authorityListener_.getNumberOfObjectAuthorities()*10) +
               (authorityListener_.getNumberOfLibraryAuthorities()*10);
      case 8:
        return selectionListener_ == null ? 21 :
          20 + (selectionListener_.getNumberOfStatuses());
      case 9: return 4;
      case 10:
        return keys_ == null ? 0 : keys_.length*4;
      case 11: return 4;
    }
    return 0;
  }

  public int getParameterOutputLength(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return inputLength_;
      case 2: return 80;
      case 11: return 4;
    }
    return 0;
  }

  public int getParameterType(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0:
      case 2:
        return Parameter.TYPE_OUTPUT;
      case 11:
        return Parameter.TYPE_INPUT_OUTPUT;
    }
    return Parameter.TYPE_INPUT;
  }

  public byte[] getParameterInputData(final int parmIndex)
  {
    final byte[] tempData = getTempDataBuffer();
    switch (parmIndex)
    {
      case 1: Conv.intToByteArray(inputLength_, tempData, 0); return tempData;
      case 3: Conv.intToByteArray(numberOfRecordsToReturn_, tempData, 0); return tempData;
      case 4: // Sort information.
        if (sortListener_ == null)
        {
          Conv.intToByteArray(0, tempData, 0);
        }
        else
        {
          final int numberOfKeys = sortListener_.getNumberOfSortKeys();
          Conv.intToByteArray(numberOfKeys, tempData, 0);
          int offset = 4;
          for (int i=0; i<numberOfKeys; ++i)
          {
            Conv.intToByteArray(sortListener_.getSortKeyFieldStartingPosition(i), tempData, offset);
            Conv.intToByteArray(sortListener_.getSortKeyFieldLength(i), tempData, offset+4);
            Conv.shortToByteArray(sortListener_.getSortKeyFieldDataType(i), tempData, offset+8);
            tempData[offset+10] = sortListener_.isAscending(i) ? (byte)0xF1 : (byte)0xF2;
            tempData[offset+11] = 0;
            offset += 12;
          }
        }
        return tempData;
      case 5:
        Conv.stringToBlankPadEBCDICByteArray(objectName_, tempData, 0, 10);
        Conv.stringToBlankPadEBCDICByteArray(objectLibrary_, tempData, 10, 10);
        return tempData;
      case 6:
        Conv.stringToBlankPadEBCDICByteArray(objectType_, tempData, 0, 10);
        return tempData;
      case 7: // Authority control
        if (authorityListener_ == null)
        {
          Conv.intToByteArray(28, tempData, 0);
          for (int i=4; i<28; ++i) tempData[i] = 0;
        }
        else
        {
          Conv.intToByteArray(getParameterInputLength(7), tempData, 0);
          Conv.intToByteArray(authorityListener_.getCallLevel(), tempData, 4);
          final int numObjectAuthorities = authorityListener_.getNumberOfObjectAuthorities();
          int displacementToObjectAuthorities = numObjectAuthorities == 0 ? 0 : 28;
          Conv.intToByteArray(displacementToObjectAuthorities, tempData, 8);
          Conv.intToByteArray(numObjectAuthorities, tempData, 12);
          final int numLibraryAuthorities = authorityListener_.getNumberOfLibraryAuthorities();
          int displacementToLibraryAuthorities = numLibraryAuthorities == 0 ? 0 : (28 + (numObjectAuthorities*10));
          Conv.intToByteArray(displacementToLibraryAuthorities, tempData, 16);
          Conv.intToByteArray(numLibraryAuthorities, tempData, 20);
          Conv.intToByteArray(0, tempData, 24); // Reserved.
          int offset = 28;
          for (int i=0; i<numObjectAuthorities; ++i)
          {
            Conv.stringToBlankPadEBCDICByteArray(authorityListener_.getObjectAuthority(i), tempData, offset, 10);
            offset += 10;
          }
          for (int i=0; i<numLibraryAuthorities; ++i)
          {
            Conv.stringToBlankPadEBCDICByteArray(authorityListener_.getLibraryAuthority(i), tempData, offset, 10);
            offset += 10;
          }
        }
        return tempData;
      case 8: // Selection control
        if (selectionListener_ == null)
        {
          Conv.intToByteArray(21, tempData, 0);
          Conv.intToByteArray(0, tempData, 4);
          Conv.intToByteArray(20, tempData, 8);
          Conv.intToByteArray(1, tempData, 12);
          Conv.intToByteArray(0, tempData, 16);
          Conv.stringToEBCDICByteArray37("*", tempData, 20);
        }
        else
        {
          Conv.intToByteArray(getParameterInputLength(8), tempData, 0);
          Conv.intToByteArray(selectionListener_.isSelected() ? 0 : 1, tempData, 4);
          Conv.intToByteArray(20, tempData, 8);
          final int numStatuses = selectionListener_.getNumberOfStatuses();
          Conv.intToByteArray(numStatuses, tempData, 12);
          Conv.intToByteArray(0, tempData, 16); // Reserved.
          int offset = 20;
          for (int i=0; i<numStatuses; ++i)
          {
            Conv.stringToEBCDICByteArray37(selectionListener_.getStatus(i), tempData, offset++);
          }
        }
        return tempData;
      case 9:
        Conv.intToByteArray(keys_ == null ? 0 : keys_.length, tempData, 0);
        return tempData;
      case 10:
        if (keys_ != null)
        {
          int offset = 0;
          for (int i=0; i<keys_.length; ++i)
          {
            Conv.intToByteArray(keys_[i], tempData, offset);
            offset += 4;
          }
        }
        return tempData;
      case 11: return ZERO;
    }
    return null;
  }

  public void setParameterOutputData(final int parmIndex, final byte[] data, final int maxLength)
  {
    switch (parmIndex)
    {
      case 0:
        if (formatter_ != null)
        {
          formatter_.format(data, maxLength, 0, formatListener_);
        }
        break;
      case 2:
        if (maxLength < 12)
        {
          info_ = null;
        }
        else
        {
          info_ = Util.readOpenListInformationParameter(data, maxLength);
        }
        break;
      default:
        break;
    }
  }
}

