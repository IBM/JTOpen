///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDHandleNullFieldMap.java
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
The JDHandleNullFieldMap class returns the value sent from
the system when not null, or a hardcoded value if null.
This will always report the field as non-null then.
**/
class JDHandleNullFieldMap
extends JDSimpleFieldMap
implements JDFieldMap
{
   static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";



    private Object valueIfNull_;



    JDHandleNullFieldMap (int fromIndex, Object valueIfNull)
    {
        super (fromIndex);
        valueIfNull_ = valueIfNull;
    }

    public Object getValue (JDRow row)
        throws SQLException
    {
        if (super.isNull (row))
            return valueIfNull_;
        else
            return super.getValue (row);
    }

    /**
        Indicates if the value was a data mapping error.
    **/
    public boolean isDataMappingError(JDRow row)
    throws SQLException
    {
        return false;
    }

    public boolean isNull (JDRow row)
        throws SQLException
    {
        return false;
    }


}
