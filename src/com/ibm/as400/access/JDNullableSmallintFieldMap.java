///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDNullableSmallintFieldMap.java
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
   The JDNullableSmallintFieldMap class converts
   a character (3) value for is nullable
   to the short value required for JDBC.
**/
class JDNullableSmallintFieldMap
extends JDSimpleFieldMap
implements JDFieldMap
{
    private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    // fromIndex is the index of the data received from server.
    JDNullableSmallintFieldMap (int fromIndex)
    {
        super (fromIndex);
    }

    /**
        Returns the nullable indicator in JDBC format.
    **/
    public Object getValue (JDRow row)
    throws SQLException
    {
        Object serverData = super.getValue (row);    // gets data from correct column
        // using fromIndex

        // if serverData == "NO", return procedureNoNulls
        //               == "YES", return procedureNullable
        //                  else return procedureNullableUnknown

        String serverDataAsString = serverData.toString ();
        int result;

        if(serverDataAsString.equalsIgnoreCase("NO"))
            return new Short ((short) DatabaseMetaData.procedureNoNulls);
        else if(serverDataAsString.equalsIgnoreCase("YES"))
            return new Short ((short) DatabaseMetaData.procedureNullable);
        else
            return new Short ((short) DatabaseMetaData.procedureNullableUnknown);
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
