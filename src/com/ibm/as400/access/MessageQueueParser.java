///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: MessageQueueParser.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

class MessageQueueParser
extends ListParser implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private MessageQueue queue_ = null;

  private AS400Structure fieldTypes_ = null;
  private AS400Structure selInfType_ = null;
  private AS400Structure types_      = null;

  private static final AS400Bin4    intType = new AS400Bin4();

  MessageQueueParser(MessageQueue queue)
  {
    queue_ = queue;
  }

  /**
   * Copyright.
   **/
private static String getCopyright ()
{
  return Copyright.copyright;
}

  private static final QueuedMessage[] NULL_LIST={};

  private QueuedMessage[] messageList_ = NULL_LIST;

  String[] ElemNames = {
      "offset to next entry",
      "offset to fields returned",
      "number of fields returned",
      "sev",
      "id",
      "type",
      "key",
      "file",
      "file lib spec at send time",
      "queue",
      "queue lib used",
      "date",
      "time"
  };

  String[] fieldNames = {
      "offset to next entry",
      "len",
      "id",
      "type of data",
      "status of data",
      "reserved",
      "len of data"
  };

  /**
    * Build the data type for info about
    * one message in the list
    **/
  AS400Structure buildElemType(AS400 system)
  {
    setSystem (system);
    if (types_ == null)
    {
      AS400DataType[] types = new AS400DataType[]
      {
        intType,        // 0 offset to next entry
        intType,        // 1 offset to fields returned
        intType,        // 2 number of fields returned

        intType,        // 3 sev
        new AS400Text( 7, as400_.getCcsid(), as400_),      // 4 id
        new AS400Text( 2, as400_.getCcsid(), as400_),      // 5 type
        new AS400ByteArray (4),        // 6 text4Type, // key
        new AS400Text(10, as400_.getCcsid(), as400_),     // 7 file
        new AS400Text(10, as400_.getCcsid(), as400_),     // 8 file lib spec at send time
        new AS400Text(10, as400_.getCcsid(), as400_),     // 9 queue
        new AS400Text(10, as400_.getCcsid(), as400_),     //10 queue lib used
        new AS400Text( 7, as400_.getCcsid(), as400_),      //11 date
        new AS400Text( 6, as400_.getCcsid(), as400_),      //12 time
      };

      types_ = new AS400Structure(types);
    }
    return types_;
  }
  /**
    * Build the data type for
    * one piece of info about a message in the list
    **/
  AS400Structure buildFieldType()
  {
    if (fieldTypes_ == null)
    {
      AS400DataType[] types = new AS400DataType[]
      {
        intType, // offset to next entry
        intType, // len
        intType, // id

        new AS400Text( 1, as400_.getCcsid(), as400_), // type of data
        new AS400Text( 1, as400_.getCcsid(), as400_), // status of data
        new AS400ByteArray(14),                       // reserved
        intType, // len of data
      };
      fieldTypes_ = new AS400Structure(types);
    }
    return fieldTypes_;
  }

  byte[] buildSelectionInfo( AS400 system,
                             int    severity,
                             String selection )
  {
    setSystem (system);
    if (selInfType_ == null)
    {
      AS400DataType[] types = new AS400DataType[]
      {
        new AS400Text(10, as400_.getCcsid(), as400_), // list direction
        new AS400Bin2(),                              // reserved
        intType,            // severity criteria
        intType,            // max msg len
        intType,            // max msg help len

        intType,            // offset of sel crit *
        intType,            // number of sel crit
        intType,            // offset of starting msg keys *
        intType,            // offset of identifiers of fields to return *
        intType,            // number of fields to return

        // array of selection criterias
        new AS400Text(10, as400_.getCcsid(), as400_),
        new AS400Text(10, as400_.getCcsid(), as400_),
        new AS400Text(10, as400_.getCcsid(), as400_),
        // array of starting message keys
        intType,
        intType,
        // array of field keys
        intType,                // msg
        intType,                // user
        intType,                // from pgm
        intType,                // reply status
        intType                 // default reply
      };
      selInfType_ = new AS400Structure(types);
    }

    Object[] objs = new Object[]
    {
      "*PRV",                 // list direction
      new Short((short)0),    // reserved
      new Integer(severity),  // severity criteria
      new Integer(511),       // max msg len
      new Integer(3000),      // max msg help len

      new Integer(44),        // offset of sel crit *
      new Integer(1),         // number of sel crit
      new Integer(0x4a),      // offset of starting msg keys *
      new Integer(0x52),      // offset of identifiers of fields to return *
      new Integer(5),         // number of fields to return

      // array of selection criterias
      selection,              // *ALL
      "",
      "",
       // array of starting message keys
      new Integer(-1),
      new Integer(-1),
      // array of field keys
      new Integer(302),       // msg
      new Integer(601),       // user
      new Integer(603),       // from pgm
      new Integer(1001),      // reply status
      new Integer(501)        // default reply
    };

    return selInfType_.toBytes( objs );
  }


  // *****
  // * Parse the various levels of the lists
  // *
  void parseLists( AS400 system,
                   byte[] listInfoData,
                   byte[] receiverData ) throws UnsupportedEncodingException
  {
    setSystem (system);
    parseListInfo( listInfoData );
    messageList_ = new QueuedMessage[returnedRecs_];
    rowPos_ =0;
    parseReceiverData( system, receiverData );
  }

  int parseFields( Object[] header,
                   int      nextReceiverPos,
                   byte[]   receiverData )
  {
    QueuedMessage msg = parseElemHeader( header );

    // next item at
    nextReceiverPos  = intFor(header[0]);

    // Each Field
    int numOfFields  = intFor(header[2]);
    int nextFieldPos = intFor(header[1]);

    for (int j=0; j<numOfFields; j++)
    {
      Object[] fieldInfo = (Object[])buildFieldType()
                           .toObject(receiverData,nextFieldPos);

      // last item is the data
      int len = intFor(fieldInfo[6]);
      String str = (String)(new AS400Text(len, getSystem().getCcsid(), getSystem())) //@B6C
                           .toObject( receiverData, nextFieldPos+32 );

      // insert str into message based on id
      int id = intFor(fieldInfo[2]);
      if (id==302)        // msg
          msg.setText( str.trim() );
      else if (id==601)   // qualified sender job
      {
        msg.setUser( str.substring(10, 20).trim() );
        msg.setFromJobName( str.substring(0, 10).trim() );
        msg.setFromJobNumber( str.substring(20).trim() );
      }
      else if (id==603)   // from pgm
        msg.setFromProgram( str.trim() );
      else if (id==501)   // default reply
        msg.setDefaultReply( str.trim() );
      else if (id==1001)  // reply status
        msg.setReplyStatus( str.trim() );

      // move to next field
      nextFieldPos = intFor(fieldInfo[0]);

      // (log)
      Trace.log( Trace.DIAGNOSTIC, "FIELD " + j);
      for (int i=0; i<fieldInfo.length-1; i++)
        Trace.log( Trace.DIAGNOSTIC,  fieldNames[i] + ": "+ fieldInfo[i] );
      Trace.log( Trace.DIAGNOSTIC,  str );
    }
    return nextReceiverPos;
  }

  QueuedMessage parseElemHeader( Object[] header )
  {
    QueuedMessage msg = new QueuedMessage();
    msg.setSeverity( intFor(header[3]) );
    msg.setID(      ((String)header[4]).trim() ); // user/userid set elsewhere
    msg.setType(    Integer.parseInt((String)header[5]) );
    msg.setDate(    (String)header[11], (String)header[12] );
    msg.setKey(     (byte[]) header[6] );

    msg.setFileName(   ((String)header[9]).trim() );
    msg.setLibraryName( ((String)header[10]).trim() );

    msg.setQueue(   queue_ );

    messageList_[rowPos_]=msg;
    return msg;
  }

  QueuedMessage[] clear()
  {
    basicClear();
    messageList_ = NULL_LIST;
    return messageList_;
  }
  QueuedMessage[] getMessageList()
  {
    return messageList_;
  }
}
