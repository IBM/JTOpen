///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: HTMLParameter.java
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

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;


/**
*  The HTMLParameter class represents a parameter within an HTML <i>servlet</i> tag.
*  
*  <P>
*  This example creates a HTMLParameter tag:
*  <BLOCKQUOTE><PRE>
*  // Create an HTMLServletParameter.
*  HTMLParameter parm = new HTMLParameter("age", "21");
*  System.out.println(parm);
*  </PRE></BLOCKQUOTE>
*  <P>
*  Here is the output of the HTMLParameter tag:<br>
*  <BLOCKQUOTE><PRE>
*  &lt;param name=&quot;age&quot; value=&quot;21&quot;&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>HTMLParameter objects generate the following events:
*  <ul>
*    <li>PropertyChangeEvent
*  </ul>
**/
public class HTMLParameter extends HTMLTagAttributes implements java.io.Serializable     // @Z1C
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";
  static final long serialVersionUID = 6620025554753779980L;


    private String name_;
    private String value_;



    /**
    *  Constructs a default HTMLParameter object.
    **/
    public HTMLParameter()
    {
        super();

    }          


    /**
    *  Constructs an HTMLParameter object with the specified <i>name</i> and <i>value</i>.
    *
    *  @param name The parameter name.
    *  @param value The parameter value.
    **/
    public HTMLParameter(String name, String value)
    {
        super();

        setName(name);
        setValue(value);
    }



    /**
     *  Returns the name of the parameter.
     *  @return The name.
     **/
    public String getName()
    {
        return name_;
    }


    /**
     *  Returns the value of the parameter.
     *  @return The value.
     **/
    public String getValue()
    {
        return value_;
    }


    /**
    *  Returns a comment tag.
    *  This method should not be called.  There is no XSL-FO support for this class.
    *  @return The comment tag.
    **/
    public String getFOTag()                                                //@C1A
    {
        Trace.log(Trace.ERROR, "Attempting to getFOTag() for an object that doesn't support it.");
        return "<!-- An HTMLParameter was here -->";
    }

    /**
    *  Returns the tag for the HTML parameter.
    *  @return The tag.
    **/
    public String getTag()
    {
        //@B1D

        if (name_ == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before setting HTML parameter name.");
            throw new ExtendedIllegalStateException(
                                                   "name", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        if (value_ == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before setting HTML parameter value.");
            throw new ExtendedIllegalStateException(
                                                   "value", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer s = new StringBuffer("<param");

        s.append(" name=\"");
        s.append(getName());
        s.append("\"");
        s.append(" value=\"");
        s.append(getValue());
        s.append("\"");
        s.append(getAttributeString());       // @Z1A
        s.append(">");

        s.append("\n");

        return s.toString();
    }


    /**
    *  Deserializes and initializes transient data.
    **/
    private void readObject(java.io.ObjectInputStream in)          
    throws java.io.IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        //@CRS changes_ = new PropertyChangeSupport(this);
    }



    /**
     *  Sets the name of the parameter.
     *
     *  @param name The name.
     **/             
    public void setName(String name)
    {
        if (name == null)
            throw new NullPointerException("name");

        if (name.length() == 0 )
        {
            throw new ExtendedIllegalArgumentException("name", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        //@B1D

        String old = name_;

        name_ = name;

        if (changes_ != null) changes_.firePropertyChange("name", old, name ); //@CRS

    }


    /**
     *  Set the value of the parameter.
     *
     *  @param value The value.
     **/
    public void setValue(String value)
    {
        if (value == null)
            throw new NullPointerException("value");

        if (value.length() == 0)
            throw new ExtendedIllegalArgumentException("value", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

        //@B1D

        String old = value_;

        value_ = value;

        if (changes_ != null) changes_.firePropertyChange("value", old, value ); //@CRS
    }


    /**
    *  Returns a String representation for the HTMLParameter tag.
    *  @return The tag.
    **/
    public String toString()
    {
        return getTag();
    }
}

