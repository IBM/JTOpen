///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDTypeInfoFieldMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;



class JDTypeInfoFieldMap
implements JDFieldMap
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    private int lengthIndex_;
    private int precisionIndex_;
    private int scaleIndex_;
    private int typeIndex_;
    private JDProperties properties_; // @M0A - added JDProperties so we can get the decimal scale & precision
    private int vrm_;                 // @M0A - added vrm so we can pass it to the newData method 

    JDTypeInfoFieldMap(int typeIndex, int lengthIndex, int precisionIndex, 
                       int scaleIndex, int vrm, JDProperties properties) // @M0C - added vrm and properties
    {
        typeIndex_          = typeIndex;
        lengthIndex_        = lengthIndex;
        precisionIndex_     = precisionIndex;
        scaleIndex_         = scaleIndex;
        properties_         = properties; // @M0A
        vrm_                = vrm;        // @M0A
    }

    public Object getValue(JDRow row)
    throws SQLException
    {
        String typeName = row.getSQLData(typeIndex_).getString().trim();
        int length = row.getSQLData(lengthIndex_).getInt();
        int precision = row.getSQLData(precisionIndex_).getInt();
        int scale = row.getSQLData(scaleIndex_).getInt();
        return SQLDataFactory.newData(typeName, length, precision, scale, null, vrm_, properties_); // @M0C - added vrm and properties
    }

    public boolean isNull(JDRow row)
    throws SQLException
    {
        return false;
    }
}
