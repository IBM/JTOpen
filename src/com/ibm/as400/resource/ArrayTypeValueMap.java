///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ArrayTypeValueMap.java
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
import java.lang.reflect.Array;


/**
The ArrayTypeValueMap class maps between a logical array value
and a physical Object[] value.  It essentially just changes the
array's element type.
**/
class ArrayTypeValueMap
extends AbstractValueMap
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Private data.
    private Class elementType_;



/**
Constructs an ArrayTypeValueMap object.

@param elementType  The element type.
**/
    public ArrayTypeValueMap(Class elementType)
    {
        if (elementType == null)
            throw new NullPointerException("elementType");

        elementType_ = elementType;
    }



/**
Maps from a physical value to a logical value.

@param physicalValue     The physical value.
@return                 The logical value.
**/
    public Object ptol(Object physicalValue)
    {
        if (physicalValue == null)
            throw new NullPointerException("physicalValue");
        if (!(physicalValue instanceof Object[]))
            throw new ExtendedIllegalArgumentException("physicalValue", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        Object[] asArray = (Object[])physicalValue;
        Object[] asSpecificArray = (Object[])Array.newInstance(elementType_, asArray.length);
        System.arraycopy(asArray, 0, asSpecificArray, 0, asArray.length);
        return asSpecificArray;
    }



}
