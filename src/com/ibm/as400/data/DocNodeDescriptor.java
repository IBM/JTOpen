///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DocNodeDescriptor.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import java.util.Enumeration;
import java.util.Vector;
import com.ibm.as400.access.InternalErrorException;                 // @A1A
import com.ibm.as400.access.Trace;                                  // @A1A

/**
  *  The DocNodeDescriptor class is an abstract class that provide implementations
  *  for common methods in the Descriptor interface.
  *
  **/

abstract class DocNodeDescriptor implements Descriptor
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    protected PcmlDocNode m_docNode;            // The PcmlDocNode object that is described by this Descriptor

    /* Constructor */
    public DocNodeDescriptor(PcmlDocNode node)
    {
        m_docNode = node;
    }
   
    public PcmlDocNode getDocNode()
    {
        return m_docNode;
    }
   /**
    * The following method is implemented in the subclasses.
    **/
    abstract public String[] getAttributeList();

   /**
    * The following method is implemented in the subclasses.
    **/
    abstract public String getAttributeValue(String attr);

   /*
    * Returns the Descriptors of the children of this node.
    *
    * @return Descriptors for the children of this Descriptor as an Enumeration.
    *         If there are no children, an empty enumeration is returned.
    **/
    public Enumeration getChildren()
    {
        int nbrChildren = m_docNode.getNbrChildren();           // Get number of children 
        Vector v = new Vector(nbrChildren);                     // Allocate correct vector size
        
        if (nbrChildren > 0)
        {
            PcmlDocNode dnChild;                                // Used to generate child Descriptors
            DocNodeDescriptor child;                              
        
            Enumeration dnChildren = m_docNode.getChildren();
            while (dnChildren.hasMoreElements())                // For each PcmlDocNode
            {
                dnChild = (PcmlDocNode)dnChildren.nextElement();             // Get the next PcmlDocNode
                if (dnChild instanceof PcmlStruct)            
                {
                    child = new StructDescriptor(dnChild);      // Allocate StructDescriptor
                }
                else if (dnChild instanceof PcmlData)
                {
                    child = new DataDescriptor(dnChild);        // Allocate DataDescriptor
                }
                else if (dnChild instanceof PcmlProgram)
                {
                    child = new ProgramDescriptor(dnChild);     // Allocate ProgramDescriptor
                }
                else if (dnChild instanceof RfmlRecordFormat)               // @A1A
                {
                    child = new RecordFormatDescriptor(dnChild);
                }
                else     // None of the above                               // @A1A
                {
                  Trace.log(Trace.ERROR, "Unrecognized child element type: " + dnChild.getClass().getName());  // @A1A
                  throw new InternalErrorException(InternalErrorException.UNKNOWN); // @A1A
                }
                v.addElement(child);                            // Add child to vector
            }
        }
        return v.elements();                                    // Return Enumeration of children
    }

   /**
    * Returns the Descriptor for another element contained within the same document.
    *
    * @return Descriptor for another element  within the same document given a qualified
    *         name (i.e. - "structName1.structName2.dataElementName").
    *         Returns null if there is no element with the specified name.
    **/
    public Descriptor getDescriptor(String qualifiedName)
    {
        PcmlDocNode dn = (PcmlDocNode)m_docNode.getDoc().getElement(qualifiedName);
        if (dn == null)
            return null;
        if (dn instanceof PcmlStruct)            
        {
            return new StructDescriptor(dn);        // Allocate StructDescriptor
        }
        else if (dn instanceof PcmlData)
        {
            return new DataDescriptor(dn);          // Allocate DataDescriptor
        }
        else if (dn instanceof PcmlProgram)
        {
            return new ProgramDescriptor(dn);       // Allocate ProgramDescriptor
        }
        else if (dn instanceof RfmlRecordFormat)                            // @A1A
        {
            return new RecordFormatDescriptor(dn);
        }
        else     // None of the above                                         @A1A
        {
          Trace.log(Trace.ERROR, "Unrecognized element type: " + dn.getClass().getName()); // @A1A
          throw new InternalErrorException(InternalErrorException.UNKNOWN); // @A1A
        }
    }

   /**
    * Returns the simple name of the Descriptor as specified on the name= attribute of its
    * associated tag.
    *
    * @return String containing the simple name of the Descriptor as specified on the name=
    *         attribute of its associated tag.
    *         If there is no name= attribute, an empty String ("") is returned.
    **/
    public String getName()
    {
        return m_docNode.getName();
    }

   /**
    * Returns the qualified name of the Descriptor.  This qualified name is the simple name
    * prefixed with the qualified name of this Descriptor's parent and separated with a period (".").
    *
    * @return String containing the qualified name of the Descriptor.
    *         If there is no name= attribute, an empty String ("") is returned.
    **/
    public String getQualifiedName()
    {
        return m_docNode.getQualifiedName();
    }

   /**
    * Returns the tag name of the Descriptor.
    *
    *
    * @return String containing the tag name of the Descriptor.
    *         For example, this method could return a String containing one of the following values:
    *         pcml
    *         struct
    *         data
    *         program
    **/
    public String getTagName()
    {
        return m_docNode.getTagName();
    }

   /**
    * Returns true if this Descriptor has childen.
    * Returns false if this Descriptor has no childen.
    *
    *
    * @return Boolean indicating if the Descriptor has children.
    **/
    public boolean hasChildren()
    {
        return m_docNode.hasChildren();
    }

}
