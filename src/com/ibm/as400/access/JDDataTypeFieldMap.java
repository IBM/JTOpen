///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDDataTypeFieldMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;

/**
   The JDDataTypeFieldMap class converts an 8 character
   description of the data type name
   to the short value required by JDBC.
**/
class JDDataTypeFieldMap
extends JDTypeInfoFieldMap
implements JDFieldMap
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    JDDataTypeFieldMap(int typeIndex, int lengthIndex, int precisionIndex, 
                       int scaleIndex, int ccsidIndex, int vrm, JDProperties properties)    // @M0C - added vrm and properties  //@KKB added ccsid
    {
        super(typeIndex, lengthIndex, precisionIndex, scaleIndex, ccsidIndex, vrm, properties);    // @M0C //@KKB
    }

    /**
        Returns the data type in JDBC format.
    **/
    public Object getValue(JDRow row)
    throws SQLException
    {
        return new Short((short)((SQLData)super.getValue(row)).getType());
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
    public boolean isNull(JDRow row)
    throws SQLException
    {
        return false;
    }
}
