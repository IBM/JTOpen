///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
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

/**
 * The QueuedMessage class represents a message on a AS/400 message queue.
 *
 * @see MessageQueue
**/
//
// Implementation note:
//
// * The constructor and set methods are not public, since it
//   never makes sense for anyone other than UserList to call
//   these.
//
public class QueuedMessage extends AS400Message
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



  private String user_;
  private String fromProgram_;
  private String fromJobName_;
  private String fromJobNumber_;
  private byte[] key_;
  private MessageQueue msgQueue_;
  private String replyStatus_;




/**
 * Constructs a QueuedMessage object.
**/
//
// This is intentionally not public so that general callers
// cannot create these objects.
//
  QueuedMessage ()
  {
  }


/**
 * Copyright.
**/
  private static String getCopyright ()
  {
    return Copyright.copyright;
  }


/**
 * Returns the program from which the message originated.
 *
 * @return the program from which the message originated.
**/
  public String getFromProgram()
  {
    return fromProgram_;
  }


/**
 * Returns the name of the job from which the the message originated.
 *
 * @return The name of the job from which the the message originated.
**/
  public String getFromJobName()
  {
    return fromJobName_;
  }

/**
 * Returns the number of the job from which the the message originated.
 *
 * @return The number of the job from which the the message originated.
**/
  public String getFromJobNumber()
  {
     return fromJobNumber_;
  }

/**
 * Returns the message key.
 *
 * @return The message key.
**/
  public byte[] getKey()
  {
    return key_;
  }

/**
 * Returns the message queue.
 *
 * @return The message queue.
**/
  public MessageQueue getQueue()
  {
    return msgQueue_;
  }

/**
 * Returns the message reply status.
 *
 * @return The message reply status.
**/
  public String getReplyStatus()
  {
    return replyStatus_;
  }


/**
 * Returns the user of the job from which the message originated.
 *
 * @return The user of the job from which the message originated.
**/
  public String getUser()
  {
    return user_;
  }


/**
 * Sets the FromJobName
 *
 * @param fromJobName The FromJobName
**/
  void setFromJobName( String fromJobName )
  {
    if (fromJobName == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'fromJobName' is null.");
      throw new NullPointerException("fromJobName");
    }

    fromJobName_ = fromJobName;
  }

/**
 * Sets the FromJobNumber
 *
 * @param fromJobNumber The FromJobNumber
**/
  void setFromJobNumber( String fromJobNumber )
  {
    if (fromJobNumber == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'fromJobNumber' is null.");
      throw new NullPointerException("fromJobNumber");
    }

    fromJobNumber_ = fromJobNumber;
  }

/**
 * Sets the FromProgram
 *
 * @param fromProgram The FromProgram
**/
  void setFromProgram( String fromProgram )
  {
    if (fromProgram == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'fromProgram' is null.");
      throw new NullPointerException("fromProgram");
    }

    fromProgram_ = fromProgram;
  }

/**
 * Sets the key
 *
 * @param key The key
**/
  void setKey( byte[] key )
  {
    key_ = key;
  }

/**
 * Sets the queue
 *
 * @param queue The Queue
**/
  void setQueue( MessageQueue queue )
  {
    if (queue == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'queue' is null.");
      throw new NullPointerException("queue");
    }

    msgQueue_ = queue;
  }

/**
 * Sets the Message Reply Status
 *
 * @param replyStatus The reply status.
**/
  void setReplyStatus( String replyStatus )
  {
    if (replyStatus == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'replyStatus' is null.");
      throw new NullPointerException("replyStatus");
    }

    replyStatus_ = replyStatus;
  }

/**
 * Sets user
 *
 * @param user The user
**/
  void setUser( String user )
  {
    if (user == null)
    {
      Trace.log(Trace.ERROR, "Parameter 'user' is null.");
      throw new NullPointerException("user");
    }

    user_ = user;
  }
}
