///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: HTMLServlet.java
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
*  The HTMLServlet class represents a server-side include in an HTML page.
*  <P>
*  This example creates a HTMLServlet tag:
*  <BLOCKQUOTE><PRE>
*  // Create an HTMLServlet.
*  HTMLServlet servlet = new HTMLServlet("myServlet", "http://server:port/dir");
*  <p>
*  // Create a parameter, then add it to the servlet.
*  HTMLParameter param = new HTMLParameter("parm1", "value1");
*  servlet.addParameter(param);
*  <p>
*  // Create and add second parameter
*  HTMLParameter param2 = servlet.add("parm2", "value2");
*  System.out.println(servlet);
*  </PRE></BLOCKQUOTE>
*  <P>
*  Here is the output of the HTMLServlet tag:<br>
*  <BLOCKQUOTE><PRE>
*  &lt;servlet name=&quot;myServlet&quot; codebase=&quot;http://server:port/dir&quot;&gt;
*  &lt;param name=&quot;parm1&quot; value=&quot;value1&quot;&gt;
*  &lt;param name=&quot;parm2&quot; value=&quot;value2&quot;&gt;
*  If you see this text, the web server providing this page does not support the SERVLET tag.
*  &lt;/servlet&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>HTMLServlet objects generate the following events:
*  <ul>
*  <li><A HREF="ElementEvent.html">ElementEvent</A> - The events fired are:
*    <ul>
*    <li>elementAdded
*    <li>elementRemoved
*    </ul>
*  <li>PropertyChangeEvent
*  </ul>

**/
public class HTMLServlet extends HTMLTagAttributes implements java.io.Serializable   // @Z1C
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


    private String name_;

    // The alternate text will get displayed if the servlet does not appear in the browser.
    private String text_ = loader_.getText("PROP_DESC_SERVLET_ALTTEXT");
    private String location_;

    private Vector list_;                // The list of servlet parameters.

    // Handles loading the appropriate resource bundle
    private static ResourceBundleLoader_h loader_;


    transient private Vector elementListeners = new Vector();      // The list of element listeners



    /**
    *  Constructs a default HTMLServlet object.
    **/
    public HTMLServlet()
    {
        super();
        list_ = new Vector();

    }


    /**
    *  Constructs an HTMLServlet object with the specified servlet <i>name</i>.
    *
    *  @param name The servlet name.
    **/
    public HTMLServlet(String name)
    {
        super();

        setName(name);

        list_ = new Vector();
    }


    /**
    *  Constructs an HTMLServlet object with the specified servlet <i>name</i> and <i>location</i>.
    *
    *  @param name The servlet name.
    *  @param location  The servlet location (http://server:port/dir).
    **/
    public HTMLServlet(String name, String location)
    {
        super();

        setName(name);
        setLocation(location);

        list_ = new Vector();

    }


    /**
    *  Adds an HTMLparameter to the servlet tag.
    *
    *  @param param The parameter.
    **/
    public void addParameter(HTMLParameter param)                                   //$A2C
    {
        //@B1D

        if (param == null)
            throw new NullPointerException("param");

        // add parameter to the list
        list_.addElement(param);

        fireElementEvent(ElementEvent.ELEMENT_ADDED);
    }


    /**
    *  Adds an HTMLParameter to the servlet.
    *
    *  @param name The parameter name.
    *  @param value The parameter value.
    *
    *  @return A HTMLParameter object.
    **/
    public HTMLParameter addParameter(String name, String value)                      //$A2C
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
     * managed by this HTMLServlet. It can be removed with removeElementListener.
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
     *  Returns the location of the servlet.
     *  @return The location.
     **/
    public String getLocation()
    {
        return location_;
    }


    /**
     *  Returns the name of the servlet.
     *  @return The name.
     **/
    public String getName()
    {
        return name_;
    }


    /**
     *  Returns the alternate text of the servlet.
     *  @return The text.
     **/
    public String getText()
    {
        return text_;
    }


    /**
    *  Returns the tag for the HTML servlet.
    *  @return The tag.
    **/
    public String getTag()
    {
        //@B1D

        if (name_ == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before setting servlet name.");
            throw new ExtendedIllegalStateException(
                                                   "name", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer s = new StringBuffer("<servlet");

        s.append(" name=\"" + getName() + "\"");

        if (location_ != null)
        {
            s.append(" codebase=\"");
            s.append(getLocation());
            s.append("\"");
            s.append(getAttributeString());       // @Z1A
            s.append(">\n");
        }
        else
        {
            s.append(getAttributeString());       // @Z1A
            s.append(">\n");
        }

        // add parameters to the servlet tag
        for (int i=0; i< list_.size(); i++)
        {
            HTMLParameter p = (HTMLParameter)list_.elementAt(i);      //$A2C

            s.append(p.getTag());
        }

        s.append(getText() + "\n");

        s.append("</servlet>\n");

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
    *  Removes an HTMLParameter from the servlet tag.
    *  @param param The parameter.
    **/
    public void removeParameter(HTMLParameter param)                                 //$A2C
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
     *  Sets the location for the servlet source.  It can refer to a remote location from which
     *  the servlet should be loaded.  The default location is assumed to be local.
     *
     *  @param location The location.
     **/
    public void setLocation(String location)
    {
        if (location == null)
            throw new NullPointerException("location");

        if (location.length() == 0 )
        {
            throw new ExtendedIllegalArgumentException("location", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        //@B1D

        String old = location_;

        location_ = location;

        changes_.firePropertyChange("location", old, location );

    }


    /**
    *  Sets the class name of the servlet.
    *
    *  @param source The name.
    **/
    public void setName(String name)
    {
        if (name == null)
            throw new NullPointerException("name");

        if (name.length() == 0)
            throw new ExtendedIllegalArgumentException("name",
                                                       ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

        //@B1D

        String old = name_;

        name_ = name;

        changes_.firePropertyChange("name", old, name );
    }


    /**
     *  Set the alternate text for the servlet, which will be displayed if
     *  the web server does not support the <i>servlet</i> tag.
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
    *  Returns a String representation for the HTMLServlet tag.
    *  @return The tag.
    **/
    public String toString()
    {
        return getTag();
    }
}
