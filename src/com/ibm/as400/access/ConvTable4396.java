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

import java.util.Hashtable;


// This is a Japanese character set.
class ConvTable4396 extends ConvTable300
{
    ConvTable4396()
    {
        super(4396);
    }

    // This method is a bad idea because it modifies existing tables
    // which are usually static tables with a single definition. 
    // Since it is a static table, it should be treated as immutable. 
    // Replace this with two methods that return the new tables
    // and keep a copy for reuse if needed again
    //static void makeAlternateMap(char[] toUnicode, char[] fromUnicode)
    //{
    //    toUnicode[0x4260] = '\uFF0D';
    //    toUnicode[0x426A] = '\uFFE4';
    //    toUnicode[0x43A1] = '\uFF5E';
    //    toUnicode[0x444A] = '\u2015';
    //    toUnicode[0x447C] = '\u2225';
    //
    //    fromUnicode[0x2015] = '\u444A';
    //    fromUnicode[0x2225] = '\u447C';
    //    fromUnicode[0xFF0D] = '\u4260';
    //    fromUnicode[0xFF5E] = '\u43A1';
    //    fromUnicode[0xFFE4] = '\u426A';
    //}
    // 
    
    static Hashtable alternateToUnicodeMap = new Hashtable(); 
    static Hashtable alternateFromUnicodeMap = new Hashtable(); 
    
    static ConvTableDoubleMap makeAlternateMap(ConvTableDoubleMap inMap) {
      ConvTableDoubleMap newMap = new ConvTableDoubleMap(inMap);  
      char[] toUnicode = inMap.getToUnicode(); 
      char [] newToUnicode = (char []) alternateToUnicodeMap.get(toUnicode); 
      if (newToUnicode == null) { 
          newToUnicode = new char[toUnicode.length]; 
          System.arraycopy(toUnicode, 0, newToUnicode, 0, toUnicode.length); 
          newToUnicode[0x444A] = '\u2015';
          newToUnicode[0x43A1] = '\uFF5E';
          newToUnicode[0x447C] = '\u2225';
          newToUnicode[0x4260] = '\uFF0D';
          newToUnicode[0x426A] = '\uFFE4';
          alternateToUnicodeMap.put(toUnicode, newToUnicode); 
      }
      newMap.setToUnicode( newToUnicode); 

      char[] fromUnicode = inMap.getFromUnicode(); 
      char[] newFromUnicode = (char[]) alternateFromUnicodeMap.get(fromUnicode);
      if (newFromUnicode == null) { 
        newFromUnicode = new char[fromUnicode.length]; 
        System.arraycopy(fromUnicode, 0, newFromUnicode, 0, fromUnicode.length); 
        newFromUnicode[0x2015] = '\u444A';
        newFromUnicode[0xFF5E] = '\u43A1';
        newFromUnicode[0x2225] = '\u447C';
        newFromUnicode[0xFF0D] = '\u4260';
        newFromUnicode[0xFFE4] = '\u426A';
        alternateToUnicodeMap.put(fromUnicode, newFromUnicode); 
      }
      newMap.setFromUnicode(newFromUnicode); 
      return newMap; 
    }
    
}
