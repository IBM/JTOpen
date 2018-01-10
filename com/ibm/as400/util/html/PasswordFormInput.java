///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: PasswordFormInput.java
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

/**
*  The PasswordFormInput class represents a password input field type
*  in an HTML form.  The trailing slash &quot;/&quot; on the PasswordFormInput 
*  tag allows it to conform to the XHTML specification.
*  <P>
*  Here is an example of a PasswordFormInput tag:<br>
*  &lt;input type=&quot;password&quot; name=&quot;password&quot; size=&quot;12&quot; /&gt;
**/
public class PasswordFormInput extends TextFormInput
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";
  static final long serialVersionUID = -8177182553241220755L;


    /**
    *  Constructs a default PasswordFormInput object.
    **/
    public PasswordFormInput()
    {
        super();

    }

    /**
    *  Constructs a PasswordFormInput object with the specified control <i>name</i>.
    *  @param name The control name of the input field.
    **/
    public PasswordFormInput(String name)
    {
        super(name);
    }

    /**
    *  Constructs a PasswordFormInput object with the specified control <i>name</i> and 
    *  initial input <i>value</i>.
    *  @param name The control name of the input field.
    *  @param value The initial value of the input field.
    **/
    public PasswordFormInput(String name, String value)
    {
        super(name, value);
    }

    /**
    *  Returns a comment tag.
    *  This method should not be called.  There is no XSL-FO support for this class.
    *  @return The comment tag.
    **/
    public String getFOTag()                                                //@D1A
    {
        Trace.log(Trace.ERROR, "Attempting to getFOTag() for an object that doesn't support it.");
        return "<!-- A PasswordFormInput was here -->";
    }

    /**
    *  Returns the tag for the password form input type.
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

        StringBuffer s = new StringBuffer("<input type=\"password\"");

        s.append(getNameAttributeTag());
        s.append(getValueAttributeTag(false));
        s.append(getMaxLengthAttributeTag());
        s.append(getSizeAttributeTag());
        s.append(getLanguageAttributeTag());                                        //$B1A
        s.append(getDirectionAttributeTag());                                       //$B1A
        s.append(getAttributeString());                                             // @Z1A
        s.append(" />");

        return s.toString();
    }
}
