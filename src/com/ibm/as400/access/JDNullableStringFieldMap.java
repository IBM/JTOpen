///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JDNullableStringFieldMap.java
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
   The JDNullableStringFieldMap class converts
   a single character value for null capable
   to the string value required by JDBC.
**/
class JDNullableStringFieldMap
extends JDSimpleFieldMap
implements JDFieldMap
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // fromIndex is the index of the data received from server.
    JDNullableStringFieldMap (int fromIndex)
    {
        super (fromIndex);
    }



/**
   Returns the copyright.
**/
    static private String getCopyright ()
    {
        return Copyright.copyright;
    }


/**
    Returns the nullable indicator in JDBC format.
**/
    public Object getValue (JDRow row)
        throws SQLException
    {
        Object serverData = super.getValue (row);// gets data from correct column
                                                             // using fromIndex

        // if serverData == "Y", return YES
        //               == "N", return NO
        //                  else return empty string indicating we don't know
        String serverDataAsString = serverData.toString ();
        String result;

        if (serverDataAsString.length() > 0)
        {
            switch (serverDataAsString.charAt (0))
            {
            case 'N':
                return "NO";
            case 'Y':
                return "YES";
            default:
                return  "";
            }
        }
        else
            return "";
    }


/**
   Indicates if value is null.
**/
    public boolean isNull (JDRow row)
        throws SQLException
    {
        return false;
    }


}
