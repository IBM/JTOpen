///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ProgramMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400BidiTransform;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;



/**
The ProgramMap class represents a map between logical values such
as {@link com.ibm.as400.resource.Resource Resource}
attribute values and data in a PCML document.  Each logical value is
referred to by a logical ID in the map.  A logical value may map to
multiple pieces of data in a PCML document.

<p>This class is intended as a helper class for implementing subclasses
of {@link com.ibm.as400.resource.Resource Resource}.
**/
public class ProgramMap
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
@param programName      The program name in the PCML definition.
@param dataName         The data name in the PCML definition.
**/
    public void add(Object id, String programName, String dataName)
    {
        // Validate the parameters.
        if (dataName == null)
            throw new NullPointerException("dataName");

        add(id, new ProgramMapEntry(programName, dataName, (int[])null, null, null));
    }



/**
Adds a map entry.

@param id               Identifies the logical value.
@param programName      The program name in the PCML definition.
@param dataName         The data name in the PCML definition.
@param valueMap         The value map, or null if there is none.
**/
    public void add(Object id, String programName, String dataName, ValueMap valueMap)
    {
        // Validate the parameters.
        if (dataName == null)
            throw new NullPointerException("dataName");

        add(id, new ProgramMapEntry(programName, dataName, (int[])null, valueMap, null));
    }



/**
Adds a map entry.

@param id               Identifies the logical value.
@param programName      The program name in the PCML definition.
@param dataName         The data name in the PCML definition.
@param valueMap         The value map, or null if there is none.
@param level            The level where this entry is valid, or null if this
                        entry is always valid.
**/
    public void add(Object id, String programName, String dataName, ValueMap valueMap, ResourceLevel level)
    {
        // Validate the parameters.
        if (dataName == null)
            throw new NullPointerException("dataName");

        add(id, new ProgramMapEntry(programName, dataName, (int[])null, valueMap, level));
    }



/**
Adds a map entry.

@param id               Identifies the logical value.
@param programName      The program name in the PCML definition.
@param dataName         The data name in the PCML definition.
@param indices          The indices in the PCML definition, or null if there are none.
**/
    public void add(Object id, String programName, String dataName, int[] indices)
    {
        // Validate the parameters.
        if (dataName == null)
            throw new NullPointerException("dataName");

        add(id, new ProgramMapEntry(programName, dataName, indices, null, null));
    }



/**
Adds a map entry for array elements.

@param id               Identifies the logical value.
@param programName      The program name in the PCML definition.
@param dataName         The data name in the PCML definition.
@param countName        The data name in the PCML defintion which specifies
                        the size of the array.
@param map         The value map, or null if there is none.
**/
    public void add(Object id, String programName, String dataName, String countName, ValueMap map)
    {
        // Validate the parameters.
        if (dataName == null)
            throw new NullPointerException("dataName");

        add(id, new ProgramMapEntry(programName, dataName, countName, map, null));
    }



/**
Adds a map entry for array elements.

@param id               Identifies the logical value.
@param programName      The program name in the PCML definition.
@param dataName         The data name in the PCML definition.
@param countName        The data name in the PCML defintion which specifies
                        the size of the array.
**/
    public void add(Object id, String programName, String dataName, String countName)
    {
        // Validate the parameters.
        if (dataName == null)
            throw new NullPointerException("dataName");

        add(id, new ProgramMapEntry(programName, dataName, countName, null, null));
    }



/**
Adds a map entry.

@param id               Identifies the logical value.
@param programName      The program name in the PCML definition, or null
                        if it will be filled in later.
@param dataName         The data name in the PCML definition.
@param indices          The indices in the PCML definition, or null if there are none.
@param valueMap         The value map, or null if there is none.
**/
    public void add(Object id, String programName, String dataName, int[] indices, ValueMap valueMap)
    {
        // Validate the parameters.
        if (dataName == null)
            throw new NullPointerException("dataName");

        add(id, new ProgramMapEntry(programName, dataName, indices, valueMap, null));
    }




/**
Adds a map entry.

@param id               Identifies the logical value.
@param entry            The entry.
**/
    private synchronized void add(Object id, ProgramMapEntry entry)
    {
        // Validate the parameters.
        if (id == null)
            throw new NullPointerException("id");
        if (entry == null)
            throw new NullPointerException("entry");

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

        entriesV.addElement(entry);
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
    ProgramMapEntry[] getEntries(Object id)
    {
        // Validate the parameter.
        if (id == null)
            throw new NullPointerException("id");
        if (! table_.containsKey(id))
            throw new ExtendedIllegalArgumentException("id", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        // Take the Vector element from the hashtable and turn it
        // into an array.
        Vector entriesV = (Vector)table_.get(id);
        ProgramMapEntry[] entries = new ProgramMapEntry[entriesV.size()];
        entriesV.copyInto(entries);
        return entries;
    }



/**
Returns the list of IDs in the map.

@return The list of IDs.
**/
    public synchronized Object[] getIDs()
    {
        // If the array needs to be created, do so here.
        // We will keep it around in case its needed again.
        if (ids_ == null) {
            ids_ = new Object[idsV_.size()];
            idsV_.copyInto(ids_);
        }
        return ids_;
    }



/**
Returns the list of IDs in the map which match a specific level.

@return The list of IDs.
**/
    synchronized Object[] getIDs(String level)
    {
        Vector subset = new Vector(idsV_.size());
        Enumeration enum1 = idsV_.elements();
        while(enum1.hasMoreElements()) {
            Object id = enum1.nextElement();
            Vector entries = (Vector)table_.get(id);
            Enumeration enum2 = entries.elements();
            while(enum2.hasMoreElements()) {
                ProgramMapEntry entry = (ProgramMapEntry)enum2.nextElement();
                if (entry.getLevel().checkLevel(level)) {
                    if (!subset.contains(id))
                        subset.addElement(id);
                }
            }
        }

        Object[] ids = new Object[subset.size()];
        subset.copyInto(ids);
        return ids;
    }



/**
Get a set of values from the PCML document and map them to
the appropriate logical values.

@param ids              Identifies the logical values.
@param system           The system.
@param document         The PCML document.
@param programName      The PCML program name, or null if
                        the program name is specified as
                        part of the entry.
@param indices          The indices, or null if not applicable,
                        or if the indices are specified as
                        part of the entry.
**/
    public Object[] getValues(Object[] ids,
                              AS400 system,
                              ProgramCallDocument document,
                              String programName,
                              int[] indices)
    throws PcmlException
    {
        // Validate the parameters.
        if (ids == null)
            throw new NullPointerException("ids");
        if (document == null)
            throw new NullPointerException("document");

        int bidiStringType = AS400BidiTransform.getStringType((char)system.getCcsid()); // @A2A

        // Loop for each specified id...
        Object[] values = new Object[ids.length];
        for(int i = 0; i < ids.length; ++i) {
            if (!table_.containsKey(ids[i]))
                throw new ExtendedIllegalArgumentException("ids[" + i + "]", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

            // Loop for each entry associated with the id...
            Vector entriesV = (Vector)table_.get(ids[i]);
            Enumeration enum = entriesV.elements();
            while(enum.hasMoreElements()) {
                ProgramMapEntry entry = (ProgramMapEntry)enum.nextElement();
                String entryProgramName = entry.getProgramName();

                // If no program name was specified here or in the entry,
                // then use the first entry.
                if ((programName == null) || (entryProgramName == null)) {
                    values[i] = entry.getValue(system, document, programName, indices, bidiStringType); // @A2C
                    break;
                }

                // Otherwise, find the one that matches.
                else if (programName.equals(entry.getProgramName())) {
                    values[i] = entry.getValue(system, document, programName, indices, bidiStringType); // @A2C
                    break;
                }
            }

            // If no value was assigned, it means something is not
            // quite right with the map.
            if (values[i] == null)
                throw new ExtendedIllegalArgumentException("values[" + i + "]", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        return values;
    }



/**
Set a set of values in the PCML document and map them from
the appropriate logical values.

@param ids              Identifies the logical values.
@param values           The logical values.
@param system           The system.
@param document         The PCML document.
@param programName      The PCML program name, or null if
                        the program name is specified as
                        part of the entry.
@param indices          The indices, or null if not applicable,
                        or if the indices are specified as
                        part of the entry.
@param bidiStringTypes   The bidi string types as defined by the CDRA (Character Data 
                        Representataion Architecture). See 
                        {@link com.ibm.as400.access.BidiStringType BidiStringType}
                        for more information and valid values. 
**/
    void setValues(Object[] ids,
                          Object[] values,
                          AS400 system,
                          ProgramCallDocument document,
                          String programName,
                          int[] indices,
                          int[] bidiStringTypes)                        // @A2A
    throws PcmlException
    {
        // Validate the parameters.
        if (ids == null)
            throw new NullPointerException("ids");
        if (values == null)
            throw new NullPointerException("values");
        if (ids.length != values.length)
            throw new ExtendedIllegalArgumentException("values", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        if (ids.length != bidiStringTypes.length)
            throw new ExtendedIllegalArgumentException("bidiStringTypes", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        if (document == null)
            throw new NullPointerException("document");

        // Loop for each specified id...
        for(int i = 0; i < ids.length; ++i) {
            if (table_.containsKey(ids[i])) {

                // Loop for each entry associated with the id...
                Vector entriesV = (Vector)table_.get(ids[i]);
                Enumeration enum = entriesV.elements();
                while(enum.hasMoreElements()) {
                    ProgramMapEntry entry = (ProgramMapEntry)enum.nextElement();
                    String entryProgramName = entry.getProgramName();

                    // If no program name was specified here or in the entry,
                    // then use the first entry.
                    if (programName == null) {
                        entry.setValue(system, document, entryProgramName, indices, values[i], bidiStringTypes[i]); // @A2C
                        break;
                    }
                    else if (entryProgramName == null) {
                        entry.setValue(system, document, programName, indices, values[i], bidiStringTypes[i]); // @A2C
                        break;
                    }

                    // Otherwise, find the one that matches.
                    else if (programName.equals(entry.getProgramName())) {
                        entry.setValue(system, document, programName, indices, values[i], bidiStringTypes[i]); // @A2C
                        break;
                    }
                }
            }
        }
    }



}
