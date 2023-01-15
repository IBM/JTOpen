///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLLongVarchar.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


final class SQLLongVarchar
extends SQLVarcharBase  implements SQLVariableCompressible
{
    static final String copyright = "Copyright (C) 1997-2013 International Business Machines Corporation and others.";


    // Note: maxLength is in bytes not counting 2 for LL.
    //
    SQLLongVarchar(int maxLength, SQLConversionSettings settings)
    {
        super(settings, 0, maxLength, ""); 
    }

    public Object clone()
    {
        return new SQLLongVarchar(maxLength_, settings_); //@pdc
    }



    

    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public int getSQLType()
    {
        return SQLData.LONG_VARCHAR;
    }


    public int getNativeType()
    {
        return 456;
    }

    public int getPrecision()
    {
        return maxLength_;
    }






}

