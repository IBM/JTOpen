///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: MessageQueue.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Calendar;


/**
The MessageQueue class represents an OS/400 message queue.  If no message
queue path is set, then the default is {@link #CURRENT CURRENT},
which represents the current user's message queue,
<code>/QSYS.LIB/QUSRSYS.LIB/<em>userID</em>.MSGQ</code>.

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
@see com.ibm.as400.access.QueuedMessage
@see com.ibm.as400.resource.RMessageQueue
@see com.ibm.as400.resource.RQueuedMessage
**/
public class MessageQueue implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";



  static final long serialVersionUID = 5L;
  static final int VRM_530 = AS400.generateVRM(5, 3, 0);


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
  public final static byte[] NEWEST                  = new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF}; // all FF's

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
  public final static byte[] OLDEST                  = new byte[4]; // all 0's

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
  public final static String REPLY                   = "*RPY";

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



  private transient   PropertyChangeSupport   propertyChangeSupport_;
  private transient   VetoableChangeSupport   vetoableChangeSupport_;

  private AS400 system_;
  private String path_;

  private static final ProgramParameter errorCode_ = new ProgramParameter(new byte[4]);
  private static final byte[] blankKey_ = new byte[] { 0x40, 0x40, 0x40, 0x40};

  private int length_;
  private byte[] handle_; // handle that references the user space used by the open list APIs
  private byte[] handleToClose_; // used to close a previously opened list
  private boolean isConnected_;

  private boolean sort_ = false;

  // Message selection information
  private int helpTextFormatting_ = MessageFile.NO_FORMATTING;
  private boolean listDirection_ = true;
  private String selectionCriteria_ = ALL;
  private int severity_;
  private int maxMessageLength_ = -1; //@G0C - Was hardcoded as 511.
  private int maxMessageHelpLength_ = -1; //@G0C - Was hardcoded as 3000.


  private byte[] userStartingMessageKey_;
  private byte[] workstationStartingMessageKey_;


  private boolean selectMessagesNeedReply_ = true;
  private boolean selectMessagesNoNeedReply_ = true;
  private boolean selectSendersCopyMessagesNeedReply_ = true;


/**
   * Constructs a MessageQueue object.
   * @see #setPath
   * @see #setSystem
**/
  public MessageQueue()
  {
    path_ = CURRENT;
  }


/**
   * Constructs a MessageQueue object.
   * The message queue path defaults to {@link #CURRENT CURRENT}.
   * @param system The system.
   * @see #setPath
**/
  public MessageQueue(AS400 system)
  {
    if (system == null) throw new NullPointerException("system");
    system_ = system;
    path_ = CURRENT;
  }


/**
   * Constructs a MessageQueue object.
   * @param system The system.
   * @param path The fully qualified integrated file system path name
   *             of the message queue, or {@link #CURRENT CURRENT} to refer to the user's
   *             default message queue.
**/
  public MessageQueue(AS400 system, String path)
  {
    if (system == null) throw new NullPointerException("system");
    if (path == null) throw new NullPointerException("path");
    system_ = system;
    if (!path.equals(CURRENT))
    {
      QSYSObjectPathName name = new QSYSObjectPathName(path, "MSGQ"); // Validate.
    }
    path_ = path;
  }


/**
   * Adds a PropertyChangeListener.  The specified PropertyChangeListener's
   * <b>propertyChange()</b> method will be called each time the value of
   * any bound property is changed.
   * @param listener The listener.
   * @see #removePropertyChangeListener
  **/
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener");
    if (propertyChangeSupport_ == null) propertyChangeSupport_ = new PropertyChangeSupport(this);
    propertyChangeSupport_.addPropertyChangeListener(listener);
  }


/**
   * Adds a VetoableChangeListener.  The specified VetoableChangeListener's
   * <b>vetoableChange()</b> method will be called each time the value of
   * any constrained property is changed.
   * @param listener The listener.
   * @see #removeVetoableChangeListener
  **/
  public void addVetoableChangeListener(VetoableChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener");
    if (vetoableChangeSupport_ == null) vetoableChangeSupport_ = new VetoableChangeSupport(this);
    vetoableChangeSupport_.addVetoableChangeListener(listener);
  }


/**
   * Closes the message list on the system.
   * This releases any system resources previously in use by this message list.
   * @exception AS400Exception                  If the system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the system.
   * @exception ObjectDoesNotExistException     If the object does not exist on the system.
**/
  public synchronized void close() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (!isConnected_)
    {
      return;
    }
    if (handleToClose_ != null && (handle_ == null || handle_ == handleToClose_))
    {
      handle_ = handleToClose_;
      handleToClose_ = null;
    }
    if (Trace.traceOn_)
    {
      Trace.log(Trace.DIAGNOSTIC, "Closing message list with handle: ", handle_);
    }
    ProgramParameter[] parms = new ProgramParameter[]
    {
      new ProgramParameter(handle_),
      errorCode_
    };
    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYCLST.PGM", parms);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }
    isConnected_ = false;
    handle_ = null;
    if (handleToClose_ != null) // Just in case.
    {
      handle_ = handleToClose_;
      handleToClose_ = null;
      close();
    }
  }


  /**
   * Closes the message list on the system when this object is garbage collected.
  **/
  protected void finalize() throws Throwable
  {
    try
    {
      close();
    }
    catch(Exception e)
    {
    }
    super.finalize();
  }


  /**
    * Returns the status of help text formatting.  Possible values are:
    * <UL>
    * <LI>{@link com.ibm.as400.access.MessageFile#NO_FORMATTING MessageFile.NO_FORMATTING} - The help text is returned as a string of characters.
    * This is the default.
 * <LI>{@link com.ibm.as400.access.MessageFile#RETURN_FORMATTING_CHARACTERS MessageFile.RETURN_FORMATTING_CHARACTERS} - The help text contains
    * formatting characters.  The formatting characters are:
    * <UL>
    * &N -- Force a new line <BR>
    * &P -- Force a new line and indent the new line six characters <BR>
    * &B -- Force a new line and indent the new line four characters
    * </UL>
    * <LI>{@link com.ibm.as400.access.MessageFile#SUBSTITUTE_FORMATTING_CHARACTERS MessageFile.SUBSTITUTE_FORMATTING_CHARACTERS} - The MessageFile class replaces
 * formatting characters with newline and space characters.
    * </UL>
    * @return  The status of help text formatting.
 * @see #setHelpTextFormatting
    **/
  public int getHelpTextFormatting()
  {
    return helpTextFormatting_;
  }


/**
   * Returns the number of messages in the list. This method implicitly calls {@link #load load()}.
   * @return The number of messages, or 0 if no list was retrieved.
   * @see #load
  **/
  public int getLength()
  {
    if (system_ == null) throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);

    try
    {
      if (handle_ == null)
      {
        load();
      }
    }
    catch (Exception e)
    {
      if (Trace.traceOn_)
      {
        Trace.log(Trace.ERROR, "Exception caught on MessageQueue getLength():", e);
      }
    }

    return length_;
  }


  /**
   * Returns the list direction.
   * @return true if the messages are listed in order from oldest to
   * newest; false if the messages are listed in order from newest
   * to oldest.
   * @see #setListDirection
**/
  public boolean getListDirection()
  {
    return listDirection_;
  }


/**
   * Returns a subset of the list of messages in the message queue.
   * This method allows the user to retrieve the message list from the server
   * in pieces. If a call to {@link #load load()} is made (either implicitly or explicitly),
   * then the messages at a given offset will change, so a subsequent call to
   * getMessages() with the same <i>listOffset</i> and <i>number</i>
   * will most likely not return the same QueuedMessages as the previous call.
   * @param listOffset The offset into the list of messages. This value must be greater than 0 and
   * less than the list length, or specify -1 to retrieve all of the messages.
   * @param number The number of messages to retrieve out of the list, starting at the specified
   * <i>listOffset</i>. This value must be greater than or equal to 0 and less than or equal
   * to the list length. If the <i>listOffset</i> is -1, this parameter is ignored.
   * @return The array of retrieved {@link com.ibm.as400.access.QueuedMessage QueuedMessage} objects.
   * The length of this array may not necessarily be equal to <i>number</i>, depending upon the size
   * of the list on the server, and the specified <i>listOffset</i>.
   * @exception AS400Exception                  If the system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the system.
   * @exception ObjectDoesNotExistException     If the object does not exist on the system.
   * @see com.ibm.as400.access.Job
  **/
  public QueuedMessage[] getMessages(int listOffset, int number) throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (listOffset < -1)
    {
      throw new ExtendedIllegalArgumentException("listOffset", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    if (number < 0 && listOffset != -1)
    {
      throw new ExtendedIllegalArgumentException("number", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    if (system_ == null)
    {
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (number == 0 && listOffset != -1)
    {
      return new QueuedMessage[0];
    }

    if (handle_ == null)
    {
      load();
    }

    if (listOffset == -1) number = length_;

    int ccsid = system_.getCcsid();
    ConvTable conv = ConvTable.getTable(ccsid, null);

    ProgramParameter[] parms2 = new ProgramParameter[7];
    int len = 400*number;
    parms2[0] = new ProgramParameter(len); // receiver variable
    parms2[1] = new ProgramParameter(BinaryConverter.intToByteArray(len)); // length of receiver variable
    parms2[2] = new ProgramParameter(handle_);
    parms2[3] = new ProgramParameter(80); // list information
    parms2[4] = new ProgramParameter(BinaryConverter.intToByteArray(number)); // number of records to return
    parms2[5] = new ProgramParameter(BinaryConverter.intToByteArray(listOffset == -1 ? -1 : listOffset+1)); // starting record
    parms2[6] = errorCode_;

    ProgramCall pc2 = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYGTLE.PGM", parms2);
    if (!pc2.run())
    {
      throw new AS400Exception(pc2.getMessageList());
    }

    byte[] listInfo = parms2[3].getOutputData();
    int totalRecords = BinaryConverter.byteArrayToInt(listInfo, 0);
    int recordsReturned = BinaryConverter.byteArrayToInt(listInfo, 4);
    while (listOffset == -1 && totalRecords > recordsReturned)
    {
      len = len*(1+(totalRecords/(recordsReturned+1)));
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Calling MessageQueue QGYGTLE again with an updated length of "+len+".");
      try
      {
        parms2[0].setOutputDataLength(len);
        parms2[1].setInputData(BinaryConverter.intToByteArray(len));
      }
      catch(PropertyVetoException pve) {}
      if (!pc2.run())
      {
        throw new AS400Exception(pc2.getMessageList());
      }
      listInfo = parms2[3].getOutputData();
      totalRecords = BinaryConverter.byteArrayToInt(listInfo, 0);
      recordsReturned = BinaryConverter.byteArrayToInt(listInfo, 4);
    }

    ListUtilities.checkListStatus(listInfo[30]);  // check the list status indicator
    byte[] data = parms2[0].getOutputData();

    QueuedMessage[] messages = new QueuedMessage[recordsReturned];
    int offset = 0;
    for (int i=0; i<messages.length; ++i) // each message
    {
      int entryOffset = BinaryConverter.byteArrayToInt(data, offset);
      int fieldOffset = BinaryConverter.byteArrayToInt(data, offset+4);
      int numFields = BinaryConverter.byteArrayToInt(data, offset+8);
      int messageSeverity = BinaryConverter.byteArrayToInt(data, offset+12);
      String messageIdentifier = conv.byteArrayToString(data, offset+16, 7).trim();
      String messageTypeString = conv.byteArrayToString(data, offset+23, 2).trim();
      int messageType = messageTypeString.length() > 0 ? messageType = Integer.parseInt(messageTypeString) : -1;
      byte[] messageKey = new byte[4];
      System.arraycopy(data, offset+25, messageKey, 0, 4);
      String messageFileName = conv.byteArrayToString(data, offset+29, 10).trim();
      String messageFileLibrarySpecified = conv.byteArrayToString(data, offset+39, 10).trim();
      String messageQueue = conv.byteArrayToString(data, offset+49, 10).trim();
      String messageQueueLibraryUsed = conv.byteArrayToString(data, offset+59, 10).trim();
      String dateSent = conv.byteArrayToString(data, offset+69, 7); // CYYMMDD
      String timeSent = conv.byteArrayToString(data, offset+76, 6); // HHMMSS
      MessageQueue mq = this;
      if (messageQueueLibraryUsed.length() > 0 && messageQueue.length() > 0)
      {
        mq = new MessageQueue(system_, QSYSObjectPathName.toPath(messageQueueLibraryUsed, messageQueue, "MSGQ"));
      }
      messages[i] = new QueuedMessage(mq, messageSeverity, messageIdentifier, messageType,
                                      messageKey, messageFileName, messageFileLibrarySpecified, dateSent, timeSent);

      // Our 7 fields should've come back.
      for (int j=0; j<numFields; ++j)
      {
        int offsetToNextField = BinaryConverter.byteArrayToInt(data, fieldOffset);
        int infoLen = BinaryConverter.byteArrayToInt(data, fieldOffset+4);
        int fieldID = BinaryConverter.byteArrayToInt(data, fieldOffset+8);
        byte type = data[fieldOffset+12];
        byte status = data[fieldOffset+13];
        int dataLen = BinaryConverter.byteArrayToInt(data, fieldOffset+28);
        if (type == (byte)0xC3) // 'C'
        {
          String value = conv.byteArrayToString(data, fieldOffset+32, dataLen);
          messages[i].setValueInternal(fieldID, value);
        }
        else if (type == (byte)0xC2) // 'B'
        {
          // We should never get here using our standard 6 keys.
          if (dataLen > 4)
          {
            messages[i].setAsLong(fieldID, BinaryConverter.byteArrayToLong(data, fieldOffset+32));
          }
          else
          {
            messages[i].setAsInt(fieldID, BinaryConverter.byteArrayToInt(data, fieldOffset+32));
          }
        }
        else
        {
          // type = 'M'
          // Used for fields 606 and 706
          // We should never get here using our standard 6 keys.
          int numStatements = BinaryConverter.byteArrayToInt(data, fieldOffset+32);
          String[] statements = new String[numStatements];
          for (int k=0; k<numStatements; ++k)
          {
            statements[k] = conv.byteArrayToString(data, fieldOffset+36+(k*10), 10);
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
   * Returns the list of messages in the message queue.
   * The enumeration retrieves the messages in blocks of 1000. If this does not yield the desired
   * performance or memory usage, please use the {@link #getMessages(int,int) getMessages()} that
   * returns an array of QueuedMessage objects and accepts a list <i>offset</i> and <i>length</i>.
   * If an error occurs while the Enumeration is loading the next block of messages, a
   * NoSuchElementException will be thrown while the real error will be logged to {@link com.ibm.as400.access.Trace Trace.ERROR}.
   * @return An Enumeration of {@link com.ibm.as400.access.QueuedMessage QueuedMessage} objects.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
  public Enumeration getMessages()
  throws  AS400Exception,
  AS400SecurityException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException
  {
    if (system_ == null)
    {
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    // @K1D if (handle_ == null)
    //@K1D {
      load(); // Need to get the length_
    //@K1D }

    return new QueuedMessageEnumeration(this, length_);
  }


/**
   * Returns the fully qualified integrated file system path name
   * of the message queue, or {@link #CURRENT CURRENT} to refer to the user's default
   * message queue.
   * @return The fully qualified integrated file system path name of the
   * message queue, or {@link #CURRENT CURRENT} to refer to the user's default
   * message queue.
   * @see #setPath
**/
  public String getPath()
  {
    return path_;
  }


/**
   * Returns the selection that describes which messages are returned.
   * @return The selection.  Possible values are:
   * <ul>
   *   <li>{@link #ALL ALL}
   *   <li>{@link #MESSAGES_NEED_REPLY MESSAGES_NEED_REPLY}
   *   <li>{@link #MESSAGES_NO_NEED_REPLY MESSAGES_NO_NEED_REPLY}
   *   <li>{@link #SENDERS_COPY_NEED_REPLY SENDERS_COPY_NEED_REPLY}
   * </ul>
   * @see #setSelection
   * @deprecated Use {@link #isSelectMessagesNeedReply isSelectMessagesNeedReply()},
   * {@link #isSelectMessagesNoNeedReply isSelectMessagesNoNeedReply()}, and
   * {@link #isSelectSendersCopyMessagesNeedReply isSelectSendersCopyMessagesNeedReply()} instead. The value
   * returned by this method may not accurately reflect the actual selection criteria used to filter the
   * list of messages.
**/
  public String getSelection()
  {
    return selectionCriteria_;
  }


/**
   * Returns the severity of the messages which are returned.
   * @return The severity of the messages which are returned.
   * @see #setSeverity
**/
  public int getSeverity()
  {
    return severity_;
  }


  /**
   * Returns whether or not messages will be sorted when {@link #ALL ALL} is specified for the selection criteria.
   * The default is false.
   * @return true if messages will be sorted by message type; false otherwise.
   * @see #setSort
  **/
  public boolean getSort()
  {
    return sort_;
  }


/**
   * Returns the system.
   * @return The system.
   * @see #setSystem
**/
  public AS400 getSystem()
  {
    return system_;
  }

  /**
   * Returns the starting message key, if one has been set.
   * @return The key, or null if none has been set.
   * @see #setUserStartingMessageKey
  **/
  public byte[] getUserStartingMessageKey()
  {
    return userStartingMessageKey_;
  }

  /**
   * Returns the starting message key, if one has been set.
   * @return The key, or null if none has been set.
   * @see #setWorkstationStartingMessageKey
  **/
  public byte[] getWorkstationStartingMessageKey()
  {
    return workstationStartingMessageKey_;
  }

  /**
   * Returns whether or not messages that need a reply are included in the list of returned messages.
   * If all three message selection getters return true, it is the equivalent of all messages being included in the
   * list of returned messages.
   * By default, all messages are returned, so this method returns true.
   * @return true if messages that need a reply are included in the list of returned messages; false if messages
   * that need a reply are excluded from the list of returned messages.
   * @see #isSelectMessagesNoNeedReply
   * @see #isSelectSendersCopyMessagesNeedReply
   * @see #setSelectMessagesNeedReply
  **/
  public boolean isSelectMessagesNeedReply()
  {
    return selectMessagesNeedReply_;
  }

  /**
   * Returns whether or not messages that do not need a reply are included in the list of returned messages.
   * If all three message selection getters return true, it is the equivalent of all messages being included in the
   * list of returned messages.
   * By default, all messages are returned, so this method returns true.
   * @return true if messages that do not need a reply are included in the list of returned messages; false if messages
   * that do not need a reply are excluded from the list of returned messages.
   * @see #isSelectMessagesNeedReply
   * @see #isSelectSendersCopyMessagesNeedReply
   * @see #setSelectMessagesNoNeedReply
  **/
  public boolean isSelectMessagesNoNeedReply()
  {
    return selectMessagesNoNeedReply_;
  }

  /**
   * Returns whether or not sender's copy messages that need a reply are included in the list of returned messages.
   * If all three message selection getters return true, it is the equivalent of all messages being included in the
   * list of returned messages.
   * By default, all messages are returned, so this method returns true.
   * @return true if sender's copy messages that need a reply are included in the list of returned messages; false if
   * sender's copy messages that need a reply are excluded from the list of returned messages.
   * @see #isSelectMessagesNeedReply
   * @see #isSelectMessagesNoNeedReply
   * @see #setSelectSendersCopyMessagesNeedReply
  **/
  public boolean isSelectSendersCopyMessagesNeedReply()
  {
    return selectSendersCopyMessagesNeedReply_;
  }


/**
   * Loads the list of messages on the system. This method informs the
   * system to build a list of messages given the previously added
   * attributes to select, retrieve, and sort. This method blocks until the system returns
   * the total number of messages it has compiled. A subsequent call to
   * {@link #getMessages getMessages()} will retrieve the actual message information
   * and attributes for each message in the list from the system.
   * <p>This method updates the list length.
   *
   * @exception AS400Exception                  If the system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the system.
   * @exception ObjectDoesNotExistException     If the object does not exist on the system.
   * @exception ServerStartupException          If the server cannot be started.
   * @exception UnknownHostException            If the system cannot be located.
   * @see #getLength
  **/
  public synchronized void load() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (system_ == null) throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);

    // Close the previous list
    if (handle_ != null || handleToClose_ != null)
    {
      close();
    }

    // Generate text objects based on system CCSID
    final int ccsid = system_.getCcsid();
    ConvTable conv = ConvTable.getTable(ccsid, null);
    AS400Text text1 = new AS400Text(1, ccsid, system_);
    AS400Text text6 = new AS400Text(6, ccsid, system_);
    AS400Text text10 = new AS400Text(10, ccsid, system_);

    // Figure out our selection criteria.
    boolean selectAll = (selectMessagesNeedReply_ && selectMessagesNoNeedReply_ && selectSendersCopyMessagesNeedReply_) ||
                        (!selectMessagesNeedReply_ && !selectMessagesNoNeedReply_ && !selectSendersCopyMessagesNeedReply_);
    String selectionCriteria = "";
    int numSelectionCriteria = selectAll ? 1 : 0;
    if (selectAll)
    {
      selectionCriteria = "*ALL      ";
    }
    else
    {
      if (selectMessagesNeedReply_)
      {
        selectionCriteria += "*MNR      ";
        ++numSelectionCriteria;
      }
      if (selectMessagesNoNeedReply_)
      {
        selectionCriteria += "*MNNR     ";
        ++numSelectionCriteria;
      }
      if (selectSendersCopyMessagesNeedReply_)
      {
        selectionCriteria += "*SCNR     ";
        ++numSelectionCriteria;
      }
    }
    int selectionCriteriaLength = selectionCriteria.length();
    boolean v5r3OrGreater = system_.getVRM() >= VRM_530;                //@K1A
    byte[] selectionInfo;                                               //@K1C
    if(v5r3OrGreater)                                                   //@K1A
        selectionInfo = new byte[84+selectionCriteriaLength];           //@K1A
    else                                                                //@K1A
        selectionInfo = new byte[80+selectionCriteriaLength];
    text10.toBytes(listDirection_ ? NEXT : PREVIOUS, selectionInfo, 0);
    byte[] userStartingMessageKey = (userStartingMessageKey_ != null ? userStartingMessageKey_ : (listDirection_ ? OLDEST : NEWEST));
    byte[] workstationStartingMessageKey = (workstationStartingMessageKey_ != null ? workstationStartingMessageKey_ : userStartingMessageKey);
    BinaryConverter.intToByteArray(severity_, selectionInfo, 12);
    BinaryConverter.intToByteArray(maxMessageLength_, selectionInfo, 16); // Only used for fields 401, 402, 403, or 404.
    BinaryConverter.intToByteArray(maxMessageHelpLength_, selectionInfo, 20); // Only used for fields 301 or 302.
    BinaryConverter.intToByteArray(44, selectionInfo, 24); // offset of selection criteria
    BinaryConverter.intToByteArray(numSelectionCriteria, selectionInfo, 28); // number of selection criteria
    int offset = 44+selectionCriteriaLength;
    BinaryConverter.intToByteArray(offset, selectionInfo, 32); // offset of starting message keys
    BinaryConverter.intToByteArray(offset+8, selectionInfo, 36); // offset of identifiers
    if(v5r3OrGreater)                                                                                   //@K1A
        BinaryConverter.intToByteArray(8, selectionInfo, 40); // number of identifiers to return        //@K1A
    else                                                                                                //@K1A
        BinaryConverter.intToByteArray(7, selectionInfo, 40); // number of identifiers to return
    //text10.toBytes(selectionCriteria_, selectionInfo, 44);
    conv.stringToByteArray(selectionCriteria, selectionInfo, 44);
    System.arraycopy(userStartingMessageKey, 0, selectionInfo, offset, 4);
    System.arraycopy(workstationStartingMessageKey, 0, selectionInfo, offset+4, 4);
    BinaryConverter.intToByteArray(302, selectionInfo, offset+8); // Message with replacement data
    BinaryConverter.intToByteArray(601, selectionInfo, offset+12); // Qualified sender job name
    BinaryConverter.intToByteArray(603, selectionInfo, offset+16); // Sending program name
    BinaryConverter.intToByteArray(1001, selectionInfo, offset+20); // Reply status
    BinaryConverter.intToByteArray(501, selectionInfo, offset+24); // Default reply
    BinaryConverter.intToByteArray(404, selectionInfo, offset+28); // Message help with replacement data and formattting characters
    BinaryConverter.intToByteArray(101, selectionInfo, offset+32); // Alert option
    if(v5r3OrGreater)                                                                           //@K1A
        BinaryConverter.intToByteArray(607, selectionInfo, offset+36); //Sender user name       //@K1A

    // Setup program parameters
    ProgramParameter[] parms = new ProgramParameter[10];
    parms[0] = new ProgramParameter(82); // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(82)); // length of receiver variable
    parms[2] = new ProgramParameter(80); // list information
    parms[3] = new ProgramParameter(BinaryConverter.intToByteArray(1)); // number of records to return (have to specify at least 1... for some reason 0 doesn't work)
    parms[4] = new ProgramParameter(new byte[] { sort_ && selectAll ? (byte)0xF1 : (byte)0xF0}); // Sort information, '0' = no sort, '1' = sort if *ALL is specified
    parms[5] = new ProgramParameter(selectionInfo); // Message selection information
    parms[6] = new ProgramParameter(BinaryConverter.intToByteArray(selectionInfo.length)); // Size of message selection information
    byte[] userOrQueueInfo = new byte[21];
    if (path_.equals(CURRENT))
    {
      userOrQueueInfo[0] = (byte)0xF0; // The user name is used.
      text10.toBytes(system_.getUserId(), userOrQueueInfo, 1);
      text10.toBytes("QUSRSYS", userOrQueueInfo, 11);
    }
    else
    {
      userOrQueueInfo[0] = (byte)0xF1; // The message queue name is used.
      QSYSObjectPathName p = new QSYSObjectPathName(path_, "MSGQ");
      String name = p.getObjectName();
      String lib = p.getLibraryName();
      text10.toBytes(name, userOrQueueInfo, 1);
      text10.toBytes(lib, userOrQueueInfo, 11);
    }
    parms[7] = new ProgramParameter(userOrQueueInfo); // User or queue information
    parms[8] = new ProgramParameter(44); // Message queues used
    parms[9] = errorCode_;

    // Call the program
    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYOLMSG.PGM", parms);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }

    isConnected_ = true;

    // List information returned
    byte[] listInformation = parms[2].getOutputData();
    ListUtilities.checkListStatus(listInformation[30]);  // check the list status indicator
    handle_ = new byte[4];
    System.arraycopy(listInformation, 8, handle_, 0, 4);

    // This second program call is to retrieve the number of messages in the list.
    // It will wait until the server has fully populated the list before it
    // returns.
    ProgramParameter[] parms2 = new ProgramParameter[7];
    parms2[0] = new ProgramParameter(1); // receiver variable
    parms2[1] = new ProgramParameter(BinaryConverter.intToByteArray(1)); // length of receiver variable
    parms2[2] = new ProgramParameter(handle_); // request handle
    parms2[3] = new ProgramParameter(80); // list information
    parms2[4] = new ProgramParameter(BinaryConverter.intToByteArray(0)); // number of records to return
    parms2[5] = new ProgramParameter(BinaryConverter.intToByteArray(-1)); // starting record
    parms2[6] = errorCode_;

    ProgramCall pc2 = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYGTLE.PGM", parms2);
    if (!pc2.run())
    {
      throw new AS400Exception(pc2.getMessageList());
    }
    byte[] listInfo2 = parms2[3].getOutputData();
    ListUtilities.checkListStatus(listInfo2[30]);  // check the list status indicator
    length_ = BinaryConverter.byteArrayToInt(listInfo2, 0);

    if (Trace.traceOn_)
    {
      Trace.log(Trace.DIAGNOSTIC, "Loaded message list with length = "+length_+" and handle: ", handle_);
    }
  }

  /**
   * Deserializes the resource.
**/
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();
  }


/**
   * Receives a message from the message queue by key.  This method
   * receives a message of any type except sender's copy.  The message is removed
   * from the message queue.  See the <a href="#receiveIDs">list of QueuedMessage
   * attribute values</a> which are set on a received message.
   * @param messageKey The message key.
   * @return The queued message, or null if the message can not be received.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @see com.ibm.as400.access.QueuedMessage#getKey
**/
  public QueuedMessage receive(byte[] messageKey)
  throws AS400Exception,
  AS400SecurityException,
  IOException,
  ObjectDoesNotExistException,
  ErrorCompletingRequestException,
  InterruptedException
  {
    return receive(messageKey, 0, REMOVE, ANY);
  }


/**
   * Receives a message from the message queue.  See the <a href="#receiveIDs">list of QueuedMessage
   * attribute values</a> which are set on a received message.
   * @param messageKey The message key, or null if no message key is needed.
   * @param waitTime The number of seconds to wait for the message to arrive in the queue
                        so it can be received.  If the message is not received within the
                        specified wait time, null is returned.  Special values are:
                        <ul>
                        <li>0 - Do not wait for the message. If the message is not in the
                                queue and you specified a message key, null is returned.
                        <li>-1 - Wait until the message arrives in the queue and is received,
                                 no matter how long it takes. The system has no limit for
                                 the wait time.
                        </ul>
   * @param messageAction The action to take after the message is received. Valid values are:
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

   * @param messageType The type of message to return.  Valid values are:
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
   *                             with the specified key.  The message key is required.
   *                         <li>REPLY -
   *                             Receives the reply to an inquiry message.  For the
   *                             message key, you can use the key to the sender's copy
   *                             of the inquiry or notify message.  The message key is
   *                             optional.
   *                         </ul>
   * @return The queued message, or null if the message can not be received.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @see com.ibm.as400.access.QueuedMessage#getKey
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
    if (messageAction == null) throw new NullPointerException("messageAction");
    if (messageType == null) throw new NullPointerException("messageType");
    if (!messageAction.equals(OLD) &&
        !messageAction.equals(REMOVE) &&
        !messageAction.equals(SAME))
    {
      throw new ExtendedIllegalArgumentException("messageAction", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    if (!messageType.equals(ANY) &&
        !messageType.equals(COMPLETION) &&
        !messageType.equals(COPY) &&
        !messageType.equals(DIAGNOSTIC) &&
        !messageType.equals(FIRST) &&
        !messageType.equals(INFORMATIONAL) &&
        !messageType.equals(INQUIRY) &&
        !messageType.equals(LAST) &&
        !messageType.equals(NEXT) &&
        !messageType.equals(PREVIOUS) &&
        !messageType.equals(REPLY))
    {
      throw new ExtendedIllegalArgumentException("messageType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    if (messageKey == null && (messageType.equals(COPY) || messageType.equals(NEXT) || messageType.equals(PREVIOUS)))
    {
      throw new ExtendedIllegalArgumentException("messageKey", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    if (system_ == null) throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);

    if (messageKey == null) messageKey = new byte[]
      {
        0x40, 0x40, 0x40, 0x40
      }; // Use all spaces for null key.

    int ccsid = system_.getCcsid();
    ConvTable conv = ConvTable.getTable(ccsid, null);

    ProgramParameter[] parms = new ProgramParameter[9];
    parms[0] = new ProgramParameter(5120); // message information
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(5120)); // length of message information
    parms[2] = new ProgramParameter(conv.stringToByteArray("RCVM0200")); // format name
    String name = null;
    String lib = null;
    if (path_.equals(CURRENT))
    {
      name = system_.getUserId();
      lib = "QUSRSYS";
    }
    else
    {
      QSYSObjectPathName p = new QSYSObjectPathName(path_, "MSGQ");
      name = p.getObjectName();
      lib = p.getLibraryName();
    }
    byte[] messageQueueName = new byte[20];
    AS400Text text10 = new AS400Text(10, ccsid);
    text10.toBytes(name, messageQueueName, 0);
    text10.toBytes(lib, messageQueueName, 10);
    parms[3] = new ProgramParameter(messageQueueName); // qualified message queue name
    parms[4] = new ProgramParameter(text10.toBytes(messageType)); // message type
    parms[5] = new ProgramParameter(messageKey); // message key
    parms[6] = new ProgramParameter(BinaryConverter.intToByteArray(waitTime)); // wait time
    parms[7] = new ProgramParameter(text10.toBytes(messageAction)); // message action
    parms[8] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QMHRCVM.PGM", parms);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }

    byte[] data = parms[0].getOutputData();

    int bytesReturned = BinaryConverter.byteArrayToInt(data, 0);
    int bytesAvailable = BinaryConverter.byteArrayToInt(data, 4);

    if (bytesReturned == 8 && bytesAvailable == 0) return null; // No message found.

    int messageSeverity = BinaryConverter.byteArrayToInt(data, 8);
    String messageIdentifier = conv.byteArrayToString(data, 12, 7).trim(); // Blank for impromptu message.

    boolean isImpromptu = messageIdentifier.length() == 0;

    int returnedType = Integer.parseInt(conv.byteArrayToString(data, 19, 2));
    byte[] returnedKey = new byte[4];
    System.arraycopy(data, 21, returnedKey, 0, 4); // Blank for a message action of *REMOVE.
    String messageFileName = conv.byteArrayToString(data, 25, 10).trim();
    String messageFileLibraryUsed = conv.byteArrayToString(data, 45, 10).trim();

    String sendingJob = conv.byteArrayToString(data, 55, 10).trim();
    String sendingUserProfile = conv.byteArrayToString(data, 65, 10).trim();
    String sendingJobNumber = conv.byteArrayToString(data, 75, 6);
    String sendingProgramName = conv.byteArrayToString(data, 81, 12).trim();
    String dateSent = conv.byteArrayToString(data, 97, 7); // CYYMMDD
    String timeSent = conv.byteArrayToString(data, 104, 6); // HHMMSS
    String alertOption = conv.byteArrayToString(data, 135, 9).trim();

    int ccsidStatusText = BinaryConverter.byteArrayToInt(data, 127);
    int ccsidMessage = BinaryConverter.byteArrayToInt(data, 144);
    int lengthDataReturned = BinaryConverter.byteArrayToInt(data, 152);

    int ccsidToUse = ccsidMessage;
    if (ccsidStatusText == -1 || ccsidToUse == 0 || ccsidToUse == 65535)
    {
      // An error occurred and the data was not converted.
      ccsidToUse = ccsid;
    }
    ConvTable conv2 = ConvTable.getTable(ccsidToUse, null);

    if (isImpromptu)
    {
      byte[] impromptuMessageBytes = new byte[lengthDataReturned];
      System.arraycopy(data, 176, impromptuMessageBytes, 0, lengthDataReturned);
      String impromptuMessage = conv2.byteArrayToString(impromptuMessageBytes, 0, lengthDataReturned, 0);
      QueuedMessage message = new QueuedMessage(this, messageSeverity, messageIdentifier,
                                                returnedType, returnedKey, messageFileName, messageFileLibraryUsed,
                                                sendingJob, sendingUserProfile, sendingJobNumber,
                                                sendingProgramName, dateSent, timeSent,
                                                impromptuMessageBytes, impromptuMessage, null, alertOption);
      resetHandle();
      return message;
    }

    int lengthMessageReturned = BinaryConverter.byteArrayToInt(data, 160);
    int lengthHelpReturned = BinaryConverter.byteArrayToInt(data, 168);

    byte[] replacementDataBytes = new byte[lengthDataReturned];
    System.arraycopy(data, 176, replacementDataBytes, 0, lengthDataReturned);

    String messageData = conv2.byteArrayToString(data, 176+lengthDataReturned, lengthMessageReturned);
    String messageHelp = conv2.byteArrayToString(data, 176+lengthDataReturned+lengthMessageReturned, lengthHelpReturned); // Blank for an immediate message.

    QueuedMessage message = new QueuedMessage(this, messageSeverity, messageIdentifier,
                                              returnedType, returnedKey, messageFileName, messageFileLibraryUsed,
                                              sendingJob, sendingUserProfile, sendingJobNumber,
                                              sendingProgramName, dateSent, timeSent,
                                              replacementDataBytes, messageData, messageHelp, alertOption);
    resetHandle();
    return message;
  }


/**
   * Remove all messages from the message queue.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
  public void remove()
  throws AS400Exception,
  AS400SecurityException,
  IOException,
  ObjectDoesNotExistException,
  ErrorCompletingRequestException,
  InterruptedException
  {
    removeMessages(ALL, blankKey_);
  }

/**
   * Removes a message from the message queue.
   * @param messageKey The message key.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
  public void remove(byte[] messageKey)
  throws AS400Exception,
  AS400SecurityException,
  IOException,
  ObjectDoesNotExistException,
  ErrorCompletingRequestException,
  InterruptedException
  {
    if (messageKey == null) throw new NullPointerException("messageKey");
    removeMessages(BYKEY, messageKey);
  }


/**
   * Remove messages from the message queue.
   * @param messageType The type of message to remove.  Valid values are:
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
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
  public void remove(String messageType)
  throws AS400Exception,
  AS400SecurityException,
  IOException,
  ObjectDoesNotExistException,
  ErrorCompletingRequestException,
  InterruptedException
  {
    if (messageType == null) throw new NullPointerException("messageType");
    if (!messageType.equals(ALL) &&
        !messageType.equals(KEEP_UNANSWERED) &&
        !messageType.equals(NEW) &&
        !messageType.equals(OLD))
    {
      throw new ExtendedIllegalArgumentException("messageType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    removeMessages(messageType, blankKey_);
  }


  // Helper method.
  private void removeMessages(String type, byte[] key)
  throws AS400Exception,
  AS400SecurityException,
  IOException,
  ObjectDoesNotExistException,
  ErrorCompletingRequestException,
  InterruptedException
  {
    ProgramParameter[] parms = new ProgramParameter[4];
    String queue = null;
    String library = null;
    if (path_.equals(CURRENT))
    {
      queue = system_.getUserId();
      library = "QUSRSYS";
    }
    else
    {
      QSYSObjectPathName pathname = new QSYSObjectPathName(path_);
      library = pathname.getLibraryName();
      queue = pathname.getObjectName();
    }
    AS400Text text10 = new AS400Text(10, system_.getCcsid(), system_);
    byte[] nameBytes = new byte[20];
    text10.toBytes(queue, nameBytes, 0);
    text10.toBytes(library, nameBytes, 10);
    parms[0] = new ProgramParameter(nameBytes); // Qualified message queue name
    parms[1] = new ProgramParameter(key); // Message key
    parms[2] = new ProgramParameter(text10.toBytes(type)); // Messages to remove
    parms[3] = errorCode_;
    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QMHRMVM.PGM", parms);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }
    resetHandle();
  }


/**
   * Removes a PropertyChangeListener.
   * @param listener The listener.
**/
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener");
    if (propertyChangeSupport_ != null) propertyChangeSupport_.removePropertyChangeListener(listener);
  }


/**
   * Removes a VetoableChangeListener.
   * @param listener The listener.
**/
  public void removeVetoableChangeListener(VetoableChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener");
    if (vetoableChangeSupport_ != null) vetoableChangeSupport_.removeVetoableChangeListener(listener);
  }


/**
   * Replies to and removes a message.
   * @param messageKey The message key.
   * @param replyText The reply. To send the default reply stored in the message description,
   * use blanks for this parameter.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
  public void reply(byte[] messageKey, String replyText)
  throws AS400Exception,
  AS400SecurityException,
  IOException,
  ObjectDoesNotExistException,
  ErrorCompletingRequestException,
  InterruptedException
  {
    reply(messageKey, replyText, true);
  }


/**
   * Replies to and removes a message if requested.
   * @param messageKey The message key.
   * @param replyText The reply. To send the default reply stored in the message description,
   * use blanks for this parameter.
   * @param remove true to remove the inquiry message and the reply from the
                    message queue after the reply is sent, false to keep the
                    inquiry message and the reply after the reply is sent.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
  public void reply(byte[] messageKey, String replyText, boolean remove)
  throws AS400Exception,
  AS400SecurityException,
  IOException,
  ObjectDoesNotExistException,
  ErrorCompletingRequestException,
  InterruptedException
  {
    if (messageKey == null) throw new NullPointerException("messageKey");
    if (replyText == null) throw new NullPointerException("replyText");
    ProgramParameter[] parms = new ProgramParameter[6];
    parms[0] = new ProgramParameter(messageKey); // Message key
    String library = null;
    String queue = null;
    if (path_.equals(CURRENT))
    {
      queue = system_.getUserId();
      library = "QUSRSYS";
    }
    else
    {
      QSYSObjectPathName pathname = new QSYSObjectPathName(path_);
      library = pathname.getLibraryName();
      queue = pathname.getObjectName();
    }
    int ccsid = system_.getCcsid();
    ConvTable conv = ConvTable.getTable(ccsid, null);
    AS400Text text10 = new AS400Text(10, ccsid);
    byte[] nameBytes = new byte[20];
    text10.toBytes(queue, nameBytes, 0);
    text10.toBytes(library, nameBytes, 10);
    parms[1] = new ProgramParameter(nameBytes); // Qualified message queue name
    byte[] replyBytes = conv.stringToByteArray(replyText);
    parms[2] = new ProgramParameter(replyBytes); // Reply text
    parms[3] = new ProgramParameter(BinaryConverter.intToByteArray(replyBytes.length)); // Length of reply text
    parms[4] = new ProgramParameter(text10.toBytes(remove ? "*YES" : "*NO")); // Remove message
    parms[5] = errorCode_;
    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QMHSNDRM.PGM", parms);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }
    resetHandle();
  }


  // Resets the handle to indicate we should close the list the next time
  // we do something, usually as a result of one of the selection criteria
  // being changed since that should build a new list on the server.
  private synchronized void resetHandle()
  {
    if (handleToClose_ == null) handleToClose_ = handle_; // Close the old list on the next load
    handle_ = null;
  }


  // 20 EBCDIC SBCS blanks
  private static final byte[] blanks20_ = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40,
    0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40};


  // Helper method
  private byte[] send(String messageID, String messageFile, byte[] data, String type, String replyQueue)
  throws AS400Exception,
  AS400SecurityException,
  IOException,
  ObjectDoesNotExistException,
  ErrorCompletingRequestException,
  InterruptedException
  {
    // Assume all parms have been validated at this point.
    ProgramParameter[] parms = new ProgramParameter[10];
    int ccsid = system_.getCcsid();
    AS400Text text7 = new AS400Text(7, ccsid);
    AS400Text text10 = new AS400Text(10, ccsid);
    parms[0] = new ProgramParameter(text7.toBytes(messageID == null ? "" : messageID)); // message identifier
    byte[] b = null;
    if (messageFile == null)
    {
      b = blanks20_;
    }
    else
    {
      b = new byte[20];
      QSYSObjectPathName path = new QSYSObjectPathName(messageFile);
      text10.toBytes(path.getObjectName(), b, 0);
      text10.toBytes(path.getLibraryName(), b, 10);
    }
    if (data == null) data = new byte[0];
    parms[1] = new ProgramParameter(b); // qualified message file name
    parms[2] = new ProgramParameter(data); // replacement data or impromptu message text
    parms[3] = new ProgramParameter(BinaryConverter.intToByteArray(data.length)); // length of replacement data or impromptu message text: 0-32767 for replacement, 1-6000 for impromptu
    parms[4] = new ProgramParameter(text10.toBytes(type)); // message type
    String queue = null;
    String lib = null;
    if (path_.equals(CURRENT))
    {
      queue = system_.getUserId();
      lib = "*USER";
    }
    else
    {
      QSYSObjectPathName path = new QSYSObjectPathName(path_);
      queue = path.getObjectName();
      lib = path.getLibraryName();
    }
    byte[] queueName = new byte[20];
    text10.toBytes(queue, queueName, 0);
    text10.toBytes(lib, queueName, 10);
    parms[5] = new ProgramParameter(queueName); // list of qualified message queue names
    parms[6] = new ProgramParameter(BinaryConverter.intToByteArray(1)); // number of message queues
    if (type.equals(INQUIRY))
    {
      b = new byte[20];
      QSYSObjectPathName path = new QSYSObjectPathName(replyQueue);
      text10.toBytes(path.getObjectName(), b, 0);
      text10.toBytes(path.getLibraryName(), b, 10);
    }
    else
    {
      b = blanks20_;
    }
    parms[7] = new ProgramParameter(b); // qualified name of the reply message queue
    parms[8] = new ProgramParameter(4); // message key
    parms[9] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QMHSNDM.PGM", parms);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }

    byte[] key = parms[8].getOutputData();
    resetHandle();
    return key;
  }


/**
   * Sends an informational message to the message queue.
   * @param messageID The message ID.
   * @param messageFile The integrated file system path name of the message file.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
  public void sendInformational(String messageID, String messageFile)
  throws AS400Exception,
  AS400SecurityException,
  IOException,
  ObjectDoesNotExistException,
  ErrorCompletingRequestException,
  InterruptedException
  {
    if (messageID == null) throw new NullPointerException("messageID");
    if (messageFile == null) throw new NullPointerException("messageFile");
    send(messageID, messageFile, null, INFORMATIONAL, null);
  }


/**
   * Sends an informational message to the message queue.
   * @param messageID The message ID.
   * @param messageFile The integrated file system path name of the message file.
   * @param substitutionData The substitution data for the message, or null if none.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
  public void sendInformational(String messageID, String messageFile, byte[] substitutionData)
  throws AS400Exception,
  AS400SecurityException,
  IOException,
  ObjectDoesNotExistException,
  ErrorCompletingRequestException,
  InterruptedException
  {
    if (messageID == null) throw new NullPointerException("messageID");
    if (messageFile == null) throw new NullPointerException("messageFile");
    send(messageID, messageFile, substitutionData, INFORMATIONAL, null);
  }


/**
   * Sends an informational message to the message queue.
   * @param messageText The message text.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
  public void sendInformational(String messageText)
  throws AS400Exception,
  AS400SecurityException,
  IOException,
  ObjectDoesNotExistException,
  ErrorCompletingRequestException,
  InterruptedException
  {
    if (messageText == null) throw new NullPointerException("messageText");
    int ccsid = system_.getCcsid();
    ConvTable conv = ConvTable.getTable(ccsid, null);
    byte[] data = conv.stringToByteArray(messageText);
    send(null, null, data, INFORMATIONAL, null);
  }


/**
   * Sends an inquiry message to the message queue.
   * @param messageID The message ID.
   * @param messageFile The integrated file system path name of the message file.
   * @param replyMessageQueue The integrated file system path name of the reply message queue.
   * @return The message key.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
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
    if (messageID == null) throw new NullPointerException("messageID");
    if (messageFile == null) throw new NullPointerException("messageFile");
    if (replyMessageQueue == null) throw new NullPointerException("replyMessageQueue");
    return send(messageID, messageFile, null, INQUIRY, replyMessageQueue);
  }


/**
   * Sends an inquiry message to the message queue.
   * @param messageID The message ID.
   * @param messageFile The integrated file system path name of the message file.
   * @param substitutionData The substitution data for the message, or null if none.
   * @param replyMessageQueue The integrated file system path name of the reply message queue.
   * @return The message key.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
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
    if (messageID == null) throw new NullPointerException("messageID");
    if (messageFile == null) throw new NullPointerException("messageFile");
    if (replyMessageQueue == null) throw new NullPointerException("replyMessageQueue");
    return send(messageID, messageFile, substitutionData, INQUIRY, replyMessageQueue);
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
    if (messageText == null) throw new NullPointerException("messageText");
    if (replyMessageQueue == null) throw new NullPointerException("replyMessageQueue");
    int ccsid = system_.getCcsid();
    ConvTable conv = ConvTable.getTable(ccsid, null);
    byte[] data = conv.stringToByteArray(messageText);
    return send(null, null, data, INQUIRY, replyMessageQueue);
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
    if (helpTextFormatting != MessageFile.NO_FORMATTING &&
        helpTextFormatting != MessageFile.RETURN_FORMATTING_CHARACTERS &&
        helpTextFormatting != MessageFile.SUBSTITUTE_FORMATTING_CHARACTERS)
      throw new ExtendedIllegalArgumentException("helpTextFormatting", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    helpTextFormatting_ = helpTextFormatting;
    resetHandle();
  }



// @E1A
/**
Sets the list direction.

@param listDirection true to list the messages in order from oldest to
                     newest; false to list the messages in order from newest
                     to oldest.  The default is true.
@see #getListDirection                     
**/
  public void setListDirection(boolean listDirection)
  {
    listDirection_ = listDirection;
    resetHandle();
  }


  /**
   * Sets whether the list should be sorted by message type when {@link #ALL ALL} messages are selected for retrieval.
   * If the selection criteria is set to something other than {@link #ALL ALL}, this sort setting is ignored.
   * @param sort true to indicate the messages should be sorted; false to indicate
   * no sorting should be performed on the message list.
   * @see #getSort
  **/
  public void setSort(boolean sort)
  {
    sort_ = sort;
    resetHandle();
  }


/**
Sets the fully qualified integrated file system path name of the
message queue.  The default is CURRENT. The path cannot be changed
if the MessageQueue object has established a connection to the server.

@param path The fully qualified integrated file system path name of the
            message queue, or CURRENT to refer to the user's default
            message queue.

@exception PropertyVetoException If the change is vetoed.
**/
  public void setPath(String path)
  throws PropertyVetoException
  {
    if (path == null) throw new NullPointerException("path");
    if (isConnected_)
    {
      throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }
    String old = path_;
    if (vetoableChangeSupport_ != null) vetoableChangeSupport_.fireVetoableChange("path", old, path);
    path_ = path;
    if (propertyChangeSupport_ != null) propertyChangeSupport_.firePropertyChange("path", old, path);
  }

  /**
   * Sets whether or not to include messages that need a reply in the returned list of messages.
   * Passing true to all three message selection setters is equivalent to retrieving all the messages.
   * By default, all messages are retrieved.
   * @param select true to include messages that need a reply; false to exclude messages that need a reply.
   * @see #isSelectMessagesNeedReply
   * @see #setSelectMessagesNoNeedReply
   * @see #setSelectSendersCopyMessagesNeedReply
  **/
  public void setSelectMessagesNeedReply(boolean select)
  {
    selectMessagesNeedReply_ = select;
    resetHandle();
  }
  
  /**
   * Sets whether or not to include messages that do not need a reply in the returned list of messages.
   * Passing true to all three message selection setters is equivalent to retrieving all the messages.
   * By default, all messages are retrieved.
   * @param select true to include messages that do not need a reply; false to exclude messages that do not need a reply.
   * @see #isSelectMessagesNoNeedReply
   * @see #setSelectMessagesNeedReply
   * @see #setSelectSendersCopyMessagesNeedReply
  **/
  public void setSelectMessagesNoNeedReply(boolean select)
  {
    selectMessagesNoNeedReply_ = select;
    resetHandle();
  }
  
  /**
   * Sets whether or not to include sender's copy messages that need a reply in the returned list of messages.
   * Passing true to all three message selection setters is equivalent to retrieving all the messages.
   * By default, all messages are retrieved.
   * @param select true to include sender's copy messages that need a reply; false to exclude sender's copy
   * messages that need a reply.
   * @see #isSelectSendersCopyMessagesNeedReply
   * @see #setSelectMessagesNeedReply
   * @see #setSelectMessagesNoNeedReply
  **/
  public void setSelectSendersCopyMessagesNeedReply(boolean select)
  {
    selectSendersCopyMessagesNeedReply_ = select;
    resetHandle();
  }

/**
Sets the selection that describes which messages are returned.
The default is ALL. This takes effect the next time the list
of queue messages is retrieved or refreshed.
<P>
Note: This method resets the selection criteria set by the
{@link #setSelectMessagesNeedReply setSelectMessagesNeedReply()},
{@link #setSelectMessagesNoNeedReply setSelectMessagesNoNeedReply()}, and
{@link #setSelectSendersCopyMessagesNeedReply setSelectSendersCopyMessagesNeedReply()}.
Using this method will only set one of the above to true, unless {@link #ALL ALL} is
specified, which will set all three of them to true.
To include combinations of the three criteria, use the individual setters instead of
this method.

@param selection The selection.  Valid values are:
                 <ul>
                 <li>{@link #ALL ALL}
                 <li>{@link #MESSAGES_NEED_REPLY MESSAGES_NEED_REPLY}
                 <li>{@link #MESSAGES_NO_NEED_REPLY MESSAGES_NO_NEED_REPLY}
                 <li>{@link #SENDERS_COPY_NEED_REPLY SENDERS_COPY_NEED_REPLY}
                 </ul>

@exception PropertyVetoException If the change is vetoed.

@see com.ibm.as400.resource.RMessageQueue#SELECTION_CRITERIA
@deprecated Use {@link #setSelectMessagesNeedReply setSelectMessagesNeedReply(boolean)},
{@link #setSelectMessagesNoNeedReply setSelectMessagesNoNeedReply(boolean)}, and
{@link #setSelectSendersCopyMessagesNeedReply setSelectSendersCopyMessagesNeedReply(boolean)} instead.
**/
  public void setSelection(String selection)
  throws PropertyVetoException
  {
    if (selection == null) throw new NullPointerException("selection");
    if (!selection.equals(ALL) &&
        !selection.equals(MESSAGES_NEED_REPLY) &&
        !selection.equals(MESSAGES_NO_NEED_REPLY) &&
        !selection.equals(SENDERS_COPY_NEED_REPLY))
    {
      throw new ExtendedIllegalArgumentException("selection", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    String old = selectionCriteria_;
    if (vetoableChangeSupport_ != null) vetoableChangeSupport_.fireVetoableChange("selection", old, selection);
    selectionCriteria_ = selection;
    
    boolean all = selection.equals(ALL);
    selectMessagesNeedReply_ = all || selection.equals(MESSAGES_NEED_REPLY);
    selectMessagesNoNeedReply_ = all || selection.equals(MESSAGES_NO_NEED_REPLY);
    selectSendersCopyMessagesNeedReply_ = all || selection.equals(SENDERS_COPY_NEED_REPLY);
    
    if (propertyChangeSupport_ != null) propertyChangeSupport_.firePropertyChange("selection", old, selection);
    resetHandle();
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
    if ((severity < 0) || (severity > 99)) throw new ExtendedIllegalArgumentException("severity", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    int old = severity_;
    if (vetoableChangeSupport_ != null) vetoableChangeSupport_.fireVetoableChange("severity", old, severity);
    severity_ = severity;
    if (propertyChangeSupport_ != null) propertyChangeSupport_.firePropertyChange("severity", old, severity);
    resetHandle();

  }



/**
Sets the system.  This cannot be changed if the object
has established a connection to the server.

@param system The system.

@exception PropertyVetoException    If the property change is vetoed.
**/
  public void setSystem(AS400 system)
  throws PropertyVetoException
  {
    if (system == null) throw new NullPointerException("system");
    if (isConnected_)
    {
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }
    AS400 old = system_;
    if (vetoableChangeSupport_ != null) vetoableChangeSupport_.fireVetoableChange("system", old, system);
    system_ = system;
    if (propertyChangeSupport_ != null) propertyChangeSupport_.firePropertyChange("system", old, system);
  }

  /**
   * Sets the starting message key used to begin searching for messages to list
   * from the corresponding entry in the message queue. Any valid message key
   * will work, including {@link #NEWEST NEWEST} and {@link #OLDEST OLDEST}.
   * If the key of a reply message is specified, the message search begins
   * with the inquiry or sender's copy message associated with that reply,
   * not the reply message itself.
   * <P>
   * If the message queue is set to {@link #CURRENT CURRENT}, then the key
   * represents the starting message key for the current user's user message queue.
   * @param key The key. Specify null to set it back to the default, which will
   * be OLDEST or NEWEST based on the list direction.
  **/
  public void setUserStartingMessageKey(byte[] key)
  {
    userStartingMessageKey_ = key;
    resetHandle();
  }

  /**
   * Sets the starting message key used to begin searching for messages to list
   * from the corresponding entry in the message queue. Any valid message key
   * will work, including {@link #NEWEST NEWEST} and {@link #OLDEST OLDEST}.
   * If the key of a reply message is specified, the message search begins
   * with the inquiry or sender's copy message associated with that reply,
   * not the reply message itself.
   * <P>
   * If the message queue is set to {@link #CURRENT CURRENT}, then the key
   * represents the starting message key for the current user's workstation message queue.
   * @param key The key. Specify null to set it back to the default.
  **/
  public void setWorkstationStartingMessageKey(byte[] key)
  {
    workstationStartingMessageKey_ = key;
    resetHandle();
  }
}




