///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  RemoteCommandImpl.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1999-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

// RemoteCommandImpl defines the implementation interface for the CommandCall and ProgramCall objects.
interface RemoteCommandImpl
{
    static final Boolean ON_THREAD = CommandCall.THREADSAFE_TRUE;
    static final Boolean OFF_THREAD = CommandCall.THREADSAFE_FALSE;
    // Warning... LOOKUP_THREADSAFETY is defined to be NULL.  Use only == for comparisions
    static final Boolean LOOKUP_THREADSAFETY = CommandCall.THREADSAFE_LOOKUP;

    static final int MESSAGE_OPTION_DEFAULT = AS400Message.MESSAGE_OPTION_UP_TO_10;

    // Values returned by getThreadsafeIndicator.
    static final int THREADSAFE_INDICATED_NO = 0;
    static final int THREADSAFE_INDICATED_YES = 1;
    static final int THREADSAFE_INDICATED_CONDITIONAL = 2;

    // Get job name, user, job number for the correct job.
    public String getJobInfo(Boolean threadSafety) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException;
    // Report whether the command is designated as threadsafe on the system.
    public int getThreadsafeIndicator(String command) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException;
    // Report whether the RemoteCommandImpl object is a native object.
    public boolean isNative();
    // Get the message list from the implementation object.
    public AS400Message[] getMessageList();
    // Run the command on the implementation object.
    public boolean runCommand(String command) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException;
    // Run the command on the implementation object.
    public boolean runCommand(String command, Boolean threadSafety, int messageOption) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException;

    // Run the command on the implementation object.
    public boolean runCommand(byte[] commandAsBytes, String commandAsString) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException;

    // Run the command on the implementation object.
    public boolean runCommand(byte[] command, Boolean threadSafety, int messageOption) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException;

    // Run the program call on the implementation object.
    public boolean runProgram(String library, String name, ProgramParameter[] parameterList) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException;

    // Run the program call on the implementation object.
    public boolean runProgram(String library, String name, ProgramParameter[] parameterList, Boolean threadSafety) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException;

    // Run the program call on the implementation object.
    public boolean runProgram(String library, String name, ProgramParameter[] parameterList, Boolean threadSafety, int messageOption) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException;
    // Run the service program call on the implementation object.
    public byte[] runServiceProgram(String library, String name, String procedureName, ProgramParameter[] serviceParameterList) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException;
    // Run the service program call on the implementation object.
    public byte[] runServiceProgram(String library, String name, String procedureName, int returnValueFormat, ProgramParameter[] serviceParameterList, Boolean threadSafety, int procedureNameCCSID, int messageOption, boolean alignOn16Bytes) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException;
    // Set the AS400Impl into the implementation object.
    public void setSystem(AS400Impl system) throws IOException;
}
