///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: HTMLForm.java
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
import java.util.Enumeration;
import java.util.Properties;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;

/**
*  The HTMLForm class represents an HTML form.
*
*  <p>HTMLForm objects generate the following events:
*  <ul>
*  <LI><A HREF="ElementEvent.html">ElementEvent</A> - The events fired are:
*    <ul>
*    <li>elementAdded
*    <li>elementRemoved
*    </ul>
*  <li>PropertyChangeEvent
*  <li>VetoableChangeEvent
*  </ul>
*  <P>
*  This examples creates an HTMLForm object and adds some form input types to it.
*  <BLOCKQUOTE><PRE>
*  <P>         // Create a text input form element for the system.
*  LabelFormElement sysPrompt = new LabelFormElement("System:");
*  TextFormInput system = new TextFormInput("System");
*  <P>         // Create a text input form element for the userId.
*  LabelFormElement userPrompt = new LabelFormElement("User:");
*  TextFormInput user = new TextFormInput("User");
*  <P>         // Create a password input form element for the password.
*  LabelFormElement passwordPrompt = new LabelFormElement("Password:");
*  PasswordFormInput password = new PasswordFormInput("Password");
*  <P>         // Create a properties object.
*  Properties prop = new Properties();
*  <P>         // Add customer name and ID values to the properties object.
*  prop.put("custName", "Mr. Toolbox");
*  prop.put("custID", "12345");
*  <P>         // Create the submit button to the form.
*  SubmitFormInput logonButton = new SubmitFormInput("logon", "Logon");
*  <P>         // Create HTMLForm object and add the panel to it.
*  HTMLForm form = new HTMLForm(servletURI);
*  form.setHiddenParameterList(prop);
*  form.addElement(sysPrompt);
*  form.addElement(system);
*  form.addElement(userPrompt);
*  form.addElement(user);
*  form.addElement(passwordPrompt);
*  form.addElement(password);
*  form.addElement(logonButton);
*  </PRE></BLOCKQUOTE>
*  <P>
*  Here is an example of an HTMLForm tag:<br>
*  <PRE><BLOCKQUOTE>
*  &lt;form action=&quot;servletURI&quot; method=&quot;get&quot;&gt;
*     System:   &lt;input type=&quot;text&quot; name=&quot;System&quot; /&gt;
*     User:     &lt;input type=&quot;text&quot; name=&quot;User&quot; /&gt;
*     Password: &lt;input type=&quot;password&quot; name=&quot;Password&quot; /&gt;
*     &lt;input type=&quot;submit&quot; name=&quot;logon&quot; value=&quot;Logon&quot; /&gt;
*     &lt;input type=&quot;hidden&quot; name=&quot;custName&quot; value=&quot;Mr. Toolbox&quot; /&gt;
*     &lt;input type=&quot;hidden&quot; name=&quot;custID&quot; value=&quot;12345&quot; /&gt;
*  &lt;/form&gt;
*  </PRE></BLOCKQUOTE>
**/
public class HTMLForm extends HTMLTagAttributes implements HTMLConstants, java.io.Serializable   // @Z1C
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    /**
      HTTP GET Method for sending form contents to the server.
      This is the default method used.
    **/
    public static final int METHOD_GET = 0;
    /**
      HTTP POST Method for sending form contents to the server.
    **/
    public static final int METHOD_POST = 1;


    private Vector list_;                  // The list of FormElements.
    private String url_;                   // The ACTION url address.
    private String target_;                // The target frame for the link resource.
    private Properties parms_;             // The hidden parameter list.
    private boolean useGet_ = true;        //
    private int method_ = METHOD_GET;      // The HTTP method used.
    private String lang_;        // The primary language used to display the tags contents.  //$B1A
    private String dir_;         // The direction of the text interpretation.                //$B1A


    transient private VetoableChangeSupport vetos_ = new VetoableChangeSupport(this);
    transient private Vector elementListeners = new Vector();      // The list of element listeners

    /**
    *  Constructs a default HTMLForm object.
    **/
    public HTMLForm()
    {
        list_ = new Vector();
    }

    /**
    *  Constructs an HTMLForm object with the specified <i>URL</i>.
    *  @param url The URL address.
    **/
    public HTMLForm(String url)
    {
        this();
        try
        {
            setURL(url);
        }
        catch (PropertyVetoException e)
        {
        }
    }

    /**
    *  Adds a form <i>element</i> to the HTMLForm.
    *  @param element The form element.
    **/
    public void addElement(HTMLTagElement element)
    {
        //@C1D

        if (element == null)
            throw new NullPointerException("element");

        list_.addElement(element);

        fireElementEvent(ElementEvent.ELEMENT_ADDED);
    }

    /**
    *  Adds an ElementListener.
    *  The ElementListener object is added to an internal list of ElementListeners;
    *  it can be removed with removeElementListener.
    *    @see #removeElementListener
    *    @param listener The ElementListener.
    **/
    public void addElementListener(ElementListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");

        elementListeners.addElement(listener);
    }


    /**
    Adds the VetoableChangeListener.  The specified
    VetoableChangeListener's <b>vetoableChange</b>
    method is called each time the value of any
    constrained property is changed.
      @see #removeVetoableChangeListener
      @param listener The VetoableChangeListener.
    **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");
        vetos_.addVetoableChangeListener(listener);
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
    *  Returns the <i>direction</i> of the text interpretation.
    *  @return The direction of the text.
    **/
    public String getDirection()                               //$B1A
    {
        return dir_;
    }


    /**
    *  Returns the form's hidden parameter list.
    *  @return The parameter list.
    **/
    public Properties getHiddenParameterList()
    {
        return parms_;
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
    *  Returns the HTTP method used for sending form contents to the server.
    *  @return The HTTP method.
    **/
    public int getMethod()
    {
        return method_;
    }

    /**
    *  Returns the HTML form tag.
    *  @return The tag.
    **/
    public String getTag()
    {
        //@C1D

        if (url_ == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before setting the action URL address.");
            throw new ExtendedIllegalStateException(
                                                   "url", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer s = new StringBuffer("<form action=\"");
        s.append(url_);
        s.append("\"");

        if (method_ == METHOD_POST)
            s.append(" method=\"post\"");
        else
            s.append(" method=\"get\"");     // The default method is GET

        if (target_ != null)
        {
            if (Trace.isTraceOn())
                Trace.log(Trace.INFORMATION, "   Using target frame.");

            s.append(" target=\"");
            s.append(target_);
            s.append("\"");
        }
        if (lang_ != null)                                                       //$B1A
        {
            //$B1A
            if (Trace.isTraceOn())                                                //$B1A
                Trace.log(Trace.INFORMATION, "   Using language attribute.");      //$B1A
                                                                                   //$B1A
            s.append(" lang=\"");                                                 //$B1A
            s.append(lang_);                                                      //$B1A
            s.append("\"");                                                       //$B1A
        }                                                                        //$B1A
        if (dir_ != null)                                                        //$B1A
        {
            //$B1A
            if (Trace.isTraceOn())                                                //$B1A
                Trace.log(Trace.INFORMATION, "   Using direction attribute.");     //$B1A
                                                                                   //$B1A
            s.append(" dir=\"");                                                  //$B1A
            s.append(dir_);                                                       //$B1A
            s.append("\"");                                                       //$B1A
        }                                                                        //$B1A

        s.append(getAttributeString());                                          // @Z1A
        s.append(">\n");

        if (parms_ != null)
        {
            Enumeration names = parms_.propertyNames();
            while (names.hasMoreElements())
            {
                String name = (String)names.nextElement();
                String value = parms_.getProperty(name);
                HiddenFormInput h = new HiddenFormInput();
                try
                {
                    h.setName(name);
                    h.setValue(value);
                }
                catch (PropertyVetoException e)
                {
                }
                addElement(h);
            }
        }

        for (int i=0; i<list_.size(); i++)
        {
            HTMLTagElement formElement = (HTMLTagElement)list_.elementAt(i);
            s.append(formElement.getTag());
            s.append("\n");
        }

        s.append("</form>");

        return s.toString();
    }

    /**
    *  Returns the target frame for the form response.
    *  @return The target frame.  One of the following constants
    *  defined in HTMLConstants:  TARGET_BLANK, TARGET_PARENT,
    *  TARGET_SELF, TARGET_TOP, or a user defined target.
    *
    *  @see com.ibm.as400.util.html.HTMLConstants
    **/
    public String getTarget()
    {
        return target_;
    }

    /**
    *  Returns the ACTION URL address of the server-side form handler.
    *  @return The URL address.
    **/
    public String getURL()
    {
        return url_;
    }

    /** Indicates if the GET method is used for sending the form contents to the server.
     *  @return true if GET is used; false otherwise.
     **/
    public boolean isUseGet()
    {
        if (method_ == METHOD_GET)
            return true;
        else
            return false;
    }

    /**
    *  Indicates if the POST method is used for sending the form contents to the server.
    *  @return true if POST is used; false otherwise.
    **/
    public boolean isUsePost()
    {
        if (method_ == METHOD_POST)
            return true;
        else
            return false;
    }

    /**
     *  Deserializes and initializes transient data.
     **/
    private void readObject(java.io.ObjectInputStream in)          //$A1A
    throws java.io.IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        changes_ = new PropertyChangeSupport(this);
        vetos_ = new VetoableChangeSupport(this);
        elementListeners = new Vector();
    }

    /**
    *  Removes a form <i>element</i> from the HTMLForm.
    *  @param element The form element.
    **/
    public void removeElement(HTMLTagElement element)
    {
        //@C1D

        if (element == null)
            throw new NullPointerException("element");

        if (list_.removeElement(element))
            fireElementEvent(ElementEvent.ELEMENT_REMOVED);
    }

    /**
    *  Removes this ElementListener from the internal list.
    *  If the ElementListener is not on the list, nothing is done.
    *  @see #addElementListener
    *  @param listener The ElementListener.
    **/
    public void removeElementListener(ElementListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");

        elementListeners.removeElement(listener);
    }



    /**
    Removes the VetoableChangeListener from the internal list.
    If the VetoableChangeListener is not on the list, nothing is done.
      @see #addVetoableChangeListener
      @param listener The VetoableChangeListener.
    **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");
        vetos_.removeVetoableChangeListener(listener);
    }


    /**
     *  Sets the <i>direction</i> of the text interpretation.
     *  @param dir The direction.  One of the following constants
     *  defined in HTMLConstants:  LTR or RTL.
     *
     *  @see com.ibm.as400.util.html.HTMLConstants
     *
     *  @exception PropertyVetoException If a change is vetoed.
     **/
    public void setDirection(String dir)                                      //$B1A
    throws PropertyVetoException
    {
        if (dir == null)
            throw new NullPointerException("dir");

        // If direction is not one of the valid HTMLConstants, throw an exception.
        if ( !(dir.equals(HTMLConstants.LTR))  && !(dir.equals(HTMLConstants.RTL)) )
        {
            throw new ExtendedIllegalArgumentException("dir", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        String old = dir_;
        vetos_.fireVetoableChange("dir", old, dir );

        dir_ = dir;

        changes_.firePropertyChange("dir", old, dir );
    }


    /**
    *  Sets the form's hidden parameter list.
    *  @param parameterList The parameter list.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setHiddenParameterList(Properties parameterList)
    throws PropertyVetoException
    {
        if (parameterList == null)
            throw new NullPointerException("parameterList");

        Properties old = parms_;
        vetos_.fireVetoableChange("parameterList", old, parameterList );

        parms_ = parameterList;

        changes_.firePropertyChange("parameterList", old, parameterList );
    }


    /**
     *  Sets the <i>language</i> of the input tag.
     *  @param lang The language.  Example language tags include:
     *  en and en-US.
     *
     *  @exception PropertyVetoException If a change is vetoed.
     **/
    public void setLanguage(String lang)                                   //$B1A
    throws PropertyVetoException
    {
        if (lang == null)
            throw new NullPointerException("lang");

        String old = lang_;
        vetos_.fireVetoableChange("lang", old, lang );

        lang_ = lang;

        changes_.firePropertyChange("lang", old, lang );
    }


    /**
    *  Sets the HTTP method used to send form contents to the server.
    *  @param method The method.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setMethod(int method)
    throws PropertyVetoException
    {
        if (method < METHOD_GET || method > METHOD_POST)
            throw new ExtendedIllegalArgumentException("method", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        int old = method_;
        vetos_.fireVetoableChange("method", new Integer(old), new Integer(method) );

        method_ = method;

        changes_.firePropertyChange("method", new Integer(old), new Integer(method) );
    }

    /**
    *  Sets the <i>target</i> frame for the form response.
    *  @param target The target frame.  One of the following constants
    *  defined in HTMLConstants:  TARGET_BLANK, TARGET_PARENT,
    *  TARGET_SELF, TARGET_TOP, or a user defined target.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    *  @see com.ibm.as400.util.html.HTMLConstants
    **/
    public void setTarget(String target)
    throws PropertyVetoException
    {
        if (target == null)
            throw new NullPointerException("target");

        String old = target_;
        vetos_.fireVetoableChange("target", old, target );

        target_ = target;

        changes_.firePropertyChange("target", old, target );
    }

    /**
    *  Sets the ACTION URL address of the server-side form handler.
    *  @param url The URL address.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setURL(String url)
    throws PropertyVetoException
    {
        if (url == null)
            throw new NullPointerException("url");

        String old = url_;
        vetos_.fireVetoableChange("url", old, url );

        url_ = url;

        changes_.firePropertyChange("url", old, url );
    }

    /**
    *  Returns the HTMLForm tag as a String.
    *  @return The tag.
    **/
    public String toString()
    {
        return getTag();
    }
}
