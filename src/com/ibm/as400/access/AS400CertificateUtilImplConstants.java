///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400CertificateUtilImplConstants.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
   <p>The AS400CertificateUtilImplConstants provides a common location for  implementation constants. 
**/
interface AS400CertificateUtilImplConstants 
{

  //number of digid search attrs
  static final int SEARCH_PARMS	=	7;
  static final int HANDLE_LEN	=	40;  

  //cpfError string for additional msg text   
  static final int ERR_STRING_LEN =	100;
  static final int SUCCESS =		0;
   

  //AS400CertificateUtil remote methods
  static final int CALL_GETCERT =	1;
  static final int CALL_GETHANDLE =	2;

  //AS400CertificateVldlUtil remote methods
  static final int CALL_VLDL_ADD =		101;
  static final int CALL_VLDL_DELETE =		102;
  static final int CALL_VLDL_LISTCERT =		103;
  static final int CALL_VLDL_CHECKCERT =	104;
  
  //AS400CertificateUserProfileUtil remote methods
  static final int CALL_USRPRF_ADD =		201;
  static final int CALL_USRPRF_DELETE =		202;
  static final int CALL_USRPRF_LISTCERT =	203;
  static final int CALL_USRPRF_FINDCERT =	204;
 
} // End of AS400CertificateUtilImplConstants 




