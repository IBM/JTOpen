///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SystemValueGroup.java
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
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
The SystemValueGroup class represents a user-defined collection of system values
and network attributes. It is not as much a container for SystemValue objects as
it is a factory for generating collections of SystemValues having certain attributes.
@see com.ibm.as400.access.SystemValue
@see com.ibm.as400.access.SystemValueList
**/
public class SystemValueGroup implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;

  private AS400 system_;
  private String name_ = null;
  private String description_ = null;
  
  private Vector values_; // Contains a list of SystemValueInfo objects @A2C
  
  
  // Property event support
//@A2D  private transient Vector listeners_;
  private transient PropertyChangeSupport changes_;
  private transient VetoableChangeSupport vetos_;
  
  
  /**
   * Constructs a SystemValueGroup object. The <i>system</i>
   * property must be set before attempting a connection.
   * @see #setSystem
  **/
  public SystemValueGroup()
  {
    initializeTransient();
    values_ = new Vector(); //@A2A
  }
  
  
  /**
   * Constructs a SystemValueGroup object. The group of system value names is initialized to be empty.
   * @param system The server that this group of system value names references.
   * @param groupName The user-defined group name to be used.
   * @param groupDescription The user-defined group description to be used.
  **/
  public SystemValueGroup(AS400 system, String groupName, String groupDescription)
  {
    if (system == null) throw new NullPointerException("system");
    system_ = system;
    if (groupName == null) throw new NullPointerException("groupName");
    name_ = groupName;
    if (groupDescription == null) throw new NullPointerException("groupDescription");
    description_ = groupDescription;
    initializeTransient();
    values_ = new Vector(); //@A2A
  }
  
  
  /**
   * Constructs a SystemValueGroup object. The group of system value names is initialized to
   * contain the system value names in <i>names</i>.
   * @param system The server that this group of system value names references.
   * @param groupName The user-defined group name to be used.
   * @param groupDescription The user-defined group description to be used.
   * @param names The array of system value names to be initially added to this group.
  **/
  public SystemValueGroup(AS400 system, String groupName, String groupDescription, String[] names)
  {
    if (system == null) throw new NullPointerException("system");
    system_ = system;
    if (groupName == null) throw new NullPointerException("groupName");
    name_ = groupName;
    if (groupDescription == null) throw new NullPointerException("groupDescription");
    description_ = groupDescription;
    if (names == null) throw new NullPointerException("names");
    
    initializeTransient();
    
    values_ = new Vector(names.length);
    for (int i=0; i<names.length; ++i) add(names[i]);
  }
    
  
  /**
   * Constructs a SystemValueGroup object. The group of system value names is initialized to
   * contain all of the system value names in the system-defined group <i>groupIndicator</i>. For example, 
   * specifying SystemValueList.GROUP_ALL for <i>groupIndicator</i> would result in this group of
   * system value names being initialized to contain all system value and network attribute names.
   * <P>
   * Note: This constructor now makes a connection to the <I>system</I> in order to retrieve the
   * release level of the server.
   * @param system The server that this group of system values references.
   * @param groupName The user-defined group name to be used.
   * @param groupDescription The user-defined group description to be used.
   * @param group The system value group constant indicating the set of system value names to be
   *              initially added to this group. Valid constants are defined in the SystemValueList class.
   * @see com.ibm.as400.access.SystemValueList
   * @see com.ibm.as400.access.SystemValueList#GROUP_ALL
  **/
  public SystemValueGroup(AS400 system, String groupName, String groupDescription, int group) // It is "group" instead of "groupIndicator" so it matches what SystemValueList has as its parm.
  {
    if (system == null) throw new NullPointerException("system");
    system_ = system;
    if (groupName == null) throw new NullPointerException("groupName");
    name_ = groupName;
    if (groupDescription == null) throw new NullPointerException("groupDescription");
    description_ = groupDescription;
    
    initializeTransient();
    
//@B0D    values_ = (Vector)SystemValueList.groups_.get(SystemValueList.getGroupName(group)); // getGroupName validates the group parm

    //@B0A - "Clone" the Vector:
    Vector original = (Vector)SystemValueList.groups_.get(SystemValueList.getGroupName(group)); // getGroupName validates the group parm
    int len = original.size();
    values_ = new Vector();
//@C0D    values_.setSize(len);
    int vrm = 0x7FFFFFFF; //@C0A - If we can't get the vrm, add all the values.
    try //@C0A
    {
      vrm = system.getVRM(); //@C0A
    }
    catch(Exception e) //@C0A
    {
      if (Trace.traceOn_) //@C0A
      {
        Trace.log(Trace.WARNING, "Couldn't retrieve VRM for system value group:", e); //@C0A
      }
    }
    for (int i=0; i<len; ++i)
    {
      SystemValueInfo svi = (SystemValueInfo)original.elementAt(i); //@C0A
      if (svi.release_ <= vrm) //@C0A
      {
        values_.addElement(svi); // Copy the SystemValueInfo objects into the new Vector @C0C
      }
    }
  }
    
  
  /**
   * Adds a system value name to this group. All names in this group must be unique, so
   * if <i>name</i> already exists in this group, it is ignored.
   * @param name The system value name to be added to this group.
   * @see #contains
   * @see #getNames
   * @see #remove
  **/
  public void add(String name)
  {
    if (name == null) throw new NullPointerException("name");
    
    SystemValueInfo svi = SystemValueList.lookup(name.toUpperCase()); // validates the String to make sure it's a valid system value @A3C
    
    synchronized(values_)
    {
      if (!values_.contains(svi))
      {
        values_.addElement(svi);
      }
    }
  }
  
  
  /**
   * Adds a PropertyChangeListener. The specified PropertyChangeListener's
   * <b>propertyChange</b> method will be called each time the value of any
   * bound property is changed.  
   * @param listener The PropertyChangeListener.
   * @see #removePropertyChangeListener
  **/
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener");
    changes_.addPropertyChangeListener(listener);
  }


  /**
   * Adds a VetoableChangeListener. The specified VetoableChangeListener's
   * <b>vetoableChange</b> method will be called each time the value of
   * any constrained property is changed.   
   * @param listener The VetoableChangeListener.
   * @see #removeVetoableChangeListener
  **/
  public void addVetoableChangeListener(VetoableChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener");
    vetos_.addVetoableChangeListener(listener);
  }


  /**
   * Determines if a system value name is part of this group.
   * @param name The system value name in question.
   * @return Returns true if the system value is part of this group; false otherwise.
   * @see #add
   * @see #getNames
   * @see #remove
  **/
  public boolean contains(String name)
  {
    if (name == null) throw new NullPointerException("name");
    SystemValueInfo svi = SystemValueList.lookup(name.toUpperCase()); //@A3C
    
    synchronized(values_)
    {
      return values_.contains(svi);
    }
  }
  
     
  /**
   * Returns the user-defined description for this group.
   * @return The group description.
   * @see #setGroupDescription
  **/
  public String getGroupDescription()
  {
    return description_;
  }
                          
                          
  /**
   * Returns the user-defined name for this group.
   * @return The group name.
   * @see #setGroupName
  **/
  public String getGroupName()
  {
    return name_;
  }
  
  
  /**
   * Returns the system value names that are currently part of this group.
   * @return An array of system value names.
   * @see #add
   * @see #contains
   * @see #remove
  **/
  public String[] getNames()
  {
    String[] names = null;
    synchronized(values_)
    {
      names = new String[values_.size()];
      for (int i=0; i<values_.size(); ++i)
      {
        SystemValueInfo svi = (SystemValueInfo)values_.elementAt(i);
        names[i] = svi.name_;
      }
    }
    return names;
  }
  
  
  /**
   * Returns the system object for this group.
   * @return The AS400 system object.
  **/
  public AS400 getSystem()
  {
    return system_;
  }
  
  
  /**
   * Returns newly-generated SystemValue objects representing the system values
   * in this group. The SystemValue objects in the Vector are sorted by system value name.
   * <p>If any value in this group is not supported by this group's <i>system</i>,
   * its corresponding SystemValue object will not be returned in the Vector.
   * Therefore, the number of SystemValue objects in the returned Vector may not
   * be the same as the number of system value names represented by this group.
   * <p>To refresh the values in a Vector of SystemValues, use the refresh() method.
   * @return A Vector of SystemValue objects.
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with the server.
   * @exception ObjectDoesNotExistException If the server object does not exist.
   * @see #refresh
  **/
  public Vector getSystemValues()
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException
  {
    // Make sure they've set the system, we don't care about the name or description.
    if (system_ == null)
    {
      throw new ExtendedIllegalStateException("system",
          ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
    
    Vector ret = null;  
    synchronized(values_)
    {
      if (Trace.isTraceOn())
      {
        Trace.log(Trace.INFORMATION, "Retrieving SystemValueGroup '"+name_+"', '"+description_+"' from "+system_+".");
      }
      ret = SystemValueUtility.retrieve(system_, values_.elements(), name_, description_);
    }
    return SystemValueList.sort(ret); // Separate the sort from the retrieve so we are synchronized
                                      // as short as possible.
  }
  
  
  /**
   * Initializes transient data.
  **/
  private void initializeTransient()
  {
//@A2D    listeners_ = new Vector();
    changes_ = new PropertyChangeSupport(this);
    vetos_ = new VetoableChangeSupport(this);
  }
  
    
  /**
   * Provided to initialize transient data if this object is de-serialized.
  **/
  private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    initializeTransient();
  }
  
  
  /**
   * Retrieves new values for the SystemValue objects in the Vector.
   * The SystemValue objects in <i>systemValues</i> have their values
   * refreshed from their respective systems.
   * This method does not create new SystemValue objects; rather,
   * it refreshes all SystemValue objects at once for a given system,
   * which is more efficient than calling clear() on each SystemValue.
   * All objects in <i>systemValues</i> must be SystemValue objects.
   * @param systemValues The group of SystemValue objects to be refreshed.
   * @exception AS400SecurityException If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with the server.
   * @exception ObjectDoesNotExistException If the server object does not exist.
   * @exception UnknownHostException If the system cannot be located.
   * @see com.ibm.as400.access.SystemValue#clear
  **/
  public static void refresh(Vector systemValues)
        throws AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException,
               ObjectDoesNotExistException,
               UnknownHostException
  {
    // systemValues is a Vector of SystemValue objects
    if (systemValues == null) throw new NullPointerException("systemValues");
    
    // We don't want to generate a whole new set of SystemValue objects, just
    // refresh the ones that were passed in.
    // There's 3 ways to do this:
    // 1) We could call clear() on each of the SystemValues.
    // Result: The values would be refreshed when the user called getValue() on each SystemValue.
    //         There would be an API call at the time the user calls getValue().
    // 2) We could call clear() and getValue() on each of the SystemValues.
    // Result: A separate ProgramCall for each SystemValue, but when the user called getValue(),
    //         the value would be cached, so there would be no going to the system.
    // 3) We could do a ProgramCall and just plug the information into the system values in the Vector.
    // Result: Only 1 ProgramCall is necessary, as all the parameters are passed on it.
    //         More logic is required to call SystemValueUtility.retrieveFromSystem and
    //         align the results with the Vector of values that was passed in, and then
    //         finally fill in the values under-the-covers.
    
    // Option 3 is preferred since it has a minimal amount of comm traffic; it is implemented here.
        
    
    // Since each SystemValue could have a different AS400, we need to
    // separate them by system.
    
    // We'd better synchronize on the Vector, so no one else changes it while we are using it.
    Hashtable bySystem = null;
    synchronized(systemValues)
    {
      if (systemValues.size() == 0) return; //@A5A - can't use Hashtable(0) in JDK 1.1.x.

      // The hashtable uses AS400 objects as the keys, and the elements are
      // Vectors of SystemValue objects that have the key for their system.
      bySystem = new Hashtable(systemValues.size());
      for (int i=0; i<systemValues.size(); ++i)
      {
        SystemValue sv = null;
        try
        {
          sv = (SystemValue)systemValues.elementAt(i);
        }
        catch(ClassCastException cce)
        {
          Trace.log(Trace.ERROR, "Exception during refresh of system value element "+i, cce);
          throw new ExtendedIllegalArgumentException("systemValues ["+i+"]",
                                                     ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        
        AS400 sys = sv.getSystem();
        if (bySystem.containsKey(sys))
        {
          Vector sameSystemVals = (Vector)bySystem.get(sys);
          sameSystemVals.addElement(sv);
        }
        else
        {
          Vector sameSystemVals = new Vector();
          sameSystemVals.addElement(sv);
          bySystem.put(sys, sameSystemVals);
        }
      }
      
      if (Trace.isTraceOn())
      {
        Trace.log(Trace.INFORMATION, "Refreshing system values: "+systemValues.size()+" values, "+bySystem.size()+" systems.");
      }
    }
    
    // Loop through all of the different systems that we put in the hashtable.  
    Enumeration enum = bySystem.keys();
    while (enum.hasMoreElements())
    {
      // Get the Vector of system values for the current system out of the hashtable
      AS400 system = (AS400)enum.nextElement();  // This is the AS400 object
      Vector inTable = (Vector)bySystem.get(system); // The sysvals that have that AS400 object for their system
      
      if (Trace.isTraceOn())
      {
        Trace.log(Trace.INFORMATION, "Refreshing "+inTable.size()+" system values for "+system+".");
      }
      
      // First, sort the Vector.
      Vector sorted = SystemValueList.sort(inTable);
    
      // Next, retrieve the values from the 400. This actually results in 2 ProgramCalls,
      // one for the system values, the other for the network attributes.
      // This code is similar to SystemValueUtility.retrieve().
      Vector valVec = new Vector(); // System values These are the SystemValueInfo objects
      Vector attrVec = new Vector(); // Network attributes
      Vector valVecSV = new Vector(); // These are the SystemValue objects
      Vector attrVecSV = new Vector();
      for (int i=0; i<sorted.size(); ++i)
      {
        SystemValue sv = (SystemValue)sorted.elementAt(i);
        SystemValueInfo svi = sv.info_;
        if (sv.getGroup() == SystemValueList.GROUP_NET)
        {
          attrVec.addElement(svi); // attrVec contains the net attributes
          attrVecSV.addElement(sv);
        }
        else
        {
          valVec.addElement(svi); // valVec contains the system values
          valVecSV.addElement(sv);
        }
      }

      Vector valObj = new Vector(); // Actual values for system values
      Vector attrObj = new Vector(); // Actual values for network attributes
      if (valVec.size() > 0)
      {
        // Get the system value values
        valObj = SystemValueUtility.retrieveFromSystem(system, valVec, false);
      }
      if (attrVec.size() > 0)
      {
        // Get the network attribute values
        attrObj = SystemValueUtility.retrieveFromSystem(system, attrVec, true);
      }
  
      // Set the new value inside each of the SystemValue objects
      for (int c=0; c<valVec.size(); ++c)
      {
        SystemValue sv = (SystemValue)valVecSV.elementAt(c);
        sv.value_ = valObj.elementAt(c);
      }
      for (int c=0; c<attrVec.size(); ++c)
      {
        SystemValue sv = (SystemValue)attrVecSV.elementAt(c);
        sv.value_ = attrObj.elementAt(c);
      }
    }
  }

  
  /**
   * Removes a system value name from this group.
   * @param name The system value name to be removed from this group.
   * @return Returns true if the system value was successfully removed; false otherwise.
   * @see #add
   * @see #contains
   * @see #getNames
  **/
  public boolean remove(String name)
  {
    if (name == null) throw new NullPointerException("name");
    SystemValueInfo svi = SystemValueList.lookup(name.toUpperCase()); //@A3C
    
    synchronized(values_)
    {
      return values_.removeElement(svi);
    }
  }
  
    
  /**
   * Removes the listener from being notified when a bound property changes.
   * @param listener The PropertyChangeListener.
   * @see #addPropertyChangeListener
  **/
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener");
    changes_.removePropertyChangeListener(listener);
  }


  /**
   * Removes the listener from being notified when a constrained property changes.
   * @param listener The VetoableChangeListener.
   * @see #addVetoableChangeListener
  **/
  public void removeVetoableChangeListener(VetoableChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener");
    vetos_.removeVetoableChangeListener(listener);
  }


  /**
   * Sets the user-defined group description for any SystemValue objects generated by this group.
   * @param groupDescription The group description.
   * @exception PropertyVetoException If the change is vetoed.
   * @see #getGroupDescription
  **/
  public void setGroupDescription(String groupDescription) throws PropertyVetoException
  {
    if (groupDescription == null) throw new NullPointerException("groupDescription");
    String old = description_;
    vetos_.fireVetoableChange("groupDescription", old, groupDescription);    
    description_ = groupDescription;
    changes_.firePropertyChange("groupDescription", old, description_);
  }
  
  
  /**
   * Sets the user-defined group name for any SystemValue objects generated by this group.
   * @param groupName The group name.
   * @exception PropertyVetoException If the change is vetoed.
   * @see #getGroupName
  **/
  public void setGroupName(String groupName) throws PropertyVetoException
  {
    if (groupName == null) throw new NullPointerException("groupName");
    String old = name_;
    vetos_.fireVetoableChange("groupName", old, groupName);
    name_ = groupName;
    changes_.firePropertyChange("groupName", old, name_);
  }
  
  
  /**
   * Sets the system for any SystemValue objects generated by this group.
   * @param system The AS400 system object.
   * @exception PropertyVetoException If the change is vetoed.
   * @see #getSystem
  **/
  public void setSystem(AS400 system) throws PropertyVetoException
  {
    if (system == null) throw new NullPointerException("system");
    AS400 old = system_;
    vetos_.fireVetoableChange("system", old, system);
    system_ = system;
    changes_.firePropertyChange("system", old, system_);
  }
  
  
  /**
   * Returns a String representation of this object.
   * @return A String representing this SystemValueGroup object.
  **/
  public String toString()
  {
    String ret = "";
    if (name_ != null)
    {
      ret += name_+" ";
    }
    ret += "{"+values_.size()+"} ";    
    if (description_ != null)
    {
      ret += description_+" ";
    }
    if (system_ != null)
    {
      ret += system_+" ";
    }
    ret += "["+super.toString()+"]";
   
    return ret;
  }
}

