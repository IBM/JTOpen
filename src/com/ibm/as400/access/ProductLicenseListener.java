///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ProductLicenseListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;
/**
    The ProductLicenseListener interface provides an interface for receiving
    ProductLicense events.
**/

public interface ProductLicenseListener extends java.util.EventListener
{
  /**
   Invoked when a license has been released.
   @param event The ProductLicense event.
   **/
  public void licenseReleased(ProductLicenseEvent event);

  /**
   Invoked when a license has been requested.
  @param event The ProductLicense event.
   **/
  public void licenseRequested(ProductLicenseEvent event);

}




