///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: MessageFBDataFormat.java
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


// Class to handle the common message feedback data structure.
class MessageFBDataFormat extends RecordFormat
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    MessageFBDataFormat(int ccsid)
    {
        super();

        addFieldDescription(new HexFieldDescription(new AS400ByteArray(8), "notUsed1"));
        addFieldDescription(new BinaryFieldDescription(new AS400Bin4(), "severityCode"));
        addFieldDescription(new CharacterFieldDescription(new AS400Text(7), "messageID"));
        addFieldDescription(new CharacterFieldDescription(new AS400Text(2), "messageType"));
        addFieldDescription(new HexFieldDescription(new AS400ByteArray(59), "notUsed2"));
        addFieldDescription(new BinaryFieldDescription(new AS400Bin4(), "replacementTextLength"));
        addFieldDescription(new HexFieldDescription(new AS400ByteArray(4), "notUsed3"));
        addFieldDescription(new BinaryFieldDescription(new AS400Bin4(), "messageTextLength"));
        addFieldDescription(new HexFieldDescription(new AS400ByteArray(20), "notUsed4"));
    }
}
