///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VMRI.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.util.*;

/**
Locale-specific objects for the AS/400 Toolbox for Java.
**/
public class VMRI extends ListResourceBundle
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   public Object[][] getContents()
   {
       return resources;
   }

   private final static Object[][] resources= {
           // #
           // # 5769-SS1
           // # (C) Copyright IBM Corp. 1997, 1998
           // # All rights reserved.
           // # US Government Users Restricted Rights -
           // # Use, duplication, or disclosure restricted
           // # by GSA ADP Schedule Contract with IBM Corp.
           // #
           // # Licensed Materials - Property of IBM
           // #

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
           // #TRANNOTE


           // #TRANNOTE #####################################################
           // #TRANNOTE Text for actions.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix ACTION_ and
           // #TRANNOTE then a short identifier to describe the
           // #TRANNOTE message.
           // #TRANNOTE
      { "ACTION_AVAILABLE", "Make available" },
      { "ACTION_CLEAR", "Clear" },
      { "ACTION_DIRECTORY_CREATE", "Create Directory" },
      { "ACTION_DELETE", "Delete" },
      { "ACTION_EDIT", "Edit" },
      { "ACTION_FILE_CREATE", "Create File" },
      { "ACTION_HOLD", "Hold" },
      { "ACTION_LIST_PROPERTIES", "List properties" },              // @D0A
      { "ACTION_MODIFY", "Modify" },
      { "ACTION_MOVE", "Move" },
      { "ACTION_PRINTNEXT", "Print next" },
      { "ACTION_PROPERTIES", "Properties" },
      { "ACTION_RELEASE", "Release" },
      { "ACTION_REMOVE", "Remove" },
      { "ACTION_RENAME", "Rename" },
      { "ACTION_REPLY", "Reply" },
      { "ACTION_SEND", "Send" },
      { "ACTION_START", "Start" },
      { "ACTION_STOP", "Stop" },
      { "ACTION_UNAVAILABLE", "Make unavailable" },
      { "ACTION_VIEW", "View" },

           // #TRANNOTE #####################################################
           // #TRANNOTE Text for column names.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix COLUMN_ and
           // #TRANNOTE then a short identifier to describe the
           // #TRANNOTE message.
           // #TRANNOTE
      { "COLUMN_ATTRIBUTES", "Attributes" },
      { "COLUMN_DESCRIPTION", "Description" },
      { "COLUMN_GROUP", "Group" },
      { "COLUMN_MODIFIED", "Modified" },
      { "COLUMN_NAME", "Name" },
      { "COLUMN_SIZE", "Size" },
      { "COLUMN_VALUE", "Value" },


           // #TRANNOTE #####################################################
           // #TRANNOTE Text for SQLResultSetForm and RecordListForm GUI.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix DBFORM_ and
           // #TRANNOTE then a short identifier to describe the text.

           // #TRANNOTE The number (index) of the record in this result set.
           // #TRANNOTE The ending colon is a separator between this label
           // #TRANNOTE and the value.
      { "DBFORM_LABEL_RECORD_NUMBER", "Record number:" },

           // #TRANNOTE Status message telling the user that there are no database
           // #TRANNOTE records to display.
      { "DBFORM_MSG_NO_DATA", "No data records available." },

           // #TRANNOTE Tooltip to display the first record.
      { "DBFORM_TOOLTIP_FIRST", "First" },

           // #TRANNOTE Tooltip to display the last record.
      { "DBFORM_TOOLTIP_LAST", "Last" },

           // #TRANNOTE Tooltip to display the next record.
      { "DBFORM_TOOLTIP_NEXT", "Next" },

           // #TRANNOTE Tooltip to display the previous record.
      { "DBFORM_TOOLTIP_PREVIOUS", "Previous" },

           // #TRANNOTE Tooltip to refresh the data.
      { "DBFORM_TOOLTIP_REFRESH", "Refresh" },


           // #TRANNOTE #####################################################
           // #TRANNOTE Text for dialog boxes.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix DLG_ and
           // #TRANNOTE then a short identifier to describe the
           // #TRANNOTE message.
           // #TRANNOTE
      { "DLG_ADD", "Add" },
      { "DLG_APPLY", "Apply" },
      { "DLG_CANCEL", "Cancel" },
      { "DLG_CHANGE", "Change" },
      { "DLG_CONFIRM_CLEAR", "Are you sure you want to clear this message queue?" },
      { "DLG_CONFIRM_CLEAR_TITLE", "Confirm Clear" },
      { "DLG_CONFIRM_DELETION", "Are you sure you want to delete this object?" },
      { "DLG_CONFIRM_DELETION_TITLE", "Confirm Delete" },
      { "DLG_CONFIRM_EXIT", "Are you sure you want to exit?" }, // @A1A
      { "DLG_CONFIRM_EXIT_TITLE", "Confirm Exit" },             // @A1A
      { "DLG_CONFIRM_REMOVE", "Are you sure you want to remove this object?" },
      { "DLG_CONFIRM_REMOVE_TITLE", "Confirm Remove" },
      { "DLG_CONFIRM_SAVE", "The text in the file has changed.  Do you want to save the changes?" },
      { "DLG_CONFIRM_SAVE_TITLE", "Confirm Save" },
      { "DLG_ERROR_TITLE", "Error" },
      { "DLG_FALSE", "False" },
      { "DLG_INVALID_INPUT", "Input not valid" },    // @A3A //@A8C
      { "DLG_OK", "OK" },
      { "DLG_NO", "No" },
      { "DLG_MODIFY", "Modify" },
      { "DLG_MODIFY_0", "Modify &0" },              // @D1A
      { "DLG_PROPERTIES_TITLE", "&0 Properties" }, // @A2C
      { "DLG_REMOVE", "Remove" },
      { "DLG_REPLACE", "Replace" },
      { "DLG_TRUE", "True" },
      { "DLG_YES", "Yes" },


           // #TRANNOTE #####################################################
           // #TRANNOTE Events.
           // #TRANNOTE #####################################################
           // #TRANNOTE Event descriptors starts with the prefix EVT_DESC,
           // #TRANNOTE event names start with EVT_NAME and
           // #TRANNOTE then a short identifier to describe the message.
           // #TRANNOTE
      { "EVT_DESC_DATA_QUEUE", "A data queue event has occurred." },

           // #TRANNOTE A data queue event has occurred.
      { "EVT_NAME_DATA_QUEUE", "dataQueue" },

      { "EVT_DESC_DOCUMENT", "A document event has occurred." },

           // #TRANNOTE A document event has occurred.
      { "EVT_NAME_DOCUMENT", "document" },

      { "EVT_DESC_ERROR", "An error has occurred." },

           // #TRANNOTE An error has occurred.
      { "EVT_NAME_ERROR", "error" },

      { "EVT_DESC_FILE", "A file event has occurred." },

           // #TRANNOTE A file event has occurred.
      { "EVT_NAME_FILE", "file" },

      { "EVT_DESC_ACTION_COMPLETED", "The action has completed." },

           // #TRANNOTE The action has completed.
      { "EVT_NAME_ACTION_COMPLETED", "actionCompleted" },

      { "EVT_DESC_DOCUMENT", "The document has changed." },

           // #TRANNOTE The document has changed.
      { "EVT_NAME_DOCUMENT", "document" },

      { "EVT_DESC_LIST_DATA", "The data in the list model has changed." },

           // #TRANNOTE The data in the list model has changed.
      { "EVT_NAME_LIST_DATA", "listData" },

      { "EVT_DESC_LIST_SELECTION", "A selection was made." },

           // #TRANNOTE A selection was made.
      { "EVT_NAME_LIST_SELECTION", "listSelection" },

           // #TRANNOTE The meaning of 'property' here is like 'attribute'.
      { "EVT_DESC_PROPERTY_CHANGE", "A bound property has changed." },

           // #TRANNOTE A bound property has changed.
           // #TRANNOTE The meaning of 'property' here is like 'attribute'.
      { "EVT_NAME_PROPERTY_CHANGE", "propertyChange" },

           // #TRANNOTE The meaning of 'property' here is like 'attribute'.
      { "EVT_DESC_PROPERTY_VETO", "A constrained property has changed." },

           // #TRANNOTE A constrained property has changed.
           // #TRANNOTE The meaning of 'property' here is like 'attribute'.
      { "EVT_NAME_PROPERTY_VETO", "vetoableChange" },

      { "EVT_DESC_TABLE_MODEL", "The data in the table model has changed." },

           // #TRANNOTE The data in the table model has changed.
      { "EVT_NAME_TABLE_MODEL", "tableModel" },

      { "EVT_DESC_TREE_EXPANSION", "Part of the tree was expanded or collapsed." },

           // #TRANNOTE Part of the tree was expanded or collapsed.
      { "EVT_NAME_TREE_EXPANSION", "treeExpansion" },

      { "EVT_DESC_TREE_MODEL", "The data in the tree model has changed." },

           // #TRANNOTE The data in the tree model has changed.
      { "EVT_NAME_TREE_MODEL", "treeModel" },

      { "EVT_DESC_TREE_SELECTION", "A tree selection was made." },

           // #TRANNOTE A tree selection was made.
      { "EVT_NAME_TREE_SELECTION", "treeSelection" },

      { "EVT_DESC_UNDOABLE_EDIT", "An edit operation which is undoable occurred." },

           // #TRANNOTE An edit operation which is undoable occurred.
      { "EVT_NAME_UNDOABLE_EDIT", "undoableEdit" },

      { "EVT_DESC_VOBJECT", "An AS400 object has been changed, created, or deleted." },

           // #TRANNOTE An AS400 object has been changed, created, or deleted.
      { "EVT_NAME_VOBJECT", "vobject" },

      { "EVT_DESC_WORKING", "A request has started or completed." },

           // #TRANNOTE A request has started or completed.
      { "EVT_NAME_WORKING", "working" },


           // #TRANNOTE #####################################################
           // #TRANNOTE Common exception messages.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix EXC_ and
           // #TRANNOTE then a short identifier to describe the
           // #TRANNOTE message.
           // #TRANNOTE
      { "EXC_AS400_ERROR", "An error occurred on the server." },
      { "EXC_DIRECTORY_NOT_CREATED", "Directory not created." },
      { "EXC_FILE_ALREADY_EXISTS", "File already exists." },
      { "EXC_FILE_NOT_CREATED", "File not created." },
      { "EXC_FILE_NOT_DELETED", "File or directory not deleted." },
      { "EXC_FILE_NOT_FOUND", "File not found." },
      { "EXC_FILE_NOT_RENAMED", "File or directory not renamed." },

           // #TRANNOTE The database connection or table property
           // #TRANNOTE has not been set.
      { "EXC_NO_TABLE", "No tables were specified." },

           // #TRANNOTE The table property is not formatted correctly.
      { "EXC_TABLE_SPEC_NOT_VALID", "Table specification not valid." },

           // #TRANNOTE The specified column identifier is not in the table.
      { "EXC_COLUMN_NOT_VALID", "Column identifier not valid." },

           // #TRANNOTE The row index is not in the valid range for the table.
      { "EXC_ROW_NOT_VALID", "Row index out of range." },



           // #TRANNOTE #####################################################
           // #TRANNOTE Text for general text that does not fit into the other
           // #TRANNOTE categories and may be used by more than one component.
           // #TRANNOTE #####################################################
           // #TRANNOTE There is no set pattern for these keys.
           // #TRANNOTE
//@B1D        { "DATE_FORMAT" , "MM/dd/yyyy" },
//@B1D        { "TIME_FORMAT" , "hh:mm:ss" },
        { "LIBRARY", "Library" }, //@A5A


           // #TRANNOTE #####################################################
           // #TRANNOTE Text for IFS GUIs.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix IFS_ and
           // #TRANNOTE then a short identifier to describe the
           // #TRANNOTE message.
           // #TRANNOTE
      { "IFS_ATTRIBUTES", "Attributes" },
      { "IFS_BYTE", "byte" },
      { "IFS_BYTES", "bytes" },
      { "IFS_CONTAINS", "Contains" },
      { "IFS_ALL_FILES_FILTER", "All files" },
      { "IFS_DIRECTORIES", "Directories" },
      { "IFS_DIRECTORY", "Directory" },
      { "IFS_DIRECTORY_DESCRIPTION", "Directory" },
      { "IFS_FILE", "File" },
      { "IFS_FILE_DESCRIPTION", "File" },
      { "IFS_FILE_NAME", "File name" },
      { "IFS_FILES", "Files" },
      { "IFS_FILES_OF_TYPE", "Files of type" },
      { "IFS_LOCATION", "Location" },
      { "IFS_MODIFIED", "Modified" },
      { "IFS_NAME", "File System" },
      { "IFS_NEW_DIRECTORY", "New Folder" },
      { "IFS_NEW_FILE", "New File" },
      { "IFS_READ", "Read" },
      { "IFS_READ_ABBREVIATION", "R" },
      { "IFS_SIZE", "Size" },
      { "IFS_ALL_FILES_FILTER", "Text files" },
      { "IFS_WRITE", "Write" },
      { "IFS_WRITE_ABBREVIATION", "W" },



           // #TRANNOTE #####################################################
           // #TRANNOTE Text for Job GUIs.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix JOB_ and
           // #TRANNOTE then a short identifier to describe the
           // #TRANNOTE message.
           // #TRANNOTE
      { "JOB_ACCOUNTING_CODE", "Job accounting code"},
      { "JOB_ACTIVE_DATE", "Date job became active"},
      { "JOB_ACTIVE_TIME", "Time job became active"},
      { "JOB_AUXILIARY_IO_REQUESTS", "Auxiliary I/O Requests" },
      { "JOB_BREAK_MESSAGE_HANDLING", "Break message handling"},
      { "JOB_CCSID", "CCSID"},
      { "JOB_COMPLETION_STATUS", "Job completion status"},
      { "JOB_COUNTRY_ID", "Country ID"},
      { "JOB_CPU_USED", "CPU Used" },
      { "JOB_CURRENT_LIB_EXISTENCE", "Current library existence"},
      { "JOB_CURRENT_LIB", "Current library if one exists"},
      { "JOB_DATE", "Job date" },
      { "JOB_DATE_ENTERED_SYSTEM", "Date entered system" },
      { "JOB_DATE_FORMAT", "Date format"},
      { "JOB_DATE_SEPARATOR", "Date separator"},
      { "JOB_DDM_CONVERSATION_HANDLING", "DDM conversation action"},
      { "JOB_DECIMAL_FORMAT", "Decimal format"},
      { "JOB_DEFAULT_CCSID", "Default CCSID"},
      { "JOB_DEFAULT_WAIT", "Default waiting time"},
      { "JOB_DESCRIPTION" , "Description" },
      { "JOB_DESCRIPTION_NAME", "Job description" },
//@A5D      { "JOB_DESCRIPTION_LIB", "    Library" },
      { "JOB_DEVICE_RECOVERY_ACTION", "Device recovery action"},
      { "JOB_END_SEVERITY", "End severity"},
      { "JOB_ENTERED_SYSTEM_DATE", "Date entered system"},
      { "JOB_ENTERED_SYSTEM_TIME", "Time entered system"},
      { "JOB_FUNCTION", "Function" },
      { "JOB_FUNCTION_NAME", "Function name" },
      { "JOB_FUNCTION_TYPE", "Function type" },
      { "JOB_INTERACTIVE_TRANSACTIONS", "Interactive transactions" },
      { "JOB_INQUIRY_MESSAGE_REPLY", "Inquiry message reply"},
      { "JOB_LANGUAGE_ID", "Language ID"},
      { "JOB_LIST_DESCRIPTION", "Job list" },
      { "JOB_LOG_MESSAGE_DESCRIPTION", "Job log message" },
      { "JOB_LOGGING_CL_PROGRAMS", "Logging of CL programs"},
      { "JOB_LOGGING_LEVEL", "Logging level"},
      { "JOB_LOGGING_SEVERITY", "Logging severity"},
      { "JOB_LOGGING_TEXT", "Logging text"},
      { "JOB_MESSAGE_QUEUE_FULL_ACTION", "Message queue full action"},
      { "JOB_MESSAGE_QUEUE_MAXIMUM_SIZE", "Message queue maximum size"},
      { "JOB_MODE_NAME", "Job mode"},
      { "JOB_NAME", "Job name" },
      { "JOB_NUMBER", "Job number" },
      { "JOB_NUMBER_OF_LIBRARIES_IN_SYSLIBL", "Number of libraries in SYSLIBL"},
      { "JOB_NUMBER_OF_LIBRARIES_IN_USRLIBL", "Number of libraries in USRLIBL"},
      { "JOB_NUMBER_OF_PRODUCT_LIBRARIES", "Number of product libraries"},
      { "JOB_POOL_IDENTIFIER", "Pool identifier"},
      { "JOB_PRINTER_DEVICE_NAME", "Printer device"},
      { "JOB_PRINT_KEYFORMAT", "Print key format"},
      { "JOB_PRINT_TEXT", "Print text"}, //@A5C: Removed leading blanks
      { "JOB_PRODUCT_LIBL", "Product libraries if they exist"},
      { "JOB_PURGE", "Eligible for purge"},
      { "JOB_PUTON_JOBQUEUE_DATE", "Date job put on job queue"},
      { "JOB_PUTON_JOBQUEUE_TIME", "Time job put on job queue"},
      { "JOB_QUEUE", "Job queue" },
      { "JOB_QUEUE_NAME", "Job queue" },
//@A5D      { "JOB_QUEUE_LIB", "   Library" },
      { "JOB_OUTPUT_QUEUE_NAME", "Output queue" },
//@A5D      { "JOB_OUTPUT_QUEUE_LIB", "   Library" },
      { "JOB_OUTPUT_QUEUE_PRIORITY", "Output queue priority" },
      { "JOB_QUEUE_PRIORITY", "Job queue priority" },
      { "JOB_ROUTING_DATA", "Routing data"},
      { "JOB_RUN_PRIORITY", "Run priority" },
      { "JOB_SCHEDULE_DATE", "Date scheduled to run"},
      { "JOB_SCHEDULE_TIME", "Time scheduled to run"},
      { "JOB_SIGNED_ON_JOB", "Signed-on job"},
      { "JOB_SORT_SEQUENCE_NAME", "Sort sequence"},
//@A5D      { "JOB_SORT_SEQUENCE_LIB", "    Library"},
      { "JOB_STATUS", "Status" },
      { "JOB_STATUS_IN_JOBQUEUE", "Job status in job queue" },
      { "JOB_STATUS_MESSAGE_HANDLING", "Status message handling"},
      { "JOB_SUBSYSTEM", "Subsystem" },
      { "JOB_SUBSYSTEM_NAME", "Subsystem name" },
//@A5D      { "JOB_SUBSYSTEM_LIB", "    Library" },
      { "JOB_SUBTYPE", "Subtype" },
      { "JOB_SWITCHES", "Job switches"},
      { "JOB_SYSTEM_LIBL", "System library list"},
      { "JOB_SYSTEM_POOL_IDENTIFIER", "System pool identifier" },
      { "JOB_TIME_SEPARATOR", "Time separator"},
      { "JOB_TIME_SLICE", "Time slice"},
      { "JOB_TIME_SLICE_END_POOL", "Time slice end pool"},
      { "JOB_TOTAL_RESPONSE_TIME", "Total response time" },
      { "JOB_TYPE", "Type" },
      { "JOB_USER", "User" },
      { "JOB_USER_LIBL", "User library list"},
      { "JOB_WORK_ID_UNIT", "Unit of work ID"},


           // #TRANNOTE #####################################################
           // #TRANNOTE Text for menus.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix MENU_ and
           // #TRANNOTE then a short identifier to describe the
           // #TRANNOTE message.
           // #TRANNOTE
      { "MENU_ACTUAL_SIZE", "Actual Size" }, // @A1A
      { "MENU_COPY", "Copy" },
      { "MENU_CUT", "Cut" },
      { "MENU_EDIT", "Edit" },
      { "MENU_EXIT", "Exit" },
      { "MENU_FILE", "File" },
      { "MENU_FIRST_PAGE", "First Page" }, // @A1A
      { "MENU_FIT_PAGE", "Fit Page" },     // @A1A
      { "MENU_FIT_WIDTH", "Fit Width" },   // @A1A
      { "MENU_FLASH_PAGE", "Flash Page" }, // @A1A
      { "MENU_GO_TO_PAGE", "Go To Page" }, // @A1A
      { "MENU_HIDE_STATUS_BAR", "Hide Status Bar" }, // @A1A
      { "MENU_HIDE_TOOL_BAR", "Hide Tool Bar" },     // @A1A
      { "MENU_LAST_PAGE", "Last Page" },   // @A1A
      { "MENU_NEXT_PAGE", "Next Page" },   // @A1A
      { "MENU_OPTIONS", "Options" },       // @A1A
      { "MENU_PASTE", "Paste" },
      { "MENU_PREVIOUS_PAGE" , "Previous Page" },    // @A1A
      { "MENU_SHOW_STATUS_BAR", "Show Status Bar" }, // @A1A
      { "MENU_SHOW_TOOL_BAR", "Show Tool Bar" },     // @A1A
      { "MENU_SAVE", "Save" },
      { "MENU_SELECT_ALL", "Select All" },
      { "MENU_VIEW", "View" },  // @A1A
      { "MENU_ZOOM", "Zoom" },  // @A1A



           // #TRANNOTE #####################################################
           // #TRANNOTE Text for Message GUIs.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix MESSAGE_ and
           // #TRANNOTE then a short identifier to describe the
           // #TRANNOTE message.
           // #TRANNOTE
      { "MESSAGE_DATE", "Date" },
      { "MESSAGE_DESCRIPTION", "Message" },
      { "MESSAGE_FILE", "Message file" },
      { "MESSAGE_FROM_JOB", "From job" },
      { "MESSAGE_FROM_JOB_NUMBER", "From job number" },
      { "MESSAGE_FROM_PROGRAM", "From program" },
      { "MESSAGE_FROM_USER", "From user" },
      { "MESSAGE_ID", "ID" },
      { "MESSAGE_LIST_DESCRIPTION", "Message list" },
      { "MESSAGE_QUEUE", "Message queue" },
      { "MESSAGE_QUEUE_CHOICE_ALL", "All" },
      { "MESSAGE_QUEUE_CHOICE_MNR", "Messages needing reply" },
      { "MESSAGE_QUEUE_CHOICE_MNNR", "Messages not needing reply" },
      { "MESSAGE_QUEUE_CHOICE_SCNR", "Senders copy needing reply" },
      { "MESSAGE_QUEUE_DESCRIPTION", "Message queue" },
      { "MESSAGE_QUEUED_DESCRIPTION", "Queued message" },
      { "MESSAGE_REPLY", "Reply" },
      { "MESSAGE_SELECTION", "Selection" },
      { "MESSAGE_SEVERITY", "Severity" },
      { "MESSAGE_TEXT", "Text" },
      { "MESSAGE_TYPE", "Type" },

      { "MESSAGE_TYPE_COMPLETION", "Completion" },
      { "MESSAGE_TYPE_DIAGNOSTIC", "Diagnostic" },
      { "MESSAGE_TYPE_INFORMATIONAL", "Informational" },
      { "MESSAGE_TYPE_INQUIRY", "Inquiry" },
      { "MESSAGE_TYPE_SENDERS_COPY", "Senders copy" },
      { "MESSAGE_TYPE_REQUEST", "Request" },
      { "MESSAGE_TYPE_REQUEST_WITH_PROMPTING", "Request with prompting" },
      { "MESSAGE_TYPE_NOTIFY", "Notify" },
      { "MESSAGE_TYPE_ESCAPE", "Escape" },
      { "MESSAGE_TYPE_REPLY_NOT_VALIDITY_CHECKED", "Reply not validity checked" },
      { "MESSAGE_TYPE_REPLY_VALIDITY_CHECKED", "Reply validity checked" },
      { "MESSAGE_TYPE_REPLY_MESSAGE_DEFAULT_USED", "Reply message default used" },
      { "MESSAGE_TYPE_REPLY_SYSTEM_DEFAULT_USED", "Reply system default used" },
      { "MESSAGE_TYPE_REPLY_FROM_SYSTEM_REPLY_LIST", "Reply from system reply list" },
      { "MESSAGE_TYPE_UNEXPECTED", "Unexpected" },

           // #TRANNOTE #####################################################
           // #TRANNOTE Text for Object Authority GUIs.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix OBJECT_ and
           // #TRANNOTE then a short identifier to describe the
           // #TRANNOTE message.
           // #TRANNOTE


      { "OBJECT_ADD_DIALOG_TITLE", "Add a User" },
      { "OBJECT_ADD_MESSAGE", "User Name: " },
      { "OBJECT_ADD_USER_EXCEPTION_DIALOG_MESSAGE" , "The user already exists." }, //@A8C
      { "OBJECT_ADD_USER_EXCEPTION_DIALOG_TITLE" , "Error" },
      { "OBJECT_AUTHORITY_ADD" , "Add" },
      { "OBJECT_AUTHORITY_ALL", "All" },
      { "OBJECT_AUTHORITY_ALTER" , "Alter" },
      { "OBJECT_AUTHORITY_CHANGE", "Change" },
      { "OBJECT_AUTHORITY_DELETE" , "Delete" },
      { "OBJECT_AUTHORITY_EXCLUDE" , "Exclude" },
      { "OBJECT_AUTHORITY_EXECUTE" , "Execute" },
      { "OBJECT_AUTHORITY_EXISTENCE" , "Existence" },
      { "OBJECT_AUTHORITY_MANAGEMENT" , "Management" },
      { "OBJECT_AUTHORITY_OPERATION" , "Operation" },
      { "OBJECT_AUTHORITY_READ" , "Read" },
      { "OBJECT_AUTHORITY_REFERENCE" , "Reference" },
      { "OBJECT_AUTHORITY_UPDATE" , "Update" },
      { "OBJECT_AUTHORITY_USE", "Use" },
      { "OBJECT_AUTHORITY_USER_DEF", "User Defined" }, //@A8A
      { "OBJECT_AUTHORITY_WRITE" , "Write" },
      { "OBJECT_AUTHORIZATION_LIST", "Authorization List" }, //@A8C
      { "OBJECT_COMMIT_DIALOG_MESSAGE" , "Data commit error." }, //@A8C
      { "OBJECT_COMMIT_DIALOG_TITLE" , "Error" },
      { "OBJECT_FROM_AUTHORIZATION_LIST" , "From Authorization List" },
      { "OBJECT_GROUP", "Primary Group" },
      { "OBJECT_LIST_MANAGEMENT", "List Management" }, //@A8A
      { "OBJECT_NAME", "Object" },
      { "OBJECT_OWNER", "Owner" },
      { "OBJECT_PERMISSION", "&0 Permissions" }, // @D2C
      { "OBJECT_PERMISSION2", "Permissions" }, // @D2A
      { "OBJECT_PERMISSION_DIALOG_MESSAGE" , "Can not display the permission of the object." }, //@A8C
      { "OBJECT_PERMISSION_DIALOG_TITLE" , "Error" },
      { "OBJECT_REMOVE_DIALOG_TITLE", "Remove a User" },
      { "OBJECT_REMOVE_MESSAGE", "Are you sure you want to remove the user?" },
      { "OBJECT_TYPE", "Type" },
      { "OBJECT_TYPE_NO_DEFINED" , "Not defined." },
      { "OBJECT_USER_NAME", "Name" },




           // #TRANNOTE #####################################################
           // #TRANNOTE This is information about this product.
           // #TRANNOTE #####################################################
      { "PRODUCT_TITLE", "Toolbox for Java" },


           // #TRANNOTE #####################################################
           // #TRANNOTE Short descriptions, display names, and values for
           // #TRANNOTE properties.  Descriptions start with PROP_DESC_ prefix,
           // #TRANNOTE display names start with PROP_NAME, values start with
           // #TRANNOTE PROP_VALUE.
           // #TRANNOTE #####################################################
      { "PROP_DESC_ACTION", "The action to perform." },

           // #TRANNOTE The action to perform.
      { "PROP_NAME_ACTION", "action" },

      { "PROP_DESC_ACTION_CONTEXT", "The context in which actions will be performed." },

           // #TRANNOTE The context in which actions will be performed.
      { "PROP_NAME_ACTION_CONTEXT", "actionContext" },

      { "PROP_DESC_ALLOW_ACTIONS", "Determines if actions are allowed." },

           // #TRANNOTE Determines if actions are allowed.
      { "PROP_NAME_ALLOW_ACTIONS", "allowActions" },

      { "PROP_DESC_CANCEL_BUTTON_TEXT", "The text for the Cancel button." },

           // #TRANNOTE The text for the Cancel button.
      { "PROP_NAME_CANCEL_BUTTON_TEXT", "cancelButtonText" },

      { "PROP_DESC_COLUMN_MODEL", "The column model." },

      { "PROP_DESC_COLUMN_ATTRIBUTE_IDS", "The column attribute IDs." },                      // @D0A

           // #TRANNOTE The resource list for various models and panes.
      { "PROP_NAME_COLUMN_ATTRIBUTE_IDS", "columnAttributeIDs" },                            // @D0A

           // #TRANNOTE The column model.
      { "PROP_NAME_COLUMN_MODEL", "columnModel" },

      { "PROP_DESC_COMMAND", "The command." },

           // #TRANNOTE The command.
      { "PROP_NAME_COMMAND", "command" },

      { "PROP_DESC_COMPONENT", "The component that determined the parent frame for dialogs." },

           // #TRANNOTE The component that determined that parent frame for dialogs.
      { "PROP_NAME_COMPONENT", "component" },

      { "PROP_DESC_CONFIRM", "Determines if certain actions are confirmed." },

           // #TRANNOTE Determines if certain actions are confirmed.
      { "PROP_NAME_CONFIRM", "confirm" },

      { "PROP_DESC_CONNECTION", "The SQL connection." },

           // #TRANNOTE The SQL connection.
      { "PROP_NAME_CONNECTION", "connection" },

      { "PROP_DESC_DIRECTORY", "The path name of the directory." },

           // #TRANNOTE The path name of the directory.
      { "PROP_NAME_DIRECTORY", "directory" },

      { "PROP_DESC_ENABLED", "Determines if the action is enabled." },

           // #TRANNOTE Whether the action is enabled.
      { "PROP_NAME_ENABLED", "enabled" },

      { "PROP_DESC_FILE_NAME", "The name of the file." },

           // #TRANNOTE The name of the file.
      { "PROP_NAME_FILE_NAME", "fileName" },

      { "PROP_DESC_FILE_ENABLE", "Determines if the control is enabled." },

           // #TRANNOTE Whether the control is enabled or disabled.
      { "PROP_NAME_FILE_ENABLE", "enabled" },

      { "PROP_DESC_FILTER", "The file filter." },

           // #TRANNOTE The file filter.
      { "PROP_NAME_FILTER", "filter" },

      { "PROP_DESC_GRID_COLOR", "The color of the grid lines in the table." },

           // #TRANNOTE The color of the grid lines in the table.
      { "PROP_NAME_GRID_COLOR", "gridColor" },

      { "PROP_DESC_GROUP_INFO", "The group information." },

           // #TRANNOTE The group information.
      { "PROP_NAME_GROUP_INFO", "groupInfo" },

      { "PROP_DESC_ICON", "The icon displayed on this control." },

           // #TRANNOTE The icon displayed on this control.
      { "PROP_NAME_ICON", "icon" },

      { "PROP_DESC_INCLUDE", "Indicates if files, directories, or both are included." },

           // #TRANNOTE Indicates if files, directories, or both are included.
      { "PROP_NAME_INCLUDE", "include" },
      { "PROP_VALUE_INCLUDE_DIRECTORIES", "Include directories" },
      { "PROP_VALUE_INCLUDE_FILES", "Include files" },
      { "PROP_VALUE_INCLUDE_BOTH", "Include both" },

      { "PROP_DESC_JOB", "The job." },

           // #TRANNOTE The job.
      { "PROP_NAME_JOB", "job" },

      { "PROP_DESC_KEY", "The values for the key fields." },

           // #TRANNOTE The values for the key fields.
      { "PROP_NAME_KEY", "key" },

      { "PROP_DESC_KEYED", "Indicates if keyed or sequential access should be used." },

           // #TRANNOTE Indicates if keyed or sequential access should be used for a file.
      { "PROP_NAME_KEYED", "keyed" },

      { "PROP_DESC_MESSAGE_LIST", "The messages resulting from a command or program call." },

           // #TRANNOTE The messages resulting from a command or program call.
      { "PROP_NAME_MESSAGE_LIST", "messageList" },

      { "PROP_DESC_MESSAGE_TEXT", "The last message resulting from a command or program call." },

           // #TRANNOTE The last message resulting from a command or program call.
      { "PROP_NAME_MESSAGE_TEXT", "messageText" },

      { "PROP_DESC_MODEL", "The data model that is presented." },

           // #TRANNOTE The data model that is presented.
      { "PROP_NAME_MODEL", "model" },

      { "PROP_DESC_NAME", "The job name." },

           // #TRANNOTE The job name.
      { "PROP_NAME_NAME", "name" },

      { "PROP_DESC_NUMBER", "The job number." },

           // #TRANNOTE The job number.
      { "PROP_NAME_NUMBER", "number" },

      { "PROP_DESC_OBJECT", "The object." },

           // #TRANNOTE The object.
      { "PROP_NAME_OBJECT", "object" },

      { "PROP_DESC_OK_BUTTON_TEXT", "The text for the OK button." },

           // #TRANNOTE The text for the OK button.
      { "PROP_NAME_OK_BUTTON_TEXT", "okButtonText" },

      { "PROP_DESC_PARAMETER_LIST", "The parameter list." },

           // #TRANNOTE The parameter list.
      { "PROP_NAME_PARAMETER_LIST", "parameterList" },

      { "PROP_DESC_PASSWORD", "The password." },

           // #TRANNOTE The password for this database connection.
      { "PROP_NAME_PASSWORD", "password" },

      { "PROP_DESC_PATH", "The path name." },

           // #TRANNOTE The path name.
      { "PROP_NAME_PATH", "path" },

      { "PROP_DESC_PATTERN", "The pattern that all file and directory names must match." },

           // #TRANNOTE The pattern that all file and directory names must match.
      { "PROP_NAME_PATTERN", "pattern" },

      { "PROP_DESC_PROGRAM", "The program." },

           // #TRANNOTE The program.
      { "PROP_NAME_PROGRAM", "program" },

      { "PROP_DESC_PROPERTIES", "The properties." },

           // #TRANNOTE The properties for this database connection.
      { "PROP_NAME_PROPERTIES", "properties" },

      { "PROP_DESC_QUERY", "The SQL query." },

           // #TRANNOTE The SQL query.
      { "PROP_NAME_QUERY", "query" },

      { "PROP_DESC_RESOURCE_LIST", "The resource list." },                                  // @D0A

           // #TRANNOTE The resource list for various models and panes.
      { "PROP_NAME_RESOURCE_LIST", "resourceList" },                                        // @D0A

      { "PROP_DESC_RESOURCE_PROPERTIES", "The resource properties." },                      // @D0A

           // #TRANNOTE The resource properties for various models and panes.
      { "PROP_NAME_RESOURCE_PROPERTIES", "resourceProperties" },                            // @D0A

      { "PROP_DESC_ROOT", "The root object." },

           // #TRANNOTE The root object for various models and panes.
      { "PROP_NAME_ROOT", "root" },

      { "PROP_DESC_SEARCH_TYPE", "The search type used for keyed access." },

           // #TRANNOTE The search type used for keyed access.
      { "PROP_NAME_SEARCH_TYPE", "searchType" },

      { "PROP_DESC_SELECTION_MODEL", "The selection model." },

           // #TRANNOTE The selection model.
      { "PROP_NAME_SELECTION_MODEL", "selectionModel" },

      { "PROP_DESC_SELECTION", "The type of messages to include in the list." },

           // #TRANNOTE The type of messages to include in the list.
      { "PROP_NAME_SELECTION", "selection" },

      { "PROP_DESC_SEVERITY", "The severity of messages to include in the list." },

           // #TRANNOTE The severity of messages to include in the list.
      { "PROP_NAME_SEVERITY", "severity" },

      { "PROP_DESC_SHOW_H_LINES", "Display horizontal lines between rows in the table." },

           // #TRANNOTE Display horizontal lines between rows in the table.
      { "PROP_NAME_SHOW_H_LINES", "showHorizontalLines" },

      { "PROP_DESC_SHOW_V_LINES", "Display vertical lines between columns in the table." },

           // #TRANNOTE Display vertical lines between columns in the table.
      { "PROP_NAME_SHOW_V_LINES", "showVerticalLines" },

      { "PROP_DESC_SQL", "The SQL statement which will be run." },

           // #TRANNOTE The SQL statement which will be run.
      { "PROP_NAME_SQL", "SQLStatement" },

      { "PROP_DESC_STATE", "The state of the dialog." },

           // #TRANNOTE The state of the dialog.
      { "PROP_NAME_STATE", "state" },
      { "PROP_VALUE_STATE_ACTIVE", "Active" },
      { "PROP_VALUE_STATE_OK", "OK" },
      { "PROP_VALUE_STATE_CANCEL", "Cancel" },

      { "PROP_DESC_SYSTEM", "The server system on which the object resides." },

           // #TRANNOTE The server on which the object resides.
      { "PROP_NAME_SYSTEM", "system" },

      { "PROP_DESC_TABLE_SCHEMAS", "The database schemas for which tables are shown." },

           // #TRANNOTE The schemas for which tables are shown.
      { "PROP_NAME_TABLE_SCHEMAS", "tableSchemas" },

      { "PROP_DESC_TABLES", "The tables for the query." },

           // #TRANNOTE The tables for the query.
      { "PROP_NAME_TABLES", "tables" },

      { "PROP_DESC_TEXT", "The text displayed on this control." },

           // #TRANNOTE The text displayed on this control.
      { "PROP_NAME_TEXT", "text" },

      { "PROP_DESC_URL", "The URL for this database connection." },

           // #TRANNOTE The URL for this database connection.
      { "PROP_NAME_URL", "URL" },

      { "PROP_DESC_USER", "The user." },

           // #TRANNOTE The user.
      { "PROP_NAME_USER", "user" },

      { "PROP_DESC_USER_INFO", "The user information." },

           // #TRANNOTE The user information.
      { "PROP_NAME_USER_INFO", "userInfo" },

      { "PROP_DESC_USER_NAME", "The user name." },

           // #TRANNOTE The user name.
      { "PROP_NAME_USER_NAME", "userName" },

      { "PROP_DESC_USER_SET_SCHEMAS", "Whether the user can set the schemas for which tables are shown." },

           // #TRANNOTE Whether the user can set the schemas for which tables are shown.
      { "PROP_NAME_USER_SET_SCHEMAS", "userSelectTableSchemas" },

      { "PROP_DESC_USER_SET_TABLES", "Whether the user can set the tables used for the query." },

           // #TRANNOTE Whether the user can set the tables used for the query.
      { "PROP_NAME_USER_SET_TABLES", "userSelectTables" },

      { "PROP_DESC_VISIBLE_ROW_COUNT", "The visible row count." },

           // #TRANNOTE The visible row count.
      { "PROP_NAME_VISIBLE_ROW_COUNT", "visibleRowCount" },

           // #TRANNOTE The data source.                                          // @D3A
      { "PROP_NAME_DATASOURCE", "dataSource" },                                    // @D3A

      { "PROP_DESC_DATASOURCE", "The data source being used." },           // @D3A


           // #TRANNOTE #####################################################
           // #TRANNOTE Text for System Pool GUIs.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix SYSTEM_POOL_ and
           // #TRANNOTE then a short identifier to describe the
           // #TRANNOTE message.
           // #TRANNOTE
      //@A1C
      { "SYSTEM_POOL_ACTIVE_TO_INELIGIBLE", "Active to ineligible" },
      { "SYSTEM_POOL_ACTIVE_TO_WAIT", "Active to wait" },
      { "SYSTEM_POOL_ACTIVITY_LEVEL", "Pool activity level" },
      { "SYSTEM_POOL_DATABASE_FAULTS", "Database faults" },
      { "SYSTEM_POOL_DATABASE_PAGES", "Database pages" },
      { "SYSTEM_POOL_IDENTIFIER", "System pool" },
      { "SYSTEM_POOL_MAXIMUM_ACTIVE_THREADS", "Maximum active threads" },
      { "SYSTEM_POOL_MAXIMUM_FAULTS", "Maximum faults" },
      { "SYSTEM_POOL_MAXIMUM_POOL_SIZE", "% Maximum pool size" },
      { "SYSTEM_POOL_MESSAGE_LOGGING", "Message logging" },
      { "SYSTEM_POOL_MINIMUM_FAULTS", "Minimum faults" },
      { "SYSTEM_POOL_MINIMUM_POOL_SIZE", "% Minimum pool size" },
      { "SYSTEM_POOL_NONDATABASE_FAULTS", "Nondatabase faults" },
      { "SYSTEM_POOL_NONDATABASE_PAGES", "Nondatabase pages" },
      { "SYSTEM_POOL_PAGING_OPTION", "Paging option" },
      { "SYSTEM_POOL_PERTHREADS_FAULTS", "Per-thread faults" },
      { "SYSTEM_POOL_PRIORITY", "Priority" },
      { "SYSTEM_POOL_POOL_DESCRIPTION", "Pool description" },
      { "SYSTEM_POOL_POOL_NAME", "Pool name" },
      { "SYSTEM_POOL_POOL_SIZE", "Pool size" },
      { "SYSTEM_POOL_RESERVED_SIZE", "Reserved size" },
      { "SYSTEM_POOL_SUBSYSTEM_NAME", "Subsystem name" },
      { "SYSTEM_POOL_SUBSYSTEM__LIBRARY_NAME", "Subsystem library name" },
      { "SYSTEM_POOL_WAIT_TO_INELIGIBLE", "Wait to ineligible" },


           // #TRANNOTE #####################################################
           // #TRANNOTE Text for System Status GUIs.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix SYSTEM_STATUS_ and
           // #TRANNOTE then a short identifier to describe the
           // #TRANNOTE message.
           // #TRANNOTE
      //@A1C
      { "SYSTEM_STATUS_AUXILIARY_STORAGE", "Auxiliary Storage" },
      { "SYSTEM_STATUS_BATCH_JOBS_RUNNING", "Batch jobs running" }, // @A7C: Removed leading blanks
      { "SYSTEM_STATUS_CPU", "CPU" },
      { "SYSTEM_STATUS_DATE_TIME", "Date and Time" },
      { "SYSTEM_STATUS_JOBS", "Jobs" },
      { "SYSTEM_STATUS_JOBS_IN_SYSTEM", "Jobs in system" }, // @A7C: Removed leading blanks
      { "SYSTEM_STATUS_STORAGE_POOLS", "Storage Pools" },
      { "SYSTEM_STATUS_SYSTEM", "System" },
      { "SYSTEM_STATUS_SYSTEM_ASP", "System ASP" }, // @A7C: Removed leading blanks
      { "SYSTEM_STATUS_SYSTEM_ASP_USED", "% System ASP used" }, // @A7C: Removed leading blanks
      { "SYSTEM_STATUS_SYSTEM_STATUS","System Status"},
      { "SYSTEM_STATUS_TOTAL_AUXILIARY_STORAGE", "Total auxiliary storage" }, // @A7C: Removed leading blanks
      { "SYSTEM_STATUS_USERS", "Users" },
      { "SYSTEM_STATUS_USERS_CURRENTLY_SIGNED_ON", "Users currently signed on" }, // @A7C: Removed leading blanks
      { "SYSTEM_STATUS_UTILIZATION", "% Utilization" }, // @A7C: Removed leading blanks


           // #TRANNOTE #####################################################
           // #TRANNOTE Text for system values group's name.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix SYSTEM_VALUE_GROUP_, a short
           // #TRANNOTE identifier to describe the system value group and
           // #TRANNOTE a postfix _NAME.
           // #TRANNOTE
        { "SYSTEM_VALUE_GROUP_ALL_NAME" , "All" },
        { "SYSTEM_VALUE_GROUP_ALC_NAME" , "Allocation" },
        { "SYSTEM_VALUE_GROUP_DATTIM_NAME" , "Date and Time" },
        { "SYSTEM_VALUE_GROUP_EDT_NAME" , "Editing" },
        { "SYSTEM_VALUE_GROUP_LIBL_NAME" , "Library List" },
        { "SYSTEM_VALUE_GROUP_MSG_NAME" , "Message and Logging" },
        { "SYSTEM_VALUE_GROUP_SEC_NAME" , "Security" },
        { "SYSTEM_VALUE_GROUP_STG_NAME" , "Storage" },
        { "SYSTEM_VALUE_GROUP_SYSCTL_NAME" , "System control" },
        { "SYSTEM_VALUE_GROUP_NET_NAME" , "Network Attributes" },

           // #TRANNOTE #####################################################
           // #TRANNOTE Text for system values group's description.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix SYSTEM_VALUE_GROUP_, a short
           // #TRANNOTE identifier to describe the system value group and
           // #TRANNOTE a postfix _DESC.
           // #TRANNOTE

        { "SYSTEM_VALUE_GROUP_ALL_DESC" , "All system values in the system" },
        { "SYSTEM_VALUE_GROUP_ALC_DESC" , "Allocation system values" },
        { "SYSTEM_VALUE_GROUP_DATTIM_DESC" , "Date and time system values" },
        { "SYSTEM_VALUE_GROUP_EDT_DESC" , "Editing system values" },
        { "SYSTEM_VALUE_GROUP_LIBL_DESC" , "Library list system values" },
        { "SYSTEM_VALUE_GROUP_MSG_DESC" , "Message and logging system values" },
        { "SYSTEM_VALUE_GROUP_SEC_DESC" , "Security system values" },
        { "SYSTEM_VALUE_GROUP_STG_DESC" , "Storage system values" },
        { "SYSTEM_VALUE_GROUP_SYSCTL_DESC" , "System control system values" },
        { "SYSTEM_VALUE_GROUP_NET_DESC" , "Network attributes of the system" },


           // #TRANNOTE #####################################################
           // #TRANNOTE Text for System Value GUIs.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix SYSTEM_VALUE_ and
           // #TRANNOTE then a short identifier to describe the
           // #TRANNOTE message.
           // #TRANNOTE

      { "SYSTEM_VALUE_LIST_DESCRIPTION", "System Value List" },
      { "SYSTEM_VALUE_GROUP_DESCRIPTION", "System Value Group" },




           // #TRANNOTE #####################################################
           // #TRANNOTE Text for Tabbed Panes.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix TAB_ and
           // #TRANNOTE then a short identifier to describe the
           // #TRANNOTE message.
           // #TRANNOTE

      { "TAB_ACTIVE", "Active" },
      { "TAB_DATETIME", "Date/Time" },
      { "TAB_DISPLAY_SESSION","Display session"},
      { "TAB_GENERAL", "General" },
      { "TAB_GROUP_INFORMATION","Group information"},
      { "TAB_INTERNATIONAL","International"},
      { "TAB_LANGUAGE", "Language" },
      { "TAB_LIBRARY_LIST" , "Library List" },
      { "TAB_MESSAGE", "Message" },
      { "TAB_OTHER", "Other" },
      { "TAB_OUTPUT"," Output"},
      { "TAB_PRINTER_OUTPUT", "Printer Output" },
      { "TAB_SECURITY","Security"},
      { "TAB_SESSION_STARTUP","Session startup"},







           // #TRANNOTE #####################################################
           // #TRANNOTE Text for User GUIs.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix USER_ and
           // #TRANNOTE then a short identifier to describe the
           // #TRANNOTE message.
           // #TRANNOTE

      { "USER_ACCOUNTING_CODE","Accounting code"},
      { "USER_ACTION_AUDIT_LEVEL","User action audit level"},
      { "USER_ALL_USERS", "All users"},
      { "USER_ALL_USERS_DES", "The profiles of all users in the system"},
      { "USER_ASSISTANCE_LEVEL", "Assistance level" },
      { "USER_ATTENTION_PROGRAM_NAME", "Attention program" },
//@A4D      { "USER_ATTENTION_PROGRAM_LIB",  "          Library" },
      { "USER_CLASS_NAME","User class name"},
      { "USER_CODED_CHARACTER_SET_ID","Coded character set ID"},
      { "USER_COUNTRY_ID","Country ID"},
      { "USER_CURRENT_LIB","Current library"},
      { "USER_CUSTOM","Custom"}, //@A4C: Removed leading blanks
      { "USER_DAYS_UNTIL_PASSWORD_EXPIRE", "Days until password expires" }, //@A4C
      { "USER_DESCRIPTION", "User" },
      { "USER_DESCRIPTION_PROMPT", "Description" },
      { "USER_DISPLAY_SIGNON_INFORMATION","Display sign-on information"},
      { "USER_GROUP_AUTHORITY","Group authority"},
      { "USER_GROUP_AUTHORITY_TYPE","Group authority type"},
      { "USER_GROUP_HAS_MEMBER","Group member indicator"}, // @A4C
      { "USER_GROUP_ID_NUMBER","Group ID"},
      { "USER_GROUPS_MEMBERS","Group members"}, // @A4C: Removed leading blanks
      { "USER_GROUP_PROFILE_NAME","Group profile name"},
      { "USER_GROUPS", "Groups"},
      { "USER_GROUPS_DES", "The profiles of all groups in the system"},

      { "USER_HIGHEST_SCHEDULE_PRIORITY","Highest schedule priority"},
      { "USER_HOME_DIRECTORY","Home directory"},
      { "USER_ID_NUMBER","User ID number"},

      { "USER_INITIAL_MENU","Initial menu"},
//@A4D      { "USER_INITIAL_MENU_LIB","Library"}, // @A4C: Removed leading blanks
      { "USER_INITIAL_PROGRAM","Initial program"},
//@A4D      { "USER_INITIAL_PROGRAM_LIB","Library"}, // @A4C: Removed leading blanks
      { "USER_IS_NO_PASSWORD", "No password indicator" }, //@A4C
      { "USER_IS_PASSWORD_SET_EXPIRE", "Set password to expire" }, //@A4C
      { "USER_IS_WITH_DIGITAL_CERTIFICATES", "Digital certificate indicator" }, //@A4C
      { "USER_JOB_DESCRIPTION_NAME","Job description"},
//@A4D      { "USER_JOB_DESCRIPTION_LIB" ,"Library"}, // @A4C: Removed leading blanks
      { "USER_LANGUAGE_ID","Language ID"},

      { "USER_LIMIT_CAPABILITIES", "Limit initial program/menu capabilities" },
      { "USER_LIMIT_DEVICE_SESSIONS","Limit device sessions"},

      { "USER_LIST_DESCRIPTION", "User list" },
      { "USER_LIST_GROUPINFO_PROMPT", "Group info" },
      { "USER_LIST_NAME","List name"},
      { "USER_LIST_USERINFO_PROMPT", "User info" },
      { "USER_LOCALE_JOB_ATTRIBUTES","Locale job attributes"},
      { "USER_LOCALE_PATH_NAME","Locale path name"},
      { "USER_MAXIMUM_ALLOWED_STORAGE","Maximum allowed storage"},
      { "USER_MESSAGE_DELIVERY","Message delivery"},
      { "USER_MESSAGE_QUEUE","Message queue"},
//@A4D      { "USER_MESSAGE_QUEUE_LIB","Library"}, // @A4C: Removed leading blanks
      { "USER_MESSAGE_SEVERITY_LEVEL","Message severity level"},
      { "USER_NAME", "Name" },
      { "USER_OBJECT_AUDITING_VALUE","Object auditing level"},
      { "USER_OUTPUT_QUEUE","Output queue"},
//@A4D      { "USER_OUTPUT_QUEUE_LIB","Library"}, // @A4C: Removed leading blanks
      { "USER_OWNER","Owner"}, //@A4C
      { "USER_PASSWORD_EXPIRE_DATE", "Date password expires" }, //@A4C
      { "USER_PASSWORD_EXPIRATION_INTERVAL", "Password expiration interval" },
      { "USER_PASSWORD_LAST_CHANGED_DATE", "Date password last changed" }, //@A4C
      { "USER_PREVIOUS_SIGNED_ON_DATE", "Previous sign-on" }, //@A4C
      { "USER_PRINT_DEVICE","Printer device"}, // @A6C

      { "USER_PROFILE_NAME","User profile name"},
      { "USER_SIGNED_ON_ATTEMPTS_NOT_VALID", "Sign-on attempts not valid" }, //@A4C
      { "USER_SORT_SEQUENCE_TABLE","Sort sequence table"},
//@A4D      { "USER_SORT_SEQUENCE_TABLE_LIB","Library"}, // @A4C: Removed leading blanks
      { "USER_SPECIAL_AUTHORITY","Special authority"},
      { "USER_SPECIAL_ENVIRONMENT","Special environment"},
      { "USER_STATUS","Status"},
      { "USER_STORAGE_USED","Storage used"},
      { "USER_SUPPLEMENTAL_GROUPS_NUMBER","Number of supplemental groups"}, //@A4C
      { "USER_SUPPLEMENTAL_GROUPS","Supplemental groups"},
      { "USER_SYSTEM_NAME", "System name" },
      { "USER_USER_AND_GROUP","Users and groups"}, // @A6C
      { "USER_USER_NAME","User name"},
      { "USER_USERS_NOT_IN_GROUPS", "Users not in groups"},
      { "USER_USERS_NOT_IN_GROUPS_DES", "The profiles of the users not in any group of the system"},

      // @D0A Start
           // #TRANNOTE #####################################################
           // #TRANNOTE Text for Resource GUI components.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix RESOURCE_ and
           // #TRANNOTE then a short identifier to describe the
           // #TRANNOTE message.
           // #TRANNOTE
      { "RESOURCE_ALL_SORTS", "All Sorts"},
      { "RESOURCE_COLUMN_NAME", "Name"},
      { "RESOURCE_CURRENT_SORTS", "Current Sorts"},
      { "RESOURCE_GENERAL_TAB", "General"},
      { "RESOURCE_SELECTION_TAB", "Selection"},
      { "RESOURCE_SORT_TAB", "Sort"},
      // @D0A End

      // @B2A
           // #TRANNOTE #####################################################
           // #TRANNOTE Text for Remote Java Application GUIs.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix REMOTE_ and
           // #TRANNOTE then a short identifier to describe the
           // #TRANNOTE message.
           // #TRANNOTE
      { "REMOTE_OUTPUT_LABEL","Output:"},
      { "REMOTE_INPUT_LABEL","Command:"},
      { "REMOTE_JAVA_START","Starting program "},
      { "REMOTE_JAVA_END1","Program "},
      { "REMOTE_JAVA_END2"," ended"},
      { "REMOTE_PROMPT","=> "},
      { "REMOTE_JAVA_ERROR","Java command error.  Usage:\n"
                         +"  java [-classpath=<value>]"
                         +"  [-verbose]"
                         +"  [-D<prop1>=<value>"
                         +"  -D<prop2>=<value>"
                         +"  [...]]"
                         +"  <class>"
                         +"  [<parm1>"
                         +"  <parm2> "
                         +"  [...]]\n" },
       { "REMOTE_SET_ERROR","Set command error.\n"
                              +"Usage: set <property=value>"},
       { "REMOTE_PROPERTY_ERROR_HEAD","The option "},
       { "REMOTE_PROPERTY_ERROR_END"," does not exist."},
       { "REMOTE_COMMAND_ERROR","Incorrect command."},
       { "REMOTE_COMMAND_MESSAGE_SEP", " : "},
       { "REMOTE_PORT_VALUE_ERROR","The value must be true or false."},
       { "REMOTE_HELP",   "To run a java application:\n"
                         +"   java [-classpath=<value>]"
                         +"  [-verbose]"
                         +"  [-D<prop1>=<value>"
                         +"  -D<prop2>=<value>"
                         +"  [...]]"
                         +"  <class>"
                         +"  [<parm1>"
                         +"  <parm2> "
                         +"  [...]]\n"
                         +"   Example:\n"
                         +"   java -classpath=/myClasses:/myClasses/lib/xx.zip com.myCompany.appl myparm1 myparm2 \n\n"
                         +"To set an option:\n"
                         +"   set option=<value>\n"
                         +"   where option is one of:\n"
                         +"        Classpath, DefaultPort, FindPort, Interpret, Optimize, Option,\n"
                         +"        SecurityCheckLevel, GarbageCollectionFrequency,\n"
                         +"        GarbageCollectionInitialSize, GarbageCollectionMaximumSize,\n"
                         +"        or GarbageCollectionPriority\n"
                         +"   Example:\n"
                         +"   set Optimize=30\n\n"
                         +"To display the value of current options:\n"
                         +"   d \n\n"
                         +"To display help:\n"
                         +"   help, h or ? \n\n"
                         +"To end this program:\n"
                         +"   quit or q \n"},
        { "REMOTE_D_LINE1","Current option settings:"},
       { "REMOTE_D_LINE2", "    SecurityCheckLevel="},
       { "REMOTE_D_LINE3", "    Classpath="},
       { "REMOTE_D_LINE4", "    GarbageCollectionFrequency="},
       { "REMOTE_D_LINE5", "    GarbageCollectionInitialSize="},
       { "REMOTE_D_LINE6", "    GarbageCollectionMaximumSize="},
       { "REMOTE_D_LINE7", "    GarbageCollectionPriority="},
       { "REMOTE_D_LINE8", "    Interpret="},
       { "REMOTE_D_LINE9", "    Optimize="},
       { "REMOTE_D_LINE10","    Option="},
       { "REMOTE_D_LINE11","    DefaultPort="},
       { "REMOTE_D_LINE12","    FindPort="},

       { "PROP_NAME_JAC", "JavaApplicationCall" },
       { "PROP_DESC_JAC", "The java application call object." } // end @B2A
   };



}

