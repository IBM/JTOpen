///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: HTMLTreeElement.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import java.util.Vector;
import java.util.Properties;
import java.text.Collator;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import com.ibm.as400.access.Trace;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.util.servlet.ServletHyperlink;

/**
*  The HTMLTreeElement class represents an hierarchial element within an HTMLTree or other
*  HTMLTreeElements.
*
*  <P>This example creates an HTMLTreeElement object.
*
*  <P>
*  <BLOCKQUOTE><PRE>
*  // Create parent HTMLTreeElement.
*  HTMLTreeElement parentElement = new HTMLTreeElement();
*  parentElement.setTextUrl(new HTMLHyperlink("http://myWebPage", "My Web Page"));
*  <P>
*  // Create HTMLTreeElement Child.
*  HTMLTreeElement childElement = new HTMLTreeElement();
*  childElement.setTextUrl(new HTMLHyperlink("http://anotherWebPage", "Another Web Page"));
*  parentElement.addElement(childElement);
*  <P>
*
*  </PRE></BLOCKQUOTE>
*
*  Once the elements are added to an HTMLTree object and the elements are expanded, the
*  HTMLTreeElements will look like this:
*  <P>
*
*  <table cellpadding="0" cellspacing="3">
*  <tr>
*  <td>
*  <a href="/servlet/myServlet?action=contract&hashcode=2043557#2043557" name="2043557">-</a>
*  </td>
*  <td>
*  <a href="http://myWebPage">My Web Page</a>
*  </td>
*  </tr>
*  <tr><td>&nbsp;</td><td>
*  <table cellpadding="0" cellspacing="3">
*  <tr>
*  <td>
*  <a href="/servlet/myServlet?action=contract&hashcode=2043712#2043712" name="2043712">-</a>
*  </td>
*  <td>
*  <a href="http://myWebServer/anotherWebPage">Another Web Page</a>
*  </td>
*  </td>
*  </tr>
*  </table>
*  </tr>
*  </table>
*
*  <P>
*  HTMLTreeElement objects generate the following events:
*  <ul>
*    <li><A HREF="ElementEvent.html">ElementEvent</A> - The events fired are:
*    <ul>
*       <li>elementAdded
*       <li>elementRemoved
*    </ul>
*    <li>PropertyChangeEvent
*  </ul>
*
*  @see com.ibm.as400.util.html.HTMLTree
*  @see com.ibm.as400.util.html.URLParser
**/
public class HTMLTreeElement implements HTMLTagElement, java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";

    private HTMLVector branches_; //@P2C

    private boolean        expanded_    = false;
    private HTMLHyperlink  textUrl_     = null;
    private HTMLHyperlink  iconUrl_     = null;
    private HTMLTagElement elementData_ = null;
    private boolean        sort_        = true;      // @A1A
    transient private Collator collator_;

    private static String expandedGif_  = null;
    private static String collapsedGif_ = null;
    private static String docGif_       = null;

    private static final String std = new String("<td>\n");     // The start table definition tag.              // @B2C
    private static final String etd = new String("</td>\n");    // The end table definition tag.              // @B2C

    transient PropertyChangeSupport changes_; //@P2C
    transient private Vector elementListeners_; // The list of element listeners @P2C


    /**
     *  Constructs a default HTMLTreeElement object.
     **/
    public HTMLTreeElement()
    {
        // @B2A
        // If the locale is Korean, then this throws
        // an ArrayIndexOutOfBoundsException.  This is
        // a bug in the JDK.  The workarond in that case
        // is just to use String.compareTo().
        try                                                                            // @B2A
        {
            collator_ = Collator.getInstance ();                           // @B2A
            collator_.setStrength (Collator.PRIMARY);                // @B2A
        }
        catch (Exception e)                                                    // @B2A
        {
            collator_ = null;                                                      // @B2A
        }

        branches_ = new HTMLVector(); //@P2C
    }


    /**
     *  Constructs an HTMLTreeElement with the specified <i>text</i>.
     *
     *  @param text The text.
     **/
    public HTMLTreeElement(String text)
    {
        this();
        setText(text);
    }


    /**
     *  Constructs an HTMLTreeElement with the specified <i>text</i>.
     *
     *  @param text The text.
     **/
    public HTMLTreeElement(HTMLTagElement text)
    {
        this();
        setText(text);
    }


    /**
     *  Constructs an HTMLTreeElement with the specified <i>textUrl</i>.
     *
     *  @param textUrl The HTMLHyperlink.
     **/
    public HTMLTreeElement(HTMLHyperlink textUrl)
    {
        this();
        setTextUrl(textUrl);
    }


    /**
     *  Adds a child element to the parent HTMLTreeElement
     *
     *  @param element The HTMLTreeElement.
     **/
    public void addElement(HTMLTreeElement element)
    {
        if (element == null)
            throw new NullPointerException("element");

        branches_.addElement(element);

        if (elementListeners_ != null) fireElementEvent(ElementEvent.ELEMENT_ADDED); //@P2C
    }


    /**
      * Adds an addElementListener.
      * The specified addElementListeners <b>elementAdded</b> method will
      * be called each time a HTMLTreeElement is added.
      * The addElementListener object is added to a list of addElementListeners
      * managed by this HTMLTreeElement. It can be removed with removeElementListener.
      *
      * @see #removeElementListener
      *
      * @param listener The ElementListener.
     **/
    public void addElementListener(ElementListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");
        if (elementListeners_ == null) elementListeners_ = new Vector(); //@P2A
        elementListeners_.addElement(listener);
    }


    /**
     *  Adds a PropertyChangeListener.  The specified
     *  PropertyChangeListener's <b>propertyChange</b>
     *  method is called each time the value of any
     *  bound property is changed.
     *
     *  @see #removePropertyChangeListener
     *  @param listener The PropertyChangeListener.
    **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        if (changes_ == null) changes_ = new PropertyChangeSupport(this); //@P2A
        changes_.addPropertyChangeListener(listener);
    }


    /**
     *  Fires the element event.
     **/
    private void fireElementEvent(int evt)
    {
        Vector targets;
        targets = (Vector) elementListeners_.clone();
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
     *  Returns the collapsed gif.
     *
     *  @return The collapsed gif.
     **/
    public String getCollapsedGif()
    {
        return collapsedGif_;
    }


    /**
     *  Returns the document gif.
     *
     *  @return The document gif.
     **/
    public String getDocGif()
    {
        return docGif_;
    }


    /**
     *  Returns the expanded gif.
     *
     *  @return The expanded gif.
     **/
    public String getExpandedGif()
    {
        return expandedGif_;
    }


    /**
     *  Returns the icon URL.
     *
     *  @return The icon URL.
     **/
    public HTMLHyperlink getIconUrl()
    {
        return iconUrl_;
    }


    /**
     *  Returns the visible text of the HTMLTreeElement.
     *
     *  @return text The text.
     **/
    public HTMLTagElement getText()
    {
        return elementData_;
    }


    /**
     *  Returns the HTMLTreeElement tag.
     *
     *  @return The tag.
     **/
    public String getTag()
    {
        if (iconUrl_ != null)
            setIconUrl(iconUrl_);
        else
            throw new ExtendedIllegalStateException("iconUrl", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        if (elementData_ == null)
            throw new ExtendedIllegalStateException("text", ExtendedIllegalStateException.PROPERTY_NOT_SET );

        StringBuffer buf = new StringBuffer();              // @B2C

        buf.append("<tr>\n");

        if (isLeaf())
        {
            if (Trace.isTraceOn())
                Trace.log(Trace.INFORMATION, "   Element is a leaf.");

            buf.append(std);

            if (docGif_ != null)
            {
                buf.append("<img src=\"");                                                                                                                      // @B2C
                buf.append(docGif_);                                                                                                                               // @B2C
                buf.append("\" border=\"0\" width=\"9\" height=\"14\" vspace=\"3\" alt=\"");                                            //@B3C
                buf.append(ResourceBundleLoader_h.getText("PROP_NAME_WORK"));                                                       // @B3C
                buf.append("\" />\n");                                                                                                                             // @B3C
            }
            else
                buf.append(">");

            buf.append(etd);

            buf.append(std);
            buf.append(elementData_.getTag());             // @B2C
            buf.append("\n");                                       // @B2C
            buf.append(etd);

            buf.append("</tr>\n");
        }
        else
        {
            String hcStr = com.ibm.as400.util.html.URLEncoder.encode(Integer.toString(this.hashCode()));

            buf.append(std);

            StringBuffer iconTag = new StringBuffer();

            if (isExpanded())
            {
                if (expandedGif_ != null)
                {                                                                                                                                                       // @B2C
                    iconTag.append("<img src=\"");                                                                                                       // @B2C
                    iconTag.append(expandedGif_);                                                                                                       // @B2C
                    iconTag.append("\" border=\"0\" width=\"9\" height=\"14\" vspace=\"3\" alt=\"");                            // @B3C
                    iconTag.append(ResourceBundleLoader_h.getText("PROP_NAME_COMPRESS"));                                 // @B3C
                    iconTag.append("\" />");                                                                                                                 // @B3C
                }
                else                                                                                                                                                    // @B2C
                    iconTag.append("-");
            }
            else
            {
                if (collapsedGif_ != null)
                {
                    iconTag.append( "<img src=\"");                                                                                                       // @B2C
                    iconTag.append(collapsedGif_);                                                                                                         // @B2C
                    iconTag.append("\" border=\"0\" width=\"9\" height=\"14\" vspace=\"3\" alt=\"");                              // @B3C
                    iconTag.append(ResourceBundleLoader_h.getText("PROP_NAME_EXPAND"));                                     // @B3C
                    iconTag.append("\" />");                                                                                                                  // @B3C
                }
                else
                    iconTag.append("+");
            }


            if (iconUrl_ != null)
            {
                try
                {
                    iconUrl_.setName(hcStr);

                    Properties iconProp = iconUrl_.getProperties();

                    if (iconProp == null)
                        iconProp = new Properties();

                    iconProp.put("hashcode", hcStr);                           // @B1C

                    if (expanded_)
                        iconProp.put("action", "contract");                     // @B1C
                    else
                        iconProp.put("action", "expand");                       // @B1C

                    iconUrl_.setProperties(iconProp);
                    URLParser parser = new URLParser(iconUrl_.getLink());
                    iconUrl_.setLink(parser.getURI());                          //$A3C
                    iconUrl_.setText(iconTag.toString());
                    iconUrl_.setLocation(hcStr);                                //$A3A

                }
                catch (PropertyVetoException e)
                { /* Ignore */
                }

                buf.append(iconUrl_.getTag());
            }
            else
            {
                buf.append(iconTag);
            }


            buf.append("\n");

            buf.append(etd);

            buf.append(std);


            // If the text URL has been set
            if (textUrl_ != null)
            {
                try
                {
                    if (textUrl_.getText() == null)
                        textUrl_.setText(elementData_.getTag() + "\n");
                }
                catch (PropertyVetoException e)
                { /* Ignore */
                }

                buf.append(textUrl_.getTag());
            }
            else                              // If the text URL has NOT been set.
            {
                buf.append(elementData_.getTag());        // @B2C
                buf.append("\n");                                  // @B2C
            }

            buf.append(etd);    // @B2C

            buf.append("</tr>\n");

            if (isExpanded())
            {
                buf.append("<tr><td>&nbsp;</td><td>\n<table cellpadding=\"0\" cellspacing=\"3\">\n"); //@P2C

                if (sort_) HTMLTree.sort(collator_, branches_);       // @A1A  // @B2C @P2C

                int size = branches_.getCount(); //@P2A
                Object[] data = branches_.getData(); //@P2A
                for (int i=0; i<size; i++) //@P2C
                {
                    HTMLTreeElement node = (HTMLTreeElement)data[i]; //@P2C

                    buf.append(node.getTag());
                }

                buf.append("</table>\n</tr>\n"); //@P2C
                //@P2D buf.append("</tr>\n");
            }
        }

        return buf.toString();
    }


    /**
     *  Returns the text URL.
     *
     *  @return The text URL.
     **/
    public HTMLHyperlink getTextUrl()
    {
        return textUrl_;
    }


    /**
     *  Indicates if the HTMLTreeElement is expanded.
     *
     *  @return true if expanded, false otherwise.
     **/
    public boolean isExpanded()
    {
        return expanded_;
    }


    /**
     *  Indicates if the HTMLTreeElement is a leaf.
     *
     *  @return true if the element is a leaf, false otherwise.
     **/
    public boolean isLeaf()
    {
        return branches_.isEmpty();
    }


    /**
     *  Deserializes and initializes transient data.
     **/
    private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException
    {
        // @B2A
        // If the locale is Korean, then this throws
        // an ArrayIndexOutOfBoundsException.  This is
        // a bug in the JDK.  The workarond in that case
        // is just to use String.compareTo().
        try                                                                            // @B2A
        {
            collator_ = Collator.getInstance ();                           // @B2A
            collator_.setStrength (Collator.PRIMARY);                // @B2A
        }
        catch (Exception e)                                                    // @B2A
        {
            collator_ = null;                                                      // @B2A
        }

        in.defaultReadObject();

        //@P2D changes_ = new PropertyChangeSupport(this);
        //@P2D elementListeners_ = new Vector();
    }


    /**
     *  Removes a child element from the parent HTMLTreeElement
     *
     *  @param element The HTMLTreeElement.
     **/
    public void removeElement(HTMLTreeElement element)
    {
        if (element == null)
            throw new NullPointerException("element");

        if (branches_.removeElement(element) && elementListeners_ != null) //@P2C
            fireElementEvent(ElementEvent.ELEMENT_REMOVED);
    }


    /**
      * Removes this ElementListener from the internal list.
      * If the ElementListener is not on the list, nothing is done.
      *
      * @see #addElementListener
      *
      * @param listener The ElementListener.
     **/
    public void removeElementListener(ElementListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");
        if (elementListeners_ != null) elementListeners_.removeElement(listener); //@P2C
    }


    /**
     *  Removes the PropertyChangeListener from the internal list.
     *  If the PropertyChangeListener is not on the list, nothing is done.
     *
     *  @see #addPropertyChangeListener
     *  @param listener The PropertyChangeListener.
    **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        if (changes_ != null) changes_.removePropertyChangeListener(listener); //@P2C
    }


    /**
     *  Indicates which HTMLTreeElement is selected.  The <i>hashcode</i> is used
     *  to determine which element within the tree to expand or collapse.
     *
     *  @param hashcode The hashcode.
     **/
    public void selected(int hashcode)
    {
        if (!isLeaf())
        {
            if (this.hashCode() == hashcode)
            {
                boolean old = expanded_;

                expanded_ = !expanded_;

                if (changes_ != null) changes_.firePropertyChange("selected", new Boolean(old), new Boolean(expanded_)); //@P2C
            }
            else
            {
                int size = branches_.getCount(); //@P2A
                Object[] data = branches_.getData(); //@P2A
                for (int i=0; i<size; i++) //@P2C
                {
                    HTMLTreeElement node = (HTMLTreeElement)data[i]; //@P2C
                    node.selected(hashcode);
                }
            }
        }
    }


    /**
     *  Set the gif to use when the HTMLTree is collapsed.
     *  The gif can be specified with an absolute or
     *  relative URL location.
     *
     *  @param gifUrl The collapsed gif location.
     **/
    public static void setCollapsedGif(String gifUrl)
    {
        if (gifUrl == null)
            throw new NullPointerException("gifUrl");

        collapsedGif_ = gifUrl;
    }


    /**
     *  Set the gif to use when the element in the tree
     *  contains documents or files and not directories.
     *  The gif can be specified with an absolute or
     *  relative URL location.
     *
     *  @param gifUrl The document gif location.
     **/
    public static void setDocGif(String gifUrl)
    {
        if (gifUrl == null)
            throw new NullPointerException("gifUrl");

        docGif_ = gifUrl;
    }


    /**
     *  Set the gif to use when the HTMLTree is expanded.
     *  The gif can be specified with an absolute or
     *  relative URL location.
     *
     *  @param gifUrl The expanded gif location.
     **/
    public static void setExpandedGif(String gifUrl)
    {
        if (gifUrl == null)
            throw new NullPointerException("gifUrl");

        expandedGif_ = gifUrl;
    }


    /**
     *  Set the HTMLTreeElement to be expanded.  The default is false.
     *
     *  @param expanded true if element is expanded; false if collapsed.
     **/
    public void setExpanded(boolean expanded)
    {
        boolean old = expanded_;

        expanded_ = expanded;

        if (changes_ != null) changes_.firePropertyChange("expanded", new Boolean(old), new Boolean(expanded_)); //@P2C
    }


    /**
     *  Set the URL for the expanded/collapsed icon and all the corresponding icons for the
     *  elements under this HTMLTreeElement.
     *
     *  @param iconUrl The icon url.
     **/
    public void setIconUrl(HTMLHyperlink iconUrl)
    {
        if (iconUrl == null)
            throw new NullPointerException("iconUrl");

        HTMLHyperlink old = iconUrl_;

        iconUrl_ = iconUrl;

        if (changes_ != null) changes_.firePropertyChange("url", old, iconUrl); //@P2C

        int size = branches_.getCount(); //@P2A
        Object[] data = branches_.getData(); //@P2A
        for (int i=0; i<size; i++) //@P2C
        {
            HTMLHyperlink l = null;
            HTMLTreeElement node = (HTMLTreeElement)data[i]; //@P2C
            if (iconUrl instanceof ServletHyperlink)
                l = (ServletHyperlink)iconUrl.clone();
            else
                l = (HTMLHyperlink)iconUrl.clone();

            node.setIconUrl(l);
        }
    }


    /**
     *  Set the visible text of the HTMLTreeElement.
     *
     *  @param element The HTMLTagElement.
     **/
    public void setText(HTMLTagElement element)
    {
        if (element == null)
            throw new NullPointerException("element");

        HTMLTagElement old = elementData_;

        elementData_ = element;

        if (changes_ != null) changes_.firePropertyChange("element", old, elementData_ ); //@P2C
    }


    /**
     *  Set the visigble test of the HTMLTreeElement.
     *
     *  @param test The text.
     **/
    public void setText(String text)
    {
        if (text == null)
            throw new NullPointerException("text");

        HTMLTagElement old = elementData_;

        elementData_ = new HTMLText(text);

        if (changes_ != null) changes_.firePropertyChange("text", old, elementData_ ); //@P2C
    }


    /**
     *  Set the URL of the HTMLTreeElement text.  Setting the textUrl will
     *  replace the viewable text property set with setText().
     *
     *  @param textUrl The HTMLHyperlink.
     **/
    public void setTextUrl(HTMLHyperlink textUrl)            // @A2C
    {
        if (textUrl == null)
            throw new NullPointerException("textUrl");

        HTMLHyperlink old = textUrl_;

        textUrl_ = textUrl;

        if (changes_ != null) changes_.firePropertyChange("url", old, textUrl ); //@P2C

        setText(textUrl);
    }


    /**
      *  Sorts the tree elements.
      *
      *  @param sort true if the elements are sorted; false otherwise.
      *              The default is true.
      **/
    public void sort(boolean sort)                          // @A1A
    {
        sort_ = sort;
    }
}
