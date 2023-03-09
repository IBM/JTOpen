///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConvTable9030.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// This is the Thai CCSID.  In real life, 9030 is a superset of 838.  But the server table for 838 was just updated to include the additions made when 9030 was created.  Therefore the 838 table we got from the server is actually the real life 9030 table.  So for 9030, we just extend 838 to pick up the new characters.
class ConvTable9030 extends ConvTable838
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    ConvTable9030()
    {
        super(9030);
    }
}
