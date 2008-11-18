///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PcmlStruct.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import com.ibm.as400.access.ProgramParameter;

import java.util.Enumeration;
import java.util.Hashtable;
import java.io.OutputStream;

class PcmlStruct extends PcmlDocNode
{
    /***********************************************************
     Static Members
    ***********************************************************/

    // Serial verion unique identifier
    static final long serialVersionUID = 5539999574454926624L;

    private static final String STRUCTATTRIBUTES[] = {
        "name",  
        "usage", 
        "count", 
        "minvrm",
        "maxvrm",
        "offset",    
        "offsetfrom",
        "outputsize"
    };
    
    private static final int VERSION_1_ATTRIBUTE_COUNT = 8;

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

    // Default constructor
    PcmlStruct()
    {
    }

    // Constructor
    public PcmlStruct(PcmlAttributeList attrs)                      // @C1C
    {
        super(attrs);                                               // @C1C
        setNodeType(PcmlNodeType.STRUCT);                           // @C1A

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

        // Set outputsize= member variable
        setOutputsize(getAttributeValue("outputsize"));

        // Set minvrm= attribute value
        setMinvrm(getAttributeValue("minvrm"));

        // Set maxvrm= attribute value
        setMaxvrm(getAttributeValue("maxvrm"));
    }

   /**
    * Return the list of valid attributes for the data element.  
    **/
    String[] getAttributeList()                                 // @C7A
    {
        return STRUCTATTRIBUTES;                                // @C7A
    }

    int getCount(PcmlDimensions indices) throws PcmlException
    {
        return resolveIntegerValue( getCount(),
                                    getCountId(),
                                    indices );
    }

    // @D1 --New XPCML method
    // @D1 -- Get the run-time dimension for this element but don't throw an exception if count not set
    final int getXPCMLCount(PcmlDimensions indices) throws PcmlException      //@D1
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


    // Returns an array of integers containing the array dimensions
    // Notes:
    //      getDimensions().length == 0 for scalar data
    PcmlDimensions getDimensions(PcmlDimensions indices) throws PcmlException
    {
        PcmlDimensions myDimensions = null;
        PcmlNode node = getParent();                            // @C8A

        // Retrieve array dimensions from all ancestors
        if (node instanceof PcmlData)                           // @C8C
        {
            myDimensions = ((PcmlData) node).getDimensions(indices);    // @C8C
        }
        else
        if  (node instanceof PcmlStruct)                        // @@C8C
        {
            myDimensions = ((PcmlStruct) node).getDimensions(indices);  // @C8C
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
        PcmlNode node = getParent();                                // @C8A

        // If an array is defined at this node,
        // remove its dimension from the array of indices
        if (isArray())
        {
            myIndex = indices.integerAt(indices.size()-1);
            indices.remove();
        }

        // Retrieve array dimensions from all ancestors
        if (node instanceof PcmlData)                               // @C8C
        {
            previousTimestamps = ((PcmlData) node).getDimensionTimestamps(indices); // @C8C
        }
        else
        if  (node instanceof PcmlStruct)                            // @C8C
        {
            previousTimestamps = ((PcmlStruct) node).getDimensionTimestamps(indices);   // @C8C
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
        PcmlNode node = getParent();                        // @C8A

        if (isArray())
            total++;

        if (node instanceof PcmlData)                       // @C8C
            total += ((PcmlData)node).getNbrOfDimensions(); // @C8C
        else
            if (node instanceof PcmlStruct)                 // @C8C
                total += ((PcmlStruct)node).getNbrOfDimensions();   // @C8C

        return total;
    }

    // Get the run-time offset value for this element
    int getOffset(PcmlDimensions indices) throws PcmlException
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


    // Get the offsetfrom= integer literal value, if any
    final int getOffsetfrom()                                       // @A1C
    {
        if ( isOffsetfromFixed() )                                  // @A1A
            return m_Offsetfrom;                                    // @A1C
        else                                                        // @A1A
            return -1;                                              // @A1A
    }

    // Get the offsetfrom= resolved element name, if any
    final String getOffsetfromId()
    {
        return resolveRelativeName(m_OffsetfromId);
    }

    // Get the offsetfrom= unresolved element name, if any
    public final String getUnqualifiedOffsetfromId()                // @C7A
    {
        return m_OffsetfromId;                                      // @C7A
    }

    // Return number of bytes to allocate in outpout buffer
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

            if (processArray)
            {
                indices.remove();
            }

        } // END: for myIndex

        return totalSize;
    }


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
        PcmlNode node = getParent();                            // @C8A
        if (isArray())
            return true;
        else
            if (node instanceof PcmlData)                       // @C8C
                return ((PcmlData)node).isArray();              // @C8C
            else
                if (node instanceof PcmlStruct)                 // @C8C
                    return ((PcmlStruct)node).isArray();        // @C8C
                else
                    return false;
    }

    public int getCount()
    {
        return m_Count;
    }

    public String getCountId()
    {
        return resolveRelativeName(m_CountId);
    }

    // Get the count= unresolved element name, if any
    public final String getUnqualifiedCountId()                      // @C7A
    {
        return m_CountId;                                          // @C7A
    }

    // Get the maxvrm= integer value -- compatible w/ AS400.generateVRM()
    // Returns Integer.MAX_VALUE is maxvrm= was not specified
    public final int getMaxvrm()
    {
        return m_MaxvrmInt;
    }

    // Get the maxvrm= String value
    public final String getMaxvrmString()               // @C7A
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
    public final String getMinvrmString()               // @C7A
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

    // Get the outputsize= unresolved element name, if any
    public final String getUnqualifiedOutputsizeId()                // @C7A
    {
        return m_OutputsizeId;                                      // @C7A
    }

    private void setCount(String count)
    {
        if (count == null || count.equals(""))
        {
            m_Count = 0;
            m_CountId = null;
            return;
        }

        try
        {
            m_Count = Integer.parseInt(count);
            m_CountId = null;
        }
        catch (NumberFormatException e)
        {
            m_Count = 0;
            m_CountId = count;
            // checkAttributes() will make sure m_CountId resolves to a <data> element with type="int"
        }
    }

    private void setMaxvrm(String maxvrm)
    {
        m_MaxvrmInt = Integer.MAX_VALUE;
        if (maxvrm == null || maxvrm.equals(""))
        {
            m_Maxvrm = null;
            return;
        }

        m_Maxvrm = maxvrm;
    }

    private void setMinvrm(String minvrm)
    {
        m_MinvrmInt = Integer.MIN_VALUE;
        if (minvrm == null || minvrm.equals(""))
        {
            m_Minvrm = null;
            return;
        }

        m_Minvrm = minvrm;
    }

    private void setOffset(String offset)
    {
        if (offset == null || offset.equals(""))
        {
            m_Offset = 0;
            m_OffsetId = null;
            return;
        }

        try
        {
            m_Offset = Integer.parseInt(offset);
            m_OffsetId = null;
        }
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
                if (qName.equals(""))                               // @A1A
                {                                                   // @A1A
                    qName = getNameForException();                  // @A1A
                }                                                   // @A1A
                String qNameResolved = resolvedNode.getQualifiedName();
                if (!qName.startsWith(qNameResolved + "."))
                {
                    getDoc().addPcmlSpecificationError(DAMRI.OFFSETFROM_NOT_FOUND, new Object[] {m_OffsetfromId, getNameForException()} );
                }
            }
        }
        else                                                        // @A1A
        // Do not allow offsetfrom= to be a literal value that is negative
        if (m_Offsetfrom < -1)                                      // @A1A
        {                                                           // @A1A
            getDoc().addPcmlSpecificationError(DAMRI.BAD_ATTRIBUTE_VALUE, new Object[] {makeQuotedAttr("offsetfrom", m_Offsetfrom), getBracketedTagName(), getNameForException()} ); // @A1A
        }                                                           // @A1A

        // Verify the minvrm= attribute
        if (m_Minvrm != null)
        {
            m_MinvrmInt = PcmlData.validateVRM(m_Minvrm);
            if (m_MinvrmInt <= 0)
            {
                getDoc().addPcmlSpecificationError(DAMRI.BAD_ATTRIBUTE_VALUE, new Object[] {makeQuotedAttr("minvrm", m_Minvrm), getBracketedTagName(), getNameForException()} ); // @A1C
            }
        }


        // Verify the maxvrm= attribute
        if (m_Maxvrm != null)
        {
            m_MaxvrmInt = PcmlData.validateVRM(m_Maxvrm);
            if (m_MaxvrmInt <= 0)
            {
                getDoc().addPcmlSpecificationError(DAMRI.BAD_ATTRIBUTE_VALUE, new Object[] {makeQuotedAttr("maxvrm", m_Maxvrm), getBracketedTagName(), getNameForException()} ); // @A1C
            }
        }

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

    }


    // Returns true if this document element is supported at the
    // at the VRM of the current host.
    // Returns false if not.
    boolean isSupportedAtHostVRM() throws PcmlException             // @A1A
    {                                                               // @A1A
        int hostVrm = getAs400VRM();      // VRM of the i5/OS system  @A1A

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

    // Convert data to i5/OS system format
    // Returns the number of bytes to'ed
    int toBytes(OutputStream bytes, int offset, PcmlDimensions indices) throws PcmlException
    {
        Enumeration children;
        PcmlDocNode child;
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

        for (int myIndex  = 0; myIndex < myCount; myIndex++)
        {

            if (processArray)
            {
                indices.add(myIndex);
            }

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

            if (processArray)
            {
                indices.remove();
            }

        }

        return totalBytes;
    } // END: public int toBytes(OutputStream bytes, int offset, PcmlDimensions indices)

    // Parses array of bytes and stores for later conversion
    // to Java objects. This allows for lazy data translation
    // for performance.
    // Returns the number of bytes consumed from the input byte array
    // Note: This may be larger than the number of bytes saved for this element
    //       because of bytes skipped due to an offset value.
    int parseBytes(byte[] bytes, int offset, Hashtable offsetStack, PcmlDimensions indices) throws PcmlException
    {
        PcmlData dataNode;               // Child of this element that is a <data> node
        PcmlStruct structNode;           // Child of this element that is a <struct> node
        int nbrBytes = 0;               // Number of bytes consumed from input byte array
        Enumeration children;           // Enumeration of children of this <struct> element
        PcmlDocNode child;               // Current child node being processed
        boolean processArray;          // Indicates whether this element is an array
        int myCount;                   // Number of array elements to process,
                                       // or set to 1 for non-array elements

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
        int skipBytes = 0;               // Initially, no need to skip bytes
        int myOffset = getOffset(indices);  // Retrieve offset value for this element
        if (myOffset > 0)                // If this element has a non-zero offset
        {
            // Determine from where the offset is based
            Integer myOffsetbase = null;
            String myOffsetfromId = getOffsetfromId();      // Get offsetfrom= element name, if any    @A1C

            // If offsetfrom= was specified with the name of another element,
            // get the base for the offset from the offset stack.
            // The offset stack is a stack of beginning offsets for all
            // ancestors of the current element. The offsetfrom= value must be one
            // of these ancestors or an error will be reported.
            if (myOffsetfromId != null)                             // @A1C
            {
                myOffsetbase = (Integer) offsetStack.get(myOffsetfromId); // @A1C
                if (myOffsetbase == null)
                {
                    throw new PcmlException(DAMRI.OFFSETFROM_NOT_FOUND, new Object[] {myOffsetfromId, getNameForException()} ); // @A1C
                }
            }
            else
            {
                // If offsetfrom= was specified with an integer literal, use it.
                if (getOffsetfrom() >= 0)                           // @A1A
                {                                                   // @A1A
                    myOffsetbase = new Integer(getOffsetfrom());    // @A1A
                }                                                   // @A1A
                // getOffsetfrom() returns -1 to indicate that offset from was not specified.
                // No offsetfrom= was specified, the offset will be relative to the
                // beginning offset of the parent of this elements parent.
                // This is the first (most recent) entry in the offset stack.
                else                                                // @A1A
                {                                                   // @A1A
                    myOffsetbase = (Integer) offsetStack.get( ((PcmlDocNode) getParent()).getQualifiedName());
                }                                                   // @A1A
            }

            // Add the base value to the offset value
            if (myOffsetbase != null)
            {
                myOffset = myOffset + myOffsetbase.intValue();
            }

            // If the total offset value is greater than the current
            // offset into the input byte array, calculate the
            // number of bytes to skip.
            // (Bytes skipped over as a result ofthe offset=.)
            if (myOffset > offset)
            {
                skipBytes = myOffset - offset;
            }
        } // End calculating bytes to skip because of offset= attribute

        // -----------------------------------------------------------
        // Now actually parse the bytes for this element
        // -----------------------------------------------------------
        for (int myIndex  = 0; myIndex < myCount; myIndex++)
        {

            // -----------------------------------------------------------
            // Add this node to the offset stack
            // This element's current offset is put in the stack for use by descendent elements
            // to resolve their offset= attributes.
            // -----------------------------------------------------------
            String qName = getQualifiedName();
            if (!qName.equals(""))
            {
                offsetStack.put(qName, new Integer(offset + skipBytes + nbrBytes));    // pva 10/30
            }

            // Add the current index for this dimension to the indices
            if (processArray)
            {
                indices.add(myIndex);
            }

            // Process each child element in this structure.
            children = getChildren();
            while (children.hasMoreElements())
            {
                child = (PcmlDocNode) children.nextElement();
                switch (child.getNodeType())
                {
                    case PcmlNodeType.STRUCT:
                        structNode = (PcmlStruct) child;
                        nbrBytes += structNode.parseBytes(bytes, offset + skipBytes + nbrBytes, offsetStack, indices);
                        break;
                    case PcmlNodeType.DATA:
                        dataNode = (PcmlData) child;
                        nbrBytes += dataNode.parseBytes(bytes, offset + skipBytes + nbrBytes, offsetStack, indices);
                        break;
                    default:
                        throw new PcmlException(DAMRI.BAD_NODE_TYPE, new Object[] {new Integer(child.getNodeType()) , getNameForException()} );
                } // END: switch (child.getNodeType())
            } // END: while (children.hasMoreElements())

            // Remove the current index for this dimension from the indices
            if (processArray)
            {
                indices.remove();
            }
            // -----------------------------------------------------------
            // Remove this node from the offset stack
            // -----------------------------------------------------------
            if (!qName.equals(""))
            {
                offsetStack.remove(qName);
            }

        } // END: for myIndex


        return nbrBytes + skipBytes;
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
}
