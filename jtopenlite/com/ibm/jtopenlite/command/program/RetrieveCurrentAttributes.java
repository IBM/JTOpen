///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  RetrieveCurrentAttributes.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

import com.ibm.jtopenlite.*;
import com.ibm.jtopenlite.command.*;

/**
 * <a href="http://publib.boulder.ibm.com/infocenter/iseries/v5r4/topic/apis/qwcrtvca.htm">QWCRTVCA</a>
 * @deprecated Use com.ibm.jtopenlit.command.porgram.wrokmgmt.RetrieveCurrentAttrbiutes instead
**/
public class RetrieveCurrentAttributes extends ProgramAdapter
{
  public static final int FORMAT_RTVC0100 = 0;
  public static final int FORMAT_RTVC0200 = 1;
  public static final int FORMAT_RTVC0300 = 2;

  private int inputFormat_;
  private int inputLength_;
  private int[] attributesToReturn_;

  private int numberOfAttributesReturned_;

  private int bytesReturned_;
  private int bytesAvailable_;

  private int numberOfLibrariesInSYSLIBL_;
  private int numberOfProductLibraries_;
  private boolean currentLibraryExistence_;
  private int numberOfLibrariesInUSRLIBL_;

  private int numberOfASPGroups_;

  private JobKeyDataListener keyDataListener_;
  private RetrieveCurrentAttributesLibraryListener libraryListener_;
  private RetrieveCurrentAttributesASPGroupListener aspGroupListener_;

  public RetrieveCurrentAttributes(int format, int lengthOfReceiverVariable, int[] attributesToReturn)
  {
    super("QSYS", "QWCRTVCA", 6);
    inputFormat_ = format;
    inputLength_ = lengthOfReceiverVariable <= 0 ? 1 : lengthOfReceiverVariable;
    attributesToReturn_ = attributesToReturn == null ? new int[0] : attributesToReturn;
  }

  void clearOutputData()
  {
    numberOfAttributesReturned_ = 0;
    bytesReturned_ = 0;
    bytesAvailable_ = 0;
    numberOfLibrariesInSYSLIBL_ = 0;
    numberOfProductLibraries_ = 0;
    currentLibraryExistence_ = false;
    numberOfLibrariesInUSRLIBL_ = 0;
    numberOfASPGroups_ = 0;
  }

  public void setFormat(int format)
  {
    inputFormat_ = format;
  }

  public int getFormat()
  {
    return inputFormat_;
  }

  public int getLengthOfReceiverVariable()
  {
    return inputLength_;
  }

  public void setLengthOfReceiverVariable(int lengthOfReceiverVariable)
  {
    inputLength_ = lengthOfReceiverVariable <= 0 ? 1 : lengthOfReceiverVariable;
  }

  public int[] getAttributesToReturn()
  {
    return attributesToReturn_;
  }

  public void setAttributesToReturn(int[] attributesToReturn)
  {
    attributesToReturn_ = attributesToReturn == null ? new int[0] : attributesToReturn;
  }

  public int getNumberOfAttributesReturned()
  {
    return numberOfAttributesReturned_;
  }

  public int getBytesAvailable()
  {
    return bytesAvailable_;
  }

  public int getBytesReturned()
  {
    return bytesReturned_;
  }

  public int getNumberOfSystemLibraries()
  {
    return numberOfLibrariesInSYSLIBL_;
  }

  public int getNumberOfProductLibraries()
  {
    return numberOfProductLibraries_;
  }

  public boolean hasCurrentLibrary()
  {
    return currentLibraryExistence_;
  }

  public int getNumberOfUserLibraries()
  {
    return numberOfLibrariesInUSRLIBL_;
  }

  public int getNumberOfASPGroups()
  {
    return numberOfASPGroups_;
  }

  int getParameterInputLengthSubclass(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return 0;
      case 1: return 4;
      case 2: return 8;
      case 3: return 4;
      case 4: return 4*attributesToReturn_.length;
      case 5: return 4;
    }
    return 0;
  }

  int getParameterOutputLengthSubclass(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return inputLength_;
      case 5: return 4;
    }
    return 0;
  }

  private String getFormatName()
  {
    switch (inputFormat_)
    {
      case FORMAT_RTVC0100: return "RTVC0100";
      case FORMAT_RTVC0200: return "RTVC0200";
      case FORMAT_RTVC0300: return "RTVC0300";
    }
    return null;
  }

  int getParameterTypeSubclass(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return Parameter.TYPE_OUTPUT;
      case 5: return Parameter.TYPE_INPUT_OUTPUT;
    }
    return Parameter.TYPE_INPUT;
  }

  byte[] getParameterInputDataSubclass(final int parmIndex)
  {
    final byte[] tempData = getTempDataBuffer();
    switch (parmIndex)
    {
      case 1: Conv.intToByteArray(inputLength_, tempData, 0); return tempData;
      case 2: Conv.stringToEBCDICByteArray37(getFormatName(), tempData, 0); return tempData;
      case 3: Conv.intToByteArray(attributesToReturn_.length, tempData, 0); return tempData;
      case 4:
        for (int i=0; i<attributesToReturn_.length; ++i)
        {
          Conv.intToByteArray(attributesToReturn_[i], tempData, i*4);
        }
        return tempData;
      case 5: return ZERO;
    }
    return null;
  }

  public void setKeyDataListener(JobKeyDataListener listener)
  {
    keyDataListener_ = listener;
  }

  public void setLibraryListener(RetrieveCurrentAttributesLibraryListener listener)
  {
    libraryListener_ = listener;
  }

  public void setASPGroupListener(RetrieveCurrentAttributesASPGroupListener listener)
  {
    aspGroupListener_ = listener;
  }

  void setParameterOutputDataSubclass(final int parmIndex, final byte[] data, final int maxLength)
  {
    final char[] c = new char[20];
    switch (parmIndex)
    {
      case 0:
        int numRead = 0;
        switch (inputFormat_)
        {
          case FORMAT_RTVC0100:
            if (maxLength >= 4)
            {
              numberOfAttributesReturned_ = Conv.byteArrayToInt(data, numRead);
              numRead += 4;
            }
            if (keyDataListener_ != null)
            {
              for (int i=0; i<numberOfAttributesReturned_ && numRead+16 <= maxLength; ++i)
              {
                int lengthOfAttributeInfoReturned = Conv.byteArrayToInt(data, numRead);
                numRead += 4;
                int key = Conv.byteArrayToInt(data, numRead);
                numRead += 4;
                int typeOfData = data[numRead] & 0x00FF;
                final boolean isBinary = typeOfData == 0x00C2;
                numRead += 4;
                int lengthOfData = Conv.byteArrayToInt(data, numRead);
                numRead += 4;
                if (numRead+lengthOfData <= maxLength)
                {
                  Util.readKeyData(data, numRead, key, lengthOfData, isBinary, keyDataListener_, c);
                  numRead += lengthOfData;
                  int skip = lengthOfAttributeInfoReturned-16-lengthOfData;
                  if (numRead+skip <= maxLength)
                  {
                    numRead += skip;
                  }
                  else
                  {
                    numRead = maxLength;
                  }
                }
                else
                {
                  numRead = maxLength;
                }
              }
            }
            break;

          case FORMAT_RTVC0200:
            if (maxLength >= 8)
            {
              bytesReturned_ = Conv.byteArrayToInt(data, 0);
              bytesAvailable_ = Conv.byteArrayToInt(data, 4);
              numRead += 8;
            }
            if (maxLength >= 24)
            {
              numberOfLibrariesInSYSLIBL_ = Conv.byteArrayToInt(data, numRead);
              numRead += 4;
              numberOfProductLibraries_ = Conv.byteArrayToInt(data, numRead);
              numRead += 4;
              currentLibraryExistence_ = Conv.byteArrayToInt(data, numRead) == 1;
              numRead += 4;
              numberOfLibrariesInUSRLIBL_ = Conv.byteArrayToInt(data, numRead);
              numRead += 4;
              if (libraryListener_ != null)
              {
                for (int i=0; i<numberOfLibrariesInSYSLIBL_ && numRead+11 <= maxLength; ++i)
                {
                  String lib = Conv.ebcdicByteArrayToString(data, numRead, 11, c);
                  numRead += 11;
                  libraryListener_.newSystemLibrary(lib);
                }
                for (int i=0; i<numberOfProductLibraries_ && numRead+11 <= maxLength; ++i)
                {
                  String lib = Conv.ebcdicByteArrayToString(data, numRead, 11, c);
                  numRead += 11;
                  libraryListener_.newProductLibrary(lib);
                }
                if (currentLibraryExistence_ && numRead+11 <= maxLength)
                {
                  String lib = Conv.ebcdicByteArrayToString(data, numRead, 11, c);
                  numRead += 11;
                  libraryListener_.currentLibrary(lib);
                }
                for (int i=0; i<numberOfLibrariesInUSRLIBL_ && numRead+11 <= maxLength; ++i)
                {
                  String lib = Conv.ebcdicByteArrayToString(data, numRead, 11, c);
                  numRead += 11;
                  libraryListener_.newUserLibrary(lib);
                }
              }
            }
            break;

          case FORMAT_RTVC0300:
            if (maxLength >= 8)
            {
              bytesReturned_ = Conv.byteArrayToInt(data, 0);
              bytesAvailable_ = Conv.byteArrayToInt(data, 4);
              numRead += 8;
            }
            if (maxLength >= 20)
            {
              int offsetToASPGroupInformation = Conv.byteArrayToInt(data, numRead);
              numRead += 4;
              numberOfASPGroups_ = Conv.byteArrayToInt(data, numRead);
              numRead += 4;
              int lengthOfASPGroupEntry = Conv.byteArrayToInt(data, numRead);
              numRead += 8;
              if (aspGroupListener_ != null)
              {
                int skip = offsetToASPGroupInformation-20;
                if (numRead+skip <= maxLength)
                {
                  numRead += skip;
                  for (int i=0; i<numberOfASPGroups_ && numRead+10 <= maxLength; ++i)
                  {
                    String aspGroupName = Conv.ebcdicByteArrayToString(data, numRead, 10, c);
                    aspGroupListener_.newASPGroup(aspGroupName);
                    skip = lengthOfASPGroupEntry-10;
                    if (numRead+skip <= maxLength)
                    {
                      numRead += skip;
                    }
                    else
                    {
                      numRead = maxLength;
                    }
                  }
                }
              }
            }
            break;
        }
        break;

      default:
        break;
    }
  }
}

