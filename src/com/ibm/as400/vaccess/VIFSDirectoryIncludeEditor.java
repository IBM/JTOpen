///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VIFSDirectoryIncludeEditor.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.beans.PropertyEditorSupport;



/**
The VIFSDirectoryIncludeEditor class provides the list of choices
for the include property.
**/
class VIFSDirectoryIncludeEditor
extends PropertyEditorSupport
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Static  data.
    private static final int      default_          = 2;

    private static final String[] initialization_   = { "VIFSDirectory.INCLUDE_FILES",
                                                        "VIFSDirectory.INCLUDE_DIRECTORIES",
                                                        "VIFSDirectory.INCLUDE_BOTH" };

    private static final String[] tags_             = { ResourceLoader.getText ("PROP_VALUE_INCLUDE_FILES"),
                                                        ResourceLoader.getText ("PROP_VALUE_INCLUDE_DIRECTORIES"),
                                                        ResourceLoader.getText ("PROP_VALUE_INCLUDE_BOTH") };



    // Private data.
    private int value_          = default_;



/**
Returns the property value as text.

@return The property value as text.
**/
    public String getAsText ()
    {
        return tags_[value_];
    }



/**
Returns the Java initialization string.

@return The Java initialization string.
**/
    public String getJavaInitializationString ()
    {
        return initialization_[value_];
    }



/**
Returns the list of choices.

@return The list of choices.
**/
    public String[] getTags()
    {
        return tags_;
    }



/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
    }



/**
Returns the property value.

@return The property value.
**/
    public Object getValue ()
    {
        return new Integer (value_);
    }



/**
Sets the property value as text.

@param  text The property value as text.
**/
    public void setAsText (String text)
    {
        boolean found = false;
        for (int i = 0; i < tags_.length; ++i)
            if (text.equals (tags_[i])) {
                value_ = i;
                found = true;
                break;
            }

        if (found)
            firePropertyChange ();
        else
            throw new IllegalArgumentException ();
    }



/**
Sets the property value.

@param  value The property value.
**/
    public void setValue (Object value)
    {
        if (value instanceof Integer) {
            value_ = ((Integer) value).intValue ();
            if ((value_ < 0) || (value_ >= tags_.length))
                value_ = default_;
        }
        else
            value_ = default_;
    }



}
