///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: OrderedListItem.java
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
*  The OrderedListItem class represents an item in an ordered list item.
*  <P>
*  This example creates a OrderedListItem tag:
*  <BLOCKQUOTE><PRE>
*  // Create an OrderedList.
*  OrderedList list = new OrderedList(HTMLConstants.CAPITALS);
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
*  Here is the output of the OrderedListItem tag:<br>
*  <BLOCKQUOTE><PRE>
*  &lt;ol type=&quot;capitals&quot;&gt;
*  &lt;li&gt;my list item&lt;/li&gt;
*  &lt;/ol&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>OrderedListItem objects generate the following events:
*  <ul>
*    <li>PropertyChangeEvent
*  </ul>
**/
public class OrderedListItem extends HTMLListItem
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    private String type_;      //The labeling scheme used to display the ordered list item <li>

    private int value_ = -1;   //The number other than the incremented value for the current List Item (LI)


    /**
    *  Constructs a default OrderedListItem object.
    **/
    public OrderedListItem()
    {
        super();
    }


    /**
    *  Constructs a OrderedListItem object with the specified list item <i>data</i>.
    *  @param data The data to use in the ordered list item.
    **/
    public OrderedListItem(HTMLTagElement data)
    {
        super();

        setItemData(data);
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
    *  Returns the number for the current list item
    *  @return The number.
    **/
    public int getValue()
    {
        return value_;
    }


    /**
    *  Returns the type and value attributes.
    *  @return The attributes.
    **/
    String getTypeAttribute()
    {
        //@B1D

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
        }

        if (value_ > 0)
        {
            s.append(" value=\"");
            s.append(Integer.toString(value_));
            s.append("\"");
        }

        return s.toString();
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
     *  Sets the order labeling to be used.  The default order is by number, (1, 2, 3, etc.).
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
     *  Sets a number other than the incremented value for the current List Item (LI) in an Ordered List (OL.) It is
     *  thus possible to create a non-sequential list. Values are automatically converted to the TYPE attribute, if present, 
     *  of the parent OL element or current LI element.  The number value must be a positive integer.
     *
     *  @param start The ordered list item value.
     *
     **/
    public void setValue(int value)
    {
        if (value < 0)
            throw new ExtendedIllegalArgumentException("value", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "   Setting current <li> number for <ol>.");

        int old = value_;

        value_ = value;

        if (changes_ != null) changes_.firePropertyChange("value", new Integer(old), new Integer(value) ); //@CRS

    }
}
