///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSCreateDirHandleRep.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2004-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;



/**
Create Working Directory Handle reply.
**/
class IFSCreateDirHandleRep extends IFSDataStream
{
  private static final String copyright = "Copyright (C) 2004-2004 International Business Machines Corporation and others.";

  private static final int WORKING_DIR_HANDLE_OFFSET = 22;

/**
Get the working directory handle.
@return the working directory handle.
**/
  int getHandle()
  {
    return get32bit(WORKING_DIR_HANDLE_OFFSET);
  }

/**
Generate a new instance of this type.
@return a reference to the new instance
**/
  public Object getNewDataStream()
  {
    return new IFSCreateDirHandleRep();
  }

/**
Generates a hash code for this data stream.
@return the hash code
**/
  public int hashCode()
  {
    return 0x8006;
  }
}




