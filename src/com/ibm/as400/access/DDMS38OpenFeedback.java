///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DDMS38OpenFeedback.java
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
 *Class representing the open feedback structure.  The structure
 *is described by CINC WWODPOFB.
**/
class DDMS38OpenFeedback
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  // offsets and byte lengths of the various portions of the feedback structure
  // that we are interested in
  private static final int FILE_OPEN_TYPE = 10;
  private static final int FILE_NAME = 11;
  private static final int FILE_NAME_LENGTH = 10;
  private static final int LIB_NAME = 21;
  private static final int LIB_NAME_LENGTH = 10;
  private static final int MBR_NAME = 31;
  private static final int MBR_NAME_LENGTH = 10;
  private static final int RECORD_LEN = 41;
  private static final int INP_BUFFER_LEN = 45;
  private static final int OUT_BUFFER_LEN = 49;
  private static final int NUM_RECORDS = 53;
  private static final int ACCESS_TYPE = 57;
  private static final int ACCESS_TYPE_LENGTH = 2;
  private static final int SUPPORT_DUPLICATE_KEYS = 59;
  private static final int SUPPORT_DUPLICATE_KEYS_LENGTH = 1;
  private static final int SOURCE_FILE_INDICATOR = 60;
  private static final int SOURCE_FILE_INDICATOR_LENGTH = 1;
  private static final int UFCB_PARAMETERS = 61;
  private static final int UFCB_PARAMETERS_LENGTH = 10;
  private static final int MAX_BLOCKED_RECORDS_TRANSFERRED = 71;
  private static final int RECORD_INCREMENT = 73;
  private static final int OPEN_FLAGS_1= 75;   // Contains info on if MBR(*ALL) was specified, if this
                                       // is a logical join file, etc.
  private static final int NUMBER_OF_ASSOCIATED_PHYS_FILE_MBRS = 76;
  private static final int MAX_RECORD_LENGTH = 82;
  private static final int RECORD_WAIT_TIME = 84;
  private static final int OPEN_FLAGS_2 = 88;  // Contains info indicating if file contains variable
                                       // length fields, whether variable length processing will
                                       // be done, ODP scope, etc.
  private static final int NULL_FIELD_BYTE_MAP = 90;
  private static final int NULL_KEY_FIELD_BYTE_MAP = 92;
  private static final int CCSID = 98;
  private static final int FIXED_FIELD_LEN = 100; // Total length of the fixed fields in a variable length
                                         // record.
  private static final int MIN_RECORD_LEN = 102;

  // The openfeedback data
  byte[] data_;
  // The offset into the data.
  int offset_;
  // Converter for EBCDIC to ASCII conversions
  ConverterImplRemote conv_; //@B5C

  /**
   *Construct a DDMS38OpenFeedback object from the specified data.
   *@param system the AS400 object from which to get the CCSID for converions.
   *@param data the data from which to obtain the open feedback information.
  **/
  DDMS38OpenFeedback(AS400ImplRemote system, byte[] data) //@B5C
    throws AS400SecurityException,
           InterruptedException,
           IOException
  {
    this(system, data, 0);
  }

  /**
   *Construct a DDMS38OpenFeedback object from the specified data.
   *@param system the AS400 object from which to get the CCSID for converions.
   *@param data the data from which to obtain the open feedback information.
  **/
  DDMS38OpenFeedback(AS400ImplRemote system, byte[] data, int offset) //@B5C
    throws AS400SecurityException,
           InterruptedException,
           IOException
  {
    data_ = data;
    offset_ = offset;
    conv_ = ConverterImplRemote.getConverter(system.getCcsid(), system); //@B5C
  }

  /**
   *Returns the access type of the file.
   *@return the access type if the the file.
  **/
  String getAccessType()
    throws AS400SecurityException,
           InterruptedException,
           IOException
  {
    return conv_.byteArrayToString(data_, offset_ + ACCESS_TYPE,
                                   ACCESS_TYPE_LENGTH);
  }

  /**
   *Returns the CCSID of the file.
   *@return the CCSID of the file.
  **/
  int getCCSID()
  {
    return BinaryConverter.byteArrayToUnsignedShort(data_, offset_ + CCSID);
  }

  /**
   *Indicates if duplicate keys are supported by the file.
   *@return true if duplicate keys are supported; false otherwise.
  **/
  boolean duplicateKeysSupported()
  {
    if (data_[offset_ + SUPPORT_DUPLICATE_KEYS] == (byte)0xC4)
    {
      return true;
    }
    return false;
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
   *Returns the input buffer length for the file.
   *@return the input buffer length.
  **/
  int getInputBufferLength()
  {
    return BinaryConverter.byteArrayToInt(data_, offset_ + INP_BUFFER_LEN);
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
    return conv_.byteArrayToString(data_, offset_ + LIB_NAME, LIB_NAME_LENGTH);
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
   *Returns the type of open for which the file is opened.
   *@return the type of open for which the file is opened.
  **/
  int getOpenType()
  {
    int type = data_[offset_ + FILE_OPEN_TYPE] & 0xFFFF;
    if (type == 64 || type == 68 || type == 192 || type == 196)
    {
      return AS400FileConstants.READ_ONLY; //@B0C
    }
    else if (type == 32 || type == 36 || type == 160 || type == 164)
    {
      return AS400FileConstants.WRITE_ONLY; //@B0C
    }
    else
    {
      return AS400FileConstants.READ_WRITE; //@B0C
    }
  }

  /**
   *Returns the output buffer length for the file.
   *@return the output buffer length.
  **/
  int getOutputBufferLength()
  {
    return BinaryConverter.byteArrayToInt(data_, offset_ + OUT_BUFFER_LEN);
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
   *Returns the record length of the records in the file.
   *@return the record length of the records in the file.
  **/
  int getRecordLength()
  {
    return BinaryConverter.byteArrayToUnsignedShort(data_, offset_ + RECORD_LEN);
  }

  /**
   *Indicates if the file contains variable length fields.
   *@return true if the file contains variable length fields; false otherwise.
  **/
  boolean hasVariableLengthFields()
  {
    if ((data_[offset_ + OPEN_FLAGS_2 + 1] & 0x80) == 0x80)
    {
      return true;
    }
    return false;
  }

  /**
   *Indicates if the file is a logical file.
   *@return true if the file is a logical file; false otherwise.
  **/
  boolean isLogicalFile()
  {
    if ((data_[offset_ + OPEN_FLAGS_1] & 0x80) == 0x80)
    {
      return true;
    }
    return false;
  }

  /**
   *Indicates if the file is a logical join file.
   *@return true if the file is a logical join file; false otherwise.
  **/
  boolean isLogicalJoinFile()
  {
    if ((data_[offset_ + OPEN_FLAGS_1] & 0x04) == 0x04)
    {
      return true;
    }
    return false;
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

  /**
   *Indicates if the file is a source file.
   *@return true if the file is a source file; false otherwise.
  **/
  boolean isSourceFile()
  {
    return (data_[offset_ + SOURCE_FILE_INDICATOR] == (byte)0xE8);
  }

  /**
   *Returns the contents of the openfeedback area as a string.  Used for
   *logging diagnostic information regarding the open of a file.
   *@return the contents of the openfeedback area as a string.
  **/
  public String toString()
  {
    StringBuffer str = new StringBuffer(0);
    try
    {
      str.append("Open type: ");
      str.append(getOpenType());
      str.append("\n");

      str.append("Library name: ");
      str.append(getLibraryName());
      str.append("\n");

      str.append("File name: ");
      str.append(getFileName());
      str.append("\n");

      str.append("Member name: ");
      str.append(getMemberName());
      str.append("\n");

      str.append("Record length: ");
      str.append(getRecordLength());
      str.append("\n");

      str.append("Input buffer length: ");
      str.append(getInputBufferLength());
      str.append("\n");

      str.append("Output buffer length: ");
      str.append(getOutputBufferLength());
      str.append("\n");

      str.append("Number of records: ");
      str.append(getNumberOfRecords());
      str.append("\n");

      str.append("Access type: ");
      str.append(getAccessType());
      str.append("\n");

      str.append("Duplicate keys supported: ");
      str.append(duplicateKeysSupported());
      str.append("\n");

      str.append("Source file: ");
      str.append(isSourceFile());
      str.append("\n");

      str.append("Max blocked records transferred: ");
      str.append(getMaxNumberOfRecordsTransferred());
      str.append("\n");

      str.append("Record increment: ");
      str.append(getRecordIncrement());
      str.append("\n");

      str.append("Has variable length fields: ");
      str.append(hasVariableLengthFields());
      str.append("\n");

      str.append("Is null capable: ");
      str.append(isNullCapable());
      str.append("\n");

      str.append("Offset to null byte field map: ");
      str.append(getNullFieldByteMapOffset());
      str.append("\n");

      str.append("Logical file: ");
      str.append(isLogicalFile());
      str.append("\n");

      str.append("Logical join file: ");
      str.append(isLogicalJoinFile());
      str.append("\n");

      str.append("CCSID: ");
      str.append(getCCSID());
      str.append("\n");
    }
    catch(Exception e)
    { // This should never happen; the AS400 object is connected when
      // it is provided to us on the constructor.  Need to shut up the compiler.
      throw new InternalErrorException(InternalErrorException.UNKNOWN);
    }
    return str.toString();
  }
}
