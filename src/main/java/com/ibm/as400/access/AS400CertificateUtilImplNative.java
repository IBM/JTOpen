///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400CertificateUtilImplNative.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
   <p>The AS400CertificateUtilImplNative provides the implementation of the native methods for accessing certificates from the AS400CertificateUtil object.
**/
class AS400CertificateUtilImplNative  extends AS400CertificateUtilImpl
{
  private static final String CLASSNAME = "com.ibm.as400.access.AS400CertificateUtilImplNative";
  static
  {
    if (Trace.traceOn_) Trace.logLoadPath(CLASSNAME);
  }

   // load the service program.
   static
   {
	   NativeMethods.loadNativeLibraryQyjspart(); 
   }




 //********************************************************************/
 //* native methods for direct local invocation                       */
 //*                                                                  */
 //* @return  Return code mapped to CPFxxxx error message.            */
 //********************************************************************/

  native int callgetCertificates(String usrSpaceName,
                        int buffSize,
                        int nextCertificateToReturn,
                        int nextCertificateOffsetIn);


  native int callgetHandle(byte[] certificate,
                   int len);


} // End of AS400CertificateUtilImplNative class





