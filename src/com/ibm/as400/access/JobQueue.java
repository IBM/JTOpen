///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JobQueue.java
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

/**
 * Represents an IBM i job queue.
 * Note that calling any of the attribute getters for the first time will
 * result in an implicit call to {@link #refresh()}.
 * If any exception is thrown by an implicit call to refresh(),
 * it will be logged to {@link com.ibm.as400.access.Trace#ERROR
 * Trace.ERROR} and rethrown as a <tt>java.lang.RuntimeException</tt>.
 * However, should an exception occur during an
 * <em>explicit</em> call to refresh(), the exception will be thrown as-is to the caller.
 * Implementation note:
 * This class internally calls the Retrieve Job Queue(QSPRJOBQ) API.
 * @author zhangze
 */
public class JobQueue implements Serializable {
	static final long serialVersionUID = 4L;
	
	private AS400 system_;
    private String name_;
	private String library_;
	
	private transient boolean loaded_;
	private static final ProgramParameter errorCode_ = new ProgramParameter(new byte[4]);
	
	private String jobQueueName_;
	private String jobQueueLibrary_;
	private int numberOfJobs_;
	private String jobQueueStatus_;
	private String subsystemName_;
	private String subsystemLibrary_;
	private String textDescription_;
	private int sequenceNumber_;
	private int maxActive_;
	private int currentActive_;
	private String authorityCheck_;
	private String operatorControlled_;
	private String format_ = "JOBQ0100";
	private transient ObjectDescription objectDescription_;
	
	/**
	 * Constructs a JobQueue.
	 * @param system  The system where the job queue resides.
	 * @param library  library The library containing the job queue.
	 * @param name  name The name of the job queue to retrieve.
	 */
	public JobQueue(AS400 system, String library, String name) {
		if (system == null)  throw new NullPointerException("system");
	    if (library == null) throw new NullPointerException("library");
	    if (name == null)    throw new NullPointerException("name");

	    system_ = system;
	    name_ = name;
	    library_ = library;
	}
	
	/**
	 * Constructs a JobQueue.
	 * @param system  The system where the job queue resides.
	 * @param path  path The fully qualified IFS path to the job queue.
	 */
	public JobQueue(AS400 system, QSYSObjectPathName path)
	  {
	    if (system == null)  throw new NullPointerException("system");
	    if (path == null)    throw new NullPointerException("path");

	    system_ = system;
	    name_ = path.getObjectName();
	    library_ = path.getLibraryName();
	  }
	
	/**
	   * Refreshes the values for all attributes of the job queue.
	   *
	   * @throws AS400Exception If the system returns an error message.
	   * @throws AS400SecurityException If a security or authority error occurs.
	   * @throws ErrorCompletingRequestException If an error occurs before the request is completed.
	   * @throws InterruptedException If this thread is interrupted.
	   * @throws ObjectDoesNotExistException If the object does not exist on the system.
	   * @throws IOException If an error occurs while communicating with the system.
	   **/
	public void refresh() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, ObjectDoesNotExistException, IOException
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
	    
	    // concat jobq name + library for program parameter[3]
	    StringBuffer qualifiedJobQName = new StringBuffer(20);
	    qualifiedJobQName.append(name_);
	    for (int i = 0; i < (10 - name_.length()); i++) {
	      qualifiedJobQName.append(" ");
	    }
	    qualifiedJobQName.append(library_);
	    
	    ProgramParameter[] parms = new ProgramParameter[5];
	    int len = 2048;
	    // receiver variable
	    parms[0] = new ProgramParameter(len);
	    // length of receiver variable
	    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(len));
	    // format name
	    parms[2] = new ProgramParameter(conv.stringToByteArray(format_));
	    // job description name
	    parms[3] = new ProgramParameter(text20.toBytes(qualifiedJobQName.toString().trim().toUpperCase()));

	    parms[4] = errorCode_;

	    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QSPRJOBQ.PGM", parms);
	    
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
	                  "JobQueue: Not enough bytes, trying again. Bytes returned = "
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
	    
	    jobQueueName_ = conv.byteArrayToString(data, 8, 10).trim();
	    jobQueueLibrary_ = conv.byteArrayToString(data, 18, 10).trim();
	    operatorControlled_ = conv.byteArrayToString(data, 28, 10).trim();
	    authorityCheck_ = conv.byteArrayToString(data, 38, 10).trim();
	    numberOfJobs_ = BinaryConverter.byteArrayToInt(data, 48);
	    jobQueueStatus_ = conv.byteArrayToString(data, 52, 10).trim();
	    subsystemName_ = conv.byteArrayToString(data, 62, 10).trim();
	    textDescription_= conv.byteArrayToString(data, 72, 50).trim();
	    subsystemLibrary_ = conv.byteArrayToString(data, 122, 10).trim();
	    sequenceNumber_ = BinaryConverter.byteArrayToInt(data, 132);
	    maxActive_ = BinaryConverter.byteArrayToInt(data, 136);
	    currentActive_ = BinaryConverter.byteArrayToInt(data, 140);
	    
	    loaded_ = true;  
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
	      IllegalStateException throwException = new IllegalStateException(e.getMessage());
	      try {
	        throwException.initCause(e); 
	      } catch (Throwable t) {} 
	      throw throwException;
	    }
	  }
	  
	  /**
	   * Return the job queue name
	   * @return  the job queue name
	   */
	  public String getJobQName()
	  {
	    if (!loaded_)  loadInformation();
	    return jobQueueName_;
	  }
	  
	  /**
	   * Return the library containing the job queue.
	   * @return  the library containing the job queue.
	   */
	  public String getJobQLibrary()
	  {
	    if (!loaded_)  loadInformation();
	    return jobQueueLibrary_;
	  }
	  
	  /**
	   * Return The name of the subsystem that can receive jobs from this job queue
	   * @return The name of the subsystem that can receive jobs from this job queue
	   */
	  public String getSubsystemName()
	  {
	    if (!loaded_)  loadInformation();
	    return subsystemName_;
	  }
	  
	  /**
	   * Return The library in which the subsystem description resides
	   * @return The library in which the subsystem description resides
	   */
	  public String getSubsystemLibrary()
	  {
	    if (!loaded_)  loadInformation();
	    return subsystemLibrary_;
	  }
	  
	  /**
	   * Return Whether a user who has job control authority is allowed to control this job queue and manage the jobs on the queue.
	   * @return Whether a user who has job control authority is allowed to control this job queue and manage the jobs on the queue.
	   * The possible values are: *YES
	   *                          *NO
	   */
	  public String getOperatorControlled()
	  {
	    if (!loaded_)  loadInformation();
	    return operatorControlled_;
	  }
	  
	  /**
	   * Whether the user must be the owner of the queue in order to control the queue by holding or releasing the queue
	   * @return Whether the user must be the owner of the queue in order to control the queue by holding or releasing the queue
	   * The possible values are: *OWNER
	   *                          *DTAAUT
	   */
	  public String getAuthorityCheck()
	  {
	    if (!loaded_)  loadInformation();
	    return authorityCheck_;
	  }
	  
	  /**
	   * Return The number of jobs in the queue
	   * @return The number of jobs in the queue
	   */
	  public int getNumberOfJobs()
	  {
	    if (!loaded_)  loadInformation();
	    return numberOfJobs_;
	  }
	  
	  /**
	   * Return The status of the job queue
	   * @return The status of the job queue
	   * The possible values are: RELEASED
	   *                          HELD
	   */
	  public String getJobQueueStatus()
	  {
	    if (!loaded_)  loadInformation();
	    return jobQueueStatus_;
	  }
	  
	  /**
	   * Return Text that briefly describes the job queue
	   * @return Text that briefly describes the job queue
	   */
	  public String getTextDescription()
	  {
	    if (!loaded_)  loadInformation();
	    return textDescription_;
	  }
	  
	  /**
	   * Return The job queue entry sequence number
	   * @return The job queue entry sequence number
	   */
	  public int getSequenceNumber()
	  {
	    if (!loaded_)  loadInformation();
	    return sequenceNumber_;
	  }
	  
	  /**
	   * Return The maximum number of jobs that can be active at the same time through this job queue entry. A -1 in this field indicates that the value is *NOMAX.
	   * @return The maximum number of jobs that can be active at the same time through this job queue entry
	   */
	  public int getMaxActive()
	  {
	    if (!loaded_)  loadInformation();
	    return maxActive_;
	  }
	  
	  /**
	   * Return The current number of jobs that are active that came through this job queue entry
	   * @return The current number of jobs that are active that came through this job queue entry
	   */
	  public int getCurrentActive()
	  {
	    if (!loaded_)  loadInformation();
	    return currentActive_;
	  }
	  
	  /**
	   * Sets the system.  Cannot be changed after the object
	   * has established a connection to the system.
	   *
	   * @param system The system where the job queue resides.
	   **/
	  public void setSystem(AS400 system) {
		  if (system == null)  throw new NullPointerException("system");
		  if (loaded_)
		      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
		  system_ = system;
	  }
	  
	  /**
	   * Sets the job queue name.  Cannot be changed after the object
	   * has established a connection to the system.
	   *
	   * @param name the job queue name
	   **/
	  public void setName(String name)
	  {
	    if (name == null)  throw new NullPointerException("name");
	    if (loaded_)
	      throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
	    name_ = name;
	  }
	  
	  /**
	   * Sets the job queue library.  Cannot be changed after the object
	   * has established a connection to the system.
	   *
	   * @param name the job queue library
	   **/
	  public void setLibraryName(String library)
	  {
	    if (library == null)  throw new NullPointerException("library");
	    if (loaded_)
	      throw new ExtendedIllegalStateException("library", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
	    library_ = library;
	  }
	  
	  /**
	   * Set retrieve Job Queue format
	   * @param format
	   * The possible values are:
       * <ul>
       * <li> "JOBQ0100" format
       * <li> "JOBQ0200" format
       * </ul>
	   */
	  public void setFormat(String format) {
		  if (format == null)  throw new NullPointerException("format");
		  if (loaded_)
		      throw new ExtendedIllegalStateException("library", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
		  if (format.equalsIgnoreCase("JOBQ0100") || format.equalsIgnoreCase("JOBQ0200"))
		      format_ = format;
	  }
	  
	  /**
	  Determines if the subsystem currently exists on the system.
	  <br>More precisely, this method reports if the subsystem <em>description</em> exists on the system.
	  @return true if the subsystem exists; false if the subsystem does not exist.
	  @exception  AS400Exception  If the program call returns error messages.
	  @exception  AS400SecurityException  If a security or authority error occurs.
	  @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
	  @exception  InterruptedException  If this thread is interrupted.
	  @exception  IOException  If an error occurs while communicating with the system.
	  @exception ObjectDoesNotExistException If the system API (that queries subsystem description information) is missing.
	 **/
	 public boolean exists()
	     throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
	 {
	     if (objectDescription_ == null) { objectDescription_ = getObjDesc(); }
	     return objectDescription_.exists();
	 }
	 
	 /**
	 Returns an ObjectDescription instance representing the subsystem.
	 @return An ObjectDescription for the subsystem.
	 **/
	public ObjectDescription getObjectDescription()
	{
	    if (objectDescription_ == null) { objectDescription_ = getObjDesc(); }
	    return objectDescription_;
	}
	  
	/**
	Gets an ObjectDescription object representing the subsystem.
	**/
	private ObjectDescription getObjDesc()
	{
	    return new ObjectDescription(system_, library_, name_, "JOBQ");
	}

}
