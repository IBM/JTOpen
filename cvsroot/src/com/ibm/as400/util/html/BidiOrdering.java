///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: BidiOrdering.java
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
import com.ibm.as400.access.ExtendedIllegalStateException;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

import java.util.Vector;

/**
*  The BidiOrdering class represents an HTML tag used to alter the language
*  and direction of text.
*  <p>
*  Here is an example of a BidiOrdering tag:<br>
*  <BLOCKQUOTE><PRE>
*  &lt;bdo lang=&quot;he&quot; dir=&quot;rtl&quot;&gt;
*  Some Hebrew Text.
*  &lt;/bdo&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>
*  The equivalent tag using XSL Formatting Objects looks like this:
*  <BLOCKQUOTE><PRE>
*  &lt;fo:block-container writing-mode='rl'&gt;
*  &lt;fo:block&gt;Some Hebrew Text&lt;/fo:block&gt;
*  &lt;/fo:block-container&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>BidiOrdering objects generate the following events:
*  <ul>
*  <LI><A HREF="ElementEvent.html">ElementEvent</A> - The events fired are:
*    <ul>
*      <li>elementAdded
*      <li>elementRemoved
*    </ul>
*  <li>PropertyChangeEvent
*  </ul>
**/
public class BidiOrdering extends HTMLTagAttributes implements java.io.Serializable            // @Z1C
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";
  static final long serialVersionUID = 367063750196907253L;

    private String lang_;        // The primary language used to display the tags contents.
    private String dir_;         // The direction of the text interpretation.
    private boolean useFO_ = false; //Indicates if XSL-FO tags are outputted.                   //@D1A

    private Vector list_ = new Vector();

    transient private Vector elementListeners;      // The list of element listeners @CRS


    /**
    *  Constructs a default BidiOrdering object.
    **/
    public BidiOrdering()
    {
        super();
    }

    /**
    *  Constructs a BidiOrdering object with the specified <i>dir</i>.
    *  @param dir The direction of the text being displayed.
    **/
    public BidiOrdering(String dir)
    {
        this();

        setDirection(dir);
    }

    /**
    *  Constructs a BidiOrdering object with the specified <i>lang</i>
    *  and <i>dir</i>.
    *  @param lang The language of the text.
    *  @param dir The direction of text being displayed.
    **/
    public BidiOrdering(String lang, String dir)
    {
        this(dir);

        setLanguage(lang);
    }


    /**
    *  Adds an HTMLTagElement to the list elements.
    *
    *  @param data The data to re-order.
    **/
    public void addItem(HTMLTagElement data)
    {
        if (data == null)
            throw new NullPointerException("data");

        list_.addElement(data);

        fireElementEvent(ElementEvent.ELEMENT_ADDED);
    }


    /**
    *  Adds a String to the list of elements.
    *
    *  @param data The data to re-order.
    **/
    public void addItem(String data)
    {
        if (data == null)
            throw new NullPointerException("data");

        list_.addElement(new HTMLText(data));

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
     *  This method is used to remove all elements contained
     *  within the BidiOrdering list.
     **/
    void clear()
    {
        list_.removeAllElements();
    }


    /**
    *  Fires the element event.
    **/
    private void fireElementEvent(int evt)
    {
      if (elementListeners == null) return; //@CRS
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
    *  Returns the <i>direction</i> of the text interpretation.
    *  @return The direction of the text.
    **/
    public String getDirection()
    {
        return dir_;
    }


    /**
    *  Returns the <i>language</i> of the element.
    *  @return The language of the element.
    **/
    public String getLanguage()
    {
        return lang_;
    }


    /**
    *  Returns the list of items.
    *  @return The list.
    **/
    public Vector getItems()
    {
        return list_;
    }


    /**
    *  Returns the tag for the Bi-Directional Ordering.
    *  @return The tag.
    **/
    public String getTag()
    {
        if(useFO_)                                        //@D1A
            return getFOTag();                            //@D1A

        // the direction is the only required tag
        if (dir_ == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before setting direction.");
            throw new ExtendedIllegalStateException(
                                                   "dir", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        if (list_.isEmpty())
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before adding items to list.");
            throw new ExtendedIllegalStateException(
                                                   "data", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer s = new StringBuffer("\n<bdo");

        if (lang_ != null)
            s.append(" lang=\"" + lang_ + "\"");

        s.append(" dir=\"" + dir_ + "\"");
        s.append(getAttributeString());           // @Z1A
        s.append(">\n");

        for (int i=0; i < list_.size(); i++)
        {
            HTMLTagElement data = (HTMLTagElement)list_.elementAt(i);
            s.append(data.getTag());
        }

        s.append("\n</bdo>\n");

        return s.toString();
    }

    /**
    *  Returns the XSL formatting object's tag for the Bi-Directional Ordering.
    *  The language attribute is not supported in XSL-FO.
    *  @return The tag.
    **/
    public String getFOTag()                                    //@D1A
    {
        //Output an XSL-FO Tag

        // the direction is the only required tag
        if (dir_ == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get XSL-FO tag before setting direction.");
            throw new ExtendedIllegalStateException(
                                                   "dir", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        if (list_.isEmpty())
        {
            Trace.log(Trace.ERROR, "Attempting to get XSL-FO tag before adding items to list.");
            throw new ExtendedIllegalStateException(
                                                   "data", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer s = new StringBuffer("\n<fo:block-container");

        //Get direction of text
        s.append(" writing-mode='");
        if(dir_.equals(HTMLConstants.RTL))
                s.append("rl'");
        else
                s.append("lr'");
        
        s.append(">\n");

        for (int i=0; i < list_.size(); i++)
        {
            HTMLTagElement data = (HTMLTagElement)list_.elementAt(i);
            s.append(data.getFOTag());
        }

        s.append("</fo:block-container>\n");
        
        return s.toString();
    }

    /**
     *  Returns if Formatting Object tags are outputted.
     *  The default value is false.
     *  @return true if the output generated is an XSL formatting object, false if the output generated is HTML.
     **/
    public boolean isUseFO()                                            //@D1A
    {
        return useFO_;
    }

    /**
    *  Removess an HTMLTagElement from the list.
    *
    *  @param data The data to remove.
    **/
    public void removeItem(HTMLTagElement data)
    {
        if (data == null)
            throw new NullPointerException("data");

        if (list_.removeElement(data))
            fireElementEvent(ElementEvent.ELEMENT_REMOVED);
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
    *  Sets the <i>direction</i> of the text interpretation.
    *  @param dir The direction.  One of the following constants
    *  defined in HTMLConstants:  LTR or RTL.
    *
    *  @see com.ibm.as400.util.html.HTMLConstants
    *
    **/
    public void setDirection(String dir)
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
    *  Sets the <i>language</i> of the input tag.
    *  @param lang The language.  Example language tags include:
    *  en and en-US.
    *
    **/
    public void setLanguage(String lang)
    {
        if (lang == null)
            throw new NullPointerException("lang");

        String old = lang_;

        lang_ = lang;

        if (changes_ != null) changes_.firePropertyChange("lang", old, lang ); //@CRS
    }

    /** 
    *  Sets if Formatting Object tags should be outputted.  
    *  The default value is false.
    *  @param useFO - true if output generated is an XSL formatting object, false if the output generated is HTML.
    **/ 
    public void setUseFO(boolean useFO)                                    //@D1A
    {
        boolean old = useFO_;

        useFO_ = useFO;

        if (changes_ != null) changes_.firePropertyChange("useFO", old, useFO );
    }

    /**
    *  Returns a String representation for the BidiOrdering tag.
    *  @return The tag.
    **/
    public String toString()
    {
        return getTag();
    }

}

