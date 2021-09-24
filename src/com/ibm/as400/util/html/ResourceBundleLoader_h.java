///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceBundleLoader_h.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
   A class representing the ResourceBundleLoader_h object which
   is used to load the resource bundle.
**/
class ResourceBundleLoader_h
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   private static MissingResourceException resourceException_;

   private static ResourceBundle resources_;



   static
   { 
     try
     {
        resources_ = ResourceBundle.getBundle("com.ibm.as400.util.html.HMRI");
     }
     catch (MissingResourceException e)
     {
       // Save the exception and rethrow it later.  This is because exceptions
       // thrown from static initializers are hard to debug.
       resourceException_ = e;
     }
   
    }


  /**
      Returns the text associated with the exception.
      @param textId  the id  which identifies the message text to return.
      @return the translatable text which describes the exception. 
  **/
  static final String getText (String textId)
  {
    if (resources_ == null)
       throw resourceException_;
    return resources_.getString(textId);
  }  


}
