///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ProgramAttributeGetter.java
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
import com.ibm.as400.access.Trace;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;
import java.io.Serializable;
import java.util.Vector;



/**
The ProgramAttributeGetter class gets attribute values by calling a
program on the server.  The program call is specified using Program Call
Markup Language (PCML).

<p>This class uses a {@link com.ibm.as400.resource.ProgramMap ProgramMap}
object to define how attribute values are
specified in the PCML definition.   This class is intended as a helper
class for implementing subclasses of {@link com.ibm.as400.resource.Resource Resource}.
**/
public class ProgramAttributeGetter
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Private data.
    private ProgramMap                  attributeMap_           = null;
    private ProgramCallDocument         document_               = null;
    private Vector                      programsCalled_         = new Vector();
    private AS400                       system_                 = null;



/**
Constructs a ProgramAttributeGetter object.

@param system               The system.
@param document             The PCML document.
@param attributeMap         The attribute map.
**/
    public ProgramAttributeGetter(AS400 system,
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
Clears any buffered attribute values.  Calling this method
ensures that subsequent calls to getValues() are current.
**/
    public void clearBuffer()
    {
        programsCalled_.removeAllElements();
    }



// @A2C
/**
Returns an attribute value.

@param attributeID     The attribute ID.
@return                The attribute value.

@exception ResourceException    If an error occurs.
**/
     public Object getValue(Object attributeID)
        throws ResourceException
     {
         return getValue(attributeID, AS400BidiTransform.getStringType((char)system_.getCcsid()));
     }



// @A2A
/**
Returns an attribute value.

@param attributeID      The attribute ID.
@param bidiStringType   The bidi string type as defined by the CDRA (Character Data 
                        Representataion Architecture). See 
                        {@link com.ibm.as400.access.BidiStringType BidiStringType}
                        for more information and valid values. 
@return                 The attribute value.

@exception ResourceException    If an error occurs.
**/
     public Object getValue(Object attributeID, int bidiStringType)
        throws ResourceException
     {
        if (attributeID == null)
            throw new NullPointerException("attributeID");

        synchronized(this) {
            ProgramMapEntry entries[] = attributeMap_.getEntries(attributeID);
            int indexUsed = -1;

            // If the definitions include a program that has already been called,
            // then that is good enough.
            String programName = null;
            for(int j = 0; j < entries.length; ++j) {
                programName = entries[j].getProgramName();
                if (programsCalled_.contains(programName)) {
                    indexUsed = j;

                    if (Trace.isTraceOn())
                        Trace.log(Trace.INFORMATION, "Getting attribute value " + attributeID + " using PCML document "
                            + document_ + ", program " + programName + " (which has already been called).");
                    break;
                }
            }

            // If none of the potential programs have been called, then
            // pick the first one and call it.
            if (indexUsed < 0) {
                indexUsed = 0;
                programName = entries[0].getProgramName();

                if (Trace.isTraceOn())
                    Trace.log(Trace.INFORMATION, "Getting attribute value " + attributeID + " using PCML document "
                        + document_ + ", program " + programName + " (which is about to be called).");

                boolean success = false;
                try {
                    success = document_.callProgram(programName);
                    if (! success)
                        throw new ResourceException(document_.getMessageList(programName));
                    programsCalled_.addElement(programName);
                }
                catch (Exception e) {
                    throw new ResourceException(ResourceException.ATTRIBUTES_NOT_RETURNED, e);
                }
            }

            // Get the value from the PCML document.
            try {
                return entries[indexUsed].getValue(system_, document_, null, null, bidiStringType); // @A2C
            }
            catch (PcmlException e) {
                throw new ResourceException(ResourceException.ATTRIBUTES_NOT_RETURNED, e);
            }
        }
     }



}
