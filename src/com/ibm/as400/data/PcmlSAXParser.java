///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PcmlSAXParser.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.io.IOException;
import java.io.SequenceInputStream;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.Stack;


class PcmlSAXParser extends DefaultHandler
{

  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";


  private transient PcmlDocument m_rootNode;
  private transient PcmlDocNode  m_currentNode;
  private transient String       m_docName;

  /**
  */
  PcmlSAXParser(String docName, ClassLoader loader)                       // @C2C
  throws MissingResourceException, IOException, ParseException, PcmlSpecificationException,
         FactoryConfigurationError, ParserConfigurationException, SAXException
  {
    m_rootNode = null;
    m_currentNode = null;

    String qualDocName;    // Potentially package qualified document name   @A2A

    // Save the document name
    if (docName.endsWith(".pcml")
        || docName.endsWith(".pcmlsrc"))
    {
      qualDocName = docName.substring(0, docName.lastIndexOf('.') );   // @A2C
    }
    else
    {
      qualDocName = docName;                                           // @A2C
    }

    m_docName = qualDocName.substring(qualDocName.lastIndexOf('.') + 1); // @A2A

    // Open the PCML header document that contains the DTD
    InputStream isHeader = SystemResourceFinder.getPCMLHeader();

    // Now try to open the PCML file
    InputStream isPCML = SystemResourceFinder.getPCMLDocument(docName, loader); // @C2C

    // Concatenate the two input streams
    SequenceInputStream sis = new SequenceInputStream(isHeader, isPCML);

    // Instantiate our error listener
    XMLErrorHandler xh = new XMLErrorHandler(m_docName, SystemResourceFinder.getHeaderLineCount());

    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(true);
    factory.setNamespaceAware(false);
    SAXParser parser = factory.newSAXParser();

    // Create an InputSource for passing to the parser.
    // Wrap any SAXExceptions as ParseExceptions.
    try
    {
      XMLReader reader = parser.getXMLReader();
      reader.setErrorHandler(xh);
      parser.parse(new InputSource(sis), this);
    }
    catch (SAXException e)
    {
      ParseException pe = new ParseException(SystemResourceFinder.format(DAMRI.FAILED_TO_PARSE, new Object[] {m_docName} ) );
      pe.addMessage(e.getMessage());
      throw pe;
    }
    // Close the input stream
    sis.close();
    sis = null;
    isPCML = null;

    // Check for errors
    ParseException exc = xh.getException();
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

    if (m_rootNode != null && m_rootNode.getPcmlSpecificationException() != null)
      throw m_rootNode.getPcmlSpecificationException();
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


  public void startElement(String uri, String localName, String qName, Attributes xmlAttrs)
  {
    String tagName = qName;
    PcmlDocNode newNode = null;

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
      newNode = null; // Unrecognized tag name chould never happen 
      // if the tags parse successfully
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


  public void endElement(String uri, String localName, String qName)
  {
    m_currentNode = (PcmlDocNode) m_currentNode.getParent();
  }
}
