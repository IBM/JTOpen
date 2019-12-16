///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PcmlSAXParser.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2010 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

//@E0C
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXNotRecognizedException;    //@E1A
import org.xml.sax.SAXNotSupportedException;     //@E1A
// import sun.misc.BASE64Decoder;                   //@E1A
import com.ibm.as400.access.BinaryConverter; //@E1A 

import org.xml.sax.XMLReader;

import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.io.BufferedInputStream;     //@E1A
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.HashSet;
import java.util.Stack;
import java.util.Vector;

import com.ibm.as400.access.Trace;

class PcmlSAXParser extends DefaultHandler
{
  private transient PcmlDocument m_rootNode;
  private transient PcmlDocNode  m_currentNode;
  private transient String       m_docName;
  private transient XMLErrorHandler m_xh;
  private transient boolean exceptionIfParseError_;

  // @E1A - New variables for XPCML.
  // @E1A - Hold current value of attributes
  Vector curAttrs = new Vector();
  Vector curQName = new Vector();

  int curDim = -1;
  String lastQName="";
  int[] dimArray = {0,0,0,0,0,0,0,0,0,0};
  PcmlDimensions dimensions = new PcmlDimensions(dimArray);

  // @E1A - initValue is used to keep track of XML data value for an element
  private String initValue="";               //@E1A

  // @E1A -- docIsXPCML - is the document an XPCML doc vs a PCML doc?
  private boolean docIsXPCML = false;                 //@E1A
  //private String xsdStreamName;                    //@E1A     // Used to determine if this is first instance of this node or if its an array
  private boolean firstInstance=true;              //@E1A 
  /** xsd transformed as a byte array input and output stream **/
  ByteArrayOutputStream xmlOut = new ByteArrayOutputStream();
  ByteArrayInputStream xmlIn;

  /** xsd file **/
  private InputStream xsdFileStream;

  // @E1A -- Set features for XML parser.  Need to set for full schema checking
  // @E1A feature ids
  /** Namespaces feature id (http://xml.org/sax/features/namespaces). */
  private static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";  //@E1A

  /** Namespace prefixes feature id (http://xml.org/sax/features/namespace-prefixes). */
  private static final String NAMESPACE_PREFIXES_FEATURE_ID = "http://xml.org/sax/features/namespace-prefixes";  //@E1A

  /** Validation feature id (http://xml.org/sax/features/validation). */
  private static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";  //@E1A

  /** Schema validation feature id (http://apache.org/xml/features/validation/schema). */
  private static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";  //@E1A

  /** Schema full checking feature id (http://apache.org/xml/features/validation/schema-full-checking). */
  private static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";   //@E1A

  /** Dynamic validation feature id (http://apache.org/xml/features/validation/dynamic). */
  private static final String DYNAMIC_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/dynamic";  //@E1A


  private static HashSet knownTypes_ = null;
  private static HashSet knownArrayTypes_ = null;


  PcmlSAXParser(String docName, InputStream docStream, InputStream xsdStream, boolean docIsXPCML, boolean exceptionIfParseError)
      throws MissingResourceException, IOException, ParseException, PcmlSpecificationException,
      FactoryConfigurationError, ParserConfigurationException, SAXException
  {
    m_rootNode = null;
    m_currentNode = null;
    exceptionIfParseError_ = exceptionIfParseError;

    String qualDocName;    // Potentially package qualified document name   @A2A

    xsdFileStream = xsdStream;  // xsd stream holder @E1A

    this.docIsXPCML = docIsXPCML;         // Fix for JTOpen Bug 1778759 - set the global docIsXPCML variable

    // initialize parsing vectors
    curAttrs.add(0,new AttributesImpl());
    curQName.add(0,"");

    // Save the document name (strip the suffix if present)
    if (docName.endsWith(".pcml") || docName.endsWith(".pcmlsrc") ||
        docName.endsWith(".xpcml") || docName.endsWith(".xpcmlsrc"))      //@E1C
    {
      qualDocName = docName.substring(0, docName.lastIndexOf('.') );   // @A2C
    }
    else
    {
      qualDocName = docName;                                           // @A2C
    }

    m_docName = qualDocName.substring(qualDocName.lastIndexOf('.') + 1); // @A2A

    // @E1A -- Changes for XPCML.  First find out if document is XPCML.  Then setup SequenceInputStream
    InputStream isHeader=null;                       //@E1A
    InputStream instream = null;

    try
    {
      // First check if xsd stream is valid if this is XPCML
      if (docIsXPCML)
      {
        // Transform the xsd stream to a byte stream we can more easily read
        if (xsdFileStream != null)
        {
          try
          {
            XPCMLHelper.doSimplifyXSDTransform(xsdFileStream, xmlOut);
          }
          catch (IOException e)
          {
            throw e;
          }
          catch (SAXException e)
          {
            throw e;
          }

          xmlIn = new ByteArrayInputStream(xmlOut.toByteArray());
          if (xmlIn == null)
            throw new MissingResourceException(SystemResourceFinder.format(DAMRI.PCML_DTD_NOT_FOUND, new Object[] {"xmlOut"}), "xmlOut", "");
        }

        // Doc is an XPCML document
        // Now try to open the XPCML file
        instream = new BufferedInputStream(docStream);
      }

      else  // doc is PCML                                              //@E1A
      {
        // Doc is a PCML document.  Do old processing...
        // Open the PCML header document that contains the DTD
        isHeader = SystemResourceFinder.getPCMLHeader();

        // Concatenate the two input streams
        instream = new SequenceInputStream(isHeader, docStream);

      }

      // Instantiate our error listener
      m_xh = new XMLErrorHandler(m_docName, SystemResourceFinder.getHeaderLineCount());

      SAXParserFactory factory = SAXParserFactory.newInstance(); //@E0A
      factory.setValidating(true); //@E0A
      factory.setNamespaceAware(false); //@E0A

      // @E1A -- Set new features for XPCML
      // set parser features
      if (docIsXPCML)
        setFeatures(factory);

      SAXParser parser = factory.newSAXParser(); //@E0A
      //@E0D        SAXParser parser = new SAXParser();                                         // @C2C
      //@E0D        try {                                                                       // @C2A
      //@E0D            parser.setFeature("http://xml.org/sax/features/validation", true);      // @C2A
      //@E0D            parser.setFeature( "http://xml.org/sax/features/namespaces", false );   // @C2A
      //@E0D        } catch (org.xml.sax.SAXException se) {                                     // @C2A
      //@E0D        }
      //@E0D        parser.setErrorHandler(xh);
      //@E0D        parser.setDocumentHandler(this);                                            // @C3C

      // Create an InputSource for passing to the parser.
      // Wrap any SAXExceptions as ParseExceptions.
      try
      {
        XMLReader reader = parser.getXMLReader(); //@E0A
        reader.setErrorHandler(m_xh); //@E0A

        parser.parse(new InputSource(instream), this); //@E0C
        // Close the input stream
        instream.close();
        instream = null;

      }
      catch (SAXException e)
      {
        Trace.log(Trace.PCML, e);
        ParseException pe = new ParseException(SystemResourceFinder.format(DAMRI.FAILED_TO_PARSE, new Object[] {m_docName} ) );
        pe.addMessage(e.getMessage());
        throw pe;
      }

      // Check for errors
      ParseException exc = m_xh.getException();
      if (exc != null)
      {
        exc.reportErrors();
        throw exc;
      }

      // Recursively walk the document tree and augment the tree with
      // cloned subtrees for <data type="struct"> nodes.
      augmentTree(m_rootNode, new Stack());

      // Perform post-parsing attribute checking.
      // Recursively walk the document tree and ask each node
      // to verify all attributes.
      // Note that this phase must be performed after the document is completely
      // parsed because some attributes (length=, count=, etc.) make reference
      // to named document elements occuring later in the document.

      checkAttributes(m_rootNode);

      // Copy in values from augmented nodes
      try
      {
        if (docIsXPCML)
          m_rootNode.copyValues(m_rootNode, m_rootNode);
      }
      catch (XmlException e)
      {
        Trace.log(Trace.PCML, "All data values may not have been copied to struct parm refs.", e);
        throw new SAXException(e);
      }

      if (m_rootNode != null && m_rootNode.getPcmlSpecificationException() != null)
      {
        throw m_rootNode.getPcmlSpecificationException();
      }
    }
    finally
    {
      if (isHeader != null) {
        try { isHeader.close(); } catch (Exception e) {};
      }
      if (instream != null) {
        try { instream.close(); } catch (Exception e) {};
      }
    }
  }


  // @E1A -- New method to do feature checking
  static void setFeatures(SAXParserFactory factory) throws SAXException, SAXNotRecognizedException, SAXNotSupportedException,  ParserConfigurationException
  {
    // @E1A -- Set new features for XPCML
    // set parser features
    factory.setFeature(NAMESPACES_FEATURE_ID, true);
    factory.setFeature(NAMESPACE_PREFIXES_FEATURE_ID, true);
    factory.setFeature(VALIDATION_FEATURE_ID, true);
    factory.setFeature(SCHEMA_VALIDATION_FEATURE_ID, true);
    factory.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, true);
    factory.setFeature(DYNAMIC_VALIDATION_FEATURE_ID, false);
  }


  // Process the PcmlDocNode tree and add new PcmlDocNode subtrees
  // for <DATA> tags that reference <STRUCT> tags.
  // (e.g. <data type="struct" struct="foobar"/>
  // This process must be done after the initial tree is built
  // because there is no requirement that the referenced <STRUCT>
  // tag be defined before the referencing <DATA> tag.
  PcmlDocument getPcmlDocument()
  {
    return m_rootNode;
  }

  // Process the PcmlDocNode tree and add new PcmlDocNode subtrees
  // for <DATA> tags that reference <STRUCT> tags.
  // (e.g. <data type="struct" struct="foobar"/>
  // This process must be done after the initial tree is built
  // because there is no requirement that the referenced <STRUCT>
  // tag be defined before the referencing <DATA> tag.
  private void augmentTree(PcmlDocNode pcmlElem, Stack recursionStack)
  {
    Enumeration children;
    PcmlDocNode child;
    String structName;
    PcmlDocNode structNode;
    PcmlData   dataNode;

    children = pcmlElem.getChildren();
    if (children == null)
      return;

    while (children.hasMoreElements())
    {
      child = (PcmlDocNode) children.nextElement();

      // Only augment the tree for <data type="struct"> items.
      // This also makes sure the <data> node has no children
      // though this should always be the case because the DTD does
      // allow the <data> tag to have nested elements.
      if (child instanceof PcmlData)  // This node is a <data> tag
      {
        dataNode = (PcmlData) child;
        if (dataNode.getDataType() == PcmlData.STRUCT) // and it is type="struct"
        {
          if (dataNode.getNbrChildren() == 0)        // and it has no children
          {
            structName = dataNode.getStruct();
            if (structName != null)
            {
              structNode = (PcmlDocNode) m_rootNode.getElement(structName);
              if (structNode instanceof PcmlStruct)
              {
                if (recursionStack.search(structNode) !=  -1)
                {
                  m_rootNode.addPcmlSpecificationError(DAMRI.CIRCULAR_REFERENCE, new Object[] {structName, dataNode.getBracketedTagName(), dataNode.getNameForException()} );
                }
                else
                {
                  Enumeration structChildren = structNode.getChildren();
                  while (structChildren.hasMoreElements())
                  {
                    PcmlDocNode structChild = (PcmlDocNode) structChildren.nextElement();
                    PcmlDocNode newChild = (PcmlDocNode) structChild.clone();

                    // Link the new node into the document tree
                    dataNode.addChild(newChild);

                    // Recursively add all of the new node's children
                    // to the document's hashtable.
                    m_rootNode.addToHashtable(newChild); // @C1C
                  }
                  // Insert subtree for this structure
                  //makeChildren(structNode.getXmlNode(), dataNode);

                }
              }
              else
              {
                if (structNode == null)
                {
                  m_rootNode.addPcmlSpecificationError(DAMRI.REF_NOT_FOUND, new Object[] {structName, "<struct>", dataNode.getBracketedTagName(), dataNode.getNameForException()} );
                }
                else
                {
                  m_rootNode.addPcmlSpecificationError(DAMRI.REF_WRONG_TYPE, new Object[] {structName, "<struct>", dataNode.getBracketedTagName(), dataNode.getNameForException()} );
                }
              }
            }
          }
          else
          {
            // Not allowed by the DTD
          }
        }
      }

      // Recursively augment the newly created tree
      recursionStack.push(child);
      augmentTree(child, recursionStack);
      recursionStack.pop();
    }
  }

  // Process the PcmlDocNode tree to resolve references to
  // named nodes in the tree.
  //
  // Some nodes (e.g. <data>) have attributes with named references
  // to other nodes (e.g. offset="xyz"). The named reference can
  // be either a simple name or a qualified name. The name
  // resolution is performed by walking up the document heirarchy looking
  // for a named element relative to the current location in the tree.
  //
  // Given the following document structure:
  //   <pcml>
  //     <program name="qabc">
  //       <struct  name="parm1">
  //         <struct name="struct1">
  //           <data name="lengthOfXyz" type="int" length="4">
  //           <data name="xyz"         type="char" length="lengthOfXyz">
  //         </struct>
  //       </struct>
  //     </program>
  //   </pcml>
  //
  // The element named "lengthOfXyz" is referenced by element "xyz" as
  // the length of the character field. In this case the following
  // names could be specified on the length attribute and would
  // resolve to the same element in the docuement:
  //    -- length="lengthOfXyz"
  //    -- length="struct1.lengthOfXyz"
  //    -- length="parm1.struct1.lengthOfXyz"
  //    -- length="qabc.parm1.struct1.lengthOfXyz"
  //
  // The resolution process recursively asks the nodes's parent if the
  // name string is a descendant of the current node.
  private void checkAttributes(PcmlDocNode pcmlElem)
  {
    Enumeration children;
    PcmlDocNode child;

    children = pcmlElem.getChildren();
    if (children == null)
      return;

    while (children.hasMoreElements())
    {
      child = (PcmlDocNode) children.nextElement();

      child.checkAttributes();

      // Recursively resolve references for all nodes in the tree
      checkAttributes(child);
    }
  }


  public void startElement(String uri, String localName, String qName, Attributes xmlAttrs) //@E0C
  {

    String tagName = qName; //@E0A
    PcmlDocNode newNode = null;
    String parmName="";
    String equivQName=qName;
    AttributesImpl uAttrs= new AttributesImpl();
    boolean uDefinedQName=false;
    boolean extendedType=false;

    // Reset initValue
    initValue="";


    if (!docIsXPCML)    // Copy old PCML code exactly - do not change
    {
      // Create a PcmlAttributeList to hold all the
      // attributes for this node.
      PcmlAttributeList attrs = new PcmlAttributeList(xmlAttrs.getLength());
      for (int attr = 0; attr < xmlAttrs.getLength(); attr++)
      {
        attrs.addAttribute( new PcmlAttribute(xmlAttrs.getQName(attr),   // @C3C
                                              xmlAttrs.getValue(attr),
                                              (true | false)) );
      }

      // Create PcmlDocNode subclass based on tag name
      if (tagName.equals("pcml"))
      {
        newNode = new PcmlDocument(attrs, m_docName);
      }
      else if (tagName.equals("program"))
      {
        newNode = new PcmlProgram(attrs);
      }
      else if (tagName.equals("struct"))
      {
        newNode = new PcmlStruct(attrs);
      }
      else if (tagName.equals("data"))
      {
        newNode = new PcmlData(attrs);
      }
      else
      {
        newNode = null;
        // Unrecognized tag name should never happen, if the tags parse successfully.
        if (m_rootNode != null)
        {
          m_rootNode.addPcmlSpecificationError(DAMRI.BAD_TAG, new Object[] {tagName, getBracketedTagName(tagName)} );
        }
        else  // we haven't established a root node yet
        {
          // This method has no 'throws' clause in its signature.
          // Therefore, to report the error and terminate processing, we have no choice but to throw a RuntimeException.
          PcmlSpecificationException pse = new PcmlSpecificationException(SystemResourceFinder.format(DAMRI.BAD_TAG, new Object[] {tagName, getBracketedTagName(tagName)}));
          throw new RuntimeException(pse);
        }
      }

      if (newNode != null)
      {
        if (m_rootNode == null)
        {
          m_rootNode = (PcmlDocument) newNode;
          m_currentNode = newNode;
        }
        else
        {
          m_currentNode.addChild(newNode);
          m_currentNode = newNode;
        }
      }
    }

    if (docIsXPCML)
    {
      if (xsdFileStream != null)
      {

        // Check if this is a user defined element. If so, convert to equivalent XPCML element
        if (!getKnownTypes().contains(qName))
        {
          // User defined parameter.  Need to find it in XSD stream.
          uDefinedQName=true;
          ByteArrayInputStream xmlIn = new ByteArrayInputStream(xmlOut.toByteArray());
          if (xmlIn == null)
            throw new MissingResourceException(SystemResourceFinder.format(DAMRI.PCML_DTD_NOT_FOUND, new Object[] {"xmlOut"}), "xmlOut", "");

          // Cache the line count of the header
          LineNumberReader lnr = new LineNumberReader(new InputStreamReader(xmlIn));
          try
          {
            String line;
            line=lnr.readLine();
            boolean found=false;
            while (line != null && !found)
            {
              if (line.indexOf("name="+"\""+qName+"\"") != -1 && line.indexOf("parm type=") != -1)
              {
                if (line.indexOf("parm type=string") != -1)
                {
                  // String parm found
                  equivQName = "stringParm";
                  found=true;
                }
                else if (line.indexOf("parm type=int") != -1)
                {
                  // Int parm found
                  equivQName="intParm";
                  found=true;
                }
                else if (line.indexOf("parm type=uint") != -1)
                {
                  // Unsigned Int parm found
                  equivQName="unsignedIntParm";
                  found=true;
                }
                else if (line.indexOf("parm type=hexBinary") != -1)
                {
                  // hexBinary parm found
                  equivQName="hexBinaryParm";
                  found=true;
                }
                else if (line.indexOf("parm type=byte") != -1)
                {
                  // Byte parm found
                  equivQName="byteParm";
                  found=true;
                }
                else if (line.indexOf("parm type=ubyte") != -1)
                {
                  // Unsigned Byte parm found
                  equivQName="unsignedByteParm";
                  found=true;
                }
                else if (line.indexOf("parm type=short") != -1)
                {
                  // Short parm found
                  equivQName="shortParm";
                  found=true;
                }
                else if (line.indexOf("parm type=ushort") != -1)
                {
                  // Unsigned Short parm found
                  equivQName="unsignedShortParm";
                  found=true;
                }
                else if (line.indexOf("parm type=long") != -1)
                {
                  // Long parm found
                  equivQName="longParm";
                  found=true;
                }
                else if (line.indexOf("parm type=ulong") != -1)
                {
                  // Unsigned Long parm found
                  equivQName="unsignedLongParm";
                  found=true;
                }
                else if (line.indexOf("parm type=float") != -1)
                {
                  // Float parm found
                  equivQName="floatParm";
                  found=true;
                }
                else if (line.indexOf("parm type=double") != -1)
                {
                  // Double parm found
                  equivQName="doubleParm";
                  found=true;
                }
                else if (line.indexOf("parm type=packed") != -1)
                {
                  // Packed parm found
                  equivQName="packedDecimalParm";
                  found=true;
                }
                else if (line.indexOf("parm type=zoned") != -1)
                {
                  // Zoned parm found
                  equivQName="zonedDecimalParm";
                  found=true;
                }
                else if (line.indexOf("parm type=date") != -1)
                {
                  // Date parm found
                  equivQName="dateParm";
                  found=true;
                }
                else if (line.indexOf("parm type=time") != -1)
                {
                  // Time parm found
                  equivQName="timeParm";
                  found=true;
                }
                else if (line.indexOf("parm type=timestamp") != -1)
                {
                  // Timestamp parm found
                  equivQName="timestampParm";
                  found=true;
                }
                else if (line.indexOf("parm type=structParm") != -1)
                {
                  // Struct parm found
                  equivQName="structParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfString") != -1)
                {
                  // String parm found
                  equivQName = "arrayOfStringParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfInt") != -1)
                {
                  // Int parm found
                  equivQName="arrayOfIntParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfUInt") != -1)
                {
                  // Unsigned Int parm found
                  equivQName="arrayOfUnsignedIntParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfHexBinary") != -1)
                {
                  // hexBinary parm found
                  equivQName="arrayOfHexBinaryParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfShort") != -1)
                {
                  // Short parm found
                  equivQName="arrayOfShortParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfUShort") != -1)
                {
                  // Unsigned Short parm found
                  equivQName="arrayOfUnsignedShortParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfLong") != -1)
                {
                  // Long parm found
                  equivQName="arrayOfLongParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfULong") != -1)
                {
                  // Unsigned Long parm found
                  equivQName="arrayOfUnsignedLongParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfFloat") != -1)
                {
                  // Float parm found
                  equivQName="arrayOfFloatParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfDouble") != -1)
                {
                  // Double parm found
                  equivQName="arrayOfDoubleParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfPacked") != -1)
                {
                  // Packed parm found
                  equivQName="arrayOfPackedDecimalParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfZoned") != -1)
                {
                  // Zoned parm found
                  equivQName="arrayOfZonedDecimalParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfDate") != -1)
                {
                  // Date parm found
                  equivQName="arrayOfDateParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfTime") != -1)
                {
                  // Time parm found
                  equivQName="arrayOfTimeParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfTimestamp") != -1)
                {
                  // Timestamp parm found
                  equivQName="arrayOfTimestampParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfStructParm") != -1)
                {
                  // Struct parm found
                  equivQName="arrayOfStructParm";
                  found=true;
                }
                else if (line.indexOf("parm type=structArray") != -1)
                {
                  // Struct found
                  equivQName="arrayOfStruct";
                  found=true;
                }
                else
                {
                  // Should never reach here.  Should get parse error if invalid type passed in
                  Trace.log(Trace.WARNING,"User defined type passed in not found in xsd stream");
                }

                // Now get attributes and set them to equivalent XPCML base attributes
                // Find and print attributes
                if (found==true)
                {
                  line = lnr.readLine();
                  while (line != null && line.indexOf("parm type=") == -1)
                  {
                    // Save attributes attributes
                    String attrName="";
                    String attrVal="";
                    // Parse line into attribute, value pair
                    if (line.indexOf("attributeName=") != -1)
                    {
                      // Found an attribute
                      attrName=line.substring(line.indexOf("attributeName=")+14, line.indexOf("attributeValue=")).trim();
                    }
                    if (line.indexOf("attributeValue=") != -1)
                    {
                      attrVal=line.substring(line.indexOf("attributeValue=")+15).trim();
                    }
                    // Save the attribute in the attribute list
                    uAttrs.addAttribute("","",attrName,"",attrVal);
                    // Read next line. While loop checks if its still an attribute line
                    line = lnr.readLine();
                  }
                }
              } // end if
              if (!found)
                line=lnr.readLine();
            } // end while
          }  // end try
          catch (IOException e)
          {
            Trace.log(Trace.PCML,"Error reading xsd stream in startElement.", e);
          }
        }
      }  // end if xsdStream not null

      if (!getKnownArrayTypes().contains(equivQName))
      {
        curDim++;

        if (uDefinedQName)
        {
          if (curAttrs.size() > curDim)
            curAttrs.set(curDim,uAttrs);
          else
            curAttrs.add(curDim,uAttrs);
        }
        else
        {
          if (curAttrs.size() > curDim)
            curAttrs.set(curDim, new AttributesImpl(xmlAttrs));
          else
            curAttrs.add(curDim, new AttributesImpl(xmlAttrs));
        }
        if (curQName.size() > curDim)
          curQName.set(curDim,equivQName);
        else
          curQName.add(curDim,equivQName);
        lastQName = equivQName;
      }

      // Create a PcmlAttributeList to hold all the
      // attributes for this node.
      AttributesImpl curList;
      String curName;
      if (equivQName.equals("i") || equivQName.equals("struct_i"))
      {
        // Check if this is first element in array.  If not, then increase dimensions by 1
        if (lastQName.indexOf("arrayOf") == -1)   // Last element not arrayOf element so up by 1
        {
          // This is not first element so up index by 1
          dimensions.set(curDim, dimensions.at(curDim)+1);
        }
        curList = new AttributesImpl( (AttributesImpl) curAttrs.elementAt(curDim));
        curName = (String)curQName.elementAt(curDim);
      }
      else
      {
         if (uDefinedQName)
         {
            curList = uAttrs;
            for (int attr = 0; attr < xmlAttrs.getLength(); attr++)
            {
                if (curList.getIndex(xmlAttrs.getQName(attr)) == -1)
                {
                    extendedType=true;
                    curList.addAttribute(xmlAttrs.getURI(attr),
                                         xmlAttrs.getLocalName(attr),
                                         xmlAttrs.getQName(attr),
                                         xmlAttrs.getType(attr),
                                         xmlAttrs.getValue(attr));
                }
            }
         }
         else
           curList = new AttributesImpl(xmlAttrs);
        curName = equivQName;
      }

      // Check if this is the first instance of this node.  If not, do not create the node again
      firstInstance = true;
      if (m_currentNode != null)
      {
        if (m_currentNode.getNodeType() == PcmlNodeType.STRUCT && !equivQName.equals("struct_i") && !equivQName.equals("struct") && !equivQName.equals("xpcml"))
        {
          boolean isInTree;
          isInTree = inTree(equivQName,curList);
          if (isInTree)
            firstInstance=false;
        }
      }
      for (int i=0; i<= curDim; ++i)
      {
        if (dimensions.at(i) > 0)
          firstInstance=false;
      }

      // Set current dimension to index value if specified
      if (equivQName.equals("i") || equivQName.equals("struct_i"))
      {
        for (int attr=0; attr < xmlAttrs.getLength(); attr++)
        {
          if (xmlAttrs.getQName(attr).equals("index"))
          {
            // Set current dimension to index value
            Integer indexInt = new Integer(xmlAttrs.getValue(attr));
            dimensions.set(curDim,indexInt.intValue());
          }
        }
      }

      // Set attributes if this is the first instance of this node
      lastQName = equivQName;
      if (firstInstance && !equivQName.equals("i") && !equivQName.equals("struct_i"))
      {
        PcmlAttributeList attrs= new PcmlAttributeList(curList.getLength()+2);

        for (int attr = 0; attr < curList.getLength(); attr++)
        {
          //@E1A -- XPCML code.  Need to convert XPCML representation of attributes to their PCML
          // equivalents.
          if (curList.getQName(attr).equals("name"))
            parmName = curList.getValue(attr);

          if (curList.getQName(attr).equals("passDirection"))                      //@E1A
          {
            //@E1A
            if (curList.getValue(attr).equals("in"))                            //@E1A
              attrs.addAttribute( new PcmlAttribute("usage",                  //@E1A
                                                    "input",
                                                    (true | false)) );
            else if (curList.getValue(attr).equals("inout"))                     //@E1A
              attrs.addAttribute( new PcmlAttribute("usage",                  //@E1A
                                                    "inputoutput",
                                                    (true | false)) );

            else if (curList.getValue(attr).equals("out"))                       //@E1A
              attrs.addAttribute( new PcmlAttribute("usage",                  //@E1A
                                                    "output",
                                                    (true | false)) );
            else if (curList.getValue(attr).equals("inherit"))                   //@E1A
              attrs.addAttribute( new PcmlAttribute("usage",                  //@E1A
                                                    "inherit",
                                                    (true | false)) );
          }
          else if (curList.getQName(attr).equals("passMode"))                    //@E1A
          {
            attrs.addAttribute( new PcmlAttribute("passby",                       //@E1A
                                                  curList.getValue(attr),
                                                  (true | false)) );
          }
          else if (curList.getQName(attr).equals("bytesPerChar"))                //@E1A
          {
            attrs.addAttribute( new PcmlAttribute("chartype",                  //@E1A
                                                  curList.getValue(attr),
                                                  (true | false)) );
          }
          else if (curList.getQName(attr).equals("totalBytes"))                  //@E1A
          {
            attrs.addAttribute( new PcmlAttribute("length",                    //@E1A
                                                  curList.getValue(attr),
                                                  (true | false)) );
          }
          else if (curList.getQName(attr).equals("outputSize"))                  //@E1A
          {
            attrs.addAttribute( new PcmlAttribute("outputsize",                //@E1A
                                                  curList.getValue(attr),
                                                  (true | false)) );
          }
          else if (curList.getQName(attr).equals("entryPoint"))                  //@E1A
          {
            attrs.addAttribute( new PcmlAttribute("entrypoint",                //@E1A
                                                  curList.getValue(attr),
                                                  (true | false)) );
          }
          else if (curList.getQName(attr).equals("returnValue"))                 //@E1A
          {
            attrs.addAttribute( new PcmlAttribute("returnvalue",               //@E1A
                                                  curList.getValue(attr),
                                                  (true | false)) );
          }
          else if (curList.getQName(attr).equals("threadSafe"))                 //@E1A
          {
            attrs.addAttribute( new PcmlAttribute("threadsafe",               //@E1A
                                                  curList.getValue(attr),
                                                  (true | false)) );
          }
          else if (curList.getQName(attr).equals("offsetFrom"))                 //@E1A
          {
            attrs.addAttribute( new PcmlAttribute("offsetfrom",               //@E1A
                                                  curList.getValue(attr),
                                                  (true | false)) );
          }
          else if (curList.getQName(attr).equals("totalDigits"))                //@E1A
          {
            attrs.addAttribute( new PcmlAttribute("length",                   //@E1A
                                                  curList.getValue(attr),
                                                  (true | false)) );
          }
          else if (curList.getQName(attr).equals("fractionDigits"))             //@E1A
          {
            attrs.addAttribute( new PcmlAttribute("precision",                //@E1A
                                                  curList.getValue(attr),
                                                  (true | false)) );
          }
          else if (curList.getQName(attr).equals("parseOrder"))                 //@E1A
          {
            attrs.addAttribute( new PcmlAttribute("parseorder",               //@E1A
                                                  curList.getValue(attr),
                                                  (true | false)) );
          }
          else if (curList.getQName(attr).equals("bidiStringType"))             //@E1A
          {
            attrs.addAttribute( new PcmlAttribute("bidistringtype",           //@E1A
                                                  curList.getValue(attr),
                                                  (true | false)) );
          }
          else if (curList.getQName(attr).equals("isEmptyString"))             //@E1A
          {
            attrs.addAttribute( new PcmlAttribute("init",           //@E1A
                                                  "",
                                                  (true | false)) );
          }
          else if (curList.getQName(attr).equals("dateFormat"))
          {
            attrs.addAttribute( new PcmlAttribute("dateformat",
                                                  curList.getValue(attr),
                                                  (true | false)) );
          }
          else if (curList.getQName(attr).equals("dateSeparator"))
          {
            attrs.addAttribute( new PcmlAttribute("dateseparator",
                                                  curList.getValue(attr),
                                                  (true | false)) );
          }
          else if (curList.getQName(attr).equals("timeFormat"))
          {
            attrs.addAttribute( new PcmlAttribute("timeformat",
                                                  curList.getValue(attr),
                                                  (true | false)) );
          }
          else if (curList.getQName(attr).equals("timeSeparator"))
          {
            attrs.addAttribute( new PcmlAttribute("timeseparator",
                                                  curList.getValue(attr),
                                                  (true | false)) );
          }
          // Note: This 'else if' clause needs to be the last one in the sequence.
          else if (!qName.equals("xpcml") || (qName.equals("xpcml") && curList.getQName(attr).equals("version")))
          {
            attrs.addAttribute( new PcmlAttribute(curList.getQName(attr),   // @C3C @E0C
                                                  curList.getValue(attr),
                                                  (true | false)) );
          }
          else
          {
            //System.out.println("Unrecognized attr: |" + curList.getQName(attr) + "|");
          }
        } // end for loop

        // Create PcmlDocNode subclass based on tag name
        if (tagName.equals("program"))
        {
          newNode = new PcmlProgram(attrs);
          // Reset dimensions
          for (int i=0; i< dimensions.size();++i)
            dimensions.set(i, 0);
        }
        else if (tagName.equals("struct"))
        {
          newNode = new PcmlStruct(attrs);
          // Check if outside program parameter list.  If so, then reset dimensions
          //
        }
        else if (tagName.equals("arrayOfStruct"))
        {
          newNode = new PcmlStruct(attrs);
          // Check if this is a user-defined element. Update field in docNode if so
          if (uDefinedQName)
            newNode.setCondensedName(qName);

        }
        else if (equivQName.equals("arrayOfStructParm"))
        {
          attrs.addAttribute( new PcmlAttribute("type",   // @E1A
                                                "struct",
                                                (true | false)) );
          newNode = new PcmlData(attrs);                 //@E1A
          // Check if this is a user-defined element. Update field in docNode if so
          if (uDefinedQName)
            newNode.setCondensedName(qName);

        }
        else if (tagName.equals("xpcml"))            //@E1A
        {
          newNode = new PcmlDocument(attrs,m_docName);     //@E1A
        }
        else if (equivQName.equals("stringParm") || equivQName.equals("arrayOfStringParm"))           //@E1A
        {

          attrs.addAttribute( new PcmlAttribute("type",    //@E1A
                                                "char",
                                                (true | false)) );
          newNode = new PcmlData(attrs);                   //@E1A
          // Check if this is a user-defined element. Update field in docNode if so
          if (uDefinedQName)
            newNode.setCondensedName(qName);
        }
        else if (equivQName.equals("hexBinaryParm") || equivQName.equals("arrayOfHexBinaryParm"))           //@E1A
        {
          attrs.addAttribute( new PcmlAttribute("type",    // @E1A
                                                "byte",
                                                (true | false)) );
          newNode = new PcmlData(attrs);                   //@E1A
          // Check if this is a user-defined element. Update field in docNode if so
          if (uDefinedQName)
            newNode.setCondensedName(qName);
        }
        else if (equivQName.equals("intParm") || equivQName.equals("arrayOfIntParm"))                    //@E1A
        {
          attrs.addAttribute( new PcmlAttribute("type",   // @E1A
                                                "int",
                                                (true | false)) );

          attrs.addAttribute( new PcmlAttribute("length",   // @E1A
                                                "4",
                                                (true | false)) );

          newNode = new PcmlData(attrs);                    //@E1A
          // Check if this is a user-defined element. Update field in docNode if so
          if (uDefinedQName)
            newNode.setCondensedName(qName);
        }
        else if (equivQName.equals("unsignedIntParm") || equivQName.equals("arrayOfUnsignedIntParm"))                    //@E1A
        {
          attrs.addAttribute( new PcmlAttribute("type",   // @E1A
                                                "int",
                                                (true | false)) );

          attrs.addAttribute( new PcmlAttribute("length",   // @E1A
                                                "4",
                                                (true | false)) );

          attrs.addAttribute( new PcmlAttribute("precision",   // @E1A
                                                "32",
                                                (true | false)) );

          newNode = new PcmlData(attrs);                    //@E1A
          // Check if this is a user-defined element. Update field in docNode if so
          if (uDefinedQName)
            newNode.setCondensedName(qName);
        }
        else if (equivQName.equals("byteParm") || equivQName.equals("arrayOfByteParm"))
        {
          attrs.addAttribute( new PcmlAttribute("type",
                                                "int",
                                                (true | false)) );

          attrs.addAttribute( new PcmlAttribute("length",
                                                "1",
                                                (true | false)) );
          newNode = new PcmlData(attrs);
          // Check if this is a user-defined element. Update field in docNode if so
          if (uDefinedQName)
            newNode.setCondensedName(qName);
        }
        else if (equivQName.equals("unsignedByteParm") || equivQName.equals("arrayOfUnsignedByteParm"))
        {
          attrs.addAttribute( new PcmlAttribute("type",
                                                "int",
                                                (true | false)) );

          attrs.addAttribute( new PcmlAttribute("length",
                                                "1",
                                                (true | false)) );

          attrs.addAttribute( new PcmlAttribute("precision",
                                                "8",
                                                (true | false)) );

          newNode = new PcmlData(attrs);
          // Check if this is a user-defined element. Update field in docNode if so
          if (uDefinedQName)
            newNode.setCondensedName(qName);
        }
        else if (equivQName.equals("shortParm") || equivQName.equals("arrayOfShortParm"))                   //@E1A
        {
          attrs.addAttribute( new PcmlAttribute("type",   // @E1A
                                                "int",
                                                (true | false)) );

          attrs.addAttribute( new PcmlAttribute("length",   // @E1A
                                                "2",
                                                (true | false)) );
          newNode = new PcmlData(attrs);
          // Check if this is a user-defined element. Update field in docNode if so
          if (uDefinedQName)
            newNode.setCondensedName(qName);
        }
        else if (equivQName.equals("unsignedShortParm") || equivQName.equals("arrayOfUnsignedShortParm"))                   //@E1A
        {
          attrs.addAttribute( new PcmlAttribute("type",   // @E1A
                                                "int",
                                                (true | false)) );

          attrs.addAttribute( new PcmlAttribute("length",   // @E1A
                                                "2",
                                                (true | false)) );

          attrs.addAttribute( new PcmlAttribute("precision",   // @E1A
                                                "16",
                                                (true | false)) );

          newNode = new PcmlData(attrs);
          // Check if this is a user-defined element. Update field in docNode if so
          if (uDefinedQName)
            newNode.setCondensedName(qName);
        }
        else if (equivQName.equals("longParm") || equivQName.equals("arrayOfLongParm"))                  //@E1A
        {
          attrs.addAttribute( new PcmlAttribute("type",   // @E1A
                                                "int",
                                                (true | false)) );

          attrs.addAttribute( new PcmlAttribute("length",   // @E1A
                                                "8",
                                                (true | false)) );
          newNode = new PcmlData(attrs);
          // Check if this is a user-defined element. Update field in docNode if so
          if (uDefinedQName)
            newNode.setCondensedName(qName);
        }
        else if (equivQName.equals("unsignedLongParm") || equivQName.equals("arrayOfUnsignedLongParm"))
        {
          attrs.addAttribute( new PcmlAttribute("type",
                                                "int",
                                                (true | false)) );

          attrs.addAttribute( new PcmlAttribute("length",
                                                "8",
                                                (true | false)) );

          attrs.addAttribute( new PcmlAttribute("precision",
                                                "64",
                                                (true | false)) );

          newNode = new PcmlData(attrs);
          // Check if this is a user-defined element. Update field in docNode if so
          if (uDefinedQName)
            newNode.setCondensedName(qName);
        }
        else if (equivQName.equals("floatParm") || equivQName.equals("arrayOfFloatParm"))                 //@E1A
        {
          attrs.addAttribute( new PcmlAttribute("type",   // @E1A
                                                "float",
                                                (true | false)) );

          attrs.addAttribute( new PcmlAttribute("length",   // @E1A
                                                "4",
                                                (true | false)) );

          newNode = new PcmlData(attrs);
          // Check if this is a user-defined element. Update field in docNode if so
          if (uDefinedQName)
            newNode.setCondensedName(qName);
        }
        else if (equivQName.equals("doubleParm") || equivQName.equals("arrayOfDoubleParm"))               //@E1A
        {
          attrs.addAttribute( new PcmlAttribute("type",   // @E1A
                                                "float",
                                                (true | false)) );

          attrs.addAttribute( new PcmlAttribute("length",   // @E1A
                                                "8",
                                                (true | false)) );

          newNode = new PcmlData(attrs);                    //@E1A
          // Check if this is a user-defined element. Update field in docNode if so
          if (uDefinedQName)
            newNode.setCondensedName(qName);
        }
        else if (equivQName.equals("zonedDecimalParm") || equivQName.equals("arrayOfZonedDecimalParm"))            //@E1A
        {
          attrs.addAttribute( new PcmlAttribute("type",     // @E1A
                                                "zoned",
                                                (true | false)) );
          newNode = new PcmlData(attrs);                    //@E1A
          // Check if this is a user-defined element. Update field in docNode if so
          if (uDefinedQName)
            newNode.setCondensedName(qName);
        }
        else if (equivQName.equals("packedDecimalParm") || equivQName.equals("arrayOfPackedDecimalParm"))           //@E1A
        {
          attrs.addAttribute( new PcmlAttribute("type",   // @E1A
                                                "packed",
                                                (true | false)) );
          newNode = new PcmlData(attrs);
          // Check if this is a user-defined element. Update field in docNode if so
          if (uDefinedQName)
            newNode.setCondensedName(qName);
        }
        else if (equivQName.equals("dateParm") || equivQName.equals("arrayOfDateParm"))
        {
          attrs.addAttribute( new PcmlAttribute("type",
                                                "date",
                                                (true | false)) );
          newNode = new PcmlData(attrs);
          // Check if this is a user-defined element. Update field in docNode if so
          if (uDefinedQName)
            newNode.setCondensedName(qName);
        }
        else if (equivQName.equals("timeParm") || equivQName.equals("arrayOfTimeParm"))
        {
          attrs.addAttribute( new PcmlAttribute("type",
                                                "time",
                                                (true | false)) );
          newNode = new PcmlData(attrs);
          // Check if this is a user-defined element. Update field in docNode if so
          if (uDefinedQName)
            newNode.setCondensedName(qName);
        }
        else if (equivQName.equals("timestampParm") || equivQName.equals("arrayOfTimestampParm"))
        {
          attrs.addAttribute( new PcmlAttribute("type",
                                                "timestamp",
                                                (true | false)) );
          newNode = new PcmlData(attrs);
          // Check if this is a user-defined element. Update field in docNode if so
          if (uDefinedQName)
            newNode.setCondensedName(qName);
        }
        else if (equivQName.equals("structParm"))    //@E1A
        {
          attrs.addAttribute( new PcmlAttribute("type",   // @E1A
                                                "struct",
                                                (true | false)) );
          newNode = new PcmlData(attrs);                 //@E1A
          // Check if this is a user-defined element. Update field in docNode if so
          if (uDefinedQName)
            newNode.setCondensedName(qName);
        }
        else
        {
          // Unrecognized tag name should never happen, if the tags parse successfully
          newNode = null;
        }

        if (newNode != null)
        {
          if (extendedType)
          {
            newNode.setIsExtendedType(true);
          }
          if (m_rootNode == null)
          {
            m_rootNode = (PcmlDocument) newNode;
            m_currentNode = newNode;
          }
          else
          {
            if (m_currentNode != null) m_currentNode.addChild(newNode);
            m_currentNode = newNode;
          }
        }
      } // end if firstInstance
      else
      {
        // Not first instance of node so we need to retrieve the node from the tree and set
        // it to current node
        if (m_currentNode.getNodeType()== PcmlNodeType.STRUCT && !equivQName.equals("struct_i") ||
            (m_currentNode.getNodeType()==PcmlNodeType.DATA && m_currentNode.getAttributeValue("type").equals("struct") ) &&
            !equivQName.equals("struct_i"))
        {
          String pName="";
          for (int attr = 0; attr < curList.getLength(); attr++)
          {
            if (curList.getQName(attr).equals("name"))
              pName = curList.getValue(attr);
          }

          Enumeration items;
          PcmlNode child=null;

          items = m_currentNode.getChildren();
          if (items == null)
            return;

          boolean found=false;
          while (items.hasMoreElements() && !found)
          {
            child = (PcmlNode) items.nextElement();
            if (child.getName().equals(pName))
              found=true;
          }
          m_currentNode = (PcmlDocNode) child;
        }
      }
    } // end docIsXPCML

  }


  /*** @E1A - New for XPCML                               **/
  /** Characters. This is the actual text of the elements  */
  /*  We need this method in XPCML because data can be     */
  /** passed in on input.                                  */
  public void characters(char ch[], int start, int length) throws SAXException {

    if (m_currentNode != null &&
        m_currentNode.getNodeType() == PcmlNodeType.DATA &&
        (m_currentNode.getAttributeValue("type") == null ||
         !m_currentNode.getAttributeValue("type").equals("struct")))    //@E1A
    {
      //@E1A
      String str = new String(ch,start,length);                                 //@E1A

      // Set the value based on the dimensions
      if (!getKnownArrayTypes().contains(lastQName))
      {
        // Concatenate to current value of init
        //             if (str.trim().length() > 0)
        //            {
        if (lastQName.equals("i"))
        {
          if (str.indexOf(0x0a) != -1)
          {
            initValue = initValue+str.substring(0,str.indexOf(0x0a));       //@E1A
          }
          else
            initValue = initValue+str;
        }
        else
          initValue = initValue+str;
        //             }

        try
        {

          // Need to determine if current node's type is byte and decode the string to
          // a byte array if so
          if (m_currentNode.getAttributeValue("type").equals("byte"))
          {
            // Need to convert hex input to bytes
            byte[] byteA = BinaryConverter.stringToBytes(initValue);
            ((PcmlData) m_currentNode).setValue(byteA, dimensions);
          }
          else
          {
            if (m_currentNode.getAttributeValue("type").equals("char") &&
                m_currentNode.getAttributeValue("isEmptyString") != null &&
                m_currentNode.getAttributeValue("isEmptyString").equals("true"))
            {
              Trace.log(Trace.PCML, "Setting an empty string");
              ( (PcmlData) m_currentNode).setValue("", new PcmlDimensions(dimensions) );
            }
            else if (initValue.trim().length() > 0 || m_currentNode.getAttributeValue("type").equals("char"))
            {
              ( (PcmlData) m_currentNode).setInit(initValue);
              ( (PcmlData) m_currentNode).setValue(initValue, dimensions);
            }
          }
          if (!firstInstance  ||
              lastQName.equals("i") || lastQName.equals("struct_i"))
          {
            // Reset init value if more than one of this node exists
            // This keeps subsequent nodes from getting set with value of init value of last node
            ( (PcmlData) m_currentNode).setInit(null);
          }
        }
        catch (Exception e)
        {
          Trace.log(Trace.PCML,"Exception when doing setValue.", e);
          Trace.log(Trace.PCML,"current node=" + m_currentNode.getQualifiedName());
          Trace.log(Trace.PCML,"initial value=" + initValue + "..");
          try
          {
            int length2 =  ((PcmlData)m_currentNode).getLength(dimensions);
          }
          catch (Exception e1)
          {
            Trace.log(Trace.PCML,"Exception due to length not being set when doing setValue.", e);
            Trace.log(Trace.PCML,"setValue not done but init attribute set.");
            return;
          }
          throw new SAXException(e);
        }
      }
    }                                                            //@E1A

  } // characters(char[],int,int);

  public void endElement(String uri, String localName, String qName) //@E0C
  {

    String equivQName=qName;
    boolean uDefinedQName=false;

    if (!docIsXPCML)
      m_currentNode = (PcmlDocNode) m_currentNode.getParent();

    if (docIsXPCML)
    {
      if (xsdFileStream != null)
      {
        // Check if this is a user defined element. If so, convert to equivalent XPCML element
        if (!getKnownTypes().contains(qName))
        {
          // User defined parameter.  Need to find it in XSD stream.
          uDefinedQName=true;
          ByteArrayInputStream xmlIn = new ByteArrayInputStream(xmlOut.toByteArray());
          if (xmlIn == null)
            throw new MissingResourceException(SystemResourceFinder.format(DAMRI.PCML_DTD_NOT_FOUND, new Object[] {"xmlOut"}), "xmlOut", "");

          // Cache the line count of the header
          LineNumberReader lnr = new LineNumberReader(new InputStreamReader(xmlIn));
          try
          {
            String line = lnr.readLine();
            boolean found=false;
            while (line != null && !found)
            {
              if (line.indexOf("name="+"\""+qName+"\"") != -1 && line.indexOf("parm type=") != -1)
              {
                if (line.indexOf("parm type=string") != -1)
                {
                  // String parm found
                  equivQName = "stringParm";
                  found=true;
                }
                else if (line.indexOf("parm type=int") != -1)
                {
                  // Int parm found
                  equivQName="intParm";
                  found=true;
                }
                else if (line.indexOf("parm type=uint") != -1)
                {
                  // Unsigned Int parm found
                  equivQName="unsignedIntParm";
                  found=true;
                }
                else if (line.indexOf("parm type=hexBinary") != -1)
                {
                  // hexBinary parm found
                  equivQName="hexBinaryParm";
                  found=true;
                }
                else if (line.indexOf("parm type=byte") != -1)
                {
                  // byte parm found
                  equivQName="byteParm";
                  found=true;
                }
                else if (line.indexOf("parm type=ubyte") != -1)
                {
                  // Unsigned Byte parm found
                  equivQName="unsignedByteParm";
                  found=true;
                }
                else if (line.indexOf("parm type=short") != -1)
                {
                  // Short parm found
                  equivQName="shortParm";
                  found=true;
                }
                else if (line.indexOf("parm type=ushort") != -1)
                {
                  // Unsigned Short parm found
                  equivQName="unsignedShortParm";
                  found=true;
                }
                else if (line.indexOf("parm type=long") != -1)
                {
                  // Long parm found
                  equivQName="longParm";
                  found=true;
                }
                else if (line.indexOf("parm type=ulong") != -1)
                {
                  // Unsigned Long parm found
                  equivQName="unsignedLongParm";
                  found=true;
                }
                else if (line.indexOf("parm type=float") != -1)
                {
                  // Float parm found
                  equivQName="floatParm";
                  found=true;
                }
                else if (line.indexOf("parm type=double") != -1)
                {
                  // Double parm found
                  equivQName="doubleParm";
                  found=true;
                }
                else if (line.indexOf("parm type=packed") != -1)
                {
                  // Packed parm found
                  equivQName="packedDecimalParm";
                  found=true;
                }
                else if (line.indexOf("parm type=zoned") != -1)
                {
                  // Zoned parm found
                  equivQName="zonedDecimalParm";
                  found=true;
                }
                else if (line.indexOf("parm type=date") != -1)
                {
                  // Date parm found
                  equivQName="dateParm";
                  found=true;
                }
                else if (line.indexOf("parm type=time") != -1)
                {
                  // Time parm found
                  equivQName="timeParm";
                  found=true;
                }
                else if (line.indexOf("parm type=timestamp") != -1)
                {
                  // Timestamp parm found
                  equivQName="timestampParm";
                  found=true;
                }
                else if (line.indexOf("parm type=structParm") != -1)
                {
                  // Struct parm found
                  equivQName="structParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfString") != -1)
                {
                  // String parm found
                  equivQName = "arrayOfStringParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfInt") != -1)
                {
                  // Int parm found
                  equivQName="arrayOfIntParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfUInt") != -1)
                {
                  // Unsigned Int parm found
                  equivQName="arrayOfUnsignedIntParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfHexBinary") != -1)
                {
                  // hexBinary parm found
                  equivQName="arrayOfHexBinaryParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfByte") != -1)
                {
                  // Byte parm found
                  equivQName="arrayOfByteParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfUByte") != -1)
                {
                  // Unsigned Byte parm found
                  equivQName="arrayOfUnsignedByteParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfShort") != -1)
                {
                  // Short parm found
                  equivQName="arrayOfShortParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfUShort") != -1)
                {
                  // Unsigned Short parm found
                  equivQName="arrayOfUnsignedShortParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfLong") != -1)
                {
                  // Long parm found
                  equivQName="arrayOfLongParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfULong") != -1)
                {
                  // Unsigned Long parm found
                  equivQName="arrayOfUnsignedLongParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfFloat") != -1)
                {
                  // Float parm found
                  equivQName="arrayOfFloatParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfDouble") != -1)
                {
                  // Double parm found
                  equivQName="arrayOfDoubleParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfPacked") != -1)
                {
                  // Packed parm found
                  equivQName="arrayOfPackedDecimalParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfZoned") != -1)
                {
                  // Zoned parm found
                  equivQName="arrayOfZonedDecimalParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfDate") != -1)
                {
                  // Date parm found
                  equivQName="arrayOfDateParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfTime") != -1)
                {
                  // Time parm found
                  equivQName="arrayOfTimeParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfTimestamp") != -1)
                {
                  // Timestamp parm found
                  equivQName="arrayOfTimestampParm";
                  found=true;
                }
                else if (line.indexOf("parm type=arrayOfStructParm") != -1)
                {
                  // Struct parm found
                  equivQName="arrayOfStructParm";
                  found=true;
                }
                else if (line.indexOf("parm type=structArray") != -1)
                {
                  // Struct parm found
                  equivQName="arrayOfStruct";
                  found=true;
                }
                {
                  // Should never reach here.  Should get parse error if invalid type passed in
                  // What should I do here?
                  Trace.log(Trace.PCML,"Error parsing xsd stream in endElement:", line);
                }
              }
              line=lnr.readLine();
            }
          }
          catch (IOException e)
          {
            Trace.log(Trace.PCML,"Error reading xsd stream in endElement.", e);
          }
        }
      }


      if (!equivQName.equals("parameterList") && !equivQName.equals("i") && !equivQName.equals("struct_i"))
        m_currentNode = (PcmlDocNode) m_currentNode.getParent();

      // Backing up tree.  Reset dimensions and current dimension
      if (!getKnownArrayTypes().contains(equivQName))
      {
        dimensions.set(curDim, 0); //reset
        curDim--;
      }
    }  // end if docIsXPCML
  }


  // Returns a string containing the tag name enclosed in brackets.
  private final static String getBracketedTagName(String tagName)
  {
    return "<" + tagName + ">";
  }


  /** Determine if node is in tree already -- used for array processing ****/
  boolean inTree(String equivQName, AttributesImpl curList)
  {
    boolean found=false;

    String pName="";
    for (int attr = 0; attr < curList.getLength(); attr++)
    {
      if (curList.getQName(attr).equals("name"))
        pName = curList.getValue(attr);
    }

    Enumeration items;
    PcmlNode child=null;

    items = m_currentNode.getChildren();
    if (items != null)
    {
      while (items.hasMoreElements() && !found)
      {
        child = (PcmlNode) items.nextElement();
        if (child.getName().equals(pName) && pName != "")
          found=true;
      }
    }
    return found;
  }


  // See Java Bug ID 4806878, http://developer.java.sun.com/developer/bugParade/bugs/4806878.html:
  // "[The] W3C XML Spec says that validation errors are not fatalerrors. And DefaultHandler implementation doesn't print out any errors for the validation errors. If user is interested in seeing the validation errors, then they need to extend the DefaultHandler [by re-implementing error() and fatalerror().]"

  public void warning(SAXParseException spe)
  throws SAXException
  {
    if (exceptionIfParseError_)
    {
      // new behavior (consistent with RfmlSAXParser)
      if (m_xh == null) throw spe;
      else m_xh.warning(spe);
    }
    else
    {
      // old behavior
      Trace.log(Trace.PCML, "[Warning]: "+  spe.getMessage());
    }
  }
  public void error(SAXParseException spe)
  throws SAXException
  {
    if (exceptionIfParseError_)
    {
      // new behavior (consistent with RfmlSAXParser)
      if (m_xh == null) throw spe;
      else m_xh.error(spe);
    }
    else
    {
      // old behavior
      Trace.log(Trace.PCML, "[Error]: "+  spe.getMessage());
    }
  }
  public void fatalError(SAXParseException spe)
  throws SAXException
  {
    if (m_xh == null) throw spe;
    else m_xh.error(spe);
  }


  // Note: After the HashSet is initially built, it is never modified.
  // Therefore we don't need to synchronize accesses after it is built.
  private static HashSet getKnownTypes()
  {
    if (knownTypes_ == null)
    {
      synchronized (PcmlSAXParser.class)
      {
        if (knownTypes_ == null)
        {
          knownTypes_ = new HashSet(50);
          knownTypes_.addAll(getKnownArrayTypes());
          String[] types =
          {
            "byteParm",
            "dateParm",
            "doubleParm",
            "floatParm",
            "hexBinaryParm",
            "intParm",
            "longParm",
            "packedDecimalParm",
            "parameterList",          // no corresponding 'array' type
            "program",                // no corresponding 'array' type
            "shortParm",
            "stringParm",
            "struct",
            "structParm",
            "timeParm",
            "timestampParm",
            "unsignedByteParm",
            "unsignedIntParm",
            "unsignedLongParm",
            "unsignedShortParm",
            "xpcml",                  // no corresponding 'array' type
            "zonedDecimalParm"
            // 41 types (including 19 array types): 12 new, plus 29 old
          };
          for (int i=0; i<types.length; i++) {
            knownTypes_.add(types[i]);
          }
        }
      }
    }
    return knownTypes_;
  }


  // Note: After the HashSet is initially built, it is never modified.
  // Therefore we don't need to synchronize accesses after it is built.
  private static HashSet getKnownArrayTypes()
  {
    if (knownArrayTypes_ == null)
    {
      synchronized (PcmlSAXParser.class)
      {
        if (knownArrayTypes_ == null)
        {
          knownArrayTypes_ = new HashSet(25);
          String[] types =
          {
            "arrayOfByteParm",
            "arrayOfDateParm",
            "arrayOfDoubleParm",
            "arrayOfFloatParm",
            "arrayOfHexBinaryParm",
            "arrayOfIntParm",
            "arrayOfLongParm",
            "arrayOfPackedDecimalParm",
            "arrayOfShortParm",
            "arrayOfStringParm",
            "arrayOfStruct",
            "arrayOfStructParm",
            "arrayOfTimeParm",
            "arrayOfTimestampParm",
            "arrayOfUnsignedByteParm",
            "arrayOfUnsignedIntParm",
            "arrayOfUnsignedLongParm",
            "arrayOfUnsignedShortParm",
            "arrayOfZonedDecimalParm",
            // 19 types (13 old, plus 6 new)
          };
          for (int i=0; i<types.length; i++) {
            knownArrayTypes_.add(types[i]);
          }
        }
      }
    }
    return knownArrayTypes_;
  }
}



