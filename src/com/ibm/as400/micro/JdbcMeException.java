///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JdbcMeException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import java.sql.*;

/**
 * JdbcMeException extends the java.sql.Exception
 * class as provided for a Java 2 Micro-Edition device.
 * Since Java 2 Micro-Edition does not include java.sql,
 * the java.sql package is also part of this driver.
 **/
public class JdbcMeException extends SQLException 
{

    /**
     *  Constructs a fully-specified JdbcMeException object.
     **/
    private JdbcMeException()
    {
        super();
    }

    /**
     *  Constructs an JdbcMeException object with a reason and SQLState.
     *
     *  @param reason a description of the exception.
     *  @param sqlState an XOPEN code identifying the exception.
     **/
    public JdbcMeException(String reason, String sqlState)
    {
        super(reason, sqlState);
    }


    /**
     *  Returns the string representation of this exception.
     *
     *  @return the exception string.
     **/
    public String toString()
    {
        return "JdbcMeException: " + super.toString();
    }
}
