///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDPrecisionFieldMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;

class JDPrecisionFieldMap
extends JDTypeInfoFieldMap
implements JDFieldMap
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    private int precisionIndex_;     //@A1A
    private int typeIndex_;          //@A1A

    JDPrecisionFieldMap(int typeIndex, int lengthIndex, int precisionIndex, 
                        int scaleIndex, int ccsidIndex, int vrm, JDProperties properties)             // @M0C - added vrm and properties    //@KKB
    {
        super(typeIndex, lengthIndex, precisionIndex, scaleIndex, ccsidIndex, vrm, properties);   // @M0C   //@KKB
        typeIndex_ = typeIndex;            //@A1A
        precisionIndex_ = precisionIndex;  //@A1A
    }

    /**
        Returns the data type in JDBC format.
    **/
    public Object getValue(JDRow row)
    throws SQLException
    {
        String typeName = row.getSQLData(typeIndex_).getString().trim();    //@A1A
        if(typeName.equals("DISTINCT"))                                     //@A1A
            return new Integer(row.getSQLData(precisionIndex_).getInt());   //@A1A
        return new Integer(((SQLData)super.getValue(row)).getPrecision());
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
