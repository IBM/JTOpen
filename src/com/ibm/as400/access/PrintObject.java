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
import java.io.UnsupportedEncodingException;                                // @B1A
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
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;



    // Attribute IDs in by network print objects
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY229">3812 SCS</A>. **/
    public static final int ATTR_3812SCS      = 0x011F;  // AT_3812SCS @ABA   
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY245">Accounting code</A>. **/
    public static final int ATTR_ACCOUNT_CODE = 0x0109; // AT_ACCOUNT_CODE @ABA 
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY1">Advanced function printing</A>. **/
    public static final int ATTR_AFP          = 0x000A;  // AFP resources used

    /** <A HREF="doc-files/PrintAttributes.html#HDRKEYIFS_C">AFP Resource integrated file system path</A>. **/
    public static final int ATTR_AFP_RESOURCE = -12;
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY224">Advanced Function Printing (AFP) resource</A>. **/
    public static final int ATTR_AFPRESOURCE  = 0x011A; //AT_AFP_RESOURCE  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY238">ASCII transparency</A>. **/
    public static final int ATTR_ASCIITRANS   = 0x0128; // AT_ASCII_TRANS  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY201">Auxilary Storage Pool</A>. **/
    public static final int ATTR_AUX_POOL     = 0x00FC; // Auxiliary Storage  @A9A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY225">Barcode</A>. **/
    public static final int ATTR_BARCODE      = 0x011B; // AT_BARCODE  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY226">Color</A>. **/
    public static final int ATTR_COLOR        = 0x011C; // AT_COLOR  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY246">Coded font array</A>. **/ 
    public static final int ATTR_CODFNT_ARRAY = 0x0132; // AT_CODED_FNT_ARRAY @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY241">Character ID</A>. **/
    public static final int ATTR_CHARID       = 0x012C; // AT_CHAR_ID  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY247">Character set library name</A>. **/
    public static final int ATTR_CHRSET_LIB   = 0x0133; // AT_CHARSET_LIB_NAME @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY248">Character set name</A>. **/
    public static final int ATTR_CHRSET       = 0x0134; // AT_CHARSET_NAME @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY258">Character set point size</A>.  **/
    public static final int ATTR_CHRSET_SIZE  = 0x0138; // AT_CHAR_POINT_SIZE @ABA 
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY242">Characters per inch changes</A>. **/
    public static final int ATTR_CPI_CHANGES  = 0x012D; // AT_CPI_CHANGES  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY227">DDS</A>. **/
    public static final int ATTR_DDS          = 0x011D; // AT_DDS   @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY228">Double-wide characters</A>. **/
    public static final int ATTR_DOUBLEWIDE   = 0x011E; // AT_DOUBLE_WIDE_CHAR @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY243">Drawer change</A>. **/
    public static final int ATTR_DRAWERCHANGE = 0x012E; // AT_DRAWER_CHANGE  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY230">Field outlining</A>. **/
    public static final int ATTR_FIELD_OUTLIN = 0x0120; // AT_FIELD_OUTLIN @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY244">Font changes</A>. **/
    public static final int ATTR_FONT_CHANGES = 0x012F; // AT_FONT_CHANGES @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY231">Graphics</A>. **/
    public static final int ATTR_GRAPHICS     = 0x0121; // AT_GRAPHICS @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY232">Graphics token</A>. **/
    public static final int ATTR_GRAPHICS_TOK = 0x0122; // AT_GRAPHICS_TOK  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY233">Group level index tags</A>. **/
    public static final int ATTR_GRPLVL_IDXTAG = 0x0123; // AT_GROUP_LVL_IDX_TAG  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY234">Highlight</A>. **/
    public static final int ATTR_HIGHLIGHT    = 0x0124; // AT_HIGHLIGHT  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY235">OfficeVision</A>. **/
    public static final int ATTR_OFFICEVISION = 0x0125; // AT_OFFICEVISION  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY236">Page groups</A>. **/
    public static final int ATTR_PAGE_GROUPS  = 0x0126; // AT_PAGE_GROUPS  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY237">Page level index tags</A>. **/
    public static final int ATTR_PAGELVLIDXTAG = 0x0127; // AT_PAGE_LVL_IDX_TAG @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY239">Record format</A>. **/
    public static final int ATTR_RCDFMT_DATA   = 0x0129; // AT_RCD_FMT_IN_DATA  @ABA 
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY1001">Resource library name</A>. **/
               static final int ATTR_RSCLIB       = 0x00AE; // Resource library
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY1002">Resource name</A>. **/
           static final int ATTR_RSCNAME      = 0x00AF; // Resource name
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY1003">Resource object type</A>. **/
           static final int ATTR_RSCTYPE      = 0x00B0; // Resource object type
    /** <A HREF="doc-files/PrintAttributes.html##HDRKEY2.5">Align forms</A>. **/
    public static final int ATTR_ALIGNFORMS   = 0x00BE;  // align forms @A1A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY240">TRC for 1403</A>. **/
    public static final int ATTR_TRC1403      = 0x012B;  // AT_TRC_1403 @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY2">Align page</A>. **/
    public static final int ATTR_ALIGN        = 0x000B;  // align forms before printing
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY3">Allow direct print</A>. **/
    public static final int ATTR_ALWDRTPRT    = 0x000C;  // allow direct printing
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY213" //ASP Device</A>. **/
    public static final int ATTR_ASPDEVICE    = 0x010A;  // AT_ASP_DEVICE  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY134">NPS Attribute default value</A>. **/
           static final int ATTR_ATTRDEFAULT  = 0x0083;  // attribute default value
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY135">NPS Attribute high limit</A>. **/
           static final int ATTR_ATTRMAX      = 0x0084;  // attribute maximum value
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY137">NPS Attribute ID</A>. **/
           static final int ATTR_ATTRID       = 0x0085;  // attribute ID
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY138">NPS Attribute low limit</A>. **/
           static final int ATTR_ATTRMIN      = 0x0086;  // attribute minimum value
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY139">NPS Attribute possible value</A>. **/
           static final int ATTR_ATTRPOSSIBL  = 0x0087;  // possible value for attribute
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY140">NPS Attribute text description</A>. **/
           static final int ATTR_ATTRDESCRIPT = 0x0088;  // attribute description
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY141">NPS Attribute type</A>. **/
           static final int ATTR_ATTRTYPE     = 0x0089;  // attribute type
    /**  <A HREF="doc-files/PrintAttributes.html#HDRKEY4">Authority</A>. **/
    public static final int ATTR_AUTHORITY    = 0x000D;  // Authority to users not on  output queue
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY5">Authority to check</A>. **/
    public static final int ATTR_AUTHCHCK     = 0x000E;  // authority allows user to all files on queue
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY6">Automatically end writer</A>. **/
    public static final int ATTR_AUTOEND      = 0x0010;  // automatically end writer *YES/*NO
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY7">Back margin offset across</A>. **/
    public static final int ATTR_BKMGN_ACR    = 0x0011;  // back margin across
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY8">Back margin offset down</A>. **/
    public static final int ATTR_BKMGN_DWN    = 0x0012;  // back margin down
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEYIFS_1">Back overlay integrated file system name</A>. **/
    public static final int ATTR_BACK_OVERLAY = -1;   // backside overlay Integrated File System path
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY9">Backside overlay library name</A>. **/
           static final int ATTR_BKOVRLLIB    = 0x0013;  // back side overlay library
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY10">Backside overlay name</A>.  **/
           static final int ATTR_BKOVRLAY     = 0x0014;  // back side overlay name
    /**  <A HREF="doc-files/PrintAttributes.html#HDRKEY12">Back overlay offset down</A>.**/
    public static final int ATTR_BKOVL_DWN    = 0x0015;  // back overlay offset down
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY11">Back overlay offset across</A>.**/
    public static final int ATTR_BKOVL_ACR    = 0x0016;  // back overlay offset across
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY12.3">Between copies status</A>. **/
    public static final int ATTR_BTWNCPYSTS  = 0x00CE;  // indicates whether the writer is between copies of @A2A
                                                        // multiple copy spooled file
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY12.6">Between files status</A>. **/
    public static final int ATTR_BTWNFILESTS  = 0x00CF;  // indicates whether the writer is between files @A2A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY13.5">Changes take effect</A>. **/
    public static final int ATTR_CHANGES      = 0x00BF;  // changed take effect @A1A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY53">Graphic character set</A>. **/
    public static final int ATTR_CHAR_ID      = 0x0037;  // set of graphic characters for this file
    /**  <A HREF="doc-files/PrintAttributes.html#HDRKEY13">Characters per inch</A>. **/
    public static final int ATTR_CPI          = 0x0017;  // characters per inch
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY16">Coded font library name</A>. **/
    public static final int ATTR_CODEDFNTLIB  = 0x0018;  // coded font library name
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY14">Code page</A>. **/
    public static final int ATTR_CODEPAGE     = 0x0019;  // code page
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY223">Code page library name</A>. **/
    public static final int ATTR_CODEPAGE_NAME_LIB = 0x0117;  // AT_CODE_PAGE_LIB @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY223.1">Code page name</A>. **/
    public static final int ATTR_CODEPAGE_NAME = 0x0118;  // AT_CODE_PAGE_NAME @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY223.2">Coded font point size</A>. **/
    public static final int ATTR_CODEDFONT_SIZE = 0x0119;  // AT_CODE_FONT_SIZE @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY15">Code font name</A>. **/
    public static final int ATTR_CODEDFNT     = 0x001A;  // coded font
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY217">Constant back overlay</A>. **/
    public static final int ATTR_CONSTBCK_OVL = 0x010E;  // AT_CONST_BACK_OVL  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY17.5">Control character</A>. **/
    public static final int ATTR_CONTROLCHAR  = 0x00C4;  // control character @A1A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY177">Convert line data</A>. **/  /* @A7A*/
    public static final int ATTR_CONVERT_LINEDATA  = 0x00F7;  // convert line data   @A7A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY17">Copies</A>. **/
    public static final int ATTR_COPIES       = 0x001C;  // copies (total)
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY18">Copies left to produce</A>. **/ 
    public static final int ATTR_COPIESLEFT   = 0x001D;  // copies left to produce   
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY191">Corner staple</A>. **/   /* @A7A*/
    public static final int ATTR_CORNER_STAPLE   = 0x00F8;  // corner staple    @A7A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY19">Current page</A>. **/
    public static final int ATTR_CURPAGE      = 0x001E;  // current page
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY20">Data format</A>.**/
    public static final int ATTR_DATAFORMAT   = 0x001F;  // data format
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEYIFS_2">Data queue integrated file system name</A>. **/
    public static final int ATTR_DATA_QUEUE    = -2;  // data queue IFSPath
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY21">Data queue library name</A>.  **/
           static final int ATTR_DATAQUELIB   = 0x0020;  // data queue library
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY22">Data queue name</A>. **/
           static final int ATTR_DATAQUE      = 0x0021;  // data queue associated with output queue
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY23">Date object created</A>. **/
    public static final int ATTR_DATE         = 0x0022;  // date file was opened
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY202">Date job created spooled file</A>. **/
    public static final int ATTR_DATE_END     = 0x00FD;  // date job created file @A9A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY216">Date file last used</A>. **/
    public static final int ATTR_DATE_USED    = 0X010D;  // AT_DATE_FILE_USED    @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY178">Date writer began processing spooled file</A>. **/   /* @A7A*/
    public static final int ATTR_DATE_WTR_BEGAN_FILE = 0x00EA;  // date writer began file @A7A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY179">Date writer completed processing spooled filed</A>. **/   /* @A7A*/
    public static final int ATTR_DATE_WTR_CMPL_FILE = 0x00EB;  // date writer finished file @A7A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY24">User specified DBCS data</A>. **/
    public static final int ATTR_DBCSDATA     = 0x0099; // contains DBCS character set data
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY25">DBCS extension characters</A>.  **/
    public static final int ATTR_DBCSEXTENSN  = 0x009A; // process DBCS extension characters
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY26">DBCS character rotation</A>.  **/
    public static final int ATTR_DBCSROTATE   = 0x009B; // rotate DBCS characters
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY221">DBCS-coded font library name</A>.  **/
    public static final int ATTR_DBCS_FNT_LIB = 0x0112; // AT_DBCS_FONT_LIB  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY221.1">DBCS-coded font name</A>.  **/
    public static final int ATTR_DBCS_FNT     = 0x0113; // AT_DBCS_FONT_LIB  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY221.2">DBCS-coded font point size</A>.  **/
    public static final int ATTR_DBCS_FNT_SIZE= 0x0114; // AT_DBCS_FONT_LIB  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY27">DBCS characters per inch</A>.  **/
    public static final int ATTR_DBCSCPI      = 0x009C; // DBCS CPI
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY28">DBCS SO/SI spacing</A>. **/
    public static final int ATTR_DBCSSISO     = 0x009D; // DBCS SI/SO positioning
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY29">Defer write</A>. **/
    public static final int ATTR_DFR_WRITE    = 0x0023;  // defer write
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY215">Decimal Format</A>. **/
    public static final int ATTR_DECIMAL_FMT  = 0x010C;  // AT_DECIMAL_FORMAT  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY32">Delete file after sending</A>. **/
    public static final int ATTR_DELETESPLF   = 0x0097; // delete file after sending
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY113">Text description</A>. **/
    public static final int ATTR_DESCRIPTION  = 0x006D;  // text description
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY34">Destination type</A>. **/
    public static final int ATTR_DESTINATION  = 0x0025;  // destination type
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY33">Destination option</A>. **/
    public static final int ATTR_DESTOPTION   = 0x0098; // destinaton option sent
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY35">Device class</A>. **/
    public static final int ATTR_DEVCLASS     = 0x0026;  // device class
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY36">Device model</A>.**/
    public static final int ATTR_DEVMODEL     = 0x0027;  // device model
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY36.5">Device status</A>.**/
    public static final int ATTR_DEVSTATUS     = 0x00C7;  // device status @A1A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY37">Device type</A>.**/
    public static final int ATTR_DEVTYPE      = 0x0028;  // device type
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY38">Display any file</A>. **/
    public static final int ATTR_DISPLAYANY   = 0x0029;  // users can display any file on queue
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY39">Drawer for separators</A>. **/
    public static final int ATTR_DRWRSEP      = 0x002A;  // drawer to use for separators
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY88">Print on both sides</A>. **/
    public static final int ATTR_DUPLEX       = 0x0055;  // print on both sides of paper
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY192">Edge stitch reference</A>. **/   /* @A7A*/
    public static final int ATTR_EDGESTITCH_REF   = 0x00EE;  // edgestitch reference @A7A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY194">Edge stitch number of staples</A>. **/   /* @A7A*/
    public static final int ATTR_EDGESTITCH_NUMSTAPLES= 0x00F0;  // edgesticth number stapes @A7A    
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY193">Edge stitch reference offset</A>. **/   /* @A7A*/
    public static final int ATTR_EDGESTITCH_REFOFF   = 0x00EF;  // edgestitch reference offset @A7A
        /** <A HREF="doc-files/PrintAttributes.html#HDRKEY195">Edge stitch staple offset info</A>. **/   /* @A7A*/
           static final int ATTR_EDGESTITCH_STPL_OFFSET_INFO   = 0x00F1;  // edgestitch staple offset info @A7A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY40">Ending page</A>.**/
    public static final int ATTR_ENDPAGE      = 0x002B;  // ending page number to print
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY40.5">End pending status</A>. **/
    public static final int ATTR_ENDPNDSTS    = 0x00CC;  // indicates whether an end writer command has been @A2A
                                                         // issued for this writer
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY168">Envelope Source</A>. **/     /* @A4A */
    public static final int ATTR_ENVLP_SOURCE = 0x00D3; // envelope source       @A4A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY87">Print fidelity</A>. **/
    public static final int ATTR_FIDELITY     = 0x0054;  // the error handling when printing
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY41">File separators</A>. **/
    public static final int ATTR_FILESEP      = 0x002C;  // number of file separators
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY42">Fold records</A>. **/
    public static final int ATTR_FOLDREC      = 0x002D;  // wrap text to next line
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY43">Font identifier</A>. **/
    public static final int ATTR_FONTID       = 0x002E;  // Font identifier to use (default)
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY214">Font resolution for formatting</A>. **/
    public static final int ATTR_FONTRESFMT   = 0x010B;  // AT_FONT_RES_FORMAT  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEYIFS_3">Form definition integrated file system name</A>. **/
    public static final int ATTR_FORM_DEFINITION = -3;  // Formdef IFSPath
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY43A">Form definition library name</A>. **/
           static final int ATTR_FORMDEFLIB   = 0x00B7; // Form definition library name
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY43B">Form definition name</A>. **/
           static final int ATTR_FORMDEF      = 0x00B6; // Form definition name
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY44">Form feed</A>. **/
    public static final int ATTR_FORMFEED     = 0x002F;  // type of paperfeed to be used
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY45">Form type</A>. **/
    public static final int ATTR_FORMTYPE     = 0x0030;  // name of the form to be used
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY46">Form type message option</A>. **/
    public static final int ATTR_FORMTYPEMSG  = 0x0043;  // form type message option
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY47">Front margin offset across</A>. **/
    public static final int ATTR_FTMGN_ACR    = 0x0031;  // front margin across
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY48">Front margin offset down</A>. **/
    public static final int ATTR_FTMGN_DWN    = 0x0032;  // front margin down
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEYIFS_4">Front overlay integrated file system name</A>. **/
    public static final int ATTR_FRONT_OVERLAY = -4;  // Front Overlay IFSPath
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY49">Front overlay library name</A>. **/
           static final int ATTR_FTOVRLLIB    = 0x0033;  // front side overlay library
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY50">Front overlay name</A>.  **/
           static final int ATTR_FTOVRLAY     = 0x0034;  // front side overlay name
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY51">Front overlay offset across</A>. **/
    public static final int ATTR_FTOVL_ACR    = 0x0036;  // front overlay offset across
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY52">Front overlay offset down</A>. **/
    public static final int ATTR_FTOVL_DWN    = 0x0035;  // front overlay offset down
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY54.5">Held status</A>. **/
    public static final int ATTR_HELDSTS      = 0x00D0;  // indicates whether the writer is held @A2A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY55">Hold spool file</A>.**/
    public static final int ATTR_HOLD         = 0x0039;  // hold the spool file
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY55.5">Hold pending status</A>. **/
    public static final int ATTR_HOLDPNDSTS   = 0x00D1;  // indicates whether a hold writer command has been @A2A
                                                         // issued for this writer
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY125">When to hold file</A>. **/
    public static final int ATTR_HOLDTYPE     = 0x009E; // When to hold spooled file
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY206"> Image Configuration </A>.  **/
    public static final int ATTR_IMGCFG       = 0x0100; // Image Configuration @A9A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY167">Initialize printer</A>. **/            /* @A4A */
           static final int ATTR_INITIALIZE_PRINTER = 0x00D2; // initialize the printer    @A4A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY56">Internet address</A>. **/
    public static final int ATTR_INTERNETADDR = 0x0094; // internet address
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY222">IPDS pass-through</A>. **/
    public static final int ATTR_IPDSPASSTHRU = 0x0116;  // AT_IPDS_PASSTHRU @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY180">IPP Attributes-ccsid</A>. **/  /* @A7A8C*/
    // public static final int ATTR_IPP_ATTR_CHARSET= 0x00E1;   IPP Attributes-charset    @A7A8D
    public static final int ATTR_IPP_ATTR_CCSID= 0x00E1;  // IPP Attributes-ccsid  @A8A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY181">IPP Job ID</A>. **/  /* @A7A*/
    public static final int ATTR_IPP_JOB_ID   = 0x00E4;  // IPP Job ID    @A7A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY182">IPP Job Name </A>. **/  /* @A7A*/
    public static final int ATTR_IPP_JOB_NAME = 0x00E6;  // IPP Job Name    @A7A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY183">IPP Job Name NL</A>. **/  /* @A7A*/
    public static final int ATTR_IPP_JOB_NAME_NL= 0x00E7;  // IPP Job Name NL    @A7A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY184">IPP Job Originating user name </A>. **/  /* @A7A*/
    public static final int ATTR_IPP_JOB_ORIGUSER= 0x00E8;  // IPP Job Originating usernam    @A7A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY185">IPP Job Originating user name NL</A>. **/  /* @A7A*/
    public static final int ATTR_IPP_JOB_ORIGUSER_NL= 0x00E9;  // Originating user NL  @A7A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY186">IPP Printer name</A>. **/  /* @A7A*/
    public static final int ATTR_IPP_PRINTER_NAME= 0x00E5;  // IPP Printer URI name    @A7A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY180.5">IPP Natural Language</A>. **/
    public static final int ATTR_IPP_ATTR_NL= 0x00FA;  // IPP natural language   @A8A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY57">Job name</A>. **/
     public static final int ATTR_JOBNAME      = 0x003B;  // name of the job that created file
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY58">Job number</A>.**/
    public static final int ATTR_JOBNUMBER    = 0x003C;  // number of the job that created file
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY59">Job separators</A>. **/
    public static final int ATTR_JOBSEPRATR   = 0x003D;  // number of job separators
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY203">System Job which created spooled file was running</A>. **/
    public static final int ATTR_JOBSYSTEM    = 0x00FB;  // System job creating spooled file ran @A9A 
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY60">Job user</A>. **/
    public static final int ATTR_JOBUSER      = 0x003E;  // name of the user that created file
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY54">Hardware justification</A>. **/
    public static final int ATTR_JUSTIFY      = 0x0038;  // hardware justification
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY61">Last page printed</A>. **/
    public static final int ATTR_LASTPAGE     = 0x003F;  // last page that printed
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY63">Library name</A>. **/
    public static final int ATTR_LIBRARY      = 0x000F;  // library name
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY65.5">Line spacing</A>. **/
    public static final int ATTR_LINESPACING  = 0x00C3;  // line spacing @A1A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY64">Lines per inch</A>. **/
    public static final int ATTR_LPI          = 0x0040;  // lines per inch
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY249">LPI changes</A>. **/
    public static final int ATTR_LPI_CHANGES  = 0x0130;  // LP Changes @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY199">Maximum jobs per client</A>. **/   /* @A7A*/
    public static final int ATTR_MAX_JOBS_PER_CLIENT = 0x00DE;  // Max jobs per client @A7A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY66">Maximum spooled output records</A>. **/
    public static final int ATTR_MAXRCDS      = 0x0042;  // *maximum number of records allowed
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY67">Measurement method</A>. **/
    public static final int ATTR_MEASMETHOD   = 0x004F;  // measurement method (*ROWCOL or *UOM)
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY65">Manufacturer type and model</A>. **/
    public static final int ATTR_MFGTYPE      = 0x0041;  // manufacturer's type & model
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEYIFS_5">Message queue integrated file system name</A>. **/
    public static final int ATTR_MESSAGE_QUEUE = -5;  // Message Queue IFSPath
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY70">Message queue library name</A>.  **/
           static final int ATTR_MSGQUELIB    = 0x0044;  // message queue library
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY71">Message queue</A>. **/
           static final int ATTR_MSGQUE       = 0x005E;  // message queue
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY68">Message help</A>. **/
    public static final int ATTR_MSGHELP      = 0x0081;  // message help text
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY69">Message ID</A>. **/
    public static final int ATTR_MSGID        = 0x0093;  // message ID
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY72">Message reply</A>. **/
    public static final int ATTR_MSGREPLY     = 0x0082;  // message reply
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY74A">Message severity</A>. **/
    public static final int ATTR_MSGSEV       = 0x009F; // Message severity
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY73">Message text</A>. **/
    public static final int ATTR_MSGTEXT      = 0x0080;  // message text
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY74">Message type</A>. **/
    public static final int ATTR_MSGTYPE      = 0x008E;  // message type
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY200">Multi-item reply capability</A>. **/   /* @A7A*/
    public static final int ATTR_MULTI_ITEM_REPLY = 0x00DC;  // multiple item reply capable @A7A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY85">Pages per side</A>. **/
    public static final int ATTR_MULTIUP      = 0x0052;  // logical pages per physical side
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY76.5">Network where output was created</A>. **/
    public static final int ATTR_NETWORK      = 0x00BD;  // network where created @A1A
    /** NLV ID **/
           static final int ATTR_NLV_ID         = 0x00B4; // NLV ID (ie: "2924");
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY142">NPS CCSID</A>. **/
    public static final int ATTR_NPSCCSID     = 0x008A;  // server CCSID
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY143">NPS object</A>. **/
           static final int ATTR_NPSOBJECT    = 0x008B;  // Object ID
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY144">NPS object action</A>. **/
           static final int ATTR_NPSACTION    = 0x008C;  // Action ID
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY145">NPS level</A>. **/
    public static final int ATTR_NPSLEVEL     = 0x008D;  // server code level
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY75">Number of bytes to read/write</A>. **/
    public static final int ATTR_NUMBYTES     = 0x007D;  // number of bytes to read/write
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY174">Number of bytes available in a stream or spooled file</A>. **//* @A4A */
    public static final int ATTR_NUMBYTES_SPLF= 0x00D9;  // number of bytes available in a stream/spooled file    @A4A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY76">Number of files</A>. **/
    public static final int ATTR_NUMFILES     = 0x0045;  // total spooled files no output queue
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY77">Number of writers started to queue</A> **/
    public static final int ATTR_NUMWRITERS   = 0x0091;  // number of writers started to queue
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY259">Number of user resource library list entries</A>. **/
    public static final int ATTR_NUMRSC_LIB_ENT  = 0x0139;  // AT_NUMRSC_LIB_ENT  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY771">Object extended attribute</A>. **/
    public static final int ATTR_OBJEXTATTR   = 0x00B1; // Object extended attribute
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY77.3">On job queue status</A>. **/
    public static final int ATTR_ONJOBQSTS   = 0x00CD;  // indicates whether the writer is on a job queue and @A2A
                                                        // therefore is not currently running
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY772">Open time commands</A>. **/
    public static final int ATTR_OPENCMDS     = 0x00A0; // Open time commands on read (for SCS)
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY78">Operator controlled</A>. **/
    public static final int ATTR_OPCNTRL      = 0X0046;  // operator controlled
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY79">Order of files On queue</A>. **/
    public static final int ATTR_ORDER        = 0X0047;  // order on queue (sequence) - *FIFO, *JOBNBR
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY80">Output priority</A>. **/
    public static final int ATTR_OUTPTY       = 0x0048;  // output priority
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY253">OS/400-create AFPDS</A>.  **/
    public static final int ATTR_OS4_CRT_AFP  = 0x0135;  // AT_OS4_CRT_AFP  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY83.5">Output bin</A>. **/
    public static final int ATTR_OUTPUTBIN    = 0x00C0;  // output bin @A1A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEYIFS_6">Output queue integrated file system name</A>. **/
    public static final int ATTR_OUTPUT_QUEUE = -6;  // Output Queue IFSPath
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY81">Output queue library name</A> **/
           static final int ATTR_OUTQUELIB    = 0x0049;  // output queue library
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY82">Output queue name</A>. **/
           static final int ATTR_OUTQUE       = 0x004A;  // output queue
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY83">Output queue status</A>. **/
    public static final int ATTR_OUTQSTS      = 0x004B;  // output queue status
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY87.5">Overall status of printer</A>. **/
    public static final int ATTR_OVERALLSTS   = 0x00C8;  // overall status of printer @A1A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY84">Overflow line number</A>. **/
    public static final int ATTR_OVERFLOW     = 0x004C;  // overflow line number
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY171">Page at a time</A>. **/     /* @A4A */
    public static final int ATTR_PAGE_AT_A_TIME = 0x00D6;  // page at a time    @A4A
     // Lines Per Page is 0x004D and isn't used anymore - use 0x004E instead
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEYIFS_D">Page definition integrated file system name</A>. **/
    public static final int ATTR_PAGE_DEFINITION = -13;  // Pagedef IFSPath  /* @A7A */
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY176A">Page definition library name</A>. **/ /* @A7A*/
           static final int ATTR_PAGDFNLIB   = 0x00F5; // Page definition library name @A7A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY176B">Page definition name</A>. **/         /* @A7A*/
           static final int ATTR_PAGDFN      = 0x00F6; // Page definition name         @A7A                                                                                     
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY62">Length of page</A>. **/
    public static final int ATTR_PAGELEN      = 0x004E;  // page length in Units of Measurement
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY172">Page number</A>. **/        /* @A4A */
    public static final int ATTR_PAGENUMBER   = 0x00D7;  // page number         @A4A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY115">Total pages</A>. **/
    public static final int ATTR_PAGES        = 0x006F;  // number of pages in spool file
   /** <A HREF="doc-files/PrintAttributes.html#HDRKEY175">Total pages estimated</A>. **/                            /* @A4A */
    public static final int ATTR_PAGES_EST    = 0x00DA;  // indicates if the number of pages is estimated    @A4A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY31">Degree of page rotation</A>. **/
    public static final int ATTR_PAGRTT       = 0x0024;  // degree of page rotation
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY31">Page Rotate</A>.  **/
    public static final int ATTR_PAGE_ROTATE  = 0x012A;   // AT_PAGE_ROTATE  @ABA
    // Chars per Line is 0x0050 and isn't used anymore - use 0x004E instead
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY126">Width of page</A>. **/
    public static final int ATTR_PAGEWIDTH    = 0x0051;  // width of page in Units of Measure
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY851">Pel density</A>. **/
    public static final int ATTR_PELDENSITY   = 0x00B2; // Font Pel Density "1"=240;"2"=300;
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY169">Paper Source 1</A>. **/     /* @A4A */
    public static final int ATTR_PAPER_SOURCE_1 = 0x00D4; // paper source 1     @A4A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY170">Paper Source 2</A>. **/     /* @A4A */
    public static final int ATTR_PAPER_SOURCE_2 = 0x00D5; // paper source 2     @A4A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY86">Point size</A>. **/
    public static final int ATTR_POINTSIZE    = 0x0053;  // the default font's point size
    /** Precompute number of bytes on open **/
           static final int ATTR_PRECOMPUTE_NUMBYTES = 0x00B8;  // Precompute Number of bytes on open
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY218.1">Program that opened file library name</A>. **/
    public static final int ATTR_PGM_OPN_LIB  = 0x010F; // AT_PGM_OPN_LIB  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY218.2">Program that opened file name</A>. **/
    public static final int ATTR_PGM_OPN_FILE = 0x0110; // AT_PGM_OPN_FILE  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY89">Print quality</A>. **/
    public static final int ATTR_PRTQUALITY   = 0x0056;  // print quality
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY90">Print sequence</A>. **/
    public static final int ATTR_PRTSEQUENCE  = 0x0057;  // print sequence
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY91">Print text</A>. **/
    public static final int ATTR_PRTTEXT      = 0x0058;  // text printed at bottom of each page
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY92">Printer</A>. **/
    public static final int ATTR_PRINTER      = 0x0059;  // printer device name
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY97.5">Printer assigned</A>. **/
    public static final int ATTR_PRTASSIGNED  = 0x00BA;  // printer assigned @A1A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY93">Printer device type</A>. **/
    public static final int ATTR_PRTDEVTYPE   = 0x005A;  // printer dev type (data stream type (*SCS, *AFPDS, etc))
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEYIFS_7">Printer file integrated file system name</A>. **/
    public static final int ATTR_PRINTER_FILE = -7;  // Printer File IFSPath
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY94">Printer file library name</A>. **/
           static final int ATTR_PRTFLIB      = 0x005B;  // printer file library
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY95">Printer file name</A>. **/
           static final int ATTR_PRTFILE      = 0x005C;  // printer file
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY207">Color supported indicator</A>. **/
    public static final int ATTR_PUBINF_COLOR_SUP = 0x0101; //Color supported indicator @A9A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY208">Pages per minute (color printing)</A>. **/
    public static final int ATTR_PUBINF_PPM_COLOR = 0x0102; //Pages per minute (color printing) @A9A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY212">Data Stream supported</A>. **/
    public static final int ATTR_PUBINF_DS = 0x0106; //Data Stream supported  @A9A 
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY209">Pages per minute (monochrome printing)</A>. **/
    public static final int ATTR_PUBINF_PPM = 0x0103; //Pages per minute (monochrome printing) @A9A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY210">Duplex supported indicator</A>. **/
    public static final int ATTR_PUBINF_DUPLEX_SUP = 0x0104; //Duplex supported indicator @A9A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY211">Published location description</A>. **/
    public static final int ATTR_PUBINF_LOCATION = 0x0105; //Published location description @A9A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY205">Printer device location name</A>. **/
    public static final int ATTR_RMTLOCNAME  = 0x00FF;  // printer device location name @A9A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY96">Printer queue</A>. **/
    public static final int ATTR_RMTPRTQ      = 0x005D;  // Remote print queue used on SNDTCPSPLF
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY97">Record length</A>. **/
    public static final int ATTR_RECLENGTH    = 0x005F;  // record length
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY102.5">Reduce output</A>. **/
    public static final int ATTR_REDUCE       = 0x00C2;  // Reduce output @A1A
           static final int ATTR_RESOURCE_AVAIL = 0x00B3; // resource is available
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY98">Remote system</A>. **/
    public static final int ATTR_RMTSYSTEM    = 0x0060;  // remote system
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY99">Replace unprintable characters</A>. **/
    public static final int ATTR_RPLUNPRT     = 0x0061;  // replace uNPrintable characters
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY100">Replacement character</A>. **/
    public static final int ATTR_RPLCHAR      = 0x0062;  // character to replace uNPrintables with
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY101">Restart printing</A>. **/
    public static final int ATTR_RESTART      = 0x0063;  // where to restart printing at
      /** <A HREF="doc-files/PrintAttributes.html#HDRKEY197">Saddle stitch number of staples</A>. **/   /* @A7A*/
    public static final int ATTR_SADDLESTITCH_NUMSTAPLES   = 0x00F3;  // edgesticth reference @A7A
        /** <A HREF="doc-files/PrintAttributes.html#HDRKEY196">Saddle stitch reference</A>. **/   /* @A7A*/
    public static final int ATTR_SADDLESTITCH_REF   = 0x00F2;  // saddlesticth reference @A7A
  /** <A HREF="doc-files/PrintAttributes.html#HDRKEY198">Saddle stitch staple offset info</A>. **/   /* @A7A*/
           static final int ATTR_SADDLESTITCH_STPL_OFFSEINFO   = 0x00F4;  // saddlesticth off. info @A7A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY102">Save spooled file</A>. **/
    public static final int ATTR_SAVE         = 0x0064;  // whether to save after printing or not
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY111">Spooled output schedule</A>. **/
    public static final int ATTR_SCHEDULE     = 0x006B;  // when available to the writer
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY116">Transform SCS to ASCII</A>. **/
    public static final int ATTR_SCS2ASCII    = 0x0071;  // transform SCS to ASCII
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY103">Seek offset</A>. **/
    public static final int ATTR_SEEKOFF      = 0x007E;  // seek offset
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY104">Seek origin</A>. **/
    public static final int ATTR_SEEKORG      = 0x007F;  // seek origin
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY105">Send priority</A>. **/
    public static final int ATTR_SENDPTY      = 0x0065;  // send priority
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY1051">Separator page</A>. **/
    public static final int ATTR_SEPPAGE      = 0x00A1; // Print banner page or not
    /**  **/
           static final int ATTR_SPLFSENDCMD  = 0x0092;  // spooled file send command
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY106">Source drawer</A>. **/
    public static final int ATTR_SRC_CODEPAGE = 0x0107;  // Source code page conversion   @AAA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY213">Source code page conversion</A>. **/
    public static final int ATTR_SRCDRWR      = 0x0066;  // source drawer
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY107">Spool the data</A>. **/
    public static final int ATTR_SPOOL        = 0x0067;  // spool the data
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY108">Spooled file name</A>. **/
    public static final int ATTR_SPOOLFILE    = 0x0068;  // spool file name
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY187">Spooled file Creation Authenication Method</A>. **/  /* @A7A*/
    public static final int ATTR_SPLF_AUTH_METHOD = 0x00E3;  // Spooled file creation auth method  @A7A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY255">Spooled file size</A>. **/
    public static final int ATTR_SPLF_SIZE    = 0x0136;     // AT_SPLF_SIZE  @ABA 
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY256">Spooled file size multiplier</A>.   **/
    public static final int ATTR_SPLF_SIZE_MULT = 0x0137;    // AT_SPLF_SIZE_MULT @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY188">Spooled file Creation Security Method</A>. **/  /* @A7A*/
    public static final int ATTR_SPLF_SECURITY_METHOD= 0x00E2;  // IPP Attributes-charset    @A7A
    
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY109">Spooled file number</A>. **/
    public static final int ATTR_SPLFNUM      = 0x0069;  // spool file number

    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY110">Spooled file status</A>. **/
    public static final int ATTR_SPLFSTATUS   = 0x006A;  // spool file status
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY1061">Spool SCS</A>. **/
    public static final int ATTR_SPLSCS       = 0x00AD; // Spool SCS attr on splfile
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY112">Starting page</A>. **/
    public static final int ATTR_STARTPAGE    = 0x006C;  // starting page to print
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY121.5">Started by</A>. **/
    public static final int ATTR_STARTEDBY    = 0x00C5;  // started by user @A1A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY122.5">System where output was created</A>. **/
    public static final int ATTR_SYSTEM       = 0x00BC;  // system where created @A1A
    /** <A HREF="../../../../PrintAttributes.html#HDRKEY214">Target code page conversion</A>. **/
    public static final int ATTR_TGT_CODEPAGE = 0x0108; // Target code page converstion @AAA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY250">System driver program name</A>. **/
    public static final int ATTR_SYS_DRV_PGM  = 0x0131;  // System driver program name @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY114">Time object created</A>. **/
    public static final int ATTR_TIME         = 0x006E;  // time spooled file was opened at
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY204">Time Job creating spooled file ended</A>. **/
    public static final int ATTR_TIME_END     = 0x00FE;  //Time job creating spooled file ended @A9A          
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY189">Time writer began procesing spooled file</A>. **/   /* @A7A*/
    public static final int ATTR_TIME_WTR_BEGAN_FILE = 0x00EC;  // time writer began file @A7A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY190">Time writer completed processing spooled filed</A>. **/   /* @A7A*/
    public static final int ATTR_TIME_WTR_CMPL_FILE = 0x00ED;  // time writer finished file @A7A
    
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY120">User ID</A>. **/
    public static final int ATTR_TOUSERID     = 0x0075;  // user id to send spool file to
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY121">User ID address</A>. **/
    public static final int ATTR_TOADDRESS    = 0x0076;  // address of user to send file to
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY117">Unit of measure</A>. **/
    public static final int ATTR_UNITOFMEAS   = 0x0072;  // unit of measure
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY118">User comment</A>. **/
    public static final int ATTR_USERCMT      = 0x0073;  // user comment
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY119">User data</A>. **/
    public static final int ATTR_USERDATA     = 0x0074;  // user data
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY1191">User defined data</A>. **/
    public static final int ATTR_USRDEFDATA   = 0x00A2; // User defined data
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY130.5">User defined file</A>. **/
    public static final int ATTR_USRDEFFILE   = 0x00C6; // User defined file @A1A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY1192">User defined option(s)</A>. **/
    public static final int ATTR_USRDEFOPT    = 0x00A3; // User defined options   
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEYIFS_9">User defined object integrated file system name</A>. **/
    public static final int ATTR_USER_DEFINED_OBJECT = -9;  // user defined object IFSPath
    /** <A HREF="#HDRWQ3">User defined object library</A>. **/
           static final int ATTR_USRDEFOBJLIB = 0x00A4; // User defined object library
    /** <A HREF="#HDRWQ4">User defined object name</A>. **/
           static final int ATTR_USRDEFOBJ    = 0x00A5; // User defined object
    /** <A HREF="#HDRWQ5">User defined object type</A>. **/
           static final int ATTR_USRDEFOBJTYP = 0x00A6; // User defined object type
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY221">User-defined text</A>. **/
    public static final int ATTR_USER_DFN_TXT = 0X0115; // AT_USER_DFN_TEXT @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY219">User who created file</A>. **/
    public static final int ATTR_USERGEN_DATA   = 0x0111; // AT_USER_GEN_DATA  @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEYIFS_A">User transform program integrated file system name</A>. **/
    public static final int ATTR_USER_TRANSFORM_PROG = -10;  // user transform program IFSPath
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY1211">User transform program library</A>. **/
           static final int ATTR_USRTFMLIB    = 0x00A7; // User transform program library
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY1212">User transform program name</A>. **/
           static final int ATTR_USRTFM       = 0x00A8; // User transform program
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY1193">User driver program data</A>. **/
    public static final int ATTR_USRDRVDATA   = 0x00A9; // User driver program data
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEYIFS_B">User driver program integrated file system name</A>. **/
    public static final int ATTR_USER_DRIVER_PROG = -11;  // user driver program IFSPath
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY1194">User driver program library</A>. **/
           static final int ATTR_USRDRVLIB    = 0x00AA; // User driver program library
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY1195">User driver program name</A>. **/
           static final int ATTR_USERDRV      = 0x00AB; // User driver program
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY173">Viewing fidelity</A>. **/      /*  @A4A */
    public static final int ATTR_VIEWING_FIDELITY = 0x00D8; // viewing fidelity     @A4A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY252"<User resource library list</A>. **/
    public static final int ATTR_RSC_LIB_LIST = 0x00F9;  // AT_USER_RESOURCE_LIBL @ABA
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY122">VM/MVS Class</A>. **/
    public static final int ATTR_VMMVSCLASS   = 0x0077;  //  VM/MVS SYSOUT class
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEYIFS_8">Workstation customization object integrated file system name</A>. **/
    public static final int ATTR_WORKSTATION_CUST_OBJECT = -8;  // Workstation Cust. obj IFSPath
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY126.5">Writing status</A>. **/
    public static final int ATTR_WRTNGSTS     = 0x00BB;  // indicates whether the printer is in writing status @A2A
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY127">Workstation customizing object name</A>. **/
           static final int ATTR_WSCUSTMOBJ   = 0x0095; // workstation customizing object
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY128">Workstation customizing object library</A>. **/
           static final int ATTR_WSCUSTMOBJL  = 0x0096; // workstation customizing object library
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY128.2">Waiting for data status</A>. **/
    public static final int ATTR_WTNGDATASTS = 0x00CB;  // indicates whether the writer has written all the @A2A
                                                        // data currently in the spooled file and is waiting
                                                        // for more data
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY128.4">Waiting for device status</A>. **/
    public static final int ATTR_WTNGDEVSTS  = 0x00C9;  // indicates whether the writer is waiting to get the  @A2A
                                                        // device from a job that is printing directly to the printer
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY128.6">Waiting for message status</A>. **/
    public static final int ATTR_WTNGMSGSTS   = 0x00CA;  // indicates whether the writer is wating for a reply @A2A
                                                         // to an inquiry message
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY123">When to automatically end writer</A>. **/
    public static final int ATTR_WTRAUTOEND   = 0x0078;  // when to automatically end writer
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY124">When to end writer</A>. **/
    public static final int ATTR_WTREND       = 0x0090;  // when to end the writer
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY551">Initialize the writer</A>. **/
    public static final int ATTR_WTRINIT      = 0x00AC; // When to initialize the writer
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY129">Writer job name</A>.  **/
    public static final int ATTR_WTRJOBNAME   = 0x0079;  // writer job name
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY130">Writer job number</A>. **/
    public static final int ATTR_WTRJOBNUM    = 0x007A;  // writer job number
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY131">Writer job status</A>. **/
    public static final int ATTR_WTRJOBSTS    = 0x007B;  // writer job status
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY132">Writer job user name</A>. **/
    public static final int ATTR_WTRJOBUSER   = 0x007C;  // writer job user name
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY133">Writer starting page</A>. **/
    public static final int ATTR_WTRSTRPAGE   = 0x008F;  // page to start the writer on
    /** <A HREF="doc-files/PrintAttributes.html#HDRKEY166">Writer started</A>. **/
    public static final int ATTR_WTRSTRTD    = 0x00C1;  // indicates whether the writer is started  @A2A


    // KEEP THIS CURRENT ***** KEEP THIS CURRENT ***** KEEP THIS CURRENT
    // KEEP THIS CURRENT ***** KEEP THIS CURRENT ***** KEEP THIS CURRENT
    // KEEP THIS CURRENT ***** KEEP THIS CURRENT ***** KEEP THIS CURRENT
    static final int                    MAX_ATTR_ID = 0x0139;  // last attribute ID @A4C @A7C @A9C @AAC

    static final String                 EMPTY_STRING = "";
    private static final String         SYSTEM = "system";

    NPCPAttribute                       attrs;
    private NPCPID                      cpID_;
    private int                         objectType_;
    private AS400                       system_;

    transient PrintObjectImpl           impl_;  // @A5A

    // These instance variables are not persistent.
    transient PropertyChangeSupport     changes;// @A5C - removed '= new();'
    transient VetoableChangeSupport     vetos;  // @A5C - remove '= new();'



    PrintObject(NPCPID idCodePoint,
                NPCPAttribute cpAttrs,
                int type)
    {
        
        cpID_ = idCodePoint;
        attrs = cpAttrs;
        objectType_ = type;
        system_ = null;
        initializeTransient();  // @A5A
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



    // Chooses the appropriate implementation.(Proxy or Remote)     // @A5A
    // Subclasses MUST supply the implementation to this method.    // @A5A
    abstract void chooseImpl()                                      // @A5A
    throws IOException, AS400SecurityException;                     // @B1A



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


    // @A6A
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
    public Integer getIntegerAttribute(int attributeID)
      throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              IOException,
              InterruptedException,
              RequestNotSupportedException
    {
        Integer aValue = null;                                  // @B2A
        if ((attrs != null) && (impl_ == null)                  // @B2A
            &&  (cpID_.converter_ != null)) {                   // @B2A
            aValue = attrs.getIntValue(attributeID);            // @B2A
        }                                                       // @B2A
        if (aValue == null) {                                   // @B2A
            if (impl_ == null)                                  // @A5A
                chooseImpl();                                   // @A5A
            aValue = impl_.getIntegerAttribute(attributeID);    // @B2C
            // update the attrs, since updateAttrs may have     // @A5A
            // been called on the remote side...                // @A5A
            attrs = impl_.getAttrValue();                       // @A5A
        }                                                       // @B2A
        return aValue;                                          // @B2C
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
    public Float getFloatAttribute(int attributeID)
       throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              IOException,
              InterruptedException,
              RequestNotSupportedException
    {
        Float aValue = null;                                    // @B2A
        if ((attrs != null) && (impl_ == null)                  // @B2A
            &&  (cpID_.converter_ != null)) {                   // @B2A
            aValue = attrs.getFloatValue(attributeID);          // @B2A
        }                                                       // @B2A
        if (aValue == null) {                                   // @B2A
            if (impl_ == null)                                  // @A5A
                chooseImpl();                                   // @A5A
            aValue = impl_.getFloatAttribute(attributeID);      // @B2C
            // update the attrs, since updateAttrs may have     // @A5A
            // been called on the remote side...                // @A5A
            attrs = impl_.getAttrValue();                       // @A5A
        }                                                       // @B2A
        return aValue;                                          // @B2C
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
    public String getStringAttribute(int attributeID)
       throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              IOException,
              InterruptedException,
              RequestNotSupportedException
    {
        String str = null;                                      // @B2A
        if ((attrs != null) && (impl_ == null)                  // @B2A
            &&  (cpID_.converter_ != null)) {                   // @B2A
            str = attrs.getStringValue(attributeID);            // @B2A
        }                                                       // @B2A
        if (str == null) {                                      // @B2A
            if (impl_ == null)                                  // @A5A
                chooseImpl();                                   // @A5A
            str = impl_.getStringAttribute(attributeID);        // @B2C
            // update the attrs, since updateAttrs may have     // @A5A
            // been called on the remote side...                // @A5A
            attrs = impl_.getAttrValue();                       // @A5A
        }                                                       // @B2A
        return str;                                             // @B2C
    }



    /**
     * Returns the server on which this object exists.
     * @return The server on which this object exists.
     **/
    final public AS400 getSystem()
    {
        return system_;
    }



    // @A5A - Added initializeTransient method
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
        initializeTransient();  // @A5A
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
        if (impl_ != null)                                          // @A5A
             impl_.setPrintObjectAttrs(cpID_, attrs, objectType_);  // @A5A
    }



    // @A5A - Added setImpl method
    /**
     * Set the system property of the PrintObject if necessary, as well
     * as the codepoint, attributes, and object type.
     **/
    void setImpl()
    throws IOException, AS400SecurityException                              // @B1A
    {
        system_.connectService(AS400.PRINT);                                // @B1A
        impl_.setSystem(system_.getImpl());     // @A6A set the system
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

        if (impl_ != null) {                                                        // @A5A
            Trace.log(Trace.ERROR, "Cannot set property 'system' after connect.");  // @A5A
            throw new ExtendedIllegalStateException( "system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED ); // @A5A
        }                                                                           // @A5A

        AS400 oldSystem = getSystem();

        // Tell any vetoers about the change. If anyone objects
        // we let the PropertyVetoException propagate back to
        // our caller.
        vetos.fireVetoableChange( SYSTEM, oldSystem, system );

        // No one vetoed, make the change.
        system_ = system;

        // we may need to pass on system... // @A5A
        if (impl_ != null)                  // @A5A
            impl_.setSystem(system_.getImpl()); // @A6A - added getImpl()

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

       // We have choosen to do nothing here for JavaBeans. We don't
       // think the Print Attributes are JavaBean properties and we
       // have not provided separate getters/setters.

        checkRunTimeState();

        if (impl_ == null)                              // @A5A
            chooseImpl();                               // @A5A
        impl_.update();                                 // @A5A
        // propagate any changes to attrs               // @A5A
        attrs = impl_.getAttrValue();                   // @A5A
    }
}


