///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConvTable1388.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.UnsupportedEncodingException;


public class ConvTable1377 extends ConvTableMixedMap
{
    private static final String copyright = "Copyright (C) 2017-2017 International Business Machines Corporation and others.";

    public ConvTable1377() throws UnsupportedEncodingException
    {
        this(1377);
    }

    ConvTable1377(int ccsid) throws UnsupportedEncodingException
    {
       /* Though officially defined as 28709, 1376 */ 
       /* the system behaves as 37, 1376 */ 
        super(ccsid, 37, 1376);
    }
}
