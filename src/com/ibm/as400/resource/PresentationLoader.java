///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PresentationLoader.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.Trace;
import java.awt.Image;
import java.beans.SimpleBeanInfo;
import java.io.Serializable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;



/**
The PresentationLoader class is a convenience class for creating
{@link com.ibm.as400.resource.Presentation Presentation}
objects using a resource bundle.
@deprecated Use packages <tt>com.ibm.as400.access</tt> and <tt>com.ibm.as400.access.list</tt> instead. 
**/
public class PresentationLoader
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;



    // Private data.
    private static final String DESCRIPTION_SUFFIX  = "_DESCRIPTION";
    private static final String FULLNAME_SUFFIX     = "_FULLNAME";
    private static final String HELP_SUFFIX         = "_HELP";
    private static final String NAME_SUFFIX         = "_NAME";

    private static final String ICON_16_SUFFIX      = "16.gif";
    private static final String ICON_32_SUFFIX      = "32.gif";

    private static IconLoader_ iconLoader_;

    private ResourceBundle  resourceBundle_;



/**
Constructs a PresentationLoader object.

@param resourceBundleBaseName The resource bundle base name.
**/
    public PresentationLoader(String resourceBundleBaseName)
    {
        if (resourceBundleBaseName == null)
            throw new NullPointerException("resourceBundleBaseName");

        resourceBundle_ = ResourceBundle.getBundle(resourceBundleBaseName);
    }



/**
Constructs a PresentationLoader object.

@param resourceBundle The resource bundle.
*/
    public PresentationLoader(ResourceBundle resourceBundle)
    {
        if (resourceBundle == null)
            throw new NullPointerException("resourceBundle");

        resourceBundle_ = resourceBundle;
    }



/**
Returns a Presentation object with text loaded from the resource bundle.
The keys in the resource bundle correspond to the key base name as
follows:

<p><table border>
<tr>
   <td>name</td>
   <td><em>keyBaseName</em><code>_NAME</code></td>
</tr>
<tr>
   <td>full name</td>
   <td><em>keyBaseName</em><code>_FULLNAME</code></td>
</tr>
<tr>
   <td>description</td>
   <td><em>keyBaseName</em><code>_DESCRIPTION</code></td>
</tr>
<tr>
   <td>help</td>
   <td><em>keyBaseName</em><code>_HELP</code></td>
</tr>
</table>

<p>All keys are optional.

@param keyBaseName  The key base name.
@return             The presentation.
**/
    public Presentation getPresentation(String keyBaseName)
    {
        if (keyBaseName == null)
            throw new NullPointerException("keyBaseName");

        return newPresentation(keyBaseName, null);
    }



/**
Returns a Presentation object with text loaded from the resource bundle.
The keys in the resource bundle correspond to the key base name as
follows:

<p><table border>
<tr>
   <td>name</td>
   <td><em>keyBaseName</em><code>_NAME</code></td>
</tr>
<tr>
   <td>full name</td>
   <td><em>keyBaseName</em><code>_FULLNAME</code></td>
</tr>
<tr>
   <td>description</td>
   <td><em>keyBaseName</em><code>_DESCRIPTION</code></td>
</tr>
<tr>
   <td>help</td>
   <td><em>keyBaseName</em><code>_HELP</code></td>
</tr>
</table>

<p>Two icons are loaded, <em>iconFileName</em>16.gif and
<em>iconFileName</em>32.gif.

<p>All keys are optional.

@param keyBaseName  The key base name.
@param iconFileName The icon file name.
@return             The presentation.
**/
    public Presentation getPresentationWithIcon(String keyBaseName, String iconFileName)
    {
        if (keyBaseName == null)
            throw new NullPointerException("keyBaseName");
        if (iconFileName == null)
            throw new NullPointerException("iconFileName");

        return newPresentation(keyBaseName, iconFileName);
    }



/**
Returns a Presentation object with text loaded from the resource bundle.
The keys in the resource bundle correspond to the key base name and
key suffix as follows:

<p><table border>
<tr>
   <td>name</td>
   <td><em>keyBaseName</em>_<em>keySuffix</em><code>_NAME</code></td>
</tr>
<tr>
   <td>full name</td>
   <td><em>keyBaseName</em>_<em>keySuffix</em><code>_FULLNAME</code></td>
</tr>
<tr>
   <td>description</td>
   <td><em>keyBaseName</em>_<em>keySuffix</em><code>_DESCRIPTION</code></td>
</tr>
<tr>
   <td>help</td>
   <td><em>keyBaseName</em>_<em>keySuffix</em><code>_HELP</code></td>
</tr>
</table>

<p>All keys are optional.

@param keyBaseName  The key base name.
@param keySuffix    The key suffix.
@return             The presentation.
**/
    public Presentation getPresentation(String keyBaseName, String keySuffix)
    {
        if (keyBaseName == null)
            throw new NullPointerException("keyBaseName");
        if (keySuffix == null)
            throw new NullPointerException("keySuffix");

        return newPresentation(newKeyBaseName(keyBaseName, keySuffix), null);
    }



/**
Returns a Presentation object with text loaded from the resource bundle.
The keys in the resource bundle correspond to the key base name and
key suffix as follows:

<p><table border>
<tr>
   <td>name</td>
   <td><em>keyBaseName</em>_<em>keySuffix</em><code>_NAME</code></td>
</tr>
<tr>
   <td>full name</td>
   <td><em>keyBaseName</em>_<em>keySuffix</em><code>_FULLNAME</code></td>
</tr>
<tr>
   <td>description</td>
   <td><em>keyBaseName</em>_<em>keySuffix</em><code>_DESCRIPTION</code></td>
</tr>
<tr>
   <td>help</td>
   <td><em>keyBaseName</em>_<em>keySuffix</em><code>_HELP</code></td>
</tr>
</table>

<p>Two icons are loaded, <em>iconFileName</em>16.gif and
<em>iconFileName</em>32.gif.

<p>All keys are optional.

@param keyBaseName  The key base name.
@param keySuffix    The key suffix.
@param iconFileName The icon file name.
@return             The presentation.
**/
    public Presentation getPresentationWithIcon(String keyBaseName, String keySuffix, String iconFileName)
    {
        if (keyBaseName == null)
            throw new NullPointerException("keyBaseName");
        if (keySuffix == null)
            throw new NullPointerException("keySuffix");
        if (iconFileName == null)
            throw new NullPointerException("iconFileName");

        return newPresentation(newKeyBaseName(keyBaseName, keySuffix), iconFileName);
    }



/**
Returns MRI text.

@param  key     The key.
@return         The MRI text, or null if the key is not found in the resource bundle.
**/
    private String getString(String key)
    {
        try {
            return resourceBundle_.getString(key);
        }
        catch(MissingResourceException e) {
            return null;
        }
    }



/**
Loads an icon.

@param  iconFileName    The icon file name.
@return                 The icon, or null if unable.
**/
//
// Implementation note: For some reason (at least with JDK 1.2.2), this
//                      ends up leaving a non-daemon AWT thread around.
//                      I am not sure why and how to avoid it.
//
    static Image loadIcon(String iconFileName)
    {
        if (iconFileName == null)
            throw new NullPointerException("iconFileName");

        if (iconLoader_ == null)
            iconLoader_ = new IconLoader_();
        try {
            return iconLoader_.loadImage(iconFileName);
        }
        catch(Exception e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Unable to load icon: " + iconFileName, e);
            return null;
        }
    }



/**
Creates and returns a new key base name in the form:

<p><em>keyBaseName</em><code>_</code><em>keySuffix</em>

@param keyBaseName  The key base name.
@param keySuffix    The key suffix.
@return             The new key base name.
**/
    private static String newKeyBaseName(String keyBaseName, String keySuffix)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(keyBaseName);
        buffer.append('_');
        buffer.append(keySuffix);
        return buffer.toString();
    }


/**
Creates and returns a new presentation.

@param keyBaseName  The key base name.
@param iconBaseName The icon base name.
@return             The presentation.
**/
    private Presentation newPresentation(String keyBaseName, String iconBaseName)
    {
        String name         = getString(keyBaseName + NAME_SUFFIX);
        String fullName     = getString(keyBaseName + FULLNAME_SUFFIX);
        String description  = getString(keyBaseName + DESCRIPTION_SUFFIX);
        String help         = getString(keyBaseName + HELP_SUFFIX);

        // If neither a name, full name, nor description are found,
        // then throw an exception.  This is probably missing MRI.
        if ((name == null) && (fullName == null) && (description == null)) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Unable to load presentation for " + keyBaseName);
            throw new ExtendedIllegalArgumentException("keyBaseName", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        // Create the presentation object.
        Presentation presentation = new Presentation();
        if (name != null)
            presentation.setName(name);
        if (fullName != null)
            presentation.setFullName(fullName);
        if (description != null)
            presentation.setValue(Presentation.DESCRIPTION_TEXT, description);
        if (help != null)
            presentation.setValue(Presentation.HELP_TEXT, help);

        // We don't load the icon now.  Instead, we wait until the caller
        // asks for it.  This improves performance for sure.  But it also
        // solves problems like AWT not being around when running on a server
        // (since a server Java program will never probably ask for the
        // icon).
        if (iconBaseName != null)
            presentation.setColorIcons(iconBaseName + ICON_16_SUFFIX, iconBaseName + ICON_32_SUFFIX);

        return presentation;
    }



/**
Loads icons as needed.
**/
//
// Implementation note:
//
// SimpleBeanInfo was the only JDK 1.1 way I could figure out to easily load images.
// This class needs to be in our package in order to load icons from our package,
// which is why I subclassed SimpleBeanInfo instead of just instantiating a
// SimpleBeanInfo object directly.
//
    private static class IconLoader_ extends SimpleBeanInfo
    { }


}
