///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCNClobLocator.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2006-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.NClob;

//@PDA jdbc40 new class


public class AS400JDBCNClobLocator extends AS400JDBCClobLocator implements NClob
{
    private static final String copyright = "Copyright (C) 2006-2006 International Business Machines Corporation and others.";

    /**
    Constructs an AS400JDBCNClobLocator object.  The data for the
    CLOB will be retrieved as requested, directly from the
    i5/OS system, using the locator handle.
    
    @param  locator             The locator.
    @param  converter           The text converter.
    @param  savedObject         The input savedOjbect.
    @param  savedScale          The saved scale.
    **/
    AS400JDBCNClobLocator(JDLobLocator locator, ConvTable converter, Object savedObject, int savedScale)
    {
        super(locator, converter, savedObject, savedScale);
    }

}
