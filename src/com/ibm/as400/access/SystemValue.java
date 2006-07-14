///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SystemValue.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2006 International Business Machines Corporation and
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
import java.util.Vector;

/**
 The SystemValue class represents a system value or network attribute on the system.
 **/
public class SystemValue implements Serializable
{
    static final long serialVersionUID = 4L;

    // The system where the system value is located.
    private AS400 system_ = null;
    // The properties of this system value.
    SystemValueInfo info_ = null;  // Also accessed by SystemValueGroup.
    // The actual data to which this system value is set.
    Object value_ = null;  // Also accessed by SystemValueGroup.

    // The user-defined group name.
    private String groupName_ = null;
    // The user-defined group description.
    private String groupDescription_ = null;

    // Locale specific MRI description.
    private String localeDescription_ = null;

    // Flag indicating this system value has been read from the system.
    transient private boolean cached_ = false;
    // Flag indicating if a connection been made.
    transient private boolean connected_ = false;

    // List of system value event bean listeners.
    transient private Vector systemValueListeners_ = null;  // Set on first add.
    // List of property change event bean listeners.
    transient private PropertyChangeSupport propertyChangeListeners_ = null;  // Set on first add.
    // List of vetoable change event bean listeners.
    transient private VetoableChangeSupport vetoableChangeListeners_ = null;  // Set on first add.

    /**
     Constructs a SystemValue object.  It creates a default SystemValue.  The <i>system</i> and <i>name</i> properties must be set before attempting a connection.
     **/
    public SystemValue()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing SystemValue object.");
    }

    /**
     Constructs a SystemValue object.  It creates a SystemValue instance that represents the system value <i>name</i> on <i>system</i>.
     @param  system  The system object representing the system on which the system value exists.
     @param  name  The name of the system value.
     **/
    public SystemValue(AS400 system, String name)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing SystemValue object, system: " + system + ", name: " + name);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (name == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'name' is null.");
            throw new NullPointerException("name");
        }
        if (name.length() == 0)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'name' is not valid: '" + name + "'");
            throw new ExtendedIllegalArgumentException("name (" + name + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        system_ = system;
        info_ = SystemValueList.lookup(name.toUpperCase());
        localeDescription_ = SystemValueList.lookupDescription(info_, system.getLocale());
    }

    // This constructor is used by SystemValueUtility to fill in all of the internal data for a SystemValue object when it comes from the API call.
    // Note that the SystemValue's cached_ flag is set to true.
    SystemValue(AS400 system, SystemValueInfo info, Object value, String groupName, String groupDescription)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing SystemValue object, system: " + system + ", group name: " + groupName);
        system_ = system;
        info_ = info;
        value_ = value;

        groupName_ = groupName;
        groupDescription_ = groupDescription;

        localeDescription_ = SystemValueList.lookupDescription(info_, system.getLocale());

        cached_ = true;
        connected_ = true;
    }

    /**
     Adds an SystemValueListener.  The specified SystemValueListener's <b>systemValueChanged</b> method will be called each time a system value has been changed.  The SystemValueListener object is added to a list of SystemValueListeners managed by this SystemValue.  It can be removed with removeSystemValueListener.
     @param  listener  The listener object.
     **/
    public void addSystemValueListener(SystemValueListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding system value listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        synchronized (this)
        {
            // If first add.
            if (systemValueListeners_ == null)
            {
                systemValueListeners_ = new Vector();
            }
            systemValueListeners_.addElement(listener);
        }
    }

    /**
     Adds a PropertyChangeListener.  The specified PropertyChangeListener's <b>propertyChange</b> method will be called each time the value of any bound property is changed.  The PropertyChangeListener object is added to a list of PropertyChangeListeners managed by this SystemValue.  It can be removed with removePropertyChangeListener.
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
     Adds a VetoableChangeListener.  The specified VetoableChangeListener's <b>vetoableChange</b> method will be called each time the value of any constrained property is changed.
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

    /**
     Clears this system value from the cache.  The next time a getValue() is performed on this system value, the value will be retrieved from the system instead of from the cache.
     **/
    public void clear()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Clearing system value from cache.");
        cached_ = false;
    }

    // Makes a "connection" to the system.  The system and name properties must be set before a connection can be made.
    private void connect() throws AS400SecurityException, IOException, RequestNotSupportedException
    {
        // Verify required attributes have been set.
        if (system_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot connect before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        if (info_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot connect before setting name.");
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        // Check if the system value is supported on this system.
        // Also don't want to prohibit the user from running to a pre-V4R2 system, even though we do not support it.
        if (info_.release_ > system_.getVRM() && info_.release_ != 0x00040200)
        {
            byte[] vrmBytes = BinaryConverter.intToByteArray(info_.release_);
            Trace.log(Trace.ERROR, "System not at correct release for system value, name: " + info_.name_ + ", release:", vrmBytes);
            throw new RequestNotSupportedException(info_.name_, RequestNotSupportedException.SYSTEM_LEVEL_NOT_CORRECT);
        }
        connected_ = true;
    }

    // Fires the event.
    // @param  oldValue  The old value.
    // @param  newValue  The new value.
    private void fireChangedEvent(Object oldValue, Object newValue)
    {
        // If we have made it this far, we know we have listeners.
        Vector targets = (Vector)systemValueListeners_.clone();
        SystemValueEvent event = new SystemValueEvent(this, oldValue, newValue);
        for (int i = 0; i < targets.size(); ++i)
        {
            SystemValueListener target = (SystemValueListener)targets.elementAt(i);
            target.systemValueChanged(event);
        }
    }

    /**
     Returns the description for this system value.
     @return  The description for the system value.
     **/
    public String getDescription()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system value description.");
        if (info_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot get description before system value name is set.");
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        String description = (localeDescription_ == null) ? info_.description_ : localeDescription_;
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Description: " + description);
        return description;
    }

    /**
     Returns the system value group to which this system value belongs.  Possible values are:
     <ul>
     <li>GROUP_ALC
     <li>GROUP_ALL
     <li>GROUP_DATTIM
     <li>GROUP_EDT
     <li>GROUP_LIBL
     <li>GROUP_MSG
     <li>GROUP_NET
     <li>GROUP_SEC
     <li>GROUP_STG
     <li>GROUP_SYSCTL
     </ul>
     @see  SystemValueList
     @return  The system value group.
     **/
    public int getGroup()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system value group.");
        if (info_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot get group before system value name is set.");
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        int group = info_.group_;

        // QFRCCVNRST was in group SYSCTL in release V5R1M0 and below, and moved to group SEC in V5R2M0 and above.  By default we have it in group SEC, so if the system release is V5R1M0 or below, we fix up the group here.
        try
        {
            if (system_ != null && info_.name_.equals("QFRCCVNRST")  && system_.getVRM() <= 0x00050100)
            {
                group = SystemValueList.GROUP_SYSCTL;
            }
        }
        catch (Exception e)
        {
            // If there is an exception getting the system VRM, do nothing.
            if (Trace.traceOn_) Trace.log(Trace.WARNING, "Couldn't retrieve VRM for system value:", e);
        }

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Group:", group);
        return group;
    }

    /**
     Returns the user-defined group description.  If this system value was not generated by the SystemValueGroup class, then null is returned.
     @return  The group description.
     **/
    public String getGroupDescription()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system value group description: " + groupDescription_);
        return groupDescription_;
    }

    /**
     Returns the user-defined group name.  If this system value was not generated by the SystemValueGroup class, then null is returned.
     @return  The group name.
     **/
    public String getGroupName()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system value group name: " + groupName_);
        return groupName_;
    }

    /**
     Returns the name of this system value.  If the name has not been set null is returned.
     @return  The name of the system value.
     **/
    public String getName()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system value name.");
        String name = (info_ == null) ? null : info_.name_;
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Name: " + name);
        return name;
    }

    /**
     Returns the supported release for this system value.  The returned value is the earliest version of i5/OS under which the system value is supported.  If the system value is supported in a release prior to V4R2M0, then V4R2M0 is returned.
     @see  AS400#generateVRM
     @return  The release.
     **/
    public int getRelease()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system value release.");
        if (info_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot get release before system value name is set.");
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        if (Trace.traceOn_)
        {
            byte[] vrmBytes = BinaryConverter.intToByteArray(info_.release_);
            Trace.log(Trace.DIAGNOSTIC, "Release:", vrmBytes);
        }
        return info_.release_;
    }

    /**
     Returns the length (in bytes) of this system value's data value component.  For system values that are of type {@link SystemValueList#TYPE_ARRAY TYPE_ARRAY}, this method returns the total size of the data value.
     @return  The size.
     **/
    public int getSize()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system value size.");
        if (info_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot get size before system value name is set.");
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        int size = (info_.type_ == SystemValueList.TYPE_ARRAY) ? info_.size_ * info_.arraySize_ : info_.size_;
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Size:", size);
        return size;
    }

    /**
     Returns the system object representing the system on which the system value exists.
     @return  The system object representing the system on which the system value exists.  If the system has not been set, null is returned.
     **/
    public AS400 getSystem()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system: " + system_);
        return system_;
    }

    /**
     Returns the type for this system value.  Possible values are:
     <ul>
     <li>{@link SystemValueList#TYPE_ARRAY TYPE_ARRAY} - The data contained by this system value is a String[] object.
     <li>{@link SystemValueList#TYPE_DATE TYPE_DATE} - The data contained by this system value is a Date object.
     <li>{@link SystemValueList#TYPE_DECIMAL TYPE_DECIMAL} - The data contained by this system value is a BigDecimal object.
     <li>{@link SystemValueList#TYPE_INTEGER TYPE_INTEGER} - The data contained by this system value is an Integer object.
     <li>{@link SystemValueList#TYPE_STRING TYPE_STRING} - The data contained by this system value is a String object.
     </ul>
     @see  SystemValueList
     @return  The return type.
     **/
    public int getType()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system value type.");
        if (info_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot get type before system value name is set.");
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Type:", info_.type_);
        return info_.type_;
    }

    /**
     Returns the current value of this system value.  Use {@link #getType getType()} to determine the type of the returned object.  For example, some system values are represented as arrays of String.
     @see  #getType
     @return  The data.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @exception  RequestNotSupportedException  If the i5/OS release level of the system does not support the system value.
     **/
    public Object getValue() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, RequestNotSupportedException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system value value.");
        if (!connected_) connect();

        if (!cached_)
        {
            // Retrieve the value here.
            value_ = SystemValueUtility.retrieve(system_, info_);
            cached_ = true;
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Value: " + value_);
        return value_;
    }

    /**
     Indicates if this system value is read only or if it can be set by the user.
     @return  true if the system value is read only; false otherwise.
     **/
    public boolean isReadOnly()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if system value is read only.");
        if (info_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot determine read only before system value name is set.");
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Read only:", info_.readOnly_);
        return info_.readOnly_;
    }

    /**
     Removes the SystemValueListener.  If the SystemValueListener is not on the list, nothing is done.
     @param  listener  The listener object.
     **/
    public void removeSystemValueListener(SystemValueListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing system value listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        // If we have listeners.
        if (systemValueListeners_ != null)
        {
            systemValueListeners_.removeElement(listener);
        }
    }

    /**
     Removes the PropertyChangeListener.  If the PropertyChangeListener is not on the list, nothing is done.
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
     Removes the VetoableChangeListener.  If the VetoableChangeListener is not on the list, nothing is done.
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

    /**
     Sets the system value name.
     @param  name  The system value.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setName(String name) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting system value name: " + name);
        if (name == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'name' is null.");
            throw new NullPointerException("name");
        }
        if (connected_)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'name' after connect.");
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            // Set instance variables.
            info_ = SystemValueList.lookup(name.toUpperCase());
            localeDescription_ = (system_ != null) ? SystemValueList.lookupDescription(info_, system_.getLocale()) : null;
        }
        else
        {
            String oldValue = (info_ != null) ? info_.name_ : null;
            SystemValueInfo info = SystemValueList.lookup(name.toUpperCase());
            String newValue = info.name_;
            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("name", oldValue, newValue);
            }
            info_ = info;
            localeDescription_ = (system_ != null) ? SystemValueList.lookupDescription(info_, system_.getLocale()) : null;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("name", oldValue, newValue);
            }
        }
    }

    /**
     Sets the system object representing the system on which the system value exists.
     @param  system  The system object representing the system on which the system value exists.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setSystem(AS400 system) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting system: " + system);

        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (connected_)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'system' after connect.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            system_ = system;
            localeDescription_ = (info_ != null) ? SystemValueList.lookupDescription(info_, system_.getLocale()) : null;
        }
        else
        {
            AS400 oldValue = system_;
            AS400 newValue = system;

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("system", oldValue, newValue);
            }
            system_ = system;
            localeDescription_ = (info_ != null) ? SystemValueList.lookupDescription(info_, system_.getLocale()) : null;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("system", oldValue, newValue);
            }
        }
    }

    /**
     Sets the value for this system value.  Use {@link #getType getType()} to determine the type of object to set.  For example, some system values are represented as arrays of String.
     @see  #getType
     @param  value  The data.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  RequestNotSupportedException  If the i5/OS release level of the system does not support the system value.
     **/
    public void setValue(Object value) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, RequestNotSupportedException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting system value value: " + value);
        if (value == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'value' is null.");
            throw new NullPointerException("value");
        }

        if (!connected_) connect();

        SystemValueUtility.set(system_, info_, value);
        value_ = value;
        cached_ = true;
        if (systemValueListeners_ != null) fireChangedEvent(value_, value);
    }
}
