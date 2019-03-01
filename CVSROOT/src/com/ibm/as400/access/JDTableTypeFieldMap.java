///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDTableTypeFieldMap.java
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
   The JDTableTypeFieldMap class converts
   the table type values received from the
   system to the values required by JDBC.
**/
class JDTableTypeFieldMap
extends JDSimpleFieldMap
implements JDFieldMap
{
    static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    static final String TABLE_TYPE_SYSTEM_TABLE  = "SYSTEM TABLE";
    static final String TABLE_TYPE_TABLE         = "TABLE";
    static final String TABLE_TYPE_VIEW          = "VIEW";
    static final String TABLE_TYPE_MATERIALIZED_QUERY_TABLE = "MATERIALIZED QUERY TABLE";       //@K1A
    static final String TABLE_TYPE_ALIAS = "ALIAS";    //@K3A


    JDTableTypeFieldMap (int fromIndex)
    {
        super (fromIndex);
    }

    /**
       Returns the table type in JDBC format.
    **/
    public Object getValue (JDRow row)
    throws SQLException
    {
        Object serverData = super.getValue (row);
        String serverDataAsString = serverData.toString ();

        if(serverDataAsString.length () > 0)
        {
            switch(serverData.toString ().charAt (0))
            {
                case 'T':
                default:
                    return TABLE_TYPE_TABLE;
                case 'V':
                    return TABLE_TYPE_VIEW;
                case 'S':
                    return TABLE_TYPE_SYSTEM_TABLE;
                case 'M':                                           //@K1A
                    return TABLE_TYPE_MATERIALIZED_QUERY_TABLE;     //@K1A
                case 'A':                                           //@K3A
                    return TABLE_TYPE_ALIAS;                        //@K3A

            }
        }
        else
            return TABLE_TYPE_TABLE;
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
       Indicates if the value is null.
    **/
    public boolean isNull (JDRow row)
    throws SQLException
    {
        return false;
    }


}
