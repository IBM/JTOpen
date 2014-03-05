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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;                   // @C1A

/**
 Represents a message returned from an IBM i system.  A Java program does not normally
 create AS400Message objects directly.  Instead, AS400Message objects are created and 
 returned by various other IBM Toolbox for Java components.
 <br>Some attributes are only available as the result of a command or program call. 
 Refer to the individual getXXX method to see if the attribute is only available as the
 result of a command or progam call. 
<br><i>Usage hint:</i> To fully "prime" an AS400Message object with additional information
 that otherwise might not be returned from the system, call the load() method.  
 For example, if getHelp() returns null, 
 try preceding the getHelp() with a call to load().
 @see  com.ibm.as400.access.AS400Exception
 @see  com.ibm.as400.access.CommandCall
 @see  com.ibm.as400.access.ProgramCall
 @see  com.ibm.as400.access.SpooledFile
 **/
public class AS400Message implements Serializable
{
    static final long serialVersionUID = 4L;
    static final int EXTENSION_VERSION = 5; 
    static final int EXTENSION_VERSION_FIRST = 5; 
    static final byte[] EXTENSION = {'J','T','O','P','E','N','E','X','T'};  

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
    
    
    //
    // New fields for version 5 of the object
    // The serial ID remains 4, but the
    // writeObject and read Object will handle these transient fields
    //
    private transient byte[] key_ = null;
    private transient String messageFileLibrarySpecified_ = null;
    private transient String sendingProgramName_ = null;
    private transient String sendingProgramInstructionNumber_ = null;
    private transient String receivingProgramName_ = null;
    private transient String receivingProgramInstructionNumber_ = null;
    private transient String sendingType_ = null;
    private transient String receivingType_ = null;
    private transient int textCcsidConversionStatusIndicator_ = 0;
    private transient int dataCcsidConversionStatusIndicator_ = 0;
    private transient String alertOption_ = null; 
    
    
    
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
     * Handle the serialization of different levels of the object
     * Each time we add a field to the object, it must be transient
     * and we must explicitly serialize it below. 
     */
    
    
    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {

      boolean nullIndicator = false; 
      byte[] oldSubstitutionData = substitutionData_; 
      // The current version is 5 meaning that the following
      // transient fields must also be serialized into the substitutionData field
      // This is nearly compatible with earlier version except that the extra
      // encoding stuff will be visible at the end of the substitutionData field
      if ((key_ != null ) || 
          (messageFileLibrarySpecified_ != null) || 
          (sendingProgramName_ != null) || 
          (sendingProgramInstructionNumber_ != null) || 
          (receivingProgramName_ != null) ||
          (receivingProgramInstructionNumber_ != null) ||
          (sendingType_ != null) ||
          (receivingType_ != null) ||
          (textCcsidConversionStatusIndicator_ != 0) ||
          (dataCcsidConversionStatusIndicator_ != 0) || 
          (alertOption_ != null)) {
       
          if (oldSubstitutionData == null) { 
            nullIndicator=true;
            oldSubstitutionData = new byte[0]; 
          }
          // Serialize this stuff into a new byte array
          ByteArrayOutputStream  baos = new ByteArrayOutputStream(); 
          ObjectOutputStream oos = new ObjectOutputStream(baos); 
          oos.writeInt(EXTENSION_VERSION);
          oos.writeBoolean(nullIndicator); 
          oos.writeObject(key_); 
          oos.writeObject(messageFileLibrarySpecified_); 
          oos.writeObject(sendingProgramName_); 
          oos.writeObject(sendingProgramInstructionNumber_); 
          oos.writeObject(receivingProgramName_);
          oos.writeObject(receivingProgramInstructionNumber_);
          oos.writeObject(sendingType_);
          oos.writeObject(receivingType_);
          oos.writeInt(textCcsidConversionStatusIndicator_ );
          oos.writeInt(dataCcsidConversionStatusIndicator_ ); 
          oos.writeObject(alertOption_); 
          oos.flush();
          byte[] newStuff=baos.toByteArray();
          substitutionData_ = new byte[oldSubstitutionData.length + EXTENSION.length + newStuff.length+4+EXTENSION.length]; 
          int offset = 0;
          System.arraycopy(oldSubstitutionData, 0, substitutionData_, offset, oldSubstitutionData.length) ;
          offset += oldSubstitutionData.length;
          System.arraycopy(EXTENSION, 0, substitutionData_, offset, EXTENSION.length);
          offset += EXTENSION.length;
          System.arraycopy(newStuff, 0, substitutionData_, offset, newStuff.length);
          offset += newStuff.length;
          // Save the original length of the extension data  
          substitutionData_[offset] = (byte)((oldSubstitutionData.length >> 24) & 0xFF); 
          offset++; 
          substitutionData_[offset] = (byte)((oldSubstitutionData.length >> 16) & 0xFF); 
          offset++; 
          substitutionData_[offset] = (byte)((oldSubstitutionData.length >> 8) & 0xFF); 
          offset++; 
          substitutionData_[offset] = (byte)((oldSubstitutionData.length ) & 0xFF); 
          offset++; 
          
          System.arraycopy(EXTENSION, 0, substitutionData_, offset, EXTENSION.length);
      }
      
      
      out.defaultWriteObject();

      if (nullIndicator) {
        substitutionData_ = null; 
      } else { 
        substitutionData_ = oldSubstitutionData;
      }
    }
    private static boolean arrayEndsWith(byte[] byteArray, byte[] pattern) {
      if (byteArray.length < pattern.length) { 
        return false; 
      }
      int searchOffset = byteArray.length - pattern.length; 
      for (int i = 0; i < pattern.length; i++) {
        if (byteArray[searchOffset+i] != pattern[i]) {
          return false; 
        }
      }
      return true; 
    }
    
    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
      in.defaultReadObject();
      
      // Check to see if substitutionData_ ends with EXTENSION
      // If so, get the length to go to the beginning of the stream. 
      // Use a ByteArrayInputStream to read the data, based on the version number.
      if (substitutionData_ != null) { 
        if (substitutionData_.length > 2 * EXTENSION.length) {
          if (arrayEndsWith(substitutionData_, EXTENSION)) {
            int lengthOffset = substitutionData_.length - EXTENSION.length - 4; 
            int realSubstitutionLength = 0; 
            realSubstitutionLength += (0xFF & ((int) substitutionData_[lengthOffset])) << 24; 
            lengthOffset++; 
            realSubstitutionLength += (0xFF & ((int) substitutionData_[lengthOffset])) << 16; 
            lengthOffset++; 
            realSubstitutionLength += (0xFF & ((int) substitutionData_[lengthOffset])) << 8; 
            lengthOffset++; 
            realSubstitutionLength += (0xFF & ((int) substitutionData_[lengthOffset])) ; 
            
            byte[] realSubstitution  = new byte[realSubstitutionLength]; 
            System.arraycopy(substitutionData_, 0, realSubstitution, 0, realSubstitutionLength);
            ByteArrayInputStream bais = new ByteArrayInputStream(substitutionData_, realSubstitutionLength + EXTENSION.length, substitutionData_.length - realSubstitutionLength -  EXTENSION.length); 
            ObjectInputStream ois = new ObjectInputStream(bais); 
            int extensionVersion = ois.readInt();
            boolean nullIndicator = false; 
            if (extensionVersion >= EXTENSION_VERSION_FIRST ) { 
              nullIndicator = ois.readBoolean(); 
              key_=(byte[]) ois.readObject(); 
              messageFileLibrarySpecified_=(String) ois.readObject(); 
              sendingProgramName_=(String)ois.readObject(); 
              sendingProgramInstructionNumber_=(String)ois.readObject(); 
              receivingProgramName_=(String)ois.readObject();
              receivingProgramInstructionNumber_ =(String)ois.readObject();
              sendingType_=(String)ois.readObject();
              receivingType_=(String)ois.readObject();
              textCcsidConversionStatusIndicator_=ois.readInt( );
              dataCcsidConversionStatusIndicator_=ois.readInt( ); 
              alertOption_=(String)ois.readObject(); 
            
            } /* FIRST VERSION */ 
            
            
            if (nullIndicator) { 
              substitutionData_ = null; 
            } else { 
              substitutionData_ = realSubstitution; 
            }
            
          } /* substititionData ends with EXTENSION */ 
        } /* substitition data is long enough to have more data */ 
      }  /* substitution data is not null */ 
      
      
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
      date_ = AS400Calendar.getGregorianInstance();
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

		 /**
		  * Set the message key.
		  * @param key
		  */
    public void setKey(byte[] key) {
      this.key_ = key; 
    }
    /**
     * Returns the 4-byte message key..  The message key is usually only available 
     * from messages returned by a command or program call. 
     */
    public byte[] getKey() { 
      return key_; 
    }

    /**
     * Set the MessageFileLibrarySpecified. 
     * @param library
     */
    public void setMessageFileLibrarySpecified(String library) {
      this.messageFileLibrarySpecified_ = library; 
      
    }
    /**
     * Get the message file library specified.  This is usually only available 
     * from messages returned by a command or program call. 
     */
    public String getMessageFileLibrarySpecified() { 
      return messageFileLibrarySpecified_; 
    }

    /**
     * Set the sending program name
     * @param programName
     */
    public void setSendingProgramName(String programName) {
      this.sendingProgramName_ = programName; 
    }
    /**
     * Get the sending program name.  This is usually only available 
     * from messages returned by a command or program call. 
     */
    public String getSendingProgramName() { 
      return sendingProgramName_; 
    }

    /**
     * Set the sending program instruction number.  
     * @param instructionNumber
     */
    public void setSendingProgramInstructionNumber(String instructionNumber) {
      this.sendingProgramInstructionNumber_ = instructionNumber; 
    }
    /**
     * Get the sending program instruction number.  This is usually only available 
     * from messages returned by a command or program call. 
     */
    public String getSendingProgramInstructionNumber() { 
      return sendingProgramInstructionNumber_; 
    }

    /**
     * Set the receiving program name.  
     * @param programName
     */
    
    public void setReceivingProgramName(String programName) {
        this.receivingProgramName_ = programName; 
    }
    /**
     * Get the receiving program name.  This is usually only available 
     * from messages returned by a command or program call. 
     */
    public String getReceivingProgramName() { 
      return receivingProgramName_; 
    }

    /**
     * set the receiving program instruction number 
     * @param instructionNumber
     */
    public void setReceivingProgramInstructionNumber(String instructionNumber) {
      this.receivingProgramInstructionNumber_ = instructionNumber;
    }
    
    /**
     * Get the receiving program instruction number.  This is usually only available 
     * from messages returned by a command or program call. 
     */
    public String getReceivingProgramInstructionNumber() { 
      return receivingProgramInstructionNumber_; 
    }

    /**
     * Set the sending type of the program 
     * @param sendingType
     */
    public void setSendingType(String sendingType) {
      this.sendingType_ = sendingType; 
    }

    /**
     * Get the type of the sender (whether it is a program or procedure). Possible values and their meanings are as follow:
     * <ul>
     * <li>0   Sender is an original program model (OPM) program or a System Licensed Internal Code (SLIC) 
     * program with up to and including 12 characters in its name.
     * <li>1   Sender is a procedure within an ILE program, and the procedure name is up to and including 
     * 256 characters in length.
     * <li>2   Sender is a procedure within an ILE program, and the procedure name is from 257 characters 
     * up to and including 4096 characters in length. For this type, the sending procedure name is blank 
     * and the name is unavailable. 
     * <li>3   Sender is a SLIC program with 13 or more characters in its name. For this type, 
     * the sending program name is blank, and the name is unavailable.
     * </ul>  
     * The type is usually only available from messages returned by a command or program call. 
     */
    public String getSendingType() { 
      return sendingType_; 
    }

    /**
     * set the receiving type 
     * @param receivingType
     */
    public void setReceivingType(String receivingType) {
      this.receivingType_ = receivingType; 
    }

    /**
     * Get the type of the receiver (whether it is a program or procedure). Possible values and their meanings are as follow:
     * <ul>
     * <li>0   Sender is an original program model (OPM) program or a System Licensed Internal Code (SLIC) 
     * program with up to and including 12 characters in its name.
     * <li>1   Sender is a procedure within an ILE program, and the procedure name is up to and including 
     * 256 characters in length.
     * <li>2   Sender is a procedure within an ILE program, and the procedure name is from 257 characters 
     * up to and including 4096 characters in length. For this type, the sending procedure name is blank 
     * and the name is unavailable. 
     * <li>3   Sender is a SLIC program with 13 or more characters in its name. For this type, 
     * the sending program name is blank, and the name is unavailable.
     * </ul>  
     * The type is usually only available from messages returned by a command or program call. 
     */
    public String getReceivingType() { 
      return receivingType_; 
    }


    /**
     * set the text ccsid conversion status indicator. 
     * @param conversionStatusIndicator
     */
    
    public void setTextCcsidConversionStatusIndicator(int conversionStatusIndicator) {
      this.textCcsidConversionStatusIndicator_ = conversionStatusIndicator; 
    }
    
    /**
     * Get the text ccsid conversion status indicator.
     * The following values may be returned:
     * <ul>
     * <li>0   No conversion was needed because the CCSID of the message or message help text matched the CCSID you wanted the message or message help text converted to.
     * <li>1   No conversion occurred because either the message or message help text was 65535 or the CCSID you wanted the message or message help text converted to was 65535.
     * <li>2   No conversion occurred because you did not supply enough space for the message or message help text.
     * <li>3   The message or message help text was converted to the CCSID specified using the best fit conversion tables.
     * <li>4   A conversion error occurred using the best fit conversion tables so a default conversion was attempted. This completed without error.
     * <li>-1  An error occurred on both the best fit and default conversions. The text was not converted.
     * </ul> 
     * This is usually only available 
     * from messages returned by a command or program call. 
     */
    public int getTextCcsidConversionStatusIndicator() { 
      return textCcsidConversionStatusIndicator_; 
    }

    /**
     * set the data ccsid conversion status indicator. 
     * @param conversionStatusIndicator
     */
    
    public void setDataCcsidConversionStatusIndicator(int conversionStatusIndicator) {
      this.dataCcsidConversionStatusIndicator_ = conversionStatusIndicator; 
    }
    
    /**
     * Get the data ccsid conversion status indicator.
     * The following values may be returned:
     * <ul>
     * <li>0   No conversion was needed because the CCSID of the message or message help text matched the CCSID you wanted the message or message help text converted to.
     * <li>1   No conversion occurred because either the message or message help text was 65535 or the CCSID you wanted the message or message help text converted to was 65535.
     * <li>2   No conversion occurred because you did not supply enough space for the message or message help text.
     * <li>3   The message or message help text was converted to the CCSID specified using the best fit conversion tables.
     * <li>4   A conversion error occurred using the best fit conversion tables so a default conversion was attempted. This completed without error.
     * <li>-1  An error occurred on both the best fit and default conversions. The text was not converted.
     * </ul> 
     * This is usually only available 
     * from messages returned by a command or program call. 
     */
    public int getDataCcsidConversionStatusIndicator() { 
      return dataCcsidConversionStatusIndicator_; 
    }

/**
 * set the alert option    
 * @param alertOption
 */

    public void setAlertOption(String alertOption) {
      this.alertOption_ = alertOption; 
    }

    /**
     * Get the alert option
     * Whether and when an SNA alert is created and sent for the message. If a message is received, the value is one of the following:
     * <ul>
     * <li>*DEFER  An alert is sent after local problem analysis.
     * <li>*IMMED  An alert is sent immediately when the message is sent to a message queue that has the allow alerts attribute set to *YES.
     * <li>*NO   No alert is sent.
     * <li>*UNATTEND   An alert is sent immediately when the system is running in unattended mode (when the value of the alert status network attribute, ALRSTS, is *UNATTEND)..
     * </ul> 
     * This is usually only available 
     * from messages returned by a command or program call. 
     */
    
    public String getAlertOption() { 
      return alertOption_; 
    }
    
  private boolean checkObject(Object a, Object b) {
    if (a == null) { 
      if (b == null) {
        return true;
      } else {
        return false; 
      }
    } else {
      return a.equals(b); 
    }
  }
  
  public boolean equals(Object o) {
    if (o instanceof AS400Message) {
      
      AS400Message m = (AS400Message) o;

      // Compare all the fields 
      if (!checkObject(date_, m.date_)) return false; 
      if (!checkObject(dateSent_, m.dateSent_)) return false;
      if (!checkObject(timeSent_, m.timeSent_)) return false; 
      if (!checkObject(   fileName_,m.fileName_)) return false;
      if (!checkObject(   id_, m.id_)) return false;
      if (!checkObject(   libraryName_, m.libraryName_)) return false;
      if (!checkObject(   defaultReply_, m.defaultReply_)) return false;
      if ( severity_ != m.severity_) return false; 
      if (!checkByteArray(substitutionData_,m.substitutionData_)) return false; 
      if (!checkObject(   text_, m.text_)) return false;
      if (type_ != m.type_) return false;        
      if (!checkObject( help_, m.help_)) return false;
      if (textCcsid_ != m.textCcsid_) return false; 
      if (substitutionDataCcsid_ != m.substitutionDataCcsid_) return false; 
      if (!checkObject(createDate_,m.createDate_)) return false;
      if (!checkObject(modificationDate_,m.modificationDate_)) return false;
        
        
        //
        // New fields for version 5 of the object
      if (!checkByteArray(key_, m.key_)) return false; 
      if (!checkObject(messageFileLibrarySpecified_,m.messageFileLibrarySpecified_)) return false;
        if (!checkObject(sendingProgramName_,m.sendingProgramName_)) return false;
        if (!checkObject(sendingProgramInstructionNumber_, m.sendingProgramInstructionNumber_)) return false;
        if (!checkObject(receivingProgramName_,m.receivingProgramName_)) return false;
        if (!checkObject(receivingProgramInstructionNumber_,m.receivingProgramInstructionNumber_)) return false;
        if (!checkObject(sendingType_,m.sendingType_)) return false;
        if (!checkObject(receivingType_,m.receivingType_)) return false;
        if ( textCcsidConversionStatusIndicator_ != m.textCcsidConversionStatusIndicator_) return false; 
        if ( dataCcsidConversionStatusIndicator_ != m.dataCcsidConversionStatusIndicator_) return false; 
        if (!checkObject( alertOption_, m.alertOption_)) return false; 

        
        return true; 
      } else {
        return false; 
      }
    }

    /* Serialization unit tests (to be commented out ) */ 
    
    private boolean checkByteArray(byte[] ba1, byte[] ba2) {
      if (ba1 == null) { 
        if (ba2 == null) {
          return true; 
        } else { 
          return false; 
        }
      } else if (ba2 == null) {
          return false; 
      } else { 
        if (ba1.length != ba2.length) return false; 
        for (int i = 0; i < ba1.length; i++) { 
          if (ba1[i] != ba2[i]) return false; 
        }
      }
      return true; 
  }
/*
    public static void main(String args[]) { 
      try { 
      AS400Message message;
      AS400Message deserializedMessage; 
      ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
      ObjectOutputStream oos = new ObjectOutputStream(baos); 
      ByteArrayInputStream bais; 
      ObjectInputStream ois; 
      StringBuffer sb = new StringBuffer(); 
      
      System.out.println("Testing serialization "); 
      
      System.out.println("Test 1:  Testing with no extra information "); 
      byte[] substitutionData = { 0x11, 0x22, 0x33, 0x44, 0x55, 0x66 }; 
      message = new AS400Message("MCH2462", "Text", "fileName", "libraryName", 10, 10, substitutionData, "help"); 
      baos.reset(); 
      oos = new ObjectOutputStream(baos); 
      oos.writeObject(message); 
      oos.flush(); 
      byte[] stuff = baos.toByteArray(); 
      
      bais = new ByteArrayInputStream(stuff); 
      ois = new ObjectInputStream(bais);
      deserializedMessage = (AS400Message) ois.readObject(); 
      if (message.equals(deserializedMessage)) {
        System.out.println("Test 1: Passed"); 
      } else {
        System.out.println("Test 1: ******************* FAILED *******************"); 
        System.out.println("Input ="+message); 
        System.out.println("Output="+deserializedMessage); 
      }
      
      
      
      

    System.out.println("Test 2:  Testing with null substitution data "); 
    byte[] substitutionData2 = null; 
    message = new AS400Message("MCH2462", "Text", "fileName", "libraryName", 10, 10, substitutionData2, "help"); 
    baos.reset(); 
    oos = new ObjectOutputStream(baos); 
    oos.writeObject(message); 
    oos.flush(); 
    stuff = baos.toByteArray(); 
    
    bais = new ByteArrayInputStream(stuff); 
    ois = new ObjectInputStream(bais);
    deserializedMessage = (AS400Message) ois.readObject(); 
    if (message.equals(deserializedMessage)) {
      System.out.println("Test 2: Passed"); 
    } else {
      System.out.println("Test 2: ******************* FAILED *******************"); 
      System.out.println("Input ="+message); 
      System.out.println("Output="+deserializedMessage); 
    }
    
    System.out.println("Test 3:  Testing with extra information "); 
    byte[] substitutionData3 = { 0x11, 0x22, 0x33, 0x44, 0x55, 0x66 }; 
    message = new AS400Message("MCH2462", "Text", "fileName", "libraryName", 10, 10, substitutionData3, "help");
    message.setSendingType("Sending type"); 
    message.setReceivingType("Receiving type"); 
    baos.reset(); 
    oos = new ObjectOutputStream(baos); 
    oos.writeObject(message); 
    oos.flush(); 
    stuff = baos.toByteArray(); 
    
    bais = new ByteArrayInputStream(stuff); 
    ois = new ObjectInputStream(bais);
    deserializedMessage = (AS400Message) ois.readObject();
    sb.setLength(0); 
    if (message.equals(deserializedMessage, sb)) {
      System.out.println("Test 3: Passed"); 
    } else {
      System.out.println("Test 3: ******************* FAILED *******************"); 
      System.out.println("Input ="+message); 
      System.out.println("Output="+deserializedMessage);
      System.out.println("Info="+sb.toString()) ;
    }
    
    
    
    

  System.out.println("Test 4:  Testing with null substitution data and extra information "); 
  byte[] substitutionData4 = null; 
  message = new AS400Message("MCH2462", "Text", "fileName", "libraryName", 10, 10, substitutionData4, "help"); 
  message.setSendingType("Sending type"); 
  message.setReceivingType("Receiving type"); 
baos.reset(); 
  oos = new ObjectOutputStream(baos); 
  oos.writeObject(message); 
  oos.flush(); 
  stuff = baos.toByteArray(); 
  
  bais = new ByteArrayInputStream(stuff); 
  ois = new ObjectInputStream(bais);
  deserializedMessage = (AS400Message) ois.readObject();
  sb.setLength(0); 
  if (message.equals(deserializedMessage, sb)) {
    System.out.println("Test 4: Passed"); 
  } else {
    System.out.println("Test 4: ******************* FAILED *******************"); 
    System.out.println("Input ="+message); 
    System.out.println("Output="+deserializedMessage); 
    System.out.println("Info="+sb.toString()) ;
  }
  
    
    
    } catch (Exception e) {
      e.printStackTrace(); 
    }
  } // main 
    
    public boolean equals(Object o, StringBuffer sb ) {
      if (o instanceof AS400Message) {
        boolean areEqual = true; 
        AS400Message m = (AS400Message) o;

        // Compare all the fields 
        if (!checkObject(date_, m.date_)) {
          sb.append("date="+date_+" m.date="+m.date_+"\n"); 
          areEqual = false; 
        }
        if (!checkObject(dateSent_, m.dateSent_)) {
          areEqual = false;
        }
        if (!checkObject(timeSent_, m.timeSent_)) {
          areEqual = false; 
        }
        if (!checkObject(   fileName_,m.fileName_)) {
          areEqual = false;
        }
        if (!checkObject(   id_, m.id_)){
          areEqual = false;
        }
        if (!checkObject(   libraryName_, m.libraryName_)){
          areEqual = false;
        }
        if (!checkObject(   defaultReply_, m.defaultReply_)) {
          areEqual = false;
        }
        if ( severity_ != m.severity_) {
          areEqual = false; 
        }
        if (!checkByteArray(substitutionData_,m.substitutionData_)) {
          areEqual = false; 
        }
        if (!checkObject(   text_, m.text_)) {
          areEqual = false;
        }
        if (type_ != m.type_) {
          areEqual = false;        
        }
        if (!checkObject( help_, m.help_)) {
          areEqual = false;
        }
        if (textCcsid_ != m.textCcsid_) {
          areEqual = false; 
        }
        if (substitutionDataCcsid_ != m.substitutionDataCcsid_) {
          areEqual = false; 
        }
        if (!checkObject(createDate_,m.createDate_)) {
          areEqual = false;
        }
        if (!checkObject(modificationDate_,m.modificationDate_)) {
          areEqual = false;
        }
          
          
          //
          // New fields for version 5 of the object
        if (!checkByteArray(key_, m.key_)) {
          areEqual = false; 
        }
        if (!checkObject(messageFileLibrarySpecified_,m.messageFileLibrarySpecified_)) {
          areEqual = false;
        }
          if (!checkObject(sendingProgramName_,m.sendingProgramName_)) {
            areEqual = false;
          }
          if (!checkObject(sendingProgramInstructionNumber_, m.sendingProgramInstructionNumber_)) {
            areEqual = false;
          }
          if (!checkObject(receivingProgramName_,m.receivingProgramName_)) {
            areEqual = false;
          }
          if (!checkObject(receivingProgramInstructionNumber_,m.receivingProgramInstructionNumber_)) {
            areEqual = false;
          }
          if (!checkObject(sendingType_,m.sendingType_)) {
            areEqual = false;
          }
          if (!checkObject(receivingType_,m.receivingType_)) {
            areEqual = false;
          }
          if ( textCcsidConversionStatusIndicator_ != m.textCcsidConversionStatusIndicator_) {
            areEqual = false; 
          }
          if ( dataCcsidConversionStatusIndicator_ != m.dataCcsidConversionStatusIndicator_) {
            areEqual = false; 
          }
          if (!checkObject( alertOption_, m.alertOption_)) {
            areEqual = false; 
          }

          
          return areEqual; 
        } else {
          sb.append("Object types not the same\n"); 
          return false; 
        }
      }

   */ 
    
    
}
