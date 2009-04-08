///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  EnvironmentVariable.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.Serializable;

/**
 The EnvironmentVariable class represents an IBM i system-level environment variable.  An environment variable is uniquely identified by the system and the environment variable name.  Environment variable names are case sensitive and cannot contain spaces or equals signs (=).
 <p>This class can only access system-level environment variables.  You must have *JOBCTL special authority to add, change, or delete system-level environment variables.
 <p>Every environment variable has a CCSID associated with it which describes the CCSID in which its contents are stored.  The default CCSID is that of the current job.
 <p>Note that environment variables are different than system values, although they are often used for the same purpose.  See <a href="SystemValue.html">SystemValue</a> for more information on how to access system values.
 <p>The environment variable value and CCSID are cached after being read once.  Call <a href="#refreshValue()">refreshValue()</a> to force the value and CCSID to be refreshed.
 <p>The following example creates two EnvironmentVariables and sets and gets their values.
 <pre>
 *    AS400 system = new AS400("mysystem");
 *    EnvironmentVariable fg = new EnvironmentVariable(system, "FOREGROUND");
 *    fg.setValue("RED");
 *
 *    EnvironmentVariable bg = new EnvironmentVariable(system, "BACKGROUND");
 *    String background = bg.getValue();
 </pre>
 @see EnvironmentVariableList
 **/
// Implementation note:
// * Currently only system-level environment variables are supported.  Job-level environment variables are not supported, since the get-value API (Qp0zGetEnv) requires the ability to call a service program procedure which returns a char*, and the ServiceProgramCall class does not support that.
// * This is probably not a major limitation, since job-level environment variables would only be valid to the program call job.  However, if the ServiceProgramCall support improves in the future, then it may be worth adding the job-level support here.
public class EnvironmentVariable implements Serializable
{
    static final long serialVersionUID = 4L;

    // Service program return codes.
    static final int RV_ENOENT = 3025;  // No such path or directory.
    static final int RV_EPERM = 3027;  // The operation is not permitted.
    static final int RV_ENOSPC = 3404;  // No space is available.

    // Null parameter for service program calls.
    static final ProgramParameter nullParameter = new ProgramParameter();
    static
    {
        try
        {
            EnvironmentVariable.nullParameter.setParameterType(ProgramParameter.PASS_BY_REFERENCE);
        }
        catch (PropertyVetoException e)
        {
            Trace.log(Trace.ERROR, "Unexpected PropertyVetoException:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }
    }

    // Convenience method for seting up common properties of service program call.
    static ServiceProgramCall setupServiceProgramCall(AS400 system)
    {
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Attempt to connect before setting system." );
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        ServiceProgramCall spc = new ServiceProgramCall(system);
        try
        {
            spc.setProgram("/QSYS.LIB/QP0ZSYSE.SRVPGM");
            spc.suggestThreadsafe();
            spc.setReturnValueFormat(ServiceProgramCall.RETURN_INTEGER);
        }
        catch (PropertyVetoException e)
        {
            Trace.log(Trace.ERROR, "Unexpected PropertyVetoException:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }
        return spc;
    }

    // Convenience method for running service program calls.
    static int runServiceProgram(ServiceProgramCall spc, String procedureName, ProgramParameter[] parameters, int rv1, int rv2) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        synchronized (spc)
        {
            if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "Calling service program procedure: " + procedureName);
            try
            {
                spc.setProcedureName(procedureName);
                spc.setParameterList(parameters);
            }
            catch (PropertyVetoException e)
            {
                Trace.log(Trace.ERROR, "Unexpected PropertyVetoException:", e);
                throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
            }
            if (spc.run())
            {
                int rv = spc.getIntegerReturnValue();
                if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "Service program procedure: " + procedureName + ", returned value: " + rv);
                if (rv == 0 || rv == rv1 || rv == rv2)
                {
                    return rv;
                }
                if (rv == EnvironmentVariable.RV_EPERM)
                {
                    Trace.log(Trace.ERROR, "*JOBCTL special authority required to add, change, or delete system-level environment variable.");
                    throw new AS400SecurityException(AS400SecurityException.SPECIAL_AUTHORITY_INSUFFICIENT);
                }
                Trace.log(Trace.ERROR, "Service program return value was unexpected.");
                throw new InternalErrorException(InternalErrorException.UNEXPECTED_RETURN_CODE);
            }
            if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "Service program procedure: " + procedureName + ", failed.");
            throw new AS400Exception(spc.getMessageList());
        }
    }

    // The system where the environment variable is located.
    private AS400 system_ = null;
    // Service program call object for running environment variable entry points.
    private transient ServiceProgramCall spc_ = null;
    // Converter for environment variable data.
    private transient Converter converter_ = null;

    // Name of environment variable.
    private String name_ = null;
    // Converted name of environment variable, null terminated.
    private byte[] nameBytes_ = null;

    // Value of enviroment variable.
    private transient String value_ = null;
    // Converted value of environment variable, null terminated.
    private transient byte[] valueBytes_ = null;

    // CCSID for environment variable data.
    private transient int ccsid_ = 0;
    // BiDi String type.
    private int stringType_ = 0;

    // List of property change event bean listeners.
    private transient PropertyChangeSupport propertyChangeListeners_ = null;  // Set on first add.

    /**
     Constructs an EnvironmentVariable object.
     **/
    public EnvironmentVariable()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing EnvironmentVariable object.");
    }

    /**
     Constructs an EnvironmentVariable object.
     @param  system  The system on which the environment variable exists.
     **/
    public EnvironmentVariable(AS400 system)
    {
        this();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, " system: " + system);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        system_ = system;
    }

    /**
     Constructs an EnvironmentVariable object.
     <p>Environment variable names are case sensitive and cannot contain spaces or equals signs (=).
     @param  system  The system on which the environment variable exists.
     @param  name  The environment variable name.
     **/
    public EnvironmentVariable(AS400 system, String name)
    {
        this(system);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, " name: '" + name + "'");
        if (name == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'name' is null.");
            throw new NullPointerException("name");
        }
        if ((name.indexOf(' ') >= 0) || (name.indexOf('=') >= 0))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'name' is not valid: " + name);
            throw new ExtendedIllegalArgumentException("name (" + name + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        name_ = name;
    }

    // Implementation note:
    // *  This package scope constructor is intended for use when populating EnvironmentVariableList.
    EnvironmentVariable(AS400 system, ServiceProgramCall spc, String name) throws IOException
    {
        this(system, name);
        spc_ = spc;
    }

    // Implementation note:
    // *  This package scope constructor is intended for use when populating EnvironmentVariableList.
    EnvironmentVariable(AS400 system, ServiceProgramCall spc, byte[] nameBytes, byte[] valueBytes, int ccsid) throws IOException
    {
        this();
        system_ = system;
        spc_ = spc;
        converter_ = new Converter(ccsid, system);

        nameBytes_ = nameBytes;

        valueBytes_ = valueBytes;

        ccsid_ = ccsid;
        stringType_ = AS400BidiTransform.getStringType(ccsid);
    }

    /**
     Adds a PropertyChangeListener.  The specified PropertyChangeListener's <b>propertyChange()</b> method will be called each time the value of any bound property is changed.
     @param  listener  The listener object.
     **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding property change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        synchronized (this)
        {
            // If first add.
            if (propertyChangeListeners_ == null)
            {
                propertyChangeListeners_ = new PropertyChangeSupport(this);
            }
            propertyChangeListeners_.addPropertyChangeListener(listener);
        }
    }

    /**
     Deletes the environment variable.  You must have *JOBCTL special authority to delete system-level environment variables.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void delete() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Deleting environment variable.");
        if (name_ == null && nameBytes_ == null)
        {
            Trace.log(Trace.ERROR, "Attempt to delete before setting name.");
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        if (spc_ == null)
        {
            spc_ = EnvironmentVariable.setupServiceProgramCall(system_);
        }
        if (converter_ == null)
        {
            converter_ = new Converter(system_.getCcsid(), system_);
        }

        if (nameBytes_ == null)
        {
            byte[] tempBytes = converter_.stringToByteArray(name_, stringType_);
            nameBytes_ = new byte[tempBytes.length + 1];
            System.arraycopy(tempBytes, 0, nameBytes_, 0, tempBytes.length);
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Name bytes:", nameBytes_);

        // Initialize the API parameters.
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, nameBytes_),
            EnvironmentVariable.nullParameter
        };
        int rv = runServiceProgram(spc_, "Qp0zDltSysEnv", parameters, RV_ENOENT, 0);
        if (rv == EnvironmentVariable.RV_ENOENT)
        {
            Trace.log(Trace.ERROR, "Environment variable does not exist.");
            throw new ObjectDoesNotExistException(getName(), ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
        }

        // Clear the cache.
        value_ = null;
        valueBytes_ = null;
        ccsid_ = 0;
        stringType_ = 0;
    }

    /**
     Returns the environment variable CCSID.
     @return  The environment variable CCSID.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getCCSID() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting environment variable CCSID.");
        if (ccsid_ == 0) refreshValue();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "CCSID:", ccsid_);
        return ccsid_;
    }

    /**
     Returns the environment variable name.
     @return  The environment variable name.
     **/
    public String getName()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting environment variable name.");
        if (name_ == null && nameBytes_ != null)
        {
            name_ = converter_.byteArrayToString(nameBytes_, 0, nameBytes_.length - 1, stringType_);
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Name: " + name_);
        return name_;
    }

    /**
     Returns the environment variable name.
     @param  stringType  The environment variable bidi string type, as defined by the CDRA (Character Data Representataion Architecture). See <a href="BidiStringType.html">BidiStringType</a> for more information and valid values.
     @return  The environment variable name.
     **/
    public String getName(int stringType)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting environment variable name.");
        if (stringType_ != stringType && nameBytes_ != null)
        {
            name_ = converter_.byteArrayToString(nameBytes_, 0, nameBytes_.length - 1, stringType);
        }
        stringType_ = stringType;
        return getName();
    }

    /**
     Returns the system on which the environment variable exists.
     @return  The system on which the environment variable exists.  If the system has not been set, null is returned.
     **/
    public AS400 getSystem()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system: " + system_);
        return system_;
    }

    /**
     Returns the value of the environment variable.
     @return  The value.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getValue() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting environment variable value.");
        if (value_ == null)
        {
            if (valueBytes_ == null) refreshValue();
            value_ = converter_.byteArrayToString(valueBytes_, 0, valueBytes_.length - 1, stringType_);
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Value: " + value_);
        return value_;
    }

    /**
     Returns the value of the environment variable.
     @param  stringType  The environment variable bidi string type, as defined by the CDRA (Character Data Representataion Architecture). See <a href="BidiStringType.html">BidiStringType</a> for more information and valid values.
     @return  The value.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getValue(int stringType) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        stringType_ = stringType;
        return getValue();
    }

    // Help de-serialize the object.
    private void readObject(java.io.ObjectInputStream ois)
      throws IOException, ClassNotFoundException
    {
      // Restore the non-static and non-transient fields.
      ois.defaultReadObject();

      // Initialize the transient fields.
      spc_ = null;
      converter_ = null;
      value_ = null;
      valueBytes_ = null;
      ccsid_ = 0;
      propertyChangeListeners_ = null;
    }

    /**
     Refreshes the environment variable value and CCSID.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void refreshValue() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Refreshing environment variable value.");
        if (name_ == null && nameBytes_ == null)
        {
            Trace.log(Trace.ERROR, "Attempt to refresh before setting name.");
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        if (spc_ == null)
        {
            spc_ = EnvironmentVariable.setupServiceProgramCall(system_);
        }
        if (converter_ == null)
        {
            converter_ = new Converter(system_.getCcsid(), system_);
        }

        if (nameBytes_ == null)
        {
            byte[] tempBytes = converter_.stringToByteArray(name_, stringType_);
            nameBytes_ = new byte[tempBytes.length + 1];
            System.arraycopy(tempBytes, 0, nameBytes_, 0, tempBytes.length);
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Name bytes:", nameBytes_);

        ProgramParameter[] parameters = new ProgramParameter[]
        {
            new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, nameBytes_),
            new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, 1024),
            new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, new byte[] { 0x00, 0x00, 0x04, 0x00 }, 4),
            new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, 4),
            EnvironmentVariable.nullParameter
        };
        int rv = runServiceProgram(spc_, "Qp0zGetSysEnv", parameters, EnvironmentVariable.RV_ENOSPC, EnvironmentVariable.RV_ENOENT);

        // If the size came back as ENOSPC, then call it again with the correct amount of bytes.
        int actualValueSize = BinaryConverter.byteArrayToInt(parameters[2].getOutputData(), 0);
        if (rv == EnvironmentVariable.RV_ENOSPC)
        {
            if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "ENOSPC returned, getting environment variable again with " + actualValueSize + " bytes.");
            try
            {
                parameters[1].setOutputDataLength(actualValueSize);
                parameters[2].setInputData(BinaryConverter.intToByteArray(actualValueSize));
            }
            catch (PropertyVetoException e)
            {
                Trace.log(Trace.ERROR, "Unexpected PropertyVetoException:", e);
                throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
            }
            runServiceProgram(spc_, "Qp0zGetSysEnv", parameters, 0, 0);
        }
        else if (rv == EnvironmentVariable.RV_ENOENT)
        {
            Trace.log(Trace.ERROR, "Environment variable does not exist.");
            throw new ObjectDoesNotExistException(getName(), ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
        }

        // Get the value.
        valueBytes_ = new byte[actualValueSize];
        System.arraycopy(parameters[1].getOutputData(), 0, valueBytes_, 0, actualValueSize);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Value bytes:", valueBytes_);
        value_ = null;

        // Get the ccsid.
        int ccsid = BinaryConverter.byteArrayToInt(parameters[3].getOutputData(), 0);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "CCSID:", ccsid);
        if (ccsid != ccsid_)
        {
            converter_ = new Converter(ccsid, system_);
            ccsid_ = ccsid;
            stringType_ = AS400BidiTransform.getStringType(ccsid);
        }
    }

    /**
     Removes the PropertyChangeListener.
     @param  listener  The listener object.
     **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing property change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        // If we have listeners.
        if (propertyChangeListeners_ != null)
        {
            propertyChangeListeners_.removePropertyChangeListener(listener);
        }
    }

    /**
     Sets the environment variable name.  This does not change the environment variable name on the system.  Instead, it changes the environment variable to which this EnvironmentVariable object references.
     <p>Environment variable names are case sensitive and cannot contain spaces or equals signs (=).
     @param  name  The environment variable name.
     **/
    public void setName(String name)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting environment variable name: '" + name + "'");
        if (name == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'name' is null.");
            throw new NullPointerException("name");
        }
        if ((name.indexOf(' ') >= 0) || (name.indexOf('=') >= 0))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'name' is not valid: " + name);
            throw new ExtendedIllegalArgumentException("name (" + name + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        // Clear the cache.
        nameBytes_ = null;
        value_ = null;
        valueBytes_ = null;
        ccsid_ = 0;
        stringType_ = 0;

        if (propertyChangeListeners_ == null)
        {
            name_ = name;
        }
        else
        {
            String oldValue = name_;
            String newValue = name;
            name_ = newValue;
            propertyChangeListeners_.firePropertyChange("name", oldValue, newValue);
        }
     }

    /**
     Sets the environment variable name.  This does not change the environment variable name on the system.  Instead, it changes the environment variable to which this EnvironmentVariable object references.
     <p>Environment variable names are case sensitive and cannot contain spaces or equals signs (=).
     @param  name  The environment variable name.
     @param  stringType  The environment variable bidi string type, as defined by the CDRA (Character Data Representataion Architecture). See <a href="BidiStringType.html">BidiStringType</a> for more information and valid values.
     **/
    public void setName(String name, int stringType)
    {
        setName(name);
        stringType_ = stringType;
    }

    /**
     Sets the system for the environment variable.  The system cannot be changed once a connection is made to the system.
     @param  system  The system on which the environment variable exists.
     **/
    public void setSystem(AS400 system)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting system: " + system);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (spc_ != null)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'system' after connect.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        if (propertyChangeListeners_ == null)
        {
            system_ = system;
        }
        else
        {
            AS400 oldValue = system_;
            AS400 newValue = system;
            system_ = newValue;
            propertyChangeListeners_.firePropertyChange("system", oldValue, newValue);
        }
    }

    /**
     Sets the value of the environment variable.  The CCSID will be set to the job CCSID.  You must have *JOBCTL special authority to add or change system-level environment variables.
     @param  value  The value.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setValue(String value) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setValue(value, 0, 0);
    }

    /**
     Sets the value of the environment variable.  You must have *JOBCTL special authority to add or change system-level environment variables.
     @param  value  The value.
     @param  ccsid  The CCSID.  Possible values are:
     <ul>
     <li>0 - Use the job CCSID.
     <li>65535 - Do not translate.
     <li>Any valid CCSID
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setValue(String value, int ccsid) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setValue(value, ccsid, AS400BidiTransform.getStringType(ccsid));
    }

    /**
     Sets the value of the environment variable.  You must have *JOBCTL special authority to add or change system-level environment variables.
     @param  value  The value.
     @param  ccsid  The CCSID.  Possible values are:
     <ul>
     <li>0 - Use the job CCSID.
     <li>65535 - Do not translate.
     <li>Any valid CCSID
     </ul>
     @param  stringType  The environment variable bidi string type, as defined by the CDRA (Character Data Representataion Architecture). See <a href="BidiStringType.html">BidiStringType</a> for more information and valid values.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setValue(String value, int ccsid, int stringType) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting environment variable value: '" + value + "', ccsid: " + ccsid + ", string type: " + stringType);
        if (value == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'value' is null.");
            throw new NullPointerException("value");
        }
        if ((ccsid < 0) || (ccsid > 65535))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'ccsid' is not valid:", ccsid);
            throw new ExtendedIllegalArgumentException("ccsid (" + ccsid + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        if (name_ == null && nameBytes_ == null)
        {
            Trace.log(Trace.ERROR, "Attempt to set value before setting name.");
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        if (spc_ == null)
        {
            spc_ = EnvironmentVariable.setupServiceProgramCall(system_);
        }

        if (ccsid == 0) ccsid = system_.getCcsid();
        converter_ = new Converter(ccsid, system_);
        ccsid_ = ccsid;
        stringType_ = stringType;

        if (nameBytes_ == null)
        {
            byte[] tempBytes = converter_.stringToByteArray(name_, stringType_);
            nameBytes_ = new byte[tempBytes.length + 1];
            System.arraycopy(tempBytes, 0, nameBytes_, 0, tempBytes.length);
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Name bytes:", nameBytes_);

        byte[] tempBytes = converter_.stringToByteArray(value, stringType_);
        valueBytes_ = new byte[tempBytes.length + 1];
        System.arraycopy(tempBytes, 0, valueBytes_, 0, tempBytes.length);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Value bytes:", valueBytes_);

        // Build the byte array for the environment variable.
        // Length is name length - one byte for null + one byte for equal sign + value length (including the null).
        byte[] parameterBytes = new byte[nameBytes_.length + valueBytes_.length];

        // Copy each part into the parameter byte array.
        System.arraycopy(nameBytes_, 0, parameterBytes, 0, nameBytes_.length - 1);
        parameterBytes[nameBytes_.length - 1] = 0x7E;  // EBCCIC equal (=).
        System.arraycopy(valueBytes_, 0, parameterBytes, nameBytes_.length, valueBytes_.length);

        // Initialize the program parameters.
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, parameterBytes),
            new ProgramParameter(ProgramParameter.PASS_BY_VALUE, BinaryConverter.intToByteArray(ccsid)),
            EnvironmentVariable.nullParameter
        };
        runServiceProgram(spc_, "Qp0zPutSysEnv", parameters, 0, 0);
    }
}
