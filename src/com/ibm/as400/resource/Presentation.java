///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: Presentation.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import java.io.Serializable;
import java.util.Hashtable;
                           


/**
The Presentation class represents presentation information that
describes an object.  This may include description text, help
text, and icons.  In addition, customized information can also
be stored.
**/
public class Presentation
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Private data.
    private static final String EMPTY_STRING_           = "";

    private String          fullName_                   = null;
    private Object          iconColor16_                = null;
    private String          iconColor16Name_            = null;
    private Object          iconColor32_                = null;
    private String          iconColor32Name_            = null;
    private String          name_                       = EMPTY_STRING_;
    private Hashtable       values_                     = new Hashtable();



/**
Constant indicating the presentation key for abbreviated name.  
**/
    public static final String NAME                     = "Name";



/**
Constant indicating the presentation key for full name.  
**/
    public static final String FULL_NAME                = "Full Name";



/**
Constant indicating the presentation key for description text.  If avaliable,
this represents a String value.
**/
    public static final String DESCRIPTION_TEXT         = "Description Text";



/**
Constant indicating the presentation key for help text.  If avaliable,
this represents a String value.
**/
    public static final String HELP_TEXT                = "Help Text";



/**
Constant indicating the presentation key for a 16x16 color icon.  If avaliable,
this represents a java.awt.Image value.
**/
    public static final String ICON_COLOR_16x16         = "16";



/**
Constant indicating the presentation key for a 32x32 color icon.  If avaliable,
this represents a java.awt.Image value.
**/
    public static final String ICON_COLOR_32x32         = "32";



/**
Constructs a Presentation object.
**/
    public Presentation()
    {
    }



/**
Constructs a Presentation object.

@param name     The abbreviated name.
**/
    public Presentation(String name)
    {
        if (name == null)
            throw new NullPointerException("name");

        name_               = name;
    }



/**
Constructs a Presentation object.

@param name         The abbreviated name.  
@param fullName     The full name.
**/
    public Presentation(String name, String fullName)
    {
        if (name == null)
            throw new NullPointerException("name");
        if (fullName == null)
            throw new NullPointerException("fullName");

        name_               = name;
        fullName_           = fullName;
    }



/**
Converts a byte array to a String hexadecimal representation
using 2 characters for each byte.  

@param bytes        The byte array.
@return             The String hexadecimal representation.
**/
    //
    // Implementation note:  I stuck this method in this
    // class for no particular reason other than it will
    // be used most commonly for providing a hex representation
    // of a byte array.
    //
    static String bytesToHex(byte[] bytes)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("0x");
        for(int i = 0; i < bytes.length; ++i) {
            int b = 0x000000ff & bytes[i];
            if (b < 16)
                buffer.append('0');
            buffer.append(Integer.toHexString(b));
        }
        return buffer.toString();
    }



/**
Returns the full name.  The length of the full name is usually 
greater than or equal to the abbreviated name.

@return The full name if a full name has been set,
        or the abbreviated name if a full name has not been set.
**/
    public String getFullName()
    {
        if (fullName_ != null)
            return fullName_;
        else
            return name_;
    }



/**
Returns the abbreviated name.  The length of the abbreviated name is usually 
less than or equal to the full name.

@return The abbreviated name.
**/
    public String getName()
    {
        return name_;
    }



/**
Returns a presentation value.

@param  key The presentation key.                               
@return The presentation value, or null if no value for the key is set.
**/
    public Object getValue(Object key)
    {
        if (key == null)
            throw new NullPointerException("key");
        
        // If the key is in the hashtable, just return its value.
        if (values_.containsKey(key))
            return values_.get(key);

        // If the key is name or full name, then return it directly.
        else if (key.equals(NAME))
            return getName();
        else if (key.equals(FULL_NAME))
            return getFullName();

        // If the key is for an icon, and the icon name is set, then
        // load the icon and store it in the hashtable.  We try to
        // put off loading the icon until its asked for.
        else if ((key.equals(ICON_COLOR_16x16)) && (iconColor16Name_ != null)) {
            iconColor16_ = PresentationLoader.loadIcon(iconColor16Name_);
            if (iconColor16_ != null)
                values_.put(ICON_COLOR_16x16, iconColor16_);
            return iconColor16_;
        }
        else if ((key.equals(ICON_COLOR_32x32)) && (iconColor32Name_ != null)) {
            iconColor32_ = PresentationLoader.loadIcon(iconColor32Name_);
            if (iconColor32_ != null)
                values_.put(ICON_COLOR_32x32, iconColor32_);
            return iconColor32_;
        }

        // Otherwise, return null.
        else 
            return null;
    }

    

/**
Sets the color icon names.  The icons themselves are not loaded
until the caller requests them.

@param iconColor16Name  The color 16x16 name.
@param iconColor32Name  The color 32x32 name.
**/    
    void setColorIcons(String iconColor16Name, String iconColor32Name)
    {
        if (iconColor16Name == null)
            throw new NullPointerException("iconColor16Name");
        if (iconColor32Name == null)
            throw new NullPointerException("iconColor32Name");

        iconColor16Name_ = iconColor16Name;
        iconColor32Name_ = iconColor32Name;
    }



/**
Sets the full name.  The length of the full name is usually 
greater than or equal to the abbreviated name.

@param fullName     The full name.
**/
    public void setFullName(String fullName)
    {
        if (fullName == null)
            throw new NullPointerException("fullName");

        fullName_ = fullName;
    }



/**
Sets the abbreviated name.  The length of the abbreviated name is usually 
less than or equal to the full name.

@param name     The abbreviated name.
**/
    public void setName(String name)
    {
        if (name == null)
            throw new NullPointerException("name");

        name_ = name;
    }



/**
Sets a presentation value.

@param key      The presentation key.
@param value    The presentation value.
**/
    public void setValue(Object key, Object value)
    {
        if (key == null)
            throw new NullPointerException("key");
        if (value == null)
            throw new NullPointerException("value");

        if (key.equals(NAME))
            name_ = value.toString();
        else if (equals(FULL_NAME))
            fullName_ = value.toString();
        else
            values_.put(key, value);
    }



/**
Returns the abbreviated name.

@return The abbreviated name.
**/
    public String toString()
    {
        return name_;
    }


}
