///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PcmlDocument.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400DataType;
import com.ibm.as400.access.AS400Text;
import com.ibm.as400.access.AS400Bin2;
import com.ibm.as400.access.AS400UnsignedBin2;
import com.ibm.as400.access.AS400Bin4;
import com.ibm.as400.access.AS400UnsignedBin4;
import com.ibm.as400.access.AS400Bin8;                              // @C4A
import com.ibm.as400.access.AS400PackedDecimal;
import com.ibm.as400.access.AS400ZonedDecimal;
import com.ibm.as400.access.AS400Float4;
import com.ibm.as400.access.AS400Float8;
import com.ibm.as400.access.AS400ByteArray;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.AS400SecurityException;

import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;                                   // @C1A
import java.io.SequenceInputStream;

import java.net.UnknownHostException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.Stack;
import java.util.Vector;

/**
*/
class PcmlDocument extends PcmlDocRoot {
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = -8169008879805188674L;

    // New attributes should be added to the end of this array
    private static final String PCMLATTRIBUTES[] = {
        "version"
    };

    private static final int VERSION_1_ATTRIBUTE_COUNT = 1;
    
    private String m_docName;

    // The following attributes added for PCML v2.0
    private String m_Version;     // version=, string literal          @B1A

    private static AS400Bin2          m_Bin2   = new AS400Bin2();
    private static AS400UnsignedBin2  m_UBin2  = new AS400UnsignedBin2();
    private static AS400Bin4          m_Bin4   = new AS400Bin4();
    private static AS400UnsignedBin4  m_UBin4  = new AS400UnsignedBin4();
    private static AS400Bin8          m_Bin8   = new AS400Bin8();   // @C4A
    private static AS400Float4        m_Float4 = new AS400Float4();
    private static AS400Float8        m_Float8 = new AS400Float8();
    private static AS400PackedDecimal m_Packed_15_5 = new AS400PackedDecimal(15, 5);
    private static AS400ZonedDecimal  m_Zoned_15_5 = new AS400ZonedDecimal(15, 5);
    
    private static long   correllationID = 0;                       // @C8A

    // Transient data not stored durial serialization
    // These are a cache of the most common converters. 
    // NOTE: For AS400Text converters, the cached objects are 'thrown away'
    // when the AS/400 system object changes via setAs400(). This will cause
    // the new system (and CCSID) to be used to construct new converters.
    private transient AS400Text          m_Text_1  = null; // Create at run time
    private transient AS400Text          m_Text_10 = null; // Create at run time
    private transient AS400ByteArray     m_Byte_1  = null; // Create at run time
    private transient AS400ByteArray     m_Byte_2  = null; // Create at run time
    private transient AS400ByteArray     m_Byte_3  = null; // Create at run time
    private transient AS400ByteArray     m_Byte_4  = null; // Create at run time
    
    private AS400              m_as400;                             // @C1C
    private int                m_as400Vrm = -1;                     // @C1C

    private transient PcmlSpecificationException m_PcmlSpecificationException;
    private transient boolean m_bSerializingWithData = false;       // @C1A
    private transient long m_DeserializationTs = 0;                 // @C1A
    
    /**
    */
    PcmlDocument(PcmlAttributeList attrs, String docName)           // @C3A
    {            
        super();                                                    // @C5A
        m_PcmlSpecificationException = null;                        

        setNodeType(PcmlNodeType.DOCUMENT);                         // @C3A
        m_XmlAttrs = attrs;                                         // @C3A
        m_docName = docName;                                        
        
        // Set the pcml version number
        m_Version = getAttributeValue("version");                   
    }                                                               
    
    // All pcml document nodes support clone.
    // This is a deep clone in that the result is to 
    // clone an entire subtree. This method recursively 
    // clones its children.
    public Object clone()                                           // @C5A
    {                                                               // @C5A
        PcmlDocument doc = (PcmlDocument) super.clone();            // @C5A
        
        // Add the entire cloned tree of objects to the document hash table
        doc.addToHashtable(doc);                                    // @C5A

        return doc;                                                 // @C5A
    }                                                               // @C5A

	// Custom deserialization
	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException                  // @C1A
	{                                                               // @C1A
		// Set timestamp for deserialization (load) of the document
		// This timestamp is used to set all data and dimension 
		// timestamps during deserialization because the 
		// system clock is not necessarily synchronized with the
		// clock when the document was serialized (due to transfering
		// the serialized document to another system).
		// Classes in the document that maintain timestamps will use
		// this value as the current timestamp during readObject(). 
		// Currently, PcmlDataValues and PcmlDataVector use timestamps.
		// Note that timestamps are only an issue when serializing "with data".
		// The timestamp sensitive classes are not serialized when serializing
		// without data.
                m_DeserializationTs = getCorrellationID();                  // @C1A @C8C

		// Default deserialization
		in.defaultReadObject();                                     // @C1A
		
		if (m_as400 == null)                                        // @C1A
		    m_as400Vrm = -1;                                        // @C1A
		
		// Perform deserialization post-processing
		readObjectPostprocessing();
	}                                                               // @C1A

    /**
    */
    AS400 getAs400() 
    {
        return m_as400;
    }

    /**
    */
    long getDeserializationTimestamp()                              // @C1A
    {                                                               // @C1A
        return m_DeserializationTs;                                 // @C1A
    }                                                               // @C1A

   /**
    * Return the list of valid attributes for the data element.  
    **/
    String[] getAttributeList()                                 // @C6A
    {
        return PCMLATTRIBUTES;                                  // @C6A
    }

    /**
    */
    boolean isSerializingWithData()                                 // @C1A
    {                                                               // @C1A
        return m_bSerializingWithData;                              // @C1A
    }                                                               // @C1A

    /**
    */
    void setSerializingWithData(boolean b)                          // @C1A
    {                                                               // @C1A
        m_bSerializingWithData = b;                                 // @C1A
    }                                                               // @C1A

    /**
    */
    int getAs400VRM() throws PcmlException
    {
		if (m_as400Vrm == -1)
		{
			// Get the VRM of the host
			// Maybe should optimize this by caching initially by callProgram and 
			// making available to all children
			try
			{
				m_as400Vrm = getAs400().getVRM();
			}
	        catch (AS400SecurityException e)
	        {
	            throw new PcmlException(e);
	        }
	        catch (UnknownHostException e)
	        {
	            throw new PcmlException(e);
	        }
	        catch (IOException e)
	        {
	            throw new PcmlException(e);
	        }
		}

		return m_as400Vrm;
    }

    /**
    */
    String getDocName() 
    {
        return m_docName;
    }

    /**
    */
    String getVersion()                                             // @B1A
    {                                                               // @B1A
        return m_Version;                                           // @B1A
    }                                                               // @B1A
    
    /*    
    */
    synchronized void setAs400(AS400 sys) 
    {
        m_as400 = sys;
        m_as400Vrm = -1;

        // Release any CCSID sensitive data converters
        // The CCSID of this AS400 may be different from previous system
        m_Text_1 = null;                                            // @C1A
        m_Text_10 = null;                                           // @C1A
    }

    /*
    */
    protected AS400DataType getConverter(int dataType , int dataLength, int dataPrecision, int ccsid ) 
                                         throws PcmlException 
    {
        switch (dataType) 
        {

            case PcmlData.CHAR:
                // If the requested CCSID is not the same as 
                // the system's default host CCSID, always create
                // a new converter. I.E. We only cach converters 
                // with CCSIDs that match the system object.
                if (ccsid != m_as400.getCcsid())                    // @C2C
				{
                    return new AS400Text(dataLength, ccsid, m_as400);
				}

                switch (dataLength) 
                {
                    case 1:
                        if (m_Text_1 == null) 
                        {
                            m_Text_1 = new AS400Text(dataLength, ccsid, m_as400);
                        }
                        return m_Text_1;
                    case 10:
                        if (m_Text_10 == null) 
                        {
                            m_Text_10 = new AS400Text(dataLength, ccsid, m_as400);
                        }
                        return m_Text_10;
                    default:
                        return new AS400Text(dataLength, ccsid, m_as400);

                }

            case PcmlData.INT:
                if (dataLength == 2) 
                {
                    if (dataPrecision == 16)
                        return m_UBin2;
                    else // must be dataPrecision == 15
                        return m_Bin2;
                }
                else 
                if (dataLength == 4) 
                { 
                    if (dataPrecision == 32)
                        return m_UBin4;
                    else // must be dataPrecision == 31
                        return m_Bin4;
                }
                else                                                // @C4A
                { // must be datalength == 8                        // @C4A
                    return m_Bin8;                                  // @C4A
                }                                                   // @C4A

            case PcmlData.PACKED:
                if (dataLength == 15 && dataPrecision == 5)
                    return m_Packed_15_5;
                else
                    return new AS400PackedDecimal(dataLength, dataPrecision);

            case PcmlData.ZONED:
                if (dataLength == 15 && dataPrecision == 5)
                    return m_Zoned_15_5;
                else
                    return new AS400ZonedDecimal(dataLength, dataPrecision);

            case PcmlData.FLOAT:
                if (dataLength == 4) 
                {
                    return m_Float4;
                }
                else 
                { // must be datalength == 8
                    return m_Float8;
                }

            case PcmlData.BYTE: 
                switch (dataLength) 
                {
                    case 1:
                        if (m_Byte_1 == null) 
                        {
                            m_Byte_1 = new AS400ByteArray(dataLength);
                        }
                        return m_Byte_1;
                    case 2:
                        if (m_Byte_2 == null) 
                        {
                            m_Byte_2 = new AS400ByteArray(dataLength);
                        }
                        return m_Byte_2;
                    case 3:
                        if (m_Byte_3 == null) 
                        {
                            m_Byte_3 = new AS400ByteArray(dataLength);
                        }
                        return m_Byte_3;
                    case 4:
                        if (m_Byte_4 == null) 
                        {
                            m_Byte_4 = new AS400ByteArray(dataLength);
                        }
                        return m_Byte_4;
                    default:
                        return new AS400ByteArray(dataLength);

                }

            default:
                throw new PcmlException(DAMRI.BAD_DATA_TYPE, new Object[] {new Integer(dataType) , "*"} );

        } // END: switch (getDataType())
    }

    /**
    */
    synchronized boolean callProgram(String name)
						           throws AS400SecurityException, 
						                  ObjectDoesNotExistException, 
						                  InterruptedException, 
						                  ErrorCompletingRequestException, 
						                  IOException, 
						                  PcmlException
    {
        return getProgramNode(name).callProgram(m_as400);
    }

    /**
    Returns an "errno" value for the named service program element.
    <p>
    The named program element must be defined as service program entrypoint. 
    The value returned is the "errno" value resulting from the most recent
    call to the program. If the program has not been called, zero is returned.

    @return The integer "errno" value for the named service program element.

    @param name The name of the &lt;program&gt; element in the PCML document.
    @exception PcmlException
               If an error occurs.
    */
    synchronized int getErrno(String name) throws PcmlException     // @B1A
    {                                                               // @B1A
        return getProgramNode(name).getErrno();                     // @B1A
    }                                                               // @B1A

    /**
    Returns an int return value for the named service program element.
    <p>
    The named program element must be defined as service program entrypoint. 
    The value returned is the integer return value from the most recent
    call to the program. If the program has not been called, zero is returned.

    @return The integer return value for the named service program element.

    @param name The name of the &lt;program&gt; element in the PCML document.
    @exception PcmlException
               If an error occurs.
    */
    synchronized int getIntReturnValue(String name) throws PcmlException // @B1A
    {                                                               // @B1A
        return getProgramNode(name).getIntReturnValue();            // @B1A
    }                                                               // @B1A

    /**
    */
    synchronized int getIntValue(String name) throws PcmlException 
    {
        return getIntValue(name, new PcmlDimensions());
    }

    /**
    */
    synchronized int getIntValue(String name, PcmlDimensions indices) throws PcmlException 
    {
        Object value;
    
        value = getValue(name, indices);
        if (value instanceof String) 
        {
            return Integer.parseInt((String) value);
        }
        else if (value instanceof Number) 
        {
            return ((Number) value).intValue();
        }  
        else 
        {
            throw new PcmlException(DAMRI.STRING_OR_NUMBER, new Object[] {value.getClass().getName(), name} );
        }
    }

    /**
    */
    synchronized String getStringValue(String name, int type) throws PcmlException  // @C7A
    {
        return getStringValue(name, new PcmlDimensions(), type);                    // @C7A
    }

    /**
    Returns a long value that replaces a timestamp.  This value is used by
    PcmlDataValues to control data conversion, PcmlDataVector to keep track 
    of when the data dimension changes, and PcmlDocument for serialization.
    */
    static synchronized long getCorrellationID()                                           // @C8A
    {
        return ++correllationID;                                                    // @C8A
    }

    /**
    */
    synchronized String getStringValue(String name, PcmlDimensions indices, int type) 
        throws PcmlException                                                        // @C7A
    {
        return getDataNode(name).getStringValue(indices, type);                     // @C7A
    }

    /**
    */
    synchronized int getOutputsize(String name) throws PcmlException 
    {
        return getOutputsize(name, new PcmlDimensions());
    }

    /**
    */
    synchronized int getOutputsize(String name, PcmlDimensions indices) throws PcmlException 
    {
        PcmlNode node = (PcmlDocNode) getElement(name);

        if (node != null && node instanceof PcmlData) 
        {
            return ((PcmlData) node).getOutputsize(indices);
        }
        else 
        if (node != null && node instanceof PcmlStruct) 
        {
            return ((PcmlStruct) node).getOutputsize(indices);
        }
        else 
        {
            return 0;
        }
    }

    /**
    Returns the list of AS/400 messages returned from running the
    program. An empty list is returned if the program has not been run yet.

    @return The array of messages returned by the AS/400 for the program.
    */
    synchronized AS400Message[] getMessageList(String name) throws PcmlException 
    {
        return getProgramNode(name).getMessageList();
    }

    /**
    */
    synchronized Object getValue(String name) throws PcmlException 
    {
    	return getValue(name, new PcmlDimensions());
    }

    /**
    */
    synchronized Object getValue(String name, PcmlDimensions indices) throws PcmlException 
    {
        return getDataNode(name).getValue(indices);
    }

    /**
    */
    synchronized boolean isArray(String name) throws PcmlException 
    {
        return getDataNode(name).isArray();
    }

    /**
    */
    synchronized boolean isInArray(String name) throws PcmlException 
    {
        return getDataNode(name).isInArray();
    }

    // Set a named data value
    /**
    */
    synchronized void setValue(String name, Object value) throws PcmlException 
    {
    	setValue(name, value, new PcmlDimensions());
    }

    // Set a named data value using dimension indices
    /**
    */
    synchronized void setValue(String name, Object value, PcmlDimensions indices) throws PcmlException 
    {
        getDataNode(name).setValue(value, indices);
    }
    
    /**
    */
    synchronized void setStringValue(String name, String value, int type) throws PcmlException  // @C7A
    {
        setStringValue(name, value, new PcmlDimensions(), type);     // @C7A
    }

    /**
    */
    synchronized void setStringValue(String name, String value, PcmlDimensions indices, int type)   
        throws PcmlException                                            // @C7A
    {
        getDataNode(name).setStringValue(value, indices, type);  // @C7A
    }

    // Overrides the threadsafe= attribute
    void setThreadsafeOverride(String program, boolean threadsafe)   // @C6A
        throws PcmlException
    {                                                               // @C6A
        getProgramNode(program).setThreadsafeOverride(threadsafe);  // @C6A
    }                                                               // @C6A

    // gets the override of the threadsafe= attribute
    boolean getThreadsafeOverride(String program)                   // @C6A
        throws PcmlException
    {                                                               // @C6A
        return getProgramNode(program).getThreadsafeOverride();     // @C6A
    }                                                               // @C6A

    // Add a subtree to the document's hashtable.
    // This is called to complete the document cloneing process.
    void addToHashtable(PcmlDocNode newChild)                       // @C5A
    {                                                               // @C5A
        String qName; // Qualified name of child                    // @C5A
        Enumeration children;                                       // @C5A
        PcmlDocNode child;                                          // @C5A

        children = newChild.getChildren();                          // @C5A
        if (children == null)                                       // @C5A
            return;                                                 // @C5A

        while ( children.hasMoreElements() )                        // @C5A
        {                                                           // @C5A
            child = (PcmlDocNode) children.nextElement();           // @C5A

            qName = child.getQualifiedName();                       // @C5A
            if ( !qName.equals("") )                                // @C5A
            {                                                       // @C5A
                if ( this.containsElement(qName) )                  // @C5A
                {                                                   // @C5A
                    this.addPcmlSpecificationError(DAMRI.MULTIPLE_DEFINE, new Object[] {qName} ); // @C5A
                }                                                   // @C5A
                this.addElement(child);                             // @C5A
            }                                                       // @C5A

            // Recursively add all nodes in the tree to the hash table
            addToHashtable(child);                                  // @C5A
        }                                                           // @C5A

    }                                                               // @C5A

    /**
    */
    private PcmlProgram getProgramNode(String name)  throws PcmlException 
    {
        PcmlNode node;

        node = getElement(name);
        if (node instanceof PcmlProgram) 
        {
            return (PcmlProgram) node;
        }
        else
        {
            if (node == null)
                throw new PcmlException(DAMRI.ELEMENT_NOT_FOUND, new Object[] {name, "<program>"} );
            else
                throw new PcmlException(DAMRI.WRONG_ELEMENT_TYPE, new Object[] {name, "<program>"} );
        }
    }

    /**
    */
    private PcmlData getDataNode(String name)  throws PcmlException 
    {
        PcmlNode node;

        node = getElement(name);
        if (node instanceof PcmlData) 
        {
            return (PcmlData) node;
        }
        else
        {
            if (node == null)
                throw new PcmlException(DAMRI.ELEMENT_NOT_FOUND, new Object[] {name, "<data>"} );
            else
                throw new PcmlException(DAMRI.WRONG_ELEMENT_TYPE, new Object[] {name, "<data>"} );
        }
    }

    // Add a Pcml specification error to the list of errors.
    PcmlSpecificationException getPcmlSpecificationException() 
    {
        return m_PcmlSpecificationException;
    }

    // Add a Pcml specification error to the list of errors.
    void addPcmlSpecificationError(String key, Object[] args) 
    {
        // If an exception object doesn't exist, create one
        if (m_PcmlSpecificationException == null) 
        {
            m_PcmlSpecificationException = new PcmlSpecificationException(SystemResourceFinder.format(DAMRI.FAILED_TO_VALIDATE, new Object[] { m_docName }));
        }

        // Now add the message
        m_PcmlSpecificationException.addMessage(SystemResourceFinder.format(key, args));
    }
}
