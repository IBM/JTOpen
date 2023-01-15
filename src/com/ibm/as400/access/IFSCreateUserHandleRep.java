///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSCreateUserHandleRep.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2016-2016 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

public class IFSCreateUserHandleRep extends IFSDataStream {
  
  private static final int HANDLE_OFFSET= 24;
  private static final int RETURN_CODE_OFFSET = 22;
  /**
  Get the working directory handle.
  @return the working directory handle.
  **/
    int getHandle()
    {
      return get32bit(HANDLE_OFFSET);
    }

  /**
  Generate a new instance of this type.
  @return a reference to the new instance
  **/
    public Object getNewDataStream()
    {
      return new IFSCreateUserHandleRep();
    }
    
    /**
    Get the return code.
    @return the return code
    **/
    int getReturnCode()
    {
      return get16bit(RETURN_CODE_OFFSET);
    }

  /**
  Generates a hash code for this data stream.
  @return the hash code
  **/
    public int hashCode()
    {
      return 0x8011;
    }
}
