///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: RCCallProgramFailureReplyDataStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// Program failed, same datastream except for request ID.
class RCCallProgramFailureReplyDataStream extends RCCallProgramReplyDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    Object getNewDataStream()
    {
        return new RCCallProgramFailureReplyDataStream();
    }

    public int hashCode()
    {
        return 0x9003;
    }
}
