///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceMetaDataTable.java
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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;



/**
The ResourceMetaDataTable class represents a data structure for
building and maintaining a list of
{@link com.ibm.as400.resource.ResourceMetaData ResourceMetaData}
objects.  This is intended for use by subclasses of
{@link com.ibm.as400.resource.Resource Resource} and
{@link com.ibm.as400.resource.ResourceList ResourceList}
implementations.  This information is externalized as an array of
ResourceMetaData objects, but this class makes it easier to build
this array without knowing the number of elements ahead of time.
@deprecated Use packages <tt>com.ibm.as400.access</tt> and <tt>com.ibm.as400.access.list</tt> instead. 
**/
public class ResourceMetaDataTable
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Private data.
    private Hashtable                               metaDataByID_       = new Hashtable();
    private ResourceMetaData[]                      metaData_           = null;

    // The presentation loader can be transient because it is only needed when
    // the table is first being built.  It will never be needed after a containing
    // object is deserialized.
    private transient PresentationLoader            presentationLoader_ = null;
    private transient String                        presentationKey_    = null;



/**
Constructs a ResourceMetaDataTable object.
**/
    public ResourceMetaDataTable()
    {
    }



/**
Constructs a ResourceMetaDataTable object.

@param presentationLoader       The presentation loader.
@param presentationKey          The presentation key.
**/
    public ResourceMetaDataTable(PresentationLoader presentationLoader,
                                 String presentationKey)
    {
        presentationLoader_     = presentationLoader;
        presentationKey_        = presentationKey;
    }



/**
Constructs a ResourceMetaDataTable object.

@param metaData                 The meta data, or null if none.
**/
    public ResourceMetaDataTable(ResourceMetaData[] metaData)
    {
        if (metaData == null)
            metaData_ = new ResourceMetaData[0];
        else
            metaData_ = metaData;

        for(int i = 0; i < metaData_.length; ++i)
            metaDataByID_.put(metaData_[i].getID(), metaData_[i]);
    }




/**
Creates and adds a ResourceMetaData object to the list.

@param id                   The ID.
@param type                 The type of value.
@return                     The ResourceMetaData object.
**/
    public ResourceMetaData add(Object id, Class type)
    {
        return add(id, type, false, null, null, false, false);
    }



/**
Creates and adds a ResourceMetaData object to the list.

@param id                   The ID.
@param type                 The type of value.
@param readOnly             true if the value is read-only,
                            false if the value is writable.
@return                     The ResourceMetaData object.
**/
    public ResourceMetaData add(Object id, Class type, boolean readOnly)
    {
        return add(id, type, readOnly, null, null, false, false);
    }



/**
Creates and adds a ResourceMetaData object to the list.

@param id                   The ID.
@param type                 The type of value.
@param defaultValue         The default value.
@return                     The ResourceMetaData object.
**/
    public ResourceMetaData add(Object id, Class type, Object defaultValue)
    {
        return add(id, type, false, null, defaultValue, false, false);
    }



/**
Creates and adds a ResourceMetaData object to the list.

@param id                   The ID.
@param type                 The type of value.
@param readOnly             true if the value is read-only,
                            false if the value is writable.
@param possibleValues       The possible values, or null if
                            there are none.  All possible values must be
                            of the correct type.
@param defaultValue         The default value, or null if there is no
                            default.
@param valueLimited         true if the value is limited to
                            the possible values, false if other values are
                            allowed.
@return                     The ResourceMetaData object.
**/
    public ResourceMetaData add(Object id, Class type, boolean readOnly,
                    Object[] possibleValues, Object defaultValue, boolean valueLimited)
    {
        return add(id, type, readOnly, possibleValues, defaultValue, valueLimited, false);
    }



/**
Creates and adds a ResourceMetaData object to the list.

@param id                   The ID.
@param type                 The type of value.
@param readOnly             true if the value is read-only,
                            false if the value is writable.
@param possibleValues       The possible values, or null if
                            there are none.  All possible values must be
                            of the correct type.
@param defaultValue         The default value, or null if there is no
                            default.
@param valueLimited         true if the value is limited to
                            the possible values, false if other values are
                            allowed.
@param multipleAllowed      true if multiple values are allowed.
@return                     The ResourceMetaData object.
**/
    public ResourceMetaData add(Object id, Class type, boolean readOnly,
                    Object[] possibleValues, Object defaultValue, boolean valueLimited,
                    boolean multipleAllowed)
    {
        return add(id, type, readOnly, possibleValues, defaultValue, valueLimited, multipleAllowed, null);
    }



/**
Creates and adds a ResourceMetaData object to the list.

@param id                   The ID.
@param type                 The type of value.
@param readOnly             true if the value is read-only,
                            false if the value is writable.
@param possibleValues       The possible values, or null if
                            there are none.  All possible values must be
                            of the correct type.
@param defaultValue         The default value, or null if there is no
                            default.
@param valueLimited         true if the value is limited to
                            the possible values, false if other values are
                            allowed.
@param multipleAllowed      true if multiple values are allowed.
@param possibleValuePresentationKeys   The possible value presentation keys, or null
                            if there are none.
@return                     The ResourceMetaData object.
**/
    public ResourceMetaData add(Object id, Class type, boolean readOnly,
                    Object[] possibleValues, Object defaultValue, boolean valueLimited,
                    boolean multipleAllowed, String[] possibleValuePresentationKeys)
    {
        // Load the presentation.  The key suffix is the id.
        Presentation presentation = null;
        if (presentationLoader_ != null)
            presentation = presentationLoader_.getPresentation(presentationKey_, id.toString());

        // Load the presentations for the possible values, if any.
        // Form the key suffix by removing *, adding _.
        Presentation[] possibleValuePresentations = null;
        if (possibleValues != null) {
            possibleValuePresentations = new Presentation[possibleValues.length];
            for(int i = 0; i < possibleValues.length; ++i) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(id);
                buffer.append('_');

                // Kludge to take into account non-String possible values.
                String asString = null;
                if (possibleValuePresentationKeys == null) {
                    if (possibleValues[i] instanceof byte[])
                        asString = Byte.toString(((byte[])possibleValues[i])[0]);
                    else
                        asString = possibleValues[i].toString();
                }
                else
                    asString = possibleValuePresentationKeys[i];

                int length = asString.length();
                for(int j = 0; j < length; ++j) {
                    char ch = asString.charAt(j);
                    if (ch == ' ')
                        buffer.append('_');
                    else if (ch != '*')
                        buffer.append(ch);
                }

                // Special case.  If the string is just "*", use two underscores.
                // This differentiates it from "".
                String keySuffix = buffer.toString();
                if (length == 1)
                    if (asString.charAt(0) == '*')
                        keySuffix = "__";

                possibleValuePresentations[i] = presentationLoader_.getPresentation(presentationKey_, buffer.toString());
            }
        }

        metaData_ = null;
        ResourceMetaData rmd = new ResourceMetaData(id, type, readOnly, possibleValues, defaultValue, valueLimited, multipleAllowed, presentation, possibleValuePresentations);
        metaDataByID_.put(id, rmd);
        return rmd;
    }



/**
Returns the array of ResourceMetaData objects.  This is the
externalized data structure.

@return             The array of ResourceMetaData objects.
**/
    public ResourceMetaData[] getMetaData()
    {
        synchronized(this) {
            Vector asVector = new Vector(metaDataByID_.size());
            int i = 0;
            Enumeration list = metaDataByID_.elements();
            while(list.hasMoreElements()) {
                ResourceMetaData rmd = (ResourceMetaData)list.nextElement();
                    asVector.addElement(rmd);
            }
            ResourceMetaData[] metaData = new ResourceMetaData[asVector.size()];
            asVector.copyInto(metaData);
            return metaData;
        }
    }



/**
Returns the array of ResourceMetaData objects.  This is the
externalized data structure.

@param  level       The current level.
@return             The array of ResourceMetaData objects.
**/
    public ResourceMetaData[] getMetaData(String level)
    {
        if (level == null)
            throw new NullPointerException("level");

        synchronized(this) {
            Vector asVector = new Vector(metaDataByID_.size());
            int i = 0;
            Enumeration list = metaDataByID_.elements();
            while(list.hasMoreElements()) {
                ResourceMetaData rmd = (ResourceMetaData)list.nextElement();
                if (rmd.getLevel().checkLevel(level)) {
                    asVector.addElement(rmd);
                }
            }
            ResourceMetaData[] metaData = new ResourceMetaData[asVector.size()];
            asVector.copyInto(metaData);
            return metaData;
        }
    }



/**
Returns the meta data for a particular ID.

@param id   The ID.
@return     The meta data.
**/
    public ResourceMetaData getMetaData(Object id)
    {
        return validateID(id);
    }



/**
Validates an ID.

@param id       The ID.
@return         The meta data.
**/
    ResourceMetaData validateID(Object id)
    {
        if (id == null)
            throw new NullPointerException("id(" + id.toString() + ")");
        if (! metaDataByID_.containsKey(id))
            throw new ExtendedIllegalArgumentException("id(" + id.toString() + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        return (ResourceMetaData)metaDataByID_.get(id);
    }



/**
Validates multiple IDs.

@param id       The IDs.
**/
    void validateIDs(Object[] id)
    {
        if (id == null)
            throw new NullPointerException("id(" + id.toString() + ")");
        for(int i = 0; i < id.length; ++i) {
            if (! metaDataByID_.containsKey(id[i]))
                throw new ExtendedIllegalArgumentException("id[" + i + "](" + id.toString() + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
    }



}
