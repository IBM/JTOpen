///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDTypeInfoFieldMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;



class JDTypeInfoFieldMap
implements JDFieldMap
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    private int lengthIndex_;
    private int precisionIndex_;
    private int scaleIndex_;
    private int typeIndex_;
    private JDProperties properties_;


    JDTypeInfoFieldMap (int typeIndex, int lengthIndex, int precisionIndex, 
                    int scaleIndex, JDProperties properties)
    {
        typeIndex_          = typeIndex;
        lengthIndex_        = lengthIndex;
        precisionIndex_     = precisionIndex;
        scaleIndex_         = scaleIndex;
        properties_         = properties;
    }



    static private String getCopyright ()
    {
        return Copyright.copyright;
    }


    public Object getValue (JDRow row)
        throws SQLException
    {
        String typeName = row.getSQLData (typeIndex_).toString ().trim ();
        int length = row.getSQLData (lengthIndex_).toInt ();
        int precision = row.getSQLData (precisionIndex_).toInt ();
        int scale = row.getSQLData (scaleIndex_).toInt ();
        return SQLDataFactory.newData (typeName, length, 
            precision, scale, null, properties_);
    }


    public boolean isNull (JDRow row)
        throws SQLException
    {
        return false;
    }


}
