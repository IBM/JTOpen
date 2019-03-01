///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DDMTerm.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 *DDM terms.  This class consists of defined constants for the DDM term code points.
**/
class DDMTerm
{
//@B1D  static final int ACCORD     = 0x1162; // Access order for ULDRECF
  static final int ACCSEC     = 0x106D; // Access method for exchange attributes - added by DDM server
  static final int ACCRDBRM   = 0x2201; // Access to RDB completed
  static final int ACCSECRD   = 0x14AC; // Access method reply from ACCSEC
  static final int AGENT      = 0x1403; // Agent resource manager
  static final int AGNPRMRM   = 0x1232; // Permanent agent error
  static final int ALTINDF    = 0x1423; // Alternate index file
  static final int BYTDR      = 0x0043; // Byte character
  static final int BYTSTRDR   = 0x0044; // Byte string
  static final int CHRSTRDR   = 0x0009; // Character string
  static final int CMBACCAM   = 0x1405; // Combined access access method
  static final int CMBKEYAM   = 0x1406; // Combined keyed access method
  static final int CMBRNBAM   = 0x1407; // Combined record number access method
  static final int CMDATHRM   = 0x121C; // Not authorized to command
  static final int CMDCHKRM   = 0x1254; // Command check
  static final int CMDCMPRM   = 0x124B; // Command processing complete reply
  static final int CMDNSPRM   = 0x1250; // Command not supported
  static final int CMMCTLTYP  = 0x11BA; // Commitment control type
  static final int CMMUOW     = 0x105A; // Commit
  static final int CMNAPPC    = 0x1444; // APPC communications manager - Used for pre-V4R2 connections
  static final int CMNTCPIP   = 0x1474; // TCP/IP communications manager - added by DDM server
  static final int CODPNTDR   = 0x0064; // Code point data - byte[2]
  static final int DCLFIL     = 0x102C; // Declare the file name
  static final int DCLNAM     = 0x1136; // Declared file name
  static final int DCLNAMRM   = 0x1256; // Invalid declared name
  static final int DICTIONARY = 0x1458; // Dictionary manager
  static final int DIRECTORY  = 0x1457; // Directory file
  static final int DIRFIL     = 0x140C; // Direct file
  static final int DRCAM      = 0x1419; // Directory access method
  static final int DRCNAM     = 0x1165; // Library name
  static final int DUPDCLRM   = 0x1255; // Duplicate declared file name reply
  static final int ENDUOWRM   = 0x220C; // End unit of work reply message
  static final int FILISOLVL  = 0x1472; // Commitment control lock level
  static final int FILNAM     = 0x110E; // File name
  static final int EXCSAT     = 0x1041; // Exchange server attributes
  static final int EXCSATRD   = 0x1443; // Exchange server attributes reply
  static final int EXTNAM     = 0x115E; // External name
  static final int KEYFIL     = 0x141E; // Keyed file
  static final int KEYORD     = 0x145D; // Key order
  static final int LCKMGR     = 0x1422; // Lock manager
  static final int LUWHLDCSR  = 0x11B5; // Hold cursor parameter
  static final int MGRDEPRM   = 0x1218; // Manager dependency error
  static final int MGRLVLLS   = 0x1404; // Manager level list
  static final int NAMDR      = 0x0066; // Name string
  static final int NAMSYMDR   = 0x0061; // Name string with only A-Z, 0-9 and '_'
  static final int PASSWORD   = 0x11A1; // Password for connecting
  static final int PRCCNVRM   = 0x1245; // Conversational protocol error
  static final int PRMNSPRM   = 0x1251; // Parameter not supported
  static final int RDB        = 0x240F; // Relational database
  static final int RDBACCRM   = 0x2207; // RDB currently accessed
  static final int RDBAFLRM   = 0x221A; // RDB access failed reply message
  static final int RDBATHRM   = 0x2203; // Not authorized to RDB
  static final int RDBNAM     = 0x2110; // Relational database name
  static final int RDBNFNRM   = 0x2211; // RDB not found
//@B1D  static final int RECCNT     = 0x111A; // Record count returned from ULDRECF
  static final int RECAL      = 0x1430; // Record attribute list
//@B1D  static final int RECORD     = 0x144A; // Record object returned from ULDRECF
  static final int RELKEYAM   = 0x1432; // Relative by key access method
  static final int RELRNBAM   = 0x1433; // Relative by record number access method
  static final int RLLBCKUOW  = 0x105B; // Rollback
  static final int RLSFILLK   = 0x1143; // Release file lock
  static final int RNBORD     = 0x145E; // Record number order
  static final int RNDKEYAM   = 0x1434; // Random by key access method
  static final int RNDRNBAM   = 0x1435; // Random by record number access method
  static final int RQSFILLK   = 0x1145; // Request file lock
  static final int RSCLMTRM   = 0x1233; // Resource limits reached
  static final int RSCRCVM    = 0x14A0; // @E0M
  static final int RTNINA     = 0x1155; // Return inactive records
  static final int SECCHK     = 0x106E; // Security check - added by DDM server
  static final int SECCHKCD   = 0x11A4; // Security check code - added by DDM server @B0A
  static final int SECCHKRD   = 0x1219; // Security check reply- added by DDM server
  static final int SECMEC     = 0x11A2; // Security mechanism - added by DDM server
  static final int SECMGR     = 0x1440; // Security manager
  static final int SECTKN     = 0x11DC; // Security token @B0A
  static final int SEQFIL     = 0x143B; // Sequential file
  static final int SQLAM      = 0x2407; // SQL Application manager
  static final int SRVCLSNM   = 0x1147; // Server class name
  static final int SRVDGN     = 0x1153; // Server diagnostic information
  static final int STRAM      = 0x1463; // Stream access method
  static final int STRFIL     = 0x1465; // Stream file
  static final int STRCMMCTL  = 0x105C; // Start commitment control
  static final int SUPERVISOR = 0x143C; // Supervisor
  static final int SVRCOD     = 0x1149; // Severity code
  static final int SYNTAXRM   = 0x124C; // Data stream syntax error
  static final int SYSCMDMGR  = 0x147F; // System command manager
  static final int S38ALCOB   = 0xD002; // Allocate object (lock object)
  static final int S38BUF     = 0xD405; // Input/Output buffer
  static final int S38CLOSE   = 0xD004; // Close file
  static final int S38CLOST   = 0xD121; // Close type
  static final int S38CMD     = 0xD006; // Submit remote command
  static final int S38CMDST   = 0xD103; // Command string
  static final int S38CTLL    = 0xD105; // Control list
  static final int S38DEL     = 0xD007; // Delete record
  static final int S38DLCOB   = 0xD008; // Deallocate object (release explicit locks)
  static final int S38FEOD    = 0xD00B; // Force end of data
  static final int S38GET     = 0xD00C; // Get record
  static final int S38GETD    = 0xD00D; // Get record at file position
  static final int S38GETK    = 0xD00E; // Get record by key
  static final int S38GETM    = 0xD00F; // Get multiple records
  static final int S38IOFB    = 0xD402; // Input/output feedback
  static final int S38LCNRD   = 0xD406; // Number of files unlocked count reply object
  static final int S38LRLS    = 0xD115; // Lock release structure
  static final int S38LRQS    = 0xD10B; // Lock request structure
  static final int S38LWAIT   = 0xD10C; // Lock wait time
  static final int S38MDATA   = 0xD10E; // Message replacement data
  static final int S38MFILE   = 0xD111; // Message file
  static final int S38MID     = 0xD112; // Message id
  static final int S38MTEXT   = 0xD116; // Message text
  static final int S38MTYPE   = 0xD117; // Message type
  static final int S38MSGRM   = 0xD201; // AS400 error message reply
  static final int S38OPEN    = 0xD011; // Open file
  static final int S38OPNFB   = 0xD404; // File open feed back area
  static final int S38OPTL    = 0xD119; // Option list
  static final int S38PUT     = 0xD012; // Put record
  static final int S38PUTM    = 0xD013; // Put multiple record
  static final int S38UFCB    = 0xD11F; // User file control block structure
  static final int S38UPDAT   = 0xD019; // Update record
  static final int SXXASPRQ   = 0xD02A; // Set ASP group
  static final int SXXPUTDR   = 0xD01C; // Insert record at file position
  static final int TRGNSPRM   = 0x125F; // Target not supported
//@B1D  static final int ULDRECF    = 0x1040; // Unload records from file
  static final int UOWDSP     = 0x2115; // Unit of work disposition
  static final int USRID      = 0x11A0; // Userid for connecting
  static final int VALNSPRM   = 0x1252; // Parameter value not supported

}
