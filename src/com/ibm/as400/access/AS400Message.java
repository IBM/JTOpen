///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400Message.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 The AS400Message class represents a message returned from an AS/400 or iSeries server.  A Java program does not normally create AS400Message objects directly.  Instead, AS400Message objects are created and returned by various other IBM Toolbox for Java components.
<br><i>Usage hint:</i> To fully "prime" an AS400Message object with additional information that otherwise might not be returned from the server, call the load() method.  For example, if getHelp() returns null, try preceding the getHelp() with a call to load().
 @see  com.ibm.as400.access.AS400Exception
 @see  com.ibm.as400.access.CommandCall
 @see  com.ibm.as400.access.ProgramCall
 @see  com.ibm.as400.access.SpooledFile
 **/
public class AS400Message implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;

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
     Message type for notify (exception already handled when API is called) messages.
     **/                                                                // @F4C
    public static final int NOTIFY = 14;

    /**
     Message type for escape (exception already handled when API is called) messages.
     **/                                                                // @F4C
    public static final int ESCAPE = 15;

    /**
     Message type for notify (exception not handled when API is called) messages.
     **/
    public static final int NOTIFY_NOT_HANDLED = 16;                    // @F4A

    /**
     Message type for escape (exception not handled when API is called) messages.
     **/
    public static final int ESCAPE_NOT_HANDLED = 17;                    // @F4A

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

    /**
     Constant for the option indicating up to ten messages sent to the caller should be returned.  For compatibility, this option is the default.  Only messages sent to the caller will be returned, messages sent by the invoked procedure to itself will not be returned.
     **/
    public static final int MESSAGE_OPTION_UP_TO_10 = 0;
    /**
     Constant for the option indicating that no messages should be returned.
     **/
    public static final int MESSAGE_OPTION_NONE = 1;
    /**
     Constant for the option indicating all the messages should be returned.  All messages sent from invocation beginning to invocation end will be returned.  Servers not supporting this new option will revert to the behavior specified for option MESSAGE_OPTION_UP_TO_10.
     **/
    public static final int MESSAGE_OPTION_ALL = 2;

    // Date and time message sent.
    private Calendar date_;
    private String dateSent_; //@G0A
    private String timeSent_; //@G0A

    // Filename of message file message is from.
    private String fileName_;
    // Message ID of message.
    private String id_;
    // Library of message file message is from.
    private String libraryName_;
    // Default reply to message.
    private String defaultReply_;
    // Severity of message.
    private int severity_ = -1;
    // Raw substitution data from message.
    private byte[] substitutionData_;
    // First level text of message.
    private String text_;
    // Type of message.
    private int type_ = -1;
    // Second level text of message.
    private String help_;
    // System message came from.
    private transient AS400 system_;
    // Flag indicating if load has been done.
    private transient boolean messageLoaded_ = false;

    // Constructs an AS400Message object.
    AS400Message()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing AS400Message object.");
    }

    // Constructs an AS400Message object.
    // @param  id  The message ID.
    // @param  text  The message text.
    AS400Message(String id, String text)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing AS400Message object, ID: " + id + " text: " + text);
        id_ = id;
        text_ = text;
    }

    // @F3A - This is used by some native code.
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

    // @F3A - This is used by some native code.
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
     Returns the date and time the message was sent.  The returned Calendar object will have the following fields set:
     <ul>
     <li>Calendar.YEAR
     <li>Calendar.MONTH
     <li>Calendar.DAY_OF_MONTH
     <li>Calendar.HOUR
     <li>Calendar.MINUTE
     <li>Calendar.SECOND
     </ul>
     @return  The date and time the message was sent, or null if not applicable.
     **/
    public Calendar getDate()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting date: " + date_);
        if (date_ == null && (dateSent_ != null || timeSent_ != null)) //@G0C
        {
          setDate(); //@G0C
        }
        return date_;
    }

    /**
     Returns the default reply.
     @return  The default reply, or null if it is not set.
     **/
    public String getDefaultReply()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting default reply: " + defaultReply_);
        return defaultReply_;
    }

    /**
     Returns the message file name.
     @return  The message file name, or null if it is not set.
     **/
    public String getFileName()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting message file name: " + fileName_);
        return fileName_;
    }

    /**
     Returns the message help.
     <p>Message formatting characters may appear in the message help and are defined as follows:
     <UL>
     <LI>&N - Force the text to a new line indented to column 2.  If the text is longer than 1 line, the next lines should be indented to column 4 until the end of the text or another format control character is found.
     <LI>&P - Force the text to a new line indented to column 6.  If the text is longer than 1 line, the next lines should start in column 4 until the end of the text or another format control character is found.
     <LI>&B - Force the text to a new line starting in column 4.  If the text is longer than 1 line, the next lines should start in column 6 until the end of the text or another format control character is found.
     </UL>
     <i>Usage hint:</i> If getHelp() returns null, try "priming" the AS400Message object by first calling load(), then getHelp().
     @return  The message help, or null if it is not set.
     **/
    public String getHelp()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting message help: " + help_);
        return help_;
    }

    /**
     Returns the message ID.
     @return  The message ID, or null if it is not set.
     **/
    public String getID()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting message ID: " + id_);
        return id_;
    }

    /**
     Returns the message file library.
     @return  The message file library, or null if it is not set.
     **/
    public String getLibraryName()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting message file library: " + libraryName_);
        return libraryName_;
    }

    /**
     Returns the full integrated file system path name of the message file.
     @return  The full integrated file system path name of the message file name, or null if it is not set.
     **/
    public String getPath()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting message file path, file name: " + fileName_ + " library: " + libraryName_);
        if (fileName_ == null || libraryName_ == null || fileName_.length() == 0 || libraryName_.length() == 0)
        {
            return null;
        }
        return QSYSObjectPathName.toPath(libraryName_, fileName_, "MSGF");
    }

    /**
     Returns the message severity.
     @return  The message severity.  Valid values are between 0 and 99, or -1 if it is not set.
     **/
    public int getSeverity()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting message severity:", severity_);
        return severity_;
    }

    /**
     Returns the substitution data.  This is unconverted data used to fill in the replacement characters in the message.
     To convert the data to something useful, see the {@link com.ibm.as400.access.CharConverter CharConverter} class
     for String conversions (CHAR fields) and the {@link com.ibm.as400.access.BinaryConverter BinaryConverter} class for
     integer (BIN fields) and other numeric conversions.
     @return  The subsitution data, or null if not set.
     **/
    public byte[] getSubstitutionData()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting message substitution data:", substitutionData_);
        return substitutionData_;
    }

    /**
     Returns the message text with the substitution text inserted.
     @return  The message text, or null if it is not set.
     **/
    public String getText()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting message text: " + text_);
        return text_;
    }

    /**
     Returns the message type.
     @return  The message type, or negative one (-1) if it is not set.  Valid values are:
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
     **/
    public int getType()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting message type:", type_);
        return type_;
    }

    // @D5c -- this method used to do all the work.  That code is now in
    // the load method that takes the formatting type.
    /**
     Loads additional message information from the server.
     If this message does not have an associated message file, this method does nothing.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     **/
    public void load() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
       load(MessageFile.DEFAULT_FORMATTING);
    }

    // @D5a -- new method that is built from the original load() method.
    /**
     Loads additional message information from the server.
     If this message does not have an associated message file, this method does nothing.
     @param  helpTextFormatting Formatting performed on the help text.  Valid
             values for this parameter are defined in the MessageFile
             class.  They are no formatting, return formatting characters,
             and replace (substitute) formatting characters.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     **/
    public void load(int helpTextFormatting) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Loading additional message information.");
        if (messageLoaded_)
        {
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Repeat message load not necessary.");
            return;
        }

        //@G1A
        if (libraryName_ == null || fileName_ == null ||
            libraryName_.trim().length() == 0 || fileName_.trim().length() == 0)
        {
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "No message file associated with this message: "+toString());
          return;
        }

        // Create message file object and get message from it.
        MessageFile file = new MessageFile(system_, QSYSObjectPathName.toPath(libraryName_, fileName_, "MSGF"));

        try                                                 // @D5a
        {                                                   // @D5a
           file.setHelpTextFormatting(helpTextFormatting);  // @D5a
        }                                                   // @D5a
        catch (PropertyVetoException pve)                   // @D5a
        {}                                                  // @D5a



        try
        {
            AS400Message retrievedMessage = file.getMessage(id_, substitutionData_);

            // Set message field that are not already set.
            if (defaultReply_ == null)
            {
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting default reply: " + retrievedMessage.defaultReply_);
                defaultReply_ = retrievedMessage.defaultReply_;
            }
            if (severity_ == -1)
            {
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting message severity:", retrievedMessage.severity_);
                severity_ = retrievedMessage.severity_;
            }
            if (text_ == null)
            {
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting message text: " + retrievedMessage.text_);
                text_ = retrievedMessage.text_;
            }
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting message help: " + retrievedMessage.help_);
            help_ = retrievedMessage.help_;
            messageLoaded_ = true;  // Set flag to not go to AS/400 again.
        }
        catch (PropertyVetoException e)
        {
            Trace.log(Trace.ERROR, "Unexpected PropertyVetoException:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }
    }


    //@G0A
    private void setDate()
    {
      if (dateSent_ == null && timeSent_ == null)
      {
        return;
      }
      if (dateSent_ != null && dateSent_.trim().length() == 0 &&
          timeSent_ != null && timeSent_.trim().length() == 0)
      {
        dateSent_ = null;
        timeSent_ = null;
        return;
      }
      date_ = Calendar.getInstance();
      date_.clear();
      if (dateSent_ != null && dateSent_.trim().length() > 0)
      {
        date_.set(Calendar.YEAR, Integer.parseInt(dateSent_.substring(0,3))+1900);
        date_.set(Calendar.MONTH, Integer.parseInt(dateSent_.substring(3,5))-1);
        date_.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateSent_.substring(5,7)));
      }
      if (timeSent_ != null && timeSent_.trim().length() > 0)
      {
        date_.set(Calendar.HOUR, Integer.parseInt(timeSent_.substring(0,2)));
        date_.set(Calendar.MINUTE, Integer.parseInt(timeSent_.substring(2,4)));
        date_.set(Calendar.SECOND, Integer.parseInt(timeSent_.substring(4,6)));
      }
      dateSent_ = null;
      timeSent_ = null;
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Date: " + date_);
    }


    // Sets the date sent and time sent.
    // @param  dateSent  The date sent.
    // @param  timeSent  The time sent.
    void setDate(String dateSent, String timeSent)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting date, date: " + dateSent + " time: " + timeSent);
      dateSent_ = dateSent; //@G0A
      timeSent_ = timeSent; //@G0A
      //@G0D  date_ = new GregorianCalendar(Integer.parseInt(dateSent.substring(0, 3)) + 1900 /* year */, Integer.parseInt(dateSent.substring(3, 5)) - 1 /* month is zero based in Calendar class */, Integer.parseInt(dateSent.substring(5, 7)) /* day */, Integer.parseInt(timeSent.substring(0, 2)) /* hour */, Integer.parseInt(timeSent.substring(2, 4)) /* minute */, Integer.parseInt(timeSent.substring(4, 6)) /* second */);
      //@G0D  if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Date: " + date_);
    }


    // Sets the default reply.
    // @param  defaultReply  The default reply.
    void setDefaultReply(String defaultReply)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting default reply: " + defaultReply);
        defaultReply_ = defaultReply;
    }

    // Sets the message file name.
    // @param  fileName  The message file name.
    void setFileName(String fileName)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting message file name: " + fileName);
        fileName_ = fileName;
    }

    // Sets the message help.
    // @param  help  The message help.
    void setHelp(String help)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting message help: " + help);
        help_ = help;
    }

    // Sets the message ID.
    // @param  messageID  The message ID.
    void setID(String id)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting message ID: " + id);
        id_ = id;
    }

    // Sets the message file library.
    // @param  messageFileLibrary  The message file library.
    void setLibraryName(String libraryName)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting message file library: " + libraryName);
        libraryName_ = libraryName;
    }

    // Sets the message severity.
    // @param  messageSeverity  The message severity. Valid values are between 0 and 99.
    void setSeverity(int severity)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting message severity:", severity);
        severity_ = severity;
    }

    // Sets the substitution data.
    // @param  substitutionData  The substitution data.
    void setSubstitutionData(byte[] substitutionData)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting message substitution data:", substitutionData);
        substitutionData_ = substitutionData;
    }

    // Sets the AS/400 system.
    // @param  system  The AS/400 system.
    void setSystem(AS400 system)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting message file system: " + system);
        system_ = system;
    }

    // Sets the message text.
    // @param  text  The message text.
    void setText(String text)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting message text: " + text);
        text_ = text;
    }

    // Sets the message type.
    // @param  type  The message type.
    void setType(int type)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting message type:", type);
        type_ = type;
    }

    /**
     Returns the message ID and message text.
     @return  The message ID and message text.
     **/
    public String toString()
    {
        return "AS400Message (ID: " + id_ + " text: " + text_ + "):" + super.toString();
    }


    // returns the original 'toString' value.  In mod 3 toString was
    // changed to return its current value.  This method is added
    // for those parts of the Toolbox that still need the value in
    // in the old format.
    // @D1a -- new method
    String toStringM2()
    {
        StringBuffer buffer = new StringBuffer ();
        String id = getID();                // @F1A
        String text = getText();            // @F1A
        if (id != null) {                   // @F1C
            buffer.append (id);             // @F1C
            buffer.append (" ");
        }
        if (text != null)                   // @F1C
            buffer.append (text);           // @F1C
        return buffer.toString().trim ();
    }
}
