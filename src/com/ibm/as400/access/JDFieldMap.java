///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JDFieldMap.java
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
The JDFieldMap interface that defines how the client sees
the data for a field. It is generated based on row data that
is returned from the server.
**/
interface JDFieldMap
{



/**
Returns the value for a field.

@param   row        The row from which to map.
@return             The value.

@exception       SQLException   If an error occurs.
**/
    public abstract Object getValue (JDRow row)
        throws SQLException;



/**
Is this field value SQL NULL?

@param   row     The row from which to map.
@return          true or false

@exception       SQLException   If an error occurs.
**/
    public abstract boolean isNull (JDRow row)
        throws SQLException;


}
