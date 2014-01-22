///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLLongNVarchar.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2006-2014 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.StringReader;
import java.sql.Blob;
import java.sql.Clob;
/* ifdef JDBC40 
import java.sql.NClob;
import java.sql.RowId;
endif */ 
import java.sql.SQLException;
/* ifdef JDBC40 
import java.sql.SQLXML;
endif */ 

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

//@PDA jdbc40 new class

final class SQLLongNVarchar 
extends SQLVarcharBase implements SQLVariableCompressible
{
  
    // Private data.

    // Note: maxLength is in bytes not counting 2 for LL.
    //
    SQLLongNVarchar(int maxLength, SQLConversionSettings settings)
    {
        super(settings,0,maxLength,"");
    }

    public Object clone()
    {
        return new SQLLongNVarchar(maxLength_, settings_);  //@pdc
    }
 



    
    


    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public int getSQLType()
    {
        return SQLData.LONG_NVARCHAR;
    }




    public String getLocalName()
    {
        return "LONGNVARCHAR";      
    }


    public int getNativeType()
    {
        return 456;
    }


    public int getType()
    {
    	/* ifdef JDBC40 
        return java.sql.Types.LONGNVARCHAR;
        endif */ 
    	/* ifndef JDBC40 */ 
    	return java.sql.Types.LONGVARCHAR; 
    	/* endif */ 
    	
    	 
    }

    public String getTypeName()
    {
    	/* ifdef JDBC40 
        return "LONGNVARCHAR";    
        endif */ 
    	/* ifndef JDBC40 */ 
    	return "LONGVARCHAR";
    	/* endif */ 
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSIONS TO JAVA TYPES                               //
    //                                                         //
    //---------------------------------------------------------//












 /* ifdef JDBC40 

    
    public SQLXML getSQLXML() throws SQLException
    {
        //This is written in terms of getString(), since it will
        // handle truncating to the max field size if needed.
        truncated_ = 0; outOfBounds_ = false; 
        return new AS400JDBCSQLXML(getString());     
    }

  endif */ 
 
}

