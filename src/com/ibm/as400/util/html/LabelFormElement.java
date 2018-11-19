///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: LabelFormElement.java
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

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;

/**
*  The LabelFormElement class represents a label HTML form element type.
*
*  <p>LabelFormElement objects generate the following events:
*  <ul>
*  <li>PropertyChangeEvent
*  <li>VetoableChangeEvent
*  </ul>
**/
public class LabelFormElement implements HTMLTagElement, java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";
  static final long serialVersionUID = -325048684986308647L;

    private String label_;


    transient private PropertyChangeSupport changes_; //@CRS
    transient private VetoableChangeSupport vetos_; //@CRS

    /**
    *  Constructs a default LabelFormElement object.
    **/
    public LabelFormElement()
    {
    }

    /**
    *  Constructs a LabelFormElement object with the specified viewable text <i>label</i>.
    *  @param label The viewable text label.
    **/
    public LabelFormElement(String label)
    {
        try
        {
            setLabel(label);
        }
        catch (PropertyVetoException e)
        {
        }
    }


    /**
    * Adds a PropertyChangeListener.  The specified PropertyChangeListener's
    * <b>propertyChange</b> method will be called each time the value of any
    * bound property is changed.
    *
    * @see #removePropertyChangeListener
    *
    * @param listener The PropertyChangeListener.
    **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");
        if (changes_ == null) changes_ = new PropertyChangeSupport(this); //@CRS
        changes_.addPropertyChangeListener(listener);
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
        if (vetos_ == null) vetos_ = new VetoableChangeSupport(this); //@CRS
        vetos_.addVetoableChangeListener(listener);
    }

    /**
    *  Returns the viewable text label.
    *  @return The text label.
    **/
    public String getLabel()
    {
        return label_;
    }

    /**
    *  Returns a comment tag.
    *  This method should not be called.  There is no XSL-FO support for this class.
    *  @return The comment tag.
    **/
    public String getFOTag()                                                //@C1A
    {
        Trace.log(Trace.ERROR, "Attempting to getFOTag() for an object that doesn't support it.");
        return "<!-- A LabelFormElement was here -->";
    }

    /**
    *  Returns the label tag.
    *  @return The tag.
    **/
    public String getTag()
    {
        //@B1D

        if (label_ == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before setting label.");
            throw new ExtendedIllegalStateException(
                                                   "label", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        return label_;
    }

    /**
    *  Deserializes and initializes transient data.
    **/
    private void readObject(java.io.ObjectInputStream in)          //$A1A
    throws java.io.IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        //@CRS changes_ = new PropertyChangeSupport(this);
        //@CRS vetos_ = new VetoableChangeSupport(this);
    }


    /**
     * Removes the PropertyChangeListener from the internal list.
     * If the PropertyChangeListener is not on the list, nothing is done.
     *
     * @see #addPropertyChangeListener
     *
     * @param listener The PropertyChangeListener.
    **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");
        if (changes_ != null) changes_.removePropertyChangeListener(listener); //@CRS
    }



    /**
     * Removes the VetoableChangeListener from the internal list.
     * If the VetoableChangeListener is not on the list, nothing is done.
     * 
     * @see #addVetoableChangeListener
     * @param listener The VetoableChangeListener.
     **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");
        if (vetos_ != null) vetos_.removeVetoableChangeListener(listener); //@CRS
    }

    /**
    *  Sets the viewable text label.
    *  @param label The viewable text label.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setLabel(String label)
    throws PropertyVetoException
    {
        if (label == null)
            throw new NullPointerException("label");

        String old = label_;
        if (vetos_ != null) vetos_.fireVetoableChange("label", old, label ); //@CRS

        label_ = label;

        if (changes_ != null) changes_.firePropertyChange("label", old, label ); //@CRS
    }

    /**
    *  Returns the String representation of the label tag.
    *  @return The tag.
    **/
    public String toString()
    {
        return label_;
    }
}
