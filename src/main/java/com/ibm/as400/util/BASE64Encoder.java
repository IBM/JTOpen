///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  BASE64Encoder.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2017-2017 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////


package com.ibm.as400.util;

import java.util.Base64;
import java.util.Base64.Encoder;

/**
 * Replacement for sun.misc.BASE64Encoder
 *
 */
public class BASE64Encoder {
 static byte[] dummyBytes = new byte[0];
private Encoder encoder; 
 public BASE64Encoder() { 

      encoder = Base64.getEncoder(); 
 }

  public String encodeBuffer(byte[] bytes) {
    return encoder.encodeToString(bytes);
  }
 
/*   
  public static void main(String[] args)  {
    System.out.println("Testing"); 
    BASE64Encoder encoder = new BASE64Encoder(); 
    byte[] test1= {0x00,0x00,0x00,0x00}; 
    byte[] test2= {(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff}; 
    Object[][] tests = {
        {"TEST1", test1, "AAAAAA==" }, 
        {"TEST2", test2, "/////w==" }, 
    };
      int failureCount = 0; 
   for (int i = 0; i  < tests.length; i++) { 
     String result = encoder.encodeBuffer((byte[]) tests[i][1]); 
     if ( ! result.equals(tests[i][2])) {
        System.out.println("Test "+tests[i][0]+ " failed got "+result+" sb "+tests[i][2]); 
        failureCount++; 
     }
     
     
   }
   System.out.println("Test done, failure count = "+failureCount); 
    
    
    
    
   
  }
  */
}
