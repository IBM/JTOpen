///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PcmlDocument.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
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
import com.ibm.as400.access.ProgramCall;

import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;                                   // @C1A
import java.io.SequenceInputStream;

import java.io.PrintWriter;                                         //@E1A
import java.io.OutputStream;                                        //@E1A
import com.ibm.as400.access.Trace;                                  //@E1A

import java.net.UnknownHostException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.Stack;
import java.util.Vector;

import com.ibm.as400.access.BinaryConverter;
//import sun.misc.BASE64Encoder;                                     //@E1A
//import sun.misc.BASE64Decoder;                                     //@E1A

/**
*/
class PcmlDocument extends PcmlDocRoot
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // Used for XPCML testing
    boolean compareSucceeded=true;


    static final long serialVersionUID = -8169008879805188674L;

    // New attributes should be added to the end of this array
    private static final String PCMLATTRIBUTES[] = {
        "version"
    };

    private static final int VERSION_1_ATTRIBUTE_COUNT = 1;

    private String m_docName;
    private String m_XsdName;     //@E1A


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
    // when the iSeries system object changes via setAs400(). This will cause
    // the new system (and CCSID) to be used to construct new converters.
    protected transient AS400Text          m_Text_1  = null; // Create at run time
    protected transient AS400Text          m_Text_10 = null; // Create at run time
    protected transient AS400ByteArray     m_Byte_1  = null; // Create at run time
    protected transient AS400ByteArray     m_Byte_2  = null; // Create at run time
    protected transient AS400ByteArray     m_Byte_3  = null; // Create at run time
    protected transient AS400ByteArray     m_Byte_4  = null; // Create at run time
                                     // @D0C: Made the above converters protected.

    private AS400              m_as400;                             // @C1C
    private int                m_as400Vrm = -1;                     // @C1C

    private transient PcmlSpecificationException m_PcmlSpecificationException;
    private transient boolean m_bSerializingWithData = false;       // @C1A
    private transient long m_DeserializationTs = 0;                 // @C1A
    private transient PcmlProgram m_pcmlProgram;


    // @E1A -- String constant for use in XPCML
    private static final String XMLNS_STRING =  " xmlns:xsi=" + "\"" + "http://www.w3.org/2001/XMLSchema-instance" + "\"" +
                          "\n" + "       xsi:noNamespaceSchemaLocation=";

    /**
    */
    PcmlDocument(PcmlAttributeList attrs, String docName)           // @C3A
    {
        super();                                                    // @C5A
        m_PcmlSpecificationException = null;

        setNodeType(PcmlNodeType.DOCUMENT);                         // @C3A
        m_XmlAttrs = attrs;                                         // @C3A
        m_docName = docName;
        m_XsdName="";                                               //@E1A

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
        m_pcmlProgram = getProgramNode(name);
        return m_pcmlProgram.callProgram(m_as400);
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
    Returns the list of iSeries system messages returned from running the
    program. An empty list is returned if the program has not been run yet.

    @return The array of messages returned by the server for the program.
    */
    synchronized AS400Message[] getMessageList(String name) throws PcmlException
    {
        return getProgramNode(name).getMessageList();
    }

    /**
     Returns the ProgramCall object that was used in the most recent invocation of {@link #callProgram() callProgram()}.
     @return The ProgramCall object; null if callProgram has not been called.
     **/
    ProgramCall getProgramCall()
    {
      return ( m_pcmlProgram == null ? null : m_pcmlProgram.getProgramCall() );
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

    // Set the path of the program to be called
    void setPath(String program, String path)                       // @D1A
        throws PcmlException
    {                                                               // @D1A
        getProgramNode(program).setPath(path);                      // @D1A
    }                                                               // @D1A

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

    /** @E1A -- NEW XPCML METHODS -- **/

    /**
     Generates XPCML representing the data contained in this program object.
     Throws a runtime exception if this object contains no data.

     @param outStream The output stream to which to write the text.
     @exception IOException  If an error occurs while writing the data.
     @exception XmlException  If an error occurs while processing XPCML.
     **/
     void generateXPCML(String pgmName,OutputStream outStream)
      throws IOException, XmlException
    {
      PrintWriter xmlFile = new PrintWriter(outStream);

      //@E1A -- New variables for XPCML.  These are used to control dimensioning
      // of a node.
      int num_dim =0;                     //@E1A
      int cur_dim=0;                   //@E1A
      int[] dimArray = {0,0,0,0,0,0,0,0,0,0};
      PcmlDimensions dims = new PcmlDimensions(dimArray);  //@E1A

      try
      {

        // Start XPCML stream by writing out xpcml tag
 //       xmlFile.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");         // @A1c
        xmlFile.println("<?xml version=\"1.0\" ?>");
        xmlFile.print("<xpcml version=" + "\"" + "4.0" +  "\"" );
        xmlFile.print(XMLNS_STRING);
        // Add .xsd file to end of <xpcml tag
        if (getXsdName() != "")
        {
           // Xsd file specified so use that
           xmlFile.println("'" + getXsdName() + "' >");
        }
        else
           xmlFile.println("'xpcml.xsd' >");


//        xmlFile.println(" xmlns:xsi=" + "\"" + "http://www.w3.org/2001/XMLSchema-instance" + "\"" +
//                          "\n" + "       xsi:noNamespaceSchemaLocation='xpcml.xsd'>");
        xmlFile.println();

        // Check if pgmName is null. If so, generate XPCML for entire PCML node tree. If not, just
        // generate XPCML for node tree associated with the given program name.
        if (pgmName == null)
           generateXPCML(this, this, xmlFile, "",num_dim, cur_dim, dims);
        else
        {
           // Get node for the program requested
           generateXPCML(this, getProgramNode(pgmName), xmlFile, "", num_dim, cur_dim, dims);
        }

        xmlFile.println("</xpcml>");

        if (xmlFile.checkError())  // Note: This flushes the stream.
        {
          Trace.log(Trace.ERROR, "Error when writing PCML to OutputStream.");
        }

      }
      finally {
        xmlFile.close(); // Note: close() flushes the stream first.
      }
    }


    /**
     Generates XPCML representing the data contained in the specified node.
     Throws a runtime exception if this object contains no data.

     @param pcmlDocNode The root node of the tree.
     @param node The node to generate PCML for.
     @param writer The writer to which to write the text.
     @param indent The indentation with which to prepend the generated XML.
     @exception IOException  If an error occurs while writing the data.
     @exception XmlException  If an error occurs while processing XPCML.
     **/
    private void generateXPCML(PcmlDocument pcmlDocNode, PcmlDocNode node, PrintWriter writer, String indent, int num_dimensions, int current_dimension, PcmlDimensions dimensions)
      throws IOException, XmlException
    {
        int tempInt;
        String lastTag="";
        boolean tagDone=false;

        // Define variable to be used to get count values of nodes
        PcmlDimensions dim = new PcmlDimensions();
        dim.add(0);


        // Reset all dimensions if this is a <program> or <pcml> node
        if (node.getNodeType() == PcmlNodeType.PROGRAM || node.getNodeType() == PcmlNodeType.DOCUMENT)
        {
            for (int i = 0; i < dimensions.size(); ++i)
            {
               dimensions.set(i,0);
            }
            current_dimension = 0;
            num_dimensions = 0;
        }

        // Check if this is the first time this node has been processed. The initial value for CountReps
        // is -10.  Get the count value for the node to determine how many times the node needs to be processed.
        if (node.getCountReps() == -10)
        {
           int nodeType = node.getNodeType();
           if (nodeType == PcmlNodeType.DATA && ((PcmlData) node).getXPCMLCount(dim) != 0)
           {
              // If the node is a data node and it has a count set tempInt to the count
              tempInt = ( (PcmlData) node).getXPCMLCount(dim);
            }
            else if (nodeType== PcmlNodeType.STRUCT && ((PcmlStruct) node).getXPCMLCount(dim) != 0)
            {
                 // If the node is a struct node and it has a count set tempInt to the count
                 tempInt = ( (PcmlStruct) node).getXPCMLCount(dim);
            }
            else
                 tempInt = 1;

            node.setCountReps(tempInt);      // Set the number of reps for the node.

            // set current dimension
            dimensions.set(current_dimension, 0);
        }

        // Start the tag.  Transform a <data tag to the correct parm tag
        if ( node.getNodeType() == PcmlNodeType.DATA)
        {
           int dataType = ((PcmlData) node).getDataType();
           switch( dataType ) {
               case PcmlData.CHAR:
                  if ( ((PcmlData) node).getXPCMLCount(dim) > 0 )
                  {
                    if (dimensions.at(current_dimension)== 0)
                    {
                       // Check if this is a user defined element
                       if (node.getCondensedName() != "")
                       {
                          writer.print(indent);
                          writer.print("<"+ node.getCondensedName());
                          lastTag="arrayOfStringParm";
                       }
                       else
                       {
                          writer.print(indent);
                          writer.print("<arrayOfStringParm");
                          lastTag="arrayOfStringParm";
                       }
                    }
                  }
                  else
                  {
                     // Check if this is a user defined element
                     if (node.getCondensedName() != "")
                     {
                        writer.print(indent);
                        writer.print("<"+ node.getCondensedName());
                        lastTag=node.getCondensedName();
                     }
                     else
                     {
                       writer.print(indent);
                       writer.print("<stringParm");
                       lastTag="stringParm";
                     }
                  }
                  break;
               case PcmlData.STRUCT:
                  if (  ((PcmlData) node).getXPCMLCount(dim) > 0 )
                  {
                    if (dimensions.at(current_dimension)== 0)
                    {
                       // Check if this is a user defined element
                       if (node.getCondensedName() != "")
                       {
                          writer.print(indent);
                          writer.print("<"+ node.getCondensedName());
                          lastTag="arrayOfStructParm";
                       }
                       else
                       {
                          writer.print(indent);
                          writer.print("<arrayOfStructParm");
                          lastTag="arrayOfStructParm";
                       }
                    }
                  }
                  else
                  {
                     // Check if this is a user defined element
                     if (node.getCondensedName() != "")
                     {
                        writer.print(indent);
                        writer.print("<"+ node.getCondensedName());
                        lastTag=node.getCondensedName();
                     }
                     else
                     {
                        writer.print(indent);
                        writer.print("<structParm");
                        lastTag="structParm";
                     }
                  }
                  break;
               case PcmlData.BYTE:
                  if (  ((PcmlData) node).getXPCMLCount(dim) > 0 )
                  {
                    if (dimensions.at(current_dimension)== 0)
                    {
                       // Check if this is a user defined element
                       if (node.getCondensedName() != "")
                       {
                          writer.print(indent);
                          writer.print("<"+ node.getCondensedName());
                          lastTag="arrayOfHexBinaryParm";
                       }
                       else
                       {
                          writer.print(indent);
                          writer.print("<arrayOfHexBinaryParm");
                          lastTag="arrayOfHexBinaryParm";
                       }
                    }
                  }
                  else
                  {
                     // Check if this is a user defined element
                     if (node.getCondensedName() != "")
                     {
                        writer.print(indent);
                        writer.print("<"+ node.getCondensedName());
                        lastTag=node.getCondensedName();
                     }
                     else
                     {
                        writer.print(indent);
                        writer.print("<hexBinaryParm");
                        lastTag="hexBinaryParm";
                     }
                  }

                  break;
               case PcmlData.INT:
                  if (node.getAttributeValue("length").equals("4"))
                  {
                     if (node.getAttributeValue("precision") != null && node.getAttributeValue("precision").equals("32"))
                     {
                        if (  ((PcmlData) node).getXPCMLCount(dim) > 0 )
  //                      if (node.getAttributeValue("count") != null )
                        {
                           if (dimensions.at(current_dimension)== 0)
                           {
                               // Check if this is a user defined element
                               if (node.getCondensedName() != "")
                               {
                                  writer.print(indent);
                                  writer.print("<"+ node.getCondensedName());
                                  lastTag="arrayOfUnsignedIntParm";
                               }
                               else
                               {
                                  writer.print(indent);
                                  writer.print("<arrayOfUnsignedIntParm");
                                  lastTag="arrayOfUnsignedIntParm";
                               }
                           }
                        }
                        else
                        {
                           // Check if this is a user defined element
                           if (node.getCondensedName() != "")
                           {
                              writer.print(indent);
                              writer.print("<"+ node.getCondensedName());
                              lastTag=node.getCondensedName();
                           }
                           else
                           {
                              writer.print(indent);
                              writer.print("<unsignedIntParm");
                              lastTag="unsignedIntParm";
                           }
                        }
                    }
                    else
                     {
                        if ( ((PcmlData) node).getXPCMLCount(dim) > 0 )
                        {
                           if (dimensions.at(current_dimension)== 0)
                           {
                              // Check if this is a user defined element
                              if (node.getCondensedName() != "")
                              {
                                 writer.print(indent);
                                 writer.print("<"+ node.getCondensedName());
                                 lastTag="arrayOfIntParm";
                              }
                              else
                              {
                                  writer.print(indent);
                                  writer.print("<arrayOfIntParm");
                                  lastTag="arrayOfIntParm";
                              }
                           }
                        }
                        else
                        {
                           // Check if this is a user defined element
                           if (node.getCondensedName() != "")
                           {
                              writer.print(indent);
                              writer.print("<"+ node.getCondensedName());
                              lastTag=node.getCondensedName();
                           }
                           else
                           {
                              writer.print(indent);
                              writer.print("<intParm");
                              lastTag="intParm";
                           }
                        }
                    }
                  }
                  else if (node.getAttributeValue("length").equals("2") )
                  {
                     if (node.getAttributeValue("precision") != null && node.getAttributeValue("precision").equals("16"))
                     {
                        if (  ((PcmlData) node).getXPCMLCount(dim) > 0 )
                        {
                           if (dimensions.at(current_dimension)== 0)
                           {
                              // Check if this is a user defined element
                              if (node.getCondensedName() != "")
                              {
                                 writer.print(indent);
                                 writer.print("<"+ node.getCondensedName());
                                 lastTag="arrayOfUnsignedShortParm";
                              }
                              else
                              {
                                  writer.print(indent);
                                  writer.print("<arrayOfUnsignedShortParm");
                                  lastTag="arrayOfUnsignedShortParm";
                              }
                           }
                        }
                        else
                        {
                          // Check if this is a user defined element
                          if (node.getCondensedName() != "")
                          {
                              writer.print(indent);
                              writer.print("<"+ node.getCondensedName());
                              lastTag=node.getCondensedName();
                          }
                          else
                          {
                              writer.print(indent);
                              writer.print("<unsignedShortParm");
                              lastTag="unsignedShortParm";
                          }
                        }
                    }
                    else
                     {
                        if (  ((PcmlData) node).getXPCMLCount(dim) > 0 )
                        {
                           if (dimensions.at(current_dimension)== 0)
                           {
                              // Check if this is a user defined element
                              if (node.getCondensedName() != "")
                              {
                                 writer.print(indent);
                                 writer.print("<"+ node.getCondensedName());
                                 lastTag="arrayOfShortParm";
                              }
                              else
                              {
                                 writer.print(indent);
                                 writer.print("<arrayOfShortParm");
                                 lastTag="arrayOfShortParm";
                              }
                           }
                        }
                        else
                        {
                           // Check if this is a user defined element
                           if (node.getCondensedName() != "")
                           {
                               writer.print(indent);
                               writer.print("<"+ node.getCondensedName());
                               lastTag=node.getCondensedName();
                           }
                           else
                           {
                               writer.print(indent);
                               writer.print("<shortParm");
                               lastTag="shortParm";
                           }
                        }
                    }
                  }
                  else if (node.getAttributeValue("length").equals("8"))
                  {
                      if (  ((PcmlData) node).getXPCMLCount(dim) > 0 )
                      {
                        if (dimensions.at(current_dimension)== 0)
                        {
                            // Check if this is a user defined element
                            if (node.getCondensedName() != "")
                            {
                                writer.print(indent);
                                writer.print("<"+ node.getCondensedName());
                                lastTag="arrayOfLongParm";
                            }
                            else
                            {
                                writer.print(indent);
                                writer.print("<arrayOfLongParm");
                                lastTag="arrayOfLongParm";
                            }
                        }
                      }
                      else
                      {
                        // Check if this is a user defined element
                        if (node.getCondensedName() != "")
                        {
                            writer.print(indent);
                            writer.print("<"+ node.getCondensedName());
                            lastTag=node.getCondensedName();
                        }
                        else
                        {
                            writer.print(indent);
                            writer.print("<longParm");
                            lastTag="longParm";
                        }
                      }
                  }
                  break;
               case PcmlData.FLOAT:
                  if (node.getAttributeValue("length").equals("4"))
                  {
                      if (  ((PcmlData) node).getXPCMLCount(dim) > 0 )
                      {
                        if (dimensions.at(current_dimension)== 0)
                        {
                          // Check if this is a user defined element
                          if (node.getCondensedName() != "")
                          {
                              writer.print(indent);
                              writer.print("<"+ node.getCondensedName());
                              lastTag="arrayOfFloatParm";
                          }
                          else
                          {
                              writer.print(indent);
                              writer.print("<arrayOfFloatParm");
                              lastTag="arrayOfFloatParm";
                          }
                        }
                      }
                      else
                      {
                        // Check if this is a user defined element
                        if (node.getCondensedName() != "")
                        {
                           writer.print(indent);
                           writer.print("<"+ node.getCondensedName());
                           lastTag=node.getCondensedName();
                        }
                        else
                        {
                           writer.print(indent);
                           writer.print("<floatParm");
                           lastTag="floatParm";
                        }
                      }
                  }
                  else if (node.getAttributeValue("length").equals("8"))
                  {
                     if (  ((PcmlData) node).getXPCMLCount(dim) > 0 )
                      {
                        if (dimensions.at(current_dimension)== 0)
                        {
                          // Check if this is a user defined element
                          if (node.getCondensedName() != "")
                          {
                              writer.print(indent);
                              writer.print("<"+ node.getCondensedName());
                              lastTag="arrayOfDoubleParm";
                          }
                          else
                          {
                             writer.print(indent);
                             writer.print("<arrayOfDoubleParm");
                             lastTag="arrayOfDoubleParm";
                          }
                        }
                      }
                      else
                      {
                        // Check if this is a user defined element
                        if (node.getCondensedName() != "")
                        {
                            writer.print(indent);
                            writer.print("<"+ node.getCondensedName());
                            lastTag=node.getCondensedName();
                        }
                        else
                        {
                          writer.print(indent);
                          writer.print("<doubleParm");
                          lastTag="doubleParm";
                        }
                      }
                  }
                  break;
               case PcmlData.ZONED:
                  if (  ((PcmlData) node).getXPCMLCount(dim) > 0 )
                  {
                    if (dimensions.at(current_dimension)== 0)
                    {
                        // Check if this is a user defined element
                        if (node.getCondensedName() != "")
                        {
                            writer.print(indent);
                            writer.print("<"+ node.getCondensedName());
                            lastTag="arrayOfZonedDecimalParm";
                        }
                        else
                        {
                            writer.print(indent);
                            writer.print("<arrayOfZonedDecimalParm");
                            lastTag="arrayOfZonedDecimalParm";
                        }
                    }
                  }
                  else
                  {
                     // Check if this is a user defined element
                     if (node.getCondensedName() != "")
                     {
                         writer.print(indent);
                         writer.print("<"+ node.getCondensedName());
                         lastTag=node.getCondensedName();
                     }
                     else
                     {
                         writer.print(indent);
                         writer.print("<zonedDecimalParm");
                         lastTag="zonedDecimalParm";
                     }
                  }
                  break;
               case PcmlData.PACKED:
                  if (  ((PcmlData) node).getXPCMLCount(dim) > 0 )
                  {
                    if (dimensions.at(current_dimension)== 0)
                    {
                      // Check if this is a user defined element
                      if (node.getCondensedName() != "")
                      {
                          writer.print(indent);
                          writer.print("<"+ node.getCondensedName());
                          lastTag="arrayOfPackedDecimalParm";
                      }
                      else
                      {
                          writer.print(indent);
                          writer.print("<arrayOfPackedDecimalParm");
                          lastTag="arrayOfPackedDecimalParm";
                      }
                    }
                  }
                  else
                  {
                    // Check if this is a user defined element
                    if (node.getCondensedName() != "")
                    {
                        writer.print(indent);
                        writer.print("<"+ node.getCondensedName());
                        lastTag=node.getCondensedName();
                    }
                    else
                    {
                        writer.print(indent);
                        writer.print("<packedDecimalParm");
                        lastTag="packedDecimalParm";
                    }
                  }
                  break;
               default:
                  throw new PcmlException(DAMRI.BAD_DATA_TYPE, new Object[] {new Integer(dataType) , "*"} );
            }
        } else if (node.getNodeType()== PcmlNodeType.STRUCT)
          {
            if (  ((PcmlStruct) node).getXPCMLCount(dim) > 0 )
            {
               if (dimensions.at(current_dimension)== 0)
               {
                  // Check if this is a user defined element
                  if (node.getCondensedName() != "")
                  {
                      writer.print(indent);
                      writer.print("<"+ node.getCondensedName());
                      lastTag="arrayOfStruct";
                  }
                  else
                  {
                     writer.print(indent);
                     writer.print("<arrayOfStruct");
                     lastTag="arrayOfStruct";
                  }
               }
            }
            else
            {
               // Check if this is a user defined element
               if (node.getCondensedName() != "")
               {
                   writer.print(indent);
                   writer.print("<"+ node.getCondensedName());
                   lastTag=node.getCondensedName();
               }
               else
               {
                  writer.print(indent);
                  writer.print("<struct");
                  lastTag="struct";
               }
            }
         }
        else if (node.getNodeType() == PcmlNodeType.PROGRAM)
        {
             writer.println();
             writer.print(indent + "<program");
             lastTag="program";
        }
        else if (node.getNodeType() == PcmlNodeType.DOCUMENT)
        {
        }
        else
        {
             // Should never get here
//           writer.print(indent + "<" + node.getTagName());
//           lastTag = node.getTagName();
        }

        // Print out any attributes that have non-null values.
        // QUESTION-- NEED TO DECIDE WHAT TO DO WITH PRECISION FOR UNSIGNED NUMERICS
        String[] attrs = node.getAttributeList();
        // Check if user-defined node.  If so, don't print out attributes
        if (node.getCondensedName() == "" || node.getIsExtendedType())
        {
           if ( node.getNodeType() == PcmlNodeType.DATA  && dimensions.at(current_dimension) > 0)
           {
               // Don't output attributes working on an array element entry
           }
           else if (node.getNodeType() == PcmlNodeType.STRUCT && dimensions.at(current_dimension) > 0)
           {
           }
           else
           {
             for (int i=0; i<attrs.length; i++)
             {
                if (node.getAttributeValue(attrs[i]) != null)
                {

                   // Convert the attributes over to XPCML attributes
                   if (attrs[i].equals("usage"))
                   {
                       if (node.getAttributeValue(attrs[i]).equals("input"))
                           writer.print(" " + "passDirection"  + "=\"" + "in" + "\"");
                       else if (node.getAttributeValue(attrs[i]).equals("output"))
                           writer.print(" " + "passDirection"  + "=\"" + "out" + "\"");
                       else if (node.getAttributeValue(attrs[i]).equals("inputoutput"))
                           writer.print(" " + "passDirection"  + "=\"" + "inout" + "\"");
                   } else if (attrs[i].equals("passby"))
                   {
                       writer.print(" " + "passMode" + "=\"" + node.getAttributeValue(attrs[i]) + "\"");
                   } else if (attrs[i].equals("type"))
                      // don't copy
                   {
                   } else if (attrs[i].equals("length") && node.getAttributeValue("type") != null
                        && ( node.getAttributeValue("type").equals("int") || node.getAttributeValue("type").equals("float")))
                   {
                        // don't copy length parm for ints or floats
                   }
                   else if (attrs[i].equals("length") && node.getAttributeValue("type") != null
                        && node.getAttributeValue("type").equals("byte"))
                   {
                        // copy length to totalBytes
                        writer.print(" " + "totalBytes"  + "=\"" + node.getAttributeValue(attrs[i]) + "\"");
                   }
                   else if (attrs[i].equals("length") && node.getAttributeValue("type") != null
                        && (node.getAttributeValue("type").equals("zoned") || node.getAttributeValue("type").equals("packed")))
                   {
                        // copy length to totalDigits
                        writer.print(" " + "totalDigits"  + "=\"" + node.getAttributeValue(attrs[i]) + "\"");
                   }
                   else if (attrs[i].equals("precision") && node.getAttributeValue("type") != null
                        && (node.getAttributeValue("type").equals("zoned") || node.getAttributeValue("type").equals("packed")))
                   {
                        // copy precision to fractionDigits
                        writer.print(" " + "fractionDigits"  + "=\"" + node.getAttributeValue(attrs[i]) + "\"");
                   }
                   else if (attrs[i].equals("version"))
                   {
                      // don't copy
                   } else if (attrs[i].equals("init"))
                   {
                      // Don't copy
                   }
                   else if (attrs[i].equals("precision"))
                   {
                      // Don't copy
                   }
                   else if (attrs[i].equals("offsetfrom"))
                   {
                        // copy offsetfrom to offsetFrom
                        writer.print(" " + "offsetFrom"  + "=\"" + node.getAttributeValue(attrs[i]) + "\"");
                   }
                     else if (attrs[i].equals("outputsize"))
                   {
                        // copy outputsize to outputSize
                        writer.print(" " + "outputSize"  + "=\"" + node.getAttributeValue(attrs[i]) + "\"");
                   }
                     else if (attrs[i].equals("bidistringtype"))
                   {
                        // copy bidistringtype to bidiStringType
                        writer.print(" " + "bidiStringType"  + "=\"" + node.getAttributeValue(attrs[i]) + "\"");
                   }
                     else if (attrs[i].equals("chartype"))
                   {
                        // copy chartype to bytesPerChar
                        writer.print(" " + "bytesPerChar"  + "=\"" + node.getAttributeValue(attrs[i]) + "\"");
                   }
                     else if (attrs[i].equals("parseorder"))
                   {
                        // copy parseorder to parseOrder
                        writer.print(" " + "parseOrder"  + "=\"" + node.getAttributeValue(attrs[i]) + "\"");
                   }
                     else if (attrs[i].equals("entrypoint"))
                   {
                        // copy entrypoint to entryPoint
                        writer.print(" " + "entryPoint"  + "=\"" + node.getAttributeValue(attrs[i]) + "\"");
                   }
                     else if (attrs[i].equals("threadsafe"))
                   {
                        // copy threadsafe to threadSafe
                        writer.print(" " + "threadSafe"  + "=\"" + node.getAttributeValue(attrs[i]) + "\"");
                   }
                     else if (attrs[i].equals("returnvalue"))
                   {
                        // copy returnvalue to returnValue
                        writer.print(" " + "returnValue"  + "=\"" + node.getAttributeValue(attrs[i]) + "\"");
                   }
                   else
                   {
                        writer.print(" " + attrs[i] + "=\"" + node.getAttributeValue(attrs[i]) + "\"");
                   }
                }
             }
          }
       }

        // Reduce the count for this node by 1.
        if (node.getCountReps() != 0)
        {
            node.setCountReps(node.getCountReps() - 1);
        }

      if (node.hasChildren())
      {

          // Finish the current tag.
          if (node.getNodeType() != PcmlNodeType.DOCUMENT && dimensions.at(current_dimension)==0)
          {
            writer.println(">");
          }

        // Add struct_i tag for struct arrays

        if (node.getNodeType() == PcmlNodeType.DATA && ((PcmlData)node).getDataType()==PcmlData.STRUCT && ((PcmlData) node).getXPCMLCount(dim) > 0 )
        {
             if (dimensions.at(current_dimension) > 0)
              {
                  writer.println(indent+ "  <struct_i index=" + "\"" + dimensions.at(current_dimension) + "\"" +  ">");
              }
              else
              {
                  writer.println(indent+ "  <struct_i index=" + "\""+ dimensions.at(current_dimension) + "\"" + ">" );
              }
              lastTag="struct_i";
        }

        if (node.getNodeType() == PcmlNodeType.STRUCT && ((PcmlStruct) node).getXPCMLCount(dim) > 0 )
        {
             if (dimensions.at(current_dimension) > 0)
              {
                  writer.println(indent+ "  <struct_i index=" + "\"" + dimensions.at(current_dimension) + "\""+ ">");
              }
              else
              {
                  writer.println(indent+ "  <struct_i index=" + "\"" + dimensions.at(current_dimension) + "\"" + ">" );
              }
              lastTag="struct_i";
        }


        // if node = "program" write out "paramterList" tag
        if ( node.getNodeType() == PcmlNodeType.PROGRAM)
        {
             writer.println(indent + "<parameterList>");
        }

        Enumeration children = node.getChildren();

        // Process the children of the current node
        while (children.hasMoreElements())
        {
          PcmlDocNode child = (PcmlDocNode) children.nextElement();

          // Reset all dimensions if this is <program> tag
          if (child.getNodeType() == PcmlNodeType.PROGRAM)
          {
             for (int i = 0; i < dimensions.size(); ++i)
             {
               dimensions.set(i, 0);
             }
             current_dimension = 0;
             num_dimensions = 0;
          }

          // Reset all dimensions if this is a <struct> tag without a parent
          if (child.getNodeType() == PcmlNodeType.STRUCT && child.getParent().getName().trim().length()==0 )
          {
             for (int i = 0; i < dimensions.size(); ++i)
             {
               dimensions.set(i, 0);
             }
             current_dimension = 0;
             num_dimensions = 0;
          }

          // Increase dimensions by 1 if this is the first child, i.e., we've gone down the tree
          if (child.getChildNbr()==0 &&
             ( (node.getNodeType() == PcmlNodeType.DATA && ((PcmlData) node).getXPCMLCount(dim) > 0 ) ||
               (node.getNodeType() == PcmlNodeType.STRUCT && ((PcmlStruct) node).getXPCMLCount(dim) > 0 ) ) )
          {
             num_dimensions++;
             current_dimension++;
             if (num_dimensions > 9)
                dimensions.add(0);
          }

          // Call generateXPCML to generate XPCML for the child
          generateXPCML(pcmlDocNode,child, writer, indent+"    ", num_dimensions, current_dimension, dimensions);
        }  // end while more children


        // Write the end tag.
        // If node = "program" write out "paramterList" tag
        if ( node.getNodeType() == PcmlNodeType.PROGRAM)
             writer.println(indent + "</parameterList>");
        if ( node.getNodeType() == PcmlNodeType.DATA)
        {
           int dataType = ((PcmlData) node).getDataType();
           writer.print(indent);
           switch( dataType ) {
               case PcmlData.CHAR:
                  if (  ((PcmlData) node).getXPCMLCount(dim) <= 0 )
                  {
                     if (node.getCondensedName() != "")
                        writer.print("</" + node.getCondensedName() + ">");
                     else
                        writer.print("</stringParm>");
                  }
                  else
                  {
                     // Processing multiples of the node.  Should end with </i
                     writer.println( "  </i>");
                     if (node.getCountReps() == 0)
                     {
                        if (node.getCondensedName() != "")
                           writer.print("</" + node.getCondensedName() + ">");
                        else
                        {
                           // All done with elements. Add end array tag
                           writer.println( "</arrayOfStringParm>");
                        }
                     }
                  }
                  break;
               case PcmlData.STRUCT:
                  if (  ((PcmlData) node).getXPCMLCount(dim) <= 0 )
                  {
                     if (node.getCondensedName() != "")
                        writer.print("</" + node.getCondensedName() + ">");
                     else
                        writer.println("</structParm>");
                  }
                  else
                  {
                     // Processing multiples of the node.  Should end with </struct_i
                     writer.println( "  </struct_i>");
                     if (node.getCountReps() == 0)
                     {
                        if (node.getCondensedName() != "")
                           writer.print(indent+"</" + node.getCondensedName() + ">");
                        else
                        {
                           // All done with elements. Add end array tag
                           writer.println(indent+"</arrayOfStructParm>");
                        }
                     }
                  }
                  break;
               case PcmlData.BYTE:
                  if ( ((PcmlData) node).getXPCMLCount(dim) <= 0 )
                  {
                     if (node.getCondensedName() != "")
                        writer.print("</" + node.getCondensedName() + ">");
                     else
                        writer.print("</hexBinaryParm>");
                  }
                  else
                  {
                     // Processing multiples of the node.  Should end with </i
                     writer.println( "  </i>");
                     if (node.getCountReps() == 0)
                     {
                        if (node.getCondensedName() != "")
                           writer.print("</" + node.getCondensedName() + ">");
                        else
                        {
                           // All done with elements. Add end array tag
                           writer.println( "</arrayOfHexBinaryParm>");
                        }
                     }
                  }
                  break;
               case PcmlData.INT:
                  if (node.getAttributeValue("length").equals("4") &&  node.getAttributeValue("precision") != null && node.getAttributeValue("precision").equals("32") )
                  {
                      if ( ((PcmlData) node).getXPCMLCount(dim) <= 0 )
                      {
                         if (node.getCondensedName() != "")
                            writer.print("</" + node.getCondensedName() + ">");
                         else
                            writer.print("</unsignedIntParm>");
                      }
                      else
                      {
                         // Processing multiples of the node.  Should end with </i
                         writer.println( "  </i>");
                         if (node.getCountReps() == 0)
                         {
                            if (node.getCondensedName() != "")
                               writer.print("</" + node.getCondensedName() + ">");
                            else
                            {
                               // All done with elements. Add end array tag
                               writer.println( "</arrayOfUnsignedIntParm>");
                            }
                         }
                      }
                  }
                  else if (node.getAttributeValue("length").equals("4"))
                  {
                      if (  ((PcmlData) node).getXPCMLCount(dim) <= 0 )
                      {
                         if (node.getCondensedName() != "")
                            writer.print("</" + node.getCondensedName() + ">");
                         else
                            writer.print("</intParm>");
                      }
                      else
                      {
                         // Processing multiples of the node.  Should end with </i
                         writer.println( "  </i>");
                         if (node.getCountReps() == 0)
                         {
                            if (node.getCondensedName() != "")
                               writer.print("</" + node.getCondensedName() + ">");
                            else
                            {
                               // All done with elements. Add end array tag
                               writer.println( "</arrayOfIntParm>");
                            }
                         }
                      }
                  }
                  else if (node.getAttributeValue("length").equals("2") && node.getAttributeValue("precision") != null && node.getAttributeValue("precision").equals("16"))
                  {
                      if (  ((PcmlData) node).getXPCMLCount(dim) <= 0 )
                      {
                         if (node.getCondensedName() != "")
                            writer.print("</" + node.getCondensedName() + ">");
                         else
                            writer.print("</unsignedShortParm>");
                      }
                      else
                      {
                         // Processing multiples of the node.  Should end with </i
                         writer.println( "  </i>");
                         if (node.getCountReps() == 0)
                         {
                            if (node.getCondensedName() != "")
                               writer.print("</" + node.getCondensedName() + ">");
                            else
                            {
                               // All done with elements. Add end array tag
                               writer.println( "</arrayOfUnsignedShortParm>");
                            }
                         }
                      }
                  }
                  else if (node.getAttributeValue("length").equals("2"))
                  {
                      if (  ((PcmlData) node).getXPCMLCount(dim) <= 0 )
                      {
                         if (node.getCondensedName() != "")
                            writer.print("</" + node.getCondensedName() + ">");
                         else
                            writer.print("</shortParm>");
                      }
                      else
                      {
                         // Processing multiples of the node.  Should end with </i
                         writer.println( "  </i>");
                         if (node.getCountReps() == 0)
                         {
                           if (node.getCondensedName() != "")
                               writer.print("</" + node.getCondensedName() + ">");
                            else
                            {
                               // All done with elements. Add end array tag
                               writer.println( "</arrayOfShortParm>");
                            }
                         }
                      }
                  }
                  else if (node.getAttributeValue("length").equals("8"))
                  {
                      if (  ((PcmlData) node).getXPCMLCount(dim) <= 0 )
                      {
                         if (node.getCondensedName() != "")
                            writer.print("</" + node.getCondensedName() + ">");
                         else
                            writer.print("</longParm>");
                      }
                      else
                      {
                         // Processing multiples of the node.  Should end with </i
                         writer.println( "  </i>");
                         if (node.getCountReps() == 0)
                         {
                            if (node.getCondensedName() != "")
                               writer.print("</" + node.getCondensedName() + ">");
                            else
                            {
                               // All done with elements. Add end array tag
                               writer.println( "</arrayOfLongParm>");
                            }
                         }
                      }
                  }
                  break;
               case PcmlData.FLOAT:
                  if (node.getAttributeValue("length").equals("4"))
                  {
                      if (  ((PcmlData) node).getXPCMLCount(dim) <= 0 )
                      {
                         if (node.getCondensedName() != "")
                            writer.print("</" + node.getCondensedName() + ">");
                         else
                            writer.print("</floatParm>");
                      }
                      else
                      {
                         // Processing multiples of the node.  Should end with </i
                         writer.println( "  </i>");
                         if (node.getCountReps() == 0)
                         {
                            if (node.getCondensedName() != "")
                               writer.print("</" + node.getCondensedName() + ">");
                            else
                            {
                               // All done with elements. Add end array tag
                               writer.println( "</arrayOfFloatParm>");
                            }
                         }
                      }
                  }
                  else if (node.getAttributeValue("length").equals("8"))
                  {
                      if (  ((PcmlData) node).getXPCMLCount(dim) <= 0 )
                      {
                         if (node.getCondensedName() != "")
                            writer.print("</" + node.getCondensedName() + ">");
                         else
                            writer.print("</doubleParm>");
                      }
                      else
                      {
                         // Processing multiples of the node.  Should end with </i
                         writer.println( "  </i>");
                         if (node.getCountReps() == 0)
                         {
                            if (node.getCondensedName() != "")
                               writer.print("</" + node.getCondensedName() + ">");
                            else
                            {
                               // All done with elements. Add end array tag
                               writer.println( "</arrayOfDoubleParm>");
                            }
                         }
                      }
                  }
                  break;
               case PcmlData.ZONED:
                  if (  ((PcmlData) node).getXPCMLCount(dim) <= 0 )
                  {
                     if (node.getCondensedName() != "")
                        writer.print("</" + node.getCondensedName() + ">");
                     else
                        writer.print("</zonedDecimalParm>");
                  }
                  else
                  {
                     // Processing multiples of the node.  Should end with </i
                     writer.println( "  </i>");
                     if (node.getCountReps() == 0)
                     {
                        if (node.getCondensedName() != "")
                            writer.print("</" + node.getCondensedName() + ">");
                        else
                        {
                           // All done with elements. Add end array tag
                           writer.println( "</arrayOfZonedDecimalParm>");
                        }
                     }
                  }
                  break;
               case PcmlData.PACKED:
                  if (  ((PcmlData) node).getXPCMLCount(dim) <= 0 )
                  {
                     if (node.getCondensedName() != "")
                        writer.print("</" + node.getCondensedName() + ">");
                     else
                        writer.print("</packedDecimalParm>");
                  }
                  else
                  {
                     // Processing multiples of the node.  Should end with </i
                     writer.println( "  </i>");
                     if (node.getCountReps() == 0)
                     {
                       if (node.getCondensedName() != "")
                            writer.print("</" + node.getCondensedName() + ">");
                        else
                        {
                           // All done with elements. Add end array tag
                           writer.println( "</arrayOfPackedDecimalParm>");
                        }
                     }
                  }
                  break;
               default:
                  throw new PcmlException(DAMRI.BAD_DATA_TYPE, new Object[] {new Integer(dataType) , "*"} );
            }
        } else if (node.getNodeType() == PcmlNodeType.STRUCT)
        {
            writer.print(indent);
            if (  ((PcmlStruct) node).getXPCMLCount(dim) <= 0 )
            {
                if (node.getCondensedName() != "")
                   writer.print("</" + node.getCondensedName() + ">");
                else
                   writer.println("</struct>");
            }
            else
            {
               // Processing multiples of the node.  Should end with </struct_i
               writer.println( "  </struct_i>");
               if (node.getCountReps() == 0)
               {
                  if (node.getCondensedName() != "")
                      writer.print(indent+"</" + node.getCondensedName() + ">");
                  else
                  {
                      // All done with elements. Add end array tag
                      writer.println(indent+ "</arrayOfStruct>");
                  }
               }
            }
        }
        else if (node.getNodeType() == PcmlNodeType.DOCUMENT)
        {
        }
        else
          writer.println(indent + "</" + node.getTagName() + ">");

        if ( node.getNodeType() == PcmlNodeType.PROGRAM)
             writer.println();

        // Reduce current and number of dimensions by 1 since their are no
        // more children on this node (stepping back up the tree a level)

        if ((node.getNodeType() == PcmlNodeType.DATA && ((PcmlData) node).getXPCMLCount(dim) > 0 ) ||
            (node.getNodeType() == PcmlNodeType.STRUCT && ((PcmlStruct) node).getXPCMLCount(dim) > 0 ))
        {
          current_dimension--;
          num_dimensions--;
        }

        if (node.getCountReps() > 0)
        {
          // This node has a count greater than 0 so we need to process it again...
          // Calculate index
          Integer countVal= new Integer(0);
          if (node.getNodeType() == PcmlNodeType.DATA && ((PcmlData) node).getXPCMLCount(dim) != 0)
             countVal = new Integer( ((PcmlData )node).getXPCMLCount(dim));
          if (node.getNodeType() == PcmlNodeType.STRUCT && ((PcmlStruct) node).getXPCMLCount(dim) != 0)
             countVal = new Integer( ((PcmlStruct )node).getXPCMLCount(dim));
          if (node.getNodeType() != PcmlNodeType.DATA && node.getNodeType() != PcmlNodeType.STRUCT)
             countVal = new Integer(0);

          dimensions.set(current_dimension, countVal.intValue() - node.getCountReps());

          // Reprocess the node because count is > 0 so we need to print it out at least one more time
          generateXPCML(pcmlDocNode, node, writer, indent, num_dimensions, current_dimension, dimensions);
        }
        else
        {
            // We're done with this node so reset its count to -10
            node.setCountReps(-10);
        }
      }
      else  // node has no children
      {

        // Write out value of node as string value
        if (node.getNodeType() == PcmlNodeType.DATA && !node.getAttributeValue("type").equals("struct"))
        {
            // Get the value of the data node and print out as a string
            String strVal="";
            String tempVal="";
            Object objVal;

            int[] indices = new int[1];
            indices[0] = node.getCountReps();

            if (node.getQualifiedName().trim().equals(""))
            {
               // Can't set values of unnamed nodes
               strVal="";
            }
            else
            {
               try {
                  if (node.getAttributeValue("type").equals("byte"))
                  {
                       int length =  ((PcmlData)node).getLength(dimensions);
                       byte[] byteVal = new byte[length];
                       byteVal = (byte[]) pcmlDocNode.getValue(node.getQualifiedName(), dimensions);
                       if (byteVal != null)
                       {
                           // Now hexBinary encode the bytes
                           strVal = BinaryConverter.bytesToString(byteVal);
                       }
                  }
                  else {
                     strVal ="";
                     objVal = pcmlDocNode.getValue(node.getQualifiedName(), dimensions);
                     if (objVal != null)
                       strVal = objVal.toString();
                  }
               }
               catch (PcmlException e)
               {
                  Trace.log(Trace.PCML, "No value for node: " + node.getQualifiedName() );
               }
               catch (java.lang.IllegalArgumentException e)
               {
                  Trace.log(Trace.PCML, "Bad count value for node: " + node.getQualifiedName() );
               }
            }
            // Check if CountReps > 0.  If so, then dealing with an array
            if ( ((PcmlData) node).getXPCMLCount(dim) > 0 )
            {
               // Outputting an array element.  Check which type first
              if (lastTag.equals("arrayOfStringParm") || lastTag.equals("arrayOfIntParm") ||
                  lastTag.equals("arrayOfLongParm") || lastTag.equals("arrayOfUnsignedIntParm") ||
                  lastTag.equals("arrayOfShortParm") || lastTag.equals("arrayOfUnsignedShortParm") ||
                  lastTag.equals("arrayOfFloatParm") || lastTag.equals("arrayOfDoubleParm") ||
                  lastTag.equals("arrayOfZonedDecimalParm") || lastTag.equals("arrayOfPackedDecimalParm") ||
                  lastTag.equals("arrayOfHexBinaryParm"))
                     writer.println(">");
              if (dimensions.at(current_dimension) > 0)
              {
                   writer.print(indent + "  <i index=" + "\"" + dimensions.at(current_dimension) + "\"" + ">" + strVal);
              }
              else
              {
                   writer.print(indent + "  <i index=" + "\"" + dimensions.at(current_dimension) + "\"" + ">" + strVal);
              }
            } else
               writer.print(">" + strVal);

            // Check if CountReps > 0.  If so, then dealing with an array
            if ( ((PcmlData) node).getXPCMLCount(dim) > 0 )
            {
               // Outputting an array element.
               writer.println("</i>");
               tagDone = true;
            }
            else
            {
               writer.println("</" + lastTag + ">");
               tagDone = true;
            }
        }
        // Finish the start tag.
        if (!tagDone)
          writer.println("/>");

        if (node.getCountReps() > 0)     // Only called when data has a count > 0
        {
          // Calculate index
          int countVal;
          if (node.getNodeType() == PcmlNodeType.DATA && ((PcmlData) node).getXPCMLCount(dim) != 0)
             countVal =  ((PcmlData )node).getXPCMLCount(dim);
            else if (node.getNodeType() == PcmlNodeType.STRUCT && ((PcmlStruct) node).getXPCMLCount(dim) != 0)
               countVal = ((PcmlStruct)node).getXPCMLCount(dim);
                else
                   countVal = 0;
          int countV=countVal-node.getCountReps();
          dimensions.set(current_dimension, countVal - node.getCountReps());
          generateXPCML(pcmlDocNode,node, writer, indent, num_dimensions, current_dimension, dimensions);
        }
        else
        {
            // Check if CountReps > 0.  If so, then dealing with an array
            if (  ((PcmlData) node).getXPCMLCount(dim) > 0 )
            {
               // Outputting an array element.  Check which type first
               int dataType = ((PcmlData) node).getDataType();
               writer.print(indent);
               switch( dataType ) {
                  case PcmlData.CHAR:
                        if (node.getCondensedName() != "")
                           writer.println("</" + node.getCondensedName() + ">");
                        else
                           writer.println("</arrayOfStringParm>");
                        break;
                  case PcmlData.INT:
                     if (node.getAttributeValue("length").equals("4") && node.getAttributeValue("precision") != null && node.getAttributeValue("precision").equals("32") )
                     {
                        if (node.getCondensedName() != "")
                           writer.println("</" + node.getCondensedName() + ">");
                        else
                           writer.println("</arrayOfUnsignedIntParm>");
                     }
                     else if (node.getAttributeValue("length").equals("4") )
                     {
                        if (node.getCondensedName() != "")
                           writer.println("</" + node.getCondensedName() + ">");
                        else
                           writer.println("</arrayOfIntParm>");
                     }
                     else if (node.getAttributeValue("length").equals("2") && node.getAttributeValue("precision") != null && node.getAttributeValue("precision").equals("16") )
                     {
                        if (node.getCondensedName() != "")
                           writer.println("</" + node.getCondensedName() + ">");
                        else
                           writer.println("</arrayOfUnsignedShortParm>");
                     }
                     else if (node.getAttributeValue("length").equals("2") )
                     {
                        if (node.getCondensedName() != "")
                           writer.println("</" + node.getCondensedName() + ">");
                        else
                           writer.println("</arrayOfShortParm>");
                     }
                     else if (node.getAttributeValue("length").equals("8") )
                     {
                        if (node.getCondensedName() != "")
                           writer.println("</" + node.getCondensedName() + ">");
                        else
                           writer.println("</arrayOfLongParm>");
                     }
                     break;
                  case PcmlData.FLOAT:
                     if (node.getAttributeValue("length").equals("8") )
                     {
                        if (node.getCondensedName() != "")
                           writer.println("</" + node.getCondensedName() + ">");
                        else
                           writer.println("</arrayOfDoubleParm>");
                     }
                     else if (node.getAttributeValue("length").equals("4") )
                     {
                        if (node.getCondensedName() != "")
                           writer.println("</" + node.getCondensedName() + ">");
                        else
                           writer.println("</arrayOfFloatParm>");
                     }
                     break;
                 case PcmlData.ZONED:
                        if (node.getCondensedName() != "")
                           writer.println("</" + node.getCondensedName() + ">");
                        else
                           writer.println("</arrayOfZonedDecimalParm>");
                        break;
                 case PcmlData.PACKED:
                        if (node.getCondensedName() != "")
                           writer.println("</" + node.getCondensedName() + ">");
                        else
                           writer.println("</arrayOfPackedDecimalParm>");
                        break;
                 case PcmlData.BYTE:
                        if (node.getCondensedName() != "")
                           writer.println("</" + node.getCondensedName() + ">");
                        else
                           writer.println("</arrayOfHexBinaryParm>");
                        break;
                 default: break;
               }
            }
            node.setCountReps(-10);
        }
      } // end else if string value
      writer.flush();
   } // end generateXPCML


    // ******************************
    // @E0 -- New methods for XPCML *
    // ******************************
    void setXsdName(String xsdName)
    {
        m_XsdName = xsdName;
    }

    String getXsdName()
    {
        return m_XsdName;
    }


   // ******************************
   // @E0 -- New method for XPCML  *
   // ******************************
    void copyValues(PcmlDocNode root, PcmlDocNode node) throws IOException, XmlException
    {
      //@E1A -- New variables for XPCML.  These are used to control dimensioning
      // of a node.
      int num_dim =0;                     //@E1A
      int cur_dim=0;                   //@E1A
      int[] dims = {0,0,0,0,0,0,0,0,0,0};
      PcmlDimensions dimensions = new PcmlDimensions(dims);
      String[] struct = new String[30];
      String[] structR = new String[30];
     int count=0;

      copyValues(root, node,  num_dim, cur_dim, dimensions ,struct, structR, count);

   }

    void copyValues(PcmlDocNode root, PcmlDocNode node, int num_dimensions, int current_dimension, PcmlDimensions dimensions,String[] struct, String[] structR, int count)
      throws IOException, XmlException
    {
        int tempInt;


        // Define variable to be used to get count values of nodes
        PcmlDimensions dim = new PcmlDimensions();
        dim.add(0);

        if (count < 0 )
          count=0;

        // Reset all dimensions if this is a <program> or <pcml> node
        if (node.getNodeType() == PcmlNodeType.PROGRAM || node.getNodeType() == PcmlNodeType.DOCUMENT)
        {
            for (int i = 0; i < dimensions.size(); ++i)
            {
               dimensions.set(i,0);
            }
            current_dimension = 0;
            num_dimensions = 0;
            for (int i=0; i< 10 ; ++i)
            {
              struct[i] = "";
              structR[i] = "";
            }
           count=0;
        }

        // Check if this is the first time this node has been processed. The initial value for CountReps
        // is -10.  Get the count value for the node to determine how many times the node needs to be processed.
        if (node.getNodeType() == PcmlNodeType.DATA && node.getAttributeValue("type").equals("struct"))
        {
            PcmlDocNode parent = (PcmlDocNode) node.getParent();
            if ( parent.getNodeType()==PcmlNodeType.DATA &&
                 parent.getAttributeValue("type").equals("struct"))
              count++;
            else
              count=0;
            // Check if parent is a struct ref also
            structR[count] = node.getName();
            struct[count] = node.getAttributeValue("struct");

        }
        if (node.getCountReps() == -10)
        {
           int nodeType = node.getNodeType();
           if (nodeType == PcmlNodeType.DATA && ((PcmlData) node).getXPCMLCount(dim) != 0 )
           {
               // If the node is a data node and it has a count set tempInt to the count
               tempInt = ( (PcmlData) node).getXPCMLCount(dim);
           }
           else if (nodeType== PcmlNodeType.STRUCT && ((PcmlStruct) node).getXPCMLCount(dim) != 0)
           {
              // If the node is a struct node and it has a count set tempInt to the count
              tempInt = ( (PcmlStruct) node).getXPCMLCount(dim);
           }
           else
              tempInt = 1;

          node.setCountReps(tempInt);      // Set the number of reps for the node.

          // set current dimension
          dimensions.set(current_dimension,0);
       }

        // Reduce the count for this node by 1.
        if (node.getCountReps() != 0)
        {
            node.setCountReps(node.getCountReps() - 1);
        }

      if (node.hasChildren())
      {

        Enumeration children = node.getChildren();

        // Process the children of the current node
        while (children.hasMoreElements())
        {
          PcmlDocNode child = (PcmlDocNode) children.nextElement();

          // Reset all dimensions if this is <program> tag
          if (child.getNodeType() == PcmlNodeType.PROGRAM)
          {
             for (int i = 0; i < dimensions.size(); ++i)
             {
               dimensions.set(i,0);
             }
             current_dimension = 0;
             num_dimensions = 0;
             for (int i=0; i< 30 ; ++i)
             {
               struct[i] = "";
               structR[i] = "";
             }
            count=0;
          }

          // Reset all dimensions if this is a <struct> tag without a parent
          if (child.getNodeType() == PcmlNodeType.STRUCT && child.getParent().getName().trim().length()==0 )
          {
             for (int i = 0; i < dimensions.size(); ++i)
             {
               dimensions.set(i,0);
             }
             current_dimension = 0;
             num_dimensions = 0;
             for (int i=0; i< 30 ; ++i)
              {
                struct[i] = "";
                structR[i] = "";
              }
            count=0;
          }

          // Increase dimensions by 1 if this is the first child, i.e., we've gone down the tree
          if (child.getChildNbr()==0 &&
             ( (node.getNodeType() == PcmlNodeType.DATA && ((PcmlData) node).getXPCMLCount(dim) > 0 ) ||
               (node.getNodeType() == PcmlNodeType.STRUCT && ((PcmlStruct) node).getXPCMLCount(dim) > 0 ) ) )
          {
             num_dimensions++;
             current_dimension++;
             if (num_dimensions > 9)
                dimensions.add(0);
          }

             copyValues(root, child, num_dimensions, current_dimension, dimensions, struct, structR, count);
        }  // end while more children

        // Reduce current and number of dimensions by 1 since their are no
        // more children on this node (stepping back up the tree a level)

        if ((node.getNodeType() == PcmlNodeType.DATA && ((PcmlData) node).getXPCMLCount(dim) > 0 ) ||
            (node.getNodeType() == PcmlNodeType.STRUCT && ((PcmlStruct) node).getXPCMLCount(dim) > 0 ))
        {
          current_dimension--;
          num_dimensions--;
          count--;
        }

        if (node.getCountReps() > 0)
        {
          // This node has a count greater than 0 so we need to process it again...
          // Calculate index
          Integer countVal= new Integer(0);
          if (node.getNodeType() == PcmlNodeType.DATA && ((PcmlData) node).getXPCMLCount(dim) != 0)
             countVal = new Integer( ((PcmlData )node).getXPCMLCount(dim));
          if (node.getNodeType() == PcmlNodeType.STRUCT && ((PcmlStruct) node).getXPCMLCount(dim) != 0)
             countVal = new Integer( ((PcmlStruct )node).getXPCMLCount(dim));
          if (node.getNodeType() != PcmlNodeType.DATA && node.getNodeType() != PcmlNodeType.STRUCT)
             countVal = new Integer(0);

          dimensions.set(current_dimension, (countVal.intValue() - node.getCountReps()));

          // Reprocess the node because count is > 0 so we need to print it out at least one more time
          copyValues(root, node,  num_dimensions, current_dimension, dimensions, struct, structR, count);
        }
        else
        {
            // We're done with this node so reset its count to -10
            node.setCountReps(-10);
        }
      }
      else  // node has no children
      {

        // Copy the node value to the new node
        if (node.getNodeType() == PcmlNodeType.DATA && !node.getAttributeValue("type").equals("struct"))
        {
            // Get the value of the data node and print out as a string
            String strVal="";
            String tempVal="";
            Object objVal;

            int[] indices = new int[1];
            indices[0] = node.getCountReps();

            if (node.getQualifiedName().trim().equals(""))
            {
               // Can't set values of unnamed nodes
               objVal=null;
            }
            else
            {
               try {
                  objVal=null;
                  if ( node.getUsage() != PcmlDocNode.OUTPUT )
                  {
                     objVal = ((PcmlData)node).getValue(dimensions);
                     if (objVal == null)
                     {
                        // Not set yet so see if the referenced node has values and set it to
                        if (  ( (PcmlDocNode)node.getParent()).getNodeType()==PcmlNodeType.DATA &&
                              ( (PcmlDocNode)node.getParent()).getAttributeValue("type").equals("struct") ||
                              ( (PcmlDocNode)node.getParent()).getNodeType()==PcmlNodeType.STRUCT)
                        {
                          // Parent is a struct parm. Check if struct parm referenced value
                          // is set and, if so, copy that value here...
                          boolean augmented=false;
                          PcmlDocNode parent = (PcmlDocNode) node.getParent();
                          while ( parent != null &&
                                  parent.getNodeType() != PcmlNodeType.PROGRAM &&
                                  augmented==false )
                          {
                             if (parent.getNodeType() == PcmlNodeType.DATA &&
                                 parent.getAttributeValue("type").equals("struct") )
                                 augmented=true;
                             parent=(PcmlDocNode) parent.getParent();
                          }
                          if (augmented)
                          {
                            String[] nodeName = new String[count+1];
                            for (int i=0; i<= count ; i++)
                            {
                               nodeName[i] = "";
                            }
                            boolean found=false;
                            for (int i=0; i<=count && !found; ++i)
                            {
                              nodeName[i] = struct[i];
                              for (int j=i+1; j<=count; j++)
                              {
                                   nodeName[i]= nodeName[i] + "." + structR[j];
                              }
                              String fullName = node.getQualifiedName();
                              String restOfName = fullName.substring(fullName.indexOf(structR[count])+structR[count].length()+1);
                              nodeName[i] = nodeName[i] + "." + restOfName;
                              String nodeToGet = nodeName[i];
                              PcmlDocRoot rootOfTree = root.getRootNode();
                              PcmlData nodeToCopy=null;
                              try {
                                 nodeToCopy = (PcmlData) rootOfTree.getElement(nodeToGet);
                              }
                              catch (NullPointerException e)
                              {
                                 nodeToCopy = null;
                              }
                              if (nodeToCopy != null)
                              {
                                int copyDim = nodeToCopy.getNbrOfDimensions();
                                int[] copy_dim = new int[copyDim];
                                for (int j=0; j< copyDim; ++j)
                                {
                                   copy_dim[j] = dimensions.at( ((PcmlData) node).getNbrOfDimensions() - copyDim + j);
                                }
                                PcmlDimensions copy_dimensions = new PcmlDimensions(copy_dim);
                                objVal = nodeToCopy.getValue(copy_dimensions);
                                if (objVal != null)
                                {
                                   ((PcmlData) node).setValue(objVal, dimensions);
                                   found=true;
                                }
                              }
                            }
                          }
                        }
                     }
                  }
               }
               catch (PcmlException e)
               {
                  Trace.log(Trace.PCML, "No value for node: " + node.getQualifiedName() );
               }
               catch (java.lang.IllegalArgumentException e)
               {
                  Trace.log(Trace.PCML, "Bad count value for node: " + node.getQualifiedName() );
               }
            }

            // Check if CountReps > 0.  If so, then dealing with an array

        if (node.getCountReps() > 0)     // Only called when data has a count > 0
        {
          // Calculate index
          int countVal;
          if (node.getNodeType() == PcmlNodeType.DATA && ((PcmlData) node).getXPCMLCount(dim) != 0)
             countVal =  ((PcmlData )node).getXPCMLCount(dim);
            else if (node.getNodeType() == PcmlNodeType.STRUCT && ((PcmlStruct) node).getXPCMLCount(dim) != 0)
               countVal = ((PcmlStruct)node).getXPCMLCount(dim);
                else
                   countVal = 0;
          int countV=countVal-node.getCountReps();
          dimensions.set(current_dimension, (countVal - node.getCountReps()));
          copyValues(root, node, num_dimensions, current_dimension, dimensions, struct, structR, count);
        }
        else
        {
           node.setCountReps(-10);
        }
      } // end else if string value
    }
 }



}
