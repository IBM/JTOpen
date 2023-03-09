///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  HashObject.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

public final class HashObject extends Object
{
  private int hash_;

  public HashObject()
  {
  }

  public void setHash(int hash)
  {
    hash_ = hash;
  }

  public int hashCode()
  {
    return hash_;
  }

  public boolean equals(Object obj)
  {
    return obj != null && obj instanceof HashObject && ((HashObject)obj).hash_ == this.hash_;
  }
}
