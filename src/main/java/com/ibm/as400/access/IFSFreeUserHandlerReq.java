///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFreeUserHandlerReq.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2016-2016 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

public class IFSFreeUserHandlerReq extends IFSDataStreamReq {

  private static final int TEMPLATE_LENGTH = 6;
  private static final int USER_HANDLE_OFFSET = 22;
  
  protected IFSFreeUserHandlerReq(int userHandler) {
    super(HEADER_LENGTH + TEMPLATE_LENGTH);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setCSInstance(userHandler);
    setReqRepID(0x0026);
    
    set32bit(userHandler, USER_HANDLE_OFFSET); 
  }
}
