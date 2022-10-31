package com.ibm.as400.access;


public class AS400JDBCConnectionAbortRunnable implements Runnable {
  
  AS400JDBCConnection connection_ = null; 
  
  public AS400JDBCConnectionAbortRunnable(AS400JDBCConnection connection) {
    connection_ = connection; 
  }
  
  public void run() {
   
      connection_.handleAbort();
    
  }

}
