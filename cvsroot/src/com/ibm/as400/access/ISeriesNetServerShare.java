///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: ISeriesNetServerShare.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2000 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 The ISeriesNetServerShare class represents a NetServer share (either a file share or a print share).
 **/
public abstract class ISeriesNetServerShare implements java.io.Serializable
{

  static final long serialVersionUID = 469818009548069269L;

  // Note: For efficiency, these attributes are not private, so they are directly accessible by the ISeriesNetServer class when composing and refreshing share objects.
  String name_;     // Note: This attribute has a getter but no setter.
  String description_;

  // Provided for use by the ISeriesNetServer class:
  boolean isFile_;  // If it's not a file, then it's a printer.

  int numOptionalParmsToSet_ = 0;


  // This method does no argument validity checking, nor does it update attributes on server.
  void setAttributeValues(String shareName, String description, boolean isFile)
  {
    name_ = shareName;
    description_ = description;
    isFile_ = isFile;
  }


  /**
   Returns the name of the share.
   @return The name of the share.
   **/
  public String getName()
  {
    return name_;
  }

  // Note: We provide no setter for the "name" attribute, since the NetServer API's don't support changing the name of a share after it's created.


  /**
   Returns the text description of the share.
   @return  The description of the share.
   **/
  public String getDescription()
  {
    return description_;
  }


  /**
   Sets the text description of the share.
   @param description  The description of the share.
   **/
  public void setDescription(String description)
  {
    if (description == null) throw new NullPointerException("path");
    description_ = description.trim();
  }

}
