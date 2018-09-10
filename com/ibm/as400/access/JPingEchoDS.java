///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JPingEchoDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 *  The JPingEchoDS is the datastream sent to the server
 *  and the server sends back a reply.
 *
 **/
class JPingEchoDS extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    /**
     *  Constructs a JPingEchoDS object.
     *
     *  @param serverId The AS/400 service.
     *  @param buffer The datastream.
     *  @param i The request correlation for this datastream.
     *
     **/
    JPingEchoDS(int serverId, byte[] buffer)
    {
       super();
       data_ = new byte[buffer.length + 20];
       setLength(buffer.length + 20);
       setHeaderID(0);
       setServerID(serverId);
       setCSInstance(0xEEEEEEEE); // The CS ID of the Ping Echo datastream (no particular reason for having 0xEEEEEEEE).
       //setCorrelation(i);
       setTemplateLen(buffer.length);
       setReqRepID(0x7FFE);  // The ID of the Ping Echo datastream (no particular reason for having 0x7FFE).
       
       System.arraycopy(buffer, 0, data_, 20, buffer.length);
    }
}
