///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ListParser.java
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

abstract class ListParser implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private static final boolean DEBUG = false;
  protected AS400 as400_             = null;

  static final AS400Bin4    intType = new AS400Bin4();

  // data from list info
  int  totalRecs_     = -1;
  int  returnedRecs_  = -1;
  int  currentRecord_ = -1;
  int  handle_        = -1;       // server handle to list
  int  recLength_     = -1;       // @A1

  int rowPos_;

  String[] ElemNames;

  /**
   * Copyright.
   **/
private static String getCopyright ()
{
  return Copyright.copyright;
}

  int getCurrentRecord()
  {
    return currentRecord_;
  }

  int getReturnedRecords()
  {
    return returnedRecs_;
  }

  AS400 getSystem()
  {
    return as400_;
  }

  int getTotalRecords()
  {
    return totalRecs_;
  }

/**
 *  Data Structure Layout:
 *    Field     Offset      Description
 *    -----     ------      ------------------------------------
 *      0       00-03       Total Records
 *      1       04-07       Returned Records
 *      2       08-11       Request Handle
 *      3       12-15       Record Length
 *      4       16          Complete Indicator
 *      5       17-29       Date / Time
 *      6       30          List Status
 *      7       31          Reserved
 *      8       32-35       Length
 *      9       36-39       First Record (?)
**/
  void parseListInfo( byte[] listInfoData )
  {
    totalRecs_       = BinaryConverter.byteArrayToInt(listInfoData, 0);
    returnedRecs_    = BinaryConverter.byteArrayToInt(listInfoData, 4);
    handle_          = BinaryConverter.byteArrayToInt(listInfoData, 8);
    recLength_       = BinaryConverter.byteArrayToInt(listInfoData, 12);   // @A1
    currentRecord_   = returnedRecs_ + BinaryConverter.byteArrayToInt(listInfoData, 36);
  }

// ************************************
  static final int intFor( Object i )
  {
    return ((Integer)i).intValue();
  }


// ************************************
  abstract AS400Structure buildElemType(AS400 system);

  abstract int parseFields( Object[]  header,
                            int       nextReceiverPos,
                            byte[]    receiverData);

  void parseReceiverData( AS400 system, byte[] receiverData )
  {
    // Each Receiver
    int nextReceiverPos = 0;
    Object[] elemh;
    AS400Structure elemType = buildElemType(system);

    for (int k=0; k<returnedRecs_; k++)
    {
      // Header
      elemh = (Object[])elemType.toObject(receiverData, nextReceiverPos);

      // Each Field
      nextReceiverPos = parseFields( elemh, nextReceiverPos, receiverData );

      rowPos_++;
    }
  }


// ************************************
  void closeList( AS400 as400 )
      throws AS400Exception
  {
    if (DEBUG) System.out.println("Closing list " + handle_ );  // $$$ RPS
    try
    {
      ProgramParameter[] parms = new ProgramParameter[2];
      // 1 request handle
      parms[0] = new ProgramParameter( intType.toBytes(new Integer(handle_)) );
      // errors
      parms[1] = new ProgramParameter( new byte[4] );

      ProgramCall pgm = new ProgramCall( as400 );
      handle_ = -1;
      if (pgm.run( "/QSYS.LIB/QGY.LIB/QGYCLST.PGM", parms )==false)
      {
        throw new AS400Exception( pgm.getMessageList() );
      }
    }
    catch (Exception e)
    {
      if (DEBUG) e.printStackTrace();
    }
  }


  // ***
  abstract void parseLists( AS400 system,
                            byte[] listInfoData,
                            byte[] receiverData ) throws UnsupportedEncodingException;

  void more( AS400 as400 )
      throws AS400Exception
  {
    if (getCurrentRecord()<1)
      return; 
    ProgramCall pgm = new ProgramCall( as400 );

    ProgramParameter[] parms = new ProgramParameter[7];

    // 1 receiver variable
    parms[0] = new ProgramParameter( 5120 );
    // 2 receiver len
    byte[] msgsize = intType.toBytes(new Integer(5120) );
    parms[1] = new ProgramParameter( msgsize );

    // 3 request handle
    parms[2] = new ProgramParameter( intType.toBytes(new Integer(handle_)) );

    // 4 list information
    parms[3] = new ProgramParameter( 80 );

    // 5 number of records to return
    parms[4] = new ProgramParameter( intType.toBytes(new Integer(10)) );

    // 6 starting record
    parms[5] = new ProgramParameter( intType.toBytes(new Integer(getCurrentRecord())) );

    // 7 error code ? inout, char*
    parms[6] = new ProgramParameter( intType.toBytes( new Integer(0) ));

    // do it
    try
    {
      if (pgm.run( "/QSYS.LIB/QGY.LIB/QGYGTLE.PGM", parms )==false)
      {
        // error on run
        throw new AS400Exception( pgm.getMessageList() );
      }

      byte[] listInfoData = parms[3].getOutputData(); // $$$  The byte[] could be passed to the parseLists
      byte[] receiverData = parms[0].getOutputData(); //      directly.  Probably more readable like this though.
      parseLists( as400, listInfoData, receiverData );
    }
    catch (Exception e)
    {
      if (DEBUG) e.printStackTrace();
    }
  }

  final void basicClear()
  {
    totalRecs_      = -1;
    returnedRecs_   = -1;
  }

  void setSystem (AS400 system)
  {
    as400_ = system;
  }
}
