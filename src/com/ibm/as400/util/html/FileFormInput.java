///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: FileFormInput.java
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
*  The FileFormInput class represents a file input type in an HTML form.
*  The trailing slash &quot;/&quot; on the FileFormInput tag allows it to 
*  conform to the XHTML specification.
*  <P>
*  Here is an example of a FileFormInput tag:<br>
*  &lt;input type=&quot;file&quot; name=&quot;myFile&quot; /&gt;
**/
public class FileFormInput extends FormInput
{   
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    /**
    *  Constructs a default FileFormInput object.
    **/
    public FileFormInput()
    {
        super();

    }

    /**
    *  Constructs a FileFormInput object with the specified control <i>name</i>.
    *  @param name The control name of the input field.
    **/
    public FileFormInput(String name)
    {
        super(name);
    }

    /**
    *  Returns a comment tag.
    *  This method should not be called.  There is no XSL-FO support for this class.
    *  @return The comment tag.
    **/
    public String getFOTag()                                                //@C1A
    {
        Trace.log(Trace.ERROR, "Attempting to getFOTag() for an object that doesn't support it.");
        return "<!-- A FileFormInput was here -->";
    }

    /**
    *  Returns the tag for the file form input type.
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

        StringBuffer s = new StringBuffer("<input type=\"file\"");

        s.append(getNameAttributeTag());
        s.append(getValueAttributeTag(true));
        s.append(getSizeAttributeTag());
        s.append(getLanguageAttributeTag());                                      //$B1A
        s.append(getDirectionAttributeTag());                                     //$B1A
        s.append(getAttributeString());                                           // @Z1A
        s.append(" />");

        return s.toString();
    }
}
