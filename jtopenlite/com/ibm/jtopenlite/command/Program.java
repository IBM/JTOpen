///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  Program.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command;

// Unnecessary import
// import com.ibm.jtopenlite.*;

/**
 * Used by classes that wish to implement a program call, this essentially represents a System i program (PGM).
 * The {@link CommandConnection#call(Program) CommandConnection.call()} method will internally call the methods
 * on this interface when it needs information about the Program being called.
 * <p></p>
 * The current order of operations (subject to change) that CommandConnection uses when call(Program) is invoked is as follows:
 * <ol>
 * <li>CommandConnection.call(program)</li>
 * <li>--> program.newCall()</li>
 * <li>--> program.getNumberOfParameters()</li>
 * <li>--> begin loop</li>
 * <li>------> program.getParameterInputLength()</li>
 * <li>--> end loop</li>
 * <li>--> program.getProgramName()</li>
 * <li>--> program.getProgramLibrary()</li>
 * <li>--> begin loop</li>
 * <li>------> program.getParameterInputLength()</li>
 * <li>------> program.getParameterOutputLength()</li>
 * <li>------> program.getParameterType()</li>
 * <li>------> program.getParameterInputData()</li>
 * <li>--> end loop</li>
 * <li>--> program.getNumberOfParameters()</li>
 * <li>--> begin loop</li>
 * <li>------> program.getParameterOutputLength()</li>
 * <li>------> program.getTempDataBuffer()</li>
 * <li>------> program.setParameterOutputData()</li>
 * <li>--> end loop</li>
 * </ol>
 * @see CommandConnection#call(Program)
**/
public interface Program
{
  /**
   * Invoked before any other methods on this interface by CommandConnection whenever this Program is called.
  **/
  public void newCall();

  /**
   * Returns the number of parameters for this program.
  **/
  public int getNumberOfParameters();

  /**
   * Returns the input length of the parameter at the specified index.
  **/
  public int getParameterInputLength(int parmIndex);

  /**
   * Returns the output length of the parameter at the specified index.
  **/
  public int getParameterOutputLength(int parmIndex);

  /**
   * Returns the type of parameter at the specified index.
   * @see Parameter
  **/
  public int getParameterType(int parmIndex);

  /**
   * Returns the input data of the parameter at the specified index.
  **/
  public byte[] getParameterInputData(int parmIndex);

  /**
   * The implementor can create their own temp byte array for the output parameter size and reuse it each time a call is performed,
   * or for more than one parameter on the same call.
   * The implementor can choose to ignore this, and simply return null. The command connection checks to see if the
   * buffer returned by this method is not null and large enough to accommodate the output parameter size.
  **/
  public byte[] getTempDataBuffer();

  /**
   * Sets the output data for the parameter at the specified index.
  **/
  public void setParameterOutputData(int parmIndex, byte[] tempData, int maxLength);

  /**
   * Returns the name of the program object.
  **/
  public String getProgramName();

  /**
   * Returns the library of the program object.
  **/
  public String getProgramLibrary();
}

