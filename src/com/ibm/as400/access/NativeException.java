///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NativeException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class NativeException extends Exception
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;
    byte[] data = null;

    NativeException(byte[] data)
    {
        this.data = data;
    }
}
