///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400CertificateVldlUtilImplNative.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
   <p>The AS400CertificateVldlUtilImplNative provides the implementation of the  native methods for accessing certificates in an AS400 validation list object.
**/
class AS400CertificateVldlUtilImplNative  extends AS400CertificateVldlUtilImpl  implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


   // load the service program.
   static
   {
      System.load("/QSYS.LIB/QYJSPART.SRVPGM");
   }





 //********************************************************************/
 //* native methods for direct local invocation                       */
 //*                                                                  */
 //* @return  Return code mapped to CPFxxxx error message.            */
 //********************************************************************/

  native int calladdCertificate(byte[] cert, int certlen,
                     String ifsPathName, int pathlen);


  native int calldeleteCertificate(byte[] cert, int certlen,
                        String ifsPathName, int pathlen,
                        int certType);


  native int calllistCertificates(String ifsPathName, int pathlen,
                      String usrSpaceName,
                      boolean[] parmEntered,
                      String[] attrS,
                      byte[] [] attrB);


  native  int  callcheckCertificate(byte[] cert, int certlen,
                         String ifsPathName, int pathlen,
                         int   certType);


} // End of AS400CertificateVldlUtilImplNative class





