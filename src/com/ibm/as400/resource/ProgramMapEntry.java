///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ProgramMapEntry.java
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
import com.ibm.as400.data.Descriptor;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;
import java.io.Serializable;



/**
The ProgramMapEntry class represents an entry in a ProgramMap.
It contains references to the following items relating to data
in a PCML document:
 
<ul>
<li>program name (optional)
<li>data name 
<li>indices (optional) OR count name (optional)
<li>value map (optional)
<li>level (optional)
</ul>
**/
class ProgramMapEntry
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Private data.
    private static final ResourceLevel  defaultLevel_                   = new ResourceLevel();

    private String                      programName_                    = null;         
    private String                      dataName_                       = null;         
    private int[]                       indices_                        = null;
    private String                      countName_                      = null;
    private ResourceLevel               level_                          = null;
    private ValueMap                    valueMap_                       = null;



/**
Constructs a ProgramMapEntry object.

@param programName  The program name, or null if this should be filled in later.
@param dataName     The data name.
@param indices      The indices, or null if not applicable, or if these should be
                    filled in later.
@param valueMap     The value map, or null if not applicable.
@param level        The level where this meta data is valid, or null if this is
                    always valid.
**/
    public ProgramMapEntry(String programName, 
                           String dataName,
                           int[] indices,
                           ValueMap valueMap,
                           ResourceLevel level)
    {
        if (dataName == null)
            throw new NullPointerException("dataName");

        programName_        = programName;
        dataName_           = dataName;
        indices_            = indices;
        countName_          = null;
        valueMap_           = valueMap;

        if (level == null)
            level_ = defaultLevel_;
        else
            level_ = level;
    }



/**
Constructs a ProgramMapEntry object.

@param programName  The program name, or null if this should be filled in later.
@param dataName     The data name.
@param countName    The data name in the PCML definition which specifies
                    the size of the array, or null if not applicable.
@param valueMap     The value map, or null if not applicable.
@param level        The level where this meta data is valid, or null if this is always valid.
**/
    public ProgramMapEntry(String programName, 
                           String dataName,
                           String countName,
                           ValueMap valueMap,
                           ResourceLevel level)
    {
        if (dataName == null)
            throw new NullPointerException("dataName");

        programName_        = programName;
        dataName_           = dataName;
        indices_            = null;
        countName_          = countName;
        valueMap_           = valueMap;

        if (level == null)
            level_ = defaultLevel_;
        else
            level_ = level;
    }



/**
Returns the program name.

@return The program name, or null if none has been set.
**/
    public String getProgramName()
    {
        return programName_;
    }

    

/**
Returns the level.

@return The level.
**/
    public ResourceLevel getLevel()
    {
        return level_;
    }



// @A2C
/**
Returns the logical value directly from the PCML document.

@param system           The system.                           
@param document         The PCML document.
@param programName      The program name, or null if it was already specified.
@param indices          The indices, or null if not applicable, or if they were
                        already specified.
@param bidiStringType   The bidi string type as defined by the CDRA (Character Data 
                        Representataion Architecture). See 
                        {@link com.ibm.as400.access.BidiStringType BidiStringType}
                        for more information and valid values. 
@return                 The logical value.                    

@throws PcmlException   If an error occurs.
**/
    public Object getValue(AS400 system,
                           ProgramCallDocument document, 
                           String programName, 
                           int[] indices,
                           int bidiStringType)
        throws PcmlException
    {
        Object value;

        // Determine the program name to use. If the program name was specified 
        // here AND in the constructor, then something is not working as intended.  
        if ((programName != null) && (programName_ != null))
            throw new ExtendedIllegalArgumentException("programName", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        StringBuffer buffer = new StringBuffer();
        buffer.append((programName == null) ? programName_ : programName);
        buffer.append('.');
        buffer.append(dataName_);
        String programNameAndDataName = buffer.toString();

        // Determine the indices to use.  If the program name was specified 
        // here AND in the constructor, then something is not working as intended.
        if ((indices != null) && (indices_ != null))
            throw new ExtendedIllegalArgumentException("indices", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        int[] indicesToUse = (indices == null) ? indices_ : indices;

        // If a count name was not specified, then just get the single value from 
        // the PCML document.  Otherwise, get multiple values and pack them into
        // an array.
        if ((indicesToUse == null) && (countName_ == null)) {                                       // @A3C
            if (isStringValue(document, programNameAndDataName))                                    // @A3A
                value = document.getStringValue(programNameAndDataName, bidiStringType);            // @A2C
            else                                                                                    // @A3A
                value = document.getValue(programNameAndDataName);                                  // @A3A
        }                                                                                           // @A3A
        else if (countName_ == null) {                                                              // @A3C
            if (isStringValue(document, programNameAndDataName))                                    // @A3A
                value = document.getStringValue(programNameAndDataName, indicesToUse, bidiStringType);  // @A2C
            else                                                                                    // @A3A
                value = document.getValue(programNameAndDataName, indicesToUse);                    // @A3A
        }                                                                                           // @A3A
      
        else {
            buffer = new StringBuffer();
            buffer.append((programName == null) ? programName_ : programName);
            buffer.append('.');
            buffer.append(countName_);
            String programNameAndCountName = buffer.toString();

            int count = document.getIntValue(programNameAndCountName);
            Object[] tempValue = new Object[count];
            for(int i = 0; i < count; ++i)
                tempValue[i] = document.getValue(programNameAndDataName, new int[] { i });                        
            value = tempValue;
        }

        // Map the physical value to its logical value.
        if (valueMap_ != null)
            value = valueMap_.ptol(value, system);

        return value;
    }



    // @A3A
    private static boolean isStringValue(ProgramCallDocument document, String programNameAndDataName)
    {
        Descriptor descriptor = document.getDescriptor().getDescriptor(programNameAndDataName);
        if (descriptor == null)
            return false;
        String type = descriptor.getAttributeValue("type");
        if (type == null)
            return false;
        return type.equals("string");
    }




// @A2C
/**
Sets the logical value directly in the PCML document.

@param system       The system.                           
@param document     The PCML document.
@param programName  The program name, or null if it was already specified.
@param indices      The indices, or null if not applicable, or if they were
                    already specified.
@param value        The logical value.                    
@param bidiStringType   The bidi string type as defined by the CDRA (Character Data 
                        Representataion Architecture). See 
                        {@link com.ibm.as400.access.BidiStringType BidiStringType}
                        for more information and valid values. 

@throws PcmlException   If an error occurs.
**/
    public void setValue(AS400 system,
                         ProgramCallDocument document, 
                         String programName, 
                         int[] indices, 
                         Object value,
                         int bidiStringType)
        throws PcmlException
    {
        // Determine the program name to use. If the program name was specified 
        // here AND in the constructor, then something is not working as intended.  
        if ((programName != null) && (programName_ != null))
            throw new ExtendedIllegalArgumentException("programName", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        StringBuffer buffer = new StringBuffer();
        buffer.append((programName == null) ? programName_ : programName);
        buffer.append('.');
        buffer.append(dataName_);
        String programNameAndDataName = buffer.toString();

        // Determine the indices to use.  If the program name was specified 
        // here AND in the constructor, then something is not working as intended.
        if ((indices != null) && (indices_ != null))
            throw new ExtendedIllegalArgumentException("indices", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        int[] indicesToUse = (indices == null) ? indices_ : indices;
        
        // Map the logical value to its physical value.
        if (valueMap_ != null)
            value = valueMap_.ltop(value, system);
    
        // Set the value in the PCML document.
        if (indicesToUse == null) {                                                                 // @A3C
            if (value instanceof String)                                                            // @A3A
                document.setStringValue(programNameAndDataName, (String)value, bidiStringType);     // @A2C
            else                                                                                    // @A3A
                document.setValue(programNameAndDataName, value);                                   // @A3A
        }                                                                                           // @A3A
        else {                                                                                      // @A3C
            if (value instanceof String)                                                            // @A3A
                document.setStringValue(programNameAndDataName, indicesToUse, (String)value, bidiStringType); // @A2C
            else                                                                                    // @A3A
                document.setValue(programNameAndDataName, indicesToUse, value);                     // @A3A
        }                                                                                           // @A3A
    }



}
