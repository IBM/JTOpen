///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConvTable300.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// This is a Japanese character set.
// In the past this directly extended 16684.
// However, there are a few code points that are different
// We add the differences here instead of generating a CCSID 300 table
// The problem with generating a CCSID 300 table is that there would be 
// thousands of code points that no longer map, and there could be applications
// depending on this behavior. @N4A
class ConvTable300 extends ConvTable16684
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";
    private static char[] toUnicodeArray300_;
    private static char[] fromUnicodeArray300_;
    
    static {
      // decompress the arrays first so that they can be fixed @N4A 
      toUnicodeArray300_ = decompress(toUnicode_.toCharArray(), 300);
      fromUnicodeArray300_ = decompress(fromUnicode_.toCharArray(), 300);
      
      // Now fix up the code points that are different  @N4A
          toUnicodeArray300_[0x4260] = '\uFF0D';
          toUnicodeArray300_[0x426A] = '\uFFE4';
          toUnicodeArray300_[0x43A1] = '\uFF5E';
          toUnicodeArray300_[0x444A] = '\u2015';
          toUnicodeArray300_[0x447C] = '\u2225';
      
          fromUnicodeArray300_[0x2015] = '\u444A';
          fromUnicodeArray300_[0x2225] = '\u447C';
          
          fromUnicodeArray300_[0x525d] = '\u5481';
          fromUnicodeArray300_[0x5c5b] = '\u5443';
          fromUnicodeArray300_[0x7c1e] = '\u54ca';
          fromUnicodeArray300_[0x87ec] = '\u53e8';
          fromUnicodeArray300_[0x9a52] = '\u53da';
          
          fromUnicodeArray300_[0xFF0D] = '\u4260';
          fromUnicodeArray300_[0xFF5E] = '\u43A1';
          fromUnicodeArray300_[0xFFE4] = '\u426A';


    }

    ConvTable300()
    {
        super(300, toUnicodeArray300_, fromUnicodeArray300_);
    }
    ConvTable300(int ccsid)
    {
        super(ccsid, toUnicodeArray300_, fromUnicodeArray300_);
    }
}
