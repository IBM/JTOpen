///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JDSimpleFieldMap.java
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
<p>A class that defines how the client sees the data for a
mapped field, i.e., a field that simply gets copied from
an actual field in the row data, as is.  Note that the
field index may be different than the field that this is
representing.

<p>For example, a field comes as the 3rd column, but we
need to map it to the 5th column.
**/
class JDSimpleFieldMap
implements JDFieldMap
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    private int                 fromIndex_;



    JDSimpleFieldMap (int fromIndex)
    {
        fromIndex_ = fromIndex;
    }



    static private String getCopyright ()
    {
        return Copyright.copyright;
    }



    public Object getValue (JDRow row)
        throws SQLException
    {
        return row.getSQLData (fromIndex_).toObject ();
    }



    public boolean isNull (JDRow row)
        throws SQLException
    {
        return row.isNull (fromIndex_);
    }


}
