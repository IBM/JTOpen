///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400Message.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;
import java.util.Calendar;

/**
 The AS400Message class represents a message returned from an AS/400.  The various get methods may return a null value depending upon the class that did the original AS400Message creation.  For example:
 <ul>
 <li>CommandCall and ProgramCall will return non-null values for the following:
 <ul>
 <li>File name
 <li>ID
 <li>Library name
 <li>Path
 <li>Severity
 <li>Substitution data
 <li>Text
 <li>Type
 </ul>
 <li>DataQueue and KeyedDataQueue will return non-null values for the following:
 <ul>
 <li>ID
 <li>Text
 </ul>
 <li>Record-level access classes will return non-null values for the following:
 <ul>
 <li>ID
 <li>Severity
 <li>Text
 </ul>
 <li>SpooledFile will return non-null values for the following:
 <ul>
 <li>Date
 <li>Help
 <li>ID
 <li>Reply
 <li>Severity
 <li>Text
 <li>Type
 </ul>
 </ul>
 **/
public class AS400Message extends Object implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    /**
     Message type for completion messages.
     **/
    public static final int COMPLETION = 1;

    /**
     Message type for diagnostic messages.
     **/
    public static final int DIAGNOSTIC = 2;

    /**
     Message type for informational messages.
     **/
    public static final int INFORMATIONAL = 4;

    /**
     Message type for inquiry messages.
     **/
    public static final int INQUIRY = 5;

    /**
     Message type for sender's copy messages.
     **/
    public static final int SENDERS_COPY = 6;

    /**
     Message type for request messages.
     **/
    public static final int REQUEST = 8;

    /**
     Message type for request with prompting messages.
     **/
    public static final int REQUEST_WITH_PROMPTING = 10;

    /**
     Message type for notify messages.
     **/
    public static final int NOTIFY = 14;

    /**
     Message type for escape messages.
     **/
    public static final int ESCAPE = 15;

    /**
     Message type for reply, not validity checked messages.
     **/
    public static final int REPLY_NOT_VALIDITY_CHECKED = 21;

    /**
     Message type for reply, validity checked messages.
     **/
    public static final int REPLY_VALIDITY_CHECKED = 22;

    /**
     Message type for reply, message default used messages.
     **/
    public static final int REPLY_MESSAGE_DEFAULT_USED = 23;

    /**
     Message type for reply, system default used messages.
     **/
    public static final int REPLY_SYSTEM_DEFAULT_USED = 24;

    /**
     Message type for reply, from system reply list messages.
     **/
    public static final int REPLY_FROM_SYSTEM_REPLY_LIST = 25;

    private Calendar date_;
    private String fileName_;
    private String id_;
    private String libraryName_;
    private String defaultReply_;
    private int severity_ = -1;
    private byte[] substitutionData_;
    private String text_;
    private int type_ = -1;
    private String help_;

    // Constructs an AS400Message object. It is the default message object.
    AS400Message()
    {
    }

    // Constructs an AS400Message object. It uses the specified ID and text.
    // @param  id  The ID for the message.
    // @param  text  The message text.
    AS400Message(String id, String text)
    {
        id_ = id;
        text_ = text;
    }

    // Constructs an AS400Message object. It uses the specified ID, text, file, library, severity, type, substitution text, and help.
    // @param  id  The ID for the message.
    // @param  text  The message text.
    // @param  fileName  The message file name.
    // @param  libraryName  The message library name.
    // @param  severity  The severity level of the message.  It must be between 0 and 99.
    // @param  type  The type of message.
    //               Valid types are:
    //                 COMPLETION
    //                 DIAGNOSTIC
    //                 INFORMATIONAL
    //                 INQUIRY
    //                 SENDERS_COPY
    //                 REQUEST
    //                 REQUEST_WITH_PROMPTING
    //                 NOTIFY
    //                 ESCAPE
    //                 REPLY_NOT_VALIDITY_CHECKED
    //                 REPLY_VALIDITY_CHECKED
    //                 REPLY_MESSAGE_DEFAULT_USED
    //                 REPLY_SYSTEM_DEFAULT_USED
    //                 REPLY_FROM_SYSTEM_REPLY_LIST
    // @param  substitutionData  The message substitution text. This is the unconverted data used to fill in the replacement characters in the message.
    // @param  help  The message help text.
    AS400Message(String id, String text, String fileName, String libraryName, int severity, int type, byte[] substitutionData, String help)
    {
        id_ = id;
        text_ = text;
        fileName_ = fileName;
        libraryName_ = libraryName;
        severity_ = severity;
        type_ = type;
        substitutionData_ = substitutionData;
        help_ = help;
    }

    // Constructs an AS400Message object.  It uses the specified ID, text, file, library, severity, type, substitution text, help, date, time, and reply when supplied.  All of the parameters are optional.
    // @param  id  Optional.  The ID for the message.
    // @param  text  Optional.  The message text.
    // @param  fileName  Optional.  The message file name.
    // @param  libraryName  Optional.  The message library name.
    // @param  severity  Optional.  The severity level of the message.  It must be between 0 and 99.
    // @param  type  Optional.  The type of message.
    //               Valid types are:
    //               COMPLETION
    //               DIAGNOSTIC
    //               INFORMATIONAL
    //               INQUIRY
    //               SENDERS_COPY
    //               REQUEST
    //               REQUEST_WITH_PROMPTING
    //               NOTIFY
    //               ESCAPE
    //               REPLY_NOT_VALIDITY_CHECKED
    //               REPLY_VALIDITY_CHECKED
    //               REPLY_MESSAGE_DEFAULT_USED
    //               REPLY_SYSTEM_DEFAULT_USED
    //               REPLY_FROM_SYSTEM_REPLY_LIST
    // @param  substitutionData  Optional.  The message substitution text.  This is the unconverted data used to fill in the replacement characters in the message.
    // @param  help  Optional.  The message help text.
    // @param  date  Optional.  The message date.
    // @param  time  Optional.  The message time.
    // @param  defaultReply  Optional.  The message reply.
    AS400Message(String id, String text, String fileName, String libraryName, int severity, int type, byte[] substitutionData, String help, String date, String time, String defaultReply)
    {
        id_ = id;
        text_ = text;
        fileName_ = fileName;
        libraryName_ = libraryName;
        severity_ = severity;
        type_ = type;
        substitutionData_ = substitutionData;
        help_ = help;
        setDate(date, time);
        defaultReply_ = defaultReply;
    }

    /**
     Returns the date and time the message was issued.  The valid fields are:
     <ul>
     <li>Calendar.YEAR
     <li>Calendar.MONTH
     <li>Calendar.DAY_OF_MONTH
     <li>Calendar.HOUR
     <li>Calendar.MINUTE
     <li>Calendar.SECOND
     </ul>
     @return  The date and time the message was issued.  If not set, null will be returned.
     **/
    public Calendar getDate()
    {
        return date_;
    }

    /**
     Returns the default reply for the message.
     @return  The default reply for the message.  If not set, null will be returned.
     **/
    public String getDefaultReply()
    {
        return defaultReply_;
    }

    /**
     Returns the message file name.  This is the AS/400 file containing the message.
     @return  The message file name.  If not set, null will be returned.
     **/
    public String getFileName()
    {
        return fileName_;
    }

    /**
     Returns any message help text.  Message formatting characters may appear in the message help and are defined as follows:
     <UL>
     <LI>&N - Force the text to a new line indented to column 2.  If the text is longer than 1 line, the next lines should be indented to column 4 until the end of the text or another format control character is found.
     <LI>&P - Force the text to a new line indented to column 6.  If the text is longer than 1 line, the next lines should start in column 4 until the end of the text or another format control character is found.
     <LI>&B - Force the text to a new line starting in column 4.  If the text is longer than 1 line, the next lines should start in column 6 until the end of the text or another format control character is found.
     </UL>
     @return  The message help text.  If not set, null will be returned.
     **/
    public String getHelp()
    {
        return help_;
    }

    /**
     Returns the ID for the message.
     @return  The ID for the message.  If not set, null will be returned.
     **/
    public String getID()
    {
        return id_;
    }

    /**
     Returns the message library name.  This is the AS/400 library containing the file and message.
     @return  The message library name.  If not set, null will be returned.
     **/
    public String getLibraryName()
    {
        return libraryName_;
    }

    /**
     Returns the full integrated file system path name of the message file containing the message.
     @return  The fully-qualified message file name.  If not set, null will be returned.
     **/
    public String getPath()
    {
        try
        {
            if (fileName_ != null  &&  libraryName_ != null)
            {
                QSYSObjectPathName path = new QSYSObjectPathName(getLibraryName().trim(), getFileName().trim(), "MSGF");
                return path.getPath();
            }
        }
        catch (Throwable e)
        {
        }
        return null;
    }

    /**
     Returns the message severity.  Severity is between 0 and 99.
     @return  The message severity. If not set, negative one (-1) will be returned.
     **/
    public int getSeverity()
    {
        return severity_;
    }

    /**
     Returns the message substitution text.  This is the unconverted data used to fill in the replacement characters in the message.
     @return  The substitution text.  If not set, null will be returned.
     **/
    public byte[] getSubstitutionData()
    {
        return substitutionData_;
    }

    /**
     Returns the message text.  The substitution text has already been inserted.
     @return  The message text.  If not set, null will be returned.
     **/
    public String getText()
    {
        return text_;
    }

    /**
     Returns the message type.  Valid types are:
     <ul>
     <li>COMPLETION
     <li>DIAGNOSTIC
     <li>INFORMATIONAL
     <li>INQUIRY
     <li>SENDERS_COPY
     <li>REQUEST
     <li>REQUEST_WITH_PROMPTING
     <li>NOTIFY
     <li>ESCAPE
     <li>REPLY_NOT_VALIDITY_CHECKED
     <li>REPLY_VALIDITY_CHECKED
     <li>REPLY_MESSAGE_DEFAULT_USED
     <li>REPLY_SYSTEM_DEFAULT_USED
     <li>REPLY_FROM_SYSTEM_REPLY_LIST
     </ul>
     @return  The message type.  If not set, negative one (-1) will be returned.
     **/
    public int getType()
    {
        return type_;
    }

    // Sets the date and time.
    // @param  date  The date.
    void setDate(Calendar date)
    {
        if (date == null)
        {
            throw new NullPointerException("date");
        }
        date_ = date;
    }

    // Sets the date and time.
    // @param  date  The date.
    // @param  time  The time.
    void setDate(String date, String time)
    {
        if (date_ == null)
        {
            date_ = Calendar.getInstance();
        }
        try
        {
            date_.set(Integer.parseInt(date.substring(0,3)) + 1900 /* year */, Integer.parseInt(date.substring(3,5)) - 1 /* month is zero based in Calendar class */, Integer.parseInt(date.substring(5,7)) /* day */, Integer.parseInt(time.substring(0,2)) /* hour */, Integer.parseInt(time.substring(2,4)) /* minute */, Integer.parseInt(time.substring(4,6)) /* second */);
        }
        catch (Exception e)
        {
        }
    }

    /**
     Sets the default reply.
     @param  file  The default reply.
     **/
    void setDefaultReply(String defaultReply)
    {
        if (defaultReply == null)
        {
            throw new NullPointerException("defaultReply");
        }
        defaultReply_ = defaultReply;
    }

    /**
     Sets the message file name.
     @param  fileName  The message file.
     **/
    void setFileName(String fileName)
    {
        if (fileName == null)
        {
            throw new NullPointerException("fileName");
        }
        fileName_ = fileName;
    }

    /**
     Sets the message help text.
     @param  help  The message help text.
     **/
    void setHelp(String help)
    {
        if (help == null)
        {
            throw new NullPointerException("help");
        }
        help_ = help;
    }

    /**
     Sets the ID for the message.  The IDs are AS/400 message IDs.
     @param  id  The ID for the message.
     **/
    void setID(String id)
    {
        if (id == null)
        {
            throw new NullPointerException("id");
        }
        id_ = id;
    }

    /**
     Sets the message library.
     @param  libraryName  The message library
     **/
    void setLibraryName(String libraryName)
    {
        if (libraryName == null)
        {
            throw new NullPointerException("libraryName");
        }
        libraryName_ = libraryName;
    }

    /**
     Sets the full integrated file system path name of the message file containing the message.
     @param  path  The fully-qualified message file name.
     **/
    void setPath(String path)
    {
        if (path == null)
        {
            throw new NullPointerException("path");
        }
        try
        {
            QSYSObjectPathName qpath = new QSYSObjectPathName(path, "MSGF");
            fileName_ = qpath.getObjectName();
            libraryName_  = qpath.getLibraryName();
        }
        catch (Throwable e)
        {
            return;
        }
    }

    /**
     Sets the message severity.  It must be between 0 and 99.
     @param  severity  The severity of the message.
     **/
    void setSeverity(int severity)
    {
        if (severity >= 0 && severity <= 99)
        {
            severity_ = severity;
        }
    }

    /**
     Sets the message substitution text.
     @param substitutionData  The substitution text.
     **/
    void setSubstitutionData(byte[] substitutionData)
    {
        if (substitutionData == null)
        {
            throw new NullPointerException("substitutionData");
        }
        substitutionData_ = substitutionData;
    }

    /**
     Sets the message text.
     @param  text  The message text.
     **/
    void setText(String text)
    {
        if (text == null)
        {
            throw new NullPointerException("text");
        }
        text_ = text;
    }

    /**
     Sets the message type.
     @param  type  The message type.
     **/
    void setType(int type)
    {
        type_ = type;
    }

    /**
     Returns a short description of the object.
     @return  The String ID and text of the AS400 message.
     **/
    public String toString()
    {
        return "AS400Message (ID: " + id_ + " text: " + text_ + "):" + super.toString();
    }
}
