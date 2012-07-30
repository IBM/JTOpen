///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  Trace.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite;

import java.io.File;
import java.io.PrintStream;

/**
 * Class representing the Tracing provided by JTOpenLite.
 *
 * <p>Trace can be enabled by setting the JVM property com.ibm.jtopenlite.Trace.category to ALL or TRUE.
 * <p>The trace output can be directed to a file by setting com.ibm.jtopenlite.Trace.file.
 * <p>The trace currently consists only of the datastream information.
 */
public class Trace {

  public final static int CATEGORY_NONE       = 0x00;
  public final static int CATEGORY_DATASTREAM = 0x01;
  public final static int CATEGORY_ALL        = 0xFF;

  static int traceCategory_ = CATEGORY_NONE;

  static PrintStream printStream = System.out;

  static {
      String category = System.getProperty("com.ibm.jtopenlite.Trace.category");

      String filename = System.getProperty("com.ibm.jtopenlite.Trace.file");
      if (filename != null) {
        File file = new File(filename);
        setPrintFile(file);
      }

      if (category != null) {
        category = category.toUpperCase().trim();
        if (category.equals("TRUE") || category.equals("ALL") ) {
          traceCategory_ = CATEGORY_ALL;
          if (printStream == System.out) {
            printStream.println("Tracing "+About.INTERFACE_NAME+" : level "+About.INTERFACE_LEVEL);
          }
        }
      }

  }



  public static void setTraceCategory(int traceCategory) {
    traceCategory_ = traceCategory;
  }

  public static int getTraceCategory() {
    return traceCategory_;
  }

  public static void setPrintFile(File file) {
     try {
       printStream = new PrintStream(file);
       printStream.println("Tracing "+About.INTERFACE_NAME+" : level "+About.INTERFACE_LEVEL);
     } catch (Exception e) {
       dumpException(e);
     }
  }

  public static PrintStream getPrintStream() {
    return printStream;
  }

  public static boolean isStreamTracingEnabled() {
    return (traceCategory_ & CATEGORY_DATASTREAM) != 0 ;
  }

  public static void dumpException(Exception e) {
    if (printStream != null) {
       e.printStackTrace(printStream);
    }
  }

}
