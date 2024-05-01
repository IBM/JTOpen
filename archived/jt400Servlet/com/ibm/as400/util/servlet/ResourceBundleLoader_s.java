///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceBundleLoader_s.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

import com.ibm.as400.access.Copyright;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
*  A class representing the ResourceBundleLoader_s object which
*  is used to load the resource bundle.
**/
class ResourceBundleLoader_s
{
   private static MissingResourceException resourceException_;
   private static ResourceBundle resources_;

   static
   {      
      try
      {
         resources_ = ResourceBundle.getBundle("com.ibm.as400.util.servlet.SMRI");
      }
      catch (MissingResourceException e)
      {
         // Save the exception and rethrow it later.  This is because exceptions
         // thrown from static initializers are hard to debug.
         resourceException_ = e;
      }
   
   }

  /**
  *  Constructs a ResourceBundleLoader_s object which loads the 
  *  resourceBundle.
  **/
  ResourceBundleLoader_s()
  {
     super();
  }


  /**
  *  Returns the text associated with the exception.
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
   *  Replaces substitution variables in a string.
   *  
   *  @param text     The text string, with substitution variables
   *                   (e.g. "Error &0 in table &1.");
   *  @param values   The replacement values.
   *
   *  @return         The text string with all substitution variables replaced.
   **/
  static String substitute (String text, String[] values)        //$B0A
  {
     String result = text;
     for (int i = 0; i < values.length; ++i) 
     {
        String variable = "&" + i;
        int j = result.indexOf (variable);
        if (j >= 0) 
        {
           StringBuffer buffer = new StringBuffer ();
           buffer.append (result.substring (0, j));
           buffer.append (values[i]);
           buffer.append (result.substring (j + variable.length ()));
           result = buffer.toString ();
        }
     }
     return result;
  }
}  
