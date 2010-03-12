///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDConnectionPoolKey.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2005-2005 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
 Helper class for storing connection pool hashkeys, used by AS400JDBCManagedDataSource and JDConnectionPoolManager.
 It encapsulates the parameters that define a unique database connection:
 user, password.
 **/
final class JDConnectionPoolKey
{
  private String user_;       // userID
  private int pwHashcode_;  // password hash
  private int hashCode_;


  JDConnectionPoolKey(String userName, int pwHashcode)
  {
    user_ = userName;
    pwHashcode_ = pwHashcode;
    hashCode_ = user_.hashCode() + pwHashcode;
  }


  // Need this for key comparisons.
  public boolean equals(Object obj)
  {
    try
    {
      JDConnectionPoolKey key = (JDConnectionPoolKey)obj;
      return (user_.equals(key.user_) && pwHashcode_ == key.pwHashcode_);
    }
    catch (Throwable e) {
      return false;
    }
  }

  String getUser()
  {
    return user_;
  }

  // Needed for good hashing.
  public int hashCode()
  {
    return hashCode_;
  }

  // Only used for logging purposes, that's why we build the String here every time
  // instead of in the constructor. We don't mind taking a performance hit when we are tracing.
  public String toString()
  {
    return "["+ user_  + "]";
  }

  void update(String userName, int pwHashcode)
  {
    user_ = userName;
    pwHashcode_ = pwHashcode;
    hashCode_ = user_.hashCode() + pwHashcode;
  }

}
