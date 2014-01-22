///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: SQLLongVargraphic.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


final class SQLLongVargraphic
extends SQLVarcharBase  implements SQLVariableCompressible
{
    static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // Private data.

    // Note: maxLength is in bytes not counting 2 for LL.
    //
    SQLLongVargraphic(int maxLength, SQLConversionSettings settings)
    {
      super(settings, 0, maxLength, "", 2); 
    }

    public Object clone()
    {
        return new SQLLongVargraphic(maxLength_, settings_);
    }



    //---------------------------------------------------------//
    //                                                         //
    // SET METHODS                                             //
    //                                                         //
    //---------------------------------------------------------//

    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public int getSQLType()
    {
        return SQLData.LONG_VARGRAPHIC;
    }


    public int getDisplaySize()
    {
        return maxLength_ / 2;
    }


    public String getLocalName()
    {
        return "LONG VARGRAPHIC";
    }

    public int getMaximumPrecision()
    {
        return 16369;
    }


    public int getNativeType()
    {
        return 472;
    }

    public int getPrecision()
    {
        // maxLength_ is in bytes;
        return maxLength_ / 2;
    }

    public int getType()
    {
        return java.sql.Types.LONGVARCHAR;
    }

    public String getTypeName()
    {
        return "LONG VARGRAPHIC";
    }







}

