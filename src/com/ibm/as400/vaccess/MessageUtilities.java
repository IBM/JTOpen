///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: MessageUtilities.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;



/**
The MessageUtilities class provides some commonly used routines
for implementation of the message support.
**/
class MessageUtilities
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String completionTypeText_                 = ResourceLoader.getText ("MESSAGE_TYPE_COMPLETION");
    private static final String diagnosticTypeText_                 = ResourceLoader.getText ("MESSAGE_TYPE_DIAGNOSTIC");
    private static final String informationalTypeText_              = ResourceLoader.getText ("MESSAGE_TYPE_INFORMATIONAL");
    private static final String inquiryTypeText_                    = ResourceLoader.getText ("MESSAGE_TYPE_INQUIRY");
    private static final String sendersCopyTypeText_                = ResourceLoader.getText ("MESSAGE_TYPE_SENDERS_COPY");
    private static final String requestTypeText_                    = ResourceLoader.getText ("MESSAGE_TYPE_REQUEST");
    private static final String requestWithPromptingTypeText_       = ResourceLoader.getText ("MESSAGE_TYPE_REQUEST_WITH_PROMPTING");
    private static final String notifyTypeText_                     = ResourceLoader.getText ("MESSAGE_TYPE_NOTIFY");
    private static final String escapeTypeText_                     = ResourceLoader.getText ("MESSAGE_TYPE_ESCAPE");
    private static final String replyNotValidityCheckedTypeText_    = ResourceLoader.getText ("MESSAGE_TYPE_REPLY_NOT_VALIDITY_CHECKED");
    private static final String replyValidityCheckedTypeText_       = ResourceLoader.getText ("MESSAGE_TYPE_REPLY_VALIDITY_CHECKED");
    private static final String replyMessageDefaultUsedTypeText_    = ResourceLoader.getText ("MESSAGE_TYPE_REPLY_MESSAGE_DEFAULT_USED");
    private static final String replySystemDefaultUsedTypeText_     = ResourceLoader.getText ("MESSAGE_TYPE_REPLY_SYSTEM_DEFAULT_USED");
    private static final String replyFromSystemReplyListTypeText_   = ResourceLoader.getText ("MESSAGE_TYPE_REPLY_FROM_SYSTEM_REPLY_LIST");
    private static final String unexpectedTypeText_                 = ResourceLoader.getText ("MESSAGE_TYPE_UNEXPECTED");



/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
    }



/**
Returns the text representation of a message type.

@param  The int representation.
@return The text representation.
**/
    static String getTypeText (int messageType)
    {
        switch (messageType) {
            case 1:
                return completionTypeText_;
            case 2:
                return diagnosticTypeText_;
            case 4:
                return informationalTypeText_;
            case 5:
                return inquiryTypeText_;
            case 6:
                return sendersCopyTypeText_;
            case 8:
                return requestTypeText_;
            case 10:
                return requestWithPromptingTypeText_;
            case 14:
                return notifyTypeText_;
            case 15:
                return escapeTypeText_;
            case 21:
                return replyNotValidityCheckedTypeText_;
            case 22:
                return replyValidityCheckedTypeText_;
            case 23:
                return replyMessageDefaultUsedTypeText_;
            case 24:
                return replySystemDefaultUsedTypeText_;
            case 25:
                return replyFromSystemReplyListTypeText_;
            default:
                return unexpectedTypeText_;
        }
    }


}
