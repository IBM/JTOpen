///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  NotImplementedException.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database.jdbc;

import java.sql.*;

/**
 * Thrown by any JDBC method that is not yet implemented by this driver.
**/
public class NotImplementedException extends SQLException
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1877922429046020802L;

NotImplementedException()
  {
    super("Not implemented");
  }
}
