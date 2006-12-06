///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCNClob.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2006-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.NClob;


/**
 * The AS400JDBCNClob class provides access to character large objects. The data
 * is valid only within the current transaction.
 */

//@PDA jdbc40 new class

public class AS400JDBCNClob extends AS400JDBCClob implements NClob
{
  

    /**
     * Constructs an AS400JDBCNClob object. The data is contained in the String.
     * No further communication with the i5/OS system is necessary.
     * 
     * @param data
     *            The NClob data.
     * @param maxLength 
     *            The max length
     */
    AS400JDBCNClob(String data, int maxLength)
    {
        super(data, maxLength);
    }

    /**
     * Constructs an AS400JDBCNClob object. The data is contained in the char array.
     * No further communication with the i5/OS system is necessary.
     * 
     * @param data
     *            The NClob data.
     */
    AS400JDBCNClob(char[] data)
    {
        super(data);
    }

}
