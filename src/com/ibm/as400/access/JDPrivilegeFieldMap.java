///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDPrivilegeFieldMap.java
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
   The JDPrivilegeFieldMap converts the authority
   information returned from the server to the string
   value required by JDBC.
   The server returns a char(2) which
   contains the following information:
<UL>
<LI> AUTHORITY   CHAR(2)
<LI> BIT 0    -  OBJECT EXISTENCE
<LI> BIT 1    -  OBJECT MANAGEMENT
<LI> BIT 2    -  POINTER AUTHORITY
<LI> BIT 3    -  SPACE AUTHORITY
<LI> BIT 4    -  READ
<LI> BIT 5    -  ADD
<LI> BIT 6    -  DELETE
<LI> BIT 7    -  UPDATE
<LI> BIT 8    -  OWNERSHIP
<LI> BIT 9    -  EXCLUDE
<LI> BIT 10   -  AUTHORIZATION LIST MGT
<LI> BIT 11   -  EXECUTE
<LI> BIT 12   -  OBJECT ALTER
<LI> BIT 13   -  OBJECT REFERENCE
<LI> BIT 14-15-  RESERVED
</UL>

  The only values returned are: "READ", "ADD",
   "DELETE", or "UPDATE".  More than one value may be
    returned.
**/
class JDPrivilegeFieldMap
extends JDSimpleFieldMap
implements JDFieldMap
{
    private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    JDPrivilegeFieldMap (int fromIndex)
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
       Returns the privileges in JDBC format.
    **/
    public Object getValue (JDRow row)
    throws SQLException
    {
        Object serverData = super.getValue (row);    // gets it from correct column
        // using fromIndex

        StringBuffer privileges = new StringBuffer ("");

        byte[] privilegeBytes = (byte[]) serverData;

        // Send back the appropriate String for authority
        // we now have 2 bytes
        // I only care about bytes 4-7 of byte 1
        if((privilegeBytes[0] & 0x08) != 0)    // Read authority
            privileges.append ("READ ");
        if((privilegeBytes[0] & 0x04) != 0)    // Add authority
            privileges.append ("ADD ");
        if((privilegeBytes[0] & 0x02) != 0)    // Delete authority
            privileges.append ("DELETE ");
        if((privilegeBytes[0] & 0x01) != 0)    // Update authority
            privileges.append ("UPDATE ");

        return privileges.toString();
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
        Indicates if value is null.
    **/
    public boolean isNull (JDRow row)
    throws SQLException
    {
        return false;
    }


}
