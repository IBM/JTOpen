///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ServiceProgramCall.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1998-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.IOException;

/**
 Allows a user to call an IBM i service program.  Input data is passed via input parameters, and output data is accessed via output parameters.  ProgramParameter objects are used to pass data between the Java program and the service program.
 <p>Limitations of this class:
 <ul>
 <li>The service program must be on an IBM i system running V4R4 or later.
 <li>Up to seven parameters can be passed to the service program.
 <li>The return value must be void or numeric.  This class does not support calling service programs that return a pointer.
 <li>Parameters can be "pass by reference" or "pass by value".
 <ul>
 <li>When pass by reference, the data is copied from Java storage to system storage, then a pointer to the system storage is passed to the service program.
 <li>Up to four bytes can be passed by value.  Parameters longer than four bytes must be passed by reference which may require a change to the service program.
 </ul>
 </ul>
 <p>The name of the service program to call is the fully qualified name in the integrated file system.  The extension is ".SRVPGM".  For example, to call MySrvPgm in MyLib, the program name is /QSYS.LIB/MYLIB.LIB/MYSRVPGM.SRVPGM.
 <p>Service program entry point notes:
 <ul>
 <li>The service program entry point to call is supplied by the Java program.  The entry point name is <b>case sensitive</b>.  If the run() method fails with the message "CPF226E - Value for a parameter was not valid.", there is a good chance the entry point name is incorrect.
 <li>The service program entry point name is converted from a Java String to an array of EBCDIC bytes before being sent to the system.  <b>By default this conversion is performed using CCSID 37</b>, not the job CCSID which Toolbox classes usually use for conversion.  This is because the entry point name is set when the service program is built.  CCSID 37 is the default because most IBM supplied service programs set the entry point name based on CCSID 37.  A setProcedureName() method exists which lets you override the default.
 </ul>
 <p>The following example calls procedure int_int in service program ENTRYPTS in library MYPGM.  The procedure takes one input parameter, an integer, and returns an integer.
 <pre>
 *    // Create a single parameter parameter list.
 *    ProgramParameter[] parameterList = new ProgramParameter[1];
 *
 *    // Create the input parameter.  We are sending the number 9 to the service program.
 *    AS400Bin4 bin4 = new AS400Bin4();
 *    byte[] parameter = bin4.toBytes(9);
 *    parameterList[0] = new ProgramParameter(parameter);
 *
 *    // Construct the system object.  The service program is on this system.
 *    AS400 system = new AS400("mySystem");
 *
 *    // Construct the ServiceProgramCall object.
 *    ServiceProgramCall sPGMCall = new ServiceProgramCall(system);
 *
 *    // Set the fully qualified service program and the parameter list.
 *    sPGMCall.setProgram("/QSYS.LIB/MYPGM.LIB/ENTRYPTS.SRVPGM", parameterList);
 *
 *    // Set the procedure to call in the service program.
 *    sPGMCall.setProcedureName("int_int");
 *
 *    // Set the format of returned value.  The program we call returns an integer.
 *    sPGMCall.setReturnValueFormat(ServiceProgramCall.RETURN_INTEGER);
 *
 *    // Call the service program.
 *    if (sPGMCall.run() != true)
 *    {
 *        // Get the error messages when the call fails.
 *        AS400Message[] messageList = sPGMCall.getMessageList();
 *        for (int i = 0; i < messageList.length; ++i)
 *        {
 *            System.out.println(messageList[i].getText());
 *        }
 *    }
 *    else
 *    {
 *        // Get the returned value when the call is successful.
 *        int i = bin4.toInt(sPGMCall.getReturnValue());
 *        System.out.println("Result is: " + i);
 *    }
 </pre>
 **/
public class ServiceProgramCall extends ProgramCall
{
    static final long serialVersionUID = 4L;

    /**
     Constant indicating the service program returns void.
     **/
    public static final int NO_RETURN_VALUE = 0;

    /**
     Constant indicating the service program returns an integer.
     **/
    public static final int RETURN_INTEGER = 1;

    // Constant indicating the service program returns an integer and an error number.  Internally we will always use this constant instead of RETURN_INTEGER.  If the program is return-int, the errno will be 0 so returning both will still work.
    static final int RETURN_INTEGER_AND_ERRNO = 3;

    // The variable represents the name of calling procedure.
    private String procedureName_ = "";

    // The variable represents the returned value after calling the procedure is successful.
    private byte[] returnValue_ = null;

    // The variable represents the format of returned value.
    private int returnValueFormat_ = NO_RETURN_VALUE ;

    // The ccsid of the entry point.  This is set at service program compile time.  For all Rochester delivered service programs this is 37 so that is the default.  The customer can override the default with the setProcedureName method.
    private int procedureNameCCSID_ = 37;

    // Whether to align the first parameter of the service program, on a 16-byte boundary.
    private boolean alignOn16Bytes_ = false;

    /**
     Constructs a ServiceProgramCall object.  A default ServiceProgramCall object is created.  The <i>system</i>, <i>program name</i>, <i>procedure name</i> and <i>parameters</i>, must be set before calling the program.
     **/
    public ServiceProgramCall()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing ServiceProgramCall object.");
    }

    /**
     Constructs a ServiceProgramCall object.  A ServiceProgramCall object representing the program on <i>system</i> is created.  The <i>program name</i>, <i>procedure name</i> and <i>parameters</i>, must be set before calling the program.
     @param  system  The system that contains the program.
     **/
    public ServiceProgramCall(AS400 system)
    {
        super(system);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing ServiceProgramCall object.");
    }

    /**
     Constructs a ServiceProgramCall object.  A ServiceProgramCall object representing the program on <i>system</i> with name <i>serviceProgram</i> and parameters <i>parameterList</i> created.  The service program's <i>procedure name</i> must be set before calling the program.
     @param  system  The system which contains the program.
     @param  serviceProgram  The service program name as a fully qualified name in the integrated file system.
     @param  parameterList  A list of up to 7 parameters with which to call the program.
     **/
    public ServiceProgramCall(AS400 system, String serviceProgram, ProgramParameter[] parameterList)
    {
        super(system, serviceProgram, parameterList);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing ServiceProgramCall object.");
    }

    /**
     Constructs a ServiceProgramCall object.  A ServiceProgramCall object representing the program on <i>system</i> with name <i>serviceProgram</i>, procedure name <i>procedureName</i>, and parameters <i>parameterList</i>, is created.
     @param  system  The system which contains the program.
     @param  serviceProgram  The program name as a fully qualified name in the integrated file system.
     @param  procedureName  The procedure in the service program to call.
     @param  parameterList  A list of up to 7 parameters with which to call the program.
     **/
    public ServiceProgramCall(AS400 system, String serviceProgram, String procedureName, ProgramParameter[] parameterList)
    {
        super(system, serviceProgram, parameterList);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing ServiceProgramCall object, procedureName: " + procedureName);
        if (procedureName == null) {
            throw new NullPointerException("procedureName");
        }
        procedureName_ = procedureName;
    }

    /**
     Constructs a ServiceProgramCall object.  A ServiceProgramCall object representing the program on <i>system</i> with name <i>serviceProgram</i>, procedure name <i>procedureName</i>, parameters <i>parameterList</i>, and returning a value as specified in <i>returnValueFormat</i>, is created.
     @param  system  The system which contains the program.
     @param  serviceProgram  The program name as a fully qualified name in the integrated file system.
     @param  procedureName  The procedure in the service program to call.
     @param  returnValueFormat  The format of the returned data.  The value must be one of the following:
     <ul>
     <li>NO_RETURN_VALUE  The procedure does not return a value.
     <li>RETURN_INTEGER  The procedure returns an integer.
     </ul>
     @param  parameterList  A list of up to 7 parameters with which to call the program.
     **/
    public ServiceProgramCall(AS400 system, String serviceProgram, String procedureName, int returnValueFormat, ProgramParameter[] parameterList)
    {
        super(system, serviceProgram, parameterList);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing ServiceProgramCall object, procedureName: " + procedureName + " return value format:", returnValueFormat);

        if (procedureName == null) {
            throw new NullPointerException("procedureName");
        }
        if (returnValueFormat < NO_RETURN_VALUE || returnValueFormat > RETURN_INTEGER)
        {
            throw new ExtendedIllegalArgumentException("returnValueFormat (" + returnValueFormat + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        procedureName_ = procedureName;
        returnValueFormat_ = returnValueFormat;
    }

    /**
     Returns the error number (errno).  If the service program returns an integer and an errno, use this method to retrieve the errno.  Zero is returned if the service program returns an integer but no errno.
     <p>The errno is valid only when the return code is non-zero.  Service programs are not required to reset the errno each time the API is called, so the errno may not be reset from a previous call.  Suppose, for example, calling an entry point the first time fails so the return code and errno are both non-zero.  If the next call works, the return code will be zero but the errno may have the non-zero value from the previous call of the entry point.  Checking only the errno would indicate the second call failed when it actually worked.  Call this method only when a call to getIntegerReturnValue returns a non-zero value.
     @return  The return data.
     **/
    public int getErrno()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting error number.");
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
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting integer return value.");
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
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting procedure name: " + procedureName_);
        return procedureName_;
    }

    /**
     Returns the data returned from the service program.  The data is returned as a byte array.  If no data is returned or if the service program has not yet been called, null is returned.
     @return  The data as a byte array.
     **/
    public byte[] getReturnValue()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting return value:", returnValue_);
        return returnValue_;
    }

    /**
     Returns the format of the returned data.
     @return  The format of the returned data.
     **/
    public int getReturnValueFormat()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting return value format:", returnValueFormat_);
        return returnValueFormat_;
    }

    /**
     Calls the service program.
     @return  true if the call is successful; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     **/
    public boolean run() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "Running service program: " + program_ + " procedure name: " + procedureName_);
        if (program_.length() == 0)
        {
            Trace.log(Trace.ERROR, "Attempt to run before setting program.");
            throw new ExtendedIllegalStateException("program", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }
        if (procedureName_.length() == 0)
        {
            Trace.log(Trace.ERROR, "Attempt to run before setting procedure name.");
            throw new ExtendedIllegalStateException("procedureName", ExtendedIllegalStateException.PROPERTY_NOT_SET );
        }
        if (parameterList_.length > 7)
        {
            Trace.log(Trace.ERROR, "Parameter list length exceeds limit of 7 parameters:", parameterList_.length);
            throw new ExtendedIllegalArgumentException("parameterList.length (" + parameterList_.length + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        chooseImpl();

        int rvf = returnValueFormat_;
        if (rvf == RETURN_INTEGER)
        {
            rvf = RETURN_INTEGER_AND_ERRNO;
        }

        // Run the service program.
        returnValue_ = impl_.runServiceProgram(library_, name_, procedureName_, rvf, parameterList_, threadSafetyValue_, procedureNameCCSID_, messageOption_, alignOn16Bytes_);

        // Retrieve the messages.
        messageList_ = impl_.getMessageList();
        // Set our system object into each of the messages.
        if (system_ != null)
        {
            for (int i = 0; i < messageList_.length; ++i)
            {
                messageList_[i].setSystem(system_);
            }
        }

        // The SRVPGM API we call will return an MCH3401 if the object or library do not exist.  We need to monitor the message list for that return code and throw an ObjectDoesNotExistException.  Unfortunately we do not know if it is the object or the library that does not exist.
        if (messageList_.length != 0)
        {
            if (messageList_[0].getID().startsWith("MCH3401"))
            {
                throw new ObjectDoesNotExistException(program_, ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
            }
        }

        // Fire action completed event.
        if (actionCompletedListeners_ != null) fireActionCompleted();
        return returnValue_ != null;
    }

    /**
     Calls the service program.  Calls the specified service program with the specified parameters.  The system and service program procedure name must be set before calling this method.
     @param  serviceProgram  The program name as a fully qualified name in the integrated file system.
     @param  parameterList  A list of up to 7 parameters with which to call the program.
     @return  true if the call is successful, false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     @exception  PropertyVetoException  If a change for a property is vetoed.
     **/
    public boolean run(String serviceProgram, ProgramParameter[] parameterList) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException, PropertyVetoException
    {
        setProgram(serviceProgram, parameterList);
        return run();
    }

    /**
     Calls the service program.
     @param  system  The system which contains the program.
     @param  serviceProgram  The program name as a fully qualified name in the integrated file system.
     @param  procedureName  The procedure in the service program to call.
     @param  returnValueFormat  The format of the returned data.  The value must be one of the following:
     <ul>
     <li>NO_RETURN_VALUE  The procedure does not return a value.
     <li>RETURN_INTEGER  The procedure returns an integer.
     </ul>
     @param  parameterList  A list of up to 7 parameters with which to call the program.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     @exception  PropertyVetoException  If a change for a property is vetoed.
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
     Sets whether to align the first parameter on a 16-byte boundary.
     Some service programs require that the "receiver variable" (typically the first parameter) be aligned on a 16-byte boundary.
     By default, no alignment is done.
     @param  align Whether to align the first parameter on a 16-byte boundary.
     **/
    public void setAlignOn16Bytes(boolean align)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting alignment: " + align);
        alignOn16Bytes_ = align;
    }

    /**
     Sets the service program procedure to call.
     @param  procedureName  The procedure in the service program to call.
     @exception  PropertyVetoException  If a change for the value of procedureName is vetoed.
     **/
    public void setProcedureName(String procedureName) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting procedure name: " + procedureName);
        if (procedureName == null) {
            throw new NullPointerException("procedureName");
        }
        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            procedureName_ = procedureName;
        }
        else
        {
            String oldValue = procedureName_;
            String newValue = procedureName;

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("procedureName", oldValue, newValue);
            }
            procedureName_ = procedureName;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("procedureName", oldValue, newValue);
            }
        }
    }

    /**
     Sets the service program procedure to call.
     @param  procedureName  The procedure in the service program to call.
     @param  procedureNameCCSID  The CCSID to use when converting the procedure name from a Java String to EBCDIC.
     @exception  PropertyVetoException  If a change for the value of procedureName is vetoed.
     **/
    public void setProcedureName(String procedureName, int procedureNameCCSID) throws PropertyVetoException
    {
        setProcedureName(procedureName);
        procedureNameCCSID_ = procedureNameCCSID;
    }

    /**
     Sets the path name of the service program.
     @param  serviceProgram  The fully qualified integrated file system path name to the service program.  The library and service program name must each be 10 characters or less.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setProgram(String serviceProgram) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting service program: " + serviceProgram);
        if (serviceProgram == null) {
            throw new NullPointerException("program");
        }
        // Verify serviceProgram is valid IFS path name.
        QSYSObjectPathName ifs = new QSYSObjectPathName(serviceProgram, "SRVPGM");

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            library_ = ifs.getLibraryName();
            name_ = ifs.getObjectName();
            program_ = serviceProgram;
        }
        else
        {
            String oldValue = program_;
            String newValue = serviceProgram;

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("program", oldValue, newValue);
            }
            library_ = ifs.getLibraryName();
            name_ = ifs.getObjectName();
            program_ = newValue;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("program", oldValue, newValue);
            }
        }
    }

    /**
     Sets the format of the returned data.
     @param  returnValueFormat  The format of the returned data.  The value must be one of the following:
     <ul>
     <li>NO_RETURN_VALUE  The procedure does not return a value.
     <li>RETURN_INTEGER  The procedure returns an integer.
     </ul>
     @exception  PropertyVetoException  If a change for the value of returnValueFormat is vetoed.
     **/
    public void setReturnValueFormat(int returnValueFormat) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting return value format:", returnValueFormat);
        if (returnValueFormat < NO_RETURN_VALUE || returnValueFormat > RETURN_INTEGER)
        {
            throw new ExtendedIllegalArgumentException("returnValueFormat (" + returnValueFormat + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            returnValueFormat_ = returnValueFormat;
        }
        else
        {
            Integer oldValue = new Integer(returnValueFormat_);
            Integer newValue = new Integer(returnValueFormat);

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("returnValueFormat", oldValue, newValue);
            }
            returnValueFormat_ = returnValueFormat;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("returnValueFormat", oldValue, newValue);
            }
        }
    }
}
