///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import java.net.UnknownHostException;

import java.util.Enumeration;
import java.util.Vector;


/**
 * The MessageQueue class represents an AS/400 message queue.
 *
 * <p>For example:
 * <pre>
 * MessageQueue queue = new MessageQueue( as400, MessageQueue.CURRENT );
 * Enumeration e = queue.getMessages ();
 * while (e.hasMoreElements ())
 * {
 *    QueuedMessage message = (QueuedMessage) e.nextElement ();
 *    System.out.println (message.getText ());
 * }
 * </pre>
 *
 * <p>MessageQueue objects generate the following events:
 * <ul>
 * <li>PropertyChangeEvent
 * </ul>
 *
 * @see QueuedMessage
**/
public class MessageQueue implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private static final boolean DEBUG = false;

  static final long serialVersionUID = -4758634419698264622L;
  private Converter converter_ = null; // @D0C @D2C
  private AS400Structure receiverStruct_ = null;

/**
 * Constant referring to all messages in the message queue.
**/
  public final static String ALL                     = "*ALL";

/**
 * Constant referring to any message in the message queue.
**/
  public final static String ANY                     = "*ANY";

/**
 * Constant referring to a message identified by a key.
**/
  public final static String BYKEY                   = "*BYKEY";

/**
 * Constant referring to completion messages.
**/
  public final static String COMPLETION              = "*COMP";

/**
 * Constant referring to the sender's copy of a previously sent
 * inquiry message.
 **/
  public final static String COPY                    = "*COPY";

/**
 * Constant referring to the current user's message queue.
**/
  public final static String CURRENT                 = "*CURRENT";

/**
 * Constant referring to diagnostic messages.
**/
  public final static String DIAGNOSTIC              = "*DIAG";

/**
 * Constant referring to the first message in the message queue.
**/
  public final static String FIRST                   = "*FIRST";

/**
 * Constant referring to informational messages.
**/
  public final static String INFORMATIONAL           = "*INFO";

/**
 * Constant referring to inquiry messages.
**/
  public final static String INQUIRY                 = "*INQ";

/**
 * Constant referring to all messages in the message queue
 * except unanswered inquiry and unanswered senders' copy messages.
**/
  public final static String KEEP_UNANSWERED         = "*KEEPUNANS";

/**
 * Constant referring to the last message in the message queue.
**/
  public final static String LAST                    = "*LAST";

/**
 * Constant referring to messages that need a reply.
**/
  public final static String MESSAGES_NEED_REPLY     = "*MNR";

/**
 * Constant referring to messages that do not need a reply.
**/
  public final static String MESSAGES_NO_NEED_REPLY  = "*MNNR";

/**
 * Constant referring to all new messages in the message queue.
 * New messages are those that have not been received.
**/
  public final static String NEW                     = "*NEW";

/**
 * Constant referring to the next message in the message queue.
**/
  public final static String NEXT                    = "*NEXT";

/**
 * Constant referring to all old messages in the message queue.
 * Old messages are those that have already been received.
**/
  public final static String OLD                     = "*OLD";

/**
 * Constant referring to the previous message in the message queue.
**/
  public final static String PREVIOUS                = "*PRV";

/**
 * Constant indicating that the message should be removed from
 * the message queue.
**/
  public final static String REMOVE                  = "*REMOVE";

/**
 * Constant referring to the reply of an inquiry message.
**/
  public final static String REPLY           = "*RPY";      // @C1

/**
 * Constant indicating that the message should remain in the
 * message queue without changing its new or old designation.
 **/
  public final static String SAME                    = "*SAME";

/**
 * Constant referring to the sender's copies of messages that
 * need replies.
**/
  public final static String SENDERS_COPY_NEED_REPLY = "*SCNR";



  private static final AS400Bin4    intType = new AS400Bin4();



  //The AS400 system the message queue is on.
  private AS400   as400_ = null;
  //The IFS path name of the message queue.
  private String  ifsName      = CURRENT;
  //The library the message queue is in.
//  String library = null;
  //The name of the message queue.
//  String name = null;

  transient private MessageQueueEnumeration lastEnumeration_ = null;
  private int     severity_    = 0;
  private String  selection_   = this.ALL;

  private static final byte[] SORT_ON  = {(byte)0xf1}; // 0xF1 is '1'
  private static final byte[] SORT_OFF = {(byte)0xf0}; // 0xF0 is '0'

  transient PropertyChangeSupport changes = new PropertyChangeSupport(this);
  transient VetoableChangeSupport vetos = new VetoableChangeSupport(this);


/**
 * Constructs a MessageQueue object.  The system property needs to
 * be set before using any method requiring a connection to the AS/400.
**/
  public MessageQueue()
  {
  }

/**
 * Constructs a MessageQueue object.
 *
 * <p>Depending on how the AS400 object was constructed, the user may
 * need to be prompted for the system name, user ID, or password
 * when any method requiring a connection to the AS/400 is used.
 *
 * @param  system  The AS/400 system on which the message queue exists.
 *                 This value cannot be null.
**/
  public MessageQueue( AS400 system )
  {
    if (system == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'system' is null.");
      throw new NullPointerException("system");
    }

    this.as400_ = system;
  }

/**
 * Constructs a MessageQueue object.
 *
 * <p>Depending on how the AS400 object was constructed, the user may
 * need to be prompted for the system name, user ID, or password
 * when any method requiring a connection to the AS/400 is used.
 *
 * @param  system  The AS/400 system on which the message queue exists.
 *                 This value cannot be null.
 * @param  path    The fully qualified integrated file system path name
 *                 of the message queue, or
 *                 CURRENT to refer to the user's default message queue.
 *                 This value cannot be null.
**/
  public MessageQueue( AS400 system, String path )
  {
    this (system);

    if (path == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'path' is null.");
      throw new NullPointerException("path");
    }

    this.ifsName = path;
  }

/**
 * Adds a listener to be notified when the value of any bound
 * property is changed. The <i>propertyChange()</i> method will be be called.
 *
 * @param listener The PropertyChangeListener.
**/
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
      throw new NullPointerException("listener");
    }
    this.changes.addPropertyChangeListener(listener);
  }

/**
 * Adds a listener to be notified when the value of any constrained
 * property is changed. The <i>vetoableChange()</i> method will be called.
 *
 * @param listener The VetoableChangeListener.
**/
  public void addVetoableChangeListener(VetoableChangeListener listener)
  {
    if (listener == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
      throw new NullPointerException("listener");
    }
    this.vetos.addVetoableChangeListener(listener);
  }


/**
 * Returns the number of messages in the list that was most recently
 * retrieved from the AS/400 (the last call to <i>getMessages()</i>).
 *
 * @return The number of messages, or 0 if no list has been retrieved.
**/
  public int getLength()
  {
    if (lastEnumeration_ == null)
        return 0;
    else
        return lastEnumeration_.getLength ();
  }

/**
 * Returns a list of messages in the message queue.
 * A valid AS/400 system must be provided before this call is made.
 *
 * @return An Enumeration of <i>QueuedMessage</i> objects.
 *
 * @exception AS400Exception                  If the AS/400 system returns an error message.
 * @exception AS400SecurityException          If a security or authority error occurs.
 * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
 * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
 * @exception InterruptedException            If this thread is interrupted.
 * @exception IOException                     If an error occurs while communicating with the AS/400.
 * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
 * @exception ServerStartupException          If the AS/400 server cannot be started.
 * @exception UnknownHostException            If the AS/400 system cannot be located.
**/
  public Enumeration getMessages ()
      throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException,
             InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (this.as400_ == null)
    {
      Trace.log(Trace.ERROR, "Attempt to connect before setting system.");
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (this.ifsName == null)
    {
      Trace.log(Trace.ERROR, "Attempt to connect before setting path.");
      throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    ProgramCall pgm = new ProgramCall( this.as400_ );

    ProgramParameter[] parms = new ProgramParameter[10];

    // 1 receiver variable
    parms[0] = new ProgramParameter( 5120 );

    // 2 receiver len
    byte[] msgsize = intType.toBytes(new Integer(5120) );
    parms[1] = new ProgramParameter( msgsize );

    // 3 list information
    parms[2] = new ProgramParameter( 80 );

    // 4 number of records to return
    parms[3] = new ProgramParameter( intType.toBytes(new Integer(13)) );

    // 5 sort information
    byte sort[] = SORT_OFF;
    parms[4] = new ProgramParameter( sort );

    if (DEBUG)
    {
      System.out.println ("Selection: " + selection_);
      System.out.println ("Severity:  " + severity_);
    }

    // 6 * message selection information
    MessageQueueParser parser = new MessageQueueParser (this);
    byte[] selinfo = parser.buildSelectionInfo( this.as400_, severity_, selection_ );
    parms[5] = new ProgramParameter( selinfo );

    // 7 size of message selection information
    parms[6] = new ProgramParameter( intType.toBytes(new Integer(selinfo.length)) );

    String blankString = "                    ";
    String testString = "1" + getQueueName(this.ifsName);
    testString += blankString.substring (0, 11 - testString.length());
    testString += getQueueLibrary(this.ifsName);

    if (DEBUG == true) System.out.println ("library string: " + testString);

    // 8 user or queue information
//    parms[7] = new ProgramParameter( text21Type.toBytes( "0" + this.name )); //This is the old format that allows the user name
    parms[7] = new ProgramParameter( new AS400Text (21, this.as400_.getCcsid(), this.as400_).toBytes( testString ));
    // $$$ allow indicator 1 (q+qlib)

    // 9 message queues used
    parms[8] = new ProgramParameter( 44 );

    // 10 error code ? inout, char*
    parms[9] = new ProgramParameter( intType.toBytes( new Integer(0) ));

    // do it
    byte[] listInfoData = null;
    byte[] receiverData = null;
    try
    {
        if (pgm.run( "/QSYS.LIB/QGY.LIB/QGYOLMSG.PGM", parms )==false)
        {
            // error on run
            throw new AS400Exception( pgm.getMessageList() );
        }

        String qsUsed =
           (String)(new AS400Text(40, this.as400_.getCcsid(), this.as400_)).toObject(parms[8].getOutputData(),4);
        Trace.log( Trace.DIAGNOSTIC,  "Message Queues Used: " + qsUsed );

        listInfoData = parms[2].getOutputData();
        receiverData = parms[0].getOutputData();
    }
    catch (PropertyVetoException e)
    {
      // Ignore.
    }

    // Create and return the enumeration.
    lastEnumeration_ = new MessageQueueEnumeration (as400_, parser,
        listInfoData, receiverData);
    return lastEnumeration_;
  }

/**
 * Returns the fully qualified integrated file system path name of the
 * message queue.
 *
 * @return The fully qualified integrated file system path name of the
 *         message queue, or CURRENT to refer to the user's default
 *         message queue.
**/
  public String getPath()
  {
//  if (this.as400_ == null)
//    {
//      Trace.log(Trace.ERROR, "Attempt to connect before setting system.");
//      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
//    }

    if (this.ifsName == null)
    {
      return this.CURRENT;
    }

    return this.ifsName;
  }

/**
 * Handle special value for queue name *CURRENT
 *
 * @param path the integrated file system path for the queue
 *
 * @return The queue name
**/
  private String getQueueFullPath(String path)
      throws AS400SecurityException,
             IOException
  {
    String q = path;
    if (q.equals(CURRENT))
    {
      q=this.as400_.getUserId();
      if (q==null  || q.equals(""))
      {
        this.as400_.connectService( AS400.COMMAND );
        q=this.as400_.getUserId();
      }
      return "/QSYS.LIB/" + "QUSRSYS.LIB/" + q + ".MSGQ";
    }
    return path;
  }

/**
 * Handle special value for queue name
 *
 * @param path the integrated file system path for the queue
 *
 * @return The queue name
**/
  private String getQueueLibrary(String path)
      throws AS400SecurityException,
             IOException
  {
    QSYSObjectPathName ifs = new QSYSObjectPathName(getQueueFullPath(path), "MSGQ");

    return ifs.getLibraryName();
  }

/**
 * Handle special value for queue name
 *
 * @param path the integrated file system path for the queue
 *
 * @return The queue name
**/
  private String getQueueName(String path)
      throws AS400SecurityException,
             IOException
  {
    QSYSObjectPathName ifs = new QSYSObjectPathName(getQueueFullPath(path), "MSGQ");

    return ifs.getObjectName();
  }

  private AS400Structure getReceiverStruct()
  {
    if (receiverStruct_ == null)
    {
      // $$$ read receiver
      receiverStruct_ = new AS400Structure(
        new AS400DataType[]
        {
          intType,    // bytes ret                                                           0   BIN(4)
          intType,    // bytes available                                                     1   BIN(4)
          intType,    // sev                                                                 2   BIN(4)
          new AS400Text( 7, this.as400_.getCcsid(), this.as400_),  // id                     3   CHAR(7)
          new AS400Text( 2, this.as400_.getCcsid(), this.as400_),  // type                   4   CHAR(2)
          new AS400ByteArray (4),                                  // key                    5   CHAR(4)
          new AS400Text(10, this.as400_.getCcsid(), this.as400_), // file                    6   CHAR(20 first  half)
          new AS400Text(10, this.as400_.getCcsid(), this.as400_), // file lib                7   CHAR(20 second half)
          new AS400Text(10, this.as400_.getCcsid(), this.as400_), // file lib used           8   CHAR(10)
          new AS400Text(10, this.as400_.getCcsid(), this.as400_), // sending job             9   CHAR(72 10 used)
          new AS400Text(10, this.as400_.getCcsid(), this.as400_), // sending user           10   CHAR(72 20 used)
          new AS400Text( 6, this.as400_.getCcsid(), this.as400_),  // sending job number    11   CHAR(72 26 used)
          new AS400Text(12, this.as400_.getCcsid(), this.as400_), // sending prog name      12   CHAR(72 38 used)
          new NullType(21),  //This used to say null4 ??// reserved                         13   CHAR(72 59 used)
          new AS400Text( 7, this.as400_.getCcsid(), this.as400_),  // date                  14   CHAR(72 66 used)
          new AS400Text( 6, this.as400_.getCcsid(), this.as400_),  // time                  15   CHAR(72 72 used)
//          new NullType(17), // reserved
          intType,    // ccsid text                                                         16   BIN(4)
          intType,    // ccsid data                                                         17   BIN(4)
          new AS400Text( 9, this.as400_.getCcsid(), this.as400_),  // alert                 18   CHAR(9)
          intType,    // ccsid message                                                      19   BIN(4)
          intType,    // ccsid repl data                                                    20   BIN(4)
          intType,    // len of repl data ret                                               21   BIN(4)
          intType,    //    avail                                                           22   BIN(4)
          intType,    // len of message ret                                                 23   BIN(4)
          intType,    //    avail                                                           24   BIN(4)
          intType,    // len of msg help ret                                                25   BIN(4)
          intType     //    avail                                                           26   BIN(4)
        }
      );
    }
    return receiverStruct_;
  }

/**
 * Returns the selection that describes which messages are returned.
 *
 * @return The selection.
**/
  public String getSelection()
  {
    return this.selection_;
  }

/**
 * Returns the severity of the messages to be returned.
 *
 * @return The severity of the messages to be returned.
**/
  public int getSeverity()
  {
    return this.severity_;
  }

/**
 * Returns the AS/400 system on which the message queue exists.
 *
 * @return The AS/400 system on which the message queue exists.
**/
  public AS400 getSystem()
  {
    return this.as400_;
  }

  // ***
  static final int intFor( Object i )
  {
    return ((Integer)i).intValue();
  }


  // Deserializes and initializes transient data.
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();

    this.changes = new PropertyChangeSupport(this);
    this.vetos = new VetoableChangeSupport(this);
  }

/**
 * Receives a message from the message queue by key.
 * The message is removed from the message queue.
 *
 * <blockquote>
 * Note: Receive is the only method that fills in most of the QueuedMessage
 * fields.
 * <p>
 * Use <i>getMessages ()</i> to return a message enumeration.
 * The <i>key</i> parameter required by the receive method is returned from the AS/400 by
 * <i>QueuedMessage.getKey ()</i>.
 * </blockquote>
 *
 * @param key The message key.
 *
 * @return The message.
 *
 * @exception AS400Exception                  If the AS/400 system returns an error message.
 * @exception AS400SecurityException          If a security or authority error occurs.
 * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
 * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
 * @exception InterruptedException            If this thread is interrupted.
 * @exception IOException                     If an error occurs while communicating with the AS/400.
 * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
 * @exception ServerStartupException          If the AS/400 server cannot be started.
 * @exception UnknownHostException            If the AS/400 system cannot be located.
 *
 * @see #getMessages
 * @see QueuedMessage#getKey
 * @see QueuedMessage
**/
  public QueuedMessage receive( byte[] key )
      throws AS400Exception, AS400SecurityException, IOException, ObjectDoesNotExistException,
             ErrorCompletingRequestException, InterruptedException
  {
    return receive( key, 0, REMOVE, ANY);
  }

/**
 * Receives a message from the message queue.
 *
 * <blockquote>
 * Note: Receive is the only method that fills in most of the QueuedMessage
 * fields.
 * <p>
 * Use <i>getMessages ()</i> to return a message enumeration.
 * The <i>key</i> parameter required by the receive method is returned from the AS/400 by
 * <i>QueuedMessage.getKey ()</i>.
 * </blockquote>
 *
 * @param key    The message key, or null if no message key is needed.
 *               This may be required, optional, or disallowed depending
 *               on the specified type.
 * @param wait   The amount of time to wait for the operation to complete.
 * @param action The action to take after the message is received.
 *               Valid values are:
 *               <ul>
 *                 <li> OLD -
 *                      Keep the message in the message queue and mark it
 *                      as an old message.  You can receive the message
 *                      again only by using the message key or by
 *                      specifying the message type NEXT, PREVIOUS,
 *                      FIRST, or LAST.
 *                 <li> REMOVE -
 *                      Remove the message from the message queue.  The
 *                      message key is no longer valid, so you cannot
 *                      receive the message again.
 *                 <li> SAME -
 *                      Keep the message in the message queue without
 *                      changing its new or old designation. SAME lets
 *                      you receive the message again later
 *                      without using the message key.
 *               </ul>
 * @param type The type of message to return.  Valid values are:
 *              <ul>
 *                <li>ANY -
 *                    Receives a message of any type except sender's copy.
 *                    The message key is optional.
 *                <li>COMPLETION -
 *                    Receives a completion message.  The message key is
 *                    optional.
 *                <li>COPY -
 *                    Receives the sender's copy of a previously sent
 *                    inquiry message.  The message key is required.
 *                <li>DIAGNOSTIC -
 *                    Receives a diagnostic message.  The message key is
 *                    optional.
 *                <li>FIRST -
 *                    Receives the first new or old message in the queue.
 *                    The message key is disallowed.
 *                <li>INFORMATIONAL -
 *                    Receives an informational message.  The message key is
 *                    optional.
 *                <li>INQUIRY -
 *                    Receives an inquiry message.  If the action is
 *                    REMOVE and a reply to the inquiry message has not
 *                    been sent yet, the default reply is automatically
 *                    sent when the inquiry message is received.
 *                    The message key is optional.
 *                <li>LAST -
 *                    Receives the last new or old message in the queue.
 *                    The message key is disallowed.
 *                <li>NEXT -
 *                    Receives the next new or old message after the
 *                    message with the specified key.  You can use the
 *                    special value TOP for the message key.  TOP designates
 *                    the message at the top of the message queue.
 *                    The message key is required.
 *                <li>PREVIOUS -
 *                    Receives the new or old message before the message
 *                    with the specified key.  The message key is required.
 *                <li>REPLY -
 *                    Receives the reply to an inquiry message.  For the
 *                    message key, you can use the key to the sender's copy
 *                    of the inquiry or notify message.  The message key is
 *                    optional.
 *              </ul>
 *
 * @return The message.
 *
 * @exception AS400Exception                  If the AS/400 system returns an error message.
 * @exception AS400SecurityException          If a security or authority error occurs.
 * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
 * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
 * @exception InterruptedException            If this thread is interrupted.
 * @exception IOException                     If an error occurs while communicating with the AS/400.
 * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
 * @exception ServerStartupException          If the AS/400 server cannot be started.
 * @exception UnknownHostException            If the AS/400 system cannot be located.
 *
 * @see #getMessages
 * @see QueuedMessage#getKey
 * @see QueuedMessage
**/
  public QueuedMessage receive( byte[] key, int wait, String action, String type )
      throws AS400Exception, AS400SecurityException, IOException, ObjectDoesNotExistException,
             ErrorCompletingRequestException, InterruptedException
  {
    if (action == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'action' is null.");
      throw new NullPointerException("action");
    }

    if (type == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'type' is null.");
      throw new NullPointerException("type");
    }

    if (this.as400_ == null)
    {
      Trace.log(Trace.ERROR, "Attempt to connect before setting system.");
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (this.ifsName == null)
    {
      Trace.log(Trace.ERROR, "Attempt to connect before setting path.");
      throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (!(action.equals (this.OLD))
    &&  !(action.equals (this.REMOVE))
    &&  !(action.equals (this.SAME)))
    {
      throw new ExtendedIllegalArgumentException ("argument(" + action + ")",
                  ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    if (!(type.equals (this.ANY))
    &&  !(type.equals (this.COMPLETION))
    &&    !(type.equals (this.COPY))         // @C1
    &&  !(type.equals (this.DIAGNOSTIC))
    &&  !(type.equals (this.FIRST))
    &&  !(type.equals (this.INFORMATIONAL))
    &&  !(type.equals (this.INQUIRY))
    &&  !(type.equals (this.LAST))
    &&  !(type.equals (this.NEXT))
    &&  !(type.equals (this.PREVIOUS))
    &&    !(type.equals (this.REPLY)))       // @C1
    {
      throw new ExtendedIllegalArgumentException ("argument(" + type + ")",
                  ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Make sure that a key is specified when required.
    if ((key == null)
        && ((type.equals (COPY))
            || (type.equals (NEXT))
            || (type.equals (PREVIOUS))))
    {
      throw new ExtendedIllegalArgumentException ("argument(" + key + ")",
                  ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Make sure that a key is not specified when disallowed.
    //if ((key != null)
    //    && ((type.equals (FIRST))
    //        || (type.equals (LAST))))
    //{
    //  throw new ExtendedIllegalArgumentException ("argument(" + key + ")",
    //              ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    //}

    // Allow null if key for message type is disallowed or optional.
    // blank key is sent to API.                            @C1
    if ((key == null)
        && (type.equals(FIRST)
            || type.equals(LAST)
            || type.equals(ANY)
            || type.equals(COMPLETION)
            || type.equals(DIAGNOSTIC)
            || type.equals(INFORMATIONAL)
            || type.equals(INQUIRY)
            || type.equals(REPLY) ))
    {
       key = new AS400Text (4, this.as400_.getCcsid(), this.as400_).toBytes("    ");
    }

    ProgramParameter[] parms = new ProgramParameter[10];
    // 1 receiver variable
    parms[0] = new ProgramParameter( 5120 );

    // 2 receiver len
    byte[] msgsize = intType.toBytes(new Integer(5120) );
    parms[1] = new ProgramParameter( msgsize );

    // 3 Format name
    parms[2] = new ProgramParameter( new AS400Text (8, this.as400_.getCcsid(), this.as400_).toBytes("RCVM0200") );

    // 4 Qualified message queue name
    byte[] qname = new byte[20];
    new AS400Text (10, this.as400_.getCcsid(), this.as400_).toBytes( getQueueName(this.ifsName), qname, 0 );
    new AS400Text (10, this.as400_.getCcsid(), this.as400_).toBytes( getQueueLibrary(this.ifsName), qname, 10 );
    parms[3] = new ProgramParameter( qname );

    // 5 Messages type
    parms[4] = new ProgramParameter( new AS400Text (10, this.as400_.getCcsid(), this.as400_).toBytes( type ) );

    // 6 Message key (or blanks if not by key)
    parms[5] = new ProgramParameter( key );

    // 7 Wait time
    parms[6] = new ProgramParameter(intType.toBytes(wait));

    // 8 Message action
    parms[7] = new ProgramParameter( new AS400Text (10, this.as400_.getCcsid(), this.as400_).toBytes( action ) );

    // 9 Error code
    parms[8] = new ProgramParameter( new byte[4] );

    //10 CCSID
    byte[] Ccsid = intType.toBytes(this.as400_.getCcsid());

    parms[9] = new ProgramParameter(Ccsid);

    ProgramCall pgm = new ProgramCall( this.as400_ );
    try
    {
      if (pgm.run( "/QSYS.LIB/QMHRCVM.PGM", parms )==false)
      {
        throw new AS400Exception( pgm.getMessageList() );
      }
    }
    catch (PropertyVetoException e)
    {
        // Ignore.
    }

    AS400Structure struct = getReceiverStruct();

    byte[] retData = parms[0].getOutputData();
    Object[] objs = (Object[])struct.toObject( retData );

    QueuedMessage msg = new QueuedMessage();

    if (DEBUG)
    {
      System.out.println ("Bytes returned:  " + intFor (objs[0]));
      System.out.println ("Bytes available: " + intFor (objs[1]));
      System.out.println ("Severity:        " + intFor (objs[2]));
    }

    // Check for the scenario where the message was not found and no error is returned.      @C1
    int NOTFOUND_BYTESRETURNED = 8;
    int NOTFOUND_BYTESAVAILABLE = 0;
    if (intFor(objs[0]) == NOTFOUND_BYTESRETURNED && intFor(objs[1]) == NOTFOUND_BYTESAVAILABLE)
    {
       msg = null;
       return msg;
    }

    boolean impromptu = false;
    msg.setSeverity( intFor(objs[2] ) );
    String messageID = new String(((String)objs[3]).trim());
    if (messageID.length() > 0)
    {
      msg.setID( messageID );
    } else
    {
      impromptu = true;
    }
    msg.setType( Integer.parseInt((String)objs[4]) );
    if (objs[5].equals("    "))
    {
      msg.setKey( key );
    } else
    try
    {
      msg.setKey( (byte[]) objs[5] );
    }
    catch (Exception ex) {}
    msg.setQueue (this);
    msg.setFileName( ((String)objs[6]).trim() );
    msg.setLibraryName( ((String)objs[7]).trim() );
    // objs[8] tells us if this is an alert message (not currently used)
    msg.setFromJobName( ((String)objs[9]).trim() );
    msg.setUser( ((String)objs[10]).trim() );
    msg.setFromJobNumber( ((String)objs[11]).trim() );
    msg.setFromProgram( ((String)objs[12]).trim() );
    // objs[13] is a reserved field
    msg.setDate( (String)objs[14], (String)objs[15] );

    // base pos for variable length strings
    int basepos = 176;

    // Message substitution data
    int sublen = intFor(objs[22] ); // using "available count"
    if (sublen > 0)
    {
      byte[] subData = new byte[sublen];
      System.arraycopy( retData, basepos, subData, 0, sublen );
      msg.setSubstitutionData (subData);
      if (impromptu == true)
      {
        AS400DataType msgTextType = new AS400Text( sublen, this.as400_.getCcsid(), this.as400_ );
        String theText = ((String)msgTextType.toObject(subData)).trim();
        if (theText.length() > 0)
        {
          msg.setText(theText);
        }
        return msg;

        //
        // NOTE:  Because we have an impromptu message, the following fields (text and help) are
        //        not used for this message.  WE ARE TAKING AN EARLY EXIT HERE.
        //

      }
    }

    // Message data or text
    int msglen = intFor(objs[24] ); // using "available count"
    if (msglen > 0)
    {
      byte[] messageData = new byte[msglen];
      System.arraycopy( retData, basepos+sublen, messageData, 0, msglen );
      AS400DataType msgTextType = new AS400Text( msglen, this.as400_.getCcsid(), this.as400_ );
      String theText = ((String)msgTextType.toObject(messageData)).trim();
      if (theText.length() > 0)
      {
        msg.setText (theText);
      }
    }

    // Message help text
    int helplen = intFor( objs[26] ); // using "available count" in case it got trimmed
    if (helplen > 0)
    {
      byte[] helpData = new byte[helplen + 1];
      System.arraycopy( retData, basepos+sublen+msglen, helpData, 0, helplen);
      AS400DataType helpTextType = new AS400Text (helplen, this.as400_.getCcsid(), this.as400_ );
      String theHelp = ((String)helpTextType.toObject(helpData)).trim();
      if (theHelp.length() > 0)
      {
        msg.setHelp (theHelp);
      }
    }

    return msg;
  }

/**
 * Remove all messages from the message queue.
 *
 * @exception AS400Exception                  If the AS/400 system returns an error message.
 * @exception AS400SecurityException          If a security or authority error occurs.
 * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
 * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
 * @exception InterruptedException            If this thread is interrupted.
 * @exception IOException                     If an error occurs while communicating with the AS/400.
 * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
 * @exception ServerStartupException          If the AS/400 server cannot be started.
 * @exception UnknownHostException            If the AS/400 system cannot be located.
**/
  public void remove()
      throws AS400Exception, AS400SecurityException, IOException, ObjectDoesNotExistException,
             ErrorCompletingRequestException, InterruptedException
  {
    remove( new AS400Text ( 4, this.as400_.getCcsid(), this.as400_).toBytes(""), this.ALL);
  }

/**
 * Removes a message from the message queue by key.
 *
 * @param key The message key.
 *
 * @exception AS400Exception                  If the AS/400 system returns an error message.
 * @exception AS400SecurityException          If a security or authority error occurs.
 * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
 * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
 * @exception InterruptedException            If this thread is interrupted.
 * @exception IOException                     If an error occurs while communicating with the AS/400.
 * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
 * @exception ServerStartupException          If the AS/400 server cannot be started.
 * @exception UnknownHostException            If the AS/400 system cannot be located.
**/
  public void remove( byte[] key )
      throws AS400Exception, AS400SecurityException, IOException, ObjectDoesNotExistException,
             ErrorCompletingRequestException, InterruptedException
  {
    remove( key, BYKEY );
  }

/**
 * Remove messages from the message queue by type.
 *
 * @param type The type of message to remove.  Valid values are:
 *              <ul>
 *                <li>ALL -
 *                    All messages in the message queue.
 *                <li>KEEP_UNANSWERED -
 *                    All messages in the message queue except unanswered
 *                    inquiry and unanswered senders' copy messages.
 *                <li>NEW -
 *                    All new messages in the message queue.  New messages
 *                    are those that have not been received.
 *                <li>OLD -
 *                    All old messages in the message queue.  Old messages
 *                    are those that have already been received.
 *              </ul>
 *
 * @exception AS400Exception                  If the AS/400 system returns an error message.
 * @exception AS400SecurityException          If a security or authority error occurs.
 * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
 * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
 * @exception InterruptedException            If this thread is interrupted.
 * @exception IOException                     If an error occurs while communicating with the AS/400.
 * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
 * @exception ServerStartupException          If the AS/400 server cannot be started.
 * @exception UnknownHostException            If the AS/400 system cannot be located.
**/
  public void remove( String type )
      throws AS400Exception, AS400SecurityException, IOException, ObjectDoesNotExistException,
             ErrorCompletingRequestException, InterruptedException
  {
    remove( new AS400Text ( 4, this.as400_.getCcsid(), this.as400_).toBytes(""), type );
  }

/**
 * Remove messages from the message queue by key and type.
 *
 * @param key  The message key.
 * @param type The type of message to remove.  Valid values are:
 *              <ul>
 *                <li>ALL -
 *                    All messages in the message queue.
 *                <li>BYKEY -
 *                    The single message specified by the message key.
 *                <li>KEEP_UNANSWERED -
 *                    All messages in the message queue except unanswered
 *                    inquiry and unanswered senders' copy messages.
 *                <li>NEW -
 *                    All new messages in the message queue.  New messages
 *                    are those that have not been received.
 *                <li>OLD -
 *                    All old messages in the message queue.  Old messages
 *                    are those that have already been received.
 *              </ul>
 *
 * @exception AS400Exception                  If the AS/400 system returns an error message.
 * @exception AS400SecurityException          If a security or authority error occurs.
 * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
 * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
 * @exception InterruptedException            If this thread is interrupted.
 * @exception IOException                     If an error occurs while communicating with the AS/400.
 * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
 * @exception ServerStartupException          If the AS/400 server cannot be started.
 * @exception UnknownHostException            If the AS/400 system cannot be located.
**/
  void remove( byte[] key, String type )
      throws AS400Exception, AS400SecurityException, IOException, ObjectDoesNotExistException,
             ErrorCompletingRequestException, InterruptedException
  {
    if (key == null)                                // @D1a
      throw new NullPointerException("key");        // @D1a

    if (type == null)                               // @D1a
      throw new NullPointerException("type");       // @D1a

    if (this.as400_ == null)
    {
      Trace.log(Trace.ERROR, "Attempt to connect before setting system.");
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (this.ifsName == null)
    {
      Trace.log(Trace.ERROR, "Attempt to connect before setting path.");
      throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (!(type.equals (this.ALL))
    &&  !(type.equals (this.BYKEY))
    &&  !(type.equals (this.KEEP_UNANSWERED))
    &&  !(type.equals (this.NEW))
    &&  !(type.equals (OLD)))
    {
      throw new ExtendedIllegalArgumentException ("argument(" + type + ")",
                  ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    ProgramParameter[] parms = new ProgramParameter[4];
    // Qualified message queue name
    byte[] qname = new byte[20];
    new AS400Text (10, this.as400_.getCcsid(), this.as400_).toBytes( getQueueName(this.ifsName), qname, 0 );
    new AS400Text (10, this.as400_.getCcsid(), this.as400_).toBytes( getQueueLibrary(this.ifsName), qname, 10 );
    parms[0] = new ProgramParameter( qname );
    // Message key (or blanks if not by key)
    parms[1] = new ProgramParameter(key);
    // Messages to remove
    parms[2] = new ProgramParameter(new AS400Text (10, this.as400_.getCcsid(), this.as400_).toBytes( type ) );
    parms[3] = new ProgramParameter( new byte[4] );

    ProgramCall pgm = new ProgramCall( this.as400_ );
    try
    {
      if (pgm.run( "/QSYS.LIB/QMHRMVM.PGM", parms )==false)
      {
        throw new AS400Exception( pgm.getMessageList() );
      }
    }
    catch (PropertyVetoException e)
    {
        // Ignore.
    }
  }

/**
 * Removes a property change listener from the listener list.
 * @param listener The PropertyChangeListener.
**/
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
      throw new NullPointerException("listener");
    }
    this.changes.removePropertyChangeListener(listener);
  }

/**
 * Removes a vetoable change listener from the listener list.
 * @param listener The VetoableChangeListener.
**/
  public void removeVetoableChangeListener(VetoableChangeListener listener)
  {
    if (listener == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
      throw new NullPointerException("listener");
    }
    this.vetos.removeVetoableChangeListener(listener);
  }

/**
 * Replies to a message.
 *
 * @param key   The message key.
 * @param reply The reply text.
 *
 * @exception AS400Exception                  If the AS/400 system returns an error message.
 * @exception AS400SecurityException          If a security or authority error occurs.
 * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
 * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
 * @exception InterruptedException            If this thread is interrupted.
 * @exception IOException                     If an error occurs while communicating with the AS/400.
 * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
 * @exception ServerStartupException          If the AS/400 server cannot be started.
 * @exception UnknownHostException            If the AS/400 system cannot be located.
**/
  public void reply( byte[] key, String reply)
      throws AS400Exception, AS400SecurityException, IOException, ObjectDoesNotExistException,
             ErrorCompletingRequestException, InterruptedException
  {
    if (key == null)                                             // D1a
    {                                                            // D1a
      Trace.log(Trace.ERROR, "Parameter 'key' is null.");        // D1a
      throw new NullPointerException("key");                     // D1a
    }                                                            // D1a

    if (reply == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'reply' is null.");
      throw new NullPointerException("reply");
    }

    if (this.as400_ == null)
    {
      Trace.log(Trace.ERROR, "Attempt to connect before setting system.");
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (this.ifsName == null)
    {
      Trace.log(Trace.ERROR, "Attempt to connect before setting path.");
      throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    ProgramParameter[] parms = new ProgramParameter[6];

    // Message key
    parms[0] = new ProgramParameter( key );

    // Qualified message queue name
    byte[] qname = new byte[20];
    new AS400Text (10, this.as400_.getCcsid(), this.as400_).toBytes( getQueueName(this.ifsName), qname, 0 );
    new AS400Text (10, this.as400_.getCcsid(), this.as400_).toBytes( getQueueLibrary(this.ifsName), qname, 10 );
    parms[1] = new ProgramParameter( qname );

    setConverter();
    byte[] byteData = converter_.stringToByteArray(reply);

    // Reply text
    parms[2] = new ProgramParameter( byteData );

    // Length of reply text
    parms[3] = new ProgramParameter( intType.toBytes( new Integer(byteData.length) ) );

    // Remove message
    parms[4] = new ProgramParameter( new AS400Text (10, this.as400_.getCcsid(), this.as400_).toBytes( "*YES" ) ); // $$$ Optionally no?
    parms[5] = new ProgramParameter( new byte[4] );

    ProgramCall pgm = new ProgramCall( this.as400_ );
    try
    {
      if (pgm.run( "/QSYS.LIB/QMHSNDRM.PGM", parms )==false)
      {
        throw new AS400Exception( pgm.getMessageList() );
      }
    }
    catch (PropertyVetoException e)
    {
        // Ignore.
    }
  }

/**
 * Sends a message to a message queue.
 *
 * @param id   The message id.
 * @param file The integrated file system path name of the message file.
 * @param substitutionData The substition data for the message, or null
 *             if none.
 * @param type The message type.  This must be one of the following:
 *             <ul>
 *                <li>INQUIRY
 *                <li>INFORMATIONAL
 *             </ul>
 * @param replyqueue The integrated file system path of the reply queue on
 *        the AS/400 system.
 *
 * @exception AS400Exception                  If the AS/400 system returns an error message.
 * @exception ExtendedIllegalArgumentException If the argument passed is not valid.
 * @exception AS400SecurityException          If a security or authority error occurs.
 * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
 * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
 * @exception InterruptedException            If this thread is interrupted.
 * @exception IOException                     If an error occurs while communicating with the AS/400.
 * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
 * @exception ServerStartupException          If the AS/400 server cannot be started.
 * @exception UnknownHostException            If the AS/400 system cannot be located.
**/
  private void send( String id,
                    String file,
                    byte[] substitutionData,
                    String type,
                    String replyqueue
                         )
       throws AS400Exception, AS400SecurityException, IOException, ObjectDoesNotExistException,
             ErrorCompletingRequestException, InterruptedException
  {
    if (this.as400_ == null)
    {
      Trace.log(Trace.ERROR, "Attempt to connect before setting system.");
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (this.ifsName == null)
    {
      Trace.log(Trace.ERROR, "Attempt to connect before setting path.");
      throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (!(type.equals (INFORMATIONAL))
    &&  !(type.equals (INQUIRY)))
    {
      throw new ExtendedIllegalArgumentException ("argument(" + type + ")",
                  ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    QSYSObjectPathName ifs = null;
    String replyQueueLib   = "";
    String replyQueueName  = "";

    if ((replyqueue != null)
    &&  (replyqueue.length() > 0))
    {
      ifs = new QSYSObjectPathName(replyqueue, "MSGQ");

      // set instance vars
      replyQueueLib  = ifs.getLibraryName();
      replyQueueName = ifs.getObjectName();
    } /* if */

    String localFile    = "";
    String localFileLib = "";
    if ((file != null)
    &&  (file.length() > 0))
    {
      ifs = new QSYSObjectPathName(file, "MSGF");

      localFile = ifs.getObjectName();
      localFileLib = ifs.getLibraryName();
    }

    ProgramParameter[] parms = new ProgramParameter[10];

    // Message identifier
    parms[0] = new ProgramParameter( new AS400Text ( 7, this.as400_.getCcsid(), this.as400_).toBytes( id ) );

    // Qualified message file name
    byte[] fname = new byte[20];
    new AS400Text (10, this.as400_.getCcsid(), this.as400_).toBytes( localFile, fname, 0 );
    new AS400Text (10, this.as400_.getCcsid(), this.as400_).toBytes( localFileLib, fname, 10 );

    parms[1] = new ProgramParameter( fname );

    // Message data or immediate text
    parms[2] = new ProgramParameter( substitutionData );

    // Length of message data or immediate text
    parms[3] = new ProgramParameter( intType.toBytes( new Integer(substitutionData.length) ) );

    // Message type
    parms[4] = new ProgramParameter( new AS400Text (10, this.as400_.getCcsid(), this.as400_).toBytes( type ) );

    // List of qualified message queue names
    byte[] qname = new byte[20];
    new AS400Text (10, this.as400_.getCcsid(), this.as400_).toBytes( getQueueName(this.ifsName), qname, 0 );
    new AS400Text (10, this.as400_.getCcsid(), this.as400_).toBytes( getQueueLibrary(this.ifsName), qname, 10 );
    parms[5] = new ProgramParameter( qname );
    parms[6] = new ProgramParameter( intType.toBytes( new Integer(1) ) );

    // Qualified name of the reply message queue
    byte[] rqname = new byte[20];
    new AS400Text (10, this.as400_.getCcsid(), this.as400_).toBytes( replyQueueName, rqname, 0 );
    new AS400Text (10, this.as400_.getCcsid(), this.as400_).toBytes( replyQueueLib, rqname, 10 );
    parms[7] = new ProgramParameter( rqname );

    // key
    parms[8] = new ProgramParameter(4);

    // errors
    parms[9] = new ProgramParameter( new byte[4] );

    ProgramCall pgm = new ProgramCall( this.as400_ );
    try
    {
      if (pgm.run( "/QSYS.LIB/QMHSNDM.PGM", parms )==false)
      {
        throw new AS400Exception( pgm.getMessageList() );
      }
    }
    catch (PropertyVetoException e)
    {
        // Ignore.
    }
  }


/**
 * Sends an informational message to the message queue.
 *
 * @param id  The message id.
 * @param file The integrated file system path of the message file.
 *
 * @exception AS400Exception                  If the AS/400 system returns an error message.
 * @exception AS400SecurityException          If a security or authority error occurs.
 * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
 * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
 * @exception InterruptedException            If this thread is interrupted.
 * @exception IOException                     If an error occurs while communicating with the AS/400.
 * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
 * @exception ServerStartupException          If the AS/400 server cannot be started.
 * @exception UnknownHostException            If the AS/400 system cannot be located.
**/
  public void sendInformational( String id, String file)
      throws AS400Exception, AS400SecurityException, IOException, ObjectDoesNotExistException,
             ErrorCompletingRequestException, InterruptedException
  {
    if (id == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'id' is null.");
      throw new NullPointerException("id");
    }

    if (file == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'file' is null.");
      throw new NullPointerException("file");
    }

    String msg = "";
    AS400Text textType = new AS400Text( msg.length(), this.as400_.getCcsid(), this.as400_ );
    send( id,
          file,
          textType.toBytes(msg),
          INFORMATIONAL,
          ""
        );
  }

/**
 * Sends an informational message to the message queue.
 *
 * @param id   The message id.
 * @param file The integrated file system path of the message file.
 * @param substitutionData The substition data.
 *
 * @exception AS400Exception                  If the AS/400 system returns an error message.
 * @exception AS400SecurityException          If a security or authority error occurs.
 * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
 * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
 * @exception InterruptedException            If this thread is interrupted.
 * @exception IOException                     If an error occurs while communicating with the AS/400.
 * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
 * @exception ServerStartupException          If the AS/400 server cannot be started.
 * @exception UnknownHostException            If the AS/400 system cannot be located.
**/
  public void sendInformational( String id, String file, byte[] substitutionData)
      throws AS400Exception, AS400SecurityException, IOException, ObjectDoesNotExistException,
             ErrorCompletingRequestException, InterruptedException
  {
    if (id == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'id' is null.");
      throw new NullPointerException("id");
    }

    if (file == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'file' is null.");
      throw new NullPointerException("file");
    }

    if (substitutionData == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'substitutionData' is null.");
      throw new NullPointerException("substitutionData");
    }

    send( id,
          file,
          substitutionData,
          INFORMATIONAL,
          ""
        );
  }

/**
 * Sends an informational message to the message queue.
 *
 * @param text The message text.
 *
 * @exception AS400Exception                  If the AS/400 system returns an error message.
 * @exception AS400SecurityException          If a security or authority error occurs.
 * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
 * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
 * @exception InterruptedException            If this thread is interrupted.
 * @exception IOException                     If an error occurs while communicating with the AS/400.
 * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
 * @exception ServerStartupException          If the AS/400 server cannot be started.
 * @exception UnknownHostException            If the AS/400 system cannot be located.
**/
  public void sendInformational( String text)
      throws AS400Exception, AS400SecurityException, IOException, ObjectDoesNotExistException,
             ErrorCompletingRequestException, InterruptedException
  {
    if (text == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'text' is null.");
      throw new NullPointerException("text");
    }

    setConverter();
    byte[] byteData = converter_.stringToByteArray(text);
    send( "",
          "",
          byteData,
          INFORMATIONAL,
          ""
        );
  }

/**
 * Sends an inquiry message to the message queue.
 *
 * @param id  The message id.
 * @param file The integrated file system path of the message file.
 * @param replyQueue The integrated file system path of the reply queue.
 *
 * @exception AS400Exception                  If the AS/400 system returns an error message.
 * @exception AS400SecurityException          If a security or authority error occurs.
 * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
 * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
 * @exception InterruptedException            If this thread is interrupted.
 * @exception IOException                     If an error occurs while communicating with the AS/400.
 * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
 * @exception ServerStartupException          If the AS/400 server cannot be started.
 * @exception UnknownHostException            If the AS/400 system cannot be located.
**/
  public void sendInquiry( String id, String file, String replyQueue )
      throws AS400Exception, AS400SecurityException, IOException, ObjectDoesNotExistException,
             ErrorCompletingRequestException, InterruptedException
  {
    if (id == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'id' is null.");
      throw new NullPointerException("id");
    }

    if (file == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'file' is null.");
      throw new NullPointerException("file");
    }

    if (replyQueue == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'replyQueue' is null.");
      throw new NullPointerException("replyQueue");
    }

    String msg = "";
    AS400Text textType = new AS400Text( msg.length(), this.as400_.getCcsid(), this.as400_ );
    send( id,
          file,
          textType.toBytes(msg),
          INQUIRY,
          replyQueue
        );
  }

/**
 * Sends an inquiry message to the message queue.
 *
 * @param id  The message id.
 * @param file The integrated file system path of the message file.
 * @param substitutionData The substition data.
 * @param replyQueue The integrated file system path of the reply queue.
 *
 * @exception AS400Exception                  If the AS/400 system returns an error message.
 * @exception AS400SecurityException          If a security or authority error occurs.
 * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
 * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
 * @exception InterruptedException            If this thread is interrupted.
 * @exception IOException                     If an error occurs while communicating with the AS/400.
 * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
 * @exception ServerStartupException          If the AS/400 server cannot be started.
 * @exception UnknownHostException            If the AS/400 system cannot be located.
**/
  public void sendInquiry( String id, String file, byte[] substitutionData, String replyQueue )
      throws AS400Exception, AS400SecurityException, IOException, ObjectDoesNotExistException,
             ErrorCompletingRequestException, InterruptedException
  {
    if (id == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'id' is null.");
      throw new NullPointerException("id");
    }

    if (file == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'file' is null.");
      throw new NullPointerException("file");
    }

    if (substitutionData == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'substitutionData' is null.");
      throw new NullPointerException("substitutionData");
    }

    if (replyQueue == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'replyQueue' is null.");
      throw new NullPointerException("replyQueue");
    }

    send( id,
          file,
          substitutionData,
          INQUIRY,
          replyQueue
        );
  }

/**
 * Sends an inquiry message to the message queue.
 *
 * @param text The message text.
 * @param replyQueue The integrated file system path of the reply queue.
 *
 * @exception AS400Exception                  If the AS/400 system returns an error message.
 * @exception AS400SecurityException          If a security or authority error occurs.
 * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
 * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
 * @exception InterruptedException            If this thread is interrupted.
 * @exception IOException                     If an error occurs while communicating with the AS/400.
 * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
 * @exception ServerStartupException          If the AS/400 server cannot be started.
 * @exception UnknownHostException            If the AS/400 system cannot be located.
**/
  public void sendInquiry( String msg, String replyQueue )
      throws AS400Exception, AS400SecurityException, IOException, ObjectDoesNotExistException,
             ErrorCompletingRequestException, InterruptedException
  {
    if (msg == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'msg' is null.");
      throw new NullPointerException("msg");
    }

    if (replyQueue == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'replyQueue' is null.");
      throw new NullPointerException("replyQueue");
    }

    setConverter();
    byte[] byteData = converter_.stringToByteArray(msg);
    send( "",
          "",
          byteData,
          INQUIRY,
          replyQueue
        );
  }

/**
 * Set the converter for this object
 *
 * @exception IOException                     If an error occurs while communicating with the AS/400.
**/
  private void setConverter() throws ExtendedIOException
  {
    if (converter_ == null)
    {
      try
      {
//@D2D        converter_ = ConverterImplRemote.getConverter(this.as400_.getCcsid(), (AS400ImplRemote)as400_.getImpl()); // @D0C
        converter_ = new Converter(as400_.getCcsid(), as400_); //@D2A
      }
      catch (UnsupportedEncodingException e)
      {
        throw new ExtendedIOException(ExtendedIOException.CANNOT_CONVERT_VALUE);
      }
    }
  }

/**
 * Sets the fully qualified integrated file system path name of the
 * message queue.  The default is CURRENT.
 *
 * @param path The fully qualified integrated file system path name of the
 *         message queue, or CURRENT to refer to the user's default
 *         message queue.
 *
 * @exception PropertyVetoException If the change is vetoed.
**/
  public void setPath( String path )
      throws PropertyVetoException
  {
    if (path == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'path' is null.");
      throw new NullPointerException("path");
    }

    String old = this.ifsName;
    this.vetos.fireVetoableChange("path", old, path );

    // Verify name is valid IFS path name.
    QSYSObjectPathName ifs = new QSYSObjectPathName(path, "MSGQ");

    // set instance vars
    this.ifsName = path.trim();
    this.changes.firePropertyChange("path", old, path );
  }

/**
 * Sets the selection that describes which messages are returned.
 * The default is ALL. This takes effect the next time that
 * <i>getMessages()</i> is called.
 *
 * @param selection The selection that describes which messages are
 *                  returned.  Valid values are:
 *                  <ul>
 *                      <li>ALL
 *                      <li>MESSAGES_NEED_REPLY
 *                      <li>MESSAGES_NO_NEED_REPLY
 *                      <li>SENDERS_COPY_NEED_REPLY
 *                  </ul>
 *                  This value cannot be null.
 *
 * @exception PropertyVetoException If the change is vetoed.
**/
  public void setSelection( String selection )
      throws PropertyVetoException
  {
    if (selection == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'selection' is null.");
      throw new NullPointerException("selection");
    }

    if (!(selection.equals (ALL))
    &&  !(selection.equals (MESSAGES_NEED_REPLY))
    &&  !(selection.equals (MESSAGES_NO_NEED_REPLY))
    &&  !(selection.equals (SENDERS_COPY_NEED_REPLY)))
    {
      throw new ExtendedIllegalArgumentException ("argument(" + selection + ")",
                  ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    String old = this.selection_;
    this.vetos.fireVetoableChange("selection", old, selection );

    selection_ = selection;
    this.changes.firePropertyChange("selection", old, selection );
  }

/**
 * Sets the severity of the messages to be returned.  All messages
 * of the specified severity and greater are returned.  The default is 0.
 * This takes effect the next time that <i>getMessages()</i> is called.
 *
 * @param severity The severity of the messages to be returned.
 *
 * @exception ExtendedIllegalArgumentException If the argument passed is not valid.
 * @exception PropertyVetoException            If the change is vetoed.
**/
  public void setSeverity( int severity )
      throws PropertyVetoException
  {
    if ((severity < 0) || (severity > 99))
    {
      throw new ExtendedIllegalArgumentException ("argument(" + severity + ")",
                  ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    Integer old = new Integer(this.severity_);
    Integer newSeverity = new Integer(severity);
    this.vetos.fireVetoableChange("severity", old, newSeverity );

    this.severity_ = severity;
    this.changes.firePropertyChange("severity", old, newSeverity );
  }

/**
 * Sets the AS/400 system on which the message queue exists.
 *
 * @param  system  The AS/400 system on which the message queue exists.
 *                 This value cannot be null.
 *
 * @exception PropertyVetoException If the change is vetoed.
**/
  public void setSystem( AS400 system )
      throws PropertyVetoException
  {
    if (system == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'system' is null.");
      throw new NullPointerException("system");
    }

    AS400 old = this.as400_;
    this.vetos.fireVetoableChange("system", old, system );

    this.as400_ = system;
    this.changes.firePropertyChange("system", old, system );
  }
}

