///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ArrayValueMap.java
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
The ArrayValueMap class maps between a logical array value
and a physical String list of values.
**/
class ArrayValueMap
extends AbstractValueMap
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;



    // Private data.
    private String noneString_ = null;



/**
Constructs an ArrayValueMap object.
**/
    public ArrayValueMap()
    {
    }



/**
Constructs an ArrayValueMap object.

@param noneString   The text which will be used if the array has 0 elements.
**/
    public ArrayValueMap(String noneString)
    {
        if (noneString == null)
            throw new NullPointerException("noneString");

        noneString_ = noneString;
    }



/**
Maps from a logical value to a physical value.

@param logicalValue     The logical value.
@return                 The physical value.
**/
    public Object ltop(Object logicalValue)
    {
        if (logicalValue == null)
            throw new NullPointerException("logicalValue");
        if (!(logicalValue instanceof Object[]))
            throw new ExtendedIllegalArgumentException("logicalValue", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        Object[] asArray = (Object[])logicalValue;
        StringBuffer buffer = new StringBuffer();

        if ((asArray.length == 0) && (noneString_ != null))
            buffer.append(noneString_);
        else {
            for(int i = 0; i < asArray.length; ++i) {
                if (i > 0)
                    buffer.append(' ');
                if (asArray[i] == null)
                    throw new NullPointerException("logicalValue[" + i + "]");
                buffer.append(asArray[i]);
            }
        }
        return buffer.toString();
    }



}
