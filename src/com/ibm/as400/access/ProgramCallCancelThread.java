///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ProgramCall.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class ProgramCallCancelThread  extends Thread {
  static final String copyright = "Copyright (C) 1996-2011 International Business Machines Corporation and others.";

   protected ProgramCall programCall_;

   public ProgramCallCancelThread(ProgramCall s)
   { 
       programCall_ = s;
   }

   public void run() 
   {
     boolean traceOn = JDTrace.isTraceOn();  
     if(traceOn) {
       Trace.log(Trace.INFORMATION, "run()");
     }

       try {
     if (programCall_  != null) { 
               
   sleep(programCall_.getTimeout() * 1000);
   traceOn = Trace.traceOn_; 
   
   if ((programCall_ != null) && (programCall_.isRunning())) {
       if (traceOn)
         Trace.log(Trace.INFORMATION, "NOTE:  ProgramCallCancelThread is cancelling a program call by user request.");
       programCall_.cancel();
   } else {
       if (traceOn) Trace.log(Trace.INFORMATION,"Doing nothing since program not running"); 
   }
     } else {
   if (traceOn) Trace.log(Trace.INFORMATION, "Doing nothing since program is null"); 
     } 
       } catch (Exception e) {
     if (traceOn) Trace.log(Trace.INFORMATION, "Exception "+e+" caught");
       }
 if (traceOn) Trace.log(Trace.INFORMATION, "Thread done"); 
   }
}
