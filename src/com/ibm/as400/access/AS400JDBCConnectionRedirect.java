///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: AS400JDBCConnectionRedirect.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2018 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.sql.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

/* ifdef JDBC40
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Executor;

endif */ 

/**
 * <p>
 * The AS400JDBCConnectionRedirect class provide a level of indirection above
 * AS400JDBCConnections. The goal is to be able to switch to use an alternative
 * server if the existing server becomes unavailable.
 * 
 * This class is only used if enableClientAffinitiesList is set to 1. 
 **/
public class AS400JDBCConnectionRedirect

extends AS400JDBCConnection {
  /* should errors not be handled? */ 
  /* This is set to true when closing result sets associated */
  /* with the old connection */ 
  
  /* How many times to seamlessly reconnect */ 
  /* This is need to prevent an infinite loop if executing a statement */ 
  /* always causes the connection to die */
  
  public static final int SEAMLESS_RETRY_COUNT = 15;
  
  boolean enableSeamlessFailover_ = false; 
  boolean doNotHandleErrors_ = false; 
  AS400JDBCConnectionImpl currentConnection_;
  JDDataSourceURL currentUrl_ = null ;
  private AS400 originalAs400;
  private JDDataSourceURL originalDataSourceUrl_;
  private JDProperties originalProperties_;
  private Properties originalInfo_; 
  
  private JDDataSourceURL [] reconnectUrls_;
  private JDProperties[]     reconnectProperties_;
  private AS400[]            reconnectAS400s_; 
  
  private int maxRetriesForClientReroute_ = -1; 
  private int retryIntervalForClientReroute_ = -1; 
  private int affinityFailbackInterval_ = 0 ; 
  private long affinityFailbackTime_ = 0;
  private boolean affinityOnAlternate_ = false; 
  
  private Vector setCommands_ = null; 
  private boolean throwException_; /* should exception be thrown from findNewConnecton */
  private boolean autoCommitSet_ = false;
  private boolean autoCommitSetting_ = false;
  private boolean transactionIsolationSet_ = false;
  private int transactionIsolationSetting_ = 0;
  private Hashtable clientInfoHashtable_ = null;
  private boolean holdabilitySet_ = false;   
  private int holdability_ = 0;
  private boolean readOnlySet_ = false; 
  private boolean readOnly_;
  private boolean networkTimeoutSet_ = false;
  private int     networkTimeout_; 
  
  private boolean lastConnectionCanSeamlessFailover_ = false; 
  private boolean topLevelApi_ = false;



  
  /** 
   * Default constructor reserved for use within package
   */
  AS400JDBCConnectionRedirect() { 
    currentConnection_ = new AS400JDBCConnectionImpl(); 
  }
  
  
  /* Setup the array of things to retry when a failure */ 
  /* This is the information that is passed to the */ 
  /* connection.setProperty method */ 
  /* currentConnection_.setProperties(dataSourceUrl, properties, as400); */ 
  
  private void setupRetryInformation() {
    
    if (JDTrace.isTraceOn()) { 
      JDTrace.logInformation(this, " setupRetryInformation"); 
    }

       enableSeamlessFailover_ = getEnableSeamlessFailover(); 
       if (enableSeamlessFailover_ ) {
          affinityFailbackInterval_ = getAffinityFailbackInterval(); 
       }
       maxRetriesForClientReroute_ = getMaxRetriesForClientReroute(); 
       retryIntervalForClientReroute_ = getRetryIntervalForClientReroute(); 
       // If retryIntervalForClientReroute is set, the default for 
       // maxRetriesForClientReroute is 3. 
       if (retryIntervalForClientReroute_ > 0 &&
           maxRetriesForClientReroute_ < 0) {
         maxRetriesForClientReroute_ = 3; 
       }
       // If maxRetriesForClientReroute is set, the default for
       // retryIntervalForClientReroute is 0
       if (maxRetriesForClientReroute_ > 0 && 
           retryIntervalForClientReroute_ < 0) {
           retryIntervalForClientReroute_= 0; 
       }
         
       
       
       Vector alternateServerNames = getAlternateServerNames(); 
       Vector alternatePortNumbers = getAlternatePortNumbers(); 
       setupAlternateServers(alternateServerNames, alternatePortNumbers); /*@X1A*/ 
      
  }
  
  
  private void setupAlternateServers(String alternateServers) {
    
    Vector alternateServerNames = getCommaSeparatedList(alternateServers); 
    Vector alternatePortNumbers = getAlternatePortNumbers(); 
    setupAlternateServers(alternateServerNames, alternatePortNumbers); 
  }
  
  // Setup the re-connect information based the alternateServerNames and alternatePortNumbers
  
  private void setupAlternateServers(Vector alternateServerNames,   Vector alternatePortNumbers) {
    boolean traceOn =JDTrace.isTraceOn();  
    if (traceOn)
      JDTrace.logInformation(this, "setupAlternateServers");

    int alternateServerCount = alternateServerNames.size(); 
    int alternatePortCount = alternatePortNumbers.size(); 
    reconnectUrls_ = new JDDataSourceURL[1+alternateServerCount]; 
    reconnectProperties_ = new JDProperties [ 1+alternateServerCount]; 
    reconnectAS400s_ = new AS400[ 1+alternateServerCount]; 
     
    reconnectUrls_[0] = originalDataSourceUrl_; 
    reconnectProperties_[0] = originalProperties_; 
    reconnectAS400s_[0] = new AS400(originalAs400); 

    
    if (traceOn) { 
      JDTrace.logInformation(this, " reconnectUrls_[0]="+ reconnectUrls_[0]);
      JDTrace.logInformation(this, " reconnectProperties[0]="+ reconnectProperties_[0]);
    }
    
    boolean secure = originalProperties_.getBoolean(JDProperties.SECURE); /*@X1A*/
    for (int i = 0; i < alternateServerCount; i++) { 
      String server = (String) alternateServerNames.elementAt(i); 
      String port = null; 
      if (i < alternatePortCount) {
         port = (String) alternatePortNumbers.elementAt(i); 
      } else {
         // If port not given, use default host server port
        if (secure) {
           port = "9471"; 
        } else {
           port = "8471"; 
        }
      }
      int setIndex = i+1; 
      reconnectUrls_[setIndex] = fixupDataSourceUrl(server, port);  
      reconnectProperties_[setIndex] = fixupProperties(server, port); 
      reconnectAS400s_[setIndex] = fixupAS400(server, port);
      
      if (traceOn) { 
        JDTrace.logInformation(this, " reconnectUrls_["+setIndex+"]="+ reconnectUrls_[setIndex]);
        JDTrace.logInformation(this, " reconnectProperties["+setIndex+"]="+ reconnectProperties_[setIndex]);
      }

    }
    
  }

  
  private AS400 fixupAS400( String server, String port) {
    AS400 as400 = new AS400(originalAs400); 
    try {
      as400.setSystemName(server);
    } catch (PropertyVetoException e) {
      // This should not happen 
    }
    return as400; 
  }

  private JDProperties fixupProperties(
      String server, String port) {
    JDProperties properties = (JDProperties) originalProperties_.clone();
    if (port != null) { 
       properties.setString(JDProperties.PORTNUMBER, port);
    }
    return properties;
  }

  private JDDataSourceURL fixupDataSourceUrl(
      String server, String port) {
    JDDataSourceURL url = new JDDataSourceURL(originalDataSourceUrl_, server, port); 
    
    return url;
  }

  int getAffinityFailbackInterval() {
    return  originalProperties_.getInt(JDProperties.AFFINITY_FAILBACK_INTERVAL); 
  }
  
  boolean getEnableSeamlessFailover() {
    int value = originalProperties_.getInt(JDProperties.ENABLE_SEAMLESS_FAILOVER); 
    if (value == 1) {
      return true; 
    }
    return false; 
  }
  
  int getMaxRetriesForClientReroute() { 
    int value = originalProperties_.getInt(JDProperties.MAX_RETRIES_FOR_CLIENT_REROUTE);
    if (value < 0) value = -1; 
    return value; 
  }

  int getRetryIntervalForClientReroute() { 
    int value = originalProperties_.getInt(JDProperties.RETRY_INTERVAL_FOR_CLIENT_REROUTE);
    if (value < 0) value = -1; 
    return value; 
  }

  
  /* return a vector of the alternate port numbers from the properties */ 
  private Vector getAlternatePortNumbers() {
    return getPropertiesList(JDProperties.CLIENT_REROUTE_ALTERNATE_PORT_NUMBER);
  }
  /* return a vector of the alternate server names from the properties */ 
  private Vector getAlternateServerNames() {
    return getPropertiesList(JDProperties.CLIENT_REROUTE_ALTERNATE_SERVER_NAME);
  }

  /* retrieve the properties that are separated by commas */ 
  private Vector getPropertiesList(int property) { 
    String propertyString = originalProperties_.getString(property);
    return getCommaSeparatedList(propertyString); 
  }
  
  // @X1A
  private Vector getCommaSeparatedList(String list) { 
    Vector propertiesList = new Vector(); 
    if (list != null) {
      int startIndex = 0; 
      int commaIndex; 
      commaIndex = list.indexOf(',',startIndex); 
      while (commaIndex >= 0) { 
        propertiesList.add(list.substring(startIndex,commaIndex));
        startIndex = commaIndex + 1; 
        commaIndex = list.indexOf(',',startIndex); 
      }
      String lastPort = list.substring(startIndex); 
      if (lastPort.length() > 0)  {
         propertiesList.add(lastPort );
      }
    }
    return propertiesList;
  }
  /** 
   * Reconnect to an alternate server if possible. 
   * Reconnect will throw the original exception if unable to reconnect. 
   * It will return true if it was able to seamlessly reconnect.
   * Otherwise it will throw the exception SQL4498 indicating that
   * the connection was re-established. 
   */
    
  boolean reconnect(SQLException originalException)  throws SQLException {
    return  findNewConnection(originalException);
    
  }
  
  private void replaySettings(AS400JDBCConnectionImpl newConnection) throws SQLException {
    if (JDTrace.isTraceOn()) { 
      JDTrace.logInformation(this, " replaySettings"); 
    }

     if (setCommands_ != null) {
       Statement stmt = newConnection.createStatement(); 
       Enumeration elements = setCommands_.elements(); 
       while (elements.hasMoreElements()) {
         String setCommand = (String) elements.nextElement(); 
         stmt.executeUpdate(setCommand); 
       }
       stmt.close(); 
     }
     if (autoCommitSet_) {
       newConnection.setAutoCommit(autoCommitSetting_); 
     }
     if (transactionIsolationSet_) {
       newConnection.setTransactionIsolation(transactionIsolationSetting_); 
     }
     
     if (holdabilitySet_) {
       newConnection.setHoldability(holdability_); 
     }

     if (readOnlySet_) { 
       newConnection.setReadOnly(readOnly_); 
     }
     if (networkTimeoutSet_) {
       newConnection.setNetworkTimeout( networkTimeout_); 
     }

     /* Restore the client information */ 
     if (clientInfoHashtable_ != null) {
        Enumeration keysEnum = clientInfoHashtable_.keys(); 
        while (keysEnum.hasMoreElements()) {
          String key = (String) keysEnum.nextElement(); 
          String value = (String) clientInfoHashtable_.get(key); 
          newConnection.setClientInfo(key, value); 
        }
     }
     /* Make sure the work is committed */ 
     newConnection.commit(); 
  }
  
  
  /**
   * Set up a new connection for use.  If the new connection can be
   * seamlessly be used, this returns true. 
   * @param newConnection
   * @return true if new connection can seamlessly be used.
   * Otherwise throws the SQL4498 exception. 
   */
  private boolean setupNewConnection(AS400JDBCConnectionImpl newConnection, JDDataSourceURL newUrl, SQLException e, boolean primaryServer) throws SQLException {
    // Now replay the settings on the new connection.
    // If this has a failure then the exception is thrown and we are unable to use the connection. 
    
    if (JDTrace.isTraceOn()) { 
      JDTrace.logInformation(this, " setupNewConnection"); 
    }

    replaySettings(newConnection); 
    if (enableSeamlessFailover_) {
      lastConnectionCanSeamlessFailover_ = currentConnection_.canSeamlessFailover();    
    }
    
    doNotHandleErrors_ = true; 
    // Close all the results sets associated with the old connection
    // If there is an error, we do not try to handle it and create a new connection. 
    try { 
      currentConnection_.closeAllResultSets();
    } catch (Exception closeException) {
      // Log this exception, but continue on
      if (JDTrace.isTraceOn())
        JDTrace.logException(this, "Exception from closeAllResultSets", e); // @J3a
    } catch (Throwable t) {
      // Log this exception, but continue on
      if (JDTrace.isTraceOn())
        JDTrace.logException(this, "Throwable from closeAllResultSets", e); // @J3a
      
    } finally { 
      doNotHandleErrors_ = false; 
    }
    // Reset the statements and point them to the new transaction manager
    currentConnection_.resetStatements(newConnection.transactionManager_); 

    // Need to fix up all the objects associated with the old connection and transfer them to 
    // the new connection.   As part of this, all existing result sets will be closed. 
    currentConnection_.transferObjects(newConnection); 
    
    // Make sure the current connection is closed.  Ignore any failures that could occur here
    try { 
      currentConnection_.close(); 
    } catch (Exception e2) { 
      
    }
    
    
    if (affinityFailbackInterval_ > 0) {
      if (primaryServer) {
        affinityOnAlternate_ = false; 
      } else {
        affinityOnAlternate_ = true; 
        affinityFailbackTime_ = System.currentTimeMillis() + 1000 * affinityFailbackInterval_; 
      }
    }
    currentConnection_ = newConnection; 
    currentUrl_ = newUrl; 
    if (lastConnectionCanSeamlessFailover_ && topLevelApi_) {
      // For a topLevelApi_ we can return true and have the connection
      // object retry.
      return true;
    } else {
      throwException_ = true;
      String[] replacementVariables = new String[2];
      replacementVariables[0] = currentConnection_.getHostName();
      replacementVariables[1] = currentConnection_.getPort();

      JDError.throwSQLException(this, JDError.EXC_CONNECTION_REESTABLISHED,
          replacementVariables, e);

      return false;
    }
  }
  /** 
   * Find and enable a new connection to the server. 
   * @return true if the new connection can be seamlessly used.
   * otherwise throw the SQL4498 exception saying that the connection was reused, 
   * otherwise throw the original exception. 
   * @param originalException -- the exception that trigger the switch. 
   *                             Will be null for affinityFailback.  
   * @throws SQLException  If a database error occurs.
   */
   boolean findNewConnection(SQLException originalException) throws SQLException {
     SQLException savedException = null; 
     int searchStart = 0; 
     if (JDTrace.isTraceOn()) { 
       JDTrace.logInformation(this, " findNewConnection for "+originalException); 
     }

     // If the original exception is an SQL7061 do not attempt to reconnect to
     // the existing system, but connect to the next system in the list.
     // Note: To get here, it has already been checked that reconnect is 
     // allowed for this SQL7061. 
    if (originalException != null) {
      int sqlcode = originalException.getErrorCode();
      if (sqlcode == -7061) {
        if (currentUrl_ != null) {
          for (int i = 0; i < reconnectUrls_.length; i++) {
            if (reconnectUrls_[i] == currentUrl_) {
              searchStart = i + 1;
              if (searchStart == reconnectUrls_.length) {
                searchStart = 0;
              }
              i = reconnectUrls_.length;
            }
          }

        }
      }
    }
    // Start at the current server and try to get a new connection.
     AS400JDBCConnectionImpl connection ; 
     Exception[] exceptions = new Exception [reconnectUrls_.length];
    int retryCount;  
    long delayMilliseconds; 
    long startMilliseconds = System.currentTimeMillis(); 
    if (maxRetriesForClientReroute_ >= 0) {
      retryCount = maxRetriesForClientReroute_; 
    } else {
      retryCount= Integer.MAX_VALUE; 
    }
    if (retryIntervalForClientReroute_ >= 0) {
      delayMilliseconds = retryIntervalForClientReroute_ * 1000; 
    } else {
      // Start delay at 30 seconds 
      delayMilliseconds = 30000; 
    }

    while (retryCount > 0) {
      long retryStartMilliseonds = System.currentTimeMillis(); 
      for (int i = searchStart; i < reconnectUrls_.length; i++) {
        connection = new AS400JDBCConnectionImpl();
        AS400 as400 = new AS400(reconnectAS400s_[i]);
        try {
          if (JDTrace.isTraceOn()) { 
            JDTrace.logInformation(this, "findNewConnection attempting "+ reconnectUrls_[i]+","+ reconnectProperties_[i]);
          }

          connection.setProperties(reconnectUrls_[i], reconnectProperties_[i],
              as400, originalInfo_);
          boolean result = setupNewConnection(connection, reconnectUrls_[i], originalException, (i == 0));
          if (JDTrace.isTraceOn()) { 
            JDTrace.logInformation(this, "findNewConnection connectionComplete"); 
          }
          
          return result; 
          
        } catch (SQLException e) {
          if (throwException_) {
            throwException_ = false; 
            throw e; 
          }
          // Unable to connect keep trying
          // Trace the exception anyway
          savedException = e; 
          exceptions[i] = e;
          if (JDTrace.isTraceOn())
            JDTrace.logException(this, "Unable to connect to system i=" + i
                + " as400=" + as400, e); // @J3a

        }
      }
      // At this point we were unable to find a connection. Wait for the specified 
      // delay time. 
      retryCount--;
      // If we started above 0 then try again before going into the retry path
      if (searchStart > 0) {
        retryCount++;
        searchStart = 0;
      } else {
        long retryDelayMilliseconds = 0;
        long retryElaspedMilliseconds = System.currentTimeMillis()
            - retryStartMilliseonds;
        // Handle the default wait behavior
        if (maxRetriesForClientReroute_ < 0
            && retryIntervalForClientReroute_ < 0) {
          retryDelayMilliseconds = delayMilliseconds - retryElaspedMilliseconds;
          delayMilliseconds = delayMilliseconds + delayMilliseconds / 2;
          long remainingMilliseconds = 600000 + startMilliseconds
              - System.currentTimeMillis();
          if (remainingMilliseconds < 0) {
            retryCount = 0;
            retryDelayMilliseconds = 0;
          } else {
            if (remainingMilliseconds < retryDelayMilliseconds
                + retryElaspedMilliseconds) {
              retryDelayMilliseconds = remainingMilliseconds
                  - retryElaspedMilliseconds;
              if (retryDelayMilliseconds < 0) {
                // Just try immediately, one last time
                retryDelayMilliseconds = 0;
              }
            }
          }

        } else {
          retryDelayMilliseconds = delayMilliseconds - retryElaspedMilliseconds;
        }

        if (retryDelayMilliseconds > 0) {
          try {
            if (JDTrace.isTraceOn()) {
              JDTrace.logInformation(this, "findNewConnection sleeping for "+retryDelayMilliseconds+" ms");
            }
            Thread.sleep(retryDelayMilliseconds);
          } catch (InterruptedException e) {
            // If we are interrupted, just give up and throw original exception
            if (originalException != null) { 
              if (JDTrace.isTraceOn()) {
                JDTrace.logInformation(this, "findNewConnection interrupted and throwing originalException "+originalException); 
              }
              throw (originalException);
            } else {
              if (savedException != null) { 
                if (JDTrace.isTraceOn()) {
                  JDTrace.logInformation(this, "findNewConnection interrupted and throwing savedException "+savedException); 
                }
                throw savedException; 
              } else {
                JDError.throwSQLException(this, JDError.EXC_CONNECTION_NONE, "INTERNAL_ERROR"); 
              }
            }
          }
        }
      }
    } /* while retrying */ 
    
    if (originalException != null) {
      if (JDTrace.isTraceOn()) {
        JDTrace.logInformation(this, "findNewConnection throwing originalException "+originalException); 
      }

      throw (originalException);
    } else {
      if (savedException != null) { 
        if (JDTrace.isTraceOn()) {
          JDTrace.logInformation(this, "findNewConnection throwing savedException "+savedException); 
        }
        throw savedException; 
      } else {
        JDError.throwSQLException(this, JDError.EXC_CONNECTION_NONE, "INTERNAL_ERROR"); 
        throw new SQLException("DEAD_CODE_NOT_REACHABLE"); 
      }
    }
    
  }
  /**
   * Determine if an SQL exception should cause the connection to switch to
   * another server. If so, switch to the new connection and return the
   * appropriate SQLException (if any)
   * 
   * 
   * @param e
   * @return true if the connection is in a state where the method can be
   *         retried.
   * @throws SQLException  If a database error occurs.
   */
  boolean handleException(SQLException e) throws SQLException {
    if (JDTrace.isTraceOn()) { 
      JDTrace.logInformation(this, " handleException("+e+")"); 
    }

    if (doNotHandleErrors_ || inFinalizer_ ) {
      throw e; 
    }
    
    int sqlCode = e.getErrorCode(); 
    String sqlState = e.getSQLState(); 
    if (((sqlCode == -99999) &&
        ((JDError.EXC_COMMUNICATION_LINK_FAILURE.equals(sqlState)) ||
        (JDError.EXC_CONNECTION_UNABLE.equals(sqlState))))
        || ( sqlCode == -7061  && should7061Reconnect(e))
        ) {
      // We do not use EXC_CONNECTION_NONE, since that is what is returned
      // after the connection has been closed or aborted.
      // 
      // We have been disconnected attempt to reconnect
      // Reconnect will return false if unable to reconnect. 
      // It will return true if it was able to seamlessly reconnect.
      // Otherwise it will throw the exception SQL4498 indicating that
      // the connection was re-established. 
      if (reconnect(e)) {
        return true; 
      }
    } else {
      throw e;
    }
    
    throw e;

  }
  
  /* Should the connection reconnect for the SQL7061 exception */
  /* Error code 71 means a new connection should be obtained */
  /* @X1A */ 
  private boolean should7061Reconnect(SQLException e) {
    String message = e.getMessage();
    if (message.indexOf(" 71.") > 0) {
      return true; 
    } else {
      return false; 
    }
  }


  /**
   * Like handleException but only returns an SQLClientInfoException in 
   * JDBC 4.0. 
   * @param e
   * @return true if the connection is in a state where the method can be
   *         retried. 
   * @throws SQLException  If a database error occurs.
   */
  
  boolean handleSQLClientInfoException(
      /* ifdef JDBC40
      SQLClientInfoException
    endif */
      /* ifndef JDBC40 */
      SQLException
      /* endif  */
      
      
      e ) throws 
  /* ifdef JDBC40
  SQLClientInfoException
endif */
  /* ifndef JDBC40 */
  SQLException
  /* endif  */

  {

    try { 
      return handleException(e); 
    } catch ( SQLException e2 ) {         
/* ifdef JDBC40
      if (e2 instanceof SQLClientInfoException) {
         throw (SQLClientInfoException) e2; 
      } else {
         JDError.throwSQLClientInfoException(
         this,
         e2.getSQLState(),
         e2,
         e.getFailedProperties());
         throw e; 
      }
endif */
  /* ifndef JDBC40 */
    throw e2;
  /* endif  */
    }
    
  }
  
  public void cancel(int id) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.cancel(id);
        retryOperation = false;
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
  }

  public synchronized void checkAccess(JDSQLStatement sqlStatement) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.checkAccess(sqlStatement);
        retryOperation = false;
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
  }

  public void checkCancel() {
    currentConnection_.checkCancel();
  }

  public synchronized boolean checkHoldabilityConstants(int holdability) {
    return currentConnection_.checkHoldabilityConstants(holdability);
  }

  public synchronized void checkOpen() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.checkOpen();
        retryOperation = false;
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized void clearWarnings() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.clearWarnings();
        retryOperation = false;
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized void close() throws SQLException {
    boolean retryOperation = true;
    int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT;
    while (retryOperation) {
      try {
        currentConnection_.close();
        retryOperation = false;
      } catch (SQLException e) {
        if (retryCount > 0) { 
           topLevelApi_ = true; 
        }
        retryOperation = handleException(e);
        retryCount--; 
      } finally {
        topLevelApi_ = false; 
      }
    }

    
  }

  public synchronized void handleAbort() {
    currentConnection_.handleAbort();

  }

  public synchronized void commit() throws SQLException {
    boolean retryOperation = true;
    int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT;
    while (retryOperation) {
      try {
        currentConnection_.commit();
        retryOperation = false;
      } catch (SQLException e) {
        if (retryCount > 0) { 
           topLevelApi_ = true; 
        }
        retryOperation = handleException(e);
        retryCount--; 
      } finally {
        topLevelApi_ = false; 
      }
    }
    
    if (affinityFailbackInterval_ > 0) {
      if (affinityOnAlternate_) {
        if (System.currentTimeMillis() > affinityFailbackTime_) {
           try { 
              if (JDTrace.isTraceOn())
                JDTrace.logInformation(this, "Attempting to reconnect because affinityFailbackTime_="+affinityFailbackTime_);
              reconnect(null); 
           } catch (SQLException e) { 
              // We do not want to fail the commit since we know it already worked
              // Just log the exception and continue on.  There will be an error 
              // when we try to use the connection. 
             if (JDTrace.isTraceOn())
               JDTrace.logException(this, "Exception from commit.reconnect", e); 
           
           }
        }
      }
    }

  }

  public synchronized void setCheckStatementHoldability(boolean check) {
    currentConnection_.setCheckStatementHoldability(check);
  }

  public synchronized int correctResultSetType(int resultSetType, int resultSetConcurrency)
      throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.correctResultSetType(resultSetType,
            resultSetConcurrency);
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return 0;
  }

  public synchronized Statement createStatement() throws SQLException {
    
    boolean retryOperation = true;
    int retryCount = AS400JDBCConnectionRedirect.SEAMLESS_RETRY_COUNT;
    while (retryOperation) {
      try {
        AS400JDBCStatement newStatement = (AS400JDBCStatement) currentConnection_.createStatement(this);
        if (enableSeamlessFailover_) { 
          return new AS400JDBCStatementRedirect(newStatement);
        } else {
          return newStatement; 
        }
      } catch (SQLException e) {
        if (retryCount > 0) { 
           topLevelApi_ = true; 
        }
        retryOperation = handleException(e);
        retryCount--; 
      } finally {
        topLevelApi_ = false; 
      }
    }
    // This code will never be reached
    // When retry count = 0 then topLevelApi_ is false and
    // handleException will throw an exception. 
    return null; 
    
    
  }

  public synchronized Statement createStatement(int resultSetType, int resultSetConcurrency)
      throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        AS400JDBCStatement newStatement =  (AS400JDBCStatement) currentConnection_.createStatement(this, resultSetType,
            resultSetConcurrency);
        if (enableSeamlessFailover_) { 
          return new AS400JDBCStatementRedirect(newStatement);
        } else {
          return newStatement; 
        }

      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;
  }

  public synchronized Statement createStatement(int resultSetType, int resultSetConcurrency,
      int resultSetHoldability) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        AS400JDBCStatement newStatement = (AS400JDBCStatement) currentConnection_.createStatement(this, resultSetType,
            resultSetConcurrency, resultSetHoldability);
        if (enableSeamlessFailover_) { 
          return new AS400JDBCStatementRedirect(newStatement);
        } else {
          return newStatement; 
        }

      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public void debug(DBBaseRequestDS request) {
    currentConnection_.debug(request);

  }

  public void debug(DBReplyRequestedDS reply) {
    currentConnection_.debug(reply);

  }

  public synchronized AS400Impl getAS400() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.getAS400();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */

    return null;

  }

  public synchronized boolean getAutoCommit() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.getAutoCommit();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return false;
  }

  public synchronized String getCatalog() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.getCatalog();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */

    return null;

  }

  public synchronized int getConcurrentAccessResolution() {
    return currentConnection_.getConcurrentAccessResolution();

  }

  public synchronized ConvTable getConverter(int ccsid) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.getConverter(ccsid);
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null; 

  }

  public synchronized int getDataCompression() {
    return currentConnection_.getDataCompression();

  }

  public synchronized String getDefaultSchema() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.getDefaultSchema();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null; 

  }

  public synchronized String getDefaultSchema(boolean returnRawValue) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.getDefaultSchema(returnRawValue);
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null; 

  }

  public synchronized int getHoldability() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.getHoldability();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return 0; 

  }

  public synchronized int getID() {
    return currentConnection_.getID();

  }

  public synchronized int getInternalHoldability() {
    return currentConnection_.getInternalHoldability();

  }

  public synchronized DatabaseMetaData getMetaData() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        AS400JDBCDatabaseMetaData metadata = (AS400JDBCDatabaseMetaData) currentConnection_.getMetaData();
        
        // Make sure the metadata references this connection
        metadata.connection_ = this; 
        
        return metadata; 
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null; 

  }

  public synchronized JDProperties getProperties() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.getProperties();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null; 

  }

  public synchronized String getServerJobIdentifier() {
    return currentConnection_.getServerJobIdentifier();

  }

  public synchronized int getServerFunctionalLevel() {
    return currentConnection_.getServerFunctionalLevel();

  }

  public synchronized AS400 getSystem() {
    return currentConnection_.getSystem();

  }

  public synchronized int getTransactionIsolation() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.getTransactionIsolation();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return 0;
  }

  public synchronized JDTransactionManager getTransactionManager() {
    return currentConnection_.getTransactionManager();

  }

  public synchronized Map getTypeMap() throws SQLException {
    return currentConnection_.getTypeMap();

  }

  public synchronized int getUnusedId(int resultSetType) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.getUnusedId(resultSetType);
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return 0;
  }

  public synchronized boolean getMustSpecifyForUpdate() {
    return currentConnection_.getMustSpecifyForUpdate();
  }

  public synchronized String getURL() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.getURL();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;
  }

  public synchronized String getUserName() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.getUserName();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized int getVRM() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.getVRM();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return 0;

  }

  public synchronized SQLWarning getWarnings() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.getWarnings();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized boolean isCursorNameUsed(String cursorName) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.isCursorNameUsed(cursorName);
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return false;
  }

  public synchronized boolean isClosed() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.isClosed();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return false;
  }

  public synchronized boolean isReadOnly() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.isReadOnly();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return false;

  }

  public synchronized boolean isReadOnlyAccordingToProperties() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.isReadOnlyAccordingToProperties();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return false;

  }

  public synchronized void markCursorsClosed(boolean isRollback) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.markCursorsClosed(isRollback);
        retryOperation = false; 
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized void markStatementsClosed() {
    currentConnection_.markStatementsClosed();

  }

  public synchronized String makeGeneratedKeySelectStatement(String sql,
      int[] columnIndexes, String[] columnNames) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.makeGeneratedKeySelectStatement(sql,
            columnIndexes, columnNames);
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized String makeGeneratedKeySelectStatement(String sql) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.makeGeneratedKeySelectStatement(sql);
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized String nativeSQL(String sql) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.nativeSQL(this, sql);
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized void notifyClose(AS400JDBCStatement statement, int id)
      throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.notifyClose(statement, id);
        retryOperation = false;
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized void postWarning(SQLWarning sqlWarning) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.postWarning(sqlWarning);
        retryOperation = false;
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized CallableStatement prepareCall(String sql) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        AS400JDBCCallableStatement newStatement = (AS400JDBCCallableStatement) currentConnection_.prepareCall(this, sql);
        newStatement.setSaveParameterValues(true); 
        if (enableSeamlessFailover_) { 
          return new AS400JDBCCallableStatementRedirect(newStatement);
        } else {
          return newStatement; 
        }

      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized CallableStatement prepareCall(String sql, int resultSetType,
      int resultSetConcurrency) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        AS400JDBCCallableStatement stmt=
        (AS400JDBCCallableStatement) currentConnection_.prepareCall(this, sql, resultSetType,
            resultSetConcurrency);
        stmt.setSaveParameterValues(true); 
        if (enableSeamlessFailover_) { 
          return new AS400JDBCCallableStatementRedirect(stmt);
        } else {
          return stmt; 
        }

        
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized CallableStatement prepareCall(String sql, int resultSetType,
      int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        AS400JDBCCallableStatement stmt=
            (AS400JDBCCallableStatement) currentConnection_.prepareCall(this, sql, resultSetType,
            resultSetConcurrency, resultSetHoldability);
        stmt.setSaveParameterValues(true); 
       
        if (enableSeamlessFailover_) { 
          return new AS400JDBCCallableStatementRedirect(stmt);
        } else {
          return stmt; 
        }
        
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized PreparedStatement prepareStatement(String sql) throws SQLException {
    boolean retryOperation = true;
    int retryCount = SEAMLESS_RETRY_COUNT; 
    while (retryOperation) {
      try {
        AS400JDBCPreparedStatement newStatement = (AS400JDBCPreparedStatement) currentConnection_.prepareStatement(this, sql);
        newStatement.setSaveParameterValues(true); 
        if (enableSeamlessFailover_) { 
          return new AS400JDBCPreparedStatementRedirect(newStatement);
        } else {
          return newStatement; 
        }

      } catch (SQLException e) {
          try { 
            retryOperation = handleException(e);
          } catch (AS400JDBCTransientException e2) {
            if (currentConnection_.canSeamlessFailover()) {
              retryCount--; 
              if (retryCount >= 0 ) {
                retryOperation = true;
              } else {
                throw e2; 
              }
            } else {
              throw e2; 
            }
          }
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
      throws SQLException {
    boolean retryOperation = true;
    int retryCount = SEAMLESS_RETRY_COUNT; 

    while (retryOperation) {
      try {
        AS400JDBCPreparedStatement newStatement = (AS400JDBCPreparedStatement) currentConnection_.prepareStatement(this, sql, autoGeneratedKeys);
        newStatement.setSaveParameterValues(true); 
        if (enableSeamlessFailover_) { 
          return new AS400JDBCPreparedStatementRedirect(newStatement);
        } else {
          return newStatement; 
        }

      } catch (SQLException e) {
        try { 
          retryOperation = handleException(e);
        } catch (AS400JDBCTransientException e2) {
          if (currentConnection_.canSeamlessFailover()) {
            retryCount--; 
            if (retryCount >= 0 ) {
              retryOperation = true;
            } else {
              throw e2; 
            }
          } else {
            throw e2; 
          }
        }
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized PreparedStatement prepareStatement(String sql, int resultSetType,
      int resultSetConcurrency) throws SQLException {
    boolean retryOperation = true;
    int retryCount = SEAMLESS_RETRY_COUNT; 
    while (retryOperation) {
      try {
        AS400JDBCPreparedStatement newStatement = (AS400JDBCPreparedStatement) currentConnection_.prepareStatement(this, sql, resultSetType,
            resultSetConcurrency);
        newStatement.setSaveParameterValues(true); 

        if (enableSeamlessFailover_) { 
          return new AS400JDBCPreparedStatementRedirect(newStatement);
        } else {
          return newStatement; 
        }

      } catch (SQLException e) {
        try { 
          retryOperation = handleException(e);
        } catch (AS400JDBCTransientException e2) {
          if (currentConnection_.canSeamlessFailover()) {
            retryCount--; 
            if (retryCount >= 0 ) {
              retryOperation = true;
            } else {
              throw e2; 
            }
          } else {
            throw e2; 
          }
        }
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized PreparedStatement prepareStatement(String sql, int resultSetType,
      int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    boolean retryOperation = true;
    int retryCount = SEAMLESS_RETRY_COUNT; 
    while (retryOperation) {
      try {
        AS400JDBCPreparedStatement newStatement =  (AS400JDBCPreparedStatement) currentConnection_.prepareStatement(this, sql, resultSetType,
            resultSetConcurrency, resultSetHoldability);
        newStatement.setSaveParameterValues(true); 

        if (enableSeamlessFailover_) { 
          return new AS400JDBCPreparedStatementRedirect(newStatement);
        } else {
          return newStatement; 
        }

      } catch (SQLException e) {
        try { 
          retryOperation = handleException(e);
        } catch (AS400JDBCTransientException e2) {
          if (currentConnection_.canSeamlessFailover()) {
            retryCount--; 
            if (retryCount >= 0 ) {
              retryOperation = true;
            } else {
              throw e2; 
            }
          } else {
            throw e2; 
          }
        }
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized PreparedStatement prepareStatement(String sql, int[] columnIndexes)
      throws SQLException {
    boolean retryOperation = true;
    int retryCount = SEAMLESS_RETRY_COUNT; 
    while (retryOperation) {
      try {
        AS400JDBCPreparedStatement newStatement = (AS400JDBCPreparedStatement) currentConnection_.prepareStatement(this, sql, columnIndexes);
        newStatement.setSaveParameterValues(true); 
        if (enableSeamlessFailover_) { 
          return new AS400JDBCPreparedStatementRedirect(newStatement);
        } else {
          return newStatement; 
        }

      } catch (SQLException e) {
        try { 
          retryOperation = handleException(e);
        } catch (AS400JDBCTransientException e2) {
          if (currentConnection_.canSeamlessFailover()) {
            retryCount--; 
            if (retryCount >= 0 ) {
              retryOperation = true;
            } else {
              throw e2; 
            }
          } else {
            throw e2; 
          }
        }
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized PreparedStatement prepareStatement(String sql, String[] columnNames)
      throws SQLException {
    boolean retryOperation = true;
    int retryCount = SEAMLESS_RETRY_COUNT; 
    while (retryOperation) {
      try {
        AS400JDBCPreparedStatement newStatement = (AS400JDBCPreparedStatement) currentConnection_.prepareStatement(this, sql, columnNames);
        newStatement.setSaveParameterValues(true); 
        if (enableSeamlessFailover_) { 
          return new AS400JDBCPreparedStatementRedirect(newStatement);
        } else {
          return newStatement; 
        }

      } catch (SQLException e) {
        try { 
          retryOperation = handleException(e);
        } catch (AS400JDBCTransientException e2) {
          if (currentConnection_.canSeamlessFailover()) {
            retryCount--; 
            if (retryCount >= 0 ) {
              retryOperation = true;
            } else {
              throw e2; 
            }
          } else {
            throw e2; 
          }
        }
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized void processSavepointRequest(String savepointStatement)
      throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.processSavepointRequest(savepointStatement);
        retryOperation = false;
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized void pseudoClose() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.pseudoClose();
        retryOperation = false; 
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized void releaseSavepoint(Savepoint savepoint) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.releaseSavepoint(savepoint);
        retryOperation = false;
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized void rollback() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.rollback();
        retryOperation = false; 
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized void rollback(Savepoint savepoint) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.rollback(savepoint);
        retryOperation = false;
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized void send(DBBaseRequestDS request) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.send(request);
        retryOperation = false; 
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized void send(DBBaseRequestDS request, int id) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.send(request, id);
        retryOperation = false;
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized void send(DBBaseRequestDS request, int id, boolean leavePending)
      throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.send(request, id, leavePending);
        retryOperation = false; 
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized void sendAndHold(DBBaseRequestDS request, int id) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.sendAndHold(request, id);
        retryOperation = false;
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized DBReplyRequestedDS sendAndReceive(DBBaseRequestDS request)
      throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.sendAndReceive(request);
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized DBReplyRequestedDS sendAndReceive(DBBaseRequestDS request, int id)
      throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.sendAndReceive(request, id);
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized DBReplyRequestedDS sendAndMultiReceive(DBBaseRequestDS request)
      throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.sendAndMultiReceive(request);
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized DBReplyRequestedDS receiveMoreData() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.receiveMoreData();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized void setAutoCommit(boolean autoCommit) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.setAutoCommit(autoCommit);
        retryOperation = false;
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    autoCommitSet_ = true; 
    autoCommitSetting_ = autoCommit; 

  }

  public synchronized void setCatalog(String catalog) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.setCatalog(catalog);
        retryOperation = false;
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized void setConcurrentAccessResolution(int concurrentAccessResolution)
      throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_
            .setConcurrentAccessResolution(concurrentAccessResolution);
        retryOperation = false;
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized void setDB2eWLMCorrelator(byte[] bytes) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.setDB2eWLMCorrelator(bytes);
        retryOperation = false;
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized void setDRDA(boolean drda) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.setDRDA(drda);
        retryOperation = false;
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized void setHoldability(int holdability) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.setHoldability(holdability);
        retryOperation = false;
        holdability_ = holdability; 
        holdabilitySet_ = true; 
        
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized void setProperties(JDDataSourceURL dataSourceUrl,
      JDProperties properties, AS400 as400, Properties info) throws SQLException {
    // We cannot retry this operation since this establishes the connection


      try {
        originalDataSourceUrl_ = dataSourceUrl; 
        originalProperties_ = properties; 
        originalAs400 = as400; 
        originalInfo_ = info; 
        if (info == null) {
          originalInfo_ = new Properties(); 
          originalInfo_.put("user", as400.getUserId());
        }
        setupRetryInformation(); 
        currentConnection_.setProperties(dataSourceUrl, properties, as400, info);
        currentUrl_ = dataSourceUrl; 
        // Check for alternative server information @X1A
        String alternateServer = currentConnection_.getAlternateServer();
        if (alternateServer != null) {  // @X1A
           setupAlternateServers(alternateServer);  
        }
        
      } catch (SQLException e) {
        try { 
            handleException(e);
        } catch (SQLException e2) { 
          if (e2.getErrorCode() == -4498) {
            // Connection was successfully established.
            // Just let the connection continue. 
          } else {
            throw e2; 
          }
        }
      }

  }

  public synchronized void setProperties(JDDataSourceURL dataSourceUrl,
      JDProperties properties, AS400Impl as400) throws SQLException {
    originalDataSourceUrl_ = dataSourceUrl; 
    currentUrl_ = dataSourceUrl; 
    originalProperties_ = properties; 
 

    setupRetryInformation(); 
    
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        
        currentConnection_.setProperties(dataSourceUrl, properties, as400);
        retryOperation = false;
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized boolean ignoreWarning(String sqlState) {
    return currentConnection_.ignoreWarning(sqlState);

  }

  public synchronized boolean ignoreWarning(SQLWarning warning) {
    return currentConnection_.ignoreWarning(warning);

  }

  public synchronized void setProperties(JDDataSourceURL dataSourceUrl,
      JDProperties properties, AS400Impl as400, boolean newServer,
      boolean skipSignonServer) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        originalDataSourceUrl_ = dataSourceUrl; 
        originalProperties_ = properties; 
        currentConnection_.setProperties(dataSourceUrl, properties, as400,
            newServer, skipSignonServer);
        retryOperation = false;
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized void setReadOnly(boolean readOnly) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.setReadOnly(readOnly);
        retryOperation = false;
        readOnlySet_ = true; 
        readOnly_ = readOnly; 
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized Savepoint setSavepoint() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.setSavepoint();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized Savepoint setSavepoint(String name) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.setSavepoint(name);
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized Savepoint setSavepoint(String name, int id) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.setSavepoint(name, id);
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized void setServerAttributes() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.setServerAttributes();
        retryOperation = false;
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized void setSystem(AS400 as400) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        originalAs400 = as400; 
        currentConnection_.setSystem(as400);
        retryOperation = false;
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized void setTransactionIsolation(int level) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.setTransactionIsolation(level);
        retryOperation = false;
        transactionIsolationSet_ = true; 
        transactionIsolationSetting_ = level; 
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    
  }

 
  public synchronized void setTypeMap(Map typeMap) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.setTypeMap(typeMap);
        retryOperation = false;
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized boolean useExtendedFormats() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.useExtendedFormats();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return false;

  }

  public synchronized String[] getValidWrappedList() {
    return currentConnection_.getValidWrappedList();

  }

  public synchronized void setClientInfo(String name, String value) 
  /* ifdef JDBC40
  throws SQLClientInfoException
endif */
  /* ifndef JDBC40 */
  throws SQLException
  /* endif  */
      {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.setClientInfo(name, value);
        if (clientInfoHashtable_ == null) { 
          clientInfoHashtable_ = new Hashtable(); 
        }
        // Null means to reset to empty
        if (value == null) value = ""; 
        
        clientInfoHashtable_.put(name, value); 
        retryOperation = false;
      } catch (
          
          /* ifdef JDBC40
           SQLClientInfoException
        endif */
          /* ifndef JDBC40 */
          SQLException
          /* endif  */
          
          e) {
        retryOperation = handleSQLClientInfoException(e);
      }
    }

  }

  public synchronized void setClientInfo(Properties properties) 
  /* ifdef JDBC40
  throws SQLClientInfoException
endif */
  /* ifndef JDBC40 */
  throws SQLException
  /* endif  */

  {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.setClientInfo(properties);
        if (clientInfoHashtable_ == null) { 
          clientInfoHashtable_ = new Hashtable(); 
        }
        String[] possibleNames = {
            "ApplicationName",
            "ClientUser",
            "ClientAccounting",
            "ClientHostname", 
            "ClientProgramID"
        };
        
        for (int i = 0; i < possibleNames.length; i++) { 
          String name = possibleNames[i]; 
          String value = properties.getProperty(name); 
          if (value == null) { 
            value = ""; 
          }
          clientInfoHashtable_.put(name, value); 
        }

        
        retryOperation = false;
        
      } catch (
          /* ifdef JDBC40
          SQLClientInfoException
       endif */
         /* ifndef JDBC40 */
         SQLException
         /* endif  */
          
          e) {
        retryOperation = handleSQLClientInfoException(e);
      }
    }

  }

  public synchronized String getClientInfo(String name) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.getClientInfo(name);
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;

  }

  public synchronized Properties getClientInfo() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.getClientInfo();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;
  }

  public synchronized Clob createClob() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.createClob();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;
  }

  public synchronized Blob createBlob() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.createBlob();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;
  }

  public synchronized Array createArrayOf(String typeName, Object[] elements)
      throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.createArrayOf(typeName, elements);
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;
  }

  public synchronized Struct createStruct(String typeName, Object[] attributes)
      throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.createStruct(typeName, attributes);
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;
  }

  public synchronized void setDBHostServerTrace(boolean trace) {
    currentConnection_.setDBHostServerTrace(trace);

  }

  public synchronized boolean doUpdateDeleteBlocking() {
    return currentConnection_.doUpdateDeleteBlocking();

  }

  public synchronized int getMaximumBlockedInputRows() {
    return currentConnection_.getMaximumBlockedInputRows();

  }

  public synchronized String getSchema() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.getSchema();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return null;
  }

  public synchronized void setNetworkTimeout(int timeout) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.setNetworkTimeout(timeout);
        retryOperation = false;
        networkTimeoutSet_ = true; 
        networkTimeout_ = timeout; 
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized int getNetworkTimeout() throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.getNetworkTimeout();
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); /* should not be reached */
    return 0;

  }

  public synchronized void setSchema(String schema) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        currentConnection_.setSchema(schema);
        retryOperation = false;
        if (setCommands_ == null) { 
          setCommands_ = new Vector(); 
        }
        setCommands_.add("SET SCHEMA "+schema); 
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }

  }

  public synchronized boolean isQueryTimeoutMechanismCancel() {
    return currentConnection_.isQueryTimeoutMechanismCancel();

  }

  public synchronized void setupVariableFieldCompression() {
    currentConnection_.setupVariableFieldCompression();

  }

  public synchronized boolean useVariableFieldCompression() {
    return currentConnection_.useVariableFieldCompression();

  }

  public synchronized boolean useVariableFieldInsertCompression() {
    return currentConnection_.useVariableFieldInsertCompression();

  }

  public synchronized void setDisableCompression(boolean disableCompression_) {
    currentConnection_.setDisableCompression(disableCompression_);

  }

  public synchronized void dumpStatementCreationLocation() {
    currentConnection_.dumpStatementCreationLocation();

  }

  public synchronized boolean testDataTruncation(AS400JDBCStatement statementWarningObject,
      AS400JDBCResultSet resultSetWarningObject, int parameterIndex,
      boolean isParameter, SQLData data, JDSQLStatement sqlStatement)
      throws SQLException {
    boolean checkRawBytes = false; 
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        checkRawBytes = currentConnection_.testDataTruncation(statementWarningObject,
            resultSetWarningObject, parameterIndex, isParameter, data,
            sqlStatement);
        retryOperation = false; 
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    return checkRawBytes; 

  }

  public synchronized ConvTable getConverter() {
    return currentConnection_.getConverter();
  }

  public synchronized void setLastServerSQLState(String lastSqlState) {
    currentConnection_.setLastServerSQLState(lastSqlState);

  }

  public synchronized String getLastServerSQLState() {
    return currentConnection_.getLastServerSQLState();
  }

  public synchronized ConvTable getPackageCCSID_Converter() {
    return currentConnection_.getPackageCCSID_Converter();
  }

  public synchronized void finalize() {
  }

  public synchronized boolean getReadOnly() {
    return currentConnection_.getReadOnly(); 
  }

  public synchronized boolean getCheckStatementHoldability() {
    return currentConnection_.getCheckStatementHoldability(); 
  }

  public synchronized String toString() {
   return currentConnection_.toString(); 
  }

  int getNewAutoCommitSupport() {
    return currentConnection_.getNewAutoCommitSupport(); 
   
  }

  void addSetCommand(String command) {
    if (setCommands_ == null) {
      setCommands_ = new Vector(); 
    }
   setCommands_.addElement(command); 
  }
  
  /**
   * Can the operation be retried after EXC_CONNECTION_REESTABLISHED. 
   */
  boolean canSeamlessFailover() {
    return lastConnectionCanSeamlessFailover_; 
  }

  String[] reconnectUrlStrings_ = null; 
  public synchronized String[] getReconnectURLs() {
    if (reconnectUrlStrings_ == null) { 
       reconnectUrlStrings_ = new String[reconnectUrls_.length];
       for (int i = 0; i < reconnectUrls_.length; i++ ) { 
         reconnectUrlStrings_[i] = reconnectUrls_[i].toString();
       }
    } 
    return reconnectUrlStrings_;
  }
   
  
  
  
  
  /* ifdef JDBC40
  public synchronized boolean isValid(int timeout) throws SQLException {
    boolean retryOperation = true;
    while (retryOperation) {
      try {
        return currentConnection_.isValid(timeout);
      } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); 
    return false;
  
  }
  endif */

   /*ifdef JDBC40
  public synchronized NClob createNClob() throws SQLException {
      boolean retryOperation = true;
      while (retryOperation) {
        try {
          return currentConnection_.createNClob(); 
        } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); 
    return null;

  }
endif */

/*ifdef JDBC40
  public synchronized SQLXML createSQLXML() throws SQLException {
        boolean retryOperation = true;
      while (retryOperation) {
        try {
          return currentConnection_.createSQLXML(); 
        } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
    JDError.throwSQLException(JDError.EXC_INTERNAL); 
    return null;
  }
  endif */

  
  /* ifdef JDBC40
  public synchronized void abort(Executor executor) throws SQLException  {
          boolean retryOperation = true;
      while (retryOperation) {
        try {
          currentConnection_.abort(executor);
          retryOperation=false;  
        } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
}
    
endif */

/* ifdef JDBC40
  public synchronized void setNetworkTimeout(Executor executor, int milliseconds)
      throws SQLException  {
                boolean retryOperation = true;
      while (retryOperation) {
        try {
          currentConnection_.setNetworkTimeout(executor, milliseconds);
          retryOperation=false;  
          networkTimeoutSet_ = true; 
          networkTimeout_ = milliseconds; 
        } catch (SQLException e) {
        retryOperation = handleException(e);
      }
    }
}

      
endif */

  
}
