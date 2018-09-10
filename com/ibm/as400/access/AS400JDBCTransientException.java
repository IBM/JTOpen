///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: AS400JDBCTransientException.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2018 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/* ifdef JDBC40 
import java.sql.SQLTransientException;
endif */

/* ifndef JDBC40 */ 
import java.sql.SQLException;
/* endif */ 

/**
 * The AS400JDBCTransientException is the JTOpen version of
 * java.sql.SQLTransientException. 
 *
 * Is it currently only used for handling EXC_CONNECTION_REESTABLISHED. 
 */

public class AS400JDBCTransientException
/* ifdef JDBC40
extends SQLTransientException
endif */
/* ifndef JDBC40 */ 
extends SQLException
/* endif */ 

{

   static final String copyright = "Copyright (C) 2018 International Business Machines Corporation and others.";

   public  AS400JDBCTransientException(String message, String sqlState, int vendorCode) {
     super(message, sqlState, vendorCode); 
   }
  
  
}
