///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: RadioFormInput.java
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

import java.beans.PropertyVetoException;

/**
*  The RadioFormInput class represents a radio button input type in an
*  HTML form which represents a <i>1-of-many</i> choice field.
*  The trailing slash &quot;/&quot; on the RadioFormInput tag allows it to 
*  conform to the XHTML specification.
*  <P>
*  Here is an example of a RadioFormInput tag:<br>
*  
*  &lt;input type=&quot;radio&quot; name=&quot;age&quot; value=&quot;twentysomething&quot; 
*  checked=&quot;checked&quot; /&gt; Age 20-29
*  
*  <P>
*  @see com.ibm.as400.util.html.RadioFormInputGroup
**/
public class RadioFormInput extends ToggleFormInput
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


    /**
    *  Constructs a default RadioFormInput object.
    **/
    public RadioFormInput()
    {
        super();

    }

    /**
    *  Constructs a RadioFormInput object with the specified viewable text <i>label</i>
    *  and initial <i>checked</i> value.
    *  @param label The viewable text label.
    *  @param checked If the radio button initializes to checked.
    **/
    public RadioFormInput(String label, boolean checked)
    {
        super(label, checked);
    }

    /**
    *  Constructs a RadioFormInput object with the specified control <i>name</i>, 
    *  initial input <i>value</i>, viewable text <i>label</i>, and initial <i>checked</i> value.
    *  @param name The control name of the input field.
    *  @param value The input value used when the field is submitted.
    *  @param label The viewable text label.
    *  @param checked If the radio button initializes to checked.
    **/
    public RadioFormInput(String name, String value, String label, boolean checked)
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
        return "<!-- A RadioFormInput was here -->";
    }

    /**
    *  Returns the tag for the radio button form input type.
    *  @return The tag.
    **/
    public String getTag()
    {
        //@C1D

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

        StringBuffer s = new StringBuffer("<input type=\"radio\"");

        s.append(getNameAttributeTag());
        s.append(getValueAttributeTag(false));
        s.append(getSizeAttributeTag());
        s.append(getLanguageAttributeTag());                                     //$B1A
        s.append(getDirectionAttributeTag());                                    //$B1A
        s.append(getAttributeString());                                          // @Z1A
        s.append(getCheckedAttributeTag());
        s.append(" /> ");
        s.append(getLabel());

        return s.toString();
    }
}
