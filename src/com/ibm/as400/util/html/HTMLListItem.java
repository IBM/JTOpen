///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HTMLListItem.java
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
import java.beans.PropertyChangeListener;


/**
*  The HTMLListItem class represents items within a HTMLList.  The items within the list 
*  can either be ordered or unordered.
*    
*  <p>HTMLListItem objects generate the following events:
*  <ul>
*  <li>PropertyChangeEvent
*  </ul>
*  <P>
**/
abstract public class HTMLListItem extends HTMLTagAttributes implements java.io.Serializable // @Z1C
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    
    private HTMLTagElement listData_;     // Data being added to the list item

    private String lang_;        // The primary language used to display the tags contents.  //$B1A
    private String dir_;         // The direction of the text interpretation.                //$B1A

    

    /**
    *  Returns the type attribute.
    *  @return The type attribute.
    **/
    abstract String getTypeAttribute();


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
    *  Returns the <i>direction</i> of the text interpretation.
    *  @return The direction of the text.
    **/
    public String getDirection()                               //$B1A
    {
        return dir_;
    }


    /**
    *  Returns the direction attribute tag.
    *  @return The direction tag.
    **/
    String getDirectionAttributeTag()                                                 //$B1A
    {
       if (Trace.isTraceOn())
          Trace.log(Trace.INFORMATION, "   Retrieving direction attribute tag.");

       if ((dir_ != null) && (dir_.length() > 0))
       {
          StringBuffer buffer = new StringBuffer(" dir=\"");
          buffer.append(dir_);
          buffer.append("\"");

          return buffer.toString();
       }
       else
          return "";
    }


    /**
     *  Returns the data in the HTMLListItem.
     *  @return The item data.
     **/
    public HTMLTagElement getItemData()
    {
        return listData_;
    }


    /**
    *  Returns the <i>language</i> of the input element.
    *  @return The language of the input element.
    **/
    public String getLanguage()                                //$B1A
    {
        return lang_;
    }


    /**
    *  Returns the language attribute tag.                                            
    *  @return The language tag.                                                      
    **/                                                                               
    String getLanguageAttributeTag()                                                  //$B1A
    {
       if (Trace.isTraceOn())
          Trace.log(Trace.INFORMATION, "   Retrieving language attribute tag.");

       if ((lang_ != null) && (lang_.length() > 0))
       {
          StringBuffer buffer = new StringBuffer(" lang=\"");
          buffer.append(lang_);
          buffer.append("\"");

          return buffer.toString();
       }
       else
          return "";
    }


    /**
    *  Returns the tag for the HTML list item.
    *  @return The tag.
    **/
    public String getTag()
    {
       if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "Generating HTMLListItem tag....");

       StringBuffer s = new StringBuffer("<li");
       
       s.append(getTypeAttribute());
       s.append(getLanguageAttributeTag());                                          //$B1A
       s.append(getDirectionAttributeTag());                                         //$B1A
       s.append(getAttributeString());                                               // @Z1A
       s.append(">");
       s.append(listData_.getTag());
       s.append("</li>\n");

       return s.toString();
    }


    
    /**
    *  Sets the <i>direction</i> of the text interpretation.
    *  @param dir The direction.  One of the following constants
    *  defined in HTMLConstants:  LTR or RTL.
    *
    *  @see com.ibm.as400.util.html.HTMLConstants
    *
    **/
    public void setDirection(String dir)                                     //$B1A
    {   
        if (dir == null)
           throw new NullPointerException("dir");

        // If direction is not one of the valid HTMLConstants, throw an exception.
        if ( !(dir.equals(HTMLConstants.LTR))  && !(dir.equals(HTMLConstants.RTL)) ) 
           throw new ExtendedIllegalArgumentException("dir", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        String old = dir_;

        dir_ = dir;

        changes_.firePropertyChange("dir", old, dir );
    }

    
    /**
     *  Sets the item data in the HTMLListItem.
     *
     *  @param data The item data.
     *
     **/
    public void setItemData(HTMLTagElement data)
    {
        if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "   Setting HTMLListItem data.");

        if (data == null)
         throw new NullPointerException("data");
        
        HTMLTagElement old = listData_;
        
        listData_ = data;

        changes_.firePropertyChange("data", old, data );
    }


    /**
    *  Sets the <i>language</i> of the input tag.
    *  @param lang The language.  Example language tags include:
    *  en and en-US.
    *
    **/
    public void setLanguage(String lang)                                      //$B1A
    {   
        if (lang == null)
           throw new NullPointerException("lang");

        String old = lang_;

        lang_ = lang;

        changes_.firePropertyChange("lang", old, lang );
    }


    /**
    *  Returns a String representation for the HTMLList tag.
    *  @return The tag.
    **/
    public String toString()
    {
        return getTag();
    }
}
