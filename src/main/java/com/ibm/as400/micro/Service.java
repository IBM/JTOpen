///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Service.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import java.util.Properties;
import java.io.IOException;

/**
Interface to standardize the format of 
services to provide from the MEServer.
**/
interface Service
{
    /**
     * @param in 
     * @param out 
    **/
    void setDataStreams(MicroDataInputStream in, MicroDataOutputStream out);

    /**
    Given a function id, returns whether or not 
    this service knows how to handle the given function.
     * @param functionId 
     * @return true if the service knows how to handle the function
    **/
    boolean acceptsRequest(int functionId);

    /**
    Takes care of handling a given request, including
    all needed stream input and output.
     * @param functionId 
     * @throws IOException 
    **/
    void handleRequest(int functionId) throws IOException;
}
