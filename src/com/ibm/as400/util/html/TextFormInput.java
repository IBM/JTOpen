///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: TextFormInput.java
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
import com.ibm.as400.access.ExtendedIllegalArgumentException;

import java.beans.PropertyVetoException;

/**
*  The TextFormInput class represents a single line text input type in 
*  an HTML form.  The trailing slash &quot;/&quot; on the TextFormInput tag 
*  allows it to conform to the XHTML specification.
*  <P>
*  Here is an example of a TextFormInput tag:<br>
*  &lt;input type=&quot;text&quot; name=&quot;userID&quot; size=&quot;40&quot; /&gt;
*
*  <p>TextFormInput objects generate the following events:
*  <ul>
*  <li>PropertyChangeEvent
*  <li>VetoableChangeEvent
*  </ul>
**/
public class TextFormInput extends FormInput
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private int maxLength_;    // The maximum length of the text field.

    
    /**
    *  Constructs a default TextFormInput object.  There is no initial
    *  limit on the maximum number of characters permitted in the text 
    *  field.
    **/
    public TextFormInput()
    {
       super();
       maxLength_ = -1;             // no limit

    }

    /**
    *  Constructs a TextFormInput object with the specified control <i>name</i>.
    *  There is no initial limit on the maximum number of characters 
    *  permitted in the text field.
    *
    *  @param name The control name of the input field.
    **/
    public TextFormInput(String name)
    {
        super(name);
        maxLength_ = -1;            // no limit
    }

    /**
    *  Constructs a TextFormInput object with the specified control <i>name</i> and
    *  initial input <i>value</i>.  There is no initial limit on the maximum number of 
    *  characters permitted in the text field.
    *
    *  @param name The control name of the input field.
    *  @param value The initial value of the input field.
    **/
    public TextFormInput(String name, String value)
    {
        super(name, value);
        maxLength_ = -1;            // no limit
    }

    /**
    *  Returns the maximum number of characters permitted in the text field.
    *  A value of -1 indicates that there is no limit.
    *  @return The maximum length.
    **/
    public int getMaxLength()
    {
        return maxLength_;
    }
               
    /**
    *  Returns the max length attribute tag.
    *  @return The tag.
    **/
    String getMaxLengthAttributeTag()
    {
        if (maxLength_ > 0)
            return " maxlength=\"" + maxLength_ + "\"";
        else
            return "";
    }

    /**
    *  Returns the tag for the text form input type.
    *  @return The tag.
    **/
    public String getTag()
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.INFORMATION, "Generating TextFormInput tag...");

        if (getName() == null)
        {
           Trace.log(Trace.ERROR, "Attempting to get tag before setting name.");
           throw new ExtendedIllegalStateException(
               "name", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer s = new StringBuffer("<input type=\"text\"");

        s.append(getNameAttributeTag());
        s.append(getValueAttributeTag(false));
        s.append(getMaxLengthAttributeTag());
        s.append(getSizeAttributeTag());
        s.append(" />");

        return s.toString();
    }

 
    /**
    *  Sets the maximum number of characters permitted in the text field.
    *  @param length The maximum length.
    *
    *  @exception PropertyVetoException If a change is vetoed.
    **/
    public void setMaxLength(int length)
      throws PropertyVetoException
    {
        if (length < 0)
           throw new ExtendedIllegalArgumentException("maxLength", ExtendedIllegalArgumentException.RANGE_NOT_VALID);

        int old = maxLength_;
        vetos_.fireVetoableChange("maxLength", new Integer(old), new Integer(length) );

        maxLength_ = length;

        changes_.firePropertyChange("maxLength", new Integer(old), new Integer(length) );
    }
}
