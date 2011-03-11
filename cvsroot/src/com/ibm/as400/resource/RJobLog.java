///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RJobLog.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.as400.access.Trace;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;
import java.beans.PropertyVetoException;



/**
The RJobLog class represents a job log.  This is used
to get a list of messages in a job log or to write messages to a job log.
In order to access a job log, the system and either the job name, user name,
and job number or internal job identifier need to be set.  A valid combination
of these must be set by getting any of the job log's messages.

<a name="default">
<p>If you do not specify any of the job name, user name, job number,
or internal job identifier, the default job is used.
The default job is the host server job for remote program calls.
</a>

<a name="selectionIDs"><p>The following selection IDs are supported:
<ul>
<li>{@link #LIST_DIRECTION LIST_DIRECTION}
<li>{@link #STARTING_MESSAGE_KEY STARTING_MESSAGE_KEY}
</ul>
</a>

<p>Use one or more of these selection IDs with
{@link com.ibm.as400.resource.ResourceList#getSelectionValue getSelectionValue()}
and {@link com.ibm.as400.resource.ResourceList#setSelectionValue setSelectionValue()}
to access the selection values for an RJobLog.

<p>RJobLog objects generate {@link com.ibm.as400.resource.RQueuedMessage RQueuedMessage}
objects.  RQueuedMessage objects have many <a href="{@docRoot}/com/ibm/as400/resource/RQueuedMessage.html#attributeIDs">
attributes</a>.  Only some of theses attribute values are set, depending on how an RQueuedMessage
object is created.  The following is a list of attribute IDs whose values are set on RQueuedMessage
objects returned in a list of job log messages:
<ul>
<li>{@link com.ibm.as400.resource.RQueuedMessage#DATE_SENT DATE_SENT}
<li>{@link com.ibm.as400.resource.RQueuedMessage#DEFAULT_REPLY DEFAULT_REPLY}
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_FILE MESSAGE_FILE}
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_HELP MESSAGE_HELP}
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_ID MESSAGE_ID}
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_KEY MESSAGE_KEY}
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_SEVERITY MESSAGE_SEVERITY}
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_TEXT MESSAGE_TEXT}
<li>{@link com.ibm.as400.resource.RQueuedMessage#MESSAGE_TYPE MESSAGE_TYPE}
<li>{@link com.ibm.as400.resource.RQueuedMessage#REPLY_STATUS REPLY_STATUS}
<li>{@link com.ibm.as400.resource.RQueuedMessage#SENDER_JOB_NAME SENDER_JOB_NAME}
<li>{@link com.ibm.as400.resource.RQueuedMessage#SENDER_JOB_NUMBER SENDER_JOB_NUMBER}
<li>{@link com.ibm.as400.resource.RQueuedMessage#SENDER_USER_NAME SENDER_USER_NAME}
<li>{@link com.ibm.as400.resource.RQueuedMessage#SENDING_PROGRAM_NAME SENDING_PROGRAM_NAME}
</ul>

<blockquote><pre>
// Create an RJobLog object to represent a specific job log.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RJobLog jobLog = new RJobLog(system, "AJOBNAME", "AUSERID", "AJOBNUMBER");
<br>
// Set the selection so that the list of messages includes
// only the newest message.
jobLog.setSelectionValue(RJobLog.STARTING_MESSAGE_KEY, RJobLog.NEWEST);
<br>
// Open the list and wait for it to complete.
jobLog.open();
jobLog.waitForComplete();
<br>
// Read and print the messages in the list.
long numberOfMessages = jobLog.getListLength();
for(long i = 0; i &lt; numberOfMessages; ++i)
{
    RQueuedMessage queuedMessage = (RQueuedMessage)jobLog.resourceAt(i);
    System.out.println(queueMessage.getAttributeValue(RQueuedMessage.MESSAGE_TEXT));
}
<br>
// Close the list.
jobLog.close();
</pre></blockquote>

@deprecated Use
{@link com.ibm.as400.access.JobLog JobLog} instead, as this package may be removed in the future.
@see RQueuedMessage
**/
public class RJobLog
extends SystemResourceList
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



/**
Constant referring to the message key for the newest message in the job log.
**/
    public final static byte[] NEWEST                  = new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };

/**
Constant referring to the next message in the job log.
**/
    public final static String NEXT                    = "*NEXT";

/**
Constant referring to the message key for the oldest message in the job log.
**/
    public final static byte[] OLDEST                  = new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 };

/**
Constant referring to the previous message in the job log.
**/
    public final static String PREVIOUS                = "*PRV";


//-----------------------------------------------------------------------------------------
// Private data.
//-----------------------------------------------------------------------------------------

    private static final String                 PRESENTATION_KEY_           = "JOB_LOG";
    private static final String                 ICON_BASE_NAME_             = "RJobLog";

    private static PresentationLoader           presentationLoader_         = new PresentationLoader("com.ibm.as400.resource.ResourceMRI");


//-----------------------------------------------------------------------------------------
// Selection IDs.
//
// * If you add a selection here, make sure and add it to the class javadoc
//   and in ResourceMRI.java.
//-----------------------------------------------------------------------------------------

    private static ResourceMetaDataTable selections_            = new ResourceMetaDataTable(presentationLoader_, PRESENTATION_KEY_);
    private static ProgramMap            selectionMap_          = new ProgramMap();

/**
Selection ID for list direction.  This identifies a String selection,
which represents the direction to list messages relative to the values
specified for the {@link #STARTING_MESSAGE_KEY STARTING_MESSAGE_KEY}
selection.  Possible values are:
<ul>
<li>{@link #NEXT NEXT}
    - Returns messages that are newer than the messages specified for
      the STARTING_MESSAGE_KEY selection.
<li>{@link #PREVIOUS PREVIOUS}
    - Returns messages that are older than the messages specified for
      the STARTING_MESSAGE_KEY selection.
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
Selection ID for starting message key.  This identifies a byte[] selection,
which represents the message key used to begin searching for messages
to list from the corresponding entry in the message queue.  Possible
values are:
<ul>
<li>{@link #OLDEST OLDEST}
    - The first message to be returned is the oldest message in the queue.
<li>{@link #NEWEST NEWEST}
    - The first message to be returned is the newest message in the queue.
<li>Any valid message key.
</ul>
The default is OLDEST.
**/
    public static final String STARTING_MESSAGE_KEY                = "STARTING_MESSAGE_KEY";

    static {
        selections_.add(STARTING_MESSAGE_KEY, byte[].class, false,
                        new byte[][] { NEWEST, OLDEST }, OLDEST, false);
        selectionMap_.add(STARTING_MESSAGE_KEY, null, "messageSelectionInformation.startingMessageKey");
    }



//-----------------------------------------------------------------------------------------
// Open list attribute map.
//
// * If you add an attribute here, make sure and add it to the class javadoc.
//-----------------------------------------------------------------------------------------

    private static ProgramMap            openListAttributeMap_  = new ProgramMap();
    private static final String          openListProgramName_   = "qgyoljbl";

    static {
        openListAttributeMap_.add(RQueuedMessage.MESSAGE_SEVERITY, null, "receiverVariable.messageSeverity");
        openListAttributeMap_.add(RQueuedMessage.MESSAGE_ID, null, "receiverVariable.messageIdentifier");
        openListAttributeMap_.add(RQueuedMessage.MESSAGE_TYPE, null, "receiverVariable.messageType", new IntegerValueMap());
        openListAttributeMap_.add(RQueuedMessage.MESSAGE_KEY, null, "receiverVariable.messageKey");
        openListAttributeMap_.add(RQueuedMessage.MESSAGE_FILE, null, "receiverVariable.messageFile", new QualifiedValueMap(QualifiedValueMap.FORMAT_20, "MSGF"));
        openListAttributeMap_.add(RQueuedMessage.DATE_SENT, null, "receiverVariable.dateAndTimeSent", new DateValueMap(DateValueMap.FORMAT_13));
        openListAttributeMap_.add(RQueuedMessage.MESSAGE_TEXT, null, "receiverVariable.messageWithReplacementData.data");
        openListAttributeMap_.add(RQueuedMessage.SENDER_JOB_NAME, null, "receiverVariable.qualifiedSenderJobName.data", new SubstringValueMap(0, 10, true));
        openListAttributeMap_.add(RQueuedMessage.SENDER_JOB_NUMBER, null, "receiverVariable.qualifiedSenderJobName.data", new SubstringValueMap(20, 6, true));
        openListAttributeMap_.add(RQueuedMessage.SENDER_USER_NAME, null, "receiverVariable.qualifiedSenderJobName.data", new SubstringValueMap(10, 10, true));
        openListAttributeMap_.add(RQueuedMessage.SENDING_PROGRAM_NAME, null, "receiverVariable.sendingProgramName.data");
        openListAttributeMap_.add(RQueuedMessage.REPLY_STATUS, null, "receiverVariable.replyStatus.data");
        openListAttributeMap_.add(RQueuedMessage.DEFAULT_REPLY, null, "receiverVariable.defaultReply.data");
        openListAttributeMap_.add(RQueuedMessage.MESSAGE_HELP, null, "receiverVariable.messageHelpWithRDAndFC.data");
    }



//-----------------------------------------------------------------------------------------
// PCML document initialization.
//-----------------------------------------------------------------------------------------

    private static final String             DOCUMENT_NAME_      = "com.ibm.as400.resource.RJobLog";
    private static ProgramCallDocument      staticDocument_     = null;

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

    private static final String                 DEFAULT_MESSAGE_FILE_       = "/QSYS.LIB/QCPFMSG.MSGF";
    private static final byte[]                 EMPTY_BYTES_                = new byte[0];
    private static final byte[]                 BLANK_INTERNAL_JOB_ID_      = new byte[] {
                                                                                (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40,
                                                                                (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40,
                                                                                (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40,
                                                                                (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40
                                                                              };

    private byte[]                              internalJobID_              = null;
    private String                              name_                       = "*";
    private String                              number_                     = "";
    private String                              user_                       = "";



//-----------------------------------------------------------------------------------------
// Code.
//-----------------------------------------------------------------------------------------

/**
Constructs an RJobLog object.
**/
    public RJobLog()
    {
        super(presentationLoader_.getPresentationWithIcon(PRESENTATION_KEY_, ICON_BASE_NAME_),
              RQueuedMessage.attributes_,
              selections_,
              null,
              openListProgramName_,
              null,
              selectionMap_);

        updatePresentation();
    }



/**
Constructs an RJobLog object.

@param system The system.
**/
    public RJobLog(AS400 system)
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
Constructs an RJobLog object.

@param system       The system.
@param name         The job name.  Specify "*" to indicate <a href="#default">the default job</a>.
@param user         The user name.  This must be blank if name is "*".
@param number       The job number.  This must be blank if name is "*".
**/
    public RJobLog(AS400 system,
                  String name,
                  String user,
                  String number)
    {
        this();
        try {
            setSystem(system);
            setName(name);
            setUser(user);
            setNumber(number);
        }
        catch(PropertyVetoException e) {
            // Ignore.
        }
    }



/**
Constructs an RJobLog object.  This sets the job name to "*INT".

@param system           The system.
@param internalJobID    The internal job identifier.
**/
    public RJobLog(AS400 system, byte[] internalJobID)
    {
        this();

        try {
            setSystem(system);
            setName("*INT");
            setInternalJobID(internalJobID);
        }
        catch(PropertyVetoException e) {
            // Ignore.

        }
    }



/**
Establishes the connection to the system.

<p>The method is called by the resource framework automatically
when the connection needs to be established.

@exception ResourceException                If an error occurs.
**/
    protected void establishConnection()
    throws ResourceException
    {
        // Validate if we can establish the connection.
        if (internalJobID_ == null) {
            if (!name_.equals("*")) {
                if (number_.length() == 0)
                    throw new ExtendedIllegalStateException("number", ExtendedIllegalStateException.PROPERTY_NOT_SET);
                if (user_.length() == 0)
                    throw new ExtendedIllegalStateException("user", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            }
        }

        // Call the superclass.
        super.establishConnection();

        // Initialize the PCML document.
        ProgramCallDocument document = (ProgramCallDocument)staticDocument_.clone();
        try {
            if (internalJobID_ != null) {
                document.setValue("qgyoljbl.messageSelectionInformation.internalJobIdentifier", internalJobID_);
                document.setValue("qgyoljbl.messageSelectionInformation.qualifiedJobName.jobName", "*INT");
            }
            else {
                document.setValue("qgyoljbl.messageSelectionInformation.internalJobIdentifier", BLANK_INTERNAL_JOB_ID_);
                document.setValue("qgyoljbl.messageSelectionInformation.qualifiedJobName.jobName", name_.toUpperCase());
                document.setValue("qgyoljbl.messageSelectionInformation.qualifiedJobName.jobNumber", number_.toUpperCase());
                document.setValue("qgyoljbl.messageSelectionInformation.qualifiedJobName.userName", user_.toUpperCase());
            }
        }
        catch(PcmlException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error setting path information in PCML document", e);
        }
        setDocument(document);
    }



/**
Returns the internal job identifier.

@return The internal job identifier, or null if none has been set.
**/
    public byte[] getInternalJobID()
    {
        return internalJobID_;
    }




/**
Returns the job name.

@return The job name, or "*" if none has been set.
**/
    public String getName()
    {
        return name_;
    }



/**
Returns the job number.

@return The job number, or "" if none has been set.
**/
    public String getNumber()
    {
        return number_;
    }



/**
Returns the user name.

@return The user name, or "" if none has been set.
**/
    public String getUser()
    {
        return user_;
    }



/**
Sets the internal job identifier.  The job name
must be set to "*INT" for this to be recognized.
This cannot be changed if the object has established
a connection to the system.

@param internalJobID    The internal job identifier.

@exception PropertyVetoException    If the property change is vetoed.
**/
    public void setInternalJobID(byte[] internalJobID)
        throws PropertyVetoException
    {
        if (internalJobID == null)
            throw new NullPointerException("internalJobID");
        if (arePropertiesFrozen())
            throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

        byte[] oldValue = internalJobID_;
        fireVetoableChange("internalJobID", oldValue, internalJobID);
        internalJobID_ = internalJobID;
        updatePresentation();
        firePropertyChange("internalJobID", oldValue, internalJobID);
    }



/**
Sets the job name.  This cannot be changed
if the object has established a connection to the system.

@param name The job name.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setName(String name)
        throws PropertyVetoException
    {
        if (name == null)
            throw new NullPointerException("name");
        if (arePropertiesFrozen())
            throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

        String oldValue = name_;
        fireVetoableChange("name", oldValue, name);
        name_ = name;
        updatePresentation();
        firePropertyChange("name", oldValue, name);
    }



/**
Sets the job number. This cannot be changed
if the object has established a connection to the system.

@param number The job number.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setNumber(String number)
        throws PropertyVetoException
    {
        if (number == null)
            throw new NullPointerException("number");
        if (arePropertiesFrozen())
            throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

        String oldValue = number_;
        fireVetoableChange("number", oldValue, number);
        number_ = number;
        updatePresentation();
        firePropertyChange("number", oldValue, number);
  }



/**
Sets the user name.  This cannot be changed
if the object has established a connection to the system.

@param user The user name.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setUser(String user)
        throws PropertyVetoException
    {
        if (user == null)
            throw new NullPointerException("user");
        if (arePropertiesFrozen())
            throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

        String oldValue = user_;
        fireVetoableChange("user", oldValue, user);
        user_ = user;
        updatePresentation();
        firePropertyChange("user", oldValue, user);
    }



/**
Updates the presentation object for this job log based on the job name,
user name, and job number.  This should be called whenever any of these
characteristics change.
**/
    private void updatePresentation()
    {
        Presentation presentation = getPresentation();
        presentation.setName(name_);

        StringBuffer fullName = new StringBuffer();
        fullName.append(number_);
        fullName.append('/');
        fullName.append(user_);
        fullName.append('/');
        fullName.append(name_);
        presentation.setFullName(fullName.toString());
    }


/**
Writes a program message to the job log for the job in which the program is running.
<br>Note: The program runs in the job of the Remote Command Host Server (QZRCSRVS) unless it is invoked "on-thread" on the i5/OS system.

@param system       The system.  If the system specifies localhost, the message is written
                    to the job log of the process from which this method is called.
                    Otherwise the message is written to the QZRCSRVS job.
@param messageID    The message ID.  The message must be in the default message file
                    /QSYS.LIB/QCPFMSG.MSGF.
@param messageType  The message type. Possible values are:
                    <ul>
                    <li>{@link com.ibm.as400.access.AS400Message#COMPLETION AS400Message.COMPLETION }
                    <li>{@link com.ibm.as400.access.AS400Message#DIAGNOSTIC  AS400Message.DIAGNOSTIC }
                    <li>{@link com.ibm.as400.access.AS400Message#INFORMATIONAL  AS400Message.INFORMATIONAL }
                    <li>{@link com.ibm.as400.access.AS400Message#ESCAPE  AS400Message.ESCAPE }
                    </ul>
                    The message type must be AS400Message.INFORMATIONAL for an immediate
                    message.
@see com.ibm.as400.access.ProgramCall#isStayOnThread()

@exception ResourceException            If an error occurs.
**/
    public static void writeMessage(AS400 system,
                                    String messageID,
                                    int messageType)
        throws ResourceException
    {
        writeMessage(system, messageID, messageType, DEFAULT_MESSAGE_FILE_, null);
    }



/**
Writes a program message to the job log for the job in which the program is running.
<br>Note: The program runs in the job of the Remote Command Host Server (QZRCSRVS) unless it is invoked "on-thread" on the i5/OS system.

@param system           The system.  If the system specifies localhost, the message is written
                        to the job log of the process from which this method is called.
                        Otherwise the message is written to the QZRCSRVS job.
@param messageID        The message ID.  The message must be in the default message file
                        /QSYS.LIB/QCPFMSG.MSGF.
@param messageType      The message type. Possible values are:
                        <ul>
                        <li>{@link com.ibm.as400.access.AS400Message#COMPLETION AS400Message.COMPLETION }
                        <li>{@link com.ibm.as400.access.AS400Message#DIAGNOSTIC  AS400Message.DIAGNOSTIC }
                        <li>{@link com.ibm.as400.access.AS400Message#INFORMATIONAL  AS400Message.INFORMATIONAL }
                        <li>{@link com.ibm.as400.access.AS400Message#ESCAPE  AS400Message.ESCAPE }
                        </ul>
                        The message type must be AS400Message.INFORMATIONAL for an immediate
                        message.
@param substitutionData The substitution data.  The substitution data can be from 0-32767 bytes
                        for a conventional message and from 1-6000 bytes for an immediate message.
@see com.ibm.as400.access.ProgramCall#isStayOnThread()

@exception ResourceException            If an error occurs.
**/
    public static void writeMessage(AS400 system,
                                    String messageID,
                                    int messageType,
                                    byte[] substitutionData)
        throws ResourceException
    {
        writeMessage(system, messageID, messageType, DEFAULT_MESSAGE_FILE_, substitutionData);
    }



/**
Writes a program message to the job log for the job in which the program is running.
<br>Note: The program runs in the job of the Remote Command Host Server (QZRCSRVS) unless it is invoked "on-thread" on the i5/OS system.

@param system           The system.  If the system specifies localhost, the message is written
                        to the job log of the process from which this method is called.
                        Otherwise the message is written to the QZRCSRVS job.
@param messageID        The message ID.
@param messageType      The message type. Possible values are:
                        <ul>
                        <li>{@link com.ibm.as400.access.AS400Message#COMPLETION AS400Message.COMPLETION }
                        <li>{@link com.ibm.as400.access.AS400Message#DIAGNOSTIC  AS400Message.DIAGNOSTIC }
                        <li>{@link com.ibm.as400.access.AS400Message#INFORMATIONAL  AS400Message.INFORMATIONAL }
                        <li>{@link com.ibm.as400.access.AS400Message#ESCAPE  AS400Message.ESCAPE }
                        </ul>
                        The message type must be AS400Message.INFORMATIONAL for an immediate
                        message.
@param messageFile      The integrated file system path name of the message file.
@see com.ibm.as400.access.ProgramCall#isStayOnThread()

@exception ResourceException            If an error occurs.
**/
    public static void writeMessage(AS400 system,
                                    String messageID,
                                    int messageType,
                                    String messageFile)
        throws ResourceException
    {
        writeMessage(system, messageID, messageType, messageFile, null);
    }



/**
Writes a program message to the job log for the job in which the program is running.
<br>Note: The program runs in the job of the Remote Command Host Server (QZRCSRVS) unless it is invoked "on-thread" on the i5/OS system.

@param system           The system.  If the system specifies localhost, the message is written
                        to the job log of the process from which this method is called.
                        Otherwise the message is written to the QZRCSRVS job.
@param messageID        The message ID.
@param messageType      The message type. Possible values are:
                        <ul>
                        <li>{@link com.ibm.as400.access.AS400Message#COMPLETION AS400Message.COMPLETION }
                        <li>{@link com.ibm.as400.access.AS400Message#DIAGNOSTIC  AS400Message.DIAGNOSTIC }
                        <li>{@link com.ibm.as400.access.AS400Message#INFORMATIONAL  AS400Message.INFORMATIONAL }
                        <li>{@link com.ibm.as400.access.AS400Message#ESCAPE  AS400Message.ESCAPE }
                        </ul>
                        The message type must be AS400Message.INFORMATIONAL for an immediate
                        message.
@param messageFile      The integrated file system path name of the message file.
@param substitutionData The substitution data.  The substitution data can be from 0-32767 bytes
                        for a conventional message and from 1-6000 bytes for an immediate message.
@see com.ibm.as400.access.ProgramCall#isStayOnThread()

@exception ResourceException            If an error occurs.
**/
    public static void writeMessage(AS400 system,
                                    String messageID,
                                    int messageType,
                                    String messageFile,
                                    byte[] substitutionData)
        throws ResourceException
    {
        // Validate the parameters.
        if (system == null)
            throw new NullPointerException("system");
         if (messageID == null)
            throw new NullPointerException("messageID");

        String messageTypeAsString = null;
        switch(messageType) {
        case AS400Message.COMPLETION:
            messageTypeAsString = "*COMP";
            break;
        case AS400Message.DIAGNOSTIC:
            messageTypeAsString = "*DIAG";
            break;
        case AS400Message.INFORMATIONAL:
            messageTypeAsString = "*INFO";
            break;
        case AS400Message.ESCAPE:
            messageTypeAsString = "*ESCAPE";
            break;
        default:
            throw new ExtendedIllegalArgumentException("messageType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        if (messageFile == null)
            throw new NullPointerException("messageFile");
        else if (messageFile.length() == 0)
            throw new ExtendedIllegalArgumentException("messageFile", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        // Set the input parameters and call the API.
        try {
            ProgramCallDocument document = (ProgramCallDocument)staticDocument_.clone();
            document.setSystem(system);
            document.setValue("qmhsndpm.messageID", messageID);

            QSYSObjectPathName messageFilePathName = new QSYSObjectPathName(messageFile);
            document.setValue("qmhsndpm.qualifiedMessageFileName.objectName", messageFilePathName.getObjectName());
            document.setValue("qmhsndpm.qualifiedMessageFileName.libraryName", messageFilePathName.getLibraryName());

            if (substitutionData == null) {
                document.setIntValue("qmhsndpm.lengthOfMessageDataOrImmediateText", 0);
                document.setValue("qmhsndpm.messageDataOrImmediateText", "");
            }
            else {
                document.setIntValue("qmhsndpm.lengthOfMessageDataOrImmediateText", substitutionData.length);
                document.setValue("qmhsndpm.messageDataOrImmediateText", substitutionData);
            }

            document.setValue("qmhsndpm.messageType", messageTypeAsString);

            if (document.callProgram("qmhsndpm") == false) {                            // @A1C

                // If only one message came back, and it is the one that we sent,          @A1A
                // then do not throw an exception.                                         @A1A
                AS400Message[] messageList = document.getMessageList("qmhsndpm");       // @A1A
                if (messageList.length == 1) {                                          // @A1A
                    if ((messageList[0].getID().equals(messageID))                      // @A1A
                        && (messageList[0].getType() == messageType)) {                 // @A1A
                        if (Trace.isTraceOn())                                          // @A1A
                            Trace.log(Trace.INFORMATION, "False escape message ignored.");    // @A1A
                        return;                                                         // @A1A
                    }                                                                   // @A1A
                }                                                                       // @A1A

                throw new ResourceException(messageList);                               // @A1C
            }                                                                           // @A1A
        }
        catch(PcmlException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error when writing a message", e);
            throw new ResourceException(e);
        }
    }



//-----------------------------------------------------------------------------------------
// List implementation.
//-----------------------------------------------------------------------------------------

    Resource newResource(String programName, int[] indices)
    throws PcmlException, ResourceException
    {
        AS400 system = getSystem();
        ProgramCallDocument document = getDocument();

        // Create the resource object.
        byte[] messageKey = (byte[])document.getValue(programName + ".receiverVariable.messageKey", indices);
        String jobLogName = RJobLog.this.toString();
        Object resourceKey = RQueuedMessage.computeResourceKey(system, jobLogName, messageKey);
        RQueuedMessage rQueuedMessage = (RQueuedMessage)ResourcePool.GLOBAL_RESOURCE_POOL.getResource(resourceKey);
        if (rQueuedMessage == null) {
            rQueuedMessage = new RQueuedMessage();
            rQueuedMessage.setResourceKey(resourceKey);
        }

        // Copy the information from the API record to the QueuedMessage attributes.
        Object[] attributeIDs = openListAttributeMap_.getIDs();
        Object[] values = openListAttributeMap_.getValues(attributeIDs, system, document, programName, indices);
        for(int i = 0; i < values.length; ++i)
            rQueuedMessage.initializeAttributeValue(attributeIDs[i], values[i]);

        // Set the presentation information.
        // The name is in the format: MessageId(MessageKey)
        // The full name is in the format: JobNumber/UserName/JobName-MessageId(MessageKey)
        StringBuffer name = new StringBuffer();
        name.append((String)rQueuedMessage.getAttributeValue(RQueuedMessage.MESSAGE_ID));
        name.append('(');
        name.append(Presentation.bytesToHex((byte[])rQueuedMessage.getAttributeValue(RQueuedMessage.MESSAGE_KEY)));
        name.append(')');

        StringBuffer fullName = new StringBuffer();
        fullName.append(jobLogName);
        fullName.append('-');
        fullName.append(name.toString());

        Presentation presentation = rQueuedMessage.getPresentation();
        presentation.setName(name.toString());
        presentation.setFullName(fullName.toString());
        presentation.setValue(Presentation.DESCRIPTION_TEXT, rQueuedMessage.getAttributeValue(RQueuedMessage.MESSAGE_TEXT));
        presentation.setValue(Presentation.HELP_TEXT, rQueuedMessage.getAttributeValue(RQueuedMessage.MESSAGE_HELP));

        return rQueuedMessage;
    }


 }


