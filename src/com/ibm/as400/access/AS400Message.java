///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400Message.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
//////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;                   // @C1A

/**
 Represents a message returned from an IBM i system.  A Java program does not normally create AS400Message objects directly.  Instead, AS400Message objects are created and returned by various other IBM Toolbox for Java components.
<br><i>Usage hint:</i> To fully "prime" an AS400Message object with additional information that otherwise might not be returned from the system, call the load() method.  For example, if getHelp() returns null, try preceding the getHelp() with a call to load().
 @see  com.ibm.as400.access.AS400Exception
 @see  com.ibm.as400.access.CommandCall
 @see  com.ibm.as400.access.ProgramCall
 @see  com.ibm.as400.access.SpooledFile
 **/
public class AS400Message implements Serializable
{
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
     **/
    public static final int NOTIFY = 14;

    /**
     Message type for escape (exception already handled when API is called) messages.
     **/
    public static final int ESCAPE = 15;

    /**
     Message type for notify (exception not handled when API is called) messages.
     **/
    public static final int NOTIFY_NOT_HANDLED = 16;

    /**
     Message type for escape (exception not handled when API is called) messages.
     **/
    public static final int ESCAPE_NOT_HANDLED = 17;

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
     Constant for the option indicating all the messages should be returned.  All messages sent from invocation beginning to invocation end will be returned.  Systems not supporting this new option will revert to the behavior specified for option MESSAGE_OPTION_UP_TO_10.
     **/
    public static final int MESSAGE_OPTION_ALL = 2;

    // Date and time message sent.
    private Calendar date_;
    private String dateSent_;
    private String timeSent_;

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

    // CCSID of text of message.
    private int textCcsid_ = -1;
    // CCSID of substitution data.
    private int substitutionDataCcsid_ = -1;

    // System message came from.
    private transient AS400 system_;
    // Flag indicating if load has been done.
    private transient boolean messageLoaded_ = false;

    // date message was created @C1A
    private Date createDate_ = null;
    
    // date message was last modified @C1A
    private Date modificationDate_ = null;
    
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

    // This is used by some native code.
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

    // This is used by some native code.
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
        if (date_ == null && (dateSent_ != null || timeSent_ != null))
        {
          setDate();
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
     <ul>
     <li>&N - Force the text to a new line indented to column 2.  If the text is longer than 1 line, the next lines should be indented to column 4 until the end of the text or another format control character is found.
     <li>&P - Force the text to a new line indented to column 6.  If the text is longer than 1 line, the next lines should start in column 4 until the end of the text or another format control character is found.
     <li>&B - Force the text to a new line starting in column 4.  If the text is longer than 1 line, the next lines should start in column 6 until the end of the text or another format control character is found.
     </ul>
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
     Returns the substitution data.  This is unconverted data used to fill in the replacement characters in the message.  To convert the data to something useful, see the {@link com.ibm.as400.access.CharConverter CharConverter} class for String conversions (CHAR fields) and the {@link com.ibm.as400.access.BinaryConverter BinaryConverter} class for integer (BIN fields) and other numeric conversions.
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

        // See if we need to copy the substitution text to the message text.
        // This might be necessary if the message is an impromptu/immediate message,
        // generated by SNDPGMMSG MSG(...), where no MSGID or MSGF was specified.
        // In such cases, the returned message might have only substitution data.
        
        if (!messageLoaded_ &&
            substitutionData_ != null &&
            substitutionData_.length != 0 &&
            (text_ == null || text_.trim().length() == 0))
        {
          try
          {
            Trace.log(Trace.DIAGNOSTIC, "The 'text' field of the message is blank. Copying substitution data to message text.");

            // Copy the (converted) substitution data into our 'text' variable.
            int ccsid;
            if (substitutionDataCcsid_ == -1) {
              Trace.log(Trace.DIAGNOSTIC, "Assuming CCSID of substitution data is 37.");
              ccsid = 37;
            }
            else ccsid = substitutionDataCcsid_;
            text_ = CharConverter.byteArrayToString(ccsid, substitutionData_);
          }
          catch (Exception e) {
            Trace.log(Trace.ERROR, e);
          }
        }
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

    /**
     Loads additional message information from the system.  If this message does not have an associated message file, this method does nothing.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     **/
    public void load() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
       load(MessageFile.NO_FORMATTING);
    }

    /**
     Loads additional message information from the system.  If this message does not have an associated message file, this method does nothing.
     @param  helpTextFormatting  Formatting performed on the help text.  Valid values for this parameter are {@link MessageFile#NO_FORMATTING NO_FORMATTING}, {@link MessageFile#RETURN_FORMATTING_CHARACTERS RETURN_FORMATTING_CHARACTERS}, and {@link MessageFile#SUBSTITUTE_FORMATTING_CHARACTERS SUBSTITUTE_FORMATTING_CHARACTERS}.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     **/
    public void load(int helpTextFormatting) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Loading additional message information.");
        if (messageLoaded_)
        {
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Repeat message load not necessary.");
            return;
        }

        if (libraryName_ == null || fileName_ == null || libraryName_.trim().length() == 0 || fileName_.trim().length() == 0)
        {
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "No message file associated with this message: " + toString());
          return;
        }

        if (system_ == null)
        {
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Unable to load message file, because system was not specified when AS400Message was created.");
          return;
        }

        // Create message file object and get message from it.
        MessageFile file = new MessageFile(system_, QSYSObjectPathName.toPath(libraryName_, fileName_, "MSGF"));

        try
        {
           file.setHelpTextFormatting(helpTextFormatting);

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
            messageLoaded_ = true;  // Set flag to not go to system again.
        }
        catch (PropertyVetoException e)
        {
            Trace.log(Trace.ERROR, "Unexpected PropertyVetoException:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }
    }

    private void setDate()
    {
      if (dateSent_ == null && timeSent_ == null)
      {
        return;
      }
      if (dateSent_ != null && dateSent_.trim().length() == 0 && timeSent_ != null && timeSent_.trim().length() == 0)
      {
        dateSent_ = null;
        timeSent_ = null;
        return;
      }
      date_ = Calendar.getInstance();
      date_.clear();
      if (dateSent_ != null && dateSent_.trim().length() > 0)
      {
        date_.set(Calendar.YEAR, Integer.parseInt(dateSent_.substring(0, 3)) + 1900);
        date_.set(Calendar.MONTH, Integer.parseInt(dateSent_.substring(3, 5)) - 1);
        date_.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateSent_.substring(5, 7)));
      }
      if (timeSent_ != null && timeSent_.trim().length() > 0)
      {
        date_.set(Calendar.HOUR, Integer.parseInt(timeSent_.substring(0, 2)));
        date_.set(Calendar.MINUTE, Integer.parseInt(timeSent_.substring(2, 4)));
        date_.set(Calendar.SECOND, Integer.parseInt(timeSent_.substring(4, 6)));
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
      dateSent_ = dateSent;
      timeSent_ = timeSent;
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

    // Sets the system.
    // @param  system  The system.
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

    // Sets the CCSID of the message text.
    // @param  ccsid  The message text CCSID.
    void setTextCcsid(int ccsid)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting message text CCSID: " + ccsid);
        textCcsid_ = ccsid;
    }

    // Sets the CCSID of the substitution data.
    // @param  ccsid  The substitution data CCSID.
    void setSubstitutionDataCcsid(int ccsid)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting message substitution data CCSID: " + ccsid);
        substitutionDataCcsid_ = ccsid;
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

    // returns the original 'toString' value.  In mod 3 toString was changed to return its current value.  This method is added for those parts of the Toolbox that still need the value in in the old format.
    String toStringM2()
    {
        StringBuffer buffer = new StringBuffer();
        String id = getID();
        String text = getText();
        if (id != null)
        {
            buffer.append(id);
            buffer.append(" ");
        }
        if (text != null)
        {
            buffer.append(text);
        }
        return buffer.toString().trim();
    }

		 /**
		  * Returns the messages create date
		  * 
		  * @return the createDate_
		  */
                 // @C1A
		 public Date getCreateDate() {
		 		 return createDate_;
		 }

		 /**
		  * Sets the messages create date
		  * 
		  * @param createDate_ the createDate_ to set
		  */
                 // @C1A
		 public void setCreateDate(Date createDate_) {
		 		 this.createDate_ = createDate_;
		 }

		 /**
		  * Returns the messages last modification date
		  * 
		  * @return the modificationDate_
		  */
                 // @C1A
		 public Date getModificationDate() {
		 		 return modificationDate_;
		 }

		 /**
		  * Sets the messages last modification date
		  * 
		  * @param modificationDate_ the modificationDate_ to set
		  */
                 // @C1A
		 public void setModificationDate(Date modificationDate_) {
		 		 this.modificationDate_ = modificationDate_;
		 }
}
