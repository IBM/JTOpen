///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ProxyImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.InvocationTargetException;



/**
The ProxyImpl interface represents proxy implementation of
AS/400 Toolbox for Java classes.
**/
interface ProxyImpl 
{




/**
Constructs an object on the proxy server.

@param connection   The connection.
**/
    public abstract void construct (ProxyClientConnection connection);


    
    public long getPxId();



}
