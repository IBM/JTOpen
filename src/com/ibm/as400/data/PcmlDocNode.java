///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PcmlDocNode.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import com.ibm.as400.access.AS400;

import java.util.Enumeration;

abstract class PcmlDocNode extends PcmlNode
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    
    public final int PROGRAM = 1;
    public final int STRUCT  = 2;
    public final int DATA    = 3;
    public final int RECORDFORMAT = 5;  // @D0A

    /***********************************************************
     Static Members
    ***********************************************************/

    /* Usage */
    public static final int UNSUPPORTED = 0;
    public static final int INHERIT     = 1;
    public static final int INPUT       = 2;
    public static final int OUTPUT      = 3;
    public static final int INPUTOUTPUT = 4;

    // Serial verion unique identifier
    static final long serialVersionUID = 2085968464631195453L;

    // This member is transient because it is only needed when
    // instantiated (parse) from source tags.
    protected transient PcmlAttributeList m_XmlAttrs;
    private   String    m_Name;
    private   String    m_QualName;
    private   String    m_TagName;
    private   int       m_NodeType;
    private   int       m_Usage;
    private   String    m_Type;    // value of "type=" attribute
    private   String    m_Struct;  // value of "struct=" attribute

    // @E0A - Add holder for condensed name if used
    private String m_CondensedName;                        //@E0A
    private boolean m_IsExtendedType;                     //@E0A

    // @E0A - Add counter for use in xpcml output processing
    private   int       m_CountReps;                    //@E0A


    // Constructor
    public PcmlDocNode()
    {
        m_XmlAttrs = null;
        m_Name = null;
        m_QualName = null;
        m_TagName = "";
        m_CondensedName="";                                        //@E0A
        m_IsExtendedType=false;                                    //@E0A
        m_NodeType = PcmlNodeType.UNSUPPORTED;
        m_CountReps = -10;                                         //@E0A
    }

    // Constructor
    public PcmlDocNode(PcmlAttributeList attrs)                     // @C1A
    {                                                               // @C1A
        m_XmlAttrs = attrs;                                         // @C1A
        m_TagName = "";                                             // @C1A
        m_CondensedName="";                                         //@E0A
        m_IsExtendedType=false;                                    //@E0A
        m_CountReps=-10;                                            //@E0A
        m_NodeType = PcmlNodeType.UNSUPPORTED;                      // @C1A

        // Set name= attribute value
        m_Name = getAttributeValue("name");                         // @C1A
        m_QualName = null;                                          // @C1A

        // Set usage= attribute value
        setUsage(getAttributeValue("usage"));                       // @C1A

        // Set type= attribute value
        m_Type = getAttributeValue("type");

        // Set struct= attribute value
        m_Struct = getAttributeValue("struct");
    }                                                               // @C1A

    public Object clone()
    {
        PcmlDocNode child = null, newChild = null;

        PcmlDocNode node = (PcmlDocNode) super.clone();
        node.m_QualName = null;

        // Now clone children
        Enumeration children = getChildren();                       // @C2A
        while (children.hasMoreElements())                          // @C2A
        {                                                           // @C2A
            child = (PcmlDocNode) children.nextElement();           // @C2A
            newChild = (PcmlDocNode) child.clone();                 // @C2A
            node.addChild(newChild);                                // @C2A
        }                                                           // @C2A

        return node;
    }


    // Add a child to this element
    protected void addChild(PcmlNode child)                         // @C2C
    {
        String qName; // Qualified name of child


        super.addChild(child);

        qName = child.getQualifiedName();
        if ( !qName.equals("") && getDoc() != null)                 // @C2C
        {
            if ( getDoc().containsElement(qName) )
            {
               getDoc().addPcmlSpecificationError(DAMRI.MULTIPLE_DEFINE, new Object[] {qName} );
            }
            ((PcmlDocRoot) getRootNode()).addElement(child);
        }
    }

    // Get the AS400 system object for this document
    AS400 getAs400()
    {
        return getDoc().getAs400();
    }

    // Get the AS400 VRM for this document
    int getAs400VRM() throws PcmlException
    {
        return getDoc().getAs400VRM();
    }

    // Get the document node (PcmlDocument)
    final PcmlDocument getDoc()
    {
        return (PcmlDocument) getRootNode();
    }

    // Get the name of the document element (NAME=)
    public String getName()
    {
        if (m_Name == null)
            return "";
        else
            return m_Name;
    }

    // Get the fully qualified name of the document element
    // The qualified name is the concatentation of the names
    // of all anscestors and the name of "this" with each
    // name separated by a period.
    public String getQualifiedName()
    {
        if (m_QualName != null && !m_QualName.equals("") )
        {
            return m_QualName;
        }

        String anscestor;
        String myName = getName();

        // If the node does not have a parent return and empty string.
        // A node without a parent is either the PcmlDocument (root of the tree)
        // or it is a node that is not inserted into a document tree (yet).
        if ( getDoc() == null )                                     // @C2A
        {                                                           // @C2A
            return "";                                              // @C2A
        }                                                           // @C2A

        if ( getParent() == null )
        {
            return "";
        }

        // For nodes in the middle of the heirarchy that have no name,
        // stop going up the heirarchy.  I.E. Do not give name like:
        //   "a.b..d.e"
        if ( myName.equals("") && getParent().getParent() != null )
        {
            return "";
        }

        anscestor = ((PcmlDocNode)getParent()).getQualifiedName();

        if ( anscestor.equals("") )
        {
            if ( getParent().getParent() != null )
            {
                return "";
            }
            else
            {
                m_QualName = myName;
                return m_QualName;
            }
        }
        else
        {
            m_QualName = anscestor + "." + myName;
            return m_QualName;
        }
    }

    // Get the fully qualified name of the document element
    // for use in exceptions.
    // Whereas getQualifiedName() returns an emptry string if
    // any of the ancestors is not named, this method
    // returns a full name with a child number for
    // any unnamed ancestors.
    // The qualified name is the concatentation of the names
    // of all anscestors and the name of "this" with each
    // name separated by a period.
    public String getNameForException()
    {
        String anscestor;
        String myName = getName();

        if ( getParent() == null )
        {
            return "";
        }

        // For nodes in the middle of the heirarchy that have no name,
        // stop going up the heirarchy.  I.E. Do not give name like:
        //   "a.b..d.e"
        if ( myName.equals("") )
        {
            myName = "[" + Integer.toString( getChildNbr() ) + "]";
        }

        anscestor = ((PcmlDocNode)getParent()).getQualifiedName();

        if ( anscestor.equals("") )
        {
            return myName;
        }
        else
        {
            return anscestor + "." + myName;
        }
    }

    // Returns one of the PcmlNodeType constants indicating
    // what type of node this is:
    // Possible values:
    //    PcmlNodeType.DOCUMENT
    //    PcmlNodeType.PROGRAM
    //    PcmlNodeType.STRUCT
    //    PcmlNodeType.DATA
    //    PcmlNodeType.RECORDFORMAT
    int getNodeType()
    {
        return m_NodeType;
    }

    protected void setNodeType(int theType)                         // @C1A
    {                                                               // @C1A
        m_NodeType = theType;                                       // @C1A
        switch (m_NodeType)                                         // @C1A
        {                                                           // @C1A
            case PcmlNodeType.DOCUMENT:                             // @C1A
                m_TagName = "pcml";                                 // @C1A
                break;                                              // @C1A
            case PcmlNodeType.RFML:                                 // @D0A
                m_TagName = "rfml";                                 // @D0A
                break;                                              // @D0A
            case PcmlNodeType.PROGRAM:                              // @C1A
                m_TagName = "program";                              // @C1A
                break;                                              // @C1A
            case PcmlNodeType.RECORDFORMAT:                         // @D0A
                m_TagName = "recordformat";                         // @D0A
                break;                                              // @D0A
            case PcmlNodeType.STRUCT:                               // @C1A
                m_TagName = "struct";                               // @C1A
                break;                                              // @C1A
            case PcmlNodeType.DATA:                                 // @C1A
                m_TagName = "data";                                 // @C1A
                break;                                              // @C1A
        }                                                           // @C1A

    }                                                               // @C1A

    // Return value of "struct=" attribute.
    // If "type" attribute value is not "struct", returns null.
    protected String getStructName()
    {
        return m_Struct;
    }

    // Return value of "type=" attribute.
    protected String getType()
    {
        return m_Type;
    }

    public int getUsage()
    {
        if (m_Usage == INHERIT)
        {
            if (getParent() == null)
            {
                return INPUTOUTPUT;
            }
            else
            {
                return ((PcmlDocNode) getParent()).getUsage();
            }
        }
        return m_Usage;
    }

    // Resolve a relative name and return its qualified name
    String resolveRelativeName(String relativeName)
    {
        PcmlDocNode relativeNode = resolveRelativeNode(relativeName);
        if (relativeNode instanceof PcmlDocNode)
            return relativeNode.getQualifiedName();
        else
            return null;
    }

    // Resolve a relative name and return the resolved node
    PcmlDocNode resolveRelativeNode(String relativeName)
    {
        PcmlDocNode p = (PcmlDocNode) this.getParent();
        PcmlDocRoot root = getRootNode();
        PcmlDocNode relativeNode = null;
        String currentName;

        if (relativeName == null)
            return null;

        while ( p != null )
        {
            currentName = p.getQualifiedName();
            if (currentName.equals(""))
            {
                currentName = relativeName;
            }
            else
            {
                currentName = currentName + "." + relativeName;
            }
            relativeNode = (PcmlDocNode) root.getElement(currentName);
            if (relativeNode != null)
            {
                return relativeNode;
            }
            p = (PcmlDocNode) p.getParent();
        }
        return null;
    }

    // Returns an Enumeration containing the children of this element
    public String toString()
    {
        String myName = getQualifiedName();
        if (myName.equals(""))
        {
            return super.toString();
        }
        else
            return super.toString() + ":" + myName;                 // @C2C
    }

    // Returns a string containing the tag name for this node
    String getTagName()
    {
        return m_TagName;
    }

    // Returns a string containing the tag name for this node
    String getBracketedTagName()
    {
        return "<" + m_TagName + ">";
    }

    // Returns a list of the names of all attributes for this node.   // @D0A
    abstract String[] getAttributeList();

    // Returns a string containing the value of the specified attribute.
    // Note: This method must not be called on a deserialized object,
    // since m_XmlAttrs is transient and will be null.
    protected String getAttributeValue(String attributeName)
    {
        return m_XmlAttrs.getAttributeValue(attributeName);         // @C1C
    }


    private void setUsage(String usage)
    {
        if (usage == null || usage.equals(""))                      // @B1C
            m_Usage = INHERIT;
        else
        if (usage.equals("inherit"))
            m_Usage = INHERIT;
        else
        if (usage.equals("input"))
            m_Usage = INPUT;
        else
        if (usage.equals("output"))
            m_Usage = OUTPUT;
        else
        if (usage.equals("inputoutput"))
            m_Usage = INPUTOUTPUT;
        else
            m_Usage = UNSUPPORTED;
    }

    protected void checkAttributes()
    {
        // Verify the syntax of the name= attribute
        // The name cannot begin with '%' (percent) or '[' (left brace) and
        // cannot contain '.' (period) or ' ' (blank).
        if (m_Name != null)
        {
            if (m_Name.indexOf('%') == 0
             || m_Name.indexOf('/') == 0
             || m_Name.indexOf('\\') == 0
             || m_Name.indexOf(':') == 0
             || m_Name.indexOf('[') == 0
             || m_Name.indexOf('.') > -1
             || m_Name.indexOf(' ') > -1 )
            {
                getDoc().addPcmlSpecificationError(DAMRI.BAD_ATTRIBUTE_SYNTAX, new Object[] {makeQuotedAttr("name", m_Name), getBracketedTagName(), getNameForException()} );
            }
        }

        if (m_Usage == UNSUPPORTED)
        {
            getDoc().addPcmlSpecificationError(DAMRI.BAD_ATTRIBUTE_SYNTAX, new Object[] {makeQuotedAttr("usage", getAttributeValue("usage")), getBracketedTagName(), getNameForException()} );
        }

        if (m_NodeType == PcmlNodeType.UNSUPPORTED)
        {
            getDoc().addPcmlSpecificationError(DAMRI.BAD_TAG, new Object[] {getBracketedTagName(), getNameForException()} );
        }
    }

    protected static String makeQuotedAttr(String attrName, String attrValue)
    {
        return attrName + "=\"" + attrValue + "\"";
    }

    protected static String makeQuotedAttr(String attrName, int attrValue)
    {
        return makeQuotedAttr(attrName, Integer.toString(attrValue));
    }

    // ******************************
    // @E0A -- New methods for XPCML *
    // ******************************
    void setCondensedName(String condensedName)
    {
        m_CondensedName = condensedName;
    }

    String getCondensedName()
    {
        return m_CondensedName;
    }

    void setIsExtendedType(boolean extendedType)
    {
        m_IsExtendedType = extendedType;
    }

    boolean getIsExtendedType()
    {
        return m_IsExtendedType;
    }

    protected void setCountReps(int reps)
    {
        m_CountReps = reps;
    }

    protected int getCountReps()
    {
        return m_CountReps;
    }
}
