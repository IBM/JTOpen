///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: LocalOpenFeedback.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;


/**
 Class representing an open feedback structure supplied by the QYSTRART
 service program.
**/
class LocalOpenFeedback extends DDMS38OpenFeedback
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  // offsets and byte lengths of the various portions of the feedback structure
  // that we are interested in
  private static final int FILE_NAME = 0;
  private static final int FILE_NAME_LENGTH = 10;
  private static final int LIB_NAME = 10;
  private static final int LIB_NAME_LENGTH = 10;
  private static final int MBR_NAME = 20;
  private static final int MBR_NAME_LENGTH = 10;
  private static final int MAX_BLOCKED_RECORDS_TRANSFERRED = 48;
  private static final int NULL_FIELD_BYTE_MAP = 64;
  private static final int NUM_RECORDS = 40;
  private static final int RECORD_LEN = 30;
  private static final int RECORD_INCREMENT = 50;
  private static final int OPEN_FLAGS_2 = 54;


  /**
   Constructs an open feedback area from data supplied by the QYSTRART
   service program's cdmOpen function.
   @param system the system
   @param data the data from cdmOpen
   @exception AS400SecurityException If a security or authority error occurs.
   @exception InterruptedException If this thread is interrupted.
   @exception IOException If an error occurs while communicating with the AS/400.
   **/

  LocalOpenFeedback(AS400ImplRemote system, //@B5C
                    byte[] data,
                    int offset)
    throws AS400SecurityException, InterruptedException, IOException
  {
    super(system, data, offset);
  }


  /**
   *Returns the file name of the file.
   *@return the file name of the file.
  **/
  String getFileName()
    throws AS400SecurityException,
           InterruptedException,
           IOException
  {
    return conv_.byteArrayToString(data_, offset_ + FILE_NAME,
                                   FILE_NAME_LENGTH);
  }

  /**
   *Returns the library name of the file.
   *@return the library name of the file.
  **/
  String getLibraryName()
    throws AS400SecurityException,
           InterruptedException,
           IOException
  {
    return conv_.byteArrayToString(data_, offset_ + LIB_NAME,
                                   LIB_NAME_LENGTH);
  }

  /**
   *Returns the maximum number of records that can read or written at one time.
   *@return the maximum number of records that can be transferred at one time.
  **/
  int getMaxNumberOfRecordsTransferred()
  {
    return BinaryConverter.byteArrayToUnsignedShort(data_, offset_ + MAX_BLOCKED_RECORDS_TRANSFERRED);
  }

  /**
   *Returns the member name.
   *@return the member name.
  **/
  String getMemberName()
    throws AS400SecurityException,
           InterruptedException,
           IOException
  {
    return conv_.byteArrayToString(data_, offset_ + MBR_NAME, MBR_NAME_LENGTH);
  }

  /**
   *Returns the offset to the null field byte map for a record.
   *@return the offset to the null field byte map.
  **/
  int getNullFieldByteMapOffset()
  {
    return BinaryConverter.byteArrayToUnsignedShort(data_, offset_ + NULL_FIELD_BYTE_MAP);
  }

  /**
   *Returns the number of records in the file at open time.
   *@return the number of records in the file at open time.
  **/
  int getNumberOfRecords()
  {
    return BinaryConverter.byteArrayToInt(data_, offset_ + NUM_RECORDS);
  }

  /**
   *Returns the record length of the records in the file.
   *@return the record length of the records in the file.
  **/
  int getRecordLength()
  {
    return BinaryConverter.byteArrayToUnsignedShort(data_, offset_ + RECORD_LEN);
  }

  /**
   *Returns the record increment for the records in the file.  This is the number
   *of bytes that will be returned or that need to be sent for a single record.
   *It includes bytes for the record data, a gap of zero or more bytes and bytes
   *for the null field byte map.
   *@return the record increment.
  **/
  int getRecordIncrement()
  {
    return BinaryConverter.byteArrayToUnsignedShort(data_, offset_ +  RECORD_INCREMENT);
  }

  /**
   *Indicates if the file contains null capable fields.
   *@return true if the file contains null capable fields; false otherwise.
  **/
  boolean isNullCapable()
  {
    if ((data_[offset_ + OPEN_FLAGS_2 + 1] & 0x40) == 0x40)
    {
      return true;
    }
    return false;
  }
}





