///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: User.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import com.ibm.as400.resource.ResourceException;
import com.ibm.as400.resource.RUser;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;



/**
The User class represents an AS/400 user profile and directory entry.

<p>Some of the attributes have associated get and set methods
defined in this class.  These are provided for backwards compatibility
with previous versions of the AS/400 Toolbox for Java.  The complete
set of attribute values can be accessed using the
{@link com.ibm.as400.resource.RUser  RUser } class.

@see com.ibm.as400.resource.RUser
**/
public class User
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;

//-----------------------------------------------------------------------------------------
// Private data.
//-----------------------------------------------------------------------------------------

    private RUser                           rUser_                  = null;




/**
Constructs a User object.
**/
    public User()
    {
        rUser_ = new RUser();
    }



/**
Constructs a User object.

@param system   The system.
@param name     The user profile name.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.
**/
    public User(AS400 system,String name)
        throws AS400Exception,
               AS400SecurityException,
               ConnectionDroppedException,
               ErrorCompletingRequestException,
               InterruptedException,
               ObjectDoesNotExistException,
               IOException,
               UnsupportedEncodingException
    {
        rUser_ = new RUser(system, name);
    }


/**
Constructs a User object.

@param rUser    The RUser object.
**/
//
// This is a package scope constructor!
//
    User(RUser rUser)
    {
        rUser_ = rUser;
    }



/**
Adds a PropertyChangeListener.  The specified PropertyChangeListener's
<b>propertyChange()</b> method will be called each time the value of
any bound property is changed.

@param listener The listener.
*/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        rUser_.addPropertyChangeListener(listener);
    }



/**
Adds a VetoableChangeListener.  The specified VetoableChangeListener's
<b>vetoableChange()</b> method will be called each time the value of
any constrained property is changed.

@param listener The listener.
*/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        rUser_.addVetoableChangeListener(listener);
    }



/**
Returns the accounting code that is associated with this user.

@return The accounting code that is associated with this user.

@see com.ibm.as400.resource.RUser#ACCOUNTING_CODE
**/
    public String getAccountingCode()
    {
        return getAttributeValueAsString(RUser.ACCOUNTING_CODE);
    }


/**
Returns the user interface that the user will use.

@return The user interface that the user will use.  Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#SYSTEM_VALUE SYSTEM_VALUE }
            - The system value QASTLVL determines which user interface the user is using.
        <li>{@link com.ibm.as400.resource.RUser#ASSISTANCE_LEVEL_BASIC ASSISTANCE_LEVEL_BASIC }
            - The Operational Assistant user interface.
        <li>{@link com.ibm.as400.resource.RUser#ASSISTANCE_LEVEL_INTERMEDIATE ASSISTANCE_LEVEL_INTERMEDIATE }
            - The system user interface.
        <li>{@link com.ibm.as400.resource.RUser#ASSISTANCE_LEVEL_ADVANCED ASSISTANCE_LEVEL_ADVANCED }
            - The expert system user interface.
        </ul>

@see com.ibm.as400.resource.RUser#ASSISTANCE_LEVEL
**/
    public String getAssistanceLevel()
    {
        return getAttributeValueAsString(RUser.ASSISTANCE_LEVEL);
    }


/**
Returns the fully qualified integrated file system path name of the
attention key handling program for this user.

@return The fully qualified integrated file system path name of the
        attention key handling program for this user. Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#SYSTEM_VALUE SYSTEM_VALUE }
            - The system value QATNPGM determines the user's attention key handling program.
        <li>{@link com.ibm.as400.resource.RUser#NONE NONE }
            - No attention key handling program is used.
        <li>{@link com.ibm.as400.resource.RUser#ATTENTION_KEY_HANDLING_PROGRAM_ASSIST ATTENTION_KEY_HANDLING_PROGRAM_ASSIST }
            - The Operational Assistant attention key handling program.
        <li>The attention key handling program name.
        </ul>

@see com.ibm.as400.resource.RUser#ATTENTION_KEY_HANDLING_PROGRAM
@see QSYSObjectPathName
**/
    public String getAttentionKeyHandlingProgram()
    {
        return getAttributeValueAsString(RUser.ATTENTION_KEY_HANDLING_PROGRAM);
    }



/*-------------------------------------------------------------------------
Convenience methods for getting attribute values.
All ResourceExceptions are swallowed!
-------------------------------------------------------------------------*/
    private boolean getAttributeValueAsBoolean(Object attributeID)
    {
        try {
            return ((Boolean)rUser_.getAttributeValue(attributeID)).booleanValue();
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error getting attribute value", e);
            return false;
        }
    }



    private int getAttributeValueAsInt(Object attributeID)
    {
        try {
            return ((Integer)rUser_.getAttributeValue(attributeID)).intValue();
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error getting attribute value", e);
            return -1;
        }
    }



    private long getAttributeValueAsLong(Object attributeID)
    {
        try {
            return ((Long)rUser_.getAttributeValue(attributeID)).longValue();
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error getting attribute value", e);
            return -1;
        }
    }



    private Object getAttributeValueAsObject(Object attributeID)
    {
        try {
            return rUser_.getAttributeValue(attributeID);
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error getting attribute value", e);
            return null;
        }
    }



    private String getAttributeValueAsString(Object attributeID)
    {
        try {
            return (String)rUser_.getAttributeValue(attributeID);
        }
        catch(ResourceException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error getting attribute value", e);
            return null;
        }
    }



/**
Returns the character code set ID to be used by the system for this user.

@return The character code set ID to be used by the system for this user.

@see com.ibm.as400.resource.RUser#CHARACTER_CODE_SET_ID
**/
    public int getCCSID()
    {
        return getAttributeValueAsInt(RUser.CHARACTER_CODE_SET_ID);
    }



/**
Returns the country ID used by the system for this user.

@return The country ID used by the system for this user.  Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#SYSTEM_VALUE SYSTEM_VALUE }
            - The system value QCNTRYID will be used to determine the country ID.
        <li>A country ID.
        </ul>

@see com.ibm.as400.resource.RUser#COUNTRY_ID
**/
    public String getCountryID()
    {
        return getAttributeValueAsString(RUser.COUNTRY_ID);
    }



/**
Returns the name of the user's current library.

@return The name of the user's current library.  Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#CURRENT_LIBRARY_NAME_DEFAULT CURRENT_LIBRARY_NAME_DEFAULT }
            - The user does not have a current library.
        <li>A library name.
        </ul>

@see com.ibm.as400.resource.RUser#CURRENT_LIBRARY_NAME
**/
    public String getCurrentLibraryName()
    {
        return getAttributeValueAsString(RUser.CURRENT_LIBRARY_NAME);
    }



/**
Returns the number of days until the password will expire.

@return The number of days until the password will expire.  Possible values are:
        <ul>
        <li>0 - The password is expired.
        <li>1-7 - The number of days until the password expires.
        <li>-1 - The password will not expire in the next 7 days.
        </ul>

@see com.ibm.as400.resource.RUser#DAYS_UNTIL_PASSWORD_EXPIRES
**/
    public int getDaysUntilPasswordExpire()
    {
        return getAttributeValueAsInt(RUser.DAYS_UNTIL_PASSWORD_EXPIRES);
    }



/**
Returns the descriptive text for the user profile.

@return The descriptive text for the user profile.

@see com.ibm.as400.resource.RUser#TEXT_DESCRIPTION
**/
    public String getDescription()
    {
        return getAttributeValueAsString(RUser.TEXT_DESCRIPTION);
    }



/**
Returns whether the sign-on information display is shown when
the user signs on.

@return Whether the sign-on information display is shown when
        the user signs on.  Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#SYSTEM_VALUE SYSTEM_VALUE }
            - The system value QDSPSGNINF determines if the sign-on information display
            is shown when the user signs on.
        <li>{@link com.ibm.as400.resource.RUser#YES YES }
            - The sign-on information display is shown when the user signs on.
        <li>{@link com.ibm.as400.resource.RUser#NO NO }
            - The sign-on information display is not shown when the user signs on.
        </ul>

@see com.ibm.as400.resource.RUser#DISPLAY_SIGN_ON_INFORMATION
**/
    public String getDisplaySignOnInformation()
    {
        return getAttributeValueAsString(RUser.DISPLAY_SIGN_ON_INFORMATION);
    }



/**
Returns the authority the user's group profile
has to objects the user creates.

@return The authority the user's group profile
        has to objects the user creates.  Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#NONE NONE }
            - The group profile has no authority to the objects the user creates,
            or the user does not have a group profile.
        <li>{@link com.ibm.as400.resource.RUser#GROUP_AUTHORITY_ALL GROUP_AUTHORITY_ALL }
            - The group profile has all authority to the objects the user creates.
        <li>{@link com.ibm.as400.resource.RUser#GROUP_AUTHORITY_CHANGE GROUP_AUTHORITY_CHANGE }
            - The group profile has change authority to the objects the user creates.
        <li>{@link com.ibm.as400.resource.RUser#GROUP_AUTHORITY_USE GROUP_AUTHORITY_USE }
            - The group profile has use authority to the objects the user creates.
        <li>{@link com.ibm.as400.resource.RUser#GROUP_AUTHORITY_EXCLUDE GROUP_AUTHORITY_EXCLUDE }
            - The group profile has exclude authority to the objects the user creates.
        </ul>

@see com.ibm.as400.resource.RUser#GROUP_AUTHORITY
**/
    public String getGroupAuthority()
    {
        return getAttributeValueAsString(RUser.GROUP_AUTHORITY);
    }



/**
Returns the type of authority the user's group has to
objects the user creates.

@return The type of authority the user's group has to
        objects the user creates.  Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#GROUP_AUTHORITY_TYPE_PRIVATE GROUP_AUTHORITY_TYPE_PRIVATE }
            - The group profile has a private authority to the objects the user creates,
            or the user does not have a group profile.
        <li>{@link com.ibm.as400.resource.RUser#GROUP_AUTHORITY_TYPE_PGP GROUP_AUTHORITY_TYPE_PGP }
            - The group profile will be the primary group for objects the user creates.
        </ul>

@see com.ibm.as400.resource.RUser#GROUP_AUTHORITY_TYPE
**/
    public String getGroupAuthorityType()
    {
        return getAttributeValueAsString(RUser.GROUP_AUTHORITY_TYPE);
    }



/**
Returns the group ID number for the user profile.
The group ID number is used to identify the user when it is a group and a
member of the group is using the integrated file system.

@return The group ID number for the user profile.  This will
        be 0 if the user does not have a group ID.

@see com.ibm.as400.resource.RUser#GROUP_ID_NUMBER
**/
    public int getGroupIDNumber()
    {
        // This cast is necessary because the original code
        // did not account for a group ID number (which is
        // an unsigned int being bigger than Integer.MAX_VALUE.
        return (int)getAttributeValueAsLong(RUser.GROUP_ID_NUMBER);
    }



/**
Returns the name of the group profile.

@return The name of the group profile.  Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#NONE NONE }
            - If the user does not have a group profile.
        <li>The group profile name.
        </ul>

@see com.ibm.as400.resource.RUser#GROUP_PROFILE_NAME
**/
    public String getGroupProfileName()
    {
        return getAttributeValueAsString(RUser.GROUP_PROFILE_NAME);
    }



/**
Returns the highest scheduling priority the user is allowed
to have for each job submitted to the system.

@return The highest scheduling priority the user is allowed
        to have for each job submitted to the system.
        The priority is a value from 0 to 9, with 0 being the
        highest priority.

@see com.ibm.as400.resource.RUser#HIGHEST_SCHEDULING_PRIORITY
**/
    public int getHighestSchedulingPriority()
           throws NumberFormatException
    {
        return getAttributeValueAsInt(RUser.HIGHEST_SCHEDULING_PRIORITY);
    }



/**
Returns the home directory for this user profile.

@return The home directory for this user profile.

@see com.ibm.as400.resource.RUser#HOME_DIRECTORY
**/
    public String getHomeDirectory()
    {
        return getAttributeValueAsString(RUser.HOME_DIRECTORY);
    }



/**
Returns the fully qualified integrated file system path name
of the initial menu for the user.

@return The fully qualified integrated file system path name
        of the initial menu for the user.   Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#INITIAL_MENU_SIGNOFF INITIAL_MENU_SIGNOFF }
        <li>The initial menu name.
        </ul>

@see com.ibm.as400.resource.RUser#INITIAL_MENU
@see QSYSObjectPathName
**/
    public String getInitialMenu()
    {
        return getAttributeValueAsString(RUser.INITIAL_MENU);
    }



/**
Returns the fully qualified integrated file system path name
of the initial program for the user.

@return The fully qualified integrated file system path name
        of the initial program for the user.

@see com.ibm.as400.resource.RUser#INITIAL_PROGRAM
@see QSYSObjectPathName
**/
    public String getInitialProgram()
    {
        return getAttributeValueAsString(RUser.INITIAL_PROGRAM);
    }



/**
Returns the fully qualified integrated file system path name
of the job description used for jobs that start through
subsystem work station entries.

@return The fully qualified integrated file system path name
        of the job description used for jobs that start through
        subsystem work station entries.

@see com.ibm.as400.resource.RUser#JOB_DESCRIPTION
@see QSYSObjectPathName
**/
    public String getJobDescription()
    {
        return getAttributeValueAsString(RUser.JOB_DESCRIPTION);
    }



/**
Returns the language ID used by the system for this user.

@return The language ID used by the system for this user.
        Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#SYSTEM_VALUE SYSTEM_VALUE }
            - The system value QLANGID will be used to determine the language ID.
        <li>The language ID.
        </ul>

@see com.ibm.as400.resource.RUser#LANGUAGE_ID
**/
    public String getLanguageID()
    {
        return getAttributeValueAsString(RUser.LANGUAGE_ID);
    }



/**
Indicates whether the user has limited capabilites.

@return Whether the user has limited capabilites.  Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#LIMIT_CAPABILITIES_PARTIAL LIMIT_CAPABILITIES_PARTIAL }
            - The user cannot change the initial program or current library.
        <li>{@link com.ibm.as400.resource.RUser#YES YES }
            - The user cannot change the initial menu, initial program,
            or current library.  The user cannot run commands from the
            command line.
        <li>{@link com.ibm.as400.resource.RUser#NO NO }
            - The user is not limited.
        </ul>

@see com.ibm.as400.resource.RUser#LIMIT_CAPABILITIES
**/
    public String getLimitCapabilities()
    {
        return getAttributeValueAsString(RUser.LIMIT_CAPABILITIES);
    }



/**
Indicates whether the user is limited to one device session.

@return Whether the user is limited to one device session.  Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#SYSTEM_VALUE SYSTEM_VALUE }
            - The system value QLMTDEVSSN determines if the user is limited to one
            device session.
        <li>{@link com.ibm.as400.resource.RUser#YES YES }
            - The user is limited to one session.
        <li>{@link com.ibm.as400.resource.RUser#NO NO }
            - The user is not limited to one device session.
        </ul>

@see com.ibm.as400.resource.RUser#LIMIT_DEVICE_SESSIONS
**/
    public String getLimitDeviceSessions()
    {
        return getAttributeValueAsString(RUser.LIMIT_DEVICE_SESSIONS);
    }



/**
Returns a list of attributes which are set from the locale path
name at the time a job is started for this user.

@return A list of attributes which are set from the locale path
        name at the time a job is started for this user.
        Possible values for the elements of this array are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#NONE NONE }
            - No job attributes are used from the locale path name at the time a job is
            started for this user profile.
        <li>{@link com.ibm.as400.resource.RUser#SYSTEM_VALUE SYSTEM_VALUE }
            - The job attributes assigned from the locale path name are determined by
            the system value QSETJOBATR at the time a job is started for this user profile.
        <li>{@link com.ibm.as400.resource.RUser#LOCALE_JOB_ATTRIBUTES_CCSID LOCALE_JOB_ATTRIBUTES_CCSID }
            - The coded character set identifier is set from the locale path name
            at the time a job is started for this user profile.
        <li>{@link com.ibm.as400.resource.RUser#LOCALE_JOB_ATTRIBUTES_DATE_FORMAT LOCALE_JOB_ATTRIBUTES_DATE_FORMAT }
            - The date format is set from the locale path name
            at the time a job is started for this user profile.
        <li>{@link com.ibm.as400.resource.RUser#LOCALE_JOB_ATTRIBUTES_DATE_SEPARATOR LOCALE_JOB_ATTRIBUTES_DATE_SEPARATOR }
            - The date separator is set from the locale path name
            at the time a job is started for this user profile.
        <li>{@link com.ibm.as400.resource.RUser#LOCALE_JOB_ATTRIBUTES_SORT_SEQUENCE LOCALE_JOB_ATTRIBUTES_SORT_SEQUENCE }
            - The sort sequence is set from the locale path name
            at the time a job is started for this user profile.
        <li>{@link com.ibm.as400.resource.RUser#LOCALE_JOB_ATTRIBUTES_TIME_SEPARATOR LOCALE_JOB_ATTRIBUTES_TIME_SEPARATOR }
            - The time separator is set from the locale path name
            at the time a job is started for this user profile.
        <li>{@link com.ibm.as400.resource.RUser#LOCALE_JOB_ATTRIBUTES_DECIMAL_FORMAT LOCALE_JOB_ATTRIBUTES_DECIMAL_FORMAT }
            - The decimal format is set from the locale path name
            at the time a job is started for this user profile.
        </ul>

@see com.ibm.as400.resource.RUser#LOCALE_JOB_ATTRIBUTES
**/
    public String[] getLocaleJobAttributes()
    {
        return (String[])getAttributeValueAsObject(RUser.LOCALE_JOB_ATTRIBUTES);
    }



/**
Returns the locale path name that is assigned to the
user profile when a job is started.

@return The locale path name that is assigned to the
        user profile when a job is started.
        Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#SYSTEM_VALUE SYSTEM_VALUE }
            - The QLOCALE system value is used to determine the locale path name.
        <li>{@link com.ibm.as400.resource.RUser#NONE NONE }
            - No locale path name is assigned.
        <li>{@link com.ibm.as400.resource.RUser#LOCALE_PATH_NAME_C LOCALE_PATH_NAME_C }
            - The C locale path name is assigned.
        <li>{@link com.ibm.as400.resource.RUser#LOCALE_PATH_NAME_POSIX LOCALE_PATH_NAME_POSIX }
            - The POSIX locale path name is assigned.
        </ul>

@see com.ibm.as400.resource.RUser#LOCALE_PATH_NAME
**/
    public String getLocalePathName()
    {
        return getAttributeValueAsString(RUser.LOCALE_PATH_NAME);
    }



/**
Returns the maximum amount of auxiliary storage (in kilobytes) that can be
assigned to store permanant objects owned by the user.

@return The maximum amount of auxiliary storage (in kilobytes) that can be
        assigned to store permanant objects owned by the user.
        If the user does not have a maximum amount of allowed storage, this will
        be -1.

@see com.ibm.as400.resource.RUser#MAXIMUM_ALLOWED_STORAGE
**/
    public int getMaximumStorageAllowed()
    {
        return getAttributeValueAsInt(RUser.MAXIMUM_ALLOWED_STORAGE);
    }



/**
Returns the fully qualified integrated file system path name of the
message queue that is used by this user.

@return The fully qualified integrated file system path name of the
        message queue that is used by this user.

@see com.ibm.as400.resource.RUser#MESSAGE_QUEUE
@see QSYSObjectPathName
**/
    public String getMessageQueue()
    {
        return getAttributeValueAsString(RUser.MESSAGE_QUEUE);
    }



/**
Returns how the messages are delivered to the message queue
used by the user.

@return How the messages are delivered to the message queue
        used by the user.  Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#MESSAGE_QUEUE_DELIVERY_METHOD_BREAK MESSAGE_QUEUE_DELIVERY_METHOD_BREAK }
            - The job to which the message queue is assigned is interrupted when a message
            arrives on the message queue.
        <li>{@link com.ibm.as400.resource.RUser#MESSAGE_QUEUE_DELIVERY_METHOD_DEFAULT MESSAGE_QUEUE_DELIVERY_METHOD_DEFAULT }
            - Messages requiring replies are answered with their default reply.
        <li>{@link com.ibm.as400.resource.RUser#MESSAGE_QUEUE_DELIVERY_METHOD_HOLD MESSAGE_QUEUE_DELIVERY_METHOD_HOLD }
            - The messages are held in the message queue until they are requested by the
            user or program.
        <li>{@link com.ibm.as400.resource.RUser#MESSAGE_QUEUE_DELIVERY_METHOD_NOTIFY MESSAGE_QUEUE_DELIVERY_METHOD_NOTIFY }
            - The job to which the message queue is assigned is notified when a message arrives
            on the message queue.
        </ul>

@see com.ibm.as400.resource.RUser#MESSAGE_QUEUE_DELIVERY_METHOD
**/
    public String getMessageQueueDeliveryMethod()
    {
        return getAttributeValueAsString(RUser.MESSAGE_QUEUE_DELIVERY_METHOD);
    }



/**
Returns the lowest severity that a message can have and still be delivered to
a user in break or notify mode.

@return The lowest severity that a message can have and still be delivered to
        a user in break or notify mode.

@see com.ibm.as400.resource.RUser#MESSAGE_QUEUE_SEVERITY
**/
    public int getMessageQueueSeverity()
    {
        return getAttributeValueAsInt(RUser.MESSAGE_QUEUE_SEVERITY);
    }



/**
Returns the user profile name.

@return The user profile name.
**/
    public String getName()
    {
        return rUser_.getName();
    }



/**
Returns the user's object auditing value.

@return The user's object auditing value.  Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#NONE NONE }
            - No additional object auditing is done for the user.
        <li>{@link com.ibm.as400.resource.RUser#OBJECT_AUDITING_VALUE_CHANGE OBJECT_AUDITING_VALUE_CHANGE }
            - Object changes are audited for the user if the object's auditing
            value is *USRPRF.
        <li>{@link com.ibm.as400.resource.RUser#OBJECT_AUDITING_VALUE_ALL OBJECT_AUDITING_VALUE_ALL }
            - Object read and change operations are audited for the user
            if the object's auditing value is *USRPRF.
        </ul>

@see com.ibm.as400.resource.RUser#OBJECT_AUDITING_VALUE
**/
    public String getObjectAuditingValue()
    {
        return getAttributeValueAsString(RUser.OBJECT_AUDITING_VALUE);
    }



/**
Returns the fully qualified integrated file system path name of the output queue
that is used by this user.

@return The fully qualified integrated file system path name of the output queue
        that is used by this user. Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#OUTPUT_QUEUE_WORK_STATION OUTPUT_QUEUE_WORK_STATION }
            - The output queue assigned to the user's work station is used.
        <li>{@link com.ibm.as400.resource.RUser#OUTPUT_QUEUE_DEVICE OUTPUT_QUEUE_DEVICE }
            - An output queue with the same name as the device specified
            in the printer device parameter is used.
        <li>The output queue name.
        </ul>

@see com.ibm.as400.resource.RUser#OUTPUT_QUEUE
@see QSYSObjectPathName
**/
    public String getOutputQueue()
    {
        return getAttributeValueAsString(RUser.OUTPUT_QUEUE);
    }



/**
Indicates who is to own objects created by this user.

@return Who is to own objects created by this user. Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#OWNER_USER_PROFILE OWNER_USER_PROFILE }
            - The user owns any objects the user creates.  If the user does not
            have a group profile, the field contains this value.
        <li>{@link com.ibm.as400.resource.RUser#OWNER_GROUP_PROFILE OWNER_GROUP_PROFILE }
            - The user's group profile owns any objects the user creates.
        </ul>

@see com.ibm.as400.resource.RUser#OWNER
**/
    public String getOwner()
    {
        return getAttributeValueAsString(RUser.OWNER);
    }



/**
Returns the date the user's password expires.

@return The date the user's password expires.

@see com.ibm.as400.resource.RUser#DATE_PASSWORD_EXPIRES
**/
    public Date getPasswordExpireDate()
    {
        return (Date)getAttributeValueAsObject(RUser.DATE_PASSWORD_EXPIRES);
    }



/**
Returns the number of days the user's password can remain
active before it must be changed.

@return The number of days the user's password can remain
        active before it must be changed.  Possible values are:
        <ul>
        <li>0 - The system value QPWDEXPITV is used to determine the user's
            password expiration interval.
        <li>-1 - The user's password does not expire.
        <li>The number of days the user's password can remain active before it must
            be changed.
        </ul>

@see com.ibm.as400.resource.RUser#PASSWORD_EXPIRATION_INTERVAL
**/
    public int getPasswordExpirationInterval()
    {
        return getAttributeValueAsInt(RUser.PASSWORD_EXPIRATION_INTERVAL);
    }



/**
Returns the date the user's password was last changed.

@return The date the user's password was last changed.

@see com.ibm.as400.resource.RUser#PASSWORD_CHANGE_DATE
**/
    public Date getPasswordLastChangedDate()
    {
        return (Date)getAttributeValueAsObject(RUser.PASSWORD_CHANGE_DATE);
    }



/**
Returns the date and time the user last signed on.

@return The date and time the user last signed on.

@see com.ibm.as400.resource.RUser#PREVIOUS_SIGN_ON
**/
    public Date getPreviousSignedOnDate()
    {
        return (Date)getAttributeValueAsObject(RUser.PREVIOUS_SIGN_ON);
    }



/**
Returns the printer used to print for this user.

@return The printer used to print for this user. Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#PRINT_DEVICE_WORK_STATION PRINT_DEVICE_WORK_STATION }
            - The printer assigned to the user's work station is used.
        <li>{@link com.ibm.as400.resource.RUser#SYSTEM_VALUE SYSTEM_VALUE }
            - The default system printer specified in the system value QPRTDEV
            is used.
        <li>The print device.
        </ul>

@see com.ibm.as400.resource.RUser#PRINT_DEVICE
**/
    public String getPrintDevice()
    {
        return getAttributeValueAsString(RUser.PRINT_DEVICE);
    }



/**
Returns the number of sign-on attempts that were not valid
since the last successful sign-on.

@return The number of sign-on attempts that were not valid
        since the last successful sign-on.

@see com.ibm.as400.resource.RUser#SIGN_ON_ATTEMPTS_NOT_VALID
**/
    public int getSignedOnAttemptsNotValid()
    {
        return getAttributeValueAsInt(RUser.SIGN_ON_ATTEMPTS_NOT_VALID);
    }



/**
Returns the fully integrated file system path name of the sort sequence
table used for string comparisons.

@return The fully integrated file system path name of the sort sequence
        table used for string comparisons.   Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#SORT_SEQUENCE_TABLE_HEX SORT_SEQUENCE_TABLE_HEX }
            - The hexadecimal values of the characters are used to determine the
            sort sequence.
        <li>{@link com.ibm.as400.resource.RUser#SORT_SEQUENCE_TABLE_UNIQUE SORT_SEQUENCE_TABLE_UNIQUE }
            - A unique-weight sort table associated with the language specified.
        <li>{@link com.ibm.as400.resource.RUser#SORT_SEQUENCE_TABLE_SHARED SORT_SEQUENCE_TABLE_SHARED }
            - A shared-weight sort table associated with the language specified.
        <li>{@link com.ibm.as400.resource.RUser#SYSTEM_VALUE SYSTEM_VALUE }
            - The system value QSRTSEQ.
        <li>The sort sequence table name.
        </ul>

@see com.ibm.as400.resource.RUser#SORT_SEQUENCE_TABLE
@see QSYSObjectPathName
**/
    public String getSortSequenceTable()
    {
        return getAttributeValueAsString(RUser.SORT_SEQUENCE_TABLE);
    }



/**
Returns a list of special authorities that the user has.

@return A list of special authorities that the user has.
        Possible values for the elements of this array are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#SPECIAL_AUTHORITIES_ALL_OBJECT SPECIAL_AUTHORITIES_ALL_OBJECT }
            - All object.
        <li>{@link com.ibm.as400.resource.RUser#SPECIAL_AUTHORITIES_SECURITY_ADMINISTRATOR SPECIAL_AUTHORITIES_SECURITY_ADMINISTRATOR }
            - Security administrator.
        <li>{@link com.ibm.as400.resource.RUser#SPECIAL_AUTHORITIES_JOB_CONTROL SPECIAL_AUTHORITIES_JOB_CONTROL }
            - Job control.
        <li>{@link com.ibm.as400.resource.RUser#SPECIAL_AUTHORITIES_SPOOL_CONTROL SPECIAL_AUTHORITIES_SPOOL_CONTROL }
            - Spool control.
        <li>{@link com.ibm.as400.resource.RUser#SPECIAL_AUTHORITIES_SAVE_SYSTEM SPECIAL_AUTHORITIES_SAVE_SYSTEM }
            - Save system.
        <li>{@link com.ibm.as400.resource.RUser#SPECIAL_AUTHORITIES_SERVICE SPECIAL_AUTHORITIES_SERVICE }
            - Service.
        <li>{@link com.ibm.as400.resource.RUser#SPECIAL_AUTHORITIES_AUDIT SPECIAL_AUTHORITIES_AUDIT }
            - Audit.
        <li>{@link com.ibm.as400.resource.RUser#SPECIAL_AUTHORITIES_IO_SYSTEM_CONFIGURATION SPECIAL_AUTHORITIES_IO_SYSTEM_CONFIGURATION }
            - Input/output system configuration.
        </ul>

@see com.ibm.as400.resource.RUser#SPECIAL_AUTHORITIES
**/
    public String[] getSpecialAuthority()
    {
        return (String[])getAttributeValueAsObject(RUser.SPECIAL_AUTHORITIES);
    }



/**
Returns the special environment the user operates in after signing on.

@return The special environment the user operates in after signing on.
        Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#SYSTEM_VALUE SYSTEM_VALUE }
            - The system value QSPCENV is used to determine the user's special
            environment.
        <li>{@link com.ibm.as400.resource.RUser#NONE NONE }
            - The user operates in the OS/400 environment.
        <li>{@link com.ibm.as400.resource.RUser#SPECIAL_ENVIRONMENT_SYSTEM_36 SPECIAL_ENVIRONMENT_SYSTEM_36 }
            - The user operates in the System/36 environment.
        </ul>

@see com.ibm.as400.resource.RUser#SPECIAL_ENVIRONMENT
**/
    public String getSpecialEnvironment()
    {
        return getAttributeValueAsString(RUser.SPECIAL_ENVIRONMENT);
    }



/**
Returns the status of the user profile.

@return The status of the user profile.
        Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#STATUS_ENABLED STATUS_ENABLED }
            - The user profile is enabled.
        <li>{@link com.ibm.as400.resource.RUser#STATUS_NOT_ENABLED STATUS_NOT_ENABLED }
            - The user profile is not enabled.
        </ul>

@see com.ibm.as400.resource.RUser#STATUS
**/
    public String getStatus()
    {
        return getAttributeValueAsString(RUser.STATUS);
    }



/**
Returns the amount of auxiliary storage (in kilobytes) occupied
by this user's owned objects.

@return The amount of auxiliary storage (in kilobytes) occupied
        by this user's owned objects.

@see com.ibm.as400.resource.RUser#STORAGE_USED
**/
    public int getStorageUsed()
    {
        return getAttributeValueAsInt(RUser.STORAGE_USED);
    }



/**
Returns the supplemental groups for the user profile.

@return The supplemental groups for the user profile.

@see com.ibm.as400.resource.RUser#SUPPLEMENTAL_GROUPS
**/
    public String[] getSupplementalGroups()
    {
        return (String[])getAttributeValueAsObject(RUser.SUPPLEMENTAL_GROUPS);
    }



/**
Returns the number of supplemental groups for the user profile.

@return The number of supplemental groups for the user profile.

@see com.ibm.as400.resource.RUser#SUPPLEMENTAL_GROUPS
**/
    public int getSupplementalGroupsNumber()
    {
       return getSupplementalGroups().length;
    }



/**
Returns the system.

@return The system.
**/
    public AS400 getSystem()
    {
        return rUser_.getSystem();
    }


/**
Returns a list of action audit levels for the user.

@return A list of action audit levels for the user.
        Possible values for the elements of this array are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#USER_ACTION_AUDIT_LEVEL_COMMAND USER_ACTION_AUDIT_LEVEL_COMMAND }
            - The user has the *CMD audit value specified in the user profile.
        <li>{@link com.ibm.as400.resource.RUser#USER_ACTION_AUDIT_LEVEL_CREATE USER_ACTION_AUDIT_LEVEL_CREATE }
            - The user has the *CREATE audit value specified in the user profile.
        <li>{@link com.ibm.as400.resource.RUser#USER_ACTION_AUDIT_LEVEL_DELETE USER_ACTION_AUDIT_LEVEL_DELETE }
            - The user has the *DELETE audit value specified in the user profile.
        <li>{@link com.ibm.as400.resource.RUser#USER_ACTION_AUDIT_LEVEL_JOB_DATA USER_ACTION_AUDIT_LEVEL_JOB_DATA }
            - The user has the *JOBDTA audit value specified in the user profile.
        <li>{@link com.ibm.as400.resource.RUser#USER_ACTION_AUDIT_LEVEL_OBJECT_MANAGEMENT USER_ACTION_AUDIT_LEVEL_OBJECT_MANAGEMENT }
            - The user has the *OBJMGT audit value specified in the user profile.
        <li>{@link com.ibm.as400.resource.RUser#USER_ACTION_AUDIT_LEVEL_OFFICE_SERVICES USER_ACTION_AUDIT_LEVEL_OFFICE_SERVICES }
            - The user has the *OFCSRV audit value specified in the user profile.
        <li>{@link com.ibm.as400.resource.RUser#USER_ACTION_AUDIT_LEVEL_OPTICAL USER_ACTION_AUDIT_LEVEL_OPTICAL }
            - The user has the *OPTICAL audit value specified in the user profile.
        <li>{@link com.ibm.as400.resource.RUser#USER_ACTION_AUDIT_LEVEL_PROGRAM_ADOPTION USER_ACTION_AUDIT_LEVEL_PROGRAM_ADOPTION }
            - The user has the *PGMADP audit value specified in the user profile.
        <li>{@link com.ibm.as400.resource.RUser#USER_ACTION_AUDIT_LEVEL_SAVE_RESTORE USER_ACTION_AUDIT_LEVEL_SAVE_RESTORE }
            - The user has the *SAVRST audit value specified in the user profile.
        <li>{@link com.ibm.as400.resource.RUser#USER_ACTION_AUDIT_LEVEL_SECURITY USER_ACTION_AUDIT_LEVEL_SECURITY }
            - The user has the *SECURITY audit value specified in the user profile.
        <li>{@link com.ibm.as400.resource.RUser#USER_ACTION_AUDIT_LEVEL_SERVICE USER_ACTION_AUDIT_LEVEL_SERVICE }
            - The user has the *SERVICE audit value specified in the user profile.
        <li>{@link com.ibm.as400.resource.RUser#USER_ACTION_AUDIT_LEVEL_SPOOLED_FILE_DATA USER_ACTION_AUDIT_LEVEL_SPOOLED_FILE_DATA }
            - The user has the *SPLFDTA audit value specified in the user profile.
        <li>{@link com.ibm.as400.resource.RUser#USER_ACTION_AUDIT_LEVEL_SYSTEM_MANAGEMENT USER_ACTION_AUDIT_LEVEL_SYSTEM_MANAGEMENT }
            - The user has the *SYSMGT audit value specified in the user profile.
        </ul>

@see com.ibm.as400.resource.RUser#USER_ACTION_AUDIT_LEVEL
**/
    public String[] getUserActionAuditLevel()
    {
        return (String[])getAttributeValueAsObject(RUser.USER_ACTION_AUDIT_LEVEL);
    }



/**
Returns the user class name.

@return The user class name.   Possible values are:
        <ul>
        <li>{@link com.ibm.as400.resource.RUser#USER_CLASS_SECURITY_OFFICER USER_CLASS_SECURITY_OFFICER }
            - The user has a class of security officer.
        <li>{@link com.ibm.as400.resource.RUser#USER_CLASS_SECURITY_ADMINISTRATOR USER_CLASS_SECURITY_ADMINISTRATOR }
            - The user has a class of security administrator.
        <li>{@link com.ibm.as400.resource.RUser#USER_CLASS_PROGRAMMER USER_CLASS_PROGRAMMER }
            - The user has a class of programmer.
        <li>{@link com.ibm.as400.resource.RUser#USER_CLASS_SYSTEM_OPERATOR USER_CLASS_SYSTEM_OPERATOR }
            - The user has a class of system operator.
        <li>{@link com.ibm.as400.resource.RUser#USER_CLASS_USER USER_CLASS_USER }
            - The user has a class of end user.
        </ul>

@see com.ibm.as400.resource.RUser#USER_CLASS
**/
    public String getUserClassName()
    {
        return getAttributeValueAsString(RUser.USER_CLASS);
    }



/**
Returns the user ID number for the user profile. This is used
to identify the user when using the integrated file system.

@return The user ID number for the user profile.

@see com.ibm.as400.resource.RUser#USER_ID_NUMBER
**/
    public int getUserIDNumber()
    {
        return (int)getAttributeValueAsLong(RUser.USER_ID_NUMBER);
    }



/**
Returns the name of the user profile.

@return The name of the user profile.

@see com.ibm.as400.resource.RUser#USER_PROFILE_NAME
**/
    public String getUserProfileName()
    {
        return getAttributeValueAsString(RUser.USER_PROFILE_NAME);
    }



/**
Indicates whether this user is a group that has members.

@return true if the user is a group that has members, false otherwise.

@see com.ibm.as400.resource.RUser#GROUP_MEMBER_INDICATOR
**/
    public boolean isGroupHasMember()
    {
        return getAttributeValueAsBoolean(RUser.GROUP_MEMBER_INDICATOR);
    }



/**
Indicates whether there is no password.

@return true if there is no password, false otherwise.

@see com.ibm.as400.resource.RUser#NO_PASSWORD_INDICATOR
**/
    public boolean isNoPassword()
    {
        return getAttributeValueAsBoolean(RUser.NO_PASSWORD_INDICATOR);
    }



/**
Indicates whether the user's password is set to expire,
requiring the user to change the password when signing on.

@return true if the password set to expire, false otherwise.

@see com.ibm.as400.resource.RUser#SET_PASSWORD_TO_EXPIRE
**/
    public boolean isPasswordSetExpire()
    {
        return getAttributeValueAsBoolean(RUser.SET_PASSWORD_TO_EXPIRE);
    }



/**
Indicates whether there are digital certificates associated with this user.

@return true if there are digital certificates associated with this user,
        false otherwise.

@see com.ibm.as400.resource.RUser#DIGITAL_CERTIFICATE_INDICATOR
**/
    public boolean isWithDigitalCertificates()
    {
        return getAttributeValueAsBoolean(RUser.SET_PASSWORD_TO_EXPIRE);
    }



/**
Refreshes the values for all attributes.  This does not cancel
uncommitted changes.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.
**/
    public void loadUserInformation()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   ObjectDoesNotExistException,
                   IOException,
                   UnsupportedEncodingException
    {
        try {
            rUser_.refreshAttributeValues();
        }
        catch(ResourceException e) {
            e.unwrap();
        }
    }



/**
Removes a PropertyChangeListener.

@param listener The listener.
**/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        rUser_.removePropertyChangeListener(listener);
    }



/**
Removes a VetoableChangeListener.

@param listener The listener.
**/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        rUser_.removeVetoableChangeListener(listener);
    }



/**
Sets the user profile name.  This does not change the user profile on
the AS/400.  Instead, it changes the user profile to which
this User object references.  This cannot be changed
if the object has established a connection to the AS/400.

@param name    The user profile name.

@exception PropertyVetoException    If the property change is vetoed.
**/
    public void setName(String name)
        throws PropertyVetoException
    {
        rUser_.setName(name);
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
        rUser_.setSystem(system);
    }



/**
Returns the user profile name.

@return The user profile name.
**/
    public String toString()
    {
        return rUser_.getName();
    }


}
