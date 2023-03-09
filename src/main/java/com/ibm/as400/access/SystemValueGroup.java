///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SystemValueGroup.java
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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 The SystemValueGroup class represents a user-defined collection of system values and network attributes.  It is not as much a container for SystemValue objects as it is a factory for generating collections of SystemValues having certain attributes.
 @see  com.ibm.as400.access.SystemValue
 @see  com.ibm.as400.access.SystemValueList
 **/
public class SystemValueGroup implements Serializable
{
    static final long serialVersionUID = 4L;

    // The system where the group of system values is located.
    private AS400 system_ = null;
    // The user-defined group name.
    private String groupName_ = null;
    // The user-defined group description.
    private String groupDescription_ = null;

    // Contains a list of SystemValueInfo objects.
    private Vector infos_ = null;

    // List of property change event bean listeners.
    private transient PropertyChangeSupport propertyChangeListeners_ = null;  // Set on first add.
    // List of vetoable change event bean listeners.
    private transient VetoableChangeSupport vetoableChangeListeners_ = null;  // Set on first add.

    /**
     Constructs a SystemValueGroup object.  The <i>system</i> property must be set before attempting a connection.
     @see  #setSystem
     **/
    public SystemValueGroup()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing SystemValueGroup object.");
        infos_ = new Vector();
    }

    /**
     Constructs a SystemValueGroup object.  The group of system value names is initialized to be empty.
     @param  system  The system that this group of system value names references.
     @param  groupName  The user-defined group name to be used.
     @param  groupDescription  The user-defined group description to be used.
     **/
    public SystemValueGroup(AS400 system, String groupName, String groupDescription)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing SystemValueGroup object, system: " + system + ", group name: " + groupName);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (groupName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'groupName' is null.");
            throw new NullPointerException("groupName");
        }
        if (groupDescription == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'groupDescription' is null.");
            throw new NullPointerException("groupDescription");
        }

        system_ = system;
        groupName_ = groupName;
        groupDescription_ = groupDescription;
        infos_ = new Vector();
    }

    /**
     Constructs a SystemValueGroup object.  The group of system value names is initialized to contain the system value names in <i>names</i>.
     @param  system  The system that this group of system value names references.
     @param  groupName  The user-defined group name to be used.
     @param  groupDescription  The user-defined group description to be used.
     @param  names  The array of system value names to be initially added to this group.
     **/
    public SystemValueGroup(AS400 system, String groupName, String groupDescription, String[] names)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing SystemValueGroup object, system: " + system + ", group name: " + groupName);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (groupName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'groupName' is null.");
            throw new NullPointerException("groupName");
        }
        if (groupDescription == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'groupDescription' is null.");
            throw new NullPointerException("groupDescription");
        }
        if (names == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'names' is null.");
            throw new NullPointerException("names");
        }

        system_ = system;
        groupName_ = groupName;
        groupDescription_ = groupDescription;
        infos_ = new Vector(names.length);
        for (int i = 0; i < names.length; ++i) add(names[i]);
    }

    /**
     Constructs a SystemValueGroup object.  The group of system value names is initialized to contain all of the system value names in the system-defined group <i>groupIndicator</i>.  For example, specifying SystemValueList.GROUP_ALL for <i>groupIndicator</i> would result in this group of system value names being initialized to contain all system value and network attribute names.
     <p>Note:  This constructor now makes a connection to the <I>system</I> in order to retrieve the IBM i release level of the system.
     @param  system  The system that this group of system values references.
     @param  groupName  The user-defined group name to be used.
     @param  groupDescription  The user-defined group description to be used.
     @param  group  The system value group constant indicating the set of system value names to be initially added to this group.  Valid constants are defined in the SystemValueList class.
     @see  com.ibm.as400.access.SystemValueList
     @see  com.ibm.as400.access.SystemValueList#GROUP_ALL
     **/
    public SystemValueGroup(AS400 system, String groupName, String groupDescription, int group)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing SystemValueGroup object, system: " + system + ", group name: " + groupName + ", group:", group);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (groupName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'groupName' is null.");
            throw new NullPointerException("groupName");
        }
        if (groupDescription == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'groupDescription' is null.");
            throw new NullPointerException("groupDescription");
        }
        if (group < 0 || group > SystemValueList.GROUP_ALL)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'group' is not valid:", group);
            throw new ExtendedIllegalArgumentException("group", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        system_ = system;
        groupName_ = groupName;
        groupDescription_ = groupDescription;

        // If we can't get the vrm, add all the values.
        int vrm = 0x7FFFFFFF;
        try
        {
            vrm = system.getVRM();
        }
        catch (Exception e)
        {
            if (Trace.traceOn_) Trace.log(Trace.WARNING, "Couldn't retrieve VRM for system value group:", e);
        }

        // Copy all the SystemValueInfo objects for the release into this object's Vector.
        Vector groupInfos = SystemValueList.groups[group];
        int length = groupInfos.size();
        infos_ = new Vector(length);
        for (int i = 0; i < length; ++i)
        {
            SystemValueInfo info = (SystemValueInfo)groupInfos.elementAt(i);
            if (info.release_ <= vrm)
            {
                // Copy the SystemValueInfo objects into the new Vector.
                infos_.addElement(info);
            }
        }

        // QFRCCVNRST was in group SYSCTL in release V5R1M0 and below, and moved to group SEC in V5R2M0 and above.  By default we have it in group SEC, so if the system release is V5R1M0 or below, we fix up the group here.
        if (vrm <= 0x00050100)
        {
            switch (group)
            {
                case SystemValueList.GROUP_SEC:
                    // Remove QFRCCVNRST from group SEC.
                    infos_.removeElement(SystemValueList.lookup("QFRCCVNRST"));
                    break;
                case SystemValueList.GROUP_SYSCTL:
                    // Add QFRCCVNRST to group SYSCTL.
                    infos_.addElement(SystemValueList.lookup("QFRCCVNRST"));
                    break;
            }
        }
    }

    /**
     Adds a system value name to this group.  All names in this group must be unique, so if <i>name</i> already exists in this group, it is ignored.
     @param  name  The system value name to be added to this group.
     @see  #contains
     @see  #getNames
     @see  #remove
     **/
    public void add(String name)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding to system value group, name: " + name);
        if (name == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'name' is null.");
            throw new NullPointerException("name");
        }

        // Validates the String to make sure it's a valid system value.
        SystemValueInfo info = SystemValueList.lookup(name.toUpperCase());

        synchronized (infos_)
        {
            if (!infos_.contains(info)) infos_.addElement(info);
        }
    }

    /**
     Adds a PropertyChangeListener.  The specified PropertyChangeListener's <b>propertyChange</b> method will be called each time the value of any bound property is changed.  The PropertyChangeListener object is added to a list of PropertyChangeListeners managed by this SystemValueGroup.  It can be removed with removePropertyChangeListener.
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
     Determines if a system value name is part of this group.
     @param  name  The system value name in question.
     @return  Returns true if the system value is part of this group; false otherwise.
     @see  #add
     @see  #getNames
     @see  #remove
     **/
    public boolean contains(String name)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if system value group contains, name: " + name);
        if (name == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'name' is null.");
            throw new NullPointerException("name");
        }

        SystemValueInfo info = SystemValueList.lookup(name.toUpperCase());

        synchronized (infos_)
        {
            return infos_.contains(info);
        }
    }

    /**
     Returns the user-defined description for this group.
     @return  The group description.
     @see  #setGroupDescription
     **/
    public String getGroupDescription()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system value group description: " + groupDescription_);
        return groupDescription_;
    }

    /**
     Returns the user-defined name for this group.
     @return  The group name.
     @see  #setGroupName
     **/
    public String getGroupName()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system value group name: " + groupName_);
        return groupName_;
    }

    /**
     Returns the system value names that are currently part of this group.
     @return  An array of system value names.
     @see  #add
     @see  #contains
     @see  #remove
     **/
    public String[] getNames()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system value group names.");
        synchronized (infos_)
        {
            int length = infos_.size();
            String[] names = new String[length];
            for (int i = 0; i < length; ++i)
            {
                names[i] = ((SystemValueInfo)infos_.elementAt(i)).name_;
            }
            return names;
        }
    }

    /**
     Returns the system object representing the system on which the system value group exists.
     @return  The system object representing the system on which the system value group exists.  If the system has not been set, null is returned.
     **/
    public AS400 getSystem()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system: " + system_);
        return system_;
    }

    /**
     Returns newly-generated SystemValue objects representing the system values in this group.  The SystemValue objects in the Vector are sorted by system value name.
     <p>If any value in this group is not supported by this group's <i>system</i>, its corresponding SystemValue object will not be returned in the Vector.  Therefore, the number of SystemValue objects in the returned Vector may not be the same as the number of system value names represented by this group.
     <p>To refresh the values in a Vector of SystemValues, use the refresh() method.
     @return  A Vector of SystemValue objects.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #refresh
     **/
    public Vector getSystemValues() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system values, system: " + system_ + " group name: " + groupName_);
        // Make sure they've set the system, we don't care about the name or description.
        if (system_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot connect to system before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        Vector systemValues = null;
        synchronized (infos_)
        {
            systemValues = SystemValueUtility.retrieve(system_, infos_.elements(), groupName_, groupDescription_);
        }
        // Separate the sort from the retrieve so we are synchronized as short as possible.
        return SystemValueList.sort(systemValues);
    }

    /**
     Retrieves new values for the SystemValue objects in the Vector.  The SystemValue objects in <i>systemValues</i> have their values refreshed from their respective systems.  This method does not create new SystemValue objects; rather, it refreshes all SystemValue objects at once for a given system, which is more efficient than calling clear() on each SystemValue.  All objects in <i>systemValues</i> must be SystemValue objects.
     @param  systemValues  The group of SystemValue objects to be refreshed.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  com.ibm.as400.access.SystemValue#clear
     **/
    public static void refresh(Vector systemValues) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Refreshing system values.");
        if (systemValues == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'systemValues' is null.");
            throw new NullPointerException("systemValues");
        }

        // Since each SystemValue could have a different system object, we need to separate them by system.
        Hashtable systemHash = null;
        synchronized (systemValues)
        {
            int length = systemValues.size();
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Number of system values:", length);
            // Can't use Hashtable(0) in JDK 1.1.x.
            if (length == 0) return;

            // The hashtable uses system objects as the keys, and the elements are Vectors of SystemValue objects that have the key for their system.
            systemHash = new Hashtable(length);
            for (int i = 0; i < length; ++i)
            {
                try
                {
                    SystemValue systemValue = (SystemValue)systemValues.elementAt(i);
                    AS400 system = systemValue.getSystem();
                    Vector sameSystemVals = (Vector)systemHash.get(system);
                    if (sameSystemVals == null)
                    {
                        sameSystemVals = new Vector();
                        systemHash.put(system, sameSystemVals);
                    }
                    sameSystemVals.addElement(systemValue);
                }
                catch (ClassCastException e)
                {
                    Trace.log(Trace.ERROR, "Type of element 'systemValues[" + i + "]' is not valid:", e);
                    throw new ExtendedIllegalArgumentException("systemValues[" + i + "]", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
                }
            }
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Number of systems:", systemHash.size());

        // Loop through all of the different systems that we put in the hashtable.
        Enumeration list = systemHash.keys();
        while (list.hasMoreElements())
        {
            // Get the Vector of system values for the current system out of the hashtable.
            AS400 system = (AS400)list.nextElement();
            // The sysvals that have that system object for their system.
            Vector systemSystemValues = (Vector)systemHash.get(system);

            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Refreshing, system: " + system + ", number of values:", systemSystemValues.size());

            // Next, retrieve the values from the system. This actually results in 2 ProgramCalls, one for the system values, the other for the network attributes.  This code is similar to SystemValueUtility.retrieve().
            Vector svInfos = new Vector(); // System values These are the SystemValueInfo objects.
            Vector naInfos = new Vector(); // Network attributes.
            Vector svSystemValues = new Vector(); // These are the SystemValue objects.
            Vector naSystemValues = new Vector();
            for (int i = 0; i < systemSystemValues.size(); ++i)
            {
                SystemValue systemValue = (SystemValue)systemSystemValues.elementAt(i);
                SystemValueInfo info = systemValue.info_;
                if (systemValue.getGroup() == SystemValueList.GROUP_NET)
                {
                    naInfos.addElement(info);
                    naSystemValues.addElement(systemValue);
                }
                else
                {
                    svInfos.addElement(info);
                    svSystemValues.addElement(systemValue);
                }
            }

            Vector svValues = new Vector(); // Actual values for system values.
            Vector naValues = new Vector(); // Actual values for network attributes.
            if (svInfos.size() > 0)
            {
                // Get the system value values.
                svValues = SystemValueUtility.retrieveFromSystem(system, svInfos, false);
            }
            if (naInfos.size() > 0)
            {
                // Get the network attribute values.
                naValues = SystemValueUtility.retrieveFromSystem(system, naInfos, true);
            }

            // Set the new value inside each of the SystemValue objects.
            for (int c = 0; c < svInfos.size(); ++c)
            {
                SystemValue systemValue = (SystemValue)svSystemValues.elementAt(c);
                systemValue.value_ = svValues.elementAt(c);
            }
            for (int c = 0; c < naInfos.size(); ++c)
            {
                SystemValue systemValue = (SystemValue)naSystemValues.elementAt(c);
                systemValue.value_ = naValues.elementAt(c);
            }
        }
    }

    /**
     Removes a system value name from this group.
     @param  name  The system value name to be removed from this group.
     @return  Returns true if the system value was successfully removed; false otherwise.
     @see  #add
     @see  #contains
     @see  #getNames
     **/
    public boolean remove(String name)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing from system value group, name: " + name);
        if (name == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'name' is null.");
            throw new NullPointerException("name");
        }

        SystemValueInfo info = SystemValueList.lookup(name.toUpperCase());

        synchronized (infos_)
        {
            return infos_.removeElement(info);
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
     Sets the user-defined group description for any SystemValue objects generated by this group.
     @param  groupDescription  The group description.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     @see  #getGroupDescription
     **/
    public void setGroupDescription(String groupDescription) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting system value group description: " + groupDescription);
        if (groupDescription == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'groupDescription' is null.");
            throw new NullPointerException("groupDescription");
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            groupDescription_ = groupDescription;
        }
        else
        {
            String oldValue = groupDescription_;
            String newValue = groupDescription;
            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("groupDescription", oldValue, newValue);
            }
            groupDescription_ = groupDescription;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("groupDescription", oldValue, newValue);
            }
        }
    }

    /**
     Sets the user-defined group name for any SystemValue objects generated by this group.
     @param  groupName  The group name.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     @see  #getGroupName
     **/
    public void setGroupName(String groupName) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting system value group name: " + groupName);
        if (groupName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'groupName' is null.");
            throw new NullPointerException("groupName");
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            groupName_ = groupName;
        }
        else
        {
            String oldValue = groupName_;
            String newValue = groupName;
            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("groupName", oldValue, newValue);
            }
            groupName_ = groupName;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("groupName", oldValue, newValue);
            }
        }
    }

    /**
     Sets the system object representing the system on which the system value group exists.
     @param  system  The system object representing the system on which the system value group exists.
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

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
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
            system_ = newValue;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("system", oldValue, newValue);
            }
        }
    }

    /**
     Returns a String representation of this object.
     @return  A String representing this SystemValueGroup object.
     **/
    public String toString()
    {
        String ret = "";
        if (groupName_ != null)
        {
            ret += groupName_ + " ";
        }
        ret += "{" + infos_.size() + "} ";
        if (groupDescription_ != null)
        {
            ret += groupDescription_ + " ";
        }
        if (system_ != null)
        {
            ret += system_ + " ";
        }
        ret += "[" + super.toString() + "]";

        return ret;
    }
}
