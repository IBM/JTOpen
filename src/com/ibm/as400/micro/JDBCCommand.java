///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDBCCommand.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import java.io.*;
import com.ibm.as400.access.MEConstants;

/**
 The JDBCCommand class represents an SQL statement.  This class allows the user to call any SQL statement.
 <P>The following example demonstrates the use of JDBCCommand:
 <br>
  <pre>
    AS400 system = new AS400("mySystem", "myUserid", "myPwd", "myMEServer");
    try
    {
        // Execute a JDBC statement.
        JDBCResultSet rs = JDBCCommand.execute(system, "select * from qiws.qcustcdt");
        
        // Get the fist row of the result set.
        String[] columns = rs.next();
        
        // While there are more rows, continue to call next() and
        // print out the selected columns.
        while (columns != null)
        {
            System.out.println(columns[2]+" "+columns[3]);
            columns = rs.next();
        }
        
        // Close the result set.
        rs.close();
    }
    catch (Exception e)
    {
        System.out.println("JDBCCommand issued an exception!");
        e.printStackTrace();
    }
    // Done with the system.
    system.disconnect();
 </pre>
**/
public final class JDBCCommand 
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    /**
     *  Construct a JDBCCommand object.  This class has a static method execute() and
     *  does not need a public constructor.
     **/
    private JDBCCommand()
    {
    }


    /**
     *  Execute an SQL statement.
     *
     *  @param system The iSeries system.
     *  @param sqlStatement The SQL statement.
     *
     *  @exception  IOException  If an error occurs while communicating with the system.
     *  @exception  MEException  If an error occurs while processing the ToolboxME request.
     *
     *  @return The JDBC result set.
     **/
    public static JDBCResultSet execute(AS400 system, String sqlStatement) throws IOException, MEException
    {
        synchronized(system)
        {
            system.signon();
            system.toServer_.writeInt(MEConstants.JDBC_COMMAND);
            system.toServer_.writeUTF(sqlStatement);
            system.toServer_.flush();

            int retVal = system.fromServer_.readInt();

            if (retVal == MEConstants.JDBC_COMMAND_SUCCEEDED)
            {
                int transactionID = system.fromServer_.readInt();

                JDBCResultSet rs = new JDBCResultSet(system, transactionID);

                return rs;
            }
            else
            {
                throw new MEException(system.fromServer_.readUTF(), retVal);
            }
        }
    }
}


