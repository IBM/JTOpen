///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: CTMRI.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import java.util.ListResourceBundle;

/**
Locale-specific objects for the CommTrace Utility.
**/
public class CTMRI extends ListResourceBundle
{
   // NLS_MESSAGEFORMAT_NONE
   // Each string is assumed NOT to be processed by the MessageFormat class.
   // This means that a single quote must be coded as 1 single quote.

   // NLS_ENCODING=UTF-8
   // Instructs the translation tools to interpret the text as UTF-8.

   public Object[][] getContents()
   {
       return resources;
   }

   private final static Object[][] resources= {

{ "Transfer", "Transfer" },
{ "TransferDescription", "Copies a file from a system to the local PC" },
{ "Display", "Display " },
{ "DisplayButtonDescription", "Displays a previously formatted trace stored on the local PC" },
{ "Format", "Format" },
{ "FormatOpt", "Format Options" },
{ "FormatDescription", "Formats a trace stored on the local PC" },
{ "FormatRemoteDescription", "Formats a trace stored on a system and saves the result on a system" },
{ "DisplayRemoteDescription", "Displays a previously formatted trace that resides on a system" },
{ "Remote", "Remote" },
{ "Local", "Local" },
{ "File", "File" },
{ "Edit", "Edit" },
{ "Commtrace", "CommTrace" },
{ "Help", "Help" },
{ "Disconnect", "Disconnect" },
{ "Exit", "Exit" },
{ "HelpTopics", "Help Topics" },
{ "AboutCommtrace", "About CommTrace ..." },
{ "About", "About CommTrace" },
{ "Version", "Version" },
{ "OK", "OK" },
{ "Cancel", "Cancel" },
{ "EOCP", "E N D  O F  C O M P U T E R  P R I N T O U T" },
{ "Show", "Show Frames:" },
{ "ShowDescription", "The number of frames to display when the next button is selected" },
{ "DisplayDescription", "Frames Displayed:" },
{ "Prolog", "Prolog" },
{ "frames", "frames" },
{ "Prev", "Prev" },
{ "Next", "Next" },
{ "Save", "Save" },
{ "SaveasBinary", "Save As Binary" },
{ "Close", "Close" },
{ "Find", "Find..." },
{ "Reverse", "Reverse" },
{ "Copy", "Copy" },
{ "Clear", "Clear" },
{ "Cut", "Cut" },
{ "Paste", "Paste" },
{ "of", "of" },
{ "possible", "possible" },
{ "PreviousPage", "Previous Page" },
{ "Src/DestIPAddr", "Source/Destination IP address:" },
{ "IPPortnum", "IP port number:" },
{ "FmtBdcst", "Format broadcast" },
{ "Record", "Record" },
{ "MACAddress", "MAC Address" },
{ "Data", "Data" },
{ "Destination", "Destination" },
{ "Source", "Source" },
{ "Frame", "Frame" },
{ "Number", "Number" },
{ "S/R", "S/R" },
{ "Length", "Length" },
{ "Timer", "Timer" },
{ "Copyright", "(c) Copyright IBM Corp. 2002. All Rights Reserved" },
{ "Find", "Find" },
{ "FindNext", "Find Next" },
{ "Cancel", "Cancel" },
{ "MatchCase", "Match Case" },
{ "WrapSearch", "Wrap Search" },
{ "SaveAs", "Save As..." },
{ "FormatDialog", "Format..." },
{ "OpenDialog", "Open..." },
{ "AllFiles", "All files (*.*)" },
{ "CommTraceFiles", "CommTrace files (*.bin)" },
{ "FiletoView", "File to View" },
{ "EOCP", "E N D  O F  C O M P U T E R  P R I N T O U T" },
{ "PTITLE", " COMMUNICATIONS TRACE         " },
{ "PTRACDES", "Trace Description  . . . . . :" },
{ "PCFGOBJ", "Configuration object . . . . : " },
{ "POTYPE", "Type . . . . . . . . . . . . : " },
{ "POTYPEH", "1=Line, 2=Network Interface, 3=Network Server" },
{ "POBJPROT", "Object protocol  . . . . . . :" },
{ "PSTRTIME", "Start date/Time  . . . . . . :" },
{ "PENDTIME", "End date/Time  . . . . . . . :" },
{ "PBYTECOL", "Bytes collected  . . . . . . :" },
{ "PBUFSIZ", "Buffer size  . . . . . . . . :" },
{ "PBUFSIZH", "In bytes                      " },
{ "PDATDIR", "Data direction . . . . . . . :" },
{ "PDATDIRH", "1=Sent, 2=Received, 3=Both" },
{ "PBUFWRP", "Stop on buffer full  . . . . :" },
{ "PBUFWRPH", "Y=Yes, N=No " },
{ "PBYTES", "Number of bytes to trace" },
{ "PBYTESB", "  Beginning bytes  . . . . . :" },
{ "PBYTESBH", "Value, *CALC" },
{ "PBYTESE", "  Ending bytes   . . . . . . :" },
{ "PBYTESEH", "Value, *CALC" },
{ "PFORMAT", "Format Options:" },
{ "PCDNAME", "Controller name  . . . . . . :" },
{ "PCDNAMEH", "*ALL, name" },
{ "PDATTYP", "Data representation  . . . . :" },
{ "PDATTYPH", "1=ASCII, 2=EBCDIC, 3=*Calc" },
{ "PFMTTCP", "Format TCP/IP data only  . . :" },
{ "PIPADDR", "  IP address . . . . . . . . :" },
{ "PIPADDRH", "*ALL, address  " },
{ "PIPPORT", "  IP port  . . . . . . . . . :" },
{ "PIPPORTH", "*ALL, IP port " },
{ "PETHDAT", "Select Ethernet data . . . . :" },
{ "PETHDATH", "1=802.3, 2=ETHV2, 3=Both" },
{ "PBDCAST", "Format Broadcast data  . . . :" },
{ "PFMTOH", "Y=Yes, N=No  " },
{ "PRMTCD", "Remote Controller  . . . . . :" },
{ "PRMTMAC", "Remote MAC Address . . . . . :" },
{ "PRMTSAP", "Remote SAP . . . . . . . . . :" },
{ "PLCLSAP", "Local SAP  . . . . . . . . . :" },
{ "PPIPID", "IP Identifier  . . . . . . . :" },
{ "PRMTIP", "Remote IP Address  . . . . . :" },
{ "PRMTALLH", "Value, *ALL" },
{ "Record", "Record" },
{ "MACAddress", "MAC Address" },
{ "Data", "Data" },
{ "Destination", "Destination" },
{ "Source", "Source" },
{ "Frame", "Frame" },
{ "Number", "Number" },
{ "S/R", "S/R" },
{ "Length", "Length" },
{ "Timer", "Timer" },
{ "NotSupported", "**** N O T E : \n NOT A SUPPORTED LINE TYPE FOR FORMATTING - ONLY ETHERNET AND TOKENRING SUPPORTED" }

};
}
