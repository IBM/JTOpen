///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HTMLApplet.java
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
*  The HTMLApplet class represents a java application embeded within an HTML page.
*  <P>
*  This example creates a HTMLApplet tag:
*  <BLOCKQUOTE><PRE>
*  // Create an HTMLApplet.
*  HTMLApplet applet = new HTMLApplet("myApplet", "http://myCompany.com/dir/", 100, 100);
*  <p>
*  // Create a parameter, then add it to the applet.
*  HTMLParameter param = new HTMLParameter("parm1", "value1");
*  applet.addParameter(param);
*  <p>
*  // Create and add second parameter
*  HTMLParameter param2 = applet.addParameter("parm2", "value2");
*  System.out.println(applet);
*  </PRE></BLOCKQUOTE>
*  <P>
*  Here is the output of the HTMLApplet tag:<br>
*  <BLOCKQUOTE><PRE>
*  &lt;applet name=&quot;myApplet&quot; codebase=&quot;http://server:port/dir&quot width=&quot;100&quot; height=&quot;100&quot;;&gt;
*  &lt;param name=&quot;parm1&quot; value=&quot;value1&quot;&gt;
*  &lt;param name=&quot;parm2&quot; value=&quot;value2&quot;&gt;
*  If you see this text, the browser does not support the APPLET tag or the applet has failed to load.
*  &lt;/applet&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>HTMLApplet objects generate the following events:
*  <ul>
*  <li><A HREF="ElementEvent.html">ElementEvent</A> - The events fired are:
*    <ul>
*    <li>elementAdded
*    <li>elementRemoved
*    </ul>
*  <li>PropertyChangeEvent
*  </ul>

**/
public class HTMLApplet extends HTMLTagAttributes implements java.io.Serializable       // @Z1C
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  private String archive_;
  private String code_;
  private String codebase_;
  private int    width_;
  private int    height_;

  // The alternate text will get displayed if the Applet does not appear in the browser.
  private String text_ = loader_.getText("PROP_DESC_APPLET_ALTTEXT");

  private Vector list_;                 // The list of Applet parameters.

  // Handles loading the appropriate resource bundle
  private static ResourceBundleLoader_h loader_;


  transient private Vector elementListeners = new Vector();       // The list of element listeners



  /**
   *  Constructs a default HTMLApplet object.
   **/
  public HTMLApplet()
  {
    super();
    list_ = new Vector();
  }


  /**
   *  Constructs an HTMLApplet object with the specified applet <i>code</i>.
   *
   *  @param code The applet name.
   **/
  public HTMLApplet(String code)
  {
    super();

    setCode(code);

    list_ = new Vector();
  }


  /**
   *  Constructs an HTMLApplet object with the specified Applet <i>code</i>, <i>width</i>, and <i>height</i>.
   *
   *  @param code The applet name.
   *  @param width The applet width.
   *  @param height The applet height.
   **/
  public HTMLApplet(String code, int width, int height)
  {
    super();

    setCode(code);
    setWidth(width);
    setHeight(height);

    list_ = new Vector();
  }


  /**
   *  Constructs an HTMLApplet object with the specified Applet <i>code</i>, <i>codebase</i>, <i>width</i>, <i>height</i>.
   *
   *  @param code The applet name.
   *  @param codebase  The base URL.
   *  @param width The applet width.
   *  @param height The applet height.
   **/
  public HTMLApplet(String code, String codebase, int width, int height)
  {
    super();

    setCode(code);
    setCodebase(codebase);
    setWidth(width);
    setHeight(height);

    list_ = new Vector();
  }


  /**
   *  Constructs an HTMLApplet object with the specified Applet <i>code</i>, <i>codebase</i>, <i>width</i>, and <i>height</i>.
   *
   *  @param code The applet name.
   *  @param codebase  The base URL.
   *  @param width The applet width.
   *  @param height The applet height.
   **/
  public HTMLApplet(String archive, String code, String codebase, int width, int height)
  {
    super();

    setArchive(archive);
    setCode(code);
    setCodebase(codebase);
    setWidth(width);
    setHeight(height);

    list_ = new Vector();
  }


  /**
   *  Adds a parameter to the applet tag.
   *
   *  @param param The parameter.
   **/
  public void addParameter(HTMLParameter param)
  {
    //@B1D

    if (param == null)
      throw new NullPointerException("param");

    // add parameter to the list
    list_.addElement(param);

    fireElementEvent(ElementEvent.ELEMENT_ADDED);
  }


  /**
   *  Adds a HTMLParameter to the applet.
   *
   *  @param name The parameter name.
   *  @param value The parameter value.
   *
   *  @return An HTMLParameter object.
   **/
  public HTMLParameter addParameter(String name, String value)
  {
    //@B1D

    if (name == null)
      throw new NullPointerException("name");
    if (value == null)
      throw new NullPointerException("value");

    //Create the HTMLParameter from the values passed in
    HTMLParameter param = new HTMLParameter(name,value);

    // Add the HTMLParameter to the group.
    list_.addElement(param);

    fireElementEvent(ElementEvent.ELEMENT_ADDED);

    return param;
  }


  /**
   * Adds an addElementListener.
   * The specified addElementListeners <b>elementAdded</b> method will
   * be called each time a HTMLParameter is added to the group.
   * The addElementListener object is added to a list of addElementListeners
   * managed by this HTMLApplet. It can be removed with removeElementListener.
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
   *  Returns the name of the archive(s).
   *  @return The archive(s).
   **/
  public String getArchive()
  {
    return archive_;
  }


  /**
   *  Returns the class name of the applet class.
   *  @return The applet name.
   **/
  public String getCode()
  {
    return code_;
  }


  /**
   *  Returns the base URL of the applet.
   *  @return The base URL.
   **/
  public String getCodebase()
  {
    return codebase_;
  }


  /**
   *  Returns the height of the applet in pixels.
   *  @return The height.
   **/
  public int getHeight()
  {
    return height_;
  }


  /**
   *  Returns the alternate text of the applet.
   *  @return The text.
   **/
  public String getText()
  {
    return text_;
  }


  /**
   *  Returns the width of the applet in pixels.
   *  @return The width.
   **/
  public int getWidth()
  {
    return width_;
  }


  /**
   *  Returns the tag for the HTML applet.
   *  @return The tag.
   **/
  public String getTag()
  {
    //@B1D

    if (code_ == null)
    {
      Trace.log(Trace.ERROR, "Attempting to get tag before setting applet code.");
      throw new ExtendedIllegalStateException("code", ExtendedIllegalStateException.PROPERTY_NOT_SET );
    }

    if (width_ <= 0)
    {
      Trace.log(Trace.ERROR, "Attempting to get tag before setting applet width.");
      throw new ExtendedIllegalStateException("width", ExtendedIllegalStateException.PROPERTY_NOT_SET );
    }

    if (height_ <= 0)
    {
      Trace.log(Trace.ERROR, "Attempting to get tag before setting applet height.");
      throw new ExtendedIllegalStateException("height", ExtendedIllegalStateException.PROPERTY_NOT_SET );
    }

    StringBuffer s = new StringBuffer("<applet");

    if (codebase_ != null)
      s.append(" codebase=\"" + getCodebase() + "\"");

    s.append(" code=\"" + getCode() + "\"");
    s.append(" width=\"" + getWidth() + "\"");
    s.append(" height=\"" + getHeight() + "\"");

    if (archive_ != null)
      s.append(" archive=\"" + getArchive() + "\"");

    s.append(getAttributeString());                    // @Z1A

    s.append(">\n");

    // add parameters to the Applet tag
    for (int i=0; i< list_.size(); i++)
    {
      HTMLParameter p = (HTMLParameter)list_.elementAt(i);

      s.append(p.getTag());
    }

    s.append(getText() + "\n");

    s.append("</applet>\n");

    return s.toString();
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
   *  Removes a parameter from the applet tag.
   *  @param param The parameter.
   **/
  public void removeParameter(HTMLParameter param)
  {
    if (param == null)
      throw new NullPointerException("param");

    //@B1D

    if (list_.removeElement(param))
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
   *  Sets the base URL of the applet.  If the CODEBASE URL is relative, it is in
   *  relation to the current document URL.
   *
   *  @param codebase The base URL.
   **/
  public void setCodebase(String codebase)
  {
    if (codebase == null)
      throw new NullPointerException("codebase");

    if (codebase.length() == 0)
    {
      throw new ExtendedIllegalArgumentException("codebase", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }

    //@B1D

    String old = codebase_;

    codebase_ = codebase;

    changes_.firePropertyChange("codebase", old, codebase );

  }


  /**
   *  Sets the name of one or more archives containing classes and other resources that will be "preloaded".
   *  The archives are separated by a ",".
   *
   *  @param archive The applet archive(s).
   **/
  public void setArchive(String archive)
  {
    if (archive == null)
      throw new NullPointerException("archive");

    if (archive.length() == 0)
      throw new ExtendedIllegalArgumentException("archive", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

    //@B1D

    String old = archive_;

    archive_ = archive;

    changes_.firePropertyChange("archive", old, archive );
  }


  /**
   *  Sets the class name of the applet code.
   *
   *  @param code The applet name.
   **/
  public void setCode(String code)
  {
    if (code == null)
      throw new NullPointerException("code");

    if (code.length() == 0)
      throw new ExtendedIllegalArgumentException("code", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

    //@B1D

    String old = code_;

    code_ = code;

    changes_.firePropertyChange("code", old, code );
  }


  /**
   *  Set the height of the applet in pixels.
   *
   *  @param height The height.
   **/
  public void setHeight(int height)
  {
    if (height <= 0)
      throw new ExtendedIllegalArgumentException("height", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

    //@B1D

    int old = height_;

    height_ = height;

    changes_.firePropertyChange("height", new Integer(old), new Integer(height) );
  }


  /**
   *  Set the alternate text for the Applet, which will be displayed if
   *  the browser does not support the <i>APPLET</i> tag or the applet fails to load.
   *
   *  @param text The alternate text.
   **/
  public void setText(String text)
  {
    if (text == null)
      throw new NullPointerException("text");

    if (text.length() == 0)
      throw new ExtendedIllegalArgumentException("text",
                                                 ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

    //@B1D

    String old = text_;

    text_ = text;

    changes_.firePropertyChange("text", old, text );
  }


  /**
   *  Set the width of the applet in pixels.
   *
   *  @param width The width.
   **/
  public void setWidth(int width)
  {
    if (width <= 0)
      throw new ExtendedIllegalArgumentException("width", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

    //@B1D

    int old = width_;

    width_ = width;

    changes_.firePropertyChange("width", new Integer(old), new Integer(width) );
  }


  /**
   *  Returns a String representation for the HTMLApplet tag.
   *  @return The tag.
   **/
  public String toString()
  {
    return getTag();
  }
}
