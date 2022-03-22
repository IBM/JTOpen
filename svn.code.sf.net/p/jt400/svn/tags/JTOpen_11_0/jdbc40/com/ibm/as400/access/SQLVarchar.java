///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLVarchar.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

final class SQLVarchar
extends SQLVarcharBase  
{
    static final String copyright = "Copyright (C) 1997-2013 International Business Machines Corporation and others.";

    SQLVarchar(int maxLength, SQLConversionSettings settings)
    {
        super(settings,0,maxLength, ""); 
    }

    public Object clone()
    {
        return new SQLVarchar(maxLength_, settings_);
    }

    
}

