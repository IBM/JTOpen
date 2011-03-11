///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConvTable5026.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.UnsupportedEncodingException;

class ConvTable5026 extends ConvTableMixedMap
{
    ConvTable5026() throws UnsupportedEncodingException
    {
        super(5026, 290, 16684);  // 16684 is a superset of 300.
        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "Using alternate map.");
        ConvTable4396.makeAlternateMap(dbTable_.toUnicode_, dbTable_.fromUnicode_);
    }
}
