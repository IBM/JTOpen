///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: UnorderedListItem.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.Trace;
import com.ibm.as400.access.ExtendedIllegalArgumentException;

import java.beans.PropertyChangeSupport;


/**
*  The UnorderedListItem class represents an item in an unordered list item.
*  <P>
*  This example creates a UnorderedListItem tag:
*  <BLOCKQUOTE><PRE>
*  // Create an UnorderedList.
*  UnorderedList list = new UnorderedList(HTMLConstants.SQUARE);
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
*  Here is the output of the UnorderedListItem tag:<br>
*  <BLOCKQUOTE><PRE>
*  &lt;ul type=&quot;square&quot;&gt;
*  &lt;li&gt;my list item&lt;/li&gt;
*  &lt;/ul&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>UnorderedListItem objects generate the following events:
*  <ul>
*    <li>PropertyChangeEvent
*  </ul>
**/
public class UnorderedListItem extends HTMLListItem
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private String type_;           //The labeling scheme used to display the unordered list item <li>.


    /**
    *  Constructs a default UnorderedList object.
    **/
    public UnorderedListItem()
    {
       super();
    }


    /**
    *  Constructs a UnorderedListItem object with the specified <i>data</i>.
    *  @param data The data to use in the unordered list item.
    **/
    public UnorderedListItem(HTMLTagElement data)
    {
        super();

        setItemData(data);
    }  
    

    /**
    *  Returns the type attribute.
    *  @return The type attribute.
    **/
    String getTypeAttribute()
    {
       if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "   Retrieving type attribute tag.");

       StringBuffer s = new StringBuffer("");

       if (type_ != null)
       {                                    
          if(type_.equals(HTMLConstants.DISC))
             s.append(" type=\"disc\"");
          else if(type_.equals(HTMLConstants.SQUARE))
             s.append(" type=\"square\"");
          else if(type_.equals(HTMLConstants.CIRCLE))
             s.append(" type=\"circle\"");
          return new String(s);
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
    *  Deserializes and initializes transient data.
    **/
    private void readObject(java.io.ObjectInputStream in)          
        throws java.io.IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        changes_ = new PropertyChangeSupport(this);
    }
    


    /**
     *  Sets the labeling scheme to be used.  The default scheme is <i>disc</>.
     *  When used at the <i>ListItem</i> level, all subsequent list labels will 
     *  carry the new TYPE scheme unless set again by a later TYPE attribute. 
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
         Trace.log(Trace.INFORMATION, "   Setting labeling type for <li>.");

        String old = type_;
        
        type_ = type;

        changes_.firePropertyChange("type", old, type );
    }
}
