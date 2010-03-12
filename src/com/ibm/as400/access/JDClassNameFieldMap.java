///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDClassNameFieldMap.java
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
The JDClassNameFieldMap class converts an 8 character
description of the data type name to the Java class name.
**/
class JDClassNameFieldMap
extends JDSimpleFieldMap
implements JDFieldMap
{
    static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // Private data.
    private SQLConversionSettings   settings_;                                  // @A1A
    private JDProperties            properties_; // @M0A - added JDProperties so we can get the decimal scale & precision
    private int                     vrm_;        // @M0A - added vrm so we can pass it to the newData method


    JDClassNameFieldMap (int fromIndex, SQLConversionSettings settings, int vrm, JDProperties properties)         // @A1C // @M0C
    {
        super (fromIndex);
        settings_ = settings;                                                   // @A1A
        properties_ = properties;  // @M0A
        vrm_ = vrm;                // @M0A
    }

    public Object getValue (JDRow row)
    throws SQLException
    {
        String sourceType = super.getValue (row).toString ();
        if(sourceType != null)
            return SQLDataFactory.newData(sourceType, 2, 1, 1, 0, settings_, vrm_, properties_).getObject ().getClass ().getName (); // @A1C  // @M0C - pass more parms //@KKB pass 0 for ccsid
        else
            return "";
    }

    /**
        Indicates if the value was a data mapping error.
    **/
    public boolean isDataMappingError(JDRow row)
    throws SQLException
    {
        return false;
    }

    public boolean isNull (JDRow row)
    throws SQLException
    {
        return false;
    }
}
