///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RMessageQueue.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.CharConverter;
import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.as400.access.Trace;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;
import java.beans.PropertyVetoException;
import java.io.UnsupportedEncodingException;



/**
The RMessageQueue class represents an AS/400 message queue.  If no message
queue path is set, then the default is {@link #CURRENT CURRENT},
which represents the current user's message queue,
<blockquote>/QSYS.LIB/QUSRSYS.LIB/<em>userID</em>.MSGQ</blockquote>.

<a name="selectionIDs"><p>The following selection IDs are supported:
<ul>
<li>{@link #FORMATTING_CHARACTERS FORMATTING_CHARACTERS}
<li>{@link #LIST_DIRECTION LIST_DIRECTION}
<li>{@link #REPLACEMENT_DATA REPLACEMENT_DATA}
<li>{@link #SELECTION_CRITERIA SELECTION_CRITERIA}
<li>{@link #SEVERITY_CRITERIA SEVERITY_CRITERIA}
<li>{@link #SORT_CRITERIA SORT_CRITERIA}
<li>{@link #STARTING_USER_MESSAGE_KEY STARTING_USER_MESSAGE_KEY}
<li>{@link #STARTING_WORKSTATION_MESSAGE_KEY STARTING_WORKSTATION_MESSAGE_KEY}
</ul>
</a>

<p>Use one or more of these selection IDs with
{@link com.ibm.as400.resource.ResourceList#getSelectionValue getSelectionValue()}
and {@link com.ibm.as400.resource.ResourceList#setSelectionValue setSelectionValue()}
to access the selection values for an RMessageQueue.

<p>RMessageQueue objects generate {@link com.ibm.as400.resource.RQueuedMessage RQueuedMessage}
objects.  RQueuedMessage objects have many <a href="{@docRoot}/com/ibm/as400/resource/RQueuedMessage.html#attributeIDs">
attributes</a>.  Only some of these attribute values are set, depending on how an RQueuedMessage
object is created.  The following is a list of attribute IDs whose values are set on RQueuedMessage
objects returned in a list of messages:
<ul>
<li>{@link com.ibm.as400.resource.RQueuedMessage#DATE_SENT DATE_SENT }
<li>{@link com.ibm.as400.resource.RQueuedMessage#DEFAULT_REPLY DEFAULT_REPLY }
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_FILE MESSAGE_FILE }
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_HELP MESSAGE_HELP }
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_ID MESSAGE_ID }
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_KEY MESSAGE_KEY }
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_QUEUE MESSAGE_QUEUE  }
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_SEVERITY MESSAGE_SEVERITY }
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_TEXT MESSAGE_TEXT }
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_TYPE MESSAGE_TYPE }
<li>{@link com.ibm.as400.resource.RQueuedMessage#REPLY_STATUS REPLY_STATUS }
<li>{@link com.ibm.as400.resource.RQueuedMessage#SENDER_JOB_NAME SENDER_JOB_NAME }
<li>{@link com.ibm.as400.resource.RQueuedMessage#SENDER_JOB_NUMBER SENDER_JOB_NUMBER }
<li>{@link com.ibm.as400.resource.RQueuedMessage#SENDER_USER_NAME SENDER_USER_NAME   }
<li>{@link com.ibm.as400.resource.RQueuedMessage#SENDING_PROGRAM_NAME SENDING_PROGRAM_NAME }
</ul>

<a name="receiveIDs">
<p>The following is a list of attribute IDs whose values are set on
objects returned by {@link #receive receive()}:
<ul>
<li>{@link com.ibm.as400.resource.RQueuedMessage#ALERT_OPTION ALERT_OPTION }
<li>{@link com.ibm.as400.resource.RQueuedMessage#DATE_SENT DATE_SENT  }
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_FILE MESSAGE_FILE }
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_HELP MESSAGE_HELP }
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_ID MESSAGE_ID  }
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_KEY MESSAGE_KEY }
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_SEVERITY MESSAGE_SEVERITY }
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_TEXT MESSAGE_TEXT }
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_TYPE MESSAGE_TYPE }
<li>{@link com.ibm.as400.resource.RQueuedMessage#SENDER_JOB_NAME SENDER_JOB_NAME }
<li>{@link com.ibm.as400.resource.RQueuedMessage#SENDER_JOB_NUMBER SENDER_JOB_NUMBER }
<li>{@link com.ibm.as400.resource.RQueuedMessage#SENDER_USER_NAME SENDER_USER_NAME }
<li>{@link com.ibm.as400.resource.RQueuedMessage#SENDING_PROGRAM_NAME SENDING_PROGRAM_NAME }
<li>{@link com.ibm.as400.resource.RQueuedMessage#SUBSTITUTION_DATA  SUBSTITUTION_DATA }
</ul>
</a>

<blockquote><pre>
// Create an RMessageQueue object to represent a specific message queue.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RMessageQueue messageQueue = new RMessageQueue(system, "/QSYS.LIB/MYLIB.LIB/MYMSGQ.MSG");
<br>
// Set the selection so that the list of messages includes
// only messages that need a reply.
messageQueue.setSelectionValue(RMessageQueue.SELECTION_CRITERIA, RMessageQueue.MESSAGES_NEED_REPLY);
<br>
// Open the list and wait for it to complete.
messageQueue.open();
messageQueue.waitForComplete();
<br>
// Read and print the messages in the list.
long numberOfMessages = messageQueue.getListLength();
for(long i = 0; i &lt; numberOfMessages; ++i)
{
    RQueuedMessage queuedMessage = (RQueuedMessage)messageQueue.resourceAt(i);
    System.out.println(queuedMessage.getAttributeValue(RQueuedMessage.MESSAGE_TEXT));
}
<br>
// Close the list.
messageQueue.close();
</pre></blockquote>

@deprecated Use
{@link com.ibm.as400.access.MessageQueue MessageQueue} instead, as this package may be removed in the future.
@see RQueuedMessage
**/
public class RMessageQueue
extends SystemResourceList
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;



//-----------------------------------------------------------------------------------------
// Presentation.
//-----------------------------------------------------------------------------------------

    private static final String                 PRESENTATION_KEY_           = "MESSAGE_QUEUE";
    private static final String                 ICON_BASE_NAME_             = "RMessageQueue";
    private static PresentationLoader           presentationLoader_         = new PresentationLoader("com.ibm.as400.resource.ResourceMRI");



//-----------------------------------------------------------------------------------------
// Message types.
//-----------------------------------------------------------------------------------------

/**
Constant referring to all messages in the message queue.
**/
    public final static String ALL                     = "*ALL";

/**
Constant referring to any message in the message queue.
**/
    public final static String ANY                     = "*ANY";

/**
Constant referring to a message identified by a key.
**/
    public final static String BYKEY                   = "*BYKEY";

/**
Constant referring to completion messages.
**/
    public final static String COMPLETION              = "*COMP";

/**
Constant referring to the sender's copy of a previously sent
inquiry message.
**/
    public final static String COPY                    = "*COPY";

/**
Constant referring to the current user's message queue.
**/
    public final static String CURRENT                 = "*CURRENT";

/**
Constant referring to diagnostic messages.
**/
    public final static String DIAGNOSTIC              = "*DIAG";

/**
Constant referring to the first message in the message queue.
**/
    public final static String FIRST                   = "*FIRST";

/**
Constant referring to informational messages.
**/
    public final static String INFORMATIONAL           = "*INFO";

/**
Constant referring to inquiry messages.
**/
    public final static String INQUIRY                 = "*INQ";

/**
Constant referring to all messages in the message queue
except unanswered inquiry and unanswered senders' copy messages.
**/
    public final static String KEEP_UNANSWERED         = "*KEEPUNANS";

/**
Constant referring to the last message in the message queue.
**/
    public final static String LAST                    = "*LAST";

/**
Constant referring to messages that need a reply.
**/
    public final static String MESSAGES_NEED_REPLY     = "*MNR";

/**
Constant referring to messages that do not need a reply.
**/
    public final static String MESSAGES_NO_NEED_REPLY  = "*MNNR";

/**
Constant referring to all new messages in the message queue.
New messages are those that have not been received.
**/
    public final static String NEW                     = "*NEW";

/**
Constant referring to the message key for the newest message in the queue.
**/
    public final static byte[] NEWEST                  = new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };

/**
Constant referring to the next message in the message queue.
**/
    public final static String NEXT                    = "*NEXT";

/**
Constant referring to all old messages in the message queue.
Old messages are those that have already been received.
**/
    public final static String OLD                     = "*OLD";

/**
Constant referring to the message key for the oldest message in the queue.
**/
    public final static byte[] OLDEST                  = new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 };

/**
Constant referring to the previous message in the message queue.
**/
    public final static String PREVIOUS                = "*PRV";

/**
Constant indicating that the message should be removed from
the message queue.
**/
    public final static String REMOVE                  = "*REMOVE";

/**
Constant referring to the reply to an inquiry message.
**/
    public final static String REPLY                   = "*RPY";      // @C1

/**
Constant indicating that the message should remain in the
message queue without changing its new or old designation.
**/
    public final static String SAME                    = "*SAME";

/**
Constant referring to the sender's copies of messages that
need replies.
**/
    public final static String SENDERS_COPY_NEED_REPLY = "*SCNR";



//-----------------------------------------------------------------------------------------
// Message types.
//-----------------------------------------------------------------------------------------

/**
Constant indicating that message help text is not formatted.
**/
    public final static Integer NO_FORMATTING                     = new Integer(com.ibm.as400.access.MessageFile.NO_FORMATTING);

/**
Constant indicating that message help text includes formatting characters.
**/
    public final static Integer RETURN_FORMATTING_CHARACTERS                     = new Integer(com.ibm.as400.access.MessageFile.RETURN_FORMATTING_CHARACTERS);

/**
Constant indicating that message help text is formatted.
**/
    public final static Integer SUBSTITUTE_FORMATTING_CHARACTERS                     = new Integer(com.ibm.as400.access.MessageFile.SUBSTITUTE_FORMATTING_CHARACTERS);



//-----------------------------------------------------------------------------------------
// Selection IDs.
//
// * If you add a selection here, make sure and add it to the class javadoc
//   and in ResourceMRI.java.
//-----------------------------------------------------------------------------------------

    private static ResourceMetaDataTable selections_            = new ResourceMetaDataTable(presentationLoader_, PRESENTATION_KEY_);
    private static ProgramMap            selectionMap_          = new ProgramMap();

/**
Selection ID for formatting characters.  This identifies a Integer selection,
which represents the type of message help text formatting.  Possible values are:
<ul>
<li>{@link #NO_FORMATTING NO_FORMATTING}
    - Message help text is not formatted.
<li>{@link #RETURN_FORMATTING_CHARACTERS RETURN_FORMATTING_CHARACTERS}
    - Message help text includes formatting characters.
<li>{@link #SUBSTITUTE_FORMATTING_CHARACTERS SUBSTITUTE_FORMATTING_CHARACTERS}
    - Message help text is formatted.
</ul>
The default is NO_FORMATTING.
**/
    public static final String FORMATTING_CHARACTERS                = "FORMATTING_CHARACTERS";

    static {
        selections_.add(FORMATTING_CHARACTERS, Integer.class, false,
                        new Integer[] { NO_FORMATTING,
                            RETURN_FORMATTING_CHARACTERS,
                            SUBSTITUTE_FORMATTING_CHARACTERS }, NO_FORMATTING, true);
    }

/**
Selection ID for list direction.  This identifies a String selection,
which represents the direction to list messages relative to the values
specified for the {@link #STARTING_USER_MESSAGE_KEY STARTING_USER_MESSAGE_KEY}
and {@link #STARTING_WORKSTATION_MESSAGE_KEY STARTING_WORKSTATION_MESSAGE_KEY}
selections.  Possible values are:
<ul>
<li>{@link #NEXT NEXT}
    - Returns messages that are newer than the messages specified for
      the STARTING_USER_MESSAGE_KEY and STARTING_WORKSTATION_MESSAGE_KEY
      selections.
<li>{@link #PREVIOUS PREVIOUS}
    - Returns messages that are older than the messages specified for
      the STARTING_USER_MESSAGE_KEY and STARTING_WORKSTATION_MESSAGE_KEY
      selections.
</ul>
The default is NEXT.
**/
    public static final String LIST_DIRECTION                        = "LIST_DIRECTION";

    static {
        selections_.add(LIST_DIRECTION, String.class, false,
                        new String[] { NEXT, PREVIOUS }, NEXT, true);
        selectionMap_.add(LIST_DIRECTION, null, "messageSelectionInformation.listDirection");
    }

/**
Selection ID for replacement data.  This identifies a Boolean selection,
which indicates whether to replace substitution data in message text.
The default is true.
**/
    public static final String REPLACEMENT_DATA                        = "REPLACEMENT_DATA";

    static {
        selections_.add(REPLACEMENT_DATA, Boolean.class, false,
                        null, Boolean.TRUE, false);
    }

/**
Selection ID for selection criteria.  This identifies a String selection,
which represents the type of messages to be listed.  Possible values are:
<ul>
<li>{@link #ALL ALL}
    - All messages are listed.
<li>{@link #MESSAGES_NEED_REPLY MESSAGES_NEED_REPLY}
    - Only messages that need a reply are listed.
<li>{@link #SENDERS_COPY_NEED_REPLY SENDERS_COPY_NEED_REPLY}
    - Only the sender's copy messages that need a reply are listed.
<li>{@link #MESSAGES_NO_NEED_REPLY MESSAGES_NO_NEED_REPLY}
    - Only messages that do not need a reply are listed.
</ul>
The default is ALL.
**/
    public static final String SELECTION_CRITERIA                = "SELECTION_CRITERIA";

    static {
        selections_.add(SELECTION_CRITERIA, String.class, false,
                        new String[] { ALL,
                            MESSAGES_NEED_REPLY,
                            SENDERS_COPY_NEED_REPLY,
                            MESSAGES_NO_NEED_REPLY }, ALL, true);
        selectionMap_.add(SELECTION_CRITERIA, null, "messageSelectionInformation.selectionCriteria");
    }

/**
Selection ID for severity criteria.  This identifies an Integer selection,
which represents the minimum severity of a message to be included in the
list.  The value must be in the range 0 to 99.   The default is 0.
**/
    public static final String SEVERITY_CRITERIA                 = "SEVERITY_CRITERIA";

    static {
        selections_.add(SEVERITY_CRITERIA, Integer.class, false,
                        null, new Integer(0), false);
        selectionMap_.add(SEVERITY_CRITERIA, null, "messageSelectionInformation.severityCriteria");
    }

/**
Selection ID for sort criteria.  This identifies a Boolean selection,
which indicates whether the list should be sorted by type if the
{@link #SELECTION_CRITERIA SELECTION_CRITERIA} selection is
set to {@link #ALL ALL}.  The default is false.
**/
    public static final String SORT_CRITERIA                        = "SORT_CRITERIA";

    static {
        selections_.add(SORT_CRITERIA, Boolean.class, false,
                        null, Boolean.FALSE, false);
        selectionMap_.add(SORT_CRITERIA, null, "sortInformation", new BooleanValueMap("0", "1"));
    }

/**
Selection ID for starting user message key.  This identifies a byte[] selection,
which represents the message key used to begin searching for messages
to list from the corresponding entry in the message queue.  If the message queue
path name is set to {@link #CURRENT CURRENT}, then this selection represents
the starting message key for the current user's user message queue.  Possible
values are:
<ul>
<li>{@link #OLDEST OLDEST}
    - The first message to be returned is the oldest message in the queue.
<li>{@link #NEWEST NEWEST}
    - The first message to be returned is the newest message in the queue.
<li>Any valid message key.
</ul>
The default is OLDEST.

<p>If a value other than {@link #OLDEST OLDEST} or {@link #NEWEST NEWEST}
is specified and a message with that key does not exist, an exception is thrown.
If the key of a reply message is specified, the message search begins with the
inquiry or sender's copy message that the reply with associated with, not the reply
message itself.
**/
    public static final String STARTING_USER_MESSAGE_KEY                = "STARTING_USER_MESSAGE_KEY";

    static {
        selections_.add(STARTING_USER_MESSAGE_KEY, byte[].class, false,
                        new byte[][] { NEWEST, OLDEST }, OLDEST, false);
        selectionMap_.add(STARTING_USER_MESSAGE_KEY, null, "messageSelectionInformation.startingMessageKeys", new int[] { 0 });
    }

/**
Selection ID for starting workstation message key.  This identifies a byte[] selection,
which represents the message key used to begin searching for messages
to list from the corresponding entry in the message queue.    If the message queue
path name is set to {@link #CURRENT CURRENT}, then this selection represents
the starting message key for the current user's workstation message queue.  Otherwise,
this selection has no effect.  Possible values are:
<ul>
<li>{@link #OLDEST OLDEST}
    - The first message to be returned is the oldest message in the queue.
<li>{@link #NEWEST NEWEST}
    - The first message to be returned is the newest message in the queue.
<li>Any valid message key.
</ul>
The default is OLDEST.

<p>If a value other than {@link #OLDEST OLDEST} or {@link #NEWEST NEWEST}
is specified and a message with that key does not exist, an exception is thrown.
If the key of a reply message is specified, the message search begins with the
inquiry or sender's copy message that the reply with associated with, not the reply
message itself.
**/
    public static final String STARTING_WORKSTATION_MESSAGE_KEY        = "STARTING_WORKSTATION_MESSAGE_KEY";

    static {
        selections_.add(STARTING_WORKSTATION_MESSAGE_KEY, byte[].class, false,
                        new byte[][] { NEWEST, OLDEST }, OLDEST, false);
        selectionMap_.add(STARTING_WORKSTATION_MESSAGE_KEY, null, "messageSelectionInformation.startingMessageKeys", new int[] { 1 });
    }



//-----------------------------------------------------------------------------------------
// Open list attribute map.
//
// * If you add an attribute here, make sure and add it to the class javadoc.
//-----------------------------------------------------------------------------------------

    private static ProgramMap            openListAttributeMap_  = new ProgramMap();
    private static final String          openListProgramName_   = "qgyolmsg";

    static {
        openListAttributeMap_.add(RQueuedMessage.MESSAGE_SEVERITY, null, "receiverVariable.messageSeverity");
        openListAttributeMap_.add(RQueuedMessage.MESSAGE_ID, null, "receiverVariable.messageIdentifier");
        openListAttributeMap_.add(RQueuedMessage.MESSAGE_TYPE, null, "receiverVariable.messageType", new IntegerValueMap());
        openListAttributeMap_.add(RQueuedMessage.MESSAGE_KEY, null, "receiverVariable.messageKey");
        openListAttributeMap_.add(RQueuedMessage.MESSAGE_QUEUE, null, "receiverVariable.messageQueue", new QualifiedValueMap(QualifiedValueMap.FORMAT_20, "MSGQ"));
        openListAttributeMap_.add(RQueuedMessage.MESSAGE_FILE, null, "receiverVariable.messageFile", new QualifiedValueMap(QualifiedValueMap.FORMAT_20, "MSGF")); // @A2A
        openListAttributeMap_.add(RQueuedMessage.DATE_SENT, null, "receiverVariable.dateAndTimeSent", new DateValueMap(DateValueMap.FORMAT_13));
        openListAttributeMap_.add(RQueuedMessage.MESSAGE_TEXT, null, "receiverVariable.message.data");
        openListAttributeMap_.add(RQueuedMessage.SENDER_JOB_NAME, null, "receiverVariable.qualifiedSenderJobName.data", new SubstringValueMap(0, 10, true));
        openListAttributeMap_.add(RQueuedMessage.SENDER_JOB_NUMBER, null, "receiverVariable.qualifiedSenderJobName.data", new SubstringValueMap(20, 6, true));
        openListAttributeMap_.add(RQueuedMessage.SENDER_USER_NAME, null, "receiverVariable.qualifiedSenderJobName.data", new SubstringValueMap(10, 10, true));
        openListAttributeMap_.add(RQueuedMessage.SENDING_PROGRAM_NAME, null, "receiverVariable.sendingProgramName.data");
        openListAttributeMap_.add(RQueuedMessage.REPLY_STATUS, null, "receiverVariable.replyStatus.data");
        openListAttributeMap_.add(RQueuedMessage.DEFAULT_REPLY, null, "receiverVariable.defaultReply.data");
        openListAttributeMap_.add(RQueuedMessage.MESSAGE_HELP, null, "receiverVariable.messageHelp.data");
    }




//-----------------------------------------------------------------------------------------
// Receive attribute map.
//
// * If you add an attribute here, make sure and add it to the class javadoc.
//-----------------------------------------------------------------------------------------

    private static final String         receiveProgramName_     = "qmhrcvm";
    private static ProgramMap           receiveAttributeMap_    = new ProgramMap();

    static {
        receiveAttributeMap_.add(RQueuedMessage.MESSAGE_SEVERITY, null, "messageInformation.messageSeverity");
        receiveAttributeMap_.add(RQueuedMessage.MESSAGE_ID, null, "messageInformation.messageID");
        receiveAttributeMap_.add(RQueuedMessage.MESSAGE_TYPE, null, "messageInformation.messageType", new IntegerValueMap());
        receiveAttributeMap_.add(RQueuedMessage.MESSAGE_KEY, null, "messageInformation.messageKey");
        receiveAttributeMap_.add(RQueuedMessage.MESSAGE_FILE, null, "messageInformation.messageFile", new QualifiedValueMap(QualifiedValueMap.FORMAT_20, "MSGF"));
        receiveAttributeMap_.add(RQueuedMessage.SENDER_JOB_NAME, null, "messageInformation.sendingJob");
        receiveAttributeMap_.add(RQueuedMessage.SENDER_JOB_NUMBER, null, "messageInformation.sendingJobsNumber");
        receiveAttributeMap_.add(RQueuedMessage.SENDER_USER_NAME, null, "messageInformation.sendingUserProfile");
        receiveAttributeMap_.add(RQueuedMessage.SENDING_PROGRAM_NAME, null, "messageInformation.sendingProgramName");
        receiveAttributeMap_.add(RQueuedMessage.DATE_SENT, null, "messageInformation.dateAndTimeSent", new DateValueMap(DateValueMap.FORMAT_13));
        receiveAttributeMap_.add(RQueuedMessage.ALERT_OPTION, null, "messageInformation.alertOption");
        receiveAttributeMap_.add(RQueuedMessage.MESSAGE_HELP, null, "messageInformation.messageHelp");
    }



//-----------------------------------------------------------------------------------------
// PCML document initialization.
//-----------------------------------------------------------------------------------------

    private static final String             DOCUMENT_NAME_              = "com.ibm.as400.resource.RMessageQueue";
    private static final String             messageFieldIdentifier_     = "qgyolmsg.messageSelectionInformation.messageFieldIdentifier";
    private static final String             messageHelpFieldIdentifier_ = "qgyolmsg.messageSelectionInformation.messageHelpFieldIdentifier";

    private static ProgramCallDocument      staticDocument_             = null;

    static {
        // Create a static version of the PCML document, then clone it for each document.
        // This will improve performance, since we will only have to deserialize the PCML
        // object once.
        try {
            staticDocument_ = new ProgramCallDocument();
            staticDocument_.setDocument(DOCUMENT_NAME_);
        }
        catch(PcmlException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error instantiating ProgramCallDocument", e);
        }
    }


//-----------------------------------------------------------------------------------------
// Private data.
//-----------------------------------------------------------------------------------------

    private static final byte[]                 BLANK_KEY                   = new byte[] { 0x40, 0x40, 0x40, 0x40 };
    private static final String                 QUSRSYS                     = "QUSRSYS";
    private ProgramCallDocument                 document_                   = null;

    private String                              path_                       = null;



//-----------------------------------------------------------------------------------------
// Code.
//-----------------------------------------------------------------------------------------

/**
Constructs an RMessageQueue object.
**/
    public RMessageQueue()
    {
        super(presentationLoader_.getPresentationWithIcon(PRESENTATION_KEY_, ICON_BASE_NAME_),
              RQueuedMessage.attributes_,
              selections_,
              null,
              openListProgramName_,
              null,
              selectionMap_);

        // Initialize the path to CURRENT.
        try {
            setPath(CURRENT);
        }
        catch(PropertyVetoException e) {
            // Ignore.
        }
    }



/**
Constructs an RMessageQueue object.

@param  system  The system.
**/
    public RMessageQueue(AS400 system)
    {
        this();

        try {
            setSystem(system);
        }
        catch(PropertyVetoException e) {
            // Ignore.
        }
    }



/**
Constructs an RMessageQueue object.

@param  system  The system.
@param  path    The fully qualified integrated file system path name
                of the message queue, or {@link #CURRENT CURRENT} to refer to the user's
                default message queue.
**/
    public RMessageQueue(AS400 system, String path)
    {
        this();

        try {
            setSystem(system);
            setPath(path);
        }
        catch(PropertyVetoException e) {
            // Ignore.
        }
    }


/**
Establishes the connection to the AS/400.

<p>The method is called by the resource framework automatically
when the connection needs to be established.

@exception ResourceException                If an error occurs.
**/
    protected void establishConnection()
    throws ResourceException
    {
        if (path_ == null)
            throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        // Call the superclass.
        super.establishConnection();

        // Initialize the PCML document.
        document_ = (ProgramCallDocument)staticDocument_.clone();
        setDocument(document_);
        try {
            document_.setValue("qgyolmsg.userOrQueueInformation.userOrQueueIndicator", (path_.equals(CURRENT) ? "0" : "1"));
            formatQualifiedMessageQueueName(document_, "qgyolmsg.userOrQueueInformation.userOrQueueName");
        }
        catch(PcmlException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error setting path information in PCML document", e);
        }
    }



/**
Formats the qualified message name into a PCML document.

@param document The PCML document.
@param name     The name of the data element.

@exception PcmlException    If a PCML exception occurs.
**/
    private void formatQualifiedMessageQueueName(ProgramCallDocument document, String name)
        throws PcmlException
    {
        if (path_.equals(CURRENT)) {
            AS400 system = getSystem();
            if (system != null) {
                String userId = system.getUserId();
                document.setValue(name + ".objectName", userId);
                document.setValue(name + ".libraryName", QUSRSYS);
            }
        }
        else {
            QSYSObjectPathName fullPath = new QSYSObjectPathName(path_);
            document.setValue(name + ".objectName", fullPath.getObjectName());
            document.setValue(name + ".libraryName", fullPath.getLibraryName());
        }
    }



/**
Returns the fully qualified integrated file system path name
of the message queue, or {@link #CURRENT CURRENT} to refer to the user's default
message queue.

@return The fully qualified integrated file system path name of the
        message queue, or {@link #CURRENT CURRENT} to refer to the user's default
        message queue.
**/
    public String getPath()
    {
        return path_;
    }




/**
Receives a message from the message queue by key.  This method
receives a message of any type except sender's copy.  The message is removed
from the message queue.  See the <a href="#receiveIDs">list of RQueuedMessage
attribute values</a> which are set on a received message.

@param messageKey   The message key.
@return             The queued message, or null if the message can not
                    be received.

@exception ResourceException                If an error occurs.

@see RQueuedMessage#MESSAGE_KEY
**/
    public RQueuedMessage receive(byte[] messageKey)
        throws ResourceException
    {
        return receive(messageKey, 0, REMOVE, ANY);
    }



/**
Receives a message from the message queue.  See the <a href="#receiveIDs">list of RQueuedMessage
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

@exception ResourceException                If an error occurs.

@see RQueuedMessage#MESSAGE_KEY
**/
    public RQueuedMessage receive(byte[] messageKey,
                                  int waitTime,
                                  String messageAction,
                                  String messageType)
        throws ResourceException
    {
        // Validate the parameters.
        if (messageAction == null)
            throw new NullPointerException("messageAction");
        if ((!messageAction.equals(OLD))
             && (!messageAction.equals(REMOVE))
             && (!messageAction.equals(SAME)))
            throw new ExtendedIllegalArgumentException("messageAction", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        if (messageType == null)
            throw new NullPointerException("messageType");
        if ((!messageType.equals(ANY))
            && (!messageType.equals(COMPLETION))
            && (!messageType.equals(COPY))
            && (!messageType.equals(DIAGNOSTIC))
            && (!messageType.equals(FIRST))
            && (!messageType.equals(INFORMATIONAL))
            && (!messageType.equals(INQUIRY))
            && (!messageType.equals(LAST))
            && (!messageType.equals(NEXT))
            && (!messageType.equals(PREVIOUS))
            && (!messageType.equals(REPLY)))
            throw new ExtendedIllegalArgumentException("messageType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        if ((messageType.equals(COPY)
             || messageType.equals(NEXT)
             || messageType.equals(PREVIOUS))
            && (messageKey == null))
            throw new ExtendedIllegalArgumentException("messageKey", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        if ((messageType.equals(FIRST)
             || messageType.equals(LAST))
            && (messageKey != null))
            throw new ExtendedIllegalArgumentException("messageKey", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        // Establish the connection if needed.
        if (!isConnectionEstablished())
            establishConnection();

        // Set the input parameters and call the API.
        try {
            formatQualifiedMessageQueueName(document_, receiveProgramName_ + ".qualifiedMessageQueueName");
            document_.setValue(receiveProgramName_ + ".messageType", messageType);
            document_.setValue(receiveProgramName_ + ".messageKey", (messageKey == null) ? BLANK_KEY : messageKey);
            document_.setIntValue(receiveProgramName_ + ".waitTime", waitTime);
            document_.setValue(receiveProgramName_ + ".messageAction", messageAction);
            if (document_.callProgram(receiveProgramName_) == false)
                throw new ResourceException(document_.getMessageList(receiveProgramName_));

            // If the bytes returned is 8, then the message can not
            // be received.
            if (document_.getIntValue(receiveProgramName_ + ".messageInformation.bytesReturned") <= 8) {
                if (Trace.isTraceOn())
                    Trace.log(Trace.INFORMATION, "Queued message can not be received.");
                return null;
            }

            // Create the RQueuedMessage object.
            AS400 system = getSystem();
            RQueuedMessage rQueuedMessage = new RQueuedMessage();
            try { rQueuedMessage.setSystem(system); } catch (PropertyVetoException pve) {}   // @D6a
            Object[] attributeIDs = receiveAttributeMap_.getIDs();
            Object[] values = receiveAttributeMap_.getValues(attributeIDs, system, document_, receiveProgramName_, null);
            for(int j = 0; j < values.length; ++j)
                rQueuedMessage.initializeAttributeValue(attributeIDs[j], values[j]);

            // Text and substitution data aren't so straightforward.  If the
            // message id is there, then message text and substitution data are
            // what they say they are.  If there is no message id, then what comes
            // back as substitution data is really impromptu message text.
            String messageText = (String)document_.getValue(receiveProgramName_ + ".messageInformation.message");
            byte[] substitutionData = (byte[])document_.getValue(receiveProgramName_ + ".messageInformation.replacementDataOrImpromptuText");
            if (((String)rQueuedMessage.getAttributeValue(RQueuedMessage.MESSAGE_ID)).length() > 0) {
                rQueuedMessage.initializeAttributeValue(RQueuedMessage.MESSAGE_TEXT, messageText);
                rQueuedMessage.initializeAttributeValue(RQueuedMessage.SUBSTITUTION_DATA, substitutionData);
            }
            else {
                CharConverter converter = new CharConverter(system.getCcsid(), system);
                rQueuedMessage.initializeAttributeValue(RQueuedMessage.MESSAGE_TEXT, converter.byteArrayToString(substitutionData));
                rQueuedMessage.initializeAttributeValue(RQueuedMessage.SUBSTITUTION_DATA, substitutionData);
            }

            Object resourceKey = RQueuedMessage.computeResourceKey(system, path_, (byte[])rQueuedMessage.getAttributeValue(RQueuedMessage.MESSAGE_KEY));
            rQueuedMessage.setResourceKey(resourceKey);
            return rQueuedMessage;
        }
        catch(PcmlException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error when receiving message", e);
            throw new ResourceException(e);
        }
        catch(UnsupportedEncodingException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error when receiving message", e);
            throw new ResourceException(e);
        }
  }



/**
Remove all messages from the message queue.

@exception ResourceException                If an error occurs.
**/
    public void remove()
        throws ResourceException
    {
        remove(BLANK_KEY, ALL);
    }



/**
Removes a message from the message queue.

@param messageKey   The message key.

@exception ResourceException                If an error occurs.
**/
    public void remove(byte[] messageKey)
        throws ResourceException
    {
        remove(messageKey, BYKEY);
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

@exception ResourceException                If an error occurs.
**/
    public void remove(String messageType)
        throws ResourceException
    {
        remove(BLANK_KEY, messageType);
    }



/**
Remove messages from the message queue.

@param messageKey   The message key.
@param messageType  The type of message to remove.  Valid values are:
                    <ul>
                    <li>ALL -
                        All messages in the message queue.
                    <li>BYKEY -
                        The single message specified by the message key.
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

@exception ResourceException                If an error occurs.
**/
    void remove(byte[] messageKey, String messageType)
        throws ResourceException
    {
        // Validate the parameters.
        if (messageKey == null)
            throw new NullPointerException("messageKey");

        if (messageType == null)
            throw new NullPointerException("messageType");
        if ((!messageType.equals(ALL))
            && (!messageType.equals(BYKEY))
            && (!messageType.equals(KEEP_UNANSWERED))
            && (!messageType.equals(NEW))
            && (!messageType.equals(OLD)))
            throw new ExtendedIllegalArgumentException("messageType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        // Establish the connection if needed.
        if (!isConnectionEstablished())
            establishConnection();

        // Set the input parameters and call the API.
        try {
            formatQualifiedMessageQueueName(document_, "qmhrmvm.qualifiedMessageQueueName");
            document_.setValue("qmhrmvm.messageKey", messageKey);
            document_.setValue("qmhrmvm.messagesToRemove", messageType);
            if (document_.callProgram("qmhrmvm") == false)
                throw new ResourceException(document_.getMessageList("qmhrmvm"));
        }
        catch (PcmlException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error when removing message", e);
            throw new ResourceException(e);
        }
    }



/**
Replies to and removes a message.

@param messageKey   The message key.
@param replyText    The reply.

@exception ResourceException                If an error occurs.
**/
    public void reply(byte[] messageKey, String replyText)
        throws ResourceException
    {
        reply(messageKey, replyText, true);
    }



/**
Replies to a message.

@param messageKey   The message key.
@param replyText    The reply.
@param remove       true to remove the inquiry message and the reply from the
                    message queue after the reply is sent, false to keep the
                    inquiry message and the reply after the reply is sent.

@exception ResourceException                If an error occurs.
**/
    public void reply(byte[] messageKey, String replyText, boolean remove)
        throws ResourceException
    {
        // Validate the parameters.
        if (messageKey == null)
            throw new NullPointerException("messageKey");
        if (replyText == null)
            throw new NullPointerException("replyText");

        // Establish the connection if needed.
        if (!isConnectionEstablished())
            establishConnection();

        // Set the input parameters and call the API.
        try {
            document_.setValue("qmhsndrm.messageKey", messageKey);
            formatQualifiedMessageQueueName(document_, "qmhsndrm.qualifiedMessageQueueName");
            byte[] asBytes = CharConverter.stringToByteArray(getSystem(), replyText);   // @A1A
            document_.setIntValue("qmhsndrm.lengthOfReplyText", asBytes.length);        // @A1C
            document_.setValue("qmhsndrm.replyText", asBytes);                          // @A1C
            document_.setValue("qmhsndrm.removeMessage", remove ? "*YES" : "*NO");
            if (document_.callProgram("qmhsndrm") == false)
                throw new ResourceException(document_.getMessageList("qmhsndrm"));
        }
        catch (PcmlException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error when replying to message", e);
            throw new ResourceException(e);
        }
    }



/**
Sends a message to the message queue.

@param messageID        The message ID.
@param messageFile      The integrated file system path name of the message file.
@param substitutionData The substitution data for the message, or null if none.
@param messageType      The message type.  Valid values are:
                        <ul>
                        <li>INQUIRY
                        <li>INFORMATIONAL
                        </ul>
@param replyMessageQueue The integrated file system path name of the reply message queue.
@return                 The message key, if this is an inquiry message,
                        null otherwise.

@exception ResourceException                If an error occurs.
**/
//
// Reminder: This is a private method, used for implementing the other methods.
//           Some validation is not necessary, since the public methods are only
//           available in certain combinations.
//
    private byte[] send(String messageID,
                        String messageFile,
                        Object substitutionData,
                        String messageType,
                        String replyMessageQueue)
        throws ResourceException
    {
        // Validate the parameters.
        if (messageID == null)
            throw new NullPointerException("messageID");
        if (messageFile == null)
            throw new NullPointerException("messageFile");

        if (substitutionData != null) {
            if ((!(substitutionData instanceof byte[]))
                && (!(substitutionData instanceof String)))
                throw new ExtendedIllegalArgumentException("substitutionData", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        if (messageType == null)
            throw new NullPointerException("messageType");
        if ((!messageType.equals(INFORMATIONAL))
            && (!messageType.equals(INQUIRY)))
            throw new ExtendedIllegalArgumentException("messageType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        if (replyMessageQueue == null)
            throw new NullPointerException("replyQueue");

        // Establish the connection if needed.
        if (!isConnectionEstablished())
            establishConnection();

        // Set the input parameters and call the API.
        try {
            String programName;
            if (substitutionData != null) {
                if (substitutionData instanceof byte[]) {
                    programName = "qmhsndmWithMessageData";
                    document_.setIntValue(programName + ".lengthOfMessageData", ((byte[])substitutionData).length);
                    document_.setValue(programName + ".messageData", substitutionData);
                }
                else {
                    programName = "qmhsndmWithImmediateText";
                    byte[] asBytes = CharConverter.stringToByteArray(getSystem(), (String)substitutionData);    // @A1A
                    document_.setIntValue(programName + ".lengthOfImmediateText", asBytes.length);              // @A1C
                    document_.setValue(programName + ".immediateText", asBytes);                                // @A1C
                }
            }
            else {
                programName = "qmhsndmWithImmediateText";
                document_.setIntValue(programName + ".lengthOfImmediateText", 0);
                document_.setValue(programName + ".immediateText", new byte[0]);                                // @A1C
            }

            document_.setValue(programName + ".messageID", messageID);

            if (messageFile.length() > 0) {
                QSYSObjectPathName messageFilePathName = new QSYSObjectPathName(messageFile);
                document_.setValue(programName + ".qualifiedMessageFileName.objectName", messageFilePathName.getObjectName());
                document_.setValue(programName + ".qualifiedMessageFileName.libraryName", messageFilePathName.getLibraryName());
            }
            else {
                document_.setValue(programName + ".qualifiedMessageFileName.objectName", "");
                document_.setValue(programName + ".qualifiedMessageFileName.libraryName", "");
            }


            document_.setValue(programName + ".messageType", messageType);
            formatQualifiedMessageQueueName(document_, programName + ".qualifiedMessageQueueNames");

            if (replyMessageQueue.length() > 0) {
                QSYSObjectPathName replyMessageQueuePathName = new QSYSObjectPathName(replyMessageQueue);
                document_.setValue(programName + ".qualifiedReplyMessageQueueName.objectName", replyMessageQueuePathName.getObjectName());
                document_.setValue(programName + ".qualifiedReplyMessageQueueName.libraryName", replyMessageQueuePathName.getLibraryName());
            }
            else {
                document_.setValue(programName + ".qualifiedReplyMessageQueueName.objectName", "");
                document_.setValue(programName + ".qualifiedReplyMessageQueueName.libraryName", "");
            }

            if (document_.callProgram(programName) == false)
                throw new ResourceException(document_.getMessageList(programName));

            // Get the output and return.
            byte[] messageKey = null;
            if (messageType.equals(INQUIRY))
                messageKey = (byte[])document_.getValue(programName + ".messageKey");
            return messageKey;
        }
        catch(PcmlException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error when sending a message", e);
            throw new ResourceException(e);
        }
    }



/**
Sends an informational message to the message queue.

@param messageID        The message ID.
@param messageFile      The integrated file system path name of the message file.

@exception ResourceException                If an error occurs.
**/
    public void sendInformational(String messageID, String messageFile)
        throws ResourceException
    {
        send(messageID, messageFile, null, INFORMATIONAL, "");
    }



/**
Sends an informational message to the message queue.

@param messageID        The message ID.
@param messageFile      The integrated file system path name of the message file.
@param substitutionData The substitution data for the message, or null if none.

@exception ResourceException                If an error occurs.
**/
    public void sendInformational(String messageID, String messageFile, byte[] substitutionData)
        throws ResourceException
    {
        send(messageID, messageFile, substitutionData, INFORMATIONAL, "");
    }



/**
Sends an informational message to the message queue.

@param messageText The message text.

@exception ResourceException                If an error occurs.
**/
    public void sendInformational(String messageText)
        throws ResourceException
    {
        if (messageText == null)
            throw new NullPointerException("messageText");

        send("", "", messageText, INFORMATIONAL, "");
  }



/**
Sends an inquiry message to the message queue.

@param messageID        The message ID.
@param messageFile      The integrated file system path name of the message file.
@param replyMessageQueue The integrated file system path name of the reply message queue.
@return                 The message key.

@exception ResourceException                If an error occurs.
**/
    public byte[] sendInquiry(String messageID,
                            String messageFile,
                            String replyMessageQueue)
        throws ResourceException
    {
        return send(messageID, messageFile, null, INQUIRY, replyMessageQueue);
    }



/**
Sends an inquiry message to the message queue.

@param messageID        The message ID.
@param messageFile      The integrated file system path name of the message file.
@param substitutionData The substitution data for the message, or null if none.
@param replyMessageQueue The integrated file system path name of the reply message queue.
@return                 The message key.

@exception ResourceException                If an error occurs.
**/
    public byte[] sendInquiry(String messageID,
                              String messageFile,
                              byte[] substitutionData,
                              String replyMessageQueue)
        throws ResourceException
    {
        return send(messageID, messageFile, substitutionData, INQUIRY, replyMessageQueue);
    }



/**
Sends an inquiry message to the message queue.

@param messageText The message text.
@param replyMessageQueue The integrated file system path name of the reply message queue.
@return                 The message key.

@exception ResourceException                If an error occurs.
**/
    public byte[] sendInquiry(String messageText, String replyMessageQueue)
        throws ResourceException
    {
        if (messageText == null)
            throw new NullPointerException("messageText");

        return send("", "", messageText, INQUIRY, replyMessageQueue);
    }



/**
Sets the fully qualified integrated file system path name of the
message queue.  The default is {@link #CURRENT CURRENT}. The path cannot be changed
if this object has already established a connection to the AS/400.

@param path The fully qualified integrated file system path name of the
            message queue, or {@link #CURRENT CURRENT} to refer to the user's default
            message queue.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setPath(String path)
        throws PropertyVetoException
    {
        if (path == null)
            throw new NullPointerException("path");
        if (arePropertiesFrozen())
            throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

        String oldValue = path_;
        fireVetoableChange("path", oldValue, path);
        path_ = path.trim();

        // Verify that it is a valid IFS path name and
        // set the presentation data.
        Presentation presentation = getPresentation();
        if (path_.equals(CURRENT)) {
            presentation.setName(CURRENT);
        }
        else {
            QSYSObjectPathName fullPath = new QSYSObjectPathName(path_);
            presentation.setName(fullPath.getObjectName());
            presentation.setFullName(fullPath.getPath());
        }

        firePropertyChange("path", oldValue, path);
    }





//-----------------------------------------------------------------------------------------
// List implementation.
//-----------------------------------------------------------------------------------------

    void setOpenParameters(ProgramCallDocument document)
        throws PcmlException, ResourceException
    {
        super.setOpenParameters(document);

        boolean replacementData = ((Boolean)getSelectionValue(REPLACEMENT_DATA)).booleanValue();
        Integer formattingCharacters = (Integer)getSelectionValue(FORMATTING_CHARACTERS);
        if (replacementData) {
            document.setIntValue(messageFieldIdentifier_, 302);
            if (formattingCharacters.equals(NO_FORMATTING))
                document.setIntValue(messageHelpFieldIdentifier_, 402);
            else
                document.setIntValue(messageHelpFieldIdentifier_, 404);
        }
        else {
            document.setIntValue(messageFieldIdentifier_, 301);
            if (formattingCharacters.equals(NO_FORMATTING))
                document.setIntValue(messageHelpFieldIdentifier_, 401);
            else
                document.setIntValue(messageHelpFieldIdentifier_, 403);
        }
    }



    Resource newResource(String programName, int[] indices)
    throws PcmlException, ResourceException
    {
        AS400 system = getSystem();

        // Create the resource object.
        byte[] messageKey = (byte[])document_.getValue(programName + ".receiverVariable.messageKey", indices);
        Object resourceKey = RQueuedMessage.computeResourceKey(system, path_, messageKey);
        RQueuedMessage rQueuedMessage = new RQueuedMessage();
        try { rQueuedMessage.setSystem(system); } catch (PropertyVetoException pve) {}   // @D6a
        rQueuedMessage.setResourceKey(resourceKey);

        // Copy the information from the API record to the QueuedMessage attributes.
        Object[] attributeIDs = openListAttributeMap_.getIDs();
        Object[] values = openListAttributeMap_.getValues(attributeIDs, system, document_, programName, indices);
        for(int i = 0; i < values.length; ++i)
            rQueuedMessage.initializeAttributeValue(attributeIDs[i], values[i]);

        // Format the help text if needed.
        Integer formattingCharacters = (Integer)getSelectionValue(FORMATTING_CHARACTERS);
        if (formattingCharacters.equals(SUBSTITUTE_FORMATTING_CHARACTERS)) {
            String messageHelp = (String)rQueuedMessage.getAttributeValue(RQueuedMessage.MESSAGE_HELP);
            messageHelp = com.ibm.as400.access.MessageFile.substituteFormattingCharacters(messageHelp);
            rQueuedMessage.initializeAttributeValue(RQueuedMessage.MESSAGE_HELP, messageHelp);
        }

        // Set the presentation information.
        // The name is in the format: MessageId(MessageKey)
        // The full name is in the format: MessageQueueName-MessageId(MessageKey)
        StringBuffer name = new StringBuffer();
        name.append((String)rQueuedMessage.getAttributeValue(RQueuedMessage.MESSAGE_ID));
        name.append('(');
        name.append(Presentation.bytesToHex((byte[])rQueuedMessage.getAttributeValue(RQueuedMessage.MESSAGE_KEY)));
        name.append(')');

        StringBuffer fullName = new StringBuffer();
        fullName.append(path_);
        fullName.append('-');
        fullName.append(name);

        Presentation presentation = rQueuedMessage.getPresentation();
        presentation.setName(name.toString());
        presentation.setFullName(fullName.toString());
        presentation.setValue(Presentation.DESCRIPTION_TEXT, rQueuedMessage.getAttributeValue(RQueuedMessage.MESSAGE_TEXT));
        presentation.setValue(Presentation.HELP_TEXT, rQueuedMessage.getAttributeValue(RQueuedMessage.MESSAGE_HELP));

        return rQueuedMessage;
    }


 }




