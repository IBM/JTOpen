package com.ibm.as400.security.auth;

///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: RetrieveFailedException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////


import com.ibm.as400.access.AS400Message;

/**
 * The RetrieveFailedException class represents an exception
 * issued when errors occur while retrieving system
 * authentication information.
 *
 * <p> If available, one or more AS400Message objects may
 * be included in the exception.
 *
 */
public class RetrieveFailedException extends AS400AuthenticationException {

    static final long serialVersionUID = 4L;

    /**
    * Constructs a RetrieveFailedException.
    *
    */
    public RetrieveFailedException() {  
	    super();
    }

    /**
    * Constructs a RetrieveFailedException.
    *
    * @param list
    *		The AS400Message objects to be associated
    *		with the exception.
    *
    */
    public RetrieveFailedException(AS400Message[] list) {
	    super(list);
    }

    /**
    * Constructs a RetrieveFailedException.
    *
    * @param rc
    *		The return code identifying the detail text
    *		to assign to the exception.
    *
    */
    public RetrieveFailedException(int rc) {
	    super(rc);
    }
}
