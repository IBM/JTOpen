///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCQueryCancelThread.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2011 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class AS400JDBCQueryCancelThread  extends Thread {
     static final String copyright = "Copyright (C) 1996-2011 International Business Machines Corporation and others.";

      protected AS400JDBCStatement statement_;

      public AS400JDBCQueryCancelThread(AS400JDBCStatement s)
      { 
          statement_ = s;
      }

      public void run() 
      {
        boolean traceOn = JDTrace.isTraceOn();  
        if(traceOn) {
          JDTrace.logInformation(this, "run()");
        }

          try {
        if (statement_  != null) { 
                  //
                  // Note .. a slight race condition exists here, so we may hit a null pointer
                  // exception when Statement.endCancelThread sets statement_ to null
                  // This exception should be caught below
                  // 
      sleep(statement_.queryTimeout_ * 1000);
      traceOn = JDTrace.isTraceOn(); 
      
      if ((statement_ != null) && (statement_.queryRunning_)) {
          if (traceOn)
            JDTrace.logInformation(this, "NOTE:  AS400JDBCQueryCancelThread is cancelling a statement by user request.");
          statement_.cancel();
      } else {
          if (traceOn) JDTrace.logInformation(this, "Doing nothing since query not running"); 
      }
        } else {
      if (traceOn) JDTrace.logInformation(this, "Doing nothing since statement is null"); 
        } 
          } catch (Exception e) {
        if (traceOn) JDTrace.logInformation(this, "Exception "+e+" caught");
          }
    if (traceOn) JDTrace.logInformation(this, "Thread done"); 
      }
}


  
 
