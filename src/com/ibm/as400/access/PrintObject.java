///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: PrintObject.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;

/**
  * The  PrintObject class is an
  * abstract base class for the various types of network print objects.
 **/

abstract public class PrintObject implements java.io.Serializable
{
    private static final String copyright = "Copyright (C) 1997-2005 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;

    // Attribute IDs in by network print objects
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x011F">3812 SCS (fonts)</A>. **/
    public static final int ATTR_3812SCS      = 0x011F;  // AT_3812SCS
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0109">Accounting code</A>. **/
    public static final int ATTR_ACCOUNT_CODE = 0x0109;  // AT_ACCOUNT_CODE
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY261">ACIF processed"</A>. **/
           static final int ATTR_ACIF         = 0x013B;  // AT_ACIF
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY1">Advanced function printing</A>. **/
    public static final int ATTR_AFP          = 0x000A;  // AT_AFP
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEYIFS_C">AFP resource integrated file system name</A>. **/
    public static final int ATTR_AFP_RESOURCE =    -12;
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY224">Advanced function printing (AFP) resource</A>. **/
    public static final int ATTR_AFPRESOURCE  = 0x011A;  // AT_AFP_RESOURCE
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0128">ASCII transparency</A>. **/
    public static final int ATTR_ASCIITRANS   = 0x0128;  // AT_ASCII_TRANS
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x00FC">Auxilary storage pool</A>. **/
    public static final int ATTR_AUX_POOL     = 0x00FC;  // AT_AUX_POOL
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x12C">Character ID</A>. **/
    public static final int ATTR_CHARID       = 0x012C;  // AT_CHAR_ID
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY261">DBCS character rotation commands</A>. **/
    public static final int ATTR_CHR_RTT_CMDS = 0x013C;  // AT_CHR_RTT_CMDS
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0133">Character set library name</A>. **/
    public static final int ATTR_CHRSET_LIB   = 0x0133;  // AT_CHARSET_LIB_NAME
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0134">Character set name</A>. **/
    public static final int ATTR_CHRSET       = 0x0134;  // AT_CHARSET_NAME
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0138">Character set point size</A>.  **/
    public static final int ATTR_CHRSET_SIZE  = 0x0138;  // AT_CHAR_POINT_SIZE
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY246">Coded font array</A>. **/
    public static final int ATTR_CODFNT_ARRAY = 0x0132;  // AT_CODED_FNT_ARRAY
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY235">OfficeVision</A>. **/
    public static final int ATTR_OFFICEVISION = 0x0125;  // AT_OFFICEVISION
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0126">Page groups</A>. **/
    public static final int ATTR_PAGE_GROUPS  = 0x0126;  // AT_PAGE_GROUPS
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY237">Page level index tags</A>. **/
    public static final int ATTR_PAGELVLIDXTAG= 0x0127;  // AT_PAGE_LVL_IDX_TAG
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY239">Record format name present in data stream</A>. **/
    public static final int ATTR_RCDFMT_DATA  = 0x0129;  // AT_RCD_FMT_IN_DATA
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY1001">Resource library name</A>. **/
           static final int ATTR_RSCLIB       = 0x00AE;  // Resource library
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY1002">Resource name</A>. **/
           static final int ATTR_RSCNAME      = 0x00AF;  // Resource name
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY1003">Resource object type</A>. **/
           static final int ATTR_RSCTYPE      = 0x00B0;  // Resource object type
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html##HDRKEY2.5">Align forms</A>. **/
    public static final int ATTR_ALIGNFORMS   = 0x00BE;  // Align forms
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY2">Align page</A>. **/
    public static final int ATTR_ALIGN        = 0x000B;  // AT_ALIGN
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY3">Allow direct print</A>. **/
    public static final int ATTR_ALWDRTPRT    = 0x000C;  // Allow direct printing
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x010A">Auxilliary storage pool device name</A>. **/
    public static final int ATTR_ASPDEVICE    = 0x010A;  // AT_ASP_DEVICE
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY134">NPS Attribute default value</A>. **/
           static final int ATTR_ATTRDEFAULT  = 0x0083;  // attribute default value
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY135">NPS Attribute high limit</A>. **/
           static final int ATTR_ATTRMAX      = 0x0084;  // attribute maximum value
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY137">NPS Attribute ID</A>. **/
           static final int ATTR_ATTRID       = 0x0085;  // attribute ID
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY138">NPS Attribute low limit</A>. **/
           static final int ATTR_ATTRMIN      = 0x0086;  // attribute minimum value
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY139">NPS Attribute possible value</A>. **/
           static final int ATTR_ATTRPOSSIBL  = 0x0087;  // possible value for attribute
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY140">NPS Attribute text description</A>. **/
           static final int ATTR_ATTRDESCRIPT = 0x0088;  // attribute description
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY89">NPS Attribute type</A>. **/
           static final int ATTR_ATTRTYPE     = 0x0089;  // attribute type
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY4">Authority</A>. **/
    public static final int ATTR_AUTHORITY    = 0x000D;  // Authority to users not on  output queue
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY5">Authority to check</A>. **/
    public static final int ATTR_AUTHCHCK     = 0x000E;  // AT_AUTHORITY_TO_CHECK
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY6">Automatically end writer</A>. **/
    public static final int ATTR_AUTOEND      = 0x0010;  // automatically end writer *YES/*NO
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0011">Back margin offset across</A>. **/
    public static final int ATTR_BKMGN_ACR    = 0x0011;  // AT_BACK_MGN_OFFSET_ACROSS
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0012">Back margin offset down</A>. **/
    public static final int ATTR_BKMGN_DWN    = 0x0012;  // AT_BACK_MGN_OFFSET_DOWN
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEYIFS_1">Back overlay integrated file system name</A>. **/
    public static final int ATTR_BACK_OVERLAY =     -1;  // Back overlay Integrated File System name
           static final int ATTR_BKOVRLLIB    = 0x0013;  // AT_BACK_OVL_LIB
           static final int ATTR_BKOVRLAY     = 0x0014;  // AT_BACK_OVL_NAME
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x011B">Barcode</A>. **/
    public static final int ATTR_BARCODE      = 0x011B;  // AT_BARCODE
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY12">Back overlay offset down</A>.**/
    public static final int ATTR_BKOVL_DWN    = 0x0015;  // AT_BACK_OVL_OFFSET_DOWN
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY11">Back overlay offset across</A>.**/
    public static final int ATTR_BKOVL_ACR    = 0x0016;  // AT_BACK_OVL_OFFSET_ACROSS
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY12.3">Between copies status</A>. **/
    public static final int ATTR_BTWNCPYSTS   = 0x00CE;  // Indicates whether the writer is between copies of multiple copy spooled file
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY12.6">Between files status</A>. **/
    public static final int ATTR_BTWNFILESTS  = 0x00CF;  // Indicates whether the writer is between files
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY13.5">Changes take effect</A>. **/
    public static final int ATTR_CHANGES      = 0x00BF;  // Changed take effect
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY53">Graphic character set</A>. **/
    public static final int ATTR_CHAR_ID      = 0x0037;  // AT_CHARID_CHAR_SET
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY13">Characters per inch</A>. **/
    public static final int ATTR_CPI          = 0x0017;  // AT_CPI
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY242">Characters per inch changes</A>. **/
    public static final int ATTR_CPI_CHANGES  = 0x012D;  // AT_CPI_CHANGES
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY16">Coded font library name</A>. **/
    public static final int ATTR_CODEDFNTLIB  = 0x0018;  // AT_CODED_FONT_LIB
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY14">Code page</A>. **/
    public static final int ATTR_CODEPAGE     = 0x0019;  // AT_CHARID_CODE_PAGE
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY223">Code page library name</A>. **/
    public static final int ATTR_CODEPAGE_NAME_LIB = 0x0117; // AT_CODE_PAGE_LIB
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY223.1">Code page name</A>. **/
    public static final int ATTR_CODEPAGE_NAME= 0x0118;  // AT_CODE_PAGE_NAME
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY223.2">Coded font point size</A>. **/
    public static final int ATTR_CODEDFONT_SIZE = 0x0119; // AT_CODE_FONT_SIZE
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY15">Coded font name</A>. **/
    public static final int ATTR_CODEDFNT     = 0x001A;  // AT_CODED_FONT_NAME
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x011C">Color</A>. **/
    public static final int ATTR_COLOR        = 0x011C;  // AT_COLOR
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x010E">Constant back overlay</A>. **/
    public static final int ATTR_CONSTBCK_OVL = 0x010E;  // AT_CONST_BACK_OVL
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY17.5">Control character</A>. **/
    public static final int ATTR_CONTROLCHAR  = 0x00C4;  // AT_CONTROL_CHARACTER
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x00F7">Convert line data</A>. **/
    public static final int ATTR_CONVERT_LINEDATA  = 0x00F7;  // AT_CONVERT_LINE_DATA
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY17">Copies</A>. **/
    public static final int ATTR_COPIES       = 0x001C;  // AT_COPIES
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY18">Copies left to produce</A>. **/
    public static final int ATTR_COPIESLEFT   = 0x001D;  // AT_COPIES_LEFT
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY191">Corner staple</A>. **/
    public static final int ATTR_CORNER_STAPLE= 0x00F8;  // AT_CORNER_STAPLE
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY19">Current page</A>. **/
    public static final int ATTR_CURPAGE      = 0x001E;  // current page
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY20">Data format</A>.**/
    public static final int ATTR_DATAFORMAT   = 0x001F;  // data format
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEYIFS_2">Data queue integrated file system name</A>. **/
    public static final int ATTR_DATA_QUEUE   =     -2;  // Data queue IFS name
           static final int ATTR_DATAQUELIB   = 0x0020;  // AT_DATA_QUEUE_LIB
           static final int ATTR_DATAQUE      = 0x0021;  // AT_DATA_QUEUE
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY23">Date file opened (created)</A>. **/
    public static final int ATTR_DATE         = 0x0022;  // AT_DATE
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY202"> </A>. **/
    public static final int ATTR_DATE_END     = 0x00FD;  // 
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY216">Date file last used</A>. **/
    public static final int ATTR_DATE_USED    = 0x010D;  // AT_DATE_FILE_USED
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY178">Date writer began processing spooled file</A>. **/
    public static final int ATTR_DATE_WTR_BEGAN_FILE = 0x00EA;  // date writer began file
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY179">Date writer completed processing spooled filed</A>. **/
    public static final int ATTR_DATE_WTR_CMPL_FILE = 0x00EB;  // date writer finished file
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0140">Days until file expires</A>. **/
    public static final int ATTR_DAYS_UNTIL_EXPIRE = 0x0140;   // AT_DAYS_UNTIL_EXPIRE
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY24">User specified DBCS data</A>. **/
    public static final int ATTR_DBCSDATA     = 0x0099; // contains DBCS character set data
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY25">DBCS extension characters</A>.  **/
    public static final int ATTR_DBCSEXTENSN  = 0x009A; // process DBCS extension characters
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY26">DBCS character rotation</A>.  **/
    public static final int ATTR_DBCSROTATE   = 0x009B; // rotate DBCS characters
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY221">DBCS coded font library name</A>.  **/
    public static final int ATTR_DBCS_FNT_LIB = 0x0112; // AT_DBCS_FONT_LIB
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY221.1">DBCS coded font name</A>.  **/
    public static final int ATTR_DBCS_FNT     = 0x0113; // AT_DBCS_FONT_LIB
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY221.2">DBCS coded font point size</A>.  **/
    public static final int ATTR_DBCS_FNT_SIZE= 0x0114; // AT_DBCS_FONT_LIB
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY27">DBCS characters per inch</A>.  **/
    public static final int ATTR_DBCSCPI      = 0x009C; // DBCS CPI
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY28">DBCS SO/SI spacing</A>. **/
    public static final int ATTR_DBCSSISO     = 0x009D; // DBCS SI/SO positioning
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x011D">Data description specifications (DDS)</A>. **/
    public static final int ATTR_DDS          = 0x011D; // AT_DDS
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x010C">Decimal format</A>. **/
    public static final int ATTR_DECIMAL_FMT  = 0x010C;  // AT_DECIMAL_FORMAT
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY29">Defer write</A>. **/
    public static final int ATTR_DFR_WRITE    = 0x0023;  // defer write
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY32">Delete file after sending</A>. **/
    public static final int ATTR_DELETESPLF   = 0x0097; // delete file after sending
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY113">Text description</A>. **/
    public static final int ATTR_DESCRIPTION  = 0x006D;  // text description
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY34">Destination type</A>. **/
    public static final int ATTR_DESTINATION  = 0x0025;  // destination type
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY33">Destination option</A>. **/
    public static final int ATTR_DESTOPTION   = 0x0098; // destinaton option sent
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY35">Device class</A>. **/
    public static final int ATTR_DEVCLASS     = 0x0026;  // device class
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY36">Device model</A>.**/
    public static final int ATTR_DEVMODEL     = 0x0027;  // device model
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY36.5">Device status</A>.**/
    public static final int ATTR_DEVSTATUS    = 0x00C7;  // device status
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY37">Device type</A>.**/
    public static final int ATTR_DEVTYPE      = 0x0028;  // device type
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY38">Display any file</A>. **/
    public static final int ATTR_DISPLAYANY   = 0x0029;  // users can display any file on queue
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY39">Drawer for separators</A>. **/
    public static final int ATTR_DRWRSEP      = 0x002A;  // drawer to use for separators
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY88">Print on both sides</A>. **/
    public static final int ATTR_DUPLEX       = 0x0055;  // print on both sides of paper
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x011E">Double wide characters</A>. **/
    public static final int ATTR_DOUBLEWIDE   = 0x011E;  // AT_DOUBLE_WIDE_CHAR
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x012E">Drawer change</A>. **/
    public static final int ATTR_DRAWERCHANGE = 0x012E;  // AT_DRAWER_CHANGE
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY192">Edge stitch reference edge</A>. **/
    public static final int ATTR_EDGESTITCH_REF= 0x00EE; // edgestitch reference edge 
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY194">Edge stitch number of staples</A>. **/
    public static final int ATTR_EDGESTITCH_NUMSTAPLES= 0x00F0;  // edgesticth number stapes
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY193">Offset from edge stitch reference edge</A>. **/
    public static final int ATTR_EDGESTITCH_REFOFF = 0x00EF;  // edgestitch reference offset 
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY195">Edge stitch staple offset</A>. **/
           static final int ATTR_EDGESTITCH_STPL_OFFSET_INFO   = 0x00F1;  // edgestitch staple offset
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY40">Ending page</A>.**/
    public static final int ATTR_ENDPAGE      = 0x002B;  // ending page number to print
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY40.5">End pending status</A>. **/
    public static final int ATTR_ENDPNDSTS    = 0x00CC;  // indicates whether an end writer command has been issued for this writer
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY168">Envelope source</A>. **/
    public static final int ATTR_ENVLP_SOURCE = 0x00D3;  // envelope source
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0141">Spooled file expiration date</A>. **/
    public static final int ATTR_EXPIRATION_DATE= 0x0141; // Spooled file expiration date
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY87">Print fidelity</A>. **/
    public static final int ATTR_FIDELITY     = 0x0054;  // the error handling when printing
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0120">Field outlining</A>. **/
    public static final int ATTR_FIELD_OUTLIN = 0x0120;  // AT_FIELD_OUTLIN
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY41">File separators</A>. **/
    public static final int ATTR_FILESEP      = 0x002C;  // number of file separators
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY42">Fold records</A>. **/
    public static final int ATTR_FOLDREC      = 0x002D;  // wrap text to next line
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x012F">Font changes</A>. **/
    public static final int ATTR_FONT_CHANGES = 0x012F;  // AT_FONT_CHANGES 
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY43">Font identifier</A>. **/
    public static final int ATTR_FONTID       = 0x002E;  // Font identifier to use (default)
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x010B">Font resolution for formatting</A>. **/
    public static final int ATTR_FONTRESFMT   = 0x010B;  // AT_FONT_RES_FORMAT  
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEYIFS_3">Form definition integrated file system name</A>. **/
    public static final int ATTR_FORM_DEFINITION =  -3;  // Form definition IFS name
           static final int ATTR_FORMDEFLIB   = 0x00B7;  // Form definition library name
           static final int ATTR_FORMDEF      = 0x00B6; // Form definition name
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY44">Form feed</A>. **/
    public static final int ATTR_FORMFEED     = 0x002F;  // type of paperfeed to be used
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY45">Form type</A>. **/
    public static final int ATTR_FORMTYPE     = 0x0030;  // name of the form to be used
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY46">Form type message option</A>. **/
    public static final int ATTR_FORMTYPEMSG  = 0x0043;  // form type message option
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY47">Front margin offset across</A>. **/
    public static final int ATTR_FTMGN_ACR    = 0x0031;  // front margin across
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY48">Front margin offset down</A>. **/
    public static final int ATTR_FTMGN_DWN    = 0x0032;  // front margin down
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEYIFS_4">Front overlay integrated file system name</A>. **/
    public static final int ATTR_FRONT_OVERLAY=     -4;  // Front overlay IFS name
           static final int ATTR_FTOVRLLIB    = 0x0033;  // Front overlay library name
           static final int ATTR_FTOVRLAY     = 0x0034;  // Front overlay name
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY51">Front overlay offset across</A>. **/
    public static final int ATTR_FTOVL_ACR    = 0x0036;  // front overlay offset across
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY52">Front overlay offset down</A>. **/
    public static final int ATTR_FTOVL_DWN    = 0x0035;  // front overlay offset down
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0121">Graphics</A>. **/
    public static final int ATTR_GRAPHICS     = 0x0121;  // AT_GRAPHICS 
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0122">Graphics token</A>. **/
    public static final int ATTR_GRAPHICS_TOK = 0x0122;  // AT_GRAPHICS_TOK
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY233">Group level index tags</A>. **/
    public static final int ATTR_GRPLVL_IDXTAG= 0x0123;  // AT_GROUP_LVL_IDX_TAG
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY54.5">Held status</A>. **/
    public static final int ATTR_HELDSTS      = 0x00D0;  // Indicates whether the writer is held
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0124">Highlight</A>. **/
    public static final int ATTR_HIGHLIGHT    = 0x0124;  // AT_HIGHLIGHT
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY55">Hold spool file</A>.**/
    public static final int ATTR_HOLD         = 0x0039;  // Hold the spool file
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY55.5">Hold pending status</A>. **/
    public static final int ATTR_HOLDPNDSTS   = 0x00D1;  // Indicates whether a hold writer command has been issued for this writer
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY125">When to hold file</A>. **/
    public static final int ATTR_HOLDTYPE     = 0x009E;  // When to hold spooled file
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY206">Image configuration</A>.  **/
    public static final int ATTR_IMGCFG       = 0x0100;  // Image Configuration
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY167">Initialize printer</A>. **/
           static final int ATTR_INITIALIZE_PRINTER = 0x00D2; // initialize the printer
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY56">Internet address</A>. **/
    public static final int ATTR_INTERNETADDR = 0x0094; // internet address
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY222">IPDS pass-through</A>. **/
    public static final int ATTR_IPDSPASSTHRU = 0x0116;  // AT_IPDS_PASSTHRU
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY180">IPP attributes ccsid</A>. **/
    public static final int ATTR_IPP_ATTR_CCSID= 0x00E1; // AT_IPP_ATTR_CCSID
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY181">IPP job ID</A>. **/
    public static final int ATTR_IPP_JOB_ID   = 0x00E4;  // IPP Job ID
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY182">IPP job name</A>. **/
    public static final int ATTR_IPP_JOB_NAME = 0x00E6;  // IPP Job Name
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY183">IPP job name natural language</A>. **/
    public static final int ATTR_IPP_JOB_NAME_NL= 0x00E7;  // IPP Job Name NL
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY184">IPP job originating user name</A>. **/
    public static final int ATTR_IPP_JOB_ORIGUSER= 0x00E8;  // IPP Job Originating usernam
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY185">IPP job originating user name natural language</A>. **/
    public static final int ATTR_IPP_JOB_ORIGUSER_NL= 0x00E9;  // Originating user NL 
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY186">IPP printer name</A>. **/
    public static final int ATTR_IPP_PRINTER_NAME= 0x00E5;  // IPP Printer URI name
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY180.5">IPP natural language</A>. **/
    public static final int ATTR_IPP_ATTR_NL  = 0x00FA;  // IPP natural language
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY57">Job name</A>. **/
    public static final int ATTR_JOBNAME      = 0x003B;  // name of the job that created file
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY58">Job number</A>.**/
    public static final int ATTR_JOBNUMBER    = 0x003C;  // number of the job that created file
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY59">Job separators</A>. **/
    public static final int ATTR_JOBSEPRATR   = 0x003D;  // number of job separators
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x00FB">Job system name</A>. **/
    public static final int ATTR_JOBSYSTEM    = 0x00FB;  // name of the system where the job that created this spooled file ran
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY60">Job user</A>. **/
    public static final int ATTR_JOBUSER      = 0x003E;  // name of the user that created file
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY54">Justification</A>. **/
    public static final int ATTR_JUSTIFY      = 0x0038;  // justification
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY61">Last page printed</A>. **/
    public static final int ATTR_LASTPAGE     = 0x003F;  // last page that printed
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY63">Library name</A>. **/
    public static final int ATTR_LIBRARY      = 0x000F;  // library name
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY65.5">Line spacing</A>. **/
    public static final int ATTR_LINESPACING  = 0x00C3;  // line spacing
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY64">Lines per inch</A>. **/
    public static final int ATTR_LPI          = 0x0040;  // AT_LPI
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0130">Lines per inch changes</A>. **/
    public static final int ATTR_LPI_CHANGES  = 0x0130;  // AT_LPI_CHANGES
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY199">Maximum jobs per client</A>. **/
    public static final int ATTR_MAX_JOBS_PER_CLIENT = 0x00DE;  // 
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY66">Maximum records</A>. **/
    public static final int ATTR_MAXRCDS      = 0x0042;  // Maximum records
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY67">Measurement method</A>. **/
    public static final int ATTR_MEASMETHOD   = 0x004F;  // Measurement method (*ROWCOL or *UOM)
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY65">Manufacturer type and model</A>. **/
    public static final int ATTR_MFGTYPE      = 0x0041;  // Manufacturer's type & model
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEYIFS_5">Message queue integrated file system name</A>. **/
    public static final int ATTR_MESSAGE_QUEUE=     -5;  // Message Queue IFSPath
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY70">Message queue library name</A>.  **/
           static final int ATTR_MSGQUELIB    = 0x0044;  // Message queue library
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY71">Message queue name</A>. **/
           static final int ATTR_MSGQUE       = 0x005E;  // Message queue
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY68">Message help</A>. **/
    public static final int ATTR_MSGHELP      = 0x0081;  // Message help text
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY69">Message ID</A>. **/
    public static final int ATTR_MSGID        = 0x0093;  // Message ID
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY72">Message reply</A>. **/
    public static final int ATTR_MSGREPLY     = 0x0082;  // Message reply
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY74A">Message severity</A>. **/
    public static final int ATTR_MSGSEV       = 0x009F;  // Message severity
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY73">Message text</A>. **/
    public static final int ATTR_MSGTEXT      = 0x0080;  // message text
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY74">Message type</A>. **/
    public static final int ATTR_MSGTYPE      = 0x008E;  // message type
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY200">Multi-item reply capability</A>. **/
    public static final int ATTR_MULTI_ITEM_REPLY = 0x00DC;  // multiple item reply capable
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY85">Pages per side</A>. **/
    public static final int ATTR_MULTIUP      = 0x0052;  // logical pages per physical side
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY76.5">Net ID where file originated</A>. **/
    public static final int ATTR_NETWORK      = 0x00BD;  // network ID where file originated
           static final int ATTR_NLV_ID       = 0x00B4;  // NLV ID (ie: "2924");
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY8A">NPS CCSID</A>. **/
    public static final int ATTR_NPSCCSID     = 0x008A;  // server CCSID
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY8B">NPS object</A>. **/
           static final int ATTR_NPSOBJECT    = 0x008B;  // Object ID
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY8C">NPS object action</A>. **/
           static final int ATTR_NPSACTION    = 0x008C;  // Action ID
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY8D">NPS level</A>. **/
    public static final int ATTR_NPSLEVEL     = 0x008D;  // server code level
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY75">Number of bytes to read/write</A>. **/
    public static final int ATTR_NUMBYTES     = 0x007D;  // number of bytes to read/write
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY174">Number of bytes available in a stream or spooled file</A>. **/
    public static final int ATTR_NUMBYTES_SPLF= 0x00D9;  // number of bytes available in a stream/spooled file
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY76">Number of files</A>. **/
    public static final int ATTR_NUMFILES     = 0x0045;  // total spooled files no output queue
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY77">Number of writers started to queue</A> **/
    public static final int ATTR_NUMWRITERS   = 0x0091;  // number of writers started to queue
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY259">Number of user resource library list entries</A>. **/
    public static final int ATTR_NUMRSC_LIB_ENT= 0x0139; // AT_NUMRSC_LIB_ENT
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY771">Object extended attribute</A>. **/
    public static final int ATTR_OBJEXTATTR   = 0x00B1;  // Object extended attribute
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY77.3">On job queue status</A>. **/
    public static final int ATTR_ONJOBQSTS    = 0x00CD;  // indicates whether the writer is on a job queue and therefore is not currently running
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY772">Open time commands</A>. **/
    public static final int ATTR_OPENCMDS     = 0x00A0;  // Open time commands on read (for SCS)
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY78">Operator controlled</A>. **/
    public static final int ATTR_OPCNTRL      = 0x0046;  // operator controlled
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY79">Order of files on queue</A>. **/
    public static final int ATTR_ORDER        = 0x0047;  // order on queue (sequence) - *FIFO, *JOBNBR
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY80">Output priority</A>. **/
    public static final int ATTR_OUTPTY       = 0x0048;  // output priority
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY253">OS/400-create AFPDS</A>.  **/
    public static final int ATTR_OS4_CRT_AFP  = 0x0135;  // AT_OS4_CRT_AFP
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY83.5">Output bin</A>. **/
    public static final int ATTR_OUTPUTBIN    = 0x00C0;  // output bin
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEYIFS_6">Output queue integrated file system name</A>. **/
    public static final int ATTR_OUTPUT_QUEUE =     -6;  // Output queue IFS name
           static final int ATTR_OUTQUELIB    = 0x0049;  // Output queue library name
           static final int ATTR_OUTQUE       = 0x004A;  // Output queue name
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY83">Output queue status</A>. **/
    public static final int ATTR_OUTQSTS      = 0x004B;  // output queue status
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY87.5">Overall status of printer</A>. **/
    public static final int ATTR_OVERALLSTS   = 0x00C8;  // overall status of printer
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY84">Overflow line number</A>. **/
    public static final int ATTR_OVERFLOW     = 0x004C;  // overflow line number
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY171">Page at a time</A>. **/
    public static final int ATTR_PAGE_AT_A_TIME= 0x00D6; // page at a time
    /* Lines Per Page is 0x004D and isn't used anymore. Use 0x004E instead. */
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEYIFS_D">Page definition integrated file system name</A>. **/
    public static final int ATTR_PAGE_DEFINITION=  -13;  // Page definition IFS name 
           static final int ATTR_PAGDFNLIB    = 0x00F5;  // Page definition library name
           static final int ATTR_PAGDFN       = 0x00F6;  // Page definition name
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY62">Length of page</A>. **/
    public static final int ATTR_PAGELEN      = 0x004E;  // page length in Units of Measurement
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY172">Page number</A>. **/
    public static final int ATTR_PAGENUMBER   = 0x00D7;  // page number
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY115">Total pages</A>. **/
    public static final int ATTR_PAGES        = 0x006F;  // number of pages in spool file
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY175">Total pages estimated</A>. **/
    public static final int ATTR_PAGES_EST    = 0x00DA;  // indicates if the number of pages is estimated
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY31">Page rotation</A>. **/
    public static final int ATTR_PAGRTT       = 0x0024;  // Page rotation
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY31">Page rotate</A>.  **/
    public static final int ATTR_PAGE_ROTATE  = 0x012A;  // AT_PAGE_ROTATE
    // Chars per Line is 0x0050 and isn't used anymore - use 0x004E instead
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY126">Width of page</A>. **/
    public static final int ATTR_PAGEWIDTH    = 0x0051;  // width of page in Units of Measure
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY851">Pel density</A>. **/
    public static final int ATTR_PELDENSITY   = 0x00B2;  // Font Pel Density "1"=240;"2"=300;
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY169">Paper source 1</A>. **/
    public static final int ATTR_PAPER_SOURCE_1= 0x00D4; // paper source 1
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY170">Paper source 2</A>. **/
    public static final int ATTR_PAPER_SOURCE_2= 0x00D5; // paper source 2
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY86">Point size</A>. **/
    public static final int ATTR_POINTSIZE    = 0x0053;  // the default font's point size
           static final int ATTR_PRECOMPUTE_NUMBYTES = 0x00B8;  // Precompute Number of bytes on open
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY218.1">Program that opened file library name</A>. **/
    public static final int ATTR_PGM_OPN_LIB  = 0x010F;  // AT_PGM_OPN_LIB
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY218.2">Program that opened file name</A>. **/
    public static final int ATTR_PGM_OPN_FILE = 0x0110;  // AT_PGM_OPN_FILE
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY89">Print quality</A>. **/
    public static final int ATTR_PRTQUALITY   = 0x0056;  // Print quality
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY90">Print sequence</A>. **/
    public static final int ATTR_PRTSEQUENCE  = 0x0057;  // Print sequence
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY91">Print text</A>. **/
    public static final int ATTR_PRTTEXT      = 0x0058;  // Text printed at bottom of each page
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY92">Printer (device name)</A>. **/
    public static final int ATTR_PRINTER      = 0x0059;  // Printer device name
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY97.5">Printer assigned</A>. **/
    public static final int ATTR_PRTASSIGNED  = 0x00BA;  // Printer assigned
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY93">Printer device type</A>. **/
    public static final int ATTR_PRTDEVTYPE   = 0x005A;  // Printer dev type (data stream type (*SCS, *AFPDS, etc))
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEYIFS_7">Printer file integrated file system name</A>. **/
    public static final int ATTR_PRINTER_FILE =     -7;  // Printer file IFS name
           static final int ATTR_PRTFLIB      = 0x005B;  // Printer file library name
           static final int ATTR_PRTFILE      = 0x005C;  // Printer file name
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY207">Color supported</A>. **/
    public static final int ATTR_PUBINF_COLOR_SUP = 0x0101; //Color supported
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY208">Pages per minute (color)</A>. **/
    public static final int ATTR_PUBINF_PPM_COLOR = 0x0102; //Pages per minute (color)
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY212">Data stream supported</A>. **/
    public static final int ATTR_PUBINF_DS    = 0x0106;  //Data Stream supported
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY209">Pages per minute (monochrome)</A>. **/
    public static final int ATTR_PUBINF_PPM   = 0x0103;  //Pages per minute (monochrome)
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY210">Duplex supported</A>. **/
    public static final int ATTR_PUBINF_DUPLEX_SUP = 0x0104; //Duplex supported
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY211">Location description</A>. **/
    public static final int ATTR_PUBINF_LOCATION = 0x0105; //Location description
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY205">Remote location name</A>. **/
    public static final int ATTR_RMTLOCNAME   = 0x00FF;  // Remote location name
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY96">Printer queue</A>. **/
    public static final int ATTR_RMTPRTQ      = 0x005D;  // Remote print queue used on SNDTCPSPLF
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY97">Record length</A>. **/
    public static final int ATTR_RECLENGTH    = 0x005F;  // record length
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY102.5">Reduce output</A>. **/
    public static final int ATTR_REDUCE       = 0x00C2;  // Reduce output
           static final int ATTR_RESOURCE_AVAIL= 0x00B3; // resource is available
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY98">Remote system name</A>. **/
    public static final int ATTR_RMTSYSTEM    = 0x0060;  // remote system name
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY99">Replace unprintable characters</A>. **/
    public static final int ATTR_RPLUNPRT     = 0x0061;  // replace uNPrintable characters
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY100">Replacement character</A>. **/
    public static final int ATTR_RPLCHAR      = 0x0062;  // character to replace uNPrintables with
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY101">Restart printing</A>. **/
    public static final int ATTR_RESTART      = 0x0063;  // where to restart printing at
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY197">Saddle stitch number of staples</A>. **/
    public static final int ATTR_SADDLESTITCH_NUMSTAPLES = 0x00F3;  // edgesticth reference
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY196">Saddle stitch reference edge</A>. **/
    public static final int ATTR_SADDLESTITCH_REF= 0x00F2;  // saddlesticth reference
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY198">Saddle stitch staple offset</A>. **/
    public static final int ATTR_SADDLESTITCH_STPL_OFFSEINFO = 0x00F4;  // saddle stitch staple offset
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0064">Save spooled file after written</A>. **/
    public static final int ATTR_SAVE         = 0x0064;  // Save spooled file after written
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0142">Save command</A>. **/
    public static final int ATTR_SAVE_COMMAND = 0x0142;  // Save command
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0143">Save device</A>. **/
    public static final int ATTR_SAVE_DEVICE  = 0x0143;  // Save device
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEYIFS_14">Save file integrated file system name</A>. **/
    public static final int ATTR_SAVE_FILE    =    -14;  // Save file IFSPath
           static final int ATTR_SAVEFILE     = 0x0145;  // Save file name
           static final int ATTR_SAVEFILELIB  = 0x0144;  // Save file library name
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0146">Save label</A>. **/
    public static final int ATTR_SAVE_LABEL   = 0x0146;  // Save label
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0147">Save sequence number</A>. **/
    public static final int ATTR_SAVE_SEQUENCE_NUMBER = 0x0147;  // Save sequence number
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0148">Save volume format</A>. **/
    public static final int ATTR_SAVE_VOLUME_FORMAT = 0x0148; // Save volume format
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x0149">Save volume ID</A>. **/
    public static final int ATTR_SAVE_VOLUME_ID = 0x0149; // Save volume ID
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY111">File available</A>. **/
    public static final int ATTR_SCHEDULE     = 0x006B;  // when available to the writer
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY116">Transform SCS to ASCII</A>. **/
    public static final int ATTR_SCS2ASCII    = 0x0071;  // transform SCS to ASCII
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY103">Seek offset</A>. **/
    public static final int ATTR_SEEKOFF      = 0x007E;  // seek offset
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY104">Seek origin</A>. **/
    public static final int ATTR_SEEKORG      = 0x007F;  // seek origin
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY105">Send priority</A>. **/
    public static final int ATTR_SENDPTY      = 0x0065;  // send priority
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY1051">Separator page</A>. **/
    public static final int ATTR_SEPPAGE      = 0x00A1;  // Print banner page or not
           static final int ATTR_SPLFSENDCMD  = 0x0092;  // spooled file send command
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY106">Source drawer</A>. **/
    public static final int ATTR_SRCDRWR      = 0x0066;  // source drawer
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY213">Source code page conversion</A>. **/
    public static final int ATTR_SRC_CODEPAGE = 0x0107;  // Source code page conversion
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY107">Spool the data</A>. **/
    public static final int ATTR_SPOOL        = 0x0067;  // spool the data
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY108">Spooled file name</A>. **/
    public static final int ATTR_SPOOLFILE    = 0x0068;  // spool file name
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY187">Spooled file creation authentication method</A>. **/
    public static final int ATTR_SPLF_AUTH_METHOD = 0x00E3;  // Spooled file creation auth method
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY260">Spooled file creator</A>. **/
    public static final int ATTR_SPLF_CREATOR = 0x013A;  // Spooled file creator
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x014A">Spooled file restored date</A>. **/
    public static final int ATTR_SPLF_RESTORED_DATE = 0x014A;  // Spooled file restored date
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x014B">Spooled file restored time</A>. **/
    public static final int ATTR_SPLF_RESTORED_TIME = 0x014B;  // Spooled file restored time
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x014C">Spooled file saved date</A>. **/
    public static final int ATTR_SPLF_SAVED_DATE = 0x014C;  // Spooled file saved date
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY0x014D">Spooled file saved time</A>. **/
    public static final int ATTR_SPLF_SAVED_TIME = 0x014D;  // Spooled file saved time
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY255">Spooled file size</A>. **/
    public static final int ATTR_SPLF_SIZE    = 0x0136;  // AT_SPLF_SIZE
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY256">Spooled file size multiplier</A>.   **/
    public static final int ATTR_SPLF_SIZE_MULT = 0x0137; // AT_SPLF_SIZE_MULT
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY188">Spooled file creation security method</A>. **/
    public static final int ATTR_SPLF_SECURITY_METHOD= 0x00E2;  // IPP Attributes-charset
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY109">Spooled file number</A>. **/
    public static final int ATTR_SPLFNUM      = 0x0069;  // spool file number
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY110">Spooled file status</A>. **/
    public static final int ATTR_SPLFSTATUS   = 0x006A;  // spool file status
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY1061">Spool SCS</A>. **/
    public static final int ATTR_SPLSCS       = 0x00AD;  // Spool SCS attr on splfile
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY112">Starting page</A>. **/
    public static final int ATTR_STARTPAGE    = 0x006C;  // starting page to print
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY121.5">Started by</A>. **/
    public static final int ATTR_STARTEDBY    = 0x00C5;  // started by user
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY122.5">System where file originated</A>. **/
    public static final int ATTR_SYSTEM       = 0x00BC;  // system where file originated
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY250">System driver program name</A>. **/
    public static final int ATTR_SYS_DRV_PGM  = 0x0131;  // System driver program name
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY214">Target code page conversion</A>. **/
    public static final int ATTR_TGT_CODEPAGE = 0x0108;  // Target code page converstion
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY114">Time file opened (created)</A>. **/
    public static final int ATTR_TIME         = 0x006E;  // time spooled file was opened (created)
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY204"> </A>. **/
    public static final int ATTR_TIME_END     = 0x00FE;  
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY189">Time writer began procesing spooled file</A>. **/
    public static final int ATTR_TIME_WTR_BEGAN_FILE = 0x00EC;  // time writer began file
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY190">Time writer completed processing spooled file</A>. **/
    public static final int ATTR_TIME_WTR_CMPL_FILE = 0x00ED;  // time writer finished file
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY120">User ID</A>. **/
    public static final int ATTR_TOUSERID     = 0x0075;  // user id to send spool file to
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY121">User ID address</A>. **/
    public static final int ATTR_TOADDRESS    = 0x0076;  // address of user to send file to
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY240">TRC for 1403</A>. **/
    public static final int ATTR_TRC1403      = 0x012B;  // AT_TRC_1403
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY117">Unit of measure</A>. **/
    public static final int ATTR_UNITOFMEAS   = 0x0072;  // unit of measure
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY118">User comment</A>. **/
    public static final int ATTR_USERCMT      = 0x0073;  // user comment
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY119">User data</A>. **/
    public static final int ATTR_USERDATA     = 0x0074;  // user data
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY1191">User defined data</A>. **/
    public static final int ATTR_USRDEFDATA   = 0x00A2;  // User defined data
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY130.5">User defined file</A>. **/
    public static final int ATTR_USRDEFFILE   = 0x00C6;  // User defined file
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY1192">User defined option(s)</A>. **/
    public static final int ATTR_USRDEFOPT    = 0x00A3;  // User defined options
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEYIFS_9">User defined object integrated file system name</A>. **/
    public static final int ATTR_USER_DEFINED_OBJECT= -9;  // User defined object IFS name
           static final int ATTR_USRDEFOBJLIB = 0x00A4;  // User defined object library
           static final int ATTR_USRDEFOBJ    = 0x00A5;  // User defined object
           static final int ATTR_USRDEFOBJTYP = 0x00A6;  // User defined object type
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY221">User defined text</A>. **/
    public static final int ATTR_USER_DFN_TXT = 0x0115;  // AT_USER_DFN_TEXT
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY219">User generated data stream</A>. **/
    public static final int ATTR_USERGEN_DATA = 0x0111;  // AT_USER_GEN_DATA
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEYIFS_A">User transform program integrated file system name</A>. **/
    public static final int ATTR_USER_TRANSFORM_PROG = -10;  // user transform program IFSPath
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY1211">User transform program library</A>. **/
           static final int ATTR_USRTFMLIB    = 0x00A7;  // User transform program library
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY1212">User transform program name</A>. **/
           static final int ATTR_USRTFM       = 0x00A8;  // User transform program
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY1193">User driver program data</A>. **/
    public static final int ATTR_USRDRVDATA   = 0x00A9;  // User driver program data
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEYIFS_B">User driver program integrated file system name</A>. **/
    public static final int ATTR_USER_DRIVER_PROG = -11; // user driver program IFSPath
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY1194">User driver program library</A>. **/
           static final int ATTR_USRDRVLIB    = 0x00AA;  // User driver program library
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY1195">User driver program name</A>. **/
           static final int ATTR_USERDRV      = 0x00AB;  // User driver program
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY173">Viewing fidelity</A>. **/
    public static final int ATTR_VIEWING_FIDELITY= 0x00D8; // viewing fidelity
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY252"<User resource library list</A>. **/
    public static final int ATTR_RSC_LIB_LIST = 0x00F9;  // AT_USER_RESOURCE_LIBL
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY122">VM/MVS Class</A>. **/
    public static final int ATTR_VMMVSCLASS   = 0x0077;  //  VM/MVS SYSOUT class
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEYIFS_8">Workstation customization object integrated file system name</A>. **/
    public static final int ATTR_WORKSTATION_CUST_OBJECT = -8;  // Workstation Cust. obj IFSPath
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY126.5">Writing status</A>. **/
    public static final int ATTR_WRTNGSTS     = 0x00BB;  // indicates whether the printer is in writing status
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY127">Workstation customizing object name</A>. **/
           static final int ATTR_WSCUSTMOBJ   = 0x0095;  // workstation customizing object
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY128">Workstation customizing object library</A>. **/
           static final int ATTR_WSCUSTMOBJL  = 0x0096;  // workstation customizing object library
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY128.2">Waiting for data status</A>. **/
    public static final int ATTR_WTNGDATASTS  = 0x00CB;  // indicates whether the writer has written all the
                                                         // data currently in the spooled file and is waiting
                                                         // for more data
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY128.4">Waiting for device status</A>. **/
    public static final int ATTR_WTNGDEVSTS   = 0x00C9;  // indicates whether the writer is waiting to get the
                                                         // device from a job that is printing directly to the printer
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY128.6">Waiting for message status</A>. **/
    public static final int ATTR_WTNGMSGSTS   = 0x00CA;  // indicates whether the writer is wating for a reply
                                                         // to an inquiry message
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY123">When to automatically end writer</A>. **/
    public static final int ATTR_WTRAUTOEND   = 0x0078;  // when to automatically end writer
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY124">When to end writer</A>. **/
    public static final int ATTR_WTREND       = 0x0090;  // when to end the writer
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY551">Initialize the writer</A>. **/
    public static final int ATTR_WTRINIT      = 0x00AC;  // When to initialize the writer
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY129">Writer job name</A>.  **/
    public static final int ATTR_WTRJOBNAME   = 0x0079;  // AT_WTR_JOB_NAME
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY130">Writer job number</A>. **/
    public static final int ATTR_WTRJOBNUM    = 0x007A;  // AT_WTR_JOB_NUMBER
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY131">Writer job status</A>. **/
    public static final int ATTR_WTRJOBSTS    = 0x007B;  // AT_WTR_JOB_STATUS
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY132">Writer job user name</A>. **/
    public static final int ATTR_WTRJOBUSER   = 0x007C;  // AT_WTR_JOB_USER
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY133">Starting page</A>. **/
    public static final int ATTR_WTRSTRPAGE   = 0x008F;  // AT_WTR_STARTING_PAGE
    /** <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrintAttributes.html#HDRKEY166">Writer started</A>. **/
    public static final int ATTR_WTRSTRTD     = 0x00C1;  // indicates whether the writer is started


    // KEEP THIS CURRENT ***** KEEP THIS CURRENT ***** KEEP THIS CURRENT
    // KEEP THIS CURRENT ***** KEEP THIS CURRENT ***** KEEP THIS CURRENT
    // KEEP THIS CURRENT ***** KEEP THIS CURRENT ***** KEEP THIS CURRENT
    static final int                    MAX_ATTR_ID = 0x014D;  // last attribute ID

    static final String                 EMPTY_STRING = "";
    private static final String         SYSTEM = "system";

    NPCPAttribute                       attrs;
    private NPCPID                      cpID_;
    private int                         objectType_;
    private AS400                       system_;

    transient PrintObjectImpl           impl_;

    // These instance variables are not persistent.
    transient PropertyChangeSupport     changes;
    transient VetoableChangeSupport     vetos;



    PrintObject(NPCPID idCodePoint,
                NPCPAttribute cpAttrs,
                int type)
    {

        cpID_ = idCodePoint;
        attrs = cpAttrs;
        objectType_ = type;
        system_ = null;
        initializeTransient();
    }



    PrintObject(AS400 system,
                NPCPID idCodePoint,
                NPCPAttribute cpAttrs,
                int type)
    {
        this(idCodePoint, cpAttrs, type);

        if( system == null )
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null");
            throw new NullPointerException("system");
        }
        system_ = system;
    }



    /**
     * Adds the specified PropertyChange listener to receive
     * PropertyChange events from this print object.
     *
     * @see #removePropertyChangeListener
     * @param listener The PropertyChange listener.
     **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        changes.addPropertyChangeListener(listener);
    }



    /**
     * Adds the specified VetoableChange listener to receive
     * VetoableChange events from this print object.
     *
     * @see #removeVetoableChangeListener
     * @param listener The VetoableChange listener.
     **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        vetos.addVetoableChangeListener(listener);
    }



    /**
     * The sub classes have default constructors implemented
     * for JavaBean support in visual builders. We need to
     * check the run time state of the object. The sub classes
     * will add additional checks by having their own
     * checkRunTimeState(), but will call super.checkRunTimeState()
     * to get this check.
     **/
    void checkRunTimeState()
    {
        if( getSystem() == null )
        {
            Trace.log(Trace.ERROR, "Parameter 'system' has not been set.");
            throw new ExtendedIllegalStateException(
              "system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
    }



    // Chooses the appropriate implementation.(Proxy or Remote)     
    // Subclasses MUST supply the implementation to this method.    
    abstract void chooseImpl()                                      
    throws IOException, AS400SecurityException;                     



    // This method is avialable for use by other classes within the package.
    final NPCPID getIDCodePoint()
    {
        // @B2A - no need for converter if the attribute list need not be built.
        if ((cpID_ != null) && (cpID_.getfListOutOfDate())) {                               // @B2C
        if (cpID_.converter_ == null) {                                                     // @B1A
            try {                                                                           // @B1A
                cpID_.setConverter((new Converter(system_.getCcsid(), system_)).impl);      // @B1A
            }                                                                               // @B1A
            catch(UnsupportedEncodingException e) {                                         // @B1A
                if (Trace.isTraceErrorOn())                                                 // @B1A
                    Trace.log(Trace.ERROR,  "Unable to set converter ", e);                 // @B1A
            }                                                                               // @B1A
        }                                                                                   // @B1A
        }                                                                                   // @B1A

        return cpID_;
    }


    // Returns the impl_ ... required for passing XXXImpl parameters
    PrintObjectImpl getImpl()
    {
        return impl_;
    }



    /**
     * Returns an attribute of the object that is a Integer type attribute.
     *
     * @param attributeID Identifies which attribute to retrieve.
     * See the following links for the attribute IDs that are valid for each
     * particular subclass.<UL>
     * <LI> <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/AFPResourceAttrs.html">AFP Resource Attributes</A>
     * <LI> <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/OutputQueueAttrs.html">Output Queue Attributes</A>
     * <LI> <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrinterAttrs.html">Printer Attributes</A>
     * <LI> <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrinterFileAttrs.html">Printer File Attributes</A>
     * <LI> <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/SpooledFileAttrs.html">Spooled File Attributes</A>
     * <LI> <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/WriterJobAttrs.html">Writer Job Attributes</A>
     * </UL>
     *
     * @return The value of the attribute.
     *
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         the server is not at the correct level.
     **/
    public Integer getIntegerAttribute(int attributeID)
      throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              IOException,
              InterruptedException,
              RequestNotSupportedException
    {
        Integer aValue = null;                                  
        if ((attrs != null) && (impl_ == null)                  
            &&  (cpID_.converter_ != null)) {                   
            aValue = attrs.getIntValue(attributeID);            
        }                                                       
        if (aValue == null) {                                   
            if (impl_ == null)                                  
                chooseImpl();                                   
            aValue = impl_.getIntegerAttribute(attributeID);    
            // update the attrs, since updateAttrs may have     
            // been called on the remote side...                
            attrs = impl_.getAttrValue();                       
        }                                                       
        return aValue;                                          
    }


    /**
     * Returns an attribute of the object that is a Integer type attribute.
     *
     * @param attributeID Identifies which attribute to retrieve.
     * See the following links for the attribute IDs that are valid for each
     * particular subclass.<UL>
     * <LI> <A HREF="../../../../AFPResourceAttrs.html">AFP Resource Attributes</A>
     * <LI> <A HREF="../../../../OutputQueueAttrs.html">Output Queue Attributes</A>
     * <LI> <A HREF="../../../../PrinterAttrs.html">Printer Attributes</A>
     * <LI> <A HREF="../../../../PrinterFileAttrs.html">Printer File Attributes</A>
     * <LI> <A HREF="../../../../SpooledFileAttrs.html">Spooled File Attributes</A>
     * <LI> <A HREF="../../../../WriterJobAttrs.html">Writer Job Attributes</A>
     * </UL>
     *
     * @return The value of the attribute.
     *
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         the server is not at the correct level.
     **/
    public Integer getSingleIntegerAttribute(int attributeID)
      throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              IOException,
              InterruptedException,
              RequestNotSupportedException
    {
        Integer aValue = null;
        if ((attrs != null) && (impl_ == null)
            &&  (cpID_.converter_ != null)) {
            aValue = attrs.getIntValue(attributeID);
        }
        if (aValue == null) {
            if (impl_ == null)
                chooseImpl();
            aValue = impl_.getSingleIntegerAttribute(attributeID);
            // update the attrs, since updateAttrs may have
            // been called on the remote side...
            attrs = impl_.getAttrValue();
        }
        return aValue;
    }

    /**
     * Returns an attribute of the object that is a Float type attribute.
     *
     * @param attributeID Identifies which attribute to retrieve.
     * See the following links for the attribute IDs that are valid for each
     * particular subclass.<UL>
     * <LI> <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/AFPResourceAttrs.html">AFP Resource Attributes</A>
     * <LI> <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/OutputQueueAttrs.html">Output Queue Attributes</A>
     * <LI> <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrinterAttrs.html">Printer Attributes</A>
     * <LI> <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrinterFileAttrs.html">Printer File Attributes</A>
     * <LI> <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/SpooledFileAttrs.html">Spooled File Attributes</A>
     * <LI> <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/WriterJobAttrs.html">Writer Job Attributes</A>
     * </UL>
     *
     * @return The value of the attribute.
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         the server is not at the correct level.
     **/
    public Float getFloatAttribute(int attributeID)
       throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              IOException,
              InterruptedException,
              RequestNotSupportedException
    {
        Float aValue = null;                                    
        if ((attrs != null) && (impl_ == null)                  
            &&  (cpID_.converter_ != null)) {                   
            aValue = attrs.getFloatValue(attributeID);          
        }                                                       
        if (aValue == null) {                                   
            if (impl_ == null)                                  
                chooseImpl();                                   
            aValue = impl_.getFloatAttribute(attributeID);      
            // update the attrs, since updateAttrs may have     
            // been called on the remote side...                
            attrs = impl_.getAttrValue();                       
        }                                                       
        return aValue;                                          
    }


/**
     * Returns an attribute of the object that is a Float type attribute.
     *
     * @param attributeID Identifies which attribute to retrieve.
     * See the following links for the attribute IDs that are valid for each
     * particular subclass.<UL>
     * <LI> <A HREF="../../../../AFPResourceAttrs.html">AFP Resource Attributes</A>
     * <LI> <A HREF="../../../../OutputQueueAttrs.html">Output Queue Attributes</A>
     * <LI> <A HREF="../../../../PrinterAttrs.html">Printer Attributes</A>
     * <LI> <A HREF="../../../../PrinterFileAttrs.html">Printer File Attributes</A>
     * <LI> <A HREF="../../../../SpooledFileAttrs.html">Spooled File Attributes</A>
     * <LI> <A HREF="../../../../WriterJobAttrs.html">Writer Job Attributes</A>
     * </UL>
     *
     * @return The value of the attribute.
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         the server is not at the correct level.
     **/
    public Float getSingleFloatAttribute(int attributeID)
       throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              IOException,
              InterruptedException,
              RequestNotSupportedException
    {
        Float aValue = null;
        if ((attrs != null) && (impl_ == null)
            &&  (cpID_.converter_ != null)) {
            aValue = attrs.getFloatValue(attributeID);
        }
        if (aValue == null) {
            if (impl_ == null)
                chooseImpl();
            aValue = impl_.getSingleFloatAttribute(attributeID);
            // update the attrs, since updateAttrs may have
            // been called on the remote side...
            attrs = impl_.getAttrValue();
        }
        return aValue;
    }


    /**
     * Returns an attribute of the object that is a String type attribute.
     *
     * @param attributeID Identifies which attribute to retrieve.
     * See the following links for the attribute IDs that are valid for each
     * particular subclass.<UL>
     * <LI> <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/AFPResourceAttrs.html">AFP Resource Attributes</A>
     * <LI> <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/OutputQueueAttrs.html">Output Queue Attributes</A>
     * <LI> <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrinterAttrs.html">Printer Attributes</A>
     * <LI> <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/PrinterFileAttrs.html">Printer File Attributes</A>
     * <LI> <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/SpooledFileAttrs.html">Spooled File Attributes</A>
     * <LI> <A HREF="{@docRoot}/com/ibm/as400/access/doc-files/WriterJobAttrs.html">Writer Job Attributes</A>
     * </UL>
     *
     * @return The value of the attribute.
     *
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         the server is not at the correct level.
     **/
    public String getStringAttribute(int attributeID)
       throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              IOException,
              InterruptedException,
              RequestNotSupportedException
    {
        String str = null;                                      
        if ((attrs != null) && (impl_ == null)                  
            &&  (cpID_.converter_ != null)) {                   
            str = attrs.getStringValue(attributeID);            
        }                                                       
        if (str == null) {                                      
            if (impl_ == null)                                  
                chooseImpl();                                   
            str = impl_.getStringAttribute(attributeID);        
            // update the attrs, since updateAttrs may have     
            // been called on the remote side...                
            attrs = impl_.getAttrValue();                       
        }                                                       
        return str;                                             
    }


    /**
     * Returns an attribute of the object that is a String type attribute.
     *
     * @param attributeID Identifies which attribute to retrieve.
     * See the following links for the attribute IDs that are valid for each
     * particular subclass.<UL>
     * <LI> <A HREF="../../../../AFPResourceAttrs.html">AFP Resource Attributes</A>
     * <LI> <A HREF="../../../../OutputQueueAttrs.html">Output Queue Attributes</A>
     * <LI> <A HREF="../../../../PrinterAttrs.html">Printer Attributes</A>
     * <LI> <A HREF="../../../../PrinterFileAttrs.html">Printer File Attributes</A>
     * <LI> <A HREF="../../../../SpooledFileAttrs.html">Spooled File Attributes</A>
     * <LI> <A HREF="../../../../WriterJobAttrs.html">Writer Job Attributes</A>
     * </UL>
     *
     * @return The value of the attribute.
     *
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         the server is not at the correct level.
     **/
    public String getSingleStringAttribute(int attributeID)
       throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              IOException,
              InterruptedException,
              RequestNotSupportedException
    {
        String str = null;
        if ((attrs != null) && (impl_ == null)
            &&  (cpID_.converter_ != null)) {
            str = attrs.getStringValue(attributeID);
        }
        if (str == null) {
            if (impl_ == null)
            chooseImpl();
            str = impl_.getSingleStringAttribute(attributeID);
            // update the attrs, since updateAttrs may have
            // been called on the remote side...
            attrs = impl_.getAttrValue();
        }
        return str;
    }


    /**
     * Returns the server on which this object exists.
     * @return The server on which this object exists.
     **/
    final public AS400 getSystem()
    {
        return system_;
    }



    private void initializeTransient()
    {
        impl_   = null;
        changes = new PropertyChangeSupport(this);
        vetos   = new VetoableChangeSupport(this);

    }



    /**
     * We need to initialize our transient and static data when
     * the object is de-serialized. static final data is OK.
     **/
    private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        initializeTransient();
    }



    /**
     * Removes the specified PropertyChange listener
     * so that it no longer receives PropertyChange events
     * from this print object.
     *
     * @see #addPropertyChangeListener
     * @param listener The PropertyChange listener.
     **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        changes.removePropertyChangeListener(listener);
    }



    /**
     * Removes the specified VetoableChange listener
     * so that it no longer receives VetoableChange events
     * from this print object.
     *
     * @see #addVetoableChangeListener
     * @param listener The VetoableChange listener.
     **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        vetos.removeVetoableChangeListener(listener);
    }



    // This is left here for public subclasses to use...
    // It is also located in PrintObjectImplRemote.java
    final void setIDCodePoint(NPCPID cpID)
    {
        cpID_ = cpID;
        if (impl_ != null)
             impl_.setPrintObjectAttrs(cpID_, attrs, objectType_);
    }


    /**
     * Set the system property of the PrintObject if necessary, as well
     * as the codepoint, attributes, and object type.
     **/
    void setImpl()
    throws IOException, AS400SecurityException
    {
        system_.connectService(AS400.PRINT);
        impl_.setSystem(system_.getImpl());
        impl_.setPrintObjectAttrs(cpID_, attrs, objectType_);
    }



    /**
     * Sets the server on which this object exists. This
     * method is primarily provided for visual application builders
     * that support JavaBeans. Application programmers should
     * specify the server in the constructor for the
     * specific print object.
     * @param system The server on which this object exists.
     *
     * @exception PropertyVetoException If the change is vetoed.
     *
     **/
    final public void setSystem(AS400 system)
      throws PropertyVetoException
    {
        if( system == null )
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }

        if (impl_ != null) {
            Trace.log(Trace.ERROR, "Cannot set property 'system' after connect.");
            throw new ExtendedIllegalStateException( "system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED );
        }

        AS400 oldSystem = getSystem();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange( SYSTEM, oldSystem, system );

        // No one vetoed, make the change.
        system_ = system;

        // we may need to pass on system...
        if (impl_ != null)
            impl_.setSystem(system_.getImpl());

        // When the system is changed, we need to "erase" any attributes
        // of the object that we have cached locally. We will refresh the
        // attributes from the new system the next time we need them.
        attrs   = null;

        // Notify any property change listeners.
        changes.firePropertyChange( SYSTEM, oldSystem, system );
    }



    /**
     * Updates the attributes of this object by going to the iSeries server and
     * retrieving the latest attributes for the object.
     *
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         the server is not at the correct level.
     **/
    public void update()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {

       // We have chosen to do nothing here for JavaBeans. We don't
       // think the Print Attributes are JavaBean properties and we
       // have not provided separate getters/setters.

        checkRunTimeState();

        if (impl_ == null)                              
            chooseImpl();                               
        impl_.update();                                 
        // propagate any changes to attrs               
        attrs = impl_.getAttrValue();                   
    }



    // this function returns the name of the ATTR corresponding to an integer id;
    // it returns NULL if the ATTR is private, or if the id is invalid;

    // in those cases where the ATTR id is specified (in this file) as a negative decimal integer,
    // this function does report the ATTR names of the private ATTRs immediately following the one
    // with the negative id; in such cases it returns the name of the ATTR with the negative decimal id;

    static String getAttributeName(int id)
    {
        switch (id)
        {
        case 0x011F: return "ATTR_3812SCS";
        case 0x0109: return "ATTR_ACCOUNT_CODE";
        case 0x000A: return "ATTR_AFP";
        case    -12: return "ATTR_AFP_RESOURCE"; // "negative decimal"
        case 0x011A: return "ATTR_AFPRESOURCE";
        case 0x0128: return "ATTR_ASCIITRANS";
        case 0x00FC: return "ATTR_AUX_POOL";
        case 0x011B: return "ATTR_BARCODE";
        case 0x011C: return "ATTR_COLOR";
        case 0x0132: return "ATTR_CODFNT_ARRAY";
        case 0x012C: return "ATTR_CHARID";
        case 0x013C: return "ATTR_CHR_RTT_CMDS";
        case 0x0133: return "ATTR_CHRSET_LIB";
        case 0x0134: return "ATTR_CHRSET";
        case 0x0138: return "ATTR_CHRSET_SIZE";
        case 0x012D: return "ATTR_CPI_CHANGES";
        case 0x011D: return "ATTR_DDS";
        case 0x011E: return "ATTR_DOUBLEWIDE";
        case 0x012E: return "ATTR_DRAWERCHANGE";
        case 0x0120: return "ATTR_FIELD_OUTLIN";
        case 0x012F: return "ATTR_FONT_CHANGES";
        case 0x0121: return "ATTR_GRAPHICS";
        case 0x0122: return "ATTR_GRAPHICS_TOK";
        case 0x0123: return "ATTR_GRPLVL_IDXTAG";
        case 0x0124: return "ATTR_HIGHLIGHT";
        case 0x0125: return "ATTR_OFFICEVISION";
        case 0x0126: return "ATTR_PAGE_GROUPS";
        case 0x0127: return "ATTR_PAGELVLIDXTAG";
        case 0x0129: return "ATTR_RCDFMT_DATA";
        case 0x00BE: return "ATTR_ALIGNFORMS";
        case 0x012B: return "ATTR_TRC1403";
        case 0x000B: return "ATTR_ALIGN";
        case 0x000C: return "ATTR_ALWDRTPRT";
        case 0x010A: return "ATTR_ASPDEVICE";
        case 0x000D: return "ATTR_AUTHORITY";
        case 0x000E: return "ATTR_AUTHCHCK";
        case 0x0010: return "ATTR_AUTOEND";
        case 0x0011: return "ATTR_BKMGN_ACR";
        case 0x0012: return "ATTR_BKMGN_DWN";
        case     -1: return "ATTR_BACK_OVERLAY";   // "negative decimal"
        case 0x0013: return "ATTR_BACK_OVERLAY";   // declared as private (ATTR_BKOVRLLIB)
        case 0x0014: return "ATTR_BACK_OVERLAY";   // declared as private (ATTR_BKOVRLAY)
        case 0x0015: return "ATTR_BKOVL_DWN";
        case 0x0016: return "ATTR_BKOVL_ACR";
        case 0x00CE: return "ATTR_BTWNCPYSTS";
        case 0x00CF: return "ATTR_BTWNFILESTS";
        case 0x00BF: return "ATTR_CHANGES";
        case 0x0037: return "ATTR_CHAR_ID";
        case 0x0017: return "ATTR_CPI";
        case 0x0018: return "ATTR_CODEDFNTLIB";
        case 0x0019: return "ATTR_CODEPAGE";
        case 0x0117: return "ATTR_CODEPAGE_NAME_LIB";
        case 0x0118: return "ATTR_CODEPAGE_NAME";
        case 0x0119: return "ATTR_CODEDFONT_SIZE";
        case 0x001A: return "ATTR_CODEDFNT";
        case 0x010E: return "ATTR_CONSTBCK_OVL";
        case 0x00C4: return "ATTR_CONTROLCHAR";
        case 0x00F7: return "ATTR_CONVERT_LINEDATA";
        case 0x001C: return "ATTR_COPIES";
        case 0x001D: return "ATTR_COPIESLEFT";
        case 0x00F8: return "ATTR_CORNER_STAPLE";
        case 0x001E: return "ATTR_CURPAGE";
        case 0x001F: return "ATTR_DATAFORMAT";
        case     -2: return "ATTR_DATA_QUEUE";   // "negative decimal"
        case 0x0020: return "ATTR_DATA_QUEUE";   // declared as private (ATTR_DATAQUELIB)
        case 0x0021: return "ATTR_DATA_QUEUE";   // declared as private (ATTR_DATAQUE)
        case 0x0022: return "ATTR_DATE";
        case 0x00FD: return "ATTR_DATE_END";
        case 0x010D: return "ATTR_DATE_USED";
        case 0x00EA: return "ATTR_DATE_WTR_BEGAN_FILE";
        case 0x00EB: return "ATTR_DATE_WTR_CMPL_FILE";
        case 0x0140: return "ATTR_DAYS_UNTIL_EXPIRE";
        case 0x0099: return "ATTR_DBCSDATA";
        case 0x009A: return "ATTR_DBCSEXTENSN";
        case 0x009B: return "ATTR_DBCSROTATE";
        case 0x0112: return "ATTR_DBCS_FNT_LIB";
        case 0x0113: return "ATTR_DBCS_FNT";
        case 0x0114: return "ATTR_DBCS_FNT_SIZE";
        case 0x009C: return "ATTR_DBCSCPI";
        case 0x009D: return "ATTR_DBCSSISO";
        case 0x0023: return "ATTR_DFR_WRITE";
        case 0x010C: return "ATTR_DECIMAL_FMT";
        case 0x0097: return "ATTR_DELETESPLF";
        case 0x006D: return "ATTR_DESCRIPTION";
        case 0x0025: return "ATTR_DESTINATION";
        case 0x0098: return "ATTR_DESTOPTION";
        case 0x0026: return "ATTR_DEVCLASS";
        case 0x0027: return "ATTR_DEVMODEL";
        case 0x00C7: return "ATTR_DEVSTATUS";
        case 0x0028: return "ATTR_DEVTYPE";
        case 0x0029: return "ATTR_DISPLAYANY";
        case 0x002A: return "ATTR_DRWRSEP";
        case 0x0055: return "ATTR_DUPLEX";
        case 0x00EE: return "ATTR_EDGESTITCH_REF";
        case 0x00F0: return "ATTR_EDGESTITCH_NUMSTAPLES";
        case 0x00EF: return "ATTR_EDGESTITCH_REFOFF";
        case 0x002B: return "ATTR_ENDPAGE";
        case 0x00CC: return "ATTR_ENDPNDSTS";
        case 0x00D3: return "ATTR_ENVLP_SOURCE";
        case 0x0141: return "ATTR_EXPIRATION_DATE";
        case 0x0054: return "ATTR_FIDELITY";
        case 0x002C: return "ATTR_FILESEP";
        case 0x002D: return "ATTR_FOLDREC";
        case 0x002E: return "ATTR_FONTID";
        case 0x010B: return "ATTR_FONTRESFMT";
        case     -3: return "ATTR_FORM_DEFINITION";  // "negative decimal"
        case 0x00B7: return "ATTR_FORM_DEFINITION";  // declared as private (ATTR_FORMDEFLIB)
        case 0x00B6: return "ATTR_FORM_DEFINITION";  // declared as private (ATTR_FORMDEF)
        case 0x002F: return "ATTR_FORMFEED";
        case 0x0030: return "ATTR_FORMTYPE";
        case 0x0043: return "ATTR_FORMTYPEMSG";
        case 0x0031: return "ATTR_FTMGN_ACR";
        case 0x0032: return "ATTR_FTMGN_DWN";
        case     -4: return "ATTR_FRONT_OVERLAY";    // "negative decimal"
        case 0x0033: return "ATTR_FRONT_OVERLAY";    // declared as private (ATTR_FTOVRLLIB)
        case 0x0034: return "ATTR_FRONT_OVERLAY";    // declared as private (ATTR_FTOVRLAY)
        case 0x0036: return "ATTR_FTOVL_ACR";
        case 0x0035: return "ATTR_FTOVL_DWN";
        case 0x00D0: return "ATTR_HELDSTS";
        case 0x0039: return "ATTR_HOLD";
        case 0x00D1: return "ATTR_HOLDPNDSTS";
        case 0x009E: return "ATTR_HOLDTYPE";
        case 0x0100: return "ATTR_IMGCFG";
        case 0x0094: return "ATTR_INTERNETADDR";
        case 0x0116: return "ATTR_IPDSPASSTHRU";
        case 0x00E1: return "ATTR_IPP_ATTR_CCSID";
        case 0x00E4: return "ATTR_IPP_JOB_ID";
        case 0x00E6: return "ATTR_IPP_JOB_NAME";
        case 0x00E7: return "ATTR_IPP_JOB_NAME_NL";
        case 0x00E8: return "ATTR_IPP_JOB_ORIGUSER";
        case 0x00E9: return "ATTR_IPP_JOB_ORIGUSER_NL";
        case 0x00E5: return "ATTR_IPP_PRINTER_NAME";
        case 0x00FA: return "ATTR_IPP_ATTR_NL";
        case 0x003B: return "ATTR_JOBNAME";
        case 0x003C: return "ATTR_JOBNUMBER";
        case 0x003D: return "ATTR_JOBSEPRATR";
        case 0x00FB: return "ATTR_JOBSYSTEM";
        case 0x003E: return "ATTR_JOBUSER";
        case 0x0038: return "ATTR_JUSTIFY";
        case 0x003F: return "ATTR_LASTPAGE";
        case 0x000F: return "ATTR_LIBRARY";
        case 0x00C3: return "ATTR_LINESPACING";
        case 0x0040: return "ATTR_LPI";
        case 0x0130: return "ATTR_LPI_CHANGES";
        case 0x00DE: return "ATTR_MAX_JOBS_PER_CLIENT";
        case 0x0042: return "ATTR_MAXRCDS";
        case 0x004F: return "ATTR_MEASMETHOD";
        case 0x0041: return "ATTR_MFGTYPE";
        case     -5: return "ATTR_MESSAGE_QUEUE";  // "negative decimal"
        case 0x0044: return "ATTR_MESSAGE_QUEUE";  // declared as private (ATTR_MSGQUELIB)
        case 0x005E: return "ATTR_MESSAGE_QUEUE";  // declared as private (ATTR_MSGQUE)
        case 0x0081: return "ATTR_MSGHELP";
        case 0x0093: return "ATTR_MSGID";
        case 0x0082: return "ATTR_MSGREPLY";
        case 0x009F: return "ATTR_MSGSEV";
        case 0x0080: return "ATTR_MSGTEXT";
        case 0x008E: return "ATTR_MSGTYPE";
        case 0x00DC: return "ATTR_MULTI_ITEM_REPLY";
        case 0x0052: return "ATTR_MULTIUP";
        case 0x00BD: return "ATTR_NETWORK";
        case 0x008A: return "ATTR_NPSCCSID";
        case 0x008D: return "ATTR_NPSLEVEL";
        case 0x007D: return "ATTR_NUMBYTES";
        case 0x00D9: return "ATTR_NUMBYTES_SPLF";
        case 0x0045: return "ATTR_NUMFILES";
        case 0x0091: return "ATTR_NUMWRITERS";
        case 0x0139: return "ATTR_NUMRSC_LIB_ENT";
        case 0x00B1: return "ATTR_OBJEXTATTR";
        case 0x00CD: return "ATTR_ONJOBQSTS";
        case 0x00A0: return "ATTR_OPENCMDS";
        case 0x0046: return "ATTR_OPCNTRL";
        case 0x0047: return "ATTR_ORDER";
        case 0x0048: return "ATTR_OUTPTY";
        case 0x0135: return "ATTR_OS4_CRT_AFP";
        case 0x00C0: return "ATTR_OUTPUTBIN";
        case     -6: return "ATTR_OUTPUT_QUEUE";  // "negative decimal"
        case 0x0049: return "ATTR_OUTPUT_QUEUE";  // declared as private (ATTR_OUTQUELIB)
        case 0x004A: return "ATTR_OUTPUT_QUEUE";  // declared as private (ATTR_OUTQUE)
        case 0x004B: return "ATTR_OUTQSTS";
        case 0x00C8: return "ATTR_OVERALLSTS";
        case 0x004C: return "ATTR_OVERFLOW";
        case 0x00D6: return "ATTR_PAGE_AT_A_TIME";
        case    -13: return "ATTR_PAGE_DEFINITION"; // "negative decimal"
        case 0x00F5: return "ATTR_PAGE_DEFINITION"; // declared as private (ATTR_PAGDFNLIB)
        case 0x00F6: return "ATTR_PAGE_DEFINITION"; // declared as private (ATTR_PAGDFN)
        case 0x004E: return "ATTR_PAGELEN";
        case 0x00D7: return "ATTR_PAGENUMBER";
        case 0x006F: return "ATTR_PAGES";
        case 0x00DA: return "ATTR_PAGES_EST";
        case 0x0024: return "ATTR_PAGRTT";
        case 0x012A: return "ATTR_PAGE_ROTATE";
        case 0x0051: return "ATTR_PAGEWIDTH";
        case 0x00B2: return "ATTR_PELDENSITY";
        case 0x00D4: return "ATTR_PAPER_SOURCE_1";
        case 0x00D5: return "ATTR_PAPER_SOURCE_2";
        case 0x0053: return "ATTR_POINTSIZE";
        case 0x010F: return "ATTR_PGM_OPN_LIB";
        case 0x0110: return "ATTR_PGM_OPN_FILE";
        case 0x0056: return "ATTR_PRTQUALITY";
        case 0x0057: return "ATTR_PRTSEQUENCE";
        case 0x0058: return "ATTR_PRTTEXT";
        case 0x0059: return "ATTR_PRINTER";
        case 0x00BA: return "ATTR_PRTASSIGNED";
        case 0x005A: return "ATTR_PRTDEVTYPE";
        case     -7: return "ATTR_PRINTER_FILE";  // "negative decimal"
        case 0x005B: return "ATTR_PRINTER_FILE";  // declared as private (ATTR_PRTFLIB)
        case 0x005C: return "ATTR_PRINTER_FILE";  // declared as private (ATTR_PRTFILE)
        case 0x0101: return "ATTR_PUBINF_COLOR_SUP";
        case 0x0102: return "ATTR_PUBINF_PPM_COLOR";
        case 0x0106: return "ATTR_PUBINF_DS";
        case 0x0103: return "ATTR_PUBINF_PPM";
        case 0x0104: return "ATTR_PUBINF_DUPLEX_SUP";
        case 0x0105: return "ATTR_PUBINF_LOCATION";
        case 0x00FF: return "ATTR_RMTLOCNAME";
        case 0x005D: return "ATTR_RMTPRTQ";
        case 0x005F: return "ATTR_RECLENGTH";
        case 0x00C2: return "ATTR_REDUCE";
        case 0x0060: return "ATTR_RMTSYSTEM";
        case 0x0061: return "ATTR_RPLUNPRT";
        case 0x0062: return "ATTR_RPLCHAR";
        case 0x0063: return "ATTR_RESTART";
        case 0x00F3: return "ATTR_SADDLESTITCH_NUMSTAPLES";
        case 0x00F2: return "ATTR_SADDLESTITCH_REF";
        case 0x00F4: return "ATTR_SADDLESTITCH_STPL_OFFSEINFO";
        case 0x0064: return "ATTR_SAVE";
        case 0x0142: return "ATTR_SAVE_COMMAND";
        case 0x0143: return "ATTR_SAVE_DEVICE";
        case    -14: return "ATTR_SAVE_FILE";
        case 0x0145: return "ATTR_SAVEFILE";
        case 0x0144: return "ATTR_SAVEFILELIB";
        case 0x0146: return "ATTR_SAVE_LABEL";
        case 0x0147: return "ATTR_SAVE_SEQUENCE_NUMBER";
        case 0x0148: return "ATTR_SAVE_VOLUME_FORMAT";
        case 0x0149: return "ATTR_SAVE_VOLUME_ID";
        case 0x006B: return "ATTR_SCHEDULE";
        case 0x0071: return "ATTR_SCS2ASCII";
        case 0x007E: return "ATTR_SEEKOFF";
        case 0x007F: return "ATTR_SEEKORG";
        case 0x0065: return "ATTR_SENDPTY";
        case 0x00A1: return "ATTR_SEPPAGE";
        case 0x0066: return "ATTR_SRCDRWR";
        case 0x0107: return "ATTR_SRC_CODEPAGE";
        case 0x0067: return "ATTR_SPOOL";
        case 0x0068: return "ATTR_SPOOLFILE";
        case 0x00E3: return "ATTR_SPLF_AUTH_METHOD";
        case 0x013A: return "ATTR_SPLF_CREATOR";
        case 0x014A: return "ATTR_SPLF_RESTORED_DATE";
        case 0x014B: return "ATTR_SPLF_RESTORED_TIME";
        case 0x014C: return "ATTR_SPLF_SAVED_DATE";
        case 0x014D: return "ATTR_SPLF_SAVED_TIME";
        case 0x0136: return "ATTR_SPLF_SIZE";
        case 0x0137: return "ATTR_SPLF_SIZE_MULT";
        case 0x00E2: return "ATTR_SPLF_SECURITY_METHOD";
        case 0x0069: return "ATTR_SPLFNUM";
        case 0x006A: return "ATTR_SPLFSTATUS";
        case 0x00AD: return "ATTR_SPLSCS";
        case 0x006C: return "ATTR_STARTPAGE";
        case 0x00C5: return "ATTR_STARTEDBY";
        case 0x00BC: return "ATTR_SYSTEM";
        case 0x0108: return "ATTR_TGT_CODEPAGE";
        case 0x0131: return "ATTR_SYS_DRV_PGM";
        case 0x006E: return "ATTR_TIME";
        case 0x00FE: return "ATTR_TIME_END";
        case 0x00EC: return "ATTR_TIME_WTR_BEGAN_FILE";
        case 0x00ED: return "ATTR_TIME_WTR_CMPL_FILE";
        case 0x0075: return "ATTR_TOUSERID";
        case 0x0076: return "ATTR_TOADDRESS";
        case 0x0072: return "ATTR_UNITOFMEAS";
        case 0x0073: return "ATTR_USERCMT";
        case 0x0074: return "ATTR_USERDATA";
        case 0x00A2: return "ATTR_USRDEFDATA";
        case 0x00C6: return "ATTR_USRDEFFILE";
        case 0x00A3: return "ATTR_USRDEFOPT";
        case     -9: return "ATTR_USER_DEFINED_OBJECT";  // "negative decimal"
        case 0x00A4: return "ATTR_USER_DEFINED_OBJECT";  // declared as private (ATTR_USRDEFOBJLIB)
        case 0x00A5: return "ATTR_USER_DEFINED_OBJECT";  // declared as private (ATTR_USRDEFOBJ)
        case 0x00A6: return "ATTR_USER_DEFINED_OBJECT";  // declared as private (ATTR_USRDEFOBJTYP)
        case 0x0115: return "ATTR_USER_DFN_TXT";
        case 0x0111: return "ATTR_USERGEN_DATA";
        case    -10: return "ATTR_USER_TRANSFORM_PROG";  // "negative decimal"
        case 0x00A7: return "ATTR_USER_TRANSFORM_PROG";  // declared as private (ATTR_USRTFMLIB)
        case 0x00A8: return "ATTR_USER_TRANSFORM_PROG";  // declared as private (ATTR_USRTFM)
        case 0x00A9: return "ATTR_USRDRVDATA";
        case    -11: return "ATTR_USER_DRIVER_PROG";     // "negative decimal"
        case 0x00AA: return "ATTR_USER_DRIVER_PROG";     // declared as private (ATTR_USRDRVLIB)
        case 0x00AB: return "ATTR_USER_DRIVER_PROG";     // declared as private (ATTR_USERDRV)
        case 0x00D8: return "ATTR_VIEWING_FIDELITY";
        case 0x00F9: return "ATTR_RSC_LIB_LIST";
        case 0x0077: return "ATTR_VMMVSCLASS";
        case     -8: return "ATTR_WORKSTATION_CUST_OBJECT";   // "negative decimal"
        case 0x00BB: return "ATTR_WRTNGSTS";
        case 0x00CB: return "ATTR_WTNGDATASTS";
        case 0x00C9: return "ATTR_WTNGDEVSTS";
        case 0x00CA: return "ATTR_WTNGMSGSTS";
        case 0x0078: return "ATTR_WTRAUTOEND";
        case 0x0090: return "ATTR_WTREND";
        case 0x00AC: return "ATTR_WTRINIT";
        case 0x0079: return "ATTR_WTRJOBNAME";
        case 0x007A: return "ATTR_WTRJOBNUM";
        case 0x007B: return "ATTR_WTRJOBSTS";
        case 0x007C: return "ATTR_WTRJOBUSER";
        case 0x008F: return "ATTR_WTRSTRPAGE";
        case 0x00C1: return "ATTR_WTRSTRTD";
        default:     return "";
        }
    }
}
