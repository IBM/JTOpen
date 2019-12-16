///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SubsystemEntryList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2009-2010 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a list of subsystem entries.
 * <p>
 * Implementation note: This class internally calls the "List Subsystem Entries" API (QWDLSBSE).
 * Information from formats SBSE0100 is retrieved.
 *
 * <p>
 * @see RoutingDataEntry
 * 
 */
public class SubsystemEntryList {
	private static final QSYSObjectPathName USERSPACE_PATH = new QSYSObjectPathName("QTEMP", "JT4QLSBSE", "USRSPC");
	private static final String SBSE_FORMAT_100 = "SBSE0100";
	private static final String SBSE_FORMAT_200 = "SBSE0200";
	private static final String SBSE_FORMAT_300 = "SBSE0300";
	private static final String SBSE_FORMAT_400 = "SBSE0400";
	private static final String SBSE_FORMAT_500 = "SBSE0500";
	private static final String SBSE_FORMAT_600 = "SBSE0600";
	private static final String SBSE_FORMAT_700 = "SBSE0700";
	
	public static final int ROUTING_ENTRY = 1;
	public static final int COMMUNICATION_ENTRY = 2;
	public static final int REMOTE_LOCATION_ENTRY = 3;
	public static final int AUTOSTART_JOB_ENTRY = 4;
	public static final int PRESTART_JOB_ENTRY = 5;
	public static final int WORKSTATION_NAME_ENTRY = 6;
	public static final int WORKSTATION_TYPE_ENTRY = 7;

	private AS400 system_;
    private String name_;
	private String library_;
	private int listType_;
	private QSYSObjectPathName path_;
	private final Map routingDataDescription_ = new HashMap();
	//QWDLSBSE
	
	/**
	 * Constructs a SubsystemEntryList object.
	 * @param system The system where the subsystem resides.
	 * @param library The library containing the subsystem.
	 * @param name The subsystem name
	 */
	public SubsystemEntryList(AS400 system, String library, String name) {
		if (system == null)  throw new NullPointerException("system");
		if (library == null) throw new NullPointerException("Subsystem library");
		if (name == null) throw new NullPointerException("Subsystem name");

		system_ = system;
		name_ = name;
	    library_ = library;
		path_ = new QSYSObjectPathName(library_, name_, "SBSD");

	}
	
	 /**
	   * Constructs a SubsystemEntryList.
	   * @param system The system where the subsystem resides.
	   * @param path The fully qualified IFS path to the subsystem.
	   **/
	public SubsystemEntryList(AS400 system, QSYSObjectPathName path) {
		if (system == null)  throw new NullPointerException("system");
	    if (path == null)    throw new NullPointerException("path");
	    
		system_ = system;
		path_ = path;
	}
	
	/**
	   * Determine the format to use on the API call, depending on the attribute.
	   *
	   * @param attribute Attribute to be retrieved
	   * @return format string
	   */
	private String lookupFormat(int listType) {
		String format;
	    switch (listType) {
	        case ROUTING_ENTRY:
	        	format = SBSE_FORMAT_100;
	        	break;
	        case COMMUNICATION_ENTRY:
	        	format = SBSE_FORMAT_200;
	        	break;
	        case REMOTE_LOCATION_ENTRY:
	        	format = SBSE_FORMAT_300;
	        	break;
	        case AUTOSTART_JOB_ENTRY:
	        	format = SBSE_FORMAT_400;
	        	break;
	        case PRESTART_JOB_ENTRY:
	        	format = SBSE_FORMAT_500;
	        	break;
	        case WORKSTATION_NAME_ENTRY:
	        	format = SBSE_FORMAT_600;
	        	break;
	        case WORKSTATION_TYPE_ENTRY:
	        	format = SBSE_FORMAT_700;
	        	break;
	        default:
	            if (Trace.traceOn_) Trace.log(Trace.WARNING, "Unrecognized list type:", listType);
	            format = SBSE_FORMAT_100;
	    }
	    return format;
	}
	
	/**
	   * Reloads all entries that have been specified.
	   *
	   * @throws ObjectDoesNotExistException If a system object necessary for the call does not exist on the system.
	   * @throws InterruptedException If this thread is interrupted.
	   * @throws IOException If an error occurs while communicating with the system.
	   * @throws ErrorCompletingRequestException If an error occurs before the request is completed.
	   * @throws AS400SecurityException If a security or authority error occurs.
	   */
	public void refresh()
		    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException,
		  IOException, ObjectDoesNotExistException
		  {
		    clear();
		    load();
		  }
	
	/**
	   * Loads all entries from the specified subsystem.
	   *
	   * @throws ObjectDoesNotExistException If an object necessary for the call does not exist on the system.
	   * @throws InterruptedException If this thread is interrupted.
	   * @throws IOException If an error occurs while communicating with the system.
	   * @throws ErrorCompletingRequestException If an error occurs before the request is completed.
	   * @throws AS400SecurityException If a security or authority error occurs.
	   * @throws AS400Exception If the program on the server sends an escape message.
	   */
	public void load() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, 
	    IOException, InterruptedException, ObjectDoesNotExistException{
		ProgramCall program = new ProgramCall(system_, "/QSYS.LIB/QWDLSBSE.PGM", buildProgramParameters(getFormat()));  // this API is not threadsafe
		
		Object lockObject;
	    boolean willRunProgramsOnThread = program.isStayOnThread();
	    if (willRunProgramsOnThread) {
	      // The calls will run in the job of the JVM, so lock for entire JVM.
	      lockObject = USERSPACE_PATH;
	    }
	    else {
	      // The calls will run in the job of the Remote Command Host Server, so lock on the connection.
	      lockObject = system_;
	    }

	    synchronized (lockObject)
	    {
	      // Create a user space in QTEMP to receive output from program call.
	      UserSpace us = new UserSpace(system_, USERSPACE_PATH.getPath());
	      us.setMustUseProgramCall(true);
	      if (!willRunProgramsOnThread)
	      {
	        us.setMustUseSockets(true);
	        // Force the use of sockets when running natively but not on-thread.
	        // We have to do it this way since UserSpace will otherwise make a native ProgramCall, and will use a different QTEMP library than that used by the host server.
	      }

	      try
	      {
	        us.create(65535, true, "JT400", (byte) 0x00, "Userspace for loading members","*ALL");  // set public authority to *ALL

	        if (!program.run()) {
	          throw new AS400Exception(program.getMessageList());
	        }

	        byte[] usBuf = new byte[65535];  // local buffer to hold bytes read from user space

	        // Read the "generic header" from the user space, into the local buffer.
	        // Note: For description of general layout of the "list API" headers, see:
	        // http://publib.boulder.ibm.com/infocenter/iseries/v6r1m0/topic/apiref/listGeneral.htm
	        int numBytesRead = us.read(usBuf, 0, 0, 0x90);  // just read the needed header fields
	        if (numBytesRead < 0x90) // verify that we at least got a header, up through "CCSID" field
	        {
	          Trace.log(Trace.ERROR, "Failed to read the generic header.  Number of bytes read: " + numBytesRead);
	          throw new InternalErrorException(InternalErrorException.UNKNOWN, numBytesRead);
	        }

	        // Parse the header, to get the offsets to the various sections.

	        // (Generic header) Offset to header section:
	        int offsetToHeaderSection = BinaryConverter.byteArrayToInt(usBuf, 0x74);

	        // (Generic header) Header section size:
	        int headerSectionSize = BinaryConverter.byteArrayToInt(usBuf, 0x78);

	        // (Generic header) Offset to list data section:
	        int offsetToListDataSection = BinaryConverter.byteArrayToInt(usBuf, 0x7C);

	        // (Generic header) List data section size:
	        int listDataSectionSize = BinaryConverter.byteArrayToInt(usBuf, 0x80);

	        // (Generic header) Number of list entries:
	        int numberOfListEntries = BinaryConverter.byteArrayToInt(usBuf, 0x84);

	        // (Generic header) Size of each entry:
	        int sizeOfEachEntry = BinaryConverter.byteArrayToInt(usBuf, 0x88);

	        // (Generic header) CCSID of data in the user space
	        int entryCCSID = BinaryConverter.byteArrayToInt(usBuf, 0x8C);

	        // (Generic header) Subsetted list indicator:
	        //String subsettedListIndicator = conv.byteArrayToString(usBuf, 0x95, 1);

	        if (entryCCSID == 0) entryCCSID = system_.getCcsid();
	        // From the API spec: "The coded character set ID for data in the list entries.  If 0, then the data is not associated with a specific CCSID and should be treated as hexadecimal data."

	        final CharConverter conv = new CharConverter(entryCCSID);

	        // Read the "header section" into the local buffer.
	        numBytesRead = us.read(usBuf, offsetToHeaderSection, 0, headerSectionSize);
	        if (numBytesRead < headerSectionSize)
	        {
	          Trace.log(Trace.ERROR, "Failed to read the header section.  Number of bytes read: " + numBytesRead);
	          throw new InternalErrorException(InternalErrorException.UNKNOWN, numBytesRead);
	        }

	        // (Header section) File library name used:
	        //final String libraryNameUsed = conv.byteArrayToString(usBuf, 10, 10).trim();

	        // Read the "list data section" into the local buffer.
	        if (listDataSectionSize > usBuf.length) {
	          usBuf = new byte[listDataSectionSize+1]; // allocate a larger buffer
	        }
	        numBytesRead = us.read(usBuf, offsetToListDataSection, 0, listDataSectionSize);
	        if (numBytesRead < listDataSectionSize)
	        {
	          Trace.log(Trace.ERROR, "Failed to read the list data section.  Number of bytes read: " + numBytesRead);
	          throw new InternalErrorException(InternalErrorException.UNKNOWN, numBytesRead);
	        }

	        // Parse the list data returned in the user space.
	        String format = getFormat();
	        for (int i = 0; i < numberOfListEntries; i++)
	        {
	          byte[] entryBuf = new byte[sizeOfEachEntry];
	          System.arraycopy(usBuf, i * sizeOfEachEntry, entryBuf, 0, sizeOfEachEntry);
	          if (format.equals(SBSE_FORMAT_100)) {
	        	  readRoutingDataEntries(entryBuf, format, conv);
	          }
	          
	        }
	      }

	      finally {
	        // Delete the temporary user space, to allow other threads to re-create and use it.
	        try { us.delete(); }
	        catch (Exception e) {
	          Trace.log(Trace.ERROR, "Exception while deleting temporary user space", e);
	        }
	      }
	    }
	}
	
	/**
	   * Returns the format for the call to QWDLSBSE depending on the entry type to be retrieved.
	   *
	   * @return format name(only SBSE0100 for now)
	   */
	private String getFormat() {
		String format = SBSE_FORMAT_100;
		String tempFormat = lookupFormat(listType_);
		if (format.compareTo(tempFormat) < 0)
			format = tempFormat;
		return format;	
	}
	
	public void setListType(int type) {
		listType_ = type;
	}
	
	/**
	   * Builds the program parameter list for a program call to QWDLSBSE.
	   *
	   * @return Program parameter list for QWDLSBSE
	   */
	private ProgramParameter[] buildProgramParameters(String format) throws UnsupportedEncodingException {
		ProgramParameter[] parameterList = new ProgramParameter[4];

	    // Qualified user space name:
	    parameterList[0] = new ProgramParameter(CharConverter.stringToByteArray(system_, USERSPACE_PATH.toQualifiedObjectName()));
	    // Format name:
	    parameterList[1] = new ProgramParameter(CharConverter.stringToByteArray(system_, format));
	    // Qualified subsystem name:
	    parameterList[2] = new ProgramParameter(CharConverter.stringToByteArray(system_, path_.toQualifiedObjectName()));
	    // Error code:
	    parameterList[3] = new ErrorCodeParameter();

	    return parameterList;
	}
	
	private void readRoutingDataEntries(byte[] entryBuf, String format, CharConverter charConverter) {
		
		String compareValue = charConverter.byteArrayToString(entryBuf, 56, 10).trim();
		RoutingDataEntry routingDataEntry = (RoutingDataEntry)routingDataDescription_.get(compareValue);
		if (routingDataEntry == null) {
			routingDataEntry = new RoutingDataEntry();
		}
			int offset = 0;
			int routingEntrySequenceNum = BinaryConverter.byteArrayToInt(entryBuf, offset);
			String routingEntryProgramName = charConverter.byteArrayToString(entryBuf, 4, 10).trim();
			String routingEntryProgramLibrary = charConverter.byteArrayToString(entryBuf, 14, 10).trim();
			String routingEntryClassName = charConverter.byteArrayToString(entryBuf, 24, 10).trim();
			String routingEntryClassLibrary = charConverter.byteArrayToString(entryBuf, 24, 10).trim();
			int maxActiveRoutingSteps = BinaryConverter.byteArrayToInt(entryBuf, 44);
			int routingEntryPoolIden = BinaryConverter.byteArrayToInt(entryBuf, 48);
			int compareStartPosition = BinaryConverter.byteArrayToInt(entryBuf, 52);
			String routingEntryThreadResourcesAffinityGroup = charConverter.byteArrayToString(entryBuf, 136, 10).trim();
			String routingEntryThreadResourcesAffinityLevel = charConverter.byteArrayToString(entryBuf, 146, 10).trim();
			String routingEntryResourcesAffinityGroup = charConverter.byteArrayToString(entryBuf, 156, 10).trim();
			
			//Set value
			routingDataEntry.setcompareValue(compareValue);
			routingDataEntry.setroutingEntrySequenceNum(routingEntrySequenceNum);
			routingDataEntry.setroutingEntryProgramName(routingEntryProgramName);
			routingDataEntry.setroutingEntryProgramLibrary(routingEntryProgramLibrary);
			routingDataEntry.setroutingEntryClassName(routingEntryClassName);
			routingDataEntry.setroutingEntryClassLibrary(routingEntryClassLibrary);
			routingDataEntry.setmaxActiveRoutingStep(maxActiveRoutingSteps);
			routingDataEntry.setroutingEntryPoolIden(routingEntryPoolIden);
			routingDataEntry.setcompareStartPosition(compareStartPosition);
			routingDataEntry.setroutingEntryThreadResourcesAffinityGroup(routingEntryThreadResourcesAffinityGroup);
			routingDataEntry.setroutingEntryThreadResourcesAffinityLevel(routingEntryThreadResourcesAffinityLevel);
			routingDataEntry.setroutingEntryResourcesAffinityGroup(routingEntryResourcesAffinityGroup);
			
			routingDataDescription_.put(compareValue, routingDataEntry);
	}
	
	/**
	   * Returns the routing data entry by compareValue. If no entry null is returned.
	   * @return the routing data entry by compareValue
	   */
	public RoutingDataEntry getRoutingDataEntry(String compareValue) {
		return (RoutingDataEntry)routingDataDescription_.get(compareValue);
	}
	
	/**
	   * Returns an array of routing data entries. If no entry could be
	   * retrieved because there are no entry or because of an error, an empty array is returned.
	   * If no entry has been retrieved yet due to no call to load(), then an empty array is
	   * returned.
	   *
	   * @return Array of retrieved member descriptions
	   */
	public RoutingDataEntry[] getRoutingDataEntry() {
		return (RoutingDataEntry[])routingDataDescription_.values().toArray(new RoutingDataEntry[routingDataDescription_.size()]);
	}
	
	/*
	public RoutingDataEntry[] getDataEntry() {
		if (getFormat().equals(SBSE_FORMAT_100))
		    return (RoutingDataEntry[])routingDataDescription_.values().toArray(new RoutingDataEntry[routingDataDescription_.size()]);
		if (getFormat().equals(SBSE_FORMAT_200))
			return 
	}
	*/
	/**
	   * Removes all entries from this object.
	   */
	public void clear() {
		routingDataDescription_.clear();
	}

}
