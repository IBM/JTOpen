///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceMetaData.java
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
import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;



/**
The ResourceMetaData class represents information about a
{@link com.ibm.as400.resource.Resource Resource}
or {@link com.ibm.as400.resource.ResourceList ResourceList}.
**/
//
// Design notes:
//
// 1. I used the term "type" instead of "class" because Java won't let
//    me call a variable "class" and it won't let me call a method
//    getClass().
//
// 3. The design of this class is meant to leave the semantics of
//    the possible values to the resource implementation.  For example,
//    if there are different possible values depending on the
//    system level or languages installed, the resource is responsible
//    for generating that list on the fly and plugging it into the
//    ResourceMetaData object.
//
// 4. By default, the ResourceMetaData and possible values are always considered
//    to be applicable.  It is possible to define the entire ResourceMetaData or
//    possible values to be applicable only for certain levels.  The notion
//    of level is intentionally vague, and can be used to represent a release,
//    a set of features, etc.
//
public class ResourceMetaData
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Private data.
    private static ResourceLevel    defaultLevel_               = new ResourceLevel();

    private Object          id_;
    private Object          defaultValue_                       = null;
    private ResourceLevel   level_                              = null;
    private boolean         multipleAllowed_                    = false;
    private Hashtable       possibleValues_                     = new Hashtable();
    private Hashtable       possibleValuePresentations_         = new Hashtable();
    private Presentation    presentation_;
    private boolean         readOnly_;
    private Class           type_;
    private boolean         valueLimited_;



/**
Constructs a ResourceMetaData object for a read-only value with no
possible values.

@param id                               The ID.
@param type                             The type of value.
@param presentation                     The presentation information.
**/
    public ResourceMetaData(Object id,
                            Class type,
                            Presentation presentation)
    {
        this(id, type, true, null, null, false, false, presentation, null);
    }



/**
Constructs a ResourceMetaData object.

@param id                               The ID.
@param type                             The type of value.
@param readOnly                         true if the value is read-only,
                                        false if the value is writable.
@param possibleValues                   The possible values, or null if
                                        there are none.  All possible values must be
                                        of the correct type.
@param defaultValue                     The default value, or null if there is no
                                        default.
@param valueLimited                     true if the value is limited to
                                        the possible values, false if other values are
                                        allowed.
@param multipleAllowed                  true if multiple values are allowed.
@param presentation                     The presentation information.
@param possibleValuePresentations       The possible value presentations, or null
                                        if none.
**/
    public ResourceMetaData(Object id,
                            Class type,
                            boolean readOnly,
                            Object[] possibleValues,
                            Object defaultValue,
                            boolean valueLimited,
                            boolean multipleAllowed,
                            Presentation presentation,
                            Presentation[] possibleValuePresentations)
    {
        if (id == null)
            throw new NullPointerException("id");
        if (type == null)
            throw new NullPointerException("type");
        if (presentation == null)
            throw new NullPointerException("presentation");

        id_                 = id;
        defaultValue_       = defaultValue;
        level_              = defaultLevel_;
        type_               = type;
        readOnly_           = readOnly;
        valueLimited_       = valueLimited;
        multipleAllowed_    = multipleAllowed;
        presentation_       = presentation;

        if (possibleValues != null)
            setPossibleValues(possibleValues, null);

        if (defaultValue != null)
            validateValue(defaultValue);

        if (possibleValuePresentations != null) {
            if (possibleValues == null)
                throw new NullPointerException("possibleValues");
            if (possibleValuePresentations.length > possibleValues.length)
                throw new ExtendedIllegalArgumentException("possibleValuePresentations", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

            for(int i = 0; i < possibleValuePresentations.length; ++i)
                possibleValuePresentations_.put(possibleValues[i], possibleValuePresentations[i]);
        }
    }



    private static boolean arrayCompare(byte[] a, byte[] b)
    {
        if (a.length != b.length)
            return false;
        for(int i = 0; i < a.length; ++i) {
            if (a[i] != b[i])
                return false;
        }
        return true;
    }


/**
Indicates if multiple values are allowed.  If this is
true, then values are expressed as arrays.

@return true if multiple values are allowed, false otherwise.
**/
    public boolean areMultipleAllowed()
    {
        return multipleAllowed_;
    }



/**
Returns the ID.

@return The ID.
**/
    public Object getID()
    {
        return id_;
    }



/**
Returns the default value.

@return The default value, or null if there is no default value.
**/
     public Object getDefaultValue()
    {
        return defaultValue_;
    }



/**
Returns the level for which this is supported.

@return The level for which this is supported.
**/
    public ResourceLevel getLevel()
    {
        return level_;
    }



/**
Returns the presentation information.

@return The presentation.
**/
     public Presentation getPresentation()
    {
        return presentation_;
    }



/**
Returns the possible values.  If the value is limited to the
possible values, then the value will always be
one of these values.

@return The possible values.  The array has zero elements if
        there are no possible values.
**/
    public Object[] getPossibleValues()
    {
        Object[] possibleValues = new Object[possibleValues_.size()];
        Enumeration enum = possibleValues_.keys();
        int i = 0;
        while(enum.hasMoreElements())
            possibleValues[i++] = enum.nextElement();
        return possibleValues;
    }



/**
Returns the possible values that are valid for the specified
level.  If this value is limited to the possible values,
then the value will always be one of these values.

@param level    The level.
@return The possible values.  The array has zero elements if
        there are no possible values.
**/
    Object[] getPossibleValues(String level)
    {
        if (level == null)
            throw new NullPointerException("level");

        Vector possibleValuesV = new Vector(possibleValues_.size());
        Enumeration enum = possibleValues_.keys();
        while(enum.hasMoreElements()) {
            Object possibleValue = enum.nextElement();
            ResourceLevel possibleValueLevel = (ResourceLevel)possibleValues_.get(possibleValue);
            if (possibleValueLevel.checkLevel(level))
                possibleValuesV.addElement(possibleValue);
        }

        Object[] possibleValues = new Object[possibleValuesV.size()];
        possibleValuesV.copyInto(possibleValues);
        return possibleValues;
    }



/**
Returns the presentation for a possible value.

@param possibleValue    The possible value.
@return The presentation for the possible value, or null if there
        is no presentation available for the possible value.
**/
    public Presentation getPossibleValuePresentation(Object possibleValue)
    {
        if (possibleValue == null)
            throw new NullPointerException("possibleValue");
        validateValue(possibleValue);

        if (possibleValuePresentations_.containsKey(possibleValue))
            return (Presentation)possibleValuePresentations_.get(possibleValue);
        else
            return null;
    }



/**
Returns the presentations for the possible values.

@return The presentations for the possible values.
        The array has zero elements if
        there are no possible values.
**/
    public Presentation[] getPossibleValuePresentations()
    {
        Object[] possibleValues = getPossibleValues();
        Presentation[] possibleValuePresentations = new Presentation[possibleValues.length];
        for(int i = 0; i < possibleValues.length; ++i)
            possibleValuePresentations[i] = getPossibleValuePresentation(possibleValues[i]);
        return possibleValuePresentations;
    }


/**
Returns the presentations for the possible values that
are valid for the specified level.

@param  level    The level.
@return The presentations for the possible values.
        The array has zero elements if
        there are no possible values.
**/
    Presentation[] getPossibleValuePresentations(String level)
    {
        if (level == null)
            throw new NullPointerException("level");

        Object[] possibleValues = getPossibleValues(level);
        Presentation[] possibleValuePresentations = new Presentation[possibleValues.length];
        for(int i = 0; i < possibleValues.length; ++i)
            possibleValuePresentations[i] = getPossibleValuePresentation(possibleValues[i]);
        return possibleValuePresentations;
    }




/**
Returns the type of value.
**/
    public Class getType()
    {
        return type_;
    }



/**
Indicates if the value is read-only.

@return true if the value is read-only,  false if
        the value is writable.
**/
    public boolean isReadOnly()
    {
        return readOnly_;
    }



/**
Indicates if the value is limited to the possible values.

@param valueLimited         true if the value is limited to
                            the possible values, false if other values
                            are allowed.
**/
    public boolean isValueLimited()
    {
        return valueLimited_;
    }



/**
Sets the level for which this is valid.

@param level        The level for which this is valid, or null if this is
                    valid for all levels.
**/
    public void setLevel(ResourceLevel level)
    {
        if (level == null)
            level_ = defaultLevel_;
        else
            level_ = level;
    }



/**
Sets the possible values for a level.  If this value
is limited, then the value will always be one of these values.

@param possibleValues       The possible values, or an empty
                            array if there are none.  All possible values
                            must be of the correct type.
@param level                The level for which the possible values are valid,
                            or null if the possible values are valid for all levels.
**/
    public void setPossibleValues(Object[] possibleValues, ResourceLevel level)
    {
        if (possibleValues == null)
            throw new NullPointerException("possibleValues");

        Class possibleValuesType = ((multipleAllowed_) && (type_.isArray())) ? type_.getComponentType() : type_;

        for (int i = 0; i < possibleValues.length; ++i) {
            if (! (possibleValuesType.isAssignableFrom(possibleValues[i].getClass())))
                throw new ExtendedIllegalArgumentException("possibleValues[" + i + "]", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        for (int i = 0; i < possibleValues.length; ++i) {
            if (level == null)
                possibleValues_.put(possibleValues[i], defaultLevel_);
            else
                possibleValues_.put(possibleValues[i], level);
        }
    }




/**
Returns the String representation of the ID.

@return The String representation of the ID.
**/
    public String toString()
    {
        return id_.toString();
    }



/**
Validates a value.

@param value            The value.
@param possibleValues   The possible values.
@return                 The index in the possible values to which
                        this value is equal, or -1 if it
                        does not equal any of the possible values.

@exception ExtendedIllegalArgumentException If the value is not the
                                    correct type or the value does
                                    not equal any of the possible values and
                                    the value is limited.
**/
    private int validateValue(Object value, Object[] possibleValues)
    {
        int index = -1;
        for (int i = 0; i < possibleValues.length; ++i) {
            if (value.getClass().equals(byte[].class)) {
                if (arrayCompare((byte[])value, (byte[])possibleValues[i])) {
                    index = i;
                    break;
                }
            }
            else if (value.equals(possibleValues[i])) {
                index = i;
                break;
            }
        }

        if ((valueLimited_) && (index < 0)) {
            throw new ExtendedIllegalArgumentException("value(" + value + ") for id(" + id_ + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        return index;
    }



/**
Validates an value.

@param value   The value.
@return        The value.  If the value should be an array and it was passed as a 
               single value, then this will return a 1-element array.

@exception ExtendedIllegalArgumentException If the value is not the
                                    correct type or the value does
                                    not equal any of the possible values and
                                    the value is limited.
**/
    Object validateValue(Object value)
    {
        if (value == null)
            throw new NullPointerException("value");

        // Validate the type.
        Class valueClass = value.getClass();
        if (multipleAllowed_) {
            if (valueClass.isArray()) {
                if (! (type_.isAssignableFrom(valueClass.getComponentType())))
                    throw new ExtendedIllegalArgumentException("value(" + value + ") type(" + valueClass + ") for id(" + id_ + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
                Object[] asArray = (Object[])value;
                for(int i = 0; i < asArray.length; ++i) {
                    if (validateValue(asArray[i], getPossibleValues()) < 0)
                        return value;
                }
                return value;
            }
            else {
                if (! (type_.isAssignableFrom(valueClass)))
                    throw new ExtendedIllegalArgumentException("value(" + value + ") type(" + valueClass + ") for id(" + id_ + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
                validateValue(value, getPossibleValues());
                // They passed a single value, but the processing code is expecting
                // an array, so create a single element array.
                Object[] asArray = (Object[])Array.newInstance(type_, 1);
                asArray[0] = value;
                return asArray;
            }
        }
        else {
            if (! (type_.isAssignableFrom(valueClass)))
                throw new ExtendedIllegalArgumentException("value(" + value + ") type(" + valueClass + ") for id(" + id_ + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            validateValue(value, getPossibleValues());
            return value;
        }
    }



/**
Validates an value for the specified level.

@param value   The value.
@param level            The level.
@return                 The index in the possible values to which
                        this value is equal, or -1 if it
                        does not equal any of the possible values.

@exception ExtendedIllegalArgumentException If the value is not the
                                    correct type or the value does
                                    not equal any of the possible values and
                                    the value is limited.
**/
    int validateValue(Object value, String level)
    {
        if (value == null)
            throw new NullPointerException("value");
        if (level == null)
            throw new NullPointerException("level");

        if (! (type_.isAssignableFrom(value.getClass())))
            throw new ExtendedIllegalArgumentException("value(" + value + ") type(" + value.getClass() + ") for id(" + id_ + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        return validateValue(value, getPossibleValues(level));
    }



}
