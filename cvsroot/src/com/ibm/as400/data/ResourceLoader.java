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

package com.ibm.as400.data;

import com.ibm.as400.access.Trace;                  // @A1A
import java.text.MessageFormat;
import java.util.*;

/**
 * A utility class which wrappers <code>ResourceBundle</code>
 * operations and handles exceptions gracefully.
 * For example, if a string resource
 * cannot be accessed, <code>ResourceLoader</code> returns
 * the string "RESOURCEMISSING", allowing the developer to
 * quickly detect the missing resource and correct the problem.
 */
class ResourceLoader extends Object
{
    private ResourceBundle m_bundle = null;

    /**
     * Sets the resource bundle name.
     *
     * <code>setResourceName</code> attempts to load the specified
     * resource bundle.  If an error occurs, an exception is logged
     * and appropriate action is taken on subsequent requests for
     * specific resources.
     *
     * @param name the name of the resource bundle to be loaded.
     * If the resource bundle could not be found, <code>getString</code>
     * will return the string "RESOURCE BUNDLE ERROR".
     */
    public void setResourceName(String name)
    {
        if (m_bundle == null)
        {
            try 
            { 
            	m_bundle = ResourceBundle.getBundle(name); 
            }
            catch (MissingResourceException e) 
            {
                Trace.log(Trace.ERROR, e);                  // @A1C
            }
        }
    }

    /**
     * Returns a locale-dependent string.
     *
     * <code>getString</code> looks for the string resource identified
     * by the specified key in the resource bundle specified on <code>setResourceName</code>.
     * If the string could not be found, <code>getString</code>
     * returns "RESOURCEMISSING".  If the resource bundle could not
     * be loaded, <code>getString</code> returns "RESOURCEBUNDLEERROR".
     *
     * @param key  the key which identifies the string to be loaded.
     */
    public String getString(String key)
    {
        if (m_bundle != null)
        {
            try
            {
                return m_bundle.getString(key);
            }
            catch (MissingResourceException e)
            {
                Trace.log(Trace.ERROR, e);                  // @A1C
                try 
                {
                    return MessageFormat.format(m_bundle.getString(DAMRI.MISSING_KEY), new Object[] {key});
                }
                catch (Exception eAgain) 
                {
                    return "RESOURCE BUNDLE ERROR.";
                }
            }
        }
        else 
            return "RESOURCE BUNDLE ERROR.";
    }

    /**
     * Returns a locale-dependent string.
     *
     * <code>getString</code> looks for the string resource identified
     * by the specified key in the resource bundle specified on <code>setResourceName</code>.
     * If the string could not be found, <code>getString</code>
     * returns "RESOURCEMISSING".  If the resource bundle could not
     * be loaded, <code>getString</code> returns "RESOURCEBUNDLEERROR".
     *
     * @param key  the key which identifies the string to be loaded.
     */
    public String getStringWithNoSubstitute(String key)
    {
        if (m_bundle != null)
        {
            try
            {
                return m_bundle.getString(key);
            }
            catch (MissingResourceException e)
            {
                return null;
            }
        }
        else 
            return null;
    }
}
