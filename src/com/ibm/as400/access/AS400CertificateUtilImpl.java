///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400CertificateUtilImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;


/**
   <p>The AS400CertificateUtilImpl provides the base class for the implementation of the methods for accessing certificates in an AS400CertificateUtil object.  
**/
abstract class AS400CertificateUtilImpl  implements java.io.Serializable, AS400CertificateUtilImplConstants
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;


  Converter converter_;        // The string to AS400 data converter. @C0C @C1C
  AS400 system_ = null; 

  // Output parms for native methods    
  int nextCertificateOffsetOut_;
  int numberCertificatesFound_;
  AS400Certificate[] certificates_;
  String cpfError_;
  byte[] handle_;
   
    

  // Returns the copyright.
  private static String getCopyright()
  {
    return Copyright.copyright;
  }

  
 //********************************************************************/
 //* methods for either direct ot remote invocation                   */
 //*                                                                  */
 //* @return  Return code mapped to CPFxxxx error message.            */
 //********************************************************************/
 
  abstract int callgetCertificates(String usrSpaceName,
				    int buffSize,
				    int nextCertificateToReturn,
				    int nextCertificateOffsetIn)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException;       

    
  abstract int callgetHandle(byte[] certificate,
			      int len)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException;       

  
  void setConverter(Converter converter)
  { 
      this.converter_ = converter;	// @C1A
  }

  
} // End of AS400CertificateUtilImpl class





