///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  User.java
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
import java.util.Date;
import java.util.Calendar;

/**
 The User class represents a user profile object on the server.
 <p>Note that calling any of the attribute getters for the first time will result in an implicit call to {@link #loadUserInformation loadUserInformation()}.  If any exceptions are thrown by loadUserInformation() during the implicit call, they will be logged to {@link com.ibm.as400.access.Trace#ERROR Trace.ERROR} and ignored.  However, should an exception occur during an explicit call to loadUserInformation(), it will be thrown to the caller.
 <p>Implementation note:  This class internally calls the Retrieve User Information (QSYRUSRI) API for the methods that retrieve user profile information.  The caller must have *READ authority to the user profile object in order to retrieve the information.  The class internally calls the Change User Profile (CHGUSRPRF) command for the methods that change user profile information.  The caller must have security administrator (*SECADM) special authority, and object management (*OBJMGT) and use (*USE) authorities to the user profile being changed.
 @see  com.ibm.as400.access.DirectoryEntry
 @see  com.ibm.as400.access.UserList
 @see  com.ibm.as400.access.UserGroup
 **/
public class User implements Serializable
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    static final long serialVersionUID = 5L;

    // These need to be in this order so we can easily reference them from the code that parses the API return information in loadUserInformation().
    private static final String[] SPECIAL_AUTHORITIES = new String[]
    {
        "*ALLOBJ",
        "*SECADM",
        "*JOBCTL",
        "*SPLCTL",
        "*SAVSYS",
        "*SERVICE",
        "*AUDIT",
        "*IOSYSCFG"
    };

    /**
     Constant value representing the String "*NONE".
     @see  #getGroupProfileName
     @see  #getAttentionKeyHandlingProgram
     @see  #getGroupAuthority
     @see  #getInitialProgram
     @see  #getLocaleJobAttributes
     @see  #getLocalePathName
     @see  #getObjectAuditingValue
     @see  #getSpecialEnvironment
     **/
    public static final String NONE = "*NONE";

    /**
     Constant value representing a special authority of "*ALLOBJ".
     @see  #getSpecialAuthority
     **/
    public static final String SPECIAL_AUTHORITY_ALL_OBJECT = SPECIAL_AUTHORITIES[0];

    /**
     Constant value representing a special authority of "*AUDIT".
     @see  #getSpecialAuthority
     **/
    public static final String SPECIAL_AUTHORITY_AUDIT = SPECIAL_AUTHORITIES[6];

    /**
     Constant value representing a special authority of "*IOSYSCFG".
     @see  #getSpecialAuthority
     **/
    public static final String SPECIAL_AUTHORITY_IO_SYSTEM_CONFIGURATION = SPECIAL_AUTHORITIES[7];

    /**
     Constant value representing a special authority of "*JOBCTL".
     @see  #getSpecialAuthority
     **/
    public static final String SPECIAL_AUTHORITY_JOB_CONTROL = SPECIAL_AUTHORITIES[2];

    /**
     Constant value representing a special authority of "*SAVSYS".
     @see  #getSpecialAuthority
     **/
    public static final String SPECIAL_AUTHORITY_SAVE_SYSTEM = SPECIAL_AUTHORITIES[4];

    /**
     Constant value representing a special authority of "*SECADM".
     @see  #getSpecialAuthority
     **/
    public static final String SPECIAL_AUTHORITY_SECURITY_ADMINISTRATOR = SPECIAL_AUTHORITIES[1];

    /**
     Constant value representing a special authority of "*SERVICE".
     @see  #getSpecialAuthority
     **/
    public static final String SPECIAL_AUTHORITY_SERVICE = SPECIAL_AUTHORITIES[5];

    /**
     Constant value representing a special authority of "*SPLCTL".
     @see  #getSpecialAuthority
     **/
    public static final String SPECIAL_AUTHORITY_SPOOL_CONTROL = SPECIAL_AUTHORITIES[3];

    // Shared error code parameter.
    private static final ProgramParameter ERROR_CODE = new ProgramParameter(new byte[8]);

    // The server where the user is located.
    private AS400 system_ = null;
    // User profile name.
    private String name_ = null;
    // Text description.
    private String description_ = null;
    // Group member indicator.
    private boolean groupHasMember_ = false;

    // Flag that indicates that the above properties were set by UserList.
    private boolean partiallyLoaded_ = false;
    // Flag that indicates that all properties were set by loadUserInformation().
    private boolean loaded_ = false;
    // Flag that indicates that a connection has been made and the properties are frozen.
    private boolean connected_ = false;

    // List of property change event bean listeners, set on first add.
    private transient PropertyChangeSupport propertyChangeListeners_ = null;
    // List of vetoable change event bean listeners, set on first add.
    private transient VetoableChangeSupport vetoableChangeListeners_ = null;

    // User profile name.
    private String userProfileName_;
    // Previous sign-on date and time.
    private Date previousSignedOnDate_;
    // Sign-on attempts not valid.
    private int signedOnAttemptsNotValid_;
    // Status.
    private String status_;
    // Password change date (raw from API).
    private byte[] passwordLastChangedDateBytes_;
    // Password change date (converted).
    private Date passwordLastChangedDate_;
    // No password indicator.
    private boolean noPassword_;
    // Password expiration interval.
    private int passwordExpirationInterval_;
    // Date password expires (raw from API).
    private byte[] passwordExpireDateBytes_;
    // Date password expires (converted).
    private Date passwordExpireDate_;
    // Days until password expires.
    private int daysUntilPasswordExpire_;
    // Set password to expire.
    private boolean passwordSetExpire_;
    // User class name.
    private String userClassName_;
    // Special authorities.
    private String[] specialAuthority_;
    // Group profile name.
    private String groupProfileName_;
    // Owner.
    private String owner_;
    // Group authority.
    private String groupAuthority_;
    // Assistance level.
    private String assistanceLevel_;
    // Current library name.
    private String currentLibraryName_;
    // Initial menu (full IFS path).
    private String initialMenu_;
    // Initial program name (full IFS path).
    private String initialProgram_;
    // Limit capabilities.
    private String limitCapabilities_;
    // Display sign-on information.
    private String displaySignOnInformation_;
    // Limit device sessions.
    private String limitDeviceSessions_;
    // Keyboard buffering.
    private String keyboardBuffering_;
    // Maximum allowed storage.
    private int maximumStorageAllowed_;
    // Storage used.
    private int storageUsed_;
    // Highest scheduling priority.
    private int highestSchedulingPriority_;
    // Job description (full IFS path).
    private String jobDescription_;
    // Accounting code.
    private String accountingCode_;
    // Message queue (full IFS path).
    private String messageQueue_;
    // Message queue delivery method.
    private String messageQueueDeliveryMethod_;
    // Message queue severity.
    private int messageQueueSeverity_;
    // Output queue (full IFS path).
    private String outputQueue_;
    // Print device.
    private String printDevice_;
    // Special environment.
    private String specialEnvironment_;
    // Attention-key-handling program (full IFS path).
    private String attentionKeyHandlingProgram_;
    // Language ID.
    private String languageID_;
    // Country or region ID.
    private String countryID_;
    // Character code set ID.
    private int ccsid_;
    // User options.
    private String[] userOptions_;
    // Sort sequence table (full IFS path).
    private String sortSequenceTable_;
    // Object auditing value.
    private String objectAuditingValue_;
    // User action audit level.
    private String[] userActionAuditLevel_;
    // Group authority type.
    private String groupAuthorityType_;
    // Supplemental groups.
    private String[] supplementalGroups_;
    // User ID number.
    private long userID_;
    // Group ID number.
    private long groupID_;
    // Home directory.
    private String homeDirectory_;
    // Locale job attributes.
    private String[] localeJobAttributes_;
    // Local path name.
    private String localePathName_;
    // Digital certificate indicator.
    private boolean withDigitalCertificates_;
    // Character identifier control.
    private String chridControl_;
    // Independent ASP name.
    private String[] iaspNames_;
    // Independent ASP maximum allowed storage.
    private int[] iaspStorageAllowed_;
    // Independent ASP Storage used.
    private int[] iaspStorageUsed_;
    // Local password management.
    private boolean localPasswordManagement_;

    /**
     Constructs a User object.
     **/
    public User()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing User object.");
    }

    /**
     Constructs a User object.  Note that this constructor no longer throws any of the declared exceptions, but they remain for compatibility.
     @param  system  The system object representing the server on which the user profile exists.
     @param  name  The user profile name.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public User(AS400 system, String name) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing User object, system: " + system + ", name: " + name);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (name == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'name' is null.");
            throw new NullPointerException("name");
        }
        if (name.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'name' is not valid: '" + name + "'");
            throw new ExtendedIllegalArgumentException("name (" + name + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        system_ = system;
        name_ = name.toUpperCase().trim();
    }

    // Called by UserList.getUsers() and a UserGroup constructor.
    User(AS400 system, String name, boolean groupHasMember, String description)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing User object, system: " + system + ", name: " + name);
        system_ = system;
        name_ = name;
        groupHasMember_ = groupHasMember;
        description_ = description;
        partiallyLoaded_ = true;
    }

    /**
     Adds a PropertyChangeListener.  The specified PropertyChangeListener's {@link java.beans.PropertyChangeListener#propertyChange propertyChange()} method will be called each time the value of any bound property is changed.
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
     Adds a VetoableChangeListener.  The specified VetoableChangeListener's {@link java.beans.VetoableChangeListener#vetoableChange vetoableChange()} method will be called each time the value of any constrained property is changed.
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
     Determines if this user profile exists on the system.  This method just calls {@link #loadUserInformation loadUserInformation()} and if no exception is thrown, the user profile exists, if a CPF9801 then the user profile does not exist.  Any other exceptions (e.g. not enough authority) are still thrown.
     <p>The value returned by this method is not cached.  That is, every time exists() is called, a call to the server is made to determine if the user profile still exists.
     @return  true if the profile exists, false if it does not.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
    **/
    public boolean exists() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Determining user existence.");
        try
        {
            loadUserInformation();
        }
        catch (AS400Exception e)
        {
            AS400Message[] messages = e.getAS400MessageList();
            if (messages.length == 1 && messages[0].getID().equalsIgnoreCase("CPF9801"))
            {
                return false;
            }
            throw e;
        }
        return true;
    }

    /**
     Retrieves the accounting code that is associated with this user.
     @return  The accounting code that is associated with this user.  If the user does not have an accounting code, an empty string ("") is returned.
     **/
    public String getAccountingCode()
    {
        if (!loaded_) refresh();
        return accountingCode_;
    }

    /**
     Retrieves the user interface that the user will use.
     @return  The user interface that the user will use.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The system value QASTLVL determines which user interface the user is using.
     <li>"*BASIC" - The Operational Assistant user interface.
     <li>"*INTERMED" - The system user interface.
     <li>"*ADVANCED" - The expert system user interface.
     </ul>
     **/
    public String getAssistanceLevel()
    {
        if (!loaded_) refresh();
        return assistanceLevel_;
    }

    /**
     Retrieves the attention key handling program for this user.
     @return  The attention key handling program for this user.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The system value QATNPGM determines the user's attention key handling program.
     <li>{@link #NONE User.NONE} - No attention key handling program is used.
     <li>The fully qualified integrated file system path name of the attention key handling program.
     </ul>
     @see  QSYSObjectPathName
     **/
    public String getAttentionKeyHandlingProgram()
    {
        if (!loaded_) refresh();
        return attentionKeyHandlingProgram_;
    }

    /**
     Retrieves the character code set ID to be used by the system for this user.
     @return The character code set ID to be used by the system for this user.  Possible values are:
     <ul>
     <li>-2 - The system value QCCSID is used to determine the user's character code set ID.
     <li>A character code set ID.
     </ul>
     **/
    public int getCCSID()
    {
        if (!loaded_) refresh();
        return ccsid_;
    }

    /**
     Retrieves the character identifier control for the user.
     @return  The character identifier control for this user.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The system value QCHRIDCTL will be used to determine the CHRID control for this user.
     <li>"*DEVD" - The *DEVD special value performs the same function as on the CHRID command parameter for display files, printer files, and panel groups.
     <li>"*JOBCCSID" - The *JOBCCSID special value performs the same function as on the CHRID command parameter for display files, printer files, and panel groups.
     </ul>
     **/
    public String getCHRIDControl()
    {
        if (!loaded_) refresh();
        return chridControl_;
    }

    /**
     Retrieves the country or region ID used by the system for this user.
     @return  The country or region ID used by the system for this user.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The system value QCNTRYID is used to determine the user's country or region ID.
     <li>A country or region ID.
     </ul>
     **/
    public String getCountryID()
    {
        if (!loaded_) refresh();
        return countryID_;
    }

    /**
     Retrieves the name of the user's current library.
     @return  The name of the user's current library.  Possible values are:
     <ul>
     <li>"*CRTDFT" - The user does not have a current library.
     <li>A library name.
     </ul>
     **/
    public String getCurrentLibraryName()
    {
        if (!loaded_) refresh();
        return currentLibraryName_;
    }

    /**
     Retrieves the number of days until the password will expire.
     @return  The number of days until the password will expire.  Possible values are:
     <ul>
     <li>0 - The password is expired.
     <li>1-7 - The number of days until the password expires.
     <li>-1 - The password will not expire in the next 7 days.
     </ul>
     **/
    public int getDaysUntilPasswordExpire()
    {
        if (!loaded_) refresh();
        return daysUntilPasswordExpire_;
    }

    /**
     Retrieves the descriptive text for the user profile.  This value is pre-loaded into any User objects generated from a UserList object so that a call to the server is not required to retrieve this value.  In the event that this User object was not constructed by a UserList, the description will need to be retrieved from the system via an implicit call to loadUserInformation().
     @return  The descriptive text for the user profile.
     **/
    public String getDescription()
    {
        if (!loaded_ && !partiallyLoaded_) refresh();
        return description_;
    }

    /**
     Retrieves the system distribution directory entry for the user profile, if one exists.  The directory entry is retrieved from the system every time this method is called, so its value is unaffected by any call to loadUserInformation().
     @return  The directory entry, or null if none exists.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public DirectoryEntry getDirectoryEntry() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        DirectoryEntryList list = new DirectoryEntryList(system_);
        list.addSelection(DirectoryEntryList.USER_PROFILE, name_.toUpperCase());
        DirectoryEntry[] entries = list.getEntries();
        if (entries.length == 0) return null;
        return entries[0];
    }

    /**
     Retrieves whether the sign-on information display is shown when the user signs on.
     @return  Whether the sign-on information display is shown when the user signs on.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The system value QDSPSGNINF determines if the sign-on information display is shown when the user signs on.
     <li>"*YES" - The sign-on information display is shown when the user signs on.
     <li>"*NO" - The sign-on information display is not shown when the user signs on.
     </ul>
     **/
    public String getDisplaySignOnInformation()
    {
        if (!loaded_) refresh();
        return displaySignOnInformation_;
    }

    /**
     Retrieves the authority the user's group profile has to objects the user creates.
     @return  The authority the user's group profile has to objects the user creates.  Possible values are:
     <ul>
     <li>{@link #NONE User.NONE} - The group profile has no authority to the objects the user creates, or the user does not have a group profile.
     <li>"*ALL" - The group profile has all authority to the objects the user creates.
     <li>"*CHANGE" - The group profile has change authority to the objects the user creates.
     <li>"*USE" - The group profile has use authority to the objects the user creates.
     <li>"*EXCLUDE" - The group profile has exclude authority to the objects the user creates.
     </ul>
     **/
    public String getGroupAuthority()
    {
        if (!loaded_) refresh();
        return groupAuthority_;
    }

    /**
     Retrieves the type of authority the user's group has to objects the user creates.
     @return  The type of authority the user's group has to objects the user creates.  Possible values are:
     <ul>
     <li>"*PRIVATE" - The group profile has a private authority to the objects the user creates, or the user does not have a group profile.
     <li>"*PGP" - The group profile will be the primary group for objects the user creates.
     </ul>
     **/
    public String getGroupAuthorityType()
    {
        if (!loaded_) refresh();
        return groupAuthorityType_;
    }

    /**
     Retrieves the group ID number for the user profile.  The group ID number is used to identify the user when it is a group and a member of the group is using the integrated file system.
     @return  The group ID number for the user profile.  Possible values are:
     <ul>
     <li>0 - user does not have a group ID (*NONE).
     <li>A GID.
     </ul>
     **/
    public long getGroupID()
    {
        if (!loaded_) refresh();
        return groupID_;
    }

    /**
     Retrieves the group ID number for the user profile.  The group ID number is used to identify the user when it is a group and a member of the group is using the integrated file system.
     @return  The group ID number for the user profile.  Possible values are:
     <ul>
     <li>0 - user does not have a group ID (*NONE).
     <li>A GID.
     </ul>
     @deprecated  This method has been replaced by {@link #getGroupID getGroupID()} which returns a long.
     **/
    public int getGroupIDNumber()
    {
        // The original code did not account for a group ID number being an unsigned 4 byte binary bigger than Integer.MAX_VALUE.
        return (int)getGroupID();
    }

    /**
     Retrieves the name of the group profile.
     @return  The name of the group profile.  Possible values are:
     <ul>
     <li>{@link #NONE User.NONE} - The user does not have a group profile.
     <li>The group profile name.
     </ul>
     **/
    public String getGroupProfileName()
    {
        if (!loaded_) refresh();
        return groupProfileName_;
    }

    /**
     Retrieves the highest scheduling priority the user is allowed to have for each job submitted to the system.
     @return  The highest scheduling priority the user is allowed to have for each job submitted to the system.  The priority is a value from 0 to 9, with 0 being the highest priority.
     **/
    public int getHighestSchedulingPriority()
    {
        if (!loaded_) refresh();
        return highestSchedulingPriority_;
    }

    /**
     Retrieves the home directory for this user profile.  The home directory is the user's initial working directory.  The working directory, associated with a process, is used in path name resolution in the directory file system for path names that do not begin with a slash (/).
     @return  The home directory for this user profile.
     **/
    public String getHomeDirectory()
    {
        if (!loaded_) refresh();
        return homeDirectory_;
    }

    /**
     Retrieves the list of independent auxiliary storage pool (IASP) names in use by this user.
     @return  The list of independent auxiliary storage pool (IASP) names in use by this user.  If no IASP name are in use by this user, a zero length array is returned.  If the server operating system is not release V5R1M0 or higher, null is returned.
     @see  #getIASPStorageAllowed
     @see  #getIASPStorageUsed
     @see  com.ibm.as400.access.AS400#getVRM
     @see  com.ibm.as400.access.AS400#generateVRM
     **/
    public String[] getIASPNames()
    {
        if (!loaded_) refresh();
        return iaspNames_;
    }

    /**
     Retrieves the maximum amount of auxiliary storage in kilobytes that can be assigned to store permanent object owned by this user on the given independant ASP.
     @return  The maximum amount of auxiliary storage in kilobytes that can be assigned to store permanent object owned by this user on the given independant ASP.  If the user does not have a maximum amount of allowed storage on the given independent ASP, -1 for *NOMAX is returned.  If the server operating system is not release V5R1M0 or higher, or if the given IASP name is not listed for this user, -2 is returned.
     @see  #getIASPNames
     @see  #getIASPStorageUsed
     **/
    public int getIASPStorageAllowed(String iaspName)
    {
        if (iaspName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'iaspName' is null.");
            throw new NullPointerException("iaspName");
        }
        if (!loaded_) refresh();
        if (iaspStorageAllowed_ == null) return -2;
        for (int i = 0; i < iaspNames_.length; ++i)
        {
            if (iaspNames_[i].equals(iaspName))
            {
                return iaspStorageAllowed_[i];
            }
        }
        return -2;
    }

    /**
     Retrieves the amount of auxiliary storage in kilobytes occupied by this user's owned objects on the given independent ASP.
     @return  The amount of auxiliary storage in kilobytes occupied by this user's owned objects on the given independent ASP.  If the server operating system is not release V5R1M0 or higher, or if the given IASP name is not listed for this user, -2 is returned.
     @see  #getIASPNames
     @see  #getIASPStorageAllowed
     **/
    public int getIASPStorageUsed(String iaspName)
    {
        if (iaspName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'iaspName' is null.");
            throw new NullPointerException("iaspName");
        }
        if (!loaded_) refresh();
        if (iaspStorageUsed_ == null) return -2;
        for (int i = 0; i < iaspNames_.length; ++i)
        {
            if (iaspNames_[i].equals(iaspName))
            {
                return iaspStorageUsed_[i];
            }
        }
        return -2;
    }

    /**
     Retrieves the initial menu for the user.
     @return  The initial menu for the user.  Possible values are:
     <ul>
     <li>"*SIGNOFF" - The user is limited to running the initial program specified in this profile.
     <li>The fully qualified integrated file system path name of the initial menu name.
     </ul>
     @see  QSYSObjectPathName
     **/
    public String getInitialMenu()
    {
        if (!loaded_) refresh();
        return initialMenu_;
    }

    /**
     Retrieves the initial program for the user.
     @return  The initial program for the user.  Possible values are:
     <ul>
     <li>{@link #NONE User.NONE} - The user does not have an initial program.
     <li>The fully qualified integrated file system path name of the initial program name.
     </ul>
     @see  QSYSObjectPathName
     **/
    public String getInitialProgram()
    {
        if (!loaded_) refresh();
        return initialProgram_;
    }

    /**
     Retrieves the fully qualified integrated file system path name of the job description used for jobs that start through subsystem work station entries.
     @return  The fully qualified integrated file system path name of the job description used for jobs that start through subsystem work station entries.
     @see  QSYSObjectPathName
     **/
    public String getJobDescription()
    {
        if (!loaded_) refresh();
        return jobDescription_;
    }

    /**
     Retrieves the keyboard buffering value that is used when a job is initialized for this user.
     @return  The keyboard buffering value that is used when a job is initialized for this user.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The system value QKBDBUF determines the keyboard buffering value for this user.
     <li>"*YES" - The type-ahead and attention-key buffering options are both on.
     <li>"*NO" - The type-ahead and attention-key buffering options are not on.
     <li>"*TYPEAHEAD" - The type-ahead option is on, but the attention-key buffering option is not.
     </ul>
    **/
    public String getKeyboardBuffering()
    {
        if (!loaded_) refresh();
        return keyboardBuffering_;
    }

    /**
     Retrieves the language ID used by the system for this user.
     @return  The language ID used by the system for this user.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The system value QLANGID will be used to determine the language ID.
     <li>The language ID.
     </ul>
     **/
    public String getLanguageID()
    {
        if (!loaded_) refresh();
        return languageID_;
    }

    /**
     Retrieves whether the user has limited capabilites.
     @return  Whether the user has limited capabilites.  Possible values are:
     <ul>
     <li>"*PARTIAL" - The user cannot change the initial program or current library.
     <li>"*YES" - The user cannot change the initial menu, initial program, or current library.  The user cannot run commands from the command line.
     <li>"*NO" - The user is not limited.
     </ul>
     **/
    public String getLimitCapabilities()
    {
        if (!loaded_) refresh();
        return limitCapabilities_;
    }

    /**
     Retrieves whether the user is limited to one device session.
     @return  Whether the user is limited to one device session.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The system value QLMTDEVSSN determines if the user is limited to one device session.
     <li>"*YES" - The user is limited to one session.
     <li>"*NO" - The user is not limited to one device session.
     </ul>
     **/
    public String getLimitDeviceSessions()
    {
        if (!loaded_) refresh();
        return limitDeviceSessions_;
    }

    /**
     Retrieves a list of the job attributes which are set from the user's locale path name.
     @return  A list of the job attributes which are set from the user's locale path name.  Possible values for the elements of this array are:
     <ul>
     <li>{@link #NONE User.NONE} - No job attributes are used from the locale path name at the time a job is started for this user profile.
     <li>"*SYSVAL" - The job attributes assigned from the locale path name are determined by the system value QSETJOBATR at the time a job is started for this user profile.
     <li>"*CCSID" - The coded character set identifier is set from the locale path name at the time a job is started for this user profile.
     <li>"*DATFMT" - The date format is set from the locale path name at the time a job is started for this user profile.
     <li>"*DATSEP" - The date separator is set from the locale path name at the time a job is started for this user profile.
     <li>"*SRTSEQ" - The sort sequence is set from the locale path name at the time a job is started for this user profile.
     <li>"*TIMSEP" - The time separator is set from the locale path name at the time a job is started for this user profile.
     <li>"*DECFMT" - The decimal format is set from the locale path name at the time a job is started for this user profile.
     </ul>
     **/
    public String[] getLocaleJobAttributes()
    {
        if (!loaded_) refresh();
        return localeJobAttributes_;
    }

    private static final String[] LOCALE_ATTRIBUTES = new String[]
    {
        User.NONE,
        "*SYSVAL",
        "*CCSID",
        "*DATFMT",
        "*DATSEP",
        "*SRTSEQ",
        "*TIMSEP",
        "*DECFMT"
    };

    /**
     Retrieves the locale path name that is assigned to the user profile when a job is started.
     @return  The locale path name that is assigned to the user profile when a job is started.  Possible values are:
     <ul>
     <li>"*C" - The C locale path name is assigned.
     <li>{@link #NONE User.NONE} - No locale path name is assigned.
     <li>"*POSIX" - The POSIX locale path name is assigned.
     <li>"*SYSVAL" - The QLOCALE system value is used to determine the locale path name.
     <li>A locale path name.
     </ul>
     **/
    public String getLocalePathName()
    {
        if (!loaded_) refresh();
        return localePathName_;
    }

    /**
     Retrieves the maximum amount of auxiliary storage (in kilobytes) that can be assigned to store permanant objects owned by the user.
     @return  The maximum amount of auxiliary storage (in kilobytes) that can be assigned to store permanant objects owned by the user.  Possible values are:
     <ul>
     <li>-1 - The user does not have a maximum amount of allowed storage (*NOMAX).
     <li>The maximum amount of auxiliary storage (in kilobytes).
     </ul>
     **/
    public int getMaximumStorageAllowed()
    {
        if (!loaded_) refresh();
        return maximumStorageAllowed_;
    }

    /**
     Retrieves the fully qualified integrated file system path name of the message queue that is used by this user.
     @return  The fully qualified integrated file system path name of the message queue that is used by this user.
     @see  QSYSObjectPathName
     **/
    public String getMessageQueue()
    {
        if (!loaded_) refresh();
        return messageQueue_;
    }

    /**
     Retrieves how the messages are delivered to the message queue used by the user.
     @return  How the messages are delivered to the message queue used by the user.  Possible values are:
     <ul>
     <li>"*BREAK" - The job to which the message queue is assigned is interrupted when a message arrives on the message queue.
     <li>"*DFT" - Messages requiring replies are answered with their default reply.
     <li>"*HOLD" - The messages are held in the message queue until they are requested by the user or program.
     <li>"*NOTIFY" - The job to which the message queue is assigned is notified when a message arrives on the message queue.
     </ul>
     **/
    public String getMessageQueueDeliveryMethod()
    {
        if (!loaded_) refresh();
        return messageQueueDeliveryMethod_;
    }

    /**
     Retrieves the lowest severity that a message can have and still be delivered to a user in break or notify mode.
     @return  The lowest severity that a message can have and still be delivered to a user in break or notify mode.  The severity is a value in the range 0 through 99.
     **/
    public int getMessageQueueSeverity()
    {
        if (!loaded_) refresh();
        return messageQueueSeverity_;
    }

    /**
     Returns the user profile name.
     @return  The user profile name.
     @see  #setName
     **/
    public String getName()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting name: " + name_);
        return name_;
    }

    /**
     Retrieves the user's object auditing value.
     @return  The user's object auditing value.  Possible values are:
     <ul>
     <li>{@link #NONE User.NONE} - No additional object auditing is done for the user.
     <li>"*CHANGE" - Object changes are audited for the user if the object's auditing value is *USRPRF.
     <li>"*ALL" - Object read and change operations are audited for the user if the object's auditing value is *USRPRF.
     </ul>
     **/
    public String getObjectAuditingValue()
    {
        if (!loaded_) refresh();
        return objectAuditingValue_;
    }

    /**
     Retrieves the output queue used by this user.
     @return  The output queue used by this user.  Possible values are:
     <ul>
     <li>"*WRKSTN" - The output queue assigned to the user's work station is used.
     <li>"*DEV" - An output queue with the same name as the device specified in the printer device parameter is used.
     <li>The fully qualified integrated file system path name of the output queue.
     </ul>
     @see QSYSObjectPathName
     **/
    public String getOutputQueue()
    {
        if (!loaded_) refresh();
        return outputQueue_;
    }

    /**
     Retrieves who is to own objects created by this user.
     @return  Who is to own objects created by this user.  Possible values are:
     <ul>
     <li>"*USRPRF" - The user owns any objects the user creates.  If the user does not have a group profile, the field contains this value.
     <li>"*GRPPRF" - The user's group profile owns any objects the user creates.
     </ul>
     **/
    public String getOwner()
    {
        if (!loaded_) refresh();
        return owner_;
    }

    /**
     Retrieves the date the user's password expires.
     @return  The date the user's password expires.  Possible values are:
     <ul>
     <li>A date object containing the date the user's password expires.
     <li>null - The user's password will not expire (password expiration interval of *NOMAX) or the user's password is set to expire.
     </ul>
     **/
    public Date getPasswordExpireDate()
    {
        if (!loaded_) refresh();
        if (passwordExpireDate_ == null)
        {
            try
            {
                passwordExpireDate_ = new DateTimeConverter(system_).convert(passwordExpireDateBytes_, "*DTS");
            }
            catch (Exception e)
            {
                if (Trace.traceOn_) Trace.log(Trace.ERROR, "Exception while converting datePasswordExpires:", e);
            }
        }
        return passwordExpireDate_;
    }

    /**
     Retrieves the number of days the user's password can remain active before it must be changed.
     @return  The number of days the user's password can remain active before it must be changed.  Possible values are:
     <ul>
     <li>0 - The system value QPWDEXPITV is used to determine the user's password expiration interval.
     <li>-1 - The user's password does not expire (*NOMAX).
     <li>1-366 - The number of days the user's password can remain active before it must be changed.
     </ul>
     **/
    public int getPasswordExpirationInterval()
    {
        if (!loaded_) refresh();
        return passwordExpirationInterval_;
    }

    /**
     Retrieves the date the user's password was last changed.
     @return  The date the user's password was last changed.
     **/
    public Date getPasswordLastChangedDate()
    {
        if (!loaded_) refresh();
        if (passwordLastChangedDate_ == null)
        {
            try
            {
                passwordLastChangedDate_ = new DateTimeConverter(system_).convert(passwordLastChangedDateBytes_, "*DTS");
            }
            catch (Exception e)
            {
                Trace.log(Trace.ERROR, "Exception while converting passwordLastChangedDate:", e);
            }
        }
        return passwordLastChangedDate_;
    }

    /**
     Retrieves the date and time the user last signed on.
     @return  The date and time the user last signed on.  Possible values are:
     <ul>
     <li>A date object containing the date and time the user last signed on.
     <li>null - The user has never signed on the system.
     </ul>
     **/
    public Date getPreviousSignedOnDate()
    {
        if (!loaded_) refresh();
        return previousSignedOnDate_;
    }

    /**
     Retrieves the printer used to print for this user.
     @return  The printer used to print for this user.  Possible values are:
     <ul>
     <li>"*WRKSTN" - The printer assigned to the user's work station is used.
     <li>"*SYSVAL" - The default system printer specified in the system value QPRTDEV is used.
     <li>The print device.
     </ul>
     **/
    public String getPrintDevice()
    {
        if (!loaded_) refresh();
        return printDevice_;
    }

    /**
     Retrieves the number of sign-on attempts that were not valid since the last successful sign-on.
     @return  The number of sign-on attempts that were not valid since the last successful sign-on.
     **/
    public int getSignedOnAttemptsNotValid()
    {
        if (!loaded_) refresh();
        return signedOnAttemptsNotValid_;
    }

    /**
     Retrieves the name of the sort sequence table used for string comparisons.
     @return  The name of the sort sequence table used for string comparisons.  Possible values are:
     <ul>
     <li>"*HEX" - The hexadecimal values of the characters are used to determine the sort sequence.
     <li>"*LANGIDUNQ" - A unique-weight sort table associated with the language specified.
     <li>"*LANGIDSHR" - A shared-weight sort table associated with the language specified.
     <li>"*SYSVAL" - The system value QSRTSEQ.
     <li>The fully qualified integrated file system path name of the sort sequence table name.
     </ul>
     @see  QSYSObjectPathName
     **/
    public String getSortSequenceTable()
    {
        if (!loaded_) refresh();
        return sortSequenceTable_;
    }

    /**
     Retrieves a list of the special authorities the user has.
     @return  A list of the special authorities the user has.  If the user has no special authorities, an empty array is returned.  Possible values for the elements of this array are:
     <ul>
     <li>{@link #SPECIAL_AUTHORITY_ALL_OBJECT User.SPECIAL_AUTHORITY_ALL_OBJECT} - All object.
     <li>{@link #SPECIAL_AUTHORITY_SECURITY_ADMINISTRATOR User.SPECIAL_AUTHORITY_SECURITY_ADMINISTRATOR} - Security administrator.
     <li>{@link #SPECIAL_AUTHORITY_JOB_CONTROL User.SPECIAL_AUTHORITY_JOB_CONTROL} - Job control.
     <li>{@link #SPECIAL_AUTHORITY_SPOOL_CONTROL User.SPECIAL_AUTHORITY_SPOOL_CONTROL} - Spool control.
     <li>{@link #SPECIAL_AUTHORITY_SAVE_SYSTEM User.SPECIAL_AUTHORITY_SAVE_SYSTEM} - Save system.
     <li>{@link #SPECIAL_AUTHORITY_SERVICE User.SPECIAL_AUTHORITY_SERVICE} - Service.
     <li>{@link #SPECIAL_AUTHORITY_AUDIT User.SPECIAL_AUTHORITY_AUDIT} - Audit.
     <li>{@link #SPECIAL_AUTHORITY_IO_SYSTEM_CONFIGURATION User.SPECIAL_AUTHORITY_IO_SYSTEM_CONFIGURATION} - Input/output system configuration.
     </ul>
     **/
    public String[] getSpecialAuthority()
    {
        if (!loaded_) refresh();
        return specialAuthority_;
    }

    /**
     Retrieves the special environment the user operates in after signing on.
     @return  The special environment the user operates in after signing on.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The system value QSPCENV is used to determine the user's special environment.
     <li>{@link #NONE User.NONE} - The user operates in the server operating system environment.
     <li>"*S36" - The user operates in the System/36 environment.
     </ul>
     **/
    public String getSpecialEnvironment()
    {
        if (!loaded_) refresh();
        return specialEnvironment_;
    }

    /**
     Retrieves the status of the user profile.
     @return  The status of the user profile.  Possible values are:
     <ul>
     <li>"*ENABLED" - The user profile is enabled; therefor, the user is able to sign on.
     <li>"*DISABLED" - The user profile is not enabled; therefor, the user cannot sign on.
     </ul>
     **/
    public String getStatus()
    {
        if (!loaded_) refresh();
        return status_;
    }

    /**
     Retrieves the amount of auxiliary storage (in kilobytes) occupied by this user's owned objects.
     @return  The amount of auxiliary storage (in kilobytes) occupied by this user's owned objects.
     **/
    public int getStorageUsed()
    {
        if (!loaded_) refresh();
        return storageUsed_;
    }

    /**
     Retrieves the supplemental groups for the user profile.
     @return  The array of supplemental groups for the user profile, or an array of length 0 if there are no supplemental groups.
     **/
    public String[] getSupplementalGroups()
    {
        if (!loaded_) refresh();
        return supplementalGroups_;
    }

    /**
     Retrieves the number of supplemental groups for the user profile.
     <p>This method simply returns getSupplementalGroups().length.
     @return  The number of supplemental groups for the user profile.
     @see  #getSupplementalGroups
     **/
    public int getSupplementalGroupsNumber()
    {
        if (!loaded_) refresh();
        return supplementalGroups_ == null ? 0 : supplementalGroups_.length;
    }

    /**
     Returns the system object representing the server on which the user profile exists.
     @return  The system object representing the server on which the user profile exists.  If the system has not been set, null is returned.
     **/
    public AS400 getSystem()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system: " + system_);
        return system_;
    }

    /**
     Retrieves a list of action audit levels for the user.
     @return  A list of action audit levels for the user.  Possible values for the elements of this array are:
     <ul>
     <li>"*CMD" - The user has the *CMD audit value specified in the user profile.
     <li>"*CREATE" - The user has the *CREATE audit value specified in the user profile.
     <li>"*DELETE" - The user has the *DELETE audit value specified in the user profile.
     <li>"*JOBDTA" - The user has the *JOBDTA audit value specified in the user profile.
     <li>"*OBJMGT" - The user has the *OBJMGT audit value specified in the user profile.
     <li>"*OFCSRV" - The user has the *OFCSRV audit value specified in the user profile.
     <li>"*OPTICAL" - The user has the *OPTICAL audit value specified in the user profile.
     <li>"*PGMADP" - The user has the *PGMADP audit value specified in the user profile.
     <li>"*SAVRST" - The user has the *SAVRST audit value specified in the user profile.
     <li>"*SECURITY" - The user has the *SECURITY audit value specified in the user profile.
     <li>"*SERVICE" - The user has the *SERVICE audit value specified in the user profile.
     <li>"*SPLFDTA" - The user has the *SPLFDTA audit value specified in the user profile.
     <li>"*SYSMGT" - The user has the *SYSMGT audit value specified in the user profile.
     </ul>
     **/
    public String[] getUserActionAuditLevel()
    {
        if (!loaded_) refresh();
        return userActionAuditLevel_;
    }

    private static final String[] AUDIT_LEVELS = new String[]
    {
        "*CMD",
        "*CREATE",
        "*DELETE",
        "*JOBDTA",
        "*OBJMGT",
        "*OFCSRV",
        "*PGMADP",
        "*SAVRST",
        "*SECURITY",
        "*SERVICE",
        "*SPLFDTA",
        "*SYSMGT",
        "*OPTICAL" // The API shows optical in the middle of the list, but experimental data proves otherwise (at least on V4R3).
    };

    /**
     Retrieves the user class name.
     @return  The user class name.  Possible values are:
     <ul>
     <li>"*SECOFR" - The user has a class of security officer.
     <li>"*SECADM" - The user has a class of security administrator.
     <li>"*PGMR" - The user has a class of programmer.
     <li>"*SYSOPR" - The user has a class of system operator.
     <li>"*USER" - The user has a class of end user.
     </ul>
     **/
    public String getUserClassName()
    {
        if (!loaded_) refresh();
        return userClassName_;
    }

    /**
     Retrieves the user ID (UID) number for the user profile.  The UID is used to identify the user when using the integrated file system.
     @return  The user ID (UID) number for the user profile.
     **/
    public long getUserID()
    {
        if (!loaded_) refresh();
        return userID_;
    }

    /**
     Retrieves the user ID (UID) number for the user profile.  The UID is used to identify the user when using the integrated file system.
     @return  The user ID (UID) number for the user profile.
     @deprecated  This method has been replaced by {@link #getUserID getUserID()} which returns a long.
     **/
    public int getUserIDNumber()
    {
        return (int)getUserID();
    }

    /**
     Retrieves a list of options for users to customize their environment.
     @return  The list of options for users to customize their environment.  Possible values include:
     <ul>
     <li>"*CLKWD" - The keywords are shown when a CL command is displayed.
     <li>"*EXPERT" - More detailed information is shown when the user is defining or changing the system using edit or display object authority.  This is independent of the ASTLVL parameter that is available on the user profile and other commands.
     <li>"*HLPFULL" - UIM online help is to be displayed full screen instead of in a window.
     <li>"*STSMSG" - Status messages sent to the user are shown.
     <li>"*NOSTSMSG" - Status messages sent to the user are not shown.
     <li>"*ROLLKEY" - The opposite action from the system default for roll keys is taken.
     <li>"*PRTMSG" - A message is sent to the user when a spooled file is printed.
     </ul>
     **/
    public String[] getUserOptions()
    {
        if (!loaded_) refresh();
        return userOptions_;
    }

    private static final String[] USER_OPTIONS = new String[]
    {
        "*CLKWD",
        "*EXPERT",
        "*HLPFULL",
        "*STSMSG",
        "*NOSTSMSG",
        "*ROLLKEY",
        "*PRTMSG"
    };

    /**
     Retrieves the name of the user profile for which the information is returned.  Note this is the name that is returned by the server, not the name that was set into this User object by the constructor or by a call to setUser().
     @return  The name of the user profile for which the information is returned.
     **/
    public String getUserProfileName()
    {
        if (!loaded_) refresh();
        return userProfileName_;
    }

    /**
     Retrieves if this user profile has been granted the specified authority, or belongs to a group profile that has been granted the specified authority.
     @param  authority  The authority to check.  It must be one of the following special authority values:
     <ul>
     <li>*ALLOBJ - All object.
     <li>*SECADM - Security administrator.
     <li>*JOBCTL - Job control.
     <li>*SPLCTL - Spool control.
     <li>*SAVSYS - Save system.
     <li>*SERVICE - Service.
     <li>*AUDIT - Audit.
     <li>*IOSYSCFG - Input/output system configuration.
     </ul>
     @return  true if this user has the authority or belongs to a group that has the authority; false if it does not have authority or an error occurs.
     **/
    public boolean hasSpecialAuthority(String authority)
    {
        if (authority == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'authority' is null.");
            throw new NullPointerException("authority");
        }

        // Check to see if this user is authorized.
        String[] specialAuthorities = getSpecialAuthority();
        if (specialAuthorities != null)
        {
            for (int i = 0; i < specialAuthorities.length; ++i)
            {
                if (specialAuthorities[i].equals(authority))
                {
                    return true;
                }
            }
        }

        // Check to see if a group this user belongs to is authorized.
        String primaryGroup = getGroupProfileName();
        if (primaryGroup != null && primaryGroup.equals(NONE))
        {
            try
            {
                User group = new User(system_, primaryGroup);
                if (group.hasSpecialAuthority(authority))
                {
                    return true;
                }
            }
            catch (Exception e)
            {
                Trace.log(Trace.ERROR, "Unexpected Exception constructing User object:", e);
                throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
            }
        }
        // Check the supplemental groups.
        String[] supplementalGroups = getSupplementalGroups();
        if (supplementalGroups != null)
        {
            for (int i = 0; i < supplementalGroups.length; ++i)
            {
                try
                {
                    User group = new User(system_, supplementalGroups[i]);
                    if (group.hasSpecialAuthority(authority))
                    {
                        return true;
                    }
                }
                catch (Exception e)
                {
                    Trace.log(Trace.ERROR, "Unexpected Exception constructing User object:", e);
                    throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
                }
            }
        }
        // Not authorized.
        return false;
    }

    /**
     Retrieves whether this user is a group that has members.
     <p>For User objects, this should always return false.  For UserGroup objects, this should return true if the group profile has members.
     @return  true if the user is a group that has members, false otherwise.
     @see  com.ibm.as400.access.UserGroup
     **/
    public boolean isGroupHasMember()
    {
        if (!loaded_ && !partiallyLoaded_) refresh();
        return groupHasMember_;
    }

    /**
     Retrieves whether the password is managed locally.
     @return  true if the password is managed locally, false otherwise.
     **/
    public boolean isLocalPasswordManagement()
    {
        if (!loaded_) refresh();
        return localPasswordManagement_;
    }

    /**
     Retrieves whether *NONE is specified for the password in the user profile.
     @return  true if *NONE is specified for the password in the user profile, false otherwise.
     **/
    public boolean isNoPassword()
    {
        if (!loaded_) refresh();
        return noPassword_;
    }

    /**
     Retrieves whether the user's password is set to expire, requiring the user to change the password when signing on.
     @return  true if the password set to expire, false otherwise.
     **/
    public boolean isPasswordSetExpire()
    {
        if (!loaded_) refresh();
        return passwordSetExpire_;
    }

    /**
     Retrieves whether there are digital certificates associated with this user.
     @return  true if there are digital certificates associated with this user; false otherwise.
     **/
    public boolean isWithDigitalCertificates()
    {
        if (!loaded_) refresh();
        return withDigitalCertificates_;
    }

    /**
     Refreshes the values for all attributes.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public void loadUserInformation() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Loading user information.");
        if (system_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot connect to server before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        if (name_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot connect to server before setting name.");
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        Converter conv = new Converter(system_.getCcsid(), system_);
        byte[] userProfileName = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        conv.stringToByteArray(name_, userProfileName);
        int receiverVariableLength = 1024;

        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Receiver variable, output, char(*).
            new ProgramParameter(receiverVariableLength),
            // Receiver variable length, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(receiverVariableLength)),
            // Format name, input, char(8), EBCDIC 'USRI0300'.
            new ProgramParameter(new byte[] { (byte)0xE4, (byte)0xE2, (byte)0xD9, (byte)0xC9, (byte)0xF0, (byte)0xF3, (byte)0xF0, (byte)0xF0 } ),
            // User profile name, input, char(10).
            new ProgramParameter(userProfileName),
            // Error code, I/O, char(*).
            ERROR_CODE
        };

        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QSYRUSRI.PGM", parameters);
        if (!pc.run())
        {
            throw new AS400Exception(pc.getMessageList());
        }

        byte[] data = parameters[0].getOutputData();

        int bytesReturned = BinaryConverter.byteArrayToInt(data, 0);
        int bytesAvailable = BinaryConverter.byteArrayToInt(data, 4);
        if (bytesReturned < bytesAvailable)
        {
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieve user information receiver variable too small, bytes returned: " + bytesReturned + ", bytes available: " + bytesAvailable);
            receiverVariableLength = bytesAvailable;
            try
            {
                parameters[0].setOutputDataLength(receiverVariableLength);
                parameters[1].setInputData(BinaryConverter.intToByteArray(receiverVariableLength));
            }
            catch (PropertyVetoException e)
            {
                Trace.log(Trace.ERROR, "Unexpected PropertyVetoException:", e);
                throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
            }
            if (!pc.run())
            {
                throw new AS400Exception(pc.getMessageList());
            }
            data = parameters[0].getOutputData();
        }

        userProfileName_ = conv.byteArrayToString(data, 8, 10).trim();

        // Previous sign-on is in format:  "CYYMMDDHHMMSS".
        String previousSignon = conv.byteArrayToString(data, 18, 13);
        if (previousSignon.trim().length() > 0)
        {
            Calendar cal = Calendar.getInstance();
            cal.clear();
            cal.set(Calendar.YEAR, 1900 + Integer.parseInt(previousSignon.substring(0, 3)));
            cal.set(Calendar.MONTH, Integer.parseInt(previousSignon.substring(3, 5)) - 1);
            cal.set(Calendar.DATE, Integer.parseInt(previousSignon.substring(5, 7)));
            cal.set(Calendar.HOUR, Integer.parseInt(previousSignon.substring(7, 9)));
            cal.set(Calendar.MINUTE, Integer.parseInt(previousSignon.substring(9, 11)));
            cal.set(Calendar.SECOND, Integer.parseInt(previousSignon.substring(11, 13)));
            previousSignedOnDate_ = cal.getTime();
        }
        else
        {
            previousSignedOnDate_ = null;
        }

        signedOnAttemptsNotValid_ = BinaryConverter.byteArrayToInt(data, 32);
        status_ = conv.byteArrayToString(data, 36, 10).trim();

        if (passwordLastChangedDateBytes_ == null) passwordLastChangedDateBytes_ = new byte[8];
        // *DTS format - convert on getter.
        System.arraycopy(data, 46, passwordLastChangedDateBytes_, 0, 8);
        passwordLastChangedDate_ = null;  // Reset.

        // EBCDIC 'Y' for no password.
        noPassword_ = data[54] == (byte)0xE8;
        // 1-366.  0 means use system value QPWDEXPITV.  -1 means *NOMAX.
        passwordExpirationInterval_ = BinaryConverter.byteArrayToInt(data, 56);

        if (passwordExpireDateBytes_ == null) passwordExpireDateBytes_ = new byte[8];
         // *DTS format.
        System.arraycopy(data, 60, passwordExpireDateBytes_, 0, 8);
        passwordExpireDate_ = null;  // Reset.

        daysUntilPasswordExpire_ = BinaryConverter.byteArrayToInt(data, 68);
        // EBCDIC 'Y' if the user's password is set to expire.
        passwordSetExpire_ = data[72] == (byte)0xE8;
        userClassName_ = conv.byteArrayToString(data, 73, 10).trim();

        int numSpecAuth = 0;
        for (int i = 0; i < 8; ++i)
        {
            if (data[83 + i] == (byte)0xE8)  // EBCDIC 'Y' is 0xE8.
            {
                ++numSpecAuth;
            }
        }
        specialAuthority_ = new String[numSpecAuth];
        int counter = 0;
        for (int i = 0; i < 8; ++i)
        {
            if (data[83 + i] == (byte)0xE8)  // EBCDIC 'Y' is 0xE8.
            {
                specialAuthority_[counter++] = SPECIAL_AUTHORITIES[i];
            }
        }

        groupProfileName_ = conv.byteArrayToString(data, 98, 10).trim();
        owner_ = conv.byteArrayToString(data, 108, 10).trim();
        groupAuthority_ = conv.byteArrayToString(data, 118, 10).trim();
        assistanceLevel_ = conv.byteArrayToString(data, 128, 10).trim();
        currentLibraryName_ = conv.byteArrayToString(data, 138, 10).trim();

        String menu = conv.byteArrayToString(data, 148, 10).trim();
        initialMenu_ = menu.equals("*SIGNOFF") ? menu : QSYSObjectPathName.toPath(conv.byteArrayToString(data, 158, 10).trim(), menu, "MNU");

        String prog = conv.byteArrayToString(data, 168, 10).trim();
        initialProgram_ = prog.equals(NONE) ? NONE : QSYSObjectPathName.toPath(conv.byteArrayToString(data, 178, 10).trim(), prog, "PGM");

        limitCapabilities_ = conv.byteArrayToString(data, 188, 10).trim();
        description_ = conv.byteArrayToString(data, 198, 50).trim();
        displaySignOnInformation_ = conv.byteArrayToString(data, 248, 10).trim();
        limitDeviceSessions_ = conv.byteArrayToString(data, 258, 10).trim();
        keyboardBuffering_ = conv.byteArrayToString(data, 268, 10).trim();
        maximumStorageAllowed_ = BinaryConverter.byteArrayToInt(data, 280);
        storageUsed_ = BinaryConverter.byteArrayToInt(data, 284);
        highestSchedulingPriority_ = data[288] & 0x0000000F;
        jobDescription_ = QSYSObjectPathName.toPath(conv.byteArrayToString(data, 299, 10).trim(), conv.byteArrayToString(data, 289, 10).trim(), "JOBD");
        accountingCode_ = conv.byteArrayToString(data, 309, 15).trim();
        messageQueue_ = QSYSObjectPathName.toPath(conv.byteArrayToString(data, 334, 10).trim(), conv.byteArrayToString(data, 324, 10).trim(), "MSGQ");
        messageQueueDeliveryMethod_ = conv.byteArrayToString(data, 344, 10).trim();
        messageQueueSeverity_ = BinaryConverter.byteArrayToInt(data, 356);

        String outQueueName = conv.byteArrayToString(data, 360, 10).trim();
        outputQueue_ = outQueueName.equals("*WRKSTN") || outQueueName.equals("*DEV") ? outQueueName : QSYSObjectPathName.toPath(conv.byteArrayToString(data, 370, 10).trim(), outQueueName, "OUTQ");

        printDevice_ = conv.byteArrayToString(data, 380, 10).trim();
        specialEnvironment_ = conv.byteArrayToString(data, 390, 10).trim();

        String keyName = conv.byteArrayToString(data, 400, 10).trim();
        attentionKeyHandlingProgram_ = keyName.equals(NONE) || keyName.equals("*SYSVAL") ? keyName : QSYSObjectPathName.toPath(conv.byteArrayToString(data, 410, 10).trim(), keyName, "PGM");

        languageID_ = conv.byteArrayToString(data, 420, 10).trim();
        countryID_ = conv.byteArrayToString(data, 430, 10).trim();
        ccsid_ = BinaryConverter.byteArrayToInt(data, 440);

        int numUserOptions = 0;
        for (int i = 0; i < 7; ++i)
        {
            if (data[444 + i] == (byte)0xE8)  // EBCDIC 'Y' is 0xE8.
            {
                ++numUserOptions;
            }
        }
        userOptions_ = new String[numUserOptions];
        counter = 0;
        for (int i = 0; i < 7; ++i)
        {
            if (data[444 + i] == (byte)0xE8)  // EBCDIC 'Y' is 0xE8.
            {
                userOptions_[counter++] = USER_OPTIONS[i];
            }
        }

        String sortName = conv.byteArrayToString(data, 480, 10).trim();
        sortSequenceTable_ = sortName.equals("*HEX") || sortName.equals("*LANGIDUNQ") || sortName.equals("*LANGIDSHR") || sortName.equals("*SYSVAL") || sortName.length() == 0 ? sortName : QSYSObjectPathName.toPath(conv.byteArrayToString(data, 490, 10).trim(), sortName, "FILE");

        objectAuditingValue_ = conv.byteArrayToString(data, 500, 10).trim();

        int numAudLevel = 0;
        for (int i = 0; i < 13; ++i)
        {
            if (data[510 + i] == (byte)0xE8)  // EBCDIC 'Y' is 0xE8.
            {
                ++numAudLevel;
            }
        }
        userActionAuditLevel_ = new String[numAudLevel];
        counter = 0;
        for (int i = 0; i < 13; ++i)
        {
            if (data[510 + i] == (byte)0xE8)  // EBCDIC 'Y' is 0xE8.
            {
                userActionAuditLevel_[counter++] = AUDIT_LEVELS[i];
            }
        }

        groupAuthorityType_ = conv.byteArrayToString(data, 574, 10).trim();

        int supplementalGroupOffset = BinaryConverter.byteArrayToInt(data, 584);
        int supplementalGroupCount = BinaryConverter.byteArrayToInt(data, 588);
        supplementalGroups_ = new String[supplementalGroupCount];
        for (int i = 0; i < supplementalGroupCount; ++i)
        {
            supplementalGroups_[i] = conv.byteArrayToString(data, supplementalGroupOffset + i * 10, 10).trim();
        }

        userID_ = BinaryConverter.byteArrayToUnsignedInt(data, 592);
        groupID_ = BinaryConverter.byteArrayToUnsignedInt(data, 596);

        int homeDirOffset = BinaryConverter.byteArrayToInt(data, 600);
        int homeDirCcsid = BinaryConverter.byteArrayToInt(data, homeDirOffset);
        int homeDirLength = BinaryConverter.byteArrayToInt(data, homeDirOffset + 16);
        Converter homeDirConv = homeDirCcsid > 0 && homeDirCcsid < 65535 ? new Converter(homeDirCcsid, system_) : conv;
        homeDirectory_ = homeDirConv.byteArrayToString(data, homeDirOffset + 32, homeDirLength).trim();

        int numLocaleJobAttribs = 0;
        for (int i = 0; i < 8; ++i)
        {
            if (data[608 + i] == (byte)0xE8)  // EBCDIC 'Y' is 0xE8.
            {
                ++numLocaleJobAttribs;
            }
        }
        localeJobAttributes_ = new String[numLocaleJobAttribs];
        counter = 0;
        for (int i = 0; i < 8; ++i)
        {
            if (data[608 + i] == (byte)0xE8)  // EBCDIC 'Y' is 0xE8.
            {
                localeJobAttributes_[counter++] = LOCALE_ATTRIBUTES[i];
            }
        }

        int localePathOffset = BinaryConverter.byteArrayToInt(data, 624);
        int localePathLength = BinaryConverter.byteArrayToInt(data, 628);
        if (localePathLength == 10)
        {
            localePathName_ = conv.byteArrayToString(data, localePathOffset, localePathLength).trim();
        }
        else
        {
            int localePathCcsid = BinaryConverter.byteArrayToInt(data, localePathOffset);
            localePathLength = BinaryConverter.byteArrayToInt(data, localePathOffset + 16);
            Converter localePathConv = localePathCcsid > 0 && localePathCcsid < 65535 ? new Converter(localePathCcsid, system_) : conv;
            localePathName_ = localePathConv.byteArrayToString(data, localePathOffset + 32, localePathLength).trim();
        }

        // EBCDIC '1' indicates the user is a group that has members.
        groupHasMember_ = data[632] == (byte)0xF1;
        // EBCDIC '1' indicates there is at least one digital certificate associated with this user.
        withDigitalCertificates_ = data[633] == (byte)0xF1;
        chridControl_ = conv.byteArrayToString(data, 634, 10).trim();

        int vrm = system_.getVRM();
        if (vrm >= 0x00050100)
        {
            int iaspOffset = BinaryConverter.byteArrayToInt(data, 644);
            int iaspCount = BinaryConverter.byteArrayToInt(data, 648);
            int iaspCountReturned = BinaryConverter.byteArrayToInt(data, 652);
            int iaspLength = BinaryConverter.byteArrayToInt(data, 656);
            if (Trace.traceOn_ && iaspCount != iaspCountReturned)
            {
                Trace.log(Trace.WARNING, "Not all IASP information was returned, count: " + iaspCount + ", returned:", iaspCountReturned);
            }
            iaspNames_ = new String[iaspCountReturned];
            iaspStorageAllowed_ = new int[iaspCountReturned];
            iaspStorageUsed_ = new int[iaspCountReturned];
            for (int i = 0; i < iaspCountReturned; ++i)
            {
                int offset = iaspOffset + (i * iaspLength);
                iaspNames_[i] = conv.byteArrayToString(data, offset, 10).trim();
                iaspStorageAllowed_[i] = BinaryConverter.byteArrayToInt(data, offset + 12);
                iaspStorageUsed_[i] = BinaryConverter.byteArrayToInt(data, offset + 16);
            }

            if (vrm >= 0x00050300)
            {
                // EBCDIC 'Y' indicates the password is managed locally.
                localPasswordManagement_ = data[660] == (byte)0xE8;
            }
        }
        loaded_ = true;
        connected_ = true;
    }

    // Helper method... calls loadUserInformation and swallows all exceptions so that all of the getters can call it.
    private void refresh()
    {
        try
        {
            loadUserInformation();
        }
        catch (Exception e)
        {
            Trace.log(Trace.ERROR, "Exception swallowed by refresh():", e);
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

    // Used by the setters to change the user profile.
    private void runCommand(String parameters) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Changing user profile.");
        if (system_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot connect to server before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        if (name_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot connect to server before setting name.");
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        CommandCall cmd = new CommandCall(system_, "QSYS/CHGUSRPRF USRPRF(" + name_ + ") " + parameters);
        cmd.setThreadSafe(false);

        if (!cmd.run())
        {
            throw new AS400Exception(cmd.getMessageList());
        }
        loaded_ = false;
        connected_ = true;
    }

    // Used by the setters to change the user auditing.
    private void runCommandAud(String parameters) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Changing user auditing.");
        if (system_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot connect to server before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
        if (name_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot connect to server before setting name.");
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        CommandCall cmd = new CommandCall(system_, "QSYS/CHGUSRAUD USRPRF(" + name_ + ") " + parameters);
        cmd.setThreadSafe(false);

        if (!cmd.run())
        {
            throw new AS400Exception(cmd.getMessageList());
        }
        loaded_ = false;
        connected_ = true;
    }

    /**
     Sets the accounting code that is associated with this user profile.
     @param accountingCode The accounting code that is associated with this user profile.  Possible values are:
     <ul>
     <li>"*BLANK" - An accounting code of 15 blanks is assigned to this user profile.
     <li>An accounting code - A 15 character accounting code to be used by jobs that get their accounting code from this user profile.  If less than 15 characters are specified, the string is padded on the right with blanks.
     </ul>
     **/
    public void setAccountingCode(String accountingCode) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (accountingCode == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'accountingCode' is null.");
            throw new NullPointerException("accountingCode");
        }
        if (accountingCode.equals("*BLANK"))
        {
            runCommand("ACGCDE(*BLANK)");
        }
        else
        {
            runCommand("ACGCDE('" + accountingCode + "')");
        }
    }

    // Convenience method for making a command string from array of strings.
    static private String setArrayToString(String[] array)
    {
        int arrayLength = array.length;
        if (arrayLength == 0) return "*NONE";
        if (arrayLength == 1) return array[0];
        String string = array[0];
        for (int i = 1; i < arrayLength; ++i)
        {
            string += " " + array[i];
        }
        return string;
    }

    /**
     Sets which user interface to use.
     @param assistanceLevel The user interface to use.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The assistance level defined in the system value QASTLVL is used.
     <li>"*BASIC" - The Operational Assistant user interface is used.
     <li>"*INTERMED" - The system user interface is used.
     <li>"*ADVANCED" - The expert system user interface is used.  To allow for more list entries, option keys and function keys are not displayed.  If a command does not have an advanced (*ADVANCED) level, the intermediate (*INTERMED) level is used.
     </ul>
     **/
    public void setAssistanceLevel(String assistanceLevel) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (assistanceLevel == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'assistanceLevel' is null.");
            throw new NullPointerException("assistanceLevel");
        }
        runCommand("ASTLVL(" + assistanceLevel + ")");
    }

    /**
     Sets the program to be used as the Attention (ATTN) key handling program for this user.  The ATTN key handling program is called when the ATTN key is pressed during an interactive job.  The program is active only when the user routes to the system-supplied QCMD processor.  The ATTN key handling program is set on before the initial program (if any) is called and it is active for both program and menu.  If the program changes the ATNPGM (by using the SETATNPGM command), the new program remains active only for the duration of the program.  When contol returns and QCMD call the menu, the original ATTN key handling program becomes active again.  If the SETATNPGM command is run from the menues or an application is called from the menues, the new ATTN key handling program that is specified overrides the original ATTN key handling program.  If *YES or *PARTIAL is specified for the Limit capabilites (LMTCPB) parameter on the Create User Profile (CRTUSRPRF) or Change User Profile (CHGUSRPRF) command, the ATTN key handling program cannot be changed.  The caller must have *USE authority to the specified program.
     @param attentionKeyHandlingProgram The program to be used as the Attention (ATTN) key handling program for this user.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The system value QATNPGM is used.
     <li>{@link #NONE User.NONE} - No ATTN key handling program is used by this user.
     <li>"*ASSIST" - The Operational Assistant ATTN key handling program, QEZMAIN, is used.
     <li>The fully qualified integrated file system path name of the attention key handling program.
     </ul>
     @see  QSYSObjectPathName
     **/
    public void setAttentionKeyHandlingProgram(String attentionKeyHandlingProgram) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (attentionKeyHandlingProgram == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'attentionKeyHandlingProgram' is null.");
            throw new NullPointerException("attentionKeyHandlingProgram");
        }
        if (attentionKeyHandlingProgram.length() == 0 || attentionKeyHandlingProgram.startsWith("*"))
        {
            runCommand("ATNPGM(" + attentionKeyHandlingProgram + ")");
        }
        else
        {
            QSYSObjectPathName ifs = new QSYSObjectPathName(attentionKeyHandlingProgram, "PGM");
            runCommand("ATNPGM(" + ifs.getLibraryName() + "/" + ifs.getObjectName() + ")");
        }
    }

    /**
     Sets the character code set identifier (CCSID) to be used for this user.
     <p>A CCSID is a 16-bit number identifying a specific set of encoding scheme identifiers, character set identifiers, code page identifiers, and additional coding-related information that uniquely identifies the coded graphic representation used.
     <p>Note:  If the value for CCSID is changed, the change does not affect job that are currently running.
     @param ccsid The character code set identifier (CCSID) to be used for this user.  Possible values are:
     <ul>
     <li>-2 - The system value QCCSID is used to determine the user's character code set ID.
     <li>A character code set ID.
     </ul>
     **/
    public void setCCSID(int ccsid) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        runCommand("CCSID(" + (ccsid == -2 ? "*SYSVAL" : Integer.toString(ccsid)) + ")");
    }

    /**
     Sets the character code set identifier (CCSID) to be used for this user.
     <p>A CCSID is a 16-bit number identifying a specific set of encoding scheme identifiers, character set identifiers, code page identifiers, and additional coding-related information that uniquely identifies the coded graphic representation used.
     <p>Note:  If the value for CCSID is changed, the change does not affect job that are currently running.
     @param ccsid The character code set identifier (CCSID) to be used for this user.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The system value QCCSID is used to determine the user's character code set ID.
     <li>"*HEX" - The CCSID 65535 is used.
     <li>A character code set ID.
     </ul>
     **/
    public void setCCSID(String ccsid) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (ccsid == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'ccsid' is null.");
            throw new NullPointerException("ccsid");
        }
        runCommand("CCSID(" + ccsid + ")");
    }

    /**
     Sets the character identifier control (CHRIDCTL) for the job.  This attribute controls the type of coded character set identifier (CCSID) conversion that occurs for display files, printer files and panel groups.  The *CHRIDCTL special value must be specified for the Character identifier (CHRID) parameter an the create, change, or override commands for display files, printer files, and panel groups before this attribute will be used.
     @param chridControl The character identifier control (CHRIDCTL) for the job.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The system value QCHRIDCTL is used.
     <li>"*DEVD" - The *DEVD special value performs the same function as on the CHRID command parameter for display files, printer files, and panel groups.
     <li>"*JOBCCSID" - The *JOBCCSID special value performs the same function as on the CHRID command parameter for display files, printer files, and panel groups.
     </ul>
     **/
    public void setCHRIDControl(String chridControl) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (chridControl == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'chridControl' is null.");
            throw new NullPointerException("chridControl");
        }
        runCommand("CHRIDCTL(" + chridControl + ")");
    }

    /**
     Sets the country or region identifier to be used for this user.
     @param countryID The country or region identifier to be used for this user.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The system value QCNTRYID is used.
     <li>A country or region identifier.
     </ul>
     **/
    public void setCountryID(String countryID) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (countryID == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'countryID' is null.");
            throw new NullPointerException("countryID");
        }
        runCommand("CNTRYID(" + countryID + ")");
    }

    /**
     Sets the name of the current library associated with the job being run.
     <p>Specifies the name of the library to be used as the current library for this user.  If *PARTIAL or *YES is specified for the Limit capabilities (LMTCPB) parameter of the Create User Profile (CRTUSRPRF) or Change User Profile (CHGUSRPRF) command, the user cannot change the current library at sign-on or with the Change Profile (CHGPRF) command.
     <p>The caller must have *USE authority to the specified library.
     @param currentLibraryName The name of the current library associated with the job being run.  Possible values are:
     <ul>
     <li>"*CRTDFT" - The user has no current library.  The library QGPL is used as the default current library.
     <li>A library name.
     </ul>
     **/
    public void setCurrentLibraryName(String currentLibraryName) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (currentLibraryName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'currentLibraryName' is null.");
            throw new NullPointerException("currentLibraryName");
        }
        runCommand("CURLIB(" + currentLibraryName + ")");
    }

    /**
     Sets the text that briefly describes the object.
     @param description The text that briefly describes the object.  Possible values are:
     <ul>
     <li>"*BLANK" - No text is specified.
     <li>No more than 50 characters of text.
     </ul>
     **/
    public void setDescription(String description) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (description == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'description' is null.");
            throw new NullPointerException("description");
        }
        if (description.equals("*BLANK"))
        {
            runCommand("TEXT(*BLANK)");
        }
        else
        {
            runCommand("TEXT('" + description + "')");
        }
    }

    /**
     Sets whether the sign-on information display is shown.
     @param displaySignOnInformation Whether the sign-on information display is shown.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The system value QDSPSGNINF is used to determine whether the sign-on information display is shown.
     <li>"*NO" - The sign-on information display is not shown.
     <li>"*YES" - The sign-on information display is shown.
     </ul>
     **/
    public void setDisplaySignOnInformation(String displaySignOnInformation) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (displaySignOnInformation == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'displaySignOnInformation' is null.");
            throw new NullPointerException("displaySignOnInformation");
        }
        runCommand("DSPSGNINF(" + displaySignOnInformation + ")");
    }

    /**
     Sets the specific authority given to the group profile for newly created objects.  If *GRPPRF is specified for the Owner (OWNER) parameter, specification of this parameter is not allowed.
     @param groupAuthority The specific authority given to the group profile for newly created objects.  Possible values are:
     <ul>
     <li>{@link #NONE User.NONE} - No group authority is given.
     <li>"*ALL" - The user can perform all operations execept those limited to the owner or controlled by authorization list management (*AUTLMGT) authority.  The user can control the object's existence, specify the security for the object, change the object, and perform basic functions on the object.  The user can also change ownership of the object.
     <li>"*CHANGE" - The user can perform all operations execept those limited to the owner or controlled by the object existence (*OBJEXIST) and object management (*OBJMGT) authorities.  The user can change and perform basic functions on the object.  *CHANGE authority provides object operational (*OBJOPR) authority and all data authority.  If the object is an authorization list, the user cannot add, change, or remove users.
     <li>"*USE" - The user can perform basic operations on the object, such as running a program or reading a file.  The user cannot change the object.  User (*USE) authority provides object operational (*OBJOPR), read (*READ), and execute (*EXECUTE) authorities.
     <li>"*EXCLUDE" - The user cannot access the object.
     </ul>
     **/
    public void setGroupAuthority(String groupAuthority) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (groupAuthority == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'groupAuthority' is null.");
            throw new NullPointerException("groupAuthority");
        }
        runCommand("GRPAUT(" + groupAuthority + ")");
    }

    /**
     Sets the type of authority to be granted to the group profile for newly-created objects.  If *NONE is specified for the Group Authority (GRPAUT) parameter, specification of this parameter is ignored.
     @param groupAuthorityType The type of authority to be granted to the group profile for newly-created objects.  Possible values are:
     <ul>
     <li>"*PRIVATE" - The group profile is granted private authority to newly-created objects, with the authority value determined by the GRPAUT parameter.  If the authority value in the GRPAUT parameter is *NONE, this value is ignored.
     <li>"*PGP" - The group profile is the primary group for newly-created objects, with the authority value determined by the GRPAUT parameter.  If the authority value in the GRPAUT parameter is *NONE, this value is ignored.
     </ul>
     **/
    public void setGroupAuthorityType(String groupAuthorityType) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (groupAuthorityType == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'groupAuthorityType' is null.");
            throw new NullPointerException("groupAuthorityType");
        }
        runCommand("GRPAUTTYP(" + groupAuthorityType + ")");
    }

    /**
     Sets the group ID number (gid number) for this user profile.  The gid number is used to identify the group profile when a member of the group is using the directory file system.  The gid number for a user may not be changed if:
     <ul>
     <li>The user profile is the primary group of an object in a directory.
     <li>There are one or more active jobs for the user.
     </ul>
     @param groupID The group ID number (gid number) for this user profile.  Possible values are:
     <ul>
     <li>0 - The user does not have a gid number or an existing gid number is removed.
     <p>Note:  This value cannot be specified if the user is a group profile or the primary group of an object.
     <li>1-4294967294 - The gid number to be assigned to the user profile.  The gid number assigned must not already be assigned to another user profile.
     </ul>
     **/
    public void setGroupID(long groupID) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (groupID == 0)
        {
            runCommand("GID(*NONE)");
        }
        else
        {
            runCommand("GID(" + Long.toString(groupID) + ")");
        }
    }

    /**
     Sets the group ID number (gid number) for this user profile.  The gid number is used to identify the group profile when a member of the group is using the directory file system.  The gid number for a user may not be changed if:
     <ul>
     <li>The user profile is the primary group of an object in a directory.
     <li>There are one or more active jobs for the user.
     </ul>
     @param groupID The group ID number (gid number) for this user profile.  Possible values are:
     <ul>
     <li>"*NONE" - The user does not have a gid number or an existing gid number is removed.
     <p>Note:  This value cannot be specified if the user is a group profile or the primary group of an object.
     <li>"*GEN" - The gid number will be generated for the user.  The system generates a gid number that is not already assigned to another user.  The gid number generated is greater than 100.
     <li>1-4294967294 - The gid number to be assigned to the user profile.  The gid number assigned must not already be assigned to another user profile.
     </ul>
     **/
    public void setGroupID(String groupID) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (groupID == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'groupID' is null.");
            throw new NullPointerException("groupID");
        }
        runCommand("GID(" + groupID + ")");
    }

    /**
     Sets the user's group profile name whose authority is used if no specific authority is given for the user.  The caller must have object management (*OBJMGT) and change (*CHANGE) authority to the profile specified for the Group profile (GRPPRF) parameter.  The required *OBJMGT authority cannot be given by a program adopt operation.
     @param groupProfileName The user's group profile name whose authority is used if no specific authority is given for the user.  Possible values are:
     <ul>
     <li>{@link #NONE User.NONE} - The user profile has no group profile.
     <li>The name of the group profile used with this user profile.
     </ul>
     **/
    public void setGroupProfileName(String groupProfileName) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (groupProfileName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'groupProfileName' is null.");
            throw new NullPointerException("groupProfileName");
        }
        runCommand("GRPPRF(" + groupProfileName + ")");
    }

    /**
     Sets the highest scheduling priority the user is allowed to have for each job submitted to the system.  This value controls the job processing priority and output priority for any job running under this user profile; that is, values specified in the JOBPTY and OUTPTY parameters of any job command cannot exceed the PTYLMT value of the user profile under which the job is run.  The scheduling priority can have a value ranging from 0 through 9, where 0 is the highest priority and 9 is the lowest priority.
     @param highestSchedulingPriority The highest scheduling priority the user is allowed to have for each job submitted to the system.
     **/
    public void setHighestSchedulingPriority(int highestSchedulingPriority) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        runCommand("PTYLMT(" + Integer.toString(highestSchedulingPriority) + ")");
    }

    /**
     Sets the path name of the home directory for this user profile.  The home directory is the user's initial working directory.  The working directory, associated with a process, is used during path name resolution in the directory file system for path names that do not begin with a slash (/).  If the home directory specified does not exist when the user signs on, the user's initial working directory is the root (/) directory.
     @param homeDirectory The path name of the home directory for this user profile.  Possible values are:
     <ul>
     <li>"*USRPRF" - The home directory assigned to the user will be /home/USRPRF, where USRPRF is the name of the user profile.
     <li>The path name of the home directory to be assigned to this user.
     </ul>
     **/
    public void setHomeDirectory(String homeDirectory) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (homeDirectory == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'homeDirectory' is null.");
            throw new NullPointerException("homeDirectory");
        }
        if (homeDirectory.length() == 0 || homeDirectory.startsWith("*"))
        {
            runCommand("HOMEDIR(" + homeDirectory + ")");
        }
        else
        {
            runCommand("HOMEDIR('" + homeDirectory + "')");
        }
    }

    /**
     Sets the initial menu displayed when the user signs on the system if the user's routing program is the command processor.  If *YES is specified for the Limit capabilities (LMTCPB) parameter, the user cannot change the menu either at sign-on or with the Change Profile (CHGPRF) command.
     <p>A System/36 environment menu can be specified as the initial menu if either of the following conditions are true:
     <ul>
     <li>*S36 is specified for the Special environments (SPCENV) parameter.
     <li>*SYSVAL is specified on the SPCENV parameter and the system value, QSPCENV, is *S36.
     </ul>
     <p>The caller must have *USE authority to the specified menu.
     @param initialMenu The initial menu displayed when the user signs on the system if the user's routing program is the command processor.  Possible values are:
     <ul>
     <li>"*SIGNOFF" - The system signs off the user when the program completes.  This is intended for users authorized only to run the program.
     <li>The fully qualified integrated file system path name of the initial menu.
     </ul>
     @see  QSYSObjectPathName
     **/
    public void setInitialMenu(String initialMenu) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (initialMenu == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'initialMenu' is null.");
            throw new NullPointerException("initialMenu");
        }
        if (initialMenu.length() == 0 || initialMenu.startsWith("*"))
        {
            runCommand("INLMNU(" + initialMenu + ")");
        }
        else
        {
            QSYSObjectPathName ifs = new QSYSObjectPathName(initialMenu, "MNU");
            runCommand("INLMNU(" + ifs.getLibraryName() + "/" + ifs.getObjectName() + ")");
        }
    }

    /**
     Sets, for an interactive job, the program called whenever a new routing step is started that has QCMD as the request processing program.  If *PARTIAL or *YES is specified for the Limit capabilities parameter, the program value cannot be changed at sign on or by using the Change Profile (CHGPRF) command.  No parameters can be passed to the program.
     <p>A System/36 environment procedure name can be specified as the initial program if the procedure is a member of the file QS36PRC (in the library list or specified library) and if either of the following conditions are true:
     <ul>
     <li>*36 is specified on the SPCENV parameter.
     <li>*SYSVAL is specified on the SPCENV parameter and the system value, QSPCENV is *S36.
     </ul>
     <p>The caller must have *USE authority to the specified program.
     @param initialProgram The initial program for the user.  Possible values are:
     <ul>
     <li>{@link #NONE User.NONE} - No program is called when the user signs on.  If a menu name is specified in the Initial menu (INLMNU) parameter, that menu is displayed.
     <li>The fully qualified integrated file system path name of the initial program for the user.
     </ul>
     @see  QSYSObjectPathName
     **/
    public void setInitialProgram(String initialProgram) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (initialProgram == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'initialProgram' is null.");
            throw new NullPointerException("initialProgram");
        }
        if (initialProgram.length() == 0 || initialProgram.startsWith("*"))
        {
            runCommand("INLPGM(" + initialProgram + ")");
        }
        else
        {
            QSYSObjectPathName ifs = new QSYSObjectPathName(initialProgram, "PGM");
            runCommand("INLPGM(" + ifs.getLibraryName() + "/" + ifs.getObjectName() + ")");
        }
    }

    /**
     Sets the fully qualified integrated file system path name of the job description used for jobs that start through subsystem work station entries.  If the job description does not exist when the user profile is created or changed, a library qualifier must be specified, because the job description name is kept in the user profile.  The caller must have *USE authority to the specified job description.
     @param jobDescription The fully qualified integrated file system path name of the job description used for jobs that start through subsystem work station entries.
     @see  QSYSObjectPathName
     **/
    public void setJobDescription(String jobDescription) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (jobDescription == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'jobDescription' is null.");
            throw new NullPointerException("jobDescription");
        }
        if (jobDescription.length() == 0 || jobDescription.startsWith("*"))
        {
            runCommand("JOBD(" + jobDescription + ")");
        }
        else
        {
            QSYSObjectPathName ifs = new QSYSObjectPathName(jobDescription, "JOBD");
            runCommand("JOBD(" + ifs.getLibraryName() + "/" + ifs.getObjectName() + ")");
        }
    }

    /**
     Sets the keyboard buffering value to be used when a job is initialized for this user profile.  If the type-ahead feature is active, you can buffer your keyboard strokes.  If the attention key buffering option is active, the attention key is buffered as any other key.  If it is not active, the attention key is not buffered and is sent to the system even if the display station is input-inhibited.  This value can alse be set by a user application.
     @param keyboardBuffering The keyboard buffering value to be used when a job is initialized for this user profile.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The system value, QKBDBUF, is used to determine the keyboard buffering value.
     <li>"*NO" - The type-ahead and attention-key buffering options are not active.
     <li>"*TYPEAHEAD" - The type-ahead option is active, but the attention key buffering option is not.
     <li>"*YES" - The type-ahead and attention key buffering options are active.
     </ul>
    **/
    public void setKeyboardBuffering(String keyboardBuffering) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (keyboardBuffering == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'keyboardBuffering' is null.");
            throw new NullPointerException("keyboardBuffering");
        }
        runCommand("KBDBUF(" + keyboardBuffering + ")");
    }

    /**
     Sets the language ID to be used for this user.
     @param languageID The language ID to be used for this user.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The system value QLANGID is used.
     <li>The language ID to be used.
     </ul>
     **/
    public void setLanguageID(String languageID) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (languageID == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'languageID' is null.");
            throw new NullPointerException("languageID");
        }
        runCommand("LANGID(" + languageID + ")");
    }

    /**
     Sets the limit to which the user can control the program, menu, current library, and the ATTN key handling program values.  It alse determines whether the user can run commands from the command line.  This parameter is ignored when the security level is 10.
     <p>Note:  When creating or changing other users' user profile, you cannot specify values on this parameter that grant greater capabilities to other users than your own user profile grants to you.  For example, if *PARTIAL is specified for the Limit capabilities (LMTCPB) parameter in your user profile, you can specify *PARTIAL or *YES for anther user.  You cannot specify *NO for another user.
     @param limitCapabilities The limit to which the user can control the program, menu, current library, and the ATTN key handling program values.  Possible values are:
     <ul>
     <li>"*NO" - The program, menu, and current library values can be changed when the usre signs on the system.  User may change the program, menu, current library, or ATTN key handling program values is the own user profiles with the Change Profile (CHGPRF) command.  Commands can be run from a command line.
     <li>"*PARTIAL" - The program and current library cannot be changed on the sign-on display.  The menu can be changed and comands can be run from a command line.  A user can change the menu value with the Change profile (CHGPRF) command.  The program, current library, and the ATTN key handling program cannot be changed using the CHGPRF command.
     <li>"*YES" - The program, menu, and current library values cannot be changed on the sign-on display.  Commands cannot be run when issued from a command line or by selecting an option from a command grouping menu such as CMDADD, but can still be run from a command entry screen.  The user cannot change the program, menu, current library, or the ATTN key program handling values by using the CHGPRF command.
     </ul>
     **/
    public void setLimitCapabilities(String limitCapabilities) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (limitCapabilities == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'limitCapabilities' is null.");
            throw new NullPointerException("limitCapabilities");
        }
        runCommand("LMTCPB(" + limitCapabilities + ")");
    }

    /**
     Sets if the number of device sessions allowed for a user is limited to 1.  This does not limit SYSREQ and second sign-on.
     @param limitDeviceSessions If the number of device sessions allowed for a user is limited to 1.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The system value QLMTDEVSSN is used to determine whether the user is limited to a single device session.
     <li>"*NO" - The user is not limited to one device session.
     <li>"*YES" - The user is limited to one session.
     </ul>
     **/
    public void setLimitDeviceSessions(String limitDeviceSessions) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (limitDeviceSessions == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'limitDeviceSessions' is null.");
            throw new NullPointerException("limitDeviceSessions");
        }
        runCommand("LMTDEVSSN(" + limitDeviceSessions + ")");
    }

    /**
     Sets which job attributes are to be taken from the locale specified for the Locale (LOCALE) parameter when the job is initiated.
     @param localeJobAttributes A list of attributes which are set from the locale path name at the time a job is started for this user.  Possible values for the elements of this array are:
     <ul>
     <li>"*SYSVAL" - The system value, QSETJOBATR, is used to determine which job attributes are taken from the locale.
     <li>{@link #NONE User.NONE} - No job attributes are taken from the locale.
     <li>"*CCSID" - The coded character set identifier from the locale is used.  The CCSID value from the locale overrides the user profile CCSID.
     <li>"*DATFMT" - The date format from the locale is used.
     <li>"*DATSEP" - The date separator from the locale is used.
     <li>"*DECFMT" - The decimal format from the locale is used.
     <li>"*SRTSEQ" - The sort sequence from the locale is used.  The sort sequence from the locale overrides the user profile sort sequence.
     <li>"*TIMSEP" - The time separator from the locale is used.
     </ul>
     **/
    public void setLocaleJobAttributes(String[] localeJobAttributes) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (localeJobAttributes == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'localeJobAttributes' is null.");
            throw new NullPointerException("localeJobAttributes");
        }
        runCommand("SETJOBATR(" + setArrayToString(localeJobAttributes) + ")");
    }

    /**
     Sets the path name of the locale that is assigned to LANG environment variable for this user.
     @param localePathName The locale path name that is assigned to the user profile when a job is started.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The system value QLOCALE is used to determine the locale path name to be assigned for this user.
     <li>{@link #NONE User.NONE} - No locale path name is assigned for this user.
     <li>"*C" - The C locale path name is assigned for this user.
     <li>"*POSIX" - The POSIX locale path name is assigned for this user.
     <li>The path name of the locale to be assigned for this user.
     </ul>
     **/
    public void setLocalePathName(String localePathName) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (localePathName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'localePathName' is null.");
            throw new NullPointerException("localePathName");
        }
        if (localePathName.length() == 0 || localePathName.startsWith("*"))
        {
            runCommand("LOCALE(" + localePathName + ")");
        }
        else
        {
            runCommand("LOCALE('" + localePathName + "')");
        }
    }

    /**
     Sets whether the user profile password should be managed locally.
     @param localPasswordManagement true if the password will be managed on the local system, false otherwise.
     **/
    public void setLocalPasswordManagement(boolean localPasswordManagement) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        runCommand("LCLPWDMGT(" + (localPasswordManagement ? "*YES" : "*NO") + ")");
    }

    /**
     Sets the maximum amount of auxiliary storage (in kilobytes) assigned to store permanant objects owned by this user profile (1 kilobyte equals 1024 bytes).  If the maximum is exceeded, when an interactive user tries to create an object, an error message is displayed, and the object is not created.  If the maximum is exceeded when an object is created in a batch job, an error message is sent to the job log (depending on the logging level of the job), and the object is not created.
     <p>Storage is allocated in 4K increments.  Therefore, if you specify MAXSTG (9), the profile is allocated 12K of storage.
     <p>When planning maximum storage for user profiles, consider the following system actions:
     <ul>
     <li>A restore operation assigns the storage to the user doing the restore, and then transfers the object to the owner.  For a large restore, specify MAXSTG(*NOMAX).
     <li>The user profile that creates a journal receiver is assigned the required storage as the receiver size grows.  If new receivers are created using JRNRCV(*GEN), the storage continues to be assigned to the user profile that wons the active journal receiver.  If a very active journal receiver is owned, specify MAXSTG(*NOMAX).
     <li>User profiles that transfer created objects to their group profile must have adequate storage in the user profiles to contain created objects before the objects are transferred to the group profile.
     <li>The owner of the library is assigned the storage for the descriptions of objects which are stored in a library, even when the objects are owned by another user profile.  Examples of such objects are text and program references.
     </ul>
     @param maximumStorageAllowed The maximum amount of auxiliary storage (in kilobytes) assigned to store permanant objects owned by this user profile.  Possible values are:
     <ul>
     <li>-1 - As much storage as is required is assigned to this profile (*NOMAX).
     <li>The maximum amount of storage for the user, in kilobytes (1 kilobyte equals 1024 bytes).
     </ul>
     **/
    public void setMaximumStorageAllowed(int maximumStorageAllowed) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        runCommand("MAXSTG(" + (maximumStorageAllowed == -1 ? "*NOMAX" : Integer.toString(maximumStorageAllowed)) + ")");
    }

    /**
     Sets the maximum amount of auxiliary storage (in kilobytes) assigned to store permanant objects owned by this user profile (1 kilobyte equals 1024 bytes).  If the maximum is exceeded, when an interactive user tries to create an object, an error message is displayed, and the object is not created.  If the maximum is exceeded when an object is created in a batch job, an error message is sent to the job log (depending on the logging level of the job), and the object is not created.
     <p>Storage is allocated in 4K increments.  Therefore, if you specify MAXSTG (9), the profile is allocated 12K of storage.
     <p>When planning maximum storage for user profiles, consider the following system actions:
     <ul>
     <li>A restore operation assigns the storage to the user doing the restore, and then transfers the object to the owner.  For a large restore, specify MAXSTG(*NOMAX).
     <li>The user profile that creates a journal receiver is assigned the required storage as the receiver size grows.  If new receivers are created using JRNRCV(*GEN), the storage continues to be assigned to the user profile that wons the active journal receiver.  If a very active journal receiver is owned, specify MAXSTG(*NOMAX).
     <li>User profiles that transfer created objects to their group profile must have adequate storage in the user profiles to contain created objects before the objects are transferred to the group profile.
     <li>The owner of the library is assigned the storage for the descriptions of objects which are stored in a library, even when the objects are owned by another user profile.  Examples of such objects are text and program references.
     </ul>
     @param maximumStorageAllowed The maximum amount of auxiliary storage (in kilobytes) assigned to store permanant objects owned by this user profile.  Possible values are:
     <ul>
     <li>"*NOMAX" - As much storage as is required is assigned to this profile.
     <li>The maximum amount of storage for the user, in kilobytes (1 kilobyte equals 1024 bytes).
     </ul>
     **/
    public void setMaximumStorageAllowed(String maximumStorageAllowed) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (maximumStorageAllowed == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'maximumStorageAllowed' is null.");
            throw new NullPointerException("maximumStorageAllowed");
        }
        runCommand("MAXSTG(" + maximumStorageAllowed + ")");
    }

    /**
     Sets the message queue to which messages are sent.
     <p>Note:  The message queue is created, if it does not exist.  The user profile specified for the User profile (USRPRF) parameter is the owner of the message queue.
     <p>The caller must have *USE authority to the specified message queue.
     @param messageQueue The message queue to which messages are sent.  Possible values are:
     <ul>
     <li>"*USRPRF" - A message queue with the same name as that specified for the USRPRF parameter is used as the message queue for this user.  This message queue is located in the QUSERSYS library.
     <li>The fully qualified integrated file system path name of the message queue to be used with this profile.
     </ul>
     @see  QSYSObjectPathName
     **/
    public void setMessageQueue(String messageQueue) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (messageQueue == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageQueue' is null.");
            throw new NullPointerException("messageQueue");
        }
        if (messageQueue.length() == 0 || messageQueue.startsWith("*"))
        {
            runCommand("MSGQ(" + messageQueue + ")");
        }
        else
        {
            QSYSObjectPathName ifs = new QSYSObjectPathName(messageQueue, "MSGQ");
            runCommand("MSGQ(" + ifs.getLibraryName() + "/" + ifs.getObjectName() + ")");
        }
    }

    /**
     Sets how the messages are sent to the message queue for this user are to be delivered.
     @param messageQueueDeliveryMethod How the messages are sent to the message queue for this user are to be delivered.  Possible values are:
     <ul>
     <li>"*NOTIFY" - The job to which the message queue is assigned is notified when a message arrives on the message queue.  For interactive jobs at a work station, the audible alarm is sounded (if the alarm feature is set) and the Message Waiting light is turned on.  The delivery mode cannot be changed to *NOTIFY if the message queue is also being used by another job.
     <li>"*HOLD" - The messages are held in the message queue until they are requested by the user or program.
     <li>"*BREAK" - The job to which the message queue is assigned is interrupted when a message arrives at the message queue.  If the job is an interactive job, the autdible alarm is sounded (if the alarm feature is set).  The delivery mode cannot be changed to *BREAK if the message queue is also being used by another job.
     <li>"*DFT" - The default reply to the inquiry message is sent.  If no default reply is specified in the message description of the inquiry message, the system default reply, *N, is used.
     </ul>
     **/
    public void setMessageQueueDeliveryMethod(String messageQueueDeliveryMethod) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (messageQueueDeliveryMethod == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'messageQueueDeliveryMethod' is null.");
            throw new NullPointerException("messageQueueDeliveryMethod");
        }
        runCommand("DLVRY(" + messageQueueDeliveryMethod + ")");
    }

    /**
     Sets the lowest severity code that a message can have and still be delivered to a user in break or notify mode.  Messages arriving at the message queue whose severities are lower than the the severity code specified for this parameter do not interrupt the job or turn on the audible alarm or the message-waiting light; they are held in the queue until they are requested by using the Display Message (DSPMSG) command.  If *BREAK or *NOTIFY is specidied for the Delivery (DLVRY) parameter, and is in effect when a message arrives at the queue, the message is delivered if the severity code associated with the message is equal or greater then the value specified here.  Otherwiese, the message is held in the queue until it is requested.
     @param messageQueueSeverity A severity code ranging from 00 through 99.
     **/
    public void setMessageQueueSeverity(int messageQueueSeverity) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        runCommand("SEV(" + Integer.toString(messageQueueSeverity) + ")");
    }

    /**
     Sets the user profile name.  This does not change the name of the user profile on the server.  Instead, it changes the user profile to which this User object references.  This property cannot be changed if the object has established a connection to the server.
     @param  name  The user profile name.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     @see  #getName
     **/
    public void setName(String name) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting name: " + name);

        if (name == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'name' is null.");
            throw new NullPointerException("name");
        }
        if (name.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'name' is not valid: '" + name + "'");
            throw new ExtendedIllegalArgumentException("name (" + name + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        if (connected_)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'name' after connect.");
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            name_ = name.toUpperCase().trim();
        }
        else
        {
            String oldValue = name_;
            String newValue = name.toUpperCase().trim();
            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("name", oldValue, newValue);
            }
            name_ = newValue;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("name", oldValue, newValue);
            }
        }
    }

    /**
     Sets the object auditing value for the user.  This value only takes effect if the object auditing (OBJAUD) value for the object being accessed has the value *USRPRF.
     <p>Implementation note:  The method internally calls the Change User Auditing (CHGUSRAUD) command and not the Change User Profile (CHGUSRPRF) command.  The caller must have audit (*AUDIT) special authority.  Changes take effect the next time a job is started for this user.
     @param objectAuditingValue The object auditing value for the user.  Possible values are:
     <ul>
     <li>{@link #NONE User.NONE} - The auditing value for the object determines when auditing is performed.
     <li>"*CHANGE" - All changes accesses by this user on all objects with the *USRPRF audit value are logged.
     <li>"*ALL" - All change and read accesses by this use on all objects with the *USRPRF audit value are logged.
     </ul>
     **/
    public void setObjectAuditingValue(String objectAuditingValue) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (objectAuditingValue == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'objectAuditingValue' is null.");
            throw new NullPointerException("objectAuditingValue");
        }
        runCommandAud("OBJAUD(" + objectAuditingValue + ")");
    }

    /**
     Sets the output queue to be used by this user profile.  The output queue must already exist when this command is run.  The caller must have *USE authority to the specified output queue.
     @param outputQueue The output queue to be used by this user profile..  Possible values are:
     <ul>
     <li>"*WRKSTN" - The output queue assigned to the user's work station is used.
     <li>"*DEV" - The output queue associated with the printer specified for the Print device (PRTDEV) parameter is used.  The output queue has the same name as the printer.  (The pringer file DEV parameter is determined by the CRTPRTF, CHGPRTF, or the OVRPRTF command).
     <p>Note:  This assumes the defaults are specified for the Output queue (OUTQ) parameter for the printer file, job description, user profile, and workstation.
     <li>The fully qualified integrated file system path name of the output queue to be used by this user profile.
     </ul>
     @see QSYSObjectPathName
     **/
    public void setOutputQueue(String outputQueue) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (outputQueue == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'outputQueue' is null.");
            throw new NullPointerException("outputQueue");
        }
        if (outputQueue.length() == 0 || outputQueue.startsWith("*"))
        {
            runCommand("OUTQ(" + outputQueue + ")");
        }
        else
        {
            QSYSObjectPathName ifs = new QSYSObjectPathName(outputQueue, "OUTQ");
            runCommand("OUTQ(" + ifs.getLibraryName() + "/" + ifs.getObjectName() + ")");
        }
    }

    /**
     Sets the user profile that is to be the owner of objects created by this user.
     @param owner The user profile that is to be the owner of objects created by this user.  Possible values are:
     <ul>
     <li>"*USRPRF" - The user profile associated with the job is the owner of the object.
     <li>"*GRPPRF" - The group profile is made the owner of newly created objects and has all authority to the object.  The user profile associated with the job does not have any specific authority to the object.  If *GRPPRF is specified, a user profile name must be specified for the Group profile (GRPPTF) parameter, and the Group authority (GRPAUT) parameter cannot be specified.
     </ul>
     **/
    public void setOwner(String owner) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (owner == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'owner' is null.");
            throw new NullPointerException("owner");
        }
        runCommand("OWNER(" + owner + ")");
    }

    /**
     Sets the password expiration interval (in days).
     @param passwordExpirationInterval The number of days the user's password can remain active before it must be changed.  Possible values are:
     <ul>
     <li>0 - The system value QPWDEXPITV is used to determine the password expiration interval (*SYSVAL).
     <li>-1 - The password does not expire (*NOMAX).
     <li>1-366 - The number of days between the date when the password is changed and the date when the password expires.
     </ul>
     **/
    public void setPasswordExpirationInterval(int passwordExpirationInterval) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        runCommand("PWDEXPITV(" + Integer.toString(passwordExpirationInterval) + ")");
    }

    /**
     Sets the password expiration interval (in days).
     @param passwordExpirationInterval The number of days the user's password can remain active before it must be changed.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The system value QPWDEXPITV is used to determine the password expiration interval.
     <li>"*NOMAX" - The password does not expire.
     <li>1-366 - The number of days between the date when the password is changed and the date when the password expires.
     </ul>
     **/
    public void setPasswordExpirationInterval(String passwordExpirationInterval) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (passwordExpirationInterval == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'passwordExpirationInterval' is null.");
            throw new NullPointerException("passwordExpirationInterval");
        }
        runCommand("PWDEXPITV(" + passwordExpirationInterval + ")");
    }

    /**
     Sets whether the password for this user is set to expired.  If the password is set to expired, the use is required to change the password to sign on the system.  When the user attempts to sign on the system, the sign-on information display is shown and the user has the option to change this password.
     @param passwordSetExpire true if the password set to expired, false otherwise.
     **/
    public void setPasswordSetExpire(boolean passwordSetExpire) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        runCommand("PWDEXP(" + (passwordSetExpire ? "*YES" : "*NO") + ")");
    }

    /**
     Sets the default printer device for this user.  If the pringer file used to create printed output specifies to spool the data, the spooled file is placed on the device's output queue, which is named the same as the device.
     <p>Note:  This assumes the defaults are specified for the Output queue (OUTQ) parameter for the printer file, job description, user profile, and workstation.
     <p>The caller must have *USE authority to the specified print device.
     @param printDevice The default printer device for this user.  Possible values are:
     <ul>
     <li>"*WRKSTN" - The printer assigned to the user's work station is used.
     <li>"*SYSVAL" - The value specified in the system value QPRTDEV is used.
     <li>The name of a printer that is to be used to print the output for this user.
     </ul>
     **/
    public void setPrintDevice(String printDevice) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (printDevice == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'printDevice' is null.");
            throw new NullPointerException("printDevice");
        }
        runCommand("PRTDEV(" + printDevice + ")");
    }

    /**
     Sets the sort sequence table to be used for string comparisons for this profile.
     @param sortSequenceTable The sort sequence table to be used for string comparisons for this profile.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The system value QSRTSEQ is used.
     <li>"*HEX" - A sort sequence table is not used.  The hexadecimal values of the characters are used to determine the sort sequence.
     <li>"*LANGIDUNQ" - A unique-weight sort table is used
     <li>"*LANGIDSHR" - A shared-weight sort table is used.
     <li>The fully qualified integrated file system path name of the sort sequence table to be used with this profile.
     </ul>
     @see  QSYSObjectPathName
     **/
    public void setSortSequenceTable(String sortSequenceTable) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (sortSequenceTable == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'sortSequenceTable' is null.");
            throw new NullPointerException("sortSequenceTable");
        }
        if (sortSequenceTable.length() == 0 || sortSequenceTable.startsWith("*"))
        {
            runCommand("SRTSEQ(" + sortSequenceTable + ")");
        }
        else
        {
            QSYSObjectPathName ifs = new QSYSObjectPathName(sortSequenceTable, "FILE");
            runCommand("SRTSEQ(" + ifs.getLibraryName() + "/" + ifs.getObjectName() + ")");
        }
    }

    /**
     Sets the special authorities given to a user.  Special authorities are required to perform certain functions on the system.  Special authorities cannot be removed from many of the system-supplied user profiles, including QSECOFR and QSYS.
     <p>The following special authorities are usually given:
     <ul>
     <li>Save system (*SAVSYS) special authority to users who need to operate the system.
     <li>Input/output system configuration (*IOSYSCFG) special authority to users who need to change system I/O configurations.
     <li>Job control (*JOBCTL) special authority is given to the user. The user is given the authority to change, display, hold, release, cancel, and clear all jobs that are running on the system or that are on a job queue or output queue that has OPRCTL (*YES) specified.  The user also has the authority to load the system, to start writers, and to stop active subsystems.
     <li>Security administrator (*SECADM) special authority to users who need to create, change, or delete user profiles.
     <li>All object (*ALLOBJ) special authority to users who need to work with system resources.
     <li>Service (*SERVICE) special authority to users who need to perform service functions.
     <li>Spool control (*SPLCTL) special authority to users who need to perform all spool-related functions.
     <li>Audit (*AUDIT) special authority to users who need to perform auditing functions.
     </ul>
     <p>Restrictions:
     <ul>
     <li>The user profile creating or changing another user profile must have all of the special authorities being given.  All special authorities are needed to give all special authorities to another user profile.
     <li>A user must have *ALLOBJ and *SECADM special authorities to give a user *SECADM special authority when using the CHGUSRPRF command.
     <li>The user must have *ALLOBJ, *SECADM, and *AUDIT special authorities to give a user *AUDIT special authority when using the CHGUSRPRF command.
     </ul>
     @param specialAuthority The special authorities given to a user.  Possible values for the elements of this array are:
     <ul>
     <li>"*USRCLS" - Special authorities are granted to this user based on the value specified on User class (USRCLS) parameter.
     <li>"*NONE" - No special authorities are granted to this user.
      <li>{@link #SPECIAL_AUTHORITY_ALL_OBJECT User.SPECIAL_AUTHORITY_ALL_OBJECT} - All object authority is given to the user.  The user can access any system resource with or without private user authorizations.
     <li>{@link #SPECIAL_AUTHORITY_AUDIT User.SPECIAL_AUTHORITY_AUDIT} - Audit authority is granted to this user.  The user is given the authority to perform auditing functions.  Auditing functions include turning auditing on or off for the system and controlling the level of auditing on an object or user. 
     <li>{@link #SPECIAL_AUTHORITY_JOB_CONTROL User.SPECIAL_AUTHORITY_JOB_CONTROL} - Job control authority is given to the user.  The user has authority to change, display, hold, release, cancel, and clear all jobs that are running on the system or that are on a job queue or output queue that has OPRCTL (*YES) specified.  The user also has the authority to start writers and to stop active subsystems.
     <li>{@link #SPECIAL_AUTHORITY_SAVE_SYSTEM User.SPECIAL_AUTHORITY_SAVE_SYSTEM} - Save system authority is given to the user profile.  This user has the authority to save, restore, and free storage for all objects on the system, with or without object management authority. 
     <li>{@link #SPECIAL_AUTHORITY_IO_SYSTEM_CONFIGURATION User.SPECIAL_AUTHORITY_IO_SYSTEM_CONFIGURATION} - Input/output (I/O) system configuration authority is given to the user.  The user has authority to change system I/O configurations. 
     <li>{@link #SPECIAL_AUTHORITY_SECURITY_ADMINISTRATOR User.SPECIAL_AUTHORITY_SECURITY_ADMINISTRATOR} - Security administrator authority is given to the user.  The user can create, change, or delete user profiles if authorized to the Create User Profile (CRTUSRPRF), Change User Profile (CHGUSRPRF), or Delete User Profile (DLTUSRPRF) commands and is authorized to the user profile.  This authority does not allow giving special authorities that this user profile does not have.  To give *SECADM special authority to another user, a user must have both *ALLOBJ and *SECADM special authorities. 
     <li>{@link #SPECIAL_AUTHORITY_SERVICE User.SPECIAL_AUTHORITY_SERVICE} - Service authority is given to this user.  The user can perform service functions. 
     <li>{@link #SPECIAL_AUTHORITY_SPOOL_CONTROL User.SPECIAL_AUTHORITY_SPOOL_CONTROL} - Spool control authority is given to this user.  The user can perform all spool functions.
     </ul>
     **/
    public void setSpecialAuthority(String[] specialAuthority) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (specialAuthority == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'specialAuthority' is null.");
            throw new NullPointerException("specialAuthority");
        }
        runCommand("SPCAUT(" + setArrayToString(specialAuthority) + ")");
    }

    /**
     Sets the special environment in which the user operates after signing on.
     @param specialEnvironment The special environment in which the user operates after signing on.  Possible values are:
     <ul>
     <li>"*SYSVAL" - The system value, QSPCENV, is used to determine the system environment after the user signs on the system.
     <li>{@link #NONE User.NONE} - The user operates in the server operating system environment after signing on the system.
     <li>"*S36" - The user operates in the System/36 environment after signing on the system.
     </ul>
     **/
    public void setSpecialEnvironment(String specialEnvironment) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (specialEnvironment == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'specialEnvironment' is null.");
            throw new NullPointerException("specialEnvironment");
        }
        runCommand("SPCENV(" + specialEnvironment + ")");
    }

    /**
     Sets the status of the user profile.
     <p>The system will disable a user profile if the number of failed sign-on attempts reaches the limit specified on the QMAXSIGN system value and option 2 or 3 has been specified on the QMAXSGNACN system value.
     @param status The status of the user profile.  Possible values are:
     <ul>
     <li>"*ENABLED" - The user profile is valid for sign-on.
     <li>"*DISABLED" - The user profile is not valid for sign-on until an authorized user enables it again.  Batch jobs can be submitted under a disabled user profile.
     </ul>
     **/
    public void setStatus(String status) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (status == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'status' is null.");
            throw new NullPointerException("status");
        }
        runCommand("STATUS(" + status + ")");
    }

    /**
     Sets the user's supplemental group profiles.  The profiles specified here, along with the group profile specified for the Group profile (GRPPRF) parameter, are used to determine what authority the user has if no specific user authority is given for the job.  If profiles are specified for this parameter, a group profile name must be specified on the GRPPRF parameter for this user profile (either on this command or on a previous Create User Profile (CRTUSRPRF) or Change User Profile (CHGUSRPRF) command.  The current user of this command must have object management (*OBJMGT) and change (*CHANGE) authority to the profiles specified for this.  The required *OBJMGT authority cannot be given by a program adopt operation.
     <p>Notes:
     <ul>
     <li>When a group profile is specified, the user is automatically granted *CHANGE and *OBJMGT authority to the group profile.
     <li>The following IBM-supplied user profiles are not valid for this parameter:
     <p>QAUTPROF, QCLUMGT, QCLUSTER, QCOLSRV, QDBSHR, QDBSHRDO, QDFTOWN, QDIRSRV, QDLFM, QDOC, QDSNX, QEJB, QFNC, QGATE, QIPP, QLPAUTO, QLPINSTALL, QMGTC, QMSF, QNETSPLF, QNFSANON, QNTP, QPEX, QPM400, QRJE, QSNADS, QSPL, QSPLJOB, QSRV, QSRVAGT, QSRVBAS, QSYS, QTCM, QTCP, QTFTP, QTSTRQS, QYCMCIMOM, QYPSJSVR
     </ul>
     @param supplementalGroups The user's supplemental group profiles.  Possible values for the elements of this array are:
     <ul>
     <li>"*NONE" - No supplemental group profiles are used with this user profile.
     <li>The group profile names to be used with this user profile and the group profile specified on the GRPPRF parameter to determine a job's eligibility for getting access to existing objects and special authority.  A maximum of 15 group profile names may be specified.
     **/
    public void setSupplementalGroups(String[] supplementalGroups) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (supplementalGroups == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'supplementalGroups' is null.");
            throw new NullPointerException("supplementalGroups");
        }
        runCommand("SUPGRPPRF(" + setArrayToString(supplementalGroups) + ")");
    }

    /**
     Sets the system object representing the server on which the user profile exists.  This property cannot be changed if the object has established a connection to the server.
     @param  system  The system object representing the server on which the user profile exists.
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
        if (connected_)
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
     Sets the level of activity that is audited for this user profile.  Note:  The system values QAUDLVL and QAUDLVL2 are used in conjunction with this parameter.  Example:  If QAUDLVL is set to *DELETE and AUDLVL is set to *CREATE, then both *DELETE and *CREATE would be audited for this user.  The default value for the QAUDLVL and QAUDLVL2 system values is *NONE.
     <p>Implementation note:  The method internally calls the Change User Auditing (CHGUSRAUD) command and not the Change User Profile (CHGUSRPRF) command.  The caller must have audit (*AUDIT) special authority.  Changes take effect the next time a job is started for this user.
     @param userActionAuditLevel The level of activity that is audited for this user profile.  Possible values for the elements of this array are:
     <ul>
     <li>"*NONE" - No auditing level is specified.  The auditing level for this user is taken from system values QAUDLVL and QAUDLVL2.
     <li>"*CMD" - CL command strings, System/36 environment operator control commands, and System/36 enviromnent procedures are logged for this user.
     <li>"*CREATE" - Auditing entries are sent when objects are created by this user.
     <li>"*DELETE" - Auditing entries are sent when objects are deleteed by this user.
     <li>"*JOBDTA" - The following actions taken by this user that affect a job are audited:
     <ul>
     <li>Job start and stop data.
     <li>Hold, release, stop, continue, change, disconnect, end, end abnormal.
     <li>Program start request (PSR) is attached to a prestart job.
     </ul>
     <li>"*OBJMGT" - Object management changes made by this user, such as move or rename, are audited.
     <li>"*OFCSRV" -  Office services changes made by this user, such as changes to the system directory and use of OfficeVision for AS/400 mail, are audited.
     <li>"*OPTICAL" - The following optical functions are audited:
     <ul>
     <li>Add or remove optical cartridge.
     <li>Change the authorization list used to secure an optical volume.
     <li>Open optical file or directory.
     <li>Create or delete optical directory.
     <li>Change or retrieve optical directory attributes.
     <li>Copy, move, or rename optical file.
     <li>Copy optical directory.
     <li>Back up optical volume.
     <li>Initialize or rename optical volume.
     <li>Convert backup optical volume to a primary volume.
     <li>Save or release help optical file.
     <li>Absolute read of an optical volume.
     </ul>
     <li>"*PGMADP" - Authority obtained through program adoption is audited for this user.
     <li>"*SAVRST" - Save and restore actions performed by this user are audited.
     <li>"*SECURITY" - Security changes made by this user are audited.
     <li>"*SERVICE" - User of the system service tools by this user is audited.
     <li>"*SPLFDTA" - Spooled file operations made by this user are audited.
     <li>"*SYSMGT" - Use of system management functions by this user are audited.
     </ul>
     **/
    public void setUserActionAuditLevel(String[] userActionAuditLevel) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (userActionAuditLevel == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'userActionAuditLevel' is null.");
            throw new NullPointerException("userActionAuditLevel");
        }
        runCommandAud("AUDLVL(" + setArrayToString(userActionAuditLevel) + ")");
    }

    /**
     Sets the type of user associated with this user profile: security officer, security administrator, programmer, system operator, or user.  The user class controls the options that are shown on a menu.  Special authorities are given only if *USRCLS is specified for the Special authority (SPCAUT) parameter.  If SPCAUT(*USRCLS) is specified, the special authorities granted will differ depending on the QSECURITY value.
     @param userClassName The type of user associated with this user profile.  Possible values are:
     <ul>
     <li>"*USER" - At QSECURITY level 10 or 20, the user has *ALLOBJ and *SAVSYS authority.  At QSECURITY level 30 or above, the user has no special authorities.
     <li>"*SECOFR" - At all levels of security, the security officer is granted the following special authorities:
     <ul>
     <li>*ALLOBJ
     <li>*SAVSYS
     <li>*JOBCTL
     <li>*SERVICE
     <li>*SPLCTL
     <li>*SECADM
     <li>*AUDIT
     <li>*IOSYSCFG
     </ul>
     <li>"*SECADM" - At QSECURITY level 10 or 20, the security administrator has *ALLOBJ, *SAVSYS, *SECADM, and *JOBCTL special authorities.  At QSECURITY level 30 or above, the user has *SECADM special authority.
     <li>"*PGMR" - At QSECURITY level 10 or 20, the programmer has *ALLOBJ, *SAVSYS, and *JOBCTL special authorities.  At QSECURITY level 30 or above, the user has no special authorities.
     <li>"*SYSOPR" - At QSECURITY level 10 or 20, the system operator has *ALLOBJ, *SAVSYS, and *JOBCTL special authorities.  At QSECURITY level 30 or above, the user has *SAVSYS and *JOBCTL special authorities.
     </ul>
     **/
    public void setUserClassName(String userClassName) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (userClassName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'userClassName' is null.");
            throw new NullPointerException("userClassName");
        }
        runCommand("USRCLS(" + userClassName + ")");
    }

    /**
     Sets the user ID number (uid number) for this user profile.  The uid number is used to identify the user when the user is using the directory file system.  The uid number for a user cannot be changed if there are one or more active jobs for the user.
     @param userID The uid number to be assigned to the user profile.  A value from 1 to 4294967294 can be entered.  The uid number assigned must not already be assigned to another user profile.
     **/
    public void setUserID(long userID) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        runCommand("UID(" + Long.toString(userID) + ")");
    }

    /**
     Sets the level of help information detail to be shown and the function of the Page Up and Page Down keys by default.  The system shows several displays that are suitable for the inexperienced user.  More experienced users must perform an extra action to see detailed information.  When values are specified for this parameter, the system presents detailed information without further action by the experienced user.
     @param userOptions The level of help information detail to be shown and the function of the Page Up and Page Down keys by default.  Possible values include:
     <ul>
     <li>"*NONE" - Detailed information is not shown.
     <li>"*CLKWD" - Parameter keywords are shown instead of the possible parameter values when a control language (CL) command is prompted.
     <li>"*EXPERT" - More detailed information is shown when the user is performing display and edit options to define or change the system (such as edit or display object authority).
     <li>"*ROLLKEY" - The actions of the Page Up and Page Down keys are reversed.
     <li>"*NOSTSMSG" - Status messages are not displayed when sent to the user.
     <li>"*STSMSG" - Status messages are displayed when sent to the user.
     <li>"*HLPFULL" - Help text is shown on a full display rather than in a window.
     <li>"*PRTMSG" - A message is sent to this user's message queue when a spooled file for this user is printed or held by the printer writer.
     </ul>
     **/
    public void setUserOptions(String[] userOptions) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (userOptions == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'userOptions' is null.");
            throw new NullPointerException("userOptions");
        }
        runCommand("USROPT(" + setArrayToString(userOptions) + ")");
    }

    /**
     Sets the string representation of this User object.
     @return  The user profile name.
     **/
    public String toString()
    {
        return super.toString() + "[" + name_ + "]";
    }
}
