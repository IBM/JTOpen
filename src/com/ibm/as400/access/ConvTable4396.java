///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConvTable4396.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// This is a Japanese character set.
class ConvTable4396 extends ConvTable16684
{
    ConvTable4396()
    {
        super(4396);
    }

    static void makeAlternateMap(char[] toUnicode, char[] fromUnicode)
    {
        toUnicode[0x444A] = '\u2015';
        toUnicode[0x43A1] = '\uFF5E';
        toUnicode[0x447C] = '\u2225';
        toUnicode[0x4260] = '\uFF0D';
        toUnicode[0x426A] = '\uFFE4';

        fromUnicode[0x2015] = '\u444A';
        fromUnicode[0xFF5E] = '\u43A1';
        fromUnicode[0x2225] = '\u447C';
        fromUnicode[0xFF0D] = '\u4260';
        fromUnicode[0xFFE4] = '\u426A';
    }
}
