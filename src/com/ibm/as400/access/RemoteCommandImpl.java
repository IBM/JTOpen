///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: RemoteCommandImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

// RemoteCommandImpl defines the implementation interface for the CommandCall and ProgramCall objects.
interface RemoteCommandImpl
{
    // Get the message list from the implementation object.
    public abstract AS400Message[] getMessageList();
    // Run the command on the implementation object.
    public abstract boolean runCommand(String command) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException;
    // Run the program call on the implementation object.
    public abstract boolean runProgram(String program, ProgramParameter[] parameterList) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException;
    // Run the service program call on the implementation object.
    public Object[] runServiceProgram(String program, String procedureName, int returnValueFormat, ProgramParameter[] serviceParameterList) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException;
    // Set the AS400Impl into the implementation object.
    public abstract void setSystem(AS400Impl system) throws IOException;
}
