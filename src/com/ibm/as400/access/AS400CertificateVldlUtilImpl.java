///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400CertificateVldlUtilImpl.java
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
   <p>The AS400CertificateVldlUtilImpl provides the base class for the implementation of the methods for accessing certificates in an AS400 validation list object.  
**/
abstract class AS400CertificateVldlUtilImpl implements java.io.Serializable, AS400CertificateUtilImplConstants
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  Converter converter_;        // The string to AS400 data converter. @C0C @C1C
  AS400 system_ = null;

  // Output parms for native methods
  String cpfError_ = null;
  int numberCertificatesFound_;
  int present_;

  
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
    
  abstract int calladdCertificate(byte[] cert, int certlen,
				   String ifsPathName, int pathlen)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException;       
    
     

  abstract int calldeleteCertificate(byte[] cert, int certlen,
				      String ifsPathName, int pathlen,
				      int certType)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException;       
    
     
 
  
  abstract int calllistCertificates(String ifsPathName, int pathlen,
				     String usrSpaceName,
				     boolean[] parmEntered, 
				     String[] attrS,
				     byte[] [] attrB)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException;       
    

   
  abstract int  callcheckCertificate(byte[] cert, int certlen,
				      String ifsPathName, int pathlen,
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
  

} // End of AS400CertificateVldlUtilImpl class





