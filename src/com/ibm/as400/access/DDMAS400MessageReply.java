///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: DDMAS400MessageReply.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

/**
 *Represents the S38MSGRM DDM data stream.  This reply stream is returned
 *when an error has occurred for which there is an AS400 error message(s).
 *Format:
 *  Bytes      Description
 * ----------  -------------------------------------------------------
 * 0-5         Header
 * 6-9         LL-CP for S38MSGRM
 *  The folowing items are optional and may or may not exist.
 *  ?          LL-CP, 16-bit severity code
 *  ?          LL-CP, 7-byte message id
 *  ?          LL-CP, 2-byte msg type
 *  ?          LL-CP, message file name
 *  ?          LL-CP, server diagnostic info
 *  ?          LL-CP, message replacement data
 *  ?          LL-CP, message text
**/
class DDMAS400MessageReply extends DDMReplyDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  ConverterImplRemote conv; //@B5C
  Vector messages_ = new Vector();
  String msgId_ = null;
  String msgFile_ = null;
  String msgReplData_ = null;
  String msgText_ = null;
  int msgType_ = -1;  // Set msgType_ to -1 to indicate that it has not been set
  String srvDiagnostic_ = null;
  int svrCode_ = -1;  // Set svrCode_ to -1 to indicate that it has not been set

  /**
   *Constructs an S38MSGRM reply message data stream.
   *@param system AS400 object representing the system we are connected to.
   *Used to determine CCSID/encoding for conversions.
   *@param data the S38MSGRM data
  **/
  DDMAS400MessageReply(AS400ImplRemote system, byte[] data) //@B5C
    throws AS400SecurityException,
           InterruptedException,
           IOException
  {
    super(data);
    conv = ConverterImplRemote.getConverter(system.getCcsid(), system); //@B5C
    parseMessageInfo();
  }

  /**
   *Returns the first AS400Message object associated with this object.
   *@return the first AS400Message object associated with this object.
  **/
  AS400Message getAS400Message()
  {
    return (messages_.size() > 0)? (AS400Message)messages_.elementAt(0): new AS400Message();
  }

  /**
   *Returns the AS400Message objects associated with this object.
   *@return the AS400Message objects associated with this object.
  **/
  AS400Message[] getAS400MessageList()
  {
    if (messages_.size() > 0)
    {
      AS400Message[] msgList = new AS400Message[messages_.size()];
      messages_.copyInto(msgList);
      return msgList;
    }
    return new AS400Message[0];
  }

  /**
   *Returns the copyright for the class.
   *@return the copyright for this class.
  **/
  private static String getCopyright()
  {
    return Copyright.copyright;
  }

  /**
   *Extracts the AS400 messages from the data supplied on the constructor
   *for this object.
  **/
  void parseMessageInfo()
  {
    int offset = 8;  // Start after header
    int length;
    int codePoint;
    while(offset < data_.length)
    { // Go through the data extracting AS400 messages
      codePoint = get16bit(offset);
      length = get16bit(offset - 2);
      if (codePoint == DDMTerm.S38MSGRM)
      { // This is an AS400 message; extract it.
        AS400Message msg = getMessage(offset + 2, length - 4);
        messages_.addElement(msg);
      }
      offset += length;
    }
  }

  /**
   *Extracts the message details for a single AS400 message.
   *@param offset the offset in data_ at which to start extracting.
   *@param len the length of the data for this AS400 message.
   *@return the AS400 message that the data represents.
  **/
  AS400Message getMessage(int offset, int len)
  {
    int end = offset + len;
    boolean done = false;
    int codePoint = get16bit(offset + 2);;
    int length;
    while (!done && (offset < end))
    { // Extract the information we want (if it exists) from the message data
      length = get16bit(offset); // Length of a particular piece of message info
      codePoint = get16bit(offset + 2); // Code point indicating which particular
                                        // piece of message info we are at.
      switch(codePoint)
      {
      case DDMTerm.SVRCOD: // Severity code; we want it
        svrCode_ = get16bit(offset + 4);
        offset += length;
        break;
      case DDMTerm.S38MID: // Message id; we want it
        msgId_ = conv.byteArrayToString(data_, offset + 4, length - 4);
        offset += length;
        break;
      case DDMTerm.S38MTEXT: // Message text; we want it
        msgText_ = conv.byteArrayToString(data_, offset + 6, get16bit(offset + 4));
        offset += length;
        break;
      case DDMTerm.S38MTYPE: // Message type
      case DDMTerm.S38MDATA: // Message substitution data (already in message text)
      case DDMTerm.S38MFILE: // Message file
      case DDMTerm.SRVDGN:   // Server diagnostics
        offset += length;    // We don't want any of these
        break;
      default:               // Done with this particular AS400 message
        done = true;
        break;
      }
    }
    // Create an AS400Message object to add to our list of AS400 messages in this reply
    // msgId_ and msgText_ may be null if no information was supplied by the
    // AS400 for these items (yes it does happen, see AS400File.close())
    AS400Message msg = new AS400Message(msgId_, msgText_);
    if (svrCode_ != -1)
    {
      msg.setSeverity(svrCode_);
    }
    return msg;
  }
}

