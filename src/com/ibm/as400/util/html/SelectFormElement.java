///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SelectFormElement.java
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

import java.util.*;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;


/**
*  The SelectFormElement class represents a select input type in an HTML form.
*  The trailing slash &quot;/&quot; on the SelectFormElement tag allows it to
*  conform to the XHTML specification.
*
*  <P>
*  This example creates a SelectFormElement object with three options and prints out the HTML tag.
*  The first two options added specify the option text, name, and select attributes.  The third
*  option added is defined by a SelectOption object.
*  <P>
*  <BLOCKQUOTE><PRE>
*  SelectFormElement list = new SelectFormElement("list1");
*  SelectOption option1 = list.addOption("Option1", "opt1");
*  SelectOption option2 = list.addOption("Option2", "opt2", false);
*  SelectOption option3 = new SelectOption("Option3", "opt3", true);
*  list.addOption(option3);
*  System.out.println(list.getTag());
*  </PRE></BLOCKQUOTE>
*
*  <P>Here is the output of the SelectFormElement tag:<br>
*  <BLOCKQUOTE><PRE>
*  &lt;select name=&quot;list1&quot;&gt;
*  &lt;option value=&quot;opt1&quot;&gt;Option1&lt;/option&gt;
*  &lt;option value=&quot;opt2&quot;&gt;Option2&lt;/option&gt;
*  &lt;option value=&quot;opt3&quot; selected=&quot;selected&quot;&gt;Option3&lt;/option&gt;
*  &lt;/select&gt;
*  </PRE></BLOCKQUOTE>
*
*  <p>SelectFormElement objects generate the following events:
*  <ul>
*  <li><A HREF="ElementEvent.html">ElementEvent</A> - The events fired are:
*    <ul>
*    <li>elementAdded
*    <li>elementRemoved
*    </ul>
*  <li>PropertyChangeEvent
*  <li>VetoableChangeEvent
*  </ul>
*
*  @see com.ibm.as400.util.html.SelectOption
**/
public class SelectFormElement extends HTMLTagAttributes implements java.io.Serializable    // @Z1C
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    private String name_;           // The select element name.
    private int size_;              // The number of visible choices.
    private boolean multiple_;      // Whether multiple selections can be made.
    private boolean optionSelected_;// Whether a option is marked as selected.

    private String lang_;        // The primary language used to display the tags contents.  //$B1A
    private String dir_;         // The direction of the text interpretation.                //$B1A

    private Vector list_;           // List of options.


    transient private VetoableChangeSupport vetos_ = new VetoableChangeSupport(this);
    transient private Vector elementListeners = new Vector();     // The list of element listeners

    /**
    *  Constructs a default SelectFormElement object.
    **/
    public SelectFormElement()
    {
        super();
        size_ = 0;
        multiple_ = false;
        list_ = new Vector();
    }

    /**
    *  Constructs a SelectFormElement with the specified control <i>name</i>.
    *  @param name The control name of the select element.
    **/
    public SelectFormElement(String name)
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
     Adds an addElementListener.
     The specified addElementListeners <b>elementAdded</b> method will
     be called each time a radioforminput is added to the group.
     The addElementListener object is added to a list of addElementListeners
     managed by this RadioFormInputGroup. It can be removed with removeElementListener.
 
     @see #removeElementListener
 
     @param listener The ElementListener.
     **/
    public void addElementListener(ElementListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");
        elementListeners.addElement(listener);
    }


    /**
   *  Adds an option to the select form element.
   *  @param option The select option.
   **/
    public void addOption(SelectOption option)
    {
        //@C1D

        if (option == null)
            throw new NullPointerException("option");

        if ((option.isSelected()) && (optionSelected_))
        {
            Trace.log(Trace.ERROR, "Previous option marked as 'selected'.");
            throw new ExtendedIllegalArgumentException("selected", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        else if ((option.isSelected()) && !(optionSelected_))
            optionSelected_ = true;
        list_.addElement(option);

        fireElementEvent(ElementEvent.ELEMENT_ADDED);
    }

    /**
    *  Adds an option with the specified viewable <i>text</i> and initial input
    *  <i>value</i> to the select form element.
    *  @param text The viewable option text.
    *  @param value The option input value.
    *
    *  @return A SelectOption object.
    **/
    public SelectOption addOption(String text, String value)
    {
        return addOption(text, value, false);

    }

    /**
    *  Adds an option with the specified viewable <i>text</i>, initial input <i>value</i>,
    *  and initial <i>selected</i> value to the select form element.  Only one option can be
    *  <i>selected</i> in the select form element at a time.
    *  @param text The viewable option text.
    *  @param value The option input value.
    *  @param selected true if the option defaults as being selected; false otherwise.
    *
    *  @return A SelectOption object.
    **/
    public SelectOption addOption(String text, String value, boolean selected)
    {
        //@C1D

        if (text == null)
            throw new NullPointerException("text");

        if (value == null)
            throw new NullPointerException("value");

        if ((selected) && (optionSelected_))
        {
            Trace.log(Trace.ERROR, "Previous option marked as 'selected'.");
            throw new ExtendedIllegalArgumentException("selected", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        else if ((selected) && !(optionSelected_))
            optionSelected_ = true;

        SelectOption option = new SelectOption(text, value, selected);
        list_.addElement(option);

        fireElementEvent(ElementEvent.ELEMENT_ADDED);

        return option;
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
    *  Returns the <i>language</i> of the input element.
    *  @return The language of the input element.
    **/
    public String getLanguage()                                //$B1A
    {
        return lang_;
    }

    /**
    *  Returns the control name of the select element.
    *  @return The control name.
    **/
    public String getName()
    {
        return name_;
    }

    /**
    *  Returns the number of elements in the option layout.
    *  @return The number of elements.
    **/
    public int getOptionCount()
    {
        return list_.size();
    }

    /**
    *  Returns the number of visible options.
    *  @return The number of options.
    **/
    public int getSize()
    {
        return size_;
    }


    /**
    *  Returns the select form element tag.
    *  @return The tag.
    **/
    public String getTag()
    {
        //@C1D

        if (name_ == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before setting name.");
            throw new ExtendedIllegalStateException(
                                                   "name", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer s = new StringBuffer("<select");

        s.append(" name=\"");
        s.append(name_);
        s.append("\"");

        if (size_ > 0)
        {
            s.append(" size=\"");
            s.append(size_);
            s.append("\"");
        }

        if (multiple_)
            s.append(" multiple=\"multiple\"");

        if ((lang_ != null) && (lang_.length() > 0))                            //$B1A
        {
            //$B1A
            if (Trace.isTraceOn())                                               //$B1A
                Trace.log(Trace.INFORMATION, "   Using language attribute.");     //$B1A
                                                                                  //$B1A
            s.append(" lang=\"");                                                //$B1A
            s.append(lang_);                                                     //$B1A
            s.append("\"");                                                      //$B1A
        }                                                                       //$B1A

        if ((dir_ != null) && (dir_.length() > 0))                              //$B1A
        {
            //$B1A
            if (Trace.isTraceOn())                                               //$B1A
                Trace.log(Trace.INFORMATION, "   Using direction attribute.");    //$B1A
                                                                                  //$B1A
            s.append(" dir=\"");                                                 //$B1A
            s.append(dir_);                                                      //$B1A
            s.append("\"");                                                      //$B1A
        }                                                                       //$B1A

        s.append(getAttributeString());                                         // @Z1A
        s.append(">\n");

        optionSelected_ = false;

        for (int i=0; i<getOptionCount(); i++)
        {
            SelectOption option = (SelectOption)list_.elementAt(i);

            s.append(option.getTag());
            s.append("\n");
        }

        s.append("</select>");

        return s.toString();
    }

    /**
    *  Indicates if the user can make multiple selections.
    *  @return true if multiple selections are allowed; false otherwise.
    **/
    public boolean isMultiple()
    {
        return multiple_;
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
    *  Removes an option from the select form element.
    *  @param option The select option.
    **/
    public void removeOption(SelectOption option)
    {
        //@C1D

        if (option == null)
            throw new NullPointerException("option");

        // if removing the option that is selected, reset the optionSelected_ flag.
        if (option.isSelected())
            optionSelected_ = false;

        if (list_.removeElement(option))
            fireElementEvent(ElementEvent.ELEMENT_REMOVED);
    }

    /**
    Removes this ElementListener from the internal list.
    If the ElementListener is not on the list, nothing is done.

    @see #addElementListener

    @param listener The ElementListener.
    **/
    public void removeElementListener(ElementListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");
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
    *  Sets whether the user can make multiple selections.
    *  @param multiple true if multiple selections are allowed; false otherwise.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setMultiple(boolean multiple)
    throws PropertyVetoException
    {
        //@C1D

        boolean old = multiple_;
        vetos_.fireVetoableChange("multiple", new Boolean(old), new Boolean(multiple) );

        multiple_ = multiple;

        changes_.firePropertyChange("multiple", new Boolean(old), new Boolean(multiple) );
    }

    /**
    *  Sets the control name of the select element.
    *  @param The control name.
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
    *  Sets the number of visible options.
    *  @param size The number of options.
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
    *  Returns the String representation of the select form element tag.
    *  @return The tag.
    **/
    public String toString()
    {
        return getTag();
    }
}
