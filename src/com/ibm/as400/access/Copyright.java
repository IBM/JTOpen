///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: Copyright.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
The Copyright interface is used to hold the copyright string and the
version information for the AS/400 Toolbox for Java.
**/
// @D0C
// Implementation notes:
//
// All classes in the package need a copyright and should get that
// by declaring a constant variable as:
// <PRE>
// static final private String x = Copyright.copyright;
// </PRE>
//
public interface Copyright
{
    public static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";
    public static final String version   = "Open Source Software, JTOpen 2.0, original codebase 5769-JC1 V4R5M0.1";
}

