///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PasswordFormInput.java
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
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    
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
    *  Returns the tag for the password form input type.
    *  @return The tag.
    **/
    public String getTag()
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.INFORMATION, "Generating PasswordFormInput tag...");

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
