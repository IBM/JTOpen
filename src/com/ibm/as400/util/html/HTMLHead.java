///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: HTMLHead.java
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

import java.util.Vector;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;


/**
*  The HTMLHead class represents an HTML head tag, which contains information about the HTML document.
*  <P>
*  This example creates an HTMLHead tag:
*  <BLOCKQUOTE><PRE>
*  // Create an HTMLHead with a title.
*  HTMLHead head = new HTMLHead("My HTML Document");
*  <p>
*  // Create an HTMLMeta.
*  HTMLMeta meta = new HTMLMeta("expires", "Mon, 01 Jun 2000 12:00:00 CST");
*  HTMLMeta meta2 = new HTMLMeta("refresh", "5", "http://www.sample.com/next.html");
*  <p>
*  // Add the meta information to the HTMLHead
*  head.addMetaInformation(meta);
*  head.addMetaInformation(meta2);
*  </PRE></BLOCKQUOTE>
*  <P>
*  Here is the output of the HTMLHead tag:<br>
*  <BLOCKQUOTE><PRE>
*  &lt;head&gt;
*  &lt;meta http-equiv=&quot;expired&quot; content=&quot;Mon, 06 Jun 2000 12:00:00 CST&quot; /&gt;
*  &lt;meta http-equiv=&quot;refresh&quot; content=&quot;5; URL=http://www.sample.com/next.html&quot /&gt;
*  &lt;title&gt;My HTML Document&lt;/title&gt;
*  &lt;/head&gt;
*  </PRE></BLOCKQUOTE>
*
*  Using XSL Formatting Objects, the head tag represents a page header.
*  <p>
*  Calling getFOTag() would produce the following.  
*  <PRE><BLOCKQUOTE>
*  &lt;fo:static-content flow-name='xsl-region-before'&gt;
*  &lt;fo:block-container&gt;
*  My HTML Document&lt;/fo:block-container&gt;
*  &lt;/fo:static-content&gt;
*  </PRE></BLOCKQUTOE>
*
*
*  <p>HTMLHead objects generate the following events:
*  <ul>
*  <LI><A HREF="ElementEvent.html">ElementEvent</A> - The events fired are:
*    <ul>
*      <li>elementAdded
*      <li>elementRemoved
*    </ul>
*    <li>PropertyChangeEvent
*  </ul>
**/
public class HTMLHead extends HTMLTagAttributes implements java.io.Serializable     // @Z1C
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";
  static final long serialVersionUID = 5006426803056962528L;

    private String title_;       // The title to use for HTML document.
    private String lang_;        // The primary language used to display the tags contents.
    private String dir_;         // The direction of the text interpretation.
    private boolean useFO_ = false;      // Indicates if XSL-FO tags are outputted.   //@C1A

    private Vector list_ = new Vector();

    transient private Vector elementListeners;      // The list of element listeners @CRS


    /**
    *  Constructs a default HTMLHead object.
    **/
    public HTMLHead()
    {
        super();
    }


    /**
    *  Constructs an HTMLHead object with the specified <i>title</i>.
    *
    *  @param title The title of the HTML document.
    **/
    public HTMLHead(String title)
    {
        super();

        setTitle(title);
    }


    /**
    *  Constructs an HTMLHead object with the specified <i>title</i> and <i>meta</i> information.
    *
    *  @param title The title of the HTML document.
    *  @param meta The HTML meta information.
    **/
    public HTMLHead(String title, HTMLMeta meta)
    {
        super();

        addMetaInformation(meta);
        setTitle(title);
    }


    /**
    *  Adds HTMLMeta information to the HTMLHead.
    *  The &lt;head&gt; tag contains a list of &lt;meta&gt; tags.
    *
    *  @param meta The meta information to add.
    **/
    public void addMetaInformation(HTMLMeta meta)
    {
        //@B1D

        if (meta == null)
            throw new NullPointerException("meta");

        list_.addElement(meta);

        fireElementEvent(ElementEvent.ELEMENT_ADDED);
    }


    /**
    *  Adds an ElementListener.
    *
    *  @param listener The ElementListener.
    **/
    public void addMetaInformationElementListener(ElementListener listener)
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
    *  Returns the direction attribute tag.
    *  @return The direction tag.
    **/
    String getDirectionAttributeTag()
    {
        if(useFO_)                                                  //@C1A
        {                                                           //@C1A
            if((dir_!=null) && (dir_.length()>0))                   //@C1A
            {                                                       //@C1A
                if(dir_.equals(HTMLConstants.RTL))                  //@C1A
                    return " writing-mode='rl'";                    //@C1A
                else                                                //@C1A
                    return " writing-mode='lr'";                    //@C1A
            }                                                       //@C1A
            else                                                    //@C1A
                return "";                                          //@C1A
        }                                                           //@C1A
        else                                                        //@C1A
        {                                                           //@C1A
            //@B1D

            if ((dir_ != null) && (dir_.length() > 0))
                return " dir=\"" + dir_ + "\"";
            else
                return "";
        }                                                           //@C1A
    }


    /**
    *  Returns the <i>language</i> of the head element.
    *  @return The language of the head element.
    **/
    public String getLanguage()
    {
        return lang_;
    }


    /**
    *  Returns the language attribute tag.
    *  @return The language tag.
    **/
    String getLanguageAttributeTag()
    {
        //@B1D

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
        //@B1D
        if(useFO_)                                      //@C1A
            return getFOTag();                          //@C1A

        if (title_ == null && list_.isEmpty())
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before setting the title or adding meta information to the head.");
            throw new ExtendedIllegalStateException(
                                                   "title/meta", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer s = new StringBuffer("");

        s.append("<head");
        s.append(getLanguageAttributeTag());
        s.append(getDirectionAttributeTag());
        s.append(getAttributeString());               // @Z1A
        s.append(">\n");

        for (int i=0; i < list_.size(); i++)
        {
            HTMLTagElement data = (HTMLTagElement)list_.elementAt(i);
            s.append(data.getTag());
        }

        if (title_ != null)
        {
            s.append("<title" );
            s.append(getLanguageAttributeTag());
            s.append(getDirectionAttributeTag());
            s.append(">");
            s.append(title_);
            s.append("</title>\n");
        }

        s.append("</head>\n");

        return s.toString();
    }


    /**
    *  Returns the tag for the XSL-FO header.
    *  The language attribute and Meta tags are not supported in XSL-FO.
    *  @return The tag.
    **/
    public String getFOTag()                                              //@C1A
    {
        //Save current state of useFO_
        boolean useFO = useFO_;

        //Indicate that Formatting Object tag is outputted
        setUseFO(true);

        if (title_ == null && list_.isEmpty())
        {
            Trace.log(Trace.ERROR, "Attempting to get XSL-FO tag before setting the title for the head.");
            throw new ExtendedIllegalStateException("title", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer s = new StringBuffer("<fo:static-content flow-name='xsl-region-before'>\n");
        s.append("<fo:block-container");
        s.append(getDirectionAttributeTag());
        s.append(">\n");

        //Add the title
        if(title_ != null)
        {
            s.append(title_);
        }
        
        s.append("</fo:block-container>\n");
        s.append("</fo:static-content>\n");

        //Set useFO_ to previous state.
        setUseFO(useFO);
        
        return s.toString();

    }


    /**
     *  Returns the title of the HTMLHead object.
     *  @return The title.
     **/
    public String getTitle()
    {
        return title_;
    }


    /**
    *  Returns if Formatting Object tags are outputted.
    *  The default value is false.
    *  @return true if the output generated is an XSL formatting object, false if the output generated is HTML.
    **/
    public boolean isUseFO()                                        //@C1A
    {
        return useFO_;
    }


    /**
     *  Removes this ElementListener.
     *
     *  @param listener The ElementListener.
     **/
    public void removeMetaInformationElementListener(ElementListener listener)
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
    *  Removes an HTMLMeta from the HTMLHead.
    *
    *  @param meta The meta information to remove.
    **/
    public void removeMetaInformation(HTMLMeta meta)
    {
        //@B1D

        if (meta == null)
            throw new NullPointerException("meta");

        if (list_.removeElement(meta))
            fireElementEvent(ElementEvent.ELEMENT_REMOVED);
    }



    /**
    *  Sets the <i>direction</i> of the text interpretation.  Setting the direction
    *  will add the dir attribute to both the <i>head</i> tag and the
    *  <i>title</i> tag if one is being used.
    *
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
    *  Sets the <i>language</i> of the HTMLHead tag.  Setting the language
    *  will add the lang attribute to both the <i>head</i> tag and the
    *  <i>title</i> tag if one is being used.
    *
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
    *  Sets the title of the HTML document.
    *
    *  @param title The title of the HTML document.
    **/
    public void setTitle(String title)
    {
        if (title == null)
            throw new NullPointerException("title");

        if (title.length() == 0)
            throw new ExtendedIllegalArgumentException("title", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "   Setting title for <head>.");

        String old = title_;

        title_ = title;

        if (changes_ != null) changes_.firePropertyChange("title", old, title ); //@CRS

    }


    /** 
    * Sets if Formatting Object tags should be used. 
    * The default value is false.
    * @param useFO - true if output generated is an XSL formatting object, false if the output generated is HTML. 
    **/    
    public void setUseFO(boolean useFO)                                //@C1A
    {
        boolean old = useFO_;

        useFO_ = useFO;

        if (changes_ != null) changes_.firePropertyChange("useFO", old, useFO );
    }


    /**
    *  Returns a String representation for the HTMLHead tag.
    *  @return The tag.
    **/
    public String toString()
    {
        return getTag();
    }
}

