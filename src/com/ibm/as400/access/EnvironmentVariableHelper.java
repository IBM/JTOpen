///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: EnvironmentVariableHelper.java
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
import java.io.UnsupportedEncodingException;


/**
The EnvironmentVariableHelper class represents common code for
all the classes used in accessing environment variables.
**/
class EnvironmentVariableHelper
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




//-----------------------------------------------------------------------------------------
// Private data.
//-----------------------------------------------------------------------------------------

            static final int                RV_EFAULT_          = 3408;
            static final int                RV_EINVAL_          = 3021;
            static final int                RV_ENOENT_          = 3025;
            static final int                RV_ENOSPC_          = 3404;
            static final int                RV_EPERM_           = 3027;
            static final int                RV_EUNKNOWN_        = 3474;
    private static final String             SRVPGM_NAME_        = "/QSYS.LIB/QP0ZSYSE.SRVPGM";

    private ServiceProgramCall              spc_                = new ServiceProgramCall();
    private AS400                           system_             = null;




/**
Constructs an EnvironmentVariableHelper object.

@param system The system.
**/
    EnvironmentVariableHelper(AS400 system)
    throws IOException
    {
        system_         = system;
        spc_            = new ServiceProgramCall(system_);

        try {
            spc_.setProgram(SRVPGM_NAME_);
            spc_.setThreadSafe(true);
        }
        catch(PropertyVetoException ignore) {
            // Ignore.
        }
    }



/**
Calls the service program when an int return value is expected.

@param procedureName            The procedure name.
@param parameters               The parameters.
@param expectedReturnValue      An expected return value, or 0 if none.
@return returnValue             The return value, either 0 or the expected
                                return value.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception RequestNotSupportedException    If the requested function is not supported because the AS/400 system is not at the correct level.
**/
    int callServiceProgramInt(String procedureName, 
                              ProgramParameter[] parameters,
                              int expectedReturnValue)
    throws AS400SecurityException, 
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
    {
        return callServiceProgramInt(procedureName, parameters, expectedReturnValue, 0);
    }



/**
Calls the service program when an int return value is expected.

@param procedureName            The procedure name.
@param parameters               The parameters.
@param expectedReturnValue1     An expected return value, or 0 if none.
@param expectedReturnValue2     An expected return value, or 0 if none.
@return returnValue             The return value, either 0 or the expected
                                return value.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception RequestNotSupportedException    If the requested function is not supported because the AS/400 system is not at the correct level.
**/
    int callServiceProgramInt(String procedureName, 
                              ProgramParameter[] parameters,
                              int expectedReturnValue1,
                              int expectedReturnValue2)
    throws AS400SecurityException, 
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
    {
        synchronized(spc_) {
            try {
                if (Trace.isTraceOn())
                    Trace.log(Trace.INFORMATION, "Calling service program procedure " + procedureName + ".");

                spc_.setProcedureName(procedureName);
                spc_.setReturnValueFormat(ServiceProgramCall.RETURN_INTEGER);
                spc_.setParameterList(parameters);
                if (spc_.run()) {
                    int rv = spc_.getIntegerReturnValue();
                    if ((rv != 0) && (rv != expectedReturnValue1) && (rv != expectedReturnValue2)) {
                        if (Trace.isTraceOn())
                            Trace.log(Trace.ERROR, procedureName + " returned " + rv);
                        if (rv == RV_ENOENT_)
                            throw new ObjectDoesNotExistException(ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
                        if (rv == RV_EPERM_)
                            throw new AS400SecurityException(AS400SecurityException.SPECIAL_AUTHORITY_INSUFFICIENT);
                        else
                            throw new InternalErrorException(InternalErrorException.UNEXPECTED_RETURN_CODE);
                    }
                    else
                        return rv;
                }
                else {
                    if (Trace.isTraceOn())
                        Trace.log(Trace.ERROR, procedureName + " failed");
                    throw new AS400Exception(spc_.getMessageList());
                }
            }
            catch(PropertyVetoException ignore) {
                // Ignore.
            }
        }
        return 0;
    }



}
