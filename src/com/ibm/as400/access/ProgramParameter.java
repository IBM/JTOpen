///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ProgramParameter.java
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
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 The ProgramParameter class is used with ProgramCall and ServiceProgramCall to pass parameter data to an AS/400 program, from an AS/400 program, or both.  Input data is passed to an AS/400 program as a byte array with <i>setInputData</i>.  Output data is requested from an AS/400 program by specifying the amount of data to return with <i>setOutputDataLength</i>.  To get the output data once the AS/400 program has run use <i>getOutputData</i>.  These values may also be set on the constructor.
 @see  ProgramCall
 @see  ServiceProgramCall
 **/
public class ProgramParameter implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    /**
     Constant indicating parameter data is passed by value.
     **/
    public static final int PASS_BY_VALUE = 1;

    /**
     Constant indicating parameter data is passed by reference.
     **/
    public static final int PASS_BY_REFERENCE = 2;

    // Valid values for parameter type.
    static final int INPUT  = 1;
    static final int OUTPUT = 2;
    static final int INOUT  = 3;

    // Variable representing the type of the service program parameter.
    private int parameterType_ = PASS_BY_VALUE;

    // Parameter data.
    private byte[] inputData_ = null;
    private int outputDataLength_ = 0;
    private byte[] outputData_ = null;

    // Temporary variables to hold information needed to put parameter on datastream.
    // These are transient to prevent increasing serialized size.
    // Values only valid during datastream construction.
    transient int length_ = 0;  // Byte length of parameter information.
    transient int maxLength_ = 0;  // Max length of input and output data.
    transient int usage_ = 0;  // Parameter usage: in, out, inout & no compression, 0-truncation, RLE
    transient byte[] compressedInputData_ = null;  // Input data compressed.

    // List of property change event bean listeners.
    private transient PropertyChangeSupport propertyChangeListeners_ = new PropertyChangeSupport(this);
    // List of vetoable change event bean listeners.
    private transient VetoableChangeSupport vetoableChangeListeners_ = new VetoableChangeSupport(this);

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
        inputData_ = inputData;
    }

    /**
     Constructs a ProgramParameter object.  An output parameter is created, since the size of the output data is passed on this constructor.
     @param  outputDataLength  The amount of data to be returned from the program.
     **/
    public ProgramParameter(int outputDataLength)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing ProgramParameter object, output data length:", outputDataLength);
        if (outputDataLength < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'outputDataLength' is not valid:", outputDataLength);
            throw new ExtendedIllegalArgumentException("outputDataLength (" + outputDataLength + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        outputDataLength_ = outputDataLength;
    }

    /**
     Constructs ProgramParameter object. An input/output parameter is created, since both data passed to the program and the amount of data returned from the program is passed on this constructor.
     @param  inputData  Parameter data passed to the program.
     @param  outputDataLength  The amount of data to be returned from the program.
     **/
    public ProgramParameter(byte[] inputData, int outputDataLength)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing ProgramParameter object, output data length: " + outputDataLength + " input data:", inputData);
        if (outputDataLength < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'outputDataLength' is not valid:", outputDataLength);
            throw new ExtendedIllegalArgumentException("outputDataLength (" + outputDataLength + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        inputData_ = inputData;
        outputDataLength_ = outputDataLength;
    }

    /**
     Constructs a ProgramParameter object.  An input parameter is created, since a byte array containing parameter data is passed on this constructor.  The type indicates if the data is pass by reference or pass by value.  The type attribute is used by ServiceProgramCall.
     @param  parameterType  The type of parameter.
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
        inputData_ = inputData;
    }

    /**
     Constructs a ProgramParameter object.  An output parameter is created, since the size of the output data is passed on this constructor.  The type indicates if the data is pass by reference or pass by value.  The type attribute is used by ServiceProgramCall.
     @param  parameterType  The type of parameter.
     @param  outputDataLength  The amount of data to be returned from the program.
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
        outputDataLength_ = outputDataLength;
    }

    /**
     Constructs ProgramParameter object.  An input/output parameter is created, since both data passed to the program and the amount of data returned from the program is passed on this constructor.  The type indicates if the data is pass by reference or pass by value.  The type attribute is used by ServiceProgramCall.
     @param  parameterType  The type of parameter.
     @param  inputData  The parameter data to be used as input to the program.
     @param  outputDataLength  The amount of data to be returned from the program.
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
        inputData_ = inputData;
        outputDataLength_ = outputDataLength;
    }

    /**
     Adds a PropertyChangeListener.  The specified PropertyChangeListener's <b>propertyChange</b> method will be called each time the value of any bound property is changed.  The PropertyListener object is added to a list of PropertyChangeListeners managed by this ProgramParameter; it can be removed with removePropertyChangeListener.
     @param  listener  The PropertyChangeListener.
     @see  #removePropertyChangeListener
     **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Adding property change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        propertyChangeListeners_.addPropertyChangeListener(listener);
    }

    /**
     Adds a VetoableChangeListener.  The specified VetoableChangeListeners <b>vetoableChange</b> method will be called each time the value of any constrained property is changed.
     @param  listener  The VetoableChangeListener.
     @see  #removeVetoableChangeListener
     **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Adding vetoable change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
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
     Returns the parameter data that has been received from the program.  Null is returned if this parameter is an input parameter.  Null is also returned before the AS/400 program is called.
     @return  The output data returned from the program.
     **/
    public byte[] getOutputData()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting output data:", outputData_);
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
     <UL>
     <LI>PASS_BY_VALUE  The parameter is passed as data.
     <LI>PASS_BY_REFERENCE  The parameter is passed as a reference.
     </UL>
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
        int usage = (inputData_ == null) ? OUTPUT : (outputDataLength_ == 0) ? INPUT : INOUT;
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Parameter usage:", usage);
        return usage;
    }

    // Deserialize and initialize transient data.
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "De-serializing ProgramParameter object.");
        in.defaultReadObject();

        propertyChangeListeners_ = new PropertyChangeSupport(this);
        vetoableChangeListeners_ = new VetoableChangeSupport(this);
    }

    /**
     Removes this PropertyChangeListener.  If the PropertyChangeListener is not on the list, nothing is done.
     @param  listener  The PropertyChangeListener.
     @see  #addPropertyChangeListener
     **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Removing property change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        propertyChangeListeners_.removePropertyChangeListener(listener);
    }

    /**
     Removes this VetoableChangeListener.  If the VetoableChangeListener is not on the list, nothing is done.
     @param  listener  The VetoableChangeListener.
     @see  #addVetoableChangeListener
     **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Removing vetoable change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        vetoableChangeListeners_.removeVetoableChangeListener(listener);
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
        vetoableChangeListeners_.fireVetoableChange("inputData", oldValue, newValue);
        inputData_ = inputData;
        propertyChangeListeners_.firePropertyChange("inputData", oldValue, newValue);
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
        Integer oldValue = new Integer(outputDataLength_);
        Integer newValue = new Integer(outputDataLength);
        vetoableChangeListeners_.fireVetoableChange("outputDataLength", oldValue, newValue);
        outputDataLength_ = outputDataLength;
        propertyChangeListeners_.firePropertyChange("outputDataLength", oldValue, newValue);
    }

    /**
     Sets the type of program parameter.  The type indicates if the data is pass by reference or pass by value.  The type attribute is used by ServiceProgramCall.
     @param  parameterType  The type of the program parameter.  The type must be one of the following:
     <UL>
     <LI>PASS_BY_VALUE  The parameter is passed as data.
     <LI>PASS_BY_REFERENCE  The parameter is passed as a reference.
     </UL>
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
        Integer oldValue = new Integer(parameterType_);
        Integer newValue = new Integer(parameterType);
        vetoableChangeListeners_.fireVetoableChange("parameterType", oldValue, newValue);
        parameterType_ = parameterType;
        propertyChangeListeners_.firePropertyChange("parameterType", oldValue, newValue);
    }
}
