///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IntegerToLongValueMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.ExtendedIllegalArgumentException;


/**
The IntegerToLongValueMap class maps between a logical unsigned
Long value and a physical Integer value.  
**/
class IntegerToLongValueMap
extends AbstractValueMap
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Private data.
    private static final long UPPER_LIMIT_ = (long)Integer.MAX_VALUE * 2 + 2;



/**
Maps from a physical value to a logical value.

@param physicalValue     The physical value.
@return                 The logical value.
**/
    public Object ptol(Object physicalValue)
    {
        if (physicalValue == null)
            throw new NullPointerException("physicalValue");
        if (!(physicalValue instanceof Integer))
            throw new ExtendedIllegalArgumentException("physicalValue", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        int intValue = ((Integer)physicalValue).intValue();
        if (intValue >= 0)
            return new Long(intValue);
        else
            return new Long( ((long)(UPPER_LIMIT_ + (long)intValue)));
    }



}
