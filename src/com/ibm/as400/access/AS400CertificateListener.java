///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400CertificateListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;
/**
   The AS400CertificateListener interface provides
   an interface for receiving AS/400 certificate events.
**/

public interface AS400CertificateListener extends java.util.EventListener
{
  /**
   Invoked when a certificate has been added.
   @param event The AS400CertificateEvent.
   **/
  public void added( AS400CertificateEvent event );

  /**
   Invoked when a certificate has been deleted.
   @param event The AS400CertificateEvent.
   **/
  public void deleted( AS400CertificateEvent event );

}




