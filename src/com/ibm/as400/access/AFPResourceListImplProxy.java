///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AFPResourceListImplProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.InvocationTargetException;

class AFPResourceListImplProxy extends PrintObjectListImplProxy 
implements ProxyImpl
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    AFPResourceListImplProxy() 
    {
        super("AFPResourceList");
    } 
}
