///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SubmitFormInput.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.Trace;

/**
*  The SubmitFormInput class represents a submit button input type in an HTML form.
*  The trailing slash &quot;/&quot; on the SubmitFormInput tag allows it to conform to
*  the XHTML specification.
*  <P>
*  Here is an example of a SubmitFormInput tag:<br>
*  &lt;input type=&quot;submit&quot; value=&quot;Send&quot; /&gt;
**/
public class SubmitFormInput extends FormInput
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


    /**
    *  Constructs a default SubmitFormInput object.
    **/
    public SubmitFormInput()
    {
        super();

    }

    /**
    *  Constructs a SubmitFormInput object with the specified control <i>name</i>.
    *  @param name The control name of the input field.
    **/
    public SubmitFormInput(String name)
    {
        super(name);
    }

    /**
    *  Constructs a SubmitFormInput object with the specified control <i>name</i> and 
    *  initial input <i>value</i>.
    *  @param name The control name of the input field.
    *  @param value The input value used when the field is submitted.
    **/
    public SubmitFormInput(String name, String value)
    {
        super(name, value);
    }

    /**
    *  Returns the tag for the submit form input type.
    *  @return The tag.
    **/
    public String getTag()
    {
        //@C1D

        StringBuffer s = new StringBuffer("<input type=\"submit\"");

        s.append(getNameAttributeTag());
        s.append(getValueAttributeTag(false));
        s.append(getSizeAttributeTag());
        s.append(getLanguageAttributeTag());                                  //$B1A
        s.append(getDirectionAttributeTag());                                 //$B1A
        s.append(getAttributeString());                                       // @Z1A
        s.append(" />");

        return s.toString();
    }
}
