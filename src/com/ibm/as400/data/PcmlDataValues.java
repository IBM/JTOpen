///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PcmlDataValues.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import com.ibm.as400.access.AS400DataType;
import com.ibm.as400.access.AS400Text;
import com.ibm.as400.access.BidiStringType;
import com.ibm.as400.access.Trace;                                  // @D3A

import java.math.BigDecimal;

import java.io.IOException;                                         // @C1A
import java.io.ObjectInputStream;                                   // @C1A
import java.io.ObjectOutputStream;                                  // @C1A
import java.io.Serializable;                                        // @C1A

import java.util.Hashtable;
import java.util.StringTokenizer;

class PcmlDataValues extends Object implements Serializable         // @C1C
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static final long serialVersionUID = -8169008879805188674L;	    // @C1A

    private PcmlData m_owner;      // PcmlData node that owns this object
    
    private PcmlDimensions m_indices; // Indices into owning PcmlNode's vectors of PcmlDataValues

    private Object m_value;        // Java native value: (String, Short, Integer, Long, Float, Double, BigDecimal. byte[])
    private long   m_valueTs;      // Correllation id of Java native value from PcmlDocument.getCorrellationID()
    private byte[] m_bytes;        // i5/OS bytes (ebcdic/big-endian)
    private long   m_bytesTs;      // Correllation id of i5/OS bytes from PcmlDocument.getCorrellationID()
    
    private int    m_bidiStringType;    // Type of string that is contained in the value @C6A

    // Default constructor
    private PcmlDataValues() 
    {
        m_value = null;
        m_bytes = null;
        m_valueTs = 0;
        m_bytesTs = 0;
        m_bidiStringType = BidiStringType.DEFAULT;                  // @C6A
    }

    // Constructor 
    PcmlDataValues(PcmlData creator, PcmlDimensions indices) 
    {
        this();
        m_owner = creator;
        m_indices = new PcmlDimensions(indices);
    }

    // Custom serialization
    private void writeObject(ObjectOutputStream out) throws IOException // @C1A
    {                                                               // @C1A
        synchronized (this)                                         // @C1A
        {                                                           // @C1A
            // For input values, serialize only the i5/OS byte value 
            // if it is current than the Java value
            if ( m_owner.getUsage() == PcmlDocNode.INPUT )          // @C1A
            {                                                       // @C1A
                // If the byte value is more recent than the Java value
                // convert the bytes to a Java value
                if (m_bytesTs > m_valueTs)                          // @C1A
                {                                                   // @C1A
                    m_value = null;                                 // @C1A
                    m_valueTs = 0;                                  // @C1A
                }                                                   // @C1A
            }                                                       // @C1A

            // For ouput values, serialize only the Java value 
            // if it is current than the i5/OS byte value
            if ( m_owner.getUsage() == PcmlDocNode.OUTPUT )         // @C1A
            {
                // If the byte value is more recent than the Java value
                // convert the bytes to a Java value
                if (m_valueTs > m_bytesTs)                          // @C1A
                {                                                   // @C1A
                    m_bytes = null;                                 // @C1A
                    m_bytesTs = 0;                                  // @C1A
                }                                                   // @C1A
            }                                                       // @C1A

            // Perform default serialization
            out.defaultWriteObject();                               // @C1A
        } // end of synchronized code                               // @C1A
    }                                                               // @C1A

	// Custom deserialization
	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException                  // @C1A
	{                                                               // @C1A
		// Default deserialization
		in.defaultReadObject();                                     // @C1A
	}                                                               // @C1A

	// Custom deserialization post-processing
	// This processing cannot be done during readObject() because
	// references to parent objects in the document are not yet set
	// due to the recursive nature of deserialization.
    void readObjectPostprocessing()                                 // @C1A
    {                                                               // @C1A
		if (m_valueTs == m_bytesTs && m_valueTs > 0)                // @C1A
		{                                                           // @C1A
	        m_valueTs = m_owner.getDoc().getDeserializationTimestamp(); // @C1A
	        m_bytesTs = m_owner.getDoc().getDeserializationTimestamp(); // @C1A
		}                                                           // @C1A
		if (m_valueTs > m_bytesTs)                                  // @C1A
		{                                                           // @C1A
	        m_valueTs = m_owner.getDoc().getDeserializationTimestamp(); // @C1A
	        m_bytesTs = 0;                                          // @C1A
		}                                                           // @C1A
		if (m_bytesTs > m_valueTs)                                  // @C1A
		{                                                           // @C1A
	        m_bytesTs = m_owner.getDoc().getDeserializationTimestamp(); // @C1A
	        m_valueTs = 0;                                          // @C1A
		}                                                           // @C1A
    }                                                               // @C1A

    // Flush data values
    // This allows the object to be reused when the
    // m_owner (PcmlData object) redimensions its vectors.
    void flushValues()
    {
        m_value = null;
        m_bytes = null;
        m_valueTs = 0;
        m_bytesTs = 0;
    }

    // Get qualified name of data object
    String getQualifiedName() 
    {
        return m_owner.getQualifiedName();
    }

    // Get name for exceptions
    String getNameForException() 
    {
        return m_owner.getNameForException();
    }


    // Get Timestamp of data
    long getTimestamp() 
    {
        return Math.max(m_valueTs, m_bytesTs);
    }

    // Get Java native value
    public Object getValue() throws PcmlException 
    {
        return bytesToValue();
    }

    // Set Java native value
    public void setValue(Object v) throws PcmlException 
    {
        // Do not allow a null value to be set
        if (v == null)
            throw new PcmlException(DAMRI.NULL_VALUE, new Object[] {getNameForException()} );

        // If the new value matches the Java type for this element
        // store the new value.
        if (getDataType() == PcmlData.STRUCT) {                    // @D0A
            throw new PcmlException(DAMRI.STRUCT_VALUE, new Object[] {getNameForException()} );   // @D0A
        }
        if ( v.getClass().equals(getValueClass()) ) 
        {
            ///m_value = v;
            if (v instanceof BigDecimal) {
              m_value = ((BigDecimal)v).setScale(getPrecision(), BigDecimal.ROUND_HALF_EVEN);       // @D0A
            }
            else {
            m_value = v;
        }
        }
        // New value does not match the Java typ for this element.
        // Convert to the Java type needed -- errors may occur.
        else 
        {
            m_value = convertValue(v, getDataType(), getLength(), getPrecision(), getNameForException());
        }
        // Update the value timestamp
        m_valueTs = PcmlDocument.getCorrellationID();           // @C7C
    }

    // Set i5/OS bytes
    public void setBytes(byte[] ba) 
    {
        m_bytes = ba;
        m_bytesTs = PcmlDocument.getCorrellationID();           // @C7C
    }

    // Set the bidi string type 
    public void setStringType(int type)             // @C6A
    {
        if (m_bidiStringType == type)               // @C6A
        {
            // If new type is same as old, just return
            return;                                 // @C6A
        }
        // If we are setting a new string type, we want to 
        // clear the java value and timestamp.  This will ensure
        // that the correct conversion occurs on subsequent
        // getValue() calls
        m_bidiStringType = type;                    // @C6A
        m_value = null;                             // @C6A
        m_valueTs = 0;                              // @C6A

    }

    // Convert i5/OS bytes to Java native value
    private Object bytesToValue() throws PcmlException 
    {
        // If the byte value is more recent than the Java value
        // convert the bytes to a Java value
        if (m_bytesTs > m_valueTs) 
        {
            m_valueTs = m_bytesTs;  // Update value timestamp
            toObject(m_bytes);
        }

        // If value is not set and there is an
        // init= value specified, initialize the value
        if (m_value == null) 
        {
            String initValue = m_owner.getInit();
            if (initValue != null)
                setValue(initValue);
        }
        
        if (m_value == null)    // If value has not been set       @C5A
            return m_value;     // return null                     @C5A
                
        // Return the Java value
        if (getDataType() == PcmlData.BYTE)
        {
            // Make a copy of the byte array, so caller can 
            // modify the value without repercussions
            byte[] srcArray = (byte[]) m_value;
            byte[] cloneArray = new byte[srcArray.length];
            for (int b = 0; b<srcArray.length; b++)
            {
                cloneArray[b] = srcArray[b];
            }
            return cloneArray;
        }
        else
        {
            // No need to make copy for other data type because they
            // are immutable objects.
            return m_value;          
        }
    }

    int getDataType()
    {
        return m_owner.getDataType();
    }

    int getLength() throws PcmlException
    {
        String charType = m_owner.getCharType();                    // @D2A
        if ((charType != null) && (charType.equals("twobyte")))
        {
            return (resolveIntegerValue( m_owner.getLength(),
                                        m_owner.getLengthId() ) * 2);    // @D2A
        }
        else
        {
        return resolveIntegerValue( m_owner.getLength(),
                                    m_owner.getLengthId() );
    }
    }

    int getOffset() throws PcmlException
    {
        return resolveIntegerValue( m_owner.getOffset(),
                                    m_owner.getOffsetId() );
    }

    int getCcsid() throws PcmlException
    {
        int tmpCcsid = resolveIntegerValue( m_owner.getCcsid(),
                                            m_owner.getCcsidId() );
        // If a CCSID is not explicitly defined for this element,
        // use the CCSID from the <program> element
        if (tmpCcsid == 0)
        {                                                           // @C2A
            PcmlNode node = m_owner.getParent();                    // @C2A
            node = node.getParent();    // get next ancestor           @C7A
            while (node != m_owner.getDoc())                        // @C2A @C7C
                node = node.getParent();                            // @C2A
            // If this element is a descendent of a Program element
            // use the CCSID saved the last time the 
            if (node instanceof PcmlProgram)                        // @C2A
                return ((PcmlProgram) node).getProgramCCSID();      // @C2A
            else if (node instanceof RfmlDocument)                  // @D0A
                return ((RfmlDocument) node).getCcsidInt();         // @D0A
            else                                                    // @C2A
                return m_owner.getDoc().getAs400().getCcsid();      // @C2A
        }                                                           // @C2A
        else
            return tmpCcsid;
    }


    // Returns the precision= attribute
    // The precision is only meaningful when type=int or packed
    // For type=int:
    //   A 2-byte signed integer is:   length=2 precision=15
    //   A 2-byte unsigned integer is: length=2 precision=16
    //   A 4-byte signed integer is:   length=4 precision=31
    //   A 4-byte unsigned integer is: length=4 precision=32
    // For type=packed:
    //   length= species the total number of digits
    //   precision= specifies the number of decimal digits
    int getPrecision()
    {
        return m_owner.getPrecision();
    }

    String getTrim()                                                // @D1A
    {                                                               // @D1A
        return m_owner.getTrim();                                   // @D1A
    }                                                               // @D1A

    boolean isArray()
    {
        return m_owner.isArray();
    }

    // Return number of bytes required to perform toBytes();
    // This is similar to C++ DataObject::FlattenedLengthAt()
    public int byteLength() throws PcmlException 
    {

        int length = getLength();

        switch (getDataType()) 
        {
            case PcmlData.CHAR:
                return length;

            case PcmlData.INT:
                return length;

            case PcmlData.PACKED:
                return length / 2 + 1; // Packed contains 2 digits per byte plust sign

            case PcmlData.ZONED:
                return length;        // Zoned contains 1 digits per byte, sign is in low order digit
                
            case PcmlData.FLOAT:
                return length;

            case PcmlData.BYTE:
                return length;

            default:
                throw new PcmlException(DAMRI.BAD_DATA_TYPE, new Object[] {new Integer(getDataType()) , getNameForException()} );

        }
    }

    // Converts an object to the correct type (class) for
    // the defined data tpe.
    // The SetValue method allows most data types to be set using 
    // either the Java class associaed with the data type or using
    // a String. This method converts the value to the Java class 
    // defined for the data type. Exceptions such as 
    // NumberFormatException may result.
    private Class getValueClass() throws PcmlException 
    {
        int dataLength = getLength();
        int dataPrecision = getPrecision();
        int dataType = getDataType();

        try 
        {
            switch (dataType) 
            {
    
                case PcmlData.CHAR:
                    return Class.forName("java.lang.String");
    
                case PcmlData.INT:
                    if (dataLength == 2) 
                    {
                        if (dataPrecision == 16) 
                        {
                            return Class.forName("java.lang.Integer");
                        }
                        else 
                        { // dataPrecision == 15 or defaulted
                            return Class.forName("java.lang.Short");
                        }
                    }
                    else 
                    if (dataLength == 4)                            // @C3A
                    { // dataLength == 4
                        if (dataPrecision == 32) 
                        {
                            return Class.forName("java.lang.Long");
                        }
                        else 
                        { // dataPrecision == 31 or defaulted
                            return Class.forName("java.lang.Integer");
                        }
                    }
                    else                                            // @C3A
                    if (dataLength == 8)                            // @C3A
                    { // dataLength == 8                            // @C3A
                        return Class.forName("java.lang.Long");     // @C3A
                    }                                               // @C3A
    
                case PcmlData.PACKED:
                case PcmlData.ZONED:
                    return Class.forName("java.math.BigDecimal");
    
    
                case PcmlData.FLOAT:
                    if (dataLength == 4) 
                    {
                        return Class.forName("java.lang.Float");
                    }
                    else 
                    { // Must be length=8
                        return Class.forName("java.lang.Double");
                    }
    
                case PcmlData.BYTE:
                    return (new byte[0]).getClass();
    
    
                default:
                    throw new PcmlException(DAMRI.BAD_DATA_TYPE, new Object[] {new Integer(getDataType()) , getNameForException()} );
    
            } // END: switch (getDataType())
        }
        catch (ClassNotFoundException e) 
        {
            throw new PcmlException(DAMRI.CLASS_NOT_FOUND, new Object[] {new Integer(getDataType()) , getNameForException()} );
        }
    }

    // Convert Java object i5/OS bytes
    // Returns the number of bytes converted
    public int toBytes(byte[] bytes, int offset) throws PcmlException 
    {
        Object value = null;
        int dataType = getDataType();
        int dataLength = getLength();
        int dataPrecision = getPrecision();
        int bytesConverted = 0;                                     // @B2A

        // Get the Java object and make sure it has been set.
        value = getValue();
        if (value == null) 
        {
            throw new PcmlException(DAMRI.INPUT_VALUE_NOT_SET, new Object[] {getNameForException()} );
        }
        
        // Get a converter from the PcmlDocument node
        // PcmlDocument will either create a converter or return 
        // and existing one.
        AS400DataType converter = m_owner.getDoc().getConverter(dataType, dataLength, dataPrecision, getCcsid());
        if (dataType != PcmlData.CHAR)                              // @C6A
        {
            synchronized(converter)                                 // @B1A
            {                                                       // @B1A
                converter.toBytes(value, bytes, offset);
                bytesConverted = converter.getByteLength();         // @B2A
            }                                                       // @B1A
        }
        else    // We need to use the string type for the conversion   @C6A
        {
            synchronized(converter)                                 // @C6A
            {
                ((AS400Text)converter).toBytes(value, bytes, offset, m_bidiStringType);  // @C6A
                bytesConverted = converter.getByteLength();         // @C6A
            }
        }

        // Detail tracing of data conversion
        if (Trace.isTracePCMLOn())                                  // @D3C
        {                                                           // @B2A
            String parseMsg;                                        // @B2A
            if (m_indices.size() > 0)                               // @B2A
            {                                                       // @B2A
                parseMsg = SystemResourceFinder.format(DAMRI.WRITE_DATA_W_INDICES, new Object[] {Integer.toHexString(offset), Integer.toString(bytesConverted), getNameForException(), m_indices.toString(), PcmlMessageLog.byteArrayToHexString(bytes, offset, bytesConverted)} ); // @B2A
            }                                                       // @B2A
            else                                                    // @B2A
            {                                                       // @B2A
                parseMsg = SystemResourceFinder.format(DAMRI.WRITE_DATA,           new Object[] {Integer.toHexString(offset), Integer.toString(bytesConverted), getNameForException(), PcmlMessageLog.byteArrayToHexString(bytes, offset, bytesConverted)} ); // @B2A
            }                                                       // @B2A
            parseMsg = parseMsg + "\t  " + Thread.currentThread();  // @B2A
            Trace.log(Trace.PCML, parseMsg);                        // @D3C
        }                                                           // @B2A
            
        return bytesConverted;                                      // @B2A
    } // public int toBytes(byte[] bytes, int offset)

    // Convert i5/OS bytes to Java Object
    // Returns the Java Object
    public Object toObject(byte[] bytes) throws PcmlException
    {
        Object newVal = null;
        int dataType = getDataType();
        int dataLength = getLength();
        int dataPrecision = getPrecision();
        
        // Get a converter from the PcmlDocument node
        // PcmlDocument will either create a converter or return 
        // and existing one.
        AS400DataType converter = m_owner.getDoc().getConverter(dataType, dataLength, dataPrecision, getCcsid());
        if (dataType != PcmlData.CHAR)                              // @C6A
        {
            synchronized(converter)                                 // @B1A
            {                                                       // @B1A
                newVal = converter.toObject(bytes);
            }                                                       // @B1A
        }
        else    // We need to use the string type for the conversion   @C6A
        {
            synchronized(converter)                                 // @C6A
            {
                newVal = ((AS400Text)converter).toObject(bytes, 0, m_bidiStringType);    // @C6A
            }
        }
        
        // For Strings, trim blanks and nulls appropriately
        if (dataType == PcmlData.CHAR) 
        {
            newVal = trimString((String) newVal, getTrim());                   // @B2A @D1C
        }
        
        // Set the new value
        setValue(newVal);
        
        return newVal;
    } // public int toBytes(byte[] bytes, int offset)

    // Parses array of bytes and stores for later conversion
    // to Java objects. This allows for lazy data translation
    // for performance.
    // Returns the number of bytes consumed from the input byte array
    // Note: This may be larger than the number of bytes saved for this element
    //       because of bytes skipped due to an offset value.
    public int parseBytes(byte[] bytes, int offset, Hashtable offsetStack) throws PcmlException 
    {
        int nbrBytes = 0;
        int skipBytes = 0;


        // Adjust offset if necessary
        // This is driven by the offset= attribute on the <data> tag
        int myOffset = getOffset();
        if (myOffset != 0) 
        {
            // Handle errors caused by bad offset values
            if (myOffset < 0 || myOffset > bytes.length)
            {
                throw new PcmlException(DAMRI.BAD_OFFSET_VALUE, new Object[] {new Integer(myOffset), new Integer(bytes.length), "<data>", getNameForException()} );
            }
            
            // Determine from where the offset is based
            Integer myOffsetbase = null;
            String myOffsetfrom = m_owner.getOffsetfromId();    // Get offsetfrom= element name, if any    @A1C

            // If offsetfrom= was specified with the name of another element, 
            // get the base for the offset from the offset stack. 
            // The offset stack is a stack of beginning offsets for all
            // ancestors of the current element. The offsetfrom= value must be one
            // of these ancestors or an error will be reported.
            if (myOffsetfrom != null) 
            {
                myOffsetbase = (Integer) offsetStack.get(myOffsetfrom);
                if (myOffsetbase == null) 
                {
                    throw new PcmlException(DAMRI.OFFSETFROM_NOT_FOUND, new Object[] {myOffsetfrom, getNameForException()} );
                }
            }
            else
            {
                // If offsetfrom= was specified with an integer literal, use it.
                if (m_owner.getOffsetfrom() >= 0)                   // @A1A
                {                                                   // @A1A
                    myOffsetbase = new Integer(m_owner.getOffsetfrom()); // @A1A
                }                                                   // @A1A
                // getOffsetfrom() returns -1 to indicate that offset from was not specified.
                // No offsetfrom= was specified, the offset will be relative to the
                // beginning offset of the parent of this elements parent.
                // This is the first (most recent) entry in the offset stack.
                else                                                // @A1A
                {                                                   // @A1A
                    myOffsetfrom = ((PcmlDocNode) m_owner.getParent()).getQualifiedName();
                    myOffsetbase = (Integer) offsetStack.get( myOffsetfrom );
                }                                                   // @A1A
            }
            
            if (myOffsetbase instanceof Integer)
            {
                myOffset = myOffset + myOffsetbase.intValue();
                // Handle errors caused by bad offset values
                if (myOffset < 0 || myOffset > bytes.length)
                {
                    throw new PcmlException(DAMRI.BAD_TOTAL_OFFSET, new Object[] {new Integer(myOffset), new Integer(bytes.length), myOffsetbase, myOffsetfrom, "<data>", getNameForException()} );
                }
            }
            
            // If offset for this element is beyond current offset
            // calculate number of bytes to skip
            if (myOffset > offset) 
            {
                skipBytes = myOffset - offset;
            }

        }

        // Get number of bytes to parse for this value
        nbrBytes = byteLength();

        // Make sure length makes sense
        if (nbrBytes < 0 || nbrBytes > PcmlData.MAX_STRING_LENGTH)
        {
            throw new PcmlException(DAMRI.BAD_DATA_LENGTH, new Object[] {new Integer(nbrBytes), new Integer(PcmlData.MAX_STRING_LENGTH), "<data>", getNameForException()} ); // @C4C
        }

        // Make sure we are not trying to access more bytes than available.
        if (offset + skipBytes + nbrBytes > bytes.length)
        {
            throw new PcmlException(DAMRI.NOT_ENOUGH_DATA, new Object[] {"<data>", getNameForException()} );
        }

        // Finally, make a new byte array, copy the bytes, and save them
        byte [] newBytes = new byte[nbrBytes];
        for (int b = 0; b < nbrBytes; b++) 
        {
            newBytes[b] = bytes[offset+ skipBytes + b];
        }
        setBytes(newBytes);

        // Detail tracing of data parsing
        if (Trace.isTracePCMLOn())                                              // @D3C
        {
            String parseMsg;
            if (m_indices.size() > 0)
            {
                parseMsg = SystemResourceFinder.format(DAMRI.READ_DATA_W_INDICES, new Object[] {Integer.toHexString(offset+ skipBytes), Integer.toString(nbrBytes), getNameForException(), m_indices.toString(), PcmlMessageLog.byteArrayToHexString(newBytes)} );
            }
            else
            {
                parseMsg = SystemResourceFinder.format(DAMRI.READ_DATA,           new Object[] {Integer.toHexString(offset+ skipBytes), Integer.toString(nbrBytes), getNameForException(), PcmlMessageLog.byteArrayToHexString(newBytes)} );
            }
            parseMsg = parseMsg + "\t  " + Thread.currentThread();
            Trace.log(Trace.PCML, parseMsg);
        }


        return skipBytes + nbrBytes;
    } // End parseBytes()

    // Resolve an integer value from either a named element or a literal
    private int resolveIntegerValue(int intLiteral, String intId) throws PcmlException 
    {
        PcmlData intNode;
        Object nodeValue;

        if (intId != null)
        {
            intNode = (PcmlData) ((PcmlDocRoot)m_owner.getRootNode()).getElement(intId);
            nodeValue = intNode.getValue(m_indices);
            if (nodeValue instanceof String) 
            {
                return Integer.parseInt((String) nodeValue);
            }
            else if (nodeValue instanceof Number) 
            {
                return ((Number) nodeValue).intValue();
            }
            else if (nodeValue == null) 
            {
                throw new PcmlException(DAMRI.INPUT_VALUE_NOT_SET, new Object[] {intNode.getNameForException()} );
            }
            else
            {
                throw new PcmlException(DAMRI.STRING_OR_NUMBER, new Object[] {nodeValue.getClass().getName(), intNode.getNameForException()} );
            }
        }
        return intLiteral;
    }

    // Converts an object to the correct type (class) for
    // the defined data type.
    // The SetValue method allows most data types to be set using 
    // either the Java class associtaed with the data type or using
    // a String. This method converts the value to the Java class 
    // defined for the data type. Exceptions such as 
    // NumberFormatException may result.
    //
    // This is implemented as a static method so it can be used
    // to verify the init= attribute after parsing the pcml document tags.
    public static Object convertValue(Object newVal, int dataType, int dataLength, int dataPrecision, String nodeNameForException) throws PcmlException 
    {
        Object convertedVal = null;
        
        if (newVal == null)
            throw new PcmlException(DAMRI.NULL_VALUE, new Object[] {nodeNameForException} );
                            
        switch (dataType) 
        {

            case PcmlData.CHAR:
                if (newVal instanceof String) 
                {
                    convertedVal = newVal;
                }
                else
                {
                    convertedVal = newVal.toString();
                }
                break;

            case PcmlData.INT:
                if (dataLength == 2) 
                {
                    if (dataPrecision == 16) 
                    {
                        if (newVal instanceof String) 
                        {
                            convertedVal = new Integer((String) newVal);
                        }
                        else 
                        {
                            if (newVal instanceof Number) 
                            {
                                convertedVal = new Integer( ((Number) newVal).intValue() );
                            }
                            else 
                            {
                                throw new PcmlException(DAMRI.STRING_OR_NUMBER, new Object[] {newVal.getClass().getName(), nodeNameForException} );
                            }
                        }
                    }
                    else 
                    { // dataPrecision == 15 or defaulted
                        if (newVal instanceof String) 
                        {
                            convertedVal = new Short( ((String) newVal) );
                        }
                        else 
                        {
                            if (newVal instanceof Number) 
                            {
                                convertedVal = new Short( ((Number) newVal).shortValue() );
                            }
                            else 
                            {
                                throw new PcmlException(DAMRI.STRING_OR_NUMBER, new Object[] {newVal.getClass().getName(), nodeNameForException} );
                            }
                        }
                    }
                }
                else 
                if (dataLength == 4)                                // @C3A
                { // dataLength == 4
                    if (dataPrecision == 32) 
                    {
                        if (newVal instanceof String) 
                        {
                            convertedVal = new Long((String) newVal);
                        }
                        else 
                        {
                            if (newVal instanceof Number) 
                            {
                                convertedVal = new Long( ((Number) newVal).longValue() );
                            }
                            else 
                            {
                                throw new PcmlException(DAMRI.STRING_OR_NUMBER, new Object[] {newVal.getClass().getName(), nodeNameForException} );
                            }
                        }
                    }
                    else 
                    { // dataPrecision == 31 or defaulted
                        if (newVal instanceof String) 
                        {
                            convertedVal = new Integer((String) newVal);
                        }
                        else 
                        {
                            if (newVal instanceof Number) 
                            {
                                convertedVal = new Integer( ((Number) newVal).intValue() );
                            }
                            else 
                            {
                                throw new PcmlException(DAMRI.STRING_OR_NUMBER, new Object[] {newVal.getClass().getName(), nodeNameForException} );
                            }
                        }
                    }
                }
                else                                                // @C3A
                { // dataLength == 8                                // @C3A
                  // dataPrecision == 63
                    if (newVal instanceof String)                   // @C3A
                    {                                               // @C3A
                        convertedVal = new Long((String) newVal);   // @C3A
                    }                                               // @C3A
                    else                                            // @C3A
                    {                                               // @C3A
                        if (newVal instanceof Number)               // @C3A
                        {                                           // @C3A
                            convertedVal = new Long( ((Number) newVal).longValue() ); // @C3A
                        }                                           // @C3A
                        else                                        // @C3A
                        {                                           // @C3A
                            throw new PcmlException(DAMRI.STRING_OR_NUMBER, new Object[] {newVal.getClass().getName(), nodeNameForException} ); // @C3A
                        }                                           // @C3A
                    }                                               // @C3A
                }                                                   // @C3A
                break;

            // Allows input value to be one of: BigDecimal, String or a subclass of Number
            case PcmlData.PACKED:
            case PcmlData.ZONED:
                if (newVal instanceof BigDecimal) 
                {
                    ///convertedVal = (BigDecimal) newVal;
                    convertedVal = ((BigDecimal)newVal).setScale(dataPrecision, BigDecimal.ROUND_HALF_EVEN);      // @D0C
                }
                else 
                {
                    if (newVal instanceof String) 
                    {
                        convertedVal = (new BigDecimal((String) newVal));
                        if (((BigDecimal)convertedVal).scale() != dataPrecision) {
                          convertedVal = ((BigDecimal)convertedVal).setScale(dataPrecision, BigDecimal.ROUND_HALF_EVEN);     // @D0A
                        }
                    }
                    else
                    {
                        if (newVal instanceof Number) 
                        {
                            ///convertedVal = new BigDecimal(((Number) newVal).doubleValue());
                            convertedVal = (new BigDecimal(((Number) newVal).doubleValue())).setScale(dataPrecision, BigDecimal.ROUND_HALF_EVEN);    // @D0C
                        }
                        else
                        {
                            throw new PcmlException(DAMRI.STRING_OR_NUMBER, new Object[] {newVal.getClass().getName(), nodeNameForException} );
                        }
                    }
                }
                break;


            case PcmlData.FLOAT:
                if (dataLength == 4) 
                {
                    if (newVal instanceof String) 
                    {
                        convertedVal = new Float((String) newVal);
                    }
                    else 
                    {
                        if (newVal instanceof Number) 
                        {
                            convertedVal = new Float( ((Number) newVal).floatValue() );
                        }
                        else 
                        {
                            throw new PcmlException(DAMRI.STRING_OR_NUMBER, new Object[] {newVal.getClass().getName(), nodeNameForException} );
                        }
                    }
                }
                else 
                { // Must be length=8
                    if (newVal instanceof String) 
                    {
                        convertedVal = new Double((String) newVal);
                    }
                    else 
                    {
                        if (newVal instanceof Number) 
                        {
                            convertedVal = new Double( ((Number) newVal).doubleValue() );
                        }
                        else 
                        {
                            throw new PcmlException(DAMRI.STRING_OR_NUMBER, new Object[] {newVal.getClass().getName(), nodeNameForException} );
                        }
                    }
                }
                break;

            case PcmlData.BYTE:
                    if (newVal instanceof String) 
                    {
                        StringTokenizer st = new StringTokenizer((String) newVal, " ,");
                        byte byteVal = 0;
                        byte[] byteArray = new byte[dataLength];
                        for (int i=0; i < dataLength; i++)
                        {
                            if (st.hasMoreTokens())
                                byteVal = Byte.decode(st.nextToken()).byteValue();
                            else
                                ; // Pad with last token in string
                            byteArray[i] = byteVal;
                        }
                        convertedVal = byteArray;
                    }
                    else 
                        if (newVal instanceof Number) 
                        {
                            byte byteVal = ((Number) newVal).byteValue();
                            byte[] byteArray = new byte[dataLength];
                            for (int i=0; i < dataLength; i++)
                            {
                                byteArray[i] = byteVal;
                            }
                            convertedVal = byteArray;
                        }
                        else 
                        {
                            throw new PcmlException(DAMRI.STRING_OR_NUMBER, new Object[] {newVal.getClass().getName(), nodeNameForException} );
                        }
                break;
    

            default:
                throw new PcmlException(DAMRI.BAD_DATA_TYPE, new Object[] {new Integer(dataType) , nodeNameForException} );

        } // END: switch (getDataType())

        return convertedVal;
    } 
    
    // Trims blanks and nulls from the ends of a string
    // Returns the trimmed string                               // @D1C
    static String trimString(String str, String trimEnd)        
    {                                                           
        if (trimEnd != null && trimEnd.equals("none"))                              
        {
            return str;                                         
        }
        char[] charBuff = str.toCharArray();                    
        int startOffset = 0;                    // Used when constructing return string 
        int lengthReturned = charBuff.length;   // Used when constructing return string 
        if (trimEnd == null || trimEnd.equals("right") || trimEnd.equals("both") )         
        {                                                       
            // Trim blanks and nulls
            trimRight: while (lengthReturned > 0)               
            {                                                   
                switch (charBuff[lengthReturned-1])             
                {                                               
                    case ' ':                                   
                    case '\0':                                  
                        lengthReturned--;                       
                        break;                                  
                    default:                                    
                        break trimRight;                        
                }                                               
            }                                                   
        }                                                       
        if (trimEnd != null && (trimEnd.equals("left") || trimEnd.equals("both")) )          
        {                                                       
            // Trim blanks and nulls
            trimLeft: while (startOffset < lengthReturned)                            
            {                                                   
                switch (charBuff[startOffset])                          
                {                                               
                    case ' ':                                   
                    case '\0':                                  
                        startOffset++;
                        lengthReturned--;
                        break;                                  
                    default:                                    
                        break trimLeft;                             
                }                                               
            }                                                   
        }
        return new String(charBuff, startOffset, lengthReturned);                  
    }                                                      
}
