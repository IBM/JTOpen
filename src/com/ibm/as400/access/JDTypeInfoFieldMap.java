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
    private int ccsidIndex_;          //@KKB  added ccsid

    JDTypeInfoFieldMap(int typeIndex, int lengthIndex, int precisionIndex, 
                       int scaleIndex, int ccsidIndex, int vrm, JDProperties properties) // @M0C - added vrm and properties //@KKB
    {
        typeIndex_          = typeIndex;
        lengthIndex_        = lengthIndex;
        precisionIndex_     = precisionIndex;
        scaleIndex_         = scaleIndex;
        properties_         = properties; // @M0A
        vrm_                = vrm;        // @M0A
        ccsidIndex_         = ccsidIndex;   //@KKB
    }

    public Object getValue(JDRow row)
    throws SQLException
    {
        String typeName = row.getSQLData(typeIndex_).getString().trim();
        int length = row.getSQLData(lengthIndex_).getInt();
        int precision = row.getSQLData(precisionIndex_).getInt();
        int scale = row.getSQLData(scaleIndex_).getInt();
        int ccsid = 0;                                          //@KKB
        if(ccsidIndex_ != 0)                                    //@KKB
           ccsid = row.getSQLData(ccsidIndex_).getInt();        //@KKB
        return SQLDataFactory.newData(typeName, length, precision, scale, ccsid, null, vrm_, properties_); // @M0C - added vrm and properties   //@KKB
    }

    /**
        Indicates if the value was a data mapping error.
    **/
    public boolean isDataMappingError(JDRow row)
    throws SQLException
    {
        return false;
    }

    public boolean isNull(JDRow row)
    throws SQLException
    {
        return false;
    }
}
