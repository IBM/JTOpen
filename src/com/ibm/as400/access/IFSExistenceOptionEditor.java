///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: IFSExistenceOptionEditor.java
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
   The IFSExistenceOptionEditor class provides
   integrated file system existence editor support.
**/

public class IFSExistenceOptionEditor extends java.beans.PropertyEditorSupport
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private Integer value_ = new Integer(0);
  private static Hashtable javaInitializationString_ = new Hashtable();
  private static Hashtable optionAsText_ = new Hashtable();
  private static ResourceBundleLoader rbl_;
  private static String[] tags_ = new String[5];
  private static Hashtable textAsOption_ = new Hashtable();

  static
  {
    javaInitializationString_.put(new Integer(IFSRandomAccessFile.OPEN_OR_CREATE),
                                  "IFSRandomAccessFile.OPEN_OR_CREATE");
    javaInitializationString_.put(new Integer(IFSRandomAccessFile.REPLACE_OR_CREATE),
                                  "IFSRandomAccessFile.REPLACE_OR_CREATE");
    javaInitializationString_.put(new Integer(IFSRandomAccessFile.FAIL_OR_CREATE),
                                  "IFSRandomAccessFile.FAIL_OR_CREATE");
    javaInitializationString_.put(new Integer(IFSRandomAccessFile.OPEN_OR_FAIL),
                                  "IFSRandomAccessFile.OPEN_OR_FAIL");
    javaInitializationString_.put(new Integer(IFSRandomAccessFile.REPLACE_OR_FAIL),
                                  "IFSRandomAccessFile.REPLACE_OR_FAIL");

    optionAsText_.put(new Integer(IFSRandomAccessFile.OPEN_OR_CREATE),
                      rbl_.getText("EDIT_OPEN_CREATE"));
    optionAsText_.put(new Integer(IFSRandomAccessFile.REPLACE_OR_CREATE),
                      rbl_.getText("EDIT_REPLACE_CREATE"));
    optionAsText_.put(new Integer(IFSRandomAccessFile.FAIL_OR_CREATE),
                      rbl_.getText("EDIT_FAIL_CREATE"));
    optionAsText_.put(new Integer(IFSRandomAccessFile.OPEN_OR_FAIL),
                      rbl_.getText("EDIT_OPEN_FAIL"));
    optionAsText_.put(new Integer(IFSRandomAccessFile.REPLACE_OR_FAIL),
                      rbl_.getText("EDIT_REPLACE_FAIL"));

    textAsOption_.put(rbl_.getText("EDIT_OPEN_CREATE"),
                      new Integer(IFSRandomAccessFile.OPEN_OR_CREATE));
    textAsOption_.put(rbl_.getText("EDIT_REPLACE_CREATE"),
                      new Integer(IFSRandomAccessFile.REPLACE_OR_CREATE));
    textAsOption_.put(rbl_.getText("EDIT_FAIL_CREATE"),
                      new Integer(IFSRandomAccessFile.FAIL_OR_CREATE));
    textAsOption_.put(rbl_.getText("EDIT_OPEN_FAIL"),
                      new Integer(IFSRandomAccessFile.OPEN_OR_FAIL));
    textAsOption_.put(rbl_.getText("EDIT_REPLACE_FAIL"),
                      new Integer(IFSRandomAccessFile.REPLACE_OR_FAIL));

    tags_[0] = rbl_.getText("EDIT_OPEN_CREATE");
    tags_[1] = rbl_.getText("EDIT_REPLACE_CREATE");
    tags_[2] = rbl_.getText("EDIT_FAIL_CREATE");
    tags_[3] = rbl_.getText("EDIT_OPEN_FAIL");
    tags_[4] = rbl_.getText("EDIT_REPLACE_FAIL");

  }

  /**
   Returns the property value in an editable form. 
   @return The property value in an editable form.
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








