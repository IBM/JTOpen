///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: QueuedMessage.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import com.ibm.as400.resource.ResourceException;
import com.ibm.as400.resource.RQueuedMessage;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;



/**
The QueuedMessage class represents a message on an AS/400 message queue
or job log.

<p>Some of the attributes have associated get methods
defined in this class.  These are provided for backwards compatibility
with previous versions of the AS/400 Toolbox for Java.  The complete
set of attribute values can be accessed using the
{@link com.ibm.as400.resource.RQueuedMessage RQueuedMessage } class.

@see com.ibm.as400.resource.RQueuedMessage
**/
//
// Implementation notes:
//
// * The constructor and set methods are not public, since it
//   never makes sense for anyone other than MessageQueue or
//   JobLog to call these.
//
// * Message keys are 4 bytes.
//
public class QueuedMessage
extends AS400Message
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;


//-----------------------------------------------------------------------------------------
// Private data.
//-----------------------------------------------------------------------------------------

    private RQueuedMessage              rQueuedMessage_;
    private MessageQueue                messageQueue_;
    private String help = null;


//-----------------------------------------------------------------------------------------
// Constructors.
//-----------------------------------------------------------------------------------------

/**
Constructs a QueuedMessage object.
**/
    QueuedMessage()
    {
        rQueuedMessage_ = new RQueuedMessage();
    }



/**
Constructs a QueuedMessage object.

@param messageQueue The message queue.
**/
    QueuedMessage(MessageQueue messageQueue)
    {
        if (messageQueue == null)
            throw new NullPointerException("messageQueue");
        rQueuedMessage_ = new RQueuedMessage();
        messageQueue_   = messageQueue;
    }



/**
Constructs a QueuedMessage object.

@param rQueuedMessage    The RQueuedMessage object.
@param messageQueue The message queue.
**/
//
// This is a package scope constructor!
//
    QueuedMessage(RQueuedMessage rQueuedMessage, MessageQueue messageQueue) // @D1C
    {
        rQueuedMessage_ = rQueuedMessage;
        messageQueue_ = messageQueue;                                       // @D1A
    }



/*-------------------------------------------------------------------------
Convenience methods for getting attribute values.
All ResourceExceptions are swallowed!
-------------------------------------------------------------------------*/
    private int getAttributeValueAsInt(Object attributeID)
    {
        try {
            return ((Integer)rQueuedMessage_.getAttributeValue(attributeID)).intValue();
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error getting attribute value", e);
            return -1;
        }
    }


    private Object getAttributeValueAsObject(Object attributeID)
    {
        try {
            return rQueuedMessage_.getAttributeValue(attributeID);
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error getting attribute value", e);
            return null;
        }
    }



    private String getAttributeValueAsString(Object attributeID)
    {
        try {
            return (String)rQueuedMessage_.getAttributeValue(attributeID);
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error getting attribute value", e);
            return null;
        }
    }



/**
Returns the date and time the message was sent.
The returned Calendar object will have the following
fields set:
<ul>
  <li>Calendar.YEAR
  <li>Calendar.MONTH
  <li>Calendar.DAY_OF_MONTH
  <li>Calendar.HOUR
  <li>Calendar.MINUTE
  <li>Calendar.SECOND
</ul>

@return The date and time the message was sent, or null
        if not applicable.

@see com.ibm.as400.resource.RQueuedMessage#DATE_SENT
**/
    public Calendar getDate()
    {
        Date date = (Date)getAttributeValueAsObject(RQueuedMessage.DATE_SENT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }



/**
Returns the default reply.

@return The default reply, or null if it is not set.

@see com.ibm.as400.resource.RQueuedMessage#DEFAULT_REPLY
**/
    public String getDefaultReply()
    {
        return getAttributeValueAsString(RQueuedMessage.DEFAULT_REPLY);
    }



/**
Returns the message file name.

@return The message file name, or null if it is not set.

@see com.ibm.as400.resource.RQueuedMessage#MESSAGE_FILE
**/
    public String getFileName()
    {
        String messageFile = getAttributeValueAsString(RQueuedMessage.MESSAGE_FILE);
        if (messageFile == null)
            return null;
        else {
            QSYSObjectPathName path = new QSYSObjectPathName(messageFile);
            return path.getObjectName();
        }
    }



/**
Returns the sending program name.

@return The sending program name, or "" if it is not set.

@see com.ibm.as400.resource.RQueuedMessage#SENDING_PROGRAM_NAME
**/
    public String getFromProgram()
    {
        return getAttributeValueAsString(RQueuedMessage.SENDING_PROGRAM_NAME);
    }



/**
Returns the sender job name.

@return The sender job name, or "" if it is not set.

@see com.ibm.as400.resource.RQueuedMessage#SENDER_JOB_NAME
**/
    public String getFromJobName()
    {
        return getAttributeValueAsString(RQueuedMessage.SENDER_JOB_NAME);
    }



/**
Returns the sender job number.

@return The sender job number, or "" if it is not set.

@see com.ibm.as400.resource.RQueuedMessage#SENDER_JOB_NUMBER
**/
    public String getFromJobNumber()
    {
        return getAttributeValueAsString(RQueuedMessage.SENDER_JOB_NUMBER);
    }


/**
Returns the message help.

<p>Message formatting characters may appear in the message help
and are defined as follows:
<UL>
  <LI>&N - Force the text to a new line indented to column 2.  If the text
           is longer than 1 line, the next lines should be indented to
           column 4 until the end of the text or another format control
           character is found.
  <LI>&P - Force the text to a new line indented to column 6.  If the
           text is longer than 1 line, the next lines should start in
           column 4 until the end of the text or another format control
           character is found.
  <LI>&B - Force the text to a new line starting in column 4.  If the
           text is longer than 1 line, the next lines should start in
           column 6 until the end of the text or another format control
           character is found.
</UL>

@return  The message help, or null if it is not set.

@see com.ibm.as400.resource.RQueuedMessage#MESSAGE_HELP
**/
    public String getHelp()
    {
        if (help == null)
           return getAttributeValueAsString(RQueuedMessage.MESSAGE_HELP);
        else
           return help;
    }



/**
Returns the message ID.

@return The message ID, or null if it is not set.

@see com.ibm.as400.resource.RQueuedMessage#MESSAGE_ID
**/
    public String getID()
    {
        return getAttributeValueAsString(RQueuedMessage.MESSAGE_ID);
    }



/**
Returns the message key.

@return The message key, or null if it is not set.

@see com.ibm.as400.resource.RQueuedMessage#MESSAGE_KEY
**/
    public byte[] getKey()
    {
        return (byte[])getAttributeValueAsObject(RQueuedMessage.MESSAGE_KEY);
    }



/**
Returns the message file library.

@return The message file library, or null if it is not set.

@see com.ibm.as400.resource.RQueuedMessage#MESSAGE_FILE
**/
    public String getLibraryName()
    {
        String messageFile = getAttributeValueAsString(RQueuedMessage.MESSAGE_FILE);
        if (messageFile == null)
            return null;
        else {
            QSYSObjectPathName path = new QSYSObjectPathName(getAttributeValueAsString(RQueuedMessage.MESSAGE_FILE));
            return path.getLibraryName();
        }
    }



/**
Returns the full integrated file system path name of the
message file.

@return  The full integrated file system path name of the
         message file name, or null if it is not set.
**/
    public String getPath()
    {
        return getAttributeValueAsString(RQueuedMessage.MESSAGE_FILE);
    }



/**
Returns the message queue.

@return The message queue, or null if it is not set.
**/
    public MessageQueue getQueue()
    {
        return messageQueue_;
    }



/**
Returns the reply status.

@return The reply status, "" if it is not set, or null
        if it is not applicable.

@see com.ibm.as400.resource.RQueuedMessage#REPLY_STATUS
**/
    public String getReplyStatus()
    {
        return getAttributeValueAsString(RQueuedMessage.REPLY_STATUS);
    }



/**
Returns the message severity.

@return The message severity.  Valid values are between 0 and 99, or -1 if
        it is not set.

@see com.ibm.as400.resource.RQueuedMessage#MESSAGE_SEVERITY
**/
    public int getSeverity()
    {
        return getAttributeValueAsInt(RQueuedMessage.MESSAGE_SEVERITY);
    }



/**
Returns the substitution data.  This is unconverted data used to
fill in the replacement characters in the message.

@return The subsitution data, or null if not set.

@see com.ibm.as400.resource.RQueuedMessage#SUBSTITUTION_DATA
**/
    public byte[] getSubstitutionData()
    {
        return (byte[])getAttributeValueAsObject(RQueuedMessage.SUBSTITUTION_DATA);
    }



/**
Returns the message text with the substitution text inserted.

@return The message text, or null if it is not set.

@see com.ibm.as400.resource.RQueuedMessage#MESSAGE_TEXT
**/
    public String getText()
    {
        return getAttributeValueAsString(RQueuedMessage.MESSAGE_TEXT);
    }



/**
Returns the message type.

@return The message type, or null if it is not set.  Valid values are:
        <ul>
          <li>COMPLETION
          <li>DIAGNOSTIC
          <li>INFORMATIONAL
          <li>INQUIRY
          <li>SENDERS_COPY
          <li>REQUEST
          <li>REQUEST_WITH_PROMPTING
          <li>NOTIFY
          <li>ESCAPE
          <li>REPLY_NOT_VALIDITY_CHECKED
          <li>REPLY_VALIDITY_CHECKED
          <li>REPLY_MESSAGE_DEFAULT_USED
          <li>REPLY_SYSTEM_DEFAULT_USED
          <li>REPLY_FROM_SYSTEM_REPLY_LIST
        </ul>

@see com.ibm.as400.resource.RQueuedMessage#MESSAGE_TYPE
**/
    public int getType()
    {
        return getAttributeValueAsInt(RQueuedMessage.MESSAGE_TYPE);
    }



/**
Returns the sender user name.

@return The sender user name, or "" if it is not set.

@see com.ibm.as400.resource.RQueuedMessage#SENDER_USER_NAME
**/
    public String getUser()
    {
        return getAttributeValueAsString(RQueuedMessage.SENDER_USER_NAME);
    }






    // @D6a new method
    /**
     Reloads message help text.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     **/
    public void load() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
       load(MessageFile.DEFAULT_FORMATTING);
    }

    // @D6a -- new method that is built from the original load() method.
    /**
     Reloads message help text.
     @param  helpTextFormatting Formatting performed on the help text.  Valid
             values for this parameter are defined in the MessageFile
             class.  They are no formatting, return formatting characters,
             and replace (substitute) formatting characters.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     **/
    public void load(int helpTextFormatting) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
       rQueuedMessage_.load(helpTextFormatting);
    }










// @D1C
    public String toString()
    {
        return super.toStringM2();
    }

}

