///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JDStripQuotesFieldMap.java
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
The JDStripQuotesFieldMap class strips double quotes off the
beginning and end of a string if they appear.
**/
class JDStripQuotesFieldMap
extends JDSimpleFieldMap
implements JDFieldMap
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    JDStripQuotesFieldMap (int fromIndex)
    {
        super (fromIndex);
    }



    static private String getCopyright ()
    {
        return Copyright.copyright;
    }



    public Object getValue (JDRow row)
        throws SQLException
    {
        Object value = super.getValue (row);
        if (value instanceof String) {
            String original = ((String) value).trim ();
            if (original.length () >= 2)
                if (original.charAt (0) == '"') {
                    String stripped  = original.substring (1, original.length() - 1);
                    return stripped;
                }
        }

        return value;
    }



    public boolean isNull (JDRow row)
        throws SQLException
    {
        return super.isNull (row);
    }


}
