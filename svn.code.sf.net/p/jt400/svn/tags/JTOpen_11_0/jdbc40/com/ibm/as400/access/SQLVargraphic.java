///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: SQLVargraphic.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

final class SQLVargraphic
extends SQLVarcharBase  implements SQLVariableCompressible
{
    static final String copyright2 = "Copyright (C) 1997-2013 International Business Machines Corporation and others.";

    // Private data.
    private int                     ccsid_; //@cca1

    // Note: maxLength is in bytes not counting 2 for LL.
    //
    SQLVargraphic(int maxLength, SQLConversionSettings settings, int ccsid)  //@cca1
    {
        super(settings, 0, maxLength, "", 2);
        truncated_ = 0; outOfBounds_ = false;
        ccsid_          = ccsid;  //@cca1
    }

    public Object clone()
    {
        return new SQLVargraphic(maxLength_, settings_, ccsid_); //@cca1
    }


    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public int getSQLType()
    {
        return SQLData.VARGRAPHIC;
    }


    public int getDisplaySize()
    {
        if(ccsid_ == 65535)    //@bingra
            return maxLength_; //@bingra
        else
            return maxLength_ / 2;
    }

    public String getLocalName()
    {
        return "VARGRAPHIC";
    }

    public int getMaximumPrecision()
    {
        return 16369;
    }


    public int getNativeType()
    {
        return 464;
    }



    public String getTypeName()
    {
        if( ccsid_ == 13488 || ccsid_ == 1200)  //@cca1
        	return "NVARCHAR";  //@cca1 same as native

    	return "VARGRAPHIC";
    }






}

