///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HiddenFormInput.java
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
*  The HiddenFormInput class represents a hidden input type in an HTML form.
*  The trailing slash &quot;/&quot; on the HiddenFormInput tag allows it to conform to
*  the XHTML specification.
*  <P>
*  Here is an example of a HiddenFormInput tag:<br>
*  &lt;input type=&quot;hidden&quot; name=&quot;account&quot; value=&quot;123456&quot; /&gt;
**/
public class HiddenFormInput extends FormInput
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    
    /**
    *  Constructs a HiddenFormInput object.
    **/
    public HiddenFormInput()
    {
       super();

    }

    /**
    *  Constructs a HiddenFormInput object with the specified control <i>name</i>.
    *  @param name The control name of the input field.
    **/
    public HiddenFormInput(String name)
    {
        super(name);
    }

    /**
    *  Constructs a HiddenFormInput object with the specified control <i>name</i> and 
    *  initial input <i>value</i>.
    *  @param name The control name of the input field.
    *  @param value The initial value of the input field.
    **/
    public HiddenFormInput(String name, String value)
    {
        super(name, value);
    }

    /**
    *  Returns the tag for the hidden form input type.
    *  @return The tag.
    **/
    public String getTag()
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.INFORMATION, "Generating HiddenFormInput tag...");

        if (getName() == null)
        {
           Trace.log(Trace.ERROR, "Attempting to get tag before setting name.");
           throw new ExtendedIllegalStateException(
               "name", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        StringBuffer s = new StringBuffer("<input type=\"hidden\"");

        s.append(getNameAttributeTag());
        s.append(getValueAttributeTag(false));
        s.append(getSizeAttributeTag());
        s.append(getLanguageAttributeTag());                                     //$B1A
        s.append(getDirectionAttributeTag());                                    //$B1A
        s.append(getAttributeString());                                          // @Z1A
        s.append(" />");

        return s.toString();
    }
}
