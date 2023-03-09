///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  CallServiceProgramParameterFormat.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2014 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program;

/**
 * Interface used to utilize the parameters of a service program call. 
 * See the implementing subclasses for examples. 
 */
public interface CallServiceProgramParameterFormat
{
  public static final int PARAMETER_FORMAT_BY_VALUE = 1;
  public static final int PARAMETER_FORMAT_BY_REFERENCE = 2;

  /**
   * This method is called to get the number of parameters used
   * by the service program procedure call. 
   * @return parameterCount
   */
  public int getParameterCount();

  /**
   * This method is used to obtain the length of the specified parameter
   * 
   * @param index  0-based parameter identifier.
   * @return parameter length
   */
  public int getParameterLength(int index);

  /**
   * This method is used to obtain the format of the specified parameter
   * 
   * @param index  0-based parameter identifier.
   * @return parameter format which is one of the following:  
   * PARAMETER_FORMAT_BY_VALUE
   * PARAMETER_FORMAT_BY_REFERENCE
   */
  public int getParameterFormat(int index);

  /**
   * This method is used to fill an output buffer with the parameter information
   * before the procedure is called. 
   * @param index 0-based parameter identifier
   * @param dataBuffer  buffer containing the data
   * @param offset  offset to where the data should be placed
   */
  public void fillInputData(int index, byte[] dataBuffer, int offset);

  /**
   * This method is used to set the internal value of the parameter from a dataBuffer
   * @param index 0-based parameter identifier
   * @param dataBuffer buffer containing the data
   * @param offset offset to where the data should be retrieved
   */
  public void setOutputData(int index, byte[] dataBuffer, int offset);
}

