///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: HistoryLog.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2006-2006 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

/**
* Represents a history log on the system.  This class is used to get a list of messages in a history log.
* The {@link #close close()} method should be called when you are finished retrieving messages from the list.
* <p>Note:  The System API this class uses is <b>NOT</b> available when connecting to V5R4 or earlier systems.
* <p>QueuedMessage objects have many attributes.  Only some of these attribute values are set, depending 
* on how a QueuedMessage object is created.  The following is a list of attributes whose values are set on 
* QueuedMessage objects returned in a list of history log messages:
* <ul>
* <li>date sent
* <li>message file name
* <li>message file library
* <li>message ID
* <li>message severity
* <li>message text
* <li>message type
* <li>sending job name
* <li>sending job number
* <li>sending job user name
* <li>sending current user
* </ul>
* @see  com.ibm.as400.access.QueuedMessage
**/
public class HistoryLog
{
    static final long serialVersionUID = 4L;
    
    /**
     * Constant indicating the current date.  The value of this constant is {@value}.
    **/
    public static final String CURRENT_DATE = "*CURRENT";
    
    /**
     * Constant indicating to list data logged from the beginning of the log.  The value of this constant is {@value}.
    **/
    public static final String BEGIN = "*BEGIN";
    
    /**
     * Constant indicating the last day on which data was logged is the last day for which logged
     * data is listed.  The value of this constant is {@value}.
    **/
    public static final String END = "*END";
    
    /**
     * Constant indicating that any logged data that is available for the specified dates should be listed.
     * The value of this constant is {@value}.
     */
    public static final String AVAIL = "*AVAIL";
    
    /**
     * Constant indicating that the message IDs in the list are to be omitted.
     */
    public static final int OMIT = 1;
    
    /**
     * Constant indicating that the message IDs in the list are to be selected.
     */
    public static final int SELECT = 0;
    
    /**
     * Message type for completion messages.
     */
    public static final String TYPE_COMPLETION = "*COMP";
    
    /**
     * Message type for copy messages.
     */
    public static final String TYPE_COPY = "*COPY";
    
    /**
     * Message type for diagnostic messages.
     */
    public static final String TYPE_DIAGNOSTIC = "*DIAG";
    
    /**
     * Message type for escape messages.
     */
    public static final String TYPE_ESCAPE = "*ESCAPE";
    
    /**
     * Message type for informational messages.
     */
    public static final String TYPE_INFORMATIONAL = "*INFO";
    
    /**
     * Message type for inquiry messages.  You can send inquiry messages only
     * to the external message queue.
     */
    public static final String TYPE_INQUIRY = "*INQ";
    
    /**
     * Message type for notify messages.
     */
    public static final String TYPE_NOTIFY = "*NOTIFY";
    
    /**
     * Message type for reply messages.
     */
    public static final String TYPE_REPLY = "*RPY";
    
    /**
     * Message type for request messages.
     */
    public static final String TYPE_REQUEST = "*RQS";
    
    // starting date for messages and jobs to be listed, the default is *CURRENT
    private String startingDate_ = CURRENT_DATE;
    // starting time for messages and jobs to be listed, the default is *AVAIL
    private String startingTime_ = AVAIL;
    // ending date for messages and jobs to be listed, the default is *END
    private String endingDate_ = END;
    // ending time for messages and jobs to be listed, the default is *AVAIL
    private String endingTime_ = AVAIL;
    // jobs for which messages in the log are listed
    private Job[] jobsToList_ = new Job[0];
    // message ids to be retrieved or omitted
    private String[] messageIDs_ = new String[0];
    // indicator if messages ids in the list should be omitted or selected
    private int idListIndicator_ = SELECT;	// the default is selected (0)
    // minimum severity of messages to be listed
    private int messageSeverity_ = 0;
    // indicator if message types in the list should be omitted or selected
    private int typesListIndicator_ = SELECT; // the default is selected (0)
    // message types to be retrieved or omitted
    private String[] messageTypes_ = new String[0];

    // The system where the history log is located.
    private AS400 system_;
    // Length of the history log message list.
    private int length_;
    // Handle that references the user space used by the open list APIs.
    private byte[] handle_;
    // If the list info has changed, close the old handle before loading the new one.
    private boolean closeHandle_ = false;

    /**
     * Constructs a HistoryLog object for the specified system.
     * @param system The system object representing the system on which the history log exists.
     */
    public HistoryLog(AS400 system)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing HistoryLog object, system: " + system);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        system_ = system;
    }

    /**
     Closes the message list on the system.  This releases any system resources previously in use by this message list.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public synchronized void close() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Closing job log message list, handle: ", handle_);
        if (handle_ == null) return;

        try {
          ListUtilities.closeList(system_, handle_);
        }
        finally {
          handle_ = null;
          closeHandle_ = false;
        }
    }

    /**
     Returns the number of messages in the history log.  This method implicitly calls {@link #load load()}.
     @return  The number of messages, or 0 if no list was retrieved.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system or when running to V5R4 or an earlier release.
     @see  #load
     **/
    public int getLength() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting history log list length.");
        if (handle_ == null || closeHandle_) load();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Length:", length_);
        return length_;
    }

    /**
     Returns the list of messages in the history log.  The messages are listed from oldest to newest.
     @return  An Enumeration of <a href="QueuedMessage.html">QueuedMessage</a> objects.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system or when running to V5R4 or an earlier release.
     **/
    public Enumeration getMessages() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving history log message list.");
        // Need to get the length.
        if (handle_ == null || closeHandle_) load();
        return new QueuedMessageEnumeration(this, length_);
    }

    /**
     Returns a subset of the list of messages in the history log.  This method allows the user to retrieve the message list from the system in pieces.  If a call to {@link #load load()} is made (either implicitly or explicitly), then the messages at a given list offset will change, so a subsequent call to getMessages() with the same <i>listOffset</i> and <i>number</i> will most likely not return the same QueuedMessages as the previous call.
     @param  listOffset  The starting offset in the list of messages (0-based).  This value must be greater than or equal to 0 and less than the list length; or specify -1 to retrieve all of the messages.
        <i>Note: Prior to JTOpen 7.2, this parameter was incorrectly described.</i>
     @param  number  The number of messages to retrieve out of the list, starting at the specified <i>listOffset</i>.  This value must be greater than or equal to 0 and less than or equal to the list length.  If the <i>listOffset</i> is -1, this parameter is ignored.
     @return  The array of retrieved {@link com.ibm.as400.access.QueuedMessage QueuedMessage} objects.  The length of this array may not necessarily be equal to <i>number</i>, depending upon the size of the list on the system, and the specified <i>listOffset</i>.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system, or when running to V5R4 or an earlier release.
     @see  com.ibm.as400.access.QueuedMessage
     **/
    public QueuedMessage[] getMessages(int listOffset, int number) throws  AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving history log message list, list offset: " + listOffset + ", number:", number);
        if (listOffset < -1)
        {
            throw new ExtendedIllegalArgumentException("listOffset (" + listOffset + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        if (number < 0 && listOffset != -1)
        {
            throw new ExtendedIllegalArgumentException("number (" + number + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        if (handle_ == null || closeHandle_) load();  // this sets the length_ variable

        if (length_ == 0 || (number == 0 && listOffset != -1)) {
          return new QueuedMessage[0];
        }

        if (listOffset == -1)
        {
            number = length_;  // request entire list
            listOffset = 0;    // ... starting at beginning of list
        }
        else if (listOffset >= length_)
        {
            if (Trace.traceOn_)
              Trace.log(Trace.WARNING, "Value of parameter 'listOffset' is beyond end of list:", listOffset + " (list length: " + length_ + ")");

            return new QueuedMessage[0];
        }
        else if (listOffset + number > length_)
        {
            number = length_ - listOffset;
        }

        int lengthOfReceiverVariable = 1024 * number;

        // Retrieve the entries in the list that was built by the most recent load().
        byte[] data = ListUtilities.retrieveListEntries(system_, handle_, lengthOfReceiverVariable, number, listOffset, null);

        Converter conv = new Converter(system_.getCcsid(), system_);

        QueuedMessage[] messages = new QueuedMessage[number];
        int offset = 0;
        for (int i = 0; i < messages.length; ++i) // each message
        {
        	// Data in the HSTL0100 Format
            int entryLength = BinaryConverter.byteArrayToInt(data, offset);
            int messageSeverity = BinaryConverter.byteArrayToInt(data, offset + 4);
            String messageIdentifier = conv.byteArrayToString(data, offset + 8, 7).trim();
            int messageType = (data[offset + 15] & 0x0F) * 10 + (data[offset + 16] & 0x0F);
            if (messageType == 0) messageType = -1;
            String messageFileName = conv.byteArrayToString(data, offset + 17, 10).trim();
            String messageFileLibrarySpecified = conv.byteArrayToString(data, offset + 27, 10).trim();
            String dateSent = conv.byteArrayToString(data, offset + 37, 7); // CYYMMDD
            String timeSent = conv.byteArrayToString(data, offset + 44, 6); // HHMMSS
            String fromJob = conv.byteArrayToString(data, offset + 56, 10).trim();
            String fromJobUser = conv.byteArrayToString(data, offset + 66, 10).trim();
            String fromJobNumber = conv.byteArrayToString(data, offset + 76, 6);
            String currentUser = conv.byteArrayToString(data, offset + 82, 10).trim();
            
            int locOfMessageData = BinaryConverter.byteArrayToInt(data, offset+96);
            int lengthOfMessageData = BinaryConverter.byteArrayToInt(data, offset + 100);
            int locOfReplacementData = BinaryConverter.byteArrayToInt(data, offset+104);
            int lengthOfReplacementData = BinaryConverter.byteArrayToInt(data, offset+108);
            String messageText = conv.byteArrayToString(data, offset + locOfMessageData, lengthOfMessageData);
            byte[] replacementDataBytes = new byte[lengthOfReplacementData];
            System.arraycopy(data, offset+locOfReplacementData, replacementDataBytes, 0, lengthOfReplacementData);
            messages[i] = new QueuedMessage(system_, messageSeverity, messageIdentifier, messageType, messageFileName, messageFileLibrarySpecified, dateSent, timeSent, fromJob, fromJobUser, fromJobNumber, currentUser, messageText, replacementDataBytes);

            offset += entryLength;
        }

        return messages;
    }

    /**
     Returns the system object representing the system on which the history log exists.
     @return  The system object representing the system on which the history log exists.  If the system has not been set, null is returned.
     **/
    public AS400 getSystem()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system: " + system_);
        return system_;
    }

    /**
     Loads the list of messages on the system.  This method informs the system to build a list of messages.  This method blocks until the system returns the total number of messages it has compiled.  A subsequent call to {@link #getMessages getMessages()} will retrieve the actual message information and attributes for each message in the list from the system.
     <p>This method updates the list length.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system or when running to V5R4 or an earlier release.
     @see  #getLength
     **/
    public synchronized void load() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Loading history log message list.");
        if (system_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot connect to server before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        // Close the previous list.
        if (closeHandle_) close();

        // Generate text objects based on system CCSID.
        Converter conv = new Converter(system_.getCcsid(), system_);
        
        // Determine size and number of message ids to filter by
        int numMessageIDs = messageIDs_.length;
        int sizeMessageIDs = numMessageIDs * 7;
        // Determine the size and number of jobs to filter by
        int numJobs = jobsToList_.length;
        int sizeJobs = numJobs * 26;
        // Determine the size and number of message types to filter by
        int numMessageTypes = messageTypes_.length;
        int sizeMessageTypes = numMessageTypes * 10;
        
        // Determine the offsets for the start of the variable information
        int offsetToVariableInformation = 92;
        int offsetToMessageIDs = offsetToVariableInformation;
        int offsetToJobs = offsetToMessageIDs + sizeMessageIDs;
        int offsetToMessageTypes = offsetToJobs + sizeJobs;
        
        // Create the message selection information.
        // message selection information will be 92 + size of message ids + size of jobs + size of message types
        byte[] messageSelectionInformation = new byte[92 + sizeMessageIDs + sizeJobs + sizeMessageTypes];
        // The length of the fixed portion of information.  This must be set to 92.  Binary(4)
        BinaryConverter.intToByteArray(92, messageSelectionInformation, 0);
        // Blank pad through date and time fields.
        for (int i = 4; i < 56; ++i) messageSelectionInformation[i] = 0x40;
        // Start date - Char(10)
        conv.stringToByteArray(startingDate_, messageSelectionInformation, 4);
        // Start time - Char(10)
        conv.stringToByteArray(startingTime_, messageSelectionInformation, 14);
        // Start after time microseconds - Char(6) 
        for (int i=24; i<30; ++i) messageSelectionInformation[i] = 0x00;
        // End by date - Char(10)
        conv.stringToByteArray(endingDate_, messageSelectionInformation, 30);
        // End by time - Char(10)
        conv.stringToByteArray(endingTime_, messageSelectionInformation, 40);
        // End by time microseconds - Char(6)
        for (int i=50; i<56; ++i) messageSelectionInformation[i] = 0x00;
        // Message ids list contents indicator - Binary(4)
        BinaryConverter.intToByteArray(idListIndicator_, messageSelectionInformation, 56);
      	// Offset to list of message ids - Binary(4)
       	BinaryConverter.intToByteArray((numMessageIDs == 0) ? 0 : offsetToMessageIDs, messageSelectionInformation, 60);
       	// Number of message ids in list - Binary(4)
       	BinaryConverter.intToByteArray((numMessageIDs == 0) ? 0 : numMessageIDs, messageSelectionInformation, 64);
       	// Offset to jobs to list - Binary(4)
       	BinaryConverter.intToByteArray((numJobs == 0) ? 0 : offsetToJobs, messageSelectionInformation, 68);
       	// Number of jobs to list - Binary(4)
       	BinaryConverter.intToByteArray((numJobs == 0) ? 0 : numJobs, messageSelectionInformation, 72);
        // Message severity - Binary(4)
        BinaryConverter.intToByteArray(messageSeverity_, messageSelectionInformation, 76);
        // Message type list contents indicator - Binary(4)
        BinaryConverter.intToByteArray(typesListIndicator_, messageSelectionInformation, 80);
       	// Offset to list of message types - Binary(4)
       	BinaryConverter.intToByteArray((numMessageTypes == 0) ? 0 : offsetToMessageTypes, messageSelectionInformation, 84);
        // Number of message types in list - Binary(4)
       	BinaryConverter.intToByteArray((numMessageTypes == 0) ? 0 : numMessageTypes, messageSelectionInformation, 88);
        // Reserved - Char(*) - An ignored Field
        // Blank pad through variable length area.
        int padFor = sizeMessageIDs + sizeJobs + sizeMessageTypes;
        for (int n = offsetToMessageIDs; n < (offsetToMessageIDs + padFor); ++n) messageSelectionInformation[n] = 0x40;
        // Message ids list - Char(*) - each message id is Char(7)
        for(int i=0; i<numMessageIDs; i++)
        	conv.stringToByteArray(messageIDs_[i], messageSelectionInformation, offsetToMessageIDs + (7*i));
        // Jobs to list - Char(*)
        // Each job consists of job name char(10), job user name char(10) and job number char(6)
       	for(int j=0; j<numJobs; j++)
       	{
       		Job job = jobsToList_[j];
       		conv.stringToByteArray(job.getName().toUpperCase().trim(), messageSelectionInformation, offsetToJobs + (26*j));	//job name
       		conv.stringToByteArray(job.getUser().toUpperCase().trim(), messageSelectionInformation, offsetToJobs + (26*j) + 10); // job user name
       		conv.stringToByteArray(job.getNumber(), messageSelectionInformation, offsetToJobs + (26*j) + 20);	// job number
       	}
        // Message types list - Char(*) - each message type is char(10)
       	for(int t=0; t<numMessageTypes; t++)
       	   	conv.stringToByteArray(messageTypes_[t], messageSelectionInformation, offsetToMessageTypes + (10*t));
        
        // Setup program parameters.
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Receiver variable, output, char(*).
            new ProgramParameter(0),
            // Length of receiver variable, input, binary(4).
            new ProgramParameter(new byte[] { 0x00, 0x00, 0x00, 0x00 } ),
            // Format name, input, char(8)
            new ProgramParameter(conv.stringToByteArray("HSTL0100")),
            // List information, output, char(80).
            new ProgramParameter(ListUtilities.LIST_INFO_LENGTH),
            // Number of records to return, input, binary(4).
            // Special value '-1' indicates that "all records are built synchronously in the list".
            new ProgramParameter(new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF } ),
            // Message selection information, input, char(*).
            new ProgramParameter(messageSelectionInformation),
            // CCSID, input, binary(4) - if zero is specified, the data is returned in the ccsid of the job
            new ProgramParameter(new byte[] { 0x00, 0x00, 0x00, 0x00}),
            // Time Zone, input, char(10) - if blank the jobs time zone is used
            new ProgramParameter(conv.stringToByteArray("*JOB      ")),
            // Error code, I/O, char(*).
            new ErrorCodeParameter(),
        };

        // Call the program.
        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QMHOLHST.PGM", parameters); // not a threadsafe API

        if (!pc.run())
        {
            throw new AS400Exception(pc.getMessageList());
        }

        // List information returned.
        byte[] listInformation = parameters[3].getOutputData();
        handle_ = new byte[4];
        System.arraycopy(listInformation, 8, handle_, 0, 4);

        // Wait for the list-building to complete.
        listInformation = ListUtilities.waitForListToComplete(system_, handle_, listInformation);

        length_ = BinaryConverter.byteArrayToInt(listInformation, 0);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Loaded history log message list, length: " + length_ + ", handle: ", handle_);
    }

    /**
     * Specifies the starting date and time for messages and jobs to be listed.
     * @param date The starting date.
     * Valid values include:
     * <ul>
     * <li>{@link #CURRENT_DATE} (default)</li>
     * <li>{@link #BEGIN}</li>
     * <li>A date in the format CYYMMDD:
     * <ul>
     * <li>C - Century, where 0 indicates years 19xx and 1 indicates years 20xx</li>
     * <li>YY - Year</li>
     * <li>MM - Month</li>
     * <li>DD - Day</li>
     * </ul>
     * </li>
     * </ul>
     * @param time The starting time.
     * Valid values include:
     * <ul>
     * <li>{@link #AVAIL} (default)
     * <li>A time specified in the 24 hour format HHMMSS:
     * <ul>
     * <li>HH - Hour</li>
     * <li>MM - Minute</li>
     * <li>SS - Second</li>
     * </ul>
     * </ul>
     * @exception ExtendedIllegalArgumentException if the date is not in a valid format.
     */
    public void setStartingDate(String date, String time) throws ExtendedIllegalArgumentException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting starting date:  " + date + " " + time);
        synchronized(this)
        {
        	// Set the starting date
        	if(date == null || date.length() == 0)
        		startingDate_ = CURRENT_DATE;
        	else if(date.trim().equalsIgnoreCase(CURRENT_DATE))
        		startingDate_ = CURRENT_DATE;
        	else if(date.trim().equalsIgnoreCase(BEGIN))
        		startingDate_ = BEGIN;
        	else	// assume the String is in the CYYMMDD format
        	{
        		if(date.trim().length() > 7)
        			throw new ExtendedIllegalArgumentException("date (" + date + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        		startingDate_ = date;
        	}
    	
        	// 	Set the starting time
        	if(time == null || time.length() == 0)
        		startingTime_ = AVAIL;
        	else if(time.trim().equalsIgnoreCase(AVAIL))
        		startingTime_ = AVAIL;
        	else	// assume the String is in HHMMSS format
        	{
        		if(time.trim().length() > 6)
        			throw new ExtendedIllegalArgumentException("time (" + time + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        		startingTime_ = time;
        	}
        	if(handle_ != null) closeHandle_ = true;
        }
    }
    
    /**
     * Specifies the starting date and time for messages and jobs to be listed.  
     * @param date The starting date.
     * @throws ExtendedIllegalArgumentException if the date is not valid.
     */
    public void setStartingDate(Date date)throws ExtendedIllegalArgumentException{
    	if (date == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'date' is null.");
            throw new NullPointerException("date");
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting starting date:  " + date);
    	String start = dateToString(date);
    	synchronized(this)
    	{
    		startingDate_ = start.substring(0, 7);
    		startingTime_ = start.substring(7);
    		if(handle_ != null) closeHandle_ = true;
    	}
    }
    
    /**
     * Returns the starting date for messages and jobs to be listed.
     * @return The starting date, or null if one of the following values
     * was used to specify the starting date:
     * <ul>
     * <li>{@link #CURRENT_DATE}</li>
     * <li>{@link #BEGIN}</li>
     * </ul>
     */
    public Date getStartingDate()
    {
    	if(startingDate_.equals(CURRENT_DATE) || startingDate_.equals(BEGIN))
    		return null;
    	else 
    		return stringToDate(startingDate_, startingTime_);
    }
    
    /**
     * Specifies the ending date and time for messages and jobs to be listed.
     * @param date The ending date. 
     * Valid values include:
     * <ul>
     * <li>{@link #CURRENT_DATE}</li>
     * <li>{@link #END} (default)</li>
     * <li>A date in the format CYYMMDD:
     * <ul>
     * <li>C - Century, where 0 indicates years 19xx and 1 indicates years 20xx</li>
     * <li>YY - Year</li>
     * <li>MM - Month</li>
     * <li>DD - Day</li>
     * </ul>
     * </li>
     * </ul>
     * @param time The ending time.
     * Valid values include:
     * <ul>
     * <li>{@link #AVAIL} (default)
     * <li>A time specified in the 24 hour format HHMMSS:
     * <ul>
     * <li>HH - Hour</li>
     * <li>MM - Minute</li>
     * <li>SS - Second</li>
     * </ul>
     * </ul>
     * @throws ExtendedIllegalArgumentException if the date is not in a valid format.
     */
    public void setEndingDate(String date, String time) throws ExtendedIllegalArgumentException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting list ending date " + date + " " + time);
        synchronized(this)
        {
        	// Set the ending date
        	if(date == null || date.length() == 0)
        		endingDate_ = END;
        	else if(date.trim().equalsIgnoreCase(CURRENT_DATE))
        		endingDate_ = CURRENT_DATE;
        	else if(date.trim().equalsIgnoreCase(END))
        		endingDate_ = END;
        	else{	// assume the string is in CYYMMDD format
        		if(date.trim().length() > 7)
        			throw new ExtendedIllegalArgumentException("date (" + date + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        		endingDate_ = date;
        	}
    	
        	// Set the ending time
        	if(time == null || time.length() == 0)
        		endingTime_ = AVAIL;
        	else if(time.trim().equalsIgnoreCase(AVAIL))
        		endingTime_ = AVAIL;
        	else	// assume the String is in HHMMSS format
        	{
        		if(time.trim().length() > 6)
        			throw new ExtendedIllegalArgumentException("time (" + time + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        		endingTime_ = time;
        	}
        	if(handle_ != null) closeHandle_ = true;
        }
    }
    
    /**
     * Specifies the end date and time for messages and jobs to be listed.
     * @param date The ending date.
     * @throws ExtendedIllegalArgumentException if the date is not valid.
     */
    public void setEndingDate(Date date) throws ExtendedIllegalArgumentException{
    	if (date == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'date' is null.");
            throw new NullPointerException("date");
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting list ending date: " + date);
    	String end = dateToString(date);
    	synchronized(this)
    	{
    		endingDate_ = end.substring(0, 7);
    		endingTime_ = end.substring(7);
    		if(handle_ != null) closeHandle_ = true;
    	}
    }
    
    /**
     * Returns the ending date for messages and jobs to be listed.
     * @return The ending date, or null if one of the following special
     * values were used for the date:
     * <ul>
     * <li>{@link #CURRENT_DATE}</li>
     * <li>{@link #END}</li>
     * </ul>
     */
    public Date getEndingDate(){
    	if(endingDate_.equals(CURRENT_DATE) || endingDate_.equals(END))
    		return null;
    	else 
    		return stringToDate(endingDate_, endingTime_);
    }
    
    private Date stringToDate(String date, String time){

        Calendar calendar = Calendar.getInstance();

        int centuryAndYear = Integer.parseInt(date.substring(0,3));
    	int month   = Integer.parseInt(date.substring(3,5));
    	int day     = Integer.parseInt(date.substring(5,7));
    	boolean timeAvail = time.equals(AVAIL);
    	int hour    = timeAvail ? 0 : Integer.parseInt(time.substring(0,2));
    	int minute  = timeAvail ? 0 : Integer.parseInt(time.substring(2,4));
    	int second  = timeAvail ? 0 : Integer.parseInt(time.substring(4));

    	calendar.set(Calendar.YEAR, centuryAndYear + 1900);
    	calendar.set(Calendar.MONTH, month - 1);
    	calendar.set(Calendar.DAY_OF_MONTH, day);
    	calendar.set(Calendar.HOUR_OF_DAY, hour);
    	calendar.set(Calendar.MINUTE, minute);
    	calendar.set(Calendar.SECOND, second);
    	calendar.set(Calendar.MILLISECOND, 0); 
    	return calendar.getTime();
    }
    
    /**
     * Converts the specified Date object to a String in the format CYYMMDDHHMMSS.
     * @param date
     * @return the date represented in the format CYYMMDD
     */
    private String dateToString(Date date){
    	StringBuffer buffer = new StringBuffer(13);
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(date);
    	int year = calendar.get(Calendar.YEAR);
    	buffer.append((year<2000) ? '0' : '1');
    	buffer.append(twoDigits(year % 100));
    	buffer.append(twoDigits(calendar.get(Calendar.MONTH) + 1));
    	buffer.append(twoDigits(calendar.get(Calendar.DAY_OF_MONTH)));
    	buffer.append(twoDigits(calendar.get(Calendar.HOUR_OF_DAY)));
    	buffer.append(twoDigits(calendar.get(Calendar.MINUTE)));
    	buffer.append(twoDigits(calendar.get(Calendar.SECOND)));
    	return buffer.toString();
    }
    
    /**
     * Verifies if the date specified is in CYYMMDD
     * @param date
     */
    /*private void verifyDate(String date){
    	// Parse the date to verify it is in the CYYMMDD format
    	int century = Integer.parseInt(date.substring(0,1));
    	if(century != 0 && century != 1)
    		throw new ExtendedIllegalArgumentException("date(" + date + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    	int year = Integer.parseInt(date.substring(1,3));
    	if(!(year >= 0) && !(year<=99))
    		throw new ExtendedIllegalArgumentException("date(" + date + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    	int month = Integer.parseInt(date.substring(3,5));
    	if(!(month > 0) && !(month<=12))
    		throw new ExtendedIllegalArgumentException("date(" + date + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    	int day = Integer.parseInt(date.substring(5));
    	if(!(day > 0))
    		throw new ExtendedIllegalArgumentException("date(" + date + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    	switch(month){
    	case 1:
    	case 3:
    	case 5:
    	case 7:
    	case 8:
    	case 10:
    	case 12:
    		if(!(day <= 31))
    			throw new ExtendedIllegalArgumentException("date(" + date + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    		break;
    	case 4:
    	case 6:
    	case 9:
    	case 11:
    		if(!(day <= 30))
    			throw new ExtendedIllegalArgumentException("date(" + date + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    	case 2:
    		if((year%4)==0)
    		{
    			if(!(day <= 29))
    				throw new ExtendedIllegalArgumentException("date(" + date + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    		}
    		else
    			if(!(day <= 28))
    				throw new ExtendedIllegalArgumentException("date(" + date + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    	}
    }
    */
    /**
     * Specifies the specific messaged IDs to be retrieved or omitted.  Each message id
     * should be 7 characters.  Up to 100 message IDs are supported.  If not specified, all 
     * of the messages will be listed for the jobs and times specified.  To select 
     * specific generic types of messages, specify the 3 character code that identifies
     * the message file followed by all zeros.  If the message ID is less than 7 characters,
     *  0's will be appended to the ID.
     * @param ids The message IDs.
     * @throws ExtendedIllegalArgumentException if more than 100 message IDs are specified, or if
     * a message ID is more than 7 characters.
     */
    public void setMessageIDs(String[] ids) throws ExtendedIllegalArgumentException{
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting list ids");

    	if(ids == null)
    	{
    		Trace.log(Trace.ERROR, "Parameter 'ids' is null.");
            throw new NullPointerException("ids");
    	}
    	if(ids.length > 100)	// up to 100 message ids are supported
    	{
    		Trace.log(Trace.ERROR, "Length of ids not valid.");
    		throw new ExtendedIllegalArgumentException("ids (" + ids.length + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    	}
    	int length = ids.length;
    	// verify the length of each id
    	for(int i=0; i<length; i++)
    	{
    		int idLength = ids[i].length();
    		if(idLength > 7)
    		{
    			Trace.log(Trace.ERROR, "Length of message id is not valid.  Message ID = " + ids[i]);
    			throw new ExtendedIllegalArgumentException("ids[" + i + "] (" + ids[i] + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    		}
    		else if(idLength < 7)
    		{
    			StringBuffer buffer = new StringBuffer(ids[i]);
    			// append zeros to the end, zeros indicating to match everything before the zeros
    			for(int j=idLength; j < 7; j++)
    				buffer.append("0");
    			ids[i]=buffer.toString();
    		}
    	}
    	synchronized(this)
    	{
    		messageIDs_ = ids;
    		if(handle_ != null) closeHandle_ = true;
    	}
    }
    
    /**
     * Returns the list of message ids used to filter which messages in the log are listed.
     * @return The list of message ids or an empty array if no message ids were specified.
     */
    public String[] getMessageIDs(){
    	return messageIDs_;
    }
    
    /**
     * Specifies if the message IDs in the list are to be omitted or selected.
     * @param indicator The indicator.
     * Valid values are:
     * <ul>
     * <li>{@link #OMIT}</li>
     * <li>{@link #SELECT} (default)</li>
     * </ul>
     * @throws ExtendedIllegalArgumentException
     */
    public void setMessageIDsListIndicator(int indicator) throws ExtendedIllegalArgumentException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting message ids list indicator: " + indicator);

    	if(indicator != OMIT && indicator != SELECT)
    		throw new ExtendedIllegalArgumentException("indicator(" + indicator + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    	
    	synchronized(this)
    	{
    		idListIndicator_ = indicator;
    		if(handle_ != null) closeHandle_ = true;
    	}
    }
    
    /**
     * Returns the list indicator indicating if the message ids in the list are omitted or selected.
     * @return The list indicator.  Possible values are:
     * <ul>
     * <li>{@link #OMIT}</li>
     * <li>{@link #SELECT}</li>
     * </ul>
     */
    public int getMessageIDsListIndicator()
    {
    	return idListIndicator_;
    }
    
    /**
     * Specifies if the message types in the list are to be omitted or selected.
     * @param indicator The indicator.
     * Valid values are:
     * <ul>
     * <li>{@link #OMIT}</li>
     * <li>{@link #SELECT} (default)</li>
     * </ul>
     * @throws ExtendedIllegalArgumentException
     */
    public void setMessageTypeListIndicator(int indicator) throws ExtendedIllegalArgumentException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting message type list indicator:  " + indicator);

    	if(indicator != OMIT && indicator != SELECT)
    		throw new ExtendedIllegalArgumentException("indicator(" + indicator + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    	synchronized(this)
    	{
    		typesListIndicator_ = indicator;
    		if(handle_ != null) closeHandle_ = true;
    	}
    }
    
    /**
     * Returns the list indicator indicating if the message types in the list should
     * be omitted or selected.
     * @return The list indicator.  Possibe values are:
     * <ul>
     * <li>{@link #OMIT}</li>
     * <li>{@link #SELECT}</li>
     * </ul>
     */
    public int getMessageTypeListIndicator()
    {
    	return typesListIndicator_;
    }
    
    /**
     * Specifies the specific message types to be retrieved or omitted.  Up to 10
     * message types are supported.  If not specified, all of the messages will
     * be listed for the jobs, message severity and times specified.
     * @param types The message types to be retrieved or omitted.  Valid values are:
     * <ul>
     * <li>{@link #TYPE_COMPLETION}</li>
     * <li>{@link #TYPE_COPY}</li>
     * <li>{@link #TYPE_DIAGNOSTIC}</li>
     * <li>{@link #TYPE_ESCAPE}</li>
     * <li>{@link #TYPE_INFORMATIONAL}</li>
     * <li>{@link #TYPE_INQUIRY}</li>
     * <li>{@link #TYPE_NOTIFY}</li>
     * <li>{@link #TYPE_REPLY}</li>
     * <li>{@link #TYPE_REQUEST}</li>
     * </ul>
     * @throws ExtendedIllegalArgumentException
     */
    public void setMessageTypes(String[] types)throws ExtendedIllegalArgumentException{
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting message types");
    	if(types == null)
    	{
    		Trace.log(Trace.ERROR, "Parameter 'types' is null.");
            throw new NullPointerException("types");
    	}
    	// up to 10 message types are supported
    	if(types.length > 10)
    	{
    		Trace.log(Trace.ERROR, "Length of types not valid.");
    		throw new ExtendedIllegalArgumentException("types (" + types.length + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    	}
    	// verify each type is one of the valid types
    	for(int i=0; i<types.length; i++)
    	{
    		String type = types[i];
    		if(!type.equals(TYPE_COMPLETION) && 
    		   ! type.equals(TYPE_COPY) &&
    	       !type.equals(TYPE_DIAGNOSTIC) &&
    	       !type.equals(TYPE_ESCAPE) &&
    	       !type.equals(TYPE_INFORMATIONAL) &&
    	       !type.equals(TYPE_INQUIRY) &&
    	       !type.equals(TYPE_NOTIFY) &&
    	       !type.equals(TYPE_REPLY) &&
    	       !type.equals(TYPE_REQUEST))
    			throw new ExtendedIllegalArgumentException("types[" + i + "] (" + type + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    	}
    	
    	synchronized(this)
    	{
    		messageTypes_ = types;
    		if(handle_ != null) closeHandle_ = true;
    	}
    }
    
    /**
     * Returns the message types to be selected or omitted from the list.
     * @return The message types.  Valid values are:
     * <ul>
     * <li>{@link #TYPE_COMPLETION}</li>
     * <li>{@link #TYPE_COPY}</li>
     * <li>{@link #TYPE_DIAGNOSTIC}</li>
     * <li>{@link #TYPE_ESCAPE}</li>
     * <li>{@link #TYPE_INFORMATIONAL}</li>
     * <li>{@link #TYPE_INQUIRY}</li>
     * <li>{@link #TYPE_NOTIFY}</li>
     * <li>{@link #TYPE_REPLY}</li>
     * <li>{@link #TYPE_REQUEST}</li>
     * </ul>
     */
    public String[] getMessageTypes()
    {
    	return messageTypes_;
    }
    
    /**
     * The minimum severity of the messages to be listed.  Possible values are
     * 0 through 99.  Specify 0 to list all messages for the jobs, times, message types,
     * and message IDs specified.  
     * @param severity The minumum severity of the messages to be listed. The default is zero.
     * @throws ExtendedIllegalArgumentException
     */
    public void setMessageSeverity(int severity) throws ExtendedIllegalArgumentException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting message severity: " + severity);
    	if(severity < 0 || severity > 99)
    		throw new ExtendedIllegalArgumentException("severity (" + severity + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    	synchronized(this)
    	{
    		messageSeverity_ = severity;
    		if(handle_ != null) closeHandle_ = true;
    	}
    }
    
    /**
     * Returns the minimum severity of the messages to be listed.
     * @return The minumum severity.  Possible values are 0 through 99.  0 indicates
     * all messages for the jobs, times, message types, and message ids specified
     * should be listed.
     */
    public int getMessageSeverity()
    {
    	return messageSeverity_;
    }
    
    /**
     * Specifies the specific jobs (if any) for which messages in the log are listed.
     * The messages for the specified jobs are retrieved only if they are logged in the 
     * period of time, message severity, and messages specified on the call.  
     * Up to 5 jobs are supported.
     * @param jobs The list of jobs.
     * @throws ExtendedIllegalArgumentException if the number of jobs is greater than five
     */
    public void setJobs(Job[] jobs) throws ExtendedIllegalArgumentException{
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting jobs");
    	// Only up to five jobs are supported
    	if(jobs == null)
    	{
    		Trace.log(Trace.ERROR, "Parameter 'jobs' is null.");
            throw new NullPointerException("jobs");
    	}
    	
    	if(jobs.length > 5)
    	{
    		Trace.log(Trace.ERROR, "Length of jobs not valid.");
    		throw new ExtendedIllegalArgumentException("jobs (" + jobs.length + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    	}
    	
    	//verify the name, user, and number are set on each of the jobs
    	for(int i=0; i<jobs.length; i++)
    	{
    		if(jobs[i] == null)
    			throw new NullPointerException("job(" + i+ ")");
    		if(jobs[i].getName() == null)
    			throw new NullPointerException("job name(" + i + ")");
    		if(jobs[i].getNumber() == null)
    			throw new NullPointerException("job number(" + i + ")");
    		if(jobs[i].getUser() == null)
    			throw new NullPointerException("job user(" + i + ")");
    	}
    	synchronized(this)
    	{
    		jobsToList_ = jobs;
    		if(handle_ != null) closeHandle_ = true;
    	}
    }
    
    /**
     * Returns the list of jobs used to filter which messages in the log are listed.
     * @return The list of jobs or null if no jobs were specified.
     */
    public Job[] getJobs()
    {
    	return jobsToList_;
    }
        
    /**
    Returns a 2 digit String representation of the value.  
    The value will be 0-padded on the left if needed.

    @param value    The value.
    @return         The 2 digit String representation.
    **/
    private String twoDigits(int value)
    {
    	if (value > 99)
    		throw new ExtendedIllegalArgumentException("value", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    	
    	String full = "00" + Integer.toString(value);
    	return full.substring(full.length() - 2);
    }


    /**
     Closes the list on the system when this object is garbage collected.
     **/
    protected void finalize() throws Throwable
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Finalize method for history log invoked.");
        if (handle_ != null) try { close(); } catch (Throwable t) {}
        super.finalize();
    }

}
