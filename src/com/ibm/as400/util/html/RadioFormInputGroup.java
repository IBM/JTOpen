///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: RadioFormInputGroup.java
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
import java.util.Properties;                    // @Z1A
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;

/**
*  The RadioFormInputGroup class represents a group of RadioFormInput objects.  Only one
*  RadioFormInput object can be checked in the group.  The trailing slash &quot;/&quot; on the
*  RadioFormInputGroup tag allows it to conform to the XHTML specification.
*  <P>
*  This example creates a radio button group and prints out the tag.
*  <BLOCKQUOTE><PRE>
*  // Create some radio buttons.
*  RadioFormInput radio0 = new RadioFormInput("age", "kid", "0-12", true);
*  RadioFormInput radio1 = new RadioFormInput("age", "teen", "13-19", false);
*  RadioFormInput radio2 = new RadioFormInput("age", "twentysomething", "20-29", false);
*  RadioFormInput radio3 = new RadioFormInput("age", "thirtysomething", "30-39", false);
*  // Create a radio button group and add the radio buttons.
*  RadioFormInputGroup ageGroup = new RadioFormInputGroup("age");
*  ageGroup.add(radio0);
*  ageGroup.add(radio1);
*  ageGroup.add(radio2);
*  ageGroup.add(radio3);
*  System.out.println(ageGroup.getTag());
*  </PRE></BLOCKQUOTE>
*  <P>
*  Here is the output of the RadioFormInputGroup tag:<br>
*  <BLOCKQUOTE><PRE>
*  &lt;input type=&quot;radio&quot; name=&quot;age&quot; value=&quot;kid&quot; checked=&quot;checked&quot; /&gt; 0-12
*  &lt;input type=&quot;radio&quot; name=&quot;age&quot; value=&quot;teen&quot; /&gt; 13-19
*  &lt;input type=&quot;radio&quot; name=&quot;age&quot; value=&quot;twentysomething&quot; /&gt; 20-29
*  &lt;input type=&quot;radio&quot; name=&quot;age&quot; value=&quot;thirtysomething&quot; /&gt; 30-39
*  </PRE></BLOCKQUOTE>
*  <p>RadioFormInputGroup objects generate the following events:
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
*  @see com.ibm.as400.util.html.RadioFormInput
**/
public class RadioFormInputGroup extends HTMLTagAttributes implements java.io.Serializable     // @Z1C
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    private String name_;                // The name of the radio input group.
    private Vector list_;                // The list of radio buttons.
    private boolean useVertAlign_ = false; // Indicates if group alignment is vertical.
    private boolean groupCheck_ = false; // Indicates if group has radioforminput checked  $A1A


    transient private VetoableChangeSupport vetos_ = new VetoableChangeSupport(this);
    transient private Vector elementListeners = new Vector();      // The list of element listeners

    /**
    *  Constructs a default RadioFormInputGroup object.
    **/
    public RadioFormInputGroup()
    {
        super();
        list_ = new Vector();
    }

    /**
    *  Constructs a RadioFormInputGroup object with the specified control <i>name</i>.
    *  @param name The group control name.
    **/
    public RadioFormInputGroup(String name)
    {
        super();
        try
        {
            setName(name);
        }
        catch (PropertyVetoException e)
        {
        }

        list_ = new Vector();
    }

    /**
    *  Adds a radio button to the group.  If the group does not have a name, the
    *  name of the first radio button added will also be the group name.
    *  @param radioButton The radio button.
    **/
    public void add(RadioFormInput radioButton)
    {
        //@B1D

        if (radioButton == null)
            throw new NullPointerException("radioButton");

        // make sure all the names are the same
        // if we don't have a name, then use the first button
        if (name_ == null)
            name_ = radioButton.getName();
        else
        {
            try
            {
                radioButton.setName(name_);
            }
            catch ( PropertyVetoException e)
            {
            }
        }

        // only 1 radio button can be checked in the group
        if (groupCheck_ && radioButton.isChecked())           //$A1A
        {
            Trace.log(Trace.ERROR, "Previous RadioButton marked as 'checked'.");
            throw new ExtendedIllegalArgumentException("checked", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        else if (!groupCheck_ && radioButton.isChecked())
            groupCheck_ = true;

        list_.addElement(radioButton);

        fireElementEvent(ElementEvent.ELEMENT_ADDED);
    }


    /**
    *  Adds a radio button to the group.  If the group does not have a name, the
    *  name of the first radio button added will also be the group name.
    *  @param name The control name of the input field.
    *  @param value The input value used when the field is submitted.
    *  @param label The viewable text label.
    *  @param checked If the radio button initializes to checked.
    *
    *  @return A RadioFormInput object.
    **/
    public RadioFormInput add(String name, String value, String label, boolean checked)       //$A3A
    {
        //@B1D

        if (name == null)
            throw new NullPointerException("name");
        if (value == null)
            throw new NullPointerException("value");
        if (label == null)
            throw new NullPointerException("label");

        //Create the RadioFormInput from the values passed in
        RadioFormInput radioButton = new RadioFormInput(name,value,label,checked);

        // make sure all the names are the same
        // if we don't have a name, then use the first button
        if (name_ == null)
            name_ = name;
        else
        {
            try
            {
                radioButton.setName(name_);
            }
            catch ( PropertyVetoException e)
            {
            }
        }

        // only 1 radio button can be checked in the group
        if (groupCheck_ && radioButton.isChecked())           //$A1A
        {
            Trace.log(Trace.ERROR, "Previous RadioButton marked as 'checked'.");
            throw new ExtendedIllegalArgumentException("checked", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        else if (!groupCheck_ && radioButton.isChecked())
            groupCheck_ = true;

        // Add the RadioFormInput to the group.
        list_.addElement(radioButton);

        fireElementEvent(ElementEvent.ELEMENT_ADDED);

        return radioButton;
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
     * Adds the VetoableChangeListener.  The specified VetoableChangeListener's
     * <b>vetoableChange</b> method will be called each time the value of any
     * constrained property is changed.
     *
     * @see #removeVetoableChangeListener
     *
     * @param listener The VetoableChangeListener.
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
    *  Returns the control name of the radio group.
    *  @return The group control name.
    **/
    public String getName()
    {
        return name_;
    }

    /**
    *  Returns the radio button group tag.
    *  @return The tag.
    **/
    public String getTag()
    {
        //@B1D

        if (getName() == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before setting name.");
            throw new ExtendedIllegalStateException(
                                                   "name", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer s = new StringBuffer("");
        groupCheck_ = false;

        Properties eProp = null;
        Properties rgProp = getAttributes();

        for (int i=0; i< list_.size(); i++)
        {
            RadioFormInput r = (RadioFormInput)list_.elementAt(i);

            eProp = r.getAttributes();                        // @Z1A

            if (eProp == null && rgProp != null)             // @Z1A
                r.setAttributes(rgProp);                      // @Z1A

            s.append(r.getTag());
            //if the user wants to align the radio group vertically
            if (useVertAlign_)
                s.append(" <br />");

            s.append("\n");
        }
        return s.toString();
    }

    /**
   *  Indicates if the radio group alignment is vertical.
   *  The default value is false.
   *  @return true if vertical; horizontal otherwise.
   **/
    public boolean isAlignmentVertical()
    {
        return useVertAlign_;
    }

    /**
    *  Deserializes and initializes transient data.
    **/
    private void readObject(java.io.ObjectInputStream in)          //$A2A
    throws java.io.IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        changes_ = new PropertyChangeSupport(this);
        vetos_ = new VetoableChangeSupport(this);
        elementListeners = new Vector();
    }

    /**
    *  Removes a radio button from the group.
    *  @param radioButton The radio button.
    **/
    public void remove(RadioFormInput radioButton)
    {
        //@B1D

        if (radioButton == null)
            throw new NullPointerException("radioButton");

        // if we are removing the checked radio button, reset the groupCheck_ flag.
        if (radioButton.isChecked())
            groupCheck_ = false;

        if (list_.removeElement(radioButton))
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
     * Removes the VetoableChangeListener from the internal list.
     * If the VetoableChangeListener is not on the list, nothing is done.
     *
     * @see #addVetoableChangeListener
     *
     * @param listener The VetoableChangeListener.
    **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");
        vetos_.removeVetoableChangeListener(listener);
    }


    /**
    *  Sets the control name of the radio group.
    *  @param name The group control name.
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
    *  Sets the alignment of the radio group to vertical.
    *  The default is false.
    *  @param verticalAlignment true if alignment is vertical; false if horizontal.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setVerticalAlignment(boolean verticalAlignment)                        //$A3A
    throws PropertyVetoException
    {
        Boolean oldAlign = new Boolean(useVertAlign_);
        Boolean newAlign = new Boolean(verticalAlignment);

        vetos_.fireVetoableChange("verticalAlignment", oldAlign, newAlign);

        useVertAlign_ = verticalAlignment;

        changes_.firePropertyChange("verticalAlignment", oldAlign, newAlign);

    }


}
