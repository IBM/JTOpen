///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ConnectionPoolException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
*  The ConnectionPoolException class represents an exception 
*  which indicates that a problem occurred with the connection pool.
**/
public class ConnectionPoolException extends Exception
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";



  static final long serialVersionUID = 4L;


  private Exception exception_;
  private int returnCode_     = UNKNOWN_ERROR;  //@A1A

  /**
   * The return code indicating that the minimum and maximum pool sizes conflict.
  **/
  public static final int CONFLICTING_POOL_SIZES = 3;    //@B0A

  /**
  The return code indicating that max connection limit has been reached.
  **/
  public static final int MAX_CONNECTIONS_REACHED     = 1;    //@A1A

  /**
  The return code indicating that an unknown error occured.
  **/
  static final int UNKNOWN_ERROR    = 2; //@A1A

  /**
  *  Constructs a default ConnectionPoolException object.
  **/
  ConnectionPoolException()
  {
    super();
  }

  /**
  *  Constructs a ConnectionPoolException object.
  *  @param exception The exception.
  **/
  ConnectionPoolException(Exception exception)
  {
    super(exception.getMessage());
    exception_ = exception;
  }

  //@A1A
  /**
  * Constructs a ConnectionPoolException object.
  * @param returnCode   The return code.
  **/
  ConnectionPoolException(int returnCode)
  {
    super(ResourceBundleLoader.getText(getMRIKey(returnCode)));
    returnCode_ = returnCode;
  }


  //@A1A
  /**
  * Returns the text associated with the return code.
  * @param  returnCode  The return code associated with this exception.
  * @return  The text string which describes the error.
  **/
  static String getMRIKey(int returnCode)  // This method is required so the message can be created and sent in super()
  {
    switch (returnCode)
    {
      case MAX_CONNECTIONS_REACHED:
        return "EXC_MAX_CONN_REACHED";
      case CONFLICTING_POOL_SIZES: //@B0A
        return "EXC_CONFLICT_POOL_SIZES"; //@B0A
      default:
        return "EXC_UNKNOWN";   // Bad return code was provided.
    }
  }

  /**
  *  Returns the original exception.
  *  @return The exception.
  **/
  public Exception getException()
  {
    return exception_;
  }

  /**
  * Returns the return code.
  * @return The return code.
  **/
  public int getReturnCode()
  {
    return returnCode_;
  }

}
