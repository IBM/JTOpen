///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JPingDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 *  The JPingDS is the datastream sent to the AS/400 server
 *  during a ping to determine if the server is running.
 *
 **/
class JPingDS extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    /**
     *  Constructs a JPingDS object.
     *
     *  @param serverId The AS/400 service.
     *  @param buffer The datastream.
     *  @param i The request correlation for this datastream.
     *
     **/
    JPingDS(int serverId, byte[] buffer)
    {
       super();
       // Add 20, which is the datastream header size, to the buffer length.
       data_ = new byte[buffer.length + 20];
       setLength(buffer.length + 20);
       setHeaderID(0);
       setServerID(serverId);
       setCSInstance(0xFFFFFFFF);  //The CS ID of the Ping datastream (no particular reason for having 0xffffffff).
       //setCorrelation(i);
       setTemplateLen(buffer.length);
       setReqRepID(0x7FFF);  //The ID of the Ping datastream (no particular reason for having 0x7fff).
       
       System.arraycopy(buffer, 0, data_, 20, buffer.length);
    }
}
