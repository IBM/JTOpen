///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  UserInfo.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.components;

/**
 * Represents user information returned by the ListUsers class.
 * The toString() method will print the fields in a format similar to WRKUSRPRF.
**/
public final class UserInfo
{
  private final String name_;
  private final String userClass_;
  private final String expired_;
  private final long maxStorage_;
  private final long storageUsed_;
  private final String description_;
  private final String locked_;
  private final String damaged_;
  private final String status_;
  private final long uid_;
  private final long gid_;

  UserInfo(String userName, String userClass, String passwordExpired,
           long maxStorage, long storageUsed, String description,
           String locked, String damaged, String status,
           long uid, long gid)
  {
    name_ = userName;
    userClass_ = userClass;
    expired_ = passwordExpired;
    maxStorage_ = maxStorage;
    storageUsed_ = storageUsed;
    description_ = description;
    locked_ = locked;
    damaged_ = damaged;
    status_ = status;
    uid_ = uid;
    gid_ = gid;
  }

  public String getName()
  {
    return name_;
  }

  public String getUserClass()
  {
    return userClass_;
  }

  public String getExpired()
  {
    return expired_;
  }

  /**
   * Returns 1 if the setting is *NOMAX.
  **/
  public long getStorageMax()
  {
    return maxStorage_;
  }

  public long getStorageUsed()
  {
    return storageUsed_;
  }

  public String getDescription()
  {
    return description_;
  }

  public String getLocked()
  {
    return locked_;
  }

  public String getDamaged()
  {
    return damaged_;
  }

  public String getStatus()
  {
    return status_;
  }

  public long getUID()
  {
    return uid_;
  }

  public long getGID()
  {
    return gid_;
  }

  public String toString()
  {
    return name_+"  "+description_;
  }
}
