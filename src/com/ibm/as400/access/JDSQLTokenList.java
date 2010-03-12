///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDSQLTokenList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// This is more efficient than a Vector, and doesn't need to be synchronized.
final class JDSQLTokenList
{
  static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

  JDSQLToken[] tokens_ = new JDSQLToken[32]; // Even a space is a token, so there will be lots!
  int count_;

  final void addToken(final JDSQLToken t)
  {
    if (count_ >= tokens_.length)
    {
      JDSQLToken[] temp = tokens_;
      tokens_ = new JDSQLToken[temp.length*2];
      System.arraycopy(temp, 0, tokens_, 0, temp.length);
    }
    tokens_[count_++] = t;
  }
}