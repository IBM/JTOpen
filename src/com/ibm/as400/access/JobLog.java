///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JobLog.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import com.ibm.as400.resource.ResourceException;
import com.ibm.as400.resource.RJobLog;
import com.ibm.as400.resource.RQueuedMessage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Enumeration;



/**
The JobLog class represents an AS/400 job log.  This is used
to get a list of messages in a job log or to write messages to a job log.

<p>The complete set of selections for a job log can be accessed using the
{@link com.ibm.as400.resource.RJobLog  RJobLog } class.

<p>QueuedMessage objects have many attributes.  Only some of theses
attribute values are set, depending on how a QueuedMessage object is
created.  The following is a list of attributes whose values are set
on QueuedMessage objects returned in a list of job log messages:
<ul>
<li>date sent
<li>default reply
<li>message file
<li>message help
<li>message ID
<li>message key
<li>message severity
<li>message text
<li>message type
<li>reply status
<li>sender job name
<li>sender job number
<li>sender user name
<li>sending program name
</ul>

@see com.ibm.as400.resource.RJobLog
**/
public class JobLog
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;

//-----------------------------------------------------------------------------------------
// Private data.
//-----------------------------------------------------------------------------------------

    private RJobLog rJobLog_;

    private transient   PropertyChangeSupport   propertyChangeSupport_;
    private transient   VetoableChangeSupport   vetoableChangeSupport_;



//-----------------------------------------------------------------------------------------
// Code.
//-----------------------------------------------------------------------------------------

/**
Constructs a JobLog object.
**/
    public JobLog()
    {
        rJobLog_ = new RJobLog();
        initializeTransient();
    }



/**
Constructs a JobLog object.

@param system The system.
**/
    public JobLog(AS400 system)
    {
        rJobLog_ = new RJobLog(system);
        initializeTransient();
    }



/**
Constructs a JobLog object.

@param system       The system.
@param name         The job name.
@param user         The user name.
@param number       The job number.
**/
    public JobLog(AS400 system,
                  String name,
                  String user,
                  String number)
    {
        rJobLog_ = new RJobLog(system, name, user, number);
        initializeTransient();
    }



/**
Adds a PropertyChangeListener.  The specified PropertyChangeListener's
<b>propertyChange()</b> method will be called each time the value of
any bound property is changed.

@param listener The listener.
*/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport_.addPropertyChangeListener(listener);
        rJobLog_.addPropertyChangeListener(listener);
    }



/**
Adds a VetoableChangeListener.  The specified VetoableChangeListener's
<b>vetoableChange()</b> method will be called each time the value of
any constrained property is changed.

@param listener The listener.
*/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        vetoableChangeSupport_.addVetoableChangeListener(listener);
        rJobLog_.addVetoableChangeListener(listener);
    }



/**
Returns the number of messages in the job log.

@return The number of messages, or 0 if no list has been retrieved.
**/
    public int getLength()
    {
        try {
            rJobLog_.waitForComplete();
            return (int)rJobLog_.getListLength();
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "An error occurred while getting the job log length", e);
            return 0;
        }
    }



/**
Returns the list of messages in the job log.  The messages are listed from
oldest to newest.

@return An Enumeration of <a href="QueuedMessage.html">QueuedMessage</a>
        objects.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception ServerStartupException          If the AS/400 server cannot be started.
@exception UnknownHostException            If the AS/400 system cannot be located.
**/
    public Enumeration getMessages()
        throws  AS400Exception,
                AS400SecurityException,
                ErrorCompletingRequestException,
                InterruptedException,
                IOException,
                ObjectDoesNotExistException
    {
        try {
            rJobLog_.refreshContents(); // @E1A
            return new EnumerationAdapter(rJobLog_.resources());
        }
        catch(ResourceException e) {
            e.unwrap();
            return null;
        }
    }



/**
Returns the job name.

@return The job name, or "" if none has been set.
**/
    public String getName()
    {
        return rJobLog_.getName();
    }



/**
Returns the job number.

@return The job number, or "" if none has been set.
**/
    public String getNumber()
    {
        return rJobLog_.getNumber();
    }



/**
Returns the system.

@return The system.
**/
    public AS400 getSystem()
    {
        return rJobLog_.getSystem();
    }



/**
Returns the user name.

@return The user name, or "" if none has been set.
**/
    public String getUser()
    {
        return rJobLog_.getUser();
    }


/**
Initializes the transient data.
**/
    private void initializeTransient()
    {
        propertyChangeSupport_      = new PropertyChangeSupport(this);
        vetoableChangeSupport_      = new VetoableChangeSupport(this);
    }



/**
Deserializes the resource.
**/
    private void readObject(ObjectInputStream in)
    throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        initializeTransient ();
    }




/**
Removes a PropertyChangeListener.

@param listener The listener.
**/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport_.removePropertyChangeListener(listener);
        rJobLog_.removePropertyChangeListener(listener);
    }



/**
Removes a VetoableChangeListener.

@param listener The listener.
**/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        vetoableChangeSupport_.removeVetoableChangeListener(listener);
        rJobLog_.removeVetoableChangeListener(listener);
    }



/**
Sets the job name.  This cannot be changed
if the object has established a connection to the AS/400.

@param name The job name.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setName(String name)
        throws PropertyVetoException
    {
        rJobLog_.setName(name);
    }



/**
Sets the job number. This cannot be changed
if the object has established a connection to the AS/400.

@param number The job number.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setNumber(String number)
        throws PropertyVetoException
    {
        rJobLog_.setNumber(number);
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
        rJobLog_.setSystem(system);
    }



/**
Sets the user name.  This cannot be changed
if the object has established a connection to the AS/400.

@param user The user name.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setUser(String user)
        throws PropertyVetoException
    {
        rJobLog_.setUser(user);
    }



/**
Writes a program message to the job log for the job in which the program is running.
<br>Note: The program runs in the job of the Remote Command Host Server (QZRCSRVS) unless it is invoked "on-thread" on the iSeries server.
@param system       The system.  If the system specifies localhost, the message is written
                    to the job log of the process from which this method is called.
                    Otherwise the message is written to the QZRCSRVS job.
@param messageID    The message ID.  The message must be in the default message file
                    /QSYS.LIB/QCPFMSG.MSGF.
@param messageType  The message type. Possible values are:
                    <ul>
                    <li>AS400Message.COMPLETION
                    <li>AS400Message.DIAGNOSTIC
                    <li>AS400Message.INFORMATIONAL
                    <li>AS400Message.ESCAPE
                    </ul>
                    The message type must be AS400Message.INFORMATIONAL for an immediate
                    message.
@see com.ibm.as400.access.ProgramCall#isStayOnThread()

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception ServerStartupException          If the AS/400 server cannot be started.
@exception UnknownHostException            If the AS/400 system cannot be located.
**/
    public static void writeMessage(AS400 system,
                                    String messageID,
                                    int messageType)
        throws  AS400SecurityException,
                ErrorCompletingRequestException,
                InterruptedException,
                IOException,
                ObjectDoesNotExistException,
                AS400Exception
    {
        try {
            RJobLog.writeMessage(system, messageID, messageType);
        }
        catch(ResourceException e) {
            e.unwrap();
        }
    }



/**
Writes a program message to the job log for the job in which the program is running.
<br>Note: The program runs in the job of the Remote Command Host Server (QZRCSRVS) unless it is invoked "on-thread" on the iSeries server.

@param system           The system.  If the system specifies localhost, the message is written
                        to the job log of the process from which this method is called.
                        Otherwise the message is written to the QZRCSRVS job.
@param messageID        The message ID.  The message must be in the default message file
                        /QSYS.LIB/QCPFMSG.MSGF.
@param messageType      The message type. Possible values are:
                        <ul>
                        <li>AS400Message.COMPLETION
                        <li>AS400Message.DIAGNOSTIC
                        <li>AS400Message.INFORMATIONAL
                        <li>AS400Message.ESCAPE
                        </ul>
                        The message type must be AS400Message.INFORMATIONAL for an immediate
                        message.
@param substitutionData The substitution data.  The substitution data can be from 0-32767 bytes
                        for a conventional message and from 1-6000 bytes for an immediate message.
@see com.ibm.as400.access.ProgramCall#isStayOnThread()

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception ServerStartupException          If the AS/400 server cannot be started.
@exception UnknownHostException            If the AS/400 system cannot be located.
**/
    public static void writeMessage(AS400 system,
                                    String messageID,
                                    int messageType,
                                    byte[] substitutionData)
        throws  AS400SecurityException,
                ErrorCompletingRequestException,
                InterruptedException,
                IOException,
                ObjectDoesNotExistException,
                AS400Exception
    {
        try {
            RJobLog.writeMessage(system, messageID, messageType, substitutionData);
        }
        catch(ResourceException e) {
            e.unwrap();
        }
    }



/**
Writes a program message to the job log for the job in which the program is running.
<br>Note: The program runs in the job of the Remote Command Host Server (QZRCSRVS) unless it is invoked "on-thread" on the iSeries server.

@param system           The system.  If the system specifies localhost, the message is written
                        to the job log of the process from which this method is called.
                        Otherwise the message is written to the QZRCSRVS job.
@param messageID        The message ID.
@param messageType      The message type. Possible values are:
                        <ul>
                        <li>AS400Message.COMPLETION
                        <li>AS400Message.DIAGNOSTIC
                        <li>AS400Message.INFORMATIONAL
                        <li>AS400Message.ESCAPE
                        </ul>
                        The message type must be AS400Message.INFORMATIONAL for an immediate
                        message.
@param messageFile      The integrated file system path name of the message file.
@see com.ibm.as400.access.ProgramCall#isStayOnThread()

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception ServerStartupException          If the AS/400 server cannot be started.
@exception UnknownHostException            If the AS/400 system cannot be located.
**/
    public static void writeMessage(AS400 system,
                                    String messageID,
                                    int messageType,
                                    String messageFile)
        throws  AS400SecurityException,
                ErrorCompletingRequestException,
                InterruptedException,
                IOException,
                ObjectDoesNotExistException,
                AS400Exception
    {
        try {
            RJobLog.writeMessage(system, messageID, messageType, messageFile);
        }
        catch(ResourceException e) {
            e.unwrap();
        }
    }



/**
Writes a program message to the job log for the job in which the program is running.
<br>Note: The program runs in the job of the Remote Command Host Server (QZRCSRVS) unless it is invoked "on-thread" on the iSeries server.

@param system           The system.  If the system specifies localhost, the message is written
                        to the job log of the process from which this method is called.
                        Otherwise the message is written to the QZRCSRVS job.
@param messageID        The message ID.
@param messageType      The message type. Possible values are:
                        <ul>
                        <li>AS400Message.COMPLETION
                        <li>AS400Message.DIAGNOSTIC
                        <li>AS400Message.INFORMATIONAL
                        <li>AS400Message.ESCAPE
                        </ul>
                        The message type must be AS400Message.INFORMATIONAL for an immediate
                        message.
@param messageFile      The integrated file system path name of the message file.
@param substitutionData The substitution data.  The substitution data can be from 0-32767 bytes
                        for a conventional message and from 1-6000 bytes for an immediate message.
@see com.ibm.as400.access.ProgramCall#isStayOnThread()

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception ServerStartupException          If the AS/400 server cannot be started.
@exception UnknownHostException            If the AS/400 system cannot be located.
**/
    public static void writeMessage(AS400 system,
                                    String messageID,
                                    int messageType,
                                    String messageFile,
                                    byte[] substitutionData)
        throws  AS400SecurityException,
                ErrorCompletingRequestException,
                InterruptedException,
                IOException,
                ObjectDoesNotExistException,
                AS400Exception
    {
        try {
            RJobLog.writeMessage(system, messageID, messageType, messageFile, substitutionData);
        }
        catch(ResourceException e) {
            e.unwrap();
        }
    }



/**
Converts the ResourceListEnumeration (whose elements are RQueuedMessage objects)
to an Enumeration whose elements are QueuedMessage objects.
**/
    private static class EnumerationAdapter implements Enumeration
    {
       private Enumeration rEnum_;

       public EnumerationAdapter(Enumeration rEnum)
       {
           rEnum_ = rEnum;
       }

       public boolean hasMoreElements()
       {
           return rEnum_.hasMoreElements();
       }

       public Object nextElement()
       {
           return new QueuedMessage((RQueuedMessage)rEnum_.nextElement(), null);
       }
    }



}


