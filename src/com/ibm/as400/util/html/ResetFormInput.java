///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ResetFormInput.java
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
*  The ResetFormInput class represents a reset button input type in an HTML form.
*  The trailing slash &quot;/&quot; on the ResetFormInput tag allows it to conform to
*  the XHTML specification.
*  <P>
*  Here is an example of a ResetFormInput tag:<br>
*  &lt;input type=&quot;reset&quot; value=&quot;Reset&quot; /&gt;
**/
public class ResetFormInput extends FormInput
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";
  static final long serialVersionUID = 2691238990019905630L;


    /**
    *  Constructs a default ResetFormInput object.
    **/
    public ResetFormInput()
    {
        super();

    }

    /**
    *  Constructs a ResetFormInput object with the specified control <i>name</i>.
    *  @param name The control name of the input field.
    **/
    public ResetFormInput(String name)
    {
        super(name);
    }

    /**
    *  Constructs a ResetFormInput object with the specified control <i>name</i> and 
    *  initial input <i>value</i>.
    *  @param name The control name of the input field.
    *  @param value The input value used when the field is submitted.
    **/
    public ResetFormInput(String name, String value)
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
        return "<!-- A ResetFormInput was here -->";
    }

    /**
    *  Returns the tag for the reset form input type.
    *  @return The tag.
    **/
    public String getTag()
    {
        //@C1D

        StringBuffer s = new StringBuffer("<input type=\"reset\"");

        s.append(getNameAttributeTag());
        s.append(getValueAttributeTag(false));
        s.append(getSizeAttributeTag());
        s.append(getLanguageAttributeTag());                               //$B1A
        s.append(getDirectionAttributeTag());                              //$B1A
        s.append(getAttributeString());                                    // @Z1A
        s.append(" />");

        return s.toString();
    }
}
