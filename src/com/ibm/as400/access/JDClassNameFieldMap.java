///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JDClassNameFieldMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
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
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private SQLConversionSettings   settings_;                                  // @A1A




    JDClassNameFieldMap (int fromIndex, SQLConversionSettings settings)         // @A1C
    {
        super (fromIndex);
        settings_ = settings;                                                   // @A1A
    }



    static private String getCopyright ()
    {
        return Copyright.copyright;
    }



    public Object getValue (JDRow row)
        throws SQLException
    {
        String sourceType = super.getValue (row).toString ();
        if (sourceType != null)
            return SQLDataFactory.newData (sourceType, 2, 1, 1, settings_).toObject ().getClass ().getName (); // @A1C
        else
            return "";
    }



    public boolean isNull (JDRow row)
        throws SQLException
    {
        return false;
    }


}
