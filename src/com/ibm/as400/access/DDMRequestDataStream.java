///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DDMRequestDataStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.CharConversionException;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 *Base class for DDM request data streams.  Initializes request data
 *streams appropriately.
**/
class DDMRequestDataStream extends DDMDataStream
{
  /**
   *Constructs request data stream with defaults:
   *  Don't continue on error, not chained, GDS id = D0, type = RQSDSS,
   *  no same request correlation.
   *Because the length is unknown, only the header is constructed.
  **/
  DDMRequestDataStream()
  {
    super();
    // Initialize the header:
    //  Don't continue on error, not chained, GDS id = D0, type = RQSDSS,
    //  no same request correlation.
    setContinueOnError(false);
    setIsChained(false);
    setGDSId((byte)0xD0);
    setHasSameRequestCorrelation(false);
    setType(1);
  }

  /**
   *Constructs request data stream with defaults:
   *  Don't continue on error, not chained, GDS id = D0, type = RQSDSS,
   *  no same request correlation.
   *@param length The total length of the data stream.
  **/
  DDMRequestDataStream(int length)
  {
    super(length);
    // Initialize the header:
    //  Don't continue on error, not chained, GDS id = D0, type = RQSDSS,
    //  no same request correlation.
    setContinueOnError(false);
    setIsChained(false);
    setGDSId((byte)0xD0);
    setHasSameRequestCorrelation(false);
    setType(1);
  }

  /**
   *Constructs request data stream as specified with defaults:
   *  GDS id = D0, type = RQSDSS.
   *@param The total length of the data stream.
   *@param contOnError true if we are to continue on error, false otherwise.
   *@param chained true if this data stream is chained, false otherwise.
   *@param sameRequestCorrelator true if this data stream has the same
   *                            request correlator as the last, false otherwise.
  **/
  DDMRequestDataStream(int length, boolean contOnError, boolean chained,
                       boolean sameRequestCorrelator)
  {
    super(length);
    // Initialize the header:
    //  Don't continue on error, not chained, GDS id = D0, type = RQSDSS,
    //  no same request correlation.
    setContinueOnError(contOnError);
    setIsChained(chained);
    setGDSId((byte)0xD0);
    setHasSameRequestCorrelation(sameRequestCorrelator);
    setType(1);
  }

  /**
   *Returns the commit data stream request.
   *@return DDMRequestDataStream for committing transactions.
   *Commit request:
   * Term = CMMUOW
   * Parms = LUWHLDCSR => byte[1] Hold cursor parameter.  Always set to F1 for DDM.
   * Size = 6 --> Header (0 - 5)
   *        2 --> LL Length of CMMUOW term and parms (6,7)
   *        2 --> CP CMMUOW code point (8,9)
   *        2 --> LL Length of LUWHLDCSR term and parm (10,11)
   *        2 --> CP LUWHLDCSR code point (12,13)
   *        1 --> LUWHLDCSR parm (14)
   *      -----
   *       15 --> Total length of the data stream
  **/
  static DDMRequestDataStream getRequestCMMUOW()
  {
    // The total length of this data stream is 15
    DDMRequestDataStream req = new DDMRequestDataStream(15);

    req.set16bit(9, 6);         // Set the total length remaining after the header
    req.set16bit(DDMTerm.CMMUOW, 8);  // Set code point for CMMUOW term
    req.set16bit(5, 10);        // Set length of the LUWHLDCSR parm
    req.set16bit(DDMTerm.LUWHLDCSR, 12);  // Set code point for LUWHLDCSR parm
    // Set the LUWHLDCSR parm to '1'
    req.data_[14] = (byte)0xF1;

    return req;
  }

// This method is not used anywhere
//  /**
//   *Returns the exchange attributes data stream request.
//   *@param release indicates if we are exchanging attributes with a pre-v4r2
//   *system or a v4r2 or later system.  Valid values are: V4R2 or PREV4R2.
//   *@return DDMRequestDataStream for exchanging attributes.
//   *Exchange attributes request:
//   * Term = EXCSAT
//   * Parms = SRVCLSNM -> CHRSTRDR => Character string containing server class name
//   *                                 in EBCDIC
//   * Size = 6 --> Header (0 - 5)
//   *        2 --> LL Length of EXCSAT term and parms (6,7)
//   *        2 --> CP EXCSAT code point (8,9)
//   *        2 --> LL Length of SRVCLSNM term and parm (10,11)
//   *        2 --> CP SRVCLSNM code point (12, 13)
//   *        2 --> LL Length of CHRSTRDR term and parms (14,15)
//   *        2 --> CP CHRSTRDR code point (16,17)
//   *        3 --> SRVCLSNM parm (QAS) (18 - 20)
//   *        2 --> LL length of MGRLVLLS term and parms
//   *        2 --> MGRLVLLS code point
//   *   4 * 23 --> Manager level parms and their values
//   *       ----
//   *       117 --> Total length of the data stream
//  **/
//  static DDMRequestDataStream getRequestEXCSAT(String release, AS400ImplRemote system) //@B5C
//    throws IOException
//  {
//    DDMRequestDataStream req;
//    req = new DDMRequestDataStream(117);
//    req.set16bit(111, 6); // Set total length remaining after header
//
//    req.set16bit(DDMTerm.EXCSAT, 8); // Set code point for EXCSAT term
//    req.set16bit(11, 10);            // Set length of SRVCLSNM parm
//    req.set16bit(DDMTerm.SRVCLSNM, 12); // Set code point for SRVCLSNM parm
//    req.set16bit(7, 14);                // Set length of CHRSTRDR parm
//    req.set16bit(DDMTerm.CHRSTRDR, 16); // Set code point for CHRSTRDR parm
//    // Set the server class name (SRVCLSNM parm)
//    ConverterImplRemote c = ConverterImplRemote.getConverter(system.getCcsid(), system); //@B5C
//    c.stringToByteArray("QAS", req.data_, 18);
//    // Set the MGRLVLS values.  Each parameter for the MGRLVLS term is appended to
//    // to the array as code point/value pairs.  No length bytes precede the
//    // parameters.
//    req.set16bit(96, 21);
//    req.set16bit(DDMTerm.MGRLVLLS, 23);
//    req.set16bit(DDMTerm.AGENT, 25);
//    req.set16bit(3, 27);
//    req.set16bit(DDMTerm.ALTINDF, 29);
//    req.set16bit(3, 31);
//    req.set16bit(DDMTerm.CMBACCAM, 33);
//    req.set16bit(3, 35);
//    req.set16bit(DDMTerm.CMBKEYAM, 37);
//    req.set16bit(3, 39);
//    req.set16bit(DDMTerm.CMBRNBAM, 41);
//    req.set16bit(3, 43);
//    if (release.equalsIgnoreCase("PREV4R2"))
//    {
//      req.set16bit(DDMTerm.CMNAPPC, 45);
//      req.set16bit(3, 47);
//    }
//    else
//    { // V4R2 system or later
//      req.set16bit(DDMTerm.CMNTCPIP, 45);  // Set CMNTCPIP code point
//      req.set16bit(5, 47);  // Set value for CMNTCPIP.  Must be 5.
//    }
//    req.set16bit(DDMTerm.DICTIONARY, 49);
//    req.set16bit(1, 51);
//    req.set16bit(DDMTerm.DIRECTORY, 53);
//    req.set16bit(3, 55);
//    req.set16bit(DDMTerm.DIRFIL, 57);
//    req.set16bit(3, 59);
//    req.set16bit(DDMTerm.DRCAM, 61);
//    req.set16bit(3, 63);
//    req.set16bit(DDMTerm.KEYFIL, 65);
//    req.set16bit(3, 67);
//    req.set16bit(DDMTerm.LCKMGR, 69);
//    req.set16bit(3, 71);
//    req.set16bit(DDMTerm.RDB, 73);
//    req.set16bit(3, 75);
//    req.set16bit(DDMTerm.RELKEYAM, 77);
//    req.set16bit(3, 79);
//    req.set16bit(DDMTerm.RELRNBAM, 81);
//    req.set16bit(3, 83);
//    req.set16bit(DDMTerm.SECMGR, 85);
//    req.set16bit(1, 87);
//    req.set16bit(DDMTerm.SEQFIL, 89);
//    req.set16bit(3, 91);
//    req.set16bit(DDMTerm.SQLAM, 93);
//    req.set16bit(3, 95);
//    req.set16bit(DDMTerm.STRAM, 97);
//    req.set16bit(3, 99);
//    req.set16bit(DDMTerm.STRFIL, 101);
//    req.set16bit(3, 103);
//    req.set16bit(DDMTerm.SUPERVISOR, 105);
//    req.set16bit(3, 107);
//    req.set16bit(DDMTerm.SYSCMDMGR, 109);
//    req.set16bit(4, 111);
//    req.set16bit(DDMTerm.RSCRCVM, 113);
//    req.set16bit(4, 115);
//
//    return req;
//  }


  /**
   *Returns the rollback data stream request.
   *@return DDMRequestDataStream for rolling back transactions.
   *Rollback request:
   * Term = RLLBCKUOW
   * Parms = LUWHLDCSR => byte[1] Hold cursor parameter.  Always set to F1 for DDM.
   * Size = 6 --> Header (0 - 5)
   *        2 --> LL Length of CMMUOW term and parms (6,7)
   *        2 --> CP CMMUOW code point (8,9)
   *        2 --> LL Length of LUWHLDCSR term and parm (10,11)
   *        2 --> CP LUWHLDCSR code point (12,13)
   *        1 --> LUWHLDCSR parm (14)
   *      -----
   *       15 --> Total length of the data stream
  **/
  static DDMRequestDataStream getRequestRLLBCKUOW()
  {
    // The total length of this data stream is 15
    DDMRequestDataStream req = new DDMRequestDataStream(15);

    req.set16bit(9, 6);         // Set the total length remaining after the header
    req.set16bit(DDMTerm.RLLBCKUOW, 8);  // Set code point for RLLBCKUOW term
    req.set16bit(5, 10);        // Set length of the LUWHLDCSR parm
    req.set16bit(DDMTerm.LUWHLDCSR, 12);  // Set code point for LUWHLDCSR parm
    // Set the LUWHLDCSR parm to '1'
    req.data_[14] = (byte)0xF1;

    return req;
  }

// This method is not used anywhere
//  /**
//   *Returns the start commitment control data stream request.
//   *@param lockLevel commitment lock level.
//   *@return DDMRequestDataStream for starting commitment control.
//   *Start commitment control request:
//   * Term = STRCMMCTL
//   * Parms = CMMCTLTYP => byte[2] Commitment control type. Always set to 0x0001.
//   *         FILISOLVL => byte[2] Commitment control lock level.
//   *                      0x2441 - *CHG
//   *                      0x2442 - *CS
//   *                      0x2443 - *ALL
//   *                      0x2445 = *NONE
//   * Size = 6 --> Header (0 - 5)
//   *        2 --> LL Length of SSTRCMMCTL term and parms (6,7)
//   *        2 --> CP STRCMMCTL code point (8,9)
//   *        2 --> LL Length of CMMCTLTYP term and parm (10,11)
//   *        2 --> CP CMMCTLTYP code point (12,13)
//   *        2 --> CMMCTLTYP parm (14, 15)
//   *        2 --> LL Length of FILISOLVL term and parm (16,17)
//   *        2 --> CP FILISOLVL code point (18,19)
//   *        2 --> FILISOLVL parm (20,21)
//   *      -----
//   *       22 --> Total length of the data stream
//  **/
//  static DDMRequestDataStream getRequestSTRCMMCTL(int lockLevel)
//  {
//    // The total length of this data stream is 22
//    DDMRequestDataStream req = new DDMRequestDataStream(22);
//
//    req.set16bit(16, 6);         // Set the total length remaining after the header
//    req.set16bit(DDMTerm.STRCMMCTL, 8); // Set code point for STRCMMCTL term
//    req.set16bit(6, 10);        // Set length of the CMMCTLTYP parm
//    req.set16bit(DDMTerm.CMMCTLTYP, 12);  // Set code point for CMMCTLTYP parm
//    // Set the commitment control type
//    req.data_[14] = (byte)0x00;
//    req.data_[15] = (byte)0x01;
//
//    req.set16bit(6, 16);         // Set length of the FILISOLVL parm
//    req.set16bit(DDMTerm.FILISOLVL, 18);// Set code point for FILISOLVL parm
//    req.data_[20] = (byte)0x24;  // All the commit lock levels start with 24
//    switch (lockLevel)
//    {
//      case AS400FileConstants.COMMIT_LOCK_LEVEL_CHANGE: //@C0C
//      {
//        req.data_[21] = (byte)0x41;
//        break;
//      }
//      case AS400FileConstants.COMMIT_LOCK_LEVEL_CURSOR_STABILITY: //@C0C
//      {
//        req.data_[21] = (byte)0x42;
//        break;
//      }
//      case AS400FileConstants.COMMIT_LOCK_LEVEL_ALL: //@C0C
//      {
//        req.data_[21] = (byte)0x43;
//        break;
//      }
//      default:
//      {
//        // Note: We should never get here.  Presumably the lock level is checked
//        // by the calling method.
//        throw new ExtendedIllegalArgumentException("lockLevel", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
//      }
//    }
//
//    return req;
//  }

// This method is not used anywhere
//  /**
//   *Returns the allocate object data stream request.
//   *@param lock the lock to obtain.
//   *@param library library name.
//   *@param file file name.
//   *@param member member name.
//   *@param wait number of seconds to wait for locks.
//   *@param system the system from which to get the CCSID for conversions
//   *@return DDMRequestDataStream for locking a file.
//   *S38ALCOB request:
//   * Term = S38ALCOB
//   * Parms = S38LRQS => FILANM => RQSFILLK =>Lock request structure:
//   *         S38LWAIT => bin4 => Amount of time to wait for the lock
//   * Size = 6 --> Header (0 - 5)
//   *        2 --> LL Length of S38ALCOB term and parms
//   *        2 --> CP S38ALCOB code point
//   *        2 --> LL Length of S38LRQS term and parm
//   *        2 --> CP S38LRQS code point
//   *          2 --> LL FILNAM term length
//   *          2 --> CP FILNAM code point
//   *          library.length + file.length + member.length + 3
//   *            --> FILNAM value = library/file(member)
//   *          2 --> LL RQSFILLK length
//   *          2 --> CP of RQSFILLK term
//   *          1 --> Value for RQSFILLK
//   *        2 --> LL Length of S38LWAIT term and parm
//   *        2 --> CP S38LWAIT code point
//   *        4 --> S38LWAIT parm
//   *      -----
//   *       34 + library.length + file.length + member.length
//   *          --> Total length of the data stream
//  **/
//  static DDMRequestDataStream getRequestS38ALCOB(byte lock, String library,
//                                                 String file, String member, int wait,
//                                                 AS400ImplRemote system) //@B5C
//    throws AS400SecurityException,
//           InterruptedException,
//           IOException
//  {
//    DDMRequestDataStream req =
//       new DDMRequestDataStream(34 + library.length() +
//                                file.length() + member.length());
//    // Determine the lengths of the terms
//    int fileNameLength = 3 + library.length() + file.length() + member.length();
//    int llFILNAM = 4 + fileNameLength;
//    int llRQSFILLK = 5;
//    int llS38LWAIT = 8;
//    int llS38LRQS = 13 + fileNameLength;
//
//    // Set the total length remaining after the header
//    req.set16bit(25 + fileNameLength, 6);
//    req.set16bit(DDMTerm.S38ALCOB, 8); // Set code point for S38ALCOB term
//
//    // Set the S38LRQS term
//    req.set16bit(llS38LRQS, 10); // Length of S38LRQS term
//    req.set16bit(DDMTerm.S38LRQS, 12); // Set S38LRQS code point
//    req.set16bit(llFILNAM, 14); // Length of FILNAM term - first parameter of the S38LRQS term
//    req.set16bit(DDMTerm.FILNAM, 16); // Set FILNAM codepoint
//    // Determine the value for the FILNAM parm
//    ConverterImplRemote c = ConverterImplRemote.getConverter(system.getCcsid(), system); //@B5C
//    c.stringToByteArray(library + "/" + file + "(" + member + ")", req.data_, 18);
//    int offset = 18 + fileNameLength;
//    req.set16bit(llRQSFILLK, offset); // Length of RQSFILLK term - second parameter of the S38LRQS term
//    req.set16bit(DDMTerm.RQSFILLK, offset + 2); // Set RQSFILLK code point
//    req.data_[offset + 4] = lock; // Value for the RQSFILLK term - already checked by the calling method
//
//    // Set the S38LWAIT term
//    req.set16bit(llS38LWAIT, offset + 5); // Length of the S38LWAIT term
//    req.set16bit(DDMTerm.S38LWAIT, offset + 7); // Set S38LWAIT code point
//    req.set32bit(wait, offset + 9); // S38LWAIT value
//
//    return req;
//  }

  /**
   *Returns the close file data stream request.
   *@param dclName the declared file name for the file.
   *@return DDMRequestDataStream for closing a file.
   *Close file request:
   * Term = S38CLOSE
   * Parms = DCLNAM => byte[8] containing declared name (alias)
   *                   for the file. The first byte must be >= 0x40.
   *         S38CLOST => 1 byte indicating type of close
   * Size = 6 --> Header (0 - 5)
   *        2 --> LL Length of S38CLOSE term and parms (6,7)
   *        2 --> CP S38CLOSE code point (8,9)
   *        2 --> LL Length of DCLNAM term and parm (10,11)
   *        2 --> CP DCLNAM code point (12,13)
   *        8 --> DCLNAM parm (14 - 21)
   *        2 --> LL Length of S38CLOST term and parm (22,23)
   *        2 --> CP S38CLOST code point (24,25)
   *        1 --> S38CLOST parm (26)
   *      -----
   *       27 --> Total length of the data stream
  **/
  static DDMRequestDataStream getRequestS38CLOSE(byte[] dclName)
  {
    // The total length of this data stream is 27
    DDMRequestDataStream req = new DDMRequestDataStream(27);

    req.set16bit(21, 6);         // Set the total length remaining after the header
    req.set16bit(DDMTerm.S38CLOSE, 8); // Set code point for S38CLOSE term
    req.set16bit(12, 10);        // Set length of the DCLNAM parm
    req.set16bit(DDMTerm.DCLNAM, 12);  // Set code point for DCLNAM parm
    // Set the declared name; declaredName is an 8-byte array
    System.arraycopy(dclName, 0, req.data_, 14, 8);
    req.set16bit(5, 22);         // Set length of the S38CLOST parm
    req.set16bit(DDMTerm.S38CLOST, 24);// Set code point for S38CLOST parm
    req.data_[26] = (byte)0x02;  // Set value for S38CLOST - 2 indicates permanent close

    return req;
  }

  /**
   *Returns the submit command data stream request.
   *@param command the command to submit.
   *@param system the system from which to get the CCSID for conversions.
   *@return DDMRequestDataStream for submitting a command.
   *Submit remote command request:
   * Term = S38CMD
   * Parms = S38CMDST => byte[] containing the command to be submitted.
   * Size = 6 --> Header (0 - 5)
   *        2 --> LL Length of S38CMD term and parms (6,7)
   *        2 --> CP S38CMD code point (8,9)
   *        2 --> LL Length of S38CMDST term and parm (10,11)
   *        2 --> CP S38CMDST code point (12,13)
   *        command.length()
   *          --> S38CMDST parm (14 - 14 + command.length() - 1)
   *      -----
   *       14 + command.length() --> Total length of the data stream
  **/
  static DDMRequestDataStream getRequestS38CMD(String command, AS400ImplRemote system) //@B5C
    throws AS400SecurityException,
           InterruptedException,
           IOException
  {
    // Convert command reight away in order to determine number of bytes
    // need for the command
    ConverterImplRemote c = ConverterImplRemote.getConverter(system.getCcsid(), system); //@B5C
    byte[] cmd = c.stringToByteArray(command);
    // The total length of this data stream is 14 + cmd.length()
    DDMRequestDataStream req = new DDMRequestDataStream(14 + cmd.length);
    req.set16bit(8 + cmd.length, 6); // Set total length remaining after header
    req.set16bit(DDMTerm.S38CMD, 8);             // Set code point for S38CMD term
    req.set16bit(4 + cmd.length, 10);// Set length of S38CMDST parm
    req.set16bit(DDMTerm.S38CMDST, 12);          // Set code point for S38CMDST parm
    // Set the command
    System.arraycopy(cmd, 0, req.data_, 14, cmd.length);

    return req;
  }

  /**
   *Returns the delete record data stream request.
   *@param dclName the declared file name for the file.
   *@return DDMRequestDataStream for opening file.
   *Open file request:
   * Term = S38DEL
   * Parms = DCLNAM => byte[8] containing declared name (alias)
   *                   for the file. The first byte must be >= 0x40.
   * Size = 6 --> Header (0 - 5)
   *        2 --> LL Length of S38DEL term and parms (6,7)
   *        2 --> CP S38DEL code point (8,9)
   *        2 --> LL Length of DCLNAM term and parm (10,11)
   *        2 --> CP DCLNAM code point (12,13)
   *        8 --> DCLNAM parm (14 - 21)
   *      -----
   *       22 --> Total length of the data stream
  **/
  static DDMRequestDataStream getRequestS38DEL(byte[] dclName)
  {
    // The total length of this data stream is 22
    DDMRequestDataStream req = new DDMRequestDataStream(22);

    req.set16bit(16, 6);             // Set total length remaining after header
    req.set16bit(DDMTerm.S38DEL, 8); // Set code point for S38DEL term
    req.set16bit(12, 10);            // Set length of DCLNAM parm
    req.set16bit(DDMTerm.DCLNAM, 12);// Set code point for DCLNAM parm
                                     // Set the declared name; declaredName is an 8-byte array
    System.arraycopy(dclName, 0, req.data_, 14, 8);

    return req;
  }

// This method is not used anywhere
//  /**
//   *Returns the deallocate object data stream request.
//   *@param locks The locks to release.
//   *@param library library name.
//   *@param file file name.
//   *@param member member name.
//   *@param system the system from which to get the CCSID for conversions.
//   *@return DDMRequestDataStream for releasing explicit locks on a file.
//   *S38DLCOB:
//   * Term = S38DLCOB
//   * Parms = S38LRLS => RLSFILLK => Lock request structure:
//   *            2 --> LL Length of FILNAM term and parm
//   *            2 --> CP FILNAM code point
//   *            library.length() + file.length() + member.length() + 3
//   *              --> FILNAM parm, library/file(member)
//   *            2 --> LL Length of RLSFILLK term and parm
//   *            2 --> CP RLSFILLK code point
//   *            1 --> byte indicating type of lock
//   *            There can be one or more S38LRLS terms
//   * Size = 6 --> Header (0 - 5)
//   *        2 --> LL Length of S38DLCOB term and parms
//   *        2 --> CP S38DLCOB code point
//   *        Repeatable:
//   *        2 --> LL Length of S38LRLS term and parm
//   *        2 --> CP S38LRLS code point
//   *          2 --> LL FILNAM term
//   *          2 --> CP FILNAM term
//   *          library.length + file.length + member.length + 3
//   *            --> FILNAM value = library/file(member)
//   *          2 --> LL RLSFILLK length
//   *          2 --> CP of RLSFILLK term
//   *          1 --> Value for RQSFILLK
//   *      -----
//   *      10 +
//   *      locks.length * (16 + library.length() + file.length() + member.length())
//  **/
//  static DDMRequestDataStream getRequestS38DLCOB(byte[] locks, String library,
//                                                 String file, String member,
//                                                 AS400ImplRemote system) //@B5C
//    throws AS400SecurityException,
//           InterruptedException,
//           IOException
//  {
//    DDMRequestDataStream req =
//          new DDMRequestDataStream(10 + locks.length * (16 + library.length() +
//                                   file.length() + member.length()));
//    // Determine the lengths of the terms
//    int fileNameLength = 3 + library.length() + file.length() + member.length();
//    int llFILNAM = 4 + fileNameLength;
//    int llRLSFILLK = 5;
//    int llS38LRLS = 13 + fileNameLength;
//
//    // Set the total length remaining after the header
//    req.set16bit(req.data_.length - 6, 6);
//    req.set16bit(DDMTerm.S38DLCOB, 8); // Set code point for S38DLCOB term
//
//    // Determine the value for the FILNAM parm
//    byte[] fileName = new byte[fileNameLength];
//    ConverterImplRemote c = ConverterImplRemote.getConverter(system.getCcsid(), system); //@B5C
//    c.stringToByteArray(library + "/" + file + "(" + member + ")", fileName, 0);
//
//    // Set the repeatable portion of the data stream (one or more S38LRLS terms)
//    int offsetRLSFILLK;
//    for (short i = 0, offset = 10; i < locks.length; ++i)
//    {
//      req.set16bit(llS38LRLS, offset);           // Set length of the S38LRLS term
//      req.set16bit(DDMTerm.S38LRLS, offset + 2); // Set code point for S38LRLS term
//      req.set16bit(llFILNAM, offset + 4);        // Set the length of the FILNAM term
//      req.set16bit(DDMTerm.FILNAM, offset + 6);  // Set the code point for FILNAM term
//                                                 // Set the FILNAM parm
//      System.arraycopy(fileName, 0, req.data_, offset + 8, fileName.length);
//      offsetRLSFILLK = offset + 8 + fileName.length;
//      req.set16bit(llRLSFILLK, offsetRLSFILLK);// Set length of the RLSFILLK term
//      req.set16bit(DDMTerm.RLSFILLK, offsetRLSFILLK + 2);  // Set code point for RLSFILLK term
//      req.data_[offsetRLSFILLK + 4] = locks[i];  // Set the RLSFILLK parm
//      offset += llS38LRLS;
//    }
//
//    return req;
//  }

  /**
   *Returns the S38FEOD request data stream with the operation specified for
   *positioning.
   *@param dclName the declared file name for the file.
   *@param type type option
   *@param share share option
   *@param data data option
   *@return DDMRequestDataStream for retrieving a record.
   * Term = S38FEOD
   * Parms = DCLNAM => byte[8] containing declared name (alias)
   *                   for the file. The first byte must be >= 0x40.
   * Size = 6 --> Header (0-5)
   *        2 --> LL S38FEOD term and parms (6,7)
   *        2 --> CP S38FEOD code point (8,9)
   *        2 --> LL DCLNAM term and parm length (10,11)
   *        2 --> CP DCLNAM code point (12,13)
   *        8 --> DCLNAM parm (14-21)
   *        2 --> LL S38OPTL term and parm length (22,23)
   *        2 --> CP S38OPTL code point (24,25)
   *        4 --> S38OPTL parm
   *              1 --> type (26)
   *              1 --> share (27)
   *              1 --> data (28)
   *              1 --> operation (29)
   *       ---
   *       30 --> data stream length
  **/
  static DDMRequestDataStream getRequestS38FEOD(byte[] dclName, int type,
                                               int share, int data)
  {
    DDMRequestDataStream req = new DDMRequestDataStream(30);

    req.set16bit(req.data_.length - 6, 6); // total length after header
    req.set16bit(DDMTerm.S38FEOD, 8);

    // Set the DCLNAM LL, CP, and parm.
    req.set16bit(12, 10);
    req.set16bit(DDMTerm.DCLNAM, 12);
    System.arraycopy(dclName, 0, req.data_, 14, 8);

    // Set the S38OPTL LL, CP, and parm.
    req.set16bit(8, 22);
    req.set16bit(DDMTerm.S38OPTL, 24);
    req.data_[26] = (byte)type;
    req.data_[27] = (byte)share;
    req.data_[28] = (byte)data;
    req.data_[29] = 0x01; // _OPER_GET

    return req;
  }

  /**
   *Returns the S38GET request data stream.
   *@param dclName the declared file name for the file.
   *@param type type option
   *@param share share option
   *@param data data option
   *@return DDMRequestDataStream for retrieving a record.
   * Term = S38GET
   * Parms = DCLNAM => byte[8] containing declared name (alias)
   *                   for the file. The first byte must be >= 0x40.
   * Size = 6 --> Header (0-5)
   *        2 --> LL S38GET term and parms (6,7)
   *        2 --> CP S38GET code point (8,9)
   *        2 --> LL DCLNAM term and parm length (10,11)
   *        2 --> CP DCLNAM code point (12,13)
   *        8 --> DCLNAM parm (14-21)
   *        2 --> LL S38OPTL term and parm length (22,23)
   *        2 --> CP S38OPTL code point (24,25)
   *        4 --> S38OPTL parm
   *              1 --> type (26)
   *              1 --> share (27)
   *              1 --> data (28)
   *              1 --> operation (29)
   *       ---
   *       30 --> data stream length
  **/
  static DDMRequestDataStream getRequestS38GET(byte[] dclName, int type,
                                               int share, int data)
  {
    DDMRequestDataStream req = new DDMRequestDataStream(30);

    req.set16bit(req.data_.length - 6, 6); // total length after header
    req.set16bit(DDMTerm.S38GET, 8);

    // Set the DCLNAM LL, CP, and parm.
    req.set16bit(12, 10);
    req.set16bit(DDMTerm.DCLNAM, 12);
    System.arraycopy(dclName, 0, req.data_, 14, 8);

    // Set the S38OPTL LL, CP, and parm.
    req.set16bit(8, 22);
    req.set16bit(DDMTerm.S38OPTL, 24);
    req.data_[26] = (byte)type;
    req.data_[27] = (byte)share;
    req.data_[28] = (byte)data;
    req.data_[29] = 1; // _OPER_GET

    return req;
  }

  /**
   *Returns the S38GETD request data stream.
   *@param dclName the declared file name for the file.
   *@param recordFormat the record format for the file.
   *@param type type option
   *@param share share option
   *@param data data option
   *@param rrn relative record number
   *@param system the system from which to get the CCSID for conversions.
   *@return DDMRequestDataStream for retrieving a record.
   * Term = S38GETD
   * Parms = DCLNAM => byte[8] containing declared name (alias)
   *                   for the file. The first byte must be >= 0x40.
   * Size = 6 --> Header (0-5)
   *        2 --> LL S38GETD term and parms (6,7)
   *        2 --> CP S38GETD code point (8,9)
   *        2 --> LL DCLNAM term and parm length (10,11)
   *        2 --> CP DCLNAM code point (12,13)
   *        8 --> DCLNAM parm (14-21)
   *        2 --> LL S38OPTL term and parm length (22,23)
   *        2 --> CP S38OPTL code point (24,25)
   *        4 --> S38OPTL parm
   *              1 --> type (26)
   *              1 --> share (27)
   *              1 --> data (28)
   *              1 --> operation (29)
   *        2 --> LL S38CTLL term and parm length (30,31)
   *        2 --> CP S38CTLL code point (32,33)
   *        26--> S38CTLL parm
   *              1 --> record format ID (34)
   *              2 --> record format length (35,36)
   *              10--> record format name (37-46)
   *              1 --> member number ID (47)
   *              2 --> member number length (48,49)
   *              2 --> member number value (50,51)
   *              1 --> relative record number ID (52)
   *              2 --> relative record number length (53,54)
   *              4 --> relative record number (55-58)
   *              1 --> control list end (59)
   *       ---
   *       60 --> data stream length
  **/
//  static DDMRequestDataStream getRequestS38GETD(byte[] dclName, RecordFormat recordFormat, int type, int share, int data, int  rrn, AS400 system)   // @A1D
  static DDMRequestDataStream getRequestS38GETD(byte[] dclName,
                                                byte[] recordFormatCTLLName,
                                                int type,
                                                int share,
                                                int data,
                                                int rrn,
                                                AS400ImplRemote system)   // @A1A @B5C
    throws AS400SecurityException,
           InterruptedException,
           IOException
  {
    DDMRequestDataStream req = new DDMRequestDataStream(60);

    req.set16bit(req.data_.length - 6, 6); // total length after header
    req.set16bit(DDMTerm.S38GETD, 8);

    // Set the DCLNAM LL, CP, and parm.
    req.set16bit(12, 10);
    req.set16bit(DDMTerm.DCLNAM, 12);
    System.arraycopy(dclName, 0, req.data_, 14, 8);

    // Set the S38OPTL LL, CP, and parm.
    req.set16bit(8, 22);
    req.set16bit(DDMTerm.S38OPTL, 24);
    req.data_[26] = (byte)type;
    req.data_[27] = (byte)share;
    req.data_[28] = (byte)data;
    req.data_[29] = 2; // _OPER_GETD

    // Set the S38CTLL LL, CP, and parm.  The control list sequence for
    // GETD parm is record format, member number, relative record number,
    // and control list end.
    req.set16bit(30, 30);
    req.set16bit(DDMTerm.S38CTLL, 32);
    req.data_[34] = 1; // record format ID
    req.set16bit(10, 35); // record format length

    // Start of @A2D
    /*
    StringBuffer recordName = new StringBuffer(recordFormat.getName());
    while (recordName.length() < 10) recordName.append(' ');
    Converter c = Converter.getConverter(system.getCcsid(), system);
    c.stringToByteArray(recordName.toString(), req.data_, 37);
    */
    // End of @A2D

    // @A2A
    System.arraycopy(recordFormatCTLLName, 0, req.data_, 37, recordFormatCTLLName.length);

    req.data_[47] = 0xf; // member number ID
    req.set16bit(2, 48); // member number length
    req.set16bit(0, 50); // member number value
    req.data_[52] = 2; // relative record number ID
    req.set16bit(4, 53); // relative record number length
    req.set32bit(rrn, 55); // relative record number value
    req.data_[59] = (byte) 0xff; // control list end

    return req;
  }


  /**
   *Returns the S38GETK request data stream.
   *@param dclName the declared file name for the file.
   *@param recordFormat the record format for the file.
   *@param type type option
   *@param share share option
   *@param data data option
   *@param keyFields the fields of the key
   *@param system the system from which to get the CCSID for conversions.
   *@return DDMRequestDataStream for retrieving a record.
   * Term = S38GETK
   * Parms = DCLNAM => byte[8] containing declared name (alias)
   *                   for the file. The first byte must be >= 0x40.
   * Size = 6 --> Header (0-5)
   *        2 --> LL S38GETK term and parms (6,7)
   *        2 --> CP S38GETK code point (8,9)
   *        2 --> LL DCLNAM term and parm length (10,11)
   *        2 --> CP DCLNAM code point (12,13)
   *        8 --> DCLNAM parm (14-21)
   *        2 --> LL S38OPTL term and parm length (22,23)
   *        2 --> CP S38OPTL code point (24,25)
   *        4 --> S38OPTL parm
   *              1 --> type (26)
   *              1 --> share (27)
   *              1 --> data (28)
   *              1 --> operation (29)
   *        2 --> LL S38CTLL term and parm length (30,31)
   *        2 --> CP S38CTLL code point (32,33)
   *        ? --> S38CTLL parm
   *              1 --> record format ID (34)
   *              2 --> record format length (35,36)
   *              10--> record format name (37-46)
   *              1 --> member number ID (47)
   *              2 --> member number length (48,49)
   *              2 --> member number value (50,51)
   *              1 --> number of fields ID (52)
   *              2 --> number of fields length (53,54)
   *              4 --> number of fields value (55-58)
   *              ? --> trio of ID, length, and value for each field (?)
   *              1 --> control list end (?)
   *       ---
   *       60 + length of the key fields as bytes + 3
   *          --> data stream length
  **/
//  static DDMRequestDataStream getRequestS38GETK(byte[] dclName, RecordFormat recordFormat, int type, int share, int data, Object[] keyFields, AS400 system)  // @A2D
  static DDMRequestDataStream getRequestS38GETK(byte[] dclName,
                                                RecordFormat recordFormat,
                                                byte[] recordFormatCTLLName,
                                                int type,
                                                int share,
                                                int data,
                                                Object[] keyFields,
                                                AS400ImplRemote system)  // @A2A @B5C
    throws AS400SecurityException,
           InterruptedException,
           IOException
  {
    // Set up null key field map
    // Set to true if any field is null
/*@E0D    boolean containsNullKey = false;       //@C2A
    for (int i=0; i<keyFields.length; ++i) //@C2A
    {                                      //@C2A
      if (keyFields[i] == null)            //@C2A
      {                                    //@C2A
        containsNullKey = true;            //@C2A
        break;                             //@C2A
      }                                    //@C2A
    }                                      //@C2A
*/ //@E0D
    
    int reqLength = 60;
    // Determine the total length of all data in keyFields.
    FieldDescription description;
    int keyLength = 0;
    ByteArrayOutputStream keyAsBytes = new ByteArrayOutputStream();
//    Converter conv;    // @A1D
    byte[] fieldAsBytes;  // Will contain each key field's data as bytes
    byte[] lengthBytes = new byte[2]; // Used for variable length fields
    for (int i = 0; i < keyFields.length; i++)
    {
      try
      {
        // Convert each key field to server data writing it to keyAsBytes
        description = recordFormat.getKeyFieldDescription(i);
        
        // Check if field is a variable length field.  This means that the field
        // is either a hex field or a character (DNCS* included) field.
        // The field has the potential of being variable length, but may be
        // fixed length.  We account for both cases in this if.
        if (description instanceof VariableLengthFieldDescription)
        {
          boolean varLength = ((VariableLengthFieldDescription)description).isVariableLength();
          if (description instanceof HexFieldDescription)
          { // Hex field
            byte[] toWrite = (byte[])keyFields[i]; //@D1C
            
            if (varLength)
            {
              // Need to write two bytes of length info prior to writing the data
              BinaryConverter.shortToByteArray((short)toWrite.length, lengthBytes, 0);
              keyAsBytes.write(lengthBytes, 0, lengthBytes.length);
            }
            keyAsBytes.write(toWrite, 0, toWrite.length);
            if (varLength)
            {
              // We need to send the maximum field length number of bytes for the
              // field, even though only keyFields[i].length bytes of the data will
              // be looked at by DDM
              int fieldLength = description.getDataType().getByteLength();
              byte[] b = {0x00};
              for (int j = toWrite.length; j<fieldLength; ++j) //@D1C
              {
                keyAsBytes.write(b, 0, 1);
              }
            }
          }
          else
          { // Character field
            // Use Converter object to translate the key field passed in.  If
            // we use the data type associated with the field description, the data
            // for the key field will be padded with blanks to the byteLength of
            // the field description.  This can cause a match of the key to occur in the
            // case that the user specifies a value that does not exactly match the key
            // field in the record.
            
//@E0D            String toWrite = (String)keyFields[i]; //@D1A
            
/*@E0D            if (varLength)
            {
              // Need to write two bytes of length info prior to writing the
              // data
              BinaryConverter.shortToByteArray((short)toWrite.length(), lengthBytes, 0); //@D1A
              keyAsBytes.write(lengthBytes, 0, lengthBytes.length);
            }
*/ //@E0D
//            conv = Converter.getConverter(((AS400Text)description.getDataType()).getCcsid());  // @A1D
//            fieldAsBytes = conv.stringToByteArray((String)keyFields[i]);                       // @A1D

            // @A1A
            // Modified code to use AS400Text to do the conversion, thus automatically
            // padding the key to the field length.
//@D0D            fieldAsBytes = description.getDataType().toBytes(toWrite);  //@A1A @C2C

//@D0: Can't use the AS400Text object here anymore because it may not have
//     its converter table "filled in" yet, and there's no way to do that 
//     on the remote side since we don't have access to an AS400 object.
//     Go back to using the Converter like before.
//@E0D            ConverterImplRemote conv = ConverterImplRemote.getConverter(system.getCcsid(), system); //@D0A
//@E0D            fieldAsBytes = conv.stringToByteArray((String)keyFields[i]); //@D0A
            
//@E0D            keyAsBytes.write(fieldAsBytes, 0, fieldAsBytes.length);

            // We need to get rid of this if now since AS400Text does the padding for us.
            // Start of @A1D
            /*
            if (varLength)
            {
              // We need to send the maximum field length number of bytes for the
              // field, even though only keyFields[i].length bytes of the data will
              // be looked at by DDM
              int fieldLength = description.getDataType().getByteLength();
              byte[] b = {0x40};
              for (int j = ((String)keyFields[i]).length(); j < fieldLength; ++j)
              {
                keyAsBytes.write(b, 0, 1);
              }
            }
            */
            // End of @A1D
            
            //@D0A: Put the varLength code back in since we can't rely on the
            // AS400Text's converter table being filled in.
/*@E0D            if (varLength)
            {
              int fieldLength = description.getDataType().getByteLength();
              byte[] b = { 0x40 };
              for (int j=((String)keyFields[i]).length(); j<fieldLength; ++j)
              {
                keyAsBytes.write(b, 0, 1);
              }
            }
*/ //@E0D


//@E0A
            String toWrite = (String)keyFields[i]; // Java String we want to write to the data stream
            AS400Text text = (AS400Text)description.getDataType();
            int fieldLength = text.getByteLength(); // How many bytes is the AS400Text object's size
            int textCcsid = text.getCcsid();
            ConverterImplRemote conv = null; // Need a converter
            if (textCcsid != 65535)
            {
              conv = ConverterImplRemote.getConverter(textCcsid, system);
            }
            else
            {
              conv = ConverterImplRemote.getConverter(system.getCcsid(), system);
            }
            
            // Note: We always write the entire field out to its full length, even if
            // it is variable length. If it is variable length, it is marked as such and
            // the database reads the 2-byte length and will ignore the remaining padding
            // characters in the datastream, but nevertheless, they need to be there in
            // order for subsequent key fields in the datastream to work.
            text.setConverter(conv); // Set the converter into the AS400Text object (side effect of proxification)
            fieldAsBytes = text.toBytes(toWrite); // Convert the Java String to IBM i EBCDIC bytes using AS400Text, which will blank pad to the length of the field for us.

            if (varLength) // If we are variable length, need to write the 2-byte length header
            {
              BinaryConverter.shortToByteArray((short)fieldAsBytes.length, lengthBytes, 0); // The length in bytes of the unpadded converted data
              keyAsBytes.write(lengthBytes, 0, lengthBytes.length); // Write the length
            } // If we are not variable length, we don't need to write the 2-byte length header.

            keyAsBytes.write(fieldAsBytes, 0, fieldAsBytes.length); // Write the entire field out to the data stream

          }
        }
        else
        {
          // Numeric field
          // If the field is null, use the default value
          fieldAsBytes = description.getDataType().toBytes(keyFields[i]);
          keyAsBytes.write(fieldAsBytes, 0, fieldAsBytes.length);
        }
      }
      catch(NullPointerException e)
      {
        // One of the key fields was null
        throw new ExtendedIllegalArgumentException("key", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      }
    }
    keyLength = keyAsBytes.size();
    reqLength += keyLength + 3; // account for ID and length bytes when specifying
                                // the key parm on the control list.

    // Instantiate a DDM request stream.
    DDMRequestDataStream req = new DDMRequestDataStream(reqLength);

    req.set16bit(reqLength - 6, 6); // total length after header
    req.set16bit(DDMTerm.S38GETK, 8);

    // Set the DCLNAM LL, CP, and parm.
    req.set16bit(12, 10);
    req.set16bit(DDMTerm.DCLNAM, 12);
    System.arraycopy(dclName, 0, req.data_, 14, 8);

    // Set the S38OPTL LL, CP, and parm.
    req.set16bit(8, 22);
    req.set16bit(DDMTerm.S38OPTL, 24);
    req.data_[26] = (byte)type;
    req.data_[27] = (byte)share;
    req.data_[28] = (byte)data;
    req.data_[29] = 3; // _OPER_GETK

    // Set the S38CTLL CP and parm.  The control list sequence for
    // GETK parm is record format, member number, number of fields, key
    // fields, and control list end.
    req.set16bit(DDMTerm.S38CTLL, 32);
    req.data_[34] = 1; // record format ID
    req.set16bit(10, 35); // record format length

    // Start of @A2D
    /*
    StringBuffer recordName = new StringBuffer(recordFormat.getName());
    while (recordName.length() < 10) recordName.append(' ');
    Converter c = Converter.getConverter(system.getCcsid(), system);
    c.stringToByteArray(recordName.toString(), req.data_, 37);
    */
    // End of @A2D

    // @A2A
    System.arraycopy(recordFormatCTLLName, 0, req.data_, 37, recordFormatCTLLName.length);

    req.data_[47] = 0xf; // member number ID
    req.set16bit(2, 48); // member number length
    req.set16bit(0, 50); // member number value
    req.data_[52] = 8; // number of fields ID
    req.set16bit(4, 53); // number of fields length
    req.set32bit(keyFields.length, 55); // number of fields value

    // Add each key field to the request.
    int offset = 59;
    AS400DataType dataType;
    // Add the ID and length for this key field.
    req.data_[offset++] = 7;
    req.set16bit(keyLength, offset);
    offset += 2;
    System.arraycopy(keyAsBytes.toByteArray(), 0, req.data_, offset, keyLength);
    offset += keyLength;
    
    // Mark the end of control list.
    req.data_[offset] = (byte) 0xff;

    // Set the S38CTLL LL.
    req.set16bit(offset - 29, 30);
    if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "GETK req: ", req.data_);
    }
    return req;
  }


  /**
   *Returns the S38GETK request data stream.
   *@param dclName the declared file name for the file.
   *@param recordFormat the record format for the file.
   *@param type type option
   *@param share share option
   *@param data data option
   *@param keyFields the byte array that contains the byte values of fields of the key
   *@param system the system from which to get the CCSID for conversions.
   *@param numberOfKeyFields The number of key fields contained in the byte array <i>keyFields</i>.
   *@return DDMRequestDataStream for retrieving a record.
   * Term = S38GETK
   * Parms = DCLNAM => byte[8] containing declared name (alias)
   *                   for the file. The first byte must be >= 0x40.
   * Size = 6 --> Header (0-5)
   *        2 --> LL S38GETK term and parms (6,7)
   *        2 --> CP S38GETK code point (8,9)
   *        2 --> LL DCLNAM term and parm length (10,11)
   *        2 --> CP DCLNAM code point (12,13)
   *        8 --> DCLNAM parm (14-21)
   *        2 --> LL S38OPTL term and parm length (22,23)
   *        2 --> CP S38OPTL code point (24,25)
   *        4 --> S38OPTL parm
   *              1 --> type (26)
   *              1 --> share (27)
   *              1 --> data (28)
   *              1 --> operation (29)
   *        2 --> LL S38CTLL term and parm length (30,31)
   *        2 --> CP S38CTLL code point (32,33)
   *        ? --> S38CTLL parm
   *              1 --> record format ID (34)
   *              2 --> record format length (35,36)
   *              10--> record format name (37-46)
   *              1 --> member number ID (47)
   *              2 --> member number length (48,49)
   *              2 --> member number value (50,51)
   *              1 --> number of fields ID (52)
   *              2 --> number of fields length (53,54)
   *              4 --> number of fields value (55-58)
   *              ? --> trio of ID, length, and value for each field (?)
   *              1 --> control list end (?)
   *       ---
   *       60 + length of the key fields as bytes + 3
   *          --> data stream length
  **/
//  static DDMRequestDataStream getRequestS38GETK(byte[] dclName, RecordFormat recordFormat, int type, int share, int data, byte[] keyFields, AS400 system, int numberOfKeyFields)  // @A2D
  static DDMRequestDataStream getRequestS38GETK(byte[] dclName,
                                                byte[] recordFormatCTLLName,
                                                int type,
                                                int share,
                                                int data,
                                                byte[] keyFields,
                                                AS400ImplRemote system, //@B5C
                                                int numberOfKeyFields)  // @A2A
    throws AS400SecurityException,
           InterruptedException,
           IOException
  {
    int reqLength = 60;
    // Determine the total length of all data in keyFields.
    FieldDescription description;
    int keyLength = keyFields.length;
    reqLength += keyLength + 3; // account for ID and length bytes when specifying
                                // the key parm on the control list.

    // Instantiate a DDM request stream.
    DDMRequestDataStream req = new DDMRequestDataStream(reqLength);

    req.set16bit(reqLength - 6, 6); // total length after header
    req.set16bit(DDMTerm.S38GETK, 8);

    // Set the DCLNAM LL, CP, and parm.
    req.set16bit(12, 10);
    req.set16bit(DDMTerm.DCLNAM, 12);
    System.arraycopy(dclName, 0, req.data_, 14, 8);

    // Set the S38OPTL LL, CP, and parm.
    req.set16bit(8, 22);
    req.set16bit(DDMTerm.S38OPTL, 24);
    req.data_[26] = (byte)type;
    req.data_[27] = (byte)share;
    req.data_[28] = (byte)data;
    req.data_[29] = 3; // _OPER_GETK

    // Set the S38CTLL CP and parm.  The control list sequence for
    // GETK parm is record format, member number, number of fields, key
    // fields, and control list end.
    req.set16bit(DDMTerm.S38CTLL, 32);
    req.data_[34] = 1; // record format ID
    req.set16bit(10, 35); // record format length

    // Start of @A2D
    /*
    StringBuffer recordName = new StringBuffer(recordFormat.getName());
    while (recordName.length() < 10) recordName.append(' ');
    Converter c = Converter.getConverter(system.getCcsid(), system);
    c.stringToByteArray(recordName.toString(), req.data_, 37);
    */
    // End of @A2D

    // @A2A
    System.arraycopy(recordFormatCTLLName, 0, req.data_, 37, recordFormatCTLLName.length);

    req.data_[47] = 0xf; // member number ID
    req.set16bit(2, 48); // member number length
    req.set16bit(0, 50); // member number value
    req.data_[52] = 8; // number of fields ID
    req.set16bit(4, 53); // number of fields length
    req.set32bit(numberOfKeyFields, 55); // number of fields value

    // Add key fields to the request.
    int offset = 59;
    AS400DataType dataType;
    // Add the ID and length for this key field.
    req.data_[offset++] = 7;
    req.set16bit(keyLength, offset);
    offset += 2;
    System.arraycopy(keyFields, 0, req.data_, offset, keyLength);
    offset += keyLength;
    // Mark the end of control list.
    req.data_[offset] = (byte) 0xff;

    // Set the S38CTLL LL.
    req.set16bit(offset - 29, 30);
    if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "GETK req: ", req.data_);
    }
    return req;
  }


  /**
   *Returns the S38GETM request data stream.
   *@param dclName the declared file name for the file.
   *@param type type option
   *@param share share option
   *@param data data option
   *@param oper operation option
   *@return DDMRequestDataStream for retrieving a record.
   * Term = S38GETM
   * Parms = DCLNAM => byte[8] containing declared name (alias)
   *                   for the file. The first byte must be >= 0x40.
   * Size = 6 --> Header (0-5)
   *        2 --> LL S38GETM term and parms (6,7)
   *        2 --> CP S38GETM code point (8,9)
   *        2 --> LL DCLNAM term and parm length (10,11)
   *        2 --> CP DCLNAM code point (12,13)
   *        8 --> DCLNAM parm (14-21)
   *        2 --> LL S38OPTL term and parm length (22,23)
   *        2 --> CP S38OPTL code point (24,25)
   *        4 --> S38OPTL parm
   *              1 --> type (26)
   *              1 --> share (27)
   *              1 --> data (28)
   *              1 --> operation (29)
   *        2 --> LL S38CTLL term and parm length (30,31)
   *        2 --> CP S38CTLL code point (32,33)
   *        5 --> S38CTLL parm
   *              1 --> number of records ID (34)
   *              2 --> number records value length (35,36)
   *              2 --> number of records value (37,38)
   *        1 --> control list end (39)
   *
   *       ---
   *       40 --> data stream length
  **/
  static DDMRequestDataStream getRequestS38GETM(byte[] dclName,
                                                int    numberOfRecords,
                                                int    type,
                                                int    share,
                                                int    data,
                                                int    oper)
  {
    DDMRequestDataStream req = new DDMRequestDataStream(40);

    req.set16bit(req.data_.length - 6, 6); // total length after header
    req.set16bit(DDMTerm.S38GETM, 8);

    // Set the DCLNAM LL, CP, and parm.
    req.set16bit(12, 10);
    req.set16bit(DDMTerm.DCLNAM, 12);
    System.arraycopy(dclName, 0, req.data_, 14, 8);

    // Set the S38OPTL LL, CP, and parm.
    req.set16bit(8, 22);
    req.set16bit(DDMTerm.S38OPTL, 24);
    req.data_[26] = (byte) type;
    req.data_[27] = (byte) share;
    req.data_[28] = (byte) data;
    req.data_[29] = (byte) oper;

    // Set the S38CTLL LL, CP, and parm.  The control list sequence for
    // GETD parm is record format, member number, relative record number,
    // and control list end.
    req.set16bit(10, 30);
    req.set16bit(DDMTerm.S38CTLL, 32);
    req.data_[34] = 0x10; // number of records ID
    req.set16bit(2, 35); // number of records length
    req.set16bit(numberOfRecords, 37); // number of records value
    req.data_[39] = (byte) 0xff; // control list end
    return req;
  }

  /**
   *Returns the open file data stream request.
   *@param ufcb the user file control block byte array.
   *@param dclName the declared file name for the file.
   *@return DDMRequestDataStream for opening file.
   *Open file request:
   * Term = S38OPEN
   * Parms = DCLNAM => byte[8] containing declared name (alias)
   *                   for the file. The first byte must be >= 0x40.
   *         S38UFCB => byte string containing the user file control block
   *                    format.  This format is defined by XPF PLMIINC
   *                    WWUFCB.
   * Size = 6 --> Header (0 - 5)
   *        2 --> LL Length of S38OPEN term and parms (6,7)
   *        2 --> CP S38OPEN code point (8,9)
   *        2 --> LL Length of DCLNAM term and parm (10,11)
   *        2 --> CP DCLNAM code point (12,13)
   *        8 --> DCLNAM parm (14 - 21)
   *        2 --> LL Length of S38UFCB term and parm (22,23)
   *        2 --> CP S38UFCB code point (23,24)
   *        ufcb.length
   *          --> S38UFCB parm (25 - 25 + ufcb.length - 1)
   *      -----
   *       26 + ufcb.length --> Total length of the data stream
  **/
  static DDMRequestDataStream getRequestS38OPEN(byte[] ufcb, byte[] dclName)
  {
    // The total length of this data stream is 26 + ufcb.length
    DDMRequestDataStream req = new DDMRequestDataStream(26 + ufcb.length);

    req.set16bit(20 + ufcb.length, 6); // Set total length remaining after header
    req.set16bit(DDMTerm.S38OPEN, 8);   // Set code point for S38OPEN term
    req.set16bit(12, 10);         // Set length of DCLNAM parm
    req.set16bit(DDMTerm.DCLNAM, 12);   // Set code point for DCLNAM parm
                                  // Set the declared name; declaredName is an 8-byte array
    System.arraycopy(dclName, 0, req.data_, 14, 8);
    req.set16bit(4 + ufcb.length, 22); // Set length of S38UFCB parm
    req.set16bit(DDMTerm.S38UFCB, 24);  // Set code point for S38UFCB
    System.arraycopy(ufcb, 0, req.data_, 26, ufcb.length);

    return req;
  }


  /**
   *Returns the S38PUTM request data stream.
   *@param dclName the declared file name.
   *@return DDMRequestDataStream to put multiple records.
   * Term = S38PUTM
   * Parms = DCLNAM => byte[8] containing declared name (alias)
   *                   for the file. The first byte must be >= 0x40.
   * Size = 6 --> Header (0-5)
   *        2 --> LL S38PUT term and parms (6,7)
   *        2 --> CP S38PUT code point (8,9)
   *        2 --> LL DCLNAM term and parm length (10,11)
   *        2 --> CP DCLNAM code point (12,13)
   *        8 --> DCLNAM parm (14-21)
   *        --------------------------
   *        22--> total length
  **/
  static DDMRequestDataStream getRequestS38PUTM(byte[] dclName)
  {
    DDMRequestDataStream req = new DDMRequestDataStream(22);

    req.set16bit(req.data_.length - 6, 6); // total length after header
    req.set16bit(DDMTerm.S38PUTM, 8);

    // Set the DCLNAM LL, CP, and parm.
    req.set16bit(12, 10);
    req.set16bit(DDMTerm.DCLNAM, 12);
    System.arraycopy(dclName, 0, req.data_, 14, 8);

    return req;
  }



//  /**
//   *Returns the SXXPUTDR request data stream.
//   *@param dclName the declared file name for the file.
//   *@param recordFormat the record format for the file.
//   *@param type type option
//   *@param share share option
//   *@param data data option
//   *@param rrn relative record number
//   *@param system the system from which to get the CCSID for conversions.
//   *@return DDMRequestDataStream for putting a record by record number.
//   * Term = SXXPUTDR
//   * Parms = DCLNAM => byte[8] containing declared name (alias)
//   *                   for the file. The first byte must be >= 0x40.
//   * Size = 6 --> Header (0-5)
//   *        2 --> LL SXXPUTDR term and parms (6,7)
//   *        2 --> CP SXXPUTDR code point (8,9)
//   *        2 --> LL DCLNAM term and parm length (10,11)
//   *        2 --> CP DCLNAM code point (12,13)
//   *        8 --> DCLNAM parm (14-21)
//   *        2 --> LL S38CTLL term and parm length (22,23)
//   *        2 --> CP S38CTLL code point (24,25)
//   *        26--> S38CTLL parm
//   *              1 --> record format ID (26)
//   *              2 --> record format length (27,28)
//   *              10--> record format name (29-38)
//   *              1 --> member number ID (39)
//   *              2 --> member number length (40,41)
//   *              2 --> member number value (42,43)
//   *              1 --> relative record number ID (44)
//   *              2 --> relative record number length (45,46)
//   *              4 --> relative record number (47-50)
//   *              1 --> control list end (51)
//   *        2 --> LL S38OPTL term and parm length (52,53)
//   *        2 --> CP S38OPTL code point (54,55)
//   *        4 --> S38OPTL parm
//   *              1 --> type (56)
//   *              1 --> share (57)
//   *              1 --> data (58)
//   *              1 --> operation (59)
//   *        2 --> LL S38BUF term and parm length (60,61)
//   *        2 --> CP S38BUF code point (62,63)
//   *        recordData.length
//   *          --> S38BUF parm (64 - 64 + openFeedback.getRecordIncrement())
//   *
//   *       ---
//   *       64 + openFeedback.getRecordIncrement() --> data stream length
//   *@exception CharConversionException If an error occurs during conversion.
//   *@exception UnsupportedEncodingException If an error occurs during conversion.
//  **/
//  /* Currently not used.
//  static DDMRequestDataStream getRequestSXXPUTDR(byte[] dclName,
//                                                 DDMS38OpenFeedback openFeedback,
//                                                 Record record,
//                                                 int rrn,
//                                                 AS400 system)
//    throws CharConversionException,
//           UnsupportedEncodingException
//  {
//    DDMRequestDataStream req =
//      new DDMRequestDataStream(64 + openFeedback.getRecordIncrement());
//
//    req.set16bit(req.data_.length - 6, 6); // total length after header
//    req.set16bit(DDMTerm.SXXPUTDR, 8);
//
//    // Set the DCLNAM LL, CP, and parm.
//    req.set16bit(12, 10);
//    req.set16bit(DDMTerm.DCLNAM, 12);
//    System.arraycopy(dclName, 0, req.data_, 14, 8);
//
//    // Set the S38CTLL LL, CP, and parm.  The control list sequence for
//    // PUTDR parm is record format, member number, relative record number,
//    // and control list end.
//    RecordFormat recordFormat = record.getRecordFormat();
//    req.set16bit(30, 22);
//    req.set16bit(DDMTerm.S38CTLL, 24);
//    req.data_[36] = 1; // record format ID
//    req.set16bit(10, 27); // record format length
//    StringBuffer recordName = new StringBuffer(recordFormat.getName());
//    while (recordName.length() < 10) recordName.append(' ');
//    Converter c = Converter.getConverter(system.getCcsid(), system);
//    c.stringToByteArray(recordName.toString(), req.data_, 29);
//    req.data_[39] = 0xf; // member number ID
//    req.set16bit(2, 40); // member number length
//    req.set16bit(0, 50); // member number value
//    req.data_[44] = 2; // relative record number ID
//    req.set16bit(4, 45); // relative record number length
//    req.set32bit(rrn, 47); // relative record number value
//    req.data_[51] = (byte) 0xff; // control list end
//
//    // Set the S38OPTL LL, CP, and parm.
//    req.set16bit(8, 52);
//    req.set16bit(DDMTerm.S38OPTL, 54);
//    req.data_[56] = 0x11;
//    req.data_[57] = 0; // not used, must be zero
//    req.data_[58] = 0; // not used, must be zero
//    req.data_[59] = 4; //
//
//    // Set the S38BUF LL, CP.
//    req.set16bit(openFeedback.getRecordIncrement() + 4, 60);
//    req.set16bit(DDMTerm.S38BUF, 62);
//
//    // Write the record data.
//    byte[] recordData = record.getContents();
//    System.arraycopy(recordData, 0, req.data_, 64, recordData.length);
//
//    // Write the null byte map array after the record data.  It starts
//    // openFeedback.getNullFieldByteMapOffset() bytes from the
//    // beginning of the record. 0xf1 == null, 0xf0 != null
//    int byteMapOffset = 64 + openFeedback.getNullFieldByteMapOffset();
//    for (int i = 0; i < record.getNumberOfFields(); i++)
//    {
//      req.data_[byteMapOffset + i] =
//        (record.isNullField(i) ? (byte) 0xf1 : (byte) 0xf0);
//    }
//
//    if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
//    {
//      Trace.log(Trace.DIAGNOSTIC, "getRequestS38PUTDR", req.data_);
//    }
//
//    return req;
//  }
// */


//@B1D: Removed this method since readAll() no longer uses it.
//  /**
//   *Returns the unload all records data stream request.
//   *@param fileType the type of access, SEQ or KEY.
//   *@param library the library name.
//   *@param file the file name.
//   *@param member the member name.
//   *@param system the AS400object from which to get the CCSID for conversions.
//   *@return DDMRequestDataStream for reading all records from a file.
//   *ULDRECF:
//   * Term = ULDRECF
//   * Parms = FILNAM => File name
//   *            2 --> LL Length of FILNAM term and parm
//   *            2 --> CP FILNAM code point
//   *            library.length() + file.length() + member.length() + 3
//   *              --> FILNAM parm, library/file(member)
//   *         ACCORD => Access order - valid values: RNBORD or KEYORD
//   *
//   * Size = 6 --> Header (0 - 5)
//   *        2 --> LL Length of ULDRECF term and parms
//   *        2 --> CP ULDRECF code point
//   *        2 --> LL Length of FILNAM term and parm
//   *        2 --> CP FILNAM code point
//   *            library.length() + file.length() + member.length() + 3
//   *              --> FILNAM parm, library/file(member)
//   *        2 --> LL Length of RTNINA term and parms
//   *        2 --> CP RTNINA code point
//   *        1 --> 'F0' == false
//   *        If fileType = "key":
//   *          2 --> LL Length of ACCORD term and parm
//   *          2 --> CP ACCORD code point
//   *          2 --> RNBORD or KEYORD code point
//   *      -----
//   *       25|19 + library.length() + file.length() + member.length() + 3
//  **/
// //@B1D: start block
// /*
//  static DDMRequestDataStream getRequestULDRECF(String fileType, String library,
//                                                String file, String member, AS400 system)
//    throws AS400SecurityException,
//           InterruptedException,
//           IOException
//  {
//    int dsLength = (fileType.equalsIgnoreCase("key"))? 25 : 19;
//    int fileNameLength =  library.length() + file.length() + member.length() + 3;
//    DDMRequestDataStream req = new DDMRequestDataStream(dsLength + fileNameLength);
//
//    req.set16bit(dsLength + fileNameLength - 6, 6); // Set length after header
//    req.set16bit(DDMTerm.ULDRECF, 8); // Set code point for ULDRECF
//    req.set16bit(fileNameLength + 4, 10); // Set length for FILNAM parm
//    req.set16bit(DDMTerm.FILNAM, 12); // Set code point for FILNAM
//    // Set the file name parm
//    Converter c = Converter.getConverter(system.getCcsid(), system);
//    c.stringToByteArray(library + "/" + file + "(" + member + ")", req.data_, 14);
//    req.set16bit(5, 14 + fileNameLength); // Set LL for RTNINA
//    req.set16bit(DDMTerm.RTNINA, 16 + fileNameLength); // Set RTNINA code point
//    req.data_[18 + fileNameLength] = (byte)0xF0;  // false; don't return inactive records
//    // Set ACCORD parm value
//    if (dsLength == 25)
//    {
//      req.set16bit(6, 19 + fileNameLength); // Set length of ACCORD parm
//      req.set16bit(DDMTerm.ACCORD, 21 + fileNameLength); // Set code point for ACCORD
//      req.set16bit(DDMTerm.KEYORD, 23 + fileNameLength);
//    }
//    return req;
//  }
//*/
//@B1D: end block

  /**
   *Returns the S38UPDAT request data stream.
   *@param type type option
   *@param share share option
   *@param data data option
   *@param dclName the declared file name.
   *@return DDMRequestDataStream to put a record.
   * Term = S38UPDAT
   * Parms = DCLNAM => byte[8] containing declared name (alias)
   *                   for the file. The first byte must be >= 0x40.
   * Size = 6 --> Header (0-5)
   *        2 --> LL S38UPDAT term and parms (6,7)
   *        2 --> CP S38UPDAT code point (8,9)
   *        2 --> LL DCLNAM term and parm length (10,11)
   *        2 --> CP DCLNAM code point (12,13)
   *        8 --> DCLNAM parm (14-21)
   *        2 --> LL S38OPTL term and parm length (22,23)
   *        2 --> CP S38OPTL code point (24,25)
   *        4 --> S38OPTL parm
   *              1 --> type (26)
   *              1 --> share (27)
   *              1 --> data (28)
   *              1 --> operation (29)
   *      ----
   *       30 --> total request length
   **/
  static DDMRequestDataStream getRequestS38UPDAT(int type,
                                                 int share,
                                                 int data,
                                                 byte[] dclName)
  {
    DDMRequestDataStream req = new DDMRequestDataStream(30);

    req.set16bit(req.data_.length - 6, 6); // total length after header
    req.set16bit(DDMTerm.S38UPDAT, 8);

    // Set the DCLNAM LL, CP, and parm.
    req.set16bit(12, 10);
    req.set16bit(DDMTerm.DCLNAM, 12);
    System.arraycopy(dclName, 0, req.data_, 14, 8);

    // Set the S38OPTL LL, CP, and parm.
    req.set16bit(8, 22);
    req.set16bit(DDMTerm.S38OPTL, 24);
    req.data_[26] = (byte) type;
    req.data_[27] = (byte) share;
    req.data_[28] = (byte) data;
    req.data_[29] = 7; // update operation

    if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "getRequestS38UPDAT", req.data_);
    }

    return req;
  }


}
