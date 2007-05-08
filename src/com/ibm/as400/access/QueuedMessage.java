///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  QueuedMessage.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2007 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;

/**
 The QueuedMessage class represents a message on a message queue or job log.
 @see  com.ibm.as400.access.MessageQueue
 @see  com.ibm.as400.access.JobLog
 **/
// Implementation notes:
// * The constructor and set methods are not public, since it never makes sense for anyone other than MessageQueue or JobLog to call these.
// * Message keys are 4 bytes.
public class QueuedMessage extends AS400Message implements Serializable
{
    static final long serialVersionUID = 5L;

    private MessageQueue messageQueue_;

    private String sendingUser_ = "";
    private String sendingProgram_ = "";
    private String sendingJobName_ = "";
    private String sendingJobNumber_ = "";
    private String currentUser_ = "";
    private byte[] key_;
  
    private String replyStatus_ = "";
    private JobHashtable values_;
    private String alertOption_ = "";
    private String message_ = "";
    private String messageHelp_ = "";
    private String messageHelpReplacement_ = "";
    private String messageHelpReplacementandFormat_ = "";
    private String senderType_ = "";
    private String sendingModuleName_ = "";
    private String sendingProcedureName_ = "";
    private String sendingUserProfile_ = "";
    private String receivingType_ = "";
    private String receivingProgramName_ = "";
    private String receivingModuleName_ = "";
    private String receivingProcedureName_ = "";
    private String requestStatus_ = "";
    private String[] number_of_sending_statements_; 
    private String[] number_of_receiver_statements_; 


    // Constructs a QueuedMessage object.
    QueuedMessage()
    {
    }

    // Constructs a QueuedMessage object.
    // @param  messageQueue  The message queue.
    QueuedMessage(MessageQueue messageQueue)
    {
        if (messageQueue == null)
        {
            throw new NullPointerException("messageQueue");
        }
        messageQueue_ = messageQueue;
        setSystem(messageQueue_.getSystem());
    }

    // Constructs a QueuedMessage object. Used by MessageQueue.receive().
    // Called from MessageQueue.getMessages().
    QueuedMessage(MessageQueue messageQueue, int messageSeverity, String messageIdentifier, int messageType, byte[] messageKey, String messageFileName, String messageLibraryName, String dateSent, String timeSent)
    {
        super(messageIdentifier, null, messageFileName, messageLibraryName, messageSeverity, messageType, null, null, dateSent, timeSent, null);
        messageQueue_ = messageQueue;
        setSystem(messageQueue_.getSystem());
        key_ = messageKey;
    }

    // Constructs a QueuedMessage object. Called from JobLog.getMessages().
    QueuedMessage(AS400 system, int messageSeverity, String messageIdentifier, int messageType, byte[] messageKey, String messageFileName, String messageLibraryName, String dateSent, String timeSent)
    {
        super(messageIdentifier, null, messageFileName, messageLibraryName, messageSeverity, messageType, null, null, dateSent, timeSent, null);
        setSystem(system);
        key_ = messageKey;
    }
    
    //@HLA
    //  Constructs a QueuedMessage object. Called from HistoryLog.getMessages().
    QueuedMessage(AS400 system, int messageSeverity, String messageIdentifier, int messageType, String messageFileName, String messageLibraryName, String dateSent, String timeSent, String sendingJob, String sendingUserProfile, String sendingJobNumber, String currentUser, String messageData, byte[] replacementData)
    {
        super(messageIdentifier, messageData, messageFileName, messageLibraryName, messageSeverity, messageType, replacementData, null, dateSent, timeSent, null);
        setSystem(system);
        sendingJobName_ = sendingJob;
        sendingJobNumber_ = sendingJobNumber;
        sendingUser_ = sendingUserProfile;
        currentUser_ = currentUser;
    }

    // Constructs a QueuedMessage object. Used by MessageQueue.receive().
    QueuedMessage(MessageQueue messageQueue, int messageSeverity, String messageIdentifier, int messageType, byte[] messageKey, String messageFileName, String messageLibraryName, String sendingJob, String sendingUserProfile, String sendingJobNumber, String sendingProgramName, String dateSent, String timeSent, byte[] replacementData, String messageData, String messageHelp, String alertOption)
    {
        super(messageIdentifier, messageData, messageFileName, messageLibraryName, messageSeverity, messageType, replacementData, messageHelp, dateSent, timeSent, null);
        messageQueue_ = messageQueue;
        setSystem(messageQueue_.getSystem());
        key_ = messageKey;
        sendingUser_ = sendingUserProfile;
        sendingJobName_ = sendingJob;
        sendingJobNumber_ = sendingJobNumber;
        sendingProgram_ = sendingProgramName;
        alertOption_ = alertOption;
    }

    /**
     Returns the alert option.
     @return  The alert option.  Possible values are:
     <ul>
     <li>*DEFER - An alert is sent after local problem analysis.
     <li>*IMMED - An alert is sent immediately when the message is sent to a message queue that has the allow alerts attribute set to *YES.
     <li>*NO - No alert is sent.
     <li>*UNATTEND - An alert is sent immediately when the system is running
     in unattended mode.  See the ALRSTS network attribute.
     <li>"" - The alert option was not specified when the message was sent.
     </ul>
     **/
    public String getAlertOption()
    {
        return alertOption_.trim();
    }

    /**
     Returns the text of a predefined message without replacement data substitution option.If an impromptu message is listed, this field contains the impromptu message text.
     @return  The message without replacement data or an empty string if not set. 
     **/
    public String getMessage()
    {
        return message_.trim();
    }

    /**
     Returns the message help for the message listed without formatting characters and without replacement of data. If an impromptu message is listed, this field contains the impromptu message text.
     @return  The message help for the message listed without formatting characters and without replacement of data or an empty string if not set.  
     **/
    public String getMessageHelp()
    {
        return messageHelp_.trim();
    }

    /**
     Returns the message help for the message listed, including the replacement data. If an impromptu message is listed, this field contains the impromptu message text.
     @return The message help for the message listed, including the replacement data or an empty string if not set.
     **/
    public String getMessageHelpReplacement()
    {
        return messageHelpReplacement_.trim();
    }
    /**
     Returns the message help for the message listed, including the replacement data and the formatting characters. If an impromptu message is listed, this field contains the impromptu message text.
     @return The m7essage help for the message listed, including the replacement data and the formatting characters or an empty string if not set.
     **/
    public String getMessageHelpReplacementandFormat()
    {
        return messageHelpReplacementandFormat_.trim();
    }
    /**
     Returns the type of the sender (whether it is a program or procedure).
     @return The type of the sender or an empty string if not set.  Possible values are:
     <ul>
     <li>0 -  Sender is an OPM or a System Licensed Internal Code (SLIC) program with a name that is 12 characters or less.
     <li>1 -  Sender is a procedure within an ILE program and the procedure name is up to and including 256 characters in length.
     <li>2 -  Sender is a procedure within an ILE program and the procedure name is from 257 characters up to and including 4096 characters in length.
     <li>3 -  Sender is a SLIC program with a name that is from 13 characters up to and including 256 characters in length.
     </ul>
     **/
    public String getSenderType()
    {
        return senderType_.trim();
    }
    /**
     Returns the name of the module that contains the procedure sending the message. If the message was not sent by a procedure within an ILE program, this field is not set and the length of data field is 0.
     @return The name of the module that contains the procedure sending the message or an empty string if not set.
     **/
    public String getSendingModuleName()
    {
        return sendingModuleName_.trim();
    }
    /**
     Returns the name of the procedure sending the message. If the message was not sent by a procedure within an ILE program, this field is not set and the length of data field is 0. A nested procedure name has each procedure name separated by a colon. The outermost procedure name is identified first followed by the procedures it contains. The innermost procedure is identified last in the string.
     @return The name of the procedure sending the message or an empty string if not set.
     **/
    public String getSendingProcedureName()
    {
        return sendingProcedureName_.trim();
    }
    
    /**
     Returns the name of the user profile that the thread was running under when the message was sent.
     @return The name of the user profile that the thread was running under or an empty string if not set.
     **/
    public String getSendingUserProfile()
    {
        return sendingUserProfile_.trim();
    }
    /**
     Returns the type of the receiver (whether it is a program or a procedure).
     @return The type of the receiver or an empty string if not set.  Possible values are:
     <ul>
     <li>0 -  Receiver is an original program model (OPM) program
     <li>1 -  Receiver is a procedure within an ILE program and the procedure name is up to and including 256 characters in length
     <li>2 -  Receiver is a procedure within an ILE program and the procedure name is 257 or more characters in length.
     </ul>
     **/
    public String getReceivingType()
    {
        return receivingType_.trim();
    }
    /**
     Returns the program name, or the ILE program name that contains the procedure that the message was sent to.
     @return The program name or the ILE program name or an empty string if not set.
     **/
    public String getReceivingProgramName()
    {
        return receivingProgramName_.trim();
    }
    /**
     Returns the name of the module that contains the procedure where the message was sent. If the message was not sent to a procedure within an Integrated Language Environment (ILE) program, this field is not set and the length of data field is 0.
     @return The name of the module that contains the procedure where the message was sent or an empty string if not set.
     **/
    public String getReceivingModuleName()
    {
        return receivingModuleName_.trim();
    }
    /**
     Returns the name of the procedure receiving the message. If the message was not sent to a procedure within an ILE program, this field is not set and the length of data field is 0. A nested procedure name has each procedure name separated by a colon. The outermost procedure name is identified first followed by the procedures it contains. The innermost procedure is identified last in the string.
     @return The name of the procedure receiving the message or an empty string if not set.
     **/
    public String getReceivingProcedureName()
    {
        return receivingProcedureName_.trim();
    }
    
    /**
     Returns information regarding the processing status of the request message. 
     @return Information regarding the processing status of the request message or an empty string if not set.  Possible values are:
     <ul>
     <li>O -  This request message has been received and processed.
     <li>C -  This request message is currently being processed.
     <li>N -  This request message has not yet been processed.
     </ul>
     **/
    public String getRequestStatus()
    {
        return requestStatus_.trim();
    }
    
    /**
     Returns the level of the request-processing program that received the request message. If the message being listed is not a request, this field is set to 0.
     @returns The level of the request-processing program that received the request message or an empty string if not set.
     **/
    public Integer getRequestLevel()
    {
        if ( values_ != null)
        {
	    return  (Integer) values_.get(1201);
	}
        return null;
    }
    
    /**
     Returns the coded character set identifier (CCSID) that the message text is returned in. If a conversion error occurs or if the CCSID you requested in the message text to be converted to is 65535, the CCSID that the message text is stored in is returned. Otherwise, the CCSID you wanted your message text converted to is returned. If you do not want the text converted before it is returned to you but you do want to know the CCSID that the message text is stored in, specify 65535 on the coded character set identifier to return text and data in parameter. The CCSID that the message text is stored in is returned in the coded character set identifier for text field.
     @return The coded character set identifier (CCSID) that the message text is returned in or null if not set. Possible values are:
     <ul>
     <li>This applies to the following fields only :
     <li> * Message
     <li> * Message with replacement data
     <li> * Message help
     <li> * Message help with replacement data
     <li> * Message help with replacement data and formatting
     <li>   characters
     <li> * Message help with formatting characters
     <li> Note: This CCSID value does not apply to the replacement data that has been substituted into the text. See the coded character set identifier for data for this information.
     </ul>
     **/
    public Integer getCcsidCodedCharacterSetIdentifierForText()
    {
        if ( values_ != null)
        {
	    return  (Integer) values_.get(1301);
	}
        return null;
    }
    
    /**
     Returns the CCSID conversion status indicator for text.
     @return The CCSID conversion status indicator for text or null if not set.  Possible values_ are:
     <ul>
     <li>0  - No conversion was needed because the CCSID of the text matched the CCSID you wanted the text converted to.
     <li>1  - No conversion occurred because either the text was 65535 or the CCSID you wanted the text converted to was 65535.
     <li>2  - No conversion occurred because you did not ask for any text to be returned.
     <li>3  - The text was converted to the CCSID specified using the best fit conversion tables.
     <li>4  - A conversion error occurred using the best fit conversion tables so a default conversion was attempted. This completed without error.
     <li>-1 - An error occurred on both the best fit and default conversions. The data was not converted.
     </ul>
     **/
    public Integer getCcsidConversionStatusIndicatorForText()
    {
        if ( values_ != null)
        {
	    return  (Integer) values_.get(1302);
	}
        return null;
    }
    
    /**
     Returns the coded character set identifier (CCSID) that the replacement data is returned in. This only applies to the part of the replacement data that corresponds to a convertible character data type (*CCHAR). All other replacement data will not be converted before it is returned and can be considered to have a CCSID of 65535. If a conversion error occurs or if the CCSID you requested the data to be converted to is 65535, the CCSID of the data is returned. If there is no *CCHAR replacement data, 65535 is returned. Otherwise the CCSID you wanted the data converted to is returned.
     @return The coded character set identifier that the replacement data is returned in or null if not set. 
     **/
    public Integer getCcsidCodedCharacterSetIdentifierForData()
    {
        if ( values_ != null)
        {
	    return  (Integer) values_.get(1303);
	}
        return null;
    }
    
    /**
     Returns the CCSID conversion status indicator for data. 
     @return The CCSID conversion status indicator for data or null if not set.  Possible values are:
     <ul>
     <li>0  - No conversion was needed because the CCSID of the data matched the CCSID that you wanted the data converted to.
     <li>1  - No conversion occurred because either the data was 65535, or the CCSID you wanted the data converted to was 65535.
     <li>2  - No conversion occurred because you did not ask for any message data to be returned or the data did not contain any *CCHAR type data.
     <li>3  - The data was converted to the CCSID specified using the best fit conversion tables.
     <li>4  - A conversion error occurred using the best fit conversion tables so a default conversion was attempted. This completed without error.
     <li>-1 - An error occurred on both the best fit and default conversions. The data was not converted.
     </ul>
     **/
    public Integer getCcsidconversionStatusIndicatorForData()
    {
        if ( values_ != null)
        {
	    return  (Integer) values_.get(1304);
	}
        return null;
    }
     
    
    /**
     Returns number of sending statement numbers or instruction numbers available followed by an array of the sending statement numbers or instruction numbers. The number of statement numbers or instruction numbers available for the sending program or procedure. For OPM programs and nonoptimized procedures, this count is 1. For optimized procedures, this count can be greater than 1. In this case, each statement number represents a potential point at which the message could have been sent. If the mapping table information has been removed from the program, this field returns a count of 0, and no statement numbers are available. The array of sending statement numbers or instruction numbers immediately follows this field in the returned data.
     @return The number of sending statement numbers or instruction numbers available followed by an array of the sending statement numbers or instruction numbers or a null string array is not set.
     **/
    public String[] getSendingStatementNumbers()
    {
        if( number_of_sending_statements_ != null )
            return number_of_sending_statements_; 
	return new String[0];
    }
    
    /**
     Returns the number of statement numbers or instruction numbers available for the receiving program or procedure. For original program model (OPM) programs and nonoptimized procedures, this count is 1.For optimized procedures, this count can be greater than 1. In this case, each statement number represents a potential point at which the message could have been received. If the mapping table information has been removed from the program, this field returns a count of 0 and no statement numbers are available. The array of receiving statement numbers or instruction numbers immediately follows this field in the returned data.
     return The number of statement numbers or instruction numbers available for the receiving program or procedure or a null string array if nothing is set. 
     **/
    public String[] getReceiverStatementNumbers()
    {
        if ( number_of_receiver_statements_ != null ) 
            return number_of_receiver_statements_;
	return new String[0];
    }
    
    /**
     Returns the sending program name.
     @return  The sending program name, or "" if it is not set.
     **/
    public String getFromProgram()
    {
        if (sendingProgram_.length() == 0 && values_ != null)
        {
            String s = (String)values_.get(603);
            if (s != null)
            {
                sendingProgram_ = s.trim();
            }
        }
        return sendingProgram_;
    }

    /**
     Returns the sender job name.
     @return  The sender job name, or "" if it is not set.
     @see  #getFromJobNumber
     @see  #getUser
     **/
    public String getFromJobName()
    {
        if (sendingJobName_.length() == 0 && values_ != null)
        {
            String s = (String)values_.get(601);
            if (s != null)
            {
                sendingJobName_ = s.substring(0,10).trim();
            }
        }
        return sendingJobName_;
    }

    /**
     Returns the sender job number.
     @return  The sender job number, or "" if it is not set.
     @see  #getFromJobName
     @see  #getUser
     **/
    public String getFromJobNumber()
    {
        if (sendingJobNumber_.length() == 0 && values_ != null)
        {
            String s = (String)values_.get(601);
            if (s != null)
            {
                sendingJobNumber_ = s.substring(20,26);
            }
        }
        return sendingJobNumber_;
    }

    /**
     Returns the sender job's user. To get the current user of the message, call {@link #getCurrentUser getCurrentUser()} when accessing a system running V5R3 or higher.
     @return  The sender job's user, or "" if it is not set.
     @see  #getFromJobName
     @see  #getFromJobNumber
     **/
    public String getUser()
    {
        if (sendingUser_.length() == 0 && values_ != null)
        {
            String s = (String)values_.get(601);
            if (s != null)
            {
                sendingUser_ = s.substring(10,20).trim();
            }
        }
        return sendingUser_;
    }

    /**
     Returns the 4-byte message key.
     @return  The message key, or null if it is not set.
     **/
    public byte[] getKey()
    {
        return key_;
    }

    /**
     Returns the message queue.
     @return  The message queue, or null if it is not set.
     **/
    public MessageQueue getQueue()
    {
        return messageQueue_;
    }

    /**
     Returns the reply status.
     @return  The reply status, "" if it is not set, or null if it is not applicable.
     **/
    public String getReplyStatus()
    {
        if (replyStatus_ == "" && values_ != null)
        {
            String s = (String)values_.get(1001);
            if (s != null)
            {
                replyStatus_ = s.trim();
            }
        }
        return replyStatus_;
    }

    /**
     Returns the current user name. If the system being accessed is running V5R2 or earlier, then "" is returned.
     @return  The current user name, or "" if it is not set.
     **/
    public String getCurrentUser()
    {
        if (currentUser_.length() == 0 && values_ != null)
        {
            String s = (String)values_.get(607);
            if (s != null)
            {
                currentUser_ = s.trim();
            }
        }
        return currentUser_;
    }

    // Helper method called by MessageQueue.
    void setAsInt(int fieldID, int value)
    {
        setValueInternal(fieldID, new Integer(value));
    }

    // Helper method called by MessageQueue.
    void setAsLong(int fieldID, long value)
    {
        setValueInternal(fieldID, new Long(value));
    }

    // Helper method called by MessageQueue.
    void setValueInternal(int fieldID, Object value)
    {
       if (values_ == null) 
           values_ = new JobHashtable();
       
       values_.put(fieldID, value);
       switch (fieldID)
       {
            case 101:
                alertOption_ = (String)value;
                break;
	    case 201:
                setSubstitutionData((byte[])value);
	        break;
	    case 301: 
                message_ = (String)value; 
                break;
            case 302:
                setText((String)value);
                break;
            case 401: 
	        messageHelp_ = (String)value; 
                break;
	    case 402:
                messageHelpReplacement_ = (String)value; 
	        break; 
	    case 403:
                messageHelpReplacementandFormat_ = (String)value; 
	        break; 
            case 404:
                setHelp((String)value);
                break;
            case 501:
                setDefaultReply((String)value);
                break;
	    case 602:
                senderType_ = (String)value; 
	        break; 
	    case 603:
	        // Nothing to be done. This is saved in the vector
	        break; 
	    case 604:
                sendingModuleName_ = (String)value; 
	        break; 
	    case 605:
                sendingProcedureName_ = (String)value; 
	        break; 
	    case 606: 
	        number_of_sending_statements_ = (String [])value;
		break;
	    case 607:
	        sendingUserProfile_ = (String)value;
		break;
	    case 702:
	        receivingType_ = (String)value;
		break;
	    case 703:
	        receivingProgramName_ = (String)value;
		break;
	    case 704:
	        receivingModuleName_ = (String)value;
		break;
	    case 705:
	        receivingProcedureName_ = (String)value;
		break;
	    case 706:
	        number_of_receiver_statements_ = (String [])value;
		break;
	    case 1101:
                requestStatus_ = (String)value;
	        break;
            default:
                break;
        }
    }

    /**
     Returns the String representation of this QueuedMessage object.
     @return  The string.
     **/
    public String toString()
    {
        return super.toStringM2();
    }
}
