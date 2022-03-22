///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSGetFileSystemRep.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2016-2016 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;



/**
Create Working Directory Handle reply.
**/
class IFSGetFileSystemRep extends IFSDataStream
{
  private static final String copyright = "Copyright (C) 2004-2004 International Business Machines Corporation and others.";

  private static final int FSI_OFFSET = 22;
  
  
/**
Get the working directory handle.
@return the working directory handle.
**/
  int getFileSystemType()
  {
    //byte[] fileSystemType = new byte[4];
    //System.arraycopy(data_, FSI_OFFSET + 6, fileSystemType, 0, fileSystemType.length);

    return get32bit(FSI_OFFSET + 6);
  }

/**
Generate a new instance of this type.
@return a reference to the new instance
**/
  public Object getNewDataStream()
  {
    return new IFSGetFileSystemRep();
  }

/**
Generates a hash code for this data stream.
@return the hash code
**/
  public int hashCode()
  {
    return 0x800F;
  }
}




