///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCSavepoint.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.Savepoint;
import java.sql.SQLException;



// JDBC 3.0

/**
<P>
The AS400JDBCSavepoint class is the Toolbox JDBC driver's
representation of a savepoint.  A savepoint is a point within the current 
transaction that can be referenced from the 
{@link com.ibm.as400.access.AS400JDBCConnection#rollback(java.sql.Savepoint) Connection.rollback(savepoint)}
method. 
When a transaction is rolled back to a savepoint, all changes made 
after the savepoint was created are undone. 
<P>
Savepoints can be either named or unnamed. 
The OS/400 server does not support nnnamed savepoints 
so internally the Toolbox JDBC driver will create 
a name to send to the server.  The
format is T_JDBCINTERNAL_n where 'n' is a counter that is
incremented every time an unnamed savepoint is created.
<P>
Considerations:
<UL>
<LI>Named savepoints must be unique.  A savepoint name cannot be reused until the savepoint is released, committed, or rolled back.
<LI>Savepoints are valid only if autocommit is off.  An exception is thrown is autocommit is enabled.
<LI>Savepoints are not valid across XA connections.  An exception is thrown if the connection is an XA connection.
<LI>Savepoints require the release after OS/400 V5R1 or later.  An exception is thrown if connecting to a V5R1 or earlier version of OS/400.
<LI>If the connection option is set to keep cursors open after a traditional rollback, cursors also remain open after a rollback to a savepoint.
</UL>

<P>
The release after OS/400 V5R1 or later is required to use savepoints.  Savepoint support
is new in modification 5 of the Toolbox JDBC driver.


**/

public class AS400JDBCSavepoint
implements Savepoint
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

                                    
    // Counter for creating unique names.
    private static int counter_ = 1;      

    // So object can be serialized / de-serialized across releases    
    static final long serialVersionUID = 4L;


    // Savepoint ID.  Name is used if user named the savepoint, id is used if the user
    // did not name the save point.
    private String name_ = null;
    private int generatedId_ = 0;
    
    // State of savepoint.
    private int status_;
       static final int ACTIVE = 1;
       static final int CLOSED = 2;



    /**
    Constructs an AS400JDBCSavepoint object.  
    **/
    AS400JDBCSavepoint (String name, int id)
    {                            
       name_ = name;
       generatedId_ = id;                                         
       status_ = ACTIVE;
    }



    // The worker method in AS400JDBCConnection needs a method that gets the name
    // no matter how it is generated.  It cannot use the public getSavepointName()
    // method since that throws an exception if the savepoint is an unnamed savepoint.
    String getName()
    {
       return name_;
    }                               


    // Counter to help create unique name for unnamed savepoints.  Every savepoint
    // on the serve has a name.  We will create a name for unnamed savepoints to
    // send to the server.
    static synchronized int getNextId()
    {
       return counter_++;
    }                              
    
    


    /**
    *  Returns the generated ID for the savepoint that this Savepoint object represents.
    *  @return the numeric ID of this savepoint.
    *  @exception SQLException if this is a named savepoint.
    **/
    public int getSavepointId()
               throws SQLException 
    {                           
       if (generatedId_ == 0)
          JDError.throwSQLException(JDError.EXC_SAVEPOINT_DOES_NOT_EXIST);

       return generatedId_;
    } 


    /**
    *  Returns the name for the savepoint that this Savepoint object represents.
    *  @return the name of this savepoint.
    *  @exception SQLException if this is an un-named savepoint.
    **/
    public String getSavepointName()
               throws SQLException 
    {                           
       if (generatedId_ != 0)
          JDError.throwSQLException(JDError.EXC_SAVEPOINT_DOES_NOT_EXIST);

       return name_;
    } 


    // The worker method in AS400JDBCConnection can use this method to determine
    // if the savepoint is closed via the release or rollback methods.  Savepoints
    // that are no longer valid because of a commit will still show a status of 
    // active. 
    int getStatus()
    {
       return status_;
    }                               


    // Change the status of this savepoint.  Most likely use of this method is to 
    // change the status from active to closed after a release or rollback.
    void setStatus(int status)
    {
       status_ = status;
    }                               
                    
}
