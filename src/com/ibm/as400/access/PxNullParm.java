///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PxNullParm.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


   
/**
The PxNullParm class represents a null
parameter in a proxy datastream.
**/
class PxNullParm 
extends PxDS
implements PxParm 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




/**
Constructs a PxNullParm object.
**/
    public PxNullParm ()
    {
        super (ProxyConstants.DS_NULL_PARM);
       
    }



/**
Returns the Object value.

@return The Object value.
**/
    public Object getObjectValue ()
    {
        return null;
    }



}



