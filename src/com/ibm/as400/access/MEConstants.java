///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: MEConstants.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
The MEConstants class defines global constants for the
ME support.
**/

public interface MEConstants
{
    // The component request datastream types.
    static final int SERVICE_COMMAND = 2;
    static final int SERVICE_DATAQUEUE = 3;
    static final int SERVICE_DATABASE = 4;
    static final int SERVICE_ALL_SERVICES = 99;

    // The default port number.  This  port number was registered
    // with the Internet Assigned Numbers Authority (http://www.iana.org)
    // on 01/29/1999.  For a complete list of registered port numbers,
    // go to http://www.isi.edu/in-notes/iana/assignments/port-numbers.
    public static final int ME_SERVER_PORT = 3470;

    // The reply datastream types.
    public static final int SIGNON_SUCCEEDED = 0x1234;
    public static final int SIGNON_FAILED    = 0x1235;

    public static final int XML_DOCUMENT_NOT_REGISTERED = 0x1236;
    public static final int XML_DOCUMENT_REGISTERED = 0x1237;
    public static final int DATA_QUEUE_WRITE_READ_SUCCESSFUL = 0x1238;
    public static final int EXCEPTION_OCCURRED = 0x1239;
    public static final int REQUEST_NOT_SUPPORTED = 0x01240;
    
    // The request datastream types.
    public static final int SIGNON = 0x1122;
    public static final int COMMAND_CALL = 0x1123;
    public static final int PROGRAM_CALL = 0x1124;
    public static final int DISCONNECT = 0x1125;
    public static final int DATA_QUEUE_READ = 0x1126;
    public static final int DATA_QUEUE_WRITE = 0x1127;
    public static final int DATA_QUEUE_BYTES = 0x1128;
    public static final int DATA_QUEUE_STRING = 0x1129;
    public static final int RETURN_CODE = 0x1130;
    public static final int RELOAD = 0x1131;

    public static final int JDBC_ABSOLUTE = 0x1350;
    public static final int JDBC_COMMAND = 0x1351;
    public static final int JDBC_CLOSE = 0x1352;
    public static final int JDBC_NEXT = 0x1353;
    public static final int JDBC_NEXT_RECORD = 0x1354;
    public static final int JDBC_PREVIOUS = 0x1355;

    public static final int JDBC_RESULT_SET_CLOSED = 0x1356;
    public static final int JDBC_COMMAND_SUCCEEDED = 0x1357;
    public static final int JDBC_EXCEPTION = 0x1358;
    public static final int JDBC_TRANSACTION_NOT_FOUND = 0x1359;
    public static final int JDBC_POSITION_CURSOR_FAILED = 0x1360;
}
