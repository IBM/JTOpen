///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AFPResourceImplProxy.java
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
  * The AFPResourceImplProxy class implements proxy versions of
  * the public methods defined in the AFPResourceImpl class.
  * The implementations are merely calls to the remote implementation
  * class.
 **/

class AFPResourceImplProxy extends PrintObjectImplProxy
implements ProxyImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    AFPResourceImplProxy() 
    {
        super("AFPResource");
    }  
}
