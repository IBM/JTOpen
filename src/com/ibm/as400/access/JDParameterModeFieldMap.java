///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDParameterModeFieldMap.java
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
   The JDParameterModeFieldMap converts a
   5 character value for parameter mode
   to the short value required by JDBC.
**/
class JDParameterModeFieldMap
extends JDSimpleFieldMap
implements JDFieldMap
{
    private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


    // fromIndex is the index of the data received from server.
    JDParameterModeFieldMap (int fromIndex)
    {
        super (fromIndex);
    }

    /**
       Returns the parameter mode in JDBC format.
    **/
    public Object getValue (JDRow row)
    throws SQLException
    {
        Object serverData = super.getValue (row);    // gets data from correct column
        // using fromIndex

        // if serverData == "IN", set to procedureColumnIn
        //               == "OUT", set to procedureColumnOut
        //               == "INOUT", set to procedureColumnInOut
        //                  else set to procedureColumnUnknown

        // We will never need to return procedureColumnResult because
        // DB2/400 does not support return values in the procedure
        // columns catalog method.

        // We will never need to return procedureColumnReturn because
        // DB2/400 does not support return values from stored procedures.

        String serverDataAsString = serverData.toString ();
        int result;

        if(serverDataAsString.equalsIgnoreCase("IN"))
            return new Short ((short) DatabaseMetaData.procedureColumnIn);
        else if(serverDataAsString.equalsIgnoreCase("OUT"))
            return new Short ((short) DatabaseMetaData.procedureColumnOut);
        else if(serverDataAsString.equalsIgnoreCase("INOUT"))
            return new Short ((short) DatabaseMetaData.procedureColumnInOut);
        else
            return new Short ((short) DatabaseMetaData.procedureColumnUnknown);
    }

    /**
        Indicates if the value was a data mapping error.
    **/
    public boolean isDataMappingError(JDRow row)
    throws SQLException
    {
        return false;
    }

    /**
       Indicates if value is null.
    **/
    public boolean isNull (JDRow row)
    throws SQLException
    {
        return false;
    }


}
