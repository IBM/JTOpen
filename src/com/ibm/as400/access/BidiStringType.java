///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  BidiStringType.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * A collection of constants
 * generally used for describing the string type of bidi (bi-directional text).
 * <p>
 * The client Bidi format is usually different from the host Bidi format. For
 * example, Bidi format of MS-Windows applications is typically Logical LTR, and
 * Bidi format of System i screen applications is typically Visual LTR.
 * Therefore the data exchange between the host and the client may need Bidi
 * layout transformation. The client Bidi format for this transformation is
 * defined by "bidi string type" property, while the host Bidi format is taken
 * according to the user profile CCSID (aka host CCSID). Each CCSID has a 
 * default string type, as defined by the CDRA (Character Data Representation 
 * Architecture), which defines a set of Bidi flags. This string type 
 * is used as the host Bidi format.
 * <p>
 * By default, the value of the "bidi string type" property is 5 (Logical LTR).
 * <p>
 * The value of the the host CCSID is received from the host, and it can be
 * different for data sent from the host to the client (such as results of
 * SELECT queries), and for data sent from the client to the host (such as field
 * content of data manipulation statements, such as INSERT or UPDATE). For the
 * former, the host CCSID is used. For the latter, it is defined using
 * property "package ccsid".
 * <p>
 * Note that the default value for the property is 13488 (UCS-2), and Bidi
 * format associated with the CCSID is 10 (Logical Contextual). Therefore, by
 * default, the data sent from a client to a host is converted into Logical
 * Contextual first, and then converted into Bidi format of the host database
 * (typically Visual LTR) by a process running on the host side (receiving job).
 * As result, Arabic/Hebrew text mixed with numerals might be reordered
 * incorrectly (round-trip problem). To prevent this problem, it is recommended
 * to set "package ccsid" to the value matching the host CCSID.
 * <p>
 * The special value "system" for the "package ccsid" property forces the JDBC
 * driver to use value matching the host CCSID. This CCSID may be different for
 * different accounts, but, by default, it matches the host database CCSID;
 * therefore, in most of cases, it is recommended for usage with Bidi systems.
 * However, it is mandatory to use a Unicode CCSID, for example 1200 (UTF-16) or
 * 13488 (UCS-2), for data manipulation statements with multilingual field
 * content.
 * <p>
 * For meta-data (names of tables, columns etc.) and method setString()
 * of classes CallableStatement and PreparedStatement, "package ccsid" is not used
 * as a mediator, and sending data is converted directly to host database CCSID,
 * or CCSID specified for the column.   
 * <p>
 * Bidi layout transformation of data manipulation statements such as INSERT 
 * or UPDATE is not supported when either "package ccsid" and host CCSID are Logical, 
 * or when host CCSID is Logical RTL (62224 for Arabic and 62235 for Hebrew). 
 * For these cases, consider usage of method setString() of classes CallableStatement 
 * and PreparedStatement, or method updateString() of class ResultSet.
 * <p>
 * Bidi layout transformation of meta-data (such as tables, columns and stored procedures names) 
 * depends on property "bidi implicit reordering". 
 * If it is set to true, it is reordered according to current setting of "bidi string type". 
 * Otherwise no reordering is occurred. In current release, this feature is supported for 
 * Visual LTR CCSIDs (420 for Arabic and 424 for Hebrew) only.  
 * <p>
 * Note that "LTR" means left-to-right, "RTL" means right-to-left, and
 * "Implicit" is alias for "Logical".
 **/
public interface BidiStringType
{
    /**
     The default string type for Bidi data, according to Unicode standard, is Implicit Contextual LTR.  Note that the Toolbox has historically defaulted to Implicit LTR.
     **/
    final static int DEFAULT = 5;

    /**
     String type used when an EBCDIC/Unicode conversion is desired, but without swapping, shaping, or transformation.
     **/
    public final static int NONE = -1;

    /**
     String Type 4
     <ul>
     <li>Type of text:  Visual
     <li>Orientation:  LTR
     <li>Symmetric swapping:  No
     <li>Numeral shape:  Nominal
     <li>Text shapes:  Shaped
     </ul>
     **/
    public final static int ST4 = 4;

    /**
     String Type 5
     <ul>
     <li>Type of text:  Implicit
     <li>Orientation:  LTR
     <li>Symmetric swapping:  Yes
     <li>Numeral shape:  Nominal
     <li>Text shapes:  Nominal
     </ul>
     **/
    public final static int ST5 = 5;

    /**
     String Type 6
     <ul>
     <li>Type of text:  Implicit
     <li>Orientation:  RTL
     <li>Symmetric swapping:  Yes
     <li>Numeral shape:  Nominal
     <li>Text shapes:  Nominal
     </ul>
     **/
    public final static int ST6 = 6;

    /**
     String Type 7
     <ul>
     <li>Type of text:  Visual
     <li>Orientation:  Contextual LTR
     <li>Symmetric swapping:  No
     <li>Numeral shape:  Nominal
     <li>Text shapes:  Nominal
     </ul>
     **/
    public final static int ST7 = 7;

    /**
     String Type 8
     <ul>
     <li>Type of text:  Visual
     <li>Orientation:  RTL
     <li>Symmetric swapping:  No
     <li>Numeral shape:  Nominal
     <li>Text shapes:  Shaped
     </ul>
     **/
    public final static int ST8 = 8;

    /**
     String Type 9
     <ul>
     <li>Type of text:  Visual
     <li>Orientation:  RTL
     <li>Symmetric swapping:  Yes
     <li>Numeral shape:  Nominal
     <li>Text shapes:  Shaped
     </ul>
     **/
    public final static int ST9 = 9;

    /**
     String Type 10
     <ul>
     <li>Type of text:  Implicit
     <li>Orientation:  Contextual LTR
     <li>Symmetric swapping:  Yes
     <li>Numeral shape:  Nominal
     <li>Text shapes:  Nominal
     </ul>
     **/
    public final static int ST10 = 10;

    /**
     String Type 11
     <ul>
     <li>Type of text:  Implicit
     <li>Orientation:  Contextual RTL
     <li>Symmetric swapping:  Yes
     <li>Numeral shape:  Nominal
     <li>Text shapes:  Nominal
     </ul>
     **/
    public final static int ST11 = 11;
}
