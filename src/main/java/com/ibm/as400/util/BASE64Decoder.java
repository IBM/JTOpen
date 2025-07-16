///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  BASE64Decoder.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2017-2017 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////


package com.ibm.as400.util;

//import java.io.IOException;
//import java.io.InputStream;
import java.util.Base64;
import java.util.Base64.Decoder;


/* Replacement for sun.misc.BASE64Decoder.  If that is available, it will be used
 * 
 */
public class BASE64Decoder {
  private Decoder decoder;
  static byte[] dummyBytes = new byte[0]; 
  public BASE64Decoder() { 
	       decoder = Base64.getDecoder(); 
  }

  public byte[] decodeBuffer(String inString) {
    return decoder.decode(inString);
  }
  
   
/*   
   
   static String dumpByteArray(byte[] b) { 
     StringBuffer sb = new StringBuffer(); 
     
     for (int i = 0; i < b.length; i++) { 
       sb.append(Integer.toHexString(0xFF & b[i])); 
       sb.append(" "); 
     }
     return sb.toString(); 
   }
   static boolean compareByteArrays(byte[] expected, byte[] actual) {
      boolean same = true; 
      if (expected.length == actual.length) {
        for (int i = 0; i < expected.length && same ; i++) {
          if (expected[i] != actual[i]) { 
            same = false; 
          }
        }
      } else {
        same = false; 
      }
      if (!same) { 
        System.out.println(" ARRAYS not same "); 
        System.out.println(" EXPECTED= "+dumpByteArray(expected));
        System.out.println(" ACTUAL=   "+dumpByteArray(actual));
        
      }
      return same; 
   }
   
   public static void main(String[] args)  {
     System.out.println("Testing"); 
     BASE64Decoder decoder = new BASE64Decoder(); 
     byte[] test1= {0x00,0x00,0x00,0x00}; 
     byte[] test2= {(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff}; 
     Object[][] tests = {
         {"TEST1", test1, "AAAAAA==" }, 
         {"TEST2", test2, "/////w==" }, 
     };
       int failureCount = 0; 
    for (int i = 0; i  < tests.length; i++) { 
      byte[] result = decoder.decodeBuffer((String) tests[i][2]); 
      if ( !compareByteArrays(result, (byte[]) tests[i][1])) {
         System.out.println("Test "+tests[i][0]+ " failed got "+result+" sb "+tests[i][1]); 
         failureCount++; 
      }
      
      
    }
    System.out.println("Test done, failure count = "+failureCount); 
     
     
     
     
    
   }
   
*/
  
  
  
}
