///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSUserHandleSeedRep.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2016-2016 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

public class IFSUserHandleSeedRep extends IFSDataStream {

  private static final int CLIENT_SEED_OFFSET = 22;
  
  byte[] getSeed() {
    byte[] ServerSeed = new byte[8];
    System.arraycopy(data_, CLIENT_SEED_OFFSET, ServerSeed, 0, ServerSeed.length);

    return ServerSeed;
  }
  /**
  Generate a new instance of this type.
  @return a reference to the new instance
  **/
    public Object getNewDataStream()
    {
      return new IFSUserHandleSeedRep();
    }
    
    /**
    Generates a hash code for this data stream.
    @return the hash code
    **/
    public int hashCode()
    {
      return 0x8010;
    }
}
