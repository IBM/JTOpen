///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: BaseDataQueue.java
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
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.Vector;

/**
 The BaseDataQueue class represents an AS/400 data queue object.
 **/
public abstract class BaseDataQueue implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;


    //The AS/400 system the data queue is on.
    private AS400 system_ = null;
    //The IFS path name of the data queue.
    private String path_ = "";
    //The library the data queue is in.
    private String library_ = "";
    //The name of the data queue.
    private String name_ = "";

    // The CCSID to be used for String conversions of entry data.
    private int ccsid_ = 0;
    // The Converter object for the above CCSID.
    private transient Converter dataConverter_ = null;

    //The maximum length of each entry on the data queue.
    int maxEntryLength_ = 0;
    //Whether information about the origin of each entry is saved.
    boolean saveSenderInformation_ = false;
    //If entries on the queue are read in LIFO or FIFO order.
    boolean FIFO_ = false;   // False = LIFO.
    //Whether data is forced to auxillary storage.
    boolean forceToAuxiliaryStorage_ = false;
    //The text description for the data queue.
    String description_ = null;
    //Whether the attributes of the data queue have been retrieved.
    boolean attributesRetrieved_ = false;

    // This variable is used to determine if a connection has been made.
    // If so, changes to the queue or system are not allowed.
    // If not, changes are allowed.
    transient BaseDataQueueImpl impl_ = null;

    // List of data queue event bean listeners.
    private transient Vector dataQueueListeners_ = new Vector();
    // List of object event bean listeners.
    private transient Vector objectListeners_ = new Vector();
    // List of property change event bean listeners.
    private transient PropertyChangeSupport propertyChangeListeners_ = new PropertyChangeSupport(this);
    // List of vetoable change event bean listeners.
    private transient VetoableChangeSupport vetoableChangeListeners_ = new VetoableChangeSupport(this);

    /**
     Constructs a BaseDataQueue object.  The system and path properties will need to be set before using any method requiring a connection to the AS/400.
     **/
    public BaseDataQueue()
    {
        super();
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing BaseDataQueue object.");
    }

    /**
     Constructs a BaseDataQueue object. It uses the specified system and path.  Depending on how the AS400 object was constructed, the user may need to be prompted for the system name, user ID, or password when any method requiring a connection to the AS/400 is done.
     @param  system  The AS/400 system on which the data queue exists.
     @param  path  The fully qualified integrated file system path name of the data queue.
     **/
    public BaseDataQueue(AS400 system, String path)
    {
        super();
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing BaseDataQueue object, system: " + system + " path: " + path);

        // Check parameters.
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (path == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'path' is null.");
            throw new NullPointerException("path");
        }
        QSYSObjectPathName ifs = new QSYSObjectPathName(path, "DTAQ");

        // Set instance variables.
        library_ = ifs.getLibraryName();
        name_ = ifs.getObjectName();
        path_ = path;
        system_ = system;
    }

    // Deserializes and initializes transient data.
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "De-serializing BaseDataQueue object.");
        in.defaultReadObject();

        // dataConverter_ can remain null.
        // impl_ can remain null.
        dataQueueListeners_ = new Vector();
        objectListeners_ = new Vector();
        propertyChangeListeners_ = new PropertyChangeSupport(this);
        vetoableChangeListeners_ = new VetoableChangeSupport(this);
    }

    /**
     Adds a listener to be notified when a data queue event occurs.
     @param  listener  The listener.
     **/
    public void addDataQueueListener(DataQueueListener listener)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Adding data queue listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        dataQueueListeners_.addElement(listener);
    }

    /**
     Adds a listener to be notified when an object event occurs.
     @param  listener  The listener.
     **/
    public void addObjectListener(ObjectListener listener)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Adding object listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        objectListeners_.addElement(listener);
    }

    /**
     Adds a listener to be notified when the value of any bound property changes.
     @param  listener  The listener.
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
     Adds a listener to be notified when the value of any constrained property changes.
     @param  listener  The listener.
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

    // Connects to the server and retrieves queue attributes, if needed.
    synchronized void open() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        // Connect to data queue server.
        boolean opened = connect();

        // Get attributes to ensure this is the right type of queue.
        if (!attributesRetrieved_)
        {
            retrieveAttributes();
            if (opened)
            {
                fireOpened();
            }
        }
    }

    // Connects to the server and retrieves client/server attributes.
    synchronized boolean connect() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
    {
        // Verify required attributes have been set.
        if (system_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot connect to data queue server before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        if (path_.length() == 0)
        {
            Trace.log(Trace.ERROR, "Cannot connect to data queue server before setting path.");
            throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        // Set implementation object if not already set.
        boolean firstTime = false;
        if (impl_ == null)
        {
            firstTime = true;
            // Have the system object load the appropriate implementation object.
            impl_ = (BaseDataQueueImpl)system_.loadImpl3("com.ibm.as400.access.BaseDataQueueImplNative", "com.ibm.as400.access.BaseDataQueueImplRemote", "com.ibm.as400.access.BaseDataQueueImplProxy");

            // Set the fixed properties in the implementation object.
            impl_.setSystemAndPath(system_.getImpl(), path_, name_, library_);
        }
        system_.signon(false);

        // Connect to the data queue server.
        impl_.processConnect();
        return firstTime;
    }

    /**
     Removes all entries from the data queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  IllegalObjectTypeException  If the AS/400 object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     @exception  ServerStartupException  If the AS/400 server cannot be started.
     @exception  UnknownHostException  If the AS/400 system cannot be located.
     **/
    public void clear() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Clearing data queue.");
        open();
        // Send clear request.
        impl_.processClear(null);
        fireCleared();
    }

    /**
     Deletes the data queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  IllegalObjectTypeException  If the AS/400 object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     @exception  ServerStartupException  If the AS/400 server cannot be started.
     @exception  UnknownHostException  If the AS/400 system cannot be located.
     **/
    public void delete() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Deleting data queue.");
        // Don't go through open, so no open event is signaled.
        connect();
        // Get attributes to ensure this is the right type of queue.
        if (!attributesRetrieved_)
        {
            retrieveAttributes();
        }

        // Send delete request.
        impl_.processDelete();
        // Indicate that the existing attribute instance variables are invalid since the data queue no longer exists.  If someone re-creates it, it may have different attributes.
        attributesRetrieved_ = false;
        fireDeleted();
    }




    /**
     Checks to see if the data queue exists.
     @return  true if the data queue exists; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  IllegalObjectTypeException  If the AS/400 object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ServerStartupException  If the AS/400 server cannot be started.
     @exception  UnknownHostException  If the AS/400 system cannot be located.
     **/
    // @D1 new method
    public boolean exists() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException
    {
        // Don't use the cached attributes (in case another app / object
        // deletes the queue).
        attributesRetrieved_ = false;

        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Exists called.");
        try
        {
           getMaxEntryLength();
           return true;
        }
        catch (ObjectDoesNotExistException e)
        {
           return false;
        }
    }


    // Fire cleared event.
    void fireCleared()
    {
        Vector targets = (Vector)dataQueueListeners_.clone();
        DataQueueEvent event = new DataQueueEvent(this, DataQueueEvent.DQ_CLEARED);
        for (int i = 0; i < targets.size(); i++)
        {
            DataQueueListener target = (DataQueueListener)targets.elementAt(i);
            target.cleared(event);
        }
    }

    // Fires create event.
    void fireCreated()
    {
        Vector targets = (Vector)objectListeners_.clone();
        ObjectEvent event = new ObjectEvent(this);
        for (int i = 0; i < targets.size(); i++)
        {
            ObjectListener target = (ObjectListener)targets.elementAt(i);
            target.objectCreated(event);
        }
    }

    // Fires delete event.
    void fireDeleted()
    {
        Vector targets = (Vector)objectListeners_.clone();
        ObjectEvent event = new ObjectEvent(this);
        for (int i = 0; i < targets.size(); i++)
        {
            ObjectListener target = (ObjectListener)targets.elementAt(i);
            target.objectDeleted(event);
        }
    }

    // Fires open event.
    void fireOpened()
    {
        Vector targets = (Vector)objectListeners_.clone();
        ObjectEvent event = new ObjectEvent(this);
        for (int i = 0; i < targets.size(); i++)
        {
            ObjectListener target = (ObjectListener)targets.elementAt(i);
            target.objectOpened(event);
        }
    }

    // Fires peek event.
    void firePeek()
    {
        Vector targets = (Vector)dataQueueListeners_.clone();
        DataQueueEvent event = new DataQueueEvent(this, DataQueueEvent.DQ_PEEKED);
        for (int i = 0; i < targets.size(); i++)
        {
            DataQueueListener target = (DataQueueListener)targets.elementAt(i);
            target.peeked(event);
        }
    }

    // Fires read event.
    void fireRead()
    {
        Vector targets = (Vector)dataQueueListeners_.clone();
        DataQueueEvent event = new DataQueueEvent(this, DataQueueEvent.DQ_READ);
        for (int i = 0; i < targets.size(); i++)
        {
            DataQueueListener target = (DataQueueListener)targets.elementAt(i);
            target.read(event);
        }
    }

    // Fires written event.
    void fireWritten()
    {
        Vector targets = (Vector)dataQueueListeners_.clone();
        DataQueueEvent event = new DataQueueEvent(this, DataQueueEvent.DQ_WRITTEN);
        for (int i = 0; i < targets.size(); i++)
        {
            DataQueueListener target = (DataQueueListener)targets.elementAt(i);
            target.written(event);
        }
    }

    /**
     Returns the CCSID used for the data in this data queue.
     @return  The CCSID used for the data in this data queue.  If the CCSID has not been set, zero (0) is returned.
     **/
    public int getCcsid()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting CCSID:", ccsid_);
        return ccsid_;
    }

    /**
     Returns the text description of the data queue.
     @return  The text description of the data queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  IllegalObjectTypeException  If the AS/400 object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     @exception  ServerStartupException  If the AS/400 server cannot be started.
     @exception  UnknownHostException  If the AS/400 system cannot be located.
     **/
    public String getDescription() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting description.");
        open();
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Description: " + description_);
        return description_;
    }

    /**
     Returns a value that indicates if entries are forced to auxiliary storage.  If true, entries are immediately written to permanent storage.  If false, written entries may be kept in memory and could be lost in the case of a power outage.
     @return  true if entries are immediately written to permanent storage; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  IllegalObjectTypeException  If the AS/400 object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     @exception  ServerStartupException  If the AS/400 server cannot be started.
     @exception  UnknownHostException  If the AS/400 system cannot be located.
     **/
    public boolean getForceToAuxiliaryStorage() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting force to auxiliary storage.");
        open();
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Force to auxiliary storage:", forceToAuxiliaryStorage_);
        return forceToAuxiliaryStorage_;
    }

    /**
     Returns the maximum entry length of the data queue.
     @return  The maximum entry length of the data queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  IllegalObjectTypeException  If the AS/400 object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     @exception  ServerStartupException  If the AS/400 server cannot be started.
     @exception  UnknownHostException  If the AS/400 system cannot be located.
     **/
    public int getMaxEntryLength() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting max entry length.");
        open();
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Max entry length:", maxEntryLength_);
        return maxEntryLength_;
    }

    /**
     Returns the name of the data queue.
     @return  The data queue name, or an empty string ("") if not set.
     **/
    public String getName()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting name: " + name_);
        return name_;
    }

    /**
     Returns the full integrated file system path name of the data queue.
     @return  The fully-qualified data queue name, or an empty string ("") if not set.
     **/
    public String getPath()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting path: " + path_);
        return path_;
    }

    /**
     Returns whether sender information is stored with each data queue entry.
     @return  true if sender information is saved; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  IllegalObjectTypeException  If the AS/400 object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     @exception  ServerStartupException  If the AS/400 server cannot be started.
     @exception  UnknownHostException  If the AS/400 system cannot be located.
     **/
    public boolean getSaveSenderInformation() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting save sender information.");
        open();
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Save sender information:", saveSenderInformation_);
        return saveSenderInformation_;
    }

    /**
     Returns the AS400 object representing the system on which the data queue exists.
     @return  The system on which the data queue exists.  If the system has not been set, null is returned.
     **/
    public AS400 getSystem()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting system: " + system_);
        return system_;
    }

    /**
     Returns a value that indicates if entries are read in FIFO order.  Returns true if entries are read off the data queue in FIFO order.  Returns false if entries are read off the data queue in LIFO order.
     @return  true if entries are read off the data queue in FIFO order; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  IllegalObjectTypeException  If the AS/400 object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     @exception  ServerStartupException  If the AS/400 server cannot be started.
     @exception  UnknownHostException  If the AS/400 system cannot be located.
     **/
    public boolean isFIFO() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Checking if data queue is FIFO.");
        open();
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "FIFO:", FIFO_);
        return FIFO_;
    }

    /**
     Refreshes the attributes of the data queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ConnectionDroppedException  If the connection is dropped unexpectedly.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the AS/400.
     @exception  IllegalObjectTypeException  If the AS/400 object is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the AS/400 object does not exist.
     @exception  ServerStartupException  If the AS/400 server cannot be started.
     @exception  UnknownHostException  If the AS/400 system cannot be located.
     **/
    public void refreshAttributes() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Refreshing data queue attributes.");
        attributesRetrieved_ = false;
        open();
    }

    /**
     Removes a data queue listener.
     @param  listener  The listener.
     **/
    public void removeDataQueueListener(DataQueueListener listener)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Removing data queue listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        dataQueueListeners_.removeElement(listener);
    }

    /**
     Removes a object listener.
     @param  listener  The listener.
     **/
    public void removeObjectListener(ObjectListener listener)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Removing object listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        objectListeners_.removeElement(listener);
    }

    /**
     Removes a property change listener.
     @param  listener  The listener.
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
     Removes a vetoable change listener.
     @param  listener  The listener.
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

    // Refreshes the attributes of the data queue.
    abstract void retrieveAttributes() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException;

    /**
     Sets the CCSID to use for the data in this data queue.
     @param  ccsid  The CCSID to use for the data in this data queue.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setCcsid(int ccsid) throws PropertyVetoException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Setting CCSID:", ccsid);

        Integer oldValue = new Integer(ccsid_);
        Integer newValue = new Integer(ccsid);

        vetoableChangeListeners_.fireVetoableChange("ccsid", oldValue, newValue);

        ccsid_ = ccsid;
        dataConverter_ = null;  // Converter no longer points to correct ccsid.

        propertyChangeListeners_.firePropertyChange("ccsid", oldValue, newValue);
    }

    /**
     Sets the fully qualified integrated file system path name of the data queue.
     @param  path  The fully qualified integrated file system path name of the data queue.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setPath(String path) throws PropertyVetoException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Setting path: " + path);

        // Check parameter.
        if (path == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'path' is null.");
            throw new NullPointerException("path");
        }

        // Make sure we have not already connected.
        if (impl_ != null)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'path' after connect.");
            throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        // Verify name is valid IFS path name.
        QSYSObjectPathName ifs = new QSYSObjectPathName(path, "DTAQ");

        // Ask for any vetos.
        String oldValue = path_;
        String newValue = path;

        vetoableChangeListeners_.fireVetoableChange("path", oldValue, newValue);

        // Set instance variables.
        library_ = ifs.getLibraryName();
        name_ = ifs.getObjectName();
        path_ = path;

        propertyChangeListeners_.firePropertyChange("path", oldValue, newValue);
    }

    /**
     Sets the system on which the data queue exists.
     @param  system  The AS/400 system on which the data queue exists.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setSystem(AS400 system) throws PropertyVetoException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Setting system: " + system);

        // Check parameter.
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }

        // Make sure we have not already connected.
        if (impl_ != null)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'system' after connect.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        AS400 oldValue = system_;
        AS400 newValue = system;

        vetoableChangeListeners_.fireVetoableChange("system", oldValue, newValue);

        // Set instance variable.
        system_ = system;

        propertyChangeListeners_.firePropertyChange("system", oldValue, newValue);
    }

    /**
     Returns the String representation of this data queue object.
     @return  The String representation of this data queue object.
     **/
    public String toString()
    {
        return "(system: " + system_ + " path: "+ path_ + "):" + super.toString();
    }

    // Get the CCSID to be used for this data queue, get it from the system object if not already set.
    private int getCcsidToUse()
    {
        if (ccsid_ == 0) return system_.getCcsid();
        return ccsid_;
    }

    byte[] stringToByteArray(String string) throws IOException
    {
        if (dataConverter_ == null) dataConverter_ = new Converter(getCcsidToUse(), system_);
        return dataConverter_.stringToByteArray(string);
    }

    String byteArrayToString(byte[] bytes) throws UnsupportedEncodingException
    {
        if (dataConverter_ == null) dataConverter_ = new Converter(getCcsidToUse(), system_);
        return dataConverter_.byteArrayToString(bytes);
    }
}
