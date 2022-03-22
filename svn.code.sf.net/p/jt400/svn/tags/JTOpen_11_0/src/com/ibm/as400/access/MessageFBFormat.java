///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: MessageFBFormat.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal; //@C1A
import java.util.Vector; //@C1A




// Class to handle the common message feedback 'header'.
class MessageFBFormat extends RecordFormat
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  MessageFBFormat()
  {
    super();

    addFieldDescription(new BinaryFieldDescription(new AS400Bin2(),
                                                   "messagesOccurred"));
    addFieldDescription(new BinaryFieldDescription(new AS400Bin2(),
                                                   "numberOfMessages"));
    addFieldDescription(new BinaryFieldDescription(new AS400Bin2(),
                                                   "maximumMessages"));
    addFieldDescription(new BinaryFieldDescription(new AS400Bin2(),
                                                   "exceptionOccurred"));
    addFieldDescription(new ArrayFieldDescription(new AS400Array(new AS400ByteArray(10240), 1), "feedbackData"));
    setLengthDependency("feedbackData", "numberOfMessages");
  }
}
