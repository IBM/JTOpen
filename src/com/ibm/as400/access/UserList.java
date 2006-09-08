///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  UserList.java
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
import java.io.Serializable;
import java.util.Enumeration;

/**
 The UserList class represents a list of user profiles on the system.
 <p>Implementation note:  This class internally uses the Open List APIs (e.g. QGYOLAUS).
 @see  com.ibm.as400.access.User
 @see  com.ibm.as400.access.UserGroup
 **/
public class UserList implements Serializable
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    static final long serialVersionUID = 5L;

    /**
     Selection value indicating that the list contains all user profiles and group profiles.
     **/
    public static final String ALL = "*ALL";

    /**
     Selection value indicating that the list contains only user profiles that are not group profiles.  These are user profiles that do not have a group identifier specified.
     **/
    public static final String USER = "*USER";

    /**
     Selection value indicating that the list contains only user profiles that are group profiles.  These are user profiles that have a group identifier specified.
     **/
    public static final String GROUP = "*GROUP";

    /**
     Selection value indicating that the list contains only user profiles that are members of a specified group.
     **/
    public static final String MEMBER = "*MEMBER";

    /**
     Selection value indicating that no group profile is specified.
     **/
    public static final String NONE = "*NONE";

    /**
     Selection value indicating that the list contains only user profiles that are not group profiles.  These are user profiles that do not have a group identifier specified.
     **/
    public static final String NOGROUP = "*NOGROUP";

    // Shared error code parameter.
    private static final ProgramParameter ERROR_CODE = new ProgramParameter(new byte[8]);

    // The system where the users are located.
    private AS400 system_ = null;
    // The selection criteria for which users are returned.
    private String userInfo_ = ALL;
    // The secection criteria for the group profile whose members are returned.
    private String groupInfo_ = NONE;
    // The profile names to include in the list.
    private String userProfile_ = ALL;

    // Length of the user list.
    private int length_ = 0;
    // Handle that references the user space used by the open list APIs.
    private byte[] handle_ = null;
    // If the user or group info has changed, close the old handle before loading the new one.
    private boolean closeHandle_ = false;

    // List of property change event bean listeners.
    private transient PropertyChangeSupport propertyChangeListeners_ = null;  // Set on first add.
    // List of vetoable change event bean listeners.
    private transient VetoableChangeSupport vetoableChangeListeners_ = null;  // Set on first add.

    /**
     Constructs a UserList object.  The {@link #setSystem system} must be set before calling any of the methods that connect to the system.  The <i>userInfo</i> parameter defaults to {@link #ALL ALL}, the <i>groupInfo</i> parameter defaults to {@link #NONE NONE}, and the <i>userProfile</i> parameter defaults to {@link #ALL ALL}.
     **/
    public UserList()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing UserList object.");
    }

    /**
     Constructs a UserList object.  The <i>userInfo</i> parameter defaults to {@link #ALL ALL}, the <i>groupInfo</i> parameter defaults to {@link #NONE NONE}, and the <i>userProfile</i> parameter defaults to {@link #ALL ALL}.
     @param  system  The system object representing the system on which the users exists.
     **/
    public UserList(AS400 system)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing UserList object, system: " + system);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        system_ = system;
    }

    /**
     Constructs a UserList object.  The <i>userProfile</i> parameter defaults to {@link #ALL ALL}.
     @param  system  The system object representing the system on which the users exists.
     @param  userInfo  The users to be returned.  Possible values are:
     <ul>
     <li>{@link #ALL ALL} - All user profiles and group profiles are returned.
     <li>{@link #USER USER} - Only user profiles that are not group profiles are returned.  These are user profiles that do not have a group identifier specified.
     <li>{@link #GROUP GROUP} - Only user profiles that are group profiles are returned.  These are user profiles that have a group identifier specified.
     <li>{@link #MEMBER MEMBER} - User profiles that are members of the group specified for <em>groupInfo</em> are returned.
     </ul>
     @param  groupInfo  The group profile whose members are to be returned.  Possible values are:
     <ul>
     <li>{@link #NONE NONE} - No group profile is specified.
     <li>{@link #NOGROUP NOGROUP} - Users who are not a member of any group are returned.
     <li>The group profile name - Users who are a member of this group are returned.
     </ul>
     **/
    public UserList(AS400 system, String userInfo, String groupInfo)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing UserList object, system: " + system + ", user info: " + userInfo + ", group info: " + groupInfo);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (userInfo == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'userInfo' is null.");
            throw new NullPointerException("userInfo");
        }
        if (groupInfo == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'groupInfo' is null.");
            throw new NullPointerException("groupInfo");
        }
        if (groupInfo.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'groupInfo' is not valid: '" + groupInfo + "'");
            throw new ExtendedIllegalArgumentException("groupInfo (" + groupInfo + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        if (!userInfo.equals(ALL) && !userInfo.equals(USER) && !userInfo.equals(GROUP) && !userInfo.equals(MEMBER))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'userInfo' is not valid: " + userInfo);
            throw new ExtendedIllegalArgumentException("userInfo (" + userInfo + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        if (userInfo.equals(MEMBER) && groupInfo.equals(NONE))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'groupInfo' is not valid: " + groupInfo);
            throw new ExtendedIllegalArgumentException("groupInfo (" + groupInfo + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (!userInfo.equals(MEMBER) && !groupInfo.equals(NONE))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'groupInfo' is not valid: " + groupInfo);
            throw new ExtendedIllegalArgumentException("groupInfo (" + groupInfo + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        system_ = system;
        userInfo_ = userInfo;
        groupInfo_ = groupInfo;
    }

    /**
     Constructs a UserList object.
     @param  system  The system object representing the system on which the users exists.
     @param  userInfo  The users to be returned.  Possible values are:
     <ul>
     <li>{@link #ALL ALL} - All user profiles and group profiles are returned.
     <li>{@link #USER USER} - Only user profiles that are not group profiles are returned.  These are user profiles that do not have a group identifier specified.
     <li>{@link #GROUP GROUP} - Only user profiles that are group profiles are returned.  These are user profiles that have a group identifier specified.
     <li>{@link #MEMBER MEMBER} - User profiles that are members of the group specified for <em>groupInfo</em> are returned.
     </ul>
     @param  groupInfo  The group profile whose members are to be returned.  Possible values are:
     <ul>
     <li>{@link #NONE NONE} - No group profile is specified.
     <li>{@link #NOGROUP NOGROUP} - Users who are not a member of any group are returned.
     <li>The group profile name - Users who are a member of this group are returned.
     </ul>
     @param  userProfile  The profile names to include in the list.  Possible values are:
     <ul>
     <li>{@link #ALL ALL} - All profiles are returned.
     <li>The user profile name - If a generic profile name is specifed, the profiles that match the the generic name are returned.  If a simple profile name is specified, only that profile is returned.
     </ul>
     **/
    public UserList(AS400 system, String userInfo, String groupInfo, String userProfile)
    {
        this(system, userInfo, groupInfo);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing UserList object, userProfile: " + userProfile);
        if (userProfile == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'userProfile' is null.");
            throw new NullPointerException("userProfile");
        }
        if (userProfile.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'userProfile' is not valid: '" + userProfile + "'");
            throw new ExtendedIllegalArgumentException("userProfile (" + userProfile + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        userProfile_ = userProfile;
    }

    /**
     Adds a PropertyChangeListener.  The specified PropertyChangeListener's <b>propertyChange()</b> method will be called each time the value of any bound property is changed.
     @param  listener  The listener.
     @see  #removePropertyChangeListener
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
     Adds a VetoableChangeListener.  The specified VetoableChangeListener's <b>vetoableChange()</b> method will be called each time the value of any constrained property is changed.
     @param  listener  The listener.
     @see  #removeVetoableChangeListener
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
     Closes the user list on the system.  This releases any system resources previously in use by this user list.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #load
     **/
    public synchronized void close() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Closing user list, handle: ", handle_);
        if (handle_ == null) return;

        ProgramParameter[] parameters = new ProgramParameter[]
        {
            new ProgramParameter(handle_),
            ERROR_CODE
        };
        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYCLST.PGM", parameters);
        if (!pc.run())
        {
            throw new AS400Exception(pc.getMessageList());
        }
        handle_ = null;
        closeHandle_ = false;
    }

    /**
     Returns the group profile whose members are to be returned.
     @return  The group profile whose members are to be returned.  Possible values are:
     <ul>
     <li>{@link #NONE NONE} - No group profile is specified.
     <li>{@link #NOGROUP NOGROUP} - Users who are not a member of any group are returned.
     <li>The group profile name - Users who are a member of this group are returned.
     </ul>
     **/
    public String getGroupInfo()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting group info: " + groupInfo_);
        return groupInfo_;
    }

    /**
     Returns the number of users in the user list. This method implicitly calls {@link #load load()}.
     @return  The number of users, or 0 if no list was retrieved.
     @see  #load
     **/
    public synchronized int getLength()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting user list length.");
        try
        {
            if (handle_ == null || closeHandle_) load();
        }
        catch (Exception e)
        {
            Trace.log(Trace.ERROR, "Exception caught getting length of user list:", e);
            if (e instanceof ExtendedIllegalStateException) throw (ExtendedIllegalStateException)e;
        }

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Length:", length_);
        return length_;
    }

    /**
     Returns the system object representing the system on which the users exist.
     @return  The system object representing the system on which the users exist.  If the system has not been set, null is returned.
     **/
    public AS400 getSystem()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system: " + system_);
        return system_;
    }

    /**
     Returns the description of which users are returned.
     @return  The description of which users are returned.  Possible values are:
     <ul>
     <li>{@link #ALL ALL} - All user profiles and group profiles are returned.
     <li>{@link #USER USER} - Only user profiles that are not group profiles are returned.  These are user profiles that do not have a group identifier specified.
     <li>{@link #GROUP GROUP} - Only user profiles that are group profiles are returned.  These are user profiles that have a group identifier specified.
     <li>{@link #MEMBER MEMBER} - User profiles that are members of the group specified for the group info are returned.
     </ul>
     **/
    public String getUserInfo()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting user info: " + userInfo_);
        return userInfo_;
    }

    /**
     Returns the user profile names of which users are returned.
     @return  The profile names of which users are returned.  Possible values are:
     <ul>
     <li>{@link #ALL ALL} - All profiles are returned.
     <li>The user profile name - If a generic profile name is specifed, the profiles that match the the generic name are returned.  If a simple profile name is specified, only that profile is returned.
     </ul>
     **/
    public String getUserProfile()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting user profile: " + userProfile_);
        return userProfile_;
    }

    /**
     Returns the list of users in the user list.
     @return  An Enumeration of {@link com.ibm.as400.access.User User} objects.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     @exception  RequestNotSupportedException  If the requested function is not supported because the system is not at the correct level.
     @see  #close
     @see  #load
     **/
    public synchronized Enumeration getUsers() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, RequestNotSupportedException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving user list.");
        // Need to get the length.
        if (handle_ == null || closeHandle_) load();

        return new UserEnumeration(this, length_);
    }

    /**
     Returns a subset of the list of users.  This method allows the user to retrieve the user list from the system in pieces.  If a call to {@link #load load()} is made (either implicitly or explicitly), then the users at a given offset will change, so a subsequent call to getUsers() with the same <i>listOffset</i> and <i>number</i> will most likely not return the same Users as the previous call.
     @param  listOffset  The offset into the list of users.  This value must be greater than 0 and less than the list length, or specify -1 to retrieve all of the users.
     @param  number  The number of users to retrieve out of the list, starting at the specified <i>listOffset</i>.  This value must be greater than or equal to 0 and less than or equal to the list length.  If the <i>listOffset</i> is -1, this parameter is ignored.
     @return  The array of retrieved {@link com.ibm.as400.access.User User} objects.  The length of this array may not necessarily be equal to <i>number</i>, depending upon the size of the list on the system, and the specified <i>listOffset</i>.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  com.ibm.as400.access.Job
     @see  #close
     @see  #load
     **/
    public synchronized User[] getUsers(int listOffset, int number) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving user list, list offset: " + listOffset + ", number:", number);
        if (listOffset < -1)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'listOffset' is not valid:", listOffset);
            throw new ExtendedIllegalArgumentException("listOffset (" + listOffset + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        if (number < 0 && listOffset != -1)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'number' is not valid:", number);
            throw new ExtendedIllegalArgumentException("number (" + number + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        if (handle_ == null || closeHandle_) load();

        if (number == 0 && listOffset != -1) return new User[0];

        if (listOffset == -1)
        {
            number = length_;
            listOffset = 0;
        }
        else if (listOffset + number > length_)
        {
            number = length_ - listOffset;
        }

        // AUTU0150 format has 62 bytes per user.
        int lengthOfReceiverVariable = number * 62;

        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Receiver variable, output, char(*).
            new ProgramParameter(lengthOfReceiverVariable),
            // Length of receiver variable, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(lengthOfReceiverVariable)),
            // Request handle, input, char(4).
            new ProgramParameter(handle_),
            // List information, output, char(80).
            new ProgramParameter(80),
            // Number of records to return, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(number)),
            // Starting record, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(listOffset + 1)),
            // Error code, I/0, char(*).
            ERROR_CODE
        };

        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYGTLE.PGM", parameters);
        if (!pc.run())
        {
            throw new AS400Exception(pc.getMessageList());
        }

        byte[] data = parameters[0].getOutputData();
        Converter conv = new Converter(system_.getCcsid(), system_);
        User[] users = new User[number];
        for (int i = 0, offset = 0; i < users.length; ++i, offset += 62)
        {
            String profileName = conv.byteArrayToString(data, offset, 10).trim();
            // 0xF1 = group profile.
            boolean isGroupProfile = data[offset + 10] == (byte)0xF1;
            // 0xF1 = group that has members.
            boolean groupHasMember = data[offset + 11] == (byte)0xF1;
            String textDescription = conv.byteArrayToString(data, offset + 12, 50).trim();
            users[i] = isGroupProfile ? new UserGroup(system_, profileName, groupHasMember, textDescription) : new User(system_, profileName, groupHasMember, textDescription);
        }

        return users;
    }

    /**
     Loads the list of users on the system.  This method informs the system to build a list of users.  This method blocks until the system returns the total number of users it has compiled.  A subsequent call to {@link #getUsers getUsers()} will retrieve the actual user information and attributes for each user in the list from the system.
     <p>This method updates the list length.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #getLength
     @see  #close
     **/
    public synchronized void load() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Loading user list.");
        if (system_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot connect to server before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        // Close the previous list.
        if (closeHandle_) close();

        // Generate text objects based on system CCSID.
        Converter conv = new Converter(system_.getCcsid(), system_);
        byte[] selectionCriteria = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        conv.stringToByteArray(userInfo_, selectionCriteria);
        byte[] groupProfileName = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        conv.stringToByteArray(groupInfo_.toUpperCase().trim(), groupProfileName);
        byte[] profileName = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        conv.stringToByteArray(userProfile_.toUpperCase().trim(), profileName);

        // Setup program parameters.
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Receiver variable, output, char(*).
            new ProgramParameter(0),
            // Length of receiver variable, input, binary(4).
            new ProgramParameter(new byte[] { 0x00, 0x00, 0x00, 0x00 } ),
            // List information, output, char(80).
            new ProgramParameter(80),
            // Number of records to return, input, binary(4).
            new ProgramParameter(new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF } ),
            // Format name, input, char(8), EBCDIC 'AUTU0150'.
            new ProgramParameter(new byte[] { (byte)0xC1, (byte)0xE4, (byte)0xE3, (byte)0xE4, (byte)0xF0, (byte)0xF1, (byte)0xF5, (byte)0xF0 } ),
            // Selection criteria, input, char(10).
            new ProgramParameter(selectionCriteria),
            // Group profile name, input, char(10).
            new ProgramParameter(groupProfileName),
            // Error code, I/0, char(*).
            ERROR_CODE,
            // Profile name, input, char(10).
            new ProgramParameter(profileName),
        };

        // Call the program.
        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYOLAUS.PGM", parameters);
        if (!pc.run())
        {
            throw new AS400Exception(pc.getMessageList());
        }

        // List information returned.
        byte[] listInformation = parameters[2].getOutputData();
        // Check the list status indicator.
        ListUtilities.checkListStatus(listInformation[30]);

        handle_ = new byte[4];
        System.arraycopy(listInformation, 8, handle_, 0, 4);
        length_ = BinaryConverter.byteArrayToInt(listInformation, 0);

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Loaded user list, length: " + length_ + ", handle: ", handle_);
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
     Sets the group profile whose members are to be returned.
     <p>This must be set to a group profile name or {@link #NOGROUP NOGROUP} if group info is set to {@link #MEMBER MEMBER}.  This must be set to {@link #NONE NONE} if group info is not set to {@link #MEMBER MEMBER}.
     @param  groupInfo  The group profile whose members are to be returned.  Possible values are:
     <ul>
     <li>{@link #NONE NONE} - No group profile is specified.
     <li>{@link #NOGROUP NOGROUP} - Users who are not a member of any group are returned.
     <li>The group profile name - Users who are a member of this group are returned.
     </ul>
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setGroupInfo(String groupInfo) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting group info: " + groupInfo);
        if (groupInfo == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'groupInfo' is null.");
            throw new NullPointerException("groupInfo");
        }
        if (groupInfo.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'groupInfo' is not valid: '" + groupInfo + "'");
            throw new ExtendedIllegalArgumentException("groupInfo (" + groupInfo + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            synchronized (this)
            {
                groupInfo_ = groupInfo;
                if (handle_ != null) closeHandle_ = true;
            }
        }
        else
        {
            String oldValue = groupInfo_;
            String newValue = groupInfo;
            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("groupInfo", oldValue, newValue);
            }
            synchronized (this)
            {
                groupInfo_ = groupInfo;
                if (handle_ != null) closeHandle_ = true;
            }
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("groupInfo", oldValue, newValue);
            }
        }
    }

    /**
     Sets the system object representing the system on which the users exist.  The system cannot be changed once a connection to the system has been established.
     @param  system  The system object representing the system on which the users exists.
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
        if (handle_ != null)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'system' after connect.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
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
            system_ = system;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("system", oldValue, newValue);
            }
        }
    }

    /**
     Sets which users are returned.
     @param  userInfo  A description of which users are returned.  Possible values are:
     <ul>
     <li>{@link #ALL ALL} - All user profiles and group profiles are returned.
     <li>{@link #USER USER} - Only user profiles that are not group profiles are returned.  These are user profiles that do not have a group identifier specified.
     <li>{@link #GROUP GROUP} - Only user profiles that are group profiles are returned.  These are user profiles that have a group identifier specified.
     <li>{@link #MEMBER MEMBER} - User profiles that are members of the group specified for the group info are returned.
     </ul>
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setUserInfo(String userInfo) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting user info: " + userInfo);
        if (userInfo == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'userInfo' is null.");
            throw new NullPointerException("userInfo");
        }

        if (!userInfo.equals(ALL) && !userInfo.equals(USER) && !userInfo.equals(GROUP) && !userInfo.equals(MEMBER))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'userInfo' is not valid: " + userInfo);
            throw new ExtendedIllegalArgumentException("userInfo", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            synchronized (this)
            {
                userInfo_ = userInfo;
                if (handle_ != null) closeHandle_ = true;
            }
        }
        else
        {
            String oldValue = userInfo_;
            String newValue = userInfo;
            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("userInfo", oldValue, newValue);
            }
            synchronized (this)
            {
                userInfo_ = userInfo;
                if (handle_ != null) closeHandle_ = true;
            }
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("userInfo", oldValue, newValue);
            }
        }
    }

    /**
     Sets which profile names to include in the list.
     @param  userProfile  The profile names to include in the list.  Possible values are:
     <ul>
     <li>{@link #ALL ALL} - All profiles are returned.
     <li>The user profile name - If a generic profile name is specifed, the profiles that match the the generic name are returned.  If a simple profile name is specified, only that profile is returned.
     </ul>
     **/
    public void setUserProfile(String userProfile)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting user profile: " + userProfile);
        if (userProfile == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'userProfile' is null.");
            throw new NullPointerException("userProfile");
        }
        if (userProfile.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'userProfile' is not valid: '" + userProfile + "'");
            throw new ExtendedIllegalArgumentException("userProfile (" + userProfile + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        synchronized (this)
        {
            userProfile_ = userProfile;
            if (handle_ != null) closeHandle_ = true;
        }
    }
}
