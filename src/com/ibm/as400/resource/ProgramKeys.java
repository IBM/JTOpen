///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ProgramKeys.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.ExtendedIllegalArgumentException;
import java.util.Hashtable;
import java.util.Vector;




/**
The ProgramKeys class represents a map between logical values (e.g.
Resource attribute values) and keys for a program call.  This is
useful for system APIs which allow values to be set using a key.
Each logical value is referred to by a logical ID in the map.
**/
class ProgramKeys
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




/**
Constant indicating the type for binary data.
**/
    public static final int BINARY          = 0;



/**
Constant indicating the type for character data.
**/
    public static final int CHAR            = 1;



    // Private data.
    private Hashtable map_        = new Hashtable();
    private Hashtable vms_        = new Hashtable();



/**
Adds a key.  The length is 4 for BINARY attributes and
10 for CHAR attributes.

@param id               Identifies the logical value.
@param key              The key.
@param type             The attribute type.  Possible values are:
                        <ul>
                        <li>BINARY
                        <li>CHAR
                        </ul>
**/
    public void add(Object id, int key, int type)
    {
        add(id, key, type, (type == BINARY) ? 4 : 10, null);
    }



/**
Adds a key.

@param id               Identifies the logical value.
@param key              The key.
@param type             The attribute type.  Possible values are:
                        <ul>
                        <li>BINARY
                        <li>CHAR
                        </ul>
@param length           The length.                        
**/
    public void add(Object id, int key, int type, int length)
    {
        add(id, key, type, length, null);
    }



/**
Adds a key.

@param id               Identifies the logical value.
@param key              The key.
@param type             The attribute type.  Possible values are:
                        <ul>
                        <li>BINARY
                        <li>CHAR
                        </ul>
@param length           The length.                        
@param valueMap         The value map, or null if none.
**/
    public void add(Object id, int key, int type, int length, ValueMap valueMap)
    {
        // Validate the parameters.
        if (id == null)
            throw new NullPointerException("id");
        if ((type < BINARY) && (type > CHAR))
            throw new ExtendedIllegalArgumentException("type", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        if (length <= 0)
            throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        // Store the key in the hashtable.  The key is the id,
        // and the element is a 3 int array with one element
        // each for key, type, and length.
        Vector v = null;
        if (map_.containsKey(id))
            v = (Vector)map_.get(id);
        else {
            v = new Vector();
            map_.put(id, v);
        }
        v.addElement(new int[] { key, type, length });

        if (valueMap != null) {
            Vector v2 = null;
            if (vms_.containsKey(id))
                v2 = (Vector)vms_.get(id);
            else {
                v2 = new Vector();
                vms_.put(id, v2);
            }
            v2.addElement(valueMap);
        }
    }



/**
Returns the keys associated with the specified logical value.

@param id           Identifies the logical value.
@return             The keys.
**/
    public int[] getKeys(Object id)
    {
        if (id == null)
            throw new NullPointerException("id");
        if (!map_.containsKey(id))
            throw new ExtendedIllegalArgumentException("id", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        Vector v = (Vector)map_.get(id);        
        int[] keys = new int[v.size()];
        for(int i = 0; i < keys.length; ++i)
            keys[i] = ((int[])v.elementAt(i))[0];
        return keys;
    }



/**
Returns the lengths associated with the specified logical value.

@param id           Identifies the logical value.
@return             The lengths.
**/
    public int[] getLengths(Object id)
    {
        if (id == null)
            throw new NullPointerException("id");
        if (!map_.containsKey(id))
            throw new ExtendedIllegalArgumentException("id", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        Vector v = (Vector)map_.get(id);        
        int[] lengths = new int[v.size()];
        for(int i = 0; i < lengths.length; ++i)
            lengths[i] = ((int[])v.elementAt(i))[2];
        return lengths;
    }



/**
Returns the types associated with the specified logical value.

@param id           Identifies the logical value.
@return             The types.
**/
    public int[] getTypes(Object id)
    {
        if (id == null)
            throw new NullPointerException("id");
        if (!map_.containsKey(id))
            throw new ExtendedIllegalArgumentException("id", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        Vector v = (Vector)map_.get(id);        
        int[] types = new int[v.size()];
        for(int i = 0; i < types.length; ++i)
            types[i] = ((int[])v.elementAt(i))[1];
        return types;
    }



/**
Returns the value maps associated with the specified logical value.

@param id           Identifies the logical value.
@return             The value map.
**/
    public ValueMap[] getValueMaps(Object id)
    {
        if (id == null)
            throw new NullPointerException("id");
        if (!map_.containsKey(id))
            throw new ExtendedIllegalArgumentException("id", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        if (vms_.containsKey(id)) {
            Vector v = (Vector)vms_.get(id);        
            ValueMap[] valueMaps = new ValueMap[v.size()];
            for(int i = 0; i < valueMaps.length; ++i)
                valueMaps[i] = (ValueMap)v.elementAt(i);
            return valueMaps;
        }
        else 
            return null;
    }



}
