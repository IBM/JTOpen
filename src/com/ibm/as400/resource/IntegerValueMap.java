///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IntegerValueMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import java.io.Serializable;


/**
The IntegerValueMap class maps between a logical Integer value
and a physical String value.
**/
class IntegerValueMap
implements ValueMap, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



/**
Maps from a logical value to a physical value.

@param logicalValue     The logical value.
@param system           The system.
@return                 The physical value.
**/
    public Object ltop(Object logicalValue, AS400 system)
    {
        if (logicalValue == null)
            throw new NullPointerException("logicalValue");
        if (!(logicalValue instanceof Integer))
            throw new ExtendedIllegalArgumentException("logicalValue", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        return logicalValue.toString();
    }



/**
Maps from a physical value to a logical value.

@param physicalValue    The physical value.
@param system           The system.
@return                 The logical value.
**/
    public Object ptol(Object physicalValue, AS400 system)
    {
        if (physicalValue == null)
            throw new NullPointerException("physicalValue");
        if (!(physicalValue instanceof String))
            throw new ExtendedIllegalArgumentException("physicalValue", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        String asString = (String)physicalValue;
        if (asString.length() == 0)
            return new Integer(0);
        else
            return new Integer(asString.trim());
    }


}
