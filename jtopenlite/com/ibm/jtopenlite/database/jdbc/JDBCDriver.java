///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  JDBCDriver.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database.jdbc;

import java.sql.*;
import java.util.*;

import com.ibm.jtopenlite.Trace;

public class JDBCDriver implements Driver
{
  public static final String DATABASE_PRODUCT_NAME_ = "DB2 UDB for AS/400";
  public static final String DRIVER_NAME_ =           "jtopenlite JDBC Driver";
  public static final String DRIVER_LEVEL_ =          "01.00";
  public static final int MAJOR_VERSION_ = 1;
  public static final int MINOR_VERSION_ = 0;
  public static final int JDBC_MAJOR_VERSION_ = 3;
  public static final int JDBC_MINOR_VERSION_ = 0;
  public static final String URL_PREFIX_ =  "jdbc:jtopenlite://";

  // public static boolean globalDebugOn = false;

static
  {
	// String debugProperty = System.getProperty("com.ibm.jtopenlite.Trace.category");
	//if (debugProperty != null) {
	//	debugProperty = debugProperty.toLowerCase();
	//	if (debugProperty.equals("jdbc") || debugProperty.equals("all") || debugProperty.equals("true")) {
	//		globalDebugOn = true;
	//	}
	//}
    try
    {
      DriverManager.registerDriver(new JDBCDriver());
    }
    catch (SQLException sql)
    {
      RuntimeException re = new RuntimeException("Error registering driver: "+sql.toString());
      re.initCause(sql);
      throw re;
    }
  }

  public JDBCDriver()
  {
  }

  public boolean acceptsURL(String url) throws SQLException
  {
      if (url == null) return false;
      return url.startsWith(URL_PREFIX_);
  }

  public Connection connect(String url, Properties info) throws SQLException
  {
    if (acceptsURL(url))
    {
      String system = url.substring(18);
      int semi = system.indexOf(";");
      if (semi >= 0)
      {
	if (info == null) info = new Properties();
        addURLProperties(system.substring(semi+1), info);
      }
      int slash = system.indexOf("/");
      if (semi >= 0 || slash >= 0)
      {
        int min = semi >= 0 && slash >= 0 ? (semi < slash ? semi : slash) : (semi >= 0 ? semi : slash);
        system = system.substring(0, min);
      }
      system = system.trim();
      String user = info.getProperty("user");
      String password = info.getProperty("password");
      boolean debugOn = info.getProperty("debug", "false").equals("true");
      if (Trace.isStreamTracingEnabled()) {
    	  debugOn=true;
      }
      return JDBCConnection.getConnection(system, user, password, debugOn);
    }
    return null;
  }

  private static final void addURLProperties(String url, Properties info)
  {

    StringTokenizer st = new StringTokenizer(url, ";");
    while (st.hasMoreTokens())
    {
      String tok = st.nextToken();
      int eq = tok.indexOf("=");
      if (eq >= 0)
      {
        info.setProperty(tok.substring(0,eq), tok.substring(eq+1));
      }
    }
  }

  public int getMajorVersion()
  {
    return 1;
  }

  public int getMinorVersion()
  {
    return 0;
  }

  public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
  {
    if (info == null) info = new Properties();
    return new DriverPropertyInfo[]
    {
      new DriverPropertyInfo("debug", info.getProperty("debug", "false"))
    };
  }

  public boolean jdbcCompliant()
  {
    return false;
  }

  public String   toString() {
	  return DRIVER_NAME_;
  }
}
