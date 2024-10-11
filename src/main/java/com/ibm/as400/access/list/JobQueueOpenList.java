///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  JobQueueOpenList.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2018-2019 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access.list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Bin4;
import com.ibm.as400.access.AS400DataType;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.AS400Structure;
import com.ibm.as400.access.AS400Text;
import com.ibm.as400.access.BinaryConverter;
import com.ibm.as400.access.Converter;
import com.ibm.as400.access.ErrorCodeParameter;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.ProgramCall;
import com.ibm.as400.access.ProgramParameter;

/**
 * An {@link com.ibm.as400.access.list.OpenList OpenList} implementation that
 * generates lists of {@link com.ibm.as400.access.list.JobQueueListItem JobQueueListItem}
 * objects.
 * 
 * <pre>
 * AS400 system = new AS400("mySystem", "myUserID", "myPassword");
 * // Return all queues with format FORMAT_0100 (OJBQ0100)
 * JobQueueOpenList list = new JobQueueOpenList(system);
 * list.open();
 * Enumeration items = list.getItems();
 * while (items.hasMoreElements()) {
 * 	   JobQueueListItem item = (JobQueueListItem) items.nextElement();
 * 	   System.out.println(item.getJobQueueName() + "/" + item.getJobQueueStatus());
 * }
 * list.close();
 * </pre>
 **/

public class JobQueueOpenList extends OpenList {

	private static final long serialVersionUID = 4436839827725754124L;
	
	/**
	 * Constant indicating that this list will accept parameters for, and generate,
	 * JobQueueListItem objects in accordance with the OJBQ0100 format of the
	 * underlying JobQueue.
	 **/
	public static final String FORMAT_0100 = "OJBQ0100";

	public static final AS400DataType JOB_QUEUE_NAME = new AS400Text(10);
	public static final AS400DataType JOB_QUEUE_LIBRARY_NAME = new AS400Text(10);
	public static final AS400DataType JOB_QUEUE_STATUS = new AS400Text(1);
	public static final AS400DataType SUBSYSTEM_NAME = new AS400Text(10);
	public static final AS400DataType SUBSYSTEM_LIBRARY_NAME = new AS400Text(10);
	private static final AS400DataType RESERVED = new AS400Text(3);
	public static final AS400DataType NUMBER_OF_JOBS_WAITING = new AS400Bin4();
	public static final AS400DataType SEQUENCE = new AS400Bin4();
	public static final AS400DataType MAXIMUM_ACTIVE = new AS400Bin4();
	public static final AS400DataType CURRENT_ACTIVE = new AS400Bin4();
	public static final AS400DataType DESCRIPTION = new AS400Text(50);
	public static final AS400DataType ASP_NAME = new AS400Text(10);

	private static final List<AS400DataType> ITEM_ELEMENTS = Arrays.asList(JOB_QUEUE_NAME, JOB_QUEUE_LIBRARY_NAME,
			JOB_QUEUE_STATUS, SUBSYSTEM_NAME, SUBSYSTEM_LIBRARY_NAME, RESERVED, NUMBER_OF_JOBS_WAITING,
			SEQUENCE, MAXIMUM_ACTIVE, CURRENT_ACTIVE, DESCRIPTION, ASP_NAME);

	private static final AS400Structure FILTER_STRUCTURE = new AS400Structure(new AS400DataType[] {
			new AS400Bin4(),            //  0 length of filter
			JOB_QUEUE_NAME,             //  1 job queue name
			JOB_QUEUE_LIBRARY_NAME,     //  2 job queue library name
			SUBSYSTEM_NAME });          //  3 active subsystem name
	
	private class SortField {
		AS400DataType field;
		boolean ascending;
		private SortField(AS400DataType field, boolean ascending) {
			this.field = field;
			this.ascending = ascending;
		}
	}
	
	private JobQueueListFilter filter;
	private List<SortField> sortFields = new ArrayList<SortField>(2);

	///////////////////////////////////////////////////////////

	public JobQueueOpenList(AS400 system) {
		super(system);
		setFilter(JobQueueListFilter.DEFAULT);
	}

	public JobQueueListFilter getFilter() {
		return filter;
	}

	public void setFilter(JobQueueListFilter filter) {
		this.filter = filter;
		resetHandle();
	}
	
	public void clearSortFields() {
		this.sortFields.clear();
	}

	public void addSortField(AS400DataType sortField, boolean ascending) {
		int ndx = ITEM_ELEMENTS.indexOf(sortField);
		if (ndx < 0) {
			throw new IllegalArgumentException("sortField");
		}
		this.sortFields.add(new SortField(sortField, ascending));
	}

	///////////////////////////////////////////////////////////

	/**
	 * Pad a string with spaces at the end to the desired length.
	 */
	protected static String padRight(String string, int length) {
		return String.format("%1$-" + length + "s", string == null ? "" : string);
	}

	/**
	 * Trim a string if not null, or return the empty string
	 */
	protected static String trimSafe(String string) {
		return string == null ? "" : string.trim();
	}
    
	private int getOffset(AS400DataType field) {
		int ndx = ITEM_ELEMENTS.indexOf(field);
		return getOffset(ndx);
	}

	private int getRecordLength() {
		return getOffset(ITEM_ELEMENTS.size());
	}

	private int getOffset(int ndx) {
		if (ndx < 0 || ndx > ITEM_ELEMENTS.size()) {
			throw new IndexOutOfBoundsException();
		}

        int offset = 0;
        for (int i = 0; i < ndx; ++i)
        {
        	offset += ITEM_ELEMENTS.get(i).getByteLength();
        }
        return offset;
	}

	private short getFieldDataType(AS400DataType field) {
		//TODO more types
		if (field instanceof AS400Text) return 4;
		return 0; //signed binary by default
	}

	private byte[] createSortData() {
		int sortItemSize = 12;
	    int numSortKeys = this.sortFields.size();
	    byte[] sortData = new byte[4 + (numSortKeys * sortItemSize)];
	    BinaryConverter.intToByteArray(numSortKeys, sortData, 0);

	    int sortOffset = 0;
	    for (int i = 0; i < numSortKeys; i++) {
	    	SortField field = sortFields.get(i);

	        BinaryConverter.intToByteArray(getOffset(field.field), sortData, 4 + sortOffset); //offset of sort field in row
	        BinaryConverter.intToByteArray(field.field.getByteLength(), sortData, 8 + sortOffset); //length of sort field
	        BinaryConverter.shortToByteArray(getFieldDataType(field.field), sortData, 12 + sortOffset); //type of sort field
	        // '1' = ascending, '2' = descending (0xF1 = 1 and 0xF2 = 2)
	        sortData[14 + sortOffset] = field.ascending ? (byte)0xF1 : (byte)0xF2;

	        sortOffset += sortItemSize;
	    }
		return sortData;
	}

	///////////////////////////////////////////////////////////

	@Override
	protected byte[] callOpenListAPI() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException {
        Converter conv = new Converter(system_.getCcsid(), system_);

        //TODO do this directly since structure doesn't support conv?
        Object[] filterValues = new Object[] { FILTER_STRUCTURE.getByteLength(),
        		padRight(filter.getJobQueueName(), JOB_QUEUE_NAME.getByteLength()),
        		padRight(filter.getJobQueueLibraryName(), JOB_QUEUE_LIBRARY_NAME.getByteLength()),
        		padRight(filter.getActiveSubsystemName(), SUBSYSTEM_NAME.getByteLength()) };
        byte[] filterData = FILTER_STRUCTURE.toBytes(filterValues);
        
        byte[] sortData = createSortData();
	    
	    ProgramParameter[] parms = new ProgramParameter[8];
	    parms[0] = new ProgramParameter(1); // receiver variable
	    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(0)); // length of receiver variable //TODO set this to fit the desired number of records and read them directly
	    parms[2] = new ProgramParameter(conv.stringToByteArray(FORMAT_0100));
	    parms[3] = new ProgramParameter(80 /*ListUtilities.LIST_INFO_LENGTH*/); // list information
	    parms[4] = new ProgramParameter(filterData);
	    parms[5] = new ProgramParameter(sortData);
	    parms[6] = new ProgramParameter(BinaryConverter.intToByteArray(1));
	    parms[7] = new ErrorCodeParameter();

        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QSPOLJBQ.PGM", parms); // threadsafe
        pc.setThreadSafe(true);
        if (!pc.run())
        {
            throw new AS400Exception(pc.getMessageList());
        }

        return parms[3].getOutputData();
	}

	@Override
	protected Object[] formatOutputData(byte[] data, int recordsReturned, int recordLength)
			throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
	{
		JobQueueListItem[] list = new JobQueueListItem[recordsReturned];
        Converter conv = new Converter(system_.getCcsid(), system_);
		
        int offset = 0;
        for (int i = 0; i < recordsReturned; i++) {
			int localOffset = offset;

			JobQueueListItem item = new JobQueueListItem();
			list[i] = item;

			item.jobQueueName = trimSafe(conv.byteArrayToString(data, localOffset, 10));
			localOffset += 10;
			item.jobQueueLibrary = trimSafe(conv.byteArrayToString(data, localOffset, 10));
			localOffset += 10;
			String statusChar = conv.byteArrayToString(data, localOffset, 1);
			item.jobQueueStatus = JobQueueListItem.JobQueueStatus.fromSystemValue(statusChar);
			localOffset += 1;
			item.subsystemName = trimSafe(conv.byteArrayToString(data, localOffset, 10));
			localOffset += 10;
			item.subsystemLibrary = trimSafe(conv.byteArrayToString(data, localOffset, 10));
			localOffset += 10;
			localOffset += 3; //reserved
			item.numberOfJobsWaitingToRun = BinaryConverter.byteArrayToInt(data, localOffset); //-1 is no subsystem or damaged
			localOffset += 4;
			item.jobQueueSequence = BinaryConverter.byteArrayToInt(data, localOffset); //-1 is no subsystem or damaged
			localOffset += 4;
			item.maximumActiveJobs = BinaryConverter.byteArrayToInt(data, localOffset); //-1 is *NOMAX, -2 is no subsystem or damaged
			localOffset += 4;
			item.numberOfJobsRunning = BinaryConverter.byteArrayToInt(data, localOffset);
			localOffset += 4;
			item.jobQueueDescription = trimSafe(conv.byteArrayToString(data, localOffset, 50));
			localOffset += 50;
			item.aspName = trimSafe(conv.byteArrayToString(data, localOffset, 10));
			localOffset += 10;

        	offset += recordLength;
        }

        return list;
	}

	@Override
	protected int getBestGuessReceiverSize(int number) {
		return getRecordLength() * number;
	}

}
