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
import java.util.Enumeration;



/**
The UserList class represents a list of AS/400 users.

<p>Some of the selections have associated get and set methods
defined in this class.  These are provided for backwards compatibility
with previous versions of the AS/400 Toolbox for Java.  The complete
set of selections can be accessed using the
{@link com.ibm.as400.resource.RUserList  RUserList }
class.

@see com.ibm.as400.resource.RUserList
**/
public class UserList
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;

//-----------------------------------------------------------------------------------------
// Selection values.
//-----------------------------------------------------------------------------------------

/**
Selection value indicating that the list contains all user profiles
and group profiles.
**/
    public static final String ALL = RUserList.ALL;

/**
Selection value indicating that the list contains only user profiles
that are not group profiles.  These are user profiles that do not have
a group identifier specified.
**/
    public static final String USER = RUserList.USER;

/**
Selection value indicating that the list contains only user profiles
that are group profiles.  These are user profiles that have a group
identifier specified.
**/
    public static final String GROUP = RUserList.GROUP;

/**
Selection value indicating that the list contains only user profiles
that are members of a specified group.
**/
    public static final String MEMBER = RUserList.MEMBER;


/**
Selection value indicating that no group profile is specified.
**/
    public static final String NONE = RUserList.NONE;

/**
Selection value indicating that the list contains only user profiles
that are not group profiles.  These are user profiles that do not have
a group identifier specified.
**/
    public static final String NOGROUP = RUserList.NOGROUP;




//-----------------------------------------------------------------------------------------
// Private data.
//-----------------------------------------------------------------------------------------

    private RUserList       rUserList_          = null;

    private transient   PropertyChangeSupport   propertyChangeSupport_;
    private transient   VetoableChangeSupport   vetoableChangeSupport_;



//-----------------------------------------------------------------------------------------
// Code.
//-----------------------------------------------------------------------------------------

/**
Constructs a UserList object.
**/
    public UserList()
    {
        rUserList_ = new RUserList();
        initializeTransient();
    }



/**
Constructs a UserList object.

@param system   The system.
**/
    public UserList(AS400 system)
    {
        rUserList_ = new RUserList(system);
        initializeTransient();
    }



/**
Constructs a UserList object.

@param system       The system.
@param userInfo     The users to be returned.  Possible values are:
                    <ul>
                    <li><a href="#ALL">ALL</a> - All user profiles and group profiles are
                        returned.
                    <li><a href="#USER">USER</a> - Only user profiles that are not group
                        profiles are returned.  These are user profiles that do not have
                        a group identifier specified.
                    <li><a href="#GROUP">GROUP</a> - Only user profiles that are group
                        profiles are returned.  These are user profiles that have
                        a group identifier specified.
                    <li><a href="#MEMBER">MEMBER</a> - User profiles that are members
                        of the group specified for <em>groupInfo</em> are returned.
                    </ul>
@param groupInfo    The group profile whose members are to be returned.  Possible values are:
                    <ul>
                    <li><a href="#NONE">NONE</a> - No group profile is specified.
                    <li><a href="#NOGROUP">NOGROUP</a> - Users who are not a member of
                        any group are returned.
                    <li>The group profile name - Users who are a member of this group are
                        returned.
                    </ul>
**/
    public UserList(AS400 system, String userInfo, String groupInfo)
    {
        rUserList_ = new RUserList(system);
        validateSelectionCombination(userInfo, groupInfo);
        try {
            rUserList_.setSelectionValue(RUserList.SELECTION_CRITERIA, userInfo);
            rUserList_.setSelectionValue(RUserList.GROUP_PROFILE, groupInfo);
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error setting selection value", e);
        }
        initializeTransient();
    }



/**
Adds a PropertyChangeListener.  The specified PropertyChangeListener's
<b>propertyChange()</b> method will be called each time the value of
any bound property is changed.

@param listener The listener.
*/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport_.addPropertyChangeListener(listener);
        rUserList_.addPropertyChangeListener(listener);
    }



/**
Adds a VetoableChangeListener.  The specified VetoableChangeListener's
<b>vetoableChange()</b> method will be called each time the value of
any constrained property is changed.

@param listener The listener.
*/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        vetoableChangeSupport_.addVetoableChangeListener(listener);
        rUserList_.addVetoableChangeListener(listener);
    }



/**
Returns the group profile whose members are to be returned.

@return The group profile whose members are to be returned.
        Possible values are:
        <ul>
        <li><a href="#NONE">NONE</a> - No group profile is specified.
        <li><a href="#NOGROUP">NOGROUP</a> - Users who are not a member of
            any group are returned.
        <li>The group profile name - Users who are a member of this group are
            returned.
        </ul>

@see com.ibm.as400.resource.RUserList#GROUP_PROFILE
**/
    public String getGroupInfo()
    {
        return getSelectionValueAsString(RUserList.GROUP_PROFILE);
    }



/**
Returns the number of users in the list.

@return The number of users, or 0 if no list has been retrieved.
**/
    public int getLength()
    {
        try {
            rUserList_.waitForComplete();
            return (int)rUserList_.getListLength();
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "An error occurred while getting the user list length", e);
            return 0;
        }
    }



/*-------------------------------------------------------------------------
Convenience method for getting selection values.
All ResourceExceptions are swallowed!
-------------------------------------------------------------------------*/
    private String getSelectionValueAsString(Object selectionID)
    {
        try {
            return (String)rUserList_.getSelectionValue(selectionID);
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error getting selection value", e);
            return null;
        }
    }



/**
Returns the system.

@return The system.
**/
    public AS400 getSystem()
    {
        return rUserList_.getSystem();
    }


/**
Returns the description of which users are returned.

@return The description of which users are returned.  Possible values are:
        <ul>
        <li><a href="#ALL">ALL</a> - All user profiles and group profiles are
            returned.
        <li><a href="#USER">USER</a> - Only user profiles that are not group
            profiles are returned.  These are user profiles that do not have
            a group identifier specified.
        <li><a href="#GROUP">GROUP</a> - Only user profiles that are group
            profiles are returned.  These are user profiles that have
            a group identifier specified.
        <li><a href="#MEMBER">MEMBER</a> - User profiles that are members
            of the group specified for the group info are returned.
        </ul>

@see com.ibm.as400.resource.RUserList#SELECTION_CRITERIA
**/
    public String getUserInfo()
    {
        return getSelectionValueAsString(RUserList.SELECTION_CRITERIA);
    }



/**
Returns the list of users in the user list.

@return An Enumeration of <a href="User.html">User</a> objects.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception RequestNotSupportedException    If the requested function is not supported because the AS/400 system is not at the correct level.
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
        try {
            rUserList_.refreshContents(); // @B1A
            return new EnumerationAdapter(rUserList_.resources());
        }
        catch(ResourceException e) {
            e.unwrap();
            return null;
        }
    }



/**
Initializes the transient data.
**/
    private void initializeTransient()
    {
        propertyChangeSupport_      = new PropertyChangeSupport(this);
        vetoableChangeSupport_      = new VetoableChangeSupport(this);
    }



/**
Deserializes the resource.
**/
    private void readObject(ObjectInputStream in)
    throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        initializeTransient ();
    }



/**
Removes a PropertyChangeListener.

@param listener The listener.
**/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport_.removePropertyChangeListener(listener);
        rUserList_.removePropertyChangeListener(listener);
    }



/**
Removes a VetoableChangeListener.

@param listener The listener.
**/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        vetoableChangeSupport_.removeVetoableChangeListener(listener);
        rUserList_.removeVetoableChangeListener(listener);
    }



/**
Sets the group profile whose members are to be returned.

<p>This must be set to a group profile name or <a href="#NOGROUP">NOGROUP</a>
if group info is set to <a href="#MEMBER">MEMBER</a>.  This must be set to
<a href="#NONE">NONE</a> if group info is not set to
<a href="#MEMBER">MEMBER</a>.

@param groupInfo    The group profile whose members are to be returned.
                    Possible values are:
                    <ul>
                    <li><a href="#NONE">NONE</a> - No group profile is specified.
                    <li><a href="#NOGROUP">NOGROUP</a> - Users who are not a member of
                        any group are returned.
                    <li>The group profile name - Users who are a member of this group are
                        returned.
                    </ul>

@exception PropertyVetoException If the change is vetoed.

@see com.ibm.as400.resource.RUserList#GROUP_PROFILE
**/
    public void setGroupInfo(String groupInfo)
        throws PropertyVetoException
    {
        if (groupInfo == null)
            throw new NullPointerException("groupInfo");

        String oldValue = getGroupInfo();
        vetoableChangeSupport_.fireVetoableChange("groupInfo", oldValue, groupInfo);
        setSelectionValueAsString(RUserList.GROUP_PROFILE, groupInfo);
        propertyChangeSupport_.firePropertyChange("groupInfo", oldValue, groupInfo);
    }



/*-------------------------------------------------------------------------
Convenience method for setting selection values.
All ResourceExceptions are swallowed!
-------------------------------------------------------------------------*/
    private void setSelectionValueAsString(Object selectionID, Object value)
    {
        try {
            rUserList_.setSelectionValue(selectionID, value);
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error setting selection value", e);
        }
    }



/**
Sets the system.  This cannot be changed if the object
has established a connection to the AS/400.

@param system The system.

@exception PropertyVetoException    If the property change is vetoed.
**/
    public void setSystem(AS400 system)
    throws PropertyVetoException
    {
        rUserList_.setSystem(system);
    }



/**
Sets which users are returned.  Possible values are:

@param userInfo A description of which users are returned.
                Possible values are:
                <ul>
                <li><a href="#ALL">ALL</a> - All user profiles and group profiles are
                    returned.
                <li><a href="#USER">USER</a> - Only user profiles that are not group
                    profiles are returned.  These are user profiles that do not have
                    a group identifier specified.
                <li><a href="#GROUP">GROUP</a> - Only user profiles that are group
                    profiles are returned.  These are user profiles that have
                    a group identifier specified.
                <li><a href="#MEMBER">MEMBER</a> - User profiles that are members
                    of the group specified for the group info are returned.
                </ul>

@exception PropertyVetoException If the change is vetoed.

@see com.ibm.as400.resource.RUserList#SELECTION_CRITERIA
**/
    public void setUserInfo(String userInfo)
        throws PropertyVetoException
    {
        if (userInfo == null)
            throw new NullPointerException("userInfo");

        String oldValue = getUserInfo();
        vetoableChangeSupport_.fireVetoableChange("userInfo", oldValue, userInfo);
        setSelectionValueAsString(RUserList.SELECTION_CRITERIA, userInfo);
        propertyChangeSupport_.firePropertyChange("userInfo", oldValue, userInfo);
    }



/**
Validates the selection combination.

@param userInfo     The user info.
@param groupInfo    The group info.
**/
    private static void validateSelectionCombination(String userInfo, String groupInfo)
    {
        if ((userInfo.equals(MEMBER)) && (groupInfo.equals(NONE)))
            throw new IllegalArgumentException("groupInfo");
        if ((!(userInfo.equals(MEMBER))) && (!(groupInfo.equals(NONE))))
            throw new IllegalArgumentException("groupInfo");
    }



/**
Converts the Enumeration (whose elements are RUser objects)
to an Enumeration whose elements are User objects.
**/
    private static class EnumerationAdapter implements Enumeration
    {
       private Enumeration rEnum_;

       public EnumerationAdapter(Enumeration rEnum)
       {
           rEnum_ = rEnum;
       }

       public boolean hasMoreElements()
       {
           return rEnum_.hasMoreElements();
       }

       public Object nextElement()
       {
           return new User((RUser)rEnum_.nextElement());
       }
    }



}
