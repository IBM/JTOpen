///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import java.net.UnknownHostException;

import java.util.Enumeration;
import java.util.Vector;

/**
 * The UserList class represents a list of AS/400 users.  
 *
 * <p>For example:
 * <pre>
 * UserList userList = new UserList( as400 );
 * Enumeration e = userList.getUsers ();
 * while (e.hasMoreElements ())
 * {
 *   User u = (User) e.nextElement ();
 *   System.out.println (u);
 * }
 * </pre>
 *
 * <p>UserList objects generate the following events:
 * <ul>
 * <li>PropertyChangeEvent
 * </ul>
 *
 * @see User
**/
public class UserList implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Descriptions of choices for user info,
    // taken from the QGYOLAUS API documentation.
    /**
     * Constant indicating that all user profile names and group
     * profile names are returned.
    **/
    public static final String ALL = "*ALL";
    
    /**
     * Constant indicating that user names that are not group profiles
     * are returned.  These are user profiles that do not have a group  identifier
     * specified.
    **/
    public static final String USER = "*USER";
    
    /**
     * Constant indicating that user names that are group profiles are
     * returned.  These are user profiles that have a group  identifier specified.
    **/
    public static final String GROUP = "*GROUP";
    
    /**
     * Constant indicating that user names that are members of the
     * group specified by the group info property are returned.
    **/
    public static final String MEMBER = "*MEMBER";
    
    
      // Descriptions of choices for group info,
      // taken from the QGYOLAUS API documentation.
    /**
     * Constant indicating that no group profile is specified.
    **/
    public static final String NONE = "*NONE";
    
    /**
     * Constant indicating that users who are not a member of any
     * group are returned.
    **/
    public static final String NOGROUP = "*NOGROUP";



    private static AS400Bin4      intType             = new AS400Bin4 ();
    
    
    // * Properties
    
    private AS400                 as400_              = null;
    private String                userInfo_           = ALL;
    private String                groupInfo_          = NONE;
    transient private UserListEnumeration   lastEnumeration_    = null;
    
    // * Properties
    
    transient private PropertyChangeSupport changes = 
                  new PropertyChangeSupport(this);
    transient private VetoableChangeSupport vetos = 
                  new VetoableChangeSupport(this);


    /**
    * Constructs a UserList object.
    *
    * The system property needs to be set before using
    * any method that requires a connection to the AS/400.
    **/
    public UserList()
    {
            
    }
 
    /**
    * Constructs a UserList object.
    *
    * <p>Depending on how the AS400 object was constructed, the user may
    * need to be prompted for the system name, user ID, or password
    * when any method requiring a connection to the AS/400 is used.
    *
    * @param system The AS/400 system from which the list of users will
    *               be retrieved.  This value cannot be null.
    **/
    public UserList( AS400 system )
    {
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        this.as400_ = system;
    }

    // @A1A : Added a constructor to handle group info.
    /**
    * Constructs a UserList object.
    *
    * <p>Depending on how the AS400 object was constructed, the user may
    * need to be prompted for the system name, user ID, or password
    * when any method requiring a connection to the AS/400 is used.
    *
    * @param system The AS/400 system from which the list of users will
    *               be retrieved.  This value cannot be null.
    * @param userInfo The user information.
    *           The valid values are:
    *           <ul>
    *             <li> ALL
    *             <li> USER
    *             <li> GROUP
    *             <li> MEMBER
    *           </ul>
    * @param groupInfo The group information.
    *            The valid values are:
    *            <ul>
    *              <li>NOGROUP
    *              <li>NONE
    *              <li>A group identifier.
    *            </ul>
    **/
    public UserList( AS400  system,
                     String userInfo,
                     String groupInfo )
    {
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        this.as400_ = system;
        
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
        if(userInfo.equals(MEMBER)&&groupInfo.equals(NONE))
            throw new ExtendedIllegalArgumentException (
                  "argument(" + groupInfo + ")",
                  ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        if(!(userInfo.equals(MEMBER))&&!(groupInfo.equals(NONE)))
            throw new ExtendedIllegalArgumentException (
                  "argument(" + groupInfo + ")",
                  ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);          

        this.userInfo_ = userInfo;
                
        this.groupInfo_ = groupInfo;
    
    }
    
    /**
    * Adds a listener to be notified when the value of any bound
    * property is changed. The <i>propertyChange()</i> method will be be called.
    *
    * @param listener The PropertyChangeListener.
    **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        this.changes.addPropertyChangeListener(listener);
    }
    
    /**
    * Adds a listener to be notified when the value of any constrained
    * property is changed. The <i>vetoableChange()</i> method will be called.
    *
    * @param listener The VetoableChangeListener.
    **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        this.vetos.addVetoableChangeListener(listener);
    }
    
    /**
    * Copyright.
    **/
    private static String getCopyright ()
    {
        return Copyright.copyright;
    }

    /**
    * Returns the group information that describes which users are returned.
    *
    * @return The group information that describes which users are returned.
    **/
    public String getGroupInfo()
    {
        return groupInfo_;
    }
    
    
    /**
    * Returns the number of users in the list that were most recently
    * retrieved from the AS/400 (the last call to <i>getUsers()</i>).
    *
    * @return The number of users, or 0 if no list has been retrieved.
    **/
    public int getLength()
    {
        if (lastEnumeration_ == null)
                return 0;
        else
            return lastEnumeration_.getLength ();
    }

    
    /**
    * Returns the AS/400 system from which the list of users will
    * be retrieved.
    *
    * @return The AS/400 system from which the list of users will
    *         be retrieved.
    **/
    public AS400 getSystem()
    {
        return this.as400_;
    }
    
    
    /**
    * Returns the user information that describes which users are returned
    * in the list.
    *
    * @return The user information.
    **/
    public String getUserInfo()
    {
        return userInfo_;
    }
    
    
    
    /**
    * Returns a list of users defined on the AS/400.
    * A valid AS/400 system must be provided before this call is made.
    *
    * @return An Enumeration of <i>User</i> objects.
    *
    * @exception AS400Exception                  If the AS/400 system returns an error message.
    * @exception AS400SecurityException          If a security or authority error occurs.
    * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
    * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
    * @exception InterruptedException            If this thread is interrupted.
    * @exception IOException                     If an error occurs while communicating with the AS/400.
    * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
    * @exception RequestNotSupportedException    If the requested function is not supported because the AS/400 system is not at the correct level.
    **/
    public Enumeration getUsers ()
           throws AS400Exception, 
                  AS400SecurityException, 
                  ErrorCompletingRequestException,
                  InterruptedException, 
                  IOException, 
                  ObjectDoesNotExistException,
                  // @A2D PropertyVetoException,
                  RequestNotSupportedException
   {
        if (this.as400_ == null)
        {
            Trace.log(Trace.ERROR, "system is null");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
//@A2D        if (userInfo_ == null)
//@A2D        {
//@A2D            Trace.log(Trace.ERROR, "Parameter 'userInfo' is null.");
//@A2D            throw new NullPointerException("userInfo");
//@A2D        }
//@A2D        if (groupInfo_ == null)
//@A2D        {
//@A2D            Trace.log(Trace.ERROR, "Parameter 'groupInfo' is null.");
//@A2D            throw new NullPointerException("groupInfo");
//@A2D        }
        
        UserGroupAccess access = new UserGroupAccess(as400_);
        Vector vector = null;                                           // @A2A
        try {                                                           // @A2A
            vector = access.retrieveUsersData(userInfo_,groupInfo_);    // @A2C
        }                                                               // @A2A
        catch (PropertyVetoException e) {                               // @A2A
            // Ignore.                                                  // @A2A
        }                                                               // @A2A
        byte[] listInfoData = (byte[])vector.elementAt(0);
        byte[] receiverData = (byte[])vector.elementAt(1);

        // Create and return the enumeration.
        lastEnumeration_ = new UserListEnumeration (as400_, listInfoData, receiverData);
        return lastEnumeration_;
    }
        
        
    /** Deserializes and initializes transient data.
     *
    **/
     
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        
        this.changes = new PropertyChangeSupport(this);
        this.vetos = new VetoableChangeSupport(this);    this.vetos = new VetoableChangeSupport(this);
    }
    
    
    /**
    * Removes a property change listener from the listener list.
    * @param listener The PropertyChangeListener.
    **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        this.changes.removePropertyChangeListener(listener);
    }
    
    /**
    * Removes a vetoable change listener from the listener list.
    * @param listener The VetoableChangeListener.
    **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        this.vetos.removeVetoableChangeListener(listener);
    }
    
    /**
    * Sets the group information that describes which users are returned.
    * The default group information is NONE.  This must be set to a valid
    * group identifier or NOGROUP when the user information
    * property is MEMBER.  This takes effect the next time that
    * <i>getUsers()</i> is called.
    *
    * @param groupInfo The group information
    *            The valid values for this parameter are:
    *            <ul>
    *              <li>NOGROUP
    *              <li>NONE
    *              <li>A group identifier.
    *            </ul>
    *            This value cannot be null.
    *
    * @exception PropertyVetoException If the change is vetoed.
    **/
    public void setGroupInfo( String groupInfo ) throws PropertyVetoException
    {
        if (groupInfo == null)
        {
           Trace.log(Trace.ERROR, "Parameter 'groupInfo' is null.");
           throw new NullPointerException("groupInfo");
        }

        String old = this.groupInfo_;
        this.vetos.fireVetoableChange("groupInfo", old, groupInfo );

        this.groupInfo_ = groupInfo;
        
        this.changes.firePropertyChange("groupInfo", old, groupInfo );
    }
    
    /**
    * Sets the AS/400 system from which the list of users will be retrieved.
    *
    * @param system The AS/400 system from which the list of users will
    *               be retrieved.  This value cannot be null.
    *
    * @exception PropertyVetoException If the change is vetoed.
    **/
    public void setSystem( AS400 system ) throws PropertyVetoException
    {
        AS400 newValue,oldValue;
        if (system == null)
        {
            throw new NullPointerException("system");
        }
        if (as400_!=null)
        {
            if (as400_.isConnected())
            {
                throw new ExtendedIllegalStateException("system",
                    ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
            }
            oldValue = as400_;
            if (as400_.equals(system)==true)
               return;
        }
        else 
            oldValue = null;
        
        vetos.fireVetoableChange("system", oldValue, system );
        as400_ = system;
        newValue = system;
        changes.firePropertyChange("system", oldValue,newValue);

        return;
    }
    
    
    /**
    * Sets the user information that describes which users are returned.
    * The default is ALL.  If MEMBER is specified, then
    * the group info property must be set to a valid group identifier or
    * NOGROUP. This takes effect the next time that
    * <i>getUsers()</i> is called.
    *
    * @param userInfo The user information.
    *           The valid values are:
    *           <ul>
    *             <li> ALL
    *             <li> USER
    *             <li> GROUP
    *             <li> MEMBER
    *           </ul>
    *           This value cannot be null.
    *
    * @exception PropertyVetoException If the change is vetoed.
    **/
    public void setUserInfo( String userInfo ) throws PropertyVetoException
    {
        if (userInfo == null)
        {
          Trace.log(Trace.ERROR, "Parameter 'userInfo' is null.");
          throw new NullPointerException("userInfo");
        }

        String old = this.userInfo_;
        this.vetos.fireVetoableChange("userInfo", old, userInfo );


        if (userInfo.equals (ALL)
        ||  userInfo.equals (USER)
        ||  userInfo.equals (GROUP)
        ||  userInfo.equals (MEMBER))
        {
            this.userInfo_ = userInfo;
        }
        else
        {
            throw new ExtendedIllegalArgumentException ("argument(" + userInfo + ")",
                      ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        this.changes.firePropertyChange("userInfo", old, userInfo );
    
    }

    /**
     * Returns the string representing the user list.
     * @return The string representing the user list.
    **/
    public String toString()
    {
        return groupInfo_;
    }
    

}
