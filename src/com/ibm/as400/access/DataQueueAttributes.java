///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DataQueueAttributes.java
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
 The DataQueueAttributes class represents an AS/400 data queue attributes object.
 **/
public class DataQueueAttributes implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;


    // The public authority of the data queue.
    private String authority_ = "*LIBCRTAUT";
    //The text description for the data queue.
    private String description_ = "";
    //The maximum length of each entry on the data queue.
    private int entryLength_ = 1000;
    //If entries on the queue are read in LIFO or FIFO order.
    private boolean FIFO_ = true;   // False = LIFO.
    //Whether data is forced to auxillary storage.
    private boolean forceToAuxiliaryStorage_ = false;
    // The byte length of the keys in the data queue.
    private int keyLength_ = 0;
    //Whether information about the origin of each entry is saved.
    private boolean saveSenderInfo_ = false;

    // List of property change event bean listeners.
    private transient PropertyChangeSupport propertyChangeListeners_ = new PropertyChangeSupport(this);
    // List of vetoable change event bean listeners.
    private transient VetoableChangeSupport vetoableChangeListeners_ = new VetoableChangeSupport(this);

    // Deserializes and initializes the transient data.
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "De-serializing DataQueueAttributes object.");
        in.defaultReadObject();

        propertyChangeListeners_ = new PropertyChangeSupport(this);
        vetoableChangeListeners_ = new VetoableChangeSupport(this);
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

    /**
     Returns the public authority of the data queue.
     @return  The public authority of the data queue.  Valid values are *ALL, *CHANGE, *EXCLUDE, *USE, *LIBCRTAUT.
     **/
    public String getAuthority()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting authority: " + authority_);
        return authority_;
    }

    /**
     Returns the text description of the data queue.
     @return  The text description of the data queue.
     **/
    public String getDescription()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting description: " + description_);
        return description_;
    }

    /**
     Returns the maximum entry length of the data queue.
     @return  The maximum entry length of the data queue.
     **/
    public int getEntryLength()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting max entry length:", entryLength_);
        return entryLength_;
    }

    /**
     Returns the byte length of the keys of the data queue.
     @return  The byte length of the keys of the data queue.
     **/
    public int getKeyLength()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting key length:", keyLength_);
        return keyLength_;
    }

    /**
     Returns a value that indicates if entries are read in FIFO order.  Returns true if entries are read off the data queue in FIFO order.  Returns false if entries are read off the data queue in LIFO order.
     @return  true if entries are read off the data queue in FIFO order; false otherwise.
     **/
    public boolean isFIFO()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Checking if is FIFO:", FIFO_);
        return FIFO_;
    }

    /**
     Returns a value that indicates if entries are forced to auxiliary storage.  If true, entries are immediately written to permanent storage.  If false, written entries may be kept in memory and could be lost in the case of a power outage.
     @return  true if entries are immediately written to permanent storage; false otherwise.
     **/
    public boolean isForceToAuxiliaryStorage()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Checking if force to auxiliary storage:", forceToAuxiliaryStorage_);
        return forceToAuxiliaryStorage_;
    }

    /**
     Returns whether sender information is stored with each data queue entry.
     @return  true if sender information is saved; false otherwise.
     **/
    public boolean isSaveSenderInfo()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Checking if save sender information:", saveSenderInfo_);
        return saveSenderInfo_;
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

    /**
     Sets the public authority of the data queue.
     @param  authority  The public authority of the data queue.  Valid values are *ALL, *CHANGE, *EXCLUDE, *USE, *LIBCRTAUT.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setAuthority(String authority) throws PropertyVetoException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Setting authority: " + authority);
        if (authority == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'authority' is null.");
            throw new NullPointerException("authority");
        }
        authority = authority.toUpperCase().trim();
        if (!(authority.equals("*ALL") || authority.equals("*CHANGE") || authority.equals("*EXCLUDE") || authority.equals("*USE") || authority.equals("*LIBCRTAUT")))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'authority' is not valid: " + authority);
            throw new ExtendedIllegalArgumentException("authority (" + authority + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        String oldValue = authority_;
        String newValue = authority;

        vetoableChangeListeners_.fireVetoableChange("authority", oldValue, newValue);
        authority_ = authority;
        propertyChangeListeners_.firePropertyChange("authority", oldValue, newValue);
    }

    /**
     Sets the text description of the data queue.
     @param  description  The text description.  This string must be 50 characters or less.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setDescription(String description) throws PropertyVetoException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Setting description: " + description);
        if (description == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'description' is null.");
            throw new NullPointerException("description");
        }
        if (description.length() > 50)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'description' is not valid: " + description);
            throw new ExtendedIllegalArgumentException("description (" + description + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        String oldValue = description_;
        String newValue = description;

        vetoableChangeListeners_.fireVetoableChange("description", oldValue, newValue);
        description_ = description;
        propertyChangeListeners_.firePropertyChange("description", oldValue, newValue);
    }

    /**
     Sets the maximum number of bytes per data queue entry.
     @param  entryLength  The maximum number of bytes per data queue entry.  Valid values are 1-64512.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setEntryLength(int entryLength) throws PropertyVetoException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Setting max entry length:", entryLength);
        if (entryLength < 1 || entryLength > 64512)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'entryLength' is not valid:", entryLength);
            throw new ExtendedIllegalArgumentException("entryLength (" + entryLength + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        Integer oldValue = new Integer(entryLength_);
        Integer newValue = new Integer(entryLength);

        vetoableChangeListeners_.fireVetoableChange("entryLength", oldValue, newValue);
        entryLength_ = entryLength;
        propertyChangeListeners_.firePropertyChange("entryLength", oldValue, newValue);
    }

    /**
     Sets a value that indicates if entries are read in FIFO order.  If true, entries are read off the data queue in FIFO order.  If false, entries are read off the data queue in LIFO order.
     @param  FIFO  true if queue entries are processed in FIFO order; false otherwise.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setFIFO(boolean FIFO) throws PropertyVetoException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Setting FIFO:", FIFO);

        Boolean oldValue = new Boolean(FIFO_);
        Boolean newValue = new Boolean(FIFO);

        vetoableChangeListeners_.fireVetoableChange("FIFO", oldValue, newValue);
        FIFO_ = FIFO;
        propertyChangeListeners_.firePropertyChange("FIFO", oldValue, newValue);
    }

    /**
     Sets a value that indicates if entries are forced to auxiliary storage.  If true, entries are immediately written to permanent storage.  If false, written entries may be kept in memory and could be lost in the case of a power outage.
     @param  forceToAuxiliaryStorage  true if writes are forced to storage before return; false otherwise.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setForceToAuxiliaryStorage(boolean forceToAuxiliaryStorage) throws PropertyVetoException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Setting force to auxiliary storage:", forceToAuxiliaryStorage);

        Boolean oldValue = new Boolean(forceToAuxiliaryStorage_);
        Boolean newValue = new Boolean(forceToAuxiliaryStorage);

        vetoableChangeListeners_.fireVetoableChange("forceToAuxiliaryStorage", oldValue, newValue);
        forceToAuxiliaryStorage_ = forceToAuxiliaryStorage;
        propertyChangeListeners_.firePropertyChange("forceToAuxiliaryStorage", oldValue, newValue);
    }

    /**
     Sets the number of bytes per data queue key.
     @param  keyLength  The number of bytes per data queue key. Valid values are 1-256.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setKeyLength(int keyLength) throws PropertyVetoException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Setting key length:", keyLength);

        if (keyLength < 1 || keyLength > 256)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'keyLength' is not valid:", keyLength);
            throw new ExtendedIllegalArgumentException("keyLength (" + keyLength + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        Integer oldValue = new Integer(keyLength_);
        Integer newValue = new Integer(keyLength);

        vetoableChangeListeners_.fireVetoableChange("keyLength", oldValue, newValue);
        keyLength_ = keyLength;
        propertyChangeListeners_.firePropertyChange("keyLength", oldValue, newValue);
    }

    /**
     Sets whether sender information is stored with each data queue entry.
     @param  saveSenderInformation  true if sender information is saved; false otherwise.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setSaveSenderInfo(boolean saveSenderInfo) throws PropertyVetoException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Setting save sender information:", saveSenderInfo);

        Boolean oldValue = new Boolean(saveSenderInfo_);
        Boolean newValue = new Boolean(saveSenderInfo);

        vetoableChangeListeners_.fireVetoableChange("saveSenderInfo", oldValue, newValue);
        saveSenderInfo_ = saveSenderInfo;
        propertyChangeListeners_.firePropertyChange("saveSenderInfo", oldValue, newValue);
    }
}
