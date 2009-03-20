///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400CertificateUsrPrfUtilImplNative.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
   <p>The AS400CertificateUsrPrfUtilImplNative provides the implementation of the native methods for accessing certificates in an i5/OS user profile object.
**/
class AS400CertificateUsrPrfUtilImplNative  extends AS400CertificateUsrPrfUtilImpl
{
  private static final String CLASSNAME = "com.ibm.as400.access.AS400CertificateUsrPrfUtilImplNative";
  static
  {
    if (Trace.traceOn_) Trace.logLoadPath(CLASSNAME);
  }

   // load the service program.
   static
   {
       try{
           System.load("/QSYS.LIB/QYJSPART.SRVPGM");
       } catch(Throwable e)
       {
               Trace.log(Trace.ERROR, "Error loading QYJSPART service program:", e); //may be that it is already loaded in multiple .war classloader
       }
   }



 //********************************************************************/
 //* native methods for direct local invocation                       */
 //*                                                                  */
 //* @return  Return code mapped to CPFxxxx error message.            */
 //********************************************************************/

  native int calladdCertificate(byte[] cert, int certlen,
                     String user);


  native int calldeleteCertificate(byte[] cert, int certlen,
                        String user,
                        int certType);


  native int calllistCertificates(String user,
                      String usrSpaceName,
                      boolean[] parmEntered,
                      String[] attrS,
                      byte[] [] attrB);


  native  int  callfindCertificateUser(byte[] cert,
                           int certlen,
                           int   certType);


} // End of AS400CertificateUsrPrfUtilImplNative class





