///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HTMLTreeElement.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import java.util.Vector;
import java.util.Properties;
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
*  <a href="/servlet/myServlet?a=s&hc=2043557#2043557" name="2043557">-</a>
*  </td>
*  <td>
*  <a href="http://myWebPage">My Web Page</a></td>
*  </tr>
*  <tr><td>&nbsp;</td><td>
*  <table cellpadding="0" cellspacing="3">
*  <tr>
*  <td>
*  <a href="/servlet/myServlet?a=s&hc=2043712#2043712" name="2043712">-</a>
*  </td>
*  <td>
*  <a href="http://myWebServer/anotherWebPage">Another Web Page</a></td>
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
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   private Vector branches_ = new Vector();

   private boolean        expanded_    = false;
   private HTMLHyperlink  textUrl_     = null;
   private HTMLHyperlink  iconUrl_     = null;
   private HTMLTagElement elementData_ = null;
   private boolean        sort_        = true;      // @A1A

   private static String expandedGif_  = null;
   private static String collapsedGif_ = null;
   private static String docGif_       = null;

   transient PropertyChangeSupport changes_ = new PropertyChangeSupport(this);
   transient private Vector elementListeners = new Vector();      // The list of element listeners


   /**
    *  Constructs a default HTMLTreeElement object.
    **/
   public HTMLTreeElement()
   {
      branches_ = new Vector();
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

      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "Adding element to the HTMLTreeElement.");

      branches_.addElement(element);

      fireElementEvent(ElementEvent.ELEMENT_ADDED);
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
      elementListeners.addElement(listener);
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
      changes_.addPropertyChangeListener(listener);
   }


   /**
    *  Fires the element event.
    **/
   private void fireElementEvent(int evt) {
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
      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "Generating HTMLTreeElement tag...");

      if (iconUrl_ != null)
         setIconUrl(iconUrl_);
      else
         throw new ExtendedIllegalStateException("iconUrl", ExtendedIllegalStateException.PROPERTY_NOT_SET);

      if (elementData_ == null)
         throw new ExtendedIllegalStateException("text", ExtendedIllegalStateException.PROPERTY_NOT_SET );

      String std = new String("<td>\n");     // The start table definition tag.
      String etd = new String("</td>\n");    // The end table definition tag.

      StringBuffer buf = new StringBuffer();

      buf.append("<tr>\n");

      if (isLeaf())
      {
         if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "   Element is a leaf.");

         buf.append(std);

         if (docGif_ != null)
            buf.append("<img src=\"" + docGif_ + "\" border=\"0\" width=\"9\" height=\"14\" vspace=\"3\" alt=\"Work with document\">\n");
         else
            buf.append(">");

         buf.append(etd);

         buf.append(std);
         buf.append(elementData_.getTag() + "\n");
         buf.append(etd);

         buf.append("</tr>\n");
      }
      else
      {
         if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "   Element is NOT a leaf.");

         String hcStr = com.ibm.as400.util.html.URLEncoder.encode(Integer.toString(this.hashCode()));

         buf.append(std);

         String iconTag;
         if (isExpanded())
         {
            if (Trace.isTraceOn())
               Trace.log(Trace.INFORMATION, "   Element is expanded.");

            if (expandedGif_ != null)
               iconTag = "<img src=\"" + expandedGif_ + "\" border=\"0\" width=\"9\" height=\"14\" vspace=\"3\" alt=\"Compress\">";
            else
               iconTag = "-";
         }
         else
         {
            if (Trace.isTraceOn())
               Trace.log(Trace.INFORMATION, "   Element is collapsed.");

            if (collapsedGif_ != null)
               iconTag = "<img src=\"" + collapsedGif_ + "\" border=\"0\" width=\"9\" height=\"14\" vspace=\"3\" alt=\"Expand\">";
            else
               iconTag = "+";
         }


         if (iconUrl_ != null)
         {
            try
            {
               iconUrl_.setName(hcStr);

               Properties iconProp = iconUrl_.getProperties();

               if (iconProp == null)
                  iconProp = new Properties();

               iconProp.put("hc", hcStr);

               if (expanded_)
                  iconProp.put("a", "s");
               else
                  iconProp.put("a", "e");

               iconUrl_.setProperties(iconProp);
               URLParser parser = new URLParser(iconUrl_.getLink());
               iconUrl_.setLink(parser.getURI());                          //$A3C
               iconUrl_.setText(iconTag);
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
            buf.append(elementData_.getTag() + "\n");
         }

         buf.append(std);

         buf.append("</tr>\n");

         if (isExpanded())
         {
            buf.append("<tr><td>&nbsp;</td><td>\n");

            buf.append("<table cellpadding=\"0\" cellspacing=\"3\">\n");
            
            if (sort_)                                     // @A1A
               branches_ = HTMLTree.sort(branches_);       // @A1A

            for (int i=0; i<branches_.size(); i++)
            {
               HTMLTreeElement node = (HTMLTreeElement)branches_.elementAt(i);

               buf.append(node.getTag());
            }

            buf.append("</table>\n");
            buf.append("</tr>\n");
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
      in.defaultReadObject();

      changes_ = new PropertyChangeSupport(this);
      elementListeners = new Vector();
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

      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "Removing element from the HTMLTreeElement.");

      if (branches_.removeElement(element))
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
      elementListeners.removeElement(listener);
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
      changes_.removePropertyChangeListener(listener);
   }


   /**
    *  Indicates which HTMLTreeElement is selected.  The <i>hashcode</i> is used
    *  to determine which element within the tree to expand or collapse.
    *
    *  @param hashcode The hashcode.
    **/
   public void selected(int hashcode)
   {
      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "   HTMLTreeElement has been selected.");

      if (!isLeaf())
      {
         if (this.hashCode() == hashcode)
         {
            boolean old = expanded_;

            expanded_ = !expanded_;

            changes_.firePropertyChange("selected", new Boolean(old), new Boolean(expanded_));
         }
         else
         {
            for (int i=0; i<branches_.size(); i++)
            {
               HTMLTreeElement node = (HTMLTreeElement)branches_.elementAt(i);
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

      changes_.firePropertyChange("expanded", new Boolean(old), new Boolean(expanded_));
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

      changes_.firePropertyChange("url", old, iconUrl);

      for (int i=0; i<branches_.size(); i++)
      {
         HTMLHyperlink l = null;
         HTMLTreeElement node = (HTMLTreeElement)branches_.elementAt(i);
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

      changes_.firePropertyChange("element", old, elementData_ );
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

      changes_.firePropertyChange("text", old, elementData_ );
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

      changes_.firePropertyChange("url", old, textUrl );

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
