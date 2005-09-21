///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  MessageQueue.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;

/**
 Represents a message queue object on the server.  If no message queue path is set, then the default is {@link #CURRENT CURRENT}, which represents the current user's message queue.  If necessary, the name of the message queue will be retrieved from the current user's user profile.
 <p>QueuedMessage objects have many attributes.  Only some of these attribute values are set, depending on how a QueuedMessage object is created.  The following is a list of attributes whose values are set on QueuedMessage objects returned in a list of messages:
 <ul>
 <li>alert option
 <li>date sent
 <li>default reply
 <li>message file
 <li>message help
 <li>message ID
 <li>message key
 <li>message queue
 <li>message severity
 <li>message text
 <li>message type
 <li>reply status
 <li>sender job name
 <li>sender job number
 <li>sender job user name
 <li>if the server release is V5R3M0 or greater, sending current user
 <li>sending program name
 </ul>
 <a name="receiveIDs">
 <p>The following is a list of attributes whose values are set on objects returned by receive():
 <ul>
 <li>alert option
 <li>date sent
 <li>message file
 <li>message help
 <li>message ID
 <li>message key
 <li>message queue
 <li>message severity
 <li>message text
 <li>message type
 <li>sender job name
 <li>sender job number
 <li>sender job user name
 <li>sending program name
 <li>substitution data
 </ul>
 </a>
 @see  com.ibm.as400.access.QueuedMessage
 **/
public class MessageQueue implements Serializable
{
    static final long serialVersionUID = 5L;

    /**
     Constant referring to all messages in the message queue.
     **/
    public final static String ALL = "*ALL";

    /**
     Constant referring to any message in the message queue.
     **/
    public final static String ANY = "*ANY";

    /**
     Constant referring to a message identified by a key.
     **/
    public final static String BYKEY = "*BYKEY";

    /**
     Constant referring to completion messages.
     **/
    public final static String COMPLETION = "*COMP";

    /**
     Constant referring to the sender's copy of a previously sent inquiry message.
     **/
    public final static String COPY = "*COPY";

    /**
     Constant referring to the current user's message queue.
     **/
    public final static String CURRENT = "*CURRENT";

    /**
     Constant referring to diagnostic messages.
     **/
    public final static String DIAGNOSTIC = "*DIAG";

    /**
     Constant referring to the first message in the message queue.
     **/
    public final static String FIRST = "*FIRST";

    /**
     Constant referring to informational messages.
     **/
    public final static String INFORMATIONAL = "*INFO";

    /**
     Constant referring to inquiry messages.
     **/
    public final static String INQUIRY = "*INQ";

    /**
     Constant referring to all messages in the message queue except unanswered inquiry and unanswered senders' copy messages.
     **/
    public final static String KEEP_UNANSWERED = "*KEEPUNANS";

    /**
     Constant referring to the last message in the message queue.
     **/
    public final static String LAST = "*LAST";

    /**
     Constant referring to messages that need a reply.
     **/
    public final static String MESSAGES_NEED_REPLY = "*MNR";

    /**
     Constant referring to messages that do not need a reply.
     **/
    public final static String MESSAGES_NO_NEED_REPLY = "*MNNR";

    /**
     Constant referring to all new messages in the message queue.  New messages are those that have not been received.
     **/
    public final static String NEW = "*NEW";

    /**
     Constant referring to the message key for the newest message in the queue.
     **/
    public final static byte[] NEWEST = new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};  // All FF's.

    /**
     Constant referring to the next message in the message queue.
     **/
    public final static String NEXT = "*NEXT";

    /**
     Constant referring to all old messages in the message queue.  Old messages are those that have already been received.
     **/
    public final static String OLD = "*OLD";

    /**
     Constant referring to the message key for the oldest message in the queue.
     **/
    public final static byte[] OLDEST = new byte[4];  // All 0's.

    /**
     Constant referring to the previous message in the message queue.
     **/
    public final static String PREVIOUS = "*PRV";

    /**
     Constant indicating that the message should be removed from the message queue.
     **/
    public final static String REMOVE = "*REMOVE";

    /**
     Constant referring to the reply to an inquiry message.
     **/
    public final static String REPLY = "*RPY";

    /**
     Constant indicating that the message should remain in the message queue without changing its new or old designation.
     **/
    public final static String SAME = "*SAME";

    /**
     Constant referring to the sender's copies of messages that need replies.
     **/
    public final static String SENDERS_COPY_NEED_REPLY = "*SCNR";

    // Shared error code parameter.
    private static final ProgramParameter ERROR_CODE = new ProgramParameter(new byte[8]);
    // Shared blank key.
    private static final byte[] BLANK_KEY = new byte[] { 0x40, 0x40, 0x40, 0x40 };

    // The server where the message queue is located.
    private AS400 system_;
    // The full IFS path name of the message queue.
    private String path_;
    // The library that contains the message queue.
    private String library_ = null;
    // The name of the message queue.
    private String name_ = null;

    // Length of the message queue message list.
    private int length_;
    // Length of the data in the message queue message list.
    private int dataLength_;
    // Handle that references the user space used by the open list APIs.
    private byte[] handle_;
    // If the list info has changed, close the old handle before loading the new one.
    private boolean closeHandle_ = false;

    // Whether the list should be sorted.
    private boolean sort_ = false;

    // Formatting option for the second level text.
    private int helpTextFormatting_ = MessageFile.NO_FORMATTING;
    // List direction oldest to newest or newest to oldest.
    private boolean listDirection_ = true;
    // Minimum severity selection criteria.
    private int severity_ = 0;
    // Starting user key for list.
    private byte[] userStartingMessageKey_;
    // starting workstation key for list.
    private byte[] workstationStartingMessageKey_;
    // Combined selection criteria for list.
    private String selection_ = ALL;
    // Selection criteria for messages needing reply.
    private boolean selectMessagesNeedReply_ = true;
    // Selection criteria for messages not needing reply.
    private boolean selectMessagesNoNeedReply_ = true;
    // Selection criteria for senders copy messages needing reply.
    private boolean selectSendersCopyMessagesNeedReply_ = true;

    // Cached converter object, reset everytime system changes.
    private transient Converter conv_ = null;
    // Cached converted qualified message queue name, reset everytime system or path changes.
    private transient byte[] qualifiedMessageQueueName_ = null;

    // List of property change event bean listeners.
    private transient PropertyChangeSupport propertyChangeListeners_ = null;  // Set on first add.
    // List of vetoable change event bean listeners.
    private transient VetoableChangeSupport vetoableChangeListeners_ = null;  // Set on first add.


    /**
     Constructs a MessageQueue object.  The system must be provided later.  The message queue path defaults to {@link #CURRENT CURRENT}.
     @see  #setPath
     @see  #setSystem
     **/
    public MessageQueue()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing MessageQueue object.");
        path_ = CURRENT;
    }

    /**
     Constructs a MessageQueue object.  The message queue path defaults to {@link #CURRENT CURRENT}.
     @param  system  The system object representing the server on which the message queue exists.
     @see  #setPath
     **/
    public MessageQueue(AS400 system)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing MessageQueue object, system: " + system);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        system_ = system;
        path_ = CURRENT;
    }

    /**
     Constructs a MessageQueue object.
     @param  system  The system object representing the server on which the message queue exists.
     @param  path  The fully qualified integrated file system path name of the message queue, or {@link #CURRENT CURRENT} to refer to the user's default message queue.
     **/
    public MessageQueue(AS400 system, String path)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing MessageQueue object, system: " + system + " path: " + path);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (path == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'path' is null.");
            throw new NullPointerException("path");
        }
        if (!path.equals(CURRENT))
        {
            // Validate the path and save the name and library.
            QSYSObjectPathName ifs = new QSYSObjectPathName(path, "MSGQ");
            library_ = ifs.getLibraryName();
            name_ = ifs.getObjectName();
        }
        path_ = path;
        system_ = system;
    }

    /**
     Adds a PropertyChangeListener.  The specified PropertyChangeListener's <b>propertyChange()</b> method will be called each time the value of any bound property is changed.
     @param  listener  The listener.
     @see  #removePropertyChangeListener
     **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding property change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        synchronized (this)
        {
            // If first add.
            if (propertyChangeListeners_ == null)
            {
                propertyChangeListeners_ = new PropertyChangeSupport(this);
            }
            propertyChangeListeners_.addPropertyChangeListener(listener);
        }
    }

    /**
     Adds a VetoableChangeListener.  The specified VetoableChangeListener's <b>vetoableChange()</b> method will be called each time the value of any constrained property is changed.
     @param  listener  The listener.
     @see  #removeVetoableChangeListener
     **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding vetoable change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        synchronized (this)
        {
            // If first add.
            if (vetoableChangeListeners_ == null)
            {
                vetoableChangeListeners_ = new VetoableChangeSupport(this);
            }
            vetoableChangeListeners_.addVetoableChangeListener(listener);
        }
    }

    /**
     Closes the message list on the server.  This releases any system resources previously in use by this message list.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public synchronized void close() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Closing message queue message list, handle: ", handle_);
        if (handle_ == null) return;

        ProgramParameter[] parameters = new ProgramParameter[]
        {
            new ProgramParameter(handle_),
            ERROR_CODE
        };
        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYCLST.PGM", parameters);
        if (!pc.run())
        {
            throw new AS400Exception(pc.getMessageList());
        }
        handle_ = null;
        closeHandle_ = false;
    }

    /**
     Closes the message list on the server when this object is garbage collected.
     **/
    protected void finalize() throws Throwable
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Finalize method for message queue invoked.");
        if (system_.isConnected(AS400.COMMAND)) close();
        super.finalize();
    }

    /**
     Returns the status of help text formatting.  Possible values are:
     <ul>
     <li>{@link com.ibm.as400.access.MessageFile#NO_FORMATTING MessageFile.NO_FORMATTING} - The help text is returned as a string of characters.  This is the default.
     <li>{@link com.ibm.as400.access.MessageFile#RETURN_FORMATTING_CHARACTERS MessageFile.RETURN_FORMATTING_CHARACTERS} - The help text contains formatting characters.  The formatting characters are:
     <ul>
     <br>&N -- Force a new line.
     <br>&P -- Force a new line and indent the new line six characters.
     <br>&B -- Force a new line and indent the new line four characters.
     </ul>
     <li>{@link com.ibm.as400.access.MessageFile#SUBSTITUTE_FORMATTING_CHARACTERS MessageFile.SUBSTITUTE_FORMATTING_CHARACTERS} - The MessageFile class replaces formatting characters with newline and space characters.
     </ul>
     @return  The status of help text formatting.
     @see  #setHelpTextFormatting
     **/
    public int getHelpTextFormatting()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting help text formatting:", helpTextFormatting_);
        return helpTextFormatting_;
    }

    /**
     Returns the number of messages in the list.  This method implicitly calls {@link #load load()}.
     @return  The number of messages, or 0 if no list was retrieved.
     @see  #load
     **/
    public int getLength()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting message queue list length.");
        try
        {
            if (handle_ == null || closeHandle_) load();
        }
        catch (Exception e)
        {
            Trace.log(Trace.ERROR, "Exception caught getting length of message queue list:", e);
            if (e instanceof ExtendedIllegalStateException) throw (ExtendedIllegalStateException)e;
        }

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Length:", length_);
        return length_;
    }

    /**
     Returns the list direction.
     @return  true if the messages are listed in order from oldest to newest; false if the messages are listed in order from newest to oldest.
     @see  #setListDirection
     **/
    public boolean getListDirection()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting list direction:", listDirection_);
        return listDirection_;
    }

    /**
     Returns the list of messages in the message queue.  The enumeration retrieves the messages in blocks of 1000.  If this does not yield the desired performance or memory usage, please use the {@link #getMessages(int,int) getMessages()} that returns an array of QueuedMessage objects and accepts a list <i>offset</i> and <i>length</i>.  If an error occurs while the Enumeration is loading the next block of messages, a NoSuchElementException will be thrown while the real error will be logged to {@link com.ibm.as400.access.Trace Trace.ERROR}.
     @return  An Enumeration of {@link com.ibm.as400.access.QueuedMessage QueuedMessage} objects.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public Enumeration getMessages() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving message queue message list.");
        // Need to get the length.
        load();
        return new QueuedMessageEnumeration(this, length_);
    }

    /**
     Returns a subset of the list of messages in the message queue.  This method allows the user to retrieve the message list from the server in pieces.  If a call to {@link #load load()} is made (either implicitly or explicitly), then the messages at a given offset will change, so a subsequent call to getMessages() with the same <i>listOffset</i> and <i>number</i> will most likely not return the same QueuedMessages as the previous call.
     @param  listOffset  The offset into the list of messages.  This value must be greater than or equal to 0 and less than the list length, or specify -1 to retrieve all of the messages.
     @param  number  The number of messages to retrieve out of the list, starting at the specified <i>listOffset</i>.  This value must be greater than or equal to 0 and less than or equal to the list length.  If the <i>listOffset</i> is -1, this parameter is ignored.
     @return  The array of retrieved {@link com.ibm.as400.access.QueuedMessage QueuedMessage} objects.  The length of this array may not necessarily be equal to <i>number</i>, depending upon the size of the list on the server, and the specified <i>listOffset</i>.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     @see  com.ibm.as400.access.QueuedMessage
     **/
    public QueuedMessage[] getMessages(int listOffset, int number) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving message queue message list, list offset: " + listOffset + ", number:", number);
        if (listOffset < -1)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'listOffset' is not valid:", listOffset);
            throw new ExtendedIllegalArgumentException("listOffset (" + listOffset + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        if (number < 0 && listOffset != -1)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'number' is not valid:", number);
            throw new ExtendedIllegalArgumentException("number (" + number + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        if (handle_ == null || closeHandle_) load();

        if (listOffset == -1)
        {
            number = length_;
            listOffset = 0;
        }
        else if (listOffset >= length_)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'listOffset' is not valid:", listOffset);
            throw new ExtendedIllegalArgumentException("listOffset (" + listOffset + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        else if (listOffset + number > length_)
        {
            number = length_ - listOffset;
        }

        if (number == 0) return new QueuedMessage[0];

        int lengthOfReceiverVariable = dataLength_ / length_ * number;

        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Receiver variable, output, char(*).
            new ProgramParameter(lengthOfReceiverVariable),
            // Length of receiver variable, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(lengthOfReceiverVariable)),
            // Request handle, input, char(4).
            new ProgramParameter(handle_),
            // List information, output, char(80).
            new ProgramParameter(80),
            // Number of records to return, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(number)),
            // Starting record, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(listOffset + 1)),
            // Error code, I/O, char(*).
            ERROR_CODE
        };

        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYGTLE.PGM", parameters);

        int recordsReturned = 0;
        do
        {
            if (pc.run())
            {
                byte[] listInformation = parameters[3].getOutputData();
                recordsReturned = BinaryConverter.byteArrayToInt(listInformation, 4);
            }
            else
            {
                AS400Message[] messages = pc.getMessageList();
                // GUI0002 means the receiver variable was too small.
                if (!messages[0].getID().equals("GUI0002")) throw new AS400Exception(messages);
            }

            if (recordsReturned < number)
            {
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieved messages, records returned: " + recordsReturned + ", number:", number);
                lengthOfReceiverVariable *= 1 + number / (recordsReturned + 1);
                if (lengthOfReceiverVariable > dataLength_) lengthOfReceiverVariable = dataLength_;
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Updated length: ", lengthOfReceiverVariable);
                parameters[0] = new ProgramParameter(lengthOfReceiverVariable);
                parameters[1] = new ProgramParameter(BinaryConverter.intToByteArray(lengthOfReceiverVariable));
            }
        } while (recordsReturned < number);

        byte[] data = parameters[0].getOutputData();

        resolveConverter();

        QueuedMessage[] messages = new QueuedMessage[number];
        int offset = 0;
        for (int i = 0; i < messages.length; ++i)
        {
            int entryOffset = BinaryConverter.byteArrayToInt(data, offset);
            int fieldOffset = BinaryConverter.byteArrayToInt(data, offset + 4);
            int numFields = BinaryConverter.byteArrayToInt(data, offset + 8);
            int messageSeverity = BinaryConverter.byteArrayToInt(data, offset + 12);
            String messageIdentifier = conv_.byteArrayToString(data, offset + 16, 7).trim();
            int messageType = (data[offset + 23] & 0x0F) * 10 + (data[offset + 24] & 0x0F);
            if (messageType == 0) messageType = -1;
            byte[] messageKey = new byte[4];
            System.arraycopy(data, offset + 25, messageKey, 0, 4);
            String messageFileName = conv_.byteArrayToString(data, offset + 29, 10).trim();
            String messageFileLibrarySpecified = conv_.byteArrayToString(data, offset + 39, 10).trim();
            String messageQueue = conv_.byteArrayToString(data, offset + 49, 10).trim();
            String messageQueueLibraryUsed = conv_.byteArrayToString(data, offset + 59, 10).trim();
            String dateSent = conv_.byteArrayToString(data, offset + 69, 7);  // CYYMMDD.
            String timeSent = conv_.byteArrayToString(data, offset + 76, 6);  // HHMMSS.
            MessageQueue mq = messageQueueLibraryUsed.length() > 0 && messageQueue.length() > 0 ? new MessageQueue(system_, QSYSObjectPathName.toPath(messageQueueLibraryUsed, messageQueue, "MSGQ")) : this;
            messages[i] = new QueuedMessage(mq, messageSeverity, messageIdentifier, messageType, messageKey, messageFileName, messageFileLibrarySpecified, dateSent, timeSent);

            // Our 7 or 8 fields should've come back.
            for (int j = 0; j < numFields; ++j)
            {
                int offsetToNextField = BinaryConverter.byteArrayToInt(data, fieldOffset);
                int fieldID = BinaryConverter.byteArrayToInt(data, fieldOffset + 8);
                byte type = data[fieldOffset + 12];
                int dataLen = BinaryConverter.byteArrayToInt(data, fieldOffset + 28);
                if (type == (byte)0xC3)  // EBCDIC 'C'.
                {
                    messages[i].setValueInternal(fieldID, conv_.byteArrayToString(data, fieldOffset + 32, dataLen));
                }
                else if (type == (byte)0xC2)  // EBCDIC 'B'.
                {
                    // We should never get here using our standard 8 keys.
                    if (dataLen > 4)
                    {
                        messages[i].setAsLong(fieldID, BinaryConverter.byteArrayToLong(data, fieldOffset + 32));
                    }
                    else
                    {
                        messages[i].setAsInt(fieldID, BinaryConverter.byteArrayToInt(data, fieldOffset + 32));
                    }
                }
                else
                {
                    // We should never get here using our standard 8 keys.
                    // Type = 'M'.  Used for fields 606 and 706.
                    int numStatements = BinaryConverter.byteArrayToInt(data, fieldOffset + 32);
                    String[] statements = new String[numStatements];
                    for (int k = 0; k < numStatements; ++k)
                    {
                        statements[k] = conv_.byteArrayToString(data, fieldOffset + 36 + k * 10, 10);
                    }
                    messages[i].setValueInternal(fieldID, statements);
                }
                fieldOffset = offsetToNextField;
            }

            offset = entryOffset;
        }

        return messages;
    }

    /**
     Returns the fully qualified integrated file system path name of the message queue, or {@link #CURRENT CURRENT} to refer to the user's default message queue.
     @return  The fully qualified integrated file system path name of the message queue, or {@link #CURRENT CURRENT} to refer to the user's default message queue.
     @see  #setPath
     **/
    public String getPath()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting path: " + path_);
        return path_;
    }

    /**
     Returns the selection that describes which messages are returned.
     @return  The selection.  Possible values are:
     <ul>
     <li>{@link #ALL ALL}
     <li>{@link #MESSAGES_NEED_REPLY MESSAGES_NEED_REPLY}
     <li>{@link #MESSAGES_NO_NEED_REPLY MESSAGES_NO_NEED_REPLY}
     <li>{@link #SENDERS_COPY_NEED_REPLY SENDERS_COPY_NEED_REPLY}
     </ul>
     @see  #setSelection
     @deprecated  Use {@link #isSelectMessagesNeedReply isSelectMessagesNeedReply()}, {@link #isSelectMessagesNoNeedReply isSelectMessagesNoNeedReply()}, and {@link #isSelectSendersCopyMessagesNeedReply isSelectSendersCopyMessagesNeedReply()} instead.  The value returned by this method may not accurately reflect the actual selection criteria used to filter the list of messages.
     **/
    public String getSelection()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting selection: " + selection_);
        return selection_;
    }

    /**
     Returns the severity of the messages which are returned.
     @return  The severity of the messages which are returned.
     @see  #setSeverity
     **/
    public int getSeverity()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting severity:", severity_);
        return severity_;
    }

    /**
     Returns whether or not messages will be sorted when {@link #ALL ALL} is specified for the selection criteria.  The default is false.
     @return  true if messages will be sorted by message type; false otherwise.
     @see  #setSort
     **/
    public boolean getSort()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting sort:", sort_);
        return sort_;
    }

    /**
     Returns the system object representing the server on which the message queue exists.
     @return  The system object representing the server on which the message queue exists.  If the system has not been set, null is returned.
     **/
    public AS400 getSystem()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system: " + system_);
        return system_;
    }

    /**
     Returns the user starting message key, if one has been set.
     @return  The key, or null if none has been set.
     @see  #setUserStartingMessageKey
     **/
    public byte[] getUserStartingMessageKey()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting user starting message key:", userStartingMessageKey_);
        return userStartingMessageKey_;
    }

    /**
     Returns the workstation starting message key, if one has been set.
     @return  The key, or null if none has been set.
     @see  #setWorkstationStartingMessageKey
     **/
    public byte[] getWorkstationStartingMessageKey()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting workstation starting message key:", workstationStartingMessageKey_);
        return workstationStartingMessageKey_;
    }

    /**
     Returns whether or not messages that need a reply are included in the list of returned messages.  If all three message selection getters return true, it is the equivalent of all messages being included in the list of returned messages.  By default, all messages are returned, so this method returns true.
     @return  true if messages that need a reply are included in the list of returned messages; false if messages that need a reply are excluded from the list of returned messages.
     @see  #isSelectMessagesNoNeedReply
     @see  #isSelectSendersCopyMessagesNeedReply
     @see  #setSelectMessagesNeedReply
     **/
    public boolean isSelectMessagesNeedReply()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if messages that need a reply are selected:", selectMessagesNeedReply_);
        return selectMessagesNeedReply_;
    }

    /**
     Returns whether or not messages that do not need a reply are included in the list of returned messages.  If all three message selection getters return true, it is the equivalent of all messages being included in the list of returned messages.  By default, all messages are returned, so this method returns true.
     @return  true if messages that do not need a reply are included in the list of returned messages; false if messages that do not need a reply are excluded from the list of returned messages.
     @see  #isSelectMessagesNeedReply
     @see  #isSelectSendersCopyMessagesNeedReply
     @see  #setSelectMessagesNoNeedReply
     **/
    public boolean isSelectMessagesNoNeedReply()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if messages that do not need a reply are selected:", selectMessagesNoNeedReply_);
        return selectMessagesNoNeedReply_;
    }

    /**
     Returns whether or not sender's copy messages that need a reply are included in the list of returned messages.  If all three message selection getters return true, it is the equivalent of all messages being included in the list of returned messages.  By default, all messages are returned, so this method returns true.
     @return  true if sender's copy messages that need a reply are included in the list of returned messages; false if sender's copy messages that need a reply are excluded from the list of returned messages.
     @see  #isSelectMessagesNeedReply
     @see  #isSelectMessagesNoNeedReply
     @see  #setSelectSendersCopyMessagesNeedReply
     **/
    public boolean isSelectSendersCopyMessagesNeedReply()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if sender's copy messages that need a reply are selected:", selectSendersCopyMessagesNeedReply_);
        return selectSendersCopyMessagesNeedReply_;
    }

    /**
     Loads the list of messages on the server.  This method informs the server to build a list of messages given the previously added attributes to select, retrieve, and sort.  This method blocks until the server returns the total number of messages it has compiled.  A subsequent call to {@link #getMessages getMessages()} will retrieve the actual message information and attributes for each message in the list from the server.
     <p>This method updates the list length.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     @see  #getLength
     **/
    public synchronized void load() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Loading message queue message list.");
        if (system_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot connect to server before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        // Close the previous list.
        if (closeHandle_) close();

        boolean v5r3OrGreater = system_.getVRM() >= 0x00050300;

        // Create the message selection information.
        boolean selectAll = (selectMessagesNeedReply_ && selectMessagesNoNeedReply_ && selectSendersCopyMessagesNeedReply_) || (!selectMessagesNeedReply_ && !selectMessagesNoNeedReply_ && !selectSendersCopyMessagesNeedReply_);
        int numberOfSelectionCriteria = selectAll ? 1 : (selectMessagesNeedReply_ ? 1 : 0) + (selectMessagesNoNeedReply_ ? 1 : 0) + (selectSendersCopyMessagesNeedReply_ ? 1 : 0);
        int numberOfFieldsToReturn = v5r3OrGreater ? 8 : 7;
        byte[] messageSelectionInformation = new byte[52 + 10 * numberOfSelectionCriteria + 4 * numberOfFieldsToReturn];
        if (listDirection_)
        {
            // EBCDIC '*NEXT'.
            System.arraycopy(new byte[] { 0x5C, (byte)0xD5, (byte)0xC5, (byte)0xE7, (byte)0xE3, 0x40, 0x40, 0x40, 0x40, 0x40 }, 0, messageSelectionInformation, 0, 10);
        }
        else
        {
            // EBCDIC '*PRV'.
            System.arraycopy(new byte[] { 0x5C, (byte)0xD7, (byte)0xD9, (byte)0xE5, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 }, 0, messageSelectionInformation, 0, 10);
        }
        BinaryConverter.intToByteArray(severity_, messageSelectionInformation, 12);
        // Set -1 into Maximum message length and Maximum message help length.
        for (int i = 16; i < 24; ++i) messageSelectionInformation[i] = (byte)0xFF;
        // Offset of selection criteria is 44.
        BinaryConverter.intToByteArray(44, messageSelectionInformation, 24);
        BinaryConverter.intToByteArray(numberOfSelectionCriteria, messageSelectionInformation, 28);
        int offset = 44 + numberOfSelectionCriteria * 10;
        // Offset of starting message keys.
        BinaryConverter.intToByteArray(offset, messageSelectionInformation, 32);
        // Offset of identifiers of fields to return.
        BinaryConverter.intToByteArray(offset + 8, messageSelectionInformation, 36);
        // Number of fields to return.
        BinaryConverter.intToByteArray(numberOfFieldsToReturn, messageSelectionInformation, 40);
        if (selectAll)
        {
            // EBCDIC '*ALL'.
            System.arraycopy(new byte[] { 0x5C, (byte)0xC1, (byte)0xD3, (byte)0xD3, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 }, 0, messageSelectionInformation, 44, 10);
        }
        else
        {
            int selectionCriteriaOffset = 44;
            if (selectMessagesNeedReply_)
            {
                // EBCDIC '*MNR'.
                System.arraycopy(new byte[] { 0x5C, (byte)0xD4, (byte)0xD5, (byte)0xD9, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 }, 0, messageSelectionInformation, selectionCriteriaOffset, 10);
                selectionCriteriaOffset += 10;
            }
            if (selectMessagesNoNeedReply_)
            {
                // EBCDIC '*MNNR'.
                System.arraycopy(new byte[] { 0x5C, (byte)0xD4, (byte)0xD5, (byte)0xD5, (byte)0xD9, 0x40, 0x40, 0x40, 0x40, 0x40 }, 0, messageSelectionInformation, selectionCriteriaOffset, 10);
                selectionCriteriaOffset += 10;
            }
            if (selectSendersCopyMessagesNeedReply_)
            {
                // EBCDIC '*SCNR'.
                System.arraycopy(new byte[] { 0x5C, (byte)0xE2, (byte)0xC3, (byte)0xD5, (byte)0xD9, 0x40, 0x40, 0x40, 0x40, 0x40 }, 0, messageSelectionInformation, selectionCriteriaOffset, 10);
            }
        }
        byte[] userStartingMessageKey = (userStartingMessageKey_ != null ? userStartingMessageKey_ : (listDirection_ ? OLDEST : NEWEST));
        System.arraycopy(userStartingMessageKey, 0, messageSelectionInformation, offset, 4);
        byte[] workstationStartingMessageKey = (workstationStartingMessageKey_ != null ? workstationStartingMessageKey_ : userStartingMessageKey);
        System.arraycopy(workstationStartingMessageKey, 0, messageSelectionInformation, offset + 4, 4);
        // Message with replacement data.
        BinaryConverter.intToByteArray(302, messageSelectionInformation, offset + 8);
        // Qualified sender job name.
        BinaryConverter.intToByteArray(601, messageSelectionInformation, offset + 12);
        // Sending program name.
        BinaryConverter.intToByteArray(603, messageSelectionInformation, offset + 16);
        // Reply status.
        BinaryConverter.intToByteArray(1001, messageSelectionInformation, offset + 20);
        // Default reply.
        BinaryConverter.intToByteArray(501, messageSelectionInformation, offset + 24);
        // Message help with replacement data and formattting characters.
        BinaryConverter.intToByteArray(404, messageSelectionInformation, offset + 28);
        // Alert option.
        BinaryConverter.intToByteArray(101, messageSelectionInformation, offset + 32);
        if (v5r3OrGreater)
        {
            // Sending user profile.
            BinaryConverter.intToByteArray(607, messageSelectionInformation, offset + 36);
        }

        // Create the user or queue information.
        byte[] userOrQueueInformation;
        if (path_.equals(CURRENT))
        {
            // EBCDIC '0' followed by EBCDIC '*CURRENT'.
            userOrQueueInformation = new byte[] { (byte)0xF0, 0x5C, (byte)0xC3, (byte)0xE4, (byte)0xD9, (byte)0xD9, (byte)0xC5, (byte)0xD5, (byte)0xE3, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        }
        else
        {
            // The message queue name is used.
            userOrQueueInformation = new byte[] { (byte)0xF1, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
            resolveConverter();
            conv_.stringToByteArray(name_, userOrQueueInformation, 1);
            conv_.stringToByteArray(library_, userOrQueueInformation, 11);
        }

        // Setup program parameters.
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Receiver variable, output, char(*).
            new ProgramParameter(0),
            // Length of receiver variable, input, binary(4).
            new ProgramParameter(new byte[] { 0x00, 0x00, 0x00, 0x00 } ),
            // List information, output, char(80).
            new ProgramParameter(80),
            // Number of records to return, input, binary(4).
            new ProgramParameter(new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF } ),
            // Sort information, input, char(1).
            // Sort information, '0' = no sort, '1' = sort if *ALL is specified
            new ProgramParameter(new byte[] { sort_ && selectAll ? (byte)0xF1 : (byte)0xF0 } ),
            // Message selection information, input, char(*).
            new ProgramParameter(messageSelectionInformation),
            // Size of message selection information, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(messageSelectionInformation.length)),
            // User or queue information, input, char(21).
            new ProgramParameter(userOrQueueInformation),
            // Message queues used, output, char(44).
            new ProgramParameter(44),
            // Error code, I/O, char(*).
            ERROR_CODE
        };

        // Call the program.
        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYOLMSG.PGM", parameters);
        if (!pc.run())
        {
            throw new AS400Exception(pc.getMessageList());
        }

        // List information returned.
        byte[] listInformation = parameters[2].getOutputData();
        // Check the list status indicator.
        ListUtilities.checkListStatus(listInformation[30]);

        handle_ = new byte[4];
        System.arraycopy(listInformation, 8, handle_, 0, 4);
        length_ = BinaryConverter.byteArrayToInt(listInformation, 0);
        dataLength_ = BinaryConverter.byteArrayToInt(listInformation, 32);

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Loaded message queue message list, length: " + length_ + ", data length: " + dataLength_ + ", handle: ", handle_);
    }

    /**
     Receives a message from the message queue by key.  This method receives a message of any type except sender's copy.  The message is removed from the message queue.  See the <a href="#receiveIDs">list of QueuedMessage attribute values</a> which are set on a received message.
     @param  messageKey  The message key.
     @return  The queued message, or null if the message can not be received.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     @see  com.ibm.as400.access.QueuedMessage#getKey
     **/
    public QueuedMessage receive(byte[] messageKey) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Receiving from message queue, message key:", messageKey);
        return receive(messageKey, 0, REMOVE, ANY);
    }

    /**
     Receives a message from the message queue.  See the <a href="#receiveIDs">list of QueuedMessage attribute values</a> which are set on a received message.
     @param  messageKey  The message key, or null if no message key is needed.
     @param  waitTime  The number of seconds to wait for the message to arrive in the queue so it can be received.  If the message is not received within the specified wait time, null is returned.  Special values are:
     <ul>
     <li>0 - Do not wait for the message. If the message is not in the queue and you specified a message key, null is returned.
     <li>-1 - Wait until the message arrives in the queue and is received, no matter how long it takes.  The system has no limit for the wait time.
     </ul>
     @param  messageAction  The action to take after the message is received. Valid values are:
     <ul>
     <li>OLD - Keep the message in the message queue and mark it as an old message.  You can receive the message again only by using the message key or by specifying the message type NEXT, PREVIOUS, FIRST, or LAST.
     <li>REMOVE - Remove the message from the message queue.  The message key is no longer valid, so you cannot receive the message again.
     <li>SAME - Keep the message in the message queue without changing its new or old designation.  SAME lets you receive the message again later without using the message key.
     </ul>
     @param  messageType  The type of message to return.  Valid values are:
     <ul>
     <li>ANY - Receives a message of any type except sender's copy.  The message key is optional.
     <li>COMPLETION - Receives a completion message.  The message key is optional.
     <li>COPY - Receives the sender's copy of a previously sent inquiry message.  The message key is required.
     <li>DIAGNOSTIC - Receives a diagnostic message.  The message key is optional.
     <li>FIRST - Receives the first new or old message in the queue.  The message key is disallowed.
     <li>INFORMATIONAL - Receives an informational message.  The message key is optional.
     <li>INQUIRY - Receives an inquiry message.  If the action is REMOVE and a reply to the inquiry message has not been sent yet, the default reply is automatically sent when the inquiry message is received.  The message key is optional.
     <li>LAST - Receives the last new or old message in the queue.  The message key is disallowed.
     <li>NEXT - Receives the next new or old message after the message with the specified key.  You can use the special value TOP for the message key.  TOP designates the message at the top of the message queue.  The message key is required.
     <li>PREVIOUS - Receives the new or old message before the message with the specified key.  The message key is required.
     <li>REPLY - Receives the reply to an inquiry message.  For the message key, you can use the key to the sender's copy of the inquiry or notify message.  The message key is optional.
     </ul>
     @return  The queued message, or null if the message can not be received.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     @see  com.ibm.as400.access.QueuedMessage#getKey
     **/
    public QueuedMessage receive(byte[] messageKey, int waitTime, String messageAction, String messageType) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Receiving from message queue, waitTime: " + waitTime + ", messageAction: " + messageAction + ", messageType: " + messageType + ", message key:", messageKey);
        if (messageAction == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageAction' is null.");
            throw new NullPointerException("messageAction");
        }
        if (messageType == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageType' is null.");
            throw new NullPointerException("messageType");
        }

        byte[] messageActionBytes;
        if (messageAction.equals(OLD))
        {
            // EBCDIC '*OLD'.
            messageActionBytes = new byte[] { 0x5C, (byte)0xD6, (byte)0xD3, (byte)0xC4, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        }
        else if (messageAction.equals(REMOVE))
        {
            // EBCDIC '*REMOVE'.
            messageActionBytes = new byte[] { 0x5C, (byte)0xD9, (byte)0xC5, (byte)0xD4, (byte)0xD6, (byte)0xE5, (byte)0xC5, 0x40, 0x40, 0x40 };
        }
        else if (messageAction.equals(SAME))
        {
            // EBCDIC '*SAME'.
            messageActionBytes = new byte[] { 0x5C, (byte)0xE2, (byte)0xC1, (byte)0xD4, (byte)0xC5, 0x40, 0x40, 0x40, 0x40, 0x40 };
        }
        else
        {
            Trace.log(Trace.ERROR, "Value of parameter 'messageAction' is not valid: " + messageAction);
            throw new ExtendedIllegalArgumentException("messageAction (" + messageAction + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        byte[] messageTypeBytes;
        if (messageType.equals(ANY))
        {
            // EBCDIC '*ANY'.
            messageTypeBytes = new byte[] { 0x5C, (byte)0xC1, (byte)0xD5, (byte)0xE8, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        }
        else if (messageType.equals(COMPLETION))
        {
            // EBCDIC '*COMP'.
            messageTypeBytes = new byte[] { 0x5C, (byte)0xC3, (byte)0xD6, (byte)0xD4, (byte)0xD7, 0x40, 0x40, 0x40, 0x40, 0x40 };
        }
        else if (messageType.equals(COPY))
        {
            // EBCDIC '*COPY'.
            messageTypeBytes = new byte[] { 0x5C, (byte)0xC3, (byte)0xD6, (byte)0xD7, (byte)0xE8, 0x40, 0x40, 0x40, 0x40, 0x40 };
        }
        else if (messageType.equals(DIAGNOSTIC))
        {
            // EBCDIC '*DIAG'.
            messageTypeBytes = new byte[] { 0x5C, (byte)0xC4, (byte)0xC9, (byte)0xC1, (byte)0xC7, 0x40, 0x40, 0x40, 0x40, 0x40 };
        }
        else if (messageType.equals(FIRST))
        {
            // EBCDIC '*FIRST'.
            messageTypeBytes = new byte[] { 0x5C, (byte)0xC6, (byte)0xC9, (byte)0xD9, (byte)0xE2, (byte)0xE3, 0x40, 0x40, 0x40, 0x40 };
        }
        else if (messageType.equals(INFORMATIONAL))
        {
            // EBCDIC '*INFO'.
            messageTypeBytes = new byte[] { 0x5C, (byte)0xC9, (byte)0xD5, (byte)0xC6, (byte)0xD6, 0x40, 0x40, 0x40, 0x40, 0x40 };
        }
        else if (messageType.equals(INQUIRY))
        {
            // EBCDIC '*INQ'.
            messageTypeBytes = new byte[] { 0x5C, (byte)0xC9, (byte)0xD5, (byte)0xD8, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        }
        else if (messageType.equals(LAST))
        {
            // EBCDIC '*LAST'.
            messageTypeBytes = new byte[] { 0x5C, (byte)0xD3, (byte)0xC1, (byte)0xE2, (byte)0xE3, 0x40, 0x40, 0x40, 0x40, 0x40 };
        }
        else if (messageType.equals(NEXT))
        {
            // EBCDIC '*NEXT'.
            messageTypeBytes = new byte[] { 0x5C, (byte)0xD5, (byte)0xC5, (byte)0xE7, (byte)0xE3, 0x40, 0x40, 0x40, 0x40, 0x40 };
        }
        else if (messageType.equals(PREVIOUS))
        {
            // EBCDIC '*PRV'.
            messageTypeBytes = new byte[] { 0x5C, (byte)0xD7, (byte)0xD9, (byte)0xE5, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        }
        else if (messageType.equals(REPLY))
        {
            // EBCDIC '*RPY'.
            messageTypeBytes = new byte[] { 0x5C, (byte)0xD9, (byte)0xD7, (byte)0xE8, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        }
        else
        {
            Trace.log(Trace.ERROR, "Value of parameter 'messageType' is not valid: " + messageType);
            throw new ExtendedIllegalArgumentException("messageType (" + messageType + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        if (messageKey == null && (messageType.equals(COPY) || messageType.equals(NEXT) || messageType.equals(PREVIOUS)))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'messageKey' is not valid: " + messageKey);
            throw new ExtendedIllegalArgumentException("messageKey (" + messageKey + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        if (system_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot connect to server before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        // Use all spaces for null key.
        if (messageKey == null) messageKey = BLANK_KEY;

        resolveQualifiedMessageQueueName();

        // Setup program parameters.
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Message information, output, char(*).
            new ProgramParameter(5120),
            // Length of message information, input, binary(4).
            new ProgramParameter(new byte[] { 0x00, 0x00, 0x14, 0x00 } ),
            // Format name, input, char(8), EBCDIC 'RCVM0200'.
            new ProgramParameter(new byte[] { (byte)0xD9, (byte)0xC3, (byte)0xE5, (byte)0xD4, (byte)0xF0, (byte)0xF2, (byte)0xF0, (byte)0xF0 } ),
            // Qualified message queue name, input, char(20).
            new ProgramParameter(qualifiedMessageQueueName_),
            // Message type, input, char(10).
            new ProgramParameter(messageTypeBytes),
            // Message key, input, char(4).
            new ProgramParameter(messageKey),
            // Wait time, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(waitTime)),
            // Message action, input, char(10).
            new ProgramParameter(messageActionBytes),
            // Error code, I/O, char(*).
            ERROR_CODE
        };
        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QMHRCVM.PGM", parameters);
        if (!pc.run())
        {
            throw new AS400Exception(pc.getMessageList());
        }

        byte[] data = parameters[0].getOutputData();

        int bytesReturned = BinaryConverter.byteArrayToInt(data, 0);
        int bytesAvailable = BinaryConverter.byteArrayToInt(data, 4);

        if (bytesReturned == 8 && bytesAvailable == 0) return null; // No message found.

        if (Trace.traceOn_ && bytesReturned < bytesAvailable) Trace.log(Trace.WARNING, "Possible truncation receiving from message queue, bytes returned: " + bytesReturned + ", bytesAvailable:", bytesAvailable);

        resolveConverter();

        int messageSeverity = BinaryConverter.byteArrayToInt(data, 8);
        // Blank for impromptu message.
        String messageIdentifier = conv_.byteArrayToString(data, 12, 7).trim();

        int messageTypeReturned = (data[19] & 0x0F) * 10 + (data[20] & 0x0F);
        byte[] messageKeyReturned = new byte[4];
        // Blank for a message action of *REMOVE.
        System.arraycopy(data, 21, messageKeyReturned, 0, 4);
        String messageFileName = conv_.byteArrayToString(data, 25, 10).trim();
        String messageFileLibraryUsed = conv_.byteArrayToString(data, 45, 10).trim();

        String sendingJob = conv_.byteArrayToString(data, 55, 10).trim();
        String sendingJobsUserProfile = conv_.byteArrayToString(data, 65, 10).trim();
        String sendingJobNumber = conv_.byteArrayToString(data, 75, 6);
        String sendingProgramName = conv_.byteArrayToString(data, 81, 12).trim();
        String dateSent = conv_.byteArrayToString(data, 97, 7);  // CYYMMDD.
        String timeSent = conv_.byteArrayToString(data, 104, 6);  // HHMMSS.
        String alertOption = conv_.byteArrayToString(data, 135, 9).trim();

        int lengthDataReturned = BinaryConverter.byteArrayToInt(data, 152);
        int lengthMessageReturned = BinaryConverter.byteArrayToInt(data, 160);
        int lengthHelpReturned = BinaryConverter.byteArrayToInt(data, 168);

        byte[] replacementDataBytes = new byte[lengthDataReturned];
        System.arraycopy(data, 176, replacementDataBytes, 0, lengthDataReturned);

        String messageData = lengthMessageReturned != 0 ? conv_.byteArrayToString(data, 176 + lengthDataReturned, lengthMessageReturned) : conv_.byteArrayToString(replacementDataBytes, 0, lengthDataReturned, 0);

        String messageHelp = lengthHelpReturned != 0 ? conv_.byteArrayToString(data, 176 + lengthDataReturned + lengthMessageReturned, lengthHelpReturned) : null;

        QueuedMessage message = new QueuedMessage(this, messageSeverity, messageIdentifier, messageTypeReturned, messageKeyReturned, messageFileName, messageFileLibraryUsed, sendingJob, sendingJobsUserProfile, sendingJobNumber, sendingProgramName, dateSent, timeSent, replacementDataBytes, messageData, messageHelp, alertOption);
        synchronized (this)
        {
            if (handle_ != null) closeHandle_ = true;
        }
        return message;
    }

    /**
     Remove all messages from the message queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public void remove() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // EBCDIC '*ALL'.
        removeMessages(new byte[] { 0x5C, (byte)0xC1, (byte)0xD3, (byte)0xD3, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 }, BLANK_KEY);
    }

    /**
     Removes a message from the message queue.
     @param  messageKey  The message key.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public void remove(byte[] messageKey) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (messageKey == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageKey' is null.");
            throw new NullPointerException("messageKey");
        }
        // EBCDIC '*BYKEY'.
        removeMessages(new byte[] { 0x5C, (byte)0xC2, (byte)0xE8, (byte)0xD2, (byte)0xC5, (byte)0xE8, 0x40, 0x40, 0x40, 0x40 }, messageKey);
    }

    /**
     Remove messages from the message queue.
     @param  messageType  The type of message to remove.  Valid values are:
     <ul>
     <li>ALL - All messages in the message queue.
     <li>KEEP_UNANSWERED - All messages in the message queue except unanswered inquiry and unanswered senders' copy messages.
     <li>NEW - All new messages in the message queue.  New messages are those that have not been received.
     <li>OLD - All old messages in the message queue.  Old messages are those that have already been received.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public void remove(String messageType) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (messageType == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageType' is null.");
            throw new NullPointerException("messageType");
        }
        byte[] messageTypeBytes;
        if (messageType.equals(ALL))
        {
            // EBCDIC '*ALL'.
            messageTypeBytes = new byte[] { 0x5C, (byte)0xC1, (byte)0xD3, (byte)0xD3, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        }
        else if (messageType.equals(KEEP_UNANSWERED))
        {
            // EBCDIC '*KEEPUNANS'.
            messageTypeBytes = new byte[] { 0x5C, (byte)0xD2, (byte)0xC5, (byte)0xC5, (byte)0xD7, (byte)0xE4, (byte)0xD5, (byte)0xC1, (byte)0xD5, (byte)0xE2 };
        }
        else if (messageType.equals(NEW))
        {
            // EBCDIC '*NEW'.
            messageTypeBytes = new byte[] { 0x5C, (byte)0xD5, (byte)0xC5, (byte)0xE6, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        }
        else if (messageType.equals(OLD))
        {
            // EBCDIC '*OLD'.
            messageTypeBytes = new byte[] { 0x5C, (byte)0xD6, (byte)0xD3, (byte)0xC4, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        }
        else
        {
            Trace.log(Trace.ERROR, "Value of parameter 'messageType' is not valid: " + messageType);
            throw new ExtendedIllegalArgumentException("messageType (" + messageType + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        removeMessages(messageTypeBytes, BLANK_KEY);
    }

    // Helper method.
    private void removeMessages(byte[] messageType, byte[] messageKey) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        resolveQualifiedMessageQueueName();

        // Setup program parameters.
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Qualified message queue name, input, char(20).
            new ProgramParameter(qualifiedMessageQueueName_),
            // Message key, input, char(4).
            new ProgramParameter(messageKey),
            // Messages to remove, input, char(10).
            new ProgramParameter(messageType),
            // Error code, I/O, char(*).
            ERROR_CODE
        };

        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QMHRMVM.PGM", parameters);
        if (!pc.run())
        {
            throw new AS400Exception(pc.getMessageList());
        }
        synchronized (this)
        {
            if (handle_ != null) closeHandle_ = true;
        }
    }

    /**
     Removes the PropertyChangeListener.  If the PropertyChangeListener is not on the list, nothing is done.
     @param  listener  The listener object.
     **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing property change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        // If we have listeners.
        if (propertyChangeListeners_ != null)
        {
            propertyChangeListeners_.removePropertyChangeListener(listener);
        }
    }

    /**
     Removes the VetoableChangeListener.  If the VetoableChangeListener is not on the list, nothing is done.
     @param  listener  The listener object.
     **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing vetoable change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        // If we have listeners.
        if (vetoableChangeListeners_ != null)
        {
            vetoableChangeListeners_.removeVetoableChangeListener(listener);
        }
    }

    // Cache the converter object for reuse.
    private synchronized void resolveConverter() throws IOException
    {
        if (conv_ == null) conv_ = new Converter(system_.getCcsid(), system_);
    }

    // Cache the converted qualified message queue name for reuse.
    private synchronized void resolveQualifiedMessageQueueName() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (qualifiedMessageQueueName_ != null) return;
        resolveConverter();
        qualifiedMessageQueueName_ = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        if (path_.equals(CURRENT))
        {
            // Need to retrieve the message queue name from the user profile object.
            User user = new User(system_, system_.getUserId());
            user.loadUserInformation();
            QSYSObjectPathName ifs = new QSYSObjectPathName(user.getMessageQueue());
            conv_.stringToByteArray(ifs.getObjectName(), qualifiedMessageQueueName_, 0);
            conv_.stringToByteArray(ifs.getLibraryName(), qualifiedMessageQueueName_, 10);
        }
        else
        {
            conv_.stringToByteArray(name_, qualifiedMessageQueueName_, 0);
            conv_.stringToByteArray(library_, qualifiedMessageQueueName_, 10);
        }
    }

    /**
     Replies to and removes a message.
     @param  messageKey  The message key.
     @param  replyText  The reply.  To send the default reply stored in the message description, use blanks for this parameter.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public void reply(byte[] messageKey, String replyText) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        reply(messageKey, replyText, true);
    }

    /**
     Replies to and removes a message if requested.
     @param  messageKey  The message key.
     @param  replyText  The reply.  To send the default reply stored in the message description, use blanks for this parameter.
     @param  remove  true to remove the inquiry message and the reply from the message queue after the reply is sent, false to keep the inquiry message and the reply after the reply is sent.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public void reply(byte[] messageKey, String replyText, boolean remove) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (messageKey == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageKey' is null.");
            throw new NullPointerException("messageKey");
        }
        if (replyText == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'replyText' is null.");
            throw new NullPointerException("replyText");
        }

        resolveConverter();
        byte[] replyBytes = conv_.stringToByteArray(replyText);
        // Set remove to EBCDIC '*YES' or EBCDIC '*NO'.
        byte[] removeBytes = remove ? new byte[] { 0x5C, (byte)0xE8, (byte)0xC5, (byte)0xE2, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 } :  new byte[] { 0x5C, (byte)0xD5, (byte)0xD6, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };

        resolveQualifiedMessageQueueName();

        // Setup program parameters.
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Message key, input, char(4).
            new ProgramParameter(messageKey),
            // Qualified message queue name, input, char(20).
            new ProgramParameter(qualifiedMessageQueueName_),
            // Reply text, input, char(*).
            new ProgramParameter(replyBytes),
            // Length of reply text, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(replyBytes.length)),
            // Remove message, input, char(10).
            new ProgramParameter(removeBytes),
            // Error code, I/O, char(*).
            ERROR_CODE
        };

        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QMHSNDRM.PGM", parameters);
        if (!pc.run())
        {
            throw new AS400Exception(pc.getMessageList());
        }
        synchronized (this)
        {
            if (handle_ != null) closeHandle_ = true;
        }
    }

    // Helper method.
    private byte[] send(String messageID, String messageFile, byte[] data, byte[] messageType, String replyQueue) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        resolveConverter();
        byte[] idBytes = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        if (messageID != null)
        {
            conv_.stringToByteArray(messageID, idBytes, 0, 7);
        }
        byte[] messageFileName = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        if (messageFile != null)
        {
            QSYSObjectPathName ifs = new QSYSObjectPathName(messageFile, "MSGF");
            conv_.stringToByteArray(ifs.getObjectName(), messageFileName, 0);
            conv_.stringToByteArray(ifs.getLibraryName(), messageFileName, 10);
        }
        if (data == null) data = new byte[0];
        byte[] messageQueueName = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        if (path_.equals(CURRENT))
        {
            conv_.stringToByteArray(system_.getUserId(), messageQueueName, 0);
            // Library is EBCDIC '*USER'.
            System.arraycopy(new byte[] { 0x5C, (byte)0xE4, (byte)0xE2, (byte)0xC5, (byte)0xD9 }, 0, messageQueueName, 10, 5);
        }
        else
        {
            QSYSObjectPathName ifs = new QSYSObjectPathName(path_);
            conv_.stringToByteArray(ifs.getObjectName(), messageQueueName, 0);
            conv_.stringToByteArray(ifs.getLibraryName(), messageQueueName, 10);
        }
        byte[] replyMessageQueueName = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        if (replyQueue != null)
        {
            QSYSObjectPathName ifs = new QSYSObjectPathName(replyQueue, "MSGQ");
            conv_.stringToByteArray(ifs.getObjectName(), replyMessageQueueName, 0);
            conv_.stringToByteArray(ifs.getLibraryName(), replyMessageQueueName, 10);
        }

        // Setup program parameters.
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Message identifier, input, char(7).
            new ProgramParameter(idBytes),
            // Qualified message file name, input, char(20).
            new ProgramParameter(messageFileName),
            // Message data or immediate text, input, char(*).
            new ProgramParameter(data),
            // Length of message data or immediate text, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(data.length)),
            // Message type, input, char(10).
            new ProgramParameter(messageType),
            // List of qualified message queue names, input, array of char(20).
            new ProgramParameter(messageQueueName),
            // Number of message queues, input, binary(4).
            new ProgramParameter(new byte[] { 0x00, 0x00, 0x00, 0x01 } ),
            // Qualified name of the reply message queue, input, char(20).
            new ProgramParameter(replyMessageQueueName),
            // Message key, output, char(4).
            new ProgramParameter(4),
            // Error code, I/O, char(*).
            ERROR_CODE
        };
        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QMHSNDM.PGM", parameters);
        if (!pc.run())
        {
            throw new AS400Exception(pc.getMessageList());
        }

        synchronized (this)
        {
            if (handle_ != null) closeHandle_ = true;
        }
        // Return the message key.
        return parameters[8].getOutputData();
    }

    /**
     Sends an informational message to the message queue.
     @param  messageID  The message ID.
     @param  messageFile  The integrated file system path name of the message file.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public void sendInformational(String messageID, String messageFile) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (messageID == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageID' is null.");
            throw new NullPointerException("messageID");
        }
        if (messageFile == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageFile' is null.");
            throw new NullPointerException("messageFile");
        }
        // EBCDIC '*INFO'.
        send(messageID, messageFile, null, new byte[] { 0x5C, (byte)0xC9, (byte)0xD5, (byte)0xC6, (byte)0xD6, 0x40, 0x40, 0x40, 0x40, 0x40 }, null);
    }

    /**
     Sends an informational message to the message queue.
     @param  messageID  The message ID.
     @param  messageFile  The integrated file system path name of the message file.
     @param  substitutionData  The substitution data for the message, or null if none.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public void sendInformational(String messageID, String messageFile, byte[] substitutionData) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (messageID == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageID' is null.");
            throw new NullPointerException("messageID");
        }
        if (messageFile == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageFile' is null.");
            throw new NullPointerException("messageFile");
        }
        // EBCDIC '*INFO'.
        send(messageID, messageFile, substitutionData, new byte[] { 0x5C, (byte)0xC9, (byte)0xD5, (byte)0xC6, (byte)0xD6, 0x40, 0x40, 0x40, 0x40, 0x40 }, null);
    }

    /**
     Sends an informational message to the message queue.
     @param  messageText  The message text.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public void sendInformational(String messageText) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (messageText == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageText' is null.");
            throw new NullPointerException("messageText");
        }
        resolveConverter();
        // EBCDIC '*INFO'.
        send(null, null, conv_.stringToByteArray(messageText), new byte[] { 0x5C, (byte)0xC9, (byte)0xD5, (byte)0xC6, (byte)0xD6, 0x40, 0x40, 0x40, 0x40, 0x40 }, null);
    }

    /**
     Sends an inquiry message to the message queue.
     @param  messageID  The message ID.
     @param  messageFile  The integrated file system path name of the message file.
     @param  replyMessageQueue  The integrated file system path name of the reply message queue.
     @return  The message key.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public byte[] sendInquiry(String messageID, String messageFile, String replyMessageQueue) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (messageID == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageID' is null.");
            throw new NullPointerException("messageID");
        }
        if (messageFile == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageFile' is null.");
            throw new NullPointerException("messageFile");
        }
        if (replyMessageQueue == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'replyMessageQueue' is null.");
            throw new NullPointerException("replyMessageQueue");
        }
        // EBCDIC '*INQ'.
        return send(messageID, messageFile, null, new byte[] { 0x5C, (byte)0xC9, (byte)0xD5, (byte)0xD8, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 }, replyMessageQueue);
    }

    /**
     Sends an inquiry message to the message queue.
     @param  messageID  The message ID.
     @param  messageFile  The integrated file system path name of the message file.
     @param  substitutionData  The substitution data for the message, or null if none.
     @param  replyMessageQueue  The integrated file system path name of the reply message queue.
     @return  The message key.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public byte[] sendInquiry(String messageID, String messageFile, byte[] substitutionData, String replyMessageQueue) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (messageID == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageID' is null.");
            throw new NullPointerException("messageID");
        }
        if (messageFile == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageFile' is null.");
            throw new NullPointerException("messageFile");
        }
        if (replyMessageQueue == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'replyMessageQueue' is null.");
            throw new NullPointerException("replyMessageQueue");
        }
        // EBCDIC '*INQ'.
        return send(messageID, messageFile, substitutionData, new byte[] { 0x5C, (byte)0xC9, (byte)0xD5, (byte)0xD8, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 }, replyMessageQueue);
    }

    /**
     Sends an inquiry message to the message queue.
     @param  messageText  The message text.
     @param  replyMessageQueue  The integrated file system path name of the reply message queue.
     @return  The message key.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public byte[] sendInquiry(String messageText, String replyMessageQueue) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (messageText == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageText' is null.");
            throw new NullPointerException("messageText");
        }
        if (replyMessageQueue == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'replyMessageQueue' is null.");
            throw new NullPointerException("replyMessageQueue");
        }
        resolveConverter();
        // EBCDIC '*INQ'.
        return send(null, null, conv_.stringToByteArray(messageText), new byte[] { 0x5C, (byte)0xC9, (byte)0xD5, (byte)0xD8, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 }, replyMessageQueue);
    }

    /**
     Sets the help text formatting value.  Possible values are:
     <ul>
     <li>{@link com.ibm.as400.access.MessageFile#NO_FORMATTING MessageFile.NO_FORMATTING} - The help text is returned as a string of characters.  This is the default.
     <li>{@link com.ibm.as400.access.MessageFile#RETURN_FORMATTING_CHARACTERS MessageFile.RETURN_FORMATTING_CHARACTERS} - The help text contains formatting characters.  The formatting characters are:
     <ul>
     <br>&N -- Force a new line.
     <br>&P -- Force a new line and indent the new line six characters.
     <br>&B -- Force a new line and indent the new line four characters.
     </ul>
     <li>{@link com.ibm.as400.access.MessageFile#SUBSTITUTE_FORMATTING_CHARACTERS MessageFile.SUBSTITUTE_FORMATTING_CHARACTERS} - The MessageFile class replaces formatting characters with new line and space characters.
     </ul>
     @param  helpTextFormatting  The help text formatting value.
     **/
    public void setHelpTextFormatting(int helpTextFormatting)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting help text formatting:", helpTextFormatting);
        if (helpTextFormatting < MessageFile.NO_FORMATTING || helpTextFormatting > MessageFile.SUBSTITUTE_FORMATTING_CHARACTERS)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'helpTextFormatting' is not valid: " + helpTextFormatting);
            throw new ExtendedIllegalArgumentException("helpTextFormatting (" + helpTextFormatting + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        synchronized (this)
        {
            helpTextFormatting_ = helpTextFormatting;
            if (handle_ != null) closeHandle_ = true;
        }
    }

    /**
     Sets the list direction.
     @param  listDirection  true to list the messages in order from oldest to newest; false to list the messages in order from newest to oldest.  The default is true.
     @see  #getListDirection
     **/
    public void setListDirection(boolean listDirection)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting list direction:", listDirection);
        synchronized (this)
        {
            listDirection_ = listDirection;
            if (handle_ != null) closeHandle_ = true;
        }
    }

    /**
     Sets whether the list should be sorted by message type when {@link #ALL ALL} messages are selected for retrieval.  If the selection criteria is set to something other than {@link #ALL ALL}, the sort setting is ignored.
     @param  sort  true to indicate the messages should be sorted; false to indicate no sorting should be performed on the message list.
     @see  #getSort
     **/
    public void setSort(boolean sort)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting sort:", sort);
        synchronized (this)
        {
            sort_ = sort;
            if (handle_ != null) closeHandle_ = true;
        }
    }

    /**
     Sets the fully qualified integrated file system path name of the message queue.  The default is CURRENT.  The path cannot be changed if the MessageQueue object has established a connection to the server.
     @param  path  The fully qualified integrated file system path name of the message queue, or CURRENT to refer to the user's default message queue.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setPath(String path) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting path: " + path);
        if (path == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'path' is null.");
            throw new NullPointerException("path");
        }
        if (handle_ != null)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'path' after connect.");
            throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            synchronized (this)
            {
                if (!path.equals(CURRENT))
                {
                    // Validate the path and save the name and library.
                    QSYSObjectPathName ifs = new QSYSObjectPathName(path, "MSGQ");
                    library_ = ifs.getLibraryName();
                    name_ = ifs.getObjectName();
                }
                path_ = path;
                qualifiedMessageQueueName_ = null;
            }
        }
        else
        {
            String oldValue = path_;
            String newValue = path;

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("path", oldValue, newValue);
            }
            synchronized (this)
            {
                if (!path.equals(CURRENT))
                {
                    // Validate the path and save the name and library.
                    QSYSObjectPathName ifs = new QSYSObjectPathName(path, "MSGQ");
                    library_ = ifs.getLibraryName();
                    name_ = ifs.getObjectName();
                }
                path_ = path;
                qualifiedMessageQueueName_ = null;
            }
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("path", oldValue, newValue);
            }
        }
    }

    /**
     Sets whether or not to include messages that need a reply in the returned list of messages.  Passing true to all three message selection setters is equivalent to retrieving all the messages.  By default, all messages are retrieved.
     @param  selectMessagesNeedReply  true to include messages that need a reply; false to exclude messages that need a reply.
     @see  #isSelectMessagesNeedReply
     @see  #setSelectMessagesNoNeedReply
     @see  #setSelectSendersCopyMessagesNeedReply
     **/
    public void setSelectMessagesNeedReply(boolean selectMessagesNeedReply)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting messages that need a reply are selected:", selectMessagesNeedReply);
        synchronized (this)
        {
            selectMessagesNeedReply_ = selectMessagesNeedReply;
            if (handle_ != null) closeHandle_ = true;
        }
    }

    /**
     Sets whether or not to include messages that do not need a reply in the returned list of messages.  Passing true to all three message selection setters is equivalent to retrieving all the messages.  By default, all messages are retrieved.
     @param  selectMessagesNoNeedReply  true to include messages that do not need a reply; false to exclude messages that do not need a reply.
     @see  #isSelectMessagesNoNeedReply
     @see  #setSelectMessagesNeedReply
     @see  #setSelectSendersCopyMessagesNeedReply
     **/
    public void setSelectMessagesNoNeedReply(boolean selectMessagesNoNeedReply)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting messages that do not need a reply are selected:", selectMessagesNoNeedReply);
        synchronized (this)
        {
            selectMessagesNoNeedReply_ = selectMessagesNoNeedReply;
            if (handle_ != null) closeHandle_ = true;
        }
    }

    /**
     Sets whether or not to include sender's copy messages that need a reply in the returned list of messages.  Passing true to all three message selection setters is equivalent to retrieving all the messages.  By default, all messages are retrieved.
     @param  selectSendersCopyMessagesNeedReply  true to include sender's copy messages that need a reply; false to exclude sender's copy messages that need a reply.
     @see  #isSelectSendersCopyMessagesNeedReply
     @see  #setSelectMessagesNeedReply
     @see  #setSelectMessagesNoNeedReply
     **/
    public void setSelectSendersCopyMessagesNeedReply(boolean selectSendersCopyMessagesNeedReply)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting sender's copy messages that need a reply are selected:", selectSendersCopyMessagesNeedReply);
        synchronized (this)
        {
            selectSendersCopyMessagesNeedReply_ = selectSendersCopyMessagesNeedReply;
            if (handle_ != null) closeHandle_ = true;
        }
    }

    /**
     Sets the selection that describes which messages are returned.  The default is ALL.  This takes effect the next time the list of queue messages is retrieved or refreshed.
     <p>Note: This method resets the selection criteria set by the {@link #setSelectMessagesNeedReply setSelectMessagesNeedReply()}, {@link #setSelectMessagesNoNeedReply setSelectMessagesNoNeedReply()}, and {@link #setSelectSendersCopyMessagesNeedReply setSelectSendersCopyMessagesNeedReply()}.  Using this method will only set one of the above to true, unless {@link #ALL ALL} is specified, which will set all three of them to true.  To include combinations of the three criteria, use the individual setters instead of this method.
     @param  selection  The selection.  Valid values are:
     <ul>
     <li>{@link #ALL ALL}
     <li>{@link #MESSAGES_NEED_REPLY MESSAGES_NEED_REPLY}
     <li>{@link #MESSAGES_NO_NEED_REPLY MESSAGES_NO_NEED_REPLY}
     <li>{@link #SENDERS_COPY_NEED_REPLY SENDERS_COPY_NEED_REPLY}
     </ul>
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     @deprecated  Use {@link #setSelectMessagesNeedReply setSelectMessagesNeedReply(boolean)}, {@link #setSelectMessagesNoNeedReply setSelectMessagesNoNeedReply(boolean)}, and {@link #setSelectSendersCopyMessagesNeedReply setSelectSendersCopyMessagesNeedReply(boolean)} instead.
     **/
    public void setSelection(String selection) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting selection: " + selection);
        if (selection == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'selection' is null.");
            throw new NullPointerException("selection");
        }
        if (!selection.equals(ALL) && !selection.equals(MESSAGES_NEED_REPLY) && !selection.equals(MESSAGES_NO_NEED_REPLY) && !selection.equals(SENDERS_COPY_NEED_REPLY))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'selection' is not valid: " + selection);
            throw new ExtendedIllegalArgumentException("selection (" + selection + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            synchronized (this)
            {
                selection_ = selection;
                boolean all = selection.equals(ALL);
                selectMessagesNeedReply_ = all || selection.equals(MESSAGES_NEED_REPLY);
                selectMessagesNoNeedReply_ = all || selection.equals(MESSAGES_NO_NEED_REPLY);
                selectSendersCopyMessagesNeedReply_ = all || selection.equals(SENDERS_COPY_NEED_REPLY);
                if (handle_ != null) closeHandle_ = true;
            }
        }
        else
        {
            String oldValue = selection_;
            String newValue = selection;

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("selection", oldValue, newValue);
            }
            synchronized (this)
            {
                selection_ = selection;
                boolean all = selection.equals(ALL);
                selectMessagesNeedReply_ = all || selection.equals(MESSAGES_NEED_REPLY);
                selectMessagesNoNeedReply_ = all || selection.equals(MESSAGES_NO_NEED_REPLY);
                selectSendersCopyMessagesNeedReply_ = all || selection.equals(SENDERS_COPY_NEED_REPLY);
                if (handle_ != null) closeHandle_ = true;
            }
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("selection", oldValue, newValue);
            }
        }
    }

    /**
     Sets the severity of the messages which are returned.  All messages of the specified severity and greater are returned.  The default is 0.  This takes effect the next time that the list of queued messages is retreived or refreshed.
     @param  severity  The severity of the messages to be returned.  The value must be between 0 and 99, inclusive.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     @see  com.ibm.as400.resource.RMessageQueue#SEVERITY_CRITERIA
     **/
    public void setSeverity(int severity) throws PropertyVetoException
    {
        if ((severity < 0) || (severity > 99))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'severity' is not valid: " + severity);
            throw new ExtendedIllegalArgumentException("severity (" + severity + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            severity_ = severity;
        }
        else
        {
            Integer oldValue = new Integer(severity_);
            Integer newValue = new Integer(severity);

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("severity", oldValue, newValue);
            }
            severity_ = severity;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("severity", oldValue, newValue);
            }
        }
        synchronized (this)
        {
            if (handle_ != null) closeHandle_ = true;
        }
    }

    /**
     Sets the system.  This cannot be changed if the object has established a connection to the server.
     @param  system  The system object representing the server on which the message queue exists.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setSystem(AS400 system) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting system: " + system);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (handle_ != null)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'system' after connect.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            synchronized (this)
            {
                system_ = system;
                conv_ = null;
                qualifiedMessageQueueName_ = null;
            }
        }
        else
        {
            AS400 oldValue = system_;
            AS400 newValue = system;

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("system", oldValue, newValue);
            }
            synchronized (this)
            {
                system_ = system;
                conv_ = null;
                qualifiedMessageQueueName_ = null;
            }
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("system", oldValue, newValue);
            }
        }
    }

    /**
     Sets the starting message key used to begin searching for messages to list from the corresponding entry in the message queue.  Any valid message key will work, including {@link #NEWEST NEWEST} and {@link #OLDEST OLDEST}.  If the key of a reply message is specified, the message search begins with the inquiry or sender's copy message associated with that reply, not the reply message itself.
     <p>If the message queue is set to {@link #CURRENT CURRENT}, then the key represents the starting message key for the current user's user message queue.
     @param  userStartingMessageKey  The key.  Specify null to set it back to the default, which will be OLDEST or NEWEST based on the list direction.
     **/
    public void setUserStartingMessageKey(byte[] userStartingMessageKey)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting user starting message key:", userStartingMessageKey);
        synchronized (this)
        {
            userStartingMessageKey_ = userStartingMessageKey;
            if (handle_ != null) closeHandle_ = true;
        }
    }

    /**
     Sets the starting message key used to begin searching for messages to list from the corresponding entry in the message queue. Any valid message key will work, including {@link #NEWEST NEWEST} and {@link #OLDEST OLDEST}.  If the key of a reply message is specified, the message search begins with the inquiry or sender's copy message associated with that reply, not the reply message itself.
     <p>If the message queue is set to {@link #CURRENT CURRENT}, then the key represents the starting message key for the current user's workstation message queue.
     @param  workstationStartingMessageKey  The key.  Specify null to set it back to the default.
     **/
    public void setWorkstationStartingMessageKey(byte[] workstationStartingMessageKey)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting workstation starting message key:", workstationStartingMessageKey);
        synchronized (this)
        {
            workstationStartingMessageKey_ = workstationStartingMessageKey;
            if (handle_ != null) closeHandle_ = true;
        }
    }
}
