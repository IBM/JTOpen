///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDSQLToken.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * JDSQLToken stores information about
 * the location of tokens inside of the SQL
 * statement for a JDSQLTokenizer object.
**/
final class JDSQLToken
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
  
  char[] data_;
  int offset_;
  int length_;
  int type_;
  int parms_;

  JDSQLToken(char[] data, int offset, int length, int type)
  {
    data_ = data;
    offset_ = offset;
    length_ = length;
    type_ = type;
  }

  JDSQLToken(char[] data, int offset, int length, int type, int parms)
  {
    this(data, offset, length, type);
    parms_ = parms;
  }

  public String getToken()
  {
    return new String(data_, offset_, length_);
  }
}
