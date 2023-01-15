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

/**
 * Represents product directory information for a specific licensed product.
 * Use the {@link com.ibm.as400.access.Product#getDirectoryInformation Product.getDirectoryInformation } method
 * to retrieve the product directory information for a product.
 * @see com.ibm.as400.access.Product
**/
public class ProductDirectoryInformation
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";

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

  /**
   * The installed full path name for the product directory. This is the installed product home directory
   * concatenated with the installed product directory name.
   * @return The installed full path name of the product directory.
  **/
  public String getInstalledPath()
  {
    return installedPath_;
  }

  /**
   * The primary full path name for the product directory. This is the primary product home directory concatenated 
   * with the primary product directory name.
   * @return The primary full path name of the product directory.
  **/
  public String getPrimaryPath()
  {
    return fullPath_;
  }

  /**
   * The public data authority given to the directory by the Restore Licensed Program (RSTLICPGM) command when
   * this product is installed if the directory does not exist. If the product load has not been successfully
   * packaged, this field is blank. Other possible values are:
   * <UL>
   * <LI>*RWX - Read, write, and execute authorities.
   * <LI>*RW - Read and write authorities.
   * <LI>*RX - Read and execute authorities.
   * <LI>*WX - Write and execute authorities.
   * <LI>*R - Read authority.
   * <LI>*W - Write authority.
   * <LI>*X - Execute authority.
   * <LI>*EXCLUDE - Restricted authority.
   * <LI>*NONE - No specific authorities.
   * </UL>
   * @return The public data authority for this product directory.
   * @see com.ibm.as400.access.UserPermission
  **/
  public String getPublicDataAuthority() 
  {
    return publicDataAuthority_;
  }

  /**
   * The public object authorities given to the directory by the Restore Licensed Program (RSTLICPGM) command when
   * this product is installed if the directory does not exist. If the product load has not been successfully
   * packaged, the number of public object authorities will be 0 and this method will return an empty String array.
   * Possible public object authorities are:
   * <UL>
   * <LI>*NONE - No authority.
   * <LI>*ALL - All authorities.
   * <LI>*OBJEXIST - Existence authority.
   * <LI>*OBJMGT - Management authority.
   * <LI>*OBJALTER - Alter authority.
   * <LI>*OBJREF - Reference authority.
   * </UL>
   * @return The public object authorities for this product directory.
   * @see com.ibm.as400.access.UserPermission
  **/
  public String[] getPublicObjectAuthorities()
  {
    return authorities_;
  }

  /**
   * Returns the string representation of this object.
   * @return The primary full path name of this product directory.
  **/
  public String toString()
  {
    return fullPath_;
  }
}

