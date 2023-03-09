///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  AttributeReturnSize.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database;

interface AttributeReturnSize
{
  public int getReturnSize();

  public boolean isReturnSizeSet();

  public void setReturnSize(int value);
}

