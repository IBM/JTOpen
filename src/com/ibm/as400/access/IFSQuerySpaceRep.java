///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSQuerySpaceRep.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;

/**
Query available file system space reply.
**/
class IFSQuerySpaceRep extends IFSDataStream
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";
  static final long NO_MAX = Long.MAX_VALUE;  // indicates user has no maximum storage limit

  private static final int UNIT_SIZE_OFFSET = 22;
  private static final int TOTAL_SPACE_OFFSET = 26;
  private static final int SPACE_AVAILABLE_OFFSET = 30;

/**
Construct a query available file system space reply.
**/
  IFSQuerySpaceRep()
  {
  }

/**
Generate a new instance of this type.
@return a reference to the new instance
**/
  public Object getNewDataStream()
  {
    return new IFSQuerySpaceRep();
  }

/**
Determine the unused space in the file system (in bytes).
Returns NO_MAX if the user profile has a "maximum storage allowed" setting of *NOMAX.
(The File Server returns a bogus value in the Space Available field in that case.)
@return the number of unused bytes in the file system
**/
  long getFreeSpace()
  {
    // Previously used get32bit(), but get32bit() incorrectly treated the 4 byte field 
    // as a 4-byte signed value.  On large systems, where the most significant bit was
    // set (in the IFSQuerySpaceRep response), we were returning a negative value.
    // BinaryConverter.byteArrayToUnsignedInt() treats the data as unsigned.
    long totalSpace = BinaryConverter.byteArrayToUnsignedInt(data_, TOTAL_SPACE_OFFSET);
    long spaceAvail = BinaryConverter.byteArrayToUnsignedInt(data_, SPACE_AVAILABLE_OFFSET);
    long unitSize = BinaryConverter.byteArrayToUnsignedInt(data_, UNIT_SIZE_OFFSET);

    if (totalSpace == 0x7FFFFFFFL) return NO_MAX;  // special value indicates no maximum storage limit
    else return (unitSize * spaceAvail);
  }

/**
Generates a hash code for this data stream.
@return the hash code
**/
  public int hashCode()
  {
    return 0x8007;
  }
}





