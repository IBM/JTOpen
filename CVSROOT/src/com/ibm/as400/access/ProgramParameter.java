///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ProgramParameter.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 Used with {@link ProgramCall ProgramCall} and {@link ServiceProgramCall ServiceProgramCall} to pass parameter data, either to a program, from a program, or both.  Input data is passed to a program as a byte array with {@link #setInputData setInputData()}.  Output data is requested from a program by specifying the amount of data to return with {@link #setOutputDataLength setOutputDataLength()}.  To get the output data once the program has run, use {@link #getOutputData getOutputData()}.  These values may also be set on the constructor.
 **/
public class ProgramParameter implements Serializable
{
    static final long serialVersionUID = 4L;

    /**
     Constant indicating parameter data is passed by value.  (This is the default.)
     **/
    public static final int PASS_BY_VALUE = 1;

    /**
     Constant indicating parameter data is passed by reference.
     **/
    public static final int PASS_BY_REFERENCE = 2;

    // Valid values for parameter usage.
    static final int NULL = 0xFF;
    static final int INPUT  = 1;
    static final int OUTPUT = 2;
    static final int INOUT  = 3;

    // Variable representing the type of the service program parameter.
    private int parameterType_ = PASS_BY_VALUE;

    // Parameter data.
    private boolean nullParameter_ = true;
    private byte[] inputData_ = null;
    private int outputDataLength_ = 0;
    private byte[] outputData_ = null;

    // Temporary variables to hold information needed to put parameter on datastream.
    // These are transient to prevent increasing serialized size.
    // Values only valid during datastream construction.
    transient int length_ = 0;  // Byte length of parameter information.
    transient int maxLength_ = 0;  // Max length of input and output data.
    transient int usage_ = 0;  // Parameter usage: in, out, inout & 0-truncation, RLE.
    transient byte[] compressedInputData_ = null;  // Input data compressed.

    // List of property change event bean listeners.
    private transient PropertyChangeSupport propertyChangeListeners_;
    // List of vetoable change event bean listeners.
    private transient VetoableChangeSupport vetoableChangeListeners_;

    /**
     Constructs a ProgramParameter object.
     **/
    public ProgramParameter()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing ProgramParameter object.");
    }

    /**
     Constructs a ProgramParameter object.  An input parameter is created since a byte array containing parameter data is passed on this constructor.
     @param  inputData  The parameter data to be used as input to the program.
     **/
    public ProgramParameter(byte[] inputData)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing ProgramParameter object, input data:", inputData);
        nullParameter_ = false;
        inputData_ = inputData;
    }

    /**
     Constructs a ProgramParameter object.  An output parameter is created, since the size of the output data is passed on this constructor.
     @param  outputDataLength  The amount of data to be returned from the program (number of bytes).
     **/
    public ProgramParameter(int outputDataLength)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing ProgramParameter object, output data length:", outputDataLength);
        if (outputDataLength < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'outputDataLength' is not valid:", outputDataLength);
            throw new ExtendedIllegalArgumentException("outputDataLength (" + outputDataLength + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        nullParameter_ = false;
        outputDataLength_ = outputDataLength;
    }

    /**
     Constructs ProgramParameter object.  A parameter that is both an input and an output parameter is created, since both data passed to the program and the amount of data returned from the program are passed on this constructor.
     @param  inputData  Parameter data passed to the program.
     @param  outputDataLength  The amount of data to be returned from the program (number of bytes).
     **/
    public ProgramParameter(byte[] inputData, int outputDataLength)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing ProgramParameter object, output data length: " + outputDataLength + " input data:", inputData);
        if (outputDataLength < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'outputDataLength' is not valid:", outputDataLength);
            throw new ExtendedIllegalArgumentException("outputDataLength (" + outputDataLength + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        nullParameter_ = false;
        inputData_ = inputData;
        outputDataLength_ = outputDataLength;
    }

    /**
     Constructs a ProgramParameter object.  An input parameter is created, since a byte array containing parameter data is passed on this constructor.  The type indicates if the data is pass by reference or pass by value.  The type attribute is used by ServiceProgramCall.
     @param  parameterType  The type of parameter.
        Valid values are {@link #PASS_BY_VALUE PASS_BY_VALUE} and {@link #PASS_BY_REFERENCE PASS_BY_REFERENCE}.  The default is PASS_BY_VALUE.
     @param  inputData  The parameter data to be used as input to the program.
     **/
    public ProgramParameter(int parameterType, byte[] inputData)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing ProgramParameter object, service program parameter type: " + parameterType + " input data:", inputData);
        if (parameterType < PASS_BY_VALUE || parameterType > PASS_BY_REFERENCE)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'parameterType' is not valid:", parameterType);
            throw new ExtendedIllegalArgumentException("parameterType (" + parameterType + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        parameterType_ = parameterType;
        nullParameter_ = false;
        inputData_ = inputData;
    }

    /**
     Constructs a ProgramParameter object.  An output parameter is created, since the size of the output data is passed on this constructor.  The type indicates if the data is pass by reference or pass by value.  The type attribute is used by ServiceProgramCall.
     @param  parameterType  The type of parameter.
        Valid values are {@link #PASS_BY_VALUE PASS_BY_VALUE} and {@link #PASS_BY_REFERENCE PASS_BY_REFERENCE}.  The default is PASS_BY_VALUE.
     @param  outputDataLength  The amount of data to be returned from the program (number of bytes).
     **/
    public ProgramParameter(int parameterType, int outputDataLength)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing ProgramParameter object, service program parameter type: " + parameterType + " output data length:", outputDataLength);
        if (parameterType < PASS_BY_VALUE || parameterType > PASS_BY_REFERENCE)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'parameterType' is not valid:", parameterType);
            throw new ExtendedIllegalArgumentException("parameterType (" + parameterType + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (outputDataLength < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'outputDataLength' is not valid:", outputDataLength);
            throw new ExtendedIllegalArgumentException("outputDataLength (" + outputDataLength + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        parameterType_ = parameterType;
        nullParameter_ = false;
        outputDataLength_ = outputDataLength;
    }

    /**
     Constructs ProgramParameter object.  A parameter that is both an input and an output parameter is created, since both data passed to the program and the amount of data returned from the program are passed on this constructor.  The type indicates if the data is pass by reference or pass by value.  The type attribute is used by ServiceProgramCall.
     @param  parameterType  The type of parameter.
        Valid values are {@link #PASS_BY_VALUE PASS_BY_VALUE} and {@link #PASS_BY_REFERENCE PASS_BY_REFERENCE}.  The default is PASS_BY_VALUE.
     @param  inputData  The parameter data to be used as input to the program.
     @param  outputDataLength  The amount of data to be returned from the program (number of bytes).
     **/
    public ProgramParameter(int parameterType, byte[] inputData, int outputDataLength)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing ProgramParameter object, service program parameter type: " + parameterType + " output data length: " + outputDataLength + " input data:", inputData);
        if (parameterType < PASS_BY_VALUE || parameterType > PASS_BY_REFERENCE)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'parameterType' is not valid:", parameterType);
            throw new ExtendedIllegalArgumentException("parameterType (" + parameterType + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (outputDataLength < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'outputDataLength' is not valid:", outputDataLength);
            throw new ExtendedIllegalArgumentException("outputDataLength (" + outputDataLength + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        parameterType_ = parameterType;
        nullParameter_ = false;
        inputData_ = inputData;
        outputDataLength_ = outputDataLength;
    }

    /**
     Adds a PropertyChangeListener.  The specified PropertyChangeListener's <b>propertyChange</b> method will be called each time the value of any bound property is changed.  The PropertyListener object is added to a list of PropertyChangeListeners managed by this ProgramParameter.  It can be removed with removePropertyChangeListener.
     @param  listener  The PropertyChangeListener.
     **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Adding property change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        if (propertyChangeListeners_ == null)
        {
          propertyChangeListeners_ = new PropertyChangeSupport(this);
        }
        propertyChangeListeners_.addPropertyChangeListener(listener);
    }

    /**
     Adds a VetoableChangeListener.  The specified VetoableChangeListeners <b>vetoableChange</b> method will be called each time the value of any constrained property is changed.
     @param  listener  The VetoableChangeListener.
     **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Adding vetoable change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        if (vetoableChangeListeners_ == null)
        {
          vetoableChangeListeners_ = new VetoableChangeSupport(this);
        }
        vetoableChangeListeners_.addVetoableChangeListener(listener);
    }

    // Returns the parameter max length.  This is the maximum of the input data length and the output data length.
    // @return  The parameter max length (number of bytes).
    int getMaxLength()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting max length.");
        int maxLength = (inputData_ == null) ? outputDataLength_ : Math.max(inputData_.length, outputDataLength_);
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Max length:", maxLength);
        return maxLength;
    }

    /**
     Returns the parameter data that will be sent to the program.  Null is returned if the input data has not been set.
     @return  The parameter data to be used as input to the program.
     **/
    public byte[] getInputData()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting input data:", inputData_);
        return inputData_;
    }

    /**
     Returns the parameter data that has been received from the program.  Null is returned if this parameter is an input parameter.  Null is also returned before the program is called.
     @return  The output data returned from the program.
     **/
    public byte[] getOutputData()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting output data.");
        return outputData_;
    }

    /**
     Returns the output parameter data length.
     @return  The amount of data to be returned from the program (number of bytes).
     **/
    public int getOutputDataLength()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting output data length:", outputDataLength_);
        return outputDataLength_;
    }

    /**
     Returns the program parameter type.  The type indicates if data is passed by reference or passed by value.  The type attribute is used by ServiceProgramCall.
     @return  The program parameter type.  The type is one of the following:
     <ul>
     <li>{@link #PASS_BY_VALUE PASS_BY_VALUE} - The parameter is passed as data.
     <li>{@link #PASS_BY_REFERENCE PASS_BY_REFERENCE} - The parameter is passed as a reference.
     </ul>
     **/
    public int getParameterType()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting parameter type:", parameterType_);
        return parameterType_;
    }

    // Returns the parameter usage.
    int getUsage()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting parameter usage.");
        int usage = nullParameter_ ? NULL : outputDataLength_ == 0 ? INPUT : inputData_ == null ? OUTPUT : INOUT;
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Parameter usage:", usage);
        return usage;
    }

    /**
     Indicates if this object represents a null parameter.
     @return  true if the parameter is null; false otherwise.
     **/
    public boolean isNullParameter()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if null parameter:", nullParameter_);
        return nullParameter_;
    }

    // Deserialize and initialize transient data.
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "De-serializing ProgramParameter object.");
        in.defaultReadObject();

//        propertyChangeListeners_ = new PropertyChangeSupport(this);
//        vetoableChangeListeners_ = new VetoableChangeSupport(this);
    }

    /**
     Removes this PropertyChangeListener.  If the PropertyChangeListener is not on the list, nothing is done.
     @param  listener  The PropertyChangeListener.
     **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Removing property change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        if (propertyChangeListeners_ != null) propertyChangeListeners_.removePropertyChangeListener(listener);
    }

    /**
     Removes this VetoableChangeListener.  If the VetoableChangeListener is not on the list, nothing is done.
     @param  listener  The VetoableChangeListener.
     **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Removing vetoable change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        if (vetoableChangeListeners_ != null) vetoableChangeListeners_.removeVetoableChangeListener(listener);
    }

    /**
     Sets the parameter data that will be sent to the program.
     @param  inputData  The parameter data to be used as input to the program.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setInputData(byte[] inputData) throws PropertyVetoException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Setting input data:", inputData);
        byte[] oldValue = inputData_;
        byte[] newValue = inputData;
        if (vetoableChangeListeners_ != null) vetoableChangeListeners_.fireVetoableChange("inputData", oldValue, newValue);
        nullParameter_ = false;
        inputData_ = inputData;
        if (propertyChangeListeners_ != null) propertyChangeListeners_.firePropertyChange("inputData", oldValue, newValue);
    }

    /**
     Sets the parameter to null.  Calling this method will clear any set input data or output data length.  Setting input data or an output data length will make the parameter not null.
     @param  nullParameter  The parameter data to be used as input to the program.
     **/
    public void setNullParameter(boolean nullParameter)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting null parameter:", nullParameter);
        nullParameter_ = nullParameter;
        inputData_ = null;
        outputDataLength_ = 0;
    }

    // Sets the parameter data that has been received from the program.
    // @param  outputData  The data to be returned from the program.
    void setOutputData(byte[] outputData)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Setting output data:", outputData);
        outputData_ = outputData;
    }

    /**
     Sets the output parameter data length.
     @param  outputDataLength  The amount of data to be returned from the program (number of bytes).
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setOutputDataLength(int outputDataLength) throws PropertyVetoException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Setting output data length:", outputDataLength);
        if (outputDataLength < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'outputDataLength' is not valid:", outputDataLength);
            throw new ExtendedIllegalArgumentException("outputDataLength (" + outputDataLength + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        Integer oldValue = null;
        Integer newValue = null;
        if (vetoableChangeListeners_ != null || propertyChangeListeners_ != null)
        {
          oldValue = new Integer(outputDataLength_);
          newValue = new Integer(outputDataLength);
        }
        if (vetoableChangeListeners_ != null) vetoableChangeListeners_.fireVetoableChange("outputDataLength", oldValue, newValue);
        nullParameter_ = false;
        outputDataLength_ = outputDataLength;
        if (propertyChangeListeners_ != null) propertyChangeListeners_.firePropertyChange("outputDataLength", oldValue, newValue);
    }

    /**
     Sets the type of program parameter.  The type indicates if the data is pass by reference or pass by value.  The type attribute is used by ServiceProgramCall.
     @param  parameterType  The type of the program parameter.  The type must be one of the following:
     <ul>
     <li>{@link #PASS_BY_VALUE PASS_BY_VALUE} - The parameter is passed as data.
     <li>{@link #PASS_BY_REFERENCE PASS_BY_REFERENCE} - The parameter is passed as a reference.
     </ul>
     The default is PASS_BY_VALUE.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setParameterType(int parameterType) throws PropertyVetoException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Setting parameter type:", parameterType);
        if (parameterType < PASS_BY_VALUE || parameterType > PASS_BY_REFERENCE)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'parameterType' is not valid:", parameterType);
            throw new ExtendedIllegalArgumentException("parameterType (" + parameterType + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        Integer oldValue = null;
        Integer newValue = null;
        if (vetoableChangeListeners_ != null || propertyChangeListeners_ != null)
        {
          oldValue = new Integer(parameterType_);
          newValue = new Integer(parameterType);
        }
        if (vetoableChangeListeners_ != null) vetoableChangeListeners_.fireVetoableChange("parameterType", oldValue, newValue);
        parameterType_ = parameterType;
        if (propertyChangeListeners_ != null) propertyChangeListeners_.firePropertyChange("parameterType", oldValue, newValue);
    }
}
