///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: RfmlRecordFormat.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import com.ibm.as400.access.*;


/**
**/
class RfmlRecordFormat extends PcmlDocNode
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";


    static final long serialVersionUID = 5L;

    private static final String RECORD_FORMAT_ATTRIBUTES[] = {
          "name",
          "description",
    };

    private String m_Description;
    private static final int DESCRIPTION_MAX_LENGTH = 50; // maximum length of "description" attribute value.
    private static final int MAX_DDS_NAME_LENGTH = 10;  // maximum length of "DDS name" for a FieldDescription.
    private static final int MAX_FIELD_TEXT_LENGTH = 50;  // maximum length of "TEXT" (description) for a FieldDescription.


    /**
     Constructor
     **/
    public RfmlRecordFormat(PcmlAttributeList attrs)
    {
        super(attrs);
        setNodeType(PcmlNodeType.RECORDFORMAT);


        // **********************************
        // Set attribute values
        //
        // The following code extracts the attribute values
        // from the parsed document node and
        // stores the values in private data members.
        // **********************************
        // Note: The superclass does the setName().
        setDescription(getAttributeValue("description"));
    }

    /**
     Creates and returns a copy of this object.
     @return A copy of this object.
     **/
    public Object clone()
    {
        RfmlRecordFormat node = (RfmlRecordFormat) super.clone();
        // Cloning does not include 'live' data, only the interface
        // definitions described by the PCML tags.
        // Null out the 'semi-transient' data values.

        return node;
    }


    /**
     Composes a list of FieldDescription objects representing the specified node and its children.
     **/
    private static void addFieldDescriptions(PcmlDocNode node, Vector fieldDescriptions, Vector namesAlreadyUsed, String preferredFieldName) throws XmlException    // @A1c added arg
    {
      boolean typeIsStruct = false;  // We set this to true if current node is <struct> or <data type="struct">.    // @A1a
      String fieldName = null;                // @A1c

      // If this is a STRUCT node, and we are being passed a parent name, then set up to pass the name on to the child.         // @A1a - This entire 'if' block.
      if (node.getNodeType() == PcmlNodeType.STRUCT) {
        typeIsStruct = true;
        if (preferredFieldName != null) fieldName = preferredFieldName;
        else fieldName = node.getName();
      }
      // If this is a DATA node, then set up the appropriate field description object and add it to the descriptions list.
      else if (node.getNodeType() == PcmlNodeType.DATA)  // It's a <data> node.  @A1c
      {
        RfmlData dNode = (RfmlData)node;
        PcmlDimensions noDimensions = new PcmlDimensions();
        int fieldLength;
        try { fieldLength = dNode.getLength(noDimensions); }
        catch (PcmlException e) { throw new XmlException(e); }
        if (preferredFieldName != null) fieldName = preferredFieldName;  // @A1a
        else fieldName = dNode.getName();   // @A1c

        FieldDescription fieldDesc = null;
        String initValue = dNode.getInit();
        int precision = dNode.getPrecision();

        switch (dNode.getDataType())
        {
            // Node is <data type="struct">.
          case (PcmlData.STRUCT):
            // Do nothing, a <struct> doesn't convert directly to a FieldDescription.  Skip it and go on to resolve any child nodes.
            typeIsStruct = true;  // @A1a
            break;

            // Node is <data type="char">.
          case (PcmlData.CHAR) :
            // See if the ccsid attribute is set.
            int ccsid = dNode.getCcsid();
            AS400Text convAS400Text;
            if (ccsid == 0) {  // ccsid was not specified
              convAS400Text = new AS400Text(fieldLength);
            }
            else {
              convAS400Text = new AS400Text(fieldLength, ccsid);
            }
            fieldDesc = new CharacterFieldDescription(convAS400Text, fieldName);
            // Note: Unless we do the explicit setCCSID(), the FieldDescription doesn't report the correct CCSID.   @A1a - This entire 'if' block.
            if (ccsid != 0) {
              ((CharacterFieldDescription)fieldDesc).setCCSID(Integer.toString(ccsid));
            }
            if (initValue != null) {
              ((CharacterFieldDescription)fieldDesc).setDFT(initValue);
            }
            break;

            // Node is <data type="int">.
          case (PcmlData.INT) :
            switch (fieldLength)
            {
              case 2:
                if (precision == 16) { // Unsigned.     @A1c - Swapped the if/else.
                  fieldDesc = new BinaryFieldDescription(new AS400UnsignedBin2(), fieldName);
                  if (initValue != null) {
                    ((BinaryFieldDescription)fieldDesc).setDFT(Integer.valueOf(initValue));
                  }
                }
                else { // Signed.         @A1c
                  fieldDesc = new BinaryFieldDescription(new AS400Bin2(), fieldName);
                  if (initValue != null) {
                    ((BinaryFieldDescription)fieldDesc).setDFT(Short.valueOf(initValue));
                  }
                }
                break;
              case 4:
                if (precision == 32) { // Unsigned.      @A1c - Swapped the if/else.
                  fieldDesc = new BinaryFieldDescription(new AS400UnsignedBin4(), fieldName);
                  if (initValue != null) {
                    ((BinaryFieldDescription)fieldDesc).setDFT(Long.valueOf(initValue));
                  }
                }
                else { // Signed.         @A1c
                  fieldDesc = new BinaryFieldDescription(new AS400Bin4(), fieldName);
                  if (initValue != null) {
                    ((BinaryFieldDescription)fieldDesc).setDFT(Integer.valueOf(initValue));
                  }
                }
                break;
              case 8:
                fieldDesc = new BinaryFieldDescription(new AS400Bin8(), fieldName);
                if (initValue != null) {
                  ((BinaryFieldDescription)fieldDesc).setDFT(Long.valueOf(initValue));
                }
                break;
              default:
                Trace.log(Trace.ERROR, "Invalid field length for type=int: " + fieldLength);
                throw new InternalErrorException(InternalErrorException.UNKNOWN);
            }
            break;

            // Node is <data type="zoned">.
          case (PcmlData.ZONED) :
            AS400ZonedDecimal convZoned = new AS400ZonedDecimal(fieldLength, precision);
            fieldDesc = new ZonedDecimalFieldDescription(convZoned, fieldName);
            if (initValue != null) {
              BigDecimal bigDec = new BigDecimal(initValue);
              if (bigDec.scale() != precision) {
                bigDec = bigDec.setScale(precision,BigDecimal.ROUND_HALF_EVEN);
              }
              ((ZonedDecimalFieldDescription)fieldDesc).setDFT(bigDec);
            }
            break;

            // Node is <data type="packed">.
          case (PcmlData.PACKED) :
            AS400PackedDecimal convPacked = new AS400PackedDecimal(fieldLength, precision);
            fieldDesc = new PackedDecimalFieldDescription(convPacked, fieldName);
            if (initValue != null) {
              BigDecimal bigDec = new BigDecimal(initValue);
              if (bigDec.scale() != precision) {
                bigDec = bigDec.setScale(precision,BigDecimal.ROUND_HALF_EVEN);
              }
              ((PackedDecimalFieldDescription)fieldDesc).setDFT(new BigDecimal(initValue));
            }
            break;

            // Node is <data type="float">.
          case (PcmlData.FLOAT) :
            switch (fieldLength)
            {
              case 4:
                fieldDesc = new FloatFieldDescription(new AS400Float4(), fieldName);
                break;
              case 8:
                fieldDesc = new FloatFieldDescription(new AS400Float8(), fieldName);
                break;
              default:
                Trace.log(Trace.ERROR, "Invalid field length for type=float: " + fieldLength);
                throw new InternalErrorException(InternalErrorException.UNKNOWN);
            }
            if (initValue != null)
            {
              if (fieldLength == 4) {
                ((FloatFieldDescription)fieldDesc).setDFT(new Float(initValue));
              }
              else { // length==8
                ((FloatFieldDescription)fieldDesc).setDFT(new Double(initValue));
              }
            }
            ((FloatFieldDescription)fieldDesc).setLength(fieldLength);
            // Note: The "precision" attribute is not allowed for type=float.
            break;

            // Node is <data type="byte">.
          case (PcmlData.BYTE) :
            AS400ByteArray convByte = new AS400ByteArray(fieldLength);
            fieldDesc = new HexFieldDescription(convByte, fieldName);
            if (initValue != null) {
              Object convertedValue = PcmlDataValues.convertValue(initValue, PcmlData.BYTE, fieldLength, 0, dNode.getNameForException()); // @A1a
              ((HexFieldDescription)fieldDesc).setDFT((byte[])convertedValue); // @A1a
              // Note: We could alternatively use Arrays.fill().  However, java.util.Arrays is new in Java2.
            }
            break;

          default:
            Trace.log(Trace.ERROR, "Invalid data field type: " + dNode.getDataType());
            throw new InternalErrorException(InternalErrorException.UNKNOWN);

        }  // ... switch

        if (fieldDesc != null) {
          // Store the fully-qualified node name in the field text description.
          String qualifiedName = dNode.getQualifiedName();
          if (qualifiedName.length() <= MAX_FIELD_TEXT_LENGTH) {
            fieldDesc.setTEXT(qualifiedName);
          }
          else {
            // Truncate from the end of the qualified name, and prepend "..".
            int startPos = qualifiedName.length() - MAX_FIELD_TEXT_LENGTH + 2;
            String truncatedName = ".." + qualifiedName.substring(startPos);
            fieldDesc.setTEXT(truncatedName);
          }

          // Derive a reasonable unique "DDS name" (limit is 10 characters).
          String ddsName = generateUniqueName(fieldName.toUpperCase(), namesAlreadyUsed);
          fieldDesc.setDDSName(ddsName);
          namesAlreadyUsed.addElement(ddsName);  // Avoid re-using the same name for another field within this RecordFormat.

          // Print a warning if a count was specified.
          try {
            int count = dNode.getCount(noDimensions);
            if (count != 1) {
              Trace.log(Trace.WARNING, "Ignoring attribute 'count' ("+count+") for field " + qualifiedName);
            }
          }
          catch (PcmlException e) { throw new XmlException(e); }

          // Add this field description to the list.
          fieldDescriptions.addElement(fieldDesc);
        }
      }  // ... else <data> node.
      else {  // Neither <data> nor <struct>.          @A1c
        // Only <data> nodes get converted directly to FieldDescriptions.
        // Do nothing with this node, and proceed to its child nodes.
      }

      // If this node had child nodes, generate field descriptions for them.

      if (node.hasChildren())
      {
        // @A1a - This entire if-else block.
        // If the current node is a <struct> or <data type="struct">, and has exactly one child, then assume that the struct is being used as an "alias", and pass the parent's name to the child (for composing the child's FieldDescription).
        String nameForChild;
        if (typeIsStruct && (node.getNbrChildren() == 1)) {
          nameForChild = fieldName;
        }
        else nameForChild = null;  // This will force the child to use own name.

        // Generate FieldDescriptions for each child node.
        Enumeration children = node.getChildren();
        while (children.hasMoreElements())
        {
          PcmlDocNode child = (PcmlDocNode) children.nextElement();
          addFieldDescriptions(child, fieldDescriptions, namesAlreadyUsed, nameForChild);       // @A1c
        }
      }
    }


    /**
     Generates a unique "DDS name" for a field, based on the field name.
     **/
    private static String generateUniqueName(String initialName, Vector namesInUse)
    {
      if (initialName.length() <= MAX_DDS_NAME_LENGTH &&
          !namesInUse.contains(initialName)) return initialName;
      else
      {
        // Truncate down to the final (MAX_DDS_NAME_LENGTH) characters.
        int startPos = Math.max(0, initialName.length()-MAX_DDS_NAME_LENGTH);
        String baseName = initialName.substring(startPos);
        if (!namesInUse.contains(baseName)) return baseName;
        int baseLength = baseName.length();

        // Try appending digits to the end.
        String suffix = Integer.toString(0);
        StringBuffer newName = new StringBuffer(baseName);
        for (int i=0; suffix.length()<MAX_DDS_NAME_LENGTH;) {
          newName.replace(0,baseLength,baseName); // Reset to the base name.
                           // Note: StringBuffer.replace() is new in Java2.
          newName.setLength(baseLength); // Delete prior suffix.
          newName.append(suffix); // Append digits to end.
          // Truncate down to the final (MAX_DDS_NAME_LENGTH) characters.
          startPos = Math.max(0, newName.length()-MAX_DDS_NAME_LENGTH);
          newName.delete(0,startPos); // Note: StringBuffer.delete() is new in Java2.
          if (!namesInUse.contains(newName.toString())) return newName.toString();
          suffix = Integer.toString(++i);  // Set next suffix.
        }
      }
      Trace.log(Trace.ERROR, "Failed to generate unique DDS name for field: " + initialName);
      throw new InternalErrorException(InternalErrorException.UNKNOWN);
    }

    /**
     Returns the list of valid attributes for the recordformat element.
     **/
    String[] getAttributeList()
    {
        return RECORD_FORMAT_ATTRIBUTES;
    }

    private static void getDataNodes(PcmlDocNode node, Vector dataNodes) throws XmlException
    {
      // If this is a DATA node, then prepare to get the next field name.
      if (node.getNodeType() != PcmlNodeType.DATA) {
        // Only <data> nodes get converted directly to FieldDescriptions.
        // Do nothing with this node, and proceed to its child nodes.
      }
      else  // It's a <data> node.
      {
        RfmlData dNode = (RfmlData)node;
        PcmlDimensions noDimensions = new PcmlDimensions();
        int fieldLength;
        try { fieldLength = dNode.getLength(noDimensions); }
        catch (PcmlException e) { throw new XmlException(e); }
        String fieldName = dNode.getName();

        switch (dNode.getDataType())
        {
            // Node is <data type="struct">.
          case (PcmlData.STRUCT):
            // Do nothing, a <struct> doesn't convert directly to a FieldDescription.  Skip it and go on to resolve any child nodes.
            // If this node had child nodes, generate field names for them.
            break;

            // Node is <data without  type="struct">.
          default :
            // Add this field description to the list.
            dataNodes.addElement(dNode);
            break;
        }
      }  // ... else <data> node.

      // If this node had child nodes, generate field names for them.
      if (node.hasChildren())
      {
        // Generate FieldDescriptions for each child node.
        Enumeration children = node.getChildren();
        while (children.hasMoreElements())
        {
          PcmlDocNode child = (PcmlDocNode) children.nextElement();
          getDataNodes(child, dataNodes);
        }
      }
    }


    /**
     Returns a string containing the recordformat description.
     Returns null if description is not set.
     **/
    String getDescription()
    {
        return m_Description;
    }


    /**
     Assigns the value of the current node and its children, by parsing the input bytes.
     **/
    int parseBytes(byte[] bytes)
           throws XmlException
    {
      PcmlDimensions noDimensions = new PcmlDimensions();

      // Stack of offsets used by RfmlData.parseBytes() and RfmlStruct.parseBytes()
      Hashtable offsetStack = new Hashtable();

      int offsetIntoBuffer = 0;

        // Convert all fields from Java objects to i5/OS data.
        Enumeration children = getChildren();  // children of this node.
        while (children.hasMoreElements())
        {
          PcmlDocNode child = (PcmlDocNode) children.nextElement();

          // Create a FieldDescription for the node and convert the Java objects to i5/OS data.
          if (child.getNodeType() == PcmlNodeType.DATA) {
            try {
              int bytesConsumed = ((RfmlData) child).parseBytes(bytes, offsetIntoBuffer, offsetStack, noDimensions);
              offsetIntoBuffer += bytesConsumed;
            }
            catch (ArrayIndexOutOfBoundsException e) {
              Trace.log(Trace.ERROR, e);
              throw new XmlException(DAMRI.INSUFFICIENT_INPUT_DATA, new Object[] {"(unknown)", Integer.toString(bytes.length), "<recordformat>", this.getNameForException()} );
            }
          }
          else {
            throw new XmlException(DAMRI.BAD_NODE_TYPE, new Object[] {new Integer(child.getNodeType()) , child.getNameForException()} );
          }
        }
        return offsetIntoBuffer;  // total number of bytes consumed
    }

    /**
     Sets the values of the current node and its children, from the values of the corresponding fields in the specified Record.
     **/
    public void setValues(Record record)
           throws java.io.UnsupportedEncodingException,
                  XmlException
    {
      Vector dataNodes = new Vector();  // This is where we will accumulate the data nodes of the record
      getDataNodes(this, dataNodes);

      for (int fieldNum=0; fieldNum<dataNodes.size(); ++fieldNum)
      {
        RfmlData dataNode = (RfmlData) dataNodes.elementAt(fieldNum);
        // Note: Reference the field by index instead of by name, to avoid having to match the name of the field in the Record.
        Object fieldValue = record.getField(fieldNum);
        if (fieldValue != null) {
          dataNode.setValue(fieldValue);
        }
        else if (Trace.isTraceWarningOn()) {
          String nameStr = dataNode.getName();
          Trace.log(Trace.WARNING, "Record field " + fieldNum + "(" + nameStr + ") is null.");
        }
      }
    }



    /**
     Sets the description= attribute value.
     **/
    void setDescription(String desc)
    {
        m_Description = desc;
    }
    // Note: We do not validate the length of the 'description' attribute in checkAttributes().


    /**
     Generates bytes representing the values of the current node and its children, in correct sequence.
     **/
    public byte[] toBytes()
           throws XmlException
    {
      // Set up the buffer for the generated bytes.
      // Use a ByteArrayOutputStream so that we can build up the buffer dynamically, without having to know its ultimate size in advance.
      ByteArrayOutputStream bytes = new ByteArrayOutputStream(512);

      PcmlDimensions noDimensions = new PcmlDimensions();
      int offsetForNextField = 0;
      Enumeration children = getChildren();

      while (children.hasMoreElements())
      {
        PcmlDocNode child = (PcmlDocNode) children.nextElement();
        switch (child.getNodeType())
        {
          case PcmlNodeType.STRUCT:
            // Do not expand the <struct> declarations.
            break;
          case PcmlNodeType.DATA:
            RfmlData dataNode = (RfmlData) child;
            {
              {
                int bytesWritten = dataNode.toBytes(bytes, offsetForNextField, noDimensions);
                offsetForNextField += bytesWritten;
              }
            }
            break;
          default:
            throw new XmlException(DAMRI.BAD_NODE_TYPE, new Object[] {new Integer(child.getNodeType()) , child.getNameForException()} );
        }
      }

      try { bytes.flush(); } catch (IOException e) { throw new XmlException(e); }

      return bytes.toByteArray();
    }


    /**
     Creates a RecordFormat object whose fields correspond to the current node and its children.
     **/
    public RecordFormat toRecordFormat() throws XmlException
    {
      Vector fieldDescriptions = new Vector();  // This is where we will accumulate the generated FieldDescription objects.
      Vector namesAlreadyUsed = new Vector(); // Field "DDS names" must be unique within the RecordFormat.  This Vector will accumulate the DDS names assigned so far, so we can generate a new unique name for each field.

      // Note: We ignore the "count" attribute.  Regardless of what value is specified in <data count=xxx>, we will generate a single FieldDescription for the node.

      // Recursively compose FieldDescription objects representing this node and its child nodes.
      addFieldDescriptions(this, fieldDescriptions, namesAlreadyUsed, null);   // @A1c

      RecordFormat recordFormat = new RecordFormat(getName());
      for (int i=0; i < fieldDescriptions.size(); ++i)
      {
        recordFormat.addFieldDescription((FieldDescription)fieldDescriptions.elementAt(i));
      }

      return recordFormat;
    }


    /**
     Custom serialization.
     **/
    private void writeObject(ObjectOutputStream out) throws IOException
    {
		synchronized (this)
		{
			// Perform default serialization
			out.defaultWriteObject();
		}
    }



}
