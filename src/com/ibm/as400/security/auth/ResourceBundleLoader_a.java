///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ResourceBundleLoader_a.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.security.auth;

import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
   A class representing the ResourceBundleLoader_a object which
   is used to load the resource bundle.
**/
class ResourceBundleLoader_a
{

   private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
   private static MissingResourceException resourceException_;

   private static ResourceBundle resources_;
   private static ResourceBundle accessResources_;
   private static ResourceBundle coreResources_;



   static
   { 
     try
     {
        resources_ = ResourceBundle.getBundle("com.ibm.as400.security.SecurityMRI");
        accessResources_ = ResourceBundle.getBundle("com.ibm.as400.access.MRI");
        coreResources_ = ResourceBundle.getBundle("com.ibm.as400.access.CoreMRI");
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

  /**
      Returns the text associated with the exception.
      @param textId  the id  which identifies the message text to return.
      @return the translatable text which describes the exception. 
  **/
  static final String getAccessText (String textId)
  {
    if (accessResources_ == null)
       throw resourceException_;
    return accessResources_.getString(textId);
  }

  /**
      Returns the text associated with the exception.
      @param textId  the id  which identifies the message text to return.
      @return the translatable text which describes the exception. 
  **/
  static final String getCoreText (String textId)
  {
    if (coreResources_ == null)
       throw resourceException_;
    return coreResources_.getString(textId);
  }

}

