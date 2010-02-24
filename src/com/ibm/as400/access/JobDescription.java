///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JobDescription.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2005-2010 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 *
 * Represents an IBM i job description.
 * <P>
 * Note that calling any of the attribute getters for the first time will
 * result in an implicit call to {@link #refresh()}.
 * If any exception is thrown by an implicit call to refresh(),
 * it will be logged to {@link com.ibm.as400.access.Trace#ERROR
 * Trace.ERROR} and rethrown as a <tt>java.lang.RuntimeException</tt>.
 * However, should an exception occur during an
 * <em>explicit</em> call to refresh(), the exception will be thrown as-is to the caller.
 * <P>
 * Implementation note:
 * This class internally calls the Retrieve Job Description (QWDRJOBD) API.
 *
 * <p><em>This class was inspired by a prototype submitted by Kendall Coolidge.</em>
 *
 **/

public class JobDescription implements Serializable
{
  static final long serialVersionUID = 4L;

  private static final int VRM520 = AS400.generateVRM(5, 2, 0);
  private static final int VRM530 = AS400.generateVRM(5, 3, 0);
  private static final int VRM540 = AS400.generateVRM(5, 4, 0);
  private int vrm_;  // system version

  private AS400 system_;
  private String name_;
  private String library_;

  private transient boolean loaded_;

  private static final ProgramParameter errorCode_ = new ProgramParameter(new byte[4]);

  private String jobDescriptionName_;
  private String jobDescriptionLibraryName_;
  private String userName_;
  private String[] initialLibraryList_;
  private String jobDate_;
  private String jobSwitches_;
  private String jobQueueName_;
  private String jobQueueLibraryName_;
  private String jobQueuePriority_;
  private String holdOnJobQueue_;
  private String outputQueueName_;
  private String outputQueueLibraryName_;
  private String outputQueuePriority_;
  private String printerDeviceName_;
  private String printText_;
  private int syntaxCheckSeverity_;
  private int endSeverity_;
  private int messageLoggingSeverity_;
  private String messageLoggingLevel_;
  private String messageLoggingText_;
  private String loggingOfCLPrograms_;
  private String inquiryMessageReply_;
  private String deviceRecoveryAction_;
  private String timeSliceEndPool_;
  private String accountingCode_;
  private String routingData_;
  private String textDescription_;
  private int jobMessageQueueMaximumSize_;
  private String jobMessageQueueFullAction_;
  private String CYMDJobDate_;
  private String allowMultipleThreads_;
  private String spooledFileAction_; // added in V5R2
  private String[] iaspNames_;       // added in V5R2
  private String ddmConversation_;   // added in V5R3
  private String jobLogOutput_;      // added in V5R4


  /**
   * Constructs a JobDescription.
   * @param system The system where the job description resides.
   * @param name The name of the job description to retrieve.
   * @param library The library containing the job description.
   **/
  public JobDescription(AS400 system, String library, String name)
  {
    if (system == null)  throw new NullPointerException("system");
    if (library == null) throw new NullPointerException("library");
    if (name == null)    throw new NullPointerException("name");

    system_ = system;
    name_ = name;
    library_ = library;
  }

  /**
   * Constructs a JobDescription.
   * @param system The system where the job description resides.
   * @param path The fully qualified IFS path to the job description.
   **/
  public JobDescription(AS400 system, QSYSObjectPathName path)
  {
    if (system == null)  throw new NullPointerException("system");
    if (path == null)    throw new NullPointerException("path");

    system_ = system;
    name_ = path.getObjectName();
    library_ = path.getLibraryName();
  }

  /**
   * Refreshes the values for all attributes of the job description.
   *
   * @throws AS400Exception If the system returns an error message.
   * @throws AS400SecurityException If a security or authority error occurs.
   * @throws ErrorCompletingRequestException If an error occurs before the request is completed.
   * @throws InterruptedException If this thread is interrupted.
   * @throws ObjectDoesNotExistException If the object does not exist on the system.
   * @throws IOException If an error occurs while communicating with the system.
   **/
  public void refresh()
    throws
    AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, ObjectDoesNotExistException, IOException
  {

    if (system_ == null)
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    if (name_ == null)
      throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    if (library_ == null)
      throw new ExtendedIllegalStateException("library", ExtendedIllegalStateException.PROPERTY_NOT_SET);

    final int ccsid = system_.getCcsid();
    ConvTable conv = ConvTable.getTable(ccsid, null);
    AS400Text text20 = new AS400Text(20, ccsid);

    // concat jobdname + library for program parameter[3]
    StringBuffer qualifiedJobDName = new StringBuffer(20);
    qualifiedJobDName.append(name_);
    for (int i = 0; i < (10 - name_.length()); i++) {
      qualifiedJobDName.append(" ");
    }
    qualifiedJobDName.append(library_);

    ProgramParameter[] parms = new ProgramParameter[5];
    int len = 2048;
    // receiver variable
    parms[0] = new ProgramParameter(len);
    // length of receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(len));
    // format name
    parms[2] = new ProgramParameter(conv.stringToByteArray("JOBD0100"));
    // job description name
    parms[3] = new ProgramParameter(text20.toBytes(qualifiedJobDName.toString().trim().toUpperCase()));

    parms[4] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QWDRJOBD.PGM", parms);

    if (!pc.run()) {
      throw new AS400Exception(pc.getMessageList());
    }

    byte[] data = parms[0].getOutputData();

    int bytesReturned = BinaryConverter.byteArrayToInt(data, 0);
    int bytesAvailable = BinaryConverter.byteArrayToInt(data, 4);
    if (bytesReturned < bytesAvailable)
    {
      if (Trace.traceOn_)
      {
        Trace.log(
                  Trace.DIAGNOSTIC,
                  "JobDescription: Not enough bytes, trying again. Bytes returned = "
                  + bytesReturned
                  + "; bytes available = "
                  + bytesAvailable);
      }
      len = bytesAvailable;
      try
      {
        parms[0].setOutputDataLength(len);
        parms[1].setInputData(BinaryConverter.intToByteArray(len));
      } catch (java.beans.PropertyVetoException pve) {} // this will never happen
      if (!pc.run()) {
        throw new AS400Exception(pc.getMessageList());
      }
      data = parms[0].getOutputData();
    }

    jobDescriptionName_ = conv.byteArrayToString(data, 8, 10).trim();
    jobDescriptionLibraryName_ = conv.byteArrayToString(data, 18, 10).trim();
    userName_ = conv.byteArrayToString(data, 28, 10).trim();
    jobDate_ = conv.byteArrayToString(data, 38, 8).trim();
    jobSwitches_ = conv.byteArrayToString(data, 46, 8).trim();
    jobQueueName_ = conv.byteArrayToString(data, 54, 10).trim();
    jobQueueLibraryName_ = conv.byteArrayToString(data, 64, 10).trim();
    jobQueuePriority_ = conv.byteArrayToString(data, 74, 2).trim();
    holdOnJobQueue_ = conv.byteArrayToString(data, 76, 10).trim();
    outputQueueName_ = conv.byteArrayToString(data, 86, 10).trim();
    outputQueueLibraryName_ = conv.byteArrayToString(data, 96, 10).trim();
    outputQueuePriority_ = conv.byteArrayToString(data, 106, 2).trim();
    printerDeviceName_ = conv.byteArrayToString(data, 108, 10).trim();
    printText_ = conv.byteArrayToString(data, 118, 30).trim();
    syntaxCheckSeverity_ = BinaryConverter.byteArrayToInt(data, 148);
    endSeverity_ = BinaryConverter.byteArrayToInt(data, 152);
    messageLoggingSeverity_ = BinaryConverter.byteArrayToInt(data, 156);
    messageLoggingLevel_ = conv.byteArrayToString(data, 160, 1).trim();
    messageLoggingText_ = conv.byteArrayToString(data, 161, 10).trim();
    loggingOfCLPrograms_ = conv.byteArrayToString(data, 171, 10).trim();
    inquiryMessageReply_ = conv.byteArrayToString(data, 181, 10).trim();
    deviceRecoveryAction_ = conv.byteArrayToString(data, 191, 13).trim();
    timeSliceEndPool_ = conv.byteArrayToString(data, 204, 10).trim();
    accountingCode_ = conv.byteArrayToString(data, 214, 15).trim();
    routingData_ = conv.byteArrayToString(data, 229, 80).trim();
    textDescription_ = conv.byteArrayToString(data, 309, 50).trim();
    jobMessageQueueMaximumSize_ = BinaryConverter.byteArrayToInt(data, 376);
    jobMessageQueueFullAction_ = conv.byteArrayToString(data, 380, 10).trim();
    CYMDJobDate_ = conv.byteArrayToString(data, 390, 10).trim();
    allowMultipleThreads_ = conv.byteArrayToString(data, 400, 10).trim();

    int initialLibraryListCount = BinaryConverter.byteArrayToInt(data, 364);
    initialLibraryList_ = new String[initialLibraryListCount];
    int initialLibraryListOffset = BinaryConverter.byteArrayToInt(data, 360);
    for (int i = 0; i < initialLibraryListCount; ++i)
    {
      initialLibraryList_[i] = conv.byteArrayToString(data, initialLibraryListOffset + (i * 11), 10).trim();
    }

    if (vrm_ == 0)  vrm_ = system_.getVRM();
    if (vrm_ >= VRM520)
    {
      spooledFileAction_ = conv.byteArrayToString(data, 410, 10).trim();

      int iaspOffset = BinaryConverter.byteArrayToInt(data, 420);
      int iaspCount = BinaryConverter.byteArrayToInt(data, 424);
      int iaspLength = BinaryConverter.byteArrayToInt(data, 428);
      iaspNames_ = new String[iaspCount];
      for (int i = 0; i < iaspCount; ++i)
      {
        int offset = iaspOffset + (i * iaspLength);
        iaspNames_[i] = conv.byteArrayToString(data, offset, 10).trim();
      }
    }

    if (vrm_ >= VRM530)
    {
      ddmConversation_ = conv.byteArrayToString(data, 432, 10).trim();
    }

    if (vrm_ >= VRM540)
    {
      jobLogOutput_ = conv.byteArrayToString(data, 442, 10).trim();
    }

    loaded_ = true;
  }

  /**
   Restores the state of this object from an object input stream.
   @param ois The stream of state information.
   @throws IOException
   @throws ClassNotFoundException
   **/
  private void readObject(java.io.ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    // Restore the non-static and non-transient fields.
    ois.defaultReadObject();

    // Initialize the transient fields.
    loaded_ = false;
  }

  /**
   * Helper method.  Calls refresh and rethrows only RuntimeException's
   * so that all of the getters can call it, without having long 'throws' lists.
   **/
  private void loadInformation() throws RuntimeException
  {
    try {
      refresh();
    }
    catch (RuntimeException e) { throw e; }
    catch (Exception e) {
      Trace.log(Trace.ERROR, "Exception rethrown by loadInformation():", e);
      throw new IllegalStateException(e.getMessage());
    }
  }

  /**
   * Returns the accounting code associated with this job description.
   * <p>An identifier assigned to jobs that use this job description.
   * This code is used to collect system resource use information.
   * If the special value *USRPRF is specified, the accounting code
   * used for jobs using this job description is obtained from the job's user profile.
   * @return The accounting code.
   **/
  public String getAccountingCode()
  {
    if (!loaded_)  loadInformation();
    return accountingCode_;
  }

  /**
   * Returns whether or not the job is allowed to run with
   * multiple user threads.
   * <p>This attribute does not prevent the operating system from creating
   * system threads in the job. This attribute is not allowed to be changed once a job starts.
   * This attribute applies to autostart jobs, prestart jobs, batch jobs
   * submitted from job schedule entries, and jobs started by using the
   * Submit Job (SBMJOB) and Batch Job (BCHJOB) commands. This attribute is
   * ignored when starting all other types of jobs.
   * This attribute should be set to <tt>true</tt> only in job descriptions
   * that are used exclusively with functions that create multiple user threads.
   * @return Whether or not the job is allowed to run with
   * multiple user threads.  Possible values are:
   * <ul>
   * <li>true - Multiple threads allowed.
   * <li>false - Multiple user threads not allowed.
   * </ul>
   **/
  public boolean isAllowMultipleThreads()
  {
    if (!loaded_)  loadInformation();
    return (allowMultipleThreads_.equals("*YES") ? true : false);
  }

  /**
   * Returns the job date in CYMD format.
   * <p>The date that will be assigned to jobs using this job description
   * when they are started. The possible values are:
   * <ul>
   * <li>"*SYSVAL" - The value in the QDATE system value is used at the time the job is started.</li>
   * <li><i>job-date</i> - The date to be used at the time the job is started. The format of
   *  the field returned in CYYMMDD where C is the century, YY is the year, MM is the month,
   *  and DD is the day. A 0 for the century indicates years 19xx and a 1 indicates
   *  years 20xx. The field is padded on the right with blanks.
   * </ul>
   * @return The job date in CYYMMDD format.
   **/
  public String getCYMDJobDate()
  {
    if (!loaded_)  loadInformation();
    return CYMDJobDate_;
  }

  /**
   * Returns the action to take when an I/O error occurs for the
   * interactive job's requesting program device.
   * The possible values are:
   * <ul>
   * <li> "*SYSVAL" -	The value in the system value QDEVRCYACN at the time the job is started is used as the device recovery action for this job description.
   * <li> "*DSCMSG" -	Disconnects the job when an I/O error occurs. When the job reconnects, the system sends a message to the application program, indicating the job has reconnected and that the workstation device has recovered.
   * <li> "*DSCENDRQS" - Disconnects the job when an I/O error occurs. When the job reconnects, the system sends the End Request (ENDRQS) command to return control to the previous request level.
   * <li> "*ENDJOB" -	Ends the job when an I/O error occurs. A message is sent to the job's log and to the history log (QHST). This message indicates that the job ended because of a device error.
   * <li> "*ENDJOBNOLIST" - Ends the job when an I/O error occurs. There is no job log produced for the job. The system sends a message to the history log (QHST). This message indicates that the job ended because of a device error.
   * </ul>
   * @return The device recovery action.
   **/
  public String getDeviceRecoveryAction()
  {
    if (!loaded_)  loadInformation();
    return deviceRecoveryAction_;
  }

  /**
   * Returns whether Distributed Data Management conversations are kept or dropped when they are not being used.
   * The possible values are:
   * <ul>
   * <li>*KEEP - The system keeps DDM conversation connections active when there are no users.
   * <li>*DROP - The system ends a DDM-allocated conversation when there are no users.
   * <li><tt>null</tt> if system is pre-V5R3.
   * </ul>
   * @return Whether DDM conversations are kept or dropped.
   **/
  public String getDDMConversation()
  {
    if (!loaded_)  loadInformation();
    return ddmConversation_;
  }

  /**
   * Returns the message severity level of escape messages that can cause a batch job to end.
   * The possible values are from 0 through 99.
   * @return The message severity level of escape messages that can cause a batch job to end.
   **/
  public int getEndSeverity()
  {
    if (!loaded_)  loadInformation();
    return endSeverity_;
  }

  /**
   * Returns whether jobs using this job description are put on the job queue in the
   * hold condition.
   * @return Whether jobs using this job description are put on the job queue in the hold condition.
   **/
  public boolean isHoldOnJobQueue()
  {
    if (!loaded_)  loadInformation();
    return (holdOnJobQueue_.equals("*YES") ? true : false);
  }

  /**
   * Returns the initial library list that is used for jobs that use this job
   * description. Only the libraries in the user portion of the library list are
   * included.
   * @return The initial library list as an array of type String
   **/
  public String[] getInitialLibraryList()
  {
    if (!loaded_)  loadInformation();
    return initialLibraryList_;
  }

  /**
   * Indicates how inquiry messages are answered for jobs that use this job description.
   * The possible values are:
   * <ul>
   * <li> "*RQD" - The job requires an answer for any inquiry messages that occur
   * while the job is running.
   * <li> "*DFT" - The system uses the default message reply to answer any inquiry
   * messages issued while the job is running. The default reply is either defined in
   * the message description or is the default system reply.
   * <li> "*SYSRPYL" - The system reply list is checked to see if there is an entry
   * for an inquiry message issued while the job is running. If a match occurs, the
   * system uses the reply value for that entry. If no entry exists for that message,
   * the system uses an inquiry message.
   * </ul>
   * @return The inquiry message reply behavior.
   **/
  public String getInquiryMessageReply()
  {
    if (!loaded_)  loadInformation();
    return inquiryMessageReply_;
  }

  /**
   * Returns the list of initial ASP groups for jobs that use this job description. This does not include the system ASP or basic user ASPs.
   * <tt>null</tt> if system is pre-V5R2.
   * @return The list of initial ASP groups.  <tt>null</tt> if system is pre-V5R2.
   **/
  public String[] getInitialASPGroupNames()
  {
    return iaspNames_;
  }

  /**
   * Returns the date that will be assigned to jobs using this job description
   * when they are started. The possible values are:
   * <ul>
   * <li> "*SYSVAL" -	The value in the QDATE system value is used at the time the job is started.
   * <li><i>job-date</i> - The date to be used at the time the job is started. This date is in the format specified for the DATFMT job attribute.
   * </ul>
   *
   * @return The date that will be assigned to jobs using this job description.
   **/
  public String getJobDateString()
  {
    if (!loaded_)  loadInformation();
    return jobDate_;
  }

  /**
   * Returns name of the library in which the job description resides.
   * @return The name of the library in which the job description resides.
   **/
  public String getLibraryName()
  {
    if (!loaded_)  loadInformation();
    return jobDescriptionLibraryName_;
  }

  /**
   * Returns name of the job description about which information is being returned.
   * @return The name of the job description about which information is being returned.
   **/
  public String getName()
  {
    if (!loaded_)  loadInformation();
    return jobDescriptionName_;
  }

  /**
   * Indicates how the job log will be produced when the job completes.
   * The possible values are:
   * <ul>
   * <li>*SYSVAL - The value is specified by the QLOGOUTPUT system value.
   * <li>*JOBLOGSVR - The job log will be produced by a job log server. For more information about job log servers, refer to the Start Job Log Server (STRLOGSVR) command.
   * <li>*JOBEND - The job log will be produced by the job itself. If the job cannot produce its own job log, the job log will be produced by a job log server. For example, a job does not produce its own job log when the system is processing a Power Down System (PWRDWNSYS) command. 
   * <li>*PND - The job log will not be produced. The job log remains pending until removed.
   * <li><tt>null</tt> if system is pre-V5R4.
   * </ul>
   * @return The job log output behavior.
   **/
  public String getJobLogOutput()
  {
    if (!loaded_)  loadInformation();
    return jobLogOutput_;
  }

  /**
   * Returns the action to be taken when the job message queue becomes full.
   * <br>The possible values are:
   * <ul>
   * <li> "*SYSVAL" - The value is specified by the system value QJOBMSGQFL.
   * <li> "*NOWRAP" -	When the message queue becomes full, do not wrap. This action will cause the job to end.
   * <li> "*WRAP" - When the message queue becomes full, wrap to the beginning and start filling again.
   * <li> "*PRTWRAP" - When the message queue becomes full, wrap the job queue and print the messages that are being overlaid.
   * </ul>
   * @return The action to be taken when the job message queue becomes full.
   **/
  public String getJobMessageQueueFullAction()
  {
    if (!loaded_)  loadInformation();
    return jobMessageQueueFullAction_;
  }

  /**
   * Returns the maximum size (in megabytes) of the job message queue.
   * <br>The possible values are:
   * <ul>
   * <li> <i>0</i> - The maximum size set by system value QJOBMSGMX at the time the job is started.
   * <li> <i>2-64</i> - The maximum size of the job message queue in megabytes.
   * </ul>
   * @return The maximum size (in megabytes) of the job message queue.
   **/
  public int getJobMessageQueueMaximumSize()
  {
    if (!loaded_)  loadInformation();
    return jobMessageQueueMaximumSize_;
  }

  /**
   * Returns the library of the job queue into which batch jobs using this job description are placed.
   * @return The library of the job queue into which batch jobs using this job description are placed.
   **/
  public String getJobQueueLibraryName()
  {
    if (!loaded_)  loadInformation();
    return jobQueueLibraryName_;
  }

  /**
   * Returns the name of the job queue into which batch jobs using this job description are placed.
   * @return The name of the job queue into which batch jobs using this job description are placed.
   **/
  public String getJobQueueName()
  {
    if (!loaded_)  loadInformation();
    return jobQueueName_;
  }

  /**
   * Returns the scheduling priority of each job that uses this job description.
   * <br>The highest priority is 1 and the lowest priority is 9.
   * @return The scheduling priority of each job that uses this job description.
   **/
  public int getJobQueuePriority()
  {
    if (!loaded_)  loadInformation();
    try { return Integer.parseInt(jobQueuePriority_); }
    catch (NumberFormatException e) {  // this should never happen, but if it does...
      Trace.log(Trace.ERROR, "Exception swallowed by getJobQueuePriority():", e);
      return 9;
    }
  }

  /**
   * Returns the initial settings for a group of eight job switches used by jobs that
   * use this job description.  These switches can be set or tested in a program and
   * used to control a program's flow.
   * <br> For each bit position, the possible values are '0' (off) and '1' (on).
   * @return A bit-map containing the initial settings for the eight job switches.
   **/
  public byte getJobSwitches()
  {
    if (!loaded_)  loadInformation();
    // Note: Byte.parseByte(string,2) interprets the byte as a signed value, therefore it won't accept a '1' at bit position 0 (the "sign bit").
    byte bitmap = (byte)0;
    for (int i=0; i<8; i++) {
      if (jobSwitches_.charAt(i) == '1') bitmap = (byte)(bitmap | (0x01 << 7-i));
    }
    return bitmap;
  }

  /**
   * Returns whether or not commands are logged for CL programs that are run.
   * @return Whether or not commands are logged for CL programs that are run.
   **/
  public boolean isLoggingOfCLPrograms()
  {
    if (!loaded_)  loadInformation();
    return (loggingOfCLPrograms_.equals("*YES") ? true : false);
  }

  /**
   * Returns the type of information logged.
   * Possible types are:
   * <ul>
   * <li> 0 - No messages are logged.
   * <li> 1 - All messages sent to the job's external message queue with a
   * severity greater than or equal to the message logging severity are logged.
   * This includes the indication of job start, job end and job completion status.
   * <li> 2 - The following information is logged:
   * 	<ul>
   * 	 <li> Level 1 information.
   *   <li> Request messages that result in a high-level message with a severity code
   *			greater than or equal to the logging severity cause the request message
   *			and all associated messages to be logged.
   *  </ul>
   * <li> 3 - The following information is logged:
   *  <ul>
   * 		<li> Level 1 and 2 information.
   * 		<li> All request messages.
   * 		<li> Commands run by a CL program are logged if it is allowed by the logging of CL programs job attribute and the log attribute of the CL program.
   * 	</ul>
   * <li> 4 - The following information is logged:
   * 	<ul>
   * 		<li> All request messages and all messages with a severity greater than or equal to the message logging severity, including trace messages.
   * 		<li> Commands run by a CL program are logged if it is allowed by the logging of CL programs job attribute and the log attribute of the CL program.
   * 	</ul>
   * </ul>
   * @return The type of information logged.
   **/
  public int getMessageLoggingLevel()
  {
    if (!loaded_)  loadInformation();
    try { return Integer.parseInt(messageLoggingLevel_); }
    catch (NumberFormatException e) {  // this should never happen, but if it does...
      Trace.log(Trace.ERROR, "Exception swallowed by getMessageLoggingLevel():", e);
      return 0;
    }
  }

  /**
   * Returns the severity level that is used in conjunction with the logging level to determine which error messages are logged in the job log.
   * The possible values are from 0 through 99.
   * @return The message logging severity level.
   **/
  public int getMessageLoggingSeverity()
  {
    if (!loaded_)  loadInformation();
    return messageLoggingSeverity_;
  }

  /**
   * Returns the level of message text that is written in the job log when a message
   * is logged according to the logging level and logging severity.
   * The possible values are:
   * <ul>
   * <li> "*MSG" - Only the message text is written to the job log.
   * <li> "*SECLVL" - Both the message text and the message help (cause and recovery) of the error message are written to the job log.
   * <li> "*NOLIST" -	If the job ends normally, no job log is produced. If the job ends abnormally (if the job end code is 20 or higher), a job log is produced. The messages that appear in the job log contain both the message text and the message help.
   * </ul>
   * @return The level of message text that is written in the job log.
   **/
  public String getMessageLoggingText()
  {
    if (!loaded_)  loadInformation();
    return messageLoggingText_;
  }

  /**
   * Returns the name of the library in which the output queue resides.
   * @return The name of the library in which the output queue resides.
   **/
  public String getOutputQueueLibraryName()
  {
    if (!loaded_)  loadInformation();
    return outputQueueLibraryName_;
  }

  /**
   * Returns the name of the default output queue that is used for spooled
   * output produced by jobs that use this job description.
   * Possible values are:
   * <ul>
   * <li> "*USRPRF" - The output queue name for jobs using this job description is obtained from the user profile of the job at the time the job is started.
   * <li> "*DEV" - The output queue with the same name as the printer device for this job description is used.
   * <li> "*WRKSTN" - The output queue name is obtained from the device description from which this job is started.
   * <li> <i>output-queue-name</i> 	The name of the output queue for this job description.
   * </ul>
   * @return The name of the default output queue.
   **/
  public String getOutputQueueName()
  {
    if (!loaded_)  loadInformation();
    return outputQueueName_;
  }

  /**
   * Returns the output priority for spooled files that are produced by jobs using this
   * job description.
   * The highest priority is 1, and the lowest priority is 9.
   * @return The output priority.
   **/
  public int getOutputQueuePriority()
  {
    if (!loaded_)  loadInformation();
    try { return Integer.parseInt(outputQueuePriority_); }
    catch (NumberFormatException e) {  // this should never happen, but if it does...
      Trace.log(Trace.ERROR, "Exception swallowed by getOutputQueuePriority():", e);
      return 9;
    }
  }

  /**
   * Returns the name of the printer device or the source for the name of the printer
   * device that is used for all spooled files created by jobs that use this job description.
   * Possible values are:
   * <ul>
   * <li> "*USRPRF" - The printer device name is obtained from the user profile of the job at the time the job is started.
   * <li> "*SYSVAL" - The value in the system value QPRTDEV at the time the job is started is used as the printer device name.
   * <li> "*WRKSTN" - The printer device name is obtained from the work station where the job was started.
   * <li> <i>printer-device-name</i> - The name of the printer device that is used with this job description.
   * </ul>
   * @return The name of the printer device.
   **/
  public String getPrinterDeviceName()
  {
    if (!loaded_)  loadInformation();
    return printerDeviceName_;
  }

  /**
   * Returns the line of text (if any) that is printed at the bottom of each page of
   * printed output for jobs using this job description.
   * @return The print text.
   **/
  public String getPrintText()
  {
    if (!loaded_)  loadInformation();
    return printText_;
  }

  /**
   * Returns the routing data that is used with this job description to start jobs.
   * The possible values are:
   * <ul>
   * <li> "QCMDI" - The default routing data QCMDI is used by the IBM-supplied interactive subsystem to route the job to the IBM-supplied control language processor QCMD in the QSYS library.
   * <li> "*RQSDTA" - Up to the first 80 characters of the request data specified in the request data field are used as the routing data for the job.
   * <li> <i>routing-data</i> - The routing data to use for jobs that use this job description.
   * </ul>
   * @return The routing data.
   **/
  public String getRoutingData()
  {
    if (!loaded_)  loadInformation();
    return routingData_;
  }

  /**
   * Returns the value that specifies whether spooled files can be accessed through
   * job interfaces once a job has completed its normal activity.
   * <tt>null</tt> if system is pre-V5R2.
   * Possible values are:
   * <ul>
   * <li> "*KEEP" - Spooled files are kept with the job when the job completes its activity.
   * <li> "*DETACH" -	Spooled files are detached from the job when the job completes its activity.
   * <li> "*SYSVAL" - The jobs using this job description will take the spooled file action specified by the QSPLFACN system value.
   * </ul>
   * @return Spooled file action.  <tt>null</tt> if system is pre-V5R2.
   **/
  public String getSpooledFileAction()
  {
    if (!loaded_)  loadInformation();
    return spooledFileAction_;
  }

  /**
   * Returns whether requests placed on the job's message queue are checked for syntax
   * as CL commands, and the message severity that causes a syntax error to end
   * processing of a job.
   * The possible values are:
   * <ul>
   * <li> <i>-1</i> - The request data is not checked for syntax as CL commands.
   * <li> <i>0-99</i> - Specifies the lowest message severity that causes a running job to end. The request data is checked for syntax as CL commands, and, if a syntax error occurs that is greater than or equal to the error message severity specified here, the running of the job that contains the erroneous command is suppressed.
   * </ul>
   * @return The syntax check severity.
   **/
  public int getSyntaxCheckSeverity()
  {
    if (!loaded_)  loadInformation();
    return syntaxCheckSeverity_;
  }

  /**
   * Returns the system where the job description is located.
   * @return The system where the job description resides.
   **/
  public AS400 getSystem()
  {
    return system_;
  }

  /**
   * Returns the user text, if any, used to briefly describe the job description.
   * @return The text description.  "" if no description.
   **/
  public String getTextDescription()
  {
    if (!loaded_)  loadInformation();
    return textDescription_;
  }

  /**
   * Returns whether interactive jobs using this job description should be moved to
   * another main storage pool when they reach time-slice end.
   * The possible values are:
   * <ul>
   * <li> "*SYSVAL" - The system value is used.
   * <li> "*NONE" - The job is not moved when it reaches time-slice end.
   * <li> "*BASE" - The job is moved to the base pool when it reaches time-slice end.
   * </ul>
   * @return Whether interactive jobs using this job description should be moved to another main storage pool when they reach time-slice end.
   **/
  public String getTimeSliceEndPool()
  {
    if (!loaded_)  loadInformation();
    return timeSliceEndPool_;
  }

  /**
   * Returns the name of the user profile associated with this job description.
   * @return The name of the user profile associated with this job description.
   **/
  public String getUserName()
  {
    if (!loaded_)  loadInformation();
    return userName_;
  }

  /**
   * Sets the library where the job description is located.  Cannot be changed if the object
   * has established a connection to the system
   * @param library The name of the library
   **/
  public void setLibraryName(String library)
  {
    if (library == null)  throw new NullPointerException("library");
    if (loaded_)
      throw new ExtendedIllegalStateException("library", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    library_ = library;
  }

  /**
   * Sets the job description name.  Cannot be changed after the object has established
   * a connection to the system.
   * @param name The job description name
   **/
  public void setName(String name)
  {
    if (name == null)  throw new NullPointerException("name");
    if (loaded_)
      throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    name_ = name;
  }

  /**
   * Sets the system.  Cannot be changed after the object
   * has established a connection to the system.
   *
   * @param system The system where the job description resides.
   **/
  public void setSystem(AS400 system)
  {
    if (system == null)  throw new NullPointerException("system");
    if (loaded_)
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    system_ = system;
    vrm_ = 0;
  }

}
