///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  RemoteCommandImplNative.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.util.StringTokenizer;

// The RemoteCommandImplNative class is the native implementation of CommandCall and ProgramCall.
class RemoteCommandImplNative extends RemoteCommandImplRemote
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // Load the service program.
    static
    {
        System.load("/QSYS.LIB/QYJSPART.SRVPGM");
    }

    protected void open(boolean threadSafety) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Native implementation object open.");
        if (!threadSafety)
        {
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending native open request to super class.");
            super.open(false);
            return;
        }
        // If converter was not set with a user override ccsid, set converter to job ccsid.
        if (!ccsidIsUserOveride_)
        {
            converter_ = ConverterImplRemote.getConverter(system_.getCcsid(), system_);
        }
            serverDataStreamLevel_ = 6;
    }

    // Indicates whether or not the command will be considered thread-safe.
    // @return  true if the command is declared to be thread-safe; false otherwise.
    public boolean isCommandThreadSafe(String command) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Native implementation object checking command thread safety.");
        open(true);

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
        ProgramParameter[] parameterList = new ProgramParameter[]
        {
            new ProgramParameter(350),
            new ProgramParameter(new byte[] { 0x00, 0x00, 0x01, 0x5e }),
            new ProgramParameter(new byte[] { (byte)0xC3, (byte)0xD4, (byte)0xC4, (byte)0xC9, (byte)0xF0, (byte)0xF1, (byte)0xF0, (byte)0xF0 } ),
            new ProgramParameter(commandName),
            new ProgramParameter(new byte[8])
        };

        try
        {
            // Retrieve command information.  Failure is returned as a message list.
            if(!runProgram("QSYS", "QCDRCMDI", parameterList, true, AS400Message.MESSAGE_OPTION_UP_TO_10))
            {
                Trace.log(Trace.ERROR, "Unable to retrieve command information.");
                String id = messageList_[messageList_.length - 1].getID();
                byte[] substitutionBytes = messageList_[messageList_.length - 1].getSubstitutionData();

                // CPF9801 - Object &2 in library &3 not found.
                if (id.equals("CPF9801") && cmdName.equals(converter_.byteArrayToString(substitutionBytes, 0, 10).trim()) && libName.equals(converter_.byteArrayToString(substitutionBytes, 10, 10).trim()) && "CMD".equals(converter_.byteArrayToString(substitutionBytes, 20, 7).trim()))
                {
                    Trace.log(Trace.DIAGNOSTIC, "Command not found.");
                    return false;  // If cmd doesn't exist, it's not threadsafe.
                }
                // CPF9810 - Library &1 not found.
                if (id.equals("CPF9810") && libName.equals(converter_.byteArrayToString(substitutionBytes).trim()))
                {
                    Trace.log(Trace.DIAGNOSTIC, "Command library not found.");
                    return false;  // If cmd doesn't exist, it's not threadsafe.
                }
                throw new AS400Exception(messageList_);
            }
        }
        catch (ObjectDoesNotExistException e)
        {
            Trace.log(Trace.ERROR, "Unexpected ObjectDoesNotExistException:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }

        // Get the data returned from the program.
        byte[] dataReceived = parameterList[0].getOutputData();
        if (Trace.isTraceOn())
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
        switch (dataReceived[333] & 0x0F)
        {
            case 0:
                if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Command not threadsafe: " + cmdLibAndName);
                return false;
            case 1:
                if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Command threadsafe: " + cmdLibAndName);
                return true;
            case 2:
                if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Conditionally threadsafe: " + cmdLibAndName);
                return false;
        }
        if (Trace.isTraceOn()) Trace.log(Trace.ERROR, "Invalid threadsafe indicator: " + cmdLibAndName);
        return false;  // Assume the command is not thread-safe.
    }

    // Runs the command.
    // @return  true if command is successful; false otherwise.
    public boolean runCommand(String command, boolean threadSafety, int messageCount) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.INFORMATION, "Native implementation running command: " + command);
        if (!threadSafety)
        {
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Sending command to super class.");
            return super.runCommand(command, false, messageCount);
        }
        open(true);
        return runCommand(converter_.stringToByteArray(command), messageCount);
    }

    // Runs the command.
    // @return  true if command is successful; false otherwise.
    public boolean runCommand(byte[] command, boolean threadSafety, int messageCount) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.INFORMATION, "Native implementation running command:", command);
        if (!threadSafety)
        {
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Sending command to super class.");
            return super.runCommand(command, false, messageCount);
        }

        open(true);

        return runCommand(command, messageCount);
    }

    private boolean runCommand(byte[] commandBytes, int messageCount) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        try
        {
            if (Trace.isTraceOn()) Trace.log(Trace.INFORMATION, "Invoking native method.");
            if (AS400.nativeVRM.vrm_ < 0x00050300)
            {
                try
                {
                    byte[] replyBytes = runCommandNative(commandBytes);
                    if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Native reply bytes:", replyBytes);

                    if (replyBytes == null) replyBytes = new byte[0];

                    // Get info from reply.
                    messageList_ = RemoteCommandImplNative.parseMessages(replyBytes, converter_);
                    return true;
                }
                catch (NativeException e)  // Exception found by C code.
                {
                    messageList_ = RemoteCommandImplNative.parseMessages(e.data, converter_);
                    return false;
                }
            }
            else
            {
                try
                {
                    byte[] replyBytes = runCommandNativeV5R3(commandBytes, messageCount);
                    if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Native reply bytes:", replyBytes);

                    // Get info from reply.
                    messageList_ = RemoteCommandImplNative.parseMessagesV5R3(replyBytes, converter_);
                    return true;
                }
                catch (NativeException e)  // Exception found by C code.
                {
                    messageList_ = RemoteCommandImplNative.parseMessagesV5R3(e.data, converter_);
                    return false;
                }
            }
        }
        finally
        {
            if (didSwap) system_.swapBack(swapToPH, swapFromPH);
        }
    }

    // Run the program.
    public boolean runProgram(String library, String name, ProgramParameter[] parameterList, boolean threadSafety, int messageCount) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.INFORMATION, "Native implementation running program: " + library + "/" + name);
        if (!threadSafety)
        {
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Sending program to super class.");
            return super.runProgram(library, name, parameterList, false, messageCount);
        }
        // Run the program on-thread.
        open(true);

        if (AS400.nativeVRM.vrm_ < 0x00050300)
        {
            // Create a "call program" request, and write it as raw bytes to a byte array.
            // Set up the buffer that contains the program to call.  The buffer contains three items:
            //  10 characters - the program to call.
            //  10 characters - the library that contains the program.
            //   4 bytes      - the number of parameters.
            byte[] programNameBuffer = {(byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
            converter_.stringToByteArray(name, programNameBuffer);
            converter_.stringToByteArray(library, programNameBuffer, 10);
            BinaryConverter.intToByteArray(parameterList.length, programNameBuffer, 20);

            // Set up the parameter structure.  There is one structure for each parameters.
            // The structure contains:
            //   4 bytes - the length of the parameter.
            //   2 bytes - the parameters usage (input/output/inout).
            //   4 bytes - the offset into the parameter buffer.
            byte[] programParameterStructure = new byte[parameterList.length * 10];
            int totalParameterLength = 0;
            for (int i = 0, offset = 0; i < parameterList.length; ++i)
            {
                int parameterMaxLength = parameterList[i].getMaxLength();
                int parameterUsage = parameterList[i].getUsage();
                if (parameterUsage == ProgramParameter.NULL)
                {
                    // Server does not allow null parameters.
                    parameterUsage = ProgramParameter.INPUT;
                }

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

            if (Trace.isTraceOn())
            {
                Trace.log(Trace.DIAGNOSTIC, "Program name bytes:", programNameBuffer);
                Trace.log(Trace.DIAGNOSTIC, "Program parameter bytes:", programParameterStructure);
                Trace.log(Trace.DIAGNOSTIC, "Program parameters:", programParameters);
            }
            byte[] swapToPH = new byte[12];
            byte[] swapFromPH = new byte[12];
            boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
            try
            {
                // Call native method.
                if (Trace.isTraceOn()) Trace.log(Trace.INFORMATION, "Invoking native method.");
                byte[] replyBytes = runProgramNative(programNameBuffer, programParameterStructure, programParameters);
                if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Native reply bytes:", replyBytes);

                // Reset the message list.
                messageList_ = new AS400Message[0];

                // For each output/inout parm, in order, set data returned.
                for (int index = 0, i = 0; i < parameterList.length; ++i)
                {
                    int parameterMaxLength = parameterList[i].getMaxLength();
                    int outputDataLength = parameterList[i].getOutputDataLength();
                    if (outputDataLength > 0)
                    {
                        byte[] outputData = new byte[outputDataLength];
                        System.arraycopy(replyBytes, index, outputData, 0, outputDataLength);
                        parameterList[i].setOutputData(outputData);
                    }
                    index += parameterMaxLength;
                }
                return true;
            }
            catch (NativeException e)  // Exception found by C code.
            {
                messageList_ = RemoteCommandImplNative.parseMessages(e.data, converter_);

                // Parse information from byte array.
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
                return false;
            }
            finally
            {
                if (didSwap) system_.swapBack(swapToPH, swapFromPH);
            }
        }
        else
        {
            byte[] tempBytes = converter_.stringToByteArray(name);
            byte[] nameBytes = new byte[tempBytes.length + 1];
            System.arraycopy(tempBytes, 0, nameBytes, 0, tempBytes.length);
            tempBytes = converter_.stringToByteArray(library);
            byte[] libraryBytes = new byte[tempBytes.length + 1];
            System.arraycopy(tempBytes, 0, libraryBytes, 0, tempBytes.length);

            byte[] offsetArray = new byte[parameterList.length * 4];
            int totalParameterLength = 0;
            for (int i = 0; i < parameterList.length; ++i)
            {
                if (parameterList[i].getUsage() == ProgramParameter.NULL)
                {
                    BinaryConverter.intToByteArray(-1, offsetArray, i * 4);
                }
                else
                {
                    BinaryConverter.intToByteArray(totalParameterLength, offsetArray, i * 4);
                }
                totalParameterLength += parameterList[i].getMaxLength();
            }

            // Set up the Parameter area.
            byte[] programParameters = new byte[totalParameterLength];
            for (int i = 0, offset = 0; i < parameterList.length; ++i)
            {
                byte[] inputData = parameterList[i].getInputData();
                if (inputData != null)
                {
                    System.arraycopy(inputData, 0, programParameters, offset, inputData.length);
                }
                offset += parameterList[i].getMaxLength();
            }

            if (Trace.isTraceOn())
            {
                Trace.log(Trace.DIAGNOSTIC, "Program name bytes:", nameBytes);
                Trace.log(Trace.DIAGNOSTIC, "Program library bytes:", libraryBytes);
                Trace.log(Trace.DIAGNOSTIC, "Number of parameters:", parameterList.length);
                Trace.log(Trace.DIAGNOSTIC, "Offset array:", offsetArray);
                Trace.log(Trace.DIAGNOSTIC, "Program parameters:", programParameters);
            }
            byte[] swapToPH = new byte[12];
            byte[] swapFromPH = new byte[12];
            boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
            try
            {
                // Call native method.
                if (Trace.isTraceOn()) Trace.log(Trace.INFORMATION, "Invoking native method.");
                byte[] replyBytes = runProgramNativeV5R3(nameBytes, libraryBytes, parameterList.length, offsetArray, programParameters, messageCount);
                if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Native reply bytes:", replyBytes);

                // Reset the message list.
                messageList_ = new AS400Message[0];

                // For each output/inout parm, in order, set data returned.
                for (int index = 0, i = 0; i < parameterList.length; ++i)
                {
                    int outputDataLength = parameterList[i].getOutputDataLength();
                    if (outputDataLength > 0)
                    {
                        byte[] outputData = new byte[outputDataLength];
                        System.arraycopy(replyBytes, index, outputData, 0, outputDataLength);
                        parameterList[i].setOutputData(outputData);
                    }
                    index += parameterList[i].getMaxLength();
                }
                return true;
            }
            catch (NativeException e)  // Exception found by C code.
            {
                messageList_ = RemoteCommandImplNative.parseMessagesV5R3(e.data, converter_);

                // Parse information from byte array.
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
                return false;
            }
            finally
            {
                if (didSwap) system_.swapBack(swapToPH, swapFromPH);
            }
        }
    }

    static AS400Message[] parseMessages(byte[] data, ConverterImplRemote converter)
    {
        int messageNumber = data.length / 10240;
        AS400Message[] messageList = new AS400Message[messageNumber];

        for (int offset = 0, i = 0; i < messageNumber; ++i)
        {
            messageList[i] = new AS400Message();
            messageList[i].setID(converter.byteArrayToString(data, offset + 12, 7));
            messageList[i].setType((data[offset + 19] & 0x0F) * 10 + (data[offset + 20] & 0x0F));
            messageList[i].setSeverity(BinaryConverter.byteArrayToInt(data, offset + 8));
            messageList[i].setFileName(converter.byteArrayToString(data, offset + 25, 10).trim());
            messageList[i].setLibraryName(converter.byteArrayToString(data, offset + 45, 10).trim());

            int substitutionDataLength = BinaryConverter.byteArrayToInt(data, offset + 80);
            int textLength = BinaryConverter.byteArrayToInt(data, offset + 88);

            byte[] substitutionData = new byte[substitutionDataLength];
            System.arraycopy(data, offset + 112, substitutionData, 0, substitutionDataLength);
            messageList[i].setSubstitutionData(substitutionData);

            messageList[i].setText(converter.byteArrayToString(data, offset + 112 + substitutionDataLength, textLength));
            offset += 10240;
        }
        return messageList;
    }

    static AS400Message[] parseMessagesV5R3(byte[] data, ConverterImplRemote converter)
    {
        int messageNumber = BinaryConverter.byteArrayToInt(data, 0);
        AS400Message[] messageList = new AS400Message[messageNumber];

        for (int offset = 4, i = 0; i < messageNumber; ++i)
        {
            messageList[i] = new AS400Message();
            messageList[i].setID(converter.byteArrayToString(data, offset + 12, 7));
            messageList[i].setType((data[offset + 19] & 0x0F) * 10 + (data[offset + 20] & 0x0F));
            messageList[i].setSeverity(BinaryConverter.byteArrayToInt(data, offset + 8));
            messageList[i].setFileName(converter.byteArrayToString(data, offset + 25, 10).trim());
            messageList[i].setLibraryName(converter.byteArrayToString(data, offset + 45, 10).trim());

            int substitutionDataLength = BinaryConverter.byteArrayToInt(data, offset + 80);
            int textLength = BinaryConverter.byteArrayToInt(data, offset + 88);

            byte[] substitutionData = new byte[substitutionDataLength];
            System.arraycopy(data, offset + 112, substitutionData, 0, substitutionDataLength);
            messageList[i].setSubstitutionData(substitutionData);

            messageList[i].setText(converter.byteArrayToString(data, offset + 112 + substitutionDataLength, textLength));
            offset += BinaryConverter.byteArrayToInt(data, offset);
            offset += BinaryConverter.byteArrayToInt(data, offset);
        }
        return messageList;
    }

    private native byte[] runCommandNative(byte[] command) throws NativeException;
    private static native byte[] runCommandNativeV5R3(byte[] command, int messageCount) throws NativeException;
    private native byte[] runProgramNative(byte[] programNameBuffer, byte[] programParameterStructure, byte[] programParameters) throws NativeException;
    private static native byte[] runProgramNativeV5R3(byte[] name, byte[] library, int numberParameters, byte[] offsetArray, byte[] programParameters, int messageCount) throws NativeException;
}
