///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ToggleFormInput.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.Trace;

import java.beans.PropertyVetoException;

/**
*  The ToggleFormInput class represents a toggle input type in an HTML form.
*
*  <p>ToggleFormInput objects generate the following events:
*  <ul>
*  <li>PropertyChangeEvent
*  <li>VetoableChangeEvent
*  </ul>
**/
public abstract class ToggleFormInput extends FormInput
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";
  static final long serialVersionUID = 4543596544106205362L;

    private boolean checked_ = false;        // If initializes to being checked.
    private String label_;


    /**
    *  Constructs a default ToggleFormInput object.
    **/
    public ToggleFormInput()
    {
        super();

    }

    /**
    *  Constructs a ToggleFormInput object with the specified viewable text <i>label</i>.
    *  @param label The viewable text label.
    **/
    public ToggleFormInput(String label)
    {
        super();
        try
        {
            setLabel(label);
        }
        catch (PropertyVetoException e)
        {
        }
    }

    /**
    *  Constructs a ToggleFormInput object with the specified viewable text <i>label</i>
    *  and initial <i>checked</i> value.
    *  @param label The viewable text label.
    *  @param checked If the toggle initializes to checked.
    **/
    public ToggleFormInput(String label, boolean checked)
    {
        this(label);
        try
        {
            setChecked(checked);
        }
        catch (PropertyVetoException e)
        {
        }
    }

    /**
    *  Returns the attribute tag.
    *  @return The tag.
    **/
    String getCheckedAttributeTag()
    {
        if (checked_)
            return " checked=\"checked\"";
        else
            return "";
    }

    /**
    *  Returns the viewable text label for the toggle.
    *  @return The viewable text label.
    **/
    public String getLabel()
    {
        return label_;
    }

    /**
    *  Indicates if the toggle is initialized to checked.
    *  @return true if checked; false otherwise.
    **/
    public boolean isChecked()
    {
        return checked_;
    }


    /**
    *  Sets whether the toggle is initialized to being checked.
    *  @param checked true if initialized to checked; false otherwise.  The default is false.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setChecked(boolean checked)
    throws PropertyVetoException
    {
        //@A1D

        boolean old = checked_;
        if (vetos_ != null) vetos_.fireVetoableChange("checked", new Boolean(old), new Boolean(checked) ); //@CRS

        checked_ = checked;

        if (changes_ != null) changes_.firePropertyChange("checked", new Boolean(old), new Boolean(checked) ); //@CRS
    }

    /**
    *  Sets the viewable text label for the toggle.
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
}
