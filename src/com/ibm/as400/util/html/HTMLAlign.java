///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HTMLAlign.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.Trace;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.ExtendedIllegalArgumentException;

import java.util.Vector;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;


/**
*  The HTMLAlign class represents a block formatting element, which uses the HTML
*  &lt;DIV&gt; tag, within an HTML page.  The tag has an implied line break before
*  and after the tag.
*  <P>
*  This example creates a HTMLAlign tag:
*  <BLOCKQUOTE><PRE>
*  // Create an ordered list.
*  OrderedList list = new OrderedList(HTMLConstants.LARGE_ROMAN);
*  OrderedListItem listItem = new OrderedListItem();
*  listItem.setItemData(new HTMLText("my list item"));
*  list.addListItem(listItem);
*  
*  // Align the list.
*  HTMLAlign align = new HTMLAlign(list, HTMLConstants.CENTER);
*  System.out.println(align);
*  </PRE></BLOCKQUOTE>
*  <P>
*  Here is the output of the HTMLAlign tag:<br>
*  <BLOCKQUOTE><PRE>
*  &lt;div align=&quot;center&quot;&gt;
*  &lt;ol type=&quot;I&quot;&gt;
*  &lt;li type=&quot;i&quot;&gt;my list item&lt;/li&gt;
*  &lt;/ol&gt;
*  &lt;/div&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>
*  Calling getFOTag() would produce the following:
*  <BLOCKQUOTE><PRE>
*  &lt;fo:block text-align='center'&gt;
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
*  &lt;/fo:block&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>HTMLAlign objects generate the following events:
*  <ul>
*  <LI><A HREF="ElementEvent.html">ElementEvent</A> - The events fired are:
*    <ul>
*      <li>elementAdded
*      <li>elementRemoved
*    </ul>
*    <li>PropertyChangeEvent
*  </ul>
**/
public class HTMLAlign extends HTMLTagAttributes implements java.io.Serializable             // @Z1C
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";
  static final long serialVersionUID = -7673670119386729128L;

    private String align_ = HTMLConstants.LEFT;

    private String lang_;        // The primary language used to display the tags contents.  //$B1A
    private String dir_;         // The direction of the text interpretation.                //$B1A
    private boolean useFO_ = false;  //Indicates if XSL-FO tags are outputted.               //@D1A

    private Vector list_ = new Vector();

    transient private Vector elementListeners;      // The list of element listeners @CRS


    /**
    *  Constructs a default HTMLAlign object.
    **/
    public HTMLAlign()
    {
        super();

    }


    /**
    *  Constructs an HTMLAlign object with the specified <i>data</i>.
    *  The default alignment is left.
    *
    *  @param data The data to align.
    **/
    public HTMLAlign(HTMLTagElement data)
    {
        super();

        addItem(data);

    }


    /**
    *  Constructs an HTMLAlign object with the specified <i>data</i> and <i>align</i> attribute.
    *
    *  @param data The data to align.
    *  @param align The type of alignment.  One of the following constants
    *  defined in HTMLConstants:  LEFT, RIGHT, or CENTER.
    **/
    public HTMLAlign(HTMLTagElement data, String align)
    {
        super();

        addItem(data);
        setAlign(align);
    }


    /**
    *  Adds an HTMLTagElement to the list of tags to align.
    *
    *  @param data The data to align.
    **/
    public void addItem(HTMLTagElement data)
    {
        //@C1D

        if (data == null)
            throw new NullPointerException("data");

        list_.addElement(data);

        fireElementEvent(ElementEvent.ELEMENT_ADDED);
    }


    /**
    *  Adds an ElementListener.
    *
    *  @param listener The ElementListener.
    **/
    public void addItemElementListener(ElementListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");

        if (elementListeners == null) elementListeners = new Vector(); //@CRS
        elementListeners.addElement(listener);
    }



    /**
     *  Fires the element event.
     **/
    private void fireElementEvent(int evt)
    {
      if (elementListeners == null) return;
        Vector targets;
        targets = (Vector) elementListeners.clone();
        ElementEvent elementEvt = new ElementEvent(this, evt);
        for (int i = 0; i < targets.size(); i++)
        {
            ElementListener target = (ElementListener)targets.elementAt(i);
            if (evt == ElementEvent.ELEMENT_ADDED)
                target.elementAdded(elementEvt);
            else if (evt == ElementEvent.ELEMENT_REMOVED)
                target.elementRemoved(elementEvt);
        }
    }


    /**
     *  Returns the alignment of the HTMLAlign object.
     *  @return The alignment.
     **/
    public String getAlign()
    {
        return align_;
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
        

        if(useFO_)                                                                    //@D1A  
        {                                                                             //@D1A
            //If outputting XSL-FO Tags                                               //@D1A
            if((dir_!=null) && (dir_.length()>0))                                     //@D1A
            {                                                                         //@D1A
                if(dir_.equals(HTMLConstants.RTL))                                    //@D1A
                    return " writing-mode='rl'";                                      //@D1A
                else                                                                  //@D1A
                    return " writing-mode='lr'";                                      //@D1A
            }                                                                         //@D1A
            else                                                                      //@D1A
                return "";                                                            //@D1A
        }                                                                             //@D1A
        else                                                                          //@D1A
        {                                                                             //@D1A
            
            //@C1D

            if ((dir_ != null) && (dir_.length() > 0))
                return " dir=\"" + dir_ + "\"";
            else
                return "";        
        }                                                                             //@D1A
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
        //@C1D

        if ((lang_ != null) && (lang_.length() > 0))
            return " lang=\"" + lang_ + "\"";
        else
            return "";
    }


    /**
    *  Returns the tag for the HTML alignment.
    *  @return The tag.
    **/
    public String getTag()
    {
        //@C1D

        if(useFO_)                                          //@D1A
            return getFOTag();                              //@D1A

        if (list_.isEmpty())
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before adding items to list.");
            throw new ExtendedIllegalStateException(
                                                   "data", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer s = new StringBuffer("");

        if (align_ != null)
        {
            if (align_.equals(HTMLConstants.LEFT))
                s.append("<div align=\"left\"");                                         //$B1C
            else if (align_.equals(HTMLConstants.RIGHT))
                s.append("<div align=\"right\"");                                        //$B1C
            else if (align_.equals(HTMLConstants.CENTER))
                s.append("<div align=\"center\"");                                       //$B1C
        }

        s.append(getLanguageAttributeTag());                                          //$B1A
        s.append(getDirectionAttributeTag());                                         //$B1A

        s.append(getAttributeString());                                               // @Z1A

        s.append(">\n");                                                              //$B1C

        for (int i=0; i < list_.size(); i++)
        {
            HTMLTagElement data = (HTMLTagElement)list_.elementAt(i);
            s.append(data.getTag());
        }

        s.append("\n</div>\n");

        return s.toString();
    }


    /**
    *  Returns the tag for the XSL-FO alignment.
    *  The language attribute is not supported in XSL-FO.
    *  @return The tag.
    **/
    public String getFOTag()                                //@D1A
    {
        //Save current useFO_ value
        boolean useFO = useFO_;

        //Indicate that XSL-FO tags are outputted.
        setUseFO(true);

        if (list_.isEmpty())
        {
            Trace.log(Trace.ERROR, "Attempting to get XSL-FO tag before adding items to list.");
            throw new ExtendedIllegalStateException(
                                                   "data", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer s = new StringBuffer("");

        if (align_ != null)                                             
        {                                                               
            if(align_.equals(HTMLConstants.LEFT))                       
                s.append("<fo:block text-align='start'");               
            else if(align_.equals(HTMLConstants.RIGHT))                 
                s.append("<fo:block text-align='end'");                 
            else if(align_.equals(HTMLConstants.CENTER))                
                s.append("<fo:block text-align='center'");              
        }                                                               

        s.append(getDirectionAttributeTag());                           
        s.append(">\n");                                                

        for (int i = 0; i<list_.size(); i++)                            
        {                                                               
            HTMLTagElement data = (HTMLTagElement)list_.elementAt(i);   
            s.append(data.getFOTag());                                  
            s.append("\n");                                             
        }                                                               
        
        s.append("</fo:block>\n");                                      

        //Set useFO_ to previous state.
        setUseFO(useFO);    

        return s.toString();
    }

    /**
     *  Removes this ElementListener.
     *
     *  @param listener The ElementListener.
     **/
    public void removeItemElementListener(ElementListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");

        if (elementListeners != null) elementListeners.removeElement(listener); //@CRS
    }


    /**
     *  Returns if Formatting Object tags are outputted.
     *  The default value is false.
     *  @return true if the output generated is an XSL formatting object, false if the output generated is HTML.
     **/
    public boolean isUseFO()                                          //@D1A
    {
        return useFO_;
    }

    /**
    *  Deserializes and initializes transient data.
    **/
    private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        //@CRS changes_ = new PropertyChangeSupport(this);
        //@CRS elementListeners = new Vector();
    }


    /**
    *  Removes an HTMLTagElement from the list of tags to align.
    *
    *  @param data The data to remove.
    **/
    public void removeItem(HTMLTagElement data)
    {
        //@C1D

        if (data == null)
            throw new NullPointerException("data");

        if (list_.removeElement(data))
            fireElementEvent(ElementEvent.ELEMENT_REMOVED);
    }



    /**
     *  Sets the horizontal alignment for a block of HTML.  The default is left alignment.
     *  @param align The alignment.  One of the following constants
     *  defined in HTMLConstants:  LEFT, RIGHT, or CENTER.
     *
     *  @see com.ibm.as400.util.html.HTMLConstants
     **/
    public void setAlign(String align)
    {
        if (align == null)
            throw new NullPointerException("align");

        // If align is not one of the valid HTMLConstants, throw an exception.
        if ( !(align.equals(HTMLConstants.LEFT))  && !(align.equals(HTMLConstants.RIGHT)) && !(align.equals(HTMLConstants.CENTER)) )
        {
            throw new ExtendedIllegalArgumentException("align", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "   Setting alignment for <div>.");

        String old = align_;

        align_ = align;

        if (changes_ != null) changes_.firePropertyChange("align", old, align ); //@CRS

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
        {
            throw new ExtendedIllegalArgumentException("dir", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        String old = dir_;

        dir_ = dir;

        if (changes_ != null) changes_.firePropertyChange("dir", old, dir ); //@CRS
    }


    /**
    *  Sets the <i>language</i> of the HTMLAlign tag.
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

        if (changes_ != null) changes_.firePropertyChange("lang", old, lang ); //@CRS
    }


    /** 
    * Sets if Formatting Object tags should be used.  
    *  The default value is false.
    * @param useFO - true if output generated is an XSL formatting object, false if the output generated is HTML.
    **/     
    public void setUseFO(boolean useFO)                            //@D1A
    {
        boolean old = useFO_;

        useFO_ = useFO;

        if (changes_ != null) changes_.firePropertyChange("useFO", old, useFO );
    }


    /**
    *  Returns a String representation for the HTMLAlign tag.
    *  @return The tag.
    **/
    public String toString()
    {
        return getTag();
    }
}
