///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RemoteCommandImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

// The RemoteCommandImplRemote class is the remote implementation of CommandCall and ProgramCall.
class RemoteCommandImplRemote implements RemoteCommandImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    AS400ImplRemote system_;
    ConverterImplRemote converter_;
    boolean ccsidIsUserOveride_ = false;  // Flag to say don't override ccsid in open().
    private AS400Server server_;
    AS400Message[] messageList_  = new AS400Message[0];
    private boolean zeroSuppression_ = false;
    private boolean rleCompression_ = false;

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
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Setting up remote command implementation object.");
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
        // Connect to server.
        open(threadSafety);

        // Set up the parameter list for the program that we will use to get the Job information (QWCRTVCA).

        // Format for Retrieve Current Attributes (QWCRTVCA) API:
        // 1 - Receiver variable - Output - Char(*)
        // 2 - Length of receiver variable - Input - Binary(4)
        // 3 - Format name - Input - Char(8)
        // 4 - Number of attributes to return - Input - Binary(4)
        // 5 - Key of attributes to be returned - Input - Array(*) of Binary(4)
        // 6 - Error code - I/O - Char(*)

        //  Relevant Key Attributes for QWCRTVCA API:
        //  ______ ___________ ______________
        // | Key  | Type      | Description  |
        // |______|___________|______________|
        // | 1009 | CHAR(26)  | Job name     | Values shown in decimal.
        // |______|___________|______________|

        ProgramParameter[] parameterList = new ProgramParameter[6];

        // First parameter: output, the receiver variable.
        byte[] dataReceived = new byte[46]; // RTVC0100's 20-byte header, plus 26 bytes for "Job name" field.
        parameterList[0] = new ProgramParameter(dataReceived.length);

        // Second parameter: input, length of receiver variable.
        byte[] receiverLength = BinaryConverter.intToByteArray(dataReceived.length);
        parameterList[1] = new ProgramParameter(receiverLength);

        // Third parameter: input, format name.
        byte[] formatName = new byte[8];
        converter_.stringToByteArray("RTVC0100", formatName);
        parameterList[2] = new ProgramParameter(formatName);

        // Fourth parameter: input, number of attributes to return.
        byte[] numAttributes = BinaryConverter.intToByteArray(1); // 1 attribute
        parameterList[3] = new ProgramParameter(numAttributes);

        // Fifth parameter: input, key(s) of attribute(s) to return.
        byte[] attributeKey = BinaryConverter.intToByteArray(1009); // "Job name"
        parameterList[4] = new ProgramParameter(attributeKey);

        // Sixth parameter: input/output, error code.

        // In format ERRC0100, the first field in the structure, "bytes provided", is a 4-byte INPUT field; it controls whether an exception is returned to the application, or the error code structure is filled in with the exception information.
        // When the "bytes provided" field is zero, all other fields are ignored and an exception is returned (rather than the error code structure getting filled in).
        byte[] errorCode = new byte[17];  // Format ERRC0100
        BinaryConverter.intToByteArray(0, errorCode, 0); // return exception if error
        parameterList[5] = new ProgramParameter(errorCode, 17);

        // Prepare to call the "Retrieve Current Attributes" API.
        // Design note: QWCRTVCA is documented to be threadsafe.

        // Note: Depending upon whether the program represented by this ProgramCall object will be run on-thread or through the host servers, we will issue the job info query accordingly, either on-thread or through the host servers, in order to get the appropriate Job.

        // Retrieve Current Attributes.  Failure is returned as a message list.
        try
        {
            if(!runProgram("QSYS", "QWCRTVCA", parameterList, threadSafety))
            {
                Trace.log(Trace.ERROR, "Unable to retrieve job information.");
                // Trace the messages.
                for (int i = 0; i < messageList_.length; ++i)
                {
                    Trace.log(Trace.ERROR, messageList_[i].toString());
                }
                throw new AS400Exception(messageList_[0]);
            }
        }
        catch (ObjectDoesNotExistException e)
        {
            Trace.log(Trace.ERROR, "Unexpected ObjectDoesNotExistException:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }

        // Get the data returned from the program.
        dataReceived = parameterList[0].getOutputData();
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Job information retrieved:", dataReceived);

        // Format of RTVC0100 structure returned from QWCRTVCA API:

        // Offset
        // __________
        //  0        | BINARY(4)  | Number of attributes returned
        // __________|____________|______________
        // These     | BINARY(4)  | Length of attribute information returned
        // fields    |____________|______________
        // repeat,   | BINARY(4)  | Key
        // in the    |____________|______________
        // order     | CHAR(1)    | Type of data
        // listed,   |____________|______________
        // for each  | CHAR(3)    | Reserved
        // key       |____________|______________
        // requested.| BINARY(4)  | Length of data
        //           |____________|______________
        //           | CHAR(*)    | Data
        //           |____________|______________
        //           | CHAR(*)    | Reserved
        // __________|____________|______________

        // Verify that one attribute was returned. Assume that if exactly one was returned, it's the one we asked for.
        int numAttributesReturned = BinaryConverter.byteArrayToInt(dataReceived, 0);
        if (numAttributesReturned != 1)
        {
            Trace.log(Trace.ERROR, "Unexpected number of job attributes retrieved:", numAttributesReturned);
            return null;
        }
        // Examine the "Job name" field.  26 bytes starting at offset 20.  Parse out the job name, user name, and job number.
        // Contents of returned "Job name" field:
        // The name of the user job that this thread is associated with.
        // The format of the job name is a 10-character simple job name,
        // a 10-character user name, and a 6-character job number.
        return converter_.byteArrayToString(dataReceived, 20, 26);
    }

    // Return message list to public object.
    public AS400Message[] getMessageList()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting message list from implementation object.");
        return messageList_;
    }

    // Indicates whether or not the AS/400 command will be considered thread-safe.
    // @return  This method always returns false for this class.
    public boolean isCommandThreadSafe(String command) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        return false;  // Only the ImplNative will ever return true.
    }

    // Connects to the AS/400.
    void open(boolean threadSafety) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
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
                    baseReply = server_.sendExchangeAttrRequest(new RCExchangeAttributesRequestDataStream());
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
                    system_.disconnectServer(server_);
                    Trace.log(Trace.ERROR, "Unknown exchange attributes reply datastream:", baseReply.data_);
                    throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
                }

                // The request completed OK.
                int rc = ((RCExchangeAttributesReplyDataStream)baseReply).getRC();
                if (rc != 0 /* not OK */ && rc != 0x0100 /* not limited capability */ && (rc < 0x0106  || rc > 0x0108)/* and not NLV warning */)
                {
                    system_.disconnectServer(server_);
                    processReturnCode(rc, messageList_);
                    byte[] rcBytes = new byte[2];
                    BinaryConverter.unsignedShortToByteArray(rc, rcBytes, 0);
                    Trace.log(Trace.ERROR, "Unexpected return code on exchange attributes:", rcBytes);
                    throw new InternalErrorException(InternalErrorException.SYNTAX_ERROR);
                }
            }

            RCExchangeAttributesReplyDataStream reply = (RCExchangeAttributesReplyDataStream)baseReply;
            // If converter was not set with an AS400 user override ccsid or set on previous open(), set converter to command server job ccsid.
            if (!ccsidIsUserOveride_)
            {
                converter_ = ConverterImplRemote.getConverter(reply.getCCSID(), system_);
            }
            int dataStreamLevel = reply.getDSLevel();
            // If DS level allows, turn zero supression on.
            if (dataStreamLevel > 1)
            {
                zeroSuppression_ = true;
            }
            if (dataStreamLevel > 2)  // If DS level allows, turn RLE compression on.
            {
                rleCompression_ = true;
            }
        }
    }

    // @d2c moved common code to runCommandCommon
    public boolean runCommand(String command, boolean threadSafety) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        Trace.log(Trace.INFORMATION, "Running command: " + command);

        // Connect to server.
        open(threadSafety);

        byte[] data = converter_.stringToByteArray(command);

        return runCommandCommon(data);
    }


    // @d2a new method
    public boolean runCommand(byte[] command, boolean threadSafety) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        Trace.log(Trace.INFORMATION, "Running command:", command);

        // Connect to server.
        open(threadSafety);

        return runCommandCommon(command);
    }


    // @D2 new method (most of the code was in runCommand(String, boolean)
    public boolean runCommandCommon(byte[] command) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        try
        {
            // Create and send request.
            DataStream baseReply = server_.sendAndReceive(new RCRunCommandRequestDataStream(command));

            // Punt if unknown data stream.
            if (!(baseReply instanceof RCRunCommandReplyDataStream))
            {
                Trace.log(Trace.ERROR, "Unknown run command reply datastream:", baseReply.data_);
                throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
            }

            RCRunCommandReplyDataStream reply = (RCRunCommandReplyDataStream)baseReply;

            // Get info from reply.
            messageList_ = reply.getMessageList(converter_);
            int rc = reply.getRC();

            // Check for error code returned
            if (rc != 0 && rc != 0x400)
            {
                processReturnCode(rc, messageList_);
            }

            if (rc == 0)
            {
                return true;
            }
            else
            {
                // rc==0x400 Command Failed, Messages returned.
                Trace.log(Trace.WARNING, "Command failed:", command);
                return false;
            }
        }
        catch (IOException e)
        {
            system_.disconnectServer(server_);
            Trace.log(Trace.ERROR, "Lost connection to remote command server:", e);
            throw e;
        }
    }





    public boolean runProgram(String library, String name, ProgramParameter[] parameterList, boolean threadSafety) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        // Assuming parameter validation has alread occured.
        Trace.log(Trace.INFORMATION, "Running program: " + library + "/" + name);

        // Connect to server
        open(threadSafety);

        // Run program on server
        try
        {
            // Create and send request.
            DataStream baseReply = server_.sendAndReceive(new RCCallProgramRequestDataStream(library, name, parameterList, converter_, zeroSuppression_, rleCompression_));

            // Punt if unknown data stream.
            if (!(baseReply instanceof RCCallProgramReplyDataStream))
            {
                Trace.log(Trace.ERROR, "Unknown run program reply datastream ", baseReply.data_);
                throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
            }

            RCCallProgramReplyDataStream reply = (RCCallProgramReplyDataStream)baseReply;

            // Check for error code returned.
            int rc = reply.getRC();
            if (rc != 0)
            {
                messageList_ = reply.getMessageList(converter_);
                if (rc == 0x0500)
                {
                    if (messageList_.length > 0)
                    {
                        Trace.log(Trace.ERROR, "Object does not exist: " + messageList_[0].getID() + " " + messageList_[0].getText());
                    }
                    else
                    {
                        Trace.log( Trace.ERROR, "Could not resolve program, no message returned.");
                    }
                    throw new ObjectDoesNotExistException(QSYSObjectPathName.toPath(library, name, "PGM"), ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
                }
                processReturnCode(rc, messageList_);
                return false;
            }

            // Set the output data into parameter list.
            reply.getParameterList(parameterList);
            return true;
        }
        catch (IOException e)
        {
            system_.disconnectServer(server_);
            Trace.log(Trace.ERROR, "Lost connection to remote command server:", e);
            throw e;
        }
    }

    public Object[] runServiceProgram(String library,
                                      String name,
                                      String procedureName,
                                      int returnValueFormat,
                                      ProgramParameter[] serviceParameterList,
                                      boolean threadSafety,
                                      int procedureNameCCSID)                    //@D9c
                    throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.INFORMATION, "Running service program: " + library + "/" + name + " procedure name: " + procedureName);

        // Connect to server.
        open(threadSafety);

        ProgramParameter[] programParameterList = new ProgramParameter[7 + serviceParameterList.length];

        byte[] serviceProgramBytes = new byte[20];
        // Blank fill service program name and library name.
        for (int i = 0; i < 20; ++i)
        {
            serviceProgramBytes[i] = (byte)0x40;
        }
        converter_.stringToByteArray(name, serviceProgramBytes, 0);
        converter_.stringToByteArray(library, serviceProgramBytes, 10);

        programParameterList[0] = new ProgramParameter(serviceProgramBytes);


        ConverterImplRemote pnConverter_ = ConverterImplRemote.getConverter(procedureNameCCSID, system_);             // @D9a
        byte[] procedureNameBytes = pnConverter_.stringToByteArray(procedureName);                                    // @D9c
        byte[] procedureNameBytesNullTerminated = new byte[procedureNameBytes.length + 1];
        System.arraycopy(procedureNameBytes, 0, procedureNameBytesNullTerminated, 0, procedureNameBytes.length);

        programParameterList[1] = new ProgramParameter(procedureNameBytesNullTerminated);

        byte[] returnValueFormatBytes = new byte[4];
        BinaryConverter.intToByteArray(returnValueFormat, returnValueFormatBytes, 0);
        programParameterList[2] = new ProgramParameter(returnValueFormatBytes);

        byte[] parameterFormatBytes = new byte[serviceParameterList.length * 4];
        for (int i = 0; i < serviceParameterList.length; ++i)
        {
            BinaryConverter.intToByteArray(serviceParameterList[i].getParameterType(), parameterFormatBytes, i * 4);
        }

        programParameterList[3] = new ProgramParameter(parameterFormatBytes);

        byte[] parameterNumberBytes = new byte[4];
        BinaryConverter.intToByteArray(serviceParameterList.length, parameterNumberBytes, 0);
        programParameterList[4] = new ProgramParameter(parameterNumberBytes);

        programParameterList[5] = new ProgramParameter(new byte[4]);

        // Define the return value length, even though the service program returns void the API middle-man we call (QZRUCLSP) still returns four bytes.  If we don't get this right the output buffers will be off by four bytes corrupting data.
        programParameterList[6] = new ProgramParameter((returnValueFormat == ServiceProgramCall.NO_RETURN_VALUE) ? 4 : 8);

        // Combines the newly created programParameterList with the value of serviceParameterList input by user to form the perfect parameter list that will be needed in the method setProgram.
        System.arraycopy(serviceParameterList, 0, programParameterList, 7, serviceParameterList.length);

        // Note: Depending upon whether the program represented by this ProgramCall object will be run on-thread or through the host servers, we will issue the service program call request accordingly, either on-thread or through the host servers.
        Object[] spc = new Object[2];    // Array used to return objects to caller.
        if (runProgram("QSYS", "QZRUCLSP", programParameterList, threadSafety) != true)
        {
            spc[0] = "false";
            return spc;
        }
        else
        {
            // Reset the message list.
            messageList_ = new AS400Message[0];
            spc[0] = "true";
            spc[1] = programParameterList[6].getOutputData();
            return spc;
        }
    }

    // Processes the return code received from the server and throws the appropriate exception.
    // @param  rc  The server return code.
    // @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
    static void processReturnCode(int rc, AS400Message[] msglist) throws ErrorCompletingRequestException
    {
        switch (rc)
        {
            // The following is the list of return codes the RMTCMD/RMTPGMCALL server sends to the client application in the request replies:
            case 0x0000:  // Request processed successfully.
                Trace.log(Trace.INFORMATION, "Request processed successfully.");
                return;

            // Initial allocate & exchange attribute return codes:
            case 0x0100:  // Limited user.
                Trace.log(Trace.WARNING, "Limited user.");
                return;
            case 0x0101:  // Invalid exchange attributes request.
                Trace.log(Trace.ERROR, "Invalid exchange attributes request.");
                throw new InternalErrorException(InternalErrorException.SYNTAX_ERROR);
            case 0x0102:  // Invalid datastream level.
                Trace.log(Trace.ERROR, "Invalid datastream level.");
                throw new InternalErrorException(InternalErrorException.DATA_STREAM_LEVEL_NOT_VALID);
            case 0x0103:  // Invalid version.
                Trace.log(Trace.ERROR, "Invalid version.");
                throw new InternalErrorException(InternalErrorException.VRM_NOT_VALID);

            case 0x0104:  // Invalid CCSID.
                Trace.log(Trace.WARNING, "Invalid CCSID.");
                return;
            case 0x0105:  // Invalid NLV, default to primary NLV:  NLV must consist of the characters 0-9.
                Trace.log(Trace.WARNING, "NLV not valid");
                return;
            case 0x0106:  // NLV not installed, default to primary NLV:  The NLV may not be supported or it may not be installed on the system.
                Trace.log(Trace.WARNING, "NLV not installed");
                return;
            case 0x0107:  // Error retrieving product information.  Can't validate NLV.
                Trace.log(Trace.WARNING, "error retrieving product info, cannot validate NLV");
                return;
            case 0x0108:  // Error trying to add NLV library to system library list:  One possible reason for failure is the user may not be authorized to CHGSYSLIBL command.
                Trace.log(Trace.WARNING, "Error adding NLV library to system lib list");
                return;

            // Return codes for all requests:  These are return codes that can result from processing any type of requests (exchange attributes, RMTCMD, RMTPGMCALL, & end).
            case 0x0200:  // Unable to process request.  An error occured on the receive data.
            case 0x0201:  // Invalid LL.
            case 0x0202:  // Invalid server ID.
            case 0x0203:  // Incomplete data.
            case 0x0205:  // Invalid request ID.
                Trace.log(Trace.ERROR, "Datastream not valid " + (rc>>8) + "," + (rc&0xff));
                throw new InternalErrorException(InternalErrorException.SYNTAX_ERROR );
            case 0x0204:  // Host resource error.
                Trace.log(Trace.ERROR, "Host Resource error");
                throw new InternalErrorException(InternalErrorException.UNKNOWN);

            // Return codes common to RMTCMD & RMTPGMCALL requests.
            case 0x0300:  // Process exit point error.  Error occurred when trying to retrieve the exit point for user exit program processing.  This can occur when the user exit program cannot be resolved.
                Trace.log(Trace.ERROR, "Process exit point error");
                throw new ErrorCompletingRequestException(ErrorCompletingRequestException.EXIT_POINT_PROCESSING_ERROR);
            case 0x0301:  // Invalid request.  The request data stream did not match what was required for the specified request.
            case 0x0302:  // Invalid parameter.
            case 0x0303:  // Maximum exceeded.  For RMTCMD, the maximum command length was exceeded and for RMTPGMCALL, the maximum number of parameters was exceeded.
                Trace.log(Trace.ERROR, "Request not valid " + (rc>>8) + "," + (rc&0xff));
                throw new InternalErrorException(InternalErrorException.SYNTAX_ERROR);
            case 0x0304:  // An error occured when calling the user exit program.
                Trace.log(Trace.ERROR, "Error calling exit program ");
                throw new ErrorCompletingRequestException(ErrorCompletingRequestException.EXIT_PROGRAM_CALL_ERROR);
            case 0x0305:  // User exit program denied the request.
                Trace.log(Trace.ERROR, "Exit program denied request");
                throw new ErrorCompletingRequestException(ErrorCompletingRequestException.EXIT_PROGRAM_DENIED_REQUEST);

            // RMTCMD specific return codes.
            case 0x0400:  // Command failed.  Messages returned.
                Trace.log(Trace.INFORMATION, "Run failed");
                return;

            // RMTPGMCALL specific return codes.
            case 0x0500:  // An error occured when resolving to the program to call.
                if (msglist != null  &&  msglist.length > 0)
                {
                    Trace.log(Trace.ERROR, msglist[0].getText());
                    throw new InternalErrorException(InternalErrorException.UNKNOWN);
                }
                else
                {
                    Trace.log(Trace.ERROR, "Could not run program");
                    throw new InternalErrorException(InternalErrorException.UNKNOWN);
                }
            case 0x0501:  // An error occured when calling the program.
                Trace.log(Trace.DIAGNOSTIC, "Error calling the program");
                // pgm not run
                return;

            default:
                Trace.log(Trace.ERROR, "Datastream unknown " + (rc>>8) + "," + (rc&0xff));
                throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
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
            System.arraycopy(data, offset + 41, substitutionData, 0, substitutionDataLength);   //@D1c
            messageList[i].setSubstitutionData(substitutionData);

            messageList[i].setText(converter.byteArrayToString(data, offset + 41 + substitutionDataLength, textLength));

            offset += BinaryConverter.byteArrayToInt(data, offset);
        }

        return messageList;
    }
}
