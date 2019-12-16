///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSCreateUserHandlerReq.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2016-2016 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

public class IFSCreateUserHandlerReq extends IFSDataStreamReq {

  private static final int TEMPLATE_LENGTH = 12;
  private static final int USER_ID_OFFSET = 22;
  private static final int PASSWORD_OFFSET = 32;
  
  protected IFSCreateUserHandlerReq(byte[] userID, byte[] password) {
    super(HEADER_LENGTH + TEMPLATE_LENGTH + password.length);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH + password.length);
    setReqRepID(0x0024);
    
    set32bit(userID.length, USER_ID_OFFSET);
    System.arraycopy(userID, 0, data_, USER_ID_OFFSET, userID.length);
    
    set64bit(password.length, PASSWORD_OFFSET);
    System.arraycopy(password, 0, data_, PASSWORD_OFFSET, password.length);
  }

  
  
}
