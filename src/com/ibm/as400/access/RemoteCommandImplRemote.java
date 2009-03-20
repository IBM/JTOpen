///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  RemoteCommandImplRemote.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2000-2007 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.util.StringTokenizer;

// The RemoteCommandImplRemote class is the remote implementation of CommandCall and ProgramCall.
class RemoteCommandImplRemote implements RemoteCommandImpl
{
    private static final String CLASSNAME = "com.ibm.as400.access.RemoteCommandImplRemote";
    static
    {
        if (Trace.traceOn_) Trace.logLoadPath(CLASSNAME);
    }

    AS400ImplRemote system_;
    ConverterImplRemote converter_;
    ConverterImplRemote unicodeConverter_;
    boolean ccsidIsUserOveride_ = false;  // Flag to say don't override ccsid in open().
    private AS400Server server_;
    AS400Message[] messageList_ = new AS400Message[0];
    int serverDataStreamLevel_ = 0;
    // Flag for detecting when sequential calls switch between on-thread and off-thread.
    protected Boolean priorCallWasOnThread_ = null;

    static
    {
        // Identify all remote command server reply data streams.
        AS400Server.addReplyStream(new RCExchangeAttributesReplyDataStream(), AS400.COMMAND);
        AS400Server.addReplyStream(new RCRunCommandReplyDataStream(), AS400.COMMAND);
        AS400Server.addReplyStream(new RCCallProgramReplyDataStream(), AS400.COMMAND);
        AS400Server.addReplyStream(new RCCallProgramFailureReplyDataStream(), AS400.COMMAND);
    }

    // Report whether the RemoteCommandImpl object is a native object.
    public boolean isNative()
    {
      return false;
    }

    // Set needed impl properties.
    public void setSystem(AS400Impl system) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting up remote command implementation object.");
        system_ = (AS400ImplRemote)system;
        // Check if user has set a ccsid we should use instead of the command server job CCSID.
        int ccsid = system_.getUserOverrideCcsid();
        if (ccsid != 0)
        {
            converter_ = ConverterImplRemote.getConverter(ccsid, system_);
            ccsidIsUserOveride_ = true;
        }
    }

    // Returns information about the job in which the command/program would be run.
    // @param threadSafety  The assumed thread safety of the command/program.
    // @return  Information about the job in which the command/program would be run.  This is a String consisting of a 10-character simple job name, a 10-character user name, and a 6-character job number.
    public String getJobInfo(Boolean threadSafety) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting job information from implementation object.");

        // Note: The runProgram() method that we call below, will call the appropriate open() method.

        // Set up the parameter list for the program that we will use to get the job information (QWCRTVCA).
        ProgramParameter[] parameterList = new ProgramParameter[6];

        // First parameter:  receiver variable - output - char(*).
        // RTVC0100's 20-byte header, plus 26 bytes for "job name" field.
        byte[] dataReceived = new byte[46];
        parameterList[0] = new ProgramParameter(dataReceived.length);

        // Second parameter:  length of receiver variable - input - binary(4).
        byte[] receiverLength = BinaryConverter.intToByteArray(dataReceived.length);
        parameterList[1] = new ProgramParameter(receiverLength);

        // Third parameter:  format name - input - char(8).
        // Set to EBCDIC "RTVC0100".
        byte[] formatName = {(byte)0xD9, (byte)0xE3, (byte)0xE5, (byte)0xC3, (byte)0xF0, (byte)0xF1, (byte)0xF0, (byte)0xF0};
        parameterList[2] = new ProgramParameter(formatName);

        // Fourth parameter:  number of attributes to return - input - binary(4).
        byte[] numAttributes = BinaryConverter.intToByteArray(1); // 1 attribute.
        parameterList[3] = new ProgramParameter(numAttributes);

        // Fifth parameter:  key of attributes to be returned - input - array(*) of binary(4).
        byte[] attributeKey = BinaryConverter.intToByteArray(1009); // "Job name."
        parameterList[4] = new ProgramParameter(attributeKey);

        // Sixth parameter:  error code - input/output - char(*).
        // Eight bytes of zero's indicates to throw exceptions.
        // Send as input because we are not interested in the output.
        parameterList[5] = new ProgramParameter(new byte[8]);

        // Prepare to call the "Retrieve Current Attributes" API.
        // Design note: QWCRTVCA is documented to be conditionally threadsafe.

        // Note: Depending upon whether the program represented by this ProgramCall object will be run on-thread or through the host servers (as indicated by the 'threadsafety' flag), we will issue the job info query accordingly, either on-thread or through the host servers, in order to get the appropriate Job.

        // Retrieve Current Attributes.  Failure is returned as a message list.
        try
        {
            boolean succeeded;
            if (threadSafety == ON_THREAD) {
              succeeded = runProgramOnThread("QSYS", "QWCRTVCA", parameterList, MESSAGE_OPTION_DEFAULT, false);
            }
            else {
              succeeded = runProgramOffThread("QSYS", "QWCRTVCA", parameterList, MESSAGE_OPTION_DEFAULT);
            }
            if (!succeeded)
            {
                Trace.log(Trace.ERROR, "Unable to retrieve job information.");
                throw new AS400Exception(messageList_);
            }
        }
        catch (ObjectDoesNotExistException e)
        {
            Trace.log(Trace.ERROR, "Unexpected ObjectDoesNotExistException:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }

        // Get the data returned from the program.
        dataReceived = parameterList[0].getOutputData();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Job information retrieved:", dataReceived);

        // Examine the "job name" field.  26 bytes starting at offset 20.  The format of the job name is a 10-character simple job name, a 10-character user name, and a 6-character job number.
        return converter_.byteArrayToString(dataReceived, 20, 26);
    }

    // Return message list to public object.
    public AS400Message[] getMessageList()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting message list from implementation object.");
        return messageList_;
    }

    // Return the value of the command's Threadsafe Indicator attribute, as designated on the system.
    // @return The value of the command's Threadsafe Indicator attribute.
    public int getThreadsafeIndicator(String command) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving command Threadsafe indicator.");
        openOnThread();  // The QCDRCMDI API is itself threadsafe.

        // Isolate out the command name from the argument(s), as the first token.
        StringTokenizer tokenizer = new StringTokenizer(command);
        String cmdLibAndName = tokenizer.nextToken().toUpperCase();
        String libName;
        String cmdName;
        // If there's a slash, parse out the library/commandName.
        int slashPos = cmdLibAndName.indexOf('/');
        if (slashPos == -1)  // No slash.
        {
            libName = "*LIBL";
            cmdName = cmdLibAndName;
        }
        else
        {
            libName = cmdLibAndName.substring(0, slashPos);
            cmdName = cmdLibAndName.substring(slashPos + 1);
        }

        // Fill the commandname array with blanks.
        byte[] commandName = {(byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40};
        // The first 10 characters contain the name of the command.
        converter_.stringToByteArray(cmdName, commandName);
        // The second 10 characters contain the name of the library where the command is located.
        converter_.stringToByteArray(libName, commandName, 10);

        // Set up the parameter list for the program that we will use to retrieve the command information (QCDRCMDI).

        // First parameter:  receiver variable - output - char(*).
        // Second parameter:  length of receiver variable - input - binary(4).
        // Third parameter:  format name - input - char(8).
        // Set to EBCDIC "CMDI0100".
        // Fourth parameter:  qualified command name - input - char(20).
        // Fifth parameter:  error code - input/output - char(*).
        // Eight bytes of zero's indicates to throw exceptions.
        // Send as input because we are not interested in the output.
        // Sixth parameter:  optional - follow proxy chain - input - char(1)							//@A1A
		// Set to 1 - If the specified command is a proxy command, follow the proxy command 			//@A1A
		// chain to the target non-proxy command and retrieve information for the target command. 		//@A1A
		// If the command is not a proxy command, retrieve information for the specified command. 		//@A1A

		int numParms;
		if ((AS400.nativeVRM.vrm_ >= 0x00060100) ||
		   (AS400.nativeVRM.vrm_ >= 0x00050400 && !system_.isMissingPTF())) {
		   numParms = 6;	// @A1C - added support for proxy commands
		}
		else numParms = 5;

		ProgramParameter[] parameterList = new ProgramParameter[numParms];
		parameterList[0] = new ProgramParameter(350);
		parameterList[1] = new ProgramParameter(new byte[] { 0x00, 0x00, 0x01, 0x5e });
		parameterList[2] = new ProgramParameter(new byte[] { (byte) 0xC3, (byte) 0xD4, (byte) 0xC4, (byte) 0xC9, (byte) 0xF0, (byte) 0xF1, (byte) 0xF0, (byte) 0xF0 });
		parameterList[3] = new ProgramParameter(commandName);
		parameterList[4] = new ProgramParameter(new byte[8]);
		if (numParms > 5)											//@A1A
			parameterList[5] = new ProgramParameter(new byte[] { (byte) 0xF1 });		//@A1A

        try
        {
          // Retrieve command information.  Failure is returned as a message list.
          boolean succeeded = runProgramOnThread("QSYS", "QCDRCMDI", parameterList, MESSAGE_OPTION_DEFAULT, true);
          if (!succeeded)
          {
            // If the exception is "MCH0802: Total parameters passed does not match number required" and we're running to V5R4, that means that the user hasn't applied PTF SI29629.  In that case, we will re-issue the program call, minus the new "follow proxy chain" parameter.
            if (numParms > 5 &&
                AS400.nativeVRM.vrm_ < 0x00060100 && AS400.nativeVRM.vrm_ >= 0x00050400 &&
                messageList_[messageList_.length - 1].getID().equals("MCH0802"))
            {
              if (Trace.traceOn_) Trace.log(Trace.WARNING, "PTF SI29629 is not installed: (MCH0802) " + messageList_[messageList_.length - 1].getText());
              // Retain result, to avoid repeated 6-parm attempts for same system object.
              system_.setMissingPTF();
              ProgramParameter[] shorterParmList = new ProgramParameter[5];
              System.arraycopy(parameterList, 0, shorterParmList, 0, 5);
              succeeded = runProgramOnThread("QSYS", "QCDRCMDI", shorterParmList, MESSAGE_OPTION_DEFAULT, true);
            }
            if (!succeeded)
            {
              Trace.log(Trace.ERROR, "Unable to retrieve command information.");
              String id = messageList_[messageList_.length - 1].getID();
              byte[] substitutionBytes = messageList_[messageList_.length - 1].getSubstitutionData();

              // CPF9801 - Object &2 in library &3 not found.
              if (id.equals("CPF9801") && cmdName.equals(converter_.byteArrayToString(substitutionBytes, 0, 10).trim()) && libName.equals(converter_.byteArrayToString(substitutionBytes, 10, 10).trim()) && "CMD".equals(converter_.byteArrayToString(substitutionBytes, 20, 7).trim()))
              {
                Trace.log(Trace.WARNING, "Command not found.");
                return 0;  // If cmd doesn't exist, say it's not threadsafe.
              }
              // CPF9810 - Library &1 not found.
              if (id.equals("CPF9810") && libName.equals(converter_.byteArrayToString(substitutionBytes).trim()))
              {
                Trace.log(Trace.WARNING, "Command library not found.");
                return 0;  // If cmd doesn't exist, say it's not threadsafe.
              }
              else throw new AS400Exception(messageList_);
            }
          }
        }
        catch (ObjectDoesNotExistException e)
        {
            Trace.log(Trace.ERROR, "Unexpected ObjectDoesNotExistException:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }

        // Get the data returned from the program.
        byte[] dataReceived = parameterList[0].getOutputData();
        if (Trace.traceOn_)
        {
            Trace.log(Trace.DIAGNOSTIC, "Command information retrieved:", dataReceived);

            // Examine the "multithreaded job action" field.
            // The "multithreaded job action" field is a single byte at offset 334.
            // Multithreaded job action. The action to take when a command that is not threadsafe is called in a multithreaded job.  The possible values are:
            // 0  Use the action specified in QMLTTHDACN system value.
            // 1  Run the command. Do not send a message.
            // 2  Send an informational message and run the command.
            // 3  Send an escape message, and do not run the command.
            // System value . . . . . :   QMLTTHDACN
            // Description  . . . . . :   Multithreaded job action
            // Interpretation:
            // 1  Perform the function that is not threadsafe without sending a message.
            // 2  Perform the function that is not threadsafe and send an informational message.
            // 3  Do not perform the function that is not threadsafe.
            Trace.log(Trace.DIAGNOSTIC, "Multithreaded job action: " + (dataReceived[334] & 0x0F));
        }

        // Examine the "threadsafe indicator" field.
        // The "threadsafe indicator" field is a single byte at offset 333.
        // Threadsafe indicator:  Whether the command can be used safely in a multithreaded job.
        // The possible values are:
        // 0   The command is not threadsafe and should not be used in a multithreaded job.
        // 1   The command is threadsafe and can be used safely in a multithreaded job.
        // 2   The command is threadsafe under certain conditions.  See the documentation for the command to determine the conditions under which the command can be used safely in a multithreaded job.
        // If the threadsafe indicator is either threadsafe or conditionally threadsafe, the multithreaded job action value will be returned as 1.
        int threadsafeIndicator = dataReceived[333] & 0x0F;
        if (Trace.traceOn_)
        {
          Trace.log(Trace.DIAGNOSTIC, "Threadsafe indicator: " + threadsafeIndicator);
        }
        return threadsafeIndicator;
    }

    // Connects to the server.
    // The ImplNative class overrides this method.
    // @param threadSafety  The assumed thread safety of the command/program.
    protected void open(Boolean threadSafety) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
      // The ImplRemote class only knows how to call commands/programs off-thread.
      openOffThread();
    }

    // Connects to the server.
    protected void openOffThread() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Remote implementation object open.");
      // Connect to server.
      // Note: We can't skip this block if (server_ != null), since the server may have become disconnected since we last used it; in which case it would need to be reconnected.
      server_ = system_.getConnection(AS400.COMMAND, false);

      // Exchange attributes with server job.  (This must be first exchange with server job to complete initialization.)  First check to see if server has already been initialized by another user.
      synchronized (server_)  // Close the window between getting and checking if exchange has been done.
      {
        DataStream baseReply = server_.getExchangeAttrReply();
        if (baseReply == null)
        {
          try
          {
            baseReply = server_.sendExchangeAttrRequest(new RCExchangeAttributesRequestDataStream(system_.getNLV()));
          }
          catch (IOException e)
          {
            system_.disconnectServer(server_);
            Trace.log(Trace.ERROR, "IOException during exchange attributes:", e);
            throw e;
          }

          if (!(baseReply instanceof RCExchangeAttributesReplyDataStream))
          {
            // Unknown data stream.
            Trace.log(Trace.ERROR, "Unknown exchange attributes reply datastream:", baseReply.data_);
            system_.disconnectServer(server_);
            throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
          }

          processReturnCode(((RCExchangeAttributesReplyDataStream)baseReply).getRC());
        }

        RCExchangeAttributesReplyDataStream reply = (RCExchangeAttributesReplyDataStream)baseReply;
        // If converter was not set with an AS400 user override ccsid, set converter to command server job ccsid.
        if (!ccsidIsUserOveride_)
        {
          converter_ = ConverterImplRemote.getConverter(reply.getCCSID(), system_);
        }
        serverDataStreamLevel_ = reply.getDSLevel();
        if (serverDataStreamLevel_ >= 10)
        {
          unicodeConverter_ = ConverterImplRemote.getConverter(1200, system_);
        }
      }
    }

    // The ImplNative class overrides this method.
    protected void openOnThread() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
      // The ImplRemote class only knows how to call commands/programs off-thread.
      openOffThread();
    }


    // This method is reserved for use by other ImplRemote classes in this package.
    // The ImplNative class overrides this method.
    public boolean runCommand(String command) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
      // The ImplRemote class only knows how to call commands/programs off-thread.
      return runCommandOffThread(command, MESSAGE_OPTION_DEFAULT); // defaults
    }


    // @param threadSafety  The assumed thread safety of the command/program.
    // The "threadSafety" parameter is disregarded in the ImplRemote implementation of this method.
    public boolean runCommand(String command, Boolean threadSafety, int messageOption) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
      return runCommandOffThread(command, messageOption);
    }


    // @param threadSafety  The assumed thread safety of the command/program.
    // The "threadSafety" parameter is disregarded in the ImplRemote implementation of this method.
    protected boolean runCommandOffThread(String command, int messageOption) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "Remote implementation running command: " + command);

        // Connect to server.
        openOffThread();

        if (serverDataStreamLevel_ >= 10)
        {
            return runCommandOffThread(unicodeConverter_.stringToByteArray(command), messageOption, 1200);
        }
        return runCommandOffThread(converter_.stringToByteArray(command), messageOption, 0);
    }

    // @param commandAsBytes The command to be executed, as a sequence of EBCDIC bytes
    // @param commandAsString The command to be executed, as a String.  This parameter is used by the ImplNative if it needs to lookup the command's threadsafety.
    public boolean runCommand(byte[] commandAsBytes, String commandAsString) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "Remote implementation running command: " + commandAsString);

      // Since we don't need to lookup threadsafety, we can ignore commandAsString.
      return runCommandOffThread(commandAsBytes, MESSAGE_OPTION_DEFAULT, 0);
    }


    // @param threadSafety  The assumed thread safety of the command/program.
    // The "threadSafety" parameter is disregarded in the ImplRemote implementation of this method.
    public boolean runCommand(byte[] command, Boolean threadSafety, int messageOption) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        return runCommandOffThread(command, messageOption, 0);
    }

    protected boolean runCommandOffThread(byte[] command, int messageOption, int ccsid) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (priorCallWasOnThread_ == ON_THREAD)
        {
          if (Trace.traceOn_) Trace.log(Trace.WARNING, "Prior call was on-thread, but this call is off-thread, so different job.");
        }
        priorCallWasOnThread_ = OFF_THREAD;

        // Connect to server.
        openOffThread();

        try
        {
            // Create and send request.
            DataStream baseReply = server_.sendAndReceive(new RCRunCommandRequestDataStream(command, serverDataStreamLevel_, messageOption, ccsid));

            // Punt if unknown data stream.
            if (!(baseReply instanceof RCRunCommandReplyDataStream))
            {
                Trace.log(Trace.ERROR, "Unknown run command reply datastream:", baseReply.data_);
                system_.disconnectServer(server_);
                throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
            }

            RCRunCommandReplyDataStream reply = (RCRunCommandReplyDataStream)baseReply;

            // Get info from reply.
            messageList_ = reply.getMessageList(converter_);
            int rc = reply.getRC();
            processReturnCode(rc);

            return rc == 0;
        }
        catch (IOException e)
        {
            system_.disconnectServer(server_);
            Trace.log(Trace.ERROR, "Lost connection to remote command server:", e);
            throw e;
        }
    }

    // The ImplNative class overrides this method.
    public boolean runProgram(String library, String name, ProgramParameter[] parameterList) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
      // The ImplRemote class only knows how to call commands/programs off-thread.
      return runProgramOffThread(library, name, parameterList, MESSAGE_OPTION_DEFAULT);
    }

    // The ImplNative class overrides this method.
    public boolean runProgram(String library, String name, ProgramParameter[] parameterList, Boolean threadSafety, int messageOption) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
      // The ImplRemote class only knows how to call commands/programs off-thread.
      return runProgramOffThread(library, name, parameterList, messageOption);
    }

    // The ImplNative class overrides this method.
    protected boolean runProgramOnThread(String library, String name, ProgramParameter[] parameterList, int messageOption, boolean currentlyOpeningOnThisThread) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
      // The ImplRemote class only knows how to call commands/programs off-thread.
      return runProgramOffThread(library, name, parameterList, messageOption);
    }

    protected boolean runProgramOffThread(String library, String name, ProgramParameter[] parameterList, int messageOption) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "Remote implementation running program: " + library + "/" + name);
        if (priorCallWasOnThread_ == ON_THREAD)
        {
          if (Trace.traceOn_) Trace.log(Trace.WARNING, "Prior call was on-thread, but this call is off-thread, so different job.");
        }
        priorCallWasOnThread_ = OFF_THREAD;

        // Connect to server.
        openOffThread();

        // Run program on server
        try
        {
            // Create and send request.
            DataStream baseReply = server_.sendAndReceive(new RCCallProgramRequestDataStream(library, name, parameterList, converter_, serverDataStreamLevel_, messageOption));

            // Punt if unknown data stream.
            if (!(baseReply instanceof RCCallProgramReplyDataStream))
            {
                Trace.log(Trace.ERROR, "Unknown run program reply datastream ", baseReply.data_);
                system_.disconnectServer(server_);
                throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
            }

            RCCallProgramReplyDataStream reply = (RCCallProgramReplyDataStream)baseReply;

            // Check for error code returned.
            int rc = reply.getRC();
            processReturnCode(rc);
            if (rc == 0)
            {
                // Set the output data into parameter list.
                reply.getParameterList(parameterList);
                messageList_ = new AS400Message[0];
                return true;
            }
            messageList_ = reply.getMessageList(converter_);
            if (rc == 0x0500 && messageList_.length != 0)
            {
                String id = messageList_[messageList_.length - 1].getID();

                if (id.equals("MCH3401"))
                {
                    byte[] substitutionBytes = messageList_[messageList_.length - 1].getSubstitutionData();
                    if (substitutionBytes[0] == 0x02 && substitutionBytes[1] == 0x01 && name.equals(converter_.byteArrayToString(substitutionBytes, 2, 30).trim()))
                    {
                        throw new ObjectDoesNotExistException(QSYSObjectPathName.toPath(library, name, "PGM"), ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
                    }
                    if (substitutionBytes[0] == 0x04 && substitutionBytes[1] == 0x01 && library.equals(converter_.byteArrayToString(substitutionBytes, 2, 30).trim()))
                    {
                        throw new ObjectDoesNotExistException(QSYSObjectPathName.toPath(library, name, "PGM"), ObjectDoesNotExistException.LIBRARY_DOES_NOT_EXIST);
                    }
                }
            }
            return false;
        }
        catch (IOException e)
        {
            system_.disconnectServer(server_);
            Trace.log(Trace.ERROR, "Lost connection to remote command server:", e);
            throw e;
        }
    }

    public byte[] runServiceProgram(String library, String name, String procedureName, int returnValueFormat, ProgramParameter[] serviceParameterList, Boolean threadSafety, int procedureNameCCSID, int messageOption) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "Remote implementation running service program: " + library + "/" + name + " procedure name: " + procedureName);

        // Connect to server.
        open(threadSafety);

        // Set up the parameter list for the program that we will use to call the service program (QZRUCLSP).
        ProgramParameter[] programParameterList = new ProgramParameter[7 + serviceParameterList.length];

        // First parameter:  qualified service program name - input - char(20).
        byte[] serviceProgramBytes = new byte[20];
        // Blank fill service program name and library name.
        for (int i = 0; i < 20; ++i)
        {
            serviceProgramBytes[i] = (byte)0x40;
        }
        converter_.stringToByteArray(name, serviceProgramBytes, 0);
        converter_.stringToByteArray(library, serviceProgramBytes, 10);
        programParameterList[0] = new ProgramParameter(serviceProgramBytes);

        // Second parameter:  export name - input - char(*).
        ConverterImplRemote procedureNameConverter = ConverterImplRemote.getConverter(procedureNameCCSID, system_);
        byte[] procedureNameBytes = procedureNameConverter.stringToByteArray(procedureName);
        byte[] procedureNameBytesNullTerminated = new byte[procedureNameBytes.length + 1];
        System.arraycopy(procedureNameBytes, 0, procedureNameBytesNullTerminated, 0, procedureNameBytes.length);
        programParameterList[1] = new ProgramParameter(procedureNameBytesNullTerminated);

        // Third parameter:  return value format - input - binary(4).
        byte[] returnValueFormatBytes = new byte[4];
        BinaryConverter.intToByteArray(returnValueFormat, returnValueFormatBytes, 0);
        programParameterList[2] = new ProgramParameter(returnValueFormatBytes);

        // Fourth parameter:  parameter formats - input - array(*) of binary(4).
        byte[] parameterFormatBytes = new byte[serviceParameterList.length * 4];
        for (int i = 0; i < serviceParameterList.length; ++i)
        {
            int parameterType = serviceParameterList[i].getParameterType();
            if (serverDataStreamLevel_ < 6 && serviceParameterList[i].getUsage() == ProgramParameter.NULL)
            {
                // Server does not allow null parameters.
                parameterType = ProgramParameter.PASS_BY_VALUE;
            }
            BinaryConverter.intToByteArray(parameterType, parameterFormatBytes, i * 4);
        }
        programParameterList[3] = new ProgramParameter(parameterFormatBytes);

        // Fifth parameter:  number of parameters - input - binary(4).
        byte[] parameterNumberBytes = new byte[4];
        BinaryConverter.intToByteArray(serviceParameterList.length, parameterNumberBytes, 0);
        programParameterList[4] = new ProgramParameter(parameterNumberBytes);

        // Sixth parameter:  error code - input/output - char(*).
        // Eight bytes of zero's indicates to throw exceptions.
        // Send as input because we are not interested in the output.
        programParameterList[5] = new ProgramParameter(new byte[8]);

        // Seventh parameter:  return value - output - char(*).
        // Define the return value length, even though the service program returns void the API middle-man we call (QZRUCLSP) still returns four bytes.  If we don't get this right the output buffers will be off by four bytes corrupting data.
        programParameterList[6] = new ProgramParameter((returnValueFormat == ServiceProgramCall.NO_RETURN_VALUE) ? 4 : 8);

        // Combines the newly created programParameterList with the value of serviceParameterList input by user to form the perfect parameter list that will be needed in the method runProgram.
        System.arraycopy(serviceParameterList, 0, programParameterList, 7, serviceParameterList.length);

        // Note: Depending upon whether the program represented by this ProgramCall object will be run on-thread or through the host servers, we will issue the service program call request accordingly, either on-thread or through the host servers.
        // Design note: The QZRUCLSP API itself is not documented to be threadsafe.
        boolean succeeded = runProgram("QSYS", "QZRUCLSP", programParameterList, threadSafety, messageOption);
        if (!succeeded)
        {
            return null;
        }
        return programParameterList[6].getOutputData();
    }

    // Processes the return code received from the server and throws the appropriate exception.
    // @param  rc  The server return code.
    // @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
    private void processReturnCode(int rc) throws ErrorCompletingRequestException
    {
        if (Trace.traceOn_)
        {
            byte[] rcBytes = new byte[2];
            BinaryConverter.unsignedShortToByteArray(rc, rcBytes, 0);
            Trace.log(Trace.DIAGNOSTIC, "Remote command server return code:", rcBytes);
        }
        switch (rc)
        {
            // The following is the list of return codes the RMTCMD/RMTPGMCALL server sends to the client application in the request replies:
            case 0x0000:  // Request processed successfully.
                if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "Request processed successfully.");
                return;

            // Initial allocate & exchange attribute return codes:
            case 0x0100:  // Limited user.
                if (Trace.traceOn_) Trace.log(Trace.WARNING, "Limited user.");
                return;
            case 0x0101:  // Invalid exchange attributes request.
                Trace.log(Trace.ERROR, "Exchange attributes request not valid.");
                system_.disconnectServer(server_);
                throw new InternalErrorException(InternalErrorException.SYNTAX_ERROR);
            case 0x0102:  // Invalid datastream level.
                Trace.log(Trace.ERROR, "Datastream level not valid.");
                system_.disconnectServer(server_);
                throw new InternalErrorException(InternalErrorException.DATA_STREAM_LEVEL_NOT_VALID);
            case 0x0103:  // Invalid version.
                Trace.log(Trace.ERROR, "Version not valid.");
                system_.disconnectServer(server_);
                throw new InternalErrorException(InternalErrorException.VRM_NOT_VALID);

            case 0x0104:  // Invalid CCSID.
                if (Trace.traceOn_) Trace.log(Trace.WARNING, "CCSID not valid.");
                return;
            case 0x0105:  // Invalid NLV, default to primary NLV:  NLV must consist of the characters 0-9.
                if (Trace.traceOn_) Trace.log(Trace.WARNING, "NLV not valid");
                return;
            case 0x0106:  // NLV not installed, default to primary NLV:  The NLV may not be supported or it may not be installed on the system.
                if (Trace.traceOn_) Trace.log(Trace.WARNING, "NLV not installed.");
                return;
            case 0x0107:  // Error retrieving product information.  Can't validate NLV.
                if (Trace.traceOn_) Trace.log(Trace.WARNING, "Error retrieving product information, cannot validate NLV.");
                return;
            case 0x0108:  // Error trying to add NLV library to system library list:  One possible reason for failure is the user may not be authorized to CHGSYSLIBL command.
                if (Trace.traceOn_) Trace.log(Trace.WARNING, "Error adding NLV library to system library list.");
                return;

            // Return codes for all requests:  These are return codes that can result from processing any type of requests (exchange attributes, RMTCMD, RMTPGMCALL, & end).
            case 0x0200:  // Unable to process request.  An error occured on the receive data.
            case 0x0201:  // Invalid LL.
            case 0x0202:  // Invalid server ID.
            case 0x0203:  // Incomplete data.
            case 0x0205:  // Invalid request ID.
                Trace.log(Trace.ERROR, "Datastream not valid.");
                system_.disconnectServer(server_);
                throw new InternalErrorException(InternalErrorException.SYNTAX_ERROR);
            case 0x0204:  // Host resource error.
                Trace.log(Trace.ERROR, "Host Resource error.");
                system_.disconnectServer(server_);
                throw new ErrorCompletingRequestException(ErrorCompletingRequestException.AS400_ERROR);

            // Return codes common to RMTCMD & RMTPGMCALL requests:
            case 0x0300:  // Process exit point error.  Error occurred when trying to retrieve the exit point for user exit program processing.  This can occur when the user exit program cannot be resolved.
                Trace.log(Trace.ERROR, "Process exit point error.");
                system_.disconnectServer(server_);
                throw new ErrorCompletingRequestException(ErrorCompletingRequestException.EXIT_POINT_PROCESSING_ERROR);
            case 0x0301:  // Invalid request.  The request data stream did not match what was required for the specified request.
            case 0x0302:  // Invalid parameter.
                Trace.log(Trace.ERROR, "Request not valid.");
                system_.disconnectServer(server_);
                throw new InternalErrorException(InternalErrorException.SYNTAX_ERROR);
            case 0x0303:  // Maximum exceeded.  For RMTCMD, the maximum command length was exceeded and for RMTPGMCALL, the maximum number of parameters was exceeded.
                Trace.log(Trace.ERROR, "Maximum exceeded.");
                throw new ErrorCompletingRequestException(ErrorCompletingRequestException.LENGTH_NOT_VALID);
            case 0x0304:  // An error occured when calling the user exit program.
                Trace.log(Trace.ERROR, "Error calling exit program.");
                system_.disconnectServer(server_);
                throw new ErrorCompletingRequestException(ErrorCompletingRequestException.EXIT_PROGRAM_CALL_ERROR);
            case 0x0305:  // User exit program denied the request.
                Trace.log(Trace.ERROR, "Exit program denied request.");
                system_.disconnectServer(server_);
                throw new ErrorCompletingRequestException(ErrorCompletingRequestException.EXIT_PROGRAM_DENIED_REQUEST);

            // RMTCMD specific return codes:
            case 0x0400:  // Command failed.  Messages returned.
                if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "Error calling the command.");
                return;
            case 0x0401:  // Invalid CCSID value.
                Trace.log(Trace.ERROR, "CCSID not valid.");
                throw new InternalErrorException(InternalErrorException.SYNTAX_ERROR);

            // RMTPGMCALL specific return codes:
            case 0x0500:  // An error occured when resolving to the program to call.
                if (Trace.traceOn_) Trace.log(Trace.ERROR, "Could not resolve program.");
                return;
            case 0x0501:  // An error occured when calling the program.
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Error calling the program.");
                return;

            default:
                Trace.log(Trace.ERROR, "Return code unknown.");
                system_.disconnectServer(server_);
                throw new InternalErrorException(InternalErrorException.UNEXPECTED_RETURN_CODE);
        }
    }

    static AS400Message[] parseMessages(byte[] data, ConverterImplRemote converter) throws IOException
    {
        int messageNumber = BinaryConverter.byteArrayToUnsignedShort(data, 22);
        AS400Message[] messageList = new AS400Message[messageNumber];

        for (int offset = 24, i = 0; i < messageNumber; ++i)
        {
            if (data[offset + 5] == 0x06)
            {
                messageList[i] = AS400ImplRemote.parseMessage(data, offset + 6, converter);
            }
            else
            {
                messageList[i] = new AS400Message();
                messageList[i].setID(converter.byteArrayToString(data, offset + 6, 7));
                messageList[i].setType((data[offset + 13] & 0x0F) * 10 + (data[offset + 14] & 0x0F));
                messageList[i].setSeverity(BinaryConverter.byteArrayToUnsignedShort(data, offset + 15));
                messageList[i].setFileName(converter.byteArrayToString(data, offset + 17, 10).trim());
                messageList[i].setLibraryName(converter.byteArrayToString(data, offset + 27, 10).trim());

                int substitutionDataLength = BinaryConverter.byteArrayToUnsignedShort(data, offset + 37);
                int textLength = BinaryConverter.byteArrayToUnsignedShort(data, offset + 39);

                byte[] substitutionData = new byte[substitutionDataLength];
                System.arraycopy(data, offset + 41, substitutionData, 0, substitutionDataLength);
                messageList[i].setSubstitutionData(substitutionData);

                messageList[i].setText(converter.byteArrayToString(data, offset + 41 + substitutionDataLength, textLength));
            }
            offset += BinaryConverter.byteArrayToInt(data, offset);
        }

        return messageList;
    }
}
