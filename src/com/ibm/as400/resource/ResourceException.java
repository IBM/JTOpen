///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.InternalErrorException;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.ReturnCodeException;
import com.ibm.as400.access.Trace;
import com.ibm.as400.data.PcmlException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ResourceBundle;



/**
The ResourceException class represents an exception that
occurs when using a
{@link com.ibm.as400.resource.Resource Resource} or
{@link com.ibm.as400.resource.ResourceList ResourceList}.
Most of the time, this exception is thrown as the result
of another exception being caught.  In this case, the underlying
exception is available here.

<p>In some cases, the exception is expected by the user.  For
example, if the user explicitly canceled an operation,
then the user expects the resulting exception.  The default
is that the exception is not expected by the user.
**/
//
// Design notes:
//
// 1. The isExpected() method came from a request to determine
//    when the exception occurred because the user canceled
//    the signon.  In Operations Navigator today, the user gets
//    an annoying error message when they cancel.  Rather
//    than externalize the concept of "sign on" here, I have
//    generalized the concept to the notion of an expected
//    exception.
//
public class ResourceException
extends Exception
implements ReturnCodeException
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    // Private data.
    private static ResourceBundle   resourceBundle_ = ResourceBundle.getBundle("com.ibm.as400.resource.ResourceMRI");

    private Throwable               exception_      = null;
    private boolean                 expected_       = false;
    private AS400Message[]          messageList_    = null;
    private int                     returnCode_     = UNKNOWN_ERROR;



/**
The return code indicating that resource attributes were not returned.
**/
    public static final int ATTRIBUTES_NOT_RETURNED     = 1;



/**
The return code indicating that resource attributes were not set.
**/
    public static final int ATTRIBUTES_NOT_SET          = 2;



/**
The return code indicating that AS400Message's were returned.
**/
    public static final int MESSAGES_RETURNED           = 3;



/**
The return code indicating that an operation failed.
**/
    public static final int OPERATION_FAILED            = 4;



/**
The return code indicating that an operation is not supported.
**/
    public static final int OPERATION_NOT_SUPPORTED     = 5;



/**
The return code indicating that an unknown problem has occurred.
**/
    public static final int UNKNOWN_ERROR               = 6;



/**
The return code indicating that user has insuffient authority.
**/
    public static final int AUTHORITY_INSUFFICIENT      = 7;



/**
The return code indicating that the resource attribute is read-only.
**/
    public static final int ATTRIBUTE_READ_ONLY         = 8;



/**
Constructs a ResourceException object.
**/
    public ResourceException()
    {
        super();
    }



/**
Constructs a ResourceException object.

@param returnCode   The return code.
**/
    public ResourceException(int returnCode)
    {
        super(buildMessage(returnCode, null));
        returnCode_ = returnCode;
    }



/**
Constructs a ResourceException object.

@param exception The underlying exception.
**/
    public ResourceException(Throwable exception)
    {
        super((exception instanceof PcmlException) ? ((PcmlException)exception).getLocalizedMessage() : exception.getMessage());

//@B0D        if (exception instanceof PcmlException) {
//@B0D            exception_ = ((PcmlException)exception).getException();
//@B0D            if (exception_ == null)
//@B0D                exception_ = exception;
//@B0D        }
//@B0D        else
        exception_ = exception;

        if (Trace.isTraceOn())
            Trace.log(Trace.ERROR, "ResourceException was thrown", exception);
    }



/**
Constructs a ResourceException object.

@param messageList  The message list.
**/
    public ResourceException(AS400Message[] messageList)
    {
        super(buildMessage(MESSAGES_RETURNED, messageList));
        returnCode_ = MESSAGES_RETURNED;
        messageList_ = messageList;

        if (Trace.isTraceOn()) {
            for(int i = 0; i < messageList.length; ++i)
                Trace.log(Trace.ERROR, messageList[i].toString());
        }
    }



/**
Constructs a ResourceException object.

@param returnCode   The return code.
@param exception    The underlying exception.
**/
    public ResourceException(int returnCode, Throwable exception)
    {
        super(buildMessage(returnCode, null));
        exception_ = exception;
        returnCode_ = returnCode;

        if (Trace.isTraceOn())
            Trace.log(Trace.ERROR, "ResourceException was thrown", exception);
    }



/**
Constructs a ResourceException object.

@param returnCode   The return code.
@param exception    The underlying exception.
@param expected     true if the exception is expected by the user,
                    false otherwise.
**/
    public ResourceException(int returnCode, Throwable exception, boolean expected)
    {
        super(buildMessage(returnCode, null));
        exception_ = exception;
        expected_   = expected;
        returnCode_ = returnCode;
    }



/**
Builds the message based on the return code.

@param returnCode   The return code.
@param messageList  The message list, or null  if none.
@return             The message.
**/
    private static String buildMessage(int returnCode, AS400Message[] messageList)
    {
        String mriKey;
        switch(returnCode) {
        case ATTRIBUTES_NOT_RETURNED:
            mriKey = "EXC_ATTRIBUTES_NOT_RETURNED";
            break;
        case ATTRIBUTES_NOT_SET:
            mriKey = "EXC_ATTRIBUTES_NOT_SET";
            break;
        case MESSAGES_RETURNED:
            if (messageList != null)
                if (messageList.length > 0) {
                    return messageList[0].getText();
                }
            mriKey = "EXC_MESSAGES_RETURNED";
            break;
        case OPERATION_FAILED:
            mriKey = "EXC_OPERATION_FAILED";
            break;
        case OPERATION_NOT_SUPPORTED:
            mriKey = "EXC_OPERATION_NOT_SUPPORTED";
            break;
        case AUTHORITY_INSUFFICIENT:
            mriKey = "EXC_AUTHORITY_INSUFFICIENT";
            break;
        case ATTRIBUTE_READ_ONLY:
            mriKey = "EXC_ATTRIBUTE_READ_ONLY";
            break;
        case UNKNOWN_ERROR:
        default:
            mriKey = "EXC_UNKNOWN_ERROR";
            break;
        }

        return resourceBundle_.getString(mriKey);
    }



/**
Returns the underlying exception, if any.

@return The underlying exception, or null if there is none.
**/
    public Throwable getException()
    {
        return exception_;
    }



/**
Returns the message list, if any.

@return The message list, or null if there is none.
**/
    public AS400Message[] getMessageList()
    {
        if (exception_ instanceof ResourceException)
            return ((ResourceException)exception_).getMessageList();
        else
            return messageList_;
    }



/**
Returns the return code.

@return The return code.
**/
    public int getReturnCode()
    {
        if (exception_ instanceof ResourceException)
            return ((ResourceException)exception_).getReturnCode();
        else
            return returnCode_;
    }



/**
Indicates if the exception is expected by the user.  For
example, if the user explicitly canceled an operation,
then the user expects the resulting exception.
**/
    public boolean isExpected()
    {
        return expected_;
    }



/**
Unwraps the underlying exception and throws it.

@exception AS400SecurityException          If a security or authority error occurs.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the server.
@exception ObjectDoesNotExistException     If the server object does not exist.
**/
    public void unwrap()
        throws  AS400SecurityException,
                ErrorCompletingRequestException,
                InterruptedException,
                IOException,
                ObjectDoesNotExistException
    {
        //@B0C - Changed this method.

        Throwable x = exception_;
        while (x != null && x instanceof PcmlException)
        {
          x = ((PcmlException)x).getException();
        }
        if (x == null) x = exception_; // Nothing else we can do here.

        if (x instanceof AS400SecurityException) throw (AS400SecurityException)x;
        if (x instanceof ErrorCompletingRequestException) throw (ErrorCompletingRequestException)x;
        if (x instanceof InterruptedException) throw (InterruptedException)x;
        if (x instanceof IOException) throw (IOException)x;
        if (x instanceof ObjectDoesNotExistException) throw (ObjectDoesNotExistException)x;
        if (x instanceof ResourceException) ((ResourceException)exception_).unwrap();
        if (x instanceof RuntimeException) throw (RuntimeException)x;
        if (messageList_ != null) throw new AS400Exception(messageList_);
        throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
    }



}
