///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxExceptionRepSV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


import java.io.PrintWriter;
import java.io.StringWriter;



/**
The PxExceptionRepSV class represents the
server view of an exception reply.
**/
class PxExceptionRepSV
extends PxRepSV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    


/**
Constructs a PxExceptionRepSV object.
**/
   public PxExceptionRepSV (Throwable e)
   {
       super (ProxyConstants.DS_EXCEPTION_REP);

       // Get the stack trace.
       StringWriter stringWriter = new StringWriter ();
       e.printStackTrace (new PrintWriter (stringWriter, true)); 
       String stackTrace = stringWriter.toString ();

       addParm (new PxSerializedObjectParm (e));
       addParm (new PxStringParm (stackTrace));

       String x = Copyright.copyright;
   }



}
