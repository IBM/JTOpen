///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JobList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import com.ibm.as400.resource.ResourceException;
import com.ibm.as400.resource.RJob;
import com.ibm.as400.resource.RJobList;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Enumeration;



/**
The JobList class represents a list of AS/400 jobs.

<p>Some of the selections have associated get and set methods
defined in this class.  These are provided for backwards compatibility
with previous versions of the AS/400 Toolbox for Java.  The complete
set of selections can be accessed using the
{@link com.ibm.as400.resource.RJobList RJobList } class.


@see com.ibm.as400.resource.RJobList
**/
public class JobList
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;


//-----------------------------------------------------------------------------------------
// Public constants.
//-----------------------------------------------------------------------------------------

/**
Constant indicating that all jobs are returned.
**/
    public static final String ALL = RJobList.ALL;


/**
Constant indicating that a blank value is used.
**/
    public static final String BLANK = RJobList.BLANK;


/**
Constant indicating that the current value is used.
**/
    public static final String CURRENT = RJobList.CURRENT;




//-----------------------------------------------------------------------------------------
// Private data.
//-----------------------------------------------------------------------------------------

    private RJobList rJobList_;

    private transient   PropertyChangeSupport   propertyChangeSupport_;
    private transient   VetoableChangeSupport   vetoableChangeSupport_;



//-----------------------------------------------------------------------------------------
// Code.
//-----------------------------------------------------------------------------------------

/**
Constructs a JobList object.
**/
    public JobList()
    {
        rJobList_ = new RJobList();
        initializeTransient();
    }



/**
Constructs a JobList object.

@param system   The system.
**/
    public JobList(AS400 system)
    {
        rJobList_ = new RJobList(system);
        initializeTransient();
    }



/**
Adds a PropertyChangeListener.  The specified PropertyChangeListener's
<b>propertyChange()</b> method will be called each time the value of
any bound property is changed.

@param listener The listener.
*/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport_.addPropertyChangeListener(listener);
        rJobList_.addPropertyChangeListener(listener);
    }



/**
Adds a VetoableChangeListener.  The specified VetoableChangeListener's
<b>vetoableChange()</b> method will be called each time the value of
any constrained property is changed.

@param listener The listener.
*/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        vetoableChangeSupport_.addVetoableChangeListener(listener);
        rJobList_.addVetoableChangeListener(listener);
    }



/**
Returns the list of jobs in the job list.

@return An Enumeration of <a href="Job.html">Job</a> objects.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception ServerStartupException          If the AS/400 server cannot be started.
@exception UnknownHostException            If the AS/400 system cannot be located.
**/
    public Enumeration getJobs ()
       throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectDoesNotExistException
    {
        try {
            rJobList_.refreshContents(); // @C1A
            return new EnumerationAdapter(rJobList_.resources());
        }
        catch(ResourceException e) {
            e.unwrap();
            return null;
        }
    }



/**
Returns the number of jobs in the list.

@return The number of jobs, or 0 if no list has been retrieved.
**/
    public int getLength()
    {
        try {
            rJobList_.waitForComplete();
            return (int)rJobList_.getListLength();
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "An error occurred while getting the job log length", e);
            return 0;
        }
    }



/**
Returns the job name that describes which jobs are returned.

@return The job name.

@see com.ibm.as400.resource.RJobList#JOB_NAME
**/
    public String getName()
    {
        return getSelectionValueAsString(RJobList.JOB_NAME);
    }



/**
Returns the job number that describes which jobs are returned.

@return The job number.

@see com.ibm.as400.resource.RJobList#JOB_NUMBER
**/
    public String getNumber()
    {
        return getSelectionValueAsString(RJobList.JOB_NUMBER);
    }



/*-------------------------------------------------------------------------
Convenience method for getting selection values.
All ResourceExceptions are swallowed!
-------------------------------------------------------------------------*/
    private String getSelectionValueAsString(Object selectionID)
    {
        try {
            return (String)rJobList_.getSelectionValue(selectionID);
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error getting selection value", e);
            return null;
        }
    }



/**
Returns the system.

@return The system.
**/
    public AS400 getSystem()
    {
        return rJobList_.getSystem();
    }



/**
Returns the user name that describes which jobs are returned.

@return The user name.

@see com.ibm.as400.resource.RJobList#USER_NAME
**/
    public String getUser()
    {
        return getSelectionValueAsString(RJobList.USER_NAME);
    }



/**
Initializes the transient data.
**/
    private void initializeTransient()
    {
        propertyChangeSupport_      = new PropertyChangeSupport(this);
        vetoableChangeSupport_      = new VetoableChangeSupport(this);
    }



/**
Deserializes the resource.
**/
    private void readObject(ObjectInputStream in)
    throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        initializeTransient ();
    }




/**
Removes a PropertyChangeListener.

@param listener The listener.
**/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport_.removePropertyChangeListener(listener);
        rJobList_.removePropertyChangeListener(listener);
    }



/**
Removes a VetoableChangeListener.

@param listener The listener.
**/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        vetoableChangeSupport_.removeVetoableChangeListener(listener);
        rJobList_.removeVetoableChangeListener(listener);
    }



/**
Sets the job name that describes which jobs are returned.
The default is ALL.  This takes effect the next time the
list of jobs is retrieved or refreshed.

@param name The job name, or ALL for all job names.

@exception PropertyVetoException If the change is vetoed.

@see com.ibm.as400.resource.RJobList#JOB_NAME
**/
    public void setName(String name)
        throws PropertyVetoException
    {
        if (name == null)
            throw new NullPointerException("name");

        String oldValue = getName();
        vetoableChangeSupport_.fireVetoableChange("name", oldValue, name);
        setSelectionValueAsString(RJobList.JOB_NAME, name);
        propertyChangeSupport_.firePropertyChange("name", oldValue, name);

    }


/**
Sets the job number that describes which jobs are returned.
The default is ALL.  This takes effect the next time the
list of jobs is retrieved or refreshed.

@param number The job number, or ALL for all job numbers.

@exception PropertyVetoException If the change is vetoed.

@see com.ibm.as400.resource.RJobList#JOB_NUMBER
**/
    public void setNumber(String number)
        throws PropertyVetoException
    {
        if (number == null)
            throw new NullPointerException("number");

        String oldValue = getNumber();
        vetoableChangeSupport_.fireVetoableChange("number", oldValue, number);
        setSelectionValueAsString(RJobList.JOB_NUMBER, number);
        propertyChangeSupport_.firePropertyChange("number", oldValue, number);

    }



/*-------------------------------------------------------------------------
Convenience method for setting selection values.
All ResourceExceptions are swallowed!
-------------------------------------------------------------------------*/
    private void setSelectionValueAsString(Object selectionID, Object value)
    {
        try {
            rJobList_.setSelectionValue(selectionID, value);
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error setting selection value", e);
        }
    }



/**
Sets the system.  This cannot be changed if the object
has established a connection to the AS/400.

@param system The system.

@exception PropertyVetoException    If the property change is vetoed.
**/
    public void setSystem(AS400 system)
    throws PropertyVetoException
    {
        rJobList_.setSystem(system);
    }



/**
Sets the user name value that describes which jobs are returned.
The default is ALL.  This takes effect the next time the
list of jobs is retrieved or refreshed.

@param user The user name, or ALL for all user names.

@exception PropertyVetoException If the change is vetoed.

@see com.ibm.as400.resource.RJobList#USER_NAME
**/
    public void setUser(String user)
        throws PropertyVetoException
    {
        if (user == null)
            throw new NullPointerException("user");

        String oldValue = getUser();
        vetoableChangeSupport_.fireVetoableChange("user", oldValue, user);
        setSelectionValueAsString(RJobList.USER_NAME, user);
        propertyChangeSupport_.firePropertyChange("user", oldValue, user);
    }


/**
Converts the Enumeration (whose elements are RQueuedMessage objects)
to an Enumeration whose elements are QueuedMessage objects.
**/
    private static class EnumerationAdapter implements Enumeration
    {
       private Enumeration rEnum_;

       public EnumerationAdapter(Enumeration rEnum)
       {
           rEnum_ = rEnum;
       }

       public boolean hasMoreElements()
       {
           return rEnum_.hasMoreElements();
       }

       public Object nextElement()
       {
           return new Job((RJob)rEnum_.nextElement());
       }
    }



}

