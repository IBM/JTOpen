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

package com.ibm.as400.micro;

/**
 *  The MEConstants class defines global constants for the
 *  ToolboxME for iSeries support.
**/

interface MEConstants
{
    // THE COMPONENT REQUEST DATASTREAM TYPES.
    static final int SERVICE_COMMAND = 2;
    static final int SERVICE_DATAQUEUE = 3;
    static final int SERVICE_DATABASE = 4;
    static final int SERVICE_ALL_SERVICES = 99;
    
    // The default port number.  This port number was registered
    // with the Internet Assigned Numbers Authority (http://www.iana.org)
    // on 01/29/1999.  For a complete list of registered port numbers,
    // go to http://www.isi.edu/in-notes/iana/assignments/port-numbers.
    static final int ME_SERVER_PORT = 3470;

    // Client/Server Seed information.
    static final int ADDER_LENGTH  = 18;  // number of bytes
    static final int MASK_LENGTH = 14;  // number of bytes
    static final int ADDER_PLUS_MASK_LENGTH = ADDER_LENGTH + MASK_LENGTH;
    
    // THE REQUEST DATASTREAM TYPES.
    static final int SIGNON = 0x1122;
    static final int COMMAND_CALL = 0x1123;
    static final int PROGRAM_CALL = 0x1124;
    static final int DISCONNECT = 0x1125;
    static final int DATA_QUEUE_READ = 0x1126;
    static final int DATA_QUEUE_WRITE = 0x1127;
    static final int DATA_QUEUE_BYTES = 0x1128;
    static final int DATA_QUEUE_STRING = 0x1129;

    // THE REPLY DATASTREAM TYPTES.
    static final int SIGNON_SUCCEEDED = 0x1234;
    static final int SIGNON_FAILED    = 0x1235;
    static final int XML_DOCUMENT_NOT_REGISTERED = 0x1236;
    static final int XML_DOCUMENT_REGISTERED = 0x1237;
    static final int DATA_QUEUE_ACTION_SUCCESSFUL = 0x1238;
    static final int EXCEPTION_OCCURRED = 0x1239;
    static final int REQUEST_NOT_SUPPORTED = 0x01240;
    
    // JDBCME functions 
    // Connection functions
    static final int CONN_NEW = 0x1250;
    static final int CONN_CLOSE = 0x1251;
    static final int CONN_CREATE_STATEMENT = 0x1252;
    static final int CONN_CREATE_STATEMENT2 = 0x1253;
    static final int CONN_PREPARE_STATEMENT = 0x1254;

    static final int CONN_SET_AUTOCOMMIT = 0x1255;
    static final int CONN_SET_TRANSACTION_ISOLATION = 0x1256;
    static final int CONN_COMMIT = 0x1257;
    static final int CONN_ROLLBACK = 0x1258;

    // Statement functions
    static final int STMT_CLOSE = 0x1261;
    static final int STMT_EXECUTE = 0x1262;
    static final int STMT_GET_RESULT_SET = 0x1263;
    static final int STMT_GET_UPDATE_COUNT = 0x1264;

    // PreparedStatement functions
    static final int PREP_EXECUTE = 0x1271;

    // ResultSet functions
    static final int RS_CLOSE = 0x1281;
    static final int RS_DELETE_ROW = 0x1282;
    static final int RS_INSERT_ROW = 0x1283;
    static final int RS_NEXT = 0x1284;
    static final int RS_PREVIOUS = 0x1285;
    static final int RS_UPDATE_ROW = 0x1286;

    static final int RS_ABSOLUTE = 0x1287;
    static final int RS_AFTER_LAST = 0x1288;
    static final int RS_BEFORE_FIRST = 0x1289;
    static final int RS_FIRST = 0x1290;
    static final int RS_IS_AFTER_LAST = 0x1291;
    static final int RS_IS_BEFORE_FIRST = 0x1292;
    static final int RS_IS_FIRST = 0x1293;
    static final int RS_IS_LAST = 0x1294;
    static final int RS_LAST = 0x1295;
    static final int RS_RELATIVE = 0x1296;

    // JDBC-ME Service functions
    static final int JDBCME_DATA_TYPE_FLOW   = 0x1900;

    // Include the data flow constants here.
   // There are multiple types of data flows that the server can
   // be setup to provide.  This allows the creation of frontends 
   // of varying complexity.
   //
   // LIMITED is the only one supported today (and the default).
   // The others will be added as time/needs allow.
   static final int DATA_FLOW_ALL          = 1;
   static final int DATA_FLOW_LIMITED      = 2;
   static final int DATA_FLOW_STRINGS_ONLY = 3;
}
