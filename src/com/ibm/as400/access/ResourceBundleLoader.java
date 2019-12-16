///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceBundleLoader.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.io.File;
import java.net.URL;


// A class representing the ResourceBundleLoader object which is used to load the resource bundle.
class ResourceBundleLoader 
{
    private static final boolean DEBUG = false;

    private static MissingResourceException resourceException_;  // Set if there is an exception during the loading of the resource bundle
    private static ResourceBundle coreResources_;  // Core toolbox resources @B2A
    private static ResourceBundle resources_;  // Base toolbox resources needed in proxy jar file @B1C
    private static ResourceBundle resources2_;  // Base toolbox resources NOT needed in proxy jar file @B1A
    private static ResourceBundle systemValueResource_;  // resource bundle for system value.
    private static String           genericDescription_         = null;

    static
    { 
        try {                                                                               // @B2A
            coreResources_ = ResourceBundle.getBundle("com.ibm.as400.access.CoreMRI");      // @B2A
        }                                                                                   // @B2A
        catch(MissingResourceException e) {                                                 // @B2A
            resourceException_ = e;                                                         // @B2A
        }                                                                                   // @B2A
                                                                                         
        try {
            resources_ = ResourceBundle.getBundle("com.ibm.as400.access.MRI");
            systemValueResource_ = ResourceBundle.getBundle("com.ibm.as400.access.SVMRI");
        }
        catch (MissingResourceException e) {
            // Save the exception and rethrow it later.  This is because exceptions thrown from static initializers are hard to debug.
            resourceException_ = e;
        }

        try {                                                                               // @B1A
            resources2_ = ResourceBundle.getBundle ("com.ibm.as400.access.MRI2");           // @B1A
        }                                                                                   // @B1A
        catch (MissingResourceException e) {                                                // @B1A
            // This resource bundle may not be found if we are running with only            // @B1A
            // the proxy jar file.  Do not flag an exception.                               // @B1A
            if (Trace.isTraceOn ())                                                         // @B1A
                Trace.log (Trace.INFORMATION, "MRI2 not found.  This is expected behavior when using the proxy jar file."); // @B1A
        }                                                                                   // @B1A

    }

    // No need to create instances of this class, all methods are static
    private ResourceBundleLoader()
    {
    }

    // Returns the text associated with the exception.                          // @B2A
    // @param  textId  the id which identifies the message text to return.      // @B2A
    // @return  the translatable text which describes the exception.            // @B2A
    static final String getCoreText(String textId)                              // @B2A
    {                                                                           // @B2A
        if (coreResources_ == null)                                             // @B2A
            throw resourceException_;                                           // @B2A
        return coreResources_.getString(textId);                                // @B2A
    }                                                                           // @B2A



    /**
     Returns an icon.

     @param  fileName    The icon file name.
     @return             The icon.
     **/
    static final Icon getIcon (String fileName)
    {
      return getIcon (fileName, null);
    }



    /**
     Returns an icon.

     @param  fileName    The icon file name.
     @param  description The icon description.
     @return             The icon.
     **/
    static final Icon getIcon (String fileName, String description)
    {
      Icon icon = null;

      if (DEBUG)
        System.out.println ("ResourceBundleLoader: Loading icon " + fileName + ".");

      try {

        // The generic description is for loading icons.  Aparantly, the
        // description is used in cases like presenting an icon to blind
        // users, etc.  In some cases, we just don't have a description,
        // so we will put up a canned description.
        if (description == null) {
          if (genericDescription_ == null)
            genericDescription_ = getText ("PRODUCT_TITLE");  // TBD: copy MRI from VMRI to MRI2
          description = genericDescription_;
        }

        URL url = ResourceBundleLoader.class.getResource (fileName);
        if (url == null)
        {
          fileName = "com" + File.separator + "ibm" + File.separator +
            "as400" + File.separator + "access" + File.separator +
            fileName;
          icon = new ImageIcon (fileName, description);
        }
        else
        {
          icon = new ImageIcon (url, description);
        }
      }
      catch (Exception e) {
        if (DEBUG)
          System.out.println ("ResourceBundleLoader: Error: " + e + ".");

        Trace.log (Trace.ERROR, "Icon " + fileName + " not loaded: " + e.getMessage() + ".");
      }

      return icon;
    }

    // Returns the system value MRI text.
    // @param  textId  the id which identifies the text to return.
    // @return  the translatable system value MRI text.
    static final String getSystemValueText(String textId)
    {
        if (systemValueResource_ == null) {
            throw resourceException_;
        }
        return systemValueResource_.getString(textId).trim();
    }

    // Returns the system value MRI text for the specified Locale.
    // @param  textId  the id which identifies the text to return.
    // @param locale The locale to use.
    // @return  the translatable system value MRI text.
    static final String getSystemValueText(String textId, Locale locale)
    {
        ResourceBundle bundle = ResourceBundle.getBundle("com.ibm.as400.access.SVMRI", locale);
        if (bundle != null)
        {
          return bundle.getString(textId);
        }
        if (systemValueResource_ == null) {
            throw resourceException_;
        }
        return systemValueResource_.getString(textId).trim();
    }

    // Returns the text associated with the exception.
    // @param  textId  the id which identifies the message text to return.
    // @return  the translatable text which describes the exception.
    static final String getText(String textId)
    {
        if (resources_ == null) {
            throw resourceException_;
        }

        try {                                                                   // @B1A
            return resources_.getString(textId);
        }                                                                       // @B1A
        catch (MissingResourceException e) {                                    // @B1A

            try {                                                               // @B2A
                return getCoreText(textId);                                     // @B2A
            }                                                                   // @B2A
            catch(MissingResourceException e2) {                                // @B2A
                if (resources2_ == null)                                        // @B1A
                    throw e;                                                    // @B1A
                else                                                            // @B1A
                    return resources2_.getString (textId);                      // @B1A
            }                                                                   // @B2A
        }                                                                       // @B1A
    }

    // @B0A
    // Returns the text associated with an MRI key, with subsitution variables.
    // @param  textId  the id which identifies the message text to return.
    // @param  value  The replacement value.
    // @return  The text string with the substitution variable replaced.
    static String getText (String textId, Object value)
    {
        String text = getText (textId);
        return substitute (text, value);
    }

    // @B0A
    // Returns the text associated with an MRI key, with subsitution variables.
    // @param  textId  the id which identifies the message text to return.
    // @param  value0  The first replacement value.
    // @param  value1  The second replacement value.
    // @return  The text string with the substitution variable replaced.
    static String getText (String textId, Object value0, Object value1)
    {
        String text = getText (textId);
        return substitute (text, new Object[] { value0, value1 });
    }

    // @B0A
    // Returns the text associated with an MRI key, with subsitution variables.
    // @param  textId  the id which identifies the message text to return.
    // @param  values  The replacement values.
    // @return  The text string with all substitution variables replaced.
    static String getText (String textId, Object[] values)
    {
        String text = getText (textId);
        return substitute (text, values);
    }

    // Replaces a single substitution variable in a string.
    // @param  text  The text string, with a single substitution variable (e.g. "Error &0 has occurred.")
    // @param  value  The replacement value.
    // @return  The text string with the substitution variable replaced.
    static String substitute(String text, Object value)
    {
        return substitute(text, new Object[] { value});
    }

    // @B0A
    // Replaces a single substitution variable in a string.
    // @param  text  The text string, with a single substitution variable (e.g. "Error &0 has occurred.")
    // @param  value0  The first replacement value.
    // @param  value1  The second replacement value.
    // @return  The text string with the substitution variable replaced.
    static String substitute(String text, Object value0, Object value1)
    {
        return substitute(text, new Object[] { value0, value1 });
    }

    // Replaces substitution variables in a string.
    // @param  text  The text string, with substitution variables (e.g. "Error &0 in table &1.")
    // @param  values  The replacement values.
    // @return  The text string with all substitution variables replaced.
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
