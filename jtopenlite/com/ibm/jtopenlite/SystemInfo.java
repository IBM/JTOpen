///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  SystemInfo.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite;


/**
 * Initially obtained from a {@link SignonConnection SignonConnection} object; contains information about the System i host,
 * such as VRM level (V5R4M0, etc) and password level. All host server connections will have an associated SystemInfo object.
 * If a connection is constructed using a system name, user, and password, then an implicit SignonConnection is made to
 * obtain the SystemInfo object, and then closed.
 * <p></p>
 * For performance reasons, when multiple connections need to be made to
 * the same host (Command, DDM, File, etc), an application may want to explicitly retrieve the SystemInfo object directly
 * from the SignonConnection, in order to avoid any implicit SignonConnections.
**/
public final class SystemInfo
{
	public final static int VERSION_540 = 0x050400; 
	public final static int VERSION_610 = 0x060100; 
	public final static int VERSION_710 = 0x070100; 
	
  private String system_;
  private int serverVersion_;
  private int serverLevel_;
  private int serverCCSID_;
  private int passwordLevel_;
  private String jobName_;

  SystemInfo(String system, int serverVersion, int serverLevel, int passwordLevel, String jobName)
  {
    system_ = system;
    serverVersion_ = serverVersion;
    serverLevel_ = serverLevel;
    passwordLevel_ = passwordLevel;
    jobName_ = jobName;
  }

  /**
   * Returns the system name.
  **/
  public String getSystem()
  {
    return system_;
  }

  void setServerCCSID(int ccsid)
  {
    serverCCSID_ = ccsid;
  }

  /**
   * Returns the server CCSID.
  **/
  public int getServerCCSID()
  {
    return serverCCSID_;
  }

  /**
   * Returns the server lipi level.
  **/
  public int getServerLevel()
  {
    return serverLevel_;
  }

  /**
   * Returns the server VRM version.
   **/

  public int getServerVersion() {
     return serverVersion_; 
  }
  
  
  /**
   * Returns the server password level.
  **/
  public int getPasswordLevel()
  {
    return passwordLevel_;
  }

  String getSignonJobName()
  {
    return jobName_;
  }

  public int hashCode()
  {
    return jobName_.hashCode();
  }

  public boolean equals(Object obj)
  {
    if (obj != null && obj instanceof SystemInfo)
    {
      SystemInfo info = (SystemInfo)obj;
      return info.serverVersion_ == this.serverVersion_ &&
             info.serverLevel_ == this.serverLevel_ &&
             info.passwordLevel_ == this.passwordLevel_ &&
             info.system_.equals(this.system_) &&
             info.jobName_.equals(this.jobName_);
    }
    return false;
  }

  public String toString()
  {
    return system_+"["+Integer.toHexString(serverVersion_)+"]:"+jobName_;
  }
}
