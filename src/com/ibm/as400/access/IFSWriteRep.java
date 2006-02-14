///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSWriteRep.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
Write data reply.
**/
class IFSWriteRep extends IFSDataStream
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

  private static final int RETURN_CODE_OFFSET = 22;
  private static final int PREVIOUS_FILE_SIZE_OFFSET = 24;
  private static final int BYTES_NOT_WRITTEN_OFFSET = 28;

/**
Construct a write reply.
**/
  IFSWriteRep()
  {
  }

/**
Generate a new instance of this type.
@return a reference to the new instance
**/
  public Object getNewDataStream()
  {
    return new IFSWriteRep();
  }

/**
Determine the amount that couldn't be written.
@return the number of bytes that were not written
**/
  int getLengthNotWritten()
  {
    return get32bit( BYTES_NOT_WRITTEN_OFFSET);
  }

/**
Get the return code
@return the return code
**/
  int getReturnCode()
  {
    return get16bit( RETURN_CODE_OFFSET);
  }

/**
Generates a hash code for this data stream.
@return the hash code
**/
  public int hashCode()
  {
    return 0x800b;
  }
}




