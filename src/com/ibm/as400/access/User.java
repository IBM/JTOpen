///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
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
//@A1 Added imports.
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.beans.PropertyVetoException;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Vector;
import java.lang.String;

/**
 * The User class represents an AS/400 user.
 * The default User constructor only provides a default virtual user. To retrieve the information of a real user on AS400,
 * the methods <i>setSystem()</i>, <i>setName()</i>  and <i>loadUserInformation()</i> should be explicitly invoked. Here
 * is an example:
 * <P><blockquote><pre>

 * // Constructs a AS400 system object.
 * AS400 system = new AS400();

 * // Create a user
 * User user = new User();
 *
 * ...
 * user.setSystem(system);
 * user.setName("Fred");
 * user.loadUserInformation();
 * ...
 * System.out.println(user.getJobDescription());
 * </pre></blockquote></p>
 *
**/
public class User
implements Serializable                                             // @A2A
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // The name of the user.
    private String name_;

    /**
    * Vector storing the information about the user.
    **/
    private Vector userInformation_;

    // The AS400 system.
    private AS400 system_;

    // Access object.
    private UserGroupAccess access_;

    // Property change and vetoable change support.
    transient private PropertyChangeSupport changes = new PropertyChangeSupport(this);
    transient private VetoableChangeSupport vetos = new VetoableChangeSupport(this);

    // Possible values for locale job attributes.                   // @A2A
    private static final String[] LOCALE_JOB_ATTRIBUTES_ =          // @A2A
        { "*NONE", "*SYSVAL", "*CCSID", "*DATFMT", "*DATSEP",       // @A2A
          "*SRTSEQ", "*TIMSEP", "*DECFMT" };                        // @A2A

    // Possible values for user action audit levels.                // @A2A
    private static final String[] USER_ACTION_AUDIT_LEVELS_ =       // @A2A
        { "*CMD", "*CREATE", "*DELETE", "*JOBDTA",                  // @A2A
          "*OBJMGT", "*OFCSRV", "*PGMADP",                          // @A2A
          "*SAVRST", "*SECURITY", "*SERVICE", "*SPLFDTA",           // @A2A
          "*SYSMGT", "*OPTICAL" };                                  // @A2A

    // Possible values for special authorities.                     // @A2A
    private static final String[] SPECIAL_AUTHORITIES_ =            // @A2A
        { "*ALLOBJ",                                                // @A2A
          "*SECADM", "*JOBCTL", "*SPLCTL",                          // @A2A
          "*SAVSYS", "*SERVICE", "*AUDIT", "*IOSYSCFG" };           // @A2A

    //@A1A Added default constructor.
    /**
     * Constructs a User object.
    **/
    public User()
    {
        access_=new UserGroupAccess();
        userInformation_=access_.getDefaultUserInformation();
    }

    //@A1C Added code to existing constructor.
    /**
     * Constructs a User object.
     *
     * @param system The AS/400 system in which the user information resides.
     * @param userProfileName The user profile name.
     * @exception AS400Exception                  If the AS/400 system returns an error message.
     * @exception AS400SecurityException          If a security or authority error occurs.
     * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException            If this thread is interrupted.
     * @exception IOException                     If an error occurs while communicating with the AS/400.
     * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
     * @exception UnsupportedEncodingException    If the character encoding is not supported.
    **/
    public User(AS400 system,String userProfileName)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   ObjectDoesNotExistException,
                   IOException,
                   // @A2D PropertyVetoException,
                   UnsupportedEncodingException
    {
        if(system == null)
                throw new NullPointerException("system");

        if(userProfileName == null)                                 //@D1a
                throw new NullPointerException("userProfileName");  //@D1a

        system_= system;
        name_  = userProfileName.toUpperCase();                     //@D1c

        try {                                                   // @A2A
            loadUserInformation();
        }                                                       // @A2A
        catch (PropertyVetoException e) {                       // @A2A
            // Ignore.                                          // @A2A
        }                                                       // @A2A
    }

  //@A1A Added method.
  /**
  * Adds a listener to be notified when the value of any bound
  * property is changed. The <i>propertyChange()</i> method will be called.
  *
  * @param listener The property change listener.
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

 //@A1A Added method.
 /**
  * Adds a listener to be notified when the value of any constrained
  * property is changed. The <i>vetoableChange()</i> method will be called.
  *
  * @param listener The vetoable change listener.
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


  // @A2A
  private String[] apiToArray (String apiReturn, String[] possibleValues)
  {
      Vector values = new Vector (possibleValues.length);
      for (int i = 0; i < possibleValues.length; ++i) {
          if (apiReturn.length () > i) {
              if (apiReturn.charAt(i) == 'Y') {
                  values.addElement (possibleValues[i]);
              }
          }
      }

      String[] array = new String[values.size ()];
      values.copyInto (array);
      return array;
  }


    //@A1A Added method.
    /**
     * Returns the accounting code that is associated with this user.
     *
     * @return The accounting code that is associated with this user. If the user
     * does not have an accounting code, this field is blank.
    **/
    public String getAccountingCode()
    {
        return (String) userInformation_.elementAt(36);
    }
    //@A1A Added method.
    /**
     * Returns the user interface that the user will use.
     *
     * @return The user interface that the user will use. The possible values are:
     *         <ul>
     *           <li>*SYSVAL - The system value QASTLVL determines which user interface the user is using.
     *           <li>*BASIC - The Operational Assistance user interface.
     *           <li>*INTERMED - The system user interface.
     *           <li>*ADVANCED - The expert system user interface.
     *         </ul>
    **/
    public String getAssistanceLevel()
    {
        return (String) userInformation_.elementAt(19);

    }
    //@A1A Added method.
    /**
     * Returns the full path of the attention-key-handling program for this user.
     *
     * @return The full path of the attention-key-handling program for this user. The special values
     * that may be used: *SYSVAL, *NONE, or *ASSIST.
    **/
    public String getAttentionKeyHandlingProgram()
    {
        String lib,name;;
        lib=(String) userInformation_.elementAt(47);
        name=(String) userInformation_.elementAt(46);
        if(lib.equals(""))
           return name;
        else
           return lib+"/"+name;
    }


    //@A1A Added method.
    /**
     * Returns the character code set identifier to be used by the system for this user.
     *
     * @return The character code set identifier to be used by the system for this user.
     * It can be the following special values:
     *         <ul>
     *           <li>-2 - The system value QCCSID is used to determine the user's character code set identifier.
     *         </ul>
    **/
    public int getCCSID()
    {
        return ((Integer) userInformation_.elementAt(50)).intValue();

    }

    //@A3A Added method.
    /**
     * Returns the character identifier control for this user.
     *
     * @return The character identifier control for this user.
     * It can be one of the following special values:
     * <ul>
     * <li>*SYSVAL - The value QCHRIDCTL system value will be
     *               used to determine the CHRID control for this user.
     *
     * <li>*DEVD - The *DEVD special value performs the same function
     *             as on the CHRID command parameter for display files,
     *             printer files, and panel groups.
     *
     * <li>*JOBCCSID - The *JOBCCSID special value performs the same
     *                 function as on the CHRID command parameter for
     *                 display files, printer files, and panel groups.
     * </ul>
    **/
//@A3D    public String getCHRIDCTL()
//@A3D    {
//@A3D        return (String)userInformation_.elementAt(69);
//@A3D    }

    /**
    * Returns the copyright.
    **/
    private static String getCopyright ()
    {
       return Copyright.copyright;
    }
    //@A1A Added method.
    /**
     * Returns the country identifier used by the system for this user.
     *
     * @return The country identifier used by the system for this user.
     * It can be the following special values:
     *         <ul>
     *           <li>*SYSVAL - The system value  QCNTRYID is used to determine the user's country identifier.
     *         </ul>
    **/
    public String getCountryID()
    {
        return (String) userInformation_.elementAt(49);

    }
    //@A1A Added method.
    /**
     * Returns the name of the user's current library.
     *
     * @return The name of the user's current library.
     * It can be the following special values:
     *         <ul>
     *           <li>*CRTDFT - The user does not have have a current library.
     *         </ul>
    **/
    public String getCurrentLibraryName()
    {
        return (String) userInformation_.elementAt(20);

    }
    //@A1A Added method.
    /**
     * Returns the number of days until the password will expire. 0 indicates that
       the password is expired.
     *
     * @return The number of days until the password will expire.
    **/
    public int getDaysUntilPasswordExpire()
    {
        return ((Integer) userInformation_.elementAt(12)).intValue();

    }

    /**
     * Returns the descriptive text for the user profile.
     *
     * @return The descriptive text for the user profile.
    **/
    public String getDescription()
    {
        return (String) userInformation_.elementAt(26);

    }
    //@A1A Added method.
    /**
     * Returns the value that indicates whether the sign-on information display is shown when
     * the user signs on.
     *
     * @return The value that indicates whether the sign-on information display is shown when
     * the user signs on. The possible values are:
     *         <ul>
     *           <li>*SYSVAL - The system value QDSPSGNINF determines if the sign-on
     *                             information display is shown when the user signs on.
     *           <li>*YES - The sign-on information display is shown when the user signs
     *                        on.
     *           <li>*NO - The sign-on information display is not shown when the user
     *                       signs on.
     *         </ul>
    **/
    public String getDisplaySignOnInformation()
    {
        return (String) userInformation_.elementAt(27);

    }

    //@A1A Added method.
    /**
     * Returns the authority the user's group profile has to the objects the user creates.
     *
     * @return The authority the user's group profile has to the objects the user creates.
     * The possible values are:
     * <ul>
     *  <li> *NONE - The group profile has no authority to the object the user creates. If the user does not have a group profile, the field contains this value.
     *  <li> *ALL - The group profile has all authority to the object the user creates.
     *  <li> *CHANGE - The group profile has change authority to the object the user creates.
     *  <li> *USE - The group profile has use authority to the object the user creates.
     *  <li> *EXCLUDE - The group profile has exclude authority to the object the user creates.
     * </ul>
    **/
    public String getGroupAuthority()
    {
        return (String) userInformation_.elementAt(18);

    }

    //@A1A Added method.
    /**
     * Returns the type of authority the user's group profile has to the objects the
     * user creates.
     *
     * @return The type of authority the user's group profile has to objects the user
     *         creates. The possible values are:
     *         <ul>
     *           <li>*PRIVATE - The group profile has a private authority to the objects
     *                            the user creates. If the user does not have a group profile,
     *                            return this value.
     *           <li>*PGP - The group profile will be the primary group for objects the user
     *                        creates.
     *         </ul>
    **/
    public String getGroupAuthorityType()
    {

       return (String) userInformation_.elementAt(56);

    }

    //@A1A Added method.
    /**
     * Returns the group identifier number for the user profile.
     *
     * @return The group identifier number for the user profile. The possible values are:
     *         <ul>
     *           <li>0 - Same as *NONE. The user does not have a group identifier.
     *           <li>1 through 4294967294 which is a valid group identifier.
     *         </ul>
    **/
    public int getGroupIDNumber()
    {
        return ((Integer) userInformation_.elementAt(60)).intValue();

    }

    //@A1A Added method.
    /**
     * Returns the name of the group profile. If the user does not have a group profile,
     * this method will return *NONE.
     *
     * @return The name of the group profile.
    **/
    public String getGroupProfileName()
    {
        return (String) userInformation_.elementAt(16);

    }

    //@A1A Added method.
    /**
     * Returns the highest scheduling priority the user is allowed to have
     * for each job submitted to the system.
     *
     * @return The highest scheduling priority the user is allowed. It is a number
     * from 0 through 9, with 0 being the highest priority.
    **/
    public int getHighestSchedulingPriority()
           throws NumberFormatException
    {
        int i=0;
        i=Integer.parseInt((String) userInformation_.elementAt(33));
        return i;

    }

    //@A1A Added method.
    /**
     * Returns the home directory for this user profile. This is the user's initial working directory.
     *
     * @return The home directory for this user profile.
    **/
    public String getHomeDirectory()
    {
        return (String) userInformation_.elementAt(69); //@A3C

    }

    //@A1A Added method.
    /**
     * Returns the full path of the initial menu for the user.  For example the possible return value is "*LIBL/MAIN".
     *
     * @return The full path of the initial menu for the user.
    **/
    public String getInitialMenu()
    {
        String lib,name;
        lib=(String) userInformation_.elementAt(22);
        name=(String) userInformation_.elementAt(21);
        if(lib.equals(""))
            return name;
        else
            return lib+"/"+name;
    }

    //@A1A Added method.
    /**
     * Returns the full path of the initial program for the user.
     *
     * @return The full path of the initial program for the user.
    **/
    public String getInitialProgram()
    {
        String lib,name;
        lib=(String) userInformation_.elementAt(24);
        name=(String) userInformation_.elementAt(23);
        if(lib.equals(""))
            return name;
        else
            return lib+"/"+name;
    }

    //@A1A Added method.
    /**
     * Returns the full path of the job description used for jobs that start through
     * subsystem work station entries.
     *
     * @return The full path of the job description used for jobs.
    **/
    public String getJobDescription()
    {
        String lib,name;
        lib=(String) userInformation_.elementAt(35);
        name=(String) userInformation_.elementAt(34);
        // @A2D if(lib.equals(""))  // lib will never be empty
        // @A2D     return name;
        // @A2D else
            return lib+"/"+name;
    }

    //@A1A Added method.
    /**
     * Returns the language identifier used by the system for this user.
     *
     * @return The language identifier used by the system for this user. It can be the
     * following special values:
     *         <ul>
     *           <li>*SYSVAL - The system value  QLANGID is used to determine
     *                           the user's language identifier.
     *         </ul>
    **/
    public String getLanguageID()
    {
        return (String) userInformation_.elementAt(48);

    }

    //@A1A Added method.
    /**
     * Returns the value indicating whether the user has limited capabilities.
     *
     * @return The value indicating whether the user has limited capabilities.
     * The possible values are:
     *         <ul>
     *           <li>*PARTIAL - The user can not change his initial program or
     *                            current library.
     *           <li>*YES - The user can not change his initial menu, initial
     *                        program, or current library. The user can not run
     *                        commands from the command line.
     *           <li>*NO - The user is not limited.
     *         </ul>
    **/
    public String getLimitCapabilities()
    {
        return (String) userInformation_.elementAt(25);

    }

    //@A1A Added method.
    /**
     * Returns the value indicating whether the user is limited to one device session.
     *
     * @return The value indicating whether the user is limited to one device session.
     * The possible values are:
     *         <ul>
     *           <li>*SYSVAL - The system value QLMTDEVSSN determines if the user
     *                           is limited to one device sessins.
     *           <li>*YES - The user is limited to one device sessions.
     *           <li>*NO - The user is not limited to one device sessions.
     *         </ul>
    **/
    public String getLimitDeviceSessions()
    {
        return (String) userInformation_.elementAt(28);

    }

    //@A1A Added method.
    /**
     * Returns the job attributes that are taken from the user's locale path name.
     *
     * @return The job attributes that are taken from the user's locale path name.
     * The possible values are :
     * <ul>
     *   <li> *NONE - No job attributes are used from the locale path name at the time a job is started for this user profile.
     *   <li> *SYSVAL - The job attributes assigned from the locale path name are dtermined by the system value QSETJOBATR at the time a job is started for this user profile.
     *   <li> *CCSID - The coded character set identifier is set from the locale path name at the time a job is started for this user profile.
     *   <li> *DATFMT - The date format is set from the locale path name at the time a job is started for this user profile.
     *   <li> *DATSEP - The date separator is set from the locale path name at the time a job is started for this user profile.
     *   <li> *SRTSEQ - The sort sequence is set from the locale path name at the time a job is started for this user profile
     *   <li> *TIMSEP - The time separator is set from the locale path name at the time a job is started for this user profile
     *   <li> *DECFMT - The decimal format is set from the locale path name at the time a job is started for this user profile
     * </ul>
    **/
    public String[] getLocaleJobAttributes()                            // @A2C
    {
        String apiReturn = (String) userInformation_.elementAt(63);     // @A2C
        return apiToArray (apiReturn, LOCALE_JOB_ATTRIBUTES_);          // @A2A
    }

    //@A1A Added method.
    /**
     * Returns the locale path name that is assigned to the user profile when a job is
     * started.
     *
     * @return The locale path name that is assigned to the user profile when a job is
     * started. The possible values are :
     * <ul>
     *  <li> *C - The C locale path name is assigned.
     *  <li> *NONE - No locale path name is assigned.
     *  <li> *POSIX - The POSIX locale path name is assigned.
     *  <li> *SYSVAL - The QLOCALE system value is used to determine the locale path name.
     * </ul>
    **/
    public String getLocalePathName()
    {
        return ((String) userInformation_.elementAt(70)).trim (); // @A2C //@A3C

    }


    //@A1A Added method.
    /**
     * Returns the maximum amount of auxiliary storage (in kilobytes) that can be
     * assigned to store permanent objects owner by the user. The value -1 is for *NOMAX.
     *
     * @return The maximum amount of auxiliary storage allowed.
    **/
    public int getMaximumStorageAllowed()
    {
        return ((Integer) userInformation_.elementAt(31)).intValue();

    }
    //@A1A Added method.
    /**
     * Returns the full path of the message queue that is used by this user. The default message queue name is the same as the user profile name. For example, the return value of the user "QAUTPROF" is "QAUTPROF".
     *
     * @return The full path of the message queue that is used by this user.
    **/
    public String getMessageQueue()
    {
       return (String) userInformation_.elementAt(37);

    }
    //@A1A Added method.
    /**
     * Returns the message queue delivery method which indicates how the messages
     * are delivered to the message queue used by the user.
     *
     * @return The message queue delivery method. The possible values are:
     *         <ul>
     *           <li>*BREAK - The job to which the message queue is assigned is interrupted
     *                          when a message arrives on the message queue.
     *           <li>*DFT - Messages requiring replies are answered with their default reply.
     *           <li>*HOLD - The messages are held in the message queue until they are requested
     *                         by the user or program.
     *           <li>*NOTIFY - The job to which the message queue is assigned is notified
     *                          when a message arrives on the message queue.
     *         </ul>
    **/
    public String getMessageQueueDeliveryMethod()
    {
        return (String) userInformation_.elementAt(39);

    }
    //@A1A Added method.
    /**
     * Returns the lowest severity that a message can have and still
     * be delivered to a user in break or notify mode. The value ranges from 0 through 99.
     *
     * @return The lowest severity that a message can have and still be delivered to a user
     *         in break or notify mode.
    **/
    public int getMessageQueueSeverity()
    {
        return ((Integer) userInformation_.elementAt(41)).intValue();

    }

    /**
     * Returns the name of the user.
     *
     * @return The name of the user.
    **/
    public String getName()
    {
        return name_;
    }
    //@A1A Added method.
    /**
     * Returns the current user's object auditing value.
     *
     * @return The current user's object auditing value.
    **/
    public String getObjectAuditingValue()
    {

        return (String) userInformation_.elementAt(54);

    }

    //@A1A Added method.
    /**
     * Returns the full path of the output queue used by this user.
     *
     * @return The full path of the output queue used by this user.
    **/
    public String getOutputQueue()
    {
       return (String) userInformation_.elementAt(42);

    }

    //@A1A Added method.
    /**
     * Returns the value indicating who is to own objects created by those user.
     *
     * @return The value indicating who is to own objects created by those user. The possible values are:
     *         <ul>
     *           <li> *USRPRF - The user owns any objects the user creates. If the
     *                           user does not have a group profile, returns this value.
     *           <li> *GRPPRF - Yhe user's profile owns any objects the user creates.
     *         </ul>
    **/
    public String getOwner()
    {
        return (String) userInformation_.elementAt(17);

    }
    //@A1A Added method.
    /**
     * Returns the date the user's password expires.
     *
     * @return The date the user's password expires.
    **/
    public Date getPasswordExpireDate()
    {
        byte[] dateString=(byte[])userInformation_.elementAt(11); //@A3C
        return access_.parseDate(dateString,UserGroupAccess.SYSTEM_TIMESTAMP_FORMAT);

    }
    //@A1A Added method.
    /**
     * Returns the number of days (from 1 through 366) the user's password can
     * remain active before it must be changed.
     *
     * @return The password expiration interval. It can be the following special values:
     *         <ul>
     *           <li>0 - The system value QPWDEXPITV is used to determine the user's password
     *                   expiration interval.
     *           <li>-1 - The user's password does not expire(*NOMAX).
     *         </ul>
    **/
    public int getPasswordExpirationInterval()
    {
        return ((Integer) userInformation_.elementAt(10)).intValue();

    }
    //@A1A Added method.
    /**
     * Returns the date the user's password was last changed.
     *
     * @return The date the user's password was last changed.
    **/
    public Date getPasswordLastChangedDate()
    {
        byte[] dateString=(byte[])userInformation_.elementAt(7); //@A3C
        return access_.parseDate(dateString,UserGroupAccess.SYSTEM_TIMESTAMP_FORMAT);

    }
    //@A1A Added method.
    /**
     * Returns the date and time the user last signed on.
     *
     * @return The date and time the user last signed on.
    **/
    public Date getPreviousSignedOnDate()
    {
        String dateString=(String)userInformation_.elementAt(3);
        return access_.parseDate(dateString,UserGroupAccess.CYYMMDDHHMMSS_FORMAT);

    }
    //@A1A Added method.
    /**
     * Returns the printer used to print for this user.
     *
     * @return The printer used to print for this user.
    **/
    public String getPrintDevice()
    {
        return (String) userInformation_.elementAt(44);

    }
    //@A1A Added method.
    /**
     * Returns the number of the sign-on attempts that were not valid since the
     * last successful sign-on.
     *
     * @return The number of the sign-on attempts that were not valid since the
     *         last successful sign-on.
    **/
    public int getSignedOnAttemptsNotValid()
    {
        return ((Integer) userInformation_.elementAt(5)).intValue();

    }
    //@A1A Added method.
    /**
     * Returns the full path of the sort sequence table used for string comparisons.
     *
     * @return The full path of the sort sequence table used for string comparisons.
    **/
    public String getSortSequenceTable()
    {
        String lib,name;
        lib=(String) userInformation_.elementAt(53);

        name=(String) userInformation_.elementAt(52);
        if(lib.equals(""))
            return name;
        else
            return lib+"/"+name;
    }

    //@A1A Added method.
    /**
     * Returns the special authority of the user.
     *
     * @return The special authority of the user.
     * Possible values are :
     * <ul>
     *   <li> *ALLOBJ - All object. Indicates the user has all object special authority.
     *   <li> *SECADM - Security administrator. Indicates the user has security administrator special authority.
     *   <li> *JOBCTL - Job control. Indicates the user has job control special authority.
     *   <li> *SPLCTL - Spool control. Indicates the user has spool control special authority.
     *   <li> *SAVSYS - Save system. Indicates the user has save system special authority.
     *   <li> *SERVICE - Service. Indicates the user has service special authority.
     *   <li> *AUDIT - Audit. Indicates the user has audit special authority.
     *   <li> *IOSYSCFG - Input/output system configuration. Indicates the user has input/output system configuration special authority.
     * </ul>
    **/
    public String[] getSpecialAuthority()                               // @A2C
    {
        String apiReturn = (String) userInformation_.elementAt(15);     // @A2C
        return apiToArray (apiReturn, SPECIAL_AUTHORITIES_);          // @A2A
    }
    //@A1A Added method.
    /**
     * Returns the special environment the user operates in after signing on.
     *
     * @return The special environment the user operates in after signing on.
     * The possible values are:
     *         <ul>
     *           <li>*SYSVAL - The system value QSPCENV is used to determine
     *                           the user's special environment.
     *           <li>*NONE - The user operates in the OS/400 environment.
     *           <li>*S36 - The user operates in the System/400 S/36 environment.
     *         </ul>
    **/
    public String getSpecialEnvironment()
    {
        return (String) userInformation_.elementAt(45);

    }
    //@A1A Added method.
    /**
     * Returns the status of the user profile.
     *
     * @return The status of the user profile. The possible values are:
     *         <ul>
     *           <li>*ENABLED - The user profile is enabled;therefore, the user is able to
     *                            sign on.
     *           <li>*DISABLED - The user profile is disabled;therefore, the user can not
     *                             sign on.
     *         </ul>
    **/
    public String getStatus()
    {
        return (String) userInformation_.elementAt(6);

    }
    //@A1A Added method.
    /**
     * Returns the amount of auxiliary storage (in kilobytes) occupied by this user's owned
     * objects.
     *
     * @return The amount of auxiliary storage (in kilobytes) occupied by this user's owned
     * objects.
    **/
    public int getStorageUsed()
    {
        return ((Integer) userInformation_.elementAt(32)).intValue();

    }
    //@A1A Added method.
    /**
     * Returns the array of supplemental groups for the user profile.
     *
     * @return The array of supplemental groups for the user profile.
    **/
    public String[] getSupplementalGroups()
    {
        int num=getSupplementalGroupsNumber();
        String[] member=new String[num];
        Object[] objs =(Object[]) userInformation_.elementAt(68);
        for (int i=0;i<member.length;i++)
        {
           member[i]=((String)objs[i]).trim (); // @A2C
        }
        return member;

    }
    //@A1A Added method.
    /**
     * Returns the number of supplemental groups returned in the array.
     *
     * @return The number of supplemental groups returned in the array.
    **/
    public int getSupplementalGroupsNumber()
    {
       return ((Integer) userInformation_.elementAt(58)).intValue();

    }
    //@A1A Added method.
    /**
     * Returns the AS/400 system.
     *
     * @return The AS/400 object.
    **/
    public AS400 getSystem()
    {
        return system_;
    }
    //@A1A Added method.
    /**
     * Returns the action audit values for this user.
     *
     * @return The action audit values for this user.
     * The possible values are :
     * <ul>
     * <li> *CMD - The user has the *CMD audit value specified in the user profile.
     * <li> *CREATE - The user has the *CREATE audit value specified in the user profile.
     * <li> *DELETE - The user has the *DELETE audit value specified in the user profile.
     * <li> *JOBDTA - The user has the *JOBDTA audit value specified in the user profile.
     * <li> *OBJMGT - The user has the *OBJMGT audit value specified in the user profile.
     * <li> *OFCSRV - The user has the *OFCSRV audit value specified in the user profile.
     * <li> *OPTICAL - The user has the *OPTICAL audit value specified in the user profile.
     * <li> *PGMADP - The user has the *PGMADP audit value specified in the user profile.
     * <li> *SAVRST - The user has the *SAVRST audit value specified in the user profile.
     * <li> *SECURITY - The user has the *SECURITY audit value specified in the user profile.
     * <li> *SERVICE - The user has the *SERVICE audit value specified in the user profile.
     * <li> *SPLFDTA - The user has the *SPLFDTA audit value specified in the user profile.
     * <li> *SYSMGT - The user has the *SYSMGT audit value specified in the user profile.
     * </ul>
    **/
    public String[] getUserActionAuditLevel()                           // @A2C
    {
        String apiReturn = (String) userInformation_.elementAt(55);     // @A2C
        return apiToArray (apiReturn, USER_ACTION_AUDIT_LEVELS_);          // @A2A
    }

    //@A1A Added method.
    /**
     * Returns the class for the user.
     *
     * @return The class for the user. The possible values are:
     *         <ul>
     *           <li>*SECOFR - The user has a class of security officer.
     *           <li>*SECADM - The user has a class of security administrator.
     *           <li>*PGMR - The user has a class of programmer.
     *           <li>*SYSOPR - The user has a class of system operator.
     *           <li>*USER - The user has a class of end user.
     *         </ul>
    **/
    public String getUserClassName()
    {
        return (String) userInformation_.elementAt(14);

    }
    //@A1A Added method.
    /**
     * Returns the user identifier number for the user profile.
     *
     * @return The user identifier number for the user profile.
    **/
    public int getUserIDNumber()
    {
        return ((Integer) userInformation_.elementAt(59)).intValue();

    }
    //@A1A Added method.
    /**
     * Returns the name of the user profile for which the information is returned.
     *
     * @return The name of the user profile for which the information is returned.
    **/
    public String getUserProfileName()
    {
        return (String) userInformation_.elementAt(2);

    }


    //@A1A Added method.
    /**
     * Indicates whether the user is a group that has members.
     *
     * @return true if the user is a group that has members; false otherwise.
    **/
    public boolean isGroupHasMember()
    {
        if(((String)userInformation_.elementAt(66)).equals("1"))
            return true;
        else
            return false;

    }
    //@A1A Added method.
    /**
     * Indicates whether *NONE is specified for the password in the user profile.
     *
     * @return true if *NONE is specified for the password in the user profile; false otherwise.
    **/
     public boolean isNoPassword()
    {
        return ((String)userInformation_.elementAt(8)).equals("Y"); //@A2C
    }

    //@A1A Added method.
    /**
     * Indicates whether the user's password is set to expire,
     * requiring the user to change the pasword when signing on.
     *
     * @return true if the user's password is set to expire; false otherwise.
    **/
    public boolean isPasswordSetExpire()
    {
        return ((String)userInformation_.elementAt(13)).equals("Y"); //@A2C
    }
    //@A1A Added method.
    /**
     * Indicates whether there are digital certificates associated with this user.
     *
     * @return true if there are digital certificates associated with this user; false otherwise.
    **/
    public boolean isWithDigitalCertificates()
    {
        return ((String)userInformation_.elementAt(67)).equals("1"); //@A2C
    }
    //@A1A Added method.
    /**
     * Loads the user information. If you have called setName() or setSystem() or both,
     * you should also call this method to reload the user information.
     *
     * @param system The AS/400 system in which the user information resides.
     * @param userProfileName The user profile name.
     * @exception AS400Exception                  If the AS/400 system returns an error message.
     * @exception AS400SecurityException          If a security or authority error occurs.
     * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException            If this thread is interrupted.
     * @exception IOException                     If an error occurs while communicating with the AS/400.
     * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
     * @exception PropertyVetoException           If the change is vetoed.
     * @exception UnsupportedEncodingException    If the character encoding is not supported.
    **/
    public void loadUserInformation()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   ObjectDoesNotExistException,
                   IOException,
                   PropertyVetoException,
                   UnsupportedEncodingException
    {
        if(system_==null)
           throw new NullPointerException("system");

        if(name_==null)
           throw new NullPointerException("userName");

        access_ = new UserGroupAccess(system_);
        access_.retrieveUserInformation(name_);
        userInformation_ = access_.getUserInformation();
    }

    //@A1A Added method.
    /**
     * Removes a property change listener.
     *
     * @param listener The property change listener.
     * @see #addPropertyChangeListener(PropertyChangeListener).
    **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
         if (listener == null)
         {
             Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
             throw new NullPointerException("listener");
         }
         changes.removePropertyChangeListener(listener);
    }
    //@A1A Added method.
    /**
     * Removes a vetoable change listener.
     *
     * @param listener The vetoable change listener.
     * @see #addVetoableChangeListener(VetoableChangeListener).
    **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
         if (listener == null)
         {
             Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
             throw new NullPointerException("listener");
         }
         vetos.removeVetoableChangeListener(listener);
    }
    //@A1A Added method.
    /**
     * Set the user profile name. The AS/400 system should have been set before
     * calling this method.
     *
     * @param userProfileName The user profile name.
     *
    **/
    public void setName(String userProfileName)
    {
        if(userProfileName == null)
                throw new NullPointerException("name");

        name_ = userProfileName.toUpperCase();             //@D1c

    }
    //@A1A Added method.
    /**
     * Set the AS/400 system.
     *
     * @param system The AS/400 system.
     * @exception Exception If an exception occurred.
     *
    **/
    public void setSystem(AS400 system)
         throws Exception
    {
        AS400 newValue,oldValue;
        if (system == null)
        {
            throw new NullPointerException("system");
        }
        if (system_!=null)
        {
            if (system_.isConnected())
            {
                throw new ExtendedIllegalStateException("system",
                    ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
            }
            oldValue = system_;
            if (system_.equals(system)==true)
               return;
        }
        else
            oldValue = null;


        vetos.fireVetoableChange("system", oldValue, system);
        system_ = system;
        newValue = system;
        changes.firePropertyChange("system", oldValue,newValue);

        return;



    }

    /**
    * Return the name of the user.
    * @return The name of the user.
    **/
    public String toString()
    {
        if(this.name_!=null)
            return this.name_;
        else
            return "";
    }


}

