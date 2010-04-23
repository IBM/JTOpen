///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: BooleanValueMap.java
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
Maps between a logical Boolean value and specified physical values.
@deprecated Use packages <tt>com.ibm.as400.access</tt> and <tt>com.ibm.as400.access.list</tt> instead. 
**/
public class BooleanValueMap
extends AbstractValueMap
implements Serializable
{
    static final long serialVersionUID = 4L;

    // Private data.
    private Object[]      falseValues_         = null;
    private Object[]      trueValues_          = null;



/**
Constructs a BooleanValueMap object.

@param falseValue   The physical value which represents false.
@param trueValue    The physical value which represents true.
**/
    public BooleanValueMap(Object falseValue, Object trueValue)
    {
        this(new Object[] { falseValue }, new Object[] { trueValue });
    }



/**
Constructs a BooleanValueMap object.

@param falseValues   The physical values which represent false.
@param trueValues    The physical values which represent true.
**/
    public BooleanValueMap(Object[] falseValues, Object[] trueValues)
    {
        if (falseValues == null)
            throw new NullPointerException("falseValues");
        if (falseValues.length == 0)
            throw new ExtendedIllegalArgumentException("falseValues", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        if (trueValues == null)
            throw new NullPointerException("trueValues");
        if (trueValues.length == 0)
            throw new ExtendedIllegalArgumentException("trueValues", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        falseValues_     = falseValues;
        trueValues_      = trueValues;
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
        if (!(logicalValue instanceof Boolean))
            throw new ExtendedIllegalArgumentException("logicalValue([class "+logicalValue.getClass().getName()+"]: "+logicalValue.toString() + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        return (((Boolean)logicalValue).booleanValue()) ? trueValues_[0] : falseValues_[0];
    }



/**
Maps from a physical value to a logical value.

@param physicalValue    The physical value.
@return                 The logical value.
**/
    public Object ptol(Object physicalValue)
    {
        if (physicalValue == null)
            throw new NullPointerException("physicalValue");

        for(int i = 0; i < falseValues_.length; ++i)
            if (physicalValue.equals(falseValues_[i]))
                return Boolean.FALSE;

        for(int i = 0; i < trueValues_.length; ++i)
            if (physicalValue.equals(trueValues_[i]))
                return Boolean.TRUE;
            
        throw new ExtendedIllegalArgumentException("physicalValue([class "+physicalValue.getClass().getName()+"]: "+physicalValue.toString() + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }


}
