///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RPrinter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.Trace;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;
import java.beans.PropertyVetoException;



/**
The RPrinter class represents an AS/400 printer.

<a name="attributeIDs <p>The following attribute IDs are supported:
<ul>
<li>{@link #ADVANCED_FUNCTION_PRINTING ADVANCED_FUNCTION_PRINTING}
<li>{@link #ALIGN_FORMS ALIGN_FORMS}
<li>{@link #ALLOW_DIRECT_PRINTING ALLOW_DIRECT_PRINTING}
<li>{@link #AUTOMATICALLY_END_WRITER AUTOMATICALLY_END_WRITER}
<li>{@link #BETWEEN_COPIES_STATUS BETWEEN_COPIES_STATUS}
<li>{@link #BETWEEN_FILES_STATUS BETWEEN_FILES_STATUS}
<li>{@link #CHANGES_TAKE_EFFECT CHANGES_TAKE_EFFECT}
<li>{@link #COPIES_LEFT_TO_PRODUCE COPIES_LEFT_TO_PRODUCE}
<li>{@link #DEVICE_NAME DEVICE_NAME}
<li>{@link #DEVICE_STATUS DEVICE_STATUS}
<li>{@link #DEVICE_TYPE DEVICE_TYPE}
<li>{@link #END_PENDING_STATUS END_PENDING_STATUS}
<li>{@link #FORM_TYPE FORM_TYPE}
<li>{@link #HELD_STATUS HELD_STATUS}
<li>{@link #HOLD_PENDING_STATUS HOLD_PENDING_STATUS}
<li>{@link #JOB_NAME JOB_NAME}
<li>{@link #JOB_NUMBER JOB_NUMBER}
<li>{@link #JOB_QUEUE_STATUS JOB_QUEUE_STATUS}
<li>{@link #MESSAGE_KEY MESSAGE_KEY}
<li>{@link #MESSAGE_OPTION MESSAGE_OPTION}
<li>{@link #MESSAGE_QUEUE MESSAGE_QUEUE}
<li>{@link #NEXT_FILE_SEPARATORS NEXT_FILE_SEPARATORS}
<li>{@link #NEXT_FORM_TYPE NEXT_FORM_TYPE}
<li>{@link #NEXT_MESSAGE_OPTION NEXT_MESSAGE_OPTION}
<li>{@link #NEXT_OUTPUT_QUEUE NEXT_OUTPUT_QUEUE}
<li>{@link #NEXT_SEPARATOR_DRAWER NEXT_SEPARATOR_DRAWER}
<li>{@link #NUMBER_OF_SEPARATORS NUMBER_OF_SEPARATORS}
<li>{@link #OUTPUT_QUEUE OUTPUT_QUEUE}
<li>{@link #OUTPUT_QUEUE_STATUS OUTPUT_QUEUE_STATUS}
<li>{@link #OVERALL_STATUS OVERALL_STATUS}
<li>{@link #PAGE_BEING_WRITTEN PAGE_BEING_WRITTEN}
<li>{@link #SEPARATOR_DRAWER SEPARATOR_DRAWER}
<li>{@link #SPOOLED_FILE_NAME SPOOLED_FILE_NAME}
<li>{@link #SPOOLED_FILE_NUMBER SPOOLED_FILE_NUMBER}
<li>{@link #STARTED_BY_USER STARTED_BY_USER}
<li>{@link #TEXT_DESCRIPTION TEXT_DESCRIPTION}
<li>{@link #TOTAL_COPIES TOTAL_COPIES}
<li>{@link #TOTAL_PAGES TOTAL_PAGES}
<li>{@link #USER_NAME USER_NAME}
<li>{@link #WAITING_FOR_DATA_STATUS WAITING_FOR_DATA_STATUS}
<li>{@link #WAITING_FOR_DEVICE_STATUS WAITING_FOR_DEVICE_STATUS}
<li>{@link #WAITING_FOR_MESSAGE_STATUS WAITING_FOR_MESSAGE_STATUS}
<li>{@link #WRITER_JOB_NAME WRITER_JOB_NAME}
<li>{@link #WRITER_JOB_NUMBER WRITER_JOB_NUMBER}
<li>{@link #WRITER_JOB_USER_NAME WRITER_JOB_USER_NAME}
<li>{@link #WRITER_STARTED WRITER_STARTED}
<li>{@link #WRITER_STATUS WRITER_STATUS}
<li>{@link #WRITING_STATUS WRITING_STATUS}
</ul>
</a>

<p>Use any of these attribute IDs with
{@link com.ibm.as400.resource.ChangeableResource#getAttributeValue getAttributeValue()}
and {@link com.ibm.as400.resource.ChangeableResource#setAttributeValue setAttributeValue()}
to access the attribute values for an RPrinter.

<blockquote><pre>
// Create an RPrinter object to refer to a specific printer.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RPrinter printer = new RPrinter(system, "PRT0506");
<br>
// Get the text description.
String textDescription = (String)printer.getAttributeValue(RPrinter.TEXT_DESCRIPTION);
<br>
// Set the form type for a printer to all.
printer.setAttributeValue(RPrinter.FORM_TYPE, RPrinter.FORM_TYPE_ALL);
<br>
// Commit the attribute change.
printer.commitAttributeChanges();
</pre></blockquote>

@see RPrinterList
**/
public class RPrinter
extends ChangeableResource
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



//-----------------------------------------------------------------------------------------
// Presentation.
//-----------------------------------------------------------------------------------------

    private static PresentationLoader   presentationLoader_ = new PresentationLoader("com.ibm.as400.resource.ResourceMRI");
    private static final String         ICON_BASE_NAME_     = "RPrinter";
    private static final String         PRESENTATION_KEY_   = "PRINTER";



//-----------------------------------------------------------------------------------------
// Attribute values.
//-----------------------------------------------------------------------------------------

/**
Attribute value that indicates that an operation is effective when the
current spooled file has been printed.
**/
    public static final String OPERATION_FILE_END             = "*FILEEND";

/**
Attribute value that indicates that an operation is effective when there
are no files are ready to print on the output queue from which the
writer is selected files.
**/
    public static final String OPERATION_NO_FILES_READY       = "*NORDYF";

/**
Attribute value that indicates that an operation is not effective.
**/
    public static final String OPERATION_NONE                 = "*NO";

/**
Attribute value for status indicating that an operation is pending and will
take effect after the current copy of the spooled file has been printed.
**/
    public static final String PENDING_STATUS_CONTROLLED        = "C";

/**
Attribute value for status indicating that an operation is pending and will
take effect as soon as its output buffers are empty.
**/
    public static final String PENDING_STATUS_IMMEDIATE         = "I";

/**
Attribute value for status indicating that no operation is pending.
**/
    public static final String PENDING_STATUS_NONE              = "N";

/**
Attribute value for status indicating that an operation is pending and will
take effect at the end of the page.
**/
    public static final String PENDING_STATUS_PAGE_END          = "P";

/**
Attribute value that indicates that separator page information is set in the device description.
**/
    public static final Integer SEPARATOR_PAGE_DEVICE       = new Integer(-2);

/**
Attribute value that indicates that separator page information is set in the file.
**/
    public static final Integer SEPARATOR_PAGE_FILE         = new Integer(-1);

/**
Attribute value that indicates that there is no separator page.
**/
    public static final Integer SEPARATOR_PAGE_NONE         = new Integer(-10);

/**
Attribute value for status indicating held.
**/
    public static final String STATUS_HELD                      = "H";

/**
Attribute value for status indicating released.
**/
    public static final String STATUS_RELEASED                  = "R";



//-----------------------------------------------------------------------------------------
// Attribute IDs.
//
// * If you add an attribute here, make sure and add it to the class javadoc.
//-----------------------------------------------------------------------------------------

    // Private data.
            static ResourceMetaDataTable        attributes_             = new ResourceMetaDataTable(presentationLoader_, PRESENTATION_KEY_);
    private static ProgramMap                   getterMap_              = new ProgramMap();
    private static CommandMap                   setterMap_              = new CommandMap();

    private static final BooleanValueMap        booleanValueMap01_      = new BooleanValueMap("0", "1");
    private static final BooleanValueMap        booleanValueMapNoYes_   = new BooleanValueMap(new String[] {"*NO", ""}, new String[] {"*YES"});
    private static final BooleanValueMap        booleanValueMapNY_      = new BooleanValueMap(new String[] {"N", ""}, new String[] { "Y" });
    private static final QuoteValueMap          quoteValueMapBlank_     = new QuoteValueMap("*BLANK");
    private static final QuoteValueMap          quoteValueMapEmpty_     = new QuoteValueMap("");
    private static final ValueMap               separatorValueMap_      = new AbstractValueMap()
            {
                public Object ltop(Object logicalValue) {
                    if (logicalValue.equals(SEPARATOR_PAGE_FILE))
                        return "*FILE";
                    else if (logicalValue.equals(SEPARATOR_PAGE_DEVICE))
                        return "*DEVD";
                    else
                        return logicalValue;
                }
            };



    private static final String                 CHGDEVPRT_              = "CHGDEVPRT";
    private static final String                 CHGWTR_                 = "CHGWTR";
    private static final String                 RPTA0100_               = "qgyrprta_rpta0100";
    private static final String                 DEVD_PARAMETER_         = "DEVD";
    private static final String                 WTR_PARAMETER_          = "WTR";



/**
Attribute ID for advanced function printing.  This identifies a read-only Boolean
attribute, which indicates whether the printer supports Advanced Function Printing.
**/
    public static final String ADVANCED_FUNCTION_PRINTING                          = "ADVANCED_FUNCTION_PRINTING";

    static {
        attributes_.add(ADVANCED_FUNCTION_PRINTING, Boolean.class, true);
        getterMap_.add(ADVANCED_FUNCTION_PRINTING, RPTA0100_, "receiverVariable.advancedFunctionPrinting", booleanValueMapNoYes_);
    }



/**
Attribute ID for align forms.  This identifies a read-only String
attribute, which represents the time at which the forms alignment
message will be sent.  Possible values are:
<ul>
<li>{@link #ALIGN_FORMS_WRITER ALIGN_FORMS_WRITER}
    - The writer determines when the message is sent.
<li>{@link #ALIGN_FORMS_FILE ALIGN_FORMS_FILE}
    - Control of the page alignment is specified by each file.
<li>"" - The writer is ended.
</ul>
**/
    public static final String ALIGN_FORMS                   = "ALIGN_FORMS";

    /**
    Attribute value indicating that the writer determines when the forms
    alignment message is sent.

    @see #ALIGN_FORMS
    **/
    public static final String ALIGN_FORMS_WRITER                   = "*WTR";

    /**
    Attribute value indicating that control of the page alignment is specified
    by each file.

    @see #ALIGN_FORMS
    **/
    public static final String ALIGN_FORMS_FILE            = "*FILE";

    static {
        attributes_.add(ALIGN_FORMS, String.class, true,
                        new Object[] {ALIGN_FORMS_WRITER, ALIGN_FORMS_FILE, "" } ,
                        null, true);
        getterMap_.add(ALIGN_FORMS, RPTA0100_, "receiverVariable.alignForms");
    }




/**
Attribute ID for allow direct printing.  This identifies a read-only Boolean
attribute, which indicates whether the printer writer allows the printer to
be allocated to a job that prints directly to a printer.
**/
    public static final String ALLOW_DIRECT_PRINTING                          = "ALLOW_DIRECT_PRINTING";

    static {
        attributes_.add(ALLOW_DIRECT_PRINTING, Boolean.class, true);
        getterMap_.add(ALLOW_DIRECT_PRINTING, RPTA0100_, "receiverVariable.allowDirectPrinting", booleanValueMapNoYes_);
    }



/**
Attribute ID for automatically end writer.  This identifies a read-only String
attribute, which represents when to end the writer if it is to end automatically.
Possible values are:
<ul>
<li>{@link #OPERATION_NO_FILES_READY OPERATION_NO_FILES_READY}
    - When no files are ready to print on the output queue from which
    the writer is selecting files to be printed.
<li>{@link #OPERATION_FILE_END OPERATION_FILE_END}
    - When the current spooled file has been printed.
<li>{@link #OPERATION_NONE OPERATION_NONE}
    - The writer will not end, but it will wait for more spooled files.
<li>"" - The writer is ended.
</ul>
**/
    public static final String AUTOMATICALLY_END_WRITER                   = "AUTOMATICALLY_END_WRITER";

    static {
        attributes_.add(AUTOMATICALLY_END_WRITER, String.class, true,
                        new Object[] {OPERATION_NO_FILES_READY,
                                      OPERATION_FILE_END,
                                      OPERATION_NONE, ""} ,
                        null, true);
        getterMap_.add(AUTOMATICALLY_END_WRITER, RPTA0100_, "receiverVariable.automaticallyEndWriter");
    }



/**
Attribute ID for between copies status.  This identifies a read-only Boolean
attribute, which indicates whether the writer is between copies of a multiple
copy spooled file.
**/
    public static final String BETWEEN_COPIES_STATUS                          = "BETWEEN_COPIES_STATUS";

    static {
        attributes_.add(BETWEEN_COPIES_STATUS, Boolean.class, true);
        getterMap_.add(BETWEEN_COPIES_STATUS, RPTA0100_, "receiverVariable.betweenCopiesStatus", booleanValueMapNY_);
    }



/**
Attribute ID for between files status.  This identifies a read-only Boolean
attribute, which indicates whether the writer is between spooled files.
**/
    public static final String BETWEEN_FILES_STATUS                          = "BETWEEN_FILES_STATUS";

    static {
        attributes_.add(BETWEEN_FILES_STATUS, Boolean.class, true);
        getterMap_.add(BETWEEN_FILES_STATUS, RPTA0100_, "receiverVariable.betweenFilesStatus", booleanValueMapNY_);
    }



/**
Attribute ID for changes take effect.  This identifies a String
attribute, which represents the time at which the pending changes to the
writer take effect.
Possible values are:
<ul>
<li>{@link #OPERATION_NO_FILES_READY OPERATION_NO_FILES_READY}
    - When all the current eligible files are printed.
<li>{@link #OPERATION_FILE_END OPERATION_FILE_END}
    - When the current spooled file is done printing.
<li>"" - No pending changes to the writer.
</ul>
**/
    public static final String CHANGES_TAKE_EFFECT                   = "CHANGES_TAKE_EFFECT";

    static {
        attributes_.add(CHANGES_TAKE_EFFECT, String.class, false,
                        new Object[] {OPERATION_NO_FILES_READY,
                                      OPERATION_FILE_END,
                                      ""} ,
                        null, true);
        getterMap_.add(CHANGES_TAKE_EFFECT, RPTA0100_, "receiverVariable.changesTakeEffect");
        setterMap_.add(CHANGES_TAKE_EFFECT, CHGWTR_, "OPTION");
    }



/**
Attribute ID for copies left to produce.  This identifies a read-only Integer
attribute, which represents the number of copies left to be printed, or 0
when no file is printing.
**/
    public static final String COPIES_LEFT_TO_PRODUCE                          = "COPIES_LEFT_TO_PRODUCE";

    static {
        attributes_.add(COPIES_LEFT_TO_PRODUCE, Integer.class, true);
        getterMap_.add(COPIES_LEFT_TO_PRODUCE, RPTA0100_, "receiverVariable.copiesLeftToProduce");
    }



/**
Attribute ID for device name.  This identifies a read-only String
attribute, which represents the name of the printer device.
**/
    public static final String DEVICE_NAME                          = "DEVICE_NAME";

    static {
        attributes_.add(DEVICE_NAME, String.class, true);
        getterMap_.add(DEVICE_NAME, RPTA0100_, "receiverVariable.deviceName");
    }



/**
Attribute ID for device status.  This identifies a read-only Integer
attribute, which represents the status of the printer device.
**/
    public static final String DEVICE_STATUS                          = "DEVICE_STATUS";

    static {
        attributes_.add(DEVICE_STATUS, Integer.class, true);
        getterMap_.add(DEVICE_STATUS, RPTA0100_, "receiverVariable.deviceStatus");
    }



/**
Attribute ID for device type.  This identifies a read-only String
attribute, which represents the type of the printer.
</ul>
**/
    public static final String DEVICE_TYPE                   = "DEVICE_TYPE";
    static {
        attributes_.add(DEVICE_TYPE, String.class, true);
        getterMap_.add(DEVICE_TYPE, RPTA0100_, "receiverVariable.printerDeviceType");
    }



/**
Attribute ID for end pending status.  This identifies a read-only String
attribute, which represents whether an End Writer (ENDWTR) command has
been issued for this writer.
Possible values are:
<ul>
<li>{@link #PENDING_STATUS_NONE PENDING_STATUS_NONE}
    - No End Writer (ENDWTR) command has been issued.
<li>{@link #PENDING_STATUS_IMMEDIATE PENDING_STATUS_IMMEDIATE}
    - The writer ends as soon as its output buffers are empty.
<li>{@link #PENDING_STATUS_CONTROLLED PENDING_STATUS_CONTROLLED}
    - The writer ends after the current copy of the spooled file has been printed.
<li>{@link #PENDING_STATUS_PAGE_END PENDING_STATUS_PAGE_END}
    - The writer ends at the end of the page.
<li>"" - The writer is ended.
</ul>
**/
    public static final String END_PENDING_STATUS                   = "END_PENDING_STATUS";

    static {
        attributes_.add(END_PENDING_STATUS, String.class, true,
                        new Object[] {PENDING_STATUS_NONE,
                                      PENDING_STATUS_IMMEDIATE,
                                      PENDING_STATUS_CONTROLLED,
                                      PENDING_STATUS_PAGE_END, ""} ,
                        null, true);
        getterMap_.add(END_PENDING_STATUS, RPTA0100_, "receiverVariable.endPendingStatus");
    }



/**
Attribute ID for form type.  This identifies a String
attribute, which represents the type of form being used to print
the spooled file.
Possible values are:
<ul>
<li>{@link #FORM_TYPE_ALL FORM_TYPE_ALL}
    - The writer is started with the option to print all spooled files
    of any form type.
<li>{@link #FORM_TYPE_FORMS FORM_TYPE_FORMS}
    - The writer is started with the option to print all spooled files
    with the same form type before using a different form type.
<li>{@link #FORM_TYPE_STANDARD FORM_TYPE_STANDARD}
    - The writer is started with the option to print all spooled files
    with a form type of *STD.
<li>A form type name - The writer is started with the option
    to print all spooled files with the specified form type.
</ul>
**/
    public static final String FORM_TYPE                   = "FORM_TYPE";

    /**
    Attribute value indicating that the writer is started with the
    option to print all spooled files of any form type.

    @see #FORM_TYPE
    **/
    public static final String FORM_TYPE_ALL                   = "*ALL";

    /**
    Attribute value indicating that the writer is started with the
    option to print all spooled files with the same form type
    before using a different form type.

    @see #FORM_TYPE
    **/
    public static final String FORM_TYPE_FORMS                 = "*FORMS";

    /**
    Attribute value indicating that the writer is started
    with the option to print all spooled files with a form type of *STD.

    @see #FORM_TYPE
    **/
    public static final String FORM_TYPE_STANDARD                = "*STD";

    static {
        attributes_.add(FORM_TYPE, String.class, false,
                        new Object[] {FORM_TYPE_ALL,
                                      FORM_TYPE_FORMS,
                                      FORM_TYPE_STANDARD} ,
                        null, false);
        getterMap_.add(FORM_TYPE, RPTA0100_, "receiverVariable.formType");
        setterMap_.add(FORM_TYPE, CHGWTR_, "FORMTYPE", quoteValueMapEmpty_);
    }



/**
Attribute ID for held status.  This identifies a read-only Boolean
attribute, which indicates whether the writer is held.
**/
    public static final String HELD_STATUS                          = "HELD_STATUS";

    static {
        attributes_.add(HELD_STATUS, Boolean.class, true);
        getterMap_.add(HELD_STATUS, RPTA0100_, "receiverVariable.heldStatus", booleanValueMapNY_);
    }



/**
Attribute ID for hold pending status.  This identifies a read-only String
attribute, which represents whether an Hold Writer (HLDWTR) command has
been issued for this writer.
Possible values are:
<ul>
<li>{@link #PENDING_STATUS_NONE PENDING_STATUS_NONE}
    - No Hold Writer (HLDWTR) command has been issued.
<li>{@link #PENDING_STATUS_IMMEDIATE PENDING_STATUS_IMMEDIATE}
    - The writer is held as soon as its output buffers are empty.
<li>{@link #PENDING_STATUS_CONTROLLED PENDING_STATUS_CONTROLLED}
    - The writer is held after the current copy of the spooled file has been printed.
<li>{@link #PENDING_STATUS_PAGE_END PENDING_STATUS_PAGE_END}
    - The writer is held at the end of the page.
<li>"" - The writer is ended.
</ul>
**/
    public static final String HOLD_PENDING_STATUS                   = "HOLD_PENDING_STATUS";

    static {
        attributes_.add(HOLD_PENDING_STATUS, String.class, true,
                        new Object[] {PENDING_STATUS_NONE,
                                      PENDING_STATUS_IMMEDIATE,
                                      PENDING_STATUS_CONTROLLED,
                                      PENDING_STATUS_PAGE_END, ""} ,
                        null, true);
        getterMap_.add(HOLD_PENDING_STATUS, RPTA0100_, "receiverVariable.holdPendingStatus");
    }



/**
Attribute ID for job name.  This identifies a read-only String
attribute, which represents the name of the job that created the spooled
file currently being processed by the writer, or "" when no spooled
file is printing.
**/
    public static final String JOB_NAME                          = "JOB_NAME";

    static {
        attributes_.add(JOB_NAME, String.class, true);
        getterMap_.add(JOB_NAME, RPTA0100_, "receiverVariable.jobName");
    }



/**
Attribute ID for job number.  This identifies a read-only String
attribute, which represents the number of the job that created the spooled
file currently being processed by the writer, or "" when no spooled
file is printing.
**/
    public static final String JOB_NUMBER                          = "JOB_NUMBER";

    static {
        attributes_.add(JOB_NUMBER, String.class, true);
        getterMap_.add(JOB_NUMBER, RPTA0100_, "receiverVariable.jobNumber");
    }


/**
Attribute ID for job queue status.  This identifies a read-only Boolean
attribute, which indicates whether the writer is on a job queue.
**/
    public static final String JOB_QUEUE_STATUS                          = "JOB_QUEUE_STATUS";

    static {
        attributes_.add(JOB_QUEUE_STATUS, Boolean.class, true);
        getterMap_.add(JOB_QUEUE_STATUS, RPTA0100_, "receiverVariable.onJobQueueStatus", booleanValueMapNY_);
    }



/**
Attribute ID for message key.  This identifies a read-only byte array
attribute, which represents the message key for the message that
the writer is waiting for a reply, or all 0x00's if the writer is not
waiting for a reply to an inquiry message.
**/
    public static final String MESSAGE_KEY                          = "MESSAGE_KEY";

    static {
        attributes_.add(MESSAGE_KEY, byte[].class, true);
        getterMap_.add(MESSAGE_KEY, RPTA0100_, "receiverVariable.messageKey");
    }



/**
Attribute ID for message option.  This identifies a read-only String
attribute, which represents an option for sending a message to the
message queue when this form is finished.
Possible values are:
<ul>
<li>{@link #MESSAGE_OPTION_MESSAGE MESSAGE_OPTION_MESSAGE}
    - A message is sent to the message queue.
<li>{@link #MESSAGE_OPTION_NO_MESSAGE MESSAGE_OPTION_NO_MESSAGE}
    - No message is sent to the message queue.
<li>{@link #MESSAGE_OPTION_INFORMATIONAL_MESSAGE MESSAGE_OPTION_INFORMATIONAL_MESSAGE}
    - An informational message is sent to the message queue.
<li>{@link #MESSAGE_OPTION_INQUIRY_MESSAGE MESSAGE_OPTION_INQUIRY_MESSAGE}
    - An inquiry message is sent to the message queue.
<li>"" - The writer is ended.
</ul>
**/
    public static final String MESSAGE_OPTION                   = "MESSAGE_OPTION";

    /**
    Attribute value indicating that a message is sent to the message queue when
    this form is finished.

    @see #MESSAGE_OPTION
    **/
    public static final String MESSAGE_OPTION_MESSAGE                   = "*MSG";

    /**
    Attribute value indicating that no message is sent to the message queue when
    this form is finished.

    @see #MESSAGE_OPTION
    **/
    public static final String MESSAGE_OPTION_NO_MESSAGE                   = "*NOMSG";

    /**
    Attribute value indicating that an informational message is sent to the message queue when
    this form is finished.

    @see #MESSAGE_OPTION
    **/
    public static final String MESSAGE_OPTION_INFORMATIONAL_MESSAGE                   = "*INFOMSG";

    /**
    Attribute value indicating that an inquiry message is sent to the message queue when
    this form is finished.

    @see #MESSAGE_OPTION
    **/
    public static final String MESSAGE_OPTION_INQUIRY_MESSAGE                   = "*INQMSG";

    static {
        attributes_.add(MESSAGE_OPTION, String.class, true,
                        new Object[] {MESSAGE_OPTION_MESSAGE,
                                      MESSAGE_OPTION_NO_MESSAGE,
                                      MESSAGE_OPTION_INFORMATIONAL_MESSAGE,
                                      MESSAGE_OPTION_INQUIRY_MESSAGE, ""} ,
                        null, true);
        getterMap_.add(MESSAGE_OPTION, RPTA0100_, "receiverVariable.messageOption");
    }



/**
Attribute ID for message queue.  This identifies a read-only String attribute,
which represents the fully qualified integrated file system path name
of the message queue that this writer uses for operational messages.

@see com.ibm.as400.access.QSYSObjectPathName
**/
    public static final String MESSAGE_QUEUE = "MESSAGE_QUEUE";

    static {
        attributes_.add(MESSAGE_QUEUE, String.class, true);
        getterMap_.add(MESSAGE_QUEUE, RPTA0100_, "receiverVariable.messageQueue",
                       new QualifiedValueMap(QualifiedValueMap.FORMAT_20, "MSGQ"));
    }



/**
Attribute ID for next file separators.  This identifies a read-only Integer
attribute, which represents the next number of separator pages to be printed
when the change to the writer takes place.
Possible values are:
<ul>
<li>{@link #SEPARATOR_PAGE_FILE SEPARATOR_PAGE_FILE}
    - The number of separator pages is specified by each file.
<li>{@link #SEPARATOR_PAGE_NONE SEPARATOR_PAGE_NONE}
    - There are no pending changes to the writer.
<li>The number of separator pages to be printed.
</ul>
**/
    public static final String NEXT_FILE_SEPARATORS                   = "NEXT_FILE_SEPARATORS";

    static {
        attributes_.add(NEXT_FILE_SEPARATORS, Integer.class, true,
                        new Object[] {SEPARATOR_PAGE_FILE, SEPARATOR_PAGE_NONE} ,
                        null, false);
        getterMap_.add(NEXT_FILE_SEPARATORS, RPTA0100_, "receiverVariable.nextFileSeparators");
    }



/**
Attribute ID for next form type.  This identifies a read-only String
attribute, which represents the next type of form to be printed.
Possible values are:
<ul>
<li>{@link #FORM_TYPE_ALL FORM_TYPE_ALL}
    - The writer is changed with the option to print all spooled
    files of any form type.
<li>{@link #FORM_TYPE_FORMS FORM_TYPE_FORMS}
    - The writer is changed with the option to print all spooled files
    with the same form type before using a different form type.
<li>{@link #FORM_TYPE_STANDARD FORM_TYPE_STANDARD}
    - The writer is changed with the option to print all spooled files
    with a form type of *STD.
<li>A form type name - The writer is changed with the option
    to print all spooled files with the specified form type.
<li>"" - No change has been made to this writer.
</ul>
**/
    public static final String NEXT_FORM_TYPE                   = "NEXT_FORM_TYPE";

    static {
        attributes_.add(NEXT_FORM_TYPE, String.class, true,
                        new Object[] {FORM_TYPE_ALL,
                                      FORM_TYPE_FORMS,
                                      FORM_TYPE_STANDARD} ,
                        null, false);
        getterMap_.add(NEXT_FORM_TYPE, RPTA0100_, "receiverVariable.nextFormType");
    }



/**
Attribute ID for next message option.  This identifies a read-only String
attribute, which represents the option for sending a message to the
message queue when the next form type is finished.
Possible values are:
<ul>
<li>{@link #MESSAGE_OPTION_MESSAGE MESSAGE_OPTION_MESSAGE}
    - A message is sent to the message queue.
<li>{@link #MESSAGE_OPTION_NO_MESSAGE MESSAGE_OPTION_NO_MESSAGE}
    - No message is sent to the message queue.
<li>{@link #MESSAGE_OPTION_INFORMATIONAL_MESSAGE MESSAGE_OPTION_INFORMATIONAL_MESSAGE}
    - An informational message is sent to the message queue.
<li>{@link #MESSAGE_OPTION_INQUIRY_MESSAGE MESSAGE_OPTION_INQUIRY_MESSAGE}
    - An inquiry message is sent to the message queue.
<li>"" - No change is pending.
</ul>
**/
    public static final String NEXT_MESSAGE_OPTION                   = "NEXT_MESSAGE_OPTION";

    static {
        attributes_.add(NEXT_MESSAGE_OPTION, String.class, true,
                        new Object[] {MESSAGE_OPTION_MESSAGE,
                                      MESSAGE_OPTION_NO_MESSAGE,
                                      MESSAGE_OPTION_INFORMATIONAL_MESSAGE,
                                      MESSAGE_OPTION_INQUIRY_MESSAGE,
                                      ""} ,
                        null, true);
        getterMap_.add(NEXT_MESSAGE_OPTION, RPTA0100_, "receiverVariable.nextMessageOption");
    }



/**
Attribute ID for next output queue.  This identifies a read-only String attribute,
which represents the fully qualified integrated file system path name
of the next output queue to be processed, or "" if no changes have been made to the writer.

@see com.ibm.as400.access.QSYSObjectPathName
**/
    public static final String NEXT_OUTPUT_QUEUE = "NEXT_OUTPUT_QUEUE";

    static {
        attributes_.add(NEXT_OUTPUT_QUEUE, String.class, true);
        getterMap_.add(NEXT_OUTPUT_QUEUE, RPTA0100_, "receiverVariable.nextOutputQueue",
                       new QualifiedValueMap(QualifiedValueMap.FORMAT_20, "OUTQ"));
    }



/**
Attribute ID for next separator drawer.  This identifies a read-only Integer
attribute, which represents the drawer from which the job and file separator
pages are to be taken if there is a change to the writer.
Possible values are:
<ul>
<li>{@link #SEPARATOR_PAGE_FILE SEPARATOR_PAGE_FILE}
    - The separator pages are taken from the same drawer that the file
    is printed from.
<li>{@link #SEPARATOR_PAGE_DEVICE SEPARATOR_PAGE_DEVICE}
    - The separator pages are taken from the separator drawer specified
    in the printer device description.
<li>{@link #SEPARATOR_PAGE_NONE SEPARATOR_PAGE_NONE}
    - There are no pending changes to the writer.
<li>A drawer number.
</ul>
**/
    public static final String NEXT_SEPARATOR_DRAWER                   = "NEXT_SEPARATOR_DRAWER";

    static {
        attributes_.add(NEXT_SEPARATOR_DRAWER, Integer.class, true,
                        new Object[] {SEPARATOR_PAGE_FILE, SEPARATOR_PAGE_DEVICE, SEPARATOR_PAGE_NONE } ,
                        null, false);
        getterMap_.add(NEXT_SEPARATOR_DRAWER, RPTA0100_, "receiverVariable.nextSeparatorDrawer");
    }




/**
Attribute ID for number of separators.  This identifies a Integer
attribute, which represents the number of separator pages to be printed.
Possible values are:
<ul>
<li>{@link #SEPARATOR_PAGE_FILE SEPARATOR_PAGE_FILE}
    - The number of separator pages is specified by each file.
<li>The number of separator pages.
</ul>
**/
    public static final String NUMBER_OF_SEPARATORS                   = "NUMBER_OF_SEPARATORS";

    static {
        attributes_.add(NUMBER_OF_SEPARATORS, Integer.class, false,
                        new Object[] {SEPARATOR_PAGE_FILE } ,
                        null, false);
        getterMap_.add(NUMBER_OF_SEPARATORS, RPTA0100_, "receiverVariable.numberOfSeparators");
        setterMap_.add(NUMBER_OF_SEPARATORS, CHGWTR_, "FILESEP", separatorValueMap_);
    }



/**
Attribute ID for output queue.  This identifies a String attribute,
which represents the fully qualified integrated file system path name
of the output queue from which the spooled files are being selected for
printing.

@see com.ibm.as400.access.QSYSObjectPathName
**/
    public static final String OUTPUT_QUEUE = "OUTPUT_QUEUE";

    static {
        attributes_.add(OUTPUT_QUEUE, String.class, false);
        getterMap_.add(OUTPUT_QUEUE, RPTA0100_, "receiverVariable.outputQueue",
                       new QualifiedValueMap(QualifiedValueMap.FORMAT_20, "OUTQ"));
        setterMap_.add(OUTPUT_QUEUE, CHGWTR_, "OUTQ",
                       new QualifiedValueMap(QualifiedValueMap.FORMAT_CL, "OUTQ"));
    }



/**
Attribute ID for output queue status.  This identifies a read-only String
attribute, which represents the status of the output queue from which spooled
files are being selected for printing.
Possible values are:
<ul>
<li>{@link #STATUS_HELD STATUS_HELD}
    - The output queue is held.
<li>{@link #STATUS_RELEASED STATUS_RELEASED}
    - The output queue is released.
<li>"" - The output queue is not set.
</ul>
**/
    public static final String OUTPUT_QUEUE_STATUS                   = "OUTPUT_QUEUE_STATUS";

    static {
        attributes_.add(OUTPUT_QUEUE_STATUS, String.class, true,
                        new Object[] {STATUS_HELD, STATUS_RELEASED, "" } ,
                        null, true);
        getterMap_.add(OUTPUT_QUEUE_STATUS, RPTA0100_, "receiverVariable.outputQueueStatus");
    }



/**
Attribute ID for overall status.  This identifies a read-only Integer
attribute, which represents the overall status of the logical printer.
**/
    public static final String OVERALL_STATUS                   = "OVERALL_STATUS";

    static {
        attributes_.add(OVERALL_STATUS, Integer.class, true);
        getterMap_.add(OVERALL_STATUS, RPTA0100_, "receiverVariable.overallStatus");
    }



/**
Attribute ID for page being written.  This identifies a read-only Integer
attribute, which represents the page number in the spooled file that is
currently being processed by the writer, or 0 if no spooled file is
printing.
**/
    public static final String PAGE_BEING_WRITTEN                   = "PAGE_BEING_WRITTEN";

    static {
        attributes_.add(PAGE_BEING_WRITTEN, Integer.class, true);
        getterMap_.add(PAGE_BEING_WRITTEN, RPTA0100_, "receiverVariable.pageBeingWritten");
    }



/**
Attribute ID for published status.  This identifies a read-only Boolean
attribute, which indicates whether the printer is published.  This attribute
is supported only when connecting to servers running OS/400 V5R1 or later.
**/
    public static final String PUBLISHED_STATUS                        = "PUBLISHED_STATUS";

    static {
        ResourceLevel level = new ResourceLevel(ResourceLevel.V5R1M0);
        ResourceMetaData rmd = attributes_.add(PUBLISHED_STATUS, Boolean.class, true);
        rmd.setLevel(level);
        getterMap_.add(PUBLISHED_STATUS, RPTA0100_, "receiverVariable.waitingForDataStatus", booleanValueMapNY_, level);
    }



/**
Attribute ID for separator drawer.  This identifies a Integer
attribute, which represents the drawer from which the job and file separator
pages are to be taken.
Possible values are:
<ul>
<li>{@link #SEPARATOR_PAGE_FILE SEPARATOR_PAGE_FILE}
    - The separator pages are taken from the same drawer that the file
    is printed from.
<li>{@link #SEPARATOR_PAGE_DEVICE SEPARATOR_PAGE_DEVICE}
    - The separator pages are taken from the separator drawer specified
    in the printer device description.
<li>A drawer number.
</ul>
**/
    public static final String SEPARATOR_DRAWER                   = "SEPARATOR_DRAWER";

    static {
        attributes_.add(SEPARATOR_DRAWER, Integer.class, false,
                        new Object[] {SEPARATOR_PAGE_FILE, SEPARATOR_PAGE_DEVICE} ,
                        null, false);
        getterMap_.add(SEPARATOR_DRAWER, RPTA0100_, "receiverVariable.drawerForSeparators");
        setterMap_.add(SEPARATOR_DRAWER, CHGWTR_, "SEPDRAWER", separatorValueMap_);
    }



/**
Attribute ID for spooled file name.  This identifies a read-only String
attribute, which represents the name of the spooled file currently
being processed by the writer, or "" if no spooled file is printing.
**/
    public static final String SPOOLED_FILE_NAME                   = "SPOOLED_FILE_NAME";

    static {
        attributes_.add(SPOOLED_FILE_NAME, String.class, true);
        getterMap_.add(SPOOLED_FILE_NAME, RPTA0100_, "receiverVariable.spooledFileName");
    }



/**
Attribute ID for spooled file number.  This identifies a read-only Integer
attribute, which represents the number of the spooled file currently
being processed by the writer, or 0 if no spooled file is printing.
**/
    public static final String SPOOLED_FILE_NUMBER                   = "SPOOLED_FILE_NUMBER";

    static {
        attributes_.add(SPOOLED_FILE_NUMBER, Integer.class, true);
        getterMap_.add(SPOOLED_FILE_NUMBER, RPTA0100_, "receiverVariable.spooledFileNumber");
    }



/**
Attribute ID for started by user.  This identifies a read-only String
attribute, which represents the name of the user that started the writer.
**/
    public static final String STARTED_BY_USER                   = "STARTED_BY_USER";

    static {
        attributes_.add(STARTED_BY_USER, String.class, true);
        getterMap_.add(STARTED_BY_USER, RPTA0100_, "receiverVariable.startedByUser");
    }



/**
Attribute ID for text description.  This identifies a String
attribute, which represents the text description of the printer device.
**/
    public static final String TEXT_DESCRIPTION                   = "TEXT_DESCRIPTION";

    static {
        attributes_.add(TEXT_DESCRIPTION, String.class, false);
        getterMap_.add(TEXT_DESCRIPTION, RPTA0100_, "receiverVariable.textDescription");
        setterMap_.add(TEXT_DESCRIPTION, CHGDEVPRT_, "TEXT", quoteValueMapBlank_);
    }



/**
Attribute ID for total copies.  This identifies a read-only Integer
attribute, which represents the total number of copies to be printed.
**/
    public static final String TOTAL_COPIES                   = "TOTAL_COPIES";

    static {
        attributes_.add(TOTAL_COPIES, Integer.class, true);
        getterMap_.add(TOTAL_COPIES, RPTA0100_, "receiverVariable.totalCopies");
    }



/**
Attribute ID for total pages.  This identifies a read-only Integer
attribute, which represents the total number of pages in the spooled
file, or 0 if no spooled file is printing.
**/
    public static final String TOTAL_PAGES                   = "TOTAL_PAGES";

    static {
        attributes_.add(TOTAL_PAGES, Integer.class, true);
        getterMap_.add(TOTAL_PAGES, RPTA0100_, "receiverVariable.totalPages");
    }



/**
Attribute ID for user name.  This identifies a read-only String
attribute, which represents the name of the user who created the spooled
file currently being processed by the writer, or "" if no spooled
file is printing.
**/
    public static final String USER_NAME                   = "USER_NAME";

    static {
        attributes_.add(USER_NAME, String.class, true);
        getterMap_.add(USER_NAME, RPTA0100_, "receiverVariable.userName");
    }



/**
Attribute ID for waiting for data status.  This identifies a read-only Boolean
attribute, which indicates whether the writer has written all of the data
currently in the spooled file and is waiting for more data.
**/
    public static final String WAITING_FOR_DATA_STATUS                          = "WAITING_FOR_DATA_STATUS";

    static {
        attributes_.add(WAITING_FOR_DATA_STATUS, Boolean.class, true);
        getterMap_.add(WAITING_FOR_DATA_STATUS, RPTA0100_, "receiverVariable.waitingForDataStatus", booleanValueMapNY_);
    }



/**
Attribute ID for waiting for device status.  This identifies a read-only Boolean
attribute, which indicates whether the writer is waiting to get the device
from a job that is printing directly to the printer.
**/
    public static final String WAITING_FOR_DEVICE_STATUS                          = "WAITING_FOR_DEVICE_STATUS";

    static {
        attributes_.add(WAITING_FOR_DEVICE_STATUS, Boolean.class, true);
        getterMap_.add(WAITING_FOR_DEVICE_STATUS, RPTA0100_, "receiverVariable.waitingForDeviceStatus", booleanValueMapNY_);
    }



/**
Attribute ID for waiting for message status.  This identifies a read-only Boolean
attribute, which indicates whether the writer is waiting for a reply to an
inquiry message.
**/
    public static final String WAITING_FOR_MESSAGE_STATUS                          = "WAITING_FOR_MESSAGE_STATUS";

    static {
        attributes_.add(WAITING_FOR_MESSAGE_STATUS, Boolean.class, true);
        getterMap_.add(WAITING_FOR_MESSAGE_STATUS, RPTA0100_, "receiverVariable.waitingForMessageStatus", booleanValueMapNY_);
    }



/**
Attribute ID for writer job name.  This identifies a read-only String
attribute, which represents the job name of the printer writer.
**/
    public static final String WRITER_JOB_NAME                   = "WRITER_JOB_NAME";

    static {
        attributes_.add(WRITER_JOB_NAME, String.class, true);
        getterMap_.add(WRITER_JOB_NAME, RPTA0100_, "receiverVariable.writerJobName");
    }



/**
Attribute ID for writer job number.  This identifies a read-only String
attribute, which represents the job number of the printer writer.
**/
    public static final String WRITER_JOB_NUMBER                   = "WRITER_JOB_NUMBER";

    static {
        attributes_.add(WRITER_JOB_NUMBER, String.class, true);
        getterMap_.add(WRITER_JOB_NUMBER, RPTA0100_, "receiverVariable.writerJobNumber");
    }



/**
Attribute ID for writer job user name.  This identifies a read-only String
attribute, which represents the the name of the system user.
**/
    public static final String WRITER_JOB_USER_NAME                   = "WRITER_JOB_USER_NAME";

    static {
        attributes_.add(WRITER_JOB_USER_NAME, String.class, true);
        getterMap_.add(WRITER_JOB_USER_NAME, RPTA0100_, "receiverVariable.writerJobUserName");
    }



/**
Attribute ID for waiting for device status.  This identifies a read-only Boolean
attribute, which indicates whether a writer is started for this printer.
**/
    public static final String WRITER_STARTED                          = "WRITER_STARTED";

    static {
        attributes_.add(WRITER_STARTED, Boolean.class, true);
        getterMap_.add(WRITER_STARTED, RPTA0100_, "receiverVariable.writerStarted", booleanValueMap01_);
    }



/**
Attribute ID for writer status.  This identifies a read-only byte[]
attribute, which represents the status of the writer for this printer.
Possible values are:
<ul>
<li>{@link #WRITER_STATUS_STARTED WRITER_STATUS_STARTED}
    - The writer is started.
<li>{@link #WRITER_STATUS_ENDED WRITER_STATUS_ENDED}
    - The writer is ended.
<li>{@link #WRITER_STATUS_JOB_QUEUE WRITER_STATUS_JOB_QUEUE}
    - The writer is on a job queue.
<li>{@link #WRITER_STATUS_HELD WRITER_STATUS_HELD}
    - The writer is held.
<li>{@link #WRITER_STATUS_MESSAGE_WAITING WRITER_STATUS_MESSAGE_WAITING}
    - The writer is waiting on a message.
</ul>
**/
    public static final String WRITER_STATUS                   = "WRITER_STATUS";

    /**
    Attribute value indicating that the writer is started.

    @see #WRITER_STATUS
    **/
    public static final byte[] WRITER_STATUS_STARTED                   = new byte[] {((byte)0x01)};

    /**
    Attribute value indicating that the writer is ended.

    @see #WRITER_STATUS
    **/
    public static final byte[] WRITER_STATUS_ENDED                     = new byte[] {((byte)0x02)};

    /**
    Attribute value indicating that the writer is on a job queue.

    @see #WRITER_STATUS
    **/
    public static final byte[] WRITER_STATUS_JOB_QUEUE                 = new byte[] {((byte)0x03)};

    /**
    Attribute value indicating that the writer is held.

    @see #WRITER_STATUS
    **/
    public static final byte[] WRITER_STATUS_HELD                      = new byte[] {((byte)0x04)};

    /**
    Attribute value indicating that the writer is waiting on a message.

    @see #WRITER_STATUS
    **/
    public static final byte[] WRITER_STATUS_MESSAGE_WAITING           = new byte[] {((byte)0x05)};

    static {
        attributes_.add(WRITER_STATUS, byte[].class, true,
                        new Object[] {WRITER_STATUS_STARTED,
                                      WRITER_STATUS_ENDED,
                                      WRITER_STATUS_JOB_QUEUE,
                                      WRITER_STATUS_HELD,
                                      WRITER_STATUS_MESSAGE_WAITING} ,
                        null, true);
        getterMap_.add(WRITER_STATUS, RPTA0100_, "receiverVariable.writerStatus");
    }



/**
Attribute ID for writing status.  This identifies a read-only String
attribute, which indicates whether the printer writer is in writing
status.
Possible values are:
<ul>
<li>{@link #WRITING_STATUS_YES WRITING_STATUS_YES}
    - The writer is in writing status.
<li>{@link #WRITING_STATUS_NO WRITING_STATUS_NO}
    - The writer is not in writing status.
<li>{@link #WRITING_STATUS_SEPARATORS WRITING_STATUS_SEPARATORS}
    - The writer is writing the file separators.
<li>"" - The writer is ended.
</ul>
**/
    public static final String WRITING_STATUS                   = "WRITING_STATUS";

    /**
    Attribute value indicating that the writer is in writing status.

    @see #WRITING_STATUS
    **/
    public static final String WRITING_STATUS_YES                   = "Y";

    /**
    Attribute value indicating that the writer is not in writing status.

    @see #WRITING_STATUS
    **/
    public static final String WRITING_STATUS_NO                    = "N";

    /**
    Attribute value indicating that the writer is writing the file separators.

    @see #WRITING_STATUS
    **/
    public static final String WRITING_STATUS_SEPARATORS            = "S";

    static {
        attributes_.add(WRITING_STATUS, String.class, true,
                        new Object[] {WRITING_STATUS_YES,
                                      WRITING_STATUS_NO,
                                      WRITING_STATUS_SEPARATORS,
                                      ""} ,
                        null, true);
        getterMap_.add(WRITING_STATUS, RPTA0100_, "receiverVariable.writingStatus");
    }



//-----------------------------------------------------------------------------------------
// PCML document initialization.
//-----------------------------------------------------------------------------------------

    private static final String             DOCUMENT_NAME_      = "com.ibm.as400.resource.RPrinter";
    private static ProgramCallDocument      staticDocument_     = null;

    static {
        // Create a static version of the PCML document, then clone it for each document.
        // This will improve performance, since we will only have to deserialize the PCML
        // object once.
        try {
            staticDocument_ = new ProgramCallDocument();
            staticDocument_.setDocument(DOCUMENT_NAME_);
        }
        catch(PcmlException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error instantiating ProgramCallDocument", e);
        }
    }



//-----------------------------------------------------------------------------------------
// Private data.
//-----------------------------------------------------------------------------------------

    private String                          name_               = null;

    private ProgramAttributeGetter          attributeGetter_    = null;
    private CommandAttributeSetter          attributeSetter_    = null;



//-----------------------------------------------------------------------------------------
// Constructors.
//-----------------------------------------------------------------------------------------

/**
Constructs an RPrinter object.
**/
    public RPrinter()
    {
        super(presentationLoader_.getPresentationWithIcon(PRESENTATION_KEY_, ICON_BASE_NAME_), null, attributes_);
    }



/**
Constructs an RPrinter object.

@param system   The system.
@param name     The printer device name.
**/
    public RPrinter(AS400 system,String name)
    {
        this();

        try {
            setSystem(system);
            setName(name);
        }
        catch(PropertyVetoException e) {
            // Ignore.
        }
    }



// @A2C
/**
Commits the specified attribute changes.

@param attributeIDs     The attribute IDs for the specified attribute changes.
@param values           The specified attribute changes
@param bidiStringTypes  The bidi string types as defined by the CDRA (Character Data 
                        Representataion Architecture). See 
                        {@link com.ibm.as400.access.BidiStringType BidiStringType}
                        for more information and valid values. 
                       
@exception ResourceException                If an error occurs.
**/
    protected void commitAttributeChanges(Object[] attributeIDs, Object[] values, int[] bidiStringTypes)
    throws ResourceException
    {
        super.commitAttributeChanges(attributeIDs, values, bidiStringTypes);

        // Establish the connection if needed.
        if (! isConnectionEstablished())
            establishConnection();

        attributeSetter_.setValues(attributeIDs, values, bidiStringTypes); // @A2C
    }




/**
Computes a resource key.

@param system       The system.
@param name         The user profile name.
@return             The resource key.
**/
    static Object computeResourceKey(AS400 system, String name)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(RPrinter.class);
        buffer.append(':');
        buffer.append(system.getSystemName());
        buffer.append(':');
        buffer.append(system.getUserId());
        buffer.append(':');
        buffer.append(name);
        return buffer.toString();
    }



/**
Establishes the connection to the AS/400.

<p>The method is called by the resource framework automatically
when the connection needs to be established.

@exception ResourceException                If an error occurs.
**/
    protected void establishConnection()
    throws ResourceException
    {
        // Validate if we can establish the connection.
        if (name_ == null)
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        // Update the PCML document.
        String nameUpper = name_.toUpperCase();
        AS400 system = getSystem();
        ProgramCallDocument document = (ProgramCallDocument)staticDocument_.clone();
        try {
            document.setSystem(system);
            document.setValue("qgyrprta_rpta0100.deviceName", nameUpper);
        }
        catch(PcmlException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error setting PCML document values", e);
        }

        // Initialize the attribute getter.
        attributeGetter_ = new ProgramAttributeGetter(system, document, getterMap_);

        // Initialize the attribute setter.
        attributeSetter_ = new CommandAttributeSetter(system, setterMap_);
        attributeSetter_.setParameterValue(CHGWTR_, WTR_PARAMETER_, nameUpper);
        attributeSetter_.setParameterValue(CHGDEVPRT_, DEVD_PARAMETER_, nameUpper);

        // Call the superclass.
        super.establishConnection();
    }


/**
Freezes any property changes.  After this is called, property
changes should not be made.  Properties are not the same thing
as attributes.  Properties are basic pieces of information
which must be set to make the object usable, such as the system
and the name.

<p>The method is called by the resource framework automatically
when the properties need to be frozen.

@exception ResourceException                If an error occurs.
**/
    protected void freezeProperties()
    throws ResourceException
    {
        // Validate if we can establish the connection.
        if (name_ == null)
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        // Update the presentation.
        Presentation presentation = getPresentation();
        presentation.setName(name_);

        // Update the resource key.
        if (getResourceKey() == null)
            setResourceKey(computeResourceKey(getSystem(), name_));

        // Call the superclass.
        super.freezeProperties();
    }



// @A2C
/**
Returns the unchanged value of an attribute.   If the attribute
value has an uncommitted change, this returns the unchanged value.
If the attribute value does not have an uncommitted change, this
returns the same value as <b>getAttributeValue()</b>.

@param attributeID  Identifies the attribute.
@param bidiStringType   The bidi string type as defined by the CDRA (Character Data 
                        Representataion Architecture). See 
                        {@link com.ibm.as400.access.BidiStringType BidiStringType}
                        for more information and valid values. 
@return             The attribute value, or null if the attribute
                    value is not available.

@exception ResourceException                If an error occurs.
**/
    public Object getAttributeUnchangedValue(Object attributeID, int bidiStringType)
    throws ResourceException
    {
        Object value = super.getAttributeUnchangedValue(attributeID, bidiStringType);
        if (value == null) {

            // Establish the connection if needed.
            if (! isConnectionEstablished())
                establishConnection();

            value = attributeGetter_.getValue(attributeID, bidiStringType); 
        }
        return value;
    }




/**
Returns the printer device name.

@return The printer device name.
**/
    public String getName()
    {
        return name_;
    }



// @A2A
/**
Indicates if this resource is enabled for bidirectional character conversion.
This always returns true.

@return Always true.
**/
    protected boolean isBidiEnabled()
    {
        return true;
    }



/**
Refreshes the values for all attributes.  This does not cancel
uncommitted changes.  This method fires an attributeValuesRefreshed()
ResourceEvent.

@exception ResourceException                If an error occurs.
**/
    public void refreshAttributeValues()
    throws ResourceException
    {
        super.refreshAttributeValues();

        if (attributeGetter_ != null)
            attributeGetter_.clearBuffer();
    }



/**
Sets the printer device name.  This does not change the printer on
the AS/400.  Instead, it changes the printer to which
this object references.  This cannot be changed
if the object has established a connection to the AS/400.

@param name    The printer device name.

@exception PropertyVetoException    If the property change is vetoed.
**/
    public void setName(String name)
        throws PropertyVetoException
    {
        if (name == null)
            throw new NullPointerException("name");
        if (arePropertiesFrozen())
            throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

        String oldValue = name_;
        fireVetoableChange("name", oldValue, name);
        name_ = name;
        firePropertyChange("name", oldValue, name);
    }


}
