///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JobI0400Format.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class JobI0400Format extends JobFormat
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  JobI0400Format(AS400 sys)
  {
    super(sys);
    setName("JOBI0400");
    addChar(13, "dateAndTimeJobEnteredSystem");
    addChar(13, "dateAndTimeJobBecameActive");
    addChar(15, "jobAccountingCode");
    addChar(10, "jobDescriptionName");
    addChar(10, "jobDescriptionLibraryName");
    addChar(24, "unitOfWorkID");
    addChar(8, "modeName");
    addChar(10, "inquiryMessageReply");
    addChar(10, "loggingOfCLPrograms");
    addChar(10, "breakMessageHandling");
    addChar(10, "statusMessageHandling");
    addChar(13, "deviceRecoveryAction");
    addChar(10, "DDMConversationHandling");
    addChar(1, "dateSeparator");
    addChar(4, "dateFormat");
    addChar(30, "printText");
    addChar(10, "submittersJobName");
    addChar(10, "submittersUserName");
    addChar(6, "submittersJobNumber");
    addChar(10, "submittersMessageQueueName");
    addChar(10, "submittersMessageQueueLibraryName");
    addChar(1, "timeSeparator");
    addBin4("codedCharacterSetID");
    addFieldDescription(new HexFieldDescription(new AS400ByteArray(8), "dateAndTimeJobIsScheduledToRun"));
    addChar(10, "printKeyFormat");
    addChar(10, "sortSequence");
    addChar(10, "sortSequenceLibrary");
    addChar(3, "languageID");
    addChar(2, "countryID");
    addChar(1, "completionStatus");
    addChar(1, "signedOnJob");
    addChar(8, "jobSwitches");
    addChar(10, "jobMessageQueueFullAction");
    addChar(1, "reserved");
    addBin4("jobMessageQueueMaximumSize");
    addBin4("defaultCodedCharacterSetIdentifier");
    addChar(80, "routingData");
    addChar(1, "decimalFormat");
    addChar(10, "characterIdentifierControl");
    addChar(30, "serverType");
  }
}  
