///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VNPMRI.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.util.*;

/**
Locale-specific objects for the IBM Toolbox for Java.
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class VNPMRI extends ListResourceBundle
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
           // #TRANNOTE Before you add a new resource, please make
           // #TRANNOTE sure you are not duplicating another.  The
           // #TRANNOTE goal is to keep the amount of translatable
           // #TRANNOTE text down.
           // #TRANNOTE
           // #TRANNOTE NOTE TO TRANSLATORS: The format of a line of MRI
           // #TRANNOTE    is { "KEY", "value" },
           // #TRANNOTE
           // #TRANNOTE The key must be left alone so translate only the value.
           // #TRANNOTE

      { "ACTIVE", "Active" },
      { "ACTIVE_READER", "Active reader" },
      { "ACTIVE_WRITER", "Active writer" },
      { "ADV_FUNC_PRINTING", "Advanced Function Printing" },
      { "ADVANCED", "Advanced" },
      { "AFTER_ALL_FILES_PRINT", "After all files print" },
      { "AFTER_CURRENT_FILE_PRINTS", "After current file prints" },
      { "ALL", "All" },
      { "ALL_DATA", "All data" },
      { "ALLOW_DIRECT_PRINTING", "Allow direct printing" },
      { "AS36_DISABLED", "AS/36 disabled" },
      { "AS36_ENABLED", "AS/36 enabled" },
      { "AS400", "AS400" },
      { "AS400_PRINTER", "Printer" },
      { "AS400_PRINTERS", "Printers" },
      { "AT_COPY_END", "After the current copy" },
      { "AT_PAGE_END", "At the end of the page" },
      { "AUTOMATIC", "Automatic" },
      { "AVAILABLE", "Available" },
      { "AVAILABLE_PENDING", "Available pending" },

      { "BEING_CREATED", "Being created" },
      { "BEING_SENT", "Being sent" },
      { "BEING_SERVICED", "Being serviced" },
      { "BETWEEN_COPIES", "Between copies" },
      { "BETWEEN_FILES", "Between files" },

      { "CHANGES_TAKE_EFFECT", "Changes take effect" },
      { "CLOSED", "Closed" },
      { "CONNECT_PENDING", "Connect pending" },
      { "CONVERTING_FOR_AFP_PRINTER", "Converting for AFP printer" },
      { "COPIES", "Copies" },
      { "COPIES_LEFT", "Copies left" },
      { "COPIES_LEFT_1_255", "Copies left (1-255)" },
      { "CURRENT_FORM_TYPE", "Current form type" },
      { "CURRENT_FORM_TYPE_NOTIFICATION", "Current form type notification" },
      { "CURRENT_JOB", "Current job" },
      { "CURRENT_NUM_SEP_PAGES", "Current number of separator pages" },
      { "CURRENT_PAGE", "Current page" },
      { "CURRENT_PAPER_SIZE", "Current paper size" },               // @A1A
      { "CURRENT_SEPARATOR_DRAWER", "Current separator drawer" },
      { "CURRENT_USER", "Current user" },
      { "CURRENT_VALUES", "Current values" },
      { "CURRENT_VIEWING_FIDELITY", "Current viewing fidelity" },   // @A1A

      { "DAMAGED", "Damaged" },
      { "DATE_CREATED", "Date created" },
      { "DATE_SENT", "Date sent" },
      { "DEF_START_PAGE", "Default start page" },
      { "DEFERRED", "Deferred" },
      { "DELETE_AFTER_SENDING", "Delete after sending" },
      { "DESCRIPTION", "Description" },
      { "DESTINATION_OPTION", "Destination option" },
      { "DESTINATION_TYPE", "Destination type" },
      { "DEVICE", "Device" },
      { "DEVICE_DEFAULT", "Device default" },
      { "DEVICE_STATUS", "Device status" },
      { "DIAGNOSTIC_MODE", "Diagnostic mode" },
      { "DIRECT_PRINT", "Allow direct printing" },

      { "END_AUTOMATICALLY", "End automatically" },
      { "END_PENDING", "End pending" },
      { "ENDED", "Ended" },

      { "FAILED", "Failed" },
      { "FAILED_READER", "Failed reader" },
      { "FAILED_WRITER", "Failed writer" },
      { "FIDELITY_ABSOLUTE", "Absolute" },  // @A1A
      { "FIDELITY_CONTENT", "Content" },    // @A1A
      { "FILE_AFTER_ALL", "After all files print" },
      { "FILE_AFTER_CURRENT", "After the current file prints" },
      { "FILE_DEFAULT", "File default" },
      { "FILE_FIRST_AVAILABLE", "First available file" },
      { "FILE_FORM_ALIGNMENT", "Only on first file" },
      { "FILE_LAST", "Last file" },
      { "FINISHED", "Finished" },
      { "FINISHED_PRINTING", "Finished printing" },
      { "FIRST_FILE_NAME", "First file to print" },
      { "FIRST_FILE_NUMBER", "File number" },
      { "FIRST_JOB_NAME", "Job name" },
      { "FIRST_JOB_USER", "Job user" },
      { "FIRST_JOB_NUMBER", "Job number" },
      { "FIRST_START_PAGE", "Starting page" },
      { "FORM_ALIGN", "Forms alignment" },
      { "FORM_TYPE", "Form type" },
      { "FORM_TYPE_ALL", "All" },
      { "FORM_TYPE_NOTIFY", "Form type notification" },
      { "FORM_TYPE_STANDARD", "Standard" },
      { "FORM_TYPE_ALL_GBT", "All, grouped by type" },
      { "FORMS", "Forms" },
      { "FORMS_ALIGNMENT", "Forms alignment" },

      { "GENERAL", "General" },
      { "GO_TO_PAGE", "Go to page"},    // @A1A
      { "GROUP", "Group" },

      { "HELD", "Held" },
      { "HIGH_PRIORITY", "High priority" },
      { "HOLD_OUTPUT", "Hold output" },
      { "HOLD_PENDING", "Hold pending" },
      { "HOLD_PRINTER", "Hold printer" },

      { "IMMEDIATELY", "Immediately" },
      { "INCLUDE", "Include" },
      { "INFORMATION_MESSAGE", "Information message" },
      { "INFO_AND_INQUIRY_MESSAGE", "Informational and inquiry messages" },
      { "INQUIRY_MESSAGE", "Inquiry message" },
      { "INTERNET_ADDRESS", "Internet address" },

      { "JOB", "Job" },
      { "JOB_NAME", "Job name" },
      { "JOB_NUMBER", "Job number" },
      { "JOB_VALUE", "Job value" },

      { "LAST_PAGE", "Last page printed" },
      { "LIBRARY", "Library" },
      { "LIBRARY_LIST", "Library list" },
      { "LOCKED", "Locked" },

      { "MANUFACTURER_TYPE", "Manufacturer type" },
      { "MESSAGE", "Message" },
      { "MESSAGE_ID", "Message ID" },
      { "MESSAGE_HELP", "Message help" },
      { "MESSAGE_QUEUE", "Message queue" },
      { "MESSAGE_QUEUE_LIB_DESCRIPTION", "Message queue library" },
      { "MESSAGE_TYPE_INQUIRY", "Inquiry message" },
      { "MESSAGE_TYPE_INQ_INFO", "Informational and inquiry message" },
      { "MESSAGE_TYPE_INFO", "Informational message" },
      { "MESSAGE_TYPE_NONE", "No message" },
      { "MESSAGE_WAITING", "Message waiting" },

      { "MOVE_OUTPUT", "Move the output" },

      { "NEXT_FORM_TYPE", "Next form type" },
      { "NEXT_FORM_TYPE_NOTIFICATION", "Next form type notification" },
      { "NEXT_NUM_SEP_PAGES", "Next number of separator pages (0-9)" },
      { "NEXT_OUTPUT_QUEUE", "Next output queue" },
      { "NEXT_SEPARATOR_DRAWER", "Next separator drawer (1-255)" },
      { "NO", "No" },
      { "NO_MESSAGE", "No message" },
      { "NONE", "None" },                       // @A1A
      { "NORMAL_PRIORITY", "Normal priority" },
      { "NOT_ASSIGNED", "Not assigned" },
      { "NOT_SCHEDULED_TO_PRINT_YET", "Not scheduled to print yet" },
      { "NUMBER", "Number" },
      { "NUMBER_OF_SEP_PAGES", "Number of separator pages" },

      { "ON_JOB_QUEUE", "On job queue" },
      { "ONLY", "Only" },
      { "OPEN", "Open" },
      { "ORIGIN", "Origin" },
      { "OTHER", "Other" },
      { "OUTPUT_NAME", "Output name" },
      { "OUTPUT_QUEUE", "Output queue" },
      { "OUTPUT_QUEUE_LIB", "Output queue library" },
      { "OUTPUT_QUEUE_STATUS", "Output queue status" },
      { "OUTPUT_DESCRIPTION", "Output" },
      { "OUTQ_PRIORITY_1_9", "Priority on output queue (1-9)" },

      { "PAGE_LIMIT_EXCEEDED", "Page limit exceeded" },
      { "PAGE_OF", "Page &0 of &1" },           // @A1A
      { "PAGES", "Pages" },
      { "PAGES_PER_COPY", "Pages per copy" },
      { "PAPER_SIZE", "Paper Size" },           // @A1A
      { "PAPER_SIZE_LETTER", "Letter" },        // @A1A
      { "PAPER_SIZE_LEGAL", "Legal" },          // @A1A
      { "PAPER_SIZE_EXECUTIVE", "Executive" },  // @A1A
      { "PAPER_SIZE_LEDGER", "Ledger" },        // @A1A
      { "PAPER_SIZE_A3", "A3" },                // @A1A
      { "PAPER_SIZE_A4", "A4" },                // @A1A
      { "PAPER_SIZE_A5", "A5" },                // @A1A
      { "PAPER_SIZE_B4", "B4" },                // @A1A
      { "PAPER_SIZE_B5", "B5" },                // @A1A
      { "PAPER_SIZE_CONT80", "CONT80" },        // @A1A
      { "PAPER_SIZE_CONT132", "CONT132" },      // @A1A
      { "PENDING", "Pending" },
      { "POWERED_OFF_NOT_AVAILABLE", "Powered off or not yet available" },
      { "POWERED_OFF", "Powered off" },
      { "PRINTED_AND_KEPT", "Printed and kept" },
      { "PRINTER", "Printer" },
      { "PRINTER_DEFAULT", " Printer default" },
      { "PRINTER_OUTPUT_TO_HOLD", "Printer output to hold" },
      { "PRINTER_OUTPUT_TO_MOVE", "Printer output to move" },
      { "PRINTER_OUTPUT_TO_SEND", "Printer output to send" },
      { "PRINTER_TO_HOLD", "Printer to hold" },
      { "PRINTER_TO_START", "Printer to start" },
      { "PRINTER_TO_STOP", "Printer to stop" },
      { "PRINT_QUEUE", "Print queue" },
      { "PRINT_SEPARATOR_PAGE", "Print separator page" },
      { "PRINTERS", "Printers" },
      { "PRINTEROUTPUT_DESCRIPTION", "Printer Output" },
      { "PRINTEROUTPUT_NAME", "Printer Output" },
      { "PRINTERQUEUE", "Printer/Queue" },
      { "PRINTING", "Printing" },
      { "PRINT_SERVICES_FACILITY", "Print Services Facility/2" },
      { "PRIORITY", "Priority on output queue" },
      { "PROP_DESC_CURRENT_PAGE", "The page currently being viewed." },// @A1A
      { "PROP_NAME_CURRENT_PAGE", "currentPage" },                     // @A1A
      { "PROP_DESC_NUMBER_OF_PAGES", "The number of pages in the spooled file." }, // @A1A
      { "PROP_NAME_NUMBER_OF_PAGES", "numberOfPages" },                // @A1A
      { "PROP_DESC_NUMBER_OF_PAGES_ESTIMATED", "Indicates if the number of pages value is estimated." }, // @A1A
      { "PROP_NAME_NUMBER_OF_PAGES_ESTIMATED", "numberOfPagesEstimated" }, // @A1A
      { "PROP_DESC_PAPER_SIZE", "The paper size." },                   // @A1A
      { "PROP_NAME_PAPER_SIZE", "paperSize" },                         // @A1A
      { "PROP_DESC_PRINTER_PRINTER", "The printer that is associated with this object." },
      { "PROP_NAME_PRINTER_PRINTER", "printer" },
      { "PROP_DESC_PRINTERS_PRINTER_FILTER", "The filter that selects printers by name." },
      { "PROP_NAME_PRINTERS_PRINTER_FILTER", "printerFilter" },
      { "PROP_DESC_SPLF", "The spooled file." },                       // @A1A
      { "PROP_NAME_SPLF", "spooledFile" },                             // @A1A
      { "PROP_DESC_SPLF_FORMTYPE_FILTER", "The filter that selects files by their form type." },
      { "PROP_NAME_SPLF_FORMTYPE_FILTER", "formTypeFilter" },
      { "PROP_DESC_SPLF_OUTQ_FILTER", "The filter that selects files by the integrated file system name of the output queue containing them." },
      { "PROP_NAME_SPLF_OUTQ_FILTER", "queueFilter" },
      { "PROP_DESC_SPLF_USER_FILTER", "The filter that selects files by the user that created them." },
      { "PROP_NAME_SPLF_USER_FILTER", "userFilter" },
      { "PROP_DESC_SPLF_USERDATA_FILTER", "The filter that selects files by their user data." },
      { "PROP_NAME_SPLF_USERDATA_FILTER", "userDataFilter" },
      { "PROP_DESC_VIEWING_FIDELITY", "The fidelity used to format the spooled file for viewing." },// @A1A
      { "PROP_NAME_VIEWING_FIDELITY", "viewingFidelity" },             // @A1A

      { "READY", "Ready" },
      { "REPLY", "Reply" },
      { "RECORD_FORMAT", "Record format" },
      { "RECORD_DATA", "Record data only" },
      { "RECOVERY_CANCELLED", "Recovery canceled" },
      { "RECOVERY_PENDING", "Recovery pending" },
      { "RELEASED", "Released" },
      { "REMOTE_SYSTEM", "Remote system" },

      { "SAVE_AFTER_PRINTING", "Save after printing" },
      { "SAVED", "Saved" },
      { "SCS", "SNA character string" },
      { "SEND_PRIORITY", "Send priority" },
      { "SEND_TO", "Send to" },
      { "SENDING", "Sending" },
      { "SENT_TO_PRINTER", "Sent to printer" },
      { "SEPARATOR_DRAWER", "Separator Drawer" },
      { "SEPARATORS", "Separators" },
      { "SIGNON_DISPLAY", "Signon display" },
      { "STANDARD", "Standard" },
      { "STARTED", "Started" },
      { "STARTED_BY", "Started by" },
      { "STATUS", "Status" },
      { "STILL_BEING_CREATED", "Still being created" },
      { "STOP_PENDING", "Stop pending" },
      { "STOP_PRINTING", "Stop printing" },
      { "STOPPED", "Stopped" },
      { "SYSTEM_NAME", "System name" },

      { "TIME_CREATED", "Time created" },
      { "TOTAL_COPIES_1_255", "Total copies (1-255)" },
      { "TRANSFORM_DATA", "Transform data" },
      { "TYPE", "Type" },

      { "UNABLE_TO_VIEW", "Unable to view spooled file" }, // @A1A
      { "UNAVAILABLE", "Unavailable" },
      { "UNAVAILABLE_PENDING", "Unavailable pending" },
      { "UNKNOWN", "Unknown" },
      { "UNUSABLE", "Unusable" },
      { "USER", "User" },
      { "USER_COMMENT", "User comment" },
      { "USER_DEFAULT", "User default" },
      { "USER_DATA_TRANSFORM", "User data transform" },
      { "USER_DATA_TRANSFORM_LIB", "User data transform library" },
      { "USER_NAME", "User name" },
      { "USER_SPEC_DATA", "User-specified data" },
      { "USE__CURRENT_LIBRARY", "Use current library" },
      { "USE_LIBRARY_LIST", "Use library list" },

      { "VARIED_OFF", "Varied off" },
      { "VARIED_ON", "Varied on" },
      { "VARY_OFF_PENDING", "Vary off pending" },
      { "VARY_ON_PENDING", "Vary on pending" },
      { "VIEWING_FIDELITY", "Viewing Fidelity" },  // @A1A
      { "VM_MVS_CLASS", "VM/MVS Class" },

      { "WAITING_FOR_DATA", "Waiting for data" },
      { "WAITING_FOR_DEVICE", "Waiting for device" },
      { "WAITING_FOR_OUTQ", "Waiting for output queue" },
      { "WAITING_FOR_PRINTER", "Waiting for printer" },
      { "WAITING_FOR_PRINTER_OUTPUT", "Waiting for printer output" },
      { "WAITING_ON_JOB_QUEUE_QSPL", "Waiting on job queue QSPL" },
      { "WAITING_ON_MESSAGE", "Waiting on message" },
      { "WAITING_TO_START", "Waiting to start" },

        // #TRANNOTE #########################################  // @A1A
        // #TRANNOTE Text for panels appearing when             // @A1A
        // #TRANNOTE viewing fidelity or paper size is          // @A1A
        // #TRANNOTE requested to be changed.                   // @A1A
        // #TRANNOTE #########################################  // @A1A
        // #TRANNOTE When the user elects to change the         // @A1A
        // #TRANNOTE viewing fidelity or the paper size while   // @A1A
        // #TRANNOTE using the spooled file viewer, a warning   // @A1A
        // #TRANNOTE dialog box is displayed indicating the     // @A1A
        // #TRANNOTE spooled file will be reloaded              // @A1A
        // #TRANNOTE using the new attribute setting.           // @A1A
        // #TRANNOTE (The \n forces a new line)                 // @A1A
      { "WARNING_FIDELITY", " Changing the viewing fidelity will \n cause the viewer to reload the \n spooled file being viewed, adjusting for \n the new attribute setting." }, // @A1A
      { "WARNING_PAPER_SIZE", " Changing the paper size will cause \n the viewer to reload the \n spooled file being viewed, adjusting for \n the new attribute setting." },     // @A1A
      { "WARNING", "Warning" },  // @A1A


      { "WORKSTATION_CUST_OBJECT", "Workstation customization object" },
      { "WORKSTATION_CUST_OBJECT_LIB", "Workstation customization object library" },
      { "WRITER", "Writer" },
      { "WRITER_AUTO_END", "Automatically end writer" },
      { "WRITER_DEFAULT", "Writer default" },
      { "WRITER_NAME", " Writer name" },
      { "WRITER_STATUS", "Writer status" },
      { "WRITER_WHEN_TO_END", "When to end" },
      { "WRITING", "Writing" },

      { "YES", "Yes" },

   };




}

