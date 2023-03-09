///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  ObjectInfo.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.components;

/**
 * Represents object information returned by the ListObjects class.
 * The toString() and toString2() methods will print
 * the various fields in a format similar to what WRKOBJ does.
**/
public class ObjectInfo
{
  private String name_;
  private String library_;
  private String type_;
  private String status_;

  private String attribute_;
  private String description_;

  ObjectInfo(String name, String lib, String type, String status)
  {
    name_ = name;
    library_ = lib;
    type_ = type;
    status_ = status;
  }

  public String getName()
  {
    return name_;
  }

  public String getLibrary()
  {
    return library_;
  }

  public String getType()
  {
    return type_;
  }

  public String getStatus()
  {
    return status_;
  }

  void setTextDescription(String s)
  {
    description_ = s;
  }

  public String getTextDescription()
  {
    return description_;
  }

  void setAttribute(String s)
  {
    attribute_ = s;
  }

  public String getAttribute()
  {
    return attribute_;
  }

  public String toString()
  {
    return name_+"  "+type_+"  "+library_+(attribute_ != null ? "  "+attribute_ : "")+(description_ != null ? "  "+description_.trim() : "");
  }

  public String toString2()
  {
    return name_+"  "+type_+"  "+library_;
  }
}

