///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: UserList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;
import com.ibm.as400.resource.ResourceException;
import com.ibm.as400.resource.RUser;
import com.ibm.as400.resource.RUserList;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;



/**
The UserList class represents a list of OS/400 user profiles.
<p>
Implementation note:
This class internally uses the Open List APIs (e.g. QGYOLAUS).

@see com.ibm.as400.access.User
@see com.ibm.as400.access.UserGroup
@see com.ibm.as400.resource.RUser
@see com.ibm.as400.resource.RUserList
**/
public class UserList implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



  static final long serialVersionUID = 5L;

/**
Selection value indicating that the list contains all user profiles
and group profiles.
**/
  public static final String ALL = "*ALL";

/**
Selection value indicating that the list contains only user profiles
that are not group profiles.  These are user profiles that do not have
a group identifier specified.
**/
  public static final String USER = "*USER";

/**
Selection value indicating that the list contains only user profiles
that are group profiles.  These are user profiles that have a group
identifier specified.
**/
  public static final String GROUP = "*GROUP";

/**
Selection value indicating that the list contains only user profiles
that are members of a specified group.
**/
  public static final String MEMBER = "*MEMBER";


/**
Selection value indicating that no group profile is specified.
**/
  public static final String NONE = "*NONE";

/**
Selection value indicating that the list contains only user profiles
that are not group profiles.  These are user profiles that do not have
a group identifier specified.
**/
  public static final String NOGROUP = "*NOGROUP";




  private AS400 system_;
  private String userInfo_ = ALL;
  private String groupInfo_ = NONE;

  private int length_;
  private byte[] handle_; // handle that references the user space used by the open list APIs
  private byte[] handleToClose_; // used to close a previously opened list
  private boolean isConnected_;

    private transient   PropertyChangeSupport   propertyChangeSupport_;
    private transient   VetoableChangeSupport   vetoableChangeSupport_;

  private static final ProgramParameter errorCode_ = new ProgramParameter(new byte[4]);


/**
Constructs a UserList object.
The {@link #setSystem system} must be set before calling
any of the methods that connect to the server.
The <i>usersInfo</i> parameter defaults to {@link #ALL ALL}
and the <i>groupInfo</i> parameter defaults to {@link #NONE NONE}.
**/
    public UserList()
    {
  }



/**
Constructs a UserList object.
The <i>usersInfo</i> parameter defaults to {@link #ALL ALL}
and the <i>groupInfo</i> parameter defaults to {@link #NONE NONE}.
@param system   The system.
**/
    public UserList(AS400 system)
    {
    if (system == null) throw new NullPointerException("system");
    system_ = system;
  }



/**
Constructs a UserList object.

@param system       The system.
@param userInfo     The users to be returned.  Possible values are:
                    <ul>
                    <li>{@link #ALL ALL} - All user profiles and group profiles are
                        returned.
                    <li>{@link #USER USER} - Only user profiles that are not group
                        profiles are returned.  These are user profiles that do not have
                        a group identifier specified.
                    <li>{@link #GROUP GROUP} - Only user profiles that are group
                        profiles are returned.  These are user profiles that have
                        a group identifier specified.
                    <li>{@link #MEMBER MEMBER} - User profiles that are members
                        of the group specified for <em>groupInfo</em> are returned.
                    </ul>
@param groupInfo    The group profile whose members are to be returned.  Possible values are:
                    <ul>
                    <li>{@link #NONE NONE} - No group profile is specified.
                    <li>{@link #NOGROUP NOGROUP} - Users who are not a member of
                        any group are returned.
                    <li>The group profile name - Users who are a member of this group are
                        returned.
                    </ul>
**/
    public UserList(AS400 system, String userInfo, String groupInfo)
    {
    if (system == null) throw new NullPointerException("system");
    if (userInfo == null) throw new NullPointerException("userInfo");
    if (groupInfo == null) throw new NullPointerException("groupInfo");

    if (!userInfo.equals(ALL) && !userInfo.equals(USER) && !userInfo.equals(GROUP) && !userInfo.equals(MEMBER))
      throw new ExtendedIllegalArgumentException("userInfo", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

    if ((userInfo.equals(MEMBER)) && (groupInfo.equals(NONE))) throw new ExtendedIllegalArgumentException("groupInfo", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    if ((!(userInfo.equals(MEMBER))) && (!(groupInfo.equals(NONE)))) throw new ExtendedIllegalArgumentException("groupInfo", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    
    system_ = system;
    userInfo_ = userInfo;
    groupInfo_ = groupInfo;
  }



/**
Adds a PropertyChangeListener.  The specified PropertyChangeListener's
<b>propertyChange()</b> method will be called each time the value of
any bound property is changed.

@param listener The listener.
@see #removePropertyChangeListener
*/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
    if (propertyChangeSupport_ == null) propertyChangeSupport_ = new PropertyChangeSupport(this);
        propertyChangeSupport_.addPropertyChangeListener(listener);
  }



/**
Adds a VetoableChangeListener.  The specified VetoableChangeListener's
<b>vetoableChange()</b> method will be called each time the value of
any constrained property is changed.

@param listener The listener.
@see #removeVetoableChangeListener
*/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
    if (vetoableChangeSupport_ == null) vetoableChangeSupport_ = new VetoableChangeSupport(this);
        vetoableChangeSupport_.addVetoableChangeListener(listener);
    }



  /**
   * Closes the message list on the system.
   * This releases any system resources previously in use by this message list.
   * @exception AS400Exception                  If the system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the system.
   * @exception ObjectDoesNotExistException     If the object does not exist on the system.
   * @see #load
  **/
  public synchronized void close() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (!isConnected_)
    {
      return;
    }
    if (handleToClose_ != null && (handle_ == null || handle_ == handleToClose_))
    {
      handle_ = handleToClose_;
      handleToClose_ = null;
    }
    if (Trace.traceOn_)
    {
      Trace.log(Trace.DIAGNOSTIC, "Closing user list with handle: ", handle_);
    }
    ProgramParameter[] parms = new ProgramParameter[]
    {
      new ProgramParameter(handle_),
      errorCode_
    };
    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYCLST.PGM", parms);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }
    isConnected_ = false;
    handle_ = null;
    if (handleToClose_ != null) // Just in case.
    {
      handle_ = handleToClose_;
      handleToClose_ = null;
      close();
    }
  }


/**
Returns the group profile whose members are to be returned.

@return The group profile whose members are to be returned.
        Possible values are:
        <ul>
        <li>{@link #NONE NONE} - No group profile is specified.
        <li>{@link #NOGROUP NOGROUP} - Users who are not a member of
            any group are returned.
        <li>The group profile name - Users who are a member of this group are
            returned.
        </ul>
@see #setGroupInfo
**/
    public String getGroupInfo()
    {
    return groupInfo_;
  }



/**
   * Returns the number of users in the user list. This method implicitly calls {@link #load load()}.
   * @return The number of users, or 0 if no list was retrieved.
   * @see #load
**/
    public int getLength()
    {
    if (system_ == null) throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    try
    {
      if (handle_ == null)
      {
        load();
      }
    }
    catch(Exception e)
    {
      if (Trace.traceOn_)
      {
        Trace.log(Trace.ERROR, "Exception caught on UserList getLength():", e);
      }
    }

    return length_;
  }


/**
Returns the system.

@return The system.
@see #setSystem
**/
    public AS400 getSystem()
    {
    return system_;
  }


/**
Returns the description of which users are returned.

@return The description of which users are returned.  Possible values are:
        <ul>
        <li>{@link #ALL ALL} - All user profiles and group profiles are
            returned.
        <li>{@link #USER USER} - Only user profiles that are not group
            profiles are returned.  These are user profiles that do not have
            a group identifier specified.
        <li>{@link #GROUP GROUP} - Only user profiles that are group
            profiles are returned.  These are user profiles that have
            a group identifier specified.
        <li>{@link #MEMBER MEMBER} - User profiles that are members
            of the group specified for the group info are returned.
        </ul>
@see #setUserInfo
**/
    public String getUserInfo()
    {
    return userInfo_;
  }



/**
Returns the list of users in the user list.

@return An Enumeration of {@link com.ibm.as400.access.User User} objects.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception RequestNotSupportedException    If the requested function is not supported because the AS/400 system is not at the correct level.
@see #close
@see #load
**/
    public Enumeration getUsers ()
           throws AS400Exception,
                  AS400SecurityException,
                  ErrorCompletingRequestException,
                  InterruptedException,
                  IOException,
                  ObjectDoesNotExistException,
                  RequestNotSupportedException
    {
    if (system_ == null)
    {
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (handle_ == null)
    {
      load(); // Need to get the length_
    }

    User[] users = getUsers(-1, length_);

    return new UserEnumeration(users);
    }



/**
   * Helper class. Used to wrap the User[] with an Enumeration.
  **/
  private static class UserEnumeration implements Enumeration
  {
    private User[] users_;
    private int counter_;
    UserEnumeration(User[] users)
    {
      users_ = users;
    }

    public final boolean hasMoreElements()
    {
      return counter_ < users_.length;
    }

    public final Object nextElement()
    {
      if (counter_ >= users_.length)
      {
        throw new NoSuchElementException();
      }
      return users_[counter_++];
    }
  }


  /**
   * Returns a subset of the list of users.
   * This method allows the user to retrieve the user list from the server
   * in pieces. If a call to {@link #load load()} is made (either implicitly or explicitly),
   * then the users at a given offset will change, so a subsequent call to
   * getUsers() with the same <i>listOffset</i> and <i>number</i>
   * will most likely not return the same Users as the previous call.
   * @param listOffset The offset into the list of users. This value must be greater than 0 and
   * less than the list length, or specify -1 to retrieve all of the users.
   * @param number The number of users to retrieve out of the list, starting at the specified
   * <i>listOffset</i>. This value must be greater than or equal to 0 and less than or equal
   * to the list length.
   * @return The array of retrieved {@link com.ibm.as400.access.User User} objects.
   * @exception AS400Exception                  If the system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the system.
   * @exception ObjectDoesNotExistException     If the object does not exist on the system.
   * @see com.ibm.as400.access.Job
   * @see #close
   * @see #load
**/
  public User[] getUsers(int listOffset, int number) throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (listOffset < -1)
    {
      throw new ExtendedIllegalArgumentException("listOffset", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    if (number < 0)
    {
      throw new ExtendedIllegalArgumentException("number", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    
    if (system_ == null)
    {
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (number == 0)
    {
      return new User[0];
    }

    if (handle_ == null)
    {
      load();
    }

    int ccsid = system_.getCcsid();
    ConvTable conv = ConvTable.getTable(ccsid, null);
    
    ProgramParameter[] parms2 = new ProgramParameter[7];
    int len = length_*62; // 0150 format has 62 bytes per user
    parms2[0] = new ProgramParameter(len); // receiver variable
    parms2[1] = new ProgramParameter(BinaryConverter.intToByteArray(len)); // length of receiver variable
    parms2[2] = new ProgramParameter(handle_);
    parms2[3] = new ProgramParameter(80); // list information
    parms2[4] = new ProgramParameter(BinaryConverter.intToByteArray(number)); // number of records to return
    parms2[5] = new ProgramParameter(BinaryConverter.intToByteArray(listOffset == -1 ? -1 : listOffset+1)); // starting record
    parms2[6] = errorCode_;

    ProgramCall pc2 = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYGTLE.PGM", parms2);
    if (!pc2.run())
    {
      throw new AS400Exception(pc2.getMessageList());
    }
    
    byte[] listInfo = parms2[3].getOutputData();
    int totalRecords = BinaryConverter.byteArrayToInt(listInfo, 0);
    int recordsReturned = BinaryConverter.byteArrayToInt(listInfo, 4);
    while (totalRecords > recordsReturned)
    {
      len = len*(1+(totalRecords/(recordsReturned+1)));
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Calling UserList QGYGTLE again with an updated length of "+len+".");
      try
      {
        parms2[0].setOutputDataLength(len);
        parms2[1].setInputData(BinaryConverter.intToByteArray(len));
      }
      catch(PropertyVetoException pve) {}
      if (!pc2.run())
      {
        throw new AS400Exception(pc2.getMessageList());
      }
      listInfo = parms2[3].getOutputData();
      totalRecords = BinaryConverter.byteArrayToInt(listInfo, 0);
      recordsReturned = BinaryConverter.byteArrayToInt(listInfo, 4);
    }

    byte[] data = parms2[0].getOutputData();

    User[] users = new User[number];
    int offset = 0;
    for (int i=0; i<number; ++i) // each user
    {
      String profileName = conv.byteArrayToString(data, offset+0, 10).trim();
      boolean isGroupProfile = (data[offset+10] == (byte)0xF1); // 0xF1 is '1' for group profile, else '0' for user profile.
      boolean hasMembers = (data[offset+11] == (byte)0xF1); // 0xF1 is '1' if the user is a group that has members.
      String textDescription = conv.byteArrayToString(data, offset+12, 50).trim();
      if (isGroupProfile)
      {
        users[i] = new UserGroup(system_, profileName, hasMembers, textDescription);
      }
      else
      {
        users[i] = new User(system_, profileName, hasMembers, textDescription);
      }
      offset += 62;
    }

    return users;
  }


/**
   * Loads the list of users on the system. This method informs the
   * system to build a list of users. This method blocks until the system returns
   * the total number of users it has compiled. A subsequent call to
   * {@link #getUsers getUsers()} will retrieve the actual user information
   * and attributes for each user in the list from the system.
   * <p>This method updates the list length.
   *
   * @exception AS400Exception                  If the system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the system.
   * @exception ObjectDoesNotExistException     If the object does not exist on the system.
   * @exception ServerStartupException          If the server cannot be started.
   * @exception UnknownHostException            If the system cannot be located.
   * @see #getLength
   * @see #close
**/
  public synchronized void load() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (system_ == null) throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);

    // Close the previous list
    if (handle_ != null || handleToClose_ != null)
    {
      close();
    }
    
    // Generate text objects based on system CCSID
    final int ccsid = system_.getCcsid();
    ConvTable conv = ConvTable.getTable(ccsid, null);
    AS400Text text10 = new AS400Text(10, ccsid, system_);

    // Setup program parameters
    ProgramParameter[] parms = new ProgramParameter[8];
    parms[0] = new ProgramParameter(1); // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(1)); // length of receiver variable
    parms[2] = new ProgramParameter(80); // list information
    parms[3] = new ProgramParameter(BinaryConverter.intToByteArray(1)); // number of records to return (have to specify at least 1... for some reason 0 doesn't work)
    parms[4] = new ProgramParameter(conv.stringToByteArray("AUTU0150")); // format name
    parms[5] = new ProgramParameter(text10.toBytes(userInfo_.toUpperCase().trim())); // selection criteria
    parms[6] = new ProgramParameter(text10.toBytes(groupInfo_.toUpperCase().trim())); // group profile name
    parms[7] = errorCode_;

    // Call the program
    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYOLAUS.PGM", parms);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }

    isConnected_ = true;
    
    // List information returned
    byte[] listInformation = parms[2].getOutputData();
    handle_ = new byte[4];
    System.arraycopy(listInformation, 8, handle_, 0, 4);

    // This second program call is to retrieve the number of messages in the list.
    // It will wait until the server has fully populated the list before it
    // returns.
    ProgramParameter[] parms2 = new ProgramParameter[7];
    parms2[0] = new ProgramParameter(1); // receiver variable
    parms2[1] = new ProgramParameter(BinaryConverter.intToByteArray(1)); // length of receiver variable
    parms2[2] = new ProgramParameter(handle_); // request handle
    parms2[3] = new ProgramParameter(80); // list information
    parms2[4] = new ProgramParameter(BinaryConverter.intToByteArray(0)); // number of records to return
    parms2[5] = new ProgramParameter(BinaryConverter.intToByteArray(-1)); // starting record
    parms2[6] = errorCode_;

    ProgramCall pc2 = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYGTLE.PGM", parms2);
    if (!pc2.run())
    {
      throw new AS400Exception(pc2.getMessageList());
    }
    byte[] listInfo2 = parms2[3].getOutputData();
    length_ = BinaryConverter.byteArrayToInt(listInfo2, 0);
    
    if (Trace.traceOn_)
    {
      Trace.log(Trace.DIAGNOSTIC, "Loaded user list with length = "+length_+" and handle: ", handle_);
    }
  }


/**
Removes a PropertyChangeListener.

@param listener The listener.
@see #addPropertyChangeListener
**/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
    if (propertyChangeSupport_ != null) propertyChangeSupport_.removePropertyChangeListener(listener);
  }



/**
Removes a VetoableChangeListener.

@param listener The listener.
@see #addVetoableChangeListener
**/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
    if (vetoableChangeSupport_ != null) vetoableChangeSupport_.removeVetoableChangeListener(listener);
  }



  // Resets the handle to indicate we should close the list the next time
  // we do something, usually as a result of one of the selection criteria
  // being changed since that should build a new list on the server.
  private synchronized void resetHandle()
  {
    if (handleToClose_ == null) handleToClose_ = handle_; // Close the old list on the next load
    handle_ = null;
  }
  

/**
Sets the group profile whose members are to be returned.

<p>This must be set to a group profile name or {@link #NOGROUP NOGROUP}
if group info is set to {@link #MEMBER MEMBER}.  This must be set to
{@link #NONE NONE} if group info is not set to
{@link #MEMBER MEMBER}.

@param groupInfo    The group profile whose members are to be returned.
                    Possible values are:
                    <ul>
                    <li>{@link #NONE NONE} - No group profile is specified.
                    <li>{@link #NOGROUP NOGROUP} - Users who are not a member of
                        any group are returned.
                    <li>The group profile name - Users who are a member of this group are
                        returned.
                    </ul>

@exception PropertyVetoException If the change is vetoed.
@see #getGroupInfo
**/
    public void setGroupInfo(String groupInfo)
        throws PropertyVetoException
    {
        if (groupInfo == null)
            throw new NullPointerException("groupInfo");

    String oldValue = groupInfo_;
    if (vetoableChangeSupport_ != null) vetoableChangeSupport_.fireVetoableChange("groupInfo", oldValue, groupInfo);
    groupInfo_ = groupInfo;
    if (propertyChangeSupport_ != null) propertyChangeSupport_.firePropertyChange("groupInfo", oldValue, groupInfo);
    resetHandle();
  }



/**
Sets the system.  This cannot be changed if the object
has established a connection to the server.

@param system The system.

@exception PropertyVetoException    If the property change is vetoed.
@see #getSystem
**/
    public void setSystem(AS400 system)
    throws PropertyVetoException
    {
    if (system == null) throw new NullPointerException("system");
    if (isConnected_)
    {
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }
    AS400 old = system_;
    if (vetoableChangeSupport_ != null) vetoableChangeSupport_.fireVetoableChange("system", old, system);
    system_ = system;
    if (propertyChangeSupport_ != null) propertyChangeSupport_.firePropertyChange("system", old, system);
  }



/**
Sets which users are returned.  Possible values are:

@param userInfo A description of which users are returned.
                Possible values are:
                <ul>
                <li>{@link #ALL ALL} - All user profiles and group profiles are
                    returned.
                <li>{@link #USER USER} - Only user profiles that are not group
                    profiles are returned.  These are user profiles that do not have
                    a group identifier specified.
                <li>{@link #GROUP GROUP} - Only user profiles that are group
                    profiles are returned.  These are user profiles that have
                    a group identifier specified.
                <li>{@link #MEMBER MEMBER} - User profiles that are members
                    of the group specified for the group info are returned.
                </ul>

@exception PropertyVetoException If the change is vetoed.
@see #getUserInfo
**/
    public void setUserInfo(String userInfo)
        throws PropertyVetoException
    {
        if (userInfo == null)
            throw new NullPointerException("userInfo");

    if (!userInfo.equals(ALL) && !userInfo.equals(USER) && !userInfo.equals(GROUP) && !userInfo.equals(MEMBER))
      throw new ExtendedIllegalArgumentException("userInfo", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

    String oldValue = userInfo_;
    if (vetoableChangeSupport_ != null) vetoableChangeSupport_.fireVetoableChange("userInfo", oldValue, userInfo);
    userInfo_ = userInfo;
    if (propertyChangeSupport_ != null) propertyChangeSupport_.firePropertyChange("userInfo", oldValue, userInfo);
    resetHandle();
  }



}
