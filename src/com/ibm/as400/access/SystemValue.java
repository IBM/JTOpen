///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: SystemValue.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.Beans;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.util.Hashtable;
import java.util.Vector;
import java.io.IOException;
import java.net.UnknownHostException;

/**
The SystemValue class represents a system value or network attribute
on the AS/400.
**/
public class SystemValue implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  private SystemValueInfo info_ = null; // The properties of this system value

  private Object value_ = null; // The actual data this system value is set to
  private AS400 system_ = null; // The AS/400 this system value belongs to.

  transient boolean cached_ = false; // Has this system value been read from the 400 yet?

  // connected_ is transient because even though the AS400 system_ object
  // gets deserialized, its password does not. So, the user would have to
  // re-specify the password when making a connection using a
  // deserialized SystemValue. We want them to be able to do that by
  // doing a SystemValue.setSystem() which means we cannot be connected upon
  // deserialization. See readObject().
  transient boolean connected_ = false; // Has a connection been made yet?

  transient Vector listeners_ = new Vector();
  transient PropertyChangeSupport changes_ = new PropertyChangeSupport(this);
  transient VetoableChangeSupport vetos_ = new VetoableChangeSupport(this);


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
    @param system The AS/400 that this system value references.
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
    system_ = system;
  }


  // package scope constructor
  // This is the backdoor way of constructing a SystemValue
  /**
  Package scope constructor.
  This is the "back door" way of constructing a SystemValue object.
    @param system The AS/400.
    @param info The SystemValueInfo for this system value.
    @param value The data contained by this system value.
  **/
  SystemValue(AS400 system, SystemValueInfo info, Object value)
  {
    system_ = system;
    info_ = info;
    value_ = value;
    cached_ = true;
  }

  /**
  Adds a system value listener to receive system value events from this system value.
    @see #removeSystemValueListener
    @param listener The system value listener.
  **/
  public void addSystemValueListener(SystemValueListener listener)
  {
    this.listeners_.addElement(listener);
  }


  /**
  Adds a PropertyChangeListener.  
    @see #removePropertyChangeListener
    @param listener The PropertyChangeListener.
  **/
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    this.changes_.addPropertyChangeListener(listener);
  }


  /**
  Adds the VetoableChangeListener.  
    @see #removeVetoableChangeListener
    @param listener The VetoableChangeListener.
  **/
  public void addVetoableChangeListener(VetoableChangeListener listener)
  {
    this.vetos_.addVetoableChangeListener(listener);
  }


  /**
  Clears this system value from the cache.
  The next time a getValue() is performed on this system value, the value
  will be retrieved from the AS/400 instead of from the cache.
  **/
  public void clear()
  {
    cached_ = false;
  }


  /**
  Makes a "connection" to the AS/400.
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
    // on this AS/400.
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
    Vector targets = (Vector)listeners_.clone();
    SystemValueEvent event = new SystemValueEvent(this, oldValue, newValue);
    for (int i=0; i<targets.size(); ++i)
    {
      SystemValueListener target = (SystemValueListener)targets.elementAt(i);
      target.systemValueChanged(event);
    }
  }


   /**
    Returns the copyright.
   **/
   private static String getCopyright()
   {
     return Copyright.copyright;
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
    return info_.description_;
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
  The returned value is the earliest version of OS/400 under which the
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
    @return The AS/400.
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
    @exception IOException If an error occurs while communicating with the AS/400.
    @exception ObjectDoesNotExistException If the AS/400 object does not exist.
    @exception PropertyVetoException If the change is vetoed.
    @exception RequestNotSupportedException If the release level of the AS/400 does not support the system value.
    @exception UnknownHostException If the AS/400 system cannot be located.
  **/
  public Object getValue()
      throws AS400SecurityException,
             ErrorCompletingRequestException,
             InterruptedException,
             IOException,
             ObjectDoesNotExistException,
             PropertyVetoException,
             RequestNotSupportedException,
             UnknownHostException
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
    listeners_ = new Vector();
    changes_ = new PropertyChangeSupport(this);
    vetos_ = new VetoableChangeSupport(this);
    cached_ = false;
    connected_ = false;
  }


  /**
  Removes the SystemValueListener from the internal list.
  If the SystemValueListener is not on the list, nothing is done.
    @see #addSystemValueListener
    @param listener The system value listener.
  **/
  public void removeSystemValueListener(SystemValueListener listener)
  {
    listeners_.removeElement(listener);
  }


  /**
  Removes the PropertyChangeListener from the internal list.
  If the PropertyChangeListener is not on the list, nothing is done.
    @see #addPropertyChangeListener
    @param listener The PropertyChangeListener.
  **/
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    changes_.removePropertyChangeListener(listener);
  }


  /**
  Removes the VetoableChangeListener from the internal list.
  If the VetoableChangeListener is not on the list, nothing is done.
    @see #addVetoableChangeListener
    @param listener The VetoableChangeListener.
  **/
  public void removeVetoableChangeListener(VetoableChangeListener listener)
  {
    vetos_.removeVetoableChangeListener(listener);
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
    vetos_.fireVetoableChange("name", old, name);
    info_ = temp;
    changes_.firePropertyChange("name", old, info_.name_);
  }


  /**
  Sets the system.
    @param system The AS/400.
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
    vetos_.fireVetoableChange("system", old, system);
    system_ = system;
    changes_.firePropertyChange("system", old, system_);
  }


  /**
  Sets the value for this system value.
    @param value The data.
    @exception AS400SecurityException If a security or authority error occurs.
    @exception ErrorCompletingRequestException If an error occurs before the request is completed.
    @exception InterruptedException If this thread is interrupted.
    @exception IOException If an error occurs while communicating with the AS/400.
    @exception PropertyVetoException If the change is vetoed.
    @exception RequestNotSupportedException If the release level of the AS/400 does not support the system value.
    @exception UnknownHostException If the AS/400 system cannot be located.
  **/
  public void setValue(Object value)
      throws AS400SecurityException,
             ErrorCompletingRequestException,
             InterruptedException,
             IOException,
             PropertyVetoException,
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

