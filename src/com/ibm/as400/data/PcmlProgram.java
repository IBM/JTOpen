///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PcmlProgram.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.ProgramCall;
import com.ibm.as400.access.ServiceProgramCall;                     // @B1A
import com.ibm.as400.access.ProgramParameter;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.ErrorCompletingRequestException;

import java.beans.PropertyVetoException;

import java.io.IOException;
import java.io.ObjectOutputStream;                                  // @C1A

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
*/
class PcmlProgram extends PcmlDocNode
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 7278339230268347333L;

    private static final String PROGRAMATTRIBUTES[] = {
          "name",
          "path",
          "parseorder",
          "entrypoint",         // PCML Ver. 2.0
          "returnvalue",        // PCML Ver. 2.0
          "threadsafe",         // PCML Ver. 3.0   @A1A
          "epccsid",            // PCML Ver. 4.0   @D1A
    };

    private static final int VERSION_1_ATTRIBUTE_COUNT = 3;
    private static final int VERSION_2_ATTRIBUTE_COUNT = 5;
    private static final int VERSION_3_ATTRIBUTE_COUNT = 6;
    private static final int VERSION_4_ATTRIBUTE_COUNT = 7; // @D1A

    private String m_Path;
    private String m_Parseorder;

    // The following attributes added for PCML v2.0
    private String m_EntrypointStr;     // entrypoint=, string literal  @B1A
    private String m_ReturnvalueStr;    // returnvalue=, string literal @B1A

    // The following attributes added for PCML v3.0
    private String m_ThreadsafeStr;     // threadsafe=, string literal  @C6A
    private boolean m_ThreadsafeOverride;  // The use of this variable has changed  @C6A @D2C
                                           // since the inital implementation.  We leave
                                           // the same name for serialization compatibility.
                                           // This is initialized to be the boolean representation
                                           // of the "threadsafe=" attribute and is changed when 
                                           // setThreadsafeOverride() is called.  This is the value 
                                           // that is used when setting the threadsafety in the 
                                           // ProgramCall object.
    private boolean m_ThreadsafeOverrideCalled;    // This is initialized to false. @D2A
                                                    // If setThreadsafeOverride() is called, this
                                                    // will be set to true.

    // The following attributes added for PCML v4.0
    private String m_EpCcsidStr;        // epccsid=, string literal     @D1A
    private int m_EpCcsid;              // integer value                @D1A

    /***********************************************************
     Semi-Transient Members --
     Not written when serializing interface definition.
     Written when serializing ProgramCallDocument object.
    ***********************************************************/
    private ProgramCall m_pgmCall;      // ProgramCall instance from most recent call
    private boolean m_pgmRc;            // Return code from most recent call             @C1A
    private int m_pgmCCSID;             // CCSID of host last time program was run       @C1A
    private AS400Message[] msgList;     // Array of AS400Message                         @C1C
    private int m_IntReturnValue;       // Int return value for a service program call   @B1A @C1C
    private int m_Errno;                // Errno for a service program call              @B1A @C1C

    /**
    */
    public PcmlProgram()
    {
        m_pgmRc = false;                                            // @C1A
        m_pgmCCSID = -1;                                            // @C1A
        msgList = null;
        m_IntReturnValue = 0;                                       // @C1A
        m_Errno = 0;                                                // @C1A
        m_ThreadsafeOverrideCalled = false;                         // @D2A
    }

    // Constructor
    public PcmlProgram(PcmlAttributeList attrs)                     // @C3C
    {
        super(attrs);                                               // @C3C
        setNodeType(PcmlNodeType.PROGRAM);                          // @C3C

        m_pgmRc = false;                                            // @C1A
        m_pgmCCSID = -1;                                            // @C1A
        msgList = null;
        m_IntReturnValue = 0;                                       // @C1A
        m_Errno = 0;                                                // @C1A
        m_ThreadsafeOverrideCalled = false;                         // @D2A

        // **********************************
        // Set attribute values
        //
        // The following code extracts the attribute values
        // from the parsed document node and
        // stores the values in private data members.
        // **********************************

        setPath(getAttributeValue("path"));
        setParseorder(getAttributeValue("parseorder"));
        setEntrypoint(getAttributeValue("entrypoint"));             // @B1A
        setReturnvalue(getAttributeValue("returnvalue"));           // @B1A
        setThreadsafe(getAttributeValue("threadsafe"));             // @C6A
        setEpCcsid(getAttributeValue("epccsid"));                   // @D1A
    }

    public Object clone()                                           // @C5A
    {                                                               // @C5A
        PcmlProgram node = (PcmlProgram) super.clone();             // @C5A
        // Cloning does not include 'live' data, only the interface
        // definitions described by the PCML tags.
        // Null out the 'semi-transient' data values.
        node.m_pgmRc = false;                                       // @C5A
        node.m_pgmCCSID = -1;                                       // @C5A
        node.msgList = null;                                        // @C5A
        node.m_IntReturnValue = 0;                                  // @C5A
        node.m_Errno = 0;                                           // @C5A

        return node;                                                // @C5A
    }                                                               // @C5A

    // Custom serialization
    private void writeObject(ObjectOutputStream out) throws IOException // @C1A
    {                                                               // @C1A
		synchronized (this)                                         // @C1A
		{                                                           // @C1A
			// Keep a local copies of semi-transient data
			boolean localPgmRc = m_pgmRc;                           // @C1A
			int localPgmCCSID = m_pgmCCSID;                         // @C1A
			AS400Message[] localMsgList = msgList;                  // @C1A
			int localIntReturnValue = m_IntReturnValue;             // @C1A
			int localErrno = m_Errno;                               // @C1A

			// If not saving with serialization, temporarily null out the
			// scalar and vector data values member variables
			// so they are not written to the ObjectOutputStream.
			if ( !getDoc().isSerializingWithData() )                // @C1A
			{                                                       // @C1A
                m_pgmRc = false;                                    // @C1A
                m_pgmCCSID = -1;                                    // @C1A
                msgList = null;                                     // @C1A
                m_IntReturnValue = 0;                               // @C1A
                m_Errno = 0;                                        // @C1A
			}                                                       // @C1A

			// Perform default serialization
			out.defaultWriteObject();                               // @C1A

			// Restore semi-transient data
			m_pgmRc = localPgmRc;                                   // @C1A
			m_pgmCCSID = localPgmCCSID;                             // @C1A
			msgList = localMsgList;                                 // @C1A
			m_IntReturnValue = localIntReturnValue;                 // @C1A
			m_Errno = localErrno;                                   // @C1A
		} // end of synchronized code                               // @C1A
    }                                                               // @C1A

    // Returns a string containing the entrypoint attribute
    String getEntrypoint()                                          // @B1A
    {                                                               // @B1A
        return m_EntrypointStr;                                     // @B1A
    }                                                               // @B1A

    // Returns a string containing the program name
    String getPath()
    {
        if (m_Path != null)
        {
            return m_Path;
        }
        else
        {
            return "/QSYS.LIB/" + getName() + ".PGM";
        }
    }

    // Returns a string containing the parseorder attribute
    String getParseorder()
    {
        return m_Parseorder;
    }

    // Returns a string containing the returnvalue attribute
    String getReturnvalue()                                         // @B1A
    {                                                               // @B1A
        return m_ReturnvalueStr;                                    // @B1A
    }                                                               // @B1A

    // Returns the string containing the threadsafe= attribute
    String getThreadsafe()                                          // @C6A
    {                                                               // @C6A
        return m_ThreadsafeStr;                                     // @C6A
    }                                                               // @C6A

    // Returns the integer value of the entrypoint ccsid
    int getEpCcsid()                                                // @D1A
    {                                                               // @D1A
        return m_EpCcsid;                                           // @D1A
    }                                                               // @D1A

   /**
    * Return the list of valid attributes for the program element.
    **/
    String[] getAttributeList()                                 // @C6A
    {
        int returnCount = 0;                                    // @C6A
        String returnArray[];                                   // @C6A

        if ( getDoc().getVersion().compareTo("2.0") < 0 )       // @C6A
            returnCount = VERSION_1_ATTRIBUTE_COUNT;            // @C6A
        else if ( getDoc().getVersion().compareTo("3.0") < 0 )  // @C6A
            returnCount = VERSION_2_ATTRIBUTE_COUNT;            // @C6A
        else if ( getDoc().getVersion().compareTo("4.0") < 0 )  // @D1A
            returnCount = VERSION_3_ATTRIBUTE_COUNT;            // @D1A
        else                            // Anything else return the entire array
            return PROGRAMATTRIBUTES;                           // @C6A

        returnArray = new String[returnCount];                  // @C6A

        System.arraycopy(PROGRAMATTRIBUTES, 0, returnArray, 0, returnCount);   // @C6A
        return returnArray;                                     // @C6A
    }


    // Returns a boolean reflecting the current setting threadsafety
    boolean getThreadsafeOverride()                                 // @C6A
    {                                                               // @C6A
        return m_ThreadsafeOverride;                                // @C6A
    }                                                               // @C6A

    // Sets the entrypoint= attribute value
    void setEntrypoint(String entrypoint)                           // @B1A
    {                                                               // @B1A
        // Handle null or empty string
        if (entrypoint == null || entrypoint.equals(""))            // @B2A
        {                                                           // @B2A
            m_EntrypointStr = null;                                 // @B2A
            return;                                                 // @B2A
        }                                                           // @B2A

        m_EntrypointStr = entrypoint;                               // @B1A
    }                                                               // @B1A

    void setEpCcsid(String ccsid)                                   // @D1A
    {                                                               // @D1A
        // Handle null or empty string                              // @D1A
        if (ccsid == null || ccsid.equals(""))                      // @D1A
        {                                                           // @D1A
            m_EpCcsidStr = null;                                    // @D1A
            m_EpCcsid = 0;                                          // @D1A
            return;                                                 // @D1A
        }                                                           // @D1A

        // Try to parse an integer from the attribute value         // @D1A
            m_EpCcsidStr = ccsid;                                   // @D1A
            m_EpCcsid = Integer.parseInt(ccsid);                    // @D1A
    }

    // Sets the path= attribute value
    void setPath(String path)
    {
        m_Path = path;
    }

    // Sets the parseorder= attribute value
    void setParseorder(String parseorder)
    {
        // Handle null or empty string
        if (parseorder == null || parseorder.equals(""))            // @B2A
        {                                                           // @B2A
            m_Parseorder = null;                                    // @B2A
            return;                                                 // @B2A
        }                                                           // @B2A

        m_Parseorder = parseorder;
    }

    /**
     Returns the ProgramCall object that was used in the most recent invocation of {@link #callProgram() callProgram()}.
     @return The ProgramCall object; null if callProgram has not been called.
     **/
    ProgramCall getProgramCall()
    {
      return m_pgmCall;
    }

    // Sets the returnvalue= attribute value
    void setReturnvalue(String returnvalue)                         // @B1A
    {                                                               // @B1A
        // Handle null or empty string
        if (returnvalue == null || returnvalue.equals(""))          // @B2A
        {                                                           // @B2A
            m_ReturnvalueStr = null;                                // @B2A
            return;                                                 // @B2A
        }                                                           // @B2A

        m_ReturnvalueStr = returnvalue;                             // @B1A
    }                                                               // @B1A

    // Sets the threadsafe= attribute value
    void setThreadsafe(String threadsafe)                           // @C6A
    {                                                               // @C6A
        // Handle null or empty string
        if (threadsafe == null || threadsafe.equals(""))            // @C6A
        {                                                           // @C6A
            m_ThreadsafeStr = null;                                 // @C6A
            m_ThreadsafeOverride = false;   // Initialize the override @C6A
            return;                                                 // @C6A
        }                                                           // @C6A

        if (threadsafe.equals("true"))                              // @C6A
            m_ThreadsafeOverride = true;    // Initialize the override @C6A
        else                                                        // @C6A
            m_ThreadsafeOverride = false;                           // @C6A
        m_ThreadsafeStr = threadsafe;                               // @C6A
    }                                                               // @C6A

    // Overrides the threadsafe= attribute
    void setThreadsafeOverride(boolean threadsafe)                  // @C6A
    {                                                               // @C6A
        m_ThreadsafeOverrideCalled = true;                          // @D2A
        m_ThreadsafeOverride = threadsafe;                          // @C6A
    }                                                               // @C6A

    protected void checkAttributes()
    {
        super.checkAttributes();

        // Validate the parseorder attribute
        String parseorder = getParseorder();
        if (parseorder != null)
        {
            StringTokenizer tokens = new StringTokenizer(parseorder);
            while (tokens.hasMoreTokens())
            {
                String token = tokens.nextToken();
                String nodeName = getQualifiedName() + "." + token;
                PcmlNode node = getRootNode().getElement(nodeName);
                if (node == null)
                {
                    getDoc().addPcmlSpecificationError(DAMRI.PARSEORDER_NOT_FOUND, new Object[] {makeQuotedAttr("parseorder", parseorder), nodeName, getBracketedTagName(),  getNameForException()} );
                }
                else
                {
                    if (node.getParent() != this)
                    {
                        getDoc().addPcmlSpecificationError(DAMRI.PARSEORDER_NOT_CHILD, new Object[] {makeQuotedAttr("parseorder", parseorder), nodeName, getBracketedTagName(), getNameForException()} );
                    }
                }
            }
        }


        // Verify the entrypoint= attribute
        if (m_EntrypointStr != null)                                // @B1A
        {                                                           // @B1A
            // Only allow this attribute when the pcml version is 2.0 or higher (e.g. <pcml version="2.0">)
            if ( getDoc().getVersion().compareTo("2.0") < 0 )       // @B1A
            {                                                       // @B1A
                getDoc().addPcmlSpecificationError(DAMRI.BAD_PCML_VERSION, new Object[] {makeQuotedAttr("entrypoint", m_EntrypointStr), "2.0", getBracketedTagName(), getNameForException()} ); // @B1A
            }                                                       // @B1A

            // The following section is moved to the callProgram method to allow for dynamically setting
            // the path.  The check cannot be done until the program is called.

            // Only allow this attribute when the path= attribute specifies a service program (*SRVPGM).
            // if ( !getPath().toUpperCase().endsWith(".SRVPGM") )     // @B1A
            // {                                                       // @B1A
            //    getDoc().addPcmlSpecificationError(DAMRI.NOT_SRVPGM, new Object[] {makeQuotedAttr("entrypoint", m_EntrypointStr), getBracketedTagName(), getNameForException()} ); // @B1A
            // }                                                       // @B1A

            // Only allow this attribute when the program has 7 or fewer parameters
            // Note: This check does not take into account that minvrm= and maxvrm=
            //       can reduce the number of parameters at runtime
            // if ( getNbrChildren() > 7 )                             // @B1A
            // {                                                       // @B1A
            //    getDoc().addPcmlSpecificationError(DAMRI.TOO_MANY_PARMS, new Object[] {makeQuotedAttr("entrypoint", m_EntrypointStr), new Integer(7), getBracketedTagName(), getNameForException()} ); // @B1A
            // }                                                       // @B1A
        }                                                           // @B1A
        // else                                                        // @B1A
        // {                                                           // @B1A
            // If entrypoint not specified, make sure it is not a *SRVPGM
        //     if ( getPath().toUpperCase().endsWith(".SRVPGM") )      // @B1A
        //     {                                                       // @B1A
        //         getDoc().addPcmlSpecificationError(DAMRI.NO_ENTRYPOINT, new Object[] {makeQuotedAttr("entrypoint", m_EntrypointStr), getBracketedTagName(), getNameForException()} ); // @B1A
        //     }                                                       // @B1A
        // }                                                           // @B1A

        // Verify the returnvalue= attribute
        if (m_ReturnvalueStr != null)                               // @B1A
        {                                                           // @B1A
            // Only allow this attribute when the pcml version is 2.0 or higher (e.g. <pcml version="2.0">)
            if ( getDoc().getVersion().compareTo("2.0") < 0 )       // @B1A
            {                                                       // @B1A
                getDoc().addPcmlSpecificationError(DAMRI.BAD_PCML_VERSION, new Object[] {makeQuotedAttr("returnvalue", m_ReturnvalueStr), "2.0", getBracketedTagName(), getNameForException()} ); // @B1A
            }                                                       // @B1A

            // The following section is moved to the callProgram method to allow for dynamically setting
            // the path.  The check cannot be done until the program is called.

            // Only allow this attribute when the path= attribute specifies a service program (*SRVPGM).
            // if ( !getPath().endsWith(".SRVPGM") )                   // @B1A
            // {                                                       // @B1A
            //    getDoc().addPcmlSpecificationError(DAMRI.NOT_SRVPGM, new Object[] {makeQuotedAttr("returnvalue", m_ReturnvalueStr), getBracketedTagName(), getNameForException()} ); // @B1A
            //}                                                       // @B1A
        }                                                           // @B1A

        // Verify the threadsafe= attribute
        if (m_ThreadsafeStr != null)                                // @C6A
        {                                                           // @C6A
            // Only allow this attribute when the pcml version is 3.0 or higher (e.g. <pcml version="3.0">)
            if ( getDoc().getVersion().compareTo("3.0") < 0 )       // @C6A
            {                                                       // @C6A
                getDoc().addPcmlSpecificationError(DAMRI.BAD_PCML_VERSION, new Object[] {makeQuotedAttr("threadsafe", m_ThreadsafeStr), "3.0", getBracketedTagName(), getNameForException()} ); // @C6A
            }                                                       // @C6A

        }                                                           // @C6A

        // Verify the epccsid= attribute
        if (m_EpCcsidStr != null)                                   // @D1A
        {                                                           // @D1A
            // Only allow this attribute when the pcml version is 3.0 or higher (e.g. <pcml version="3.0">)
            if ( getDoc().getVersion().compareTo("4.0") < 0 )       // @D1A
            {                                                       // @D1A
                getDoc().addPcmlSpecificationError(DAMRI.BAD_PCML_VERSION, new Object[] {makeQuotedAttr("epccsid", m_EpCcsidStr), "4.0", getBracketedTagName(), getNameForException()} ); // @D1A
            }                                                       // @D1A

        }                                                           // @D1A

    }

    /**
    */
    public boolean callProgram(AS400 as400)
           throws AS400SecurityException,
                  ObjectDoesNotExistException,
                  InterruptedException,
                  ErrorCompletingRequestException,
                  IOException,
                  PcmlException
    {
        ProgramParameter[] childParms;     // One entry in array for every child of <program>           @A1A
                                           // This array is indexed by child number, entries for
                                           // parameters not supported at host VRm are null.
        ProgramParameter[] supportedParms; // One entry in array for each parm supported by host VRM    @A1A
        Enumeration children;              // Enumeration of children of this <program> element
        PcmlDocNode child;                 // Current child element of this <program> element
        PcmlStruct  structNode;            // Current child cast as a <struct> element
        PcmlData    dataNode;              // Current child cast as a <data> element
        boolean bSupportedAtVRM;           // Current element is supported at host VRM                  @A2A
        byte[] bytes;                      // Byte Array for doing toBytes()
        int nbrSupportedParms;             // Number of parameters supported at current host VRM        @A1A
        int childNbr;                      // Current child of this <program> element
        int outputSize;                    // Size of output required for current child element
        int usage;                         // usage= attribute for current child element
        int passby;                        // passby= for <data>, always set to passby="reference" for <struct> @B1A
        PcmlDimensions noDimensions = new PcmlDimensions();

        // Stack of offsets used by PcmlData.parseBytes() and PcmlStruct.parseBytes()
        Hashtable offsetStack = new Hashtable();

        // The following checks were moved here from checkAttributes(). This allows for dynamically setting
        // the path to be used for the callProgram.
        
        // Only allow the returnvalue attribute when the path= attribute specifies a service program (*SRVPGM).
        if ( getReturnvalue() != null && !getPath().toUpperCase().endsWith(".SRVPGM") )                   // @D1A
        {                                                       
           throw new PcmlException(DAMRI.NOT_SRVPGM, new Object[] {makeQuotedAttr("returnvalue", m_ReturnvalueStr), getBracketedTagName(), getNameForException()} ); // @D1A
        }   

        if ( getPath().toUpperCase().endsWith(".SRVPGM") )      // @D1A
        {
            // Service programs must have an entrypoint
            if (getEntrypoint() == null)                        // @D1A
            {
                throw new PcmlException(DAMRI.NO_ENTRYPOINT, new Object[] {makeQuotedAttr("entrypoint", m_EntrypointStr), getBracketedTagName(), getNameForException()} ); // @D1A
            }
            // Service programs can only have 7 or fewer parameters (this is a server limitation).
            // Note: This check does not take into account that minvrm= and maxvrm=
            //       can reduce the number of parameters at runtime
            if ( getNbrChildren() > 7 )                             // @D1A
            {                                                       
                throw new PcmlException(DAMRI.TOO_MANY_PARMS, new Object[] {makeQuotedAttr("entrypoint", m_EntrypointStr), new Integer(7), getBracketedTagName(), getNameForException()} ); // @D1A
            }                                                       
        }
        else if (getEntrypoint() != null)                           // @D1A
        {
            // Only service programs can have an entrypoint
            throw new PcmlException(DAMRI.NOT_SRVPGM, new Object[] {makeQuotedAttr("entrypoint", m_EntrypointStr), getBracketedTagName(), getNameForException()} ); // @D1A
        }

        // Reset return value and "errno" in case an exception occurs
        m_pgmRc = false;                                            // @C1A
        msgList = null;                                             // @C1A
        m_IntReturnValue = 0;                                       // @B1A
        m_Errno = 0;                                                // @B1A

        // Save CCSID of system for character conversion
        m_pgmCCSID = as400.getCcsid();                              // @C2A

        //
        // Convert all input parameters from Java objects to i5/OS data
        //
        childNbr = 0;
        children = getChildren();
        nbrSupportedParms = 0;                                      // @A1A
        passby = ProgramParameter.PASS_BY_REFERENCE;                // @B1A
        childParms = new ProgramParameter[getNbrChildren()];        // @A1A
        while (children.hasMoreElements())
        {

            child = (PcmlDocNode) children.nextElement();
            usage = child.getUsage();
            outputSize = 0;

            //
            // Create a byte array for the parameter and convert the Java objects to i5/OS data
            //
            bSupportedAtVRM = false;                                // @A2A
            bytes = null;                                           // @A2A
            switch (child.getNodeType())
            {
                case PcmlNodeType.STRUCT:
                    structNode = (PcmlStruct) child;
                    if ( structNode.isSupportedAtHostVRM() )        // @A1A
                    {                                               // @A1A
                        bSupportedAtVRM = true;                     // @A2A
                        passby = ProgramParameter.PASS_BY_REFERENCE; // @B1A
                        outputSize = structNode.getOutputsize(noDimensions);
                        if (usage == PcmlDocNode.INPUT || usage == PcmlDocNode.INPUTOUTPUT)
                        {
                            bytes = new byte[outputSize];           // @A2M
                            structNode.toBytes(bytes, 0, noDimensions);
                            // Dump the data stream if trace is turned on
                            PcmlMessageLog.traceParameter(getPath(), child.getNameForException(), bytes); // @A1C
                        }
                    }                                               // @A1A
                    break;
                case PcmlNodeType.DATA:
                    dataNode = (PcmlData) child;
                    if ( dataNode.isSupportedAtHostVRM() )          // @A1A
                    {                                               // @A1A
                        bSupportedAtVRM = true;                     // @A2A
                        passby = dataNode.getPassby();              // @B1A
                        outputSize = dataNode.getOutputsize(noDimensions);
                        if (usage == PcmlDocNode.INPUT || usage == PcmlDocNode.INPUTOUTPUT)
                        {
                            bytes = new byte[outputSize];           // @A2M
                            dataNode.toBytes(bytes, 0, noDimensions);
                            // Dump the data stream if trace is turned on
                            PcmlMessageLog.traceParameter(getPath(), child.getNameForException(), bytes); // @A1C
                        }
                    }                                               // @A1A
                    break;
                default:
                    throw new PcmlException(DAMRI.BAD_NODE_TYPE, new Object[] {new Integer(child.getNodeType()) , child.getNameForException()} );
            }

            // Parameter is supported at current host VRM
            if (bSupportedAtVRM)                                    // @A2C
            {                                                       // @A1A
                switch (usage)                                      // @A2A
                {                                                   // @A2A
                    case PcmlDocNode.INPUT:
                        // Create a input ProgramParameter using the byte array
                        childParms[childNbr] = new ProgramParameter( passby, bytes );      // @A2A @B1C
                        break;                                                     // @A2A

                    case PcmlDocNode.OUTPUT:                        // @A2A
                        // Create a output ProgramParameter using output size
                        childParms[childNbr] = new ProgramParameter( passby, outputSize ); // @A2A @B1C
                        break;                                                     // @A2A

                    case PcmlDocNode.INPUTOUTPUT:                   // @A2A
                        // Create a input/output ProgramParameter using byte array and output size
                        childParms[childNbr] = new ProgramParameter( passby, bytes, outputSize );   // @A2A @B1C
                        break;                                                              // @A2A
                }

                nbrSupportedParms++;                                // @A1A
            }                                                       // @A1A
            else                                                    // @A1A
            {                                                       // @A1A
                childParms[childNbr] = null;                        // @A1A
            }                                                       // @A1A

            childNbr++;
        }

        // Now build array of ProgramParameters for the supported parms
        supportedParms = new ProgramParameter[nbrSupportedParms];   // @A1A
        int supportedParmNbr = 0;                                   // @A1A
        for (childNbr = 0; childNbr < getNbrChildren(); childNbr++) // @A1A
        {                                                           // @A1A
            if ( childParms[childNbr] != null)                      // @A1A
            {                                                       // @A1A
                supportedParms[supportedParmNbr++] = childParms[childNbr]; // @A1A
            }                                                       // @A1A
        }                                                           // @A1A


        //
        // Set the path name and parameters for the target program
        //

        if ( isServiceProgram() )                                   // @B1A
        {                                                           // @B1A
            int rtnValType;                                         // @B1A
            if ( m_ReturnvalueStr != null && m_ReturnvalueStr.equals("integer") ) // @B1A
                rtnValType = ServiceProgramCall.RETURN_INTEGER;     // @B1A
            else                                                    // @B1A
                rtnValType = ServiceProgramCall.NO_RETURN_VALUE;    // @B1A

            m_pgmCall = new ServiceProgramCall(as400,
                                             getPath(),
                                             getEntrypoint(),
                                             rtnValType,
                                             supportedParms);       // @B1A
            if (getEpCcsid() != 0)                                  // @D1A
            {                                                       // @D1A
                try 
                {
                    ((ServiceProgramCall) m_pgmCall).setProcedureName(getEntrypoint(),   // @D1A
                                         getEpCcsid());             // @D1A
                }
                catch (PropertyVetoException e)                     // @D1A
                {}                                                  // @D1A

            }                                                       // @D1A
        }                                                           // @B1A
        else                                                        // @B1A
        {                                                           // @B1A
            m_pgmCall = new ProgramCall(as400,
                                        getPath(),
                                        supportedParms);            // @A1C @B1C
        }                                                           // @B1A

        // If threadsafety has been specified, set the attribute in the ProgramCall object
        if ( (m_ThreadsafeOverrideCalled) ||                        // @D2A
             (getThreadsafe() != null) )                            // @D2A
        {
        m_pgmCall.setThreadSafe(getThreadsafeOverride());             // @C6A
        }

        //
        // Call the target program
        //
        m_pgmRc = m_pgmCall.run();                                    // @B1A

        //
        // If the program signalled a message, save the message list.
        //
        if (m_pgmRc != true)
        {
            msgList = m_pgmCall.getMessageList();
            return m_pgmRc;
        }

        //
        // If the program is a service program, save the integer return
        // value and errno.
        //
        m_IntReturnValue = 0;                                       // @B1A
        m_Errno = 0;                                                // @B1A
        if ( isServiceProgram() )                                   // @B1A
        {                                                           // @B1A
            if ( m_ReturnvalueStr != null && m_ReturnvalueStr.equals("integer") ) // @B1A
            {                                                       // @B1A
                m_IntReturnValue = ((ServiceProgramCall) m_pgmCall).getIntegerReturnValue(); // @B1A
                m_Errno = ((ServiceProgramCall) m_pgmCall).getErrno(); // @B1A
            }                                                       // @B1A
        }                                                           // @B1A

        //
        // Parse the bytes from the output parameters
        // Data conversion is delayed until the values are requested.
        //
        String parseorder = getParseorder();
        if (parseorder != null)
        {
            StringTokenizer tokens = new StringTokenizer(parseorder);
            Vector orderVector = new Vector(tokens.countTokens());
            while (tokens.hasMoreTokens())
            {
                String token = tokens.nextToken();
                PcmlNode node = getRootNode().getElement(getQualifiedName() + "." + token);
                // Should never fail because parseorder attribute was checked after parsing
                if (node == null)
                {
                    throw new PcmlException(DAMRI.PARSEORDER_NOT_FOUND, new Object[] {makeQuotedAttr("parseorder", parseorder), token, getBracketedTagName(),  getNameForException()} );
                }
                else
                {
                    // Make sure node found is a child of this <program> element
                    // Should never fail because parseorder attribute was checked after parsing
                    if (node.getParent() != this)
                    {
                        throw new PcmlException(DAMRI.PARSEORDER_NOT_CHILD, new Object[] {makeQuotedAttr("parseorder", parseorder), token, getBracketedTagName(),  getNameForException()} );
                    }
                    else
                    {
                        // For <struct> elements check if it is supported at host VRM
                        if ( node instanceof PcmlStruct )           // @A1A
                        {                                           // @A1A
                            if ( ((PcmlStruct) node).isSupportedAtHostVRM() ) // @A1A
                            {                                       // @A1A
                                    orderVector.addElement(node);
                            }                                       // @A1A
                        }                                           // @A1A
                        // For <data> elements check if it is supported at host VRM
                        else if ( node instanceof PcmlData )        // @A1A
                        {                                           // @A1A
                            if ( ((PcmlData) node).isSupportedAtHostVRM() ) // @A1A
                            {                                       // @A1A
                                orderVector.addElement(node);       // @A1A
                            }                                       // @A1A
                        }                                           // @A1A
                        else                                        // @A1A
                        {                                           // @A1A
                            orderVector.addElement(node);           // @A1A
                        }                                           // @A1A
                    }
                }
            }
            children = orderVector.elements();
        }
        // parseorder= not specified, get enumeration of children in birth order
        else
        {
            children = getChildren();
        }

        // Process children to parse i5/OS data
        while (children.hasMoreElements())
        {
            child = (PcmlDocNode) children.nextElement();
            childNbr = child.getChildNbr();

            // If this child (parameter) has an output buffer
            // parse the bytes into its fields
            // Data is not converted to Java objects until the value is requested.
            if ( childParms[childNbr] instanceof ProgramParameter
              && childParms[childNbr].getOutputDataLength() > 0)    // @A1C
            {
                bytes = childParms[childNbr].getOutputData();       // @A1C

                PcmlMessageLog.traceParameter(getPath(), child.getNameForException(), bytes); // @A1C

                switch (child.getNodeType())
                {
                    case PcmlNodeType.STRUCT:
                        ((PcmlStruct) child).parseBytes(bytes, 0, offsetStack, new PcmlDimensions() );
                        break;
                    case PcmlNodeType.DATA:
                        ((PcmlData) child).parseBytes(bytes, 0, offsetStack, new PcmlDimensions() );
                        break;
                    default:
                        throw new PcmlException(DAMRI.BAD_NODE_TYPE, new Object[] {new Integer(child.getNodeType()) , child.getNameForException()} );
                }
            }

        }

        return true;
    }

    /**
    Returns the CCSID for this program program element.
    <p>
    The CCSID is saved at the beginning of the callProgram() method.
    This value is used for subsequent character conversion.
    This is especially helpful when the ProgramCallDocument is
    serialized with data and transported around the management
    central network.

    @return The integer CCSID for this program program element.

    */
    int getProgramCCSID()                                           // @C2A
    {                                                               // @C2A
        if (m_pgmCCSID == -1)                                       // @C2A
            return 65535;                                           // @C2A
        else                                                        // @C2A
            return m_pgmCCSID;                                      // @C2A
    }                                                               // @C2A

    /**
    Returns an "errno" value for this service program element.
    <p>
    This element must be defined as service program entrypoint.
    The value returned is the "errno" value resulting from the most recent
    call to the program. If the program has not been called, zero is returned.

    @return The integer "errno" value for this service program element.

    @exception PcmlException
               If an error occurs.
    */
    int getErrno() throws PcmlException                             // @B1A
    {                                                               // @B1A
        if ( isServiceProgram() )                                   // @B1A
        {                                                           // @B1A
            if ( m_ReturnvalueStr.equals("integer") )               // @B1A
                return m_Errno;                                     // @B1A

            throw new PcmlException(DAMRI.NOT_SERVICE_PGM, new Object[] { getBracketedTagName(), makeQuotedAttr("returnvalue", "integer"),  getBracketedTagName(), getNameForException()} ); // @B1A
        }                                                           // @B1A

        throw new PcmlException(DAMRI.NOT_SERVICE_PGM, new Object[] { getBracketedTagName(), makeQuotedAttr("entrypoint", ""),  getBracketedTagName(), getNameForException()} ); // @B1A
    }                                                               // @B1A

    /**
    Returns an int return value for this service program element.
    <p>
    This element must be defined as a service program entrypoint.
    The value returned is the integer return value from the most recent
    call to the program. If the program has not been called, zero is returned.

    @return The integer return value for this service program element.

    @exception PcmlException
               If an error occurs.
    */
    int getIntReturnValue() throws PcmlException                    // @B1A
    {                                                               // @B1A
        if ( isServiceProgram() )                                   // @B1A
        {                                                           // @B1A
            if ( m_ReturnvalueStr.equals("integer") )               // @B1A
                return m_IntReturnValue;                            // @B1A

            throw new PcmlException(DAMRI.NOT_SERVICE_PGM, new Object[] { getBracketedTagName(), makeQuotedAttr("returnvalue", "integer"),  getBracketedTagName(), getNameForException()} ); // @B1A
        }                                                           // @B1A

        throw new PcmlException(DAMRI.NOT_SERVICE_PGM, new Object[] { getBracketedTagName(), makeQuotedAttr("entrypoint", ""),  getBracketedTagName(), getNameForException()} ); // @B1A
    }                                                               // @B1A

    AS400Message[] getMessageList()
    {
        return msgList;
    }

    /**
    Returns whether or not this element is defined as a service program entrypoint.

    @return Returns true if this element is defined as a service program entrypoint.
    Returns false otherwise.

    */
    private boolean isServiceProgram()                              // @B1A
    {                                                               // @B1A
        return (getEntrypoint() != null);                           // @B1A
    }                                                               // @B1A
}
