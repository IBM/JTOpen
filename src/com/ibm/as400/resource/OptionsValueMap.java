///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: OptionsValueMap.java
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
import java.util.Vector;



/**
The QualifiedValueMap class maps between a logical value
which is a String array and a physical value, which is a
single String in which each character indicates either true or false.
**/
class OptionsValueMap
implements ValueMap, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Private data.
    private char            falsePhysicalValue_;
    private char            truePhysicalValue_;
    private String[]        possibleLogicalValues_;
    


/**
Constructs an OptionsValueMap object.

@param falsePhysicalValue       The physical value character indicating false.
@param truePhysicalValue        The physical value character indicating true.
@param possibleLogicalValues    The possible logical values.
**/
    public OptionsValueMap(char falsePhysicalValue,
                           char truePhysicalValue, 
                           String[] possibleLogicalValues)
    {
        falsePhysicalValue_         = falsePhysicalValue;
        truePhysicalValue_          = truePhysicalValue;
        possibleLogicalValues_      = possibleLogicalValues;
    }


        
/**
Maps from a logical value to a physical value.

@param logicalValue     The logical value.
@param system           The system.
@return                 The physical value.
**/
    public Object ltop(Object logicalValue, AS400 system)
    {
        // Validate the logical value.
        if (logicalValue == null)
            throw new NullPointerException("logicalValue");
        if (!(logicalValue instanceof String[]))
            throw new ExtendedIllegalArgumentException("logicalValue", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        
        // For each possible logical value, check to see if its specified,
        // and build up the physical value.
        String[] asArray = (String[])logicalValue;
        StringBuffer buffer = new StringBuffer(possibleLogicalValues_.length);
        for (int i = 0; i < possibleLogicalValues_.length; ++i) {
            boolean found = false;
            for(int j = 0; (j < asArray.length) && (!found); ++j) {
                if (possibleLogicalValues_[i].equals(asArray[j])) {
                    found = true;
                    break;
                }
            }
            buffer.append(found ? truePhysicalValue_ : falsePhysicalValue_);
        }

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

        // For each character in the physical value, check
        // to see if it indicates true, and if so, add
        // the corresponding option.
        String physicalValueS = (String)physicalValue;
        int length = Math.max(physicalValueS.length(), possibleLogicalValues_.length);
        Vector logicalValuesV = new Vector(length);
        for (int i = 0; i < length; ++i)
            if (physicalValueS.charAt(i) == truePhysicalValue_)
                logicalValuesV.addElement(possibleLogicalValues_[i]);

        // Create the array for the logical values.
        String[] logicalValues = new String[logicalValuesV.size()];
        logicalValuesV.copyInto(logicalValues);
        return logicalValues;
    }


}
