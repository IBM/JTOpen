///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: UnorderedList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.Trace;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.ExtendedIllegalArgumentException;

import java.beans.PropertyChangeSupport;



/**
*  The UnorderedList class represents an HTML unordered list, &lt;ul&gt;.
*  <P>
*  This example creates a UnorderedList tag:
*  <BLOCKQUOTE><PRE>
*  // Create an UnorderedList.
*  UnorderedList list = new UnorderedList();
*  <p>
*  // Use circles when displaying the list items.
*  list.setType(HTMLConstants.CIRCLE);
*  <p>
*  // Create an UnorderedListItem.
*  UnorderedListItem listItem = new UnorderedListItem();
*  <p>
*  // Set the data in the list item.
*  listItem.setItemData(new HTMLText("my list item"));
*  <p>
*  // Add the list item to the UnorderedList.
*  list.addListItem(listItem);
*  System.out.println(list.toString());
*  </PRE></BLOCKQUOTE>
*  <P>
*  Here is the output of the UnorderedList tag:<br>
*  <BLOCKQUOTE><PRE>
*  &lt;ul type=&quot;circle&quot;&gt;
*  &lt;li&gt;my list item&lt;/li&gt;
*  &lt;/ul&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>UnorderedList objects generate the following events:
*  <ul>
*    <li>PropertyChangeEvent
*  </ul>
**/
public class UnorderedList extends HTMLList
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


    private String type_;       //The labeling scheme used to display the list <ul>.


    /**
    *  Constructs a default UnorderedList object.
    **/
    public UnorderedList()
    {
        super();
    }


    /**
    *  Constructs a UnorderedList object with the specified labeling <i>type</i>.
    *  @param type The labeling scheme used to display the list.  One of the following 
    *  constants defined in HTMLConstants:  DISC, SQUARE, or CIRCLE.
    **/
    public UnorderedList(String type)
    {
        super();

        setType(type);
    }


    /**
    *  Returns the attribute tag.
    *  @return The tag.
    **/
    private String getTypeAttributeTag()
    {
        //@C1D

        StringBuffer s = new StringBuffer("");

        if (type_ != null)
        {
            if (type_.equals(HTMLConstants.DISC))
                s.append(" type=\"disc\"");
            else if (type_.equals(HTMLConstants.SQUARE))
                s.append(" type=\"square\"");
            else if (type_.equals(HTMLConstants.CIRCLE))
                s.append(" type=\"circle\"");

            return s.toString();
        }
        else
            return "";

    }


    /**
    *  Returns the type of the labeling scheme.
    *  @return The type.
    **/
    public String getType()
    {
        return type_;
    }


    /**
    *  Returns the tag for the unordered list.
    *  @return The tag.
    **/
    public String getTag()
    {
        //@C1D

        if (getItems().isEmpty())
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before setting items in list.");
            throw new ExtendedIllegalStateException(
                                                   "items", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer s = new StringBuffer("<ul");

        s.append(getTypeAttributeTag());

        if (isCompact())
            s.append(" compact=\"compact\"");

        s.append(getLanguageAttributeTag());                                          //$B1A
        s.append(getDirectionAttributeTag());                                         //$B1A
        s.append(getAttributeString());                                               // @Z1A

        s.append(">\n");
        s.append(getItemAttributeTag());
        s.append("</ul>\n");

        return s.toString();
    }


    /**
    *  Deserializes and initializes transient data.
    **/
    private void readObject(java.io.ObjectInputStream in)          
    throws java.io.IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        changes_ = new PropertyChangeSupport(this);
    }



    /**
     *  Sets the labeling scheme to be used.  The default scheme is <i>disc</i>.
     *
     *  @param type The labeling scheme.  One of the following constants
     *  defined in HTMLConstants:  DISC, SQUARE, or CIRCLE.
     *
     *  @see com.ibm.as400.util.html.HTMLConstants
     **/
    public void setType(String type)
    {
        if (type == null)
            throw new NullPointerException("type");

        // If type is not one of the valid HTMLConstants, throw an exception.
        if ( !(type.equals(HTMLConstants.DISC))  && !(type.equals(HTMLConstants.CIRCLE)) && !(type.equals(HTMLConstants.SQUARE)) )
        {
            throw new ExtendedIllegalArgumentException("type", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "   Setting labeling type for <ul>.");

        String old = type_;

        type_ = type;

        changes_.firePropertyChange("type", old, type );
    }

}
