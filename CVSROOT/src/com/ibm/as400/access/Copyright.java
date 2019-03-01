///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  Copyright.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 The Copyright interface is used to hold the copyright string and the version information for the IBM Toolbox for Java.
 **/
public interface Copyright
{
    /** @deprecated  This field is reserved for use within the Toolbox product. **/
    public static String copyright = "Copyright (C) 1997-2011 International Business Machines Corporation and others.";
    public static String version   = "Open Source Software, JTOpen 7.4, codebase 5770-SS1 V7R1M0.03 2011/03/3 @C7";  // As of V7R1: JC1 is merged into SS1 Option 3

    // Constants for reference by AS400JDBCDriver.
    static final int    MAJOR_VERSION = 9; // ex: "9" indicates V7R1
    static final int    MINOR_VERSION = 5; // ex: "1" indicates PTF #1 
                                           //Note: JTOpen 7.4 is synching with ptf 9.5
    static final String DRIVER_LEVEL  = "07010005"; //(ex: 05040102 -> V5R4M1 PTF#2) (needed for hidden clientInfo) (each # is 2 digits in length)
    
}
