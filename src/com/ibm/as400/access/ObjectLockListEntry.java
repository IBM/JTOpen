///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ObjectLockListEntry.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2008 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
 Represents a single IBM i lock placed on an ObjectDescription. 
 <p>Each entry corresponds to an entry from the List Object Locks (QWCLOBJL) API.<br>  
 Instances of this class are created by the {@link ObjectDescription#getObjectLockList() ObjectDescription.getObjectLockList()} method.
**/

public class ObjectLockListEntry
{
  // --------------------------------
  /**
  Lock scope - indicates lock has job scope.
   **/
  public static final int LOCK_SCOPE_JOB = 0;
  /**
  Lock scope - indicates lock has thread scope.
   **/
  public static final int LOCK_SCOPE_THREAD = 1;
  /**
  Lock scope - indicates lock has lock space scope.
   **/
  public static final int LOCK_SCOPE_LOCK_SPACE = 2;
    
  // --------------------------------
  /**
  Lock state for the lock request.  Indicates no locks exist.
   **/
  public static final String LOCK_STATE_NONE                 = "*NONE";
  /**
  Lock state for the lock request.  Indicates lock is shared for read.
   **/
  public static final String LOCK_STATE_SHARED_READ          = "*SHRRD";
  /**
  Lock state for the lock request.  Indicates lock is shared for update.
   **/
  public static final String LOCK_STATE_SHARED_UPDATE        = "*SHRUPD";
  /**
  Lock state for the lock request.  Indicates lock is shared for no update.
   **/
  public static final String LOCK_STATE_SHARED_NO_UPDATE     = "*SHRNUP";
  /**
  Lock state for the lock request.  Indicates exclusive lock which allows read.
   **/
  public static final String LOCK_STATE_EXCLUSIVE_ALLOW_READ = "*EXCLRD";
  /**
  Lock state for the lock request.  Indicates exclusive lock with no read.
   **/
  public static final String LOCK_STATE_EXCLUSIVE_NO_READ    = "*EXCL";
    
  // --------------------------------
  /**
  Lock status - the lock is currently held by the job or thread.
   **/
  public static final int LOCK_STATUS_LOCK_HELD = 1;
  /**
  Lock status - the job or thread is waiting for the lock (synchronous).
   **/
  public static final int LOCK_STATUS_JOB_THREAD_WAITING_SYNC = 2;
  /**
  Lock status - the job or thread has a lock request outstanding for the object (asynchronous).
   **/
  public static final int LOCK_STATUS_LOCK_REQUEST_OUTSTANDING_ASYNC = 3;

  // --------------------------------
  /**
  Lock type - lock on the object
   **/
  public static final int LOCK_TYPE_OBJECT = 1;
  /**
  Lock type - lock on the member control block.
   **/
  public static final int LOCK_TYPE_MEMBER_CONTROL_BLOCK = 2;
  /**
  Lock type - lock on the access path used to access a member's data
   **/
  public static final int LOCK_TYPE_ACCESS_PATH = 3;
  /**
  Lock type - lock on the actual data within the member.
   **/
  public static final int LOCK_TYPE_DATA_WITHIN_MEMBER = 4;

  // --------------------------------
  /**
  Lock share - the file is not shared, the file is a physical file, or the field is not applicable to object type.
   **/
  public static final int LOCK_SHARE_FILE_NOT_SHARED = 0;
  /**
  Lock share - the file is shared.
   **/
  public static final int LOCK_SHARE_FILE_SHARED = 1;

  // --------------------------------
  /**
  Job name machine process.  The job holding the lock is an internal machine process. 
   **/
  public static final String JOB_NAME_MACHINE                 = "MACHINE";
  /**
  Job name lock space. The lock is attached to a lock space.
   **/
  public static final String JOB_NAME_LOCK_SPACE              = "*LCKSPC";
  /**
  Special value indicating that the system was unable to determine a value.
   **/
  public static final String VALUE_CANNOT_BE_DETERMINED       = "*N";

  
  // private member data representing an entry from the QWCLOBJL() API
  private String  jobName_;
  private String  jobUsername_;
  private String  jobNumber_;
  private String  lockState_;
  private int     lockStatus_;
  private int     lockType_;
  private String  memberName_;
  private String  share_;
  private String  lockScope_;
  private long    threadID_;

   
  // Make constructors package scoped - only constructed by UserObjectsOwnedList.getObjectLockList()
  // No verification of data is performed in the constructors, since  
  // the data for these parameters comes from the List Object Locks (QWCLOBJL) API.
  ObjectLockListEntry(String jobName, String jobUserName, String jobNumber, String lockState, int lockStatus, int lockType, String memberName, String share, String lockScope, long threadID)
  {
    jobName_      = jobName;
    jobUsername_  = jobUserName;
    jobNumber_    = jobNumber;
    lockState_    = lockState;
    lockStatus_   = lockStatus;
    lockType_     = lockType;
    memberName_   = memberName;
    share_        = share;
    lockScope_    = lockScope;
    threadID_     = threadID;
  }
  
  /**
  Returns the simple job name of the job that issued the lock request. 
   @return the job name. The following special values may be returned:
   <ul>
     <li>{@link #JOB_NAME_MACHINE JOB_NAME_MACHINE} - The lock is held by an internal machine process. If this value is returned, the job number and job user name will be blank.
     <li>{@link #JOB_NAME_LOCK_SPACE JOB_NAME_LOCK_SPACE} - The lock is attached to a lock space. If this value is returned, the job number and job user name will be blank.
     <li>{@link #VALUE_CANNOT_BE_DETERMINED VALUE_CANNOT_BE_DETERMINED} - The job name cannot be determined.
   </ul>
   **/
  public String getJobName()
  {
    return jobName_;
  }

  /**
   The user name under which the job that issued the lock request is run. 
   The user name is the same as the user profile name and can come from several different sources depending on the type of job.
   @return the job user name. The following special value may be returned:
   <ul>
     <li>{@link #VALUE_CANNOT_BE_DETERMINED VALUE_CANNOT_BE_DETERMINED} - The job user name cannot be determined.
   </ul>
   **/
  public String getJobUserName()
  {
    return jobUsername_;
  }
  
  /**
  The system-assigned job number of the job that issued the lock request. 
  @return the job number. The following special value may be returned:
  <ul>
     <li>{@link #VALUE_CANNOT_BE_DETERMINED VALUE_CANNOT_BE_DETERMINED} - The job number cannot be determined.
  </ul>
  **/
  public String getJobNumber()
  {
    return jobNumber_;
  }
  
  /**
  Returns the value indicating the lock state.
  @return lock state. Possible values:
   <ul>
     <li>{@link #LOCK_STATE_NONE LOCK_STATE_NONE} - Indicates no locks exist.
     <li>{@link #LOCK_STATE_SHARED_READ LOCK_STATE_SHARED_READ} - Indicates lock is shared for read.
     <li>{@link #LOCK_STATE_SHARED_UPDATE LOCK_STATE_SHARED_UPDATE} - Indicates lock is shared for update.
     <li>{@link #LOCK_STATE_SHARED_NO_UPDATE LOCK_STATE_SHARED_NO_UPDATE} - Indicates lock is shared for no update.
     <li>{@link #LOCK_STATE_EXCLUSIVE_ALLOW_READ LOCK_STATE_EXCLUSIVE_ALLOW_READ} - Indicates exclusive lock which allows read. 
     <li>{@link #LOCK_STATE_EXCLUSIVE_NO_READ LOCK_STATE_EXCLUSIVE_NO_READ} - Indicates exclusive lock with no read.
   </ul>
   **/
  public String getLockState()
  {
    
    if (!(lockState_.equals(LOCK_STATE_NONE)) &&
        !(lockState_.equals(LOCK_STATE_SHARED_READ)) &&
        !(lockState_.equals(LOCK_STATE_SHARED_UPDATE)) &&
        !(lockState_.equals(LOCK_STATE_SHARED_NO_UPDATE)) &&
        !(lockState_.equals(LOCK_STATE_EXCLUSIVE_ALLOW_READ)) &&
        !(lockState_.equals(LOCK_STATE_EXCLUSIVE_NO_READ)))
    {
      Trace.log(Trace.ERROR, "Invalid lock state: "+ lockState_);
      throw new InternalErrorException(InternalErrorException.UNKNOWN, lockState_);
    }
    return lockState_;
  }
  

  /**
  Returns the value indicating the lock status.
  @return lock status. Possible values:
   <ul>
     <li>{@link #LOCK_STATUS_LOCK_HELD LOCK_STATUS_LOCK_HELD} - The lock is currently held by the job or thread.
     <li>{@link #LOCK_STATUS_JOB_THREAD_WAITING_SYNC LOCK_STATUS_JOB_THREAD_WAITING_SYNC} - The job or thread is waiting for the lock (synchronous).
     <li>{@link #LOCK_STATUS_LOCK_REQUEST_OUTSTANDING_ASYNC LOCK_STATUS_LOCK_REQUEST_OUTSTANDING_ASYNC} - The job or thread has a lock request outstanding for the object (asynchronous).
   </ul>
   **/
  public int getLockStatus()
  { 
    if ((lockStatus_ != LOCK_STATUS_LOCK_HELD) &&
        (lockStatus_ != LOCK_STATUS_JOB_THREAD_WAITING_SYNC) &&
        (lockStatus_ != LOCK_STATUS_LOCK_REQUEST_OUTSTANDING_ASYNC))
    {
      Trace.log(Trace.ERROR, "Invalid lock status:", lockStatus_);
      throw new InternalErrorException(InternalErrorException.UNKNOWN, lockStatus_);
    }
    return lockStatus_;
  }
  
  /**
  Returns the value indicating the lock type.
  @return lock type. Possible values:
   <ul>
     <li>{@link #LOCK_TYPE_OBJECT LOCK_TYPE_OBJECT} - Lock on the object
     <li>{@link #LOCK_TYPE_MEMBER_CONTROL_BLOCK LOCK_TYPE_MEMBER_CONTROL_BLOCK} - Lock on the member control block.
     <li>{@link #LOCK_TYPE_ACCESS_PATH LOCK_TYPE_ACCESS_PATH} - Lock on the access path used to access a member's data
     <li>{@link #LOCK_TYPE_DATA_WITHIN_MEMBER LOCK_TYPE_DATA_WITHIN_MEMBER} - Lock on the actual data within the member.
   </ul>
   **/
  public int getLockType()
  {
    if ((lockType_ != LOCK_TYPE_OBJECT) &&
        (lockType_ != LOCK_TYPE_MEMBER_CONTROL_BLOCK) &&
        (lockType_ != LOCK_TYPE_ACCESS_PATH) &&
        (lockType_ != LOCK_TYPE_DATA_WITHIN_MEMBER))
    {
      Trace.log(Trace.ERROR, "Invalid lock type:", lockType_);
      throw new InternalErrorException(InternalErrorException.UNKNOWN, lockType_);
    }
    return lockType_;
  }
  
  /**
   Share. Whether shared file member locks are associated with the file member.
  @return lock share. Possible values:
   <ul>
     <li>{@link #LOCK_SHARE_FILE_NOT_SHARED LOCK_SHARE_FILE_NOT_SHARED} - The file is not shared, the file is a physical file, or the field is not applicable to object type.
     <li>{@link #LOCK_SHARE_FILE_SHARED LOCK_SHARE_FILE_SHARED} - The file is shared.
   </ul>
  **/
  public int getShare()
  {
    int intShare;
    try {
      intShare = Integer.parseInt(share_);
    }
    catch (NumberFormatException e)
    {
      Trace.log(Trace.ERROR, "Invalid number conversion for (" + share_ +")");
      throw new InternalErrorException(InternalErrorException.UNKNOWN, e.getMessage());
    }
    
    if ((intShare != LOCK_SHARE_FILE_NOT_SHARED) &&
        (intShare != LOCK_SHARE_FILE_SHARED))
    {
      Trace.log(Trace.ERROR, "Invalid lock share:", intShare);
      throw new InternalErrorException(InternalErrorException.UNKNOWN, intShare);
    }

    return intShare;
  }
  
  /**
  Returns the value indicating the lock scope.
  @return lock scope. Possible values:
   <ul>
     <li>{@link #LOCK_SCOPE_JOB LOCK_SCOPE_JOB} - Lock has job scope.
     <li>{@link #LOCK_SCOPE_THREAD LOCK_SCOPE_THREAD} - Lock has thread scope.
     <li>{@link #LOCK_SCOPE_LOCK_SPACE LOCK_SCOPE_LOCK_SPACE} - Lock has lock space scope
   </ul>
   **/
  public int getLockScope()
  {
    int intLockScope;
    try {
      intLockScope = Integer.parseInt(lockScope_);
    }
    catch (NumberFormatException e)
    {
      Trace.log(Trace.ERROR, "Invalid number conversion for (" + lockScope_ +")");
      throw new InternalErrorException(InternalErrorException.UNKNOWN, e.getMessage());
    }

    if ((intLockScope != LOCK_SCOPE_JOB) &&
        (intLockScope != LOCK_SCOPE_THREAD) &&
        (intLockScope != LOCK_SCOPE_LOCK_SPACE))
    {
      Trace.log(Trace.ERROR, "Invalid lock scope:", intLockScope);
      throw new InternalErrorException(InternalErrorException.UNKNOWN, intLockScope);
    }

    return intLockScope;
  }
  
  /**
  The identifier of the thread that is holding a thread-scoped lock or waiting for a lock. 
  @return a long representing the thread identifier.  For locks that do not have a lock scope of {@link #LOCK_SCOPE_THREAD LOCK_SCOPE_THREAD}, this value is not meaningful and will likely be zero.
  **/
  public long getThreadID()
  {
    return threadID_;
  }

  /**
   Returns a string representation of this object lock
   @return a string representing the lock by [job name/job user name/job number/thread ID]
   **/
  public String toString()
  {
    return "["+getJobName()+"/"+getJobUserName()+"/"+getJobNumber()+"/0x"+Long.toHexString(getThreadID()).toUpperCase()+"]";
  }
}
