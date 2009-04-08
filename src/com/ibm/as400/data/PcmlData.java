///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PcmlData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.ProgramParameter;                       // @B1A
import com.ibm.as400.access.BidiStringType;                         // @C9A

import java.io.IOException;                                         // @C1A
import java.io.ObjectOutputStream;                                  // @C1A
import java.io.OutputStream;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

class PcmlData extends PcmlDocNode
{
    /***********************************************************
     Static Members
    ***********************************************************/

    // Constant values for type= attribute
    public static final int UNSUPPORTED = 0;
    public static final int CHAR   = 1;
    public static final int INT    = 2;
    public static final int PACKED = 3;
    public static final int ZONED  = 4;
    public static final int FLOAT  = 5;
    public static final int BYTE   = 6;
    public static final int STRUCT = 7;

    // Largest length= supported for type="char" and type="byte"
    public static final int MAX_STRING_LENGTH = 1024*1024;          // @C6C

    // Serial verion unique identifier
    static final long serialVersionUID = 8578048664805881489L;

    // New attributes should be added to the end of this array
    private static final String DATAATTRIBUTES[] = {
        "name",
        "usage",
        "count",
        "minvrm",
        "maxvrm",
        "offset",
        "offsetfrom",
        "outputsize",
        "type",
        "length",
        "precision",
        "ccsid",
        "init",
        "struct",
        "passby",                       // PCML Ver. 2.0
        "bidistringtype",               // PCML Ver. 3.0               @C9A
        "trim",                         // PCML Ver. 4.0               @D1A
        "chartype"                      // PCML Ver. 4.0               @D2A
        // Note: "keyfield" is unique to RFML, so is not listed here.
    };
    private static final int VERSION_1_ATTRIBUTE_COUNT = 14;
    private static final int VERSION_2_ATTRIBUTE_COUNT = 15;
    private static final int VERSION_3_ATTRIBUTE_COUNT = 16;        // @C9A
    private static final int VERSION_4_ATTRIBUTE_COUNT = 18;        // @D1A @D2C

    /***********************************************************
     Instance Members
    ***********************************************************/

    // The "m_name" and "m_usage" attributes are implemented by PcmlDocNode

    // The following values are implemented by PcmlData and PcmlStruct
    private int    m_Count;         // count=, integer literal
    private String m_CountId;       // count=, element name
    private int    m_Offset;        // offset=, integer literal
    private String m_OffsetId;      // offset=, element name
    private boolean m_OffsetfromFixed;  // Flag indicating whether serialized version @A1A
                                        // of this object contains fix for offsetfrom
    private int    m_Offsetfrom;    // offsetfrom=, integer literal    @A1A
    private String m_OffsetfromId;  // offsetfrom=, element name
    private String m_Minvrm;        // minvrm=, string literal
    private int    m_MinvrmInt;     // minvrm=, from AS400.generateVRM()
    private String m_Maxvrm;        // maxvrm=, string literal
    private int    m_MaxvrmInt;     // maxvrm=, from AS400.generateVRM()
    private int    m_Outputsize;    // outputsize=, integer literal
    private String m_OutputsizeId;  // outputsize=, element name

    // The following attribute are implemented only by PcmlData
    private String m_TypeStr;       // type=, string literal
    private int    m_Type;          // type=, integer representing data type
    private int    m_Length;        // length=, integer literal
    private String m_LengthId;      // length=, element name
    private boolean m_LengthWasSpecified; // Indicates whether length was specified.  @D0A
    private int    m_Precision;     // precison=, integer literal
    private int    m_Ccsid;         // ccsid=, integer literal
    private String m_CcsidId;       // ccsid=, element name
    private boolean m_CcsidWasSpecified; // Indicates whether ccsid was specified.  @D0A
    private String m_Init;          // init=, string literal
    private String m_StructId;      // struct=, element name

    // The following attributes added for PCML v2.0
    private String m_PassbyStr;     // passby=, string literal                       @B1A
    private int    m_Passby;        // passby=, integer representing passby value    @B1A

    // The following attributes added for PCML v3.0
    private String m_BidistringtypeStr;     // bidistringtype=, string literal       @C9A
    private int    m_Bidistringtype;        // bidistringtype=, integer representing value    @C9A

    private boolean m_IsRfml;        // Indicates whether RFML versus PCML.  @D0A

    // The following attributes added for PCML v4.0
    private String m_TrimStr;        // trim=, string literal          @D1A
    private String m_CharType;       // chartype=, string literal      @D2A

    // The following attributes added for RFML v5.0 (not relevant to PCML)
    private String  m_KeyFieldStr;   // keyfield=, string literal
    private boolean m_KeyField;      // keyfield=, boolean representing value

    /***********************************************************
     Semi-Transient Members --
     Not written when serializing interface definition.
     Written when serializing ProgramCallDocument object.
    ***********************************************************/
    private PcmlDataValues m_scalarValue;                           // @C1C
    private PcmlDataVector m_vectorValue;                           // @C1C

    // Default constructor
    PcmlData()
    {
      this(false);                                                   // @D0A
    }

    // Constructor with description
    PcmlData(PcmlAttributeList attrs)                               // @C3C
    {
        this(attrs, false);                                         // @D0C
    }

    PcmlData(boolean isRfml)                                   // @D0A
    {
        m_IsRfml = isRfml;
    }

    // Constructor with description
    PcmlData(PcmlAttributeList attrs, boolean isRfml)          // @D0A
    {
        super(attrs);                                               // @C3C
        m_IsRfml = isRfml;                                          // @D0A
        setNodeType(PcmlNodeType.DATA);                             // @C3C

        // **********************************
        // Set attribute values
        //
        // The following code extracts the attribute values
        // from the parsed document node and
        // stores the values in private data members.
        // **********************************

        // Set count= attribute value
        setCount(getAttributeValue("count"));

        // Set offset= attribute value
        setOffset(getAttributeValue("offset"));

        // Set offsetfrom= attribute value
        setOffsetfrom(getAttributeValue("offsetfrom"));

        // Set minvrm= attribute value
        setMinvrm(getAttributeValue("minvrm"));

        // Set maxvrm= attribute value
        setMaxvrm(getAttributeValue("maxvrm"));

        // Set type= attribute value
        setType(getAttributeValue("type"));

        // Set length= attribute value
        setLength(getAttributeValue("length"));

        // Set precision= attribute value
        setPrecision(getAttributeValue("precision"));

        // Set ccsid= member variable
        setCcsid(getAttributeValue("ccsid"));

        // Set init= member variable
        setInit(getAttributeValue("init"));

        // Set outputsize= member variable
        setOutputsize(getAttributeValue("outputsize"));

        // Set struct= member variable
        setStruct(getAttributeValue("struct"));

        // Set passby= member variable
        setPassby(getAttributeValue("passby"));                     // @B1A

        // Set bidistringtype= member variable
        setBidiStringType(getAttributeValue("bidistringtype"));     // @C9A

        // Set trim= attribute value
        setTrim(getAttributeValue("trim"));                         // @D1A

        // Set chartype= attribute value
        setCharType(getAttributeValue("chartype"));                 // @D2A

        // Set keyfield= attribute value  (relevant only to RFML)
        setKeyField(getAttributeValue("keyfield"));

        m_scalarValue = null; // Transient data created as needed
        m_vectorValue = null; // Transient data created as needed

    }

    public Object clone()                                           // @C5A
    {                                                               // @C5A
        PcmlData node = (PcmlData) super.clone();                   // @C5A
        // Cloning does not include 'live' data, only the interface
        // definitions described by the PCML tags.
        // Null out the 'semi-transient' data values.
        node.m_scalarValue = null;                                  // @C5A
        node.m_vectorValue = null;                                  // @C5A

        return node;                                                // @C5A
    }                                                               // @C5A


    // Custom serialization
    private void writeObject(ObjectOutputStream out) throws IOException // @C1A
    {                                                               // @C1A
		synchronized (this)                                         // @C1A
		{                                                           // @C1A
			// Keep a local reference to the scalar and vector data values
	 		PcmlDataValues scalarValue = m_scalarValue;             // @C1A
			PcmlDataVector vectorValue = m_vectorValue;             // @C1A

			// If not saving with serialization, temporarily null out the
			// scalar and vector data values member variables
			// so they are not written to the ObjectOutputStream.
			if ( !getDoc().isSerializingWithData() )                // @C1A
			{                                                       // @C1A
				m_scalarValue = null;                               // @C1A
				m_vectorValue = null;                               // @C1A
			}                                                       // @C1A

			// Perform default serialization
			out.defaultWriteObject();                               // @C1A

			// Restore scalar and vector data values
			m_scalarValue = scalarValue;                            // @C1A
			m_vectorValue = vectorValue;                            // @C1A
		} // end of synchronized code                               // @C1A
    }                                                               // @C1A

	// Custom deserialization post-processing
	// This processing cannot be done during readObject() because
	// references to parent objects in the document are not yet set
	// due to the recursive nature of deserialization.
    void readObjectPostprocessing()                                 // @C1A
    {                                                               // @C1A
        if (m_scalarValue != null)                                  // @C1A
            m_scalarValue.readObjectPostprocessing();               // @C1A
        if (m_vectorValue != null)                                  // @C1A
            m_vectorValue.readObjectPostprocessing();               // @C1A

        super.readObjectPostprocessing();                           // @C1A
    }                                                               // @C1A

    // Returns a single PcmlDataValues given an array of indices
    private PcmlDataValues getPcmlDataValues(PcmlDimensions indices) throws PcmlException
    {
        int index;
        int[] myDimensions = getDimensions(indices).asArray();


        // Make sure enough indices are specified
        // Allow more indices than necessary in order to
        // allow nieces and nephews to get access to
        // values such as getCount()
        if (indices.size() < myDimensions.length)
        {
            throw new PcmlException(DAMRI.TOO_FEW_INDICES, new Object[] {new Integer(indices.size()), new Integer(myDimensions.length), getNameForException()} );
        }

        if (myDimensions.length == 0)
        {
            if (m_scalarValue == null)
            {
                m_scalarValue = new PcmlDataValues(this, new PcmlDimensions());
            }
            return m_scalarValue;
        }

        PcmlDataValues item;
        PcmlDataVector v, nextVector;
        PcmlDimensions myIndices = new PcmlDimensions();

        // If the vector for the first dimension (first index) has not been
        // created, create and initialize the vector
        if (m_vectorValue == null)
        {
            m_vectorValue = new PcmlDataVector(myDimensions[0], this, myIndices );
            for (int elem = 0; elem < myDimensions[0]; elem++)
            {
                m_vectorValue.addElement(null);
            }
        }

        // Walk down the tree of vectors. All but the last index is to a
        // vector; the last index is to a PcmlDataValues.
        long[] myDimensionTimestamps = getDimensionTimestamps(indices);
        v = m_vectorValue;
        for (int i = 0; i < myDimensions.length; i++)
        {

            // If the current dimension is more recent
            // than the dimension of the data, we need to
            // trow away all data at this and deeper dimensions
            // because it is stale.
            // In other words, the count= value was changed for this dimension
            // and we need to throw away all the values stored.
            if ( myDimensionTimestamps[i] > v.getTimestamp() )
            {
                v.redimension(myDimensions[i]);
            }

            // Get index for current dimension and add it to my working PcmlDimensions
            index = indices.at(i);
            myIndices.add(index);

            // Make sure index is not out of bounds
            if (index < 0 || index >= myDimensions[i])
            {
                throw new PcmlException(DAMRI.INDEX_OUT_OF_BOUNDS, new Object[] {new Integer(myDimensions[i]-1), new Integer(i), indices, getNameForException()} );  // @D0C Subtract 1 from myDimensions to get the upper end of range to come out right in the message.
            }

            // If we have are not on the last (deepest) dimension
            // get the PcmlDataVector for this dimension
            if (i != myDimensions.length - 1 )
            {
                nextVector = v.vectorAt(index);
                // If no PcmlDataVector has been created yet, create one now
                if (nextVector == null)
                {
                    nextVector = new PcmlDataVector(myDimensions[i+1], this, myIndices);
                    for (int elem = 0; elem < myDimensions[i+1]; elem++)
                    {
                        nextVector.addElement(null);
                    }
                    v.setElementAt(nextVector, index);
                }
                v = nextVector;
            }
            // We are on the last (deepest) dimension,
            // get the PcmlDataValues object -- at last!
            else
            {
                item = v.valuesAt(index);
                // If no PcmlDataValues has been created yet, create it now.
                if (item == null)
                {
                    item = new PcmlDataValues(this, myIndices);
                    v.setElementAt(item, index);
                }
                // Finally return the PcmlDataValues object requested
                return item;
            }
        }

        // We should never get here, but...
        throw new PcmlException(DAMRI.ERROR_ACCESSING_VALUE, new Object[] {indices, myDimensions, getNameForException()} );
    }

    // Get Timestamp of data
    long getTimestamp(PcmlDimensions indices) throws PcmlException
    {
        // Make sure enough indices are specified
        if ( indices.size() >= getNbrOfDimensions() )
        {
            PcmlDataValues values = getPcmlDataValues(indices);
            return values.getTimestamp();
        }
        else
        {
            throw new PcmlException(DAMRI.TOO_FEW_INDICES, new Object[] {new Integer(indices.size()), new Integer(getNbrOfDimensions()), getNameForException()} );
        }
    }

    // Get Java native value
    final Object getValue() throws PcmlException
    {
        return getValue(new PcmlDimensions());
    }

    // Get Java native value
    final Object getValue(PcmlDimensions indices) throws PcmlException
    {
        if (m_Type == CHAR)                                     // @C9A
        {
            return getStringValue(indices, m_Bidistringtype);   // @C9A
        }
        else                                                    // @C9A
        {
            // Make sure enough indices are specified
            if ( indices.size() >= getNbrOfDimensions() )
            {
                PcmlDataValues values = getPcmlDataValues(indices);
                return values.getValue();
            }
            else
            {
                throw new PcmlException(DAMRI.TOO_FEW_INDICES, new Object[] {new Integer(indices.size()), new Integer(getNbrOfDimensions()), getNameForException()} );
            }
        }
    }


    // Get String value specifying string type
    final String getStringValue(PcmlDimensions indices, int type)
        throws PcmlException                                            // @C9A
    {
        Object val = null;

        // Make sure enough indices are specified
        if ( indices.size() >= getNbrOfDimensions() )                   // @C9A
        {
            PcmlDataValues values = getPcmlDataValues(indices);         // @C9A
            if (m_Type == CHAR)                                         // @CBA
                values.setStringType(type); // Set the string type             @C9A
            val = values.getValue();    // Get the value              @C9A @CAC
            if (val == null)                                            // @CBA
            {
                return null;                                            // @CBA
            }
            if (val instanceof Number)
            {
                return ((Number)val).toString();                        // @C9A
            }
            else if (val instanceof String)                             // @C9A
            {
                return (String)val;                                     // @C9A
            }
            else                                                        // @C9A
            {
                throw new PcmlException(DAMRI.STRING_OR_NUMBER, new Object[] {val.getClass().getName(), getNameForException()} );   // @C9A
            }
        }
        else
        {
            throw new PcmlException(DAMRI.TOO_FEW_INDICES, new Object[] {new Integer(indices.size()), new Integer(getNbrOfDimensions()), getNameForException()} );  // @C9A
        }
    }

    // Set Java native value
    final void setValue(Object v) throws PcmlException
    {
        setValue(v, new PcmlDimensions());
    }

    // Set Java native value
    final void setValue(Object v, PcmlDimensions indices) throws PcmlException
    {
        // Make sure enough indices are specified
        if ( indices.size() >= getNbrOfDimensions() )
        {
            PcmlDataValues values = getPcmlDataValues(indices);
            values.setStringType(m_Bidistringtype);
            values.setValue(v);
        }
        else
        {
            throw new PcmlException(DAMRI.TOO_FEW_INDICES, new Object[] {new Integer(indices.size()), new Integer(getNbrOfDimensions()), getNameForException()} );
        }
    }

    // Set String value specifying string type
    final void setStringValue(String val, PcmlDimensions indices, int type)
        throws PcmlException                                            // @C9A
    {
        // Make sure enough indices are specified
        if ( indices.size() >= getNbrOfDimensions() )                   // @C9A
        {
            PcmlDataValues values = getPcmlDataValues(indices);         // @C9A
            values.flushValues();       // Flush current values            @C9A
            values.setStringType(type); // Set the string type             @C9A
            values.setValue(val);       // Set the value                   @C9A
        }
        else
        {
            throw new PcmlException(DAMRI.TOO_FEW_INDICES, new Object[] {new Integer(indices.size()), new Integer(getNbrOfDimensions()), getNameForException()} );  // @C9A
        }
    }

    // Set IBM i system bytes
    void setBytes(byte[] ba)
    {
    }

   /**
    * Return the list of valid attributes for the data element.
    **/
    String[] getAttributeList()                                 // @C7A
    {
        int returnCount = 0;                                    // @C7A
        String returnArray[];                                   // @C7A

        if ( getDoc().getVersion().compareTo("2.0") < 0 )       // @C7A
            returnCount = VERSION_1_ATTRIBUTE_COUNT;            // @C7A
        else if ( getDoc().getVersion().compareTo("3.0") < 0 )  // @C9A
            returnCount = VERSION_2_ATTRIBUTE_COUNT;            // @C9A
        else if ( getDoc().getVersion().compareTo("4.0") < 0 )  // @D1A
            returnCount = VERSION_3_ATTRIBUTE_COUNT;            // @D1A
        else                            // Anything else return the entire array
            return DATAATTRIBUTES;                              // @C7A

        returnArray = new String[returnCount];                  // @C7A

        System.arraycopy(DATAATTRIBUTES, 0, returnArray, 0, returnCount);   // @C7A
        return returnArray;                                     // @C7A
    }


    final int getCcsid(PcmlDimensions indices) throws PcmlException
    {
        int tmpCcsid = resolveIntegerValue( getCcsid(),
                                            getCcsidId(),
                                            indices );
        // If a CCSID is not explicitly defined for this element,
        // use the CCSID from the <program> element
        if (tmpCcsid == 0)
        {                                                           // @C2A
            PcmlNode node = getParent();                            // @C2A
            while (node.getParent() != getDoc())                    // @C2A
                node = node.getParent();                            // @C2A
            // If this element is a descendent of a Program element
            // use the CCSID saved the last time the
            if (node instanceof PcmlProgram)                        // @C2A
                return ((PcmlProgram) node).getProgramCCSID();      // @C2A
            else                                                    // @C2A
                return getDoc().getAs400().getCcsid();              // @C2A
        }                                                           // @C2A
        else
            return tmpCcsid;
    }

    // Get the run-time dimension for this element
    final int getCount(PcmlDimensions indices) throws PcmlException
    {
        return resolveIntegerValue( getCount(),
                                    getCountId(),
                                    indices );
    }

    // @E0A -- New XPCML method
    // Get the run-time dimension for this element but don't throw an exception if count not set
    final int getXPCMLCount(PcmlDimensions indices) throws PcmlException
    {
        int rc;
        try {
          rc = resolveIntegerValue( getCount(),
                                    getCountId(),
                                    indices );
          return rc;
        }
        catch (Exception e)
        {
           return 0;
        }
    }

    // Get the count= integer literal value, if any
    public final int getCount()
    {
        return m_Count;
    }

    // Get the count= resolved element name, if any
    public final String getCountId()
    {
        return resolveRelativeName(m_CountId);
    }

    // Get the count= unresolved element name, if any
    public final String getUnqualifiedCountId()                     // @C7A
    {
        return m_CountId;                                           // @C7A
    }

    final int getLength(PcmlDimensions indices) throws PcmlException
    {
        return resolveIntegerValue( getLength(),
                                    getLengthId(),
                                    indices );
    }

    // Get the run-time offset value for this element
    final int getOffset(PcmlDimensions indices) throws PcmlException
    {
        return resolveIntegerValue( getOffset(),
                                    getOffsetId(),
                                    indices );
    }

    // Get the offset= integer literal value, if any
    public final int getOffset()
    {
        return m_Offset;
    }

    // Get the offset= resolved element name, if any
    public final String getOffsetId()
    {
        return resolveRelativeName(m_OffsetId);
    }

    // Get the offset= unresolved element name, if any
    public final String getUnqualifiedOffsetId()                    // @C7A
    {
        return m_OffsetId;                                          // @C7A
    }

    // Return indication of whether this object contains
    // the fix for ofsetfrom
    private final boolean isOffsetfromFixed()                       // @A1A
    {                                                               // @A1A
        return m_OffsetfromFixed;                                   // @A1A
    }                                                               // @A1A

    // Get the passby= value as an integer (ProgramParameter constant)
    public final int getPassby()                                    // @B1A
    {                                                               // @B1A
        if (m_PassbyStr != null)                                    // @B1A
            return m_Passby;                                        // @B1A
        else                                                        // @B1A
            return ProgramParameter.PASS_BY_REFERENCE;              // @B1A
    }                                                               // @B1A

    // Get the bidistringtype= value as an integer
    public final String getBidistringtypeStr()                    // @C9A @CAC
    {
        return m_BidistringtypeStr;                            // @C9A @CAC
    }

    // Get the offsetfrom= integer literal value, if any
    final int getOffsetfrom()                                       // @A1C
    {
        if ( isOffsetfromFixed() )                                  // @A1A
            return m_Offsetfrom;                                    // @A1C
        else                                                        // @A1A
            return -1;                                              // @A1A
    }

    // Get the offsetfrom= resolved element name, if any
    final String getOffsetfromId()                                  // @A1A
    {                                                               // @A1A
        return resolveRelativeName(m_OffsetfromId);                 // @A1A
    }                                                               // @A1A

    // Get the offset= unresolved element name, if any
    public final String getUnqualifiedOffsetfromId()                // @C7A
    {
        return m_OffsetfromId;                                      // @C7A
    }

    // Returns an array of integers containing the array dimensions
    // Notes:
    //      getDimensions().length == 0 for scalar data
    PcmlDimensions getDimensions(PcmlDimensions indices) throws PcmlException
    {
        PcmlDimensions myDimensions = null;
        PcmlNode node = getParent();                                // @CCA

        // Retrieve array dimensions from all ancestors
        if (node instanceof PcmlData)                               // @CCC
        {
            myDimensions = ((PcmlData) node).getDimensions(indices);// @CCC
        }
        else
        if  (node instanceof PcmlStruct)                            // @CCC
        {
            myDimensions = ((PcmlStruct) node).getDimensions(indices);  // @CCC
        }
        else
        {
            myDimensions = new PcmlDimensions(getNbrOfDimensions());
        }

        // If this node is defined as an array, add its dimension
        if (isArray())
        {
            int myCount = getCount(indices);
            myDimensions.add(myCount);
        }

        return myDimensions;
    }

    // Returns an array of integers containing the timestamps
    // for each of the array dimensions for this node.
    // Notes:
    //      getNbrOfDimensions() == 0 for scalar data
    //      getNbrOfDimensions() == getDimensions().length
    long[] getDimensionTimestamps(PcmlDimensions indices) throws PcmlException
    {
        long[] myTimestamps;
        Integer myIndex = null;
        long[] previousTimestamps;
        PcmlNode node = getParent();                                                // @CCA

        // If an array is defined at this node,
        // remove its dimension from the array of indices
        if (isArray())
        {
            myIndex = indices.integerAt(indices.size()-1);
            indices.remove();
        }

        // Retrieve array dimensions from all ancestors
        if (node instanceof PcmlData)                                               // @CCC
        {
            previousTimestamps = ((PcmlData) node).getDimensionTimestamps(indices); // @CCC
        }
        else
        if  (node instanceof PcmlStruct)                                            // @CCC
        {
            previousTimestamps = ((PcmlStruct) node).getDimensionTimestamps(indices);   // @CCC
        }
        else
        {
            previousTimestamps = new long[0];
        }

        // If this node is defined as an array, add its dimension
        // back to the array of indices and get the time stamp for this dimension.
        if (myIndex != null)
        {
            int i;
            indices.add(myIndex);
            myTimestamps = new long[previousTimestamps.length + 1];
            for (i = 0; i < previousTimestamps.length; i++)
            {
                myTimestamps[i] = previousTimestamps[i];
            }
            myTimestamps[i] = resolveDimensionTimestamp(indices);
            if (i > 0)
            {
                myTimestamps[i] = Math.max(myTimestamps[i], myTimestamps[i-1]);
            }
        }
        else
        {
            myTimestamps = previousTimestamps;
        }

        return myTimestamps;
    }

    // Returns the number of dimensions for this data node
    // Notes:
    //      getNbrOfDimensions() == 0 for scalar data
    //      getNbrOfDimensions() == getDimensions().length
    int getNbrOfDimensions()
    {
        int total = 0;
        PcmlNode node = getParent();                                // @CCA

        if (isArray())
            total++;

        if (node instanceof PcmlData)                               // @CCC
            total += ((PcmlData)node).getNbrOfDimensions();         // @CCC
        else
            if (node instanceof PcmlStruct)                         // @CCC
                total += ((PcmlStruct)node).getNbrOfDimensions();   // @CCC

        return total;
    }

    int getOutputsize(PcmlDimensions indices) throws PcmlException
    {
        int totalSize = 0;
        int myCount;
        boolean processArray;

        // If outputsize= was specified for this element use that
        // as the output size for this and all descendents.
        totalSize = resolveIntegerValue( getOutputsize(),
                                         getOutputsizeId(),
                                         indices );
        if (totalSize > 0)
            return totalSize;

        if (isArray() && indices.size() < getNbrOfDimensions() )
        {
            myCount = getCount(indices);
            processArray = true;
        }
        else
        {
            myCount = 1;
            processArray = false;
        }

        for (int myIndex  = 0; myIndex < myCount; myIndex++)
        {

            if (processArray)
            {
                indices.add(myIndex);
            }


            switch (getDataType())
            {

                case PcmlData.STRUCT:
                    Enumeration children;
                    PcmlDocNode child;

                    children = getChildren();
                    while (children.hasMoreElements())
                    {
                        child = (PcmlDocNode) children.nextElement();
                        switch (child.getNodeType())
                        {
                            case PcmlNodeType.STRUCT:
                                totalSize += ((PcmlStruct) child).getOutputsize(indices);
                                break;
                            case PcmlNodeType.DATA:
                                totalSize += ((PcmlData) child).getOutputsize(indices);
                                break;
                            default:
                                throw new PcmlException(DAMRI.BAD_NODE_TYPE, new Object[] {new Integer(child.getNodeType()) , getNameForException()} );
                        }
                    }
                    break;

                // For all scalar types
                default:
                    totalSize += getPcmlDataValues(indices).byteLength();
                    if (totalSize == 0)
                    {
                        totalSize = 32;
                    }

            }

            if (processArray)
            {
                indices.remove();
            }

        } // END: for myIndex

        return totalSize;
    }

    // Get the trim= resolved element name, if any
    public final String getTrim()                                   // @D1A
    {                                                               // @D1A
        return m_TrimStr;                                           // @D1A
    }                                                               // @D1A

    // Get the trim= resolved element name, if any
    public final String getCharType()                               // @D2A
    {                                                               // @D2A
        return m_CharType;                                          // @D2A
    }                                                               // @D2A

    boolean isArray()
    {
        if ( getCount() > 0 )
            return true;
        else
            if ( getCountId() != null )
                return true;

        return false;
    }

    // Returns true if this node is defined as an array or
    // has an ancestor defined as an array.
    boolean isInArray()
    {
        PcmlNode node = getParent();                                // @CCA

        if (isArray())
            return true;
        else
            if (node instanceof PcmlData)                           // @CCC
                return ((PcmlData)node).isInArray();                // @CCC
            else
                if (node instanceof PcmlStruct)                     // @CCC
                    return ((PcmlStruct)node).isInArray();          // @CCC
                else
                    return false;
    }

    // Returns true if the length attribute has been specified.        @D0A
    public final boolean isLengthSpecified()
    {
      return (m_LengthWasSpecified || m_Length != 0 || m_LengthId != null);
      // Note: This conditional is beefed-up to handle the case where a PcmlData object from an older version (before m_LengthWasSpecified was added) was serialized and then deserialized into the current version.
    }

    // Returns true if this document element is supported at the
    // at the VRM of the current host.
    // Returns false if not.
    boolean isSupportedAtHostVRM() throws PcmlException             // @A1A
    {                                                               // @A1A
        if (m_IsRfml) return true;                                  // @D0A
        int hostVrm = getAs400VRM();      // VRM of the IBM i system  @A1A

        // If the minvrm= for this element is greater than the server VRM
        // do not process this element. The item is not available at this release.
        if (getMinvrm() > hostVrm)                                  // @A1A
        {                                                           // @A1A
            return false;                                           // @A1A
        }                                                           // @A1A

        // If the maxvrm= for this element is less than the server VRM
        // do not process this element. The item is not available at this release.
        if (getMaxvrm() < hostVrm)                                  // @A1A
        {                                                           // @A1A
            return false;                                           // @A1A
        }                                                           // @A1A

        return true;                                                // @A1A
    }                                                               // @A1A


    // Convert Java object IBM i system bytes
    // Returns the number of bytes converted
    int toBytes(OutputStream bytes, int offset, PcmlDimensions indices) throws PcmlException
    {
        int totalBytes = 0;
        int myCount;
        boolean processArray;

        // Do not process if this element is not supported at the
        // VRM of the current host.
        if ( !isSupportedAtHostVRM() )                              // @A1C
            return 0;

        // If this is an array element, set up array processing information
        if (isArray() && indices.size() < getNbrOfDimensions() )
        {
            myCount = getCount(indices);
            processArray = true;
        }
        else // Non-array element, only process once.
             // Note: Although this element is not an array
             // (i.e. does not have a count= attribute)
             // It may be a child of an element that is an array.
        {
            myCount = 1;
            processArray = false;
        }

        // -----------------------------------------------------------
        // Now actually convert data to bytes
        // -----------------------------------------------------------
        for (int myIndex  = 0; myIndex < myCount; myIndex++)
        {

            if (processArray)
            {
                indices.add(myIndex);
            }

            switch (getDataType())
            {

                case PcmlData.STRUCT:
                    Enumeration children;
                    PcmlDocNode child;

                    children = getChildren();
                    while (children.hasMoreElements())
                    {
                        child = (PcmlDocNode) children.nextElement();
                        switch (child.getNodeType())
                        {
                            case PcmlNodeType.STRUCT:
                                totalBytes += ((PcmlStruct) child).toBytes(bytes, offset + totalBytes, indices);
                                break;
                            case PcmlNodeType.DATA:
                                totalBytes += ((PcmlData) child).toBytes(bytes, offset + totalBytes, indices);
                                break;
                            default:
                                throw new PcmlException(DAMRI.BAD_NODE_TYPE, new Object[] {new Integer(child.getNodeType()) , getNameForException()} );
                        } // END: switch (child.getNodeType())
                    } // END: while (children.hasMoreElements())
                    break;

                default:
                    // Convert scalar leaf node based on current dimensions
                    totalBytes += getPcmlDataValues(indices).toBytes(bytes, offset + totalBytes);
                    break;

            } // END: switch (getDataType())

            if (processArray)
            {
                indices.remove();
            }

        } // END: for myIndex

        return totalBytes;
    } // public void toBytes(OutputStream bytes, int offset, PcmlDimensions indices)


    // Parses array of bytes and stores for later conversion
    // to Java objects. This allows for lazy data translation
    // for better performance.
    // Returns the number of bytes consumed from the input byte array
    // Note: This may be larger than the number of bytes saved for this element
    //       because of bytes skipped due to an offset value.
    int parseBytes(byte[] bytes, int offset, Hashtable offsetStack, PcmlDimensions indices) throws PcmlException
    {
        PcmlData dataNode;          // Child of this element that is a <data> node
        PcmlStruct structNode;      // Child of this element that is a <struct> node
        int nbrBytes = 0;
        int myCount;
        boolean processArray;

        // Do not process if this element is not supported at the
        // VRM of the current host.
        if ( !isSupportedAtHostVRM() )                              // @A1C
            return 0;

        // If this is an array element, set up array processing information
        if (isArray() && indices.size() < getNbrOfDimensions() )
        {
            myCount = getCount(indices);
            processArray = true;
        }
        else // Non-array element, only process once.
             // Note: Although this element is not an array
             // (i.e. does not have a count= attribute)
             // It may be a child of an element that is an array.
        {
            myCount = 1;
            processArray = false;
        }

        // -----------------------------------------------------------
        // Calculate bytes to skip based on the offset=
        // and offsetfrom= attributes.
        // -----------------------------------------------------------
        int skipBytes = 0;               // Initially, no need to skip bytes            @C8A
        if (getDataType() == PcmlData.STRUCT)                                       //  @C8A
        {
            int myOffset = getOffset(indices);  // Retrieve offset value for this element   @C8A
            if (myOffset > 0)                // If this element has a non-zero offset       @C8A
            {
                // Determine from where the offset is based
                Integer myOffsetbase = null;                                            //  @C8A
                String myOffsetfromId = getOffsetfromId();      // Get offsetfrom= element name, if any    @C8A

                // If offsetfrom= was specified with the name of another element,
                // get the base for the offset from the offset stack.
                // The offset stack is a stack of beginning offsets for all
                // ancestors of the current element. The offsetfrom= value must be one
                // of these ancestors or an error will be reported.
                if (myOffsetfromId != null)                             // @C8A
                {
                    myOffsetbase = (Integer) offsetStack.get(myOffsetfromId); // @C8A
                    if (myOffsetbase == null)                           // @C8A
                    {
                        throw new PcmlException(DAMRI.OFFSETFROM_NOT_FOUND, new Object[] {myOffsetfromId, getNameForException()} ); // @C8A
                    }
                }
                else
                {
                    // If offsetfrom= was specified with an integer literal, use it.
                    if (getOffsetfrom() >= 0)                           // @C8A
                    {
                        myOffsetbase = new Integer(getOffsetfrom());    // @C8A
                    }
                    // getOffsetfrom() returns -1 to indicate that offset from was not specified.
                    // No offsetfrom= was specified, the offset will be relative to the
                    // beginning offset of the parent of this elements parent.
                    // This is the first (most recent) entry in the offset stack.
                    else
                    {
                        myOffsetbase = (Integer) offsetStack.get( ((PcmlDocNode) getParent()).getQualifiedName());  //@C8A
                    }
                }

                // Add the base value to the offset value
                if (myOffsetbase != null)                               // @C8A
                {
                    myOffset = myOffset + myOffsetbase.intValue();      // @C8A
                }

                // If the total offset value is greater than the current
                // offset into the input byte array, calculate the
                // number of bytes to skip.
                // (Bytes skipped over as a result ofthe offset=.)
                if (myOffset > offset)                                  // @C8A
                {
                    skipBytes = myOffset - offset;                      // @C8A
                }
            } // End calculating bytes to skip because of offset= attribute
        }

        // -----------------------------------------------------------
        // Now actually parse the bytes for this element
        // -----------------------------------------------------------
        for (int myIndex  = 0; myIndex < myCount; myIndex++)
        {

            if (processArray)
            {
                indices.add(myIndex);
            }

            // If this is not a structure, get the PcmlDataValues object and
            // parse the bytes
            if (getDataType() != PcmlData.STRUCT)
            {
                nbrBytes += getPcmlDataValues(indices).parseBytes(bytes, offset + skipBytes + nbrBytes, offsetStack);   // @C8C
            }
            else
            {
                Enumeration children;
                PcmlDocNode child;

                // Add this node to the offset stack
                String qName = getQualifiedName();
                if (!qName.equals(""))
                {
                    offsetStack.put(qName, new Integer(offset + skipBytes + nbrBytes)); // @C8C
                }

                children = getChildren();
                while (children.hasMoreElements())
                {
                    child = (PcmlDocNode) children.nextElement();
                    switch (child.getNodeType())
                    {
                        case PcmlNodeType.STRUCT:
                            structNode = (PcmlStruct) child;
                            nbrBytes += structNode.parseBytes(bytes, offset + skipBytes + nbrBytes, offsetStack, indices);  // @C8C
                            break;
                        case PcmlNodeType.DATA:
                            dataNode = (PcmlData) child;
                            nbrBytes += dataNode.parseBytes(bytes, offset + skipBytes + nbrBytes, offsetStack, indices);    // @C8C
                            break;
                        default:
                            throw new PcmlException(DAMRI.BAD_NODE_TYPE, new Object[] {new Integer(child.getNodeType()) , getNameForException()} );
                    } // END: switch (child.getNodeType())
                } // END: while (children.hasMoreElements())

                // Remove this node from the offset stack
                if (!qName.equals(""))
                {
                    offsetStack.remove(qName);
                }

            }

            if (processArray)
            {
                indices.remove();
            }

        } // END: for myIndex

        return nbrBytes + skipBytes;                // @C8C
    } // public int parseBytes(byte[] bytes, int offset)


    // Resolve an integer value from either a named element or a literal
    private int resolveIntegerValue(int intLiteral, String name, PcmlDimensions indices) throws PcmlException
    {
        PcmlNode node;
        PcmlData dataNode;
        Object nodeValue;

        if (name != null)
        {
            node = getDoc().getElement(name);
            if (node instanceof PcmlData)
            {
                dataNode = (PcmlData) node;
                nodeValue = dataNode.getValue(indices);
                if (nodeValue instanceof String)
                {
                    return Integer.parseInt((String) nodeValue);
                }
                else if (nodeValue instanceof Number)
                {
                    return ((Number) nodeValue).intValue();
                }
                else
                {
                    if (nodeValue == null)
                        throw new PcmlException(DAMRI.INPUT_VALUE_NOT_SET, new Object[] {dataNode.getNameForException()} );
                    else
                        throw new PcmlException(DAMRI.STRING_OR_NUMBER, new Object[] {nodeValue.getClass().getName(), dataNode.getNameForException()} );
                }
            }
            else
            {
                if (node == null)
                    throw new PcmlException(DAMRI.ELEMENT_NOT_FOUND, new Object[] {name, "<data>"} );
                else
                    throw new PcmlException(DAMRI.WRONG_ELEMENT_TYPE, new Object[] {name, "<data>"} );
            }
        }
        return intLiteral;
    }

    // Resolve a timestamp for the given indices
    private long resolveDimensionTimestamp(PcmlDimensions indices) throws PcmlException
    {
        PcmlNode node;
        String name =  getCountId();

        if (name != null)
        {
            node = getDoc().getElement(name);
            if (node instanceof PcmlData)
            {
                return ((PcmlData)node).getTimestamp(indices);
            }
            else
            {
                if (node == null)
                    throw new PcmlException(DAMRI.ELEMENT_NOT_FOUND, new Object[] {name, "<data>"} );
                else
                    throw new PcmlException(DAMRI.WRONG_ELEMENT_TYPE, new Object[] {name, "<data>"} );
            }
        }
        return Long.MIN_VALUE;
    }

    // Get the ccsid= integer literal value, if any
    public final int getCcsid()
    {
        return m_Ccsid;
    }

    // Get the ccsid= resolved element name, if any
    public final String getCcsidId()
    {
        return resolveRelativeName(m_CcsidId);
    }

    // Get the offset= unresolved element name, if any
    public final String getUnqualifiedCcsidId()                     // @C7A
    {
        return m_CcsidId;                                           // @C7A
    }

    // Get the type= value as an Integer
    public final int getDataType()
    {
        return m_Type;
    }

    // Get the type= value as a String
    public final String getDataTypeString()
    {
        return m_TypeStr;
    }

    // Get the init= string value, if any
    public final String getInit()
    {
        return m_Init;
    }

    // Get the length= integer literal value, if any
    public final int getLength()
    {
        return m_Length;
    }

    // Get the length= resolved element name, if any
    public final String getLengthId()
    {
        return resolveRelativeName(m_LengthId);
    }

    // Get the offset= unresolved element name, if any
    public final String getUnqualifiedLengthId()                    // @C7A
    {
        return m_LengthId;                                          // @C7A
    }

    // Get the maxvrm= integer value -- compatible w/ AS400.generateVRM()
    // Returns Integer.MAX_VALUE is maxvrm= was not specified
    public final int getMaxvrm()
    {
        return m_MaxvrmInt;
    }

    // Get the maxvrm= String value
    public final String getMaxvrmString()                       // @C7A
    {
        return m_Maxvrm;
    }

    // Get the minvrm= integer value  -- compatible w/ AS400.generateVRM()
    // Returns Integer.MIN_VALUE minvrm= was not specified
    public final int getMinvrm()
    {
        return m_MinvrmInt;
    }

    // Get the minvrm= String value
    public final String getMinvrmString()                       // @C7A
    {
        return m_Minvrm;
    }

    // Get the outputsize= integer literal value, if any
    public final int getOutputsize()
    {
        return m_Outputsize;
    }

    // Get the outputsize= resolved element name, if any
    public final String getOutputsizeId()
    {
        return resolveRelativeName(m_OutputsizeId);
    }

    // Get the offset= unresolved element name, if any
    public final String getUnqualifiedOutputsizeId()                // @C7A
    {
        return m_OutputsizeId;                                      // @C7A
    }

    // Get the precision= integer literal value, if any
    public final int getPrecision()
    {
        return m_Precision;
    }

    // Get the struct= element name, if any
    public final String getStruct()
    {
        return m_StructId;
    }

    // Get the keyfield= value, if any
    public boolean isKeyField()
    {
        return m_KeyField;
    }

    // Set the count= attribute
    private void setCount(String count)
    {
        // Handle null or empty string
        if (count == null || count.equals(""))
        {
            m_Count = 0;
            m_CountId = null;
            return;
        }

        // Try to parse an integer from the attribute value
        try
        {
            m_Count = Integer.parseInt(count);
            m_CountId = null;
        }
        // If value is not an integer, it must be an element name
        // checkAttributes() will be caled later to verify the element name
        catch (NumberFormatException e)
        {
            m_Count = 0;
            m_CountId = count;
        }
    }

    private void setCcsid(String ccsid)
    {
        // Handle null or empty string
        if (ccsid == null || ccsid.equals(""))
        {
            m_Ccsid = 0;
            m_CcsidId = null;
            return;
        }

        m_CcsidWasSpecified = true;                           // @D0A

        // Try to parse an integer from the attribute value
        try
        {
            m_Ccsid = Integer.parseInt(ccsid);
            m_CcsidId = null;
        }
        // If value is not an integer, it must be an element name
        // checkAttributes() will be called later to verify the element name
        catch (NumberFormatException e)
        {
            m_Ccsid = 0;
            m_CcsidId = ccsid;
        }
    }

    void setInit(String init)      // E0C
    {
// @D0D
//        // Handle null or empty string
//        if (init == null || init.equals(""))
//        {
//            m_Init = null;
//            return;
//        }

        // Save the attribute value
        m_Init = init;
        // checkAttributes() will verify the value against the data type
    }

    protected void setLength(String length)           // @D0C
    {
        // Handle null or empty string
        if (length == null || length.equals(""))
        {
            m_Length = 0;
            m_LengthId = null;
            return;
        }

        m_LengthWasSpecified = true;  // @D0A

        // Try to parse an integer from the attribute value
        try
        {
            m_Length = Integer.parseInt(length);
            m_LengthId = null;
        }
        // If value is not an integer, it must be an element name
        // checkAttributes() will be caled later to verify the element name
        catch (NumberFormatException e)
        {
            m_Length = 0;
            m_LengthId = length;
            // checkAttributes() will make sure m_LengthId resolves to a <data> element with type="int"
        }
    }

    private void setMaxvrm(String maxvrm)
    {
        m_MaxvrmInt = Integer.MAX_VALUE;
        // Handle null or empty string
        if (maxvrm == null || maxvrm.equals(""))
        {
            m_Maxvrm = null;
            return;
        }

        // Save the attribute value
        m_Maxvrm = maxvrm;
    }

    private void setMinvrm(String minvrm)
    {
        m_MinvrmInt = Integer.MIN_VALUE;
        // Handle null or empty string
        if (minvrm == null || minvrm.equals(""))
        {
            m_Minvrm = null;
            return;
        }

        m_Minvrm = minvrm;
    }

    private void setOffset(String offset)
    {
        // Handle null or empty string
        if (offset == null || offset.equals(""))
        {
            m_Offset = 0;
            m_OffsetId = null;
            return;
        }

        // Try to parse an integer from the attribute value
        try
        {
            m_Offset = Integer.parseInt(offset);
            m_OffsetId = null;
        }
        // If value is not an integer, it must be an element name
        // checkAttributes() will be caled later to verify the element name
        catch (NumberFormatException e)
        {
            m_Offset = 0;
            m_OffsetId = offset;
            // checkAttributes() will make sure m_OffsetId resolves to a <data> element with type="int"
        }
    }

    private void setOffsetfrom(String offsetfrom)
    {
        m_OffsetfromFixed = true;                                   // @A1A
        // Handle null or empty string
        if (offsetfrom == null || offsetfrom.equals(""))
        {
            m_Offsetfrom = -1;                                      // @A1A
            m_OffsetfromId = null;
            return;
        }

        try                                                         // @A1A
        {                                                           // @A1A
            m_Offsetfrom = Integer.parseInt(offsetfrom);            // @A1A
            m_OffsetfromId = null;                                  // @A1A
        }                                                           // @A1A
        catch (NumberFormatException e)                             // @A1A
        {                                                           // @A1A
            m_Offsetfrom = 0;                                       // @A1A
            m_OffsetfromId = offsetfrom;
            // checkAttributes() will make sure m_OffsetfromId resolves to a document element that
            //        an ancestor of this node.
        }                                                           // @A1A
    }

    private void setOutputsize(String outputsize)
    {
        // Handle null or empty string
        if (outputsize == null || outputsize.equals(""))
        {
            m_Outputsize = 0;
            return;
        }

        // Try to parse an integer from the attribute value
        try
        {
            m_Outputsize = Integer.parseInt(outputsize);
            m_OutputsizeId = null;
        }
        // If value is not an integer, it must be an element name
        // checkAttributes() will be caled later to verify the element name
        catch (NumberFormatException e)
        {
            m_Outputsize = 0;
            m_OutputsizeId = outputsize;
            // checkAttributes() will make sure m_OutputsizeId resolves to a <data> element with type="int"
        }
    }

    private void setPassby(String passby)                           // @B1A
    {                                                               // @B1A
        // Handle null or empty string
        if (passby == null || passby.equals(""))                    // @B1A
        {                                                           // @B1A
            m_PassbyStr = null;                                     // @B1A
            m_Passby = ProgramParameter.PASS_BY_REFERENCE;          // @B1A
            return;                                                 // @B1A
        }                                                           // @B1A

        // Save the attribute value
        m_PassbyStr = passby;                                       // @B1A
        if ( m_PassbyStr.equals("value") )                          // @B1A
        {                                                           // @B1A
            m_Passby = ProgramParameter.PASS_BY_VALUE;              // @B1A
        }                                                           // @B1A
        else // Either passby="reference" was specified or defaulted   @B1A
        {                                                           // @B1A
            m_Passby = ProgramParameter.PASS_BY_REFERENCE;          // @B1A
        }                                                           // @B1A
    }                                                               // @B1A


    private void setBidiStringType(String type)                     // @C9A
    {
        // Handle null or empty string
        if (type == null || type.equals(""))                        // @C9A
        {
            m_BidistringtypeStr = null;                             // @C9A
            m_Bidistringtype = BidiStringType.DEFAULT;              // @C9A
            return;                                                 // @C9A
        }                                                           // @C9A

        // Save the attribute value
        m_BidistringtypeStr = type;                                 // @C9A
        if ( m_BidistringtypeStr.equals("ST4") )                    // @C9A
        {
            m_Bidistringtype = BidiStringType.ST4;                  // @C9A
        }
        else if ( m_BidistringtypeStr.equals("ST5") )               // @C9A
        {
            m_Bidistringtype = BidiStringType.ST5;                  // @C9A
        }
        else if ( m_BidistringtypeStr.equals("ST6") )               // @C9A
        {
            m_Bidistringtype = BidiStringType.ST6;                  // @C9A
        }
        else if ( m_BidistringtypeStr.equals("ST7") )               // @C9A
        {
            m_Bidistringtype = BidiStringType.ST7;                  // @C9A
        }
        else if ( m_BidistringtypeStr.equals("ST8") )               // @C9A
        {
            m_Bidistringtype = BidiStringType.ST8;                  // @C9A
        }
        else if ( m_BidistringtypeStr.equals("ST9") )               // @C9A
        {
            m_Bidistringtype = BidiStringType.ST9;                  // @C9A
        }
        else if ( m_BidistringtypeStr.equals("ST10") )              // @C9A
        {
            m_Bidistringtype = BidiStringType.ST10;                 // @C9A
        }
        else if ( m_BidistringtypeStr.equals("ST11") )              // @C9A
        {
            m_Bidistringtype = BidiStringType.ST11;                 // @C9A
        }
        else                                                        // @C9A
        {
            m_Bidistringtype = BidiStringType.DEFAULT;              // @C9A
        }
    }

    private void setPrecision(String precision)
    {
        // Handle null or empty string
        if (precision == null || precision.equals(""))
        {
            m_Precision = 0;
            return;
        }

        // Try to parse an integer from the attribute value
        try
        {
            m_Precision = Integer.parseInt(precision);
        }
        // If value is not an integer, set the precision to -1
        // checkAttributes() log an error that the precision is invalid.
        catch (NumberFormatException e)
        {
            // checkAttributes() will add a PcmlSpecificationException
            m_Precision = -1;
        }
    }

    private void setStruct(String struct)
    {
        // Handle null or empty string
        if (struct == null || struct.equals(""))
        {
            m_StructId = null;
            return;
        }

        // Save the attribute value
        m_StructId = struct;
        // checkAttributes() will make sure m_StructId resolves to a document element
    }

    private void setType(String type)
    {
        m_TypeStr = type;

        // char | int | packed | zoned | float | byte | struct
        if (type.equals("char"))
            m_Type = CHAR;
        else
        if (type.equals("int"))
            m_Type = INT;
        else
        if (type.equals("packed"))
            m_Type = PACKED;
        else
        if (type.equals("zoned"))
            m_Type = ZONED;
        else
        if (type.equals("float"))
            m_Type = FLOAT;
        else
        if (type.equals("byte"))
            m_Type = BYTE;
        else
        if (type.equals("struct"))
            m_Type = STRUCT;
        else
            // checkattributes() will add a pcml specification error.
            m_Type = UNSUPPORTED;
    }

    private void setTrim(String trimEnd)                            // @D1A
    {                                                               // @D1A
        // Handle null or empty string                              // @D1A
        if (trimEnd == null || trimEnd.equals(""))                  // @D1A
        {                                                           // @D1A
            m_TrimStr = null;                                       // @D1A
            return;                                                 // @D1A
        }                                                           // @D1A

        // Save the attribute value
        m_TrimStr = trimEnd;                                        // @D1A
    }

    private void setCharType(String charType)                       // @D2A
    {                                                               // @D2A
        // Handle null or empty string                              // @D2A
        if (charType == null || charType.equals(""))                // @D2A
        {                                                           // @D2A
            m_CharType = null;                                      // @D2A
            return;                                                 // @D2A
        }                                                           // @D2A

        // Save the attribute value
        m_CharType = charType;                                      // @D2A
    }

    private void setKeyField(String keyField)
    {
        // Handle null or empty string
        if (keyField == null || keyField.equals(""))
        {
            m_KeyField = false;
            return;
        }

        // Save the attribute value
        m_KeyFieldStr = keyField;
        if (keyField.equalsIgnoreCase("true")) m_KeyField = true;
        else m_KeyField = false;
    }

    protected void checkAttributes()
    {
        //String resolvedName = null;
        PcmlDocNode resolvedNode;

        super.checkAttributes();

        // Verify the count= attribute
        // If an integer was specified for the count, no checking is needed.
        // If a document element ID was was specified, make sure
        // it resolves to a <data> element with type="int".
        if (m_CountId != null)
        {
            resolvedNode = resolveRelativeNode(m_CountId);
            if (resolvedNode == null)
            {
                getDoc().addPcmlSpecificationError(DAMRI.ATTR_REF_NOT_FOUND, new Object[] {makeQuotedAttr("count", m_CountId), getNameForException()} );
            }
            else
            {
                if (resolvedNode instanceof PcmlData)
                {
                    if ( ((PcmlData)resolvedNode).getDataType() != PcmlData.INT )
                    {
                        getDoc().addPcmlSpecificationError(DAMRI.ATTR_REF_WRONG_NODETYPE, new Object[] {makeQuotedAttr("count", m_CountId), resolvedNode.getQualifiedName(), "<data>", getNameForException()} );
                    }
                }
                else
                {
                    getDoc().addPcmlSpecificationError(DAMRI.ATTR_REF_WRONG_DATATYPE, new Object[] {makeQuotedAttr("count", m_CountId), resolvedNode.getQualifiedName(), "type=\"int\"", getNameForException()} );
                }
            }
        }
        else
        // Do not allow count= to be a literal value that is negative
        if (m_Count < 0)
        {
            getDoc().addPcmlSpecificationError(DAMRI.BAD_ATTRIBUTE_VALUE, new Object[] {makeQuotedAttr("count", m_Count), getBracketedTagName(), getNameForException()} ); // @A1C
        }


        // Verify the ccsid= attribute
        // If an integer was specified for the ccsid, no checking is needed.
        // If a document element ID was was specified, make sure
        // it resolves to a <data> element with type="int".
        if (m_IsRfml && m_CcsidWasSpecified && (getDataType() != CHAR))   // @D0A
        {
          getDoc().addPcmlSpecificationError(DAMRI.ATTRIBUTE_NOT_ALLOWED, new Object[] {makeQuotedAttr("ccsid",  getAttributeValue("ccsid")), makeQuotedAttr("type", getDataTypeString()), getBracketedTagName(), getNameForException()} );
        }
        if (m_CcsidId != null)
        {
            resolvedNode = resolveRelativeNode(m_CcsidId);
            if (resolvedNode == null)
            {
                getDoc().addPcmlSpecificationError(DAMRI.ATTR_REF_NOT_FOUND, new Object[] {makeQuotedAttr("ccsid", m_CcsidId), getNameForException()} );
            }
            else
            {
                if (resolvedNode instanceof PcmlData)
                {
                    if ( ((PcmlData)resolvedNode).getDataType() != PcmlData.INT )
                    {
                        getDoc().addPcmlSpecificationError(DAMRI.ATTR_REF_WRONG_NODETYPE, new Object[] {makeQuotedAttr("ccsid", m_CcsidId), resolvedNode.getQualifiedName(), "<data>", getNameForException()} );
                    }
                }
                else
                {
                    getDoc().addPcmlSpecificationError(DAMRI.ATTR_REF_WRONG_DATATYPE, new Object[] {makeQuotedAttr("ccsid", m_CcsidId), resolvedNode.getQualifiedName(), "type=\"int\"", getNameForException()} );
                }
            }
        }
        else
        // Do not allow ccsid= to be a literal value that is negative or greater than 65535.   @D0C
        if (m_Ccsid < 0 || m_Ccsid > 65535)  // @D0C - added check for >65535.
        {
            getDoc().addPcmlSpecificationError(DAMRI.BAD_ATTRIBUTE_VALUE, new Object[] {makeQuotedAttr("ccsid", m_Ccsid), getBracketedTagName(), getNameForException()} ); // @A1C
        }


        // Verify the init= attribute
        if (getInit() != null)
        {
            try
            {
                PcmlDataValues.convertValue((Object) getInit(), getDataType(), getLength(), getPrecision(), getNameForException());
            }
            catch (Exception e)
            {
                getDoc().addPcmlSpecificationError(DAMRI.INITIAL_VALUE_ERROR, new Object[] {getInit(), getBracketedTagName(), getNameForException()} );
            }

        }



        // Verify the length= attribute
        // If an integer was specified for the length, no checking is needed.
        // If a document element ID was was specified, make sure
        // it resolves to a <data> element with type="int".
        if (m_LengthId != null)
        {
            switch (getDataType())
            {
                case CHAR:
                case BYTE:
                    break;
                default:
                    getDoc().addPcmlSpecificationError(DAMRI.ATTRIBUTE_NOT_ALLOWED, new Object[] {makeQuotedAttr("length",  getAttributeValue("length")), getDataTypeString(), getBracketedTagName(), getNameForException()} );
            }


            resolvedNode = resolveRelativeNode(m_LengthId);
            if (resolvedNode == null)
            {
                getDoc().addPcmlSpecificationError(DAMRI.ATTR_REF_NOT_FOUND, new Object[] {makeQuotedAttr("length", m_LengthId), getNameForException()} );
            }
            else
            {
                if (resolvedNode instanceof PcmlData)
                {
                    if ( ((PcmlData)resolvedNode).getDataType() != PcmlData.INT )
                    {
                        getDoc().addPcmlSpecificationError(DAMRI.ATTR_REF_WRONG_NODETYPE, new Object[] {makeQuotedAttr("length", m_LengthId), resolvedNode.getQualifiedName(), "<data>", getNameForException()} );
                    }
                }
                else
                {
                    getDoc().addPcmlSpecificationError(DAMRI.ATTR_REF_WRONG_DATATYPE, new Object[] {makeQuotedAttr("length", m_LengthId), resolvedNode.getQualifiedName(), "type=\"int\"", getNameForException()} );
                }
            }
        }
        else
        {
            // Verify the integer literal specified for length.
            if (m_Length == -1)
            {
                getDoc().addPcmlSpecificationError(DAMRI.BAD_ATTRIBUTE_SYNTAX, new Object[] {makeQuotedAttr("length", getAttributeValue("length")), "type=\"int\"", getBracketedTagName(), getNameForException()} );
            }
            else
            {
                switch (getDataType())
                {
                    case CHAR:
                    case BYTE:
                        if ( m_Length < 0 || m_Length > (MAX_STRING_LENGTH) )
                        {
                            getDoc().addPcmlSpecificationError(DAMRI.ATTRIBUTE_NOT_ALLOWED, new Object[] {makeQuotedAttr("length",  getAttributeValue("length")), "type=\"int\"", getBracketedTagName(), getNameForException()} );
                        }
                        break;
                    case INT:
                        if (m_Length != 2 && m_Length != 4 && m_Length != 8)    // @C4C
                        {
                            getDoc().addPcmlSpecificationError(DAMRI.ATTRIBUTE_NOT_ALLOWED, new Object[] {makeQuotedAttr("length",  getAttributeValue("length")), "type=\"int\"", getBracketedTagName(), getNameForException()} );
                        }
                        break;
                    case PACKED:
                        if (m_Length < 1 || m_Length > 31)
                        {
                            getDoc().addPcmlSpecificationError(DAMRI.ATTRIBUTE_NOT_ALLOWED, new Object[] {makeQuotedAttr("length",  getAttributeValue("length")), "type=\"int\"packed", getBracketedTagName(), getNameForException()} );
                        }
                        break;
                    case ZONED:
                        if (m_Length < 1 || m_Length > 31)
                        {
                            getDoc().addPcmlSpecificationError(DAMRI.ATTRIBUTE_NOT_ALLOWED, new Object[] {makeQuotedAttr("length",  getAttributeValue("length")), "type=\"int\"zoned", getBracketedTagName(), getNameForException()} );
                        }
                        break;
                    case FLOAT:
                        if (m_Length != 4 && m_Length != 8)
                        {
                            getDoc().addPcmlSpecificationError(DAMRI.ATTRIBUTE_NOT_ALLOWED, new Object[] {makeQuotedAttr("length",  getAttributeValue("length")), "type=\"float\"", getBracketedTagName(), getNameForException()} );
                        }
                        break;
                    case STRUCT:
                        if ( getAttributeValue("length") != null
                         && !getAttributeValue("length").equals("") ) // @++C
                        {
                            getDoc().addPcmlSpecificationError(DAMRI.ATTRIBUTE_NOT_ALLOWED, new Object[] {makeQuotedAttr("length",  getAttributeValue("length")), "type=\"struct\"", getBracketedTagName(), getNameForException()} );
                        }
                        break;

                }
                // Extra logic for RFML.                                      @D0A
                if (m_IsRfml)
                {
                  // If type="struct", the 'struct' attribute is required.
                  if (getDataType() == STRUCT)
                  {
                    if (getAttributeValue("struct") == null ||
                        getAttributeValue("struct").equals(""))
                    {
                      getDoc().addPcmlSpecificationError(DAMRI.NO_STRUCT, new Object[] {makeQuotedAttr("struct", null), getBracketedTagName(), getNameForException()} );
                    }
                  }
                  // Otherwise, the 'length' attribute is required.
                  else if (!m_LengthWasSpecified)
                  {
                    getDoc().addPcmlSpecificationError(DAMRI.NO_LENGTH, new Object[] {makeQuotedAttr("length", null), getBracketedTagName(), getNameForException()} );
                  }
                }
            }
        }

        // Verify the offset= attribute
        // If an integer was specified for the offset, no checking is needed.
        // If a document element ID was was specified, make sure
        // it resolves to a <data> element with type="int".
        if (m_OffsetId != null)
        {
            resolvedNode = resolveRelativeNode(m_OffsetId);
            if (resolvedNode == null)
            {
                getDoc().addPcmlSpecificationError(DAMRI.ATTR_REF_NOT_FOUND, new Object[] {makeQuotedAttr("offset", m_OffsetId), getNameForException()} );
            }
            else
            {
                if (resolvedNode instanceof PcmlData)
                {
                    if ( ((PcmlData)resolvedNode).getDataType() != PcmlData.INT )
                    {
                        getDoc().addPcmlSpecificationError(DAMRI.ATTR_REF_WRONG_NODETYPE, new Object[] {makeQuotedAttr("offset", m_OffsetId), resolvedNode.getQualifiedName(), "<data>", getNameForException()} );
                    }
                }
                else
                {
                    getDoc().addPcmlSpecificationError(DAMRI.ATTR_REF_WRONG_DATATYPE, new Object[] {makeQuotedAttr("offset", m_OffsetId), resolvedNode.getQualifiedName(), "type=\"int\"", getNameForException()} );
                }
            }
        }
        else
        // Do not allow offset= to be a literal value that is negative
        if (m_Offset < 0)
        {
            getDoc().addPcmlSpecificationError(DAMRI.BAD_ATTRIBUTE_VALUE, new Object[] {makeQuotedAttr("offset", m_Offset), getBracketedTagName(), getNameForException()} ); // @A1C
        }

        // Verify the offsetfrom= attribute
        // If a document element ID was was specified, make sure
        // it resolves to a document element that is an ancestor of this element.
        if (m_OffsetfromId != null)
        {
            resolvedNode = resolveRelativeNode(m_OffsetfromId);
            if (resolvedNode == null)
            {
                getDoc().addPcmlSpecificationError(DAMRI.ATTR_REF_NOT_FOUND, new Object[] {makeQuotedAttr("offsetfrom", m_OffsetfromId), getNameForException()} );
            }
            else
            {
                String qName = getQualifiedName();
                if (qName.equals(""))
                {
                    qName = getNameForException();
                }
                String qNameResolved = resolvedNode.getQualifiedName();
                if (!qName.startsWith(qNameResolved + "."))
                {
                    getDoc().addPcmlSpecificationError(DAMRI.OFFSETFROM_NOT_FOUND, new Object[] {m_OffsetfromId, getNameForException()} );
                }
            }
        }
        else
        // Do not allow offsetfrom= to be a literal value that is negative
        if (m_Offsetfrom < -1)                                      // @A1A
        {                                                           // @A1A
            getDoc().addPcmlSpecificationError(DAMRI.BAD_ATTRIBUTE_VALUE, new Object[] {makeQuotedAttr("offsetfrom", m_Offsetfrom), getBracketedTagName(), getNameForException()} ); // @A1A
        }                                                           // @A1A

        // Verify the outputsize= attribute
        // If an integer was specified for the offset, make sure it is in valid range.
        // If a document element ID was was specified, make sure
        // it resolves to a <data> element with type="int".
        if (m_OutputsizeId != null)
        {
            resolvedNode = resolveRelativeNode(m_OutputsizeId);
            if (resolvedNode == null)
            {
                getDoc().addPcmlSpecificationError(DAMRI.ATTR_REF_NOT_FOUND, new Object[] {makeQuotedAttr("outputsize", m_OutputsizeId), getNameForException()} );
            }
            else
            {
                if (resolvedNode instanceof PcmlData)
                {
                    if ( ((PcmlData)resolvedNode).getDataType() != PcmlData.INT )
                    {
                        getDoc().addPcmlSpecificationError(DAMRI.ATTR_REF_WRONG_NODETYPE, new Object[] {makeQuotedAttr("outputsize", m_OutputsizeId), resolvedNode.getQualifiedName(), "<data>", getNameForException()} );
                    }
                }
                else
                {
                    getDoc().addPcmlSpecificationError(DAMRI.ATTR_REF_WRONG_DATATYPE, new Object[] {makeQuotedAttr("outputsize", m_OutputsizeId), resolvedNode.getQualifiedName(), "type=\"int\"", getNameForException()} );
                }
            }
        }
        else
        // Do not allow offset= to be a literal value that is negative
        if (m_Outputsize < 0)
        {
            getDoc().addPcmlSpecificationError(DAMRI.BAD_ATTRIBUTE_VALUE, new Object[] {makeQuotedAttr("outputsize", m_Outputsize), getBracketedTagName(), getNameForException()} ); // @A1C
        }


        // Verify the precision= attribute
        if (getAttributeValue("precision") != null
        &&  !getAttributeValue("precision").equals("") ) // @++C
        {
            switch (getDataType())
            {
                // precision= is not allowed for these data types
                case CHAR:
                case BYTE:
                case FLOAT:
                case STRUCT:
                       getDoc().addPcmlSpecificationError(DAMRI.ATTRIBUTE_NOT_ALLOWED, new Object[] {makeQuotedAttr("precision",  getAttributeValue("precision")), makeQuotedAttr("type", getDataTypeString()), getBracketedTagName(), getNameForException()} );
                    break;

                // For type=int, precision= must be 15 or 16 or 21 or 32 depending on length=
                case INT:
                    if (m_Length == 2)
                    {
                        if (m_Precision != 15 && m_Precision != 16)
                        {
                            getDoc().addPcmlSpecificationError(DAMRI.BAD_ATTRIBUTE_VALUE, new Object[] {makeQuotedAttr("precision",  getAttributeValue("precision")), getBracketedTagName(), getNameForException()} );
                        }
                    }
                    if (m_Length == 4)
                    {
                        if (m_Precision != 31 && m_Precision != 32)
                        {
                            getDoc().addPcmlSpecificationError(DAMRI.BAD_ATTRIBUTE_VALUE, new Object[] {makeQuotedAttr("precision",  getAttributeValue("precision")), getBracketedTagName(), getNameForException()} );
                        }
                    }
                    if (m_Length == 8)                              // @C4A
                    {                                               // @C4A
                        if (m_Precision != 63)                      // @C4A
                        {                                           // @C4A
                            getDoc().addPcmlSpecificationError(DAMRI.BAD_ATTRIBUTE_VALUE, new Object[] {makeQuotedAttr("precision",  getAttributeValue("precision")), getBracketedTagName(), getNameForException()} ); // @C4A
                        }                                           // @C4A
                    }                                               // @C4A
                    break;

                // For type=packed and type=zoned,
                // precision= must be >= 0 and <= the data length (length=)
                case PACKED:
                case ZONED:
                    if (m_Precision < 0 || m_Precision > m_Length)
                    {
                        getDoc().addPcmlSpecificationError(DAMRI.BAD_ATTRIBUTE_VALUE, new Object[] {makeQuotedAttr("precision",  getAttributeValue("precision")), getBracketedTagName(), getNameForException()} );
                    }
                    break;

            }

        }


        // Verify the struct= attribute
        if (m_StructId != null)
        {
            if (getDataType() != STRUCT)
            {
                   getDoc().addPcmlSpecificationError(DAMRI.ATTRIBUTE_NOT_ALLOWED, new Object[] {makeQuotedAttr("struct",  getAttributeValue("struct")), makeQuotedAttr("type", getDataTypeString()), getBracketedTagName(), getNameForException()} );
            }
        }



        // Verify the minvrm= attribute
        if (m_Minvrm != null)
        {
            m_MinvrmInt = validateVRM(m_Minvrm);
            if (m_MinvrmInt <= 0)
            {
                getDoc().addPcmlSpecificationError(DAMRI.BAD_ATTRIBUTE_VALUE, new Object[] {makeQuotedAttr("minvrm", m_Minvrm), getBracketedTagName(), getNameForException()} ); // @A1C
            }
        }


        // Verify the maxvrm= attribute
        if (m_Maxvrm != null)
        {
            m_MaxvrmInt = validateVRM(m_Maxvrm);
            if (m_MaxvrmInt <= 0)
            {
                getDoc().addPcmlSpecificationError(DAMRI.BAD_ATTRIBUTE_VALUE, new Object[] {makeQuotedAttr("maxvrm", m_Maxvrm), getBracketedTagName(), getNameForException()} ); // @A1C
            }
        }

        // Verify the passby= attribute
        if (m_PassbyStr != null)                                    // @B1A
        {                                                           // @B1A
            // Only allow this attribute when the pcml version is 2.0 or higher (e.g. <pcml version="2.0">)
            if ( getDoc().getVersion().compareTo("2.0") < 0 )       // @B1A
            {                                                       // @B1A
                getDoc().addPcmlSpecificationError(DAMRI.BAD_PCML_VERSION, new Object[] {makeQuotedAttr("passby", m_PassbyStr), "2.0", getBracketedTagName(), getNameForException()} ); // @B1A @C9C
            }                                                       // @B1A

            // Only allow this attribute when it is a child of <program>
            if ( !(getParent() instanceof PcmlProgram) )            // @B1A
            {                                                       // @B1A
                getDoc().addPcmlSpecificationError(DAMRI.NOT_CHILD_OF_PGM, new Object[] {makeQuotedAttr("passby", m_PassbyStr), getBracketedTagName(), getNameForException()} ); // @B1A
            }                                                       // @B1A
        }                                                           // @B1A

        // Verify the bidistringtype= attribute
        if (m_BidistringtypeStr != null)                            // @C9A
        {                                                           // @C9A
            // Only allow this attribute when the pcml version is 3.0 or higher (e.g. <pcml version="3.0">)
            if ( getDoc().getVersion().compareTo("3.0") < 0 )       // @C9A
            {                                                       // @C9A
                getDoc().addPcmlSpecificationError(DAMRI.BAD_PCML_VERSION, new Object[] {makeQuotedAttr("bidistringtype", m_BidistringtypeStr), "3.0", getBracketedTagName(), getNameForException()} ); // @C9A
            }                                                       // @C9A
        }

        // Verify the trim= attribute
        if (m_TrimStr != null)                                      // @D1A
        {                                                           // @D1A
            // Only allow this attribute when the pcml version is 4.0 or higher (e.g. <pcml version="4.0">)
            if ( getDoc().getVersion().compareTo("4.0") < 0 )       // @D1A
            {                                                       // @D1A
                getDoc().addPcmlSpecificationError(DAMRI.BAD_PCML_VERSION, new Object[] {makeQuotedAttr("trim", m_TrimStr), "4.0", getBracketedTagName(), getNameForException()} ); // @D1A
            }                                                       // @D1A
        }

        // Verify the chartype= attribute
        if (m_CharType != null)                                     // @D2A
        {                                                           // @D2A
            // Only allow this attribute when the pcml version is 4.0 or higher (e.g. <pcml version="4.0">)
            if ( getDoc().getVersion().compareTo("4.0") < 0 )       // @D2A
            {                                                       // @D2A
                getDoc().addPcmlSpecificationError(DAMRI.BAD_PCML_VERSION, new Object[] {makeQuotedAttr("chartype", m_CharType), "4.0", getBracketedTagName(), getNameForException()} ); // @D2A
            }                                                       // @D2A
            else
            {
                if (getDataType() != CHAR)                             // @D2A
                {
                  getDoc().addPcmlSpecificationError(DAMRI.ATTRIBUTE_NOT_ALLOWED, new Object[] {makeQuotedAttr("chartype",  getAttributeValue("chartype")), makeQuotedAttr("type", getDataTypeString()), getBracketedTagName(), getNameForException()} );
                }
            }
        }

        // Verify the keyfield= attribute  (applies only to RFML)
        if (m_KeyFieldStr != null)
        {
            // Only allow this attribute when the rfml version is 5.0 or higher (e.g. <rfml version="5.0">)
            if (!m_IsRfml)  // if not rfml, then assume it's pcml
            {
              getDoc().addPcmlSpecificationError(DAMRI.ATTRIBUTE_NOT_ALLOWED, new Object[] {makeQuotedAttr("keyfield",  getAttributeValue("keyfield")), makeQuotedAttr("pcml", getDataTypeString()), getBracketedTagName(), getNameForException()} );
            }
            if ( getDoc().getVersion().compareTo("5.0") < 0 )
            {
                getDoc().addPcmlSpecificationError(DAMRI.BAD_PCML_VERSION, new Object[] {makeQuotedAttr("keyfield", m_KeyFieldStr), "5.0", getBracketedTagName(), getNameForException()} );
            }
        }

    }


    // Check if a string is a valid IBM i system VRM
    // Allowed syntax:
    //    "VxRyMz"
    //
    // where:
    //    "V", "R" and "M" are literal characters
    //    x is an integer from 1 to 255
    //    y is an integer from 0 to 255
    //    z is an integer from 0 to 255
    //
    // If valid, a positive integer is returned. This is the value generated
    // by com.ibm.as400.access.AS400.generateVRM().
    // If invalid, -1 is returned.
    private static final String vrmDelimChars = "VRM";
    static int validateVRM(String vrmStr)
    {
        StringTokenizer vrmTokens, vrmDelimiters;

        int as400vrm = -1;
        int[] vrm = new int[] {-1, -1, -1};

        vrmTokens = new StringTokenizer(vrmStr, vrmDelimChars, true);
        int vrmTokenNbr = 0;
        vrmDelimiters = new StringTokenizer(vrmDelimChars, vrmDelimChars, true);

        if (vrmTokens.countTokens() == 6)
        {
            while (vrmDelimiters.hasMoreTokens())
            {
                if (vrmTokens.nextToken().equals(vrmDelimiters.nextToken()))
                {
                    try
                    {
                        vrm[vrmTokenNbr] = Integer.parseInt(vrmTokens.nextToken());
                        vrmTokenNbr++;
                    }
                    catch (NumberFormatException e)
                    { }
                }
            }
        }

        // If all of the integers are within allowed ranges
        // generate the VRM integer to be returned.
        if ( vrm[0] >= 1 && vrm[0] <= 255
          && vrm[1] >= 0 && vrm[0] <= 255
          && vrm[2] >= 0 && vrm[0] <= 255)
        {
            as400vrm = AS400.generateVRM(vrm[0],vrm[1],vrm[2]);
        }

        return as400vrm;
    }

}
