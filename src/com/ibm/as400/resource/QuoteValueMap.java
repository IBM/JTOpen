///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: QuoteValueMap.java
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
The QuoteValueMap class maps between a logical value (without
single quotes) and a physical value (with single quotes).
It will not quote special values (starting with *) and will
optionally turn empty strings into a special value (e.g. *NONE).
**/
class QuoteValueMap 
implements ValueMap, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Private data.
    private static final char SINGLE_QUOTE_ = '\'';

    private String emptyStringSpecialValue_ = null;



/**
Constructs a QuoteValueMap object.
**/
    public QuoteValueMap()
    {
    }



/**
Constructs a QuoteValueMap object.

@param emptyStringSpecialValue      The special value for empty strings.
**/
    public QuoteValueMap(String emptyStringSpecialValue)
    {
        if (emptyStringSpecialValue == null)
            throw new NullPointerException("emptyStringSpecialValue");

        emptyStringSpecialValue_ = emptyStringSpecialValue;
    }



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
        if (!(logicalValue instanceof String))
            throw new ExtendedIllegalArgumentException("logicalValue", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        String asString = (String)logicalValue;

        // Handle the empty string if needed.
        if ((asString.length() == 0) && (emptyStringSpecialValue_ != null))
            return emptyStringSpecialValue_;

        // Do not quote special values.
        if (asString.length() > 0)
            if (asString.charAt(0) == '*')
                return asString;

        // Otherwise...
        StringBuffer buffer = new StringBuffer();        
        buffer.append(SINGLE_QUOTE_);
        buffer.append(asString);
        buffer.append(SINGLE_QUOTE_);
        return buffer.toString();
    }



/**
Maps from a physical value to a logical value.

@param physicalValue    The physical value.
@param system           The system.
@return                 The logical value.
**/
    public Object ptol(Object physicalValue, AS400 system)
    {
        // Validate the physical value.
        if (physicalValue == null)
            throw new NullPointerException("physicalValue");
        if (!(physicalValue instanceof String))
            throw new ExtendedIllegalArgumentException("physicalValue", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        String asString = (String)physicalValue;

        int length = asString.length();

        if (asString.length() >= 2) {
            if (asString.charAt(0) == SINGLE_QUOTE_) {
                if (asString.charAt(length - 1) == SINGLE_QUOTE_)
                    return asString.substring(1, length-1);
                else
                    return asString.substring(1);
            }
            else {
                if (asString.charAt(length - 1) == SINGLE_QUOTE_)
                    return asString.substring(0, length-1);
            }
        }

        return asString;
    }



}
