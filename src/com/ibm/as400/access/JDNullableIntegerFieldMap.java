///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDNullableIntegerFieldMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;



/**
   The JDNullableIntgerFieldMap class converts
   a character value for null capable
   to the integer value required by JDBC.
**/
class JDNullableIntegerFieldMap
extends JDSimpleFieldMap
implements JDFieldMap
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    // fromIndex is the index of the data received from server.
    JDNullableIntegerFieldMap (int fromIndex)
    {
        super (fromIndex);
    }



/**
   Returns the copyright.
**/
    static private String getCopyright ()
    {
        return Copyright.copyright;
    }



/**
    Returns the nullable indicator in JDBC format.
**/
    public Object getValue (JDRow row)
        throws SQLException
    {
        Object serverData = super.getValue (row);// gets data from correct column
                                                             // using fromIndex

        // if serverData == "Y", return columnNullable
        //               == "N", return columnNoNulls
        //                  else return columnNullableUnknown

        String serverDataAsString = serverData.toString();
        if (serverDataAsString.length () > 0)
        {
            switch (serverDataAsString.charAt (0))
            {
            case 'N':
                return new Integer (DatabaseMetaData.columnNoNulls);
            case 'Y':
                return new Integer (DatabaseMetaData.columnNullable);
            default:
                return new Integer (DatabaseMetaData.columnNullableUnknown);
            }
        }
        else
            return new Integer (DatabaseMetaData.columnNullableUnknown);
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
