///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSShareOptionEditor.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Hashtable;

/**
   The IFSShareOptionEditor class provides
   integrated file system share option editor support.
**/
public class IFSShareOptionEditor extends java.beans.PropertyEditorSupport
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private Integer value_ = new Integer(0);
  private static Hashtable javaInitializationString_ = new Hashtable();
  private static Hashtable optionAsText_ = new Hashtable();
  private static ResourceBundleLoader rbl_;
  private static Hashtable textAsOption_ = new Hashtable();
  private static String[] tags_ = new String[4];

  static
  {
    javaInitializationString_.put(new Integer(IFSFileInputStream.SHARE_ALL),
                                 "IFSFileInputStream.SHARE_ALL");
    javaInitializationString_.put(new Integer(IFSFileInputStream.SHARE_READERS),
                                 "IFSFileInputStream.SHARE_READERS");
    javaInitializationString_.put(new Integer(IFSFileInputStream.SHARE_WRITERS),
                                 "IFSFileInputStream.SHARE_WRITERS");
    javaInitializationString_.put(new Integer(IFSFileInputStream.SHARE_NONE),
                                 "IFSFileInputStream.SHARE_NONE");

    optionAsText_.put(new Integer(IFSFileInputStream.SHARE_ALL),
                      rbl_.getText("EDIT_SHARE_ALL"));
    optionAsText_.put(new Integer(IFSFileInputStream.SHARE_READERS),
                      rbl_.getText("EDIT_SHARE_READERS"));
    optionAsText_.put(new Integer(IFSFileInputStream.SHARE_WRITERS),
                      rbl_.getText("EDIT_SHARE_WRITERS"));
    optionAsText_.put(new Integer(IFSFileInputStream.SHARE_NONE),
                      rbl_.getText("EDIT_SHARE_NONE"));

    textAsOption_.put(rbl_.getText("EDIT_SHARE_ALL"),
                      new Integer(IFSFileInputStream.SHARE_ALL));
    textAsOption_.put(rbl_.getText("EDIT_SHARE_READERS"),
                      new Integer(IFSFileInputStream.SHARE_READERS));
    textAsOption_.put(rbl_.getText("EDIT_SHARE_WRITERS"),
                      new Integer(IFSFileInputStream.SHARE_WRITERS));
    textAsOption_.put(rbl_.getText("EDIT_SHARE_NONE"),
                      new Integer(IFSFileInputStream.SHARE_NONE));

    tags_[0] = rbl_.getText("EDIT_SHARE_ALL");
    tags_[1] = rbl_.getText("EDIT_SHARE_READERS");
    tags_[2] = rbl_.getText("EDIT_SHARE_WRITERS");
    tags_[3] = rbl_.getText("EDIT_SHARE_NONE");
  }

  /**
   Returns the property value in a form suitable for editing.
   @return The property value in a form suitable for editing.
   **/
  public String getAsText()
  {
    return (String) optionAsText_.get(value_);
  }

  /**
   Returns the copyright.
   @return The copyright String.
   **/
  private static String getCopyright()
  {
    return Copyright.copyright;
  }

  /**
   Returns a code fragment representing an initializer for the current value.
   This method is intended for use when generating Java code to set the
   property value.
   @return A code fragment representing an initializer for the current value.
   **/
  public String getJavaInitializationString()
  {
    return (String) javaInitializationString_.get(value_);
  }

  /**
   Returns the set of possible values for the property.
   @return The set of possible values for the property.
   **/
  public String[] getTags()
  {
    return tags_;
  }

  /**
   Returns the value of the property.
   @return The value of the property.
   **/
  public Object getValue()
  {
    return value_;
  }

  /**
   Sets the property value from the specified String.
   @param text The property value.
   **/
  public void setAsText(String text)
  {
    value_ = (Integer) textAsOption_.get(text);
    firePropertyChange();
  }
    
  /**
   Sets the value of the object that is to be edited.
   @param value The new target object to be edited.
   **/
  public void setValue(Object value)
  {
    if (value instanceof Integer)
    {
      value_ = (Integer) value;
      firePropertyChange();
    }
  }
}



