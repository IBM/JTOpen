///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: CommandAttributeSetter.java
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
import com.ibm.as400.access.CharConverter;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.Trace;
import java.io.ByteArrayOutputStream;                                          
import java.io.IOException; 
import java.io.OutputStreamWriter;                                          
import java.io.Serializable;
import java.io.Writer;                                          
import java.util.Enumeration;
import java.util.Hashtable;



/**
The CommandAttributeSetter class sets attribute values by issuing an
AS/400 command.  The base command string is usually an AS/400 command
name and any invariant parameter names and values.

<p>This class uses a <a href="CommandMap.html">
CommandMap</a> object to define how attribute values are
specified in the command string.
**/
class CommandAttributeSetter
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Private data.
    private CommandMap                  attributeMap_           = null;
    private CommandCall                 commandCall_            = null;
    private CharConverter               converter_              = null;                 // @A2A
    private int                         defaultStringType_      = -1;                   // @A2A
    private Hashtable                   invariants_             = new Hashtable();
    private AS400                       system_                 = null;



/**
Constructs a CommandAttributeSetter object.

@param system           The system.
@param attributeMap     The attribute map.
**/
    public CommandAttributeSetter(AS400 system, CommandMap attributeMap)
    {
        if (system == null)
            throw new NullPointerException("system");
        if (attributeMap == null)
            throw new NullPointerException("attributeMap");

        system_ = system;
        commandCall_ = new CommandCall(system);
        attributeMap_ = attributeMap;
    }



// @A2A
    private void append(ByteArrayOutputStream commandString, 
                        String text) 
    throws IOException                                                                            
    {
        if (defaultStringType_ == -1)
            defaultStringType_ = AS400BidiTransform.getStringType((char)system_.getCcsid());
        if (converter_ == null)
            converter_ = new CharConverter(system_.getCcsid(), system_);                                                    
        commandString.write(converter_.stringToByteArray(text));
    }



// @A2A
    private void append(ByteArrayOutputStream commandString, 
                        String parameterName, 
                        Object parameterValue) 
    throws IOException                                                                            
    {
        if (defaultStringType_ == -1)
            defaultStringType_ = AS400BidiTransform.getStringType((char)system_.getCcsid());
        append(commandString, parameterName, parameterValue, defaultStringType_);
    }



// @A2A
    private void append(ByteArrayOutputStream commandString, 
                        String parameterName, 
                        Object parameterValue,
                        int bidiStringType) 
    throws IOException                                                                            
    {
        if (converter_ == null)
            converter_ = new CharConverter(system_.getCcsid(), system_);                                                    
        commandString.write(converter_.stringToByteArray(parameterName + '('));
        commandString.write(converter_.stringToByteArray(parameterValue.toString(), bidiStringType));
        commandString.write(converter_.stringToByteArray(") "));
    }



/**
Sets an invariant parameter value.

@param commandName      The command name.
@param parameterName    The command parameter name.
@param parameterValue   The command parameter value.
**/
    public void setParameterValue(String commandName, String parameterName, String parameterValue)
    {
        if (commandName == null)
            throw new NullPointerException("commandName");
        if (parameterName == null)
            throw new NullPointerException("parameterName");
        if (parameterValue == null)
            throw new NullPointerException("parameterValue");

        synchronized(this) {
            Hashtable parameters;
            if (invariants_.containsKey(commandName))
                parameters = (Hashtable)invariants_.get(commandName);
            else {
                parameters = new Hashtable();
                invariants_.put(commandName, parameters);
            }

            parameters.put(parameterName, parameterValue);
        }
    }



// @A2C
/**
Sets the attribute values.  The system must be set before this is called.

@param attributeIDs     The attribute IDs.
@param values           The values.  This array must contain the same number of
                        elements as attributeIDs.

@exception ResourceException    If an error occurs.
**/
     public void setValues(Object[] attributeIDs, Object[] values)
        throws ResourceException
     {
         if (attributeIDs == null)
             throw new NullPointerException("attributeIDs");

         if (defaultStringType_ == -1)
             defaultStringType_ = AS400BidiTransform.getStringType((char)system_.getCcsid());
         int[] bidiStringTypes = new int[attributeIDs.length];
         for(int i = 0; i < bidiStringTypes.length; ++i)
             bidiStringTypes[i] = defaultStringType_;

         setValues(attributeIDs, values, bidiStringTypes);
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
        if (attributeIDs.length != bidiStringTypes.length)                                                                              // @A2A
            throw new ExtendedIllegalArgumentException("bidiStringTypes", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);  // @A2A
        if (commandCall_.getSystem() == null)
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

        try {                                                                                   // @A2A
            synchronized(this) {
    
                // Create a list of command strings to execute.  The command strings                   @A2C
                // are ByteArrayOutputStreams (and ultimately byte[]s) to enable bidi strings.         @A2A
                Hashtable commandStrings = new Hashtable();
                for (int i = 0; i < attributeIDs.length; ++i) {
                    CommandMapEntry[] entries = attributeMap_.getEntries(attributeIDs[i]);
    
                    // If there is an entry for a command already being used,
                    // then just specify it there.
                    ByteArrayOutputStream commandString = null;                                     // @A2C
                    int indexUsed = -1;
                    for (int j = 0; j < entries.length; ++j) {
                        String commandName = entries[j].getCommandName();
                        if (commandStrings.containsKey(commandName)) {
                            commandString = (ByteArrayOutputStream)commandStrings.get(commandName); // @A2C
                            indexUsed = j;
                            break;
                        }
                    }
    
                    // If there is no command already being used, then use the first
                    // entry.
                    if (commandString == null) {
                        String commandName = entries[0].getCommandName();
                        commandString = new ByteArrayOutputStream();                                // @A2C
                        append(commandString, commandName + ' ');                                   // @A2A
                        if (invariants_.containsKey(commandName)) {
                            Hashtable parameters = (Hashtable)invariants_.get(commandName);
                            Enumeration enum = parameters.keys();
                            while(enum.hasMoreElements()) {
                                String parameterName = (String)enum.nextElement();
                                String parameterValue = (String)parameters.get(parameterName);
                                append(commandString, parameterName, parameterValue);               // @A2C
                            }
                        }
                        commandStrings.put(commandName, commandString);
                        indexUsed = 0;
                    }
    
                    // Append the attribute and value to the command string.
                    ValueMap valueMap = entries[indexUsed].getValueMap();
                    Object value = (valueMap != null) ? valueMap.ltop(values[i], commandCall_.getSystem()) : values[i];
                    append(commandString, entries[indexUsed].getParameterName(), value, bidiStringTypes[i]); // @A2C
                }
    
                // Execute the command strings.
                Enumeration enum = commandStrings.elements();
                while(enum.hasMoreElements()) {
                    ByteArrayOutputStream commandString = (ByteArrayOutputStream)enum.nextElement();        // @A2C
                    byte[] asBytes = commandString.toByteArray();                                           // @A2C
                    if (Trace.isTraceOn()) {                                                                // @A2C
                        String asString = converter_.byteArrayToString(asBytes);                            // @A2A
                        Trace.log(Trace.INFORMATION, "Setting attribute values using command: "             // @A2C
                                  + asString + ".");                                                        // @A2C
                    }                                                                                       // @A2A
                    try {
                        boolean success = commandCall_.run(asBytes);                                        // @A2C
                        if (! success)
                            throw new ResourceException(commandCall_.getMessageList());
                    }
                    catch(Exception e) {
                        throw new ResourceException(ResourceException.ATTRIBUTES_NOT_SET, e);
                    }
                }
            }
        }                                                                                               // @A2A
        catch(IOException e) {                                                                          // @A2A
            throw new ResourceException(ResourceException.ATTRIBUTES_NOT_SET, e);                       // @A2A
        }                                                                                               // @A2A
    }


}
