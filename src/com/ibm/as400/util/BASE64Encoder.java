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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.ibm.as400.access.Trace;

/**
 * Replacement for sun.misc.BASE64Encoder
 * 
 * When sun.misc.BAS64Encoder is available via reflection use that. 
 * 
 * @author jeber
 *
 */
public class BASE64Encoder {
 Object  encoder ; 
 boolean sunMisc = false;
 boolean javaUtil = false; 
 private Method encodeBufferMethod; 
 static byte[] dummyBytes = new byte[0]; 
 public BASE64Encoder() { 
   try {
     String sunClass = "sun.misc.BASE64Encoder"; 
    Class encoderClass = Class.forName(sunClass);
    encoder = encoderClass.newInstance(); 
    Class[] parameterTypes = new Class[1]; 
    parameterTypes[0] = dummyBytes.getClass(); 
    encodeBufferMethod = encoderClass.getMethod("encodeBuffer", parameterTypes);
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
    Class encoderClass;
    try {
      Class baseClass = Class.forName("java.util.Base64");
      Class [] zeroArgs = new Class[0]; 
      Method getEncoderMethod = baseClass.getMethod("getEncoder", zeroArgs); 
      encoder = getEncoderMethod.invoke(null, null) ; 
      encoderClass = encoder.getClass(); 
    Class[] parameterTypes = new Class[1]; 
    parameterTypes[0] = dummyBytes.getClass(); 
    encodeBufferMethod = encoderClass.getMethod("encodeToString", parameterTypes);
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

  public String encodeBuffer(byte[] bytes) {
    if (sunMisc | javaUtil) {
      Object[] args = new Object[1];
      args[0] = bytes;
      try {
        return (String) encodeBufferMethod.invoke(encoder, args);
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
