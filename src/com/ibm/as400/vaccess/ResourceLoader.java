///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceLoader.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.io.File;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;



/**
The ResourceLoader class is used to load the MRI resources.
**/
class ResourceLoader
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Debug flag.  This can be helpful in debugging
    // ExceptionInInitializerError exceptions, which are ususally
    // caused by a resource not being found.  Set this to
    // true and recompile.
    private static final boolean DEBUG_ = false;



    // Private data.
    private static String           genericDescription_         = null;
    private static ResourceBundle   resources_                  = null;
    private static ResourceBundle   printResources_             = null;
    private static ResourceBundle   queryResources_             = null;



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

        if (DEBUG_)
            System.out.println ("ResourceLoader: Loading icon " + fileName + ".");

        try {

            // The generic description is for loading icons.  Aparantly, the
            // description is used in cases like presenting an icon to blind
            // users, etc.  In some cases, we just don't have a description,
            // so we will put up a canned description.
            if (description == null) {
                if (genericDescription_ == null)
                    genericDescription_ = getText ("PRODUCT_TITLE");
                description = genericDescription_;
            }

            URL url = ResourceLoader.class.getResource (fileName);
            if (url == null)
            {
                fileName = "com" + File.separator + "ibm" + File.separator +
                    "as400" + File.separator + "vaccess" + File.separator +
                    fileName;
                icon = new ImageIcon (fileName, description);
            }
            else
            {
                icon = new ImageIcon (url, description);
            }
        }
        catch (Exception e) {
            if (DEBUG_)
                System.out.println ("ResourceLoader: Error: " + e + ".");

            Trace.log (Trace.ERROR, "Icon " + fileName + " not loaded: " + e.getMessage() + ".");
        }

        return icon;
    }



/**
Returns a resource string.

@param key      The key in the MRI resource bundle.
@return         The translatable text.
**/
    static final String getPrintText (String key)
    {
        if (DEBUG_)
            System.out.println ("ResourceLoader: Loading text " + key + ".");

        try {
            // Load the resource bundle the first time.
            if (printResources_ == null)
                printResources_ = ResourceBundle.getBundle ("com.ibm.as400.vaccess.VNPMRI");

            // Load the resource.
            return printResources_.getString (key);
        }
        catch (MissingResourceException e) {
            if (DEBUG_)
                System.out.println ("ResourceLoader: Error: " + e + ".");

            Trace.log (Trace.ERROR, "Error while loading print MRI resource: " + key + ".");
            throw e;
        }
    }



/**
Returns a resource string from the VQRYMRI properties file.

@param key      The key in the MRI resource bundle.
@return         The translatable text.
**/
    static final String getQueryText (String key)
    {
        if (DEBUG_)
            System.out.println ("ResourceLoader: Loading text " + key + ".");

        try {
            // Load the resource bundle the first time.
            if (queryResources_ == null)
                queryResources_ = ResourceBundle.getBundle ("com.ibm.as400.vaccess.VQRYMRI");

            // Load the resource.
            return queryResources_.getString (key);
        }
        catch (MissingResourceException e) {
            if (DEBUG_)
                System.out.println ("ResourceLoader: Error: " + e + ".");

            Trace.log (Trace.ERROR, "Error while loading query MRI resource: " + key + ".");
            throw e;
        }
    }



/**
Returns a resource string.

@param key      The key in the MRI resource bundle.
@return         The translatable text.
**/
    static final String getText (String key)
    {
        if (DEBUG_)
            System.out.println ("ResourceLoader: Loading text " + key + ".");

        try {
            // Load the resource bundle the first time.
            if (resources_ == null)
                resources_ = ResourceBundle.getBundle ("com.ibm.as400.vaccess.VMRI");

            // Load the resource.
            return resources_.getString (key);
        }
        catch (MissingResourceException e) {
            if (DEBUG_)
                System.out.println ("ResourceLoader: Error: " + e + ".");

            Trace.log (Trace.ERROR, "Error while loading MRI resource: " + key + ".");
            throw e;
        }
    }



// @A1A
/**
Replaces a single substitution variable in a string.

@param text     The text string, with a single substitution variable
                (e.g. "Error &0 has occurred.");
@param value    The replacement value.
@return         The text string with the substitution variable
                replaced.
**/
    static String substitute (String text, String value)
    {
        return substitute (text, new String[] { value });
    }



// @A1A
/**
Replaces substitution variables in a string.

@param text     The text string, with substitution variables
                (e.g. "Error &0 in table &1.");
@param values   The replacement values.
@return         The text string with all substitution variables
                replaced.
**/
    static String substitute (String text, String[] values)
    {
        String result = text;
        for (int i = 0; i < values.length; ++i) {
            String variable = "&" + i;
            int j = result.indexOf (variable);
            if (j >= 0) {
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

