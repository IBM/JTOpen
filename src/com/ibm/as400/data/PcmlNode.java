///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PcmlNode.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import com.ibm.as400.access.Trace;
import java.io.Serializable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;


abstract class PcmlNode implements Serializable, Cloneable {        // @C2A
    static final long serialVersionUID = -2955209136470053178L;	    // @C1A
    private static Hashtable separatorsMap_;

    private PcmlNode parent;
    private PcmlNode prevSibling;
    private PcmlNode nextSibling;
    private PcmlNode firstChild;
    private PcmlNode lastChild;
    private int nbrChildren;

    PcmlNode() 
    {
        parent = null;
        prevSibling = null;
        nextSibling = null;
        firstChild = null;
        lastChild = null;
        nbrChildren = 0;
    }
    
    // All pcml document nodes support clone.
    // This is a deep clone in that the result is to 
    // clone an entire subtree. This method recursively 
    // clones its children.
    public Object clone()                                           // @C2A
    {                                                               // @C2A
        PcmlNode node = null, child = null, newChild = null;        // @C2A
        try                                                         // @C2A
        {                                                           // @C2A
            node = (PcmlNode) super.clone();                        // @C2A
            node.parent = null;                                     // @C2A
            node.firstChild = null;                                 // @C2A
            node.lastChild = null;                                  // @C2A
            node.prevSibling = null;                                // @C2A
            node.nextSibling = null;                                // @C2A
            node.nbrChildren = 0;                                   // @C2A
        }                                                           // @C2A
        catch (CloneNotSupportedException e)                        // @C2A
        {}                                                          // @C2A

        return node;                                                // @C2A
    }                                                               // @C2A

	// Custom deserialization post-processing
	// This processing cannot be done during readObject() because
	// references to parent objects in the document are not yet set
	// due to the recursive nature of deserialization.
    void readObjectPostprocessing()                                 // @C1A
    {                                                               // @C1A
        Enumeration items;                                          // @C1A
        PcmlNode child;                                             // @C1A

        if (getNbrChildren() == 0)                                  // @C1A
            return;                                                 // @C1A

        items = getChildren();                                      // @C1A
        while (items.hasMoreElements() )                            // @C1A
        {                                                           // @C1A
            child = (PcmlNode) items.nextElement();                 // @C1A
            child.readObjectPostprocessing();                       // @C1A
        }                                                           // @C1A
    }                                                               // @C1A

    public abstract String getName();

    public abstract String getQualifiedName();
    
    protected final PcmlNode getParent() 
    {
        return parent;
    }

    private final void setParent(PcmlNode node) 
    {
        parent = node;
    }

    protected final PcmlNode getNextSibling() 
    {
        return nextSibling;
    }

    private final void setNextSibling(PcmlNode node) 
    {
        nextSibling = node;
    }

    protected final PcmlNode getPrevSibling() 
    {
        return prevSibling;
    }

    private final void setPrevSibling(PcmlNode node) 
    {
        prevSibling = node;
    }

    protected Enumeration getChildren() 
    {
        Vector v = new Vector(nbrChildren);
        PcmlNode child = firstChild;
        while (child != null) 
        {
            v.addElement(child);
            child = child.getNextSibling();
        }
        return v.elements();
    }

    protected final int getNbrChildren() 
    {
        return nbrChildren;
    }

    protected final int getChildNbr() 
    {
    	  int birthOrder = 0;
    	  PcmlNode p = this;
    	  while (p.getPrevSibling() != null) 
    	  {
    	  	  birthOrder++;
    	  	  p = p.getPrevSibling();
    	  }
        return birthOrder;
    }

    protected final boolean  hasChildren() 
    {
        return nbrChildren > 0;
    }

    protected void addChild(PcmlNode node) 
    {
        if (!hasChildren()) 
        {
            firstChild = node;
        }
        else
        {
            lastChild.setNextSibling(node);
            node.setPrevSibling(lastChild);
        }
        lastChild = node;
        node.setParent(this);
        nbrChildren++;
    }

    // Return the root node of this node
    protected PcmlDocRoot getRootNode() 
    {
        PcmlNode p = this;
        PcmlNode q = parent;                    // @C4A

        while ( q != null )                     // @C4C
        {
            p = q;                              // @C4A
            q = p.getParent();                  // @C4C
        }
        
        if (p instanceof PcmlDocRoot) 
        {
            return (PcmlDocRoot) p;
        }
        else 
        {
            return null;
        }
    }

    // Print the PcmlDescNode tree
    protected void printTree(PcmlNode p, int level)
    {
        Enumeration items;
        PcmlNode child;

        for (int i = 0; i<level; i++) 
        {
            System.out.print("  ");
        }
        System.out.println(p.toString());
        items = p.getChildren();

        if (items == null)
            return;

        while (items.hasMoreElements() ) 
        {
            child = (PcmlNode) items.nextElement();
            printTree(child, level+1);
        }
    }

    // Separator characters.
    static final Character AMPERSAND = new Character('&');
    static final Character BLANK = new Character(' ');
    static final Character COLON = new Character(':');
    static final Character COMMA = new Character(',');
    static final Character HYPHEN = new Character('-');
    static final Character PERIOD = new Character('.');
    static final Character SLASH = new Character('/');


    private static Hashtable getSeparatorsMap()
    {
      if (separatorsMap_ == null)
      {
        synchronized (PcmlNode.class)
        {
          if (separatorsMap_ == null)
          {
            separatorsMap_ = new Hashtable(12);
            separatorsMap_.put("ampersand", AMPERSAND);
            separatorsMap_.put("blank", BLANK);
            separatorsMap_.put("colon", COLON);
            separatorsMap_.put("comma", COMMA);
            separatorsMap_.put("hyphen", HYPHEN);
            separatorsMap_.put("period", PERIOD);
            separatorsMap_.put("slash", SLASH);
          }
        }
      }
      return separatorsMap_;
    }

    static boolean isValidSeparatorName(String separatorName)
    {
      boolean found = getSeparatorsMap().containsKey(separatorName);
      if (found) return true;
      else return (separatorName.equals("none"));  // special value indicating "no separator"
    }


    // Returns the char value associated with a given separator name.
    // Returns null if separatorName is "none" or if the name is not recognized.
    // For that reason, caller should first validate the separatorName by calling isValidSeparatorName().
    static Character separatorAsChar(String separatorName)
    {
      return (Character)getSeparatorsMap().get(separatorName);
    }

}
