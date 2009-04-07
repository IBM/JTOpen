///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400CertificateUsrPrfUtilImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.beans.PropertyVetoException;


/**
   <p>The AS400CertificateUsrPrfUtilImpl provides the base class for the implementation of the methods for accessing certificates in an IBM i user profile object.  
**/
abstract class AS400CertificateUsrPrfUtilImpl implements AS400CertificateUtilImplConstants
{
  Converter converter_;        // The string to IBM i data converter. @C0C @C1C
  AS400 system_ = null;
  
  // Output parms for native methods
  String  cpfError_ = null;
  int numberCertificatesFound_;
  String  userName_ = null;  

  
 //********************************************************************/
 //* methods for either direct ot remote invocation                   */
 //*                                                                  */
 //* @return  Return code mapped to CPFxxxx error message.            */
 //********************************************************************/
    
  abstract int calladdCertificate(byte[] cert, int certlen,
				   String user)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException;       
    
     

  abstract int calldeleteCertificate(byte[] cert, int certlen,
				      String user,
				      int certType)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException;       
    

  
  abstract int calllistCertificates(String user,
				     String usrSpaceName,
				     boolean[] parmEntered, 
				     String[] attrS,
				     byte[] [] attrB)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException;       
    

   
  abstract int  callfindCertificateUser(byte[] cert,
					int certlen,
					int   certType)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException;       


  void setConverter(Converter converter)
  { 
      this.converter_ = converter;	// @C1A
  }
  

} // End of AS400CertificateUsrPrfUtilImpl class





