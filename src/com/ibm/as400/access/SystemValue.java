///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SystemValue.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.util.Vector;
import java.util.Locale; //@F0A
import java.io.IOException;
import java.net.UnknownHostException;

/**
The SystemValue class represents a system value or network attribute
on the server.
**/
public class SystemValue implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;



  // The info_ and value_ are package scope because they need
  // to be accessed directly by the SystemValueGroup class.
  
  /*@B0D private */ SystemValueInfo info_; // The properties of this system value

  /*@B0D private */ Object value_; // The actual data this system value is set to
  
  private AS400 system_; // The server this system value belongs to.

  transient boolean cached_ = false; // Has this system value been read from the 400 yet?

  // connected_ is transient because even though the AS400 system_ object
  // gets deserialized, its password does not. So, the user would have to
  // re-specify the password when making a connection using a
  // deserialized SystemValue. We want them to be able to do that by
  // doing a SystemValue.setSystem() which means we cannot be connected upon
  // deserialization. See readObject().
  transient boolean connected_ = false; // Has a connection been made yet?

  transient Vector listeners_ = null; //@F0C
  transient PropertyChangeSupport changes_ = null; //@F0C
  transient VetoableChangeSupport vetos_ = null; //@F0C


  private String name_; //@B0A The user-defined group name
  private String description_; //@B0A The user-defined group description
  
  private String localeDescription_; //@F0A - locale-specific MRI description

  /**
  Constructs a SystemValue object.
  It creates a default SystemValue. The <i>system</i> and <i>name</i>
  properties must be set before attempting a connection.
  **/
  public SystemValue()
  {
  }


  /**
  Constructs a SystemValue object.
  It creates a SystemValue instance that represents the system value
  <i>name</i> on <i>system</i>.
    @param system The server that this system value references.
    @param name The name of the system value.
  **/
  public SystemValue(AS400 system, String name)
  {
    if (system == null)
    {
      throw new NullPointerException("system");
    }
    if (name == null)
    {
      throw new NullPointerException("name");
    }
    if (name.length() == 0)
    {
      throw new ExtendedIllegalArgumentException("name",
          ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    info_ = SystemValueList.lookup(name.toUpperCase());
    localeDescription_ = SystemValueList.lookupDescription(info_, system.getLocale()); //@F0A
    system_ = system;
  }


  /**
  Package scope constructor.
  This is the "back door" way of constructing a SystemValue object.
  This constructor is used by SystemValueUtility to fill in all of the internal data
  for a SystemValue object when it comes off of the API call.
  Note that the SystemValue's cached_ flag is set to true.
    @param system The server.
    @param info The SystemValueInfo for this system value.
    @param value The data contained by this system value.
  **/
  SystemValue(AS400 system, SystemValueInfo info, Object value, String name, String description) //@B0C
  {
    system_ = system;
    info_ = info;
    localeDescription_ = SystemValueList.lookupDescription(info_, system.getLocale()); //@F0A
    value_ = value;
    cached_ = true;
    
    name_ = name; //@B0A
    description_ = description; //@B0A
    connected_ = true; //@B2A
  }

  /**
  Adds a system value listener to receive system value events from this system value.
    @see #removeSystemValueListener
    @param listener The system value listener.
  **/
  public void addSystemValueListener(SystemValueListener listener)
  {
    if (listener == null) throw new NullPointerException("listener"); //@B1A
    if (listeners_ == null) listeners_ = new Vector(); //@F0A
    listeners_.addElement(listener);
  }


  /**
  Adds a listener to be notified when the value of any bound property is changed.
    @see #removePropertyChangeListener
    @param listener The PropertyChangeListener.
  **/
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener"); //@B1A
    if (changes_ == null) changes_ = new PropertyChangeSupport(this); //@F0A
    changes_.addPropertyChangeListener(listener);
  }


  /**
  Adds a listener to be notified when the value of any constrained property is changed.
    @see #removeVetoableChangeListener
    @param listener The VetoableChangeListener.
  **/
  public void addVetoableChangeListener(VetoableChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener"); //@B1A
    if (vetos_ == null) vetos_ = new VetoableChangeSupport(this); //@F0A
    vetos_.addVetoableChangeListener(listener);
  }


  /**
  Clears this system value from the cache.
  The next time a getValue() is performed on this system value, the value
  will be retrieved from the server instead of from the cache.
  **/
  public void clear()
  {
    cached_ = false;
  }


  /**
  Makes a "connection" to the server.
  The <i>system</i> and <i>name</i> properties must be set before a
  connection can be made.
  **/
  private void connect()
      throws AS400SecurityException,
             IOException,
             RequestNotSupportedException,
             UnknownHostException
  {
    if (system_ == null)
    {
      throw new ExtendedIllegalStateException("system",
          ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
    if (info_ == null)
    {
      throw new ExtendedIllegalStateException("name",
          ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    // Check if the system value is supported
    // on this server.
    // Also don't want to prohibit the user from running to a
    // pre-V4R2 system, even though we do not support it.
    if (info_.release_ > system_.getVRM() &&
        info_.release_ != AS400.generateVRM(4,2,0))
    {
      throw new RequestNotSupportedException(info_.name_,
          RequestNotSupportedException.SYSTEM_LEVEL_NOT_CORRECT);
    }
    connected_ = true;
  }


  /**
  Fires the event.
    @param oldValue The old value.
    @param newValue The new value.
  **/
  private void fireChangedEvent(Object oldValue, Object newValue)
  {
    if (listeners_ != null) //@F0A
    {
    Vector targets = (Vector)listeners_.clone();
    SystemValueEvent event = new SystemValueEvent(this, oldValue, newValue);
    for (int i=0; i<targets.size(); ++i)
    {
      SystemValueListener target = (SystemValueListener)targets.elementAt(i);
      target.systemValueChanged(event);
    }
  }
  }


  /**
  Returns the description for this system value.
    @return The description for the system value.
  **/
  public String getDescription()
  {
    if (info_ == null)
    {
      throw new ExtendedIllegalStateException("name",
          ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
    if (localeDescription_ == null) //@F0A
    {
    return info_.description_;
  }
    else //@F0A
    {
      return localeDescription_; //@F0A
    }
  }


  /**
  Returns the system value group to which this system value belongs.
  Possible values are:
  <UL>
  <LI>GROUP_ALC
  <LI>GROUP_ALL
  <LI>GROUP_DATTIM
  <LI>GROUP_EDT
  <LI>GROUP_LIBL
  <LI>GROUP_MSG
  <LI>GROUP_NET
  <LI>GROUP_SEC
  <LI>GROUP_STG
  <LI>GROUP_SYSCTL
  </UL>
    @see SystemValueList
    @return The system value group.
  **/
  public int getGroup()
  {
    if (info_ == null)
    {
      throw new ExtendedIllegalStateException("name",
          ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
    return info_.group_;
  }


  //@B0A
  /**
   * Returns the user-defined group description. If this system value was
   * not generated by the SystemValueGroup class, then null is returned.
   * @return The group description.
  **/
  public String getGroupDescription() //@B0A
  {
    return description_;
  }
  
        
  //@B0A
  /**
   * Returns the user-defined group name. If this system value was
   * not generated by the SystemValueGroup class, then null is returned.
   * @return The group name.
  **/
  public String getGroupName() //@B0A
  {
    return name_;
  }
  
        
  /**
  Returns the name of this system value.
    @return The name of the system value.
  **/
  public String getName()
  {
    if (info_ == null)
    {
      return null;
    }
    return info_.name_;
  }


  /**
  Returns the supported release version for this system value.
  The returned value is the earliest version of server under which the
  system value is supported. If the system value is supported in a release
  prior to V4R2, then V4R2 is returned.
    @see AS400#generateVRM
    @return The release.
  **/
  public int getRelease()
  {
    if (info_ == null)
    {
      throw new ExtendedIllegalStateException("name",
          ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
    return info_.release_;
  }


  /**
  Returns the length (in bytes) of this system value's data value component.
  For system values that are of type TYPE_ARRAY, this method returns the
  total size of the data value.
    @return The size.
  **/
  public int getSize()
  {
    if (info_ == null)
    {
      throw new ExtendedIllegalStateException("name",
          ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
    if (info_.returnType_ == SystemValueList.TYPE_ARRAY)
      return (info_.size_ * info_.arraySize_);
    return info_.size_;
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
  Returns the type for this system value.
  Possible values are:
  <ul>
  <li>TYPE_ARRAY - The data contained by this system value is a String[] object. 
  <li>TYPE_DATE - The data contained by this system value is a Date object.
  <li>TYPE_DECIMAL - The data contained by this system value is a BigDecimal object.
  <li>TYPE_INTEGER - The data contained by this system value is an Integer object.
  <li>TYPE_STRING - The data contained by this system value is a String object.
  </ul>
    @see SystemValueList
    @return The return type.
  **/
  public int getType()
  {
    if (info_ == null)
    {
      throw new ExtendedIllegalStateException("name",
          ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
    return info_.returnType_;
  }


  /**
  Returns the current value of this system value.
    @return The data.
    @exception AS400SecurityException If a security or authority error occurs.
    @exception ErrorCompletingRequestException If an error occurs before the request is completed.
    @exception InterruptedException If this thread is interrupted.
    @exception IOException If an error occurs while communicating with the server.
    @exception ObjectDoesNotExistException If the server object does not exist.
    @exception RequestNotSupportedException If the release level of the server does not support the system value.
  **/
  public Object getValue()
      throws AS400SecurityException,
             ErrorCompletingRequestException,
             InterruptedException,
             IOException,
             ObjectDoesNotExistException,
//@B0D             PropertyVetoException,
             RequestNotSupportedException
  {
    if (!connected_)
      connect();

    if (!cached_)
    {
      // Retrieve the value here
      value_ = SystemValueUtility.retrieve(system_, info_);
      cached_ = true;
    }
    return value_;
  }
    

  /**
  Indicates if this system value is read only or if it can be
  set by the user.
    @return true if the system value is read only; false otherwise.
  **/
  public boolean isReadOnly()
  {
    if (info_ == null)
    {
      throw new ExtendedIllegalStateException("name",
          ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
    return info_.readOnly_;
  }


  /**
  Provided to initialize transient data if this object is de-serialized.
  **/
  private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    //@F0D listeners_ = new Vector();
    //@F0D changes_ = new PropertyChangeSupport(this);
    //@F0D vetos_ = new VetoableChangeSupport(this);
    cached_ = false;
    connected_ = false;
  }


  /**
  Removes a listener from the SystemValue listeners list.
    @see #addSystemValueListener
    @param listener The system value listener.
  **/
  public void removeSystemValueListener(SystemValueListener listener)
  {
    if (listener == null) throw new NullPointerException("listener"); //@B1A
    if (listeners_ != null) listeners_.removeElement(listener); //@F0C
  }


  /**
  Removes this listener from being notified when a bound property changes.
    @see #addPropertyChangeListener
    @param listener The PropertyChangeListener.
  **/
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener"); //@B1A
    if (changes_ != null) changes_.removePropertyChangeListener(listener); //@F0C
  }


  /**
  Removes this listener from being notified when a constrained property changes.
    @see #addVetoableChangeListener
    @param listener The VetoableChangeListener.
  **/
  public void removeVetoableChangeListener(VetoableChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener"); //@B1A
    if (vetos_ != null) vetos_.removeVetoableChangeListener(listener); //@F0C
  }


  /**
  Sets the system value name.
    @param name The system value.
    @exception PropertyVetoException If the change is vetoed.
  **/
  public void setName(String name) throws PropertyVetoException
  {
    if (name == null)
    {
      throw new NullPointerException("name");
    }
    if (connected_)
    {
      throw new ExtendedIllegalStateException("name",
          ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }
    String old = (info_ != null ? info_.name_ : null);
    SystemValueInfo temp = SystemValueList.lookup(name.toUpperCase());
    if (vetos_ != null) vetos_.fireVetoableChange("name", old, name); //@F0C
    info_ = temp;
    if (system_ != null) //@F0A
    {
      localeDescription_ = SystemValueList.lookupDescription(info_, system_.getLocale()); //@F0A
    }
    else //@F0A
    {
      localeDescription_ = null; //@F0A
    }
    if (changes_ != null) changes_.firePropertyChange("name", old, info_.name_); //@F0C
  }


  /**
  Sets the system.
    @param system The system.
    @exception PropertyVetoException If the change is vetoed.
  **/
  public void setSystem(AS400 system) throws PropertyVetoException
  {
    if (system == null)
    {
      throw new NullPointerException("system");
    }
    if (connected_)
    {
      throw new ExtendedIllegalStateException("system",
          ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }
    AS400 old = system_;
    if (vetos_ != null) vetos_.fireVetoableChange("system", old, system); //@F0C
    system_ = system;
    if (info_ != null) //@F0A
    {
      localeDescription_ = SystemValueList.lookupDescription(info_, system_.getLocale()); //@F0A
    }
    else //@F0A
    {
      localeDescription_ = null; //@F0A
    }
    if (changes_ != null) changes_.firePropertyChange("system", old, system_); //@F0C
  }


  /**
  Sets the value for this system value.
    @param value The data.
    @exception AS400SecurityException If a security or authority error occurs.
    @exception ErrorCompletingRequestException If an error occurs before the request is completed.
    @exception InterruptedException If this thread is interrupted.
    @exception IOException If an error occurs while communicating with the server.
    @exception RequestNotSupportedException If the release level of the server does not support the system value.
    @exception UnknownHostException If the system cannot be located.
  **/
  public void setValue(Object value)
      throws AS400SecurityException,
             ErrorCompletingRequestException,
             InterruptedException,
             IOException,
//@B0D             PropertyVetoException,  // The value isn't a bean property, so it shouldn't do this.
             RequestNotSupportedException,
             UnknownHostException
  {
    if (!connected_)
      connect();

    if (value == null)
    {
      throw new NullPointerException("value");
    }
    SystemValueUtility.set(system_, info_, value);
    fireChangedEvent(value_, value);
    value_ = value;
    cached_ = true;
  }
}

