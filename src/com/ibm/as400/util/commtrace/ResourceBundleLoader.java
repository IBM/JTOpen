///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ResourceBundleLoader.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


// A class representing the ResourceBundleLoader object which is used to load the resource bundle.
class ResourceBundleLoader 
{
  private static final String copyright = "Copyright (C) 2002 International Business Machines Corporation and others.";


    private static MissingResourceException resourceException_;  // Set if there is an exception during the loading of the resource bundle
    private static ResourceBundle coreResources_;  // Core toolbox resources @B2A

    static
    { 
        try {                                                                               
            coreResources_ = ResourceBundle.getBundle("com.ibm.as400.util.commtrace.CTMRI");   
        }                                                                                   
        catch(MissingResourceException e) {                                                 
            resourceException_ = e;                                                         
        }                                                                                   
    }

    // No need to create instances of this class, all methods are static
    private ResourceBundleLoader()
    {
    }

    // Returns the text associated with the exception.
    // @param  textId  the id which identifies the message text to return.
    // @return  the translatable text which describes the exception.
    static final String getText(String textId)
    {
        if (coreResources_ == null) {
            throw resourceException_;
        }

        try {                                                                  
            return coreResources_.getString(textId);
        }                                                                       
        catch (MissingResourceException e) {                                    
           throw e;
        }                                                                       
    }
}

