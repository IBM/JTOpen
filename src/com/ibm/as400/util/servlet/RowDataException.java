///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RowDataException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

import com.ibm.as400.access.Copyright;
/**
*   The RowDataException class represents an exception which indicates 
*   that a problem occurred when working with the row data.
**/
public class RowDataException extends Exception 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   private String s = Copyright.copyright;
   private Exception exception_;

   /**
   *  Constructs a default RowDataException.
   **/
   RowDataException() 
   { 
      super(); 
   }
  
   /**
   *  Constructs a RowDataException.
   *
   *  @param exception The exception.
   **/
    public RowDataException(Exception exception)            // @B1C
    {
        super(exception.getMessage());                       
        exception_ = exception;
    }

   /**
   *  Returns the original exception.
   *  @return The exception.
   **/
   public Exception getException()
   {
      return exception_;
   }
}
