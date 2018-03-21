package com.ibm.as400.access;


public class AS400JDBCConnectionAbortRunnable implements Runnable {
  
  AS400JDBCConnectionImpl connection_ = null; 
  
  public AS400JDBCConnectionAbortRunnable(AS400JDBCConnectionImpl connection) {
    connection_ = connection; 
  }
  
  public void run() {
   
      connection_.handleAbort();
    
  }

}
