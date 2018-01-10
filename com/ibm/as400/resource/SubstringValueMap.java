///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SubstringValueMap.java
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
The SubstringValueMap class maps a logical value as a substring
of a physical value.  This only makes sense mapping from 
a physical value to a logical value.
**/
class SubstringValueMap
extends AbstractValueMap
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Private data.
    private int         offset_             = 0;
    private int         length_             = -1;
    private boolean     trim_               = false;



/**
Constructs a SubstringValueMap object.

@param offset   The offset of the logical value within the
                physical value.
@param length   The length of the logical value within the
                physical value.  Specify -1 to indicate
                that the logical value extends to the end
                of the physical value.                
@param trim     true if the logical value should be trimmed,
                false otherwise.                
**/
    public SubstringValueMap(int offset, int length, boolean trim)
    {
        offset_ = offset;
        length_ = length;
        trim_ = trim;
    }


        
/**
Maps from a physical value to a logical value.

@param physicalValue    The physical value.
@return                 The logical value.
**/
    public Object ptol(Object physicalValue)
    {
        // Validate the physical value.
        if (physicalValue == null)
            throw new NullPointerException("physicalValue");
        if (!(physicalValue instanceof String))
            throw new ExtendedIllegalArgumentException("physicalValue", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        String asString = (String)physicalValue;
        int endOffset = offset_ + length_;
        String logicalValue = null;

        // If the offset is greater than the length of the
        // physical value, then just return an empty string.
        if (offset_ > asString.length())
            logicalValue = "";

        // If the length is less than zero, then return the
        // substring to the end of the physical value.
        else if (length_ < 0)
            logicalValue = asString.substring(offset_);

        // If the end offset is greater than the length
        // of the physical value, then go to the end of the
        // physical value.
        else if (endOffset > asString.length())
            logicalValue = asString.substring(offset_);

        // Otherwise, return the appropriate substring.
        else
            logicalValue = asString.substring(offset_, endOffset);

        return trim_ ? logicalValue.trim() : logicalValue;
    }


}
