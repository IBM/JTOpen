///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DDMS38IOFB.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 *Class representing the S38IOFB data returned for the S38IOFB DDM term when
 *a I/O operation is done on a database file.
**/
class DDMS38IOFB
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private static final int RECORD_FORMAT = 0;  // Offset to the record format name
  private static final int RECORD_FORMAT_LENGTH = 10; // Length of the record format name
  private static final int RECORD_LEN = 10;  // Offset to the record length (4 bytes)
  private static final int NUMBER_OF_RECORDS = 14; // Offset to the number of records returned
  private static final int NUMBER_OF_RECORDS_LOCKED = 17; // Offset to the number of records locked
  private static final int RRN_MOST_RECENTLY_LOCKED = 19; // Offset to the relative record number of the most recently locked record (4 bytes)
  private static final int MEMBER_LOCKED = 23; // Offset to the member number of the most recently locked member
  private static final int RECORD_NUMBER = 55; // Offset to record number of the record read
  // Array containing IO feedback information
  protected byte[] data_;
  // Offset in data_ at which the IO feedback info starts
  protected int offset_;

  /**
   *Constructs a DDMS38IOFB object from the specified data starting at the
   *specified offset in data.
   *@param data the data from which to extract the S38IOFB info.
   *@param offset the offset in data at which the S38IOFB info starts.
  **/
  DDMS38IOFB(byte[] data, int offset)
  {
    // Set the instance data
    // The getters will extract the information directly from data_ based on the
    // defined constants and offset_.
    // Both data and offset are expected to be valid and will not be checked here.
    data_ = data;
    offset_ = offset;
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

  /**
   *Returns the information provided in the S38IOFB as a string.  This method is
   *used for logging S38IOFB information. 
   *@return the S38IOFB as a string.
  **/
  public String toString()
  {
    StringBuffer str = new StringBuffer();
    str.append("Number of records returned: ");
    str.append((new Integer(getNumberOfRecordsReturned())).toString());
    str.append("\n");

    str.append("Record length: ");
    str.append((new Integer(getRecordLength())).toString());
    str.append("\n");

    return str.toString();
  }
}
