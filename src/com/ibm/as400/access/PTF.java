///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PTF.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Date;
import java.util.Locale;

/**
 * Represents a Program Temporary Fix (PTF) for a licensed program
 * product. Call {@link #refresh refresh()} to reload all of the values from the system.
 * Individual getters will only refresh their own necessary information.
 * @see com.ibm.as400.access.Product#getPTFs
**/
public class PTF
{
  private AS400 system_;
  private String productID_;
  private String returnedProductID_; // Product ID that comes back on the API call.
  private boolean actionPending_;
  private String actionRequired_;
  private boolean hasCoverLetter_;
  private int iplAction_;
  private String iplRequired_;
  private String loadedStatus_;
  private String maximumLevel_;
  private String minimumLevel_;
  private boolean ptfOnOrder_;
  private String ptfProductLoad_;
  private String ptfProductOption_;
  private String ptfID_;
  private boolean isPTFReleased_;
  private String ptfReleaseLevel_;
  private String returnedReleaseLevel_; // Release level that comes back on the API call.
  private boolean saveFileExists_;
  private Date statusDate_;

  private String currentIPLSource_;
  private String licGroup_;
  private String saveFile_;  
  private String supersedingPTF_;
  private String targetRelease_;
  //private String supersededByPTFID_;      // V5R2
  private String currentServerIPLSource_; // V5R3
  private int serverIPLRequired_ = -1;    // V5R3
  private String creationDateAndTime_;    // V5R3

  private boolean loaded_ = false;
  private boolean partiallyLoaded_ = false;
  private boolean partiallyLoadedGroup_ = false;     //@K1A
  private boolean loaded200_ = false;
  private boolean loaded300_ = false;
  private boolean loaded500_ = false;
  private boolean loaded600_ = false;
  private boolean loaded700_ = false;
  private boolean loaded800_ = false;
//  private boolean loaded900_ = false;
  private int chunkSize_ = 8192;

  // PTFR0200
  private PTFCoverLetter[] coverLetters_;

  // PTFR0300
  private PTF[] requisites_;
  private boolean isPreRequisite_; // type
  private boolean isCoRequisite_; // type
  private boolean isConditional_;
  private boolean isRequired_;

  // PTFR0500
  private PTF[] dependents_;
  private boolean isDependent_; // type

  private boolean loadedRequisites_ = false;
  private boolean loadedDependents_ = false;

  // PTFR0600
  private String[] apars_;

  // PTFR0700
  private String[] symptoms_;

  // PTFR0800
  private PTFExitProgram[] exitPrograms_;

  // PTFR0900
//  private PTFPrecondition[] preconditions_;

  private String messageData_; // This is loaded from CPX3501 and contains the translated text.


  
  /**
   * Constant indicating that no action is required.
  **/
  public static final String ACTION_NOT_REQUIRED = "0";
  
  /**
   * Constant indicating that the PTF contains activation instructions
   * in the cover letter. The PTF has an exit program to update the
   * status of the PTF after the activation instructions have been
   * performed.
  **/
  public static final String ACTION_REQUIRED_EXIT_PROGRAM = "1";
  
  /**
   * Constant indicating that the PTF contains activation instructions
   * in the cover letter, but no exit program exists to verify the
   * activation instructions were performed.
  **/
  public static final String ACTION_REQUIRED_CANNOT_VERIFY = "2";
  
  
  
  /**
   * Constant indicating no action will occur at the next IPL.
  **/
  public static final int IPL_ACTION_NONE = 0;
  
  /**
   * Constant indicating the PTF will be temporarily applied at the next IPL.
  **/
  public static final int IPL_ACTION_APPLY_TEMPORARY = 1;
  
  /**
   * Constant indicating the PTF will be temporarily removed at the next IPL.
  **/
  public static final int IPL_ACTION_REMOVE_TEMPORARY = 2;
  
  /**
   * Constant indicating the PTF will be permanently applied at the next IPL.
  **/
  public static final int IPL_ACTION_APPLY_PERMANENT = 3;
  
  /**
   * Constant indicating the PTF will be permanently removed at the next IPL.
  **/
  public static final int IPL_ACTION_REMOVE_PERMANENT = 4;
  
  
  
  /**
   * Constant indicating the system is currently operating on the A IPL source.
  **/
  public static final String IPL_SOURCE_A = "A";
  
  /**
   * Constant indicating the system is currently operating on the B IPL source.
  **/
  public static final String IPL_SOURCE_B = "B";
  
  /**
   * Constant indicating the current IPL source could not be determined.
  **/
  public static final String IPL_SOURCE_UNKNOWN = " ";
  
  
  
  /**
   * Constant representing a product ID of *ONLY.
  **/
  public static final String PRODUCT_ID_ONLY = "*ONLY";
  
  /**
   * Constant representing a release level of *ONLY.
  **/
  public static final String PRODUCT_RELEASE_ONLY = "*ONLY";


  
  /**
   * Constant indicating that the PTF is delayed and must be applied at IPL time.
  **/
  public static final String PTF_TYPE_DELAYED = "0";
  
  /**
   * Constant indicating that the PTF is immediate and can be applied immediately.
   * No IPL is needed.
  **/
  public static final String PTF_TYPE_IMMEDIATE = "1";
  
  /**
   * Constant indicating that the PTF type is not known.
  **/
  public static final String PTF_TYPE_UNKNOWN = " ";
  
  
  
  /**
   * Constant indicating that there is a co-requisite relationship between two PTFs.
   * @see #getRelationship
  **/
  public static final String RELATIONSHIP_COREQ = "*COREQ";
  
  /**
   * Constant indicating that there is a dependent (pre-requisite) relationship between two PTFs.
   * @see #getRelationship
  **/
  public static final String RELATIONSHIP_DEPEND = "*DEPEND";
  
  /**
   * Constant indicating that there is no known relationship between two PTFs.
   * @see #getRelationship
  **/
  public static final String RELATIONSHIP_NONE = "*NONE";
  
  /**
   * Constant indicating that there is a pre-requisite relationship between two PTFs.
   * @see #getRelationship
  **/
  public static final String RELATIONSHIP_PREREQ = "*PREREQ";
  
  /**
   * Constant indicating that two PTFs are identical.
   * @see #getRelationship
  **/
  public static final String RELATIONSHIP_SAME = "*SAME";
  
  
  
  /**
   * Constant indicating that a PTF is not loaded.
  **/
  public static final String STATUS_NOT_LOADED = "0";
  
  /**
   * Constant indicating that a PTF is loaded.
  **/
  public static final String STATUS_LOADED = "1";
  
  /**
   * Constant indicating that a PTF is applied.
  **/
  public static final String STATUS_APPLIED = "2";
  
  /**
   * Constant indicating that a PTF is permanently applied.
  **/
  public static final String STATUS_APPLIED_PERMANENT = "3";
  
  /**
   * Constant indicating that a PTF is permanently removed.
  **/
  public static final String STATUS_REMOVED_PERMANENT = "4";
  
  /**
   * Constant indicating that a PTF is damaged.
  **/
  public static final String STATUS_DAMAGED = "5";
  
  /**
   * Constant indicating that a PTF is superseded.
  **/
  public static final String STATUS_SUPERSEDED = "6";
  
  
  
  /**
   * Constructs a PTF object. The product ID defaults to PRODUCT_ID_ONLY
   * and the release level defaults to PRODUCT_RELEASE_ONLY.
   * @param system The system.
   * @param ptfID The PTF ID (e.g. "SF64578")
  **/
  public PTF(AS400 system, String ptfID)
  {
    this(system, ptfID, PRODUCT_ID_ONLY, PRODUCT_RELEASE_ONLY);
  }

  
  /**
   * Constructs a PTF object.
   * @param system The system.
   * @param ptfID The PTF ID (e.g. "SF64578")
   * @param productID The product ID (e.g. "5722JC1"). This value must
   * either be {@link #PRODUCT_ID_ONLY PRODUCT_ID_ONLY} or a valid product ID.
   * @param releaseLevel The PTF release level (e.g. "V5R1M0"). This value
   * must either be {@link #PRODUCT_RELEASE_ONLY PRODUCT_RELEASE_ONLY} or a valid release level.
  **/
  public PTF(AS400 system, String ptfID, String productID, String releaseLevel)
  {
    if (system == null) throw new NullPointerException("system");
    if (ptfID == null) throw new NullPointerException("ptfID");
    if (productID == null) throw new NullPointerException("productID");
    if (releaseLevel == null) throw new NullPointerException("releaseLevel");

    system_ = system;
    String id = ptfID.toUpperCase().trim();
    if (id.length() != 7)
    {
      throw new ExtendedIllegalArgumentException("ptfID", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    String prod = productID.toUpperCase().trim();
    if (prod.length() != 7 && !prod.equals(PRODUCT_ID_ONLY))
    {
      throw new ExtendedIllegalArgumentException("productID", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    String release = releaseLevel.toUpperCase().trim();
    if (!prod.equals(PRODUCT_RELEASE_ONLY) && release.length() != 6)
    {
      throw new ExtendedIllegalArgumentException("releaseLevel", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    ptfID_ = id;
    productID_ = prod;
    ptfReleaseLevel_ = release;
  }

  
  /**
   * Package scope constructor called by the others.
  **/
  PTF(AS400 system, String productID, String ptfID, String ptfReleaseLevel, String ptfOption, String ptfFeature)
  {
    system_ = system;
    productID_ = productID;
    ptfID_ = ptfID;
    ptfReleaseLevel_ = ptfReleaseLevel;
    ptfProductOption_ = ptfOption;
    ptfProductLoad_ = ptfFeature;
  }

  
  /**
   * Package scope constructor used for dependent PTFs.
  **/
  PTF(AS400 system, String productID, String ptfID, String ptfReleaseLevel, String ptfProductOption, String ptfProductLoad,
      String minLevel, String maxLevel, boolean type)
  {
    this(system, productID, ptfID, ptfReleaseLevel, ptfProductOption, ptfProductLoad);
    minimumLevel_ = minLevel;
    maximumLevel_ = maxLevel;
    isDependent_ = type;
    isCoRequisite_ = !type;
    loadedDependents_ = true;
  }

  
  /**
   * Package scope constructor used for requisite PTFs.
  **/
  PTF(AS400 system, String productID, String ptfID, String ptfReleaseLevel, String ptfProductOption, String ptfProductLoad,
      String minLevel, String maxLevel, boolean type, boolean cond, boolean reqRequired)
  {
    this(system, productID, ptfID, ptfReleaseLevel, ptfProductOption, ptfProductLoad);
    minimumLevel_ = minLevel;
    maximumLevel_ = maxLevel;
    isPreRequisite_ = type;
    isCoRequisite_ = !type;
    isConditional_ = cond;
    isRequired_ = reqRequired;
    loadedRequisites_ = true;
  }


  /**
   * Package scope constructor used by Product.getPTFs().
  **/
  PTF(AS400 system, String productID, String ptfID, String ptfReleaseLevel, String ptfProductOption, String ptfProductLoad,
      String loadedStatus, boolean saveFileExists, boolean hasCoverLetter, boolean ptfOnOrder,
      int iplAction, boolean actionPending, String actionRequired, String iplRequired,
      boolean isPTFReleased, String minimumLevel, String maximumLevel, Date statusDate)
  {
    this(system, productID, ptfID, ptfReleaseLevel, ptfProductOption, ptfProductLoad);
    loadedStatus_ = loadedStatus;
    saveFileExists_ = saveFileExists;
    hasCoverLetter_ = hasCoverLetter;
    ptfOnOrder_ = ptfOnOrder;
    iplAction_ = iplAction;
    actionPending_ = actionPending;
    actionRequired_ = actionRequired;
    iplRequired_ = iplRequired;
    isPTFReleased_ = isPTFReleased;
    minimumLevel_ = minimumLevel;
    maximumLevel_ = maximumLevel;
    statusDate_ = statusDate;
    partiallyLoaded_ = true;
  }

  //@K1A
  /**
   * Package scope constructor used by PTFGroup.getPTFs().
  **/
  PTF(AS400 system, String ptfID, String productID, String ptfReleaseLevel, String ptfProductOption, String ptfProductLoad,
      String minimumLevel, String maximumLevel, String loadedStatus, int iplAction, String actionPending, String actionRequired,
      String coverLetterStatus, String onOrderStatus, String saveFileStatus, String saveFileName, String saveFileLibraryName,
      String supersededByPTFId, String latestSupersedingPTFId, String productStatus)
  {
    this(system, productID, ptfID, ptfReleaseLevel, ptfProductOption, ptfProductLoad);
    //currently no getters for 'Save file library name', 'latest superseding ptf', 'product status'
    loadedStatus_ = loadedStatus;
    saveFileExists_ = saveFileStatus.equals("0") ? false : true;
    hasCoverLetter_ = coverLetterStatus.equals("0") ? false : true;
    ptfOnOrder_ = onOrderStatus.equals("0") ? false : true;
    iplAction_ = iplAction;
    actionPending_ = actionPending.equals("0") ? false : true;
    actionRequired_ = actionRequired;
    minimumLevel_ = minimumLevel;
    maximumLevel_ = maximumLevel;
    saveFile_ = saveFileName;
    supersedingPTF_ = supersededByPTFId;
    partiallyLoadedGroup_ = true;

  }

  
  /**
   * Returns the action required to make this PTF active when it is applied.
   * See the cover letter to determine what action needs to be taken. Possible
   * return values are:
   * <UL>
   * <LI>{@link #ACTION_NOT_REQUIRED ACTION_NOT_REQUIRED}
   * <LI>{@link #ACTION_REQUIRED_EXIT_PROGRAM ACTION_REQUIRED_EXIT_PROGRAM}
   * <LI>{@link #ACTION_REQUIRED_CANNOT_VERIFY ACTION_REQUIRED_CANNOT_VERIFY}
   * <UL>
   * @return The action required.
  **/
  public String getActionRequired()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_ && !partiallyLoadedGroup_) refresh(100);      //@K1C
    return actionRequired_;
  }

  
  /** 
   * Retrieves the list of APAR numbers that were fixed by this PTF.
   * @return The APAR numbers.
   * @see #getSymptomStrings
  **/
  public String[] getAPARNumbers()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded600_) refresh(600);
    return apars_;
  }

     
  /**
   * Retrieves the cover letter for this PTF from the system.
   * The cover letter returned is for the default NLV for the system.
   * If there are no cover letters, this method returns null.
   * @return The cover letter.
  **/
  public PTFCoverLetter getCoverLetter()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    return getCoverLetter(system_.getLocale());
  }


  /**
   * Retrieves the cover letter for this PTF from the system based on the given locale.
   * The cover letter returned is for the NLV that corresponds to the given locale.
   * If there is no cover letter that corresponds to the determined NLV, the first one
   * retrieved from the system is returned. If there are no cover letters, null is returned.
   * @param locale The locale.
   * @return The cover letter.
  **/
  public PTFCoverLetter getCoverLetter(Locale locale)
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    PTFCoverLetter[] letters = getCoverLetters();
    if (letters.length > 0)
    {
      String nlv = ExecutionEnvironment.getNlv(locale);
      for (int i=0; i<letters.length; ++i)
      {
        if (letters[i].getNLV().equals(nlv))
        {
          return letters[i];
        }
      }
      return letters[0];
    }
    return null;
  }


  /**
   * Retrieves the cover letters for this PTF from the system, if they exist.
   * Each cover letter is for its own national language version (NLV).
   * @return The array of cover letters. If there are no cover letters, an array
   * of size 0 is returned.
  **/
  public PTFCoverLetter[] getCoverLetters()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (hasCoverLetter())
    {
      if (!loaded200_) refresh(200);
      return coverLetters_;
    }
    return new PTFCoverLetter[0];
  }


  /**
   * Returns the copy of Licensed Internal Code that the system is currently
   * operating from. The previous IPL of the system used this copy of Licensed
   * Internal Code. Possible values are:
   * <UL>
   * <LI>{@link #IPL_SOURCE_A IPL_SOURCE_A}
   * <LI>{@link #IPL_SOURCE_B IPL_SOURCE_B}
   * <LI>{@link #IPL_SOURCE_UNKNOWN IPL_SOURCE_UNKNOWN}
   * </UL>
   * @return The current IPL source.
  **/
  public String getCurrentIPLSource()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_) refresh(100);
    return currentIPLSource_;
  }

  
  /**
   * Retrieves the list of PTFs that are dependent upon this PTF.
   * If there are no dependent PTFs, an array of size 0 will be returned.
   * @return The array of dependent PTFs.
   * @see #getRequisitePTFs
   * @see #getSupersedingPTF
  **/
  public PTF[] getDependentPTFs()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded500_) refresh(500);
    return dependents_;
  }

  
  /**
   * Retrieves the list of exit programs for this PTF.
   * @return The array of exit programs.
  **/
  public PTFExitProgram[] getExitPrograms()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded800_) refresh(800);
    return exitPrograms_;
  }


  /**
   * Returns the ID number for this PTF (e.g. "SF64578").
   * @return The PTF ID.
  **/
  public String getID()
  {
    return ptfID_;
  }

  
  /**
   * Returns the action to be taken on this PTF during the next IPL.
   * Possible values are:
   * <UL>
   * <LI>{@link #IPL_ACTION_NONE IPL_ACTION_NONE}
   * <LI>{@link #IPL_ACTION_APPLY_TEMPORARY IPL_ACTION_APPLY_TEMPORARY}
   * <LI>{@link #IPL_ACTION_REMOVE_TEMPORARY IPL_ACTION_REMOVE_TEMPORARY}
   * <LI>{@link #IPL_ACTION_APPLY_PERMANENT IPL_ACTION_APPLY_PERMANENT}
   * <LI>{@link #IPL_ACTION_REMOVE_PERMANENT IPL_ACTION_REMOVE_PERMANENT}
   * </UL>
   * @return The IPL action.
  **/
  public int getIPLAction()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_ && !partiallyLoadedGroup_) refresh(100);      //@K1C
    return iplAction_;
  }

  
  /**
   * Returns the type of PTF (delayed or immediate).
   * Possible values are:
   * <UL>
   * <LI>{@link #PTF_TYPE_DELAYED PTF_TYPE_DELAYED}
   * <LI>{@link #PTF_TYPE_IMMEDIATE PTF_TYPE_IMMEDIATE}
   * <LI>{@link #PTF_TYPE_UNKNOWN PTF_TYPE_UNKNOWN}
   * </UL>
   * @return The type of PTF. This indicates if an IPL is required to apply the PTF.
  **/
  public String getIPLRequired()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh(100);
    return iplRequired_;
  }

  
  /**
   * Returns the name of the Licensed Internal Code Group for this PTF. If the name
   * of the group is not available or if the PTF is not a Licensed Internal Code
   * fix, this method returns "".
   * @return The Licensed Interanl Code Group name.
  **/
  public String getLICGroup()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_) refresh(100);
    return licGroup_;
  }

     
  /**
   * Returns the current loaded status of this PTF. See the
   * {@link #getLoadedStatusMessage getLoadedStatusMessage()} method
   * for the translated description text of the loaded status.
   * Possible values are:
   * <UL>
   * <LI>{@link #STATUS_NOT_LOADED STATUS_NOT_LOADED}
   * <LI>{@link #STATUS_LOADED STATUS_LOADED}
   * <LI>{@link #STATUS_APPLIED STATUS_APPLIED}
   * <LI>{@link #STATUS_APPLIED_PERMANENT STATUS_APPLED_PERMANENT}
   * <LI>{@link #STATUS_REMOVED_PERMANENT STATUS_REMOVED_PERMANENT}
   * <LI>{@link #STATUS_DAMAGED STATUS_DAMAGED}
   * <LI>{@link #STATUS_SUPERSEDED STATUS_SUPERSEDED}
   * </UL>
   * @return The loaded status.
   * @see #getLoadedStatusMessage
  **/
  public String getLoadedStatus()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_ && !partiallyLoadedGroup_) refresh(100);      //@K1C
    return loadedStatus_;
  }

  
  /**
   * Returns the translated description text for the specified status.
   * The text is retrieved from the CPX3501 message on the system.
   * @param loadedStatus The loaded status. See {@link #getLoadedStatus getLoadedStatus()}
   * for the list of valid values.
   * @return The status message, or "" if the loaded status was not valid.
  **/
  public String getLoadedStatusMessage(String loadedStatus)
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (messageData_ == null)
    {
      try
      {
        MessageFile mf = new MessageFile(system_, "/QSYS.LIB/QCPFMSG.MSGF");
        AS400Message msg = mf.getMessage("CPX3501");
        if (msg != null) messageData_ = msg.getHelp();
      }
      catch (PropertyVetoException pve) {}  // will never happen
    }
    // String, offset, length
    // NONE, 0, 11
    // DAMAGED, 11, 16
    // SUPERSEDED, 27, 20
    // TEMPORARILY APPLIED, 47, 35
    // NOT APPLIED, 82, 22
    // PERMANENTLY APPLIED, 104, 35
    // ON ORDER ONLY, 139, 25
    // SAVE FILE ONLY, 164, 27
    // COVER LETTER ONLY, 191, 32
    // TEMPORARILY APPLIED - ACN, 223, 43
    // TEMPORARILY REMOVED - ACN, 266, 43
    // PERMANENTLY REMOVED - ACN, 309, 43
    // PERMANENTLY APPLIED - ACN, 352, 43
    // TEMPORARILY APPLIED - PND, 395, 43
    // TEMPORARILY REMOVED - PND, 438, 43
    // PERMANENTLY REMOVED - PND, 481, 43
    // PERMANENTLY APPLIED - PND, 524, 43
    // PERMANENTLY REMOVED, 567, 35
    if (loadedStatus.equals(STATUS_NOT_LOADED))
    {
      return messageData_.substring(0, 11).trim();
    }
    if (loadedStatus.equals(STATUS_DAMAGED))
    {
      return messageData_.substring(11, 27).trim();
    }
    else if (loadedStatus.equals(STATUS_SUPERSEDED))
    {
      return messageData_.substring(27, 47).trim();
    }
    else if (loadedStatus.equals(STATUS_APPLIED))
    {
      return messageData_.substring(47, 82).trim(); // temporarily applied
    }
    else if (loadedStatus.equals(STATUS_APPLIED_PERMANENT))
    {
      return messageData_.substring(104, 139).trim();
    }
    else if (loadedStatus.equals(STATUS_LOADED))
    {
      return messageData_.substring(82, 104).trim(); // not applied
    }
    else if (loadedStatus.equals(STATUS_REMOVED_PERMANENT))
    {
      return messageData_.substring(567, 602).trim();
    }
    return "";
  }

  
  /**
   * Returns the highest release level of the product on which this
   * PTF can be installed. If the minimum and maximum levels are the
   * same, this PTF can only be installed on one level of the product.
   * The level can be "AA" through "99", or blank if the product has
   * no level.
   * @return The release level.
   * @see #getMinimumLevel
  **/
  public String getMaximumLevel()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_ && !partiallyLoadedGroup_) refresh(100);      //@K1C
    return maximumLevel_;
  }

  
  /**
   * Returns the lowest release level of the product on which this
   * PTF can be installed. If the minimum and maximum levels are the
   * same, this PTF can only be installed on one level of the product.
   * The level can be "AA" through "99", or blank if the product has
   * no level.
   * @return The release level.
   * @see #getMaximumLevel
  **/
  public String getMinimumLevel()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_ && !partiallyLoadedGroup_) refresh(100);      //@K1C
    return minimumLevel_;
  }

 
  /**
   * Retrieves the list of preconditions for this PTF.
   * @return The array of preconditions.
  **/
/*  public PTFPrecondition[] getPreconditions()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded900_) refresh(900);
    return preconditions_;
  }
*/

  /**
   * Returns the product feature to which this PTF applies. This
   * value will be blank if the feature cannot be determined (as in
   * the case of a dependent or requisite PTF).
   * @return The product feature.
  **/
  public String getProductFeature()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh(100);
    return ptfProductLoad_;
  }

     
  /**
   * Returns the product ID of this PTF.  For example: "5722JC1"
   * If this value was initially set to PRODUCT_ID_ONLY, it
   * will be overwritten with the value returned from the system
   * after the values have been refreshed.
   * @return The product ID.
  **/
  public String getProductID()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (returnedProductID_ == null) refresh(100);
    return returnedProductID_;
  }

     
  /**
   * Returns the product option to which this PTF applies. This value
   * will be blank if the option cannot be determined (as in the case
   * of a dependent or requisite PTF).
   * @return The product option.
  **/ 
  public String getProductOption()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_ && !partiallyLoadedGroup_) refresh(100);  //@K1C
    return ptfProductOption_;
  }

  
  /**
   * Returns the relationship between this PTF and another PTF.
   * @param ptf The PTF to compare.
   * @return The relationship between the two PTFs. Possible values are:
   * <UL>
   * <LI>{@link #RELATIONSHIP_PREREQ RELATIONSHIP_PREREQ} - If this PTF is a pre-requisite of the specified PTF.
   * <LI>{@link #RELATIONSHIP_COREQ RELATIONSHIP_COREQ} - If this PTF is a co-requisite of the specified PTF.
   * <LI>{@link #RELATIONSHIP_DEPEND RELATIONSHIP_DEPEND} - If the specified PTF is a pre-requisite of this PTF.
   * <LI>{@link #RELATIONSHIP_SAME RELATIONSHIP_SAME} - If this PTF is identical to the specified PTF.
   * <LI>{@link #RELATIONSHIP_NONE RELATIONSHIP_NONE} - If there is no known relationship.
   * </UL>
  **/
  public String getRelationship(PTF ptf)
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    // * <LI>{@link #RELATIONSHIP_DIST RELATIONSHIP_DIST} - If this PTF is a distribution requisite of the specified PTF.
    String id = getID();
    String id2 = ptf.getID();
    if (id.equals(id2)) return RELATIONSHIP_SAME;

    refresh(300);
    refresh(500);
    ptf.refresh(300);
    ptf.refresh(500);
    PTF[] requisites = getRequisitePTFs();
    PTF[] dependents = getDependentPTFs();
    PTF[] req2 = ptf.getRequisitePTFs();
    PTF[] dep2 = ptf.getDependentPTFs();
    for (int i=0; i<requisites.length; ++i)
    {
      if (requisites[i].getID().equals(id2))
      {
        if (requisites[i].isCoRequisite()) return RELATIONSHIP_COREQ;
        return RELATIONSHIP_DEPEND;
      }
    }
    for (int i=0; i<dependents.length; ++i)
    {
      if (dependents[i].getID().equals(id2))
      {
        if (dependents[i].isCoRequisite()) return RELATIONSHIP_COREQ;
        return RELATIONSHIP_PREREQ;
      }
    }
    for (int i=0; i<req2.length; ++i)
    {
      if (req2[i].getID().equals(id))
      {
        if (req2[i].isCoRequisite()) return RELATIONSHIP_COREQ;
        return RELATIONSHIP_PREREQ;
      }
    }
    for (int i=0; i<dep2.length; ++i)
    {
      if (dep2[i].getID().equals(id))
      {
        if (dep2[i].isCoRequisite()) return RELATIONSHIP_COREQ;
        return RELATIONSHIP_DEPEND;
      }
    }
    return RELATIONSHIP_NONE;
  }

  /**
   * Returns the release level of this PTF (e.g. "V5R1M0").
   * If this value was initially set to PRODUCT_RELEASE_ONLY, it
   * will be overwritten with the value returned from the system
   * after the values have been refreshed.
   * @return The release level.
  **/
  public String getReleaseLevel()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (returnedReleaseLevel_ == null) refresh(100);
    return returnedReleaseLevel_;
  }

     
  /**
   * Retrieves the list of pre- and co-requisite PTFs for this PTF.
   * To determine whether the PTFs returned by this method are
   * co-requisites or pre-requisites with this PTF object, call their
   * respective {@link #isCoRequisite isCoRequisite()} and {@link #isPreRequisite isPreRequisite()}
   * methods.
   * @return The list of PTFs.
  **/
  public PTF[] getRequisitePTFs()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded300_) refresh(300);
    return requisites_;
  }


  /**
   * Returns the full pathname of the save file for this PTF, if one exists.
   * @return The save file, or null if this PTF has no save file on the system.
  **/
  public String getSaveFile()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (hasSaveFile())
    {
      if (!loaded_) refresh(100);
      return saveFile_;
    }
    return null;
  }


  /**
   * Returns the date and time the PTF status last changed.
   * If the status date and time are not available, null is returned.
   * @return The status date.
  **/
  public Date getStatusDate()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh(100);
    return statusDate_;
  }

  
  /**
   * Returns the PTF ID of the PTF that supersedes this PTF. This will be ""
   * if there is no superseding PTF, or if the superseding PTF is not known.
   * @return The PTF ID.
   * @see #getDependentPTFs
   * @see #getRequisitePTFs
  **/
  public String getSupersedingPTF()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_) refresh(100);
    return supersedingPTF_;
  }

  
  /**
   * Returns the list of symptom strings for the problems fixed by this PTF.
   * @return The symptom strings.
   * @see #getAPARNumbers
  **/
  public String[] getSymptomStrings()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded700_) refresh(700);
    return symptoms_;
  }

  
  /**
   * Returns the earliest release of the operating system on which you can load and apply
   * this PTF (e.g. "V4R5M0").
   * @return The target release.
  **/
  public String getTargetRelease()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_) refresh(100);
    return targetRelease_;
  }


  /**
   * Returns the date and time that the PTF was created.
   * If the creation date and time cannot be determined, null is returned.
   * <p>NOTE:  This method is not supported when running to OS/400 V5R2 or earlier releases.
   * @return The date and time that the PTF was created, or null if not determined or system is pre-V5R3.
  **/
  public Date getCreationDate()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_) refresh(100);

    TimeZone tz = DateTimeConverter.timeZoneForSystem(system_);
    Calendar dateTime = Calendar.getInstance(tz);
    dateTime.clear();

    // CYYMMDDHHMMSS format.
    String dattim = creationDateAndTime_;  // abbreviate
    if (dattim == null) return null;
    else
    {
      dateTime.set(Integer.parseInt(dattim.substring(0, 3)) + 1900,
                   Integer.parseInt(dattim.substring(3, 5)) - 1,
                   Integer.parseInt(dattim.substring(5, 7)),
                   Integer.parseInt(dattim.substring(7, 9)),
                   Integer.parseInt(dattim.substring(9, 11)),
                   Integer.parseInt(dattim.substring(11, 13)));

      return dateTime.getTime();
    }
  }
  
  /**
   Indicates whether a server IPL must be performed in order to activate
   the changes for the PTF.
   <p>NOTE:  This method is not supported when running to OS/400 V5R2 or earlier releases.
   @return Whether a server IPL must be performed.
   The possible values are:
   <ul>
   <li>0 No server IPL is required to activate the changes for the PTF.
   <li>1 A server IPL must be performed using the T server IPL source in order to activate the changes for the PTF.
   <li>2 A server IPL must be performed using the P server IPL source in order to activate the changes for the PTF.
   <li>-1 The value of the "IPL required" property cannot be determined, or system is pre-V5R3.
   </ul>
  **/
  public int getServerIPLRequired()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    return serverIPLRequired_;
  }
  
  /**
   * This method is used internally by getCoverLetters().
   * getCoverLetters() just returns an array of size 0
   * if hasCoverLetter() is false.
  **/
  boolean hasCoverLetter()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_ && !partiallyLoadedGroup_) refresh(100);      //@K1C
    return hasCoverLetter_;
  }

  
  /**
   * This method is used internally by getSaveFile().
   * getSaveFile() just returns null if hasSaveFile() is false.
  **/
  boolean hasSaveFile()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_ && !partiallyLoadedGroup_) refresh(100);      //@K1C
    return saveFileExists_;
  }

  
  /**
   * Indicates if a required action has yet to be performed to make
   * this PTF active. If true, check the activation instructions in
   * the cover letter to determine what the action is.
   * @return true if a required action needs to occur for this PTF
   * to be active; false if no required actions are pending for this
   * PTF.
   * @see #getActionRequired
   * @see #getCoverLetters
  **/ 
  public boolean isActionPending()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_ && !partiallyLoadedGroup_) refresh(100);      //@K1C
    return actionPending_;
  }

  
  /**
   * Indicates if this PTF has a conditional relationship
   * with any of its dependents or requisites. This determines whether
   * it is necessary to check the system for the presence of software
   * that is described by this product ID, release level, option,
   * and feature.
   * @return true if the requisite PTF is required by this PTF only on
   * systems that contain the software described in the other fields;
   * false if the requisite PTF is required by this PTF on all systems
   * that can use this PTF.
  **/
  public boolean isConditional()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loadedRequisites_ && !loaded300_) refresh(300);
    return isConditional_;
  }

  
  /**
   * Indicates if this PTF has a co-requisite relationship with another PTF.
   * <P>
   * If this PTF object was returned by a call to {@link #getRequisitePTFs getRequisitePTFs()} or
   * {@link #getDependentPTFs getDependentPTFs()},
   * then this method indicates if this PTF has a co-requisite relationship with the
   * PTF of which it is a requisite or dependent.
   * <P>
   * If this PTF object was constructed otherwise, then this method indicates if this
   * PTF has a known co-requisite relationship with at least one other PTF on the system. This
   * is accomplished by checking the list of known dependent and requisite PTFs for this PTF.
   *                                                                                          
   * @return true if this PTF is a co-requisite, false otherwise.
   * @see #getDependentPTFs
   * @see #getRequisitePTFs
   * @see #isDependent
   * @see #isPreRequisite
  **/
  public boolean isCoRequisite()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loadedRequisites_ && !loadedDependents_ && !loaded300_)
    {
      refresh(300);
      refresh(500);
    }
    return isCoRequisite_;
  }

  
  /**
   * Indicates if this PTF has a dependent relationship with another PTF.
   * <P>
   * If this PTF object was returned by a call to {@link #getDependentPTFs getDependentPTFs()},
   * then this method indicates if the PTF that generated this PTF object is a
   * pre-requisite of this PTF object.
   * <P>
   * If this PTF object was constructed otherwise, then this method indicates if this
   * PTF is a known dependent of at least one other PTF on the system.
   *                                                                                          
   * @return true if this PTF is dependent on another PTF (that is, another PTF is
   * a pre-requisite of this PTF), false otherwise.
   * @see #getDependentPTFs
   * @see #getRequisitePTFs
   * @see #isCoRequisite
   * @see #isPreRequisite
  **/
  public boolean isDependent()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loadedDependents_ && !loaded300_) refresh(300);
    return isDependent_;
  }

  
  /**
   * Indicates if this PTF has been ordered.
   * @return true if the PTF has been ordered; false if it has not been ordered
   * or has already been received.
  **/
  public boolean isOnOrder()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_ && !partiallyLoadedGroup_) refresh(100);          //@K1C
    return ptfOnOrder_;
  }

  
  /**
   * Indicates if this PTF has a pre-requisite relationship with another PTF.
   * <P>
   * If this PTF object was returned by a call to {@link #getRequisitePTFs getRequisitePTFs()},
   * then this method indicates if this PTF is a pre-requisite for the PTF object
   * that generated this PTF.
   * <P>
   * If this PTF object was constructed otherwise, then this method indicates if this
   * PTF is a known pre-requisite of at least one other PTF on the system.
   *                                                                                          
   * @return true if this PTF is a pre-requisite of another PTF, false otherwise.
   * @see #getDependentPTFs
   * @see #getRequisitePTFs
   * @see #isCoRequisite
   * @see #isDependent
  **/
  public boolean isPreRequisite()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loadedRequisites_ && !loaded500_) refresh(500);
    return isPreRequisite_;
  }

  
  /**
   * Indicates if the PTF save file is available for distribution to other systems.
   * This is true only when the System Manager licensed product is on the system and
   * the product is supported. The save file status should also be checked.
   * @return true if the PTF save file is released and can be distributed; false if
   * the save file cannot be distributed.
   * @see #getSaveFile
  **/
  public boolean isReleased()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loaded_ && !partiallyLoaded_) refresh(100);
    return isPTFReleased_;
  }

  
  /**
   * Indicates if this PTF is required on the system because it is a pre-requisite
   * for another PTF.
   * @return true if this PTF is required on the system, false if it is not required.
  **/ 
  public boolean isRequired()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (!loadedRequisites_ && !loaded300_) refresh(300);
    return isRequired_;
  }


  /**
   * Refreshes all the values for this PTF by retrieving them from the system.
  **/
  public void refresh()
       throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              InterruptedException,
              IOException,
              ObjectDoesNotExistException
  {
    refresh(200);
    refresh(300);
    refresh(500);
    refresh(600);
    refresh(700);
    refresh(800);
  }


  private static int baseSize_ = 115; // This was 108 for V5R1.

  /**
   * This refresh method does all the work.
  **/
  private void refresh(int whichFormat)
    throws AS400Exception,
           AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
  {
    String format = null;
    int len = 0;
    switch(whichFormat)
    {
      case 100:
        format = "PTFR0100";
        len = 200; // Updated length to prevent recursive loop. Was 112. (Old comment: Why isn't this 108? Hmm.)
        break;
      case 200:
        format = "PTFR0200";
        len = baseSize_+12+chunkSize_; // 108+12+(36*numberOfCoverLetters)
        break;
      case 300:
        format = "PTFR0300";
        len = baseSize_+12+chunkSize_; // 108+12+(35*numberOfRequisites)
        break;
      case 500:
        format = "PTFR0500";
        len = baseSize_+12+chunkSize_; // 108+12+(33*numberOfDependents)
        break;
      case 600:
        format = "PTFR0600";
        len = baseSize_+12+chunkSize_; // 108+12+(7*numberOfAPARs)
        break;
      case 700:
        format = "PTFR0700";
        len = baseSize_+12+chunkSize_; // 108+12+(symptomStringData)
        break;
      case 800:
        format = "PTFR0800";
        len = baseSize_+12+chunkSize_; // 108+12+(29*numberOfExitPrograms)
        break;
//      case 900:
//        format = "PTFR0900";
//        len = baseSize_+12+chunkSize_; // 108+12+(30*numberOfPreconditions)
      default:
        format = "PTFR0100";
        len = baseSize_+chunkSize_;
        break;
    }
    int ccsid = system_.getCcsid();
    ConvTable conv = ConvTable.getTable(ccsid, null);

    ProgramParameter[] parms = new ProgramParameter[5];
    parms[0] = new ProgramParameter(len); // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(len)); // length of receiver variable
    byte[] ptfInfo = new byte[50];
    AS400Text text7 = new AS400Text(7, ccsid, system_);
    AS400Text text6 = new AS400Text(6, ccsid, system_);
    AS400Text text25 = new AS400Text(25, ccsid, system_);
    text7.toBytes(ptfID_, ptfInfo, 0);
    text7.toBytes(productID_, ptfInfo, 7);
    text6.toBytes(ptfReleaseLevel_, ptfInfo, 14);
    ptfInfo[24] = (byte)0xF0; // '0' means close the PTF database when query is done, '1' means leave it open.
    text25.toBytes(" ", ptfInfo, 25);
    parms[2] = new ProgramParameter(ptfInfo); // PTF information
    parms[3] = new ProgramParameter(conv.stringToByteArray(format)); // format name
    parms[4] = new ProgramParameter(BinaryConverter.intToByteArray(0)); // error code

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QPZRTVFX.PGM", parms);
    // Assumption of thread-safety defaults to false, or to the value of the "threadSafe" system property (if it has been set).
    //pc.setThreadSafe(false);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }

    byte[] output = parms[0].getOutputData();
    int bytesReturned = BinaryConverter.byteArrayToInt(output, 0);
    int bytesAvailable = BinaryConverter.byteArrayToInt(output, 4);
    if (bytesReturned < bytesAvailable)
    {
      chunkSize_ = bytesAvailable;
      refresh(whichFormat);
      return;
    }
    returnedProductID_ = conv.byteArrayToString(output, 12, 7);
    returnedReleaseLevel_ = conv.byteArrayToString(output, 26, 6);
    ptfProductOption_ = conv.byteArrayToString(output, 32, 4);
    ptfProductLoad_ = conv.byteArrayToString(output, 36, 4);
    loadedStatus_ = conv.byteArrayToString(output, 40, 1);
    hasCoverLetter_ = (output[41] == (byte)0xF1);
    ptfOnOrder_ = (output[42] == (byte)0xF1);
    saveFileExists_ = (output[43] == (byte)0xF1);
    if (saveFileExists_)
    {
      String fileName = conv.byteArrayToString(output, 44, 10).trim();
      String fileLibrary = conv.byteArrayToString(output, 54, 10).trim();
      saveFile_ = QSYSObjectPathName.toPath(fileLibrary, fileName, "SAVF");
    }
    else
    {
      saveFile_ = "";
    }
    iplRequired_ = conv.byteArrayToString(output, 64, 1); // also known as PTF type
    iplAction_ = (int)(output[65] & 0x000F); // EBCDIC 0xF0 = '0', 0xF1 = '1', etc.
    actionPending_ = (output[66] == (byte)0xF1);
    actionRequired_ = conv.byteArrayToString(output, 67, 1);
    isPTFReleased_ = (output[68] == (byte)0xF1);
    targetRelease_ = conv.byteArrayToString(output, 69, 6);
    supersedingPTF_ = conv.byteArrayToString(output, 75, 7).trim();
    currentIPLSource_ = conv.byteArrayToString(output, 82, 1);
    minimumLevel_ = conv.byteArrayToString(output, 83, 2);
    maximumLevel_ = conv.byteArrayToString(output, 85, 2);
    // formatInformationAvailable_;
    String d = conv.byteArrayToString(output, 88, 13);
    // Parse the date
    if (d.trim().length() == 13)
    {
      Calendar cal = Calendar.getInstance();
      cal.clear();
      cal.set(Integer.parseInt(d.substring(0,3)) + 1900, // year
              Integer.parseInt(d.substring(3,5))-1,     // month is zero based
              Integer.parseInt(d.substring(5,7)),       // day
              Integer.parseInt(d.substring(7,9)),       // hour
              Integer.parseInt(d.substring(9,11)),      // minute
              Integer.parseInt(d.substring(11,13)));    // second
      statusDate_ = cal.getTime();
    }
    else
    {
      statusDate_ = null;
    }
    licGroup_ = conv.byteArrayToString(output, 101, 7).trim();
    if (output.length >= 115)
    {
      // V5R2 and higher
      //supersededByPTFID_ = conv.byteArrayToString(output, 108, 7).trim();

      if (output.length >= 130)
      {
        // V5R3 and higher
        currentServerIPLSource_ = conv.byteArrayToString(output, 115, 1).trim();
        serverIPLRequired_ = (int)(output[116] & 0x000F); // EBCDIC 0xF0 = '0', 0xF1 = '1', etc.
        creationDateAndTime_ = conv.byteArrayToString(output, 117, 13).trim();
      }
    }
    loaded_ = true;

    if (whichFormat == 200)
    {
      int offset = BinaryConverter.byteArrayToInt(output, 8);
      int entryOffset = BinaryConverter.byteArrayToInt(output, offset);
      offset += 4;
      int numberOfNLVs = BinaryConverter.byteArrayToInt(output, offset);
      offset += 4;
      int entryLength = BinaryConverter.byteArrayToInt(output, offset);
      coverLetters_ = new PTFCoverLetter[numberOfNLVs];
      for (int i=0; i<numberOfNLVs; ++i)
      {
        offset = entryOffset + (i*entryLength);
        String nlv = conv.byteArrayToString(output, offset, 4);
        offset += 4;
        String fileName = conv.byteArrayToString(output, offset, 10).trim();
        offset += 10;
        String fileLibrary = conv.byteArrayToString(output, offset, 10).trim();
        offset += 10;
        String fileMember = conv.byteArrayToString(output, offset, 10).trim();
        offset += 10;
        int preInstructions = (int)(output[offset++] & 0x000F);
        int postInstructions = (int)(output[offset] & 0x000F);
        String path = QSYSObjectPathName.toPath(fileLibrary, fileName, fileMember, "MBR");
        coverLetters_[i] = new PTFCoverLetter(system_, nlv, path, preInstructions, postInstructions);
      }
      loaded200_ = true;
    }
    else if (whichFormat == 300) // Pre-requisites of this PTF
    {
      int offset = BinaryConverter.byteArrayToInt(output, 8);
      int entryOffset = BinaryConverter.byteArrayToInt(output, offset);
      offset += 4;
      int numReqs = BinaryConverter.byteArrayToInt(output, offset);
      offset += 4;
      int entryLength = BinaryConverter.byteArrayToInt(output, offset);
      requisites_ = new PTF[numReqs];
      //isDependent_ = false;
      //isConditional_ = false;
      for (int i=0; i<numReqs; ++i)
      {
        offset = entryOffset + (i*entryLength);
        String reqProdID = conv.byteArrayToString(output, offset, 7);
        offset += 7;
        String reqPTFID = conv.byteArrayToString(output, offset, 7);
        offset += 7;
        String release = conv.byteArrayToString(output, offset, 6);
        offset += 6;
        String reqMinLvl = conv.byteArrayToString(output, offset, 2);
        offset += 2;
        String reqMaxLvl = conv.byteArrayToString(output, offset, 2);
        offset += 2;
        byte prereqType = output[offset++];
        boolean type = (prereqType == (byte)0xF1); // '1' is a pre-req; '2' is a co-req.
        boolean cond = (output[offset++] == (byte)0xF1); // '1' is conditional; '0' is not.
        boolean required = (output[offset++] == (byte)0xF1); // '1' is required; '0' is not.
        String option = conv.byteArrayToString(output, offset, 4);
        offset += 4;
        String reqLoadID = conv.byteArrayToString(output, offset, 4);
        requisites_[i] = new PTF(system_, reqProdID, reqPTFID, release, option, reqLoadID, reqMinLvl, reqMaxLvl, type, cond, required);
        if (type)
        {
          isDependent_ = true;
        }
        else
        {
          isCoRequisite_ = true;
        }
        if (cond)
        {
          isConditional_ = true;
        }
      }
      loaded300_ = true;
    }
    else if (whichFormat == 500) // Dependents of this PTF
    {
      int offset = BinaryConverter.byteArrayToInt(output, 8);
      int entryOffset = BinaryConverter.byteArrayToInt(output, offset);
      offset += 4;
      int numDeps = BinaryConverter.byteArrayToInt(output, offset);
      offset += 4;
      int entryLength = BinaryConverter.byteArrayToInt(output, offset);
      dependents_ = new PTF[numDeps];
      //isPreRequisite_ = false;
      for (int i=0; i<numDeps; ++i)
      {
        offset = entryOffset + (i*entryLength);
        String depProdID = conv.byteArrayToString(output, offset, 7);
        offset += 7;
        String depPTFID = conv.byteArrayToString(output, offset, 7);
        offset += 7;
        String release = conv.byteArrayToString(output, offset, 6);
        offset += 6;
        String depMinLvl = conv.byteArrayToString(output, offset, 2);
        offset += 2;
        String depMaxLvl = conv.byteArrayToString(output, offset, 2);
        offset += 2;
        byte depType = output[offset++];
        boolean type = (depType == (byte)0xF1); // '1' is a pre-req; '2' is a co-req.
        String option = conv.byteArrayToString(output, offset, 4);
        offset += 4;
        String depLoadID = conv.byteArrayToString(output, offset, 4);
        dependents_[i] = new PTF(system_, depProdID, depPTFID, release, option, depLoadID, depMinLvl, depMaxLvl, type);
        if (type)
        {
          isPreRequisite_ = true;
        }
        else
        {
          isCoRequisite_ = true;
        }
      }
      loaded500_ = true;
    }
    else if (whichFormat == 600)
    {
      int offset = BinaryConverter.byteArrayToInt(output, 8);
      int entryOffset = BinaryConverter.byteArrayToInt(output, offset);
      offset += 4;
      int numAPARs = BinaryConverter.byteArrayToInt(output, offset);
      offset += 4;
      int entryLength = BinaryConverter.byteArrayToInt(output, offset);
      apars_ = new String[numAPARs];
      for (int i=0; i<numAPARs; ++i)
      {
        offset = entryOffset + (i*entryLength);
        apars_[i] = conv.byteArrayToString(output, offset, 7);
      }
      loaded600_ = true;
    }
    else if (whichFormat == 700)
    {
      int offset = BinaryConverter.byteArrayToInt(output, 8);
      int entryOffset = BinaryConverter.byteArrayToInt(output, offset);
      offset += 4;
      int numStrings = BinaryConverter.byteArrayToInt(output, offset);
      offset += 4;
      int entryLength = BinaryConverter.byteArrayToInt(output, offset);
      symptoms_ = new String[numStrings];
      for (int i=0; i<numStrings; ++i)
      {
        offset = entryOffset + (i*entryLength);
        int symptomOffset = BinaryConverter.byteArrayToInt(output, offset);
        offset += 4;
        int symptomLength = BinaryConverter.byteArrayToInt(output, offset);
        symptoms_[i] = conv.byteArrayToString(output, symptomOffset, symptomLength);
      }
      loaded700_ = true;
    }
    else if (whichFormat == 800)
    {
      int offset = BinaryConverter.byteArrayToInt(output, 8);
      int entryOffset = BinaryConverter.byteArrayToInt(output, offset);
      offset += 4;
      int numProgs = BinaryConverter.byteArrayToInt(output, offset);
      offset += 4;
      int entryLength = BinaryConverter.byteArrayToInt(output, offset);
      exitPrograms_ = new PTFExitProgram[numProgs];
      for (int i=0; i<numProgs; ++i)
      {
        offset = entryOffset + (i*entryLength);
        int userDataOffset = BinaryConverter.byteArrayToInt(output, offset);
        offset += 4;
        int userDataLength = BinaryConverter.byteArrayToInt(output, offset);
        offset += 4;
        String name = conv.byteArrayToString(output, offset, 10).trim();
        offset += 10;
        String lib = conv.byteArrayToString(output, offset, 10).trim();
        offset += 10;
        String path = QSYSObjectPathName.toPath(lib, name, "PGM");
        String runOption = conv.byteArrayToString(output, offset, 1);
        String userData = conv.byteArrayToString(output, userDataOffset, userDataLength);
        exitPrograms_[i] = new PTFExitProgram(path, runOption, userData);
      }
      loaded800_ = true;
    }
/*    else if (whichFormat == 900)
    {
      int offset = BinaryConverter.byteArrayToInt(output, 8);
      if (offset == 0)
      {
        preconditions_ = new PTFPrecondition[0];
      }
      else
      {
      System.out.println("offset = "+offset);
      int entryOffset = BinaryConverter.byteArrayToInt(output, offset);
      offset += 4;
      System.out.println("entryoffset = "+entryOffset);
      int numConds = BinaryConverter.byteArrayToInt(output, offset);
      offset += 4;
      System.out.println("numConds = "+numConds);
      int entryLength = BinaryConverter.byteArrayToInt(output, offset);
      System.out.println("entrylength = "+entryLength);
      preconditions_ = new PTFPrecondition[numConds];
      for (int i=0; i<numConds; ++i)
      {
        offset = entryOffset + (i*entryLength);
        String preCondType = conv.byteArrayToString(output, offset, 10).trim();
        offset += 10;
        String preCondName = conv.byteArrayToString(output, offset, 10).trim();
        offset += 10;
        String preCondLib = conv.byteArrayToString(output, offset, 10).trim();
//        String path = QSYSObjectPathName.toPath(preCondLib, preCondName, preCondType);
        preconditions_[i] = new PTFPrecondition(preCondLib, preCondName, preCondType);
      }
      }
      loaded900_ = true;
    }
*/    

  }


  /**
   * Returns a String representation of this PTF in the format "PTF ID/release level/product ID".
   * @return The String representing this PTF.
  **/
  public String toString()
  {
    StringBuffer buf = new StringBuffer(ptfID_);
    buf.append('/');
    buf.append(ptfReleaseLevel_);
    buf.append('/');
    buf.append(productID_);
    return buf.toString();
  }
}
