///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDUpdateDeleteRuleFieldMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;
import java.sql.DatabaseMetaData;

/**
   The JDUpdateDeleteRuleFieldMap class converts
   the update and delete rule values
   received from the system to the values
   required by JDBC.
**/

//-------------------------------------------------//
//   The system returns the following:
//   0 = cascade
//   1 = No action or restrict
//   2 = set null or set default
//
//   JDBC has 5 possible values:
//     importedKeyNoAction
//     importedKeyCascade
//     importedKeySetNull
//     importedKeySetDefault
//     importedKeyRestrict
//
//   Since the system groups together
//   some of the values, all of the
//   possible JDBC values can not be returned.
//
//   For Update Rule, the only values
//   supported by the system are
//   no action and restrict.  Since
//   the value of 1 is returned for
//   both no action and restrict,
//   the value of importKeyRestrict
//   will always be returned for the
//   update rule.
//
//   For Delete Rule
//   the following will be returned.  It is
//   consistent with the ODBC implementation.
//    if 0 from system = importedKeyCascade
//    if 1 from system = importedKeyRestrict
//    if 2 from system = importedKeySetNull
//
//
//    importedKeyNoAction and importedKeySetDefault
//    will not be returned.
//-------------------------------------------------//

class JDUpdateDeleteRuleFieldMap
extends JDSimpleFieldMap
implements JDFieldMap
{
    static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    // fromIndex is the index of the data received from the system.
    JDUpdateDeleteRuleFieldMap (int fromIndex)
    {
        super (fromIndex);
    }

    /**
        Returns the table type in JDBC format
    **/
    public Object  getValue (JDRow row)
    throws SQLException
    {
        Object serverData = super.getValue (row);    // gets data from correct column
        // using fromIndex

        switch(((Number) serverData).intValue ())
        {
            case 0:
                return new Short ((short) DatabaseMetaData.importedKeyCascade);
            case 1:
                return new Short ((short) DatabaseMetaData.importedKeyRestrict);
            default:
                return new Short ((short) DatabaseMetaData.importedKeySetNull);
        }
    }
}
