///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: EnvironmentVariableList.java
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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;



/**
The EnvironmentVariableList class represents a list of 
system-level AS/400 environment variables.   

<p>This class can only access system-level environment variables
on V4R4 or later AS/400s.  You must have *JOBCTL special authority
to access system-level environment variables.

<p>This example gets the list of environment variables as a 
java.util.Properties object:
<pre>
AS400 system = new AS400("mysystem");
EnvironmentVariableList evList = new EnvironmentVariableList(system);
Properties p = evList.getProperties();
</pre>

<p>This example uses an Enumeration to print the list of environment 
variable names and values:
<pre>
AS400 system = new AS400("mysystem");
EnvironmentVariableList evList = new EnvironmentVariableList(system);
Enumeration enum = evList.getEnvironmentVariables();
while(enum.hasMoreElements())
{
    EnvironmentVariable ev = (EnvironmentVariable)enum.nextElement();
    System.out.println(ev.getName() + "=" + ev.getValue());
}
</pre>

@see EnvironmentVariable
**/
//
// Implementation note:  
//
// 1.  There is currently no API to get a list of job-level
//     environment variables.  Mike Mundy said this is not
//     available since C programs can get the list via the
//     ENVIRON environment variable.  
//
// 2.  The reason for the V4R4 and later limitation is that 
//     ServiceProgramCall only works to V4R4 and later.
//
public class EnvironmentVariableList 
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;


//-----------------------------------------------------------------------------------------
// Private data.
//-----------------------------------------------------------------------------------------

    private static final int                        INITIAL_CCSID_BUFFER_SIZE_  = 1024;
    private static final int                        INITIAL_LIST_BUFFER_SIZE_   = 4096;

    private AS400                                   system_                     = null;

    private transient EnvironmentVariableHelper     helper_                     = null;
    private transient PropertyChangeSupport         propertyChangeSupport_      = null;



/**
Constructs a EnvironmentVariableList object.
**/
    public EnvironmentVariableList()
    {          
        initializeTransient();
    }
 


/**
Constructs a EnvironmentVariableList object.

@param system   The system.
**/
    public EnvironmentVariableList(AS400 system)
    {
        initializeTransient();
        setSystem(system);
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
Returns an enumeration that contains an EnvironmentVariable object
for each environment variable on the system.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public Enumeration getEnvironmentVariables()
    throws AS400SecurityException, 
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
    {
        if (system_ == null)
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        if (helper_ == null)
            helper_ = new EnvironmentVariableHelper(system_);

        ProgramParameter[] parameters;
        parameters = new ProgramParameter[5];
        parameters[0] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, new byte[1], INITIAL_LIST_BUFFER_SIZE_);
        parameters[1] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, BinaryConverter.intToByteArray(INITIAL_LIST_BUFFER_SIZE_), 4);
        parameters[2] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, new byte[1], INITIAL_CCSID_BUFFER_SIZE_);
        parameters[3] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, BinaryConverter.intToByteArray(INITIAL_CCSID_BUFFER_SIZE_), 4);
        parameters[4] = new ProgramParameter(null);
        int rv = helper_.callServiceProgramInt("Qp0zGetAllSysEnv", parameters, 
                                               EnvironmentVariableHelper.RV_ENOSPC_, 
                                               EnvironmentVariableHelper.RV_ENOENT_);

        // If the size came back as ENOSPC, then call it again with the correct 
        // amount of bytes.
        int actualListBufferSize = BinaryConverter.byteArrayToInt(parameters[1].getOutputData(), 0);
        int actualCcsidBufferSize = BinaryConverter.byteArrayToInt(parameters[3].getOutputData(), 0);
        if (rv == EnvironmentVariableHelper.RV_ENOSPC_) {
            if (Trace.isTraceOn())
                Trace.log(Trace.INFORMATION, "ENOSPC returned, getting ev list again with " 
                          + actualListBufferSize + " bytes for the list and " + actualCcsidBufferSize
                          + " bytes for the ccsids.");
            parameters[0] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, new byte[1], actualListBufferSize);
            parameters[1] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, BinaryConverter.intToByteArray(actualListBufferSize), 4);
            parameters[2] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, new byte[1], actualCcsidBufferSize);
            parameters[3] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, BinaryConverter.intToByteArray(actualCcsidBufferSize), 4);
            helper_.callServiceProgramInt("Qp0zGetAllSysEnv", parameters, 0);
        }
        
        // If ENOENT is returned, then there are no environment variables.
        else if (rv == EnvironmentVariableHelper.RV_ENOENT_) {
            Vector asVector = new Vector();
            return asVector.elements();
        }

        // Build up a Vector of EnvironmentVariable objects.
        Vector asVector = new Vector();
        // @A1D int i = 0;
        int offsetIntoListBuffer = 0;
        int offsetIntoCcsidBuffer = 0;
        byte[] listBufferBytes = parameters[0].getOutputData();
        byte[] ccsidBufferBytes = parameters[2].getOutputData();

        //System.out.print("List buffer bytes:");
        //for(int k=0; k < 100; ++k)
        //    System.out.print("[" + listBufferBytes[k] + "]");
        //System.out.println();
        //System.out.print("CCSID buffer bytes:");
        //for(int k=0; k < 100; ++k)
        //    System.out.print("[" + ccsidBufferBytes[k] + "]");
        //System.out.println();

        while(offsetIntoListBuffer < actualListBufferSize) {

            // Find the next null.
            int nextNull = offsetIntoListBuffer;
            while(listBufferBytes[nextNull] != 0)
                ++nextNull;

            // If the next null is only 1 greater than the previous,
            // then we are done.
            if (nextNull == offsetIntoListBuffer)
                break;

            // Get the name=value expression.
            int nextCcsid = BinaryConverter.byteArrayToInt(ccsidBufferBytes, offsetIntoCcsidBuffer);
            Converter converter = new Converter((nextCcsid == 0) ? system_.getCcsid() : nextCcsid, system_);
            
            // Create the EnvironmentVariable object.
            int i = offsetIntoListBuffer;                                                   // @A1A
            while(listBufferBytes[i] != (byte)0x7E) // Ebcdic equals                        // @A1A
                ++i;                                                                        // @A1A
            byte[] name = new byte[i - offsetIntoListBuffer];                               // @A1A
            System.arraycopy(listBufferBytes, offsetIntoListBuffer, name, 0, i - offsetIntoListBuffer);   // @A1A
            byte[] value = new byte[nextNull - i - 1];                                      // @A1A
            System.arraycopy(listBufferBytes, i + 1, value, 0, nextNull - i - 1);           // @A1A
            asVector.addElement(new EnvironmentVariable(system_, name, helper_, value, nextCcsid));    // @A1C

            // Get ready for the next iteration.
            offsetIntoListBuffer = nextNull + 1;
            offsetIntoCcsidBuffer += 4;
        }

        return asVector.elements();
    }



// @A1A
/**
Returns a new Properties object which contains an entry for
each environment variable in the list.  
    
@return The new Properties object.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public Properties getProperties()
    throws AS400SecurityException, 
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
    {
        Properties properties = new Properties();
        Enumeration enum = getEnvironmentVariables(); 
        while(enum.hasMoreElements()) {
            EnvironmentVariable environmentVariable = (EnvironmentVariable)enum.nextElement();
            properties.put(environmentVariable.getName(), environmentVariable.getValue());
        }
        return properties;
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
Initializes transient data.
**/
    private void initializeTransient ()
    {
        propertyChangeSupport_  = new PropertyChangeSupport(this);
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
Sets the value of each environment variable defined
in a Properties object.

@param properties   The Properties object.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public void setProperties(Properties properties)
    throws AS400SecurityException, 
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
    {
        if (system_ == null)                                                                                   
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        setProperties(properties, AS400BidiTransform.getStringType((char)system_.getCcsid())); // @A1C
    }



// @A1A
/**
Sets the value of each environment variable defined
in a Properties object.

@param properties   The Properties object.
@param type The environment variable bidi string type, as defined by the CDRA (Character 
      Data Representataion Architecture). See {@link com.ibm.as400.access.BidiStringType BidiStringType} for more information and valid values. 
      
@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
**/
    public void setProperties(Properties properties, int type)
    throws AS400SecurityException, 
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
    {
        if (properties == null)
            throw new NullPointerException("properties");
        if (system_ == null)
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        if (helper_ == null)
            helper_ = new EnvironmentVariableHelper(system_);

        Enumeration propertyNames = properties.propertyNames();
        while(propertyNames.hasMoreElements()) {
            String propertyName = (String)propertyNames.nextElement();
            EnvironmentVariable environmentVariable = new EnvironmentVariable(system_, propertyName, helper_);
            environmentVariable.setValue(properties.getProperty(propertyName), 0, type); // @A1C
        }
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
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        AS400 oldValue = system_;
        system_ = system;
        propertyChangeSupport_.firePropertyChange("system", oldValue, system);
    }




}
