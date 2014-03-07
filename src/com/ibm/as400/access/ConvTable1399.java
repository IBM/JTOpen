///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConvTable1399.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;


class ConvTable1399 extends ConvTableMixedMap
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    ConvTable1399() throws UnsupportedEncodingException
    {
        this(1399);
        dbTable_ = ConvTable1399.makeAlternateMap(dbTable_);  /*@KDA*/
    }

    ConvTable1399(int ccsid) throws UnsupportedEncodingException
    {
        super(ccsid, 5123, 16684);
        dbTable_ = ConvTable1399.makeAlternateMap(dbTable_);  /*@KDA*/
    }
    
    
    
    
    
    /**
     * The following is a mapping used only by CCSID 1399 and so
     * it is not included in the base tables.  This was updated
     * on the system using MA41801.  
     */
    /*@KDA*/
    
    static Hashtable alternateToUnicodeMap = new Hashtable(); 
    static Hashtable alternateFromUnicodeMap = new Hashtable(); 
    
    static ConvTableDoubleMap makeAlternateMap(ConvTableDoubleMap inMap) {
      ConvTableDoubleMap newMap = new ConvTableDoubleMap(inMap);  
      char[] toUnicode = inMap.getToUnicode(); 
      char [] newToUnicode = (char []) alternateToUnicodeMap.get(toUnicode); 
      if (newToUnicode == null) { 
          newToUnicode = new char[toUnicode.length]; 
          System.arraycopy(toUnicode, 0, newToUnicode, 0, toUnicode.length); 
          newToUnicode[0x4260] = '\u2212';
          newToUnicode[0x426A] = '\u00a6';
          newToUnicode[0x43A1] = '\u301c';
          newToUnicode[0x444A] = '\u2014';
          newToUnicode[0x447C] = '\u2016';
          alternateToUnicodeMap.put(toUnicode, newToUnicode); 
      }
      newMap.setToUnicode(newToUnicode);

      char[] fromUnicode = inMap.getFromUnicode(); 
      char[] newFromUnicode = (char[]) alternateFromUnicodeMap.get(fromUnicode);
      if (newFromUnicode == null) { 
        newFromUnicode = new char[fromUnicode.length]; 
        System.arraycopy(fromUnicode, 0, newFromUnicode, 0, fromUnicode.length); 
        newFromUnicode[0x2015] = '\uDDB7';
        newFromUnicode[0x2225] = '\uDFE5';
        newFromUnicode[0xFF0D] = '\uE9F3';
        newFromUnicode[0xFF5E] = '\uE9F4';
        newFromUnicode[0xFFE4] = '\uE9F5';
        alternateToUnicodeMap.put(fromUnicode, newFromUnicode); 
      }
      newMap.setFromUnicode(newFromUnicode); 
      return newMap; 
    }

}
