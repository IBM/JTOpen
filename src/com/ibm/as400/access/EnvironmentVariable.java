///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: EnvironmentVariable.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;



/**
The EnvironmentVariable class represents an AS/400 system-level
environment variable.  An AS/400 environment variable is uniquely
identified by the AS/400 system and the environment variable name.
Environment variable names are case sensitive and cannot contain
spaces or equals signs (=).

<p>This class can only access system-level environment variables
on V4R4 or later AS/400s.  You must have *JOBCTL special authority
to access system-level environment variables.

<p>Every environment variable has a CCSID associated with it which
describes the CCSID in which its contents are stored.  The default
CCSID is that of the current job.

<p>Note that environment variables are different than system values,
although they are often used for the same purpose.  See
<a href="SystemValue.html">SystemValue</a> for
more information on how to access system values.

<p>The environment variable value and CCSID are cached after being
read once.  Call <a href="#refreshValue()">refreshValue()</a> to
force the value and CCSID to be refreshed.

<p>The following example creates two EnvironmentVariables and sets
and gets their values.

<pre>
AS400 system = new AS400("mysystem");
EnvironmentVariable fg = new EnvironmentVariable(system, "FOREGROUND");
fg.setValue("RED");
<br>
EnvironmentVariable bg = new EnvironmentVariable(system, "BACKGROUND");
String background = bg.getValue();
</pre>

@see EnvironmentVariableList
**/
//
// Implementation notes:
//
// * Currently only system-level environment variables are supported.
//   Job-level environment variables are not supported, since the get-value
//   API (Qp0zGetEnv) requires the ability to call a service program
//   procedure which returns a char*, and the ServiceProgramCall class
//   does not support that.
//
//   This is probably not a major limitation, since job-level environment
//   variables would only be valid to the program call job.  However,
//   if the ServiceProgramCall support improves in the future, then
//   it may be worth adding the job-level support here.
//
// * The reason for the V4R4 and later limitation is that
//   ServiceProgramCall only works to V4R4 and later.
//
public class EnvironmentVariable
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;


//-----------------------------------------------------------------------------------------
// Private data.
//-----------------------------------------------------------------------------------------

    private static final int                        CCSID_NOT_SET_      = -99;
    private static final int                        INITIAL_VALUE_SIZE_ = 1024;

    private int                                     cachedCcsid_        = CCSID_NOT_SET_;
    private transient byte[]                        cachedValue_        = null;             // @A1C
    private transient int                           cachedValueSize_    = 0;                // @A1A
    private transient Converter                     converter_          = null;             // @A1A
    private String                                  name_               = null;
    private byte[]                                  nameAsBytes_        = null;             // @A1A
    private int                                     nameType_           = -1;               // @A1A
    private AS400                                   system_             = null;

    private transient EnvironmentVariableHelper     helper_                 = null;
    private transient PropertyChangeSupport         propertyChangeSupport_  = null;



/**
Constructs an EnvironmentVariable object.
**/
    public EnvironmentVariable()
    {
        initializeTransient();
    }



/**
Constructs an EnvironmentVariable object.

@param system   The system.
**/
    public EnvironmentVariable(AS400 system)
    {
        initializeTransient();
        setSystem(system);
    }



/**
Constructs an EnvironmentVariable object.

@param system   The system.
@param name     The environment variable name.
**/
    public EnvironmentVariable(AS400 system, String name)
    {
        initializeTransient();
        setSystem(system);
        setName(name);
    }



/**
Constructs an EnvironmentVariable object.

@param system   The system.
@param name     The environment variable name.
@param helper   The environment variable helper.

@exception IOException If an error occurs.
**/
//
// Implementation note:
//
// This package scope constructor is intended for use when populating
// EnvironmentVariableList.
//
    EnvironmentVariable(AS400 system, String name, EnvironmentVariableHelper helper)
    throws IOException
    {
        initializeTransient();
        setSystem(system);
        setName(name);

        converter_      = new Converter(system.getCcsid(), system);
        helper_         = helper;
    }



/**
Constructs an EnvironmentVariable object.

@param system   The system.
@param name     The environment variable name.
@param helper   The environment variable helper.
@param value    The initial value.
@param ccsid    The initial CCSID.

@exception IOException If an error occurs.
**/
//
// Implementation note:
//
// This package scope constructor is intended for use when populating
// EnvironmentVariableList.
//
    EnvironmentVariable(AS400 system, byte[] name,                          // @A1C
                        EnvironmentVariableHelper helper,
                        byte[] value, int ccsid)                            // @A1C
    throws IOException
    {
        initializeTransient();
        setSystem(system);
        // @A1D setName(name);

        helper_         = helper;
        cachedValue_    = value;
        cachedValueSize_= value.length;                                     // @A1A
        cachedCcsid_    = ccsid;
        nameAsBytes_    = name;                                             // @A1A

        converter_ = new Converter(system.getCcsid(), system);              // @A1A
    }




/**
Adds a PropertyChangeListener.  The specified PropertyChangeListener's
<b>propertyChange()</b> method will be called each time the value of
any bound property is changed.

@param listener The listener.
*/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        propertyChangeSupport_.addPropertyChangeListener(listener);
    }



/**
Deletes the environment variable.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public void delete()
    throws AS400SecurityException,
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
    {
        if (system_ == null)
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        if (getName() == null)                                                                  // @A1C
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        if (helper_ == null)
            helper_ = new EnvironmentVariableHelper(system_);

        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "Deleting ev:" + getName() + ".");                     // @A1C

        // Initialize the API parameters.
        Converter converter = new Converter(system_.getCcsid(), system_);
        ProgramParameter[] parameters;
        parameters = new ProgramParameter[2];
        parameters[0] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, makeBytes(converter, getName())); // @A1C
        parameters[1] = new ProgramParameter(null);
        helper_.callServiceProgramInt("Qp0zDltSysEnv", parameters, 0);

        // Clear the cache.
        cachedCcsid_ = CCSID_NOT_SET_;
        cachedValue_ = null;
    }



/**
Returns the environment variable CCSID.

@return The environment variable CCSID.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public int getCCSID()
    throws AS400SecurityException,
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
    {
        if (system_ == null)
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        if (getName() == null)                                                                  // @A1C
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        if (cachedCcsid_ == CCSID_NOT_SET_)
            refreshValue();
        return cachedCcsid_;
    }



/**
Returns the environment variable name.

@return The environment variable name.
**/
    public String getName()
    {
        return getName(AS400BidiTransform.getStringType((char)cachedCcsid_));             // @A1C
    }


// @A1A
/**
Returns the environment variable name.

@param type The environment variable bidi string type, as defined by the CDRA (Character
      Data Representataion Architecture). See <a href="BidiStringType.html">
      BidiStringType</a> for more information and valid values.

@return The environment variable name.
**/
    public String getName(int type)
    {   
        if ((nameType_ == type) && (name_ != null))
            return name_;
        else if (nameAsBytes_ != null) 
        {
            name_ = converter_.byteArrayToString(nameAsBytes_, type);
            nameType_ = type;
            return name_;
        }
        else
           return name_;
    }



/**
Returns the system.

@return The system.
**/
    public AS400 getSystem()
    {
        return system_;
    }



/**
Returns the value of the environment variable.

@return The value.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public String getValue()
    throws AS400SecurityException,
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
    {
        return getValue(AS400BidiTransform.getStringType((char)cachedCcsid_)); // @A1C
    }



// @A1A
/**
Returns the value of the environment variable.

@param type The environment variable bidi string type, as defined by the CDRA (Character
      Data Representataion Architecture). See <a href="BidiStringType.html">
      BidiStringType</a> for more information and valid values.
@return The value.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public String getValue(int type)
    throws AS400SecurityException,
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
    {
        if (system_ == null)
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        if (getName() == null)                                                                  // @A1C
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        
        if (cachedValue_ == null)
            refreshValue();
        return converter_.byteArrayToString(cachedValue_, 0, cachedValueSize_, type); // @A1C
    }



/**
Initializes transient data.
**/
    private void initializeTransient ()
    {
        propertyChangeSupport_  = new PropertyChangeSupport(this);
    }



    private static byte[] makeBytes(Converter converter, String name)
    {
        byte[] temp = converter.stringToByteArray(name);
        byte[] temp2 = new byte[temp.length + 1];
        System.arraycopy(temp, 0, temp2, 0, temp.length);
        temp2[temp.length] = 0;
        return temp2;
    }



/**
Restores the state of the object from an input stream.
This is used when deserializing an object.

@param in   The input stream.
**/
    private void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject ();
        initializeTransient ();
    }





/**
Refreshes the environment variable value and CCSID.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public void refreshValue()
    throws AS400SecurityException,
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
    {
        if (system_ == null)
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        if (getName() == null)                                                                  // @A1C
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
       
        // Clear the cache, in case we get an exception on the way.
        cachedCcsid_ = CCSID_NOT_SET_;
        cachedValue_ = null;

        if (helper_ == null)
            helper_ = new EnvironmentVariableHelper(system_);

        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "Getting ev value for " + name_ + ".");

        // Initialize the program parameters.
        if (converter_ == null)                                             // @A1A
            converter_ = new Converter(system_.getCcsid(), system_);        // @A1C
        if (nameAsBytes_ == null)                                           // @A1A
            nameAsBytes_ = converter_.stringToByteArray(name_);             // @A1A
        ProgramParameter[] parameters;
        parameters = new ProgramParameter[5];
        parameters[0] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, nameAsBytes_); // @A1C
        parameters[1] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, new byte[1], INITIAL_VALUE_SIZE_);
        parameters[2] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, BinaryConverter.intToByteArray(INITIAL_VALUE_SIZE_), 4);
        parameters[3] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, BinaryConverter.intToByteArray(system_.getCcsid()), 4);
        parameters[4] = new ProgramParameter(null);
        int rv = helper_.callServiceProgramInt("Qp0zGetSysEnv", parameters, EnvironmentVariableHelper.RV_ENOSPC_);

        // If the size came back as ENOSPC, then call it again with the correct
        // amount of bytes.
        int actualValueSize = BinaryConverter.byteArrayToInt(parameters[2].getOutputData(), 0);
        if (rv == EnvironmentVariableHelper.RV_ENOSPC_) {
            if (Trace.isTraceOn())
                Trace.log(Trace.INFORMATION, "ENOSPC returned, getting ev again with " + actualValueSize + " bytes.");
            parameters[1] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, new byte[1], actualValueSize);
            parameters[2] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, BinaryConverter.intToByteArray(actualValueSize), 4);
            helper_.callServiceProgramInt("Qp0zGetSysEnv", parameters, 0);
        }

        cachedCcsid_ = BinaryConverter.byteArrayToInt(parameters[3].getOutputData(), 0);

        // Subtract 1 from the actual value size to account for the null termination.
        cachedValue_ = parameters[1].getOutputData();                                       // @A1C
        cachedValueSize_ = actualValueSize - 1;                                             // @A1A
    }



/**
Removes a PropertyChangeListener.

@param listener The listener.
*/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        propertyChangeSupport_.removePropertyChangeListener(listener);
    }



/**
Sets the environment variable name.  This does not change the
environment variable name on the AS/400.  Instead, it changes the
environment variable to which this EnvironmentVariable object
references.  This cannot be changed if the object has established
a connection to the AS/400.

<p>Environment variable names are case sensitive and cannot
contain spaces or equals signs (=).

@param name    The environment variable name.
**/
    public void setName(String name)
    {
        setName(name, AS400BidiTransform.getStringType((char)cachedCcsid_));              // @A1C
    }



// @A1C
/**
Sets the environment variable name.  This does not change the
environment variable name on the AS/400.  Instead, it changes the
environment variable to which this EnvironmentVariable object
references.  This cannot be changed if the object has established
a connection to the AS/400.

<p>Environment variable names are case sensitive and cannot
contain spaces or equals signs (=).

@param name    The environment variable name.
@param type The environment variable bidi string type, as defined by the CDRA (Character
      Data Representataion Architecture). See <a href="BidiStringType.html">
      BidiStringType</a> for more information and valid values.
**/
    public void setName(String name, int type)
    {   
        if (name == null)
            throw new NullPointerException("name");
        if ((name.indexOf(' ') >= 0)
            || (name.indexOf('=') >= 0))
            throw new ExtendedIllegalArgumentException("name", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        String oldValue = name_;
        name_ = name;
        nameAsBytes_ = null;                                    // @A1A
        nameType_ = type;                                       // @A1A
        propertyChangeSupport_.firePropertyChange("name", oldValue, name);

        // Clear the cache.
        cachedCcsid_ = CCSID_NOT_SET_;
        cachedValue_ = null;
        cachedValueSize_ = 0;                                   // @A1A
    }



/**
Sets the system.  This cannot be changed if the object has established
a connection to the AS/400.

@param system   The system.
**/
    public void setSystem(AS400 system)
    {
        if (system == null)
            throw new NullPointerException("system");
        if (helper_ != null)
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

        AS400 oldValue = system_;
        system_ = system;
        propertyChangeSupport_.firePropertyChange("system", oldValue, system);
    }



/**
Sets the value of the environment variable.
The CCSID will be set to the job CCSID.

@param value    The value.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public void setValue(String value)
    throws AS400SecurityException,
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
    {
        setValue(value, 0);
    }



/**
Sets the value of the environment variable.

@param value    The value.
@param ccsid    The CCSID.  Possible values are:
                <ul>
                <li>0 - Use the job CCSID.
                <li>65535 - Do not translate.
                <li>Any valid CCSID
                </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public void setValue(String value, int ccsid)
    throws AS400SecurityException,
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
    {
        setValue(value, ccsid, AS400BidiTransform.getStringType((char)cachedCcsid_)); // @A1A
    }



// @A1A
/**
Sets the value of the environment variable.

@param value    The value.
@param ccsid    The CCSID.  Possible values are:
                <ul>
                <li>0 - Use the job CCSID.
                <li>65535 - Do not translate.
                <li>Any valid CCSID
                </ul>
@param type The environment variable bidi string type, as defined by the CDRA (Character
      Data Representataion Architecture). See <a href="BidiStringType.html">
      BidiStringType</a> for more information and valid values.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public void setValue(String value, int ccsid, int type)
    throws AS400SecurityException,
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
    {   
        if (value == null)
            throw new NullPointerException("value");
        if ((ccsid < 0) || (ccsid > 65535))
            throw new ExtendedIllegalArgumentException("ccsid", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        if (system_ == null)
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        if (getName() == null)                                                                  // @A1C
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        
        // Clear the cache, in case we get an exception on the way.
        cachedCcsid_ = CCSID_NOT_SET_;
        cachedValue_ = null;
        cachedValueSize_ = 0;                                   // @A1A

        if (helper_ == null)
            helper_ = new EnvironmentVariableHelper(system_);

        // Initialize the program parameters.
        ProgramParameter[] parameters;
        
        byte[] ccsidBytes = BinaryConverter.intToByteArray((ccsid == 0) ? system_.getCcsid() : ccsid);
        
        if (converter_ == null)                                 // @A1A
        {  
           //Calling connectService ensures that we will be using the jobs ccsid and not a ccsid           // @A2A
           //determined by the locale of the user.                                                         // @A2A 
           system_.connectService(AS400.COMMAND);                                                          // @A2A
           converter_ = new Converter(/*(ccsid == 0) ?*/ system_.getCcsid()/* : ccsid*/, system_); // @A1C // @A2C
        }                                                                                                  // @A2A
        
        if (nameAsBytes_ == null)                                           // @A2A
           nameAsBytes_ = converter_.stringToByteArray(name_, type);        // @A2A

        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION, "Setting ev value:" + name_ + "=" + value + " (ccsid = " + ccsid + ").");

        parameters = new ProgramParameter[3];
        
        // Build the byte array for the environment variable.
        //byte[] asBytes = converter_.stringToByteArray(buffer.toString(), type);             // @A2D
        byte[] asBytes;                                                                       // @A2C

        // To allow bidi data to be used in an environment variable, each
        // part (the name of the variable, the equal, and the value of the variable)
        // must be converted into bytes and passed to service program call.                   // @A2A
        byte[] part1 = converter_.stringToByteArray(name_, type);                             // @A2A
        byte[] part2 = converter_.stringToByteArray("=");                                     // @A2A
        byte[] part3 = converter_.stringToByteArray(value, type);                             // @A2A

        // Allocate the proper byte array size.                                               // @A2A
        asBytes = new byte[part1.length + part2.length + part3.length];                       // @A2A

        // Copy each part into the end byte array.                                            // @A2A
        System.arraycopy(part1,0,asBytes,0,part1.length);                                     // @A2A
        System.arraycopy(part2,0,asBytes,part1.length,part2.length);                          // @A2A
        System.arraycopy(part3,0,asBytes,part1.length+part2.length,part3.length);             // @A2A
        
        parameters[0] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, asBytes);  // @A1C
        parameters[1] = new ProgramParameter(ProgramParameter.PASS_BY_VALUE, ccsidBytes);
        parameters[2] = new ProgramParameter(null);
        helper_.callServiceProgramInt("Qp0zPutSysEnv", parameters, 0);

        // Cache the value and ccsid (but only if not 0, since we won't know
        // the actual ccsid in that case).
        // @A1D cachedValue_ = asBytes;                                                             // @A1C
        // @A1D cachedValueSize_ = asBytes.length;                                                  // @A1C
        // @A1D if (ccsid != 0)
        // @A1D    cachedCcsid_ = ccsid;
    }



}
