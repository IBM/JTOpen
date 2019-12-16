///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSUserHandleSeedReq.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2016-2016 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

public class IFSUserHandleSeedReq extends IFSDataStreamReq {
  
  private static final int TEMPLATE_LENGTH = 10;
  private static final int CLIENT_SEED_OFFSET = 22;
  
  
  IFSUserHandleSeedReq(byte[] password) {
    super(HEADER_LENGTH + TEMPLATE_LENGTH);
    setLength(data_.length);
    setTemplateLen(TEMPLATE_LENGTH);
    setReqRepID(0x0023);
    set64bit(password.length, CLIENT_SEED_OFFSET);
    
    System.arraycopy(password, 0, data_, CLIENT_SEED_OFFSET, password.length);
  }
}
