///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDHardcodedFieldMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;

/**
A class that defines how the client sees the data for a
hardcoded field, i.e., a field that always has a constant
value.
**/
class JDHardcodedFieldMap
implements JDFieldMap
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    private Object hardcodedValue_;
    private boolean hardcodedNull_;



    JDHardcodedFieldMap (Object hardcodedValue)
    {
        hardcodedValue_ = hardcodedValue;
        hardcodedNull_  = false;
    }


    // Use this to hard code null
    JDHardcodedFieldMap (Object hardcodedValue, boolean hardcodedNull)
    {
        hardcodedValue_ = hardcodedValue;
        hardcodedNull_  = hardcodedNull;
    }



/**
Copyright.
**/
    static private String getCopyright ()
    {
        return Copyright.copyright;
    }



    public Object getValue (JDRow row)
        throws SQLException
    {
        return hardcodedValue_;
    }



    public boolean isNull (JDRow row)
        throws SQLException
    {
        return hardcodedNull_;
    }

}
