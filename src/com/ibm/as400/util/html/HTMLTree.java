///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HTMLTree.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import java.util.Vector;
import java.text.Collator;                        // @A1A
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

import javax.servlet.http.HttpServletRequest;

import com.ibm.as400.access.Trace;
import com.ibm.as400.access.ExtendedIllegalStateException;


/**
*  The HTMLTree class represents an HTML hierarchical tree of HTML elements.
*
*  <P>
*  This example creates an HTMLTree object with five elements.
*  The first three elements will be added directly to the HTMLTree and the remaining two
*  elements will extend off one of the first three elements.
*
*  <P>
*
*  <BLOCKQUOTE><PRE>
*  // Create a URLParser object.
*  URLParser urlParser = new URLParser(myHttpServletRequest.getRequestURI());
*  <P>
*  // Create parent HTMLTreeElements.
*  HTMLTreeElement parentElement = new HTMLTreeElement();
*  parentElement.setTextUrl(new HTMLHyperlink("http://myWebPage", "My Web Page"));
*  <P>
*  HTMLTreeElement parentElement2 = new HTMLTreeElement();
*  parentElement2.setText(new HTMLText("Parent Element 2"));
*  <P>
*  HTMLTreeElement parentElement3 = new HTMLTreeElement();
*  parentElement3.setText(new HTMLText("Parent Element 3"));
*  <P>
*  // Create children HTMLTreeElements.
*  HTMLTreeElement childElement1 = new HTMLTreeElement();
*  childElement1.setTextUrl(new HTMLHyperlink("http://anotherWebPage", "Another Web Page"));
*  parentElement.addElement(childElement1);
*  <P>
*  // Create a child of the first Child Element.
*  HTMLTreeElement subChildElement1 = new HTMLTreeElement();
*  subChildElement1.setText(new HTMLHyperlink("http://myHomePage", "Home"));
*  <P>
*  // Add the sub-child to the parent child element.
*  childElement1.addElement(subChildElement1);
*  <P>
*  // Set the URL link for the Expand/Collapsed Icon.
*  ServletHyperlink iconUrl = new ServletHyperlink(urlParser.getURI());
*  iconUrl.setHttpServletResponse(resp);
*  parentElement.setIconUrl(iconUrl);
*  parentElement2.setIconUrl(iconUrl);
*  parentElement3.setIconUrl(iconUrl);
*  <P>
*  // Add the parent elements to the tree.
*  tree.addElement(parentElement);
*  tree.addElement(parentElement2);
*  tree.addElement(parentElement3);
*  </PRE></BLOCKQUOTE>
*
*  <P>
*  When the HTMLTree is first viewed in a browser, none of the elements in the tree will be
*  expanded, so the tree will look like this:
*  <P>
*
*  <table cellpadding="0" cellspacing="3">
*  <tr>
*  <td>
*  <a href="/servlet/myServlet#2043557?a=e&hc=2043557" name="2043557">+</a>
*  </td>
*  <td>
*  <a href="http://myWebPage">My Web Page</a></td>
*  </tr>
*  <tr>
*  <td>
*  </td>
*  <td>
*  Parent Element 2
*  </td>
*  </tr>
*  <tr>
*  <td>
*  </td>
*  <td>
*  Parent Element 3
*  </td>
*  </tr>
*  </table>
*
*  <P>
*  When the elements in the HTMLTree are expanded by traversing the hierarchy, the tree will look like:
*  <P>
*
*  <table cellpadding="0" cellspacing="3">
*  <tr>
*  <td>
*  <a href="/servlet/myServlet#2043557?a=s&hc=2043557" name="2043557">-</a>
*  </td>
*  <td>
*  <a href="http://myWebPage">My Web Page</a></td>
*  </tr>
*  <tr><td>&nbsp;</td><td>
*  <table cellpadding="0" cellspacing="3">
*  <tr>
*  <td>
*  <a href="/servlet/myServlet#2043712?a=s&hc=2043712" name="2043712">-</a>
*  </td>
*  <td>
*  <a href="http://myWebServer/anotherWebPage">Another Web Page</a></td>
*  </tr>
*  <tr><td>&nbsp;</td><td>
*  <table cellpadding="0" cellspacing="3">
*  <tr>
*  <td>
*  </td>
*  <td>
*  <a href="http://myWebserver/myHomePage">Home</a>
*  </td>
*  </tr>
*  </table>
*  </tr>
*  </table>
*  </tr>
*  <tr>
*  <td>
*  </td>
*  <td>
*  Parent Element 2
*  </td>
*  </tr>
*  <tr>
*  <td>
*  </td>
*  <td>
*  Parent Element 3
*  </td>
*  </tr>
*  </table>
*
*  <P>
*  HTMLTree objects generate the following events:
*  <ul>
*    <li><A HREF="ElementEvent.html">ElementEvent</A> - The events fired are:
*    <ul>
*       <li>elementAdded
*       <li>elementRemoved
*    </ul>
*    <li>PropertyChangeEvent
*  </ul>
*
*  @see com.ibm.as400.util.html.HTMLTreeElement
*  @see com.ibm.as400.util.html.URLParser
**/
public class HTMLTree implements HTMLTagElement, java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   private Vector branches_;
   private HttpServletRequest request_;
   private boolean sort_ = true;            // @A1A
   private static  Collator  collator_;     // @A1A

   transient private PropertyChangeSupport changes_ = new PropertyChangeSupport(this);
   transient private Vector elementListeners = new Vector();      // The list of element listeners


   /**
    *  Constructs a default HTMLTree object.
    **/
   public HTMLTree()
   {
      super();

      branches_ = new Vector();
   }


   /**
    *  Constructs an HTMLTree object with the specified HttpServletRequest.
    *  The request is the mechanism used to provide continuity while expanding
    *  and collapsing the tree.
    **/
   public HTMLTree(HttpServletRequest request)
   {
      this();
      setHttpServletRequest(request);
   }


   /**
    *  Adds an HTMLTreeElement to the tree.
    *
    *  @param element The HTMLTreeElement.
    **/
   public void addElement(HTMLTreeElement element)
   {
      if (element == null)
         throw new NullPointerException("element");

      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "Adding HTMLTreeElement to the tree.");

      branches_.addElement(element);

      fireElementEvent(ElementEvent.ELEMENT_ADDED);
   }


   /**
     * Adds an addElementListener.
     * The specified addElementListeners <b>elementAdded</b> method will
     * be called each time a RadioFormInput is added to the group.
     * The addElementListener object is added to a list of addElementListeners
     * managed by this RadioFormInputGroup. It can be removed with removeElementListener.
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
    *  Removes an HTMLTreeElement from the tree.
    *
    *  @param element The HTMLTreeElement.
    **/
   public void removeElement(HTMLTreeElement element)
   {
      if (element == null)
         throw new NullPointerException("element");

      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "Removing HTMLTreeElement from the tree.");

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
   *  Fires the element event.
   **/
   private void fireElementEvent(int evt)
   {
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
    *  Returns the HttpServletRequest.
    *
    *  @return The request.
    **/
   public HttpServletRequest getHttpServletRequest()
   {
      return request_;
   }


   /**
    *  Returns the HTMLTree tag.
    *
    *  @return The tag.
    **/
   public String getTag()
   {
      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "Generating HTMLTree tag...");

      if (request_ == null)
         throw new ExtendedIllegalStateException("request", ExtendedIllegalStateException.PROPERTY_NOT_SET );

      // Get the hashcode parameter from the HTTP request.
      String hcStr = request_.getParameter("hc");

      StringBuffer buf1 = new StringBuffer("<table cellpadding=\"0\" cellspacing=\"3\">\n");

      if (sort_)                          // @A1A
         branches_ = sort(branches_);     // @A1A

      for (int i=0; i<branches_.size(); i++)
      {
         HTMLTreeElement node = (HTMLTreeElement)branches_.elementAt(i);

         // expand/contract tree
         if (hcStr != null)
         {
            int hc = Integer.parseInt(hcStr);
            node.selected(hc);
         }

         buf1.append(node.getTag());
      }

      buf1.append("</table>\n");

      return buf1.toString();
   }


   /**
    *  Sets the Http servlet <i>request</i>.  The request is the mechanism
    *  used to provide continuity while expanding and collapsing the tree.
    *
    *  @param request The Http servlet request.
    **/
   public void setHttpServletRequest(HttpServletRequest request)
   {
      if (request == null)
         throw new NullPointerException("request");

      HttpServletRequest old = request_;

      request_ = request;

      changes_.firePropertyChange("request", old, request_);
   }


   /**
     *  Sorts the elements within the HTMLTree.
     *
     *  @param sort true if the elements are sorted; false otherwise.
     *              The default is true.
     **/
    public void sort(boolean sort)                                                  // @A1A
    {
       sort_ = sort;
    }


    /**
     *  Sorts a vector of objects.
     *
     *  @param  objects The objects.
     *
     *  @return The sorted Vector.
     **/
    static Vector sort (Vector list)                                                // @A1A
    {   
        Object[] objectArray = new Object[list.size()];
        list.copyInto(objectArray);

        sort2(objectArray);

        list.removeAllElements();

        for (int i = 0; i < objectArray.length; ++i)
            list.addElement(objectArray[i]);

        return list;
    }

     
    /**
    *  Sorts an array of objects.
    *
    *  @param  objects The objects.
    **/
    static void sort2 (Object[] objects)                                            // @A1A
    {   
        // This uses a quick-sort.
        Object temp;
        int length = objects.length;
        for (int i = 0; i < length; ++i) 
        {
            for (int j = i + 1; j < length; ++j) 
            {
                if (sortCompare (objects[i], objects[j])) 
                {
                    temp = objects[i];
                    objects[i] = objects[j];
                    objects[j] = temp;
                }
            }
        }
    }


    /** 
     *  Compares two objects for the sort.
     *
     *  @param  objectI             The ith object.
     *  @param  objectJ             The jth object.
     *  @return                     true if the ith object is before the
     *                              jth object, false otherwise.
     **/
    private static boolean sortCompare (Object objectI, Object objectJ)             // @A1A
    {
        Object valueI;
        Object valueJ;

        // When the object is a HTMLTreeElement or FileTreeElement the
        // sort must be done against the name or viewable text
        // of the element otherwise nothing will be sorted properly.
        if (objectI instanceof HTMLTreeElement)
        {
           valueI = ((HTMLTreeElement)objectI).getText();
           valueJ = ((HTMLTreeElement)objectJ).getText();
        }
        else if (objectI instanceof FileTreeElement)
        {
           valueI = ((FileTreeElement)objectI).getFile().getName();
           valueJ = ((FileTreeElement)objectJ).getFile().getName();
        }
        else
        {
           valueI = objectI.toString();
           valueJ = objectJ.toString();
        }

        // If the locale is Korean, then this throws
        // an ArrayIndexOutOfBoundsException.  This is
        // a bug in the JDK.  The workarond in that case
        // is just to use String.compareTo().
        try 
        {
           collator_ = Collator.getInstance ();
           collator_.setStrength (Collator.PRIMARY);
        }
        catch (Exception e) 
        {
           collator_ = null;
        }
        
        // Check for nulls.
        if (valueI == null)
           valueI = "";
        if (valueJ == null)
           valueJ = "";
        
        boolean comparison = false;
        
        // If they are equal, then use the next column.
        if (valueI.toString().equals(valueJ.toString()))     
        { /* do nothing */ }
        else if (collator_ != null)   // Otherwise, do the comparison using this column.
           comparison = (collator_.compare (valueI.toString(), valueJ.toString()) < 0);
        else
           comparison = (valueI.toString().compareTo(valueJ.toString()) < 0);

        // Return the value.
        return (comparison != true);
    }

}
