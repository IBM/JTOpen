///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: Command.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.*;


/**
 * The Command class represents an OS/400 CL command (*CMD) object. This class uses the 
 * QCDRCMDD and QCDRCMDI system APIs to retrieve information about an OS/400 CL command.
 * <P>
 * To actually execute a CL command, see the
 * {@link com.ibm.as400.access.CommandCall CommandCall} class.
**/
public class Command
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";
  
  /**
   * Constant indicating that the multithreaded job action used by
   * the command is not to run the command, and issue an escape message instead.
   * @see #getMultithreadedJobAction
  **/  
  public static final byte ACTION_ESCAPE_MESSAGE = (byte)0xF3;

  /**
   * Constant indicating that the multithreaded job action used by
   * the command is to run the command and issue an informational message.
   * @see #getMultithreadedJobAction
  **/
  public static final byte ACTION_INFO_MESSAGE = (byte)0xF2;

  /**
   * Constant indicating that the multithreaded job action used by
   * the command is to run the command and issue no messages.
   * @see #getMultithreadedJobAction
  **/
  public static final byte ACTION_NO_MESSAGE = (byte)0xF1;
  
  /**
   * Constant indicating that the multithreaded job action used by
   * the command is specified by the QMLTTHDACN system value.
   * @see #getMultithreadedJobAction
  **/
  public static final byte ACTION_SYSTEM_VALUE = (byte)0xF0;

  /**
   * Constant indicating that the command is allowed to run in all environments.
   * @see #isAllowedToRun
  **/
  public static final int ALLOW_ALL = 7;

  /**
   * Constant indicating that the command is allowed to run in a batch job (*BATCH).
   * @see #isAllowedToRun
  **/
  public static final int ALLOW_BATCH_JOB = 4;
  
  /**
   * Constant indicating that the command is allowed to run in a batch program (*BPGM).
   * @see #isAllowedToRun
  **/
  public static final int ALLOW_BATCH_PROGRAM = 0;
  
  /**
   * Constant indicating that the command is allowed to run in a batch REXX procedure (*BREXX).
   * @see #isAllowedToRun
  **/
  public static final int ALLOW_BATCH_REXX_PROCEDURE = 5;
  
  /**
   * Constant indicating that the command can be run using QCMDEXC, QCAEXEC, or QCAPCMD (*EXEC).
   * @see #isAllowedToRun
  **/
  public static final int ALLOW_EXEC = 2;
  
  /**
   * Constant indicating that the command is allowed to run in an interactive job (*INTERACT).
   * @see #isAllowedToRun
  **/
  public static final int ALLOW_INTERACTIVE_JOB = 3;
  
  /**
   * Constant indicating that the command is allowed to run in an interactive program (*IPGM).
   * @see #isAllowedToRun
  **/
  public static final int ALLOW_INTERACTIVE_PROGRAM = 1;
  
  /**
   * Constant indicating that the command is allowed to run in an interactive REXX procedure (*IREXX).
   * @see #isAllowedToRun
  **/
  public static final int ALLOW_INTERACTIVE_REXX_PROCEDURE = 6;
  
  /**
   * Constant indicating that the command will run in all modes.
   * @see #isOperatingMode
  **/
  public static final int MODE_ALL = 13;

  /**
   * Constant indicating that the command will run in debug mode of the operating environment.
   * @see #isOperatingMode
  **/
  public static final int MODE_DEBUG = 11;
  
  /**
   * Constant indicating that the command will run in production mode of the operating environment.
   * @see #isOperatingMode
  **/
  public static final int MODE_PRODUCTION = 10;
  
  /**
   * Constant indicating that the command will run in service mode of the operating environment.
   * @see #isOperatingMode
  **/
  public static final int MODE_SERVICE = 12;

  /**
   * Constant indicating that a program is called from system state.
   * @see #USER_STATE
  **/
  public static final String SYSTEM_STATE = "*S";

  /**
   * Constant indicating that the command is threadsafe under certain conditions. See the API documentation
   * for the command to determine the conditions for threadsafety.
   * @see #THREADSAFE_NO
   * @see #THREADSAFE_YES
   * @see #getThreadSafety
  **/
  public static final byte THREADSAFE_CONDITIONAL = (byte)0xF2;

  /**
   * Constant indicating that the command is not threadsafe.
   * @see #THREADSAFE_CONDITIONAL
   * @see #THREADSAFE_YES
   * @see #getThreadSafety
  **/
  public static final byte THREADSAFE_NO = (byte)0xF0;

  /**
   * Constant indicating that the command is threadsafe.
   * @see #THREADSAFE_CONDITIONAL
   * @see #THREADSAFE_NO
   * @see #getThreadSafety
  **/
  public static final byte THREADSAFE_YES = (byte)0xF1;

  /**
   * Constant indicating that a program is called from user state.
   * @see #SYSTEM_STATE
  **/
  public static final String USER_STATE = "*U";


  private static final ProgramParameter errorCode_ = new ProgramParameter(new byte[4]);

  private AS400 system_;
  private String library_;
  private String command_;

  private String commandProcessingProgram_;
  private String sourceFile_;
  private String validityCheckProgram_;
  private String mode_;
  private String whereAllowedToRun_;
  private boolean allowLimitedUser_;
  private int maxPosParms_;
  private String promptMessageFile_;
  private String messageFile_;
  private String helpPanelGroup_;
  private String helpIdentifier_;
  private String searchIndex_;
  private String currentLibrary_;
  private String productLibrary_;
  private String promptOverrideProgram_;
  private String restricted_;
  private String description_;
  private String cppState_;
  private String vcState_;
  private String poState_;
  private int ccsid_;
  private boolean guiEnabled_;
  private byte threadsafe_;
  private byte multithreadedJobAction_;

  private String xml_;

  private boolean refreshed_ = false;
  private boolean refreshedXML_ = false;

  /**
   * Constructs a Command object.
   * @param system The server on which the command resides.
   * @param commandPath The fully qualified integrated file system path of the command,
   * e.g. "/QSYS.LIB/CRTUSRPRF.CMD"
   * @see com.ibm.as400.access.QSYSObjectPathName
  **/
  public Command(AS400 system, String commandPath)
  {
    if (system == null) throw new NullPointerException("system");
    if (commandPath == null) throw new NullPointerException("commandPath");

    QSYSObjectPathName path = new QSYSObjectPathName(commandPath);
    if (!path.getObjectType().equals("CMD"))
    {
      throw new ExtendedIllegalArgumentException("commandPath", ExtendedIllegalArgumentException.PATH_NOT_VALID);
    }
    system_ = system;
    library_ = path.getLibraryName().trim().toUpperCase();
    command_ = path.getObjectName().trim().toUpperCase();
  }

  /**
   * Constructs a Command object.
   * @param system The server on which the command resides.
   * @param library The library in which the command resides, e.g. "QSYS"
   * @param command The name of the command, e.g. "CRTUSRPRF"
  **/
  public Command(AS400 system, String library, String command)
  {
    if (system == null) throw new NullPointerException("system");
    if (library == null) throw new NullPointerException("library");
    if (command == null) throw new NullPointerException("command");

    system_ = system;
    library_ = library.trim().toUpperCase();
    command_ = command.trim().toUpperCase();
  }

  /**
   * Indicates whether or not a user with limited authorities is allowed to run this command.
   * @return true if a limited user is allowed to run this command; false otherwise.
  **/
  public boolean allowsLimitedUser() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return allowLimitedUser_;
  }

  /**
   * Returns the coded character set ID (CCSID) associated with this command.
   * It is the value of the job CCSID when this command was created.
   * @return The CCSID of the command.
  **/
  public int getCCSID() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return ccsid_;
  }

  /**
   * Returns the fully qualified integrated file system path of the program
   * that accepts parameters from this command processes this command.
   * @return The command processing program name, or *REXX if the command processing
   * program is a REXX procedure.
  **/
  public String getCommandProcessingProgram() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return commandProcessingProgram_;
  }

  /**
   * Returns the state from which the command processing program is called.
   * Possible values are:
   * <UL>
   * <LI>{@link #SYSTEM_STATE SYSTEM_STATE} - The command processing program is called from system state.
   * <LI>{@link #USER_STATE USER_STATE} - The command processing program is called from user state.
   * </UL>
   * @return The state.
  **/
  public String getCommandProcessingState() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return cppState_;
  }

  /**
   * Returns the library used as the current library during the processing of this
   * command. Special values include:
   * <UL>
   * <LI>*NOCHG - The current library does not change for the processing of this command.
   * If the current library is changed during the procesing of the command, the change
   * remains in effect after command processing is complete.
   * <LI>*CRTDFT - No current library is active during processing of the command. The
   * current library that was active before command processing began is restored when
   * processing is complete.
   * </UL>
   * @return The current library name.
  **/
  public String getCurrentLibrary() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return currentLibrary_;
  }

  /**
   * Returns the user text used to briefly describe this command and its function.
   * @return The text description, or "" if there is none.
  **/
  public String getDescription() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return description_;
  }

  /**
   * Returns the name of the general help module for the names of the help
   * identifiers for this command. Special values include:
   * <UL>
   * <LI>*NONE - No help identifier is specified.
   * <LI>*CMD - The name of the command is used.
   * </UL>
   * @return The help identifier.
  **/
  public String getHelpIdentifier() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return helpIdentifier_;
  }

  /**
   * Returns the fully integrated file system path of the help panel group in which
   * the online help information exists for this command.
   * @return The help panel group name, or *NONE if no help panel group is defined
   * for this command.
  **/
  public String getHelpPanelGroup() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return helpPanelGroup_;
  }

  /**
   * Returns the fully qualified integrated file system path of the help search index
   * used for this command.
   * @return The search index name, or *NONE if no help search index is specified.
  **/
  public String getHelpSearchIndex() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return searchIndex_;
  }

  /**
   * Returns the library for this Command object.
   * @return The library.
   * @see #getName
  **/
  public String getLibrary()
  {
    return library_;
  }

  /**
   * Returns the maximum number of parameters that can be coded in a positional
   * manner for this command.
   * @return The number of parameters, or -1 if there is no maximum positional coding
   * limit specified for this command.
  **/
  public int getMaximumPositionalParameters() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return maxPosParms_;
  }

  /**
   * Returns a MessageFile object representing the message file
   * from which messages identified on the DEP statements used to define the command
   * are retrieved.
   * @return The message file.
  **/
  public MessageFile getMessageFile() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return new MessageFile(system_, messageFile_);
  }

  /**
   * Returns the action taken when a command that is not threadsafe is called in
   * a multithreaded job. Possible values are:
   * <UL>
   * <LI>{@link #ACTION_SYSTEM_VALUE ACTION_SYSTEM_VALUE} - Use the action specified
   * in the QMLTTHDACN system value.
   * <LI>{@link #ACTION_NO_MESSAGE ACTION_NO_MESSAGE} - Run the command. Do not
   * send a message.
   * <LI>{@link #ACTION_INFO_MESSAGE ACTION_INFO_MESSAGE} - Send an informational
   * message and run the command.
   * <LI>{@link #ACTION_ESCAPE_MESSAGE ACTION_ESCAPE_MESSAGE} - Send an escape
   * message and do not run the command.
   * </UL>
   * If the threadsafe indicator is either {@link #THREADSAFE_YES THREADSAFE_YES}
   * or {@link #THREADSAFE_CONDITIONAL THREADSAFE_CONDITIONAL},
   * then the multithreaded job action will be returned as {@link #ACTION_NO_MESSAGE ACTION_NO_MESSAGE}.
   * @return The multithreaded job action for this command.
   * @see #getThreadSafety
  **/
  public byte getMultithreadedJobAction() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return multithreadedJobAction_;
  }

  
  /**
   * Returns the CL command name for this Command object.
   * @return The command name.
   * @see #getLibrary
  **/
  public String getName()
  {
    return command_;
  }


  /**
   * Returns the library that is in effect during the processing of the command.
   * Special values include:
   * <UL>
   * <LI>*NOCHG - The product library does not change for the processing of this command.
   * <LI>*NONE - There is no product library in the job's library list.
   * </UL>
   * @return The product library name.
  **/
  public String getProductLibrary() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return productLibrary_;
  }

  /**
   * Returns a MessageFile object representing the message file
   * that contains the prompt text for this command.
   * @return The message file, or null if no message file was specified for
   * the prompt text.
  **/
  public MessageFile getPromptMessageFile() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    if (promptMessageFile_ != null)
    {
      return new MessageFile(system_, promptMessageFile_);
    }
    return null;
  }

  /**
   * Returns the fully qualified integrated file system path of the program
   * that replaces default values on the prompt display with the current
   * actual values for the parameter.
   * @return The prompt override program name, or *NONE if no prompt override
   * program was specified for this command.
  **/
  public String getPromptOverrideProgram() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return promptOverrideProgram_;
  }

  /**
   * Returns the state from which the prompt override program is called.
   * Possible values are:
   * <UL>
   * <LI>{@link #SYSTEM_STATE SYSTEM_STATE} - The prompt override program is called from system state.
   * <LI>{@link #USER_STATE USER_STATE} - The prompt override program is called from user state.
   * </UL>
   * @return The state.
  **/
  public String getPromptOverrideState() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return poState_;
  }

  /**
   * Returns the version, release, and modification level to which this command is
   * restricted. If this field is blank, the command can be used in the current release.
   * This applies only to a command used in a CL program. It must match the contents of
   * the target release parameter on the Create CL Program (CRTCLPGM) command.
   * @return The release in the format "VxRxMx", or "" if the command can be used in
   * the current release.
   * @see com.ibm.as400.access.AS400#getVRM
  **/
  public String getRestrictedRelease() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return restricted_;
  }

  /**
   * Returns the fully qualified integrated file system path of the 
   * source file member that contains the command definition statements
   * used to create this command.
   * @return The source file name.
  **/
  public String getSourceFile() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return sourceFile_;
  }


  /**
   * Returns the AS400 object for this command.
   * @return The system.
  **/
  public AS400 getSystem()
  {
    return system_;
  }

  /**
   * Returns the type of threadsafety for this command; that is, whether or not this command
   * can be used safely in a multithreaded job.
   * Possible values are:
   * <UL>
   * <LI>{@link #THREADSAFE_NO THREADSAFE_NO} - This command is not threadsafe and should not be
   * used in a multithreaded job. The value for the multithreaded job action defines the action
   * to be taken by the command analyzer when the command is used in a multithreaded job.
   * <LI>{@link #THREADSAFE_YES THREADSAFE_YES} - This command is threadsafe and can be used
   * safely in a multithreaded job.
   * <LI>{@link #THREADSAFE_CONDITIONAL THREADSAFE_CONDITIONAL} - This command is threadsafe
   * under certain conditions. See the documentation for the command to determine the conditions
   * under which the command can be used safely in a multithreaded job.
   * </UL>
   * @return The threadsafety indicator for this command.
   * @see #getMultithreadedJobAction
  **/
  public byte getThreadSafety() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return threadsafe_;
  }

  /**
   * Returns the fully qualified integrated file system path of the program
   * that performs additional user-defined validity checking on the parameters
   * for this command.
   * @return The validity check program name, or *NONE if no separate user-defined
   * validity checking is done for this command.
  **/
  public String getValidityCheckProgram() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return validityCheckProgram_;
  }

  /**
   * Returns the state from which the validity check program is called.
   * Possible values are:
   * <UL>
   * <LI>{@link #SYSTEM_STATE SYSTEM_STATE} - The validity check program is called from system state.
   * <LI>{@link #USER_STATE USER_STATE} - The validity check program is called from user state.
   * </UL>
   * @return The state.
  **/
  public String getValidityCheckState() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return vcState_;
  }

  /**
   * Retrieves the XML source for this CL command.
   * @return The XML describing this command.
  **/
  public String getXML() throws AS400Exception, AS400SecurityException,
                                ErrorCompletingRequestException, IOException,
                                InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshedXML_) refreshXML();
    return xml_;
  }

  /**
   * Returns the environments in which this command is allowed to run.
   * @param environment The environment to check. Possible values are:
   * <UL>
   * <LI>{@link #ALLOW_BATCH_PROGRAM ALLOW_BATCH_PROGRAM}
   * <LI>{@link #ALLOW_INTERACTIVE_PROGRAM ALLOW_INTERACTIVE_PROGRAM}
   * <LI>{@link #ALLOW_EXEC ALLOW_EXEC}
   * <LI>{@link #ALLOW_INTERACTIVE_JOB ALLOW_INTERACTIVE_JOB}
   * <LI>{@link #ALLOW_BATCH_JOB ALLOW_BATCH_JOB}
   * <LI>{@link #ALLOW_BATCH_REXX_PROCEDURE ALLOW_BATCH_REXX_PROCEDURE}
   * <LI>{@link #ALLOW_INTERACTIVE_REXX_PROCEDURE ALLOW_INTERACTIVE_REXX_PROCEDURE}
   * <LI>{@link #ALLOW_ALL ALLOW_ALL}
   * </UL>
   * @return true if this command is allowed to run in the specified environment; false otherwise.
  **/
  public boolean isAllowedToRun(int environment) throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    switch(environment)
    {
      case ALLOW_BATCH_PROGRAM:
        return whereAllowedToRun_.charAt(0) == '1';
      case ALLOW_INTERACTIVE_PROGRAM:
        return whereAllowedToRun_.charAt(1) == '1';
      case ALLOW_EXEC:
        return whereAllowedToRun_.charAt(2) == '1';
      case ALLOW_INTERACTIVE_JOB:
        return whereAllowedToRun_.charAt(3) == '1';
      case ALLOW_BATCH_JOB:
        return whereAllowedToRun_.charAt(4) == '1';
      case ALLOW_BATCH_REXX_PROCEDURE:
        return whereAllowedToRun_.charAt(5) == '1';
      case ALLOW_INTERACTIVE_REXX_PROCEDURE:
        return whereAllowedToRun_.charAt(6) == '1';
      case ALLOW_ALL:
        return whereAllowedToRun_.startsWith("1111111");
      default:
        break;
    }
    throw new ExtendedIllegalArgumentException("environment", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
  }


  /**
   * Indicates whether or not this command is allowed to run in one or more
   * of the batch environments:
   * <UL>
   * <LI>{@link #ALLOW_BATCH_PROGRAM ALLOW_BATCH_PROGRAM}
   * <LI>{@link #ALLOW_BATCH_JOB ALLOW_BATCH_JOB}
   * <LI>{@link #ALLOW_BATCH_REXX_PROCEDURE ALLOW_BATCH_REXX_PROCEDURE}
   * </UL>
   * @return true if this command is allowed to run in one or more of the batch environments; false otherwise.
   * @see #isAllowedToRun
  **/
  public boolean isAllowedToRunBatch() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return whereAllowedToRun_.charAt(0) == '1' ||
           whereAllowedToRun_.charAt(4) == '1' ||
           whereAllowedToRun_.charAt(5) == '1';
  }

  /**
   * Indicates whether or not this command is allowed to run in one or more
   * of the interactive environments:
   * <UL>
   * <LI>{@link #ALLOW_INTERACTIVE_PROGRAM ALLOW_INTERACTIVE_PROGRAM}
   * <LI>{@link #ALLOW_INTERACTIVE_JOB ALLOW_INTERACTIVE_JOB}
   * <LI>{@link #ALLOW_INTERACTIVE_REXX_PROCEDURE ALLOW_INTERACTIVE_REXX_PROCEDURE}
   * </UL>
   * @return true if this command is allowed to run in one or more of the interactive environments; false otherwise.
   * @see #isAllowedToRun
  **/
  public boolean isAllowedToRunInteractive() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return whereAllowedToRun_.charAt(1) == '1' ||
           whereAllowedToRun_.charAt(3) == '1' ||
           whereAllowedToRun_.charAt(6) == '1';
  }

  /**
   * Indicates whether or not the command prompt panels are enabled for conversion
   * to a graphical user interface.
   * @return true if the command prompt panels are enabled for conversion to a
   * graphical user interface by including information about the panel content in
   * the 5250 data stream; false otherwise.
  **/
  public boolean isEnabledForGUI() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    return guiEnabled_;
  }


  /**
   * Returns the mode of operating environment to which the command applies.
   * @param mode The operating mode to check. Possible values are:
   * <UL>
   * <LI>{@link #MODE_PRODUCTION MODE_PRODUCTION} - Production mode.
   * <LI>{@link #MODE_DEBUG MODE_DEBUG} - Debug mode.
   * <LI>{@link #MODE_SERVICE MODE_SERVICE} - Service mode.
   * <LI>{@link #MODE_ALL MODE_ALL} - All of the above modes.
   * </UL>
   * @return true if this command applies to the specified operating mode; false otherwise.
  **/
  public boolean isOperatingMode(int mode) throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();
    switch(mode)
    {
      case MODE_PRODUCTION:
        return mode_.charAt(0) == '1';
      case MODE_DEBUG:
        return mode_.charAt(1) == '1';
      case MODE_SERVICE:
        return mode_.charAt(2) == '1';
      case MODE_ALL:
        return mode_.startsWith("111");
      default:
        break;
    }
    throw new ExtendedIllegalArgumentException("mode", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
  }


  /**
   * Refreshes the information for this Command object.
   * This method is used to perform the call to the server
   * that retrieves the command information for this CL command. That
   * information is cached internally until this method is called again.
  **/
  public void refresh() throws AS400Exception, AS400SecurityException,
                               ErrorCompletingRequestException, IOException,
                               InterruptedException, ObjectDoesNotExistException
  {
    refreshCommandInfo();
    refreshXML();
  }

  
  // Worker method.
  private void refreshCommandInfo() throws AS400Exception, AS400SecurityException,
                                    ErrorCompletingRequestException, IOException,
                                    InterruptedException, ObjectDoesNotExistException
  {
    // Call the QCDRCMDI API to get all of the information.
    ProgramParameter[] parms = new ProgramParameter[5];
    parms[0] = new ProgramParameter(335); // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(335)); // length of receiver variable
    parms[2] = new ProgramParameter(CharConverter.stringToByteArray(37, system_, "CMDI0100")); // format name
    byte[] cmdBytes = new byte[20];
    AS400Text text10 = new AS400Text(10, system_.getCcsid());
    text10.toBytes(command_.toUpperCase().trim(), cmdBytes, 0);
    text10.toBytes(library_.toUpperCase().trim(), cmdBytes, 10);
    parms[3] = new ProgramParameter(cmdBytes); // qualified command name
    parms[4] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QCDRCMDI.PGM", parms);
    pc.setThreadSafe(true);

    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }

    byte[] outputData = parms[0].getOutputData();
    CharConverter conv = new CharConverter(system_.getCcsid());
    
    String cppName = conv.byteArrayToString(outputData, 18, 10).trim();
    if (cppName.equals("*REXX"))
    {
      commandProcessingProgram_ = cppName;
    }
    else
    {
      String cppLib = conv.byteArrayToString(outputData, 28, 10).trim();
      commandProcessingProgram_ = QSYSObjectPathName.toPath(cppLib, cppName, "PGM");
    }
    
    String srcName = conv.byteArrayToString(outputData, 48, 10).trim();
    String srcLib = conv.byteArrayToString(outputData, 58, 10).trim();
    String srcMbr = conv.byteArrayToString(outputData, 68, 10).trim();
    sourceFile_ = QSYSObjectPathName.toPath(srcLib, srcName, srcMbr, "MBR");
    
    String vldName = conv.byteArrayToString(outputData, 78, 10).trim();
    if (vldName.equals("*NONE"))
    {
      validityCheckProgram_ = vldName;
    }
    else
    {
      String vldLib = conv.byteArrayToString(outputData, 88, 10).trim();
      validityCheckProgram_ = QSYSObjectPathName.toPath(vldLib, vldName, "PGM");
    }

    mode_ = conv.byteArrayToString(outputData, 98, 10); // Don't trim!

    whereAllowedToRun_ = conv.byteArrayToString(outputData, 108, 15); // Don't trim!

    allowLimitedUser_ = outputData[123] == 0xF1; // '1' for *YES, '0' for *NO

    maxPosParms_ = BinaryConverter.byteArrayToInt(outputData, 124);

    String promptName = conv.byteArrayToString(outputData, 128, 10).trim();
    if (promptName.equals("*NONE"))
    {
      promptMessageFile_ = null;
    }
    else
    {
      String promptLib = conv.byteArrayToString(outputData, 138, 10).trim();
      promptMessageFile_ = QSYSObjectPathName.toPath(promptLib, promptName, "MSGF");
    }

    String msgName = conv.byteArrayToString(outputData, 148, 10).trim();
    String msgLib = conv.byteArrayToString(outputData, 158, 10).trim();
    messageFile_ = QSYSObjectPathName.toPath(msgLib, msgName, "MSGF");

    String helpName = conv.byteArrayToString(outputData, 168, 10).trim();
    if (helpName.equals("*NONE"))
    {
      helpPanelGroup_ = helpName;
    }
    else
    {
      String helpLib = conv.byteArrayToString(outputData, 178, 10).trim();
      helpPanelGroup_ = QSYSObjectPathName.toPath(helpLib, helpName, "PNLGRP");
    }

    helpIdentifier_ = conv.byteArrayToString(outputData, 188, 10).trim();

    String searchName = conv.byteArrayToString(outputData, 198, 10).trim();
    if (searchName.equals("*NONE"))
    {
      searchIndex_ = searchName;
    }
    else
    {
      String searchLib = conv.byteArrayToString(outputData, 208, 10).trim();
      searchIndex_ = QSYSObjectPathName.toPath(searchLib, searchName, "SCHIDX");
    }

    currentLibrary_ = conv.byteArrayToString(outputData, 218, 10).trim();

    productLibrary_ = conv.byteArrayToString(outputData, 228, 10).trim();

    String pOverName = conv.byteArrayToString(outputData, 238, 10).trim();
    if (pOverName.equals("*NONE"))
    {
      promptOverrideProgram_ = pOverName;
    }
    else
    {
      String pOverLib = conv.byteArrayToString(outputData, 248, 10).trim();
      promptOverrideProgram_ = QSYSObjectPathName.toPath(pOverLib, pOverName, "PGM");
    }

    restricted_ = conv.byteArrayToString(outputData, 258, 6).trim();

    description_ = conv.byteArrayToString(outputData, 264, 50).trim();

    cppState_ = conv.byteArrayToString(outputData, 314, 2);

    vcState_ = conv.byteArrayToString(outputData, 316, 2);

    poState_ = conv.byteArrayToString(outputData, 318, 2);

    // Skipping bookshelf information

    ccsid_ = BinaryConverter.byteArrayToInt(outputData, 328);

    guiEnabled_ = outputData[332] == (byte)0xF1; // '1' means the command includes info about the panel content in the 5250 datastream

    threadsafe_ = outputData[333]; // '1' means it is threadsafe, '2' means threadsafe-conditional

    multithreadedJobAction_ = outputData[334];

    refreshed_ = true;
  }


  // Worker method.  
  private void refreshXML() throws AS400Exception, AS400SecurityException,
                                   ErrorCompletingRequestException, IOException,
                                   InterruptedException, ObjectDoesNotExistException
  {
    ProgramParameter[] parms = new ProgramParameter[6];
    byte[] cmdBytes = new byte[20];
    AS400Text text10 = new AS400Text(10, system_.getCcsid());
    text10.toBytes(command_.toUpperCase().trim(), cmdBytes, 0);
    text10.toBytes(library_.toUpperCase().trim(), cmdBytes, 10);

    // Make 2 program calls. The first call tells us how much
    // data is coming back. The second call actually retrieves the data.
    parms[0] = new ProgramParameter(cmdBytes); // qualified command name
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(8)); // destination, AKA length of receiver variable
    parms[2] = new ProgramParameter(CharConverter.stringToByteArray(37, system_, "DEST0100")); // destination format name
    parms[3] = new ProgramParameter(8); // receiver variable
    parms[4] = new ProgramParameter(CharConverter.stringToByteArray(37, system_, "CMDD0100")); // receiver format name
    parms[5] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QCDRCMDD.PGM", parms);
    pc.setThreadSafe(true);

    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }

    byte[] outputData = parms[3].getOutputData();
    int bytesReturned = BinaryConverter.byteArrayToInt(outputData, 0);
    int bytesAvailable = BinaryConverter.byteArrayToInt(outputData, 4);
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(bytesAvailable+8));
    try
    {
      parms[3].setOutputDataLength(bytesAvailable+8);
    }
    catch(java.beans.PropertyVetoException pve) {}
    
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }
    
    outputData = parms[3].getOutputData();
    bytesReturned = BinaryConverter.byteArrayToInt(outputData, 0);
    bytesAvailable = BinaryConverter.byteArrayToInt(outputData, 4);
    byte[] xmlResults = new byte[bytesReturned];
    System.arraycopy(outputData, 8, xmlResults, 0, bytesReturned);
    ConvTable conv1208 = ConvTable.getTable(1208, null);
    xml_ = conv1208.byteArrayToString(outputData, 8, bytesReturned, 0);
    refreshedXML_ = true;
  }


  /**
   * Returns a String representation of this Command, in the form
   * of the command's fully qualified integrated file system path.
   * @return The path.
  **/
  public String toString()
  {
    return QSYSObjectPathName.toPath(library_, command_, "CMD");
  }
}
