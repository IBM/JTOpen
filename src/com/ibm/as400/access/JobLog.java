///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JobLog.java
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
import java.net.UnknownHostException;
import java.util.Enumeration;



/**
The JobLog class represents an OS/400 job log.  This is used
to get a list of messages in a job log or to write messages to a job log.

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
@see com.ibm.as400.access.QueuedMessage
@see com.ibm.as400.resource.RJobLog
**/
public class JobLog implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";



  static final long serialVersionUID = 4L;

  private transient   PropertyChangeSupport   propertyChangeSupport_;
  private transient   VetoableChangeSupport   vetoableChangeSupport_;


  private AS400 system_;
  private String name_ = "*";
  private String user_ = "";
  private String number_ = "";

  private int length_;
  private byte[] handle_; // handle that references the user space used by the open list APIs
  private byte[] handleToClose_; // used to close a previously opened list
  private boolean isConnected_;

  // We don't currently allow the user to specify a direction or any other
  // attributes, so we just hardcode these.
  private static final int maxMessageLength_ = 511;
  private static final int maxMessageHelpLength_ = 3000;

  private static final ProgramParameter errorCode_ = new ProgramParameter(new byte[4]);

  private boolean listDirection_ = true;
  private byte[] startingMessageKey_;

/**
   * Constructs a JobLog object.
**/
  public JobLog()
  {
  }



/**
Constructs a JobLog object.

@param system The system.
**/
  public JobLog(AS400 system)
  {
    if (system == null) throw new NullPointerException("system");
    system_ = system;
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
    if (system == null) throw new NullPointerException("system");
    if (name == null) throw new NullPointerException("name");
    if (user == null) throw new NullPointerException("user");
    if (number == null) throw new NullPointerException("number");
    system_ = system;
    name_ = name;
    user_ = user;
    number_ = number;
  }



/**
Adds a PropertyChangeListener.  The specified PropertyChangeListener's
<b>propertyChange()</b> method will be called each time the value of
any bound property is changed.

@param listener The listener.
@see #removePropertyChangeListener
*/
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener");
    if (propertyChangeSupport_ == null) propertyChangeSupport_ = new PropertyChangeSupport(this);
    propertyChangeSupport_.addPropertyChangeListener(listener);
  }



/**
Adds a VetoableChangeListener.  The specified VetoableChangeListener's
<b>vetoableChange()</b> method will be called each time the value of
any constrained property is changed.

@param listener The listener.
@see #removeVetoableChangeListener
*/
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
      Trace.log(Trace.DIAGNOSTIC, "Closing job log message list with handle: ", handle_);
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
   * Returns the number of messages in the job log. This method implicitly calls {@link #load load()}.
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
   * @return true if the messages will be sorted oldest to newest; false if they will
   * be sorted newest to oldest. The default is true.
  **/
  public boolean getListDirection()
  {
    return listDirection_;
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
    if (system_ == null)
    {
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (handle_ == null)
    {
      load(); // Need to get the length_
    }

    return new QueuedMessageEnumeration(this, length_);
  }


  /**
   * Returns a subset of the list of messages in the job log.
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
    int len = 700*number;
    parms2[0] = new ProgramParameter(len); // receiver variable
    parms2[1] = new ProgramParameter(BinaryConverter.intToByteArray(len)); // length of receiver variable
    parms2[2] = new ProgramParameter(handle_);
    parms2[3] = new ProgramParameter(80); // list information
    parms2[4] = new ProgramParameter(BinaryConverter.intToByteArray(number)); // number of records to return
    parms2[5] = new ProgramParameter(BinaryConverter.intToByteArray(listOffset == -1 ? -1 : listOffset+1)); // starting record
    parms2[6] = errorCode_;

    ProgramCall pc2 = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYGTLE.PGM", parms2);
    boolean success = pc2.run();
    AS400Message[] msgs = pc2.getMessageList();
    while (!success && msgs != null && msgs.length == 1 && msgs[0].getID().equalsIgnoreCase("GUI0002"))
    {
      len = len*2;
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Calling JobLog QGYGTLE again after GUI0002 with an updated length of "+len+".");
      try
      {
        parms2[0].setOutputDataLength(len);
        parms2[1].setInputData(BinaryConverter.intToByteArray(len));
      }
      catch (PropertyVetoException pve)
      {
      }
      success = pc2.run();
      msgs = pc2.getMessageList();
    }
    if (!success)
    {
      throw new AS400Exception(msgs);
    }

    byte[] listInfo = parms2[3].getOutputData();
    int totalRecords = BinaryConverter.byteArrayToInt(listInfo, 0);
    int recordsReturned = BinaryConverter.byteArrayToInt(listInfo, 4);
    while (listOffset == -1 && totalRecords > recordsReturned)
    {
      len = len*(1+(totalRecords/(recordsReturned+1)));
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Calling JobLog QGYGTLE again with an updated length of "+len+".");
      try
      {
        parms2[0].setOutputDataLength(len);
        parms2[1].setInputData(BinaryConverter.intToByteArray(len));
      }
      catch (PropertyVetoException pve)
      {
      }
      if (!pc2.run())
      {
        throw new AS400Exception(pc2.getMessageList());
      }
      listInfo = parms2[3].getOutputData();
      totalRecords = BinaryConverter.byteArrayToInt(listInfo, 0);
      recordsReturned = BinaryConverter.byteArrayToInt(listInfo, 4);
    }

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
      String dateSent = conv.byteArrayToString(data, offset+49, 7); // CYYMMDD
      String timeSent = conv.byteArrayToString(data, offset+56, 6); // HHMMSS

      messages[i] = new QueuedMessage(system_, messageSeverity, messageIdentifier, messageType, //@G1C
                                      messageKey, messageFileName, messageFileLibrarySpecified,
                                      dateSent, timeSent);

      // Our 5 fields should've come back.
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
   * Returns the job name.
   * @return The job name, or "*" if none has been set.
   * @see #setName
  **/
  public String getName()
  {
    return name_;
  }



  /**
   * Returns the job number.
   * @return The job number, or "" if none has been set.
   * @see #setNumber
  **/
  public String getNumber()
  {
    return number_;
  }

  /**
   * Returns the starting message key.
   * @return The key.
   * @see #setStartingMessageKey
  **/
  public byte[] getStartingMessageKey()
  {
    return startingMessageKey_;
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
   * Returns the user name.
   * @return The user name, or "" if none has been set.
   * @see #setUser
  **/
  public String getUser()
  {
    return user_;
  }


  private static final byte[] blanks16_ = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40,
    0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40};

  /**
   * Loads the list of messages on the system. This method informs the
   * system to build a list of messages. This method blocks until the system returns
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
    if (!name_.equals("*"))
    {
      if (user_.equals("")) throw new ExtendedIllegalStateException("user", ExtendedIllegalStateException.PROPERTY_NOT_SET);
      if (number_.equals("")) throw new ExtendedIllegalStateException("number", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

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
    byte[] selectionInfo = new byte[105];
    text10.toBytes(listDirection_ ? MessageQueue.NEXT : MessageQueue.PREVIOUS, selectionInfo, 0);
    text10.toBytes(name_.toUpperCase().trim(), selectionInfo, 10);
    text10.toBytes(user_.toUpperCase().trim(), selectionInfo, 20);
    text6.toBytes(number_, selectionInfo, 30);
    System.arraycopy(blanks16_, 0, selectionInfo, 36, 16); // internal job identifier
    byte[] startingMessageKey = (startingMessageKey_ != null ? startingMessageKey_ : (listDirection_ ? MessageQueue.OLDEST : MessageQueue.NEWEST));
    System.arraycopy(startingMessageKey, 0, selectionInfo, 52, 4);
    // Note: 601 (qualified sender job name) never returns any data for this API,
    // so we leave it out.
    BinaryConverter.intToByteArray(maxMessageLength_, selectionInfo, 56); // Only used for fields 401, 402, 403, or 404.
    BinaryConverter.intToByteArray(maxMessageHelpLength_, selectionInfo, 60); // Only used for fields 301 or 302.
    BinaryConverter.intToByteArray(80, selectionInfo, 64); // offset of identifiers
    BinaryConverter.intToByteArray(6, selectionInfo, 68); // number of identifiers to return
    BinaryConverter.intToByteArray(104, selectionInfo, 72); // offset of call message queue name
    BinaryConverter.intToByteArray(1, selectionInfo, 76); // size of call message queue name
    BinaryConverter.intToByteArray(302, selectionInfo, 80); // Message with replacement data
    BinaryConverter.intToByteArray(603, selectionInfo, 84); // Sending program name
    BinaryConverter.intToByteArray(1001, selectionInfo, 88); // Reply status
    BinaryConverter.intToByteArray(501, selectionInfo, 92); // Default reply
    BinaryConverter.intToByteArray(404, selectionInfo, 96); // Message help with replacement data and formattting characters
    BinaryConverter.intToByteArray(101, selectionInfo, 100); // Alert option
    conv.stringToByteArray("*", selectionInfo, 104); // call message queue name; * means messages from every call of the job are listed.

    // Setup program parameters
    ProgramParameter[] parms = new ProgramParameter[7];
    parms[0] = new ProgramParameter(62); // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(62)); // length of receiver variable
    parms[2] = new ProgramParameter(80); // list information
    parms[3] = new ProgramParameter(BinaryConverter.intToByteArray(1)); // number of records to return (have to specify at least 1... for some reason 0 doesn't work)
    parms[4] = new ProgramParameter(selectionInfo); // Message selection information
    parms[5] = new ProgramParameter(BinaryConverter.intToByteArray(selectionInfo.length)); // Size of message selection information
    parms[6] = errorCode_;

    // Call the program
    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYOLJBL.PGM", parms);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }

    isConnected_ = true;

    // List information returned
    byte[] listInformation = parms[2].getOutputData();
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
    length_ = BinaryConverter.byteArrayToInt(listInfo2, 0);

    if (Trace.traceOn_)
    {
      Trace.log(Trace.DIAGNOSTIC, "Loaded job log message list with length = "+length_+" and handle: ", handle_);
    }
  }






/**
Removes a PropertyChangeListener.

@param listener The listener.
@see #addPropertyChangeListener
**/
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener");
    if (propertyChangeSupport_ != null) propertyChangeSupport_.removePropertyChangeListener(listener);
  }



/**
Removes a VetoableChangeListener.

@param listener The listener.
@see #addVetoableChangeListener
**/
  public void removeVetoableChangeListener(VetoableChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener");
    if (vetoableChangeSupport_ != null) vetoableChangeSupport_.removeVetoableChangeListener(listener);
  }



  // Resets the handle to indicate we should close the list the next time
  // we do something, usually as a result of one of the selection criteria
  // being changed since that should build a new list on the server.
  private synchronized void resetHandle()
  {
    if (handleToClose_ == null) handleToClose_ = handle_; // Close the old list on the next load
    handle_ = null;
  }

  /**
   * Sets the list direction.
   * @param direction true to sort the messages oldest to newest; false to sort them newest to oldest.
   * The default is true.
  **/
  public void setListDirection(boolean direction)
  {
    listDirection_ = direction;
    resetHandle();
  }

/**
Sets the job name.  This cannot be changed
if the object has established a connection to the server.

@param name The job name.

@exception PropertyVetoException If the change is vetoed.
**/
  public void setName(String name)
  throws PropertyVetoException
  {
    if (name == null) throw new NullPointerException("name");
    String old = name_;
    if (vetoableChangeSupport_ != null) vetoableChangeSupport_.fireVetoableChange("name", old, name);
    name_ = name;
    if (propertyChangeSupport_ != null) propertyChangeSupport_.firePropertyChange("name", old, name);
    resetHandle();
  }



/**
Sets the job number. This cannot be changed
if the object has established a connection to the server.

@param number The job number.

@exception PropertyVetoException If the change is vetoed.
**/
  public void setNumber(String number)
  throws PropertyVetoException
  {
    if (number == null) throw new NullPointerException("number");
    String old = number_;
    if (vetoableChangeSupport_ != null) vetoableChangeSupport_.fireVetoableChange("number", old, number);
    number_ = number;
    if (propertyChangeSupport_ != null) propertyChangeSupport_.firePropertyChange("number", old, number);
    resetHandle();
  }



  /**
   * Sets the message key used to begin searching for messages to list from the
   * corresponding entry in the message queue. Any valid message key will work,
   * including {@link com.ibm.as400.access.MessageQueue#OLDEST MessageQueue.OLDEST} and
   * {@link com.ibm.as400.access.MessageQueue#NEWEST MessageQueue#NEWEST}.
   * @param key The key. Specify null to set it back to the default.
  **/
  public void setStartingMessageKey(byte[] key)
  {
    startingMessageKey_ = key;
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
Sets the user name.  This cannot be changed
if the object has established a connection to the server.

@param user The user name.

@exception PropertyVetoException If the change is vetoed.
**/
  public void setUser(String user)
  throws PropertyVetoException
  {
    if (user == null) throw new NullPointerException("user");
    String old = user_;
    if (vetoableChangeSupport_ != null) vetoableChangeSupport_.fireVetoableChange("user", old, user);
    user_ = user;
    if (propertyChangeSupport_ != null) propertyChangeSupport_.firePropertyChange("user", old, user);
    resetHandle();
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
    if (system == null) throw new NullPointerException("system");
    if (messageID == null) throw new NullPointerException("messageID");
    switch (messageType)
    {
      case AS400Message.COMPLETION:
      case AS400Message.DIAGNOSTIC:
      case AS400Message.INFORMATIONAL:
      case AS400Message.ESCAPE:
        break;
      default:
        throw new ExtendedIllegalArgumentException("messageType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    sendProgramMessage(system, messageID, "/QSYS.LIB/QCPFMSG.MSGF", null, messageType, false);
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
    if (system == null) throw new NullPointerException("system");
    if (messageID == null) throw new NullPointerException("messageID");
    switch (messageType)
    {
      case AS400Message.COMPLETION:
      case AS400Message.DIAGNOSTIC:
      case AS400Message.INFORMATIONAL:
      case AS400Message.ESCAPE:
        break;
      default:
        throw new ExtendedIllegalArgumentException("messageType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    sendProgramMessage(system, messageID, "/QSYS.LIB/QCPFMSG.MSGF", substitutionData, messageType, false);
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
    if (system == null) throw new NullPointerException("system");
    if (messageID == null) throw new NullPointerException("messageID");
    switch (messageType)
    {
      case AS400Message.COMPLETION:
      case AS400Message.DIAGNOSTIC:
      case AS400Message.INFORMATIONAL:
      case AS400Message.ESCAPE:
        break;
      default:
        throw new ExtendedIllegalArgumentException("messageType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (messageFile == null) throw new NullPointerException("messageFile");
    QSYSObjectPathName verify = new QSYSObjectPathName(messageFile);
    if (!verify.getObjectType().equals("MSGF"))
    {
      throw new ExtendedIllegalArgumentException("messageFile", ExtendedIllegalArgumentException.PATH_NOT_VALID);
    }
    sendProgramMessage(system, messageID, messageFile, null, messageType, false);

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
    if (system == null) throw new NullPointerException("system");
    if (messageID == null) throw new NullPointerException("messageID");
    switch (messageType)
    {
      case AS400Message.COMPLETION:
      case AS400Message.DIAGNOSTIC:
      case AS400Message.INFORMATIONAL:
      case AS400Message.ESCAPE:
        break;
      default:
        throw new ExtendedIllegalArgumentException("messageType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (messageFile == null) throw new NullPointerException("messageFile");
    QSYSObjectPathName verify = new QSYSObjectPathName(messageFile);
    if (!verify.getObjectType().equals("MSGF"))
    {
      throw new ExtendedIllegalArgumentException("messageFile", ExtendedIllegalArgumentException.PATH_NOT_VALID);
    }
    sendProgramMessage(system, messageID, messageFile, substitutionData, messageType, false);
  }

/**
Writes a program message to the job log for the job in which the program is running.
The message is sent to the Remote Command Host Server (QZRCSRVS) unless true is specified
for the <i>onThread</i> parameter and is invoked while running on the iSeries server.

@param system           The system. The system cannot be null.
@param messageID        The message ID. The message ID cannot be null.
@param messageType      The message type. Possible values are:
                        <ul>
                        <li>AS400Message.COMPLETION
                        <li>AS400Message.DIAGNOSTIC
                        <li>AS400Message.INFORMATIONAL
                        <li>AS400Message.ESCAPE
                        </ul>
                        The message type must be AS400Message.INFORMATIONAL for an immediate
                        message.
@param messageFile      The integrated file system path name of the message file. If null is specified,
                        the message file used is /QSYS.LIB/QCPFMSG.MSGF.
@param substitutionData The substitution data.  The substitution data can be from 0-32767 bytes
                        for a conventional message and from 1-6000 bytes for an immediate message. If null
                        is specified, no substitution data is used.
@param onThread         Whether or not to stay on thread when calling the API to write the message
                        to the job log. true to write the message to the current job's job log, false
                        to write the message to the Remote Command Host Server job's job log. Note
                        that this parameter is meaningless unless this Java program is running on
                        the iSeries server and the system object is using native optimizations.
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
                                  byte[] substitutionData,
                                  boolean onThread)
  throws  AS400SecurityException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  AS400Exception
  {
    if (system == null) throw new NullPointerException("system");
    if (messageID == null) throw new NullPointerException("messageID");
    switch (messageType)
    {
      case AS400Message.COMPLETION:
      case AS400Message.DIAGNOSTIC:
      case AS400Message.INFORMATIONAL:
      case AS400Message.ESCAPE:
        break;
      default:
        throw new ExtendedIllegalArgumentException("messageType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (messageFile == null) throw new NullPointerException("messageFile");
    QSYSObjectPathName verify = new QSYSObjectPathName(messageFile);
    if (!verify.getObjectType().equals("MSGF"))
    {
      throw new ExtendedIllegalArgumentException("messageFile", ExtendedIllegalArgumentException.PATH_NOT_VALID);
    }
    sendProgramMessage(system, messageID, messageFile, substitutionData, messageType, onThread);
  }

  
  private static final byte[] typeCompletion_    = new byte[] { 0x5C, (byte)0xC3, (byte)0xD6, (byte)0xD4, (byte)0xD7, 0x40, 0x40, 0x40, 0x40, 0x40}; // "*COMP     "
  private static final byte[] typeDiagnostic_    = new byte[] { 0x5C, (byte)0xC4, (byte)0xC9, (byte)0xC1, (byte)0xC7, 0x40, 0x40, 0x40, 0x40, 0x40}; // "*DIAG     "
  private static final byte[] typeEscape_        = new byte[] { 0x5C, (byte)0xC5, (byte)0xE2, (byte)0xC3, (byte)0xC1, (byte)0xD7, (byte)0xC5, 0x40, 0x40, 0x40}; // "*ESCAPE   "
  private static final byte[] typeInformational_ = new byte[] { 0x5C, (byte)0xC9, (byte)0xD5, (byte)0xC6, (byte)0xD6, 0x40, 0x40, 0x40, 0x40, 0x40}; // "*INFO     "

  private static final byte[] callStackEntry_ = new byte[] { 0x5C, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40}; // "*         "

  private static void sendProgramMessage(AS400 system, String messageIdentifier,
                                         String messageFile, byte[] replacementData,
                                         int messageType, boolean onThread)
  throws  AS400SecurityException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  AS400Exception
  {
    // Assume all parameters have been validated.
    int ccsid = system.getCcsid();
    ConvTable conv = ConvTable.getTable(ccsid, null);
    AS400Text text7 = new AS400Text(7, ccsid);
    AS400Text text10 = new AS400Text(10, ccsid);
    ProgramParameter[] parms = new ProgramParameter[9];
    parms[0] = new ProgramParameter(text7.toBytes(messageIdentifier)); // message identifier
    byte[] mf = new byte[20];
    QSYSObjectPathName path = new QSYSObjectPathName(messageFile);
    text10.toBytes(path.getObjectName(), mf, 0);
    text10.toBytes(path.getLibraryName(), mf, 10);
    parms[1] = new ProgramParameter(mf); // qualified message file name
    if (replacementData == null) replacementData = new byte[0];
    parms[2] = new ProgramParameter(replacementData); // replacement data or impromptu message text
    parms[3] = new ProgramParameter(BinaryConverter.intToByteArray(replacementData.length)); // length of replacment data or impromptu message text
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
    parms[4] = new ProgramParameter(type); // message type
    parms[5] = new ProgramParameter(callStackEntry_); // call stack entry; specify "*" (length 10) to mean the current call stack entry
    parms[6] = new ProgramParameter(BinaryConverter.intToByteArray(0)); // call stack counter; 0 means to send the message to the specified call stack entry
    parms[7] = new ProgramParameter(4); // message key
    parms[8] = errorCode_;

    ProgramCall pc = new ProgramCall(system, "/QSYS.LIB/QMHSNDPM.PGM", parms);
    pc.setThreadSafe(onThread); // The QMHSNDPM is threadsafe, but we only want to stay on-thread
                                // if the user wants to write to the current job log instead of the
                                // remote command server's job log.
    if (!pc.run())
    {
      // If one message came back and it is the one we sent,
      // then we're OK.
      AS400Message[] msgs = pc.getMessageList();
      if (msgs.length == 1 && msgs[0].getID().equals(messageIdentifier) &&
          msgs[0].getType() == messageType)
      {
        if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "Expected escape message ignored.");
        return;
      }
      throw new AS400Exception(msgs);
    }
  }

}


