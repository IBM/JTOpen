///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SpooledFile.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;

/**
 * The SpooledFile class represents an AS/400 spooled file.
 *  You can use an instance of this class to manipulate an individual
 *  AS/400 spooled file (hold, release, delete, send, read, and so on).
 * To create new spooled files on the AS/400, use the
 * SpooledFileOutputStream class.
 *
 * See <a href="../../../../SpooledFileAttrs.html">Spooled File Attributes</a> for
 * valid attributes.
 *
 * @see PrintObjectInputStream
 * @see PrintObjectPageInputStream
 * @see PrintObjectTransformedInputStream
 **/

/* @A2C - Changed to implement java.io.Serializable */
public class SpooledFile extends PrintObject
implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   
    static final long serialVersionUID = 4L;


    transient boolean       fMsgRetrieved_  = false;    /* @A2C - Changed to transient */

    // constructor used internally (not externalized since it takes
    // an ID code point
    SpooledFile(AS400 system, NPCPIDSplF id, NPCPAttribute attrs)
    {
       super(system, id, attrs, NPConstants.SPOOLED_FILE); // @B1C
    }


    // We have decide that spooled files are too transient to
    // be JavaBeans.


    /**
     * Constructs a SpooledFile object. It uses the specified system and
     * spooled file attributes that identify it on that system.
     *
     * @param system The system on which this spooled file exists.
     * @param name The name of the spooled file.
     * @param number The number of the spooled file.
     * @param jobName The name of the job that created the spooled file.
     * @param jobUser The user who created the spooled file.
     * @param jobNumber The number of the job that created the spooled file.
     *
     **/
    public SpooledFile(AS400 system,
                       String name,
                       int    number,
                       String jobName,
                       String jobUser,
                       String jobNumber)
    {
        super(system,
              new NPCPIDSplF(name,
                             number,
                             jobName,
                             jobUser,
                             jobNumber),
              null,
              NPConstants.SPOOLED_FILE); // @B1C

        // base class constructor checks for null system.
        if (name == null)
        {
	    Trace.log(Trace.ERROR, "Parameter 'name' is null.");
            throw new NullPointerException("name");
        }

        if (number < -1)    // @B2C  (changed from 1 to -1 to allow 0(=*ONLY) and -1(=*LAST))
        {
	    Trace.log(Trace.ERROR, "Parameter 'number' is less than -1.");   // @B2C
	    throw new ExtendedIllegalArgumentException(
                "number(" + number + ")",
                ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
	    }

        if (jobName == null)
        {
	    Trace.log(Trace.ERROR, "Parameter 'jobName' is null.");
            throw new NullPointerException("jobName");
        }

        if (jobUser == null)
        {
	    Trace.log(Trace.ERROR, "Parameter 'jobUser' is null.");
            throw new NullPointerException("jobUser");
        }

        if (jobNumber == null)
        {
	    Trace.log(Trace.ERROR, "Parameter 'jobNumber' is null.");
            throw new NullPointerException("jobNumber");
        }
    }
   // Alternate constructor for spooled files detached from jobs @B3 
    /**
     * Constructs a SpooledFile object. It uses the specified system and
     * spooled file attributes that identify it on that system.
     *
     * @param system The system on which this spooled file exists.
     * @param name The name of the spooled file.
     * @param number The number of the spooled file.
     * @param jobName The name of the job that created the spooled file.
     * @param jobUser The user who created the spooled file.
     * @param jobNumber The number of the job that created the spooled file.
     * @param jobSysName The name of the system where the spooled file was created.
     * @param createDate The date the spooled file was created on the system.
     * @param createTime The time the spooled file was created on the system.
     *
     **/
   
    public SpooledFile(AS400 system,
                       String name,
                       int    number,
                       String jobName,
                       String jobUser,
                       String jobNumber,
                       String jobSysName,
                       String createDate,
                       String createTime)
    {
        super(system,
              new NPCPIDSplF(name,
                             number,
                             jobName,
                             jobUser,
                             jobNumber,
                             jobSysName,
                             createDate,
                             createTime),
              null,
              NPConstants.SPOOLED_FILE); // @B1C

        // base class constructor checks for null system.
   
        if (name == null)
        {
	    Trace.log(Trace.ERROR, "Parameter 'name' is null.");
            throw new NullPointerException("name");
        }

        if (number < -1)    // @B2C  (changed from 1 to -1 to allow 0(=*ONLY) and -1(=*LAST))
        {
	    Trace.log(Trace.ERROR, "Parameter 'number' is less than -1.");   // @B2C
	    throw new ExtendedIllegalArgumentException(
                "number(" + number + ")",
                ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
	    }

        if (jobName == null)
        {
	    Trace.log(Trace.ERROR, "Parameter 'jobName' is null.");
            throw new NullPointerException("jobName");
        }

        if (jobUser == null)
        {
	    Trace.log(Trace.ERROR, "Parameter 'jobUser' is null.");
            throw new NullPointerException("jobUser");
        }

        if (jobNumber == null)
        {
	    Trace.log(Trace.ERROR, "Parameter 'jobNumber' is null.");
            throw new NullPointerException("jobNumber");
        }
        if (jobSysName == null)
        {
         Trace.log(Trace.ERROR, "Parameter 'jobSysName' is null.");
            throw new NullPointerException("jobSysName");
        }
        if (createDate == null)
        {
         Trace.log(Trace.ERROR, "Parameter 'createDate' is null.");
            throw new NullPointerException("createDate");
        }
        if (createTime == null)
        {
        Trace.log(Trace.ERROR, "Parameter 'createTime' is null.");
            throw new NullPointerException("createTime");
        }
    }



    /** Replies to the message that caused the spooled file to wait.
     *
     * @param reply The string that contains the reply for the message.
     *              The default reply can be obtained by calling
     *              the getMessage() method, and then calling the
     *              getDefaultReply() method on the message object that is returned.
     *              Other possible replies are given in the message help,
     *              which can also be retrieved from the message object returned
     *              on the getMessage() method.
     *
     * @exception AS400Exception If the AS/400 system  returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is
     *                                            completed.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                          AS/400 system is not at the correct level.
     **/
    public void answerMessage(String reply)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        if (impl_ == null)
            chooseImpl();
        ((SpooledFileImpl) impl_).answerMessage(reply);
        // update the spooled file attributes
        attrs = impl_.getAttrValue();
        fMsgRetrieved_ = ((SpooledFileImpl) impl_).getFMsgRetrieved();
    }



    // A3A - Added function
    /**
     * Chooses the implementation
     **/
    void chooseImpl()
    throws IOException, AS400SecurityException                              // @B1A
    {
        // We need to get the system to connect to...
        AS400 system = getSystem();
        if (system == null) {
            Trace.log( Trace.ERROR, "Attempt to use SpooledFile before setting system." );
            throw new ExtendedIllegalStateException("system",
                                    ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        impl_ = (SpooledFileImpl) system.loadImpl2("com.ibm.as400.access.SpooledFileImplRemote",
                                                   "com.ibm.as400.access.SpooledFileImplProxy");
        // @A4A  The connectService(AS400.PRINT) is done in setImpl()
        // @A4A  in the Printobject class.
        super.setImpl();  
    }
    
        
    
    // @C1A - added method
    /**
     * Creates a copy of the spooled file this (SpooledFile) object represents.  The
     * new spooled file is created on the same output queue and on the same system 
     * as the original spooled file. A reference to the new spooled file is returned.
     *
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         AS/400 system is not at the correct level.
     **/
    public SpooledFile copy()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        String name = getStringAttribute(ATTR_OUTPUT_QUEUE);
        OutputQueue outq = new OutputQueue(getSystem(), name);
        SpooledFile sf = copy(outq);
        return sf;
    }



    // @C1A - added method
    /**
     * Creates a copy of the spooled file this object represents.  The
     * new spooled file is created on the specified output queue.
     * A reference to the new spooled file is returned.
     *
     * @param outputQueue The output queue location to create the new version of the
     *       original spooled file.  The spooled file will be created to the first
     *       position on this output queue.  The output queue and this spooled
     *       file must reside on the same system.
     *
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         AS/400 system is not at the correct level.
     **/
    public SpooledFile copy(OutputQueue outputQueue)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        // choose implementations
        if (impl_ == null) {
            chooseImpl();
        }
        if (outputQueue.getImpl() == null) {     
            outputQueue.chooseImpl();        
        }                        
        
        NPCPIDSplF spID = 
        ((SpooledFileImpl) impl_).copy((OutputQueueImpl)outputQueue.getImpl()); 
    	
        try {
            spID.setConverter((new Converter(getSystem().getCcsid(), getSystem())).impl);
        }
        catch (UnsupportedEncodingException e) {
            if (Trace.isTraceErrorOn()) {
                Trace.log(Trace.ERROR, "Error initializing converter for spooled file.");
            }
        }
        SpooledFile sf = new SpooledFile(getSystem(), spID, null);
        return sf;
    }



    /**
      * Deletes the spooled file on the AS/400.
      *
      * @exception AS400Exception If the AS/400 system returns an error message.
      * @exception AS400SecurityException If a security or authority error occurs.
      * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
      * @exception IOException If an error occurs while communicating with the AS/400.
      * @exception InterruptedException If this thread is interrupted.
      **/
    public void delete()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException
    {
        if (impl_ == null)
            chooseImpl();
        ((SpooledFileImpl) impl_).delete();
    }



    /**
      * Returns an input stream that can be used to read the contents of the
      * spooled file.
      * This method will fail with an AS400Exception if the spooled file is
      * still being created (ATTR_SPLFSTATUS is *OPEN).
      *
      * @return The input stream object that can be used to read the contents
      *         of this spooled file.
      * @exception AS400Exception If the AS/400 system returns an error message.
      * @exception AS400SecurityException If a security or authority error occurs.
      * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
      * @exception IOException If an error occurs while communicating with the AS/400.
      * @exception InterruptedException If this thread is interrupted.
      * @exception RequestNotSupportedException If the requested function is not supported
      *                                         because the AS/400 system is not at the
      *                                         correct level.
      **/
    public PrintObjectInputStream getInputStream()
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException,
               RequestNotSupportedException
    {
        // possible open options that we could use -
        //  Open time commands - *YES or *NO
        //  Send through host print transform and params for that if YES
        // If we want to do those we''ll have to add another getInputStream
        // that takes a PrintParameterList as input...
        PrintObjectInputStream is = new PrintObjectInputStream(this, null);
        return is;
    }



    /**
      * Returns the name of the job that created the spooled file.
      * @return The job name.
      **/
    public String getJobName()
    {
        NPCPID IDCodePoint = getIDCodePoint();

        if( IDCodePoint == null ) {
            return EMPTY_STRING; // ""
        } else {
            return IDCodePoint.getStringValue(ATTR_JOBNAME);
        }
    }



    /**
      * Returns the number of the job that created the spooled file.
      * @return The job number.
      **/
    public String getJobNumber()
    {
        NPCPID IDCodePoint = getIDCodePoint();

        if( IDCodePoint == null ) {
            return EMPTY_STRING; // ""
        } else {
            return IDCodePoint.getStringValue(ATTR_JOBNUMBER);
        }
    }



    /**
     * Returns the ID of the user that created the spooled file.
     * @return The user ID.
     **/
    public String getJobUser()
    {
        NPCPID IDCodePoint = getIDCodePoint();

        if( IDCodePoint == null ) {
            return EMPTY_STRING; // ""
        } else {
            return IDCodePoint.getStringValue(ATTR_JOBUSER);
        }
    }

   // @B3A 
   // the next three attributes are added to provide the decoupled spooled file 
   // identity @B3A
     /**
     * Returns the name of the system where the spooled file was created.
     * @return The name of the system where the spooled file was created.
     **/
    public String getJobSysName()
    {
        NPCPID IDCodePoint = getIDCodePoint();
        if ( IDCodePoint == null ){
            return EMPTY_STRING;
        } else {
            return IDCodePoint.getStringValue(ATTR_JOBSYSTEM);
        }
    }// @B3A #1
     /**
     * Returns the date of the spooled file creation.
     * @return The date of the spooled file creation.
     **/
    public String getCreateDate()
    {
        NPCPID IDCodePoint = getIDCodePoint();
        if ( IDCodePoint == null ){
            return EMPTY_STRING;
        } else {
            return IDCodePoint.getStringValue(ATTR_DATE);
        }
    }// @B3A #2
     /**
     * Returns the time of spooled file creation.
     * @return The time of the spooled file creation.
     **/
    public String getCreateTime()
    {
        NPCPID IDCodePoint = getIDCodePoint();
        if ( IDCodePoint == null ){
            return EMPTY_STRING;
        } else {
            return IDCodePoint.getStringValue(ATTR_TIME);
        }
    }// @B3A #3
    
    /**
      * Returns the message that is associated with this spooled file.
      * A spooled file has a message associated with it if its
      * ATTR_SPLFSTATUS attribute returns *MESSAGE.
      *
      * @return The AS400Message object that contains the message text,
      *   type, severity, id, date, time, and default reply.
      * @exception AS400Exception If the AS/400 system returns an error message.
      * @exception AS400SecurityException If a security or authority error occurs.
      * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
      * @exception IOException If an error occurs while communicating with the AS/400.
      * @exception InterruptedException If this thread is interrupted.
      **/
    public AS400Message getMessage()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException
    {
        if (impl_ == null);
            chooseImpl();
        AS400Message msg = ((SpooledFileImpl) impl_).getMessage();
        fMsgRetrieved_   = ((SpooledFileImpl) impl_).getFMsgRetrieved();
        return msg;
    }



    /**
     * Returns the name of the spooled file.
     * @return The name of the spooled file.
     **/
    public String getName()
    {
        NPCPID IDCodePoint = getIDCodePoint();

        if( IDCodePoint == null )
        {
            return EMPTY_STRING; // ""
        } else {
            return IDCodePoint.getStringValue(ATTR_SPOOLFILE);
        }
    }



    /**
      * Returns the number of the spooled file.
      * @return The number of the spooled file.
      **/
    public int getNumber()
    {
        NPCPID IDCodePoint = getIDCodePoint();

        if( IDCodePoint == null ) {
            return 0;
        } else {
            return IDCodePoint.getIntValue(ATTR_SPLFNUM).intValue();
        }
    }



    // below function added 6/12/98 @A2A
    /**
     * Returns a page input stream that can be used to read the contents of the
     * spooled file, one page at a time.
     * <br>
     * See <a href="../../../../TransInStr.html">Example using PrintObjectPageInputStream</a>
     * <br>
     * @param pageStreamOptions A print parameter list that contains
     *  parameters for generating the page input stream. <br>
     * The following attributes are optional:
     * <UL>
     *   <LI> ATTR_MFGTYPE                 - Specifies the manufacturer type and model.
     *   <LI> ATTR_WORKSTATION_CUST_OBJECT - Specifies the integrated file system name of
     *                                       the workstation customization object to be
     *                                       used.  The workstation customizing object
     *                                       associated with the manufacturer, type, and
     *                                       model is the default.
     *   <LI> ATTR_PAPER_SOURCE_1          - Specifies the paper size of drawer 1.
     *   <LI> ATTR_PAPER_SOURCE_2          - Specifies the paper size of drawer 2.
     *   <LI> ATTR_VIEWING_FIDELITY        - Specifies the fidelity used to process pages.
     *
     * </UL>
     *
     * @return A page input stream object that can be used to read the contents
     *         of this spooled file, one page at a time.
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed,
                                                  or the spooled file format is not supported.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported
     *                                         because the AS/400 system is not at the
     *                                         correct level.
     **/
    public PrintObjectPageInputStream getPageInputStream(PrintParameterList pageStreamOptions)
	    throws AS400Exception,
	           AS400SecurityException,
	           ErrorCompletingRequestException,
	           IOException,
	           InterruptedException,
	           RequestNotSupportedException
    {
        PrintObjectPageInputStream is = new PrintObjectPageInputStream(this, pageStreamOptions);
        return is;
    }



    // below function added 6/12/98 @A2A
    /**
     * Returns a transformed input stream that can be used to read the contents of the
     * spooled file.
     * <br>
     * See <a href="../../../../TransInStr.html">Example using PrintObjectTransformedInputStream</a>
     * <br>
     * @param transformOptions A print parameter list that contains
     *  parameters for generating the transformed input stream. <br>
     * The following attribute MUST be set:
     * <UL>
     *   <LI> ATTR_MFGTYPE  - Specifies the manufacturer, type, and model.
     * </UL>
     * The following attributes are optional:
     * <UL>
     *   <LI> ATTR_WORKSTATION_CUST_OBJECT - Specifies the integrated file system name of
     *                                       the workstation customization object to be
     *                                       used.  The workstation customizing object
     *                                       associated with the manufacturer, type, and
     *                                       model is the default.
     *   <LI> ATTR_PAPER_SOURCE_1          - Specifies the paper size of drawer 1.
     *   <LI> ATTR_PAPER_SOURCE_2          - Specifies the paper size of drawer 2.
     *
     * </UL>
     *
     * @return The transformed input stream object that can be used to read the contents
     *         of the transformed spooled file.
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed,
     *                                            or the spooled file format is not supported.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported
     *                                         because the AS/400 system is not at the
     *                                         correct level.
     **/
    public PrintObjectTransformedInputStream getTransformedInputStream(PrintParameterList transformOptions)
	    throws AS400Exception,
	           AS400SecurityException,
	           ErrorCompletingRequestException,
	           IOException,
	           InterruptedException,
	           RequestNotSupportedException
    {
        PrintObjectTransformedInputStream is = new PrintObjectTransformedInputStream(this, transformOptions);
        return is;
    }



    /**
     * Holds the spooled file.
     * @param holdType When to hold the spooled file.
     *  May be any of the following values:
     * <UL>
     *   <LI> *IMMED - The spooled file is held immediately.
     *   <LI> *PAGEEND - The spooled file is held at the end of the current page.
     * </UL>
     *  <i>holdType</i> may be null.  If <i>holdType</i> is not specified, the default is
     * *IMMED.
     *
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         AS/400 system is not at the correct level.
     **/
    public void hold(String holdType)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        if (impl_ == null)
            chooseImpl();
        ((SpooledFileImpl) impl_).hold(holdType);
        // update the spooled file attributes
        attrs = impl_.getAttrValue();
    }



    /**
     * Moves the spooled file to another output queue or to another
     * position on the same output queue.
     *
     * @param targetSpooledFile The spooled file to move this
     *       spooled file after.  The targetSpooledFile and this spooled file
     *       must reside on the same AS/400.
     *
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         AS/400 system is not at the correct level.
     **/
    public void move(SpooledFile targetSpooledFile)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        if (impl_ == null)
            chooseImpl();
        if (targetSpooledFile.getImpl() == null) {      // @A4A
            targetSpooledFile.chooseImpl();             // @A4A
        }                                               // @A4A
        ((SpooledFileImpl) impl_).move((SpooledFileImpl)targetSpooledFile.getImpl()); // @A4C
    	//update the spooled file attributes
        attrs = impl_.getAttrValue();
    }



    /**
     * Moves the spooled file to another output queue.
     *
     * @param targetOutputQueue The output queue to move the
     *       spooled file to.  The spooled file will be moved to the first
     *       position on this output queue.  The output queue and this spooled
     *       file must reside on the same AS/400.
     *
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                          AS/400 system is not at the correct level.
     **/
    public void move(OutputQueue targetOutputQueue)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        if (impl_ == null)
            chooseImpl();
        if (targetOutputQueue.getImpl() == null) {      // @A4A
            targetOutputQueue.chooseImpl();             // @A4A
        }                                               // @A4A
        ((SpooledFileImpl) impl_).move((OutputQueueImpl)targetOutputQueue.getImpl());  // @A4C
    	// update the spooled file attributes
        attrs = impl_.getAttrValue();
    }



    /**
     * Moves the spooled file to the first position on the output queue.
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                          AS/400 system is not at the correct level.
     **/
    public void moveToTop()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        if (impl_ == null)
            chooseImpl();
        ((SpooledFileImpl) impl_).moveToTop();
    	// update the spooled file attributes
        attrs = impl_.getAttrValue();
    }



    /* @A2A - Added method below.
    /**
    Restores the state of the object from an input stream.
    This is used when deserializing an object.

    @param in   The input stream.

    @exception IOException Thrown if an IO error occurs.
    @exception ClassNotFoundException Thrown if class is not found.
    **/
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        fMsgRetrieved_ = false;
    }



    /**
     * Releases a held spooled file on the AS/400.
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                          AS/400 system is not at the correct level.
     **/
    public void release()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        if (impl_ == null)
            chooseImpl();
        ((SpooledFileImpl) impl_).release();
    	// update the spooled file attributes
        attrs = impl_.getAttrValue();
    }



    /**
      * Sends the spooled file to another user on the same system or to
      * a remote system on the network.  The equivalent of the AS/400
      * Send Network Spooled File
      * (SNDNETSPLF) command will be issued against the spooled file.
      *
      * @param sendOptions A print parameter list that contains the
      *  parameters for the send.  The following attributes MUST be set:
      * <UL>
      *   <LI> ATTR_TOUSERID  - Specifies the user ID to send the spooled file to.
      *   <LI> ATTR_TOADDRESS - Specifies the remote system to send the spooled file to.
      * </UL>
      * The following attributes are optional:
      * <UL>
      *   <LI> ATTR_DATAFORMAT - Specifies the data format in which to transmit the
      *                           spooled file.  May be either of *RCDDATA or
      *                           *ALLDATA.  *RCDDATA is the default.
      *   <LI> ATTR_VMMVSCLASS - Specifies the VM/MVS SYSOUT class for distributions
      *                          sent to a VM host system or to an MVS host system.
      *                          May be A to Z or 0 to 9.  A is the default.
      *   <LI> ATTR_SENDPTY - Specifies the queueing priority used for this spooled file
      *                        when it is being routed through a SNADS network.  May be
      *                        *NORMAL or *HIGH.  *NORMAL is the default.
      * </UL>
      *
      * @exception AS400Exception If the AS/400 system returns an error message.
      * @exception AS400SecurityException If a security or authority error occurs.
      * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
      * @exception IOException If an error occurs while communicating with the AS/400.
      * @exception InterruptedException If this thread is interrupted.
      **/
    public void sendNet(PrintParameterList sendOptions)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException
    {
        if (impl_ == null)
            chooseImpl();
        ((SpooledFileImpl) impl_).sendNet(sendOptions);
    }



    /**
      * Sends a spooled file to be printed on a remote system.
      * The equivalent of the AS/400 Send TCP/IP Spooled File
      * (SNDTCPSPLF) command will be issued against the spooled file.
      * This is the AS/400 version of the TCP/IP LPR command.
      *
      * @param sendOptions A print parameter list that contains the
      *  parameters for the send.  The following attributes MUST be set:
      * <UL>
      *   <LI> ATTR_RMTSYSTEM - Specifies the remote system to which the print
      *                          request will be sent.  May be a remote system
      *                          name or the special value *INTNETADR.
      *   <LI> ATTR_RMTPRTQ - Specifies the name of the destination print queue.
      * </UL>
      * The following attributes are optional:
      * <UL>
      *   <LI> ATTR_DELETESPLF - Specifies whether or not to delete the spooled file
      *                           after it has been successfully sent.  May be *NO
      *                           or *YES.   *NO is the default.
      *   <LI> ATTR_DESTOPTION - Specifies a destination-dependant option.  These options will
      *                          be sent to the remote system with the spooled file.
      *   <LI> ATTR_DESTINATION - Specifies the type of system to which the spooled file is
      *                           being sent.  When sending to other AS/400 systems, this value
      *                           should be *AS/400.  May also be *OTHER or *PSF/2.
      *                           *OTHER is the default.
      *   <LI> ATTR_INTERNETADDR - Specifies the Internet address of the receiving system.
      *   <LI> ATTR_MFGTYPE  - Specifies the manufacturer, type, and model when transforming print
      *                        data from SCS or AFP to ASCII.
      *   <LI> ATTR_SCS2ASCII - Specifies whether the print data is to be transformed to
      *                         ASCII.  May be *NO or *YES.  *NO is the default.
      *   <LI> ATTR_WSCUSTMOBJ - Specifies the name of the workstation customization object.
      *   <LI> ATTR_WSCUSTMOBJL - Specifies the name of the workstation customization object library.
      *   <LI> ATTR_SEPPAGE - Specifies whether to print the separator page.  May be
      *                        *NO or *YES.  *YES is the default.
      *   <LI> ATTR_USRDTATFMLIB - Specifies the name of the user data transform library.
      *   <LI> ATTR_USRDTATFM - Specifies the name of the user data transform.
      * </UL>
      *
      * @exception AS400Exception If the AS/400 system returns an error message.
      * @exception AS400SecurityException If a security or authority error occurs.
      * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
      * @exception IOException If an error occurs while communicating with the AS/400.
      * @exception InterruptedException If this thread is interrupted.
      **/
    public void sendTCP(PrintParameterList sendOptions)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException
    {
        if (impl_ == null)
            chooseImpl();
        ((SpooledFileImpl) impl_).sendTCP(sendOptions);
    }



    /**
     * Sets one or more attributes of the object.  See
     * <a href="../../../../SpooledFileAttrs.html">Spooled File Attributes</a> for
     * a list of valid attributes that can be changed.
     *
     * @param attributes A print parameter list that contains the
     *  attributes to be changed.
     *
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the AS/400
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                          AS/400 system is not at the correct level.
     **/
    public void setAttributes(PrintParameterList attributes)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        if (attributes == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'attributes' is null.");
            throw new NullPointerException("attributes");
        }

        if (impl_ == null)
            chooseImpl();
        ((SpooledFileImpl) impl_).setAttributes(attributes);

        // we changed the spooled file attributes on the host,
        // merge those changed attributes into our current attributes
        // here.
        if (attrs == null)
        {
            attrs = new NPCPAttribute();
        }
    	// update the spooled file attributes
        attrs = impl_.getAttrValue();
    }

} // SpooledFile class


