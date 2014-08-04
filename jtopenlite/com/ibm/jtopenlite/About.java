///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  About.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
//
// Major Change Log
// Date       Description
// ---------- ---------------------------------------
// 2014.08.04 Use client NLV for remote command server. 
//            Ignore various error responses which exchanging attributes with remote
//            command server. 
//           
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite;

/**
 * This class provides information about the current version of JTOpenLite
 *
 */
public class About {
    public static String INTERFACE_NAME="jtopenlite";
    /**
     * The INTERFACE_LEVEL represents the level of the interface.  For now,
     * we just use the current date.  This date is automatically adjusted
     * each time jtopenlite.jar is built.
     */
    public static String INTERFACE_LEVEL="20140804";
    
    
}
