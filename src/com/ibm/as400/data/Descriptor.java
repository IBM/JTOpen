///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: Descriptor.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import java.util.Enumeration;

/**
  *  The Descriptor interface defines a mechanism for inspecting a node
  *  of an XML based document.
  *  <p>
  *  The idea is to give a programmer the ability to inspect the various
  *  tags and attribute values without having to parse the tags.  This is
  *  useful in that some XML based documents do post-parser processing
  *  that can then be handled appropriately by implementations of this
  *  interface.
  *
  **/

public interface Descriptor
{

   /**
    * Returns an array of attribute names for the current node.
    *
    * @return Array containing the names of all attributes associated with
    *         the node type for this descriptor.
    *         If there are no attributes for this descriptor, an empty array is returned.
    **/
    public String[] getAttributeList();

   /**
    * Returns the value of the named attribute.
    *
    * @return String containing the value of the specified attribute.
    *         If the named attribute is not valid for this descriptor, null is returned.
    **/
    public String getAttributeValue(String attr);

   /**
    * Returns the Descriptors of the children of this node.
    *
    * @return Descriptors for the children of this Descriptor as an Enumeration.
    *         If there are no children, an empty enumeration is returned.
    **/
    public Enumeration getChildren();

   /**
    * Returns the Descriptor for another element contained within the same document.
    *
    * @return Descriptor for another element  within the same document given a qualified
    *         name (i.e. - "structName1.structName2.dataElementName").
    *         Returns null if there is no element with the specified name.
    **/
    public Descriptor getDescriptor(String qualifiedName);

   /**
    * Returns the simple name of the Descriptor as specified on the name= attribute of its
    * associated tag.
    *
    * @return String containing the simple name of the Descriptor as specified on the name=
    *         attribute of its associated tag.
    *         If there is no name= attribute, null is returned.
    **/
    public String getName();

   /**
    * Returns the qualified name of the Descriptor.  This qualified name is the simple name
    * prefixed with the qualified name of this Descriptor's parent and separated with a period (".").
    *
    * @return String containing the qualified name of the Descriptor.
    *         If there is no qualified name, null is returned.
    **/
    public String getQualifiedName();

   /**
    * Returns the tag name of the Descriptor.
    *
    *
    * @return String containing the tag name of the element in the Descriptor.
    **/
    public String getTagName();

   /**
    * Returns true if this Descriptor has childen.
    * Returns false if this Descriptor has no childen.
    *
    *
    * @return Boolean indicating if the Descriptor has children.
    **/
    public boolean hasChildren();

}
