///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: SelectOption.java
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

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;

/**
*  The SelectOption class represents an option in an HTML option form element.<BR>
*  The option form element is then used in an HTML select form element, which represents a group of 
*  selectable options. The trailing slash &quot;/&quot; on the SelectOption tag allows it to conform to
*  the XHTML specification.
*  <P>
*  This example creates a SelectOption object named <i>item1</i> that is initially selected.
*  <P>
*  <BLOCKQUOTE><PRE>
*  SelectOption item1 = new SelectOption("Item1", "item1", true);
*  System.out.println(item1.getTag());
*  </PRE></BLOCKQUOTE>
*
*  <P>Here is the output of the SelectOption tag:<br>
*  <BLOCKQUOTE><PRE>
*  &lt;option value=&quot;item1&quot; selected=&quot;selected&quot;&gt;Item1&lt;/option&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>SelectOption objects generate the following events:
*  <ul>
*  <li>PropertyChangeEvent
*  <li>VetoableChangeEvent
*  </ul>
*  
*  @see com.ibm.as400.util.html.SelectFormElement
**/
public class SelectOption implements HTMLTagElement, java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private String value_;             // The option value.
    private boolean selected_;         // Whether the option defaults as selected.
    private String text_;              // The option text.


    transient private PropertyChangeSupport changes_ = new PropertyChangeSupport(this);
    transient private VetoableChangeSupport vetos_ = new VetoableChangeSupport(this);

    /**
    *  Constructs a default SelectOption object.
    **/
    public SelectOption()
    {
        super();
        selected_ = false;
    }

    /**
    *  Constructs a SelectOption object with the specified viewable <i>text</i> and initial
    *  input <i>value</i>.  By default, the option is not selected.
    *  @param text The veiwable option text.
    *  @param value The input value used when the field is submitted.
    **/
    public SelectOption(String text, String value)
    {
        super();
        try {
           setValue(value);
           setText(text);
        }
        catch (PropertyVetoException e)
        {
        }
    }

    /**
    *  Constructs a SelectOption object with the specified viewable <i>text</i>,
    *  initial input <i>value</i>, and initial <i>selected</i> value.
    *  @param text The viewable option text.
    *  @param value The input value used when the field is submitted.
    *  @param selected Whether the option defaults as being selected.
    **/
    public SelectOption(String text, String value, boolean selected)
    {
        super();
        try {
           setValue(value);
           setSelected(selected);
           setText(text);
        }
        catch (PropertyVetoException e)
        {
        }
    }

    /**
    Adds a PropertyChangeListener.  The specified PropertyChangeListener's
    <b>propertyChange</b> method will be called each time the value of any
    bound property is changed.
      @see #removePropertyChangeListener
      @param listener The PropertyChangeListener.
    **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
       if (listener == null)
            throw new NullPointerException ("listener");
       changes_.addPropertyChangeListener(listener);
    }
 
 
    /**
    Adds the VetoableChangeListener.  The specified VetoableChangeListener's
    <b>vetoableChange</b> method will be called each time the value of any
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
    *  Returns the select option tag.
    *  @return The tag.
    **/
    public String getTag()
    {
        return getTag(text_);
    }

    /**
    *  Returns the select option tag with the new viewable option <i>text</i>.
    *  The original text of the select option object is not changed/updated.
    *  @param text The new option text.
    *  @return The tag.
    **/
    public String getTag(String text)
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.INFORMATION, "Generating SelectOption tag...");

        StringBuffer s = new StringBuffer("<option");

        if (value_ != null)
            s.append(" value=\"" + value_ + "\"");

        if (selected_)
            s.append(" selected=\"selected\"");

        s.append(">");

        if (text == null)
        {
           Trace.log(Trace.ERROR, "Parameter 'text' is null.");
           throw new ExtendedIllegalStateException(
               "text", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }
        else
           s.append(text);

        s.append("</option>");

        return s.toString();
    }

    /**
    *  Returns the viewable option text.
    *  @return The option text.
    **/
    public String getText()
    {
        return text_;
    }

    /**
    *  Returns the input value used when the field is submitted.
    *  @return The input value.
    **/
    public String getValue()
    {
        return value_;
    }

    /**
    *  Indicates if the option defaults to being selected.
    *  @return true if defaults as selected; false otherwise.
    **/
    public boolean isSelected()
    {
        return selected_;
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
    Removes the PropertyChangeListener from the internal list.
    If the PropertyChangeListener is not on the list, nothing is done.
      @see #addPropertyChangeListener
      @param listener The PropertyChangeListener.
    **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
       if (listener == null)
            throw new NullPointerException ("listener");
       changes_.removePropertyChangeListener(listener);
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
    *  Sets whether the option defaults as being selected.
    *  @param selected Whether the option defaults as selected.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setSelected(boolean selected)
      throws PropertyVetoException
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.INFORMATION, "   Option defaults as being selected.");

        boolean old = selected_;

        vetos_.fireVetoableChange("selected", new Boolean(old), new Boolean(selected) );

        selected_ = selected;

        changes_.firePropertyChange("selected", new Boolean(old), new Boolean(selected) );
    }
    
    /**
    *  Sets the option text with the specified viewable <i>text</i>.
    *  @param text The option text.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setText(String text)
      throws PropertyVetoException
    {
        if (text == null)
           throw new NullPointerException("text");

        String old = text_;

        vetos_.fireVetoableChange("text", old, text );

        text_ = text;

        changes_.firePropertyChange("text", old, text );
    }

    /**
    *  Sets the input <i>value</i> used when the field is submitted.
    *  @param value The input value.
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
    *  Returns the String representation of the select option tag.
    *  @return The tag.
    **/
    public String toString()
    {
        return getTag();
    }
}
