///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ProxyConstants.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The ProxyConstants class defines global contants for the
proxy support.
**/
class ProxyConstants
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // The modification identifiers.   These are used as part of the
    // connect request to notify the proxy server what modification
    // the client is running with.
    static final String MOD_3                  = "mod3";
    static final String MOD_4                  = "mod4";
    static final String CURRENT_MOD            = MOD_4;



    // The default port numbers.  These port numbers were registered
    // by the Internet Assigned Numbers Authority (http://www.iana.org)
    // on 01/29/1999.  For a complete list of registered port numbers,
    // or http://www.isi.edu/in-notes/iana/assignments/port-numbers
    // for the complete list.
    static final int    PORT_NUMBER                     =  3470;
    static final int    SECURE_PORT_NUMBER              =  3471;   //$B1C


    // Impl creation flags.
    static final int     IMPL_NONE                       = 0;
    static final int     IMPL_REMOTE_OR_PROXY            = 2;
    // static final int     IMPL_NATIVE_REMOTE_OR_PROXY     = 3;



    // Listener operations.
    static final int     LISTENER_OPERATION_ADD         = 1;
    static final int     LISTENER_OPERATION_REMOVE      = 2;



    // The parameter datastream types.
    static final short   DS_BYTE_PARM                   = 25010;
    static final short   DS_SHORT_PARM                  = 25020;
    static final short   DS_INT_PARM                    = 25030;
    static final short   DS_LONG_PARM                   = 25040;
    static final short   DS_FLOAT_PARM                  = 25050;
    static final short   DS_DOUBLE_PARM                 = 25060;
    static final short   DS_BOOLEAN_PARM                = 25070;
    static final short   DS_CHAR_PARM                   = 25080;
    static final short   DS_STRING_PARM                 = 26010;
    static final short   DS_SERIALIZED_OBJECT_PARM      = 26020;
    static final short   DS_PROXY_OBJECT_PARM           = 26030;
    static final short   DS_NULL_PARM                   = 26040;
    static final short   DS_TOOLBOX_OBJECT_PARM         = 26050;

    static final short   DS_CLASS_PARM                  = 27010;



    // The request datastream types.
    static final short   DS_CONNECT_REQ                  = 11010;
    static final short   DS_CONNECT_TUNNEL_REQ           = 11015;  // @D1a
    static final short   DS_DISCONNECT_REQ               = 11020;
    static final short   DS_CONFIGURE_REQ                = 11030;
    static final short   DS_END_REQ                      = 11040;
    static final short   DS_LOAD_REQ                     = 11050;

    static final short   DS_CONSTRUCTOR_REQ              = 12010;
    static final short   DS_METHOD_REQ                   = 12020;
    static final short   DS_FINALIZE_REQ                 = 12040;
    static final short   DS_LISTENER_REQ                 = 12050;



    // The reply datastream types.
    static final short   DS_ACCEPT_REP                   = 18010;
    static final short   DS_REJECT_REP                   = 18020;
    static final short   DS_LOAD_REP                     = 18030;

    static final short   DS_RETURN_REP                   = 19010;
    static final short   DS_EXCEPTION_REP                = 20010;
    static final short   DS_EVENT_REP                    = 21010;




}

