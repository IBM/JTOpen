///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: FormInput.java
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

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;

/**
*  The FormInput class represents an input element in an HTML form.
*    
*  <p>FormInput objects generate the following events:
*  <ul>
*  <li>PropertyChangeEvent
*  <li>VetoableChangeEvent
*  </ul>
**/
abstract public class FormInput extends HTMLTagAttributes implements java.io.Serializable   // @Z1C
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private String name_;        // The input field name.
    private String value_;       // The initial value of the input field.
    private int size_ = 0;       // The visible size of the field in average char widths.
    private String lang_;        // The primary language used to display the tags contents.  //$B1A
    private String dir_;         // The direction of the text interpretation.                //$B1A


    transient VetoableChangeSupport vetos_ = new VetoableChangeSupport(this);

    /**
    *  Constructs a default FormInput object.
    **/
    public FormInput()
    {
        super();
    }

    /**
    *  Constructs a FormInput object with the specified control <i>name</i>.
    *  @param name The control name of the input field.
    **/
    public FormInput(String name)
    {
        this();
        try
        {
            setName(name);
        }
        catch (PropertyVetoException e)
        {
        }
    }

    /**
    *  Constructs a FormInput object with the specified controal <i>name</i>
    *  and the initial input <i>value</i>.
    *  @param name The control name of the input field.
    *  @param value The initial value of the input field.
    **/
    public FormInput(String name, String value)
    {
        this(name);
        try
        {
            setValue(value);
        }
        catch (PropertyVetoException e)
        {
        }
    }



    /**
     * Adds the VetoableChangeListener.  The specified VetoableChangeListener's
     * <b>vetoableChange</b> method will be called each time the value of any
     * constrained property is changed.
     *
     * @see #removeVetoableChangeListener
     *
     *@param listener The VetoableChangeListener.
    **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");
        vetos_.addVetoableChangeListener(listener);
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
    *  Returns the <i>language</i> of the input element.
    *  @return The language of the input element.
    **/
    public String getLanguage()                                //$B1A
    {
        return lang_;
    }

    /**
    *  Returns the control <i>name</i> of the input field.
    *  @return The control name of the input field.
    **/
    public String getName()
    {
        return name_;
    }

    /**
    *  Returns the initial <i>value</i> of the input field.
    *  @return The initial value.
    **/
    public String getValue()
    {
        return value_;
    }

    /**
    *  Returns the <i>size</i> of the input field.
    *  The size refers to the width of the input field in pixels or characters.
    *  @return The field size.
    **/
    public int getSize()
    {
        return size_;
    }


    /**
    *  Returns the direction attribute tag.
    *  @return The direction tag.
    **/
    String getDirectionAttributeTag()                                                 //$B1A
    {
        //@C1D

        if ((dir_ != null) && (dir_.length() > 0))
        {
            StringBuffer buffer = new StringBuffer(" dir=\"");
            buffer.append(dir_);
            buffer.append("\"");

            return buffer.toString();
        }
        else
            return "";
    }


    /**
    *  Returns the language attribute tag.                                            
    *  @return The language tag.                                                      
    **/                                                                               
    String getLanguageAttributeTag()                                                  //$B1A
    {
        //@C1D

        if ((lang_ != null) && (lang_.length() > 0))
        {
            StringBuffer buffer = new StringBuffer(" lang=\"");
            buffer.append(lang_);
            buffer.append("\"");

            return buffer.toString();
        }
        else
            return "";
    }


    /**
    *  Returns the name attribute tag.
    *  @return The name tag.
    **/
    String getNameAttributeTag()
    {
        //@C1D

        if ((name_ != null) && (name_.length() > 0))
        {
            StringBuffer buffer = new StringBuffer(" name=\"");
            buffer.append(name_);
            buffer.append("\"");

            return buffer.toString();
        }
        else
            return "";
    }

    /**
    *  Returns the size attribute tag.
    *  @return The size tag.
    **/
    String getSizeAttributeTag()
    {
        //@C1D

        if (size_ > 0)
        {
            StringBuffer buffer = new StringBuffer(" size=\"");
            buffer.append(size_);
            buffer.append("\"");

            return buffer.toString();
        }
        else
            return "";
    }

    /**
    *  Returns the value attribute tag.
    *  @param encode true if the value needs to be encoded; false otherwise.
    *  @return The value tag.
    **/
    String getValueAttributeTag(boolean encode)
    {
        //@C1D

	if (value_ != null)  //@C2C
        {
            if (Trace.isTraceOn())
                Trace.log(Trace.DIAGNOSTIC, "   URL value needs Encoding: " + encode);

            if (encode)
                value_ = URLEncoder.encode(value_);

            StringBuffer buffer = new StringBuffer(" value=\"");
            buffer.append(value_);
            buffer.append("\"");

            return buffer.toString();
        }
        else
            return "";
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
    public void setDirection(String dir)                                     //$B1A
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
    *  Sets the <i>language</i> of the input tag.
    *  @param lang The language.  Example language tags include:
    *  en and en-US.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setLanguage(String lang)                                      //$B1A
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
    *  Sets the control <i>name</i> of the input field.
    *  @param name The control name of the input field.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setName(String name)
    throws PropertyVetoException
    {   
        if (name == null)
            throw new NullPointerException("name");

        String old = name_;
        vetos_.fireVetoableChange("name", old, name );

        name_ = name;

        changes_.firePropertyChange("name", old, name );
    }

    /**
    *  Sets the <i>size</i> of the input field.
    *  The size refers to the width of the input field in pixels or characters according to its type.
    *
    *  @param size The field size.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setSize(int size)
    throws PropertyVetoException
    {
        if (size < 0)
            throw new ExtendedIllegalArgumentException("size", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        int old = size_;
        vetos_.fireVetoableChange("size", new Integer(old), new Integer(size) );

        size_ = size;

        changes_.firePropertyChange("size", new Integer(old), new Integer(size) );
    }

    /**
    *  Sets the initial value of the input field.
    *  @param value The initial input value.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setValue(String value)
    throws PropertyVetoException
    {   
        if (value == null)
            throw new NullPointerException("value");

        String old = value_;
        vetos_.fireVetoableChange("value", old, value );

        value_ = value;

        changes_.firePropertyChange("value", old, value );
    }


    /**
    *  Returns a String representation for the form input tag.
    *  @return The tag.
    **/
    public String toString()
    {
        return getTag();
    }
}
