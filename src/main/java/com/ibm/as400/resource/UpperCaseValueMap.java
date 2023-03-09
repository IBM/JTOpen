///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: UpperCaseValueMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.ExtendedIllegalArgumentException;
import java.io.Serializable;


/**
The UpperCaseValueMap class maps between a logical String value
and a physical String value.  It ensures that the physical String
value will be all uppercase characters.
**/
class UpperCaseValueMap
extends AbstractValueMap
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



/**
Maps from a logical value to a physical value.

@param logicalValue     The logical value.
@return                 The physical value.
**/
    public Object ltop(Object logicalValue)
    {
        if (logicalValue == null)
            throw new NullPointerException("logicalValue");
        if (!(logicalValue instanceof String))
            throw new ExtendedIllegalArgumentException("logicalValue", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        return ((String)logicalValue).toUpperCase();
    }



}
