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
    // Get job name, user, job number for the correct job.
    public String getJobInfo(boolean threadSafety) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException;
    // Check command object's thread safety.
    public boolean isCommandThreadSafe(String command) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException;
    // Get the message list from the implementation object.
    public AS400Message[] getMessageList();
    // Run the command on the implementation object.
    public boolean runCommand(String command, boolean threadSafety, int messageOption) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException;
    // Run the command on the implementation object.
    public boolean runCommand(byte[] command, boolean threadSafety, int messageOption) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException;
    // Run the program call on the implementation object.
    public boolean runProgram(String library, String name, ProgramParameter[] parameterList, boolean threadSafety, int messageOption) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException;
    // Run the service program call on the implementation object.
    public byte[] runServiceProgram(String library, String name, String procedureName, int returnValueFormat, ProgramParameter[] serviceParameterList, boolean threadSafety, int procedureNameCCSID, int messageOption) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException;
    // Set the AS400Impl into the implementation object.
    public void setSystem(AS400Impl system) throws IOException;
}
