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
    private boolean hardcodedDataMappingError_;



    JDHardcodedFieldMap (Object hardcodedValue)
    {
        hardcodedValue_ = hardcodedValue;
        hardcodedNull_  = false;
        hardcodedDataMappingError_ = false;
    }


    // Use this to hard code null
    JDHardcodedFieldMap (Object hardcodedValue, boolean hardcodedNull, boolean hardcodedDataMappingError)
    {
        hardcodedValue_ = hardcodedValue;
        hardcodedNull_  = hardcodedNull;
        hardcodedDataMappingError_ = hardcodedDataMappingError;
    }

    public Object getValue (JDRow row)
        throws SQLException
    {
        return hardcodedValue_;
    }

    /**
        Indicates if the value was a data mapping error.
    **/
    public boolean isDataMappingError(JDRow row)
    throws SQLException
    {
        return hardcodedDataMappingError_;
    }

    public boolean isNull (JDRow row)
        throws SQLException
    {
        return hardcodedNull_;
    }

}
