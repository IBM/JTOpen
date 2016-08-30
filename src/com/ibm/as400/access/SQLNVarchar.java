///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLNVarchar.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2006-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


//@PDA jdbc40 new class

final class SQLNVarchar
extends SQLVarcharBase implements SQLVariableCompressible
{
 
    // Private data.
    private int                     ccsid_; //@cca1
	
    // Note: maxLength is in bytes not counting 2 for LL.
    //
    SQLNVarchar(int maxLength, SQLConversionSettings settings)
    {
        super(settings, 0, maxLength, "", 2);
        truncated_ = 0; outOfBounds_ = false;
        ccsid_          = 1200;  //@cca1
        
    }

    public Object clone()
    {
        return new SQLNVarchar(maxLength_, settings_);
    }



    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public int getSQLType()
    {
        return SQLData.NVARCHAR;
    }

    public int getDisplaySize()
    {
            return maxLength_ / 2;
    }


    public String getLocalName()
    {
        return "NVARCHAR";
    }

    public int getMaximumPrecision()
    {
        return 16369;
    }

    public int getType()
    {
    	/* ifdef JDBC40 
        return java.sql.Types.NVARCHAR;
        endif */ 
    	/* ifndef JDBC40 */
        return java.sql.Types.VARCHAR;
    	/* endif */ 
    }


    public int getNativeType()
    {
        return 464;
    }

    public String getTypeName()
    {
    	/* ifdef JDBC40 
        return "NVARCHAR";
        endif */ 
    	
    	/* ifndef JDBC40 */
        return "VARCHAR";
    	/* endif */ 
    }


}

