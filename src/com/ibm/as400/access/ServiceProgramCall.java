///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ServiceProgramCall.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.IOException;

/**
 The ServiceProgramCall class allows a user to call an AS/400 service program, passing data via input parameters, then accessing data returned via output parameters.  ProgramParameter objects are used to pass data between the Java program and the AS/400 service program.
 <P>ServiceProgramCall subclasses ProgramCall.  Much of the setup to call the service program is done via methods inherited from ProgramCall.  For example setSystem() and getSystem() are methods inherited from ProgramCall.
 <P>Limitations of this class:
 <UL>
 <LI>The service program must be on an AS/400 running OS/400 V4R4 or later.
 <LI>Up to seven parameters can be passed to the service program.
 <LI>The return value must be void or numeric.  This class does not support calling service programs that return a pointer.
 <LI>Parameters can be "pass by reference" or "pass by value".  When pass by reference, the data is copied from Java storage to AS/400 storage, then a pointer to the AS/400 storage is passed to the service program.
 </UL>
 <P>The name of the service program to call is the fully qualified name in the AS/400's integrated file system.  The extension is ".SRVPGM".  For example, to call MySrvPgm in MyLib, the program name is /QSYS.LIB/MYLIB.LIB/MYSRVPGM.SRVPGM.
 <p>The following example calls procedure int_int in service program ENTRYPTS in library MYPGM.  The procedure takes one input parameter, an integer, and returns an integer.
 <pre>
    // Construct the parameter list.  It contains the single parameter to the service program.
    ProgramParameter[] parameterList = new ProgramParameter[1];
    // Create the input parameter.  In this case we are sending the number 9 to the service program.
    AS400Bin4 bin4 = new AS400Bin4();
    byte[] parm = bin4.toBytes(9);
    parameterList[0] = new ProgramParameter(parm);
    // Construct the AS/400 object.  The service program is on this AS/400.
    AS400 as400 = new AS400("mySystem");
    // Construct the ServiceProgramCall object.
    ServiceProgramCall sPGMCall = new ServiceProgramCall(as400);
    // Set the fully qualified service program and the parameter list.
    sPGMCall.setProgram("/QSYS.LIB/MYPGM.LIB/ENTRYPTS.SRVPGM", parameterList);
    // Set the procedure to call in the service program.
    sPGMCall.setProcedureName("int_int");
    // Set the format of returned value.  The program we call returns an integer.
    sPGMCall.setReturnValueFormat(ServiceProgramCall.RETURN_INTEGER);
    // Call the service program.  If true is returned the program was successfully called.  If false is returned the program could not be started.  A list of messages is returned when the program cannot be started.
    if (sPGMCall.run() != true)
    {
        // Get the error messages when the call fails.
        AS400Message[] messageList = sPGMCall.getMessageList();
        for (int msg = 0; msg < messageList.length; ++msg)
        {
            System.out.println(messageList[msg].toString());
        }
    }
    else
    {
        // Get the returned value when the call is successful.
        int i = bin4.toInt(sPGMCall.getReturnValue());
        System.out.println("Result is: " + i);
    }
 </pre>

 @see ProgramParameter
 @see ProgramCall
 **/
public class ServiceProgramCall extends ProgramCall
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    /**
     Constant indicating the service program returns void.
     **/
    public static final int NO_RETURN_VALUE = 0;

    /**
     Constant indicating the service program returns an integer.
     **/
    public static final int RETURN_INTEGER  = 1;

    // Constant indicating the service program returns an integer and an error number.  Internally we will always use this constant instead of RETURN_INTEGER.  If the program is return-int, the errno will be 0 so returning both will still work.
    private static final int RETURN_INTEGER_AND_ERRNO  = 3;

    // The variable represents the name of calling procedure.
    private String procedureName_ = "";

    // The variable represents the returned value after calling the procedure is successful.
    private byte[] returnValue_ = null;

    // The variable represents the format of returned value.
    private int returnValueFormat_ = NO_RETURN_VALUE ;

    /**
     Constructs a ServiceProgramCall object.  A default ServiceProgramCall object is created.  The <i>system</i>, <i>program name</i>, <i>procedure name</i> and <i>parameters</i>, must be set before calling the program.
     **/
    public ServiceProgramCall()
    {
        super();
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing ServiceProgramCall object.");
    }

    /**
     Constructs a ServiceProgramCall object.  A ServiceProgramCall object representing the program on <i>system</i> is created.  The <i>program name</i>, <i>procedure name</i> and <i>parameters</i>, must be set before calling the program.
     @param  system  The AS/400 that contains the program.
     **/
    public ServiceProgramCall(AS400 system)
    {
        super(system);
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing ServiceProgramCall object.");
    }

    /**
     Constructs a ServiceProgramCall object.  A ServiceProgramCall object representing the program on <i>system</i> with name <i>serviceProgram</i> and parameters <i>parameterList</i> created.  The service program's <i>procedure name</i> must be set before calling the program.
     @param  system  The AS/400 which contains the program.
     @param  serviceProgram  The service program name as a fully qualified name in the integrated file system.
     @param  parameterList  A list of up to 7 parameters with which to call the program.
     **/
    public ServiceProgramCall(AS400 system, String serviceProgram, ProgramParameter[] parameterList)
    {
        super(system, serviceProgram, parameterList);
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing ServiceProgramCall object.");
    }

    /**
     Constructs a ServiceProgramCall object.  A ServiceProgramCall object representing the program on <i>system</i> with name <i>serviceProgram</i>, procedure name <i>procedureName</i>, and parameters <i>parameterList</i>, is created.
     @param  system  The AS/400 which contains the program.
     @param  serviceProgram  The program name as a fully qualified name in the integrated file system.
     @param  procedureName  The procedure in the service program to call.
     @param  parameterList  A list of up to 7 parameters with which to call the program.
     **/
    public ServiceProgramCall(AS400 system, String serviceProgram, String procedureName, ProgramParameter[] parameterList)
    {
        super(system, serviceProgram, parameterList);
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing ServiceProgramCall object, procedureName: " + procedureName);
        if (procedureName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'procedureName' is null.");
            throw new NullPointerException("procedureName");
        }
        procedureName_ = procedureName;
    }

    /**
     Constructs a ServiceProgramCall object.  A ServiceProgramCall object representing the program on <i>system</i> with name <i>serviceProgram</i>, procedure name <i>procedureName</i>, parameters <i>parameterList</i>, and returning a value as specified in <i>returnValueFormat</i>, is created.
     @param  system  The AS/400 which contains the program.
     @param  serviceProgram  The program name as a fully qualified name in the integrated file system.
     @param  procedureName  The procedure in the service program to call.
     @param  returnValueFormat  The format of the returned data. The value must be one of the following:
     <UL>
     <LI>NO_RETURN_VALUE  The procedure does not return a value.
     <LI>RETURN_INTEGER  The procedure returns an integer.
     </UL>
     @param  parameterList  A list of up to 7 parameters with which to call the program.
     **/
    public ServiceProgramCall(AS400 system, String serviceProgram, String procedureName, int returnValueFormat, ProgramParameter[] parameterList)
    {
        super(system, serviceProgram, parameterList);
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing ServiceProgramCall object, procedureName: " + procedureName + " return value format:", returnValueFormat);

        if (procedureName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'procedureName' is null.");
            throw new NullPointerException("procedureName");
        }
        if (returnValueFormat < NO_RETURN_VALUE || returnValueFormat > RETURN_INTEGER)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'returnValueFormat' is not valid:", returnValueFormat);
            throw new ExtendedIllegalArgumentException("returnValueFormat (" + returnValueFormat + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        procedureName_ = procedureName;
        returnValueFormat_ = returnValueFormat;
    }

    /**
     Returns the error number (errno).  If the service program returns an integer and an errno, use this method to retrieve the errno.  Zero is returned if the service program returns an integer but no errno.
     @return  The return data.
     **/
    public int getErrno()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting error number.");
        if (returnValue_ == null || returnValueFormat_ != RETURN_INTEGER)
        {
            Trace.log(Trace.ERROR, "Attempt to get error number before running service program.");
            throw new ExtendedIllegalStateException("returnValueFormat", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        if (returnValue_.length > 7)
        {
            return BinaryConverter.byteArrayToInt(returnValue_, 4);
        }
        return 0;
    }

    /**
     Returns the return data when the service program returns an integer.
     @return  The return data.
     **/
    public int getIntegerReturnValue()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting integer return value.");
        if (returnValue_ == null || returnValueFormat_ != RETURN_INTEGER)
        {
            Trace.log(Trace.ERROR, "Attempt to get integer return value before running service program.");
            throw new ExtendedIllegalStateException("returnValueFormat", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }
        return BinaryConverter.byteArrayToInt(returnValue_, 0);
    }

    /**
     Returns the service program procedure to be called.  If the name has not been set, an empty string ("") is returned.
     @return  The service program procedure to be called.
     **/
    public String getProcedureName()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting procedure name: " + procedureName_);
        return procedureName_;
    }

    /**
     Returns the data returned from the service program.  The data is returned as a byte array.  If no data is returned or if the service program has not yet been called, null is returned.
     @return  The data as a byte array.
     **/
    public byte[] getReturnValue()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting return value.");
        return returnValue_;
    }

    /**
     Returns the format of the returned data.
     @return  The format of the returned data.
     **/
    public int getReturnValueFormat()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting return value format:", returnValueFormat_);
        return returnValueFormat_;
    }

    /**
     Calls the service program.
     @return  true if the call is successful, false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     **/
    public boolean run() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        String program = getProgram();
        if (Trace.isTraceOn()) Trace.log(Trace.INFORMATION, "Running program: " + program + " procedure name: " + procedureName_);
        if (program.length() == 0)
        {
            Trace.log(Trace.ERROR, "Attempt to run before setting program.");
            throw new ExtendedIllegalStateException("program", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }
        if (procedureName_.length() == 0)
        {
            Trace.log(Trace.ERROR, "Attempt to run before setting procedure name.");
            throw new ExtendedIllegalStateException("procedureName", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }

        chooseImpl();

        int rvf = returnValueFormat_;
        if (rvf == RETURN_INTEGER)
        {
            rvf = RETURN_INTEGER_AND_ERRNO;
        }

        Object[] returnVal = impl_.runServiceProgram(program, procedureName_, rvf, getParameterList());
        messageList_ = impl_.getMessageList();

        fireActionCompleted();  // notify listeners
        if (returnVal[0].equals("false"))
        {
            return false;
        }
        else
        {
            returnValue_ = (byte[])returnVal[1];
            return true;
        }
    }

    /**
     Calls the service program.  Calls the specified service program with the specified parameters.  The system object and service program procedure name must be set before calling this method.
     @param  serviceProgram  The program name as a fully qualified name in the integrated file system.
     @param  parameterList  A list of up to 7 parameters with which to call the program.
     @return  true if the call is successful, false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     @exception  PropertyVetoException  If a change for a property is vetoed.
     **/
    public boolean run(String serviceProgram, ProgramParameter[] parameterList) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException, PropertyVetoException
    {
        setProgram(serviceProgram, parameterList);
        return run();
    }

    /**
     Calls the service program.
     @param  system  The AS/400 which contains the program.
     @param  serviceProgram  The program name as a fully qualified name in the integrated file system.
     @param  procedureName  The procedure in the service program to call.
     @param  returnValueFormat  The format of the returned data. The value must be one of the following:
     <UL>
     <LI>NO_RETURN_VALUE  The procedure does not return a value.
     <LI>RETURN_INTEGER  The procedure returns an integer.
     </UL>
     @param  parameterList  A list of up to 7 parameters with which to call the program.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     @exception  PropertyVetoException If a change for a property is vetoed.
     **/
    public boolean run(AS400 system, String serviceProgram, String procedureName, int returnValueFormat, ProgramParameter[] parameterList) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException, PropertyVetoException
    {
        setSystem(system);
        setProgram(serviceProgram);
        setProcedureName(procedureName);
        setReturnValueFormat(returnValueFormat);
        setParameterList(parameterList);
        return run();
    }

    /**
     Sets the service program procedure to call.
     @param  procedureName  The procedure in the service program to call.
     @exception  PropertyVetoException  If a change for the value of procedureName is vetoed.
     **/
    public void setProcedureName(String procedureName) throws PropertyVetoException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Setting procedure name: " + procedureName);
        if (procedureName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'procedureName' is null.");
            throw new NullPointerException("procedureName");
        }
        String old = procedureName_;
        vetoableChangeListeners_.fireVetoableChange("procedureName", old, procedureName);
        procedureName_ = procedureName;
        propertyChangeListeners_.firePropertyChange("procedureName", old, procedureName);
    }

    /**
     Sets the format of the returned data.
     @param  returnValueFormat  The format of the returned data. The value must be one of the following:
     <UL>
     <LI>NO_RETURN_VALUE  The procedure does not return a value.
     <LI>RETURN_INTEGER  The procedure returns an integer.
     </UL>
     @exception  PropertyVetoException  If a change for the value of returnValueFormat is vetoed.
     **/
    public void setReturnValueFormat(int returnValueFormat) throws PropertyVetoException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Setting return value format:", returnValueFormat);
        if (returnValueFormat < NO_RETURN_VALUE || returnValueFormat > RETURN_INTEGER)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'returnValueFormat' is not valid:", returnValueFormat);
            throw new ExtendedIllegalArgumentException("returnValueFormat (" + returnValueFormat + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        Integer oldValue = new Integer(returnValueFormat_);
        Integer newValue = new Integer(returnValueFormat);
        vetoableChangeListeners_.fireVetoableChange("returnValueFormat", oldValue, newValue);
        returnValueFormat_ = returnValueFormat;
        propertyChangeListeners_.firePropertyChange("returnValueFormat", oldValue, newValue);
    }
}
