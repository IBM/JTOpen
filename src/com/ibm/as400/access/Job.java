///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: Job.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import com.ibm.as400.resource.ResourceException;
import com.ibm.as400.resource.RJob;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;



/**
The Job class represents an AS/400 job.  In order to access a job,
the system and either the job name, user name, and job number or
internal job identifier need to be set.  A valid combination of
these must be set by getting or setting any of the job's attributes.

<p>Some of the attributes have associated get and set methods
defined in this class.  These are provided for backwards compatibility
with previous versions of the AS/400 Toolbox for Java.  The complete
set of attribute values can be accessed using the
{@link com.ibm.as400.resource.RJob  RJob } class.

<p>Note: To obtain information about the job in which an
AS/400 program or command runs, do something like the following:
<pre>
AS400 sys = new AS400();
ProgramCall pgm = new ProgramCall(sys);
pgm.setThreadSafe(true); // indicates the program is to be run on-thread
String jobNumber = pgm.getJob().getNumber();
</pre>

@see com.ibm.as400.resource.RJob
@see CommandCall#getJob
@see ProgramCall#getJob
**/
public class Job
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;

//-----------------------------------------------------------------------------------------
// Private data.
//-----------------------------------------------------------------------------------------

            static final int        INTERNAL_JOB_ID_CCSID_  = 13488;

    private boolean                 cacheChanges_           = true;
    private RJob                    rJob_                   = null;



//-----------------------------------------------------------------------------------------
// Constructors.
//-----------------------------------------------------------------------------------------

/**
Constructs a Job object.
**/
    public Job()
    {
        rJob_ = new RJob();
    }



/**
Constructs a Job object.

@param system The system.
**/
    public Job(AS400 system)
    {
        rJob_ = new RJob(system);
    }



/**
Constructs a Job object.

@param system       The system.
@param name         The job name.  Specify "*" to indicate the job that this
                    program running in.
@param user         The user name.  This must be blank if name is "*".
@param number       The job number.  This must be blank if name is "*".
**/
    public Job(AS400 system,
               String name,
               String user,
               String number)
    {
        rJob_ = new RJob(system, name, user, number);
    }



/**
Constructs a Job object.  This sets the job name to "*INT".

@param system           The system.
@param internalJobID    The internal job identifier.
**/
    public Job(AS400 system, String internalJobID)
    {
        try {
            rJob_ = new RJob(system, CharConverter.stringToByteArray(INTERNAL_JOB_ID_CCSID_, system, internalJobID));
        }
        catch(UnsupportedEncodingException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Unable to convert internal job id.", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }
    }



/**
Constructs a Job object.

@param rJob    The RJob object.
**/
//
// This is a package scope constructor!
//
    Job(RJob rJob)
    {
        rJob_ = rJob;
    }



/**
Adds a PropertyChangeListener.  The specified PropertyChangeListener's
<b>propertyChange()</b> method will be called each time the value of
any bound property is changed.

@param listener The listener.
*/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        rJob_.addPropertyChangeListener(listener);
    }



/**
Adds a VetoableChangeListener.  The specified VetoableChangeListener's
<b>vetoableChange()</b> method will be called each time the value of
any constrained property is changed.

@param listener The listener.
*/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        rJob_.addVetoableChangeListener(listener);
    }



/**
Commits all uncommitted attribute changes.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.
**/
    public void commitChanges()
        throws AS400Exception,
               AS400SecurityException,
               ConnectionDroppedException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
        try {
            rJob_.commitAttributeChanges();
        }
        catch(ResourceException e) {
            e.unwrap();
        }
    }



/**
Gets an attribute value as a boolean and unwraps any ResourceExceptions.

@param attributeID      Identifies the attribute.
@return                 The attribute value.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.
**/
    private boolean customGetAttributeValueAsBoolean(Object attributeID)
    throws AS400Exception,
           AS400SecurityException,
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException,
           UnsupportedEncodingException
    {
        try {
            return ((Boolean)rJob_.getAttributeValue(attributeID)).booleanValue();
        }
        catch(ResourceException e) {
            e.unwrap();
            return false;
        }
    }



/**
Gets an attribute value as a Date and unwraps any ResourceExceptions.

@param attributeID      Identifies the attribute.
@return                 The attribute value.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.
**/
    private Date customGetAttributeValueAsDate(Object attributeID)
    throws AS400Exception,
           AS400SecurityException,
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException,
           UnsupportedEncodingException
    {
        try {
            return (Date)rJob_.getAttributeValue(attributeID);
        }
        catch(ResourceException e) {
            e.unwrap();
            return null;
        }
    }



/**
Gets an attribute value as an int and unwraps any ResourceExceptions.

@param attributeID      Identifies the attribute.
@return                 The attribute value.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.
**/
    private int customGetAttributeValueAsInt(Object attributeID)
    throws AS400Exception,
           AS400SecurityException,
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException,
           UnsupportedEncodingException
    {
        try {
            return ((Integer)rJob_.getAttributeValue(attributeID)).intValue();
        }
        catch(ResourceException e) {
            e.unwrap();
            return -1;
        }
    }



/**
Gets an attribute value as a String and unwraps any ResourceExceptions.

@param attributeID      Identifies the attribute.
@return                 The attribute value.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.
**/
    private String customGetAttributeValueAsString(Object attributeID)
    throws AS400Exception,
           AS400SecurityException,
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException,
           UnsupportedEncodingException
    {
        try {
            return (String)rJob_.getAttributeValue(attributeID);
        }
        catch(ResourceException e) {
            e.unwrap();
            return null;
        }
    }



/**
Gets an attribute value as a String[] and unwraps any ResourceExceptions.

@param attributeID      Identifies the attribute.
@return                 The attribute value.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.
**/
    private String[] customGetAttributeValueAsStringArray(Object attributeID)
    throws AS400Exception,
           AS400SecurityException,
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException,
           UnsupportedEncodingException
    {
        try {
            return (String[])rJob_.getAttributeValue(attributeID);
        }
        catch(ResourceException e) {
            e.unwrap();
            return null;
        }
    }



/**
Sets an attribute value as a boolean and unwraps any ResourceExceptions.

@param attributeID      Identifies the attribute.
@param value            The attribute value.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.
**/
    private void customSetAttributeValueAsBoolean(Object attributeID, boolean value)
    throws AS400Exception,
           AS400SecurityException,
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException,
           UnsupportedEncodingException
    {
        try {
            rJob_.setAttributeValue(attributeID, new Boolean(value));
        }
        catch(ResourceException e) {
            e.unwrap();
        }
    }



/**
Sets an attribute value as a Date and unwraps any ResourceExceptions.

@param attributeID      Identifies the attribute.
@param value            The attribute value.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.
**/
    private void customSetAttributeValueAsDate(Object attributeID, Date value)
    throws AS400Exception,
           AS400SecurityException,
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException,
           UnsupportedEncodingException
    {
        try {
            rJob_.setAttributeValue(attributeID, value);
        }
        catch(ResourceException e) {
            e.unwrap();
        }
    }



/**
Sets an attribute value as an int and unwraps any ResourceExceptions.

@param attributeID      Identifies the attribute.
@param value            The attribute value.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.
**/
    private void customSetAttributeValueAsInt(Object attributeID, int value)
    throws AS400Exception,
           AS400SecurityException,
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException,
           UnsupportedEncodingException
    {
        try {
            rJob_.setAttributeValue(attributeID, new Integer(value));
        }
        catch(ResourceException e) {
            e.unwrap();
        }
    }



/**
Sets an attribute value as a String and unwraps any ResourceExceptions.

@param attributeID      Identifies the attribute.
@param value            The attribute value.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.
**/
    private void customSetAttributeValueAsString(Object attributeID, String value)
    throws AS400Exception,
           AS400SecurityException,
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException,
           UnsupportedEncodingException
    {
        try {
            rJob_.setAttributeValue(attributeID, value);
        }
        catch(ResourceException e) {
            e.unwrap();
        }
    }



/**
Returns the number of auxiliary I/O requests for the initial thread of this job.

@return The number of auxiliary I/O requests.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#AUXILIARY_IO_REQUESTS
**/
    public int getAuxiliaryIORequests()
        throws AS400Exception,
               AS400SecurityException,
               ConnectionDroppedException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
        return customGetAttributeValueAsInt(RJob.AUXILIARY_IO_REQUESTS);
    }



/**
Returns a value which represents how this job handles break messages.

@return How this job handles break messages.  Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RJob#BREAK_MESSAGE_HANDLING_NORMAL RJob.BREAK_MESSAGE_HANDLING_NORMAL }
            - The message queue status determines break message handling.
        <li>{@link com.ibm.as400.resource.RJob#BREAK_MESSAGE_HANDLING_HOLD RJob.BREAK_MESSAGE_HANDLING_HOLD }
            - The message queue holds break messages until a user or program
            requests them.
        <li>{@link com.ibm.as400.resource.RJob#BREAK_MESSAGE_HANDLING_NOTIFY RJob.BREAK_MESSAGE_HANDLING_NOTIFY }
            - The system notifies the job's message queue when a message
            arrives.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#BREAK_MESSAGE_HANDLING
**/
    public String getBreakMessageHandling()
        throws AS400Exception,
               AS400SecurityException,
               ConnectionDroppedException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.BREAK_MESSAGE_HANDLING);
    }



/**
Indicates if the attribute value changes are cached.

@return true if attribute value changes are cached,
        false if attribute value changes are committed
        immediatly.
**/
    public boolean getCacheChanges()
    {
        return cacheChanges_;
    }



/**
Returns the coded character set identifier (CCSID).

@return The coded character set identifier.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#CCSID
**/
    public int getCodedCharacterSetID()
        throws AS400Exception,
               AS400SecurityException,
               ConnectionDroppedException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
        return customGetAttributeValueAsInt(RJob.CCSID);
    }



/**
Returns the completion status of the job.

@return The completion status of the job. Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RJob#COMPLETION_STATUS_NOT_COMPLETED RJob.COMPLETION_STATUS_NOT_COMPLETED }
            - The job has not completed.
        <li>{@link com.ibm.as400.resource.RJob#COMPLETION_STATUS_COMPLETED_NORMALLY RJob.COMPLETION_STATUS_COMPLETED_NORMALLY }
            - The job completed normally.
        <li>{@link com.ibm.as400.resource.RJob#COMPLETION_STATUS_COMPLETED_ABNORMALLY RJob.COMPLETION_STATUS_COMPLETED_ABNORMALLY }
            - The job completed abnormally.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#COMPLETION_STATUS
**/
    public String getCompletionStatus()
        throws AS400Exception,
               AS400SecurityException,
               ConnectionDroppedException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.COMPLETION_STATUS);
    }



/**
Returns the country ID.

@return The country ID.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#COUNTRY_ID
**/
    public String getCountryID()
        throws AS400Exception,
               AS400SecurityException,
               ConnectionDroppedException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.COUNTRY_ID);
    }



/**
Returns the amount of processing time used (in milliseconds) that
the job used.

@return The amount of processing time used (in milliseconds) the
        the job used.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#CPU_TIME_USED
**/
    public int getCPUUsed()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsInt(RJob.CPU_TIME_USED);
    }



/**
Returns the name of the current library for the initial thread of the job.

@return The name of the current library for the initial thread of the job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#CURRENT_LIBRARY
**/
    public String getCurrentLibrary()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.CURRENT_LIBRARY);
    }



/**
Indicates if a current library exists.

@return true if a current library exists, false otherwise.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#CURRENT_LIBRARY_EXISTENCE
**/
    public boolean getCurrentLibraryExistence()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsBoolean(RJob.CURRENT_LIBRARY_EXISTENCE);
    }



/**
Returns the date and time when the job was placed on the
system.

@return  The date and time when the job was placed on the system.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#DATE_ENTERED_SYSTEM
**/
    public Date getDate()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsDate(RJob.DATE_ENTERED_SYSTEM);
    }



/**
Returns the format in which dates are presented.

@return The format in which dates are presented.  Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RJob#DATE_FORMAT_YMD RJob.DATE_FORMAT_YMD }  - Year, month, and day format.
        <li>{@link com.ibm.as400.resource.RJob#DATE_FORMAT_MDY RJob.DATE_FORMAT_MDY }  - Month, day, and year format.
        <li>{@link com.ibm.as400.resource.RJob#DATE_FORMAT_DMY RJob.DATE_FORMAT_DMY }  - Day, month, and year format.
        <li>{@link com.ibm.as400.resource.RJob#DATE_FORMAT_JULIAN RJob.DATE_FORMAT_JULIAN }  - Julian format (year and day).
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#DATE_FORMAT
**/
    public String getDateFormat()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.DATE_FORMAT);
    }



/**
Returns the date separator. The date separator is used to separate days,
months, and years when representing a date.

@return The date separator.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#DATE_SEPARATOR
**/
    public String getDateSeparator()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.DATE_SEPARATOR);
    }



/**
Returns whether connections using distributed data management (DDM) protocols
remain active when they are not being used.

@return Whether connections using distributed data management (DDM) protocols
        remain active when they are not being used.  Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RJob#KEEP_DDM_CONNECTIONS_ACTIVE_KEEP RJob.KEEP_DDM_CONNECTIONS_ACTIVE_KEEP }  - The system keeps DDM connections active when there
            are no users.
        <li>{@link com.ibm.as400.resource.RJob#KEEP_DDM_CONNECTIONS_ACTIVE_DROP RJob.KEEP_DDM_CONNECTIONS_ACTIVE_DROP }  - The system ends a DDM connection when there are no
            users.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#KEEP_DDM_CONNECTIONS_ACTIVE
**/
    public String getDDMConversationHandling()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.KEEP_DDM_CONNECTIONS_ACTIVE);
    }



/**
Returns the decimal format used for this job.

@return The decimal format used for this job. Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RJob#DECIMAL_FORMAT_PERIOD RJob.DECIMAL_FORMAT_PERIOD }  - Uses a period for a decimal point, a comma
            for a 3-digit grouping character, and zero-suppresses to the left of
            the decimal point.
        <li>{@link com.ibm.as400.resource.RJob#DECIMAL_FORMAT_COMMA_I RJob.DECIMAL_FORMAT_COMMA_I }  - Uses a comma for a decimal point and a period for
            a 3-digit grouping character.  The zero-suppression character is in the
            second character (rather than the first) to the left of the decimal
            notation.  Balances with zero  values to the left of the comma are
            written with one leading zero.
        <li>{@link com.ibm.as400.resource.RJob#DECIMAL_FORMAT_COMMA_J RJob.DECIMAL_FORMAT_COMMA_J }  - Uses a comma for a decimal point, a period for a
            3-digit grouping character, and zero-suppresses to the left of the decimal
            point.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#DECIMAL_FORMAT
**/
  public String getDecimalFormat()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.DECIMAL_FORMAT);
    }



/**
Returns the default coded character set identifier (CCSID) for this job.

@return The default coded character set identifier (CCSID) for this job.
        The value will be 0 if the job is not active.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#DEFAULT_CCSID
**/
    public int getDefaultCodedCharacterSetIdentifier()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsInt(RJob.DEFAULT_CCSID);
    }



  //@A2A
/**
Returns the default maximum time (in seconds) that a thread in the job waits
for a system instruction.

@return The default maximum time (in seconds) that a thread in the job
        waits for a system instruction.  The value -1 means there is no maximum.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#DEFAULT_WAIT_TIME
**/
    public int getDefaultWait()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsInt(RJob.DEFAULT_WAIT_TIME);
    }



/**
Returns the action taken for interactive jobs when an I/O error occurs
for the job's requesting program device.

@return The action taken for interactive jobs when an I/O error occurs
        for the job's requesting program device.  Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RJob#DEVICE_RECOVERY_ACTION_MESSAGE RJob.DEVICE_RECOVERY_ACTION_MESSAGE }  - Signals the I/O error message to the
            application and lets the application program perform error recovery.
        <li>{@link com.ibm.as400.resource.RJob#DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE } RJob.DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE }  - Disconnects the
            job when an I/O error occurs.  When the job reconnects, the system sends an
            error message to the application program, indicating the job has reconnected
            and that the workstation device has recovered.
        <li>{@link com.ibm.as400.resource.RJob#DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST RJob.DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST }  - Disconnects
            the job when an I/O error occurs.  When the job reconnects, the system sends
            the End Request (ENDRQS) command to return control to the previous request
            level.
        <li>{@link com.ibm.as400.resource.RJob#DEVICE_RECOVERY_ACTION_END_JOB RJob.DEVICE_RECOVERY_ACTION_END_JOB }  - Ends the job when an I/O error occurs.  A
            message is sent to the job's log and to the history log (QHST) indicating
            the job ended because of a device error.
        <li>{@link com.ibm.as400.resource.RJob#DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST RJob.DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST }  - Ends the job when an I/O
            error occurs.  There is no job log produced for the job.  The system sends
            a message to the QHST log indicating the job ended because of a device error.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#DEVICE_RECOVERY_ACTION
**/
    public String getDeviceRecoveryAction()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.DEVICE_RECOVERY_ACTION);
    }



/**
Returns the message severity level of escape messages that can cause a batch
job to end.  The batch job ends when a request in the batch input stream sends
an escape message, whose severity is equal to or greater than this value, to the
request processing program.

@return The message severity level of escape messages that can cause a batch
        job to end.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#END_SEVERITY
**/
    public int getEndSeverity()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsInt(RJob.END_SEVERITY);
    }



/**
Returns additional information about the function the initial thread is currently
performing.  This information is updated only when a command is processed.

@return The additional information about the function the initial thread is currently
        performing.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#FUNCTION_NAME
**/
    public String getFunctionName()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.FUNCTION_NAME);
    }



/**
Returns the high-level function type the initial thread is performing,
if any.

@return The high-level function type the initial thread is performing,
        if any.  Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RJob#FUNCTION_TYPE_BLANK RJob.FUNCTION_TYPE_BLANK }  - The system is not performing a logged function.
        <li>{@link com.ibm.as400.resource.RJob#FUNCTION_TYPE_COMMAND RJob.FUNCTION_TYPE_COMMAND }  - A command is running interactively, or it is
            in a batch input stream, or it was requested from a system menu.
        <li>{@link com.ibm.as400.resource.RJob#FUNCTION_TYPE_DELAY RJob.FUNCTION_TYPE_DELAY }  - The initial thread of the job is processing
            a Delay Job (DLYJOB) command.
        <li>{@link com.ibm.as400.resource.RJob#FUNCTION_TYPE_GROUP RJob.FUNCTION_TYPE_GROUP }  - The Transfer Group Job (TFRGRPJOB) command
            suspended the job.
        <li>{@link com.ibm.as400.resource.RJob#FUNCTION_TYPE_INDEX RJob.FUNCTION_TYPE_INDEX }  - The initial thread of the job is rebuilding
            an index (access path).
        <li>{@link com.ibm.as400.resource.RJob#FUNCTION_TYPE_IO RJob.FUNCTION_TYPE_IO }  - The job is a subsystem monitor that is performing
            input/output (I/O) operations to a work station.
        <li>{@link com.ibm.as400.resource.RJob#FUNCTION_TYPE_LOG RJob.FUNCTION_TYPE_LOG }  - The system logs history information in a database
            file.
        <li>{@link com.ibm.as400.resource.RJob#FUNCTION_TYPE_MENU RJob.FUNCTION_TYPE_MENU }  - The initial thread of the job is currently
            at a system menu.
        <li>{@link com.ibm.as400.resource.RJob#FUNCTION_TYPE_MRT RJob.FUNCTION_TYPE_MRT }  - The job is a multiple requester terminal (MRT)
            job is the {@link com.ibm.as400.resource.RJob#JOB_TYPE job type }  is {@link com.ibm.as400.resource.RJob#JOB_TYPE_BATCH JOB_TYPE_BATCH }
            and the {@link com.ibm.as400.resource.RJob#JOB_SUBTYPE job subtype }  is {@link com.ibm.as400.resource.RJob#JOB_SUBTYPE_MRT JOB_SUBTYPE_MRT } ,
            or it is an interactive job attached to an MRT job if the
            {@link com.ibm.as400.resource.RJob#JOB_TYPE job type }  is {@link com.ibm.as400.resource.RJob#JOB_TYPE_INTERACTIVE JOB_TYPE_INTERACTIVE } .
        <li>{@link com.ibm.as400.resource.RJob#FUNCTION_TYPE_PROCEDURE RJob.FUNCTION_TYPE_PROCEDURE }  - The initial thread of the job is running
            a procedure.
        <li>{@link com.ibm.as400.resource.RJob#FUNCTION_TYPE_PROGRAM RJob.FUNCTION_TYPE_PROGRAM }  - The initial thread of the job is running
            a program.
        <li>{@link com.ibm.as400.resource.RJob#FUNCTION_TYPE_SPECIAL RJob.FUNCTION_TYPE_SPECIAL }  - The function type is special.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#FUNCTION_TYPE
**/
    public String getFunctionType()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.FUNCTION_TYPE);
    }



/**
Returns how the job answers inquiry messages.

@return How the job answers inquiry messages.  Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RJob#INQUIRY_MESSAGE_REPLY_REQUIRED RJob.INQUIRY_MESSAGE_REPLY_REQUIRED }  - The job requires an answer for any inquiry
            messages that occur while this job is running.
        <li>{@link com.ibm.as400.resource.RJob#INQUIRY_MESSAGE_REPLY_DEFAULT RJob.INQUIRY_MESSAGE_REPLY_DEFAULT }  - The system uses the default message reply to
            answer any inquiry messages issued while this job is running.  The default
            reply is either defined in the message description or is the default system
            reply.
        <li>{@link com.ibm.as400.resource.RJob#INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST RJob.INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST } The system reply list is
            checked to see if there is an entry for an inquiry message issued while this
            job is running.  If a match occurs, the system uses the reply value for that
            entry.  If no entry exists for that message, the system uses an inquiry message.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#INQUIRY_MESSAGE_REPLY
**/
    public String getInquiryMessageReply()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.INQUIRY_MESSAGE_REPLY);
    }



/**
Returns the number of interactive transactions.

@return The number of interactive transactions.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#INTERACTIVE_TRANSACTIONS
**/
    public int getInteractiveTransactions()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsInt(RJob.INTERACTIVE_TRANSACTIONS);
    }



/**
Returns the internal job identifier.

@return The internal job identifier.
**/
    public String getInternalJobID()
    {
        try {
            return CharConverter.byteArrayToString(INTERNAL_JOB_ID_CCSID_, getSystem(), rJob_.getInternalJobID());
        }
        catch(UnsupportedEncodingException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Unable to convert internal job id.", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }
    }



/**
Returns the identifier assigned to the job by the system to collect resource
use information for the job when job accounting is active.

@return The identifier assigned to the job by the system to collect resource
        use information for the job when job accounting is active.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#ACCOUNTING_CODE
**/
    public String getJobAccountingCode()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.ACCOUNTING_CODE);
    }



/**
Returns the date and time when the job began to run on the system.

@return The date and time when the job began to run on the system.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#DATE_STARTED
**/
    public Date getJobActiveDate()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsDate(RJob.DATE_STARTED);
    }



/**
Returns the date to be used for the job.

@return The date to be used for the job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#JOB_DATE
**/
    public Date getJobDate()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsDate(RJob.JOB_DATE);
    }



/**
Returns the fully qualified integrated path name for the job
description.

@return The fully qualified integrated path name for the job
        description.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#JOB_DESCRIPTION
@see QSYSObjectPathName
**/
    public String getJobDescription()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.JOB_DESCRIPTION);
    }



/**
Returns the date and time when the job was placed on the system.

@return The date and time when the job was placed on the system.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#DATE_ENTERED_SYSTEM
**/
    public Date getJobEnterSystemDate()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsDate(RJob.DATE_ENTERED_SYSTEM);
    }



/**
Returns the job log.

@return The job log.
**/
    public JobLog getJobLog()
    {
        try {
            // Rather than using name_, user_, and number_, I will get
            // their attribute values.  This will work when CURRENT or
            // an internal job id is specified.
            return new JobLog(getSystem(),
                              (String)rJob_.getAttributeValue(RJob.JOB_NAME),
                              (String)rJob_.getAttributeValue(RJob.USER_NAME),
                              (String)rJob_.getAttributeValue(RJob.JOB_NUMBER));
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error getting the job log", e);
            return null;
        }
    }


/**
Returns the action to take when the message queue is full.

@return The action to take when the message queue is full.  Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RJob#MESSAGE_QUEUE_ACTION_NO_WRAP RJob.MESSAGE_QUEUE_ACTION_NO_WRAP }  - Do not wrap. This action causes the job to end.
        <li>{@link com.ibm.as400.resource.RJob#MESSAGE_QUEUE_ACTION_WRAP RJob.MESSAGE_QUEUE_ACTION_WRAP }  - Wrap to the beginning and start filling again.
        <li>{@link com.ibm.as400.resource.RJob#MESSAGE_QUEUE_ACTION_PRINT_WRAP RJob.MESSAGE_QUEUE_ACTION_PRINT_WRAP }  - Wrap the message queue and print the
            messages that are being overlaid because of the wrapping.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#MESSAGE_QUEUE_ACTION
**/
    public String getJobMessageQueueFullAction()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.MESSAGE_QUEUE_ACTION);
    }



/**
Returns the maximum size (in megabytes) that the job message queue can become.

@return The maximum size (in megabytes) that the job message queue can become.
        The range is 2 through 64.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#MESSAGE_QUEUE_MAX_SIZE
**/
    public int getJobMessageQueueMaximumSize()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsInt(RJob.MESSAGE_QUEUE_MAX_SIZE);
    }



/**
Returns the date and time the job was put on the job queue.

@return The date and time the job was put on the job queue.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#JOB_QUEUE_DATE
**/
    public Date getJobPutOnJobQueueDate()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsDate(RJob.JOB_QUEUE_DATE);
    }



/**
Returns the date and time the job is scheduled to become active.

@return The date and time the job is scheduled to become active.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#SCHEDULE_DATE
**/
    public Date getScheduleDate()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsDate(RJob.SCHEDULE_DATE);
    }



/**
Returns the status of the job on the job queue.

@return The status of the job on the job queue.  Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RJob#JOB_QUEUE_STATUS_BLANK RJob.JOB_QUEUE_STATUS_BLANK }  - The job is not on a job queue.
        <li>{@link com.ibm.as400.resource.RJob#JOB_QUEUE_STATUS_SCHEDULED RJob.JOB_QUEUE_STATUS_SCHEDULED }  - The job will run as scheduled.
        <li>{@link com.ibm.as400.resource.RJob#JOB_QUEUE_STATUS_HELD RJob.JOB_QUEUE_STATUS_HELD }  - The job is being held on the job queue.
        <li>{@link com.ibm.as400.resource.RJob#JOB_QUEUE_STATUS_RELEASED RJob.JOB_QUEUE_STATUS_RELEASED }  - The job is ready to be selected.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#JOB_QUEUE_STATUS
**/
    public String getJobStatusInJobQueue()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.JOB_QUEUE_STATUS);
    }



/**
Returns the current setting of the job switches used by this job.

@return The current setting of the job switches used by this job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#JOB_SWITCHES
**/
    public String getJobSwitches()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.JOB_SWITCHES);
    }



/**
Returns the language identifier associated with this job.

@return The language identifier associated with this job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#LANGUAGE_ID
**/
    public String getLanguageID()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.LANGUAGE_ID);
    }



/**
Returns a value indicating whether or not messages are logged for
CL programs.

@return The value indicating whether or not messages are logged for
        CL programs. Possible values are: {@link com.ibm.as400.resource.RJob#YES RJob.YES }  and
        {@link com.ibm.as400.resource.RJob#NO RJob.NO } .

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#LOG_CL_PROGRAMS
**/
    public String getLoggingCLPrograms()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return (customGetAttributeValueAsBoolean(RJob.LOG_CL_PROGRAMS) ? RJob.YES : RJob.NO);
    }



// @D1C
/**
Returns the type of information logged.

@return The type of information logged.  Possible values are:
        <ul>
        <li>0  - No messages are logged.
        <li>1  - All messages sent
            to the job's external message queue with a severity greater than or equal to
            the message logging severity are logged.
        <li>2  -
            Requests or commands from CL programs for which the system issues messages with
            a severity code greater than or equal to the logging severity and all messages
            associated with those requests or commands that have a severity code greater
            than or equal to the logging severity are logged.
        <li>3  - All requests or commands from CL programs and all messages
            associated with those requests or commands that have a severity code greater
            than or equal to the logging severity are logged.
        <li>4  - All requests or commands from CL programs and all messages
            with a severity code greater than or equal to the logging severity are logged.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#LOGGING_LEVEL
**/
    public int getLoggingLevel()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return Integer.parseInt(customGetAttributeValueAsString(RJob.LOGGING_LEVEL)); // @D1C
    }



/**
Returns the minimum severity level that causes error messages to be logged
in the job log.

@return The minimum severity level that causes error messages to be logged
        in the job log.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#LOGGING_SEVERITY
**/
    public int getLoggingSeverity()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsInt(RJob.LOGGING_SEVERITY);
    }



/**
Returns the level of message text that is written in the job log
or displayed to the user.

@return The level of message text that is written in the job log
        or displayed to the user.  Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RJob#LOGGING_TEXT_MESSAGE RJob.LOGGING_TEXT_MESSAGE }  - Only the message is written to the job log.
        <li>{@link com.ibm.as400.resource.RJob#LOGGING_TEXT_SECLVL RJob.LOGGING_TEXT_SECLVL }  - Both the message and the message help for the
            error message are written to the job log.
        <li>{@link com.ibm.as400.resource.RJob#LOGGING_TEXT_NO_LIST RJob.LOGGING_TEXT_NO_LIST }  - If the job ends normally, there is no job log.
            If the job ends abnormally, there is a job log.  The messages appearing in the
            job log contain both the message and the message help.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#LOGGING_TEXT
**/
    public String getLoggingText()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.LOGGING_TEXT);
    }



/**
Returns the mode name of the advanced program-to-program
communications (APPC) device that started the job.

@return The mode name of the advanced program-to-program
        communications (APPC) device that started the job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#MODE
**/
    public String getModeName()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.MODE);
    }



/**
Returns the job name.

@return The job name.
**/
    public String getName()
    {
        return rJob_.getName();
    }



/**
Returns the job number.

@return The job number.
**/
    public String getNumber()
    {
        return rJob_.getNumber();
    }



/**
Returns the number of libraries in the system portion of the library list of the
initial thread.

@return The number of libraries in the system portion of the library list of the
        initial thread.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#SYSTEM_LIBRARY_LIST
**/
    public int getNumberOfLibrariesInSYSLIBL()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsStringArray(RJob.SYSTEM_LIBRARY_LIST).length;
    }


/**
Returns the number of libraries in the user portion of the library list of
the initial thread.

@return The number of libraries in the user portion of the library list of
the initial thread.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#USER_LIBRARY_LIST
**/
    public int getNumberOfLibrariesInUSRLIBL()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsStringArray(RJob.USER_LIBRARY_LIST).length;
    }



/**
Returns the number of libraries that contain product information
for the initial thread.

@return The number of libraries that contain product information
        for the initial thread.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#PRODUCT_LIBRARIES
**/
    public int getNumberOfProductLibraries()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsStringArray(RJob.PRODUCT_LIBRARIES).length;
    }



/**
Returns the fully qualified integrated file system path name of the default
output queue that is used for spooled output produced by this job.

@return The fully qualified integrated file system path name of the default
        output queue that is used for spooled output produced by this job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#OUTPUT_QUEUE
@see QSYSObjectPathName
**/
    public String getOutputQueue()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.OUTPUT_QUEUE);
    }



/**
Returns the output priority for spooled files that this job produces.

@return The output priority for spooled files that this job produces.
        The highest priority is 0 and the lowest is 9.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#OUTPUT_QUEUE_PRIORITY
**/
    public int  getOutputQueuePriority()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsInt(RJob.OUTPUT_QUEUE_PRIORITY);
    }



/**
Returns the identifier of the system-related pool from which the job's
main storage is allocated.

@return The identifier of the system-related pool from which the job's
        main storage is allocated.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#SYSTEM_POOL_ID
**/
    public int getPoolIdentifier()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsInt(RJob.SYSTEM_POOL_ID);
    }



/**
Returns the printer device used for printing output from this job.

@return The printer device used for printing output from this job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#PRINTER_DEVICE_NAME
**/
    public String getPrinterDeviceName()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.PRINTER_DEVICE_NAME);
    }



/**
Returns a value indicating whether border and header information is provided when
the Print key is pressed.

@return The value indicating whether border and header information is provided when
        the Print key is pressed.
        <ul>
        <li>{@link com.ibm.as400.resource.RJob#NONE RJob.NONE }  - The border and header information is not
            included with output from the Print key.
        <li>{@link com.ibm.as400.resource.RJob#PRINT_KEY_FORMAT_BORDER RJob.PRINT_KEY_FORMAT_BORDER }  - The border information
            is included with output from the Print key.
        <li>{@link com.ibm.as400.resource.RJob#PRINT_KEY_FORMAT_HEADER RJob.PRINT_KEY_FORMAT_HEADER }  - The header information
            is included with output from the Print key.
        <li>{@link com.ibm.as400.resource.RJob#PRINT_KEY_FORMAT_ALL RJob.PRINT_KEY_FORMAT_ALL }  - The border and header information
            is included with output from the Print key.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#PRINT_KEY_FORMAT
**/
    public String getPrintKeyFormat()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.PRINT_KEY_FORMAT);
    }



/**
Returns the line of text, if any, that is printed at the
bottom of each page of printed output for the job.

@return The line of text, if any, that is printed at the
        bottom of each page of printed output for the job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#PRINT_TEXT
**/
    public String getPrintText()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.PRINT_TEXT);
    }



/**
Returns the libraries that contain product information for the initial thread.

@return The libraries that contain product information for the initial thread.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#PRODUCT_LIBRARIES
**/
    public String[] getProductLibraries()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsStringArray(RJob.PRODUCT_LIBRARIES);
    }



  //@A2A
/**
Indicates whether the job is eligible to be moved out of main storage
and put into auxiliary storage at the end of a time slice or when it is
beginning a long wait.

@return true the job is eligible to be moved out of main storage
        and put into auxiliary storage at the end of a time slice or when it is
        beginning a long wait, or false otherwise.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#ELIGIBLE_FOR_PURGE
**/
    public boolean getPurge()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsBoolean(RJob.ELIGIBLE_FOR_PURGE);
    }



/**
Returns the fully qualified integrated file system path name
of the job queue that the job is on, or that the job was on if
it is active.

@return The fully qualified integrated file system path name
        of the job queue that the job is on, or that the job was on if
        it is active.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#JOB_QUEUE
@see QSYSObjectPathName
**/
    public String getQueue()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.JOB_QUEUE);
    }



/**
Returns the scheduling priority of the job compared to other jobs on the same job queue.

@return The scheduling priority of the job compared to other jobs on the same job queue.
        The highest priority is 0 and the lowest is 9.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#JOB_QUEUE_PRIORITY
**/
    public int getQueuePriority()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsInt(RJob.JOB_QUEUE_PRIORITY);
    }


/**
Returns the routing data that is used to determine the routing entry
that identifies the program to start for the routing step.

@return The routing data that is used to determine the routing entry
that identifies the program to start for the routing step.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#ROUTING_DATA
**/
    public String getRoutingData()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.ROUTING_DATA);
    }


/**
Returns the priority at which the job is currently running, relative
to other jobs on the system.

@return The priority at which the job is currently running, relative
        to other jobs on the system.  The run priority ranges from 1
        (highest priority) to 99 (lowest priority).

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#RUN_PRIORITY
**/
    public int getRunPriority()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsInt(RJob.RUN_PRIORITY);
    }



/**
Indicates whether the job is to be treated like a signed-on user on
the system.

@return true if the job is to be treated like a signed-on user on
        the system, false otherwise.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#SIGNED_ON_JOB
**/
    public boolean getSignedOnJob()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsBoolean(RJob.SIGNED_ON_JOB);
    }



/**
Returns the fully qualified integrated file system path name of the
sort sequence table.

@return The fully qualified integrated file system path name of the
        sort sequence table.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#SORT_SEQUENCE_TABLE
@see QSYSObjectPathName
**/
    public String getSortSequenceTable()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.SORT_SEQUENCE_TABLE);
    }



/**
Returns the job status.

@return The job status. Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RJob#JOB_STATUS_ACTIVE RJob.JOB_STATUS_ACTIVE }  - The job is active.
        <li>{@link com.ibm.as400.resource.RJob#JOB_STATUS_JOBQ RJob.JOB_STATUS_JOBQ }  - The job is currently on a job queue.
        <li>{@link com.ibm.as400.resource.RJob#JOB_STATUS_OUTQ RJob.JOB_STATUS_OUTQ }  - The job has completed running, but still has output
            on an output queue.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#JOB_STATUS
**/
    public String getStatus()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.JOB_STATUS);
    }



/**
Returns a value indicating status messages are displayed for this job.

@return The value indicating status messages are displayed for this job.
        <ul>
        <li>{@link com.ibm.as400.resource.RJob#NONE RJob.NONE }  -
            This job does not display status messages.
        <li>{@link com.ibm.as400.resource.RJob#STATUS_MESSAGE_HANDLING_NORMAL RJob.STATUS_MESSAGE_HANDLING_NORMAL }  -
            This job displays status messages.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#STATUS_MESSAGE_HANDLING
**/
    public String getStatusMessageHandling()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.STATUS_MESSAGE_HANDLING);
    }



/**
Returns the fully qualified integrated file system path name of the
subsystem description for the subsystem in which the job is running.

@return The fully qualified integrated file system path name of the
        subsystem description for the subsystem in which the job is
        running.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#SUBSYSTEM
@see QSYSObjectPathName
**/
    public String getSubsystem()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.SUBSYSTEM);
    }


/**
Returns additional information about the job type.

@return Additional information about the job type. Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RJob#JOB_SUBTYPE_BLANK RJob.JOB_SUBTYPE_BLANK }  - The job has no special subtype or is not a valid job.
        <li>{@link com.ibm.as400.resource.RJob#JOB_SUBTYPE_IMMEDIATE RJob.JOB_SUBTYPE_IMMEDIATE }  - The job is an immediate job.
        <li>{@link com.ibm.as400.resource.RJob#JOB_SUBTYPE_PROCEDURE_START_REQUEST RJob.JOB_SUBTYPE_PROCEDURE_START_REQUEST }  - The job started
            with a procedure start request.
        <li>{@link com.ibm.as400.resource.RJob#JOB_SUBTYPE_MACHINE_SERVER_JOB RJob.JOB_SUBTYPE_MACHINE_SERVER_JOB }  - The job is an AS/400
            Advanced 36 machine server job.
        <li>{@link com.ibm.as400.resource.RJob#JOB_SUBTYPE_PRESTART RJob.JOB_SUBTYPE_PRESTART }  - The job is a prestart job.
        <li>{@link com.ibm.as400.resource.RJob#JOB_SUBTYPE_PRINT_DRIVER RJob.JOB_SUBTYPE_PRINT_DRIVER }  - The job is a print driver job.
        <li>{@link com.ibm.as400.resource.RJob#JOB_SUBTYPE_MRT RJob.JOB_SUBTYPE_MRT }  - The job is a System/36 multiple requester terminal
            (MRT) job.
        <li>{@link com.ibm.as400.resource.RJob#JOB_SUBTYPE_ALTERNATE_SPOOL_USER RJob.JOB_SUBTYPE_ALTERNATE_SPOOL_USER }  - Alternate spool user.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#JOB_SUBTYPE
**/
    public String getSubtype()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.JOB_SUBTYPE);
    }



/**
Returns the system.

@return The system.
**/
    public AS400 getSystem()
    {
        return rJob_.getSystem();
    }



/**
Returns the system portion of the library list of the initial thread.

@return The system portion of the library list of the initial thread.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#SYSTEM_LIBRARY_LIST
**/
    public String[] getSystemLibraryList()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsStringArray(RJob.SYSTEM_LIBRARY_LIST);
    }



/**
Returns the value used to separate hours, minutes, and seconds when presenting
a time.

@return The value used to separate hours, minutes, and seconds when presenting
        a time.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#TIME_SEPARATOR
**/
    public String getTimeSeparator()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.TIME_SEPARATOR);
    }



  //@A2A
/**
Returns the maximum amount of processor time (in milliseconds) given to
each thread in this job before other threads in this job and in other
jobs are given the opportunity to run.

@return The maximum amount of processor time (in milliseconds) given to
        each thread in this job before other threads in this job and in other
        jobs are given the opportunity to run.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#TIME_SLICE
**/
    public int getTimeSlice()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsInt(RJob.TIME_SLICE);
    }



  //@A2A
/**
Returns a value indicating whether a thread in an interactive
job moves to another main storage pool at the end of its time slice.

@return The value indicating whether a thread in an interactive
        job moves to another main storage pool at the end of its time slice.
        Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RJob#NONE RJob.NONE }  -
            A thread in the job does not move to another main storage pool when it reaches
            the end of its time slice.
        <li>{@link com.ibm.as400.resource.RJob#TIME_SLICE_END_POOL_BASE RJob.TIME_SLICE_END_POOL_BASE }  -
            A thread in the job moves to the base pool when it reaches
            the end of its time slice.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#TIME_SLICE_END_POOL
**/
    public String getTimeSliceEndPool()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.TIME_SLICE_END_POOL);
    }



/**
Returns the total amount of response time (in milliseconds) for the
initial thread.

@return The total amount of response time (in milliseconds) for the
        initial thread.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#TOTAL_RESPONSE_TIME
**/
    public int getTotalResponseTime()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsInt(RJob.TOTAL_RESPONSE_TIME);
    }



/**
Returns the job type.

@return The job type. Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RJob#JOB_TYPE_NOT_VALID RJob.JOB_TYPE_NOT_VALID }  - The job is not a valid job.
        <li>{@link com.ibm.as400.resource.RJob#JOB_TYPE_AUTOSTART RJob.JOB_TYPE_AUTOSTART }  - The job is an autostart job.
        <li>{@link com.ibm.as400.resource.RJob#JOB_TYPE_BATCH RJob.JOB_TYPE_BATCH }  - The job is a batch job.
        <li>{@link com.ibm.as400.resource.RJob#JOB_TYPE_INTERACTIVE RJob.JOB_TYPE_INTERACTIVE }  - The job is an interactive job.
        <li>{@link com.ibm.as400.resource.RJob#JOB_TYPE_SUBSYSTEM_MONITOR RJob.JOB_TYPE_SUBSYSTEM_MONITOR }  - The job is a subsystem monitor job.
        <li>{@link com.ibm.as400.resource.RJob#JOB_TYPE_SPOOLED_READER RJob.JOB_TYPE_SPOOLED_READER }  - The job is a spooled reader job.
        <li>{@link com.ibm.as400.resource.RJob#JOB_TYPE_SYSTEM RJob.JOB_TYPE_SYSTEM }  - The job is a system job.
        <li>{@link com.ibm.as400.resource.RJob#JOB_TYPE_SPOOLED_WRITER RJob.JOB_TYPE_SPOOLED_WRITER }  - The job is a spooled writer job.
        <li>{@link com.ibm.as400.resource.RJob#JOB_TYPE_SCPF_SYSTEM RJob.JOB_TYPE_SCPF_SYSTEM }  - The job is the SCPF system job.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#JOB_TYPE
**/
    public String getType()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsString(RJob.JOB_TYPE);
    }



/**
Returns the user name.

@return The user name.
**/
    public String getUser()
    {
        return rJob_.getUser();
    }



/**
Returns the user portion of the library list of
the initial thread.

@return The user portion of the library list of
        the initial thread.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#USER_LIBRARY_LIST
**/
    public String[] getUserLibraryList()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        return customGetAttributeValueAsStringArray(RJob.USER_LIBRARY_LIST);
    }



/**
Returns the unit of work identifier. The unit of work identifier is used to
track jobs across multiple systems. If a job is not associated with a source or
target system using advanced program-to-program communications (APPC),
this information is not used. Every job on the system is assigned a unit
of work identifier.

@return The unit of work identifier, which is made up of:
        <ul>
        <li>Location name - 8 Characters. The name of the source system that
                            originated the APPC job.
        <li>Network ID - 8 Characters. The network name associated with the
                         unit of work.
        <li>Instance - 6 Characters. The value that further identifies the
                        source of the job. This is shown as hexadecimal data.
        <li>Sequence Number - 2 Character. A value that identifies a check-point
                              within the application program.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#LOCATION_NAME
@see com.ibm.as400.resource.RJob#NETWORK_ID
@see com.ibm.as400.resource.RJob#INSTANCE
@see com.ibm.as400.resource.RJob#SEQUENCE_NUMBER
**/
    public String getWorkIDUnit()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        StringBuffer buffer = new StringBuffer("                        "); // 24 characters. @D1C
        buffer.insert(0, customGetAttributeValueAsString(RJob.LOCATION_NAME));
        buffer.insert(8, customGetAttributeValueAsString(RJob.NETWORK_ID));

        try {                                                                                // @D1A
            buffer.insert(16, new String((byte[])rJob_.getAttributeValue(RJob.INSTANCE)));   // @D1C
        }                                                                                    // @D1A
        catch(ResourceException e) {                                                         // @D1A
            e.unwrap();                                                                      // @D1A
        }                                                                                    // @D1A

        buffer.insert(22, customGetAttributeValueAsString(RJob.SEQUENCE_NUMBER));
        String workIDUnit = buffer.toString();                      // @D1A
        if (workIDUnit.length() > 24)                               // @D1A
            workIDUnit = workIDUnit.substring(0,24);                // @D1A
        return workIDUnit;                                          // @D1C
    }



/**
Refreshes the values for all attributes.  This does not cancel
uncommitted changes.
**/
    public void loadInformation()
    {
        try {
            rJob_.refreshAttributeValues();
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error loading information", e);
        }
    }



/**
Removes a PropertyChangeListener.

@param listener The listener.
**/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        rJob_.removePropertyChangeListener(listener);
    }



/**
Removes a VetoableChangeListener.

@param listener The listener.
**/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        rJob_.removeVetoableChangeListener(listener);
    }



/**
Sets how this job handles break messages.

@param breakMessageHandling How this job handles break messages.  Possible values are:
                            <ul>
                            <li>{@link com.ibm.as400.resource.RJob#BREAK_MESSAGE_HANDLING_NORMAL RJob.BREAK_MESSAGE_HANDLING_NORMAL }
                                - The message queue status determines break message handling.
                            <li>{@link com.ibm.as400.resource.RJob#BREAK_MESSAGE_HANDLING_HOLD RJob.BREAK_MESSAGE_HANDLING_HOLD }
                                - The message queue holds break messages until a user or program
                                  requests them.
                            <li>{@link com.ibm.as400.resource.RJob#BREAK_MESSAGE_HANDLING_NOTIFY RJob.BREAK_MESSAGE_HANDLING_NOTIFY }
                                - The system notifies the job's message queue when a message
                                  arrives.
                            </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#BREAK_MESSAGE_HANDLING
**/
    public void setBreakMessageHandling(String breakMessageHandling)
           throws  AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.BREAK_MESSAGE_HANDLING, breakMessageHandling);
    }



/**
Sets the value indicating whether attribute value changes are
committed immediately.   The default is true.

@param cacheChanges true to cache attribute value changes,
                    false to commit all attribute value changes
                    immediately.
**/
    public void setCacheChanges(boolean cacheChanges)
    {
        if (cacheChanges) {
            try {
                rJob_.commitAttributeChanges();
            }
            catch (ResourceException e) {
                if (Trace.isTraceOn())
                    Trace.log(Trace.ERROR, "Error loading information", e);
            }
        }
        cacheChanges_ = cacheChanges;
    }



/**
Sets the coded character set identifier (CCSID).

@param codedCharacterSetID  The coded character set identifier (CCSID).  The
                            following special values can be used:
                            <ul>
                            <li>{@link com.ibm.as400.resource.RJob#CCSID_SYSTEM_VALUE RJob.CCSID_SYSTEM_VALUE }  - The CCSID specified
                                in the system value QCCSID is used.
                            <li>{@link com.ibm.as400.resource.RJob#CCSID_INITIAL_USER RJob.CCSID_INITIAL_USER }  - The CCSID specified
                                in the user profile under which this thread was initially running is used.
                            </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#CCSID
**/
    public void setCodedCharacterSetID(int codedCharacterSetID)
           throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsInt(RJob.CCSID, codedCharacterSetID);
    }



/**
Sets the country ID.

@param countryID    The country ID.  The following special values can be used:
                    <ul>
                    <li>{@link com.ibm.as400.resource.RJob#SYSTEM_VALUE RJob.SYSTEM_VALUE }  - The
                        system value QCNTRYID is used.
                    <li>{@link com.ibm.as400.resource.RJob#USER_PROFILE RJob.USER_PROFILE }  - The
                        country ID specified in the user profile under which this thread
                        was initially running is used.
                    </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#COUNTRY_ID
**/
    public void setCountryID(String countryID)
           throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.COUNTRY_ID, countryID);
    }



/**
Sets the format in which dates are presented.

@param dateFormat The format in which dates are presented.  Possible values are:
<ul>
<li>{@link com.ibm.as400.resource.RJob#DATE_FORMAT_SYSTEM_VALUE RJob.DATE_FORMAT_SYSTEM_VALUE }  - The
    system value QDATFMT is used.
<li>{@link com.ibm.as400.resource.RJob#DATE_FORMAT_YMD RJob.DATE_FORMAT_YMD }  - Year, month, and day format.
<li>{@link com.ibm.as400.resource.RJob#DATE_FORMAT_MDY RJob.DATE_FORMAT_MDY }  - Month, day, and year format.
<li>{@link com.ibm.as400.resource.RJob#DATE_FORMAT_DMY RJob.DATE_FORMAT_DMY }  - Day, month, and year format.
<li>{@link com.ibm.as400.resource.RJob#DATE_FORMAT_JULIAN RJob.DATE_FORMAT_JULIAN }  - Julian format (year and day).
</ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#DATE_FORMAT
**/
    public void setDateFormat(String dateFormat)
           throws AS400Exception,
                  AS400SecurityException,
                  ConnectionDroppedException,
                  ErrorCompletingRequestException,
                  InterruptedException,
                  IOException,
                  ObjectDoesNotExistException,
                  UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.DATE_FORMAT, dateFormat);
    }



/**
Sets the value used to separate days, months, and years when presenting
a date.

@param dateSeparator    The value used to separate days, months, and years
                        when presenting a date.  The following special value
                        can be used:
                        <ul>
                        <li>{@link com.ibm.as400.resource.RJob#DATE_SEPARATOR_SYSTEM_VALUE RJob.DATE_SEPARATOR_SYSTEM_VALUE }  - The
                            system value QDATSEP is used.
                        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#DATE_SEPARATOR
**/
    public void setDateSeparator(String dateSeparator)
           throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.DATE_SEPARATOR, dateSeparator);
    }



/**
Sets whether connections using distributed data management (DDM)
protocols remain active when they are not being used.

@param ddmConversationHandling  Whether connections using distributed data
                                management (DDM) protocols remain active
                                when they are not being used.  Possible values are:
                                <ul>
                                <li>{@link com.ibm.as400.resource.RJob#KEEP_DDM_CONNECTIONS_ACTIVE_KEEP RJob.KEEP_DDM_CONNECTIONS_ACTIVE_KEEP }  - The system keeps DDM connections active when there
                                    are no users.
                                <li>{@link com.ibm.as400.resource.RJob#KEEP_DDM_CONNECTIONS_ACTIVE_DROP RJob.KEEP_DDM_CONNECTIONS_ACTIVE_DROP }  - The system ends a DDM connection when there are no
                                    users.
                                </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#KEEP_DDM_CONNECTIONS_ACTIVE
**/
    public void setDDMConversationHandling(String ddmConversationHandling)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.KEEP_DDM_CONNECTIONS_ACTIVE, ddmConversationHandling);
    }



/**
Sets the decimal format used for this job.

@param decimalFormat    The decimal format used for this job.  Possible values are:
                        <ul>
                        <li>{@link com.ibm.as400.resource.RJob#DECIMAL_FORMAT_PERIOD RJob.DECIMAL_FORMAT_PERIOD }  - Uses a period for a decimal point, a comma
                            for a 3-digit grouping character, and zero-suppresses to the left of
                            the decimal point.
                        <li>{@link com.ibm.as400.resource.RJob#DECIMAL_FORMAT_COMMA_I RJob.DECIMAL_FORMAT_COMMA_I }  - Uses a comma for a decimal point and a period for
                            a 3-digit grouping character.  The zero-suppression character is in the
                            second character (rather than the first) to the left of the decimal
                            notation.  Balances with zero  values to the left of the comma are
                            written with one leading zero.
                        <li>{@link com.ibm.as400.resource.RJob#DECIMAL_FORMAT_COMMA_J RJob.DECIMAL_FORMAT_COMMA_J }  - Uses a comma for a decimal point, a period for a
                            3-digit grouping character, and zero-suppresses to the left of the decimal
                            point.
                        <li>{@link com.ibm.as400.resource.RJob#SYSTEM_VALUE RJob.SYSTEM_VALUE }  - The
                            system value QECFMT is used.
                        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#DECIMAL_FORMAT
**/
    public void setDecimalFormat(String decimalFormat)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.DECIMAL_FORMAT, decimalFormat);
    }



/**
Sets the default maximum time (in seconds) that a thread in the job
waits for a system instruction.

@param defaultWait  The default maximum time (in seconds) that a thread in the job
                    waits for a system instruction.  The value -1 means there is no maximum.
                    The value 0 is not valid.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#DEFAULT_WAIT_TIME
**/
    public void setDefaultWait(int defaultWait)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsInt(RJob.DEFAULT_WAIT_TIME, defaultWait);
    }



/**
Sets the action taken for interactive jobs when an I/O error occurs
for the job's requesting program device.

@param deviceRecoveryAction The action taken for interactive jobs when an I/O error occurs
                            for the job's requesting program device.  Possible values are:
                            <li>{@link com.ibm.as400.resource.RJob#DEVICE_RECOVERY_ACTION_MESSAGE RJob.DEVICE_RECOVERY_ACTION_MESSAGE }  - Signals the I/O error message to the
                                application and lets the application program perform error recovery.
                            <li>{@link com.ibm.as400.resource.RJob#DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE RJob.DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE }  - Disconnects the
                                job when an I/O error occurs.  When the job reconnects, the system sends an
                                error message to the application program, indicating the job has reconnected
                                and that the workstation device has recovered.
                            <li>{@link com.ibm.as400.resource.RJob#DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST RJob.DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST }  - Disconnects
                                the job when an I/O error occurs.  When the job reconnects, the system sends
                                the End Request (ENDRQS) command to return control to the previous request
                                level.
                            <li>{@link com.ibm.as400.resource.RJob#DEVICE_RECOVERY_ACTION_END_JOB RJob.DEVICE_RECOVERY_ACTION_END_JOB }  - Ends the job when an I/O error occurs.  A
                                message is sent to the job's log and to the history log (QHST) indicating
                                the job ended because of a device error.
                            <li>{@link com.ibm.as400.resource.RJob#DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST RJob.DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST }  - Ends the job when an I/O
                                error occurs.  There is no job log produced for the job.  The system sends
                                a message to the QHST log indicating the job ended because of a device error.
                            <li>{@link com.ibm.as400.resource.RJob#SYSTEM_VALUE RJob.SYSTEM_VALUE }  - The
                                system value QDEVRCYACN is used.
                            </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#DEVICE_RECOVERY_ACTION
**/
    public void setDeviceRecoveryAction(String deviceRecoveryAction)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.DEVICE_RECOVERY_ACTION, deviceRecoveryAction);
    }



/**
Sets how the job answers inquiry messages.

@param inquiryMessageReply  How the job answers inquiry messages.  Possible values are:
                            <ul>
                            <li>{@link com.ibm.as400.resource.RJob#INQUIRY_MESSAGE_REPLY_REQUIRED RJob.INQUIRY_MESSAGE_REPLY_REQUIRED }  - The job requires an answer for any inquiry
                                messages that occur while this job is running.
                            <li>{@link com.ibm.as400.resource.RJob#INQUIRY_MESSAGE_REPLY_DEFAULT RJob.INQUIRY_MESSAGE_REPLY_DEFAULT }  - The system uses the default message reply to
                                answer any inquiry messages issued while this job is running.  The default
                                reply is either defined in the message description or is the default system
                                reply.
                            <li>{@link com.ibm.as400.resource.RJob#INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST RJob.INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST } The system reply list is
                                checked to see if there is an entry for an inquiry message issued while this
                                job is running.  If a match occurs, the system uses the reply value for that
                                entry.  If no entry exists for that message, the system uses an inquiry message.
                            </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#INQUIRY_MESSAGE_REPLY
**/
    public void setInquiryMessageReply(String inquiryMessageReply)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.INQUIRY_MESSAGE_REPLY, inquiryMessageReply);
    }




/**
Sets the internal job identifier.  This does not change
the job on the AS/400.  Instead, it changes the job
this Job object references.  The job name
must be set to "*INT" for this to be recognized.
This cannot be changed if the object has established
a connection to the AS/400.

@param internalJobID    The internal job identifier.

@exception PropertyVetoException    If the property change is vetoed.
**/
    public void setInternalJobID(String internalJobID)
        throws PropertyVetoException
    {
        try {
            rJob_.setInternalJobID(CharConverter.stringToByteArray(INTERNAL_JOB_ID_CCSID_, getSystem(), internalJobID));
        }
        catch(UnsupportedEncodingException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Unable to convert internal job id.", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }
    }



/**
Sets the identifier assigned to the job by the system to collect resource
use information for the job when job accounting is active.

@param jobAccountingCode    The identifier assigned to the job by the
                            system to collect resource use information
                            for the job when job accounting is active.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#ACCOUNTING_CODE
**/
    public void setJobAccountingCode(String jobAccountingCode)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.ACCOUNTING_CODE, jobAccountingCode);
    }



/**
Sets the date to be used for the job.

@param jobDate The date to be used for the job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#JOB_DATE
**/
    public void setJobDate(Date jobDate)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsDate(RJob.JOB_DATE, jobDate);
    }


/**
Sets the action to take when the message queue is full.

@param jobMessageQueueFullAction    The action to take when the message queue is full.  Possible values are:
                                    <ul>
                                    <li>{@link com.ibm.as400.resource.RJob#MESSAGE_QUEUE_ACTION_NO_WRAP RJob.MESSAGE_QUEUE_ACTION_NO_WRAP }  - Do not wrap. This action causes the job to end.
                                    <li>{@link com.ibm.as400.resource.RJob#MESSAGE_QUEUE_ACTION_WRAP RJob.MESSAGE_QUEUE_ACTION_WRAP }  - Wrap to the beginning and start filling again.
                                    <li>{@link com.ibm.as400.resource.RJob#MESSAGE_QUEUE_ACTION_PRINT_WRAP RJob.MESSAGE_QUEUE_ACTION_PRINT_WRAP }  - Wrap the message queue and print the
                                        messages that are being overlaid because of the wrapping.
                                    </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#MESSAGE_QUEUE_ACTION
**/
    public void setJobMessageQueueFullAction(String jobMessageQueueFullAction)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.MESSAGE_QUEUE_ACTION, jobMessageQueueFullAction);
    }



/**
Sets the current setting of the job switches used by this job.

@param jobSwitches The current setting of the job switches used by this job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#JOB_SWITCHES
**/
    public void setJobSwitches(String jobSwitches)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.JOB_SWITCHES, jobSwitches);
    }



/**
Sets the language identifier associated with this job.

@param languageID       The language identifier associated with this job.
                        The following special values can be used:
                        <ul>
                        <li>{@link com.ibm.as400.resource.RJob#SYSTEM_VALUE RJob.SYSTEM_VALUE }  - The
                            system value QLANGID is used.
                        <li>{@link com.ibm.as400.resource.RJob#USER_PROFILE RJob.USER_PROFILE }  - The
                            language identifier specified in the user profile in which this thread
                            was initially running is used.
                        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#LANGUAGE_ID
**/
    public void setLanguageID(String languageID)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.LANGUAGE_ID, languageID);
    }



/**
Sets whether messages are logged for CL programs.

@param loggingCLPrograms    The value indicating whether or not messages are logged for
                            CL programs. Possible values are: {@link com.ibm.as400.resource.RJob#YES RJob.YES }  and
                            {@link com.ibm.as400.resource.RJob#NO RJob.NO } .

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#LOG_CL_PROGRAMS
**/
    public void setLoggingCLPrograms(String loggingCLPrograms)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        if (loggingCLPrograms == null)
            throw new NullPointerException("loggingCLPrograms");
        if (loggingCLPrograms.equals(RJob.YES))
            customSetAttributeValueAsBoolean(RJob.LOG_CL_PROGRAMS, true);
        else if (loggingCLPrograms.equals(RJob.NO))
            customSetAttributeValueAsBoolean(RJob.LOG_CL_PROGRAMS, false);
        else
            throw new ExtendedIllegalArgumentException("loggingCLPrograms", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }



// @D1C
/**
Sets the type of information that is logged.

@param loggingLevel The type of information that is logged.  Possible values are:
                    <ul>
                    <li>0 - No messages are logged.
                    <li>1 - All messages sent
                        to the job's external message queue with a severity greater than or equal to
                        the message logging severity are logged.
                    <li>2 -
                        Requests or commands from CL programs for which the system issues messages with
                        a severity code greater than or equal to the logging severity and all messages
                        associated with those requests or commands that have a severity code greater
                        than or equal to the logging severity are logged.
                    <li>3 - All requests or commands from CL programs and all messages
                        associated with those requests or commands that have a severity code greater
                        than or equal to the logging severity are logged.
                    <li>4 - All requests or commands from CL programs and all messages
                        with a severity code greater than or equal to the logging severity are logged.
                    </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#LOGGING_LEVEL
**/
    public void setLoggingLevel(int loggingLevel)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.LOGGING_LEVEL, Integer.toString(loggingLevel)); // @D1C
    }



/**
Sets the minimum severity level that causes error messages to be logged
in the job log.

@param loggingSeverity  The minimum severity level that causes error messages to be logged
                        in the job log.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#LOGGING_SEVERITY
**/
    public void setLoggingSeverity(int loggingSeverity)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsInt(RJob.LOGGING_SEVERITY, loggingSeverity);
    }



/**
Sets the level of message text that is written in the job log
or displayed to the user.

@param loggingText  The level of message text that is written in the job log
                    or displayed to the user.  Possible values are:
                    <ul>
                    <li>{@link com.ibm.as400.resource.RJob#LOGGING_TEXT_MESSAGE RJob.LOGGING_TEXT_MESSAGE }  - Only the message is written to the job log.
                    <li>{@link com.ibm.as400.resource.RJob#LOGGING_TEXT_SECLVL RJob.LOGGING_TEXT_SECLVL }  - Both the message and the message help for the
                        error message are written to the job log.
                    <li>{@link com.ibm.as400.resource.RJob#LOGGING_TEXT_NO_LIST RJob.LOGGING_TEXT_NO_LIST }  - If the job ends normally, there is no job log.
                        If the job ends abnormally, there is a job log.  The messages appearing in the
                        job log contain both the message and the message help.
                    </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#LOGGING_TEXT
**/
    public void setLoggingText(String loggingText)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.LOGGING_TEXT, loggingText);
    }



/**
Sets the job name.  This does not change the job on
the AS/400.  Instead, it changes the job
this Job object references.   This cannot be changed
if the object has established a connection to the AS/400.

@param name    The job name.  Specify "*" to indicate the job this
               program running in, or "*INT" to indicate that the job
               is specified using the internal job identifier.

@exception PropertyVetoException    If the property change is vetoed.
**/
    public void setName(String name)
        throws PropertyVetoException
    {
        rJob_.setName(name);
    }



/**
Sets the job number.  This does not change the job on
the AS/400.  Instead, it changes the job
this Job object references.  This cannot be changed
if the object has established a connection to the AS/400.

@param number    The job number.  This must be blank if the job name is "*".

@exception PropertyVetoException    If the property change is vetoed.
**/
    public void setNumber(String number)
        throws PropertyVetoException
    {
        rJob_.setNumber(number);
    }



/**
Sets the name of the default output queue that is used for
spooled output produced by this job.

@param outputQueue  The fully qualified integrated integrated file system path name of
                    the default output queue that is used for
                    spooled output produced by this job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#OUTPUT_QUEUE
@see QSYSObjectPathName
**/
    public void setOutputQueue(String outputQueue)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.OUTPUT_QUEUE, outputQueue);
    }



/**
Sets the output priority for spooled output files that this job
produces.

@param outputQueuePriority  The output priority for spooled output files that this job
                            produces.   The highest priority is 0 and the lowest is 9.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#OUTPUT_QUEUE_PRIORITY
**/
    public void setOutputQueuePriority(int outputQueuePriority)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsInt(RJob.OUTPUT_QUEUE_PRIORITY, outputQueuePriority);
    }



/**
Sets the printer device name used for printing output
from this job.

@param printerDeviceName    The printer device name used for printing output
                            from this job.  The following special values can be used:
                            <ul>
                            <li>{@link com.ibm.as400.resource.RJob#SYSTEM_VALUE RJob.SYSTEM_VALUE }  - The
                                system value QPRTDEV is used.
                            <li>{@link com.ibm.as400.resource.RJob#PRINTER_DEVICE_NAME_WORK_STATION RJob.PRINTER_DEVICE_NAME_WORK_STATION }  - The
                                default printer device used with this job is the printer device
                                assigned to the work station that is associated with the job.
                            <li>{@link com.ibm.as400.resource.RJob#USER_PROFILE RJob.USER_PROFILE }  - The
                                printer device name specified in the user profile in which this thread
                                was initially running is used.
                            </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#PRINTER_DEVICE_NAME
**/
    public void setPrinterDeviceName (String printerDeviceName)
         throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.PRINTER_DEVICE_NAME, printerDeviceName);
    }



/**
Sets whether border and header information is provided when
the Print key is pressed.

@param printKeyFormat   Whether border and header information is provided when
                        the Print key is pressed.  Possible values are:
                        <ul>
                        <li>{@link com.ibm.as400.resource.RJob#NONE RJob.NONE }  - The border and header information is not
                            included with output from the Print key.
                        <li>{@link com.ibm.as400.resource.RJob#PRINT_KEY_FORMAT_BORDER RJob.PRINT_KEY_FORMAT_BORDER }  - The border information
                            is included with output from the Print key.
                        <li>{@link com.ibm.as400.resource.RJob#PRINT_KEY_FORMAT_HEADER RJob.PRINT_KEY_FORMAT_HEADER }  - The header information
                            is included with output from the Print key.
                        <li>{@link com.ibm.as400.resource.RJob#PRINT_KEY_FORMAT_ALL RJob.PRINT_KEY_FORMAT_ALL }  - The border and header information
                            is included with output from the Print key.
                        <li>{@link com.ibm.as400.resource.RJob#SYSTEM_VALUE RJob.SYSTEM_VALUE }  - The
                            system value QPRTKEYFMT is used.
                        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#PRINT_KEY_FORMAT
**/
    public void setPrintKeyFormat(String printKeyFormat)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.PRINT_KEY_FORMAT, printKeyFormat);
    }



/**
Sets the line of text, if any, that is printed at the
bottom of each page of printed output for the job.

@param printText    The line of text, if any, that is printed at the
                    bottom of each page of printed output for the job.
                    The following special value can be used:
                    <ul>
                    <li>{@link com.ibm.as400.resource.RJob#SYSTEM_VALUE RJob.SYSTEM_VALUE }  - The
                        system value QPRTTXT is used.
                    </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#PRINT_TEXT
**/
    public void setPrintText (String printText)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.PRINT_TEXT, printText);
    }



/**
Sets the value indicating whether the job is eligible to be moved out of main storage
and put into auxiliary storage at the end of a time slice or when it is
beginning a long wait.

@param purge    true to indicate that the job is eligible to be moved out of main storage
                and put into auxiliary storage at the end of a time slice or when it is
                beginning a long wait, false otherwise.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#ELIGIBLE_FOR_PURGE
**/
    public void setPurge(boolean purge)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsBoolean(RJob.ELIGIBLE_FOR_PURGE, purge);
    }



/**
Sets the job queue that the job is currently on.

@param jobQueue     The fully qualified integrate file system path name
                    of the job queue.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#JOB_QUEUE
@see QSYSObjectPathName
**/
    public void setQueue(String jobQueue)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.JOB_QUEUE, jobQueue);
    }



/**
Sets the scheduling priority of the job compared to other jobs
on the same job queue.

@param queuePriority    The scheduling priority of the job compared to other jobs
                        on the same job queue.  The highest priority is 0 and the
                        lowest is 9.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#JOB_QUEUE_PRIORITY
**/
    public void setQueuePriority(int queuePriority)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsInt(RJob.JOB_QUEUE_PRIORITY, queuePriority);
    }



/**
Sets the priority at which the job is currently running,
relative to other jobs on the system.

@param runPriority  The priority at which the job is currently running,
                    relative to other jobs on the system.  The run priority
                    ranges from 1 (highest priority) to 99 (lowest priority).

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#RUN_PRIORITY
**/
    public void setRunPriority(int runPriority)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsInt(RJob.RUN_PRIORITY, runPriority);
    }


/**
Sets the date and time the job is scheduled to become active.

@param scheduleDate The date and time the job is scheduled to become active.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#SCHEDULE_DATE
**/
    public void setScheduleDate(Date scheduleDate)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsDate(RJob.SCHEDULE_DATE, scheduleDate);
    }



/**
Sets the date the job is scheduled to become active.

@param scheduleDate     The date the job is scheduled to become active,
                        in the format <em>CYYMMDD</em>, where <em>C</em>
                        is the century, <em>YY</em> is the year, <em>MM</em>
                        is the month, and <em>DD</em> is the day.  A 0 for
                        the century flag indicates years 19<em>xx</em> and a
                        1 indicates years 20<em>xx</em>.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#SCHEDULE_DATE
**/
    public void setScheduleDate(String scheduleDate)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        if (scheduleDate == null)
            throw new NullPointerException("scheduleDate");
        if (scheduleDate.length() != 7)
            throw new ExtendedIllegalArgumentException("scheduleDate", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        Calendar calendar = Calendar.getInstance();

        int century = Integer.parseInt(scheduleDate.substring(0,1));
        int year    = Integer.parseInt(scheduleDate.substring(1,3));
        int month   = Integer.parseInt(scheduleDate.substring(3,5));
        int day     = Integer.parseInt(scheduleDate.substring(5,7));

        calendar.set(Calendar.YEAR, year + ((century == 0) ? 1900 : 2000));
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        customSetAttributeValueAsDate(RJob.SCHEDULE_DATE, calendar.getTime());
    }



/**
Sets the date and time the job is scheduled to become active.

@param scheduleDate The date and time the job is scheduled to become active.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#SCHEDULE_DATE
**/
    public void setScheduleTime(Date scheduleTime)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        setScheduleDate(scheduleTime);
    }



/**
Sets the time the job is scheduled to become active.

@param scheduleTime     The time the job is scheduled to become active,
                        in the format <em>HHMMSS</em>, where <em>HH</em> are
                        the hours, <em>MM</em> are the minutes, and <em>SS</em>
                        are the seconds.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#SCHEDULE_DATE
**/
    public void setScheduleTime(String scheduleTime)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        if (scheduleTime == null)
            throw new NullPointerException("scheduleTime");
        if (scheduleTime.length() != 6)
            throw new ExtendedIllegalArgumentException("scheduleTime", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        Calendar calendar = Calendar.getInstance();

        int hours   = Integer.parseInt(scheduleTime.substring(0,2));
        int minutes = Integer.parseInt(scheduleTime.substring(2,4));
        int seconds = Integer.parseInt(scheduleTime.substring(4,6));

        calendar.set(Calendar.YEAR, 0);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, seconds);

        customSetAttributeValueAsDate(RJob.SCHEDULE_DATE, calendar.getTime());
    }



/**
Sets the sort sequence table.

@param sortSequenceTable    The fully qualified integrated file system path name
                            of the sort sequence table.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#SORT_SEQUENCE_TABLE
@see QSYSObjectPathName
**/
    public void setSortSequenceTable(String sortSequenceTable)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.SORT_SEQUENCE_TABLE, sortSequenceTable);
    }



/**
Sets the value which indicates whether status messages are displayed for
this job.

@param statusMessageHandling    The value which indicates whether status messages are displayed for
                                this job. Possible values are:
                                <ul>
                                <li>{@link com.ibm.as400.resource.RJob#NONE RJob.NONE }  -
                                    This job does not display status messages.
                                <li>{@link com.ibm.as400.resource.RJob#STATUS_MESSAGE_HANDLING_NORMAL RJob.STATUS_MESSAGE_HANDLING_NORMAL }  -
                                    This job displays status messages.
                                <li>{@link com.ibm.as400.resource.RJob#SYSTEM_VALUE RJob.SYSTEM_VALUE }  - The
                                    system value QSTSMSG is used.
                                <li>{@link com.ibm.as400.resource.RJob#USER_PROFILE RJob.USER_PROFILE }  - The
                                    status message handling that is specified in the user profile under which this thread
                                    was initially running is used.
                                </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#STATUS_MESSAGE_HANDLING
**/
    public void setStatusMessageHandling(String statusMessageHandling)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.STATUS_MESSAGE_HANDLING, statusMessageHandling);
    }



/**
Sets the system.  This cannot be changed if the object
has established a connection to the AS/400.

@param system The system.

@exception PropertyVetoException    If the property change is vetoed.
**/
    public void setSystem(AS400 system)
    throws PropertyVetoException
    {
        rJob_.setSystem(system);
    }



/**
Sets the value used to separate hours, minutes, and seconds when presenting
a time.

@param timeSeparator    The value used to separate hours, minutes, and seconds
                        when presenting a time.  The following special value
                        can be used:
                        <ul>
                        <li>{@link com.ibm.as400.resource.RJob#TIME_SEPARATOR_SYSTEM_VALUE RJob.TIME_SEPARATOR_SYSTEM_VALUE }  - The
                            system value QTIMSEP is used.
                        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#TIME_SEPARATOR
**/
    public void setTimeSeparator(String timeSeparator)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.TIME_SEPARATOR, timeSeparator);
    }



/**
Sets the maximum amount of processor time (in milliseconds) given to
each thread in this job before other threads in this job and in other
jobs are given the opportunity to run.

@param timeSlice    The maximum amount of processor time (in milliseconds) given to
                    each thread in this job before other threads in this job and in other
                    jobs are given the opportunity to run.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#TIME_SLICE
**/
    public void setTimeSlice(int timeSlice)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsInt(RJob.TIME_SLICE, timeSlice);
    }



/**
Sets the value which indicates whether a thread in an interactive job moves to another main storage
pool at the end of its time slice.

@param timeSliceEndPool     The value which indicates whether a thread in an interactive job
                            moves to another main storage pool at the end of its time slice.
                            Possible values are:
                            <ul>
                            <li>{@link com.ibm.as400.resource.RJob#NONE RJob.NONE }  -
                                A thread in the job does not move to another main storage pool when it reaches
                                the end of its time slice.
                            <li>{@link com.ibm.as400.resource.RJob#TIME_SLICE_END_POOL_BASE RJob.TIME_SLICE_END_POOL_BASE }  -
                                A thread in the job moves to the base pool when it reaches
                                the end of its time slice.
                            </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see com.ibm.as400.resource.RJob#TIME_SLICE_END_POOL
**/
    public void setTimeSliceEndPool(String timeSliceEndPool)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        customSetAttributeValueAsString(RJob.TIME_SLICE_END_POOL, timeSliceEndPool);
    }



/**
Sets the user name.  This does not change the job on
the AS/400.  Instead, it changes the job
this Job object references.  This cannot be changed
if the object has established a connection to the AS/400.

@param user    The user name.  This must be blank if the job name is "*".

@exception PropertyVetoException    If the property change is vetoed.
**/
    public void setUser(String user)
        throws PropertyVetoException
    {
        rJob_.setUser(user);
    }



/**
Returns the string representation in the format
"number/user/name".

@return The string representation.
**/
    public String toString()
    {
        return rJob_.toString();
    }




}
