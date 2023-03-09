///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename: OpenListOfJobsKeyField.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.workmgmt;

public class OpenListOfJobsKeyField
{
  private int key_;
  private boolean isBinary_;
  private int length_;
  private int displacement_;

  public OpenListOfJobsKeyField(int key, boolean isBinary, int lengthOfData, int displacementToData)
  {
    key_ = key;
    isBinary_ = isBinary;
    length_ = lengthOfData;
    displacement_ = displacementToData;
  }

  public int getKey()
  {
    return key_;
  }

  public boolean isBinary()
  {
    return isBinary_;
  }

  public int getLength()
  {
    return length_;
  }

  public int getDisplacement()
  {
    return displacement_;
  }
}
