///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RemoteCommandImplNative.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.util.StringTokenizer;

// The RemoteCommandImplNative class is the native implementation of CommandCall and ProgramCall.
class RemoteCommandImplNative extends RemoteCommandImplRemote
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private static final int REPLY_BUFFER_LENGTH = 10 * 1024; // 10K bytes

    // Load the service program.
    static
    {
        System.load("/QSYS.LIB/QYJSPART.SRVPGM");
    }

    void open(boolean threadSafety) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (!threadSafety)
        {
            super.open(false);
            return;
        }
        // If converter was not set with an AS400 user override ccsid or set on previous open(), set converter to job ccsid.
        if (!ccsidIsUserOveride_)
        {
            converter_ = ConverterImplRemote.getConverter(system_.getCcsid(), system_);
        }
    }

    // Queries the AS/400 to determine if the command is declared to be thread-safe.
    // @return  true if the command is declared on the AS/400 to be thread-safe; false otherwise.
    // Indicates whether or not the AS/400 command will be considered thread-safe.
    // @return  true if the command should be considered thread-safe; false if the command should be considered non-thread-safe.
    public boolean isCommandThreadSafe(String command) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        open(true);

        // Isolate out the command name from the argument(s), as the first token.
        StringTokenizer tokenizer = new StringTokenizer(command);
        String cmdLibAndName = tokenizer.nextToken();    // @A1c
        String libName;                                  // @A1a
        String cmdName;
        // If there's a slash, parse out the library/commandName.
        int slashPos = cmdLibAndName.indexOf('/');       // @A1a
        if (slashPos == -1) // no slash                  // @A1a
        {
          libName = "*LIBL";                             // @A1a
          cmdName = cmdLibAndName;                       // @A1a
        }
        else                                             // @A1a
        {
          libName = cmdLibAndName.substring(0,slashPos); // @A1a
          cmdName = cmdLibAndName.substring(slashPos+1); // @A1a
        }

        // Uppercase the command name if it has lowercase characters.
        boolean foundLowerCaseChar = false;
        for (int i = 0; i < cmdName.length() && !foundLowerCaseChar; ++i)
        {
            if (Character.isLowerCase(cmdName.charAt(i))) foundLowerCaseChar = true;
        }
        if (foundLowerCaseChar)
        {
            cmdName = cmdName.toUpperCase(new java.util.Locale("en","US"));
        }

        // Set up the parameter list.

        // Format for "Retrieve Command Info" API (QCDRCMDI):
        // 1 - Receiver variable - Output - Char *
        // 2 - Length of receiver variable - Input - Bin 4
        // 3 - Format name - Input - Char 8
        // 4 - Qualified command name - Input - Char 20
        // 5 - Error code - I/O - Char *

        byte[] dataReceived = new byte[350];

        ProgramParameter[] parameterList = new ProgramParameter[5];

        // Set up the input parameters that never change.

        // Second parameter: input, length of receiver variable.
        byte[] receiverLength = new byte[4];
        BinaryConverter.intToByteArray(dataReceived.length, receiverLength, 0);
        parameterList[1] = new ProgramParameter(receiverLength);

        // Third parameter: input, format name.
        byte[] formatName = new byte[8];
        converter_.stringToByteArray("CMDI0100", formatName);
        parameterList[2] = new ProgramParameter(formatName);

        // Set up the remaining parameters.

        // First parameter: output, is the receiver variable.
        //
        // Format of data returned:
        // Offset - Type - Field
        // 0  - Bin 4 - Bytes returned
        // 4  - Bin 4 - Bytes available
        // 8  - Char 10 - Command name
        // 18 - Char 10 - Command library name
        // 28 - Char 10 - Command processing program name
        // 38 - Char 10 - Command processing program library name
        // ......
        // 333 - Char 1 - Threadsafe indicator
        // 334 - Char 1 - Multithreaded job action
        // 335 - Char 15 - Reserved

        parameterList[0] = new ProgramParameter(dataReceived.length);

        // Fourth parameter: input, qualified command name.
        //
        // Qualified command name
        // INPUT; CHAR(20)
        //
        // The name of the command whose values are to be retrieved.
        // The first 10 characters contain the name of the command.
        // The second 10 characters contain the name of the library where the command is located.
        //
        // You can use these special values for the library name:
        //
        // *CURLIB - The job's current library
        // *LIBL - The library list
        byte[] commandName = new byte[20];
        for (int i = 0; i < 20; ++i)      // Fill the commandname array with blanks.
        {
            commandName[i] = (byte)0x40;
        }
        converter_.stringToByteArray(cmdName, commandName);
        converter_.stringToByteArray(libName, commandName, 10);
        parameterList[3] = new ProgramParameter(commandName);

        // Fifth parameter: input/output, error code
        //
        // Format ERRC0100:
        // Offset - Use - Type - Field
        // 0 - Input - Bin 4 - Bytes provided
        // 4 - Output - Bin 4 - Bytes available
        // 8 - Output - Char 7 - Exception ID
        // 15 - Output - Char 1 - Reserved
        // 16 - Output - Char * - Exception data
        byte[] errorCode = new byte[17];
        // A value >=8 of bytes provided means the 400 will return exception
        // information on the parameter instead of throwing an exception
        // to the application. We provide 0 to ensure it does indeed
        // throw us an exception.
        BinaryConverter.intToByteArray(0, errorCode, 0);
        parameterList[4] = new ProgramParameter(errorCode, 17);

        // Prepare to call the "Retrieve Command Info" API (program).
        // Design note: XXXXX XXXXXX has indicated that, even though
        // the QCDRCMDI API is documented as "not threadsafe",
        // for our purposes we can assume that it *is* threadsafe.

        // Retrieve Command Information.  Failure is returned as a message list.
        try
        {
            if(!runProgram("QSYS", "QCDRCMDI", parameterList, true))
            {
                Trace.log(Trace.ERROR, "Unable to retrieve command information.");
                // Trace the messages.
                for (int i = 0; i < messageList_.length; ++i)
                {
                    Trace.log(Trace.ERROR, messageList_[i].toString());
                }

                // Since the lookup failed, assume the command under scrutiny is not thread-safe.
                throw new AS400Exception(messageList_[0]);
            }
        }
        catch (ObjectDoesNotExistException e)
        {
            Trace.log(Trace.ERROR, "Unexpected ObjectDoesNotExistException:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }

        // Get the data returned from the program
        dataReceived = parameterList[0].getOutputData();
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Command Information retrieved:", dataReceived);

        // The "threadsafe indicator" field is a single byte at offset 333.
        //
        // Threadsafe indicator: Whether the command can be used safely in a multithreaded job.
        // The possible values are:
        // 0   The command is not threadsafe and should not be used in a multithreaded job.
        // 1   The command is threadsafe and can be used safely in a multithreaded job.
        // 2   The command is threadsafe under certain conditions.  See the documentation for the command to determine the conditions under which the command can be used safely in a multithreaded job.

        // The "multithreaded job action" field is a single byte at offset 334.
        //
        // Multithreaded job action. The action to take when a command
        // that is not threadsafe is called in a multithreaded job.
        // The possible values are:
        //
        // 0  Use the action specified in QMLTTHDACN system value.
        // 1  Run the command. Do not send a message.
        // 2  Send an informational message and run the command.
        // 3  Send an escape message, and do not run the command.
        //
        // If the threadsafe indicator is either threadsafe or conditionally
        // threadsafe, the multithreaded job action value will be returned as 1.

        // System value . . . . . :   QMLTTHDACN
        // Description  . . . . . :   Multithreaded job action
        //
        // Interpretation:
        //                      1=Perform the function that is not
        //                        threadsafe without sending a message
        //                      2=Perform the function that is not
        //                        threadsafe and send an informational
        //                        message
        //                      3=Do not perform the function that is
        //                        not threadsafe

        // Examine the "threadsafe indicator" field.
        String tsiVal = converter_.byteArrayToString(dataReceived, 333, 1);
        if (Trace.isTraceOn())
        {
            // Examine the "multithreaded job action" field.
            String mtjaVal = converter_.byteArrayToString(dataReceived, 334, 1);
            Trace.log(Trace.DIAGNOSTIC, "<<<Multithreaded job action = "+ mtjaVal + " >>>");
        }
        if (tsiVal.equals("0"))
        {
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "<<<NOT THREADSAFE>>>: " + command);
            return false;
        }
        if (tsiVal.equals("1"))
        {
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "<<<THREADSAFE>>>: " + command);
            return true;
        }
        if (tsiVal.equals("2"))
        {
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Conditionally threadsafe: " + command);
            return false;
        }
        if (Trace.isTraceOn()) Trace.log(Trace.ERROR, "<<<UNRECOGNIZED THREADSAFE>>>: " + command);
        if (Trace.isTraceOn()) Trace.log(Trace.ERROR, "Invalid threadsafe indicator: " + tsiVal);
        return false;  // assume the command is not thread-safe
    }

    // Runs the command on the AS/400.
    // @return  true if command is successful; false otherwise.
    public boolean runCommand(String command, boolean threadSafety) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (!threadSafety)
        {
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "CommandCallImplNative.run() --> Calling super.run()");
            return super.runCommand(command, false);
        }

        open(true);

        // Uppercase the command name if it has lowercase characters.
        // When we bypass the host server, we apparently also bypass
        // some automatic uppercasing (by the Command Analyzer).
        StringTokenizer tokenizer = new StringTokenizer(command);
        String cmdName = tokenizer.nextToken();
        boolean foundLowerCaseChar = false;
        for (int i = 0; i < cmdName.length() && !foundLowerCaseChar; ++i)
        {
            if (Character.isLowerCase(cmdName.charAt(i))) foundLowerCaseChar = true;
        }
        if (foundLowerCaseChar)
        {
            StringBuffer buf = new StringBuffer(command.length());
            // Uppercase the command name only.
            buf.append(cmdName.toUpperCase(new java.util.Locale("en","US")));
            buf.append(command.substring(cmdName.length())); // Parameters.
            command = buf.toString();
        }

        // Run the command on the AS/400, on the current thread (bypassing sockets).

        // Create a "run command" request, and write it as raw bytes to a byte array.
        byte[] rawRequest = converter_.stringToByteArray(command);

        // Call native method.
        if (Trace.isTraceOn()) Trace.log(Trace.INFORMATION, "Calling runCommandNative(" + command + ")");

        return runCommandCommon(command, rawRequest);
    }

    // Runs the command on the AS/400.
    // @return  true if command is successful; false otherwise.
    public boolean runCommand(byte[] command, boolean threadSafety) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (!threadSafety)
        {
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "CommandCallImplNative.run() --> Calling super.run()");
            return super.runCommand(command, false);
        }

        open(true);

        return runCommandCommon(null, command);
    }



    // @D2a new method (most of the code is from runCommand(String, boolean)
    public boolean runCommandCommon(String command, byte[] rawRequest) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "CommandCallImplNative.runOnThread(): request = ", rawRequest);

        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            byte[] rawReply = runCommandNative(rawRequest);

            if (Trace.isTraceOn()) Trace.log(Trace.INFORMATION, "Returned from runCommandNative.");

            if (rawReply == null)
            {
                Trace.log (Trace.ERROR, "Null reply returned from runCommandNative()");
                return true;
            }
            if (rawReply.length == 0)
            {
                Trace.log (Trace.ERROR, "Zero-length reply returned from runCommandNative()");
                return true;
            }

            if (Trace.isTraceOn()) Trace.log( Trace.DIAGNOSTIC, "CommandCallImplNative.runOnThread(): reply = ", rawReply);

            // Get info from reply.
            int messageNumber = rawReply.length / 10240;
            messageList_ = new AS400Message[messageNumber];

            for (int i = 0; i < messageNumber; ++i)
            {
                int offset = i * 10240;
                messageList_[i] = new AS400Message();
                messageList_[i].setID(converter_.byteArrayToString(rawReply, offset + 12, 7));
                messageList_[i].setType((rawReply[offset + 19] & 0x0F) * 10 + (rawReply[offset + 20] & 0x0F));
                messageList_[i].setSeverity(BinaryConverter.byteArrayToInt(rawReply, offset + 8));
                messageList_[i].setFileName(converter_.byteArrayToString(rawReply, offset + 25, 10).trim());
                messageList_[i].setLibraryName(converter_.byteArrayToString(rawReply, offset + 45, 10).trim());

                int substitutionDataLength = BinaryConverter.byteArrayToInt(rawReply, offset + 80);
                int textLength = BinaryConverter.byteArrayToInt(rawReply, offset + 88);

                byte[] substitutionData = new byte[substitutionDataLength];
                System.arraycopy(rawReply, offset + 112, substitutionData, 0, substitutionDataLength);
                messageList_[i].setSubstitutionData(substitutionData);

                messageList_[i].setText(converter_.byteArrayToString(rawReply, offset + 112 + substitutionDataLength, textLength));
            }

            return true;
        }
        catch (NativeException e)  // exception found by C code
        {
            int messageNumber = e.data.length / 10240;
            messageList_ = new AS400Message[messageNumber];

            for (int i = 0; i < messageNumber; ++i)
            {
                int offset = i * 10240;
                messageList_[i] = new AS400Message();
                messageList_[i].setID(converter_.byteArrayToString(e.data, offset + 12, 7));
                messageList_[i].setType((e.data[offset + 19] & 0x0F) * 10 + (e.data[offset + 20] & 0x0F));
                messageList_[i].setSeverity(BinaryConverter.byteArrayToInt(e.data, offset + 8));
                messageList_[i].setFileName(converter_.byteArrayToString(e.data, offset + 25, 10).trim());
                messageList_[i].setLibraryName(converter_.byteArrayToString(e.data, offset + 45, 10).trim());

                int substitutionDataLength = BinaryConverter.byteArrayToInt(e.data, offset + 80);
                int textLength = BinaryConverter.byteArrayToInt(e.data, offset + 88);

                byte[] substitutionData = new byte[substitutionDataLength];
                System.arraycopy(e.data, offset + 112, substitutionData, 0, substitutionDataLength);
                messageList_[i].setSubstitutionData(substitutionData);

                messageList_[i].setText(converter_.byteArrayToString(e.data, offset + 112 + substitutionDataLength, textLength));
            }

            // parse information from byte array
            String id = converter_.byteArrayToString(e.data, 12, 7);

            if (id.equals("CPF9802") || // Not authorized to object &2 in &3.
                id.equals("CPF2189"))   // Not authorized to object &1 in &2 type *&3.
            {
                if (command != null)
                   throw new AS400SecurityException(command, AS400SecurityException.OBJECT_AUTHORITY_INSUFFICIENT);
                else
                   throw new AS400SecurityException(AS400SecurityException.OBJECT_AUTHORITY_INSUFFICIENT);
            }
            if (id.equals("CPF9820") || // Not authorized to use library &1.
                id.equals("CPF2182"))   // Not authorized to library &1.
            {
                if (command != null)
                   throw new AS400SecurityException(command, AS400SecurityException.LIBRARY_AUTHORITY_INSUFFICIENT);
                else
                   throw new AS400SecurityException(AS400SecurityException.LIBRARY_AUTHORITY_INSUFFICIENT);
            }
            return false;
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }
    }




    /**
     Sets the program name and the parameter list and
     runs the program on the AS/400.

     @param ifsPath  The fully qualified integrated file system path name
     to the program.
     The library and program name must each be 10 characters or less.
     @param parmList The list of parameters with which to run the program.

     @return true if program ran successfully, false otherwise.

     @exception AS400SecurityException If a security or authority error occurs.
     @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     @exception ErrorCompletingRequestException If an error occurs before
     the request is completed.
     @exception InterruptedException If this thread is interrupted.
     @exception IOException If an error occurs while communicating with the AS/400.
     @exception UnknownHostException If the AS/400 system cannot be located.
     @exception ObjectDoesNotExistException If the AS/400 object does not exist.
     **/
    public boolean runProgram(String library, String name, ProgramParameter[] parameterList, boolean threadSafety) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        // Note: Since we are running an ImplNative, we can infer that the system object does not have its mustUseSockets attribute turned on.
        if (!threadSafety)
        {
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "RemoteCommandImplNative.run() --> Calling super.runProgram()");
            return super.runProgram(library, name, parameterList, false);
        }
        // run the program on-thread
        open(true);

        // Create a "call program" request, and write it as raw bytes to a byte array.

        // Set up the buffer that contains the program to call.  The
        // buffer contains three items:
        //    10 characters - the program to call
        //    10 characters - the library that contains the program
        //     4 bytes      - the number of parameters

        StringBuffer programName = new StringBuffer("                    ");
        programName.insert(0, name);
        programName.insert(10, library);
        programName.setLength(20);
        String programNameString = programName.toString();

        byte[] programNameBuffer = new byte[24];
        converter_.stringToByteArray(programNameString, programNameBuffer);

        BinaryConverter.intToByteArray(parameterList.length, programNameBuffer, 20);

        // Set up the parameter structure.  There is one structure
        // for each parameters.  The structure contains:
        //    4 bytes - the length of the parameter
        //    2 bytes - the parameters usage (input/output/inout)
        //    4 bytes - the offset into the parameter buffer

        byte[] programParameterStructure = new byte[parameterList.length * 10];
        int totalParameterLength = 0;
        for (int i = 0, offset = 0; i < parameterList.length; ++i)
        {
            int parameterMaxLength = parameterList[i].getMaxLength();
            int parameterUsage = parameterList[i].getUsage();

            BinaryConverter.intToByteArray(parameterMaxLength, programParameterStructure, i * 10);
            BinaryConverter.unsignedShortToByteArray(parameterUsage, programParameterStructure, i * 10 + 4);
            BinaryConverter.intToByteArray(offset, programParameterStructure, i * 10 + 6);

            offset += parameterMaxLength;
            totalParameterLength += parameterMaxLength;
        }

        // Set up the Parameter area.
        byte[] programParameters = new byte[totalParameterLength];
        for (int i = 0, offset = 0; i < parameterList.length; ++i)
        {
            byte[] inputData = parameterList[i].getInputData();
            int parameterMaxLength = parameterList[i].getMaxLength();
            if (inputData != null)
            {
                System.arraycopy(inputData, 0, programParameters, offset, inputData.length);
            }
            offset += parameterMaxLength;
        }

        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "RemoteCommandImplNative.runProgramOnThread(): programNameBuffer = ", programNameBuffer);
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "RemoteCommandImplNative.runProgramOnThread(): programParameterStructure = ", programParameterStructure);
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "RemoteCommandImplNative.runProgramOnThread(): programParameters = ", programParameters);
        byte[] rawReply = null;
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            // Call native method.
            if (Trace.isTraceOn()) Trace.log( Trace.INFORMATION, "Calling runProgramNative(" + library + "/" + name + ")");

            rawReply = runProgramNative(programNameBuffer, programParameterStructure, programParameters);

            if (Trace.isTraceOn() && Trace.isTraceInformationOn())
                Trace.log( Trace.INFORMATION, "Returned from runProgramNative.");
        }
        catch (NativeException e)  // exception found by C code
        {
            int messageNumber = e.data.length / 10240;
            messageList_ = new AS400Message[messageNumber];

            for (int i = 0; i < messageNumber; ++i)
            {
                int offset = i * 10240;
                messageList_[i] = new AS400Message();
                messageList_[i].setID(converter_.byteArrayToString(e.data, offset + 12, 7));
                messageList_[i].setType((e.data[offset + 19] & 0x0F) * 10 + (e.data[offset + 20] & 0x0F));
                messageList_[i].setSeverity(BinaryConverter.byteArrayToInt(e.data, offset + 8));
                messageList_[i].setFileName(converter_.byteArrayToString(e.data, offset + 25, 10).trim());
                messageList_[i].setLibraryName(converter_.byteArrayToString(e.data, offset + 45, 10).trim());

                int substitutionDataLength = BinaryConverter.byteArrayToInt(e.data, offset + 80);
                int textLength = BinaryConverter.byteArrayToInt(e.data, offset + 88);

                byte[] substitutionData = new byte[substitutionDataLength];
                System.arraycopy(e.data, offset + 112, substitutionData, 0, substitutionDataLength);
                messageList_[i].setSubstitutionData(substitutionData);

                messageList_[i].setText(converter_.byteArrayToString(e.data, offset + 112 + substitutionDataLength, textLength));
            }

            // parse information from byte array
            String id = converter_.byteArrayToString(e.data, 12, 7);

            if (id.equals("CPF9801") || // Object &2 in library &3 not found.
                id.equals("CPF2105") || // Object &1 in &2 type *&3 not found.
                id.equals("CPF9805"))   // Object &2 in library &3 destroyed.
            {
                throw new ObjectDoesNotExistException(QSYSObjectPathName.toPath(library, name, "PGM"), ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
            }
            if (id.equals("CPF9810")) // Library &1 not found.
            {
                throw new ObjectDoesNotExistException(QSYSObjectPathName.toPath(library, name, "PGM"), ObjectDoesNotExistException.LIBRARY_DOES_NOT_EXIST);
            }
            if (id.equals("CPF9802") || // Not authorized to object &2 in &3.
                id.equals("CPF2189"))   // Not authorized to object &1 in &2 type *&3.
            {
                throw new AS400SecurityException(QSYSObjectPathName.toPath(library, name, "PGM"), AS400SecurityException.OBJECT_AUTHORITY_INSUFFICIENT);
            }
            if (id.equals("CPF9820") || // Not authorized to use library &1.
                id.equals("CPF2182"))   // Not authorized to library &1.
            {
                throw new AS400SecurityException(QSYSObjectPathName.toPath(library, name, "PGM"), AS400SecurityException.LIBRARY_AUTHORITY_INSUFFICIENT);
            }
            return false;
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }

        if (rawReply == null)
        {
            Trace.log (Trace.ERROR, "Null reply returned from runProgramNative()");
            throw new InternalErrorException(InternalErrorException.UNKNOWN);
        }
        if (rawReply.length == 0)
        {
            Trace.log (Trace.ERROR, "Zero-length reply returned from runProgramNative()");
            throw new InternalErrorException(InternalErrorException.UNKNOWN);
        }

        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "RemoteCommandImplNative.runProgramOnThread(): reply = ", rawReply);

        // For each output/inout parm, in order, set data returned.
        for (int index = 0, i = 0; i < parameterList.length; ++i)
        {
            int parameterMaxLength = parameterList[i].getMaxLength();
            int outputDataLength = parameterList[i].getOutputDataLength();
            if (outputDataLength > 0)
            {
                byte[] outputData = new byte[outputDataLength];
                System.arraycopy(rawReply, index, outputData, 0, outputDataLength);
                parameterList[i].setOutputData(outputData);
            }
            index += parameterMaxLength;
        }
        return true;
    }

    /**
     Submits a Remote Command Call request datastream (as a byte array)
     directly to the Host Server code that handles command call,
     without going through sockets.

     @param request The command (with any arguments) to run on the AS/400,
     in the form of a dpcreqds object that has been written into a byte array.

     @param requestLength The length of request (number of bytes).

     @param replyBuffer The buffer in which to the reply data may be returned.
     If the buffer is too small to contain the reply, runCommandNative
     will allocate a new buffer.

     @param replyBufferLength The length of replyBuffer (number of bytes).

     @return A byte array containing a Remote Command Call reply datastream.
     If replyBuffer was large enough to contain the reply, then the returned
     value is simply a pointer to replyBuffer.
     **/
    private native byte[] runCommandNative(byte[] request) throws NativeException;

    /**
     Submits a Remote Program Call request datastream (as a byte array)
     directly to the Host Server code that handles program call,
     without going through sockets.

     @param request The program (with any parameters) to run on the AS/400,
     in the form of a dpcreqds object that has been written into a byte array.

     @param requestLength The length of request (number of bytes).

     @param replyBuffer The buffer in which to the reply data may be returned.
     If the buffer is too small to contain the reply, runProgramNative
     will allocate a new buffer.

     @param replyBufferLength The length of replyBuffer (number of bytes).

     @return A byte array containing a Remote Program Call reply datastream.
     If replyBuffer was large enough to contain the reply, then the returned
     value is simply a pointer to replyBuffer.
     **/
    private native byte[] runProgramNative(byte[] request, byte[] requestLength, byte[] replyBuffer) throws NativeException;
}
