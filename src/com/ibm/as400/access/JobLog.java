///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  JobLog.java
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
 Represents a job log on the server.  This class is used to get a list of messages in a job log or to write messages to a job log.
 <p>QueuedMessage objects have many attributes.  Only some of theses attribute values are set, depending on how a QueuedMessage object is created.  The following is a list of attributes whose values are set on QueuedMessage objects returned in a list of job log messages:
 <ul>
 <li>alert option
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
 <li>sending program name
 </ul>
 @see  com.ibm.as400.access.QueuedMessage
 **/
public class JobLog implements Serializable
{
    static final long serialVersionUID = 4L;

    // We don't currently allow the user to specify a direction or any other attributes, so we just hardcode these.
    private static final int maxMessageLength_ = 511;
    private static final int maxMessageHelpLength_ = 3000;

    // Shared error code parameter.
    private static final ProgramParameter ERROR_CODE = new ProgramParameter(new byte[8]);

    // The server where the job log is located.
    private AS400 system_;
    // The job name.
    private String name_ = "*";
    // The job user name.
    private String user_ = "";
    // The job number.
    private String number_ = "";

    // Length of the job log message list.
    private int length_;
    // Handle that references the user space used by the open list APIs.
    private byte[] handle_;
    // If the list info has changed, close the old handle before loading the new one.
    private boolean closeHandle_ = false;

    // The direction of the list.
    private boolean listDirection_ = true;
    // The starting message key.
    private byte[] startingMessageKey_;

    // List of property change event bean listeners.
    private transient PropertyChangeSupport propertyChangeListeners_ = null;  // Set on first add.
    // List of vetoable change event bean listeners.
    private transient VetoableChangeSupport vetoableChangeListeners_ = null;  // Set on first add.

    /**
     Constructs a JobLog object.  The system must be provided later.  The job information defaults to the server job in which the program is run.
     **/
    public JobLog()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing JobLog object.");
    }

    /**
     Constructs a JobLog object.  The job information defaults to the server job in which the program is run.
     @param  system  The system object representing the server on which the job log exists.
     **/
    public JobLog(AS400 system)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing JobLog object, system: " + system);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        system_ = system;
    }

    /**
     Constructs a JobLog object.
     @param  system  The system object representing the server on which the job log exists.
     @param  name  The job name.
     @param  user  The job user name.
     @param  number  The job number.
     **/
    public JobLog(AS400 system, String name, String user, String number)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing JobLog object, system: " + system + ", name: " + name + ", user: " + user + ", number: " + number);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (name == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'name' is null.");
            throw new NullPointerException("name");
        }
        if (user == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'user' is null.");
            throw new NullPointerException("user");
        }
        if (number == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'number' is null.");
            throw new NullPointerException("number");
        }
        system_ = system;
        name_ = name;
        user_ = user;
        number_ = number;
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
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Closing job log message list, handle: ", handle_);
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
     Returns the number of messages in the job log.  This method implicitly calls {@link #load load()}.
     @return  The number of messages, or 0 if no list was retrieved.
     @see  #load
     **/
    public int getLength()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting job log list length.");
        try
        {
            if (handle_ == null || closeHandle_) load();
        }
        catch (Exception e)
        {
            Trace.log(Trace.ERROR, "Exception caught getting length of job log list:", e);
            if (e instanceof ExtendedIllegalStateException) throw (ExtendedIllegalStateException)e;
        }

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Length:", length_);
        return length_;
    }

    /**
     Returns the list direction.
     @return  true if the messages will be sorted oldest to newest; false if they will be sorted newest to oldest.  The default is true.
     **/
    public boolean getListDirection()
    {
        return listDirection_;
    }

    /**
     Returns the list of messages in the job log.  The messages are listed from oldest to newest.
     @return  An Enumeration of <a href="QueuedMessage.html">QueuedMessage</a> objects.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public Enumeration getMessages() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving job log message list.");
        // Need to get the length.
        if (handle_ == null || closeHandle_) load();
        return new QueuedMessageEnumeration(this, length_);
    }

    /**
     Returns a subset of the list of messages in the job log.  This method allows the user to retrieve the message list from the server in pieces.  If a call to {@link #load load()} is made (either implicitly or explicitly), then the messages at a given offset will change, so a subsequent call to getMessages() with the same <i>listOffset</i> and <i>number</i> will most likely not return the same QueuedMessages as the previous call.
     @param  listOffset  The offset into the list of messages.  This value must be greater than 0 and less than the list length, or specify -1 to retrieve all of the messages.
     @param  number  The number of messages to retrieve out of the list, starting at the specified <i>listOffset</i>.  This value must be greater than or equal to 0 and less than or equal to the list length.  If the <i>listOffset</i> is -1, this parameter is ignored.
     @return  The array of retrieved {@link com.ibm.as400.access.QueuedMessage QueuedMessage} objects.  The length of this array may not necessarily be equal to <i>number</i>, depending upon the size of the list on the server, and the specified <i>listOffset</i>.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     @see  com.ibm.as400.access.QueuedMessage
     **/
    public QueuedMessage[] getMessages(int listOffset, int number) throws  AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving job log message list, list offset: " + listOffset + ", number:", number);
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

        if (number == 0 && listOffset != -1) return new QueuedMessage[0];

        if (listOffset == -1)
        {
            number = length_;
            listOffset = 0;
        }
        else if (listOffset > length_)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'listOffset' is not valid:", listOffset);
            throw new ExtendedIllegalArgumentException("listOffset (" + listOffset + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        else if (listOffset + number > length_)
        {
            number = length_ - listOffset;
        }

        int lengthOfReceiverVariable = 1024 * number;

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
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Updated length: ", lengthOfReceiverVariable);
                parameters[0] = new ProgramParameter(lengthOfReceiverVariable);
                parameters[1] = new ProgramParameter(BinaryConverter.intToByteArray(lengthOfReceiverVariable));
            }
        } while (recordsReturned < number);

        byte[] data = parameters[0].getOutputData();
        Converter conv = new Converter(system_.getCcsid(), system_);

        QueuedMessage[] messages = new QueuedMessage[number];
        int offset = 0;
        for (int i = 0; i < messages.length; ++i) // each message
        {
            int entryOffset = BinaryConverter.byteArrayToInt(data, offset);
            int fieldOffset = BinaryConverter.byteArrayToInt(data, offset + 4);
            int numFields = BinaryConverter.byteArrayToInt(data, offset + 8);
            int messageSeverity = BinaryConverter.byteArrayToInt(data, offset + 12);
            String messageIdentifier = conv.byteArrayToString(data, offset + 16, 7).trim();
            int messageType = (data[offset + 23] & 0x0F) * 10 + (data[offset + 24] & 0x0F);
            if (messageType == 0) messageType = -1;
            byte[] messageKey = new byte[4];
            System.arraycopy(data, offset + 25, messageKey, 0, 4);
            String messageFileName = conv.byteArrayToString(data, offset + 29, 10).trim();
            String messageFileLibrarySpecified = conv.byteArrayToString(data, offset + 39, 10).trim();
            String dateSent = conv.byteArrayToString(data, offset + 49, 7); // CYYMMDD
            String timeSent = conv.byteArrayToString(data, offset + 56, 6); // HHMMSS

            messages[i] = new QueuedMessage(system_, messageSeverity, messageIdentifier, messageType, messageKey, messageFileName, messageFileLibrarySpecified, dateSent, timeSent);

            // Our 6 fields should've come back.
            for (int j = 0; j < numFields; ++j)
            {
                int offsetToNextField = BinaryConverter.byteArrayToInt(data, fieldOffset);
                int fieldID = BinaryConverter.byteArrayToInt(data, fieldOffset + 8);
                byte type = data[fieldOffset + 12];
                int dataLen = BinaryConverter.byteArrayToInt(data, fieldOffset + 28);
                if (type == (byte)0xC3) // 'C'
                {
                    messages[i].setValueInternal(fieldID, conv.byteArrayToString(data, fieldOffset + 32, dataLen));
                }
                else if (type == (byte)0xC2) // 'B'
                {
                    // We should never get here using our standard 6 keys.
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
                    // We should never get here using our standard 6 keys.
                    // Type = 'M'.  Used for fields 606 and 706
                    int numStatements = BinaryConverter.byteArrayToInt(data, fieldOffset + 32);
                    String[] statements = new String[numStatements];
                    for (int k = 0; k < numStatements; ++k)
                    {
                        statements[k] = conv.byteArrayToString(data, fieldOffset + 36 + (k * 10), 10);
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
     Returns the job name.
     @return  The job name, or "*" if none has been set.
     @see  #setName
     **/
    public String getName()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting name: " + name_);
        return name_;
    }

    /**
     Returns the job number.
     @return  The job number, or "" if none has been set.
     @see  #setNumber
     **/
    public String getNumber()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting number: " + number_);
        return number_;
    }

    /**
     Returns the starting message key.
     @return  The key.
     @see  #setStartingMessageKey
     **/
    public byte[] getStartingMessageKey()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting starting message key:", startingMessageKey_);
        return startingMessageKey_;
    }

    /**
     Returns the system object representing the server on which the job log exists.
     @return  The system object representing the server on which the job log exists.  If the system has not been set, null is returned.
     @see  #setSystem
     **/
    public AS400 getSystem()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system: " + system_);
        return system_;
    }

    /**
     Returns the job user name.
     @return  The job user name, or "" if none has been set.
     @see  #setUser
     **/
    public String getUser()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting user: " + user_);
        return user_;
    }

    /**
     Loads the list of messages on the server.  This method informs the server to build a list of messages.  This method blocks until the server returns the total number of messages it has compiled.  A subsequent call to {@link #getMessages getMessages()} will retrieve the actual message information and attributes for each message in the list from the server.
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
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Loading job log message list.");
        if (system_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot connect to server before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        if (!name_.equals("*"))
        {
            if (user_.equals(""))
            {
                Trace.log(Trace.ERROR, "Cannot connect to server before setting user.");
                throw new ExtendedIllegalStateException("user", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            }
            if (number_.equals(""))
            {
                Trace.log(Trace.ERROR, "Cannot connect to server before setting number.");
                throw new ExtendedIllegalStateException("number", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            }
        }

        // Close the previous list.
        if (closeHandle_) close();

        // Generate text objects based on system CCSID.
        Converter conv = new Converter(system_.getCcsid(), system_);

        // Create the message selection information.
        byte[] messageSelectionInformation = new byte[105];
        if (listDirection_)
        {
            // EBCDIC '*NEXT'.
            System.arraycopy(new byte[] { 0x5C, (byte)0xD5, (byte)0xC5, (byte)0xE7, (byte)0xE3 }, 0, messageSelectionInformation, 0, 5);
        }
        else
        {
            // EBCDIC '*PRV '.
            System.arraycopy(new byte[] { 0x5C, (byte)0xD7, (byte)0xD9, (byte)0xE5, 0x40 }, 0, messageSelectionInformation, 0, 5);
        }
        // Blank pad from end of List direction through Internal job identifier.
        for (int i = 5; i < 52; ++i) messageSelectionInformation[i] = 0x40;
        conv.stringToByteArray(name_.toUpperCase().trim(), messageSelectionInformation, 10);
        conv.stringToByteArray(user_.toUpperCase().trim(), messageSelectionInformation, 20);
        conv.stringToByteArray(number_, messageSelectionInformation, 30);
        byte[] startingMessageKey = (startingMessageKey_ != null ? startingMessageKey_ : (listDirection_ ? MessageQueue.OLDEST : MessageQueue.NEWEST));
        System.arraycopy(startingMessageKey, 0, messageSelectionInformation, 52, 4);
        // Only used for fields 401, 402, 403, or 404.
        BinaryConverter.intToByteArray(maxMessageLength_, messageSelectionInformation, 56);
        // Only used for fields 301 or 302.
        BinaryConverter.intToByteArray(maxMessageHelpLength_, messageSelectionInformation, 60);
        // Offset of identifiers.
        BinaryConverter.intToByteArray(80, messageSelectionInformation, 64);
        // Number of identifiers to return.
        BinaryConverter.intToByteArray(6, messageSelectionInformation, 68);
        // Offset of call message queue name.
        BinaryConverter.intToByteArray(104, messageSelectionInformation, 72);
        // Size of call message queue name.
        BinaryConverter.intToByteArray(1, messageSelectionInformation, 76);
        // Message with replacement data.
        BinaryConverter.intToByteArray(302, messageSelectionInformation, 80);
        // Sending program name.
        BinaryConverter.intToByteArray(603, messageSelectionInformation, 84);
        // Reply status.
        BinaryConverter.intToByteArray(1001, messageSelectionInformation, 88);
        // Default reply.
        BinaryConverter.intToByteArray(501, messageSelectionInformation, 92);
        // Message help with replacement data and formatting characters.
        BinaryConverter.intToByteArray(404, messageSelectionInformation, 96);
        // Alert option.
        BinaryConverter.intToByteArray(101, messageSelectionInformation, 100);
        // Call message queue name, EBCDIC '*', Messages from every call of the job are listed.
        messageSelectionInformation[104] = 0x5C;

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
            // Message selection information, input, char(*).
            new ProgramParameter(messageSelectionInformation),
            // Size of message selection information, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(messageSelectionInformation.length)),
            // Error code, I/O, char(*).
            ERROR_CODE,
        };

        // Call the program.
        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYOLJBL.PGM", parameters);
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

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Loaded job log message list, length: " + length_ + ", handle: ", handle_);
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

    /**
     Sets the list direction.
     @param  listDirection  true to sort the messages oldest to newest; false to sort them newest to oldest.  The default is true.
     **/
    public void setListDirection(boolean listDirection)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting list direction: " + listDirection);
        synchronized (this)
        {
            listDirection_ = listDirection;
            if (handle_ != null) closeHandle_ = true;
        }
    }

    /**
     Sets the job name.  This cannot be changed if the object has established a connection to the server.
     @param  name  The job name.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setName(String name) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting name: " + name);
        if (name == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'name' is null.");
            throw new NullPointerException("name");
        }
        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            synchronized (this)
            {
                name_ = name;
                if (handle_ != null) closeHandle_ = true;
            }
        }
        else
        {
            String oldValue = name_;
            String newValue = name;
            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("name", oldValue, newValue);
            }
            synchronized (this)
            {
                name_ = name;
                if (handle_ != null) closeHandle_ = true;
            }
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("name", oldValue, newValue);
            }
        }
    }

    /**
     Sets the job number. This cannot be changed if the object has established a connection to the server.
     @param  number  The job number.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setNumber(String number) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting number: " + number);
        if (number == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'number' is null.");
            throw new NullPointerException("number");
        }
        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            synchronized (this)
            {
                number_ = number;
                if (handle_ != null) closeHandle_ = true;
            }
        }
        else
        {
            String oldValue = number_;
            String newValue = number;
            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("number", oldValue, newValue);
            }
            synchronized (this)
            {
                number_ = number;
                if (handle_ != null) closeHandle_ = true;
            }
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("number", oldValue, newValue);
            }
        }
    }

    /**
     Sets the message key used to begin searching for messages to list from the corresponding entry in the job log.  Any valid message key will work, including {@link com.ibm.as400.access.MessageQueue#OLDEST MessageQueue.OLDEST} and {@link com.ibm.as400.access.MessageQueue#NEWEST MessageQueue#NEWEST}.
     @param  startingMessageKey  The key.  Specify null to set it back to the default, which will be OLDEST or NEWEST based on the list direction.
     **/
    public void setStartingMessageKey(byte[] startingMessageKey)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting starting message key:", startingMessageKey);
        synchronized (this)
        {
            startingMessageKey_ = startingMessageKey;
            if (handle_ != null) closeHandle_ = true;
        }
    }

    /**
     Sets the system.  This cannot be changed if the object has established a connection to the server.
     @param  system  The system object representing the server on which the job log exists.
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
            system_ = system;
        }
        else
        {
            AS400 oldValue = system_;
            AS400 newValue = system;

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("system", oldValue, newValue);
            }
            system_ = system;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("system", oldValue, newValue);
            }
        }
    }

    /**
     Sets the job user name.  This cannot be changed if the object has established a connection to the server.
     @param  user  The job user name.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setUser(String user) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting user: " + user);
        if (user == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'user' is null.");
            throw new NullPointerException("user");
        }
        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            synchronized (this)
            {
                user_ = user;
                if (handle_ != null) closeHandle_ = true;
            }
        }
        else
        {
            String oldValue = user_;
            String newValue = user;
            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("user", oldValue, newValue);
            }
            synchronized (this)
            {
                user_ = user;
                if (handle_ != null) closeHandle_ = true;
            }
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("user", oldValue, newValue);
            }
        }
    }

    /**
     Writes a program message to the job log for the job in which the program is running.
     <br>Note: The program runs in the job of the Remote Command Host Server (QZRCSRVS) unless it is invoked "on-thread" on the server.
     @param  system  The system object representing the server on which the job log exists.  If the system specifies localhost, the message is written to the job log of the process from which this method is called.  Otherwise the message is written to the QZRCSRVS job.
     @param  messageID  The message ID.  The message must be in the default message file /QSYS.LIB/QCPFMSG.MSGF.
     @param  messageType  The message type.  Possible values are:
     <ul>
     <li>AS400Message.COMPLETION
     <li>AS400Message.DIAGNOSTIC
     <li>AS400Message.INFORMATIONAL
     <li>AS400Message.ESCAPE
     </ul>
     The message type must be AS400Message.INFORMATIONAL for an immediate message.
     @see  com.ibm.as400.access.ProgramCall#isStayOnThread()
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public static void writeMessage(AS400 system, String messageID, int messageType) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Writing message to job log, system: " + system + ", message ID: " + messageID + ", message type:", messageType);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (messageID == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageID' is null.");
            throw new NullPointerException("messageID");
        }
        switch (messageType)
        {
            case AS400Message.COMPLETION:
            case AS400Message.DIAGNOSTIC:
            case AS400Message.INFORMATIONAL:
            case AS400Message.ESCAPE:
                break;
            default:
                Trace.log(Trace.ERROR, "Value of parameter 'messageType' is not valid: " + messageType);
                throw new ExtendedIllegalArgumentException("messageType (" + messageType + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        sendProgramMessage(system, messageID, "/QSYS.LIB/QCPFMSG.MSGF", null, messageType, false);
    }

    /**
     Writes a program message to the job log for the job in which the program is running.
     <br>Note: The program runs in the job of the Remote Command Host Server (QZRCSRVS) unless it is invoked "on-thread" on the server.
     @param  system  The system.  If the system specifies localhost, the message is written to the job log of the process from which this method is called.  Otherwise the message is written to the QZRCSRVS job.
     @param  messageID  The message ID.  The message must be in the default message file /QSYS.LIB/QCPFMSG.MSGF.
     @param  messageType  The message type. Possible values are:
     <ul>
     <li>AS400Message.COMPLETION
     <li>AS400Message.DIAGNOSTIC
     <li>AS400Message.INFORMATIONAL
     <li>AS400Message.ESCAPE
     </ul>
     The message type must be AS400Message.INFORMATIONAL for an immediate message.
     @param  substitutionData  The substitution data.  The substitution data can be from 0-32767 bytes for a conventional message and from 1-6000 bytes for an immediate message.
     @see  com.ibm.as400.access.ProgramCall#isStayOnThread()
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public static void writeMessage(AS400 system, String messageID, int messageType, byte[] substitutionData) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Writing message to job log, system: " + system + ", message ID: " + messageID + ", message type: " + messageType + ", substitution data:", substitutionData);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (messageID == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageID' is null.");
            throw new NullPointerException("messageID");
        }
        switch (messageType)
        {
            case AS400Message.COMPLETION:
            case AS400Message.DIAGNOSTIC:
            case AS400Message.INFORMATIONAL:
            case AS400Message.ESCAPE:
                break;
            default:
                Trace.log(Trace.ERROR, "Value of parameter 'messageType' is not valid: " + messageType);
                throw new ExtendedIllegalArgumentException("messageType (" + messageType + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        sendProgramMessage(system, messageID, "/QSYS.LIB/QCPFMSG.MSGF", substitutionData, messageType, false);
    }

    /**
     Writes a program message to the job log for the job in which the program is running.
     <br>Note: The program runs in the job of the Remote Command Host Server (QZRCSRVS) unless it is invoked "on-thread" on the server.
     @param  system  The system.  If the system specifies localhost, the message is written to the job log of the process from which this method is called.  Otherwise the message is written to the QZRCSRVS job.
     @param  messageID  The message ID.
     @param  messageType  The message type. Possible values are:
     <ul>
     <li>AS400Message.COMPLETION
     <li>AS400Message.DIAGNOSTIC
     <li>AS400Message.INFORMATIONAL
     <li>AS400Message.ESCAPE
     </ul>
     The message type must be AS400Message.INFORMATIONAL for an immediate message.
     @param  messageFile  The integrated file system path name of the message file.
     @see  com.ibm.as400.access.ProgramCall#isStayOnThread()
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public static void writeMessage(AS400 system, String messageID, int messageType, String messageFile) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (messageID == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageID' is null.");
            throw new NullPointerException("messageID");
        }
        switch (messageType)
        {
            case AS400Message.COMPLETION:
            case AS400Message.DIAGNOSTIC:
            case AS400Message.INFORMATIONAL:
            case AS400Message.ESCAPE:
                break;
            default:
                Trace.log(Trace.ERROR, "Value of parameter 'messageType' is not valid: " + messageType);
                throw new ExtendedIllegalArgumentException("messageType (" + messageType + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (messageFile == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageFile' is null.");
            throw new NullPointerException("messageFile");
        }
        QSYSObjectPathName verify = new QSYSObjectPathName(messageFile, "MSGF");
        sendProgramMessage(system, messageID, messageFile, null, messageType, false);
    }

    /**
     Writes a program message to the job log for the job in which the program is running.
     <br>Note: The program runs in the job of the Remote Command Host Server (QZRCSRVS) unless it is invoked "on-thread" on the server.
     @param  system  The system.  If the system specifies localhost, the message is written to the job log of the process from which this method is called.  Otherwise the message is written to the QZRCSRVS job.
     @param  messageID  The message ID.
     @param  messageType  The message type. Possible values are:
     <ul>
     <li>AS400Message.COMPLETION
     <li>AS400Message.DIAGNOSTIC
     <li>AS400Message.INFORMATIONAL
     <li>AS400Message.ESCAPE
     </ul>
     The message type must be AS400Message.INFORMATIONAL for an immediate message.
     @param  messageFile  The integrated file system path name of the message file.
     @param  substitutionData  The substitution data.  The substitution data can be from 0-32767 bytes for a conventional message and from 1-6000 bytes for an immediate message.
     @see  com.ibm.as400.access.ProgramCall#isStayOnThread()
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public static void writeMessage(AS400 system, String messageID, int messageType, String messageFile, byte[] substitutionData) throws  AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (messageID == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageID' is null.");
            throw new NullPointerException("messageID");
        }
        switch (messageType)
        {
            case AS400Message.COMPLETION:
            case AS400Message.DIAGNOSTIC:
            case AS400Message.INFORMATIONAL:
            case AS400Message.ESCAPE:
                break;
            default:
                Trace.log(Trace.ERROR, "Value of parameter 'messageType' is not valid: " + messageType);
                throw new ExtendedIllegalArgumentException("messageType (" + messageType + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (messageFile == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageFile' is null.");
            throw new NullPointerException("messageFile");
        }
        QSYSObjectPathName verify = new QSYSObjectPathName(messageFile, "MSGF");
        sendProgramMessage(system, messageID, messageFile, substitutionData, messageType, false);
    }

    /**
     Writes a program message to the job log for the job in which the program is running.  The message is sent to the Remote Command Host Server (QZRCSRVS) unless true is specified for the <i>onThread</i> parameter and is invoked while running on the server.
     @param  system  The system.  The system cannot be null.
     @param  messageID  The message ID. The message ID cannot be null.
     @param  messageType  The message type. Possible values are:
     <ul>
     <li>AS400Message.COMPLETION
     <li>AS400Message.DIAGNOSTIC
     <li>AS400Message.INFORMATIONAL
     <li>AS400Message.ESCAPE
     </ul>
     The message type must be AS400Message.INFORMATIONAL for an immediate message.
     @param  messageFile  The integrated file system path name of the message file.  If null is specified, the message file used is /QSYS.LIB/QCPFMSG.MSGF.
     @param  substitutionData  The substitution data.  The substitution data can be from 0-32767 bytes for a conventional message and from 1-6000 bytes for an immediate message.  If null is specified, no substitution data is used.
     @param  onThread  Whether or not to stay on thread when calling the API to write the message to the job log. true to write the message to the current job's job log, false to write the message to the Remote Command Host Server job's job log.  Note that this parameter is meaningless unless this Java program is running on the server and the system object is using native optimizations.
     @see  com.ibm.as400.access.ProgramCall#isStayOnThread()
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public static void writeMessage(AS400 system, String messageID, int messageType, String messageFile, byte[] substitutionData, boolean onThread) throws  AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (messageID == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageID' is null.");
            throw new NullPointerException("messageID");
        }
        switch (messageType)
        {
            case AS400Message.COMPLETION:
            case AS400Message.DIAGNOSTIC:
            case AS400Message.INFORMATIONAL:
            case AS400Message.ESCAPE:
                break;
            default:
                Trace.log(Trace.ERROR, "Value of parameter 'messageType' is not valid: " + messageType);
                throw new ExtendedIllegalArgumentException("messageType (" + messageType + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (messageFile == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageFile' is null.");
            throw new NullPointerException("messageFile");
        }
        QSYSObjectPathName verify = new QSYSObjectPathName(messageFile, "MSGF");
        sendProgramMessage(system, messageID, messageFile, substitutionData, messageType, onThread);
    }

    private static final byte[] typeCompletion_ = new byte[] { 0x5C, (byte)0xC3, (byte)0xD6, (byte)0xD4, (byte)0xD7, 0x40, 0x40, 0x40, 0x40, 0x40 }; // "*COMP     "
    private static final byte[] typeDiagnostic_ = new byte[] { 0x5C, (byte)0xC4, (byte)0xC9, (byte)0xC1, (byte)0xC7, 0x40, 0x40, 0x40, 0x40, 0x40 }; // "*DIAG     "
    private static final byte[] typeEscape_ = new byte[] { 0x5C, (byte)0xC5, (byte)0xE2, (byte)0xC3, (byte)0xC1, (byte)0xD7, (byte)0xC5, 0x40, 0x40, 0x40 }; // "*ESCAPE   "
    private static final byte[] typeInformational_ = new byte[] { 0x5C, (byte)0xC9, (byte)0xD5, (byte)0xC6, (byte)0xD6, 0x40, 0x40, 0x40, 0x40, 0x40 }; // "*INFO     "

    private static final byte[] callStackEntry_ = new byte[] { 0x5C, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 }; // "*         "

    private static void sendProgramMessage(AS400 system, String messageID, String messageFile, byte[] replacementData, int messageType, boolean onThread) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Assume all parameters have been validated.
        Converter conv = new Converter(system.getCcsid(), system);

        byte[] messageIdentifier = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        conv.stringToByteArray(messageID, messageIdentifier, 0, 7);

        byte[] qualifiedMessageFileName = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        QSYSObjectPathName path = new QSYSObjectPathName(messageFile);
        conv.stringToByteArray(path.getObjectName(), qualifiedMessageFileName, 0, 10);
        conv.stringToByteArray(path.getLibraryName(), qualifiedMessageFileName, 10, 10);

        if (replacementData == null) replacementData = new byte[0];

        byte[] type = null;
        switch (messageType)
        {
            case AS400Message.COMPLETION:
                type = typeCompletion_;
                break;
            case AS400Message.DIAGNOSTIC:
                type = typeDiagnostic_;
                break;
            case AS400Message.ESCAPE:
                type = typeEscape_;
                break;
            case AS400Message.INFORMATIONAL:
                type = typeInformational_;
                break;
            default:
                break;
        }

        // Setup program parameters.
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Message identifier, input, char(7).
            new ProgramParameter(messageIdentifier),
            // Qualified message file name, input, char(20).
            new ProgramParameter(qualifiedMessageFileName),
            // Message data or immediate text, input, char(*).
            new ProgramParameter(replacementData),
            // Length of message data or immediate text, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(replacementData.length)),
            // Message type, input, char(10).
            new ProgramParameter(type),
            // Call stack entry, input, char(*).
            new ProgramParameter(callStackEntry_),
            // Call stack counter, input, binary(4).
            new ProgramParameter(new byte[] { 0x00, 0x00, 0x00, 0x00 } ),
            // Message key, output, char(4).
            new ProgramParameter(4),
            // Error code, I/O, char(*).
            ERROR_CODE
        };

        ProgramCall pc = new ProgramCall(system, "/QSYS.LIB/QMHSNDPM.PGM", parameters);
        pc.setThreadSafe(onThread); // The QMHSNDPM is threadsafe, but we only want to stay on-thread
        // if the user wants to write to the current job log instead of the
        // remote command server's job log.
        if (!pc.run())
        {
            // If one message came back and it is the one we sent,
            // then we're OK.
            AS400Message[] msgs = pc.getMessageList();
            if (msgs.length == 1 && msgs[0].getID().equals(messageID) &&
                msgs[0].getType() == messageType)
            {
                if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "Expected escape message ignored.");
                return;
            }
            throw new AS400Exception(msgs);
        }
    }
}
