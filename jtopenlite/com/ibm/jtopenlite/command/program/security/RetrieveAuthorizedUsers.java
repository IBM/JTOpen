///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename: RetrieveAuthorizedUsers.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.security;

import com.ibm.jtopenlite.*;
import com.ibm.jtopenlite.command.*;

/**
 * <a href="http://publib.boulder.ibm.com/infocenter/iseries/v5r4/topic/apis/qsyrautu.htm">QSYRAUTU</a>
**/
public class RetrieveAuthorizedUsers implements Program
{
  private static final byte[] ZERO = new byte[4];

  public static final int FORMAT_AUTU0100 = 0;
  public static final int FORMAT_AUTU0150 = 1;
  public static final int FORMAT_AUTU0200 = 2;
  public static final int FORMAT_AUTU0250 = 3;

  public static final String SELECTION_ALL = "*ALL";
  public static final String SELECTION_USER = "*USER";
  public static final String SELECTION_GROUP = "*GROUP";
  public static final String SELECTION_MEMBER = "*MEMBER";

  public static final String STARTING_PROFILE_FIRST = "*FIRST";

  public static final String GROUP_NONE = "*NONE";
  public static final String GROUP_NO_GROUP = "*NOGROUP";

  public static final String ENDING_PROFILE_LAST = "*LAST";

  private int inputFormat_;
  private int inputLength_;
  private String inputSelection_;
  private String inputStart_;
  private boolean inputIncludeStart_;
  private String inputGroup_;
  private String inputEnd_;

  private int bytesReturned_;
  private int bytesAvailable_;
  private int numberOfProfileNames_;

  private RetrieveAuthorizedUsersListener listener_;

  private byte[] tempData_;

  public RetrieveAuthorizedUsers(int format, int lengthOfReceiverVariable, String selectionCriteria, String startingProfileName, boolean includeStartingProfile, String groupProfileName, String endingProfileName)
  {
    inputFormat_ = format;
    inputLength_ = lengthOfReceiverVariable <= 0 ? 1 : lengthOfReceiverVariable;
    inputSelection_ = selectionCriteria == null ? SELECTION_ALL : selectionCriteria;
    inputStart_ = startingProfileName == null ? STARTING_PROFILE_FIRST : startingProfileName;
    inputIncludeStart_ = includeStartingProfile;
    inputGroup_ = groupProfileName == null ? GROUP_NONE : groupProfileName;
    inputEnd_ = endingProfileName;
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

  public String getProgramName()
  {
    return "QSYRAUTU";
  }

  public String getProgramLibrary()
  {
    return "QSYS";
  }

  public int getNumberOfParameters()
  {
    return (inputEnd_ == null ? 9 : 10);
  }

  public void newCall()
  {
    bytesReturned_ = 0;
    bytesAvailable_ = 0;
    numberOfProfileNames_ = 0;
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

  public int getBytesReturned()
  {
    return bytesReturned_;
  }

  public int getBytesAvailable()
  {
    return bytesAvailable_;
  }

  public int getNumberOfProfileNames()
  {
    return numberOfProfileNames_;
  }

  public int getParameterInputLength(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return 0;
      case 1: return 4;
      case 2: return 0;
      case 3: return 8;
      case 4: return 10;
      case 5: return 10;
      case 6: return 1;
      case 7: return 10;
      case 8: return 4;
      case 9: return 10;
    }
    return 0;
  }

  public int getParameterOutputLength(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return inputLength_;
      case 2: return 16;
      case 8: return 4;
    }
    return 0;
  }

  private String getFormatName()
  {
    switch (inputFormat_)
    {
      case FORMAT_AUTU0100: return "AUTU0100";
      case FORMAT_AUTU0150: return "AUTU0150";
      case FORMAT_AUTU0200: return "AUTU0200";
      case FORMAT_AUTU0250: return "AUTU0250";
    }
    return null;
  }

  public int getParameterType(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return Parameter.TYPE_OUTPUT;
      case 2: return Parameter.TYPE_OUTPUT;
      case 8: return Parameter.TYPE_INPUT_OUTPUT;
    }
    return Parameter.TYPE_INPUT;
  }

  public byte[] getParameterInputData(final int parmIndex)
  {
    final byte[] tempData = getTempDataBuffer();
    switch (parmIndex)
    {
      case 1: Conv.intToByteArray(inputLength_, tempData, 0); return tempData;
      case 3: Conv.stringToEBCDICByteArray37(getFormatName(), tempData, 0); return tempData;
      case 4: Conv.stringToBlankPadEBCDICByteArray(inputSelection_, tempData, 0, 10); return tempData;
      case 5: Conv.stringToBlankPadEBCDICByteArray(inputStart_, tempData, 0, 10); return tempData;
      case 6: tempData[0] = inputIncludeStart_ ? (byte)0xF1 : (byte)0xF0; return tempData;
      case 7: Conv.stringToBlankPadEBCDICByteArray(inputGroup_, tempData, 0, 10); return tempData;
      case 8: return ZERO;
      case 9: Conv.stringToBlankPadEBCDICByteArray(inputEnd_, tempData, 0, 10); return tempData;
    }
    return null;
  }

  public void setListener(RetrieveAuthorizedUsersListener listener)
  {
    listener_ = listener;
  }

  public void setParameterOutputData(final int parmIndex, final byte[] data, final int maxLength)
  {
    switch (parmIndex)
    {
      case 2:
        bytesReturned_ = Conv.byteArrayToInt(data, 0);
        bytesAvailable_ = Conv.byteArrayToInt(data, 4);
        numberOfProfileNames_ = Conv.byteArrayToInt(data, 8);
        // int entryLength = Conv.byteArrayToInt(data, 12);
        break;
      case 0:
        if (listener_ == null)
        {
          return;
        }
        int numRead = 0;
        final char[] c = new char[50];
        while (numRead < maxLength)
        {
          int remaining = maxLength - numRead;
          switch (inputFormat_)
          {
            case FORMAT_AUTU0100:
              if (remaining >= 12)
              {
                String profileName = Conv.ebcdicByteArrayToString(data, numRead, 10, c);
                numRead += 10;
                int userOrGroupIndicator = data[numRead++] & 0x00FF;
                int groupMembersIndicator = data[numRead++] & 0x00FF;
                listener_.newEntry(profileName, userOrGroupIndicator == 0x00F1, groupMembersIndicator == 0x00F1, null, null);
              }
              else
              {
                numRead += remaining;
              }
              break;
            case FORMAT_AUTU0150:
              if (remaining >= 62)
              {
                String profileName = Conv.ebcdicByteArrayToString(data, numRead, 10, c);
                numRead += 10;
                int userOrGroupIndicator = data[numRead++] & 0x00FF;
                int groupMembersIndicator = data[numRead++] & 0x00FF;
                String textDescription = Conv.ebcdicByteArrayToString(data, numRead, 50, c);
                numRead += 50;
                listener_.newEntry(profileName, userOrGroupIndicator == 0x00F1, groupMembersIndicator == 0x00F1, textDescription, null);
              }
              else
              {
                numRead += remaining;
              }
              break;
            case FORMAT_AUTU0200:
              if (remaining >= 176)
              {
                String profileName = Conv.ebcdicByteArrayToString(data, numRead, 10, c);
                numRead += 10;
                int userOrGroupIndicator = data[numRead++] & 0x00FF;
                int groupMembersIndicator = data[numRead++] & 0x00FF;
                int numberOfGroupProfiles = Conv.byteArrayToInt(data, numRead);
                numRead += 4;
                String[] groupProfiles = new String[numberOfGroupProfiles];
                if (numberOfGroupProfiles > 0)
                {
                  for (int i=0; i<numberOfGroupProfiles; ++i)
                  {
                    groupProfiles[i] = Conv.ebcdicByteArrayToString(data, numRead, 10, c);
                    numRead += 10;
                  }
                  numRead += 10*(16-numberOfGroupProfiles);
                }
                else
                {
                  numRead += 160;
                }
                listener_.newEntry(profileName, userOrGroupIndicator == 0x00F1, groupMembersIndicator == 0x00F1, null, groupProfiles);
              }
              else
              {
                numRead += remaining;
              }
              break;
            case FORMAT_AUTU0250:
              if (remaining >= 228)
              {
                String profileName = Conv.ebcdicByteArrayToString(data, numRead, 10, c);
                numRead += 10;
                int userOrGroupIndicator = data[numRead++] & 0x00FF;
                int groupMembersIndicator = data[numRead++] & 0x00FF;
                String textDescription = Conv.ebcdicByteArrayToString(data, numRead, 50, c);
                numRead += 50;
                numRead += 2;
                int numberOfGroupProfiles = Conv.byteArrayToInt(data, numRead);
                numRead += 4;
                String[] groupProfiles = new String[numberOfGroupProfiles];
                if (numberOfGroupProfiles > 0)
                {
                  groupProfiles = new String[numberOfGroupProfiles];
                  for (int i=0; i<numberOfGroupProfiles; ++i)
                  {
                    groupProfiles[i] = Conv.ebcdicByteArrayToString(data, numRead, 10, c);
                    numRead += 10;
                  }
                  numRead += 10*(16-numberOfGroupProfiles);
                }
                else
                {
                  numRead += 160;
                }
                listener_.newEntry(profileName, userOrGroupIndicator == 0x00F1, groupMembersIndicator == 0x00F1, textDescription, groupProfiles);
              }
              else
              {
                numRead += remaining;
              }
              break;
            default:
              numRead += remaining;
              break;
          }
        }
        break;
      default:
        break;
    }
  }
}

