///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400FileConstants.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
 *Contains constants formerly residing in AS400File.
 *Created so that dependencies on AS400File in the DDM datastream classes
 *can be removed, thereby reducing the size of the jar file generated
 *by JarMaker.
**/
interface AS400FileConstants
{
  //@A1A
  /**
   * Constant indicating a text description of *BLANK.
   *@see AS400File#create
  **/
  static public final String BLANK = "*BLANK";

  /**
   *Constant indicating a commit lock level of *ALL.
   *Every record accessed in the file is locked until the
   *transaction is committed or rolled back.
   *@see AS400File#startCommitmentControl
   **/
  static public final int COMMIT_LOCK_LEVEL_ALL = 0;

  /**
   *Constant indicating a commit lock level of *CHANGE.
   *Every record read for update is locked. If a record
   *is updated, added, or deleted, that record remains locked
   *until the transaction is committed or rolled back.  Records
   *that are accessed for update but are released without being
   *updated are unlocked.
   *@see AS400File#startCommitmentControl
   **/
  static public final int COMMIT_LOCK_LEVEL_CHANGE = 1;
  /**
   *Constant indicating a commit lock level of *CS.
   *Every record accessed is locked.  Records that are not
   *updated or deleted are locked only until a different record
   *is accessed.  Records that are updated, added, or deleted are locked
   *until the transaction is committed or rolled back.
   *@see AS400File#startCommitmentControl
   **/
  static public final int COMMIT_LOCK_LEVEL_CURSOR_STABILITY = 2;
  /**
   *Constant indicating that the commit lock level specified on the
   *startCommitmentControl() method should be used.
   *The record locking specified by the commitLockLevel parameter on the
   *startCommitmentControl() method will apply to transactions using this file.
   **/
  static public final int COMMIT_LOCK_LEVEL_DEFAULT = 4;
  /**
   *Constant indicating that no commitment control should be used for the file.
   *No commitment control will apply to this file.
   *@see AS400File#startCommitmentControl
   **/
  static public final int COMMIT_LOCK_LEVEL_NONE = 3;

  /**
   *Constant indicating lock type of read willing to share with
   *other readers.  This is the equivalent of specifying *SHRNUP
   *on the Allocate Object (ALCOBJ) command.
   *@see AS400File#lock
   **/
  static public final int READ_ALLOW_SHARED_READ_LOCK = 1;
  /**
   *Constant indicating lock type of read willing to share with
   *updaters.  This is the equivalent of specifying *SHRRD
   *on the Allocate Object (ALCOBJ) command.
   *@see AS400File#lock
   **/
  static public final int READ_ALLOW_SHARED_WRITE_LOCK = 0;
  /**
   *Constant indicating lock type of read willing to share with no one.
   *This is the equivalent of specifying *EXCL on the Allocate Object (ALCOBJ)
   *command.
   *@see AS400File#lock
   **/
  static public final int READ_EXCLUSIVE_LOCK = 4;
  /**
   *Constant indicating open type of read only.
   *@see AS400File#open
   **/
  static public final int READ_ONLY = 0;
  /**
   *Constant indicating open type of read/write.
   *@see AS400File#open
   **/
  static public final int READ_WRITE = 1;

  //@A1A
  /**
   * Constant indicating a text description of *SRCMBRTXT.
   *@see AS400File#create
  **/
  static public final String SOURCE_MEMBER_TEXT = "*SRCMBRTXT";

  //@A1A
  /**
   * Constant indicating a file type of *DATA.
   *@see AS400File#create
  **/
  static public final String TYPE_DATA = "*DATA";

  //@A1A
  /**
   * Constant indicating a file type of *SRC.
   *@see AS400File#create
  **/
  static public final String TYPE_SOURCE = "*SRC";

  /**
   *Constant indicating lock type of update willing to share with
   *readers.  This is the equivalent of specifying *EXCLRD
   *on the Allocate Object (ALCOBJ) command.
   *@see AS400File#lock
   **/
  static public final int WRITE_ALLOW_SHARED_READ_LOCK = 3;
  /**
   *Constant indicating lock type of update willing to share with
   *updaters.  This is the equivalent of specifying *SHRUPD
   *on the Allocate Object (ALCOBJ) command.
   *@see AS400File#lock
   **/
  static public final int WRITE_ALLOW_SHARED_WRITE_LOCK = 2;
  /**
   *Constant indicating lock type of update willing to share with
   *no one.  This is the equivalent of specifying *EXCL
   *on the Allocate Object (ALCOBJ) command.
   *@see AS400File#lock
   **/
  static public final int WRITE_EXCLUSIVE_LOCK = 5;
  /**
   *Constant indicating open type of write only.
   *@see AS400File#open
   **/
  static public final int WRITE_ONLY = 2;
}


