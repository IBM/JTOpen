///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: OrderedList.java
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
*  The OrderedList class represents an HTML ordered list, &lt;ol&gt;.
*  <P>
*  This example creates a OrderedList tag:
*  <BLOCKQUOTE><PRE>
*  // Create an OrderedList.
*  OrderedList list = new OrderedList();
*  <p>
*  // Use large roman numerals when displaying the list items.
*  list.setType(HTMLConstants.LARGE_ROMAN);
*  <p>
*  // Create an OrderedListItem.
*  OrderedListItem listItem = new OrderedListItem();
*  <p>
*  // Set the data in the list item.
*  listItem.setItemData(new HTMLText("my list item"));
*  <p>
*  // Add the list item to the OrderedList.
*  list.addListItem(listItem);
*  System.out.println(list.toString());
*  </PRE></BLOCKQUOTE>
*  <P>
*  Here is the output of the OrderedList tag:<br>
*  <BLOCKQUOTE><PRE>
*  &lt;ol type=&quot;I&quot;&gt;
*  &lt;li&gt;my list item&lt;/li&gt;
*  &lt;/ol&gt;
*  </PRE></BLOCKQUOTE>
*  <p>Here is the output of the OrderedList tag using XSL Formatting Objects:
*  <PRE><BLOCKQUOTE>
*  &lt;fo:block-container&gt;
*  &lt;fo:list-block&gt;
*  &lt;fo:list-item&gt;
*  &lt;fo:list-item-label&gt;I.&lt;/fo:list-item-label&gt;
*  &lt;fo:list-item-body&gt;&lt;fo:block-container&gt;&lt;fo:block&gt;my list item&lt;/fo:block&gt;
*  &lt;/fo:block-container&gt;
*  &lt;/fo:list-item-body&gt;
*  &lt;/fo:list-item&gt;
*  &lt;/fo:list-block&gt;
*  &lt;/fo:block-container&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>OrderedList objects generate the following events:
*  <ul>
*    <li>PropertyChangeEvent
*  </ul>
**/
public class OrderedList extends HTMLList
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


    private String type_;     //The labeling scheme used to display the list <ol>.

    private int start_ = -1;  //Indicate a number other than "1" to use in incrementing the list structure.



    /**
    *  Constructs a default OrderedList object.
    **/
    public OrderedList()
    {
        super();
    }


    /**
    *  Constructs a OrderedList object with the specified order labeling <i>type</i>.
    *  @param type The order labeling used to display the ordered list.  One of the following 
    *  constants defined in HTMLConstants:  NUMBERS, CAPITALS, LOWER_CASE, LARGE_ROMAN, or SMALL_ROMAN.
    **/
    public OrderedList(String type)
    {
        super();

        setType(type);
    }


    /**
    *  Returns the starting number in the order labeling.
    *  @return The starting number.
    **/
    public int getStartingSequenceNumber()
    {
        return start_;
    }


    /**
    *  Returns the tag for the ordered list.
    *  @return The tag.
    **/
    public String getTag()
    {

        //@C1D

        if(isUseFO())                      //@D1A
            return getFOTag();          //@D1A

        if (getItems().isEmpty())
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before setting items in list.");
            throw new ExtendedIllegalStateException(
                                                   "items", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer s = new StringBuffer("<ol");

        s.append(getTypeAttributeTag());

        if (start_ > 0)
        {
            s.append(" start=\"");
            s.append(Integer.toString(start_));
            s.append("\"");
        }

        if (isCompact())
            s.append(" compact=\"compact\"");

        s.append(getLanguageAttributeTag());                                          //$B1A
        s.append(getDirectionAttributeTag());                                         //$B1A
        s.append(getAttributeString());                                               // @Z1A

        s.append(">\n");
        s.append(getItemAttributeTag());
        s.append("</ol>\n");

        return s.toString();
    }


    /**
    *  Returns the XSL-FO tag for the ordered list.
    *  @return The tag.
    **/
    public String getFOTag()            //@D1A
    {
        //Save current state of useFO_
        boolean useFO = isUseFO();

        setUseFO(true);
        if (getItems().isEmpty())
        {
            Trace.log(Trace.ERROR, "Attempting to get XSL-FO tag before setting items in list.");
            throw new ExtendedIllegalStateException(
                                                   "items", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        if(type_ == null)
           type_ = HTMLConstants.NUMBERS;

        StringBuffer s = new StringBuffer("<fo:block-container");
        s.append(getDirectionAttributeTag());
        s.append(">\n");
        s.append("<fo:list-block>\n");
        s.append(getItemAttributeFOTag(type_));
        s.append("</fo:list-block>\n");
        s.append("</fo:block-container>\n");
        //Set useFO_ to previous state
        setUseFO(useFO);
        
        return s.toString();
    }


    /**
    *  Returns the type of the order labeling.
    *  @return The type.
    **/
    public String getType()
    {
        return type_;
    }


    /**
    *  Returns the type attributes.
    *  @return The type attributes.
    **/
    private String getTypeAttributeTag()
    {
        //@C1D

        StringBuffer s = new StringBuffer("");

        if (type_ != null)
        {
            if (type_.equals(HTMLConstants.NUMBERS))
                s.append(" type=\"1\"");
            else if (type_.equals(HTMLConstants.CAPITALS))
                s.append(" type=\"A\"");
            else if (type_.equals(HTMLConstants.LOWER_CASE))
                s.append(" type=\"a\"");
            else if (type_.equals(HTMLConstants.LARGE_ROMAN))
                s.append(" type=\"I\"");
            else if (type_.equals(HTMLConstants.SMALL_ROMAN))
                s.append(" type=\"i\"");

            return s.toString();
        }
        else
            return "";

    }


    /**
     *  Sets the order labeling to be used.  The default order is by <i>number</i>, (1, 2, 3, etc.).
     *
     *  @param type The order labeling scheme.  One of the following constants
     *  defined in HTMLConstants:  NUMBERS, CAPITALS, LOWER_CASE, LARGE_ROMAN, or SMALL_ROMAN.
     *
     *  @see com.ibm.as400.util.html.HTMLConstants
     **/
    public void setType(String type)
    {
        if (type == null)
            throw new NullPointerException("type");

        // If type is not one of the valid HTMLConstants, throw an exception.
        if ( !(type.equals(HTMLConstants.NUMBERS))  && !(type.equals(HTMLConstants.CAPITALS)) 
             && !(type.equals(HTMLConstants.LOWER_CASE)) && !(type.equals(HTMLConstants.LARGE_ROMAN)) 
             && !(type.equals(HTMLConstants.SMALL_ROMAN)) )
        {
            throw new ExtendedIllegalArgumentException("type", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "   Setting order labeling type for <ol>.");

        String old = type_;

        type_ = type;

        if (changes_ != null) changes_.firePropertyChange("type", old, type ); //@CRS
    }


    /**
    *  Deserializes and initializes transient data.
    **/
    private void readObject(java.io.ObjectInputStream in)          
    throws java.io.IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        //@CRS changes_ = new PropertyChangeSupport(this);
    }


    /**
     *  Sets the starting number to something other than "1" to use in incrementing the list structure. 
     *  Values are automatically converted to the TYPE attribute, if present. The starting number must
     *  be a positive integer.
     *
     *  @param start The ordered list starting number.
     *
     **/
    public void setStartingSequenceNumber(int start)
    {
        if (start < 0)
            throw new ExtendedIllegalArgumentException("start", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "   Setting starting number for <ol>.");

        int old = start_;

        start_ = start;

        if (changes_ != null) changes_.firePropertyChange("start", new Integer(old), new Integer(start) ); //@CRS
    }


}
