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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Calendar;



/**
The User class represents an OS/400 user profile and directory entry.
<P>
Note that calling any of the attribute getters for the first time will
result in an implicit call to {@link #loadUserInformation loadUserInformation()}.
If any exceptions are thrown by loadUserInformation() during the implicit call,
they will be logged to {@link com.ibm.as400.access.Trace#ERROR Trace.ERROR} and
ignored. However, should an exception occur during an explicit call to
loadUserInformation(), it will be thrown to the caller.
<P>
Implementation note:
This class internally calls the Retrieve User Information (QSYRUSRI) API.

@see com.ibm.as400.access.UserList
@see com.ibm.as400.access.UserGroup
@see com.ibm.as400.resource.RUser
@see com.ibm.as400.resource.RUserList
**/
public class User implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  static final long serialVersionUID = 5L;

    private AS400 system_;
    private int vrm_;
    private String name_;
    private String description_;
    private boolean hasMembers_;

    private boolean partiallyLoaded_ = false;
    private boolean loaded_ = false;

    private transient PropertyChangeSupport propertyChangeSupport_;
    private transient VetoableChangeSupport vetoableChangeSupport_;

    private static final ProgramParameter errorCode_ = new ProgramParameter(new byte[4]);

    private static final int VRM510 = AS400.generateVRM(5, 1, 0);


    private String userProfileName_;
    private Date previousSignon_;
    private int signonAttemptsNotValid_;
    private String status_;
    private byte[] passwordLastChangedBytes_;
    private Date passwordLastChanged_;
    private boolean noPasswordIndicator_;
    private int passwordExpirationInterval_;
    private byte[] datePasswordExpiresBytes_;
    private Date datePasswordExpires_;
    private int daysUntilPasswordExpires_;
    private boolean passwordSetToExpire_;
    private String userClass_;
    private String[] specialAuthorities_;
    private String groupProfile_;
    private String owner_;
    private String groupAuthority_;
    private String assistanceLevel_;
    private String currentLibrary_;
    private String initialMenu_;
    private String initialProgram_;
    private String limitCapabilities_;
    private String displaySignonInfo_;
    private String limitDeviceSessions_;
    private String keyboardBuffering_;
    private int maxAllowedStorage_;
    private int storageUsed_;
    private int highestSchedulingPriority_;
    private String jobDescription_;
    private String accountingCode_;
    private String messageQueue_;
    private String messageQueueDeliveryMethod_;
    private int messageQueueSeverity_;
    private String outputQueue_;
    private String printDevice_;
    private String specialEnvironment_;
    private String attentionKeyHandlingProgram_;
    private String languageID_;
    private String countryID_;
    private int ccsid_;
    private String[] userOptions_;
    private String sortSequenceTable_;
    private String objectAuditingValue_;
    private String[] userActionAuditLevel_;
    private String groupAuthorityType_;
    private long userID_;
    private long groupID_;
    private String[] localeJobAttributes_;
    private boolean digitalCertificateIndicator_;
    private String characterIdentifierControl_;
    private String[] supplementalGroups_;
    private String homeDirectory_;
    private String localePathName_;
    private String[] iaspNames_;
    private int[] iaspMaxAllowed_;
    private int[] iaspStorageUsed_;


/**
Constructs a User object.
**/
    public User()
    {
    }



/**
Constructs a User object.
Note that this constructor no longer throws any of the declared exceptions, but
they remain for compatibility.
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
      if (system == null) throw new NullPointerException("system");
      if (name == null) throw new NullPointerException("name");
      system_ = system;
      name_ = name;
    }


    // Called by UserList.getUsers().
    User(AS400 system, String name, boolean hasMembers, String description)
    {
      system_ = system;
      name_ = name;
      hasMembers_ = hasMembers;
      description_ = description;
      partiallyLoaded_ = true;
    }


/**
Adds a PropertyChangeListener.  The specified PropertyChangeListener's
{@link java.beans.PropertyChangeListener#propertyChange propertyChange()}
method will be called each time the value of any bound property is changed.

@param listener The listener.
@see #removePropertyChangeListener
*/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
      if (listener == null) throw new NullPointerException("listener");
      if (propertyChangeSupport_ == null) propertyChangeSupport_ = new PropertyChangeSupport(this);
      propertyChangeSupport_.addPropertyChangeListener(listener);
    }



/**
Adds a VetoableChangeListener.  The specified VetoableChangeListener's
{@link java.beans.VetoableChangeListener#vetoableChange vetoableChange()}
method will be called each time the value of any constrained property is changed.

@param listener The listener.
@see #removeVetoableChangeListener
*/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
      if (listener == null) throw new NullPointerException("listener");
      if (vetoableChangeSupport_ == null) vetoableChangeSupport_ = new VetoableChangeSupport(this);
      vetoableChangeSupport_.addVetoableChangeListener(listener);
    }



/**
Returns the accounting code that is associated with this user.

@return The accounting code that is associated with this user.
**/
    public String getAccountingCode()
    {
      if (!loaded_) refresh();
      return accountingCode_;
    }


/**
Returns the user interface that the user will use.

@return The user interface that the user will use.  Possible values are:
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
Returns the fully qualified integrated file system path name of the
attention key handling program for this user.

@return The fully qualified integrated file system path name of the
        attention key handling program for this user. Possible values are:
        <ul>
        <li>"*SYSVAL" - The system value QATNPGM determines the user's attention key handling program.
        <li>"*NONE" - No attention key handling program is used.
        <li>"*ASSIST" - The Operational Assistant attention key handling program.
        <li>The attention key handling program name.
        </ul>
@see QSYSObjectPathName
**/
    public String getAttentionKeyHandlingProgram()
    {
      if (!loaded_) refresh();
      return attentionKeyHandlingProgram_;
    }



/**
Returns the character code set ID to be used by the system for this user.
The special value of -2 indicates that the system value QCCSID is used
to determine the user's CCSID.

@return The character code set ID to be used by the system for this user.
**/
    public int getCCSID()
    {
      if (!loaded_) refresh();
      return ccsid_;
    }


    /**
     * Returns the character identifier control for the user.
     * Possible values iare:
     * <UL>
     * <LI>"*SYSVAL" - The system value QCHRIDCTL will be used to determine the CHRID control for this user.
     * <LI>"*DEVD" - The *DEVD special value performs the same function as on the CHRID command
     * parameter for display files, printer files, and panel groups.
     * <LI>"*JOBCCSID" - The *JOBCCSID special value performs the same function as on the CHRID command
     * parameter for display files, printer files, and panel groups.
     * <LI>A character identifier control.
     * </UL>
     * @return The character identifier control for this user.
    **/
    public String getCHRIDControl()
    {
      if (!loaded_) refresh();
      return characterIdentifierControl_;
    }

/**
Returns the country ID used by the system for this user.

@return The country ID used by the system for this user.  Possible values are:
        <ul>
        <li>"*SYSVAL" - The system value QCNTRYID will be used to determine the country ID.
        <li>A country ID.
        </ul>
**/
    public String getCountryID()
    {
      if (!loaded_) refresh();
      return countryID_;
    }



/**
Returns the name of the user's current library.

@return The name of the user's current library.  Possible values are:
        <ul>
        <li>"*CRTDFT" - The user does not have a current library.
        <li>A library name.
        </ul>
**/
    public String getCurrentLibraryName()
    {
      if (!loaded_) refresh();
      return currentLibrary_;
    }



/**
Returns the number of days until the password will expire.

@return The number of days until the password will expire.  Possible values are:
        <ul>
        <li>0 - The password is expired.
        <li>1-7 - The number of days until the password expires.
        <li>-1 - The password will not expire in the next 7 days.
        </ul>
**/
    public int getDaysUntilPasswordExpire()
    {
      if (!loaded_) refresh();
      return daysUntilPasswordExpires_;
    }



/**
Returns the descriptive text for the user profile.
This value is pre-loaded into any User objects generated off of a
UserList object so that a call to the server is not required to
retrieve this value. In the event that this User object was not
constructed by a UserList, the description will need to be 
retrieved from the system via an implicit call to loadUserInformation().
@return The descriptive text for the user profile.
**/
    public String getDescription()
    {
      if (!loaded_ && !partiallyLoaded_) refresh();
      return description_;
    }



/**
Returns whether the sign-on information display is shown when
the user signs on.

@return Whether the sign-on information display is shown when
        the user signs on.  Possible values are:
        <ul>
        <li>"*SYSVAL" - The system value QDSPSGNINF determines if the sign-on information display
            is shown when the user signs on.
        <li>"*YES" - The sign-on information display is shown when the user signs on.
        <li>"*NO" - The sign-on information display is not shown when the user signs on.
        </ul>
**/
    public String getDisplaySignOnInformation()
    {
      if (!loaded_) refresh();
      return displaySignonInfo_;
    }



/**
Returns the authority the user's group profile
has to objects the user creates.

@return The authority the user's group profile
        has to objects the user creates.  Possible values are:
        <ul>
        <li>"*NONE" - The group profile has no authority to the objects the user creates,
            or the user does not have a group profile.
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
Returns the type of authority the user's group has to
objects the user creates.

@return The type of authority the user's group has to
        objects the user creates.  Possible values are:
        <ul>
        <li>"*PRIVATE" - The group profile has a private authority to the objects the user creates,
            or the user does not have a group profile.
        <li>"*PGP" - The group profile will be the primary group for objects the user creates.
        </ul>
**/
    public String getGroupAuthorityType()
    {
      if (!loaded_) refresh();
      return groupAuthorityType_;
    }



    
    
/**
Returns the group ID number for the user profile.
The group ID number is used to identify the user when it is a group and a
member of the group is using the integrated file system.

@return The group ID number for the user profile.  This will
        be 0 if the user does not have a group ID.
**/
    public long getGroupID()
    {
      if (!loaded_) refresh();
      return groupID_;
    }



/**
Returns the group ID number for the user profile.
The group ID number is used to identify the user when it is a group and a
member of the group is using the integrated file system.

@return The group ID number for the user profile.  This will
        be 0 if the user does not have a group ID.
@deprecated This method has been replaced by {@link #getGroupID getGroupID()} which 
returns a long.
**/
    public int getGroupIDNumber()
    {
        // This cast is necessary because the original code
        // did not account for a group ID number (which is
        // an unsigned int being bigger than Integer.MAX_VALUE.
      return (int)getGroupID();
    }



/**
Returns the name of the group profile.

@return The name of the group profile.  Possible values are:
        <ul>
        <li>"*NONE" - If the user does not have a group profile.
        <li>The group profile name.
        </ul>
**/
    public String getGroupProfileName()
    {
      if (!loaded_) refresh();
      return groupProfile_;
    }



/**
Returns the highest scheduling priority the user is allowed
to have for each job submitted to the system.
<p>
Note this method no longer throws a NumberFormatException, but
the exception declaration remains for compatibility.
@return The highest scheduling priority the user is allowed
        to have for each job submitted to the system.
        The priority is a value from 0 to 9, with 0 being the
        highest priority.
**/
    public int getHighestSchedulingPriority()
           throws NumberFormatException
    {
      if (!loaded_) refresh();
      return highestSchedulingPriority_;
    }



/**
Returns the home directory for this user profile.

@return The home directory for this user profile.
**/
    public String getHomeDirectory()
    {
      if (!loaded_) refresh();
      return homeDirectory_;
    }


    /**
     * Returns a list of independent auxiliary storage pool (IASP) names
     * in use by this user.
     * @return If the server is V5R1 and higher, returns the array of IASP names; null otherwise.
     * @see #getIASPStorageAllowed
     * @see #getIASPStorageUsed
     * @see com.ibm.as400.access.AS400#getVRM
     * @see com.ibm.as400.access.AS400#generateVRM
    **/
    public String[] getIASPNames()
    {
      if (!loaded_) refresh();
      return iaspNames_;
    }

    /**
     * Returns the maximum storage this user is allowed to use for the given IASP.
     * @return If the server is V5R1 and higher, returns the maximum allowed storage
     * in kilobytes or -1 for *NOMAX; returns -2 if the server is pre-V5R1 or if the
     * given IASP name is not listed for this user.
     * @see #getIASPNames
     * @see #getIASPStorageUsed
    **/
    public int getIASPStorageAllowed(String iaspName)
    {
      if (iaspName == null) throw new NullPointerException("iaspName");
      if (!loaded_) refresh();
      if (iaspMaxAllowed_ == null || iaspNames_ == null) return -2;
      for (int i=0; i<iaspNames_.length; ++i)
      {
        if (iaspNames_[i].equals(iaspName))
        {
          return iaspMaxAllowed_[i];
        }
      }
      return -2;
    }

    /**
     * Returns the amount of storage taken by this user's owned objects for the given IASP.
     * @return If the server is V5R1 and higher, returns the storage used
     * in kilobytes; returns -2 if the server is pre-V5R1 or if the
     * given IASP name is not listed for this user.
     * @see #getIASPNames
     * @see #getIASPStorageAllowed
    **/
    public int getIASPStorageUsed(String iaspName)
    {
      if (iaspName == null) throw new NullPointerException("iaspName");
      if (!loaded_) refresh();
      if (iaspStorageUsed_ == null || iaspNames_ == null) return -2;
      for (int i=0; i<iaspNames_.length; ++i)
      {
        if (iaspNames_[i].equals(iaspName))
        {
          return iaspStorageUsed_[i];
        }
      }
      return -2;
    }


/**
Returns the fully qualified integrated file system path name
of the initial menu for the user.

@return The fully qualified integrated file system path name
        of the initial menu for the user.   Possible values are:
        <ul>
        <li>"*SIGNOFF"
        <li>The initial menu name.
        </ul>
@see QSYSObjectPathName
**/
    public String getInitialMenu()
    {
      if (!loaded_) refresh();
      return initialMenu_;
    }



/**
Returns the fully qualified integrated file system path name
of the initial program for the user.

@return The fully qualified integrated file system path name
        of the initial program for the user.
        Possible values are:
        <UL>
        <LI>"*NONE" - If the user does not have an initial program.
        <LI>The initial program name.
        </UL>
@see QSYSObjectPathName
**/
    public String getInitialProgram()
    {
      if (!loaded_) refresh();
      return initialProgram_;
    }



/**
Returns the fully qualified integrated file system path name
of the job description used for jobs that start through
subsystem work station entries.

@return The fully qualified integrated file system path name
        of the job description used for jobs that start through
        subsystem work station entries.
@see QSYSObjectPathName
**/
    public String getJobDescription()
    {
      if (!loaded_) refresh();
      return jobDescription_;
    }


    /**
     * Returns the keyboard buffering value that is used when a job
     * is initialized for this user. Possible values are:
     * <UL>
     * <LI>"*SYSVAL" - The system value QKBDBUF determines the keyboard buffering value for this user.
     * <LI>"*YES" - The type-ahead and attention-key buffering options are both on.
     * <LI>"*NO" - The type-ahead and attention-key buffering options are not on.
     * <LI>"*TYPEAHEAD" - The type-ahead option is on, but the attention-key buffering option is not.
     * </UL>
     * @return The keyboard buffering value.
    **/
    public String getKeyboardBuffering()
    {
      if (!loaded_) refresh();
      return keyboardBuffering_;
    }

/**
Returns the language ID used by the system for this user.

@return The language ID used by the system for this user.
        Possible values are:
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
Indicates whether the user has limited capabilites.

@return Whether the user has limited capabilites.  Possible values are:
        <ul>
        <li>"*PARTIAL" - The user cannot change the initial program or current library.
        <li>"*YES" - The user cannot change the initial menu, initial program,
            or current library.  The user cannot run commands from the
            command line.
        <li>"*NO" - The user is not limited.
        </ul>
**/
    public String getLimitCapabilities()
    {
      if (!loaded_) refresh();
      return limitCapabilities_;
    }



/**
Indicates whether the user is limited to one device session.

@return Whether the user is limited to one device session.  Possible values are:
        <ul>
        <li>"*SYSVAL" - The system value QLMTDEVSSN determines if the user is limited to one
            device session.
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
Returns a list of attributes which are set from the locale path
name at the time a job is started for this user.

@return A list of attributes which are set from the locale path
        name at the time a job is started for this user.
        Possible values for the elements of this array are:
        <ul>
        <li>"*NONE" - No job attributes are used from the locale path name at the time a job is
            started for this user profile.
        <li>"*SYSVAL" - The job attributes assigned from the locale path name are determined by
            the system value QSETJOBATR at the time a job is started for this user profile.
        <li>"*CCSID" - The coded character set identifier is set from the locale path name
            at the time a job is started for this user profile.
        <li>"*DATFMT" - The date format is set from the locale path name
            at the time a job is started for this user profile.
        <li>"*DATSEP" - The date separator is set from the locale path name
            at the time a job is started for this user profile.
        <li>"*SRTSEQ" - The sort sequence is set from the locale path name
            at the time a job is started for this user profile.
        <li>"*TIMSEP" - The time separator is set from the locale path name
            at the time a job is started for this user profile.
        <li>"*DECFMT" - The decimal format is set from the locale path name
            at the time a job is started for this user profile.
        </ul>
**/
    public String[] getLocaleJobAttributes()
    {
      if (!loaded_) refresh();
      return localeJobAttributes_;
    }

    private static final String[] LOCALE_ATTRIBUTES = new String[]
    {
      "*NONE",
      "*SYSVAL",
      "*CCSID",
      "*DATFMT",
      "*DATSEP",
      "*SRTSEQ",
      "*TIMSEP",
      "*DECFMT"
    };


/**
Returns the locale path name that is assigned to the
user profile when a job is started.

@return The locale path name that is assigned to the
        user profile when a job is started.
        Possible values are:
        <ul>
        <li>"*SYSVAL" - The QLOCALE system value is used to determine the locale path name.
        <li>"*NONE" - No locale path name is assigned.
        <li>"*C" - The C locale path name is assigned.
        <li>"*POSIX" - The POSIX locale path name is assigned.
        <li>A locale path name.
        </ul>
**/
    public String getLocalePathName()
    {
      if (!loaded_) refresh();
      return localePathName_;
    }



/**
Returns the maximum amount of auxiliary storage (in kilobytes) that can be
assigned to store permanant objects owned by the user.

@return The maximum amount of auxiliary storage (in kilobytes) that can be
        assigned to store permanant objects owned by the user.
        If the user does not have a maximum amount of allowed storage, this will
        be -1.
**/
    public int getMaximumStorageAllowed()
    {
      if (!loaded_) refresh();
      return maxAllowedStorage_;
    }



/**
Returns the fully qualified integrated file system path name of the
message queue that is used by this user.

@return The fully qualified integrated file system path name of the
        message queue that is used by this user.
@see QSYSObjectPathName
**/
    public String getMessageQueue()
    {
      if (!loaded_) refresh();
      return messageQueue_;
    }



/**
Returns how the messages are delivered to the message queue
used by the user.

@return How the messages are delivered to the message queue
        used by the user.  Possible values are:
        <ul>
        <li>"*BREAK" - The job to which the message queue is assigned is interrupted when a message
            arrives on the message queue.
        <li>"*DFT" - Messages requiring replies are answered with their default reply.
        <li>"*HOLD" - The messages are held in the message queue until they are requested by the
            user or program.
        <li>"*NOTIFY" - The job to which the message queue is assigned is notified when a message arrives
            on the message queue.
        </ul>
**/
    public String getMessageQueueDeliveryMethod()
    {
      if (!loaded_) refresh();
      return messageQueueDeliveryMethod_;
    }



/**
Returns the lowest severity that a message can have and still be delivered to
a user in break or notify mode.

@return The lowest severity that a message can have and still be delivered to
        a user in break or notify mode, in the range 0 through 99.
**/
    public int getMessageQueueSeverity()
    {
      if (!loaded_) refresh();
      return messageQueueSeverity_;
    }



/**
Returns the user profile name.

@return The user profile name.
@see #setName
**/
    public String getName()
    {
      return name_;  
    }



/**
Returns the user's object auditing value.

@return The user's object auditing value.  Possible values are:
        <ul>
        <li>"*NONE" - No additional object auditing is done for the user.
        <li>"*CHANGE" - Object changes are audited for the user if the object's auditing
            value is *USRPRF.
        <li>"*ALL" - Object read and change operations are audited for the user
            if the object's auditing value is *USRPRF.
        </ul>
**/
    public String getObjectAuditingValue()
    {
      if (!loaded_) refresh();
      return objectAuditingValue_;
    }



/**
Returns the fully qualified integrated file system path name of the output queue
that is used by this user.

@return The fully qualified integrated file system path name of the output queue
        that is used by this user. Possible values are:
        <ul>
        <li>"*WRKSTN" - The output queue assigned to the user's work station is used.
        <li>"*DEV" - An output queue with the same name as the device specified
            in the printer device parameter is used.
        <li>The output queue name.
        </ul>
@see QSYSObjectPathName
**/
    public String getOutputQueue()
    {
      if (!loaded_) refresh();
      return outputQueue_;
    }



/**
Indicates who is to own objects created by this user.

@return Who is to own objects created by this user. Possible values are:
        <ul>
        <li>"*USRPRF" - The user owns any objects the user creates.  If the user does not
            have a group profile, the field contains this value.
        <li>"*GRPPRF" - The user's group profile owns any objects the user creates.
        </ul>
**/
    public String getOwner()
    {
      if (!loaded_) refresh();
      return owner_;
    }



/**
Returns the date the user's password expires.

@return The date the user's password expires.
**/
    public Date getPasswordExpireDate()
    {
      if (!loaded_) refresh();
      if (datePasswordExpires_ == null)
      {
        try
        {
          DateTimeConverter dtc = new DateTimeConverter(system_);
          datePasswordExpires_ = dtc.convert(datePasswordExpiresBytes_, "*DTS");
        }
        catch(Exception e)
        {
          if (Trace.traceOn_)
          {
            Trace.log(Trace.ERROR, "Exception while converting datePasswordExpires:", e);
          }
        }
      }
      return datePasswordExpires_;
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
**/
    public int getPasswordExpirationInterval()
    {
      if (!loaded_) refresh();
      return passwordExpirationInterval_;
    }



/**
Returns the date the user's password was last changed.

@return The date the user's password was last changed.
**/
    public Date getPasswordLastChangedDate()
    {
      if (!loaded_) refresh();
      if (passwordLastChanged_ == null)
      {
        try
        {
          DateTimeConverter dtc = new DateTimeConverter(system_);
          passwordLastChanged_ = dtc.convert(passwordLastChangedBytes_, "*DTS");
        }
        catch(Exception e)
        {
          if (Trace.traceOn_)
          {
            Trace.log(Trace.ERROR, "Exception while converting passwordLastChangedDate:", e);
          }
        }
      }
      return passwordLastChanged_;
    }



/**
Returns the date and time the user last signed on.

@return The date and time the user last signed on, or null if it is not known.
**/
    public Date getPreviousSignedOnDate()
    {
      if (!loaded_) refresh();
      return previousSignon_;
    }



/**
Returns the printer used to print for this user.

@return The printer used to print for this user. Possible values are:
        <ul>
        <li>"*WRKSTN" - The printer assigned to the user's work station is used.
        <li>"*SYSVAL" - The default system printer specified in the system value QPRTDEV
            is used.
        <li>The print device.
        </ul>
**/
    public String getPrintDevice()
    {
      if (!loaded_) refresh();
      return printDevice_;
    }



/**
Returns the number of sign-on attempts that were not valid
since the last successful sign-on.

@return The number of sign-on attempts that were not valid
        since the last successful sign-on.
**/
    public int getSignedOnAttemptsNotValid()
    {
      if (!loaded_) refresh();
      return signonAttemptsNotValid_;
    }



/**
Returns the fully integrated file system path name of the sort sequence
table used for string comparisons.

@return The fully integrated file system path name of the sort sequence
        table used for string comparisons.   Possible values are:
        <ul>
        <li>"*HEX" - The hexadecimal values of the characters are used to determine the
            sort sequence.
        <li>"*LANGIDUNQ" - A unique-weight sort table associated with the language specified.
        <li>"*LANGIDSHR" - A shared-weight sort table associated with the language specified.
        <li>"*SYSVAL" - The system value QSRTSEQ.
        <li>The sort sequence table name.
        </ul>
@see QSYSObjectPathName
**/
    public String getSortSequenceTable()
    {
      if (!loaded_) refresh();
      return sortSequenceTable_;
    }



/**
Returns a list of special authorities that the user has.

@return A list of special authorities that the user has.
        Possible values for the elements of this array are:
        <ul>
        <li>"*ALLOBJ" - All object.
        <li>"*SECADM" - Security administrator.
        <li>"*JOBCTL" - Job control.
        <li>"*SPLCTL" - Spool control.
        <li>"*SAVSYS" - Save system.
        <li>"*SERVICE" - Service.
        <li>"*AUDIT" - Audit.
        <li>"*IOSYSCFG" - Input/output system configuration.
        </ul>
**/
    public String[] getSpecialAuthority()
    {
      if (!loaded_) refresh();
      return specialAuthorities_;
    }
    
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
Returns the special environment the user operates in after signing on.

@return The special environment the user operates in after signing on.
        Possible values are:
        <ul>
        <li>"*SYSVAL" - The system value QSPCENV is used to determine the user's special
            environment.
        <li>"*NONE" - The user operates in the OS/400 environment.
        <li>"*S36" - The user operates in the System/36 environment.
        </ul>
**/
    public String getSpecialEnvironment()
    {
      if (!loaded_) refresh();
      return specialEnvironment_;
    }



/**
Returns the status of the user profile.

@return The status of the user profile.
        Possible values are:
        <ul>
        <li>"*ENABLED" - The user profile is enabled.
        <li>"*DISABLED" - The user profile is not enabled.
        </ul>
**/
    public String getStatus()
    {
      if (!loaded_) refresh();
      return status_;
    }



/**
Returns the amount of auxiliary storage (in kilobytes) occupied
by this user's owned objects.

@return The amount of auxiliary storage (in kilobytes) occupied
        by this user's owned objects.
**/
    public int getStorageUsed()
    {
      if (!loaded_) refresh();
      return storageUsed_;
    }



/**
Returns the supplemental groups for the user profile.

@return The array of supplemental groups for the user profile, or
a String array of length 0 if there are no supplemental groups.
**/
    public String[] getSupplementalGroups()
    {
      if (!loaded_) refresh();
      return supplementalGroups_;
    }



/**
Returns the number of supplemental groups for the user profile.
<P>
This method simply returns getSupplementalGroups().length.

@return The number of supplemental groups for the user profile.
@see #getSupplementalGroups
**/
    public int getSupplementalGroupsNumber()
    {
      if (!loaded_) refresh();
      return supplementalGroups_ == null ? 0 : supplementalGroups_.length;
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
Returns a list of action audit levels for the user.

@return A list of action audit levels for the user.
        Possible values for the elements of this array are:
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
      "*OPTICAL"
        // The API shows optical in the middle of the list,
        // but experimental data proves otherwise (at least
        // on V4R3).
    };




/**
Returns the user class name.

@return The user class name.   Possible values are:
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
      return userClass_;
    }



/**
Returns the user ID number for the user profile. This is used
to identify the user when using the integrated file system.

@return The user ID number for the user profile.
**/
    public long getUserID()
    {
      if (!loaded_) refresh();
      return userID_;
    }


/**
Returns the user ID number for the user profile. This is used
to identify the user when using the integrated file system.

@return The user ID number for the user profile.
@deprecated This method has been replaced by {@link #getUserID getUserID()} which 
returns a long.
**/
    public int getUserIDNumber()
    {
      return (int)getUserID();
    }


/**
     * Returns a list of options for users to customize their environment.
     * Possible values include:
     * <UL>
     * <LI>"*CLKWD" - The keywords are shown when a CL command is displayed.
     * <LI>"*EXPERT" - More detailed information is shown when the user is defining or
     * changing the system using edit or display object authority. This is independent of the ASTLVL 
     * parameter that is available on the user profile and other commands.
     * <LI>"*HLPFULL" - UIM online help is to be displayed full screen instead of in a window.
     * <LI>"*STSMSG" - Status messages sent to the user are shown.
     * <LI>"*NOSTSMSG" - Status messages sent to the user are not shown.
     * <LI>"*ROLLKEY" - The opposite action from the system default for roll keys is taken.
     * <LI>"*PRTMSG" - A message is sent to the user when a spooled file is printed.
     * </UL>
     * @return The user options that are set.
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
Returns the name of the user profile on the system.
Note this is the name that is returned by the system, not the name that
was set into this User object by the constructor or a call to setUser().
@return The name of the user profile.
**/
    public String getUserProfileName()
    {
      if (!loaded_) refresh();
      return userProfileName_;
    }


/**
Indicates whether this user is a group that has members.
<p>
For User objects, this should always return false.
For UserGroup objects, this should return true if the group profile
has members.
@return true if the user is a group that has members, false otherwise.
@see com.ibm.as400.access.UserGroup
**/
    public boolean isGroupHasMember()
    {
      if (!loaded_ && !partiallyLoaded_) refresh();
      return hasMembers_;
    }



/**
Indicates whether there is no password.

@return true if there is no password, false otherwise.
**/
    public boolean isNoPassword()
    {
      if (!loaded_) refresh();
      return noPasswordIndicator_;
    }



/**
Indicates whether the user's password is set to expire,
requiring the user to change the password when signing on.

@return true if the password set to expire, false otherwise.
**/
    public boolean isPasswordSetExpire()
    {
      if (!loaded_) refresh();
      return passwordSetToExpire_;
    }



/**
Indicates whether there are digital certificates associated with this user.

@return true if there are digital certificates associated with this user,
        false otherwise.
**/
    public boolean isWithDigitalCertificates()
    {
      if (!loaded_) refresh();
      return digitalCertificateIndicator_;
    }



/**
Refreshes the values for all attributes.

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
      if (system_ == null) throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
      if (name_ == null) throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);

      final int ccsid = system_.getCcsid();
      ConvTable conv = ConvTable.getTable(ccsid, null);
      AS400Text text10 = new AS400Text(10, ccsid);

      ProgramParameter[] parms = new ProgramParameter[5];
      int len = 1024;
      parms[0] = new ProgramParameter(len); // receiver variable
      parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(len)); // length of receiver variable
      parms[2] = new ProgramParameter(conv.stringToByteArray("USRI0300")); // format name
      parms[3] = new ProgramParameter(text10.toBytes(name_.toUpperCase().trim())); // user profile name
      parms[4] = errorCode_;

      ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QSYRUSRI.PGM", parms);
      if (!pc.run())
      {
        throw new AS400Exception(pc.getMessageList());
      }

      byte[] data = parms[0].getOutputData();

      int bytesReturned = BinaryConverter.byteArrayToInt(data, 0);
      int bytesAvailable = BinaryConverter.byteArrayToInt(data, 4);
      if (bytesReturned < bytesAvailable)
      {
        if (Trace.traceOn_)
        {
          Trace.log(Trace.DIAGNOSTIC, "User: not enough bytes, trying again. Bytes returned = "+bytesReturned+"; bytes available = "+bytesAvailable);
        }
        len = bytesAvailable;
        try
        {
          parms[0].setOutputDataLength(len);
          parms[1].setInputData(BinaryConverter.intToByteArray(len));
        }
        catch(PropertyVetoException pve) {}
        if (!pc.run())
        {
          throw new AS400Exception(pc.getMessageList());
        }
        data = parms[0].getOutputData();
      }

      userProfileName_ = conv.byteArrayToString(data, 8, 10).trim();
      
      String previousSignon = conv.byteArrayToString(data, 18, 13); // CYYMMDDHHMMSS
      if (previousSignon.trim().length() > 0)
      {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.YEAR, 1900 + Integer.parseInt(previousSignon.substring(0,3)));
        cal.set(Calendar.MONTH, Integer.parseInt(previousSignon.substring(3,5))-1);
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(previousSignon.substring(5,7)));
        cal.set(Calendar.HOUR, Integer.parseInt(previousSignon.substring(7,9)));
        cal.set(Calendar.MINUTE, Integer.parseInt(previousSignon.substring(9,11)));
        cal.set(Calendar.SECOND, Integer.parseInt(previousSignon.substring(11,13)));
        previousSignon_ = cal.getTime();
      }
      else
      {
        previousSignon_ = null;
      }

      signonAttemptsNotValid_ = BinaryConverter.byteArrayToInt(data, 32);
      status_ = conv.byteArrayToString(data, 36, 10).trim();
      
      if (passwordLastChangedBytes_ == null) passwordLastChangedBytes_ = new byte[8];
      System.arraycopy(data, 46, passwordLastChangedBytes_, 0, 8); // *DTS format - convert on getter
      passwordLastChanged_ = null; // reset

      noPasswordIndicator_ = data[54] == (byte)0xE8; // 'Y' for no password
      passwordExpirationInterval_ = BinaryConverter.byteArrayToInt(data, 56); // 1-366. 0 means use system value QPWDEXPITV. -1 means *NOMAX.
      
      if (datePasswordExpiresBytes_ == null) datePasswordExpiresBytes_ = new byte[8];
      System.arraycopy(data, 60, datePasswordExpiresBytes_, 0, 8); // *DTS format
      datePasswordExpires_ = null; // reset

      daysUntilPasswordExpires_ = BinaryConverter.byteArrayToInt(data, 68);
      passwordSetToExpire_ = data[72] == (byte)0xE8; // 'Y' if the user's password is set to expire
      userClass_ = conv.byteArrayToString(data, 73, 10).trim();
      int numSpecAuth = 0;
      for (int i=0; i<8; ++i)
      {
        if (data[83+i] == (byte)0xE8) // 'Y' is EBCDIC 0xE8
        {
          ++numSpecAuth;
        }
      }
      specialAuthorities_ = new String[numSpecAuth];
      int counter = 0;
      for (int i=0; i<8; ++i)
      {
        if (data[83+i] == (byte)0xE8) // 'Y' is EBCDIC 0xE8
        {
          specialAuthorities_[counter++] = SPECIAL_AUTHORITIES[i];
        }
      }
      groupProfile_ = conv.byteArrayToString(data, 98, 10).trim();
      owner_ = conv.byteArrayToString(data, 108, 10).trim();
      groupAuthority_ = conv.byteArrayToString(data, 118, 10).trim();
      assistanceLevel_ = conv.byteArrayToString(data, 128, 10).trim();
      currentLibrary_ = conv.byteArrayToString(data, 138, 10).trim();
      String menu = conv.byteArrayToString(data, 148, 10).trim();
      if (menu.equals("*SIGNOFF"))
      {
        initialMenu_ = menu;
      }
      else
      {
        initialMenu_ = QSYSObjectPathName.toPath(conv.byteArrayToString(data, 158, 10).trim(), menu, "MNU");
      }
      String prog = conv.byteArrayToString(data, 168, 10).trim();
      if (prog.equals("*NONE"))
      {
        initialProgram_ = prog;
      }
      else
      {
        initialProgram_ = QSYSObjectPathName.toPath(conv.byteArrayToString(data, 178, 10).trim(), prog, "PGM");
      }
      limitCapabilities_ = conv.byteArrayToString(data, 188, 10).trim();
      description_ = conv.byteArrayToString(data, 198, 50).trim();
      displaySignonInfo_ = conv.byteArrayToString(data, 248, 10).trim();
      limitDeviceSessions_ = conv.byteArrayToString(data, 258, 10).trim();
      keyboardBuffering_ = conv.byteArrayToString(data, 268, 10).trim();
      maxAllowedStorage_ = BinaryConverter.byteArrayToInt(data, 280);
      storageUsed_ = BinaryConverter.byteArrayToInt(data, 284);
      highestSchedulingPriority_ = Integer.parseInt(conv.byteArrayToString(data, 288, 1));
      String jobdName = conv.byteArrayToString(data, 289, 10).trim();
      String jobdLib = conv.byteArrayToString(data, 299, 10).trim();
      jobDescription_ = QSYSObjectPathName.toPath(jobdLib, jobdName, "JOBD");
      accountingCode_ = conv.byteArrayToString(data, 309, 15).trim();
      String queueName = conv.byteArrayToString(data, 324, 10).trim();
      String queueLib = conv.byteArrayToString(data, 334, 10).trim();
      messageQueue_ = QSYSObjectPathName.toPath(queueLib, queueName, "MSGQ");
      messageQueueDeliveryMethod_ = conv.byteArrayToString(data, 344, 10).trim();
      messageQueueSeverity_ = BinaryConverter.byteArrayToInt(data, 356);
      String outQueueName = conv.byteArrayToString(data, 360, 10).trim();
      if (outQueueName.equals("*WRKSTN") || outQueueName.equals("*DEV"))
      {
        outputQueue_ = outQueueName;
      }
      else
      {
        outputQueue_ = QSYSObjectPathName.toPath(conv.byteArrayToString(data, 370, 10).trim(), outQueueName, "OUTQ");
      }
      printDevice_ = conv.byteArrayToString(data, 380, 10).trim();
      specialEnvironment_ = conv.byteArrayToString(data, 390, 10).trim();
      String keyName = conv.byteArrayToString(data, 400, 10).trim();
      if (keyName.equals("*SYSVAL") || keyName.equals("*NONE") || keyName.equals("*ASSIST"))
      {
        attentionKeyHandlingProgram_ = keyName;
      }
      else
      {
        attentionKeyHandlingProgram_ = QSYSObjectPathName.toPath(conv.byteArrayToString(data, 410, 10).trim(), keyName, "PGM");
      }
      languageID_ = conv.byteArrayToString(data, 420, 10).trim();
      countryID_ = conv.byteArrayToString(data, 430, 10).trim();
      ccsid_ = BinaryConverter.byteArrayToInt(data, 440);
      int numUserOptions = 0;
      for (int i=0; i<7; ++i)
      {
        if (data[444+i] == (byte)0xE8) // 'Y'
        {
          ++numUserOptions;
        }
      }
      userOptions_ = new String[numUserOptions];
      counter = 0;
      for (int i=0; i<7; ++i)
      {
        if (data[444+i] == (byte)0xE8) // 'Y'
        {
          userOptions_[counter++] = USER_OPTIONS[i];
        }
      }
      
      String sortName = conv.byteArrayToString(data, 480, 10).trim();
      if (sortName.equals("*HEX") || sortName.equals("*LANGIDUNQ") ||
          sortName.equals("*LANGIDSHR") || sortName.equals("*SYSVAL") || sortName.length() == 0)
      {
        sortSequenceTable_ = sortName;
      }
      else
      {
        sortSequenceTable_ = QSYSObjectPathName.toPath(conv.byteArrayToString(data, 490, 10).trim(), sortName, "FILE");
      }
      objectAuditingValue_ = conv.byteArrayToString(data, 500, 10).trim();
      int numAudLevel = 0;
      for (int i=0; i<13; ++i)
      {
        if (data[510+i] == (byte)0xE8) // 'Y'
        {
          ++numAudLevel;
        }
      }
      userActionAuditLevel_ = new String[numAudLevel];
      counter = 0;
      for (int i=0; i<13; ++i)
      {
        if (data[510+i] == (byte)0xE8) // 'Y'
        {
          userActionAuditLevel_[counter++] = AUDIT_LEVELS[i];
        }
      }

      groupAuthorityType_ = conv.byteArrayToString(data, 574, 10).trim();
      
      int supplementalGroupOffset = BinaryConverter.byteArrayToInt(data, 584);
      int supplementalGroupCount = BinaryConverter.byteArrayToInt(data, 588);
      supplementalGroups_ = new String[supplementalGroupCount];
      for (int i=0; i<supplementalGroupCount; ++i)
      {
        supplementalGroups_[i] = conv.byteArrayToString(data, supplementalGroupOffset+(i*10), 10).trim();
      }
      userID_ = BinaryConverter.byteArrayToUnsignedInt(data, 592);
      groupID_ = BinaryConverter.byteArrayToUnsignedInt(data, 596);
      
      int homeDirOffset = BinaryConverter.byteArrayToInt(data, 600);
      //int homeDirLength = BinaryConverter.byteArrayToInt(data, 604);
      int homeDirCcsid = BinaryConverter.byteArrayToInt(data, homeDirOffset);
      int homeDirLength = BinaryConverter.byteArrayToInt(data, homeDirOffset+16);
      ConvTable homeDirConv = conv;
      if (homeDirCcsid > 0 && homeDirCcsid < 65535)
      {
        homeDirConv = ConvTable.getTable(homeDirCcsid, null);
      }
      homeDirectory_ = homeDirConv.byteArrayToString(data, homeDirOffset+32, homeDirLength).trim();
      
      int numLocaleJobAttribs = 0;
      for (int i=0; i<8; ++i)
      {
        if (data[608+i] == (byte)0xE8) // 'Y'
        {
          ++numLocaleJobAttribs;
        }
      }
      localeJobAttributes_ = new String[numLocaleJobAttribs];
      counter = 0;
      for (int i=0; i<8; ++i)
      {
        if (data[608+i] == (byte)0xE8) // 'Y'
        {
          localeJobAttributes_[counter++] = LOCALE_ATTRIBUTES[i];
        }
      }
      
      int localePathOffset = BinaryConverter.byteArrayToInt(data, 624);
      int localePathLength = BinaryConverter.byteArrayToInt(data, 628);
      localePathName_ = conv.byteArrayToString(data, localePathOffset, localePathLength).trim();
      if (!localePathName_.startsWith("*"))
      {
        int localePathCcsid = BinaryConverter.byteArrayToInt(data, localePathOffset);
        localePathLength = BinaryConverter.byteArrayToInt(data, localePathOffset+16);
        ConvTable localePathConv = conv;
        if (localePathCcsid > 0 && localePathCcsid < 65535)
        {
          localePathConv = ConvTable.getTable(localePathCcsid, null);
        }
        localePathName_ = localePathConv.byteArrayToString(data, localePathOffset+32, localePathLength).trim();
      }

      hasMembers_ = data[632] == (byte)0xF1; // '1' indicates the user is a group that has members
      digitalCertificateIndicator_ = data[633] == (byte)0xF1; // '1' indicates there is at least one digital certificate associated with this user
      characterIdentifierControl_ = conv.byteArrayToString(data, 634, 10).trim();
      
      if (vrm_ == 0) vrm_ = system_.getVRM();
      if (vrm_ >= VRM510)
      {
        int iaspOffset = BinaryConverter.byteArrayToInt(data, 644);
        int iaspCount = BinaryConverter.byteArrayToInt(data, 648);
        int iaspCountReturned = BinaryConverter.byteArrayToInt(data, 652);
        int iaspLength = BinaryConverter.byteArrayToInt(data, 656);
        if (Trace.traceOn_ && iaspCount != iaspCountReturned)
        {
          Trace.log(Trace.WARNING, "Not all IASP information was returned: "+iaspCount+", "+iaspCountReturned);
        }
        iaspNames_ = new String[iaspCountReturned];
        iaspMaxAllowed_ = new int[iaspCountReturned];
        iaspStorageUsed_ = new int[iaspCountReturned];
        for (int i=0; i<iaspCountReturned; ++i)
        {
          int offset = iaspOffset+(i*iaspLength);
          iaspNames_[i] = conv.byteArrayToString(data, offset, 10).trim();
          iaspMaxAllowed_[i] = BinaryConverter.byteArrayToInt(data, offset+12);
          iaspStorageUsed_[i] = BinaryConverter.byteArrayToInt(data, offset+16);
        }
      }
      loaded_ = true;
    }

    // Helper method... calls loadUserInformation and swallows all exceptions
    // so that all of the getters can call it.
    private void refresh()
    {
      try
      {
        loadUserInformation();
      }
      catch(Exception e)
      {
        if (Trace.traceOn_)
        {
          Trace.log(Trace.ERROR, "Exception swallowed by refresh():", e);
        }
      }
    }


/**
Removes a PropertyChangeListener.

@param listener The listener.
**/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
      if (listener == null) throw new NullPointerException("listener");
      if (propertyChangeSupport_ != null) propertyChangeSupport_.removePropertyChangeListener(listener);
    }



/**
Removes a VetoableChangeListener.

@param listener The listener.
**/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
      if (listener == null) throw new NullPointerException("listener");
      if (vetoableChangeSupport_ != null) vetoableChangeSupport_.removeVetoableChangeListener(listener);
    }



/**
Sets the user profile name.  This does not change the name of the user profile on
the server.  Instead, it changes the user profile to which
this User object references.  This cannot be changed
if the object has established a connection to the server.

@param name    The user profile name.

@exception PropertyVetoException    If the property change is vetoed.
@see #getName
**/
    public void setName(String name)
        throws PropertyVetoException
    {
      if (name == null) throw new NullPointerException("name");
      if (loaded_) throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
      String old = name_;
      if (vetoableChangeSupport_ != null) vetoableChangeSupport_.fireVetoableChange("name", old, name);
      name_ = name;
      if (propertyChangeSupport_ != null) propertyChangeSupport_.firePropertyChange("name", old, name);
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
      if (loaded_) throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
      AS400 old = system_;
      if (vetoableChangeSupport_ != null) vetoableChangeSupport_.fireVetoableChange("system", old, system);
      system_ = system;
      vrm_ = 0;
      if (propertyChangeSupport_ != null) propertyChangeSupport_.firePropertyChange("system", old, system);
    }



/**
Returns the string representation of this User object.

@return The user profile name.
**/
    public String toString()
    {
      return super.toString()+"["+name_+"]";
    }


}
