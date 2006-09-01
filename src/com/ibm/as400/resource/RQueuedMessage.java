///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RQueuedMessage.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.*;
import java.util.Date;
import java.io.*;
import java.beans.PropertyVetoException;


/**
The RQueuedMessage class represents a message in a message queue
or job log.

<a name="attributeIDs"><p>The following attribute IDs are supported:
<ul>
<li>{@link #ALERT_OPTION ALERT_OPTION}
<li>{@link #DATE_SENT DATE_SENT}
<li>{@link #DEFAULT_REPLY DEFAULT_REPLY}
<li>{@link #MESSAGE_FILE MESSAGE_FILE}
<li>{@link #MESSAGE_HELP MESSAGE_HELP}
<li>{@link #MESSAGE_ID MESSAGE_ID}
<li>{@link #MESSAGE_KEY MESSAGE_KEY}
<li>{@link #MESSAGE_QUEUE MESSAGE_QUEUE}
<li>{@link #MESSAGE_SEVERITY MESSAGE_SEVERITY}
<li>{@link #MESSAGE_TEXT MESSAGE_TEXT}
<li>{@link #MESSAGE_TYPE MESSAGE_TYPE}
<li>{@link #REPLY_STATUS REPLY_STATUS}
<li>{@link #SENDER_JOB_NAME SENDER_JOB_NAME}
<li>{@link #SENDER_USER_NAME SENDER_USER_NAME}
<li>{@link #SENDER_JOB_NUMBER SENDER_JOB_NUMBER}
<li>{@link #SENDING_PROGRAM_NAME SENDING_PROGRAM_NAME}
<li>{@link #SUBSTITUTION_DATA SUBSTITUTION_DATA}
</ul>
</a>

<p>Use any of these attribute IDs with
{@link com.ibm.as400.resource.Resource#getAttributeValue getAttributeValue()}
to access the attribute values for an RQueuedMessage.

<blockquote><pre>
// Create an RMessageQueue object to refer to a specific message queue.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RMessageQueue messageQueue = new RMessageQueue(system, "/QSYS.LIB/MYLIB.LIB/MYMSGQ.MSGQ");
<br>
// Get the first RQueuedMessage from the RMessageQueue.
RQueuedMessage queuedMessage = messageQueue.resourceAt(0);
<br>
// Get the message text from the RQueuedMessage.
String messageText = (String)queuedMessage.getAttributeValue(RQueuedMessage.MESSAGE_TEXT);
</pre></blockquote>

@deprecated Use
{@link com.ibm.as400.access.QueuedMessage QueuedMessage} instead, as this package may be removed in the future.
@see RMessageQueue
@see RJobLog
**/
//
// Implementation notes:
//
// * Message keys are 4 bytes.
//
public class RQueuedMessage
extends Resource
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



//-----------------------------------------------------------------------------------------
// Presentation.
//-----------------------------------------------------------------------------------------

    private static PresentationLoader   presentationLoader_ = new PresentationLoader("com.ibm.as400.resource.ResourceMRI");
    private static final String         ICON_BASE_NAME_     = "RQueuedMessage";
    private static final String         PRESENTATION_KEY_   = "QUEUED_MESSAGE";



//-----------------------------------------------------------------------------------------
// Attribute IDs.
//
// * If you add an attribute here, make sure and add it to the class javadoc,
//   and to ResourceMRI.java.
//-----------------------------------------------------------------------------------------

    static ResourceMetaDataTable attributes_        = new ResourceMetaDataTable(presentationLoader_, PRESENTATION_KEY_);

/**
Attribute ID for alert option.  This identifies a read-only String attribute,
which represents whether and when an SNA alert is created and sent for the
message.  Possible values are:
<ul>
<li>{@link #ALERT_OPTION_DEFER ALERT_OPTION_DEFER} - An alert is sent after
    local problem analysis.
<li>{@link #ALERT_OPTION_IMMEDIATE ALERT_OPTION_IMMEDIATE} - An alert is sent immediately
    when the message is sent to a message queue that has the allows alerts.
<li>{@link #ALERT_OPTION_NO ALERT_OPTION_NO} - No alert is sent.
<li>{@link #ALERT_OPTION_UNATTENDED ALERT_OPTION_UNATTENDED} - An alert is sent immediately
    when the system is running in unattended mode.
<li>"" - The alert option is not specified.
</ul>
**/
    public static final String ALERT_OPTION                    = "ALERT_OPTION";

    /**
    Constant for {@link #ALERT_OPTION ALERT_OPTION} attribute value -
    An alert is sent after local problem analysis.
    **/
    public static final String ALERT_OPTION_DEFER              = "*DEFER";

    /**
    Constant for {@link #ALERT_OPTION ALERT_OPTION} attribute value -
    An alert is sent immediately when the message is sent to a message queue
    that has the allows alerts.
    **/
    public static final String ALERT_OPTION_IMMEDIATE          = "*IMMED";

    /**
    Constant for {@link #ALERT_OPTION ALERT_OPTION} attribute value -
    No alert is sent.
    **/
    public static final String ALERT_OPTION_NO                 = "*NO";

    /**
    Constant for {@link #ALERT_OPTION ALERT_OPTION} attribute value -
    An alert is sent immediately when the system is running in unattended mode.
    **/
    public static final String ALERT_OPTION_UNATTENDED         = "*UNATTEND";

    static {
        attributes_.add(ALERT_OPTION, String.class, true,
                        new String[] { ALERT_OPTION_DEFER,
                            ALERT_OPTION_IMMEDIATE,
                            ALERT_OPTION_NO,
                            ALERT_OPTION_UNATTENDED,
                            "" }, null, true);
    }

/**
Attribute ID for date sent.  This identifies a read-only Date attribute,
which represents the date and time on which the message was sent.
The Date value is converted using the default Java locale.
**/
    public static final String DATE_SENT                        = "DATE_SENT";

    static {
        attributes_.add(DATE_SENT, Date.class, true);
    }

/**
Attribute ID for default reply.  This identifies a read-only String attribute,
which represents the text of the default reply when a stored message is being
listed, and a default reply exists.
**/
    public static final String DEFAULT_REPLY                    = "DEFAULT_REPLY";

    static {
        attributes_.add(DEFAULT_REPLY, String.class, true);
    }

/**
Attribute ID for message file.  This identifies a read-only String attribute,
which represents the fully qualified integrated file system path name
of the message file containing the message.

@see com.ibm.as400.access.QSYSObjectPathName
**/
    public static final String MESSAGE_FILE                = "MESSAGE_FILE";

    static {
        attributes_.add(MESSAGE_FILE, String.class, true);
    }

/**
Attribute ID for message help.  This identifies a read-only String attribute,
which represents the message help.
**/
    public static final String MESSAGE_HELP                     = "MESSAGE_HELP";

    static {
        attributes_.add(MESSAGE_HELP, String.class, true);
    }

/**
Attribute ID for message ID.  This identifies a read-only String attribute,
which represents the message identifier.
**/
    public static final String MESSAGE_ID                       = "MESSAGE_ID";

    static {
        attributes_.add(MESSAGE_ID, String.class, true);
    }

/**
Attribute ID for message key.  This identifies a read-only byte array attribute,
which represents the message key.
**/
    public static final String MESSAGE_KEY                      = "MESSAGE_KEY";

    static {
        attributes_.add(MESSAGE_KEY, byte[].class, true);
    }

/**
Attribute ID for message queue.  This identifies a read-only String attribute,
which represents the fully qualified integrated file system path name of
the message queue.

@see com.ibm.as400.access.QSYSObjectPathName
**/
    public static final String MESSAGE_QUEUE               = "MESSAGE_QUEUE";

    static {
        attributes_.add(MESSAGE_QUEUE, String.class, true);
    }

/**
Attribute ID for message severity.  This identifies a read-only Integer attribute,
which represents the severity of the message.  Possible values are 0 through 99.
**/
    public static final String MESSAGE_SEVERITY                 = "MESSAGE_SEVERITY";

    static {
        attributes_.add(MESSAGE_SEVERITY, Integer.class, true);
    }

/**
Attribute ID for message text.  This identifies a read-only String attribute,
which represents the message text.
**/
    public static final String MESSAGE_TEXT                     = "MESSAGE_TEXT";

    static {
        attributes_.add(MESSAGE_TEXT, String.class, true);
    }

/**
Attribute ID for message type.  This identifies a read-only Integer attribute,
which represents the message type.  Converted to an int, the possible values are:
<ul>
<li>{@link com.ibm.as400.access.AS400Message#COMPLETION COMPLETION }
<li>{@link com.ibm.as400.access.AS400Message#DIAGNOSTIC DIAGNOSTIC }
<li>{@link com.ibm.as400.access.AS400Message#INFORMATIONAL  INFORMATIONAL }
<li>{@link com.ibm.as400.access.AS400Message#INQUIRY  INQUIRY }
<li>{@link com.ibm.as400.access.AS400Message#SENDERS_COPY  SENDERS_COPY }
<li>{@link com.ibm.as400.access.AS400Message#REQUEST  REQUEST }
<li>{@link com.ibm.as400.access.AS400Message#REQUEST_WITH_PROMPTING  REQUEST_WITH_PROMPTING }
<li>{@link com.ibm.as400.access.AS400Message#NOTIFY  NOTIFY }
<li>{@link com.ibm.as400.access.AS400Message#ESCAPE  ESCAPE }
<li>{@link com.ibm.as400.access.AS400Message#NOTIFY_NOT_HANDLED  NOTIFY_NOT_HANDLED }
<li>{@link com.ibm.as400.access.AS400Message#ESCAPE_NOT_HANDLED  ESCAPE_NOT_HANDLED }
<li>{@link com.ibm.as400.access.AS400Message#REPLY_NOT_VALIDITY_CHECKED  REPLY_NOT_VALIDITY_CHECKED }
<li>{@link com.ibm.as400.access.AS400Message#REPLY_VALIDITY_CHECKED  REPLY_VALIDITY_CHECKED }
<li>{@link com.ibm.as400.access.AS400Message#REPLY_MESSAGE_DEFAULT_USED  REPLY_MESSAGE_DEFAULT_USED }
<li>{@link com.ibm.as400.access.AS400Message#REPLY_SYSTEM_DEFAULT_USED  REPLY_SYSTEM_DEFAULT_USED }
<li>{@link com.ibm.as400.access.AS400Message#REPLY_FROM_SYSTEM_REPLY_LIST  REPLY_FROM_SYSTEM_REPLY_LIST }
</ul>
**/                                                                                     // @A1A
    public static final String MESSAGE_TYPE                     = "MESSAGE_TYPE";

    static {
        attributes_.add(MESSAGE_TYPE, Integer.class, true,
                        new Integer[] { new Integer(AS400Message.COMPLETION),
                                        new Integer(AS400Message.DIAGNOSTIC),
                                        new Integer(AS400Message.INFORMATIONAL),
                                        new Integer(AS400Message.INQUIRY),
                                        new Integer(AS400Message.SENDERS_COPY),
                                        new Integer(AS400Message.REQUEST),
                                        new Integer(AS400Message.REQUEST_WITH_PROMPTING),
                                        new Integer(AS400Message.NOTIFY),
                                        new Integer(AS400Message.ESCAPE),
                                        new Integer(AS400Message.NOTIFY_NOT_HANDLED),           // @A1A
                                        new Integer(AS400Message.ESCAPE_NOT_HANDLED),           // @A1A
                                        new Integer(AS400Message.REPLY_NOT_VALIDITY_CHECKED),
                                        new Integer(AS400Message.REPLY_VALIDITY_CHECKED),
                                        new Integer(AS400Message.REPLY_MESSAGE_DEFAULT_USED),
                                        new Integer(AS400Message.REPLY_SYSTEM_DEFAULT_USED),
                                        new Integer(AS400Message.REPLY_FROM_SYSTEM_REPLY_LIST) }, null, true);
    }

/**
Attribute ID for reply status.  This identifies a read-only String attribute, which
represents the reply status of the message.   Possible values are:
<ul>
<li>{@link #REPLY_STATUS_ACCEPTS_SENT REPLY_STATUS_ACCEPTS_SENT} -
    Message accepts a reply, and a reply has been sent.
<li>{@link #REPLY_STATUS_ACCEPTS_NOT_SENT REPLY_STATUS_ACCEPTS_NOT_SENT} -
    Message accepts a reply, and a reply has not been sent.  (The message
    is waiting for a reply.)
<li>{@link #REPLY_STATUS_NOT_ACCEPT REPLY_STATUS_NOT_ACCEPT} -
    Message does not accept a reply.
</ul>
**/
    public static final String REPLY_STATUS                     = "REPLY_STATUS";

    /**
    Constant for {@link #REPLY_STATUS REPLY_STATUS} attribute value -
    Message accepts a reply, and a reply has been sent.
    **/
    public static final String REPLY_STATUS_ACCEPTS_SENT        = "A";

    /**
    Constant for {@link #REPLY_STATUS REPLY_STATUS} attribute value -
    Message accepts a reply, and a reply has not been sent.  (The message
    is waiting for a reply.)
    **/
    public static final String REPLY_STATUS_ACCEPTS_NOT_SENT    = "W";

    /**
    Constant for {@link #REPLY_STATUS REPLY_STATUS} attribute value -
    Message does not accept a reply.
    **/
    public static final String REPLY_STATUS_NOT_ACCEPT          = "N";

    static {
        attributes_.add(REPLY_STATUS, String.class, true,
            new String[] { REPLY_STATUS_ACCEPTS_SENT,
                REPLY_STATUS_ACCEPTS_NOT_SENT,
                REPLY_STATUS_NOT_ACCEPT }, null, true);
    }

/**
Attribute ID for sender job name.  This identifies a read-only String attribute,
which represents the job name of the sender.
**/
    public static final String SENDER_JOB_NAME                  = "SENDER_JOB_NAME";

    static {
        attributes_.add(SENDER_JOB_NAME, String.class, true);
    }

/**
Attribute ID for sender user name.  This identifies a read-only String attribute,
which represents the user name of the sender.
**/
    public static final String SENDER_USER_NAME                 = "SENDER_USER_NAME";

    static {
        attributes_.add(SENDER_USER_NAME, String.class, true);
    }

/**
Attribute ID for sender job number.  This identifies a read-only String attribute,
which represents the job number of the sender.
**/
    public static final String SENDER_JOB_NUMBER                = "SENDER_JOB_NUMBER";

    static {
        attributes_.add(SENDER_JOB_NUMBER, String.class, true);
    }

/**
Attribute ID for sending program name.  This identifies a read-only String attribute,
which represents the sending program name or ILE program name that contains the procedure
sending the message.
**/
    public static final String SENDING_PROGRAM_NAME             = "SENDING_PROGRAM_NAME";

    static {
        attributes_.add(SENDING_PROGRAM_NAME, String.class, true);
    }

/**
Attribute ID for substitution data.  This identifies a read-only byte array attribute,
which represents the values for subsitution variables in a predefined message, or the
text of an impromptu message.
**/
    public static final String SUBSTITUTION_DATA                = "SUBSTITUTION_DATA";

    static {
        attributes_.add(SUBSTITUTION_DATA, byte[].class, true);
    }



//-----------------------------------------------------------------------------------------
// Private data.
//-----------------------------------------------------------------------------------------



//-----------------------------------------------------------------------------------------
// Constructors.
//-----------------------------------------------------------------------------------------
//
// Implementation notes:
//
// * The constructor and set methods are public, since they
//   need to be accessible to the access.QueuedMessage class.

/**
Constructs an RQueuedMessage object.
**/
    public RQueuedMessage()
    {
        super(presentationLoader_.getPresentationWithIcon(PRESENTATION_KEY_, ICON_BASE_NAME_), null, attributes_);
    }




/**
Computes the resource key.

@param system           The system.
@param containerName    The container name (e.g. message queue or job log name).
@param messageKey       The message key.
**/
    static Object computeResourceKey(AS400 system, String containerName, byte[] messageKey)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(RQueuedMessage.class);
        buffer.append(':');
        buffer.append(system.getSystemName());
        buffer.append(':');
        buffer.append(system.getUserId());
        buffer.append(':');
        buffer.append(containerName);
        buffer.append(':');
        buffer.append(Presentation.bytesToHex(messageKey));
        return buffer.toString();
    }









    // @D6a -- new method
    /**
     Reload message help text.
     @param  helpTextFormatting Formatting performed on the help text.  Valid
             values for this parameter are defined in the MessageFile
             class.  They are no formatting, return formatting characters,
             and replace (substitute) formatting characters.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     **/
    public void load(int helpTextFormatting) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {

       //////////////////////////////////////////////////////////////////////
       // Note -- this method returns when things go bad.  The original code
       // threw exceptions but customers complained.  The purpose of this
       // method is to improve the help text so customers asked that if
       // the help text cannot be improved simply return so they can get
       // the original help text.  Don't throw an exception they have to
       // handle.
       //////////////////////////////////////////////////////////////////////

       AS400 system = getSystem();

       String file    = null;
       String library = null;
       String IFSPath = getAttributeValueAsString(MESSAGE_FILE);

       if ((system  == null) ||
           (IFSPath == null))
       {

          if (Trace.isTraceOn())
              Trace.log(Trace.INFORMATION, "system or path is null in load()");

          return;
       }

       try
       {
          QSYSObjectPathName path = new QSYSObjectPathName(IFSPath);
          library = path.getLibraryName();
          file    = path.getObjectName();

          String ID = getAttributeValueAsString(MESSAGE_ID);
          byte[] substitutionData = (byte[]) getAttributeValueAsObject(SUBSTITUTION_DATA);

          if ((library == null) ||
              (file    == null) ||
              (ID      == null))
          {
             if (Trace.isTraceOn())
                 Trace.log(Trace.INFORMATION, "library, file or ID is null in load()");

             return;
          }

          AS400Message result = null;
          MessageFile msgFile = new MessageFile(system,
                                           QSYSObjectPathName.toPath(library, file, "MSGF"));

          msgFile.setHelpTextFormatting(helpTextFormatting);
          result = msgFile.getMessage(ID, substitutionData);

          initializeAttributeValue(MESSAGE_HELP, result.getHelp());
        }
        catch (Exception e)
        {
           if (Trace.isTraceOn())
               Trace.log(Trace.ERROR, "Exception getting help from message file in load()", e);
        }
    }



    // @D6a new method to support load()
    private Object getAttributeValueAsObject(Object attributeID)
    {
        try
        {
           return getAttributeValue(attributeID);
        }
        catch(ResourceException e)
        {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error getting attribute value", e);
            return null;
        }
    }


    // @D6a new method to support load()
    private String getAttributeValueAsString(Object attributeID)
    {
        try
        {
            return (String) getAttributeValue(attributeID);
        }
        catch(ResourceException e)
        {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error getting attribute value", e);
            return null;
        }
    }




}

