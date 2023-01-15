///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDNonUniqueFieldMap.java
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
   The JDNonUniqueFieldMap class converts a
   non-unique character value
   to the short value required by JDBC.
**/
class JDNonUniqueFieldMap
extends JDSimpleFieldMap
implements JDFieldMap
{
    static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    // fromIndex is the index of the data received from the system.
    // Need to specify the index to the non-unique information
    // returned from the system.
    JDNonUniqueFieldMap (int fromIndex)
    {
        super (fromIndex);
    }

    /**
        Returns the non-unique value in JDBC format.
    **/
    public Object getValue (JDRow row)
    throws SQLException
    {
        Object serverData = super.getValue (row);    // gets data from correct column
        // using fromIndex


        // if serverData == "D", allows duplicate values (set to true)
        //               == "U", must be unique  (set to false)
        if(serverData.toString ().charAt (0) == 'D')
            return new Boolean (true);
        else
            return new Boolean (false);
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
