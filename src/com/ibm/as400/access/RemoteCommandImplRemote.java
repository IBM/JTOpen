///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
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
import java.io.UnsupportedEncodingException;

// The RemoteCommandImplRemote class is the remote implementation of CommandCall and ProgramCall.
class RemoteCommandImplRemote implements RemoteCommandImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private AS400ImplRemote system_;
    private ConverterImplRemote converter_;
    private AS400Server server_;
    private AS400Message[] messageList_  = {};
    private boolean zeroSuppression_ = false;

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
        if (ccsid != 0) converter_ = ConverterImplRemote.getConverter(ccsid, system_);
    }

    // Return message list to public object.
    public AS400Message[] getMessageList()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting message list from implementation object.");
        return messageList_;
    }

    // Connects to the AS/400.
    private void open() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
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
                    throw (IOException)e.fillInStackTrace();
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
            if (converter_ == null)
            {
                converter_ = ConverterImplRemote.getConverter(reply.getCCSID(), system_);
            }
            // If DS level allows, turn zero supression on.
            if (reply.getDSLevel() > 1)
            {
                zeroSuppression_ = true;
            }
        }
    }

    public boolean runCommand(String command) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        Trace.log(Trace.INFORMATION, "Running command: " + command);

        // Connect to server.
        open();

        // Run command on server.
        try
        {
            // Create and send request.
            DataStream baseReply = server_.sendAndReceive(new RCRunCommandRequestDataStream(converter_.stringToByteArray(command)));

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
                Trace.log(Trace.WARNING, "Command " + command + " failed." );
                return false;
            }
        }
        catch (IOException e)
        {
            system_.disconnectServer(server_);
            Trace.log(Trace.ERROR, "Lost connection to remote command server:", e);
            throw (IOException)e.fillInStackTrace();
        }
    }

    public boolean runProgram(String program, ProgramParameter[] parameterList) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        QSYSObjectPathName ifsPath = new QSYSObjectPathName(program, "PGM");

        // Assuming parameter validation has alread occured.
        Trace.log(Trace.INFORMATION, "Running program: " + program);

        // Connect to server
        open();

        // Run program on server
        try
        {
            // Create and send request.
            DataStream baseReply = server_.sendAndReceive(new RCCallProgramRequestDataStream(ifsPath.getLibraryName(), ifsPath.getObjectName(), parameterList, converter_, zeroSuppression_));

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
                    throw new ObjectDoesNotExistException(program, ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST );
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
            throw (IOException)e.fillInStackTrace();
        }
    }

    public Object[] runServiceProgram(String serviceProgram, String procedureName, int returnValueFormat, ProgramParameter[] serviceParameterList) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.INFORMATION, "Running program: " + serviceProgram + " procedure name: " + procedureName);

        // Connect to server.
        open();

        ProgramParameter[] programParameterList = new ProgramParameter[7 + serviceParameterList.length];

        byte[] serviceProgramBytes = new byte[20];
        // Blank fill servcie program name and library name.
        for (int i = 0; i < 20; ++i)
        {
            serviceProgramBytes[i] = (byte)0x40;
        }
        QSYSObjectPathName ifsPath = new QSYSObjectPathName(serviceProgram, "SRVPGM");
        converter_.stringToByteArray(ifsPath.getObjectName(), serviceProgramBytes, 0);
        converter_.stringToByteArray(ifsPath.getLibraryName(), serviceProgramBytes, 10);

        programParameterList[0] = new ProgramParameter(serviceProgramBytes);

        byte[] procedureNameBytes = converter_.stringToByteArray(procedureName);
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

        // Create a new ProgramCall object to run.
        RemoteCommandImplRemote programCall = new RemoteCommandImplRemote();
        programCall.setSystem(system_);

        Object[] spc = new Object[2];    // Array used to return objects to caller.
        if (programCall.runProgram("/QSYS.LIB/QZRUCLSP.PGM", programParameterList) != true)
        {
            messageList_ = programCall.getMessageList();
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
    static private void processReturnCode(int rc, AS400Message[] msglist) throws ErrorCompletingRequestException
    {
        switch (rc)
        {
            case 0x0000: // cmd successful.
                Trace.log(Trace.INFORMATION, "Run succeeded");
                return;

            case 0x0100: // Limited capability user.
                Trace.log(Trace.WARNING, "Limited User");
                return;

            case 0x0101: // EA rq
                Trace.log(Trace.ERROR, "Datastream error on exchange attributes request.");
                throw new InternalErrorException(InternalErrorException.SYNTAX_ERROR);
            case 0x0102: // DS lvl
                Trace.log(Trace.ERROR, "Datastream level not valid");
                throw new InternalErrorException(InternalErrorException.DATA_STREAM_LEVEL_NOT_VALID);
            case 0x0103: // VRM
                Trace.log(Trace.ERROR, "VRM not valid");
                throw new InternalErrorException(InternalErrorException.VRM_NOT_VALID);

                // $$$ NLV
            case 0x0104: // Invalid ccsid.
                Trace.log(Trace.WARNING, "CCSID not valid");
                return;
            case 0x0105: // Invalid nlv (warning).
                Trace.log(Trace.WARNING, "NLV not valid");
                return;
            case 0x0106: // NLV not installed (warning).
                Trace.log(Trace.WARNING, "NLV not installed");
                return;
            case 0x0107: // Error retrieving product info, cannot validate NLV.
                Trace.log(Trace.WARNING, "error retrieving product info, cannot validate NLV");
                return;
            case 0x0108: // Error adding nlv lib to sys lib list.
                Trace.log(Trace.WARNING, "Error adding NLV library to system lib list");
                return;

            case 0x0200: // Error on the recvd data.
            case 0x0201: // LL
            case 0x0202: // server ID
            case 0x0203: // incomplete data
            case 0x0205: // inv rq id
                Trace.log(Trace.ERROR, "Datastream not valid " + (rc>>8) + "," + (rc&0xff));
                throw new InternalErrorException(InternalErrorException.SYNTAX_ERROR );
            case 0x0204: // host resource
                Trace.log(Trace.ERROR, "Host Resource error");
                throw new InternalErrorException(InternalErrorException.UNKNOWN);

            case 0x0300: // Process exit point error.
                Trace.log(Trace.ERROR, "Process exit point error");
                throw new ErrorCompletingRequestException(ErrorCompletingRequestException.EXIT_POINT_PROCESSING_ERROR);
            case 0x0301: // invalid rq
            case 0x0302: // inv parm
            case 0x0303: // max exceeded
                Trace.log(Trace.ERROR, "Request not valid " + (rc>>8) + "," + (rc&0xff));
                throw new InternalErrorException(InternalErrorException.SYNTAX_ERROR);
            case 0x0304: // Error calling exit pgm.
                Trace.log(Trace.ERROR, "Error calling exit program ");
                throw new ErrorCompletingRequestException(ErrorCompletingRequestException.EXIT_PROGRAM_CALL_ERROR);
            case 0x0305: // Exit pgm denied rq.
                Trace.log( Trace.ERROR, "Exit program denied request");
                throw new ErrorCompletingRequestException(ErrorCompletingRequestException.EXIT_PROGRAM_DENIED_REQUEST);

            case 0x0400: // cmd failed, messages returned.
                Trace.log(Trace.INFORMATION, "Run failed");
                return;

            case 0x0500: // resolving pgm to call
                // pgm not found ($$$ =? obj does not exist?)
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
            case 0x0501: // calling the pgm
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
            System.arraycopy(data, 41, substitutionData, 0, substitutionDataLength);
            messageList[i].setSubstitutionData(substitutionData);

            messageList[i].setText(converter.byteArrayToString(data, offset + 41 + substitutionDataLength, textLength));

            offset += BinaryConverter.byteArrayToInt(data, offset);
        }

        return messageList;
    }
}
