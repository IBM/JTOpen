///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DataStreamException.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite;

import java.io.*;
import java.util.*;

/**
 * An IOException that represents an error processing a System i Host Server datastream reply.
**/
public class DataStreamException extends IOException
{
  /**
	 * Id for serialization
	 */
	private static final long serialVersionUID = -3804731342532950986L;

/**
   * Constant representing a datastream reply type that has an unsuccessful return code.
  **/
  public static final String BAD_RETURN_CODE = "Bad return code";

  /**
   * Constant representing a datastream reply type that has an unexpected datastream length (LL).
  **/
  public static final String BAD_LENGTH = "Bad length";

  /**
   * Constant representing a datastream reply type that has an unexpected reply.
  **/
  public static final String BAD_REPLY = "Bad reply";

  /**
   * Constant representing a datastream reply type that contains an error message.
  **/
  public static final String ERROR_MESSAGE = "Error message";

  private String type_;
  private String dataStreamName_;
  private int value_;
  private ArrayList<Message> messages_ = new ArrayList<Message>();

  protected DataStreamException(String type, String dataStreamName, int value)
  {
    /* BAD_RETURN_CODE is a string so .equals must be used */ 
    super(type+" on "+dataStreamName+": "+(BAD_RETURN_CODE.equals(type) || type == BAD_REPLY ? ("0x"+Integer.toHexString(value)) : ""+value));
    type_ = type;
    dataStreamName_ = dataStreamName;
    value_ = value;
  }

  private DataStreamException(String dataStreamName, Message message)
  {
    super(ERROR_MESSAGE+" on "+dataStreamName+": "+message);
    type_ = ERROR_MESSAGE;
    dataStreamName_ = dataStreamName;
    value_ = message.getSeverity();
    messages_.add(message);
  }

  /**
   * Returns the datastream reply type.
  **/
  public String getType()
  {
    return type_;
  }

  /**
   * Returns the name of the datastream that caused this exception.
  **/
  public String getDataStreamName()
  {
    return dataStreamName_;
  }

  /**
   * Returns the value associated with this exception, if any.
  **/
  public int getValue()
  {
    return value_;
  }

  /**
   * Returns the primary error message associated with this exception, if any.
  **/
  public Message getErrorMessage()
  {
    return messages_.size() == 0 ? null : messages_.get(0);
  }

  /**
   * Associates a message with this exception.
  **/
  public void addMessage(Message message)
  {
    messages_.add(message);
  }

  /**
   * Returns the array of error messages associated with this exception, if any.
  **/
  public Message[] getErrorMessages()
  {
    Message[] arr = new Message[messages_.size()];
    messages_.toArray(arr);
    return arr;
  }

  /**
   * Factory method for constructing a datastream exception with the provided bad return code value.
  **/
  public static DataStreamException badReturnCode(String dataStreamName, int value)
  {
    return new DataStreamException(BAD_RETURN_CODE, dataStreamName, value);
  }

  /**
   * Factory method for constructing a datastream exception with the provided bad length value.
  **/
  public static DataStreamException badLength(String dataStreamName, int value)
  {
    return new DataStreamException(BAD_LENGTH, dataStreamName, value);
  }

  /**
   * Factory method for constructing a datastream exception with the provided bad reply value.
  **/
  public static DataStreamException badReply(String dataStreamName, int codepoint)
  {
    return new DataStreamException(BAD_REPLY, dataStreamName, codepoint);
  }

  /**
   * Factory method for constructing a datastream exception with the provided error message.
  **/
  public static DataStreamException errorMessage(String dataStreamName, Message message)
  {
    return new DataStreamException(dataStreamName, message);
  }
}
