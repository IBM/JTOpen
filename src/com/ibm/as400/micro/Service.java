//////////////////////////////////////////////////////////////////////
//
// IBM Confidential
//
// OCO Source Materials
//
// The Source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office
//
// 5722-JC1
// (C) Copyright IBM Corp. 2002
//
////////////////////////////////////////////////////////////////////////
//
// File Name:    Service.java
//
// Description:  See comments below
//
// Classes:      Service
//
////////////////////////////////////////////////////////////////////////
//
// CHANGE ACTIVITY:
//
//  Flg=PTR/DCR   Release       Date        Userid     Comments
//        D98585.1  v5r2m0.jacl  09/11/01   wiedrich  Created.
//
// END CHANGE ACTIVITY
//
////////////////////////////////////////////////////////////////////////
package com.ibm.as400.micro;

import java.util.Properties;
import java.io.IOException;

/**
Interface to standardize the format of server
services to provide from the MEServer.
**/
interface Service
{
    /**
    **/
    void setDataStreams(MicroDataInputStream in, MicroDataOutputStream out);

    /**
    Given a function id, returns whether or not 
    this service knows how to handle the given function.
    **/
    boolean acceptsRequest(int functionId);

    /**
    Takes care of handling a given request, including
    all needed stream input and output.
    **/
    void handleRequest(int functionId) throws IOException;
}
