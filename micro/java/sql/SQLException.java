///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package java.sql;

/**
 *  An exception that provides information on a database access error or other errors. 
 *  <p>
 *  Each SQLException provides several kinds of information: 
 *  <ul>
 *    <li>a string describing the error. This is used as the Java Exception message, available via the method getMessage. 
 *    <li>a "SQLstate" string, which follows the XOPEN SQLstate conventions. The values of the SQLState string
 *         are described in the XOPEN SQL spec, available via the method getSQLState. 
 *  </ul>
 **/
public class SQLException extends Exception
{
    private String sqlState_;

    
    /**
     *  Constructs a fully-specified SQLException object.
     **/
    public SQLException()
    {
        super();
        sqlState_ = null;
    }
    
    /**
     *  Constructs an SQLException object with a reason; SQLState defaults to null, and vendorCode defaults to 0.
     *
     *  @param reason a description of the exception.
     **/
    public SQLException(String reason)
    {
        super(reason);
        sqlState_ = null;
    }

    /**
     *  Constructs an SQLException object with a reason and SQLState; vendorCode defaults to 0.
     *
     *  @param reason a description of the exception.
     *  @param sqlState an XOPEN code identifying the exception.
     **/
    public SQLException(String reason, String sqlState)
    {
        super(reason);
        sqlState_ = sqlState;
    }

    /**
     *  Retrieves the SQLState for this SQLException object.
     *
     *  @return the SQLState value
     **/
    public String getSQLState()
    {
        return(sqlState_);
    }

    /**
     *  Returns the string representation of this exception.
     *
     *  @return the exception string.
     **/
    public String toString()
    {
        if (sqlState_ != null)
            return "[" + sqlState_ + "]:" + getMessage();

        return getMessage();
    }

}
