///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSQuerySpaceRep.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
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
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

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
Determine the file system capacity (in bytes).
@return the total number of bytes the file system can store
**/
  long getCapacity()
  {
    return ((long) get32bit( UNIT_SIZE_OFFSET) *
            (long) get32bit( TOTAL_SPACE_OFFSET));
  }

  // Get the copyright.
  private static String getCopyright()
  {
    return Copyright.copyright;
  }

/**
Determine the unused space in the file system (in bytes).
@return the number of unused bytes in the file system
**/
  long getFreeSpace()
  {
    return ((long) get32bit( UNIT_SIZE_OFFSET) *
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





