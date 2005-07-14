///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ProgramAttributeSetter.java
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
import com.ibm.as400.access.Trace;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;



/**
The ProgramAttributeSetter class sets attribute values by calling a
program on the server.  The program call is specified using Program Call
Markup Language (PCML).  

<p>This class uses a {@link com.ibm.as400.resource.ProgramMap ProgramMap} 
object to define how attribute values are
specified in the PCML definition.   This class is intended as a helper
class for implementing subclasses of {@link com.ibm.as400.resource.Resource Resource}</a>.
@deprecated Use packages <tt>com.ibm.as400.access</tt> and <tt>com.ibm.as400.access.list</tt> instead. 
**/
public class ProgramAttributeSetter
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Private data.
    private ProgramMap                  attributeMap_           = null;
    private int                         defaultBidiStringType_  = -1;                   // @A2A
    private ProgramCallDocument         document_               = null;
    private AS400                       system_                 = null;



/**
Constructs a ProgramAttributeSetter object.

@param system               The system.              
@param document             The PCML document.
@param attributeMap         The attribute map.
**/
    public ProgramAttributeSetter(AS400 system,
                                  ProgramCallDocument document, 
                                  ProgramMap attributeMap)
    {
        if (system == null)
            throw new NullPointerException("system");
        if (document == null)
            throw new NullPointerException("document");
        if (attributeMap == null)
            throw new NullPointerException("attributeMap");

        system_             = system;
        document_           = document;
        attributeMap_       = attributeMap;
    }



/**
Initializes the attribute values for the setter.
This is useful for "setter" programs that have multiple fields, all of which must contain valid values.

@param attributeGetter      The attribute getter with which to retrieve the current values from the system.
                        
@exception ResourceException    If an error occurs.                        
**/
    public void initializeAttributeValues(ProgramAttributeGetter attributeGetter)
      throws ResourceException
    {
      initializeAttributeValues(attributeGetter, null);
    }



/**
Initializes the attribute values for the setter.
This is useful for "setter" programs that have multiple fields, all of which must contain valid values.

@param attributeGetter      The attribute getter with which to retrieve the current values from the system.
@param attrsToInitializeFirst  The IDs of the attributes to initialize first, in desired order of initialization.  These will typically be "length of another field" attributes.

@exception ResourceException    If an error occurs.                        
**/
    public void initializeAttributeValues(ProgramAttributeGetter attributeGetter, Object[] attrsToInitializeFirst)
      throws ResourceException
    {
      attributeGetter.clearBuffer();
      if (defaultBidiStringType_ == -1)                                                             // @A2A
          defaultBidiStringType_ = AS400BidiTransform.getStringType((char)system_.getCcsid());      // @A2A

      try
      {
        synchronized(this) {
          Object[] attrIDs = attributeMap_.getIDs();

          // First rearrange the ids into the desired order of initialization.
          if (attrsToInitializeFirst != null)
          {
            for (int i=0; i<attrsToInitializeFirst.length; i++)
            {
              Object id = attrsToInitializeFirst[i];
              boolean foundIt = false;
              for (int j=i; j<attrIDs.length; j++) {
                if (attrIDs[j].equals(id))
                {
                  Object temp = attrIDs[i];
                  attrIDs[i] = attrIDs[j];
                  attrIDs[j] = temp;
                  foundIt = true;
                  break;
                }
              }
              if (! foundIt) {
                Trace.log(Trace.ERROR, "Specified attribute ID is not in the getter's map: " + (String)id);
                throw new ResourceException(ResourceException.ATTRIBUTES_NOT_SET);
              }
            }
          }
          
          for (int i=0; i<attrIDs.length; i++)
          {
            String attrID = (String)attrIDs[i];
            Object value = attributeGetter.getValue(attrID);
            ProgramMapEntry[] entries = attributeMap_.getEntries(attrID);
            String programName = null;
            for (int j = 0; j < entries.length; ++j)
            {
              programName = entries[j].getProgramName();
              entries[j].setValue(system_, document_, null, null, value, defaultBidiStringType_);   // @A2C
            }
          }
        }
      }
      catch (PcmlException e) {
        throw new ResourceException(ResourceException.ATTRIBUTES_NOT_SET, e);
      }
    }



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
        if (values == null)
            throw new NullPointerException("values");
        if (attributeIDs.length != values.length)
            throw new ExtendedIllegalArgumentException("values", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        if (defaultBidiStringType_ == -1)                                                             // @A2A
            defaultBidiStringType_ = AS400BidiTransform.getStringType((char)system_.getCcsid());      // @A2A

        synchronized(this) {
            Vector programsToCall = new Vector();
            for(int i = 0; i < attributeIDs.length; ++i) {
                ProgramMapEntry[] entries = attributeMap_.getEntries(attributeIDs[i]);
                
                // If the definitions include a program that has already going to be called,
                // then that is good enough.  
                String programName = null;
                int indexUsed = -1;
                for(int j = 0; j < entries.length; ++j) {
                    programName = entries[j].getProgramName();
                    if (programsToCall.contains(programName)) {                        
                        indexUsed = j;
                        try {
                            entries[j].setValue(system_, document_, null, null, values[i], defaultBidiStringType_); // @A2C
                            if (Trace.isTraceOn())
                                Trace.log(Trace.INFORMATION, "Setting attribute value using PCML document " 
                                    + document_ + ", program " + programName + ".");
                        }
                        catch(PcmlException e) {
                            throw new ResourceException(ResourceException.ATTRIBUTES_NOT_SET, e);
                        }
                        break;
                    }                    
                }

                // If none of the potential programs have been called, then 
                // pick the first one and call it.
                if (indexUsed < 0) {
                    indexUsed = 0;
                    programName = entries[0].getProgramName();                    
                    try {
                        entries[0].setValue(system_, document_, null, null, values[i], defaultBidiStringType_); // @A2C
                        if (Trace.isTraceOn())
                            Trace.log(Trace.INFORMATION, "Setting attribute value using PCML document " 
                                + document_ + ", program " + programName + ".");
                        programsToCall.addElement(programName);
                    }
                    catch(PcmlException e) {
                        throw new ResourceException(ResourceException.ATTRIBUTES_NOT_SET, e);
                    }
                    //break;                 // @A3d
                }
            }
            
            // Call all of the necessary programs.
            Enumeration programs = programsToCall.elements();
            while(programs.hasMoreElements()) {
                String programName = (String)programs.nextElement();
                boolean success = false;
                try {
                    success = document_.callProgram(programName);
                    if (! success) 
                        throw new ResourceException(document_.getMessageList(programName));
                }
                catch (PcmlException e) {
                    throw new ResourceException(ResourceException.ATTRIBUTES_NOT_SET, e);
                }
            }
        }
	}



}

