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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.ibm.as400.access.Trace;

/* Replacement for sun.misc.BASE64Decoder.  If that is available, it will be used
 * 
 */
public class BASE64Decoder {

  
  

  Object  decoder ; 
  boolean sunMisc = false;
  boolean javaUtil = false; 
  private Method decodeStringMethod; 
  static byte[] dummyBytes = new byte[0]; 
  public BASE64Decoder() { 
    try {
      String sunClass = "sun.misc.BASE64Decoder"; 
     Class decoderClass = Class.forName(sunClass);
     decoder = decoderClass.newInstance(); 
     Class[] parameterTypes = new Class[1]; 
     parameterTypes[0] = Class.forName("java.lang.String");  
     decodeStringMethod = decoderClass.getMethod("decodeBuffer", parameterTypes);
     
     sunMisc = true; 
   } catch (ClassNotFoundException e) {
     Trace.log(Trace.ERROR, e); 
   } catch (InstantiationException e) {
     Trace.log(Trace.ERROR, e); 
   } catch (IllegalAccessException e) {
     Trace.log(Trace.ERROR, e); 
   } catch (SecurityException e) {
     Trace.log(Trace.ERROR, e); 
   } catch (NoSuchMethodException e) {
     Trace.log(Trace.ERROR, e); 
   } 

   if (!sunMisc) {
     // Try the new method
     Class decoderClass;
     try {
       Class baseClass = Class.forName("java.util.Base64");
       Class [] zeroArgs = new Class[0]; 
       Method getDecoderMethod = baseClass.getMethod("getDecoder", zeroArgs); 
       decoder = getDecoderMethod.invoke(null, null) ; 
       decoderClass = decoder.getClass(); 
     Class[] parameterTypes = new Class[1]; 
     decodeStringMethod = decoderClass.getMethod("encodeToString", parameterTypes);
     javaUtil = true;  
     } catch (ClassNotFoundException e) {
       Trace.log(Trace.ERROR, e); 
     } catch (IllegalAccessException e) {
       Trace.log(Trace.ERROR, e); 
     } catch (SecurityException e) {
       Trace.log(Trace.ERROR, e); 
     } catch (NoSuchMethodException e) {
       Trace.log(Trace.ERROR, e); 
     } catch (IllegalArgumentException e) {
       Trace.log(Trace.ERROR, e); 
     } catch (InvocationTargetException e) {
       Trace.log(Trace.ERROR, e); 
     }
  
    
   }
  }

   public byte[] decodeBuffer(String inString) {
     if (sunMisc | javaUtil) {
       Object[] args = new Object[1];
       args[0] = inString;
       try {
         return (byte[]) decodeStringMethod.invoke(decoder, args);
       } catch (Exception e) {
         if (e instanceof InvocationTargetException) {
           InvocationTargetException ite = (InvocationTargetException) e;
           Throwable cause = ite.getCause();
           if (cause instanceof Error) {
             throw (Error) cause;
           } else {
             Error error = new Error("UNEXPECTED EXCEPTION");
             error.initCause(e);
             throw error;
           }
         } else {
           Error error = new Error("UNEXPECTED EXCEPTION");
           error.initCause(e);
           throw error;
         }
       }
     } else {
       throw new Error("UNIMPLEMENTED");
     }
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
