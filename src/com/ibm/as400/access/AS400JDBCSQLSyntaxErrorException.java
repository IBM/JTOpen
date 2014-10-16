///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: AS400JDBCSQLSyntaxErrorException.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2014-2015 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.*;

/**
<p>The AS400JDBCSQLSyntaxErrorException class provides additional information
about SQL Syntax Errors thrown by the database.  In particular, the method
getPositionOfSyntaxError returns the position, if available, of where the 
syntax error occurred in the original SQL statement.  The exception also 
includes the original SQL statement that encountered the error. 
**/
public class AS400JDBCSQLSyntaxErrorException
/* ifdef JDBC40
extends SQLSyntaxErrorException
endif */
/* ifndef JDBC40 */ 
extends SQLException   
/* endif */
{
    static final String copyright = "Copyright (C) 2014-2014 International Business Machines Corporation and others.";
    private static final long serialVersionUID = -202038790097280171L;
    
    private static boolean includeLocationAndText = false; 
    static {
      try { 
        String property = System.getProperty("com.ibm.as400.access.AS400JDBCSQLSyntaxErrorException.includeLocationAndText");
        if (property != null) {
          property = property.toLowerCase(); 
          if (!"false".equals(property)) {
            includeLocationAndText=true; 
          }
        }
      } catch (Throwable e) {
        
      }
    }

    private int locationOfSyntaxError_; 
    private String sqlStatementText_; 
    /**
     * Constructs a AS400JDBCSQLSyntaxErrorException object. 
     */
    public AS400JDBCSQLSyntaxErrorException(int locationOfSyntaxError, String sqlStatementText) {
      super(); 
      locationOfSyntaxError_ = locationOfSyntaxError; 
      sqlStatementText_ = sqlStatementText; 
    }
    /**
     * Constructs a AS400JDBCSQLSyntaxErrorException object with a given reason. 
     */
    
    public AS400JDBCSQLSyntaxErrorException(String reason, int locationOfSyntaxError, String sqlStatementText) {
      super(reason); 
      locationOfSyntaxError_ = locationOfSyntaxError; 
      sqlStatementText_ = sqlStatementText; 
    }

    /**
     * Constructs a AS400JDBCSQLSyntaxErrorException object with a given reason and SQLState. 
     */
    
    public AS400JDBCSQLSyntaxErrorException(String reason, String SQLState, int locationOfSyntaxError, String sqlStatementText) {
      super(reason, SQLState); 
      locationOfSyntaxError_ = locationOfSyntaxError; 
      sqlStatementText_ = sqlStatementText; 
    }
    
    /**
     * Constructs a AS400JDBCSQLSyntaxErrorException object with a given reason, SQLState and vendorCode. 
     */
    public AS400JDBCSQLSyntaxErrorException(String reason, String SQLState, int vendorCode, int locationOfSyntaxError, String sqlStatementText) {
      super(reason,SQLState,vendorCode); 
      locationOfSyntaxError_ = locationOfSyntaxError; 
      sqlStatementText_ = sqlStatementText; 
    }
    
    /**
     * Constructs a AS400JDBCSQLSyntaxErrorException object with a given reason, SQLState, vendorCode and cause. 
     */
    
    public AS400JDBCSQLSyntaxErrorException(String reason, String sqlState, int vendorCode, Throwable cause, int locationOfSyntaxError, String sqlStatementText) {
/* ifdef JDBC40    
      super(reason, sqlState, vendorCode, cause);
   endif */ 
/* ifndef JDBC40 */
      super(reason, sqlState, vendorCode);
/* endif */       
      locationOfSyntaxError_ = locationOfSyntaxError; 
      sqlStatementText_ = sqlStatementText; 
      
    }
    
    /**
     * Constructs a AS400JDBCSQLSyntaxErrorException object with a given reason, SQLState and cause. 
     */
    
    public AS400JDBCSQLSyntaxErrorException(String reason, String sqlState, Throwable cause, int locationOfSyntaxError, String sqlStatementText) {
/* ifdef JDBC40    
      super(reason, sqlState, cause); 
   endif */ 
/* ifndef JDBC40 */
      super(reason, sqlState); 
/* endif */       
      locationOfSyntaxError_ = locationOfSyntaxError; 
      sqlStatementText_ = sqlStatementText; 
    }
    
    /**
     * Constructs a AS400JDBCSQLSyntaxErrorException object with a given reason and cause. 
     */
    
    public AS400JDBCSQLSyntaxErrorException(String reason, Throwable cause, int locationOfSyntaxError, String sqlStatementText) {
/* ifdef JDBC40    
      super(reason, cause); 
   endif */ 
/* ifndef JDBC40 */
      super(reason); 
/* endif */       
      locationOfSyntaxError_ = locationOfSyntaxError; 
      sqlStatementText_ = sqlStatementText; 
    }
    
    /**
     * Constructs a AS400JDBCSQLSyntaxErrorException object with a given cause. 
     */
    
    public AS400JDBCSQLSyntaxErrorException(Throwable cause, int locationOfSyntaxError, String sqlStatementText) {
/* ifdef JDBC40    
    super(cause); 
   endif */ 
/* ifndef JDBC40 */
      super(); 
/* endif */       
      locationOfSyntaxError_ = locationOfSyntaxError; 
      sqlStatementText_ = sqlStatementText; 
    }
    
    
    /**
     * returns the location of the syntax error, if available.
     * Returns 0 if the location is not available.    
     */
    public int getLocationOfSyntaxError() { 
      return locationOfSyntaxError_; 
    }
    
    /**
     * returns the sql statement text which encountered the error, if available.
     * returns null if the statement is not available.  
     */
    public String getSqlStatementText() { 
      
      return sqlStatementText_; 
    }

    public String getMessage() { 
      String message = super.getMessage(); 
      if (includeLocationAndText) {
        message += " @"+locationOfSyntaxError_+":"+sqlStatementText_; 
      }
      return message; 
    }
    
    
    
}
