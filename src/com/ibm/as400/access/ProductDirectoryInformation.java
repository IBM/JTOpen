///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ProductDirectoryInformation.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

public class ProductDirectoryInformation
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

  private String publicDataAuthority_;
  private String fullPath_;
  private String installedPath_;
  private String[] authorities_;

  ProductDirectoryInformation(String publicAuth, String fullPath, String installPath, String[] authorities)
  {
    publicDataAuthority_ = publicAuth;
    fullPath_ = fullPath;
    installedPath_ = installPath;
    authorities_ = authorities;
  }

  public String getInstalledPath()
  {
    return installedPath_;
  }

  public String getPrimaryPath()
  {
    return fullPath_;
  }

  public String getPublicDataAuthority() 
  {
    return publicDataAuthority_;
  }

  public String[] getPublicObjectAuthorities()
  {
    return authorities_;
  }

  public String toString()
  {
    return fullPath_;
  }
}

