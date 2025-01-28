///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  JobQueueListFilter.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2018-2019 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access.list;

public class JobQueueListFilter {

	public static final String ALL = "*ALL";
	public static final String JOB_QUEUE_ALLOCATED = "*ALLOCATED";
	public static final String JOB_QUEUE_DEFINED = "*DEFINED";
	public static final String JOB_QUEUE_LIBRARY_ALL_USER = "*ALLUSR";
	public static final String JOB_QUEUE_LIBRARY_CURRENT_LIBRARY = "*CURLIB";
	public static final String JOB_QUEUE_LIBRARY_LIBRARY_LIST = "*LIBL";
	public static final String JOB_QUEUE_LIBRARY_USER_LIBRARY = "*USRLIBL";

	public static final JobQueueListFilter DEFAULT = new JobQueueListFilter(ALL, ALL, null);

	private String activeSubsystemName;
	private String jobQueueName;
	private String jobQueueLibraryName;

	public JobQueueListFilter(String jobQueueName, String jobQueueLibraryName, String activeSubsystemName) {
		this.activeSubsystemName = activeSubsystemName;
		this.jobQueueLibraryName = jobQueueLibraryName;
		this.jobQueueName = jobQueueName;
	}

	public static JobQueueListFilter forAllAllocated() {
		return new JobQueueListFilter(JOB_QUEUE_ALLOCATED, null, ALL);
	}
	public static JobQueueListFilter forSubsystem(String subsystemName, boolean allocatedOnly) {
		return new JobQueueListFilter(allocatedOnly ? JOB_QUEUE_ALLOCATED : JOB_QUEUE_DEFINED, null, subsystemName);
	}
	public static JobQueueListFilter forJobQueue(String jobQueueName) {
		return forJobQueue(jobQueueName, ALL);
	}
	public static JobQueueListFilter forJobQueue(String jobQueueName, String jobQueueLibraryName) {
		return new JobQueueListFilter(jobQueueName, jobQueueLibraryName, null);
	}

	public String getActiveSubsystemName() {
		return activeSubsystemName;
	}
	public String getJobQueueName() {
		return jobQueueName;
	}
	public String getJobQueueLibraryName() {
		return jobQueueLibraryName;
	}
}
