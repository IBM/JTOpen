///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ButtonFormInput.java
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
*  The ButtonFormInput class represents a button input type in an HTML form.
*  The trailing slash &quot;/&quot; on the ButtonFormInput tag allows it to conform to
*  the XHTML specification.
*  <P>
*  
*  Here is an example of a ButtonFormInput tag calling a javascript defined within a HTML page:<br>
*  &lt;input type=&quot;button&quot; name=&quot;button1&quot; value=&quot;Press Me&quot; onclick=&quot;test()&quot; /&gt;
*
*  <p>
*  Here is a sample javascript which displays an <i>alert</i> box with the specified message:<br>
*  <PRE>
*  &lt;head&gt;
*  &lt;script language=&quot;javascript&quot;&gt;
*     function test()
*     {
*        alert(&quot;This is a sample script executed with a ButtonFormInput.&quot;)
*     }
*  &lt;/script&gt;
*  &lt;/head&gt;
*  </PRE> 
* 
*  <p>ButtonFormInput objects generate the following events:
*  <ul>
*  <li>PropertyChangeEvent
*  <li>VetoableChangeEvent
*  </ul>
*
**/
public class ButtonFormInput extends FormInput
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    private String action_;             // The action the button will perform when pressed.

    /**
    *  Constructs a default ButtonFormInput object.
    **/
    public ButtonFormInput()
    {
        super();

    }

    /**
    *  Constructs a ButtonFormInput object with the specified control <i>name</i>.
    *  @param name The control name of the input field.
    **/
    public ButtonFormInput(String name)
    {
        super(name);
    }

    /**
    *  Constructs a ButtonFormInput object with the specified control <i>name</i> and 
    *  viewable text <i>value</i> of the button.
    *  @param name The control name of the input field.
    *  @param value The viewable text value of the button.
    **/
    public ButtonFormInput(String name, String value)
    {
        super(name, value);
    }

    /**
     *  Constructs a ButtonFormInput object with the specified control <i>name</i>, 
     *  viewable text <i>value</i> of the button, and the <i>action</i> to perform
     *  when the button is pressed.
     *  @param name The control name of the input field.
     *  @param value The viewable text value of the button.
     *  @param action The script to execute.
     **/
    public ButtonFormInput(String name, String value, String action)                  //$A1A
    {
        super(name, value);
        try
        {
            setAction(action);
        }
        catch (PropertyVetoException e)
        {
        }
    }


    /**
     *  Returns the action being performed by the button.
     *  @return The script being executed.
     **/
    public String getAction()                                      //$A1A
    {
        return action_;
    }


    /**
     *  Returns the tag for the button form input type.
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

        StringBuffer s = new StringBuffer("<input type=\"button\"");

        s.append(getNameAttributeTag());
        s.append(getValueAttributeTag(false));
        s.append(getSizeAttributeTag());
        s.append(getLanguageAttributeTag());                                          //$B1A
        s.append(getDirectionAttributeTag());                                         //$B1A
        s.append(getAttributeString());                                               // @Z1A

        if (getAction() == null)                                                      //$A1A
        {
            //$A1A
            Trace.log(Trace.ERROR, "Attempting to get tag before setting action.");    //$A1A
            throw new ExtendedIllegalStateException(                                   //$A1A
                                                                                       "action", ExtendedIllegalStateException.PROPERTY_NOT_SET);    //$A1A
        }                                                                             //$A1A
        else                                                                          //$A1A
        {
            //$A1A
            s.append(" onclick=\"");                                                   //$A1A
            s.append(action_);                                                         //$A1A
            s.append("\"");                                                            //$A1A
        }                                                                             //$A1A

        s.append(" />");

        return s.toString();
    }


    /**
     *  Sets the action to perform when the button is clicked.  Buttons have no default behavior. 
     *  Each button may have client-side scripts associated with the element's event attributes. 
     *  When an event occurs (the user presses the button), the associated script is triggered.  
     *  @param action The script to execute.
     *
     *  @exception PropertyVetoException If a change is vetoed.
     **/
    public void setAction(String action)                                 //$A1A
    throws PropertyVetoException
    {
        if (action == null)
            throw new NullPointerException("action");

        if (action.length() == 0)
            throw new ExtendedIllegalArgumentException("action", 
                                                       ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        String old = action_;

        if (vetos_ != null) vetos_.fireVetoableChange("action", old, action); //@CRS

        action_ = action;

        if (changes_ != null) changes_.firePropertyChange("action", old, action); //@CRS

    }
}

