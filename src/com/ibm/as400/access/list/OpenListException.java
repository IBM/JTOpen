///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  OpenListException.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access.list;

/**
 Represents an exception that indicates an error has occurred processing an Open List API call on the server.
 **/
public class OpenListException extends Exception
{
    /**
     Error indicating that the list information did not exist or there was not enough list information returned to determine the list status.
     **/
    public static final int LIST_INFO_NOT_VALID = 0x00100000;

    /**
     Status indicating that the building of the list is pending.
     **/
    public static final int LIST_STATUS_PENDING = 0x000000F0;

    /**
     Status indicating that the list is in the process of being built.
     **/
    public static final int LIST_STATUS_BUILDING = 0x000000F1;

    /**
     Status indicating an error occurred when building the list.
     **/
    public static final int LIST_STATUS_ERROR = 0x000000F3;

    /**
     Status indicating the list is primed and ready to be built.
     **/
    public static final int LIST_STATUS_PRIMED = 0x000000F4;

    /**
     Status indicating there is too much data to be returned.
     **/
    public static final int LIST_STATUS_FULL = 0x000000F5;


    private int status_;

    OpenListException(int status)
    {
        status_ = status;
    }

    /**
     Returns the list status indicator for the Open List API call that generated this exception, or the error code.  Possible values are:
     <ul>
     <li>{@link #LIST_STATUS_PENDING LIST_STATUS_PENDING}
     <li>{@link #LIST_STATUS_BUILDING LIST_STATUS_BUILDING}
     <li>{@link #LIST_STATUS_ERROR LIST_STATUS_ERROR}
     <li>{@link #LIST_STATUS_PRIMED LIST_STATUS_PRIMED}
     <li>{@link #LIST_STATUS_FULL LIST_STATUS_FULL}
     <li>{@link #LIST_INFO_NOT_VALID LIST_INFO_NOT_VALID}
     </ul>
     @return  The error code.
     **/
    public int getStatus()
    {
        return status_;
    }
}
