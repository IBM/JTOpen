///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400Exception.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
   The AS400Exception class represents an exception
   that indicates that an error has occurred on the AS/400
   system.  One or more <A HREF="AS400Message.html">AS400Message</A> objects are
   included in this exception.
**/
public class AS400Exception extends ErrorCompletingRequestException
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    AS400Message[] msgList_;

    /**
       Constructs an AS400Exception object.

       @param  msgList  The list of <A HREF="AS400Message.html">AS400Message</A>s
                        causing this exception.
    **/
    public AS400Exception(AS400Message[] msgList) // @D0C
    {
        super(ErrorCompletingRequestException.AS400_ERROR,
              msgList[0].getID() + " " + msgList[0].getText() );
        msgList_ = msgList;
    }

    /**
       Constructs an AS400Exception object. It includes
       a single message.

       @param  message  The <A HREF="AS400Message.html">AS400Message</A>
                        causing this exception.
    **/
    AS400Exception(AS400Message message)
    {
        super(ErrorCompletingRequestException.AS400_ERROR,
              message.getID() + " " + message.getText()  );
        msgList_ = new AS400Message[1];
        msgList_[0] = message;
    }

    /**
       Returns the <A HREF="AS400Message.html">AS400Message</A> causing this exception.

       @return The AS400Message causing this exception.
    **/
    public AS400Message getAS400Message()
    {
        return msgList_[0];
    }

    /**
      Returns the list of <A HREF="AS400Message.html">AS400Message</A>s causing this exception.

      @return The list of AS400Messages causing this exception.
    */
    public AS400Message[] getAS400MessageList()
    {
        return msgList_;
    }


  }
