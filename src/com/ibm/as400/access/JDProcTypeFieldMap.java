///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDProcTypeFieldMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;
import java.sql.DatabaseMetaData;


/**
   The JDProcTypeFieldMap class converts the
   update and delete rule values
   received from the server to the values
   required for JDBC.
**/

//-------------------------------------------------//
//   JDBC supports:
//     procedureResultUnknown
//     procedureNoResult
//     procedureReturnsResult
//
//
//    if 0 from server = procedureNoResult
//    if >0 from server = procedureReturnsResult
//    if any other value =  procedureResultUnknown
//
//-------------------------------------------------//

class JDProcTypeFieldMap
extends JDSimpleFieldMap
implements JDFieldMap
{
    private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    // fromIndex is the index of the data received from server.
    JDProcTypeFieldMap (int fromIndex)
    {
        super (fromIndex);
    }

    /**
       Returns the procedure type in JDBC format.
    **/
    public Object getValue (JDRow row)
    throws SQLException
    {
        Object serverData = super.getValue (row);    // gets data from correct column
        // using fromIndex

        int result;
        int serverDataAsInt = ((Number) serverData).intValue ();

        if(serverDataAsInt == 0)
            return new Short ((short) DatabaseMetaData.procedureNoResult);
        else if(serverDataAsInt >= 0)
            return new Short ((short) DatabaseMetaData.procedureReturnsResult);
        else
            return new Short ((short) DatabaseMetaData.procedureResultUnknown);
    }
}
