///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  MessageException.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents an IOException that includes one or more Messages as part of its exception text.
**/
public class MessageException extends IOException
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 3994984723312909458L;
  private final String text_;
  private Message[] messages_;
  private List<Message> messagesList_;

  /**
   * Constructs a MessageException with the specified preamble and messages.
  **/
  public MessageException(final String preamble, final Message[] messages)
  {
    super(buildString(preamble, messages));
    text_ = preamble;
    messages_ = messages;
    messagesList_ = null; 
  }

  public MessageException(final String preamble, final List<Message> messages)
  {
    super(buildString(preamble, messages));
    text_ = preamble;
    messagesList_ = messages;
    messages_ = null; 
  }

  /**
   * Constructs a MessageException with no preamble.
  **/
  public MessageException(final Message[] messages)
  {
    super(buildString(null, messages));
    text_ = null;
    messages_ = messages;
    messagesList_ = null; 
  }

  public MessageException(final List<Message> messages)
  {
    super(buildString(null, messages));
    text_ = null;
    messages_ = null; 
    messagesList_ = messages;
  }

  private static final String buildString(final String text, final List<Message> messages)
  {
	StringBuffer s = new StringBuffer(); 
	if (text != null) {
		s.append(text);
		s.append("\n"); 
		
	}
	s.append(messages.get(0).toString()); 

    for (int i=1; i<messages.size(); ++i)
    {
      s.append("\n"); 
      s.append(messages.get(i).toString());
    }
    return s.toString();
  }

  private static final String buildString(final String text, final Message[] messages)
  {
       // Use string buffer to improve performance 
	StringBuffer s = new StringBuffer(); 
	if (text == null) { 
		s.append(messages[0].toString());
	} else { 
		s.append(text); 
		s.append("\n"); 
		s.append(messages[0].toString());
	}
    for (int i=1; i<messages.length; ++i)
    {
    s.append("\n"); 
    s.append(messages[i].toString());
    }
    return s.toString();
  }

  /**
   * Returns the preamble, which may be null.
  **/
  public String getPreamble()
  {
    return text_;
  }

  /**
   * Returns the array of Messages.
  **/
  public Message[] getMessages()
  {
	if (messages_ == null) { 
		if (messagesList_ != null) { 
		    messages_ = new Message[messagesList_.size()]; 
		    for (int i = 0 ;i < messages_.length; i++) {
		    	messages_[i] = messagesList_.get(i); 
		    }
		}
	}
    return messages_;
  }
  
  /**
   * Returns the list of Messages.
  **/
  public List<Message> getMessagesList()
  {
	if (messagesList_ == null) {
		if (messages_ != null) { 
			messagesList_ = new LinkedList<Message>(); 
			for (int i = 0; i < messages_.length ; i++) {
				messagesList_.add(messages_[i]); 
			}
		}
	}
    return messagesList_;
  }
  
}

