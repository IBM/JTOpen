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
    /** @deprecated This field is reserved for use within the Toolbox product. **/
    public static final String copyright = "Copyright (C) 1997-2010 International Business Machines Corporation and others.";
    public static final String version   = "Open Source Software, JTOpen 6.7, codebase 5761-JC1 V6R1M0.11";

    // Constants for reference by AS400JDBCDriver.
    static final int    MAJOR_VERSION = 8;  // ex: "8" indicates V6R1
    static final int    MINOR_VERSION = 11; // ex: "8" indicates PTF #8 //Note: JTOpen 6.5 is synching with ptf8
    static final String DRIVER_LEVEL  = "07010001"; //(ex: 05040102 -> V5R4M1 PTF#2) (needed for hidden clientInfo) (each # is 2 digits in length)
}
