///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
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
    long totalSpace = (long)get32bit(TOTAL_SPACE_OFFSET);
    if (totalSpace == 0x7FFFFFFFL) return NO_MAX;  // special value indicates no maximum storage limit
    else return ((long) get32bit( UNIT_SIZE_OFFSET) *
            (long) get32bit( SPACE_AVAILABLE_OFFSET));
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





