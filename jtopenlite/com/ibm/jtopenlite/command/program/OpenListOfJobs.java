///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OpenListOfJobs.java
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
 * <a href="http://publib.boulder.ibm.com/infocenter/iseries/v5r4/topic/apis/qgyoljob.htm">QGYOLJOB</a>
 * This class fully implements the V5R4 specification of QGYOLJOB.
 *
 * <p></p>
 * Example:
 *
 * <pre>
 * CommandConnection conn = CommandConnection.getConnection(...);
 *
 * OpenListOfJobsFormatOLJB0200 jobFormat = new OpenListOfJobsFormatOLJB0200(); // Use the OLJB0200 format to get key info back.
 * int receiverSize = 100; // This should be large enough for the initial call.
 * int numRecordsToReturn = 1; // Need to return at least one record to get the key definition back.
 * int[] fieldsToReturn = new int[] { 1906 }; // Subsystem information.
 *
 * OpenListOfJobs jobList = new OpenListOfJobs(jobFormat, receiverSize, numRecordsToReturn, fieldsToReturn);
 *
 * OpenListOfJobsSelectionListener jobSelector = ...; // Define your own. Optional.
 * jobList.setSelectionListener(jobSelector, OpenListOfJobs.SELECTION_OLJS0100);
 *
 * OpenListOfJobsSortListener jobSorter = ...; // Define your own. Optional.
 * jobList.setSortListener(jobSorter);
 *
 * CommandResult result = conn.call(jobList);
 * // Assuming it succeeded...
 *
 * OpenListOfJobsKeyField[] keyDefinitions = jobList.getKeyFields();
 * ListInformation listInfo = jobList.getListInformation();
 * byte[] requestHandle = listInfo.getRequestHandle();
 * int recordLength = listInfo.getRecordLength();
 *
 * // Now, the list is building on the server.
 * // Call GetListEntries once to wait for the list to finish building, for example.
 * receiverSize = 100; // Should be good enough for the first call.
 * numRecordsToReturn = 0;
 * int startingRecord = -1; // Wait until whole list is built before returning. Optional.
 * GetListEntries getJobs = new GetListEntries(receiverSize, requestHandle, recordLength, numRecordsToReturn, startingRecord, jobFormat);
 * result = conn.call(getJobs);
 * // Assuming it succeeded...
 *
 * listInfo = getJobs.getListInformation();
 * int totalRecords = listInfo.getTotalRecords();
 *
 * // Now retrieve the job records in chunks of, for example, 300 at a time.
 * numRecordsToReturn = 300;
 * receiverSize = recordLength * numRecordsToReturn;
 * startingRecord = 1;
 * getJobs.setLengthOfReceiverVariable(receiverSize);
 * getJobs.setNumberOfRecordsToReturn(numRecordsToReturn);
 * getJobs.setStartingRecord(startingRecord);
 * jobFormat.setKeyFields(keyDefinitions);
 * OpenListOfJobsFormatOLJB0200Listener callback = ...; // Define your own.
 * jobFormat.setListener(callback); // Ready to process.
 *
 * while (startingRecord <= totalRecords)
 * {
 *   result = conn.call(getJobs);
 *   // Assuming it succeeded...
 *   listInfo = getJobs.getListInformation();
 *   startingRecord += listInfo.getRecordsReturned();
 *   getJobs.setStartingRecord(startingRecord);
 * }
 *
 * // All done.
 * CloseList close = new CloseList(requestHandle);
 * result = conn.call(close);
 * conn.close();
 * </pre>
**/
public class OpenListOfJobs extends ProgramAdapter
{
  public static final int SELECTION_OLJS0100 = 3;
  public static final int SELECTION_OLJS0200 = 4;

  private OpenListOfJobsFormat inputFormat_;
  private int inputLength_;
  private int numberOfRecordsToReturn_;
  private OpenListOfJobsSortListener sortListener_;
  private int[] fieldsToReturn_;
  private int selectionFormat_ = SELECTION_OLJS0100;
  private OpenListOfJobsSelectionListener selectionListener_;
  private boolean resetStats_;
  private long elapsedTime_;
  private ListInformation info_;
  private OpenListOfJobsKeyField[] keyFields_;

  public OpenListOfJobs(OpenListOfJobsFormat format, int lengthOfReceiverVariable, int numberOfRecordsToReturn, int[] fieldsToReturn)
  {
    super("QGY", "QGYOLJOB", 13);
    inputFormat_ = format;
    inputLength_ = lengthOfReceiverVariable <= 0 ? 1 : lengthOfReceiverVariable;
    numberOfRecordsToReturn_ = numberOfRecordsToReturn;
    fieldsToReturn_ = fieldsToReturn == null ? new int[0] : fieldsToReturn;
  }

  void clearOutputData()
  {
    elapsedTime_ = 0;
    info_ = null;
    keyFields_ = null;
  }

  public OpenListOfJobsKeyField[] getKeyFields()
  {
    return keyFields_;
  }

  public ListInformation getListInformation()
  {
    return info_;
  }

  int getNumberOfParametersSubclass()
  {
    int num = 13;
    if (selectionListener_ != null)
    {
      num++;
    }
    if (inputFormat_.getType() == OpenListOfJobsFormat.FORMAT_OLJB0300)
    {
      num += 3;
      if (selectionListener_ == null)
      {
        num++;
      }
    }
    return num;
  }

  public OpenListOfJobsSortListener getSortListener()
  {
    return sortListener_;
  }

  public void setSortListener(OpenListOfJobsSortListener sortListener)
  {
    sortListener_ = sortListener;
  }

  public boolean getResetStatusStatistics()
  {
    return resetStats_;
  }

  public void setResetStatusStatistics(boolean resetStatusStatistics)
  {
    resetStats_ = resetStatusStatistics;
  }

  public long getElapsedTime()
  {
    return elapsedTime_;
  }

  public int getNumberOfRecordsToReturn()
  {
    return numberOfRecordsToReturn_;
  }

  public void setNumberOfRecordsToReturn(int numberOfRecordsToReturn)
  {
    numberOfRecordsToReturn_ = numberOfRecordsToReturn;
  }

  public int[] getFieldsToReturn()
  {
    return fieldsToReturn_;
  }

  public void setFieldsToReturn(int[] fieldsToReturn)
  {
    fieldsToReturn_ = fieldsToReturn == null ? new int[0] : fieldsToReturn;
  }

  public OpenListOfJobsSelectionListener getSelectionListener()
  {
    return selectionListener_;
  }

  public void setSelectionListener(OpenListOfJobsSelectionListener selectionListener, int selectionFormat)
  {
    selectionListener_ = selectionListener;
    selectionFormat_ = selectionListener_ == null ? SELECTION_OLJS0100 : selectionFormat;
  }

  int getParameterInputLengthSubclass(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return 0;
      case 1: return 4;
      case 2: return 8;
      case 3: return 0;
      case 4: return 4;
      case 5: return 0;
      case 6: return 4;
      case 7: return sortListener_ == null ? 4 : 4+12*sortListener_.getNumberOfSortKeys();
      case 8:
        int num = 60;
        if (selectionListener_ != null)
        {
          num += 10*selectionListener_.getPrimaryJobStatusCount() +
                 4*selectionListener_.getActiveJobStatusCount() +
                 10*selectionListener_.getJobsOnJobQueueStatusCount() +
                 20*selectionListener_.getJobQueueNameCount();
          if (selectionFormat_ == SELECTION_OLJS0200)
          {
            num += 48 + 10*selectionListener_.getCurrentUserProfileCount() +
                   30*selectionListener_.getServerTypeCount() +
                   10*selectionListener_.getActiveSubsystemCount() +
                   4*selectionListener_.getMemoryPoolCount() +
                   4*selectionListener_.getJobTypeEnhancedCount() +
                   26*selectionListener_.getQualifiedJobNameCount();
          }
        }
        return num;
      case 9: return 4;
      case 10: return 4;
      case 11: return 4*fieldsToReturn_.length;
      case 12: return 4;
      case 13: return 8;
      case 14: return 1;
      case 15: return 0;
      case 16: return 4;
    }
    return 0;
  }

  int getParameterOutputLengthSubclass(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return inputLength_;
      case 3: return 4+20*fieldsToReturn_.length;
      case 5: return 80;
      case 12: return 4;
      case 15: return 16;
    }
    return 0;
  }

//  private String getFormatName()
//  {
//    switch (inputFormat_)
//    {
//      case FORMAT_OLJB0100: return "OLJB0100";
//      case FORMAT_OLJB0200: return "OLJB0200";
//      case FORMAT_OLJB0300: return "OLJB0300";
//    }
//    return null;
//  }

  private String getSelectionFormatName()
  {
    switch (selectionFormat_)
    {
      case SELECTION_OLJS0100: return "OLJS0100";
      case SELECTION_OLJS0200: return "OLJS0200";
    }
    return null;
  }

  int getParameterTypeSubclass(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0:
      case 3:
      case 5:
      case 15:
        return Parameter.TYPE_OUTPUT;
      case 12:
        return Parameter.TYPE_INPUT_OUTPUT;
    }
    return Parameter.TYPE_INPUT;
  }

//  void writeParameterInputDataSubclass(final HostOutputStream out, final int parmIndex) throws IOException
  byte[] getParameterInputDataSubclass(final int parmIndex)
  {
    final byte[] tempData = getTempDataBuffer();
    switch (parmIndex)
    {
      case 1: Conv.intToByteArray(inputLength_, tempData, 0); return tempData;
      case 2: Conv.stringToEBCDICByteArray37(inputFormat_.getName(), tempData, 0); return tempData;
      case 4: Conv.intToByteArray(getParameterOutputLength(3), tempData, 0); return tempData;
      case 6: Conv.intToByteArray(numberOfRecordsToReturn_, tempData, 0); return tempData;
      case 7: // Sort info.
        final int numSortKeys = sortListener_ == null ? 0 : sortListener_.getNumberOfSortKeys();
        Conv.intToByteArray(numSortKeys, tempData, 0);
//        out.writeInt(numSortKeys);
        int offset = 4;
        for (int i=0; i<numSortKeys; ++i)
        {
//          out.writeInt(sortListener_.getSortKeyFieldStartingPosition(i));
          Conv.intToByteArray(sortListener_.getSortKeyFieldStartingPosition(i), tempData, offset);
          offset += 4;
          //out.writeInt(sortListener_.getSortKeyFieldLength(i));
          Conv.intToByteArray(sortListener_.getSortKeyFieldLength(i), tempData, offset);
          offset += 4;
          //out.writeShort(sortListener_.getSortKeyFieldDataType(i));
          Conv.shortToByteArray(sortListener_.getSortKeyFieldDataType(i), tempData, offset);
          offset += 2;
//          out.write(sortListener_.isAscending(i) ? 0xF1 : 0xF2);
          tempData[offset++] = sortListener_.isAscending(i) ? (byte)0xF1 : (byte)0xF2;
//          out.write(0);
          tempData[offset++] = 0;
        }
        return tempData;
      case 8: // Job selection info.
        final String jobName = selectionListener_ == null ? "*CURRENT" : selectionListener_.getJobName();
        final String userName = selectionListener_ == null ? "*ALL" : selectionListener_.getUserName();
        final String jobNumber = selectionListener_ == null ? "*ALL" : selectionListener_.getJobNumber();
        final String jobType = selectionListener_ == null ? "*" : selectionListener_.getJobType();
//        out.write(Util.blankPad(jobName, 10));
        //Conv.writePadEBCDIC(jobName, 10, out);
        Conv.stringToBlankPadEBCDICByteArray(jobName, tempData, 0, 10);
//        out.write(Util.blankPad(userName, 10));
        //Conv.writePadEBCDIC(userName, 10, out);
        Conv.stringToBlankPadEBCDICByteArray(userName, tempData, 10, 10);
//        out.write(Util.blankPad(jobNumber, 6));
        //Conv.writePadEBCDIC(jobNumber, 6, out);
        Conv.stringToBlankPadEBCDICByteArray(jobNumber, tempData, 20, 6);
//        out.write(Util.blankPad(jobType, 1));
        //Conv.writePadEBCDIC(jobType, 1, out);
        Conv.stringToBlankPadEBCDICByteArray(jobType, tempData, 26, 1);
        //out.write(0);
        tempData[27] = 0;
        int tempOffset = 28;
        if (selectionListener_ == null)
        {
          //for (int i=0; i<8; ++i) out.writeInt(0);
          for (int i=28; i<60; i+=2)
          {
            tempData[i] = 0;
            tempData[i+1] = 0;
          }
        }
        else
        {
          offset = selectionFormat_ == SELECTION_OLJS0200 ? 108 : 60;
          //out.writeInt(offset);
          Conv.intToByteArray(offset, tempData, tempOffset);
          tempOffset += 4;
          final int primaryJobStatusCount = selectionListener_.getPrimaryJobStatusCount();
          //out.writeInt(primaryJobStatusCount);
          Conv.intToByteArray(primaryJobStatusCount, tempData, tempOffset);
          tempOffset += 4;
          offset += 10*primaryJobStatusCount;
          //out.writeInt(offset);
          Conv.intToByteArray(offset, tempData, tempOffset);
          tempOffset += 4;
          final int activeJobStatusCount = selectionListener_.getActiveJobStatusCount();
          //out.writeInt(activeJobStatusCount);
          Conv.intToByteArray(activeJobStatusCount, tempData, tempOffset);
          tempOffset += 4;
          offset += 4*activeJobStatusCount;
          //out.writeInt(offset);
          Conv.intToByteArray(offset, tempData, tempOffset);
          tempOffset += 4;
          final int jobsOnJobQueueStatusCount = selectionListener_.getJobsOnJobQueueStatusCount();
          //out.writeInt(jobsOnJobQueueStatusCount);
          Conv.intToByteArray(jobsOnJobQueueStatusCount, tempData, tempOffset);
          tempOffset += 4;
          offset += 10*jobsOnJobQueueStatusCount;
          //out.writeInt(offset);
          Conv.intToByteArray(offset, tempData, tempOffset);
          tempOffset += 4;
          final int jobQueueNameCount = selectionListener_.getJobQueueNameCount();
          //out.writeInt(jobQueueNameCount);
          Conv.intToByteArray(jobQueueNameCount, tempData, tempOffset);
          tempOffset += 4;
          offset += 20*jobQueueNameCount;
          final int currentUserProfileCount = selectionListener_.getCurrentUserProfileCount();
          final int serverTypeCount = selectionListener_.getServerTypeCount();
          final int activeSubsystemCount = selectionListener_.getActiveSubsystemCount();
          final int memoryPoolCount = selectionListener_.getMemoryPoolCount();
          final int jobTypeEnhancedCount = selectionListener_.getJobTypeEnhancedCount();
          final int qualifiedJobNameCount = selectionListener_.getQualifiedJobNameCount();
          if (selectionFormat_ == SELECTION_OLJS0200)
          {
            //out.writeInt(offset);
            Conv.intToByteArray(offset, tempData, tempOffset);
            tempOffset += 4;
            //out.writeInt(currentUserProfileCount);
            Conv.intToByteArray(currentUserProfileCount, tempData, tempOffset);
            tempOffset += 4;
            offset += 10*currentUserProfileCount;
            //out.writeInt(offset);
            Conv.intToByteArray(offset, tempData, tempOffset);
            tempOffset += 4;
            //out.writeInt(serverTypeCount);
            Conv.intToByteArray(serverTypeCount, tempData, tempOffset);
            tempOffset += 4;
            offset += 30*serverTypeCount;
            //out.writeInt(offset);
            Conv.intToByteArray(offset, tempData, tempOffset);
            tempOffset += 4;
            //out.writeInt(activeSubsystemCount);
            Conv.intToByteArray(activeSubsystemCount, tempData, tempOffset);
            tempOffset += 4;
            offset += 10*activeSubsystemCount;
            //out.writeInt(offset);
            Conv.intToByteArray(offset, tempData, tempOffset);
            tempOffset += 4;
            //out.writeInt(memoryPoolCount);
            Conv.intToByteArray(memoryPoolCount, tempData, tempOffset);
            tempOffset += 4;
            offset += 4*memoryPoolCount;
            //out.writeInt(offset);
            Conv.intToByteArray(offset, tempData, tempOffset);
            tempOffset += 4;
            //out.writeInt(jobTypeEnhancedCount);
            Conv.intToByteArray(jobTypeEnhancedCount, tempData, tempOffset);
            tempOffset += 4;
            offset += 4*jobTypeEnhancedCount;
            //out.writeInt(offset);
            Conv.intToByteArray(offset, tempData, tempOffset);
            tempOffset += 4;
            //out.writeInt(qualifiedJobNameCount);
            Conv.intToByteArray(qualifiedJobNameCount, tempData, tempOffset);
            tempOffset += 4;
          }
          for (int i=0; i<primaryJobStatusCount; ++i)
          {
            //out.write(Util.blankPad(selectionListener_.getPrimaryJobStatus(i), 10));
            //Conv.writePadEBCDIC10(selectionListener_.getPrimaryJobStatus(i), out);
            Conv.stringToBlankPadEBCDICByteArray(selectionListener_.getPrimaryJobStatus(i), tempData, tempOffset, 10);
            tempOffset += 10;
          }
          for (int i=0; i<selectionListener_.getActiveJobStatusCount(); ++i)
          {
            //out.write(Util.blankPad(selectionListener_.getActiveJobStatus(i), 4));
            //Conv.writePadEBCDIC(selectionListener_.getActiveJobStatus(i), 4, out);
            Conv.stringToBlankPadEBCDICByteArray(selectionListener_.getActiveJobStatus(i), tempData, tempOffset, 4);
            tempOffset += 4;
          }
          for (int i=0; i<selectionListener_.getJobsOnJobQueueStatusCount(); ++i)
          {
            //out.write(Util.blankPad(selectionListener_.getJobsOnJobQueueStatus(i), 10));
            //Conv.writePadEBCDIC10(selectionListener_.getJobsOnJobQueueStatus(i), out);
            Conv.stringToBlankPadEBCDICByteArray(selectionListener_.getJobsOnJobQueueStatus(i), tempData, tempOffset, 10);
            tempOffset += 10;
          }
          for (int i=0; i<selectionListener_.getJobQueueNameCount(); ++i)
          {
            //out.write(Util.blankPad(selectionListener_.getJobQueueName(i), 20));
            //Conv.writePadEBCDIC(selectionListener_.getJobQueueName(i), 20, out);
            Conv.stringToBlankPadEBCDICByteArray(selectionListener_.getJobQueueName(i), tempData, tempOffset, 20);
            tempOffset += 20;
          }
          if (selectionFormat_ == SELECTION_OLJS0200)
          {
            for (int i=0; i<currentUserProfileCount; ++i)
            {
              //out.write(Util.blankPad(selectionListener_.getCurrentUserProfile(i), 10));
              //Conv.writePadEBCDIC10(selectionListener_.getCurrentUserProfile(i), out);
              Conv.stringToBlankPadEBCDICByteArray(selectionListener_.getCurrentUserProfile(i), tempData, tempOffset, 10);
              tempOffset += 10;
            }
            for (int i=0; i<serverTypeCount; ++i)
            {
              //out.write(Util.blankPad(selectionListener_.getServerType(i), 30));
              //Conv.writePadEBCDIC(selectionListener_.getServerType(i), 30, out);
              Conv.stringToBlankPadEBCDICByteArray(selectionListener_.getServerType(i), tempData, tempOffset, 30);
              tempOffset += 30;
            }
            for (int i=0; i<activeSubsystemCount; ++i)
            {
              //out.write(Util.blankPad(selectionListener_.getActiveSubsystem(i), 10));
              //Conv.writePadEBCDIC10(selectionListener_.getActiveSubsystem(i), out);
              Conv.stringToBlankPadEBCDICByteArray(selectionListener_.getActiveSubsystem(i), tempData, tempOffset, 10);
              tempOffset += 10;
            }
            for (int i=0; i<memoryPoolCount; ++i)
            {
              //out.writeInt(selectionListener_.getMemoryPool(i));
              Conv.intToByteArray(selectionListener_.getMemoryPool(i), tempData, tempOffset);
              tempOffset += 4;
            }
            for (int i=0; i<jobTypeEnhancedCount; ++i)
            {
              //out.writeInt(selectionListener_.getJobTypeEnhanced(i));
              Conv.intToByteArray(selectionListener_.getJobTypeEnhanced(i), tempData, tempOffset);
              tempOffset += 4;
            }
            for (int i=0; i<qualifiedJobNameCount; ++i)
            {
              //out.write(Util.blankPad(selectionListener_.getQualifiedJobName(i), 26));
              //Conv.writePadEBCDIC(selectionListener_.getQualifiedJobName(i), 26, out);
              Conv.stringToBlankPadEBCDICByteArray(selectionListener_.getQualifiedJobName(i), tempData, tempOffset, 26);
              tempOffset += 26;
            }
          }
        }
        return tempData;
      case 9: Conv.intToByteArray(getParameterInputLength(8), tempData, 0); return tempData;
      case 10: Conv.intToByteArray(fieldsToReturn_.length, tempData, 0); return tempData;
      case 11: // Key fields.
        for (int i=0; i<fieldsToReturn_.length; ++i)
        {
          //out.writeInt(fieldsToReturn_[i]);
          Conv.intToByteArray(fieldsToReturn_[i], tempData, i*4);
        }
        return tempData;
      case 12: return ZERO;
      case 13: Conv.stringToEBCDICByteArray37(getSelectionFormatName(), tempData, 0); return tempData;
      case 14: tempData[0] = resetStats_ ? (byte)0xF1 : (byte)0xF0; return tempData;
      case 16: Conv.intToByteArray(16, tempData, 0); return tempData;
    }
    return null;
  }

//  void readParameterOutputDataSubclass(final HostInputStream in, final int parmIndex, final int maxLength) throws IOException
  void setParameterOutputDataSubclass(final int parmIndex, final byte[] data, final int maxLength)
  {
    int numRead = 0;
    switch (parmIndex)
    {
      case 0:
        inputFormat_.format(data, maxLength, inputFormat_.getMinimumRecordLength());
        break;
      case 3:
        keyFields_ = null;
        if (maxLength >= 4)
        {
          final int numberOfFields = Conv.byteArrayToInt(data, numRead);
          numRead += 4;
          keyFields_ = new OpenListOfJobsKeyField[numberOfFields];
          for (int i=0; i<numberOfFields && numRead+20 <= maxLength; ++i)
          {
            final int lengthOfFieldInfo = Conv.byteArrayToInt(data, numRead);
            numRead += 4;
            final int keyField = Conv.byteArrayToInt(data, numRead);
            numRead += 4;
            final int typeOfData = data[numRead] & 0x00FF;
            final boolean isBinary = typeOfData == 0xC2;
            //in.skipBytes(3);
            numRead += 4;
            final int lengthOfData = Conv.byteArrayToInt(data, numRead);
            numRead += 4;
            final int displacementToData = Conv.byteArrayToInt(data, numRead);
            numRead += 4;
            keyFields_[i] = new OpenListOfJobsKeyField(keyField, isBinary, lengthOfData, displacementToData);
            int skip = lengthOfFieldInfo-20;
            if (maxLength-numRead >= skip)
            {
              //in.skipBytes(skip);
              numRead += skip;
            }
          }
          //in.skipBytes(maxLength-numRead);
        }
        break;
      case 5:
        if (maxLength < 12)
        {
          //in.skipBytes(maxLength);
          info_ = null;
        }
        else
        {
          info_ = Util.readOpenListInformationParameter(data, maxLength);
        }
        break;
      case 15:
        if (maxLength >= 16)
        {
          int bytesReturned = Conv.byteArrayToInt(data, 0);
          int bytesAvailable = Conv.byteArrayToInt(data, 4);
          elapsedTime_ = Conv.byteArrayToLong(data, 8);
//          numRead = 16;
        }
//        in.skipBytes(maxLength-numRead);
        break;
      default:
//        in.skipBytes(maxLength);
        break;
    }
  }
}

