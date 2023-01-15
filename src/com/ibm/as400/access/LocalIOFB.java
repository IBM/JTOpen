///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: LocalIOFB.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class LocalIOFB extends DDMS38IOFB
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private static final int NUMBER_OF_RECORDS = 4; // Offset to the number of records returned
  private static final int RECORD_LEN = 0;  // Offset to the record length (4 bytes)
  private static final int RECORD_NUMBER = 6; // Offset to record number of the record read
  private static final int ACTUAL_BYTES_RETURNED = 10; // Offset to total bytes returned (number of records returned * bytes for each record)


  LocalIOFB(byte[] data,
            int offset)
  {
    super(data, offset);
  }

  /**
   *Returns the actual bytes returned from the last I/O operation.
   *@return number of bytes returned.
   **/
  int getActualBytesReturned()
  {
    return BinaryConverter.byteArrayToInt(data_, offset_ +
                                          ACTUAL_BYTES_RETURNED);
  }


  /**
   *Returns the number of records returned from the i/o operation that this
   *object provides feedback for.
   *@return the number of records.
  **/
  int getNumberOfRecordsReturned()
  {
    return BinaryConverter.byteArrayToUnsignedShort(data_, offset_ + NUMBER_OF_RECORDS);
  }

  /**
   *Returns the record length of the records for the i/o operation that this
   *object provides feedback for.
   *@return the record length.
  **/
  int getRecordLength()
  {
    return BinaryConverter.byteArrayToInt(data_, offset_ + RECORD_LEN);
  }

  /**
   *Returns the record number of the record returned from the i/o operation that this
   *object provides feedback for.
   *@return the record number.
  **/
  int getRecordNumber()
  {
    return BinaryConverter.byteArrayToInt(data_, offset_ + RECORD_NUMBER);
  }
}





