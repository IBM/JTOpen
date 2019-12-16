///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDCharOctetLengthFieldMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;

class JDCharOctetLengthFieldMap
extends JDTypeInfoFieldMap
implements JDFieldMap
{
    static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    private int typeIndex_;     //@A1A
    int lengthIndex_;   //@A1A
    JDCharOctetLengthFieldMap(int typeIndex, int lengthIndex, int precisionIndex, 
                              int scaleIndex, int ccsidIndex, int vrm, JDProperties properties)    // @M0C - added vrm and properties //@KKB
    {
        super(typeIndex, lengthIndex, precisionIndex, scaleIndex, ccsidIndex, vrm, properties);    // @M0C  //@KKB
        typeIndex_ = typeIndex;       //@A1A
        lengthIndex_ = lengthIndex;   //@A1A
    }

    /**
        Returns the data type in JDBC format.
    **/
    public Object getValue(JDRow row)
    throws SQLException
    {
        String typeName = row.getSQLData(typeIndex_).getString().trim();        //@A1A  retrieve the type name
        if(typeName.equals("DISTINCT"))                                         //@A1A  We do not have a SQLData class for DISTINCTs
            return new Integer(-1);                                             //@A1A
        SQLData data = (SQLData)super.getValue(row);
        if(data.isText())
            return new Integer(data.getPrecision());
        else
            return new Integer(-1);
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
