///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VIFSConstants.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;



/**
The VIFSConstants interface defines property identifiers
for IFS components.

@see VIFSDirectory
@see VIFSFile
**/
public interface VIFSConstants
{



/**
Property identifier for the attributes.
**/
    public static final Object      ATTRIBUTES_PROPERTY           = "Attributes";

/**
Property identifier for the modified time.
**/
    public static final Object      MODIFIED_PROPERTY             = "Modified";

/**
Property identifier for the size.
**/
    public static final Object      SIZE_PROPERTY                 = "Size";



}
