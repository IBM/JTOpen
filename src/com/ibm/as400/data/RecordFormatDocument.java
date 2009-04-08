///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: RecordFormatDocument.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import com.ibm.as400.access.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.io.UnsupportedEncodingException;

import java.lang.reflect.Method;

import java.util.Enumeration;
import java.util.Vector;
import java.util.MissingResourceException;

/**
 * Supports the use of Record Format Markup Language
 * (RFML) to specify formats and data values for file records and data buffers.
 * RFML is an XML language similar to PCML (Program Call Markup Language).
 *
 * RecordFormatDocument parses an RFML document and allows the application to
 * set and get values of specific fields within a record.
 *
 * The command line interface may be used to serialize
 * RFML document definitions.
 * <pre>
 * <kbd>java com.ibm.as400.data.RecordFormatDocument
 *     -serialize
 *     <i>RFML document name</i></kbd>
 * </pre>
 * Options:
 * <dl>
 * <dt><kbd>-serialize</kbd>
 * <dd>Parses the RFML document and creates a serialized version of the document.
 * The name of the serialized file will match the document name, and the file extension will be
 * <code><strong>.rfml.ser</code></strong> (lowercase).
 * <p><dt><kbd><i>rfml document name</i></kbd>
 * <dd>The fully-qualified resource name of the RFML document
 * which defines the record format(s).
 * </dl>
 *
 * <p><b>Serialized RFML files</b>
 * <br>To increase run-time performance, you can use a serialized RFML file.
 * A serialized RFML file contains serialized Java objects representing the RFML.
 * The objects that are serialized are the same objects that are created when you
 * construct the RecordFormatDocument object from a RFML source file.
 * Using serialized RFML files gives you better performance because the IBM
 * XML parser is not needed at run-time to interpret and validate the RFML tags.
 *
 * <p>For usage examples, refer to the PCML section of the Toolbox Programmer's
 * Guide.  The behavior of RecordFormatDocument matches that of
 * {@link com.ibm.as400.data.ProgramCallDocument ProgramCallDocument}
 * for methods that have the same name, such as setIntValue() and getIntValue().
 *
 * <p>Note: This class requires Java 2 (v1.2.2) or later.
 *
 **/
public class RecordFormatDocument implements Serializable, Cloneable
{
    static final long serialVersionUID = 5L;       // @A6C

    static final String RFML_VERSION = "4.0"; // NOTE: This needs to match the current value for the "version" attribute for the <rfml> tag.  @A1a

    private RfmlDocument m_rfmlDoc_;


    /**
     Constructs a RecordFormatDocument.
     <p>Prior to using an object constructed with this method,
     either {@link #setDocument(String) setDocument}
     or {@link #setValues(String,com.ibm.as400.access.Record) setValues} must be called.
     */
    public RecordFormatDocument()
    {}

    /**
    Constructs a RecordFormatDocument.
    The RFML document resource will be loaded from the classpath.
    The classpath will first be searched for a serialized resource.
    If a serialized resource is not found, the classpath will be
    searched for an RFML source file.

    @param documentName The document resource name of the RFML document for the record formats to be referenced.
     All RFML-related file extensions are assumed to be lowercase (for example, <tt>.rfml</tt> or <tt>.rfml.ser</tt>).
    The resource name can be a package qualified name. For example, "com.myCompany.myPackage.myRfml"

	@exception XmlException when the specified RFML document cannot be found.
    */
    public RecordFormatDocument(String documentName)
    	throws XmlException
    {
      if (documentName == null) throw new NullPointerException("documentName");
      m_rfmlDoc_ = loadRfmlDocument(documentName, null);
    }

    /**
    Constructs a RecordFormatDocument.
    The RFML document resource will be loaded from the classpath.
    The classpath will first be searched for a serialized resource.
    If a serialized resource is not found, the classpath will be
    searched for an RFML source file.

    @param documentName The document resource name of the RFML document for the record formats to be referenced.
     All RFML-related file extensions are assumed to be lowercase (for example, <tt>.rfml</tt> or <tt>.rfml.ser</tt>).
    The resource name can be a package qualified name. For example, "com.myCompany.myPackage.myRfml"
    @param loader The ClassLoader that will be used when loading the specified document resource.

	@exception XmlException when the specified RFML document cannot be found.
    */
    public RecordFormatDocument(String documentName, ClassLoader loader)
    	throws XmlException
    {
      if (documentName == null) throw new NullPointerException("documentName");
      if (loader == null) throw new NullPointerException("loader");
        m_rfmlDoc_ = loadRfmlDocument(documentName, loader);
    }


    /**
     Constructs a RecordFormatDocument from a Record object.
     <br>Note:
     The following Record datatypes are not supported by this method:
     <ul>
     <li>{@link com.ibm.as400.access.AS400Structure AS400Structure}
     <li>{@link com.ibm.as400.access.AS400Array AS400Array} with elements of type AS400Array (multidimensional array)
     </ul>

     @param record The Record object.
     */
    public RecordFormatDocument(Record record)
      throws XmlException
    {
      if (record == null) throw new NullPointerException("record");
      RecordFormat recordFormat = record.getRecordFormat();
      if (recordFormat == null) {
        throw new XmlException(DAMRI.RECORD_NOT_INITIALIZED);
      }
      setRecordFormat(recordFormat);
      setValues(recordFormat.getName(), record);
    }

    /**
    Constructs a RecordFormatDocument from a RecordFormat object.
     <br>Note:
     The following Record datatypes are not supported by this method:
     <ul>
     <li>{@link com.ibm.as400.access.AS400Structure AS400Structure}
     <li>{@link com.ibm.as400.access.AS400Array AS400Array} with elements of type AS400Array (multidimensional array)
     </ul>

    @param recordFormat The RecordFormat object.
    */
    public RecordFormatDocument(RecordFormat recordFormat)
      throws XmlException
    {
      if (recordFormat == null) throw new NullPointerException("recordFormat");
      setRecordFormat(recordFormat);
    }

    /**
     Adds the specified attribute <name,value> to the attribute list.
     **/
    private static void addAttribute(PcmlAttributeList attrList, String attrName, String attrValue)
    {
      PcmlAttribute attr = new PcmlAttribute(attrName, attrValue, true);
      attrList.addAttribute(attr);
    }

    /**
     Creates and returns a copy of this object.
     @return A copy of this object.
     **/
    public Object clone()
    {
   	    RecordFormatDocument newRfml = null;
        try
        {
            newRfml = (RecordFormatDocument) super.clone();
            if (m_rfmlDoc_ != null)
                newRfml.m_rfmlDoc_ = (RfmlDocument) m_rfmlDoc_.clone();
        }
        catch (CloneNotSupportedException e) {
          Trace.log(Trace.DIAGNOSTIC, e);
        }

        return newRfml;
    }

    /**
     Returns an attribute list containing values derived from the FieldDescription.
     If the DataType of the FieldDescription is AS400Structure or a multidimensional AS400Array, throws an XmlException.
     **/
    private static PcmlAttributeList generateAttributeList(FieldDescription fieldDesc, boolean isKeyField, Vector priorFieldNames, String recordFormatName)
      throws XmlException
    {
      // Determine what kind of field description it is, and map it to the appropriate RFML element.
      AS400DataType dataType = fieldDesc.getDataType();

      PcmlAttributeList attrList = new PcmlAttributeList(1);
      int count = 1;

      // Note: Classes that implement interface AS400DataType:
      // AS400Array, AS400Bin2, AS400Bin4, AS400Bin8, AS400ByteArray, AS400Float4, AS400Float8, AS400PackedDecimal, AS400Structure, AS400Text, AS400UnsignedBin2, AS400UnsignedBin4, AS400ZonedDecimal.

//      if (dataType instanceof AS400Array)
      int dtType = dataType.getInstanceType();

      if (dtType == AS400DataType.TYPE_ARRAY)
      {
        // Set the 'count' attribute.
        count = ((AS400Array)dataType).getNumberOfElements();
        if (count < 0) {
          Trace.log(Trace.DIAGNOSTIC, "AS400Array count is not set ("+count+")");
          count = 1;
        }
        addAttribute(attrList, "count", Integer.toString(count));
        // Set the data type to the contained type, so we can then set the attributes appropriately.
        dataType = ((AS400Array)dataType).getType();
        dtType = dataType.getInstanceType();
//        if (dataType instanceof AS400Array) {
        if (dataType.getInstanceType() == AS400DataType.TYPE_ARRAY)
        {
          // We don't yet support AS400Array-within-AS400Array.
          throw new XmlException(DAMRI.MULTI_ARRAY_NOT_SUPPORTED);
        }
      }

//      if (dataType instanceof AS400Structure) {
      if (dtType == AS400DataType.TYPE_STRUCTURE)
      {
        // Note: This type is left for a future enhancement.
        throw new XmlException(DAMRI.DATATYPE_NOT_SUPPORTED, new String[] {"AS400Structure"} );
      }

      // Set the 'name' attribute.
      String fieldName = fieldDesc.getFieldName();
      if (priorFieldNames.contains(fieldName)) {  // @A1a
        throw new XmlException(DAMRI.DUPLICATE_FIELD_NAME, new String[] {recordFormatName, fieldName} );
      }
      if (fieldName != null && fieldName.length() != 0) {
        addAttribute(attrList, "name", fieldName);
        priorFieldNames.add(fieldName);  // Remember that this name is already used.
      }

      // Set the 'length' attribute.
      // Note: At this point we know we don't have an AS400Structure.  All RFML datatypes except "struct" require the 'length' attribute.
      if (dtType == AS400DataType.TYPE_BIN2 ||
          dtType == AS400DataType.TYPE_UBIN2 ||
          dtType == AS400DataType.TYPE_BIN4 ||
          dtType == AS400DataType.TYPE_UBIN4 ||
          dtType == AS400DataType.TYPE_BIN8 ||
          dtType == AS400DataType.TYPE_FLOAT4 ||
          dtType == AS400DataType.TYPE_FLOAT8 ||
          dtType == AS400DataType.TYPE_PACKED)
      {
        // The numeric field types have architected lengths.  We will set the length in the switch() statement below.
      }
      else {
        int fieldLength;
        if (count == 0) fieldLength = fieldDesc.getLength();
        else fieldLength = fieldDesc.getLength() / count;
        addAttribute(attrList, "length", Integer.toString(fieldLength));
      }

      // If the field description has a default value, set the 'init' attribute.
      Object defaultValue = fieldDesc.getDFT();
      if (defaultValue == null) defaultValue = fieldDesc.getDFTCurrentValue();
      if (defaultValue != null)
      {
        if (defaultValue instanceof byte[] &&
            ((byte[])defaultValue).length != 0)
        {
          // Note: In PCML (and RFML), if the 'init' attribute specifies a single-byte numeric value, that value is replicated into each byte in the byte-array.
          byte[] dftBytes = (byte[])defaultValue;
          if (isSameByteRepeated(dftBytes)) {
            String byte0 = Byte.toString(dftBytes[0]);
            addAttribute(attrList, "init", byte0);
          }
          else {
            // Construct a String that lists out each separate byte value, e.g. "7 8 9 10".
            StringBuffer valList = new StringBuffer();
            for (int i=0; i<dftBytes.length; i++) {
              valList.append(Byte.toString(dftBytes[i]) + " ");
            }
            valList.deleteCharAt(valList.length()-1); // Strip final " ".
                      // Note: StringBuffer.deleteCharAt() is new in Java2.
            addAttribute(attrList, "init", valList.toString());
          }
        }
        else addAttribute(attrList, "init", defaultValue.toString());
      }

      // Now set the type-specific attributes.

      switch (dtType)
      {
        case AS400DataType.TYPE_BIN2:
          addAttribute(attrList, "type", "int");
          addAttribute(attrList, "length", "2");
          break;
        case AS400DataType.TYPE_BIN4:
          addAttribute(attrList, "type", "int");
          addAttribute(attrList, "length", "4");
          break;
        case AS400DataType.TYPE_BIN8:
          addAttribute(attrList, "type", "int");
          addAttribute(attrList, "length", "8");
          break;
        case AS400DataType.TYPE_UBIN2:
          addAttribute(attrList, "type", "int");
          addAttribute(attrList, "length","2");
          addAttribute(attrList, "precision", "16");
          break;
        case AS400DataType.TYPE_UBIN4:
          addAttribute(attrList, "type", "int");
          addAttribute(attrList, "length","4");
          addAttribute(attrList, "precision", "32");
          break;
        case AS400DataType.TYPE_BYTE_ARRAY:
          addAttribute(attrList, "type", "byte");
          break;
        case AS400DataType.TYPE_FLOAT4:
          addAttribute(attrList, "type", "float");
          addAttribute(attrList, "length", "4");
          // Note: PCML (and therefore RFML too) doesn't allow a 'precision' attribute for type="float".
          break;
        case AS400DataType.TYPE_FLOAT8:
          addAttribute(attrList, "type", "float");
          addAttribute(attrList, "length", "8");
          // Note: PCML (and therefore RFML too) doesn't allow a 'precision' attribute for type="float".
          break;
        case AS400DataType.TYPE_PACKED:
          addAttribute(attrList, "type", "packed");
          // $A5 
          int numDigits = ((AS400PackedDecimal) dataType).getNumberOfDigits();
          addAttribute(attrList, "length", Integer.toString(numDigits));  // end of $A5 change

  // $A2        int precision = ((PackedDecimalFieldDescription)fieldDesc).getDecimalPositions();
          int precision = ((AS400PackedDecimal) dataType).getNumberOfDecimalPositions(); // $A2
          addAttribute(attrList, "precision", Integer.toString(precision));
          break;
        case AS400DataType.TYPE_TEXT:
          addAttribute(attrList, "type", "char");
          // The attributes 'ccsid' and 'bidistringtype' are unique to type="char".
          // Note: The FieldDescription classes don't know about bidistringtype.

          // $A4 -- Change way ccsid is retrieved for ArrayFieldDescriptions
          try
          {
            if (fieldDesc instanceof ArrayFieldDescription)
            {
              int ccsid = ((AS400Text) dataType).getCcsid();
              if (ccsid > 0) {
                 addAttribute(attrList, "ccsid", Integer.toString(ccsid));
              }
            } else
            {
               Method method = fieldDesc.getClass().getMethod("getCCSID", (java.lang.Class[]) null); //@pdc cast for jdk1.5
               String ccsids = (String)method.invoke(fieldDesc, (java.lang.Object[]) null); //@pdc cast for jdk1.5
               if (ccsids != null && ccsids.length() != 0) {
                 addAttribute(attrList, "ccsid", ccsids);
               }
             }
          }
          catch (Exception e) { // This is OK, since not all FieldDescriptions that have type==AS400Text have ccsid's.
            Trace.log(Trace.DIAGNOSTIC, e);
          }
          break;
        case AS400DataType.TYPE_ZONED:
          addAttribute(attrList, "type", "zoned");
  // $A2        int precision = ((ZonedDecimalFieldDescription)fieldDesc).getDecimalPositions();
          int precisionZ = ((AS400ZonedDecimal) dataType).getNumberOfDecimalPositions(); // $A2
          addAttribute(attrList, "precision", Integer.toString(precisionZ));
          break;
        default:
         // None of the above.
          Trace.log(Trace.ERROR, "Unrecognized data type: dtType=="+dtType);
          throw new InternalErrorException(InternalErrorException.UNKNOWN);
      }

      // If the field is a "key field", set the keyfield attribute.
      if (isKeyField) {
        addAttribute(attrList, "keyfield", "true");
      }

      return attrList;
    }


    /**
    Returns a Descriptor for the specified RFML document.
    The RFML document resource will be loaded from the classpath.
    The classpath will first be searched for a serialized resource.
    If a serialized resource is not found, the classpath will be
    searched for an RFML source file.

    @param documentName The document resource name of the RFML document for which the Descriptor is returned.
    The resource name can be a package qualified name. For example, "com.myCompany.myPackage.myRfml"

    @return A descriptor for the <rfml> element of the named RFML file.

	@exception XmlException when the specified RFML document cannot be found.
    **/
    public static Descriptor getDescriptor(String documentName)     // @A7c
        throws XmlException
    {
      if (documentName == null) throw new NullPointerException("documentName");
        RfmlDocument pd = null;

        pd = loadRfmlDocument(documentName, null);

        return new RfmlDescriptor(pd);
    }

    /**
    Returns a Descriptor representing the specified RFML document.
    The RFML document resource will be loaded from the classpath.
    The classpath will first be searched for a serialized resource.
    If a serialized resource is not found, the classpath will be
    searched for an RFML source file.

    @param documentName The document resource name of the RFML document for which the Descriptor is returned.
    The resource name can be a package qualified name. For example, "com.myCompany.myPackage.myRfml"
    @param loader The ClassLoader that will be used when loading the specified document resource.
    @return A descriptor for the <rfml> element of the named RFML file.

	@exception XmlException when the specified RFML document cannot be found.
    **/
    public static Descriptor getDescriptor(String documentName, ClassLoader loader)     // @A7c
        throws XmlException
    {
      if (documentName == null) throw new NullPointerException("documentName");
      if (loader == null) throw new NullPointerException("loader");
        RfmlDocument pd = null;

        pd = loadRfmlDocument(documentName, loader);

        return new RfmlDescriptor(pd);
    }

    /**
    Returns a Descriptor representing the current RFML document.

    @return A descriptor for the <rfml> element of the current RFML file, or
            null if the RFML document has not be set.
    **/
    public Descriptor getDescriptor()     // @A7c
    {
        if (m_rfmlDoc_ != null) return new RfmlDescriptor(m_rfmlDoc_);
        else return null;
    }

    /**
    Returns a Descriptor representing the current RFML document.

    @return The Descriptor for the <rfml> element of the current RFML file, or
            null if the RFML document has not be set.
    **/
    String getDocName()
    {

        if (m_rfmlDoc_ == null) return null;
        else return m_rfmlDoc_.getDocName();
    }

    /**
    Returns a <code>double</code> value for the named element.

    @return The integer value for the named element.

    @param name The name of the &lt;data&gt; element in the PCML document.
    @exception XmlException  If an error occurs while processing RFML.
    **/
    public double getDoubleValue(String name)
        throws XmlException
    {
      // Note: The getDoubleValue() methods require Java2 or later, since they use Double.parseDouble().
      if (name == null) throw new NullPointerException("name");
      if (m_rfmlDoc_ == null) {
        throw new XmlException(DAMRI.DOCUMENT_NOT_SET );
      }
      return m_rfmlDoc_.getDoubleValue(name);
    }

    /**
    Returns a <code>double</code> value for the named element given indices to the data element.
    If the data element is an array or is an element in a structure array, an index
    must be specified for each dimension of the data.

    @return The integer value for the named element.

    @param name The name of the &lt;data&gt; element in the PCML document.
    @param indices An array of indices for accessing the value of an element in an array.
    @exception XmlException  If an error occurs while processing RFML.
    **/
    public double getDoubleValue(String name, int[] indices)
        throws XmlException
    {
      if (name == null) throw new NullPointerException("name");
      if (indices == null) throw new NullPointerException("indices");
      if (m_rfmlDoc_ == null) {
        throw new XmlException(DAMRI.DOCUMENT_NOT_SET );
      }
      return m_rfmlDoc_.getDoubleValue(name, new PcmlDimensions(indices));
    }

    /**
    Returns an <code>int</code> value for the named element.

    @return The integer value for the named element.

    @param name The name of the &lt;data&gt; element in the PCML document.
    @exception XmlException  If an error occurs while processing RFML.
    **/
    public int getIntValue(String name)
        throws XmlException
    {
      if (name == null) throw new NullPointerException("name");
      if (m_rfmlDoc_ == null) {
        throw new XmlException(DAMRI.DOCUMENT_NOT_SET );
      }
      return m_rfmlDoc_.getIntValue(name);
    }

    /**
    Returns an <code>int</code> value for the named element given indices to the data element.
    If the data element is an array or is an element in a structure array, an index
    must be specified for each dimension of the data.

    @return The integer value for the named element.

    @param name The name of the &lt;data&gt; element in the PCML document.
    @param indices An array of indices for accessing the value of an element in an array.
    @exception XmlException  If an error occurs while processing RFML.
    **/
    public int getIntValue(String name, int[] indices)
        throws XmlException
    {
      if (name == null) throw new NullPointerException("name");
      if (indices == null) throw new NullPointerException("indices");
      if (m_rfmlDoc_ == null) {
        throw new XmlException(DAMRI.DOCUMENT_NOT_SET );
      }
      return m_rfmlDoc_.getIntValue(name, new PcmlDimensions(indices));
    }

    /**
    Returns a <code>String</code> value for the named &lt;data type="char"&gt; element.
    <p>
    This method should be used when the string type cannot be determined until
    run-time.  In those cases, the RFML document cannot be used to indicate
    the string type so this method can be used to get the value using the
    string type that is specified.

    @param name The name of the <code>&lt;data&gt;</code> element in the RFML document.
    @param type The bidi string type, as defined by the CDRA (Character
                Data Representation Architecture).
    @exception XmlException  If an error occurs while processing RFML.
    **/
    public String getStringValue(String name, int type)
        throws XmlException
    {
      if (name == null) throw new NullPointerException("name");
      if (m_rfmlDoc_ == null) {
        throw new XmlException(DAMRI.DOCUMENT_NOT_SET );
      }
        return m_rfmlDoc_.getStringValue(name, type);
    }

    /**
    Returns a <code>String</code> value for the named &lt;data type="char"&gt; element, given indices to the data element.
    If the data element is an array or is an element in a structure array, an index
    must be specified for each dimension of the data.
    <p>
    This method should be used when the string type cannot be determined until
    run-time.  In those cases, the RFML document cannot be used to indicate
    the string type so this method can be used to get the value using the
    string type that is specified.

    @param name The name of the <code>&lt;data&gt;</code> element in the RFML document.
    @param indices An array of indices for setting the value of an element in an array.
    @param type The bidi string type, as defined by the CDRA (Character
                Data Representation Architecture).
    @exception XmlException  If an error occurs while processing RFML.
    **/
    public String getStringValue(String name, int[] indices, int type)
        throws XmlException
    {
      if (name == null) throw new NullPointerException("name");
      if (indices == null) throw new NullPointerException("indices");
      if (m_rfmlDoc_ == null) {
        throw new XmlException(DAMRI.DOCUMENT_NOT_SET );
      }
        return m_rfmlDoc_.getStringValue(name, new PcmlDimensions(indices), type);
    }



    /**
    Returns the Java object value for the named element.
    <p>
    The type of object returned depends on the description in the RFML document.
    <table border=1>
    <tr valign=top><th>RFML Description</th><th>Object Returned</th></tr>
    <tr valign=top><td>type=char</td><td>String</td></tr>
    <tr valign=top><td>type=byte</td><td>byte[]</td></tr>
    <tr valign=top><td>type=int<br>
                             length=2<br>
                             precision=15</td><td>Short</td></tr>
    <tr valign=top><td>type=int<br>
                             length=2<br>
                             precision=16</td><td>Integer</td></tr>
    <tr valign=top><td>type=int<br>
                             length=4<br>
                             precision=31</td><td>Integer</td></tr>
    <tr valign=top><td>type=int<br>
                             length=4<br>
                             precision=32</td><td>Long</td></tr>
    <tr valign=top><td>type=int<br>
                             length=8<br>
                             precision=63</td><td>Long</td></tr>
    <tr valign=top><td>type=packed</td><td>BigDecimal</td></tr>
    <tr valign=top><td>type=zoned</td><td>BigDecimal</td></tr>
    <tr valign=top><td>type=float<br>
                             length=4</td><td>Float</td></tr>
    <tr valign=top><td>type=float<br>
                             length=8</td><td>Double</td></tr>
    <tr valign=top><td>type=struct</td><td>null</td></tr>
    </table>

    @return The Java object value for the named <code>&lt;data&gt;</code> element in the RFML document.

    @param name The name of the <code>&lt;data&gt;</code> element in the RFML document.
    @exception XmlException  If an error occurs while processing RFML.
    **/
    public Object getValue(String name)
        throws XmlException
    {
      if (name == null) throw new NullPointerException("name");
      if (m_rfmlDoc_ == null) {
        throw new XmlException(DAMRI.DOCUMENT_NOT_SET );
      }
        return m_rfmlDoc_.getValue(name);
    }

    /**
    Returns the Java object value for the named element given indices to the data element.
    If the data element is an array or is an element in a structure array, an index
    must be specified for each dimension of the data.
    <p>
    The type of object returned depends on the description in the RFML document.

    @return The Java object value for the named <code>&lt;data&gt;</code> element in the RFML document.

    @param name The name of the <code>&lt;data&gt;</code> element in the RFML document.
    @param indices An array of indices for accessing the value of an element in an array.
    @exception XmlException  If an error occurs while processing RFML.
    **/
    public Object getValue(String name, int[] indices)
        throws XmlException
    {
      if (name == null) throw new NullPointerException("name");
      if (indices == null) throw new NullPointerException("indices");
      if (m_rfmlDoc_ == null) {
        throw new XmlException(DAMRI.DOCUMENT_NOT_SET );
      }
        return m_rfmlDoc_.getValue(name, new PcmlDimensions(indices));
    }

    /**
     Determines whether the specified byte array consists of the same byte repeated.
     For example, returns true for {1,1,1,1}, and returns false for {1,2,3,4}.
     **/
    private static boolean isSameByteRepeated(byte[] bytes)
    {
      // Assume that the array is non-null and length is non-zero.
      for (int i=1; i<bytes.length; i++) {
        if (bytes[i] != bytes[0]) return false;
      }
      return true;
    }


    /**
      Loads a serialized RfmlDocument or constructs the document from
      a Rfml source file.

	@exception XmlException when the specified RFML document cannot be found.
    **/
    private static RfmlDocument loadRfmlDocument(String docName, ClassLoader loader)
        throws XmlException
    {
      RfmlDocument pd = null;

      pd = loadSerializedRfmlDocument(docName, loader);

      // If a RfmlDocument was successfully loaded from a serialized file
      // return the document loaded.
      if (pd != null)
        return pd;

      pd = loadSourceRfmlDocument(docName, loader);

      return pd;
    }

    /**
      Loads a serialized RfmlDocument from a serialized file.
    **/
    private static RfmlDocument loadSerializedRfmlDocument(String docName, ClassLoader loader)
        throws XmlException
    {
        RfmlDocument pd = null;
        InputStream is = null;
        ObjectInputStream in = null;

        // First try to find a serialized Rfml document
        try
        {
            // Try to open the serialized Rfml document
            is = SystemResourceFinder.getSerializedRFMLDocument(docName, loader);

            in = new ObjectInputStream(is);
            pd = (RfmlDocument)in.readObject();
        }
        catch (MissingResourceException e)
        {
            // Ignore exception and try looking for Rfml source (below)
        }
        catch (StreamCorruptedException e)
        {
            // Ignore exception and try looking for Rfml source (below)
        }
        catch (IOException e)
        {
            if (Trace.isTraceErrorOn())
               e.printStackTrace(Trace.getPrintWriter());
            throw new XmlException(e);
        }
        catch (ClassNotFoundException e)
        {
            if (Trace.isTraceErrorOn())
               e.printStackTrace(Trace.getPrintWriter());
            throw new XmlException(e);
        }
        finally
        {
          if (in != null) try { in.close(); } catch (Exception e) {}
          if (is != null) try { is.close(); } catch (Exception e) {}
        }

        return pd;
    }

    /**
      Loads a RfmlDocument from a Rfml source file.
    **/
    private static RfmlDocument loadSourceRfmlDocument(String docName, ClassLoader loader)
        throws XmlException
    {
        RfmlDocument pd = null;

        // Construct the Rfml document from a source file
        try
        {
            RfmlSAXParser psp = new RfmlSAXParser();
            psp.parse(docName, loader);
            pd = psp.getRfmlDocument();
        }
        catch (ParseException pe)
        {
            pe.reportErrors();
            throw new XmlException(pe);
        }
        catch (PcmlSpecificationException pse)
        {
            pse.reportErrors();
            throw new XmlException(pse); // Note: MRI refers to PCML.
        }
        catch (IOException ioe)
        {
            if (Trace.isTraceErrorOn())
               ioe.printStackTrace(Trace.getPrintWriter());
            throw new XmlException(ioe);
        }
        catch (Exception e) //@E0A
        {
            if (Trace.isTraceErrorOn()) //@E0A
               e.printStackTrace(Trace.getPrintWriter()); //@E0A
            throw new XmlException(e); //@E0A
        }
        return pd;
    }

    /**
     Provides a command line interface to RecordFormatDocument.  See the class description.
     @param args The arguments.
     **/
    public static void main(String[] args)
    {
      final String errMsg = SystemResourceFinder.format(DAMRI.PCD_ARGUMENTS);

      if (args.length == 2)
      {
        if (!args[0].equalsIgnoreCase("-SERIALIZE"))
        {
          System.out.println(errMsg);
          System.exit(-1);
        }

        // Load the document from source (previously serialized documents are ignored)
        FileOutputStream fos = null;
        try
        {
          RecordFormatDocument doc = new RecordFormatDocument(args[1]);
          String outFileName = doc.getDocName() + SystemResourceFinder.m_rfmlSerializedExtension;
          fos = new FileOutputStream(outFileName);
          doc.serialize(fos);

          Trace.log(Trace.PCML, SystemResourceFinder.format(DAMRI.XML_SERIALIZED, new Object[] {"RFML", outFileName} ));   // @A6C
        }
        catch (Exception e)
        {
          System.out.println(e.getLocalizedMessage());
          System.exit(-1);
        }
        finally
        {
          if (fos != null) try { fos.close(); } catch (Exception e) {}
        }

      }
      else
      {
        System.out.println(errMsg);
        System.exit(-1);
      }

    }


    /**
      Saves a RfmlDocument as a serialized resource.
    **/
    private static void saveRfmlDocument(RfmlDocument pd, OutputStream outStream)
        throws XmlException
    {
        ObjectOutputStream out = null;

        pd.setSerializingWithData(false);

        try
        {
            out = new ObjectOutputStream(outStream);
            out.writeObject(pd);
        }
        catch (IOException e)
        {
            if (Trace.isTraceErrorOn())
               e.printStackTrace(Trace.getPrintWriter());
            throw new XmlException(e);
        }
        finally
        {
          if (out != null) try { out.close(); } catch (Exception e) {}
        }
    }


    /**
     Serializes the RecordFormatDocument.

     @param outputStream The output stream to which to serialize the object.
     @exception IOException  If an error occurs while writing to the stream.
     @exception XmlException  If an error occurs while processing RFML.
     **/
    public void serialize(OutputStream outputStream)
      throws IOException, XmlException
    {
      if (outputStream == null) {
        throw new NullPointerException("outputStream");
      }
      if (m_rfmlDoc_ == null) {
        throw new XmlException(DAMRI.DOCUMENT_NOT_SET );
      }
      saveRfmlDocument(m_rfmlDoc_, outputStream);
    }

    /**
     Serializes the RecordFormatDocument.

     @param file The file to which to serialize the object.
     @exception IOException  If an error occurs while writing to the file.
     @exception XmlException  If an error occurs while processing RFML.
     **/
    public void serialize(File file)
      throws IOException, XmlException
    {
      FileOutputStream fos = null;
      try
      {
        fos = new FileOutputStream(file);
        serialize(fos);
      }
      finally
      {
        if (fos != null) fos.close();
      }
    }

    /**
     Serializes the RecordFormatDocument.

     @param fileName The name of the file to which to serialize the object.
     @exception IOException  If an error occurs while writing to the file.
     @exception XmlException  If an error occurs while processing RFML.
     **/
    public void serialize(String fileName)
      throws IOException, XmlException
    {
      FileOutputStream fos = null;
      try
      {
        fos = new FileOutputStream(fileName);
        serialize(fos);
      }
      finally
      {
        if (fos != null) fos.close();
      }
    }

    /**
    Sets the Java object value for the named element using an int input value.
    <p>
    The named element must be able to be set using a Integer object.

    @param name The name of the <code>&lt;data&gt;</code> element in the RFML document.
    @param value The int value for the named element.
    @exception XmlException  If an error occurs while processing RFML.
    **/
    public void setIntValue(String name, int value)
        throws XmlException
    {
        setValue(name, new Integer(value));
    }

    /**
    Sets the Java object value for the named element using an int input value
    given indices to the data element.
    <p>
    The named element must be able to be set using a Integer object.

    @param name The name of the <code>&lt;data&gt;</code> element in the RFML document.
    @param indices An array of indices for setting the value of an element in an array.
    @param value The int value for the named element.
    @exception XmlException  If an error occurs while processing RFML.
    **/
    public void setIntValue(String name, int[] indices, int value)
        throws XmlException
    {
        setValue(name, indices, new Integer(value));
    }

    /**
    Sets the document format.
    */
    private void setRecordFormat(RecordFormat recordFormat)
      throws XmlException
   	{
      // Assume the caller has validated the argument.

      String fmtName = recordFormat.getName();
      if (fmtName == null || fmtName.length() == 0) {
        Trace.log(Trace.ERROR, "RecordFormat name is null or zero-length.");
        throw new XmlException(DAMRI.RECORDFORMAT_NOT_INITIALIZED);
      }
      setRecordFormat(fmtName, recordFormat);
    }

    /**
    Sets the document format.
    */
    private void setRecordFormat(String formatName, RecordFormat recordFormat)
      throws XmlException
   	{
      // Assume the caller has validated the arguments.

      // Set up the document node.
      // Get document name and attributes (version and ccsid).  Note that the RecordFormat class has no CCSID attribute.
      PcmlAttributeList attrList = new PcmlAttributeList(1);
      addAttribute(attrList, "version", RFML_VERSION);                    // @A1c
      m_rfmlDoc_ = new RfmlDocument(attrList, recordFormat.getName());

      // Set up a recordformat node.
      attrList = new PcmlAttributeList(1);
      addAttribute(attrList, "name", formatName);
      RfmlRecordFormat recFormatNode = new RfmlRecordFormat(attrList);
      m_rfmlDoc_.addChild(recFormatNode);

      // Examine the field descriptions in the RecordFormat.
      FieldDescription[] descriptions = recordFormat.getFieldDescriptions();
      FieldDescription[] keyFieldDescriptions = recordFormat.getKeyFieldDescriptions();
      Vector priorFieldNames = new Vector();  // Detect any duplicate names.  // @A1a
      for (int i=0; i<descriptions.length; i++)
      {
        FieldDescription fieldDesc = descriptions[i];
        boolean isKeyField = contains(keyFieldDescriptions, fieldDesc);
        attrList = generateAttributeList(fieldDesc, isKeyField, priorFieldNames, recordFormat.getName());
        if (attrList != null) {
          RfmlData dataNode = new RfmlData(attrList);
          recFormatNode.addChild(dataNode);
        }
      }
    }


    // Utility method for determining if an array contains a specified element.
    private static final boolean contains(Object[] array, Object element)
    {
      boolean contains = false;
      for (int i=0; i<array.length && !contains; i++) {
        if (array[i].equals(element)) contains = true;
      }
      return contains;
    }

    /**
    Sets the Java object value for the named &lt;data type="char"&gt; element using a String input.
    <p>
    This method should be used when the string type cannot be determined until
    run-time.  In those cases, the RFML document cannot be used to indicate
    the string type so this method can be used to set the value and the
    string type of the input value.

    @param name The name of the <code>&lt;data&gt;</code> element in the RFML document.
    @param value The int value for the named element.
    @param type The bidi string type, as defined by the CDRA (Character
                Data Representation Architecture).
    @exception XmlException  If an error occurs while processing RFML.
    **/
    public void setStringValue(String name, String value, int type)
        throws XmlException
    {
      if (name == null) throw new NullPointerException("name");
      if (value == null) throw new NullPointerException("value");
      if (m_rfmlDoc_ == null) {
        throw new XmlException(DAMRI.DOCUMENT_NOT_SET );
      }
          m_rfmlDoc_.setStringValue(name, value, type);
    }

    /**
    Sets the Java object value for the named &lt;data type="char"&gt; element using a String input value, given indices to the data element.
    <p>
    This method should be used when the string type cannot be determined until
    run-time.  In those cases, the RFML document cannot be used to indicate
    the string type so this method can be used to set the value and the
    string type of the input value.

    @param name The name of the <code>&lt;data&gt;</code> element in the RFML document.
    @param indices An array of indices for setting the value of an element in an array.
    @param value The int value for the named element.
    @param type The bidi string type, as defined by the CDRA (Character
                Data Representation Architecture).
    @exception XmlException  If an error occurs while processing RFML.
    **/
    public void setStringValue(String name, int[] indices, String value, int type)
        throws XmlException
    {
      if (name == null) throw new NullPointerException("name");
      if (indices == null) throw new NullPointerException("indices");
      if (value == null) throw new NullPointerException("value");
      if (m_rfmlDoc_ == null) {
        throw new XmlException(DAMRI.DOCUMENT_NOT_SET );
      }
          m_rfmlDoc_.setStringValue(name, value, new PcmlDimensions(indices), type);
    }

    /**
    Sets the RFML document resource.
    The RFML document resource will be loaded from the classpath.
    The classpath will first be searched for a serialized resource.
    If a serialized resource is not found, the classpath will be
    searched for an RFML source file.

    @param documentName The document resource name of the RFML document for the record formats to be referenced.
     All RFML-related file extensions are assumed to be lowercase (for example, <tt>.rfml</tt> or <tt>.rfml.ser</tt>).
    The resource name can be a package qualified name. For example, "com.myCompany.myPackage.myRfml"

	@exception XmlException when the specified RFML document cannot be found.
    **/
    public void setDocument(String documentName)
        throws XmlException
    {
      if (documentName == null) throw new NullPointerException("documentName");
        if (m_rfmlDoc_ != null)
            throw new XmlException(DAMRI.DOCUMENT_ALREADY_SET );

        m_rfmlDoc_ = loadRfmlDocument(documentName, null);
    }

    /**
    Sets the RFML document resource.
    The RFML document resource will be loaded from the classpath.
    The classpath will first be searched for a serialized resource.
    If a serialized resource is not found, the classpath will be
    searched for an RFML source file.

    @param documentName The document resource name of the RFML document for the record formats to be referenced.
     All RFML-related file extensions are assumed to be lowercase (for example, <tt>.rfml</tt> or <tt>.rfml.ser</tt>).
    The resource name can be a package qualified name. For example, "com.myCompany.myPackage.myRfml"
    @param loader The ClassLoader that will be used when loading the specified document resource.

	@exception XmlException when the specified RFML document cannot be found.
    **/
    public void setDocument(String documentName, ClassLoader loader)
        throws XmlException
    {
      if (documentName == null) throw new NullPointerException("documentName");
      if (loader == null) throw new NullPointerException("loader");
        if (m_rfmlDoc_ != null)
            throw new XmlException(DAMRI.DOCUMENT_ALREADY_SET );

        m_rfmlDoc_ = loadRfmlDocument(documentName, loader);
    }

    /**
    Sets the Java object value for the named element.
    <p>
    If the input value provided is not an instance of the
    correct Java class for the defined data type, it will be converted
    to the correct Java class.
    For example, if an element is defined as
     "<code>type=int length=2 precision=15</code>",
    it will automatically be converted to a Java Short object, if the value specified is an instance of Number or String.

    @param name The name of the <code>&lt;data&gt;</code> element in the RFML document.
    @param value The Java object value for the named element. The type of Object passed must be
    the correct type for the element definition or a String that can be converted to the correct type.  Null values are not allowed.
    @exception XmlException  If an error occurs while processing RFML.
    **/
    public void setValue(String name, Object value)
        throws XmlException
    {
      if (name == null) throw new NullPointerException("name");
      if (value == null) throw new NullPointerException("value");
      if (m_rfmlDoc_ == null) {
        throw new XmlException(DAMRI.DOCUMENT_NOT_SET );
      }
          m_rfmlDoc_.setValue(name, value);
    }

    /**
    Sets the Java object value for the named element
    given indices to the data element.
    If the data element is an array or is an element in a structure array, an index
    must be specified for each dimension of the data.
    <p>
    If the input value provided is not an instance of the
    correct Java class for the defined data type, it will be converted
    to the correct Java class.
    For example, if an element is defined as
     "<code>type=int length=2 precision=15</code>",
    it will automatically be converted to a Java Short object, if the value specified is an instance of Number or String.

    @param name The name of the <code>&lt;data&gt;</code> element in the RFML document.
    @param indices An array of indices for setting the value of an element in an array.
    @param value The Java object value for the named element. The type of Object passed must be
    the correct type for the element definition or a String that can be converted to the correct type.  Null values are not allowed.
    @exception XmlException  If an error occurs while processing RFML.
    **/
    public void setValue(String name, int[] indices, Object value)
        throws XmlException
    {
      if (name == null) throw new NullPointerException("name");
      if (indices == null) throw new NullPointerException("indices");
      if (value == null) throw new NullPointerException("value");
      if (m_rfmlDoc_ == null) {
        throw new XmlException(DAMRI.DOCUMENT_NOT_SET );
      }
          m_rfmlDoc_.setValue(name, value, new PcmlDimensions(indices));
    }


    /**
     Sets the Java object values for the specified <code>&lt;recordformat&gt;</code> element, based on the values stored in the record object passed into the method.
     <br>
     For any given field, if the input value provided is not an instance of the
     correct Java class for the defined data type, it will be converted
     to the correct Java class.
     For example, if an element is defined as
     "<code>type=int length=2 precision=15</code>",
     it will automatically be converted to a Java Short object, if the value specified is an instance of Number or String.  If any field in the Record is null-valued, a warning is issued.
     <br>Note:
     The following Record datatypes are not supported by this method:
     <ul>
     <li>{@link com.ibm.as400.access.AS400Structure AS400Structure}
     <li>{@link com.ibm.as400.access.AS400Array AS400Array} with elements of type AS400Array (multidimensional array)
     </ul>

     @param formatName The name of the <code>&lt;recordformat&gt;</code> element in the RFML document.
     @param record The record object containing the data.
     @exception XmlException  If an error occurs while processing RFML, or if an encoding specified by the Record is unsupported. 
     **/
    public void setValues(String formatName, Record record)
        throws XmlException
    {
      if (formatName == null) throw new NullPointerException("formatName");
      if (record == null) throw new NullPointerException("record");
      if (m_rfmlDoc_ == null) {
        RecordFormat recFmt = record.getRecordFormat();
        if (recFmt != null) setRecordFormat(formatName, recFmt);
        else {
          Trace.log(Trace.ERROR, "The Record has a null RecordFormat.");
          throw new XmlException(DAMRI.RECORD_NOT_INITIALIZED);
        }
      }

      // Get the node for the specified recordformat element.
      RfmlRecordFormat recFormatNode = m_rfmlDoc_.getRecordFormatNode(formatName);

      // Set the value of each <code>&lt;data&gt;</code> tag to the corresponding field value in the record.
      try {
        recFormatNode.setValues(record);
      }
      catch (UnsupportedEncodingException e) {
        throw new XmlException(e);
      }
    }

    /**
    Sets the data represented by the specified <code>&lt;recordformat&gt;</code> element.

    @param formatName The name of the <code>&lt;recordformat&gt;</code> element in the RFML document.
    @param values The byte values for all the fields in the the named element.  The length of the byte array must exactly match the length of data required, otherwise an XmlException is thrown.
    @exception XmlException   If an error occurs while processing RFML.
    **/
    public void setValues(String formatName, byte[] values)
        throws XmlException
    {
      if (formatName == null) throw new NullPointerException("formatName");
      if (values == null) throw new NullPointerException("values");
      if (m_rfmlDoc_ == null) {
        throw new XmlException(DAMRI.DOCUMENT_NOT_SET );
      }

      // Get the node for the specified recordformat element.
      RfmlRecordFormat recFormatNode = m_rfmlDoc_.getRecordFormatNode(formatName);

      // Feed the bytes to the node.
      int bytesConsumed = recFormatNode.parseBytes(values);
      if (bytesConsumed < values.length)
      {
        throw new XmlException(DAMRI.EXCESS_INPUT_DATA, new Object[] {Integer.toString(bytesConsumed), Integer.toString(values.length), "<recordformat>", m_rfmlDoc_.getNameForException()} );
      }
    }


    /**
    Returns the data contained by the specified <code>&lt;recordformat&gt;</code> element, as a byte array.

    @return The data contained by the record.

    @param formatName The name of the <code>&lt;recordformat&gt;</code> element in the RFML document.
    **/
    public byte[] toByteArray(String formatName)
        throws XmlException
    {
      if (formatName == null) throw new NullPointerException("formatName");
      if (m_rfmlDoc_ == null) {
        throw new XmlException(DAMRI.DOCUMENT_NOT_SET );
      }

      // Get the node for the specified recordformat element.
      RfmlRecordFormat recFormatNode = m_rfmlDoc_.getRecordFormatNode(formatName);

      // Generate bytes.
      return recFormatNode.toBytes();
    }


    /**
     Returns a Record object with the same structure and data as the specified <code>&lt;recordformat&gt;</code> element.
     Before this method is called, all &lt;data&gt; elements must either have their values set, or have 'init' values.
     <br>Note: When the Record is created, the 'count' attribute of &lt;data&gt; elements is disregarded.

     @return A Record object containing the same information as the specified <code>&lt;recordformat&gt;</code> element.

     @param formatName The name of the <code>&lt;recordformat&gt;</code> element in the RFML document.
     @exception XmlException  If an error occurs while processing RFML.
     **/
    public Record toRecord(String formatName)
        throws XmlException
    {
      try {
        return new Record(toRecordFormat(formatName), toByteArray(formatName));
      }
      catch (UnsupportedEncodingException e) {
        throw new XmlException(e);  // This should never happen, but if it does...
      }
    }


    /**
     Returns a RecordFormat object with the same structure as the specified <code>&lt;recordformat&gt;</code> element.
     Note: For &lt;data&gt; elements, the 'count' attribute is disregarded.

     @return A RecordFormat object with the same structure as the specified <code>&lt;recordformat&gt;</code> element.

     @param formatName The name of the <code>&lt;recordformat&gt;</code> element in the RFML document.
     **/
    public RecordFormat toRecordFormat(String formatName)
        throws XmlException
    {
      if (formatName == null) throw new NullPointerException("formatName");
      if (m_rfmlDoc_ == null) {
        throw new XmlException(DAMRI.DOCUMENT_NOT_SET );
      }

      // Get the node for the specified recordformat element.
      RfmlRecordFormat recFormatNode = m_rfmlDoc_.getRecordFormatNode(formatName);

      // Generate a RecordFormat.
      return recFormatNode.toRecordFormat();
    }


    /**
     Generates XML (RFML) representing the data contained in this object.
     Throws an XmlException if this object contains no data.

     @param outputStream The output stream to which to write the text.
     @exception IOException  If an error occurs while writing the data.
     @exception XmlException  If an error occurs while processing RFML.
     **/
    public void toXml(OutputStream outputStream)
      throws IOException, XmlException
    {
      if (outputStream == null) {
        throw new NullPointerException("outputStream");
      }
      if (m_rfmlDoc_ == null) {
        throw new XmlException(DAMRI.DOCUMENT_NOT_SET );
      }
      m_rfmlDoc_.toXml(outputStream);
    }

    /**
     Generates XML (RFML) representing the data contained in this object.
     Throws an XmlException if this object contains no data.

     @param file The file to which to write the text.
     @exception IOException  If an error occurs while writing the data.
     @exception XmlException  If an error occurs while processing RFML.
     **/
    public void toXml(File file)
      throws IOException, XmlException
    {
      FileOutputStream fos = null;
      try
      {
        fos = new FileOutputStream(file);
        toXml(fos);
      }
      finally
      {
        if (fos != null) fos.close();
      }
    }

    /**
     Generates XML (RFML) representing the data contained in this object.
     Throws an XmlException if this object contains no data.

     @param fileName The pathname of the file to which to write the text.
     @exception IOException  If an error occurs while writing the data.
     @exception XmlException  If an error occurs while processing RFML.
     **/
    public void toXml(String fileName)
      throws IOException, XmlException
    {
      FileOutputStream fos = null;
      try
      {
        fos = new FileOutputStream(fileName);
        toXml(fos);
      }
      finally
      {
        if (fos != null) fos.close();
      }
    }

    /**
     Custom serialization.
     **/
    private void writeObject(ObjectOutputStream out)
        throws IOException
    {
		synchronized (this)
		{
            if (m_rfmlDoc_ != null) {
                m_rfmlDoc_.setSerializingWithData(true);
            }

			// Perform default serialization
			out.defaultWriteObject();

		} // end of synchronized code
    }

}
