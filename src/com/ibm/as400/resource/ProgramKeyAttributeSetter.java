///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ProgramKeyAttributeSetter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.BinaryConverter;
import com.ibm.as400.access.CharConverter;
import com.ibm.as400.access.Trace;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;



/**
The ProgramKeyAttributeSetter class sets attribute values by calling an
AS/400 program.  The program call is specified using Program Call
Markup Language (PCML).  

<p>This class uses a {@link com.ibm.as400.resource.ProgramKeys ProgramKeys}
object to define how attribute values are
specified in the PCML definition.    This class is intended as a helper
class for implementing subclasses of {@link com.ibm.as400.resource.Resource Resource}.
**/
//
// Implementation notes:
//
// *  This class is package-scope.  I did not make it public because I 
//    am not convinced that it is needed for general purpose components.
//
class ProgramKeyAttributeSetter
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Private data. 
    private static final String         numberOfKeysDataName_   = "numberOfKeys";
    private static final String         contentsDataName_       = "contents";

    private ProgramKeys                 attributeKeys_          = null;
    private CharConverter               charConverter_          = null;
    private ProgramCallDocument         document_               = null;
    private String                      keysDataName_           = null;
    private String                      programName_            = null;
    private AS400                       system_                 = null;



/**
Constructs a ProgramKeyAttributeSetter object.

@param system               The system.
@param documentName         The PCML document.
@param programName          The program name.
@param keysDataName         The keys data name.
@param attributeKeys        The attribute keys.
**/
    public ProgramKeyAttributeSetter(AS400 system,
                                  ProgramCallDocument document, 
                                  String programName,
                                  String keysDataName,
                                  ProgramKeys attributeKeys)
    {
        if (system == null)
            throw new NullPointerException("system");
        if (document == null)
            throw new NullPointerException("document");
        if (programName == null)
            throw new NullPointerException("programName");
        if (keysDataName == null)
            throw new NullPointerException("keysDataName");
        if (attributeKeys == null)
            throw new NullPointerException("attributeKeys");

        document_           = document;
        keysDataName_       = keysDataName;
        programName_        = programName;
        attributeKeys_      = attributeKeys;
        system_             = system;        
        
        try {
            charConverter_ = new CharConverter(system.getCcsid(), system);
        }
        catch(UnsupportedEncodingException e) {
            // Just trace this, since we are using the CCSID reported by
            // the system.
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error setting system on program attribute setter", e);
        }

    }



/**
Pads a String to an exact length.

@param value    The original String.
@param length   The padded length.
@return         The padded String.
**/
    private static String pad(String value, int length)
    {
        StringBuffer buffer = new StringBuffer(value);
        for(int i = value.length(); i < length; ++i)
            buffer.append(' ');
        return buffer.toString();
    }



// @A2C
/**
Sets the attribute values.  The system must be set before this is called.

@param attributeIDs     The attribute IDs.
@param values           The values.  This array must contain the same number of
                        elements as attributeIDs.
@param bidiStringTypes  The bidi string types as defined by the CDRA (Character Data 
                        Representataion Architecture). See 
                        {@link com.ibm.as400.access.BidiStringType BidiStringType}
                        for more information and valid values. 
                        
@exception ResourceException    If an error occurs.                        
**/
	public void setValues(Object[] attributeIDs, Object[] values, int[] bidiStringTypes)
    throws ResourceException
	{
        if (attributeIDs == null)
            throw new NullPointerException("attributeIDs");
        if (values == null)
            throw new NullPointerException("values");
        if (attributeIDs.length != values.length)
            throw new ExtendedIllegalArgumentException("values", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        if (charConverter_ == null)
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        // If there is nothing to set, then do nothing.
        if (attributeIDs.length == 0)
            return;

        try {
            synchronized(this) {

                // Come up with a list of actual keys to write. 
                Hashtable actualValues = new Hashtable();
                Hashtable stringTypes = new Hashtable();
                for (int i = 0; i < attributeIDs.length; ++i) {
                    int[] keys          = attributeKeys_.getKeys(attributeIDs[i]);
                    int[] types         = attributeKeys_.getTypes(attributeIDs[i]);
                    int[] lengths       = attributeKeys_.getLengths(attributeIDs[i]);
                    ValueMap[] valueMaps= attributeKeys_.getValueMaps(attributeIDs[i]);

                    // For each of the keys returned...
                    for(int j = 0; j < keys.length; ++j) {

                        // Pass the value through the value map if needed.
                        Object value = values[i];
                        if (valueMaps != null)
                            if (valueMaps[j] != null)
                                value = valueMaps[j].ltop(values[i], system_);
    
                        // For BINARY and CHAR, just add the actual value.  For qualifieds, store
                        // the value away until both parts have been specified.
                        switch(types[j]) {
                        case ProgramKeys.BINARY:
                            actualValues.put(new Integer(keys[j]), value);
                            break;
                        case ProgramKeys.CHAR:
                            Integer key = new Integer(keys[j]);
                            actualValues.put(key, pad((String)value, lengths[j]));
                            stringTypes.put(key, new Integer(bidiStringTypes[i]));
                            break;
                        default:
                            if (Trace.isTraceOn())
                                Trace.log(Trace.ERROR, "Error setting attribute " + attributeIDs[i] + ", type = " + types[i]);
                        }
                    }
                }

                // Set the number of keys.
                StringBuffer buffer = new StringBuffer();
                buffer.append(programName_);
                buffer.append('.');
                buffer.append(keysDataName_);
                buffer.append('.');

                document_.setIntValue(buffer.toString() + numberOfKeysDataName_, actualValues.size());

                // Set the keys in the PCML document.
                String programAndContentsDataName = buffer.toString() + contentsDataName_;
                Enumeration keys = actualValues.keys();
                for (int i = 0; keys.hasMoreElements(); ++i) {                   
                    Integer key         = (Integer)keys.nextElement();
                    int[] indices       = new int[] { i };
                    Object value        = actualValues.get(key);
                    Integer stringType  = (Integer)stringTypes.get(key);       // @A2A

                    document_.setIntValue(programAndContentsDataName + ".key", indices, key.intValue());
                    if (value instanceof Integer) {
                        document_.setIntValue(programAndContentsDataName + ".lengthOfAttributeInformation", indices, 20);
                        document_.setValue(programAndContentsDataName + ".type", indices, "B");
                        document_.setIntValue(programAndContentsDataName + ".length", indices, 4);
                        document_.setValue(programAndContentsDataName + ".data", indices, BinaryConverter.intToByteArray(((Integer)value).intValue()));
                    }
                    else if (value instanceof String) {
                        int length = ((String)value).length();
                        document_.setIntValue(programAndContentsDataName + ".lengthOfAttributeInformation", indices, length + 16);
                        document_.setValue(programAndContentsDataName + ".type", indices, "C");
                        document_.setIntValue(programAndContentsDataName + ".length", indices, length);
                        document_.setValue(programAndContentsDataName + ".data", indices, charConverter_.stringToByteArray((String)value, stringType.intValue())); // @A2C
                    }
                    else {
                        if (Trace.isTraceOn())
                            Trace.log(Trace.ERROR, "Error setting attribute with key " + key + ", value class = " + value.getClass());
                    }

                }                
                
            }
                                                                                                                   // Set the number of keys.
            boolean success = document_.callProgram(programName_);
            if (! success) 
                throw new ResourceException(document_.getMessageList(programName_));                
        }
        catch(PcmlException e) {
            throw new ResourceException(e);
        }
    }

}
