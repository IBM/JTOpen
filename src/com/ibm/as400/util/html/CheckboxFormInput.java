///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: CheckboxFormInput.java
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

import java.beans.PropertyVetoException;

/**
*  The CheckboxFormInput class represents a checkbox input type in an 
*  HTML form where the checkbox represents an <i>n-of-many</i> choice field.
*  The trailing slash &quot;/&quot; on the CheckboxFormInput tag allows it to 
*  conform to the XHTML specification.
*  <P>
*  Here is an example of a CheckboxFormInput tag:<br>
*  &lt;input type=&quot;checkbox&quot; name=&quot;uscitizen&quot; value=&quot;yes&quot; 
*  checked=&quot;checked&quot; /&gt; textLabel
**/
public class CheckboxFormInput extends ToggleFormInput
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    /**
    *  Constructs a default CheckboxFormInput object.
    **/
    public CheckboxFormInput()
    {
        super();

    }

    /**
    *  Constructs a CheckboxFormInput object with the specified viewable text <i>label</i>
    *  and initial <i>checked</i> value.
    *  @param label The viewable text label.
    *  @param checked If the checkbox initializes to checked.
    **/
    public CheckboxFormInput(String label, boolean checked)
    {
        super(label, checked);              
    }

    /**
    *  Constructs a CheckboxFormInput object with the specified control <i>name</i>, 
    *  initial input <i>value</i>, viewable text <i>label</i>, and initial <i>checked</i> value.
    *  @param name The control name of the input field.
    *  @param value The input value used when the field is submitted.
    *  @param label The viewable text label.
    *  @param checked If the checkbox initializes to checked.
    **/
    public CheckboxFormInput(String name, String value, String label, boolean checked)
    {
        super(label, checked);
        try
        {
            setName(name);
            setValue(value);
        }
        catch (PropertyVetoException e)
        {
        }
    }

    /**
    *  Returns a comment tag.
    *  This method should not be called.  There is no XSL-FO support for this class.
    *  @return The comment tag.
    **/
    public String getFOTag()                                                //@D1A
    {
        Trace.log(Trace.ERROR, "Attempting to getFOTag() for an object that doesn't support it.");
        return "<!-- A CheckboxFormInput was here -->";
    }

    /**
    *  Returns the tag for the checkbox form input type.
    *  @return The tag.
    **/
    public String getTag()
    {

        if (getName() == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before setting name.");
            throw new ExtendedIllegalStateException(
                                                   "name", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }
        if (getValue() == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before setting value.");
            throw new ExtendedIllegalStateException(
                                                   "value", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }
        if (getLabel() == null)
        {
            Trace.log(Trace.ERROR, "Attempting to get tag before setting label.");
            throw new ExtendedIllegalStateException(
                                                   "label", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer s = new StringBuffer("<input type=\"checkbox\"");

        s.append(getNameAttributeTag());
        s.append(getValueAttributeTag(false));
        s.append(getSizeAttributeTag());
        s.append(getLanguageAttributeTag());                                         //$B1A
        s.append(getDirectionAttributeTag());                                        //$B1A
        s.append(getAttributeString());                                              // @Z1A
        s.append(getCheckedAttributeTag());                                          
        s.append(" /> ");
        s.append(getLabel());

        return s.toString();
    }
}
