///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ReturnCodeException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
   The ReturnCodeException interface defines the methods
   required for exceptions that contain a return code.  A return
   code is used to further identify the cause of an error when an
   exception can be thrown for several reasons.
   The return code values are defined in the class that implements
   this interface.
**/

// The return code values must be defined as constants within
// the class that implements this interface.
public interface ReturnCodeException
{

    /**
       Returns the return code associated with this exception.
       @return   The return code associated with this exception.
    **/
    public int getReturnCode();
}
