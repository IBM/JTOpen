///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  JVMInfo.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
 Provide information about the currently running JVM.
 **/
public class JVMInfo
{

  static String javaVersion = null;
  static boolean jdk14;
  static boolean jdk16;

  protected static void initializeJavaVersion() {
    String version = System.getProperty("java.version");
    if (version != null) {
      if (version.length() > 2) {
        if (version.charAt(0) == '1' && version.charAt(2) < '4') {
          jdk14 = false;
          jdk16 = false;
        } else {
          if (version.charAt(0) == '1' && version.charAt(2) < '6') {
            jdk14 = true;
            jdk16 = false;
          } else {
            jdk14 = true;
            jdk16 = true;

          }
        }
      } else {
        // Android JVM returns 0 and runs with JDK 1.5
        if ("0".equals(version)) {
          jdk14 = true;
          jdk16 = false;
        }
      }
    }



  }

  /*
   * Is the JVM running JDK 1.4 or later.
   */
  public static boolean isJDK14() {
    if (javaVersion == null) {
      initializeJavaVersion();
    }
    return jdk14;
  }

  /*
   * Is the JVM running JDK 1.6 or later.
   */
  public static boolean isJDK16() {
    if (javaVersion == null) {
      initializeJavaVersion();
    }
    return jdk16;
  }


}
