///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  BaseDataQueue.java
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
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 The BaseDataQueue class represents an iSeries server data queue object.
 **/
public abstract class BaseDataQueue implements Serializable
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;

    // The server where the data queue is located.
    private AS400 system_ = null;
    // The full IFS path name of the data queue.
    private String path_ = "";
    // The library that contains the data queue.
    private String library_ = "";
    // The name of the data queue.
    private String name_ = "";

    // The CCSID to be used for String conversions of entry data.
    private int ccsid_ = 0;
    // The Converter object for the above CCSID.
    private transient Converter dataConverter_ = null;

    // The maximum length of each entry on the data queue.
    int maxEntryLength_ = 0;
    // Whether information about the origin of each entry is saved.
    boolean saveSenderInformation_ = false;
    // If entries on the queue are read in LIFO or FIFO order.
    boolean FIFO_ = false;   // False = LIFO.
    // Whether data is forced to auxillary storage.
    boolean forceToAuxiliaryStorage_ = false;
    // The text description for the data queue.
    String description_ = null;
    // Whether the attributes of the data queue have been retrieved.
    boolean attributesRetrieved_ = false;

    // This variable is used to determine if a connection has been made.
    // If so, changes to the queue or system are not allowed.
    // If not, changes are allowed.
    transient BaseDataQueueImpl impl_ = null;

    // List of data queue event bean listeners.
    transient Vector dataQueueListeners_ = null;  // Set on first add.
    // List of object event bean listeners.
    transient Vector objectListeners_ = null;  // Set on first add.
    // List of property change event bean listeners.
    private transient PropertyChangeSupport propertyChangeListeners_ = null;  // Set on first add.
    // List of vetoable change event bean listeners.
    private transient VetoableChangeSupport vetoableChangeListeners_ = null;  // Set on first add.

    /**
     Constructs a BaseDataQueue object.  The system and path properties must be set before using any method requiring a connection to the server.
     **/
    public BaseDataQueue()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing BaseDataQueue object.");
    }

    /**
     Constructs a BaseDataQueue object. It uses the specified system and path.
     @param  system  The system object representing the server on which the data queue exists.
     @param  path  The fully qualified integrated file system path name of the data queue.
     **/
    public BaseDataQueue(AS400 system, String path)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing BaseDataQueue object, system: " + system + " path: " + path);

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

    /**
     Adds a listener to be notified when a data queue event occurs.
     @param  listener  The listener object.
     **/
    public void addDataQueueListener(DataQueueListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding data queue listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        synchronized (this)
        {
            // If first add.
            if (dataQueueListeners_ == null)
            {
                dataQueueListeners_ = new Vector();
            }
            dataQueueListeners_.addElement(listener);
        }
    }

    /**
     Adds a listener to be notified when an object event occurs.
     @param  listener  The listener object.
     **/
    public void addObjectListener(ObjectListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding object listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        synchronized (this)
        {
            // If first add.
            if (objectListeners_ == null)
            {
                objectListeners_ = new Vector();
            }
            objectListeners_.addElement(listener);
        }
    }

    /**
     Adds a listener to be notified when the value of any bound property changes.
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
     Adds a listener to be notified when the value of any constrained property changes.
     @param  listener  The listener object.
     **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding vetoable change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        synchronized (this)
        {
            // If first add.
            if (vetoableChangeListeners_ == null)
            {
                vetoableChangeListeners_ = new VetoableChangeSupport(this);
            }
            vetoableChangeListeners_.addVetoableChangeListener(listener);
        }
    }

    // Connects to the server and retrieves queue attributes, if needed.
    synchronized void open() throws AS400SecurityException, IOException
    {
        // Connect to data queue server.
        boolean opened = impl_ == null;
        chooseImpl();
        if (opened && objectListeners_ != null) fireObjectEvent(ObjectEvent.OBJECT_OPENED);
    }

    // Connects to the server and retrieves client/server attributes.
    synchronized void chooseImpl() throws AS400SecurityException, IOException
    {
        if (system_ != null) system_.signon(false);

        // Set implementation object if not already set.
        if (impl_ == null)
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
            // Have the system object load the appropriate implementation object.
            impl_ = (BaseDataQueueImpl)system_.loadImpl3("com.ibm.as400.access.BaseDataQueueImplNative", "com.ibm.as400.access.BaseDataQueueImplRemote", "com.ibm.as400.access.BaseDataQueueImplProxy");

            // Set the fixed properties in the implementation object.
            impl_.setSystemAndPath(system_.getImpl(), path_, name_, library_);
        }
    }

    /**
     Removes all entries from the data queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public void clear() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Clearing data queue.");
        open();
        // Send clear request.
        impl_.clear(null);
        if (dataQueueListeners_ != null) fireDataQueueEvent(DataQueueEvent.DQ_CLEARED);
    }

    /**
     Deletes the data queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public void delete() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Deleting data queue.");
        // Don't go through open, so no open event is signaled.
        chooseImpl();

        // Send delete request.
        impl_.delete();
        // Indicate that the existing attribute instance variables are invalid since the data queue no longer exists.  If someone re-creates it, it may have different attributes.
        attributesRetrieved_ = false;
        if (objectListeners_ != null) fireObjectEvent(ObjectEvent.OBJECT_DELETED);
    }

    /**
     Checks to see if the data queue exists.
     @return  true if the data queue exists; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     **/
    public boolean exists() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking data queue existence.");

        // Don't use the cached attributes (in case another app / object deletes the queue).
        attributesRetrieved_ = false;
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

    // Fire data queue events.
    void fireDataQueueEvent(int id)
    {
        Vector targets = (Vector)dataQueueListeners_.clone();
        DataQueueEvent event = new DataQueueEvent(this, id);
        for (int i = 0; i < targets.size(); ++i)
        {
            DataQueueListener target = (DataQueueListener)targets.elementAt(i);
            switch (id)
            {
                case DataQueueEvent.DQ_CLEARED:
                    target.cleared(event);
                    break;
                case DataQueueEvent.DQ_PEEKED:
                    target.peeked(event);
                    break;
                case DataQueueEvent.DQ_READ:
                    target.read(event);
                    break;
                case DataQueueEvent.DQ_WRITTEN:
                    target.written(event);
                    break;
            }
        }
    }

    // Fire object events.
    void fireObjectEvent(int id)
    {
        Vector targets = (Vector)objectListeners_.clone();
        ObjectEvent event = new ObjectEvent(this);
        for (int i = 0; i < targets.size(); i++)
        {
            ObjectListener target = (ObjectListener)targets.elementAt(i);
            switch (id)
            {
                case ObjectEvent.OBJECT_CREATED:
                    target.objectCreated(event);
                    break;
                case ObjectEvent.OBJECT_DELETED:
                    target.objectDeleted(event);
                    break;
                case ObjectEvent.OBJECT_OPENED:
                    target.objectOpened(event);
                    break;
            }
        }
    }

    /**
     Returns the CCSID used for the data in this data queue.
     @return  The CCSID used for the data in this data queue.  If the CCSID has not been set, zero (0) is returned.
     **/
    public int getCcsid()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting CCSID:", ccsid_);
        return ccsid_;
    }

    /**
     Returns the text description of the data queue.
     @return  The text description of the data queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public String getDescription() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting description.");
        open();
        if (!attributesRetrieved_)
        {
            retrieveAttributes();
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Description: " + description_);
        return description_;
    }

    /**
     Returns a value that indicates if entries are forced to auxiliary storage.  If true, entries are immediately written to permanent storage.  If false, written entries may be kept in memory and could be lost in the case of a power outage.
     @return  true if entries are immediately written to permanent storage; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public boolean getForceToAuxiliaryStorage() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting force to auxiliary storage.");
        open();
        if (!attributesRetrieved_)
        {
            retrieveAttributes();
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Force to auxiliary storage:", forceToAuxiliaryStorage_);
        return forceToAuxiliaryStorage_;
    }

    /**
     Returns the maximum entry length of the data queue.
     @return  The maximum entry length of the data queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public int getMaxEntryLength() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting max entry length.");
        open();
        if (!attributesRetrieved_)
        {
            retrieveAttributes();
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Max entry length:", maxEntryLength_);
        return maxEntryLength_;
    }

    /**
     Returns the name of the data queue.
     @return  The data queue name, or an empty string ("") if not set.
     **/
    public String getName()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting name: " + name_);
        return name_;
    }

    /**
     Returns the full integrated file system path name of the data queue.
     @return  The fully-qualified data queue name, or an empty string ("") if not set.
     **/
    public String getPath()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting path: " + path_);
        return path_;
    }

    /**
     Returns whether sender information is stored with each data queue entry.
     @return  true if sender information is saved; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public boolean getSaveSenderInformation() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting save sender information.");
        open();
        if (!attributesRetrieved_)
        {
            retrieveAttributes();
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Save sender information:", saveSenderInformation_);
        return saveSenderInformation_;
    }

    /**
     Returns the system object representing the server on which the data queue exists.
     @return  The system object representing the server on which the data queue exists.  If the system has not been set, null is returned.
     **/
    public AS400 getSystem()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system: " + system_);
        return system_;
    }

    /**
     Returns a value that indicates if entries are read in FIFO order.  Returns true if entries are read off the data queue in FIFO order.  Returns false if entries are read off the data queue in LIFO order.
     @return  true if entries are read off the data queue in FIFO order; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public boolean isFIFO() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if data queue is FIFO.");
        open();
        if (!attributesRetrieved_)
        {
            retrieveAttributes();
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "FIFO:", FIFO_);
        return FIFO_;
    }

    /**
     Refreshes the attributes of the data queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  IllegalObjectTypeException  If the object on the server is not the required type.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public void refreshAttributes() throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Refreshing data queue attributes.");
        attributesRetrieved_ = false;
        open();
        retrieveAttributes();
    }

    /**
     Removes a data queue listener.
     @param  listener  The listener object.
     **/
    public void removeDataQueueListener(DataQueueListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing data queue listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        // If we have listeners.
        if (dataQueueListeners_ != null)
        {
            dataQueueListeners_.removeElement(listener);
        }
    }

    /**
     Removes a object listener.
     @param  listener  The listener object.
     **/
    public void removeObjectListener(ObjectListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing object listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        // If we have listeners.
        if (objectListeners_ != null)
        {
            objectListeners_.removeElement(listener);
        }
    }

    /**
     Removes a property change listener.
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
     Removes a vetoable change listener.
     @param  listener  The listener object.
     **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing vetoable change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        // If we have listeners.
        if (vetoableChangeListeners_ != null)
        {
            vetoableChangeListeners_.removeVetoableChangeListener(listener);
        }
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
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting CCSID:", ccsid);

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            ccsid_ = ccsid;
            dataConverter_ = null;  // Converter no longer points to correct ccsid.
        }
        else
        {
            Integer oldValue = new Integer(ccsid_);
            Integer newValue = new Integer(ccsid);

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("ccsid", oldValue, newValue);
            }

            ccsid_ = ccsid;
            dataConverter_ = null;  // Converter no longer points to correct ccsid.

            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("ccsid", oldValue, newValue);
            }
        }
    }

    /**
     Sets the fully qualified integrated file system path name of the data queue.
     @param  path  The fully qualified integrated file system path name of the data queue.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setPath(String path) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting path: " + path);

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

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            // Set instance variables.
            library_ = ifs.getLibraryName();
            name_ = ifs.getObjectName();
            path_ = path;
        }
        else
        {
            String oldValue = path_;
            String newValue = path;

            // Ask for any vetos.
            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("path", oldValue, newValue);
            }

            // Set instance variables.
            library_ = ifs.getLibraryName();
            name_ = ifs.getObjectName();
            path_ = path;

            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("path", oldValue, newValue);
            }
        }
    }

    /**
     Sets the system object representing the server on which the data queue exists.
     @param  system  The system object representing the server on which the data queue exists.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setSystem(AS400 system) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting system: " + system);

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

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            // Set instance variable.
            system_ = system;
        }
        else
        {
            AS400 oldValue = system_;
            AS400 newValue = system;

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("system", oldValue, newValue);
            }

            // Set instance variable.
            system_ = system;

            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("system", oldValue, newValue);
            }
        }
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
