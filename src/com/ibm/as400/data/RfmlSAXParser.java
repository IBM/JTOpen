///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: RfmlSAXParser.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
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
import org.xml.sax.EntityResolver;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.io.IOException;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.Stack;

import com.ibm.as400.access.Trace;

class RfmlSAXParser extends DefaultHandler implements EntityResolver
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";


  private transient RfmlDocument m_rootNode;
  private transient PcmlDocNode  m_currentNode;
  private transient String       m_docName;
  private transient XMLErrorHandler m_xh;

  /**
   Processes the PcmlDocNode tree and adds new PcmlDocNode subtrees
   for <DATA> tags that reference <STRUCT> tags.
   (e.g. <data type="struct" struct="foobar"/>
   This process must be done after the initial tree is built
   because there is no requirement that the referenced <STRUCT>
   tag be defined before the referencing <DATA> tag.
   **/
  private void augmentTree(PcmlDocNode RfmlElem, Stack recursionStack)
  {
    Enumeration children;
    PcmlDocNode child;
    String structName;
    PcmlDocNode structNode;
    RfmlData   dataNode;

    children = RfmlElem.getChildren();
    if (children == null)
      return;

    while (children.hasMoreElements())
    {
      child = (PcmlDocNode) children.nextElement();

      // Only augment the tree for <data type="struct"> items.
      // This also makes sure the <data> node has no children
      // though this should always be the case because the DTD does
      // allow the <data> tag to have nested elements.
      if (child instanceof RfmlData)  // This node is a <data> tag
      {
        dataNode = (RfmlData) child;
        if (dataNode.getDataType() == RfmlData.STRUCT) // and it is type="struct"
        {
          if (dataNode.getNbrChildren() == 0)        // and it has no children
          {
            structName = dataNode.getStruct();
            if (structName != null)
            {
              structNode = (PcmlDocNode) m_rootNode.getElement(structName);
              if (structNode instanceof RfmlStruct)
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
                    m_rootNode.addToHashtable(newChild);
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

  /**
   Processes the PcmlDocNode tree to resolve references to
   named nodes in the tree.

   Some nodes (e.g. <data>) have attributes with named references
   to other nodes (e.g. offset="xyz"). The named reference can
   be either a simple name or a qualified name. The name
   resolution is performed by walking up the document heirarchy looking
   for a named element relative to the current location in the tree.

   Given the following document structure:
   <rfml>
     <recordformat name="qabc">
       <data name="parm1" type="struct" struct="parm1"/>
     </recordformat>
     <struct  name="parm1">
       <data name="struct1" type="struct" struct="struct1"/>
     </struct>
     <struct name="struct1">
       <data name="lengthOfXyz" type="int" length="4">
       <data name="xyz"         type="char" length="lengthOfXyz">
     </struct>
   </rfml>

   The element named "lengthOfXyz" is referenced by element "xyz" as
   the length of the character field. In this case the following
   names could be specified on the length attribute and would
   resolve to the same element in the document:
   -- length="lengthOfXyz"
   -- length="struct1.lengthOfXyz"
   -- length="parm1.struct1.lengthOfXyz"
   -- length="qabc.parm1.struct1.lengthOfXyz"

   The resolution process recursively asks the nodes's parent if the
   name string is a descendant of the current node.
   **/
  private void checkAttributes(PcmlDocNode RfmlElem)
  {
    Enumeration children;
    PcmlDocNode child;

    children = RfmlElem.getChildren();
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

  /** Returns the root node for the document.
   **/
  RfmlDocument getRfmlDocument()
  {
    return m_rootNode;
  }

  /** Parses the document, using the specified class loader.
   **/
  void parse(String docName, ClassLoader loader)
  throws MissingResourceException, IOException, ParseException, PcmlSpecificationException, //@E0C
  SAXNotRecognizedException, SAXNotSupportedException, ParserConfigurationException,
  SAXException
  {
    m_rootNode = null;
    m_currentNode = null;

    String qualDocName;    // Potentially package qualified document name.

    // Save the document name
    if (docName.endsWith(".rfml")
        || docName.endsWith(".rfmlsrc"))
    {
      qualDocName = docName.substring(0, docName.lastIndexOf('.') );
    }
    else
    {
      qualDocName = docName;
    }

    m_docName = qualDocName.substring(qualDocName.lastIndexOf('.') + 1);

    // Now try to open the Rfml file
    InputStream inStream = SystemResourceFinder.getRFMLDocument(docName, loader);  // TBD: Buffer the InputStream???  On AIX at least, this always appears to return a java.io.BufferedInputStream.

    // Instantiate our error listener
    if (m_xh == null) {
      m_xh = new XMLErrorHandler(m_docName, 0);
    }

    SAXParserFactory factory = SAXParserFactory.newInstance(); //@E0A
    factory.setValidating(true); //@E0A
    factory.setNamespaceAware(false); //@E0A
//@E0D            SAXParser parser = new SAXParser();
//@E0D             try {
//@E0D               parser.setFeature("http://xml.org/sax/features/validation", true);
    ///parser.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
    factory.setFeature("http://apache.org/xml/features/continue-after-fatal-error", false); // TBD: This is to eliminate hang condition if rfml.dtd is not found by SAXParser.parse().  See below.
    SAXParser parser = factory.newSAXParser(); //@E0A

//@E0D               parser.setFeature( "http://xml.org/sax/features/namespaces", false );
//@E0D             }
//@E0D             catch (org.xml.sax.SAXException se) {
//@E0D               if (Trace.isTraceErrorOn()) se.printStackTrace(Trace.getPrintWriter());
    ///System.out.println("Exception = " + se);
//@E0D             }
//@E0D             parser.setErrorHandler(xh);
//@E0D             parser.setContentHandler(this);
//@E0D             parser.setEntityResolver(this);  // So that we can find the rfml.dtd for the parser.

    // Create an InputSource for passing to the parser.
    // Wrap any SAXExceptions as ParseExceptions.
    try
    {
      XMLReader reader = parser.getXMLReader(); //@E0A
      reader.setErrorHandler(m_xh); //@E0A
      reader.setEntityResolver(this);  //@E0A So that we can find the rfml.dtd for the parser.
      parser.parse(new InputSource(inStream), this); // TBD: This hangs if rfml.dtd can't be found and "continue-after-fatal-error" is set to true. @E0C
    }
    catch (SAXException e)
    {
      if (Trace.isTraceErrorOn()) e.printStackTrace(Trace.getPrintWriter());
      ParseException pe = new ParseException(SystemResourceFinder.format(DAMRI.FAILED_TO_PARSE, new Object[] {m_docName} ) );
      pe.addMessage(e.getMessage());
      throw pe;
    }

    // Close the input stream
    inStream.close();

    // Check for errors
    ParseException exc = m_xh.getException();
    if (exc != null)
    {
      ///exc.reportErrors();   // Note - This is redundant with a call in loadSourceXxxDocument() in RecordFormatDocument and ProgramCallDocument.
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

    if (m_rootNode != null && m_rootNode.getPcmlSpecificationException() != null)
      throw m_rootNode.getPcmlSpecificationException();
  }

  /**
   Implementation of method of interface org.xml.sax.EntityResolver.
   Allows the application to resolve external entities.

   The Parser will call this method before opening any external entity except the top-level document entity (including the external DTD subset, external entities referenced within the DTD, and external entities referenced within the document element): the application may request that the parser resolve the entity itself, that it use an alternative URI, or that it use an entirely different input source.
   **/
  public InputSource resolveEntity (String publicId, String systemId)
  throws SAXException
  {
    // Note: publicId is generally null.
    if (systemId != null && systemId.length() > 0)
    {
      int finalSlashPos = systemId.lastIndexOf("/");
      String sysFile;

      if (finalSlashPos == -1)
      {
        sysFile = systemId;
      }
      else
      {
        sysFile = systemId.substring(finalSlashPos+1);
      }

      if (sysFile.equals("rfml.dtd"))
      {
        InputStream isHeader = SystemResourceFinder.getRFMLHeader();
        return new InputSource(isHeader);
      }
    }

    return super.resolveEntity(publicId, systemId);
  }


  /***************************************************************
   The following are extensions to the org.xml.sax.HandlerBase
   which is a default implementation of DocumentHandler.
   ****************************************************************/

  /** Start element. **/
  public void startElement(String namespaceURI, String localname, String tagName, Attributes xmlAttrs)
  {

    PcmlDocNode newNode = null;


    // Create a PcmlAttributeList to hold all the
    // attributes for this node.
    PcmlAttributeList attrs = new PcmlAttributeList(xmlAttrs.getLength());
    for (int attr = 0; attr < xmlAttrs.getLength(); attr++)
    {
// $A1 
      attrs.addAttribute( new PcmlAttribute(xmlAttrs.getQName(attr),
                                            xmlAttrs.getValue(attr),
                                            true) );
    }

    // Create PcmlDocNode subclass based on tag name
    if (tagName.equals("rfml"))
    {
      newNode = new RfmlDocument(attrs, m_docName);
    }
    else if (tagName.equals("recordformat"))
    {
      newNode = new RfmlRecordFormat(attrs);

    }
    else if (tagName.equals("struct"))
    {
      newNode = new RfmlStruct(attrs);
    }
    else if (tagName.equals("data"))
    {
      ///newNode = new RfmlData(attrs);
      newNode = new RfmlData(attrs);
    }
    else
    {
      newNode = null; // Unrecognized tag name chould never happen
      // if the tags parse successfully
    }

    if (newNode != null)
    {
      if (m_rootNode == null)
      {
        m_rootNode = (RfmlDocument) newNode;
        m_currentNode = newNode;
      }
      else
      {
        m_currentNode.addChild(newNode);
        m_currentNode = newNode;
      }
    }

  } // startElement(String,AttributeList)

  /** End element. **/
  public void endElement(String namespaceURI, String localname, String element)
  {

    m_currentNode = (PcmlDocNode) m_currentNode.getParent();

  }

  // See Java Bug ID 4806878, http://developer.java.sun.com/developer/bugParade/bugs/4806878.html:
  // "[The] W3C XML Spec says that validation errors are not fatalerrors. And DefaultHandler implementation doesn't print out any errors for the validation errors. If user is interested in seeing the validation errors, then they need to extend the DefaultHandler [by re-implementing error() and fatalerror().]"

  public void warning(SAXParseException spe)
  throws SAXException
  {
    if (m_xh == null) throw spe;
    else m_xh.warning(spe);
  }
  public void error(SAXParseException spe)
  throws SAXException
  {
    if (m_xh == null) throw spe;
    else m_xh.error(spe);
  }
  public void fatalError(SAXParseException spe)
  throws SAXException
  {
    if (m_xh == null) throw spe;
    else m_xh.fatalError(spe);
  }
}
