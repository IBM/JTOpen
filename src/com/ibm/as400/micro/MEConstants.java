///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

/**
 *  The MEConstants class defines global constants for the
 *  ToolboxME for iSeries support.
**/

interface MEConstants
{
    // THE COMPONENT REQUEST DATASTREAM TYPES.

    /**
     *  Constant indicating the Command service.
     **/
    static final int SERVICE_COMMAND = 2;

    /**
     *  Constant indicating the Dataqueue service.
     **/
    static final int SERVICE_DATAQUEUE = 3;

    /**
     *  Constant indicating the Database service.
     **/
    static final int SERVICE_DATABASE = 4;

    /**
     *  Constant indicating all services.
     **/
    static final int SERVICE_ALL_SERVICES = 99;


    // The default port number.  This port number was registered
    // with the Internet Assigned Numbers Authority (http://www.iana.org)
    // on 01/29/1999.  For a complete list of registered port numbers,
    // go to http://www.isi.edu/in-notes/iana/assignments/port-numbers.
    /**
     *  Constant indicating the MEServer port.
     **/
    static final int ME_SERVER_PORT = 3470;

    
    // THE REPLY DATASTREAM TYPTES.
    
    /**
     *  Constant indicating the iSeries signon succeeded.
     **/
    static final int SIGNON_SUCCEEDED = 0x1234;

    /**
     *  Constant indicating the iSeries signon failed.
     **/
    static final int SIGNON_FAILED    = 0x1235;
                                                     
    /**
     *  Constant indicating the XML document was not registered with the MEServer.
     **/
    static final int XML_DOCUMENT_NOT_REGISTERED = 0x1236;

    /**
     *  Constant indicating the XML document was registered with the MEServer.
     **/
    static final int XML_DOCUMENT_REGISTERED = 0x1237;

    /**
     *  Constant indicating the Dataqueue action was performed successfully.
     **/
    static final int DATA_QUEUE_ACTION_SUCCESSFUL = 0x1238;

    /**
     *  Constant indicating that an exception occurred.
     **/
    static final int EXCEPTION_OCCURRED = 0x1239;

    /**
     *  Constant indicating that the ToolboxME request is not supported.
     **/
    static final int REQUEST_NOT_SUPPORTED = 0x01240;
    

    // THE REQUEST DATASTREAM TYPES.
    
    /**
     *  Constant indicating a signon request.
     **/
    static final int SIGNON = 0x1122;

    /**
     *  Constant indicating a CommandCall request.
     **/
    static final int COMMAND_CALL = 0x1123;

    /**
     *  Constant indicating a ProgramCall request.
     **/
    static final int PROGRAM_CALL = 0x1124;

    /**
     *  Constant indicating a disconnect request.
     **/
    static final int DISCONNECT = 0x1125;
    
    /**
     *  Constant indicating a data queue read request.
     **/
    static final int DATA_QUEUE_READ = 0x1126;

    /**
     *  Constant indicating a data queue write request.
     **/
    static final int DATA_QUEUE_WRITE = 0x1127;

    /**
     *  Constant indicating a data queue request performed with byte data.
     **/
    static final int DATA_QUEUE_BYTES = 0x1128;

    /**
     *  Constant indicating a data queue request performed with string data.
     **/
    static final int DATA_QUEUE_STRING = 0x1129;

    /**
     *  Constant indicating an SQL absolute request.
     **/
    static final int SQL_ABSOLUTE = 0x1350;

    /**
     *  Constant indicating an SQL query request.
     **/
    static final int SQL_QUERY = 0x1351;

    /**
     *  Constant indicating an SQL close request.
     **/
    static final int SQL_CLOSE = 0x1352;

    /**
     *  Constant indicating an SQL first request.
     **/
    static final int SQL_FIRST = 0x1353;

    /**
     *  Constant indicating an SQL lst request.
     **/
    static final int SQL_LAST = 0x1354;

    /**
     *  Constant indicating an SQL next request.
     **/
    static final int SQL_NEXT = 0x1355;

    /**
     *  Constant indicating an SQL previous request.
     **/
    static final int SQL_PREVIOUS = 0x1356;

    /**
     *  Constant indicating an SQL update request.
     **/
    static final int SQL_UPDATE = 0x1357;

    /**
     *  Constant indicating an SQL row number request.
     **/
    static final int SQL_ROW_NUMBER = 0x1358;

    /**
     *  Constant indicating an SQL row data request.
     **/
    static final int SQL_ROW_DATA = 0x1359;

    /**
     *  Constant indicating the SQL result set has been closed.
     **/
    static final int SQL_RESULT_SET_CLOSED = 0x1360;

    /**
     *  Constant indicating the SQL statement succeeded.
     **/
    static final int SQL_STATEMENT_SUCCEEDED = 0x1361;

    /**
     *  Constant indicating an SQL exception occurred.
     **/
    static final int SQL_EXCEPTION = 0x1362;

    /**
     *  Constant indicating the positioning of the SQL cursor was successful.
     **/
    static final int SQL_POSITION_CURSOR_SUCCESSFUL = 0x1363;

    /**
     *  Constant indicating the positioning of the SQL cursor failed.
     **/
    static final int SQL_POSITION_CURSOR_FAILED = 0x1364;

    static final int SQL_REQUEST_FAILED = 0x1365;

    static final int SQL_REQUEST_SUCCESSFUL = 0x1366;

    static final int NO_CONNECTION_PROPERTIES = 0x1367;
}
