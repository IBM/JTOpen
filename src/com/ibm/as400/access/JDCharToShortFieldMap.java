///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDCharToShortFieldMap.java
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
The JDCharToShortFieldMap class converts a 2 character field to 
the short value required by JDBC.  It handles the situation where
the server sends back data of type char, but it actually contains 
an integer.  For example, instead of having the expected "00F1" 
for 1, it has "0001".
**/
class JDCharToShortFieldMap
extends JDSimpleFieldMap
implements JDFieldMap
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


    JDCharToShortFieldMap (int fromIndex)
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
        Object serverData = super.getValue (row);

        if (serverData instanceof String) {                                     // @C1A
            // In V4R3 and before:                                              // @C1A
            // The server returns a smallint tagged as a char(2), so we need
            // to convert it ourselves. 
            byte[] asBytes = ((String) serverData).getBytes ();
            short asShort = BinaryConverter.byteArrayToShort (asBytes, 0);
            return new Short (asShort);
        }                                                                       // @C1A
        else {                                                                  // @C1A
            // In V4R4, this started coming back as a Short.                    // @C1A
            return serverData;                                                  // @C1A
        }                                                                       // @C1A
    }


    public boolean isNull (JDRow row)
        throws SQLException
    {
        return false;
    }


}
