///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  RemoteCommandImplRemote.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2000-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

// The RemoteCommandImplRemote class is the remote implementation of CommandCall and ProgramCall.
class RemoteCommandImplRemote implements RemoteCommandImpl
{
    private static final String copyright = "Copyright (C) 2000-2003 International Business Machines Corporation and others.";

    AS400ImplRemote system_;
    ConverterImplRemote converter_;
    boolean ccsidIsUserOveride_ = false;  // Flag to say don't override ccsid in open().
    private AS400Server server_;
    AS400Message[] messageList_ = new AS400Message[0];
    int serverDataStreamLevel_ = 0;

    static
    {
        // Identify all remote command server reply data streams.
        AS400Server.addReplyStream(new RCExchangeAttributesReplyDataStream(), AS400.COMMAND);
        AS400Server.addReplyStream(new RCRunCommandReplyDataStream(), AS400.COMMAND);
        AS400Server.addReplyStream(new RCCallProgramReplyDataStream(), AS400.COMMAND);
        AS400Server.addReplyStream(new RCCallProgramFailureReplyDataStream(), AS400.COMMAND);
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
    // @return  Information about the job in which the command/program would be run.  This is a String consisting of a 10-character simple job name, a 10-character user name, and a 6-character job number.
    public String getJobInfo(boolean threadSafety) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting job infomation from implementation object.");
        // Connect to server.
        open(threadSafety);

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
        // Design note: QWCRTVCA is documented to be threadsafe.

        // Note: Depending upon whether the program represented by this ProgramCall object will be run on-thread or through the host servers, we will issue the job info query accordingly, either on-thread or through the host servers, in order to get the appropriate Job.

        // Retrieve Current Attributes.  Failure is returned as a message list.
        try
        {
            if (!runProgram("QSYS", "QWCRTVCA", parameterList, threadSafety, AS400Message.MESSAGE_OPTION_UP_TO_10))
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

    // Indicates whether or not the command will be considered thread-safe.
    // @return  This method always returns false for this class.
    public boolean isCommandThreadSafe(String command) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Remote implementation object returns false for command thread safety.");
        return false;  // Only the ImplNative will ever return true.
    }

    // Connects to the server.
    protected void open(boolean threadSafety) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Remote implementation object open.");
        // Connect to server.
        server_ = system_.getConnection(AS400.COMMAND, false);

        // Exchange attributes with server job.  (This must be first exchange with server job to complete initialization.)  First check to see if server has already been initialized by another user.
        synchronized (server_)  // Close the window between getting and checking if exchange has been done.
        {
            DataStream baseReply = server_.getExchangeAttrReply();
            if (baseReply == null)
            {
                try
                {
                    baseReply = server_.sendExchangeAttrRequest(new RCExchangeAttributesRequestDataStream(ExecutionEnvironment.getNlv(system_.getLocale())));
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
        }
    }

    public boolean runCommand(String command, boolean threadSafety, int messageCount) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "Remote implementation running command: " + command);

        // Connect to server.
        open(threadSafety);

        byte[] data = converter_.stringToByteArray(command);

        return runCommand(data, messageCount);
    }

    public boolean runCommand(byte[] command, boolean threadSafety, int messageCount) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "Remote implementation running command:", command);

        // Connect to server.
        open(threadSafety);

        return runCommand(command, messageCount);
    }

    private boolean runCommand(byte[] command, int messageCount) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        try
        {
            // Create and send request.
            DataStream baseReply = server_.sendAndReceive(new RCRunCommandRequestDataStream(command, serverDataStreamLevel_, messageCount));

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

    public boolean runProgram(String library, String name, ProgramParameter[] parameterList, boolean threadSafety, int messageCount) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "Remote implementation running program: " + library + "/" + name);

        // Connect to server
        open(threadSafety);

        // Run program on server
        try
        {
            // Create and send request.
            DataStream baseReply = server_.sendAndReceive(new RCCallProgramRequestDataStream(library, name, parameterList, converter_, serverDataStreamLevel_, messageCount));

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
            if (rc == 0x0500)
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

    public byte[] runServiceProgram(String library, String name, String procedureName, int returnValueFormat, ProgramParameter[] serviceParameterList, boolean threadSafety, int procedureNameCCSID, int messageCount) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
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
        if (!runProgram("QSYS", "QZRUCLSP", programParameterList, threadSafety, messageCount))
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
                throw new InternalErrorException(InternalErrorException.SYNTAX_ERROR );
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

    static AS400Message[] parseMessages(byte[] data, ConverterImplRemote converter)
    {
        int messageNumber = BinaryConverter.byteArrayToUnsignedShort(data, 22);
        AS400Message[] messageList = new AS400Message[messageNumber];

        for (int offset = 24, i = 0; i < messageNumber; ++i)
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

            offset += BinaryConverter.byteArrayToInt(data, offset);
        }

        return messageList;
    }
}
