///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: CommandMap.java
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
import java.util.Hashtable;
import java.util.Vector;



/**
The CommandMap class represents a map between logical values (e.g. Resource 
attribute values or ResourceList selection values) and parameter values
in a CL command.  Each logical value is refered to by a logical ID in the map.
A logical value may map to multiple parameters in multiple CL commands.
**/
class CommandMap
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Private data.
    private Hashtable   table_              = new Hashtable();
    private Object[]    ids_                = null;
    private Vector      idsV_               = new Vector();



/**
Adds a map entry.

@param id               Identifies the logical value.
@param commandName      The command name.
@param parameterName    The parameter name.
**/
    public void add(Object id, String commandName, String parameterName)
    {
        add(id, commandName, parameterName, null);
    }



/**
Adds a map entry.

@param id               Identifies the logical value.
@param commandName      The command name.
@param parameterName    The parameter name.
@param valueMap         The value map, or null if there is none.
**/
    public void add(Object id, String commandName, String parameterName, ValueMap valueMap)
    {
        // Validate the parameters.
        if (id == null)
            throw new NullPointerException("id");
        if (commandName == null)
            throw new NullPointerException("commandName");
        if (parameterName == null)
            throw new NullPointerException("parameterName");

        // Add the entry to the table.  The table is a hashtable where the keys
        // are IDs, and the elements are each a Vector with a list of entries.
        // This Vector is necessary to account for IDs that map to multiple
        // entries.
        Vector entriesV;
        if (table_.containsKey(id))
            entriesV = (Vector)table_.get(id);
        else {
            entriesV = new Vector();
            table_.put(id, entriesV);
        }
        
        entriesV.addElement(new CommandMapEntry(commandName, parameterName, valueMap));
        idsV_.addElement(id);

        // Since we changed the list, reset the array to null to force it to be
        // rebuilt the next time it is needed.
        ids_ = null;
    }



/**
Returns the map entries for a particular logical value.

@param id               Identifies the logical value.
@return                 The entries.
**/
    public CommandMapEntry[] getEntries(Object id)
    {        
        // Validate the parameter.
        if (id == null)
            throw new NullPointerException("id");
        if (! table_.containsKey(id))
            throw new ExtendedIllegalArgumentException("id", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        // Take the Vector element from the hashtable and turn it
        // into an array.
        Vector entriesV = (Vector)table_.get(id);
        CommandMapEntry[] entries = new CommandMapEntry[entriesV.size()];
        entriesV.copyInto(entries);
        return entries;
    }



}
