///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ListUtilities.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;


/**
Provides utilities for retrieving lists of objects.
**/
class ListUtilities
{
  // Default setting for listWaitTimeout property.
  // This is the maximum amount of time to wait for a list to complete before giving up.
  private static final int DEFAULT_MAX_WAIT_TIME = 60;  // 60 seconds

  // The length of the "List information" structure parameter.
  static final int LIST_INFO_LENGTH = 80;  // 80 bytes


  //
  // Possible values for the "Information complete indicator" field:
  // Whether all requested information has been returned.
  //

  // Complete and accurate information.
  // All of the requested records have been returned in the receiver variable.
  static final char INFORMATION_COMPLETE = 'C';

  // Incomplete information.
  // An interruption causes the receiver variable to contain incomplete information.
  static final char INFORMATION_INTERRUPTED = 'I';

  // Partial and accurate information.
  // Partial information is returned when the receiver variable is full and not all of the records requested are returned.
  static final char INFORMATION_PARTIAL = 'P';



  //
  // Possible values for the "List status indicator" field:
  // The status of building the list.
  //

  // The building of the list is pending.
  static final char LIST_PENDING = '0';
    // Note: For a synchronous request, we'd probably never get this status back.

  // The list is in the process of being built.
  static final char LIST_BEING_BUILT = '1';
    // Note: Even though we wouldn't normally expect this status for synchronous requests,
    // we've occasionally gotten it for requests that list massively large numbers of objects.

  // The list has been completely built.
  static final char LIST_COMPLETE = '2';

  // An error occurred when building the list.
  // The next call to the Get List Entries (QGYGTLE) API will cause the error to be signaled
  // to the caller of the QGYGTLE API. 
  static final char LIST_ERROR = '3';

  // The list is primed and ready to be built.
  // The list will be built asynchronously by a server job,
  // but the server job has not necessarily started building the list yet.
  static final char LIST_PRIMED = '4';

  // Given the current selection criteria and information requested,
  // there is too much data to be returned.
  static final char LIST_TOO_MUCH_DATA = '5';


  /**
   Returns the value of the "list status indicator" field returned by QGY* API's.

   @param listInformation The "list information" structure returned by a QGY* API (that requested the building of a list).
   @return The converted value of the "list status information" field.  Possible values are '0', '1', '2', '3', '4', or '5'.
   @throws ErrorCompletingRequestException if the List Status Information is other than "complete", "being built", or "pending"; or if the Information Complete Indicator is "interrupted".
   **/
  private static char checkListStatus(byte[] listInformation)
    throws ErrorCompletingRequestException
  {
    char infoCompleteIndicator, listStatusIndicator;
    try {
      // Convert the two CHAR(1) fields from EBCDIC to Unicode.
      byte[] arry = { listInformation[16] };    // ICI is at offset 16
      infoCompleteIndicator = new CharConverter(37).byteArrayToString(arry,0,1).charAt(0);
      arry[0] = listInformation[30];            // LSI is at offset 30
      listStatusIndicator = new CharConverter(37).byteArrayToString(arry,0,1).charAt(0);
    }
    catch (java.io.UnsupportedEncodingException e) { // will never happen
      throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e.getMessage());
    }

    switch (listStatusIndicator)
    {
      case LIST_COMPLETE:
        break;   // This is the indicator that we normally expect.

      case LIST_BEING_BUILT:
      case LIST_PENDING:
        // These status values are unusual, but aren't necessarily error conditions
        // (even if we indicated we wanted the list built synchronously).
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "List status indicator:", listStatusIndicator);
        break;

      default:  // any other status

        StringBuffer msg = new StringBuffer("Unable to synchronously build object list on server.");
        msg.append("\n  List status indicator: " + listStatusIndicator);
        msg.append("\n  Info complete indicator: " + infoCompleteIndicator);

        try {
          msg.append("\n  Total records:    " +
                     BinaryConverter.byteArrayToInt(listInformation, 0));
          msg.append("\n  Records returned: " +
                     BinaryConverter.byteArrayToInt(listInformation, 4));
        }
        catch (Throwable t) {}  // will never happen
        finally {
          Trace.log(Trace.ERROR, msg.toString());
            throw new ErrorCompletingRequestException(ErrorCompletingRequestException.AS400_ERROR);
        }
    }

    if (infoCompleteIndicator == INFORMATION_INTERRUPTED)
    {
      Trace.log(Trace.ERROR, "Info complete indicator: " + infoCompleteIndicator);
      throw new ErrorCompletingRequestException(ErrorCompletingRequestException.AS400_ERROR);
    }

    return listStatusIndicator;
  }


  /**
   Closes the list on the system.  This releases any system resources previously in use by the list.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  static void closeList(AS400 system, byte[] listHandle) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (listHandle == null) return;

    ProgramParameter[] parameters = new ProgramParameter[]
    {
      new ProgramParameter(listHandle),
      new ErrorCodeParameter()
    };
    ProgramCall pc = new ProgramCall(system, "/QSYS.LIB/QGY.LIB/QGYCLST.PGM", parameters);  // not a threadsafe API
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }
  }


  // Calls QGYGTLE to get the current "list information" on the progress of list-building.
  private static byte[] refreshListInformation(byte[] listHandle, ProgramCall pgmCall)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (pgmCall.getParameterList().length == 0)
    {
      ProgramParameter[] parameters = new ProgramParameter[]
      {
        // Receiver variable, output, char(*).
        new ProgramParameter(8),   // minimum length is 8 bytes
        // Length of receiver variable, input, binary(4).
        new ProgramParameter(BinaryConverter.intToByteArray(8)),
        // Request handle, input, char(4).
        new ProgramParameter(listHandle),
        // List information, output, char(80).
        new ProgramParameter(LIST_INFO_LENGTH),
        // Number of records to return, input, binary(4).
        // '0' indicates: "Only the list information is returned and no actual list entries are returned."
        new ProgramParameter(new byte[] { 0x00, 0x00, 0x00, 0x00 } ),
        // Starting record, input, binary(4).
        // '0' indicates: "The list information should be returned to the caller immediately."
        // '-1' indicates: "The whole list should be built before the list information is returned to the caller."
        //new ProgramParameter(new byte[] { 0x00, 0x00, 0x00, 0x00} ),
        new ProgramParameter(new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF} ),
        // Error code, I/0, char(*).
        new ErrorCodeParameter()

      };          
      try { pgmCall.setProgram("/QSYS.LIB/QGY.LIB/QGYGTLE.PGM", parameters); }
      catch (java.beans.PropertyVetoException pve) {} // will never happen
    }

    if (!pgmCall.run()) {
      throw new AS400Exception(pgmCall.getMessageList());
    }        

    return pgmCall.getParameterList()[3].getOutputData();  // the "List Information" structure
  }


  /**
   Calls QGYGTLE (repeatedly if necessary) until the specified list is completely built.
   For use following calls to APIs that return an "Open list information format" structure.
   @param system The system where the list is being built.
   @param listHandle The list handle for the list.
   @param listInformation The "list information" structure returned by a QGY* API (that requested the building of a list).
   @return The final "list information" structure.
   **/
  static byte[] waitForListToComplete(AS400 system, byte[] listHandle, byte[] listInformation)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    ProgramCall pgmCall = null;  // for calling QGYGTLE
    final int waitSecondsPerIteration = 1;  // wait 1 second between retries
    int accumulatedWaitSeconds = 0;  // accumulated total wait time
    int maxWaitSeconds = getMaxWaitTime();

    char listStatus = checkListStatus(listInformation);

    while (listStatus != LIST_COMPLETE &&
           accumulatedWaitSeconds < maxWaitSeconds)
    {
      try {
        Thread.sleep(waitSecondsPerIteration*1000); // wait for 1 second
        accumulatedWaitSeconds += waitSecondsPerIteration;
      }
      catch (InterruptedException ie) {}  // ignore

      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Calling QGYGTLE.PGM to wait for list to be completely built.");

      // See if the building of the list (on the server) has completed yet.

      // Note: Even when we specify '-1' for the "number of records" parameter on the QGYOxxx request (to build the list synchronously), we can encounter a "list being built" status, if the request is building a massively large list of objects.

      if (pgmCall == null) pgmCall = new ProgramCall(system);
      listInformation = refreshListInformation(listHandle, pgmCall);
      listStatus = checkListStatus(listInformation);
    }

    if (listStatus != LIST_COMPLETE) {
      Trace.log(Trace.ERROR, "The building of the list did not complete within the specified time limit of " + maxWaitSeconds + " seconds.");
      throw new ErrorCompletingRequestException(ErrorCompletingRequestException.AS400_ERROR);
    }
    return listInformation;
  }


  /**
   Calls QGYGTLE, repeatedly if necessary, to retrieve the specified number of list entries.
   This assumes that the list has previously been built on the system.
   @param system The system where the list has been built.
   @param listHandle The list handle for the list.
   @param lengthOfReceiverVariable The value of the "Length of receiver variable" field.
   @param number The number of list entries to return.
   @param listOffset The offset into the list (0-based).
   @param outputListInfoContainer Container in which to receive the generated "List information" structure. Ignored if null.
   **/
  static byte[] retrieveListEntries(AS400 system, byte[] listHandle, int lengthOfReceiverVariable, int number, int listOffset, Object[] outputListInfoContainer)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    ProgramParameter[] parameters = new ProgramParameter[]
    {
      // Receiver variable, output, char(*).
      new ProgramParameter(lengthOfReceiverVariable),
      // Length of receiver variable, input, binary(4).
      new ProgramParameter(BinaryConverter.intToByteArray(lengthOfReceiverVariable)),
      // Request handle, input, char(4).
      new ProgramParameter(listHandle),
      // List information, output, char(80).
      new ProgramParameter(LIST_INFO_LENGTH),
      // Number of records to return, input, binary(4).
      new ProgramParameter(BinaryConverter.intToByteArray(number)),

      // Starting record, input, binary(4).  (1-based: The first record is record number '1')
      // '0' indicates that the list information should be returned to the caller immediately. The special value 0 is only allowed when the number of records to return parameter is zero.
      // '-1' indicates that the whole list should be built before the list information is returned to the caller.
      new ProgramParameter(BinaryConverter.intToByteArray(listOffset == -1 ? -1 : listOffset+1)),

      // Error code, I/0, char(*).
      new ErrorCodeParameter()
    };

    ProgramCall pc = new ProgramCall(system, "/QSYS.LIB/QGY.LIB/QGYGTLE.PGM", parameters); // not a threadsafe API

    byte[] listInformation;
    int recordsReturned;

    // Call QGYGTLE, to retrieve the list entries.
    // If we discover that the "receiver variable" was too small, try calling again, with progressively larger "receiver variable".
    do
    {
      if (pc.run())
      {
        listInformation = parameters[3].getOutputData();
        checkListStatus(listInformation);
        recordsReturned = BinaryConverter.byteArrayToInt(listInformation, 4);
      }
      else  // the call to QGYGTLE failed
      {
        listInformation = null;
        recordsReturned = 0;
        // See if the call failed because of a too-small receiver variable.
        AS400Message[] messages = pc.getMessageList();
        // GUI0002 means that the receiver variable was too small to hold the list.
        if (!messages[0].getID().equals("GUI0002")) {
          throw new AS400Exception(messages);
        }
      }

      if (recordsReturned < number) // we didn't get as many records as we requested
      {
        if (listInformation != null)
        {
          // See if we've reached the end of the list.
          int totalRecords = BinaryConverter.byteArrayToInt(listInformation, 0); // The total number of records available in the list.
          int firstRecordInReceiverVariable = BinaryConverter.byteArrayToInt(listInformation, 36);

          // Note: The "First record in receiver variable" field is 1-based; that is, the first record is record number '1' (rather than '0').
          if ((firstRecordInReceiverVariable + recordsReturned) > totalRecords) {
            // All the records in the list have been returned, so don't keep requesting more.
            break;
          }
        }

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieved messages, records returned: " + recordsReturned + ", number:", number);
        if (recordsReturned < 0)
        { // This will never happen, but satisfy the static code analyzer.
          throw new InternalErrorException(InternalErrorException.UNKNOWN, "Records returned: " + recordsReturned);
        }
        // Try again, with a larger "receiver variable".
        lengthOfReceiverVariable *= 1 + number / (recordsReturned + 1);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Updated length: ", lengthOfReceiverVariable);
        parameters[0] = new ProgramParameter(lengthOfReceiverVariable);
        parameters[1] = new ProgramParameter(BinaryConverter.intToByteArray(lengthOfReceiverVariable));
      }
    } while (recordsReturned < number);

    // If the caller specified a non-null 'outputListInfo' parameter, copy the "list information" structure into it, to pass it back to the caller.
    if (outputListInfoContainer != null && listInformation != null)
    {
      outputListInfoContainer[0] = listInformation;
    }

    return parameters[0].getOutputData();  // the contents of the "receiver variable" field
  }


  // Returns the maximum number of seconds to wait for a list to be built.
  private static int getMaxWaitTime()
  {
    int listWaitTimeout = DEFAULT_MAX_WAIT_TIME;
    String propVal = SystemProperties.getProperty(SystemProperties.LIST_WAIT_TIMEOUT);
    if (propVal != null)
    {
      try {
        listWaitTimeout = Integer.parseInt(propVal);
        if (listWaitTimeout == 0) listWaitTimeout = Integer.MAX_VALUE; // '0' means "no limit"
      }
      catch (Exception e) {
        if (Trace.traceOn_) Trace.log(Trace.WARNING, "Error retrieving listWaitTimeout property value:", e);
      }
    }
    return listWaitTimeout;
  }


}
