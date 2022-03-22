///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: Command.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
//
// @A1 - 9/18/2007 - Changes to follow proxy command chain
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

import java.io.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.util.*;
import org.xml.sax.InputSource;
import javax.xml.parsers.*;
import org.xml.sax.SAXException;

/**
 * Represents information about a CL command (*CMD) object on the system.
 * <P>
 * To actually execute a CL command, use the
 * {@link com.ibm.as400.access.CommandCall CommandCall} class.
 * <P>
 * To generate HTML help for a CL command, see the
 * {@link com.ibm.as400.util.CommandHelpRetriever CommandHelpRetriever} utility.
 *
**/
public class Command implements Serializable
{
  // NOTE:  This class uses the QCDRCMDD and QCDRCMDI system APIs to retrieve information about a CL command.

  static final long serialVersionUID = 6L;

  /**
   * Constant indicating that the multithreaded job action used by
   * the command is not to run the command, but issue an escape message instead.
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
   * Constant indicating that the command is allowed to run using QCMDEXC, QCAEXEC, or QCAPCMD (*EXEC).
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
  private String path_;

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
  private String xml2_;
  private transient PanelGroupHelpIdentifier[] helpIDs_;
  private String xmlProductLibrary_;
  private String xmlPanelGroup_;
  private String xmlHelpText_;

  private boolean refreshed_ = false;
  private boolean refreshedXML_ = false;
  private boolean refreshedXML2_ = false;
  private boolean refreshedHelpIDs_ = false;
  private boolean refreshedHelpText_ = false;
  private boolean refreshedParsedXML_ = false;
  private boolean loadedDescription_ = false;
  private boolean detectedMissingPTF_ = false; // V5R4 system is missing PTF SI29629 (5722SS1)


  // List of property change event bean listeners.
  private transient PropertyChangeSupport propertyChangeListeners_;


  /**
   * Constructs a Command object. The system and command path must be set
   * before this object can be used.
   * @see #setPath
   * @see #setSystem
  **/
  public Command()
  {
//    initializeTransient();
  }


  /**
   * Constructs a Command object.
   * @param system The system on which the command resides.
   * @param path The fully qualified integrated file system path of the command,
   * e.g. "/QSYS.LIB/CRTUSRPRF.CMD".
   * @see com.ibm.as400.access.QSYSObjectPathName
  **/
  public Command(AS400 system, String path)
  {
    if (system == null)
      throw new NullPointerException("system");

    if (path == null)
      throw new NullPointerException("path");

    QSYSObjectPathName.validatePath(path, "CMD");

    system_ = system;
    path_ = path;

//    initializeTransient();
  }

  /**
   * Called from CommandList.
  **/
  Command(AS400 system, String path, String description)
  {
    system_ = system;
    path_ = path;
    description_ = description;
    loadedDescription_ = true;
  }


  /**
   *  Adds a PropertyChangeListener.  The specified PropertyChangeListener's <b>propertyChange</b> method will be called each time the value of any bound property is changed.
   *  The PropertyChangeListener object is added to a list of PropertyChangeListeners managed by this Command.  It can be removed with removePropertyChangeListener.
   *
   *  @param  listener  The PropertyChangeListener.
  **/
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
    synchronized(this)
    {
      if (propertyChangeListeners_ == null) propertyChangeListeners_ = new PropertyChangeSupport(this);
    }
    propertyChangeListeners_.addPropertyChangeListener(listener);
  }


  /**
   * Indicates whether or not a user with limited authorities is allowed to run this command.
   * @return true if a limited user is allowed to run this command; false otherwise.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public boolean allowsLimitedUser() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

    return allowLimitedUser_;
  }

  /**
   * Returns the coded character set ID (CCSID) associated with this command.
   * It is the value of the job CCSID when this command was created.
   * @return The CCSID of the command.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public int getCCSID() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

    return ccsid_;
  }

  /**
   * Returns the fully qualified integrated file system path of the program
   * that accepts parameters from this command processes this command.
   * @return The command processing program name, or *REXX if the command processing
   * program is a REXX procedure.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public String getCommandProcessingProgram() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

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
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public String getCommandProcessingState() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

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
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public String getCurrentLibrary() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

    return currentLibrary_;
  }

  /**
   * Returns the user text used to briefly describe this command and its function.
   * @return The text description, or "" if there is none.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public String getDescription() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_ && !loadedDescription_)
      refreshCommandInfo();

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
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public String getHelpIdentifier() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

    return helpIdentifier_;
  }

  /**
   * Returns the fully integrated file system path of the help panel group in which
   * the online help information exists for this command.
   * @return The help panel group, or null if no help panel group is defined
   * for this command.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public PanelGroup getHelpPanelGroup() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

    if (helpPanelGroup_.equals("*NONE")) return null;
    return new PanelGroup(system_, helpPanelGroup_);
  }

  /**
   * Returns the fully qualified integrated file system path of the help search index
   * used for this command.
   * @return The search index name, or *NONE if no help search index is specified.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public String getHelpSearchIndex() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

    return searchIndex_;
  }

  /**
   * Returns the maximum number of parameters that can be coded in a positional
   * manner for this command.
   * @return The number of parameters, or -1 if there is no maximum positional coding
   * limit specified for this command.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public int getMaximumPositionalParameters() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

    return maxPosParms_;
  }

  /**
   * Returns a MessageFile object representing the message file
   * from which messages identified on the DEP statements used to define the command
   * are retrieved.
   * @return The message file.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public MessageFile getMessageFile() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

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
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
   * @see #getThreadSafety
  **/
  public byte getMultithreadedJobAction() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

    return multithreadedJobAction_;
  }


  /**
   * Returns the path name of this Command object.
   * @return The path name, or null if the path is not set.
   * @see #setPath
  **/
  public String getPath()
  {
    return path_;
  }


  /**
   * Returns the library that is in effect during the processing of the command.
   * Special values include:
   * <UL>
   * <LI>*NOCHG - The product library does not change for the processing of this command.
   * <LI>*NONE - There is no product library in the job's library list.
   * </UL>
   * @return The product library name.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public String getProductLibrary() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

    return productLibrary_;
  }

  /**
   * Returns a MessageFile object representing the message file
   * that contains the prompt text for this command.
   * @return The message file, or null if no message file was specified for
   * the prompt text.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public MessageFile getPromptMessageFile() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

    if (promptMessageFile_ != null)
      return new MessageFile(system_, promptMessageFile_);

    return null;
  }

  /**
   * Returns the fully qualified integrated file system path of the program
   * that replaces default values on the prompt display with the current
   * actual values for the parameter.
   * @return The prompt override program name, or *NONE if no prompt override
   * program was specified for this command.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public String getPromptOverrideProgram() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

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
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public String getPromptOverrideState() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

    return poState_;
  }

  /**
   * Returns the version, release, and modification level to which this command is
   * restricted. If this field is blank, the command can be used in the current release.
   * This applies only to a command used in a CL program. It must match the contents of
   * the target release parameter on the Create CL Program (CRTCLPGM) command.
   * @return The release in the format "VxRxMx", or "" if the command can be used in
   * the current release.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
   * @see com.ibm.as400.access.AS400#getVRM
  **/
  public String getRestrictedRelease() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

    return restricted_;
  }

  /**
   * Returns the fully qualified integrated file system path of the
   * source file member that contains the command definition statements
   * used to create this command.
   * @return The source file name, or null if the source file is not known.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public String getSourceFile() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

    return sourceFile_;
  }


  /**
   * Returns the system object for this command.
   * @return The system, or null if the system is not set.
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
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
   * @see #getMultithreadedJobAction
  **/
  public byte getThreadSafety() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

    return threadsafe_;
  }

  /**
   * Returns the fully qualified integrated file system path of the program
   * that performs additional user-defined validity checking on the parameters
   * for this command.
   * @return The validity check program name, or *NONE if no separate user-defined
   * validity checking is done for this command.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public String getValidityCheckProgram() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

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
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public String getValidityCheckState() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

    return vcState_;
  }

  /**
   * Returns the API string for the "where allowed to run" field.
   * This method is externalized for those classes that need the entire
   * field. Simpler access to this data can be achieved by using the
   * following methods:
   * <UL>
   * <LI>{@link #isAllowedToRun isAllowedToRun(mode)}
   * <LI>{@link #isAllowedToRunBatch isAllowedToRunBatch}
   * <LI>{@link #isAllowedToRunInteractive isAllowedToRunInteractive}
   * </UL>
   * @return The "where allowed to run" field.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public String getWhereAllowedToRun() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refreshCommandInfo();

    return whereAllowedToRun_;
  }


  /**
   * Retrieves the XML source for this CL command.
   * @return The XML describing this command.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public String getXML() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshedXML_)
      refreshXML(true);

    return xml_;
  }

  /**
   * Retrieves the extended XML source for this CL command. This returns more
   * information than {@link #getXML getXML()}.
   * <P>
   * Note: This method uses the CMDD0200 format on the QCDRCMDD API.
   * If the system does not support this format, an AS400Exception will be
   * thrown with a message ID of CPF3C21, and you should use the getXML() method instead.
   * @return The XML describing this command.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
   * @see #getXML
  **/
  public String getXMLExtended() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshedXML2_)
      refreshXML(false);

    return xml2_;
  }

  /**
   * Parses the XML source for this CL command and returns
   * the help identifiers.
   * @return The help identifiers, or null if no panel group was found in the XML source.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
   * @throws SAXException  If SAX error occurs
   * @throws ParserConfigurationException  If there is an error in the parser configuration
  **/
  public PanelGroupHelpIdentifier[] getXMLHelpIdentifiers() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException, SAXException, ParserConfigurationException
  {
    if (!refreshedHelpIDs_)
      refreshHelpIDs();
    return helpIDs_;
  }

  /**
   * Parses the XML source for this CL command and returns
   * the product library.
   * @return The product library, or null if no panel group was found in the XML source.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
   * @throws SAXException  If SAX error occurs
   * @throws ParserConfigurationException  If there is an error in the parser configuration
  **/
  public String getXMLProductLibrary() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException, SAXException, ParserConfigurationException
  {
    if (!refreshedParsedXML_)
      refreshParsedXML();
    return xmlProductLibrary_;
  }

  /**
   * Parses the XML source for this CL command and returns
   * the name of the panel group.
   * @return The panel group, or null if no panel group was found in the XML source.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
   * @throws SAXException  If SAX error occurs
   * @throws ParserConfigurationException  If there is an error in the parser configuration
  **/
  public String getXMLPanelGroup() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException, SAXException, ParserConfigurationException
  {
    if (!refreshedParsedXML_)
      refreshParsedXML();
    return xmlPanelGroup_;
  }

  /**
   * Parses the XML source for this CL command and returns
   * the help text.
   * @return The help text, or null if no panel group was found in the XML source.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
   * @throws SAXException  If SAX error occurs
   * @throws ParserConfigurationException  If there is an error in the parser configuration
  **/
  public String getXMLHelpText() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException, SAXException, ParserConfigurationException
  {
    if (!refreshedHelpText_)
      refreshHelpText(null);
    return xmlHelpText_;
  }

  /**
   * Parses the XML source for this CL command and returns
   * the help text. The XML help text generated by this method is not cached like
   * the text generated by {@link #getXMLHelpText getXMLHelpText()}.
   * @param panelGroup The panel group used to generate the help text, instead of the Command's defined panel group.
   * @return The help text.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
   * @throws SAXException  If SAX error occurs
   * @throws ParserConfigurationException  If there is an error in the parser configuration
   * @see #getXMLPanelGroup
  **/
  public synchronized String getXMLHelpText(PanelGroup panelGroup) throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException, SAXException, ParserConfigurationException
  {
    if (panelGroup == null) throw new NullPointerException("panelGroup");
    // Don't preserve this one.
    String oldText = xmlHelpText_;
    refreshHelpText(panelGroup);
    String newText = xmlHelpText_;
    xmlHelpText_ = oldText;
    return newText;
  }

  private synchronized void refreshHelpText(PanelGroup pg) throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException, SAXException, ParserConfigurationException
  {
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Command.refreshHelpText("+pg+").");
    xmlHelpText_ = null;
    String pGroup = (pg == null ? getXMLPanelGroup() : pg.getPath());
    String prdLib = getXMLProductLibrary();
    boolean added = false;
    if (pGroup != null)
    {
      if (prdLib != null)
      {
        prdLib = prdLib.trim().toUpperCase();
        // Add the product library to the library list
        // otherwise the API used by PanelGroup won't find the help text.
        // First, check to see if it's there.
        Job job = new Job(system_); // Current job
        String[] userLibraries = job.getUserLibraryList();
        String[] sysLibraries = job.getSystemLibraryList();
        String curLibrary = job.getCurrentLibrary();
        boolean exists = false;
        if (curLibrary.trim().equalsIgnoreCase(prdLib))
        {
          exists = true;
        }
        for (int i=0; i<userLibraries.length && !exists; ++i)
        {
          if (userLibraries[i].trim().equalsIgnoreCase(prdLib))
          {
            exists = true;
          }
        }
        for (int i=0; i<sysLibraries.length && !exists; ++i)
        {
          if (sysLibraries[i].trim().equalsIgnoreCase(prdLib))
          {
            exists = true;
          }
        }
        if (!exists)
        {
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Command.refreshHelpText: Adding "+prdLib+" to library list.");
          // We have to try to add it.
          String addlible = "ADDLIBLE LIB("+prdLib+")";  // not threadsafe
          CommandCall cc = new CommandCall(system_, addlible);
          added = cc.run();
        }
      }

      PanelGroupHelpIdentifier[] ids = getXMLHelpIdentifiers();
      String[] helpIDs = new String[ids.length];
      for (int i=0; i<ids.length; ++i) helpIDs[i] = ids[i].getName();
      PanelGroup panelGroup = new PanelGroup(system_, pGroup);
      String helpResults = panelGroup.getHelpText(helpIDs);

      xmlHelpText_ = helpResults;

      // Remove the product library from the library list if we added it.
      if (added)
      {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "CommandHelpRetriever: Removing "+prdLib+" from library list.");
        String rmvlible = "RMVLIBLE LIB("+prdLib+")";  // not threadsafe
        CommandCall cc = new CommandCall(system_, rmvlible);
        cc.run();
      }
    }

    refreshedHelpText_ = true;
  }


  private synchronized void refreshParsedXML() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException, SAXException, ParserConfigurationException
  {
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Command.refreshParsedXML().");
    xmlPanelGroup_ = null;
    xmlProductLibrary_ = null;
    String xml = null ;
    try {
     xml = getXML();
    CommandHelpHandler handler = new CommandHelpHandler();
    //SAXParser parser = new SAXParser();
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(false);
    SAXParser parser = factory.newSAXParser();
    //parser.setFeature("http://xml.org/sax/features/namespaces", false);
    //parser.setContentHandler(handler);
    parser.parse(new InputSource(new StringReader(xml)), handler);
    String panelGroup = handler.getPanelGroup();
    String prodLib = handler.getProductLibrary();
    boolean added = false;
    if (panelGroup != null) xmlPanelGroup_ = panelGroup.trim();
    if (prodLib != null && !prodLib.equalsIgnoreCase("__NONE")) xmlProductLibrary_ = prodLib.trim();

    refreshedParsedXML_ = true;
    } catch (SAXException e) {

      if (Trace.traceOn_) {
          Trace.log(Trace.DIAGNOSTIC, "Exception in Command.refreshParsedXML().");
          Trace.log(Trace.DIAGNOSTIC, e);
          Trace.log(Trace.DIAGNOSTIC, "path is "+path_);
          Trace.log(Trace.DIAGNOSTIC, "XML is "+xml);
      }
      throw e;
    }
  }


  private synchronized void refreshHelpIDs() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException, SAXException, ParserConfigurationException
  {
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Command.refreshHelpIDs().");
    helpIDs_ = null;
    String xml = getXML();
    CommandHelpHandler handler = new CommandHelpHandler();
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(false);
    SAXParser parser = factory.newSAXParser();
    parser.parse(new InputSource(new StringReader(xml)), handler);

    // Add the product library to the library list
    // otherwise the API used by PanelGroup won't find the help text.
    // First, check to see if it's there.
    boolean added = false;
    String prdLib = getXMLProductLibrary();
    if (prdLib != null)
    {
      Job job = new Job(system_); // Current job
      String[] userLibraries = job.getUserLibraryList();
      String[] sysLibraries = job.getSystemLibraryList();
      String curLibrary = job.getCurrentLibrary();
      boolean exists = false;
      if (curLibrary.trim().equalsIgnoreCase(prdLib))
      {
        exists = true;
      }
      for (int i=0; i<userLibraries.length && !exists; ++i)
      {
        if (userLibraries[i].trim().equalsIgnoreCase(prdLib))
        {
          exists = true;
        }
      }
      for (int i=0; i<sysLibraries.length && !exists; ++i)
      {
        if (sysLibraries[i].trim().equalsIgnoreCase(prdLib))
        {
          exists = true;
        }
      }
      if (!exists)
      {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Command.refreshHelpIDs: Adding "+prdLib+" to library list.");
        // We have to try to add it.
        String addlible = "ADDLIBLE LIB("+prdLib+")";  // not threadsafe
        CommandCall cc = new CommandCall(system_, addlible);
        added = cc.run();
      }
    }

    Vector keywords = handler.getKeywords();
    String helpID = handler.getHelpID();
    String[] ids = new String[keywords.size() + 3];
    ids[0] = helpID;
    ids[1] = helpID+"/ERROR/MESSAGES";
    ids[2] = helpID+"/COMMAND/EXAMPLES";
    for (int i=3; i<ids.length; ++i)
    {
      ids[i] = helpID+"/"+keywords.elementAt(i-3);
    }
//    PanelGroup pg = getHelpPanelGroup();
    String pgName = getXMLPanelGroup();
    PanelGroup pg = new PanelGroup(system_, pgName);
    helpIDs_ = pg.getHelpIdentifiers(ids);

    // Remove the product library from the library list if we added it.
    if (added)
    {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "CommandHelpRetriever: Removing "+prdLib+" from library list.");
      String rmvlible = "RMVLIBLE LIB("+xmlProductLibrary_+")";  // not threadsafe
      CommandCall cc = new CommandCall(system_, rmvlible);
      cc.run();
    }

    refreshedHelpIDs_ = true;
  }


  // Called on construct or after de-serialization
//  private void initializeTransient()
//  {
//    propertyChangeListeners_ = new PropertyChangeSupport(this);
//  }


  /**
   * Indicates whether the command is allowed to run in the specified environment.
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
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public boolean isAllowedToRun(int environment) throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

    switch (environment)
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
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
   * @see #isAllowedToRun
  **/
  public boolean isAllowedToRunBatch() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

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
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
   * @see #isAllowedToRun
  **/
  public boolean isAllowedToRunInteractive() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

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
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public boolean isEnabledForGUI() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

    return guiEnabled_;
  }


  /**
   * Indicates if this command applies to the specified mode of operating environment.
   * @param mode The operating mode to check. Possible values are:
   * <UL>
   * <LI>{@link #MODE_PRODUCTION MODE_PRODUCTION} - Production mode.
   * <LI>{@link #MODE_DEBUG MODE_DEBUG} - Debug mode.
   * <LI>{@link #MODE_SERVICE MODE_SERVICE} - Service mode.
   * <LI>{@link #MODE_ALL MODE_ALL} - All of the above modes.
   * </UL>
   * @return true if this command applies to the specified operating mode; false otherwise.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
  **/
  public boolean isOperatingMode(int mode) throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_)
      refreshCommandInfo();

    switch (mode)
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


  // Called when this object is de-serialized
  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException
  {
    in.defaultReadObject();
//    initializeTransient();
  }


  /**
   * Refreshes the information for this Command object.
   * This method is used to perform the call to the system
   * that retrieves the command information, XML, and help ID information for this CL command.
   * That information is cached internally until this method is called again. Note that
   * this method does not currently refresh the extended XML information provided by
   * the {@link #getXMLExtended getXMLExtended()} method.
   * <p>
   * The necessary information is implicitly refreshed by the various getter methods.
   * The system and path must be set before refresh() is called.
   * @throws AS400Exception If an error occurs.
   * @throws  AS400SecurityException  If a security or authority error occurs.
   * @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   * @throws  IOException  If an error occurs while communicating with the system.
   * @throws  InterruptedException  If this thread is interrupted.
   * @throws ObjectDoesNotExistException  If the object does not exist.
   * @throws SAXException  If SAX error occurs
   * @throws ParserConfigurationException  If there is an error in the parser configuration
   * @see #setPath
   * @see #setSystem
  **/
  public synchronized void refresh() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException, SAXException, ParserConfigurationException
  {
    refreshCommandInfo();
    refreshXML(true); // Refresh just the CMDD0100 format for now, since the 0200 format is new.
    refreshParsedXML();
    refreshHelpIDs();
  }


  // Worker method.
  private synchronized void refreshCommandInfo() throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Refreshing command information for "+path_+".");
    if (system_ == null) throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    if (path_ == null) throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_SET);

    // Call the QCDRCMDI API to get all of the information.
    int vrm = system_.getVRM();		// @A1A
    int numParms;
    if ((vrm >= 0x00060100) ||
        (vrm >= 0x00050400 && !detectedMissingPTF_)) {
      numParms = 6;	// @A1C - added support for proxy commands
    }
    else numParms = 5;

    ProgramParameter[] parms = new ProgramParameter[numParms];
    parms[0] = new ProgramParameter(335); // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(335)); // length of receiver variable
    parms[2] = new ProgramParameter(CharConverter.stringToByteArray(37, system_, "CMDI0100")); // format name
    byte[] cmdBytes = new byte[20];
    AS400Text text10 = new AS400Text(10, system_.getCcsid());
    QSYSObjectPathName p = new QSYSObjectPathName(path_);
    String command = p.getObjectName();
    String library = p.getLibraryName();
    text10.toBytes(command, cmdBytes, 0);
    text10.toBytes(library, cmdBytes, 10);
    parms[3] = new ProgramParameter(cmdBytes); // qualified command name
    parms[4] = errorCode_;

    // @A1A - Add support for proxy commands
    if (numParms > 5)		// @A1A
      parms[5] = new ProgramParameter(new byte[] { (byte) 0xF1 });	// @A1A Follow proxy chain, input CHAR(1)

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QCDRCMDI.PGM", parms);
    // Note: This is a threadsafe API.  However, on some code paths we may have called ADDLIBLE before getting here.  Therefore we must be careful to stay in the same thread in which ADDLIBLE executed.

    boolean succeeded = pc.run();
    if (!succeeded)
    {
      // If the exception is "MCH0802: Total parameters passed does not match number required" and we're running to V5R4, that means that the user hasn't applied PTF SI29629.  In that case, we will re-issue the program call, minus the new "follow proxy chain" parameter.
      AS400Message[] msgList = pc.getMessageList();
      if (numParms > 5 &&
          vrm < 0x00060100 && vrm >= 0x00050400 &&  // system VRM is V5R4
          msgList[msgList.length - 1].getID().equals("MCH0802"))  // wrong number of parms
      {
        if (Trace.traceOn_) Trace.log(Trace.WARNING, "PTF SI29629 is not installed: (MCH0802) " + msgList[msgList.length - 1].getText());
        detectedMissingPTF_ = true;  // remember that the PTF is missing
        ProgramParameter[] shorterParmList = new ProgramParameter[5];
        System.arraycopy(parms, 0, shorterParmList, 0, 5);
        try { pc.setParameterList(shorterParmList); } catch (PropertyVetoException e) {}
        succeeded = pc.run();
      }
      if (!succeeded) throw new AS400Exception(pc.getMessageList());
    }

    byte[] outputData = parms[0].getOutputData();
    CharConverter conv = new CharConverter(system_.getCcsid());

    String cppName = conv.byteArrayToString(outputData, 28, 10).trim();//@O2C offset is not correct before
    if (cppName.equals("*REXX"))
    {
      commandProcessingProgram_ = cppName;
    }
    else
    {
      String cppLib = conv.byteArrayToString(outputData, 38, 10).trim();//@O2C offset is not correct before
      commandProcessingProgram_ = QSYSObjectPathName.toPath(cppLib, cppName, "PGM");
    }

    String srcName = conv.byteArrayToString(outputData, 48, 10).trim();
    String srcLib = conv.byteArrayToString(outputData, 58, 10).trim();
    String srcMbr = conv.byteArrayToString(outputData, 68, 10).trim();
    try
    {
      sourceFile_ = QSYSObjectPathName.toPath(srcLib, srcName, srcMbr, "MBR");
    }
    catch(ExtendedIllegalArgumentException eiae)
    {
      if (Trace.traceOn_) Trace.log(Trace.WARNING, "Unable to process source file: '"+srcName+","+srcLib+","+srcMbr+"'.", eiae);
      sourceFile_ = null;
    }

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

    allowLimitedUser_ = outputData[123] == (byte)0xF1; // '1' for *YES, '0' for *NO

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

    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Successfully refreshed command information for "+path_+".");
  }


  // Worker method.
  private synchronized void refreshXML(boolean f) throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException
  {
    String format = f ? "CMDD0100" : "CMDD0200";
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Refreshing XML (format "+format+") information for "+path_+".");
    if (system_ == null) throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    if (path_ == null) throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_SET);

    ProgramParameter[] parms = new ProgramParameter[6];
    byte[] cmdBytes = new byte[20];
    AS400Text text10 = new AS400Text(10, system_.getCcsid());
    QSYSObjectPathName p = new QSYSObjectPathName(path_);
    String command = p.getObjectName().trim().toUpperCase();
    String library = p.getLibraryName().trim().toUpperCase();
    text10.toBytes(command, cmdBytes, 0);
    text10.toBytes(library, cmdBytes, 10);

    // Make 2 program calls. The first call tells us how much
    // data is coming back. The second call actually retrieves the data.
    // Since a large amount of data can be returned, and the size is so
    // variable depending on the command, we do not send up an initial
    // guess size on the first attempt, just enough to get the real amount back.
    parms[0] = new ProgramParameter(cmdBytes); // qualified command name
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(8)); // destination, AKA length of receiver variable
    parms[2] = new ProgramParameter(CharConverter.stringToByteArray(37, system_, "DEST0100")); // destination format name
    parms[3] = new ProgramParameter(8); // receiver variable
    parms[4] = new ProgramParameter(CharConverter.stringToByteArray(37, system_, format)); // receiver format name
    parms[5] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QCDRCMDD.PGM", parms); // this is a threadsafe API
    // Note: This is a threadsafe API.  However, on some code paths we may have called ADDLIBLE before getting here.  Therefore we must be careful to stay in the same thread in which ADDLIBLE executed.

    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }

    byte[] outputData = parms[3].getOutputData();
    int bytesReturned = BinaryConverter.byteArrayToInt(outputData, 0);
    int bytesAvailable = BinaryConverter.byteArrayToInt(outputData, 4);

    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Refreshing XML /QSYS.LIB/QCDRCMDD.PGM returned bytesAvailable="+bytesAvailable);

    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(bytesAvailable+8));
    try
    {
      parms[3].setOutputDataLength(bytesAvailable+8);
    }
    catch (java.beans.PropertyVetoException pve)
    {
    }

    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }

    outputData = parms[3].getOutputData();
    bytesReturned = BinaryConverter.byteArrayToInt(outputData, 0);
    //bytesAvailable = BinaryConverter.byteArrayToInt(outputData, 4);
    ConvTable conv1208 = ConvTable.getTable(1208, null); // CCSID 1208 is UTF-8
    if (f)
    {
      xml_ = conv1208.byteArrayToString(outputData, 8, bytesReturned, 0);
      refreshedXML_ = true;
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Refreshing XML /QSYS.LIB/QCDRCMDD.PGM returned data="+xml_);

    }
    else
    {
      xml2_ = conv1208.byteArrayToString(outputData, 8, bytesReturned, 0);
      refreshedXML2_ = true;
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Refreshing XML /QSYS.LIB/QCDRCMDD.PGM returned data="+xml2_);
    }
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Successfully refreshed XML (format "+format+") information for "+path_+".");
  }


  /**
   *  Removes the PropertyChangeListener.  If the PropertyChangeListener is not on the list, nothing is done.
   *
   *  @param  listener  The PropertyChangeListener.
   **/
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
    if (propertyChangeListeners_ != null) propertyChangeListeners_.removePropertyChangeListener(listener);
  }


  /**
   * Sets the path name of the command.
   * @param path The command path, e.g. "/QSYS.LIB/CRTUSRPRF.CMD".
   * @see #getPath
  **/
  public void setPath(String path)
  {
    if (path == null) throw new NullPointerException("path");

    QSYSObjectPathName.validatePath(path, "CMD");

    synchronized(this)
    {
      String old = path_;
      path_ = path;

      // In case you want to switch commands on-the-fly.
      refreshed_ = false;
      refreshedXML_ = false;
      refreshedXML2_ = false;
      refreshedHelpIDs_ = false;
      refreshedHelpText_ = false;
      refreshedParsedXML_ = false;
      loadedDescription_ = false;

      if (propertyChangeListeners_ != null) propertyChangeListeners_.firePropertyChange("path", old, path);
    }
  }

  /**
   *  Sets the system from which to retrieve the command list.
   *
   *  @param  system  The system from which to retrieve the commands.
   * @see #getSystem
   **/
  public void setSystem(AS400 system)
  {
    if (system == null) throw new NullPointerException("system");

    synchronized(this)
    {
      AS400 old = system_;
      system_ = system;

      refreshed_ = false;
      refreshedXML_ = false;
      refreshedXML2_ = false;
      refreshedHelpIDs_ = false;
      refreshedHelpText_ = false;
      refreshedParsedXML_ = false;
      loadedDescription_ = false;

      if (propertyChangeListeners_ != null) propertyChangeListeners_.firePropertyChange("system", old, system);
    }
  }


  /**
   * Returns a String representation of this Command.
   * @return The string, which includes the fully integrated file system
   * path name of this command.
  **/
  public String toString()
  {
    return super.toString()+"["+path_+"]";
  }
}
