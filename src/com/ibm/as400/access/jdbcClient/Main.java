///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: AS400JDBCPreparedStatement.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2006 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access.jdbcClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.CRC32;

import com.ibm.as400.access.JVMInfo;


/**
 * Main class for the jdbcClient program. This sample client uses JDBC to connect to
 * the specified URL using the specified userid and password.
 *
 * This program is provided AS/IS and formal support will NOT be provided.
 * This program should not be used in a production environment.
 *
 * Because this program is provided AS/IS, no consideration is made for NLS support.
 *
 */
public class Main implements Runnable {

  public static String[] knownDrivers = { "com.ibm.as400.access.AS400JDBCDriver",
                                          "com.ibm.db2.jdbc.app.DB2Driver",
                                          "com.ibm.jtopenlite.database.jdbc.JDBCDriver",
                                          };

  public static String promptString = ">";
  public static String usage = "Usage:  java com.ibm.as400.access.jdbcClient.Main <jdbcUrl> <userid> <password>";

  public static String[] commandHelp = {
      "com.ibm.as400.access.jdbcClient.Main executes SQL commands using a JDBC connection.  ",
      "  This program is provided AS IS and formal support will NOT be provided.",
      "  This program should not be used in a production environment.",
      "",
      "Parameters are passed to a CALL procedure by using the following ",
      "CALL ... -- INPARM [p1]....    Calls the stored procedure with the specified parameters",
      "",
      "Using CL:  [as400 command] will use QSYS.QCMDEXEC to execute the as400 command ",
      "",
      "Besides SQL COMMANDS, the following COMMANDS and SUBCOMMANDS are available",
      "",
      "!USAGE                          Displays this information",
      "!HELP                           Displays this information",
      "!PREPARE [sql statement]        prepares an sql statement",
      "!EXECUTEQUERY                   Invokes executeQuery on the prepared statement",
      "!EXECUTEUPDATE                  Invokes executeUpdate on the prepared statement",
      "!SETPARM [index],[value]        Sets the parameter for the sql statement",
      "!SETPARMFROMVAR [index],[var]   Sets the parameter using a variable",
      "!SETRESULTSETTYPE [..]          Sets the results set type for prepare",
      "!SETRESULTSETCONCURRENCY [..]   ..",
      "!SETRESULTSETHOLDABILITY [..]   ..",
      "!REUSE STATEMENT [true|false]   Controls whethe the stmt object is reused",
      "!ECHO [string]                  Echos the string",
      "!ECHOCOMMAND [true|false]       Should the input command be echod.",
      "!PRINTSTACKTRACE [true|false]   Should the stack trace be printed for SQLExceptions.",
      "-- [string]                     Specifies a comment",
      "!SETQUERYTIMEOUT [number]       Sets the query timeout for subsequent statements",
      "!RESET CONNECTION PROPERTIES    Resets connection properties",
      "!ADD CONNECTION PROPERTY        Set properties to be used on subsequent connects",
      "!REUSE CONNECTION [true|false]  Should the connection be reused by connect to ",
      "!CONNECT TO URL [URL] [USERID=..] [PASSWORD=...]  Connect using the URL",
      "!CONNECT TO SCHEMA [schema]            Reconnect using the current URL to the specified schema",
      "!CONNECT RESET                  Closes the current connection",
      "!EXISTFILE                      Indicates if the specified file exists (on the client)",
      "!GC                             Force the Java garbage collector to run",
      "!OUTPUT FORMAT [xml | html]     Set the output format to include XML or HTML",
      "!SHOWMIXEDUX [true | false]     Set if mixed UX strings will be displayed",
      "!SET AUTOCOMMIT [true|false]    Sets the autocommit value",
      "!SET TRANSACTIONISOLATION [VALUE] Sets the autocommit value",
      "                                 Supported values are ",
      "                               TRANSACTION_READ_UNCOMMITTED",
      "                               TRANSACTION_READ_COMMITTED",
      "                               TRANSACTION_REPEATABLE_READ",
      "                               TRANSACTION_SERIALIZABLE",
      "!GETSERVERJOBNAME               Returns connection.getServerJobName",
      "!CLOSESTATEMENTRS [on|off]      Close statement and result set after execution of query default off",
      "!MEASUREEXECUTE [on|off]        Measure time to do execute",
      "!CHARACTERDETAILS [on|off]      Turn on to see entire character details -- default of off",
      "!MANUALFETCH [on|off]           Set if manual fetch operations should be used",
      "!RS.NEXT,!RS.FIRST, !RS.LAST, !RS.PREVIOUS, !RS.ABSOLUTE pos, !RS.RELATIVE pos, !RS.BEFOREFIRST, !RS.AFTERLAST",
      "                               Call rs.next,... for manually fetching",
      "!DMD.GETCOLUMNS catalog, schemaPattern, tableNamePattern, columnNamePattern ",
      "!DMD.GETTABLES catalog, schemaPattern, tableNamePattern, type1 | type2",
      "!DMD.GETINDEXINFO catalog, schema, table, booleanUnique, booleanApproximate ",
      "!DMD.GETSCHEMAS",
      "!HISTORY.CLEAR                    Clears the stored history",
      "!HISTORY.SHOW                     Shows the history of commands",
      "!SETCLITRACE [true|false]         Sets CLI tracing for native JDBC driver -- valid V5R5 and later",
      "!SETDB2TRACE [0|1|2|3|4]          Sets jdbc tracing for native JDBC driver  -- valid V5R5 and later",

      "",
      "Parameters for prepared statements and callable statements may be specified in the following formats",
      "UX'....'                       Unicode string (in hexadecimal)",
      "X'....'                        Byte array (in hexademical)",
      "FILEBLOB=<filename>            A Blob retrieved from the named file",
      "FILECLOB=<filename>            A clob retrieved from the named file",
      "SAVEDPARM=<number>             A parameter from a previous CALL statement",
      "GEN_BYTE_ARRAY+<count>         A generated byte array of count bytes",
      "GEN_HEX_STRING+<count>         A generated hex string",
      "GEN_CHAR_ARRAY+<count>C<ccsid> A generated character string",
      "SQLARRAY[TYPE:e1:e2:...]       A JAVA.SQL.ARRAY type",
      "                               Types are String:BigDecimal:Date:Time:Timestamp:Blob:Clob:int:short:long:float:double:byteArray",
      "SQLARRAY[Date:e1 e2 ...]       A JAVA.SQL.ARRAY with data blank sep",
      "SQLARRAY[Timestamp:e1|e2 ...]  A JAVA.SQL.ARRAY with timestamp | sep",

      "",
      "The following prefixes are available",
      "!INVISIBLE:     The command and its results are not echoed",
      "!SILENT:        The results of the command are not echoed",
      "",

      "",
      "The following 'reflection' based commands are available",
      "!SETVAR [VARNAME] = [METHODCALL]  Sets a variable use a method.. i.e. ",
      "                                 SETVAR BLOB = RS.getBlob(1)",
      "!SETVAR [VARNAME] [PARAMETER SPECIFICATION] Sets a variable using a parameter specification",
      "!SETNEWVAR [VARNAME] = [CONSTRUCTORCALL]  Sets a variable by calling the contructor",
      "                                 SETNEWVAR DS = com.ibm.db2.jdbc.app.UDBDataSource()",
      "!SHOWVARMETHODS [VARNAME]         Shows the methods for a variable",

      "!CALLMETHOD [METHODCALL]          Calls a method on a variable",
      "  Hint:  To see a result set use CALLMETHOD com.ibm.as400.access.jdbcClient.Main.dispResultSet(RS)",
      "",
      "!THREAD [COMMAND]                 Runs a command in its own thread.",
      "!REPEAT [NUMBER] [COMMAND]        Repeat a command a number of times.",
      "" };

  String url_; /* URL for the current connection */
  String userid_; /* userid for the current connection */
  String password_; /* password for the current connection */
  boolean prompt_ = true; /* should prompting be used */
  boolean echoCommand_ = false; /* should command be echoed */
  boolean printStackTrace_ = false; /* should stack trace be printed */
  int queryTimeout_ = 0;
  boolean measureExecute_ = false;
  boolean manualFetch_ = false;
  int resultSetType_ = ResultSet.TYPE_FORWARD_ONLY;
  int resultSetConcurrency_ = ResultSet.CONCUR_READ_ONLY;
  int resultSetHoldability_ = ResultSet.HOLD_CURSORS_OVER_COMMIT;
  boolean jdk14_ = false;
  boolean jdk16_ = false;
  private boolean hideWarnings_ = false;
  private boolean toolboxDriver_ = false;

  Connection connection_;
  Statement stmt_;
  private int manualResultSetNumCols_;
  private ResultSet manualResultSet_;
  private String[] manualResultSetColumnLabel_;
  private int showLobThreshold_ = 4096;
  private boolean characterDetails_ = false;
  private int stringSampleSize_ = 256;
  private boolean showMixedUX_;
  private int[] manualResultSetColType_;
  private boolean closeStatementRS_;
  private PreparedStatement pstmt_;
  private String[] savedStringParm_  = new String[256];
  private boolean echoComments_ = false;
  private String urlArgs_="";
  private boolean debug_ = false;
  private String conLabel_;
  private CallableStatement cstmt_;
  private Vector threads_ = new Vector();

  boolean html_ = false;
  boolean xml_ = false;
  //
  // Optimization for using a connection pool
  //
  private boolean useConnectionPool_ = false;
  private boolean reuseStatement_ = false;
  private Connection poolConnection = null;
  private String poolUserId = null;
  private String poolPassword = null;
  private String poolUrl = null;
  private Hashtable connectionPool = new Hashtable();
  private Hashtable variables = new Hashtable();
  private int conCount;
  private String conName;
  private boolean silent;

  private Vector history = new Vector();

  void initializeDefaults() {

    //
    // Load drivers
    //
    for (int i = 0; i < knownDrivers.length; i++) {
      try {
        Class.forName(knownDrivers[i]);
      } catch (Exception e) {
        if (debug_ | printStackTrace_) {
          e.printStackTrace();
        }
      }
    }
    //
    // look for jdk1.4
    //
    jdk14_ = JVMInfo.isJDK14(); 
    jdk16_ = JVMInfo.isJDK16(); 
    
    addVariable("MAIN", this);

    //
    // Look for debug setting
    //
    String debug = System.getProperty("com.ibm.as400.access.jdbcClient.debug");
    if (debug != null) {
        debug = debug.toUpperCase();
        if (debug.equals("TRUE")) {
          debug_ = true;
    }
    }

    String moreDrivers = System.getProperty("com.ibm.as400.access.jdbcClient.drivers");
    while (moreDrivers != null) {
      String loadDriver = null;
      int colonIndex = moreDrivers.indexOf(":");
      if (colonIndex > 0) {
        loadDriver = moreDrivers.substring(0,colonIndex);
        moreDrivers = moreDrivers.substring(colonIndex+1).trim();
        if (moreDrivers.length() == 0) moreDrivers = null;
      } else {
        loadDriver = moreDrivers;
        moreDrivers = null;
      }
      try {
        Class.forName(loadDriver);
      } catch (Exception e) {
        if (debug_ || printStackTrace_ ) {
          e.printStackTrace();
        }
      }

    }

  }

  public Main(String url, String userid, String password) throws SQLException {
    initializeDefaults();
    url_ = url;
    userid_ = userid;
    password_ = password;
    try {
       connection_ = DriverManager.getConnection(url_, userid_, password_);
       addVariable("CON", connection_);
    } catch (SQLException ex ) {
      System.out.println("Unable to connect to "+url_+" using "+userid_);
      throw ex;
    }

  }

  Main() {
    initializeDefaults();
  }

  public int go(InputStream in, PrintStream out1) {
    int rc = 0;
    boolean running = true;
    String query;

    try {
      BufferedReader input = new BufferedReader(new InputStreamReader(in));
      if (prompt_)
        out1.print(promptString);
      query = input.readLine();
      while (running) {
        running = executeTopLevelCommand(query, out1);
        if (running) {
          if (prompt_)
            out1.print(promptString);
          query = input.readLine();
          if (query != null) {
            query = query.trim();
          } else {
            // EOF found
            running = false;
          }
        }
      }
      if (connection_ != null) {
        connection_.close();
      }
      connection_ = null;
      variables.remove("CON");

    } catch (Exception e) {
      e.printStackTrace(out1);
    } catch (java.lang.UnknownError jlu) {
      jlu.printStackTrace(out1);
    }

    return rc;
  }

  //
  // Threaded run information
  //
  String command_;
  PrintStream out_;
  private long startTime_;
  private long finishTime_;

  public Main(String command, PrintStream out) {
    this.command_ = command;
    this.out_ = out;
  }

  public void run() {
    Thread thisThread = Thread.currentThread();
    out_.println("Thread " + thisThread + " running " + command_);
    executeTopLevelCommand(command_, out_);
    out_.println("Thread " + thisThread + " ending");
  }

  public void useConnectionPool(boolean value) {
    useConnectionPool_ = value;
  }

  public void setUrl(String newUrl) {
    url_ = newUrl;
    if (url_.indexOf(":as400:") > 0) {
      toolboxDriver_ = true;
    }
  }

  public void setUserId(String newUserId, PrintStream out1) {
    if (debug_)
      out1.println("User ID set to " + newUserId);
    userid_ = newUserId;
  }

  public void setPassword(String newpassword) {
    password_ = newpassword;
  }

  //
  // remove properties from the string so that they are not
  // duplicated.
  //
  public String removeProperty(String url, String newProperties) {
    int equalsIndex = newProperties.indexOf("=");
    while (equalsIndex > 0) {
      // System.out.println("Removing "+newProperties+" from "+url);
      String property = newProperties.substring(0, equalsIndex).trim();

      //
      // fix url
      //
      int propertyIndex = url.indexOf(property);
      while (propertyIndex > 0) {
        int semicolonIndex = url.indexOf(";", propertyIndex);
        if (semicolonIndex > 0) {
          url = url.substring(0, propertyIndex).trim()
              + url.substring(semicolonIndex + 1).trim();
        } else {
          url = url.substring(0, propertyIndex).trim();
        }
        propertyIndex = url.indexOf(property);
      }

      // adjust new properties
      int semicolonIndex = newProperties.indexOf(";", equalsIndex);
      if (semicolonIndex > 0) {
        newProperties = newProperties.substring(semicolonIndex + 1);
      } else {
        newProperties = "";
      }
      equalsIndex = newProperties.indexOf("=");
    }
    // if the url ends with a ; remove it
    return url;
  }


  public void setManualResultSetColType(ResultSetMetaData rsmd)
      throws SQLException {
    manualResultSetColType_ = new int[manualResultSetNumCols_ + 1];
    int i;
    for (i = 1; i <= manualResultSetNumCols_; i++) {
      manualResultSetColType_[i] = rsmd.getColumnType(i);
    }

  }



  public Connection getPooledConnection(String thisConnectUserId,
      String thisConnectPassword, String connectUrl, PrintStream out1) throws SQLException {
    if ((poolConnection != null) && thisConnectUserId.equals(poolUserId)
        && thisConnectPassword.equals(poolPassword)
        && (connectUrl.equals(poolUrl))) {
      connection_ = poolConnection;
      addVariable("CON", connection_);

    } else {
      if (poolConnection != null) {
        String key = poolUserId + "." + poolPassword + "." + poolUrl;
        // return to the pool
        connectionPool.put(key, poolConnection);
        if (debug_)
          out1.println("Added connection to pool for " + key);
      }
      // Try to get one from the pool
      String key = thisConnectUserId + "." + thisConnectPassword + "."
          + connectUrl;
      connection_ = (Connection) connectionPool.get(key);
      if (connection_ != null) {
        addVariable("CON", connection_);

        if (debug_)
          out1.println("Retrieved connection from pool for " + key);
        connectionPool.remove(key);
      } else {
        if (debug_)
          out1.println("Didn't retrieve connection from pool for " + key);
        if (thisConnectUserId.equals("null")
            && thisConnectPassword.equals("null")) {
          connection_ = DriverManager.getConnection(connectUrl);
        } else {
          connection_ = DriverManager.getConnection(connectUrl,
              thisConnectUserId, thisConnectPassword);
        }
        addVariable("CON", connection_);

      }
      poolConnection = connection_;
      poolUserId = thisConnectUserId;
      poolPassword = thisConnectPassword;
      poolUrl = connectUrl;

    }
    return connection_;
  }


  /**
   * execute an sql query
   * @param command -- SQL command to execute
   * @throws Exception
   */
  void executeSqlQuery(String command, PrintStream out1) throws Exception {
    history.addElement(command);
    if (connection_ != null) {
      if (stmt_ != null && reuseStatement_) {
        // dont do anything -- reuse it
      } else {
        if (stmt_ != null) {
          stmt_.close();

        }
        if (jdk14_) {
          stmt_ = connection_.createStatement(resultSetType_,
              resultSetConcurrency_, resultSetHoldability_);
        } else {
          stmt_ = connection_.createStatement();
        }
        addVariable("STMT", stmt_);
      }

    }
    if (queryTimeout_ != 0) {
      stmt_.setQueryTimeout(queryTimeout_);
    }
    if (stmt_ != null) {
      // Submit a query, creating a ResultSet object
      if (measureExecute_) {
        startTime_ = System.currentTimeMillis();
      }

      ResultSet rs = stmt_.executeQuery(command);
      if (measureExecute_) {
        finishTime_ = System.currentTimeMillis();
        out1.println("TIME: " + (finishTime_ - startTime_) + " ms");
      }

      SQLWarning warning = stmt_.getWarnings();
      // Display all columns and rows from the result set
      if (manualFetch_) {
        ResultSetMetaData rsmd = rs.getMetaData();
        manualResultSetNumCols_ = rsmd.getColumnCount();
        setManualResultSetColType(rsmd);
        manualResultSet_ = rs;
        addVariable("RS", manualResultSet_);
        manualResultSetColumnLabel_ = dispColumnHeadings(out1, rs, rsmd,
            false, manualResultSetNumCols_, html_, xml_ );
      } else {
        dispResultSet(out1, rs, false);
        // Display any warnings
        if (warning != null) {
          if (!silent) {
            dispWarning(out1, warning, hideWarnings_, html_);
          }
        }
        // Close the result set
        if (closeStatementRS_) {
          rs.close();
        }
      }
    } else {
      out1.println("UNABLE to EXECUTE SELECT because not connected");
    }
}

  /*
   * Execute a CL command on the current connection using the existing statement object.
   */
  public void executeCLCommand (String clCommand, PrintStream out1) throws Exception {
    history.addElement("CL: "+clCommand);

    int commandSize = clCommand.length();
    String clCommandSize155;
    if (commandSize < 10)         clCommandSize155="000000000"+commandSize+".00000";
    else if (commandSize < 100)   clCommandSize155="00000000" +commandSize+".00000";
    else if (commandSize < 1000)  clCommandSize155="0000000" +commandSize+".00000";
    else if (commandSize < 10000) clCommandSize155="000000" +commandSize+".00000";
    else                          clCommandSize155="00000" +commandSize+".00000";

    String command = "CALL QSYS.QCMDEXC('"+clCommand+"    ',"+clCommandSize155+")";

    if (connection_ != null) {
      if (stmt_ != null && reuseStatement_) {
        // dont do anything -- reuse it
      } else {
        if (stmt_ != null) {
          stmt_.close();

        }
        if (jdk14_) {
          stmt_ = connection_.createStatement(resultSetType_,
              resultSetConcurrency_, resultSetHoldability_);
        } else {
          stmt_ = connection_.createStatement();
        }
        addVariable("STMT", stmt_);
      }

    }
    if (stmt_ != null) {
      // Submit a query, creating a ResultSet object
      if (measureExecute_) {
        startTime_ = System.currentTimeMillis();
      }

      stmt_.executeUpdate(command);
      if (measureExecute_) {
        finishTime_ = System.currentTimeMillis();
        out1.println("TIME: " + (finishTime_ - startTime_) + " ms");
      }

      SQLWarning warning = stmt_.getWarnings();
      // Display all columns and rows from the result set
    } else {
      out1.println("UNABLE to EXECUTE SELECT because not connected");
    }
}

  public void executeCallCommand(String command, PrintStream out1) throws Exception {
    history.addElement(command);
    if (connection_ != null) {
      //
      // See if input parameters are passed.
      // If so, they are comma separated
      // A unicode string may be specified using UX'dddd'
      // A byte array may be specified using X'dddd'
      // or GEN_BYTE_ARRAY+'nnnn'
      //
      int parmIndex = command.indexOf("-- INPARM");
      String parms = null;
      if (parmIndex > 0) {
        parms = command.substring(parmIndex + 9).trim();
        command = command.substring(0, parmIndex);
      }

      if (jdk14_) {
        cstmt_ = connection_.prepareCall(command, resultSetType_,
            resultSetConcurrency_, resultSetHoldability_);
      } else {
        cstmt_ = connection_.prepareCall(command);
      }
      addVariable("CSTMT", cstmt_);

      if (jdk14_) {
        //
        // If JDK 1.4 is available then use metadata
        // to set parameters
        //
        ParameterMetaData pmd = cstmt_.getParameterMetaData();
        int parmCount = pmd.getParameterCount();
        for (int parm = 1; parm <= parmCount; parm++) {
          int mode = pmd.getParameterMode(parm);
          if (mode == ParameterMetaData.parameterModeOut
              || mode == ParameterMetaData.parameterModeInOut) {

            // Register the output parameter as the correct type
            // For most of the types, we will register as VARCHAR
            // since we will be used getString() to get the
            // output.

            int type = pmd.getParameterType(parm);
            switch (type) {
            case Types.BLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case -8: /* ROWID */
            case Types.ARRAY:
              cstmt_.registerOutParameter(parm, type);
              break;
            default:
              cstmt_.registerOutParameter(parm, Types.VARCHAR);

            }
          }

          if (mode == ParameterMetaData.parameterModeIn
              || mode == ParameterMetaData.parameterModeInOut) {
            String thisParm = parms;
            if (parms != null) {
              parmIndex = parms.indexOf(",");
              if (parmIndex >= 0) {
                thisParm = parms.substring(0, parmIndex).trim();
                parms = parms.substring(parmIndex + 1).trim();
              }
            }
            if (thisParm != null) {
              setParameter(cstmt_, thisParm, parm, out1);
            } else {
              out1.println("Warning:  thisParm is null");
              out1.println("--INPARM not found but num param > 0 ");
            }

          }
        }
      } else {
        //
        // If there is a question mark, assume that parameter markers were
        // used and
        // throw an exception
        //
        if (command.indexOf("?") >= 0) {
          throw new SQLException(
              "Use of parameter markers in call statement only supported in JDK 1.4 -- statement was "
                  + command);
        }
      }

      boolean resultSetAvailable = cstmt_.execute();
      // Display any warnings
      SQLWarning warning = cstmt_.getWarnings();
      if (warning != null) {
        if (!silent) {
          dispWarning(out1, warning, hideWarnings_, html_);
        }
        if (html_) {
          out1.println("Statement was " + command);
        }
      }

      if (jdk14_) {
        //
        // If JDK 1.4 is available then use metadata
        // to get parameters
        //
        ParameterMetaData pmd = cstmt_.getParameterMetaData();
        int parmCount = pmd.getParameterCount();
        for (int parm = 1; parm <= parmCount; parm++) {
          int mode = pmd.getParameterMode(parm);
          if (mode == ParameterMetaData.parameterModeOut
              || mode == ParameterMetaData.parameterModeInOut) {

            int type = pmd.getParameterType(parm);

            switch (type) {
            case Types.BLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case -8: /* ROWID */

            {
              out1.print("Parameter " + parm + " returned ");
              byte[] bytes = cstmt_.getBytes(parm);
              if (bytes == null) {
                out1.println("null");
              } else {
                if (bytes.length < showLobThreshold_) {
                  out1.print("X'");
                  for (int i = 0; i < bytes.length; i++) {
                    int unsignedInt = 0xFF & bytes[i];
                    if (unsignedInt < 0x10) {
                      out1.print("0" + Integer.toHexString(unsignedInt));
                    } else {
                      out1.print(Integer.toHexString(unsignedInt));
                    }
                  }
                  out1.println("'");
                } else {
                  CRC32 checksum = new CRC32();
                  checksum.update(bytes);
                  out1.println("ARRAY[size=" + bytes.length + ",CRC32="
                      + checksum.getValue() + "]");
                }
              }
            }
              break;
            case Types.ARRAY:
              out1.print("Parameter " + parm + " returned ARRAY ");
              printArray(out1, cstmt_.getArray(parm));
              out1.println();

              break;
            default:
              out1.print("Parameter " + parm + " returned ");
              savedStringParm_[parm] = cstmt_.getString(parm);
              printUnicodeString(out1, savedStringParm_[parm]);
              out1.println();
            }
          }
        }
      }

      if (resultSetAvailable) {
        ResultSet rs = cstmt_.getResultSet();
        if (rs != null) {
          if (manualFetch_) {
            ResultSetMetaData rsmd = rs.getMetaData();
            manualResultSetNumCols_ = rsmd.getColumnCount();
            setManualResultSetColType(rsmd);
            manualResultSet_ = rs;
            addVariable("RS", manualResultSet_);
            manualResultSetColumnLabel_ = dispColumnHeadings(out1, rs,
                rsmd, false, manualResultSetNumCols_, html_, xml_);
          } else {

            dispResultSet(out1, rs, false);
            if (closeStatementRS_) {
              rs.close();
              rs = null;
            }
          }
          // Look for more result tests
          if (!manualFetch_) {
            while (cstmt_.getMoreResults()) {
              out1.println("<<<< NEXT RESULT SET >>>>>>>");
              rs = cstmt_.getResultSet();
              dispResultSet(out1, rs, false);
              if (closeStatementRS_) {
                rs.close();
                rs = null;
              }
            }
          }
        }
      }
      if (!manualFetch_ && closeStatementRS_) {
        cstmt_.close();
      }
    } else {
      out1.println("UNABLE to EXECUTE CALL because not connected");
    }
}


  public void executeSqlCommand(String command, PrintStream out1) throws Exception  {
    //
    // just attempt to execute the statement
    //
    if (connection_ != null) {
      if (connection_ != null) {
        if (stmt_ != null && reuseStatement_) {
          // dont do anything -- reuse it
        } else {

          if (jdk14_) {
            stmt_ = connection_.createStatement(resultSetType_,
                resultSetConcurrency_, resultSetHoldability_);
          } else {
            stmt_ = connection_.createStatement();
          }
          addVariable("STMT", stmt_);
        }
      }
      if (queryTimeout_ != 0) {
        stmt_.setQueryTimeout(queryTimeout_);
      }
      if (measureExecute_) {
        startTime_ = System.currentTimeMillis();
      }
      stmt_.executeUpdate(command);
      history.addElement(command);
      if (measureExecute_) {
        finishTime_ = System.currentTimeMillis();
        out1.println("TIME: " + (finishTime_ - startTime_) + " ms");
      }

      //
      // Don't forget to check for warnings...
      //
      SQLWarning warning = stmt_.getWarnings();
      if (warning != null) {
        if (!silent) {
          dispWarning(out1, warning, hideWarnings_, html_);
        }
      }

    } else {
      out1.println("UNABLE to EXECUTE because not connected");
    }
}

  public void processException(SQLException ex, String command, PrintStream out1) {
    // A SQLException was generated. Catch it and
    // display the error information. Note that there
    // could be multiple error objects chained
    // together
    if (!silent) {
      out1.println("\n*** SQLException caught ***");
      out1.println("Statement was " + command);
      Throwable t = ex;
      while (t != null) {
        if (t instanceof SQLException) {
          ex = (SQLException) t;
          out1.println("SQLState: " + ex.getSQLState());
          String exMessage = ex.getMessage();
          exMessage = cleanupMessage(exMessage);
          out1.println("Message:  " + exMessage);
          out1.println("Vendor:   " + ex.getErrorCode());
          if (debug_  || printStackTrace_ )
            ex.printStackTrace(out1);
          int thisCode = ex.getErrorCode();
          if (thisCode == -104) { // TOKEN NOT VALID
              out1.println("\nToken not valid found.  Use !HELP to see what you can do");
          }

          t = ex.getNextException();
          if (t == null) {
            try {
              // get cause added in JDK 1.4 ignore if not there
              t = ex.getCause();
            } catch (Throwable t2) {

            }
          }
          out1.println("");
        } else {
          if (t != null) {
            t.printStackTrace( out1);

            try {
              // get cause added in JDK 1.4 ignore if not there
              t = t.getCause();
            } catch (Throwable t2) {

            }

          }
        }

      } // while
    }
}


  /**
   * Execute a top level command. This may be an SQL statement or a command !
   *
   * @param command
   * @param out1
   * @return
   */
  public boolean executeTopLevelCommand(String command, PrintStream out1) {
    boolean returnCode = true;
    silent = false;

    command = command.trim();

    //
    // Strip the invisible if it exists

    if (command.toUpperCase().startsWith("!INVISIBLE:")) {
      silent = true;
      command = command.substring(10).trim();
    } else {
      if (echoCommand_) {
        out1.println(command);
        if (html_)
          out1.println("<BR>");
      }
    }

    //
    // Strip the silent if it exists
    //
    if (command.toUpperCase().startsWith("!SILENT:")) {
      silent = true;
      command = command.substring(7).trim();
    }

    try {
      //
      // Figure out the command
      //
      String upcaseCommand = command.toUpperCase();
      if (upcaseCommand.startsWith("SELECT")
          || upcaseCommand.startsWith("VALUES")) {
        executeSqlQuery(command,out1);
      } else if (upcaseCommand.startsWith("CL:")) {
        String clCommand = command.substring(3).trim();
        executeCLCommand(clCommand, out1);
      } else if (upcaseCommand.startsWith("!ECHO")
          || upcaseCommand.startsWith("--") || upcaseCommand.startsWith("//")
          || upcaseCommand.startsWith("/*")) {

        history.addElement(command);

        if (echoComments_) {
          out1.println(command);
          if (html_)
            out1.println("<BR>");
        }
      } else if (upcaseCommand.equals("!QUIT")
          || upcaseCommand.equals("!EXIT")
          || upcaseCommand.equals("QUIT")
          || upcaseCommand.equals("EXIT")) {
        returnCode = false;
      } else if (command.length() > 0 && command.startsWith("!")) {
        command = command.substring(1);
        executeCommand(command, out1);
      } else if (upcaseCommand.startsWith("CALL ")) {
        executeCallCommand(command, out1);
      } else {
        //
        // If not a blank line
        //
        if (upcaseCommand.length() != 0) {
          executeSqlCommand(command, out1);
        }
      }

    } catch (SQLException ex) {

      processException(ex, command, out1);
    } catch (Exception e) {
      out1.println("\n*** exception caught *** " + e);
      out1.println("Statement was " + command);
      e.printStackTrace(out1);
    } catch (java.lang.UnknownError jlu) {
      out1.println("\n*** java.lang.UnknownError caught ***" + jlu);
      out1.println("Statement was " + command);
      jlu.printStackTrace(out1);

    } // catch
    finally {
      if (stmt_ != null) {
        try {
          if (!reuseStatement_) {
            if (!manualFetch_ && closeStatementRS_) {
              stmt_.close();
              stmt_ = null;
              variables.remove("STMT");
            }
          }
        } catch (Exception e) {
          e.printStackTrace(out1);
        }
      }
    }

    return returnCode;
  }

  /**
   * Executes a command that is not an SQL query.
   * The ! has already been stripped from the command.
   *
   * @param command1
   * @param out1
   * @return
   */
  public boolean executeCommand(String command1, PrintStream out1) {
    boolean returnCode = true;
    silent = false;

    command1 = command1.trim();

    //
    // Strip the invisible if it exists

    if (command1.toUpperCase().startsWith("INVISIBLE:")) {
      silent = true;
      command1 = command1.substring(10).trim();
    }

    //
    // Strip the silent if it exists
    //
    if (command1.toUpperCase().startsWith("SILENT:")) {
      silent = true;
      command1 = command1.substring(7).trim();
    }

    try {
      //
      // Figure out the command
      //
      String upcaseCommand = command1.toUpperCase();
      if (upcaseCommand.startsWith("PREPARE")) {
        history.addElement("!"+command1);
        command1 = command1.substring(7).trim();
        if (pstmt_ != null) {
          if (closeStatementRS_) {
            pstmt_.close();
          }
        }
        if (jdk14_) {
          pstmt_ = connection_.prepareStatement(command1, resultSetType_,
              resultSetConcurrency_, resultSetHoldability_);
        } else {
          pstmt_ = connection_.prepareStatement(command1);
        }
        addVariable("PSTMT", pstmt_);

      } else if (upcaseCommand.startsWith("SETRESULTSETTYPE")) {
        history.addElement("!"+command1);
        command1 = command1.substring(16).trim();
        if (command1.indexOf("FORWARD_ONLY") >= 0) {
          resultSetType_ = ResultSet.TYPE_FORWARD_ONLY;
        } else if (command1.indexOf("SCROLL_INSENSITIVE") >= 0) {
          resultSetType_ = ResultSet.TYPE_SCROLL_INSENSITIVE;
        } else if (command1.indexOf("SCROLL_SENSITIVE") >= 0) {
          resultSetType_ = ResultSet.TYPE_SCROLL_SENSITIVE;
        } else {
          out1.println("Value of '" + command1 + " not valid use");
          out1
              .println("     FORWARD_ONLY, SCROLL_INSENSITIVE, or SCROLL_SENSITIVE");
        }
      } else if (upcaseCommand.startsWith("SETRESULTSETCONCURRENCY")) {
        history.addElement("!"+command1);
        command1 = command1.substring(15).trim();
        if (command1.indexOf("READ_ONLY") >= 0) {
          resultSetConcurrency_ = ResultSet.CONCUR_READ_ONLY;
        } else if (command1.indexOf("UPDATABLE") >= 0) {
          resultSetConcurrency_ = ResultSet.CONCUR_UPDATABLE;
        } else {
          out1.println("Value of '" + command1 + " not valid. Use");
          out1.println(" CONCUR_READ_ONLY or CONCUR_UPDATABLE ");
        }
      } else if (upcaseCommand.startsWith("SETRESULTSETHOLDABILITY")) {
        history.addElement("!"+command1);
        command1 = command1.substring(15).trim();
        if (command1.indexOf("HOLD") >= 0) {
          resultSetHoldability_ = ResultSet.HOLD_CURSORS_OVER_COMMIT;
        } else if (command1.indexOf("CLOSE") >= 0) {
          resultSetHoldability_ = ResultSet.CLOSE_CURSORS_AT_COMMIT;
        } else {
          out1.println("Value of '" + command1 + " not valid. Use");
          out1.println(" HOLD_CURSORS_OVER_COMMIT or CLOSE_CURSORS_AT_COMMIT");
        }
      } else if (upcaseCommand.startsWith("EXECUTEQUERY")) {
        history.addElement("!"+command1);
        if (pstmt_ != null) {
          if (measureExecute_) {
            startTime_ = System.currentTimeMillis();
          }

          ResultSet rs = pstmt_.executeQuery();
          if (measureExecute_) {
            finishTime_ = System.currentTimeMillis();
            out1.println("TIME: " + (finishTime_ - startTime_) + " ms");
          }
          if (manualFetch_) {
            ResultSetMetaData rsmd = rs.getMetaData();
            manualResultSetNumCols_ = rsmd.getColumnCount();
            setManualResultSetColType(rsmd);
            manualResultSet_ = rs;
            addVariable("RS", manualResultSet_);
            manualResultSetColumnLabel_ = dispColumnHeadings(out1, rs, rsmd,
                false, manualResultSetNumCols_, html_, xml_);
          } else {

            // Display all columns and rows from the result set
            dispResultSet(out1, rs, false);
            // Display any warnings
            SQLWarning warning = pstmt_.getWarnings();
            if (warning != null) {
              if (!silent) {
                dispWarning(out1, warning, hideWarnings_, html_);
              }
            }
            // Close the result set
            if (closeStatementRS_) {
              rs.close();
            }
          }
        } else {
          out1
              .println("UNABLE to EXECUTE QUERY because prepared statement does not exist");
        }
      } else if (upcaseCommand.startsWith("EXECUTEUPDATE")) {
        history.addElement("!"+command1);
        if (pstmt_ != null) {
          if (measureExecute_) {
            startTime_ = System.currentTimeMillis();
          }
          pstmt_.executeUpdate();
          if (measureExecute_) {
            finishTime_ = System.currentTimeMillis();
            out1.println("TIME: " + (finishTime_ - startTime_) + " ms");
          }

          // Display any warnings
          SQLWarning warning = pstmt_.getWarnings();
          if (warning != null) {
            if (!silent) {
              dispWarning(out1, warning, hideWarnings_, html_);
            }
          }
        } else {
          out1
              .println("UNABLE to EXECUTE UPDATE because prepared statement does not exist");
        }
      } else if (upcaseCommand.startsWith("SETPARMFROMVAR")) {
        history.addElement("!"+command1);
        if (pstmt_ != null) {
          command1 = command1.substring(14).trim();
          int commaIndex = command1.indexOf(",");
          if (commaIndex > 0) {
            String indexString = command1.substring(0, commaIndex).trim();
            int index = Integer.parseInt(indexString);
            String parmString = command1.substring(commaIndex + 1).trim();
            Object varObject = variables.get(parmString);
            if (varObject != null) {
              pstmt_.setObject(index, varObject);
              SQLWarning warning = pstmt_.getWarnings();
              if (warning != null) {
                if (!silent) {
                  dispWarning(out1, warning, hideWarnings_, html_);
                }
              }

            } else {
              out1.println("Unable to find object for variable " + parmString);
              showValidVariables(out1);
            }
          } else {
            out1
                .println("UNABLE to find comma for SETPARM  --> SETPARM [index],[value]");
          }
        } else {
          out1
              .println("UNABLE to SETPARM because prepared statement does not exist");
        }
      } else if (upcaseCommand.startsWith("SETPARM")) {
        history.addElement("!"+command1);
        if (pstmt_ != null) {
          command1 = command1.substring(7).trim();
          int commaIndex = command1.indexOf(",");
          if (commaIndex > 0) {
            String indexString = command1.substring(0, commaIndex).trim();
            int index = Integer.parseInt(indexString);
            String parmString = command1.substring(commaIndex + 1).trim();
            setParameter(pstmt_, parmString, index, out1);
          } else {
            out1
                .println("UNABLE to find comma for SETPARM  --> SETPARM [index],[value]");
          }
        } else {
          out1
              .println("UNABLE to SETPARM because prepared statement does not exist");
        }
      } else if (upcaseCommand.startsWith("ECHO")
          || upcaseCommand.startsWith("--") || upcaseCommand.startsWith("//")
          || upcaseCommand.startsWith("/*")) {

        history.addElement("!"+command1);

        if (echoComments_) {
          out1.println(command1);
          if (html_)
            out1.println("<BR>");
        }

        // Already echoed, don't do anything
      } else if (upcaseCommand.startsWith("SETQUERYTIMEOUT")) {
        history.addElement("!"+command1);
        String arg = command1.substring(16).trim();
        try {
          queryTimeout_ = Integer.parseInt(arg);
          out1.println("-->Query timeout set to " + queryTimeout_);
        } catch (Exception e) {
          out1.println("Unable to parse (" + arg + ")");
        }

        // Already echoed, don't do anything
      } else if (upcaseCommand.startsWith("RESET CONNECTION PROPERTIES")) {
        history.addElement("!"+command1);
        urlArgs_ = "";
      } else if (upcaseCommand.startsWith("ADD CONNECTION PROPERTY")) {
        history.addElement("!"+command1);
        String newProperty = command1.substring(23).trim();
        urlArgs_ = removeProperty(urlArgs_, newProperty);
        urlArgs_ += "; " + newProperty;

      } else if (upcaseCommand.startsWith("CONNECT TO URL")) {
        history.addElement("!"+command1);
        if (connection_ != null) {

          if (connection_ == poolConnection) {
            // Dont close our one poolConnection
            // It may be reused on the next run
          } else {
            connection_.close();
          }
        }

        String connectUrl = command1.substring(14).trim();
        String thisConnectUserId = null;
        String thisConnectPassword = null;
        int userIdIndex = connectUrl.indexOf("USERID=");
        if (userIdIndex > 0) {
          thisConnectUserId = connectUrl.substring(userIdIndex + 7).trim();
          connectUrl = connectUrl.substring(0, userIdIndex);
          int spaceIndex = thisConnectUserId.indexOf(" ");
          if (spaceIndex >= 0) {
            thisConnectUserId = thisConnectUserId.substring(0, spaceIndex);
          }
          int passwordIndex = command1.indexOf("PASSWORD=");
          if (passwordIndex > 0) {
            thisConnectPassword = command1.substring(passwordIndex + 9).trim();
            spaceIndex = thisConnectPassword.indexOf(" ");
            if (spaceIndex > 0) {
              thisConnectPassword = thisConnectPassword
                  .substring(0, spaceIndex);
            }
            if (debug_)
              out1.println("Connecting using " + userid_ + ", "
                  + password_ + " to " + connectUrl);
            Driver iSeriesDriver = null;
            try {

              if (connectUrl.indexOf("jdbc:db2://") >= 0) {
                if (debug_)
                  out1.println("Loading jcc driver");
                Class.forName("com.ibm.db2.jcc.DB2Driver").newInstance();
                Driver currentDriver = DriverManager.getDriver(url_);
                if (currentDriver.getClass().getName().equals(
                    "com.ibm.db2.jdbc.app.DB2Driver")) {
                  if (debug_)
                    out1.println("removing native driver");
                  iSeriesDriver = currentDriver;
                  DriverManager.deregisterDriver(iSeriesDriver);
                }

              }

              if (useConnectionPool_) {
                connection_ = getPooledConnection(thisConnectUserId,
                    thisConnectPassword, connectUrl, out1);

              } else {
                connection_ = DriverManager.getConnection(connectUrl,
                    thisConnectUserId, thisConnectPassword);
                addVariable("CON", connection_);
                SQLWarning warning = connection_.getWarnings();
                if (warning != null) {
                  if (!silent) {
                    dispWarning(out1, warning, hideWarnings_, html_);
                  }
                }

              }

            } catch (Exception e) {
              e.printStackTrace(out1);
              thisConnectPassword = null;
            }
            if (iSeriesDriver != null) {
              DriverManager.registerDriver(iSeriesDriver);
            }
            conLabel_ = conName;
            if (conCount > 0)
              conLabel_ = conLabel_ + conCount;
            conCount++;

          } /* password Index */
        } /* userIdIndex */
        if (thisConnectPassword == null) {
          out1
              .println("Usage:  CONNECT TO URL [URL] [USERID=XXXX] [PASSWORD=YYYY]");
          out1
              .println("  i.e.  CONNECT TO URL jdbc:db2:localhost USERID=EBERHARD PASSWORD=XXXXX");
          out1
              .println("        CONNECT TO URL jdbc:db2://localhost/*LOCAL USERID=EBERHARD PASSWORD=XXXXX");
          out1
              .println("        CONNECT TO URL jdbc:db2:SAMPLE\\;transaction isolation=serializable USERID=EBERHARD PASSWORD=XXXXXXX");
        }

      } else if (upcaseCommand.startsWith("CONNECT TO SCHEMA")) {
        history.addElement("!"+command1);
        if (connection_ != null) {
          if (connection_ == poolConnection) {
            // Dont close our one poolConnection
            // It may be reused on the next run
          } else {
            connection_.close();
          }
          connection_ = null;
        }

        String connectUrl;
        String schema = command1.substring(17).trim();
        if (schema.length() > 0) {

          //
          // Determine if a system name was specified..
          //
          int slashIndex = schema.indexOf('/');
          if (slashIndex >= 0) {
            int colonIndex = url_.indexOf(":");
            colonIndex = url_.indexOf(":", colonIndex + 1);

            String baseUrl = url_.substring(0, colonIndex + 1);
            connectUrl = baseUrl + schema + urlArgs_;
          } else {
            connectUrl = url_ + "/" + schema + urlArgs_;
          }

        } else {
          connectUrl = url_ + urlArgs_;
        }
        //
        // Cleanup URL if needed
        //
        {
          int cleanIndex = connectUrl.indexOf(" ;");
          while (cleanIndex > 0) {
            connectUrl = connectUrl.substring(0, cleanIndex)
                + connectUrl.substring(cleanIndex + 1);
            cleanIndex = connectUrl.indexOf(" ;");
          }
        }
        if (userid_ != null) {
          if (debug_)
            out1.println("Connecting using " + userid_ + ", " + password_
                + " to " + connectUrl);
          if (useConnectionPool_) {
            connection_ = getPooledConnection(userid_, password_, connectUrl, out1);
          } else {
            connection_ = DriverManager.getConnection(connectUrl, userid_,
                password_);
            SQLWarning warning = connection_.getWarnings();
            if (warning != null) {
              if (!silent) {
                dispWarning(out1, warning, hideWarnings_, html_);
              }
            }

          }
          addVariable("CON", connection_);

        } else {
          if (debug_)
            out1.println("Connecting using default id and password to "
                + connectUrl);
          if (useConnectionPool_) {
            connection_ = getPooledConnection("null", "null", connectUrl, out1);
          } else {
            connection_ = DriverManager.getConnection(connectUrl);
            SQLWarning warning = connection_.getWarnings();
            if (warning != null) {
              if (!silent) {
                dispWarning(out1, warning, hideWarnings_, html_);
              }
            }

          }
          addVariable("CON", connection_);
        }

        conLabel_ = conName;
        if (conCount > 0)
          conLabel_ = conLabel_ + conCount;
        conCount++;

        //
        // Need to set path so that stuff can be found
        //
        // set current path so that Java STP is found when called thru JDBC
        int semicolonIndex = schema.indexOf(';');
        if (semicolonIndex > 0) {
          schema = schema.substring(0, semicolonIndex).trim();
        }
        if (schema.length() > 0) {
          int slashIndex = schema.lastIndexOf("/");
          if (slashIndex >= 0) {
            schema = schema.substring(slashIndex + 1);
          }
          PreparedStatement pStmt = connection_
              .prepareStatement("SET CURRENT PATH " + schema + ", SYSTEM PATH");
          pStmt.execute();
          pStmt.close();
        }

      } else if (upcaseCommand.startsWith("CONNECT RESET")) {
        history.addElement("!"+command1);
        if (connection_ != null) {

          if (connection_ == poolConnection) {
            // Dont close our one poolConnection
            // It may be reused on the next run
          } else {
            connection_.close();
          }
          connection_ = null;
          variables.remove("CON");

        }

      } else if (upcaseCommand.startsWith("CHARACTERDETAILS")) {
        history.addElement("!"+command1);
        String arg = command1.substring(16).trim().toUpperCase();
        if (arg.equals("TRUE")) {
          characterDetails_ = true;
        } else if (arg.equals("ON")) {
          characterDetails_ = true;
        } else if (arg.equals("FALSE")) {
          characterDetails_ = false;
        } else if (arg.equals("OFF")) {
          characterDetails_ = false;
        } else {
          out1.println("Invalid arg '" + arg + "' for CHARACTERDETAILS");
        }
      } else if (upcaseCommand.startsWith("ECHOCOMMAND")) {
        history.addElement("!"+command1);
        String arg = command1.substring(11).trim().toUpperCase();
        if (arg.equals("TRUE")) {
          echoCommand_ = true;
        } else if (arg.equals("ON")) {
          echoCommand_ = true;
        } else if (arg.equals("FALSE")) {
          echoCommand_ = false;
        } else if (arg.equals("OFF")) {
          echoCommand_ = false;
        } else {
          out1.println("Invalid arg '" + arg + "' for ECHOCOMMAND");
        }
      } else if (upcaseCommand.startsWith("PRINTSTACKTRACE")) {
        history.addElement("!"+command1);
        String arg = command1.substring(15).trim().toUpperCase();
        if (arg.equals("TRUE")) {
          printStackTrace_ = true;
        } else if (arg.equals("ON")) {
          printStackTrace_ = true;
        } else if (arg.equals("FALSE")) {
          printStackTrace_ = false;
        } else if (arg.equals("OFF")) {
          printStackTrace_ = false;
        } else {
          out1.println("Invalid arg '" + arg + "' for ECHOCOMMAND");
        }
      } else if (upcaseCommand.startsWith("CLOSESTATEMENTRS")) {
        history.addElement("!"+command1);
        String arg = command1.substring(16).trim().toUpperCase();
        if (arg.equals("TRUE")) {
          closeStatementRS_ = true;
        } else if (arg.equals("ON")) {
          closeStatementRS_ = true;
        } else if (arg.equals("FALSE")) {
          closeStatementRS_ = false;
        } else if (arg.equals("OFF")) {
          closeStatementRS_ = false;
        } else {
          out1.println("Invalid arg '" + arg + "' for closeStatementRS");
        }
      } else if (upcaseCommand.startsWith("MEASUREEXECUTE")) {
        history.addElement("!"+command1);
        String arg = command1.substring(14).trim().toUpperCase();
        if (arg.equals("TRUE")) {
          measureExecute_ = true;
        } else if (arg.equals("ON")) {
          measureExecute_ = true;
        } else if (arg.equals("FALSE")) {
          measureExecute_ = false;
        } else if (arg.equals("OFF")) {
          measureExecute_ = false;
        } else {
          out1.println("Invalid arg '" + arg + "' for measureExecute");
        }
      } else if (upcaseCommand.startsWith("CALL ")) {
        history.addElement(command1);
        if (connection_ != null) {
          //
          // See if input parameters are passed.
          // If so, they are comma separated
          // A unicode string may be specified using UX'dddd'
          // A byte array may be specified using X'dddd'
          // or GEN_BYTE_ARRAY+'nnnn'
          //
          int parmIndex = command1.indexOf("-- INPARM");
          String parms = null;
          if (parmIndex > 0) {
            parms = command1.substring(parmIndex + 9).trim();
            command1 = command1.substring(0, parmIndex);
          }

          if (jdk14_) {
            cstmt_ = connection_.prepareCall(command1, resultSetType_,
                resultSetConcurrency_, resultSetHoldability_);
          } else {
            cstmt_ = connection_.prepareCall(command1);
          }
          addVariable("CSTMT", cstmt_);

          if (jdk14_) {
            //
            // If JDK 1.4 is available then use metadata
            // to set parameters
            //
            ParameterMetaData pmd = cstmt_.getParameterMetaData();
            int parmCount = pmd.getParameterCount();
            for (int parm = 1; parm <= parmCount; parm++) {
              int mode = pmd.getParameterMode(parm);
              if (mode == ParameterMetaData.parameterModeOut
                  || mode == ParameterMetaData.parameterModeInOut) {

                // Register the output parameter as the correct type
                // For most of the types, we will register as VARCHAR
                // since we will be used getString() to get the
                // output.

                int type = pmd.getParameterType(parm);
                switch (type) {
                case Types.BLOB:
                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                case -8: /* ROWID */
                case Types.ARRAY:
                  cstmt_.registerOutParameter(parm, type);
                  break;
                default:
                  cstmt_.registerOutParameter(parm, Types.VARCHAR);

                }
              }

              if (mode == ParameterMetaData.parameterModeIn
                  || mode == ParameterMetaData.parameterModeInOut) {
                String thisParm = parms;
                if (parms != null) {
                  parmIndex = parms.indexOf(",");
                  if (parmIndex >= 0) {
                    thisParm = parms.substring(0, parmIndex).trim();
                    parms = parms.substring(parmIndex + 1).trim();
                  }
                }
                if (thisParm != null) {
                  setParameter(cstmt_, thisParm, parm, out1);
                } else {
                  out1.println("Warning:  thisParm is null");
                  out1.println("--INPARM not found but num param > 0 ");
                }

              }
            }
          } else {
            //
            // If there is a question mark, assume that parameter markers were
            // used and
            // throw an exception
            //
            if (command1.indexOf("?") >= 0) {
              throw new SQLException(
                  "Use of parameter markers in call statement only supported in JDK 1.4 -- statement was "
                      + command1);
            }
          }

          boolean resultSetAvailable = cstmt_.execute();
          // Display any warnings
          SQLWarning warning = cstmt_.getWarnings();
          if (warning != null) {
            if (!silent) {
              dispWarning(out1, warning, hideWarnings_, html_);
            }
            if (html_) {
              out1.println("Statement was " + command1);
            }
          }

          if (jdk14_) {
            //
            // If JDK 1.4 is available then use metadata
            // to get parameters
            //
            ParameterMetaData pmd = cstmt_.getParameterMetaData();
            int parmCount = pmd.getParameterCount();
            for (int parm = 1; parm <= parmCount; parm++) {
              int mode = pmd.getParameterMode(parm);
              if (mode == ParameterMetaData.parameterModeOut
                  || mode == ParameterMetaData.parameterModeInOut) {

                int type = pmd.getParameterType(parm);

                switch (type) {
                case Types.BLOB:
                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                case -8: /* ROWID */

                {
                  out1.print("Parameter " + parm + " returned ");
                  byte[] bytes = cstmt_.getBytes(parm);
                  if (bytes == null) {
                    out1.println("null");
                  } else {
                    if (bytes.length < showLobThreshold_) {
                      out1.print("X'");
                      for (int i = 0; i < bytes.length; i++) {
                        int unsignedInt = 0xFF & bytes[i];
                        if (unsignedInt < 0x10) {
                          out1.print("0" + Integer.toHexString(unsignedInt));
                        } else {
                          out1.print(Integer.toHexString(unsignedInt));
                        }
                      }
                      out1.println("'");
                    } else {
                      CRC32 checksum = new CRC32();
                      checksum.update(bytes);
                      out1.println("ARRAY[size=" + bytes.length + ",CRC32="
                          + checksum.getValue() + "]");
                    }
                  }
                }
                  break;
                case Types.ARRAY:
                  out1.print("Parameter " + parm + " returned ARRAY ");
                  printArray(out1, cstmt_.getArray(parm));
                  out1.println();

                  break;
                default:
                  out1.print("Parameter " + parm + " returned ");
                  savedStringParm_[parm] = cstmt_.getString(parm);
                  printUnicodeString(out1, savedStringParm_[parm]);
                  out1.println();
                }
              }
            }
          }

          if (resultSetAvailable) {
            ResultSet rs = cstmt_.getResultSet();
            if (rs != null) {
              if (manualFetch_) {
                ResultSetMetaData rsmd = rs.getMetaData();
                manualResultSetNumCols_ = rsmd.getColumnCount();
                setManualResultSetColType(rsmd);
                manualResultSet_ = rs;
                addVariable("RS", manualResultSet_);
                manualResultSetColumnLabel_ = dispColumnHeadings(out1, rs,
                    rsmd, false, manualResultSetNumCols_, html_, xml_);
              } else {

                dispResultSet(out1, rs, false);
                if (closeStatementRS_) {
                  rs.close();
                  rs = null;
                }
              }
              // Look for more result tests
              if (!manualFetch_) {
                while (cstmt_.getMoreResults()) {
                  out1.println("<<<< NEXT RESULT SET >>>>>>>");
                  rs = cstmt_.getResultSet();
                  dispResultSet(out1, rs, false);
                  if (closeStatementRS_) {
                    rs.close();
                    rs = null;
                  }
                }
              }
            }
          }
          if (!manualFetch_ && closeStatementRS_) {
            cstmt_.close();
          }
        } else {
          out1.println("UNABLE to EXECUTE CALL because not connected");
        }
      } else if (upcaseCommand.startsWith("EXISTFILE")) {
        history.addElement("!"+command1);
        String filename = command1.substring(9).trim();
        try {
          File testFile = new File(filename);
          if (testFile.exists()) {
            out1.println("EXISTFILE " + filename + ": YES");
          } else {
            out1.println("EXISTFILE " + filename + ": NO");
          }
        } catch (Exception e) {
          e.printStackTrace(out1);
        }
      } else if (upcaseCommand.startsWith("GC")) {
        history.addElement("!"+command1);
        startTime_ = System.currentTimeMillis();
        System.gc();
        finishTime_ = System.currentTimeMillis();

        out1.println("GC ran in " + (finishTime_ - startTime_) + " milliseconds");

      } else if (upcaseCommand.startsWith("OUTPUT FORMAT ")) {
        history.addElement("!"+command1);
        String format = command1.substring(14).trim().toUpperCase();
        if (format.equals("XML")) {
          xml_ = true;
          html_ = false;
        } else if (format.equals("HTML")) {
          html_ = true;
          xml_ = false;
        } else {
          out1.println("Error.  Did not recognize output format '"
              + format + "'");
        }
      } else if (upcaseCommand.startsWith("SHOWMIXEDUX ")) {
        history.addElement("!"+command1);
        String format = command1.substring(11).trim().toUpperCase();
        if (format.equals("TRUE")) {
          showMixedUX_ = true;
        } else if (format.equals("FALSE")) {
          showMixedUX_ = false;
        } else {
          out1.println("Error.  Did not recognize SHOWMIXEDUX value  '"
              + format + "'");
        }
      } else if (upcaseCommand.startsWith("QUIT")
          || upcaseCommand.startsWith("EXIT")) {
        returnCode = false;

      } else if (upcaseCommand.startsWith("SET AUTOCOMMIT")) {
        history.addElement("!"+command1);
        String setting = command1.substring(14).trim();
        if (setting.startsWith("true")) {
          connection_.setAutoCommit(true);
        } else if (setting.startsWith("false")) {
          connection_.setAutoCommit(false);
        } else {
          out1
              .println("SET AUTOCOMMIT:  Didn't understand \"" + setting + "\"");
          out1.println("  Usage:  SET AUTOCOMMIT true");
          out1.println("          SET AUTOCOMMIT false");

        }

      } else if (upcaseCommand.startsWith("REUSE CONNECTION")) {
        history.addElement("!"+command1);
        String setting = command1.substring(16).trim();
        if (setting.startsWith("true")) {
          useConnectionPool_ = true;
        } else if (setting.startsWith("false")) {
          useConnectionPool_ = false;
        } else {
          out1.println("REUSE CONNECTION:  Didn't understand \"" + setting
              + "\"");
          out1.println("  Usage:  REUSE CONNECTION true");
          out1.println("          REUSE CONNECTION false");
        }

      } else if (upcaseCommand.startsWith("REUSE STATEMENT")) {
        history.addElement("!"+command1);
        String setting = command1.substring(16).trim();
        if (setting.startsWith("true")) {
          reuseStatement_ = true;
        } else if (setting.startsWith("false")) {
          reuseStatement_ = false;
        } else {
          out1.println("REUSE STATEMENT:  Didn't understand \"" + setting
              + "\"");
          out1.println("  Usage:  REUSE STATEMENT true");
          out1.println("          REUSE STATEMENT false");
        }

      } else if (upcaseCommand.startsWith("SETCLITRACE")) {
        history.addElement("!"+command1);
        boolean b = false;
        boolean ok = false;
        String setting = command1.substring(11).trim();
        if (setting.startsWith("true")) {
          b = true;
          ok = true;
        } else if (setting.startsWith("false")) {
          b = false;
          ok = true;
        }

        if (ok) {
          try {
            Class traceClass = Class.forName("com.ibm.db2.jdbc.app.T");
            Class[] argClasses = new Class[1];
            argClasses[0] = Boolean.TYPE;
            java.lang.reflect.Method method = traceClass.getMethod(
                "setCliTrace", argClasses);
            Object[] args = new Object[1];
            args[0] = new Boolean(b);
            method.invoke(null, args);

          } catch (Exception e) {
            out1.println("Exception while setting cli trace");
            e.printStackTrace(out1);
          }

        } else {
          out1.println("SETCLITRACE:  Didn't understand \"" + setting + "\"");
          out1.println("  Usage:  SETCLITRACE true");
          out1.println("          SETCLITRACE false");
        }

      } else if (upcaseCommand.startsWith("SETDB2TRACE")) {
        history.addElement("!"+command1);

        try {
          String setting = command1.substring(11).trim();

          Class traceClass = Class.forName("com.ibm.db2.jdbc.app.T");
          Class[] argClasses = new Class[1];
          argClasses[0] = Integer.TYPE;
          java.lang.reflect.Method method = traceClass.getMethod("setDb2Trace",
              argClasses);
          Object[] args = new Object[1];
          if (setting.startsWith("true")) {
            args[0] = new Integer(3);
          } else if (setting.startsWith("false")) {
            args[0] = new Integer(0);
          } else {
            args[0] = new Integer(Integer.parseInt(setting));
          }
          method.invoke(null, args);

        } catch (Exception e) {
          out1.println("Exception while setting cli trace");
          e.printStackTrace(out1);
        }

      } else if (upcaseCommand.startsWith("SET TRANSACTIONISOLATION")) {
        history.addElement("!"+command1);
        String setting = command1.substring(24).trim();

        if (setting.startsWith("TRANSACTION_READ_UNCOMMITTED")) {
          connection_
              .setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        } else if (setting.startsWith("TRANSACTION_READ_COMMITTED")) {
          connection_
              .setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        } else if (setting.startsWith("TRANSACTION_REPEATABLE_READ")) {
          connection_
              .setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        } else if (setting.startsWith("TRANSACTION_SERIALIZABLE")) {
          connection_
              .setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        } else {
          out1.println("SET TRANSACTIONISOLATION:  Didn't understand \""
              + setting + "\"");
          out1
              .println("  Usage:  SET TRANSACTIONISOLATION TRANSACTION_READ_UNCOMMITTED");
          out1
              .println("          SET TRANSACTIONISOLATION TRANSACTION_READ_COMMITTED");
          out1
              .println("          SET TRANSACTIONISOLATION TRANSACTION_REPEATABLE_READ");
          out1
              .println("          SET TRANSACTIONISOLATION TRANSACTION_SERIALIZABLE");

        }

      } else if (upcaseCommand.startsWith("USAGE")
          || upcaseCommand.startsWith("HELP")) {
        for (int u = 0; u < commandHelp.length; u++) {
          out1.println(commandHelp[u]);
        }

      } else if (upcaseCommand.startsWith("GETSERVERJOBNAME")) {
        history.addElement("!"+command1);
        try {
          String jobName = ReflectionUtil.callMethod_S(connection_,
              "getServerJobName");
          out1.println("getServerJobName returned " + jobName);
        } catch (java.lang.NoSuchMethodException nsme) {
          try {
            // Check for toolbox Driver
            DatabaseMetaData dmd = connection_.getMetaData();
            String driverName = dmd.getDriverName();
            if (driverName.indexOf("Toolbox") >= 0 || driverName.indexOf("jtopenlite") >= 0) {
              String jobName = "";
              try {
                jobName = ReflectionUtil.callMethod_S(connection_,
                    "getServerJobIdentifier");
                // Reformat the job name it comes in as QZDASOINITQUSER 364288
                if (jobName.length() >= 26) {
                  jobName = jobName.substring(20).trim() + "/"
                      + jobName.substring(10, 20).trim() + "/"
                      + jobName.substring(0, 10).trim();
                }
                out1.println("getServerJobName returned " + jobName);

              } catch (Exception e) {
                out1.println("server jobname is not available");
              }

            } else {
              out1.println("getServerJobName:3 failed with exception " + nsme
                  + " for driver " + driverName);
              nsme.printStackTrace(out1);
            }

          } catch (Exception e) {
            out1.println("getServerJobName:2 failed with 2 exceptions ");
            nsme.printStackTrace(out1);
            e.printStackTrace(out1);
          }
        } catch (Exception e) {
          out1.println("getServerJobName:1 failed with exception " + e);
          e.printStackTrace(out1);
        }
      } else if (upcaseCommand.startsWith("DMD.GETCOLUMNS")) {
        history.addElement("!"+command1);
        try {
          String catalog = null;
          String schemaPattern = null;
          String tableNamePattern = null;
          String columnNamePattern = null;

          DatabaseMetaData dmd = connection_.getMetaData();

          String args = command1.substring(14).trim();
          int commaIndex;
          commaIndex = args.indexOf(",");
          if (commaIndex > 0) {
            catalog = args.substring(0, commaIndex).trim();
            if (catalog.equals("null"))
              catalog = null;
            args = args.substring(commaIndex + 1);
            commaIndex = args.indexOf(",");
            if (commaIndex > 0) {
              schemaPattern = args.substring(0, commaIndex).trim();
              if (schemaPattern.equals("null"))
                schemaPattern = null;
              args = args.substring(commaIndex + 1);
              commaIndex = args.indexOf(",");
              if (commaIndex > 0) {
                tableNamePattern = args.substring(0, commaIndex).trim();
                if (tableNamePattern.equals("null"))
                  tableNamePattern = null;
                columnNamePattern = args.substring(commaIndex + 1).trim();
                if (columnNamePattern.equals("null"))
                  columnNamePattern = null;
              }
            }
          }
          out1.println("Calling dmd.getColumns(" + catalog + ", "
              + schemaPattern + ", " + tableNamePattern + ", "
              + columnNamePattern + ")");
          ResultSet rs = dmd.getColumns(catalog, schemaPattern,
              tableNamePattern, columnNamePattern);

          if (rs != null) {
            dispResultSet(out1, rs, false);
            rs.close();
          }

        } catch (Exception e) {
          out1
              .println("databaseMetaData.getColumns failed with exception " + e);
          e.printStackTrace(out1);
        }
      } else if (upcaseCommand.startsWith("DMD.GETTABLES")) {
        history.addElement("!"+command1);
        try {
          String catalog = null;
          String schemaPattern = null;
          String tableNamePattern = null;
          String typePattern = null;
          String[] types = null;

          DatabaseMetaData dmd = connection_.getMetaData();

          String args = command1.substring(14).trim();
          int commaIndex;
          commaIndex = args.indexOf(",");
          if (commaIndex > 0) {
            catalog = args.substring(0, commaIndex).trim();
            if (catalog.equals("null"))
              catalog = null;
            args = args.substring(commaIndex + 1);
            commaIndex = args.indexOf(",");
            if (commaIndex > 0) {
              schemaPattern = args.substring(0, commaIndex).trim();
              if (schemaPattern.equals("null"))
                schemaPattern = null;
              args = args.substring(commaIndex + 1);
              commaIndex = args.indexOf(",");
              if (commaIndex > 0) {
                tableNamePattern = args.substring(0, commaIndex).trim();
                if (tableNamePattern.equals("null"))
                  tableNamePattern = null;
                typePattern = args.substring(commaIndex + 1).trim();
                if (typePattern.equals("null")) {
                  typePattern = null;
                } else {
                  Vector vectorList = new Vector();
                  int barIndex = typePattern.indexOf('|');
                  while (barIndex > 0) {
                    String thisType = typePattern.substring(0, barIndex);
                    vectorList.add(thisType);

                    typePattern = typePattern.substring(1 + barIndex);
                    barIndex = typePattern.indexOf('|');
                  }
                  vectorList.add(typePattern);

                  int size = vectorList.size();
                  types = new String[size];
                  for (int i = 0; i < size; i++) {
                    types[i] = (String) vectorList.elementAt(i);
                  }
                }
              }
            }
          }
          out1.println("Calling dmd.getTables(" + catalog + ", "
              + schemaPattern + ", " + tableNamePattern + ", " + typePattern
              + "=" + StringFormatUtil.stringArrayContents(types) + ")");
          ResultSet rs = dmd.getTables(catalog, schemaPattern,
              tableNamePattern, types);

          if (rs != null) {
            dispResultSet(out1, rs, false);
            rs.close();
          }

        } catch (Exception e) {
          out1.println("databaseMetaData.getTables failed with exception " + e);
          e.printStackTrace(out1);
        }

      } else if (upcaseCommand.startsWith("DMD.GETINDEXINFO")) {
        history.addElement("!"+command1);
        try {
          String catalog = null;
          String schema = null;
          String table = null;
          boolean booleanUnique = false;
          boolean booleanApproximate = false;

          DatabaseMetaData dmd = connection_.getMetaData();

          String args = command1.substring(16).trim();
          int commaIndex;
          commaIndex = args.indexOf(",");
          if (commaIndex > 0) {
            catalog = args.substring(0, commaIndex).trim();
            if (catalog.equals("null"))
              catalog = null;
            args = args.substring(commaIndex + 1);
            commaIndex = args.indexOf(",");
            if (commaIndex > 0) {
              schema = args.substring(0, commaIndex).trim();
              if (schema.equals("null"))
                schema = null;
              args = args.substring(commaIndex + 1);
              commaIndex = args.indexOf(",");
              if (commaIndex > 0) {
                table = args.substring(0, commaIndex).trim();
                if (table.equals("null"))
                  table = null;
                args = args.substring(commaIndex + 1);
                commaIndex = args.indexOf(",");
                if (commaIndex > 0) {
                  String unique = args.substring(0, commaIndex).trim();
                  booleanUnique = unique.equalsIgnoreCase("true");
                  args = args.substring(commaIndex + 1);
                  String approximate = args;
                  booleanApproximate = approximate.equalsIgnoreCase("true");

                }
              }
            }
          }
          out1.println("Calling dmd.getIndexInfo(" + catalog + ", " + schema
              + ", " + table + ", " + booleanUnique + "," + booleanApproximate
              + ")");
          ResultSet rs = dmd.getIndexInfo(catalog, schema, table,
              booleanUnique, booleanApproximate);

          if (rs != null) {
            dispResultSet(out1, rs, false);
            rs.close();
          }

        } catch (Exception e) {
          out1.println("databaseMetaData.getIndexInfo failed with exception "
              + e);
          e.printStackTrace(out1);
        }

      } else if (upcaseCommand.startsWith("DMD.GETSCHEMAS")) {
        history.addElement("!"+command1);
        try {

          DatabaseMetaData dmd = connection_.getMetaData();

          out1.println("Calling dmd.getSchemas()");
          ResultSet rs = dmd.getSchemas();

          if (rs != null) {
            dispResultSet(out1, rs, false);
            rs.close();
          }

        } catch (Exception e) {
          out1
              .println("databaseMetaData.getSchemas failed with exception " + e);
          e.printStackTrace(out1);
        }
      } else if (upcaseCommand.startsWith("HISTORY.CLEAR")) {
        history.clear();
      } else if (upcaseCommand.startsWith("HISTORY.SHOW")) {
        Enumeration enumeration = history.elements();
        while (enumeration.hasMoreElements()) {
          String info = (String) enumeration.nextElement();
          out1.println(info);
        }
      } else if (upcaseCommand.startsWith("MANUALFETCH")) {
        history.addElement("!"+command1);
        String arg = command1.substring(11).trim().toUpperCase();
        if (arg.equals("TRUE")) {
          manualFetch_ = true;
        } else if (arg.equals("ON")) {
          manualFetch_ = true;
        } else if (arg.equals("FALSE")) {
          manualFetch_ = false;
        } else if (arg.equals("OFF")) {
          manualFetch_ = false;
        } else {
          out1.println("Invalid arg '" + arg + "' for MANUALFETCH");
        }
      } else if (upcaseCommand.startsWith("RS.NEXT")) {
        history.addElement("!"+command1);
        boolean ok = manualResultSet_.next();
        if (ok) {
          dispRow(out1, manualResultSet_, false, manualResultSetNumCols_,
              manualResultSetColType_, manualResultSetColumnLabel_, null, xml_, html_, showLobThreshold_, stringSampleSize_, characterDetails_, showMixedUX_);
        } else {
          out1.println("rs.next returned false");
        }
      } else if (upcaseCommand.startsWith("RS.FIRST")) {
        history.addElement("!"+command1);
        boolean ok = manualResultSet_.first();
        if (ok) {
          dispRow(out1, manualResultSet_, false, manualResultSetNumCols_,
              manualResultSetColType_, manualResultSetColumnLabel_, null, xml_, html_, showLobThreshold_, stringSampleSize_, characterDetails_, showMixedUX_);
        } else {
          out1.println("rs.first returned false");
        }
      } else if (upcaseCommand.startsWith("RS.BEFOREFIRST")) {
        history.addElement("!"+command1);
        manualResultSet_.beforeFirst();
        out1.println("rs.beforeFirst called");
      } else if (upcaseCommand.startsWith("RS.AFTERLAST")) {
        history.addElement("!"+command1);
        manualResultSet_.afterLast();
        out1.println("rs.afterLast called");
      } else if (upcaseCommand.startsWith("RS.LAST")) {
        history.addElement("!"+command1);
        boolean ok = manualResultSet_.last();
        if (ok) {
          dispRow(out1, manualResultSet_, false, manualResultSetNumCols_,
              manualResultSetColType_, manualResultSetColumnLabel_, null, xml_, html_, showLobThreshold_, stringSampleSize_, characterDetails_, showMixedUX_);
        } else {
          out1.println("rs.last returned false");
        }
      } else if (upcaseCommand.startsWith("RS.PREVIOUS")) {
        history.addElement("!"+command1);
        boolean ok = manualResultSet_.previous();
        if (ok) {
          dispRow(out1, manualResultSet_, false, manualResultSetNumCols_,
              manualResultSetColType_, manualResultSetColumnLabel_, null, xml_, html_, showLobThreshold_, stringSampleSize_, characterDetails_, showMixedUX_);
        } else {
          out1.println("rs.previous returned false");
        }
      } else if (upcaseCommand.startsWith("RS.ABSOLUTE")) {
        history.addElement("!"+command1);
        String arg = command1.substring(11).trim().toUpperCase();
        int pos = Integer.parseInt(arg);
        boolean ok = manualResultSet_.absolute(pos);
        if (ok) {
          dispRow(out1, manualResultSet_, false, manualResultSetNumCols_,
              manualResultSetColType_, manualResultSetColumnLabel_, null, xml_, html_, showLobThreshold_, stringSampleSize_, characterDetails_, showMixedUX_);
        } else {
          out1.println("rs.absolute returned false");
        }
      } else if (upcaseCommand.startsWith("RS.RELATIVE")) {
        history.addElement("!"+command1);
        String arg = command1.substring(11).trim().toUpperCase();
        int pos = Integer.parseInt(arg);
        boolean ok = manualResultSet_.relative(pos);
        if (ok) {
          dispRow(out1, manualResultSet_, false, manualResultSetNumCols_,
              manualResultSetColType_, manualResultSetColumnLabel_, null, xml_, html_, showLobThreshold_, stringSampleSize_, characterDetails_, showMixedUX_);
        } else {
          out1.println("rs.relative returned false");
        }
      } else if (upcaseCommand.startsWith("SETVAR")) {
        history.addElement("!"+command1);
        try {
          String left = command1.substring(6).trim();
          int equalsIndex = left.indexOf("=");
          if (equalsIndex > 0) {
            String variableName = left.substring(0, equalsIndex).trim();
            left = left.substring(equalsIndex + 1);
            Object variable = callMethod(left, out1);

            if (variable != null) {
              addVariable(variableName, variable);
              out1.println(variableName + "=" + variable.toString());
            } else {
              out1.println("ERROR:  Method not found or output is null");
            }
          } else {
            /* Check to see if we can set like a parameter */
            /* JWE */
            int spaceIndex = left.indexOf(" ");
            if (spaceIndex > 0) {
              String variableName = left.substring(0, spaceIndex).trim();
              left = left.substring(spaceIndex + 1);

              Object variable = getParameterObject(left, out1);

              if (variable != null) {
                addVariable(variableName, variable);
                out1.println(variableName + "=" + variable.toString());
              } else {
                out1.println("ERROR:  Unable to get parameter ");
              }

            } else {
              out1
                  .println("ERROR:  '=' or ' ' not found after SETVAR [VAR]");
            }

          }

        } catch (Exception e) {
          e.printStackTrace(out1);
        }
      } else if (upcaseCommand.startsWith("SETNEWVAR")) {
        history.addElement("!"+command1);
        try {
          String left = command1.substring(9).trim();
          int equalsIndex = left.indexOf("=");
          if (equalsIndex > 0) {
            String variableName = left.substring(0, equalsIndex).trim();
            left = left.substring(equalsIndex + 1);
            Object variable = callNewMethod(left, out1);

            if (variable != null) {
              addVariable(variableName, variable);
              out1.println(variableName + "=" + variable.toString());
            } else {
              out1.println("ERROR:  Method not found or output is null");
            }
          } else {
            out1.println("line missing =");
          }

        } catch (Exception e) {
          e.printStackTrace(out1);
        }
      } else if (upcaseCommand.startsWith("SHOWVARMETHODS")) {
        history.addElement("!"+command1);
        String left = command1.substring(14).trim();
        showMethods(left, out1);
      } else if (upcaseCommand.startsWith("THREAD ")) {
        history.addElement("!"+command1);
        String newcommand = command1.substring(7).trim();
        out1.println("Starting thread for " + newcommand);
        Main runnable = new Main(newcommand, out1);
        Thread t = new Thread(runnable);
        t.start();
        threads_.add(t);
      } else if (upcaseCommand.startsWith("REPEAT ")) {
        history.addElement("!"+command1);
        String left = command1.substring(7).trim();
        int spaceIndex = left.indexOf(" ");
        if (spaceIndex > 0) {
          int repeatCount = Integer.parseInt(left.substring(0, spaceIndex));
          if (repeatCount > 0) {
            String newCommand = left.substring(spaceIndex).trim();
            int beginCount = repeatCount;
            int iteration = 1;
            while (repeatCount > 0) {
              out1.println("Iteration " + iteration + " of " + beginCount);
              iteration++;
              executeTopLevelCommand(newCommand, out1);
              repeatCount--;
            }
          } else {
            out1.println("Error.. invalid repeat count "
                + left.substring(0, spaceIndex));
          }
        } else {
          out1.println("Error.  No count for repeat");
        }

      } else if (upcaseCommand.startsWith("CALLMETHOD")) {
        history.addElement("!"+command1);
        String left = command1.substring(10).trim();

        Object obj = callMethod(left, out1);
        out1.println("Call returned " + obj);
        if ((obj != null) && (obj instanceof InputStream)) {
          out1.println("InputStream[ ");
          InputStream is = (InputStream) obj;
          int val = is.read();
          while (val > 0) {
            out1.print(" " + Integer.toHexString(val));
            val = is.read();
          }
          out1.println("]");
        }
        if ((obj != null) && (obj.getClass().isArray())) {
          int arrayLength = java.lang.reflect.Array.getLength(obj);
          out1.println("  .. Array of size " + arrayLength);
          for (int i = 0; i < arrayLength; i++) {
            Object obj2 = java.lang.reflect.Array.get(obj, i);
            if (obj2 instanceof DriverPropertyInfo) {
              DriverPropertyInfo info = (DriverPropertyInfo) obj2;
              out1.println("[" + i + "]=" + info.name + " " + info.value
                  + " " + info.description);
            } else if (obj2 instanceof java.lang.Byte) {
              int value = 0xff & ((java.lang.Byte) obj2).intValue();
              out1.println("[" + i + "]=0x" + Integer.toHexString(value)
                  + " a[" + StringFormatUtil.asciiChar(value) + "]" + " e["
                  + StringFormatUtil.ebcdicChar(value) + "]");
            } else {
              out1.println("[" + i + "][" + obj2.getClass().getName()
                  + "]=" + obj2);
            }
          }
        }
      } else {
        //
        // If not a blank line
        //
        if (upcaseCommand.length() != 0) {
          //
          // just attempt to execute the statement
          //
          if (connection_ != null) {
            if (connection_ != null) {
              if (stmt_ != null && reuseStatement_) {
                // dont do anything -- reuse it
              } else {

                if (jdk14_) {
                  stmt_ = connection_.createStatement(resultSetType_,
                      resultSetConcurrency_, resultSetHoldability_);
                } else {
                  stmt_ = connection_.createStatement();
                }
                addVariable("STMT", stmt_);
              }
            }
            if (queryTimeout_ != 0) {
              stmt_.setQueryTimeout(queryTimeout_);
            }
            if (measureExecute_) {
              startTime_ = System.currentTimeMillis();
            }
            stmt_.executeUpdate(command1);
            history.addElement(command1);
            if (measureExecute_) {
              finishTime_ = System.currentTimeMillis();
              out1.println("TIME: " + (finishTime_ - startTime_) + " ms");
            }

            //
            // Don't forget to check for warnings...
            //
            SQLWarning warning = stmt_.getWarnings();
            if (warning != null) {
              if (!silent) {
                dispWarning(out1, warning, hideWarnings_, html_);
              }
            }

          } else {
            out1.println("UNABLE to EXECUTE because not connected");
          }

        }
      }

    } catch (SQLException ex) {

      // A SQLException was generated. Catch it and
      // display the error information. Note that there
      // could be multiple error objects chained
      // together
      if (!silent) {
        processException(ex, command1, out1);
      }
    } catch (Exception e) {
      out1.println("\n*** exception caught *** " + e);
      out1.println("Statement was " + command1);
      e.printStackTrace(out1);
    } catch (java.lang.UnknownError jlu) {
      out1.println("\n*** java.lang.UnknownError caught ***" + jlu);
      out1.println("Statement was " + command1);
      jlu.printStackTrace(out1);

    } // catch
    finally {
      if (stmt_ != null) {
        try {
          if (!reuseStatement_) {
            if (!manualFetch_ && closeStatementRS_) {
              stmt_.close();
              stmt_ = null;
              variables.remove("STMT");
            }
          }
        } catch (Exception e) {
          e.printStackTrace(out1);
        }
      }
    }

    return returnCode;
  }


  private Object callMethod(String left, PrintStream out1) {
    try {
      Object variable = null;
      int paramIndex = left.indexOf("(");
      if (paramIndex > 0) {
        int dotIndex = left.lastIndexOf(".", paramIndex);
        if (dotIndex > 0) {
          String callVariable = left.substring(0, dotIndex).trim();
          Object callObject = variables.get(callVariable);
          Class callClass = null;
          left = left.substring(dotIndex + 1).trim();
          paramIndex = left.indexOf("(");
          String methodName = left.substring(0, paramIndex).trim();
          left = left.substring(paramIndex + 1);
          if (callObject == null) {
            // Try to find the variable as a class
            try {
              callClass = Class.forName(callVariable);
            } catch (Exception e) {
            }
          }
          if (callObject != null || callClass != null) {

            if (paramIndex > 0) {
              Method[] methods;

              // getMethods does not work on connection object for
              // pre JDK 1.4

              if ((callObject instanceof Connection) && (!jdk14_)) {
                if (methodName.equals("commit")) {

                  methods = new Method[1];
                  methods[0] = callObject.getClass().getMethod(methodName,
                      new Class[0]);
                } else {
                  // Try calling zero argument method
                  methods = new Method[1];
                  methods[0] = callObject.getClass().getMethod(methodName,
                      new Class[0]);
                }
              } else {
                if (callObject != null) {
                  methods = callObject.getClass().getMethods();
                } else {
                  // Note:  callClass cannot be null because of callObject != null || callClass != null condition above
                  methods = callClass.getMethods();
                }
              }
              boolean methodFound = false;
              boolean anyMethodFound = false;
              for (int m = 0; !methodFound && (m < methods.length)
                  && (variable == null); m++) {
                int p = 0;
                int methodParameterCount = 0;

                if (methods[m].getName().equals(methodName)) {
                  Class[] parameterTypes = methods[m].getParameterTypes();
                  String argsLeft = left;
                  Object[] parameters = new Object[parameterTypes.length];
                  methodFound = true;
                  anyMethodFound = true;
                  methodParameterCount = parameterTypes.length;

                  String methodParameters = "";
                  for (p = 0; p < parameterTypes.length; p++) {
                    // out1.println("Args left is "+argsLeft);
                    // Handle double quote delimited parameters strings
                    int argStartIndex = 0;
                    int argEndIndex = 0;
                    int nextArgIndex = 0;
                    if ((argsLeft.length() > 1)
                        && ((argsLeft.charAt(0) == '"') || (argsLeft.charAt(0) == '\''))) {
                      argStartIndex = 1;

                      argEndIndex = argsLeft.indexOf(argsLeft.charAt(0), 1);
                      if (argEndIndex > 0) {
                        // check for "," or ")"
                        if ((argsLeft.charAt(argEndIndex + 1) == ',')
                            || (argsLeft.charAt(argEndIndex + 1) == ')')) {
                          nextArgIndex = argEndIndex + 2;
                        } else {
                          out1.println("[,)] does not follow #"
                              + argsLeft.charAt(0) + "#");
                          argEndIndex = -1;
                        }
                      }
                    } else {
                      argEndIndex = argsLeft.indexOf(",");
                      if (argEndIndex < 0) {
                        argEndIndex = argsLeft.indexOf(")");
                      }
                      if (argEndIndex >= 0) {
                        nextArgIndex = argEndIndex + 1;
                      }
                    }
                    if (argEndIndex < 0) {
                      methodFound = false;
                      out1.println("Unable to find arg with remaining args "
                          + argsLeft);
                      out1.println("Number of parameters is "
                          + parameterTypes.length);
                      methodFound = false;
                    } else {
                      if (argStartIndex <= argEndIndex) {
                        String arg = argsLeft.substring(argStartIndex,
                            argEndIndex).trim();

                        argsLeft = argsLeft.substring(nextArgIndex);

                        //
                        // If the arg refers to a variable try to use it
                        //
                        Object argObject = variables.get(arg);
                        if (argObject != null) {
                          parameters[p] = argObject;
                        } else {

                          //
                          // Now convert the arg from a string into something
                          // else
                          //
                          String parameterTypeName = parameterTypes[p]
                              .getName();
                          methodParameters += parameterTypeName + " ";
                          if (arg.equals("null")) {
                            parameters[p] = null;
                          } else if (parameterTypeName.equals("java.lang.String") ||
                              parameterTypeName.equals("java.lang.Object") ) {
                            parameters[p] = arg;
                          } else if (parameterTypeName.equals("boolean")) {
                            try {
                              parameters[p] = new Boolean(arg);
                            } catch (Exception e) {
                              out1.println("Could not parse " + arg
                                  + " as integer");
                              methodFound = false;
                            }
                          } else if (parameterTypeName.equals("short")) {
                            try {
                              parameters[p] = new Short(arg);
                            } catch (Exception e) {
                              out1.println("Could not parse " + arg
                                  + " as short");
                              methodFound = false;
                            }
                          } else if (parameterTypeName.equals("int")) {
                            try {
                              parameters[p] = new Integer(arg);
                            } catch (Exception e) {
                              out1.println("Could not parse " + arg
                                  + " as integer");
                              methodFound = false;
                            }
                          } else if (parameterTypeName.equals("long")) {
                            try {
                              parameters[p] = new Long(arg);
                            } catch (Exception e) {
                              out1.println("Could not parse " + arg
                                  + " as long");
                              methodFound = false;
                            }
                          } else if (parameterTypeName.equals("float")) {
                            try {
                              parameters[p] = new Float(arg);
                            } catch (Exception e) {
                              out1.println("Could not parse " + arg
                                  + " as float");
                              methodFound = false;
                            }
                          } else if (parameterTypeName.equals("double")) {
                            try {
                              parameters[p] = new Double(arg);
                            } catch (Exception e) {
                              out1.println("Could not parse " + arg
                                  + " as double");
                              methodFound = false;
                            }
                          } else if (parameterTypeName
                              .equals("[Ljava.lang.String;")) {
                            if (arg.charAt(0) == '[') {
                              String arrayString = arg.substring(1);
                              int len = arrayString.length();
                              int arrayCount = 1;
                              for (int i = 0; i < len; i++) {
                                if (arrayString.charAt(i) == '+')
                                  arrayCount++;
                              }
                              String[] a = new String[arrayCount];
                              parameters[p] = a;

                              int arrayIndex = 0;
                              int startIndex = 0;
                              int endIndex;
                              endIndex = arrayString.indexOf('+', startIndex);
                              if (endIndex < 0)
                                endIndex = arrayString.indexOf(']', startIndex);
                              while (endIndex > 0) {
                                if (arrayIndex < arrayCount) {
                                  a[arrayIndex] = arrayString.substring(
                                      startIndex, endIndex);
                                  arrayIndex++;
                                }
                                startIndex = endIndex + 1;
                                if (startIndex >= len) {
                                  endIndex = -1;
                                } else {
                                  endIndex = arrayString.indexOf('+',
                                      startIndex);
                                  if (endIndex < 0)
                                    endIndex = arrayString.indexOf(']',
                                        startIndex);
                                }
                              }

                            } else {
                              out1.println("Could not parse " + arg
                                  + " as String array .. try [A+B+C]");
                              methodFound = false;
                            }
                          } else if (parameterTypeName.equals("[I")) {
                            if (arg.charAt(0) == '[') {
                              String arrayString = arg.substring(1);
                              int len = arrayString.length();
                              int arrayCount = 1;
                              for (int i = 0; i < len; i++) {
                                if (arrayString.charAt(i) == '+')
                                  arrayCount++;
                              }
                              int[] a = new int[arrayCount];
                              parameters[p] = a;

                              String piece = "";
                              try {
                                int arrayIndex = 0;
                                int startIndex = 0;
                                int endIndex;
                                endIndex = arrayString.indexOf('+', startIndex);
                                if (endIndex < 0)
                                  endIndex = arrayString.indexOf(']',
                                      startIndex);
                                while (endIndex > 0) {
                                  if (arrayIndex < arrayCount) {
                                    a[arrayIndex] = Integer
                                        .parseInt(arrayString.substring(
                                            startIndex, endIndex));
                                    arrayIndex++;
                                  }
                                  startIndex = endIndex + 1;
                                  if (startIndex >= len) {
                                    endIndex = -1;
                                  } else {
                                    endIndex = arrayString.indexOf('+',
                                        startIndex);
                                    if (endIndex < 0)
                                      endIndex = arrayString.indexOf(']',
                                          startIndex);
                                  }
                                }
                              } catch (Exception e) {
                                out1.println("Exception " + e + " piece = "
                                    + piece);
                                out1.println("Could not parse " + arg
                                    + " as Integer.. try [1+2+3]");
                                methodFound = false;
                              }

                            } else {
                              out1.println("Could not parse " + arg
                                  + " as Integer.. try [1+2+3]");
                              methodFound = false;
                            }
                          } else {
                            out1.println("Did not handle parameter with class "
                                + parameterTypeName);
                            methodFound = false;
                          }
                        } /* parameter was a variable */
                      }
                    } /* unable to find args */
                  }
                  if (methodFound) {
                    if (p == methodParameterCount) {
                      if ((argsLeft.trim().equals(")"))
                          || (argsLeft.trim().length() == 0)) {
                        try {
                          methods[m].setAccessible(true);
                          variable = methods[m].invoke(callObject, parameters);
                        } catch (Exception e) {
                          e.printStackTrace(out1);
                          out1.println("Calling method " + methodName
                              + " with " + methodParameters + " failed");
                          methodFound = false;
                        }
                      } else {
                        out1.println("Not calling method " + methodName
                            + " with " + methodParameters
                            + " because argsLeft = " + argsLeft);
                        methodFound = false;
                      }
                    } else {
                      out1.println("Not calling method " + methodName
                          + " with " + methodParameters
                          + " because parsed parameter count = " + p);
                    }
                  } else {
                    out1.println("Method not found " + methodName);
                  }
                }
              }
              if (!anyMethodFound) {
                out1.println("ERROR:  Method not found " + methodName);
              }

            } else {
              out1.println("ERROR:  could find ( in " + left);
            }
          } else {
            out1.println("ERROR:  could not find variable or class "
                + callVariable);
            showValidVariables(out1);

          }
        } else {
          out1.println("ERROR:  could find . in " + left);
        }
      } else {
        out1.println("ERROR:  could find ( in " + left);
      }
      return variable;
    } catch (Exception e) {
      out1.println("Unexpected exception");
      e.printStackTrace(out1);
      return null;
    } catch (NoClassDefFoundError ncdfe) {
      out1.println("NoClassDefFoundError");
      ncdfe.printStackTrace(out1);
      return null;
    }
  }

  private Object callNewMethod(String left, PrintStream out1) {
    try {
      Object variable = null;
      int paramIndex = left.indexOf("(");
      if (paramIndex > 0) {
        String newClassName = left.substring(0, paramIndex).trim();
        Class newClass = null;
        left = left.substring(paramIndex + 1);
        // Try to find the variable as a class
        try {
          newClass = Class.forName(newClassName);
        } catch (Exception e) {
        }

        if (newClass != null) {
          if (paramIndex > 0) {
            Constructor[] constructors;
            constructors = newClass.getConstructors();
            boolean methodFound = false;
            for (int m = 0; !methodFound && (m < constructors.length)
                && (variable == null); m++) {

              Class[] parameterTypes = constructors[m].getParameterTypes();
              String argsLeft = left;
              Object[] parameters = new Object[parameterTypes.length];
              methodFound = true;
              String methodParameters = "";
              for (int p = 0; p < parameterTypes.length; p++) {
                // out1.println("Args left is "+argsLeft);
                // Handle double quote delimited parameters strings
                int argStartIndex = 0;
                int argEndIndex = 0;
                int nextArgIndex = 0;
                if ((argsLeft.length() > 1) && (argsLeft.charAt(0) == '"')) {
                  argStartIndex = 1;
                  argEndIndex = argsLeft.indexOf('"', 1);
                  if (argEndIndex > 0) {
                    // check for "," or ")"
                    if ((argsLeft.charAt(argEndIndex + 1) == ',')
                        || (argsLeft.charAt(argEndIndex + 1) == ')')) {
                      nextArgIndex = argEndIndex + 2;
                    } else {
                      out1.println("[,)] does not follow ");
                      argEndIndex = -1;
                    }
                  }
                } else {
                  argEndIndex = argsLeft.indexOf(",");
                  if (argEndIndex < 0) {
                    argEndIndex = argsLeft.indexOf(")");
                  }
                  if (argEndIndex >= 0) {
                    nextArgIndex = argEndIndex + 1;
                  }
                }
                if (argEndIndex < 0) {
                  methodFound = false;
                  out1.println("Unable to find arg in " + argsLeft);
                  methodFound = false;
                } else {
                  String arg = argsLeft.substring(argStartIndex, argEndIndex)
                      .trim();

                  argsLeft = argsLeft.substring(nextArgIndex);

                  //
                  // If the arg refers to a variable try to use it
                  //
                  Object argObject = variables.get(arg);
                  if (argObject != null) {
                    parameters[p] = argObject;
                  } else {

                    //
                    // Now convert the arg from a string into something else
                    //
                    String parameterTypeName = parameterTypes[p].getName();
                    methodParameters += parameterTypeName + " ";
                    if (arg.equals("null")) {
                      parameters[p] = null;
                    } else if (parameterTypeName.equals("java.lang.String")) {
                      parameters[p] = arg;
                    } else if (parameterTypeName.equals("int")) {
                      try {
                        parameters[p] = new Integer(arg);
                      } catch (Exception e) {
                        out1.println("Could not parse " + arg
                            + " as integer");
                        methodFound = false;
                      }
                    } else if (parameterTypeName.equals("boolean")) {
                      try {
                        parameters[p] = new Boolean(arg);
                      } catch (Exception e) {
                        out1.println("Could not parse " + arg
                            + " as integer");
                        methodFound = false;
                      }
                    } else if (parameterTypeName.equals("long")) {
                      try {
                        parameters[p] = new Long(arg);
                      } catch (Exception e) {
                        out1.println("Could not parse " + arg
                            + " as long");
                        methodFound = false;
                      }
                    } else if (parameterTypeName.equals("[Ljava.lang.String;")) {
                      if (arg.charAt(0) == '[') {
                        String arrayString = arg.substring(1);
                        int len = arrayString.length();
                        int arrayCount = 1;
                        for (int i = 0; i < len; i++) {
                          if (arrayString.charAt(i) == '+')
                            arrayCount++;
                        }
                        String[] a = new String[arrayCount];
                        parameters[p] = a;

                        int arrayIndex = 0;
                        int startIndex = 0;
                        int endIndex;
                        endIndex = arrayString.indexOf('+', startIndex);
                        if (endIndex < 0)
                          endIndex = arrayString.indexOf(']', startIndex);
                        while (endIndex > 0) {
                          if (arrayIndex < arrayCount) {
                            a[arrayIndex] = arrayString.substring(startIndex,
                                endIndex);
                            arrayIndex++;
                          }
                          startIndex = endIndex + 1;
                          if (startIndex >= len) {
                            endIndex = -1;
                          } else {
                            endIndex = arrayString.indexOf('+', startIndex);
                            if (endIndex < 0)
                              endIndex = arrayString.indexOf(']', startIndex);
                          }
                        }

                      } else {
                        out1.println("Could not parse " + arg
                            + " as String array .. try [A+B+C]");
                        methodFound = false;
                      }
                    } else if (parameterTypeName.equals("[I")) {
                      if (arg.charAt(0) == '[') {
                        String arrayString = arg.substring(1);
                        int len = arrayString.length();
                        int arrayCount = 1;
                        for (int i = 0; i < len; i++) {
                          if (arrayString.charAt(i) == '+')
                            arrayCount++;
                        }
                        int[] a = new int[arrayCount];
                        parameters[p] = a;

                        String piece = "";
                        try {
                          int arrayIndex = 0;
                          int startIndex = 0;
                          int endIndex;
                          endIndex = arrayString.indexOf('+', startIndex);
                          if (endIndex < 0)
                            endIndex = arrayString.indexOf(']', startIndex);
                          while (endIndex > 0) {
                            if (arrayIndex < arrayCount) {
                              a[arrayIndex] = Integer.parseInt(arrayString
                                  .substring(startIndex, endIndex));
                              arrayIndex++;
                            }
                            startIndex = endIndex + 1;
                            if (startIndex >= len) {
                              endIndex = -1;
                            } else {
                              endIndex = arrayString.indexOf('+', startIndex);
                              if (endIndex < 0)
                                endIndex = arrayString.indexOf(']', startIndex);
                            }
                          }
                        } catch (Exception e) {
                          out1.println("Exception " + e + " piece = "
                              + piece);
                          out1.println("Could not parse " + arg
                              + " as Integer.. try [1+2+3]");
                          methodFound = false;
                        }

                      } else {
                        out1.println("Could not parse " + arg
                            + " as Integer.. try [1+2+3]");
                        methodFound = false;
                      }
                    } else {
                      out1.println("Did not handle parameter with class "
                          + parameterTypeName);
                      methodFound = false;
                    }
                  } /* parameter was not a variable */
                } /* unable to find args */
              } /* looping through parameter types */
              if (methodFound) {
                if ((argsLeft.trim().equals(")"))
                    || (argsLeft.trim().length() == 0)) {
                  try {
                    variable = constructors[m].newInstance(parameters);
                  } catch (Exception e) {
                    e.printStackTrace(out1);
                    out1.println("Creating object  with "
                        + methodParameters + " failed");
                    methodFound = false;
                  }
                } else {
                  out1.println("Not calling constructor " + " with "
                      + methodParameters + " because argsLeft = " + argsLeft);
                  methodFound = false;
                }
              } /* method not found */
            } /* for loop for constructors */
          } else {
            out1.println("ERROR:  could find ( in " + left);
          }
        } else {
          out1.println("ERROR:  could not find variable or class "
              + newClassName);

        }
      } else {
        out1.println("ERROR:  could find ( in " + left);
      }
      return variable;
    } catch (Exception e) {
      out1.println("Unexpected exception");
      e.printStackTrace(out1);
      return null;
    } catch (NoClassDefFoundError ncdfe) {
      out1.println("NoClassDefFoundError");
      ncdfe.printStackTrace(out1);
      return null;
    }
  }

  private void showValidVariables(PrintStream out1) {
    out1.println("Valid variables are the following");
    Enumeration keys = variables.keys();
    while (keys.hasMoreElements()) {
      out1.println(keys.nextElement());
    }
  }

  private void showMethods(String left, PrintStream out1) {
    String callVariable = left.trim();
    Object callObject = variables.get(callVariable);
    Class callClass = null;
    if (callObject == null) {
      try {
        callClass = Class.forName(callVariable);
      } catch (Exception e) {
      }
    }
    if (callObject != null || callClass != null) {
      Method[] methods;
      if (callObject != null) {
        methods = callObject.getClass().getMethods();
      } else {
        // callClass cannot be null because of callObject != null || callClass != null condition above
        methods = callClass.getMethods();
      }
      for (int m = 0; (m < methods.length); m++) {
        String methodInfo;
        Class returnType = methods[m].getReturnType();
        if (returnType != null) {
          methodInfo = returnType.getName() + " " + methods[m].getName();
        } else {
          methodInfo = "void " + methods[m].getName();
        }
        Class[] parameterTypes = methods[m].getParameterTypes();
        methodInfo += "(";
        for (int p = 0; p < parameterTypes.length; p++) {
          String parameterTypeName = parameterTypes[p].getName();
          if (p > 0)
            methodInfo += ",";
          methodInfo += parameterTypeName;
        }
        methodInfo += ")";
        out1.println(methodInfo);
      }
    } else {
      out1.println("Could not find variable " + callVariable);
      showValidVariables(out1);
    }
  }


  static private String[] dispColumnHeadings(PrintStream out1, ResultSet rs,
      ResultSetMetaData rsmd, boolean trim, int numCols, boolean html, boolean xml) throws SQLException {
    int i;
    // Display column headings

    // Build up the output so it can be sent as a single out1.println()
    StringBuffer output = new StringBuffer();

    if (html) {
      output.append("<table border>\n");
    }

    String[] columnLabel = new String[numCols + 1];

    for (i = 1; i <= numCols; i++) {
      columnLabel[i] = rsmd.getColumnLabel(i);
      if (html) {
        output.append("<th>" + columnLabel[i].replace('_', ' '));
      } else {
        if (!xml) {
          if (i > 1)
            output.append(",");
          output.append(columnLabel[i]);
        }
      }
    }
    if (html)
      output.append("<tr>\n");
    if (xml) {
      output.append("<table>");
      out1.println(output.toString());
    } else {
      output.append("");
      out1.println(output.toString());
    }

    return columnLabel;
  }

  /* @SuppressWarnings("fallthrough") */
  static private void dispRow(PrintStream out1, ResultSet rs, boolean trim,
      int numCols, int colType[], String columnLabel[], String format[],
      boolean xml, boolean html,
      int showLobThreshold, int stringSampleSize, boolean characterDetails, boolean showMixedUX)
      throws SQLException {
    int i;
    StringBuffer output = new StringBuffer();
    if (xml)
      output.append("<row>\n");
    for (i = 1; i <= numCols; i++) {
      if (html) {
        output.append("<td>");
      } else if (xml) {
        output.append("   <" + columnLabel[i] + ">");
      } else {
        if (i > 1)
          output.append(",");
      }
      //
      // Handle blob and binary types...
      //

      switch (colType[i]) {

      case 2004: // Types.BLOB
        // case Types.BLOB:
      {
        Blob blob = rs.getBlob(i);
        if (blob != null) {
          if (blob.getClass().getName().equals(
              "com.ibm.db2.jdbc.app.DB2BlobLocator")) {
            try {
              int loc = ReflectionUtil.callMethod_I(blob, "getLocator");
              output.append("L#" + loc + ":");
            } catch (Exception e) {
              // just ignore
            }
          }
        }
      }
        // Fall through
      case Types.BINARY:
      case Types.VARBINARY:
      case Types.LONGVARBINARY:
      case -8: // rowId
        byte bytes[] = rs.getBytes(i);
        if (bytes == null) {
          output.append(rs.getString(i));
        } else {
          if (bytes.length < showLobThreshold) {
            for (int j = 0; j < bytes.length; j++) {
              int showInt = bytes[j] & 0xFF;
              if (showInt >= 0x10) {
                output.append(Integer.toHexString(showInt));
              } else {
                output.append("0" + Integer.toHexString(showInt));
              }
            }
          } else {
            CRC32 checksum = new CRC32();
            checksum.update(bytes);
            output.append("ARRAY[size=" + bytes.length + ",CRC32="
                + checksum.getValue() + "]");
          }
        }
        break;
      default: {
        String outString = rs.getString(i);
        if (trim && outString != null)
          outString = outString.trim();
        if (format != null && (i - 1) < format.length && format[i - 1] != null) {
          outString = formatString(outString, format[i - 1]);
        }
        appendUnicodeString(output, outString, showLobThreshold, stringSampleSize, characterDetails, showMixedUX);
      }
        break;
      }
      if (xml) {
        output.append("</" + columnLabel[i] + ">\n");
      }
    } /* for i */
    if (html) {
      out1.println(output.toString() + "<tr>");
    } else if (xml) {
      out1.println(output.toString() + "</row>");
    } else {
      out1.println(output.toString());
    }

  }

  // -------------------------------------------------------------------
  // dispResultSet
  // Displays all columns and rows in the given result set
  // -------------------------------------------------------------------

  private static String stripTS(String s) {
    s = s.replace(' ', '-');
    s = s.replace(':', '.');
    int i = s.indexOf(".000000");
    if (i > 0) {
      s = s.substring(0, i);
    }
    return s;
  }

  private static String formatString(String outString, String format) {
    if (format != null) {
      int replaceIndex = format.indexOf("{STUFF}");
      if (replaceIndex > 0) {
        return formatString(outString, format.substring(0, replaceIndex))
            + outString
            + formatString(outString, format.substring(replaceIndex + 7));
      } else if (replaceIndex == 0) {
        return outString
            + formatString(outString, format.substring(replaceIndex + 7));
      } else {
        replaceIndex = format.indexOf("{STRIPPEDTS}");
        if (replaceIndex > 0) {
          return formatString(outString, format.substring(0, replaceIndex))
              + stripTS(outString)
              + formatString(outString, format.substring(replaceIndex + 12));
        } else if (replaceIndex == 0) {
          return stripTS(outString)
              + formatString(outString, format.substring(replaceIndex + 12));
        } else {

          replaceIndex = format.indexOf("{PART");
          if (replaceIndex >= 0) {
            int endBrace = format.indexOf("}", replaceIndex);
            int length = endBrace - replaceIndex + 1;
            int number = format.charAt(replaceIndex + 5) - '0';
            String separator = format.substring(replaceIndex + 9, endBrace);
            String part = getNthItem(outString, separator, number);
            if (replaceIndex > 0) {
              return formatString(outString, format.substring(0, replaceIndex))
                  + part
                  + formatString(outString, format.substring(replaceIndex
                      + length));
            } else { /* index must be zero */
              return part
                  + formatString(outString, format.substring(replaceIndex
                      + length));
            }
          } else {
            return format;
          }
        } /* not STRIPPEDTS */
      } /* not STUFF */
    } else {
      return outString;
    }

  }

  private static String getNthItem(String outString, String separator,
      int number) {
    String rest = outString;
    int separatorLength = separator.length();
    int separatorIndex = rest.indexOf(separator);
    int count = 1;
    while (separatorIndex > 0) {
      if (count == number) {
        return rest.substring(0, separatorIndex);
      }
      rest = rest.substring(separatorIndex + separatorLength);
      separatorIndex = rest.indexOf(separator);
      count++;
    }
    return rest;
  }

  //
  // Convenience method to display result set
  //
  public static void dispResultSet(ResultSet rs) throws SQLException {
    dispResultSet(System.out, rs, false, null, false, false, 16384, 16384, true, true, false );
  }

  void dispResultSet(PrintStream out1, ResultSet rs, boolean trim)
      throws SQLException {
    dispResultSet(out1, rs, trim, null, xml_, html_, showLobThreshold_, stringSampleSize_, characterDetails_, showMixedUX_, hideWarnings_);
  }

  static void dispResultSet(PrintStream out1, ResultSet rs, boolean trim,
      String[] format, boolean xml, boolean html,
      int showLobThreshold, int stringSampleSize, boolean characterDetails, boolean showMixedUX, boolean hideWarnings) throws SQLException {
    int i;

    // Get the ResultSetMetaData. This will be used for
    // the column headings

    ResultSetMetaData rsmd = rs.getMetaData();

    // Get the number of columns in the result set

    int numCols = rsmd.getColumnCount();

    String[] columnLabel = dispColumnHeadings(out1, rs, rsmd, trim, numCols, xml, html);

    //
    // figure out column types
    //
    int colType[] = new int[numCols + 1];
    for (i = 1; i <= numCols; i++) {
      colType[i] = rsmd.getColumnType(i);
      if (false)
        out1.println("Type of column " + i + " is " + colType[i]);
    }

    // Display data, fetching until end of the result set

    boolean more = rs.next();
    while (more) {

      // Loop through each column, getting the
      // column data and displaying
      dispRow(out1, rs, trim, numCols, colType, columnLabel, format, xml, html, showLobThreshold, stringSampleSize, characterDetails, showMixedUX);

      //
      // Check for warnings.
      //
      SQLWarning warning = rs.getWarnings();
      if (warning != null) {
        dispWarning(out1, warning, hideWarnings, html);
      }

      // Fetch the next result set row

      more = rs.next();
    }

    if (html)
      out1.println("</table>");
    if (xml)
      out1.println("</table>");

  }

  static private void dispWarning(PrintStream out1, SQLWarning warning, boolean hideWarnings, boolean html) {
    if (hideWarnings) {
      return;
    }
    if (warning != null) {
      out1.println("\n *** Warning ***\n");
      if (html)
        out1.println("<br>");
      while (warning != null) {
        out1.println("SQLState: " + warning.getSQLState());
        if (html)
          out1.println("<br>");
        out1.println("Message:  " + cleanupMessage(warning.getMessage()));
        if (html)
          out1.println("<br>");
        out1.println("Vendor:   " + warning.getErrorCode());
        if (html)
          out1.println("<br>");
        out1.println("");
        if (html)
          out1.println("<br>");
        warning = warning.getNextWarning();
      }

    }
  }

  private static String cleanupMessage(String message) {
    boolean invalidCharacter = false;
    //
    // Check to see if invalid character
    //
    char chars[] = message.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if ((chars[i] < 0x20) || (chars[i] > 0x7e)) {
        invalidCharacter = true;
        chars[i] = '*';
      }
    }
    if (!invalidCharacter) {
      return message;
    } else {
      return new String(chars);
    }
  }

  private void printArray(PrintStream out1, Array outArray) throws SQLException {
    if (outArray == null) {
      out1.print("nullArray");
    } else {
      Object[] array = (Object[]) outArray.getArray();
      if (array == null) {
        out1.print("XXXX_null_returned_from_outArray.getArray");
      } else {
        String componentTypeName = array.getClass().getComponentType()
            .getName();
        out1.print(componentTypeName + "[" + array.length + "]=[");
        for (int i = 0; i < array.length; i++) {
          if (i > 0)
            out1.print(",");
          if (array[i] == null) {
            out1.print("null");
          } else {
            if (componentTypeName.equals("[B")) {
              out1.print(StringFormatUtil.dumpBytes((byte[]) array[i]));
            } else if (array[i] instanceof java.sql.Blob) {
              java.sql.Blob blob = (java.sql.Blob) array[i];
              long length = blob.length();
              out1.print(StringFormatUtil.dumpBytes(blob.getBytes(1,
                  (int) length)));
            } else if (array[i] instanceof java.sql.Clob) {
              java.sql.Clob clob = (java.sql.Clob) array[i];
              long length = clob.length();
              out1.print(clob.getSubString(1, (int) length));
            } else if (array[i] instanceof String) {
              printUnicodeString(out1, (String) array[i]);
            } else {
              out1.print(array[i].toString());
            }
          }
        }
        out1.print("]");
      }
    }

  }

  private void printUnicodeString(PrintStream out1, String outString) {
    //
    // Check length
    //
    if (outString != null) {
      int length = outString.length();
      if (length > showLobThreshold_) {
        out1.print("CHARARRAY[size=" + length + ",CRC32=" + getCRC32(outString)
            + "]->");
        if (!characterDetails_) {
          outString = outString.substring(0, stringSampleSize_);
        }
      }
    }
    //
    // See if all the characters are 7 bit ASCII.. If so just print
    //
    if (outString != null) {
      char chars[] = outString.toCharArray();
      boolean nonAsciiFound = false;
      for (int i = 0; !nonAsciiFound && i < chars.length; i++) {
        if (chars[i] != 0x0d && chars[i] != 0x0a && chars[i] != 0x09
            && (chars[i] >= 0x7F || chars[i] < 0x20)) {
          nonAsciiFound = true;
        }
      }
      if (!nonAsciiFound) {
        out1.print(outString);
      } else {
        if (showMixedUX_) {
          showMixedUXOutput(out1, chars);
        } else {
          out1.print("U'");
          for (int i = 0; i < chars.length; i++) {
            int showInt = chars[i] & 0xFFFF;

            if ((showInt > 0xFF00 && showInt < 0xFF5F) || (showInt == 0x3000)) {
              //
              // Show fat characters - right now don't worry that its in the U'
              //
              if (showInt == 0x3000) {
                out1.print("| ");
              } else {
                out1.print('|');
                out1.print((char) (showInt - 0xFEE0));
              }
            } else {
              String showString = Integer.toHexString(showInt);
              if (showInt >= 0x1000) {
                out1.print(showString);
              } else if (showInt >= 0x0100) {
                out1.print("0" + showString);
              } else if (showInt >= 0x0010) {
                out1.print("00" + showString);
              } else {
                out1.print("000" + showString);
              }
            }
          }
          out1.print("'");
        }
      }
    } else {
      out1.print(outString);
    }
  }

  static private void appendUnicodeString(StringBuffer sb, String outString,
      int showLobThreshold, int stringSampleSize, boolean characterDetails, boolean showMixedUX) {
    //
    // Check length
    //
    if (outString != null) {
      int length = outString.length();
      if (length > showLobThreshold) {
        sb.append("CHARARRAY[size=" + length + ",CRC32=" + getCRC32(outString)
            + "]->");
        if (!characterDetails) {
          outString = outString.substring(0, stringSampleSize);
        }
      }
    }
    //
    // See if all the characters are 7 bit ASCII.. If so just print
    //
    if (outString != null) {
      char chars[] = outString.toCharArray();
      boolean nonAsciiFound = false;
      for (int i = 0; !nonAsciiFound && i < chars.length; i++) {
        if (chars[i] != 0x0d && chars[i] != 0x0a && chars[i] != 0x09
            && (chars[i] >= 0x7F || chars[i] < 0x20)) {
          nonAsciiFound = true;
        }
      }
      if (!nonAsciiFound) {
        sb.append(outString);
      } else {
        if (showMixedUX) {
          appendMixedUXOutput(sb, chars);
        } else {
          sb.append("U'");
          for (int i = 0; i < chars.length; i++) {
            int showInt = chars[i] & 0xFFFF;

            if ((showInt > 0xFF00 && showInt < 0xFF5F) || (showInt == 0x3000)) {
              //
              // Show fat characters - right now don't worry that its in the U'
              //
              if (showInt == 0x3000) {
                sb.append("| ");
              } else {
                sb.append('|');
                sb.append((char) (showInt - 0xFEE0));
              }
            } else {
              String showString = Integer.toHexString(showInt);
              if (showInt >= 0x1000) {
                sb.append(showString);
              } else if (showInt >= 0x0100) {
                sb.append("0" + showString);
              } else if (showInt >= 0x0010) {
                sb.append("00" + showString);
              } else {
                sb.append("000" + showString);
              }
            }
          }
          sb.append("'");
        }
      }
    } else {
      sb.append(outString);
    }
  }

  public void showMixedUXOutput(PrintStream out1, char[] chars) {
    boolean inUX = false;
    for (int i = 0; i < chars.length; i++) {
      int showInt = chars[i] & 0xFFFF;

      if (showInt == 0x0a || showInt >= 0x20 && showInt < 0x7F) {
        if (inUX) {
          out1.print("''");
          inUX = false;
        }
        out1.print(chars[i]);
      } else {
        if (!inUX) {
          inUX = true;
          out1.print("UX''");
        }

        String showString = Integer.toHexString(showInt);

        if (showInt >= 0x1000) {
          out1.print(showString);
        } else if (showInt >= 0x0100) {
          out1.print("0" + showString);
        } else if (showInt >= 0x0010) {
          out1.print("00" + showString);
        } else {
          out1.print("000" + showString);
        }
      }
    } /* for */
    if (inUX) {
      out1.print("''");
    }
  }

  public static void appendMixedUXOutput(StringBuffer sb, char[] chars) {
    boolean inUX = false;
    for (int i = 0; i < chars.length; i++) {
      int showInt = chars[i] & 0xFFFF;

      if (showInt == 0x0a || showInt >= 0x20 && showInt < 0x7F) {
        if (inUX) {
          sb.append("''");
          inUX = false;
        }
        sb.append(chars[i]);
      } else {
        if (!inUX) {
          inUX = true;
          sb.append("UX''");
        }

        String showString = Integer.toHexString(showInt);

        if (showInt >= 0x1000) {
          sb.append(showString);
        } else if (showInt >= 0x0100) {
          sb.append("0" + showString);
        } else if (showInt >= 0x0010) {
          sb.append("00" + showString);
        } else {
          sb.append("000" + showString);
        }
      }
    } /* for */
    if (inUX) {
      sb.append("''");
    }
  }

  /* Get the parameter object from the parameter string */
  public Object getParameterObject(String thisParm, PrintStream out1) {

    if (thisParm.indexOf("UX'") == 0) {
      int len = thisParm.length();
      thisParm = thisParm.substring(3, len - 1);
      if (thisParm.indexOf("null") >= 0) {
        return null;
      } else {
        String stuffString = null;
        try {
          // HANDLE a unicode string
          len = len - 4;
          char[] stuff = new char[len / 4];
          for (int i = 0; i < stuff.length; i++) {
            String piece = thisParm.substring(i * 4, i * 4 + 4);
            stuff[i] = (char) Integer.parseInt(piece, 16);
          }
          stuffString = new String(stuff);
        } catch (Exception e) {
          out1.println("Processing of " + thisParm + " failed");
          e.printStackTrace(out1);
        }
        return stuffString;
      }
    } else if (thisParm.indexOf("X'") == 0) {
      int len = thisParm.length();
      thisParm = thisParm.substring(2, len - 1);
      if (thisParm.indexOf("null") >= 0) {
        return null;
      } else {
        byte[] stuff = null;
        try {
          // HANDLE a byte array
          len = len - 3;
          stuff = new byte[len / 2];
          for (int i = 0; i < stuff.length; i++) {
            String piece = thisParm.substring(i * 2, i * 2 + 2);
            stuff[i] = (byte) Integer.parseInt(piece, 16);
          }
        } catch (Exception e) {
          out1.println("Processing of " + thisParm + " failed");
          e.printStackTrace(out1);
        }
        return stuff;
      }

    } else if (thisParm.indexOf("FILEBLOB=") == 0) {
      java.sql.Blob blob = null;
      try {
        String filename = thisParm.substring(9).trim();
        // Read the file into a byte array and create a lob
        byte[] stuff = null;

        File file = new File(filename);
        int length = (int) file.length();
        stuff = new byte[length];
        FileInputStream inputStream = new FileInputStream(filename);
        inputStream.read(stuff);
        inputStream.close();

        blob = new ClientBlob(stuff);

      } catch (Exception e) {
        out1.println("Processing of " + thisParm + " failed because of " + e);
        e.printStackTrace(out1);
      }
      return blob;
    } else if (thisParm.indexOf("FILECLOB=") == 0) {
      java.sql.Clob clob = null;
      try {
        String filename = thisParm.substring(9).trim();
        // Read the file into a byte array and create a lob
        char[] stuff = null;
        File file = new File(filename);
        int length = (int) file.length();
        stuff = new char[length];
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        reader.read(stuff, 0, length);

        clob = new ClientClob(new String(stuff));

      } catch (Exception e) {
        out1.println("Processing of " + thisParm + " failed because of " + e);
        e.printStackTrace(out1);
      }
      return clob;
    } else if (thisParm.indexOf("SAVEDPARM=") == 0) {
      out1.println("ERROR:  SAVEDPARM not supported yet");
    } else if (thisParm.indexOf("SQLARRAY[") == 0) {
      out1.println("ERROR:  SQLARRAY not supported yet");
      /* handleSqlarrayParm(cstmt, thisParm, parm, out); */
    } else if (thisParm.indexOf("GEN_BYTE_ARRAY+") == 0) {
      return getGenByteArrayParm(thisParm, out1);
    } else if (thisParm.indexOf("GEN_HEX_STRING+") == 0) {
      out1.println("ERROR:  GEN_HEX_STRING+ not supported yet");
      /* handleGenHexStringParm(cstmt, thisParm, parm, out); */
    } else if (thisParm.indexOf("GEN_CHAR_ARRAY+") == 0) {
      out1.println("ERROR:  GEN_CHAR_ARRAY+ not supported yet");
    }
    /* Otherwise, just return the string */
    return thisParm;
  }

  public void setParameter(PreparedStatement cstmt, String thisParm, int parm,
      PrintStream out) throws SQLException {

    if (thisParm.indexOf("UX'") == 0) {
      handleUnicodeStringParm(cstmt, thisParm, parm, out);
    } else if (thisParm.indexOf("X'") == 0) {
      handleByteArrayParm(cstmt, thisParm, parm, out);
    } else if (thisParm.indexOf("FILEBLOB=") == 0) {
      handleFileBlobParm(cstmt, thisParm, parm, out);
    } else if (thisParm.indexOf("FILECLOB=") == 0) {
      handleFileClobParm(cstmt, thisParm, parm, out);
    } else if (thisParm.indexOf("SAVEDPARM=") == 0) {
      handleSavedParm(cstmt, thisParm, parm, out);
    } else if (thisParm.indexOf("SQLARRAY[") == 0) {
      handleSqlarrayParm(cstmt, thisParm, parm, out);
    } else if (thisParm.indexOf("GEN_BYTE_ARRAY+") == 0) {
      handleGenByteArrayParm(cstmt, thisParm, parm, out);
    } else if (thisParm.indexOf("GEN_HEX_STRING+") == 0) {
      handleGenHexStringParm(cstmt, thisParm, parm, out);
    } else if (thisParm.indexOf("GEN_CHAR_ARRAY+") == 0) {
      String stuffString = null;

      int specifiedLength = -1; // the user specified length
      try {
        String specifiedLengthString = null;
        String charSetString = null;
        int charSet = -1; // ccsid indentification
        int indexC = -1; // to hold index of delimitor ('C')

        // get my delimited numbers
        String chopped = (thisParm.substring(15)).trim();
        // System.out.println("chopped = "+chopped);
        indexC = chopped.indexOf("C");
        // System.out.println("indexC = "+indexC);
        specifiedLengthString = chopped.substring(0, indexC);
        // System.out.println("specifiedLengthString = "+specifiedLengthString);
        charSetString = (chopped.substring(indexC + 1)).trim();
        // System.out.println("charSetString = "+charSetString);
        specifiedLength = Integer.parseInt(specifiedLengthString);
        // System.out.println("specifiedLength = "+specifiedLength);
        charSet = Integer.parseInt(charSetString);
        // System.out.println("charSet = "+charSet);

        char[] chars37 = { 'a', 'b', 'c', 'd' };
        char[] chars1208 = { '\u00c0', '\u35c0', '\ub5a0', '\u3055', '\u31ff',
            '\u3066' };
        char[] chars13488 = { '\u3055', '\u31ff', '\u3066' };
        char[] chars1200 = { '\u00c0', '\u35c0', '\ub5a0', '\u3055', '\u31ff',
            '\u3066' };
        char[] chars835 = { '\u5e03', '\u5f17', '\u672b', '\u5378', '\u59cb',
            '\u8679', '\u500c', '\u89f4', '\u9853', '\u8271', '\u8f44',
            '\u977e', '\u52f1' };

        char[] baseChars = new char[specifiedLength];
        char[] stuff = new char[specifiedLength];

        switch (charSet) {
        case 37:
          baseChars = chars37;
          break;
        case 835:
          baseChars = chars835;
          break;

        case 1200:
          baseChars = chars1200;
          break;

        case 1208:
          baseChars = chars1208;
          break;

        case 13488:
          baseChars = chars13488;
          break;
        }

        for (int i = 0; i < specifiedLength; i++) {

          // System.out.println(i);
          stuff[i] = (char) baseChars[i % baseChars.length];

        }
        // System.out.println("after the switch for loop");
        stuffString = new String(stuff);
      } catch (Exception e) {
        out.println("Processing of " + thisParm + " failed");
        e.printStackTrace(out);
      }
      cstmt.setString(parm, stuffString);
      out.println("CHARARRAY[size=" + specifiedLength + ",CRC32="
          + getCRC32(stuffString) + ",CRC32xor1=" + getCRC32xor1(stuffString)
          + "]");
      SQLWarning warning = cstmt.getWarnings();
      if (warning != null) {
        if (!silent) {
          dispWarning(out, warning, hideWarnings_, html_);
        }
      }

    } else {

      // String surrounding quotes
      if (thisParm.indexOf("'") == 0) {
        int lastQuote = thisParm.indexOf("'", 1);
        if (lastQuote > 0) {
          thisParm = thisParm.substring(1, lastQuote);
        } else {
          thisParm = thisParm.substring(1);
        }
      }

      cstmt.setString(parm, thisParm);
      SQLWarning warning = cstmt.getWarnings();
      if (warning != null) {
        if (!silent) {
          dispWarning(out, warning, hideWarnings_, html_);
        }
      }

    }
  }

  static String getCRC32(String input) {
    if (input == null) return null; 
    int length = input.length();
    byte[] byteArray = new byte[2 * length];
    for (int i = 0; i < length; i++) {
      int c = (int) input.charAt(i);
      byteArray[2 * i] = (byte) ((c & 0xFF00) >> 16);
      byteArray[2 * i + 1] = (byte) (c & 0xFF);
    }
    CRC32 checksum = new CRC32();
    checksum.update(byteArray);
    return "" + checksum.getValue();
  }

  String getCRC32xor1(String input) {
    if (input == null) return null; 
    int length = input.length();
    byte[] byteArray = new byte[2 * length];
    for (int i = 0; i < length; i++) {
      int c = (int) input.charAt(i);
      byteArray[2 * i] = (byte) ((c & 0xFF00) >> 16);
      if (byteArray[2 * i] == (byte) 0xD8) {
        // Don't xor unicod D8XX
        byteArray[2 * i + 1] = (byte) ((c) & 0xFF);
      } else {
        byteArray[2 * i + 1] = (byte) ((c ^ 1) & 0xFF);
      }
    }
    CRC32 checksum = new CRC32();
    checksum.update(byteArray);
    return "" + checksum.getValue();
  }

  void handleUnicodeStringParm(PreparedStatement cstmt1, String thisParm,
      int parm, PrintStream out1) throws SQLException {

    String stuffString = (String) getParameterObject(thisParm, out1);
    cstmt1.setString(parm, stuffString);
    SQLWarning warning = cstmt1.getWarnings();
    if (warning != null) {
      if (!silent) {
        dispWarning(out1, warning, hideWarnings_, html_);
      }
    }

  }

  void handleByteArrayParm(PreparedStatement cstmt1, String thisParm, int parm,
      PrintStream out1) throws SQLException {
    byte[] stuff = (byte[]) getParameterObject(thisParm, out1);
    cstmt1.setBytes(parm, stuff);
    SQLWarning warning = cstmt1.getWarnings();
    if (warning != null) {
      if (!silent) {
        dispWarning(out1, warning, hideWarnings_, html_);
      }
    }
  }

  byte[] getGenByteArrayParm(String thisParm, PrintStream out1) {
    byte[] stuff = null;
    try {
      String lengthString = thisParm.substring(15);
      int length = Integer.parseInt(lengthString);
      stuff = new byte[length];
      for (int i = 0; i < length; i++) {
        stuff[i] = (byte) (i & 0xFF);
      }
    } catch (Exception e) {
      out1.println("Processing of " + thisParm + " failed");
      e.printStackTrace(out1);
    }
    return stuff;
  }

  void handleGenByteArrayParm(PreparedStatement cstmt1, String thisParm,
      int parm, PrintStream out1) throws SQLException {
    byte[] stuff = getGenByteArrayParm(thisParm, out1);
    cstmt1.setBytes(parm, stuff);

    CRC32 checksum = new CRC32();
    checksum.update(stuff);
    out1.println("GEN_BYTE_ARRAY generated array of size = " + stuff.length
        + " with checksum of " + checksum.getValue());
  }

  void handleSavedParm(PreparedStatement cstmt1, String thisParm, int parm,
      PrintStream out1) throws SQLException {

    int number = 0;
    try {
      String parmNumber = thisParm.substring(10);
      number = Integer.parseInt(parmNumber);
    } catch (Exception e) {
      out1.println("Processing of " + thisParm + " failed");
      e.printStackTrace(out1);
    }
    cstmt1.setString(parm, savedStringParm_[number]);
    out1.println("SAVEDPARM set(" + parm + "," + savedStringParm_[number]
        + " from saved " + number);
    SQLWarning warning = cstmt1.getWarnings();
    if (warning != null) {
      if (!silent) {
        dispWarning(out1, warning, hideWarnings_, html_);
      }
    }

  }

  static void appendDigit(StringBuffer buffer, int digit) {
    switch (digit) {
    case 0:
      buffer.append('0');
      break;
    case 1:
      buffer.append('1');
      break;
    case 2:
      buffer.append('2');
      break;
    case 3:
      buffer.append('3');
      break;
    case 4:
      buffer.append('4');
      break;
    case 5:
      buffer.append('5');
      break;
    case 6:
      buffer.append('6');
      break;
    case 7:
      buffer.append('7');
      break;
    case 8:
      buffer.append('8');
      break;
    case 9:
      buffer.append('9');
      break;
    case 10:
      buffer.append('a');
      break;
    case 11:
      buffer.append('b');
      break;
    case 12:
      buffer.append('c');
      break;
    case 13:
      buffer.append('d');
      break;
    case 14:
      buffer.append('e');
      break;
    case 15:
      buffer.append('f');
      break;
    }
  }

  void handleGenHexStringParm(PreparedStatement cstmt1, String thisParm,
      int parm, PrintStream out1) throws SQLException {
    StringBuffer stuff = null;
    byte[] bytes = null;
    try {
      String lengthString = thisParm.substring(15);
      int length = Integer.parseInt(lengthString);
      stuff = new StringBuffer(2 * length);
      bytes = new byte[length];
      for (int i = 0; i < length; i++) {
        bytes[i] = (byte) (i & 0xFF);
        appendDigit(stuff, (i & 0xF0) >> 4);
        appendDigit(stuff, i & 0x0F);
      }

    } catch (Exception e) {
      out1.println("Processing of " + thisParm + " failed");
      e.printStackTrace(out1);
      throw new SQLException("Unable to set HexString parameter");
    }

    cstmt1.setString(parm, stuff.toString());

    CRC32 checksum = new CRC32();
    checksum.update(bytes);
    out1.println("GEN_BYTE_ARRAY generated array of size = " + bytes.length
        + " with checksum of " + checksum.getValue());

    SQLWarning warning = cstmt1.getWarnings();
    if (warning != null) {
      if (!silent) {
        dispWarning(out1, warning, hideWarnings_, html_);
      }
    }

  }

  void handleFileBlobParm(PreparedStatement cstmt, String thisParm, int parm,
      PrintStream out) throws SQLException {
    java.sql.Blob blob = (java.sql.Blob) getParameterObject(thisParm, out);
    cstmt.setBlob(parm, blob);

  }

  void handleFileClobParm(PreparedStatement cstmt, String thisParm, int parm,
      PrintStream out) throws SQLException {
    java.sql.Clob clob = (java.sql.Clob) getParameterObject(thisParm, out);
    cstmt.setClob(parm, clob);
  }

  public Array makeArray(Object parameter, String arrayType) throws Exception {
    Object[] objectArray = new Object[0];
    Class argTypes[] = new Class[2];
    argTypes[0] = "".getClass();
    argTypes[1] = objectArray.getClass();
    Array arrayParameter = (Array) ReflectionUtil.callMethod_O(connection_,
        "createArrayOf", argTypes, arrayType, parameter);
    return arrayParameter;
  }


  void handleSqlarrayParm(PreparedStatement cstmt, String thisParm, int parm,
      PrintStream out) throws SQLException {
    try {
      // Format SQLARRAY[TYPE:e1:e2:...]
      // Strip off the SQLARRAY[
      String left = thisParm.substring(9).trim();
      int colonIndex = left.indexOf(":");
      boolean emptyArray = false;
      if (colonIndex == -1) {
        colonIndex = left.indexOf("]");
        if (colonIndex > 0) {
          emptyArray = true;
        }
      }
      if (colonIndex > 0) {
        String typename = left.substring(0, colonIndex);
        if (emptyArray) {
          left = left.substring(colonIndex);
        } else {
          left = left.substring(colonIndex + 1);
        }
        // Put the string parameters into a vecto
        Vector parameterVector = new Vector();
        String arraySep = ":";
        if (typename.equals("Time")) {
          arraySep = " ";
        }
        if (typename.equals("Timestamp")) {
          arraySep = "|";
        }

        colonIndex = left.indexOf(arraySep);
        while (colonIndex >= 0) {
          String piece = left.substring(0, colonIndex);
          parameterVector.addElement(piece);
          left = left.substring(colonIndex + 1);
          colonIndex = left.indexOf(arraySep);
        }

        int braceIndex = left.indexOf("]");
        if (braceIndex >= 0) {
          if (!emptyArray) {
            parameterVector.addElement(left.substring(0, braceIndex));
          }
          int arrayCardinality = parameterVector.size();

          String validTypes = "String:BigDecimal:Date:Time:Timestamp:Blob:Clob:int:short:long:float:double:byteArray";
          if (typename.equals("String")) {
            String[] parameter = new String[arrayCardinality];
            for (int i = 0; i < arrayCardinality; i++) {
              String s = (String) parameterVector.get(i);

              if ("null".equals(s)) {
                parameter[i] = null;
              } else {
                if (s.indexOf("UX'") == 0) {
                  int len = s.length();
                  len = len - 4;
                  char[] stuff = new char[len / 4];
                  for (int j = 0; j < stuff.length; j++) {
                    String piece = s.substring(3 + j * 4, 3 + j * 4 + 4);
                    stuff[j] = (char) Integer.parseInt(piece, 16);
                  }
                  parameter[i] = new String(stuff);
                } else {
                  parameter[i] = s;
                }
              }
            }

            if (toolboxDriver_) {
              cstmt.setArray(parm, makeArray(parameter, "VARCHAR"));
            } else {
              cstmt.setObject(parm, parameter);
            }
          } else if (typename.equals("Clob")) {
            Clob[] parameter = new Clob[arrayCardinality];
            for (int i = 0; i < arrayCardinality; i++) {
              String s = (String) parameterVector.get(i);

              if ("null".equals(s)) {
                parameter[i] = null;
              } else {
                parameter[i] = new ClientClob(s);
              }
            }
            if (toolboxDriver_) {
              cstmt.setArray(parm, makeArray(parameter, "CLOB"));
            } else {
              cstmt.setObject(parm, parameter);
            }

          } else if (typename.equals("BigDecimal")) {
            BigDecimal[] parameter = new BigDecimal[arrayCardinality];
            for (int i = 0; i < arrayCardinality; i++) {
              String s = (String) parameterVector.get(i);
              if ("null".equals(s)) {
                parameter[i] = null;
              } else {
                parameter[i] = new BigDecimal(s);
              }
            }

            if (toolboxDriver_) {
              cstmt.setArray(parm, makeArray(parameter, "DECIMAL"));
            } else {
              cstmt.setObject(parm, parameter);
            }
          } else if (typename.equals("Date")) {
            Date[] parameter = new Date[arrayCardinality];
            for (int i = 0; i < arrayCardinality; i++) {
              String s = (String) parameterVector.get(i);
              if ("null".equals(s)) {
                parameter[i] = null;
              } else {
                parameter[i] = Date.valueOf(s);
              }
            }
            if (toolboxDriver_) {
              cstmt.setArray(parm, makeArray(parameter, "DATE"));
            } else {
              cstmt.setObject(parm, parameter);
            }
          } else if (typename.equals("Time")) {
            Time[] parameter = new Time[arrayCardinality];
            for (int i = 0; i < arrayCardinality; i++) {
              String s = (String) parameterVector.get(i);
              if ("null".equals(s)) {
                parameter[i] = null;
              } else {
                parameter[i] = Time.valueOf(s);
              }
            }
            if (toolboxDriver_) {
              cstmt.setArray(parm, makeArray(parameter, "TIME"));
            } else {
              cstmt.setObject(parm, parameter);
            }
          } else if (typename.equals("Timestamp")) {
            Timestamp[] parameter = new Timestamp[arrayCardinality];
            for (int i = 0; i < arrayCardinality; i++) {
              String s = (String) parameterVector.get(i);
              if ("null".equals(s)) {
                parameter[i] = null;
              } else {
                parameter[i] = Timestamp.valueOf(s);
              }
            }
            if (toolboxDriver_) {
              cstmt.setArray(parm, makeArray(parameter, "TIMESTAMP"));
            } else {
              cstmt.setObject(parm, parameter);
            }
          } else if (typename.equals("Integer")) {
            Integer[] parameter = new Integer[arrayCardinality];
            for (int i = 0; i < arrayCardinality; i++) {
              String s = (String) parameterVector.get(i);
              if ("null".equals(s)) {
                parameter[i] = null;
              } else {
                parameter[i] = new Integer(Integer.parseInt(s));
              }
            }
            if (toolboxDriver_) {
              cstmt.setArray(parm, makeArray(parameter, "INTEGER"));
            } else {
              cstmt.setObject(parm, parameter);
            }
          } else if (typename.equals("int")) {
            int[] parameter = new int[arrayCardinality];
            for (int i = 0; i < arrayCardinality; i++) {
              String s = (String) parameterVector.get(i);
              if ("null".equals(s)) {
                parameter[i] = 0;
              } else {
                parameter[i] = Integer.parseInt(s);
              }
            }
            if (toolboxDriver_) {
              // Toolbox does not handle native types on convert
              Integer[] newParameter = new Integer[arrayCardinality];
              for (int i = 0; i < arrayCardinality; i++) {
                newParameter[i] = new Integer(parameter[i]);
              }
              cstmt.setArray(parm, makeArray(newParameter, "INTEGER"));
            } else {

              cstmt.setObject(parm, parameter);
            }
          } else if (typename.equals("Short")) {
            Short[] parameter = new Short[arrayCardinality];
            for (int i = 0; i < arrayCardinality; i++) {
              String s = (String) parameterVector.get(i);
              if ("null".equals(s)) {
                parameter[i] = null;
              } else {
                parameter[i] = new Short((short) Integer.parseInt(s));
              }
            }
            if (toolboxDriver_) {
              cstmt.setArray(parm, makeArray(parameter, "SMALLINT"));
            } else {

              cstmt.setObject(parm, parameter);
            }
          } else if (typename.equals("short")) {
            short[] parameter = new short[arrayCardinality];
            for (int i = 0; i < arrayCardinality; i++) {
              String s = (String) parameterVector.get(i);
              if ("null".equals(s)) {
                parameter[i] = 0;
              } else {
                parameter[i] = (short) Integer.parseInt(s);
              }
            }
            if (toolboxDriver_) {
              // Toolbox does not handle native types on convert
              Short[] newParameter = new Short[arrayCardinality];
              for (int i = 0; i < arrayCardinality; i++) {
                newParameter[i] = new Short(parameter[i]);
              }

              cstmt.setArray(parm, makeArray(newParameter, "SMALLINT"));
            } else {
              cstmt.setObject(parm, parameter);
            }
          } else if (typename.equals("Long")) {
            Long[] parameter = new Long[arrayCardinality];
            for (int i = 0; i < arrayCardinality; i++) {
              String s = (String) parameterVector.get(i);
              if ("null".equals(s)) {
                parameter[i] = null;
              } else {
                parameter[i] = new Long(Long.parseLong(s));
              }
            }
            if (toolboxDriver_) {
              cstmt.setArray(parm, makeArray(parameter, "BIGINT"));
            } else {
              cstmt.setObject(parm, parameter);
            }
          } else if (typename.equals("long")) {
            long[] parameter = new long[arrayCardinality];
            for (int i = 0; i < arrayCardinality; i++) {
              String s = (String) parameterVector.get(i);
              if ("null".equals(s)) {
                parameter[i] = 0;
              } else {
                parameter[i] = Long.parseLong(s);
              }
            }
            if (toolboxDriver_) {
              // Toolbox does not handle native types on convert
              Long[] newParameter = new Long[arrayCardinality];
              for (int i = 0; i < arrayCardinality; i++) {
                newParameter[i] = new Long(parameter[i]);
              }

              cstmt.setArray(parm, makeArray(newParameter, "BIGINT"));
            } else {
              cstmt.setObject(parm, parameter);
            }
          } else if (typename.equals("Float")) {
            Float[] parameter = new Float[arrayCardinality];
            for (int i = 0; i < arrayCardinality; i++) {
              String s = (String) parameterVector.get(i);
              if ("null".equals(s)) {
                parameter[i] = null;
              } else {
                parameter[i] = new Float((float) Double.parseDouble(s));
              }
            }
            if (toolboxDriver_) {
              cstmt.setArray(parm, makeArray(parameter, "REAL"));
            } else {
              cstmt.setObject(parm, parameter);
            }
          } else if (typename.equals("float")) {
            float[] parameter = new float[arrayCardinality];
            for (int i = 0; i < arrayCardinality; i++) {
              String s = (String) parameterVector.get(i);
              if ("null".equals(s)) {
                parameter[i] = 0;
              } else {
                parameter[i] = (float) Double.parseDouble(s);
              }
            }
            if (toolboxDriver_) {
              // Toolbox does not handle native types on convert
              Float[] newParameter = new Float[arrayCardinality];
              for (int i = 0; i < arrayCardinality; i++) {
                newParameter[i] = new Float(parameter[i]);
              }

              cstmt.setArray(parm, makeArray(newParameter, "REAL"));
            } else {
              cstmt.setObject(parm, parameter);
            }
          } else if (typename.equals("Double")) {
            Double[] parameter = new Double[arrayCardinality];
            for (int i = 0; i < arrayCardinality; i++) {
              String s = (String) parameterVector.get(i);
              if ("null".equals(s)) {
                parameter[i] = null;
              } else {
                parameter[i] = new Double(Double.parseDouble(s));
              }
            }
            if (toolboxDriver_) {
              cstmt.setArray(parm, makeArray(parameter, "DOUBLE"));
            } else {
              cstmt.setObject(parm, parameter);
            }

          } else if (typename.equals("double")) {
            double[] parameter = new double[arrayCardinality];
            for (int i = 0; i < arrayCardinality; i++) {
              String s = (String) parameterVector.get(i);
              if ("null".equals(s)) {
                parameter[i] = 0;
              } else {
                parameter[i] = Double.parseDouble(s);
              }
            }
            if (toolboxDriver_) {
              // Toolbox does not handle native types on convert
              Double[] newParameter = new Double[arrayCardinality];
              for (int i = 0; i < arrayCardinality; i++) {
                newParameter[i] = new Double(parameter[i]);
              }

              cstmt.setArray(parm, makeArray(newParameter, "DOUBLE"));
            } else {
              cstmt.setObject(parm, parameter);
            }
          } else if (typename.equals("byteArray")) {
            byte[][] parameter = new byte[arrayCardinality][];
            for (int i = 0; i < arrayCardinality; i++) {
              String s = (String) parameterVector.get(i);
              if ("null".equals(s)) {
                parameter[i] = null;
              } else {

                byte stuff[] = new byte[s.length() / 2];
                for (int j = 0; j < stuff.length; j++) {
                  String piece = s.substring(j * 2, j * 2 + 2);
                  stuff[j] = (byte) Integer.parseInt(piece, 16);
                }

                parameter[i] = stuff;
              }
            }
            if (toolboxDriver_) {
              cstmt.setArray(parm, makeArray(parameter, "BINARY"));
            } else {
              cstmt.setObject(parm, parameter);
            }

          } else if (typename.equals("Blob")) {
            Blob[] parameter = new Blob[arrayCardinality];
            for (int i = 0; i < arrayCardinality; i++) {
              String s = (String) parameterVector.get(i);
              if ("null".equals(s)) {
                parameter[i] = null;
              } else {

                byte stuff[] = new byte[s.length() / 2];
                for (int j = 0; j < stuff.length; j++) {
                  String piece = s.substring(j * 2, j * 2 + 2);
                  stuff[j] = (byte) Integer.parseInt(piece, 16);
                }

                parameter[i] = new ClientBlob(stuff);
              }
            }
            if (toolboxDriver_) {
              cstmt.setArray(parm, makeArray(parameter, "BLOB"));
            } else {

              cstmt.setObject(parm, parameter);
            }

          } else if (typename.equalsIgnoreCase("null")) {
            cstmt.setObject(parm, null);
          } else {
            throw new Exception("Type [" + typename
                + "] not valid: valid types=" + validTypes);
          }

        } else {
          throw new Exception(
              "Unable to find ending brace for SQLARRAY[TYPE:e1:e2:...]");
        }
      } else {
        throw new Exception("TYPE not found for SQLARRAY[TYPE:e1:e2:...]");
      }
    } catch (Exception e) {
      out.println("Processing of ARRAYPARAMETER '" + thisParm
          + "' failed because of " + e);
      if (e instanceof SQLException) {
        throw (SQLException) e;
      } else {
        e.printStackTrace(out);
      }
    }

  }

  void addVariable(String var, Object value) {
    variables.put(var, value);
    // Set the values for some local objects if they get changed
    if (var.equals("CON")) { connection_ = (Connection) value; }
    else if (var.equals("STMT")) { stmt_ = (Statement) value; }
    else if (var.equals("PSTMT")) { pstmt_ = (PreparedStatement) value; }
    else if (var.equals("CSTMT")) { cstmt_ = (CallableStatement) value; }
    else if (var.equals("RS")) { manualResultSet_ =  (ResultSet) value; }
  }

  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println(usage);
      System.exit(1);
    } else {
      try {

        Main main;
        if (args.length == 1) {
          main = new Main(args[0], null, null);
        } else if (args.length == 2) {
          main = new Main(args[0], args[1], null );
        } else {
          main = new Main(args[0], args[1], args[2]);
        }

        int rc = main.go(System.in, System.out);
        System.exit(rc);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

  }

}
