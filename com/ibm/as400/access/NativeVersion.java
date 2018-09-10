///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  NativeVersion.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// The NativeVersion class is used to hold the version level for the IBM Toolbox for Java Native Classes.
class NativeVersion
{
    private static final int VERSION = 2;  // Mod3 is version 1.

    // Hashcode returns the version of the native classes.
    // @return  The native version.
    public int hashCode()
    {
        return VERSION;
    }
}
