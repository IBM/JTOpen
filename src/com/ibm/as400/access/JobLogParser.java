///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JobLogParser.java
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

class JobLogParser
extends ListParser implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private AS400Structure fieldTypes_ = null;
  private AS400Structure selInfType_ = null;
  private AS400Structure types_      = null;

  private static final QueuedMessage[] NULL_LIST={};

  private QueuedMessage[] messageList_ = NULL_LIST;

/**
 * lists of names of fields
**/
  // Used for tracing/debug (no MRI)
  String[] OLJLNames = {
      "offset to next entry",
      "offset to fields returned",
      "number of fields returned",
      "sev",
      "id",
      "type",
      "key",
      "file",
      "file lib spec at send time",
      "date",
      "time"
  };

  // Used for tracing (no MRI)
  String[] fieldNames = {
      "offset to next entry",
      "len",
      "id",
      "type of data",
      "status of data",
      "reserved",
      "len of data"
  };

  // ***
  JobLogParser()
  {
    ElemNames = OLJLNames;
  }

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
        intType, // offset to next entry
        intType, // offset to fields returned
        intType, // number of fields returned

        intType, // sev
        new AS400Text( 7, as400_.getCcsid(), as400_), // id
        new AS400Text( 2, as400_.getCcsid(), as400_), // type
        new AS400ByteArray (4), // intType, // text4Type, // key
        new AS400Text(10, as400_.getCcsid(), as400_), // file
        new AS400Text(10, as400_.getCcsid(), as400_), // file lib spec at send time
        new AS400Text( 7, as400_.getCcsid(), as400_), // date
        new AS400Text( 6, as400_.getCcsid(), as400_), // time
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
        intType,                                      // len of data
      };
      fieldTypes_ = new AS400Structure(types);
    }
    return (fieldTypes_);
  }

  byte[] buildSelectionInfo( AS400 system,
                             String jobName,
                             String userName,
                             String jobNumber)
  {
    setSystem (system);
    if (selInfType_ == null)
    {
      AS400DataType[] types = new AS400DataType[]
      {
        new AS400Text(10, as400_.getCcsid(), as400_), // list direction
        new AS400Text(10, as400_.getCcsid(), as400_), // qualified job name
        new AS400Text(10, as400_.getCcsid(), as400_),
        new AS400Text( 6, as400_.getCcsid(), as400_),
        new AS400Text(16, as400_.getCcsid(), as400_), // internal job id
        intType,        // starting message key
        intType,        // max msg len
        intType,        // max msg help len
        intType,        // offset of id of fields to ret
        intType,        // number of fields to ret
        intType,        // offset to call msg q name
        intType,        // size of call msg q name

        intType, intType, intType, intType, intType,
        new AS400Text( 1, as400_.getCcsid(), as400_)        // call msg q name
      };
      selInfType_ = new AS400Structure(types);
    }

    Object[] objs = new Object[]
    {
      "*PRV",             // list direction
      jobName,            // qualified job name
      userName,
      jobNumber,
      "",                 // internal job id
      new Integer(-1),    // starting message key
      new Integer(511),   // max msg len
      new Integer(3000),  // max msg help len
      new Integer(80),    // offset of id of fields to ret
      new Integer(5),     // number of fields to ret  // $$$ 5?
      new Integer(100),   // offset to call msg q name
      new Integer(1),     // size of call msg q name

      new Integer(302),   // msg
      new Integer(603),   // from pgm
      new Integer(703),   // to pgm
      new Integer(1001),  // rply status
      new Integer(501),   // dflt reply
      "*"                 // call msg q name
    };

    return selInfType_.toBytes( objs );
  }

  /**
   * Copyright.
   **/
private static String getCopyright ()
{
  return Copyright.copyright;
}

  // ***
  void parseLists( AS400 system, byte[] listInfoData, byte[] receiverData ) throws UnsupportedEncodingException
  {
    setSystem (system);
    parseListInfo( listInfoData );
    messageList_ = new QueuedMessage[returnedRecs_];
    rowPos_ =0;
    parseReceiverData( system, receiverData );
  }

  int parseFields( Object[] header, int nextReceiverPos, byte[] receiverData )
  {
      QueuedMessage msg = parseElemHeader( header );

      // next item at
      nextReceiverPos = ((Integer)(header[0])).intValue();

      // Each Field
      int numOfFields = ((Integer)header[2]).intValue();
      int nextFieldPos = ((Integer)(header[1])).intValue();

      AS400Structure fieldType = buildFieldType();

      for (int j=0; j<numOfFields; j++)
      {
        Object[] fieldInfo = (Object[])fieldType
                             .toObject(receiverData,nextFieldPos);

        // last item is the data
        int len = ((Integer)(fieldInfo[6])).intValue();
        String str = (String)(new AS400Text(len, as400_.getCcsid(), as400_))
                             .toObject( receiverData, nextFieldPos+32 );

        // insert str into message based on id
        int id = ((Integer)(fieldInfo[2])).intValue();
        if (id==302) // msg
            msg.setText( str.trim() );
        else if (id==601)   // qualified sender job
        {
          msg.setUser( str.substring(10, 20).trim() );
          msg.setFromJobName( str.substring(0, 10).trim() );
          msg.setFromJobNumber( str.substring(20).trim() );
        }
        else if (id==603) // from pgm
          msg.setFromProgram( str.trim() );
        else if (id==501)   // default reply
          msg.setDefaultReply( str.trim() );
        else if (id==1001)  // reply status
            ;   

        // move to next field
        nextFieldPos = ((Integer)(fieldInfo[0])).intValue();

        Trace.log( Trace.WARNING, "FIELD " + j);
        for (int i=0; i<fieldInfo.length-1; i++)
          Trace.log( Trace.WARNING,  fieldNames[i] + ": "+ fieldInfo[i] );
        Trace.log( Trace.WARNING,  str );
      }
      return nextReceiverPos;
  }

  // ***
  QueuedMessage parseElemHeader( Object[] header )
  {
    // similar to MessageQueueParser, but missing a few fields.
    QueuedMessage msg = new QueuedMessage();
    msg.setSeverity( intFor(header[3]) );
    msg.setID( ((String)header[4]).trim() ); // $$$ user/userid???
    msg.setType( Integer.parseInt(((String)header[5])) );
    msg.setDate( (String)header[9], (String)header[10] );
    msg.setKey( (byte[]) header[6] );

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
