///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  RemoteCommandImplNative.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1999-2007 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
//
// @A1 - 9/18/2007 - Changes to follow proxy command chain.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;


// The RemoteCommandImplNative class is the native implementation of CommandCall and ProgramCall.
class RemoteCommandImplNative extends RemoteCommandImplRemote
{
  private static final String CLASSNAME = "com.ibm.as400.access.RemoteCommandImplNative";
  static
  {
    if (Trace.traceOn_) Trace.logLoadPath(CLASSNAME);
  }

    static
    {
        try{
            System.load("/QSYS.LIB/QYJSPART.SRVPGM");
        }catch(Throwable e)
        {
            Trace.log(Trace.ERROR, "Error loading QYJSPART service program:", e); //may be that it is already loaded in multiple .war classloader
        }
    }

    // Report whether the RemoteCommandImpl object is a native object.
    public boolean isNative()
    {
      return true;
    }

    // Connects to the server.
    // @param threadSafety  The assumed thread safety of the command/program.
    protected void open(Boolean threadSafety) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
      if (threadSafety == LOOKUP_THREADSAFETY || threadSafety == OFF_THREAD) {
        openOffThread();
      }
      else {
        openOnThread();
      }
    }

    protected void openOnThread() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Native implementation object open.");

      // If converter was not set with a user override ccsid, set converter to job ccsid.
      if (!ccsidIsUserOveride_ && (converter_ == null))
      {
        converter_ = ConverterImplRemote.getConverter(system_.getCcsid(), system_);
      }
      if (AS400.nativeVRM.vrm_ >= 0x00050300)
      {
        if (AS400.nativeVRM.vrm_ >= 0x00060100)
        {
          serverDataStreamLevel_ = 10;
          if (unicodeConverter_ == null) {
            unicodeConverter_ = ConverterImplRemote.getConverter(1200, system_);
          }
        }
        else
        {
          serverDataStreamLevel_ = 7;
        }
      }

      // Set the secondary language library on the server.
      if (system_.isMustAddLanguageLibrary() &&
          !system_.isSkipFurtherSettingOfLanguageLibrary()) // see if we should try
      {
        // Note: If we were going through the Remote Command Host Server, the host server would set the secondary language library for us.
        // Since we're not using the host server, we need to handle this ourselves.
        // We need to do this on every open, since several different threads may be using this RemoteCommandImpl object.

        // Retrieve the name of the secondary language library (if any).
        String secLibName = retrieveSecondaryLanguageLibName();  // never returns null
        // Set the NLV on server to match the client's locale.
        if (secLibName.length() != 0)
        {
          setNlvOnServer(secLibName);
        }
        // Retain result, to avoid repeated library lookups for same system object.
        system_.setLanguageLibrary(secLibName);
        // Set to non-null, to indicate we already looked-up the value.
      }

    }


    // Retrieves the secondary language library (if any).
    // If fail to retrieve library name, or name is blank, returns "".
    private String retrieveSecondaryLanguageLibName()
    {
      String secLibName = system_.getLanguageLibrary();
      if (secLibName == null)  // 'null' implies not already looked-up
      {
        String clientNLV = system_.getNLV(); // NLV of client (based on locale)
        try
        {
          int ccsid = system_.getCcsid();
          ConvTable conv = ConvTable.getTable(ccsid, null);

          ProgramParameter[] parameterList = new ProgramParameter[6];
          int len = 108+10; // length of PRDR0100, plus first 10 bytes of PRDR0200
          parameterList[0] = new ProgramParameter(len); // receiver variable - PRDR0100 plus first 10 bytes of PRDR0200
          parameterList[1] = new ProgramParameter(BinaryConverter.intToByteArray(len)); // length of receiver variable
          parameterList[2] = new ProgramParameter(conv.stringToByteArray("PRDR0200")); // format name

          byte[] productInfo = new byte[36];  // product information
          AS400Text text4 = new AS400Text(4, ccsid, system_);
          AS400Text text6 = new AS400Text(6, ccsid, system_);
          AS400Text text7 = new AS400Text(7, ccsid, system_);
          AS400Text text10 = new AS400Text(10, ccsid, system_);
          text7.toBytes("*OPSYS", productInfo, 0);  // product ID
          text6.toBytes("*CUR", productInfo, 7);  // release level
          text4.toBytes("0000", productInfo, 13);  // product option
          text10.toBytes(clientNLV, productInfo, 17); // load ID (specifies desired NLV)
          BinaryConverter.intToByteArray(36, productInfo, 28);  // length of product information parm
          BinaryConverter.intToByteArray(ccsid, productInfo, 32);  // ccsid for returned directory
          parameterList[3] = new ProgramParameter(productInfo); // product information
          parameterList[4] = new ProgramParameter(new byte[4]); // error code
          parameterList[5] = new ProgramParameter(conv.stringToByteArray("PRDI0200")); // product information format name

          // Call QSZRTVPR (Change System Library List) to add the library for the secondary language.
          // Note: QSZRTVPR is documented as non-threadsafe. However, the API owner has indicated that this API will never alter the state of the system, and that it cannot damage the system; so it can safely be called on-thread.
          boolean succeeded = runProgramOnThread("QSYS", "QSZRTVPR", parameterList, AS400Message.MESSAGE_OPTION_UP_TO_10, true);
          // Note: This method is only called from within open().
          // The final parm indicates that the on-thread open() has already been done (on this thread).

          if (!succeeded)
          {
            Trace.log(Trace.WARNING, "Unable to retrieve secondary language library name for NLV " + clientNLV, new AS400Exception(messageList_));
          }
          else
          {
            byte[] outputData = parameterList[0].getOutputData();
            int offsetToAddlInfo = BinaryConverter.byteArrayToInt(outputData, 84);
            secLibName = conv.byteArrayToString(outputData, offsetToAddlInfo, 10).trim();
            if (secLibName.length() == 0) {
              Trace.log(Trace.WARNING, "Unable to retrieve secondary language library name for NLV " + clientNLV + ": Blank library name returned.");
            }
          }
        }
        catch (Throwable t) {
          Trace.log(Trace.WARNING, "Unable to retrieve secondary language library name for NLV " + clientNLV, t);
        }
      }

      return (secLibName == null ? "" : secLibName);
    }

    // Sets the NLV (for the current thread) on the server, so that system msgs are returned in correct language.
    private void setNlvOnServer(String secondaryLibraryName)
    {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Native implementation object setting national language for messages.");
      try
      {
        // Call CHGSYSLIBL (Change System Library List) to add the library for the secondary language.
        // Note: According to the spec, CHGSYSLIBL "changes the system portion of the library list for the current thread".
        // Prior to V6R1, CHGSYSLIBL is documented as non-threadsafe.  However, the CL owner has indicated that this CL has actually been threadsafe all along, and that it cannot damage the system; so it can safely be called on-thread.
        // At worst, if system value QMLTTHDACN == 3, the system will simply refuse to execute the command.  In which case, the secondary language library won't get added.
        String cmd = "QSYS/CHGSYSLIBL LIB("+secondaryLibraryName+") OPTION(*ADD)";
        boolean succeeded = runCommandOnThread(cmd, AS400Message.MESSAGE_OPTION_UP_TO_10, true);
        // Note: This method is only called from within open().
        // The final parm indicates that the on-thread open() has already been done (on this thread).

        if (!succeeded)
        {
          if (messageList_.length !=0)
          {
            if (messageList_[0].getID().equals("CPF2103")) // lib is already in list
            {
              // Tolerate this error.  It means that we're good to go.
              // If this is the very first native open() for this system_, set flag to indicate that the lib is already in list by default.  This will eliminate clutter in the job log, from subsequent attempts to set it.
              if (system_.getLanguageLibrary() == null) { // null implies first native open
                system_.setSkipFurtherSettingOfLanguageLibrary(); // don't keep trying on subsequent open's
              }
            }
            else if (messageList_[0].getID().equals("CPD0032")) // not auth'd to call CHGSYSLIBL
            {
              system_.setSkipFurtherSettingOfLanguageLibrary(); // don't keep trying on subsequent open's
              Trace.log(Trace.DIAGNOSTIC, "Profile " + system_.getUserId() + " not authorized to use CHGSYSLIBL to add secondary language library " + secondaryLibraryName + " to liblist.");
              // Note: The Remote Command Host Server runs this command under greater authority.
            }
            else if (messageList_[0].getID().equals("CPF2110")) // library not found
            {
              system_.setSkipFurtherSettingOfLanguageLibrary();  // don't keep trying on subsequent open's
              Trace.log(Trace.WARNING, "Secondary language library " + secondaryLibraryName + " was not found.");
            }
            else
            {
              Trace.log(Trace.ERROR, "Unable to add secondary language library " + secondaryLibraryName + " to library list.", new AS400Exception(messageList_));
            }
          }
          else  // no system messages returned
          {
            Trace.log(Trace.WARNING, "Unable to add secondary language library " + secondaryLibraryName + " to library list.");
          }
        }
      }
      catch (Throwable t)
      {
        Trace.log(Trace.WARNING, "Failed to add secondary language library " + secondaryLibraryName + " to library list.", t);
      }
    }


    // This method is reserved for use by other Impl classes in this package.
    public boolean runCommand(String command) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
      // The caller didn't specify whether to call the command on- or off-thread.
      // Base the decision on the setting the "threadSafe" system property.

      if (shouldRunOnThread(command)) {
        return runCommandOnThread(command, MESSAGE_OPTION_DEFAULT, false);
      }
      else {
        return runCommandOffThread(command, MESSAGE_OPTION_DEFAULT);
      }
    }


    // Runs the command.
    // @param threadSafety  The assumed thread safety of the command/program.
    // @return  true if command is successful; false otherwise.
    public boolean runCommand(String command, Boolean threadSafety, int messageOption) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
      boolean runOnThread;

      if (threadSafety == ON_THREAD) {
        runOnThread = true;
      }
      else if (threadSafety == OFF_THREAD) {
        runOnThread = false;
      }
      else // threadSafety == LOOKUP_THREADSAFETY
      {
        // Look up the command's indicated threadsafety on the system.
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "LOOKING-UP thread safety of command: " + command);
        runOnThread = (getThreadsafeIndicator(command) == CommandCall.THREADSAFE_YES);
      }

      if (runOnThread) {
        return runCommandOnThread(command, MESSAGE_OPTION_DEFAULT, false);
      }
      else {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Delegating runCommand() to super class.");
        return runCommandOffThread(command, messageOption);
      }
    }

    // Runs the command.
    // @return  true if command is successful; false otherwise.
    private boolean runCommandOnThread(String command, int messageOption, boolean currentlyOpeningOnThisThread) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        if (Trace.traceOn_)
        {
          Trace.log(Trace.INFORMATION, "Native implementation running command: " + command);
          Trace.log(Trace.DIAGNOSTIC, "Running command ON-THREAD: " + command);
        }
        if (!currentlyOpeningOnThisThread) openOnThread();

        if (AS400.nativeVRM.vrm_ >= 0x00060100)
        {
            return runCommandOnThread(unicodeConverter_.stringToByteArray(command), messageOption, 1200);
        }
        return runCommandOnThread(converter_.stringToByteArray(command), messageOption, 0);
    }

    public boolean runCommand(byte[] commandAsBytes, String commandAsString) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
      // The caller didn't specify whether to call the command on- or off-thread.
      // Base the decision on the setting the "threadSafe" system property.

      if (shouldRunOnThread(commandAsString)) {
        openOnThread();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Running command ON-THREAD: " + commandAsString);
        return runCommandOnThread(commandAsBytes, MESSAGE_OPTION_DEFAULT, 0);
      }
      else {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Running command OFF-THREAD: " + commandAsString);
        return runCommandOffThread(commandAsBytes, MESSAGE_OPTION_DEFAULT, 0);
      }
    }

    private boolean runCommandOnThread(byte[] commandBytes, int messageOption, int ccsid) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
        byte[] swapToPH = new byte[12];
        byte[] swapFromPH = new byte[12];
        boolean didSwap = system_.swapTo(swapToPH, swapFromPH);
        if (priorCallWasOnThread_ == OFF_THREAD)
        {
          if (Trace.traceOn_) Trace.log(Trace.WARNING, "Prior call was off-thread, but this call is on-thread, so different job.");
        }
        priorCallWasOnThread_ = ON_THREAD;

        try
        {
            if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "Invoking native method.");
            if (AS400.nativeVRM.vrm_ < 0x00050300)
            {
                try
                {
                    byte[] replyBytes = runCommandNative(commandBytes);
                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Native reply bytes:", replyBytes);

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
                    byte[] replyBytes = AS400.nativeVRM.vrm_ < 0x00060100 ? runCommandNativeV5R3(commandBytes, messageOption) : NativeMethods.runCommand(commandBytes, ccsid, messageOption);
                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Native reply bytes:", replyBytes);

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

    public boolean runProgram(String library, String name, ProgramParameter[] parameterList) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
      // The caller didn't specify whether to call the command on- or off-thread.
      // Base the decision on the setting the "threadSafe" system property.

      String property = ProgramCall.getThreadSafetyProperty();
      if (property != null && property.equals("true")) {
        // call the program on-thread
        return runProgramOnThread(library, name, parameterList, MESSAGE_OPTION_DEFAULT, false);
      }
      else {
        // call the program off-thread
        return runProgramOffThread(library, name, parameterList, MESSAGE_OPTION_DEFAULT);
      }
    }

    public boolean runProgram(String library, String name, ProgramParameter[] parameterList, Boolean threadSafety, int messageOption) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
      // Note: We don't have a way to look up the thread safety of programs.
      if (threadSafety == ON_THREAD) {
        return runProgramOnThread(library, name, parameterList, messageOption, false);
      }
      else {
        return runProgramOffThread(library, name, parameterList, messageOption);
      }
    }

    // Run the program.
    protected boolean runProgramOnThread(String library, String name, ProgramParameter[] parameterList, int messageOption, boolean currentlyOpeningOnThisThread) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_)
        {
          Trace.log(Trace.INFORMATION, "Native implementation running program: " + library + "/" + name);
          Trace.log(Trace.DIAGNOSTIC, "Running program ON-THREAD: " + library + "/" + name);
        }
        if (priorCallWasOnThread_ == OFF_THREAD)
        {
          if (Trace.traceOn_) Trace.log(Trace.WARNING, "Prior call was off-thread, but this call is on-thread, so different job.");
        }
        priorCallWasOnThread_ = ON_THREAD;

        // Run the program on-thread.
        if (!currentlyOpeningOnThisThread) openOnThread();

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

            if (Trace.traceOn_)
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
                if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "Invoking native method.");
                byte[] replyBytes = runProgramNative(programNameBuffer, programParameterStructure, programParameters);
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Native reply bytes:", replyBytes);

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
                if (messageList_.length == 0) return false;

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

            if (Trace.traceOn_)
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
                if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "Invoking native method.");
                byte[] replyBytes = AS400.nativeVRM.vrm_ < 0x00060100 ? runProgramNativeV5R3(nameBytes, libraryBytes, parameterList.length, offsetArray, programParameters, messageOption) : NativeMethods.runProgram(nameBytes, libraryBytes, parameterList.length, offsetArray, programParameters, messageOption);
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Native reply bytes:", replyBytes);

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
                if (messageList_.length == 0) return false;

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
            int helpLength = BinaryConverter.byteArrayToInt(data, offset + 96);

            byte[] substitutionData = new byte[substitutionDataLength];
            System.arraycopy(data, offset + 112, substitutionData, 0, substitutionDataLength);
            messageList[i].setSubstitutionData(substitutionData);

            messageList[i].setText(converter.byteArrayToString(data, offset + 112 + substitutionDataLength, textLength));
            messageList[i].setHelp(converter.byteArrayToString(data, offset + 112 + substitutionDataLength + textLength, helpLength));

            offset += BinaryConverter.byteArrayToInt(data, offset);
            offset += BinaryConverter.byteArrayToInt(data, offset);
        }
        return messageList;
    }


    // Determines whether or not the command should be called on-thread.
    private final boolean shouldRunOnThread(String command) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
    {
      boolean runOnThread;

      // Check the threadSafe property, and apply it if set.
      String property = CommandCall.getThreadSafetyProperty();

      if ((property == null) || (property.equals("false"))) {
        runOnThread = false;
      }
      else if (property.equals("true")) {
        runOnThread = true;
      }
      else if (property.equals("lookup")) {
        // Look it up on the system.
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "LOOKING-UP thread safety of command: " + command);
        runOnThread = (getThreadsafeIndicator(command) == CommandCall.THREADSAFE_YES);
      }
      else {
        runOnThread = false;
        // Assume the utility method has logged a warning about unrecognized property value.
      }
      return runOnThread;
    }

    private native byte[] runCommandNative(byte[] command) throws NativeException;
    private static native byte[] runCommandNativeV5R3(byte[] command, int messageOption) throws NativeException;
    private native byte[] runProgramNative(byte[] programNameBuffer, byte[] programParameterStructure, byte[] programParameters) throws NativeException;
    private static native byte[] runProgramNativeV5R3(byte[] name, byte[] library, int numberParameters, byte[] offsetArray, byte[] programParameters, int messageOption) throws NativeException;
}
