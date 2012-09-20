///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  ListDiskStatusesImpl.java
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
import com.ibm.jtopenlite.command.program.print.*;
import com.ibm.jtopenlite.command.program.openlist.*;
import com.ibm.jtopenlite.ddm.*;
import java.io.*;
import java.util.*;

class ListDiskStatusesImpl implements
		OpenListOfSpooledFilesFormatOSPL0300Listener,
		OpenListOfSpooledFilesFilterOSPF0100Listener, DDMReadCallback {
	private boolean done_ = false;
	private String outputQueueLibrary_ = null;

	ListDiskStatusesImpl() {
	}

	private String jobName_;
	private String jobUser_;
	private String jobNumber_;
	private String spooledFileName_;
	private int spooledFileNumber_;

	private String elapsedTime_; // TODO

	private final char[] charBuffer_ = new char[132];

	private int skip_ = 0;
	private boolean theEnd_ = false;
  private final ArrayList<DiskStatus> statuses_ = new ArrayList<DiskStatus>();

	public void newRecord(DDMCallbackEvent event, DDMDataBuffer dataBuffer) {
		final byte[] data = dataBuffer.getRecordDataBuffer();
		String line = Conv.ebcdicByteArrayToString(data, 0, data.length,
				charBuffer_);
		if (skip_ == 1) {
			int index = line.indexOf(":");
			if (index > 0) {
				int end = line.indexOf("System name", index);
				if (end > index) {
          elapsedTime_ = line.substring(index + 1, end - index).trim();
				}
			}
			++skip_;
		} else if (skip_ < 4) {
			++skip_;
		} else if (!theEnd_) {
			if (line.indexOf("E N D  O F  L I S T I N G") >= 0) {
				theEnd_ = true;
			} else {
				StringTokenizer st = new StringTokenizer(line);
				String unit = nextToken(st);
				String type = nextToken(st);
				String sizeMB = nextToken(st);
				String percentUsed = nextToken(st);
				String ioRequests = nextToken(st);
				String requestSizeKB = nextToken(st);
				String readRequests = nextToken(st);
				String writeRequests = nextToken(st);
				String readKB = nextToken(st);
				String writeKB = nextToken(st);
				String percentBusy = nextToken(st);
				String asp = nextToken(st);
				String protectionType = nextToken(st);
				String protectionStatus = nextToken(st);
				String compression = nextToken(st);
				DiskStatus ds = new DiskStatus(unit, type, sizeMB, percentUsed,
						ioRequests, requestSizeKB, readRequests, writeRequests,
						readKB, writeKB, percentBusy, asp, protectionType,
						protectionStatus, compression);
				statuses_.add(ds);
			}
		}
	}

	private String nextToken(StringTokenizer st) {
		return st.hasMoreTokens() ? st.nextToken() : "";
	}

	public void recordNotFound(DDMCallbackEvent event) {
		done_ = true;
	}

	public void endOfFile(DDMCallbackEvent event) {
		done_ = true;
	}

	private boolean done() {
		return done_;
	}

  public void totalRecordsInList(int total) {
  }

  public void openComplete() {
  }

  public boolean stopProcessing() {
    return false;
  }

	public void newSpooledFileEntry(String jobName, String jobUser,
			String jobNumber, String spooledFileName, int spooledFileNumber,
			int fileStatus, String dateOpened, String timeOpened,
			String spooledFileSchedule, String jobSystemName, String userData,
			String formType, String outputQueueName, String outputQueueLibrary,
			int auxiliaryStoragePool, long size, int totalPages,
			int copiesLeftToPrint, String priority,
			int internetPrintProtocolJobIdentifier) {
		jobName_ = jobName;
		jobUser_ = jobUser;
		jobNumber_ = jobNumber;
		spooledFileName_ = spooledFileName;
		spooledFileNumber_ = spooledFileNumber;
	}

	public int getNumberOfUserNames() {
		return 1;
	}

	public String getUserName(int index) {
		return "*ALL";
	}

	public int getNumberOfOutputQueues() {
		return 1;
	}

	public String getOutputQueueName(int index) {
		return "DSKSTS";
	}

	public String getOutputQueueLibrary(int index) {
		return outputQueueLibrary_;
	}

	public String getFormType() {
		return "*ALL";
	}

	public String getUserSpecifiedData() {
		return "*ALL";
	}

	public int getNumberOfStatuses() {
		return 1;
	}

	public String getStatus(int index) {
		return "*ALL";
	}

	public int getNumberOfPrinterDevices() {
		return 1;
	}

	public String getPrinterDevice(int index) {
		return "*ALL";
	}

	public String getElapsedTime() {
		return elapsedTime_;
	}

	/**
	 * NOTE: The workingLibrary will be deleted when this method is called.
	 **/
	public DiskStatus[] getDiskStatuses(final CommandConnection cc,
      final DDMConnection ddmConn, String workingLibrary, boolean reset)
			throws IOException {
		final SystemInfo si1 = cc.getInfo();
		final SystemInfo si2 = ddmConn.getInfo();
		if (!si1.getSystem().equals(si2.getSystem())
				|| si1.getServerLevel() != si2.getServerLevel()) {
      throw new IOException("Command connection does not match DDM connection.");
		}

		skip_ = 0;
		theEnd_ = false;
		statuses_.clear();
		elapsedTime_ = null;
		done_ = false;
		outputQueueLibrary_ = workingLibrary;

		// I really wish spooled files could go into QTEMP!
    // DDMConnection conn = DDMConnection.getConnection("rchasa12", "csmith",
    // "s1r4l0in");
		// CommandConnection conn = CommandConnection.getConnection("rchasa12",
		// "csmith", "s1r4l0in");
		// System.out.println(conn.getJobName());
		CommandResult result = cc.execute("CLROUTQ OUTQ(" + workingLibrary
				+ "/DSKSTS)");
		if (!result.succeeded()) {
      List<Message> messages = result.getMessagesList();
      if (messages.size() != 1 && !messages.get(0).getID().equals("CPF3357")) {
				throw new IOException("Error clearing output queue: "
						+ result.toString());
			}
		}
		result = cc.execute("DLTLIB " + workingLibrary);
		if (!result.succeeded()) {
      List<Message> messages = result.getMessagesList();
      if (messages.size() != 1 || !messages.get(0).getID().equals("CPF2110")) // Library
                                                                              // not
                                                                              // found.
      {
        throw new IOException("Error deleting library: " + result.toString());
			}
		}
		result = cc.execute("CRTLIB " + workingLibrary);
		if (!result.succeeded())
      throw new IOException("Error creating library: " + result.toString());
    result = cc.execute("CRTPF " + workingLibrary
						+ "/DSKSTS RCDLEN(132) MAXMBRS(*NOMAX) SIZE(*NOMAX) LVLCHK(*NO)");
		if (!result.succeeded())
			throw new IOException("Error creating physical file: "
					+ result.toString());
		result = cc.execute("CRTOUTQ OUTQ(" + workingLibrary + "/DSKSTS)");
		if (!result.succeeded())
      throw new IOException("Error creating output queue: " + result.toString());
		result = cc.execute("CHGJOB OUTQ(" + workingLibrary + "/DSKSTS)");
		if (!result.succeeded())
			throw new IOException("Error changing job: " + result.toString());
    result = cc.execute("WRKDSKSTS OUTPUT(*PRINT) RESET("
        + (reset ? "*YES" : "*NO") + ")");
		if (!result.succeeded())
      throw new IOException("Error running WRKDSKSTS: " + result.toString());
    OpenListOfSpooledFilesFormatOSPL0300 format = new OpenListOfSpooledFilesFormatOSPL0300();
    OpenListOfSpooledFiles list = new OpenListOfSpooledFiles(format, 256, -1,
        null, this, null, null, null);
    list.setFormatListener(this);
		result = cc.call(list);
		if (!result.succeeded())
			throw new IOException("Error retrieving spooled file: "
					+ result.toString());
		ListInformation info = list.getListInformation();
		CloseList close = new CloseList(info.getRequestHandle());
		result = cc.call(close);
		if (!result.succeeded())
			throw new IOException("Error closing spooled file list: "
					+ result.toString());
		String jobID = jobNumber_.trim() + "/" + jobUser_.trim() + "/"
				+ jobName_.trim();
    result = cc.execute("CPYSPLF FILE(" + spooledFileName_.trim() + ") TOFILE("
        + workingLibrary + "/DSKSTS) JOB(" + jobID + ") SPLNBR("
        + spooledFileNumber_ + ") MBROPT(*REPLACE)");
		if (!result.succeeded())
      throw new IOException("Error copying spooled file: " + result.toString());

    DDMFile file = ddmConn.open(workingLibrary, "DSKSTS", "DSKSTS", "DSKSTS",
        DDMFile.READ_ONLY, false, 200, 1);
		while (!done()) {
			ddmConn.readNext(file, this);
		}
		ddmConn.close(file);
    result = cc.execute("DLTLIB " + workingLibrary);

		DiskStatus[] arr = new DiskStatus[statuses_.size()];
    statuses_.toArray(arr);
    return arr;

		/*
		 * Class.forName("com.ibm.jtopenlite.database.jdbc.JDBCDriver");
		 * java.sql.Connection c =
		 * DriverManager.getConnection("jdbc:systemi://rchasa12", "csmith",
		 * "s1r4l0in"); Statement s = c.createStatement(); ResultSet rs =
		 * s.executeQuery("SELECT * FROM QZRDDSKSTS.DSKSTS"); int skip = 0;
		 * boolean theEnd = false; while (rs.next()) { String line =
		 * rs.getString(1); if (skip < 4) { ++skip; } else if (!theEnd) { if
		 * (line.indexOf("E N D  O F  L I S T I N G") >= 0) { theEnd = true; }
		 * else { StringTokenizer st = new StringTokenizer(line); String unit =
		 * st.nextToken(); String type = st.nextToken(); String sizeMB =
		 * st.nextToken(); String percentUsed = st.nextToken(); String
		 * ioRequests = st.nextToken(); String requestSizeKB = st.nextToken();
		 * String readRequests = st.nextToken(); String writeRequests =
		 * st.nextToken(); String readKB = st.nextToken(); String writeKB =
		 * st.nextToken(); String percentBusy = st.nextToken(); String asp =
		 * st.nextToken(); String protectionType = st.nextToken(); String
		 * protectionStatus = st.nextToken(); String compression =
		 * st.nextToken(); //TODO } } } rs.close(); s.close(); c.close();
		 */
	}
}
