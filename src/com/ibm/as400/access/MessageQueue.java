///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: MessageQueue.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import com.ibm.as400.resource.ResourceException;
import com.ibm.as400.resource.RMessageQueue;
import com.ibm.as400.resource.RQueuedMessage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Enumeration;



/**
The MessageQueue class represents an AS/400 message queue.  If no message
queue path is set, then the default is {@link #CURRENT CURRENT},
which represents the current user's message queue,
<code>/QSYS.LIB/QUSRSYS.LIB/<em>userID</em>.MSGQ</code>.

<p>Some of the selections have associated get and set methods
defined in this class.  These are provided for backwards compatibility
with previous versions of the AS/400 Toolbox for Java.  The complete
set of selections can be accessed using the
{@link com.ibm.as400.resource.RMessageQueue  RMessageQueue } class.

<p>QueuedMessage objects have many attributes.  Only some of these attribute
values are set, depending on how a QueuedMessage object is created.  The
following is a list of attributes whose values are set on QueuedMessage
objects returned in a list of messages:
<ul>
<li>date sent
<li>default reply
<li>message ID
<li>message key
<li>message queue
<li>message severity
<li>message text
<li>message type
<li>reply status
<li>sender job name
<li>sender job number
<li>sender user name
<li>sending program name
</ul>

<a name="receiveIDs">
<p>The following is a list of attributes whose values are set on
objects returned by receive():
<ul>
<li>alert option
<li>date sent
<li>message file
<li>message help
<li>message ID
<li>message key
<li>message severity
<li>message text
<li>message type
<li>sender job name
<li>sender job number
<li>sender user name
<li>sending program name
<li>substitution data
</ul>
</a>

@see com.ibm.as400.resource.RMessageQueue
**/
public class MessageQueue
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;


//-----------------------------------------------------------------------------------------
// Message types.
//-----------------------------------------------------------------------------------------

/**
Constant referring to all messages in the message queue.
**/
    public final static String ALL                     = RMessageQueue.ALL;

/**
Constant referring to any message in the message queue.
**/
    public final static String ANY                     = RMessageQueue.ANY;

/**
Constant referring to a message identified by a key.
**/
    public final static String BYKEY                   = RMessageQueue.BYKEY;

/**
Constant referring to completion messages.
**/
    public final static String COMPLETION              = RMessageQueue.COMPLETION;

/**
Constant referring to the sender's copy of a previously sent
inquiry message.
**/
    public final static String COPY                    = RMessageQueue.COPY;

/**
Constant referring to the current user's message queue.
**/
    public final static String CURRENT                 = RMessageQueue.CURRENT;

/**
Constant referring to diagnostic messages.
**/
    public final static String DIAGNOSTIC              = RMessageQueue.DIAGNOSTIC;

/**
Constant referring to the first message in the message queue.
**/
    public final static String FIRST                   = RMessageQueue.FIRST;

/**
Constant referring to informational messages.
**/
    public final static String INFORMATIONAL           = RMessageQueue.INFORMATIONAL;

/**
Constant referring to inquiry messages.
**/
    public final static String INQUIRY                 = RMessageQueue.INQUIRY;

/**
Constant referring to all messages in the message queue
except unanswered inquiry and unanswered senders' copy messages.
**/
    public final static String KEEP_UNANSWERED         = RMessageQueue.KEEP_UNANSWERED;

/**
Constant referring to the last message in the message queue.
**/
    public final static String LAST                    = RMessageQueue.LAST;

/**
Constant referring to messages that need a reply.
**/
    public final static String MESSAGES_NEED_REPLY     = RMessageQueue.MESSAGES_NEED_REPLY;

/**
Constant referring to messages that do not need a reply.
**/
    public final static String MESSAGES_NO_NEED_REPLY  = RMessageQueue.MESSAGES_NO_NEED_REPLY;

/**
Constant referring to all new messages in the message queue.
New messages are those that have not been received.
**/
    public final static String NEW                     = RMessageQueue.NEW;

/**
Constant referring to the message key for the newest message in the queue.
**/
    public final static byte[] NEWEST                  = RMessageQueue.NEWEST;

/**
Constant referring to the next message in the message queue.
**/
    public final static String NEXT                    = RMessageQueue.NEXT;

/**
Constant referring to all old messages in the message queue.
Old messages are those that have already been received.
**/
    public final static String OLD                     = RMessageQueue.OLD;

/**
Constant referring to the message key for the oldest message in the queue.
**/
    public final static byte[] OLDEST                  = RMessageQueue.OLDEST;

/**
Constant referring to the previous message in the message queue.
**/
    public final static String PREVIOUS                = RMessageQueue.PREVIOUS;

/**
Constant indicating that the message should be removed from
the message queue.
**/
    public final static String REMOVE                  = RMessageQueue.REMOVE;

/**
Constant referring to the reply to an inquiry message.
**/
    public final static String REPLY                   = RMessageQueue.REPLY;      // @C1

/**
Constant indicating that the message should remain in the
message queue without changing its new or old designation.
**/
    public final static String SAME                    = RMessageQueue.SAME;

/**
Constant referring to the sender's copies of messages that
need replies.
**/
    public final static String SENDERS_COPY_NEED_REPLY = RMessageQueue.SENDERS_COPY_NEED_REPLY;



//-----------------------------------------------------------------------------------------
// Private data.
//-----------------------------------------------------------------------------------------

    private RMessageQueue rMessageQueue_;
    private int           helpTextFormatting_     = MessageFile.NO_FORMATTING;       // @E1A
    private boolean       listDirection_          = true;                            // @E1A

    private transient   PropertyChangeSupport   propertyChangeSupport_;
    private transient   VetoableChangeSupport   vetoableChangeSupport_;



//-----------------------------------------------------------------------------------------
// Code.
//-----------------------------------------------------------------------------------------

/**
Constructs a MessageQueue object.
**/
    public MessageQueue()
    {
        rMessageQueue_ = new RMessageQueue();
        initializeTransient();
    }



/**
Constructs a MessageQueue object.

@param  system  The system.
**/
    public MessageQueue(AS400 system)
    {
        rMessageQueue_ = new RMessageQueue(system);
        initializeTransient();
    }



/**
Constructs a MessageQueue object.

@param  system  The system.
@param  path    The fully qualified integrated file system path name
                of the message queue, or CURRENT to refer to the user's
                default message queue.
**/
    public MessageQueue(AS400 system, String path)
    {
        rMessageQueue_ = new RMessageQueue(system, path);
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
        rMessageQueue_.addPropertyChangeListener(listener);
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
        rMessageQueue_.addVetoableChangeListener(listener);
    }



/**
Returns the number of messages in the message queue.

@return The number of messages, or 0 if no list has been retrieved.
**/
    public int getLength()
    {
        try {
            rMessageQueue_.waitForComplete();
            return (int)rMessageQueue_.getListLength();
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "An error occurred while getting the message queue length", e);
            return 0;
        }
    }



// @E1A
    /**
      * Returns the status of help text formatting.  Possible values are:
      * <UL>
      * <LI>{@link com.ibm.as400.access.MessageFile#NO_FORMATTING MessageFile.NO_FORMATTING} - The help text is returned as a string of characters.
      * This is the default.
      * <LI>{@link com.ibm.as400.access.MessageFile#RETURN_FORMATTING_CHARACTERS MessageFile.RETURN_FORMATTING_CHARACTERS} - The help text contains AS/400
      * formatting characters.  The formatting characters are:
      * <UL>
      * &N -- Force a new line <BR>
      * &P -- Force a new line and indent the new line six characters <BR>
      * &B -- Force a new line and indent the new line four characters
      * </UL>
      * <LI>{@link com.ibm.as400.access.MessageFile#SUBSTITUTE_FORMATTING_CHARACTERS MessageFile.SUBSTITUTE_FORMATTING_CHARACTERS} - The MessageFile class replaces
      * AS/400 formatting characters with newline and space characters.
      * </UL>
      * @return  The status of help text formatting.
      **/
   public int getHelpTextFormatting()
   {
      return helpTextFormatting_;
   }



// @E1A
/**
Returns the list direction.

@return true if the messages are listed in order from oldest to
        newest; false if the messages are listed in order from newest
        to oldest.
**/
   public boolean getListDirection()
   {
      return listDirection_;
   }



/**
Returns the list of messages in the message queue.

@return An Enumeration of {@link com.ibm.as400.access.QueuedMessage QueuedMessage}
objects.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public Enumeration getMessages ()
        throws  AS400Exception,
                AS400SecurityException,
                ErrorCompletingRequestException,
                InterruptedException,
                IOException,
                ObjectDoesNotExistException
    {
        try {
            rMessageQueue_.refreshContents(); // @E4A
            return new EnumerationAdapter(rMessageQueue_.resources());
        }
        catch(ResourceException e) {
            e.unwrap();
            return null;
        }
    }



/**
Returns the fully qualified integrated file system path name
of the message queue, or CURRENT to refer to the user's default
message queue.

@return The fully qualified integrated file system path name of the
        message queue, or CURRENT to refer to the user's default
        message queue.
**/
    public String getPath()
    {
        return rMessageQueue_.getPath();
    }



/**
Returns the selection that describes which messages are returned.

@return The selection.  Possible values are:
        <ul>
        <li>{@link #ALL ALL}
        <li>{@link #MESSAGES_NEED_REPLY MESSAGES_NEED_REPLY}
        <li>{@link #MESSAGES_NO_NEED_REPLY MESSAGES_NO_NEED_REPLY}
        <li>{@link #SENDERS_COPY_NEED_REPLY SENDERS_COPY_NEED_REPLY}
        </ul>

@see com.ibm.as400.resource.RMessageQueue#SELECTION_CRITERIA
**/
    public String getSelection()
    {
        return getSelectionValueAsString(RMessageQueue.SELECTION_CRITERIA);
    }



/*-------------------------------------------------------------------------
Convenience method for getting selection values.
All ResourceExceptions are swallowed!
-------------------------------------------------------------------------*/
    private int getSelectionValueAsInt(Object selectionID)
    {
        try {
            return ((Integer)rMessageQueue_.getSelectionValue(selectionID)).intValue();
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error getting selection value", e);
            return -1;
        }
    }



    private String getSelectionValueAsString(Object selectionID)
    {
        try {
            return (String)rMessageQueue_.getSelectionValue(selectionID);
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error getting selection value", e);
            return null;
        }
    }



/**
Returns the severity of the messages which are returned.

@return The severity of the messages which are returned.

@see com.ibm.as400.resource.RMessageQueue#SEVERITY_CRITERIA
**/
    public int getSeverity()
    {
        return getSelectionValueAsInt(RMessageQueue.SEVERITY_CRITERIA);
    }



/**
Returns the system.

@return The system.
**/
    public AS400 getSystem()
    {
        return rMessageQueue_.getSystem();
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
Receives a message from the message queue by key.  This method
receives a message of any type except sender's copy.  The message is removed
from the message queue.  See the <a href="#receiveIDs">list of QueuedMessage
attribute values</a> which are set on a received message.

@param messageKey   The message key.
@return             The queued message, or null if the message can not
                    be received.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.

@see QueuedMessage#getKey
**/
    public QueuedMessage receive(byte[] messageKey)
        throws AS400Exception,
               AS400SecurityException,
               IOException,
               ObjectDoesNotExistException,
               ErrorCompletingRequestException,
               InterruptedException
    {
        try {
            RQueuedMessage rQueuedMessage = rMessageQueue_.receive(messageKey);
            if (rQueuedMessage == null)
                return null;
            return new QueuedMessage(rQueuedMessage, this); // @D2C
        }
        catch(ResourceException e) {
            e.unwrap();
            return null;
        }
    }



/**
Receives a message from the message queue.  See the <a href="#receiveIDs">list of QueuedMessage
attribute values</a> which are set on a received message.

@param messageKey       The message key, or null if no message key is needed.
@param waitTime         The number of seconds to wait for the message to arrive in the queue
                        so it can be received.  If the message is not received within the
                        specified wait time, null is returned.  Special values are:
                        <ul>
                        <li>0 - Do not wait for the message. If the message is not in the
                                queue and you specified a message key, null is returned.
                        <li>-1 - Wait until the message arrives in the queue and is received,
                                 no matter how long it takes. The system has no limit for
                                 the wait time.
                        </ul>
@param messageAction    The action to take after the message is received. Valid values are:
                        <ul>
                        <li>OLD -
                            Keep the message in the message queue and mark it
                            as an old message.  You can receive the message
                            again only by using the message key or by
                            specifying the message type NEXT, PREVIOUS,
                            FIRST, or LAST.
                        <li>REMOVE -
                            Remove the message from the message queue.  The
                            message key is no longer valid, so you cannot
                            receive the message again.
                        <li>SAME -
                            Keep the message in the message queue without
                            changing its new or old designation. SAME lets
                            you receive the message again later
                            without using the message key.
                        </ul>

@param messageType      The type of message to return.  Valid values are:
                        <ul>
                        <li>ANY -
                            Receives a message of any type except sender's copy.
                            The message key is optional.
                        <li>COMPLETION -
                            Receives a completion message.  The message key is
                            optional.
                        <li>COPY -
                            Receives the sender's copy of a previously sent
                            inquiry message.  The message key is required.
                        <li>DIAGNOSTIC -
                            Receives a diagnostic message.  The message key is
                            optional.
                        <li>FIRST -
                            Receives the first new or old message in the queue.
                            The message key is disallowed.
                        <li>INFORMATIONAL -
                            Receives an informational message.  The message key is
                            optional.
                        <li>INQUIRY -
                            Receives an inquiry message.  If the action is
                            REMOVE and a reply to the inquiry message has not
                            been sent yet, the default reply is automatically
                            sent when the inquiry message is received.
                            The message key is optional.
                        <li>LAST -
                            Receives the last new or old message in the queue.
                            The message key is disallowed.
                        <li>NEXT -
                            Receives the next new or old message after the
                            message with the specified key.  You can use the
                            special value TOP for the message key.  TOP designates
                            the message at the top of the message queue.
                            The message key is required.
                        <li>PREVIOUS -
                            Receives the new or old message before the message
                            with the specified key.  The message key is required.
                        <li>REPLY -
                            Receives the reply to an inquiry message.  For the
                            message key, you can use the key to the sender's copy
                            of the inquiry or notify message.  The message key is
                            optional.
                        </ul>
@return             The queued message, or null if the message can not
                    be received.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.

@see QueuedMessage#getKey
**/
    public QueuedMessage receive(byte[] messageKey,
                                 int waitTime,
                                 String messageAction,
                                 String messageType)
        throws AS400Exception,
               AS400SecurityException,
               IOException,
               ObjectDoesNotExistException,
               ErrorCompletingRequestException,
               InterruptedException
    {
        try {
            RQueuedMessage rQueuedMessage = rMessageQueue_.receive(messageKey, waitTime, messageAction, messageType);
            if (rQueuedMessage == null)
                return null;
            return new QueuedMessage(rQueuedMessage, this);   // @D2C
        }
        catch(ResourceException e) {
            e.unwrap();
            return null;
        }
  }



/**
Remove all messages from the message queue.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public void remove()
        throws AS400Exception,
               AS400SecurityException,
               IOException,
               ObjectDoesNotExistException,
               ErrorCompletingRequestException,
               InterruptedException
    {
        try {
            rMessageQueue_.remove();
        }
        catch(ResourceException e) {
            e.unwrap();
        }
    }



/**
Removes a message from the message queue.

@param messageKey   The message key.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public void remove(byte[] messageKey)
        throws AS400Exception,
               AS400SecurityException,
               IOException,
               ObjectDoesNotExistException,
               ErrorCompletingRequestException,
               InterruptedException
    {
        try {
            rMessageQueue_.remove(messageKey);
        }
        catch(ResourceException e) {
            e.unwrap();
        }
    }



/**
Remove messages from the message queue.

@param messageType  The type of message to remove.  Valid values are:
                    <ul>
                    <li>ALL -
                        All messages in the message queue.
                    <li>KEEP_UNANSWERED -
                        All messages in the message queue except unanswered
                        inquiry and unanswered senders' copy messages.
                    <li>NEW -
                        All new messages in the message queue.  New messages
                        are those that have not been received.
                    <li>OLD -
                        All old messages in the message queue.  Old messages
                        are those that have already been received.
                    </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public void remove(String messageType)
        throws AS400Exception,
               AS400SecurityException,
               IOException,
               ObjectDoesNotExistException,
               ErrorCompletingRequestException,
               InterruptedException
    {
        try {
            rMessageQueue_.remove(messageType);
        }
        catch(ResourceException e) {
            e.unwrap();
        }
    }



/**
Removes a PropertyChangeListener.

@param listener The listener.
**/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport_.removePropertyChangeListener(listener);
        rMessageQueue_.removePropertyChangeListener(listener);
    }



/**
Removes a VetoableChangeListener.

@param listener The listener.
**/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        vetoableChangeSupport_.removeVetoableChangeListener(listener);
        rMessageQueue_.removeVetoableChangeListener(listener);
    }



/**
Replies to and removes a message.

@param messageKey   The message key.
@param replyText    The reply.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public void reply(byte[] messageKey, String replyText)
        throws AS400Exception,
               AS400SecurityException,
               IOException,
               ObjectDoesNotExistException,
               ErrorCompletingRequestException,
               InterruptedException
    {
        try {
            rMessageQueue_.reply(messageKey, replyText);
        }
        catch(ResourceException e) {
            e.unwrap();
        }
    }


//@E3A
/**
Replies to and removes a message if requested.

@param messageKey   The message key.
@param replyText    The reply.
@param remove       true to remove the inquiry message and the reply from the
                    message queue after the reply is sent, false to keep the
                    inquiry message and the reply after the reply is sent.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public void reply(byte[] messageKey, String replyText, boolean remove)
        throws AS400Exception,
               AS400SecurityException,
               IOException,
               ObjectDoesNotExistException,
               ErrorCompletingRequestException,
               InterruptedException
    {
        try {
            rMessageQueue_.reply(messageKey, replyText, remove);
        }
        catch(ResourceException e) {
            e.unwrap();
        }
    }


/**
Sends an informational message to the message queue.

@param messageID        The message ID.
@param messageFile      The integrated file system path name of the message file.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public void sendInformational(String messageID, String messageFile)
        throws AS400Exception,
               AS400SecurityException,
               IOException,
               ObjectDoesNotExistException,
               ErrorCompletingRequestException,
               InterruptedException
    {
        try {
            rMessageQueue_.sendInformational(messageID, messageFile);
        }
        catch(ResourceException e) {
            e.unwrap();
        }
    }



/**
Sends an informational message to the message queue.

@param messageID        The message ID.
@param messageFile      The integrated file system path name of the message file.
@param substitutionData The substitution data for the message, or null if none.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public void sendInformational(String messageID, String messageFile, byte[] substitutionData)
        throws AS400Exception,
               AS400SecurityException,
               IOException,
               ObjectDoesNotExistException,
               ErrorCompletingRequestException,
               InterruptedException
    {
        try {
            rMessageQueue_.sendInformational(messageID, messageFile, substitutionData);
        }
        catch(ResourceException e) {
            e.unwrap();
        }
    }



/**
Sends an informational message to the message queue.

@param messageText The message text.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public void sendInformational(String messageText)
        throws AS400Exception,
               AS400SecurityException,
               IOException,
               ObjectDoesNotExistException,
               ErrorCompletingRequestException,
               InterruptedException
    {
        try {
            rMessageQueue_.sendInformational(messageText);
        }
        catch(ResourceException e) {
            e.unwrap();
        }
  }



/**
Sends an inquiry message to the message queue.

@param messageID        The message ID.
@param messageFile      The integrated file system path name of the message file.
@param replyMessageQueue The integrated file system path name of the reply message queue.
@return                 The message key.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public byte[] sendInquiry(String messageID,
                            String messageFile,
                            String replyMessageQueue)
        throws AS400Exception,
               AS400SecurityException,
               IOException,
               ObjectDoesNotExistException,
               ErrorCompletingRequestException,
               InterruptedException
    {
        try {
            return rMessageQueue_.sendInquiry(messageID, messageFile, replyMessageQueue);
        }
        catch(ResourceException e) {
            e.unwrap();
            return null;
        }
    }



/**
Sends an inquiry message to the message queue.

@param messageID        The message ID.
@param messageFile      The integrated file system path name of the message file.
@param substitutionData The substitution data for the message, or null if none.
@param replyMessageQueue The integrated file system path name of the reply message queue.
@return                 The message key.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public byte[] sendInquiry(String messageID,
                              String messageFile,
                              byte[] substitutionData,
                              String replyMessageQueue)
        throws AS400Exception,
               AS400SecurityException,
               IOException,
               ObjectDoesNotExistException,
               ErrorCompletingRequestException,
               InterruptedException
    {
        try {
            return rMessageQueue_.sendInquiry(messageID, messageFile, substitutionData, replyMessageQueue);
        }
        catch(ResourceException e) {
            e.unwrap();
            return null;
        }
    }



/**
Sends an inquiry message to the message queue.

@param messageText The message text.
@param replyMessageQueue The integrated file system path name of the reply message queue.
@return                 The message key.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public byte[] sendInquiry(String messageText, String replyMessageQueue)
        throws AS400Exception,
               AS400SecurityException,
               IOException,
               ObjectDoesNotExistException,
               ErrorCompletingRequestException,
               InterruptedException
    {
        try {
            return rMessageQueue_.sendInquiry(messageText, replyMessageQueue);
        }
        catch(ResourceException e) {
            e.unwrap();
            return null;
        }
    }



// @E1A
    /**
      * Sets the help text formatting value.  Possible values are:
      * <UL>
      * <LI>{@link com.ibm.as400.access.MessageFile#NO_FORMATTING MessageFile.NO_FORMATTING} - the help text is returned as a string of characters.
      * This is the default.
      * <LI>{@link com.ibm.as400.access.MessageFile#RETURN_FORMATTING_CHARACTERS MessageFile.RETURN_FORMATTING_CHARACTERS} - the help text contains AS/400
      * formatting characters.  The formatting characters are:
      * <UL>
      * &N -- Force a new line <BR>
      * &P -- Force a new line and indent the new line six characters <BR>
      * &B -- Force a new line and indent the new line four characters
      * </UL>
      * <LI>{@link com.ibm.as400.access.MessageFile#SUBSTITUTE_FORMATTING_CHARACTERS MessageFile.SUBSTITUTE_FORMATTING_CHARACTERS} - the MessageFile class replaces
      * AS/400 formatting characters with new line and space characters.
      * </UL>
      * @param value The help text formatting value.
      **/
   public void setHelpTextFormatting(int helpTextFormatting)
   {
      try {
          rMessageQueue_.setSelectionValue(RMessageQueue.FORMATTING_CHARACTERS,
             new Integer(helpTextFormatting));
          helpTextFormatting_ = helpTextFormatting;
      }
      catch(ResourceException e) {
       if (Trace.isTraceOn())
             Trace.log(Trace.ERROR, "Error setting help text formatting", e);
      }
   }



// @E1A
/**
Sets the list direction.

@param listDirection true to list the messages in order from oldest to
                     newest; false to list the messages in order from newest
                     to oldest.  The default is true.
**/
   public void setListDirection(boolean listDirection)
   {
      try {
          rMessageQueue_.setSelectionValue(RMessageQueue.LIST_DIRECTION,
             listDirection ? RMessageQueue.NEXT : RMessageQueue.PREVIOUS);
          rMessageQueue_.setSelectionValue(RMessageQueue.STARTING_USER_MESSAGE_KEY,
             listDirection ? RMessageQueue.OLDEST : RMessageQueue.NEWEST);
          rMessageQueue_.setSelectionValue(RMessageQueue.STARTING_WORKSTATION_MESSAGE_KEY,
             listDirection ? RMessageQueue.OLDEST : RMessageQueue.NEWEST);
          listDirection_ = listDirection;
      }
      catch(ResourceException e) {
       if (Trace.isTraceOn())
             Trace.log(Trace.ERROR, "Error setting list direction", e);
      }
   }



/**
Sets the fully qualified integrated file system path name of the
message queue.  The default is CURRENT. The path cannot be changed
if the MessageQueue object has established a connection to the AS/400.

@param path The fully qualified integrated file system path name of the
            message queue, or CURRENT to refer to the user's default
            message queue.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setPath(String path)
        throws PropertyVetoException
    {
        rMessageQueue_.setPath(path);
    }



/**
Sets the selection that describes which messages are returned.
The default is ALL. This takes effect the next time the list
of queue messages is retrieved or refreshed.

@param selection The selection.  Valid values are:
                 <ul>
                 <li>{@link #ALL ALL}
                 <li>{@link #MESSAGES_NEED_REPLY MESSAGES_NEED_REPLY}
                 <li>{@link #MESSAGES_NO_NEED_REPLY MESSAGES_NO_NEED_REPLY}
                 <li>{@link #SENDERS_COPY_NEED_REPLY SENDERS_COPY_NEED_REPLY}
                 </ul>

@exception PropertyVetoException If the change is vetoed.

@see com.ibm.as400.resource.RMessageQueue#SELECTION_CRITERIA
**/
    public void setSelection(String selection)
        throws PropertyVetoException
    {
        if (selection == null)
            throw new NullPointerException("selection");

        String oldValue = getSelection();
        String newValue = selection;
        vetoableChangeSupport_.fireVetoableChange("selection", oldValue, newValue);
        setSelectionValueAsString(RMessageQueue.SELECTION_CRITERIA, selection);
        propertyChangeSupport_.firePropertyChange("selection", oldValue, newValue);
    }



/*-------------------------------------------------------------------------
Convenience method for setting selection values.
All ResourceExceptions are swallowed!
-------------------------------------------------------------------------*/
    private void setSelectionValueAsInt(Object selectionID, int value)
    {
        try {
            rMessageQueue_.setSelectionValue(selectionID, new Integer(value));
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error setting selection value", e);
        }
    }


    private void setSelectionValueAsString(Object selectionID, Object value)
    {
        try {
            rMessageQueue_.setSelectionValue(selectionID, value);
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error setting selection value", e);
        }
    }



/**
Sets the severity of the messages which are returned.
All messages of the specified severity and greater are returned.
The default is 0.  This takes effect the next time that the list
of queued messages is retreived or refreshed.

@param severity The severity of the messages to be returned.  The
                value must be between 0 and 99, inclusive.

@exception PropertyVetoException            If the change is vetoed.

@see com.ibm.as400.resource.RMessageQueue#SEVERITY_CRITERIA
**/
    public void setSeverity(int severity)
        throws PropertyVetoException
    {
        if ((severity < 0) || (severity > 99))
            throw new ExtendedIllegalArgumentException("severity", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        Integer oldValue = new Integer(getSeverity());
        Integer newValue = new Integer(severity);
        vetoableChangeSupport_.fireVetoableChange("severity", oldValue, newValue);
        setSelectionValueAsInt(RMessageQueue.SEVERITY_CRITERIA, severity);
        propertyChangeSupport_.firePropertyChange("severity", oldValue, newValue);
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
        rMessageQueue_.setSystem(system);
    }



/**
Converts the Enumeration (whose elements are RQueuedMessage objects)
to an Enumeration whose elements are QueuedMessage objects.
**/
    private class EnumerationAdapter implements Enumeration                                     // @D2C
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
           return new QueuedMessage((RQueuedMessage)rEnum_.nextElement(), MessageQueue.this);    // @D2C
       }
    }



 }




