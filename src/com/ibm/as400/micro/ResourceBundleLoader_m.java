///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ResourceBundleLoader_m.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
   A class representing the ResourceBundleLoader_m object which
   is used to load the resource bundle.
**/
class ResourceBundleLoader_m
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

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
