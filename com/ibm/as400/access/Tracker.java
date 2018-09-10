///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: Tracker.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

final class Tracker
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

  private boolean set_ = true;

  public boolean isSet()
  {
    return set_;
  }

  public void set(boolean set)
  {
    set_ = set;
  }
}
