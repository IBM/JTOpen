///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename: OpenListHandler.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.openlist;

import com.ibm.jtopenlite.command.*;
import java.io.*;

/**
 * Use this to easily read entries created by an OpenListProgram.
 * <p/>
 * Example: Get a list of journal objects (*JRN) on the system
 * <pre>
 * // Start by getting a connection.
 * CommandConnection conn = CommandConnection.getConnection("system", "user", "password");
 *
 * // The formatter converts the raw output bytes from the program call into more useful things.
 * OpenListOfObjectsFormat format = new OpenListOfObjectsFormat();
 *
 * // The format listener gets notified when those useful things are ready.
 * OpenListOfObjectsFormatListener listener = new ObjectListOfObjectsListener()
 * {
 *   public void newObjectEntry(String objectNameUsed, String objectLibraryUsed,
 *                              String objectTypeUsed, String informationStatus, int numberOfFieldsReturned)
 *   {
 *     System.out.println("Journal "+objectNameUsed+" in library "+objectLibraryUser+".");
 *   }
 *
 *   public void newObjectFieldData(int lengthOfFieldInformation, int keyField, String typeOfData,
 *                                  int lengthOfData, int offsetToData, byte[] tempDataBuffer)
 *   {
 *     if (keyField == 203)
 *     {
 *       // Text description.
 *       System.out.println("Description: "+Conv.ebcdicByteArrayToString(tempDataBuffer, offsetToData, lengthOfData));
 *     }
 *     else if (keyField == 605)
 *     {
 *       // ASP device name.
 *       System.out.println("ASP device: "+Conv.ebcdicByteArrayToString(tempDataBuffer, offsetToData, lengthOfData));
 *     }
 *   }
 *
 *   // We want all of the entries in the list, so don't stop processing at any time.
 *   public boolean stopProcessing()
 *   {
 *     return false;
 *   }
 *
 *   public void totalRecordsInList(int total)
 *   {
 *     System.out.println("Total objects found: "+total);
 *   }
 *
 *   public void openComplete()
 *   {
 *   }
 * };
 *
 * // The selection listener tells the OpenListOfJobs about other criteria we want to use to filter
 * // out objects from the list.
 * OpenListOfObjectsSelectionListener selectionListener = new OpenListOfObjectsSelectionListener()
 * {
 *   public boolean isSelected()
 *   {
 *     return false;
 *   }
 *
 *   public int getNumberOfStatuses()
 *   {
 *     return 1;
 *   }
 *
 *   public String getStatus(int index)
 *   {
 *     return "A"; // Omit objects we do not have authority to.
 *   }
 * };
 *
 * // These keys retrieve the text description (203) and the ASP device name (605).
 * int[] keys = new int[] { 203, 605 };
 *
 * // Construct the open list program call.
 * OpenListOfObjects list = new OpenListOfObjects(format, 1024, 0, null, "*ALL", "*ALL", "*JRN", null, selectionListener, keys);
 *
 * // Use an OpenListHandler to help us get all of the entries.
 * OpenListHandler handler = new OpenListHandler(list, format, listener);
 *
 * // Kick it off. The handler will call GetListEntries, which in turn will call our formatter with
 * // the output data for each entry in the list, which in turn will call our format listener to
 * // notify us via newObjectEntry() and newObjectFieldData().
 * handler.process(conn, 1000);
 *
 * // All done.
 * conn.close();
 * </pre>
**/
public class OpenListHandler
{
  private final OpenListProgram program_;
  private ListFormatListener listener_;
  private final GetListEntries getEntries_;
  private final CloseList close_ = new CloseList();

  public OpenListHandler(OpenListProgram program, ListEntryFormat format, ListFormatListener listener)
  {
    program_ = program;
    listener_ = listener;
    getEntries_ = new GetListEntries();
    getEntries_.setFormatter(format);
  }

  public ListFormatListener getFormatListener()
  {
    return listener_;
  }

  public void setFormatListener(ListFormatListener listener)
  {
    listener_ = listener;
  }

  /**
   * Calls the OpenListProgram, then loops calling GetListEntries, then finally calls CloseList.
   * @param conn The connection to use.
   * @param numRecordsToReturn The number of records the GetListEntries call should return each time.
  **/
  public void process(CommandConnection conn, int numRecordsToReturn) throws IOException
  {
    CommandResult result = conn.call(program_);
    if (!result.succeeded())
    {
      throw new IOException("Open list failed: "+result.toString());
    }

    ListInformation listInfo = program_.getListInformation();
    byte[] requestHandle = listInfo.getRequestHandle();
    close_.setRequestHandle(requestHandle);

    if (listener_ != null) listener_.openComplete();

    try
    {
      int recordLength = listInfo.getRecordLength();
      // Now, the list is building on the server.
      // Call GetListEntries once to wait for the list to finish building, for example.
      int receiverSize = 8;
      int startingRecord = -1;
      getEntries_.setLengthOfReceiverVariable(receiverSize);
      getEntries_.setRequestHandle(requestHandle);
      getEntries_.setRecordLength(recordLength);
      getEntries_.setNumberOfRecordsToReturn(0);
      getEntries_.setStartingRecord(startingRecord);
      getEntries_.setFormatListener(null);
      result = conn.call(getEntries_);
      if (!result.succeeded())
      {
        throw new IOException("Get list entries failed: "+result.toString());
      }

      listInfo = getEntries_.getListInformation();
      int totalRecords = listInfo.getTotalRecords();
      if (listener_ != null) listener_.totalRecordsInList(totalRecords);

      // Now retrieve each object record in chunks of 800 at a time.
      receiverSize = recordLength * numRecordsToReturn;
      if (receiverSize <= 0) receiverSize = 100000;
      startingRecord = 1;
      getEntries_.setLengthOfReceiverVariable(receiverSize);
      getEntries_.setNumberOfRecordsToReturn(numRecordsToReturn);
      getEntries_.setStartingRecord(startingRecord);
      getEntries_.setFormatListener(listener_); // Ready to process.
      if (listener_ != null) { 
      while (!listener_.stopProcessing() && startingRecord <= totalRecords)
      {
        result = conn.call(getEntries_);
        if (!result.succeeded())
        {
          throw new IOException("Get objects failed: "+result.toString());
        }
        // Assuming it succeeded.
        listInfo = getEntries_.getListInformation();
        startingRecord += listInfo.getRecordsReturned();
        getEntries_.setStartingRecord(startingRecord);
      }
      }
    }
    finally
    {
      conn.call(close_);
    }
  }
}
