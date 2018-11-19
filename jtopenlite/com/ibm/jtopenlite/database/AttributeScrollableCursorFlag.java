///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  AttributeScrollableCursorFlag.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database;

interface AttributeScrollableCursorFlag
{
  public int getScrollableCursorFlag();

  public boolean isScrollableCursorFlagSet();

  public void setScrollableCursorFlag(int value);
}

