///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: CommandMapEntry.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import java.io.Serializable;


/**
The CommandMapEntry class represents an entry in a CommandMap.
It contains references to the following items relating to a
parameter in a CL command:
 
<ul>
<li>command name
<li>parameter name 
<li>value map (optional)
</ul>
**/
class CommandMapEntry
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;

    // Private data.
    private String                      commandName_                    = null;         
    private String                      parameterName_                  = null;         
    private ValueMap                    valueMap_                       = null;



/**
Constructs a CommandMapEntry object.

@param commandName      The command name.
@param parameterName    The parameter name.
@param valueMap         The value map, or null if not applicable.
**/
    public CommandMapEntry(String commandName, 
                           String parameterName,
                           ValueMap valueMap)
    {
        if (commandName == null)
            throw new NullPointerException("commandName");
        if (parameterName == null)
            throw new NullPointerException("parameterName");

        commandName_        = commandName;
        parameterName_      = parameterName;
        valueMap_           = valueMap;
    }



/**
Returns the command name.

@return The command name.
**/  
    public String getCommandName()
    {
        return commandName_;
    }


    
/**
Returns the parameter name.

@return The parameter name.
**/
    public String getParameterName()
    {
        return parameterName_;
    }



/**
Returns the value map.

@return The value map, or null if none was specified.
**/    
    public ValueMap getValueMap()
    {
        return valueMap_;
    }


}
