///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ClusteredHashTableEntry.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.net.*;
import java.io.*;
import java.util.*;
import java.beans.*;

/**
The ClusteredHashTableEntry class represents an entry in an OS/400 highly available
Clustered Hash Table. This class is only intended to be used with the
{@link com.ibm.as400.access.ClusteredHashTable  ClusteredHashTable} class.
<p>

Note: This class uses APIs that are available only when connecting to servers running OS/400 V5R2 or later.<p>
**/
public class ClusteredHashTableEntry implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

  /**
     Constants
  **/

  static final long serialVersionUID = 5L;

  /**
    Entry authority option that identifies any user can access the entry in the clustered hash table.
  **/
  public final static int ENTRY_AUTHORITY_ANY_USER = 1;

  /**
    Entry authority option that identifies a user with *ALLOBJ authority, the user that last stored the entry or both can access it.
  **/
  public final static int ENTRY_AUTHORITY_LAST_USER = 0;

  /**
    Update option to indicate if the specified key already exists allow the entry to be updated on the ClusteredHashTable.put() request.
  **/
  public final static int DUPLICATE_KEY_UPDATE = 1;

  /**
    Update option to indicate if the specified key already exists then do not allow the ClusteredHashTable.put() request to succeed.
  **/
  public final static int DUPLICATE_KEY_FAIL = 0;

  /**
    Data is consistent across the clustered hash table domain.
  **/
  public final static int ENTRY_STATUS_CONSISTENT = 0;

  /**
    Data is not consistent across the clustered hash table domain.
  **/
  public final static int ENTRY_STATUS_INCONSISTENT = 1;

  /**
    Maximum amount of user data that can be stored in a clustered hash table entry.
  **/
  public final static int MAX_USER_DATA_LENGTH = 61000; 


  /**
    Variables
  **/
  private byte[] userData_ = null;
  private int entryAuthority_ = ENTRY_AUTHORITY_LAST_USER;
  private int entryStatus_ = ENTRY_STATUS_CONSISTENT;
  private int entryUpdateOption_ = DUPLICATE_KEY_FAIL;
  private byte[] key_ = null;
  private int timeToLive_ = 60;
  private String ownerProfile_ = null;
  private String modifyProfile_ = null;

  private transient PropertyChangeSupport changes_ = new PropertyChangeSupport(this);

  /**
   Constructs a default ClusteredHashTableEntry object.
   The <i>key</i> and <i>data</i> must be set prior to invoking
   the {@link com.ibm.as400.access.ClusteredHashTable#put ClusteredHashTable.put()} method.
  **/
  public ClusteredHashTableEntry()
  {
  }


  /**
   Constructs a ClusteredHashTableEntry object that represents an entry in the clustered hash table.
   @param key The key that identifies the entry.
              The {@link com.ibm.as400.access.ClusteredHashTable#generateKey ClusteredHashTable.generateKey()} method can be used to provide a unique key.
   @param userData The user data to be stored in the clustered hash table.
                   The length of this data must be 1 through MAX_USER_DATA_LENGTH bytes.
   @param timeToLive The time (in seconds) that the entry will be allowed to remain in the clustered hash table.  If the value is -1, the entry will never expire.
                     The value must be greater than or equal to 60 seconds.
   @param entryAuthority This field identifies who is allowed to access, for example update
                         and retrieve, the entry associated with the key. This value must be
                         ENTRY_AUTHORITY_LAST_USER if the current cluster version is 2. 
                         For more information on the current cluster version see the Cluster
                         Version section of the Cluster Resource Services APIs.
                         Valid values are:
         <ul>
         <li>ENTRY_AUTHORITY_LAST_USER = The user who requests the ClusteredHashTable.put(), a user with *ALLOBJ authority or both is allowed to access the entry. 
         <li>ENTRY_AUTHORITY_ANY_USER = Any user can access the entry. 
         </ul>
   @param updateOption This is the action used on a ClusteredHashTable.put() request when the key
                       specified on the constructor or set using setKey() already exists in the
                       clustered hash table. This value must be DUPLICATE_KEY_FAIL if the current
                       cluster version is 2. For more information on the current cluster version
                       see the Cluster Version section of the Cluster Resource Services APIs.
                       It is only valid for the duration of the ClusteredHashTable.put() request. Valid values are:
         <ul>
         <li>DUPLICATE_KEY_FAIL = Do not allow the ClusteredHashTable.put() if the key already exists. 
         <li>DUPLICATE_KEY_UPDATE = Allow the entry associated with the key to be updated if it already exists in the clustered hash table. 
         </ul>
  **/
  public ClusteredHashTableEntry(byte[] key, byte[] userData, int timeToLive, int entryAuthority, int updateOption)
  {
    if (key == null)
    {
      throw new NullPointerException("key");
    }
    if (userData == null)
    {
      throw new NullPointerException("userData");
    }

    if (userData.length < 1 || userData.length > MAX_USER_DATA_LENGTH)
    {
      throw new ExtendedIllegalArgumentException("userData", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    // must be at least one minute, or up to one year, -1 means infinity
    if (((timeToLive != -1) && (timeToLive < 60)) || (timeToLive > 525600 * 60)) // @A1C
    {
      throw new ExtendedIllegalArgumentException("timeToLive", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    if (entryAuthority != ENTRY_AUTHORITY_LAST_USER && entryAuthority != ENTRY_AUTHORITY_ANY_USER)
    {
      throw new ExtendedIllegalArgumentException("entryAuthority", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    if (updateOption != DUPLICATE_KEY_FAIL && updateOption != DUPLICATE_KEY_UPDATE)
    {
      throw new ExtendedIllegalArgumentException("updateOption", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }


    // set instance vars
    key_ = new byte[key.length];
    System.arraycopy(key, 0, key_, 0, key.length);
    userData_ = new byte[userData.length];
    System.arraycopy(userData, 0, userData_, 0, userData.length);
    timeToLive_ = timeToLive;
    entryAuthority_ = entryAuthority;
    entryUpdateOption_ = updateOption;
  }


  /**
    Adds a listener to be notified when the value of any bound property is changed.
    @see #removePropertyChangeListener
    @param listener The PropertyChangeListener.
  **/
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener");
    changes_.addPropertyChangeListener(listener);
  }


  /**
    Returns the entry status. Valid values are:
    <ul>
    <li>ENTRY_STATUS_CONSISTENT = The entry is consistent between the clustered hash table domain. 
    <li>ENTRY_STATUS_INCONSISTENT = The entry is not consistent between the clustered hash table domain.
    For more details on why an entry is inconsistent see the description of this field in the
    QcstRetrieveCHTEntry or the QcstListCHTKeys API.
    </ul> 
    @return The entry status.
  **/
  public int getEntryStatus()
  {
    return entryStatus_;
  }


  /**
   Returns the entry authority. This field identifies who is allowed to access, for example
   update and retrieve, the entry associated with the key.  Valid values are:
   <ul>
   <li>ENTRY_AUTHORITY_LAST_USER = The user who requests the ClusteredHashTable.put(), a user
   with *ALLOBJ authority or both is allowed to access the entry. 
   <li>ENTRY_AUTHORITY_ANY_USER = Any user can access the entry. 
   </ul>
   @return The entry authority. The default value is ENTRY_AUTHORITY_LAST_USER.
 **/
  public int getEntryAuthority()
  {
    return entryAuthority_;
  }


  /**
    Returns the key.
    @return A byte array copy of the key, or null if the key is not set.
  **/
  public byte[] getKey()
  {
    if (key_ == null) return null;
    byte[] b = new byte[key_.length];
    System.arraycopy(key_, 0, b, 0, key_.length);
    return b;
  }


  /**
    Returns the time to live (in seconds) that was passed to the constructor.  This value cannot be retrieved from the hash table.  The only purpose of this method is to see what the user passed into the ClusteredHashTableEntry.  If no value was specified in the constructor, this will return the defaulted value.
    @return The time to live. The default is 60 seconds.
  **/
  public int getTimeToLive()
  {
    return timeToLive_;
  }


  /**
    Returns the update option that was passed to the constructor.  This value cannot be retrieved from the hash table so will have default value.
    @return The update option. The default is DUPLICATE_KEY_FAIL.
  **/
  public int getUpdateOption()
  {
    return entryUpdateOption_;
  }


  /**
    Returns the user data.
    @return A byte array copy of the user data, or null if the user data is not set.
  **/
  public byte[] getUserData()
  {
    if (userData_ == null) return null;
    byte[] b = new byte[userData_.length];
    System.arraycopy(userData_, 0, b, 0, userData_.length);
    return b;
  }

  
  /**
     Returns the user profile that created the entry.
     @return The user profile that created the entry.
   **/
  public String getOwnerProfile()  // @A1C
  {
    if (ownerProfile_ == null)
	return null;
    return ownerProfile_;
  }


  /**
     Sets the user profile that created the entry.
   **/
  protected void setOwnerProfile(String usr)  // @A1C
  {
      if(usr == null)
      {
	  throw new NullPointerException("usr");
      }
      if ((usr.length() == 0) || (usr.length() > 10))
      {
	  throw new ExtendedIllegalArgumentException("usr", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
      }
      ownerProfile_ = usr;
  }

  /**
     Returns the user profile that modified the entry.
     @return The user profile that modified the entry.
   **/
  public String getModifiedProfile()  // @A1A
  {
    if (modifyProfile_ == null)
      return null;
    return modifyProfile_;
  }


  /**
     Sets the user profile that modified the entry.
   **/
  protected void setModifiedProfile(String usr)  // @A1A
  {
      if(usr == null)
      {
	  throw new NullPointerException("usr");
      }
      if ((usr.length() == 0) || (usr.length() > 10))
      {
	  throw new ExtendedIllegalArgumentException("usr", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
      }
      modifyProfile_ = usr;
  }


  /**
  Provided to initialize transient data if this object is de-serialized.
  **/
  private void initializeTransient()
  {
    changes_ = new PropertyChangeSupport(this);
  }


  /**
   Restores the state of this object from an object input stream.
   @param ois The stream of state information.
   @exception IOException
   @exception ClassNotFoundException
   **/
  private void readObject(java.io.ObjectInputStream ois) throws IOException, ClassNotFoundException
  {
    // Restore the non-static and non-transient fields.
    ois.defaultReadObject();

    // Initialize the transient fields.
    initializeTransient();
  }


  /**
    Removes the listener from being notified when a bound property changes.
    @see #addPropertyChangeListener
    @param listener The PropertyChangeListener.
  **/
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener");
    changes_.removePropertyChangeListener(listener);
  }


  /**
    Sets the entry authority. This identifies who is allowed to access, for example
    update and retrieve, the entry associated with the key. This value must be 0 if
    the current cluster version is 2.  For more information on the current cluster
    version see the Cluster Version section of the Cluster Resource Services APIs.
    Valid values are:
    <ul>
    <li>ENTRY_AUTHORITY_LAST_USER = The user who requests the ClusteredHashTable.put(), a user with *ALLOBJ authority or both is allowed to access the entry. 
    <li>ENTRY_AUTHORITY_ANY_USER = Any user can access the entry. 
    </ul>
    @param entryAuthority The value of the entry authority. The default for this parameter is ENTRY_AUTHORITY_LAST_USER.
  **/
  public void setEntryAuthority(int entryAuthority)
  {
    if (entryAuthority != ENTRY_AUTHORITY_LAST_USER && entryAuthority != ENTRY_AUTHORITY_ANY_USER)
    {
      throw new ExtendedIllegalArgumentException("entryAuthority", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    int old = entryAuthority_;
    entryAuthority_ = entryAuthority;
    changes_.firePropertyChange("entryAuthority", new Integer(old), new Integer(entryAuthority_));
  }

  
  /**
    Sets the key. The ClusteredHashTable.generateKey() method can be used to generate the key.  ClusterHashTable keys must be 16 bytes.
    The <i>key</i> must be set before invoking the ClusteredHashTable.put() method.
    @param key The key.
  **/
  public void setKey(byte[] key)
  {
    if (key == null)
    {
      throw new NullPointerException("key");
    }
    if (key.length != 16)  // a cht key must be 16 bytes

    {
	throw new ExtendedIllegalArgumentException("key", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }

    byte[] old = key_;
    key_ = new byte[key.length];
    System.arraycopy(key, 0, key_, 0, key.length);
    changes_.firePropertyChange("key", old, key_);
  }

  /**
    Sets the entry status for the entry.  Can only be 0 or 1.
    0 means consistant in the Cluster Hash Table.
    1 means it is inconstistant.  The entry is not the same on all nodes in the Clustered Hash Table domain.
    @param entryStatus, the status of the entry.
  **/
  protected void setEntryStatus(int entryStatus)
  {
    // status can only be 1 or 0
    if((entryStatus != 0) && (entryStatus != 1))
    {
      throw new ExtendedIllegalArgumentException("entryStatus", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    entryStatus_ = entryStatus;
  }

  
  /**
    Sets the time to live (in seconds) an entry remains in the clustered hash table.
    This value must be greater than or equal to 60 seconds. If the value is -1, the entry will never expire.
    @param key The value of the time to live. The default for the timeToLive is 60 seconds.
  **/
  public void setTimeToLive(int timeToLive)
  {
    // must be at least one minute, or up to one year, -1 means infinity
    if (((timeToLive != -1) && (timeToLive < 60)) || (timeToLive > 525600 * 60)) // @A1C
    {
      throw new ExtendedIllegalArgumentException("timeToLive", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    int old = timeToLive_;
    timeToLive_ = timeToLive;
    changes_.firePropertyChange("timeToLive", new Integer(old), new Integer(timeToLive_));
  }


  /**
    Sets the update option. This is the action used by ClusteredHashTable.put() when the
    specified key already exists in the clustered hash table. This value must be 0 if the
    current cluster version is 2. For more information on the current cluster version see
    the Cluster Version section of the Cluster Resource Services APIs. It is only valid
    for the duration of the ClusteredHashTable.put() method. Valid values are:
    <ul>
    <li>DUPLICATE_KEY_FAIL = Do not allow the ClusteredHashTable.put() to succeed if the key already exists.
    <li>DUPLICATE_KEY_UPDATE = Allow the entry associated with the key to be updated if it already exists in the clustered hash table. 
    </ul>
    @param updateOption The value of the update option.
  **/
  public void setUpdateOption(int updateOption)
  {
    if (updateOption != DUPLICATE_KEY_FAIL && updateOption != DUPLICATE_KEY_UPDATE)
    {
      throw new ExtendedIllegalArgumentException("updateOption", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    int old = entryUpdateOption_;
    entryUpdateOption_ = updateOption;
    changes_.firePropertyChange("updateOption", new Integer(old), new Integer(entryUpdateOption_));
  }


  /**
    Sets the user data to be stored in the clustered hash table. The length of the data
    must be 1 through MAX_USER_DATA_LENGTH. The <i>user data</i> must be set before
    invoking the ClusteredHashTable.put() method.
    @param userData The user data.
  **/
  public void setUserData(byte[] userData)
  {
    if (userData == null)
    {
      throw new NullPointerException("userData");
    }
    if (userData.length < 1 || userData.length > MAX_USER_DATA_LENGTH)
    {
      throw new ExtendedIllegalArgumentException("userData", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }

    byte[] old = userData_;
    userData_ = new byte[userData.length];
    System.arraycopy(userData, 0, userData_, 0, userData.length);
    changes_.firePropertyChange("userData", old, userData_);
  }   
}
