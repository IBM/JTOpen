///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: DBDataStreamException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;




/**
   The DBDataStreamException class represents an
   exception which indicates that a problem occurred when
   trying to create the data stream.	
**/

 class DBDataStreamException extends Exception 
 {
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  	DBDataStreamException() { super(); }
  
	DBDataStreamException(String s) { super(s); }

       // Returns the copyright.
       private static String getCopyright()
       {
        return Copyright.copyright;
       }	

 }





