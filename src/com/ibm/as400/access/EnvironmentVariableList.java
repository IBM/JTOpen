///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  EnvironmentVariableList.java
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
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 The EnvironmentVariableList class represents a list of OS/400 system-level environment variables.
 <p>This class can only access system-level environment variables.  You must have *JOBCTL special authority to add, change, or delete system-level environment variables.
 <p>This example gets the list of environment variables as a java.util.Properties object:
 <pre>
 *    AS400 system = new AS400("mysystem");
 *    EnvironmentVariableList evList = new EnvironmentVariableList(system);
 *    Properties p = evList.getProperties();
 </pre>
 <p>This example uses an Enumeration to print the list of environment variable names and values:
 <pre>
 *    AS400 system = new AS400("mysystem");
 *    EnvironmentVariableList evList = new EnvironmentVariableList(system);
 *    Enumeration enum = evList.getEnvironmentVariables();
 *    while (enum.hasMoreElements())
 *    {
 *        EnvironmentVariable ev = (EnvironmentVariable)enum.nextElement();
 *        System.out.println(ev.getName() + "=" + ev.getValue());
 *    }
 </pre>
 @see EnvironmentVariable
 **/
// Implementation note:
// * There is currently no API to get a list of job-level environment variables.  This is not available since C programs can get the list via the ENVIRON environment variable.
public class EnvironmentVariableList implements Serializable
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;

    private AS400 system_ = null;
    private transient ServiceProgramCall spc_ = null;

    // List of property change event bean listeners.
    private transient PropertyChangeSupport propertyChangeListeners_ = null;  // Set on first add.

    /**
     Constructs a EnvironmentVariableList object.
     **/
    public EnvironmentVariableList()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing EnvironmentVariableList object.");
    }

    /**
     Constructs a EnvironmentVariableList object.
     @param  system  The system.
     **/
    public EnvironmentVariableList(AS400 system)
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
     Returns an enumeration that contains an EnvironmentVariable object for each environment variable on the system.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public Enumeration getEnvironmentVariables() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting environment variables.");
        if (spc_ == null)
        {
            spc_ = EnvironmentVariable.setupServiceProgramCall(system_);
        }

        ProgramParameter[] parameters = new ProgramParameter[]
        {
            new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, 4096),
            new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, new byte[] { 0x00, 0x00, 0x10, 0x00 }, 4),
            new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, 1024),
            new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, new byte[] { 0x00, 0x00, 0x04, 0x00 }, 4),
            EnvironmentVariable.nullParameter
        };

        int rv = EnvironmentVariable.runServiceProgram(spc_, "Qp0zGetAllSysEnv", parameters, EnvironmentVariable.RV_ENOSPC, EnvironmentVariable.RV_ENOENT);

        int actualListBufferSize = BinaryConverter.byteArrayToInt(parameters[1].getOutputData(), 0);
        int actualCcsidBufferSize = BinaryConverter.byteArrayToInt(parameters[3].getOutputData(), 0);
        // If the size came back as ENOSPC, then call it again with the correct amount of bytes.
        if (rv == EnvironmentVariable.RV_ENOSPC)
        {
            if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "ENOSPC returned, getting environment variable list again with " + actualListBufferSize + " bytes for the list and " + actualCcsidBufferSize + " bytes for the ccsids.");
            try
            {
                parameters[0].setOutputDataLength(actualListBufferSize);
                parameters[1].setInputData(BinaryConverter.intToByteArray(actualListBufferSize));
                parameters[2].setOutputDataLength(actualCcsidBufferSize);
                parameters[3].setInputData(BinaryConverter.intToByteArray(actualCcsidBufferSize));
            }
            catch (PropertyVetoException e)
            {
                Trace.log(Trace.ERROR, "Unexpected PropertyVetoException:", e);
                throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
            }
            EnvironmentVariable.runServiceProgram(spc_, "Qp0zGetAllSysEnv", parameters, 0, 0);
        }
        // If ENOENT is returned, then there are no environment variables.
        else if (rv == EnvironmentVariable.RV_ENOENT)
        {
            return new Vector().elements();
        }

        // Build up a Vector of EnvironmentVariable objects.
        Vector list = new Vector();
        int offsetIntoListBuffer = 0;
        int offsetIntoCcsidBuffer = 0;
        byte[] listBufferBytes = parameters[0].getOutputData();
        byte[] ccsidBufferBytes = parameters[2].getOutputData();

        do
        {
            // Find the EBCDIC equals (=).
            int equalPosition = offsetIntoListBuffer;
            while (listBufferBytes[equalPosition] != 0x7E) ++equalPosition;

            // Find the null.
            int nullPosition = equalPosition;
            while (listBufferBytes[nullPosition] != 0) ++nullPosition;

            // Get the name, make null terminated.
            byte[] nameBytes = new byte[equalPosition - offsetIntoListBuffer + 1];
            System.arraycopy(listBufferBytes, offsetIntoListBuffer, nameBytes, 0, nameBytes.length - 1);
            // Get the value, make null terminated.
            byte[] valueBytes = new byte[nullPosition - equalPosition];
            System.arraycopy(listBufferBytes, equalPosition + 1, valueBytes, 0, valueBytes.length - 1);

            // Get the CCSID.
            int ccsid = BinaryConverter.byteArrayToInt(ccsidBufferBytes, offsetIntoCcsidBuffer);

            // Create the EnvironmentVariable object, add it to vector.
            list.addElement(new EnvironmentVariable(system_, spc_, nameBytes, valueBytes, ccsid));

            // Get ready for the next iteration.
            offsetIntoListBuffer = nullPosition + 1;
            offsetIntoCcsidBuffer += 4;

            // Until ending null terminator.
        } while (listBufferBytes[offsetIntoListBuffer] != 0);

        return list.elements();
    }

    /**
     Returns a new Properties object which contains an entry for each environment variable in the list.
     @return  The new Properties object.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public Properties getProperties() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting environment variables as Properties.");
        Properties properties = new Properties();
        Enumeration enum = getEnvironmentVariables();
        while (enum.hasMoreElements())
        {
            EnvironmentVariable environmentVariable = (EnvironmentVariable)enum.nextElement();
            properties.put(environmentVariable.getName(), environmentVariable.getValue());
        }
        return properties;
    }

    /**
     Returns the server on which the environment variable list exists.
     @return  The server on which the environment variable list exists.  If the server has not been set, null is returned.
     **/
    public AS400 getSystem()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system: " + system_);
        return system_;
    }

    /**
     Removes a PropertyChangeListener.
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
     Sets the value of each environment variable defined in a Properties object.
     @param  properties  The Properties object.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public void setProperties(Properties properties) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Set string type to -1 to signal lookup based on CCSID.
        setProperties(properties, -1);
    }

    /**
     Sets the value of each environment variable defined in a Properties object.
     @param  properties  The Properties object.
     @param  stringType  The environment variable bidi string type, as defined by the CDRA (Character Data Representataion Architecture). See {@link com.ibm.as400.access.BidiStringType BidiStringType} for more information and valid values.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public void setProperties(Properties properties, int stringType) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting environment variables from Properties.");
        if (properties == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'properties' is null.");
            throw new NullPointerException("properties");
        }
        if (spc_ == null)
        {
            spc_ = EnvironmentVariable.setupServiceProgramCall(system_);
        }

        if (stringType == -1) stringType = AS400BidiTransform.getStringType(system_.getCcsid());

        Enumeration propertyNames = properties.propertyNames();
        while (propertyNames.hasMoreElements())
        {
            String propertyName = (String)propertyNames.nextElement();
            EnvironmentVariable environmentVariable = new EnvironmentVariable(system_, spc_, propertyName);
            environmentVariable.setValue(properties.getProperty(propertyName), 0, stringType);
        }
    }

    /**
     Sets the server for the environment variable list.  The server cannot be changed once a connection is made to the server.
     @param  system  The server on which the environment variable list exists.
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
}
