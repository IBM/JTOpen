////////////////////////////////////////////////////////////////////////
// IBM Confidential
//
// OCO Source Materials
//
// The Source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office
//
// 5769-SS1
// (C) Copyright IBM Corp. 1999,1999
////////////////////////////////////////////////////////////////////////
// File Name:    ResourceBundleLoader_h.java
//
// Classes:      ResourceBundleLoader_h                                                                                                                                                                        

////////////////////////////////////////////////////////////////////////
// CHANGE ACTIVITY:
//   $A0=PTR/DCR   Release   Date            Userid    Comments
//           D98585      V5R2      07/30/2001  wiedrich  Created
// 
// END CHANGE ACTIVITY
////////////////////////////////////////////////////////////////////////
package com.ibm.as400.micro;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
   A class representing the ResourceBundleLoader_m object which
   is used to load the resource bundle.
**/
class ResourceBundleLoader_m
{
   private static MissingResourceException resourceException_;

   private static ResourceBundle resources_;



   static
   { 
     try
     {
        resources_ = ResourceBundle.getBundle("com.ibm.as400.access.MRI2");
     }
     catch (MissingResourceException e)
     {
       // Save the exception and rethrow it later.  This is because exceptions
       // thrown from static initializers are hard to debug.
       resourceException_ = e;
     }
   
    }


  /**
   *  Returns the text associated with the exception.
   *  
   *  @param textId  the id  which identifies the message text to return.
   *  @return the translatable text which describes the exception. 
   **/
  static final String getText (String textId)
  {
    if (resources_ == null)
       throw resourceException_;
    return resources_.getString(textId);
  }  

  /**
   *  Returns the text associated with an MRI key, with subsitution variables.
   *  
   *  @param  textId  the id which identifies the message text to return.
   *  @param  value  The replacement value.
   *  @return  The text string with the substitution variable replaced.
   **/
    static String getText (String textId, Object value)
    {
        String text = getText (textId);
        return substitute (text, new Object[] { value });
    }


    /**
     *  Returns the text associated with an MRI key, with subsitution variables.
     *
     *  @param  textId  the id which identifies the message text to return.
     *  @param  value0  The first replacement value.
     *  @param  value1  The second replacement value.
     *  @return  The text string with the substitution variable replaced.
     **/
    static String getText (String textId, Object value0, Object value1)
    {
        String text = getText (textId);
        return substitute (text, new Object[] { value0, value1 });
    }


    /**
     *  Replaces substitution variables in a string.
     *  
     *  @param  text  The text string, with substitution variables (e.g. "Error &0 in table &1.")
     *  @param  values  The replacement values.
     *  @return  The text string with all substitution variables replaced.
     **/
    static String substitute (String text, Object[] values)
    {
        String result = text;
        for (int i = 0; i < values.length; ++i) {
            String variable = "&" + i;
            int j = result.indexOf (variable);
            if (j >= 0) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(result.substring(0, j));
                buffer.append(values[i].toString ());
                buffer.append(result.substring(j + variable.length ()));
                result = buffer.toString ();
            }
        }
        return result;
    }
}
