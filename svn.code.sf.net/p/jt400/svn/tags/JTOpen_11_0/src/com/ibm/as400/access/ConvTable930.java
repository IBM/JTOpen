///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConvTable930.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.UnsupportedEncodingException;

public class ConvTable930 extends ConvTableMixedMap
{
    public ConvTable930() throws UnsupportedEncodingException
    {
        super(930, 1000930, 2000930);  // Used to be 290, 16684 is a superset of 300.
         
    }
}
