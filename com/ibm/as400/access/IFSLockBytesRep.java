///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSLockBytesRep.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
Lock bytes reply.
**/
class IFSLockBytesRep extends IFSDataStream
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

  private static final int RETURN_CODE_OFFSET = 22;

/**
Construct a lock bytes reply.
**/
  IFSLockBytesRep()
  {
  }

/**
Generate a new instance of this type.
@return a reference to the new instance
**/
  public Object getNewDataStream()
  {
    return new IFSLockBytesRep();
  }

/**
Determine the return code.
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
    return 0x800A;
  }
}


