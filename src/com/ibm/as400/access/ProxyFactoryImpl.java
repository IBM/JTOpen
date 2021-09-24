///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ProxyFactoryImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The ProxyFactoryImpl interface represents proxy implementation of
IBM Toolbox for Java classes that gets created on the server.
**/
interface ProxyFactoryImpl 
{



/**
Initializes an proxy object returned server.

@param proxyId      The proxy id.
@param connection   The connection.
**/
    public abstract void initialize (long proxyId, ProxyClientConnection connection);
    


}
